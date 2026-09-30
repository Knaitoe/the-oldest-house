package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MotherRenderer extends MobRenderer<MotherEntity, MotherModel> {
    private static final ResourceLocation[] TEXTURES = textures();

    private static ResourceLocation[] textures() {
        ResourceLocation[] textures = new ResourceLocation[16];
        for (int stage = 0; stage < textures.length; stage++) {
            textures[stage] = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,
                    "textures/entity/mother_of_strays" + (stage == 0 ? "" : "_" + stage) + ".png");
        }
        return textures;
    }

    public MotherRenderer(EntityRendererProvider.Context context) {
        super(context, new MotherModel(context.bakeLayer(MotherModel.LAYER)), 0.35F);
    }

    @Override public ResourceLocation getTextureLocation(MotherEntity entity) {
        int stage = Math.max(0, Math.min(TEXTURES.length - 1,
                (int) (entity.shownCorruption() * TEXTURES.length)));
        return TEXTURES[stage];
    }
}
