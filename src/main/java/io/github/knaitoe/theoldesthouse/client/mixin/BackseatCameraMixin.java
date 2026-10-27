package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.BackseatClient;
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

/** A small rearward cabin offset after the native eye is placed. The body never moves. */
@Mixin(Camera.class)
public abstract class BackseatCameraMixin {
    @Shadow public abstract Vec3 getPosition();
    @Shadow protected abstract void setPosition(Vec3 at);
    @Inject(method="setup",at=@At("TAIL"))
    private void backseat$acceleration(BlockGetter level,Entity entity,boolean detached,boolean reverse,float partial,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(detached||entity!=mc.player||(Object)this!=mc.gameRenderer.getMainCamera())return;
        double pull=BackseatClient.cameraPullback(partial);if(pull>0)setPosition(getPosition().add(0,0,pull));
    }
}
