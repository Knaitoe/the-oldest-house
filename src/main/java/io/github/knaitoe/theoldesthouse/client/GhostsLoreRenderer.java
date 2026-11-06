package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GhostsSetBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** A camera image. There is no corresponding block, entity, collision or interactable object in the room. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsLoreRenderer {
    private GhostsLoreRenderer(){}
    public static void image(PoseStack pose,MultiBufferSource buffers,int light){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(GhostsSetBlock.Kind.BASEMENT_EFFIGY,Direction.SOUTH),pose,buffers,light,OverlayTexture.NO_OVERLAY);}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        var at=GhostsTakeClient.effigy();var mc=Minecraft.getInstance();if(at==null||mc.level==null)return;
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();pose.pushPose();pose.translate(at.getX()-camera.x,at.getY()-camera.y,at.getZ()-camera.z);
        var buffers=mc.renderBuffers().bufferSource();int light=LevelRenderer.getLightColor(mc.level,at);image(pose,buffers,net.minecraft.client.renderer.LightTexture.pack(Math.max(7,net.minecraft.client.renderer.LightTexture.block(light)),net.minecraft.client.renderer.LightTexture.sky(light)));buffers.endBatch();pose.popPose();
    }
}
