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
        if(s.getValue(WinchesterFittingBlock.KIND)==WinchesterFittingBlock.Kind.HALL_STAND_TOP){mirror(clock,s,pose,out);return;}
        ClockHands.draw(clock,s.getValue(WinchesterFittingBlock.FACING),ClockHands.Time.THIRTEEN,.5f,6f/16,12f/16,pose,out,light);
    }
    /**
     * 0.4.223 (owner: "this should utilize the mirror system we designed"): the hall stand's glass is a true plane mirror
     * ({@link HouseReadReflection}): the hall and whoever stands in front of it, the reader as the House's version of them. The
     * glass in the model faces north at 14.5 px with its frame's sides 1.5 px in; in the mirror's own frame (facing south) that is
     * 1.5 px off the back, 1.5 to 14.5 px across and up to the frame's head at 14 px.
     */
    static HouseReadReflection.Glass glass(WinchesterClockEntity stand,net.minecraft.world.level.block.state.BlockState s){
        return new HouseReadReflection.Glass(stand.getBlockPos(),s.getValue(WinchesterFittingBlock.FACING),1.5f/16,14.5f/16,.5f/16,14f/16,1.6f/16,null);
    }
    private static void mirror(WinchesterClockEntity stand,net.minecraft.world.level.block.state.BlockState s,PoseStack pose,MultiBufferSource out){
        var glass=glass(stand,s);HouseReadReflection.want(glass);
        if(HouseReadReflection.showing(glass.at()))HouseReadReflection.drawGlass(glass,pose,out);
    }
}
