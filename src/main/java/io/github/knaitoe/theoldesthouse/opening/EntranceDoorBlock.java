package io.github.knaitoe.theoldesthouse.opening;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The door that appears in a wall of the player's base.
 *
 * It never opens as a vanilla door: its owner steps through it into the
 * House, anyone else hears a locked rattle. It cannot be broken in survival,
 * moved by pistons, opened by redstone or wind charges, or knocked out by
 * removing the floor beneath it. The iron block-set type keeps villagers
 * and zombies from treating it as a wooden door.
 */
public final class EntranceDoorBlock extends DoorBlock {
    public static final MapCodec<EntranceDoorBlock> CODEC = simpleCodec(EntranceDoorBlock::new);

    public EntranceDoorBlock(BlockBehaviour.Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_DOOR)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    public static BlockPos lowerHalf(BlockPos pos, BlockState state) {
        return state.hasProperty(HALF) && state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && OpeningSequence.useEntranceDoor(serverPlayer, lowerHalf(pos, state))) {
            return InteractionResult.CONSUME;
        }
        rattle(level, pos);
        return InteractionResult.CONSUME;
    }

    public static void rattle(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.9F, 0.8F + level.getRandom().nextFloat() * 0.1F);
    }

    /** Sounds like the wooden door it looks like, whatever its block-set type. */
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.WOOD;
    }

    /** Redstone never opens it. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
    }

    /**
     * The lower half does not need a floor: digging out the block beneath
     * must not destroy it. The upper half still needs its lower half.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? super.canSurvive(state, level, pos) : true;
    }
}
