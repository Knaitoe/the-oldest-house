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
    public static final int EDGE=-261,FAR_SIDE=-420,BACK=-435,FAR=-442;
    /** The far side: how wide its ground is, and the lane behind its railings. */
    public static final int SIDE=24,LANE_N=-440,LANE_S=-437;
    /** What a piece of the course is made of. */
    public enum Made{SOD,LID,VINE,STONE}
    /** A piece: a rectangle of ground at one height. */
    public record Piece(String name,Made made,int x0,int x1,int zSouth,int zNorth,double top){
        public boolean has(int x,int z){return x>=x0&&x<=x1&&z<=zSouth&&z>=zNorth;}
    }
    /** The plain way, from the hill to the far side, in the order it is ridden. */
    public static final List<Piece> WAY=way();
    private static List<Piece> way(){
        var out=new ArrayList<Piece>();
        // Graves that have come away, each a little off the line of the last.
        out.add(new Piece("first grave",Made.SOD,-3,3,-264,-270,2));
        out.add(new Piece("second grave",Made.SOD,-1,5,-273,-279,2));
        out.add(new Piece("third grave",Made.SOD,-5,1,-282,-288,1));
        // "crooked fences": a long strip with board fence half across it, one side and then the other.
        out.add(new Piece("the fences",Made.SOD,-2,2,-291,-314,1));
        // "a bridge of coffin lids": two blocks of lid, one of nothing. (The gap before it is the one of three.)
        for(int i=0;i<5;i++)out.add(new Piece("lid "+(i+1),Made.LID,-1,0,-318-3*i,-319-3*i,0));
        out.add(new Piece("the grave past the lids",Made.SOD,-3,3,-333,-338,0));
        // "a pumpkin vine the size of a road, curling up and down": up three blocks by half blocks, over, and down, swinging east and back.
        int[] east={0,0,0,0,1,1,1,2,2,2,2,2,2,2,1,1,1,0,0,0,0,0,0,0,0,0,0,0};
        double[] high={0,0,.5,.5,1,1,1.5,1.5,2,2,2.5,2.5,3,3,3,3,3,3,2.5,2.5,2,2,1.5,1.5,1.5,1.5,1.5,1.5};
        for(int i=0;i<east.length;i++)out.add(new Piece("vine "+i,Made.VINE,-1+east[i],east[i],-341-i,-341-i,high[i]));
        // "stairs of tilted stones": up from 1 to 5, half a block every two.
        for(int i=0;i<9;i++)out.add(new Piece("stair "+i,Made.STONE,-1,1,-371-2*i,-372-2*i,1+.5*i));
        out.add(new Piece("the top",Made.SOD,-3,3,-389,-394,5));
        // The last stretch: a strip running down to the far side, with one gap in it.
        for(int i=0;i<4;i++)out.add(new Piece("stretch "+i,Made.SOD,-1,1,-397-3*i,-399-3*i,4-.5*(i/2)));
        for(int i=0;i<3;i++)out.add(new Piece("stretch "+(4+i),Made.SOD,-1,1,-411-3*i,-413-3*i,3));
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
        out.add(new Piece("high grave 1",Made.SOD,4,6,-282,-299,4));
        out.add(new Piece("high grave 2",Made.SOD,4,6,-302,-317,4));
        out.add(new Piece("high grave 3",Made.SOD,4,6,-320,-325,3));
        for(int i=0;i<5;i++)out.add(new Piece("high step "+i,Made.STONE,4,6,-326-i,-326-i,2.5-.5*i));
        // Its foot is long: a bike comes down the steps fast, and has the length of the grave beside it to turn onto it.
        out.add(new Piece("the high way's foot",Made.SOD,4,6,-331,-338,0));
        return List.copyOf(out);
    }
    /**
     * 0.4.497, the long way. The proposal: "long: a run only the Comet's top speed carries". West of the vine, from the grave
     * past the lids to the stairs: a run of coffin lids, {@link #RUN_ROWS} rows, that gives way from its near end once somebody is
     * on it, faster than any bike goes but the Comet, and the Rattler in its burst (BikeNightHollow.run). It is level where the
     * vine climbs three blocks, swings and comes down again; past it, steps up to the stairs' second pair.
     */
    public static final int RUN_X0=-5,RUN_X1=-4,RUN_SOUTH=-341,RUN_ROWS=28;
    public static final List<Piece> LONG=longWay();
    private static List<Piece> longWay(){
        var out=new ArrayList<Piece>();
        out.add(new Piece("the long way's head",Made.SOD,RUN_X0,RUN_X1,-337,-340,0));
        for(int i=0;i<RUN_ROWS;i++)out.add(new Piece("run "+i,Made.LID,RUN_X0,RUN_X1,RUN_SOUTH-i,RUN_SOUTH-i,0));
        out.add(new Piece("long step 0",Made.STONE,RUN_X0,RUN_X1+1,-369,-369,.5));
        out.add(new Piece("long step 1",Made.STONE,RUN_X0,RUN_X1+1,-370,-371,1));
        out.add(new Piece("the long way's foot",Made.SOD,RUN_X0,-2,-372,-375,1.5));
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
    /** Every way, each in the order it is ridden; and every piece of all of them. */
    public static final List<List<Piece>> WAYS=List.of(WAY,HIGH,LONG);
    public static final List<Piece> ALL=WAYS.stream().flatMap(List::stream).toList();
    /** How high a body stands at a place on the course; NaN where there is none of it. */
    public static double top(int x,int z){for(var p:ALL)if(p.has(x,z))return p.top();return Double.NaN;}

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
    public record Island(String name,int zNear,int zFar,double x,double top,int z){
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
        out.add(new Island("the hill",CalderGraves.SMALL_ROW,EDGE,0,top,EDGE+RUN));
        for(var way:WAYS){
            for(int i=0;i<way.size();){
                int j=i;while(j+1<way.size()&&touch(way.get(j),way.get(j+1)))j++;
                int near=way.get(i).zSouth(),end=way.get(j).zNorth();
                if(near-end+1>=STAND){
                    int z=Math.min(near-1,end+RUN),index=out.size();
                    for(int k=i;k<=j;k++){var p=way.get(k);OF[base+k]=index;if(z<=p.zSouth()&&z>=p.zNorth())out.add(new Island(name(way.get(i),way.get(j)),near,end,(p.x0()+p.x1()+1)/2.0,p.top(),z));}
                }
                i=j+1;
            }
            base+=way.size();
            if(way==WAY){far=out.size();out.add(new Island("the far side",FAR_SIDE,BACK+1,0,top,FAR_SIDE-3));}
        }
        FAR_ISLAND=far;ISLANDS=List.copyOf(out);
    }
    /** Lids are never part of an island: the plain way's are a bridge, and the long way's give way. */
    private static boolean touch(Piece a,Piece c){return a.made()!=Made.LID&&c.made()!=Made.LID&&c.zSouth()==a.zNorth()-1&&c.x0()<=a.x1()&&c.x1()>=a.x0();}
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
    /** The lowest ground anywhere near somebody who is that far north: under it by enough, they have missed everything. */
    public static double floor(double z){
        double low=Double.POSITIVE_INFINITY;
        for(var p:ALL)if(z<=p.zSouth()+1+NEAR&&z>=p.zNorth()-NEAR)low=Math.min(low,p.top());
        if(z>=EDGE-NEAR||z<=FAR_SIDE+1+NEAR)low=Math.min(low,CalderGraves.level(EDGE));
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
    }
    /** The hill's ground ends ragged: off the gate's line it reaches a row or three further, and what is left of the side railings ends in a pier. */
    static void edge(ServerLevel l,BlockPos b,Random rnd){
        int y=CalderGraves.level(EDGE);var sod=mat("hollow_sod");
        for(int x=CalderGraves.W+1;x<CalderGraves.E;x++){
            // Not beside the first piece of the way: the only way onto it is over the gap.
            int more=Math.abs(x)<=6?0:1+rnd.nextInt(3);
            for(int z=EDGE-1;z>=EDGE-more;z--){set(l,b,x,y-1,z,sod);if(rnd.nextInt(3)==0)set(l,b,x,y-2,z,grave(CalderGraveBlock.Kind.ROOTS,Direction.NORTH,rnd.nextInt(3)));}
            // The row the ground ends on is sod too: it is the side of the hill now.
            set(l,b,x,y-1,EDGE,sod);
        }
        for(int x:new int[]{CalderGraves.W,CalderGraves.E}){
            box(l,b,x,y,EDGE,x,y+1,EDGE,mat("brick"));set(l,b,x,y+2,EDGE,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));
        }
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
            }
        }
    }
    /** What stands on the pieces: the headstones still in the graves, and the fences. */
    static void dress(ServerLevel l,BlockPos b){
        var stone=CalderGraveBlock.Kind.STONE;var picket=CalderGraveBlock.Kind.PICKET;var south=Direction.SOUTH;
        // {x, z, kind's ordinal, stage}: each stands on its piece, at its piece's height.
        Object[][] things={{-2,-266,stone,1},{2,-268,stone,6},{-1,-275,CalderGraveBlock.Kind.CROSS,1},{-4,-284,stone,9},{0,-287,stone,13},
            {-2,-297,picket,0},{-1,-297,picket,1},{1,-305,picket,2},{2,-305,picket,0},{-2,-301,picket,3},{2,-310,picket,3},{-2,-312,picket,2},
            {-3,-335,stone,3},{3,-336,stone,14},{-3,-391,stone,10},{3,-392,CalderGraveBlock.Kind.CROSS,0},
            // The high way's: along its east side, which leaves two blocks to ride on.
            {6,-287,stone,5},{6,-296,stone,11},{6,-306,CalderGraveBlock.Kind.CROSS,0},{6,-314,stone,2},{6,-322,stone,8}};
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
        for(int x=-SIDE-1;x<=SIDE+1;x++){
            if(x>=CalderGraves.PATH_W&&x<=CalderGraves.PATH_E){
                var facing=(x-CalderGraves.PATH_W)%2==0?Direction.NORTH:Direction.SOUTH;
                set(l,b,x,y,BACK,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,0));set(l,b,x,y+1,BACK,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,1));
            }else if(x==pw||x==pe||Math.abs(x)==SIDE+1||(x<pw&&(pw-x)%7==0)||(x>pe&&(x-pe)%7==0))pier(l,b,x,y,BACK);
            else rail(l,b,x,y,BACK,Direction.NORTH);
        }
        int[][] stones={{-18,-424,2},{-12,-427,7},{-7,-423,12},{8,-425,4},{14,-428,15},{20,-424,8},{-20,-431,0},{11,-432,5}};
        for(int[] s:stones)set(l,b,s[0],y,s[1],grave(CalderGraveBlock.Kind.STONE,Direction.SOUTH,s[2]));
    }
    private static void pier(ServerLevel l,BlockPos b,int x,int y,int z){box(l,b,x,y,z,x,y+1,z,mat("brick"));set(l,b,x,y+2,z,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));}
    private static void rail(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing){set(l,b,x,y,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,0));set(l,b,x,y+1,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,1));}
}
