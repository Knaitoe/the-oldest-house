package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYouBlock.Kind.*;

/**
 * 0.4.257, A Place for You (docs/A_PLACE_FOR_YOU_0_4_257.md): Mara's house, built whole, in the evening. Every position is relative
 * to the base; north is into the scene.
 * <p>
 * The reader comes through a door in an old garden wall (z 0) onto a short road (z -1 to -3) that runs east and west into the trees.
 * Across it, a picket fence (z -4) with its gate at x 0 and the mailbox just inside; the front path up the yard to the porch (z -10 to
 * -10) and the front door at (0,0,-12). The house stands from x -10 to 10 and z -11 to -29, clapboard outside and papered walls within, its
 * ground floor at 0 and its upstairs at 5.
 * Downstairs: the hall up the middle (x -2 to 2), its first rows the vestibule with the coat hooks and the hall glass; the sitting room
 * west (x -8 to -4, z -13 to -20) through an archway; Kit's room behind it (z -22 to -27) through Kit's door at (-3,0,-24); the kitchen
 * east (x 4 to 8, z -13 to -22) through an archway, its table of six places in the middle, the counters and the stove along its back,
 * the fridge by the front wall and the screen door east onto the side porch (x 11 to 13, z -17 to -21); the bathroom behind it (z -24
 * to -27) through its door at (3,0,-25); the back door at (0,0,-28) with a cupboard in front of its catch. The stair climbs the hall's
 * east side (x 1 and 2, z -17 to -21) to the landing upstairs, where Mara's room, the spare room and two back rooms stand shut.
 */
public final class PlaceForYouSite {
    private PlaceForYouSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PLACE_FOR_YOU;
    public static final int UP=5;
    // ------------------------------------------------------------------------------------------------ the approach
    public static final BlockPos GATE=new BlockPos(0,0,-4),MAILBOX=new BlockPos(2,0,-5),BAG_GATE=new BlockPos(1,0,-5),FRONT_DOOR=new BlockPos(0,0,-12),BACK_DOOR=new BlockPos(0,0,-28);
    // ------------------------------------------------------------------------------------------------ the kitchen
    /** The table (two wide, three long) and its six places: Mara, Theo's empty chair and Ren on the west side; Kit, the reader and Alex on the east. */
    public static final BlockPos MARA_CHAIR=new BlockPos(4,0,-15),THEO_CHAIR=new BlockPos(4,0,-16),REN_CHAIR=new BlockPos(4,0,-17),
        KIT_CHAIR=new BlockPos(7,0,-15),READER_CHAIR=new BlockPos(7,0,-16),ALEX_CHAIR=new BlockPos(7,0,-17);
    public static final List<BlockPos> CHAIRS=List.of(MARA_CHAIR,THEO_CHAIR,REN_CHAIR,KIT_CHAIR,READER_CHAIR,ALEX_CHAIR);
    /** On the table: the place card at the reader's place (the source), the plates at the others, Theo's kept and set. */
    public static final BlockPos CARD=new BlockPos(6,1,-16),THEO_PLATE=new BlockPos(5,1,-16),MARA_PLATE=new BlockPos(5,1,-15);
    /** The three plates the reader can clear: Kit's, Ren's and Alex's. */
    public static final List<BlockPos> PLATES=List.of(new BlockPos(6,1,-15),new BlockPos(5,1,-17),new BlockPos(6,1,-17));
    /** The counters along the back: the sink, the stove with the kettle on it, the cups at the end, where Kit's things are unpacked. */
    public static final BlockPos SINK=new BlockPos(5,0,-22),STOVE=new BlockPos(7,0,-22),KETTLE=new BlockPos(7,1,-22),CUPS=new BlockPos(8,1,-22),
        SINK_TOP=new BlockPos(5,1,-22),UNPACKED=new BlockPos(4,1,-22),SPOON=new BlockPos(6,1,-22);
    /** Mara's fridge, its top showing the reader's picture. */
    public static final BlockPos FRIDGE=new BlockPos(8,0,-13);
    /** Where Kit's bag waits when it is brought back in: on the floor beside Kit's chair. */
    public static final BlockPos BAG_KITCHEN=new BlockPos(8,0,-15);
    public static final BlockPos SCREEN_DOOR=new BlockPos(9,0,-19),KITCHEN_LIGHT=new BlockPos(6,3,-18);
    // ------------------------------------------------------------------------------------------------ the sitting room and the hall
    public static final BlockPos SOFA=new BlockPos(-8,0,-16),SOFA_B=new BlockPos(-8,0,-17),RADIO=new BlockPos(-8,1,-14),PHOTO=new BlockPos(-6,1,-20),PICTURES=new BlockPos(-6,2,-20);
    public static final BlockPos GLASS=new BlockPos(-2,1,-14),HOOKS=new BlockPos(2,1,-13),KIT_DOOR=new BlockPos(-3,0,-24),BATH_DOOR=new BlockPos(3,0,-25);
    public static final BlockPos HALL_CHAIR=new BlockPos(-1,0,-13);
    // ------------------------------------------------------------------------------------------------ the bathroom
    public static final BlockPos ROUTINE=new BlockPos(5,1,-27),BATH_SINK=new BlockPos(6,0,-27),BATH_MIRROR=new BlockPos(6,1,-27),CABINET=new BlockPos(8,1,-26),TUB=new BlockPos(8,0,-24);
    // ------------------------------------------------------------------------------------------------ upstairs, shut in this slice
    public static final List<BlockPos> SHUT_DOORS=List.of(new BlockPos(-3,UP,-16),new BlockPos(3,UP,-14),new BlockPos(3,UP,-24),new BlockPos(-3,UP,-24));
    /** The road's two ends, where it goes on into the trees. */
    public static boolean roadEnd(Vec3 r){return Math.abs(r.x)>=11&&r.z<-0.5&&r.z>-4&&r.y<3;}
    public static boolean onRoad(Vec3 r){return r.z<-0.5&&r.z>-4&&r.y<3;}
    public static boolean inside(Vec3 r){return r.x>=-18&&r.x<19&&r.z>=-34&&r.z<1&&r.y>=-3&&r.y<19;}
    /** Inside the house itself, either floor. */
    public static boolean inHouse(Vec3 r){return r.x>=-9&&r.x<10&&r.z>=-28.7&&r.z<-11&&r.y>=-.5&&r.y<10;}
    /** Inside the fence: the yard, the porches and the house. */
    public static boolean inYard(Vec3 r){return r.x>=-14&&r.x<16&&r.z<-4.2&&r.z>=-30&&r.y<10;}
    public static boolean inKitchen(Vec3 r){return r.x>=3.5&&r.x<9&&r.z>=-22.7&&r.z<-12&&r.y<4;}
    public static boolean inBathroom(Vec3 r){return r.x>=3.5&&r.x<9&&r.z>=-27.7&&r.z<-23.5&&r.y<4;}
    public static boolean inSitting(Vec3 r){return r.x>=-8.7&&r.x<-3&&r.z>=-20.7&&r.z<-12&&r.y<4;}

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    static BlockState mat(String name){return PlaceForYouRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    static void door(ServerLevel l,BlockPos b,BlockPos at,Block door,Direction facing,boolean open){
        var s=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing);}
    static BlockState barrett(BarrettHomeBlock.Kind kind,Direction facing,boolean lit){return BarrettHomeBlock.of(kind,facing).setValue(BarrettHomeBlock.LIT,lit);}
    static BlockState read(HouseReadPropBlock.Part part,Direction facing){return HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,part).setValue(HorizontalDirectionalBlock.FACING,facing);}
    static BlockState fit(PlaceForYouBlock.Kind kind,Direction facing,int stage){return PlaceForYouBlock.of(kind,facing,stage);}
    static BlockState leaves(Block leaves){return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}

    /** A window through both skins of a wall: the pane in the clapboard, the papered wall's cell open behind it. */
    static void window(ServerLevel l,BlockPos b,int x,int y,int z,Direction out){
        var outer=new BlockPos(x,y,z).relative(out);set(l,b,outer,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,z,Blocks.AIR.defaultBlockState());
    }
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var siding=mat("siding");var paper=mat("paper");var stripe=mat("stripe");var trim=mat("trim");
        // The ground: earth under grass across the whole scene, open to the evening sky.
        box(l,b,-18,-3,-34,18,18,0,air);box(l,b,-18,-3,-34,18,-2,0,Blocks.DIRT.defaultBlockState());box(l,b,-18,-1,-34,18,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        // The garden wall the reader comes through, old stone with a coping; the road across in front of it, into the trees both ways.
        box(l,b,-18,-1,0,18,2,0,Blocks.MOSSY_STONE_BRICKS.defaultBlockState());box(l,b,-18,3,0,18,3,0,Blocks.STONE_BRICK_SLAB.defaultBlockState());
        box(l,b,-17,-1,-3,17,-1,-1,Blocks.DIRT_PATH.defaultBlockState());
        for(int x:new int[]{-18,-17,17,18})for(int z=-3;z<=-1;z++)box(l,b,x,0,z,x,4,z,leaves(Blocks.SPRUCE_LEAVES));
        // The picket fence round the yard, its gate, the lamp on the gatepost, the mailbox (the account waits in it).
        for(int x=-14;x<=15;x++)if(x!=0)set(l,b,x,0,-4,Blocks.BIRCH_FENCE.defaultBlockState());
        for(int z=-5;z>=-30;z--){set(l,b,-14,0,z,Blocks.BIRCH_FENCE.defaultBlockState());set(l,b,15,0,z,Blocks.BIRCH_FENCE.defaultBlockState());}
        for(int x=-14;x<=15;x++)set(l,b,x,0,-30,Blocks.BIRCH_FENCE.defaultBlockState());
        set(l,b,GATE,Blocks.BIRCH_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.SOUTH));
        set(l,b,-1,1,-4,Blocks.LANTERN.defaultBlockState());set(l,b,MAILBOX,read(HouseReadPropBlock.Part.MAILBOX,Direction.SOUTH));
        // The front path, the porch with its posts and roof, the porch light.
        box(l,b,0,-1,-8,0,-1,-5,Blocks.DIRT_PATH.defaultBlockState());box(l,b,-5,-1,-10,5,-1,-9,mat("porch"));
        for(int x:new int[]{-5,5})for(int y=0;y<=2;y++)set(l,b,x,y,-9,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,-5,3,-10,5,3,-9,Blocks.SPRUCE_SLAB.defaultBlockState());set(l,b,1,2,-10,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The house: clapboard outside, papered walls within, the rooms inside those; the floors and the ceilings.
        box(l,b,-10,-1,-29,10,9,-11,siding);box(l,b,-9,-1,-28,9,9,-12,paper);box(l,b,-8,0,-27,8,3,-13,air);box(l,b,-8,UP,-27,8,8,-13,air);
        box(l,b,-8,-1,-27,8,-1,-13,mat("boards"));box(l,b,-8,UP-1,-27,8,UP-1,-13,mat("boards"));box(l,b,-9,9,-28,9,9,-12,mat("boards"));
        // The roof: a gable running front to back in shingle, overhanging the walls, its ends in clapboard.
        for(int i=0;i<=8;i++){box(l,b,-11+i,9+i,-30,-11+i,9+i,-10,mat("shingle"));box(l,b,11-i,9+i,-30,11-i,9+i,-10,mat("shingle"));
            if(i>0){box(l,b,-10+i,9+i,-29,10-i,9+i,-29,siding);box(l,b,-10+i,9+i,-11,10-i,9+i,-11,siding);}}
        box(l,b,-2,17,-30,2,17,-10,mat("shingle"));
        // Ground-floor walls: the hall's two sides, the sitting room's back, the kitchen's back.
        box(l,b,-3,0,-27,-3,3,-13,paper);box(l,b,3,0,-27,3,3,-13,paper);box(l,b,-8,0,-21,-4,3,-21,stripe);box(l,b,4,0,-23,8,3,-23,paper);
        // The front door through both skins, and the back door; the archways into the sitting room and the kitchen; Kit's door and the bathroom's.
        door(l,b,FRONT_DOOR,Blocks.SPRUCE_DOOR,Direction.SOUTH,false);box(l,b,0,0,-11,0,1,-11,air);
        door(l,b,BACK_DOOR,Blocks.SPRUCE_DOOR,Direction.NORTH,false);box(l,b,0,0,-29,0,1,-29,air);
        box(l,b,-3,0,-16,-3,2,-15,air);box(l,b,3,0,-15,3,2,-14,air);
        door(l,b,KIT_DOOR,Blocks.OAK_DOOR,Direction.WEST,false);door(l,b,BATH_DOOR,Blocks.OAK_DOOR,Direction.EAST,false);
        // Windows: two to the front each side, the kitchen east over the side porch, the sitting room west, Kit's room and the bathroom at the back, upstairs at the front.
        for(int y=1;y<=2;y++){
            for(int x:new int[]{-7,-6,-5,5,6,7})window(l,b,x,y,-12,Direction.SOUTH);
            for(int z:new int[]{-15,-16})window(l,b,9,y,z,Direction.EAST);
            for(int z:new int[]{-15,-16,-17})window(l,b,-9,y,z,Direction.WEST);
            for(int x:new int[]{-6,-5,6})window(l,b,x,y,-28,Direction.NORTH);
            for(int x:new int[]{-6,-5,5,6})window(l,b,x,UP+y,-12,Direction.SOUTH);}
        // The stair up the hall's east side: five treads over a trim stringer, its rail stepping up with them, and a well cut through the landing
        // over the lower four, so the top tread sits flush with the landing's boards.
        for(int i=0;i<5;i++){int z=-17-i;for(int x=1;x<=2;x++){set(l,b,x,i,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));for(int u=0;u<i;u++)set(l,b,x,u,z,trim);}
            for(int u=0;u<=i;u++)set(l,b,0,u,z,u==i?Blocks.DARK_OAK_FENCE.defaultBlockState():trim);}
        box(l,b,1,UP-1,-20,2,UP-1,-16,air);
        for(int z=-20;z<=-16;z++)set(l,b,0,UP,z,Blocks.DARK_OAK_FENCE.defaultBlockState());for(int x=0;x<=2;x++)set(l,b,x,UP,-15,Blocks.DARK_OAK_FENCE.defaultBlockState());
        // Upstairs: the landing down the middle, Mara's room and the spare room at the front, two back rooms, every door shut.
        box(l,b,-3,UP,-27,-3,8,-13,paper);box(l,b,3,UP,-27,3,8,-13,paper);box(l,b,-8,UP,-21,-4,8,-21,paper);box(l,b,4,UP,-21,8,8,-21,paper);
        for(var at:SHUT_DOORS)door(l,b,at,Blocks.OAK_DOOR,at.getX()<0?Direction.WEST:Direction.EAST,false);
        set(l,b,-7,UP,-14,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,-7,UP,-15,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,-5,UP,-20,furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));set(l,b,-8,UP,-18,furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST));
        set(l,b,6,UP,-14,Blocks.LIGHT_BLUE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,6,UP,-15,Blocks.LIGHT_BLUE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,8,UP,-19,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST));
        for(var at:new int[][]{{5,-25},{6,-25},{7,-26},{-6,-25},{-7,-26}})set(l,b,at[0],UP,at[1],barrett(BarrettHomeBlock.Kind.BOXES,Direction.SOUTH,false));
        set(l,b,0,UP+3,-24,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The vestibule: the coat hooks, the hall glass, the hall chair, two lights down the hall.
        set(l,b,HOOKS,fit(PlaceForYouBlock.Kind.HOOKS,Direction.WEST,0));glass(l,b,0);
        set(l,b,HALL_CHAIR,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));
        set(l,b,0,3,-15,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));set(l,b,-1,3,-25,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The back passage: the cupboard stood in front of the back door's catch.
        set(l,b,1,0,-27,barrett(BarrettHomeBlock.Kind.SHELVES,Direction.WEST,false));
        // The sitting room: striped paper, the sofa on the west wall, the two armchairs, the radio, the sideboard with Theo's photograph and the household's pictures over it.
        box(l,b,-9,0,-20,-9,3,-13,stripe);box(l,b,-8,0,-12,-4,3,-12,stripe);
        for(int y=1;y<=2;y++){for(int x:new int[]{-7,-6,-5})window(l,b,x,y,-12,Direction.SOUTH);for(int z:new int[]{-15,-16,-17})window(l,b,-9,y,z,Direction.WEST);}
        set(l,b,SOFA,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.EAST));set(l,b,SOFA_B,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.EAST));
        set(l,b,-5,0,-16,furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST));set(l,b,-5,0,-18,furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST));
        set(l,b,RADIO.below(),furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));set(l,b,RADIO,fit(PlaceForYouBlock.Kind.RADIO,Direction.EAST,0));
        set(l,b,PHOTO.below(),furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));set(l,b,PHOTO,fit(PlaceForYouBlock.Kind.PHOTO,Direction.SOUTH,0));
        set(l,b,PICTURES,fit(PlaceForYouBlock.Kind.PICTURES,Direction.SOUTH,0));
        set(l,b,-4,0,-13,barrett(BarrettHomeBlock.Kind.FLOOR_LAMP,Direction.SOUTH,true));set(l,b,-6,3,-17,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // Kit's room: the bed, the desk, the drawers, a light.
        set(l,b,-7,0,-26,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,-7,0,-25,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,-4,0,-27,furniture(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH));set(l,b,-8,0,-22,furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST));
        set(l,b,-6,3,-24,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The kitchen: linoleum, the table and its six chairs, the counters, the stove and the kettle, the cups, the fridge, the screen door.
        box(l,b,4,-1,-22,8,-1,-13,mat("lino"));
        for(int x=5;x<=6;x++)for(int z=-17;z<=-15;z++)set(l,b,x,0,z,fit(TABLE,Direction.NORTH,0));
        for(var chair:CHAIRS)set(l,b,chair,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,chair.getX()<5?Direction.EAST:Direction.WEST));
        for(int x=4;x<=8;x++)set(l,b,x,0,-22,fit(COUNTER,Direction.SOUTH,x==SINK.getX()?1:0));set(l,b,STOVE,fit(PlaceForYouBlock.Kind.STOVE,Direction.SOUTH,1));
        set(l,b,FRIDGE,read(HouseReadPropBlock.Part.FRIDGE,Direction.WEST));set(l,b,FRIDGE.above(),read(HouseReadPropBlock.Part.FRIDGE_TOP,Direction.WEST));
        door(l,b,SCREEN_DOOR,Blocks.BIRCH_DOOR,Direction.EAST,false);box(l,b,10,0,-19,10,1,-19,air);
        set(l,b,KITCHEN_LIGHT,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The side porch off the kitchen: painted boards, a rail on its open sides, its posts and roof.
        box(l,b,11,-1,-21,13,-1,-17,mat("porch"));
        for(int z=-21;z<=-17;z++)if(z!=-19)set(l,b,13,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,11,3,-21,13,3,-17,Blocks.SPRUCE_SLAB.defaultBlockState());for(int z:new int[]{-21,-17})for(int y=1;y<=2;y++)set(l,b,13,y,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        set(l,b,12,2,-19,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The bathroom: tiles, the vanity and its sink and mirror, the tub along the east wall, the toilet, the cabinet on the wall, a light.
        box(l,b,4,-1,-27,8,-1,-24,mat("tile"));box(l,b,4,0,-28,8,3,-28,mat("tile"));window(l,b,6,1,-28,Direction.NORTH);window(l,b,6,2,-28,Direction.NORTH);
        set(l,b,5,0,-27,read(HouseReadPropBlock.Part.VANITY,Direction.SOUTH));set(l,b,BATH_SINK,read(HouseReadPropBlock.Part.SINK,Direction.SOUTH));set(l,b,7,0,-27,read(HouseReadPropBlock.Part.VANITY,Direction.SOUTH));
        set(l,b,BATH_MIRROR,read(HouseReadPropBlock.Part.MIRROR,Direction.SOUTH));set(l,b,BATH_MIRROR.above(),read(HouseReadPropBlock.Part.MIRROR_TOP,Direction.SOUTH));
        set(l,b,TUB,read(HouseReadPropBlock.Part.TUB_FOOT,Direction.EAST));set(l,b,TUB.north(),read(HouseReadPropBlock.Part.TUB_HEAD,Direction.EAST));
        set(l,b,4,0,-26,read(HouseReadPropBlock.Part.TOILET,Direction.EAST));set(l,b,CABINET,read(HouseReadPropBlock.Part.CABINET,Direction.WEST));
        set(l,b,6,3,-25,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The trees all round: spruce and birch, close, so the road seems to go on.
        for(int x=-18;x<=18;x++)for(int z=-34;z<=-31;z++)if(Math.floorMod(x*7+z*3,5)!=0)box(l,b,x,0,z,x,3+((x+z)&1),z,leaves(Math.floorMod(x,3)==0?Blocks.BIRCH_LEAVES:Blocks.SPRUCE_LEAVES));
        for(int z=-30;z<=-5;z++)for(int x:new int[]{-18,-17,-16,16,17,18})if(Math.floorMod(x+z*5,4)!=0||x==18||x==-18)box(l,b,x,0,z,x,3+(z&1),z,leaves(Blocks.SPRUCE_LEAVES));
        for(var at:new int[][]{{-16,-8},{-16,-24},{17,-12},{17,-26},{-6,-32},{7,-32}}){box(l,b,at[0],0,at[1],at[0],4,at[1],Blocks.SPRUCE_LOG.defaultBlockState());box(l,b,at[0]-1,5,at[1]-1,at[0]+1,6,at[1]+1,leaves(Blocks.SPRUCE_LEAVES));set(l,b,at[0],7,at[1],leaves(Blocks.SPRUCE_LEAVES));}
        reset(l,b,new CompoundTag());
    }

    /** The hall glass as the reader's snapshot left it: as it hangs (0), turned to the wallpaper (1) or covered with a cloth (2). */
    public static void glass(ServerLevel l,BlockPos b,int stage){set(l,b,GLASS,fit(PlaceForYouBlock.Kind.GLASS,Direction.EAST,stage));}
    /** The routine item on the bathroom counter, or the counter bare. */
    public static void routine(ServerLevel l,BlockPos b,int item){set(l,b,ROUTINE,item<0?Blocks.AIR.defaultBlockState():fit(PlaceForYouBlock.Kind.ROUTINE,Direction.SOUTH,Math.floorMod(item,16)));}
    public static boolean routineOut(ServerLevel l,BlockPos b){return PlaceForYouBlock.is(l.getBlockState(b.offset(ROUTINE)),PlaceForYouBlock.Kind.ROUTINE);}
    /** Kit's bag at the gate, in the kitchen, or nowhere (carried, or gone with Kit); unpacked onto the counter. */
    public static void bag(ServerLevel l,BlockPos b,String where){
        set(l,b,BAG_GATE,where.equals("gate")?fit(BAG,Direction.SOUTH,0):Blocks.AIR.defaultBlockState());
        set(l,b,BAG_KITCHEN,where.equals("kitchen")?fit(BAG,Direction.WEST,0):Blocks.AIR.defaultBlockState());
        set(l,b,UNPACKED,where.equals("unpacked")?fit(BAG,Direction.SOUTH,2):Blocks.AIR.defaultBlockState());
    }
    /** The table set for supper (the plates the reader may clear among them), or cleared to the sink. */
    public static void table(ServerLevel l,BlockPos b,boolean set){
        for(var at:PLATES)set(l,b,at,set?fit(PLATE,Direction.NORTH,0):Blocks.AIR.defaultBlockState());
        set(l,b,MARA_PLATE,set?fit(PLATE,Direction.NORTH,0):Blocks.AIR.defaultBlockState());
        set(l,b,THEO_PLATE,fit(PLATE,Direction.NORTH,0));
        set(l,b,SINK_TOP,set?Blocks.AIR.defaultBlockState():fit(PLATE,Direction.NORTH,2));
    }
    public static void card(ServerLevel l,BlockPos b,boolean there){set(l,b,CARD,there?fit(PlaceForYouBlock.Kind.CARD,Direction.EAST,0):Blocks.AIR.defaultBlockState());}
    /** Kit's door, or the cupboard door it becomes. */
    public static void kitsDoor(ServerLevel l,BlockPos b,boolean cupboard){
        if(cupboard){set(l,b,KIT_DOOR,fit(CUPBOARD,Direction.EAST,0));set(l,b,KIT_DOOR.above(),fit(CUPBOARD,Direction.EAST,1));}
        else door(l,b,KIT_DOOR,Blocks.OAK_DOOR,Direction.WEST,false);
    }
    public static boolean cupboard(ServerLevel l,BlockPos b){return PlaceForYouBlock.is(l.getBlockState(b.offset(KIT_DOOR)),CUPBOARD);}
    /** Kit's chair at the table, or tucked away. */
    public static void kitsChair(ServerLevel l,BlockPos b,boolean there){set(l,b,KIT_CHAIR,there?furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST):Blocks.AIR.defaultBlockState());}
    public static void cups(ServerLevel l,BlockPos b,boolean poured){set(l,b,CUPS,fit(PlaceForYouBlock.Kind.CUPS,Direction.SOUTH,poured?1:0));}
    public static void kettle(ServerLevel l,BlockPos b,boolean there){set(l,b,KETTLE,there?fit(PlaceForYouBlock.Kind.KETTLE,Direction.SOUTH,0):Blocks.AIR.defaultBlockState());}
    public static void spoon(ServerLevel l,BlockPos b,boolean there){set(l,b,SPOON,there?fit(PlaceForYouBlock.Kind.SPOON,Direction.EAST,0):Blocks.AIR.defaultBlockState());}
    /** The kitchen light: on, or off as the last person leaves it. */
    public static void kitchenLight(ServerLevel l,BlockPos b,boolean on){set(l,b,KITCHEN_LIGHT,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,on));}
    /** The front door held open with the hall chair, or shut and the chair back in the hall. */
    public static void propped(ServerLevel l,BlockPos b,boolean propped){
        door(l,b,FRONT_DOOR,Blocks.SPRUCE_DOOR,Direction.SOUTH,propped);
        set(l,b,HALL_CHAIR,propped?Blocks.AIR.defaultBlockState():furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));
        set(l,b,1,0,-13,propped?furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH):Blocks.AIR.defaultBlockState());
    }
    /** The garden gate, open or shut. */
    public static void gate(ServerLevel l,BlockPos b,boolean open){var at=b.offset(GATE);var s=l.getBlockState(at);if(s.getBlock() instanceof FenceGateBlock&&s.getValue(FenceGateBlock.OPEN)!=open)BuildBlocks.set(l,at,s.setValue(FenceGateBlock.OPEN,open),F);}
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FRONT_DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    public static void frontDoor(ServerLevel l,BlockPos b,boolean open){var s=l.getBlockState(b.offset(FRONT_DOOR));if(s.getBlock() instanceof DoorBlock d)d.setOpen(null,l,s,b.offset(FRONT_DOOR),open);}
    /**
     * The reader's picture on Mara's fridge, from their own kitchen drawing (face, hands, hair, clothes, the face the pencil drew and
     * the body), or a pencil figure with no face if there is none to copy; taken down, the fridge door is bare.
     */
    public static void picture(ServerLevel l,BlockPos b,CompoundTag drawing,boolean up){
        var top=b.offset(FRIDGE.above());var s=l.getBlockState(top);if(!s.is(HouseReadRegistry.FITTING.get()))return;
        BuildBlocks.set(l,top,s.setValue(HouseReadPropBlock.TURNED,up),F);
        if(l.getBlockEntity(top) instanceof HouseReadPropBlock.Fitting door){var read=drawing.isEmpty()?HouseReadKitchen.blank():drawing.copy();door.setRead(read);}
    }
    public static boolean pictureUp(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FRIDGE.above()));return s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.TURNED);}
    /** Puts the house back as the evening begins: the table set, the card at the reader's place, the bag nowhere yet, Kit's door a door, the lights on. */
    public static void reset(ServerLevel l,BlockPos b,CompoundTag drawing){
        table(l,b,true);card(l,b,true);bag(l,b,"none");kitsDoor(l,b,false);kitsChair(l,b,true);cups(l,b,false);kettle(l,b,true);spoon(l,b,false);
        kitchenLight(l,b,true);propped(l,b,false);routine(l,b,-1);picture(l,b,drawing,true);gate(l,b,false);
    }
}
