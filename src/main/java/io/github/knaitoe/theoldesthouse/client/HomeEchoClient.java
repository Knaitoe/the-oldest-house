package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HomeEchoPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Read-only native block echoes. They neither move the original furniture nor create a collectible copy. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HomeEchoClient {
    private static HomeEchoPayload state;private static int lease;private static ClientLevel level;
    public static void accept(HomeEchoPayload p){state=p;lease=25;level=Minecraft.getInstance().level;}
    private static boolean active(){var m=Minecraft.getInstance();return lease>0&&state!=null&&m.level==level&&m.player!=null&&m.player.isAlive();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(!active()){state=null;level=null;lease=0;}}
    @SubscribeEvent public static void fogColor(ViewportEvent.ComputeFogColor e){if(active()&&state.hush()>0){float factor=1-state.hush()*.0018F;e.setRed(e.getRed()*factor);e.setGreen(e.getGreen()*factor);e.setBlue(e.getBlue()*factor);}}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){if(active()&&state.hush()>0&&e.getMode()==FogRenderer.FogMode.FOG_TERRAIN){float far=Math.max(18,64-state.hush()*.4F);e.setFarPlaneDistance(Math.min(e.getFarPlaneDistance(),far));e.setCanceled(true);}}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS||!active()||state.chair()<0)return;
        var mc=Minecraft.getInstance();BlockPos at=state.at();if(!mc.level.getBlockState(at).isAir()||e.getCamera().getPosition().distanceToSqr(at.getCenter())>900)return;
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();pose.pushPose();pose.translate(at.getX()-camera.x,at.getY()-camera.y,at.getZ()-camera.z);
        int light=net.minecraft.client.renderer.LevelRenderer.getLightColor(mc.level,at);
        mc.getBlockRenderer().renderSingleBlock(Block.stateById(state.chair()),pose,buffers,light,OverlayTexture.NO_OVERLAY);
        if(state.floor()>=0){pose.translate(0,-.998,0);mc.getBlockRenderer().renderSingleBlock(Block.stateById(state.floor()),pose,buffers,light,OverlayTexture.NO_OVERLAY);}
        pose.popPose();buffers.endBatch();
    }
}
