package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.GoatmanClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native sleeping overwrites ComputeCameraAngles; apply the actual bed view after that native branch. */
@Mixin(Camera.class)
public abstract class GoatmanBedCameraMixin {
    @Shadow protected abstract void setRotation(float yaw,float pitch);
    @Inject(method="setup",at=@At("TAIL"))
    private void goatman$centerTable(BlockGetter level,Entity entity,boolean detached,boolean reverse,float partial,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(detached||entity!=mc.player||(Object)this!=mc.gameRenderer.getMainCamera()||!GoatmanClient.watchingFromBed())return;
        var direction=mc.player.getBedOrientation();if(direction!=null)setRotation(direction.toYRot(),0);
    }
}
