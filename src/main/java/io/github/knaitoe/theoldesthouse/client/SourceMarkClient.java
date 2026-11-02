package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.SourceMarkPayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

/**
 * 0.4.273: the paper a vignette is waiting on this reader to read (labyrinth/SourceMark), drawn so it can be found: a faint warm
 * outline round its own shape that breathes slowly, and now and then a mote rising off it, like dust in lamplight. The outline is
 * drawn with ordinary depth, so walls hide it; it is drawn only near (MARK_RANGE) and only until the server says the paper is read.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SourceMarkClient {
    private SourceMarkClient(){}
    /** How near the reader must be for the mark to be drawn. */
    public static final double MARK_RANGE=28;
    private static @Nullable BlockPos at;private static @Nullable ClientLevel world;private static int age;
    private static final DustParticleOptions MOTE=new DustParticleOptions(new Vector3f(1F,.93F,.74F),.6F);

    public static void accept(SourceMarkPayload payload){var mc=Minecraft.getInstance();if(payload.on()){at=payload.at();world=mc.level;}else{at=null;world=null;}}
    public static @Nullable BlockPos marked(){var mc=Minecraft.getInstance();return at!=null&&mc.level==world?at:null;}
    /** The breath, 0..1, over about three seconds. */
    static float breath(float t){return .5F+.5F*Mth.sin(t*(float)(Math.PI*2/60F));}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var mark=marked();if(mark==null||mc.player==null||mc.isPaused())return;age++;
        if(mc.player.distanceToSqr(mark.getCenter())>MARK_RANGE*MARK_RANGE||age%14!=0)return;
        var shape=mc.level.getBlockState(mark).getShape(mc.level,mark,CollisionContext.of(mc.player));
        double top=shape.isEmpty()?.5:shape.max(net.minecraft.core.Direction.Axis.Y),r=mc.level.random.nextDouble();
        mc.level.addParticle(MOTE,mark.getX()+.2+.6*mc.level.random.nextDouble(),mark.getY()+top+.05,mark.getZ()+.2+.6*r,0,.012,0);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        var mc=Minecraft.getInstance();var mark=marked();if(mark==null||mc.player==null)return;
        var camera=e.getCamera().getPosition();if(camera.distanceToSqr(mark.getCenter())>MARK_RANGE*MARK_RANGE)return;
        var shape=mc.level.getBlockState(mark).getShape(mc.level,mark,CollisionContext.of(mc.player));if(shape.isEmpty())return;
        float alpha=.18F+.42F*breath(age+e.getPartialTick().getGameTimeDeltaPartialTick(false));
        var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();var lines=buffers.getBuffer(RenderType.lines());
        pose.pushPose();pose.translate(mark.getX()-camera.x,mark.getY()-camera.y,mark.getZ()-camera.z);
        for(var box:shape.toAabbs())LevelRenderer.renderLineBox(pose,lines,box.inflate(.004),1F,.9F,.66F,alpha);
        pose.popPose();buffers.endBatch(RenderType.lines());
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){at=null;world=null;}
}
