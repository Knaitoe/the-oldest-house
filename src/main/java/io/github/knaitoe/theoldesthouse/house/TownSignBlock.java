package io.github.knaitoe.theoldesthouse.house;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
/** A broad, readable lake-town board on its own grounded post. */
public final class TownSignBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TownSignBlock> CODEC=simpleCodec(TownSignBlock::new);
    public TownSignBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
}
