package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.PinehookBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.364, Last Weekend at Pinehook (docs/PINEHOOK_0_4_364.md): the cabin in the pines by the lake, under the outside sky at the end of a
 * summer evening. Every position is relative to the base, the ground just inside the House's door; north is -z.
 *
 * <p>The House's door opens in the back of a woodshed at the end of a gravel drive. The drive runs north to a parking apron in front of
 * the cabin; a dirt lane runs east from it through the trees, the way Len comes in. The cabin has a porch along its front, a living room
 * with a fireplace, the stereo and the television, a kitchen with the breaker box by the back door, a hall to a bathroom and a bedroom.
 * Behind it a path runs to the shed among the pines, where Jude's things are, and on down to a sandy beach, a dock out over the lake with
 * a T at its far end, and Len's boathouse beside it, locked.
 */
public final class PinehookSite {
    private PinehookSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PINEHOOK;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final int WEST=-36,EAST=36,NORTH=-96,LOW=-5,HIGH=18;
    /** The cabin (x -8 to 8, z -20 to -34) and its porch (z -17 to -19); the living room (z -21 to -27); the wall between at z -28; the
     * hall, the bathroom and the bedroom west of it, the kitchen east (x 3 to 7, z -29 to -33). */
    public static final int CAB_W=-8,CAB_E=8,CAB_S=-20,CAB_N=-34,PORCH_S=-17,MID=-28;
    public static final BlockPos FRONT_DOOR=new BlockPos(0,0,CAB_S),BACK_DOOR=new BlockPos(5,0,CAB_N),BATH_DOOR=new BlockPos(0,0,-31),BED_DOOR=new BlockPos(-2,0,-30);
    public static final BlockPos FRONT_WINDOW=new BlockPos(-4,1,CAB_S),SOURCE=new BlockPos(-6,0,-18),ENDING=new BlockPos(-7,0,-26);
    public static final BlockPos STEREO=new BlockPos(-6,0,-27),TV=new BlockPos(7,0,-24),TAPES=new BlockPos(7,0,-26),HEARTH=new BlockPos(-8,0,-24);
    public static final BlockPos STOVE=new BlockPos(7,0,-31),BREAKER=new BlockPos(3,1,-33),COOLER_SPOT=new BlockPos(6,0,-33),COUNTER=new BlockPos(7,0,-32),KIT=new BlockPos(1,1,-32);
    public static final BlockPos COOLER_START=new BlockPos(3,0,-18);
    public static final List<BlockPos> LAMPS=List.of(new BlockPos(-3,3,-24),new BlockPos(3,3,-24),new BlockPos(5,3,-31),new BlockPos(0,3,-30),new BlockPos(-5,3,-31),new BlockPos(0,3,-33),new BlockPos(0,2,-18));
    /** The shed among the pines (x -20 to -15, z -38 to -43): its door on the east, its window to the south, a back window to the north;
     * Jude's duffel on the bench; the bag thrown down behind it. */
    public static final int SHED_W=-20,SHED_E=-15,SHED_S=-38,SHED_N=-43;
    public static final BlockPos SHED_DOOR=new BlockPos(SHED_E,0,-40),SHED_WINDOW=new BlockPos(-18,1,SHED_S),SHED_BACK=new BlockPos(-17,1,SHED_N),DUFFEL=new BlockPos(-19,0,-41),BAG=new BlockPos(-17,0,-45);
    /** The beach (z -46 to -49) and the lake beyond, its floor at y -5; the dock (x 6 and 7) out to z -64, its T across x 2 to 11 at
     * z -62 to -64; Len's boathouse (x 12 to 18, z -45 to -54). */
    public static final int BEACH_S=-46,LAKE_S=-50,LAKE_FLOOR=-5,DOCK_W=6,DOCK_E=7,DOCK_END=-64,T_W=2,T_E=11,T_S=-62;
    public static final int BOAT_W=12,BOAT_E=18,BOAT_S=-45,BOAT_N=-54;
    public static final BlockPos BOATHOUSE_DOOR=new BlockPos(15,0,BOAT_S);
    /** Where the onward door waits: a pump house at the west end of the apron. */
    public static final BlockPos ONWARD=new BlockPos(-31,0,-13);
    /** Where the cast stand and go. */
    public static final Vec3 CASEY_PORCH=new Vec3(1.5,0,-18.5),JUDE_PORCH=new Vec3(-2.5,0,-18.5),NINA_LIVING=new Vec3(-4.5,0,-22.5),LEN_GATE=new Vec3(33.5,0,-13.5),LEN_PORCH=new Vec3(5.5,0,-18.5),
        WINDOW_OUT=new Vec3(-4.5,0,-18.6),BACK_OUT=new Vec3(5.5,0,-35.5),SHED_IN=new Vec3(-17.5,0,-40.5),SHED_OUT=new Vec3(-13.5,0,-40.5),BEHIND_SHED=new Vec3(-17.5,0,-44.5),
        KITCHEN=new Vec3(4.5,0,-30.5),BATHROOM=new Vec3(0.5,0,-32.5),BREAKER_SPOT=new Vec3(3.5,0,-32.5),DOCK_EAST=new Vec3(10.5,0,-63.5),DOCK_WEST=new Vec3(2.5,0,-63.5),LIVING=new Vec3(0.5,0,-24.5);
    /** The ways the cast walk, point by point: from the back step to the shed; from the porch round the west side to the shed; from the
     * front window round to the back door; from the shed back to the back step; Casey from the porch to the breaker box and to the
     * kitchen; from the box out to the back step; Nina from the living room to the bathroom, from the bathroom out to the shed, and back. */
    public static final Vec3[] TO_SHED={new Vec3(5.5,0,-35.5),new Vec3(5.5,0,-39.5),new Vec3(-10.5,0,-40.5),SHED_OUT,SHED_IN};
    public static final Vec3[] FRONT_TO_SHED={new Vec3(-8.5,0,-18.6),new Vec3(-10.5,0,-18.5),new Vec3(-10.5,0,-37.5),new Vec3(-10.5,0,-40.5),SHED_OUT,SHED_IN};
    public static final Vec3[] WINDOW_TO_BACK={new Vec3(-8.5,0,-18.6),new Vec3(-10.5,0,-18.5),new Vec3(-10.5,0,-37.5),new Vec3(5.5,0,-37.5),BACK_OUT};
    public static final Vec3[] FROM_SHED={SHED_OUT,new Vec3(-10.5,0,-40.5),new Vec3(5.5,0,-39.5),BACK_OUT};
    public static final Vec3[] TO_KITCHEN={new Vec3(0.5,0,-19.5),new Vec3(0.5,0,-21.5),new Vec3(4.5,0,-26.5),KITCHEN};
    public static final Vec3[] TO_BOX={new Vec3(0.5,0,-19.5),new Vec3(0.5,0,-21.5),new Vec3(4.5,0,-26.5),KITCHEN,BREAKER_SPOT};
    public static final Vec3[] BOX_TO_BACK={new Vec3(5.5,0,-33.5),BACK_OUT};
    public static final Vec3[] TO_BATHROOM={LIVING,new Vec3(0.5,0,-29.5),BATHROOM};
    public static final Vec3[] NINA_OUT={new Vec3(0.5,0,-29.5),new Vec3(0.5,0,-26.5),new Vec3(4.5,0,-26.5),new Vec3(4.5,0,-30.5),new Vec3(5.5,0,-33.5),BACK_OUT,new Vec3(5.5,0,-39.5),new Vec3(-10.5,0,-40.5),SHED_OUT};
    public static final Vec3[] NINA_IN={new Vec3(-13.5,0,-44.5),new Vec3(-10.5,0,-40.5),new Vec3(5.5,0,-39.5),BACK_OUT,new Vec3(5.5,0,-33.5),new Vec3(4.5,0,-30.5),new Vec3(4.5,0,-26.5),new Vec3(0.5,0,-26.5),new Vec3(0.5,0,-29.5),BATHROOM};
    public static final Vec3[] LEN_WALK={new Vec3(10.5,0,-13.5),new Vec3(1.5,0,-15.5),new Vec3(1.5,0,-18.0),LEN_PORCH};

    public static boolean inCabin(Vec3 r){return r.x>CAB_W&&r.x<CAB_E+1&&r.z<CAB_S&&r.z>CAB_N&&r.y>-.5&&r.y<4;}
    public static boolean inShed(Vec3 r){return r.x>SHED_W&&r.x<SHED_E&&r.z<SHED_S&&r.z>SHED_N&&r.y>-.5&&r.y<3;}
    public static boolean onPorch(Vec3 r){return r.x>=CAB_W&&r.x<CAB_E+1&&r.z<PORCH_S+1&&r.z>=CAB_S&&r.y>-.5;}
    public static boolean onDock(Vec3 r){return(r.x>=DOCK_W&&r.x<DOCK_E+1&&r.z<=LAKE_S+1&&r.z>DOCK_END||r.x>=T_W&&r.x<T_E+1&&r.z<=T_S+1&&r.z>DOCK_END)&&r.y>-.5;}
    public static boolean atDockEnd(Vec3 r){return r.x>=T_W&&r.x<T_E+1&&r.z<=T_S+1&&r.z>DOCK_END&&r.y>-.5;}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState s(Block block){return block.defaultBlockState();}
    static BlockState fit(Kind k,Direction f,int stage){return PinehookBlock.of(k,f,stage);}
    static BlockState water(){return Blocks.WATER.defaultBlockState();}
    static BlockState stairs(Block block,Direction f){return block.defaultBlockState().setValue(StairBlock.FACING,f);}
    static BlockState top(Block slab){return slab.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);}

    // ------------------------------------------------------------------------------------------------ the build
    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,LOW,NORTH,EAST,HIGH,1,s(Blocks.AIR));
        ground(l,b);
        lake(l,b);
        woodshed(l,b);
        drive(l,b);
        cabin(l,b);
        shed(l,b);
        dock(l,b);
        boathouse(l,b);
        pines(l,b);
        pumpHouse(l,b);
        lights(l,b,true);
    }
    /** The ground: grass on dirt on stone, everywhere south of the beach. */
    static void ground(ServerLevel l,BlockPos b){
        box(l,b,WEST,LOW,NORTH,EAST,LOW,1,s(Blocks.STONE));
        box(l,b,WEST,LOW+1,BEACH_S+1,EAST,-2,1,s(Blocks.DIRT));box(l,b,WEST,-1,BEACH_S+1,EAST,-1,1,s(Blocks.GRASS_BLOCK));
        var rnd=new Random(3640);
        for(int x=WEST+1;x<EAST;x++)for(int z=0;z>BEACH_S;z--){int r=rnd.nextInt(14);if(r==0)set(l,b,x,-1,z,s(Blocks.PODZOL));else if(r==1)set(l,b,x,-1,z,s(Blocks.COARSE_DIRT));}
    }
    /** The beach and the lake to the north edge, with banks along both sides. */
    static void lake(ServerLevel l,BlockPos b){
        box(l,b,WEST,LOW+1,LAKE_S+1,EAST,-2,BEACH_S,s(Blocks.SAND));box(l,b,WEST,-1,LAKE_S+1,EAST,-1,BEACH_S,s(Blocks.SAND));
        box(l,b,WEST,LAKE_FLOOR,NORTH,EAST,LAKE_FLOOR,LAKE_S,s(Blocks.GRAVEL));
        var rnd=new Random(3641);
        for(int x=WEST;x<=EAST;x++)for(int z=LAKE_S;z>=NORTH;z--)if(rnd.nextInt(5)==0)set(l,b,x,LAKE_FLOOR,z,s(rnd.nextBoolean()?Blocks.SAND:Blocks.MUD));
        box(l,b,WEST,LAKE_FLOOR+1,NORTH,EAST,-1,LAKE_S,water());
        // The shallows by the beach: a step of sand under the water.
        box(l,b,WEST,LAKE_FLOOR+1,LAKE_S-1,EAST,-3,LAKE_S,s(Blocks.SAND));
        for(int x:new int[]{WEST,WEST+1,EAST-1,EAST}){box(l,b,x,LOW+1,NORTH,x,-2,LAKE_S,s(Blocks.DIRT));set(l,b,x,-1,0,s(Blocks.GRASS_BLOCK));box(l,b,x,-1,NORTH,x,-1,LAKE_S,s(Blocks.GRASS_BLOCK));}
        box(l,b,WEST,LOW+1,NORTH,EAST,-2,NORTH,s(Blocks.DIRT));box(l,b,WEST,-1,NORTH,EAST,-1,NORTH,s(Blocks.GRASS_BLOCK));
        // Reeds and lily pads along the margins.
        for(int x=WEST+2;x<=EAST-2;x+=1){if(x>=DOCK_W-1&&x<=BOAT_E+1)continue;if(rnd.nextInt(4)==0)set(l,b,x,0,LAKE_S+1,s(Blocks.SHORT_GRASS));if(rnd.nextInt(6)==0)set(l,b,x,0,LAKE_S-3,s(Blocks.LILY_PAD));}
    }
    /** The woodshed round the House's door, open to the north, firewood stacked inside. */
    static void woodshed(ServerLevel l,BlockPos b){
        var wall=s(Blocks.SPRUCE_PLANKS);var post=s(Blocks.STRIPPED_SPRUCE_LOG);
        for(int x=-4;x<=4;x++){if(Math.abs(x)<=3&&x!=-4&&x!=4){for(int y=0;y<=3;y++)if(x!=0||y>1)set(l,b,x,y,1,wall);}else for(int y=0;y<=3;y++)set(l,b,x,y,1,post);}
        for(int z=0;z>=-3;z--)for(int y=0;y<=3;y++){set(l,b,-4,y,z,z==-3?post:wall);set(l,b,4,y,z,z==-3?post:wall);}
        box(l,b,-5,4,-4,5,4,2,s(Blocks.SPRUCE_SLAB));
        box(l,b,-3,-1,-3,3,-1,0,s(Blocks.SPRUCE_PLANKS));
        for(int z=0;z>=-2;z--)for(int y=0;y<=1;y++){set(l,b,-3,y,z,s(Blocks.OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));set(l,b,3,y,z,s(Blocks.OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));}
        set(l,b,-3,2,-1,s(Blocks.CHAIN));
    }
    /** The gravel drive to the apron, the lane east through the trees. */
    static void drive(ServerLevel l,BlockPos b){
        box(l,b,-1,-1,-12,1,-1,-4,s(Blocks.GRAVEL));
        box(l,b,-7,-1,-16,4,-1,-12,s(Blocks.GRAVEL));
        for(int x=5;x<=EAST-2;x++)for(int z=-13;z>=-14;z--)set(l,b,x,-1,z,s(Blocks.DIRT_PATH));
        // The path round the cabin's west side to the back, and on to the shed and the beach.
        for(int z=-16;z>=-37;z--)set(l,b,-10,-1,z,s(Blocks.DIRT_PATH));
        for(int z=CAB_N-1;z>=-40;z--)set(l,b,5,-1,z,s(Blocks.DIRT_PATH));
        for(int x=-14;x<=5;x++)set(l,b,x,-1,-40,s(Blocks.DIRT_PATH));
        for(int x=-10;x<=5;x++)set(l,b,x,-1,-37,s(Blocks.DIRT_PATH));
        for(int z=-41;z>BEACH_S;z--)set(l,b,6,-1,z,s(Blocks.DIRT_PATH));
        // A fire ring between the cabin and the lake, logs round it.
        set(l,b,-4,0,-43,s(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT,false));
        for(int[] c:new int[][]{{-6,-43},{-2,-43}})set(l,b,c[0],0,c[1],s(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        set(l,b,-4,0,-45,s(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        // A sign at the apron.
        LastCrossingSite.standingSign(l,b.offset(3,0,-11),new String[]{"","PINEHOOK","",""});
    }
    /** The cabin: porch, living room, kitchen, hall, bathroom and bedroom; a gable roof north to south. */
    static void cabin(ServerLevel l,BlockPos b){
        var wall=s(Blocks.SPRUCE_PLANKS);var post=s(Blocks.STRIPPED_SPRUCE_LOG);var floor=s(Blocks.SPRUCE_PLANKS);var inner=s(Blocks.BIRCH_PLANKS);
        box(l,b,CAB_W,-1,CAB_N,CAB_E,-1,CAB_S,floor);
        for(int y=0;y<=3;y++){
            for(int x=CAB_W;x<=CAB_E;x++){set(l,b,x,y,CAB_S,wall);set(l,b,x,y,CAB_N,wall);}
            for(int z=CAB_S;z>=CAB_N;z--){set(l,b,CAB_W,y,z,wall);set(l,b,CAB_E,y,z,wall);}
            for(int[] c:new int[][]{{CAB_W,CAB_S},{CAB_E,CAB_S},{CAB_W,CAB_N},{CAB_E,CAB_N}})set(l,b,c[0],y,c[1],post);
        }
        box(l,b,CAB_W,4,CAB_N,CAB_E,4,CAB_S,s(Blocks.SPRUCE_PLANKS));
        // The gable roof, its ridge running north and south over the middle.
        for(int i=0;i<=9;i++){int y=4+i;int xw=CAB_W-1+i,xe=CAB_E+1-i;
            for(int z=CAB_S+1;z>=CAB_N-1;z--){
                if(xw>=xe){set(l,b,0,y,z,s(Blocks.DARK_OAK_PLANKS));continue;}
                set(l,b,xw,y,z,stairs(Blocks.DARK_OAK_STAIRS,Direction.EAST));set(l,b,xe,y,z,stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST));
            }
            if(xw>=xe)break;
            for(int x=xw+1;x<xe;x++){set(l,b,x,y,CAB_S,wall);set(l,b,x,y,CAB_N,wall);}
        }
        // Inside walls: the living room's back wall with its doorways, the hall, the bathroom and the bedroom.
        for(int y=0;y<=3;y++){for(int x=CAB_W;x<=CAB_E;x++)set(l,b,x,y,MID,inner);for(int z=MID;z>=CAB_N;z--){set(l,b,-2,y,z,inner);set(l,b,2,y,z,inner);}for(int x=-2;x<=2;x++)set(l,b,x,y,-31,inner);}
        for(int y=0;y<=1;y++){set(l,b,0,y,MID,s(Blocks.AIR));for(int x=3;x<=5;x++)set(l,b,x,y,MID,s(Blocks.AIR));}
        for(int x=3;x<=5;x++)set(l,b,x,2,MID,top(Blocks.BIRCH_SLAB));
        NovelRooms.door(l,b.offset(FRONT_DOOR),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        NovelRooms.door(l,b.offset(BACK_DOOR),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        NovelRooms.door(l,b.offset(BATH_DOOR),Direction.NORTH,Blocks.BIRCH_DOOR,false);
        NovelRooms.door(l,b.offset(BED_DOOR),Direction.WEST,Blocks.BIRCH_DOOR,false);
        // Windows: two pairs across the front, the kitchen's and the bedroom's, one in the bathroom.
        for(int x:new int[]{-5,-4,4,5})set(l,b,x,1,CAB_S,s(Blocks.GLASS_PANE));
        for(int z:new int[]{-23,-25})set(l,b,CAB_E,1,z,s(Blocks.GLASS_PANE));
        for(int z:new int[]{-30,-32})set(l,b,CAB_E,1,z,s(Blocks.GLASS_PANE));
        set(l,b,CAB_W,1,-31,s(Blocks.GLASS_PANE));set(l,b,-5,1,CAB_N,s(Blocks.GLASS_PANE));set(l,b,0,1,CAB_N,s(Blocks.GLASS_PANE));
        // The fireplace in the west wall, its chimney outside.
        box(l,b,CAB_W-1,0,-25,CAB_W-1,10,-23,s(Blocks.COBBLESTONE));
        for(int y=0;y<=3;y++){set(l,b,CAB_W,y,-23,s(Blocks.COBBLESTONE));set(l,b,CAB_W,y,-25,s(Blocks.COBBLESTONE));}
        set(l,b,HEARTH,s(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT,true).setValue(CampfireBlock.SIGNAL_FIRE,false));set(l,b,CAB_W,1,-24,s(Blocks.AIR));
        for(int y=2;y<=3;y++)set(l,b,CAB_W,y,-24,s(Blocks.COBBLESTONE));
        set(l,b,CAB_W+1,-1,-24,s(Blocks.STONE_BRICKS));
        // The living room: the couch facing the fire, a rug, the stereo, the television with its tapes and an armchair facing it.
        for(int z=-23;z>=-25;z--)set(l,b,-3,0,z,stairs(Blocks.SPRUCE_STAIRS,Direction.EAST));
        box(l,b,-2,0,-25,2,0,-23,s(Blocks.BROWN_CARPET));
        set(l,b,STEREO,fit(Kind.STEREO,Direction.SOUTH,0));set(l,b,-7,0,-27,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));
        set(l,b,TV,fit(Kind.TV,Direction.WEST,0));set(l,b,TAPES,fit(Kind.TAPES,Direction.WEST,0));
        set(l,b,3,0,-24,stairs(Blocks.SPRUCE_STAIRS,Direction.WEST));
        set(l,b,-7,0,-21,s(Blocks.SPRUCE_SLAB));set(l,b,ENDING,top(Blocks.SPRUCE_SLAB));
        // The kitchen: the counter along the east wall with the stove and the sink, the fridge, the table, the breaker box by the back door.
        for(int z=-29;z>=-33;z--)set(l,b,CAB_E-1,0,z,s(Blocks.POLISHED_ANDESITE));
        set(l,b,STOVE,fit(Kind.STOVE,Direction.WEST,0));set(l,b,CAB_E-1,0,-30,s(Blocks.WATER_CAULDRON));
        for(int y=0;y<=1;y++)set(l,b,3,y,-29,s(Blocks.WHITE_CONCRETE));
        set(l,b,4,0,-31,top(Blocks.BIRCH_SLAB));set(l,b,4,0,-32,top(Blocks.BIRCH_SLAB));
        set(l,b,3,0,-31,stairs(Blocks.BIRCH_STAIRS,Direction.WEST));set(l,b,3,0,-32,stairs(Blocks.BIRCH_STAIRS,Direction.WEST));
        set(l,b,BREAKER,fit(Kind.BREAKER,Direction.SOUTH,0));
        // The bathroom: a basin, the first-aid kit on the wall.
        set(l,b,-1,0,-33,s(Blocks.CAULDRON));set(l,b,KIT,fit(Kind.KIT,Direction.WEST,0));set(l,b,1,0,-33,s(Blocks.QUARTZ_SLAB));
        // The bedroom: two beds, a chest of drawers.
        bed(l,b,-6,-32);bed(l,b,-4,-32);
        set(l,b,-7,0,-29,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));
        // The porch: its deck, posts and roof, a bench, the guest book on its stand, the cooler and the bags.
        box(l,b,CAB_W,-1,PORCH_S,CAB_E,-1,CAB_S+1,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=2;y++){set(l,b,CAB_W,y,PORCH_S,post);set(l,b,CAB_E,y,PORCH_S,post);set(l,b,-3,y,PORCH_S,post);set(l,b,3,y,PORCH_S,post);}
        box(l,b,CAB_W,3,PORCH_S,CAB_E,3,CAB_S+1,top(Blocks.SPRUCE_SLAB));
        for(int x=-7;x<=-4;x++)set(l,b,x,0,PORCH_S,s(Blocks.SPRUCE_FENCE));for(int x=4;x<=7;x++)set(l,b,x,0,PORCH_S,s(Blocks.SPRUCE_FENCE));
        LiteraryRooms.paper(l,b,SOURCE,LiteraryTexts.source(PLACE));
        set(l,b,6,0,-19,stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));set(l,b,7,0,-19,stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));
        set(l,b,-7,0,-19,s(Blocks.BROWN_WOOL));set(l,b,-7,0,-18,s(Blocks.GREEN_CARPET));
        set(l,b,COOLER_START,fit(Kind.COOLER,Direction.SOUTH,0));
    }
    static void bed(ServerLevel l,BlockPos b,int x,int z){
        var bed=s(Blocks.WHITE_BED).setValue(BedBlock.FACING,Direction.NORTH);
        set(l,b,x,0,z,bed.setValue(BedBlock.PART,BedPart.HEAD));set(l,b,x,0,z+1,bed.setValue(BedBlock.PART,BedPart.FOOT));
    }
    /** The shed among the pines: Jude's duffel on the bench, a lamp hook, and the bag thrown down behind it. */
    static void shed(ServerLevel l,BlockPos b){
        var wall=s(Blocks.SPRUCE_PLANKS);var post=s(Blocks.STRIPPED_SPRUCE_LOG);
        box(l,b,SHED_W,-1,SHED_N,SHED_E,-1,SHED_S,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=2;y++){
            for(int x=SHED_W;x<=SHED_E;x++){set(l,b,x,y,SHED_S,wall);set(l,b,x,y,SHED_N,wall);}
            for(int z=SHED_S;z>=SHED_N;z--){set(l,b,SHED_W,y,z,wall);set(l,b,SHED_E,y,z,wall);}
            for(int[] c:new int[][]{{SHED_W,SHED_S},{SHED_E,SHED_S},{SHED_W,SHED_N},{SHED_E,SHED_N}})set(l,b,c[0],y,c[1],post);
        }
        box(l,b,SHED_W-1,3,SHED_N-1,SHED_E+1,3,SHED_S+1,s(Blocks.SPRUCE_SLAB));
        NovelRooms.door(l,b.offset(SHED_DOOR),Direction.WEST,Blocks.SPRUCE_DOOR,false);
        set(l,b,SHED_WINDOW,s(Blocks.GLASS_PANE));set(l,b,SHED_BACK,s(Blocks.GLASS_PANE));
        set(l,b,-19,0,-42,top(Blocks.SPRUCE_SLAB));set(l,b,-19,0,-39,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));
        set(l,b,DUFFEL,fit(Kind.DUFFEL,Direction.EAST,0));
        set(l,b,-16,2,-42,s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        set(l,b,BAG,fit(Kind.BAG,Direction.SOUTH,0));
    }
    /** The dock out over the lake, its T at the far end. */
    static void dock(ServerLevel l,BlockPos b){
        for(int z=BEACH_S;z>DOCK_END;z--)for(int x=DOCK_W;x<=DOCK_E;x++)set(l,b,x,-1,z,s(Blocks.SPRUCE_PLANKS));
        box(l,b,T_W,-1,DOCK_END+1,T_E,-1,T_S,s(Blocks.SPRUCE_PLANKS));
        for(int z=LAKE_S;z>DOCK_END;z-=4)for(int x:new int[]{DOCK_W,DOCK_E})for(int y=LAKE_FLOOR+1;y<=-2;y++)set(l,b,x,y,z,s(Blocks.SPRUCE_FENCE).setValue(FenceBlock.WATERLOGGED,true));
        for(int x:new int[]{T_W,T_E})for(int y=LAKE_FLOOR+1;y<=-2;y++)set(l,b,x,y,DOCK_END+1,s(Blocks.SPRUCE_FENCE).setValue(FenceBlock.WATERLOGGED,true));
        for(int x:new int[]{T_W,T_E})set(l,b,x,0,DOCK_END+1,s(Blocks.SPRUCE_FENCE));
        set(l,b,T_E,1,DOCK_END+1,s(Blocks.LANTERN));
    }
    /** Len's boathouse: its door shut and locked, a sign. */
    static void boathouse(ServerLevel l,BlockPos b){
        var wall=s(Blocks.DARK_OAK_PLANKS);var post=s(Blocks.STRIPPED_DARK_OAK_LOG);
        box(l,b,BOAT_W,-1,BOAT_N,BOAT_E,-1,BOAT_S,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=3;y++){
            for(int x=BOAT_W;x<=BOAT_E;x++){set(l,b,x,y,BOAT_S,wall);set(l,b,x,y,BOAT_N,wall);}
            for(int z=BOAT_S;z>=BOAT_N;z--){set(l,b,BOAT_W,y,z,wall);set(l,b,BOAT_E,y,z,wall);}
            for(int[] c:new int[][]{{BOAT_W,BOAT_S},{BOAT_E,BOAT_S},{BOAT_W,BOAT_N},{BOAT_E,BOAT_N}})set(l,b,c[0],y,c[1],post);
        }
        box(l,b,BOAT_W-1,4,BOAT_N-1,BOAT_E+1,4,BOAT_S+1,s(Blocks.DARK_OAK_SLAB));
        var door=s(Blocks.IRON_DOOR).setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,BOATHOUSE_DOOR,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,BOATHOUSE_DOOR.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        NovelRooms.sign(l,b.offset(BOATHOUSE_DOOR.getX()+1,1,BOAT_S+1),Direction.SOUTH,new String[]{"PRIVATE","","NO ENTRY",""});
        set(l,b,13,1,BOAT_S,s(Blocks.GLASS_PANE));set(l,b,17,1,BOAT_S,s(Blocks.GLASS_PANE));
    }
    /** The pines: thick round the edges, scattered between the buildings, clear of the paths. */
    static void pines(ServerLevel l,BlockPos b){
        var rnd=new Random(3642);
        for(int x=WEST+2;x<=EAST-2;x+=3)for(int z=-2;z>BEACH_S+2;z-=3){
            int px=x+rnd.nextInt(2),pz=z-rnd.nextInt(2);
            if(clear(px,pz))continue;
            boolean edge=px<-24||px>22||pz>-6;
            if(edge?rnd.nextInt(3)==0:rnd.nextInt(4)!=0)continue;
            LastCrossingSite.tree(l,b,px,pz,5+rnd.nextInt(4));
        }
    }
    /** Cells kept free of trees: the buildings, the drive, the lane, the paths and their margins. */
    static boolean clear(int x,int z){
        if(x>=-6&&x<=6&&z>=-4)return true;
        if(x>=-9&&x<=6&&z<=-3&&z>=-17)return true;
        if(z<=-11&&z>=-16&&x>=-34&&x<=EAST)return true;
        if(x>=CAB_W-4&&x<=CAB_E+3&&z<=-15&&z>=CAB_N-4)return true;
        if(x>=-12&&x<=-8&&z<=-14&&z>=-39)return true;
        if(x>=SHED_W-3&&x<=8&&z<=-35&&z>=-47)return true;
        if(x>=3&&x<=9&&z<=-38)return true;
        if(x>=BOAT_W-2&&x<=BOAT_E+2&&z<=BOAT_S+2)return true;
        return false;
    }
    /** A pump house at the west end of the apron: the onward door is in its side. */
    static void pumpHouse(ServerLevel l,BlockPos b){
        var wall=s(Blocks.STONE_BRICKS);
        for(int y=0;y<=3;y++)for(int x=-35;x<=-31;x++)for(int z=-11;z>=-16;z--)if(x==-35||x==-31||z==-11||z==-16)set(l,b,x,y,z,wall);
        box(l,b,-35,4,-16,-31,4,-11,s(Blocks.STONE_BRICK_SLAB));box(l,b,-34,-1,-15,-32,-1,-12,s(Blocks.STONE_BRICKS));
        for(int y=0;y<=1;y++)set(l,b,ONWARD.getX(),y,ONWARD.getZ(),s(Blocks.AIR));
        for(int x=-30;x<=-8;x++)set(l,b,x,-1,-13,s(Blocks.DIRT_PATH));
    }
    /** Every lamp in the cabin and on the porch lit, or dark. */
    public static void lights(ServerLevel l,BlockPos b,boolean on){
        for(var at:LAMPS)set(l,b,at,fit(Kind.LAMP,Direction.NORTH,on?1:0));
        var tv=l.getBlockState(b.offset(TV));if(!on&&PinehookBlock.is(tv,Kind.TV))set(l,b,TV,tv.setValue(PinehookBlock.STAGE,0));
        var stereo=l.getBlockState(b.offset(STEREO));if(!on&&PinehookBlock.is(stereo,Kind.STEREO))set(l,b,STEREO,stereo.setValue(PinehookBlock.STAGE,0));
    }
}
