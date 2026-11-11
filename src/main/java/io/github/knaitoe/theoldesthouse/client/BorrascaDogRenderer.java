package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

/** 0.4.462: Biscuit is drawn as the game draws a wolf, on his own skin (tools/draw_borrasca_dog.py). */
public final class BorrascaDogRenderer extends WolfRenderer {
    public static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_biscuit.png");
    public BorrascaDogRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(Wolf wolf){return SKIN;}
}
