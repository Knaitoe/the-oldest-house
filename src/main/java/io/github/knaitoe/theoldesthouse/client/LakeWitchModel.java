package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Full authored mesh: hunched shoulders, lake-soaked hair, torn skirt, elbows, knees and wrists. */
public final class LakeWitchModel extends HierarchicalModel<LakeWitchEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "lake_witch"), "main");
    private final ModelPart root, body, head, leftArm, rightArm, leftLeg, rightLeg;
    public LakeWitchModel(ModelPart baked) {
        root = baked.getChild("root"); body = root.getChild("body"); head = body.getChild("head");
        leftArm = body.getChild("left_arm"); rightArm = body.getChild("right_arm");
        leftLeg = root.getChild("left_leg"); rightLeg = root.getChild("right_leg");
    }
    @Override public ModelPart root() { return root; }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 48).addBox(-4, -11, -2.5F, 8, 12, 5)
                .texOffs(0, 80).addBox(-5, 0, -3.5F, 10, 8, 7), PartPose.offset(0, -13, 0));
        for (int i = 0; i < 4; i++) body.addOrReplaceChild("hem_" + i, CubeListBuilder.create().texOffs(0, 80)
                .addBox(-1.1F, 0, -3.2F, 2.2F, 3 + i % 2, 6.4F), PartPose.offset(-3.5F + i * 2.3F, 7, 0));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -8, -3, 6, 8, 6), PartPose.offset(0, -11, -.3F));
        head.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(96, 0).addBox(-2.5F, -4.8F, -3.12F, 1.6F, .65F, .2F)
                .addBox(.9F, -4.8F, -3.12F, 1.6F, .65F, .2F), PartPose.ZERO);
        head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(96, 0).addBox(-1.6F, -2.1F, -3.15F, 3.2F, .8F, .2F), PartPose.ZERO);
        head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(64, 0).addBox(-3.5F, -8.5F, -3.4F, 7, 3, 7)
                .texOffs(64, 32).addBox(-3.5F, -6, 2.8F, 7, 14, 1.6F)
                .addBox(-3.6F, -6, -2.8F, 1.4F, 12, 6).addBox(2.2F, -6, -2.8F, 1.4F, 13, 6), PartPose.ZERO);
        for (int side : new int[]{-1, 1}) {
            PartDefinition arm = body.addOrReplaceChild(side < 0 ? "right_arm" : "left_arm", CubeListBuilder.create().texOffs(32, 0)
                    .addBox(-1.4F, 0, -1.4F, 2.8F, 11, 2.8F), PartPose.offset(side * 5.2F, -10, 0));
            PartDefinition forearm = arm.addOrReplaceChild("forearm", CubeListBuilder.create().texOffs(32, 0)
                    .addBox(-1.1F, 0, -1.1F, 2.2F, 10, 2.2F), PartPose.offset(0, 10, 0));
            forearm.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(32, 0).addBox(-1.4F, 0, -1, 2.8F, 4, 2)
                    .texOffs(96, 0).addBox(-1.3F, 3, -1.25F, .7F, 1.5F, .8F).addBox(-.35F, 3, -1.25F, .7F, 1.8F, .8F)
                    .addBox(.6F, 3, -1.25F, .7F, 1.4F, .8F), PartPose.offset(0, 9, 0));
            PartDefinition leg = root.addOrReplaceChild(side < 0 ? "right_leg" : "left_leg", CubeListBuilder.create().texOffs(32, 0)
                    .addBox(-1.5F, 0, -1.5F, 3, 7, 3), PartPose.offset(side * 2.2F, -13, .5F));
            PartDefinition shin = leg.addOrReplaceChild("shin", CubeListBuilder.create().texOffs(32, 0).addBox(-1.2F, 0, -1.2F, 2.4F, 7, 2.4F), PartPose.offset(0, 6, 0));
            shin.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0, -3.2F, 3, 1.5F, 4.4F), PartPose.offset(0, 6, 0));
        }
        return LayerDefinition.create(mesh, 128, 128);
    }
    @Override public void setupAnim(LakeWitchEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        body.xRot = .12F + Mth.sin(age * .055F) * .02F;
        head.xRot = -.1F + pitch * Mth.DEG_TO_RAD * .35F; head.yRot = yaw * Mth.DEG_TO_RAD * .6F;
        leftLeg.xRot = Mth.cos(walk * .65F) * amount * .65F;
        rightLeg.xRot = Mth.cos(walk * .65F + Mth.PI) * amount * .65F;
        leftLeg.getChild("shin").xRot = Math.max(0, -leftLeg.xRot) * .7F;
        rightLeg.getChild("shin").xRot = Math.max(0, -rightLeg.xRot) * .7F;
        leftArm.xRot = -.1F + rightLeg.xRot * .35F; rightArm.xRot = -.1F + leftLeg.xRot * .35F;
        leftArm.zRot = -.09F; rightArm.zRot = .09F;
        leftArm.getChild("forearm").xRot = -.18F; rightArm.getChild("forearm").xRot = -.22F;
        head.getChild("hair").zRot = Mth.sin(age * .07F) * .015F;
        if (entity.striking()) {
            body.xRot = .32F; head.xRot = -.3F;
            leftArm.xRot = -1.75F; rightArm.xRot = -1.55F;
            leftArm.getChild("forearm").xRot = -.8F; rightArm.getChild("forearm").xRot = -.65F;
            leftArm.getChild("forearm").getChild("hand").xRot = -.45F;
        }
    }
}
