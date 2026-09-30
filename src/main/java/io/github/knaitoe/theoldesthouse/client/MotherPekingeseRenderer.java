package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherPekingese;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MotherPekingeseRenderer extends MobRenderer<MotherPekingese, MotherPekingeseModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheOldestHouse.MOD_ID, "textures/entity/mother_pekingese.png");
    public MotherPekingeseRenderer(EntityRendererProvider.Context context) {
        super(context, new MotherPekingeseModel(context.bakeLayer(MotherPekingeseModel.LAYER)), 0.25F);
    }
    @Override public ResourceLocation getTextureLocation(MotherPekingese entity) { return TEXTURE; }
}
