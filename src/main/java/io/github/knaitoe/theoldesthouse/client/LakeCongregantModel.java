package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LakeCongregantEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public final class LakeCongregantModel extends HumanoidModel<LakeCongregantEntity> {
    public LakeCongregantModel(ModelPart root) { super(root); }
    @Override public void setupAnim(LakeCongregantEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        super.setupAnim(entity, walk, 0, age, 0, 0);
        leftArm.xRot = rightArm.xRot = entity.preacher() ? -.25F : -.05F;
        float seat=entity.seating(age-entity.tickCount);
        leftLeg.xRot=rightLeg.xRot=-1.45F*seat;leftLeg.yRot=.15F*seat;rightLeg.yRot=-.15F*seat;
        body.xRot=.1F*seat;head.xRot=.12F*seat;
    }
}
