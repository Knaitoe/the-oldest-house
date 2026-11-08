package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonBlock.Kind.*;

/**
 * 0.4.274, Second Person (docs/SECOND_PERSON_0_4_274.md): the street and the flat above the launderette, built whole. Every position is
 * relative to the base; north is into the scene.
 * <p>
 * The reader comes out of a front door in a terrace (z 0) onto its pavement (z -1, -2), steps down to the road (z -4 to -7) and up to the
 * far pavement (z -8 to -11), where the lamp post, the street tree and the bus stop are. Across it stands the building, from x -9 to 8 and
 * z -12 to -31: the launderette on the ground floor (x -8 to 2, z -13 to -20), shut and dark; the side door at (5,0,-12) and the stair up
 * the east side (x 4 and 5, z -16 to -20) to the landing (x 4 to 7, z -21 to -30), with the flat's door at (3,5,-27) and Iris's at
 * (5,5,-31). The flat is the first floor (standing height 5), double-skinned (brick outside, paper or tile within), its rooms inside
 * x -7 to 2 and z -14 to -30: the sitting room across the front (z -14 to -19) with the street window; the hall up the east side (x 1, 2)
 * with the coat hook and the hall glass; the kitchen (x -7 to -1, z -21 to -23) with its window west; the bathroom (x -3 to -1, z -25 to
 * -27) off the hall and the bedroom (x -7 to -5, z -25 to -27) off the back passage (z -29); the airing cupboard at the passage's west
 * end, its tank set into the back wall, which is also the thin wall to Iris's flat behind.
 */
public final class SecondPersonSite {
    private SecondPersonSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_PERSON;
    public static final int UP=5;
    // ------------------------------------------------------------------------------------------------ the street
    public static final BlockPos STREET_DOOR=new BlockPos(5,0,-12),LAUNDERETTE_DOOR=new BlockPos(1,0,-12),LAMP_POST=new BlockPos(-7,0,-9),TREE=new BlockPos(-1,0,-10);
    /** The bus stop's flag and its bench, where the book waits at the end. */
    public static final BlockPos STOP=new BlockPos(8,0,-9),BENCH=new BlockPos(10,0,-10),BENCH_BOOK=new BlockPos(10,0,-10);
    // ------------------------------------------------------------------------------------------------ the landing
    public static final BlockPos FLAT_DOOR=new BlockPos(3,UP,-27),IRIS_DOOR=new BlockPos(5,UP,-31),LANDING=new BlockPos(5,UP,-27);
    // ------------------------------------------------------------------------------------------------ the sitting room
    public static final BlockPos TV=new BlockPos(-7,UP,-17),ARMCHAIR=new BlockPos(-4,UP,-17),SOFA_A=new BlockPos(-5,UP,-19),SOFA_B=new BlockPos(-4,UP,-19);
    public static final BlockPos PHONE_TABLE=new BlockPos(-7,UP,-19),PHONE=new BlockPos(-7,UP+1,-19),LAMP_TABLE=new BlockPos(-3,UP,-19),LAMP=new BlockPos(-3,UP+1,-19);
    /** The bookcase by the front wall; the book on its east end, the photograph's place on its west end. */
    public static final BlockPos SHELF_A=new BlockPos(0,UP,-14),SHELF_B=new BlockPos(1,UP,-14),BOOK=new BlockPos(1,UP+1,-14),PHOTO_HOME=new BlockPos(0,UP+1,-14);
    /** The street window, from inside: four panes wide at x -5 to -2, two high. */
    public static final int WINDOW_X0=-5,WINDOW_X1=-2;
    // ------------------------------------------------------------------------------------------------ the boxes, and what is in them
    public static final BlockPos PHOTO_BOX=new BlockPos(-1,UP,-17),COAT_BOX=new BlockPos(0,UP,-18),DRAWING_BOX=new BlockPos(-2,UP,-21),LAST_BOX=new BlockPos(2,UP,-16);
    // ------------------------------------------------------------------------------------------------ the hall
    public static final BlockPos GLASS=new BlockPos(2,UP+1,-23),HOOK=new BlockPos(2,UP,-25),MAT=new BlockPos(2,UP,-27);
    // ------------------------------------------------------------------------------------------------ the kitchen
    public static final BlockPos TABLE=new BlockPos(-6,UP,-22),CLOTH=new BlockPos(-6,UP+1,-22),RESIDENT_CHAIR=new BlockPos(-7,UP,-22),GUEST_CHAIR=new BlockPos(-5,UP,-22);
    public static final BlockPos SINK=new BlockPos(-3,UP,-23),TAP=new BlockPos(-3,UP+1,-23),FRIDGE=new BlockPos(-1,UP,-23),ROTA=new BlockPos(-2,UP+1,-21),RADIO=new BlockPos(-4,UP+1,-23);
    /** The kitchen's window behind the resident's chair, from inside (dark after evening): the pane is in the brick at x -9. */
    public static final BlockPos KITCHEN_WINDOW=new BlockPos(-8,UP+1,-22);
    // ------------------------------------------------------------------------------------------------ the bathroom and the bedroom
    public static final BlockPos TUB=new BlockPos(-3,UP,-25),BASIN=new BlockPos(-2,UP,-27),MIRROR=new BlockPos(-2,UP+1,-27),ROUTINE_HOME=new BlockPos(-1,UP+1,-27);
    public static final BlockPos BATH_DOOR=new BlockPos(0,UP,-26),KITCHEN_DOOR=new BlockPos(0,UP,-22),BED_DOOR=new BlockPos(-5,UP,-28),BED_HEAD=new BlockPos(-7,UP,-26),BED_FOOT=new BlockPos(-6,UP,-26);
    public static final BlockPos BEDSIDE=new BlockPos(-7,UP,-25),CHEST=new BlockPos(-5,UP,-25);
    // ------------------------------------------------------------------------------------------------ the airing cupboard
    public static final BlockPos CUPBOARD_DOOR=new BlockPos(-6,UP,-29),CUPBOARD=new BlockPos(-7,UP,-29),TANK=new BlockPos(-7,UP,-30);
    // ------------------------------------------------------------------------------------------------ 0.4.369, winter
    /** The hall table by the kitchen door, where the post goes unopened, and the ticket on it; the hall chair under the glass's corner. */
    public static final BlockPos HALL_TABLE=new BlockPos(2,UP,-22),TICKET_HOME=new BlockPos(2,UP+1,-22),HALL_CHAIR=new BlockPos(2,UP,-24);
    /** Iris's tin, on the kitchen counter by the fridge; the vanity under the basin, whose drawer the routine goes into in the worst of it. */
    public static final BlockPos TIN_AT=new BlockPos(-2,UP+1,-23),VANITY=new BlockPos(-1,UP,-27);
    /** The sleeve laid on the armchair's arm stands in the cell above the armchair. */
    public static final BlockPos ARM=ARMCHAIR.above();

    public static boolean inside(Vec3 r){return r.x>=-17&&r.x<18&&r.z>=-32&&r.z<1&&r.y>=-3&&r.y<13;}
    /** Inside the flat itself (not the landing or the stair). */
    public static boolean inFlat(Vec3 r){return r.x>=-7&&r.x<3&&r.z>=-30&&r.z<-13&&r.y>=UP-.5&&r.y<UP+3;}
    public static boolean inSitting(Vec3 r){return inFlat(r)&&r.z>=-20&&r.z<-13;}
    public static boolean inHall(Vec3 r){return inFlat(r)&&r.x>=1&&r.z<-20;}
    public static boolean inKitchen(Vec3 r){return inFlat(r)&&r.x<0&&r.z>=-24&&r.z<-20;}
    public static boolean inBathroom(Vec3 r){return inFlat(r)&&r.x>=-3&&r.x<0&&r.z>=-28&&r.z<-24;}
    public static boolean inBedroom(Vec3 r){return inFlat(r)&&r.x<-4&&r.z>=-28&&r.z<-24;}
    public static boolean inCupboard(Vec3 r){return r.x>=-7&&r.x<-6&&r.z>=-30&&r.z<-28&&r.y>=UP-.5&&r.y<UP+3;}
    /** On the street (either pavement or the road), outside the building. */
    public static boolean onStreet(Vec3 r){return r.z<-.2&&r.z>-11.8&&r.y<3;}
    /** The bedroom doorway, where a reader standing is "in the doorway". */
    public static boolean inBedroomDoorway(Vec3 r){return Math.abs(r.x-(BED_DOOR.getX()+.5))<.9&&r.z>=-29.5&&r.z<-26.5&&r.y>=UP-.5&&r.y<UP+3;}
    /** The thin wall to Iris's flat: the passage's back wall. */
    public static boolean irisWall(BlockPos rel){return rel.getZ()==-30&&rel.getX()>=-5&&rel.getX()<=2&&rel.getY()>=UP&&rel.getY()<=UP+2;}
    /** By the front door, where a thing set down cannot be missed on the way out (the mat and the floor round it). */
    public static boolean byTheDoor(BlockPos rel){return rel.getX()>=1&&rel.getX()<=2&&rel.getZ()<=-26&&rel.getZ()>=-28&&rel.getY()==UP;}

    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    static BlockState mat(String name){return SecondPersonRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    static void door(ServerLevel l,BlockPos b,BlockPos at,Block door,Direction facing,boolean open){
        var s=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseholdFurnitureBlock.state(kind,facing);}
    static BlockState tract(TractHouseBlock.Kind kind,Direction facing){return TractHouseBlock.of(kind,facing);}
    static BlockState read(HouseReadPropBlock.Part part,Direction facing){return HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,part).setValue(HorizontalDirectionalBlock.FACING,facing);}
    static BlockState fit(SecondPersonBlock.Kind kind,Direction facing,int stage){return SecondPersonBlock.of(kind,facing,stage);}
    static BlockState light(){return BarrettHomeBlock.of(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH).setValue(BarrettHomeBlock.LIT,true);}
    static BlockState leaves(Block leaves){return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    /** A window through both skins: the pane in the outer brick, the inner skin's cell open behind it. */
    static void window(ServerLevel l,BlockPos b,int x,int y,int z,Direction out){var outer=new BlockPos(x,y,z).relative(out);set(l,b,outer,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,z,Blocks.AIR.defaultBlockState());}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var brick=mat("brick");var paper=mat("paper");var stone=Blocks.STONE.defaultBlockState();
        // The ground and the open sky; stone under everything.
        box(l,b,-17,-3,-31,17,12,0,air);box(l,b,-17,-3,-31,17,-2,0,stone);
        // The terrace the reader comes out of: stock brick, its front doors and dark windows, a parapet.
        box(l,b,-17,-1,0,17,9,0,brick);box(l,b,-17,10,0,17,10,0,Blocks.STONE_BRICK_SLAB.defaultBlockState());
        for(int x:new int[]{-12,6,12}){door(l,b,new BlockPos(x,0,0),Blocks.IRON_DOOR,Direction.NORTH,false);set(l,b,x,2,0,Blocks.BLACK_STAINED_GLASS.defaultBlockState());}
        for(int x:new int[]{-15,-14,-9,-8,-3,-2,2,3,8,9,14,15})for(int y:new int[]{1,2,5,6,7})set(l,b,x,y,0,Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        for(int x=-16;x<=16;x+=4)set(l,b,x,4,0,Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        // Its pavement, the kerb, the road a step down, the far kerb and pavement.
        box(l,b,-16,-1,-2,16,-1,-1,mat("paving"));box(l,b,-16,-1,-3,16,-1,-3,mat("kerb"));box(l,b,-16,-2,-7,16,-2,-4,mat("road"));box(l,b,-16,-1,-7,16,-1,-4,air);
        box(l,b,-16,-1,-8,16,-1,-8,mat("kerb"));box(l,b,-16,-1,-11,16,-1,-9,mat("paving"));
        for(int x=-15;x<=15;x+=3)set(l,b,x,-2,-5,Blocks.WHITE_CONCRETE.defaultBlockState());
        // The street goes on past both ends, between walls of other houses.
        box(l,b,-17,-1,-31,-17,9,-1,brick);box(l,b,17,-1,-31,17,9,-1,brick);box(l,b,-16,-1,-31,16,-1,-12,stone);
        // The lamp post, the street tree in its square of earth, the bus stop and its bench.
        for(int y=0;y<=3;y++)set(l,b,LAMP_POST.above(y),Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());set(l,b,LAMP_POST.above(4),Blocks.LANTERN.defaultBlockState());
        set(l,b,TREE.below(),Blocks.COARSE_DIRT.defaultBlockState());for(int y=0;y<=3;y++)set(l,b,TREE.above(y),Blocks.OAK_LOG.defaultBlockState());
        set(l,b,STOP,fit(SecondPersonBlock.Kind.STOP,Direction.SOUTH,0));for(int x=9;x<=11;x++)set(l,b,x,0,-10,Blocks.SPRUCE_SLAB.defaultBlockState());
        // The building: brick all round, the flat's inner skins, its rooms, the launderette below and the stair hall.
        box(l,b,-9,-1,-31,8,9,-12,brick);box(l,b,-8,0,-30,2,8,-13,paper);
        // The launderette: dark, its machines along the back, a bench, the shopfront and its fascia; a ceiling under the flat's floor.
        box(l,b,-8,0,-20,2,2,-13,air);box(l,b,-8,-1,-20,2,-1,-13,mat("launderette"));box(l,b,-8,3,-20,2,3,-13,mat("ceiling"));
        for(int x=-7;x<=1;x++)set(l,b,x,0,-20,furniture(HouseholdFurnitureBlock.Kind.WASHING_MACHINE,Direction.SOUTH));
        for(int x=-6;x<=-3;x++)set(l,b,x,0,-15,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        box(l,b,-8,0,-12,2,0,-12,mat("shopfront"));box(l,b,-7,1,-12,0,2,-12,Blocks.GLASS_PANE.defaultBlockState());box(l,b,-8,3,-12,2,3,-12,mat("shopfront"));
        door(l,b,LAUNDERETTE_DOOR,Blocks.IRON_DOOR,Direction.SOUTH,false);
        for(int i=0;i<3;i++)set(l,b,-5+i,3,-11,fit(SecondPersonBlock.Kind.SIGN,Direction.SOUTH,i));
        // The stair hall: the street door, the stair up the east side, the cupboard under the landing, the landing and its rails, its light.
        box(l,b,4,0,-30,7,8,-13,air);box(l,b,4,-1,-30,7,-1,-13,Blocks.SPRUCE_PLANKS.defaultBlockState());
        door(l,b,STREET_DOOR,Blocks.SPRUCE_DOOR,Direction.SOUTH,false);set(l,b,5,2,-12,Blocks.GLASS_PANE.defaultBlockState());
        for(int i=0;i<5;i++){int z=-16-i;for(int x=4;x<=5;x++){set(l,b,x,i,z,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));for(int u=0;u<i;u++)set(l,b,x,u,z,Blocks.SPRUCE_PLANKS.defaultBlockState());}}
        box(l,b,6,0,-20,7,3,-16,paper);box(l,b,4,0,-30,5,3,-21,paper);box(l,b,6,0,-30,7,3,-21,paper);
        box(l,b,4,UP-1,-30,5,UP-1,-21,Blocks.SPRUCE_PLANKS.defaultBlockState());box(l,b,6,UP-1,-30,7,UP-1,-16,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int z=-19;z<=-16;z++)set(l,b,6,UP,z,Blocks.SPRUCE_FENCE.defaultBlockState());for(int x=6;x<=7;x++)set(l,b,x,UP,-15,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,4,9,-30,7,9,-13,mat("ceiling"));set(l,b,5,8,-25,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        door(l,b,FLAT_DOOR,Blocks.SPRUCE_DOOR,Direction.WEST,false);door(l,b,IRIS_DOOR,Blocks.DARK_OAK_DOOR,Direction.SOUTH,false);
        // The flat: carpet over its floor (lino in the kitchen and bathroom), the artex ceiling, a flat roof behind the parapet.
        box(l,b,-7,UP,-30,2,UP+2,-14,air);box(l,b,-7,UP-1,-30,2,UP-1,-14,mat("carpet"));box(l,b,-7,UP+3,-30,2,UP+3,-14,mat("ceiling"));
        box(l,b,-9,9,-31,8,9,-12,mat("road"));box(l,b,-9,10,-12,8,10,-12,brick);
        // Its walls: the sitting room's back (doorway at the hall), the hall's west wall, the kitchen's north wall, the rooms' north wall, the bedroom/bathroom partition.
        box(l,b,-7,UP,-20,0,UP+2,-20,paper);box(l,b,0,UP,-30,0,UP+2,-21,paper);box(l,b,-7,UP,-24,-1,UP+2,-24,paper);box(l,b,-7,UP,-28,-1,UP+2,-28,paper);box(l,b,-4,UP,-27,-4,UP+2,-25,paper);
        // The back passage and the hall's end open into each other; the passage's back wall is the inner skin.
        box(l,b,0,UP,-29,0,UP+1,-29,air);
        // Doors: the kitchen's and the bathroom's off the hall, the bedroom's off the passage, the airing cupboard's at the passage's end.
        door(l,b,KITCHEN_DOOR,Blocks.OAK_DOOR,Direction.EAST,false);door(l,b,BATH_DOOR,Blocks.OAK_DOOR,Direction.EAST,false);door(l,b,BED_DOOR,Blocks.OAK_DOOR,Direction.NORTH,false);door(l,b,CUPBOARD_DOOR,Blocks.OAK_DOOR,Direction.EAST,false);
        // Windows: the street window, the kitchen's and the bedroom's to the west.
        for(int y=UP+1;y<=UP+2;y++){for(int x=WINDOW_X0;x<=WINDOW_X1;x++)window(l,b,x,y,-13,Direction.SOUTH);window(l,b,-8,y,-22,Direction.WEST);window(l,b,-8,y,-26,Direction.WEST);}
        // The sitting room: the television, the armchair facing it, the sofa, the phone on its table, the lamp on another, the bookcase, a radiator, the light.
        set(l,b,TV,tract(TractHouseBlock.Kind.TV_OFF,Direction.EAST));set(l,b,ARMCHAIR,furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST));
        set(l,b,SOFA_A,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.SOUTH));set(l,b,SOFA_B,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.SOUTH));
        set(l,b,PHONE_TABLE,read(HouseReadPropBlock.Part.SIDE_TABLE,Direction.EAST));set(l,b,LAMP_TABLE,read(HouseReadPropBlock.Part.SIDE_TABLE,Direction.SOUTH));
        set(l,b,SHELF_A,fit(SHELF,Direction.NORTH,0));set(l,b,SHELF_B,fit(SHELF,Direction.NORTH,0));
        set(l,b,-3,UP,-14,read(HouseReadPropBlock.Part.RADIATOR,Direction.NORTH));set(l,b,-3,UP+2,-16,light());
        // The hall: the mat inside the door, its light; 0.4.369, a table by the kitchen door and a chair against the wall.
        set(l,b,1,UP+2,-24,light());set(l,b,HALL_TABLE,read(HouseReadPropBlock.Part.SIDE_TABLE,Direction.WEST));set(l,b,HALL_CHAIR,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        // The kitchen: tiles, lino, the table for two under its window, the counters with the sink and its tap, the fridge, cupboards over, the light.
        box(l,b,-7,UP-1,-23,-1,UP-1,-21,mat("lino"));box(l,b,-8,UP,-23,-8,UP+2,-21,mat("kitchen"));box(l,b,-7,UP,-24,-1,UP+2,-24,mat("kitchen"));
        window(l,b,-8,UP+1,-22,Direction.WEST);window(l,b,-8,UP+2,-22,Direction.WEST);
        set(l,b,TABLE,furniture(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH));set(l,b,RESIDENT_CHAIR,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));set(l,b,GUEST_CHAIR,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        set(l,b,-4,UP,-23,tract(TractHouseBlock.Kind.COUNTER,Direction.SOUTH));set(l,b,SINK,tract(TractHouseBlock.Kind.COUNTER_SINK,Direction.SOUTH));set(l,b,-2,UP,-23,tract(TractHouseBlock.Kind.COUNTER,Direction.SOUTH));
        set(l,b,TAP,read(HouseReadPropBlock.Part.TAP,Direction.SOUTH));set(l,b,FRIDGE,read(HouseReadPropBlock.Part.FRIDGE,Direction.SOUTH));set(l,b,FRIDGE.above(),read(HouseReadPropBlock.Part.FRIDGE_TOP,Direction.SOUTH));
        set(l,b,-4,UP+2,-23,tract(TractHouseBlock.Kind.WALL_CABINET,Direction.SOUTH));set(l,b,-2,UP+2,-23,tract(TractHouseBlock.Kind.WALL_CABINET,Direction.SOUTH));set(l,b,-4,UP+2,-21,light());
        // The bathroom: tiles, lino, the tub along the west wall, the toilet, the basin and the cupboard beside it under the mirror cabinet, the light.
        box(l,b,-3,UP-1,-27,-1,UP-1,-25,mat("lino"));box(l,b,-4,UP,-27,-4,UP+2,-25,mat("tile"));box(l,b,-3,UP,-28,-1,UP+2,-28,mat("tile"));
        set(l,b,TUB,read(HouseReadPropBlock.Part.TUB_FOOT,Direction.WEST));set(l,b,TUB.north(),read(HouseReadPropBlock.Part.TUB_HEAD,Direction.WEST));
        set(l,b,-3,UP,-27,read(HouseReadPropBlock.Part.TOILET,Direction.EAST));set(l,b,BASIN,read(HouseReadPropBlock.Part.SINK,Direction.SOUTH));set(l,b,-1,UP,-27,read(HouseReadPropBlock.Part.VANITY,Direction.SOUTH));
        set(l,b,MIRROR,fit(SecondPersonBlock.Kind.MIRROR,Direction.SOUTH,0));set(l,b,-2,UP+2,-26,light());
        // The bedroom: the bed under the window, the bedside table, the chest of drawers, the light.
        set(l,b,BED_FOOT,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.WEST).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,BED_HEAD,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.WEST).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,BEDSIDE,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));set(l,b,CHEST,furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST));set(l,b,-6,UP+2,-26,light());
        // The airing cupboard: its tank in the back wall, warm.
        set(l,b,TANK,fit(SecondPersonBlock.Kind.TANK,Direction.SOUTH,0));set(l,b,TANK.above(),fit(SecondPersonBlock.Kind.TANK,Direction.SOUTH,1));
        // 0.4.283: the street as a British terrace (SecondPersonStreet).
        SecondPersonStreet.dress(l,b);
        reset(l,b,new CompoundTag(),"");
        RoomActors.settlePanes(l,b,PLACE);
    }

    // ------------------------------------------------------------------------------------------------ the year's changes
    /**
     * The flat as late summer begins: boxes, three of them taped and one coming up the stairs; the hook empty, the fridge bare, no glass
     * on the wall, the book on the bookcase; the phone on its hook, the lamp off; the street in leaf.
     */
    public static void reset(ServerLevel l,BlockPos b,CompoundTag drawing,String coat){
        for(var at:List.of(PHOTO_BOX,COAT_BOX,DRAWING_BOX))set(l,b,at,fit(BOX,Direction.NORTH,0));set(l,b,LAST_BOX,Blocks.AIR.defaultBlockState());
        set(l,b,HOOK,read(HouseReadPropBlock.Part.EMPTY_HANGER,Direction.WEST));picture(l,b,drawing,false);set(l,b,GLASS,Blocks.AIR.defaultBlockState());
        set(l,b,BOOK,fit(SecondPersonBlock.Kind.BOOK,Direction.NORTH,0));set(l,b,PHOTO_HOME,Blocks.AIR.defaultBlockState());set(l,b,ROUTINE_HOME,Blocks.AIR.defaultBlockState());
        phone(l,b,false);lamp(l,b,false);mirror(l,b,false);set(l,b,CLOTH,Blocks.AIR.defaultBlockState());set(l,b,ROTA,Blocks.AIR.defaultBlockState());set(l,b,RADIO,Blocks.AIR.defaultBlockState());
        set(l,b,BENCH_BOOK,Blocks.SPRUCE_SLAB.defaultBlockState());set(l,b,TIN_AT,Blocks.AIR.defaultBlockState());set(l,b,TICKET_HOME,Blocks.AIR.defaultBlockState());set(l,b,ARM,Blocks.AIR.defaultBlockState());season(l,b,0);
    }
    /** The street through the window: in leaf (0), autumn with leaves down (1), winter, bare, with snow lying (2), the last morning, bare (3). */
    public static void season(ServerLevel l,BlockPos b,int season){
        var crown=season==0?leaves(Blocks.OAK_LEAVES):season==1?leaves(Blocks.DARK_OAK_LEAVES):Blocks.AIR.defaultBlockState();
        snow(l,b,season==2);
        // 0.4.283: the crown rounded, its eight corners left open.
        for(int x=-2;x<=0;x++)for(int z=-11;z<=-9;z++)for(int y=3;y<=5;y++){if(x==-1&&z==-10&&y==3)continue;if(x!=-1&&z!=-10&&y!=4){set(l,b,x,y,z,Blocks.AIR.defaultBlockState());continue;}boolean edge=Math.abs(x+1)+Math.abs(z+10)+Math.abs(y-4)>2;set(l,b,x,y,z,season==1&&edge&&Math.floorMod(x*7+z*3+y,3)==0?Blocks.AIR.defaultBlockState():crown);}
        for(int x=-4;x<=3;x++)for(int z=-11;z<=-9;z++)if(x!=-1||z!=-10){var at=b.offset(x,0,z);if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.BROWN_CARPET))set(l,b,x,0,z,season==1&&Math.floorMod(x*5+z*3,4)==0?Blocks.BROWN_CARPET.defaultBlockState():Blocks.AIR.defaultBlockState());}
        // Inside: the rota on the kitchen wall and the radio on the counter from autumn; the boxes flattened and put out of the way.
        set(l,b,ROTA,season>=1?fit(SecondPersonBlock.Kind.ROTA,Direction.NORTH,0):Blocks.AIR.defaultBlockState());
        set(l,b,RADIO,season>=1?read(HouseReadPropBlock.Part.RADIO,Direction.SOUTH):Blocks.AIR.defaultBlockState());
        if(season>=1){for(var at:List.of(PHOTO_BOX,COAT_BOX,DRAWING_BOX,LAST_BOX))set(l,b,at,Blocks.AIR.defaultBlockState());for(int x=-6;x<=-5;x++)for(int z=-16;z<=-15;z++)set(l,b,x,UP,z,Blocks.BROWN_CARPET.defaultBlockState());}
    }
    /** 0.4.369: snow lying on both pavements in winter, in drifts against the walls, and gone again by the last morning. */
    static void snow(ServerLevel l,BlockPos b,boolean lying){
        for(int x=-16;x<=16;x++)for(int z:new int[]{-1,-2,-9,-10,-11}){
            var at=b.offset(x,0,z);var s=l.getBlockState(at);
            boolean drift=z==-1||z==-11||Math.floorMod(x*7+z*3,5)!=0;
            if(lying&&drift&&s.isAir()&&l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))BuildBlocks.set(l,at,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,z==-1||z==-11?2:1),F);
            else if(!lying&&s.is(Blocks.SNOW))BuildBlocks.set(l,at,Blocks.AIR.defaultBlockState(),F);
        }
    }
    /** Iris's sewing tin: none (-1), shut (0), open with the half-finished sleeve in it (1), open without it (2). */
    public static void tin(ServerLevel l,BlockPos b,int stage){set(l,b,TIN_AT,stage<0?Blocks.AIR.defaultBlockState():fit(SecondPersonBlock.Kind.TIN,Direction.SOUTH,stage));}
    public static int tin(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(TIN_AT));return SecondPersonBlock.is(s,SecondPersonBlock.Kind.TIN)?s.getValue(SecondPersonBlock.STAGE):-1;}
    /** The sleeve on the armchair's arm: half-finished on its needles (0), or finished and folded on the seat (1), or not there (-1). */
    public static void sleeve(ServerLevel l,BlockPos b,int stage){set(l,b,ARM,stage<0?Blocks.AIR.defaultBlockState():fit(SecondPersonBlock.Kind.SLEEVE,Direction.WEST,stage));}
    public static int sleeve(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(ARM));return SecondPersonBlock.is(s,SecondPersonBlock.Kind.SLEEVE)?s.getValue(SecondPersonBlock.STAGE):-1;}
    /** The reader's picture on the fridge (their own kitchen drawing, or a blank sheet), or the fridge bare. */
    public static void picture(ServerLevel l,BlockPos b,CompoundTag drawing,boolean up){
        var top=b.offset(FRIDGE.above());var s=l.getBlockState(top);if(!s.is(HouseReadRegistry.FITTING.get()))return;
        BuildBlocks.set(l,top,s.setValue(HouseReadPropBlock.TURNED,up),F);
        if(l.getBlockEntity(top) instanceof HouseReadPropBlock.Fitting door)door.setRead(drawing.isEmpty()?HouseReadKitchen.blank():drawing.copy());
    }
    public static boolean pictureUp(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FRIDGE.above()));return s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.TURNED);}
    /** The coat on its hook (the very coat the reader took in the coatroom, or a plain one), or the hook empty. */
    public static void coat(ServerLevel l,BlockPos b,String coat,boolean hung){set(l,b,HOOK,read(hung?coatPart(coat):HouseReadPropBlock.Part.EMPTY_HANGER,Direction.WEST));}
    public static HouseReadPropBlock.Part coatPart(String coat){var g=coat==null||coat.isEmpty()?null:HouseReadWardrobe.byId(coat);try{return g==null?HouseReadPropBlock.Part.WOOL:HouseReadWardrobe.part(g);}catch(IllegalArgumentException e){return HouseReadPropBlock.Part.WOOL;}}
    public static boolean coatHung(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(HOOK));return s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.PART)!=HouseReadPropBlock.Part.EMPTY_HANGER;}
    /** The hall glass: none (-1), hanging facing the hall (0) or turned to the wall (1). */
    public static void glass(ServerLevel l,BlockPos b,int stage){set(l,b,GLASS,stage<0?Blocks.AIR.defaultBlockState():fit(SecondPersonBlock.Kind.GLASS,Direction.WEST,stage));}
    public static int glass(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(GLASS));return SecondPersonBlock.is(s,SecondPersonBlock.Kind.GLASS)?s.getValue(SecondPersonBlock.STAGE):-1;}
    public static void phone(ServerLevel l,BlockPos b,boolean off){set(l,b,PHONE,fit(SecondPersonBlock.Kind.PHONE,Direction.EAST,off?1:0));}
    public static boolean phoneOff(ServerLevel l,BlockPos b){return SecondPersonBlock.stage(l.getBlockState(b.offset(PHONE)))==1;}
    /** The sitting-room lamp: a switched-off lamp is TURNED. */
    public static void lamp(ServerLevel l,BlockPos b,boolean on){set(l,b,LAMP,read(HouseReadPropBlock.Part.LAMP,Direction.SOUTH).setValue(HouseReadPropBlock.TURNED,!on));}
    public static boolean lampOn(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(LAMP));return s.is(HouseReadRegistry.FITTING.get())&&!s.getValue(HouseReadPropBlock.TURNED);}
    public static void mirror(ServerLevel l,BlockPos b,boolean steamed){set(l,b,MIRROR,fit(SecondPersonBlock.Kind.MIRROR,Direction.SOUTH,steamed?1:0));}
    public static boolean steamed(ServerLevel l,BlockPos b){return SecondPersonBlock.stage(l.getBlockState(b.offset(MIRROR)))==1;}
    /** The tablecloth: none (-1), rumpled (0), straightened (1), laid for two (2). */
    public static void cloth(ServerLevel l,BlockPos b,int stage){set(l,b,CLOTH,stage<0?Blocks.AIR.defaultBlockState():fit(SecondPersonBlock.Kind.CLOTH,Direction.NORTH,stage));}
    public static int cloth(ServerLevel l,BlockPos b){return SecondPersonBlock.stage(l.getBlockState(b.offset(CLOTH)));}
    public static void box(ServerLevel l,BlockPos b,BlockPos at,int stage){set(l,b,at,stage<0?Blocks.AIR.defaultBlockState():fit(BOX,Direction.NORTH,stage));}
    /** A thing set down: the routine item by its number, the coat folded, the drawing as a sheet, the photograph standing. */
    public static void thing(ServerLevel l,BlockPos b,BlockPos at,String thing,int routine){set(l,b,at,thingState(thing,routine));}
    static BlockState thingState(String thing,int routine){return switch(thing){case "routine"->fit(ROUTINE,Direction.NORTH,Math.floorMod(routine<0?11:routine,16));case "coat"->fit(SecondPersonBlock.Kind.COAT,Direction.NORTH,0);case "drawing"->fit(SHEET,Direction.NORTH,0);
        case "ticket"->fit(SecondPersonBlock.Kind.TICKET,Direction.NORTH,0);case "sleeve"->fit(SecondPersonBlock.Kind.SLEEVE,Direction.NORTH,2);default->fit(PHOTO,Direction.NORTH,0);};}
    /** Which of the resident's things stands at a position, if any. */
    public static @Nullable String thingAt(ServerLevel l,BlockPos at){var k=SecondPersonBlock.kind(l.getBlockState(at));if(k==null)return null;return switch(k){case ROUTINE->"routine";case COAT->"coat";case SHEET->"drawing";case PHOTO->"photo";case TICKET->"ticket";case SLEEVE->"sleeve";default->null;};}
    /** The keepsake waiting on the bus-stop bench at the end. */
    public static void benchBook(ServerLevel l,BlockPos b,boolean there){set(l,b,BENCH_BOOK,there?fit(SecondPersonBlock.Kind.BOOK,Direction.SOUTH,1):Blocks.SPRUCE_SLAB.defaultBlockState());}
}
