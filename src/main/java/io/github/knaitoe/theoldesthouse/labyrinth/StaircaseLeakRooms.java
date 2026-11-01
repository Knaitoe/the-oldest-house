package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.Kind.*;

/** A compact saved palette is the room's original template, including its empty cells. */
public final class StaircaseLeakRooms {
    /**
     * The five leaks, then (0.4.214) the household's own rooms behind its doors (StaircaseHousehold). A household room keeps the
     * leak engine's pocket slot, saved template, return and restoration; it stands in the same street.
     */
    public enum Kind { KITCHEN,LAUNDRY,CAR,REPAIR,LATE_KITCHEN,DINING,BEDROOM,MENDING,DAWN;
        public boolean household(){return ordinal()>=DINING.ordinal();}
    }
    public static final BlockPos MIN=new BlockPos(-12,-3,-20),MAX=new BlockPos(12,8,11);
    private static final int SX=25,SY=12,SZ=32;
    public static final int CELLS=SX*SY*SZ;
    private StaircaseLeakRooms(){}
    public static Kind kind(int index){return switch(index){case 0->Kind.KITCHEN;case 12->Kind.LAUNDRY;case 6->Kind.CAR;case 9->Kind.REPAIR;case 32->Kind.LATE_KITCHEN;default->null;};}
    public static BlockPos base(BlockPos origin,int slot){return new BlockPos(origin.getX()+8192+Math.floorMod(slot,64)*64,80,origin.getZ()+8192+Math.floorDiv(slot,64)*64);}
    public static AABB bounds(BlockPos base){return new AABB(Vec3.atLowerCornerOf(base.offset(MIN)),Vec3.atLowerCornerOf(base.offset(MAX).offset(1,1,1)));}
    public static Vec3 spawn(BlockPos base,Kind kind){if(kind.household())return StaircaseHouseholdRooms.spawn(base,kind);return kind==Kind.CAR?new Vec3(base.getX()+1.5,base.getY(),base.getZ()+.5):new Vec3(base.getX()+1.5,base.getY(),base.getZ()-2.5);}
    public static boolean inside(BlockPos base,Kind kind,Vec3 p){if(kind.household())return StaircaseHouseholdRooms.inside(base,kind,p);int half=kind==Kind.REPAIR?4:3;return kind==Kind.CAR?bounds(base).contains(p):p.x>base.getX()-half-.7&&p.x<base.getX()+half+.7&&p.z>base.getZ()-(kind==Kind.KITCHEN||kind==Kind.LATE_KITCHEN?6:7)+.2&&p.z<base.getZ()+.5&&p.y>=base.getY()-.5&&p.y<base.getY()+4.5;}
    public static BlockPos position(BlockPos base,int i){return base.offset(MIN.getX()+i%SX,MIN.getY()+i/(SX*SZ),MIN.getZ()+i/SX%SZ);}
    private static int index(int x,int y,int z){return (y-MIN.getY())*SX*SZ+(z-MIN.getZ())*SX+x-MIN.getX();}

    public static final class Template {
        public final List<BlockState> palette;public final int[] cells;
        private Template(List<BlockState> palette,int[] cells){this.palette=palette;this.cells=cells;}
        public BlockState state(int i){return palette.get(cells[i]);}
        public CompoundTag save(){
            var t=new CompoundTag();var p=new ListTag();for(var s:palette)p.add(NbtUtils.writeBlockState(s));t.put("Palette",p);
            var runs=new ArrayList<Integer>();for(int i=0;i<cells.length;){int id=cells[i],end=i+1;while(end<cells.length&&cells[end]==id)end++;runs.add(id);runs.add(end-i);i=end;}
            t.putIntArray("Runs",runs.stream().mapToInt(Integer::intValue).toArray());return t;
        }
        public static Template load(ServerLevel l,CompoundTag t){
            var palette=new ArrayList<BlockState>();for(var n:t.getList("Palette",Tag.TAG_COMPOUND))palette.add(NbtUtils.readBlockState(l.registryAccess().lookupOrThrow(Registries.BLOCK),(CompoundTag)n));
            int[] runs=t.getIntArray("Runs"),cells=new int[CELLS];int cursor=0;
            for(int i=0;i+1<runs.length;i+=2){int end=Math.min(CELLS,cursor+runs[i+1]);Arrays.fill(cells,cursor,end,runs[i]);cursor=end;}
            if(cursor!=CELLS||palette.isEmpty())throw new IllegalArgumentException("Incomplete saved staircase room template");return new Template(palette,cells);
        }
    }
    static final class Plan {
        final BlockState[] cells=new BlockState[CELLS];Plan(){Arrays.fill(cells,Blocks.AIR.defaultBlockState());}
        void put(int x,int y,int z,BlockState state){if(x>=MIN.getX()&&x<=MAX.getX()&&y>=MIN.getY()&&y<=MAX.getY()&&z>=MIN.getZ()&&z<=MAX.getZ())cells[index(x,y,z)]=state;}
        void box(int x0,int y0,int z0,int x1,int y1,int z1,BlockState state){for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)for(int x=x0;x<=x1;x++)put(x,y,z,state);}
        void prop(int x,int y,int z,StaircaseLeakProps.Kind kind){put(x,y,z,StaircaseLeakProps.state(kind));}
        void door(int x,int z,boolean glass){var s=(glass?Blocks.BIRCH_DOOR:Blocks.OAK_DOOR).defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH);put(x,0,z,s);put(x,1,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
        Template done(){var ids=new LinkedHashMap<BlockState,Integer>();int[] encoded=new int[CELLS];for(int i=0;i<CELLS;i++){var s=cells[i];encoded[i]=ids.computeIfAbsent(s,k->ids.size());}return new Template(List.copyOf(ids.keySet()),encoded);}
    }
    public static Template authored(Kind kind,int[] kitchenCups){
        var p=new Plan();street(p);var wood=Blocks.OAK_PLANKS.defaultBlockState();var paper=StaircaseLeakRegistry.WALLPAPER.get().defaultBlockState();
        if(kind.household()){StaircaseHouseholdRooms.authored(kind,p);return p.done();}
        if(kind==Kind.CAR){car(p,0,0);p.put(0,0,-1,Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState());p.prop(0,1,-1,RADIO);p.prop(1,0,-1,BREAD_BAG);p.prop(-1,1,0,BELT);p.prop(1,0,0,CAR_SEAT);p.put(0,5,0,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,10));return p.done();}
        boolean kitchen=kind==Kind.KITCHEN||kind==Kind.LATE_KITCHEN;int half=kind==Kind.REPAIR?4:3,back=kitchen?-6:-7;
        p.box(-half-1,-1,back,half+1,-1,0,wood);p.box(-half-1,0,back,-half-1,3,0,paper);p.box(half+1,0,back,half+1,3,0,paper);p.box(-half-1,0,back,half+1,3,back,paper);p.box(-half-1,0,0,half+1,3,0,paper);p.box(-half-1,4,back,half+1,4,0,Blocks.BIRCH_PLANKS.defaultBlockState());p.door(0,0,false);
        p.box(-1,1,back,1,2,back,Blocks.GLASS.defaultBlockState());p.prop(0,3,-3,LIGHT);
        if(kitchen){
            p.box(-3,0,-4,3,0,-4,Blocks.SPRUCE_PLANKS.defaultBlockState());p.prop(-3,1,-4,kind==Kind.KITCHEN?SINK_FULL:SINK_EMPTY);
            for(int i=0;i<3;i++){if(kind==Kind.KITCHEN){p.prop(i-2,1,-4,new StaircaseLeakProps.Kind[]{CUP_BLUE,CUP_CREAM,CUP_RED}[i]);p.prop(i-2,2,-5,HOOK);}else{int color=kitchenCups.length==3?Math.max(0,Math.min(2,kitchenCups[i])):i;p.prop(i-2,2,-5,new StaircaseLeakProps.Kind[]{CUP_HUNG_BLUE,CUP_HUNG_CREAM,CUP_HUNG_RED}[color]);}}
            if(kind==Kind.KITCHEN)p.prop(1,1,-4,PLATE);p.prop(3,1,-4,kind==Kind.KITCHEN?GERANIUM:GERANIUM_PLATE);p.put(3,1,-2,StaircaseLeakProps.state(TOWEL).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST));p.prop(2,2,-4,SMALL_LIGHT);
            p.put(-2,0,-1,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));p.put(2,0,-1,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));p.prop(2,1,-1,APRON);
        }else if(kind==Kind.LAUNDRY){
            var bed=Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);p.put(-2,0,-4,bed);p.put(-2,0,-5,bed.setValue(BedBlock.PART,BedPart.HEAD));p.prop(-2,1,-4,LAUNDRY);p.prop(-2,1,-5,SHIRTS);p.prop(-1,0,-6,TROUSERS);
            p.box(2,0,-6,3,0,-6,Blocks.SPRUCE_PLANKS.defaultBlockState());p.prop(2,1,-6,LIST_SPOT);p.put(3,0,-2,Blocks.COMPOSTER.defaultBlockState());
        }else{
            p.put(-2,0,-5,StaircaseLeakProps.state(DRAWER).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH));p.prop(-2,1,-5,CANDLE);p.prop(-4,2,-6,CLOCK);
            p.put(-2,0,-2,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));p.put(2,0,-2,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));p.put(3,0,-2,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));p.prop(3,1,-2,CUP_CREAM);
            p.box(2,1,0,3,2,0,Blocks.GLASS.defaultBlockState());car(p,4,6);
        }
        return p.done();
    }
    /**
     * 0.4.214: the street every room stands in, at night. It replaces the 0.4.49 deepslate box: black overhead and all round
     * (the leak fog closes it in), the yard and washing line behind, the backs of the next terrace across the yard with a lit window
     * or two under slate roofs, the road in front with houses across it, and a street lamp by the pear tree's stump.
     */
    static void street(Plan p){
        var night=Blocks.BLACK_CONCRETE.defaultBlockState();var brick=Blocks.BRICKS.defaultBlockState();var glass=Blocks.GLASS.defaultBlockState();var lit=Blocks.SHROOMLIGHT.defaultBlockState();
        var roof=Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState();
        p.box(-12,-3,-20,12,-1,11,Blocks.DIRT.defaultBlockState());p.box(-11,-1,-19,11,-1,4,Blocks.GRASS_BLOCK.defaultBlockState());
        p.box(-12,0,-20,-12,8,11,night);p.box(12,0,-20,12,8,11,night);p.box(-12,0,-20,12,8,-20,night);p.box(-12,0,11,12,8,11,night);p.box(-12,8,-20,12,8,11,night);
        // The yard: a fence across it, the washing line between two posts and three shirts on it.
        p.box(-8,0,-16,8,0,-16,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST,true).setValue(FenceBlock.EAST,true));p.box(-5,0,-13,-5,2,-13,Blocks.OAK_FENCE.defaultBlockState());p.box(5,0,-13,5,2,-13,Blocks.OAK_FENCE.defaultBlockState());p.box(-4,2,-13,4,2,-13,Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.WEST,true).setValue(TripWireBlock.EAST,true));
        for(int x:new int[]{-3,1,3})p.prop(x,1,-13,WASHING);
        // The next terrace's backs: two windows each, one of them lit somewhere, slate roofs sloping down to the yard.
        int house=0;for(int x:new int[]{-11,-6,-1,4,9}){int w=Math.min(x+3,11);
            p.box(x,0,-19,w,5,-17,brick);
            for(int y:new int[]{1,3}){p.box(x+1,y,-17,Math.min(x+2,w),y+1,-17,glass);if((house*2+y)%5==1)p.box(x+1,y,-18,Math.min(x+2,w),y+1,-18,lit);}
            for(int xx=x;xx<=w;xx++){p.put(xx,6,-17,roof.setValue(StairBlock.FACING,Direction.NORTH));p.put(xx,6,-18,Blocks.DEEPSLATE_TILES.defaultBlockState());p.put(xx,7,-19,roof.setValue(StairBlock.FACING,Direction.SOUTH));p.put(xx,6,-19,Blocks.DEEPSLATE_TILES.defaultBlockState());}
            house++;}
        // The road in front, the kerb, the stump where the pear tree was, a lamp beside it, and the houses across the road.
        p.box(-11,-1,5,11,-1,9,Blocks.STONE_BRICKS.defaultBlockState());p.box(-11,-1,4,11,-1,4,Blocks.SMOOTH_STONE.defaultBlockState());
        p.box(-11,-1,6,11,-1,8,Blocks.GRAY_CONCRETE.defaultBlockState());p.put(-5,-1,7,Blocks.STONE_BRICKS.defaultBlockState());p.put(-5,0,4,Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        p.box(-7,0,4,-7,2,4,Blocks.SPRUCE_FENCE.defaultBlockState());p.put(-7,3,4,Blocks.LANTERN.defaultBlockState());
        p.box(-11,0,10,11,5,10,brick);for(int x=-10;x<=10;x+=4){p.box(x,1,10,x+1,2,10,glass);p.box(x,3,10,x+1,4,10,glass);if(Math.floorMod(x,8)==2)p.box(x,3,11,x+1,4,11,lit);}
        p.box(-11,6,10,11,6,10,roof.setValue(StairBlock.FACING,Direction.SOUTH));
    }
    private static void car(Plan p,int ox,int oz){
        var paint=Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();p.box(ox-2,0,oz-2,ox+2,0,oz+2,paint);p.box(ox-2,1,oz-2,ox-2,2,oz+2,Blocks.BLACK_STAINED_GLASS.defaultBlockState());p.box(ox+2,1,oz-2,ox+2,2,oz+2,Blocks.BLACK_STAINED_GLASS.defaultBlockState());p.box(ox-2,1,oz-2,ox+2,2,oz-2,Blocks.BLACK_STAINED_GLASS.defaultBlockState());p.box(ox-2,1,oz+2,ox+2,2,oz+2,Blocks.BLACK_STAINED_GLASS.defaultBlockState());p.box(ox-2,3,oz-1,ox+2,3,oz+1,paint);
        for(int x:new int[]{-2,2})for(int z:new int[]{-1,1})p.put(ox+x,-1,oz+z,Blocks.BLACK_CONCRETE.defaultBlockState());p.prop(ox-1,0,oz,CAR_SEAT);p.prop(ox+1,0,oz,CAR_SEAT);
        // Clear native seated bodies, not solid blocks inside the passenger cabin.
        p.box(ox-1,0,oz-1,ox+1,1,oz+1,Blocks.AIR.defaultBlockState());p.prop(ox-1,0,oz,CAR_SEAT);p.prop(ox+1,0,oz,CAR_SEAT);
    }
}
