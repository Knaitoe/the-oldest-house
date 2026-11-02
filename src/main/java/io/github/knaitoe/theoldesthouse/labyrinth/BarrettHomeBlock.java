package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Barretts' house (0.4.121): the walls, blinds and garage of the family's own home, the things that show somebody
 * lives in it, and the film crew's gear spread through it. tools/draw_barrett_home.py draws the models, authored facing
 * north. The double bed and the tub draw past their own block; BED_FILL gives the cells they cover their body.
 */
public final class BarrettHomeBlock extends Block {
    public static final MapCodec<BarrettHomeBlock> CODEC=simpleCodec(BarrettHomeBlock::new);
    public enum Kind implements StringRepresentable {
        PAINT,CONCRETE,DRYWALL,WINDOW,GARAGE_DOOR,STOREY,CEILING_LIGHT,CONSOLE,COAT_HOOKS,SHOE_RACK,PHOTOS,CALENDAR,FRIDGE,FRIDGE_TOP,
        SOFA_A,SOFA_B,TV_STAND,BOOKSHELF,PLANT,LAUNDRY,CLOTHES,DESK,DESK_KID,POSTER_A,POSTER_B,DRESSER,DOUBLE_BED,BED_FILL,
        TOILET,TUB,VANITY,WASHER,DRYER,BOXES,WATER_HEATER,SHELVES,CABLE,CABLE_CORNER,SOFTBOX,DIRECTORS_CHAIR,VILLAGE_CART,
        APPLE_BOXES,SANDBAGS,CRAFT_TABLE,FOLDING_TABLE,OFFICE_CHAIR,
        /** 0.4.202 (the owner's notes on the Barretts' house): the upper column of a double window; the family's own photographs,
         *  the portrait over the door, the crucifix, Merry's drawing and the bathroom prints; the bathrooms' tub-shower, vanities,
         *  stool, sorter, towels, mat, tiled floor and wainscot; the living room's coffee table, lamp and Merry's toys; the den's
         *  dust sheet and schedule board; the kitchen's cork board, backpacks and fruit; the girls' rooms; the linen closet; the
         *  parents' nightstands. Drawn by tools/draw_barrett_home.py. */
        WINDOW_HEAD,PHOTO_WEDDING,PHOTO_MARJORIE,PHOTO_MERRY,PHOTO_BEACH,PHOTO_BABY,PHOTO_SISTERS,PORTRAIT,CRUCIFIX,MERRY_DRAWING,
        PRINT_SOAP,PRINT_WHALE,TUB_SHOWER,VANITY_FAMILY,VANITY_KIDS,STEP_STOOL,HAMPER,TOWEL_BAR,DIRTY_TOWELS,BATH_MAT,STOREY_TILE,
        BATH_WALL,COFFEE_TABLE,FLOOR_LAMP,TOYS,COVERED,SCHEDULE_BOARD,CORK_BOARD,BACKPACKS,FRUIT_BOWL,STUFFED,CEILING_STARS,DRAWINGS,
        FAIRY_LIGHTS,POSTER_C,PINBOARD,GUITAR,BOOMBOX,LINEN_SHELVES,NIGHTSTAND_JOHN,NIGHTSTAND_SARAH,
        /** 0.4.220 (the owner's walk-through): the second coat rail, the sideboard's halves, the kitchen's stove, hood, wall cabinets,
         *  microwave, toaster, coffee maker and dishwasher, the crew's field monitor, and a rug of each room's own. */
        COAT_HOOKS_B,SIDEBOARD_L,SIDEBOARD_R,RANGE,RANGE_HOOD,WALL_CABINET,MICROWAVE,TOASTER,COFFEE_MAKER,DISHWASHER,FIELD_MONITOR,
        RUG_RUNNER,RUG_RUNNER_NARROW,RUG_LIVING,MERRY_RUG,RUG_SHAG,
        /** 0.4.242 (owner: "camera tracks breaking rug"): the hall runner with the crew's cable lying across it. */
        RUG_RUNNER_CABLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean cube(){return ordinal()<=STOREY.ordinal()||this==WINDOW_HEAD||this==STOREY_TILE||this==BATH_WALL;}
        /** Both halves of a window. */
        public boolean window(){return this==WINDOW||this==WINDOW_HEAD;}
        /** Hung on a wall or a ceiling: walked past, never into. */
        public boolean flat(){return switch(this){case CEILING_LIGHT,COAT_HOOKS,PHOTOS,CALENDAR,CLOTHES,POSTER_A,POSTER_B,CABLE,CABLE_CORNER,PHOTO_WEDDING,PHOTO_MARJORIE,PHOTO_MERRY,
            PHOTO_BEACH,PHOTO_BABY,PHOTO_SISTERS,PORTRAIT,CRUCIFIX,MERRY_DRAWING,PRINT_SOAP,PRINT_WHALE,TOWEL_BAR,DIRTY_TOWELS,BATH_MAT,TOYS,SCHEDULE_BOARD,
            CORK_BOARD,BACKPACKS,CEILING_STARS,DRAWINGS,FAIRY_LIGHTS,POSTER_C,PINBOARD,STUFFED,COAT_HOOKS_B,RUG_RUNNER,RUG_RUNNER_NARROW,RUG_LIVING,MERRY_RUG,RUG_SHAG,RUG_RUNNER_CABLE->true;default->false;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    /** 0.4.242: whether a light gives its light. The house's own states are always lit; GhostsLights sends one reader unlit copies once Merry has gone out of the door on the last day. */
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT=net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public BarrettHomeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.PAINT).setValue(FACING,Direction.NORTH).setValue(LIT,true));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,LIT);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.BARRETT_HOME.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind){return of(kind,Direction.NORTH);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.BARRETT_HOME.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){if(!s.getValue(LIT))return 0;return switch(s.getValue(KIND)){case CEILING_LIGHT->14;case SOFTBOX->15;case WINDOW,WINDOW_HEAD->6;case VILLAGE_CART->5;case FLOOR_LAMP->12;case FAIRY_LIGHTS->7;case TV_STAND->6;case CEILING_STARS->3;case FIELD_MONITOR->5;case RANGE_HOOD->9;default->0;};}

    private static VoxelShape base(Kind k){return switch(k){
        case PAINT,CONCRETE,DRYWALL,WINDOW,GARAGE_DOOR,STOREY,FOLDING_TABLE,WINDOW_HEAD,STOREY_TILE,BATH_WALL->Shapes.block();
        case PHOTO_WEDDING,PHOTO_MARJORIE,PHOTO_MERRY,PHOTO_BABY->Block.box(3,3,15,13,14,16);case PHOTO_BEACH,PHOTO_SISTERS->Block.box(2,4,15,14,13,16);
        case PORTRAIT->Block.box(0,0,15,16,16,16);case CRUCIFIX->Block.box(3,1,15,13,15,16);case MERRY_DRAWING,DRAWINGS,CORK_BOARD,PINBOARD->Block.box(1,1,15,15,15,16);
        case PRINT_SOAP,PRINT_WHALE->Block.box(0,0,15,16,16,16);case TUB_SHOWER->Block.box(0,0,0,16,9,16);
        case VANITY_FAMILY,VANITY_KIDS->Block.box(0,0,4,16,13,16);case STEP_STOOL->Block.box(3,0,4,13,4.5,12);case HAMPER->Block.box(1,0,4,15,14,13);
        case TOWEL_BAR->Block.box(0,1,14,16,12,16);case DIRTY_TOWELS->Block.box(1,0,2,15,3,15);case BATH_MAT->Block.box(1,0,2,15,.5,14);
        case COFFEE_TABLE->Block.box(0,0,2,16,8,14);case FLOOR_LAMP->Block.box(5,0,5,11,16,11);case TOYS->Block.box(2,0,3,12,7,12);
        case COVERED->Shapes.or(Block.box(0,0,1,16,9,16),Block.box(0,9,9,16,15,16));case SCHEDULE_BOARD->Block.box(1,2,14,15,15,16);
        case BACKPACKS->Block.box(1,1,12,15,12.5,16);case FRUIT_BOWL->Block.box(4,0,4,12,7,12);case STUFFED->Block.box(1,0,4,15,8,13);
        case CEILING_STARS->Block.box(0,15.8,0,16,16,16);case FAIRY_LIGHTS->Block.box(0,9,15.6,16,16,16);case POSTER_C->Block.box(2,1,15.6,14,15,16);
        case GUITAR->Block.box(4,0,8,12,16,15);case BOOMBOX->Block.box(1,0,6,14,8,14.5);case LINEN_SHELVES->Block.box(0,0,7,16,16,16);
        case NIGHTSTAND_JOHN,NIGHTSTAND_SARAH->Block.box(.5,0,2.5,15.5,12,14.5);
        case COAT_HOOKS_B->Block.box(0,0,12.5,16,14.5,16);case SIDEBOARD_L,SIDEBOARD_R->Block.box(0,0,3,16,13,16);
        case RANGE->Block.box(0,0,1,16,15,16);case RANGE_HOOD->Block.box(0,0,7,16,16,16);case WALL_CABINET->Block.box(0,0,9,16,14,16);
        case MICROWAVE->Block.box(2,0,5,15,8.5,14);case TOASTER->Block.box(3,0,6,14.5,6,12);case COFFEE_MAKER->Block.box(4,0,6,12,11,14);
        case DISHWASHER->Shapes.block();case FIELD_MONITOR->Block.box(1,0,8,15,14,13);
        case RUG_RUNNER,RUG_RUNNER_NARROW,RUG_LIVING,MERRY_RUG,RUG_SHAG->Block.box(0,0,0,16,.5,16);
        case RUG_RUNNER_CABLE->Block.box(0,0,0,16,.75,16);
        case CEILING_LIGHT->Block.box(4,14.5,4,12,16,12);
        case CONSOLE->Block.box(0,0,4,16,12.5,15.5);
        case COAT_HOOKS->Block.box(0,2,11.5,16,14.5,16);
        case SHOE_RACK->Block.box(0,0,9,16,8,16);
        case PHOTOS->Block.box(0,2,15.5,16,14,16);
        case CALENDAR->Block.box(4,2,15.6,12,14,16);
        case FRIDGE->Block.box(1,0,2,15,16,16);
        case FRIDGE_TOP->Block.box(1,0,1.5,15,14,16);
        case SOFA_A,SOFA_B->Shapes.or(Block.box(0,0,3,16,7,16),Block.box(0,7,13,16,15,16));
        case TV_STAND->Shapes.or(Block.box(0,0,5,16,5,15),Block.box(0,6.5,9.5,16,16,10.5));
        case BOOKSHELF->Block.box(0,0,4,16,16,16);
        case PLANT->Block.box(4.5,0,4.5,11.5,7,11.5);
        case LAUNDRY->Block.box(2,0,3,14,9.5,13);
        case CLOTHES->Block.box(0,0,0,16,.6,16);
        case DESK->Block.box(0,0,2,16,12,15);
        case DESK_KID->Block.box(0,0,3,16,11,15);
        case POSTER_A,POSTER_B->Block.box(1,1,15.6,15,15,16);
        case DRESSER->Block.box(0,0,4,16,13,16);
        case DOUBLE_BED,BED_FILL->Block.box(0,0,0,16,10,16);
        case TOILET->Shapes.or(Block.box(4,0,2,12,9,12),Block.box(3,8,12,13,16,16));
        case TUB->Block.box(0,0,0,16,9,16);
        case VANITY->Block.box(0,0,4,16,13,16);
        case WASHER,DRYER->Block.box(1,0,2,15,15,16);
        case BOXES->Shapes.or(Block.box(0,0,0,16,8,16),Block.box(2,8,3,14,15,13));
        case WATER_HEATER->Block.box(3,0,3,13,16,13);
        case SHELVES->Block.box(0,0,8,16,16,16);
        case CABLE,CABLE_CORNER->Block.box(0,0,0,16,.25,16);
        case SOFTBOX->Block.box(6,0,6,10,16,10);
        case DIRECTORS_CHAIR->Shapes.or(Block.box(2,0,3,14,9,13.5),Block.box(2,9,12,14,16,13.5));
        case VILLAGE_CART->Block.box(.5,0,3,15.5,16,13);
        case APPLE_BOXES->Block.box(1,0,2,15,10,14);
        case SANDBAGS->Block.box(2,0,3,14,7,13);
        case CRAFT_TABLE->Block.box(0,0,2,16,12,14);
        case OFFICE_CHAIR->Shapes.or(Block.box(3,0,3,13,8,13),Block.box(3,8,12.5,13,16,13.5));
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var all=SHAPES.computeIfAbsent(s.getValue(KIND),x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(base(s.getValue(KIND)),s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND).flat()?Shapes.empty():shape(s);
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    // Walls and windows keep the light where it is; everything else lets it past.
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?15:0;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    /** The cells a model drawn past its own block covers, for its BED_FILL bodies: the bed's three, the tub's one. */
    public static java.util.List<BlockPos> covers(BlockPos at,Kind kind,Direction facing){
        var right=facing.getClockWise();var back=facing.getOpposite();
        return switch(kind){
            case DOUBLE_BED->java.util.List.of(at.relative(right),at.relative(back),at.relative(right).relative(back));
            case TUB,TUB_SHOWER->java.util.List.of(at.relative(back));
            default->java.util.List.of();
        };
    }
}
