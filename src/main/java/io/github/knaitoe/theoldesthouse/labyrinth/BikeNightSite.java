package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.464, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): Calder Falls on the last night of October, under the outside sky. Every position
 * is relative to the base, the ground just inside the door the reader comes through; north is -z; people stand at y 0 on ground at y -1.
 *
 * <p>This slice, the park. The House's door is the door of the park's tool shed, in the brick wall along the park's south side. A path
 * of concrete flags runs north from it to the gate, which is chained (the town beyond it comes later). Iron railings between brick
 * piers close the other three sides. West of the path is the playground's sand in its timber edging, under two lamps, one of which is
 * failing. By the gate the six bikes stand in a rack under a lamp of their own. Outside the gate, Park Street: the pavement, the kerb,
 * the road and its centre line, and dark trees past it.
 *
 * <p>0.4.465: the swings. A steel frame along the playground's west side, its bar four blocks over the sand, with four belt seats on
 * chains under it, each looking east over the open sand a jump off one comes down on; and a lamp of their own behind them.
 *
 * <p>0.4.466: the slide, in the playground's north-east corner under its lamp: a ladder, a deck two blocks up, and a chute going
 * down to the south.
 */
public final class BikeNightSite {
    private BikeNightSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BIKE_NIGHT;
    /** The railings' three lines (the back wall is at z 1), and how far the ground is laid past them. */
    public static final int WEST=-31,EAST=31,NORTH=-45,FAR_WEST=-56,FAR_EAST=56,FAR_NORTH=-72;
    /** The path from the door to the gate, and the gateway's four blocks between its piers. */
    public static final int PATH_W=-2,PATH_E=1,PIER_W=-3,PIER_E=2;
    /** The playground's sand, and the gap in its edging where the side path comes in. */
    public static final int SAND_W=-26,SAND_E=-7,SAND_S=-12,SAND_N=-34,SIDE_S=-20,SIDE_N=-21;
    /** Park Street, from the park outward: pavement, kerb, road, kerb, pavement; and the row its centre line is on. */
    public static final int WALK_S=-46,KERB_S=-48,ROAD_S=-49,ROAD_N=-55,KERB_N=-56,WALK_N=-58,LINE=-52;
    /** The rack by the gate: six places in a row, the bikes standing south of it with their front wheels in it. */
    public static final int RACK_X=4,RACK_Z=-43,BIKE_Z=-42;
    /** The lamps: by the door, the failing one and the good one over the playground, and the rack's. Each is four blocks tall. */
    public static final BlockPos DOOR_LAMP=new BlockPos(-7,0,-3),FLICKER=new BlockPos(-5,0,-13),PLAY_LAMP=new BlockPos(-5,0,-33),RACK_LAMP=new BlockPos(10,0,-43);
    /** The swings' own lamp, behind them on the grass. */
    public static final BlockPos SWING_LAMP=new BlockPos(-28,0,-25);
    public static final List<BlockPos> LAMPS=List.of(DOOR_LAMP,FLICKER,PLAY_LAMP,RACK_LAMP,SWING_LAMP);
    /**
     * The swings: the line the frame stands on, how high its bar is over the sand, where its three posts and four seats are along
     * it, and the way a sitter looks.
     */
    public static final int SWING_X=-23,SWING_BAR=4;
    public static final int[] SWING_POSTS={-31,-25,-19},SWING_SEATS={-29,-27,-23,-21};
    public static final Direction SWING_FORWARD=Direction.EAST;
    /** 0.4.466, the slide: where its ladder stands, and the way it goes down. */
    public static final BlockPos SLIDE=new BlockPos(-10,0,-32);
    public static final Direction SLIDE_FORWARD=Direction.SOUTH;
    /**
     * 0.4.467, the monkey bars: the row they run along, east and west; their first and last blocks of rungs; and how high the
     * rungs' block is over the sand. A ladder stands at each end, a block beyond the rungs.
     */
    public static final int BARS_Z=-15,BARS_W=-19,BARS_E=-14,BARS_Y=2;
    /** 0.4.468, the seesaw: where its stand is, and the way its first end points. Its ends are over the blocks two out each way. */
    public static final BlockPos SEESAW=new BlockPos(-10,0,-17);
    public static final Direction SEESAW_FACING=Direction.NORTH;
    /**
     * 0.4.471, the climbing dome: the middle of its ground, in the playground's south-west corner. It is three blocks across;
     * its ground is five square and two high (DomeBlock). And the spring rider, in the north-west corner, and the way its horse
     * looks.
     */
    public static final BlockPos DOME=new BlockPos(-24,0,-14),SPRING=new BlockPos(-25,0,-33);
    public static final Direction SPRING_FACING=Direction.EAST;
    /** 0.4.471: the drinking fountain, on the grass by the side path; and the payphone, on the back wall west of the door. */
    public static final BlockPos FOUNTAIN=new BlockPos(-5,0,-19),PHONE=new BlockPos(-10,1,0);
    /** 0.4.469, the merry-go-round: its hub. Its deck is over the eight blocks round it as well. */
    public static final BlockPos ROUNDABOUT=new BlockPos(-15,0,-32);
    public static final int LAMP_TOP=3;
    /** The sheet taped to the wall by the door (the first paper), and where a reader's account is handed over. */
    public static final BlockPos SOURCE=new BlockPos(-4,1,0),ENDING=new BlockPos(-4,2,0);
    /** The way on, until the far gate is built: a service door in the back wall, east of the shed. */
    public static final BlockPos ONWARD=new BlockPos(16,0,1);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    /** Inside the railings. */
    public static boolean inPark(Vec3 r){return r.x>WEST+.5&&r.x<EAST+.5&&r.z<1&&r.z>NORTH+.5&&r.y>-1&&r.y<12;}
    /** On the playground's sand. */
    public static boolean onSand(Vec3 r){return r.x>=SAND_W&&r.x<SAND_E+1&&r.z>=SAND_N&&r.z<SAND_S+1&&r.y>-.6&&r.y<3;}
    /** A block of the playground's sand. */
    public static boolean sandAt(BlockPos r){return r.getY()==-1&&r.getX()>=SAND_W&&r.getX()<=SAND_E&&r.getZ()>=SAND_N&&r.getZ()<=SAND_S;}
    /** The slide's deck (the block its platform is the top of), the top block of its chute, and its level end. */
    public static BlockPos slideDeck(){return SLIDE.relative(SLIDE_FORWARD).above();}
    public static BlockPos slideTop(){return SLIDE.relative(SLIDE_FORWARD,2).above();}
    public static BlockPos slideEnd(){return SLIDE.relative(SLIDE_FORWARD,2+CalderPlayBlock.CHUTES);}
    /** A block of the monkey bars' rungs, counted from the west; and the ladder at an end of them (0 the west, 1 the east), its lower block. */
    public static BlockPos rungsAt(int k){return new BlockPos(BARS_W+k,BARS_Y,BARS_Z);}
    public static int rungs(){return BARS_E-BARS_W+1;}
    public static BlockPos barsLadder(int end){return new BlockPos(end==0?BARS_W-1:BARS_E+1,0,BARS_Z);}
    /** The block an end of the seesaw's board is over (0 its first end, 1 its other). */
    public static BlockPos seesawEnd(int end){return SeesawBlock.end(SEESAW,SEESAW_FACING,end);}
    /** Where a swing's seat hangs. */
    public static BlockPos swingAt(int seat){return new BlockPos(SWING_X,0,SWING_SEATS[seat]);}
    /** Where a bike stands in the rack. */
    public static BlockPos bikeAt(NightBike.Make make){return new BlockPos(RACK_X+make.ordinal(),0,BIKE_Z);}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static boolean free(ServerLevel l,BlockPos b,int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z)).isAir();}
    static BlockState mat(String name){return BikeNightRegistry.material(name).defaultBlockState();}
    static BlockState fit(CalderBlock.Kind kind,Direction facing,int stage){return CalderBlock.of(kind,facing,stage);}
    static BlockState play(CalderPlayBlock.Kind kind,Direction facing,int stage){return CalderPlayBlock.of(kind,facing,stage);}
    static BlockState leaves(){return BikeNightRegistry.LEAVES.get().defaultBlockState();}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(46400);
        box(l,b,FAR_WEST,-4,FAR_NORTH,FAR_EAST,17,0,air);
        box(l,b,FAR_WEST,-4,FAR_NORTH,FAR_EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,FAR_WEST,-1,FAR_NORTH,FAR_EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        wall(l,b);
        street(l,b);
        paths(l,b);
        sandpit(l,b);
        swings(l,b);
        slide(l,b);
        bars(l,b);
        seesaw(l,b);
        roundabout(l,b);
        dome(l,b);
        set(l,b,SPRING,SpringBlock.of(SpringBlock.Part.FOOT,SPRING_FACING));
        railings(l,b);
        gate(l,b);
        for(var at:LAMPS)lamp(l,b,at,true);
        benches(l,b);
        rack(l,b);
        signs(l,b);
        trees(l,b,rnd);
        litter(l,b,rnd);
    }

    /** The park's back wall, with the tool shed's door in it: brick six high under a stone coping, the whole width of the ground. */
    static void wall(ServerLevel l,BlockPos b){
        box(l,b,FAR_WEST,-1,1,FAR_EAST,5,1,mat("brick"));
        box(l,b,FAR_WEST,6,1,FAR_EAST,6,1,mat("kerb"));
    }
    /** The shed's own front stands two courses over the wall, under a coping of its own. Laid after the House's door is in. */
    static void shed(ServerLevel l,BlockPos b){
        int half=LabyrinthPlaces.VESTIBULE_HALF_WIDTH,top=LabyrinthPlaces.VESTIBULE_TOP;
        box(l,b,-half,6,1,half,top,1,mat("brick"));
        box(l,b,-half,top+1,1,half,top+1,1,mat("kerb"));
    }
    /** Park Street, running east and west past the gate. */
    static void street(ServerLevel l,BlockPos b){
        box(l,b,FAR_WEST,-1,WALK_S-1,FAR_EAST,-1,WALK_S,mat("paving"));
        box(l,b,FAR_WEST,-1,KERB_S,FAR_EAST,-1,KERB_S,mat("kerb"));
        box(l,b,FAR_WEST,-1,ROAD_N,FAR_EAST,-1,ROAD_S,mat("asphalt"));
        box(l,b,FAR_WEST,-1,LINE,FAR_EAST,-1,LINE,BikeNightRegistry.ROAD_LINE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        box(l,b,FAR_WEST,-1,KERB_N,FAR_EAST,-1,KERB_N,mat("kerb"));
        box(l,b,FAR_WEST,-1,WALK_N,FAR_EAST,-1,WALK_N+1,mat("paving"));
    }
    /** The path from the door to the gate and out to the pavement, and the side path to the playground. */
    static void paths(ServerLevel l,BlockPos b){
        box(l,b,PATH_W,-1,WALK_S,PATH_E,-1,0,mat("paving"));
        box(l,b,SAND_E+1,-1,SIDE_N,PATH_W-1,-1,SIDE_S,mat("paving"));
    }
    /** The playground: sand, and squared timbers round it, open where the side path comes in. */
    static void sandpit(ServerLevel l,BlockPos b){
        box(l,b,SAND_W,-1,SAND_N,SAND_E,-1,SAND_S,Blocks.SAND.defaultBlockState());
        for(int x=SAND_W;x<=SAND_E;x++)for(int z=SAND_N;z<=SAND_S;z++){
            boolean w=x==SAND_W,e=x==SAND_E,n=z==SAND_N,s=z==SAND_S;
            if(!(w||e||n||s))continue;
            if(e&&z<=SIDE_S&&z>=SIDE_N)continue;
            // A corner's two timbers lie along the edge it faces and the edge to that one's left.
            if(n&&w)set(l,b,x,0,z,fit(CalderBlock.Kind.BORDER,Direction.NORTH,1));
            else if(n&&e)set(l,b,x,0,z,fit(CalderBlock.Kind.BORDER,Direction.EAST,1));
            else if(s&&e)set(l,b,x,0,z,fit(CalderBlock.Kind.BORDER,Direction.SOUTH,1));
            else if(s&&w)set(l,b,x,0,z,fit(CalderBlock.Kind.BORDER,Direction.WEST,1));
            else set(l,b,x,0,z,fit(CalderBlock.Kind.BORDER,n?Direction.NORTH:s?Direction.SOUTH:w?Direction.WEST:Direction.EAST,0));
        }
    }
    /**
     * The swings. Three posts and a bar over them; under the bar, between the posts, two seats to a bay, each on its chains. What
     * a seat faces is behind whoever sits on it; a bar ends on an end post (it goes off to the east of a post facing north) and
     * runs on over the middle one.
     */
    static void swings(ServerLevel l,BlockPos b){
        var behind=SWING_FORWARD.getOpposite();int first=SWING_POSTS[0],last=SWING_POSTS[SWING_POSTS.length-1];
        for(int z=first;z<=last;z++){
            boolean post=false,seat=false;for(int at:SWING_POSTS)post|=at==z;for(int at:SWING_SEATS)seat|=at==z;
            if(post){
                set(l,b,SWING_X,0,z,play(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.FOOT));
                for(int y=1;y<SWING_BAR;y++)set(l,b,SWING_X,y,z,play(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.SHAFT));
                set(l,b,SWING_X,SWING_BAR,z,z==first?play(CalderPlayBlock.Kind.POST,Direction.EAST,CalderPlayBlock.END)
                    :z==last?play(CalderPlayBlock.Kind.POST,Direction.WEST,CalderPlayBlock.END):play(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.THROUGH));
                continue;
            }
            set(l,b,SWING_X,SWING_BAR,z,play(CalderPlayBlock.Kind.BAR,behind,seat?CalderPlayBlock.HANGERS:0));
            if(!seat)continue;
            set(l,b,SWING_X,0,z,play(CalderPlayBlock.Kind.SWING,behind,0));
            for(int y=1;y<SWING_BAR;y++)set(l,b,SWING_X,y,z,play(CalderPlayBlock.Kind.CHAIN,behind,0));
        }
    }
    /**
     * The slide: a ladder of two, the deck at the top of it under its guard, four blocks of chute going down the way it faces
     * (the upper two a block higher than the lower two), and the level end.
     */
    static void slide(ServerLevel l,BlockPos b){
        var f=SLIDE_FORWARD;
        set(l,b,SLIDE,play(CalderPlayBlock.Kind.LADDER,f,0));set(l,b,SLIDE.above(),play(CalderPlayBlock.Kind.LADDER,f,1));
        set(l,b,slideDeck(),play(CalderPlayBlock.Kind.DECK,f,0));set(l,b,slideDeck().above(),play(CalderPlayBlock.Kind.DECK,f,CalderPlayBlock.GUARD));
        for(int k=0;k<CalderPlayBlock.CHUTES;k++){var at=slideTop().relative(f,k);set(l,b,CalderPlayBlock.chuteLow(k)?at.below():at,play(CalderPlayBlock.Kind.CHUTE,f,k));}
        set(l,b,slideEnd(),play(CalderPlayBlock.Kind.RUNOUT,f,0));
    }
    /**
     * The monkey bars: a ladder at each end, facing the rungs, and the rungs between them over a child's head. The end blocks of
     * rungs face their ladders, and their rails run on over them.
     */
    static void bars(ServerLevel l,BlockPos b){
        for(int end=0;end<2;end++){
            var toward=end==0?Direction.EAST:Direction.WEST;var foot=barsLadder(end);
            for(int y=0;y<BARS_Y;y++)set(l,b,foot.above(y),play(CalderPlayBlock.Kind.LADDER,toward,CalderPlayBlock.BARS+(y==BARS_Y-1?1:0)));
        }
        for(int k=0;k<rungs();k++)set(l,b,rungsAt(k),play(CalderPlayBlock.Kind.RUNGS,k==0?Direction.WEST:Direction.EAST,k==0||k==rungs()-1?CalderPlayBlock.RUNG_END:0));
    }
    /** The seesaw: its stand, and the place each end of its board is over. The board is the stand's, and starts with its first end down. */
    static void seesaw(ServerLevel l,BlockPos b){
        set(l,b,SEESAW,SeesawBlock.of(SeesawBlock.Part.STAND,SEESAW_FACING));
        for(int end=0;end<2;end++)set(l,b,seesawEnd(end),SeesawBlock.of(SeesawBlock.Part.END,end==0?SEESAW_FACING:SEESAW_FACING.getOpposite()));
    }
    /** The climbing dome: the cells of its ground. The middle one on the sand keeps what draws it. */
    static void dome(ServerLevel l,BlockPos b){
        // Where its ground runs out over the sandpit's edging the edging stays: the dome's bars do not reach that far.
        for(int y=0;y<DomeBlock.HIGH;y++)for(int x=0;x<DomeBlock.WIDE;x++)for(int z=0;z<DomeBlock.WIDE;z++){
            var at=DOME.offset(x-DomeBlock.MID,y,z-DomeBlock.MID);
            if(free(l,b,at.getX(),at.getY(),at.getZ()))set(l,b,at,DomeBlock.cell(x,y,z));
        }
    }
    /** The merry-go-round: its hub, which keeps the wheel, and the rest of the ground its deck is over. */
    static void roundabout(ServerLevel l,BlockPos b){
        for(var cell:RoundaboutBlock.Cell.values())if(cell!=RoundaboutBlock.Cell.WHEEL)set(l,b,ROUNDABOUT.offset(cell.dx,0,cell.dz),RoundaboutBlock.of(cell));
    }
    /** Any swing left as if somebody were on it (a server stopped under a reader) hangs still again. */
    static void swingsBack(ServerLevel l,BlockPos b){
        for(int i=0;i<SWING_SEATS.length;i++){
            var at=b.offset(swingAt(i));var s=l.getBlockState(at);
            if(!CalderPlayBlock.is(s,CalderPlayBlock.Kind.SWING)||s.getValue(CalderPlayBlock.STAGE)!=CalderPlayBlock.GONE)continue;
            if(!l.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.house.SeatEntity.class,new net.minecraft.world.phys.AABB(at).inflate(5,4,5),seat->at.equals(seat.support())).isEmpty())continue;
            HouseReadSwing.restore(l,at);
        }
    }
    /** A brick pier, three high, under a stone cap. */
    static void pier(ServerLevel l,BlockPos b,int x,int z){box(l,b,x,0,z,x,1,z,mat("brick"));set(l,b,x,2,z,fit(CalderBlock.Kind.CAP,Direction.NORTH,0));}
    /** One block of railings, two high, across the way it faces. */
    static void rail(ServerLevel l,BlockPos b,int x,int z,Direction facing){set(l,b,x,0,z,fit(CalderBlock.Kind.FENCE,facing,0));set(l,b,x,1,z,fit(CalderBlock.Kind.FENCE,facing,1));}
    /** The railings on three sides, a pier at each corner, at the gate and every so often between. */
    static void railings(ServerLevel l,BlockPos b){
        for(int x=WEST;x<=EAST;x++){
            if(x>=PATH_W&&x<=PATH_E)continue;
            if(x==WEST||x==EAST||x==PIER_W||x==PIER_E||(x<PIER_W&&(PIER_W-x)%7==0)||(x>PIER_E&&(x-PIER_E)%7==0&&x<EAST-2))pier(l,b,x,NORTH);
            else rail(l,b,x,NORTH,Direction.NORTH);
        }
        for(int z=NORTH+1;z<=0;z++)for(int x:new int[]{WEST,EAST}){
            if(z<0&&(-z)%9==0)pier(l,b,x,z);else rail(l,b,x,z,Direction.EAST);
        }
    }
    /** The gate: two leaves, two blocks each, shut, a chain round the stiles where they meet and a padlock on the park's side. */
    static void gate(ServerLevel l,BlockPos b){
        int[] x={PATH_W,PATH_W+1,PATH_E-1,PATH_E};
        Direction[] facing={Direction.NORTH,Direction.SOUTH,Direction.NORTH,Direction.SOUTH};
        int[] lower={0,2,3,0};
        for(int i=0;i<4;i++){set(l,b,x[i],0,NORTH,fit(CalderBlock.Kind.GATE,facing[i],lower[i]));set(l,b,x[i],1,NORTH,fit(CalderBlock.Kind.GATE,facing[i],1));}
    }
    /**
     * 0.4.479: the gate swung open on its hinges. Each leaf stands out over the pavement, end on to the gateway, its hinge by the pier
     * and its free end toward the road; the chain hangs from the west leaf's.
     */
    public static void gateOpen(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        for(int x=PATH_W;x<=PATH_E;x++)for(int y=0;y<2;y++)l.setBlock(b.offset(x,y,NORTH),air,3);
        for(int x:new int[]{PATH_W,PATH_E}){
            l.setBlock(b.offset(x,0,NORTH-1),fit(CalderBlock.Kind.GATE,Direction.WEST,0),3);l.setBlock(b.offset(x,1,NORTH-1),fit(CalderBlock.Kind.GATE,Direction.WEST,1),3);
            l.setBlock(b.offset(x,0,NORTH-2),fit(CalderBlock.Kind.GATE,Direction.EAST,x==PATH_W?2:0),3);l.setBlock(b.offset(x,1,NORTH-2),fit(CalderBlock.Kind.GATE,Direction.EAST,1),3);
        }
    }
    public static boolean gateIsOpen(ServerLevel l,BlockPos b){return !CalderBlock.is(l.getBlockState(b.offset(PATH_W,0,NORTH)),CalderBlock.Kind.GATE);}
    /** And shut again, chained and locked, as it is built. */
    public static void gateShut(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        for(int x:new int[]{PATH_W,PATH_E})for(int z=NORTH-2;z<=NORTH-1;z++)for(int y=0;y<2;y++)if(CalderBlock.is(l.getBlockState(b.offset(x,y,z)),CalderBlock.Kind.GATE))l.setBlock(b.offset(x,y,z),air,3);
        int[] x={PATH_W,PATH_W+1,PATH_E-1,PATH_E};Direction[] facing={Direction.NORTH,Direction.SOUTH,Direction.NORTH,Direction.SOUTH};int[] lower={0,2,3,0};
        for(int i=0;i<4;i++){l.setBlock(b.offset(x[i],0,NORTH),fit(CalderBlock.Kind.GATE,facing[i],lower[i]),3);l.setBlock(b.offset(x[i],1,NORTH),fit(CalderBlock.Kind.GATE,facing[i],1),3);}
    }
    /** A lamp post, four blocks tall: its foot, two of post, and its lantern, lit or dark. */
    public static void lamp(ServerLevel l,BlockPos b,BlockPos at,boolean lit){
        set(l,b,at,fit(CalderBlock.Kind.LAMP,Direction.NORTH,CalderBlock.FOOT));
        for(int y=1;y<LAMP_TOP;y++)set(l,b,at.above(y),fit(CalderBlock.Kind.LAMP,Direction.NORTH,CalderBlock.POST));
        lantern(l,b,at,lit);
    }
    /** Only the lantern, for the failing lamp. Clients are told; nothing else is disturbed. */
    public static void lantern(ServerLevel l,BlockPos b,BlockPos at,boolean lit){
        var top=b.offset(at).above(LAMP_TOP);var want=fit(CalderBlock.Kind.LAMP,Direction.NORTH,lit?CalderBlock.LIT:CalderBlock.DARK);
        if(!l.getBlockState(top).equals(want))BuildBlocks.set(l,top,want,Block.UPDATE_CLIENTS);
    }
    public static boolean lit(ServerLevel l,BlockPos b,BlockPos at){var s=l.getBlockState(b.offset(at).above(LAMP_TOP));return CalderBlock.is(s,CalderBlock.Kind.LAMP)&&s.getValue(CalderBlock.STAGE)==CalderBlock.LIT;}
    /** A bench two blocks long, looking {@code facing}; {@code left} is the end on the sitter's left. */
    static void bench(ServerLevel l,BlockPos b,int x,int z,Direction facing){
        set(l,b,x,0,z,fit(CalderBlock.Kind.BENCH,facing,1));
        var right=new BlockPos(x,0,z).relative(facing.getClockWise());
        set(l,b,right,fit(CalderBlock.Kind.BENCH,facing,2));
    }
    static void benches(ServerLevel l,BlockPos b){
        bench(l,b,3,-16,Direction.WEST);set(l,b,3,0,-18,fit(CalderBlock.Kind.BIN,Direction.NORTH,0));   // by the path, looking across it
        bench(l,b,-5,-23,Direction.WEST);                                                           // by the playground, looking at it
        set(l,b,-5,0,-23,fit(CalderBlock.Kind.BENCH,Direction.WEST,CalderBlock.CARVED));                // the one with the heart cut in it
        set(l,b,FOUNTAIN,fit(CalderBlock.Kind.FOUNTAIN,Direction.NORTH,0));
        set(l,b,PHONE,fit(CalderBlock.Kind.PHONE,Direction.NORTH,0));
        bench(l,b,3,-30,Direction.WEST);
    }
    /** The rack by the gate, and the six bikes in it, in the order they are named. */
    static void rack(ServerLevel l,BlockPos b){
        for(var make:NightBike.Make.values()){
            set(l,b,RACK_X+make.ordinal(),0,RACK_Z,BikeRackBlock.of(Direction.NORTH));
            set(l,b,bikeAt(make),NightBikeBlock.of(make,Direction.NORTH,false));
        }
    }
    /** The board over the gate, looking out at the street; the rules on the wall by the door; and the sheet taped up beside them. */
    static void signs(ServerLevel l,BlockPos b){
        CalderSigns.hang(l,b.offset(PIER_E,3,NORTH),CalderSigns.Sign.ARCH,Direction.NORTH);
        CalderSigns.hang(l,b.offset(8,1,0),CalderSigns.Sign.RULES,Direction.NORTH);
        set(l,b,SOURCE,fit(CalderBlock.Kind.NOTE,Direction.NORTH,0));
    }
    /** One tree: an oak's trunk, and what October has left of its crown. */
    static void tree(ServerLevel l,BlockPos b,int x,int z,int height,Random rnd){
        for(int y=0;y<height;y++)set(l,b,x,y,z,Blocks.OAK_LOG.defaultBlockState());
        int top=height-1;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-1;dy<=3;dy++){
            double d=Math.sqrt(dx*dx+dz*dz+(dy-1)*(dy-1)*1.6);
            if(d>3.1||(dx==0&&dz==0&&dy<=0))continue;
            if(d>2.2&&rnd.nextInt(3)==0)continue;
            if(rnd.nextInt(9)==0)continue;
            if(free(l,b,x+dx,top+dy,z+dz))set(l,b,x+dx,top+dy,z+dz,leaves());
        }
    }
    /** The park's own trees, and the dark belt of them outside the railings and past the street. */
    static void trees(ServerLevel l,BlockPos b,Random rnd){
        int[][] park={{-20,-5},{13,-8},{23,-20},{14,-27},{24,-38},{-14,-40},{21,-5},{-28,-3},{9,-36}};
        for(int[] t:park)tree(l,b,t[0],t[1],5+rnd.nextInt(2),rnd);
        for(int z=-4;z>=FAR_NORTH+3;z-=5){
            for(int x:new int[]{WEST-6-rnd.nextInt(3),WEST-14-rnd.nextInt(4),WEST-22,EAST+6+rnd.nextInt(3),EAST+14+rnd.nextInt(4),EAST+22}){
                int at=z+rnd.nextInt(3)-1;if(at<=WALK_S+1&&at>=WALK_N-2)continue;
                tree(l,b,x,at,5+rnd.nextInt(3),rnd);
            }
        }
        for(int x=FAR_WEST+4;x<=FAR_EAST-4;x+=5+rnd.nextInt(2))for(int z:new int[]{WALK_N-5-rnd.nextInt(2),WALK_N-10-rnd.nextInt(2)})tree(l,b,x,z,5+rnd.nextInt(3),rnd);
    }
    /** Leaves down on the grass, thickest under the trees. */
    static void litter(ServerLevel l,BlockPos b,Random rnd){
        var facings=new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
        for(int x=FAR_WEST+1;x<=FAR_EAST-1;x++)for(int z=FAR_NORTH+1;z<=0;z++){
            if(!BuildBlocks.state(l,b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK)||!free(l,b,x,0,z))continue;
            boolean under=false;for(int y=3;y<=8&&!under;y++)under=BuildBlocks.state(l,b.offset(x,y,z)).is(BikeNightRegistry.LEAVES.get());
            if(rnd.nextInt(under?3:11)!=0)continue;
            set(l,b,x,0,z,CalderLitterBlock.of(facings[rnd.nextInt(4)],rnd.nextInt(4)));
        }
    }
}
