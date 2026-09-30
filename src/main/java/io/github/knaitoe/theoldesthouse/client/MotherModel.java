package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Authored cuboid geometry. Regenerate with tools/generate_mother_assets.py. */
public final class MotherModel extends HierarchicalModel<MotherEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "mother_of_strays"), "main");
    private final ModelPart root;
    public MotherModel(ModelPart baked) { root = baked.getChild("root"); }
    @Override public ModelPart root() { return root; }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p_root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 11.0F, 5.0F).texOffs(27, 0).addBox(-3.5F, 3.0F, -4.0F, 3.0F, 4.0F, 3.0F).texOffs(40, 0).addBox(0.5F, 3.0F, -4.0F, 3.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -23.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_neck = p_root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -23.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(66, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F).texOffs(91, 0).addBox(-0.5F, -3.0F, -4.0F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -26.0F, -0.1F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_hair = p_head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(96, 0).addBox(-3.5F, -7.0F, -2.6F, 7.0F, 2.0F, 6.0F).texOffs(0, 17).addBox(-3.5F, -5.0F, 2.5F, 7.0F, 6.0F, 1.0F).texOffs(17, 17).addBox(-3.6F, -5.0F, -2.4F, 1.0F, 5.0F, 5.0F).texOffs(30, 17).addBox(2.8F, -5.0F, -2.4F, 1.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(43, 17).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.2F, -0.2F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_shawl = p_root.addOrReplaceChild("shawl", CubeListBuilder.create().texOffs(68, 17).addBox(-5.0F, -0.5F, -3.0F, 10.0F, 5.0F, 7.0F), PartPose.offsetAndRotation(0.0F, -22.5F, 0.2F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_collar = p_root.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(103, 17).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -22.0F, -3.2F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_skirt_upper = p_root.addOrReplaceChild("skirt_upper", CubeListBuilder.create().texOffs(0, 30).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 6.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_skirt_lower = p_root.addOrReplaceChild("skirt_lower", CubeListBuilder.create().texOffs(33, 30).addBox(-6.0F, 0.0F, -4.0F, 12.0F, 7.0F, 8.0F), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_left_leg = p_root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(74, 30).addBox(-1.5F, 6.0F, -2.8F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_right_leg = p_root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(91, 30).addBox(-1.5F, 6.0F, -2.8F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_left_arm = p_root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(108, 30).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offsetAndRotation(4.5F, -22.0F, 0.0F, 0.0F, 0.0F, -0.06F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 46).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.14F, 0.0F, 0.0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(9, 46).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_left_finger_0 = p_left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(20, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 3.0F, 0.7F).texOffs(25, 46).addBox(-0.38F, 2.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(-1.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        PartDefinition p_left_finger_1 = p_left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(30, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 5.0F, 0.7F).texOffs(35, 46).addBox(-0.38F, 4.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(0.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        PartDefinition p_left_finger_2 = p_left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(40, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 4.0F, 0.7F).texOffs(45, 46).addBox(-0.38F, 3.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(1.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        PartDefinition p_right_arm = p_root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(50, 46).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offsetAndRotation(-4.5F, -22.0F, 0.0F, 0.0F, 0.0F, 0.06F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(63, 46).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.14F, 0.0F, 0.0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(72, 46).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_right_finger_0 = p_right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(83, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 3.0F, 0.7F).texOffs(88, 46).addBox(-0.38F, 2.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(-1.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        PartDefinition p_right_finger_1 = p_right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(93, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 5.0F, 0.7F).texOffs(98, 46).addBox(-0.38F, 4.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(0.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        PartDefinition p_right_finger_2 = p_right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(103, 46).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 4.0F, 0.7F).texOffs(108, 46).addBox(-0.38F, 3.5F, -0.5F, 0.76F, 2.0F, 0.6F), PartPose.offsetAndRotation(1.0F, 2.5F, -0.2F, -0.18F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 256);
    }
    @Override public void setupAnim(MotherEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        ModelPart head = root.getChild("head");
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float raw = Mth.clamp(entity.shownCorruption(), 0.0F, 1.0F);
        float t = Mth.clamp((raw - 0.20F) / 0.80F, 0.0F, 1.0F);
        float c = t * t * (3.0F - 2.0F * t);
        float jt = Mth.clamp((raw - 0.74F) / 0.26F, 0.0F, 1.0F);
        float j = jt * jt * (3.0F - 2.0F * jt);
        root.xScale = root.zScale = 0.90F;
        root.yScale = 0.86F;
        root.getChild("body").zScale = 1.0F + c * 0.08F;
        root.getChild("body").xRot = c * 0.08F;
        head.xScale = 1.0F + c * 0.08F;
        head.yScale = 1.0F + c * 0.14F;
        head.xRot += c * 0.13F;
        root.getChild("neck").yScale = 1.0F + c * 0.15F;
        ModelPart jaw = head.getChild("jaw");
        jaw.visible = raw > 0.74F;
        jaw.yScale = 0.18F + j * 0.32F;
        jaw.xRot = j * 0.08F;
        for (String side : new String[]{"left", "right"}) {
            float sign = side.equals("left") ? 1.0F : -1.0F;
            ModelPart arm = root.getChild(side + "_arm");
            arm.xRot = Mth.cos(walk * 0.38F + (sign > 0 ? 0 : Mth.PI)) * amount * 0.25F;
            arm.zRot = -sign * (0.06F + c * 0.09F);
            ModelPart forearm = arm.getChild(side + "_forearm");
            forearm.xRot = entity.isCarrying() ? -1.90F : -0.14F - c * 0.16F;
            ModelPart hand = forearm.getChild(side + "_hand");
            hand.yScale = 1.0F + c * 0.08F;
            for (int i = 0; i < 3; i++) {
                ModelPart finger = hand.getChild(side + "_finger_" + i);
                finger.yScale = 1.0F + c * 0.14F;
                finger.xRot = -0.18F - c * 0.30F + Mth.sin(age * 0.035F + i) * 0.045F;
            }
        }
        root.getChild("shawl").zScale = 1.0F + c * 0.05F;
        root.getChild("skirt_lower").xRot = Mth.sin(walk * 0.25F) * amount * 0.025F;
        root.getChild("body").y += Mth.sin(age * 0.028F) * (0.05F + raw * 0.035F);
    }
}
