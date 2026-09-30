package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MotherRenderer extends MobRenderer<MotherEntity, MotherModel> {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/mother_of_strays.png"),
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/mother_of_strays_1.png"),
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/mother_of_strays_2.png"),
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/mother_of_strays_3.png")
    };

    public MotherRenderer(EntityRendererProvider.Context context) {
        super(context, new MotherModel(context.bakeLayer(MotherModel.LAYER)), 0.35F);
    }

    @Override public ResourceLocation getTextureLocation(MotherEntity entity) {
        return TEXTURES[Math.min(3, (int) (entity.shownCorruption() * 3.99F))];
    }
}
