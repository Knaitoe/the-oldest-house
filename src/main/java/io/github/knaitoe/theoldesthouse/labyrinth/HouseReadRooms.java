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
    /**
     * 0.4.160, the owner's playtest notes: a close front hall. The coats hang on the west wall's hooks after an empty hanger,
     * the boots on the mat below them (0.4.169: twelve coats and six pairs, more hooks at the east wall's far end); the bench,
     * radiator and window are on the east; the hall glass and its towel on the back wall beside the curtained way on; the
     * umbrella stand by the screen door.
     */
    public static final BlockPos COAT_ENTRY=new BlockPos(0,0,2),PORCH_ENTRY=new BlockPos(0,0,13),MIRROR=new BlockPos(2,0,-8),TOWEL_AT=new BlockPos(3,1,-8),UMBRELLA_AT=new BlockPos(3,0,1),RAIL_AT=new BlockPos(-2,3,-2),SCREEN=new BlockPos(0,3,-8),CURTAIN_AT=new BlockPos(0,0,-9);
    /** Every coat hook, in the order the wardrobe fills them (0.4.169). */
    public static final List<BlockPos> PEGS=HouseReadWardrobe.coatHooks();
    /** The porch's lights: the sconce by the screen door, the kitchen pendant and the lamp; supper's end puts them out. */
    public static final List<BlockPos> PORCH_LIGHTS=List.of(new BlockPos(1,4,-7),new BlockPos(0,6,-18),new BlockPos(3,4,-12));
    private HouseReadRooms(){}
    public static AABB bounds(BlockPos b,HouseReadSites.Room room){return room==HouseReadSites.Room.KITCHEN?HouseReadKitchen.bounds(b):room!=HouseReadSites.Room.COATROOM&&room!=HouseReadSites.Room.PORCH?HouseReadRemainingRooms.bounds(b,room):room==HouseReadSites.Room.COATROOM?new AABB(b.getX()-8,b.getY()-2,b.getZ()-16,b.getX()+9,b.getY()+6,b.getZ()+5):new AABB(b.getX()-25,b.getY()-4,b.getZ()-48,b.getX()+26,b.getY()+18,b.getZ()+16);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState state){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),state,F);}
    private static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    public static BlockState part(HouseReadPropBlock.Part part){return HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,part);}
    public static void fitting(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part){put(l,b,at,part(part));}
    private static void fitting(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part,Direction facing){put(l,b,at,part(part).setValue(HorizontalDirectionalBlock.FACING,facing));}
    private static void fitting(ServerLevel l,BlockPos b,int x,int y,int z,HouseReadPropBlock.Part part,Direction facing){fitting(l,b,new BlockPos(x,y,z),part,facing);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    public static void door(ServerLevel l,BlockPos b,BlockPos at,Block block,Direction facing){var s=block.defaultBlockState().setValue(DoorBlock.FACING,facing);put(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));put(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
    /** Each room's way back. The shelter's sits beside its jukebox on the land side (0.4.159). */
    public static BlockPos entry(HouseReadSites.Room room){return room==HouseReadSites.Room.PORCH?PORCH_ENTRY:room==HouseReadSites.Room.SHELTER?HouseReadRemainingRooms.SHELTER_ENTRY:COAT_ENTRY;}
    public static void build(ServerLevel l,BlockPos b,HouseReadSites.Room room,UUID owner){
        // 0.4.161: an unentered room from an older layout is rebuilt in place, so nothing of the old one may stand outside the new.
        var area=bounds(b,room);BuildBlocks.box(l,BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX-1,area.maxY-1,area.maxZ-1),Blocks.AIR.defaultBlockState(),F);
        if(room==HouseReadSites.Room.COATROOM)coatroom(l,b,owner);else if(room==HouseReadSites.Room.PORCH)porch(l,b);else if(room==HouseReadSites.Room.KITCHEN)HouseReadKitchen.build(l,b,owner);else HouseReadRemainingRooms.build(l,b,room,owner);reshape(l,b,room);}
    /**
     * 0.4.160, the owner's playtest notes ("blocks not connecting"): the build places blocks without neighbour updates, so
     * fences, panes, stairs and gates are given their joined shapes once everything stands, one layer at a time.
     */
    private static void reshape(ServerLevel l,BlockPos b,HouseReadSites.Room room){
        var area=bounds(b,room);
        for(int y=(int)Math.floor(area.minY);y<area.maxY;y++){final int layer=y;BuildBlocks.after(l,()->{
            for(int x=(int)Math.floor(area.minX);x<area.maxX;x++)for(int z=(int)Math.floor(area.minZ);z<area.maxZ;z++){
                var at=new BlockPos(x,layer,z);var state=l.getBlockState(at);if(state.isAir()||!state.getFluidState().isEmpty())continue;
                var shaped=Block.updateFromNeighbourShapes(state,l,at);if(!shaped.equals(state)&&!shaped.isAir())l.setBlock(at,shaped,F);
            }
        });}
    }
    public static BlockPos note(HouseReadSites.Room room){return switch(room){case COATROOM->UMBRELLA_AT;case PORCH->RAIL_AT;case WAITING->HouseReadRemainingRooms.CALENDAR_AT;case BATHROOM->HouseReadRemainingRooms.POSTCARD_AT;case SHELTER->HouseReadRemainingRooms.JUKEBOX_AT;case KITCHEN->HouseReadKitchen.TIN_LEFT_AT;};}
    private static void coatroom(ServerLevel l,BlockPos b,UUID owner){
        HouseReadRemainingRooms.shell(l,b,4,-9,HouseReadRegistry.WALLPAPER.get().defaultBlockState(),HouseReadRegistry.FLOORBOARDS.get().defaultBlockState(),HouseReadRegistry.CEILING.get().defaultBlockState(),HouseReadRegistry.SCREEN.get());
        box(l,b,-4,0,-9,-4,0,2,HouseReadRegistry.WAINSCOT.get());box(l,b,4,0,-9,4,0,2,HouseReadRegistry.WAINSCOT.get());box(l,b,-4,0,-9,4,0,-9,HouseReadRegistry.WAINSCOT.get());box(l,b,-4,0,2,4,0,2,HouseReadRegistry.WAINSCOT.get());
        door(l,b,COAT_ENTRY,HouseReadRegistry.SCREEN.get(),Direction.SOUTH);
        // The rain comes through the screen: a wet porch step outside the door.
        box(l,b,-1,-1,3,1,-1,5,HouseReadRegistry.PORCH_BOARDS.get());box(l,b,-1,0,5,1,3,5,HouseReadRegistry.OVERCAST.get());box(l,b,-2,0,3,-2,3,5,HouseReadRegistry.OVERCAST.get());box(l,b,2,0,3,2,3,5,HouseReadRegistry.OVERCAST.get());box(l,b,-1,4,3,1,4,5,HouseReadRegistry.OVERCAST.get());box(l,b,-1,0,3,1,3,4,Blocks.AIR);put(l,b,COAT_ENTRY.south(),Blocks.AIR.defaultBlockState());
        // 0.4.169: the wardrobe. An empty hanger by the door, then twelve coats on the hooks down the west wall and the far end
        // of the east, and six pairs of boots on the mat under the west hooks, each in an order of the reader's own.
        fitting(l,b,HouseReadWardrobe.EMPTY_HOOK,EMPTY_HANGER,Direction.EAST);
        var hooks=HouseReadWardrobe.coatHooks();var coats=HouseReadWardrobe.coats();int[] coatOrder=HouseReadWardrobe.order(owner,false);
        for(int i=0;i<hooks.size();i++)fitting(l,b,hooks.get(i),HouseReadWardrobe.part(coats.get(coatOrder[i])),HouseReadWardrobe.facing(hooks.get(i)));
        var boots=HouseReadWardrobe.footwear();int[] bootOrder=HouseReadWardrobe.order(owner,true);
        for(int i=0;i<HouseReadWardrobe.BOOT_PLACES.size();i++)fitting(l,b,HouseReadWardrobe.BOOT_PLACES.get(i),HouseReadWardrobe.part(boots.get(bootOrder[i])),Direction.EAST);
        // 0.4.172: the three outfits on their hangers along the rail on the back wall, west of the curtain.
        var outfits=HouseReadWardrobe.outfits();int[] outfitOrder=HouseReadWardrobe.outfitOrder(owner);
        for(int i=0;i<HouseReadWardrobe.RAIL.size();i++)fitting(l,b,HouseReadWardrobe.RAIL.get(i),HouseReadWardrobe.part(outfits.get(outfitOrder[i])),Direction.SOUTH);
        // The east side: the bench under the window, the radiator by the door.
        for(int z=-3;z<=-2;z++)fitting(l,b,3,0,z,BENCH,Direction.WEST);box(l,b,4,1,-3,4,2,-2,HouseReadRegistry.WINDOW.get());box(l,b,5,0,-4,6,3,-1,HouseReadRegistry.OVERCAST.get());box(l,b,5,1,-3,5,2,-2,Blocks.AIR);
        fitting(l,b,3,0,0,RADIATOR,Direction.WEST);fitting(l,b,UMBRELLA_AT,UMBRELLA);
        // The back wall: the hall glass and its towel, and the curtained way on into a short dark passage.
        fitting(l,b,MIRROR,HouseReadPropBlock.Part.MIRROR);fitting(l,b,MIRROR.above(),MIRROR_TOP);fitting(l,b,TOWEL_AT,TOWEL_BAR);
        box(l,b,-1,-1,-12,1,2,-10,HouseReadRegistry.WALLPAPER.get());box(l,b,0,0,-11,0,1,-10,Blocks.AIR);
        for(int y=0;y<=1;y++)fitting(l,b,CURTAIN_AT.above(y),CURTAIN);
        for(int z=1;z>=-8;z--)fitting(l,b,new BlockPos(0,0,z),RUNNER);
        fitting(l,b,new BlockPos(0,3,-3),PENDANT);
    }
    private static void porch(ServerLevel l,BlockPos b){
        box(l,b,-24,-3,-47,24,17,15,Blocks.AIR.defaultBlockState());
        box(l,b,-24,-3,-47,24,-2,15,Blocks.DIRT.defaultBlockState());box(l,b,-24,-1,-47,24,-1,15,Blocks.GRASS_BLOCK.defaultBlockState());
        for(int x=-23;x<=23;x++)for(int z=-46;z<=14;z++)if(Math.abs(x)>20||z<-44){int h=(Math.abs(x)+Math.abs(z))%3;box(l,b,x,0,z,x,h,z,Blocks.DIRT.defaultBlockState());put(l,b,new BlockPos(x,h+1,z),Blocks.GRASS_BLOCK.defaultBlockState());}
        // 0.4.160: the house in its own materials. A raised porch on posts, two clear blocks under its floor and a lattice
        // skirt open at both sides; the house behind it, papered inside, under a gabled shingle roof.
        box(l,b,-6,2,-24,6,2,-2,HouseReadRegistry.PORCH_BOARDS.get());
        box(l,b,-6,3,-24,-6,10,-8,HouseReadRegistry.CLAPBOARD.get());box(l,b,6,3,-24,6,10,-8,HouseReadRegistry.CLAPBOARD.get());
        box(l,b,-6,3,-24,6,10,-24,HouseReadRegistry.CLAPBOARD.get());box(l,b,-6,3,-8,6,10,-8,HouseReadRegistry.CLAPBOARD.get());
        box(l,b,-5,3,-23,-5,6,-9,HouseReadRegistry.WALLPAPER.get());box(l,b,5,3,-23,5,6,-9,HouseReadRegistry.WALLPAPER.get());
        box(l,b,-5,7,-23,5,7,-9,HouseReadRegistry.CEILING.get());box(l,b,-4,2,-23,4,2,-9,HouseReadRegistry.LINOLEUM.get());
        // A brick foundation under the house; only the porch is open underneath.
        box(l,b,-6,0,-24,6,1,-8,HouseReadRegistry.LOT_WALL.get());
        // Gable roof: shingle stairs up both sides to the ridge, clapboard filling the gable ends, an overhang all round.
        for(int k=0;k<7;k++){int y=11+k;
            box(l,b,-(7-k),y,-25,-(7-k),y,-7,HouseReadRegistry.SHINGLE_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            box(l,b,7-k,y,-25,7-k,y,-7,HouseReadRegistry.SHINGLE_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
            if(6-k>=-(6-k)){box(l,b,-(6-k),y,-24,6-k,y,-24,HouseReadRegistry.CLAPBOARD.get());box(l,b,-(6-k),y,-8,6-k,y,-8,HouseReadRegistry.CLAPBOARD.get());}}
        box(l,b,0,17,-25,0,17,-7,HouseReadRegistry.SHINGLES.get());box(l,b,0,18,-25,0,18,-7,HouseReadRegistry.SHINGLE_SLAB.get());
        // Windows front and back, the screen door with its sconce, the front door at the back of the hall.
        for(int x:new int[]{-3,3})for(int z:new int[]{-24,-8})box(l,b,x,4,z,x+1,5,z,HouseReadRegistry.WINDOW.get().defaultBlockState());
        door(l,b,SCREEN,HouseReadRegistry.SCREEN.get(),Direction.SOUTH);fitting(l,b,PORCH_LIGHTS.get(0),PORCH_LIGHT);
        door(l,b,new BlockPos(0,3,-24),HouseReadRegistry.PANEL_DOOR.get(),Direction.SOUTH);
        // The porch: posts from the ground to the roof at its corners and middle, railings round it with a gap for the steps,
        // a shingle roof, and lattice between the posts below, open at z -5 on both sides.
        for(int x:new int[]{-6,6})for(int z:new int[]{-7,-2}){box(l,b,x,0,z,x,1,z,HouseReadRegistry.POST.get());box(l,b,x,3,z,x,6,z,HouseReadRegistry.POST.get());}
        for(int x:new int[]{-6,6})for(int z=-6;z<=-3;z++){put(l,b,new BlockPos(x,3,z),HouseReadRegistry.RAIL.get().defaultBlockState());if(z!=-5)box(l,b,x,0,z,x,1,z,HouseReadRegistry.LATTICE.get());}
        for(int x=-5;x<=5;x++)if(Math.abs(x)>1){put(l,b,new BlockPos(x,3,-2),HouseReadRegistry.RAIL.get().defaultBlockState());box(l,b,x,0,-2,x,1,-2,HouseReadRegistry.LATTICE.get());}
        // The steps rest on solid boards, so nothing hangs over air.
        for(int x=-1;x<=1;x++){put(l,b,new BlockPos(x,0,-1),HouseReadRegistry.PORCH_BOARDS.get().defaultBlockState());box(l,b,x,0,-2,x,1,-2,HouseReadRegistry.PORCH_BOARDS.get());}
        box(l,b,-7,7,-7,7,7,-1,HouseReadRegistry.SHINGLE_SLAB.get());box(l,b,-7,6,-1,7,6,-1,HouseReadRegistry.TRIM.get());
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)put(l,b,new BlockPos(x,y,-y),HouseReadRegistry.PORCH_STEP.get().defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        // The kitchen through the window: the warm table and its chairs, the plate kept warm, the pendant and a lamp.
        fitting(l,b,-1,3,-17,TABLE_END,Direction.EAST);fitting(l,b,0,3,-17,TABLE_MID,Direction.SOUTH);fitting(l,b,1,3,-17,TABLE_END,Direction.WEST);fitting(l,b,new BlockPos(0,4,-17),PLATE);
        fitting(l,b,0,3,-16,CHAIR,Direction.SOUTH);fitting(l,b,0,3,-18,CHAIR,Direction.NORTH);fitting(l,b,PORCH_LIGHTS.get(1),PENDANT);fitting(l,b,3,3,-12,SIDE_TABLE,Direction.SOUTH);fitting(l,b,PORCH_LIGHTS.get(2),LAMP);
        fitting(l,b,RAIL_AT,RAIL,Direction.SOUTH);
        // Gravel continues past the mailbox into the trees. None of the three ways on needs a key.
        box(l,b,11,-1,-24,15,-1,11,Blocks.GRAVEL.defaultBlockState());box(l,b,11,-1,-44,15,-1,-25,Blocks.COARSE_DIRT.defaultBlockState());box(l,b,-16,-1,-10,-7,-1,-6,Blocks.GRAVEL.defaultBlockState());
        fitting(l,b,new BlockPos(12,0,-23),MAILBOX);door(l,b,new BlockPos(14,0,-42),HouseReadRegistry.BOATHOUSE_DOOR.get(),Direction.SOUTH);
        box(l,b,-15,0,-11,-15,0,-5,HouseReadRegistry.PICKET.get());put(l,b,new BlockPos(-15,0,-8),HouseReadRegistry.GATE.get().defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        box(l,b,-2,0,13,2,3,14,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));door(l,b,PORCH_ENTRY,HouseReadRegistry.BOATHOUSE_DOOR.get(),Direction.SOUTH);box(l,b,0,0,14,0,1,14,Blocks.AIR.defaultBlockState());
        // An old swing set on painted posts, rope, and a seat that can be gently nudged.
        for(int x:new int[]{-12,-7})box(l,b,x,0,2,x,3,2,HouseReadRegistry.POST.get());box(l,b,-12,4,2,-7,4,2,HouseReadRegistry.POST.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int x:new int[]{-10,-9})box(l,b,x,1,2,x,3,2,HouseReadRegistry.ROPE.get());fitting(l,b,new BlockPos(-10,0,2),SWING);fitting(l,b,new BlockPos(-9,0,2),SWING);
        for(int[] tree:new int[][]{{-19,-35},{-17,-22},{19,-33},{18,-13},{-19,8},{19,8},{7,-43}}){box(l,b,tree[0],0,tree[1],tree[0],6,tree[1],Blocks.OAK_LOG.defaultBlockState());box(l,b,tree[0]-2,4,tree[1]-2,tree[0]+2,8,tree[1]+2,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
    }
}
