package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.LodgingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LodgingFloor.Pair;
import io.github.knaitoe.theoldesthouse.labyrinth.LodgingWall.Part;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.549, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md): the lodging house, built whole in its interior slot. The
 * House's door is its front door, in the middle of the south wall. Its constants are its plan.
 *
 * <p>Ground floor: the front hall, with the stair rising north up its east side over a cupboard; the parlor west of it and the dining
 * room east; the back passage across the house behind them; off that, Daniel's room (back west), the back stair down to the cellar
 * (middle) and the scullery (back east, round behind the back stair). Upstairs: the bedroom gallery railed round the stairwell, six
 * guest rooms off it, three a side, and north of it a narrow corridor to the workroom (west) and Vera's room (east). Behind the side and
 * back walls on both floors, the passages, a body wide, with ladders at the back corners down to the cellar. The cellar runs under
 * all of it: the sill round three sides under the frames' walls, a post on it under every frame, the ratchet in the middle of the back
 * run, and a dead post standing on its own.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class LodgingSite {
    private LodgingSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The outside walls. */
    public static final int WEST=-16,EAST=16,FRONT=0,BACK=-31;
    /** Walking levels, and the slabs under them: the cellar's floor, the ground floor's, the upper floor's, the roof. */
    public static final int CELLAR=-4,GROUND=0,UPPER=5,CELLAR_SLAB=-5,GROUND_SLAB=-1,UPPER_SLAB=4,ROOF=9;
    /** The passages behind the walls: west and east, down the sides; north, across the back. */
    public static final int PASS_W=-15,PASS_E=15,PASS_N=-30;
    /** The rooms' outside walls, between them and the passages. */
    public static final int ROOM_W=-14,ROOM_E=14,ROOM_N=-29;
    /** The hall, x -3 to 3, from the door to z -12; the back passage across the house, z -13 to -15. */
    public static final int HALL_N=-12,BACK_S=-13,BACK_N=-15,BACK_WALL=-16;
    /** The back stair's well, x -1 to 1 from z -17 to -19, and its north wall. */
    public static final int WELL_N=-19,WELL_WALL=-20;
    /** The upstairs gallery, x -3 to 3 to z -15; the narrow corridor north of it, x -1 to 1, to z -28. */
    public static final int GALLERY_N=-15;
    /** The guest rooms, one to six: three west (x -13 to -5) and three east (x 5 to 13), each from z0 to z1. */
    public static final int[][] ROOM_Z={{-1,-4},{-6,-10},{-12,-15}};
    /** Each guest room's frame, its opening's z in the room's outside wall. */
    public static final int[] FRAME_Z={-3,-8,-13};
    /** Each guest room's door in the gallery's walls (its lower cell); and its hatch, from the passage. */
    public static final BlockPos[] ROOM_DOORS={new BlockPos(-4,UPPER,-3),new BlockPos(-4,UPPER,-8),new BlockPos(-4,UPPER,-13),
        new BlockPos(4,UPPER,-3),new BlockPos(4,UPPER,-7),new BlockPos(4,UPPER,-13)};
    public static final int[] HATCH_Z={-1,-10,-15};
    /** Daniel's frame: its opening in his room's back wall, held standing in it. */
    public static final BlockPos DANIEL_FRAME=new BlockPos(-8,GROUND,ROOM_N);
    /** Doors (lower cells). */
    public static final BlockPos PARLOR_DOOR=new BlockPos(-4,0,-9),DINING_DOOR=new BlockPos(4,0,-5),DANIEL_DOOR=new BlockPos(-8,0,BACK_WALL),
        SCULLERY_DOOR=new BlockPos(8,0,BACK_WALL),JIB=new BlockPos(ROOM_W,0,-14),CUPBOARD=new BlockPos(2,0,-10),SCULLERY_BACK=new BlockPos(0,0,ROOM_N),
        SCULLERY_SIDE=new BlockPos(ROOM_E,0,-24),WORK_DOOR=new BlockPos(-2,UPPER,-20),VERA_DOOR=new BlockPos(2,UPPER,-20),UPPER_BACK=new BlockPos(0,UPPER,ROOM_N);
    /** The hatches: the parlor's, the dining room's, and the one Daniel's food comes through. */
    public static final BlockPos PARLOR_HATCH=new BlockPos(ROOM_W,1,-6),DINING_HATCH=new BlockPos(ROOM_E,1,-6),FOOD_HATCH=new BlockPos(ROOM_W,1,-22);
    /** The first paper, the house's terms, on the hall table; each reader's account will wait on the bench across from it. */
    public static final BlockPos SOURCE=new BlockPos(-3,0,-5),ENDING=new BlockPos(3,0,-2);
    /** The hall stand with its glass, and the coat hooks by the door. */
    public static final BlockPos HALL_STAND=new BlockPos(-3,0,-7);
    /** The way on: the scullery's back door. */
    public static final BlockPos ONWARD=new BlockPos(10,0,ROOM_N);
    /** The cellar: the ratchet in front of the back run of the sill, and the dead post nobody's room stands on. */
    public static final BlockPos RATCHET=new BlockPos(0,CELLAR,ROOM_N+1),DEAD_POST=new BlockPos(-9,CELLAR,-5);
    /** The main stair's treads, lowest first: x 2 and 3, rising north. */
    public static final int STAIR_FOOT=-7;
    /** The ladders between the floors: at the side passages' front ends, against the front wall. */
    public static final int LADDER_Z=-1;
    /** The back stair down: its top tread, set into the well's floor, and how many treads it takes to the cellar's, one lower each. */
    public static final int CELLAR_STAIR_TOP=-18,CELLAR_TREADS=GROUND_SLAB-CELLAR+1;

    // ------------------------------------------------------------------------------------------------ where a body is
    private static boolean in(Vec3 r,double x0,double z0,double x1,double z1){return r.x>=x0&&r.x<x1+1&&r.z>=z0&&r.z<z1+1;}
    /** Anywhere inside the house, the cellar and the passages included. */
    public static boolean inHouse(Vec3 r){return r.x>WEST&&r.x<EAST&&r.z>BACK&&r.z<FRONT&&r.y>CELLAR_SLAB&&r.y<ROOF;}
    public static boolean upstairs(Vec3 r){return inHouse(r)&&r.y>=UPPER_SLAB+.5;}
    public static boolean inCellar(Vec3 r){return inHouse(r)&&r.y<GROUND_SLAB;}
    public static boolean inHall(Vec3 r){return !upstairs(r)&&!inCellar(r)&&in(r,-3,HALL_N,3,-1);}
    public static boolean inParlor(Vec3 r){return !upstairs(r)&&!inCellar(r)&&in(r,-13,-11,-5,-1);}
    public static boolean inDining(Vec3 r){return !upstairs(r)&&!inCellar(r)&&in(r,5,-11,13,-1);}
    public static boolean inBackPassage(Vec3 r){return !upstairs(r)&&!inCellar(r)&&in(r,-13,BACK_N,13,BACK_S);}
    public static boolean inDaniels(Vec3 r){return !upstairs(r)&&!inCellar(r)&&in(r,-13,-28,-3,-17);}
    public static boolean inScullery(Vec3 r){return !upstairs(r)&&!inCellar(r)&&(in(r,3,-28,13,-17)||in(r,-1,-28,2,WELL_WALL-1));}
    /** In the passages behind the walls, on either floor (not the cellar). */
    public static boolean inPassages(Vec3 r){return inHouse(r)&&!inCellar(r)&&(r.x<ROOM_W||r.x>=ROOM_E+1||r.z<ROOM_N);}
    public static boolean onGallery(Vec3 r){return upstairs(r)&&(in(r,-3,GALLERY_N,3,-1)||in(r,-1,-28,1,BACK_WALL));}
    /** Which guest room (0 to 5) a body is in upstairs, or -1. */
    public static int guestRoom(Vec3 r){
        if(!upstairs(r))return -1;
        for(int i=0;i<3;i++){if(in(r,-13,ROOM_Z[i][1],-5,ROOM_Z[i][0]))return i;if(in(r,5,ROOM_Z[i][1],13,ROOM_Z[i][0]))return 3+i;}
        return -1;
    }
    public static boolean inWorkroom(Vec3 r){return upstairs(r)&&in(r,-13,-28,-3,-17);}
    public static boolean inVeras(Vec3 r){return upstairs(r)&&in(r,3,-28,13,-17);}
    /** The opening of guest room i's frame (its lower cell). */
    public static BlockPos frame(int room){return new BlockPos(room<3?ROOM_W:ROOM_E,UPPER,FRAME_Z[room%3]);}
    /** The direction guest room i's frame faces: into the room. */
    public static Direction frameFacing(int room){return room<3?Direction.EAST:Direction.WEST;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1),Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1),s);}
    static BlockState mat(String name){return LodgingRegistry.material(name).defaultBlockState();}
    static BlockState fit(Kind kind,Direction facing,int stage){return LodgingBlock.of(kind,facing,stage);}
    static BlockState floor(Pair pair){return LodgingFloor.of(pair);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    /** A run of two-faced wall, both storeys' worth or one: the lowest course of each storey with its skirting. */
    private static void wall(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int y0,int y1,LodgingWall.Pair pair,Direction front){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)for(int y=y0;y<=y1;y++)
            set(l,b,x,y,z,LodgingWall.of(pair,front,y==GROUND||y==UPPER?Part.LOW:Part.MID));
    }
    /** A run of plain wall in one material, its lowest course in that material's skirting. */
    private static void plain(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int y0,int y1,String paper){
        for(int y=y0;y<=y1;y++)box(l,b,x0,y,z0,x1,y,z1,mat(y==GROUND||y==UPPER?paper+"_low":paper));
    }
    /** A door's two cells, shut, facing the side it is approached from. */
    public static void leaf(ServerLevel l,BlockPos b,BlockPos at,Kind kind,Direction facing,boolean open,int extra){
        set(l,b,at,fit(kind,facing,(open?1:0)|extra));set(l,b,at.above(),fit(kind,facing,(open?1:0)|2|extra));
    }
    /** A frame: its opening (two cells) with a jamb either side and its head across the top. */
    static void frame(ServerLevel l,BlockPos b,BlockPos opening,Direction facing,boolean daniel){
        var right=facing.getClockWise();
        for(int y=0;y<2;y++){
            set(l,b,opening.above(y),fit(Kind.FRAME_BACK,facing,y|(daniel?2:0)));
            set(l,b,opening.above(y).relative(right,-1),fit(Kind.FRAME_SIDE,facing,y|(daniel?4:0)));
            set(l,b,opening.above(y).relative(right,1),fit(Kind.FRAME_SIDE,facing,y|2|(daniel?4:0)));
        }
        for(int k=-1;k<=1;k++)set(l,b,opening.above(2).relative(right,k),fit(Kind.FRAME_HEAD,facing,k+1+(daniel?3:0)));
    }
    private static void hatch(ServerLevel l,BlockPos b,BlockPos at,LodgingWall.Pair pair,Direction front){set(l,b,at,LodgingWall.of(pair,front,Part.HATCH));}

    public static void build(ServerLevel l,BlockPos b){
        // The whole volume, then the outside walls of brick from the cellar's floor to the roof.
        box(l,b,WEST,CELLAR_SLAB,BACK,EAST,ROOF,FRONT,AIR);
        box(l,b,WEST,CELLAR_SLAB,BACK,WEST,ROOF,FRONT,mat("brick"));box(l,b,EAST,CELLAR_SLAB,BACK,EAST,ROOF,FRONT,mat("brick"));
        box(l,b,WEST,CELLAR_SLAB,BACK,EAST,ROOF,BACK,mat("brick"));box(l,b,WEST,CELLAR_SLAB,FRONT,EAST,ROOF,FRONT,mat("brick"));
        // Their lowest course in the cellar has gone damp.
        for(int z=BACK+1;z<FRONT;z++){set(l,b,WEST,CELLAR,z,mat("brick_damp"));set(l,b,EAST,CELLAR,z,mat("brick_damp"));}
        for(int x=WEST;x<=EAST;x++){set(l,b,x,CELLAR,BACK,mat("brick_damp"));set(l,b,x,CELLAR,FRONT,mat("brick_damp"));}
        floors(l,b);
        groundWalls(l,b);
        upperWalls(l,b);
        stairs(l,b);
        cellar(l,b);
        fittings(l,b);
        furnish(l,b);
    }

    private static void floors(ServerLevel l,BlockPos b){
        box(l,b,WEST+1,CELLAR_SLAB,BACK+1,EAST-1,CELLAR_SLAB,FRONT-1,mat("flags"));
        // The ground floor over the cellar.
        box(l,b,WEST+1,GROUND_SLAB,BACK+1,EAST-1,GROUND_SLAB,FRONT-1,floor(Pair.BOARDS));
        box(l,b,-3,GROUND_SLAB,HALL_N,3,GROUND_SLAB,-1,floor(Pair.TILE));
        box(l,b,-13,GROUND_SLAB,BACK_N,13,GROUND_SLAB,BACK_S,floor(Pair.WORN));
        box(l,b,-13,GROUND_SLAB,-28,-3,GROUND_SLAB,-17,floor(Pair.SCUFFED));
        box(l,b,3,GROUND_SLAB,-28,13,GROUND_SLAB,-17,floor(Pair.FLAGS));box(l,b,-1,GROUND_SLAB,-28,2,GROUND_SLAB,WELL_WALL-1,floor(Pair.FLAGS));
        box(l,b,PASS_W,GROUND_SLAB,PASS_N,PASS_W,GROUND_SLAB,-1,floor(Pair.PASSAGE_LOW));box(l,b,PASS_E,GROUND_SLAB,PASS_N,PASS_E,GROUND_SLAB,-1,floor(Pair.PASSAGE_LOW));
        box(l,b,PASS_W,GROUND_SLAB,PASS_N,PASS_E,GROUND_SLAB,PASS_N,floor(Pair.PASSAGE_LOW));
        // Upstairs over the ground floor: the runner down the gallery and the corridor, boards either side of it.
        box(l,b,WEST+1,UPPER_SLAB,BACK+1,EAST-1,UPPER_SLAB,FRONT-1,floor(Pair.ROOM));
        box(l,b,-1,UPPER_SLAB,-28,-1,UPPER_SLAB,-1,floor(Pair.RUNNER_W));box(l,b,0,UPPER_SLAB,-28,0,UPPER_SLAB,-1,floor(Pair.RUNNER));box(l,b,1,UPPER_SLAB,-28,1,UPPER_SLAB,-1,floor(Pair.RUNNER_E));
        box(l,b,-13,UPPER_SLAB,-28,-3,UPPER_SLAB,-17,floor(Pair.WORK));box(l,b,3,UPPER_SLAB,-28,13,UPPER_SLAB,-17,floor(Pair.CARPET));
        box(l,b,PASS_W,UPPER_SLAB,PASS_N,PASS_W,UPPER_SLAB,-1,floor(Pair.PASSAGE));box(l,b,PASS_E,UPPER_SLAB,PASS_N,PASS_E,UPPER_SLAB,-1,floor(Pair.PASSAGE));
        box(l,b,PASS_W,UPPER_SLAB,PASS_N,PASS_E,UPPER_SLAB,PASS_N,floor(Pair.PASSAGE));
        box(l,b,WEST+1,ROOF,BACK+1,EAST-1,ROOF,FRONT-1,floor(Pair.TOP));
        box(l,b,PASS_W,ROOF,PASS_N,PASS_W,ROOF,-1,floor(Pair.TOP_PASSAGE));box(l,b,PASS_E,ROOF,PASS_N,PASS_E,ROOF,-1,floor(Pair.TOP_PASSAGE));
        box(l,b,PASS_W,ROOF,PASS_N,PASS_E,ROOF,PASS_N,floor(Pair.TOP_PASSAGE));
    }

    private static void groundWalls(ServerLevel l,BlockPos b){
        int y0=GROUND,y1=GROUND+3;
        // The front wall's inner faces: the parlor's, the hall's, the dining room's.
        wall(l,b,-13,FRONT,-5,FRONT,y0,y1,LodgingWall.Pair.PARLOR_LATH,Direction.NORTH);wall(l,b,-3,FRONT,3,FRONT,y0,y1,LodgingWall.Pair.HALL_LATH,Direction.NORTH);
        wall(l,b,5,FRONT,13,FRONT,y0,y1,LodgingWall.Pair.DINING_LATH,Direction.NORTH);
        // The rooms' outside walls, the passages behind them.
        wall(l,b,ROOM_W,-1,ROOM_W,-12,y0,y1,LodgingWall.Pair.PARLOR_LATH,Direction.EAST);wall(l,b,ROOM_W,BACK_S,ROOM_W,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_LATH,Direction.EAST);
        wall(l,b,ROOM_W,-17,ROOM_W,ROOM_N,y0,y1,LodgingWall.Pair.DANIEL_LATH,Direction.EAST);
        wall(l,b,ROOM_E,-1,ROOM_E,-12,y0,y1,LodgingWall.Pair.DINING_LATH,Direction.WEST);wall(l,b,ROOM_E,BACK_S,ROOM_E,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_LATH,Direction.WEST);
        wall(l,b,ROOM_E,-17,ROOM_E,ROOM_N,y0,y1,LodgingWall.Pair.SCULLERY_LATH,Direction.WEST);
        wall(l,b,-13,ROOM_N,-2,ROOM_N,y0,y1,LodgingWall.Pair.DANIEL_LATH,Direction.SOUTH);wall(l,b,-1,ROOM_N,13,ROOM_N,y0,y1,LodgingWall.Pair.SCULLERY_LATH,Direction.SOUTH);
        // The hall's sides, and the front rooms' backs on the back passage.
        wall(l,b,-4,-1,-4,HALL_N,y0,y1,LodgingWall.Pair.HALL_PARLOR,Direction.EAST);wall(l,b,4,-1,4,HALL_N,y0,y1,LodgingWall.Pair.HALL_DINING,Direction.WEST);
        wall(l,b,-13,HALL_N,-5,HALL_N,y0,y1,LodgingWall.Pair.HALL_PARLOR,Direction.NORTH);wall(l,b,5,HALL_N,13,HALL_N,y0,y1,LodgingWall.Pair.HALL_DINING,Direction.NORTH);
        set(l,b,-4,y0,HALL_N,LodgingWall.of(LodgingWall.Pair.HALL_PARLOR,Direction.NORTH,Part.LOW));set(l,b,4,y0,HALL_N,LodgingWall.of(LodgingWall.Pair.HALL_DINING,Direction.NORTH,Part.LOW));
        for(int y=y0+1;y<=y1;y++){set(l,b,-4,y,HALL_N,LodgingWall.of(LodgingWall.Pair.HALL_PARLOR,Direction.NORTH,Part.MID));set(l,b,4,y,HALL_N,LodgingWall.of(LodgingWall.Pair.HALL_DINING,Direction.NORTH,Part.MID));}
        // The back passage's north side: Daniel's wall, the back stair's arch, the scullery's.
        wall(l,b,-13,BACK_WALL,-2,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_DANIEL,Direction.SOUTH);wall(l,b,2,BACK_WALL,13,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_SCULLERY,Direction.SOUTH);
        wall(l,b,-1,BACK_WALL,1,BACK_WALL,y1,y1,LodgingWall.Pair.HALL_DISTEMPER,Direction.SOUTH);
        // Daniel's east wall, on the back stair and then on the scullery's alcove.
        wall(l,b,-2,-17,-2,WELL_N,y0,y1,LodgingWall.Pair.DANIEL_DISTEMPER,Direction.WEST);wall(l,b,-2,WELL_WALL,-2,-28,y0,y1,LodgingWall.Pair.DANIEL_SCULLERY,Direction.WEST);
        // The back stair's east wall and its north wall, the scullery behind both.
        wall(l,b,2,-17,2,WELL_N,y0,y1,LodgingWall.Pair.SCULLERY_DISTEMPER,Direction.EAST);wall(l,b,-1,WELL_WALL,2,WELL_WALL,y0,y1,LodgingWall.Pair.SCULLERY_DISTEMPER,Direction.NORTH);
        // The doors and hatches in them.
        leaf(l,b,PARLOR_DOOR,Kind.DOOR,Direction.EAST,false,0);leaf(l,b,DINING_DOOR,Kind.DOOR,Direction.WEST,false,0);
        leaf(l,b,DANIEL_DOOR,Kind.BOLT_DOOR,Direction.SOUTH,false,4);leaf(l,b,SCULLERY_DOOR,Kind.DOOR,Direction.SOUTH,false,0);
        leaf(l,b,JIB,Kind.JIB,Direction.EAST,false,0);leaf(l,b,SCULLERY_BACK,Kind.SERVICE,Direction.SOUTH,false,0);leaf(l,b,SCULLERY_SIDE,Kind.SERVICE,Direction.WEST,false,0);
        hatch(l,b,PARLOR_HATCH,LodgingWall.Pair.PARLOR_LATH,Direction.EAST);hatch(l,b,DINING_HATCH,LodgingWall.Pair.DINING_LATH,Direction.WEST);
        hatch(l,b,FOOD_HATCH,LodgingWall.Pair.DANIEL_LATH,Direction.EAST);
        // The windows in the front wall, shuttered from inside.
        for(int x:new int[]{-11,-7,7,11}){set(l,b,x,1,FRONT,fit(Kind.WINDOW,Direction.NORTH,0));set(l,b,x,2,FRONT,fit(Kind.WINDOW,Direction.NORTH,1));}
        // The parlor's chimney breast in its back wall, the grate in the middle of it.
        plain(l,b,-10,HALL_N+1,-8,HALL_N+1,y0,y1,"paper_parlor");
        set(l,b,-9,y0,HALL_N+1,fit(Kind.FIREPLACE,Direction.SOUTH,0));set(l,b,-9,y0+1,HALL_N+1,fit(Kind.FIREPLACE,Direction.SOUTH,1));
        // Daniel's frame in his back wall.
        frame(l,b,DANIEL_FRAME,Direction.SOUTH,true);
    }

    private static void upperWalls(ServerLevel l,BlockPos b){
        int y0=UPPER,y1=UPPER+3;
        wall(l,b,-13,FRONT,-5,FRONT,y0,y1,LodgingWall.Pair.ROOM_LATH,Direction.NORTH);wall(l,b,-3,FRONT,3,FRONT,y0,y1,LodgingWall.Pair.HALL_LATH,Direction.NORTH);
        wall(l,b,5,FRONT,13,FRONT,y0,y1,LodgingWall.Pair.ROOM_LATH,Direction.NORTH);
        wall(l,b,ROOM_W,-1,ROOM_W,BACK_WALL,y0,y1,LodgingWall.Pair.ROOM_LATH,Direction.EAST);wall(l,b,ROOM_W,-17,ROOM_W,ROOM_N,y0,y1,LodgingWall.Pair.WORK_LATH,Direction.EAST);
        wall(l,b,ROOM_E,-1,ROOM_E,BACK_WALL,y0,y1,LodgingWall.Pair.ROOM_LATH,Direction.WEST);wall(l,b,ROOM_E,-17,ROOM_E,ROOM_N,y0,y1,LodgingWall.Pair.VERA_LATH,Direction.WEST);
        wall(l,b,-13,ROOM_N,-2,ROOM_N,y0,y1,LodgingWall.Pair.WORK_LATH,Direction.SOUTH);wall(l,b,-1,ROOM_N,1,ROOM_N,y0,y1,LodgingWall.Pair.HALL_LATH,Direction.SOUTH);
        wall(l,b,2,ROOM_N,13,ROOM_N,y0,y1,LodgingWall.Pair.VERA_LATH,Direction.SOUTH);
        // The gallery's walls, the rooms behind them.
        wall(l,b,-4,-1,-4,GALLERY_N,y0,y1,LodgingWall.Pair.HALL_ROOM,Direction.EAST);wall(l,b,4,-1,4,GALLERY_N,y0,y1,LodgingWall.Pair.HALL_ROOM,Direction.WEST);
        // The partitions between the guest rooms, papered alike on both sides.
        for(int z:new int[]{-5,-11}){plain(l,b,-13,z,-5,z,y0,y1,"paper_room");plain(l,b,5,z,13,z,y0,y1,"paper_room");}
        // North of the gallery: the third rooms' backs on the workroom and Vera's room, the gallery narrowing to the corridor.
        wall(l,b,-13,BACK_WALL,-4,BACK_WALL,y0,y1,LodgingWall.Pair.ROOM_WORK,Direction.SOUTH);wall(l,b,4,BACK_WALL,13,BACK_WALL,y0,y1,LodgingWall.Pair.ROOM_VERA,Direction.SOUTH);
        wall(l,b,-3,BACK_WALL,-2,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_WORK,Direction.SOUTH);wall(l,b,2,BACK_WALL,3,BACK_WALL,y0,y1,LodgingWall.Pair.HALL_VERA,Direction.SOUTH);
        wall(l,b,-2,-17,-2,-28,y0,y1,LodgingWall.Pair.HALL_WORK,Direction.EAST);wall(l,b,2,-17,2,-28,y0,y1,LodgingWall.Pair.HALL_VERA,Direction.WEST);
        // Doors: the six rooms, the workroom, Vera's, the corridor's end into the back passage.
        for(int i=0;i<6;i++)leaf(l,b,ROOM_DOORS[i],Kind.DOOR,i<3?Direction.EAST:Direction.WEST,false,0);
        leaf(l,b,WORK_DOOR,Kind.DOOR,Direction.EAST,false,0);leaf(l,b,VERA_DOOR,Kind.DOOR,Direction.WEST,false,0);leaf(l,b,UPPER_BACK,Kind.SERVICE,Direction.SOUTH,false,0);
        // Each room's frame, and its hatch from the passage.
        for(int i=0;i<6;i++){
            frame(l,b,frame(i),frameFacing(i),false);
            hatch(l,b,new BlockPos(i<3?ROOM_W:ROOM_E,UPPER+1,HATCH_Z[i%3]),LodgingWall.Pair.ROOM_LATH,frameFacing(i));
        }
        // The front windows: the first two rooms' and the gallery's.
        for(int x:new int[]{-9,0,9}){set(l,b,x,UPPER+1,FRONT,fit(Kind.WINDOW,Direction.NORTH,0));set(l,b,x,UPPER+2,FRONT,fit(Kind.WINDOW,Direction.NORTH,1));}
    }

    private static void stairs(ServerLevel l,BlockPos b){
        // The main stair, x 2 and 3, five treads rising north from the hall to the gallery; the gallery's floor open over its upper half.
        var up=LodgingRegistry.STAIR.get().defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
        for(int i=0;i<5;i++)for(int x=2;x<=3;x++)set(l,b,x,GROUND+i,STAIR_FOOT-i,up);
        box(l,b,2,UPPER_SLAB,STAIR_FOOT-2,3,UPPER_SLAB,STAIR_FOOT-3,AIR);
        // Under it, the panelling and the cupboard behind its low door; its inside is the stair's east half, two and three cells high.
        box(l,b,2,GROUND,STAIR_FOOT-1,3,GROUND,STAIR_FOOT-1,mat("panel"));
        box(l,b,2,GROUND,STAIR_FOOT-2,2,GROUND+1,STAIR_FOOT-2,mat("panel"));box(l,b,2,GROUND+2,STAIR_FOOT-3,2,GROUND+2,STAIR_FOOT-3,mat("panel"));
        box(l,b,2,GROUND,STAIR_FOOT-4,2,GROUND+3,STAIR_FOOT-4,mat("panel"));box(l,b,3,GROUND,STAIR_FOOT-5,3,GROUND+3,STAIR_FOOT-5,mat("panel"));
        leaf(l,b,CUPBOARD,Kind.CUPBOARD,Direction.WEST,false,0);
        // Its handrail up the open side, the newel at its foot; the gallery's rail round the well.
        set(l,b,1,GROUND,STAIR_FOOT,fit(Kind.BANISTER,Direction.NORTH,1));
        for(int i=0;i<4;i++)set(l,b,2,GROUND+1+i,STAIR_FOOT-i,fit(Kind.BANISTER,Direction.NORTH,0));
        set(l,b,2,UPPER,STAIR_FOOT-2,fit(Kind.RAIL,Direction.WEST,1));set(l,b,3,UPPER,STAIR_FOOT-2,fit(Kind.RAIL,Direction.SOUTH,0));
        set(l,b,2,UPPER,STAIR_FOOT-3,fit(Kind.RAIL,Direction.WEST,0));
        // The back stair down, x -1 to 1: four treads descending north from the well's floor, the first set into it, the floor open over
        // the next two so a body going down has its head clear.
        var down=LodgingRegistry.CELLAR_STAIR.get().defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
        for(int i=0;i<CELLAR_TREADS;i++)for(int x=-1;x<=1;x++)set(l,b,x,GROUND_SLAB-i,CELLAR_STAIR_TOP-i,down);
        box(l,b,-1,GROUND_SLAB,CELLAR_STAIR_TOP-1,1,GROUND_SLAB,CELLAR_STAIR_TOP-2,AIR);
        // Bricked in underneath, as a stair down a cellar is.
        for(int i=0;i<CELLAR_TREADS-1;i++)box(l,b,-1,CELLAR,CELLAR_STAIR_TOP-i,1,GROUND_SLAB-1-i,CELLAR_STAIR_TOP-i,mat("brick"));
        // The ladders at the side passages' front ends, from the cellar's floor to the upper passage, up against the front wall.
        for(int y=CELLAR;y<=UPPER+3;y++){set(l,b,PASS_W,y,LADDER_Z,fit(Kind.LADDER,Direction.NORTH,0));set(l,b,PASS_E,y,LADDER_Z,fit(Kind.LADDER,Direction.NORTH,0));}
    }

    private static void cellar(ServerLevel l,BlockPos b){
        // Brick piers under the hall's walls and the back rooms.
        for(int[] p:new int[][]{{-4,-6},{4,-6},{-4,-14},{4,-14},{-7,-24},{7,-24}})box(l,b,p[0],CELLAR,p[1],p[0],CELLAR+2,p[1],mat("brick"));
        // The sill, round three sides under the frames' walls.
        for(int z=-1;z>=ROOM_N;z--){set(l,b,ROOM_W,CELLAR,z,fit(Kind.SILL,Direction.NORTH,0));set(l,b,ROOM_E,CELLAR,z,fit(Kind.SILL,Direction.NORTH,0));}
        for(int x=ROOM_W+1;x<ROOM_E;x++)set(l,b,x,CELLAR,ROOM_N,fit(Kind.SILL,Direction.EAST,0));
        // A post on the sill under every frame, its foot in an iron shoe on the sill with the wedge driven beside it.
        for(int i=0;i<6;i++){var f=frame(i);set(l,b,f.getX(),CELLAR,f.getZ(),fit(Kind.SHOE,Direction.NORTH,0));set(l,b,f.getX(),CELLAR+1,f.getZ(),fit(Kind.POST,Direction.NORTH,0));set(l,b,f.getX(),CELLAR+2,f.getZ(),fit(Kind.POST,Direction.NORTH,0));}
        set(l,b,DANIEL_FRAME.getX(),CELLAR,ROOM_N,fit(Kind.SHOE,Direction.EAST,0));set(l,b,DANIEL_FRAME.getX(),CELLAR+1,ROOM_N,fit(Kind.POST,Direction.EAST,0));set(l,b,DANIEL_FRAME.getX(),CELLAR+2,ROOM_N,fit(Kind.POST,Direction.EAST,0));
        // The ratchet that holds the sill, in the middle of the back run.
        set(l,b,RATCHET,fit(Kind.RATCHET,Direction.SOUTH,0));set(l,b,RATCHET.above(),fit(Kind.RATCHET,Direction.SOUTH,1));
        // The dead post, standing in its shoe on the bare floor.
        set(l,b,DEAD_POST,fit(Kind.SHOE,Direction.EAST,1));set(l,b,DEAD_POST.above(),fit(Kind.POST,Direction.EAST,1));set(l,b,DEAD_POST.above(2),fit(Kind.POST,Direction.EAST,0));
    }

    private static void fittings(ServerLevel l,BlockPos b){
        // The hall: hooks by the door, the table with the terms, the stand with its glass; the bench across from them.
        set(l,b,-3,2,-1,fit(Kind.COAT_RAIL,Direction.EAST,0));set(l,b,-3,2,-2,fit(Kind.COAT_RAIL,Direction.EAST,0));
        set(l,b,SOURCE,fit(Kind.HALL_TABLE,Direction.EAST,0));
        set(l,b,HALL_STAND,fit(Kind.HALL_STAND,Direction.EAST,0));set(l,b,HALL_STAND.above(),fit(Kind.HALL_STAND,Direction.EAST,1));
        set(l,b,ENDING,fit(Kind.BENCH,Direction.WEST,0));set(l,b,ENDING.north(),fit(Kind.BENCH,Direction.WEST,1));
        // The guest rooms' numbers over their doors.
        for(int i=0;i<6;i++){var d=ROOM_DOORS[i];set(l,b,d.getX()+(i<3?1:-1),UPPER+2,d.getZ(),fit(Kind.NUMBER,i<3?Direction.EAST:Direction.WEST,i));}
        // The marks on Daniel's east wall.
        for(int z=-24;z>=-28;z--)for(int y=GROUND;y<=GROUND+3;y++)set(l,b,-3,y,z,fit(Kind.TALLY,Direction.WEST,Math.floorMod(z*3+y*5,8)));
        // Lights: bulbs on their flexes, the cellar's and Daniel's turned low; none in the passages.
        for(var at:LAMPS)set(l,b,at,fit(Kind.LAMP,Direction.NORTH,0));
        for(var at:LOW_LAMPS)set(l,b,at,fit(Kind.LAMP,Direction.NORTH,1));
    }
    // ------------------------------------------------------------------------------------------------ what is in the rooms (0.4.550)
    /** Guest room i's bed: its head cell. The front rooms' beds stand with their heads to the front wall, the others' to the room's back. */
    public static BlockPos bed(int room){int side=room<3?-1:1;int k=room%3;return new BlockPos(side*9,UPPER,k==0?ROOM_Z[0][0]:ROOM_Z[k][1]);}
    /** Which way guest room i's bed points, head to foot. */
    public static Direction bedFacing(int room){return room%3==0?Direction.NORTH:Direction.SOUTH;}
    /** Guest room i's washstand, against the room's back wall by the door. */
    public static BlockPos washstand(int room){return new BlockPos(room<3?-6:6,UPPER,ROOM_Z[room%3][1]);}
    /** Guest room i's chair, close by the bed's head, turned to it. */
    public static BlockPos guestChair(int room){return new BlockPos(room<3?-11:11,UPPER,bed(room).getZ());}
    /** The holder for guest room i's card, on the gallery's wall beside its door. */
    public static BlockPos cardHolder(int room){var d=ROOM_DOORS[room];return new BlockPos(d.getX()+(room<3?1:-1),UPPER+1,d.getZ()+1);}
    /** The parlor: the settee facing the grate (its left half), the armchairs either side of it, the small table, the waiting chairs. */
    public static final BlockPos SETTEE=new BlockPos(-9,0,-7),SIDE_TABLE=new BlockPos(-8,0,-7);
    public static final List<BlockPos> ARMCHAIRS=List.of(new BlockPos(-11,0,-9),new BlockPos(-7,0,-9)),PARLOR_CHAIRS=List.of(new BlockPos(-13,0,-3),new BlockPos(-5,0,-3));
    /** The dining room's long table, two cells by four, and the sideboard against its back wall (its left half). */
    public static final int TABLE_W=8,TABLE_E=9,TABLE_S=-4,TABLE_N=-7;
    public static final BlockPos SIDEBOARD=new BlockPos(8,0,-11);
    /** Daniel's room: the cot (its head), the shelf under the food hatch, his chair, the pail. */
    public static final BlockPos COT=new BlockPos(-13,0,ROOM_N+1),SHELF=FOOD_HATCH.below().east(),DANIEL_CHAIR=new BlockPos(-7,0,-25),DANIEL_PAIL=new BlockPos(-4,0,ROOM_N+1);
    /** The scullery: the tray laid on the table's near half, the range, the sink, the dresser, the pail. */
    public static final BlockPos TRAY=new BlockPos(8,0,-22),RANGE=new BlockPos(13,0,-20),SINK=new BlockPos(6,0,ROOM_N+1),DRESSER=new BlockPos(3,0,-18),SCULLERY_PAIL=new BlockPos(4,0,ROOM_N+1);
    /** The workroom: the table of preparations (its left half), its chair, the record of guests, the key's hook by the door, the cupboard. */
    public static final BlockPos WORK_TABLE=new BlockPos(-9,UPPER,-23),WORK_CHAIR=new BlockPos(-9,UPPER,-22),RECORD=new BlockPos(-6,UPPER,ROOM_N+1),KEY_HOOK=new BlockPos(-3,UPPER+1,-21),
        WORK_CUPBOARD=new BlockPos(-13,UPPER,-26);
    /** Vera's room: her bed (its head), the dressing table and its chair, the wardrobe. */
    public static final BlockPos VERA_BED=new BlockPos(13,UPPER,-26),DRESSING_TABLE=new BlockPos(8,UPPER,ROOM_N+1),VERA_CHAIR=new BlockPos(8,UPPER,-27),VERA_WARDROBE=new BlockPos(3,UPPER,-25);
    /** The cellar: the coil of rope by the dead post, the crates along the back, the coal. */
    public static final BlockPos ROPE=DEAD_POST.west(),COAL=new BlockPos(5,CELLAR,-26);
    public static final List<BlockPos> CRATES=List.of(new BlockPos(11,CELLAR,-27),new BlockPos(12,CELLAR,-27),new BlockPos(-12,CELLAR,-27));

    static BlockState thing(LodgingThing.Kind kind,Direction facing,int stage){return LodgingThing.of(kind,facing,stage);}
    /** A thing two cells side by side: half 0 here, half 1 to its right as one faces its front. */
    private static void pair(ServerLevel l,BlockPos b,BlockPos at,LodgingThing.Kind kind,Direction f,int extra){set(l,b,at,thing(kind,f,extra));set(l,b,LodgingThing.right(at,f),thing(kind,f,1|extra));}
    /** A thing two cells end to end: its head here, its foot the cell it points to. */
    private static void ends(ServerLevel l,BlockPos b,BlockPos head,LodgingThing.Kind kind,Direction f,int extra){set(l,b,head,thing(kind,f,extra));set(l,b,LodgingThing.ahead(head,f),thing(kind,f,1|extra));}
    /** A thing two cells high: the upper cell is stage 1. */
    private static void tall(ServerLevel l,BlockPos b,BlockPos at,LodgingThing.Kind kind,Direction f){set(l,b,at,thing(kind,f,0));set(l,b,at.above(),thing(kind,f,1));}

    private static void furnish(ServerLevel l,BlockPos b){
        // The six guest rooms: a bed made and turned down, a washstand with its basin, jug and glass, a hard chair by the bed's head; and
        // outside each door, the holder its guest's card will go in.
        for(int i=0;i<6;i++){
            ends(l,b,bed(i),LodgingThing.Kind.BED,bedFacing(i),0);
            set(l,b,washstand(i),thing(LodgingThing.Kind.WASHSTAND,Direction.SOUTH,0));
            set(l,b,guestChair(i),thing(LodgingThing.Kind.CHAIR,i<3?Direction.EAST:Direction.WEST,0));
            set(l,b,cardHolder(i),thing(LodgingThing.Kind.CARD_HOLDER,i<3?Direction.EAST:Direction.WEST,0));
        }
        // The parlor, where everybody waits together: the settee facing the grate, an armchair either side of it, a small table, and a
        // chair by each window.
        pair(l,b,SETTEE,LodgingThing.Kind.SETTEE,Direction.NORTH,0);set(l,b,SIDE_TABLE,thing(LodgingThing.Kind.SIDE_TABLE,Direction.NORTH,0));
        set(l,b,ARMCHAIRS.get(0),thing(LodgingThing.Kind.ARMCHAIR,Direction.EAST,0));set(l,b,ARMCHAIRS.get(1),thing(LodgingThing.Kind.ARMCHAIR,Direction.WEST,0));
        set(l,b,PARLOR_CHAIRS.get(0),thing(LodgingThing.Kind.CHAIR,Direction.EAST,0));set(l,b,PARLOR_CHAIRS.get(1),thing(LodgingThing.Kind.CHAIR,Direction.WEST,0));
        // The dining room: the long table with its leaves in, ten chairs round it, and the sideboard.
        for(int z=TABLE_N;z<=TABLE_S;z++){
            boolean end=z==TABLE_N||z==TABLE_S;
            set(l,b,TABLE_W,0,z,thing(LodgingThing.Kind.LONG_TABLE,end?(z==TABLE_N?Direction.NORTH:Direction.WEST):Direction.NORTH,end?1:0));
            set(l,b,TABLE_E,0,z,thing(LodgingThing.Kind.LONG_TABLE,end?(z==TABLE_N?Direction.EAST:Direction.SOUTH):Direction.SOUTH,end?1:0));
            set(l,b,TABLE_W-1,0,z,thing(LodgingThing.Kind.CHAIR,Direction.EAST,0));set(l,b,TABLE_E+1,0,z,thing(LodgingThing.Kind.CHAIR,Direction.WEST,0));
        }
        set(l,b,TABLE_W,0,TABLE_N-1,thing(LodgingThing.Kind.CHAIR,Direction.SOUTH,0));set(l,b,TABLE_E,0,TABLE_S+1,thing(LodgingThing.Kind.CHAIR,Direction.NORTH,0));
        pair(l,b,SIDEBOARD,LodgingThing.Kind.SIDEBOARD,Direction.SOUTH,0);
        // Daniel's room, his years: the cot along the wall, the shelf under the food hatch, his chair turned to the frame, a pail.
        ends(l,b,COT,LodgingThing.Kind.COT,Direction.SOUTH,0);
        set(l,b,SHELF,thing(LodgingThing.Kind.SHELF,Direction.EAST,0));
        set(l,b,DANIEL_CHAIR,thing(LodgingThing.Kind.CHAIR,Direction.NORTH,0));set(l,b,DANIEL_PAIL,thing(LodgingThing.Kind.PAIL,Direction.NORTH,0));
        // The scullery, Vera's kitchen: the range in low with its flue to the ceiling, the stone sink, the dresser, and on the table the
        // tray laid for Daniel's hatch.
        set(l,b,RANGE,thing(LodgingThing.Kind.RANGE,Direction.WEST,0));for(int y=1;y<=3;y++)set(l,b,RANGE.above(y),thing(LodgingThing.Kind.RANGE,Direction.WEST,1));
        set(l,b,SINK,thing(LodgingThing.Kind.SINK,Direction.SOUTH,0));tall(l,b,DRESSER,LodgingThing.Kind.DRESSER,Direction.EAST);
        set(l,b,TRAY,thing(LodgingThing.Kind.SCULLERY_TABLE,Direction.EAST,2));set(l,b,LodgingThing.right(TRAY,Direction.EAST),thing(LodgingThing.Kind.SCULLERY_TABLE,Direction.EAST,1));
        set(l,b,SCULLERY_PAIL,thing(LodgingThing.Kind.PAIL,Direction.NORTH,0));
        // The workroom: the table of preparations and its chair, the record of guests, the key on its hook by the door, a tall cupboard.
        pair(l,b,WORK_TABLE,LodgingThing.Kind.WORK_TABLE,Direction.NORTH,0);set(l,b,WORK_CHAIR,thing(LodgingThing.Kind.CHAIR,Direction.NORTH,0));
        set(l,b,RECORD,thing(LodgingThing.Kind.CARD_INDEX,Direction.SOUTH,0));set(l,b,KEY_HOOK,thing(LodgingThing.Kind.KEY_HOOK,Direction.WEST,0));
        tall(l,b,WORK_CUPBOARD,LodgingThing.Kind.WARDROBE,Direction.EAST);
        // Vera's room: her bed made tight, the dressing table and its chair, her wardrobe.
        ends(l,b,VERA_BED,LodgingThing.Kind.BED,Direction.WEST,2);
        set(l,b,DRESSING_TABLE,thing(LodgingThing.Kind.DRESSING_TABLE,Direction.SOUTH,0));set(l,b,VERA_CHAIR,thing(LodgingThing.Kind.CHAIR,Direction.NORTH,0));
        tall(l,b,VERA_WARDROBE,LodgingThing.Kind.WARDROBE,Direction.EAST);
        // The cellar: a coil of rope beside the dead post, crates along the back, the coal.
        set(l,b,ROPE,thing(LodgingThing.Kind.ROPE,Direction.NORTH,0));for(var at:CRATES)set(l,b,at,thing(LodgingThing.Kind.CRATE,Direction.NORTH,0));
        set(l,b,COAL,thing(LodgingThing.Kind.COAL,Direction.NORTH,0));
    }
    /** The bulbs, each hung a cell under its ceiling. */
    public static final List<BlockPos> LAMPS=List.of(new BlockPos(0,3,-3),new BlockPos(0,3,-10),new BlockPos(-9,3,-6),new BlockPos(9,3,-6),new BlockPos(-8,3,-14),
        new BlockPos(8,3,-14),new BlockPos(0,3,-18),new BlockPos(8,3,-24),new BlockPos(0,8,-3),new BlockPos(0,8,-13),new BlockPos(0,8,-24),new BlockPos(-9,8,-2),
        new BlockPos(-9,8,-8),new BlockPos(-9,8,-13),new BlockPos(9,8,-2),new BlockPos(9,8,-8),new BlockPos(9,8,-13),new BlockPos(-8,8,-22),new BlockPos(8,8,-22));
    public static final List<BlockPos> LOW_LAMPS=List.of(new BlockPos(-8,3,-22),new BlockPos(0,-2,-23),new BlockPos(-8,-2,-10),new BlockPos(9,-2,-12));

    // ------------------------------------------------------------------------------------------------ doors
    /** Whether the door whose lower cell is here stands open. */
    public static boolean open(ServerLevel l,BlockPos b,BlockPos at){return LodgingBlock.open(l.getBlockState(b.offset(at)));}
    /** The lower cell of the door a cell belongs to, or null if it is none. */
    public static BlockPos doorAt(ServerLevel l,BlockPos b,BlockPos rel){
        var s=l.getBlockState(b.offset(rel));if(!s.is(LodgingRegistry.FITTING.get())||!s.getValue(LodgingBlock.KIND).leaf())return null;
        return (s.getValue(LodgingBlock.STAGE)&2)==2?rel.below():rel;
    }
    /** Every door in the house (lower cells), the guest rooms' included. */
    public static List<BlockPos> doors(){
        var out=new java.util.ArrayList<BlockPos>(List.of(PARLOR_DOOR,DINING_DOOR,DANIEL_DOOR,SCULLERY_DOOR,JIB,CUPBOARD,SCULLERY_BACK,SCULLERY_SIDE,WORK_DOOR,VERA_DOOR,UPPER_BACK));
        out.addAll(List.of(ROOM_DOORS));return out;
    }
}
