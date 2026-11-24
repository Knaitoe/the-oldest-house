package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.StoryPieces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): the House's own blocks cannot be broken, but a story piece a reader set down outside the
 * House breaks like an ordinary block. The server knows which were set down; the client allows any outside the House, and the server
 * puts back what it refuses.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class PieceBreakMixin {
    @Shadow @Final private float destroySpeed;
    @Shadow public abstract Block getBlock();
    @Inject(method="getDestroySpeed",at=@At("HEAD"),cancellable=true)
    private void theOldestHouse$piece(BlockGetter level,BlockPos pos,CallbackInfoReturnable<Float> cir){
        if(destroySpeed<0&&StoryPieces.breakable(getBlock(),level,pos))cir.setReturnValue(1.5F);
    }
}
