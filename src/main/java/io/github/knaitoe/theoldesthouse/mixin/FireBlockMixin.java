package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The impossible hallway does not burn, and fire does not keep inside it. */
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
    private void theOldestHouse$hallwayDoesNotBurn(Level level, BlockPos pos, int chance, RandomSource random, int age, Direction face,
            CallbackInfo ci) {
        if (HouseImpossibleHallway.isShielded(level, pos)) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void theOldestHouse$fireGoesOutInTheHallway(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (HouseImpossibleHallway.isShielded(level, pos)) {
            level.removeBlock(pos, false);
            ci.cancel();
        }
    }
}
