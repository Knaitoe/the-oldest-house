package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LakeWitchRenderer extends MobRenderer<LakeWitchEntity, LakeWitchModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/lake_witch.png");
    public LakeWitchRenderer(EntityRendererProvider.Context context) { super(context, new LakeWitchModel(context.bakeLayer(LakeWitchModel.LAYER)), .4F); }
    @Override protected void setupRotations(LakeWitchEntity e,com.mojang.blaze3d.vertex.PoseStack pose,float age,float yaw,float partial,float scale){
        super.setupRotations(e,pose,age,yaw,partial,scale);if(e.memory()&&e.memoryPhase()==1){pose.translate(0,.9,0);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));}
    }
    @Override public ResourceLocation getTextureLocation(LakeWitchEntity entity) { return entity.memory() ? ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_witch_memory.png") : TEXTURE; }
}
