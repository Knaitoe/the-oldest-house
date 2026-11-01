package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.Kind.*;

/**
 * 0.4.214: the household's rooms behind its doors, built into the leak engine's pocket template and standing in the same street
 * (StaircaseLeakRooms.street). Coordinates are relative to the slot's base: the room's one working door is at (0,0,0) in its south
 * wall, as in every leak room, and walking out of it ends the visit.
 */
public final class StaircaseHouseholdRooms {
    private StaircaseHouseholdRooms(){}

    // ------------------------------------------------------------------------------------------------ Sunday lunch
    /** The six household places (south row then north row), each laid on its own table cell, and the visitor's at the east end. */
    public static final BlockPos[] PLACES={new BlockPos(-1,0,-4),new BlockPos(0,0,-4),new BlockPos(1,0,-4),new BlockPos(-1,0,-5),new BlockPos(0,0,-5),new BlockPos(1,0,-5)};
    public static final BlockPos VISITOR=new BlockPos(2,0,-4),BREAD=new BlockPos(2,0,-5);
    /** The chair at each place, in the same order, and the seventh chair against the east wall, and where it is pulled up to. */
    public static final BlockPos[] CHAIRS={new BlockPos(-1,0,-3),new BlockPos(0,0,-3),new BlockPos(1,0,-3),new BlockPos(-1,0,-6),new BlockPos(0,0,-6),new BlockPos(1,0,-6)};
    public static final BlockPos SEVENTH=new BlockPos(4,0,-5),SEVENTH_UP=new BlockPos(3,0,-4);
    public static final BlockPos SIDEBOARD=new BlockPos(-4,0,-4),CUPBOARD=new BlockPos(4,1,-7),VASE=new BlockPos(4,1,-2);
    public static final BlockPos[] HATCH={new BlockPos(-1,1,-9),new BlockPos(0,1,-9),new BlockPos(1,1,-9)};
    /** A chair pushed back from the table stands one block further from it. */
    public static BlockPos pushedBack(BlockPos chair){return chair.offset(0,0,chair.getZ()==-3?1:-1);}
    public static Direction chairFacing(BlockPos chair){return chair.getZ()==-3||chair.getZ()==-2?Direction.NORTH:Direction.SOUTH;}
    public static BlockState chair(Direction facing){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.CANE_CHAIR).setValue(HouseholdFurnitureBlock.FACING,facing);}
    static BlockState prop(StaircaseLeakProps.Kind kind,Direction facing){return StaircaseLeakProps.state(kind).setValue(BlockStateProperties.HORIZONTAL_FACING,facing);}

    // ------------------------------------------------------------------------------------------------ Fresh sheets
    public static final BlockPos BED_FOOT=new BlockPos(-3,0,-6),BED_HEAD=new BlockPos(-3,0,-7),AIRING_LOW_AT=new BlockPos(3,0,-7),AIRING_HIGH_AT=new BlockPos(3,1,-7),
        BASKET=new BlockPos(2,0,-2),FOLD=new BlockPos(0,0,-4),SOCK=new BlockPos(-2,0,-5);
    // ------------------------------------------------------------------------------------------------ Mending day
    public static final BlockPos LAMP_CLOSE=new BlockPos(1,0,-5),LAMP_ASIDE=new BlockPos(3,0,-6),COAT=new BlockPos(1,0,-6),SEWING=new BlockPos(2,0,-6),TIN=new BlockPos(-2,1,-6),ARMCHAIR=new BlockPos(-1,0,-6);
    // ------------------------------------------------------------------------------------------------ First light
    public static final BlockPos KETTLE=new BlockPos(-1,1,-8),FIRST_CUP=new BlockPos(-3,1,-8),SECOND_CUP=new BlockPos(-2,1,-8),POTATO=new BlockPos(-4,1,-6),SUPPER=new BlockPos(0,1,-8),
        SLEEPER=new BlockPos(4,0,-4),SETTEE=new BlockPos(4,0,-3),SIDE=new BlockPos(4,0,-5);

    // ------------------------------------------------------------------------------------------------ The long way home
    public static final BlockPos HANDBRAKE=new BlockPos(0,0,0),WINDSCREEN=new BlockPos(1,1,-2),MAP=new BlockPos(1,1,-1),RADIO_AT=new BlockPos(0,1,-1),PASSENGER=new BlockPos(1,0,0);
    // ------------------------------------------------------------------------------------------------ Washing day
    public static final BlockPos[] LINE={new BlockPos(-3,1,-6),new BlockPos(-2,1,-6),new BlockPos(-1,1,-6),new BlockPos(0,1,-6),new BlockPos(1,1,-6),new BlockPos(2,1,-6)};
    public static final BlockPos WASHING=new BlockPos(2,0,-2),BLANKET=new BlockPos(-2,0,-1),HORSE=new BlockPos(3,0,-1);
    // ------------------------------------------------------------------------------------------------ The lay-by
    public static final BlockPos LAYBY_CAR=new BlockPos(0,0,-5),SANDWICH_AT=new BlockPos(1,1,-8),COFFEE_AT=new BlockPos(-1,1,-8),BELL_AT=new BlockPos(-2,1,-5),WHEEL_AT=new BlockPos(-1,1,-6),SEAT_AT=new BlockPos(1,0,-5);
    // ------------------------------------------------------------------------------------------------ The last landing
    public static final BlockPos CUPS=new BlockPos(-2,0,-4),SPOONS=new BlockPos(-1,0,-4),CAMP_LAMP_AT=new BlockPos(2,0,-3),CANDLE_AT=new BlockPos(1,0,-5),TINS=new BlockPos(0,0,-6),LIST=new BlockPos(-3,0,-6),COAT_SEAT=new BlockPos(3,0,-7),ROLL=new BlockPos(-3,0,-2);

    public static Vec3 spawn(BlockPos base,StaircaseLeakRooms.Kind kind){return new Vec3(base.getX()+.5,base.getY(),base.getZ()-1.5);}
    /** The room's walking space; outside it (through the door) the visit ends. */
    public static boolean inside(BlockPos base,StaircaseLeakRooms.Kind kind,Vec3 p){
        double x=p.x-base.getX(),y=p.y-base.getY(),z=p.z-base.getZ();
        return switch(kind){
            case YARD->x>-4.7&&x<5.7&&z>-10.8&&z<.5&&y>=-.5&&y<4.5;
            case LAYBY->x>-6.7&&x<7.7&&z>-10.8&&z<.5&&y>=-.5&&y<4.5;
            default->x>-4.7&&x<5.7&&z>-8.8&&z<.5&&y>=-.5&&y<4.5;
        };
    }
    static void authored(StaircaseLeakRooms.Kind kind,StaircaseLeakRooms.Plan p){
        switch(kind){
            case DINING->dining(p);
            case BEDROOM->bedroom(p);
            case MENDING->mending(p);
            case DAWN->dawn(p);
            case DRIVE->drive(p);
            case YARD->yard(p);
            case LAYBY->layby(p);
            case LANDING->landing(p);
            default->{}
        }
    }

    /** The household's own walls: its wallpaper over a skirting board, a floor, a ceiling and the one working door. */
    private static void shell(StaircaseLeakRooms.Plan p,BlockState floor){
        var paper=StaircaseLeakRegistry.WALLPAPER.get().defaultBlockState();var skirting=Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        p.box(-5,-1,-9,5,-1,0,floor);p.box(-5,0,-9,-5,3,0,paper);p.box(5,0,-9,5,3,0,paper);p.box(-5,0,0,5,3,0,paper);p.box(-5,0,-9,5,3,-9,paper);
        p.box(-5,0,-9,5,0,-9,skirting);p.box(-5,0,0,5,0,0,skirting);p.box(-5,0,-9,-5,0,0,skirting);p.box(5,0,-9,5,0,0,skirting);
        p.box(-5,4,-9,5,4,0,Blocks.BIRCH_PLANKS.defaultBlockState());p.door(0,0,false);
    }
    private static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing);}
    private static void put(StaircaseLeakRooms.Plan p,BlockPos at,StaircaseLeakProps.Kind kind,Direction facing){p.put(at.getX(),at.getY(),at.getZ(),prop(kind,facing));}

    /**
     * Fresh sheets (sheets: The airing cupboard, Fresh sheets, The basket, Lost and found). The back bedroom under the eaves: the
     * bed stripped against the north wall, the airing cupboard built into the corner, the basket of warm washing by the door, and a
     * rag rug in the middle where a sheet can be shaken out and folded between two people.
     */
    private static void bedroom(StaircaseLeakRooms.Plan p){
        shell(p,Blocks.OAK_PLANKS.defaultBlockState());
        p.box(0,1,-9,1,2,-9,Blocks.GLASS.defaultBlockState());
        put(p,BED_FOOT,BED_FOOT_BARE,Direction.NORTH);put(p,BED_HEAD,BED_HEAD_BARE,Direction.NORTH);
        p.put(-4,0,-7,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));p.prop(-4,1,-7,CANDLE);
        put(p,AIRING_LOW_AT,AIRING_LOW,Direction.WEST);put(p,AIRING_HIGH_AT,AIRING_HIGH,Direction.WEST);p.box(4,0,-8,4,3,-6,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());p.box(3,2,-8,3,3,-6,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());p.put(3,0,-8,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());p.put(3,1,-8,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());p.put(3,0,-6,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());p.put(3,1,-6,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());
        put(p,BASKET,BASKET_FULL,Direction.NORTH);put(p,FOLD,FOLD_EMPTY,Direction.SOUTH);
        p.put(4,0,-3,furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST));p.prop(0,3,-4,LIGHT);
        p.box(-2,-1,-4,0,-1,-2,Blocks.LIGHT_GRAY_WOOL.defaultBlockState());
    }
    /**
     * Mending day (sheets: Thursday, The hem, Mending day, The small tin, The window latch). The front room by the lamp: Ruth's
     * armchair, the little table with the biscuit tin of buttons, the blue coat laid over the sewing table with the sewing box beside
     * it, and the standing lamp drawn up too close, so it throws a shadow over the work.
     */
    private static void mending(StaircaseLeakRooms.Plan p){
        shell(p,Blocks.SPRUCE_PLANKS.defaultBlockState());
        p.box(-3,1,-9,-2,2,-9,Blocks.GLASS.defaultBlockState());p.prop(4,2,-8,CLOCK);
        p.put(ARMCHAIR.getX(),0,ARMCHAIR.getZ(),furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH));
        p.put(-2,0,-6,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH));put(p,TIN,BUTTON_TIN,Direction.SOUTH);
        put(p,COAT,COAT_SLEEVE,Direction.SOUTH);put(p,SEWING,SEWING_BOX,Direction.SOUTH);put(p,LAMP_CLOSE,LAMP_STAND,Direction.SOUTH);
        p.put(-3,0,-3,furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST));p.prop(-3,1,-2,CUP_CREAM);p.put(-3,0,-2,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP));
        p.box(-2,-1,-7,2,-1,-3,Blocks.RED_WOOL.defaultBlockState());
    }
    /**
     * First light (sheets: Breakfast, For tomorrow, The sink, The loaf, The saucepan). The kitchen before dawn, the counter along the
     * north wall with the cooker, the kettle, two cups, the potatoes and Bell's plate; through the open half of the room, the settee
     * with somebody asleep on it under a blanket that rises and falls, and a small table by its head.
     */
    private static void dawn(StaircaseLeakRooms.Plan p){
        shell(p,Blocks.SMOOTH_STONE.defaultBlockState());
        p.box(-4,-1,-8,1,-1,-1,Blocks.WHITE_TERRACOTTA.defaultBlockState());p.box(2,-1,-8,4,-1,-1,Blocks.SPRUCE_PLANKS.defaultBlockState());
        p.box(1,0,-8,1,3,-6,StaircaseLeakRegistry.WALLPAPER.get().defaultBlockState());p.box(1,3,-5,1,3,-2,StaircaseLeakRegistry.WALLPAPER.get().defaultBlockState());
        p.box(-4,0,-8,0,0,-8,Blocks.SPRUCE_PLANKS.defaultBlockState());p.put(-1,0,-8,Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING,Direction.SOUTH));p.box(-4,0,-7,-4,0,-6,Blocks.SPRUCE_PLANKS.defaultBlockState());
        p.box(-3,2,-9,-2,3,-9,Blocks.BLUE_STAINED_GLASS.defaultBlockState());
        put(p,KETTLE,KETTLE_COLD,Direction.SOUTH);put(p,FIRST_CUP,TEACUP_EMPTY,Direction.SOUTH);put(p,SECOND_CUP,TEACUP_EMPTY,Direction.SOUTH);put(p,POTATO,POTATOES,Direction.EAST);put(p,SUPPER,SUPPER_PLATE,Direction.SOUTH);
        put(p,SETTEE,SETTEE_FEET,Direction.WEST);put(p,SLEEPER,SLEEPER_IN,Direction.WEST);put(p,SIDE,SIDE_EMPTY,Direction.WEST);
        p.prop(2,2,-8,SMALL_LIGHT);p.put(3,0,-8,furniture(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));
    }

    /**
     * The long way home (sheets: On the way home, At the lights, The turn, The bend by the school, The handbrake). The passenger seat
     * of the small car, parked in the street: the drive is the street lamps going by down both sides, the engine and the stops. The
     * driver's seat is empty, its belt fastened.
     */
    private static void drive(StaircaseLeakRooms.Plan p){
        StaircaseLeakRooms.car(p,0,0);p.put(0,0,-1,Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState());
        put(p,RADIO_AT,RADIO,Direction.SOUTH);p.prop(1,0,-1,BREAD_BAG);p.prop(-1,1,0,BELT);put(p,MAP,ROAD_MAP,Direction.SOUTH);put(p,HANDBRAKE,HANDBRAKE_DOWN,Direction.SOUTH);
        put(p,WINDSCREEN,WINDSCREEN_FOGGED,Direction.SOUTH);p.prop(-1,1,-1,CAR_WHEEL);p.put(0,5,0,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,6));
    }
    /**
     * Washing day (sheets: After the rain, The blanket, A stubborn stain, The clothes horse). The back yard behind the kitchen: the
     * washing line between two posts, the basket of wet washing, the blanket folded on the back step and the clothes horse by the door.
     */
    private static void yard(StaircaseLeakRooms.Plan p){
        var brick=Blocks.BRICKS.defaultBlockState();p.box(-5,-1,-11,5,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        p.box(-5,0,0,5,4,0,brick);p.box(-3,1,0,-2,2,0,Blocks.GLASS.defaultBlockState());p.door(0,0,false);p.box(-5,5,0,5,5,0,Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        p.box(-1,-1,-1,1,-1,-1,Blocks.STONE_BRICKS.defaultBlockState());
        var fence=Blocks.OAK_FENCE.defaultBlockState();p.box(-5,0,-11,5,0,-11,fence.setValue(FenceBlock.EAST,true).setValue(FenceBlock.WEST,true));p.box(-5,0,-10,-5,0,-1,fence.setValue(FenceBlock.NORTH,true).setValue(FenceBlock.SOUTH,true));p.box(5,0,-10,5,0,-1,fence.setValue(FenceBlock.NORTH,true).setValue(FenceBlock.SOUTH,true));
        p.box(-4,0,-6,-4,2,-6,fence);p.box(3,0,-6,3,2,-6,fence);for(var at:LINE)put(p,at,LINE_EMPTY,Direction.SOUTH);
        put(p,WASHING,WET_BASKET,Direction.SOUTH);put(p,BLANKET,BLANKET_FOLDED,Direction.SOUTH);put(p,HORSE,CLOTHES_HORSE,Direction.WEST);
        p.put(-4,0,-9,Blocks.COMPOSTER.defaultBlockState());p.put(4,0,-9,Blocks.POTTED_FERN.defaultBlockState());p.put(0,4,1,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9));
    }
    /**
     * The lay-by (sheets: A longer way, The queue, The petrol station, The lay-by). A gravel lay-by off a country road in the drizzle:
     * the car pulled in with its passenger door open and Bell asleep against its far window, sandwiches and two coffees on the bonnet,
     * hedges along the road.
     */
    private static void layby(StaircaseLeakRooms.Plan p){
        var night=Blocks.BLACK_CONCRETE.defaultBlockState();
        p.box(-12,-3,-20,12,-1,11,Blocks.DIRT.defaultBlockState());p.box(-12,0,-20,-12,8,11,night);p.box(12,0,-20,12,8,11,night);p.box(-12,0,-20,12,8,-20,night);p.box(-12,0,11,12,8,11,night);p.box(-12,8,-20,12,8,11,night);
        p.box(-11,-1,-19,11,-1,10,Blocks.GRASS_BLOCK.defaultBlockState());p.box(-7,-1,-10,7,-1,0,Blocks.GRAVEL.defaultBlockState());
        p.box(-11,-1,-15,11,-1,-11,Blocks.GRAY_CONCRETE.defaultBlockState());for(int x=-11;x<=11;x+=3)p.put(x,-1,-13,Blocks.WHITE_CONCRETE.defaultBlockState());
        var hedge=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);p.box(-11,0,-17,11,1,-16,hedge);p.box(-8,0,-10,-8,1,1,hedge);p.box(8,0,-10,8,1,1,hedge);p.box(-11,0,1,11,1,2,hedge);
        p.box(-3,0,-17,-2,1,-16,Blocks.AIR.defaultBlockState());p.put(-3,0,-17,Blocks.SPRUCE_FENCE_GATE.defaultBlockState());p.put(-2,0,-17,Blocks.SPRUCE_FENCE_GATE.defaultBlockState());
        StaircaseLeakRooms.car(p,LAYBY_CAR.getX(),LAYBY_CAR.getZ());p.box(-1,0,-8,1,0,-8,Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState());
        p.box(2,1,-6,2,2,-4,Blocks.AIR.defaultBlockState());put(p,BELL_AT,BELL_WINDOW,Direction.EAST);put(p,WHEEL_AT,CAR_WHEEL,Direction.SOUTH);
        put(p,SANDWICH_AT,SANDWICHES,Direction.SOUTH);put(p,COFFEE_AT,COFFEES,Direction.SOUTH);
        p.box(5,0,-2,5,2,-2,Blocks.SPRUCE_FENCE.defaultBlockState());p.put(5,3,-2,Blocks.LANTERN.defaultBlockState());
    }
    /**
     * The last landing (sheets: The landing, Our supper, and the rest of the stair sheets). A piece of the great staircase's own stone
     * in the dark, with the household's camp: the cups and spoons, the lamp, the last candle and the warm knife, three tins, the
     * morning list on a bag, a washing line between two brackets with a space in the middle, a coat spread on the seat by the wall.
     */
    private static void landing(StaircaseLeakRooms.Plan p){
        var stone=HouseBlocks.STAIRCASE_STONE.get().defaultBlockState();var dark=Blocks.DEEPSLATE_TILES.defaultBlockState();
        p.box(-12,-3,-20,12,8,11,Blocks.AIR.defaultBlockState());p.box(-12,-3,-20,12,-3,11,dark);p.box(-12,-2,-20,-12,8,11,dark);p.box(12,-2,-20,12,8,11,dark);p.box(-12,-2,-20,12,8,-20,dark);p.box(-12,-2,11,12,8,11,dark);p.box(-12,8,-20,12,8,11,dark);
        p.box(-5,-1,-9,5,-1,1,stone);p.box(-5,-2,-9,5,-2,1,stone);
        var bars=Blocks.IRON_BARS.defaultBlockState();p.box(-5,0,-9,-5,0,0,bars.setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true));p.box(5,0,-9,5,0,0,bars.setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true));
        p.box(-5,0,-9,5,3,-9,dark);p.box(-5,4,-9,5,4,-9,Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        p.door(0,0,false);p.box(-1,0,0,-1,1,0,dark);p.box(1,0,0,1,1,0,dark);p.box(-1,2,0,1,2,0,dark);
        p.box(-4,0,0,-2,0,0,bars.setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));p.box(2,0,0,4,0,0,bars.setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));
        put(p,CUPS,CAMP_CUPS,Direction.SOUTH);put(p,SPOONS,CAMP_SPOONS,Direction.SOUTH);put(p,CAMP_LAMP_AT,CAMP_LAMP,Direction.WEST);put(p,CANDLE_AT,CAMP_CANDLE,Direction.SOUTH);
        put(p,TINS,CAMP_TINS,Direction.SOUTH);put(p,LIST,CAMP_LIST,Direction.EAST);put(p,COAT_SEAT,CAMP_COAT,Direction.WEST);put(p,ROLL,CAMP_ROLL,Direction.SOUTH);
        p.put(-2,2,-8,Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.X));p.put(2,2,-8,Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.X));
        put(p,new BlockPos(-1,2,-8),CAMP_LINE,Direction.SOUTH);put(p,new BlockPos(1,2,-8),CAMP_LINE,Direction.SOUTH);
        p.put(0,5,-4,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,4));
    }

    /**
     * Sunday lunch (sheets: The good cups, The towel rail, The table, Sunday lunch). A dining room nine by eight, the serving
     * hatch through to a dark kitchen in the north wall, a table of six with a seventh chair against the wall, the sideboard with
     * the cutlery, the cupboard with the good cups, and the flowers under the window.
     */
    private static void dining(StaircaseLeakRooms.Plan p){
        var paper=StaircaseLeakRegistry.WALLPAPER.get().defaultBlockState();var floor=Blocks.DARK_OAK_PLANKS.defaultBlockState();var skirting=Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        p.box(-5,-1,-9,5,-1,0,floor);p.box(-5,0,-9,-5,3,0,paper);p.box(5,0,-9,5,3,0,paper);p.box(-5,0,0,5,3,0,paper);p.box(-5,0,-9,5,3,-9,paper);
        p.box(-5,0,-9,5,0,-9,skirting);p.box(-5,0,0,5,0,0,skirting);p.box(-5,0,-9,-5,0,0,skirting);p.box(5,0,-9,5,0,0,skirting);
        p.box(-5,4,-9,5,4,0,Blocks.BIRCH_PLANKS.defaultBlockState());p.door(0,0,false);
        // The window over the flowers, in the east wall.
        p.box(5,1,-3,5,2,-2,Blocks.GLASS.defaultBlockState());
        // The hatch through to the kitchen: three cells wide, one high, a tray on its sill.
        for(var at:HATCH){p.put(at.getX(),at.getY(),at.getZ(),prop(HATCH_EMPTY,Direction.SOUTH));p.put(at.getX(),2,at.getZ(),Blocks.AIR.defaultBlockState());}
        p.box(-2,1,-9,-2,2,-9,skirting);p.box(2,1,-9,2,2,-9,skirting);p.box(-2,3,-9,2,3,-9,skirting);
        // The kitchen beyond, dark but for the light over the cooker: a counter, a cooker and a pan.
        p.box(-4,-1,-13,4,-1,-10,Blocks.SMOOTH_STONE.defaultBlockState());p.box(-4,0,-13,4,3,-13,Blocks.WHITE_TERRACOTTA.defaultBlockState());p.box(-4,0,-13,-4,3,-10,Blocks.WHITE_TERRACOTTA.defaultBlockState());p.box(4,0,-13,4,3,-10,Blocks.WHITE_TERRACOTTA.defaultBlockState());p.box(-4,4,-13,4,4,-10,Blocks.BIRCH_PLANKS.defaultBlockState());
        p.box(-3,0,-12,3,0,-12,Blocks.SPRUCE_PLANKS.defaultBlockState());p.put(0,0,-12,Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING,Direction.SOUTH).setValue(FurnaceBlock.LIT,true));p.prop(2,2,-12,SMALL_LIGHT);
        // The table: six places on six cells, the visitor's end and the bread board's end.
        for(int i=0;i<PLACES.length;i++){var at=PLACES[i];p.put(at.getX(),0,at.getZ(),prop(PLACE_BARE,i<3?Direction.SOUTH:Direction.NORTH));}
        p.put(VISITOR.getX(),0,VISITOR.getZ(),prop(TABLE_END,Direction.EAST));p.put(BREAD.getX(),0,BREAD.getZ(),prop(BREAD_BOARD,Direction.EAST));
        for(var c:CHAIRS)p.put(c.getX(),0,c.getZ(),chair(chairFacing(c)));
        p.put(SEVENTH.getX(),0,SEVENTH.getZ(),chair(Direction.WEST));
        // The sideboard with the cutlery, a clock over it; the good cups on the chest of drawers; the flowers under the window.
        p.put(SIDEBOARD.getX(),0,SIDEBOARD.getZ(),prop(SIDEBOARD_SHUT,Direction.EAST));p.put(-4,0,-5,prop(SIDEBOARD_SHUT,Direction.EAST));p.prop(-4,2,-8,CLOCK);
        p.put(4,0,-7,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS).setValue(HouseholdFurnitureBlock.FACING,Direction.WEST));
        p.put(CUPBOARD.getX(),CUPBOARD.getY(),CUPBOARD.getZ(),prop(CUPBOARD_CUPS,Direction.WEST));
        p.put(4,0,-2,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE).setValue(HouseholdFurnitureBlock.FACING,Direction.WEST));
        p.put(VASE.getX(),VASE.getY(),VASE.getZ(),prop(VASE_FLOWERS,Direction.WEST));
        p.prop(0,3,-4,LIGHT);p.prop(0,3,-6,LIGHT_OFF);
        p.box(-3,-1,-7,3,-1,-2,Blocks.RED_WOOL.defaultBlockState());p.box(-2,-1,-6,2,-1,-3,Blocks.BROWN_WOOL.defaultBlockState());
        // The rug is set into the floor, so the chairs stand level on it.
    }
}
