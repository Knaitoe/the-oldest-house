package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A shallow cupboard front; the original barrel behind it remains the actual inventory. */
public final class HallCupboardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HallCupboardBlock> CODEC = simpleCodec(HallCupboardBlock::new);

    public HallCupboardBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> Block.box(0, 0, 0, 4, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 4);
            case WEST -> Block.box(12, 0, 0, 16, 16, 16);
            default -> Block.box(0, 0, 12, 16, 16, 16);
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos storage = pos.relative(state.getValue(FACING).getOpposite());
        if (!level.getBlockState(storage).is(Blocks.BARREL)) return InteractionResult.PASS;
        if (!level.isClientSide && level.getBlockEntity(storage) instanceof BarrelBlockEntity barrel) {
            player.openMenu(barrel);
            player.awardStat(Stats.OPEN_BARREL);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
