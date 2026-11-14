package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TrialMirrorBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.TrialRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.389, the door that follows: the trial room's tall mirrors are true plane mirrors ({@link HouseReadReflection}), the same
 * glass the quiet rooms and the Winchester hall stand use, so a reader can watch the doorway behind them in one.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class TrialMirrorRenderer implements BlockEntityRenderer<TrialMirrorBlock.Glass> {
    public TrialMirrorRenderer(BlockEntityRendererProvider.Context c){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TrialRegistry.MIRROR_ENTITY.get(),TrialMirrorRenderer::new);}
    /** 0.4.592: the glass is two blocks high and hangs from the lower: it is drawn while any of it is in view, not only that block. */
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(TrialMirrorBlock.Glass be){var s=be.getBlockState();
        return s.is(TrialRegistry.MIRROR.get())&&!s.getValue(TrialMirrorBlock.UPPER)?new HouseReadReflection.Glass(be.getBlockPos(),s.getValue(TrialMirrorBlock.FACING)).box():new net.minecraft.world.phys.AABB(be.getBlockPos());}
    @Override public void render(TrialMirrorBlock.Glass be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var s=be.getBlockState();if(!s.is(TrialRegistry.MIRROR.get())||s.getValue(TrialMirrorBlock.UPPER))return;
        var glass=new HouseReadReflection.Glass(be.getBlockPos(),s.getValue(TrialMirrorBlock.FACING));HouseReadReflection.want(glass);
        if(HouseReadReflection.showing(glass.at()))HouseReadReflection.drawGlass(glass,pose,out);
    }
}
