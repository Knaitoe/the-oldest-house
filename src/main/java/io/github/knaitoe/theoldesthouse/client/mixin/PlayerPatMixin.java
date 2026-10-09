package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CompanionAnimation;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class PlayerPatMixin implements CompanionAnimation.PatModel {
    @Override public void oldestHousePat(float age){CompanionAnimation.playerPose((PlayerModel<?>)(Object)this,net.minecraft.world.entity.HumanoidArm.RIGHT,age);}
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void pat(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){CompanionAnimation.player((PlayerModel<?>)(Object)this,entity,age);io.github.knaitoe.theoldesthouse.client.BodyLossClient.pose((PlayerModel<?>)(Object)this,entity);}
}
