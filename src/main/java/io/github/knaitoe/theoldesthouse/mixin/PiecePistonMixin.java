package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.StoryPieces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0.4.761: a story piece set down outside the House is remembered where it stands ({@code PlacedPieces}), so a piston may neither push
 * nor pull it. Moved, it would forget its reader and become unbreakable; destroyed, it would give nothing back.
 */
@Mixin(PistonBaseBlock.class)
public abstract class PiecePistonMixin {
    @Inject(method="isPushable",at=@At("HEAD"),cancellable=true)
    private static void theOldestHouse$piece(BlockState state,Level level,BlockPos pos,Direction direction,boolean destroy,Direction facing,CallbackInfoReturnable<Boolean> cir){
        if(StoryPieces.isPiece(state.getBlock()))cir.setReturnValue(false);
    }
}
