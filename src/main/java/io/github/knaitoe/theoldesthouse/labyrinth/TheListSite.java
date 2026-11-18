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
    /** The scene's edges. South of the House's door are the back yards and the cotton behind them. */
    public static final int WEST=-88,EAST=96,NORTH=-176,SOUTH=24;
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
    public static final int TOWER_X=57,TOWER_Z=-82,TOWER_LEG_TOP=21,CATWALK_Y=22,TANK_BOTTOM=23,TANK_TOP=29;
    public static final int PLAYA_W=70,PLAYA_S=-100;
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
    /** Under a roof: the house, a store, the gin, the seed house. */
    public static boolean indoors(Vec3 r){
        return inHouse(r)||inStore(r)||r.x>GIN_W+1&&r.x<GIN_E&&r.z<GIN_S&&r.z>GIN_N+1&&r.y<GIN_TOP||r.x>SEED_W+1&&r.x<SEED_E&&r.z<SEED_S&&r.z>SEED_N+1&&r.y<SEED_TOP;
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
        main(l,b);
        driveIn(l,b);
        tracks(l,b);
        gin(l,b);
        seedHouse(l,b);
        tower(l,b);
        playa(l,b);
        poles(l,b);
        mesquite(l,b);
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
        lawn(l,b,-22,HOUSE_S+1,22,SOUTH-2);
        ground(l,b,FRONT_DOOR.getX(),HOUSE_N-1,FRONT_DOOR.getX(),YARD_N,mat("sidewalk"));
        set(l,b,FRONT_DOOR.getX()+1,1,HOUSE_N-1,fit(Kind.YARD,Direction.NORTH,0));
        set(l,b,8,0,YARD_N,fit(Kind.MAILBOX,Direction.NORTH,0));
        fenceRun(l,b,-22,HOUSE_S+1,-22,SOUTH-2,Direction.EAST);
        fenceRun(l,b,22,HOUSE_S+1,22,SOUTH-2,Direction.EAST);
        fenceRun(l,b,-22,SOUTH-2,22,SOUTH-2,Direction.NORTH);
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
        neighbor(l,b,-8,8,-78,-88,Direction.SOUTH,7,false,true);
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
        set(l,b,sw+3,0,ss-1,fit(Kind.DRIVEIN,Direction.SOUTH,5));set(l,b,sw+5,0,ss-1,fit(Kind.DRIVEIN,Direction.SOUTH,6));
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
        for(int z=GIN_S-4;z>=GIN_S-8;z--)set(l,b,mid,0,z,fit(Kind.GIN,Direction.NORTH,6));
        for(int z=GIN_S-10;z>=GIN_N+12;z-=4){set(l,b,mid-3,0,z,fit(Kind.GIN,Direction.EAST,2));set(l,b,mid+3,0,z,fit(Kind.GIN,Direction.WEST,2));set(l,b,mid-3,1,z,fit(Kind.GIN,Direction.EAST,2));set(l,b,mid+3,1,z,fit(Kind.GIN,Direction.WEST,2));}
        for(int z=GIN_S-9;z>=GIN_N+8;z--)set(l,b,mid,0,z,fit(Kind.GIN,Direction.NORTH,6));
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
        set(l,b,dx+2,SEED_TOP-1,(SEED_N+SEED_S)/2,fit(Kind.FIXTURE,Direction.NORTH,3));
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
        int[][] at={{60,-20},{60,-52},{60,-36},{68,-96},{60,-150},{68,-160},{-55,-44},{-12,-56},{12,-86},{-56,-84},{93,-36},{88,-98},{50,-60},{40,-36},
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
}
