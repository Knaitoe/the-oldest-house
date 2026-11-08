package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.GrowingBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.350, Growing Things (docs/GROWING_THINGS_0_4_350.md): the Barretts' back yard and house on a Saturday afternoon, years before the
 * filming, under the outside sky. Every position is relative to the base, the ground just inside the House's door; north is -z.
 *
 * <p>The House's door opens in the brick backs of a row of garages onto an alley. Across it, the yards' back fences; the Barretts' is
 * boards topped with pickets, with a garden gate whose latch is held by a loop of twine and the Saturday list tied to its post: the clear
 * decision. Behind it, a family yard: the washing line with the washing on it, a swing, the shed in the corner, a garden bed along the
 * east fence, and shoots coming up in the lawn where nothing was planted, thicker toward the house. The back step is going over. The house
 * is two storeys of clapboard, its rooms painted as they are in the Ghosts Set years later: the kitchen at the back with its window over
 * the yard, the hall and the stairs, the front room; upstairs the girls' room over the kitchen, with a card table by its window, and their
 * parents' room at the front. The neighbours' yards and houses stand either side and the street runs past the front. The way on is the
 * side door of a garage along the alley.
 */
public final class GrowingThingsSite {
    private GrowingThingsSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.GROWING_THINGS;
    public static final int WEST=-30,EAST=30,NORTH=-60;
    /** The alley (z 0 to -4) and the back fences along its north side. */
    public static final int ALLEY_N=-4,FENCE_Z=-5;
    /** The Barretts' yard inside its fences. */
    public static final int YARD_W=-12,YARD_E=12,YARD_S=-6,YARD_N=-29,SIDE_W=-13,SIDE_E=13;
    /** The house: the clapboard shell (x -11..11, z -30..-48), the painted walls inside it, the rooms x -9..9, z -32..-46. Ground floor
     * walked at y 1, upstairs at y 6. */
    public static final int HOUSE_W=-11,HOUSE_E=11,HOUSE_S=-30,HOUSE_N=-48,GROUND=1,UP=6;
    /** The kitchen (x -9..1, z -32..-38), the partition to the hall at x 2, and the wall to the front room at z -39. */
    public static final int KITCHEN_W=-9,KITCHEN_E=1,KITCHEN_S=-32,KITCHEN_N=-38,PARTITION_X=2,KITCHEN_WALL_Z=-39;
    public static final BlockPos GATE=new BlockPos(3,0,-5),NOTE=new BlockPos(4,0,-5),ONWARD=new BlockPos(16,0,1);
    public static final BlockPos BACK_DOOR=new BlockPos(0,1,-30),FRONT_DOOR=new BlockPos(6,1,-48),KITCHEN_DOOR=new BlockPos(2,1,-35),ROOM_DOOR=new BlockPos(2,6,-35);
    /** The back step (three wide), the spade's place beside it once it has been left, and the washing basket. */
    public static final int STEP_Z=-29;
    public static final BlockPos SPADE=new BlockPos(2,0,-29),BASKET=new BlockPos(-2,0,-28);
    /** The kitchen window over the yard, two panes wide; the shoots under its sill outside. */
    public static final List<BlockPos> WINDOW=List.of(new BlockPos(-6,2,-30),new BlockPos(-5,2,-30));
    /** The kitchen table's two halves, by the window and where it is moved to. */
    public static final BlockPos TABLE_AT=new BlockPos(-6,1,-33),TABLE_MOVED=new BlockPos(-5,1,-37);
    /** The kitchen counter by the hall door, where supper waits; an account waits above it. */
    public static final BlockPos COUNTER=new BlockPos(1,1,-38),ENDING=new BlockPos(1,2,-38);
    /** The girls' card table by their window upstairs, and their room. */
    public static final BlockPos UP_TABLE=new BlockPos(-5,6,-33);
    public static final int ROOM_W=-9,ROOM_E=1,ROOM_S=-32,ROOM_N=-38;
    /** The washing line: posts at x -9 and -1, the line between them a block up. */
    public static final int LINE_Z=-12,LINE_W=-8,LINE_E=-2;
    public static final List<Integer> GARMENTS=List.of(-8,-7,-5,-4,-2);
    /** The shed in the yard's west corner, its door on its east side. */
    public static final BlockPos SHED_DOOR=new BlockPos(-9,0,-8),SHED_INSIDE=new BlockPos(-11,0,-7);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean inAlley(Vec3 r){return r.z>FENCE_Z+.5&&r.z<1;}
    public static boolean inYard(Vec3 r){return r.x>=YARD_W&&r.x<YARD_E+1&&r.z<=FENCE_Z&&r.z>HOUSE_S+.5;}
    public static boolean inHouse(Vec3 r){return r.x>HOUSE_W+1&&r.x<HOUSE_E-1&&r.z<HOUSE_S-1&&r.z>HOUSE_N+1&&r.y>.5;}
    public static boolean inKitchen(Vec3 r){return inHouse(r)&&r.y<5&&r.x>=KITCHEN_W&&r.x<KITCHEN_E+1&&r.z<=KITCHEN_S+1&&r.z>KITCHEN_N;}
    public static boolean upstairs(Vec3 r){return inHouse(r)&&r.y>=5.5;}
    public static boolean inRoom(Vec3 r){return upstairs(r)&&r.x>=ROOM_W&&r.x<ROOM_E+1&&r.z<=ROOM_S+1&&r.z>ROOM_N;}
    /** Past the gate: in the yard or the house. */
    public static boolean past(Vec3 r){return inYard(r)||inHouse(r)||r.z<HOUSE_S&&r.x>=HOUSE_W&&r.x<=HOUSE_E;}
    public static boolean nearSteps(Vec3 r){return Math.abs(r.x-.5)<4&&r.z<-24&&r.z>-30;}
    /** The kitchen floor's cells, where the boards lift: everything inside the kitchen walls but the fittings along the west wall and the counter. */
    public static List<BlockPos> floorCells(){
        var out=new ArrayList<BlockPos>();
        for(int x=KITCHEN_W+1;x<=KITCHEN_E;x++)for(int z=KITCHEN_S;z>=KITCHEN_N;z--){if(x==COUNTER.getX()&&z==COUNTER.getZ())continue;out.add(new BlockPos(x,GROUND,z));}
        return out;
    }
    /** The table's two cells from its west half. */
    public static List<BlockPos> table(BlockPos west){return List.of(west,west.east());}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState leaves(){return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    static BlockState clapboard(){return YardRooms.mat("clapboard");}
    static BlockState paint(){return BarrettHomeBlock.of(BarrettHomeBlock.Kind.PAINT);}
    static BlockState shoot(int stage){return GrowingBlock.of(Kind.SHOOT,Direction.NORTH,stage);}

    // ------------------------------------------------------------------------------------------------ the build
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,WEST,-5,NORTH,EAST,16,1,air);
        box(l,b,WEST,-5,NORTH,EAST,-2,1,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,1,Blocks.GRASS_BLOCK.defaultBlockState());
        alley(l,b);
        fences(l,b);
        neighbours(l,b);
        street(l,b);
        house(l,b);
        yard(l,b);
        living(l,b);
    }
    static void alley(ServerLevel l,BlockPos b){
        var rnd=new Random(3640);
        // The garages' brick backs along the south side, the House's door among them; packed gravel down the middle.
        for(int x=WEST;x<=EAST;x++){
            for(int z=0;z>=ALLEY_N;z--)set(l,b,x,-1,z,rnd.nextInt(5)==0?Blocks.COARSE_DIRT.defaultBlockState():z==0||z==ALLEY_N?Blocks.DIRT_PATH.defaultBlockState():Blocks.GRAVEL.defaultBlockState());
            if(Math.abs(x)<=3)continue;
            for(int y=0;y<=3;y++)set(l,b,x,y,1,Blocks.BRICKS.defaultBlockState());
            set(l,b,x,4,1,Blocks.BRICK_SLAB.defaultBlockState());
        }
        // The way on: a garage's side door in the brick, hung by OnwardDoors.
        for(int y=0;y<=1;y++)set(l,b,ONWARD.getX(),y,ONWARD.getZ(),Blocks.AIR.defaultBlockState());
        // Bins along the garages, a drain, weeds at the wall foot.
        for(int x:new int[]{-18,-7,9,22}){set(l,b,x,0,0,Blocks.COMPOSTER.defaultBlockState());set(l,b,x+1,0,0,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));}
        for(int x=WEST;x<=EAST;x+=3)if(Math.abs(x)>4&&rnd.nextInt(2)==0)set(l,b,x,0,ALLEY_N,Blocks.SHORT_GRASS.defaultBlockState());
    }
    static void fences(ServerLevel l,BlockPos b){
        var planks=Blocks.SPRUCE_PLANKS.defaultBlockState();var picket=Blocks.SPRUCE_FENCE.defaultBlockState();
        // The neighbours' back fences, boards two high; the Barretts', boards topped with pickets, so the washing shows over it.
        for(int x=WEST;x<=EAST;x++){
            boolean barretts=x>=SIDE_W&&x<=SIDE_E;
            set(l,b,x,0,FENCE_Z,planks);set(l,b,x,1,FENCE_Z,barretts?picket:planks);
        }
        // The garden gate, its latch held by a loop of twine; the Saturday list tied to its post.
        set(l,b,GATE,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH));
        set(l,b,GATE.getX(),1,GATE.getZ(),air());
        set(l,b,NOTE,GrowingBlock.of(Kind.NOTE,Direction.SOUTH));
        // The side fences between the yards, boards two high, back to the houses.
        for(int z=FENCE_Z;z>=HOUSE_S;z--)for(int x:new int[]{SIDE_W,SIDE_E})for(int y=0;y<=1;y++)set(l,b,x,y,z,planks);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static void neighbours(ServerLevel l,BlockPos b){
        var rnd=new Random(3641);
        // Each side, a neighbour's lawn with a tree and a shed, and the back of their house beyond it.
        for(int side=-1;side<=1;side+=2){
            int x0=side<0?WEST:SIDE_E+1,x1=side<0?SIDE_W-1:EAST;
            tree(l,b,side*21,-15,6,rnd);
            box(l,b,side<0?x0+1:x1-3,0,-7,side<0?x0+3:x1-1,2,-9,Blocks.SPRUCE_PLANKS.defaultBlockState());
            box(l,b,side<0?x0+1:x1-3,3,-7,side<0?x0+3:x1-1,3,-9,Blocks.SPRUCE_SLAB.defaultBlockState());
            for(int x=x0;x<=x1;x++){
                for(int y=0;y<=8;y++)for(int z=HOUSE_S;z>=HOUSE_N;z--)if(x==x0||x==x1||z==HOUSE_S||z==HOUSE_N)set(l,b,x,y,z,y==0?Blocks.STONE_BRICKS.defaultBlockState():clapboard());
                for(int z=HOUSE_S;z>=HOUSE_N;z--)set(l,b,x,9,z,Blocks.DARK_OAK_SLAB.defaultBlockState());
            }
            for(int x=x0+2;x<=x1-2;x+=4)for(int y:new int[]{2,6}){set(l,b,x,y,HOUSE_S,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,HOUSE_N,Blocks.GLASS_PANE.defaultBlockState());}
        }
    }
    static void street(ServerLevel l,BlockPos b){
        // The front: a strip of lawn, the path from the Barretts' front door, the sidewalk, the street, and a hedge across it.
        for(int x=WEST;x<=EAST;x++){
            set(l,b,x,-1,-50,Blocks.SMOOTH_STONE.defaultBlockState());
            for(int z=-51;z>=-55;z--)set(l,b,x,-1,z,z==-53&&Math.floorMod(x,5)!=0?Blocks.YELLOW_CONCRETE.defaultBlockState():Blocks.GRAY_CONCRETE.defaultBlockState());
            set(l,b,x,-1,-56,Blocks.SMOOTH_STONE.defaultBlockState());
            for(int z=-57;z>=NORTH;z--)for(int y=0;y<=2;y++)set(l,b,x,y,z,leaves());
        }
        set(l,b,FRONT_DOOR.getX(),-1,-49,Blocks.SMOOTH_STONE.defaultBlockState());
    }
    static void house(ServerLevel l,BlockPos b){
        var floor=GhostsSetBlock.of(GhostsSetBlock.Kind.FLOOR,Direction.NORTH);var tile=GhostsSetBlock.of(GhostsSetBlock.Kind.TILE,Direction.NORTH);
        var storey=BarrettHomeBlock.of(BarrettHomeBlock.Kind.STOREY);var ceiling=GhostsSetBlock.of(GhostsSetBlock.Kind.CEILING,Direction.NORTH);
        // The shell: clapboard outside, painted walls within it, a stone sill course.
        for(int y=0;y<=10;y++){
            box(l,b,HOUSE_W,y,HOUSE_S,HOUSE_E,y,HOUSE_S,y==0?Blocks.STONE_BRICKS.defaultBlockState():clapboard());
            box(l,b,HOUSE_W,y,HOUSE_N,HOUSE_E,y,HOUSE_N,y==0?Blocks.STONE_BRICKS.defaultBlockState():clapboard());
            box(l,b,HOUSE_W,y,HOUSE_N,HOUSE_W,y,HOUSE_S,y==0?Blocks.STONE_BRICKS.defaultBlockState():clapboard());
            box(l,b,HOUSE_E,y,HOUSE_N,HOUSE_E,y,HOUSE_S,y==0?Blocks.STONE_BRICKS.defaultBlockState():clapboard());
        }
        for(int y=1;y<=9;y++){
            box(l,b,HOUSE_W+1,y,HOUSE_S-1,HOUSE_E-1,y,HOUSE_S-1,paint());box(l,b,HOUSE_W+1,y,HOUSE_N+1,HOUSE_E-1,y,HOUSE_N+1,paint());
            box(l,b,HOUSE_W+1,y,HOUSE_N+1,HOUSE_W+1,y,HOUSE_S-1,paint());box(l,b,HOUSE_E-1,y,HOUSE_N+1,HOUSE_E-1,y,HOUSE_S-1,paint());
        }
        box(l,b,HOUSE_W+2,0,HOUSE_S-2,HOUSE_E-2,0,HOUSE_N+2,floor);
        box(l,b,KITCHEN_W,0,KITCHEN_S,KITCHEN_E,0,KITCHEN_N,tile);
        box(l,b,HOUSE_W+2,1,HOUSE_S-2,HOUSE_E-2,4,HOUSE_N+2,air());
        box(l,b,HOUSE_W+1,5,HOUSE_S-1,HOUSE_E-1,5,HOUSE_N+1,storey);
        box(l,b,HOUSE_W+2,6,HOUSE_S-2,HOUSE_E-2,9,HOUSE_N+2,air());
        box(l,b,HOUSE_W+1,10,HOUSE_S-1,HOUSE_E-1,10,HOUSE_N+1,ceiling);
        // The roof: a low gable along the house, its ridge east to west, the gables boarded.
        for(int i=0;i<=9;i++){
            int y=11+i/2;var stair=Blocks.DARK_OAK_STAIRS.defaultBlockState();
            for(int x=HOUSE_W-1;x<=HOUSE_E+1;x++){
                set(l,b,x,y,HOUSE_S+1-i,i%2==0?stair.setValue(StairBlock.FACING,Direction.NORTH):Blocks.DARK_OAK_PLANKS.defaultBlockState());
                set(l,b,x,y,HOUSE_N-1+i,i%2==0?stair.setValue(StairBlock.FACING,Direction.SOUTH):Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
            for(int z=HOUSE_S-i;z>=HOUSE_N+i;z--)for(int x:new int[]{HOUSE_W,HOUSE_E})if(y>11)set(l,b,x,y-1,z,clapboard());
        }
        // Inner walls: the partition between the kitchen and front room (west) and the hall (east), and the wall between kitchen and front room.
        box(l,b,PARTITION_X,1,HOUSE_S-2,PARTITION_X,4,HOUSE_N+2,paint());box(l,b,PARTITION_X,6,HOUSE_S-2,PARTITION_X,9,HOUSE_N+2,paint());
        box(l,b,HOUSE_W+2,1,KITCHEN_WALL_Z,PARTITION_X-1,4,KITCHEN_WALL_Z,paint());box(l,b,HOUSE_W+2,6,KITCHEN_WALL_Z,PARTITION_X-1,9,KITCHEN_WALL_Z,paint());
        for(var d:List.of(KITCHEN_DOOR,ROOM_DOOR,new BlockPos(PARTITION_X,1,-43)))box(l,b,d.getX(),d.getY(),d.getZ(),d.getX(),d.getY()+1,d.getZ(),air());
        box(l,b,-4,1,KITCHEN_WALL_Z,-4,2,KITCHEN_WALL_Z,air());
        door(l,b,-4,6,KITCHEN_WALL_Z,Direction.SOUTH,false);
        // The back door onto the yard, the front door onto the street (kept shut).
        door(l,b,BACK_DOOR.getX(),1,HOUSE_S,Direction.SOUTH,false);box(l,b,BACK_DOOR.getX(),1,HOUSE_S-1,BACK_DOOR.getX(),2,HOUSE_S-1,air());
        door(l,b,FRONT_DOOR.getX(),1,HOUSE_N,Direction.NORTH,false);box(l,b,FRONT_DOOR.getX(),1,HOUSE_N+1,FRONT_DOOR.getX(),2,HOUSE_N+1,air());
        // Windows: the kitchen's over the yard (the bowing frame); the girls' room above it; the front room and the hall onto the street.
        for(var w:WINDOW){set(l,b,w,GrowingBlock.of(Kind.FRAME,Direction.SOUTH,1));set(l,b,w.getX(),w.getY(),w.getZ()-1,air());}
        for(int x=-5;x<=-4;x++){set(l,b,x,7,HOUSE_S,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,7,HOUSE_S-1,air());}
        for(int x:new int[]{-6,-2,4})for(int y:new int[]{2,7}){set(l,b,x,y,HOUSE_N,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,HOUSE_N+1,air());}
        for(int y:new int[]{2,7}){set(l,b,7,y,HOUSE_S,Blocks.GLASS_PANE.defaultBlockState());set(l,b,7,y,HOUSE_S-1,air());}
        // The stairs up the hall's east side, two wide, from the back of the hall to the landing; the well railed above.
        for(int i=0;i<5;i++)for(int x=8;x<=9;x++)set(l,b,x,1+i,-33-i,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        box(l,b,8,5,-32,9,5,-36,air());
        for(int z=-32;z>=-37;z--)set(l,b,7,6,z,Blocks.OAK_FENCE.defaultBlockState());
        set(l,b,7,6,-37,air());
        // Light: the kitchen, the hall, the front room, the girls' room, the landing.
        for(var at:List.of(new BlockPos(-4,4,-35),new BlockPos(6,4,-40),new BlockPos(-4,4,-43),new BlockPos(-4,9,-35),new BlockPos(5,9,-40),new BlockPos(-4,9,-43)))
            set(l,b,at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12));
        furnish(l,b);
    }
    static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction f,boolean open){
        var s=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,f).setValue(DoorBlock.OPEN,open);
        set(l,b,x,y,z,s);set(l,b,x,y+1,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,BarrettHomeBlock.Kind k,Direction f){
        set(l,b,x,y,z,BarrettHomeBlock.of(k,f));
        for(var c:BarrettHomeBlock.covers(new BlockPos(x,y,z),k,f))set(l,b,c.getX(),c.getY(),c.getZ(),BarrettHomeBlock.of(BarrettHomeBlock.Kind.BED_FILL,f));
    }
    static void furnish(ServerLevel l,BlockPos b){
        // The kitchen: counters, the sink and the cooker along the west wall, the fridge in the corner; the counter by the hall door.
        for(int z:new int[]{-33,-34,-37})set(l,b,KITCHEN_W,1,z,GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER,Direction.EAST));
        set(l,b,KITCHEN_W,1,-35,GhostsSetBlock.of(GhostsSetBlock.Kind.SINK,Direction.EAST));
        set(l,b,KITCHEN_W,1,-36,GhostsSetBlock.of(GhostsSetBlock.Kind.COOKER,Direction.EAST));
        put(l,b,KITCHEN_W,1,KITCHEN_N,BarrettHomeBlock.Kind.FRIDGE,Direction.EAST);put(l,b,KITCHEN_W,2,KITCHEN_N,BarrettHomeBlock.Kind.FRIDGE_TOP,Direction.EAST);
        set(l,b,COUNTER,GrowingBlock.of(Kind.COUNTER,Direction.WEST));
        put(l,b,0,2,KITCHEN_WALL_Z+1,BarrettHomeBlock.Kind.CALENDAR,Direction.SOUTH);
        // The hall: coat hooks and shoes by the front door, the family's photographs.
        put(l,b,9,2,-45,BarrettHomeBlock.Kind.COAT_HOOKS,Direction.WEST);put(l,b,9,1,-46,BarrettHomeBlock.Kind.SHOE_RACK,Direction.WEST);
        put(l,b,3,2,-41,BarrettHomeBlock.Kind.PHOTOS,Direction.EAST);
        // The front room: the sofa, the television, a bookshelf, a plant.
        put(l,b,-6,1,-44,BarrettHomeBlock.Kind.SOFA_A,Direction.NORTH);put(l,b,-5,1,-44,BarrettHomeBlock.Kind.SOFA_B,Direction.NORTH);
        put(l,b,-5,1,-41,BarrettHomeBlock.Kind.TV_STAND,Direction.NORTH);put(l,b,1,1,-46,BarrettHomeBlock.Kind.BOOKSHELF,Direction.WEST);
        put(l,b,-9,1,-46,BarrettHomeBlock.Kind.PLANT,Direction.EAST);
        // Upstairs: the girls' room over the kitchen, two beds, a desk; the card table set by their window. Their parents' room in front.
        for(int x:new int[]{-9,-7}){var bed=Blocks.PINK_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);set(l,b,x,UP,-37,bed.setValue(BedBlock.PART,BedPart.HEAD));set(l,b,x,UP,-36,bed.setValue(BedBlock.PART,BedPart.FOOT));}
        put(l,b,0,UP,-37,BarrettHomeBlock.Kind.DESK_KID,Direction.WEST);put(l,b,-9,7,-34,BarrettHomeBlock.Kind.POSTER_A,Direction.EAST);
        set(l,b,UP_TABLE,GrowingBlock.of(Kind.UP_TABLE,Direction.NORTH,0));set(l,b,UP_TABLE.east(),GrowingBlock.of(Kind.UP_TABLE,Direction.NORTH,1));
        put(l,b,-5,UP,-45,BarrettHomeBlock.Kind.DOUBLE_BED,Direction.SOUTH);put(l,b,-9,UP,-41,BarrettHomeBlock.Kind.DRESSER,Direction.EAST);
    }
    static void yard(ServerLevel l,BlockPos b){
        var rnd=new Random(3642);
        // The washing line's two posts.
        for(int x:new int[]{LINE_W-1,LINE_E+1})for(int y=0;y<=1;y++)set(l,b,x,y,LINE_Z,Blocks.OAK_FENCE.defaultBlockState());
        // The swing on its frame.
        for(int x:new int[]{5,9})for(int y=0;y<=2;y++)set(l,b,x,y,-10,Blocks.OAK_FENCE.defaultBlockState());
        box(l,b,5,3,-10,9,3,-10,Blocks.OAK_SLAB.defaultBlockState());
        set(l,b,7,2,-10,Blocks.CHAIN.defaultBlockState());
        set(l,b,7,1,-10,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        // The shed in the corner, its door on the yard side.
        box(l,b,YARD_W,0,YARD_S,SHED_DOOR.getX(),2,-9,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,YARD_W+1,0,YARD_S-1,SHED_DOOR.getX()-1,1,-8,air());
        box(l,b,YARD_W,3,YARD_S,SHED_DOOR.getX(),3,-9,Blocks.SPRUCE_SLAB.defaultBlockState());
        var shedDoor=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.EAST);
        set(l,b,SHED_DOOR,shedDoor);set(l,b,SHED_DOOR.above(),shedDoor.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        set(l,b,YARD_W+1,0,-8,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
        // The garden bed along the east fence, dug and planted; the shoots in it are not what was planted.
        for(int z=-16;z>=-24;z--)for(int x=9;x<=11;x++){set(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState());
            set(l,b,x,0,z,(x+z)%3==0?shoot(1):Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE,4+rnd.nextInt(3)));}
        box(l,b,8,-1,-15,8,-1,-25,Blocks.COARSE_DIRT.defaultBlockState());
        // The path from the gate to the back step.
        for(int z=FENCE_Z-1;z>=STEP_Z+1;z--)set(l,b,z>-17?GATE.getX():z>-22?2:1,-1,z,Blocks.DIRT_PATH.defaultBlockState());
        set(l,b,BASKET,GrowingBlock.of(Kind.BASKET,Direction.NORTH,0));
    }
    /**
     * Everything the afternoon changes, as it stands at the start of a day: the washing on the line, the table by the window, the floor
     * flat, the frame just bowing, the step bare, no spade, and the shoots coming up in the lawn, thicker toward the house.
     */
    public static void living(ServerLevel l,BlockPos b){
        var rnd=new Random(3643);
        for(int x=LINE_W;x<=LINE_E;x++){int i=GARMENTS.indexOf(x);set(l,b,x,1,LINE_Z,i<0?GrowingBlock.of(Kind.LINE,Direction.NORTH):GrowingBlock.of(Kind.WASHING,Direction.NORTH,new int[]{0,0,1,2,3}[i]));}
        for(var c:table(TABLE_MOVED))set(l,b,c,air());
        var cells=table(TABLE_AT);set(l,b,cells.get(0),GrowingBlock.of(Kind.TABLE,Direction.NORTH,0));set(l,b,cells.get(1),GrowingBlock.of(Kind.TABLE,Direction.NORTH,1));
        for(var c:floorCells())if(!cells.contains(c))set(l,b,c,air());
        for(var w:WINDOW)set(l,b,w,GrowingBlock.of(Kind.FRAME,Direction.SOUTH,1));
        for(int x=-1;x<=1;x++)set(l,b,x,0,STEP_Z,GrowingBlock.of(Kind.STEPS,Direction.NORTH,0));
        set(l,b,SPADE,air());
        set(l,b,BASKET,GrowingBlock.of(Kind.BASKET,Direction.NORTH,0));
        // The shoots: none near the alley, more and taller toward the house; round the line's posts; thick under the kitchen window.
        for(int z=YARD_S;z>=YARD_N;z--)for(int x=YARD_W;x<=YARD_E;x++){
            var at=b.offset(x,0,z);var below=l.getBlockState(at.below());
            if(!below.is(Blocks.GRASS_BLOCK))continue;var here=l.getBlockState(at);
            if(!here.isAir()&&!GrowingBlock.is(here,Kind.SHOOT))continue;
            double t=(YARD_S-z)/(double)(YARD_S-YARD_N);
            if(rnd.nextDouble()<.03+.45*t*t){int stage=Math.min(3,(int)(t*3.2)+rnd.nextInt(2)-(rnd.nextInt(3)==0?1:0));BuildBlocks.set(l,at,shoot(Math.max(0,stage)),F);}
            else if(GrowingBlock.is(here,Kind.SHOOT))BuildBlocks.set(l,at,air(),F);
        }
        for(int x:new int[]{LINE_W-1,LINE_E+1})for(int dz:new int[]{-1,1}){var at=b.offset(x,0,LINE_Z+dz);if(l.getBlockState(at).isAir())BuildBlocks.set(l,at,shoot(1),F);}
        for(var w:WINDOW){set(l,b,w.getX(),0,w.getZ()+1,shoot(3));set(l,b,w.getX(),1,w.getZ()+1,shoot(1));}
    }
    /** Grows the lawn's shoots a stage nearer the house (within {@code reach} of it), as the afternoon goes on. */
    public static void grow(ServerLevel l,BlockPos b,int reach){
        for(int z=HOUSE_S+1;z<=HOUSE_S+reach;z++)for(int x=YARD_W;x<=YARD_E;x++){
            var at=b.offset(x,0,z);var s=l.getBlockState(at);
            if(GrowingBlock.is(s,Kind.SHOOT)&&s.getValue(GrowingBlock.STAGE)<3)BuildBlocks.set(l,at,s.setValue(GrowingBlock.STAGE,s.getValue(GrowingBlock.STAGE)+1),F);
            else if(s.isAir()&&l.getBlockState(at.below()).is(Blocks.GRASS_BLOCK)&&Math.floorMod(x*5+z*3,4)==0)BuildBlocks.set(l,at,shoot(0),F);
        }
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int h,Random rnd){
        for(int y=0;y<h;y++)set(l,b,x,y,z,Blocks.OAK_LOG.defaultBlockState());
        for(int y=3;y<=h;y++){int r=y<h-1?2:1;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y==h)&&Math.abs(dx)+Math.abs(dz)<=r+1&&rnd.nextInt(6)!=0){var at=b.offset(x+dx,y,z+dz);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,leaves(),F);}}
    }
}
