package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.WinchesterClockEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.WinchesterFittingBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.220: the Winchester hall clock's hands, moving round its dial of thirteen hours. The dial is the block model's (the face
 * twelve pixels square on the case's front, its centre six pixels up); the hands are drawn over it by {@link ClockHands}.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WinchesterClockRenderer implements BlockEntityRenderer<WinchesterClockEntity> {
    public WinchesterClockRenderer(BlockEntityRendererProvider.Context c){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(LiteraryRegistry.WINCHESTER_CLOCK.get(),WinchesterClockRenderer::new);}
    @Override public void render(WinchesterClockEntity clock,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var s=clock.getBlockState();if(!s.is(LiteraryRegistry.WINCHESTER_FITTING.get()))return;
        ClockHands.draw(clock,s.getValue(WinchesterFittingBlock.FACING),ClockHands.Time.THIRTEEN,.5f,6f/16,12f/16,pose,out,light);
    }
}
