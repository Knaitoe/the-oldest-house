package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ClapGhostRenderer extends MobRenderer<ClapGhostEntity, ClapGhostModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheOldestHouse.MOD_ID, "textures/entity/clap_ghost_girl.png");
    public ClapGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new ClapGhostModel(context.bakeLayer(ClapGhostModel.LAYER)), 0);
    }
    @Override protected int getBlockLightLevel(ClapGhostEntity entity,net.minecraft.core.BlockPos pos){return 12;}
    @Override public ResourceLocation getTextureLocation(ClapGhostEntity entity) { return TEXTURE; }
    @Override public boolean shouldRender(ClapGhostEntity entity, Frustum frustum, double x, double y, double z) {
        var player = Minecraft.getInstance().player;
        return player != null && entity.viewer().filter(player.getUUID()::equals).isPresent()
                && super.shouldRender(entity, frustum, x, y, z);
    }
}
