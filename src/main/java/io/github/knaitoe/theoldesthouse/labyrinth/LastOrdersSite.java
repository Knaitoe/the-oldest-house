package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.416, Last orders (docs/LAST_ORDERS_0_4_416.md): a saloon bar long after midnight, built whole in its interior slot. From the door,
 * looking north: the bar counter down the east side with its stools, back bar and till; five numbered booths down the west wall; the pool
 * table under its lamp in the middle of the floor; the dartboard on the far wall with the league board beside it; and a window in the east wall,
 * past the end of the bar, onto a street that is never the same season twice.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class LastOrdersSite {
    private LastOrdersSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The shell: walls on these lines, floor under y 0, ceiling at TOP. */
    public static final int WEST=-12,EAST=12,NORTH=-32,TOP=6;
    /** The counter runs down x 7 from z -5 to -18; the stools stand at x 5; the back bar is at x 11. */
    public static final int COUNTER_X=7,COUNTER_S=-5,COUNTER_N=-18,STOOL_X=5,BACK_X=11;
    /** The booths: five, their tables at x -11 and -10, at these z, numbered one to five from the door. */
    public static final int[] BOOTHS={-6,-10,-14,-18,-22};
    /** The stranger's booth (four) and the next booth (three). */
    public static final int STRANGER_BOOTH=3,NEXT_BOOTH=2;
    /** The board on the house rules by the door (the source), and the till at the near end of the counter (the account). */
    public static final BlockPos RULES=new BlockPos(-4,1,-1),TILL=new BlockPos(COUNTER_X,1,COUNTER_S);
    /** The pool table: its foot here, the head one north. */
    public static final BlockPos TABLE=new BlockPos(-2,0,-12);
    /** The dartboard on the far wall, face south, and the oche line on the floor in front of it. */
    public static final BlockPos BOARD=new BlockPos(2,1,NORTH+1);
    public static final int OCHE_Z=NORTH+4;
    /** The league board: eight signs on the far wall west of the dartboard, two rows of four. */
    public static final BlockPos[] LEAGUE;
    static{var l=new BlockPos[8];for(int i=0;i<8;i++)l[i]=new BlockPos(-3-(i%4),2-i/4,NORTH+1);LEAGUE=l;}
    public static final BlockPos LEAGUE_HEAD=new BlockPos(-4,3,NORTH+1);
    /** The window in the east wall past the end of the bar, and the little street beyond it. */
    public static final int WINDOW_Z0=-23,WINDOW_Z1=-21;
    /** The way on: the street door, against the west wall by the front. */
    public static final BlockPos ONWARD=new BlockPos(WEST+1,0,-2);
    /** Where the stranger sits (booth four's far bench, facing the door) and where his opponent sits across from him. */
    public static final Vec3 STRANGER_SEAT=new Vec3(-10.0,0,BOOTHS[STRANGER_BOOTH]-1+.5),OPPONENT_SEAT=new Vec3(-10.0,0,BOOTHS[STRANGER_BOOTH]+1+.5);
    /** Where the stranger's roll lies when it is on the table. */
    public static final Vec3 ROLL=new Vec3(-10.0,1.05,BOOTHS[STRANGER_BOOTH]+.5);
    /** The two in the next booth, sat on its far bench with their backs to the stranger's booth. */
    public static final Vec3[] NEXT_SEATS={new Vec3(-10.5,0,BOOTHS[NEXT_BOOTH]-1+.5),new Vec3(-9.5,0,BOOTHS[NEXT_BOOTH]-1+.5)};
    /** The one at the bar, on the far stool; the three at the board, stood back from the oche. */
    public static final Vec3 BAR_SEAT=new Vec3(STOOL_X+.5,0,COUNTER_N+1.5);
    public static final Vec3[] BOARD_STANDS={new Vec3(-.5,0,OCHE_Z+2.5),new Vec3(4.5,0,OCHE_Z+2.5),new Vec3(6.0,0,OCHE_Z+.5)};
    /** Where whoever is throwing stands: on the oche, square to the board. */
    public static final Vec3 OCHE=new Vec3(BOARD.getX()+.5,0,OCHE_Z+.5);
    /** Where the stranger stands back from the oche while someone else throws. */
    public static final Vec3 DARTS_STAND=new Vec3(6.0,0,OCHE_Z+2.5);
    /** The clock over the door, at ten to. */
    public static final BlockPos CLOCK=new BlockPos(0,3,-1);
    /** At the pool table, for the stranger to stand while he plays: the head end, off the cloth. */
    public static final Vec3 TABLE_STAND=new Vec3(TABLE.getX()+1.8,0,TABLE.getZ()-1.5);

    // ------------------------------------------------------------------------------------------------ where a body is
    public static boolean nearCounter(Vec3 r){return r.x>=STOOL_X-1.5&&r.x<=COUNTER_X&&r.z<=COUNTER_S+1&&r.z>=COUNTER_N-1;}
    public static boolean atFront(Vec3 r){return r.z>-4.5;}
    public static boolean inBar(Vec3 r){return r.x>WEST&&r.x<EAST&&r.z<.5&&r.z>NORTH&&r.y>-1&&r.y<TOP;}
    public static boolean atBooth(Vec3 r,int booth){return r.x<-7.5&&Math.abs(r.z-(BOOTHS[booth]+.5))<2.2;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState road(RoadhouseBlock.Kind kind,Direction facing){return RoadhouseBlock.of(kind,facing);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    public static BlockState wall(){return Blocks.SPRUCE_PLANKS.defaultBlockState();}
    public static BlockState floor(){return Blocks.DARK_OAK_PLANKS.defaultBlockState();}
    public static BlockState ceiling(){return Blocks.DARK_OAK_PLANKS.defaultBlockState();}

    public static void build(ServerLevel l,BlockPos b){
        // The shell: dark boards underfoot, spruce walls over a dark wainscot, a dark ceiling on beams.
        box(l,b,WEST,-1,NORTH,EAST,TOP,0,wall());
        box(l,b,WEST+1,0,NORTH+1,EAST-1,TOP-1,-1,AIR);
        box(l,b,WEST+1,-1,NORTH+1,EAST-1,-1,-1,floor());
        box(l,b,WEST+1,TOP,NORTH+1,EAST-1,TOP,-1,ceiling());
        for(int z=NORTH+1;z<=-1;z++){set(l,b,WEST,0,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());set(l,b,WEST,1,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());set(l,b,EAST,0,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());set(l,b,EAST,1,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());}
        for(int x=WEST+1;x<=EAST-1;x++){set(l,b,x,0,NORTH,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());set(l,b,x,1,NORTH,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());}
        for(int z=-4;z>=NORTH+2;z-=4)box(l,b,WEST+1,TOP-1,z,EAST-1,TOP-1,z,Blocks.DARK_OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X));
        bar(l,b);booths(l,b);pool(l,b);darts(l,b);window(l,b);
        // The house rules by the door, and the clock over it.
        set(l,b,RULES,road(RoadhouseBlock.Kind.CORKBOARD,Direction.NORTH));
        set(l,b,CLOCK,io.github.knaitoe.theoldesthouse.house.SceneDetailBlock.state(io.github.knaitoe.theoldesthouse.house.SceneDetailBlock.Kind.CLOCK,Direction.NORTH));
        // A few lamps, low: the bar is dark between them.
        for(var at:new BlockPos[]{new BlockPos(6,TOP-2,-9),new BlockPos(6,TOP-2,-15),new BlockPos(-9,TOP-2,-8),new BlockPos(-9,TOP-2,-16),new BlockPos(-9,TOP-2,-24),new BlockPos(2,TOP-2,-27),new BlockPos(0,TOP-2,-4)})hang(l,b,at);
    }
    /** A lantern hung on a short chain from the ceiling. */
    static void hang(ServerLevel l,BlockPos b,BlockPos at){
        for(int y=at.getY()+1;y<TOP;y++)set(l,b,at.getX(),y,at.getZ(),Blocks.CHAIN.defaultBlockState());
        set(l,b,at,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    static void bar(ServerLevel l,BlockPos b){
        for(int z=COUNTER_S;z>=COUNTER_N;z--){
            var kind=z==COUNTER_S?RoadhouseBlock.Kind.REGISTER:z==-10||z==-14?RoadhouseBlock.Kind.TAPS:z==-12?RoadhouseBlock.Kind.GLASSES:RoadhouseBlock.Kind.COUNTER;
            set(l,b,COUNTER_X,0,z,road(RoadhouseBlock.Kind.COUNTER,Direction.WEST));
            // The till faces whoever works the bar; the taps and glasses face the room.
            if(kind!=RoadhouseBlock.Kind.COUNTER)set(l,b,COUNTER_X,1,z,road(kind,kind==RoadhouseBlock.Kind.REGISTER?Direction.EAST:Direction.WEST));
            set(l,b,BACK_X,0,z,road(RoadhouseBlock.Kind.BACK_BAR,Direction.WEST));
            set(l,b,BACK_X,1,z,road(RoadhouseBlock.Kind.SHELF,Direction.WEST));
            set(l,b,BACK_X,2,z,(z&1)==0?road(RoadhouseBlock.Kind.MIRROR,Direction.WEST):road(RoadhouseBlock.Kind.SHELF,Direction.WEST));
            if((z-COUNTER_S)%2==0&&z<COUNTER_S)set(l,b,STOOL_X,0,z,road(RoadhouseBlock.Kind.STOOL,Direction.EAST));
        }
        // The counter's end turns back to the wall at the far end; the flap by the till lets staff in.
        set(l,b,COUNTER_X+1,0,COUNTER_N,road(RoadhouseBlock.Kind.COUNTER,Direction.NORTH));
        set(l,b,COUNTER_X+2,0,COUNTER_N,road(RoadhouseBlock.Kind.COUNTER,Direction.NORTH));
        set(l,b,BACK_X,3,-11,road(RoadhouseBlock.Kind.NEON_BEER,Direction.WEST));
    }
    static void booths(ServerLevel l,BlockPos b){
        for(int i=0;i<BOOTHS.length;i++){
            int c=BOOTHS[i];
            for(int x=-11;x<=-10;x++){
                set(l,b,x,0,c,road(RoadhouseBlock.Kind.BOOTH_TABLE,Direction.NORTH));
                set(l,b,x,0,c+1,road(RoadhouseBlock.Kind.BOOTH,Direction.NORTH));
                set(l,b,x,0,c-1,road(RoadhouseBlock.Kind.BOOTH,Direction.SOUTH));
            }
            // A high partition between booths, open above head height.
            for(int x=-11;x<=-9;x++){set(l,b,x,0,c+2,Blocks.DARK_OAK_PLANKS.defaultBlockState());set(l,b,x,1,c+2,Blocks.DARK_OAK_FENCE.defaultBlockState());}
            NovelRooms.sign(l,b.offset(-9,1,c+2).south(0),Direction.EAST,new String[]{"",String.valueOf(i+1),"",""});
        }
        int last=BOOTHS[BOOTHS.length-1]-2;
        for(int x=-11;x<=-9;x++){set(l,b,x,0,last,Blocks.DARK_OAK_PLANKS.defaultBlockState());set(l,b,x,1,last,Blocks.DARK_OAK_FENCE.defaultBlockState());}
        // A small lamp on each table.
        for(int c:BOOTHS)set(l,b,-11,1,c,Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT,true));
    }
    static void pool(ServerLevel l,BlockPos b){
        PoolTableBlock.place(l,b.offset(TABLE),Direction.NORTH);
        // One lamp over the cloth, low.
        for(int z=TABLE.getZ();z>=TABLE.getZ()-1;z--){for(int y=4;y<TOP;y++)set(l,b,TABLE.getX(),y,z,Blocks.CHAIN.defaultBlockState());set(l,b,TABLE.getX(),3,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
        // The cue rack on the west side.
        set(l,b,-6,1,-12,Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,-6,0,-12,Blocks.DARK_OAK_FENCE.defaultBlockState());
    }
    static void darts(ServerLevel l,BlockPos b){
        set(l,b,BOARD,BarRegistry.DARTBOARD.get().defaultBlockState().setValue(DartboardBlock.FACING,Direction.SOUTH));
        // The oche: a line of darker board in the floor.
        for(int x=BOARD.getX()-1;x<=BOARD.getX()+1;x++)set(l,b,x,-1,OCHE_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X));
        NovelRooms.sign(l,b.offset(LEAGUE_HEAD),Direction.SOUTH,new String[]{"","LEAGUE","",""});
        for(int i=0;i<LEAGUE.length;i++)NovelRooms.sign(l,b.offset(LEAGUE[i]),Direction.SOUTH,new String[]{"","","",""});
    }
    /** The window: three panes onto a scrap of street, which the bar keeps in one season or another. */
    static void window(ServerLevel l,BlockPos b){
        for(int z=WINDOW_Z0;z<=WINDOW_Z1;z++)for(int y=1;y<=3;y++)set(l,b,EAST,y,z,Blocks.GLASS_PANE.defaultBlockState().setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,true).setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,true));
        season(l,b,0);
    }
    /** The street beyond the window in a season: 0 spring, 1 summer, 2 autumn, 3 winter. */
    public static void season(ServerLevel l,BlockPos b,int s){
        int k=Math.floorMod(s,4);
        var sky=(switch(k){case 0,1->Blocks.LIGHT_BLUE_CONCRETE;case 2->Blocks.LIGHT_GRAY_CONCRETE;default->Blocks.WHITE_CONCRETE;}).defaultBlockState();
        var ground=(switch(k){case 0,1->Blocks.GRASS_BLOCK;case 2->Blocks.PODZOL;default->Blocks.SNOW_BLOCK;}).defaultBlockState();
        var crown=switch(k){case 0->Blocks.CHERRY_LEAVES.defaultBlockState();case 1->Blocks.OAK_LEAVES.defaultBlockState();case 2->Blocks.ORANGE_WOOL.defaultBlockState();default->AIR;};
        if(crown.hasProperty(net.minecraft.world.level.block.LeavesBlock.PERSISTENT))crown=crown.setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true);
        int x0=EAST+1,x1=EAST+3,zc=(WINDOW_Z0+WINDOW_Z1)/2;
        box(l,b,x0,-1,WINDOW_Z0-1,x1,4,WINDOW_Z1+1,sky);
        box(l,b,x0,0,WINDOW_Z0,x1-1,3,WINDOW_Z1,AIR);
        box(l,b,x0,-1,WINDOW_Z0,x1-1,-1,WINDOW_Z1,ground);
        set(l,b,x0+1,0,zc,Blocks.SPRUCE_LOG.defaultBlockState());set(l,b,x0+1,1,zc,Blocks.SPRUCE_LOG.defaultBlockState());
        set(l,b,x0+1,2,zc,k==3?Blocks.SPRUCE_LOG.defaultBlockState():crown);
        set(l,b,x0+1,2,zc-1,crown);set(l,b,x0+1,2,zc+1,crown);set(l,b,x0+1,3,zc,crown);
        if(k==3){set(l,b,x0+1,3,zc,Blocks.SNOW.defaultBlockState());set(l,b,x0,0,WINDOW_Z0,Blocks.SNOW.defaultBlockState());}
        // Daylight out there, whatever the hour in here.
        set(l,b,x0,3,WINDOW_Z1,Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL,15));
    }
}
