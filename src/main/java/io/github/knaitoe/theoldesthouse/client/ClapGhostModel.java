package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostEntity;
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

/** Regenerate exact geometry/UVs with tools/generate_clap_assets.py. */
public final class ClapGhostModel extends HierarchicalModel<ClapGhostEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "clap_ghost_girl"), "main");
    private final ModelPart root;
    public ClapGhostModel(ModelPart baked) { root = baked.getChild("root"); }
    @Override public ModelPart root() { return root; }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p_root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0.0F, -2.5F, 7.0F, 10.0F, 5.0F), PartPose.offset(0.0F, -15.0F, 0.0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, -16.0F, 0.0F));
        PartDefinition p_hair = p_head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(50, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 2.0F, 7.0F).texOffs(79, 0).addBox(-3.5F, -5.0F, 2.5F, 7.0F, 6.0F, 1.0F).texOffs(96, 0).addBox(-3.5F, -5.0F, -2.5F, 1.0F, 5.0F, 5.0F).texOffs(109, 0).addBox(2.5F, -5.0F, -2.5F, 1.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition p_left_arm = p_root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F).texOffs(9, 16).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(4.0F, -15.0F, 0.0F));
        PartDefinition p_right_arm = p_root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(18, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F).texOffs(27, 16).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-4.0F, -15.0F, 0.0F));
        PartDefinition p_left_leg = p_root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(45, 16).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(1.6F, -6.0F, 0.0F));
        PartDefinition p_right_leg = p_root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(65, 16).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(-1.6F, -6.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
    @Override public void setupAnim(ClapGhostEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.xScale = root.yScale = root.zScale = 0.80F;
        root.getChild("head").yRot = yaw * Mth.DEG_TO_RAD;
        root.getChild("left_leg").xRot = Mth.sin(age * 0.42F) * 0.18F;
        root.getChild("right_leg").xRot = -Mth.sin(age * 0.42F) * 0.18F;
        root.getChild("left_arm").zRot = -0.045F;
        root.getChild("right_arm").zRot = 0.045F;
    }
}
