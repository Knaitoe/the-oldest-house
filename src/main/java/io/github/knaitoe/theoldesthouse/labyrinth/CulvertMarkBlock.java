package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 0.4.560: the culvert's own concrete with something painted on the face it turns to ({@link #FACING}), at the height of a
 * reader sitting in a canoe. Nobody's name and no words: arrows the way on, a count, hands where hands push off, the house
 * under its waterline, a helmet, a half door, a spiral, bubbles. Drawn by tools/draw_lake_house_exterior_art.py.
 */
public final class CulvertMarkBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CulvertMarkBlock> CODEC=simpleCodec(CulvertMarkBlock::new);
    /** What is painted. The house takes two blocks, its left half and its right as a reader faces the wall. */
    public enum Mark implements StringRepresentable {
        ARROW_LEFT,ARROW_RIGHT,TALLY,HAND,HOUSE_LEFT,HOUSE_RIGHT,HELMET,HALF_DOOR,SPIRAL,BUBBLES;
        @Override public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Mark> MARK=EnumProperty.create("mark",Mark.class);
    public CulvertMarkBlock(Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(MARK,Mark.HAND));
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,MARK);}
    public static BlockState of(Mark mark,Direction facing){return LakeHouseRegistry.CULVERT_MARK.get().defaultBlockState().setValue(MARK,mark).setValue(FACING,facing);}
}
