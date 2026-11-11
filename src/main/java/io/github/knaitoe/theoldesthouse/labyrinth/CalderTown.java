package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.481, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act two: the town"): Calder Falls north of the park, built whole. Every position
 * is relative to the park's base (BikeNightSite); north is -z; people stand at y 0 on ground at y -1.
 *
 * <p>The owner, on the town as first built, a grid of even streets: "the neighborhood looks good but would look better with less
 * even streets, some suburb shortcuts, etc", and "clutter and decor in alleys would benefit it too. space between buildings,
 * dumpsters, trash". So the streets are not a grid. Each is a line through its own corners with its own width ({@link #STREETS}),
 * and what a place is (road, kerb, pavement) is how far it is from the nearest of them ({@link #surface}): corners come out round,
 * and a street can bend, slant, or end in a circle.
 * <pre>
 *                  the cemetery's railings, its gate where the lane comes nearest
 *        .--------- Church Lane, round the foot of the hill ----------.
 *        |  the school, its lawn,     | the |   Elm Court: six houses   |
 *   West |  its field behind fence    | cut |   round a turning circle  | East
 *   Ave, |-- Elm Street, which slants across Mill's end ---------------| Ave,
 *   which|  the Food Fair, its yard,  |  M  | two shops' alley, the dock |which
 *   bends|  its lot                   |  i  | Wash-O-Mat, Video, barber  |bends
 *        |======= Main Street, the wide one ============================|
 *        |  hardware, bank, theater;  |  l  |  Big Top Burgers in its    |
 *        |  their alley; a house, and |  l  |  lot, the drive-thru lane  |
 *        |  the lot nobody has built  |     |  through it                |
 *        '--------- Park Street, past the park's gate -----------------'
 * </pre>
 * The ways through that are not streets: the gaps between the shops into their alleys; the dock; the drive-thru lane; the trail
 * across the empty lot, over its ramp and through the fence; the two holes in the school's fence, under the bleachers between
 * them; the cut, a footpath between the school and the houses; the path out of the head of Elm Court; and the boards gone from
 * two back fences. Main Street and the corners are lit. The alleys, the cut and the field are not.
 */
public final class CalderTown {
    private CalderTown(){}
    /** The town's ground, and how high over it is cleared. Its south edge is the pavement along the park's railings: Park Street is the town's to lay. */
    public static final int W=-62,E=62,N=-182,S=BikeNightSite.WALK_S,TOP=12;
    /** What a place can be, by the streets: nothing of theirs, or road, kerb or pavement. */
    public static final int NONE=-1,ROAD=0,KERB=1,PAVEMENT=2;
    /** A street: the road's half-width, whether a line is painted down its middle, and the corners of its centre line (x, z, x, z...). */
    public record Street(String name,int half,boolean lined,int... at){
        /** How far a place is from its centre line. */
        public double from(double x,double z){
            double best=Double.MAX_VALUE;
            for(int i=0;i+3<at.length;i+=2){
                double ax=at[i],az=at[i+1],dx=at[i+2]-ax,dz=at[i+3]-az,len=dx*dx+dz*dz;
                double t=len==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(z-az)*dz)/len));
                best=Math.min(best,Math.hypot(x-(ax+t*dx),z-(az+t*dz)));
            }
            return best;
        }
    }
    public static final Street PARK_STREET=new Street("park",3,true,-50,-52,50,-52),
        WEST_AVENUE=new Street("west",3,true,-50,-52,-50,-104,-46,-116,-46,-165),
        CHURCH_LANE=new Street("church",2,true,-46,-165,-32,-165,-22,-171,14,-171,26,-165,46,-165),
        EAST_AVENUE=new Street("east",2,true,50,-52,50,-90,46,-100,46,-165),
        MAIN_STREET=new Street("main",4,true,-50,-92,50,-92),
        MILL_STREET=new Street("mill",3,true,0,-52,0,-92),
        MILL_NORTH=new Street("mill_north",2,true,2,-92,2,-123),
        ELM_STREET=new Street("elm",2,true,-46,-129,-12,-129,10,-121,46,-121),
        ELM_COURT=new Street("court",2,false,22,-121,22,-137),
        COURT_CIRCLE=new Street("circle",4,false,22,-141,22,-141);
    public static final Street[] STREETS={PARK_STREET,WEST_AVENUE,CHURCH_LANE,EAST_AVENUE,MAIN_STREET,MILL_STREET,MILL_NORTH,ELM_STREET,ELM_COURT,COURT_CIRCLE};
    /** The two footpaths: the cut, between the school's fence and the houses' from Elm Street to Church Lane; and the path out of the head of Elm Court. Each is two flags wide. */
    public static final int CUT_W=-6,CUT_E=-5,CUT_S=-133,CUT_N=-165,PATH_W=21,PATH_E=22,PATH_S=-149,PATH_N=-161;
    /** The cemetery's railings, north of Church Lane, and its gateway between its piers. */
    public static final int RAILS=-177,GATE_W=-2,GATE_E=1;
    /** How high a house's walls are. */
    private static final int WALL=4;
    /** The bank's clock: its first place (the rest follow to the west), and the minute of the day a night starts at. */
    public static final BlockPos CLOCK=new BlockPos(-26,3,-85);
    public static final int CLOCK_PLACES=5,CLOCK_FROM=23*60+41;
    /** The theater's marquee: the west end of its lower row, on the front of the canopy. */
    public static final BlockPos MARQUEE=new BlockPos(-17,2,-87);
    private static final CalderSigns.Sign[] FEATURES={CalderSigns.Sign.MARQUEE_A,CalderSigns.Sign.MARQUEE_B,CalderSigns.Sign.MARQUEE_C};
    /** The Food Fair's three windows, the west end of each, on the row in front of its wall. */
    public static final BlockPos[] FOOD={new BlockPos(-37,0,-112),new BlockPos(-32,0,-112),new BlockPos(-22,0,-112)};
    /** The burger place's name: over its front, and on its back wall facing Main Street. The west end of each one's lower row. */
    public static final BlockPos NEON_SOUTH=new BlockPos(22,2,-66),NEON_NORTH=new BlockPos(22,2,-77);
    /** The drive-thru lane, along the east side of the burger place: its three rows across, and its two ends. */
    public static final int LANE_W=33,LANE_E=35,LANE_S=-66,LANE_N=-77;
    /** The laundromat's dock, in the alley behind it: its first and last blocks, its rows, and the row a bike comes up onto it from. */
    public static final int DOCK_W=11,DOCK_E=18,DOCK_S=-110,DOCK_N=-112,DOCK_RAMP=10;
    /** The holes in the school's fence: on Elm Street, and behind the bleachers on Church Lane's side. One is due north of the other. */
    public static final BlockPos HOLE_SOUTH=new BlockPos(-18,0,-135),HOLE_NORTH=new BlockPos(-18,0,-159);
    /** The school's field, and its bleachers: four rows along its north side, each a block higher and further back, the first a block up. */
    public static final int FIELD_W=-27,FIELD_E=-8,FIELD_N=-158,FIELD_S=-136,STAND_W=-25,STAND_E=-11,STAND_FRONT=-155,STAND_ROWS=4;
    /** The field's yard lines: every fourth row across it, from one to the other; and where the scoreboard hangs on the school's wall. */
    public static final int YARD_W=-26,YARD_E=-10,YARD_STEP=4,YARD_N=-153,YARD_S=-138,SCORE_LO=-150;
    /** Where the three one-sheets hang along the theater's side, each from its lower row. */
    public static final int[] POSTERS={-74,-78,-82};
    /** The empty lot on Park Street: where its trail's ramp starts (it goes up to the west, two blocks long and two wide), and the gap in the fence at the trail's end. */
    public static final BlockPos LOT_RAMP=new BlockPos(-15,0,-64),LOT_GAP=new BlockPos(-23,0,-69);
    /** Boards gone from two back fences: from the cut into a yard on Elm Court, and from a yard there onto East Avenue. */
    public static final BlockPos YARD_GAP_WEST=new BlockPos(-4,0,-145),YARD_GAP_EAST=new BlockPos(40,0,-145);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    /** What the streets make of a place: road where any of them is that near, else kerb, else pavement, else nothing. */
    public static int surface(int x,int z){
        int best=NONE;
        for(var s:STREETS){
            double d=s.from(x,z);int k=d<=s.half+.5?ROAD:d<=s.half+1.5?KERB:d<=s.half+3.5?PAVEMENT:NONE;
            if(k!=NONE&&(best==NONE||k<best))best=k;
        }
        return best;
    }
    /** Whether a place is on one of the two footpaths. */
    public static boolean footpath(int x,int z){return x>=CUT_W&&x<=CUT_E&&z>=CUT_N&&z<=CUT_S||x>=PATH_W&&x<=PATH_E&&z>=PATH_N&&z<=PATH_S;}
    /** Whether a place is in the town: on its ground, north of the park's railings. */
    public static boolean inTown(BlockPos rel){return rel.getX()>=W&&rel.getX()<=E&&rel.getZ()>=N&&rel.getZ()<=S;}

    // ------------------------------------------------------------------------------------------------ small things
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        NovelRooms.box(l,b,Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1),Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1),s);
    }
    private static boolean free(ServerLevel l,BlockPos b,int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z)).isAir();}
    private static BlockState mat(String name){return BikeNightSite.mat(name);}
    private static BlockState town(CalderTownBlock.Kind kind,Direction facing,int stage){return CalderTownBlock.of(kind,facing,stage);}
    private static BlockState fit(CalderBlock.Kind kind,Direction facing,int stage){return BikeNightSite.fit(kind,facing,stage);}
    /** Something of the town's, where there is nothing: a thing that would take the place of another is a mistake in the builder, and says so. */
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,CalderTownBlock.Kind kind,Direction facing,int stage){
        if(!free(l,b,x,y,z))throw new IllegalStateException("the "+kind+" at "+x+","+y+","+z+" would stand where "+BuildBlocks.state(l,b.offset(x,y,z))+" is");
        set(l,b,x,y,z,town(kind,facing,stage));
    }
    /** The cell {@code out} blocks in front of a wall that looks {@code facing}: {@code plane} is the wall's own row, {@code w} how far along it. */
    private static BlockPos before(Direction facing,int plane,int w,int y,int out){
        return switch(facing){
            case SOUTH->new BlockPos(w,y,plane+out);
            case NORTH->new BlockPos(w,y,plane-out);
            case EAST->new BlockPos(plane+out,y,w);
            default->new BlockPos(plane-out,y,w);
        };
    }
    /** Hangs a sign on a wall that looks {@code facing}: its blocks from {@code lo} along the wall, its bottom row at {@code y}. */
    private static void on(ServerLevel l,BlockPos b,CalderSigns.Sign s,Direction facing,int plane,int lo,int y){
        int left=facing==Direction.SOUTH||facing==Direction.WEST?lo:lo+s.cols-1;
        CalderSigns.hang(l,b.offset(before(facing,plane,left,y,1)),s,facing);
    }
    /** Something fixed to a wall that looks {@code facing}, in the cell in front of it. */
    private static void fixed(ServerLevel l,BlockPos b,CalderTownBlock.Kind kind,int stage,Direction facing,int plane,int w,int y){
        var at=before(facing,plane,w,y,1);put(l,b,at.getX(),at.getY(),at.getZ(),kind,facing,stage);
    }
    /** An awning over a front, from {@code lo} to {@code hi} along the wall. */
    private static void awning(ServerLevel l,BlockPos b,Direction facing,int plane,int lo,int hi,int y,int stripe){
        for(int w=lo;w<=hi;w++)fixed(l,b,CalderTownBlock.Kind.AWNING,stripe,facing,plane,w,y);
    }
    /** A drainpipe up a wall, its foot on the ground and its head under the coping. */
    private static void pipe(ServerLevel l,BlockPos b,Direction facing,int plane,int w,int high){
        for(int y=0;y<high;y++)fixed(l,b,CalderTownBlock.Kind.PIPE,y==0?1:y==high-1?2:0,facing,plane,w,y);
    }
    /** A dumpster against a wall that looks {@code facing}: two blocks along it from {@code lo}, its lid shut or thrown back. */
    private static void dumpster(ServerLevel l,BlockPos b,Direction facing,int plane,int lo,boolean open){
        // Its first half is at the left hand of whoever stands with their back to the wall.
        boolean rising=facing==Direction.NORTH||facing==Direction.EAST;
        fixed(l,b,CalderTownBlock.Kind.DUMPSTER,open?CalderTownBlock.OPEN:0,facing,plane,rising?lo:lo+1,0);
        fixed(l,b,CalderTownBlock.Kind.DUMPSTER,(open?CalderTownBlock.OPEN:0)+1,facing,plane,rising?lo+1:lo,0);
    }
    /** The pavement nearest a place that has nothing standing on it, {@code high} blocks up. */
    private static BlockPos pavement(ServerLevel l,BlockPos b,int x,int z,int high){
        for(int r=0;r<=4;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r||surface(x+dx,z+dz)!=PAVEMENT)continue;
            boolean clear=true;for(int y=0;y<high&&clear;y++)clear=free(l,b,x+dx,y,z+dz);
            if(clear)return new BlockPos(x+dx,0,z+dz);
        }
        throw new IllegalStateException("no pavement to stand a thing on near "+x+","+z);
    }
    private static void lamp(ServerLevel l,BlockPos b,int x,int z){BikeNightSite.lamp(l,b,pavement(l,b,x,z,4),true);}
    /** A post: its foot and the rest of it, {@code high} blocks in all. */
    private static void pole(ServerLevel l,BlockPos b,int x,int z,int high){
        set(l,b,x,0,z,fit(CalderBlock.Kind.LAMP,Direction.NORTH,CalderBlock.FOOT));
        for(int y=1;y<high;y++)set(l,b,x,y,z,fit(CalderBlock.Kind.LAMP,Direction.NORTH,CalderBlock.POST));
    }
    /** A corner's two street names on one post: the name of the street that runs east and west, and over it the other's, across it. */
    private static void names(ServerLevel l,BlockPos b,int x,int z,CalderSigns.Sign across,CalderSigns.Sign along){
        for(int r=0;r<=4;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
            int px=x+dx,pz=z+dz;if(Math.max(Math.abs(dx),Math.abs(dz))!=r||surface(px,pz)!=PAVEMENT)continue;
            boolean clear=true;
            for(int y=0;y<3&&clear;y++)clear=free(l,b,px,y,pz);
            for(int k=-1;k<=1&&clear;k++)clear=free(l,b,px+k,3,pz)&&free(l,b,px,4,pz+k);
            if(!clear)continue;
            pole(l,b,px,pz,3);
            CalderSigns.hang(l,b.offset(px-1,3,pz),across,Direction.SOUTH);
            CalderSigns.hang(l,b.offset(px,4,pz-1),along,Direction.WEST);
            return;
        }
        throw new IllegalStateException("nowhere for the street names near "+x+","+z);
    }
    /** One name on a post, lying north and south: a footpath's, at its mouth. The post never stands in the path. */
    private static void blade(ServerLevel l,BlockPos b,int x,int z,CalderSigns.Sign sign){
        for(int r=0;r<=4;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
            int px=x+dx,pz=z+dz;if(Math.max(Math.abs(dx),Math.abs(dz))!=r||surface(px,pz)!=PAVEMENT||px>=CUT_W-1&&px<=CUT_E+1)continue;
            boolean clear=true;
            for(int y=0;y<3&&clear;y++)clear=free(l,b,px,y,pz);
            for(int k=-1;k<=1&&clear;k++)clear=free(l,b,px,3,pz+k);
            if(!clear)continue;
            pole(l,b,px,pz,3);CalderSigns.hang(l,b.offset(px,3,pz-1),sign,Direction.WEST);
            return;
        }
        throw new IllegalStateException("nowhere for the blade near "+x+","+z);
    }
    /** A sign on a post on the pavement, read by whoever comes at it from {@code facing}: a stop sign, or one two blocks wide. */
    private static void posted(ServerLevel l,BlockPos b,int x,int z,CalderSigns.Sign sign,Direction facing){
        var at=pavement(l,b,x,z,3);pole(l,b,at.getX(),at.getZ(),2);
        CalderSigns.hang(l,b.offset(at).above(2),sign,facing);
    }
    /** A run of fence two blocks high along one row from end to end, chain-link or boards. What is already standing in its way stays. */
    private static void fence(ServerLevel l,BlockPos b,CalderTownBlock.Kind kind,int x0,int z0,int x1,int z1){
        var facing=z0==z1?Direction.NORTH:Direction.EAST;
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++){
            if(surface(x,z)!=NONE||!free(l,b,x,0,z)||!free(l,b,x,1,z))continue;
            set(l,b,x,0,z,town(kind,facing,CalderTownBlock.LOWER));set(l,b,x,1,z,town(kind,facing,CalderTownBlock.UPPER));
        }
    }
    /**
     * A fence's post, where one run meets another: a whole block, two high, boards' of timber and chain-link's of concrete. A
     * fence is on the middle line of its blocks, so two runs that meet at a corner leave a gap between them a child gets through;
     * a post closes it.
     */
    private static void post(ServerLevel l,BlockPos b,int x,int z,boolean timber){box(l,b,x,0,z,x,1,z,mat(timber?"plank":"concrete"));}
    private static void link(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){fence(l,b,CalderTownBlock.Kind.CHAINLINK,x0,z0,x1,z1);}
    private static void boards(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){fence(l,b,CalderTownBlock.Kind.BOARDS,x0,z0,x1,z1);}
    /** A way through a fence: the wire cut and pulled back, or the boards gone. */
    private static void hole(ServerLevel l,BlockPos b,BlockPos at){
        var s=BuildBlocks.state(l,b.offset(at));
        if(!s.is(BikeNightRegistry.TOWN.get()))throw new IllegalStateException("no fence to make a hole in at "+at+": "+s);
        var kind=s.getValue(CalderTownBlock.KIND);var facing=s.getValue(CalderTownBlock.FACING);
        set(l,b,at,town(kind,facing,CalderTownBlock.TORN));set(l,b,at.above(),town(kind,facing,CalderTownBlock.TORN_UPPER));
    }
    /** A tree, where the streets have left the ground alone and nothing stands. */
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int high,Random rnd){
        if(surface(x,z)!=NONE||footpath(x,z)||!free(l,b,x,0,z)||!BuildBlocks.state(l,b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK))
            throw new IllegalStateException("a tree at "+x+","+z+" would not stand on grass");
        BikeNightSite.tree(l,b,x,z,high,rnd);
    }
    /** Something left lying about, a list of it: each is x, z, the way it looks (0 north, 1 east, 2 south, 3 west) and its stage. */
    private static void strew(ServerLevel l,BlockPos b,CalderTownBlock.Kind kind,int... at){
        var facings=new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
        for(int i=0;i+3<at.length;i+=4)put(l,b,at[i],0,at[i+1],kind,facings[at[i+2]],at[i+3]);
    }

    // ------------------------------------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(48100);
        ground(l,b);
        grounds(l,b);
        streets(l,b);
        mainSouth(l,b,rnd);
        emptyLot(l,b,rnd);
        bigTop(l,b);
        foodFair(l,b);
        mainNorth(l,b);
        school(l,b,rnd);
        court(l,b,rnd);
        avenues(l,b,rnd);
        cemetery(l,b,rnd);
        corners(l,b,rnd);
        fixtures(l,b);
        int[] first=shown(0);
        for(int i=0;i<CLOCK_PLACES;i++)if(first[i]>=0)set(l,b,CLOCK.west(i),CalderDigitBlock.of(first[i],Direction.NORTH));
        litter(l,b,rnd);
    }
    /** The ground the town stands on, and the two strips past Park Street's ends. */
    static void ground(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var dirt=Blocks.DIRT.defaultBlockState();var grass=Blocks.GRASS_BLOCK.defaultBlockState();
        int south=BikeNightSite.WALK_N-1;
        box(l,b,W,-4,N,E,TOP,south,air);box(l,b,W,-4,N,E,-2,south,dirt);box(l,b,W,-1,N,E,-1,south,grass);
        for(int side=-1;side<=1;side+=2){
            int x0=side*(BikeNightSite.FAR_EAST+1),x1=side*E,z0=south+1,z1=BikeNightSite.NORTH;
            box(l,b,x0,-4,z0,x1,TOP,z1,air);box(l,b,x0,-4,z0,x1,-2,z1,dirt);box(l,b,x0,-1,z0,x1,-1,z1,grass);
        }
    }
    /** What the blocks between the streets are floored with, before the streets are laid over their edges: lots, alleys, the field, the footpaths. */
    static void grounds(ServerLevel l,BlockPos b){
        var gravel=mat("gravel");var lot=mat("lot");var paving=mat("paving");
        // Behind and between the three shops south of Main Street.
        box(l,b,-43,-1,-84,-7,-1,-70,gravel);
        // Big Top's lot.
        box(l,b,7,-1,-84,44,-1,-59,lot);
        // The Food Fair's lot, and the yard at the east end of the building.
        box(l,b,-43,-1,-112,-4,-1,-100,lot);box(l,b,-11,-1,-123,-4,-1,-113,lot);
        // Behind and between the three shops north of Main Street; flags at the end of the row.
        box(l,b,8,-1,-114,40,-1,-100,gravel);box(l,b,39,-1,-109,40,-1,-100,paving);
        // The school's field.
        box(l,b,FIELD_W,-1,FIELD_N,FIELD_E,-1,FIELD_S,mat("turf"));
        // Its yard lines, chalked across it. The middle one is the line from one hole in the fence to the other.
        for(int x=YARD_W;x<=YARD_E;x+=YARD_STEP)box(l,b,x,-1,YARD_N,x,-1,YARD_S,mat("turf_line"));
        // The two footpaths.
        box(l,b,CUT_W,-1,CUT_N,CUT_E,-1,CUT_S,paving);box(l,b,PATH_W,-1,PATH_N,PATH_E,-1,PATH_S,paving);
    }
    /**
     * The streets, laid over everything else on the ground: road, kerb and pavement by how near a place is to the nearest street,
     * and a line down the middle of a straight run where no other street comes near. Park Street is laid again here, so that its
     * ends turn into the avenues.
     */
    static void streets(ServerLevel l,BlockPos b){
        var road=mat("asphalt");var kerb=mat("kerb");var walk=mat("paving");var grass=Blocks.GRASS_BLOCK.defaultBlockState();
        for(int x=W;x<=E;x++)for(int z=N;z<=BikeNightSite.WALK_S;z++){
            int k=surface(x,z);
            if(k==NONE){if(z>=BikeNightSite.WALK_N&&Math.abs(x)<=BikeNightSite.FAR_EAST)set(l,b,x,-1,z,grass);continue;}
            set(l,b,x,-1,z,k==ROAD?road:k==KERB?kerb:walk);
        }
        for(var s:STREETS){
            if(!s.lined)continue;
            for(int i=0;i+3<s.at.length;i+=2){
                int ax=s.at[i],az=s.at[i+1],bx=s.at[i+2],bz=s.at[i+3];if(ax!=bx&&az!=bz)continue;
                var line=BikeNightRegistry.ROAD_LINE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,az==bz?Direction.Axis.X:Direction.Axis.Z);
                for(int x=Math.min(ax,bx);x<=Math.max(ax,bx);x++)for(int z=Math.min(az,bz);z<=Math.max(az,bz);z++){
                    boolean crossed=false;for(var other:STREETS)if(other!=s&&other.from(x,z)<=other.half+1.5){crossed=true;break;}
                    // A line stops short of where its own street turns, too.
                    boolean turning=(i>0&&Math.hypot(x-ax,z-az)<s.half+1.5)||(i+4<s.at.length&&Math.hypot(x-bx,z-bz)<s.half+1.5);
                    if(!crossed&&!turning)set(l,b,x,-1,z,line);
                }
            }
        }
    }

    /** A building with a flat roof: solid, of one material from its slab to its top, tarred inside a concrete coping. */
    private static void shell(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int high,BlockState wall){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)
            if(surface(x,z)!=NONE)throw new IllegalStateException("a building at "+x+","+z+" would stand in the street");
        box(l,b,x0,-1,z0,x1,-1,z1,mat("concrete"));
        box(l,b,x0,0,z0,x1,high-1,z1,wall);
        box(l,b,x0+1,high-1,z0+1,x1-1,high-1,z1-1,mat("roofing"));
        box(l,b,x0,high,z0,x1,high,z0,mat("concrete"));box(l,b,x0,high,z1,x1,high,z1,mat("concrete"));
        box(l,b,x0,high,z0,x0,high,z1,mat("concrete"));box(l,b,x1,high,z0,x1,high,z1,mat("concrete"));
    }
    /**
     * A house: clapboard four high on a slab, under shingles whose ridge runs along its front. A door in the middle of the front
     * with a window each side of it, boards before the door, and whatever it has put out for the night.
     */
    private static void house(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,Direction front,Random rnd){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)if(surface(x,z)!=NONE)throw new IllegalStateException("a house at "+x+","+z+" would stand in the street");
        boolean wide=front.getAxis()==Direction.Axis.Z;
        int w0=wide?x0:z0,w1=wide?x1:z1,d0=wide?z0:x0,d1=wide?z1:x1;
        box(l,b,x0,-1,z0,x1,-1,z1,mat("concrete"));box(l,b,x0,0,z0,x1,WALL-1,z1,mat("siding"));
        int ridge=WALL;
        // The roof: each course two rows in from the one under it. On the two ends, what a course does not cover is wall.
        for(int k=0;d0-1+2*k<=d1+1-2*k;k++){
            int lo=d0-1+2*k,hi=d1+1-2*k;ridge=WALL+k;
            for(int w=w0;w<=w1;w++)for(int d=lo;d<=hi;d++){
                int x=wide?w:d,z=wide?d:w;if(x<W||x>E)continue;
                boolean end=(w==w0||w==w1)&&d>=lo+2&&d<=hi-2;
                set(l,b,x,WALL+k,z,mat(end?"siding":"shingle"));
            }
        }
        int cw=w0+2,cd=(d0+d1)/2;
        for(int y=ridge;y<=ridge+1;y++)set(l,b,wide?cw:cd,y,wide?cd:cw,mat("brick"));
        int plane=switch(front){case SOUTH->z1;case NORTH->z0;case EAST->x1;default->x0;},mid=(w0+w1)/2;
        var windows=new CalderSigns.Sign[]{CalderSigns.Sign.HOUSE_A,CalderSigns.Sign.HOUSE_B,CalderSigns.Sign.HOUSE_C};
        on(l,b,CalderSigns.Sign.HOUSE_DOOR,front,plane,mid,0);
        on(l,b,windows[rnd.nextInt(3)],front,plane,w0+1,1);on(l,b,windows[rnd.nextInt(3)],front,plane,w1-2,1);
        for(int w=mid-1;w<=mid+1;w++)for(int out=1;out<=2;out++){var at=before(front,plane,w,-1,out);if(surface(at.getX(),at.getZ())==NONE)set(l,b,at,mat("plank"));}
        for(int side=-1;side<=1;side+=2)if(rnd.nextInt(3)!=0)fixed(l,b,CalderTownBlock.Kind.PUMPKIN,rnd.nextInt(4),front,plane,mid+side,0);
    }
    /** A path of flags, where the streets have not already paved. */
    private static void path(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)if(surface(x,z)==NONE)set(l,b,x,-1,z,mat("paving"));
    }

    /**
     * South of Main Street, west of Mill: the hardware store, the bank and the theater on Main, a gap between each and the next;
     * their alley behind, which is as wide as each is deep; and a house on Park Street. What is in the alley is listed here.
     */
    static void mainSouth(ServerLevel l,BlockPos b,Random rnd){
        var n=Direction.NORTH;var s=Direction.SOUTH;int wall=-84;
        // The theater, and the canopy its marquee is on, out over the pavement.
        shell(l,b,-21,wall,-7,-72,7,mat("tanbrick"));
        box(l,b,-18,2,wall-2,-10,3,wall-1,mat("concrete"));
        on(l,b,CalderSigns.Sign.THEATER_FRONT,n,wall,-17,0);
        on(l,b,FEATURES[0],n,wall-2,MARQUEE.getX(),MARQUEE.getY());
        // Three one-sheets in lit cases down its side, on Mill Street.
        on(l,b,CalderSigns.Sign.POSTER_A,Direction.EAST,-7,POSTERS[0],1);on(l,b,CalderSigns.Sign.POSTER_B,Direction.EAST,-7,POSTERS[1],1);
        on(l,b,CalderSigns.Sign.POSTER_C,Direction.EAST,-7,POSTERS[2],1);
        // The bank. Its clock is over its name.
        shell(l,b,-32,wall,-24,-74,5,mat("stucco"));
        on(l,b,CalderSigns.Sign.BANK_FRONT,n,wall,-30,0);on(l,b,CalderSigns.Sign.BANK_NAME,n,wall,-31,2);
        // The hardware store.
        shell(l,b,-43,wall,-35,-77,4,mat("siding"));
        on(l,b,CalderSigns.Sign.HARDWARE_FRONT,n,wall,-42,0);awning(l,b,n,wall,-42,-37,2,1);on(l,b,CalderSigns.Sign.HARDWARE_NAME,n,wall,-42,3);
        // The yards' fence along the alley's far side; boards are gone from it where the empty lot's trail comes up to it.
        boards(l,b,-43,-69,-7,-69);
        hole(l,b,LOT_GAP);hole(l,b,LOT_GAP.east());
        // Behind the hardware store: its back door under a bulb, pallets, a crate, drums.
        on(l,b,CalderSigns.Sign.BACK_DOOR,s,-77,-39,0);fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,s,-77,-39,2);
        fixed(l,b,CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET_STOOD,s,-77,-42,0);fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.METER,s,-77,-36,1);
        strew(l,b,CalderTownBlock.Kind.CRATE,-41,-75,0,CalderTownBlock.PALLET, -37,-76,2,0, -42,-71,1,CalderTownBlock.MILK_CRATE);
        strew(l,b,CalderTownBlock.Kind.DRUM,-36,-74,0,2, -38,-70,0,1);
        strew(l,b,CalderTownBlock.Kind.TRASH,-40,-73,1,0, -37,-72,2,CalderTownBlock.PUDDLE);
        // Between the hardware store and the bank: a drainpipe, boxes, a skull somebody has sprayed.
        pipe(l,b,Direction.WEST,-32,-80,5);
        on(l,b,CalderSigns.Sign.TAG_SKULL,Direction.EAST,-35,-81,1);
        strew(l,b,CalderTownBlock.Kind.BOXES,-34,-79,1,1, -34,-76,0,CalderTownBlock.FLAT);
        strew(l,b,CalderTownBlock.Kind.TRASH,-33,-83,0,1, -34,-82,3,0);
        // Behind the bank: a dumpster, bags beside it, a cooler in the wall.
        dumpster(l,b,s,-74,-28,false);
        fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.COOLER,s,-74,-30,2);
        strew(l,b,CalderTownBlock.Kind.BAGS,-29,-73,2,2, -25,-73,2,0);
        strew(l,b,CalderTownBlock.Kind.TRASH,-27,-71,0,2, -31,-71,1,1);
        // Between the bank and the theater: a name on the theater's wall, bags, a drum.
        on(l,b,CalderSigns.Sign.TAG_HERE,Direction.WEST,-21,-81,1);
        strew(l,b,CalderTownBlock.Kind.BAGS,-23,-78,1,1);
        strew(l,b,CalderTownBlock.Kind.DRUM,-22,-83,0,1);
        strew(l,b,CalderTownBlock.Kind.TRASH,-22,-76,2,0, -23,-81,0,2);
        // Behind the theater, where the alley is at its narrowest: its stage door, and what Tully wrote.
        on(l,b,CalderSigns.Sign.BACK_DOOR,s,-72,-10,0);fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,s,-72,-10,2);
        on(l,b,CalderSigns.Sign.TAG_RULES,s,-72,-19,1);
        pipe(l,b,s,-72,-8,7);
        strew(l,b,CalderTownBlock.Kind.BAGS,-14,-71,2,0);
        strew(l,b,CalderTownBlock.Kind.TYRES,-12,-70,0,3);
        strew(l,b,CalderTownBlock.Kind.TRASH,-16,-70,3,1, -9,-70,0,CalderTownBlock.PUDDLE);
        // A house, looking over Park Street at the park, and the fence down the side of its yard.
        house(l,b,-41,-67,-31,-61,s,rnd);
        boards(l,b,-28,-68,-28,-59);post(l,b,-28,-68,true);
        on(l,b,CalderSigns.Sign.BEWARE,Direction.EAST,-31,-65,1);
        tree(l,b,-29,-62,5,rnd);
    }
    /**
     * The lot nobody has built on, between that house and Mill Street: grass, a trail worn across it from Park Street to the gap in
     * the alley's fence, a ramp somebody has made on the trail, and what gets left on empty ground.
     */
    static void emptyLot(ServerLevel l,BlockPos b,Random rnd){
        var worn=Blocks.COARSE_DIRT.defaultBlockState();
        box(l,b,-9,-1,-64,-8,-1,-59,worn);box(l,b,-23,-1,-64,-8,-1,-63,worn);box(l,b,-23,-1,-68,-22,-1,-63,worn);
        for(int z=LOT_RAMP.getZ();z<=LOT_RAMP.getZ()+1;z++){
            set(l,b,LOT_RAMP.getX(),0,z,BikeRampBlock.of(Direction.WEST,BikeRampBlock.Part.LOW));
            set(l,b,LOT_RAMP.getX()-1,0,z,BikeRampBlock.of(Direction.WEST,BikeRampBlock.Part.HIGH));
        }
        pole(l,b,-17,-60,2);CalderSigns.hang(l,b.offset(-18,2,-60),CalderSigns.Sign.FOR_SALE,Direction.SOUTH);
        strew(l,b,CalderTownBlock.Kind.TYRES,-12,-67,0,1, -20,-61,0,0);
        strew(l,b,CalderTownBlock.Kind.DRUM,-8,-67,0,1);
        strew(l,b,CalderTownBlock.Kind.CART,-25,-62,1,0);
        strew(l,b,CalderTownBlock.Kind.BOXES,-19,-66,0,CalderTownBlock.FLAT);
        strew(l,b,CalderTownBlock.Kind.TRASH,-13,-61,0,1, -11,-66,2,2);
        tree(l,b,-26,-66,6,rnd);
    }
    /** South of Main Street, east of Mill: Big Top Burgers in its lot. A fence divides the lot, and the drive-thru lane is the way through it. */
    static void bigTop(ServerLevel l,BlockPos b){
        var s=Direction.SOUTH;var n=Direction.NORTH;
        shell(l,b,19,-76,31,-67,4,mat("stucco"));
        on(l,b,CalderSigns.Sign.BIGTOP_FRONT,s,-67,22,0);
        on(l,b,CalderSigns.Sign.BIGTOP_NAME,s,-67,NEON_SOUTH.getX(),NEON_SOUTH.getY());
        on(l,b,CalderSigns.Sign.BIGTOP_NAME,n,-76,NEON_NORTH.getX(),NEON_NORTH.getY());
        for(int x=9;x<=18;x+=3)box(l,b,x,-1,-65,x,-1,-62,mat("lot_line"));
        for(int x=38;x<=41;x+=3)box(l,b,x,-1,-65,x,-1,-62,mat("lot_line"));
        // The fence across the lot, and along the far side of the lane.
        link(l,b,7,-71,18,-71);link(l,b,LANE_E+1,-76,LANE_E+1,-67);link(l,b,LANE_E+2,-71,44,-71);post(l,b,LANE_E+2,-71,false);
        // Over each end of the lane, its board between two posts.
        for(int z:new int[]{LANE_S,LANE_N}){pole(l,b,LANE_W-1,z,4);pole(l,b,LANE_E+1,z,4);}
        CalderSigns.hang(l,b.offset(LANE_W,3,LANE_S),CalderSigns.Sign.DRIVE_THRU,s);
        CalderSigns.hang(l,b.offset(LANE_E,3,LANE_N),CalderSigns.Sign.DRIVE_THRU,n);
        set(l,b,18,1,-70,fit(CalderBlock.Kind.PHONE,Direction.WEST,0));
        fixed(l,b,CalderTownBlock.Kind.PUMPKIN,0,s,-67,21,0);fixed(l,b,CalderTownBlock.Kind.PUMPKIN,2,s,-67,29,0);
        set(l,b,30,0,-66,fit(CalderBlock.Kind.BIN,n,0));
        // Round the back: the kitchen's door under a bulb, the dumpster with its lid up, crates, the grease drums.
        on(l,b,CalderSigns.Sign.BACK_DOOR,n,-76,20,0);fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,n,-76,20,2);
        dumpster(l,b,n,-76,29,true);
        fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.VENT,n,-76,21,2);
        strew(l,b,CalderTownBlock.Kind.CRATE,21,-77,0,CalderTownBlock.MILK_CRATE, 28,-79,0,CalderTownBlock.PALLET);
        strew(l,b,CalderTownBlock.Kind.DRUM,19,-78,0,2);
        strew(l,b,CalderTownBlock.Kind.BAGS,31,-78,0,1);
        strew(l,b,CalderTownBlock.Kind.TRASH,24,-79,0,2, 27,-81,1,1, 22,-82,0,CalderTownBlock.PUDDLE, 12,-63,0,2, 40,-64,3,1);
        BikeNightSite.lamp(l,b,new BlockPos(40,0,-61),true);
    }
    /** North of Main Street, west of Mill: the Calder Food Fair across the back of its lot, and the yard its deliveries come to. */
    static void foodFair(ServerLevel l,BlockPos b){
        var s=Direction.SOUTH;var e=Direction.EAST;int wall=-113,east=-12;
        shell(l,b,-39,-123,east,wall,5,mat("tanbrick"));
        on(l,b,CalderSigns.Sign.FOOD_A,s,wall,FOOD[0].getX(),0);on(l,b,CalderSigns.Sign.FOOD_B,s,wall,FOOD[1].getX(),0);on(l,b,CalderSigns.Sign.FOOD_C,s,wall,FOOD[2].getX(),0);
        on(l,b,CalderSigns.Sign.FOOD_DOOR,s,wall,-27,0);on(l,b,CalderSigns.Sign.FOODFAIR_NAME,s,wall,-29,3);
        for(int x=-38;x<=-8;x+=3){box(l,b,x,-1,-110,x,-1,-107,mat("lot_line"));box(l,b,x,-1,-104,x,-1,-101,mat("lot_line"));}
        // (0.4.488: the row of four that stood in the aisle is the stock boy's chain now, and moves: CalderCarts.)
        strew(l,b,CalderTownBlock.Kind.CART,-34,-103,1,0);
        fixed(l,b,CalderTownBlock.Kind.PUMPKIN,1,s,wall,-29,0);fixed(l,b,CalderTownBlock.Kind.PUMPKIN,3,s,wall,-24,0);
        BikeNightSite.lamp(l,b,new BlockPos(-24,0,-106),true);
        // The yard: a shutter and a door in the building's end, a dumpster by them and another out in the yard, pallets, what is thrown out.
        on(l,b,CalderSigns.Sign.SHUTTER,e,east,-121,0);on(l,b,CalderSigns.Sign.BACK_DOOR,e,east,-116,0);on(l,b,CalderSigns.Sign.NO_PARKING,e,east,-123,2);
        fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,e,east,-116,2);
        dumpster(l,b,e,east,-119,false);
        boards(l,b,-4,-123,-4,-115);
        dumpster(l,b,Direction.WEST,-4,-122,true);
        fixed(l,b,CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET_STOOD,Direction.WEST,-4,-118,0);
        strew(l,b,CalderTownBlock.Kind.CRATE,-8,-122,0,CalderTownBlock.PALLET, -9,-114,0,0, -8,-114,1,CalderTownBlock.MILK_CRATE);
        strew(l,b,CalderTownBlock.Kind.BOXES,-7,-119,0,1, -9,-121,1,3);
        strew(l,b,CalderTownBlock.Kind.BAGS,-10,-123,0,2);
        strew(l,b,CalderTownBlock.Kind.TRASH,-8,-117,0,CalderTownBlock.PUDDLE, -6,-115,1,0, -7,-121,2,2);
    }
    /**
     * North of Main Street, east of Mill: the Wash-O-Mat, the Video Hut and the barber on Main, a gap between each and the next;
     * their alley behind, the laundromat's dock in it, and a fence between the alley and Elm Street.
     */
    static void mainNorth(ServerLevel l,BlockPos b){
        var s=Direction.SOUTH;var n=Direction.NORTH;int wall=-100;
        shell(l,b,8,-109,18,wall,4,mat("stucco"));
        on(l,b,CalderSigns.Sign.WASH_FRONT,s,wall,10,0);awning(l,b,s,wall,10,16,2,2);on(l,b,CalderSigns.Sign.WASH_NAME,s,wall,10,3);
        set(l,b,18,1,wall+1,fit(CalderBlock.Kind.PHONE,s,0));
        shell(l,b,21,-108,29,wall,4,mat("tanbrick"));
        on(l,b,CalderSigns.Sign.VIDEO_FRONT,s,wall,23,0);on(l,b,CalderSigns.Sign.VIDEO_NAME,s,wall,22,2);
        shell(l,b,32,-107,38,wall,4,mat("brick"));
        on(l,b,CalderSigns.Sign.BARBER_FRONT,s,wall,34,0);awning(l,b,s,wall,34,37,2,0);on(l,b,CalderSigns.Sign.BARBER_NAME,s,wall,33,3);
        fixed(l,b,CalderTownBlock.Kind.POLE,0,s,wall,33,1);
        set(l,b,39,0,-102,fit(CalderBlock.Kind.BIN,n,0));
        // The dock, against the laundromat's back wall: up the boards at its west end, and off its east end.
        box(l,b,DOCK_W,0,DOCK_N,DOCK_E,0,DOCK_S,mat("concrete"));
        for(int z=DOCK_N;z<=DOCK_S;z++)set(l,b,DOCK_RAMP,0,z,BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.STEEP));
        on(l,b,CalderSigns.Sign.SHUTTER,n,-109,15,1);on(l,b,CalderSigns.Sign.DELIVERIES,n,-109,14,3);
        put(l,b,12,1,DOCK_S,CalderTownBlock.Kind.BOXES,n,1);put(l,b,18,1,DOCK_S,CalderTownBlock.Kind.CRATE,n,0);
        // The fence along Elm Street's side of the alley; boards are gone from it too.
        boards(l,b,8,-115,40,-115);
        hole(l,b,new BlockPos(28,0,-115));hole(l,b,new BlockPos(29,0,-115));
        // Between the laundromat and the Video Hut.
        pipe(l,b,Direction.WEST,21,-104,4);
        fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.METER,Direction.EAST,18,-103,1);
        strew(l,b,CalderTownBlock.Kind.BAGS,19,-107,1,0);
        strew(l,b,CalderTownBlock.Kind.TRASH,20,-102,0,1, 19,-105,2,0);
        // Behind the Video Hut: a dumpster, its door under a bulb.
        dumpster(l,b,n,-108,23,false);
        on(l,b,CalderSigns.Sign.BACK_DOOR,n,-108,27,0);fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,n,-108,27,2);
        // Between the Video Hut and the barber: a drum, a skull, and somebody's cat.
        on(l,b,CalderSigns.Sign.LOST_CAT,Direction.EAST,29,-102,1);on(l,b,CalderSigns.Sign.TAG_SKULL,Direction.WEST,32,-104,1);
        strew(l,b,CalderTownBlock.Kind.DRUM,31,-106,0,0);
        strew(l,b,CalderTownBlock.Kind.BOXES,30,-103,3,0);
        // Behind the barber: his door, a cooler in the wall, a tyre against it, bags.
        on(l,b,CalderSigns.Sign.BACK_DOOR,n,-107,33,0);
        fixed(l,b,CalderTownBlock.Kind.UNIT,CalderTownBlock.COOLER,n,-107,36,2);fixed(l,b,CalderTownBlock.Kind.TYRES,2,n,-107,37,0);
        strew(l,b,CalderTownBlock.Kind.BAGS,35,-108,0,2);
        // Along the fence, and down the middle.
        fixed(l,b,CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET_STOOD,s,-115,14,0);
        strew(l,b,CalderTownBlock.Kind.DRUM,22,-114,2,2);
        strew(l,b,CalderTownBlock.Kind.BOXES,25,-114,2,3, 33,-114,2,CalderTownBlock.FLAT);
        strew(l,b,CalderTownBlock.Kind.BAGS,16,-114,2,3);
        strew(l,b,CalderTownBlock.Kind.CRATE,36,-113,1,CalderTownBlock.MILK_CRATE);
        strew(l,b,CalderTownBlock.Kind.TYRES,39,-113,0,0);
        strew(l,b,CalderTownBlock.Kind.TRASH,20,-112,0,CalderTownBlock.PUDDLE, 33,-111,1,2, 26,-112,3,0, 9,-113,2,1);
    }
    /**
     * North of Elm Street, west of the cut: Calder Jr. High at the block's west end, its lawn before it on Elm, and its field
     * behind chain-link. A hole in the fence on Elm and another due north of it, behind the bleachers, which stand a block clear
     * of the ground: straight across the field and under them is the way.
     */
    static void school(ServerLevel l,BlockPos b,Random rnd){
        var s=Direction.SOUTH;int wall=-139,west=-39,east=-28,fenceS=FIELD_S+1,fenceN=FIELD_N-1,fenceE=FIELD_E+1;
        shell(l,b,west,fenceN,east,wall,5,mat("tanbrick"));
        box(l,b,west,2,fenceN,east,2,wall,mat("concrete"));
        on(l,b,CalderSigns.Sign.SCHOOL_FRONT,s,wall,-38,0);on(l,b,CalderSigns.Sign.TROPHY,s,wall,-31,0);on(l,b,CalderSigns.Sign.JRHIGH_NAME,s,wall,-38,3);
        path(l,b,-37,wall+1,-34,fenceS);
        // The tree somebody has been at with toilet paper.
        tree(l,b,-30,-136,6,rnd);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            int x=-30+dx,z=-136+dz;
            if(!free(l,b,x,3,z)||free(l,b,x,4,z)||rnd.nextInt(2)!=0)continue;
            set(l,b,x,3,z,town(CalderTownBlock.Kind.TP,Direction.NORTH,0));
            if(free(l,b,x,2,z)&&rnd.nextInt(2)==0)set(l,b,x,2,z,town(CalderTownBlock.Kind.TP,Direction.EAST,0));
        }
        // The field's fence, and the two holes in it.
        link(l,b,FIELD_W,fenceS,fenceE,fenceS);link(l,b,fenceE,fenceS,fenceE,fenceN);link(l,b,FIELD_W,fenceN,fenceE,fenceN);
        link(l,b,FIELD_W,wall+1,FIELD_W,fenceS);
        post(l,b,fenceE,fenceS,false);post(l,b,fenceE,fenceN,false);post(l,b,FIELD_W,fenceS,false);post(l,b,FIELD_W,wall+1,false);
        hole(l,b,HOLE_SOUTH);hole(l,b,HOLE_NORTH);
        // The scoreboard, on the school's wall over the field. It is off; the last game is still on it.
        on(l,b,CalderSigns.Sign.SCOREBOARD,Direction.EAST,east,SCORE_LO,2);
        // The bleachers, on posts front and back.
        for(int k=0;k<STAND_ROWS;k++)for(int x=STAND_W;x<=STAND_E;x++)set(l,b,x,k+1,STAND_FRONT-k,town(CalderTownBlock.Kind.BLEACHER,s,0));
        for(int x:new int[]{STAND_W,-21,-15,STAND_E}){
            set(l,b,x,0,STAND_FRONT,BikeNightSite.play(CalderPlayBlock.Kind.POST,Direction.NORTH,CalderPlayBlock.FOOT));
            for(int y=0;y<STAND_ROWS;y++)set(l,b,x,y,STAND_FRONT-STAND_ROWS+1,BikeNightSite.play(CalderPlayBlock.Kind.POST,Direction.NORTH,y==0?CalderPlayBlock.FOOT:CalderPlayBlock.SHAFT));
        }
        tree(l,b,-13,-163,6,rnd);
    }
    /**
     * North of Elm Street, east of the cut: Elm Court, six houses round a turning circle. Board fences shut their yards in; the
     * path out of the head of the court goes between two of them to Church Lane; and boards are gone from the fence along the
     * cut and from the one along East Avenue.
     */
    static void court(ServerLevel l,BlockPos b,Random rnd){
        house(l,b,5,-137,9,-129,Direction.EAST,rnd);path(l,b,10,-133,16,-133);
        house(l,b,2,-149,6,-141,Direction.EAST,rnd);path(l,b,7,-145,15,-145);
        house(l,b,8,-158,16,-154,Direction.SOUTH,rnd);path(l,b,12,-151,PATH_W-1,-151);
        house(l,b,26,-158,34,-154,Direction.SOUTH,rnd);path(l,b,PATH_E+1,-151,30,-151);
        house(l,b,34,-149,38,-141,Direction.WEST,rnd);path(l,b,29,-145,33,-145);
        house(l,b,32,-137,36,-129,Direction.WEST,rnd);path(l,b,28,-133,31,-133);
        // The fences: along the cut, behind the two houses at the head of the court, along East Avenue, and down each side of the path.
        boards(l,b,CUT_E+1,-160,CUT_E+1,CUT_S);
        boards(l,b,CUT_E+1,-160,PATH_W-1,-160);boards(l,b,PATH_E+1,-159,40,-159);boards(l,b,40,-159,40,-127);
        boards(l,b,PATH_W-1,-159,PATH_W-1,-153);boards(l,b,PATH_E+1,-159,PATH_E+1,-153);
        post(l,b,CUT_E+1,-160,true);post(l,b,PATH_W-1,-160,true);post(l,b,PATH_E+1,-159,true);post(l,b,40,-159,true);
        hole(l,b,YARD_GAP_WEST);hole(l,b,YARD_GAP_EAST);
        // Hedges along Elm Street at the two corner lots.
        for(int x=28;x<=39;x++)if(surface(x,-127)==NONE&&free(l,b,x,0,-127))set(l,b,x,0,-127,mat("hedge"));
        for(int x=-3;x<=3;x++)if(surface(x,-135)==NONE&&free(l,b,x,0,-135))set(l,b,x,0,-135,mat("hedge"));
        tree(l,b,-1,-139,6,rnd);tree(l,b,9,-151,5,rnd);tree(l,b,37,-152,6,rnd);tree(l,b,38,-132,5,rnd);tree(l,b,4,-156,7,rnd);tree(l,b,13,-139,5,rnd);
        strew(l,b,CalderTownBlock.Kind.TYRES,0,-146,0,0);
        strew(l,b,CalderTownBlock.Kind.CRATE,39,-143,0,CalderTownBlock.MILK_CRATE);
    }
    /** Along the far side of each avenue: houses looking over it, set back as the avenue bends, a board fence behind them. */
    static void avenues(ServerLevel l,BlockPos b,Random rnd){
        // West Avenue's: four where it runs along the edge of the ground, four where it has bent in.
        for(int z0:new int[]{-69,-82,-95,-108})house(l,b,-62,z0,-59,z0+8,Direction.EAST,rnd);
        for(int z0:new int[]{-126,-139,-152,-165})house(l,b,-58,z0,-55,z0+8,Direction.EAST,rnd);
        boards(l,b,-62,-176,-62,-70);post(l,b,-62,-176,true);
        // East Avenue's: two south of its bend, five north of it.
        for(int z0:new int[]{-72,-86})house(l,b,59,z0,62,z0+8,Direction.WEST,rnd);
        for(int z0:new int[]{-108,-121,-134,-147,-160})house(l,b,55,z0,58,z0+8,Direction.WEST,rnd);
        boards(l,b,62,-176,62,-73);post(l,b,62,-176,true);
        for(int[] t:new int[][]{{-60,-98,6},{-60,-85,5},{-60,-72,6},{-59,-113,6},{-60,-122,5},{-61,-148,6},{-60,-170,6},{-53,-172,7},
                {60,-75,5},{58,-94,6},{60,-90,5},{60,-117,6},{61,-143,5},{60,-164,6},{53,-171,6}})tree(l,b,t[0],t[1],t[2],rnd);
    }
    /** The cemetery's railings along the north of the town, and its gate where Church Lane comes nearest. */
    static void cemetery(ServerLevel l,BlockPos b,Random rnd){
        int pw=GATE_W-1,pe=GATE_E+1;
        for(int x=W;x<=E;x++){
            if(x>=GATE_W&&x<=GATE_E)continue;
            if(x==pw||x==pe||x==W||x==E||(x<pw&&(pw-x)%7==0)||(x>pe&&(x-pe)%7==0))BikeNightSite.pier(l,b,x,RAILS);else BikeNightSite.rail(l,b,x,RAILS,Direction.NORTH);
        }
        for(int x=GATE_W;x<=GATE_E;x++){
            var facing=(x-GATE_W)%2==0?Direction.NORTH:Direction.SOUTH;
            set(l,b,x,0,RAILS,fit(CalderBlock.Kind.GATE,facing,0));set(l,b,x,1,RAILS,fit(CalderBlock.Kind.GATE,facing,1));
        }
        CalderSigns.hang(l,b.offset(pw,3,RAILS),CalderSigns.Sign.CEMETERY,Direction.SOUTH);
        // 0.4.489: what was a line of trees behind the railings is the cemetery itself (CalderGraves), which has its own.
        // Between the lane and the railings, where the lane swings away from them.
        for(int[] t:new int[][]{{-38,-173},{-30,-175},{32,-173},{40,-175},{24,-174}})if(surface(t[0],t[1])==NONE)BikeNightSite.tree(l,b,t[0],t[1],5+rnd.nextInt(3),rnd);
    }
    /**
     * Past Park Street's two ends: the park's railings carried on along its side of the street to the edge of the ground, and
     * chain-link from them to the last house on each avenue, with a tree or two behind it.
     */
    static void corners(ServerLevel l,BlockPos b,Random rnd){
        int line=BikeNightSite.NORTH,yard=BikeNightSite.FAR_EAST+1;
        for(int side=-1;side<=1;side+=2){
            for(int x=BikeNightSite.EAST+1;x<=E;x++){
                if((x-BikeNightSite.EAST)%7==0||x==yard)BikeNightSite.pier(l,b,side*x,line);else BikeNightSite.rail(l,b,side*x,line,Direction.NORTH);
            }
            // The wire ends on two whole posts against the last house's own corner. A run of it along the house's side would stand on
            // its block's middle line, half a block off the wall, and a child walks round the end of that.
            int last=side<0?-61:-64;
            link(l,b,side*yard,last+1,side*yard,line-1);post(l,b,side*yard,last,false);post(l,b,side*(yard+1),last,false);
            BikeNightSite.tree(l,b,side*(E-2),-51,6,rnd);BikeNightSite.tree(l,b,side*(E-1),line-3,5,rnd);
        }
    }
    /** What stands on the pavements: lamps, the street names at the corners, stop signs, and the rest. Each finds the pavement nearest where it is meant. */
    static void fixtures(ServerLevel l,BlockPos b){
        // Lamps: at the corners, along Main Street, one at the head of Elm Court, one by the cemetery's gate.
        // Those by the gaps between two shops stand at a shop's corner, beside the gap and not in its mouth: a bike comes out of it.
        int[][] lamps={{-5,-86},{5,-86},{-3,-98},{7,-98},{-24,-86},{-36,-86},{25,-86},{40,-86},{-24,-98},{17,-98},{33,-98},
            {-44,-86},{-44,-98},{44,-86},{44,-98},{-5,-57},{5,-57},{-44,-57},{44,-57},{-40,-125},{7,-117},{-3,-125},{40,-117},{-25,-133},
            {26,-131},{25,-147},{3,-166},{5,-175},{-40,-161},{35,-161},{-44,-67},{-40,-146},{44,-72},{40,-140}};
        for(int[] at:lamps)lamp(l,b,at[0],at[1]);
        // Street names.
        var mill=CalderSigns.Sign.MILL_ST;var west=CalderSigns.Sign.WEST_AVE;var east=CalderSigns.Sign.EAST_AVE;
        names(l,b,-6,-58,CalderSigns.Sign.PARK_ST,mill);names(l,b,6,-85,CalderSigns.Sign.MAIN_ST,mill);names(l,b,-3,-99,CalderSigns.Sign.MAIN_ST,mill);
        names(l,b,7,-116,CalderSigns.Sign.ELM_ST,mill);
        names(l,b,-44,-58,CalderSigns.Sign.PARK_ST,west);names(l,b,-44,-85,CalderSigns.Sign.MAIN_ST,west);names(l,b,-40,-124,CalderSigns.Sign.ELM_ST,west);
        names(l,b,-40,-160,CalderSigns.Sign.CHURCH_LN,west);
        names(l,b,44,-58,CalderSigns.Sign.PARK_ST,east);names(l,b,44,-85,CalderSigns.Sign.MAIN_ST,east);names(l,b,40,-116,CalderSigns.Sign.ELM_ST,east);
        names(l,b,40,-160,CalderSigns.Sign.CHURCH_LN,east);
        names(l,b,27,-126,CalderSigns.Sign.ELM_ST,CalderSigns.Sign.ELM_CT);
        // Where a street ends on another, and at the mouth of the court.
        posted(l,b,6,-119,CalderSigns.Sign.STOP,Direction.SOUTH);
        posted(l,b,-41,-133,CalderSigns.Sign.STOP,Direction.EAST);posted(l,b,41,-126,CalderSigns.Sign.STOP,Direction.WEST);
        posted(l,b,-56,-58,CalderSigns.Sign.STOP,Direction.NORTH);posted(l,b,55,-58,CalderSigns.Sign.STOP,Direction.NORTH);
        posted(l,b,17,-127,CalderSigns.Sign.DEAD_END,Direction.SOUTH);
        // The way to the cemetery, named where the cut leaves Elm Street: a path's blade, lying along the path.
        blade(l,b,CUT_W-2,CUT_S+2,CalderSigns.Sign.TO_CEMETERY);
        // A bench and a bin outside the Video Hut; hydrants; post boxes.
        var seat=pavement(l,b,26,-98,1);
        if(free(l,b,seat.getX()-1,0,seat.getZ())){BikeNightSite.bench(l,b,seat.getX(),seat.getZ(),Direction.SOUTH);}
        var bin=pavement(l,b,28,-98,1);set(l,b,bin,fit(CalderBlock.Kind.BIN,Direction.NORTH,0));
        for(int[] at:new int[][]{{-10,-98},{38,-86},{-20,-125},{20,-166}})set(l,b,pavement(l,b,at[0],at[1],1),town(CalderTownBlock.Kind.HYDRANT,Direction.NORTH,0));
        set(l,b,pavement(l,b,-20,-86,1),town(CalderTownBlock.Kind.MAILBOX,Direction.NORTH,0));
        set(l,b,pavement(l,b,14,-117,1),town(CalderTownBlock.Kind.MAILBOX,Direction.SOUTH,0));
    }
    /** Leaves down on the town's grass, thickest under its trees. */
    static void litter(ServerLevel l,BlockPos b,Random rnd){
        var facings=new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
        for(int x=W+1;x<=E-1;x++)for(int z=N+1;z<=BikeNightSite.WALK_N-1;z++){
            if(!BuildBlocks.state(l,b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK)||!free(l,b,x,0,z))continue;
            boolean under=false;for(int y=3;y<=9&&!under;y++)under=BuildBlocks.state(l,b.offset(x,y,z)).is(BikeNightRegistry.LEAVES.get());
            if(rnd.nextInt(under?3:12)!=0)continue;
            set(l,b,x,0,z,CalderLitterBlock.of(facings[rnd.nextInt(4)],rnd.nextInt(4)));
        }
    }

    // ------------------------------------------------------------------------------------------------ what changes
    /**
     * The bank's clock, {@code tick} ticks into a night: the time for seven seconds, then the temperature for three. The night
     * starts at nineteen minutes to midnight, and it gets a degree colder every five minutes.
     */
    public static void clock(ServerLevel l,BlockPos b,long tick){
        int[] show=shown(tick);
        for(int i=0;i<CLOCK_PLACES;i++){
            var at=b.offset(CLOCK).west(i);var want=show[i]<0?Blocks.AIR.defaultBlockState():CalderDigitBlock.of(show[i],Direction.NORTH);
            if(!l.getBlockState(at).equals(want))l.setBlock(at,want,Block.UPDATE_CLIENTS);
        }
    }
    /** What the clock is to show {@code tick} ticks into a night, a place at a time from the reader's left: -1 where a place is dark. */
    public static int[] shown(long tick){
        long minute=CLOCK_FROM+tick/1200;int hour=(int)(minute/60%12),past=(int)(minute%60),degrees=Math.max(33,41-(int)(tick/6000));
        if(hour==0)hour=12;
        return tick%200>=140?new int[]{-1,degrees/10,degrees%10,CalderDigitBlock.DEGREES,-1}
            :new int[]{hour>=10?hour/10:-1,hour%10,CalderDigitBlock.COLON,past/10,past%10};
    }
    /** What the clock's places do show, in the same order. */
    public static int[] clockShows(ServerLevel l,BlockPos b){
        int[] out=new int[CLOCK_PLACES];
        for(int i=0;i<CLOCK_PLACES;i++){var s=l.getBlockState(b.offset(CLOCK).west(i));out[i]=s.is(BikeNightRegistry.DIGIT.get())?s.getValue(CalderDigitBlock.VALUE):-1;}
        return out;
    }
    /** Which double feature the marquee has up: 0, 1 or 2; -1 if none. */
    public static int feature(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(MARQUEE));
        for(int i=0;i<FEATURES.length;i++)if(CalderSignBlock.is(s,FEATURES[i]))return i;
        return -1;
    }
    /** Puts a double feature up on the marquee in place of the one that is there. */
    public static void feature(ServerLevel l,BlockPos b,int which){
        var up=FEATURES[Math.floorMod(which,FEATURES.length)];
        for(int row=0;row<up.rows;row++)for(int col=0;col<up.cols;col++)
            l.setBlock(b.offset(MARQUEE).offset(col,row,0),CalderSignBlock.of(up,(up.rows-1-row)*up.cols+(up.cols-1-col),Direction.NORTH),Block.UPDATE_CLIENTS);
    }
    /** Changes a sign that hangs on a wall for another of its size, block for block. Where no sign hangs, nothing is hung. */
    private static void change(ServerLevel l,BlockPos b,CalderSigns.Sign to,Direction facing,int plane,int lo,int y){
        int left=facing==Direction.SOUTH||facing==Direction.WEST?lo:lo+to.cols-1;var at=b.offset(before(facing,plane,left,y,1));
        for(int row=0;row<to.rows;row++)for(int col=0;col<to.cols;col++){
            var cell=CalderSigns.cell(to,at,facing,col,row);var now=l.getBlockState(cell);
            if(CalderSignBlock.is(now)&&!CalderSignBlock.is(now,to))l.setBlock(cell,CalderSignBlock.of(to,row*to.cols+col,facing),Block.UPDATE_CLIENTS);
        }
    }
    /** 0.4.486: the burger place's name, both of them, with its bad letter out or lit. */
    public static void neon(ServerLevel l,BlockPos b,boolean dim){
        var to=dim?CalderSigns.Sign.BIGTOP_NAME_DIM:CalderSigns.Sign.BIGTOP_NAME;
        change(l,b,to,Direction.SOUTH,NEON_SOUTH.getZ()-1,NEON_SOUTH.getX(),NEON_SOUTH.getY());change(l,b,to,Direction.NORTH,NEON_NORTH.getZ()+1,NEON_NORTH.getX(),NEON_NORTH.getY());
    }
    public static boolean neonDim(ServerLevel l,BlockPos b,boolean north){return CalderSignBlock.is(l.getBlockState(b.offset(north?NEON_NORTH:NEON_SOUTH)),CalderSigns.Sign.BIGTOP_NAME_DIM);}
    private static final CalderSigns.Sign[] AISLES={CalderSigns.Sign.FOOD_A,CalderSigns.Sign.FOOD_B,CalderSigns.Sign.FOOD_C},AISLES_DARK={CalderSigns.Sign.FOOD_A_DARK,CalderSigns.Sign.FOOD_B_DARK,CalderSigns.Sign.FOOD_C_DARK};
    /** 0.4.486: one of the Food Fair's three windows, with the lights behind it on or off. */
    public static void aisle(ServerLevel l,BlockPos b,int which,boolean lit){change(l,b,(lit?AISLES:AISLES_DARK)[which],Direction.SOUTH,FOOD[which].getZ()-1,FOOD[which].getX(),FOOD[which].getY());}
    public static boolean aisleLit(ServerLevel l,BlockPos b,int which){return CalderSignBlock.is(l.getBlockState(b.offset(FOOD[which])),AISLES[which]);}
    /** A new night: the next double feature on the marquee, and the clock back at the night's first minute. */
    public static void newNight(ServerLevel l,BlockPos b){
        if(!l.isLoaded(b.offset(MARQUEE))||!l.isLoaded(b.offset(CLOCK)))return;
        feature(l,b,feature(l,b)+1);clock(l,b,0);
    }
}
