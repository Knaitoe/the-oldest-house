package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 0.4.523, the line (docs/PELL_HOUSE_LINE_0_4_523.md): the paint round the whole of June's lot, a cell of it in each block of the
 * ground it lies on. The paint is in the ground's top: this block is the ground itself (SURFACE says which) with the line painted on
 * it, so the side gate, the mat, the pool fence and the kennel stand on the line as they would on paint, and the line goes under
 * them. Drawn by tools/draw_pell_line.py.
 * <ul>
 * <li>SHAPE: STRAIGHT runs north to south when FACING is north or south and east to west when it is east or west; CORNER joins
 * FACING and the side clockwise of it ({@link #ends}); THIN is straight and barely a finger wide (behind the shed).</li>
 * <li>SURFACE: what the paint lies on, and so how it has taken: soaked into CONCRETE; on the drive, over the TYRE marks that have
 * been painted over; beaded on PAVERs; sitting on the LAWN's blades, which are dead under it; pooled in MULCH, laid twice; flaking
 * off DECK boards.</li>
 * <li>STAGE: as on the first visit, the second (painted again), the last night (wet in places), the last night with a footprint
 * smeared through it, scrubbed half away, scrubbed out (a gap); and from 0.4.567, scrubbed a quarter away and three quarters away, so
 * that the paint is seen going as the brush works at it.</li>
 * </ul>
 */
public final class PellLine extends HorizontalDirectionalBlock {
    public static final MapCodec<PellLine> CODEC=simpleCodec(PellLine::new);
    public enum Shape implements StringRepresentable {
        STRAIGHT,CORNER,THIN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public enum Surface implements StringRepresentable {
        CONCRETE,TYRE,PAVER,LAWN,MULCH,DECK;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The house's material this surface is, under the paint. */
        public String material(){return switch(this){case TYRE->"concrete";default->getSerializedName();};}
    }
    public static final EnumProperty<Shape> SHAPE=EnumProperty.create("shape",Shape.class);
    public static final EnumProperty<Surface> SURFACE=EnumProperty.create("surface",Surface.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    /** The stages a cell can be in. */
    public static final int FIRST=0,SECOND=1,LAST=2,FOOTPRINT=3,HALF_SCRUBBED=4,GAP=5,QUARTER_SCRUBBED=6,MOSTLY_SCRUBBED=7;
    /** 0.4.567: how far a cell is scrubbed, in quarters: none for the paint as it was laid, four for scrubbed out. */
    public static int scrubbed(int stage){return switch(stage){case QUARTER_SCRUBBED->1;case HALF_SCRUBBED->2;case MOSTLY_SCRUBBED->3;case GAP->4;default->0;};}
    /** 0.4.567: the stage of a cell a quarter further scrubbed than this. */
    public static int scrubbedOn(int stage){return switch(scrubbed(stage)){case 0->QUARTER_SCRUBBED;case 1->HALF_SCRUBBED;case 2->MOSTLY_SCRUBBED;default->GAP;};}
    public PellLine(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SHAPE,Shape.STRAIGHT)
        .setValue(SURFACE,Surface.LAWN).setValue(STAGE,FIRST));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,SHAPE,SURFACE,STAGE);}
    /** A cell of the line joining the sides a and b (opposite sides make it straight), on this surface, at this stage. */
    public static BlockState of(Direction a,Direction b,Surface surface,int stage){return of(a,b,surface,stage,false);}
    /** The same, barely a finger wide if thin (a straight cell only). */
    public static BlockState of(Direction a,Direction b,Surface surface,int stage,boolean thin){
        var s=PellRegistry.LINE.get().defaultBlockState().setValue(SURFACE,surface).setValue(STAGE,Math.max(FIRST,Math.min(MOSTLY_SCRUBBED,stage)));
        if(a.getOpposite()==b)return s.setValue(SHAPE,thin?Shape.THIN:Shape.STRAIGHT).setValue(FACING,a.getAxis()==Direction.Axis.Z?Direction.NORTH:Direction.EAST);
        // A corner faces the side whose clockwise neighbour is the other.
        return s.setValue(SHAPE,Shape.CORNER).setValue(FACING,a.getClockWise()==b?a:b);
    }
    public static boolean is(BlockState s){return s.is(PellRegistry.LINE.get());}
    /** The two sides of its cell this piece of line runs out through. */
    public static List<Direction> ends(BlockState s){
        Direction f=s.getValue(FACING);
        return s.getValue(SHAPE)==Shape.CORNER?List.of(f,f.getClockWise()):List.of(f,f.getOpposite());
    }
    /** Whether this cell still holds the line (anything short of scrubbed out). */
    public static boolean unbroken(BlockState s){return is(s)&&s.getValue(STAGE)!=GAP;}
    /** The ground underfoot sounds like what it is: grass and mulch, boards, or stone. */
    @Override public SoundType getSoundType(BlockState s,LevelReader l,BlockPos at,@Nullable Entity e){
        return switch(s.getValue(SURFACE)){case LAWN,MULCH->SoundType.GRASS;case DECK->SoundType.WOOD;default->SoundType.STONE;};
    }
}
