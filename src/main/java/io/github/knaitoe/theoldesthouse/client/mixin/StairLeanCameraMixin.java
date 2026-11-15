package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.StaircaseLeanView;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 0.4.594: a reader leaning over a rail of the great staircase's well. The view goes out over the rail after the native eye is placed; the body never moves. */
@Mixin(Camera.class)
public abstract class StairLeanCameraMixin {
    @Shadow public abstract Vec3 getPosition();
    @Shadow protected abstract void setPosition(Vec3 at);
    @Inject(method="setup",at=@At("TAIL"))
    private void stairLean$over(BlockGetter level,Entity entity,boolean detached,boolean reverse,float partial,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(detached||entity!=mc.player||(Object)this!=mc.gameRenderer.getMainCamera())return;
        var over=StaircaseLeanView.offset(partial);if(over!=null)setPosition(getPosition().add(over));
    }
}
