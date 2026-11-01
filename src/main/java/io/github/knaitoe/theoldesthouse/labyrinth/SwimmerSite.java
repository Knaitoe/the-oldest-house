package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SwimmerBlock.Kind.*;

/**
 * 0.4.227, the last swimmer (docs/LAST_SWIMMER_0_4_227.md): a small municipal pool at closing time, built whole. The reader comes
 * in at the front (the base) into the lobby (x -4 to 4, z 0 to -5), with the reception counter on the left and the closing
 * checklist on it; the attendant's office behind the counter (x -11 to -6, z -1 to -8) has a window onto the pool hall. A short
 * corridor of lockers (z -7 to -8) leads into the hall (x -14 to 14, z -10 to -44, eight high), with the pool in the middle: six
 * lanes, 25 blocks long (z -13 to -37), shallow at the south end and deep at the north, a lane rope between each lane. The cover's
 * reel runs along the east deck; the pace clock is on the north wall; a skylight is over the middle of the pool. The fire door in
 * the west wall at the south end is the way on. Every position is relative to the base.
 */
public final class SwimmerSite {
    private SwimmerSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SWIMMER;
    /** The water's surface row, and the pool's ends and sides (inclusive). */
    public static final int SURFACE=-1,POOL_X0=-8,POOL_X1=8,POOL_NORTH=-37,POOL_SOUTH=-13;
    /** The hall's interior. */
    public static final int HALL_X=14,HALL_SOUTH=-10,HALL_NORTH=-44,HALL_HIGH=8;
    public static final int LANES=6,COLUMNS=POOL_X1-POOL_X0+1;
    /** The reel along the east deck, and the cover's south strap where the reader hauls. */
    public static final int REEL_X=11;
    public static final BlockPos REEL_SOUTH=new BlockPos(REEL_X,0,POOL_SOUTH+1),REEL_NORTH=new BlockPos(REEL_X,0,POOL_NORTH-1);
    /** The closing checklist on the reception counter (the source), the closing log beside it (the account, also handed over at the door), the office radio, the pace clock. */
    public static final BlockPos CHECKLIST=new BlockPos(-4,1,-3),LOG=new BlockPos(-4,1,-4),RADIO=new BlockPos(-10,1,-5),PACE=new BlockPos(0,3,HALL_NORTH);
    /** The fire door in the west wall (the way on), and where the attendant stands holding it. */
    public static final int EXIT_X=-15,EXIT_Z=-11;
    public static final BlockPos HOLD=new BlockPos(-14,0,-12);
    /** Where the attendant waits at first: the north end of the reel. */
    public static final BlockPos ATTENDANT_START=new BlockPos(REEL_X-1,0,POOL_NORTH-2);
    /** The hall's lamps, north first: the order they go out in. */
    public static final List<BlockPos> LAMPS=lamps();
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** The west column of lane i (0, the west lane, to 5); a lane is two columns wide. */
    public static int laneX(int lane){return POOL_X0+3*lane;}
    /** The middle of lane i, between its two columns, as a block-space x. */
    public static double laneMiddle(int lane){return laneX(lane)+1.0;}
    /** The column the cover's k-th haul reaches: it runs out from the east side, one column a haul. */
    public static int coverColumn(int k){return POOL_X1-k;}
    public static boolean rope(int x){return x>POOL_X0&&x<POOL_X1&&Math.floorMod(x-POOL_X0,3)==2;}
    /** The pool's floor under a row: two deep at the shallow end, four at the deep. */
    public static int floor(int z){return z>=-20?-3:z>=-28?-4:-5;}
    public static boolean inPool(BlockPos rel){return rel.getX()>=POOL_X0&&rel.getX()<=POOL_X1&&rel.getZ()>=POOL_NORTH&&rel.getZ()<=POOL_SOUTH;}
    public static boolean inHall(Vec3 r){return r.x>=-HALL_X&&r.x<HALL_X+1&&r.z>=HALL_NORTH&&r.z<HALL_SOUTH+1&&r.y>=-6&&r.y<HALL_HIGH;}
    public static boolean inside(Vec3 r){return r.x>=-HALL_X&&r.x<HALL_X+1&&r.z>=HALL_NORTH&&r.z<1&&r.y>=-6&&r.y<HALL_HIGH;}
    /** The deck at the pool's south end, where the reader hauls the cover. */
    public static boolean southDeck(Vec3 r){return r.z>=POOL_SOUTH+1&&r.z<HALL_SOUTH+1&&r.x>=-HALL_X&&r.x<HALL_X+1;}

    private static List<BlockPos> lamps(){
        var out=new ArrayList<BlockPos>();
        for(int z:new int[]{-42,-34,-24,-14})for(int x:new int[]{-11,11})out.add(new BlockPos(x,HALL_HIGH-1,z));
        for(int z:new int[]{-32,-16})for(int x:new int[]{-5,5})out.add(new BlockPos(x,HALL_HIGH-1,z));
        out.sort((a,b)->Integer.compare(a.getZ(),b.getZ()));
        return List.copyOf(out);
    }
    static BlockState mat(String name){return SwimmerRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(l,b,x,y,z,s);}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var water=Blocks.WATER.defaultBlockState();
        // The shell: blockwork round the front rooms, tile round the hall.
        box(l,b,-15,-7,-45,15,10,0,mat("blockwork"));
        // The lobby, with the reception counter on the left and a bench on the right.
        box(l,b,-4,0,-5,4,3,0,air);box(l,b,-4,-1,-5,4,-1,0,mat("lino"));box(l,b,-4,4,-5,4,4,0,mat("ceiling"));
        for(int z=-4;z<=-2;z++)set(l,b,-4,0,z,mat("blockwork"));
        set(l,b,CHECKLIST,SwimmerBlock.of(CLIPBOARD,Direction.EAST));set(l,b,LOG,SwimmerBlock.of(CLIPBOARD,Direction.EAST));
        set(l,b,3,0,-3,SwimmerBlock.of(BENCH,Direction.WEST));
        set(l,b,0,2,-5,SwimmerBlock.of(SIGN,Direction.SOUTH,1));
        set(l,b,0,3,-2,SwimmerBlock.of(LAMP,Direction.NORTH));
        // The office behind the counter: a desk with the radio on it, a chair, a window onto the hall, a window onto the lobby.
        box(l,b,-11,0,-8,-6,3,-1,air);box(l,b,-11,-1,-8,-6,-1,-1,mat("lino"));box(l,b,-11,4,-8,-6,4,-1,mat("ceiling"));
        set(l,b,-5,0,-2,air);set(l,b,-5,1,-2,air);
        set(l,b,-5,1,-4,Blocks.GLASS_PANE.defaultBlockState());
        set(l,b,-10,0,-5,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH));
        set(l,b,RADIO,SwimmerBlock.of(SwimmerBlock.Kind.RADIO,Direction.SOUTH,1));
        set(l,b,-10,0,-4,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH));
        set(l,b,-7,3,-4,SwimmerBlock.of(LAMP,Direction.NORTH));
        for(int x=-11;x<=-7;x++)for(int y=1;y<=2;y++)set(l,b,x,y,-9,Blocks.GLASS_PANE.defaultBlockState());
        // The corridor of lockers between the lobby and the hall.
        set(l,b,0,0,-6,air);set(l,b,0,1,-6,air);
        box(l,b,-4,0,-8,4,2,-7,air);box(l,b,-4,-1,-8,4,-1,-7,mat("lino"));box(l,b,-4,3,-8,4,3,-7,mat("wall_tile"));
        for(int x:new int[]{-4,-3,-2,2,3,4})for(int y=0;y<=1;y++)set(l,b,x,y,-8,SwimmerBlock.of(LOCKERS,Direction.SOUTH,x==3&&y==0?1:0));
        // The hall: tile walls with a band, the ceiling, and the deck.
        box(l,b,-HALL_X,0,HALL_NORTH,HALL_X,HALL_HIGH-1,HALL_SOUTH,air);
        box(l,b,-HALL_X,-1,HALL_NORTH,HALL_X,-1,HALL_SOUTH,mat("deck"));box(l,b,-HALL_X,HALL_HIGH,HALL_NORTH,HALL_X,HALL_HIGH,HALL_SOUTH,mat("ceiling"));
        for(int x=-15;x<=15;x++)for(int y=0;y<HALL_HIGH;y++){set(l,b,x,y,HALL_NORTH-1,mat(y==2?"wall_band":"wall_tile"));if(x<-11||x>-7||y<1||y>2)set(l,b,x,y,HALL_SOUTH+1,mat(y==2?"wall_band":"wall_tile"));}
        for(int z=HALL_NORTH;z<=HALL_SOUTH;z++)for(int y=0;y<HALL_HIGH;y++){set(l,b,-15,y,z,mat(y==2?"wall_band":"wall_tile"));set(l,b,15,y,z,mat(y==2?"wall_band":"wall_tile"));}
        set(l,b,0,0,HALL_SOUTH+1,air);set(l,b,0,1,HALL_SOUTH+1,air);
        // The skylight over the middle of the pool: a square of night above the water.
        box(l,b,-3,HALL_HIGH,-27,3,HALL_HIGH,-21,Blocks.GLASS.defaultBlockState());box(l,b,-3,HALL_HIGH+1,-27,3,HALL_HIGH+1,-21,air);box(l,b,-3,HALL_HIGH+2,-27,3,HALL_HIGH+2,-21,Blocks.BLACK_CONCRETE.defaultBlockState());
        // The pool: tile walls and floor, the water, a dark lane line down the middle of each lane, and the ropes.
        for(int z=POOL_NORTH-1;z<=POOL_SOUTH+1;z++)for(int x=POOL_X0-1;x<=POOL_X1+1;x++){
            boolean edge=z<POOL_NORTH||z>POOL_SOUTH||x<POOL_X0||x>POOL_X1;int f=floor(Math.max(POOL_NORTH,Math.min(POOL_SOUTH,z)));
            for(int y=-6;y<=SURFACE;y++){
                if(edge)set(l,b,x,y,z,mat("pool_tile"));
                else if(y<f)set(l,b,x,y,z,mat("pool_tile"));
                else if(y==f)set(l,b,x,y,z,mat(Math.floorMod(x-POOL_X0,3)==0&&z<POOL_SOUTH-1&&z>POOL_NORTH+1?"pool_line":"pool_tile"));
                else set(l,b,x,y,z,rope(x)&&y==SURFACE?SwimmerBlock.of(ROPE,Direction.NORTH):water);
            }
        }
        // Depth tiles on the deck at either end, both sides.
        for(int x:new int[]{-10,10}){set(l,b,x,-1,POOL_SOUTH,SwimmerBlock.of(DEPTH,Direction.NORTH,0));set(l,b,x,-1,POOL_NORTH,SwimmerBlock.of(DEPTH,Direction.NORTH,1));}
        // The starting blocks at the shallow end, one to a lane.
        for(int lane=0;lane<LANES;lane++)set(l,b,laneX(lane),0,POOL_SOUTH+2,SwimmerBlock.of(START,Direction.NORTH,lane));
        // The cover on its reel along the east deck, a stand at each end with the crank wheel.
        for(int z=POOL_NORTH;z<=POOL_SOUTH;z++)set(l,b,REEL_X,0,z,SwimmerBlock.of(REEL,Direction.EAST,0));
        set(l,b,REEL_SOUTH,SwimmerBlock.of(REEL_END,Direction.SOUTH));set(l,b,REEL_NORTH,SwimmerBlock.of(REEL_END,Direction.NORTH));
        // The north wall: the pace clock, and the two signs.
        set(l,b,PACE,SwimmerBlock.of(PACE_CLOCK,Direction.SOUTH,0));
        set(l,b,-6,3,HALL_NORTH,SwimmerBlock.of(SIGN,Direction.SOUTH,1));set(l,b,6,3,HALL_NORTH,SwimmerBlock.of(SIGN,Direction.SOUTH,0));
        // Rails at the deep end, benches on both decks, a towel on the west deck, kickboards in the north-east corner, drains.
        set(l,b,POOL_X0-1,0,POOL_NORTH+3,SwimmerBlock.of(LADDER,Direction.WEST));set(l,b,POOL_X1+1,0,POOL_NORTH+3,SwimmerBlock.of(LADDER,Direction.EAST));
        // (The west deck south of the benches stays clear: the attendant walks down it to the fire door.)
        for(int z=-28;z<=-26;z++)set(l,b,-HALL_X,0,z,SwimmerBlock.of(BENCH,Direction.EAST));
        for(int z=-32;z<=-30;z++)set(l,b,HALL_X,0,z,SwimmerBlock.of(BENCH,Direction.WEST));
        set(l,b,-12,0,-40,SwimmerBlock.of(TOWEL,Direction.NORTH));
        set(l,b,HALL_X,0,HALL_NORTH+2,SwimmerBlock.of(BOARDS,Direction.WEST));set(l,b,HALL_X-1,0,HALL_NORTH+1,SwimmerBlock.of(BOARDS,Direction.SOUTH));
        for(int x:new int[]{-12,12})set(l,b,x,0,-25,SwimmerBlock.of(DRAIN,Direction.NORTH));
        // The fire door's sign over the west wall's south end; the lamps under the ceiling.
        set(l,b,EXIT_X+1,2,EXIT_Z,SwimmerBlock.of(EXIT_SIGN,Direction.EAST));
        for(var at:LAMPS)set(l,b,at,SwimmerBlock.of(LAMP,Direction.NORTH,0));
        // Panes join the way they would have been fitted.
        BuildBlocks.after(l,()->{for(var at:List.of(new BlockPos(-5,1,-4)))join(l,b.offset(at));for(int x=-11;x<=-7;x++)for(int y=1;y<=2;y++)join(l,b.offset(x,y,-9));});
    }
    private static void join(ServerLevel l,BlockPos at){var s=l.getBlockState(at);if(!(s.getBlock() instanceof IronBarsBlock))return;for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,l.getBlockState(at.relative(d)),l,at,at.relative(d));l.setBlock(at,s,F);}

    /** Lays the cover out to {@code k} columns from the east side: covered columns flat, the k-th the leading edge; the rest open water and ropes. */
    public static void cover(ServerLevel l,BlockPos b,int k){
        for(int i=0;i<COLUMNS;i++){int x=coverColumn(i);
            for(int z=POOL_NORTH;z<=POOL_SOUTH;z++){
                var at=b.offset(x,SURFACE,z);var now=l.getBlockState(at);
                BlockState next=i<k?SwimmerBlock.of(COVER,Direction.WEST,i==k-1?2:0):rope(x)?SwimmerBlock.of(ROPE,Direction.NORTH):Blocks.WATER.defaultBlockState();
                if(i<k&&SwimmerBlock.is(now,COVER)&&now.getValue(SwimmerBlock.STAGE)==1)continue;
                if(!now.equals(next))l.setBlock(at,next,F);
            }
        }
        int reel=k<6?0:k<12?1:2;
        for(int z=POOL_NORTH;z<=POOL_SOUTH;z++){var at=b.offset(REEL_X,0,z);var s=l.getBlockState(at);if(SwimmerBlock.is(s,REEL)&&s.getValue(SwimmerBlock.STAGE)!=reel)l.setBlock(at,s.setValue(SwimmerBlock.STAGE,reel),F);}
    }
}
