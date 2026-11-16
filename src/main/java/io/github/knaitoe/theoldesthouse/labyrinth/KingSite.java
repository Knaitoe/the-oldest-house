package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.641, A sort of thing, part 1 (docs/SLEEPING_KING_PLAN.md, docs/SLEEPING_KING_0_4_641.md): the place, built whole. Every position
 * is relative to the base; north is into the scene.
 * <p>
 * The House's door opens into the drawing-room (x -6 to 6, z -1 to -12, floor 0, ceiling 5): the chimney-piece against the north wall
 * with the fire in its grate and the looking-glass over it, three wide and two high; the hearthrug before it, the chessmen fallen in
 * the hearth, the little table with the chess problem on it printed backwards, the armchair, a tall window in each side wall. The
 * room's north wall is the glass (z -13.5): behind it the same room the other way round (z -15 to -26), its things reflected across
 * that plane, and in its far wall, where the House's door is in the first, a doorway onto the board.
 * <p>
 * The board is a strip of three files and eight ranks of sixteen-block squares, light and dark turf by parity, ruled from the house
 * northward: rank r begins at z {@link #rankNear(int)}, and two rows of brook lie between each rank and the next. Hedges divide the
 * files (x -9 and 8) with a gap in each rank; a gravel road runs up the middle file over footbridges. Around the strip the country is
 * ruled to x ±64. The fourth rank is the wood, and the King's tree stands in its east file.
 * <p>
 * The land is drawn as the proposal's deep sleep has it: the third to fifth ranks filled, the first, seventh and eighth ruled, and the
 * second and sixth the band between, a block at a time.
 */
public final class KingSite {
    private KingSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int CEIL=5,RANKS=8,SQUARE=16,PITCH=18,NEAR=-30,WEST=-25,EAST=24,COUNTRY=64;
    /**
     * 0.4.656: how far past either side hedge the scene still holds a reader, as its room holds them past the far hedge. Off the board
     * they are nowhere (KingBreath): the King's own rule puts them back, or lets them leave him his sleep. Until 0.4.656 the scene
     * ended at the side hedges, so a reader who went through one was no longer in it and nothing caught them.
     */
    public static final int OFF_BOARD=6;
    /** The glass's plane, between the drawing-room and the room behind it. */
    public static final double MIRROR_Z=-13.5;
    // ------------------------------------------------------------------------------------------------ the drawing-room
    public static final BlockPos FIRE=new BlockPos(0,0,-12),RUG=new BlockPos(0,0,-9),CHESSMEN=new BlockPos(1,0,-11),TABLE=new BlockPos(-4,0,-7),
        PROBLEM=new BlockPos(-4,1,-7),ARMCHAIR=new BlockPos(-4,0,-9);
    /** Where the reader's way on is: a door in the drawing-room's east wall, between the window and the fire. */
    public static final BlockPos ONWARD=new BlockPos(7,0,-9);
    /** The chess problem is the first paper. */
    public static final BlockPos SOURCE=PROBLEM;
    // ------------------------------------------------------------------------------------------------ the board
    /** The doorway out of the room behind the glass. */
    public static final BlockPos GARDEN_DOOR=new BlockPos(0,0,-27);
    /** The King's tree, on the fourth rank's east file, and where he lies under it. */
    public static final BlockPos TREE=new BlockPos(16,0,-91),KING=new BlockPos(16,0,-86);
    /** 0.4.643: the account waits on the drawing-room's armchair, where a reader comes to after either ending. */
    public static final BlockPos ENDING=ARMCHAIR;
    /** 0.4.643: where a reader comes to in the drawing-room, between the armchair and the fire, facing the glass. */
    public static final net.minecraft.world.phys.Vec3 COME_TO=new net.minecraft.world.phys.Vec3(-2.5,0,-8.5);
    /**
     * 0.4.643: the brothers' rattle, lying spoiled on his square between him and the hedge's gap; the two of them stand arm in arm just
     * inside the gap, facing along the road, Tweedledum to the south.
     */
    public static final BlockPos RATTLE=new BlockPos(13,0,-88),DUM=new BlockPos(10,0,-90),DEE=new BlockPos(10,0,-91);

    /** The z the rank (1 to 8) begins at, nearest the house, and the last z it covers. */
    /**
     * 0.4.644, part 4. The hill on the second square, in its west file; the Queen waits on the road at its foot and, once she has let
     * go of a reader, stands at the far side of the field looking back at the peg. The glass table with the bottle and the cake stands
     * by the road. On the third square, in the east file, the railway carriage: its door faces the hedge's gap, two seats face each
     * other inside, and a reader whose ride is over is put down on the road at the near edge of the fourth square.
     */
    public static final BlockPos HILL=new BlockPos(-17,0,-55),QUEEN=new BlockPos(-3,0,-51),QUEEN_FAR=new BlockPos(2,0,-62),GLASS_TABLE=new BlockPos(5,0,-54);
    public static final int HILL_RADIUS=7,HILL_HEIGHT=3;
    public static final int CAR_WEST=11,CAR_EAST=22,CAR_NORTH=-74,CAR_SOUTH=-70;
    public static final BlockPos CAR_DOOR=new BlockPos(11,0,-72),GUARD=new BlockPos(16,0,-72);
    public static final BlockPos[] SEATS={new BlockPos(13,0,-71),new BlockPos(13,0,-72),new BlockPos(13,0,-73),new BlockPos(20,0,-71),new BlockPos(20,0,-72),new BlockPos(20,0,-73)};
    public static final net.minecraft.world.phys.Vec3 RIDE_OUT=new net.minecraft.world.phys.Vec3(1.5,0,-85.5);
    /**
     * 0.4.645, "Who are you?": the mushroom in the west file at the far edge of the fifth square, where the fill gives out, with the
     * Caterpillar on its cap; and the drawn bridge that carries the road over the brook beyond, and the bank on the near side of it
     * where a thing that has gone through is set back.
     */
    public static final BlockPos MUSHROOM=new BlockPos(-16,0,-114);
    public static final net.minecraft.world.phys.Vec3 BANK=new net.minecraft.world.phys.Vec3(0,0,-116.5);
    /** The drawn bridge's two rows (the brook beyond the fifth rank), across the road at x -1 and 0. */
    public static boolean onDrawnBridge(Vec3 r){int z=(int)Math.floor(r.z);return r.x>=-1.4&&r.x<1.4&&(z==rankFar(5)-1||z==rankFar(5)-2);}
    /** The Queen's peg on a square (2 to 8): beside the road, just past the crossing. */
    /**
     * 0.4.646: the court of cards on the sixth square. The herald waits at the far bank of the drawn bridge; the dock is on the road; the
     * King and Queen of Hearts stand at the far end of it; jurors stand in two rows either side, and a row of the pack's guard stands
     * across the middle file just short of the brook, faces to the court. The lawn is the east file, and the Cat's tree stands on it.
     */
    public static final BlockPos COURT_KING=new BlockPos(-3,0,-130),COURT_QUEEN=new BlockPos(2,0,-130),HERALD=new BlockPos(-3,0,-121);
    public static final int GUARD_ROW=-133,JURY_WEST=-6,JURY_EAST=5,JURY_NEAR=-123,JURY_FAR=-128;
    public static final net.minecraft.world.phys.Vec3 DOCK=new net.minecraft.world.phys.Vec3(.5,0,-126.5);
    public static final BlockPos CAT_TREE=new BlockPos(18,0,-127);
    /** Where the Cat's head is, in its tree and over the court (the actor's feet: the head is a block and three quarters up). */
    public static final net.minecraft.world.phys.Vec3 CAT_IN_TREE=new net.minecraft.world.phys.Vec3(15.5,3.45,-126.5),CAT_OVER=new net.minecraft.world.phys.Vec3(-.5,2.6,-131.5);
    /** Whether a place is in the court: the sixth square's middle file, from the bank to the guard. */
    public static boolean inCourt(Vec3 r){return rank((int)Math.floor(r.z))==6&&file((int)Math.floor(r.x))==2;}
    /** Whether a reader has got past the court's guard, toward the brook. */
    public static boolean pastCourt(Vec3 r){return r.z<GUARD_ROW-.4&&r.z>=rankFar(6)-3&&file((int)Math.floor(r.x))==2;}

    /**
     * 0.4.647, the forest on the seventh square. The Knight waits on his horse a little east of the road beside the square's peg and rides
     * with the readers to the last brook, a pace east of the road ({@link #KNIGHT_LANE}). The road turns there: a side path leaves it
     * eastward along the square's far edge ({@link #TURN_Z}), through the hedge, into the east file and in among a stand of trees that hides
     * its end ({@link #TURN_END}).
     */
    public static final BlockPos KNIGHT_WAIT=new BlockPos(4,0,-141);
    public static final double KNIGHT_LANE=3.5;
    public static final int TURN_Z=-151;
    public static final net.minecraft.world.phys.Vec3 TURN_END=new net.minecraft.world.phys.Vec3(19.5,0,-150.5);
    /** Whether a place is on the turn's path (the two rows of it, from the road to its end among the trees). */
    public static boolean onTurn(int x,int z){return (z==TURN_Z||z==TURN_Z-1)&&x>=1&&x<=(int)TURN_END.x+1;}

    public static BlockPos peg(int rank){return rank==RANKS?FEAST_HEAD:new BlockPos(2,0,rankNear(rank)-1);}
    /**
     * 0.4.648: the feast on the eighth square. A long table on the road down the middle file, two blocks wide from FEAST_NEAR to FEAST_FAR,
     * with three high-backed chairs down each side and one at its foot; the dishes on the table (FEAST_DISHES, with their kinds); and its
     * head, beyond the far end, where the square's peg stands.
     */
    public static final int FEAST_NEAR=-160,FEAST_FAR=-166;
    public static final BlockPos FEAST_HEAD=new BlockPos(0,0,-167),FEAST_FOOT=new BlockPos(-1,0,-159);
    /** The side chairs, west then east, near to far, and the foot chair. */
    public static final BlockPos[] FEAST_CHAIRS={new BlockPos(-2,0,-161),new BlockPos(-2,0,-163),new BlockPos(-2,0,-165),new BlockPos(1,0,-161),new BlockPos(1,0,-163),new BlockPos(1,0,-165),FEAST_FOOT};
    public static final BlockPos[] FEAST_DISHES={new BlockPos(-1,0,-161),new BlockPos(-1,0,-163),new BlockPos(-1,0,-165),new BlockPos(0,0,-162)};
    public static final KingBlock.Kind[] DISH_KINDS={KingBlock.Kind.CANDLES,KingBlock.Kind.PUDDING,KingBlock.Kind.CANDLES,KingBlock.Kind.MUTTON};
    public static boolean onFeast(int x,int z){return (x==-1||x==0)&&z<=FEAST_NEAR&&z>=FEAST_FAR;}
    /** The hill's height over a column, 0 off it. */
    public static int hill(int x,int z){double d=Math.hypot(x-HILL.getX(),z-HILL.getZ());return d>=HILL_RADIUS?0:(int)Math.round(HILL_HEIGHT*(1-d/HILL_RADIUS)+.25);}
    public static boolean inCarriage(net.minecraft.world.phys.Vec3 r){return r.x>=CAR_WEST&&r.x<CAR_EAST+1&&r.z>=CAR_NORTH&&r.z<CAR_SOUTH+1&&r.y>=-.5&&r.y<4;}
    public static int rankNear(int r){return NEAR-(r-1)*PITCH;}
    public static int rankFar(int r){return rankNear(r)-SQUARE+1;}
    /** The rank a z lies in, 0 if in a brook or off the board. */
    public static int rank(int z){for(int r=1;r<=RANKS;r++)if(z<=rankNear(r)&&z>=rankFar(r))return r;return 0;}
    /** The file an x lies in (1 west, 2 middle, 3 east), 0 for a hedge or off the strip. */
    public static int file(int x){return x>=WEST&&x<=-10?1:x>=-8&&x<=7?2:x>=9&&x<=EAST?3:0;}
    /** Whether the square at (rank, file) is the light one. */
    public static boolean light(int rank,int file){return (rank+file)%2==0;}

    /** The proposal's deep sleep: how ruled the land is at z, from 0 (filled) to 1 (ruled). */
    public static double ruledness(int z){
        double third=rankNear(3)+.5,sixth=rankNear(6),seventh=rankNear(7)+.5,second=rankNear(2);
        if(z>third)return z>second?1:Mth.clamp((z-third)/(second-third+.5),0,1);
        if(z<sixth+.5){if(z<seventh)return 1;return Mth.clamp((sixth+.5-z)/(sixth+.5-seventh),0,1);}
        return 0;
    }
    /**
     * 0.4.643: how ruled the land at z is at each depth of his sleep (KingSleep): deep (0) is the proposal's table above; lightly (1) fills
     * only the fourth rank, with the third and fifth the band; barely (2) and awake (3) are handled across the board by
     * {@link #ruledness(int,int,int,int)}.
     */
    public static double ruledness(int z,int level){
        if(level<=0)return ruledness(z);
        double near=rankNear(4)+.5,far=rankNear(5)+.5;
        if(z>near)return Mth.clamp((z-near)/PITCH,0,1);
        if(z<far)return Mth.clamp((far-z)/PITCH,0,1);
        return 0;
    }
    /** Whether the block at (x,z) is drawn ruled: certain outside the band, by a fixed hash within it. */
    public static boolean ruled(int x,int z){return ruled(x,z,0);}
    /**
     * 0.4.642: how ruled the land at (x,z) is with the fill's edge moved {@code shift} blocks out (his in-breath moves it one, KingBreath):
     * toward the house on the near side of the wood and toward the far ranks beyond it, so the filled land grows both ways.
     */
    public static double ruledness(int x,int z,int shift){return ruledness(x,z,shift,0);}
    /**
     * 0.4.643: the same at a depth of his sleep. Barely asleep, only his own square is filled and the rest of the fourth rank is the
     * band, thinning west across it; the in-breath moves that edge a block west. Awake, all of it is ruled.
     */
    public static double ruledness(int x,int z,int shift,int level){
        double r;
        // 0.4.648: settled by the last song, the country is filled to its edges, the one time it is.
        if(level==KingSleep.FULL)return 0;
        if(level>=3)r=1;
        else if(level==2){double edge=8.5-shift;r=z>rankNear(4)||z<rankFar(4)?1:x>=edge?0:Mth.clamp((edge-x)/34,0,1);}
        else r=shift==0?ruledness(z,level):z>rankFar(4)?ruledness(z-shift,level):ruledness(z+shift,level);
        if(Math.abs(x)>EAST+1)r=Math.max(r,Mth.clamp((Math.abs(x)-EAST-1)/8.0,0,1));
        return r;
    }
    public static boolean ruled(int x,int z,int shift){return ruled(x,z,shift,0);}
    public static boolean ruled(int x,int z,int shift,int level){
        double r=ruledness(x,z,shift,level);
        // Within the band the land goes over in clumps of four blocks, not one block at a time.
        if(r<=0)return false;if(r>=1)return true;return hash(x>>1,z>>1)<r;
    }
    private static final long[][] BREATHING=new long[5][];
    private static long[] changing;
    /** Every column (x, z, as BlockPos longs at y 0) whose drawing changes between the out-breath and the in-breath. */
    public static long[] breathing(){return breathing(0);}
    /** 0.4.643: the same at a depth of his sleep. */
    public static synchronized long[] breathing(int level){
        level=Mth.clamp(level,0,KingSleep.FULL);
        if(BREATHING[level]==null){
            var cells=new java.util.ArrayList<Long>();
            for(int x=-COUNTRY;x<=COUNTRY;x++)for(int z=rankFar(RANKS)-8;z<=0;z++)if(ruled(x,z,0,level)!=ruled(x,z,1,level))cells.add(BlockPos.asLong(x,0,z));
            BREATHING[level]=cells.stream().mapToLong(Long::longValue).toArray();
        }
        return BREATHING[level];
    }
    /** 0.4.643: every column whose drawing differs anywhere between the depths of his sleep and the two halves of his breath. */
    public static synchronized long[] changing(){
        if(changing==null){
            var cells=new java.util.ArrayList<Long>();
            for(int x=-COUNTRY;x<=COUNTRY;x++)for(int z=rankFar(RANKS)-8;z<=0;z++){
                boolean first=ruled(x,z,0,0),differs=false;
                for(int level=0;level<=KingSleep.FULL&&!differs;level++)for(int shift=0;shift<=1;shift++)if(ruled(x,z,shift,level)!=first){differs=true;break;}
                if(differs)cells.add(BlockPos.asLong(x,0,z));
            }
            changing=cells.stream().mapToLong(Long::longValue).toArray();
        }
        return changing;
    }
    static double hash(int x,int z){long h=x*341873128712L+z*132897987541L;h^=h>>>29;h*=0xBF58476D1CE4E5B9L;h^=h>>>32;return (h&0xFFFFFF)/(double)0x1000000;}

    public static boolean inside(Vec3 r){return r.x>=-COUNTRY&&r.x<COUNTRY+1&&r.z>=rankFar(RANKS)-8&&r.z<1&&r.y>=-4&&r.y<32;}
    public static boolean inRoom(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-12.9&&r.z<0&&r.y>=-.5&&r.y<CEIL;}
    public static boolean behindGlass(Vec3 r){return r.x>=-6&&r.x<7&&r.z>=-26.9&&r.z<-13.6&&r.y>=-.5&&r.y<CEIL;}
    public static boolean onBoard(Vec3 r){return r.z<-28&&r.z>=rankFar(RANKS)-2&&r.x>=-COUNTRY&&r.x<COUNTRY+1;}

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static BlockState mat(String name){return KingRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState fit(KingBlock.Kind kind,Direction facing){return KingBlock.of(kind,facing);}
    static BlockState fit(KingBlock.Kind kind,Direction facing,int stage){return KingBlock.of(kind,facing,stage);}
    /** The mirror image, across the glass, of a cell of the drawing-room. */
    public static BlockPos reflect(BlockPos p){return new BlockPos(p.getX(),p.getY(),-27-p.getZ());}
    static Direction reflect(Direction d){return d.getAxis()==Direction.Axis.Z?d.getOpposite():d;}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        country(l,b);
        house(l,b);
        room(l,b,false);
        room(l,b,true);
        board(l,b);
        wood(l,b);
        rattle(l,b);
        hillside(l,b);
        carriage(l,b);
        mushroom(l,b);
        drawnBridge(l,b);
        court(l,b);
        tree(l,b,CAT_TREE.getX(),CAT_TREE.getZ(),6,2);
        forest(l,b);
        // 0.4.644: a peg on every square from the second, and the glass table by the road on the hill's square.
        for(int r=2;r<=RANKS;r++)set(l,b,peg(r),KingBlock.of(KingBlock.Kind.PEG,Direction.NORTH));
        set(l,b,GLASS_TABLE,KingBlock.of(KingBlock.Kind.GLASS_TABLE,Direction.WEST));
        feast(l,b,false);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.648: the feast on the eighth rank
    /** The feast laid, every piece drawn ({@code filled} false) or filled; a fresh dream lays it drawn again (KingFeast.reset). */
    public static void feast(ServerLevel l,BlockPos b,boolean filled){
        int stage=filled?1:0;
        for(int z=FEAST_NEAR;z>=FEAST_FAR;z--)for(int x=-1;x<=0;x++)set(l,b,x,0,z,KingBlock.of(KingBlock.Kind.FEAST_TABLE,x<0?Direction.WEST:Direction.EAST,stage));
        for(int i=0;i<FEAST_DISHES.length;i++){var at=FEAST_DISHES[i];set(l,b,at,KingBlock.of(DISH_KINDS[i],at.getX()<0?Direction.WEST:Direction.EAST,stage));}
        for(var at:FEAST_CHAIRS)set(l,b,at,KingBlock.of(KingBlock.Kind.FEAST_CHAIR,at.equals(FEAST_FOOT)?Direction.NORTH:at.getX()<0?Direction.EAST:Direction.WEST,stage));
    }

    /** 0.4.644: the hill, turf over soil, raised in the same drawing as the land round it, so it breathes with it. */
    static void hillside(ServerLevel l,BlockPos b){
        for(int x=HILL.getX()-HILL_RADIUS;x<=HILL.getX()+HILL_RADIUS;x++)for(int z=HILL.getZ()-HILL_RADIUS;z<=HILL.getZ()+HILL_RADIUS;z++){
            int h=hill(x,z);if(h<=0||file(x)!=1)continue;boolean r=ruled(x,z);
            var top=l.getBlockState(b.offset(x,-1,z));
            for(int y=-1;y<h-1;y++)set(l,b,x,y,z,KingRegistry.country("soil",r));
            set(l,b,x,h-1,z,top.isAir()?KingRegistry.country(light(2,1)?"turf_light":"turf_dark",r):top);
        }
    }

    /**
     * 0.4.644: the railway carriage, one compartment on its wheels in the field: panelled walls with a window each side, a roof, a door
     * at its west end, and two seats facing each other across it.
     */
    static void carriage(ServerLevel l,BlockPos b){
        var panel=mat("panel");var floor=mat("floorboards");var roof=mat("ceiling");
        box(l,b,CAR_WEST,-1,CAR_NORTH,CAR_EAST,-1,CAR_SOUTH,floor);
        for(int y=0;y<=2;y++){
            for(int x=CAR_WEST;x<=CAR_EAST;x++){set(l,b,x,y,CAR_NORTH,panel);set(l,b,x,y,CAR_SOUTH,panel);}
            for(int z=CAR_NORTH;z<=CAR_SOUTH;z++){set(l,b,CAR_WEST,y,z,panel);set(l,b,CAR_EAST,y,z,panel);}
        }
        box(l,b,CAR_WEST+1,0,CAR_NORTH+1,CAR_EAST-1,2,CAR_SOUTH-1,AIR);
        box(l,b,CAR_WEST,3,CAR_NORTH,CAR_EAST,3,CAR_SOUTH,roof);
        set(l,b,CAR_DOOR,AIR);set(l,b,CAR_DOOR.above(),AIR);
        for(int x:new int[]{15,18}){
            set(l,b,x,1,CAR_NORTH,fit(KingBlock.Kind.WINDOW,Direction.SOUTH,0));set(l,b,x,2,CAR_NORTH,fit(KingBlock.Kind.WINDOW,Direction.SOUTH,1));
            set(l,b,x,1,CAR_SOUTH,fit(KingBlock.Kind.WINDOW,Direction.NORTH,0));set(l,b,x,2,CAR_SOUTH,fit(KingBlock.Kind.WINDOW,Direction.NORTH,1));
        }
        for(var seat:SEATS)set(l,b,seat,fit(KingBlock.Kind.SEAT,seat.getX()<GUARD.getX()?Direction.EAST:Direction.WEST));
        for(int x:new int[]{CAR_WEST+2,CAR_EAST-2})for(int z:new int[]{CAR_NORTH-1,CAR_SOUTH+1})set(l,b,x,0,z,fit(KingBlock.Kind.WHEEL,Direction.NORTH));
    }
    /** 0.4.645: the mushroom, its stalk and its broad cap a block up. */
    static void mushroom(ServerLevel l,BlockPos b){
        set(l,b,MUSHROOM,fit(KingBlock.Kind.STALK,Direction.NORTH));set(l,b,MUSHROOM.above(),fit(KingBlock.Kind.CAP,Direction.NORTH));
        for(int y=2;y<=4;y++)set(l,b,MUSHROOM.above(y),AIR);
    }
    /** 0.4.645: the footbridge beyond the fifth square is only drawn (KingDrawnBridgeBlock). */
    static void drawnBridge(ServerLevel l,BlockPos b){
        for(int z=rankFar(5)-2;z<=rankFar(5)-1;z++)for(int x=-1;x<=0;x++)set(l,b,x,-1,z,KingRegistry.DRAWN_BRIDGE.get().defaultBlockState());
    }
    /**
     * 0.4.646: the court's pack standing: the jurors in their two rows, faces in, and the guard across the middle file, faces to the
     * court. Whatever lay fallen where the King, the Queen and the herald stand is cleared, for a fresh dream.
     */
    public static void court(ServerLevel l,BlockPos b){
        for(int x=-8;x<=7;x++)set(l,b,x,0,GUARD_ROW,KingCardBlock.of(Math.floorMod(x+8,6),Direction.SOUTH,false));
        for(int z=JURY_FAR;z<=JURY_NEAR;z++){
            set(l,b,JURY_WEST,0,z,KingCardBlock.of(Math.floorMod(z,6),Direction.EAST,false));
            set(l,b,JURY_EAST,0,z,KingCardBlock.of(Math.floorMod(z+3,6),Direction.WEST,false));
        }
        for(var at:new BlockPos[]{COURT_KING,COURT_QUEEN,HERALD})if(KingCardBlock.is(l.getBlockState(b.offset(at))))set(l,b,at,AIR);
    }
    /** 0.4.643: the brothers' rattle lying on his square. */
    public static void rattle(ServerLevel l,BlockPos b){set(l,b,RATTLE,KingBlock.of(KingBlock.Kind.RATTLE,Direction.NORTH));}

    // ------------------------------------------------------------------------------------------------ the country round the strip
    static void country(ServerLevel l,BlockPos b){
        int far=rankFar(RANKS)-8;
        for(int x=-COUNTRY;x<=COUNTRY;x++)for(int z=far;z<=0;z++){
            boolean r=ruled(x,z);
            set(l,b,x,-3,z,KingRegistry.country("soil",r));set(l,b,x,-2,z,KingRegistry.country("soil",r));
            set(l,b,x,-1,z,KingRegistry.country(((x>>3)+(z>>3)&1)==0?"turf_light":"turf_dark",r));
            for(int y=0;y<=8;y++)set(l,b,x,y,z,AIR);
        }
        // The land beyond the eighth rank is a low ruled hedge, as far as the country goes: the end of the board.
        for(int x=-COUNTRY;x<=COUNTRY;x++)for(int y=0;y<=1;y++)set(l,b,x,y,far,KingRegistry.country("hedge",true));
        for(int z=far;z<=0;z++)for(int y=0;y<=1;y++){set(l,b,-COUNTRY,y,z,KingRegistry.country("hedge",true));set(l,b,COUNTRY,y,z,KingRegistry.country("hedge",true));}
    }

    // ------------------------------------------------------------------------------------------------ the house: two rooms back to back
    static void house(ServerLevel l,BlockPos b){
        var brick=mat("chimney");
        // The outer skin, chimney brick, x -8 to 8 and back to z -28; the inner walls the rooms' own (the drawing-room's south wall at z 0
        // is the vestibule's back, the other room's north wall at -27 its mirror).
        box(l,b,-8,-1,-28,8,CEIL+1,0,brick);
        box(l,b,-7,0,-27,7,CEIL-1,0,mat("panel"));
        box(l,b,-6,0,-26,6,CEIL-1,-1,AIR);
        // The back of the chimney-pieces: one thick wall of brick between the rooms.
        box(l,b,-7,0,-14,7,CEIL-1,-13,brick);
        // Floors and ceilings.
        box(l,b,-6,-1,-26,6,-1,-1,mat("floorboards"));box(l,b,-6,CEIL,-26,6,CEIL,-1,mat("ceiling"));
        box(l,b,-6,-1,-14,6,-1,-13,brick);
        // A low roof of brick, stepped, so the house is a thing in the country and not a box.
        for(int y=CEIL+2,i=0;i<3;y++,i++)box(l,b,-8+i*2,y,-27,8-i*2,y,0,brick);
        // The House's door at z 0 in the drawing-room's south wall; the doorway out of the other room at z -27, through the brick.
        set(l,b,0,0,0,AIR);set(l,b,0,1,0,AIR);
        set(l,b,GARDEN_DOOR,AIR);set(l,b,GARDEN_DOOR.above(),AIR);set(l,b,GARDEN_DOOR.north(),AIR);set(l,b,GARDEN_DOOR.north().above(),AIR);
        set(l,b,GARDEN_DOOR.below(),mat("floorboards"));set(l,b,GARDEN_DOOR.north().below(),KingRegistry.country("path",true));
        // The windows go through the brick: each is open to the country behind it.
        for(int x=-8;x<=8;x+=16)for(int z:new int[]{-4,-6,-21,-23})for(int y=1;y<=2;y++)set(l,b,x,y,z,AIR);
        // A step of path from the doorway to the board's first rank.
        for(int z=-29;z>=NEAR;z--){set(l,b,-1,-1,z,KingRegistry.country("path",ruled(-1,z)));set(l,b,0,-1,z,KingRegistry.country("path",ruled(0,z)));}
    }

    /** One of the two rooms; the second is the first reflected across the glass. */
    static void room(ServerLevel l,BlockPos b,boolean behind){
        java.util.function.UnaryOperator<BlockPos> at=p->behind?reflect(p):p;
        java.util.function.UnaryOperator<Direction> to=d->behind?reflect(d):d;
        // Walls: panel to the dado, paper above it.
        for(int y=1;y<CEIL;y++){
            for(int x=-7;x<=7;x+=14)for(int z=-1;z>=-12;z--)set(l,b,at.apply(new BlockPos(x,y,z)),mat("wallpaper"));
            for(int x=-6;x<=6;x++)set(l,b,at.apply(new BlockPos(x,y,0)),mat("wallpaper"));
        }
        for(int x=-6;x<=6;x++)set(l,b,at.apply(new BlockPos(x,0,0)),mat("panel"));
        // The chimney wall either side of the breast is the room's own: panel to the dado, paper above.
        for(int x=-6;x<=6;x++)if(Math.abs(x)>2)for(int y=0;y<CEIL;y++)set(l,b,at.apply(new BlockPos(x,y,-13)),mat(y==0?"panel":"wallpaper"));
        if(!behind){set(l,b,0,0,0,AIR);set(l,b,0,1,0,AIR);}
        else{set(l,b,GARDEN_DOOR,AIR);set(l,b,GARDEN_DOOR.above(),AIR);}
        // The chimney-piece: two jambs and the fire, the hearth before it, the glass above, three across and two high.
        var into=to.apply(Direction.SOUTH);
        for(int i=-1;i<=1;i++){
            set(l,b,at.apply(new BlockPos(i,0,-12)),fit(KingBlock.Kind.MANTEL,into,i+1));
            set(l,b,at.apply(new BlockPos(i,-1,-11)),mat("hearth"));set(l,b,at.apply(new BlockPos(i,-1,-12)),mat("hearth"));
            set(l,b,at.apply(new BlockPos(i,1,-12)),fit(KingBlock.Kind.GLASS,into,i+1));set(l,b,at.apply(new BlockPos(i,2,-12)),fit(KingBlock.Kind.GLASS,into,i+4));
        }
        // The chimney breast either side of the glass and above it, in brick.
        for(int y=1;y<CEIL;y++){set(l,b,at.apply(new BlockPos(-2,y,-12)),mat("chimney"));set(l,b,at.apply(new BlockPos(2,y,-12)),mat("chimney"));}
        for(int x=-1;x<=1;x++)for(int y=3;y<CEIL;y++)set(l,b,at.apply(new BlockPos(x,y,-12)),mat("chimney"));
        set(l,b,at.apply(new BlockPos(-2,0,-12)),mat("chimney"));set(l,b,at.apply(new BlockPos(2,0,-12)),mat("chimney"));
        // The hearthrug, the chessmen in the hearth, the table and its paper, the armchair turned to the fire.
        for(int x=-1;x<=1;x++)for(int z=-9;z>=-10;z--)set(l,b,at.apply(new BlockPos(x,0,z)),fit(KingBlock.Kind.RUG,into));
        set(l,b,at.apply(CHESSMEN),fit(KingBlock.Kind.CHESSMEN,into));
        set(l,b,at.apply(TABLE),fit(KingBlock.Kind.TABLE,into));
        set(l,b,at.apply(PROBLEM),fit(KingBlock.Kind.PROBLEM,into,behind?1:0));
        set(l,b,at.apply(ARMCHAIR),fit(KingBlock.Kind.ARMCHAIR,to.apply(Direction.NORTH)));
        // A tall window in each side wall, looking in.
        for(int x=-7;x<=7;x+=14){var in=x<0?Direction.EAST:Direction.WEST;
            for(int z=-4;z>=-6;z-=2){set(l,b,at.apply(new BlockPos(x,1,z)),fit(KingBlock.Kind.WINDOW,in,0));set(l,b,at.apply(new BlockPos(x,2,z)),fit(KingBlock.Kind.WINDOW,in,1));}}
    }

    // ------------------------------------------------------------------------------------------------ the board
    static void board(ServerLevel l,BlockPos b){
        for(int r=1;r<=RANKS;r++){
            int near=rankNear(r),far=rankFar(r);
            for(int x=WEST;x<=EAST;x++)for(int z=far;z<=near;z++){
                int f=file(x);boolean ruled=ruled(x,z);
                if(f==0){
                    // The hedge between the files, with a gap at the middle of each rank.
                    boolean gap=z==near-7||z==near-8;
                    set(l,b,x,-1,z,KingRegistry.country("turf_dark",ruled));
                    if(!gap){set(l,b,x,0,z,KingRegistry.country("hedge",ruled));set(l,b,x,1,z,KingRegistry.country("hedge",ruled));}
                }else set(l,b,x,-1,z,KingRegistry.country(light(r,f)?"turf_light":"turf_dark",ruled));
                if(f==2&&(x==-1||x==0))set(l,b,x,-1,z,KingRegistry.country("path",ruled));
            }
            // The brook beyond this rank, two rows sunk one block; a footbridge carries the road over.
            if(r<RANKS)for(int x=WEST-1;x<=EAST+1;x++)for(int z=far-2;z<=far-1;z++){
                boolean ruled=ruled(x,z);
                set(l,b,x,-2,z,KingRegistry.country("brook",ruled));set(l,b,x,-1,z,AIR);
                if(x==-1||x==0)set(l,b,x,-1,z,KingRegistry.country("bridge",ruled));
                // 0.4.642: the verge over the brook, never seen: a wall to a thing, wading to a sort of thing, nothing to a line.
                else set(l,b,x,0,z,KingRegistry.VERGE.get().defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ the wood on the fourth rank
    static void wood(ServerLevel l,BlockPos b){
        int near=rankNear(4),far=rankFar(4);
        // Small trees over the whole rank, off the road and the hedge gaps, at fixed places.
        for(int x=WEST+2;x<=EAST-2;x+=5)for(int z=near-2;z>=far+2;z-=5){
            int dx=(int)(hash(x,z)*3)-1,dz=(int)(hash(z,x)*3)-1,tx=x+dx,tz=z+dz;
            if(file(tx)==0||Math.abs(tx)<=3||Math.abs(tx-TREE.getX())<=5&&Math.abs(tz-TREE.getZ())<=6)continue;
            tree(l,b,tx,tz,4+(int)(hash(tx,tz)*2),2);
        }
        tree(l,b,TREE.getX(),TREE.getZ(),7,4);
        // The trunk is two thick at his tree.
        for(int y=0;y<5;y++){set(l,b,TREE.getX()+1,y,TREE.getZ(),KingRegistry.country("bark",false));}
    }
    // ------------------------------------------------------------------------------------------------ 0.4.647: the forest on the seventh rank
    /**
     * Trees over the whole seventh rank, closer set than the wood's and kept off the road, the Knight's lane, the hedge gaps, the peg and
     * the turn's path; the turn itself, through the hedge and along the far edge of the square; and a stand of trees east of x 14 round
     * its end, close enough that a rider who goes in among them is out of sight from the road.
     */
    static void forest(ServerLevel l,BlockPos b){
        int near=rankNear(7),far=rankFar(7);
        for(int x=WEST+2;x<=EAST-2;x+=4)for(int z=near-2;z>=far+2;z-=4){
            int dx=(int)(hash(x,z)*3)-1,dz=(int)(hash(z,x)*3)-1,tx=x+dx,tz=z+dz;
            if(!forestTree(tx,tz))continue;
            tree(l,b,tx,tz,4+(int)(hash(tx,tz)*2),2);
        }
        // The turn: the hedge opened where the path goes through it, and the path laid along the far edge of the square to the stand.
        for(int z=TURN_Z-1;z<=TURN_Z;z++)for(int x=1;x<=(int)TURN_END.x+1;x++){
            for(int y=0;y<=6;y++)set(l,b,x,y,z,AIR);
            set(l,b,x,-1,z,KingRegistry.country("path",ruled(x,z)));
        }
        // The stand round the turn's end: trees on both sides of the path and beyond its end, and boughs low enough to close it over.
        int[][] stand={{14,-148},{16,-147},{18,-148},{20,-147},{22,-149},{23,-151},{21,-153},{18,-153},{15,-153},{17,-145}};
        for(var t:stand)tree(l,b,t[0],t[1],5,2);
    }
    /** Whether the forest may set a tree's trunk at (x,z) (its boughs reach two blocks round it). */
    static boolean forestTree(int x,int z){
        if(rank(z)!=7||file(x)==0)return false;
        if(x>=-3&&x<=6)return false; // the road and the Knight's lane, and two blocks either side of them
        int hedge=Math.min(Math.abs(x+9),Math.abs(x-8));
        if(hedge<=1)return false; // a trunk against the hedge
        if(hedge<=3&&Math.abs(z-(rankNear(7)-7.5))<=4)return false; // the hedge gaps
        if(z<=TURN_Z+3&&x>=1&&x<=13)return false; // the turn's path, short of the stand
        if(x>=13&&z<=TURN_Z+4&&z>=TURN_Z-4)return false; // the stand is set by hand
        return true;
    }
    static void tree(ServerLevel l,BlockPos b,int x,int z,int height,int spread){
        boolean ruled=ruled(x,z);var bark=KingRegistry.country("bark",ruled);var bough=KingRegistry.country("bough",ruled);
        for(int y=0;y<height;y++)set(l,b,x,y,z,bark);
        int top=height-1;
        for(int dx=-spread;dx<=spread;dx++)for(int dy=-1;dy<=spread;dy++)for(int dz=-spread;dz<=spread;dz++){
            double d=dx*dx+dz*dz+dy*dy*1.6;if(d>spread*spread+1)continue;if(dx==0&&dz==0&&dy<1)continue;
            if(hash(x+dx*7,z+dz*13+dy)<.08)continue;
            set(l,b,x+dx,top+dy,z+dz,bough);
        }
    }
}
