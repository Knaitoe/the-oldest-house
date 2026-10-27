package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;

/** A grain bin with domestic wreckage caught in its steelwork, rather than isolated jumping platforms. */
public final class SiloRooms {
    public static final int HEIGHT=64;
    public static final BlockPos HATCH=new BlockPos(0,HEIGHT,-8),ENTRY=new BlockPos(0,0,13);
    public static final List<BlockPos> PAPERS=List.of(new BlockPos(-2,0,-7),new BlockPos(-2,31,-4),new BlockPos(-2,62,-7));
    public record Board(BlockPos at,SiloTimberBlock.Kind kind,Direction facing){}
    public static final List<Board> BOARDS=boards();
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private SiloRooms(){}
    public static AABB bounds(BlockPos b){return new AABB(b.getX()-12,b.getY()-3,b.getZ()-12,b.getX()+13,b.getY()+69,b.getZ()+17);}
    private static List<Board> boards(){
        var out=new LinkedHashMap<BlockPos,Board>();
        // Doors and joists are still attached to the same portions of the old house; none float as numbered steps.
        patch(out,-7,10,-6,1,-4,SiloTimberBlock.Kind.DOOR_LOW,Direction.NORTH);
        patch(out,3,19,-2,6,1,SiloTimberBlock.Kind.TABLE,Direction.EAST);
        patch(out,-3,30,-8,1,-7,SiloTimberBlock.Kind.DOOR_HIGH,Direction.NORTH);
        patch(out,-3,30,-6,-1,-4,SiloTimberBlock.Kind.DOOR_HIGH,Direction.SOUTH);
        patch(out,-5,30,-5,-1,-3,SiloTimberBlock.Kind.TABLE,Direction.NORTH);
        patch(out,-1,42,4,1,5,SiloTimberBlock.Kind.SWING,Direction.SOUTH);
        patch(out,-8,51,-2,-4,-1,SiloTimberBlock.Kind.JOIST,Direction.NORTH);
        patch(out,-2,61,-9,1,-6,SiloTimberBlock.Kind.DOOR_HIGH,Direction.EAST);
        out.remove(new BlockPos(-2,30,-4));out.remove(new BlockPos(-2,61,-7));out.remove(new BlockPos(0,61,-9));
        return List.copyOf(out.values());
    }
    private static void patch(Map<BlockPos,Board> out,int x,int y,int z,int X,int Z,SiloTimberBlock.Kind kind,Direction face){for(int a=x;a<=X;a++)for(int c=z;c<=Z;c++){var p=new BlockPos(a,y,c);out.put(p,new Board(p,kind,face));}}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,net.minecraft.world.level.block.state.BlockState state){BuildBlocks.set(l,b.offset(x,y,z),state,F);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,net.minecraft.world.level.block.state.BlockState state){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),state,F);}
    public static void build(ServerLevel l,BlockPos b){
        box(l,b,-12,-3,-12,12,68,16,Blocks.AIR.defaultBlockState());
        for(int x=-11;x<=11;x++)for(int z=-11;z<=11;z++){
            int radius=x*x+z*z;if(radius>121)continue;
            for(int y=-3;y<0;y++)put(l,b,x,y,z,SiloRegistry.CORN.get().defaultBlockState());
            if(radius>=90)for(int y=0;y<=67;y++)put(l,b,x,y,z,(y%8==0?SiloRegistry.STEEL:SiloRegistry.TIN).get().defaultBlockState());
            if(radius<90){put(l,b,x,64,z,SiloRegistry.TIN.get().defaultBlockState());put(l,b,x,66,z,SiloRegistry.SKY.get().defaultBlockState());put(l,b,x,67,z,SiloRegistry.TIN.get().defaultBlockState());}
        }
        // The native maintenance ladder remains after wood fails: a fall cannot destroy access to the history.
        for(int y=0;y<=61;y++)put(l,b,0,y,-9,SiloRegistry.LADDER.get().defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        for(int x:new int[]{-6,6})for(int z:new int[]{-4,4})for(int y=0;y<=63;y++)if(y<3||y%8!=5)put(l,b,x,y,z,SiloRegistry.STEEL.get().defaultBlockState());
        for(int y:new int[]{10,19,30,42,51,61}){
            for(int x=-10;x<=10;x++)put(l,b,x,y,2,SiloRegistry.STEEL.get().defaultBlockState());
            for(int z=-8;z<=7;z++)put(l,b,7,y,z,SiloRegistry.STEEL.get().defaultBlockState());
        }
        for(var wood:BOARDS)BuildBlocks.set(l,b.offset(wood.at),SiloTimberBlock.of(wood.kind,wood.facing),F);
        // A split kitchen table is wedged around a real beam; its paper survives the surrounding top failing.
        box(l,b,-5,30,-4,1,30,-4,SiloRegistry.STEEL.get().defaultBlockState());
        put(l,b,-2,61,-7,SiloRegistry.STEEL.get().defaultBlockState());
        // A porch swing's old suspension and a bare window frame remain recognizable in the wreckage.
        for(int x:new int[]{-1,1})for(int y=43;y<=48;y++)put(l,b,x,y,4,Blocks.CHAIN.defaultBlockState());
        box(l,b,-6,49,4,6,49,4,SiloRegistry.STEEL.get().defaultBlockState());
        put(l,b,-9,51,-1,SiloRegistry.STEEL.get().defaultBlockState());
        for(int y=52;y<=54;y++)for(int x=-8;x<=-5;x++)if(x==-8||x==-5||y==52||y==54)put(l,b,x,y,-2,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());
        // The twisted auger runs through, rather than sitting beside, the remnants of a kitchen.
        for(int y=0;y<60;y++)put(l,b,4,y,0,(y%3==0?SiloRegistry.TIN:SiloRegistry.STEEL).get().defaultBlockState());
        for(var at:PAPERS)BuildBlocks.set(l,b.offset(at),SiloRegistry.PAPER.get().defaultBlockState(),F);
        BuildBlocks.set(l,b.offset(HATCH),SiloRegistry.HATCH.get().defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.SOUTH),F);
        // A full native vestibule and actual return door, with no opening out into the void.
        box(l,b,-2,-1,9,2,-1,16,SiloRegistry.CORN.get().defaultBlockState());
        for(int z=9;z<=14;z++)for(int y=0;y<=3;y++)for(int x=-2;x<=2;x++)put(l,b,x,y,z,(Math.abs(x)==2||y==3)?SiloRegistry.TIN.get().defaultBlockState():Blocks.AIR.defaultBlockState());
        NovelRooms.safeApproach(l,b.offset(0,0,12));LabyrinthBuilder.entrance(l,b.offset(0,0,12),SiloRegistry.TIN.get().defaultBlockState(),SiloRegistry.CORN.get().defaultBlockState(),SiloRegistry.STEEL.get().defaultBlockState());
        NovelRooms.door(l,b.offset(ENTRY),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
        // In the dark the small lamps belong to old maintenance landings, not an indicated route.
        for(var at:List.of(new BlockPos(-7,9,2),new BlockPos(7,18,-1),new BlockPos(1,60,-7)))put(l,b,at.getX(),at.getY(),at.getZ(),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        put(l,b,-4,31,-4,Blocks.LANTERN.defaultBlockState());
    }
    public static void seed(ServerLevel l,BlockPos b,UUID reader){for(int i=0;i<PAPERS.size();i++)if(l.getBlockEntity(b.offset(PAPERS.get(i))) instanceof SiloPaperBlock.Paper paper)paper.seed(reader,i);}
}
