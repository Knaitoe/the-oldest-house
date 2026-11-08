package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.348, The Supper (docs/MARSH_SUPPER_0_4_348.md): the Marsh farm at dusk, under the outside sky. Every position is relative to the
 * base, the ground just inside the door the reader comes through; north is -z.
 *
 * <p>Out of the House's door, between two hedges, a corn maze: thirteen rows of three-block cells cut and trodden through corn two high,
 * so the farmhouse's upstairs windows show over the tassels from the first turn. It opens onto a mown clearing inside a split-rail fence:
 * the dinner bell on its post, the pump, the yard light. The farmhouse stands at the clearing's far end, two storeys of clapboard with a
 * porch along its front and round its east side; the doormat is at the top of the steps. Inside, the dining room (the table, the
 * chairs, the coats by the door) and, through a partition with a serving hatch and a swing door, the kitchen (the butcher's block, the
 * cooker, the sink, and a back door onto the porch). The only drive runs east out of the clearing through the corn to the county road;
 * an old oak lies across it, its root plate standing out of the ditch on the south side and its crown in the corn on the north. On foot
 * a reader squeezes between the roots and the ditch. The gate onto the county road at the drive's end is the way on. Field corn, three
 * high, stands round the fence, and a treeline closes the farm in.
 */
public final class MarshSupperSite {
    private MarshSupperSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.MARSH_SUPPER;
    public static final int WEST=-32,EAST=32,NORTH=-86;
    /** The maze: thirteen rows of nineteen three-block cells from z -2 to z -40, x -28 to 28. */
    public static final int MAZE_ROWS=13,MAZE_COLS=19,MAZE_X0=-28,MAZE_Z0=-2;
    /** The clearing inside the split-rail fence, and the fence's own lines. */
    public static final int CLEAR_W=-21,CLEAR_E=21,CLEAR_S=-42,CLEAR_N=-81,FENCE_W=-22,FENCE_E=22,FENCE_S=-41,FENCE_N=-82;
    /** The farmhouse: walls x -11..9, z -60..-74; its floor at y 0, the ground floor's air y 1..4, the upstairs floor y 5, the roof from y 10. */
    public static final int HOUSE_W=-11,HOUSE_E=9,HOUSE_S=-60,HOUSE_N=-74,GROUND=1;
    /** The porch: along the front (z -56..-59) and round the east side (x 10..12) to z -72. */
    public static final int PORCH_S=-56,PORCH_N=-59,PORCH_E=12,PORCH_EAST_END=-72;
    public static final BlockPos FRONT_DOOR=new BlockPos(-4,1,-60),MAT=new BlockPos(-4,1,-59),PORCH_LIGHT=new BlockPos(-2,3,-59);
    public static final BlockPos COAT_HOOK=new BlockPos(-7,2,-61),FRONT_WINDOW=new BlockPos(-8,2,-60);
    /** The table: x -6..-4, z -64..-69, its top at y 1; place settings sit on it at y 2. */
    public static final int TABLE_W=-6,TABLE_E=-4,TABLE_S=-64,TABLE_N=-69;
    /** The household's chairs: Ida at the south head facing the kitchen door, Royce at the north head, Nana and Corinne on the west side, Del on the east. */
    public static final BlockPos IDA_SEAT=new BlockPos(-5,1,-63),ROYCE_SEAT=new BlockPos(-5,1,-70),NANA_SEAT=new BlockPos(-7,1,-65),DEL_SEAT=new BlockPos(-3,1,-65),CORINNE_SEAT=new BlockPos(-7,1,-67);
    /** The readers' chairs, one each: two on the west side, three on the east. */
    public static final List<BlockPos> READER_SEATS=List.of(new BlockPos(-7,1,-66),new BlockPos(-3,1,-66),new BlockPos(-7,1,-68),new BlockPos(-3,1,-67),new BlockPos(-3,1,-68));
    /** On the table: the bread board, the jug, where the roast is set, and the lamp. */
    public static final BlockPos BREAD=new BlockPos(-5,2,-65),JUG=new BlockPos(-5,2,-66),ROAST=new BlockPos(-5,2,-67),TABLE_LAMP=new BlockPos(-5,2,-68);
    /** The partition between the dining room (west) and the kitchen (east), its serving hatch and its swing door. */
    public static final int PARTITION_X=0;
    public static final BlockPos HATCH=new BlockPos(0,2,-66),KITCHEN_DOOR=new BlockPos(0,1,-71);
    /** The kitchen: the butcher's block with the keys on it, the coat over a chair, the window onto the east porch, the back door. */
    public static final BlockPos BUTCHER=new BlockPos(5,1,-67),KEYS=new BlockPos(5,2,-67),COAT_CHAIR=new BlockPos(6,1,-69),KITCHEN_WINDOW=new BlockPos(9,2,-66),BACK_DOOR=new BlockPos(9,1,-70);
    /** The clearing's dinner bell, yard light and pump. */
    public static final BlockPos BELL=new BlockPos(5,1,-52),YARD_LIGHT=new BlockPos(7,0,-46),PUMP=new BlockPos(-6,0,-48);
    /** The drive east to the county road, the oak across it (trunk, root plate, the ledge between the roots and the ditch), and the gate. */
    public static final int DRIVE_S=-59,DRIVE_N=-61,DRIVE_FROM=13,DRIVE_TO=29,TREE_X=25,DITCH_Z=-64,LEDGE_Z=-63,ROOTS_Z=-62;
    public static final BlockPos GATE=new BlockPos(29,0,-60);
    /** The supper card on its post at the mouth of the maze (the first paper), and where an account waits: on the chest of drawers by the table. */
    public static final BlockPos CARD=new BlockPos(2,0,-1),ENDING=new BlockPos(-3,2,-73);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inHouse(Vec3 r){return r.x>HOUSE_W&&r.x<HOUSE_E&&r.z<HOUSE_S&&r.z>HOUSE_N+1&&r.y>.5&&r.y<5;}
    public static boolean inDining(Vec3 r){return inHouse(r)&&r.x<PARTITION_X;}
    public static boolean inKitchen(Vec3 r){return inHouse(r)&&r.x>=PARTITION_X+1;}
    public static boolean onPorch(Vec3 r){return r.y>.5&&r.y<4&&(r.z<=PORCH_S+1&&r.z>PORCH_N&&r.x>=HOUSE_W&&r.x<PORCH_E+1||r.x>=HOUSE_E+1&&r.x<PORCH_E+1&&r.z<=PORCH_N+1&&r.z>PORCH_EAST_END);}
    public static boolean onEastPorch(Vec3 r){return r.y>.5&&r.y<4&&r.x>=HOUSE_E+1&&r.x<PORCH_E+1&&r.z<=PORCH_N+1&&r.z>PORCH_EAST_END;}
    public static boolean inClearing(Vec3 r){return r.x>=CLEAR_W&&r.x<CLEAR_E+1&&r.z<=CLEAR_S+1&&r.z>CLEAR_N;}
    public static boolean inMaze(Vec3 r){return r.z<=MAZE_Z0+1&&r.z>MAZE_Z0-3*MAZE_ROWS&&r.x>=MAZE_X0&&r.x<MAZE_X0+3*MAZE_COLS;}
    public static boolean nearTree(Vec3 r){return Math.abs(r.x-(TREE_X+.5))<6&&r.z<-50&&r.z>-66;}
    /** Out past the oak and at the gate onto the county road. */
    public static boolean pastTree(Vec3 r){return r.x>GATE.getX()-1.5&&r.z<DRIVE_S+1&&r.z>DITCH_Z;}
    /** Every reader chair, the household's first. */
    public static List<BlockPos> chairs(){var out=new ArrayList<BlockPos>(List.of(IDA_SEAT,ROYCE_SEAT,NANA_SEAT,DEL_SEAT,CORINNE_SEAT));out.addAll(READER_SEATS);return out;}
    /** Which way a chair faces: the heads along the table, the sides across it. */
    public static Direction facing(BlockPos seat){return seat.equals(IDA_SEAT)?Direction.NORTH:seat.equals(ROYCE_SEAT)?Direction.SOUTH:seat.getX()<TABLE_W?Direction.EAST:Direction.WEST;}
    /** The place setting in front of a chair, on the table. */
    public static BlockPos setting(BlockPos seat){return seat.relative(facing(seat)).above();}

    // ------------------------------------------------------------------------------------------------ the maze
    private static boolean[][] maze;
    /** The maze's open cells, row 0 by the House's door, row 12 by the clearing: a fixed recursive-backtracked maze with a few loops cut through. */
    public static synchronized boolean[][] maze(){
        if(maze!=null)return maze;
        var open=new boolean[MAZE_ROWS][MAZE_COLS];var rnd=new Random(9348);
        var stack=new ArrayDeque<int[]>();open[1][9]=true;stack.push(new int[]{1,9});
        int[][] dirs={{2,0},{-2,0},{0,2},{0,-2}};
        while(!stack.isEmpty()){
            var cur=stack.peek();var next=new ArrayList<int[]>();
            for(var d:dirs){int r=cur[0]+d[0],c=cur[1]+d[1];if(r>0&&r<MAZE_ROWS-1&&c>0&&c<MAZE_COLS-1&&!open[r][c])next.add(new int[]{r,c,d[0]/2,d[1]/2});}
            if(next.isEmpty()){stack.pop();continue;}
            var n=next.get(rnd.nextInt(next.size()));open[cur[0]+n[2]][cur[1]+n[3]]=true;open[n[0]][n[1]]=true;stack.push(new int[]{n[0],n[1]});
        }
        // A few walls cut through, so the maze has loops and nobody is ever sent all the way back.
        for(int cut=0,tries=0;cut<7&&tries<400;tries++){int r=1+rnd.nextInt(MAZE_ROWS-2),c=1+rnd.nextInt(MAZE_COLS-2);
            if(open[r][c]||(r%2==1)==(c%2==1))continue;open[r][c]=true;cut++;}
        open[0][9]=true;open[MAZE_ROWS-1][9]=true;
        maze=open;return open;
    }
    public static boolean openCell(int row,int col){return row>=0&&row<MAZE_ROWS&&col>=0&&col<MAZE_COLS&&maze()[row][col];}
    /** The maze cell a local position is in, or null outside it. */
    public static int[] cell(double x,double z){int col=(int)Math.floor((x-MAZE_X0)/3),row=(int)Math.floor((MAZE_Z0+1-z)/3);return row>=0&&row<MAZE_ROWS&&col>=0&&col<MAZE_COLS?new int[]{row,col}:null;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState mat(String name){return MarshRegistry.material(name).defaultBlockState();}
    static BlockState clapboard(){return YardRooms.mat("clapboard");}
    static BlockState leaves(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    static BlockState stalk(int stage,Direction f){return YardBlock.of(YardBlock.Kind.STALK,f,stage);}
    /** Standing corn, two high in the maze and three in the field round it. */
    static void corn(ServerLevel l,BlockPos b,int x,int z,int high,Random rnd){
        var f=Direction.from2DDataValue(rnd.nextInt(4));
        set(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());
        set(l,b,x,0,z,stalk(0,f));
        if(high==2){set(l,b,x,1,z,stalk(2,f));set(l,b,x,2,z,Blocks.AIR.defaultBlockState());}
        else{set(l,b,x,1,z,stalk(1,f));set(l,b,x,2,z,stalk(2,f));}
    }

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(93480);
        box(l,b,WEST,-5,NORTH,EAST,18,0,air);
        box(l,b,WEST,-5,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        treeline(l,b,rnd);
        mazeCorn(l,b);
        field(l,b,rnd);
        clearing(l,b,rnd);
        drive(l,b,rnd);
        house(l,b);
        furnish(l,b);
        // The supper card on its post, beside the way into the maze, its face to the House's door.
        set(l,b,CARD,MarshBlock.of(MarshBlock.Kind.CARD,Direction.SOUTH));
    }

    /** A treeline closes the farm in: oaks and their leaves along the edges, and hedges either side of the House's door. */
    static void treeline(ServerLevel l,BlockPos b,Random rnd){
        var oak=leaves(Blocks.OAK_LEAVES);var dark=leaves(Blocks.DARK_OAK_LEAVES);
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=0;z++){
            boolean edge=x<=WEST+2||x>=EAST-2||z<=NORTH+1||z>=-1&&Math.abs(x)>=4;
            if(!edge)continue;
            int top=z>=-1?3:6+rnd.nextInt(3);
            for(int y=0;y<=top;y++)set(l,b,x,y,z,(x+z)%3==0?dark:oak);
            if(z<-1&&rnd.nextInt(9)==0)for(int y=0;y<top-1;y++)set(l,b,x,y,z,Blocks.OAK_LOG.defaultBlockState());
        }
        // The House's door opens in the back wall of a clapboard shed, under a slab roof.
        box(l,b,-3,0,1,3,4,1,clapboard());
        box(l,b,-4,5,0,4,5,1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        for(int x=-1;x<=1;x++)for(int z=-1;z<=0;z++)set(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());
    }

    /** The maze: open cells trodden, the rest standing corn two high, so the upstairs windows show over the tassels. */
    static void mazeCorn(ServerLevel l,BlockPos b){
        var rnd=new Random(93481);
        for(int row=0;row<MAZE_ROWS;row++)for(int col=0;col<MAZE_COLS;col++){
            boolean open=openCell(row,col);
            for(int dx=0;dx<3;dx++)for(int dz=0;dz<3;dz++){int x=MAZE_X0+3*col+dx,z=MAZE_Z0-3*row-dz;
                if(open){set(l,b,x,-1,z,rnd.nextInt(5)==0?Blocks.COARSE_DIRT.defaultBlockState():Blocks.DIRT_PATH.defaultBlockState());for(int y=0;y<=2;y++)set(l,b,x,y,z,Blocks.AIR.defaultBlockState());}
                else corn(l,b,x,z,2,rnd);}
        }
    }
    /** Every stalk somebody pushed flat stands up again: two high in the maze, three in the field round it. */
    public static void regrowBent(ServerLevel l,BlockPos b){
        var rnd=new Random(93482);
        for(int x=WEST+3;x<=EAST-3;x++)for(int z=NORTH+2;z<=-2;z++){var st=l.getBlockState(b.offset(x,0,z));
            if(!YardBlock.is(st,YardBlock.Kind.STALK)||st.getValue(YardBlock.STAGE)!=3)continue;
            boolean maze=x>=MAZE_X0&&x<MAZE_X0+3*MAZE_COLS&&z<=MAZE_Z0&&z>MAZE_Z0-3*MAZE_ROWS;corn(l,b,x,z,maze?2:3,rnd);}
    }

    /** Field corn three high round the clearing's fence and up to the treeline, east and west of the maze too. */
    static void field(ServerLevel l,BlockPos b,Random rnd){
        for(int x=WEST+3;x<=EAST-3;x++)for(int z=NORTH+2;z<=-2;z++){
            boolean maze=x>=MAZE_X0&&x<MAZE_X0+3*MAZE_COLS&&z<=MAZE_Z0&&z>MAZE_Z0-3*MAZE_ROWS;
            boolean inside=x>=FENCE_W&&x<=FENCE_E&&z<=FENCE_S&&z>=FENCE_N;
            boolean drive=x>FENCE_E&&z<=DRIVE_S+3&&z>=DITCH_Z-1;
            boolean crown=x>=TREE_X-3&&x<=TREE_X+3&&z<=-50&&z>=-58;
            if(maze||inside||drive||crown)continue;
            corn(l,b,x,z,3,rnd);
        }
    }

    /** The mown clearing inside its split-rail fence: the path to the porch, the dinner bell, the pump, the yard light, the woodpile. */
    static void clearing(ServerLevel l,BlockPos b,Random rnd){
        var fence=Blocks.OAK_FENCE.defaultBlockState();
        for(int x=FENCE_W;x<=FENCE_E;x++){if(Math.abs(x)>1)set(l,b,x,0,FENCE_S,fence);set(l,b,x,0,FENCE_N,fence);}
        for(int z=FENCE_N;z<=FENCE_S;z++){set(l,b,FENCE_W,0,z,fence);if(z>DRIVE_S||z<DRIVE_N)set(l,b,FENCE_E,0,z,fence);}
        // The trodden path from the maze's mouth to the porch steps.
        for(int z=FENCE_S;z>=-48;z--)for(int x=-1;x<=1;x++)set(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());
        for(int x=-5;x<=1;x++)for(int z=-48;z>=-50;z--)set(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());
        for(int z=-50;z>=-55;z--)for(int x=-5;x<=-3;x++)set(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());
        // The dinner bell in its yoke on a post by the steps.
        set(l,b,BELL.below(),Blocks.SPRUCE_FENCE.defaultBlockState());set(l,b,BELL,MarshBlock.of(MarshBlock.Kind.BELL,Direction.SOUTH));
        // The pump over its trough.
        set(l,b,PUMP,Blocks.COBBLESTONE_WALL.defaultBlockState());set(l,b,PUMP.above(),Blocks.LIGHTNING_ROD.defaultBlockState());
        set(l,b,PUMP.south(),Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,2));
        // The yard light on its pole, the lamp hung from an arm over the clearing.
        for(int y=0;y<=5;y++)set(l,b,YARD_LIGHT.getX(),y,YARD_LIGHT.getZ(),Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(l,b,YARD_LIGHT.getX()-1,5,YARD_LIGHT.getZ(),Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(l,b,YARD_LIGHT.getX()-1,4,YARD_LIGHT.getZ(),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The woodpile along the west end of the house.
        for(int z=-63;z>=-68;z--)for(int y=0;y<=1;y++)set(l,b,HOUSE_W-2,y,z,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
    }

    /** The drive east to the county road; the oak across it, root plate up out of the ditch, crown in the corn; the gate at the end. */
    static void drive(ServerLevel l,BlockPos b,Random rnd){
        var gravel=YardRooms.mat("wet_gravel");
        for(int x=DRIVE_FROM;x<=DRIVE_TO;x++){
            for(int z=DRIVE_N;z<=DRIVE_S;z++)set(l,b,x,-1,z,gravel);
            set(l,b,x,-1,ROOTS_Z,Blocks.GRASS_BLOCK.defaultBlockState());set(l,b,x,-1,LEDGE_Z,Blocks.MUD.defaultBlockState());
            set(l,b,x,-1,DITCH_Z,Blocks.WATER.defaultBlockState());set(l,b,x,-2,DITCH_Z,Blocks.MUD.defaultBlockState());
            set(l,b,x,-1,DITCH_Z-1,Blocks.MUD.defaultBlockState());
            if(rnd.nextInt(3)==0)set(l,b,x,0,DITCH_Z-1,Blocks.SHORT_GRASS.defaultBlockState());
        }
        // Close the ditch's two ends so its water stays put.
        set(l,b,DRIVE_FROM-1,-1,DITCH_Z,Blocks.MUD.defaultBlockState());set(l,b,DRIVE_TO+1,-1,DITCH_Z,Blocks.MUD.defaultBlockState());
        // The oak: two logs thick across the whole drive, moss along its top and a split near its foot.
        var log=Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z);
        for(int z=-55;z>=DRIVE_N;z--)for(int y=0;y<=1;y++)set(l,b,TREE_X,y,z,z==-60&&y==1?Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z):log);
        for(int z=-55;z>=DRIVE_N;z--)if(z!=-60)set(l,b,TREE_X,2,z,Blocks.MOSS_CARPET.defaultBlockState());
        // Its root plate standing up out of the ditch side, earth still in it.
        for(int x=TREE_X-1;x<=TREE_X+1;x++)for(int y=0;y<=3;y++)set(l,b,x,y,ROOTS_Z,(x+y)%2==0?Blocks.ROOTED_DIRT.defaultBlockState():Blocks.MANGROVE_ROOTS.defaultBlockState());
        set(l,b,TREE_X,4,ROOTS_Z,Blocks.MANGROVE_ROOTS.defaultBlockState());
        // Its crown lying in the corn on the north side.
        var crown=leaves(Blocks.OAK_LEAVES);
        for(int x=TREE_X-3;x<=TREE_X+3;x++)for(int z=-50;z>=-56;z--)for(int y=0;y<=4;y++){
            double d=Math.hypot(x-TREE_X,(z+53)*1.1)+y*.6;if(d<3.8&&!(x==TREE_X&&z<=-55&&y<=1))set(l,b,x,y,z,crown);}
        for(int z=-52;z>=-55;z--)set(l,b,TREE_X,1,z,log);
        // The gate onto the county road, between two posts in the treeline.
        for(int y=0;y<=1;y++){set(l,b,GATE.getX(),y,GATE.getZ()+1,Blocks.SPRUCE_FENCE.defaultBlockState());set(l,b,GATE.getX(),y,GATE.getZ()-1,Blocks.SPRUCE_FENCE.defaultBlockState());}
        set(l,b,GATE.getX(),2,GATE.getZ()+1,Blocks.SPRUCE_LOG.defaultBlockState());set(l,b,GATE.getX(),2,GATE.getZ()-1,Blocks.SPRUCE_LOG.defaultBlockState());
        set(l,b,GATE.getX(),2,GATE.getZ(),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,GATE.getX(),3,GATE.getZ()+1,Blocks.LANTERN.defaultBlockState());
    }

    /** The farmhouse: two storeys of clapboard, its windows lit upstairs, the porch along the front and round the east side, the roof. */
    static void house(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var board=clapboard();
        // The porch: deck, steps, posts, rail and roof.
        var deck=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,HOUSE_W,0,PORCH_N,PORCH_E,0,PORCH_S,deck);box(l,b,HOUSE_E+1,0,PORCH_EAST_END,PORCH_E,0,PORCH_N,deck);
        for(int x=-5;x<=-3;x++)set(l,b,x,0,PORCH_S+1,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        var rail=Blocks.SPRUCE_FENCE.defaultBlockState();
        for(int x=HOUSE_W;x<=PORCH_E;x++)if(x<-5||x>-3)set(l,b,x,1,PORCH_S,rail);
        for(int z=PORCH_S;z>=PORCH_EAST_END;z--)if(z>DRIVE_S-1||z<DRIVE_N)set(l,b,PORCH_E,1,z,rail);
        // Two steps up off the drive onto the east porch, for the back door.
        for(int z=DRIVE_S-1;z>=DRIVE_N;z--)set(l,b,PORCH_E+1,0,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
        for(int x=HOUSE_E+1;x<=PORCH_E;x++)set(l,b,x,1,PORCH_EAST_END,rail);
        set(l,b,HOUSE_W,1,PORCH_N,rail);set(l,b,HOUSE_W,1,PORCH_N+1,rail);set(l,b,HOUSE_W,1,PORCH_N+2,rail);
        for(var post:new int[][]{{HOUSE_W,PORCH_S},{-6,PORCH_S},{-2,PORCH_S},{PORCH_E,PORCH_S},{PORCH_E,-64},{PORCH_E,PORCH_EAST_END}})
            for(int y=1;y<=4;y++)set(l,b,post[0],y,post[1],Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        var roof=Blocks.DARK_OAK_SLAB.defaultBlockState();
        box(l,b,HOUSE_W-1,5,PORCH_N,PORCH_E+1,5,PORCH_S+1,roof);box(l,b,HOUSE_E+1,5,PORCH_EAST_END-1,PORCH_E+1,5,PORCH_N,roof);
        // The shell: floor, walls two storeys high, the upstairs floor over the ground floor.
        box(l,b,HOUSE_W,0,HOUSE_N,HOUSE_E,9,HOUSE_S,board);
        box(l,b,HOUSE_W+1,1,HOUSE_N+1,HOUSE_E-1,4,HOUSE_S-1,air);
        box(l,b,HOUSE_W+1,6,HOUSE_N+1,HOUSE_E-1,9,HOUSE_S-1,air);
        box(l,b,HOUSE_W+1,0,HOUSE_N+1,PARTITION_X,0,HOUSE_S-1,mat("boards"));
        box(l,b,PARTITION_X+1,0,HOUSE_N+1,HOUSE_E-1,0,HOUSE_S-1,mat("linoleum"));
        box(l,b,HOUSE_W+1,5,HOUSE_N+1,HOUSE_E-1,5,HOUSE_S-1,mat("boards"));
        // The partition: wallpaper on the dining room's side, with the serving hatch through it and the swing door at its north end.
        box(l,b,PARTITION_X,1,HOUSE_N+1,PARTITION_X,4,HOUSE_S-1,mat("wallpaper"));
        set(l,b,HATCH,air);
        // The roof: a gable running east and west, its ridge over the middle of the house, the gable ends boarded.
        for(int k=0;k<=7;k++){int y=10+k;
            for(int x=HOUSE_W-1;x<=HOUSE_E+1;x++){
                set(l,b,x,y,HOUSE_S+1-k,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
                set(l,b,x,y,HOUSE_N-1+k,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}
            for(int z=HOUSE_N+k;z<=HOUSE_S-k;z++){set(l,b,HOUSE_W,y,z,board);set(l,b,HOUSE_E,y,z,board);}
        }
        for(int x=HOUSE_W-1;x<=HOUSE_E+1;x++){set(l,b,x,17,-67,Blocks.DARK_OAK_PLANKS.defaultBlockState());set(l,b,x,18,-67,Blocks.DARK_OAK_SLAB.defaultBlockState());}
        // A chimney up the west gable.
        for(int y=1;y<=18;y++)set(l,b,HOUSE_W-1,y,-66,Blocks.BRICKS.defaultBlockState());
        // Doors: the front door off the porch, the kitchen's swing door, the back door onto the east porch.
        NovelRooms.door(l,b.offset(FRONT_DOOR),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        NovelRooms.door(l,b.offset(KITCHEN_DOOR),Direction.EAST,Blocks.OAK_DOOR,false);
        NovelRooms.door(l,b.offset(BACK_DOOR),Direction.WEST,Blocks.SPRUCE_DOOR,false);
        // Windows: downstairs glass; upstairs, every window lit behind its curtains; the attic's in each gable.
        var pane=Blocks.GLASS_PANE.defaultBlockState();
        for(int y=2;y<=3;y++){
            for(int x:new int[]{-9,-8})set(l,b,x,y,HOUSE_S,pane);
            for(int z:new int[]{-64,-67,-70})set(l,b,HOUSE_W,y,z,pane);
            for(int z:new int[]{-63,-66})set(l,b,HOUSE_E,y,z,pane);
            set(l,b,4,y,HOUSE_N,pane);set(l,b,-7,y,HOUSE_N,pane);
        }
        for(int x:new int[]{-9,-6,3,6}){set(l,b,x,7,HOUSE_S,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.SOUTH));set(l,b,x,7,HOUSE_N,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.NORTH));}
        for(int z:new int[]{-64,-70}){set(l,b,HOUSE_W,7,z,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.WEST));set(l,b,HOUSE_E,7,z,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.EAST));}
        for(int y=12;y<=13;y++){set(l,b,HOUSE_W,y,-67,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.WEST));set(l,b,HOUSE_E,y,-67,MarshBlock.of(MarshBlock.Kind.WINDOW,Direction.EAST));}
        // The porch light by the front door, and the doormat at the top of the steps.
        set(l,b,PORCH_LIGHT,MarshBlock.of(MarshBlock.Kind.PORCH_LIGHT,Direction.SOUTH));
        set(l,b,MAT,MarshBlock.of(MarshBlock.Kind.MAT,Direction.SOUTH));
    }

    /** The dining room and the kitchen, laid for supper. */
    static void furnish(ServerLevel l,BlockPos b){
        // The table: a long one, its legs at the corners.
        for(int x=TABLE_W;x<=TABLE_E;x++)for(int z=TABLE_N;z<=TABLE_S;z++){
            boolean w=x==TABLE_W,e=x==TABLE_E,n=z==TABLE_N,s=z==TABLE_S;
            Direction leg=w&&n?Direction.NORTH:e&&n?Direction.EAST:e&&s?Direction.SOUTH:w&&s?Direction.WEST:null;
            set(l,b,x,GROUND,z,GhostsSetBlock.of(leg==null?GhostsSetBlock.Kind.TABLE:GhostsSetBlock.Kind.TABLE_LEG,leg==null?Direction.NORTH:leg));
        }
        for(var seat:chairs())set(l,b,seat,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,facing(seat)));
        lay(l,b);
        set(l,b,TABLE_LAMP,GhostsSetBlock.of(GhostsSetBlock.Kind.LAMP,Direction.SOUTH));
        // The dining room's lamps over the table, the chests of drawers along the north wall, the coats by the door.
        for(int z:new int[]{-65,-68})set(l,b,-5,4,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        set(l,b,-9,GROUND,HOUSE_N+1,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));
        set(l,b,-3,GROUND,HOUSE_N+1,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));
        set(l,b,-9,GROUND+1,HOUSE_N+1,GhostsSetBlock.of(GhostsSetBlock.Kind.BOOKS_COOK,Direction.SOUTH));
        set(l,b,COAT_HOOK,MarshBlock.of(MarshBlock.Kind.COAT,Direction.NORTH));
        // The kitchen: the counter under the hatch, the sink and the cooker along the east wall, the butcher's block, the lamp.
        set(l,b,HATCH.below().east(),GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER,Direction.WEST));
        set(l,b,8,GROUND,-62,GhostsSetBlock.of(GhostsSetBlock.Kind.SINK,Direction.WEST));
        for(int z:new int[]{-61,-63,-64})set(l,b,8,GROUND,z,GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER,Direction.WEST));
        set(l,b,8,GROUND,-72,GhostsSetBlock.of(GhostsSetBlock.Kind.COOKER,Direction.WEST));
        set(l,b,8,GROUND,-73,GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER,Direction.WEST));
        set(l,b,BUTCHER,MarshBlock.of(MarshBlock.Kind.BUTCHER,Direction.NORTH));
        set(l,b,4,4,-67,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        kitchen(l,b,false);
    }

    /** The table as it is laid before supper: a plate at every chair, the bread board and the jug in the middle. */
    public static void lay(ServerLevel l,BlockPos b){
        for(var seat:chairs())set(l,b,setting(seat),GhostsSetBlock.of(GhostsSetBlock.Kind.PLATE,Direction.NORTH));
        set(l,b,BREAD,MarshBlock.of(MarshBlock.Kind.BREAD,Direction.NORTH));
        set(l,b,JUG,GhostsSetBlock.of(GhostsSetBlock.Kind.JUG,Direction.NORTH));
        set(l,b,ROAST,Blocks.AIR.defaultBlockState());
    }
    /** What the kitchen shows: nothing out of place, or Corinne's keys on the side by the block and her coat over a chair. */
    public static void kitchen(ServerLevel l,BlockPos b,boolean hers){
        set(l,b,KEYS,hers?MarshBlock.of(MarshBlock.Kind.KEYS,Direction.EAST):Blocks.AIR.defaultBlockState());
        set(l,b,COAT_CHAIR,hers?MarshBlock.of(MarshBlock.Kind.COAT,Direction.WEST,1):HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST));
        set(l,b,COAT_HOOK,hers?Blocks.AIR.defaultBlockState():MarshBlock.of(MarshBlock.Kind.COAT,Direction.NORTH));
    }
}
