package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Bud's note (Artifacts): in their own world, ice under its reader grips like ordinary ground. Runs on both sides. */
@Mixin(LivingEntity.class)
public abstract class ArtifactGripMixin {
    @Redirect(method="travel",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float grip(BlockState state,LevelReader level,BlockPos pos,Entity entity){return Artifacts.friction((LivingEntity)(Object)this,state.getFriction(level,pos,entity));}
}
