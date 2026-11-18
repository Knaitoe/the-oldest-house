package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYouPortrait;
import io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYouRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 0.4.691, A Place for You: the reader's picture framed on the landing wall, or its nail once it has come down. */
public final class PlaceForYouPortraitRenderer implements BlockEntityRenderer<PlaceForYouPortrait.Portrait> {
    public PlaceForYouPortraitRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(PlaceForYouPortrait.Portrait be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var state=be.getBlockState();
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-state.getValue(HorizontalDirectionalBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);
        HouseReadKitchenClient.framed(be.read(),state.getValue(PlaceForYouPortrait.HUNG),pose,out,light);
        pose.popPose();
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {@SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(PlaceForYouRegistry.PORTRAIT_ENTITY.get(),PlaceForYouPortraitRenderer::new);}}
}
