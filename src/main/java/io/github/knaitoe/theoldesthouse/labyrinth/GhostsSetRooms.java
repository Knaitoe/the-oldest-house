package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import static io.github.knaitoe.theoldesthouse.labyrinth.GhostsSetBlock.Kind.*;

/** A furnished Barrett home inside the production shell: downstairs, landing, two bedrooms and a basement. */
public final class GhostsSetRooms {
    public static final BlockPos TABLE=new BlockPos(0,0,-28),MARJORIE_ROOM=new BlockPos(-10,5,-33),
            MERRY_ROOM=new BlockPos(9,5,-32),BASEMENT=new BlockPos(7,-4,-25),MONITOR_AT=new BlockPos(16,0,-33);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static java.util.Set<BlockPos> protectedCells=java.util.Set.of();
    private static java.util.function.BooleanSupplier repairGuard;
    private GhostsSetRooms() {}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s) {var at=b.offset(x,y,z);if(protectedCells.contains(at))return;if(repairGuard!=null)BuildBlocks.protectedSet(l,at,s,F,repairGuard,protectedCells);else BuildBlocks.set(l,at,s,F);}
    static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s) {if(repairGuard==null)BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,F);else for(var c:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1))set(l,b,c.getX(),c.getY(),c.getZ(),s);}
    static void repair(ServerLevel l,BlockPos b,java.util.Set<BlockPos> keep,java.util.function.BooleanSupplier guard){protectedCells=keep;repairGuard=guard;try{build(l,b);}finally{protectedCells=java.util.Set.of();repairGuard=null;}}
    static void prop(ServerLevel l,BlockPos b,int x,int y,int z,GhostsSetBlock.Kind k,Direction f) { set(l,b,x,y,z,k==CASE?LiteraryRegistry.GHOST_CASE.get().defaultBlockState().setValue(GhostsCaseBlock.FACING,f):GhostsSetBlock.of(k,f)); }
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f) {set(l,b,x,y,z,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,k).setValue(HouseholdFurnitureBlock.FACING,f));}
    private static void literary(ServerLevel l,BlockPos b,BlockPos rel,LiteraryPropBlock.Kind k,Direction f){set(l,b,rel.getX(),rel.getY(),rel.getZ(),LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,k).setValue(LiteraryPropBlock.FACING,f));}
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction f){var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,f).setValue(DoorBlock.OPEN,true);set(l,b,x,y,z,state);set(l,b,x,y+1,z,state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
    public static void build(ServerLevel l,BlockPos b) {
        var wall=GhostsSetBlock.of(WALLPAPER,Direction.NORTH); var floor=GhostsSetBlock.of(FLOOR,Direction.NORTH);
        fill(l,b,-20,-1,-46,20,-1,0,floor);fill(l,b,-20,11,-46,20,11,0,GhostsSetBlock.of(CEILING,Direction.NORTH));
        for(int y=0;y<=10;y++) {
            fill(l,b,-20,y,-46,20,y,-46,wall);fill(l,b,-20,y,0,20,y,0,wall);
            fill(l,b,-20,y,-46,-20,y,0,wall);fill(l,b,20,y,-46,20,y,0,wall);
        }
        // The family lives behind an ordinary front door; equipment occupies the arrival passage and east service wing.
        fill(l,b,-14,0,-14,13,4,-14,wall);door(l,b,0,0,-14,Direction.SOUTH);
        fill(l,b,-14,0,-44,-14,3,-15,wall);door(l,b,-14,0,-18,Direction.WEST);
        fill(l,b,13,0,-44,13,10,-15,GhostsSetBlock.of(PANEL,Direction.WEST));
        door(l,b,13,0,-35,Direction.EAST);
        fill(l,b,-14,4,-44,12,4,-15,floor);
        fill(l,b,-14,5,-23,12,9,-23,wall);
        door(l,b,-9,5,-23,Direction.SOUTH);
        door(l,b,8,5,-23,Direction.SOUTH);
        fill(l,b,0,5,-44,0,9,-24,wall);
        fill(l,b,-14,5,-44,-14,9,-24,wall);
        fill(l,b,-14,10,-44,12,10,-15,GhostsSetBlock.of(CEILING,Direction.NORTH));
        prop(l,b,-14,7,-33,PLASTER,Direction.EAST);
        // Five broad, backed treads meet the upstairs landing; the central arrival and all source/ending approaches stay clear.
        for(int i=0;i<5;i++)for(int x=-18;x<=-16;x++) {
            fill(l,b,x,-1,-16-i,x,i-1,-16-i,floor);
            set(l,b,x,i,-16-i,GhostsSetBlock.of(STAIR,Direction.NORTH));
        }
        fill(l,b,-18,4,-22,-15,4,-21,floor);for(int i=0;i<5;i++)prop(l,b,-19,i+1,-16-i,RAIL,Direction.NORTH);
        fill(l,b,-19,5,-22,-19,5,-21,GhostsSetBlock.of(RAIL,Direction.NORTH));
        for(int x:new int[]{-12,10})for(int z:new int[]{-41,-26}) {
            prop(l,b,x,3,z,LAMP,Direction.NORTH);prop(l,b,x,9,z,LAMP,Direction.NORTH);
        }
        // A six-block table with a continuous underside and real leg clearance. The rusty fittings can be filmed from below.
        for(int x=-1;x<=1;x++)for(int z=-29;z<=-28;z++)prop(l,b,x,0,z,GhostsSetBlock.Kind.TABLE,Direction.NORTH);
        prop(l,b,-1,1,-28,PLATE,Direction.NORTH);prop(l,b,1,1,-28,PLATE,Direction.NORTH);
        prop(l,b,-1,1,-29,PLATE,Direction.NORTH);prop(l,b,1,1,-29,PLAIN_PLATE,Direction.NORTH);
        prop(l,b,0,0,-29,BOLTS,Direction.NORTH);
        for(int x:new int[]{-3,3})for(int z:new int[]{-29,-27})prop(l,b,x,0,z,CHAIR,x<0?Direction.EAST:Direction.WEST);
        for(int x=-12;x<=-7;x++)prop(l,b,x,0,-42,x==-10?SINK:x==-8?COOKER:COUNTER,Direction.SOUTH);
        prop(l,b,-12,1,-42,JUG,Direction.SOUTH);prop(l,b,-7,1,-42,BOOKS,Direction.SOUTH);
        furniture(l,b,-10,0,-18,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        furniture(l,b,-7,0,-18,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST);
        for(int x=-10;x<=-7;x++)for(int z=-21;z<=-19;z++)prop(l,b,x,0,z,RUG,Direction.NORTH);
        // Distinct, lived-in bedrooms rather than a bed on the stage: books, the borrowed hat, a jug trap and the sisters' pictures.
        prop(l,b,-10,5,-33,BED_HEAD,Direction.NORTH);prop(l,b,-10,5,-32,BED_FOOT,Direction.NORTH);
        prop(l,b,10,5,-41,BED_HEAD,Direction.NORTH);prop(l,b,10,5,-40,BED_FOOT,Direction.NORTH);
        for(int x:new int[]{-12,10}){prop(l,b,x-1,5,-41,COUNTER,Direction.SOUTH);prop(l,b,x-1,6,-41,BOOKS,Direction.SOUTH);}
        prop(l,b,-11,5,-41,COUNTER,Direction.SOUTH);prop(l,b,-11,6,-41,HAT,Direction.SOUTH);prop(l,b,8,7,-44,DRAWING,Direction.SOUTH);
        prop(l,b,8,8,-23,JUG,Direction.SOUTH);prop(l,b,11,5,-26,BOOKS,Direction.NORTH);
        // Basement beneath the kitchen, reached by an enclosed stair beside the living room.
        fill(l,b,3,-5,-35,11,-5,-20,GhostsSetBlock.of(TILE,Direction.NORTH));
        for(int y=-4;y<=-1;y++) {
            fill(l,b,3,y,-35,11,y,-35,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,3,y,-20,11,y,-20,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,3,y,-35,3,y,-20,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,11,y,-35,11,y,-20,GhostsSetBlock.of(PANEL,Direction.NORTH));
        }
        fill(l,b,4,-4,-34,10,-2,-21,Blocks.AIR.defaultBlockState());
        for(int i=0;i<4;i++)for(int x=7;x<=9;x++) {
            set(l,b,x,-1,-21-i,Blocks.AIR.defaultBlockState());
            set(l,b,x,-1-i,-21-i,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        }
        prop(l,b,4,-2,-33,LAMP,Direction.SOUTH);prop(l,b,10,-4,-32,CASE,Direction.WEST);
        prop(l,b,5,-4,-33,BOOKS,Direction.SOUTH);
        // Production equipment is kept against supported surfaces, leaving the rooms useful to a moving camera operator.
        prop(l,b,16,0,-33,COUNTER,Direction.WEST);literary(l,b,new BlockPos(16,1,-33),LiteraryPropBlock.Kind.TELEVISION,Direction.EAST);prop(l,b,18,0,-33,CASE,Direction.WEST);
        prop(l,b,17,0,-12,CASE,Direction.SOUTH);prop(l,b,-18,0,-30,CAMERA,Direction.EAST);
        literary(l,b,LiteraryRooms.BOOTH,LiteraryPropBlock.Kind.RECEIVER,Direction.WEST);
        for(int x:new int[]{-18,18})for(int z:new int[]{-10,-40})literary(l,b,new BlockPos(x,0,z),LiteraryPropBlock.Kind.LIGHT_STAND,Direction.SOUTH);
        fill(l,b,14,4,-41,19,4,-32,GhostsSetBlock.of(CEILING,Direction.NORTH));
        prop(l,b,16,3,-38,LAMP,Direction.NORTH);prop(l,b,0,10,-9,LAMP,Direction.NORTH);
        BuildBlocks.after(l,()->{var data=LabyrinthData.get(l.getServer());GhostsSet.markBuilt(data,b);GhostsSetDressing.markBuilt(data,b);GhostsSetFinish.markBuilt(l,b);});
    }
}
