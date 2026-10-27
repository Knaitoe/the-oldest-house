package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.*;

/** Four ordinary lives behind four existing fragments. The other fragments end where they did. */
public final class DomesticLives0133 {
    public static final String STATE="domestic_lives_0128";
    public record Home(String id,LabyrinthPlace hall,int x0,int x1,int z0,int z1,BlockPos opening,String paper){
        public BoundingBox local(){return new BoundingBox(x0,-1,z0,x1,4,z1);}
        public AABB area(BlockPos b){return new AABB(b.getX()+x0,b.getY()-1,b.getZ()+z0,b.getX()+x1+1,b.getY()+5,b.getZ()+z1+1);}
    }
    public static final List<Home> HOMES=List.of(
            new Home("family",LabyrinthPlace.STRAIGHT_HALL,-20,-7,-26,-14,new BlockPos(-7,0,-20),"family_paper"),
            new Home("students",LabyrinthPlace.BENT_HALL,7,20,-18,-7,new BlockPos(7,0,-14),"student_paper"),
            new Home("drinker",LabyrinthPlace.BENT_HALL,-8,1,-36,-24,new BlockPos(-8,0,-29),"smoked_paper"),
            new Home("grandmother",LabyrinthPlace.CROSS_HALL,-19,-8,-8,3,new BlockPos(-10,0,-8),"rose_paper"));
    /** A fixed discovery for this world, rather than extending most ordinary snippets. */
    public static Set<String> selection(long seed){
        var random=new Random(seed^0x4d3e91b80c92a167L);var rows=new ArrayList<>(HOMES);Collections.shuffle(rows,random);
        var chosen=new LinkedHashSet<String>();var first=rows.getFirst();chosen.add(first.id);
        if(random.nextInt(4)==0)for(var h:rows)if(h.hall!=first.hall){chosen.add(h.id);break;}
        return Set.copyOf(chosen);
    }
    public static boolean selected(ServerLevel l,BlockPos b,Home h){
        // Never seal an extension already made in 0.4.128, including on an old save.
        var state=LabyrinthData.get(l.getServer()).state(STATE);String key=key(b,h);
        return state.getBoolean(key)||state.getBoolean(key+":chosen")||state.contains(key+":cursor")||selection(l.getSeed()).contains(h.id);
    }
    public static BlockPos approach(Home h){return switch(h.id){
        case "family"->new BlockPos(-1,0,-18);case "students"->new BlockPos(1,0,-12);
        case "drinker"->new BlockPos(-14,0,-27);default->new BlockPos(-8,0,-14);
    };}
    public static BoundingBox workBounds(Home h){
        var a=approach(h);return new BoundingBox(Math.min(h.x0,a.getX()-1),-1,Math.min(h.z0,a.getZ()-1),Math.max(h.x1,a.getX()+1),4,Math.max(h.z1,a.getZ()+1));
    }
    public static AABB workArea(BlockPos b,Home h){var a=workBounds(h);return new AABB(b.getX()+a.minX(),b.getY()-1,b.getZ()+a.minZ(),b.getX()+a.maxX()+1,b.getY()+5,b.getZ()+a.maxZ()+1);}
    public record Edit(BlockPos at,BlockState next){}
    private static final Map<String,List<Edit>> PLANS=new HashMap<>();
    private DomesticLives0133(){}
    public static BoundingBox extend(LabyrinthPlace p,BoundingBox old){
        int x0=old.minX(),x1=old.maxX(),z0=old.minZ(),z1=old.maxZ(),y0=old.minY(),y1=old.maxY();
        for(var h:HOMES)if(h.hall==p){x0=Math.min(x0,h.x0);x1=Math.max(x1,h.x1);z0=Math.min(z0,h.z0);z1=Math.max(z1,h.z1);y0=Math.min(y0,-1);y1=Math.max(y1,4);}
        return new BoundingBox(x0,y0,z0,x1,y1,z1);
    }
    public static String key(BlockPos base,Home h){return h.id+":"+base.asLong();}
    private static void put(Map<BlockPos,BlockState> e,int x,int y,int z,BlockState s){e.put(new BlockPos(x,y,z),s);}
    private static void fit(Map<BlockPos,BlockState> e,int x,int y,int z,UncannyFurnitureBlock.Kind k,Direction f){put(e,x,y,z,UncannyFurnitureBlock.of(k,f));}
    private static void fit(Map<BlockPos,BlockState> e,int x,int y,int z,UncannyFurnitureBlock.Kind k){fit(e,x,y,z,k,Direction.NORTH);}
    private static void handled(Map<BlockPos,BlockState> e,int x,int y,int z,DomesticFittingBlock.Kind k){put(e,x,y,z,DomesticFittingBlock.of(k,Direction.NORTH));}
    private static void handled(Map<BlockPos,BlockState> e,int x,int y,int z,DomesticFittingBlock.Kind k,Direction f){put(e,x,y,z,DomesticFittingBlock.of(k,f));}
    private static void closet(Map<BlockPos,BlockState> e,int x,int z,Direction f){handled(e,x,0,z,DomesticFittingBlock.Kind.CLOSET_LOWER,f);handled(e,x,1,z,DomesticFittingBlock.Kind.CLOSET_UPPER,f);}
    private static void interior(Map<BlockPos,BlockState> e,int x,int y,int z,VignetteInteriorBlock.Kind k,Direction f){put(e,x,y,z,VignetteInteriorBlock.of(k,f));}
    private static void wallX(Map<BlockPos,BlockState> e,Home h,int x,int z0,int z1,int... gaps){
        for(int z=z0;z<=z1;z++)for(int y=0;y<4;y++){boolean gap=false;for(int g:gaps)if(g==z)gap=true;put(e,x,y,z,gap?Blocks.AIR.defaultBlockState():DomesticRegistry.material(h.paper).defaultBlockState());}
    }
    private static void wallZ(Map<BlockPos,BlockState> e,Home h,int z,int x0,int x1,int... gaps){
        for(int x=x0;x<=x1;x++)for(int y=0;y<4;y++){boolean gap=false;for(int g:gaps)if(g==x)gap=true;put(e,x,y,z,gap?Blocks.AIR.defaultBlockState():DomesticRegistry.material(h.paper).defaultBlockState());}
    }
    private static void bed(Map<BlockPos,BlockState> e,int x,int z,Block type){
        var s=type.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);put(e,x,0,z,s.setValue(BedBlock.PART,BedPart.FOOT));put(e,x,0,z-1,s.setValue(BedBlock.PART,BedPart.HEAD));
    }
    private static void lamp(Map<BlockPos,BlockState> e,int x,int z){put(e,x,3,z,Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HANGING,true));}
    private static void window(Map<BlockPos,BlockState> e,int x,int z,Direction inward){
        fit(e,x,1,z,UncannyFurnitureBlock.Kind.WINDOW_FRAME,inward);
        var behind=new BlockPos(x,1,z).relative(inward.getOpposite());
        put(e,behind.getX(),behind.getY(),behind.getZ(),Blocks.BLACK_WOOL.defaultBlockState());
    }
    public static List<Edit> plan(Home h){return PLANS.computeIfAbsent(h.id,ignored->{
        var e=new LinkedHashMap<BlockPos,BlockState>();
        for(int y=-1;y<=4;y++)for(int z=h.z0;z<=h.z1;z++)for(int x=h.x0;x<=h.x1;x++){
            boolean edge=x==h.x0||x==h.x1||z==h.z0||z==h.z1;
            var s=y==-1?DomesticRegistry.material(h.id.equals("family")&&x>=-13&&z<=-21||h.id.equals("grandmother")&&x>=-13&&z<=-5?"linoleum":"parquet").defaultBlockState():y==4?Blocks.BIRCH_PLANKS.defaultBlockState():edge?DomesticRegistry.material(h.paper).defaultBlockState():Blocks.AIR.defaultBlockState();
            put(e,x,y,z,s);
        }
        switch(h.id){
            case "family"->{
                wallX(e,h,-14,-25,-15,-22,-18);wallZ(e,h,-20,-19,-15);
                for(int i=0;i<3;i++)bed(e,-19+i*2,-24,new Block[]{Blocks.ORANGE_BED,Blocks.LIGHT_BLUE_BED,Blocks.PURPLE_BED}[i]);
                fit(e,-18,0,-21,UncannyFurnitureBlock.Kind.WARDROBE);interior(e,-16,0,-21,VignetteInteriorBlock.Kind.TOY_CUPBOARD,Direction.SOUTH);
                bed(e,-18,-18,Blocks.GREEN_BED);bed(e,-17,-18,Blocks.GREEN_BED);
                closet(e,-19,-16,Direction.EAST);handled(e,-18,0,-15,DomesticFittingBlock.Kind.PACKED_CLOTHES);
                fit(e,-19,0,-19,UncannyFurnitureBlock.Kind.BEDSIDE);handled(e,-19,1,-19,DomesticFittingBlock.Kind.PHOTO_FAMILY);fit(e,-16,0,-19,UncannyFurnitureBlock.Kind.BEDSIDE);fit(e,-16,1,-19,UncannyFurnitureBlock.Kind.DESK_LAMP);fit(e,-15,0,-16,UncannyFurnitureBlock.Kind.WARDROBE,Direction.WEST);
                interior(e,-15,1,-21,VignetteInteriorBlock.Kind.HILL_HEIGHT_MARKS,Direction.WEST);
                for(int x=-13;x<=-9;x++)interior(e,x,0,-25,VignetteInteriorBlock.Kind.HOLIDAY_LARDER,Direction.NORTH);
                interior(e,-12,0,-25,VignetteInteriorBlock.Kind.HOLIDAY_SINK,Direction.NORTH);fit(e,-8,0,-25,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);handled(e,-13,1,-25,DomesticFittingBlock.Kind.BREAKFAST);
                for(int x=-11;x<=-10;x++)fit(e,x,0,-22,UncannyFurnitureBlock.Kind.TABLE);
                for(int x:new int[]{-11,-10}){fit(e,x,0,-21,UncannyFurnitureBlock.Kind.DINER_CHAIR);fit(e,x,0,-23,UncannyFurnitureBlock.Kind.DINER_CHAIR,Direction.SOUTH);}
                fit(e,-9,0,-22,UncannyFurnitureBlock.Kind.DINER_CHAIR,Direction.WEST);
                fit(e,-12,0,-15,UncannyFurnitureBlock.Kind.SOFA_LEFT);fit(e,-11,0,-15,UncannyFurnitureBlock.Kind.SOFA_RIGHT);fit(e,-11,0,-17,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                window(e,-8,-17,Direction.WEST);lamp(e,-11,-19);lamp(e,-17,-22);lamp(e,-17,-17);
            }
            case "students"->{
                wallX(e,h,14,-17,-8,-15,-10);wallZ(e,h,-13,15,19);
                bed(e,16,-16,Blocks.BLUE_BED);bed(e,19,-16,Blocks.RED_BED);bed(e,18,-10,Blocks.BLACK_BED);
                fit(e,15,0,-14,UncannyFurnitureBlock.Kind.DESK);put(e,15,1,-14,DomesticFittingBlock.of(DomesticFittingBlock.Kind.DESK_REMNANTS,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,1));
                fit(e,19,0,-14,UncannyFurnitureBlock.Kind.BOOKCASE);handled(e,17,0,-14,DomesticFittingBlock.Kind.BOOK_BOX);
                fit(e,15,0,-8,UncannyFurnitureBlock.Kind.DESK);fit(e,15,1,-8,UncannyFurnitureBlock.Kind.DESK_LAMP);handled(e,19,0,-8,DomesticFittingBlock.Kind.PACKED_CLOTHES);
                closet(e,19,-12,Direction.WEST);handled(e,12,0,-17,DomesticFittingBlock.Kind.RECORD_PLAYER);fit(e,13,0,-17,UncannyFurnitureBlock.Kind.BOOKCASE);
                for(int x=8;x<=10;x++)interior(e,x,0,-17,VignetteInteriorBlock.Kind.TRAILER_CUPBOARD,Direction.NORTH);
                interior(e,8,0,-17,VignetteInteriorBlock.Kind.TRAILER_SINK,Direction.NORTH);fit(e,11,0,-17,UncannyFurnitureBlock.Kind.FRIDGE,Direction.SOUTH);
                fit(e,9,0,-8,UncannyFurnitureBlock.Kind.SOFA_LEFT);fit(e,10,0,-8,UncannyFurnitureBlock.Kind.SOFA_RIGHT);fit(e,10,0,-10,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                handled(e,8,0,-9,DomesticFittingBlock.Kind.EMPTY_BOTTLES);window(e,19,-9,Direction.WEST);lamp(e,11,-13);lamp(e,17,-14);lamp(e,17,-9);
            }
            case "drinker"->{
                wallZ(e,h,-32,-7,0,-2,-1);
                bed(e,-6,-34,Blocks.GRAY_BED);fit(e,-3,0,-35,UncannyFurnitureBlock.Kind.WARDROBE,Direction.SOUTH);
                fit(e,-7,0,-34,UncannyFurnitureBlock.Kind.BEDSIDE);handled(e,-7,1,-34,DomesticFittingBlock.Kind.FOLDED_SHIRT);
                for(int z=-31;z<=-29;z++)interior(e,0,0,z,VignetteInteriorBlock.Kind.TRAILER_CUPBOARD,Direction.WEST);
                interior(e,0,0,-30,VignetteInteriorBlock.Kind.TRAILER_SINK,Direction.WEST);handled(e,0,1,-29,DomesticFittingBlock.Kind.DIRTY_DISHES);fit(e,0,0,-28,UncannyFurnitureBlock.Kind.FRIDGE,Direction.WEST);
                fit(e,-1,0,-26,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.SOUTH);fit(e,-2,0,-28,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                handled(e,0,0,-26,DomesticFittingBlock.Kind.EMPTY_BOTTLES);handled(e,-7,0,-28,DomesticFittingBlock.Kind.BOTTLE_RETURNS);
                put(e,-1,-1,-26,DomesticRegistry.material("parquet_worn").defaultBlockState());put(e,-1,-1,-28,DomesticRegistry.material("parquet_worn").defaultBlockState());put(e,-1,-1,-30,DomesticRegistry.material("parquet_worn").defaultBlockState());window(e,0,-27,Direction.WEST);lamp(e,-4,-29);lamp(e,-3,-34);
            }
            case "grandmother"->{
                wallX(e,h,-14,-7,2,-4,0);wallZ(e,h,-2,-18,-15);
                bed(e,-18,-5,Blocks.PINK_BED);fit(e,-15,0,-7,UncannyFurnitureBlock.Kind.WARDROBE,Direction.SOUTH);fit(e,-17,0,-6,UncannyFurnitureBlock.Kind.BEDSIDE);fit(e,-17,1,-6,UncannyFurnitureBlock.Kind.DESK_LAMP);
                fit(e,-18,0,0,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.SOUTH);fit(e,-16,0,0,UncannyFurnitureBlock.Kind.ARMCHAIR,Direction.SOUTH);
                fit(e,-17,0,2,UncannyFurnitureBlock.Kind.COUNTER);handled(e,-17,1,2,DomesticFittingBlock.Kind.RADIO,Direction.SOUTH);
                fit(e,-15,0,2,UncannyFurnitureBlock.Kind.SIDEBOARD);fit(e,-15,1,2,UncannyFurnitureBlock.Kind.CUP);interior(e,-15,0,1,VignetteInteriorBlock.Kind.SEWING_BASKET,Direction.SOUTH);window(e,-18,1,Direction.EAST);
                fit(e,-15,0,-3,UncannyFurnitureBlock.Kind.BEDSIDE);put(e,-15,1,-3,DomesticFittingBlock.of(DomesticFittingBlock.Kind.PHOTO_COUPLE,Direction.SOUTH).setValue(DomesticFittingBlock.STAGE,1));
                for(int x=-13;x<=-11;x++)interior(e,x,0,-7,VignetteInteriorBlock.Kind.HOLIDAY_LARDER,Direction.NORTH);
                interior(e,-13,0,-7,VignetteInteriorBlock.Kind.HOLIDAY_SINK,Direction.NORTH);fit(e,-9,0,-5,UncannyFurnitureBlock.Kind.FRIDGE,Direction.WEST);
                fit(e,-11,0,-2,UncannyFurnitureBlock.Kind.TABLE);fit(e,-12,0,-2,UncannyFurnitureBlock.Kind.DINER_CHAIR,Direction.EAST);fit(e,-11,0,-1,UncannyFurnitureBlock.Kind.DINER_CHAIR);
                lamp(e,-11,-5);lamp(e,-16,-4);lamp(e,-16,0);
            }
            default->throw new IllegalArgumentException(h.id);
        }
        // Extend a free edge of the snippet into a two-wide passage, around its
        // original counter/bed and finite container, rather than through them.
        int x0,x1,z0,z1;boolean sideways=!h.id.equals("grandmother");
        switch(h.id){
            case "family"->{x0=-6;x1=-3;z0=-21;z1=-21;}
            case "students"->{x0=3;x1=6;z0=-15;z1=-15;}
            case "drinker"->{x0=-12;x1=-9;z0=-30;z1=-30;}
            default->{x0=-11;x1=-11;z0=-12;z1=-9;}
        }
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            put(e,x,-1,z,DomesticRegistry.material(h.id.equals("family")||h.id.equals("grandmother")?"linoleum":"parquet").defaultBlockState());
            for(int y=0;y<4;y++)put(e,x,y,z,Blocks.AIR.defaultBlockState());put(e,x,4,z,Blocks.BIRCH_PLANKS.defaultBlockState());
            for(int y=0;y<4;y++)put(e,sideways?x:x-1,y,sideways?z-1:z,DomesticRegistry.material(h.paper).defaultBlockState());
        }
        for(int n=0;n<2;n++)for(int y=-1;y<4;y++){
            var at=h.opening.relative(sideways?Direction.NORTH:Direction.WEST,n).above(y);e.remove(at);
            e.put(at,y==-1?DomesticRegistry.material("parquet").defaultBlockState():Blocks.AIR.defaultBlockState());
        }
        return e.entrySet().stream().map(v->new Edit(v.getKey(),v.getValue())).toList();
    });}
}
