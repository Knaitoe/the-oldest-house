package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.UncannyPropBlock.Kind.*;

/** Immutable 0.4.121 blueprint. Recorded in production; executed only by the native old-save fixture. */
public final class UncannyRooms0121 {
    public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.PAINTED_INTERIOR,LabyrinthPlace.SECOND_WAITRESS,LabyrinthPlace.TUNNEL_PERIPHERY,LabyrinthPlace.MISSING_SLEEP,LabyrinthPlace.LAYERED_MAN,LabyrinthPlace.MOTION_LIGHT);
    public static final BlockPos PARLOR_BOARD=new BlockPos(0,1,-28),PARLOR_DOOR=new BlockPos(8,0,-23);
    public static final BlockPos DINER_TABLE=new BlockPos(5,1,-19),MONITOR_AT=new BlockPos(-5,6,-22),BED_AT=new BlockPos(-6,5,-26),PARENTS_DOOR=new BlockPos(4,5,-15);
    public static final BlockPos CELLAR_DOOR=new BlockPos(0,0,-7),MASS=new BlockPos(0,-5,-20),TOOL=new BlockPos(5,-4,-13),KEY_CAVITY=new BlockPos(0,-4,-20);
    public static final BlockPos INTERCOM=new BlockPos(3,1,-10),FLOOD=new BlockPos(0,4,-20),HINGED=new BlockPos(4,0,-5);
    public static final List<BlockPos> FALSE_DOORS=List.of(new BlockPos(-4,0,-4),new BlockPos(-4,0,-8),new BlockPos(0,0,-2));
    public static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private UncannyRooms0121(){}
    public static boolean isUncanny(LabyrinthPlace p){return p!=null&&PLACES.contains(p);}
    public static BlockPos source(LabyrinthPlace p){return switch(p){
        case PAINTED_INTERIOR->new BlockPos(3,1,-11);case SECOND_WAITRESS->new BlockPos(-6,1,-8);case TUNNEL_PERIPHERY->new BlockPos(-3,1,-5);
        case MISSING_SLEEP->new BlockPos(-6,6,-22);case LAYERED_MAN->new BlockPos(3,1,-5);case MOTION_LIGHT->new BlockPos(3,1,-9);default->throw new IllegalArgumentException(p.id());};}
    public static BlockPos account(LabyrinthPlace p){return switch(p){
        case PAINTED_INTERIOR->new BlockPos(10,1,-10);case SECOND_WAITRESS->new BlockPos(-6,1,-5);case TUNNEL_PERIPHERY->new BlockPos(3,1,-69);
        case MISSING_SLEEP->new BlockPos(3,1,-4);case LAYERED_MAN->new BlockPos(-3,1,-5);case MOTION_LIGHT->new BlockPos(6,1,-3);default->throw new IllegalArgumentException(p.id());};}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var r=p.room();box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR);
        switch(p){case PAINTED_INTERIOR->boarded(l,b);case SECOND_WAITRESS->diner(l,b);case TUNNEL_PERIPHERY->tunnel(l,b);case MISSING_SLEEP->bedroom(l,b);case LAYERED_MAN->cellar(l,b);case MOTION_LIGHT->foyer(l,b);default->throw new IllegalArgumentException(p.id());}
        // The return vestibule opens at z=0; several authored front walls stand at z=-1.
        // Join both cells without deleting the diner's own door farther inside the forecourt.
        box(l,b,0,0,-1,0,1,0,Blocks.AIR);
        Block threshold=switch(p){case PAINTED_INTERIOR->Blocks.COARSE_DIRT;case SECOND_WAITRESS->Blocks.GRAVEL;case TUNNEL_PERIPHERY->Blocks.SMOOTH_STONE;case MISSING_SLEEP->Blocks.SPRUCE_PLANKS;default->Blocks.DARK_OAK_PLANKS;};
        for(int z=-1;z<=0;z++)if(BuildBlocks.state(l,b.offset(0,-1,z)).isAir())at(l,b,0,-1,z,threshold);
        fit(l,b,account(p),LEDGER,Direction.SOUTH);
    }
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),FLAGS);}
    private static void state(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,FLAGS);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){NovelRooms.box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    public static void fit(ServerLevel l,BlockPos b,BlockPos rel,UncannyPropBlock.Kind k,Direction face){state(l,b,rel,UncannyPropBlock.of(k,face));}
    private static void room(ServerLevel l,BlockPos b,int x,int X,int z,int Z,int y,int height,Block wall,Block floor){NovelRooms.room(l,b,x,X,z,Z,y,height,wall,floor);}
    private static void table(ServerLevel l,BlockPos b,int x,int y,int z){NovelRooms.furniture(l,b.offset(x,y,z),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);}
    private static void chair(ServerLevel l,BlockPos b,int x,int y,int z,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),HouseholdFurnitureBlock.Kind.CANE_CHAIR,f);}
    private static void door(ServerLevel l,BlockPos b,BlockPos rel,Direction f,boolean open){box(l,b,rel.getX(),rel.getY(),rel.getZ(),rel.getX(),rel.getY()+1,rel.getZ(),Blocks.AIR);NovelRooms.door(l,b.offset(rel),f,Blocks.DARK_OAK_DOOR,open);}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){state(l,b,new BlockPos(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));at(l,b,x,y+1,z,Blocks.CHAIN);}
    private static void note(ServerLevel l,BlockPos b,LabyrinthPlace p,UncannyPropBlock.Kind kind){fit(l,b,source(p),kind,Direction.SOUTH);}
    private static void boarded(ServerLevel l,BlockPos b){
        room(l,b,-14,14,-38,0,0,9,Blocks.DIRT,Blocks.COARSE_DIRT);
        for(int z=-2;z>=-35;z-=5)for(int x:new int[]{-13,13}){box(l,b,x,0,z,x,2,z,Blocks.ROOTED_DIRT);at(l,b,x,3,z,Blocks.HANGING_ROOTS);}
        room(l,b,-8,8,-28,-7,0,5,Blocks.SPRUCE_PLANKS,Blocks.DARK_OAK_PLANKS);
        box(l,b,-8,0,-15,8,4,-15,Blocks.SPRUCE_PLANKS);door(l,b,new BlockPos(0,0,-15),Direction.SOUTH,true);
        door(l,b,new BlockPos(0,0,-7),Direction.SOUTH,true);
        for(int z:new int[]{-11,-19,-24})for(int x:new int[]{-9,9})for(int y=1;y<=2;y++)fit(l,b,new BlockPos(x,y,z),PAINTED_BOARD,x<0?Direction.EAST:Direction.WEST);
        for(int x=-2;x<=2;x++)for(int y=1;y<=2;y++)fit(l,b,new BlockPos(x,y,-28),PAINTED_BOARD,Direction.SOUTH);
        door(l,b,PARLOR_DOOR,Direction.WEST,false);
        table(l,b,3,0,-11);chair(l,b,3,0,-9,Direction.NORTH);note(l,b,LabyrinthPlace.PAINTED_INTERIOR,PAPER);
        for(int z:new int[]{-18,-24}){NovelRooms.furniture(l,b.offset(-6,0,z),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);chair(l,b,6,0,z,Direction.WEST);}
        box(l,b,-7,0,-26,-5,1,-26,Blocks.BOOKSHELF);at(l,b,-7,1,-10,Blocks.FURNACE);
        table(l,b,10,0,-10);fit(l,b,new BlockPos(11,0,-12),BOX,Direction.SOUTH);
        lamp(l,b,1,3,-10);lamp(l,b,-2,3,-20);
        // This is earth outside every painted opening, including behind the removable rear plank.
        box(l,b,-3,0,-30,3,4,-30,Blocks.DIRT);
    }
    private static void diner(ServerLevel l,BlockPos b){
        box(l,b,-27,-3,-56,27,-2,0,Blocks.STONE);box(l,b,-27,-1,-56,27,-1,0,Blocks.GRAVEL);
        room(l,b,-10,10,-32,-3,0,5,Blocks.WHITE_TERRACOTTA,Blocks.SMOOTH_QUARTZ);
        for(int x=-9;x<=9;x++)for(int z=-31;z<=-4;z++)if((x+z&1)==0)at(l,b,x,-1,z,Blocks.BLACK_CONCRETE);
        // The diner wall stands at z=-3. Place the working entrance in that wall,
        // rather than one block out in the forecourt with solid terracotta behind it.
        door(l,b,new BlockPos(0,0,-3),Direction.SOUTH,true);
        for(int z=-7;z>=-28;z--)at(l,b,-6,0,z,Blocks.SMOOTH_QUARTZ);
        fit(l,b,source(LabyrinthPlace.SECOND_WAITRESS),REGISTER,Direction.EAST);fit(l,b,new BlockPos(-6,1,-22),PIE_CASE,Direction.EAST);
        for(int z:new int[]{-10,-19,-27}){
            for(int x=4;x<=7;x++)state(l,b,new BlockPos(x,0,z),Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
            for(int x=3;x<=8;x++){state(l,b,new BlockPos(x,0,z-2),Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));state(l,b,new BlockPos(x,0,z+2),Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));}
            for(int y=1;y<=3;y++)for(int zz=z-1;zz<=z+1;zz++)at(l,b,11,y,zz,Blocks.GLASS);
        }
        for(int z:new int[]{-8,-16,-24}){box(l,b,-3,4,z,3,4,z,Blocks.SEA_LANTERN);box(l,b,-3,5,z,3,5,z,Blocks.IRON_BARS);}
        for(int z=-43;z>=-48;z--)box(l,b,-27,-1,z,27,-1,z,Blocks.BLACK_CONCRETE);
        for(int x=-26;x<=26;x+=6)box(l,b,x,0,-46,x+2,0,-46,Blocks.YELLOW_CARPET);
        table(l,b,-6,0,-5);fit(l,b,new BlockPos(0,6,-3),SIGN,Direction.SOUTH);
    }
    private static void tunnel(ServerLevel l,BlockPos b){
        room(l,b,-3,3,-73,-1,0,3,Blocks.LIGHT_GRAY_CONCRETE,Blocks.SMOOTH_STONE);
        for(int z=-3;z>=-70;z--){at(l,b,-3,2,z,Blocks.IRON_BARS);at(l,b,3,2,z,Blocks.IRON_BARS);}
        for(int z:new int[]{-12,-28,-44,-60}){
            fit(l,b,new BlockPos(-3,2,z),SIGNAL,Direction.EAST);fit(l,b,new BlockPos(3,1,z-2),SIGN,Direction.WEST);
            at(l,b,0,3,z,Blocks.SMOOTH_STONE);at(l,b,-1,-1,z,Blocks.YELLOW_CONCRETE);at(l,b,1,-1,z,Blocks.YELLOW_CONCRETE);
        }
        at(l,b,-3,0,-5,Blocks.STONE);note(l,b,LabyrinthPlace.TUNNEL_PERIPHERY,CLIPBOARD);
        at(l,b,3,0,-69,Blocks.STONE);box(l,b,-3,3,-73,3,6,-65,Blocks.AIR);box(l,b,-3,6,-73,3,6,-65,Blocks.SMOOTH_STONE);
        lamp(l,b,0,5,-69);
    }
    private static void bedroom(ServerLevel l,BlockPos b){
        room(l,b,-9,9,-29,-1,0,10,Blocks.WHITE_TERRACOTTA,Blocks.SPRUCE_PLANKS);
        box(l,b,-9,4,-29,9,4,-1,Blocks.SPRUCE_PLANKS);
        for(int z=-11;z>=-28;z--){at(l,b,-3,5,z,Blocks.OAK_PLANKS);at(l,b,4,5,z,Blocks.OAK_PLANKS);for(int y=6;y<=8;y++){at(l,b,-3,y,z,Blocks.WHITE_TERRACOTTA);at(l,b,4,y,z,Blocks.WHITE_TERRACOTTA);}}
        for(int i=0;i<5;i++){int z=-4-i;box(l,b,-8,0,z,-6,i-1,z,Blocks.SPRUCE_PLANKS);for(int x=-8;x<=-6;x++)state(l,b,new BlockPos(x,i,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));box(l,b,-8,i+1,z,-6,5,z,Blocks.AIR);}
        box(l,b,-8,4,-11,-4,4,-9,Blocks.SPRUCE_PLANKS);
        door(l,b,new BlockPos(-3,5,-16),Direction.WEST,true);door(l,b,PARENTS_DOOR,Direction.EAST,true);
        NovelRooms.bed(l,b.offset(BED_AT),Blocks.GRAY_BED,Direction.NORTH);NovelRooms.bed(l,b.offset(6,5,-25),Blocks.BLUE_BED,Direction.NORTH);
        table(l,b,-5,5,-22);table(l,b,-6,5,-22);fit(l,b,MONITOR_AT,MONITOR,Direction.SOUTH);fit(l,b,source(LabyrinthPlace.MISSING_SLEEP),PAPER,Direction.SOUTH);
        for(var camera:List.of(new BlockPos(-8,8,-24),new BlockPos(0,8,-28),new BlockPos(8,8,-12),new BlockPos(0,3,-4)))fit(l,b,camera,CAMERA,Direction.SOUTH);
        NovelRooms.furniture(l,b.offset(6,0,-17),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST);table(l,b,-5,0,-20);
        for(int z:new int[]{-8,-18,-27})lamp(l,b,0,8,z);
        for(int x:new int[]{-7,7})for(int y=6;y<=7;y++)at(l,b,x,y,-30,Blocks.BLACK_STAINED_GLASS);
        table(l,b,3,0,-4);
    }
    private static void cellar(ServerLevel l,BlockPos b){
        room(l,b,-7,7,-30,-1,0,4,Blocks.WHITE_TERRACOTTA,Blocks.DARK_OAK_PLANKS);
        room(l,b,-7,7,-29,-9,-5,4,Blocks.GRAY_CONCRETE,Blocks.SMOOTH_STONE);
        box(l,b,-2,-5,-11,2,0,-6,Blocks.AIR);
        for(int i=0;i<6;i++){int z=-6-i,y=-i;box(l,b,-1,-6,z,1,y-1,z,Blocks.STONE);for(int x=-1;x<=1;x++)state(l,b,new BlockPos(x,y,z),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}
        door(l,b,CELLAR_DOOR,Direction.SOUTH,true);
        for(int y=-5;y<=0;y++)state(l,b,new BlockPos(-6,y,-10),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        for(int x:new int[]{-3,3})table(l,b,x,0,-5);
        note(l,b,LabyrinthPlace.LAYERED_MAN,LEDGER);
        box(l,b,-1,-5,-21,1,-3,-19,Blocks.AIR);
        for(int x=-1;x<=1;x++)for(int y=-5;y<=-3;y++)for(int z=-21;z<=-19;z++)fit(l,b,new BlockPos(x,y,z),FLESH,Direction.SOUTH);
        // A real cavity keeps the original key in custody until its front layer is peeled.
        at(l,b,0,-4,-20,Blocks.AIR);
        at(l,b,5,-5,-13,Blocks.SMOOTH_STONE);fit(l,b,TOOL,CLEAVER,Direction.SOUTH);
        at(l,b,-5,-5,-23,Blocks.CAULDRON);fit(l,b,new BlockPos(-6,-3,-23),CAMERA,Direction.EAST);
        lamp(l,b,-3,-2,-14);lamp(l,b,3,2,-5);
    }
    private static void foyer(ServerLevel l,BlockPos b){
        room(l,b,-4,4,-14,-1,0,5,Blocks.SMOOTH_SANDSTONE,Blocks.DARK_OAK_PLANKS);
        room(l,b,-7,7,-25,-15,0,7,Blocks.STONE_BRICKS,Blocks.GRAVEL);
        box(l,b,-3,0,-14,3,3,-14,Blocks.GLASS);door(l,b,new BlockPos(0,0,-14),Direction.SOUTH,false);
        fit(l,b,INTERCOM,BUZZER,Direction.WEST);fit(l,b,source(LabyrinthPlace.MOTION_LIGHT),SCRAWL,Direction.WEST);fit(l,b,FLOOD,FLOODLIGHT,Direction.SOUTH);
        for(int z=-3;z>=-9;z--)box(l,b,5,0,z,7,3,z,Blocks.AIR);
        box(l,b,5,-1,-10,7,-1,-1,Blocks.DARK_OAK_PLANKS);box(l,b,5,4,-10,7,4,-1,Blocks.SMOOTH_SANDSTONE);
        box(l,b,8,0,-10,8,4,-1,Blocks.SMOOTH_SANDSTONE);box(l,b,5,0,-10,7,4,-10,Blocks.SMOOTH_SANDSTONE);
        door(l,b,HINGED,Direction.WEST,false);box(l,b,4,0,-2,7,3,0,Blocks.AIR);
        // The true service door has native hinges. False frames are added only when actually unobserved.
        table(l,b,6,0,-3);lamp(l,b,0,3,-4);
        for(int x:new int[]{-6,6}){at(l,b,x,0,-23,Blocks.FLOWER_POT);at(l,b,x,1,-23,Blocks.DEAD_BUSH);}
    }
}
