package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeCongregantEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LakeCongregantRenderer extends MobRenderer<LakeCongregantEntity, LakeCongregantModel> {
    public LakeCongregantRenderer(EntityRendererProvider.Context context) { super(context, new LakeCongregantModel(context.bakeLayer(ModelLayers.PLAYER)), .3F); }
    @Override protected void setupRotations(LakeCongregantEntity e,com.mojang.blaze3d.vertex.PoseStack pose,float age,float yaw,float partial,float scale){
        super.setupRotations(e,pose,age,yaw,partial,scale);
        if(e.lying()){pose.translate(0,.22,0);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));}
    }
    @Override public ResourceLocation getTextureLocation(LakeCongregantEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/" + (entity.preacher() ? "lake_preacher" : entity.memoryBoy()?"lake_boy": "lake_congregant"+(entity.preservedEra()>0?"_"+entity.preservedEra():"")) + ".png");
    }
}
