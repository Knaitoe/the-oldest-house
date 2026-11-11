package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.422, Borrasca's working town (docs/BORRASCA_TOWN_0_4_422.md). The owner, November 9: "Make the town more functional too. A diner
 * where the player can grab some pie and coffee ... a sheriff's office with a town drunk, etc. it shouldn't feel like drowned town",
 * and the shops agreed after it. The street past the chapel (world z -75 to -121, flat, lift 0) has the diner, the antiques shop with
 * the historical society behind it and the gas station on its west side, and the doctor's office, the drugstore and the bar on its
 * east; the general store gains a hardware counter, and the town's framed history moves to the society.
 *
 * <p>Everything here is placed in world coordinates relative to the base (the street past the chapel is the drawn-out row of the hill
 * map, so it has no authored coordinates); {@link #click} matches a clicked block before it is converted back to authored.
 */
public final class BorrascaShops {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private BorrascaShops(){}

    // ---- Where things are, in the world, relative to the base ------------------------------------------------------------------
    /** The town's framed history, now on the south wall of the historical society, hung high. */
    public static final BlockPos HISTORY=new BlockPos(-18,2,-95);
    public static final BlockPos DINER_DOOR=new BlockPos(-7,0,-84),MENU=new BlockPos(-14,2,-90),PIE_CASE=new BlockPos(-16,1,-87),URN=new BlockPos(-16,1,-82),DINER_TILL=new BlockPos(-16,1,-84);
    public static final BlockPos ANTIQUES_DOOR=new BlockPos(-7,0,-100),SOCIETY_DOOR=new BlockPos(-15,0,-100),MAP=new BlockPos(-20,2,-100),PHOTOS=new BlockPos(-18,2,-106),CURIOS=new BlockPos(-11,0,-104),HELMET=new BlockPos(-19,0,-104),SHOP_CLOCK=new BlockPos(-10,2,-106);
    public static final BlockPos STATION_DOOR=new BlockPos(-16,0,-114),PAYPHONE=new BlockPos(-15,1,-113),PRICE=new BlockPos(-5,2,-109),COOLER=new BlockPos(-15,0,-117),STATION_CANDY=new BlockPos(-20,0,-112);
    public static final BlockPos[] PUMPS={new BlockPos(-10,0,-112),new BlockPos(-10,0,-117)};
    public static final BlockPos DOCTOR_DOOR=new BlockPos(7,0,-84),CARDS=new BlockPos(10,2,-88),JAR=new BlockPos(12,1,-80),SCALE=new BlockPos(18,0,-80),CHART=new BlockPos(18,2,-84),EXAM=new BlockPos(16,0,-87);
    public static final BlockPos DRUG_DOOR=new BlockPos(7,0,-98),FOUNTAIN=new BlockPos(13,1,-103),MEDS=new BlockPos(20,0,-98),RACK=new BlockPos(10,0,-95),DRUG_CANDY=new BlockPos(14,0,-95);
    /** The drugstore's two arcade cabinets by its south wall, DEEP SEAM then BIG CAT (0.4.436, BorrascaArcade). */
    public static final BlockPos[] CABINETS={new BlockPos(9,0,-93),new BlockPos(11,0,-93)};
    public static final BlockPos BAR_DOOR=new BlockPos(7,0,-114),POOL=new BlockPos(14,0,-111),DARTS=new BlockPos(21,2,-114);
    /** The hardware counter in the general store: the pegboard on the south wall, where the frame used to hang. */
    public static final BlockPos[] PEGBOARD={new BlockPos(-22,1,-17),new BlockPos(-23,1,-17),new BlockPos(-22,2,-17),new BlockPos(-23,2,-17)};

    // ---- Small helpers -----------------------------------------------------------------------------------------------------------
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){set(l,b,x,y,z,block.defaultBlockState());}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){NovelRooms.box(l,b,Math.min(x,X),Math.min(y,Y),Math.min(z,Z),Math.max(x,X),Math.max(y,Y),Math.max(z,Z),s);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    private static BlockState fit(BorrascaShopBlock.Kind k,Direction d){return BorrascaShopBlock.of(k,d);}
    private static void fit(ServerLevel l,BlockPos b,BlockPos at,BorrascaShopBlock.Kind k,Direction d){if(k.wall())back(l,b,at,d);set(l,b,at,fit(k,d));}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,BorrascaShopBlock.Kind k,Direction d){fit(l,b,new BlockPos(x,y,z),k,d);}
    /**
     * 0.4.444 (the owner's screenshot: a clock hung on a window): a piece that hangs on a wall has wall behind it. Where the shop's
     * builder left one of its small side windows there, the wall is carried across it.
     */
    static void back(ServerLevel l,BlockPos b,BlockPos at,Direction d){
        var behind=b.offset(at).relative(d.getOpposite());if(!BuildBlocks.state(l,behind).is(Blocks.GLASS_PANE))return;
        var wall=behind.below();while(BuildBlocks.state(l,wall).is(Blocks.GLASS_PANE))wall=wall.below();
        BuildBlocks.set(l,behind,BuildBlocks.state(l,wall),F);
    }
    private static void road(ServerLevel l,BlockPos b,int x,int y,int z,RoadhouseBlock.Kind k,Direction d){set(l,b,x,y,z,RoadhouseBlock.of(k,d));}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction d){NovelRooms.furniture(l,b.offset(x,y,z),k,d);}
    private static void sign(ServerLevel l,BlockPos b,int x,int y,int z,Direction d,String... lines){NovelRooms.sign(l,b.offset(x,y,z),d,lines);}
    private static BlockState slab(Block block,SlabType type){return block.defaultBlockState().setValue(SlabBlock.TYPE,type);}
    private static BlockState stair(Block block,Direction facing){return block.defaultBlockState().setValue(StairBlock.FACING,facing);}
    private static BlockState log(Block block,Direction.Axis axis){return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    private static BlockState hanging(){return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);}

    /**
     * A shop on the street: a stone footing, walls five high with corner posts, a flat roof behind a false front that stands two above it
     * on the street side with the shop's sign, windows either side of the door, an awning over the front (a slab with cloth on it) and a sidewalk before it.
     * {@code west} shops face east across the street; the rest face west. z0 is the south wall, z1 the north.
     */
    private static void shop(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,boolean west,BlockState wall,Block corner,BlockState floor,BlockState roof,int doorZ,int[] windows,Block awning,Block door,String... signLines){
        int front=west?x1:x0;Direction out=west?Direction.EAST:Direction.WEST;int o=west?1:-1;
        box(l,b,x0-1,0,z1-1,x1+1,9,z0+1,AIR);
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++)set(l,b,x,-1,z,(x==x0||x==x1||z==z0||z==z1)?Blocks.STONE_BRICKS.defaultBlockState():floor);
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++){if(x!=x0&&x!=x1&&z!=z0&&z!=z1)continue;box(l,b,x,0,z,x,4,z,wall);}
        for(int x:new int[]{x0,x1})for(int z:new int[]{z0,z1})box(l,b,x,0,z,x,4,z,log(corner,Direction.Axis.Y));
        box(l,b,x0,5,z1,x1,5,z0,roof);
        // The false front, stepped down at its ends.
        box(l,b,front,5,z1+2,front,6,z0-2,wall);box(l,b,front,7,z1+2,front,7,z0-2,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));
        box(l,b,front,6,z1,front,6,z1+1,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));box(l,b,front,6,z0-1,front,6,z0,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));
        NovelRooms.door(l,b.offset(front,0,doorZ),out,door,false);
        for(int z:windows)box(l,b,front,1,z,front,2,z,Blocks.GLASS_PANE);
        // Side windows, small, high on the walls.
        for(int x=x0+3;x<=x1-3;x+=4){set(l,b,x,2,z0,Blocks.GLASS_PANE);set(l,b,x,2,z1,Blocks.GLASS_PANE);}
        for(int z=z1+1;z<=z0-1;z++){set(l,b,front+o,3,z,slab(Blocks.SPRUCE_SLAB,SlabType.TOP));set(l,b,front+o,4,z,awning);}
        for(int z=z1-1;z<=z0+1;z++){set(l,b,front+o,-1,z,Blocks.SMOOTH_STONE);set(l,b,front+2*o,-1,z,Blocks.SMOOTH_STONE);}
        sign(l,b,front+o,6,doorZ,out,signLines);
        BorrascaRooms.join(l,b,x0-1,0,z1-1,x1+1,3,z0+1);
    }

    static void build(ServerLevel l,BlockPos b){
        diner(l,b);antiques(l,b);station(l,b);doctor(l,b);drugstore(l,b);bar(l,b);hardware(l,b);poles(l,b);alley(l,b);
    }

    /** The Hilltop Cafe: a counter with stools, booths along the windows, the pie case and the urn on the counter, the kitchen behind. */
    private static void diner(ServerLevel l,BlockPos b){
        shop(l,b,-21,-78,-7,-91,true,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.STRIPPED_BIRCH_LOG,BorrascaRegistry.CHECKER.get().defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            DINER_DOOR.getZ(),new int[]{-89,-88,-87,-81,-80,-79},Blocks.RED_CARPET,Blocks.SPRUCE_DOOR,"HILLTOP","CAFE","Pie - Coffee","Plate Lunch");
        for(int z=-89;z<=-80;z++)road(l,b,-16,0,z,RoadhouseBlock.Kind.COUNTER,Direction.EAST);
        for(int z:new int[]{-88,-86,-82,-80})road(l,b,-15,0,z,RoadhouseBlock.Kind.STOOL,Direction.WEST);
        fit(l,b,PIE_CASE,BorrascaShopBlock.Kind.PIE_CASE,Direction.EAST);fit(l,b,URN,BorrascaShopBlock.Kind.URN,Direction.EAST);road(l,b,DINER_TILL.getX(),DINER_TILL.getY(),DINER_TILL.getZ(),RoadhouseBlock.Kind.REGISTER,Direction.WEST);
        // Booths by the windows: a seat each end and the table between.
        for(int z0:new int[]{-89,-81}){road(l,b,-9,0,z0,RoadhouseBlock.Kind.BOOTH,Direction.SOUTH);road(l,b,-9,0,z0+1,RoadhouseBlock.Kind.BOOTH_TABLE,Direction.SOUTH);road(l,b,-9,0,z0+2,RoadhouseBlock.Kind.BOOTH,Direction.NORTH);}
        fit(l,b,MENU,BorrascaShopBlock.Kind.MENU,Direction.SOUTH);fit(l,b,-8,2,-80,BorrascaShopBlock.Kind.OPEN,Direction.EAST);
        // The kitchen behind the counter: the range, the sink, the shelves, the door to the back.
        set(l,b,-20,0,-89,Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST));set(l,b,-20,0,-88,Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST));
        set(l,b,-20,0,-86,Blocks.CAULDRON);for(int z=-84;z<=-81;z++)fit(l,b,-20,0,z,BorrascaShopBlock.Kind.CROCKERY,Direction.EAST);
        set(l,b,-20,0,-80,Blocks.BARREL);set(l,b,-19,0,-80,Blocks.BARREL);
        set(l,b,-12,0,-90,Blocks.JUKEBOX);set(l,b,-8,0,-85,Blocks.POTTED_FERN);
        for(int z:new int[]{-88,-83})set(l,b,-12,4,z,hanging());set(l,b,-18,4,-85,hanging());
        joists(l,b,-20,-8,Blocks.STRIPPED_BIRCH_LOG,-90,-86,-81);
    }

    /** Rowe's Antiques in front, the Drisking Historical Society through the door at the back. */
    private static void antiques(ServerLevel l,BlockPos b){
        shop(l,b,-21,-94,-7,-107,true,Blocks.BRICKS.defaultBlockState(),Blocks.STRIPPED_DARK_OAK_LOG,Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            ANTIQUES_DOOR.getZ(),new int[]{-105,-104,-103,-97,-96,-95},Blocks.GREEN_CARPET,Blocks.DARK_OAK_DOOR,"ROWE'S","ANTIQUES","Bought & Sold","Historical Soc.");
        box(l,b,-20,4,-106,-8,4,-95,BorrascaRegistry.TIN.get().defaultBlockState());
        // The partition and its door into the society.
        box(l,b,-15,0,-106,-15,4,-95,Blocks.DARK_OAK_PLANKS);NovelRooms.door(l,b.offset(SOCIETY_DOOR),Direction.EAST,Blocks.DARK_OAK_DOOR,false);
        sign(l,b,-14,2,-101,Direction.EAST,"DRISKING","HISTORICAL","SOCIETY","Open Saturdays");
        // The shop: two glass cases, a counter with a bell, chairs and lamps for sale, a clock on the wall.
        fit(l,b,CURIOS,BorrascaShopBlock.Kind.CASE,Direction.EAST);fit(l,b,-11,0,-96,BorrascaShopBlock.Kind.CASE,Direction.EAST);
        for(int z=-99;z>=-102;z--){if(z==-100)continue;road(l,b,-13,0,z,RoadhouseBlock.Kind.COUNTER,Direction.EAST);}
        set(l,b,-13,1,-99,Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT,BellAttachType.FLOOR).setValue(BellBlock.FACING,Direction.EAST));
        furniture(l,b,-9,0,-106,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);furniture(l,b,-14,0,-106,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        furniture(l,b,-9,0,-95,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);set(l,b,-14,0,-95,Blocks.DECORATED_POT);set(l,b,-14,1,-106,Blocks.LANTERN);
        fit(l,b,SHOP_CLOCK,BorrascaShopBlock.Kind.CLOCK,Direction.SOUTH);
        // The society: the frame, the map of the old workings, the photographs, a miner's helmet, the reading table.
        back(l,b,HISTORY,Direction.NORTH);set(l,b,HISTORY,BorrascaFittingBlock.of(BorrascaFittingBlock.Kind.NOTICE,Direction.NORTH));
        fit(l,b,MAP,BorrascaShopBlock.Kind.MAP,Direction.EAST);fit(l,b,PHOTOS,BorrascaShopBlock.Kind.PHOTOS,Direction.SOUTH);fit(l,b,-17,2,-106,BorrascaShopBlock.Kind.PHOTOS,Direction.SOUTH);
        fit(l,b,HELMET,BorrascaShopBlock.Kind.HELMET,Direction.EAST);furniture(l,b,-19,0,-98,HouseholdFurnitureBlock.Kind.READING_DESK,Direction.WEST);furniture(l,b,-18,0,-98,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        set(l,b,-11,4,-100,hanging());set(l,b,-18,4,-102,hanging());
    }

    /** The Ridge Service station: two pumps under a canopy on the forecourt, the price board by the street, the payphone, the cooler. */
    private static void station(ServerLevel l,BlockPos b){
        shop(l,b,-26,-110,-16,-119,true,Blocks.SMOOTH_SANDSTONE.defaultBlockState(),Blocks.STRIPPED_SPRUCE_LOG,Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            STATION_DOOR.getZ(),new int[]{-112,-111,-116,-117},Blocks.WHITE_CARPET,Blocks.SPRUCE_DOOR,"RIDGE","SERVICE","Gas - Bait","Ice - Candy");
        // The forecourt and the pump island under the canopy.
        for(int x=-13;x<=-6;x++)for(int z=-121;z<=-108;z++)set(l,b,x,-1,z,(x+z)%7==0?Blocks.COARSE_DIRT:Blocks.GRAVEL);
        box(l,b,-11,-1,-119,-9,-1,-110,Blocks.SMOOTH_STONE);
        for(var p:PUMPS){fit(l,b,p,BorrascaShopBlock.Kind.PUMP,Direction.EAST);fit(l,b,p.above(),BorrascaShopBlock.Kind.PUMP_TOP,Direction.EAST);}
        for(int x:new int[]{-12,-8})for(int z:new int[]{-110,-119})box(l,b,x,0,z,x,3,z,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));
        box(l,b,-13,4,-120,-7,4,-109,slab(Blocks.SMOOTH_STONE_SLAB,SlabType.BOTTOM));set(l,b,-10,3,-114,hanging());
        box(l,b,-6,0,-109,-6,3,-109,log(Blocks.DARK_OAK_LOG,Direction.Axis.Y));fit(l,b,PRICE,BorrascaShopBlock.Kind.PRICE,Direction.EAST);
        fit(l,b,PAYPHONE,BorrascaShopBlock.Kind.PAYPHONE,Direction.EAST);fit(l,b,COOLER,BorrascaShopBlock.Kind.COOLER,Direction.EAST);
        // Inside: the candy rack, the counter and till, a second cooler. 0.4.430 (owner: "i hope you're still working on detailing the
        // architecture"): along the back wall the station's own stock, not shelves of books: chips, motor oil and antifreeze, the bait
        // freezer under its sign, and the feed store's calendar behind the counter; a mat inside the door, joists overhead, a second
        // lamp, and used tires stacked out front.
        fit(l,b,STATION_CANDY,BorrascaShopBlock.Kind.CANDY,Direction.EAST);
        for(int z=-118;z<=-116;z++)road(l,b,-19,0,z,RoadhouseBlock.Kind.COUNTER,Direction.EAST);road(l,b,-19,1,-117,RoadhouseBlock.Kind.REGISTER,Direction.WEST);
        fit(l,b,-25,0,-111,BorrascaShopBlock.Kind.SNACKS,Direction.EAST);fit(l,b,-25,0,-112,BorrascaShopBlock.Kind.OIL,Direction.EAST);fit(l,b,-25,0,-113,BorrascaShopBlock.Kind.OIL,Direction.EAST);
        fit(l,b,-25,0,-114,BorrascaShopBlock.Kind.BAIT,Direction.EAST);fit(l,b,-25,1,-114,BorrascaShopBlock.Kind.BAIT_SIGN,Direction.EAST);
        fit(l,b,-25,0,-115,BorrascaShopBlock.Kind.OIL,Direction.EAST);fit(l,b,-25,0,-116,BorrascaShopBlock.Kind.OIL,Direction.EAST);
        fit(l,b,-25,1,-117,BorrascaShopBlock.Kind.CALENDAR,Direction.EAST);
        fit(l,b,-22,0,-118,BorrascaShopBlock.Kind.COOLER,Direction.SOUTH);set(l,b,-21,4,-114,hanging());set(l,b,-23,4,-115,hanging());
        box(l,b,-18,0,-114,-17,0,-114,Blocks.GRAY_CARPET);joists(l,b,-25,-17,Blocks.STRIPPED_SPRUCE_LOG,-112,-117);
        fit(l,b,-15,0,-119,BorrascaShopBlock.Kind.TIRES,Direction.WEST);
    }
    /** 0.4.430: ceiling joists across a shop, under its roof, on rows clear of every hanging lamp. */
    private static void joists(ServerLevel l,BlockPos b,int x0,int x1,Block log,int... zs){
        for(int z:zs)box(l,b,x0,4,z,x1,4,z,log(log,Direction.Axis.X));
    }

    /** Dr. Hale's office: a waiting room with the corkboard of birth cards and the lollipop jar, the examination room behind. */
    private static void doctor(ServerLevel l,BlockPos b){
        shop(l,b,7,-78,19,-89,false,Blocks.WHITE_CONCRETE.defaultBlockState(),Blocks.STRIPPED_BIRCH_LOG,BorrascaRegistry.LINO.get().defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            DOCTOR_DOOR.getZ(),new int[]{-87,-86,-81,-80},Blocks.LIGHT_BLUE_CARPET,Blocks.BIRCH_DOOR,"R. HALE, M.D.","Family","Medicine","Walk-ins welcome");
        // Waiting room: chairs along the walls, a low table, the receptionist's desk with the jar on it.
        for(int x=9;x<=11;x++){furniture(l,b,x,0,-79,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);furniture(l,b,x,0,-88,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);}
        furniture(l,b,8,0,-79,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        furniture(l,b,12,0,-80,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);fit(l,b,JAR,BorrascaShopBlock.Kind.JAR,Direction.WEST);furniture(l,b,12,0,-81,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);
        fit(l,b,CARDS,BorrascaShopBlock.Kind.CARDS,Direction.SOUTH);
        // The partition, and the examination room: the table, the scale, the eye chart, a cabinet and a basin.
        box(l,b,13,0,-88,13,4,-79,Blocks.WHITE_CONCRETE);NovelRooms.door(l,b.offset(13,0,-84),Direction.WEST,Blocks.BIRCH_DOOR,false);
        fit(l,b,EXAM,BorrascaShopBlock.Kind.EXAM,Direction.WEST);fit(l,b,SCALE,BorrascaShopBlock.Kind.SCALE,Direction.WEST);fit(l,b,CHART,BorrascaShopBlock.Kind.CHART,Direction.WEST);
        fit(l,b,18,0,-88,BorrascaShopBlock.Kind.MEDS,Direction.WEST);set(l,b,14,0,-79,Blocks.CAULDRON);
        set(l,b,10,4,-84,hanging());set(l,b,16,4,-84,hanging());
        joists(l,b,8,18,Blocks.STRIPPED_BIRCH_LOG,-87,-81);
    }

    /** Main Street Drug: the soda fountain with its stools, the pharmacy counter at the back, the comic spinner and the candy rack. */
    private static void drugstore(ServerLevel l,BlockPos b){
        shop(l,b,7,-92,21,-105,false,Blocks.MUD_BRICKS.defaultBlockState(),Blocks.STRIPPED_DARK_OAK_LOG,BorrascaRegistry.LINO.get().defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            DRUG_DOOR.getZ(),new int[]{-103,-102,-101,-95,-94,-93},Blocks.RED_CARPET,Blocks.SPRUCE_DOOR,"MAIN STREET","DRUG","Soda Fountain","Prescriptions");
        // The soda fountain along the north wall: counter, taps and stools.
        for(int x=10;x<=16;x++)road(l,b,x,0,-103,RoadhouseBlock.Kind.COUNTER,Direction.SOUTH);
        fit(l,b,FOUNTAIN,BorrascaShopBlock.Kind.FOUNTAIN,Direction.SOUTH);fit(l,b,11,1,-103,BorrascaShopBlock.Kind.URN,Direction.SOUTH);
        for(int x:new int[]{10,12,14,16})road(l,b,x,0,-102,RoadhouseBlock.Kind.STOOL,Direction.NORTH);
        // The pharmacy at the back, behind its counter.
        // 0.4.444 (owner: "Facing the wrong way. Give the shelves more variance?"): the register's keys are the pharmacist's, and the
        // shelves behind him alternate between two arrangements.
        for(int z=-102;z<=-95;z++)road(l,b,18,0,z,RoadhouseBlock.Kind.COUNTER,Direction.WEST);road(l,b,18,1,-97,RoadhouseBlock.Kind.REGISTER,Direction.EAST);
        for(int z=-103;z<=-94;z++)for(int y=0;y<=1;y++)set(l,b,20,y,z,BorrascaShopBlock.of(BorrascaShopBlock.Kind.MEDS,Direction.WEST,(z+y)&1));
        fit(l,b,RACK,BorrascaShopBlock.Kind.RACK,Direction.WEST);fit(l,b,DRUG_CANDY,BorrascaShopBlock.Kind.CANDY,Direction.WEST);
        fit(l,b,8,2,-101,BorrascaShopBlock.Kind.OPEN,Direction.WEST);
        // 0.4.436: the two cabinets against the south wall, facing into the shop.
        for(int game=0;game<CABINETS.length;game++){set(l,b,CABINETS[game],BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET,Direction.NORTH,game));set(l,b,CABINETS[game].above(),BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET_TOP,Direction.NORTH,game));}
        for(int x:new int[]{11,16})set(l,b,x,4,-98,hanging());
        joists(l,b,8,20,Blocks.STRIPPED_DARK_OAK_LOG,-102,-94);
    }

    /** The Tipple: a long bar with its back bar, stools, a pool table and a dartboard, a booth, and a beer sign in the window. */
    private static void bar(ServerLevel l,BlockPos b){
        shop(l,b,7,-108,22,-121,false,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.DARK_OAK_LOG,Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),
            BAR_DOOR.getZ(),new int[]{-111,-117},Blocks.BLACK_CARPET,Blocks.DARK_OAK_DOOR,"THE TIPPLE","Cold Beer","Pool - Darts","21 and over");
        for(int x=10;x<=17;x++){road(l,b,x,0,-120,RoadhouseBlock.Kind.BACK_BAR,Direction.SOUTH);road(l,b,x,0,-118,RoadhouseBlock.Kind.COUNTER,Direction.SOUTH);}
        road(l,b,12,1,-118,RoadhouseBlock.Kind.TAPS,Direction.SOUTH);road(l,b,15,1,-120,RoadhouseBlock.Kind.GLASSES,Direction.SOUTH);road(l,b,11,1,-120,RoadhouseBlock.Kind.MIRROR,Direction.SOUTH);
        for(int x:new int[]{10,12,14,16})road(l,b,x,0,-117,RoadhouseBlock.Kind.STOOL,Direction.NORTH);
        PoolTableBlock.place(l,b.offset(POOL),Direction.EAST);road(l,b,14,4,-111,RoadhouseBlock.Kind.POOL_LAMP,Direction.SOUTH);
        set(l,b,DARTS,BarRegistry.DARTBOARD.get().defaultBlockState().setValue(DartboardBlock.FACING,Direction.WEST));
        road(l,b,20,0,-109,RoadhouseBlock.Kind.BOOTH,Direction.SOUTH);road(l,b,20,0,-110,RoadhouseBlock.Kind.BOOTH_TABLE,Direction.SOUTH);road(l,b,20,0,-111,RoadhouseBlock.Kind.BOOTH,Direction.NORTH);
        road(l,b,8,2,-111,RoadhouseBlock.Kind.NEON_BEER,Direction.WEST);road(l,b,12,1,-109,RoadhouseBlock.Kind.CORKBOARD,Direction.NORTH);
        set(l,b,12,4,-113,hanging());set(l,b,18,4,-116,hanging());
        joists(l,b,8,21,Blocks.DARK_OAK_LOG,-119,-115,-110);
    }

    /** 0.4.444: the mouth of the alley between the drugstore and the Tipple, and the flattened boxes where, years later, Earl sits. */
    private static void alley(ServerLevel l,BlockPos b){
        for(int x=8;x<=9;x++){set(l,b,x,-1,-107,Blocks.GRAVEL);set(l,b,x,0,-107,Blocks.BROWN_CARPET);}
    }

    /** The general store's hardware counter: a pegboard of rope, flashlights, batteries and pocketknives where the frame used to hang. */
    private static void hardware(ServerLevel l,BlockPos b){
        for(var at:PEGBOARD)fit(l,b,at,BorrascaShopBlock.Kind.PEGBOARD,Direction.NORTH);
        sign(l,b,-22,3,-17,Direction.NORTH,"","HARDWARE","Ask at the counter","");
    }

    /** The poles carry on up the east side of the street, a lamp on an arm over it from each. */
    private static void poles(ServerLevel l,BlockPos b){
        for(int z:new int[]{-76,-90,-106,-122}){
            box(l,b,5,0,z,5,6,z,log(Blocks.DARK_OAK_LOG,Direction.Axis.Y));
            box(l,b,4,6,z,6,6,z,log(Blocks.STRIPPED_DARK_OAK_LOG,Direction.Axis.X));
            set(l,b,4,5,z,hanging());
        }
    }

    // ---- What a click on a fitting does ----------------------------------------------------------------------------------------
    /**
     * A click in the town, in world coordinates relative to the base. Returns true when it was one of the shops' things. Children and
     * grown readers see the same street; a few things read differently by visit (prices went up, the cards are a later year's).
     */
    static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        int visit=own.getInt("Visit");boolean grown=visit>=3;var at=b.offset(rel);
        var place=BorrascaStory.PLACE;
        // 0.4.428: a child can get into the historical society only while Mr. Rowe is out front for his smoke.
        if((rel.equals(SOCIETY_DOOR)||rel.equals(SOCIETY_DOOR.above()))&&!BorrascaPeople.societyOpen(p,b,own)){BorrascaPeople.notForKids(p,own);return true;}
        if(rel.equals(HISTORY)){if(grown)LiteraryVignettes.open(p,place,own,"History",BorrascaTexts.document("History"),at);else BorrascaStory.cue(p,"A framed page of typing, hung up high. You can't read it from down here.");return true;}
        if(rel.equals(MENU)){LiteraryVignettes.open(p,place,own,grown?"MenuLater":"Menu",BorrascaShopTexts.menu(grown),at);return true;}
        if(rel.equals(CARDS)){LiteraryVignettes.open(p,place,own,grown?"CardsLater":visit==2?"Cards2":"Cards",BorrascaShopTexts.cards(visit),at);return true;}
        if(rel.equals(MAP)){if(!grown&&!own.contains("SocietyChild"))own.putInt("SocietyChild",own.getBoolean("RoweOut")?1:2);LiteraryVignettes.open(p,place,own,"MineMap",BorrascaShopTexts.map(),at);return true;}
        for(var peg:PEGBOARD)if(rel.equals(peg)){LiteraryVignettes.open(p,place,own,grown?"HardwareLater":"Hardware",BorrascaShopTexts.hardware(grown),at);return true;}
        // 0.4.425: a child gets one lollipop from the jar, with the receptionist there to say so.
        if(rel.equals(JAR)&&!grown&&BorrascaPeople.atWork(p,own,BorrascaPeople.ODELL)){BorrascaPeople.lollipop(p,own);return true;}
        var s=p.serverLevel().getBlockState(at);
        if(!s.is(BorrascaRegistry.SHOP.get()))return false;
        // 0.4.426: the payphone has its own dial.
        if(s.getValue(BorrascaShopBlock.KIND)==BorrascaShopBlock.Kind.PAYPHONE){BorrascaPayphone.open(p,own);return true;}
        // 0.4.435: the children's bikes can be ridden.
        if(s.getValue(BorrascaShopBlock.KIND)==BorrascaShopBlock.Kind.BIKE)return BorrascaStory.bikeClick(p,b,own,at,s);
        // 0.4.444 (owner: "maybe give the different pumps different last prices?"): each pump's own last sale.
        if(s.getValue(BorrascaShopBlock.KIND)==BorrascaShopBlock.Kind.PUMP||s.getValue(BorrascaShopBlock.KIND)==BorrascaShopBlock.Kind.PUMP_TOP){BorrascaStory.cue(p,BorrascaShopTexts.pump(rel.getZ()==PUMPS[1].getZ()?1:0,grown));return true;}
        // 0.4.436: the drugstore's cabinets play their own games.
        int game=BorrascaArcade.game(s);if(game>=0){BorrascaArcade.click(p,at,own,game);return true;}
        String line=BorrascaShopTexts.look(s.getValue(BorrascaShopBlock.KIND),s.getValue(BorrascaShopBlock.STAGE),grown);
        if(line==null)return false;
        BorrascaStory.cue(p,line);return true;
    }
}
