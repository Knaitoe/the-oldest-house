package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.699, The list, part 1a (docs/THE_LIST_0_4_699.md): the middle of Lamesa at night, under the outside sky. Every position is relative to
 * the base, the floor just inside the House's door; north is -z; people stand at y 0 on ground at y -1.
 *
 * <p>The House's door is the back door of the reader's own house: a brick-and-siding ranch house on South Ninth with a carport, a swamp
 * cooler on the roof and a porch light, its kitchen, hall, the reader's room, the bathroom, the parents' room and the front room. Behind it
 * the House's own hall stands as the metal shop in the back yard, inside a chain-link fence with a clothesline and a burn barrel. South
 * Ninth is caliche; so is South Seventh to the north; houses face both, dark or lit. Avenue K is the highway, paved, running north and
 * south past them. Main Street crosses it farther north, paved, with curbs and sidewalks: the feed store with the bus bench out front,
 * Dawson Drug, the video store, the sheriff's substation and the Caprock drive-in with its stalls and its sign. East of the highway, the
 * cotton runs to the tracks, and past the tracks are the gin, its yard of modules, the seed house and the water tower with the town's name
 * on it. North of them the playa lies flat and pale. Everywhere else is cotton, open and white, to a haze.
 */
public final class TheListSite {
    private TheListSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.THE_LIST;
    /** The scene's edges. South of the House's door are the back yard, the cotton behind it and, from 0.4.701, the pasture. */
    public static final int WEST=-164,EAST=96,NORTH=-176,SOUTH=140;
    /** The town stands at its own height, as the other tall places outside do, so the water tower's catwalk and tank are in it. */
    public static final int BASE_Y=96;
    /** The reader's house: its outer walls, the wallboard inside them, the ceiling's height. */
    public static final int HOUSE_W=-13,HOUSE_E=13,HOUSE_S=0,HOUSE_N=-18,CEILING=3;
    /** The rooms: the kitchen, the reader's room, the hall, the parents' room, the bathroom, the front room; the walls between them. */
    public static final int KITCHEN_E=0,ROOM_W=2,FRONT_WALL_Z=-8,HALL_S=-9,HALL_N=-10,BACK_WALL_Z=-11,PARENTS_E=-6,BATH_W=-4,BATH_E=-2,LIVING_W=0;
    /** The front yard, South Ninth, the lots across it, South Seventh, the lots across that, the alley behind Main Street. */
    public static final int YARD_N=-24,NINTH_S=-25,NINTH_N=-31,SEVENTH_S=-68,SEVENTH_N=-74,ALLEY_S=-94,ALLEY_N=-95;
    /** The streets' west end, where the cotton begins. */
    public static final int STREETS_W=-60;
    /** Avenue K, the highway, and the line down it. */
    public static final int AVE_W=18,AVE_E=24,AVE_LINE=21;
    /** Main Street: the south side's fronts, its sidewalk and curb, the pavement and its line, the north side's curb, sidewalk and fronts. */
    public static final int MAIN_FRONT_S=-109,WALK_S_S=-110,WALK_S_N=-112,CURB_S=-113,MAIN_S=-114,MAIN_LINE=-117,MAIN_N=-121,CURB_N=-122,
        WALK_N_S=-123,WALK_N_N=-125,MAIN_FRONT_N=-126,MAIN_BACK_S=-96,MAIN_BACK_N=-140;
    /** The tracks: the ballast and the rails on it. */
    public static final int TRACK_W=62,TRACK_X=64,TRACK_E=66;
    /** The gin, its yard of modules, the seed house, the water tower's middle and the playa. */
    public static final int GIN_W=72,GIN_E=92,GIN_S=-40,GIN_N=-68,GIN_TOP=9;
    public static final int MODULES_S=-12,MODULES_N=-36;
    public static final int SEED_W=74,SEED_E=84,SEED_S=-74,SEED_N=-86,SEED_TOP=10;
    /** The seed house bulb's height, on its chain from the roof. */
    public static final int BULB_Y=SEED_TOP-4;
    public static final int TOWER_X=57,TOWER_Z=-82,TOWER_LEG_TOP=21,CATWALK_Y=22,TANK_BOTTOM=23,TANK_TOP=29;
    public static final int PLAYA_W=70,PLAYA_S=-100;
    /** 0.4.700, the school's field out west (docs/THE_LIST_0_4_700.md): its fence; the field's sidelines and end lines and its middle; the
     * gate in the east fence where South Seventh comes in; the field house's back wall on the field; the press box's door. */
    public static final int FENCE_W=-161,FENCE_E=-96,FENCE_N=-98,FENCE_S=-16;
    public static final int FIELD_W=-136,FIELD_E=-112,FIELD_N=-88,FIELD_S=-28,FIELD_X=-124,FIELD_Z=-58,GATE_N=-73,GATE_S=-69,FIELD_HOUSE_S=-100,PRESS_DOOR_Z=-53;
    /** Amber's house on South Seventh: its walls; the front and back doors, the television in the den and her desk. */
    public static final int AMBER_W=-12,AMBER_E=12,AMBER_S=-78,AMBER_N=-90;
    public static final BlockPos AMBER_FRONT=new BlockPos(0,0,AMBER_S),AMBER_BACK=new BlockPos(0,0,AMBER_N),AMBER_TV=new BlockPos(-10,0,-82),AMBER_DESK=new BlockPos(5,0,-80);
    /** 0.4.701, the pasture south of town (docs/THE_LIST_0_4_701.md): the back yard's fence; the county road west off Avenue K; the
     * pasture's fence, its wire gap in the north fence and the lease road inside; the stock tank and the windmill beside it, whose head is
     * at WIND_TOP; the three pumpjacks' Samson posts and the way each horsehead faces; the lease's tank battery. */
    public static final int YARD_S=22,ROAD_N=42,ROAD_S=46,PAST_W=-76,PAST_E=6,PAST_N=34,PAST_S=124,GAP_X=-40,LEASE_N=43,LEASE_S=45,WIND_TOP=12;
    public static final BlockPos TANK_AT=new BlockPos(-30,0,84),WINDMILL_AT=new BlockPos(-38,0,84);
    public static final BlockPos[] JACKS={new BlockPos(-40,0,56),new BlockPos(-12,0,100),new BlockPos(-62,0,108)};
    public static final Direction[] JACK_FACING={Direction.NORTH,Direction.WEST,Direction.EAST};
    public static final BlockPos[] BATTERY_TANKS={new BlockPos(-4,0,51),new BlockPos(0,0,51)};
    /** The sprinklers going on the neighbors' lawns. */
    public static final int[][] SPRINKLERS={{-30,-34},{-3,-65},{-36,-21}};
    /** The House's door at the back of the kitchen; the front door; the reader's own door; the empty house across the street, whose door
     * is the way on. */
    public static final BlockPos FRONT_DOOR=new BlockPos(5,0,HOUSE_N),ROOM_DOOR=new BlockPos(5,0,FRONT_WALL_Z),ONWARD=new BlockPos(0,0,-37);
    /** The telephone on the kitchen wall, where the first paper will be; the reader's desk with the tape deck, where each reader's
     * account will be. */
    public static final BlockPos SOURCE=new BlockPos(-11,1,-6),ENDING=new BlockPos(10,0,-2);
    /** The reader's bed, head and foot. */
    public static final BlockPos BED_HEAD=new BlockPos(11,0,-6),BED_FOOT=new BlockPos(10,0,-6);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inScene(Vec3 r){return r.x>=WEST&&r.x<=EAST+1&&r.z>=NORTH&&r.z<=SOUTH+1&&r.y>=-6&&r.y<=40;}
    /** Inside the pasture's fence. */
    public static boolean inPasture(Vec3 r){return r.x>PAST_W+1&&r.x<PAST_E&&r.z>PAST_N+1&&r.z<PAST_S;}
    /** Where a pumpjack's machine block stands: over its Samson post, two up. */
    public static BlockPos jackMachine(int i){return JACKS[i].above(2);}
    public static BlockPos windmillHead(){return WINDMILL_AT.above(WIND_TOP);}
    /** Inside the reader's house, under its ceiling. */
    public static boolean inHouse(Vec3 r){return r.x>HOUSE_W+1&&r.x<HOUSE_E&&r.z<HOUSE_S&&r.z>HOUSE_N+1&&r.y>-.5&&r.y<CEILING;}
    /** Inside one of Main Street's stores. */
    public static boolean inStore(Vec3 r){
        if(r.y<-.5||r.y>4)return false;
        boolean south=r.z>MAIN_FRONT_S+1&&r.z<MAIN_BACK_S,north=r.z<MAIN_FRONT_N&&r.z>MAIN_BACK_N+1;
        if(!south&&!north)return false;
        for(int[] s:STORES)if(r.x>s[0]+1&&r.x<s[1]&&(s[2]==0?south:north))return true;
        return false;
    }
    /** Inside Amber's house, under its ceiling. */
    public static boolean inAmber(Vec3 r){return r.x>AMBER_W+1&&r.x<AMBER_E&&r.z<AMBER_S&&r.z>AMBER_N+1&&r.y>-.5&&r.y<CEILING;}
    /** Inside the field house. */
    public static boolean inFieldHouse(Vec3 r){return r.x>FIELD_X-11&&r.x<FIELD_X+12&&r.z<FIELD_HOUSE_S&&r.z>MAIN_FRONT_S+1&&r.y>-.5&&r.y<3;}
    /** Under a roof: the reader's house, Amber's, the field house, a store, the gin, the seed house. */
    public static boolean indoors(Vec3 r){
        return inHouse(r)||inAmber(r)||inFieldHouse(r)||inStore(r)||r.x>GIN_W+1&&r.x<GIN_E&&r.z<GIN_S&&r.z>GIN_N+1&&r.y<GIN_TOP||r.x>SEED_W+1&&r.x<SEED_E&&r.z<SEED_S&&r.z>SEED_N+1&&r.y<SEED_TOP;
    }
    /** Main Street's stores, west to east: {west wall, east wall, 0 south side or 1 north side}. The feed store, Dawson Drug, the video
     * store, the substation. */
    static final int[][] STORES={{-42,-24,0},{-18,-4,0},{-14,2,1},{30,40,0}};

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return TheListRegistry.material(name).defaultBlockState();}
    static BlockState fit(Kind kind,Direction facing,int stage){return TheListBlock.of(kind,facing,stage);}
    static final BlockState AIR=Blocks.AIR.defaultBlockState();
    /** A number from the place alone, never from chance, so that the town is built the same each time. */
    static double hash(int x,int z,int salt){
        long h=x*734287L+z*912367L+salt*15485863L;h^=h>>>13;h*=0x5bd1e995L;h^=h>>>15;h*=0x27d4eb2dL;h^=h>>>16;
        return (h&0xFFFFFF)/(double)0x1000000;
    }

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,-6,NORTH,EAST,14,SOUTH,AIR);
        box(l,b,WEST,-4,NORTH,EAST,-2,SOUTH,mat("dirt"));
        cotton(l,b);
        streets(l,b);
        lots(l,b);
        house(l,b);
        yard(l,b);
        shop(l,b);
        neighbors(l,b);
        amberHouse(l,b);
        yards(l,b);
        school(l,b);
        main(l,b);
        driveIn(l,b);
        tracks(l,b);
        gin(l,b);
        seedHouse(l,b);
        tower(l,b);
        playa(l,b);
        poles(l,b);
        mesquite(l,b);
        pasture(l,b);
    }

    // ------------------------------------------------------------------------------------------------ the ground
    /** Cotton over everything first, in rows running north and south with furrows between, open and white; a plant shorter here, one
     * leaning over at a row's end, bare stalks where it was picked. Everything else is laid over it. */
    static void cotton(ServerLevel l,BlockPos b){
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=SOUTH;z++){
            set(l,b,x,-1,z,mat("soil"));
            set(l,b,x,0,z,cottonAt(x,z));
        }
    }
    static BlockState cottonAt(int x,int z){
        if(Math.floorMod(x,2)==1)return AIR;
        double h=hash(x,z,7);
        int stage=h<.06?3:h<.2?1:0;
        if(Math.floorMod(z,23)==0)stage=2;
        return fit(Kind.COTTON,Direction.from2DDataValue((int)(hash(x,z,8)*4)),stage);
    }
    /** Clear a run of ground to a surface, nothing standing on it. */
    static void ground(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,BlockState surface){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++){set(l,b,x,-1,z,surface);set(l,b,x,0,z,AIR);}
    }
    /** A yard's grass: thin, with bare dirt where it has worn through. */
    static void lawn(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++){
            set(l,b,x,-1,z,hash(x,z,11)<.18?mat("dirt"):mat("lawn"));set(l,b,x,0,z,AIR);
        }
    }

    /** South Ninth and South Seventh in caliche from the cotton's edge to the highway; Avenue K paved with a dashed line, the whole length;
     * Main Street paved, edge to edge, with its line, curbs and sidewalks; South Ninth going on east of the highway to the gin. */
    static void streets(ServerLevel l,BlockPos b){
        ground(l,b,STREETS_W,NINTH_S,AVE_W-1,NINTH_N,mat("caliche"));
        ground(l,b,STREETS_W,SEVENTH_S,AVE_W-1,SEVENTH_N,mat("caliche"));
        ground(l,b,STREETS_W,ALLEY_S,AVE_W-1,ALLEY_N,mat("caliche"));
        ground(l,b,AVE_E+1,NINTH_S-1,GIN_W-1,NINTH_N+1,mat("caliche"));
        ground(l,b,AVE_W,NORTH,AVE_E,SOUTH,mat("asphalt"));
        for(int z=NORTH;z<=SOUTH;z++)if(Math.floorMod(z,6)<3&&!(z<=MAIN_S&&z>=MAIN_N))set(l,b,AVE_LINE,-1,z,mat("main_line"));
        ground(l,b,WEST,MAIN_S,EAST,MAIN_N,mat("asphalt"));
        for(int x=WEST;x<=EAST;x++){
            if(Math.floorMod(x,6)<3&&(x<AVE_W||x>AVE_E))set(l,b,x,-1,MAIN_LINE,mat("main_line"));
            if(x<AVE_W||x>AVE_E){set(l,b,x,-1,CURB_S,mat("curb"));set(l,b,x,-1,CURB_N,mat("curb"));set(l,b,x,0,CURB_S,AIR);set(l,b,x,0,CURB_N,AIR);}
        }
        ground(l,b,WEST,WALK_S_S,AVE_W-1,WALK_S_N,mat("sidewalk"));
        ground(l,b,AVE_E+1,WALK_S_S,TRACK_W-2,WALK_S_N,mat("sidewalk"));
        ground(l,b,WEST,WALK_N_S,AVE_W-1,WALK_N_N,mat("sidewalk"));
        ground(l,b,AVE_E+1,WALK_N_S,TRACK_W-2,WALK_N_N,mat("sidewalk"));
        // The highway's shoulders, caliche, where it runs between the cotton.
        for(int z=NORTH;z<=SOUTH;z++){
            if(z<=WALK_S_S&&z>=WALK_N_N)continue;
            set(l,b,AVE_W-1,-1,z,mat("caliche"));set(l,b,AVE_W-1,0,z,AIR);set(l,b,AVE_E+1,-1,z,mat("caliche"));set(l,b,AVE_E+1,0,z,AIR);
        }
        // The corners' street signs and a stop sign at each street that meets the highway.
        corner(l,b,AVE_W-2,0,NINTH_S+1,0);
        corner(l,b,AVE_W-2,0,SEVENTH_S+1,2);
        corner(l,b,AVE_W-2,0,WALK_S_S,1);
        // Stop signs where each street meets the highway, on the right of anyone driving east to it.
        set(l,b,AVE_W-3,0,NINTH_S+1,fit(Kind.SIGNPOST,Direction.WEST,3));
        set(l,b,AVE_W-3,0,SEVENTH_S+1,fit(Kind.SIGNPOST,Direction.WEST,3));
    }
    /** A corner's post with its two blades on top: the street's and the avenue's. */
    static void corner(ServerLevel l,BlockPos b,int x,int y,int z,int which){
        set(l,b,x,y,z,fit(Kind.SIGNPOST,Direction.NORTH,which));
    }

    /** The lots on both streets west of the highway: worn grass, chain-link along the backs and between some of them, weeds where nobody
     * mows. The houses on them are laid over these. */
    static void lots(ServerLevel l,BlockPos b){
        lawn(l,b,STREETS_W,NINTH_N-1,AVE_W-2,SEVENTH_S+1);
        lawn(l,b,STREETS_W,SEVENTH_N-1,AVE_W-2,ALLEY_S+1);
        lawn(l,b,-48,HOUSE_S,AVE_W-2,YARD_N);
        // The back fences between the two rows of lots, along the middle of each block.
        fenceRun(l,b,STREETS_W,-50,AVE_W-2,-50,Direction.NORTH);
        fenceRun(l,b,STREETS_W,-91,AVE_W-2,-91,Direction.NORTH);
        for(int x:new int[]{-50,-18,14})fenceRun(l,b,x,-49,x,-38,Direction.EAST);
        for(int x:new int[]{-50,-18,14})fenceRun(l,b,x,-51,x,-60,Direction.EAST);
        // Weeds in the unmowed corners and along the fences.
        for(int x=STREETS_W;x<=AVE_W-2;x++)for(int z=ALLEY_S+1;z<=YARD_N;z++){
            if(z<=NINTH_S&&z>=NINTH_N||z<=SEVENTH_S&&z>=SEVENTH_N)continue;
            if(!BuildBlocks.state(l,b.offset(x,0,z)).isAir())continue;
            double h=hash(x,z,21);
            if(h<.025)set(l,b,x,0,z,fit(Kind.WEED,Direction.from2DDataValue((int)(hash(x,z,22)*4)),h<.006?0:h<.012?1:h<.02?2:3));
        }
    }
    /** Chain-link from one end to the other along x or z, a post every fourth. */
    static void fenceRun(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,Direction faces){
        if(z0==z1)for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)set(l,b,x,0,z0,fit(Kind.FENCE,faces,Math.floorMod(x,4)==0?1:0));
        else for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x0,0,z,fit(Kind.FENCE,faces,Math.floorMod(z,4)==0?1:0));
    }

    // ------------------------------------------------------------------------------------------------ the reader's house
    /**
     * The house: a brick course along the foot of the outer walls, siding above, and wallboard inside them; linoleum in the kitchen and the
     * bathroom, carpet elsewhere; the ceiling at three. The kitchen at the back, with the House's door in its south wall, the sink under its
     * window, the range, the refrigerator, the cabinets, the dinette and the telephone; the reader's room beside it; a hall across the
     * middle; the parents' room, the bathroom and the front room along the front, the front door into the front room. A low roof of
     * shingles over all of it, the swamp cooler on it, the carport to the west.
     */
    static void house(ServerLevel l,BlockPos b){
        for(int x=HOUSE_W;x<=HOUSE_E;x++)for(int z=HOUSE_N;z<=HOUSE_S;z++){
            boolean outer=x==HOUSE_W||x==HOUSE_E||z==HOUSE_N||z==HOUSE_S;
            boolean lining=!outer&&(x==HOUSE_W+1||x==HOUSE_E-1||z==HOUSE_N+1||z==HOUSE_S-1);
            boolean kitchen=x<=KITCHEN_E&&z>FRONT_WALL_Z,bath=x>=BATH_W&&x<=BATH_E&&z<BACK_WALL_Z;
            set(l,b,x,-1,z,outer||lining?mat("slab"):kitchen||bath?mat("lino"):mat("carpet"));
            for(int y=0;y<CEILING;y++)set(l,b,x,y,z,outer?(y==0?mat("wainscot"):mat("siding")):lining?(livingWall(x,z)?mat("paneling"):mat("drywall")):AIR);
            set(l,b,x,CEILING,z,outer?mat("siding"):mat("ceiling"));
        }
        // The walls between the rooms, wallboard on both sides; paneling where they face the front room.
        for(int x=HOUSE_W+2;x<=HOUSE_E-2;x++)for(int y=0;y<CEILING;y++){set(l,b,x,y,FRONT_WALL_Z,mat("drywall"));set(l,b,x,y,BACK_WALL_Z,x>=LIVING_W?mat("paneling"):mat("drywall"));}
        for(int z=HOUSE_S-2;z>FRONT_WALL_Z;z--)for(int y=0;y<CEILING;y++)set(l,b,ROOM_W-1,y,z,mat("drywall"));
        for(int z=BACK_WALL_Z-1;z>=HOUSE_N+2;z--)for(int y=0;y<CEILING;y++){set(l,b,PARENTS_E+1,y,z,mat("drywall"));set(l,b,BATH_E+1,y,z,mat("drywall"));}
        // The openings: the House's door through both layers of the back wall, the kitchen into the hall, the hall into the front room.
        for(int y=0;y<=1;y++){set(l,b,0,y,HOUSE_S,AIR);set(l,b,0,y,HOUSE_S-1,AIR);}
        for(int y=0;y<=1;y++){set(l,b,-3,y,FRONT_WALL_Z,AIR);for(int x=1;x<=3;x++)set(l,b,x,y,BACK_WALL_Z,AIR);}
        // Doors: the reader's room, the parents' room and the bathroom off the hall, the front door through the outer wall.
        door(l,b,ROOM_DOOR,Direction.NORTH,true);
        door(l,b,new BlockPos(-8,0,BACK_WALL_Z),Direction.NORTH,false);
        door(l,b,new BlockPos(-3,0,BACK_WALL_Z),Direction.NORTH,false);
        for(int y=0;y<=1;y++)set(l,b,FRONT_DOOR.getX(),y,HOUSE_N+1,AIR);
        door(l,b,FRONT_DOOR,Direction.NORTH,true);
        windows(l,b);
        kitchen(l,b);
        room(l,b);
        hall(l,b);
        parents(l,b);
        bath(l,b);
        living(l,b);
        roof(l,b,HOUSE_W,HOUSE_E,HOUSE_S,HOUSE_N,true);
    }
    private static boolean livingWall(int x,int z){return x>=LIVING_W&&z<BACK_WALL_Z;}
    /** A door, its lower half at the cell and its upper above, set to open outward of the wall it faces from. */
    static void door(ServerLevel l,BlockPos b,BlockPos at,Direction out,boolean right){
        var d=TheListRegistry.HOUSE_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,out).setValue(DoorBlock.HINGE,right?DoorHingeSide.RIGHT:DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    /** A store's glass door, shut in the wall's outer face like its windows, so no reveal shows to the street. */
    static void glassDoor(ServerLevel l,BlockPos b,BlockPos at,Direction out,boolean right){
        var d=TheListRegistry.GLASS_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,out.getOpposite()).setValue(DoorBlock.HINGE,right?DoorHingeSide.LEFT:DoorHingeSide.RIGHT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    /** A window through a double wall: the pane in the outer wall at the middle course, the wallboard opened behind it to a deep sill. */
    static void window(ServerLevel l,BlockPos b,int x,int z,Direction out,int stage){
        set(l,b,x,1,z,fit(Kind.WINDOW,out,stage));
        var in=new BlockPos(x,1,z).relative(out.getOpposite());set(l,b,in,AIR);
    }
    static void windows(ServerLevel l,BlockPos b){
        // None in the back wall behind the shop, which stands against it from x -8 to 8.
        window(l,b,-10,HOUSE_S,Direction.SOUTH,2);window(l,b,10,HOUSE_S,Direction.SOUTH,3);window(l,b,HOUSE_E,-4,Direction.EAST,0);
        window(l,b,HOUSE_W,-4,Direction.WEST,3);window(l,b,HOUSE_W,-14,Direction.WEST,0);
        window(l,b,HOUSE_E,-14,Direction.EAST,0);
        window(l,b,-9,HOUSE_N,Direction.NORTH,0);window(l,b,-3,HOUSE_N,Direction.NORTH,3);
        window(l,b,8,HOUSE_N,Direction.NORTH,4);window(l,b,9,HOUSE_N,Direction.NORTH,5);
    }
    /** The kitchen: counters along the back wall with the sink under the window, the range, the refrigerator by the back door, cabinets
     * over the counters; the telephone on the west wall by the hall; the dinette in the middle with its chairs; the light over the table. */
    static void kitchen(ServerLevel l,BlockPos b){
        int z=HOUSE_S-2;var f=Direction.NORTH;
        for(int x=-11;x<=-3;x++){
            set(l,b,x,0,z,fit(Kind.KITCHEN,f,x==-10?1:x==-6?2:x==-4?7:0));
            if(x!=-10&&x!=-6)set(l,b,x,2,z,fit(Kind.KITCHEN,f,5));
        }
        set(l,b,-2,0,z,fit(Kind.KITCHEN,f,3));set(l,b,-2,1,z,fit(Kind.KITCHEN,f,4));
        set(l,b,SOURCE,fit(Kind.KITCHEN,Direction.EAST,6));
        set(l,b,-6,0,-5,fit(Kind.TABLE,f,0));
        set(l,b,-6,0,-4,fit(Kind.TABLE,Direction.NORTH,1));set(l,b,-6,0,-6,fit(Kind.TABLE,Direction.SOUTH,1));
        set(l,b,-7,0,-5,fit(Kind.TABLE,Direction.EAST,1));set(l,b,-5,0,-5,fit(Kind.TABLE,Direction.WEST,1));
        set(l,b,-6,2,-5,fit(Kind.FIXTURE,f,0));
        set(l,b,-1,2,-4,fit(Kind.FIXTURE,f,0));
    }
    /** The reader's room: the bed against the east wall, the nightstand with the clock radio, the desk with the tape deck in the corner,
     * the dresser, the closet's folding doors on the west wall, a low bookcase; posters on the walls. */
    static void room(ServerLevel l,BlockPos b){
        set(l,b,BED_HEAD,fit(Kind.BED,Direction.WEST,0));set(l,b,BED_FOOT,fit(Kind.BED,Direction.WEST,1));
        set(l,b,11,0,-7,fit(Kind.ROOM,Direction.WEST,2));
        set(l,b,ENDING,fit(Kind.ROOM,Direction.NORTH,1));
        set(l,b,8,0,-2,fit(Kind.ROOM,Direction.NORTH,0));
        set(l,b,ROOM_W,0,-4,fit(Kind.ROOM,Direction.EAST,3));set(l,b,ROOM_W,1,-4,fit(Kind.ROOM,Direction.EAST,3));
        set(l,b,ROOM_W,0,-5,fit(Kind.ROOM,Direction.EAST,3));set(l,b,ROOM_W,1,-5,fit(Kind.ROOM,Direction.EAST,3));
        set(l,b,ROOM_W,0,-7,fit(Kind.ROOM,Direction.EAST,4));
        set(l,b,11,1,-4,fit(Kind.POSTER,Direction.WEST,0));
        set(l,b,9,1,-2,fit(Kind.POSTER,Direction.NORTH,1));
        set(l,b,7,1,FRONT_WALL_Z+1,fit(Kind.POSTER,Direction.SOUTH,3));
        set(l,b,ROOM_W,1,-3,fit(Kind.POSTER,Direction.EAST,5));
        set(l,b,6,2,-5,fit(Kind.FIXTURE,Direction.NORTH,3));
    }
    static void hall(ServerLevel l,BlockPos b){
        set(l,b,-6,2,HALL_N,fit(Kind.FIXTURE,Direction.NORTH,1));set(l,b,6,2,HALL_S,fit(Kind.FIXTURE,Direction.NORTH,1));
        set(l,b,-1,1,HALL_S,fit(Kind.POSTER,Direction.NORTH,4));
    }
    static void parents(ServerLevel l,BlockPos b){
        set(l,b,-11,0,-14,fit(Kind.BED,Direction.EAST,0));set(l,b,-10,0,-14,fit(Kind.BED,Direction.EAST,1));
        set(l,b,-11,0,-15,fit(Kind.BED,Direction.EAST,0));set(l,b,-10,0,-15,fit(Kind.BED,Direction.EAST,1));
        set(l,b,-11,0,-16,fit(Kind.ROOM,Direction.EAST,2));set(l,b,-11,0,-12,fit(Kind.ROOM,Direction.EAST,2));
        set(l,b,-7,0,-16,fit(Kind.ROOM,Direction.SOUTH,0));
        set(l,b,-6,1,-12,fit(Kind.POSTER,Direction.WEST,4));
    }
    static void bath(ServerLevel l,BlockPos b){
        set(l,b,BATH_W,0,-16,fit(Kind.BATH,Direction.EAST,0));set(l,b,BATH_W+1,0,-16,fit(Kind.BATH,Direction.EAST,1));
        set(l,b,BATH_E,0,-16,fit(Kind.BATH,Direction.SOUTH,2));
        set(l,b,BATH_E,0,-13,fit(Kind.BATH,Direction.WEST,3));set(l,b,BATH_E,1,-13,fit(Kind.BATH,Direction.WEST,4));
        set(l,b,-3,2,-14,fit(Kind.FIXTURE,Direction.NORTH,3));
    }
    /** The front room: paneling, the couch along the hall wall facing the console television, the recliner, an end table and its lamp, the
     * coffee table; the picture window on the front. */
    static void living(ServerLevel l,BlockPos b){
        set(l,b,6,0,-12,fit(Kind.DEN,Direction.NORTH,0));set(l,b,7,0,-12,fit(Kind.DEN,Direction.NORTH,1));
        set(l,b,8,0,-12,fit(Kind.DEN,Direction.NORTH,4));
        set(l,b,6,0,-16,fit(Kind.DEN,Direction.SOUTH,3));
        set(l,b,10,0,-15,fit(Kind.DEN,Direction.WEST,2));
        set(l,b,6,0,-14,fit(Kind.DEN,Direction.NORTH,5));
        set(l,b,11,1,-12,fit(Kind.POSTER,Direction.WEST,4));
    }
    /**
     * A low roof of shingles over a house: from each long side up to a ridge along it, half a block up for every block in, in slabs; the
     * gable ends filled with siding. Where the shop stands against the back of the reader's house, the shop's wall goes up in place of the
     * roof's lowest courses.
     */
    static void roof(ServerLevel l,BlockPos b,int x0,int x1,int zs,int zn,boolean readers){
        var slab=TheListRegistry.SHINGLE_SLAB.get().defaultBlockState();
        int depth=zs-zn+1,half=(depth+1)/2;
        for(int k=0;k<=half;k++){
            int y=CEILING+1+k/2;SlabType type=k%2==0?SlabType.BOTTOM:SlabType.TOP;
            int s=zs+1-k,n=zn-1+k;if(s<n)break;
            for(int x=x0-1;x<=x1+1;x++){
                boolean gable=x==x0||x==x1;
                for(int z:s==n?new int[]{s}:new int[]{s,n}){
                    if(readers&&z==zs+1&&Math.abs(x)<=8)continue;
                    boolean overWall=z==zs||z==zn,eave=z==zs+1||z==zn-1;
                    // An upper slab over a wall or in a gable would leave the half under it open to the outside: a full course there.
                    BlockState st=type==SlabType.TOP&&gable&&!eave?mat("siding"):type==SlabType.TOP&&overWall?slab.setValue(SlabBlock.TYPE,SlabType.DOUBLE):slab.setValue(SlabBlock.TYPE,type);
                    set(l,b,x,y,z,st);
                    if(gable&&!eave)for(int yy=CEILING+1;yy<y;yy++)set(l,b,x,yy,z,mat("siding"));
                }
            }
            if(s==n||s-1<n+1)continue;
            for(int z=n+1;z<s;z++){for(int yy=CEILING+1;yy<y;yy++){set(l,b,x0,yy,z,mat("siding"));set(l,b,x1,yy,z,mat("siding"));}}
        }
        if(!readers)return;
        // The swamp cooler on the back slope where a course is a full step, its duct down into the roof; the aerial on the other side.
        int coolY=CEILING+1+3/2;
        set(l,b,-4,coolY+1,zs+1-3,fit(Kind.ROOFTOP,Direction.SOUTH,0));
        set(l,b,6,CEILING+1+5/2+1,zn-1+5,fit(Kind.ROOFTOP,Direction.NORTH,2));set(l,b,6,CEILING+1+5/2+2,zn-1+5,fit(Kind.ROOFTOP,Direction.NORTH,2));
        set(l,b,-9,CEILING+1+3/2+1,zn-1+3,fit(Kind.ROOFTOP,Direction.NORTH,3));
    }
    /** The carport on the west side: posts at its corners, its roof at the ceiling's height, the concrete under it out to the street. */
    static void carport(ServerLevel l,BlockPos b){
        ground(l,b,HOUSE_W-7,-3,HOUSE_W-1,NINTH_S+1,mat("slab"));
        for(int x=HOUSE_W-7;x<=HOUSE_W-1;x++)for(int z=-16;z>=-4;z--)set(l,b,x,2,z,fit(Kind.YARD,Direction.NORTH,7));
        for(int z:new int[]{-4,-10,-16}){set(l,b,HOUSE_W-7,0,z,fit(Kind.YARD,Direction.NORTH,6));set(l,b,HOUSE_W-7,1,z,fit(Kind.YARD,Direction.NORTH,6));}
    }

    // ------------------------------------------------------------------------------------------------ the yards
    /** The front yard: the walk from the stoop to the street, the porch light by the front door, the mailbox at the curb. The back yard:
     * chain-link round it, the clothesline, the burn barrel, the faucet on the back wall. */
    static void yard(ServerLevel l,BlockPos b){
        lawn(l,b,HOUSE_W-8,HOUSE_N-1,HOUSE_E+4,YARD_N);
        lawn(l,b,-22,HOUSE_S+1,22,YARD_S);
        ground(l,b,FRONT_DOOR.getX(),HOUSE_N-1,FRONT_DOOR.getX(),YARD_N,mat("sidewalk"));
        set(l,b,FRONT_DOOR.getX()+1,1,HOUSE_N-1,fit(Kind.YARD,Direction.NORTH,0));
        set(l,b,8,0,YARD_N,fit(Kind.MAILBOX,Direction.NORTH,0));
        fenceRun(l,b,-22,HOUSE_S+1,-22,YARD_S,Direction.EAST);
        fenceRun(l,b,22,HOUSE_S+1,22,YARD_S,Direction.EAST);
        fenceRun(l,b,-22,YARD_S,22,YARD_S,Direction.NORTH);
        set(l,b,-22,0,4,fit(Kind.FENCE,Direction.EAST,2));
        // The clothesline: two posts and the lines between them at a reader's head.
        set(l,b,-15,0,6,fit(Kind.YARD,Direction.NORTH,1));set(l,b,-15,1,6,fit(Kind.YARD,Direction.NORTH,1));
        set(l,b,-15,0,16,fit(Kind.YARD,Direction.NORTH,1));set(l,b,-15,1,16,fit(Kind.YARD,Direction.NORTH,1));
        for(int z=7;z<=15;z++)set(l,b,-15,1,z,fit(Kind.YARD,Direction.EAST,2));
        set(l,b,15,0,19,fit(Kind.YARD,Direction.NORTH,3));
        set(l,b,10,0,HOUSE_S+1,fit(Kind.YARD,Direction.SOUTH,4));
        set(l,b,-10,1,HOUSE_S+1,fit(Kind.YARD,Direction.SOUTH,0));
        carport(l,b);
    }
    /** The House's own hall stands behind the kitchen as the metal shop in the back yard: painted steel round it and over it, a roll-up
     * door and a window in its side, a light over the door; its front rises over the house's roof. */
    static void shop(ServerLevel l,BlockPos b){
        int w=-8,e=8,s=18,top=8;
        // Its walls: the panels rusted along the foot where the yard's water stands; galvanized trim up each corner and round the top.
        for(int z=1;z<=s;z++)for(int y=-1;y<=top;y++){set(l,b,w,y,z,shopWall(w,y,z,top,z==s));set(l,b,e,y,z,shopWall(e,y,z,top,z==s));}
        for(int x=w;x<=e;x++){for(int y=-1;y<=top;y++)set(l,b,x,y,s,shopWall(x,y,s,top,x==w||x==e));for(int z=1;z<s;z++)if(x>w&&x<e)set(l,b,x,top,z,mat("flat_roof"));}
        for(int x=w;x<=e;x++)for(int y=CEILING+1;y<=top;y++)set(l,b,x,y,0,y==top||x==w||x==e?mat("galvanized"):mat("shop_metal"));
        for(int z=6;z<=8;z++){set(l,b,e,0,z,fit(Kind.SHOP,Direction.EAST,0));set(l,b,e,1,z,fit(Kind.SHOP,Direction.EAST,0));set(l,b,e,2,z,fit(Kind.SHOP,Direction.EAST,1));}
        set(l,b,e+1,3,7,fit(Kind.SHOP,Direction.EAST,3));
        set(l,b,e,1,13,fit(Kind.SHOP,Direction.EAST,2));set(l,b,w,1,10,fit(Kind.SHOP,Direction.WEST,2));
        ground(l,b,e+1,6,e+4,8,mat("slab"));
    }

    /** One cell of the shop's wall: trim at a corner and along the top, the rust along the foot, the painted panels between. */
    static BlockState shopWall(int x,int y,int z,int top,boolean corner){
        if(corner||y==top)return mat("galvanized");
        return y<=0?mat("rusted"):mat("shop_metal");
    }

    // ------------------------------------------------------------------------------------------------ the other houses
    /** The houses along both streets, each its own: walls, windows dark or lit, a front door, a stoop, a porch light on or off, a low roof,
     * a mailbox at the curb, the walk up to the door; some with a carport, some with chain-link along the front. Shut for now. The one across from the reader's,
     * dark, with nobody's car in its drive and no light on its porch, has the way on behind its front door. */
    static void neighbors(ServerLevel l,BlockPos b){
        neighbor(l,b,-46,-30,-8,-18,Direction.NORTH,1,true,false);
        neighbor(l,b,-42,-26,-37,-47,Direction.SOUTH,2,false,true);
        neighbor(l,b,-8,8,-37,-47,Direction.SOUTH,3,false,false);
        neighbor(l,b,-42,-26,-52,-62,Direction.NORTH,4,true,true);
        neighbor(l,b,-8,8,-52,-62,Direction.NORTH,5,false,false);
        neighbor(l,b,-42,-26,-78,-88,Direction.SOUTH,6,true,false);
    }
    /** One house from x0 to x1 and z0 to z1 (z0 its front if it faces south, its back if it faces north), facing the street. */
    static void neighbor(ServerLevel l,BlockPos b,int x0,int x1,int za,int zb,Direction faces,int seed,boolean carport,boolean fence){
        int zs=Math.max(za,zb),zn=Math.min(za,zb);boolean empty=seed==3;
        // Each its own: lap siding over a brick course, face brick to the eaves, or stucco.
        BlockState foot=seed%3==1?mat("wainscot"):seed%3==2?mat("storefront"):mat("stucco"),walls=seed%3==1?mat("siding"):seed%3==2?mat("storefront"):mat("stucco");
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            boolean outer=x==x0||x==x1||z==zn||z==zs;
            set(l,b,x,-1,z,mat("slab"));
            for(int y=0;y<CEILING;y++)set(l,b,x,y,z,outer?(y==0?foot:walls):AIR);
            set(l,b,x,CEILING,z,outer?walls:mat("ceiling"));
        }
        roof(l,b,x0,x1,zs,zn,false);
        int frontZ=faces==Direction.SOUTH?zs:zn,mid=(x0+x1)/2,step=faces==Direction.SOUTH?1:-1;
        BlockPos door=new BlockPos(mid,0,frontZ);
        // A neighbor's front door shuts in the wall's outer face, as their windows do, so no reveal shows to the street.
        if(empty){door=ONWARD;}else door(l,b,door,faces.getOpposite(),seed%2!=0);
        if(!empty&&seed%3!=0)set(l,b,door.getX()+1,1,frontZ+step,fit(Kind.YARD,faces,0));
        for(int x=x0+2;x<=x1-2;x+=3){
            if(Math.abs(x-mid)<2)continue;
            boolean lit=!empty&&hash(x,frontZ,seed)<.35;
            set(l,b,x,1,frontZ,fit(Kind.WINDOW,faces,lit?1:hash(x,frontZ,seed+1)<.5?0:3));
            boolean litBack=!empty&&hash(x,frontZ,seed+2)<.25;
            set(l,b,x,1,faces==Direction.SOUTH?zn:zs,fit(Kind.WINDOW,faces.getOpposite(),litBack?1:0));
        }
        for(int z=zn+2;z<=zs-2;z+=4){set(l,b,x0,1,z,fit(Kind.WINDOW,Direction.WEST,0));set(l,b,x1,1,z,fit(Kind.WINDOW,Direction.EAST,hash(x1,z,seed)<.3&&!empty?1:0));}
        // The walk from the door to the street, flush with the floor so nothing lifts a body into the lintel, and the mailbox at the curb.
        int curb=faces==Direction.SOUTH?NINTH_N-1:NINTH_S+1;
        if(zn<=-50&&zn>-70)curb=SEVENTH_S+1;else if(zn<=-70)curb=SEVENTH_N-1;
        for(int z=frontZ+step;z!=curb+step;z+=step){set(l,b,door.getX(),-1,z,mat("sidewalk"));set(l,b,door.getX(),0,z,AIR);}
        set(l,b,door.getX()+3,0,curb,fit(Kind.MAILBOX,faces,0));
        // The carport on the side away from the utility pole, its slab from the back of its roof out to the curb.
        boolean west=seed==6;int cx=west?x0-6:x1+1;
        if(carport){
            for(int x=cx;x<=cx+5;x++)for(int z=zn+1;z<=zs-1;z++)set(l,b,x,2,z,fit(Kind.YARD,Direction.NORTH,7));
            int post=west?cx:cx+5;
            for(int z:new int[]{zn+1,zs-1}){set(l,b,post,0,z,fit(Kind.YARD,Direction.NORTH,6));set(l,b,post,1,z,fit(Kind.YARD,Direction.NORTH,6));}
            int far=curb<zn?zs-1:zn+1;
            for(int x=cx;x<=cx+5;x++)for(int z=Math.min(far,curb);z<=Math.max(far,curb);z++){
                if(x==post&&(z==zn+1||z==zs-1))continue;
                set(l,b,x,-1,z,mat("slab"));set(l,b,x,0,z,AIR);
            }
        }
        if(fence){
            // Chain-link across the front yard, short of the carport's drive, a gate at the walk.
            int fz=frontZ+step*Math.min(4,Math.abs(curb-frontZ)-1);
            int fw=carport&&west?x0:x0-2,fe=carport&&!west?x1:x1+2;
            for(int x=fw;x<=fe;x++)if(x!=door.getX())set(l,b,x,0,fz,fit(Kind.FENCE,Direction.NORTH,Math.floorMod(x,4)==0?1:0));
            set(l,b,door.getX(),0,fz,fit(Kind.FENCE,Direction.NORTH,2));
        }
    }

    // ------------------------------------------------------------------------------------------------ the school's field
    /**
     * 0.4.713, the sprinkler line down the home sideline (TheListLine): its cap by the north end zone, lengths of pipe coupled end to
     * end, the four risers off it, the elbow level with the end of the home bench, the hose and the valve past the bench, shut. Laid as
     * it was built whenever it is laid: the valve shut and the risers clean. Only where its ground is loaded.
     */
    /** 0.4.714, the fryer as it was built: off, the oil cold and clean. Only where its ground is loaded. */
    static void fryer(ServerLevel l,BlockPos b){
        if(l.isLoaded(b.offset(TheListFryer.AT)))set(l,b,TheListFryer.AT,fit(Kind.FRYER,TheListFryer.FACING,TheListFryer.OFF));
    }
    static void line(ServerLevel l,BlockPos b){
        for(int z=TheListLine.N;z<=TheListLine.S;z++){
            var rel=new BlockPos(TheListLine.X,0,z);
            int stage=z==TheListLine.N?TheListLine.CAP:z==TheListLine.S?TheListLine.ELBOW:TheListLine.riserAt(rel)>=0?TheListLine.RISER:TheListLine.PIPE;
            if(l.isLoaded(b.offset(rel)))set(l,b,rel,fit(Kind.LINE,Direction.NORTH,stage));
        }
        if(l.isLoaded(b.offset(TheListLine.HOSE)))set(l,b,TheListLine.HOSE,fit(Kind.LINE,Direction.EAST,TheListLine.HOSE_RUN));
        if(l.isLoaded(b.offset(TheListLine.VALVE)))set(l,b,TheListLine.VALVE,fit(Kind.LINE,Direction.NORTH,TheListLine.SHUT));
    }
    /** Whether a cell inside the stadium is on the track, inside it, or out past it: its distance from the line down the infield's
     * middle, between the two centres of the track's turns. The track is the ring from just over 19 out to 21. */
    static double fromInfield(int x,int z){double dz=Math.max(0,Math.abs(z-FIELD_Z)-TURN);double dx=x-FIELD_X;return Math.sqrt(dx*dx+dz*dz);}
    static final int TURN=17;
    static boolean onTrack(int x,int z){double d=fromInfield(x,z);return d>19&&d<=21;}
    /** The field's own turf: the sidelines, the end zones, a line every five, and the turf mown in bands between them. */
    static BlockState fieldAt(int x,int z){
        if(x==FIELD_W||x==FIELD_E)return mat("turf_side");
        if(z<=FIELD_N+4||z>=FIELD_S-4)return mat("endzone");
        int from=z-(FIELD_N+5);
        if(from%5==0)return mat("turf_line");
        return (from/5)%2==0?mat("turf"):mat("turf_dark");
    }
    /**
     * The high school's field west of town, out where South Seventh's caliche ends at its gate, the lot beside the road. Chain-link round
     * all of it. Inside, the grass worn to dirt; the cinder track round the field; the field lined and mown, its end zones and its goalposts;
     * the home stands on the west with the press box on top of them, the visitors' stands on the east, the team benches at the sidelines;
     * four towers of lights, dark; the scoreboard past the south end; the concession stand and the ticket booth by the gate; the field house
     * on Main Street at the north end, its doors onto the field.
     */
    static void school(ServerLevel l,BlockPos b){
        lawn(l,b,FENCE_W,MAIN_FRONT_S,FENCE_E,FENCE_N-1);
        ground(l,b,FENCE_E+1,SEVENTH_S,STREETS_W-1,SEVENTH_N,mat("caliche"));
        ground(l,b,FENCE_E+2,SEVENTH_N-2,STREETS_W-4,SEVENTH_N-16,mat("caliche"));
        streetLight(l,b,-80,SEVENTH_N-9,Direction.NORTH);
        for(int x=FENCE_W+1;x<FENCE_E;x++)for(int z=FENCE_N+1;z<FENCE_S;z++){
            double d=fromInfield(x,z);
            BlockState s=d>19&&d<=21?mat("track_cinder"):x>=FIELD_W&&x<=FIELD_E&&z>=FIELD_N&&z<=FIELD_S?fieldAt(x,z):d<=19?mat("turf"):hash(x,z,61)<.16?mat("dirt"):mat("lawn");
            set(l,b,x,-1,z,s);set(l,b,x,0,z,AIR);
        }
        // The fence: the north side broken where the field house's doors open onto it, the east side where the road comes in.
        for(int x=FENCE_W;x<=FENCE_E;x++){
            if(x<FIELD_X-2||x>FIELD_X+1)set(l,b,x,0,FENCE_N,fit(Kind.FENCE,Direction.NORTH,Math.floorMod(x,4)==0?1:0));
            set(l,b,x,0,FENCE_S,fit(Kind.FENCE,Direction.NORTH,Math.floorMod(x,4)==0?1:0));
        }
        for(int z=FENCE_N+1;z<FENCE_S;z++){
            set(l,b,FENCE_W,0,z,fit(Kind.FENCE,Direction.EAST,Math.floorMod(z,4)==0?1:0));
            if(z<GATE_N||z>GATE_S)set(l,b,FENCE_E,0,z,fit(Kind.FENCE,Direction.EAST,Math.floorMod(z,4)==0?1:0));
        }
        set(l,b,FENCE_E,0,GATE_N-1,fit(Kind.FENCE,Direction.EAST,1));set(l,b,FENCE_E,0,GATE_S+1,fit(Kind.FENCE,Direction.EAST,1));
        // The gaps are open ground: the road's caliche on through the gate, grass where the field house's doors come out.
        ground(l,b,FENCE_E,GATE_N,FENCE_E,GATE_S,mat("caliche"));
        lawn(l,b,FIELD_X-2,FENCE_N,FIELD_X+1,FENCE_N);
        goalpost(l,b,FIELD_N,Direction.SOUTH);
        goalpost(l,b,FIELD_S,Direction.NORTH);
        homeStands(l,b);
        visitorStands(l,b);
        // The team benches at the sidelines, the home team's on the west.
        for(int z=FIELD_Z-10;z<=FIELD_Z+10;z++){if(z==FIELD_Z)continue;set(l,b,FIELD_W-2,0,z,fit(Kind.STANDS,Direction.EAST,4));set(l,b,FIELD_E+2,0,z,fit(Kind.STANDS,Direction.WEST,4));}
        // The towers of lights at the corners, their banks aimed in at the field, all of them dark tonight.
        for(int[] t:new int[][]{{FIELD_X-24,FIELD_N-2},{FIELD_X-24,FIELD_S+2},{FIELD_X+24,FIELD_N-2},{FIELD_X+24,FIELD_S+2}}){
            for(int y=0;y<16;y++)set(l,b,t[0],y,t[1],fit(Kind.FIELD,Direction.NORTH,6));
            set(l,b,t[0],16,t[1],fit(Kind.FIELD,t[0]<FIELD_X?Direction.EAST:Direction.WEST,7));
        }
        scoreboard(l,b);
        concession(l,b);
        ticketBooth(l,b);
        line(l,b);
        fieldHouse(l,b);
    }
    /** A goalpost at one end of the field: its padded foot behind the end line, its pole up to the gooseneck, which bends out over the line
     * to the crossbar's middle; the bar across, and an upright rising from each of its ends. */
    static void goalpost(ServerLevel l,BlockPos b,int endZ,Direction in){
        int baseZ=endZ-in.getStepZ();
        set(l,b,FIELD_X,0,baseZ,fit(Kind.FIELD,Direction.NORTH,0));
        for(int y=1;y<=2;y++)set(l,b,FIELD_X,y,baseZ,fit(Kind.FIELD,Direction.NORTH,1));
        set(l,b,FIELD_X,3,baseZ,fit(Kind.FIELD,in,2));
        set(l,b,FIELD_X,3,endZ,fit(Kind.FIELD,in,3));
        set(l,b,FIELD_X-2,3,endZ,fit(Kind.FIELD,Direction.NORTH,4));set(l,b,FIELD_X+2,3,endZ,fit(Kind.FIELD,Direction.SOUTH,4));
        for(int y=4;y<=8;y++){set(l,b,FIELD_X-2,y,endZ,fit(Kind.FIELD,Direction.NORTH,5));set(l,b,FIELD_X+2,y,endZ,fit(Kind.FIELD,Direction.NORTH,5));}
    }
    /** The home stands on the west of the track: a concrete walk at their foot, eight rows rising half a block apiece, the frame under them,
     * a rail along the top; the press box on its posts behind the top row, its windows on the field, its door off the top row, the horn on
     * its roof. Under the high rows a body can walk. */
    static void homeStands(ServerLevel l,BlockPos b){
        int front=FIELD_X-22,zn=FIELD_Z-16,zs=FIELD_Z+16;
        for(int z=zn;z<=zs;z++){
            set(l,b,front,-1,z,mat("sidewalk"));
            for(int r=0;r<8;r++){
                int x=front-1-r,y=r/2;
                set(l,b,x,-1,z,mat("dirt"));
                set(l,b,x,y,z,fit(Kind.STANDS,Direction.EAST,r%2));
                if(y>=1&&Math.floorMod(z,4)==0)for(int yy=0;yy<y;yy++)set(l,b,x,yy,z,fit(Kind.STANDS,Direction.EAST,2));
            }
            if(z!=PRESS_DOOR_Z)set(l,b,front-8,4,z,fit(Kind.STANDS,Direction.EAST,3));
        }
        // The press box: a floor on four posts level with the top row, walls of plywood, its windows along the field side.
        int pe=front-9,pw=pe-4,pn=FIELD_Z-6,ps=FIELD_Z+6;
        for(int x=pw;x<=pe;x++)for(int z=pn;z<=ps;z++){
            set(l,b,x,3,z,mat("slab"));
            boolean wall=x==pw||x==pe||z==pn||z==ps;
            for(int y=4;y<=6;y++)set(l,b,x,y,z,wall?mat("siding"):AIR);
        }
        flatRoof(l,b,pw,pe,pn,ps,7,mat("siding"));
        for(int[] c:new int[][]{{pw,pn},{pw,ps},{pe,pn},{pe,ps}})for(int y=0;y<3;y++)set(l,b,c[0],y,c[1],fit(Kind.YARD,Direction.NORTH,6));
        for(int z=pn+1;z<=ps-2;z++){set(l,b,pe,5,z,fit(Kind.STANDS,Direction.EAST,5));set(l,b,pe,6,z,fit(Kind.STANDS,Direction.EAST,5));}
        // The table under the windows where the announcer and the two spotters sit, each with a microphone.
        for(int z:new int[]{FIELD_Z-4,FIELD_Z,FIELD_Z+4})set(l,b,pe-1,4,z,fit(Kind.STANDS,Direction.EAST,6));
        steelDoor(l,b,new BlockPos(pe,4,PRESS_DOOR_Z),Direction.WEST,false);
        set(l,b,pw+2,9,FIELD_Z,fit(Kind.STANDS,Direction.EAST,7));
    }
    /** The visitors' stands, four rows on the east of the track with a rail along the top. */
    static void visitorStands(ServerLevel l,BlockPos b){
        int front=FIELD_X+23;
        for(int z=FIELD_Z-8;z<=FIELD_Z+12;z++){
            for(int r=0;r<4;r++){int x=front+r;set(l,b,x,-1,z,mat("dirt"));set(l,b,x,r/2,z,fit(Kind.STANDS,Direction.WEST,r%2));
                if(r>=2&&Math.floorMod(z,4)==0)set(l,b,x,0,z,fit(Kind.STANDS,Direction.WEST,2));}
            set(l,b,front+3,2,z,fit(Kind.STANDS,Direction.WEST,3));
        }
    }
    /** The scoreboard past the south end of the field on its two posts, read from the field: HOME, the quarter and VISITORS across the top,
     * the down, the yards to go and the ball's place under them, every bulb dark. */
    static void scoreboard(ServerLevel l,BlockPos b){
        int z=FENCE_S-2;
        for(int y=0;y<=6;y++){set(l,b,FIELD_X-2,y,z,fit(Kind.SCORE,Direction.NORTH,6));set(l,b,FIELD_X+2,y,z,fit(Kind.SCORE,Direction.NORTH,6));}
        // Read from the field: the viewer's left is the east.
        for(int i=0;i<3;i++){set(l,b,FIELD_X+1-i,6,z,fit(Kind.SCORE,Direction.NORTH,i));set(l,b,FIELD_X+1-i,5,z,fit(Kind.SCORE,Direction.NORTH,3+i));}
    }
    /** The concession stand inside the gate: cinder block under a flat roof, its shutter down over the counter and its prices beside it. */
    static void concession(ServerLevel l,BlockPos b){
        int x0=FENCE_E-6,x1=FENCE_E-2,zn=GATE_N-13,zs=GATE_N-8;
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            set(l,b,x,-1,z,mat("slab"));
            boolean wall=x==x0||x==x1||z==zn||z==zs;
            for(int y=0;y<3;y++)set(l,b,x,y,z,wall?mat("cinder"):AIR);
        }
        flatRoof(l,b,x0,x1,zn,zs,3,mat("cinder"));
        for(int x=x0+1;x<=x0+2;x++)set(l,b,x,1,zs+1,fit(Kind.LOCKER,Direction.SOUTH,6));
        set(l,b,x0+3,1,zs+1,fit(Kind.LOCKER,Direction.SOUTH,7));
        set(l,b,x0+2,2,zs+1,fit(Kind.YARD,Direction.SOUTH,0));
    }
    /** The ticket booth outside the gate, its window on the road. */
    static void ticketBooth(ServerLevel l,BlockPos b){
        int x0=FENCE_E+1,x1=FENCE_E+3,zn=SEVENTH_S+1,zs=SEVENTH_S+3;
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            set(l,b,x,-1,z,mat("slab"));
            boolean wall=x==x0||x==x1||z==zn||z==zs;
            for(int y=0;y<3;y++)set(l,b,x,y,z,wall?mat("cinder"):AIR);
        }
        flatRoof(l,b,x0,x1,zn,zs,3,mat("cinder"));
        set(l,b,x0+1,1,zn,fit(Kind.SCORE,Direction.NORTH,7));
    }
    /**
     * The field house on Main Street, its front on the sidewalk with the stores and its back on the field: cinder block under a flat roof,
     * a steel door off the street and two onto the field with a light over them. The locker room with its lockers along two walls and
     * benches down the middle; through the wall, the coach's office with its desk and chalkboard, and the showers behind it. One light
     * left on in the locker room.
     */
    static void fieldHouse(ServerLevel l,BlockPos b){
        int x0=FIELD_X-12,x1=FIELD_X+12,zn=MAIN_FRONT_S,zs=FIELD_HOUSE_S,wall=FIELD_X+3;
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            boolean outer=x==x0||x==x1||z==zn||z==zs,shower=x>wall&&z>=zs-2;
            set(l,b,x,-1,z,outer?mat("slab"):shower?mat("tile"):mat("slab"));
            for(int y=0;y<3;y++)set(l,b,x,y,z,outer?mat("cinder"):AIR);
        }
        flatRoof(l,b,x0,x1,zn,zs,3,mat("cinder"));
        // The wall between the locker room and the coach's side, open to each; the wall between the office and the showers.
        for(int z=zn+1;z<zs;z++)for(int y=0;y<3;y++)set(l,b,wall,y,z,y<2&&(z==zn+3||z==zs-2)?AIR:mat("cinder"));
        for(int x=wall+1;x<x1;x++)for(int y=0;y<3;y++)set(l,b,x,y,zs-3,mat("cinder"));
        steelDoor(l,b,new BlockPos(FIELD_X-6,0,zn),Direction.SOUTH,false);
        set(l,b,FIELD_X-5,2,zn-1,fit(Kind.YARD,Direction.NORTH,0));
        steelDoor(l,b,new BlockPos(FIELD_X-1,0,zs),Direction.NORTH,false);steelDoor(l,b,new BlockPos(FIELD_X,0,zs),Direction.NORTH,true);
        set(l,b,FIELD_X+2,2,zs+1,fit(Kind.YARD,Direction.SOUTH,0));
        // The lockers: along the north wall either side of the street door, and down the west wall.
        for(int x=x0+1;x<wall;x++){if(Math.abs(x-(FIELD_X-6))<=1)continue;set(l,b,x,0,zn+1,fit(Kind.LOCKER,Direction.SOUTH,0));set(l,b,x,1,zn+1,fit(Kind.LOCKER,Direction.SOUTH,1));}
        for(int z=zn+2;z<zs;z++){set(l,b,x0+1,0,z,fit(Kind.LOCKER,Direction.EAST,0));set(l,b,x0+1,1,z,fit(Kind.LOCKER,Direction.EAST,1));}
        for(int x=x0+3;x<wall-1;x++)if(Math.abs(x-(FIELD_X-6))>1)set(l,b,x,0,zn+4,fit(Kind.LOCKER,Direction.NORTH,2));
        set(l,b,FIELD_X-4,2,zn+5,fit(Kind.FIXTURE,Direction.NORTH,2));
        // The coach's office, the showers behind it.
        set(l,b,wall+5,0,zn+2,fit(Kind.LOCKER,Direction.SOUTH,3));set(l,b,wall+5,0,zn+3,fit(Kind.TABLE,Direction.NORTH,1));
        set(l,b,wall+3,1,zn+1,fit(Kind.LOCKER,Direction.SOUTH,4));set(l,b,wall+2,1,zn+1,fit(Kind.LOCKER,Direction.SOUTH,4));
        for(int x=wall+2;x<x1;x+=2)set(l,b,x,1,zs-2,fit(Kind.LOCKER,Direction.SOUTH,5));
    }
    /** A flat roof over a box from x0 to x1 and zn to zs: a ceiling inside at {@code y}, the gravel a course above it, and the walls carried up
     * through both so no edge of gravel shows from the ground. */
    static void flatRoof(ServerLevel l,BlockPos b,int x0,int x1,int zn,int zs,int y,BlockState wall){
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            boolean edge=x==x0||x==x1||z==zn||z==zs;
            set(l,b,x,y,z,edge?wall:mat("ceiling"));set(l,b,x,y+1,z,edge?wall:mat("flat_roof"));
        }
    }
    /** One of the field house's steel doors, shut in the outer face of its wall like the stores' doors. */
    static void steelDoor(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean right){
        var d=TheListRegistry.STEEL_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,right?DoorHingeSide.RIGHT:DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    // ------------------------------------------------------------------------------------------------ Amber's house
    /**
     * Amber's house on South Seventh, built as the reader's is, double-walled, with face brick to the eaves outside. Through the front door
     * a short hall; the den on the west with the television and the VCR, the shelf of rented tapes, the couch and the recliner, the picture
     * window; Amber's room on the east with her desk under her own window, the notebook on it, her bed, her bookcase and her posters; behind
     * them the kitchen, her mother's room and the bathroom; the back door onto the alley. The television is on in the den, her lamp on her
     * desk; the rest is dark. Shut for now: the front door is locked.
     */
    static void amberHouse(ServerLevel l,BlockPos b){
        int w=AMBER_W,e=AMBER_E,s=AMBER_S,n=AMBER_N,mid=n+5;
        for(int x=w;x<=e;x++)for(int z=n;z<=s;z++){
            boolean outer=x==w||x==e||z==n||z==s,lining=!outer&&(x==w+1||x==e-1||z==n+1||z==s-1);
            boolean kitchen=x<=-3&&z<mid,bath=x>=9&&z<mid,den=x<=-3&&z>mid;
            set(l,b,x,-1,z,outer||lining?mat("slab"):kitchen||bath?mat("lino"):mat("carpet"));
            for(int y=0;y<CEILING;y++)set(l,b,x,y,z,outer?mat("storefront"):lining?(den||x==w+1&&z>mid?mat("paneling"):mat("drywall")):AIR);
            set(l,b,x,CEILING,z,outer?mat("storefront"):mat("ceiling"));
        }
        // The walls between the rooms: across the middle, either side of the hall, and between her mother's room and the bathroom.
        for(int x=w+2;x<=e-2;x++)for(int y=0;y<CEILING;y++)set(l,b,x,y,mid,mat("drywall"));
        for(int z=n+2;z<=s-2;z++)for(int y=0;y<CEILING;y++){set(l,b,-2,y,z,z>mid?mat("paneling"):mat("drywall"));set(l,b,2,y,z,mat("drywall"));}
        for(int z=n+2;z<mid;z++)for(int y=0;y<CEILING;y++)set(l,b,8,y,z,mat("drywall"));
        for(int y=0;y<=1;y++){
            for(int x=-1;x<=1;x++)set(l,b,x,y,mid,AIR);
            set(l,b,-6,y,mid,AIR);set(l,b,-7,y,mid,AIR);
            set(l,b,-2,y,s-2,AIR);set(l,b,-2,y,s-3,AIR);set(l,b,-2,y,n+3,AIR);
            set(l,b,0,y,s-1,AIR);set(l,b,0,y,n+1,AIR);
        }
        door(l,b,AMBER_FRONT,Direction.NORTH,true);
        door(l,b,AMBER_BACK,Direction.SOUTH,false);
        door(l,b,new BlockPos(2,0,s-4),Direction.EAST,false);
        door(l,b,new BlockPos(2,0,n+4),Direction.EAST,true);
        door(l,b,new BlockPos(8,0,n+3),Direction.EAST,false);
        // The den.
        set(l,b,AMBER_TV,fit(Kind.AMBER,Direction.EAST,0));
        set(l,b,w+2,0,s-6,fit(Kind.AMBER,Direction.EAST,1));set(l,b,w+2,1,s-6,fit(Kind.AMBER,Direction.EAST,1));
        set(l,b,w+2,0,s-5,fit(Kind.AMBER,Direction.EAST,1));
        set(l,b,w+3,0,s-2,fit(Kind.AMBER,Direction.NORTH,2));
        set(l,b,-3,0,s-4,fit(Kind.DEN,Direction.WEST,0));set(l,b,-3,0,s-5,fit(Kind.DEN,Direction.WEST,1));
        set(l,b,-3,0,s-6,fit(Kind.DEN,Direction.WEST,4));
        set(l,b,-6,0,s-4,fit(Kind.AMBER,Direction.EAST,7));
        set(l,b,-6,0,s-2,fit(Kind.DEN,Direction.WEST,2));
        set(l,b,w+2,1,s-3,fit(Kind.POSTER,Direction.EAST,4));
        set(l,b,-7,1,s,fit(Kind.WINDOW,Direction.SOUTH,5));set(l,b,-6,1,s,fit(Kind.WINDOW,Direction.SOUTH,4));
        set(l,b,-7,1,s-1,AIR);set(l,b,-6,1,s-1,AIR);
        set(l,b,w,1,s-2,fit(Kind.WINDOW,Direction.WEST,0));set(l,b,w+1,1,s-2,AIR);
        // Amber's room.
        set(l,b,AMBER_DESK,fit(Kind.AMBER,Direction.NORTH,3));set(l,b,AMBER_DESK.getX(),0,AMBER_DESK.getZ()-1,fit(Kind.TABLE,Direction.SOUTH,1));
        // Her window is two panes over the desk and beside it, so from Seventh she can be seen at it (0.4.707).
        for(int x=AMBER_DESK.getX();x<=AMBER_DESK.getX()+1;x++){set(l,b,x,1,s,fit(Kind.AMBER,Direction.SOUTH,6));set(l,b,x,1,s-1,AIR);}
        set(l,b,8,1,s,fit(Kind.WINDOW,Direction.SOUTH,0));set(l,b,8,1,s-1,AIR);
        set(l,b,e,1,s-4,fit(Kind.WINDOW,Direction.EAST,0));set(l,b,e-1,1,s-4,AIR);
        set(l,b,e-2,0,s-5,fit(Kind.BED,Direction.WEST,0));set(l,b,e-3,0,s-5,fit(Kind.BED,Direction.WEST,1));
        set(l,b,e-2,0,s-6,fit(Kind.AMBER,Direction.WEST,4));
        set(l,b,4,0,mid+1,fit(Kind.AMBER,Direction.SOUTH,5));set(l,b,5,0,mid+1,fit(Kind.AMBER,Direction.SOUTH,5));
        set(l,b,3,0,s-2,fit(Kind.ROOM,Direction.EAST,0));
        set(l,b,e-2,1,s-2,fit(Kind.POSTER,Direction.WEST,1));set(l,b,7,1,mid+1,fit(Kind.POSTER,Direction.SOUTH,0));set(l,b,3,1,s-5,fit(Kind.POSTER,Direction.EAST,3));
        // The kitchen.
        int kz=n+2;
        for(int x=w+2;x<=-3;x++){
            int st=x==w+2?3:x==-8?2:x==-6?1:x==-4?7:0;
            set(l,b,x,0,kz,fit(Kind.KITCHEN,Direction.SOUTH,st));
            if(st==3)set(l,b,x,1,kz,fit(Kind.KITCHEN,Direction.SOUTH,4));
            else if(x!=-8&&x!=-6)set(l,b,x,2,kz,fit(Kind.KITCHEN,Direction.SOUTH,5));
        }
        set(l,b,-6,1,n,fit(Kind.WINDOW,Direction.NORTH,2));set(l,b,-6,1,n+1,AIR);
        set(l,b,-4,0,mid-1,fit(Kind.TABLE,Direction.NORTH,0));set(l,b,-5,0,mid-1,fit(Kind.TABLE,Direction.EAST,1));set(l,b,-3,0,mid-1,fit(Kind.TABLE,Direction.WEST,1));
        set(l,b,w+2,1,mid-1,fit(Kind.KITCHEN,Direction.EAST,6));
        // Her mother's room; the bathroom.
        for(int x=5;x<=6;x++){set(l,b,x,0,n+2,fit(Kind.BED,Direction.SOUTH,0));set(l,b,x,0,n+3,fit(Kind.BED,Direction.SOUTH,1));}
        set(l,b,4,0,n+2,fit(Kind.ROOM,Direction.SOUTH,2));set(l,b,7,0,n+2,fit(Kind.ROOM,Direction.SOUTH,2));set(l,b,7,0,n+4,fit(Kind.ROOM,Direction.WEST,0));
        set(l,b,5,1,n,fit(Kind.WINDOW,Direction.NORTH,0));set(l,b,5,1,n+1,AIR);
        set(l,b,9,0,n+2,fit(Kind.BATH,Direction.EAST,0));set(l,b,10,0,n+2,fit(Kind.BATH,Direction.EAST,1));
        set(l,b,10,0,n+4,fit(Kind.BATH,Direction.WEST,2));
        set(l,b,9,0,n+4,fit(Kind.BATH,Direction.NORTH,3));set(l,b,9,1,n+4,fit(Kind.BATH,Direction.NORTH,4));
        set(l,b,10,1,n,fit(Kind.WINDOW,Direction.NORTH,3));set(l,b,10,1,n+1,AIR);
        roof(l,b,w,e,s,n,false);
        // Out front: the walk to the street, the porch light by the door, the mailbox at the curb, chain-link across the yard with its gate.
        for(int z=s+1;z<=SEVENTH_N-1;z++){set(l,b,0,-1,z,mat("sidewalk"));set(l,b,0,0,z,AIR);}
        set(l,b,1,1,s+1,fit(Kind.YARD,Direction.SOUTH,0));
        set(l,b,3,0,SEVENTH_N-1,fit(Kind.MAILBOX,Direction.SOUTH,0));
        for(int x=w-2;x<=e+2;x++)if(x!=0)set(l,b,x,0,s+2,fit(Kind.FENCE,Direction.NORTH,Math.floorMod(x,4)==0?1:0));
        set(l,b,0,0,s+2,fit(Kind.FENCE,Direction.NORTH,2));
        // Out back: the step down to the alley, her trash can.
        for(int z=n-1;z>=ALLEY_S+1;z--){set(l,b,0,-1,z,mat("sidewalk"));set(l,b,0,0,z,AIR);}
        // 0.4.701: her back gate stands open onto the alley, one step out from the back door, so the door has somewhere to open onto.
        set(l,b,0,0,AMBER_N-1,fit(Kind.FENCE,Direction.NORTH,3));
        set(l,b,2,0,ALLEY_S+1,fit(Kind.LOT,Direction.SOUTH,2));
    }

    // ------------------------------------------------------------------------------------------------ the yards
    /** What the neighbors leave out in their yards: sprinklers going on the lawns, a doghouse by a back fence, trash cans at the alley, a
     * bike down in the grass, lawn chairs, a grill, a hose reel by a wall, a kiddie pool gone green. */
    static void yards(ServerLevel l,BlockPos b){
        for(int[] p:SPRINKLERS)set(l,b,p[0],0,p[1],fit(Kind.LOT,Direction.NORTH,0));
        set(l,b,-38,0,-49,fit(Kind.LOT,Direction.SOUTH,1));set(l,b,-34,0,-89,fit(Kind.LOT,Direction.SOUTH,1));
        for(int x:new int[]{-29,-28})set(l,b,x,0,ALLEY_S+1,fit(Kind.LOT,Direction.SOUTH,2));
        set(l,b,-27,0,NINTH_N-1,fit(Kind.LOT,Direction.SOUTH,2));
        set(l,b,5,0,-65,fit(Kind.LOT,Direction.WEST,3));
        set(l,b,-40,0,-5,fit(Kind.LOT,Direction.SOUTH,4));set(l,b,-38,0,-5,fit(Kind.LOT,Direction.WEST,4));
        set(l,b,-34,0,-4,fit(Kind.LOT,Direction.NORTH,5));
        set(l,b,-43,0,-56,fit(Kind.LOT,Direction.WEST,6));
        set(l,b,-44,0,-3,fit(Kind.LOT,Direction.NORTH,7));
    }

    // ------------------------------------------------------------------------------------------------ Main Street
    /**
     * Main Street's stores, each a front onto the sidewalk with its name across the top: the feed store, low and long, with the bus bench
     * and the bus stop's sign at the curb; Dawson Drug in face brick with its display windows lit, an awning and a glass door; the video
     * store across the street, its name lit, its windows full of boxes; the sheriff's substation east of the highway with its star, its
     * flag on its pole and its bench. Street lights along both curbs.
     */
    static void main(ServerLevel l,BlockPos b){
        feedStore(l,b);
        drugstore(l,b);
        videoStore(l,b);
        substation(l,b);
        // Empty fronts between them: older brick with dark windows.
        for(int[] span:new int[][]{{-58,-46,0},{-2,14,0},{-44,-18,1},{4,14,1}})blankFront(l,b,span[0],span[1],span[2]==0);
        for(int x=-56;x<=56;x+=14){
            if(x<AVE_W-2||x>AVE_E+2)streetLight(l,b,x,WALK_S_N,Direction.NORTH);
            int xn=x+7;
            if((xn<AVE_W-2||xn>AVE_E+2)&&xn<TRACK_W-1)streetLight(l,b,xn,WALK_N_S,Direction.SOUTH);
        }
    }
    /** A store's shell between two walls on one side of Main Street: its front at the sidewalk, its back at the alley or the field; the
     * floor, the walls, the flat roof, the strip lights inside. */
    static void shell(ServerLevel l,BlockPos b,int x0,int x1,boolean south,BlockState front,BlockState walls,BlockState floor,int top){shell(l,b,x0,x1,south,front,walls,floor,top,true);}
    /** A store's box on Main Street; its strip lights only if it is open (a closed one is dark behind its glass). */
    static void shell(ServerLevel l,BlockPos b,int x0,int x1,boolean south,BlockState front,BlockState walls,BlockState floor,int top,boolean lights){
        int zf=south?MAIN_FRONT_S:MAIN_FRONT_N,zb=south?MAIN_BACK_S:MAIN_BACK_N,zs=Math.max(zf,zb),zn=Math.min(zf,zb);
        for(int x=x0;x<=x1;x++)for(int z=zn;z<=zs;z++){
            boolean wall=x==x0||x==x1||z==zn||z==zs;
            set(l,b,x,-1,z,floor);
            // The walls carried up through the roof's course, so no edge of gravel shows on a front or a side; the gravel only within.
            for(int y=0;y<=top;y++)set(l,b,x,y,z,y==top&&!wall?mat("flat_roof"):wall?(z==zf?front:walls):AIR);
            if(lights&&!wall&&(x-x0)%4==2&&(z-zn)%4==2)set(l,b,x,top-1,z,fit(Kind.FIXTURE,Direction.NORTH,2));
        }
        // A parapet along the front, a course above the roof.
        for(int x=x0;x<=x1;x++)set(l,b,x,top+1,zf,front);
    }
    /** A store's front: display windows lit or dark either side of its door, transoms over them, an awning out over the sidewalk, brick
     * pilasters at its ends, and its name in boards across the course above (the awning left off where the name hangs low in its place). */
    static void front(ServerLevel l,BlockPos b,int x0,int x1,boolean south,int doorX,boolean lit,boolean awning,Kind sign,int boards,int signY){
        int zf=south?MAIN_FRONT_S:MAIN_FRONT_N;Direction out=south?Direction.NORTH:Direction.SOUTH;int step=south?-1:1;
        for(int x=x0+1;x<x1;x++){
            if(Math.abs(x-doorX)<=0)continue;
            set(l,b,x,0,zf,fit(Kind.FRONT,out,lit?0:4));set(l,b,x,1,zf,fit(Kind.FRONT,out,lit?0:4));set(l,b,x,2,zf,fit(Kind.FRONT,out,1));
        }
        set(l,b,doorX,2,zf,fit(Kind.FRONT,out,1));
        glassDoor(l,b,new BlockPos(doorX,0,zf),out,true);
        set(l,b,x0,0,zf,fit(Kind.FRONT,out,5));set(l,b,x0,1,zf,fit(Kind.FRONT,out,5));set(l,b,x0,2,zf,fit(Kind.FRONT,out,5));
        set(l,b,x1,0,zf,fit(Kind.FRONT,out,5));set(l,b,x1,1,zf,fit(Kind.FRONT,out,5));set(l,b,x1,2,zf,fit(Kind.FRONT,out,5));
        if(awning)for(int x=x0+1;x<x1;x++){set(l,b,x,3,zf+step,fit(Kind.FRONT,out,2));}
        if(sign==null)return;
        // Read from the street: the viewer's left is the side the boards begin on.
        var left=out.getClockWise();int mid=(x0+x1)/2;
        int start=mid+(left.getStepX()*(boards/2));
        for(int i=0;i<boards;i++)set(l,b,start-left.getStepX()*i,signY,zf+step,fit(sign,out,i));
    }
    /** A run of older fronts between the stores, each building its own: six to nine wide, one or two storeys, face brick, stucco or
     * painted block, and either a dark storefront under its transoms or a pair of small windows; upper windows where it has a second
     * storey, and a capped parapet on the brick ones. Nothing is open in them. */
    static void blankFront(ServerLevel l,BlockPos b,int x0,int x1,boolean south){
        int start=x0,n=0;
        while(start<x1){
            int end=Math.min(x1,start+6+(int)(hash(start,south?3:5,61)*4));
            if(x1-end<5)end=x1;
            oldFront(l,b,start,end,south,n++);
            start=end;
        }
    }
    static void oldFront(ServerLevel l,BlockPos b,int x0,int x1,boolean south,int n){
        double h=hash(x0,n,62);int kind=(int)(h*3),top=4+(int)(hash(x0,n,63)*3);
        BlockState face=kind==0?mat("storefront"):kind==1?mat("stucco"):mat("cinder");
        shell(l,b,x0,x1,south,face,mat("cinder"),mat("slab"),top,false);
        int zf=south?MAIN_FRONT_S:MAIN_FRONT_N;Direction out=south?Direction.NORTH:Direction.SOUTH;
        if(hash(x0,n,64)<.55){
            // A storefront gone dark: its display glass, the transoms over it, brick piers at its ends.
            for(int x=x0+1;x<x1;x++){set(l,b,x,0,zf,fit(Kind.FRONT,out,4));set(l,b,x,1,zf,fit(Kind.FRONT,out,4));set(l,b,x,2,zf,fit(Kind.FRONT,out,1));}
            for(int y=0;y<3;y++){set(l,b,x0,y,zf,fit(Kind.FRONT,out,5));set(l,b,x1,y,zf,fit(Kind.FRONT,out,5));}
        }else for(int x=x0+2;x<x1-1;x+=3){set(l,b,x,1,zf,fit(Kind.FRONT,out,4));set(l,b,x+1,1,zf,fit(Kind.FRONT,out,4));}
        if(top>=6)for(int x=x0+2;x<x1-1;x+=2)set(l,b,x,4,zf,fit(Kind.WINDOW,out,0));
        if(kind==0)for(int x=x0;x<=x1;x++)set(l,b,x,top+1,zf,fit(Kind.FRONT,out,5));
    }
    static void feedStore(ServerLevel l,BlockPos b){
        int x0=STORES[0][0],x1=STORES[0][1];
        shell(l,b,x0,x1,true,mat("galvanized"),mat("galvanized"),mat("slab"),5);
        front(l,b,x0,x1,true,-33,false,true,Kind.SIGN_FEED,4,4);
        int z=MAIN_FRONT_S+1;
        for(int x=x0+2;x<=x0+6;x++){set(l,b,x,0,z+3,fit(Kind.FEED,Direction.EAST,0));set(l,b,x,1,z+3,fit(Kind.FEED,Direction.EAST,x%2==0?0:1));}
        for(int x=x0+2;x<=x0+6;x++)set(l,b,x,0,z+7,fit(Kind.FEED,Direction.EAST,1));
        set(l,b,x1-3,0,z+1,fit(Kind.FEED,Direction.NORTH,3));set(l,b,x1-4,0,z+1,fit(Kind.FEED,Direction.NORTH,3));
        set(l,b,x1-2,0,z+6,fit(Kind.FEED,Direction.WEST,4));set(l,b,x1-2,0,z+8,fit(Kind.FEED,Direction.WEST,4));
        set(l,b,x1-6,0,z+10,fit(Kind.FEED,Direction.NORTH,2));
        set(l,b,x1-1,1,z+4,fit(Kind.POSTER,Direction.WEST,2));
        // The bus bench at the curb facing the street, the bus stop's sign beside it.
        set(l,b,-36,0,WALK_S_N,fit(Kind.FEED,Direction.NORTH,5));set(l,b,-35,0,WALK_S_N,fit(Kind.FEED,Direction.NORTH,6));
        set(l,b,-38,0,WALK_S_N,fit(Kind.SIGNPOST,Direction.NORTH,4));
        // The loading dock at the side.
        set(l,b,x0-1,0,-102,fit(Kind.FEED,Direction.WEST,7));set(l,b,x0-1,0,-103,fit(Kind.FEED,Direction.WEST,7));
    }
    static void drugstore(ServerLevel l,BlockPos b){
        int x0=STORES[1][0],x1=STORES[1][1];
        shell(l,b,x0,x1,true,mat("storefront"),mat("storefront"),mat("tile"),5);
        front(l,b,x0,x1,true,-11,true,true,Kind.SIGN_DRUG,4,4);
        int z=MAIN_FRONT_S+1;
        for(int zz=z+2;zz<=z+8;zz++){set(l,b,-14,0,zz,fit(Kind.DRUG,Direction.EAST,zz==z+2||zz==z+8?1:0));set(l,b,-8,0,zz,fit(Kind.DRUG,Direction.EAST,zz==z+2||zz==z+8?1:0));}
        for(int x=x0+1;x<x1;x++){if(x==-6)continue;set(l,b,x,0,MAIN_BACK_S-1,fit(Kind.DRUG,Direction.NORTH,4));set(l,b,x,1,MAIN_BACK_S-1,fit(Kind.DRUG,Direction.NORTH,4));}
        set(l,b,-6,0,MAIN_BACK_S-3,fit(Kind.DRUG,Direction.NORTH,2));set(l,b,-5,0,MAIN_BACK_S-3,fit(Kind.DRUG,Direction.NORTH,3));
        for(int x=-17;x<=-16;x++)set(l,b,x,0,z+4,fit(Kind.DRUG,Direction.EAST,5));
        set(l,b,x0+1,0,z+1,fit(Kind.DRUG,Direction.EAST,6));
        set(l,b,x1-1,0,z+1,fit(Kind.DRUG,Direction.WEST,7));
    }
    static void videoStore(ServerLevel l,BlockPos b){
        int x0=STORES[2][0],x1=STORES[2][1];
        shell(l,b,x0,x1,false,mat("stucco"),mat("cinder"),mat("store_carpet"),5);
        front(l,b,x0,x1,false,-6,true,true,Kind.SIGN_VIDEO,5,4);
        int z=MAIN_FRONT_N-1;
        // The horror wall along the west, new releases along the east, a double shelf down the middle; the counter by the door.
        for(int zz=z-2;zz>=MAIN_BACK_N+2;zz--){set(l,b,x0+1,0,zz,fit(Kind.VIDEO,Direction.EAST,0));set(l,b,x0+1,1,zz,fit(Kind.VIDEO,Direction.EAST,0));}
        for(int zz=z-2;zz>=MAIN_BACK_N+2;zz--){set(l,b,x1-1,0,zz,fit(Kind.VIDEO,Direction.WEST,1));set(l,b,x1-1,1,zz,fit(Kind.VIDEO,Direction.WEST,1));}
        for(int zz=z-3;zz>=z-9;zz--)set(l,b,-6,0,zz,fit(Kind.VIDEO,Direction.EAST,2));
        set(l,b,-2,0,z-1,fit(Kind.VIDEO,Direction.WEST,3));set(l,b,-2,0,z-2,fit(Kind.VIDEO,Direction.WEST,4));
        // The return slot, in a brick pier beside the door, opening onto the sidewalk.
        for(int y=0;y<3;y++)set(l,b,-5,y,MAIN_FRONT_N,fit(Kind.FRONT,Direction.SOUTH,5));
        set(l,b,-5,1,MAIN_FRONT_N+1,fit(Kind.VIDEO,Direction.SOUTH,5));
        set(l,b,-9,0,z-1,fit(Kind.VIDEO,Direction.SOUTH,6));
        set(l,b,x1-1,2,z-3,fit(Kind.VIDEO,Direction.WEST,7));
    }
    static void substation(ServerLevel l,BlockPos b){
        int x0=STORES[3][0],x1=STORES[3][1];
        shell(l,b,x0,x1,true,mat("stucco"),mat("stucco"),mat("tile"),4);
        front(l,b,x0,x1,true,35,false,false,Kind.SIGN_LAW,4,3);
        int z=MAIN_FRONT_S+1;
        set(l,b,33,0,z+4,fit(Kind.LAW,Direction.NORTH,0));set(l,b,37,0,z+4,fit(Kind.LAW,Direction.NORTH,1));
        set(l,b,x1-1,0,MAIN_BACK_S-1,fit(Kind.LAW,Direction.WEST,2));set(l,b,x1-1,0,MAIN_BACK_S-2,fit(Kind.LAW,Direction.WEST,2));
        set(l,b,x0+1,0,z+1,fit(Kind.LAW,Direction.EAST,3));set(l,b,x0+1,0,z+2,fit(Kind.LAW,Direction.EAST,3));
        set(l,b,x0+1,1,z+6,fit(Kind.LAW,Direction.EAST,6));
        set(l,b,x0+2,0,MAIN_BACK_S-1,fit(Kind.LAW,Direction.NORTH,7));
        // The flagpole out front with the flag at its top.
        for(int y=0;y<6;y++)set(l,b,x1+2,y,WALK_S_S,fit(Kind.LAW,Direction.NORTH,4));
        set(l,b,x1+2,6,WALK_S_S,fit(Kind.LAW,Direction.EAST,5));
    }
    /** A street light: a pole at the curb, its top the lamp's own, the arm out over the street with the head on its end. */
    static void streetLight(ServerLevel l,BlockPos b,int x,int z,Direction over){
        for(int y=0;y<5;y++)set(l,b,x,y,z,fit(Kind.POLE,over,0));
        set(l,b,x,5,z,fit(Kind.POLE,over,5));
    }

    // ------------------------------------------------------------------------------------------------ the drive-in
    /**
     * The Caprock drive-in on the north side of Main east of the highway: its lot paved to the street, a row of stalls under a canopy with
     * lights along its edge, each stall with its menu and speaker on a post; the stand with its order window and the fryer and flat-top
     * behind it; picnic tables; and the sign on its pole at the corner, lit, with its arrow.
     */
    static void driveIn(ServerLevel l,BlockPos b){
        int x0=AVE_E+3,x1=58,zn=-146;
        ground(l,b,x0,WALK_N_N-1,x1,zn,mat("drive_lot"));
        // The stand: block walls, the order window facing the lot, the kitchen behind it.
        int sw=x0+6,se=x0+14,ss=-133,sn=-141;
        for(int x=sw;x<=se;x++)for(int z=sn;z<=ss;z++){
            boolean wall=x==sw||x==se||z==sn||z==ss;
            set(l,b,x,-1,z,mat("tile"));
            for(int y=0;y<4;y++)set(l,b,x,y,z,wall?mat("cinder"):AIR);
            set(l,b,x,4,z,wall?mat("cinder"):mat("flat_roof"));
        }
        for(int x=sw+2;x<=se-2;x+=2){set(l,b,x,1,ss,fit(Kind.DRIVEIN,Direction.SOUTH,4));}
        // 0.4.714: the fryer is its own fitting now, its knob to the room (TheListFryer).
        set(l,b,TheListFryer.AT,fit(Kind.FRYER,TheListFryer.FACING,TheListFryer.OFF));set(l,b,sw+5,0,ss-1,fit(Kind.DRIVEIN,Direction.SOUTH,6));
        set(l,b,se,0,sn+3,AIR);set(l,b,se,1,sn+3,AIR);door(l,b,new BlockPos(se,0,sn+3),Direction.EAST,true);
        set(l,b,sw+4,3,sn+4,fit(Kind.FIXTURE,Direction.NORTH,2));
        // The stalls: a canopy along the lot's east side, posts at every other stall, a menu and speaker at each.
        int cz0=-131,cz1=-142,cx=x1-6;
        for(int z=cz1;z<=cz0;z++)for(int x=cx;x<=cx+4;x++)set(l,b,x,3,z,fit(Kind.DRIVEIN,Direction.WEST,x==cx?1:0));
        for(int z=cz1;z<=cz0;z+=3){set(l,b,cx,0,z,fit(Kind.DRIVEIN,Direction.WEST,2));set(l,b,cx,1,z,fit(Kind.DRIVEIN,Direction.WEST,2));set(l,b,cx,2,z,fit(Kind.DRIVEIN,Direction.WEST,2));
            set(l,b,cx+1,0,z+1,fit(Kind.DRIVEIN,Direction.WEST,3));}
        for(int x:new int[]{x0+2,x0+4})set(l,b,x,0,-129,fit(Kind.DRIVEIN,Direction.NORTH,7));
        // The sign at the corner on its pole: the board three across and two down, its arrow below.
        int px=x0+1,pz=WALK_N_N-1;
        for(int y=0;y<5;y++)set(l,b,px,y,pz,fit(Kind.SIGN_DRIVEIN,Direction.SOUTH,0));
        set(l,b,px+1,5,pz,fit(Kind.SIGN_DRIVEIN,Direction.SOUTH,7));
        for(int col=0;col<3;col++){
            set(l,b,px-1+col,7,pz,fit(Kind.SIGN_DRIVEIN,Direction.SOUTH,1+col));
            set(l,b,px-1+col,6,pz,fit(Kind.SIGN_DRIVEIN,Direction.SOUTH,4+col));
        }
    }

    // ------------------------------------------------------------------------------------------------ east of the highway
    /** The tracks north and south past the gin: ballast, the rails, a crossbuck either side where Main Street and the gin road cross them. */
    static void tracks(ServerLevel l,BlockPos b){
        for(int z=NORTH;z<=SOUTH;z++){
            for(int x=TRACK_W;x<=TRACK_E;x++){set(l,b,x,-1,z,mat("ballast"));set(l,b,x,0,z,AIR);}
            if(z<=MAIN_S&&z>=MAIN_N){for(int x=TRACK_W;x<=TRACK_E;x++)set(l,b,x,-1,z,mat("asphalt"));}
            if(z<=NINTH_S-1&&z>=NINTH_N+1){for(int x=TRACK_W;x<=TRACK_E;x++)set(l,b,x,-1,z,mat("caliche"));}
            set(l,b,TRACK_X,0,z,fit(Kind.TRACK,Direction.NORTH,0));
        }
        set(l,b,TRACK_W-1,0,MAIN_S+1,fit(Kind.SIGNPOST,Direction.WEST,5));set(l,b,TRACK_E+1,0,MAIN_N-1,fit(Kind.SIGNPOST,Direction.EAST,5));
        set(l,b,TRACK_W-1,0,NINTH_S,fit(Kind.SIGNPOST,Direction.WEST,5));set(l,b,TRACK_E+1,0,NINTH_N,fit(Kind.SIGNPOST,Direction.EAST,5));
    }
    /**
     * The gin: a long building of galvanized iron, rusted along its foot, its roof at nine. Inside, the module feeder at the yard end, the
     * gin stands in a row, the lint cleaner, the press at the far end; the augers in their troughs between them; the suction pipe going up
     * out of the roof and across to the yard. Outside, wrapped bales by the press door. To the south, the module yard: long modules under
     * tarps in rows on the caliche.
     */
    static void gin(ServerLevel l,BlockPos b){
        ground(l,b,GIN_W-4,MODULES_S,EAST,GIN_N-6,mat("caliche"));
        for(int x=GIN_W;x<=GIN_E;x++)for(int z=GIN_N;z<=GIN_S;z++){
            boolean wall=x==GIN_W||x==GIN_E||z==GIN_N||z==GIN_S;
            set(l,b,x,-1,z,mat("slab"));
            for(int y=0;y<GIN_TOP;y++)set(l,b,x,y,z,wall?(y==0||hash(x,z+y,31)<.12?mat("rusted"):mat("galvanized")):AIR);
            set(l,b,x,GIN_TOP,z,mat("galvanized"));
            if(!wall&&(x-GIN_W)%5==2&&(z-GIN_N)%6==3)set(l,b,x,GIN_TOP-1,z,fit(Kind.FIXTURE,Direction.NORTH,3));
        }
        // The doors: the big one on the yard side, the press door at the north end.
        for(int x=GIN_W+8;x<=GIN_W+11;x++)for(int y=0;y<=3;y++)set(l,b,x,y,GIN_S,AIR);
        for(int x=GIN_W+3;x<=GIN_W+5;x++)for(int y=0;y<=2;y++)set(l,b,x,y,GIN_N,AIR);
        int mid=GIN_W+10;
        set(l,b,mid,0,GIN_S-2,fit(Kind.GIN,Direction.SOUTH,0));set(l,b,mid,1,GIN_S-2,fit(Kind.GIN,Direction.SOUTH,1));
        set(l,b,mid+1,0,GIN_S-2,fit(Kind.GIN,Direction.SOUTH,0));set(l,b,mid+1,1,GIN_S-2,fit(Kind.GIN,Direction.SOUTH,1));
        for(int z=GIN_S-4;z>=GIN_S-8;z--)set(l,b,mid,0,z,fit(Kind.GIN,Direction.EAST,6));
        for(int z=GIN_S-10;z>=GIN_N+12;z-=4){set(l,b,mid-3,0,z,fit(Kind.GIN,Direction.EAST,2));set(l,b,mid+3,0,z,fit(Kind.GIN,Direction.WEST,2));set(l,b,mid-3,1,z,fit(Kind.GIN,Direction.EAST,2));set(l,b,mid+3,1,z,fit(Kind.GIN,Direction.WEST,2));}
        for(int z=GIN_S-9;z>=GIN_N+8;z--)set(l,b,mid,0,z,fit(Kind.GIN,Direction.EAST,6));
        set(l,b,mid,0,GIN_N+6,fit(Kind.GIN,Direction.NORTH,3));set(l,b,mid,1,GIN_N+6,fit(Kind.GIN,Direction.NORTH,3));
        set(l,b,GIN_W+4,0,GIN_N+3,fit(Kind.GIN,Direction.NORTH,4));set(l,b,GIN_W+4,1,GIN_N+3,fit(Kind.GIN,Direction.NORTH,5));
        // The suction pipe up through the roof and across over the yard.
        for(int y=0;y<=GIN_TOP+3;y++)set(l,b,mid+5,y,GIN_S+1,fit(Kind.GIN,Direction.NORTH,7));
        // Bales by the press door, wrapped and tagged.
        for(int x=GIN_W+1;x<=GIN_W+7;x+=2){set(l,b,x,0,GIN_N-2,fit(Kind.BALE,Direction.NORTH,x%4==1?1:0));set(l,b,x,0,GIN_N-3,fit(Kind.BALE,Direction.NORTH,0));if(x<GIN_W+5)set(l,b,x,1,GIN_N-2,fit(Kind.BALE,Direction.NORTH,0));}
        // The module yard: rows of modules two high, each eight long, under tarps with their ends showing.
        for(int row=0;row<3;row++){
            int z0=MODULES_S-2-row*7;
            for(int m=0;m<2;m++){
                int x0=GIN_W+1+m*11;
                for(int x=x0;x<x0+8;x++)for(int z=z0;z>z0-3;z--)for(int y=0;y<2;y++)set(l,b,x,y,z,fit(Kind.BALE,Direction.EAST,x==x0||x==x0+7?3:2));
            }
        }
    }
    /** The seed house: a tall shed of galvanized bins by the tracks, its sliding door to the south with the lamp over it, a ladder up its
     * side to the roof, the chute out of its top; inside, the bins' walls, seed piled against them, the auger in its trench. */
    static void seedHouse(ServerLevel l,BlockPos b){
        for(int x=SEED_W;x<=SEED_E;x++)for(int z=SEED_N;z<=SEED_S;z++){
            boolean wall=x==SEED_W||x==SEED_E||z==SEED_N||z==SEED_S;
            set(l,b,x,-1,z,mat("slab"));
            for(int y=0;y<SEED_TOP;y++)set(l,b,x,y,z,wall?mat("galvanized"):AIR);
            set(l,b,x,SEED_TOP,z,mat("galvanized"));
        }
        int dx=(SEED_W+SEED_E)/2;
        for(int x=dx-1;x<=dx+1;x++){set(l,b,x,0,SEED_S,fit(Kind.SEED,Direction.SOUTH,4));set(l,b,x,1,SEED_S,fit(Kind.SEED,Direction.SOUTH,4));set(l,b,x,2,SEED_S,fit(Kind.SEED,Direction.SOUTH,5));}
        set(l,b,dx,3,SEED_S+1,fit(Kind.SEED,Direction.SOUTH,6));
        for(int y=0;y<SEED_TOP;y++)set(l,b,SEED_E+1,y,SEED_S-3,fit(Kind.SEED,Direction.EAST,3));
        for(int y=SEED_TOP+1;y<=SEED_TOP+3;y++)set(l,b,dx,y,SEED_N+2,fit(Kind.SEED,Direction.NORTH,7));
        for(int z=SEED_N+1;z<SEED_S;z++){set(l,b,SEED_W+3,0,z,fit(Kind.SEED,Direction.EAST,0));set(l,b,SEED_W+3,1,z,fit(Kind.SEED,Direction.EAST,0));set(l,b,SEED_W+3,2,z,fit(Kind.SEED,Direction.EAST,0));}
        for(int z=SEED_N+1;z<SEED_S-2;z++)for(int x=SEED_W+1;x<SEED_W+3;x++)set(l,b,x,0,z,fit(Kind.SEED,Direction.NORTH,1));
        for(int z=SEED_N+1;z<SEED_S-1;z++)set(l,b,dx+1,0,z,fit(Kind.SEED,Direction.NORTH,2));
        // 0.4.706 frames: at the roof the bulb was a speck from the floor; it hangs on a chain to where it is seen.
        for(int y=BULB_Y+1;y<SEED_TOP;y++)set(l,b,dx+2,y,(SEED_N+SEED_S)/2,Blocks.CHAIN.defaultBlockState());
        set(l,b,dx+2,BULB_Y,(SEED_N+SEED_S)/2,fit(Kind.FIXTURE,Direction.NORTH,3));
    }
    /**
     * The water tower: four legs braced in three places, a ladder up a leg to the catwalk, the catwalk round the bowl with its
     * rail, the tank over it with the town's name on its west and south faces, its roof and cap, the beacon on top, the riser down the
     * middle to the ground.
     */
    static void tower(ServerLevel l,BlockPos b){
        int cx=TOWER_X,cz=TOWER_Z;
        ground(l,b,cx-5,cz-5,cx+5,cz+5,mat("caliche"));
        for(int[] leg:new int[][]{{-2,-2},{2,-2},{-2,2},{2,2}})for(int y=0;y<=TOWER_LEG_TOP;y++)set(l,b,cx+leg[0],y,cz+leg[1],fit(Kind.TOWER,Direction.NORTH,y%7==6?1:0));
        for(int y=0;y<=TOWER_LEG_TOP;y++)set(l,b,cx,y,cz,fit(Kind.TOWER,Direction.NORTH,6));
        for(int x=cx-3;x<=cx+3;x++)for(int z=cz-3;z<=cz+3;z++){
            boolean ring=Math.abs(x-cx)==3||Math.abs(z-cz)==3;
            set(l,b,x,CATWALK_Y,z,ring?fit(Kind.TOWER,Direction.NORTH,3):fit(Kind.TANK,Direction.NORTH,6));
            if(ring){Direction out=Math.abs(z-cz)==3?(z>cz?Direction.SOUTH:Direction.NORTH):(x>cx?Direction.EAST:Direction.WEST);
                if(!(x==cx+2&&z==cz+3))set(l,b,x,CATWALK_Y+1,z,fit(Kind.TOWER,out,4));}
        }
        // The ladder up the south-east leg's face, on through the catwalk where its rail leaves a gap.
        for(int y=0;y<=CATWALK_Y;y++)set(l,b,cx+2,y,cz+3,fit(Kind.TOWER,Direction.SOUTH,2));
        for(int y=TANK_BOTTOM;y<=TANK_TOP;y++)for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++){
            boolean corner=Math.abs(x-cx)==2&&Math.abs(z-cz)==2,edge=Math.abs(x-cx)==2||Math.abs(z-cz)==2;
            if(corner||!edge)continue;
            set(l,b,x,y,z,fit(Kind.TANK,Direction.NORTH,0));
        }
        // LA ME SA, on the face toward the town and the face toward the highway; read with the viewer's left first.
        for(int i=0;i<3;i++){set(l,b,cx-2,26,cz-1+i,fit(Kind.TANK,Direction.WEST,1+i));set(l,b,cx-1+i,26,cz+2,fit(Kind.TANK,Direction.SOUTH,1+i));}
        for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++){if(Math.abs(x-cx)==2&&Math.abs(z-cz)==2)continue;set(l,b,x,TANK_TOP+1,z,fit(Kind.TANK,Direction.NORTH,4));}
        set(l,b,cx,TANK_TOP+2,cz,fit(Kind.TANK,Direction.NORTH,5));
        set(l,b,cx,TANK_TOP+3,cz,fit(Kind.TOWER,Direction.NORTH,5));
    }
    /** The playa north of the gin: a dry lake bed, flat and pale, its edge ragged into the cotton. */
    static void playa(ServerLevel l,BlockPos b){
        for(int x=PLAYA_W;x<=EAST;x++)for(int z=NORTH;z<=PLAYA_S;z++){
            double edge=Math.min(x-PLAYA_W,PLAYA_S-z);
            if(edge<3&&hash(x,z,41)>edge/3.0)continue;
            if(z<=CURB_S&&z>=CURB_N)continue;
            set(l,b,x,-1,z,mat("playa"));set(l,b,x,0,z,AIR);
        }
    }

    // ------------------------------------------------------------------------------------------------ poles and trees
    /** Utility poles along the back of the lots and up the highway's east side, their wires sagging between them; a transformer on one, a
     * street light on two. The poles keep off the streets and the gin road. */
    static void poles(ServerLevel l,BlockPos b){
        int first=STREETS_W+4,last=first+16*((AVE_W-6-first)/16);
        for(int z:new int[]{NINTH_N-1,SEVENTH_N-1}){
            for(int x=first;x<=last;x+=16){
                for(int y=0;y<6;y++)set(l,b,x,y,z,fit(Kind.POLE,Direction.NORTH,0));
                set(l,b,x,6,z,fit(Kind.POLE,Direction.EAST,1));
                if(x==-40)set(l,b,x,5,z,fit(Kind.POLE,Direction.SOUTH,6));
                if(x==-24||x==8)set(l,b,x,4,z,fit(Kind.POLE,Direction.SOUTH,5));
            }
            for(int x=first;x<=last;x++){
                int d=Math.floorMod(x-first,16);if(d==0)continue;
                set(l,b,x,6,z,fit(Kind.POLE,Direction.NORTH,sag(Math.min(d,16-d))));
            }
            streetLight(l,b,AVE_W-2,z,Direction.SOUTH);
        }
        int x=AVE_E+2;var at=new java.util.ArrayList<Integer>();
        for(int z=SOUTH-4;z>=NORTH+4;z-=16){
            if(z<=WALK_S_S+2&&z>=WALK_N_N-2)continue;
            at.add(z<=NINTH_S+1&&z>=NINTH_N-1?NINTH_N-2:z);
        }
        for(int i=0;i<at.size();i++){
            int z=at.get(i);
            for(int y=0;y<6;y++)set(l,b,x,y,z,fit(Kind.POLE,Direction.EAST,0));
            set(l,b,x,6,z,fit(Kind.POLE,Direction.NORTH,1));
            if(i+1==at.size())continue;
            int z2=at.get(i+1);if(z-z2>24)continue;
            for(int zz=z-1;zz>z2;zz--)set(l,b,x,6,zz,fit(Kind.POLE,Direction.EAST,sag(Math.min(z-zz,zz-z2))));
        }
    }
    /** How low the wires hang this far from the nearer pole. */
    static int sag(int fromPole){return fromPole<=2?2:fromPole<=5?3:4;}
    /** Mesquite along the tracks, at the backs of the lots, by the gin and round the playa's edge: low trees, crooked, a trunk that forks
     * into a thin canopy. */
    static void mesquite(ServerLevel l,BlockPos b){
        int[][] at={{60,-20},{60,-52},{60,-36},{68,-96},{60,-150},{68,-160},{-55,-44},{-12,-56},{-56,-84},{93,-36},{88,-98},{50,-60},{40,-36},
            {30,-10},{-30,12},{-60,2},{-70,-40},{-75,-102},{-50,-150},{8,-160},{30,-160},{-16,-84}};
        for(int[] p:at)tree(l,b,p[0],p[1]);
    }
    static void tree(ServerLevel l,BlockPos b,int x,int z){
        int h=hash(x,z,51)<.5?2:3;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){set(l,b,x+dx,0,z+dz,AIR);}
        set(l,b,x,-1,z,mat("dirt"));
        for(int y=0;y<h-1;y++)set(l,b,x,y,z,fit(Kind.MESQUITE,Direction.NORTH,0));
        set(l,b,x,h-1,z,fit(Kind.MESQUITE,Direction.NORTH,1));
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            int r=Math.abs(dx)+Math.abs(dz);if(r>3)continue;
            if(dx==0&&dz==0){set(l,b,x,h,z,fit(Kind.MESQUITE,Direction.NORTH,2));continue;}
            if(hash(x+dx,z+dz,52)<.25)continue;
            set(l,b,x+dx,h,z+dz,fit(Kind.MESQUITE,Direction.from2DDataValue((int)(hash(x+dx,z+dz,53)*4)),r>=2?3:2));
        }
        Direction side=Direction.from2DDataValue((int)(hash(x,z,54)*4));
        set(l,b,x+side.getStepX(),1,z+side.getStepZ(),fit(Kind.MESQUITE,side,4));
    }

    // ------------------------------------------------------------------------------------------------ 0.4.701: the pasture
    /**
     * South across the cotton from the back yard: a caliche county road west off Avenue K with a stop sign at the highway, and a pasture of
     * buffalo grass in barbed wire on cedar posts. The road comes in over a cattle guard in the east fence and goes on as the lease road;
     * a wire gap in the north fence opens by hand. Inside: the stock tank with trodden mud round it and its windmill beside it, the pipe
     * from the pump over the rim; a hay ring and a mineral block; cow trails worn to the dirt; mesquite and prickly pear, broomweed,
     * tumbleweeds blown against the fence; three pumpjacks on caliche pads, each with its well's plate, on spurs off the lease road; the
     * tank battery inside its berm by the gate; a power line along the north fence; the lease's sign at the road.
     */
    static void pasture(ServerLevel l,BlockPos b){
        // The ground: grass grazed short, worn to dirt in patches; the fence's row clear.
        for(int x=PAST_W;x<=PAST_E;x++)for(int z=PAST_N;z<=PAST_S;z++){
            set(l,b,x,-1,z,hash(x,z,61)<.12?mat("dirt"):mat("pasture"));set(l,b,x,0,z,AIR);
        }
        // Cow trails from the tank out to the hay ring, the far corner and the south fence.
        for(int[] to:new int[][]{{-20,66},{-66,92},{-36,121},{-8,80}})trail(l,b,TANK_AT.getX(),TANK_AT.getZ(),to[0],to[1]);
        // The county road to the highway, its stop sign; the cattle guard in the fence; the lease road on in.
        ground(l,b,PAST_E+2,ROAD_N,AVE_W-2,ROAD_S,mat("caliche"));
        set(l,b,AVE_W-3,0,ROAD_S+1,fit(Kind.SIGNPOST,Direction.WEST,3));
        ground(l,b,PAST_W+8,LEASE_N,PAST_E-1,LEASE_S,mat("caliche"));
        for(int x=PAST_E-1;x<=PAST_E+1;x++)for(int z=ROAD_N;z<=ROAD_S;z++){set(l,b,x,-1,z,fit(Kind.PASTURE,Direction.EAST,4));set(l,b,x,0,z,AIR);set(l,b,x,-2,z,mat("dirt"));}
        // The fence: a post every five, wire between, a braced post at each corner with the next post beside it.
        for(int x=PAST_W;x<=PAST_E;x++){fence(l,b,x,PAST_N,Direction.NORTH,x-PAST_W);fence(l,b,x,PAST_S,Direction.NORTH,x-PAST_W);}
        for(int z=PAST_N;z<=PAST_S;z++){
            fence(l,b,PAST_W,z,Direction.EAST,z-PAST_N);
            if(z<ROAD_N||z>ROAD_S)fence(l,b,PAST_E,z,Direction.EAST,z-PAST_N);
        }
        set(l,b,PAST_E,0,ROAD_N-1,fit(Kind.PASTURE,Direction.EAST,0));set(l,b,PAST_E,0,ROAD_S+1,fit(Kind.PASTURE,Direction.EAST,0));
        set(l,b,PAST_W,0,PAST_N,fit(Kind.PASTURE,Direction.NORTH,5));set(l,b,PAST_W+1,0,PAST_N,fit(Kind.PASTURE,Direction.NORTH,0));
        set(l,b,PAST_E,0,PAST_N,fit(Kind.PASTURE,Direction.EAST,5));set(l,b,PAST_E,0,PAST_N+1,fit(Kind.PASTURE,Direction.EAST,0));
        set(l,b,PAST_E,0,PAST_S,fit(Kind.PASTURE,Direction.SOUTH,5));set(l,b,PAST_E-1,0,PAST_S,fit(Kind.PASTURE,Direction.NORTH,0));
        set(l,b,PAST_W,0,PAST_S,fit(Kind.PASTURE,Direction.WEST,5));set(l,b,PAST_W,0,PAST_S-1,fit(Kind.PASTURE,Direction.EAST,0));
        // The wire gap, shut, between two posts.
        set(l,b,GAP_X-2,0,PAST_N,fit(Kind.PASTURE,Direction.NORTH,0));set(l,b,GAP_X+2,0,PAST_N,fit(Kind.PASTURE,Direction.NORTH,0));
        for(int x=GAP_X-1;x<=GAP_X+1;x++)set(l,b,x,0,PAST_N,fit(Kind.PASTURE,Direction.NORTH,2));
        // The stock tank and its windmill; the hay ring and the mineral block.
        stockTank(l,b);
        windmill(l,b);
        hayRing(l,b,-20,66);
        set(l,b,-24,0,77,fit(Kind.PASTURE,Direction.NORTH,6));
        // The pumpjacks, each on a pad at the end of its spur.
        for(int i=0;i<JACKS.length;i++)pumpjack(l,b,i);
        ground(l,b,JACKS[0].getX()-1,LEASE_S+1,JACKS[0].getX()+1,JACKS[0].getZ()-5,mat("caliche"));
        ground(l,b,JACKS[1].getX()-1,LEASE_S+1,JACKS[1].getX()+1,JACKS[1].getZ()-4,mat("caliche"));
        ground(l,b,JACKS[2].getX()-4,LEASE_S+1,JACKS[2].getX()-2,JACKS[2].getZ()-4,mat("caliche"));
        // The two-track from the lease road down to the tank.
        for(int z=LEASE_S+1;z<=TANK_AT.getZ()-7;z++){set(l,b,TANK_AT.getX()-1,-1,z,mat("dirt"));set(l,b,TANK_AT.getX()+1,-1,z,mat("dirt"));}
        battery(l,b);
        // Mesquite, prickly pear, broomweed and tufts on the grass; tumbleweeds caught along the south and west fences.
        for(int[] t:new int[][]{{-60,70},{-8,72},{-50,96},{-25,112},{-70,52},{-45,118},{-2,116}})tree(l,b,t[0],t[1]);
        for(int x=PAST_W+1;x<PAST_E;x++)for(int z=PAST_N+1;z<PAST_S;z++){
            if(!l.getBlockState(b.offset(x,0,z)).isAir()||!l.getBlockState(b.offset(x,-1,z)).is(TheListRegistry.material("pasture")))continue;
            double h=hash(x,z,62);
            boolean fence=x==PAST_W+1||z==PAST_S-1;
            if(fence&&hash(x,z,63)<.18)set(l,b,x,0,z,fit(Kind.WEED,Direction.NORTH,0));
            else if(h<.006)set(l,b,x,0,z,fit(Kind.WEED,Direction.from2DDataValue((int)(hash(x,z,64)*4)),4));
            else if(h<.03)set(l,b,x,0,z,fit(Kind.WEED,Direction.from2DDataValue((int)(hash(x,z,64)*4)),2));
            else if(h<.045)set(l,b,x,0,z,fit(Kind.WEED,Direction.from2DDataValue((int)(hash(x,z,64)*4)),1));
        }
        // The power line in along the north fence from the highway's poles.
        powerLine(l,b);
        // The lease's sign at the road, outside the guard.
        set(l,b,PAST_E+4,0,ROAD_S+2,fit(Kind.BATTERY,Direction.NORTH,4));
    }
    /** One cell of the pasture's fence along a run: a post every five, wire between. */
    static void fence(ServerLevel l,BlockPos b,int x,int z,Direction along,int i){
        set(l,b,x,0,z,fit(Kind.PASTURE,along,Math.floorMod(i,5)==0?0:1));set(l,b,x,-1,z,mat("pasture"));
    }
    /** A trail worn by cattle, a little crooked, two cells wide in places. */
    static void trail(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        int n=Math.max(Math.abs(x1-x0),Math.abs(z1-z0));
        for(int i=0;i<=n;i++){
            double t=i/(double)n,wob=Math.sin(t*9+x1)*1.6;
            int x=(int)Math.round(x0+(x1-x0)*t+wob*(z1-z0)/(double)Math.max(1,n)),z=(int)Math.round(z0+(z1-z0)*t-wob*(x1-x0)/(double)Math.max(1,n));
            set(l,b,x,-1,z,mat("dirt"));if(hash(x,z,65)<.4)set(l,b,x+1,-1,z,mat("dirt"));
        }
    }
    /** Water in the tank: seven across, an octagon of whole cells. */
    public static boolean inTank(int dx,int dz){return Math.abs(dx)<=3&&Math.abs(dz)<=3&&Math.abs(dx)+Math.abs(dz)<=4;}
    /**
     * The stock tank: water in its cells over a galvanized floor; round it, in each cell that touches the water, its wall on that side, a
     * corner where it touches on two, the seam where it only meets at a point. Trodden mud round it all. The windmill's pipe comes over the
     * west wall (the pipe along the ground is the windmill's to lay).
     */
    static void stockTank(ServerLevel l,BlockPos b){
        int cx=TANK_AT.getX(),cz=TANK_AT.getZ();
        for(int dx=-7;dx<=7;dx++)for(int dz=-7;dz<=7;dz++){
            if(dx*dx+dz*dz>6.5*6.5&&!(dx*dx+dz*dz<=8*8&&hash(cx+dx,cz+dz,66)<.5))continue;
            set(l,b,cx+dx,-1,cz+dz,mat("mud"));set(l,b,cx+dx,0,cz+dz,AIR);
        }
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
            int x=cx+dx,z=cz+dz;
            if(inTank(dx,dz)){set(l,b,x,-1,z,mat("galvanized"));set(l,b,x,0,z,net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());continue;}
            boolean n=inTank(dx,dz-1),so=inTank(dx,dz+1),e=inTank(dx+1,dz),w=inTank(dx-1,dz);
            int sides=(n?1:0)+(so?1:0)+(e?1:0)+(w?1:0);
            if(sides==1){set(l,b,x,0,z,fit(Kind.STOCK,so?Direction.NORTH:n?Direction.SOUTH:e?Direction.WEST:Direction.EAST,0));continue;}
            if(sides==2){set(l,b,x,0,z,fit(Kind.STOCK,so&&e?Direction.NORTH:w&&so?Direction.EAST:n&&w?Direction.SOUTH:Direction.WEST,1));continue;}
            boolean se=inTank(dx+1,dz+1),sw=inTank(dx-1,dz+1),nw=inTank(dx-1,dz-1),ne=inTank(dx+1,dz-1);
            if(se||sw||nw||ne)set(l,b,x,0,z,fit(Kind.STOCK,se?Direction.NORTH:sw?Direction.EAST:nw?Direction.SOUTH:Direction.WEST,2));
        }
        set(l,b,cx-4,0,cz,fit(Kind.STOCK,Direction.WEST,3));
    }
    /**
     * The windmill west of the tank, its wheel to the east: a tower of four legs on concrete footings, leaning in to a platform eleven
     * blocks up, the pump at its foot; the head on the tower, its wheel turning in front (the machine block and its renderer) and the tail
     * behind on its boom.
     */
    static void windmill(ServerLevel l,BlockPos b){
        int x=WINDMILL_AT.getX(),z=WINDMILL_AT.getZ();
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){set(l,b,x+dx,0,z+dz,AIR);if(hash(x+dx,z+dz,67)<.6)set(l,b,x+dx,-1,z+dz,mat("dirt"));}
        for(int k=0;k<4;k++)set(l,b,x,3*k,z,fit(Kind.WINDMILL,Direction.EAST,k));
        set(l,b,x-1,0,z-1,fit(Kind.WINDMILL,Direction.NORTH,6));set(l,b,x+1,0,z-1,fit(Kind.WINDMILL,Direction.EAST,6));
        set(l,b,x+1,0,z+1,fit(Kind.WINDMILL,Direction.SOUTH,6));set(l,b,x-1,0,z+1,fit(Kind.WINDMILL,Direction.WEST,6));
        set(l,b,x,WIND_TOP,z,TheListMachineBlock.of(TheListMachineBlock.Machine.WINDMILL,Direction.EAST));
        set(l,b,x-1,WIND_TOP,z,fit(Kind.WINDMILL,Direction.EAST,7));
        // The pump's pipe along the ground to the tank's spout, laid after the ground round the tower is cleared.
        for(int px=x+2;px<TANK_AT.getX()-4;px++)set(l,b,px,0,z,fit(Kind.STOCK,Direction.EAST,4));
    }
    /** A hay ring round the last of a bale, on ground the cattle have worn to dirt round it. */
    static void hayRing(ServerLevel l,BlockPos b,int x,int z){
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){set(l,b,x+dx,0,z+dz,AIR);set(l,b,x+dx,-1,z+dz,hash(x+dx,z+dz,68)<.7?mat("dirt"):mat("mud"));}
        set(l,b,x,0,z,fit(Kind.PASTURE,Direction.NORTH,7));
    }
    /**
     * Pumpjack i on its caliche pad: the wellhead three blocks before its Samson post with the well's plate beside it, the post, the
     * machine block above it that keeps the beam, cranks, pitman arms and rod turning, and the gearbox and motor on the skid two behind.
     */
    static void pumpjack(ServerLevel l,BlockPos b,int i){
        var a=JACKS[i];var f=JACK_FACING[i];var side=f.getClockWise();
        for(int along=-4;along<=4;along++)for(int across=-3;across<=3;across++){
            var at=a.relative(f,along).relative(side,across);
            set(l,b,at.getX(),-1,at.getZ(),mat("caliche"));set(l,b,at.getX(),0,at.getZ(),AIR);
        }
        set(l,b,a.relative(f,3),fit(Kind.PUMPJACK,f,0));
        set(l,b,a.relative(f,3).relative(side,2),fit(Kind.BATTERY,f,5+i));
        set(l,b,a,fit(Kind.PUMPJACK,f,1));
        set(l,b,a.above(2),TheListMachineBlock.of(TheListMachineBlock.Machine.PUMPJACK,f));
        set(l,b,a.relative(f.getOpposite(),2),fit(Kind.PUMPJACK,f,7));
    }
    /**
     * The tank battery inside the gate: two tanks and the separator inside an earth berm a block high, with a way in from the lease road.
     * Each tank is drawn by its two middle blocks; the cells round them only hold its girth.
     */
    static void battery(ServerLevel l,BlockPos b){
        int x0=-7,x1=5,z0=LEASE_S+2,z1=LEASE_S+10;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            set(l,b,x,-1,z,mat("dirt"));set(l,b,x,0,z,AIR);
            boolean edge=x==x0||x==x1||z==z0||z==z1;
            if(edge&&!(z==z0&&x>=-2&&x<=-1))set(l,b,x,0,z,mat("dirt"));
        }
        for(var t:BATTERY_TANKS){
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int y=0;y<=4;y++)set(l,b,t.getX()+dx,y,t.getZ()+dz,fit(Kind.BATTERY,Direction.EAST,3));
            set(l,b,t,fit(Kind.BATTERY,Direction.EAST,0));set(l,b,t.above(3),fit(Kind.BATTERY,Direction.EAST,1));
        }
        set(l,b,3,0,BATTERY_TANKS[0].getZ(),fit(Kind.BATTERY,Direction.NORTH,2));
    }
    /** The power line in from the highway's poles along the north fence, its wires sagging between the poles. */
    static void powerLine(ServerLevel l,BlockPos b){
        int z=PAST_N+6,from=AVE_E+2;
        var at=new java.util.ArrayList<Integer>();
        for(int x=from-16;x>=PAST_W+6;x-=16)at.add(x);
        for(int x:at){
            for(int y=0;y<6;y++)set(l,b,x,y,z,fit(Kind.POLE,Direction.NORTH,0));
            set(l,b,x,6,z,fit(Kind.POLE,Direction.EAST,1));
        }
        for(int x=at.get(at.size()-1)+1;x<from;x++){
            if(at.contains(x))continue;
            int near=from-x;for(int p:at)near=Math.min(near,Math.abs(x-p));
            set(l,b,x,6,z,fit(Kind.POLE,Direction.NORTH,sag(near)));
        }
    }
}
