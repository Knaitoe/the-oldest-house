package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * 0.4.489, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act three: the graveyard"): Calder Hill Cemetery, north of the town's railings,
 * built whole. Every position is relative to the park's base, as the town's are. This is its ground and what stands on it; what
 * happens there (the mist, the three falling back, the row) is another slice's.
 *
 * <p>The proposal: "It sits on a rise at the edge of town, inside an old iron fence. The paths are gravel. The obstacles: headstones
 * at every lean and height, and taller memorials: an angel; an obelisk; a family mausoleum with iron doors; a fresh open grave with a
 * mound and a tarp, which is a hazard to jump." And: "One row of small headstones near the back all carries the same autumn date,
 * years before, with names and nothing else."
 *
 * <p>The rise is three banks, each a row of half blocks with the ground a block higher behind it: anybody walks up them, and a bike
 * rides up them without slowing. The gravel is one path north from the gate, four across it (one to a level) and two up the sides.
 * The stones stand in rows across the hill, so that riding north is riding through them; the line of the gate's path is clear in
 * every row. The rows fill toward the back. The path ends at the newest grave, which is open: its heap is the ramp over it.
 *
 * <p>What is cut on a stone is said to a hand on it ({@link #inscription}): a stone's face is sixteen pixels and has room for an
 * emblem and the lines of a name, and for a year only on the low markers and the small ones.
 */
public final class CalderGraves {
    private CalderGraves(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The railings: the town's along the front, these down both sides and across the back. The ground goes on a little behind them. */
    public static final int W=CalderTown.W,E=CalderTown.E,FRONT=CalderTown.RAILS,BACK=-266,FAR=-272,TOP=17;
    /** The banks: each row is half blocks, and north of it the ground is a block higher. */
    public static final int[] BANKS={-190,-208,-226};
    /** The gate's path, as wide as the gate, and where it ends. */
    public static final int PATH_W=CalderTown.GATE_W,PATH_E=CalderTown.GATE_E,PATH_END=-243;
    /** The paths across (each is this row and the one north of it), how far they reach, and the two up the sides (each this and the next east). */
    public static final int[] ACROSS={-180,-200,-218,-236};
    public static final int ACROSS_W=-56,ACROSS_E=56;
    public static final int[] UP={-30,29};
    /** The rows of stones, and how many places in a hundred of each are empty. */
    public static final int[] ROWS={-183,-186,-194,-197,-204,-212,-215,-222,-230,-233,-240,-250};
    private static final int[] EMPTY={50,45,42,38,34,30,27,24,20,17,14,10};
    /** The row of small stones, and where its six stand: three on each side of the gate's line. */
    public static final int SMALL_ROW=-256;
    public static final int[] SMALL_X={-9,-6,-3,2,5,8};
    /** The angel and the obelisk (their bases), and the mausoleum: its walls, and its doors in the south one. */
    public static final BlockPos ANGEL=new BlockPos(-12,2,-216),OBELISK=new BlockPos(14,1,-198);
    public static final int TOMB_W=34,TOMB_E=40,TOMB_N=-246,TOMB_S=-241,TOMB_HIGH=4,DOOR_W=36,DOOR_E=37;
    /**
     * The open grave: its pit (two wide, two long, two deep), and the row its heap is in, south of it. Two long is a grave's length,
     * and it is what the heap throws a bike over: flat out, a plain bike leaves the heap's top and comes down a little under three
     * blocks on, which is over a pit of two and the bike's own half length with something to spare, and short of a pit of three.
     */
    public static final int PIT_W=-1,PIT_E=0,PIT_S=-245,PIT_N=-246,PIT_DEEP=2,HEAP=-244;
    private static final int[][] TREES={{-58,-184,6},{-52,-192,7},{-44,-206,6},{-57,-210,7},{-40,-224,6},{-55,-228,8},{-48,-244,7},{-58,-252,6},{-24,-188,6},{-20,-246,7},
        {56,-185,7},{50,-192,6},{42,-207,7},{57,-213,6},{46,-224,8},{54,-229,6},{52,-245,7},{58,-253,6},{22,-189,6},{18,-228,7},
        {-36,-262,7},{-12,-261,6},{12,-262,7},{34,-261,6},{-52,-262,8},{52,-262,7}};

    // ------------------------------------------------------------------------------------------------ where things are
    /** How many banks are south of a row: the block a body stands in there (on a bank's own row, half a block up it). */
    public static int level(int z){int n=0;for(int bank:BANKS)if(z<bank)n++;return n;}
    public static boolean bank(int z){for(int bank:BANKS)if(z==bank)return true;return false;}
    public static boolean inside(int x,int z){return x>W&&x<E&&z<FRONT&&z>BACK;}
    /** Whether a place is gravel. */
    public static boolean path(int x,int z){
        if(!inside(x,z))return false;
        if(x>=PATH_W&&x<=PATH_E&&z>=PATH_END)return true;
        for(int row:ACROSS)if((z==row||z==row-1)&&x>=ACROSS_W&&x<=ACROSS_E)return true;
        for(int side:UP)if((x==side||x==side+1)&&z<=ACROSS[0]&&z>=ACROSS[ACROSS.length-1]-1)return true;
        // The spur from the last path across to the mausoleum's doors.
        return x>=DOOR_W&&x<=DOOR_E&&z<ACROSS[ACROSS.length-1]-1&&z>TOMB_S;
    }
    /** Whether a place is kept for something that is not a row's stone: the gate's line, a memorial and the room round it, the grave, a tree. */
    public static boolean kept(int x,int z){
        if(x>=PATH_W&&x<=PATH_E)return true;
        if(Math.abs(x-ANGEL.getX())<=1&&Math.abs(z-ANGEL.getZ())<=1||Math.abs(x-OBELISK.getX())<=1&&Math.abs(z-OBELISK.getZ())<=1)return true;
        if(x>=TOMB_W-2&&x<=TOMB_E+2&&z>=TOMB_N-2&&z<=TOMB_S+1)return true;
        if(x>=PIT_W-2&&x<=PIT_E+2&&z>=PIT_N-3&&z<=HEAP+1)return true;
        for(int[] t:TREES)if(t[0]==x&&t[1]==z)return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ small things
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        NovelRooms.box(l,b,Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1),Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1),s);
    }
    private static boolean free(ServerLevel l,BlockPos b,int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z)).isAir();}
    private static BlockState mat(String name){return BikeNightSite.mat(name);}
    private static BlockState grave(CalderGraveBlock.Kind kind,Direction facing,int stage){return CalderGraveBlock.of(kind,facing,stage);}
    /** Something of the cemetery's, where there is nothing: a thing that would take the place of another is a mistake in the builder, and says so. */
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,CalderGraveBlock.Kind kind,Direction facing,int stage){
        if(!free(l,b,x,y,z))throw new IllegalStateException("the "+kind+" at "+x+","+y+","+z+" would stand where "+BuildBlocks.state(l,b.offset(x,y,z))+" is");
        set(l,b,x,y,z,grave(kind,facing,stage));
    }
    /** A brick pier under its cap, standing on the ground at {@code y}: three high, and a block more where it spans a bank. */
    private static void pier(ServerLevel l,BlockPos b,int x,int z){
        int y=level(z),high=bank(z)?3:2;
        box(l,b,x,y,z,x,y+high-1,z,mat("brick"));set(l,b,x,y+high,z,BikeNightSite.fit(CalderBlock.Kind.CAP,Direction.NORTH,0));
    }
    /** One block of railings, two high, across the way it faces, on the ground where it is. */
    private static void rail(ServerLevel l,BlockPos b,int x,int z,Direction facing){
        int y=level(z);set(l,b,x,y,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,0));set(l,b,x,y+1,z,BikeNightSite.fit(CalderBlock.Kind.FENCE,facing,1));
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int height,Random rnd){
        int y0=level(z);
        for(int y=0;y<height;y++)set(l,b,x,y0+y,z,Blocks.OAK_LOG.defaultBlockState());
        int top=y0+height-1;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-1;dy<=3;dy++){
            double d=Math.sqrt(dx*dx+dz*dz+(dy-1)*(dy-1)*1.6);
            if(d>3.1||(dx==0&&dz==0&&dy<=0))continue;
            if(d>2.2&&rnd.nextInt(3)==0)continue;
            if(rnd.nextInt(9)==0)continue;
            if(top+dy<=TOP&&Math.abs(x+dx)<=E&&z+dz>=FAR&&free(l,b,x+dx,top+dy,z+dz))set(l,b,x+dx,top+dy,z+dz,BikeNightSite.leaves());
        }
    }

    // ------------------------------------------------------------------------------------------------ the building
    public static void build(ServerLevel l,BlockPos b){
        var rnd=new Random(48900);
        ground(l,b);
        railings(l,b);
        memorials(l,b);
        openGrave(l,b);
        rows(l,b,rnd);
        for(int[] t:TREES){
            if(path(t[0],t[1])||bank(t[1]))throw new IllegalStateException("a tree at "+t[0]+","+t[1]+" would stand on a path or a bank");
            for(int row:ROWS)if(row==t[1])throw new IllegalStateException("a tree at "+t[0]+","+t[1]+" would stand in a row of stones");
            tree(l,b,t[0],t[1],t[2],rnd);
        }
        // The dark behind the back railings.
        for(int x=W+2;x<=E-2;x+=4+rnd.nextInt(3))tree(l,b,x,BACK-2-rnd.nextInt(4),6+rnd.nextInt(3),rnd);
    }
    /** The hill: earth under it, the cemetery's grass over it and gravel on the paths, with a row of half blocks up each bank. */
    static void ground(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var dirt=Blocks.DIRT.defaultBlockState();var turf=mat("grave_turf");var gravel=mat("gravel");
        var turfStep=BikeNightRegistry.GRAVE_TURF_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
        var gravelStep=BikeNightRegistry.GRAVEL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
        // North of what the town cleared for itself.
        box(l,b,W,-4,FAR,E,TOP,CalderTown.N-1,air);
        for(int z=FAR;z<FRONT;z++){
            int y=level(z);
            box(l,b,W,-4,z,E,y-2,z,dirt);
            for(int x=W;x<=E;x++){
                boolean walk=path(x,z);
                set(l,b,x,y-1,z,walk?gravel:turf);
                if(bank(z))set(l,b,x,y,z,walk?gravelStep:turfStep);
            }
        }
    }
    /**
     * The fence, joined all the way round ("which must be connected properly"): the town's front railings end in a pier at each corner
     * and these start from it; a pier stands on every bank, tall enough to meet the rails below it and above; and the back has its
     * own corners, and a gate between two piers in the gate's line, shut.
     */
    static void railings(ServerLevel l,BlockPos b){
        for(int z=FRONT-1;z>BACK;z--)for(int x:new int[]{W,E}){
            if(bank(z)||(FRONT-z)%9==0)pier(l,b,x,z);else rail(l,b,x,z,Direction.EAST);
        }
        int pw=PATH_W-1,pe=PATH_E+1,y=level(BACK);
        for(int x=W;x<=E;x++){
            if(x>=PATH_W&&x<=PATH_E){
                var facing=(x-PATH_W)%2==0?Direction.NORTH:Direction.SOUTH;
                set(l,b,x,y,BACK,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,0));set(l,b,x,y+1,BACK,BikeNightSite.fit(CalderBlock.Kind.GATE,facing,1));
            }else if(x==pw||x==pe||x==W||x==E||(x<pw&&(pw-x)%7==0)||(x>pe&&(x-pe)%7==0))pier(l,b,x,BACK);
            else rail(l,b,x,BACK,Direction.NORTH);
        }
    }
    /** The angel, the obelisk and the Calders' mausoleum. */
    static void memorials(ServerLevel l,BlockPos b){
        for(int i=0;i<3;i++){
            put(l,b,ANGEL.getX(),ANGEL.getY()+i,ANGEL.getZ(),CalderGraveBlock.Kind.ANGEL,Direction.SOUTH,i);
            put(l,b,OBELISK.getX(),OBELISK.getY()+i,OBELISK.getZ(),CalderGraveBlock.Kind.OBELISK,Direction.SOUTH,i);
        }
        var marble=mat("marble");int y=level(TOMB_S),top=y+TOMB_HIGH;
        // Solid: nobody goes in. Its roof oversails the walls and steps up to a ridge from front to back.
        box(l,b,TOMB_W,y,TOMB_N,TOMB_E,top-1,TOMB_S,marble);
        box(l,b,TOMB_W-1,top,TOMB_N-1,TOMB_E+1,top,TOMB_S+1,marble);
        box(l,b,TOMB_W+1,top+1,TOMB_N-1,TOMB_E-1,top+1,TOMB_S+1,marble);
        box(l,b,TOMB_W+3,top+2,TOMB_N-1,TOMB_E-3,top+2,TOMB_S+1,marble);
        // The doors, set back in the south wall: two leaves, two high. As somebody facing them sees them, the west leaf is the left.
        for(int i=0;i<4;i++)set(l,b,i%2==0?DOOR_W:DOOR_E,y+i/2,TOMB_S,grave(CalderGraveBlock.Kind.DOOR,Direction.SOUTH,i));
        put(l,b,DOOR_W-1,y,TOMB_S+1,CalderGraveBlock.Kind.GOODS,Direction.SOUTH,CalderGraveBlock.WREATH);
        put(l,b,ANGEL.getX(),ANGEL.getY(),ANGEL.getZ()+1,CalderGraveBlock.Kind.GOODS,Direction.SOUTH,CalderGraveBlock.FLOWERS);
    }
    /**
     * The newest grave, where the gate's path ends: dug and not filled. Its heap is at its south end and is ridden up (the proposal:
     * "the mound is the ramp"); a bike going fast enough clears the pit, and one that is not goes in. A step of earth is left at the
     * north end, which a bike climbs and anybody jumps ("falling in costs time and nothing else"). A tarpaulin over the rest of the
     * earth on one side, and one pegged out flat on the other.
     */
    static void openGrave(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var earth=mat("grave_earth");int y=level(PIT_S);
        for(int x=PIT_W;x<=PIT_E;x++){
            for(int z=PIT_N;z<=PIT_S;z++){box(l,b,x,y-PIT_DEEP,z,x,y-1,z,air);set(l,b,x,y-PIT_DEEP-1,z,earth);}
            set(l,b,x,y-PIT_DEEP,PIT_N,earth);
            set(l,b,x,y-1,HEAP,earth);
            put(l,b,x,y,HEAP,CalderGraveBlock.Kind.MOUND,Direction.NORTH,0);
        }
        // The pit's sides are cut earth, not turf.
        for(int z=PIT_N;z<=PIT_S;z++)for(int x:new int[]{PIT_W-1,PIT_E+1})set(l,b,x,y-2,z,earth);
        put(l,b,PIT_E+1,y,PIT_S-1,CalderGraveBlock.Kind.TARP,Direction.NORTH,CalderGraveBlock.HEAPED);
        put(l,b,PIT_W-1,y,PIT_S-1,CalderGraveBlock.Kind.TARP,Direction.NORTH,0);
    }
    /** The rows, and the small row behind them. A place a row would have and has not is a gap to ride through. */
    static void rows(ServerLevel l,BlockPos b,Random rnd){
        for(int r=0;r<ROWS.length;r++){
            int z=ROWS[r],y=level(z);
            for(int x=W+3+rnd.nextInt(2);x<=E-3;x+=2+rnd.nextInt(3)){
                if(kept(x,z)||path(x,z)||rnd.nextInt(100)<EMPTY[r])continue;
                if(rnd.nextInt(11)==0)put(l,b,x,y,z,CalderGraveBlock.Kind.CROSS,Direction.SOUTH,rnd.nextInt(3)==0?1:0);
                else put(l,b,x,y,z,CalderGraveBlock.Kind.STONE,Direction.SOUTH,rnd.nextInt(CalderGraveBlock.STONES));
                // Something left at it, on the side its name is.
                if(rnd.nextInt(12)==0&&!path(x,z+1)&&!bank(z+1)&&free(l,b,x,level(z+1),z+1)){
                    int what=rnd.nextInt(20);
                    put(l,b,x,level(z+1),z+1,CalderGraveBlock.Kind.GOODS,Direction.SOUTH,what<10?CalderGraveBlock.FLOWERS:what<14?CalderGraveBlock.WREATH:what<17?CalderGraveBlock.FLAG:CalderGraveBlock.LANTERN);
                }
            }
        }
        for(int i=0;i<SMALL_X.length;i++)put(l,b,SMALL_X[i],level(SMALL_ROW),SMALL_ROW,CalderGraveBlock.Kind.SMALL,Direction.SOUTH,i);
    }

    // ------------------------------------------------------------------------------------------------ what is cut on them
    private static int mix(int x,int z){int h=x*73856093^z*19349663;h^=h>>>13;h*=0x5bd1e995;h^=h>>>15;return h&0x7fffffff;}
    /**
     * What is cut on the thing at a place, as a hand on it is told. A headstone's is made from where it stands, so the same stone says
     * the same thing in every copy of the town and to everybody: a family's name by the plot (six places of a row are one plot), a
     * given name, two years, and for some a line under them. A low marker's last year is the one on its face. Nobody under a stone
     * here died young: the only children in this ground are the row's, and theirs say what the proposal says they say.
     */
    public static String inscription(BlockState s,BlockPos rel){
        int stage=s.getValue(CalderGraveBlock.STAGE);
        switch(s.getValue(CalderGraveBlock.KIND)){
            case SMALL:return BikeNightTexts.SMALL_NAMES[stage%BikeNightTexts.SMALL_NAMES.length]+". "+BikeNightTexts.SMALL_DATE+".";
            case ANGEL:return BikeNightTexts.ANGEL_STONE;
            case OBELISK:return BikeNightTexts.OBELISK_STONE;
            case DOOR:return BikeNightTexts.TOMB_DOORS;
            default:break;
        }
        int h=mix(rel.getX(),rel.getZ()),plot=mix(Math.floorDiv(rel.getX(),6),rel.getZ());
        String name=BikeNightTexts.GIVEN[h%BikeNightTexts.GIVEN.length]+" "+BikeNightTexts.FAMILIES[plot%BikeNightTexts.FAMILIES.length];
        boolean low=CalderGraveBlock.low(s);
        int lived=21+(h>>>5)%64,died=low&&stage>=CalderGraveBlock.LOW?BikeNightTexts.MARKER_YEARS[stage-CalderGraveBlock.LOW]:BikeNightTexts.FIRST_YEAR+(h>>>11)%(BikeNightTexts.LAST_YEAR-BikeNightTexts.FIRST_YEAR+1),born=died-lived;
        // The row's year is the row's: nobody else here was born or died in it.
        if(!low&&died==BikeNightTexts.SMALL_YEAR)died--;
        if(died-lived==BikeNightTexts.SMALL_YEAR)lived++;
        born=died-lived;
        // A wooden cross has a name and a year in paint.
        if(CalderGraveBlock.is(s,CalderGraveBlock.Kind.CROSS)&&stage==1)return name+". "+died+".";
        int line=(h>>>17)%(BikeNightTexts.EPITAPHS.length*2);
        return name+". "+born+"-"+died+"."+(line<BikeNightTexts.EPITAPHS.length?" "+BikeNightTexts.EPITAPHS[line]+".":"");
    }
}
