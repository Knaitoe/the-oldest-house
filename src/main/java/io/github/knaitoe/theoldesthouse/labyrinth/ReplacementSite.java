package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.ReplacementBlock.Kind.*;

/**
 * 0.4.218, the replacement (docs/REPLACEMENT_0_4_218.md): a residential street at midnight, under the outside sky. The reader comes
 * out of the front door of a house at the street's south end (the entry, at the base), boarded from the outside like the rest. The
 * road runs north between sidewalks, three houses either side, every window and door boarded over, a telephone pole with the
 * posters on it by the first, street lamps, and at the far end a turning circle and the one house that isn't boarded: its door
 * open, its curtains filthy. Inside it: the front room, the kitchen and the back bedroom, and the crate in the bedroom's far corner.
 * Its kitchen's back door is the way on. A hedge closes the street in. Every position is relative to the base.
 */
public final class ReplacementSite {
    private ReplacementSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.REPLACEMENT;
    public static final int SIDE=17,FAR=-63;
    /** The poster on the telephone pole (the source) and the mailbox by the house the reader came out of (the account). */
    public static final BlockPos POSTER=new BlockPos(4,1,-6),MAILBOX=new BlockPos(2,0,-3);
    /** The open house: its front door, and the crate in the back bedroom with the pen behind its door. */
    public static final BlockPos HOUSE_DOOR=new BlockPos(0,0,-51),CRATE=new BlockPos(5,0,-61),PEN=new BlockPos(6,0,-61);
    public static final int HOUSE_FRONT=-51,HOUSE_BACK=-62,HOUSE_SIDE=7;
    public static final int EXIT_X=-3,EXIT_Z=HOUSE_BACK;
    /** Where the replacement lies down when it is done following: the blanket in the front room. */
    public static final BlockPos LIES_DOWN=new BlockPos(-1,0,-55);
    static final int[][] LOTS={{-9,-15},{-21,-27},{-33,-39}};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static BlockState mat(String name){return ReplacementRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    /** Inside the open house: its three rooms. */
    public static boolean inHouse(Vec3 r){return r.z<HOUSE_FRONT&&r.z>=HOUSE_BACK+1&&Math.abs(r.x)<HOUSE_SIDE&&r.y>-.5&&r.y<4;}
    public static boolean inside(Vec3 r){return r.x>-SIDE&&r.x<SIDE+1&&r.z>FAR&&r.z<1&&r.y>-1.5&&r.y<10;}
    /** Back out on the street, past the open house's front door. */
    public static boolean street(Vec3 r){return r.z>=HOUSE_FRONT+1&&inside(r);}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,-SIDE,-2,FAR,SIDE,12,0,air);
        box(l,b,-SIDE,-2,FAR,SIDE,-2,0,Blocks.DIRT.defaultBlockState());box(l,b,-SIDE,-1,FAR,SIDE,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        // The road, its sidewalks, the turning circle at the end, and the path from the reader's door.
        box(l,b,-3,-1,-48,3,-1,-4,mat("asphalt"));box(l,b,-7,-1,-50,7,-1,-44,mat("asphalt"));
        for(int x:new int[]{-5,-4,4,5})box(l,b,x,-1,-43,x,-1,-4,Blocks.SMOOTH_STONE.defaultBlockState());
        box(l,b,-1,-1,-3,1,-1,-1,Blocks.SMOOTH_STONE.defaultBlockState());
        // The house the reader came out of: boarded from this side too.
        box(l,b,-7,0,0,7,4,0,mat("siding_a"));box(l,b,-7,5,0,7,5,0,Blocks.DARK_OAK_SLAB.defaultBlockState());
        for(int x:new int[]{-4,-3,3,4})set(l,b,x,1,0,ReplacementBlock.of(BOARDS,Direction.SOUTH));
        set(l,b,MAILBOX,ReplacementBlock.of(ReplacementBlock.Kind.MAILBOX,Direction.NORTH));
        // The pole by the first house, papered with posters.
        for(int y=0;y<=5;y++)set(l,b,5,y,-6,Blocks.SPRUCE_FENCE.defaultBlockState());
        set(l,b,POSTER,ReplacementBlock.of(ReplacementBlock.Kind.POSTER,Direction.WEST));
        // Six boarded houses, three either side.
        String[] paint={"siding_a","siding_b","siding_c"};
        for(int i=0;i<LOTS.length;i++){house(l,b,-15,-8,LOTS[i][1],LOTS[i][0],Direction.EAST,paint[i%3]);house(l,b,8,15,LOTS[i][1],LOTS[i][0],Direction.WEST,paint[(i+1)%3]);}
        // The street lamps: two still lit, the last one dead.
        lamp(l,b,5,-18,0);lamp(l,b,-5,-30,0);lamp(l,b,5,-42,1);
        open(l,b);
        hedge(l,b);
    }
    /** A boarded house: siding, a slab roof with a ridge, the door and every window boarded, and a path to the sidewalk. */
    static void house(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,Direction faces,String paint){
        box(l,b,x0,0,z0,x1,3,z1,mat(paint));box(l,b,x0-1,4,z0-1,x1+1,4,z1+1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        int ridgeX=(x0+x1)/2;box(l,b,ridgeX,5,z0,ridgeX+1,5,z1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        int front=faces==Direction.EAST?x1:x0,mid=(z0+z1)/2;
        set(l,b,front,0,mid,ReplacementBlock.of(BOARD_DOOR,faces,0));set(l,b,front,1,mid,ReplacementBlock.of(BOARD_DOOR,faces,1));
        for(int z:new int[]{mid-2,mid+2})set(l,b,front,1,z,ReplacementBlock.of(BOARDS,faces));
        for(int x=x0+2;x<=x1-2;x+=3){set(l,b,x,1,z0,ReplacementBlock.of(BOARDS,Direction.NORTH));set(l,b,x,1,z1,ReplacementBlock.of(BOARDS,Direction.SOUTH));}
        int step=faces==Direction.EAST?1:-1;for(int x=front+step;Math.abs(x)<=5;x+=step)set(l,b,x,-1,mid,Blocks.SMOOTH_STONE.defaultBlockState());
    }
    static void lamp(ServerLevel l,BlockPos b,int x,int z,int stage){for(int y=0;y<=3;y++)set(l,b,x,y,z,Blocks.IRON_BARS.defaultBlockState());set(l,b,x,4,z,ReplacementBlock.of(LAMP,Direction.NORTH,stage));}

    /** The one house that isn't boarded. */
    static void open(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var wall=mat("siding_stained");
        box(l,b,-HOUSE_SIDE,-1,HOUSE_BACK,HOUSE_SIDE,4,HOUSE_FRONT,wall);
        box(l,b,-HOUSE_SIDE-1,5,HOUSE_BACK-1,HOUSE_SIDE+1,5,HOUSE_FRONT+1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-6,0,HOUSE_BACK+1,6,3,HOUSE_FRONT-1,air);
        box(l,b,-6,-1,-55,6,-1,HOUSE_FRONT-1,mat("carpet_stained"));box(l,b,-6,-1,HOUSE_BACK+1,6,-1,-56,Blocks.DARK_OAK_PLANKS.defaultBlockState());
        box(l,b,-6,4,HOUSE_BACK+1,6,4,HOUSE_FRONT-1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        // The front door, left open, and the windows either side of it, their glass gone brown.
        var door=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,true);
        set(l,b,HOUSE_DOOR,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,HOUSE_DOOR.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        for(int x:new int[]{-4,-3,3,4})for(int y=1;y<=2;y++)set(l,b,x,y,HOUSE_FRONT,Blocks.BROWN_STAINED_GLASS_PANE.defaultBlockState());
        // The partition between the front room and the two back rooms, and the wall between those.
        box(l,b,-6,0,-56,6,3,-56,wall);for(int x:new int[]{-3,3})for(int y=0;y<=1;y++)set(l,b,x,y,-56,air);
        box(l,b,0,0,HOUSE_BACK+1,0,3,-57,wall);
        // The front room: a sofa, papers, bags, bowls, a litter tray, blankets over shapes, a cat on the cushion that does not move.
        set(l,b,-5,0,-53,ReplacementBlock.of(SOFA,Direction.EAST));set(l,b,-5,0,-54,ReplacementBlock.of(SOFA,Direction.EAST));
        set(l,b,-5,1,-54,ReplacementBlock.of(CAT,Direction.EAST));
        for(int[] at:new int[][]{{5,-52},{6,-53},{-6,-52}})set(l,b,at[0],0,at[1],ReplacementBlock.of(PAPERS,Direction.NORTH));
        for(int[] at:new int[][]{{6,-55},{5,-55},{6,-54}})set(l,b,at[0],0,at[1],ReplacementBlock.of(BAGS,Direction.NORTH));
        set(l,b,-2,0,-53,ReplacementBlock.of(BOWLS,Direction.NORTH));set(l,b,2,0,-54,ReplacementBlock.of(BOWLS,Direction.EAST));set(l,b,4,0,-53,ReplacementBlock.of(LITTER,Direction.NORTH));
        set(l,b,LIES_DOWN,ReplacementBlock.of(BLANKET,Direction.NORTH,1));
        for(int x:new int[]{-6,-5,-1,1,5,6})set(l,b,x,1,-55,ReplacementBlock.of(COLLARS,Direction.SOUTH));
        // The kitchen: cans, bowls, bags, and a dog in the corner that does not move.
        set(l,b,-6,0,-57,ReplacementBlock.of(CANS,Direction.EAST));set(l,b,-6,0,-58,ReplacementBlock.of(CANS,Direction.EAST));
        set(l,b,-2,0,-60,ReplacementBlock.of(BOWLS,Direction.NORTH));set(l,b,-1,0,-61,ReplacementBlock.of(BAGS,Direction.NORTH));
        set(l,b,-6,0,-61,ReplacementBlock.of(DOG,Direction.EAST));
        // The back bedroom: blankets over shapes, a cat by the wall, and the crate in the far corner, shut.
        set(l,b,2,0,-58,ReplacementBlock.of(BLANKET,Direction.NORTH,0));set(l,b,3,0,-60,ReplacementBlock.of(BLANKET,Direction.EAST,1));set(l,b,1,0,-61,ReplacementBlock.of(CAT,Direction.SOUTH));
        set(l,b,CRATE,ReplacementBlock.of(ReplacementBlock.Kind.CRATE,Direction.WEST,0));set(l,b,PEN.north(),air);
        set(l,b,PEN.south(),ReplacementBlock.of(CRATE_SIDE,Direction.WEST));set(l,b,PEN.above(),ReplacementBlock.of(CRATE_SIDE,Direction.WEST));set(l,b,PEN,air);
        set(l,b,6,0,HOUSE_BACK,wall);
        // A little light from nowhere, so the rooms can be seen at all.
        for(int[] at:new int[][]{{0,-53},{-3,-59},{3,-59}})set(l,b,at[0],3,at[1],Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,5));
    }
    /** A hedge round the street, too high to climb: trunks every few blocks, boughs between. */
    static void hedge(ServerLevel l,BlockPos b){
        var leaves=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int z=-1;z>FAR;z--)for(int x:new int[]{-SIDE,SIDE})for(int y=0;y<=5;y++)set(l,b,x,y,z,(Math.floorMod(z,4)==0&&y<5)?Blocks.OAK_LOG.defaultBlockState():leaves);
        for(int x=-SIDE;x<=SIDE;x++)for(int y=0;y<=5;y++)set(l,b,x,y,FAR,(Math.floorMod(x,4)==0&&y<5)?Blocks.OAK_LOG.defaultBlockState():leaves);
        for(int x=-SIDE;x<=SIDE;x++)if(Math.abs(x)>7)for(int y=0;y<=5;y++)set(l,b,x,y,0,leaves);
    }
}
