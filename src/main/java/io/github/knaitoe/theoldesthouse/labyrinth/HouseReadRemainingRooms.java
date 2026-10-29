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
     * 0.4.159: the waiting room's three ways to pass the time stand at the same distance from the door (about seven blocks):
     * the low table to the left, the cart ahead and the phone chair by the window to the right.
     */
    public static final BlockPos PHONE_AT=new BlockPos(6,1,-4),PHONE_CHAIR=new BlockPos(6,0,-3),GLASSES_AT=new BlockPos(-5,1,-4),PAPER_AT=new BlockPos(-4,1,-4),MAGAZINES_AT=new BlockPos(-5,1,-5),ASHTRAY_AT=new BlockPos(-4,1,-5),CRT_AT=new BlockPos(-1,0,-7),COOLER_AT=new BlockPos(1,0,-7),
        CALENDAR_AT=new BlockPos(-4,2,-13),POSTCARD_AT=new BlockPos(1,2,-7),JUKEBOX_AT=new BlockPos(0,0,1),TAP_AT=new BlockPos(0,1,-6),SWITCH_AT=new BlockPos(-2,1,-7);
    /** The shelter (0.4.159): the strangers lean on the west and east rails, and the dock runs out ahead, past both of them. */
    public static final double FIGURE_X=9.3,FIGURE_Z=-8.5;
    public static final BlockPos SHELTER_ENTRY=new BlockPos(-3,0,2),DOCK_LANTERN=new BlockPos(1,-1,-22);
    public static final int DOCK_Z=-21;
    /** The middle of the floorboards, between the two rails: the same distance from both strangers. */
    public static boolean middle(BlockPos b,net.minecraft.world.phys.Vec3 at){return Math.abs(at.x-b.getX()-.5)<2.5&&at.z<b.getZ()-1.5&&at.z>b.getZ()-15&&at.y>b.getY()-.5;}
    public static boolean onDock(BlockPos b,net.minecraft.world.phys.Vec3 at){return at.z<b.getZ()+DOCK_Z&&Math.abs(at.x-b.getX()-.5)<3;}
    public static final List<BlockPos> ROUTINE_PLACES=List.of(new BlockPos(-2,1,-3),new BlockPos(2,1,-3),new BlockPos(-3,1,-2),new BlockPos(3,1,-2));
    public static final List<BlockPos> BATH_LIGHTS=List.of(new BlockPos(0,3,-3),new BlockPos(0,3,-10));
    private HouseReadRemainingRooms(){}
    public static AABB bounds(BlockPos b,HouseReadSites.Room room){return switch(room){case WAITING->new AABB(b.getX()-11,b.getY()-3,b.getZ()-19,b.getX()+12,b.getY()+7,b.getZ()+6);case BATHROOM->new AABB(b.getX()-8,b.getY()-3,b.getZ()-16,b.getX()+9,b.getY()+7,b.getZ()+6);case SHELTER->new AABB(b.getX()-24,b.getY()-6,b.getZ()-30,b.getX()+25,b.getY()+10,b.getZ()+9);default->throw new IllegalArgumentException(room.name());};}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),s,F);}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fit(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part){HouseReadRooms.fitting(l,b,at,part);}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,HouseReadPropBlock.Part part){fit(l,b,new BlockPos(x,y,z),part);}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){put(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    private static void shell(ServerLevel l,BlockPos b,int half,int end,BlockState wall,BlockState floor){
        box(l,b,-half,-2,end,half,4,3,Blocks.AIR.defaultBlockState());box(l,b,-half,-2,end,half,-1,3,floor);box(l,b,-half,4,end,half,4,3,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-half,0,end,-half,3,3,wall);box(l,b,half,0,end,half,3,3,wall);box(l,b,-half,0,end,half,3,end,wall);box(l,b,-half,0,2,half,3,3,wall);
        HouseReadRooms.door(l,b,HouseReadRooms.COAT_ENTRY,Blocks.OAK_DOOR,Direction.SOUTH);box(l,b,0,0,3,0,1,3,Blocks.AIR.defaultBlockState());
    }
    public static int[] routineOrder(UUID id){var order=new ArrayList<>(List.of(0,1,2,3));Collections.shuffle(order,new Random(id.getMostSignificantBits()^Long.rotateLeft(id.getLeastSignificantBits(),17)^139));return order.stream().mapToInt(Integer::intValue).toArray();}
    public static void build(ServerLevel l,BlockPos b,HouseReadSites.Room room,UUID owner){switch(room){case WAITING->waiting(l,b);case BATHROOM->bathroom(l,b,owner);case SHELTER->shelter(l,b);default->throw new IllegalArgumentException(room.name());}}
    private static void waiting(ServerLevel l,BlockPos b){
        box(l,b,-11,-2,-18,11,6,3,Blocks.AIR.defaultBlockState());
        shell(l,b,7,-17,HouseReadRegistry.PANELS.get().defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState());box(l,b,-5,-1,-11,5,-1,0,Blocks.BROWN_WOOL.defaultBlockState());
        var chair=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,ARMCHAIR);
        for(int z:new int[]{-3,-6,-9}){put(l,b,-6,0,z,chair.setValue(HorizontalDirectionalBlock.FACING,Direction.EAST));put(l,b,6,0,z,chair.setValue(HorizontalDirectionalBlock.FACING,Direction.WEST));}
        // The low table: the folded local paper, the magazines, an ashtray and a pair of reading glasses.
        for(int x:new int[]{-5,-4})for(int z:new int[]{-4,-5})put(l,b,x,0,z,Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        fit(l,b,GLASSES_AT,GLASSES);fit(l,b,PAPER_AT,PAPER);fit(l,b,MAGAZINES_AT,MAGAZINES);fit(l,b,ASHTRAY_AT,ASHTRAY);
        // The cart: the television on its rolling cart, and the water cooler beside it; the way to the counter runs between them.
        fit(l,b,CRT_AT,CRT);fit(l,b,COOLER_AT,COOLER);
        // The armrest of the chair nearest the outlet, the phone already half unplugged, and the window onto the lot.
        put(l,b,6,0,-4,Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));fit(l,b,PHONE_AT,PHONE);put(l,b,6,1,-5,Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.WALL).setValue(LeverBlock.FACING,Direction.WEST));
        box(l,b,7,1,-2,7,2,-1,Blocks.GLASS_PANE.defaultBlockState());box(l,b,7,1,-7,7,2,-6,Blocks.GLASS_PANE.defaultBlockState());
        fit(l,b,-6,0,-10,CLOCK_BASE);fit(l,b,-6,1,-10,CLOCK_FACE);
        for(int x=-7;x<=7;x++)if(Math.abs(x)>1){put(l,b,x,0,-12,Blocks.SPRUCE_PLANKS.defaultBlockState());fit(l,b,x,1,-12,RECEPTION);}
        put(l,b,-5,0,-11,Blocks.SPRUCE_PLANKS.defaultBlockState());BuildBlocks.set(l,b.offset(-5,1,-11),Blocks.OAK_SIGN.defaultBlockState(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(b.offset(-5,1,-11)) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign){var text=sign.getFrontText().setMessage(0,net.minecraft.network.chat.Component.literal("Back in")).setMessage(1,net.minecraft.network.chat.Component.literal("a moment."));sign.setText(text,true);sign.setWaxed(true);}});fit(l,b,-2,1,-11,BELL);put(l,b,-2,0,-11,Blocks.SPRUCE_PLANKS.defaultBlockState());fit(l,b,CALENDAR_AT,CALENDAR);
        HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.WAITING),Blocks.IRON_DOOR,Direction.SOUTH);
        lamp(l,b,0,3,-5);lamp(l,b,0,3,-12);
        // A shallow, enclosed rainy parking lot seen through the two clear east windows: wet asphalt, painted lines, a parked car's glass.
        box(l,b,8,-1,-10,10,-1,1,Blocks.GRAY_CONCRETE.defaultBlockState());box(l,b,10,0,-10,10,4,1,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());box(l,b,8,0,-10,10,4,-10,Blocks.GRAY_TERRACOTTA.defaultBlockState());box(l,b,8,0,1,10,4,1,Blocks.GRAY_TERRACOTTA.defaultBlockState());box(l,b,8,4,-10,10,4,1,Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
        for(int z:new int[]{-8,-4,0})put(l,b,9,0,z,Blocks.WHITE_CARPET.defaultBlockState());put(l,b,9,0,-2,Blocks.GRAY_CARPET.defaultBlockState());put(l,b,9,1,-2,Blocks.GRAY_STAINED_GLASS.defaultBlockState());put(l,b,9,0,-6,Blocks.GRAY_CARPET.defaultBlockState());
    }
    private static void bathroom(ServerLevel l,BlockPos b,UUID owner){
        shell(l,b,5,-12,HouseReadRegistry.TILE.get().defaultBlockState(),HouseReadRegistry.LINOLEUM.get().defaultBlockState());
        box(l,b,-5,0,-8,5,3,-8,HouseReadRegistry.TILE.get().defaultBlockState());box(l,b,-4,-1,-11,4,-1,-9,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-5,0,-12,5,3,-12,HouseReadRegistry.WALLPAPER.get().defaultBlockState());
        for(int x:new int[]{-3,3}){put(l,b,x,0,-3,Blocks.STRIPPED_OAK_WOOD.defaultBlockState());put(l,b,x>0?2:-2,0,-2,Blocks.STRIPPED_OAK_WOOD.defaultBlockState());}

        fit(l,b,0,0,-6,SINK);put(l,b,0,0,-7,Blocks.STRIPPED_OAK_WOOD.defaultBlockState());fit(l,b,TAP_AT,TAP);fit(l,b,new BlockPos(0,1,-7),MIRROR);fit(l,b,new BlockPos(0,2,-7),MIRROR_TOP);fit(l,b,POSTCARD_AT,POSTCARD);fit(l,b,SWITCH_AT,LIGHT_SWITCH);
        int[] order=routineOrder(owner);var kinds=new HouseReadPropBlock.Part[]{ROUTINE_A,ROUTINE_B,ROUTINE_C,ROUTINE_D};
        for(int i=0;i<4;i++){var pos=ROUTINE_PLACES.get(i);fit(l,b,pos.below(),i<2?SINK:CABINET);fit(l,b,pos,kinds[order[i]]);}
        fit(l,b,-4,0,-1,RADIATOR);fit(l,b,4,0,-6,TOWEL);
        for(var at:BATH_LIGHTS)lamp(l,b,at.getX(),at.getY(),at.getZ());
        HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.BATHROOM),Blocks.IRON_DOOR,Direction.SOUTH);
        // A glimpse of a quiet bedroom beyond the actual light-controlled door.
        put(l,b,1,0,-10,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));put(l,b,1,0,-11,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        box(l,b,-5,1,-6,-5,2,-5,Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState());box(l,b,-6,0,-7,-6,4,-4,Blocks.GRAY_TERRACOTTA.defaultBlockState());
    }
    private static void shelter(ServerLevel l,BlockPos b){
        box(l,b,-23,-5,-29,23,8,7,Blocks.AIR.defaultBlockState());
        box(l,b,-23,-5,-29,23,-3,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-22,-2,-28,22,-2,6,Blocks.WATER.defaultBlockState());box(l,b,-23,-2,-29,-23,-1,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,23,-2,-29,23,-1,7,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-23,-2,-29,23,-1,-29,Blocks.DEEPSLATE.defaultBlockState());box(l,b,-23,-2,7,23,-1,7,Blocks.DEEPSLATE.defaultBlockState());
        box(l,b,-23,0,-29,23,6,-29,Blocks.BLACK_CONCRETE.defaultBlockState());box(l,b,-23,0,-29,-23,6,7,Blocks.BLACK_CONCRETE.defaultBlockState());box(l,b,23,0,-29,23,6,7,Blocks.BLACK_CONCRETE.defaultBlockState());
        for(int x=-20;x<=20;x+=4)box(l,b,x,0,-28,x+2,2+(Math.abs(x)%3),-27,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        // A roofed, dry deck over the lake. The land side is behind the reader; the lake opens ahead.
        box(l,b,-10,-1,-20,10,-1,3,HouseReadRegistry.PAVILION.get().defaultBlockState());
        for(int x:new int[]{-10,10})for(int z:new int[]{-19,-9,1})box(l,b,x,0,z,x,5,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int z:new int[]{-19,-9,1})box(l,b,-11,5,z,11,5,z,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        box(l,b,-11,6,-21,11,6,4,Blocks.SPRUCE_SLAB.defaultBlockState());
        // The west and east railings, unbroken, and the lake rail ahead with its steps down to the dock.
        for(int z=-20;z<=1;z++)if(z!=-19&&z!=-9&&z!=1){put(l,b,-10,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());put(l,b,10,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());}
        for(int x=-9;x<=9;x++)if(Math.abs(x)>1)put(l,b,x,0,-20,Blocks.SPRUCE_FENCE.defaultBlockState());
        for(int x=-8;x<=8;x++)fit(l,b,x,4,-9,STRING_LIGHT);
        // The back wall on the land side: the jukebox against it, the way back to its left and the way on to its right.
        box(l,b,-10,0,2,10,3,2,HouseReadRegistry.PAVILION.get().defaultBlockState());box(l,b,-10,4,2,10,4,2,Blocks.SPRUCE_PLANKS.defaultBlockState());
        put(l,b,0,0,1,HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,JUKEBOX).setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));
        HouseReadRooms.door(l,b,SHELTER_ENTRY,Blocks.SPRUCE_DOOR,Direction.SOUTH);HouseReadRooms.door(l,b,HouseReadMoments.exit(HouseReadSites.Room.SHELTER),Blocks.SPRUCE_DOOR,Direction.SOUTH);
        // Steps down from the lake rail to a short dock, a dry lantern by them, and a covered rowboat tied alongside.
        for(int x=-1;x<=1;x++){put(l,b,x,-1,-21,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));box(l,b,x,-2,-27,x,-2,-22,HouseReadRegistry.PAVILION.get().defaultBlockState());}
        for(int z:new int[]{-22,-27})for(int x:new int[]{-2,2})box(l,b,x,-2,z,x,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        put(l,b,DOCK_LANTERN.getX(),DOCK_LANTERN.getY(),DOCK_LANTERN.getZ(),Blocks.LANTERN.defaultBlockState());fit(l,b,3,-1,-25,ROWBOAT);
        // A bottle on each rail beside its stranger, the two lit alike.
        for(int x:new int[]{-10,10}){put(l,b,x,0,-7,Blocks.SPRUCE_FENCE.defaultBlockState());fit(l,b,x,1,-7,BOTTLE);}
        lamp(l,b,-6,4,-8);lamp(l,b,6,4,-8);
    }
}
