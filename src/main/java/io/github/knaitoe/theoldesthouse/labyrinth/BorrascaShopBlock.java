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
 * <li>OPEN, a lit sign in a window.</li>
 * <li>0.4.430, the owner: "i hope you're still working on detailing the architecture". The station's own stock: OIL, steel shelving of
 * motor oil and antifreeze; BAIT, the bait freezer with cups on its lid; SNACKS, a wire rack of chips; CALENDAR, the feed store's
 * calendar; BAIT_SIGN, a painted board; TIRES, a stack of used tires. GROCERIES, the general store's shelf of tins, boxes and flour;
 * CROCKERY, the diner's shelf of plates, mugs and bowls. They replace the shelves that read as bookcases.</li>
 * <li>0.4.431: BIKE, a child's bike: Kendra's on its kickstand (stage 0), Patrick's dropped on its side (stage 1). It runs past the
 * block at both ends and nothing bumps into it.</li>
 * </ul>
 */
public final class BorrascaShopBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BorrascaShopBlock> CODEC=simpleCodec(BorrascaShopBlock::new);
    public enum Kind implements StringRepresentable {
        PIE_CASE,URN,MENU,PEGBOARD,PUMP,PUMP_TOP,PRICE,PAYPHONE,COOLER,CANDY,CASE,HELMET,MAP,PHOTOS,CLOCK,SCALE,JAR,CARDS,EXAM,CHART,FOUNTAIN,MEDS,RACK,OPEN,
        OIL,BAIT,SNACKS,CALENDAR,BAIT_SIGN,TIRES,GROCERIES,CROCKERY,BIKE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Flat on a wall, behind the model's south face. */
        public boolean wall(){return switch(this){case MENU,PEGBOARD,PRICE,MAP,PHOTOS,CLOCK,CARDS,CHART,OPEN,CALENDAR,BAIT_SIGN->true;default->false;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public BorrascaShopBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.MENU).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return BorrascaRegistry.SHOP.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(1,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BorrascaRegistry.SHOP.get())&&s.getValue(KIND)==kind;}
    static int light(BlockState s){return s.getValue(KIND)==Kind.OPEN?7:s.getValue(KIND)==Kind.PUMP_TOP?5:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case MENU,PEGBOARD,PRICE,MAP,PHOTOS,CARDS,CHART->MarshBlock.turned(f,0,0,14,16,16,16);
            case CLOCK->MarshBlock.turned(f,4,4,14,12,12,16);
            case OPEN->MarshBlock.turned(f,2,4,14,14,11,16);
            case PAYPHONE->MarshBlock.turned(f,3,1,10,13,16,16);
            case PIE_CASE->Block.box(1,0,2,15,9,14);
            case URN->Block.box(4,0,4,12,13,12);
            case PUMP->MarshBlock.turned(f,2,0,4,14,16,13);
            case PUMP_TOP->MarshBlock.turned(f,2,0,4,14,11,13);
            case COOLER->MarshBlock.turned(f,1,0,2,15,13,15);
            case CANDY,RACK->Block.box(2,0,3,14,15,13);
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
        };
    }
    /** What a body walks into: the counters' pieces, the pumps, the cooler, the cases and the table. Wall pieces are reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case PUMP,PUMP_TOP,COOLER,CANDY,RACK,CASE,MEDS,EXAM,SCALE,PIE_CASE,URN,HELMET,JAR,FOUNTAIN,OIL,BAIT,SNACKS,TIRES,GROCERIES,CROCKERY->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
}
