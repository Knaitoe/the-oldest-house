package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.522, the Pell house's things (docs/PELL_HOUSE_THINGS_0_4_522.md): what is in each room, drawn facing north by
 * tools/draw_pell_things.py and turned by FACING. STAGE is what a thing shows at the time; what each stage is, is written beside the
 * thing's line in PellTexts.thing.
 * <ul>
 * <li>Two cells side by side (the table, the chore chart, the three boxes, the freezer shut or open, the pegboard, the kennel): half 0 is the cell on
 * the left as one faces the front and half 1 the cell to its right ({@link #right}). The freezer's, the pegboard's and the kennel's half
 * is STAGE's bit 0.</li>
 * <li>Two cells end to end (the bed, the mattress, the camp bed, the bath): end 0 is the head and end 1 the cell FACING points to
 * ({@link #ahead}).</li>
 * <li>Two cells stacked (the fridge and its top, the wardrobe, the height marks, the tapes): the upper is the cell above. The
 * wardrobe's and the marks' upper cell is STAGE's bit 0.</li>
 * <li>CIRCLE: the circle painted on Callum's floor, one floor cell of it in each block (in place of the carpet), never turned: which
 * cell it is, is its FACING and STAGE ({@link #circle}).</li>
 * </ul>
 */
public final class PellThing extends HorizontalDirectionalBlock {
    public static final MapCodec<PellThing> CODEC=simpleCodec(PellThing::new);
    public enum Kind implements StringRepresentable {
        COUNTER,SINK,STOVE,FRIDGE,FRIDGE_TOP,KNIFE_BLOCK,LUNCHBOXES,CUPBOARD_UPPER,TABLE,CHAIR,CHORE_CHART,CALENDAR,
        RECLINER,TV_CABINET,MANTEL,WINDOW_SEAT,SIDE_TABLE,FLOOR_LAMP,PHOTOS,HOOKS,COAT_HOOK,
        BED,DRAWERS,DESK,BOOKSHELF,WARDROBE,SWITCH,MARKS,BORDER,
        MATTRESS,CIRCLE,SHOES,CUTLERY,VANITY,MIRROR,BATH,TOILET,
        FREEZER,STOOL,POWER,CORD,TROUGH,BUCKET,CHLORINE,WASHER,
        CAMP_BED,BULB,BOX_WINTER,BOX_BOOKS,BOX_DONT,PEGBOARD,TOWEL,
        TAPES,WORKBENCH,SHED_TV,TIN,TRANSCRIPT,SHED_CALENDAR,
        STAKE,KENNEL,BOWL,POOL_CHAIR,PUMP,LANTERN,PHONE,FREEZER_OPEN,FOR_SALE,MARKS_AFTER,PHOTO_BROKEN,
        SEEDLING,FLOWERS,EDGING;
        // 0.4.548: PHOTO_BROKEN, the last school photograph with its glass broken (PellMark).
        // 0.4.574, the front beds (docs/PELL_HOUSE_NOTES_0_4_574.md): SEEDLING, Tilly's stake, which grows with the visits
        // (PellWeek.dress); FLOWERS, June's geraniums east of the step; EDGING, the bricks on edge along the beds, in the lawn's
        // cell on the side it turns to FACING (stage 1 the one brick at a bed's corner). A reader's record keeps what they have
        // put a hand on by a kind's place in this list (PellCompany.saw): new kinds go at its end.
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Flat on a wall, on the floor or hung: nothing a body bumps into. */
        public boolean flat(){return switch(this){case CHORE_CHART,CALENDAR,PHOTOS,HOOKS,COAT_HOOK,SWITCH,MARKS,BORDER,MIRROR,POWER,CORD,BULB,PEGBOARD,TOWEL,
            TRANSCRIPT,SHED_CALENDAR,SHOES,BOWL,STAKE,PHONE,MARKS_AFTER,PHOTO_BROKEN,SEEDLING,FLOWERS,EDGING->true;default->false;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** The circle's cells in the order the art tool numbers its models: a cell's index over four is its FACING here, the rest its STAGE. */
    private static final Direction[] ORDER={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    public PellThing(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.COUNTER).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PellRegistry.THING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    /** Cell i of the circle on Callum's floor: i is its row from the north times three, and its column from the west. */
    public static BlockState circle(int i){return of(Kind.CIRCLE,ORDER[i/4],i%4);}
    /** Which cell of the circle a CIRCLE state is. */
    public static int cell(BlockState s){Direction f=s.getValue(FACING);int at=0;for(int i=0;i<4;i++)if(ORDER[i]==f)at=i;return at*4+s.getValue(STAGE);}
    public static boolean is(BlockState s,Kind kind){return s.is(PellRegistry.THING.get())&&s.getValue(KIND)==kind;}
    public static boolean is(BlockState s){return s.is(PellRegistry.THING.get());}
    /** 0.4.526: what a body sits in (the kitchen chairs), for a reader (HouseSitting) and for the house's people (their renderer).
     *  0.4.527: June's recliner and the window seat too. */
    public static boolean seat(BlockState s){return is(s,Kind.CHAIR)||is(s,Kind.RECLINER)||is(s,Kind.WINDOW_SEAT);}
    /** How high a seat holds the hips, in sixteenths of a block: the kitchen chair's seat is ten up, the recliner's cushion and the
     *  window seat's nine. */
    public static int seatHeight(BlockState s){return is(s,Kind.CHAIR)?10:9;}
    /**
     * 0.4.527: what one of the house's people lies along to sleep, and how high its top is in sixteenths: Nessa's bed, Callum's mattress
     * on the floor, the camp bed in the garage. Each is two cells long, its head in the cell it stands in and its foot in the cell it
     * faces (PellSite.lengthwise), so a sleeper's head is toward the way it does not face.
     */
    public static int lieHeight(BlockState s){return !is(s)?-1:switch(s.getValue(KIND)){case BED->9;case CAMP_BED->8;case MATTRESS->4;default->-1;};}
    /** The head cell of the bed this cell is part of. */
    public static BlockPos pillow(BlockPos at,BlockState s){return s.getValue(STAGE)==1?at.relative(s.getValue(FACING).getOpposite()):at;}
    /**
     * 0.4.531: the camp bed is a bed to the game, so a reader can lie down on it (PellMorning.lieDown). The game lays a sleeper with
     * their head toward the way a bed points; the camp bed points from its head to its foot, so that is the way it does not face. It
     * has no OCCUPIED to set.
     */
    // 0.4.545: and Nessa's bed, for a reader as Tilly to lie in (PellTilly).
    @Override public boolean isBed(BlockState s,BlockGetter level,BlockPos pos,@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity sleeper){return is(s,Kind.CAMP_BED)||is(s,Kind.BED);}
    @Override public void setBedOccupied(BlockState s,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.LivingEntity sleeper,boolean occupied){}
    @Override public Direction getBedDirection(BlockState s,net.minecraft.world.level.LevelReader level,BlockPos pos){return is(s,Kind.CAMP_BED)||is(s,Kind.BED)?s.getValue(FACING).getOpposite():s.getValue(FACING);}
    /** The cell to the right of a thing facing f, as one faces its front: half 1 of a thing two cells wide. */
    public static BlockPos right(BlockPos at,Direction f){return at.relative(f.getCounterClockWise());}
    /** The cell a thing's FACING points to: end 1 of a thing two cells long. */
    public static BlockPos ahead(BlockPos at,Direction f){return at.relative(f);}
    /** The bare bulb lights the garage while it is on (stage 0); the standard lamp lights the chair while it is on (stage 1); the
     * television gives a little while it plays (stage 1); 0.4.530: June's lantern, set down on the grass at night, lights the strip;
     * 0.4.536: the shed's portable television gives less, while a tape plays (stage 1). */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case BULB->s.getValue(STAGE)==0?13:0;case FLOOR_LAMP->s.getValue(STAGE)==1?10:0;
        case TV_CABINET->s.getValue(STAGE)==1?6:0;case SHED_TV->s.getValue(STAGE)==1?4:0;case LANTERN->12;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COUNTER,SINK,STOVE,LUNCHBOXES,KNIFE_BLOCK,CIRCLE->Shapes.block();
            case FRIDGE,FRIDGE_TOP->t(f,0,0,1,16,16,16);
            case FREEZER->t(f,0,0,1,16,14,16);
            case CUPBOARD_UPPER->stage==1?Shapes.block():t(f,0,0,7,16,16,16);
            case TABLE->t(f,0,0,1,16,15,15);
            case CHAIR->Shapes.or(t(f,3,0,3,13,10,13),t(f,3,10,12,13,16,13.5));
            case CHORE_CHART,PHOTOS,PHOTO_BROKEN,HOOKS,PEGBOARD,TRANSCRIPT->t(f,0,0,15,16,16,16);
            case CALENDAR,SHED_CALENDAR->t(f,2,0,15,14,16,16);
            case RECLINER->t(f,1,0,1,15,14,16);
            case TV_CABINET->Shapes.or(t(f,0,0,2,16,10,16),t(f,2,10,4,14,16,15));
            case MANTEL->Shapes.or(t(f,0,0,10,16,14,16),t(f,0,14,8,16,16,16));
            case WINDOW_SEAT->t(f,0,0,2,16,9,16);
            case SIDE_TABLE->Block.box(3,0,3,13,12,13);
            case FLOOR_LAMP->Block.box(5,0,5,11,16,11);
            case COAT_HOOK->t(f,0,0,14.5,16,16,16);
            case BED->stage==0?Shapes.or(Block.box(0,0,0,16,9,16),t(f,0,9,15,16,16,16)):Block.box(0,0,0,16,9,16);
            case DRAWERS->t(f,1,0,3,15,14,16);
            case DESK->t(f,0,0,2,16,14,16);
            case BOOKSHELF->t(f,0,0,8,16,16,16);
            case WARDROBE->t(f,0,0,2,16,16,16);
            case SWITCH->t(f,5,3,15,11,13,16);
            // 0.4.542: wide enough for the N beside the last mark.
            case MARKS,MARKS_AFTER->t(f,1,0,15,11,16,16);
            case BORDER->t(f,0,8,15,16,16,16);
            case MATTRESS->Block.box(0,0,0,16,4,16);
            case SHOES->t(f,2,0,9,14,3.5,15);
            case CUTLERY->t(f,1,0,3,15,15,16);
            case VANITY->t(f,0,0,3,16,15,16);
            case MIRROR->t(f,1,0,14,15,14,16);
            case BATH->stage==0?t(f,0,0,0,16,9,15):t(f,0,0,1,16,9,16);
            case TOILET->t(f,3,0,2,13,15,16);
            case STOOL->Block.box(3,0,3,13,9,13);
            case POWER->t(f,2,1,15,14,15,16);
            case CORD->Block.box(0,0,0,16,.5,16);
            case TROUGH->t(f,1,0,2,15,15,15);
            case BUCKET->Block.box(3,0,3,13,9,13);
            case CHLORINE->Block.box(2,0,2,14,14,14);
            case WASHER->Block.box(1,0,1,15,16,15);
            case CAMP_BED->t(f,1,0,0,15,8,16);
            case BULB->Block.box(6.5,1,6.5,9.5,16,9.5);
            case BOX_WINTER,BOX_BOOKS,BOX_DONT->t(f,0,0,2,16,12,15);
            case TOWEL->t(f,0,0,13,16,3,16);
            case TAPES->t(f,0,0,6,16,16,16);
            case WORKBENCH->t(f,0,0,1,16,15,16);
            case SHED_TV->t(f,1,0,4,15,13,14);
            // 0 on the bench with the brush beside it; 1 open on the grass.
            case TIN->stage==1?Block.box(4,0,4,11,8,11):Block.box(4,0,4,15,8,11);
            // 0.4.574: a stake and what stands in front of it, on the side it faces; a geranium; a run of edging, or its corner brick.
            case STAKE,SEEDLING->t(f,3,0,2,13,16,9);
            case FLOWERS->Block.box(2,0,2,14,10,14);
            case EDGING->stage==1?t(f,13,0,0,16,3,3):t(f,0,0,0,16,3,3);
            case KENNEL->t(f,0,0,2,16,14,15);
            case BOWL->Block.box(4,0,4,12,2,12);
            case POOL_CHAIR->Shapes.or(t(f,1,0,1,15,8,15),t(f,1,7,12,15,16,16));
            case PUMP->t(f,2,0,3,14,11,13);
            case LANTERN->Block.box(4,0,4,12,13,12);
            // 0.4.532: the kitchen's telephone, on the wall by the arch.
            case PHONE->t(f,5,1,13,11,14,16);
            // 0.4.542, the house's aftermath (PellAftermath): the agent's sign on its post in the front lawn.
            case FOR_SALE->Shapes.or(t(f,7,0,8.5,9,16,10.5),t(f,0,8,7.5,16,16,8.5));
            // 0.4.540: open, the chest is hollow: its floor, its front and back, its end, and the lid stood up against the wall.
            case FREEZER_OPEN->(stage&1)==0?Shapes.or(t(f,0,0,1,16,3,16),t(f,0,0,1,16,14,2.5F),t(f,0,0,14.5F,16,16,16),t(f,0,0,1,1.5F,14,16))
                :Shapes.or(t(f,0,0,1,16,3,16),t(f,0,0,1,16,14,2.5F),t(f,0,0,14.5F,16,16,16),t(f,14.5F,0,1,16,14,16));
        };
    }
    /** What stops a body: everything standing on the floor except the flat things, which nobody bumps into. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND).flat()?Shapes.empty():getShape(s,l,p,c);
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)!=Kind.CIRCLE;}
}
