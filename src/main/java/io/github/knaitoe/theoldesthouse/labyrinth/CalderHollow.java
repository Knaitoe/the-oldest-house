package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.494, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act three"): the Hollow's ground. The proposal: "a course of floating ground,
 * high and low, over nothing"; "islands of graveyard turf with their headstones still in them; crooked fences; a bridge of coffin
 * lids; a pumpkin vine the size of a road, curling up and down; stairs of tilted stones"; and "The Hollow folds back down into the
 * graveyard's far side. The riders come out through the back gate onto a quiet lane above the town."
 *
 * <p>0.4.521: it is no longer past the town's own cemetery, where its ground stood over the hill for the town to see. It lies past
 * the cemetery's fold (BikeNightFold), ENTRY-EXTRA blocks north of where it was, beyond the expanse (CalderExpanse); every position
 * here is where it is now, and the hill's edge is the expanse's. Its back gate and the lane behind it are built twice: here, and
 * behind the town's cemetery, where somebody who goes through this one is carried ({@link #back}).
 *
 * <p>This is the plain way through, which any bike takes, and (0.4.496) the first of the ways the bikes' own perks open: the high
 * way, the Hopper's ({@link #HIGH}), and (0.4.497) the long way, the Comet's ({@link #LONG}). The other three are not built. The hill's
 * ground stops at {@link #EDGE}, ragged, and from there to {@link #FAR_SIDE} there is nothing but the course. The cemetery's back
 * railings and its back gate are on the far side of it now, where its ground begins again.
 *
 * <p>It is laid out from what a bike jumps (measured, in the proposal). Every gap on the plain way is two blocks but one of three.
 * On the level a tap of the hop carries a plain bike 2.3 blocks, which clears two only if it is made in the last block before the
 * brink, and a held hop 3.3, which clears three only from the brink itself: the first time the way was ridden twice, the second
 * rider came off against the far side of the three. So the three, and most of the twos, come down: the piece past the gap is a
 * block or half a block lower, which gives a hop the time to carry. What height that loses is got back by steps (the vine, the
 * stairs), never by a jump. Every position is relative to the park's base; a piece's height is where a body stands on it, and the
 * hill's top is at 3.
 */
public final class CalderHollow {
    private CalderHollow(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The last row the hill's ground reaches on the gate's line; where the far side's ground begins; its back railings; and how far north anything is. */
    public static final int EDGE=-1317,FAR_SIDE=-1476,BACK=-1491,FAR=-1498;
    /** The far side: how wide its ground is, and the lane behind its railings. */
    public static final int SIDE=24,LANE_N=-1496,LANE_S=-1493;
    /**
     * 0.4.508, the way on. A brick wall runs along the lane's far verge and round its two ends, and in it, east of the gate's
     * line, is a door (OnwardPlacements). It stands three rows back from the lane, so that somebody who comes through the back gate
     * at speed has eight blocks to turn or stop in.
     */
    public static final int WALL=FAR-1,WALL_HIGH=3,DOOR_X=5;
    /** What a piece of the course is made of. */
    public enum Made{SOD,LID,VINE,STONE,EARTH,BANK,TENDRIL}
    /** A piece: a rectangle of ground at one height. */
    public record Piece(String name,Made made,int x0,int x1,int zSouth,int zNorth,double top){
        public boolean has(int x,int z){return x>=x0&&x<=x1&&z<=zSouth&&z>=zNorth;}
    }
    /** The plain way, from the hill to the far side, in the order it is ridden. */
    public static final List<Piece> WAY=way();
    private static List<Piece> way(){
        var out=new ArrayList<Piece>();
        // Graves that have come away, each a little off the line of the last.
        out.add(new Piece("first grave",Made.SOD,-3,3,-1320,-1326,2));
        out.add(new Piece("second grave",Made.SOD,-1,5,-1329,-1335,2));
        out.add(new Piece("third grave",Made.SOD,-5,1,-1338,-1344,1));
        // "crooked fences": a long strip with board fence half across it, one side and then the other.
        out.add(new Piece("the fences",Made.SOD,-2,2,-1347,-1370,1));
        // "a bridge of coffin lids": two blocks of lid, one of nothing. (The gap before it is the one of three.)
        for(int i=0;i<5;i++)out.add(new Piece("lid "+(i+1),Made.LID,-1,0,-1374-3*i,-1375-3*i,0));
        out.add(new Piece("the grave past the lids",Made.SOD,-3,3,-1389,-1394,0));
        // "a pumpkin vine the size of a road, curling up and down": up three blocks by half blocks, over, and down, swinging east and back.
        int[] east={0,0,0,0,1,1,1,2,2,2,2,2,2,2,1,1,1,0,0,0,0,0,0,0,0,0,0,0};
        double[] high={0,0,.5,.5,1,1,1.5,1.5,2,2,2.5,2.5,3,3,3,3,3,3,2.5,2.5,2,2,1.5,1.5,1.5,1.5,1.5,1.5};
        for(int i=0;i<east.length;i++)out.add(new Piece("vine "+i,Made.VINE,-1+east[i],east[i],-1397-i,-1397-i,high[i]));
        // "stairs of tilted stones": up from 1 to 5, half a block every two.
        for(int i=0;i<9;i++)out.add(new Piece("stair "+i,Made.STONE,-1,1,-1427-2*i,-1428-2*i,1+.5*i));
        out.add(new Piece("the top",Made.SOD,-3,3,-1445,-1450,5));
        // The last stretch: a strip running down to the far side, with one gap in it.
        for(int i=0;i<4;i++)out.add(new Piece("stretch "+i,Made.SOD,-1,1,-1453-3*i,-1455-3*i,4-.5*(i/2)));
        for(int i=0;i<3;i++)out.add(new Piece("stretch "+(4+i),Made.SOD,-1,1,-1467-3*i,-1469-3*i,3));
        return List.copyOf(out);
    }
    /**
     * 0.4.496, the high way. The proposal: "high: a gap only the Hopper's jump clears". From the second grave, east of the line,
     * a ledge two blocks up across two blocks of nothing: a held hop on the Hopper goes as high as that, and on any other bike a
     * block and a quarter. From there it runs level over the fences and the lids, three graves long, and comes down by steps to
     * its foot, which lies along the east side of the grave past the lids, level with it.
     */
    public static final List<Piece> HIGH=high();
    private static List<Piece> high(){
        var out=new ArrayList<Piece>();
        out.add(new Piece("high grave 1",Made.SOD,4,6,-1338,-1355,4));
        out.add(new Piece("high grave 2",Made.SOD,4,6,-1358,-1373,4));
        out.add(new Piece("high grave 3",Made.SOD,4,6,-1376,-1381,3));
        for(int i=0;i<5;i++)out.add(new Piece("high step "+i,Made.STONE,4,6,-1382-i,-1382-i,2.5-.5*i));
        // Its foot is long: a bike comes down the steps fast, and has the length of the grave beside it to turn onto it.
        out.add(new Piece("the high way's foot",Made.SOD,4,6,-1387,-1394,0));
        return List.copyOf(out);
    }
    /**
     * 0.4.497, the long way. The proposal: "long: a run only the Comet's top speed carries". West of the vine, from the grave
     * past the lids to the stairs: a run of coffin lids, {@link #RUN_ROWS} rows, that gives way from its near end once somebody is
     * on it, faster than any bike goes but the Comet, and the Rattler in its burst (BikeNightHollow.run). It is level where the
     * vine climbs three blocks, swings and comes down again; past it, steps up to the stairs' second pair.
     */
    public static final int RUN_X0=-5,RUN_X1=-4,RUN_SOUTH=-1397,RUN_ROWS=28;
    public static final List<Piece> LONG=longWay();
    private static List<Piece> longWay(){
        var out=new ArrayList<Piece>();
        out.add(new Piece("the long way's head",Made.SOD,RUN_X0,RUN_X1,-1393,-1396,0));
        for(int i=0;i<RUN_ROWS;i++)out.add(new Piece("run "+i,Made.LID,RUN_X0,RUN_X1,RUN_SOUTH-i,RUN_SOUTH-i,0));
        out.add(new Piece("long step 0",Made.STONE,RUN_X0,RUN_X1+1,-1425,-1425,.5));
        out.add(new Piece("long step 1",Made.STONE,RUN_X0,RUN_X1+1,-1426,-1427,1));
        out.add(new Piece("the long way's foot",Made.SOD,RUN_X0,-2,-1428,-1431,1.5));
        return List.copyOf(out);
    }
    /** Whether a place (from the park's base) is on the run, and which of its rows; -1 off it. */
    public static int runRow(double x,double z){
        if(x<RUN_X0||x>=RUN_X1+1)return -1;int row=RUN_SOUTH-(int)Math.floor(z);return row>=0&&row<RUN_ROWS?row:-1;
    }
    /** One row of the run: there, or gone. */
    public static void runRow(ServerLevel l,BlockPos b,int row,boolean there){
        var s=there?half(BikeNightRegistry.COFFIN_LID.get(),true):Blocks.AIR.defaultBlockState();
        for(int x=RUN_X0;x<=RUN_X1;x++)l.setBlock(b.offset(x,-1,RUN_SOUTH-row),s,Block.UPDATE_CLIENTS);
    }
    /**
     * 0.4.504, the steep way. The proposal: "steep: a climb only the Hare gets going up in time". East of the stairs: a turn off
     * the stairs' first pair, a level run beside them, {@link #LOOSE_ROWS} rows of loose earth, and a bank of it, {@link #BANK_ROWS}
     * whole blocks up in as many rows, to a head beside the last stretch's first piece. Loose earth takes the way off a bike
     * (NightBike.LOOSE), and on it a wheel digs in at a step: a bike mounts a whole block only with the way on to pay for the climb,
     * which nothing can be pedalled up to there but the Hare, which is driven nearly twice as hard and on which a climb costs a
     * third (and the Rattler while its card has it, pedalled as well). And the bank does not wait: once somebody is on it, it slides away (BikeNightHollow.bank). It comes up three blocks in three rows
     * where the stairs take twelve, and misses the top, where a witch crosses.
     */
    public static final int BANK_X0=4,BANK_X1=5,LOOSE_SOUTH=-1444,LOOSE_ROWS=4,BANK_SOUTH=LOOSE_SOUTH-LOOSE_ROWS,BANK_ROWS=3,BANK_FOOT=1;
    public static final List<Piece> STEEP=steepWay();
    private static List<Piece> steepWay(){
        var out=new ArrayList<Piece>();
        out.add(new Piece("the steep way's turn",Made.SOD,2,BANK_X1,-1427,-1432,BANK_FOOT));
        out.add(new Piece("the steep way's run",Made.SOD,BANK_X0,BANK_X1,-1433,LOOSE_SOUTH+1,BANK_FOOT));
        for(int i=0;i<LOOSE_ROWS;i++)out.add(new Piece("the loose earth "+i,Made.EARTH,BANK_X0,BANK_X1,LOOSE_SOUTH-i,LOOSE_SOUTH-i,BANK_FOOT));
        for(int i=0;i<BANK_ROWS;i++)out.add(new Piece("bank "+i,Made.BANK,BANK_X0,BANK_X1,BANK_SOUTH-i,BANK_SOUTH-i,BANK_FOOT+1+i));
        out.add(new Piece("the steep way's head",Made.SOD,2,BANK_X1,BANK_SOUTH-BANK_ROWS,BANK_SOUTH-BANK_ROWS-4,BANK_FOOT+BANK_ROWS));
        return List.copyOf(out);
    }
    /** Whether a place (from the park's base) is on the bank, and which of its rows; -1 off it. */
    public static int bankRow(double x,double z){
        if(x<BANK_X0||x>=BANK_X1+1)return -1;int row=BANK_SOUTH-(int)Math.floor(z);return row>=0&&row<BANK_ROWS?row:-1;
    }
    /** The bank, all of it: there, or gone. It is earth from the level of its foot up. */
    public static void bank(ServerLevel l,BlockPos b,boolean there){
        var s=there?mat("hollow_earth"):Blocks.AIR.defaultBlockState();
        for(int i=0;i<BANK_ROWS;i++)for(int x=BANK_X0;x<=BANK_X1;x++)for(int y=BANK_FOOT-1;y<=BANK_FOOT+i;y++)l.setBlock(b.offset(x,y,BANK_SOUTH-i),s,Block.UPDATE_CLIENTS);
    }
    /**
     * 0.4.505, the tight way. The proposal: "tight: a twisting path along the vine only the Wasp turns sharply enough for". East
     * of the vine, from the high way's foot to the stairs' foot: a tendril of the vine with walls of it either side, {@link
     * #TIGHT_ROWS} rows, crossed every {@link #GATE_EVERY} rows from the row {@link #GATE_FIRST} by a wall with one block's
     * opening, at the west side and the east side by turns. So the way through swings four blocks across in six along, and
     * back. It curls up from its near end behind whoever is in it (BikeNightHollow.curl), so it is ridden at speed or not at
     * all. At a bike's top speed the Wasp turns in under two blocks and an ordinary bike in three; the Wasp keeps four fifths
     * of its way off a wall it meets at any slant, and anything else, meeting a wall square, puts its rider off.
     */
    public static final int TIGHT_X0=5,TIGHT_X1=9,TIGHT_SOUTH=-1395,TIGHT_ROWS=26,GATE_FIRST=5,GATE_EVERY=6,GATE_HIGH=3;
    public static final List<Piece> TIGHT=tightWay();
    private static List<Piece> tightWay(){
        var out=new ArrayList<Piece>();
        for(int i=0;i<TIGHT_ROWS;i++)out.add(new Piece("tendril "+i,Made.TENDRIL,TIGHT_X0-1,TIGHT_X1+1,TIGHT_SOUTH-i,TIGHT_SOUTH-i,0));
        out.add(new Piece("the tight way's step",Made.VINE,TIGHT_X0,TIGHT_X1,TIGHT_SOUTH-TIGHT_ROWS,TIGHT_SOUTH-TIGHT_ROWS-1,.5));
        out.add(new Piece("the tight way's foot",Made.VINE,TIGHT_X0-1,TIGHT_X1,TIGHT_SOUTH-TIGHT_ROWS-2,TIGHT_SOUTH-TIGHT_ROWS-5,1));
        return List.copyOf(out);
    }
    /** Which of the tendril's gates a row of it is, or -1. */
    public static int gate(int row){return row>=GATE_FIRST&&row<TIGHT_ROWS&&(row-GATE_FIRST)%GATE_EVERY==0?(row-GATE_FIRST)/GATE_EVERY:-1;}
    /** The one block a gate is open at: the west side's for the first, the east side's for the next, and so on. */
    public static int gateOpen(int gate){return gate%2==0?TIGHT_X0:TIGHT_X1;}
    /** Whether a place (from the park's base) is on the tendril, its walls' rows with it, and which of its rows; -1 off it. */
    public static int tendrilRow(double x,double z){
        if(x<TIGHT_X0-1||x>=TIGHT_X1+2)return -1;int row=TIGHT_SOUTH-(int)Math.floor(z);return row>=0&&row<TIGHT_ROWS?row:-1;
    }
    /** One row of the tendril, its walls and its gate with it: there, or gone. What lies in the way between the walls is left alone. */
    public static void tendrilRow(ServerLevel l,BlockPos b,int row,boolean there){
        var s=there?mat("vine"):Blocks.AIR.defaultBlockState();int z=TIGHT_SOUTH-row,gate=gate(row);
        for(int x=TIGHT_X0-1;x<=TIGHT_X1+1;x++){
            l.setBlock(b.offset(x,-1,z),s,Block.UPDATE_CLIENTS);
            if(x<TIGHT_X0||x>TIGHT_X1||gate>=0&&x!=gateOpen(gate))for(int y=0;y<GATE_HIGH;y++)l.setBlock(b.offset(x,y,z),s,Block.UPDATE_CLIENTS);
        }
    }
    /**
     * 0.4.506, the dark way. The proposal: "dark: only the Lamp shows where the ground is". West of the first two graves, from
     * the hill's edge to the third grave: three islands in a bank of mist ({@link #dark}; BikeNightGraveyard.mist) so thick that
     * somebody in it sees two blocks and a half. Every gap is three blocks, which is further than that, and each island is set
     * across from the last, a block of it in line: the second east of the first, the third east of the second, and the third
     * grave off the third's east end. There is no one line through all of it, and somebody who rides straight on from the middle
     * of an island goes down. A Lamp that is throwing its light cuts the bank for its rider and whoever rides close, and they
     * see the next island from the last.
     */
    public static final int DARK_W=-24,DARK_E=-6,DARK_SOUTH=EDGE+3,DARK_NORTH=-1345,DARK_MOUTH_W=-20,DARK_MOUTH_E=-15;
    public static final List<Piece> DARK=List.of(
        new Piece("the dark way's first island",Made.SOD,-19,-16,-1321,-1325,2),new Piece("the dark way's second island",Made.SOD,-16,-13,-1329,-1333,2),
        new Piece("the dark way's third island",Made.SOD,-13,-8,-1337,-1341,2));
    /** Whether a place (from the park's base) is in the dark way's bank of mist. */
    public static boolean dark(double x,double z){return x>=DARK_W&&x<DARK_E&&z<DARK_SOUTH&&z>DARK_NORTH;}
    /**
     * 0.4.501, the werewolf's ground. The proposal: "a werewolf runs alongside on a parallel island and leaps across at gaps."
     * Beside two of the plain way's gaps (the first grave to the second; the one in the last stretch): a strip he runs up beside
     * the riders, a perch at its end level with the gap, and a perch across the way from it. None of it is a way for anybody or an
     * island, and none of it is within two blocks of ground that is.
     */
    public static final List<Piece> WOLFS=List.of(
        new Piece("wolf run 1",Made.SOD,-8,-7,-1320,-1325,2),new Piece("wolf perch 1 west",Made.SOD,-8,-6,-1326,-1329,2),new Piece("wolf perch 1 east",Made.SOD,8,10,-1326,-1329,2),
        new Piece("wolf run 2",Made.SOD,-7,-6,-1456,-1463,3),new Piece("wolf perch 2 west",Made.SOD,-7,-5,-1464,-1467,3),new Piece("wolf perch 2 east",Made.SOD,5,7,-1464,-1467,3));
    /**
     * A gap he leaps: where along the way its middle and its two brinks are; where across the way its middle is, and how high his
     * ground is there; the middles of his two perches; and where his run begins.
     */
    public record Leap(double z,double south,double north,double x,double top,double west,double east,double run){}
    public static final List<Leap> LEAPS=List.of(new Leap(-1327,-1326,-1328,1.5,2,-6.5,9.5,-1319.5),new Leap(-1465,-1464,-1466,.5,3,-5.5,6.5,-1455.5));
    // ------------------------------------------------------------------------------------------------ 0.4.507, the line Gus rides
    /** A place on the line (from the park's base), and whether it is in the air over a gap. */
    public record Mark(Vec3 at,boolean air){}
    /**
     * As answered: "Gus rides the plain way through the Hollow." The plain way as somebody rides it who never misses: from the
     * hill on the gate's line to the far side's ground, up the middle of each piece and round the fences' pickets, and over each
     * gap on an arc as a hop makes. Places a fifth of a block apart, in the order they are ridden.
     */
    public static final List<Mark> TRAIL=trail();
    private static List<Mark> trail(){
        int level=CalderGraves.level(EDGE);var at=new ArrayList<Vec3>();var hop=new ArrayList<Boolean>();
        at.add(new Vec3(.5,level,EDGE+3.5));hop.add(false);at.add(new Vec3(.5,level,EDGE+.3));hop.add(false);
        Piece last=null;
        for(var p:WAY){
            double x=(p.x0()+p.x1()+1)/2.0;
            at.add(new Vec3(x,p.top(),p.zSouth()+.7));hop.add(last==null||p.zSouth()<last.zNorth()-1);
            if(p.name().equals("the fences")){
                for(double[] m:new double[][]{{1.5,-1349},{1.5,-1354},{-1,-1358},{-1,-1362},{.5,-1365}}){at.add(new Vec3(m[0],p.top(),m[1]));hop.add(false);}
                x=.5;
            }
            at.add(new Vec3(x,p.top(),p.zNorth()+.3));hop.add(false);last=p;
        }
        at.add(new Vec3(.5,level,FAR_SIDE-3));hop.add(false);
        var out=new ArrayList<Mark>();out.add(new Mark(at.get(0),false));
        for(int i=1;i<at.size();i++){
            var a=at.get(i-1);var c=at.get(i);boolean air=hop.get(i);double far=a.distanceTo(c),high=air?Math.min(1.2,Math.max(.3,far*.25)):0;
            int steps=Math.max(1,(int)Math.ceil(far/.2));
            for(int k=1;k<=steps;k++){double f=k/(double)steps;out.add(new Mark(a.lerp(c,f).add(0,high*4*f*(1-f),0),air&&k<steps));}
        }
        return List.copyOf(out);
    }
    /** Every way, each in the order it is ridden; and every piece of all of them. */
    public static final List<List<Piece>> WAYS=List.of(WAY,HIGH,LONG,STEEP,TIGHT,DARK);
    public static final List<Piece> ALL=WAYS.stream().flatMap(List::stream).toList();
    /** How high a body stands at a place on the course; NaN where there is none of it. */
    public static double top(int x,int z){for(var p:ALL)if(p.has(x,z))return p.top();for(var p:WOLFS)if(p.has(x,z))return p.top();return Double.NaN;}

    // ------------------------------------------------------------------------------------------------ 0.4.495, the islands
    /**
     * How long a run of ground must be to be an island; how far short of its far end somebody who has missed is set down on it (the
     * run they have to get going again); and how far along the way ground counts as near somebody in the air. Blocks.
     */
    public static final int STAND=5,RUN=8,NEAR=6;
    /**
     * "Missing a jump drops them onto the last island they stood on (a checkpoint)." An island: the hill, the far side, and each run
     * of pieces that touch and is long enough to get going on again. The lids are not islands: each is two blocks long, and a bridge.
     * Its place is where somebody is set down on it: on its own line, RUN short of its far end, or a block in from its near end if
     * it is shorter than that. {@code x}, {@code top} and {@code z} are that place: the row, and where across it.
     */
    public record Island(String name,String way,int zNear,int zFar,double x,double top,int z){
        /** Where a body is set down, from the park's base. */
        public Vec3 place(){return new Vec3(x,top,z+.5);}
    }
    /** The hill is the first island. The plain way's follow it, then the far side ({@link #FAR_ISLAND}), then the other ways' in turn. */
    public static final int HILL=0;
    public static final int FAR_ISLAND;
    public static final List<Island> ISLANDS;
    /** Which island each piece of {@link #ALL} is part of, or -1. */
    private static final int[] OF;
    static{
        var out=new ArrayList<Island>();OF=new int[ALL.size()];java.util.Arrays.fill(OF,-1);int top=CalderGraves.level(EDGE),far=0,base=0;
        // The hill: the gate's line, between the small stones.
        out.add(new Island("the hill","",CalderGraves.SMALL_ROW,EDGE,0,top,EDGE+RUN));
        for(var way:WAYS){
            for(int i=0;i<way.size();){
                int j=i;while(j+1<way.size()&&touch(way.get(j),way.get(j+1)))j++;
                int near=way.get(i).zSouth(),end=way.get(j).zNorth();
                if(near-end+1>=STAND){
                    int z=Math.min(near-1,end+RUN),index=out.size();
                    for(int k=i;k<=j;k++){var p=way.get(k);OF[base+k]=index;if(z<=p.zSouth()&&z>=p.zNorth())out.add(new Island(name(way.get(i),way.get(j)),way==WAY?"plain":way==HIGH?"high":way==LONG?"long":way==STEEP?"steep":way==TIGHT?"tight":"dark",near,end,(p.x0()+p.x1()+1)/2.0,p.top(),z));}
                }
                i=j+1;
            }
            base+=way.size();
            if(way==WAY){far=out.size();out.add(new Island("the far side","",FAR_SIDE,BACK+1,0,top,FAR_SIDE-3));}
        }
        FAR_ISLAND=far;ISLANDS=List.copyOf(out);
    }
    /** Lids are never part of an island: the plain way's are a bridge, and the long way's give way. Nor is the steep way's bank, which slides, or the tight way's tendril, which curls up. */
    private static boolean touch(Piece a,Piece c){return a.made()!=Made.LID&&c.made()!=Made.LID&&a.made()!=Made.BANK&&c.made()!=Made.BANK&&a.made()!=Made.TENDRIL&&c.made()!=Made.TENDRIL&&c.zSouth()==a.zNorth()-1&&c.x0()<=a.x1()&&c.x1()>=a.x0();}
    private static String name(Piece first,Piece last){
        String a=first.name().replaceAll(" [0-9]+$",""),c=last.name().replaceAll(" [0-9]+$","");
        return a.equals(c)?a:a+" and "+c;
    }
    /**
     * The island somebody has under them, standing at a place (from the park's base): the one whose ground their feet are on, or
     * that a wheel is still on at its brink. -1 on a lid, on anything standing on a piece, or over nothing.
     */
    public static int stood(double x,double y,double z){
        for(double dx:new double[]{0,-.3,.3})for(double dz:new double[]{0,-.3,.3}){
            int bx=(int)Math.floor(x+dx),bz=(int)Math.floor(z+dz);
            for(int i=0;i<ALL.size();i++){var p=ALL.get(i);if(p.has(bx,bz)&&Math.abs(y-p.top())<.1)return OF[i];}
        }
        if(Math.abs(y-CalderGraves.level(EDGE))>=.1)return -1;
        // The hill reaches as far as three rows past its edge, off the gate's line.
        return z>=EDGE-3.3?HILL:z<FAR_SIDE+1.3?FAR_ISLAND:-1;
    }
    /**
     * The lowest ground anywhere near somebody who is that far north and that far across: under it by enough, they have missed
     * everything. Near is NEAR along the way and ASIDE across it (0.4.504: the steep way runs four blocks under the top, off to
     * one side of it, and is not what somebody who has come off the top's other side is going to land on). Somebody with
     * nothing that near across is over nothing at all: for them it is the lowest ground that far north, wherever across it is.
     */
    public static final int ASIDE=3;
    public static double floor(double x,double z){
        double low=Double.POSITIVE_INFINITY,any=Double.POSITIVE_INFINITY;
        for(var p:ALL)if(z<=p.zSouth()+1+NEAR&&z>=p.zNorth()-NEAR){any=Math.min(any,p.top());if(x>=p.x0()-ASIDE&&x<=p.x1()+1+ASIDE)low=Math.min(low,p.top());}
        if(z>=EDGE-NEAR||z<=FAR_SIDE+1+NEAR)low=Math.min(low,CalderGraves.level(EDGE));
        if(low==Double.POSITIVE_INFINITY)low=any;
        return low==Double.POSITIVE_INFINITY?0:low;
    }

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        NovelRooms.box(l,b,Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1),Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1),s);
    }
    private static BlockState mat(String name){return BikeNightSite.mat(name);}
    private static BlockState grave(CalderGraveBlock.Kind kind,Direction facing,int stage){return CalderGraveBlock.of(kind,facing,stage);}
    private static BlockState half(Block slab,boolean upper){return slab.defaultBlockState().setValue(SlabBlock.TYPE,upper?SlabType.TOP:SlabType.BOTTOM);}

    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(49400);var air=Blocks.AIR.defaultBlockState();
        // Past the edge there is nothing: the hill's ground, its back railings and the trees behind them are gone from here on.
        box(l,b,CalderGraves.W,-4,FAR,CalderGraves.E,CalderGraves.TOP,EDGE-1,air);
        edge(l,b,rnd);
        for(var p:ALL)piece(l,b,p,rnd);
        dress(l,b);
        farSide(l,b,rnd);
        for(var p:WOLFS)piece(l,b,p,rnd);
        for(int row=0;row<TIGHT_ROWS;row++)tendrilRow(l,b,row,true);
    }
    /** The hill's ground ends ragged: off the gate's line it reaches a row or three further. (0.4.521: the expanse's ground; there are no side railings to end.) */
    static void edge(ServerLevel l,BlockPos b,Random rnd){
        int y=CalderGraves.level(EDGE);var sod=mat("hollow_sod");
        for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){
            // Not beside the first piece of the way: the only way onto it is over the gap.
            int more=Math.abs(x)<=6?0:1+rnd.nextInt(3);
            // Nor at the dark way's mouth: its first gap is the same from wherever along it somebody leaves the hill.
            if(x>=DARK_MOUTH_W&&x<=DARK_MOUTH_E)more=0;
            for(int z=EDGE-1;z>=EDGE-more;z--){set(l,b,x,y-1,z,sod);if(rnd.nextInt(3)==0)set(l,b,x,y-2,z,grave(CalderGraveBlock.Kind.ROOTS,Direction.NORTH,rnd.nextInt(3)));}
            // The row the ground ends on is sod too: it is the side of the hill now.
            set(l,b,x,y-1,EDGE,sod);
        }
        // 0.4.521: the expanse has no side railings, so nothing ends in a pier here.
    }
    /** One piece: its ground at its height, and under a piece of sod, here and there, roots. */
    static void piece(ServerLevel l,BlockPos b,Piece p,Random rnd){
        boolean whole=p.top()==Math.floor(p.top());int y=(int)Math.floor(p.top());
        for(int x=p.x0();x<=p.x1();x++)for(int z=p.zNorth();z<=p.zSouth();z++){
            switch(p.made()){
                case SOD->{
                    if(whole){set(l,b,x,y-1,z,mat("hollow_sod"));if(rnd.nextInt(3)==0)set(l,b,x,y-2,z,grave(CalderGraveBlock.Kind.ROOTS,Direction.NORTH,rnd.nextInt(3)));}
                    else set(l,b,x,y,z,half(BikeNightRegistry.GRAVE_TURF_SLAB.get(),false));
                }
                case LID->set(l,b,x,y-1,z,half(BikeNightRegistry.COFFIN_LID.get(),true));
                case VINE->{if(whole)set(l,b,x,y-1,z,mat("vine"));else set(l,b,x,y,z,half(BikeNightRegistry.VINE_SLAB.get(),false));}
                case STONE->{if(whole)set(l,b,x,y-1,z,half(BikeNightRegistry.STONE_STEP.get(),true));else set(l,b,x,y,z,half(BikeNightRegistry.STONE_STEP.get(),false));}
                case EARTH->set(l,b,x,y-1,z,mat("hollow_earth"));
                // The tendril's walls and gates are stood on it when the course is built (tendrilRow).
                case TENDRIL->set(l,b,x,y-1,z,mat("vine"));
                // A bank is earth all the way down to its foot.
                case BANK->{for(int under=BANK_FOOT-1;under<y;under++)set(l,b,x,under,z,mat("hollow_earth"));}
            }
        }
    }
    /** What stands on the pieces: the headstones still in the graves, and the fences. */
    static void dress(ServerLevel l,BlockPos b){
        var stone=CalderGraveBlock.Kind.STONE;var picket=CalderGraveBlock.Kind.PICKET;var south=Direction.SOUTH;
        // {x, z, kind's ordinal, stage}: each stands on its piece, at its piece's height.
        Object[][] things={{-2,-1322,stone,1},{2,-1324,stone,6},{-1,-1331,CalderGraveBlock.Kind.CROSS,1},{-4,-1340,stone,9},{0,-1343,stone,13},
            {-2,-1353,picket,0},{-1,-1353,picket,1},{1,-1361,picket,2},{2,-1361,picket,0},{-2,-1357,picket,3},{2,-1366,picket,3},{-2,-1368,picket,2},
            {-3,-1391,stone,3},{3,-1392,stone,14},{-3,-1447,stone,10},{3,-1448,CalderGraveBlock.Kind.CROSS,0},
            // The high way's: along its east side, which leaves two blocks to ride on.
            {6,-1343,stone,5},{6,-1352,stone,11},{6,-1362,CalderGraveBlock.Kind.CROSS,0},{6,-1370,stone,2},{6,-1378,stone,8}};
        for(var t:things){
            int x=(Integer)t[0],z=(Integer)t[1];double top=top(x,z);
            if(Double.isNaN(top)||top!=Math.floor(top))throw new IllegalStateException("nothing whole to stand a thing on at "+x+","+z);
            set(l,b,x,(int)top,z,grave((CalderGraveBlock.Kind)t[2],south,(Integer)t[3]));
        }
    }
    /**
     * The far side: the cemetery's ground again, at the height of the hill's top, with a few of its stones; its own railings round
     * it, the back ones with the back gate in them, shut; and behind those the lane.
     */
    static void farSide(ServerLevel l,BlockPos b,Random rnd){
        int y=CalderGraves.level(EDGE);var turf=mat("grave_turf");var sod=mat("hollow_sod");var dirt=Blocks.DIRT.defaultBlockState();
        for(int x=-SIDE-1;x<=SIDE+1;x++)for(int z=FAR;z<=FAR_SIDE;z++){
            boolean lane=z<=LANE_S&&z>=LANE_N;
            box(l,b,x,y-3,z,x,y-2,z,z==FAR_SIDE?sod:dirt);
            set(l,b,x,y-1,z,z==FAR_SIDE?sod:lane?mat("asphalt"):x>=CalderGraves.PATH_W&&x<=CalderGraves.PATH_E&&z>BACK?mat("gravel"):turf);
        }
        // Railings: down both sides from where the ground begins, and across the back with the gate in the gate's line.
        int pw=CalderGraves.PATH_W-1,pe=CalderGraves.PATH_E+1;
        for(int z=FAR_SIDE;z>BACK;z--)for(int x:new int[]{-SIDE-1,SIDE+1}){
            if(z==FAR_SIDE||(FAR_SIDE-z)%7==0)pier(l,b,x,y,z);else rail(l,b,x,y,z,Direction.EAST);
        }
        back(l,b,0,-SIDE-1,SIDE+1);
        int[][] stones={{-18,-1480,2},{-12,-1483,7},{-7,-1479,12},{8,-1481,4},{14,-1484,15},{20,-1480,8},{-20,-1487,0},{11,-1488,5}};
        for(int[] s:stones)set(l,b,s[0],y,s[1],grave(CalderGraveBlock.Kind.STONE,Direction.SOUTH,s[2]));
    }
    /**
     * The back railings with the back gate in them, from {@code west} to {@code east}, and behind them the lane, the wall along its far
     * verge and round its ends: on the Hollow's far side ({@code dz} 0), where it is the railings' own width; and (0.4.521) behind
     * the town's cemetery ({@code dz} BikeNightFold.EXIT), across the cemetery's width, where the lane is somebody who comes through
     * the far side's gate is carried to. Only the town's wall has the doorway in it: nobody stands on the far side's lane.
     */
    public static void back(ServerLevel l,BlockPos b,int dz,int west,int east){
        int y=CalderGraves.level(EDGE);var turf=mat("grave_turf");var dirt=Blocks.DIRT.defaultBlockState();var brick=mat("brick");
        if(dz!=0)for(int x=-SIDE-1;x<=SIDE+1;x++)for(int z=WALL+1;z<BACK;z++){
            box(l,b,x,y-3,z+dz,x,y-2,z+dz,dirt);
            set(l,b,x,y-1,z+dz,z<=LANE_S&&z>=LANE_N?mat("asphalt"):turf);
        }
        int pw=CalderGraves.PATH_W-1,pe=CalderGraves.PATH_E+1;
        for(int x=west;x<=east;x++){
            if(dz!=0)box(l,b,x,y-3,BACK+dz,x,y-1,BACK+dz,dirt);
            if(x>=CalderGraves.PATH_W&&x<=CalderGraves.PATH_E){
                var facing=(x-CalderGraves.PATH_W)%2==0?Direction.NORTH:Direction.SOUTH;
                set(l,b,x,y,BACK+dz,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,0));set(l,b,x,y+1,BACK+dz,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,1));
            }else if(x==pw||x==pe||x==west||x==east||Math.abs(x)==SIDE+1||(x<pw&&(pw-x)%7==0)||(x>pe&&(x-pe)%7==0))pier(l,b,x,y,BACK+dz);
            else rail(l,b,x,y,BACK+dz,Direction.NORTH);
        }
        // 0.4.508: the wall along the lane's far verge and round its ends, capped, with the doorway left in it.
        for(int x=-SIDE-1;x<=SIDE+1;x++){
            box(l,b,x,y-3,WALL+dz,x,y-1,WALL+dz,dirt);
            boolean doorway=x==DOOR_X&&dz!=0;
            for(int dy=0;dy<WALL_HIGH;dy++)if(!doorway||dy>=2)set(l,b,x,y+dy,WALL+dz,brick);
            set(l,b,x,y+WALL_HIGH,WALL+dz,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));
        }
        for(int x:new int[]{-SIDE-1,SIDE+1})for(int z=FAR;z<BACK;z++){
            for(int dy=0;dy<WALL_HIGH;dy++)set(l,b,x,y+dy,z+dz,brick);
            set(l,b,x,y+WALL_HIGH,z+dz,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));
        }
    }
    /**
     * 0.4.498, the back gate: open, its leaves end-on on the lane's side of its piers, or shut as it is built. (0.4.521: both of them,
     * the far side's and the town cemetery's, together.) "The riders come out
     * through the back gate onto a quiet lane above the town."
     */
    public static void backGate(ServerLevel l,BlockPos b,boolean open){
        for(int dz:new int[]{0,BikeNightFold.EXIT})if(backGateLoaded(l,b,dz))backGate(l,b,open,dz);
    }
    /** Whether one of the two back gates (dz 0 the Hollow's, EXIT the town cemetery's) has its ground in. */
    public static boolean backGateLoaded(ServerLevel l,BlockPos b,int dz){
        int y=CalderGraves.level(EDGE);return l.isLoaded(b.offset(CalderGraves.PATH_W,y,BACK+dz))&&l.isLoaded(b.offset(CalderGraves.PATH_E,y,BACK+dz-2));
    }
    /** Whether one of the two back gates stands open: nothing between its piers. */
    public static boolean backGateOpen(ServerLevel l,BlockPos b,int dz){
        return l.getBlockState(b.offset(CalderGraves.PATH_W,CalderGraves.level(EDGE),BACK+dz)).isAir();
    }
    static void backGate(ServerLevel l,BlockPos o,boolean open,int dz){
        var b=o.offset(0,0,dz);
        int y=CalderGraves.level(EDGE);var air=Blocks.AIR.defaultBlockState();var gate=CalderBlock.Kind.GATE;
        for(int x:new int[]{CalderGraves.PATH_W,CalderGraves.PATH_E})for(int z=BACK-2;z<=BACK-1;z++)for(int dy=0;dy<2;dy++)if(CalderBlock.is(l.getBlockState(b.offset(x,y+dy,z)),gate))l.setBlock(b.offset(x,y+dy,z),air,3);
        for(int x=CalderGraves.PATH_W;x<=CalderGraves.PATH_E;x++)for(int dy=0;dy<2;dy++){
            var facing=(x-CalderGraves.PATH_W)%2==0?Direction.NORTH:Direction.SOUTH;
            l.setBlock(b.offset(x,y+dy,BACK),open?air:BikeNightSite.fit(gate,facing,dy),3);
        }
        if(open)for(int x:new int[]{CalderGraves.PATH_W,CalderGraves.PATH_E})for(int dy=0;dy<2;dy++){
            l.setBlock(b.offset(x,y+dy,BACK-1),BikeNightSite.fit(gate,Direction.WEST,dy),3);
            l.setBlock(b.offset(x,y+dy,BACK-2),BikeNightSite.fit(gate,Direction.EAST,dy),3);
        }
    }
    /**
     * 0.4.509. "the taken cap is hanging on the gate": the cell it hangs in, against the town's side of the back gate's east post,
     * at a child's eye; and where somebody on a bike stops to reach it.
     */
    public static final int CAP_X=CalderGraves.PATH_E+1,CAP_Z=BACK+1,CAP_UP=1;
    public static final Vec3 CAP_BY=new Vec3(CAP_X+.5,CalderGraves.level(EDGE),CAP_Z+1.4);
    /** The cap hung there, or taken down. True if the cell is as asked: nothing else is ever put out of it. */
    public static boolean hungCap(ServerLevel l,BlockPos b,boolean hung){
        var at=b.offset(CAP_X,CalderGraves.level(EDGE)+CAP_UP,CAP_Z);var now=l.getBlockState(at);boolean is=CalderBlock.is(now,CalderBlock.Kind.HAT);
        if(hung==is)return true;
        if(hung&&!now.isAir())return false;
        l.setBlock(at,hung?BikeNightSite.fit(CalderBlock.Kind.HAT,Direction.SOUTH,0):Blocks.AIR.defaultBlockState(),3);return true;
    }
    public static boolean capHung(ServerLevel l,BlockPos b){return CalderBlock.is(l.getBlockState(b.offset(CAP_X,CalderGraves.level(EDGE)+CAP_UP,CAP_Z)),CalderBlock.Kind.HAT);}
    public static boolean backGateOpen(ServerLevel l,BlockPos b){return !CalderBlock.is(l.getBlockState(b.offset(CalderGraves.PATH_W,CalderGraves.level(EDGE),BACK)),CalderBlock.Kind.GATE);}
    private static void pier(ServerLevel l,BlockPos b,int x,int y,int z){box(l,b,x,y,z,x,y+1,z,mat("brick"));set(l,b,x,y+2,z,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));}
    private static void rail(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing){set(l,b,x,y,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,0));set(l,b,x,y+1,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,1));}
}
