package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Small authored objects; choreography never puts these into a player's inventory. */
public final class StaircaseLeakProps extends Block {
    public enum Kind implements StringRepresentable {
        CUP_BLUE,CUP_CREAM,CUP_RED,HOOK,CUP_HUNG_BLUE,CUP_HUNG_CREAM,CUP_HUNG_RED,
        TOWEL,WASHING,PLATE,GERANIUM,GERANIUM_PLATE,SINK_FULL,SINK_EMPTY,SINK_CLEAN,
        LAUNDRY,SOCK_SINGLE,SHIRTS,TROUSERS,LIST_SPOT,LIST,
        RADIO,RADIO_OFF,BREAD_BAG,CAR_SEAT,BELT,
        DRAWER,DRAWER_OPEN,CANDLE,BUTTON_TIN,CLOCK,APRON,LIGHT,LIGHT_OFF,SMALL_LIGHT,
        // 0.4.214, Sunday lunch: the table's places (bare, laid, laid with a fork that doesn't match, a cup, a bowl), its two ends, the
        // sideboard with the cutlery, the cupboard of good cups, the flowers on the sill and the hatch to the kitchen.
        PLACE_BARE,PLACE_LAID,PLACE_ODD,PLACE_CUP,PLACE_BOWL,TABLE_END,TABLE_END_LAID,TABLE_END_CUP,TABLE_PLAIN,BREAD_BOARD,BREAD_CUT,
        SIDEBOARD_SHUT,SIDEBOARD_OPEN,CUPBOARD_CUPS,CUPBOARD_EMPTY,VASE_FLOWERS,VASE_CUP,HATCH_EMPTY,HATCH_BOWLS,
        // 0.4.214, Fresh sheets: the bed's two halves, the airing cupboard, the basket, a sheet being folded and a sock.
        BED_FOOT_BARE,BED_FOOT_MADE,BED_HEAD_BARE,BED_HEAD_MADE,BED_HEAD_PLAIN,BED_HEAD_STRIPED,AIRING_LOW,AIRING_LOW_EMPTY,AIRING_HIGH,AIRING_HIGH_STACKED,
        BASKET_FULL,BASKET_HALF,BASKET_EMPTY,BASKET_STRIPED,BASKET_SOCK,FOLD_EMPTY,SHEET_ONE_END,SHEET_LIFTED,SHEET_FOLDED,SHEET_CREASED,FLOOR_SOCK,
        // Mending day: the standing lamp, the sewing box and needle, the blue coat on its table and the button tin open.
        LAMP_STAND,SEWING_BOX,SEWING_THREADED,COAT_SLEEVE,COAT_TURNED,COAT_SHUT,COAT_SEWN,COAT_BUTTONED,BUTTON_TIN_OPEN,
        // First light: the kettle, the cups, the potatoes, Bell's plate, the settee with someone asleep under a blanket and its side table.
        KETTLE_COLD,KETTLE_ON,KETTLE_BOILED,TEACUP_EMPTY,TEACUP_FULL,POTATOES,POTATOES_PEELED,SUPPER_PLATE,SUPPER_WRAPPED,SUPPER_NAMED,
        SLEEPER_IN,SLEEPER_OUT,SLEEPER_AWAKE,SETTEE_FEET,SIDE_EMPTY,SIDE_CUP,
        // The long way home: the handbrake, the fogged windscreen, the road map on the dash.
        HANDBRAKE_DOWN,HANDBRAKE_UP,WINDSCREEN_FOGGED,WINDSCREEN_WIPED,ROAD_MAP,
        // Washing day: the basket of wet washing, the line, the blanket at the back door, the clothes horse.
        WET_BASKET,WET_BASKET_EMPTY,DRY_BASKET,LINE_EMPTY,LINE_SHIRT,LINE_SOAKED,BLANKET_FOLDED,BLANKET_ONE_END,BLANKET_SHAKEN,CLOTHES_HORSE,CLOTHES_HORSE_SLEEVE,
        // The lay-by: sandwiches and two coffees on the bonnet, the wrapper, Bell's window and the horn.
        SANDWICHES,WRAPPER,WRAPPER_FOLDED,COFFEES,BELL_WINDOW,CAR_WHEEL,
        // The last landing: the household's camp.
        CAMP_CUPS,CAMP_CUPS_COUNTED,CAMP_SPOONS,CAMP_SPOONS_COUNTED,CAMP_LAMP,CAMP_LAMP_LIT,CAMP_CANDLE,CAMP_CANDLE_CUT,CAMP_TINS,CAMP_LIST,CAMP_LINE,CAMP_COAT,CAMP_ROLL,CAMP_KEPT;
        /** The table's cells and the sideboard stand solid at table height; the hatch fills its opening. */
        public boolean table(){return name().startsWith("PLACE_")||name().startsWith("TABLE_")||name().startsWith("BREAD_");}
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public StaircaseLeakProps(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.CUP_BLUE).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
    public static BlockState state(Kind kind){return StaircaseLeakRegistry.PROP.get().defaultBlockState().setValue(KIND,kind);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,BlockStateProperties.HORIZONTAL_FACING);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var k=s.getValue(KIND);if(k.table())return box(0,0,0,16,13,16);return box(1,0,1,15,15,15);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var k=s.getValue(KIND);
        if(k==Kind.DRAWER||k==Kind.DRAWER_OPEN)return box(1,0,1,15,16,15);
        if(k.table())return box(0,0,0,16,12,16);
        if(k.name().startsWith("BED_")||k.name().startsWith("SLEEPER_")||k==Kind.SETTEE_FEET)return box(0,0,0,16,9,16);
        if(k.name().startsWith("AIRING_")||k==Kind.WINDSCREEN_FOGGED||k==Kind.WINDSCREEN_WIPED||k==Kind.BELL_WINDOW)return Shapes.block();
        if(k==Kind.CLOTHES_HORSE||k==Kind.CLOTHES_HORSE_SLEEVE)return box(1,0,6,15,14,10);
        if(k.name().startsWith("COAT_")||k.name().startsWith("SIDE_"))return box(0,0,0,16,11,16);
        if(k==Kind.SIDEBOARD_SHUT||k==Kind.SIDEBOARD_OPEN)return box(0,0,0,16,14,16);
        if(k==Kind.HATCH_EMPTY||k==Kind.HATCH_BOWLS)return Shapes.block();
        return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(BlockStateProperties.HORIZONTAL_FACING,r.rotate(s.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
}
