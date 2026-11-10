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
 * 0.4.422, Borrasca's town (docs/BORRASCA_TOWN_0_4_422.md): the shops' own fittings, drawn facing north by
 * tools/draw_borrasca_town_art.py and turned by their facing. Wall pieces hang on the wall behind them (their back is to the south of
 * the model, so a wall piece facing north hangs on a wall to its south).
 * <ul>
 * <li>The diner: PIE_CASE, a glass case of pies on the counter (stage 1, one pie cut); URN, the coffee urn; MENU, the letter board.</li>
 * <li>The general store's hardware counter: PEGBOARD, rope, flashlights, batteries and pocketknives on hooks (stage 1, a flashlight
 * gone from its hook).</li>
 * <li>The gas station: PUMP and PUMP_TOP, the two halves of a pump; PRICE, the price board; PAYPHONE, the phone on the wall; COOLER, the
 * soda cooler; CANDY, the candy rack (the drugstore has one too).</li>
 * <li>The antiques shop and the historical society behind it: CASE, a glass case of small old things; HELMET, a miner's helmet and lamp
 * on a stand; MAP, the framed map of the old workings; PHOTOS, framed photographs; CLOCK, a wall clock.</li>
 * <li>The doctor's office: SCALE, the beam scale; JAR, the lollipop jar (stage 1, nearly empty); CARDS, the corkboard of birth cards;
 * EXAM, the examination table; CHART, the eye chart.</li>
 * <li>The drugstore: FOUNTAIN, the soda fountain's taps on the counter; MEDS, the pharmacy shelf; RACK, the comic spinner.</li>
 * <li>OPEN, a lit sign in a window. 0.4.444: it hangs at the front of its own block, inside the shop against the glass, facing the
 * street (it used to stand in the window's place).</li>
 * <li>0.4.430, the owner: "i hope you're still working on detailing the architecture". The station's own stock: OIL, steel shelving of
 * motor oil and antifreeze; BAIT, the bait freezer with cups on its lid; SNACKS, a wire rack of chips; CALENDAR, the feed store's
 * calendar; BAIT_SIGN, a painted board; TIRES, a stack of used tires. GROCERIES, the general store's shelf of tins, boxes and flour;
 * CROCKERY, the diner's shelf of plates, mugs and bowls. They replace the shelves that read as bookcases.</li>
 * <li>0.4.431: BIKE, a child's bike: Kendra's on its kickstand (stage 0), Patrick's dropped on its side (stage 1). It runs past the
 * block at both ends and nothing bumps into it.</li>
 * <li>0.4.436: CABINET and CABINET_TOP, the drugstore's arcade cabinets (BorrascaArcade), lower and upper halves; the stage is the
 * game: 0 DEEP SEAM, 1 BIG CAT. The screen in the upper half glows a little.</li>
 * <li>0.4.439: what the machine leaves on the last night (BorrascaFeeding). REMAINS on the pile at the end of the belt, pulp and bone
 * (stage 0) or hair and a felt slipper (stage 1); WRISTBAND, the band from her wrist with its card; SMEAR, drawn on the belt's
 * rubber by a display where it comes out of the slot (stage 0 a streak, stage 1 soaked).</li>
 * <li>0.4.444, the owner: "Give new textures to the different docs." The papers that can be read, each with its own face: DOC_DISPATCH
 * and DOC_LOG on clipboards, DOC_PAYROLL on green-bar paper, DOC_SIGNIN with its pen, DOC_INTAKE with its stamp, DOC_TAGS (three
 * manila tags), DOC_FORMS (a stack), DOC_LEDGER (the night book, open), DOC_LETTER (the pasted note) and DOC_RECEIPT (a pink carbon).
 * A paper's facing is the way the top of its page points, away from whoever reads it. MEDS has a second arrangement (stage 1).</li>
 * </ul>
 */
public final class BorrascaShopBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BorrascaShopBlock> CODEC=simpleCodec(BorrascaShopBlock::new);
    public enum Kind implements StringRepresentable {
        PIE_CASE,URN,MENU,PEGBOARD,PUMP,PUMP_TOP,PRICE,PAYPHONE,COOLER,CANDY,CASE,HELMET,MAP,PHOTOS,CLOCK,SCALE,JAR,CARDS,EXAM,CHART,FOUNTAIN,MEDS,RACK,OPEN,
        OIL,BAIT,SNACKS,CALENDAR,BAIT_SIGN,TIRES,GROCERIES,CROCKERY,BIKE,CABINET,CABINET_TOP,REMAINS,WRISTBAND,SMEAR,
        DOC_DISPATCH,DOC_PAYROLL,DOC_SIGNIN,DOC_INTAKE,DOC_TAGS,DOC_FORMS,DOC_LEDGER,DOC_LETTER,DOC_RECEIPT,DOC_LOG;
        /** A paper lying on a desk. */
        public boolean doc(){return ordinal()>=DOC_DISPATCH.ordinal()&&ordinal()<=DOC_LOG.ordinal();}
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Flat on a wall, behind the model's south face. */
        public boolean wall(){return switch(this){case MENU,PEGBOARD,PRICE,MAP,PHOTOS,CLOCK,CARDS,CHART,CALENDAR,BAIT_SIGN->true;default->false;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public BorrascaShopBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.MENU).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return BorrascaRegistry.SHOP.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(1,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BorrascaRegistry.SHOP.get())&&s.getValue(KIND)==kind;}
    static int light(BlockState s){return s.getValue(KIND)==Kind.OPEN?7:s.getValue(KIND)==Kind.PUMP_TOP||s.getValue(KIND)==Kind.CABINET_TOP?5:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case MENU,PEGBOARD,PRICE,MAP,PHOTOS,CARDS,CHART->MarshBlock.turned(f,0,0,14,16,16,16);
            case CLOCK->MarshBlock.turned(f,4,4,14,12,12,16);
            case OPEN->MarshBlock.turned(f,0,4,0,16,11,2);
            case PAYPHONE->MarshBlock.turned(f,3,1,10,13,16,16);
            case PIE_CASE->Block.box(1,0,2,15,9,14);
            case URN->Block.box(4,0,4,12,13,12);
            case PUMP->MarshBlock.turned(f,2,0,4,14,16,13);
            case PUMP_TOP->MarshBlock.turned(f,2,0,4,14,11,13);
            case COOLER->MarshBlock.turned(f,1,0,2,15,13,15);
            case CANDY->Block.box(2,0,3,14,15,13);
            case RACK->Block.box(3,0,3,13,16,13);
            case DOC_DISPATCH,DOC_PAYROLL,DOC_SIGNIN,DOC_INTAKE,DOC_TAGS,DOC_FORMS,DOC_LEDGER,DOC_LETTER,DOC_RECEIPT,DOC_LOG->Block.box(1,0,1,15,2,15);
            case CASE,MEDS->Shapes.block();
            case HELMET->Block.box(4,0,4,12,9,12);
            case SCALE->Block.box(3,0,3,13,16,13);
            case JAR->Block.box(5,0,5,11,9,11);
            case EXAM->MarshBlock.turned(f,0,0,2,16,10,14);
            case FOUNTAIN->MarshBlock.turned(f,2,0,6,14,10,12);
            case OIL,GROCERIES,CROCKERY->MarshBlock.turned(f,0,0,2,16,16,14);
            case BAIT->MarshBlock.turned(f,1,0,2,15,13,14);
            case SNACKS->MarshBlock.turned(f,1,0,5,15,16,11);
            case CALENDAR->MarshBlock.turned(f,3,1,15,13,15,16);
            case BAIT_SIGN->MarshBlock.turned(f,0,0,14,16,16,16);
            case TIRES->Block.box(1,0,1,15,12,15);
            case BIKE->s.getValue(STAGE)==0?MarshBlock.turned(f,0,0,6,16,14,10):MarshBlock.turned(f,0,0,2,16,6,16);
            case CABINET->MarshBlock.turned(f,1,0,0,15,16,16);
            case CABINET_TOP->MarshBlock.turned(f,1,0,3,15,16,16);
            case REMAINS->Block.box(1,0,1,15,4,15);
            case WRISTBAND->Block.box(2,0,3,14,1,13);
            case SMEAR->Block.box(0,0,0,16,1,16);
        };
    }
    /** What a body walks into: the counters' pieces, the pumps, the cooler, the cases and the table. Wall pieces are reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case PUMP,PUMP_TOP,COOLER,CANDY,RACK,CASE,MEDS,EXAM,SCALE,PIE_CASE,URN,HELMET,JAR,FOUNTAIN,OIL,BAIT,SNACKS,TIRES,GROCERIES,CROCKERY,CABINET,CABINET_TOP->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
}
