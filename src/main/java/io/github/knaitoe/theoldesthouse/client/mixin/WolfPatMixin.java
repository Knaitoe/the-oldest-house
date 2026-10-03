package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CompanionAnimation;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WolfModel.class)
public abstract class WolfPatMixin implements CompanionAnimation.PatModel {
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart tail;
    @Override public void oldestHousePat(float age){CompanionAnimation.wolfPose(head,tail,age);}
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/animal/Wolf;FFFFF)V",at=@At("TAIL"))
    private void pat(Wolf wolf,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){CompanionAnimation.wolf(head,tail,wolf,age);}
}
