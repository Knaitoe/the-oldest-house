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

    public static Vec3 spawn(BlockPos base,StaircaseLeakRooms.Kind kind){return new Vec3(base.getX()+.5,base.getY(),base.getZ()-1.5);}
    /** The room's walking space; outside it (through the door) the visit ends. */
    public static boolean inside(BlockPos base,StaircaseLeakRooms.Kind kind,Vec3 p){
        double x=p.x-base.getX(),y=p.y-base.getY(),z=p.z-base.getZ();
        return switch(kind){
            default->x>-4.7&&x<5.7&&z>-8.8&&z<.5&&y>=-.5&&y<4.5;
        };
    }
    static void authored(StaircaseLeakRooms.Kind kind,StaircaseLeakRooms.Plan p){
        switch(kind){
            case DINING->dining(p);
            case BEDROOM->bedroom(p);
            case MENDING->mending(p);
            case DAWN->dawn(p);
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
