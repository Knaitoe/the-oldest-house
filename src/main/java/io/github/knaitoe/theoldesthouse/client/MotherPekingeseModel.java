package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherPekingese;
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
public final class MotherPekingeseModel extends HierarchicalModel<MotherPekingese> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "mother_pekingese"), "main");
    private final ModelPart root;
    public MotherPekingeseModel(ModelPart baked) { root = baked.getChild("root"); }
    @Override public ModelPart root() { return root; }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p_root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 10.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_mane = p_root.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(37, 0).addBox(-4.5F, -3.5F, -2.5F, 9.0F, 7.0F, 5.0F), PartPose.offsetAndRotation(0.0F, -5.0F, -3.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(66, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 5.0F, 5.0F).texOffs(89, 0).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 1.0F).texOffs(100, 0).addBox(-1.0F, -0.4F, -4.5F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, -5.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(107, 0).addBox(0.0F, -1.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(3.0F, -0.8F, 0.3F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -1.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(-3.0F, -0.8F, 0.3F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_bandage = p_head.addOrReplaceChild("bandage", CubeListBuilder.create().texOffs(11, 17).addBox(-3.08F, -3.1F, -3.08F, 6.16F, 1.2F, 5.16F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_tail = p_root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(38, 17).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -7.0F, 4.0F, -0.7F, 0.0F, 0.0F));
        PartDefinition p_left_front = p_root.addOrReplaceChild("left_front", CubeListBuilder.create().texOffs(53, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(2.8F, -3.0F, -3.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_right_front = p_root.addOrReplaceChild("right_front", CubeListBuilder.create().texOffs(62, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-2.8F, -3.0F, -3.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_left_back = p_root.addOrReplaceChild("left_back", CubeListBuilder.create().texOffs(71, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(2.8F, -3.0F, 3.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_right_back = p_root.addOrReplaceChild("right_back", CubeListBuilder.create().texOffs(80, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-2.8F, -3.0F, 3.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 256);
    }
    @Override public void setupAnim(MotherPekingese entity, float walk, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        ModelPart head = root.getChild("head");
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        root.getChild("left_front").xRot = Mth.cos(walk * 0.8F) * amount;
        root.getChild("right_back").xRot = Mth.cos(walk * 0.8F) * amount;
        root.getChild("right_front").xRot = Mth.cos(walk * 0.8F + Mth.PI) * amount;
        root.getChild("left_back").xRot = Mth.cos(walk * 0.8F + Mth.PI) * amount;
        root.getChild("tail").yRot = Mth.sin(age * 0.12F) * 0.16F;
    }
}
