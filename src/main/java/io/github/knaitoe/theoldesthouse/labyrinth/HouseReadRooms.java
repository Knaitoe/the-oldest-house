package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.*;

/** Whole new private builds. No existing room, original actor or cache is redecorated. */
public final class HouseReadRooms {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final BlockPos COAT_ENTRY=new BlockPos(0,0,2),PORCH_ENTRY=new BlockPos(0,0,13),MIRROR=new BlockPos(-4,0,-9),UMBRELLA_AT=new BlockPos(3,0,0),RAIL_AT=new BlockPos(-2,3,-2),SCREEN=new BlockPos(0,3,-8);
    public static final List<BlockPos> PEGS=List.of(new BlockPos(-4,0,-3),new BlockPos(0,0,-5),new BlockPos(4,0,-3));
    private HouseReadRooms(){}
    public static AABB bounds(BlockPos b,HouseReadSites.Room room){return room!=HouseReadSites.Room.COATROOM&&room!=HouseReadSites.Room.PORCH?HouseReadRemainingRooms.bounds(b,room):room==HouseReadSites.Room.COATROOM?new AABB(b.getX()-8,b.getY()-2,b.getZ()-16,b.getX()+9,b.getY()+6,b.getZ()+5):new AABB(b.getX()-25,b.getY()-4,b.getZ()-48,b.getX()+26,b.getY()+18,b.getZ()+16);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState state){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),state,F);}
    private static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    public static void fitting(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part){put(l,b,at,HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,part));}
    public static void door(ServerLevel l,BlockPos b,BlockPos at,Block block,Direction facing){var s=block.defaultBlockState().setValue(DoorBlock.FACING,facing);put(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));put(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
    /** Each room's way back. The shelter's sits beside its jukebox on the land side (0.4.159). */
    public static BlockPos entry(HouseReadSites.Room room){return room==HouseReadSites.Room.PORCH?PORCH_ENTRY:room==HouseReadSites.Room.SHELTER?HouseReadRemainingRooms.SHELTER_ENTRY:COAT_ENTRY;}
    public static int[] shuffle(UUID owner){var values=new ArrayList<>(List.of(0,1,2));Collections.shuffle(values,new Random(owner.getMostSignificantBits()^owner.getLeastSignificantBits()^138));return values.stream().mapToInt(Integer::intValue).toArray();}
    public static void build(ServerLevel l,BlockPos b,HouseReadSites.Room room,UUID owner){if(room==HouseReadSites.Room.COATROOM)coatroom(l,b,owner);else if(room==HouseReadSites.Room.PORCH)porch(l,b);else HouseReadRemainingRooms.build(l,b,room,owner);}
    public static BlockPos note(HouseReadSites.Room room){return switch(room){case COATROOM->UMBRELLA_AT;case PORCH->RAIL_AT;case WAITING->HouseReadRemainingRooms.CALENDAR_AT;case BATHROOM->HouseReadRemainingRooms.POSTCARD_AT;case SHELTER->HouseReadRemainingRooms.JUKEBOX_AT;};}
    private static void coatroom(ServerLevel l,BlockPos b,UUID owner){
        box(l,b,-7,-2,-15,7,5,3,Blocks.AIR.defaultBlockState());
        box(l,b,-7,-2,-15,7,-1,3,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-7,4,-15,7,4,3,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-7,0,-15,-7,3,3,HouseReadRegistry.WALLPAPER.get().defaultBlockState());box(l,b,7,0,-15,7,3,3,HouseReadRegistry.WALLPAPER.get().defaultBlockState());
        box(l,b,-7,0,-15,7,3,-15,HouseReadRegistry.WALLPAPER.get().defaultBlockState());box(l,b,-7,0,2,7,3,3,HouseReadRegistry.WALLPAPER.get().defaultBlockState());
        box(l,b,-1,-1,-1,1,-1,1,Blocks.BROWN_WOOL.defaultBlockState());
        door(l,b,COAT_ENTRY,Blocks.OAK_DOOR,Direction.SOUTH);put(l,b,COAT_ENTRY.south(),Blocks.AIR.defaultBlockState());put(l,b,COAT_ENTRY.south().above(),Blocks.AIR.defaultBlockState());
        int[] order=shuffle(owner);var coats=new HouseReadPropBlock.Part[]{WOOL,TRENCH,BARN};var boots=new HouseReadPropBlock.Part[]{WOOL_BOOTS,TRENCH_BOOTS,BARN_BOOTS};
        for(int i=0;i<3;i++){var at=PEGS.get(i);fitting(l,b,at,coats[order[i]]);fitting(l,b,at.east(),boots[order[i]]);}
        box(l,b,-7,0,-10,-2,3,-10,HouseReadRegistry.WALLPAPER.get().defaultBlockState());box(l,b,2,0,-10,7,3,-10,HouseReadRegistry.WALLPAPER.get().defaultBlockState());
        box(l,b,-7,1,-6,-7,2,-5,Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState());box(l,b,-8,0,-7,-8,3,-4,Blocks.GRAY_TERRACOTTA.defaultBlockState());
        fitting(l,b,MIRROR,HouseReadPropBlock.Part.MIRROR);fitting(l,b,MIRROR.above(),MIRROR_TOP);
        fitting(l,b,UMBRELLA_AT,UMBRELLA);fitting(l,b,new BlockPos(-6,0,-1),RADIATOR);fitting(l,b,new BlockPos(-3,0,-9),TOWEL);
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)fitting(l,b,new BlockPos(x,y,-10),CURTAIN);
        // The wooden bench by the coats (0.4.159), its back to the wall.
        for(int z=-7;z<=-5;z++)put(l,b,new BlockPos(6,0,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        box(l,b,-5,-1,-14,5,-1,-12,Blocks.RED_WOOL.defaultBlockState());put(l,b,new BlockPos(0,3,-13),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));put(l,b,new BlockPos(0,3,-2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private static void porch(ServerLevel l,BlockPos b){
        box(l,b,-24,-3,-47,24,16,15,Blocks.AIR.defaultBlockState());
        box(l,b,-24,-3,-47,24,-2,15,Blocks.DIRT.defaultBlockState());box(l,b,-24,-1,-47,24,-1,15,Blocks.GRASS_BLOCK.defaultBlockState());
        for(int x=-23;x<=23;x++)for(int z=-46;z<=14;z++)if(Math.abs(x)>20||z<-44){int h=(Math.abs(x)+Math.abs(z))%3;box(l,b,x,0,z,x,h,z,Blocks.DIRT.defaultBlockState());put(l,b,new BlockPos(x,h+1,z),Blocks.GRASS_BLOCK.defaultBlockState());}
        // A real raised porch leaves two clear blocks below its floor and a side opening in the lattice.
        box(l,b,-6,2,-24,6,2,-2,Blocks.OAK_PLANKS.defaultBlockState());
        box(l,b,-6,3,-24,-6,10,-8,HouseReadRegistry.CLAPBOARD.get().defaultBlockState());box(l,b,6,3,-24,6,10,-8,HouseReadRegistry.CLAPBOARD.get().defaultBlockState());
        box(l,b,-6,3,-24,6,10,-24,HouseReadRegistry.CLAPBOARD.get().defaultBlockState());box(l,b,-6,3,-8,6,10,-8,HouseReadRegistry.CLAPBOARD.get().defaultBlockState());
        box(l,b,-6,7,-24,6,7,-8,Blocks.OAK_PLANKS.defaultBlockState());box(l,b,-7,11,-25,7,11,-7,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-5,2,-23,5,2,-9,HouseReadRegistry.LINOLEUM.get().defaultBlockState());
        for(int x:new int[]{-3,3})for(int z:new int[]{-24,-8})box(l,b,x,4,z,x+1,5,z,Blocks.GLASS_PANE.defaultBlockState());
        for(int x:new int[]{-6,6})for(int z=-7;z<=-2;z++){put(l,b,new BlockPos(x,3,z),Blocks.OAK_FENCE.defaultBlockState());if(z!=-5)put(l,b,new BlockPos(x,0,z),Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN,true).setValue(TrapDoorBlock.FACING,x<0?Direction.EAST:Direction.WEST));}
        for(int y=0;y<3;y++)put(l,b,new BlockPos(0,y,-y),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        for(int x:new int[]{-6,6}){box(l,b,x,4,-3,x,6,-3,Blocks.OAK_FENCE.defaultBlockState());}box(l,b,-6,7,-8,6,7,-2,Blocks.OAK_SLAB.defaultBlockState());
        door(l,b,SCREEN,HouseReadRegistry.SCREEN.get(),Direction.SOUTH);put(l,b,new BlockPos(0,6,-7),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        door(l,b,new BlockPos(0,3,-24),Blocks.OAK_DOOR,Direction.SOUTH);
        box(l,b,-1,3,-18,1,3,-17,Blocks.SPRUCE_PLANKS.defaultBlockState());fitting(l,b,new BlockPos(0,4,-17),PLATE);
        for(int x:new int[]{-2,2})put(l,b,new BlockPos(x,3,-17),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<0?Direction.EAST:Direction.WEST));
        put(l,b,new BlockPos(0,6,-18),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));put(l,b,new BlockPos(3,6,-12),Blocks.CHAIN.defaultBlockState());put(l,b,new BlockPos(3,5,-12),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        fitting(l,b,RAIL_AT,RAIL);
        // Gravel continues past the mailbox into the trees. None of the three ways on needs a key.
        box(l,b,11,-1,-24,15,-1,11,Blocks.GRAVEL.defaultBlockState());box(l,b,11,-1,-44,15,-1,-25,Blocks.COARSE_DIRT.defaultBlockState());box(l,b,-16,-1,-10,-7,-1,-6,Blocks.GRAVEL.defaultBlockState());
        fitting(l,b,new BlockPos(12,0,-23),MAILBOX);door(l,b,new BlockPos(14,0,-42),Blocks.OAK_DOOR,Direction.SOUTH);
        box(l,b,-15,0,-11,-15,0,-5,Blocks.OAK_FENCE.defaultBlockState());put(l,b,new BlockPos(-15,0,-8),Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));door(l,b,PORCH_ENTRY,Blocks.OAK_DOOR,Direction.SOUTH);
        box(l,b,-2,0,13,2,3,14,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));door(l,b,PORCH_ENTRY,Blocks.OAK_DOOR,Direction.SOUTH);box(l,b,0,0,14,0,1,14,Blocks.AIR.defaultBlockState());
        // An old swing set, supported chains, and a seat that can be gently nudged.
        for(int x:new int[]{-12,-7})box(l,b,x,0,2,x,4,2,Blocks.OAK_FENCE.defaultBlockState());box(l,b,-12,4,2,-7,4,2,Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int x:new int[]{-10,-9})box(l,b,x,1,2,x,3,2,Blocks.CHAIN.defaultBlockState());fitting(l,b,new BlockPos(-10,0,2),SWING);fitting(l,b,new BlockPos(-9,0,2),SWING);
        for(int[] tree:new int[][]{{-19,-35},{-17,-22},{19,-33},{18,-13},{-19,8},{19,8},{7,-43}}){box(l,b,tree[0],0,tree[1],tree[0],6,tree[1],Blocks.OAK_LOG.defaultBlockState());box(l,b,tree[0]-2,4,tree[1]-2,tree[0]+2,8,tree[1]+2,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
    }
}
