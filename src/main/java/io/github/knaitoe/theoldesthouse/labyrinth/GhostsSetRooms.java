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

/** The Barretts' home where the days are filmed: downstairs, landing, two bedrooms and a basement; BarrettHome makes the rest of the house (0.4.121, condensed in 0.4.174). */
public final class GhostsSetRooms {
    public static final BlockPos TABLE=new BlockPos(0,0,-19),MARJORIE_ROOM=new BlockPos(-7,5,-24),
            MERRY_ROOM=new BlockPos(7,5,-23),BASEMENT=new BlockPos(7,-4,-18),MONITOR_AT=new BlockPos(8,0,-11);
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
        // 0.4.174 (owner: "condense house - make it into a appropriately smaller, more lived in structure"): one family
        // house twenty-one blocks across and twenty-five deep, two storeys over a basement. Painted walls; the old
        // wallpaper was the production's.
        var wall=BarrettHomeBlock.of(BarrettHomeBlock.Kind.PAINT); var floor=GhostsSetBlock.of(FLOOR,Direction.NORTH); var air=Blocks.AIR.defaultBlockState();
        var storey=BarrettHomeBlock.of(BarrettHomeBlock.Kind.STOREY);
        fill(l,b,-11,-1,-26,11,-1,0,floor);fill(l,b,-11,10,-26,11,11,0,GhostsSetBlock.of(CEILING,Direction.NORTH));
        for(int y=0;y<=9;y++) {
            fill(l,b,-11,y,-26,11,y,-26,wall);fill(l,b,-11,y,0,11,y,0,wall);
            fill(l,b,-11,y,-26,-11,y,0,wall);fill(l,b,11,y,-26,11,y,0,wall);
        }
        // One floor between the storeys, open over the hall: the stair climbs through a two-storey foyer to the landing.
        fill(l,b,-10,4,-25,10,4,-1,storey);fill(l,b,-3,4,-8,3,4,-1,air);
        // Downstairs the front room, the hall and the den are along the front; behind them the open kitchen and dining room,
        // and the utility strip with the basement stair. The family's ordinary door joins the hall to the dining room.
        fill(l,b,-4,0,-11,-4,3,-1,wall);fill(l,b,-4,0,-10,-4,2,-9,air);
        fill(l,b,4,0,-11,4,3,-1,wall);fill(l,b,4,0,-3,4,2,-2,air);
        fill(l,b,-10,0,-12,10,3,-12,wall);door(l,b,0,0,-12,Direction.SOUTH);fill(l,b,-7,0,-12,-6,2,-12,air);door(l,b,6,0,-12,Direction.SOUTH);
        fill(l,b,5,0,-25,5,3,-13,wall);fill(l,b,5,0,-14,5,2,-13,air);
        // Upstairs: John and Sarah's room and the bathroom along the front, the landing and its corridor, then the girls'
        // rooms and their bathroom between them at the back.
        fill(l,b,-4,5,-10,-4,9,-1,wall);fill(l,b,4,5,-10,4,9,-1,wall);
        fill(l,b,-10,5,-11,-4,9,-11,wall);fill(l,b,4,5,-11,10,9,-11,wall);door(l,b,-7,5,-11,Direction.NORTH);door(l,b,7,5,-11,Direction.NORTH);
        fill(l,b,-10,5,-14,10,9,-14,wall);door(l,b,-6,5,-14,Direction.SOUTH);door(l,b,0,5,-14,Direction.SOUTH);door(l,b,6,5,-14,Direction.SOUTH);
        fill(l,b,-2,5,-25,-2,9,-15,wall);fill(l,b,3,5,-25,3,9,-15,wall);
        // The plaster Marjorie's climb on day three cracks, in her room's west wall.
        prop(l,b,-11,7,-24,PLASTER,Direction.EAST);
        // Five broad, backed treads against the front room's wall rise to the landing; the entrance and its approach stay clear.
        for(int i=0;i<5;i++)for(int x=-3;x<=-1;x++) {
            fill(l,b,x,-1,-4-i,x,i-1,-4-i,floor);
            set(l,b,x,i,-4-i,GhostsSetBlock.of(STAIR,Direction.NORTH));
        }
        for(int i=0;i<5;i++)prop(l,b,0,i+1,-4-i,i==4?STAIR_RAIL_TOP:STAIR_RAIL,Direction.NORTH);
        // The landing's banister over the foyer, where the day-six leap is filmed; the stair's head is kept clear.
        for(int x=1;x<=3;x++)prop(l,b,x,5,-9,RAIL,Direction.EAST);
        for(int[] at:new int[][]{{-8,3,-23},{2,3,-24},{-8,9,-23},{-8,9,-17},{8,9,-23},{8,9,-17}})prop(l,b,at[0],at[1],at[2],LAMP,Direction.NORTH);
        // One two-by-three table, four corner legs, and chairs close enough to eat.
        for(int x=0;x<=1;x++)for(int z=-20;z<=-18;z++)set(l,b,x,0,z,tableState(x,z));
        for(int x=0;x<=1;x++)for(int z:new int[]{-20,-18})prop(l,b,x,1,z,x==1&&z==-20?PLAIN_PLATE:PLATE,Direction.NORTH);
        for(int x:new int[]{-1,2})for(int z:new int[]{-20,-18})prop(l,b,x,0,z,CHAIR,x<0?Direction.EAST:Direction.WEST);
        // The kitchen along the back wall, either side of the back door.
        for(int x=-9;x<=-4;x++)prop(l,b,x,0,-25,x==-7?SINK:x==-5?COOKER:COUNTER,Direction.SOUTH);
        prop(l,b,-9,1,-25,JUG,Direction.SOUTH);prop(l,b,-4,1,-25,BOOKS,Direction.SOUTH);
        // The family corner where the day-one interviews are shot: two armchairs facing each other on a rug.
        furniture(l,b,-9,0,-17,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        furniture(l,b,-6,0,-17,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST);
        for(int x=-8;x<=-7;x++)for(int z=-18;z<=-14;z++)prop(l,b,x,0,z,RUG,Direction.NORTH);
        // Distinct, lived-in bedrooms rather than a bed on the stage: books, the borrowed hat, a jug trap and the sisters' pictures.
        prop(l,b,-7,5,-24,BED_HEAD,Direction.NORTH);prop(l,b,-7,5,-23,BED_FOOT,Direction.NORTH);
        prop(l,b,9,5,-25,BED_HEAD,Direction.NORTH);prop(l,b,9,5,-24,BED_FOOT,Direction.NORTH);
        prop(l,b,-10,5,-25,COUNTER,Direction.SOUTH);prop(l,b,-10,6,-25,BOOKS,Direction.SOUTH);prop(l,b,-8,5,-25,COUNTER,Direction.SOUTH);prop(l,b,-8,6,-25,HAT,Direction.SOUTH);
        prop(l,b,6,5,-25,COUNTER,Direction.SOUTH);prop(l,b,6,6,-25,BOOKS,Direction.SOUTH);prop(l,b,7,7,-25,DRAWING,Direction.SOUTH);
        prop(l,b,6,8,-14,JUG,Direction.SOUTH);prop(l,b,9,5,-17,BOOKS,Direction.NORTH);
        // Basement beneath the dining room and the utility strip, reached by an enclosed stair at the strip's front.
        fill(l,b,3,-5,-26,11,-5,-13,GhostsSetBlock.of(TILE,Direction.NORTH));
        for(int y=-4;y<=-2;y++) {
            fill(l,b,3,y,-26,11,y,-26,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,3,y,-13,11,y,-13,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,3,y,-26,3,y,-13,GhostsSetBlock.of(PANEL,Direction.NORTH));
            fill(l,b,11,y,-26,11,y,-13,GhostsSetBlock.of(PANEL,Direction.NORTH));
        }
        fill(l,b,4,-4,-25,10,-2,-14,air);
        for(int i=0;i<4;i++)for(int x=7;x<=9;x++) {
            set(l,b,x,-1,-14-i,air);
            set(l,b,x,-1-i,-14-i,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        }
        // 0.4.199 (owner: "remove the banisters atop the basement drop so the room behind is actually accessible"): the
        // stairwell is open, with the strip's floor either side of it, so the washer and the shelves behind it can be reached.
        prop(l,b,4,-2,-24,LAMP,Direction.SOUTH);prop(l,b,10,-4,-24,CASE,Direction.WEST);
        prop(l,b,5,-4,-25,BOOKS,Direction.SOUTH);
        // Production equipment is kept against supported surfaces, leaving the rooms useful to a moving camera operator.
        prop(l,b,8,0,-11,COUNTER,Direction.NORTH);literary(l,b,new BlockPos(8,1,-11),LiteraryPropBlock.Kind.TELEVISION,Direction.SOUTH);prop(l,b,9,0,-11,CASE,Direction.SOUTH);
        prop(l,b,6,0,-1,CASE,Direction.NORTH);prop(l,b,5,0,-9,CAMERA,Direction.EAST);
        literary(l,b,LiteraryRooms.BOOTH,LiteraryPropBlock.Kind.RECEIVER,Direction.WEST);
        for(var at:new BlockPos[]{new BlockPos(-10,0,-13),new BlockPos(4,0,-25)})literary(l,b,at,LiteraryPropBlock.Kind.LIGHT_STAND,Direction.SOUTH);
        BarrettHome.build(l,b);
        BuildBlocks.after(l,()->{var data=LabyrinthData.get(l.getServer());GhostsSet.markBuilt(data,b);GhostsSetDressing.markBuilt(data,b);GhostsSetFinish.markBuilt(l,b);});
    }
    public static net.minecraft.world.level.block.state.BlockState tableState(int x,int z){
        if(x==0&&z==-20)return GhostsSetBlock.of(BOLTS,Direction.NORTH);
        if(z==-19)return GhostsSetBlock.of(GhostsSetBlock.Kind.TABLE,Direction.NORTH);
        return GhostsSetBlock.of(TABLE_LEG,z==-20?Direction.EAST:x==0?Direction.WEST:Direction.SOUTH);
    }
}
