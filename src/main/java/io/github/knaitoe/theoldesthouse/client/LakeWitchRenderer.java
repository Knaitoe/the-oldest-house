package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LakeWitchRenderer extends MobRenderer<LakeWitchEntity, LakeWitchModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/lake_witch.png");
    public LakeWitchRenderer(EntityRendererProvider.Context context) { super(context, new LakeWitchModel(context.bakeLayer(LakeWitchModel.LAYER)), .4F); }
    @Override public ResourceLocation getTextureLocation(LakeWitchEntity entity) { return TEXTURE; }
}
