package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeCongregantEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LakeCongregantRenderer extends MobRenderer<LakeCongregantEntity, LakeCongregantModel> {
    public LakeCongregantRenderer(EntityRendererProvider.Context context) { super(context, new LakeCongregantModel(context.bakeLayer(ModelLayers.PLAYER)), .3F); }
    @Override public ResourceLocation getTextureLocation(LakeCongregantEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/" + (entity.preacher() ? "lake_preacher" : entity.memoryBoy()?"lake_boy": "lake_congregant"+(entity.preservedEra()>0?"_"+entity.preservedEra():"")) + ".png");
    }
}
