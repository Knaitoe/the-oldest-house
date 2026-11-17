package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md): a gas station and grocery on a two-lane road in the wheat, a late summer afternoon, under the
 * outside sky. Every position is relative to the base, the floor just inside the House's door; north is -z; people stand at y 0 on ground
 * at y -1.
 *
 * <p>The House's door is the store's back door, so a reader comes in behind the counter: the register and the keeper's stool, shelves of
 * cartons and cans on the back wall with a radio on one, and a television on a bracket. Past the counter's flap are the coolers, two
 * rows of shelving, a rack of road maps and a coffee machine, and the plate windows on the forecourt. A door in the east wall goes into
 * the garage bay, where a car stands up on the lift. Out front, two pumps on an island under a canopy, a diesel pump with a rig beside it,
 * the station's sign on its pole, and the road. West of the store is the keeper's yard: his trailer on its blocks, a clothesline, and the
 * storm cellar's slanted doors over steep steps down to a low room of shelves, a cot, a crock and a lantern. Across the road, telephone
 * poles, a windbreak, and a farmhouse behind it with its drive. Everywhere else is wheat, flat into a haze, and out in it two fenced
 * squares of gravel, each with a flat concrete lid, a pole and a light, a dirt track to each.
 */
public final class FallowSite {
    private FallowSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.FALLOW;
    /** The scene's edges: wide, so that the haze closes in before them. */
    public static final int WEST=-56,EAST=56,NORTH=-136;
    /** The store: its walls (the south one is the House's, with the door at x 0) and its roof, and the counter's line. */
    public static final int STORE_W=-8,STORE_E=8,STORE_S=0,STORE_N=-12,CEILING=4,COUNTER_Z=-4;
    /** The run of shelving, front to back between the coolers and the aisle to the counter. */
    public static final int GONDOLA_X=-3,GONDOLA_S=-6,GONDOLA_N=-9;
    /** The garage bay, east of the store and sharing its wall, a storey and a half high; the opening in its front wall. */
    public static final int GARAGE_E=18,GARAGE_TOP=5,BAY_W=10,BAY_E=16;
    /** The forecourt's concrete, the pump island, the canopy's deck and its height. */
    public static final int APRON_W=-20,APRON_E=28,APRON_N=-27,ISLAND_Z=-19,CANOPY_W=-7,CANOPY_E=6,CANOPY_S=-15,CANOPY_N=-23,CANOPY_Y=5;
    /** The road: its south shoulder, its two lanes, the dashed line between them, its north shoulder; the poles along its far side. */
    public static final int SHOULDER_S=-28,ROAD_S=-29,LINE_Z=-31,ROAD_N=-34,SHOULDER_N=-35,POLES_Z=-36,POLE_TOP=6;
    /** The keeper's yard, west of the store. */
    public static final int YARD_W=-27,YARD_N=-19;
    /** The keeper's trailer on its blocks: its floor is a block up. */
    public static final int TRAILER_W=-24,TRAILER_E=-21,TRAILER_S=-3,TRAILER_N=-14;
    /** The storm cellar: the doors over the stair well, and the low room under the yard (floor at -4, people at -3, ceiling at -1). */
    public static final int CELLAR_W=-18,CELLAR_E=-9,CELLAR_S=-11,CELLAR_N=-18,CELLAR_FLOOR=-4,WELL_W=-14,WELL_E=-13,WELL_S=-8,WELL_N=-11;
    /** The cot's head and foot, the crock of water, the lantern on its hook. */
    public static final BlockPos COT_HEAD=new BlockPos(CELLAR_E-1,CELLAR_FLOOR+1,-15),CROCK=new BlockPos(CELLAR_W+1,CELLAR_FLOOR+1,CELLAR_N+1),
        LANTERN=new BlockPos(-13,-2,-14);
    /** The farmhouse across the road, behind its windbreak. */
    public static final int FARM_W=-34,FARM_E=-22,FARM_S=-52,FARM_N=-62,WINDBREAK_Z=-44,FARM_DRIVE_X=-28;
    /** The two pads in the wheat, each a fenced square thirteen across, its lid five across in the middle; their tracks from the road. */
    public static final BlockPos PAD_A=new BlockPos(-20,0,-90),PAD_B=new BlockPos(20,0,-108);
    public static final int PAD_HALF=6,LID_HALF=2;
    /** 0.4.674: how far each lid slides east off its seat, a column at a time; the shaft under the seat's west three columns, three
     * across and four deep, its floor at y -6; where something stands in it, its foot. */
    public static final int LID_SLIDE=3,SHAFT_TOP=-2,SHAFT_FLOOR=-6;
    public static Vec3 shaft(BlockPos pad){return new Vec3(pad.getX()-.5,SHAFT_FLOOR+1,pad.getZ()+.5);}
    /** The House's door at the back of the store; the store's front door; the door into the garage; the trailer's door. */
    public static final BlockPos FRONT_DOOR=new BlockPos(0,0,STORE_N),GARAGE_DOOR=new BlockPos(STORE_E,0,-5),TRAILER_DOOR=new BlockPos(TRAILER_E,1,-8);
    /** The first paper, the keeper's note by the register; where each reader's account will be, under the counter by the till. */
    public static final BlockPos NOTE=new BlockPos(-2,1,COUNTER_Z),ENDING=new BlockPos(-1,0,COUNTER_Z);
    /** The way on: the farmhouse's front door, facing the road. */
    public static final BlockPos ONWARD=new BlockPos(FARM_DRIVE_X,0,FARM_S);
    /** Where each of them stands: the keeper behind his register, the woman at the coffee machine, the girl on the step out front, her
     * man in the garage by the car, the driver at his rig. */
    public static final BlockPos KEEPER_AT=new BlockPos(-1,0,-2)/* on his stool */,WOMAN_AT=new BlockPos(5,0,-9),GIRL_AT=new BlockPos(3,0,-13),
        MAN_AT=new BlockPos(13,0,-10),DRIVER_AT=new BlockPos(12,0,-20);
    /** The keeper's stool behind the register, where he sits (ActorSeat). A reader who comes in by the House's door, behind the counter,
     * goes round him along the counter and out by its lifted flap at the east end. */
    public static final BlockPos STOOL=KEEPER_AT;
    /** The radio on the back shelf; the television on its bracket. */
    public static final BlockPos RADIO=new BlockPos(-4,1,-1),TV=new BlockPos(5,2,-1);
    /** The car on the lift: where the middle of its body is, a hand's height over a reader's head. */
    public static final Vec3 CAR_AT=new Vec3(13.5,2.0,-5.5);
    /** The rig: its front at x 14, lying west to east along z -21 and -22. */
    public static final int RIG_FRONT=14,RIG_Z=-21,TRAILER_FRONT=17,TRAILER_BACK=26;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inStore(Vec3 r){return r.x>STORE_W+.5&&r.x<STORE_E+.5&&r.z<STORE_S-.3&&r.z>STORE_N+.5&&r.y>-.5&&r.y<CEILING;}
    public static boolean behindCounter(Vec3 r){return inStore(r)&&r.z>COUNTER_Z+.5;}
    public static boolean inGarage(Vec3 r){return r.x>STORE_E+.5&&r.x<GARAGE_E+.5&&r.z<STORE_S-.3&&r.z>STORE_N+.5&&r.y>-.5&&r.y<GARAGE_TOP;}
    public static boolean inCellar(Vec3 r){return r.x>CELLAR_W+.5&&r.x<CELLAR_E+.5&&r.z<CELLAR_S+3.5&&r.z>CELLAR_N+.5&&r.y<-.5&&r.y>CELLAR_FLOOR;}
    public static boolean onForecourt(Vec3 r){return r.x>APRON_W&&r.x<APRON_E+1&&r.z<STORE_N+.5&&r.z>APRON_N;}
    /** Anywhere in the scene, from the road's far side to the wheat's edge. */
    public static boolean inScene(Vec3 r){return r.x>=WEST&&r.x<=EAST+1&&r.z>=NORTH&&r.z<=1&&r.y>=-6&&r.y<=16;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return FallowRegistry.material(name).defaultBlockState();}
    static BlockState fit(FallowBlock.Kind kind,Direction facing,int stage){return FallowBlock.of(kind,facing,stage);}
    static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(66872);
        box(l,b,WEST,-6,NORTH,EAST,16,0,AIR);
        box(l,b,WEST,-6,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        wheat(l,b,rnd);
        road(l,b);
        poles(l,b);
        farm(l,b,rnd);
        pads(l,b);
        forecourt(l,b);
        store(l,b);
        garage(l,b);
        yard(l,b);
        cellar(l,b);
        rig(l,b);
    }

    /** 0.4.680: the station built again for a fresh afternoon after the light has had it (FallowRuin.mend). Its parts are drawn from their
     * plans alone, so they come back as they were built: the forecourt and the rig on it, the store, the garage, the yard and the cellar.
     * The store's back wall is laid whole by its plan, so the House's opening through it is cut again, as the scene's builder cut it. */
    static void mend(ServerLevel l,BlockPos b){
        forecourt(l,b);
        store(l,b);
        garage(l,b);
        yard(l,b);
        cellar(l,b);
        rig(l,b);
        set(l,b,0,0,0,AIR);set(l,b,0,1,0,AIR);
    }

    /** The whole of the ground under wheat first, ripe and standing, a bowed head here and there; everything else is laid over it. */
    static void wheat(ServerLevel l,BlockPos b,Random rnd){
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=0;z++){
            set(l,b,x,-1,z,mat("soil"));
            set(l,b,x,0,z,fit(FallowBlock.Kind.WHEAT,Direction.from2DDataValue(rnd.nextInt(4)),rnd.nextInt(5)==0?1:0));
        }
    }
    private static void clearGround(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,BlockState ground){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++){set(l,b,x,-1,z,ground);set(l,b,x,0,z,AIR);}
    }

    /** The road from one edge to the other: gravel shoulders, two lanes of asphalt, a dashed line between them. */
    static void road(ServerLevel l,BlockPos b){
        clearGround(l,b,WEST,SHOULDER_S,EAST,SHOULDER_S,mat("shoulder"));
        clearGround(l,b,WEST,SHOULDER_N,EAST,SHOULDER_N,mat("shoulder"));
        clearGround(l,b,WEST,ROAD_S,EAST,ROAD_N,mat("asphalt"));
        for(int x=WEST;x<=EAST;x++)if(Math.floorMod(x,6)<3)set(l,b,x,-1,LINE_Z,mat("road_line"));
    }

    /** Telephone poles along the road's north side every sixteen blocks, their crossarms carrying two wires that sag between them. */
    static void poles(ServerLevel l,BlockPos b){
        for(int x=-48;x<=48;x+=16){
            for(int y=0;y<POLE_TOP;y++)set(l,b,x,y,POLES_Z,fit(FallowBlock.Kind.POLE,Direction.NORTH,0));
            set(l,b,x,POLE_TOP,POLES_Z,fit(FallowBlock.Kind.POLE,Direction.NORTH,1));
        }
        for(int x=WEST;x<=EAST;x++){
            if(Math.floorMod(x,16)==0&&x>=-48&&x<=48)continue;
            int d=Math.min(Math.floorMod(x,16),16-Math.floorMod(x,16));
            set(l,b,x,POLE_TOP,POLES_Z,fit(FallowBlock.Kind.POLE,Direction.NORTH,d<=2?2:d<=5?3:4));
        }
    }

    /** The farmhouse across the road: a windbreak of trees along its yard, a gravel drive, a porch on the road side, two storeys of
     * painted boards under a shingled gable with a brick chimney. Its front door is the way on (OnwardPlacements). */
    static void farm(ServerLevel l,BlockPos b,Random rnd){
        clearGround(l,b,-42,WINDBREAK_Z-1,-14,FARM_N-4,Blocks.GRASS_BLOCK.defaultBlockState());
        clearGround(l,b,FARM_DRIVE_X-1,SHOULDER_N-1,FARM_DRIVE_X+1,FARM_S-1,mat("gravel"));
        // The windbreak: a row of broad trees with a gap where the drive goes through.
        for(int x=-40;x<=-16;x+=3){
            if(Math.abs(x-FARM_DRIVE_X)<3)continue;
            int h=5+rnd.nextInt(2);
            for(int y=0;y<h;y++)set(l,b,x,y,WINDBREAK_Z,Blocks.OAK_LOG.defaultBlockState());
            var leaves=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=h-2;dy<=h+1;dy++){
                int r=Math.abs(dx)+Math.abs(dz)+(dy>h?1:0);
                if(r<=3&&!(dx==0&&dz==0&&dy<h)&&rnd.nextInt(7)!=0&&l.getBlockState(b.offset(x+dx,dy,WINDBREAK_Z+dz)).isAir())set(l,b,x+dx,dy,WINDBREAK_Z+dz,leaves);
            }
        }
        // The house: boards on a stone sill, an upper floor, a gable running east-west.
        box(l,b,FARM_W,-1,FARM_N,FARM_E,-1,FARM_S,mat("porch"));
        for(int x=FARM_W;x<=FARM_E;x++)for(int z=FARM_N;z<=FARM_S;z++){
            boolean wall=x==FARM_W||x==FARM_E||z==FARM_N||z==FARM_S;
            for(int y=0;y<=6;y++)set(l,b,x,y,z,wall?(y==0?mat("plinth"):mat("farm_siding")):AIR);
            if(!wall){set(l,b,x,3,z,mat("porch"));}
        }
        // Windows in both storeys, two to each side of the door and along the ends.
        for(int x:new int[]{FARM_W+2,FARM_W+4,FARM_E-4,FARM_E-2})for(int y:new int[]{1,4}){
            set(l,b,x,y,FARM_S,fit(FallowBlock.Kind.FARM,Direction.SOUTH,0));set(l,b,x,y,FARM_N,fit(FallowBlock.Kind.FARM,Direction.NORTH,0));
        }
        for(int z:new int[]{FARM_S-3,FARM_N+3})for(int y:new int[]{1,4}){
            set(l,b,FARM_W,y,z,fit(FallowBlock.Kind.FARM,Direction.WEST,0));set(l,b,FARM_E,y,z,fit(FallowBlock.Kind.FARM,Direction.EAST,0));
        }
        // The gable roof: shingle stairs stepping up from both eaves to a ridge, the gable ends filled with boards.
        var stairs=FallowRegistry.SHINGLE_STAIRS.get().defaultBlockState();
        int mid=(FARM_S+FARM_N)/2,half=(FARM_S-FARM_N)/2+1;
        for(int k=0;k<=half;k++){
            int y=7+k,zs=FARM_S+1-k,zn=FARM_N-1+k;
            if(zs<zn)break;
            for(int x=FARM_W-1;x<=FARM_E+1;x++){
                if(zs==zn){set(l,b,x,y,zs,mat("farm_shingle"));continue;}
                set(l,b,x,y,zs,stairs.setValue(StairBlock.FACING,Direction.NORTH));
                set(l,b,x,y,zn,stairs.setValue(StairBlock.FACING,Direction.SOUTH));
            }
            for(int z=zn+1;z<zs;z++){set(l,b,FARM_W,y,z,mat("farm_siding"));set(l,b,FARM_E,y,z,mat("farm_siding"));}
        }
        for(int y=7;y<=14;y++)set(l,b,FARM_E-3,y,mid,mat("brick"));
        // The porch along the front: a board floor, posts and a rail, a step down to the drive, and the mailbox by the road.
        for(int x=FARM_W;x<=FARM_E;x++){
            set(l,b,x,-1,FARM_S+1,mat("porch"));set(l,b,x,-1,FARM_S+2,mat("porch"));
            boolean post=x==FARM_W||x==FARM_E||x==FARM_DRIVE_X-2||x==FARM_DRIVE_X+2||x==-31||x==-25;
            if(post){set(l,b,x,0,FARM_S+2,fit(FallowBlock.Kind.FARM,Direction.SOUTH,1));set(l,b,x,1,FARM_S+2,fit(FallowBlock.Kind.FARM,Direction.SOUTH,1));set(l,b,x,2,FARM_S+2,fit(FallowBlock.Kind.FARM,Direction.SOUTH,1));}
            else if(Math.abs(x-FARM_DRIVE_X)>1)set(l,b,x,0,FARM_S+2,fit(FallowBlock.Kind.FARM,Direction.SOUTH,2));
            set(l,b,x,3,FARM_S+1,stairs.setValue(StairBlock.FACING,Direction.NORTH));set(l,b,x,3,FARM_S+2,stairs.setValue(StairBlock.FACING,Direction.NORTH));
        }
        // The farmhouse's front door at ONWARD is the way on; OnwardPlacements sets it in the wall.
        set(l,b,FARM_DRIVE_X+2,0,SHOULDER_N-1,fit(FallowBlock.Kind.FARM,Direction.SOUTH,3));
    }

    /** A door, its lower half at the cell and its upper above, facing out of the wall it is set in. */
    static void door(ServerLevel l,BlockPos b,BlockPos at,Direction out){
        var d=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,out).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    /** One pad: a square of gravel inside a chain-link fence with a gate on the track side, a light on a pole at one corner, a squat
     * hatch box, and in the middle a flat lid of concrete, its edges seamed where it will move. A dirt track runs from the road to the gate. */
    static void pad(ServerLevel l,BlockPos b,BlockPos c){
        int cx=c.getX(),cz=c.getZ(),h=PAD_HALF;
        // The track: two worn ruts with weeds between them, from the road's shoulder to the gate.
        for(int z=SHOULDER_N-1;z>cz+h;z--){set(l,b,cx,-1,z,mat("track"));set(l,b,cx,0,z,AIR);set(l,b,cx+1,-1,z,mat("track"));set(l,b,cx+1,0,z,AIR);}
        clearGround(l,b,cx-h,cz-h,cx+h,cz+h,mat("gravel"));
        for(int x=cx-h;x<=cx+h;x++)for(int z=cz-h;z<=cz+h;z++){
            boolean edgeX=x==cx-h||x==cx+h,edgeZ=z==cz-h||z==cz+h;
            if(!edgeX&&!edgeZ)continue;
            boolean corner=edgeX&&edgeZ,post=corner||(edgeX?Math.floorMod(z-cz,4)==0:Math.floorMod(x-cx,4)==0);
            Direction run=edgeZ?(z==cz+h?Direction.SOUTH:Direction.NORTH):(x==cx+h?Direction.EAST:Direction.WEST);
            if(z==cz+h&&(x==cx||x==cx+1)){set(l,b,x,0,z,fit(FallowBlock.Kind.PAD,Direction.SOUTH,2));continue;}
            set(l,b,x,0,z,fit(FallowBlock.Kind.PAD,run,post?1:0));
        }
        // 0.4.674: under the lid's seat, a shaft three across and four deep, walled and floored in concrete, empty.
        for(int x=cx-LID_HALF-1;x<=cx+1;x++)for(int z=cz-LID_HALF;z<=cz+LID_HALF;z++){
            boolean wall=x==cx-LID_HALF-1||x==cx+1||Math.abs(z-cz)==LID_HALF;
            set(l,b,x,SHAFT_FLOOR,z,mat("apron"));
            for(int y=SHAFT_FLOOR+1;y<=SHAFT_TOP;y++)set(l,b,x,y,z,wall?mat("apron"):AIR);
        }
        for(int x=cx-LID_HALF;x<=cx+LID_HALF+LID_SLIDE;x++)for(int z=cz-LID_HALF;z<=cz+LID_HALF;z++)set(l,b,x,-1,z,lidAt(c,x,z,0));
        for(int y=0;y<4;y++)set(l,b,cx-h+1,y,cz-h+1,fit(FallowBlock.Kind.PAD,Direction.SOUTH,3));
        set(l,b,cx-h+1,4,cz-h+1,fit(FallowBlock.Kind.PAD,Direction.SOUTH,4));
        // The hatch box west of the lid, clear of where the lid runs.
        set(l,b,cx-h+2,0,cz+3,fit(FallowBlock.Kind.PAD,Direction.EAST,7));
    }
    /**
     * 0.4.674: a pad's lid, slid this many columns east of its seat (0 to LID_SLIDE). The lid lies on the gravel's level, seamed round its
     * edge; the part of its seat it has uncovered is the shaft's open mouth, with a concrete rim at either end; the gravel it has not
     * yet reached is gravel. Nothing is set unless the whole of it is loaded.
     */
    public static boolean lid(ServerLevel l,BlockPos b,BlockPos c,int slid){
        int cx=c.getX(),cz=c.getZ();
        if(!l.isLoaded(b.offset(cx-LID_HALF,-1,cz-LID_HALF))||!l.isLoaded(b.offset(cx+LID_HALF+LID_SLIDE,-1,cz+LID_HALF)))return false;
        for(int x=cx-LID_HALF;x<=cx+LID_HALF+LID_SLIDE;x++)for(int z=cz-LID_HALF;z<=cz+LID_HALF;z++){
            var st=lidAt(c,x,z,slid);var at=b.offset(x,-1,z);if(!l.getBlockState(at).equals(st))l.setBlock(at,st,3);
        }
        return true;
    }
    /** What lies at a cell of a pad's lid and its run, at the gravel's level, with the lid slid so far. */
    static BlockState lidAt(BlockPos c,int x,int z,int slid){
        int cx=c.getX(),cz=c.getZ(),lx=x-slid;
        if(Math.abs(lx-cx)<=LID_HALF){
            boolean edge=Math.abs(lx-cx)==LID_HALF||Math.abs(z-cz)==LID_HALF;
            Direction out=Math.abs(z-cz)==LID_HALF?(z>cz?Direction.SOUTH:Direction.NORTH):(lx>cx?Direction.EAST:Direction.WEST);
            return fit(FallowBlock.Kind.PAD,edge?out:Direction.NORTH,edge?5:6);
        }
        if(x<cx-LID_HALF+slid)return Math.abs(z-cz)==LID_HALF?mat("apron"):AIR;
        return mat("gravel");
    }
    /** How far a pad's lid stands slid, read from the blocks: the first of its seat's columns that is not lid. */
    public static int slid(ServerLevel l,BlockPos b,BlockPos c){
        for(int k=0;k<=LID_SLIDE;k++)if(FallowBlock.is(l.getBlockState(b.offset(c.getX()-LID_HALF+k,-1,c.getZ())),FallowBlock.Kind.PAD))return k;
        return LID_SLIDE;
    }
    static void pads(ServerLevel l,BlockPos b){pad(l,b,PAD_A);pad(l,b,PAD_B);}

    /** The forecourt: concrete laid in squares from the store to the shoulder, the pump island with its two pumps, the canopy on its four
     * columns, the diesel pump on its own island, the sign on its pole by the road. */
    static void forecourt(ServerLevel l,BlockPos b){
        clearGround(l,b,APRON_W,STORE_N-1,APRON_E,APRON_N,mat("apron"));
        clearGround(l,b,APRON_W,APRON_N,APRON_E,APRON_N,mat("apron"));
        for(int x=-3;x<=2;x++){
            boolean end=x==-3||x==2;
            if(x==-2||x==1)continue;
            set(l,b,x,0,ISLAND_Z,fit(FallowBlock.Kind.ISLAND,end?(x<0?Direction.WEST:Direction.EAST):Direction.NORTH,end?1:0));
        }
        for(int x:new int[]{-2,1}){set(l,b,x,0,ISLAND_Z,fit(FallowBlock.Kind.PUMP,Direction.NORTH,0));set(l,b,x,1,ISLAND_Z,fit(FallowBlock.Kind.PUMP,Direction.NORTH,1));}
        // The canopy: a deck at the height of the store's eaves, its lights let into its underside, fascia round its edge, four columns.
        for(int x=CANOPY_W;x<=CANOPY_E;x++)for(int z=CANOPY_N;z<=CANOPY_S;z++){
            boolean w=x==CANOPY_W,e=x==CANOPY_E,s=z==CANOPY_S,n=z==CANOPY_N;
            BlockState st;
            if((w||e)&&(s||n))st=fit(FallowBlock.Kind.CANOPY,n?(w?Direction.NORTH:Direction.EAST):(e?Direction.SOUTH:Direction.WEST),3);
            else if(w||e||s||n)st=fit(FallowBlock.Kind.CANOPY,n?Direction.NORTH:s?Direction.SOUTH:w?Direction.WEST:Direction.EAST,2);
            else st=fit(FallowBlock.Kind.CANOPY,Direction.NORTH,(Math.floorMod(x+1,4)==0&&Math.floorMod(z+1,3)==0)?1:0);
            set(l,b,x,CANOPY_Y,z,st);
        }
        for(int[] c:new int[][]{{CANOPY_W+1,CANOPY_S-1},{CANOPY_E-1,CANOPY_S-1},{CANOPY_W+1,CANOPY_N+1},{CANOPY_E-1,CANOPY_N+1}}){
            set(l,b,c[0],0,c[1],fit(FallowBlock.Kind.CANOPY,Direction.NORTH,5));
            for(int y=1;y<CANOPY_Y;y++)set(l,b,c[0],y,c[1],fit(FallowBlock.Kind.CANOPY,Direction.NORTH,y==CANOPY_Y-1?6:4));
        }
        // The diesel pump on its own island by the rig.
        set(l,b,DRIVER_AT.getX()+1,0,ISLAND_Z,fit(FallowBlock.Kind.ISLAND,Direction.WEST,1));
        set(l,b,DRIVER_AT.getX()+2,0,ISLAND_Z,fit(FallowBlock.Kind.PUMP,Direction.SOUTH,2));set(l,b,DRIVER_AT.getX()+2,1,ISLAND_Z,fit(FallowBlock.Kind.PUMP,Direction.SOUTH,3));
        set(l,b,DRIVER_AT.getX()+3,0,ISLAND_Z,fit(FallowBlock.Kind.ISLAND,Direction.EAST,1));
        // The sign by the road, where the drivers coming either way see it first.
        int sx=-12,sz=APRON_N;
        for(int y=0;y<5;y++)set(l,b,sx,y,sz,fit(FallowBlock.Kind.SIGN,Direction.NORTH,0));
        set(l,b,sx,5,sz,fit(FallowBlock.Kind.SIGN,Direction.NORTH,1));set(l,b,sx,6,sz,fit(FallowBlock.Kind.SIGN,Direction.NORTH,2));
    }

    /** The store: painted boards on a block plinth, a flat roof with the station's name on a board along its front, plate windows either
     * side of the front door, and inside, the counter, its register and its flap, the back shelves, the coolers, the shelving, the maps,
     * the coffee and the lights. */
    static void store(ServerLevel l,BlockPos b){
        box(l,b,STORE_W,-1,STORE_N,STORE_E,-1,STORE_S,mat("lino"));
        for(int x=STORE_W;x<=STORE_E;x++)for(int z=STORE_N;z<=STORE_S;z++){
            boolean wall=x==STORE_W||x==STORE_E||z==STORE_N||z==STORE_S;
            for(int y=0;y<CEILING;y++)set(l,b,x,y,z,wall?(y==0?mat("plinth"):mat("siding")):AIR);
            set(l,b,x,CEILING,z,mat("ceiling"));
            set(l,b,x,CEILING+1,z,wall?mat("siding"):mat("roof"));
        }
        // The name on a board standing along the front of the roof, centred over the door. It is read from the forecourt, facing south,
        // where east is on the left: its first piece is the east end (0.4.680; it read backwards before).
        for(int i=0;i<4;i++)set(l,b,1-i,CEILING+2,STORE_N,fit(FallowBlock.Kind.SIGNBAND,Direction.NORTH,i));
        door(l,b,FRONT_DOOR,Direction.NORTH);
        for(int x=-6;x<=6;x++){
            if(Math.abs(x)<2)continue;
            set(l,b,x,1,STORE_N,fit(FallowBlock.Kind.WINDOW,Direction.NORTH,0));set(l,b,x,2,STORE_N,fit(FallowBlock.Kind.WINDOW,Direction.NORTH,1));
        }
        // A window in each side wall, toward the front.
        for(int z:new int[]{-9,-10}){set(l,b,STORE_W,1,z,fit(FallowBlock.Kind.WINDOW,Direction.WEST,0));set(l,b,STORE_W,2,z,fit(FallowBlock.Kind.WINDOW,Direction.WEST,1));}
        // The counter across the room, the register and the candy rack on it, the flap at the east end to get round.
        for(int x=STORE_W+1;x<=STORE_E-2;x++)set(l,b,x,0,COUNTER_Z,fit(FallowBlock.Kind.COUNTER,Direction.NORTH,x==-1?1:x==-5?3:0));
        set(l,b,STORE_E-1,0,COUNTER_Z,fit(FallowBlock.Kind.COUNTER,Direction.NORTH,2));
        set(l,b,NOTE,fit(FallowBlock.Kind.NOTE,Direction.NORTH,0));
        set(l,b,STOOL,fit(FallowBlock.Kind.STOOL,Direction.NORTH,0));
        // The back wall's shelves either side of the House's door: cabinets below, cartons and cans above, the radio on one.
        for(int x=STORE_W+1;x<=STORE_E-1;x++){
            if(Math.abs(x)<3)continue;
            set(l,b,x,0,STORE_S-1,fit(FallowBlock.Kind.BACKSHELF,Direction.NORTH,0));
            set(l,b,x,1,STORE_S-1,fit(FallowBlock.Kind.BACKSHELF,Direction.NORTH,x==RADIO.getX()?2:1));
        }
        set(l,b,TV,fit(FallowBlock.Kind.TV,Direction.NORTH,0));
        // The coolers along the west wall, glass doors and all.
        // Its lit panel runs along the tops of the doors, ICE over the second door and COLD over the third as a reader faces it.
        for(int z=-5;z>=-8;z--){set(l,b,STORE_W+1,0,z,fit(FallowBlock.Kind.COOLER,Direction.EAST,0));set(l,b,STORE_W+1,1,z,fit(FallowBlock.Kind.COOLER,Direction.EAST,z==-6?1:z==-7?2:3));}
        // A run of shelving from front to back, double-sided, with its low top, so the register can be seen from the front door.
        for(int z=GONDOLA_S;z>=GONDOLA_N;z--){
            set(l,b,GONDOLA_X,0,z,fit(FallowBlock.Kind.GONDOLA,Direction.EAST,Math.floorMod(z,3)));set(l,b,GONDOLA_X,1,z,fit(FallowBlock.Kind.GONDOLA,Direction.EAST,3));
        }
        // The rack of road maps by the door, and the coffee machine on its stand by the east wall.
        set(l,b,-3,0,STORE_N+1,fit(FallowBlock.Kind.MAPS,Direction.SOUTH,0));set(l,b,-3,1,STORE_N+1,fit(FallowBlock.Kind.MAPS,Direction.SOUTH,1));
        set(l,b,STORE_E-1,0,-9,fit(FallowBlock.Kind.COFFEE,Direction.WEST,0));
        for(int[] at:new int[][]{{-4,-5},{4,-5},{-4,-9},{4,-9}})set(l,b,at[0],CEILING-1,at[1],fit(FallowBlock.Kind.TUBE,Direction.NORTH,0));
        // The door into the garage.
        door(l,b,GARAGE_DOOR,Direction.EAST);
    }

    /** The garage bay: corrugated walls on the store's east wall, a stained floor, the front open where the door is rolled up, the lift's
     * two posts and their crossbars, a bench with its pegboard, tyres and a drum. */
    static void garage(ServerLevel l,BlockPos b){
        box(l,b,STORE_E,-1,STORE_N,GARAGE_E,-1,STORE_S,mat("garage_floor"));
        for(int x=STORE_E+1;x<=GARAGE_E;x++)for(int z=STORE_N;z<=STORE_S;z++){
            boolean wall=x==GARAGE_E||z==STORE_N||z==STORE_S;
            boolean bay=z==STORE_N&&x>=BAY_W&&x<=BAY_E;
            for(int y=0;y<GARAGE_TOP;y++)set(l,b,x,y,z,wall&&!(bay&&y<GARAGE_TOP-1)?mat("metal"):AIR);
            set(l,b,x,GARAGE_TOP,z,mat("roof"));
        }
        for(int y=CEILING;y<=GARAGE_TOP;y++)for(int z=STORE_N;z<=STORE_S;z++)set(l,b,STORE_E,y,z,mat("metal"));
        for(int x:new int[]{BAY_W+1,BAY_E-1}){
            set(l,b,x,0,-6,fit(FallowBlock.Kind.LIFT,Direction.NORTH,0));set(l,b,x,1,-6,fit(FallowBlock.Kind.LIFT,Direction.NORTH,0));
            set(l,b,x,2,-6,fit(FallowBlock.Kind.LIFT,Direction.NORTH,6));
        }
        for(int x=BAY_W+2;x<=BAY_E-2;x++)set(l,b,x,1,-6,fit(FallowBlock.Kind.LIFT,Direction.NORTH,1));
        for(int z=-2;z>=-4;z--){set(l,b,GARAGE_E-1,0,z,fit(FallowBlock.Kind.LIFT,Direction.WEST,2));set(l,b,GARAGE_E-1,1,z,fit(FallowBlock.Kind.LIFT,Direction.WEST,3));}
        set(l,b,GARAGE_E-1,0,-9,fit(FallowBlock.Kind.LIFT,Direction.WEST,4));set(l,b,GARAGE_E-1,0,-10,fit(FallowBlock.Kind.LIFT,Direction.WEST,5));
        for(int[] at:new int[][]{{12,-3},{12,-9},{16,-3},{16,-9}})set(l,b,at[0],GARAGE_TOP-1,at[1],fit(FallowBlock.Kind.TUBE,Direction.NORTH,0));
    }

    /** The keeper's yard: trodden earth inside a wire fence, his trailer on its blocks with its steps and its tank, a clothesline, a
     * barrel, and the storm cellar's doors on their low frame. */
    static void yard(ServerLevel l,BlockPos b){
        clearGround(l,b,YARD_W,0,STORE_W-1,YARD_N,mat("yard"));
        for(int x=YARD_W;x<=STORE_W-1;x++)set(l,b,x,0,YARD_N,fit(FallowBlock.Kind.TRAILER,Direction.NORTH,7));
        for(int z=0;z>=YARD_N+1;z--)set(l,b,YARD_W,0,z,fit(FallowBlock.Kind.TRAILER,Direction.WEST,7));
        set(l,b,-16,0,YARD_N,AIR);set(l,b,-15,0,YARD_N,AIR);
        // The trailer: skirting round its foot, ribbed walls, a low roof, curtained windows, its door up a step on the east side.
        for(int x=TRAILER_W;x<=TRAILER_E;x++)for(int z=TRAILER_N;z<=TRAILER_S;z++){
            boolean wall=x==TRAILER_W||x==TRAILER_E||z==TRAILER_N||z==TRAILER_S;
            set(l,b,x,0,z,wall?mat("skirting"):mat("lino"));
            for(int y=1;y<=3;y++)set(l,b,x,y,z,wall?mat("trailer"):AIR);
            set(l,b,x,4,z,mat("trailer_roof"));
        }
        for(int z:new int[]{-5,-11,-12})set(l,b,TRAILER_E,2,z,fit(FallowBlock.Kind.TRAILER,Direction.EAST,0));
        for(int z:new int[]{-5,-9,-12})set(l,b,TRAILER_W,2,z,fit(FallowBlock.Kind.TRAILER,Direction.WEST,0));
        set(l,b,TRAILER_W+1,2,TRAILER_S,fit(FallowBlock.Kind.TRAILER,Direction.SOUTH,0));set(l,b,TRAILER_W+2,2,TRAILER_N,fit(FallowBlock.Kind.TRAILER,Direction.NORTH,0));
        door(l,b,TRAILER_DOOR,Direction.EAST);
        set(l,b,TRAILER_E+1,0,TRAILER_DOOR.getZ(),fit(FallowBlock.Kind.TRAILER,Direction.EAST,3));
        set(l,b,(TRAILER_W+TRAILER_E)/2,0,TRAILER_N-1,fit(FallowBlock.Kind.TRAILER,Direction.NORTH,1));
        set(l,b,TRAILER_E+1,0,-4,fit(FallowBlock.Kind.TRAILER,Direction.EAST,2));
        // The clothesline: two posts and the lines between them, and the barrel by the store's wall.
        // (A post stands two blocks tall from its one cell, its crossbar across the lines; the lines hang in the cells between at that height.)
        set(l,b,-18,0,-3,fit(FallowBlock.Kind.TRAILER,Direction.EAST,4));set(l,b,-11,0,-3,fit(FallowBlock.Kind.TRAILER,Direction.EAST,4));
        // Two runs of washing, one each way round: shirts on the one, a towel on the other.
        for(int x=-17;x<=-12;x++)set(l,b,x,1,-3,fit(FallowBlock.Kind.TRAILER,Math.floorMod(x,2)==0?Direction.NORTH:Direction.SOUTH,5));
        set(l,b,STORE_W-1,0,-2,fit(FallowBlock.Kind.TRAILER,Direction.NORTH,6));
        // The cellar's doors, two flat leaves on a low concrete curb, over a well of steps going down to the north.
        for(int z=WELL_S;z>=WELL_N;z--){
            int seg=z==WELL_S?0:z==WELL_N?2:1;
            set(l,b,WELL_W,0,z,fit(FallowBlock.Kind.CELLAR,Direction.NORTH,seg));
            set(l,b,WELL_E,0,z,fit(FallowBlock.Kind.CELLAR,Direction.NORTH,seg+3));
        }
    }

    /** The storm cellar: stone walls, a floor of packed earth, the yard's own ground for a roof, timbered from under; three steep steps
     * down from the doors through a gap in the room's south wall; shelves of jars and tins, a cot, a crock of water and a lantern. */
    static void cellar(ServerLevel l,BlockPos b){
        for(int x=CELLAR_W;x<=CELLAR_E;x++)for(int z=CELLAR_N;z<=CELLAR_S;z++){
            boolean wall=x==CELLAR_W||x==CELLAR_E||z==CELLAR_N||z==CELLAR_S;
            set(l,b,x,CELLAR_FLOOR,z,mat("cellar_floor"));
            for(int y=CELLAR_FLOOR+1;y<=-2;y++)set(l,b,x,y,z,wall?mat("cellar_stone"):AIR);
            set(l,b,x,-1,z,mat("cellar_roof"));
        }
        // The well: stone either side, open overhead to the doors, three steps each a half step at a time from the yard's own level, and
        // at the foot a landing on the cellar's floor in the gap through its south wall, still under the doors, so a body comes off the
        // last step with open air over its head and walks on into the room under its roof (0.4.674: the steps began a full block below
        // the yard, which a body could drop down but not walk, nor climb without a jump).
        var steps=FallowRegistry.CELLAR_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
        for(int z=WELL_S;z>=WELL_N;z--){
            int step=z==WELL_N?CELLAR_FLOOR:-1-(WELL_S-z);
            for(int y=CELLAR_FLOOR;y<=-1;y++){set(l,b,WELL_W-1,y,z,mat("cellar_stone"));set(l,b,WELL_E+1,y,z,mat("cellar_stone"));}
            for(int x=WELL_W;x<=WELL_E;x++)for(int y=CELLAR_FLOOR;y<=-1;y++)set(l,b,x,y,z,y<step?mat("cellar_stone"):y==step?(z==WELL_N?mat("cellar_floor"):steps):AIR);
        }
        // A fieldstone lintel over the gap: the roof's edge there would show the yard's earth to anyone looking down the well.
        for(int x=WELL_W;x<=WELL_E;x++)set(l,b,x,-1,WELL_N-1,mat("cellar_stone"));
        // Shelves along the west wall, the cot along the east, the crock in the far corner, the lantern hung from the middle of the roof.
        for(int z=CELLAR_S-1;z>=CELLAR_N+2;z--){
            set(l,b,CELLAR_W+1,CELLAR_FLOOR+1,z,fit(FallowBlock.Kind.SHELF,Direction.EAST,0));
            set(l,b,CELLAR_W+1,CELLAR_FLOOR+2,z,fit(FallowBlock.Kind.SHELF,Direction.EAST,Math.floorMod(z,2)));
        }
        set(l,b,COT_HEAD,fit(FallowBlock.Kind.COT,Direction.NORTH,0));set(l,b,COT_HEAD.south(),fit(FallowBlock.Kind.COT,Direction.NORTH,1));
        set(l,b,CROCK,fit(FallowBlock.Kind.CROCK,Direction.EAST,0));
        set(l,b,LANTERN,fit(FallowBlock.Kind.CROCK,Direction.NORTH,1));
    }

    /** The driver's rig by the diesel pump: a cab with its long hood to the west, and its trailer behind, a box on two axles standing on
     * its legs. Each slice of the cab is drawn whole by one cell; its other half is a cell that only stands in the way. */
    static void rig(ServerLevel l,BlockPos b){
        int z=RIG_Z,z2=RIG_Z-1;
        // facing WEST: the slice's model draws both cells, the north one (z2) being its right-hand half seen from the cab.
        int[][] cab={{0,1,-1},{2,3,6},{4,5,6}};
        for(int i=0;i<3;i++){
            int x=RIG_FRONT+i;
            set(l,b,x,0,z,fit(FallowBlock.Kind.RIG,Direction.WEST,cab[i][0]));set(l,b,x,1,z,fit(FallowBlock.Kind.RIG,Direction.WEST,cab[i][1]));
            if(cab[i][2]>=0)set(l,b,x,2,z,fit(FallowBlock.Kind.RIG,Direction.WEST,cab[i][2]));
            for(int y=0;y<=(cab[i][2]>=0?2:1);y++)set(l,b,x,y,z2,fit(FallowBlock.Kind.RIG,Direction.WEST,7));
        }
        for(int x=TRAILER_FRONT;x<=TRAILER_BACK;x++){
            boolean wheels=x>=TRAILER_BACK-2,legs=x==TRAILER_FRONT+2;
            set(l,b,x,0,z,wheels?fit(FallowBlock.Kind.BOX,Direction.WEST,0):legs?fit(FallowBlock.Kind.BOX,Direction.WEST,1):AIR);
            set(l,b,x,0,z2,wheels||legs?fit(FallowBlock.Kind.BOX,Direction.WEST,3):AIR);
            for(int y=1;y<=3;y++){
                var face=x==TRAILER_BACK?fit(FallowBlock.Kind.BOX,Direction.EAST,2):mat("box");
                set(l,b,x,y,z,face);set(l,b,x,y,z2,face);
            }
        }
    }
}
