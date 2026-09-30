package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
/** Four short pieces meeting at a knot, rather than a glowing navigation line. */
public final class TrailLineBlock extends Block {
    public static final MapCodec<TrailLineBlock> CODEC=simpleCodec(TrailLineBlock::new);
    public TrailLineBlock(BlockBehaviour.Properties properties) {
        super(properties);registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.NORTH,false).setValue(BlockStateProperties.SOUTH,false)
                .setValue(BlockStateProperties.EAST,false).setValue(BlockStateProperties.WEST,false));
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,
            net.minecraft.core.BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return Block.box(0,0,0,16,.2,16);
    }
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        builder.add(BlockStateProperties.NORTH,BlockStateProperties.SOUTH,BlockStateProperties.EAST,BlockStateProperties.WEST);
    }
}
