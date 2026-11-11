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
 * 0.4.407, The Drawing (docs/THE_DRAWING_0_4_407.md): a village square on a bright summer morning, under the outside sky. Every position
 * is relative to the base, the ground just inside the door the reader comes through; north is -z; people stand at y 0 on ground at y -1.
 *
 * <p>Out of the House's door, a lane between two hedges runs north and opens onto the square: flagstones with grass in the joints, round
 * a patch of lawn. In the middle of the lawn, on a three-legged stool, the black box. Down the west side the post office (its board by
 * the door, where the notice is pinned), the bank and a house; down the east side the grocery under its awning, a low stone wall where
 * the children pile stones, and a house; across the north side the meeting hall between two more houses. The hall's door is the way on.
 * Trees close the square in.
 */
public final class DrawingSite {
    private DrawingSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.THE_DRAWING;
    public static final int WEST=-30,EAST=30,NORTH=-72;
    /** The lane: a path three wide from the House's door to the square, hedges either side. */
    public static final int LANE_W=-1,LANE_E=1,LANE_N=-12;
    /** The square's open flagstones, and the lawn in the middle. */
    public static final int SQUARE_W=-13,SQUARE_E=13,SQUARE_S=-13,SQUARE_N=-55,LAWN_W=-7,LAWN_E=7,LAWN_S=-27,LAWN_N=-41;
    /** The stool, and the box on it. */
    public static final BlockPos STOOL=new BlockPos(0,0,-34),BOX=new BlockPos(0,1,-34);
    /** The post office board, by the door: the notice (the first paper) and, beside it, where each reader's account waits. */
    /*
     * 0.4.447 (the owner's screenshot of 10 November): the account's half hung at z -18, across the lower pane of the window between
     * the board and the door. It is on the wall at the notice's other side now; the wall has two boards' width there and no more.
     */
    public static final BlockPos NOTICE=new BlockPos(-13,1,-17),ACCOUNT=new BlockPos(-13,1,-16);
    /** The low stone wall down the east side and the two piles against it. */
    public static final int WALL_X=15,WALL_S=-28,WALL_N=-40;
    public static final BlockPos PILE_SOUTH=new BlockPos(14,0,-31),PILE_NORTH=new BlockPos(14,0,-37);
    /** The meeting hall's door: the way on. */
    public static final BlockPos HALL_DOOR=new BlockPos(0,0,-56);
    /** The doors villagers come out of: the post office, the bank, the grocery, and the houses. */
    public static final BlockPos POST_DOOR=new BlockPos(-14,0,-21),BANK_DOOR=new BlockPos(-14,0,-34),GROCERY_DOOR=new BlockPos(14,0,-20),
        WEST_HOUSE_DOOR=new BlockPos(-14,0,-47),EAST_HOUSE_DOOR=new BlockPos(14,0,-47),NW_HOUSE_DOOR=new BlockPos(-17,0,-56),NE_HOUSE_DOOR=new BlockPos(17,0,-56);
    /** Where a reader is set when their day is cut: the head of the lane, looking up it at the square. */
    public static final Vec3 LANE_HEAD=new Vec3(.5,0,-10.5);
    /** The four houses on the square, in the order they are shuttered, one a year: west, east, north-west, north-east. */
    public static final int HOUSES=4;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inLane(Vec3 r){return r.x>LANE_W-1.5&&r.x<LANE_E+2.5&&r.z>LANE_N+.5&&r.z<1.2&&r.y>-.5&&r.y<3;}
    public static boolean inSquare(Vec3 r){return r.x>SQUARE_W-.5&&r.x<SQUARE_E+1.5&&r.z<SQUARE_S+1&&r.z>SQUARE_N-.5&&r.y>-.5&&r.y<4;}
    public static boolean onLawn(Vec3 r){return r.x>=LAWN_W&&r.x<LAWN_E+1&&r.z<=LAWN_S+1&&r.z>LAWN_N;}
    /** Within reach of the box, to draw from it. */
    public static boolean atBox(Vec3 r){return Math.abs(r.x-.5)<3.2&&Math.abs(r.z+33.5)<3.2;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return DrawingRegistry.material(name).defaultBlockState();}
    static BlockState leaves(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    static BlockState stairs(Block b,Direction f){return b.defaultBlockState().setValue(StairBlock.FACING,f);}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(94070);
        box(l,b,WEST,-4,NORTH,EAST,16,0,air);
        box(l,b,WEST,-4,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        treeline(l,b,rnd);
        lane(l,b,rnd);
        square(l,b,rnd);
        postOffice(l,b);
        bank(l,b);
        grocery(l,b);
        wall(l,b);
        house(l,b,-24,-14,-42,-52,Direction.EAST,WEST_HOUSE_DOOR);
        house(l,b,14,24,-42,-52,Direction.WEST,EAST_HOUSE_DOOR);
        northHouse(l,b,-24,-11,NW_HOUSE_DOOR);
        northHouse(l,b,11,24,NE_HOUSE_DOOR);
        hall(l,b);
        boxAndStool(l,b,0);
        shutters(l,b,0);
        piles(l,b,0);
    }

    /** Woods all round, the lane's hedges, and the House's door in a stone garden wall at the lane's foot. */
    static void treeline(ServerLevel l,BlockPos b,Random rnd){
        var oak=leaves(Blocks.OAK_LEAVES);var birch=leaves(Blocks.BIRCH_LEAVES);
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=0;z++){
            boolean edge=x<=WEST+2||x>=EAST-2||z<=NORTH+2;
            if(!edge)continue;
            int top=6+rnd.nextInt(4);
            for(int y=0;y<=top;y++)set(l,b,x,y,z,(x*7+z*3)%5==0?birch:oak);
            if(rnd.nextInt(7)==0)for(int y=0;y<top-2;y++)set(l,b,x,y,z,Blocks.OAK_LOG.defaultBlockState());
        }
        // Behind the buildings: gardens, with a tree here and there.
        for(int[] t:new int[][]{{-27,-10},{-20,-8},{-27,-29},{27,-12},{20,-7},{27,-36},{-27,-60},{27,-62},{-6,-68},{6,-69},{-10,-6},{10,-5}})tree(l,b,t[0],t[1],rnd);
        // The garden wall the House's door opens in, under a little slate coping.
        box(l,b,-4,0,1,4,3,1,Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        box(l,b,-4,4,1,4,4,1,Blocks.STONE_BRICK_SLAB.defaultBlockState());
    }
    static void tree(ServerLevel l,BlockPos b,int x,int z,Random rnd){
        int h=4+rnd.nextInt(2);var log=Blocks.OAK_LOG.defaultBlockState();var leaf=leaves(Blocks.OAK_LEAVES);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=h-1;y<=h+1;y++){if(Math.abs(dx)+Math.abs(dz)>(y==h+1?2:3))continue;if(l.getBlockState(b.offset(x+dx,y,z+dz)).isAir())set(l,b,x+dx,y,z+dz,leaf);}
        for(int y=0;y<h;y++)set(l,b,x,y,z,log);
    }
    static void lane(ServerLevel l,BlockPos b,Random rnd){
        var hedge=leaves(Blocks.OAK_LEAVES);var path=Blocks.DIRT_PATH.defaultBlockState();
        for(int z=0;z>=LANE_N;z--){
            for(int x=LANE_W;x<=LANE_E;x++)set(l,b,x,-1,z,rnd.nextInt(5)==0?Blocks.COARSE_DIRT.defaultBlockState():path);
            for(int x:new int[]{-3,-2,2,3})for(int y=0;y<=1;y++)set(l,b,x,y,z,hedge);
        }
        // Small front gardens either side of the lane, picket fenced, with a bench by the square.
        for(int z=-2;z>=LANE_N;z-=3){set(l,b,-6,0,z,Blocks.POPPY.defaultBlockState());set(l,b,6,0,z-1,Blocks.OXEYE_DAISY.defaultBlockState());}
        for(int x=-12;x<=-4;x++)set(l,b,x,0,LANE_N,Blocks.BIRCH_FENCE.defaultBlockState());
        for(int x=4;x<=12;x++)set(l,b,x,0,LANE_N,Blocks.BIRCH_FENCE.defaultBlockState());
    }
    /** The flagstones and the lawn, the trees on the lawn's corners, benches. */
    static void square(ServerLevel l,BlockPos b,Random rnd){
        var flag=mat("flagstone");
        box(l,b,SQUARE_W,-1,SQUARE_N,SQUARE_E,-1,SQUARE_S,flag);
        // The flagstones run in under the front of each building too, so nobody steps off them onto turf at a doorway.
        box(l,b,SQUARE_W-1,-1,SQUARE_N,SQUARE_W-1,-1,SQUARE_S,flag);box(l,b,SQUARE_E+1,-1,SQUARE_N,SQUARE_E+1,-1,SQUARE_S,flag);
        box(l,b,LANE_W,-1,SQUARE_S+1,LANE_E,-1,SQUARE_S+1,flag);
        box(l,b,LAWN_W,-1,LAWN_N,LAWN_E,-1,LAWN_S,Blocks.GRASS_BLOCK.defaultBlockState());
        for(int i=0;i<30;i++){int x=LAWN_W+rnd.nextInt(LAWN_E-LAWN_W+1),z=LAWN_N+rnd.nextInt(LAWN_S-LAWN_N+1);if(Math.abs(x)<=3&&z<-30&&z>-38)continue;set(l,b,x,0,z,(i%3==0?Blocks.DANDELION:Blocks.SHORT_GRASS).defaultBlockState());}
        // An old oak at two corners of the lawn.
        tree(l,b,LAWN_W,LAWN_S,rnd);tree(l,b,LAWN_E,LAWN_N,rnd);
        // Benches along the lawn's south edge, facing the box.
        for(int x:new int[]{-5,-4,4,5})set(l,b,x,0,LAWN_S+1,stairs(Blocks.SPRUCE_STAIRS,Direction.SOUTH));
        // Lamp posts at the square's corners.
        for(int[] p:new int[][]{{-12,-14},{12,-14},{-12,-54},{12,-54}}){for(int y=0;y<=2;y++)set(l,b,p[0],y,p[1],Blocks.DARK_OAK_FENCE.defaultBlockState());set(l,b,p[0],3,p[1],Blocks.LANTERN.defaultBlockState());}
    }
    /** A two-storey front on a side of the square: walls of the given material, a door, ground-floor windows, a pitched roof. */
    static void front(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,BlockState wall,Direction faces,BlockPos door,Block doorBlock){
        int lx=Math.min(x0,x1),hx=Math.max(x0,x1),lz=Math.min(z0,z1),hz=Math.max(z0,z1);var air=Blocks.AIR.defaultBlockState();
        box(l,b,lx,-1,lz,hx,7,hz,wall);box(l,b,lx+1,0,lz+1,hx-1,6,hz-1,air);box(l,b,lx+1,-1,lz+1,hx-1,-1,hz-1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,lx+1,3,lz+1,hx-1,3,hz-1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        // A pitched roof whose ridge runs along the front.
        boolean alongZ=faces.getAxis()==Direction.Axis.X;
        int span=alongZ?hx-lx:hz-lz;
        for(int k=0;k<=span/2;k++){int y=8+k;
            if(alongZ){for(int z=lz-1;z<=hz+1;z++){set(l,b,lx-1+k,y,z,stairs(Blocks.DARK_OAK_STAIRS,Direction.EAST));set(l,b,hx+1-k,y,z,stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST));}
                for(int x=lx+k;x<=hx-k;x++){set(l,b,x,y,lz,wall);set(l,b,x,y,hz,wall);}}
            else{for(int x=lx-1;x<=hx+1;x++){set(l,b,x,y,lz-1+k,stairs(Blocks.DARK_OAK_STAIRS,Direction.SOUTH));set(l,b,x,y,hz+1-k,stairs(Blocks.DARK_OAK_STAIRS,Direction.NORTH));}
                for(int z=lz+k;z<=hz-k;z++){set(l,b,lx,y,z,wall);set(l,b,hx,y,z,wall);}}
        }
        if((span&1)==0){int y=8+span/2;if(alongZ)for(int z=lz-1;z<=hz+1;z++)set(l,b,lx+span/2,y,z,Blocks.DARK_OAK_SLAB.defaultBlockState());else for(int x=lx-1;x<=hx+1;x++)set(l,b,x,y,lz+span/2,Blocks.DARK_OAK_SLAB.defaultBlockState());}
        // The door (closed and kept so: the office and the bank are shut for the morning, the houses are out in the square).
        var inward=faces.getOpposite();NovelRooms.door(l,b.offset(door),inward,doorBlock,false);
        // Windows: two either side of the door on the ground floor, a row above.
        var pane=Blocks.GLASS_PANE.defaultBlockState();
        for(int d:new int[]{-3,-2,2,3})for(int y=1;y<=2;y++){var at=alongZ?door.offset(0,y,d):door.offset(d,y,0);if(at.getX()>lx&&at.getX()<hx||at.getZ()>lz&&at.getZ()<hz)set(l,b,at,pane);}
        for(int d=-4;d<=4;d+=2){var at=alongZ?door.offset(0,5,d):door.offset(d,5,0);if(alongZ?(at.getZ()>lz&&at.getZ()<hz):(at.getX()>lx&&at.getX()<hx))set(l,b,at,pane);}
        // A step at the door.
        var step=door.relative(faces).below();set(l,b,step,Blocks.STONE_BRICKS.defaultBlockState());
    }
    static void postOffice(ServerLevel l,BlockPos b){
        front(l,b,-24,-14,-15,-25,mat("weatherboard"),Direction.EAST,POST_DOOR,Blocks.SPRUCE_DOOR);
        // The board by the door, two pins wide: the notice, and beside it a bare pin.
        set(l,b,NOTICE,DrawingBlock.of(DrawingBlock.Kind.NOTICE,Direction.EAST));
        set(l,b,ACCOUNT,DrawingBlock.of(DrawingBlock.Kind.ACCOUNT,Direction.EAST));
        set(l,b,POST_DOOR.offset(1,3,0),DrawingBlock.of(DrawingBlock.Kind.SIGN,Direction.EAST,0));
    }
    static void bank(ServerLevel l,BlockPos b){
        front(l,b,-24,-14,-29,-39,mat("brick"),Direction.EAST,BANK_DOOR,Blocks.DARK_OAK_DOOR);
        set(l,b,BANK_DOOR.offset(1,3,0),DrawingBlock.of(DrawingBlock.Kind.SIGN,Direction.EAST,1));
        // Stone steps up and two pilasters.
        for(int y=0;y<=6;y++){set(l,b,-14,y,BANK_DOOR.getZ()-2,Blocks.SMOOTH_STONE.defaultBlockState());set(l,b,-14,y,BANK_DOOR.getZ()+2,Blocks.SMOOTH_STONE.defaultBlockState());}
    }
    static void grocery(ServerLevel l,BlockPos b){
        front(l,b,14,24,-15,-25,mat("weatherboard"),Direction.WEST,GROCERY_DOOR,Blocks.OAK_DOOR);
        for(int z=-16;z>=-24;z--)set(l,b,13,3,z,DrawingBlock.of(DrawingBlock.Kind.AWNING,Direction.WEST));
        set(l,b,GROCERY_DOOR.offset(-1,4,0),DrawingBlock.of(DrawingBlock.Kind.SIGN,Direction.WEST,2));
        // Crates out front under the awning, a barrel by the door.
        set(l,b,13,0,-17,DrawingBlock.of(DrawingBlock.Kind.CRATE,Direction.WEST,0));set(l,b,13,0,-18,DrawingBlock.of(DrawingBlock.Kind.CRATE,Direction.WEST,1));
        set(l,b,13,0,-23,DrawingBlock.of(DrawingBlock.Kind.CRATE,Direction.WEST,0));
        set(l,b,13,0,-22,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
    }
    /** The low stone wall down the east side, with a vegetable plot behind it. */
    static void wall(ServerLevel l,BlockPos b){
        for(int z=WALL_S;z>=WALL_N;z--){set(l,b,WALL_X,0,z,Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState());set(l,b,WALL_X,-1,z,Blocks.COBBLESTONE.defaultBlockState());}
        for(int x=16;x<=24;x++)for(int z=WALL_S-1;z>=WALL_N+1;z--){set(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState());set(l,b,x,0,z,(x%2==0?Blocks.POTATOES:Blocks.CARROTS).defaultBlockState().setValue(CropBlock.AGE,5));}
    }
    static void house(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,Direction inward,BlockPos door){front(l,b,x0,x1,z0,z1,mat("weatherboard"),inward.getOpposite(),door,Blocks.SPRUCE_DOOR);}
    static void northHouse(ServerLevel l,BlockPos b,int x0,int x1,BlockPos door){front(l,b,x0,x1,-56,-64,mat("weatherboard"),Direction.SOUTH,door,Blocks.SPRUCE_DOOR);}
    /** The meeting hall: weatherboard with a columned porch and a pediment; its door is the way on. */
    static void hall(ServerLevel l,BlockPos b){
        front(l,b,-8,8,-56,-66,mat("weatherboard"),Direction.SOUTH,HALL_DOOR,Blocks.SPRUCE_DOOR);
        for(int x:new int[]{-6,-3,3,6})for(int y=0;y<=5;y++)set(l,b,x,y,-55,Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
        box(l,b,-7,6,-55,7,6,-55,Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
        set(l,b,HALL_DOOR.offset(0,3,1),DrawingBlock.of(DrawingBlock.Kind.SIGN,Direction.SOUTH,3));
        box(l,b,-2,-1,-55,2,-1,-55,Blocks.STONE_BRICKS.defaultBlockState());
        // The doorway is left open for the way on, which hangs its own door in it.
        set(l,b,HALL_DOOR,Blocks.AIR.defaultBlockState());set(l,b,HALL_DOOR.above(),Blocks.AIR.defaultBlockState());
    }
    /** The houses' windows, curtained, or shuttered for as many houses as the village has lost. */
    static List<BlockPos> windows(int house){
        var out=new ArrayList<BlockPos>();
        BlockPos door=switch(house){case 0->WEST_HOUSE_DOOR;case 1->EAST_HOUSE_DOOR;case 2->NW_HOUSE_DOOR;default->NE_HOUSE_DOOR;};
        boolean alongZ=house<2;
        for(int d:new int[]{-3,3})for(int y:new int[]{1,2})out.add(alongZ?door.offset(0,y,d):door.offset(d,y,0));
        for(int d:new int[]{-2,2})out.add(alongZ?door.offset(0,5,d):door.offset(d,5,0));
        return out;
    }
    static Direction windowFacing(int house){return switch(house){case 0->Direction.EAST;case 1->Direction.WEST;default->Direction.SOUTH;};}
    public static void shutters(ServerLevel l,BlockPos b,int shut){
        for(int h=0;h<HOUSES;h++)for(var w:windows(h))set(l,b,w,DrawingBlock.of(DrawingBlock.Kind.SHUTTER,windowFacing(h),h<shut?1:0));
    }
    /** The box on its stool, shabbier with the years: string, then wire, then a bootlace. */
    public static void boxAndStool(ServerLevel l,BlockPos b,int stage){
        set(l,b,STOOL,DrawingBlock.of(DrawingBlock.Kind.STOOL,Direction.SOUTH));
        set(l,b,BOX,DrawingBlock.of(DrawingBlock.Kind.BOX,Direction.SOUTH,stage));
    }
    public static void piles(ServerLevel l,BlockPos b,int stage){
        set(l,b,PILE_SOUTH,DrawingBlock.of(DrawingBlock.Kind.STONES,Direction.WEST,stage));
        set(l,b,PILE_NORTH,DrawingBlock.of(DrawingBlock.Kind.STONES,Direction.WEST,Math.max(0,stage-1)));
    }
}
