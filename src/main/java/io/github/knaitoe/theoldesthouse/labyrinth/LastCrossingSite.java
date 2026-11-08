package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.351, The Last Crossing (docs/LAST_CROSSING_0_4_351.md): the flood station where the keeper is trying to get the last cable ferry
 * across, under the outside sky. Every position is relative to the base, the ground just inside the House's door; north is -z.
 *
 * <p>The House's door opens in the back of a machine shed onto a farm road that runs north and drops into floodwater. A power line on
 * poles runs up a levee beside the road and carries the eye north-east to the station on its mound at the river's edge: the keeper's
 * office with its porch, where Vera takes the roll; the pump room, its gauge room above and the intake below, the two joined by a stair
 * and an inspection landing; the ferry ramp with the sluice pit beside it, and the ferry itself, sitting low against the landing; a narrow
 * spillway; and across it the smaller cable house, its drum, its rollers and its fairlead, the cable running out over the river. Under the
 * apron the service branches join the intake to the pit and the cable house's flooded inspection passage, their grates shut. A lamp burns
 * on the far bank, where the cable is made fast and the way on waits in a shelter.
 */
public final class LastCrossingSite {
    private LastCrossingSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_CROSSING;
    public static final int WEST=-24,EAST=60,NORTH=-112,LOW=-13,HIGH=14;
    /** The near land (z 1 to -12), the flood (its floor at y -4, its water to y -1), the levee (x 4 to 7), the mound (x 10 to 56, z -35
     * to -66), the river (z -67 to -92, its floor at y -12) and the far bank (z -93 and north). */
    public static final int NEAR_N=-12,FLOOD_FLOOR=-4,LEVEE_W=4,LEVEE_E=7,MOUND_W=10,MOUND_E=56,MOUND_S=-35,MOUND_N=-66,RIVER_S=-67,RIVER_N=-92,FAR_S=-93,RIVER_FLOOR=-12;
    /** The keeper's office (x 10 to 20, z -38 to -46) and its porch (z -36 to -37). */
    public static final int OFFICE_W=10,OFFICE_E=20,OFFICE_S=-38,OFFICE_N=-46;
    public static final BlockPos OFFICE_DOOR=new BlockPos(15,0,-38),PORCH_TABLE=new BlockPos(12,0,-37),CLIPBOARD=new BlockPos(12,1,-37),PORCH_COIL=new BlockPos(19,0,-37);
    public static final BlockPos SLATE=new BlockPos(13,1,-45),PLATE_HOME=new BlockPos(16,1,-45),STOVE=new BlockPos(19,0,-40),LAMP_HOOK=new BlockPos(11,1,-41),ENDING=new BlockPos(19,1,-44);
    public static final Vec3 VERA_PORCH=new Vec3(13.5,0,-36.5),VERA_PLATE=new Vec3(16.5,0,-43.5),VERA_STOVE=new Vec3(18.5,0,-40.5),VERA_RAMP=new Vec3(31.5,0,-57.5);
    /** The pump room (x 12 to 24, z -52 to -66): the gauge room walked at y 0, the intake below walked at y -6. */
    public static final int PUMP_W=12,PUMP_E=24,PUMP_S=-52,PUMP_N=-66,LOWER=-6;
    public static final BlockPos PUMP_DOOR=new BlockPos(18,0,-52),GAUGE=new BlockPos(23,1,-62),BRAKE=new BlockPos(22,0,-63),PIPE_A_UP=new BlockPos(23,1,-64),PUMP_BRACKET=new BlockPos(13,1,-62);
    public static final BlockPos BLEED=new BlockPos(16,-6,-64),PUMP=new BlockPos(20,-6,-64),GRATE=new BlockPos(16,-7,-63),PIPE_A_DOWN=new BlockPos(15,-5,-65),WRENCH_RUN=new BlockPos(15,-6,-64);
    public static final List<BlockPos> VALVES=List.of(new BlockPos(17,-6,-64),new BlockPos(18,-6,-64),new BlockPos(19,-6,-64));
    /** The inspection landing, the opening in the gauge room's floor over the intake. */
    public static final int LANDING_W=16,LANDING_E=19,LANDING_S=-62,LANDING_N=-64;
    /** The sluice pit beside the ramp (x 25 and 26, z -59 to -61, its floor at y -8), its wheel below and the pin on the deck above. */
    public static final BlockPos WHEEL=new BlockPos(26,-7,-61),PIN=new BlockPos(26,0,-61),PIPE_B_DOWN=new BlockPos(25,-5,-61),PIPE_B_UP=new BlockPos(25,1,-61);
    /** The ramp head and landing (x 28 to 37, z -56 to -63), the bell, the loose line, the depth post, and where the line is staged. */
    public static final BlockPos BELL=new BlockPos(36,0,-58),RAMP_COIL=new BlockPos(32,0,-63),STAGED_COIL=new BlockPos(34,0,-57),DEPTH=new BlockPos(29,0,-64);
    public static final Vec3 LANDING_SPOT=new Vec3(33.5,0,-61.5);
    /** The ferry: its deck x 30 to 36, z -64 to -76, low against the sill. */
    public static final int FERRY_W=30,FERRY_E=36,FERRY_S=-64,FERRY_N=-76;
    /** The spillway's channel (x 39 and 40) from its culvert at z -42 to the river, and the footbridge across it. */
    public static final int SPILL_W=39,SPILL_E=40,SPILL_S=-42,BRIDGE_Z=-50;
    /** The cable house (x 42 to 52, z -54 to -66): the drum, the brake, the rollers along x 47, the fairlead, the hatch, the dry ledge. */
    public static final int CABLE_W=42,CABLE_E=52,CABLE_S=-54,CABLE_N=-66,CABLE_X=47,LEDGE_X=43;
    public static final BlockPos CABLE_DOOR=new BlockPos(45,0,-54),DRUM=new BlockPos(47,0,-56),CABLE_BRAKE=new BlockPos(49,0,-57),FAIRLEAD=new BlockPos(47,0,-66),HATCH=new BlockPos(45,-1,-61),CABLE_BRACKET=new BlockPos(51,1,-60);
    public static final List<BlockPos> ROLLERS=List.of(new BlockPos(47,0,-58),new BlockPos(47,0,-60),new BlockPos(47,0,-62),new BlockPos(47,0,-64));
    public static final BlockPos LOOSE_ROLLER=new BlockPos(47,0,-60);
    /** The far bank: the landing, the lamp, the cable's anchor and the shelter whose door is the way on. */
    public static final BlockPos FAR_LAMP=new BlockPos(37,3,-95),ANCHOR=new BlockPos(47,0,-94),ONWARD=new BlockPos(43,0,-98);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean nearLand(Vec3 r){return r.z>NEAR_N-.5;}
    public static boolean onMound(Vec3 r){return r.x>=MOUND_W&&r.x<MOUND_E+1&&r.z<=MOUND_S+1&&r.z>MOUND_N;}
    public static boolean onPorch(Vec3 r){return r.x>=OFFICE_W&&r.x<OFFICE_E+1&&r.z<=MOUND_S&&r.z>OFFICE_S&&r.y>-.5&&r.y<3;}
    public static boolean inOffice(Vec3 r){return r.x>OFFICE_W&&r.x<OFFICE_E&&r.z<OFFICE_S&&r.z>OFFICE_N&&r.y>-.5&&r.y<4;}
    public static boolean inGaugeRoom(Vec3 r){return r.x>PUMP_W&&r.x<PUMP_E&&r.z<PUMP_S&&r.z>PUMP_N&&r.y>-.6&&r.y<5;}
    public static boolean inIntake(Vec3 r){return r.x>PUMP_W&&r.x<PUMP_E&&r.z<PUMP_S&&r.z>PUMP_N&&r.y<-1&&r.y>-7;}
    public static boolean inPit(Vec3 r){return r.x>=25&&r.x<27&&r.z<=-58&&r.z>-62&&r.y<-.5;}
    public static boolean atRamp(Vec3 r){return r.x>=28&&r.x<38&&r.z<=-55&&r.z>-64&&r.y>-.6;}
    public static boolean onFerry(Vec3 r){return r.x>=FERRY_W&&r.x<FERRY_E+1&&r.z<=FERRY_S+1&&r.z>FERRY_N&&r.y>-.6;}
    public static boolean inCableHouse(Vec3 r){return r.x>CABLE_W&&r.x<CABLE_E&&r.z<CABLE_S&&r.z>CABLE_N&&r.y>-.6;}
    public static boolean onLedge(Vec3 r){return inCableHouse(r)&&r.x>=LEDGE_X&&r.x<LEDGE_X+1&&r.y>=.4;}
    public static boolean onBridge(Vec3 r){return r.x>=SPILL_W-1&&r.x<SPILL_E+2&&Math.abs(r.z-(BRIDGE_Z+.5))<1.2&&r.y>-.6;}
    public static boolean inRiver(Vec3 r){return r.z<RIVER_S+1&&r.z>FAR_S+1&&!onFerry(r);}
    public static boolean farBank(Vec3 r){return r.z<=FAR_S+1;}
    /** The station: anywhere on the mound, in its buildings above or below, or on the ferry. */
    public static boolean atStation(Vec3 r){return onMound(r)||onPorch(r)||inIntake(r)||inPit(r)||onFerry(r);}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState s(Block block){return block.defaultBlockState();}
    static BlockState fit(Kind k,Direction f){return CrossingBlock.of(k,f);}
    static BlockState fit(Kind k,Direction f,int stage){return CrossingBlock.of(k,f,stage);}
    static BlockState water(){return Blocks.WATER.defaultBlockState();}
    static BlockState chain(Direction.Axis axis){return Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    static BlockState fence(Block block,boolean wet){var st=block.defaultBlockState();return st.hasProperty(BlockStateProperties.WATERLOGGED)?st.setValue(BlockStateProperties.WATERLOGGED,wet):st;}

    // ------------------------------------------------------------------------------------------------ the build
    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,LOW,NORTH,EAST,HIGH,1,s(Blocks.AIR));
        ground(l,b);
        road(l,b);
        levee(l,b);
        mound(l,b);
        office(l,b);
        pumpRoom(l,b);
        pit(l,b);
        branches(l,b);
        landing(l,b);
        ferry(l,b);
        spillway(l,b);
        cableHouse(l,b);
        farBank(l,b);
        powerLine(l,b);
        living(l,b);
    }
    /** The land, the flood, the river and the far bank, before anything stands on them. */
    static void ground(ServerLevel l,BlockPos b){
        box(l,b,WEST,LOW,NORTH,EAST,LOW,1,s(Blocks.STONE));
        // The near land: grass over dirt.
        box(l,b,WEST,LOW+1,NEAR_N,EAST,-2,1,s(Blocks.DIRT));box(l,b,WEST,-1,NEAR_N,EAST,-1,1,s(Blocks.GRASS_BLOCK));
        // The flood over the drowned fields, everywhere between the near land and the river.
        box(l,b,WEST,LOW+1,RIVER_S+1,EAST,FLOOD_FLOOR-1,NEAR_N-1,s(Blocks.DIRT));box(l,b,WEST,FLOOD_FLOOR,RIVER_S+1,EAST,FLOOD_FLOOR,NEAR_N-1,s(Blocks.MUD));
        box(l,b,WEST,FLOOD_FLOOR+1,RIVER_S+1,EAST,-1,NEAR_N-1,water());
        // The river, deep and wide, and its far bank.
        box(l,b,WEST,LOW+1,RIVER_N,EAST,RIVER_FLOOR,RIVER_S,s(Blocks.GRAVEL));box(l,b,WEST,RIVER_FLOOR+1,RIVER_N,EAST,-1,RIVER_S,water());
        box(l,b,WEST,LOW+1,NORTH,EAST,-2,FAR_S,s(Blocks.DIRT));box(l,b,WEST,-1,NORTH,EAST,-1,FAR_S-1,s(Blocks.GRASS_BLOCK));
        box(l,b,WEST,RIVER_FLOOR,FAR_S,EAST,-1,FAR_S,s(Blocks.STONE_BRICKS));
        var rnd=new Random(3730);
        // Drowned fence lines across the fields, posts standing out of the water, and a few dead trees.
        for(int z=-16;z>=-60;z-=11)for(int x=WEST+2;x<=EAST-2;x+=3){if(x>=MOUND_W-1&&x<=MOUND_E+1&&z<=MOUND_S+1)continue;if(x>=-3&&x<=3||x>=LEVEE_W-1&&x<=LEVEE_E+2)continue;if(rnd.nextInt(4)==0)continue;
            for(int y=FLOOD_FLOOR+1;y<=0;y++)set(l,b,x,y,z,fence(Blocks.SPRUCE_FENCE,y<0));}
        for(int[] t:new int[][]{{-14,-22},{-9,-41},{-19,-55},{58,-30},{-4,-60}}){int x=t[0],z=t[1];
            for(int y=FLOOD_FLOOR+1;y<=3+rnd.nextInt(3);y++)set(l,b,x,y,z,s(Blocks.STRIPPED_OAK_LOG));
            set(l,b,x+1,2,z,fence(Blocks.OAK_FENCE,false));set(l,b,x,3,z-1,fence(Blocks.OAK_FENCE,false));}
        // A half-drowned hay bale or two, and reeds at the water's edge along the near land.
        for(int[] h:new int[][]{{-12,-30},{-17,-33},{52,-20}})set(l,b,h[0],FLOOD_FLOOR+1,h[1],s(Blocks.HAY_BLOCK));
        for(int x=WEST;x<=EAST;x+=2)if(rnd.nextInt(3)==0&&Math.abs(x)>3)set(l,b,x,0,NEAR_N,s(Blocks.SHORT_GRASS));
        // Banks at the west and east edges hold the water in, with trees along them.
        for(int x:new int[]{WEST,WEST+1,EAST-1,EAST}){box(l,b,x,LOW+1,NORTH,x,-2,1,s(Blocks.DIRT));box(l,b,x,-1,NORTH,x,-1,1,s(Blocks.GRASS_BLOCK));}
        for(int z=-8;z>NORTH+4;z-=9){tree(l,b,WEST,z,4+rnd.nextInt(3));tree(l,b,EAST,z-4,4+rnd.nextInt(3));}
    }
    /** The farm road from the House's door, dropping into the water, a barrier across it at the edge. */
    static void road(ServerLevel l,BlockPos b){
        // The shed's back wall, the House's door in it.
        for(int x=-8;x<=8;x++){if(Math.abs(x)<=3)continue;for(int y=0;y<=3;y++)set(l,b,x,y,1,s(Blocks.SPRUCE_PLANKS));set(l,b,x,4,1,s(Blocks.SPRUCE_SLAB));}
        for(int z=0;z>=NEAR_N;z--)for(int x=-1;x<=1;x++)set(l,b,x,-1,z,s(Blocks.DIRT_PATH));
        // Under the water the road goes on, a little higher than the fields either side, until it is gone.
        for(int z=NEAR_N-1;z>=-30;z--)for(int x=-1;x<=1;x++){set(l,b,x,FLOOD_FLOOR+1,z,s(Blocks.DIRT));set(l,b,x,FLOOD_FLOOR+2,z,s(z>-24?Blocks.GRAVEL:Blocks.MUD));}
        // The road's fences, standing out of the water where it goes under.
        for(int z=-3;z>=-27;z-=3)for(int x:new int[]{-3,3}){int bottom=z>NEAR_N?0:FLOOD_FLOOR+1;for(int y=bottom;y<=0;y++)set(l,b,x,y,z,fence(Blocks.SPRUCE_FENCE,y<0));}
        // A sawhorse across the road at the water, with its sign.
        for(int x=-1;x<=1;x++){set(l,b,x,0,NEAR_N,fence(Blocks.SPRUCE_FENCE,false));set(l,b,x,1,NEAR_N,s(Blocks.SPRUCE_SLAB));}
        set(l,b,2,0,NEAR_N,s(Blocks.SPRUCE_FENCE));
        standingSign(l,b.offset(0,0,NEAR_N+1),new String[]{"","ROAD","FLOODED",""});
    }
    /** The levee beside the road, up to the station, the power line along its east side. */
    static void levee(ServerLevel l,BlockPos b){
        for(int z=NEAR_N;z>=MOUND_S;z--)for(int x=LEVEE_W;x<=LEVEE_E;x++){box(l,b,x,FLOOD_FLOOR,z,x,FLOOD_FLOOR,z,s(Blocks.DIRT));for(int y=FLOOD_FLOOR+1;y<=-2;y++)set(l,b,x,y,z,s(Blocks.DIRT));set(l,b,x,-1,z,s(x==5||x==6?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK));}
        // Where it turns onto the mound by the porch.
        for(int z=-32;z>=MOUND_S;z--)for(int x=LEVEE_E+1;x<MOUND_W;x++){for(int y=FLOOD_FLOOR+1;y<=-2;y++)set(l,b,x,y,z,s(Blocks.DIRT));set(l,b,x,-1,z,s(Blocks.COARSE_DIRT));}
    }
    /** The station's mound: an apron of concrete on stone, its edges walled, its river wall dropping to the riverbed. */
    static void mound(ServerLevel l,BlockPos b){
        box(l,b,MOUND_W,RIVER_FLOOR,MOUND_N,MOUND_E,-2,MOUND_S,s(Blocks.STONE));
        box(l,b,MOUND_W,-1,MOUND_N,MOUND_E,-1,MOUND_S,s(Blocks.SMOOTH_STONE));
        for(int z=MOUND_S;z>=MOUND_N;z--){set(l,b,MOUND_W,-1,z,s(Blocks.STONE_BRICKS));set(l,b,MOUND_E,-1,z,s(Blocks.STONE_BRICKS));}
        for(int x=MOUND_W;x<=MOUND_E;x++){set(l,b,x,-1,MOUND_S,s(Blocks.STONE_BRICKS));box(l,b,x,RIVER_FLOOR,MOUND_N,x,-1,MOUND_N,s(Blocks.STONE_BRICKS));}
        // Bollards along the river wall; a rain barrel; sandbags along the east edge.
        for(int x=12;x<=54;x+=6){if(x>=PUMP_W&&x<=PUMP_E||x>=28&&x<=CABLE_E)continue;set(l,b,x,0,MOUND_N+1,s(Blocks.ANDESITE_WALL));}
        set(l,b,22,0,-48,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));set(l,b,23,0,-48,s(Blocks.WATER_CAULDRON));
        for(int z=-40;z>=-46;z--)set(l,b,MOUND_E-1,0,z,s(Blocks.MUD_BRICK_SLAB));
    }
    /** The keeper's office and its porch, where the roll is taken; the slate, the plate and the stove inside. */
    static void office(ServerLevel l,BlockPos b){
        var wall=s(Blocks.SPRUCE_PLANKS);var post=s(Blocks.STRIPPED_SPRUCE_LOG);
        box(l,b,OFFICE_W,-1,OFFICE_N,OFFICE_E,-1,OFFICE_S,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=3;y++){
            for(int x=OFFICE_W;x<=OFFICE_E;x++){set(l,b,x,y,OFFICE_S,wall);set(l,b,x,y,OFFICE_N,wall);}
            for(int z=OFFICE_S;z>=OFFICE_N;z--){set(l,b,OFFICE_W,y,z,wall);set(l,b,OFFICE_E,y,z,wall);}
            for(int[] c:new int[][]{{OFFICE_W,OFFICE_S},{OFFICE_E,OFFICE_S},{OFFICE_W,OFFICE_N},{OFFICE_E,OFFICE_N}})set(l,b,c[0],y,c[1],post);
        }
        box(l,b,OFFICE_W,4,OFFICE_N,OFFICE_E,4,OFFICE_S,s(Blocks.SPRUCE_PLANKS));
        // A gable roof east to west, its eaves over the porch and the back.
        for(int i=0;i<=5;i++){int y=4+i;int zs=OFFICE_S+1-i,zn=OFFICE_N-1+i;if(zs<zn)break;
            for(int x=OFFICE_W-1;x<=OFFICE_E+1;x++){
                if(zs==zn||i==4){set(l,b,x,y,zs,s(Blocks.DARK_OAK_PLANKS));if(zs!=zn)for(int z=zs;z>=zn;z--)set(l,b,x,y,z,s(Blocks.DARK_OAK_PLANKS));}
                else{set(l,b,x,y,zs,s(Blocks.DARK_OAK_STAIRS).setValue(StairBlock.FACING,Direction.NORTH));set(l,b,x,y,zn,s(Blocks.DARK_OAK_STAIRS).setValue(StairBlock.FACING,Direction.SOUTH));}
            }
            if(i>=1)for(int z=zs-1;z>zn;z--){set(l,b,OFFICE_W,y,z,wall);set(l,b,OFFICE_E,y,z,wall);}
            if(i==4)break;
        }
        NovelRooms.door(l,b.offset(OFFICE_DOOR),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        for(int z:new int[]{-41,-42,-43}){set(l,b,OFFICE_W,1,z,s(Blocks.GLASS_PANE));set(l,b,OFFICE_E,1,z,s(Blocks.GLASS_PANE));}
        set(l,b,12,1,OFFICE_S,s(Blocks.GLASS_PANE));set(l,b,18,1,OFFICE_S,s(Blocks.GLASS_PANE));
        // Inside: the slate and the plate on the north wall, the desk, the stove, the lamp on its hook, a bench, a shelf of binders.
        set(l,b,SLATE,fit(Kind.SLATE,Direction.SOUTH));
        set(l,b,PLATE_HOME,fit(Kind.PLATE,Direction.SOUTH));
        set(l,b,18,0,-44,fit(Kind.TABLE,Direction.NORTH));set(l,b,19,0,-44,fit(Kind.TABLE,Direction.NORTH));
        set(l,b,18,0,-43,s(Blocks.SPRUCE_STAIRS).setValue(StairBlock.FACING,Direction.SOUTH));
        set(l,b,STOVE,fit(Kind.STOVE,Direction.WEST));for(int y=1;y<=3;y++)set(l,b,STOVE.getX(),y,STOVE.getZ(),s(Blocks.IRON_BARS));
        set(l,b,LAMP_HOOK,fit(Kind.HOOK,Direction.EAST,1));
        set(l,b,11,0,-44,s(Blocks.SPRUCE_SLAB));set(l,b,11,0,-45,s(Blocks.CHISELED_BOOKSHELF).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST));
        set(l,b,12,0,-39,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));
        set(l,b,15,3,-42,s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        // The porch: its deck, posts and roof, the table with Vera's clipboard, a bench against the wall, a rail along its east half.
        box(l,b,OFFICE_W,-1,OFFICE_S+1,OFFICE_E,-1,MOUND_S-1,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=2;y++){set(l,b,OFFICE_W,y,MOUND_S-1,post);set(l,b,OFFICE_E,y,MOUND_S-1,post);}
        box(l,b,OFFICE_W,3,OFFICE_S+1,OFFICE_E,3,MOUND_S-1,s(Blocks.SPRUCE_SLAB).setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,PORCH_TABLE,fit(Kind.TABLE,Direction.NORTH));set(l,b,CLIPBOARD,fit(Kind.CLIPBOARD,Direction.SOUTH));
        for(int x=17;x<=18;x++)set(l,b,x,0,-37,s(Blocks.SPRUCE_STAIRS).setValue(StairBlock.FACING,Direction.NORTH));
        for(int x=16;x<=OFFICE_E-1;x++)set(l,b,x,0,MOUND_S-1,s(Blocks.SPRUCE_FENCE));
        set(l,b,15,2,MOUND_S-1,s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        NovelRooms.sign(l,b.offset(14,2,-37),Direction.SOUTH,new String[]{"ROOK FERRY","FLOOD STATION","","KEEPER"});
    }
    /** The pump room: the gauge room above with its gauge, brake and landing; the intake below with its pipe, screw, valves and pump. */
    static void pumpRoom(ServerLevel l,BlockPos b){
        var base=s(Blocks.STONE_BRICKS);var upper=s(Blocks.POLISHED_ANDESITE);
        for(int y=-8;y<=4;y++){var w=y<=0?base:upper;
            for(int x=PUMP_W;x<=PUMP_E;x++){set(l,b,x,y,PUMP_S,w);set(l,b,x,y,PUMP_N,w);}
            for(int z=PUMP_S;z>=PUMP_N;z--){set(l,b,PUMP_W,y,z,w);set(l,b,PUMP_E,y,z,w);}}
        box(l,b,PUMP_W,5,PUMP_N,PUMP_E,5,PUMP_S,s(Blocks.STONE_BRICK_SLAB));
        box(l,b,PUMP_W+1,LOWER-1,PUMP_N+1,PUMP_E-1,LOWER-1,PUMP_S-1,s(Blocks.STONE_BRICKS));
        box(l,b,PUMP_W+1,LOWER,PUMP_N+1,PUMP_E-1,-2,PUMP_S-1,s(Blocks.AIR));
        box(l,b,PUMP_W+1,-1,PUMP_N+1,PUMP_E-1,-1,PUMP_S-1,upper);
        box(l,b,PUMP_W+1,0,PUMP_N+1,PUMP_E-1,4,PUMP_S-1,s(Blocks.AIR));
        NovelRooms.door(l,b.offset(PUMP_DOOR),Direction.NORTH,Blocks.IRON_DOOR,true);
        for(int x:new int[]{14,22})for(int y=1;y<=2;y++)set(l,b,x,y,PUMP_S,s(Blocks.GLASS_PANE));
        for(int z:new int[]{-56,-60})set(l,b,PUMP_W,2,z,s(Blocks.GLASS_PANE));
        // The stair down along the west wall, its well railed off above.
        for(int i=0;i<=5;i++)set(l,b,PUMP_W+1,-1-i,-54-i,s(Blocks.STONE_BRICK_STAIRS).setValue(StairBlock.FACING,Direction.SOUTH));
        for(int z=-55;z>=-58;z--)set(l,b,PUMP_W+1,-1,z,s(Blocks.AIR));
        for(int z=-54;z>=-58;z--)set(l,b,PUMP_W+2,0,z,s(Blocks.IRON_BARS));set(l,b,PUMP_W+1,0,-59,s(Blocks.IRON_BARS));
        // The inspection landing: an opening in the floor over the intake, railed round.
        box(l,b,LANDING_W,-1,LANDING_N,LANDING_E,-1,LANDING_S,s(Blocks.AIR));
        for(int x=LANDING_W-1;x<=LANDING_E+1;x++){set(l,b,x,0,LANDING_S+1,s(Blocks.IRON_BARS));set(l,b,x,0,LANDING_N-1,s(Blocks.IRON_BARS));}
        for(int z=LANDING_S;z>=LANDING_N;z--){set(l,b,LANDING_W-1,0,z,s(Blocks.IRON_BARS));set(l,b,LANDING_E+1,0,z,s(Blocks.IRON_BARS));}
        // The gauge room: the gauge, the brake beside the landing, the speaking pipe's upper mouth, a bracket for the plate.
        set(l,b,GAUGE,fit(Kind.GAUGE,Direction.WEST));set(l,b,BRAKE,fit(Kind.BRAKE,Direction.WEST));set(l,b,PIPE_A_UP,fit(Kind.PIPE,Direction.WEST));
        set(l,b,PUMP_BRACKET,fit(Kind.BRACKET,Direction.EAST));
        for(int z:new int[]{-56,-60,-64})set(l,b,18,4,z,s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        set(l,b,22,0,-54,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));set(l,b,23,0,-54,s(Blocks.SMITHING_TABLE));
        // The intake: the pipe in from the west wall, the bleed screw, the three valves, the pump; the broken grate beside the screw.
        set(l,b,PUMP_W,LOWER,-64,s(Blocks.IRON_BARS));
        for(int x=13;x<=15;x++)set(l,b,x,LOWER,-64,fit(Kind.RUN,Direction.SOUTH));
        set(l,b,BLEED,fit(Kind.BLEED,Direction.SOUTH));
        for(int i=0;i<3;i++)set(l,b,VALVES.get(i),fit(Kind.VALVE,Direction.SOUTH,i));
        set(l,b,PUMP,fit(Kind.PUMP,Direction.SOUTH));
        set(l,b,GRATE,fit(Kind.GRATE,Direction.NORTH,1));for(int y=-9;y<=-8;y++)set(l,b,GRATE.getX(),y,GRATE.getZ(),water());
        set(l,b,PIPE_A_DOWN,fit(Kind.PIPE,Direction.SOUTH));
        for(int[] c:new int[][]{{15,-56},{21,-56},{21,-60},{15,-60}})set(l,b,c[0],-2,c[1],s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        set(l,b,22,LOWER,-54,s(Blocks.BARREL).setValue(BarrelBlock.FACING,Direction.UP));
        // The way into the branches: a grate in the east wall, shut, the pit beyond it.
        for(int y=LOWER;y<=LOWER+1;y++)set(l,b,PUMP_E,y,-60,s(Blocks.IRON_BARS));
    }
    /** The sluice pit beside the ramp: a ladder down to the wheel, the pin on its deck above, a speaking pipe between them. */
    static void pit(ServerLevel l,BlockPos b){
        box(l,b,25,-8,-61,26,-8,-59,s(Blocks.STONE_BRICKS));
        box(l,b,25,-7,-61,26,-1,-59,s(Blocks.AIR));
        for(int y=-8;y<=-1;y++){for(int z=-62;z<=-58;z++)set(l,b,27,y,z,s(Blocks.STONE_BRICKS));for(int x=25;x<=26;x++){set(l,b,x,y,-58,s(Blocks.STONE_BRICKS));set(l,b,x,y,-62,s(Blocks.STONE_BRICKS));}}
        for(int y=-7;y<=-1;y++)set(l,b,25,y,-59,s(Blocks.LADDER).setValue(LadderBlock.FACING,Direction.NORTH));
        for(int x=25;x<=26;x++)set(l,b,x,-1,-61,s(Blocks.IRON_BLOCK));
        set(l,b,WHEEL,fit(Kind.WHEEL,Direction.SOUTH));set(l,b,PIN,fit(Kind.PIN,Direction.SOUTH));
        set(l,b,PIPE_B_DOWN,fit(Kind.PIPE,Direction.EAST));set(l,b,PIPE_B_UP,fit(Kind.PIPE,Direction.EAST));
        for(int z=-60;z<=-59;z++)set(l,b,27,0,z,s(Blocks.IRON_BARS));set(l,b,26,0,-58,s(Blocks.IRON_BARS));
        set(l,b,26,-2,-59,s(Blocks.AIR));
    }
    /** The service branches under the apron, dry, their grates shut: from the pit east under the ramp and the spillway to the cable house. */
    static void branches(ServerLevel l,BlockPos b){
        for(int y=-7;y<=-6;y++){set(l,b,27,y,-60,s(Blocks.IRON_BARS));box(l,b,28,y,-60,42,y,-60,s(Blocks.AIR));set(l,b,43,y,-60,s(Blocks.IRON_BARS));}
        box(l,b,28,-8,-60,42,-8,-60,s(Blocks.STONE_BRICKS));
        for(int x=30;x<=42;x+=4)set(l,b,x,-5,-60,s(Blocks.STONE_BRICKS));
    }
    /** The ramp head and the landing where the ferry lies: the bell, the loose line, the depth post, the cut in the river wall and its sill. */
    static void landing(ServerLevel l,BlockPos b){
        box(l,b,28,-1,-63,37,-1,-56,s(Blocks.SMOOTH_STONE));
        for(int x=29;x<=37;x++)box(l,b,x,RIVER_FLOOR,-63,x,-1,-63,s(Blocks.STONE_BRICKS));
        // The cut: the sill under a block of water, the river beyond it.
        for(int x:new int[]{29,37})box(l,b,x,RIVER_FLOOR,MOUND_N,x,-1,-64,s(Blocks.STONE_BRICKS));
        box(l,b,FERRY_W,RIVER_FLOOR,-65,FERRY_E,-3,-64,s(Blocks.STONE));box(l,b,FERRY_W,-2,-65,FERRY_E,-1,-64,water());
        box(l,b,FERRY_W,RIVER_FLOOR+1,MOUND_N,FERRY_E,-1,MOUND_N,water());
        set(l,b,BELL,s(Blocks.BELL).setValue(BellBlock.FACING,Direction.SOUTH).setValue(BellBlock.ATTACHMENT,BellAttachType.FLOOR));
        set(l,b,30,0,-63,s(Blocks.ANDESITE_WALL));set(l,b,36,0,-63,s(Blocks.ANDESITE_WALL));
        set(l,b,DEPTH,fit(Kind.DEPTH,Direction.SOUTH));
        NovelRooms.sign(l,b.offset(PUMP_E+1,2,-56),Direction.EAST,new String[]{"","RAMP","KEEP CLEAR",""});
    }
    /** The ferry, a flat cable ferry low against its sill, its wheelhouse with the nameplate, and its line to the cable. */
    static void ferry(ServerLevel l,BlockPos b){
        var deck=s(Blocks.DARK_OAK_PLANKS);
        box(l,b,FERRY_W,-1,FERRY_N,FERRY_E,-1,FERRY_S,deck);
        for(int z=FERRY_S;z>=FERRY_N;z--){set(l,b,FERRY_W,-2,z,deck);set(l,b,FERRY_E,-2,z,deck);}
        for(int x=FERRY_W;x<=FERRY_E;x++)set(l,b,x,-2,FERRY_N,deck);
        for(int z=FERRY_S-2;z>=FERRY_N+1;z--){set(l,b,FERRY_W,0,z,s(Blocks.SPRUCE_FENCE));set(l,b,FERRY_E,0,z,s(Blocks.SPRUCE_FENCE));}
        for(int x=FERRY_W;x<=FERRY_E;x++)set(l,b,x,0,FERRY_N,s(Blocks.SPRUCE_FENCE));
        // The wheelhouse, four wide, its nameplate across its front.
        for(int y=0;y<=2;y++)for(int x=31;x<=34;x++)for(int z=-70;z>=-72;z--)if(x==31||x==34||z==-70||z==-72)set(l,b,x,y,z,deck);
        set(l,b,32,1,-70,s(Blocks.GLASS_PANE));set(l,b,33,1,-70,s(Blocks.GLASS_PANE));
        set(l,b,34,0,-71,s(Blocks.AIR));set(l,b,34,1,-71,s(Blocks.AIR));
        box(l,b,30,3,-73,35,3,-69,s(Blocks.DARK_OAK_SLAB));
        set(l,b,32,2,-69,fit(Kind.NAMEPLATE,Direction.SOUTH,0));set(l,b,33,2,-69,fit(Kind.NAMEPLATE,Direction.SOUTH,1));
        set(l,b,33,0,-71,s(Blocks.LECTERN));
        // The traveller line from the ferry's rail out to the cable.
        for(int x=FERRY_E+1;x<CABLE_X;x++)set(l,b,x,0,-70,chain(Direction.Axis.X));
    }
    /** The narrow spillway, full to the brim, from its culvert down to the river; the footbridge across it. */
    static void spillway(ServerLevel l,BlockPos b){
        box(l,b,SPILL_W-1,-5,MOUND_N,SPILL_E+1,-1,SPILL_S,s(Blocks.STONE_BRICKS));
        box(l,b,SPILL_W,-5,MOUND_N,SPILL_E,-5,SPILL_S,s(Blocks.STONE_BRICKS));
        box(l,b,SPILL_W,-4,MOUND_N,SPILL_E,-1,SPILL_S,water());
        box(l,b,SPILL_W,RIVER_FLOOR+1,MOUND_N,SPILL_E,-1,MOUND_N,water());
        box(l,b,SPILL_W-1,-5,SPILL_S+1,SPILL_E+1,0,SPILL_S+1,s(Blocks.STONE_BRICKS));
        for(int x=SPILL_W;x<=SPILL_E;x++)for(int y=-3;y<=-2;y++)set(l,b,x,y,SPILL_S+1,s(Blocks.IRON_BARS));
        for(int x=SPILL_W;x<=SPILL_E;x++){set(l,b,x,-1,BRIDGE_Z,s(Blocks.SPRUCE_PLANKS));set(l,b,x,0,BRIDGE_Z+1,s(Blocks.SPRUCE_FENCE));set(l,b,x,0,BRIDGE_Z-1,s(Blocks.SPRUCE_FENCE));}
        for(int z=SPILL_S;z>MOUND_N;z--){if(z==BRIDGE_Z)continue;set(l,b,SPILL_W-1,0,z,s(Blocks.SPRUCE_FENCE));set(l,b,SPILL_E+1,0,z,s(Blocks.SPRUCE_FENCE));}
    }
    /** The cable house across the spillway: its drum and brake, the rollers, the fairlead, the hatch over the flooded passage, the dry ledge. */
    static void cableHouse(ServerLevel l,BlockPos b){
        var wall=s(Blocks.SPRUCE_PLANKS);
        box(l,b,CABLE_W+1,-1,CABLE_N+1,CABLE_E-1,-1,CABLE_S-1,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=4;y++){
            for(int x=CABLE_W;x<=CABLE_E;x++){set(l,b,x,y,CABLE_S,wall);set(l,b,x,y,CABLE_N,y==0?s(Blocks.STONE_BRICKS):wall);}
            for(int z=CABLE_S;z>=CABLE_N;z--){set(l,b,CABLE_W,y,z,wall);set(l,b,CABLE_E,y,z,wall);}
            for(int[] c:new int[][]{{CABLE_W,CABLE_S},{CABLE_E,CABLE_S},{CABLE_W,CABLE_N},{CABLE_E,CABLE_N}})set(l,b,c[0],y,c[1],s(Blocks.STRIPPED_SPRUCE_LOG));
        }
        box(l,b,CABLE_W,5,CABLE_N,CABLE_E,5,CABLE_S,s(Blocks.STONE_BRICK_SLAB));
        NovelRooms.door(l,b.offset(CABLE_DOOR),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        for(int x:new int[]{44,50})set(l,b,x,2,CABLE_S,s(Blocks.GLASS_PANE));for(int z:new int[]{-58,-62})set(l,b,CABLE_E,2,z,s(Blocks.GLASS_PANE));
        // The flooded inspection passage under the rollers, and the hatch down into it.
        box(l,b,44,-8,-63,50,-8,-59,s(Blocks.STONE_BRICKS));box(l,b,44,-7,-63,50,-2,-59,water());
        set(l,b,HATCH,fit(Kind.HATCH,Direction.NORTH));
        // The drum and the brake, the cable over the rollers to the fairlead, one roller off its cradle.
        set(l,b,DRUM,fit(Kind.DRUM,Direction.NORTH));set(l,b,CABLE_BRAKE,fit(Kind.BRAKE,Direction.WEST));
        for(var r:ROLLERS)set(l,b,r,fit(Kind.ROLLER,Direction.NORTH,r.equals(LOOSE_ROLLER)?1:0));
        for(int z=-57;z>=-65;z-=2)set(l,b,CABLE_X,0,z,chain(Direction.Axis.Z));
        set(l,b,FAIRLEAD,fit(Kind.FAIRLEAD,Direction.NORTH));
        // The dry ledge along the west wall, out of the drum's way.
        for(int z=-56;z>=-64;z--)set(l,b,LEDGE_X,0,z,s(Blocks.STONE_BRICK_SLAB));
        set(l,b,CABLE_BRACKET,fit(Kind.BRACKET,Direction.WEST));
        for(int z:new int[]{-58,-63})set(l,b,46,4,z,s(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        // The cable out over the river to its anchor on the far bank.
        for(int z=CABLE_N-1;z>ANCHOR.getZ();z--)set(l,b,CABLE_X,0,z,chain(Direction.Axis.Z));
    }
    /** The far bank: the landing, the lamp that burns there, the cable's anchor, the shelter with the way on, a line of trees. */
    static void farBank(ServerLevel l,BlockPos b){
        box(l,b,FERRY_W,-1,-96,FERRY_E,-1,FAR_S-1,s(Blocks.SPRUCE_PLANKS));
        for(int y=0;y<=2;y++)set(l,b,FAR_LAMP.getX(),y,FAR_LAMP.getZ(),s(Blocks.SPRUCE_FENCE));
        set(l,b,FAR_LAMP,s(Blocks.LANTERN));
        set(l,b,ANCHOR,s(Blocks.IRON_BLOCK));set(l,b,ANCHOR.above(),s(Blocks.STONE_BRICK_WALL));
        var wall=s(Blocks.SPRUCE_PLANKS);
        for(int y=0;y<=3;y++)for(int x=40;x<=46;x++)for(int z=-98;z>=-104;z--)if(x==40||x==46||z==-98||z==-104)set(l,b,x,y,z,wall);
        box(l,b,39,4,-105,47,4,-97,s(Blocks.SPRUCE_SLAB));
        for(int y=0;y<=1;y++)set(l,b,ONWARD.getX(),y,ONWARD.getZ(),s(Blocks.AIR));
        var rnd=new Random(3740);
        for(int x=WEST+3;x<=EAST-3;x+=7){int z=-106+rnd.nextInt(4);if(x>=36&&x<=48)continue;tree(l,b,x,z,4+rnd.nextInt(3));}
    }
    /** A sign on its own post, its words facing south. */
    static void standingSign(ServerLevel l,BlockPos at,String[] lines){
        BuildBlocks.set(l,at,Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION,0),F);
        BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign){var text=sign.getFrontText();for(int i=0;i<4;i++)text=text.setMessage(i,net.minecraft.network.chat.Component.literal(lines[i]));sign.setText(text,true);}});
    }
    static void tree(ServerLevel l,BlockPos b,int x,int z,int h){
        for(int y=0;y<h;y++)set(l,b,x,y,z,s(Blocks.SPRUCE_LOG));
        var leaves=s(Blocks.SPRUCE_LEAVES).setValue(LeavesBlock.PERSISTENT,true);
        for(int y=h-3;y<=h;y++){int r=y>=h-1?1:2;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(dx==0&&dz==0&&y<h)continue;if(Math.abs(dx)+Math.abs(dz)>r+1)continue;set(l,b,x+dx,y,z+dz,leaves);}}
    }
    /** The power line from beside the House's door up the levee, along the mound's edge and over the apron to the pump room. */
    static void powerLine(ServerLevel l,BlockPos b){
        var pole=s(Blocks.STRIPPED_DARK_OAK_LOG);
        for(int z=-4;z>=-34;z-=6){int bottom=z>NEAR_N?0:FLOOD_FLOOR+1;for(int y=bottom;y<=5;y++)set(l,b,LEVEE_E,y,z,pole);set(l,b,LEVEE_E-1,5,z,s(Blocks.DARK_OAK_FENCE));set(l,b,LEVEE_E+1,5,z,s(Blocks.DARK_OAK_FENCE));}
        for(int z=-5;z>=-33;z--){if(Math.floorMod(-4-z,6)==0)continue;set(l,b,LEVEE_E,5,z,chain(Direction.Axis.Z));}
        for(int x=LEVEE_E+2;x<23;x++)set(l,b,x,5,-34,chain(Direction.Axis.X));
        for(int y=FLOOD_FLOOR+1;y<=5;y++)set(l,b,23,y,-34,pole);
        for(int z=-35;z>PUMP_S;z--)set(l,b,23,5,z,chain(Direction.Axis.Z));
        set(l,b,23,6,PUMP_S,s(Blocks.LIGHTNING_ROD));
    }
    /** Everything the crew can move or change, as it stands in the morning before anyone comes. */
    public static void living(ServerLevel l,BlockPos b){
        set(l,b,CLIPBOARD,fit(Kind.CLIPBOARD,Direction.SOUTH));
        set(l,b,PLATE_HOME,fit(Kind.PLATE,Direction.SOUTH));
        set(l,b,PUMP_BRACKET,fit(Kind.BRACKET,Direction.EAST));set(l,b,CABLE_BRACKET,fit(Kind.BRACKET,Direction.WEST));
        set(l,b,LAMP_HOOK,fit(Kind.HOOK,Direction.EAST,1));
        set(l,b,GAUGE,fit(Kind.GAUGE,Direction.WEST));set(l,b,BRAKE,fit(Kind.BRAKE,Direction.WEST));
        set(l,b,BLEED,fit(Kind.BLEED,Direction.SOUTH));set(l,b,PUMP,fit(Kind.PUMP,Direction.SOUTH));
        set(l,b,WHEEL,fit(Kind.WHEEL,Direction.SOUTH));set(l,b,PIN,fit(Kind.PIN,Direction.SOUTH));
        set(l,b,RAMP_COIL,fit(Kind.COIL,Direction.NORTH));set(l,b,PORCH_COIL,s(Blocks.AIR));set(l,b,STAGED_COIL,s(Blocks.AIR));
        set(l,b,LOOSE_ROLLER,fit(Kind.ROLLER,Direction.NORTH,1));set(l,b,FAIRLEAD,fit(Kind.FAIRLEAD,Direction.NORTH));
        set(l,b,HATCH,fit(Kind.HATCH,Direction.NORTH));
    }
}
