package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LakeCongregantEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public final class LakeCongregantModel extends HumanoidModel<LakeCongregantEntity> {
    public LakeCongregantModel(ModelPart root) { super(root); }
    @Override public void setupAnim(LakeCongregantEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        super.setupAnim(entity, walk, 0, age, 0, 0);
        leftArm.xRot = rightArm.xRot = entity.preacher() ? -.25F : -.05F;
        if (entity.seated()) { leftLeg.xRot = rightLeg.xRot = -1.45F; leftLeg.yRot = .15F; rightLeg.yRot = -.15F; }
    }
}
