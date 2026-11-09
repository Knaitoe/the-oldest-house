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
 * 0.4.409, the city on the ice (docs/THE_BLACK_CITY_0_4_409.md): an expedition's camp on the ice, a pass between peaks too regular to be
 * peaks, and beyond it a city of dark stone with a ramp turning down into a pit, a tunnel under it, and a warm shore. Every position is
 * relative to the base, the floor just inside the door the reader comes through; north is -z; people stand at y 0 on ground at y -1.
 *
 * <p>The reader comes out of the House's door in the back wall of the radio hut. Its front door opens onto the camp: torn tents, an empty
 * dog line, the geology tent, a dissecting table under a tarp, an aeroplane with its fabric cut, and the mess hut, whose door is the way on.
 * North of the camp a pass climbs on cut steps between walls of rock to a crest; three nooks in its east wall are out of the wind. From
 * the crest a stair goes down into the city: a plaza of dark stone, low walls in rings, five stepped towers, and in the middle a square pit.
 * A ramp turns down its walls, one turn and a half, past twelve carved panels, to a landing, and a tunnel runs north from it to a warm
 * shore on black water under the far peaks.
 */
public final class CitySite {
    private CitySite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BLACK_CITY;
    /** Above the slot stack, so the pit and the tunnel under it have room: the camp's floor stands at this height in the outside. */
    public static final int BASE_Y=100;
    public static final int WEST=-64,EAST=64,NORTH=-192,BOTTOM=-46,TOP=60;
    /** The radio hut, its front door, the radio on the crates and the log and the message pad either side of it; the hut's lamp. */
    public static final int HUT_W=-5,HUT_E=5,HUT_S=1,HUT_N=-8;
    public static final BlockPos HUT_DOOR=new BlockPos(0,0,HUT_N),RADIO=new BlockPos(4,1,-3),LOGBOOK=new BlockPos(4,1,-4),PAD=new BlockPos(4,1,-2),HUT_LAMP=new BlockPos(0,3,-4);
    /** Where a reader taken in the tunnel comes to: on the ice in front of the hut. */
    public static final Vec3 WAKE=new Vec3(.5,0,-11.5);
    /** The camp's flat ice, and the mess hut whose door is the way on. */
    public static final int CAMP_N=-38,CAMP_HALF=36;
    public static final BlockPos ONWARD=new BlockPos(-13,0,-28);
    /** The pass: the cut steps up, the crest, the stair down into the city; the city from there north. */
    public static final int CREST_S=-71,CREST_N=-78,DESCENT_N=-102,BASIN_N=-182,BASIN_HALF=56,PATH=3;
    /** The pit: its middle, its rim wall (16 and 17 out), the ramp's band (12 to 15 out) and the shaft inside it. */
    public static final int PIT_X=0,PIT_Z=-140,RIM=16,BAND_IN=12,BAND_OUT=15,SHAFT=11;
    /** One turn of the ramp along its middle; its whole length, from the rim to the landing; how far down it goes. */
    public static final double TURN=108,RAMP=175.5,DROP=30;
    /** The tunnel north from the landing, and the cavern it opens on: the shore, then the water. */
    public static final int TUNNEL_S=-157,TUNNEL_N=-175,CAVE_S=-177,SHORE_N=-181,CAVE_N=-190,FLOOR=-30;
    /** The foot of the ladder out of the shaft's water, against the landing's curb. */
    public static final BlockPos SHAFT_LADDER=new BlockPos(4,-35,-151);
    /** The nooks out of the wind in the pass's east wall, at these rows. */
    public static final int[] NOOKS={-54,-64,-74};
    /** The five stepped towers around the pit. */
    public static final int[][] TOWERS={{0,-176},{34,-150},{24,-112},{-24,-112},{-34,-150}};
    /** The lamps in the tunnel and on the shore: they go out as what fills the tunnel passes them. */
    public static final List<BlockPos> LAMPS=List.of(new BlockPos(0,-26,-160),new BlockPos(0,-26,-166),new BlockPos(0,-26,-172),new BlockPos(-6,-29,-179),new BlockPos(6,-29,-179));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ the pass's ground
    /** The height people stand at on the path through the pass, row by row; 0 on the camp's ice and in the city. */
    public static int stand(int z){
        if(z>=CAMP_N)return 0;
        if(z>=CREST_S+1)return (int)Math.floor((CAMP_N-z)*.75+.5);
        if(z>=CREST_N)return 24;
        if(z>=DESCENT_N)return 24-(CREST_N-z);
        return 0;
    }

    // ------------------------------------------------------------------------------------------------ the ramp
    /** Where a cell of the band is along one turn of the ramp's middle, from the south-east corner going west, then north, east and south. */
    static double along(int u,int v){
        if(v>=Math.abs(u))return 13.5-u;
        if(-u>=Math.abs(v))return 27+(13.5-v);
        if(-v>=Math.abs(u))return 54+(u+13.5);
        return 81+(v+13.5);
    }
    /** The height people stand at a distance along the ramp, in half blocks; past its end, the landing. */
    public static double rampStand(double s){return s>RAMP?-DROP:Math.round(2.0*(-DROP*s/RAMP))/2.0;}
    public static int cheb(int x,int z){return Math.max(Math.abs(x-PIT_X),Math.abs(z-PIT_Z));}
    static int cheb(Vec3 r){return cheb((int)Math.floor(r.x),(int)Math.floor(r.z));}
    /** One floor cell of the ramp: where it is, its distance along, and the height people stand there. */
    record Floor(int x,int z,int turn,double s,double stand){}
    public static final List<Floor> FLOORS;
    /** One carved panel: its twelve cells and their parts, the way it faces, and the middle of its face. */
    public record Mural(int index,List<BlockPos> cells,List<Integer> parts,Direction facing,Vec3 centre,double s){}
    public static final List<Mural> MURALS;
    /** What fills the tunnel, slice by slice from the far shore to the rim: the empty cells it takes as it comes. */
    public static final List<List<BlockPos>> CHASE;
    /** Where the chase begins (the far edge of the shore) and the slice that is the landing at the foot of the ramp. */
    public static final int LANDING_SLICE=26;
    static{
        var floors=new ArrayList<Floor>();
        for(int x=-BAND_OUT;x<=BAND_OUT;x++)for(int z=PIT_Z-BAND_OUT;z<=PIT_Z+BAND_OUT;z++){
            int u=x-PIT_X,v=z-PIT_Z,d=Math.max(Math.abs(u),Math.abs(v));if(d<BAND_IN||d>BAND_OUT)continue;
            double s0=along(u,v);
            floors.add(new Floor(x,z,0,Math.max(0,s0),rampStand(Math.max(0,s0))));
            double s1=TURN+s0;floors.add(new Floor(x,z,1,Math.min(s1,RAMP+1),rampStand(s1)));
        }
        FLOORS=List.copyOf(floors);
        // The panels: each at a distance along a side of a turn, four cells wide, three high, its foot at the height people stand.
        int[][] at={{0,0,19},{0,1,8},{0,1,19},{0,2,8},{0,2,19},{0,3,8},{0,3,19},{1,0,8},{1,0,19},{1,1,8},{1,1,19},{1,2,8}};
        var murals=new ArrayList<Mural>();
        for(int i=0;i<at.length;i++){
            int turn=at[i][0],side=at[i][1],o=at[i][2];double s=turn*TURN+side*27+o;double centre=13.5-o;
            int lo=(int)Math.round(centre-1.5),foot=(int)Math.ceil(rampStand(s));
            var cells=new ArrayList<BlockPos>();var parts=new ArrayList<Integer>();Direction facing;Vec3 mid;
            // Which way along the wall each column runs, so the picture reads from the viewer's left.
            switch(side){
                case 0->{facing=Direction.NORTH;for(int y=foot;y<foot+3;y++)for(int u=lo;u<lo+4;u++){cells.add(new BlockPos(PIT_X+u,y,PIT_Z+RIM));parts.add((foot+2-y)*4+(lo+3-u));}mid=new Vec3(PIT_X+lo+2,foot+1.5,PIT_Z+RIM);}
                case 1->{facing=Direction.EAST;int vlo=(int)Math.round(centre-1.5);for(int y=foot;y<foot+3;y++)for(int v=vlo;v<vlo+4;v++){cells.add(new BlockPos(PIT_X-RIM,y,PIT_Z+v));parts.add((foot+2-y)*4+(vlo+3-v));}mid=new Vec3(PIT_X-RIM+1,foot+1.5,PIT_Z+vlo+2);}
                case 2->{facing=Direction.SOUTH;int ulo=(int)Math.round(-centre-1.5);for(int y=foot;y<foot+3;y++)for(int u=ulo;u<ulo+4;u++){cells.add(new BlockPos(PIT_X+u,y,PIT_Z-RIM));parts.add((foot+2-y)*4+(u-ulo));}mid=new Vec3(PIT_X+ulo+2,foot+1.5,PIT_Z-RIM+1);}
                default->{facing=Direction.WEST;int vlo=(int)Math.round(-centre-1.5);for(int y=foot;y<foot+3;y++)for(int v=vlo;v<vlo+4;v++){cells.add(new BlockPos(PIT_X+RIM,y,PIT_Z+v));parts.add((foot+2-y)*4+(v-vlo));}mid=new Vec3(PIT_X+RIM,foot+1.5,PIT_Z+vlo+2);}
            }
            murals.add(new Mural(i,List.copyOf(cells),List.copyOf(parts),facing,mid,s));
        }
        MURALS=List.copyOf(murals);
        // The chase: the shore's breadth over the water's edge, the tunnel's mouth and length, the pit wall, then the ramp back up.
        var slices=new ArrayList<List<BlockPos>>();
        int top=LANDING_SLICE+(int)Math.floor(RAMP);
        for(int i=0;i<=top;i++)slices.add(new ArrayList<>());
        for(int z=-182;z<=CAVE_S;z++)for(int x=-14;x<=14;x++)for(int y=FLOOR;y<FLOOR+4;y++)slices.get(z+182).add(new BlockPos(x,y,z));
        for(int z=CAVE_S+1;z<=PIT_Z-RIM;z++)for(int x=-2;x<=2;x++)for(int y=FLOOR;y<FLOOR+5;y++)slices.get(z+182).add(new BlockPos(x,y,z));
        for(var f:FLOORS){
            int d=cheb(f.x,f.z);if(d==BAND_IN)continue;
            int i=f.s>=RAMP?LANDING_SLICE:LANDING_SLICE+(int)Math.floor(RAMP-f.s);
            int from=(int)Math.ceil(f.stand);
            for(int y=from;y<from+4;y++)slices.get(Math.min(top,i)).add(new BlockPos(f.x,y,f.z));
        }
        var frozen=new ArrayList<List<BlockPos>>();for(var s:slices)frozen.add(List.copyOf(s));
        CHASE=List.copyOf(frozen);
    }

    // ------------------------------------------------------------------------------------------------ where things are
    /** In the radio hut. */
    public static boolean inHut(Vec3 r){return r.x>HUT_W+.5&&r.x<HUT_E+.5&&r.z<HUT_S&&r.z>HUT_N+.5&&r.y>-.5&&r.y<4;}
    /** On the camp's ice, or in its huts. */
    public static boolean inCamp(Vec3 r){return r.z>CAMP_N-.5&&r.y>-2&&r.y<12;}
    /** On the path through the pass, from the camp's edge to the foot of the stair into the city. */
    public static boolean onPass(Vec3 r){return r.z<=CAMP_N-.5&&r.z>DESCENT_N-.5&&Math.abs(r.x-.5)<PATH+3.5&&r.y>-2&&r.y<30;}
    static boolean nook(int z){for(int n:NOOKS)if(n==z)return true;return false;}
    /** In one of the nooks out of the wind. */
    public static boolean sheltered(Vec3 r){for(int z:NOOKS)if(r.x>PATH+.6&&r.x<PATH+3.2&&r.z>z-.2&&r.z<z+1.2)return true;return false;}
    /** Out in the wind: the upper pass and the crest, outside the nooks. */
    public static boolean exposed(Vec3 r){return onPass(r)&&r.z<-49.5&&r.z>CREST_N-1.5&&!sheltered(r);}
    /** On the crest, where the city and the far peaks are in view. */
    public static boolean atLookout(Vec3 r){return r.z<CREST_S+1.5&&r.z>CREST_N-.5&&Math.abs(r.x-.5)<PATH+1&&r.y>22&&r.y<28;}
    /** In the city: the plaza, the towers and the pit. */
    public static boolean inCity(Vec3 r){return r.z<DESCENT_N+.5&&r.z>BASIN_N-.5;}
    /** In the pit: on the ramp, on the landing or in the shaft's water. */
    public static boolean inPit(Vec3 r){return cheb(r)<RIM&&r.y<.3&&r.y>-40;}
    /** In the tunnel or the cavern. */
    public static boolean underground(Vec3 r){return r.z<PIT_Z-RIM+.5&&r.z>CAVE_N-1&&r.y<FLOOR+12&&r.y>FLOOR-6&&Math.abs(r.x)<16;}
    /** On the shore, at the water's edge under the far peaks. */
    public static boolean onShore(Vec3 r){return underground(r)&&r.z<CAVE_S+.5&&r.z>SHORE_N-1;}
    /** Where the chase is: the cavern, the tunnel and the pit. */
    public static boolean inChase(Vec3 r){return underground(r)||inPit(r);}
    /**
     * How far along the chase a body is, in blocks from the far edge of the shore toward the rim; NaN out of it. On the ramp the turn is
     * taken from the height the body stands at.
     */
    public static double chase(Vec3 r){
        if(underground(r))return Math.max(0,r.z+182);
        if(!inPit(r))return Double.NaN;
        int x=(int)Math.floor(r.x),z=(int)Math.floor(r.z),d=cheb(x,z);
        if(d<BAND_IN)return LANDING_SLICE;
        double s0=along(x-PIT_X,z-PIT_Z),a=Math.max(0,s0),b=TURN+s0;
        double s=Math.abs(r.y-rampStand(a))<=Math.abs(r.y-rampStand(b))?a:Math.min(RAMP,b);
        return LANDING_SLICE+(RAMP-s);
    }
    /** The panel a cell belongs to, or -1. */
    public static int mural(BlockPos rel){for(var m:MURALS)if(m.cells.contains(rel))return m.index;return -1;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return CityRegistry.material(name).defaultBlockState();}
    static BlockState fit(CityBlock.Kind kind,Direction facing){return CityBlock.of(kind,facing,0);}
    static BlockState fit(CityBlock.Kind kind,Direction facing,int stage){return CityBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState(),ROCK=Blocks.DEEPSLATE.defaultBlockState(),WATER=Blocks.WATER.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        terrain(l,b);
        hut(l,b);
        camp(l,b);
        pass(l,b);
        towers(l,b);
        rings(l,b);
        pit(l,b);
        tunnel(l,b);
        murals(l,b);
        for(var at:LAMPS)set(l,b,at,fit(CityBlock.Kind.LAMP,Direction.NORTH,1));
        drifts(l,b);
    }

    // ------------------------------------------------------------------------------------------------ the ground
    /** How high a stepped peak stands at a cell: four lower for every two cells out from its middle, nothing past twelve. */
    private static int peak(int x,int z,int cx,int cz,int top,int reach){int c=Math.max(Math.abs(x-cx),Math.abs(z-cz));return c>reach?Integer.MIN_VALUE:top-4*(c/2);}
    private static final int[][] PEAKS={{22,-46,50,12},{-22,-46,50,12},{22,-62,52,12},{-22,-62,52,12},{22,-88,50,12},{-22,-88,50,12},{42,-54,56,12},{-42,-54,56,12},{42,-80,56,12},{-42,-80,56,12},
        {-44,-189,58,6},{-22,-188,60,6},{0,-189,60,6},{22,-188,60,6},{44,-189,58,6}};
    /** A little unevenness to the rock, from a fixed seed. */
    private static int rough(int x,int z){long h=(x*734287L)^(z*912271L)^0x5DEECE66DL;h^=h>>>13;h*=0x9E3779B97F4A7C15L;return (int)((h>>>58)&3);}
    /** The top of the ground at a cell, and whether it is a mountainside rather than somewhere people walk; MIN_VALUE where the pit is. */
    static int ground(int x,int z){
        int ax=Math.abs(x);
        if(z>=CAMP_N){int t=ax<=CAMP_HALF?-1:Math.min(52,-1+(ax-CAMP_HALF)*3);if(z>=-3&&ax>=7)t=Math.max(t,-1+(z+4)*3);return t;}
        if(z>=DESCENT_N){int s=stand(z);if(ax<=PATH)return s-1;if(ax<=6)return s+1+(ax-PATH)*2;return Math.min(54,s+8+(int)((ax-6)*1.5)+rough(x,z));}
        if(z>=BASIN_N){if(cheb(x,z)<=RIM+1)return Integer.MIN_VALUE;return ax<=BASIN_HALF?-1:Math.min(56,-1+(ax-BASIN_HALF)*4+rough(x,z));}
        return Math.min(58,-1+(BASIN_N-z)*7+rough(x,z));
    }
    static boolean walked(int x,int z){int ax=Math.abs(x);if(z>=CAMP_N)return ax<=CAMP_HALF&&!(z>=-3&&ax>=7);if(z>=DESCENT_N)return ax<=PATH;if(z>=BASIN_N)return ax<=BASIN_HALF;return false;}
    static void terrain(ServerLevel l,BlockPos b){
        int w=EAST-WEST+1,dz=-NORTH+1;int[][] top=new int[w][dz];boolean[][] stepped=new boolean[w][dz];
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=0;z++){
            int t=ground(x,z);
            if(t!=Integer.MIN_VALUE&&!walked(x,z))for(var p:PEAKS){int h=peak(x,z,p[0],p[1],p[2],p[3]);if(h>t){t=h;stepped[x-WEST][-z]=true;}}
            top[x-WEST][-z]=t;
        }
        var snow=mat("snow");var basalt=mat("basalt");var fives=mat("fives");var ice=mat("ice");
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=0;z++){
            int t=top[x-WEST][-z];if(t==Integer.MIN_VALUE)continue;
            // The pass's walls and path are solid to below the path, so the nooks cut into rock; elsewhere a shell four deep.
            // Down to the lowest neighbour as well, so a cliff over lower ground is rock all the way down to it.
            boolean pass=z<CAMP_N&&z>=DESCENT_N&&Math.abs(x)<=6;
            int from=pass?stand(z)-4:t-3;
            boolean flat=true;
            for(int[] n:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=x+n[0],nz=z+n[1];if(nx<WEST||nx>EAST||nz<NORTH||nz>0)continue;int o=top[nx-WEST][-nz];if(o==Integer.MIN_VALUE)continue;if(Math.abs(o-t)>1)flat=false;from=Math.min(from,o);}
            BlockState under=stepped[x-WEST][-z]?basalt:ROCK,surface;
            if(stepped[x-WEST][-z])surface=(t%4==2)?fives:basalt;
            else if(z<DESCENT_N&&z>=BASIN_N&&Math.abs(x)<=BASIN_HALF)surface=basalt;
            else if(flat||z>=CAMP_N&&Math.abs(x)<=CAMP_HALF)surface=rough(x,z)==0&&((x*7+z*3)&3)==0&&z>=CAMP_N&&Math.abs(x)>8?ice:snow;
            else surface=ROCK;
            box(l,b,x,from,z,x,t-1,z,under);set(l,b,x,t,z,surface);
        }
    }

    // ------------------------------------------------------------------------------------------------ the camp
    static void hut(ServerLevel l,BlockPos b){
        var boards=mat("boards");
        box(l,b,HUT_W,-1,HUT_N,HUT_E,-1,HUT_S,boards);
        box(l,b,HUT_W,0,HUT_N,HUT_E,3,HUT_N,boards);box(l,b,HUT_W,0,HUT_N,HUT_W,3,HUT_S,boards);box(l,b,HUT_E,0,HUT_N,HUT_E,3,HUT_S,boards);
        box(l,b,HUT_W,0,HUT_S,HUT_E,3,HUT_S,boards);
        box(l,b,HUT_W-1,4,HUT_N-1,HUT_E+1,4,HUT_S,Blocks.SPRUCE_SLAB.defaultBlockState());
        box(l,b,HUT_W-1,5,HUT_N-1,HUT_E+1,5,HUT_S,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,2));
        box(l,b,HUT_W+1,0,HUT_N+1,HUT_E-1,3,HUT_S-1,AIR);
        set(l,b,HUT_DOOR,Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
        set(l,b,HUT_DOOR.above(),Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        for(int y=1;y<=2;y++){set(l,b,HUT_W,y,-4,Blocks.GLASS_PANE.defaultBlockState());set(l,b,HUT_E,y,-6,Blocks.GLASS_PANE.defaultBlockState());set(l,b,-3,y,HUT_N,Blocks.GLASS_PANE.defaultBlockState());set(l,b,3,y,HUT_N,Blocks.GLASS_PANE.defaultBlockState());}
        // The radio on its crates against the east wall, the log one side of it, the message pad the other.
        for(int z=-2;z>=-4;z--)set(l,b,4,0,z,fit(CityBlock.Kind.CRATE,Direction.WEST));
        set(l,b,RADIO,fit(CityBlock.Kind.RADIO,Direction.WEST));set(l,b,LOGBOOK,fit(CityBlock.Kind.LOGBOOK,Direction.WEST));set(l,b,PAD,fit(CityBlock.Kind.PAD,Direction.WEST));
        set(l,b,-4,0,-6,fit(CityBlock.Kind.STOVE,Direction.EAST));set(l,b,-4,0,-2,fit(CityBlock.Kind.CRATE,Direction.EAST));set(l,b,-4,1,-2,fit(CityBlock.Kind.CRATE,Direction.EAST));set(l,b,-3,0,-2,fit(CityBlock.Kind.CRATE,Direction.EAST));
        set(l,b,HUT_LAMP,fit(CityBlock.Kind.LAMP,Direction.NORTH,1));
    }
    static void tent(ServerLevel l,BlockPos b,int cx,int cz,int seed){
        var canvas=mat("canvas");var rnd=new Random(seed);
        for(int z=cz-2;z<=cz+2;z++){
            int[][] rows={{0,2},{1,2},{2,1},{3,0}};
            for(int[] row:rows){int y=row[0],w=row[1];
                for(int x:new int[]{cx-w,cx+w}){if(rnd.nextInt(5)==0)set(l,b,x,y,z,fit(CityBlock.Kind.FABRIC,x<cx?Direction.WEST:Direction.EAST));else set(l,b,x,y,z,canvas);}
            }
        }
        for(int x=cx-1;x<=cx+1;x++)for(int y=0;y<=1;y++)set(l,b,x,y,cz-2,canvas);
        set(l,b,cx,0,cz+2,AIR);set(l,b,cx,1,cz+2,AIR);
    }
    static void camp(ServerLevel l,BlockPos b){
        var canvas=mat("canvas");var boards=mat("boards");
        tent(l,b,-24,-14,94091);tent(l,b,-30,-24,94092);tent(l,b,-18,-22,94093);
        // The dog line: stakes, the tether between them, the empty harnesses beside it.
        for(int x=6;x<=18;x++)set(l,b,x,0,-12,(x-6)%4==0?Blocks.SPRUCE_FENCE.defaultBlockState():Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.X));
        for(int x=7;x<=17;x+=3)set(l,b,x,0,-13,fit(CityBlock.Kind.HARNESS,Direction.from2DDataValue((x-7)/3)));
        // The geology tent, open at the front, its cores in their boxes.
        tent(l,b,14,-14,94094);for(int z=-13;z>=-15;z--)set(l,b,14,0,z,fit(CityBlock.Kind.CORES,Direction.EAST));
        // The dissecting table under its tarp.
        for(int[] c:new int[][]{{6,-20},{10,-20},{6,-24},{10,-24}})box(l,b,c[0],0,c[1],c[0],2,c[1],Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,6,3,-24,10,3,-20,canvas);set(l,b,8,0,-22,fit(CityBlock.Kind.TABLE,Direction.SOUTH));
        // The aeroplane on its skis: the fuselage along the ice, the wing across it, the tail, the propeller; the fabric opened in cuts.
        var rnd=new Random(94095);
        for(int x=21;x<=32;x++)for(int y=0;y<=2;y++)for(int z=-27;z<=-25;z++){boolean edge=y!=1||z!=-26;if(!edge)continue;set(l,b,x,y,z,rnd.nextInt(4)==0?fit(CityBlock.Kind.FABRIC,z<-26?Direction.NORTH:Direction.SOUTH):canvas);}
        box(l,b,26,2,-34,27,2,-18,canvas);set(l,b,26,2,-29,fit(CityBlock.Kind.FABRIC,Direction.NORTH));set(l,b,27,2,-21,fit(CityBlock.Kind.FABRIC,Direction.SOUTH));
        box(l,b,20,1,-26,20,4,-26,canvas);set(l,b,33,1,-26,fit(CityBlock.Kind.PROP,Direction.EAST));
        // Crates in heaps, and fuel under a lean-to.
        for(int[] c:new int[][]{{-8,-18},{-7,-18},{-8,-19},{12,-30},{13,-30},{-2,-30},{3,-24}})set(l,b,c[0],0,c[1],fit(CityBlock.Kind.CRATE,Direction.NORTH));
        set(l,b,-8,1,-18,fit(CityBlock.Kind.CRATE,Direction.NORTH));
        // The mess hut: its door, the way on, faces the camp.
        box(l,b,-17,-1,-34,-9,-1,-28,boards);box(l,b,-17,0,-34,-9,3,-28,boards);box(l,b,-16,0,-33,-10,3,-29,AIR);
        box(l,b,-18,4,-35,-8,4,-27,Blocks.SPRUCE_SLAB.defaultBlockState());box(l,b,-18,5,-35,-8,5,-27,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,2));
        set(l,b,ONWARD,AIR);set(l,b,ONWARD.above(),AIR);
        set(l,b,-15,1,-28,Blocks.GLASS_PANE.defaultBlockState());set(l,b,-11,1,-28,Blocks.GLASS_PANE.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ the pass
    static void pass(ServerLevel l,BlockPos b){
        var snow=mat("snow");
        for(int z=CAMP_N-1;z>=DESCENT_N;z--){
            int s=stand(z),south=stand(z+1),north=stand(z-1);
            for(int x=-PATH;x<=PATH;x++){
                BlockState st;
                if(s>south)st=(z<CREST_N?Blocks.POLISHED_BLACKSTONE_STAIRS:Blocks.COBBLED_DEEPSLATE_STAIRS).defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
                else if(s>north)st=(z<CREST_N?Blocks.POLISHED_BLACKSTONE_STAIRS:Blocks.COBBLED_DEEPSLATE_STAIRS).defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
                else st=z<CREST_N?mat("basalt"):snow;
                set(l,b,x,s-1,z,st);box(l,b,x,s,z,x,s+12,z,AIR);
            }
            // Fixed-rope stakes along both edges of the steps.
            if(z%4==0&&!nook(z))for(int x:new int[]{-PATH,PATH})set(l,b,x,s,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        // The nooks out of the wind, cut into the east wall.
        for(int z:NOOKS){int s=stand(z);box(l,b,PATH+1,s,z,PATH+2,s+2,z,AIR);set(l,b,PATH+1,s-1,z,snow);set(l,b,PATH+2,s-1,z,snow);}
    }

    // ------------------------------------------------------------------------------------------------ the city
    static void towers(ServerLevel l,BlockPos b){
        var basalt=mat("basalt");var fives=mat("fives");int[] half={7,5,4,2,1};
        for(int[] t:TOWERS){
            int cx=t[0],cz=t[1];
            for(int tier=0;tier<5;tier++){
                int h=half[tier],y0=tier*8,y1=y0+7;
                box(l,b,cx-h,y0,cz-h,cx+h,y1,cz-h,basalt);box(l,b,cx-h,y0,cz+h,cx+h,y1,cz+h,basalt);box(l,b,cx-h,y0,cz-h,cx-h,y1,cz+h,basalt);box(l,b,cx+h,y0,cz-h,cx+h,y1,cz+h,basalt);
                box(l,b,cx-h,y1,cz-h,cx+h,y1,cz+h,fives);
            }
            box(l,b,cx,40,cz,cx,45,cz,basalt);
            // A doorway on the side toward the pit, too tall for anyone who lived here to have been small.
            int dx=Integer.signum(PIT_X-cx),dz=Integer.signum(PIT_Z-cz);if(Math.abs(PIT_X-cx)<Math.abs(PIT_Z-cz))dx=0;else dz=0;
            int wx=cx+dx*half[0],wz=cz+dz*half[0];
            if(dx!=0)box(l,b,wx,0,wz-1,wx,5,wz+1,AIR);else box(l,b,wx-1,0,wz,wx+1,5,wz,AIR);
        }
    }
    static boolean inTower(int x,int z){for(int[] t:TOWERS)if(Math.max(Math.abs(x-t[0]),Math.abs(z-t[1]))<=7)return true;return false;}
    /** Low walls in rings round the pit, carved in fives, open on the four ways in. */
    static void rings(ServerLevel l,BlockPos b){
        var fives=mat("fives");
        for(int ring:new int[]{24,31})for(int u=-ring;u<=ring;u++)for(int v=-ring;v<=ring;v++){
            if(Math.max(Math.abs(u),Math.abs(v))!=ring||Math.abs(u)<=2||Math.abs(v)<=2)continue;
            int x=PIT_X+u,z=PIT_Z+v;if(z>DESCENT_N-1||z<BASIN_N||Math.abs(x)>BASIN_HALF||inTower(x,z))continue;
            set(l,b,x,0,z,fives);
        }
    }
    static void pit(ServerLevel l,BlockPos b){
        var basalt=mat("basalt");var wall=Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
        int bottom=-38;
        // The rim wall, two thick, down to the shaft's floor; the floor and the black water over it.
        for(int x=PIT_X-RIM-1;x<=PIT_X+RIM+1;x++)for(int z=PIT_Z-RIM-1;z<=PIT_Z+RIM+1;z++){
            int d=cheb(x,z);
            if(d>=RIM)box(l,b,x,bottom,z,x,-1,z,basalt);
            else{set(l,b,x,bottom,z,basalt);box(l,b,x,bottom+1,z,x,bottom+3,z,WATER);}
        }
        // A parapet round the rim, open where the ramp begins.
        for(int x=PIT_X-RIM-1;x<=PIT_X+RIM+1;x++)for(int z=PIT_Z-RIM-1;z<=PIT_Z+RIM+1;z++){
            if(cheb(x,z)!=RIM+1)continue;
            if(z-PIT_Z>=RIM&&x-PIT_X>=10&&x-PIT_X<=RIM)continue;
            set(l,b,x,0,z,wall);
        }
        // The ramp: half a block lower every few steps, ledged out from the wall, a curb and a low wall along its inner edge.
        for(var f:FLOORS){
            int top=(int)Math.ceil(f.stand),cell=top-1;boolean half=f.stand!=top;int d=cheb(f.x,f.z);
            box(l,b,f.x,cell-2,f.z,f.x,cell-1,f.z,basalt);
            if(d==BAND_IN){set(l,b,f.x,cell,f.z,basalt);set(l,b,f.x,cell+1,f.z,wall);continue;}
            set(l,b,f.x,cell,f.z,half?Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM):basalt);
        }
        // A way out of the water for anyone who goes over the wall: a ladder up the inner face of the landing's curb, and a gap in the
        // wall above it.
        int lx=SHAFT_LADDER.getX(),lz=SHAFT_LADDER.getZ();
        box(l,b,lx,bottom,lz-1,lx,FLOOR-1,lz-1,basalt);set(l,b,lx,FLOOR,lz-1,AIR);
        for(int y=bottom+1;y<FLOOR;y++)set(l,b,lx,y,lz,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH).setValue(LadderBlock.WATERLOGGED,y<=bottom+3));
    }
    static void tunnel(ServerLevel l,BlockPos b){
        var basalt=mat("basalt");var rnd=new Random(94096);
        // The tunnel north from the landing, through the rim wall.
        box(l,b,-3,FLOOR-1,CAVE_S+1,3,FLOOR+5,PIT_Z-RIM,basalt);box(l,b,-2,FLOOR,CAVE_S+1,2,FLOOR+4,PIT_Z-RIM,AIR);
        // The cavern: the shore, then black water to the far wall, under a low roof.
        box(l,b,-15,FLOOR-4,CAVE_N-1,15,FLOOR+10,CAVE_S+1,basalt);
        box(l,b,-14,FLOOR,CAVE_N,14,FLOOR+9,CAVE_S,AIR);
        box(l,b,-14,FLOOR-3,CAVE_N,14,FLOOR-1,SHORE_N-1,WATER);
        box(l,b,-2,FLOOR,CAVE_S+1,2,FLOOR+4,CAVE_S+1,AIR);
        for(int i=0;i<14;i++){int x=rnd.nextInt(27)-13,z=CAVE_N+rnd.nextInt(CAVE_S-CAVE_N),len=1+rnd.nextInt(3);box(l,b,x,FLOOR+10-len,z,x,FLOOR+9,z,basalt);}
        // Prints in the grit, both ways along the tunnel and over the shore.
        for(int i=0;i<16;i++){int x=rnd.nextInt(5)-2,z=CAVE_S+1+rnd.nextInt(PIT_Z-RIM-CAVE_S-1);set(l,b,x,FLOOR,z,fit(CityBlock.Kind.PRINTS,rnd.nextBoolean()?Direction.NORTH:Direction.SOUTH));}
        for(int i=0;i<10;i++){int x=rnd.nextInt(25)-12,z=SHORE_N+rnd.nextInt(CAVE_S-SHORE_N+1);set(l,b,x,FLOOR,z,fit(CityBlock.Kind.PRINTS,Direction.from2DDataValue(rnd.nextInt(4))));}
        for(var at:LAMPS)if(at.getY()==FLOOR+1)set(l,b,at.below(),Blocks.SPRUCE_FENCE.defaultBlockState());
    }
    static void murals(ServerLevel l,BlockPos b){for(var m:MURALS)for(int i=0;i<m.cells.size();i++)set(l,b,m.cells.get(i),CityMural.of(m.index,m.parts.get(i),m.facing));}
    /** Snow blown into the corners of the plaza and the camp, from a fixed seed. */
    static void drifts(ServerLevel l,BlockPos b){
        var rnd=new Random(94097);
        for(int i=0;i<700;i++){
            int x=rnd.nextInt(2*BASIN_HALF-3)-BASIN_HALF+2,z=BASIN_N+1+rnd.nextInt(DESCENT_N-BASIN_N-2);
            if(cheb(x,z)<=RIM+2||inTower(x,z)||Math.abs(x)<=2)continue;
            int ring=cheb(x,z);if(ring==24||ring==31)continue;
            set(l,b,x,0,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1+rnd.nextInt(2)));
        }
    }

    // ------------------------------------------------------------------------------------------------ what changes
    /** The tunnel's and the shore's lamps, lit or out. */
    public static void lamps(ServerLevel l,BlockPos b,boolean lit){for(var at:LAMPS)lamp(l,b,at,lit);}
    public static void lamp(ServerLevel l,BlockPos b,BlockPos at,boolean lit){var s=l.getBlockState(b.offset(at));if(CityBlock.is(s,CityBlock.Kind.LAMP)||s.isAir())l.setBlock(b.offset(at),fit(CityBlock.Kind.LAMP,Direction.NORTH,lit?1:0),F);}
    /** What fills the tunnel comes into one slice, wherever it is empty. */
    public static void fill(ServerLevel l,BlockPos b,int slice){if(slice<0||slice>=CHASE.size())return;var mass=CityRegistry.MASS.get().defaultBlockState();for(var at:CHASE.get(slice)){var p=b.offset(at);if(l.getBlockState(p).isAir())l.setBlock(p,mass,F);}}
    /** And goes out of one again. */
    public static void clear(ServerLevel l,BlockPos b,int slice){if(slice<0||slice>=CHASE.size())return;for(var at:CHASE.get(slice)){var p=b.offset(at);if(l.getBlockState(p).is(CityRegistry.MASS.get()))l.setBlock(p,AIR,F);}}
    public static void clearAll(ServerLevel l,BlockPos b){for(int i=0;i<CHASE.size();i++)clear(l,b,i);}
}
