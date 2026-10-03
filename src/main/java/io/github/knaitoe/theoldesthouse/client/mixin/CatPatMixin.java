package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CompanionAnimation;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CatModel.class)
public abstract class CatPatMixin extends OcelotModel<Cat> implements CompanionAnimation.PatModel {
    protected CatPatMixin(ModelPart root){super(root);}
    @Override public void oldestHousePat(float age){CompanionAnimation.catPose(head,tail1,tail2,age);}
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/animal/Cat;FFFFF)V",at=@At("TAIL"))
    private void pat(Cat cat,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){CompanionAnimation.cat(head,tail1,tail2,cat,age);}
}
