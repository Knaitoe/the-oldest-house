package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.*;

/** Three complete new private sets. Existing copies and their original objects are never rebuilt. */
public final class HouseReadRemainingRooms {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /**
     * 0.4.160, the owner's playtest notes: the waiting room's three ways to pass the time stand about six blocks from where
     * the reader comes in, spaced out: a low table to the left with the glasses, the paper and the magazine, one to a place;
     * the television cart and the cooler ahead; the phone on its side table by the chair to the right, charging at the outlet.
     */
    public static final BlockPos GLASSES_AT=new BlockPos(-5,1,-2),PAPER_AT=new BlockPos(-5,1,-3),MAGAZINES_AT=new BlockPos(-5,1,-4),CRT_AT=new BlockPos(-1,0,-6),COOLER_AT=new BlockPos(1,0,-6),
        PHONE_AT=new BlockPos(6,1,-4),PHONE_CHAIR=new BlockPos(6,0,-3),OUTLET_AT=new BlockPos(6,0,-5),CALENDAR_AT=new BlockPos(-3,1,-14),CLOCK_AT=new BlockPos(3,2,-11),SIGN_AT=new BlockPos(3,1,-11),BELL_AT=new BlockPos(-3,1,-11),
        POSTCARD_AT=new BlockPos(1,2,-9),JUKEBOX_AT=new BlockPos(0,0,1),TAP_AT=new BlockPos(0,0,-9),SWITCH_AT=new BlockPos(1,1,1),SINK_AT=new BlockPos(0,0,-9),BATH_MIRROR=new BlockPos(0,1,-9);
    /** The shelter (0.4.159): the strangers lean on the west and east rails, and the dock runs out ahead, past both of them. */
    public static final double FIGURE_X=9.3,FIGURE_Z=-8.5;
    public static final BlockPos SHELTER_ENTRY=new BlockPos(-3,0,2),DOCK_LANTERN=new BlockPos(3,0,-22);
    public static final int DOCK_Z=-21;
    /** The middle of the floorboards, between the two rails: the same distance from both strangers. */
    public static boolean middle(BlockPos b,net.minecraft.world.phys.Vec3 at){return Math.abs(at.x-b.getX()-.5)<2.5&&at.z<b.getZ()-1.5&&at.z>b.getZ()-15&&at.y>b.getY()-.5;}
    public static boolean onDock(BlockPos b,net.minecraft.world.phys.Vec3 at){return at.z<b.getZ()+DOCK_Z&&Math.abs(at.x-b.getX()-.5)<3;}
    /**
     * The routine blocks on the vanity counter (0.4.167): eight, four either side of the sink, two tools to a block, a set's
     * two blocks side by side. The postcard moved up beside the mirror's top to make room.
     */
    public static final List<BlockPos> ROUTINE_PLACES=List.of(new BlockPos(-4,1,-9),new BlockPos(-3,1,-9),new BlockPos(-2,1,-9),new BlockPos(-1,1,-9),new BlockPos(1,1,-9),new BlockPos(2,1,-9),new BlockPos(3,1,-9),new BlockPos(4,1,-9));
    /** The bar light over the mirror and the light in the middle of the ceiling; the switch turns both off. */
    public static final List<BlockPos> BATH_LIGHTS=List.of(new BlockPos(0,3,-9),new BlockPos(0,3,-4));
    private HouseReadRemainingRooms(){}
    public static AABB bounds(BlockPos b,HouseReadSites.Room room){return switch(room){case WAITING->new AABB(b.getX()-11,b.getY()-3,b.getZ()-19,b.getX()+12,b.getY()+7,b.getZ()+6);case BATHROOM->new AABB(b.getX()-8,b.getY()-3,b.getZ()-16,b.getX()+9,b.getY()+7,b.getZ()+6);case SHELTER->new AABB(b.getX()-24,b.getY()-6,b.getZ()-30,b.getX()+25,b.getY()+10,b.getZ()+9);default->throw new IllegalArgumentException(room.name());};}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block s){box(l,b,x,y,z,X,Y,Z,s.defaultBlockState());}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fit(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part){HouseReadRooms.fitting(l,b,at,part);}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,HouseReadPropBlock.Part part){fit(l,b,new BlockPos(x,y,z),part);}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,HouseReadPropBlock.Part part,Direction facing){put(l,b,x,y,z,HouseReadRooms.part(part).setValue(HorizontalDirectionalBlock.FACING,facing));}
    private static void fit(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part,Direction facing){fit(l,b,at.getX(),at.getY(),at.getZ(),part,facing);}
    /** A room shell: floor, ceiling, four walls and the entry door at the middle of the front wall. */
    static void shell(ServerLevel l,BlockPos b,int half,int end,BlockState wall,BlockState floor,BlockState ceiling,Block door){
        box(l,b,-half,-2,end,half,4,3,Blocks.AIR.defaultBlockState());box(l,b,-half,-2,end,half,-1,3,floor);box(l,b,-half,4,end,half,4,3,ceiling);
        box(l,b,-half,0,end,-half,3,3,wall);box(l,b,half,0,end,half,3,3,wall);box(l,b,-half,0,end,half,3,end,wall);box(l,b,-half,0,2,half,3,3,wall);
        HouseReadRooms.door(l,b,HouseReadRooms.COAT_ENTRY,door,Direction.SOUTH);box(l,b,0,0,3,0,1,3,Blocks.AIR.defaultBlockState());
    }
    /** The routine block at each counter place, per reader: the four sets in a shuffled order, each set's pair either way round. */
    public static int[] routineOrder(UUID id){
        var random=new Random(id.getMostSignificantBits()^Long.rotateLeft(id.getLeastSignificantBits(),17)^139);var sets=new ArrayList<>(List.of(0,1,2,3));Collections.shuffle(sets,random);
        int[] order=new int[8];for(int k=0;k<4;k++){boolean swap=random.nextBoolean();order[2*k]=sets.get(k)*2+(swap?1:0);order[2*k+1]=sets.get(k)*2+(swap?0:1);}return order;
    }
    public static void build(ServerLevel l,BlockPos b,HouseReadSites.Room room,UUID owner){switch(room){case WAITING->waiting(l,b);case BATHROOM->bathroom(l,b,owner);case SHELTER->shelter(l,b);default->throw new IllegalArgumentException(room.name());}}
    private static void waiting(ServerLevel l,BlockPos b){
        box(l,b,-11,-2,-18,11,6,3,Blocks.AIR.defaultBlockState());
        shell(l,b,7,-15,HouseReadRegistry.PANELS.get().defaultBlockState(),HouseReadRegistry.CARPET.get().defaultBlockState(),HouseReadRegistry.CEILING_TILE.get().defaultBlockState(),HouseReadRegistry.PANEL_DOOR.get());
        // The reception partition: a counter, a lightly frosted window and the office door, with a ledge in front for the bell.
        for(int x=-7;x<=7;x++){
            if(x==0)continue;
            if(Math.abs(x)==1){box(l,b,x,0,-12,x,3,-12,HouseReadRegistry.PANELS.get());continue;}
            put(l,b,x,0,-12,HouseReadRegistry.COUNTER.get().defaultBlockState());fit(l,b,x,1,-12,RECEPTION);box(l,b,x,2,-12,x,3,-12,HouseReadRegistry.PANELS.get());
            if(Math.abs(x)<=6)put(l,b,x,0,-11,HouseReadRegistry.COUNTER.get().defaultBlockState());
        }
        HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.WAITING),HouseReadRegistry.OFFICE_DOOR.get(),Direction.SOUTH);
        fit(l,b,BELL_AT,BELL);fit(l,b,SIGN_AT,SIGN);fit(l,b,CLOCK_AT,WALL_CLOCK);
        // Behind the glass: the receptionist's desk and lamp, and the calendar on the back wall.
        fit(l,b,2,0,-14,TABLE_END,Direction.EAST);fit(l,b,3,0,-14,TABLE_END,Direction.WEST);fit(l,b,3,1,-14,LAMP);fit(l,b,2,0,-13,CHAIR);fit(l,b,CALENDAR_AT,CALENDAR);
        // The low table: glasses, paper and magazine, one to a place, with two armchairs facing it from the wall.
        fit(l,b,-5,0,-2,TABLE_END,Direction.NORTH);fit(l,b,-5,0,-3,TABLE_MID,Direction.EAST);fit(l,b,-5,0,-4,TABLE_END,Direction.SOUTH);
        fit(l,b,GLASSES_AT,GLASSES);fit(l,b,PAPER_AT,PAPER);fit(l,b,MAGAZINES_AT,MAGAZINES);
        for(int z:new int[]{-2,-4})fit(l,b,-6,0,z,ARMCHAIR,Direction.EAST);
        // Ahead: a panelled column, the television cart parked against it facing the armchairs and the cooler on its far side.
        box(l,b,0,0,-6,0,3,-6,HouseReadRegistry.PANELS.get());fit(l,b,CRT_AT,CRT,Direction.WEST);fit(l,b,COOLER_AT,COOLER,Direction.EAST);
        // To the right: the phone chair by the window, the phone on its side table, charging at the outlet; one more chair.
        fit(l,b,PHONE_CHAIR,ARMCHAIR,Direction.WEST);fit(l,b,6,0,-4,SIDE_TABLE);fit(l,b,PHONE_AT,PHONE);fit(l,b,OUTLET_AT,OUTLET,Direction.WEST);fit(l,b,6,0,-7,ARMCHAIR,Direction.WEST);
        box(l,b,7,1,-2,7,2,-1,HouseReadRegistry.WINDOW.get());box(l,b,7,1,-7,7,2,-6,HouseReadRegistry.WINDOW.get());
        for(var at:List.of(new BlockPos(-3,4,-3),new BlockPos(3,4,-3),new BlockPos(0,4,-8),new BlockPos(0,4,-14)))fit(l,b,at,CEILING_PANEL);
        // The rainy lot through the two windows: wet asphalt and a painted line, the brick wall across the way, low cloud.
        box(l,b,8,-1,-11,11,-1,2,HouseReadRegistry.ASPHALT.get());box(l,b,9,-1,-10,9,-1,1,HouseReadRegistry.ASPHALT_LINE.get());
        box(l,b,11,0,-11,11,4,2,HouseReadRegistry.LOT_WALL.get());box(l,b,8,0,-11,10,4,-11,HouseReadRegistry.LOT_WALL.get());box(l,b,8,0,2,10,4,2,HouseReadRegistry.LOT_WALL.get());
        box(l,b,8,4,-10,10,4,1,Blocks.AIR.defaultBlockState());box(l,b,8,5,-11,11,5,2,HouseReadRegistry.OVERCAST.get());put(l,b,9,3,-5,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9));
    }
    private static void bathroom(ServerLevel l,BlockPos b,UUID owner){
        shell(l,b,5,-10,HouseReadRegistry.TILE.get().defaultBlockState(),HouseReadRegistry.LINOLEUM.get().defaultBlockState(),HouseReadRegistry.CEILING.get().defaultBlockState(),HouseReadRegistry.PANEL_DOOR.get());
        // Tile to shoulder height, wallpaper above it.
        box(l,b,-5,2,-10,-5,3,2,HouseReadRegistry.WALLPAPER.get());box(l,b,5,2,-10,5,3,2,HouseReadRegistry.WALLPAPER.get());box(l,b,-5,2,-10,5,3,-10,HouseReadRegistry.WALLPAPER.get());box(l,b,-5,2,2,5,3,2,HouseReadRegistry.WALLPAPER.get());
        // The vanity across the back wall: the sink and its mirror in the middle, the routines four blocks either side, the
        // postcard tucked at the mirror's edge, and the bar light above.
        for(int x=-4;x<=4;x++)if(x!=0)fit(l,b,x,0,-9,VANITY);
        fit(l,b,SINK_AT,SINK);fit(l,b,BATH_MIRROR,MIRROR);fit(l,b,BATH_MIRROR.above(),MIRROR_TOP);fit(l,b,POSTCARD_AT,POSTCARD);fit(l,b,BATH_LIGHTS.get(0),VANITY_LIGHT);
        int[] order=routineOrder(owner);
        for(int i=0;i<ROUTINE_PLACES.size();i++)fit(l,b,ROUTINE_PLACES.get(i),HouseReadPropBlock.ROUTINE_BLOCKS.get(order[i]));
        // The tub along the west wall under a frosted window, the toilet and towel bar on the east, a mat, a radiator by the door.
        fit(l,b,-4,0,-7,TUB_FOOT,Direction.WEST);fit(l,b,-4,0,-6,TUB_HEAD,Direction.WEST);box(l,b,-5,2,-7,-5,2,-6,HouseReadRegistry.WINDOW.get());
        fit(l,b,4,0,-6,TOILET,Direction.WEST);fit(l,b,4,1,-3,TOWEL_BAR,Direction.WEST);fit(l,b,-3,0,-6,BATH_MAT,Direction.EAST);fit(l,b,0,0,-8,BATH_MAT);fit(l,b,-4,0,0,RADIATOR,Direction.EAST);
        fit(l,b,SWITCH_AT,LIGHT_SWITCH,Direction.NORTH);fit(l,b,BATH_LIGHTS.get(1),PENDANT);
        // The bedroom beyond the light-controlled door, glimpsed: a turned-down bed, a nightstand and its lamp.
        HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.BATHROOM),HouseReadRegistry.PANEL_DOOR.get(),Direction.SOUTH);
        box(l,b,1,-1,-16,8,4,-11,HouseReadRegistry.WALLPAPER.get());box(l,b,2,0,-15,7,3,-11,Blocks.AIR.defaultBlockState());box(l,b,2,-1,-15,7,-1,-11,HouseReadRegistry.FLOORBOARDS.get());
        put(l,b,6,0,-13,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.EAST).setValue(BedBlock.PART,BedPart.FOOT));put(l,b,7,0,-13,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.EAST).setValue(BedBlock.PART,BedPart.HEAD));
        fit(l,b,7,0,-15,SIDE_TABLE);fit(l,b,7,1,-15,LAMP);
    }
    private static void shelter(ServerLevel l,BlockPos b){
        box(l,b,-23,-5,-29,23,8,7,Blocks.AIR.defaultBlockState());
        box(l,b,-23,-5,-29,23,-3,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-22,-2,-28,22,-2,6,Blocks.WATER.defaultBlockState());box(l,b,-23,-2,-29,-23,-1,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,23,-2,-29,23,-1,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-23,-2,-29,23,-1,-29,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-23,-2,7,23,-1,7,Blocks.DEEPSLATE.defaultBlockState());
        box(l,b,-23,0,-29,23,6,-29,HouseReadRegistry.NIGHT.get());box(l,b,-23,0,-29,-23,6,7,HouseReadRegistry.NIGHT.get());box(l,b,23,0,-29,23,6,7,HouseReadRegistry.NIGHT.get());
        for(int x=-20;x<=20;x+=4)box(l,b,x,0,-28,x+2,2+(Math.abs(x)%3),-27,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        // A roofed, dry deck over the lake. The land side is behind the reader; the lake opens ahead.
        box(l,b,-10,-1,-20,10,-1,3,HouseReadRegistry.PAVILION.get().defaultBlockState());
        for(int x:new int[]{-10,10})for(int z:new int[]{-19,-9,1})box(l,b,x,0,z,x,4,z,HouseReadRegistry.DOCK_POST.get());
        for(int z:new int[]{-19,-9,1})box(l,b,-11,5,z,11,5,z,HouseReadRegistry.DOCK_POST.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        box(l,b,-11,6,-21,11,6,4,HouseReadRegistry.SHAKE_SLAB.get());
        // The west and east railings, unbroken, and the lake rail ahead with its steps down to the dock.
        for(int z=-20;z<=1;z++)if(z!=-19&&z!=-9&&z!=1){put(l,b,-10,0,z,HouseReadRegistry.DOCK_RAIL.get().defaultBlockState());put(l,b,10,0,z,HouseReadRegistry.DOCK_RAIL.get().defaultBlockState());}
        for(int x=-9;x<=9;x++)if(Math.abs(x)>1)put(l,b,x,0,-20,HouseReadRegistry.DOCK_RAIL.get().defaultBlockState());
        for(int x=-8;x<=8;x++)fit(l,b,x,4,-9,STRING_LIGHT);
        // The back wall on the land side: the jukebox against it, the way back to its left and the way on to its right.
        box(l,b,-10,0,2,10,3,2,HouseReadRegistry.PAVILION.get().defaultBlockState());box(l,b,-10,4,2,10,4,2,HouseReadRegistry.PAVILION.get());
        put(l,b,0,0,1,HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,JUKEBOX).setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));
        HouseReadRooms.door(l,b,SHELTER_ENTRY,HouseReadRegistry.BOATHOUSE_DOOR.get(),Direction.SOUTH);HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.SHELTER),HouseReadRegistry.BOATHOUSE_DOOR.get(),Direction.SOUTH);
        // Steps down from the lake rail to a short dock, a dry lantern by them, and a covered rowboat tied alongside.
        for(int x=-1;x<=1;x++){put(l,b,x,-1,-21,HouseReadRegistry.DOCK_STEP.get().defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));box(l,b,x,-2,-27,x,-2,-22,HouseReadRegistry.PAVILION.get().defaultBlockState());}
        for(int z:new int[]{-22,-27})for(int x:new int[]{-2,2})if(z!=-22||x!=2)box(l,b,x,-2,z,x,0,z,HouseReadRegistry.DOCK_RAIL.get().defaultBlockState());
        // The lantern hangs out over the water from an arm on the dock's corner post, clear of the boards.
        box(l,b,2,-2,-22,2,1,-22,HouseReadRegistry.DOCK_POST.get());put(l,b,3,1,-22,HouseReadRegistry.DOCK_POST.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));fit(l,b,DOCK_LANTERN,DECK_LANTERN);fit(l,b,3,-1,-25,ROWBOAT);
        // A bottle on each rail beside its stranger, the two lit alike.
        for(int x:new int[]{-10,10}){put(l,b,x,0,-7,HouseReadRegistry.DOCK_RAIL.get().defaultBlockState());fit(l,b,x,1,-7,BOTTLE);}
        fit(l,b,-6,4,-9,DECK_LANTERN);fit(l,b,6,4,-9,DECK_LANTERN);
    }
}
