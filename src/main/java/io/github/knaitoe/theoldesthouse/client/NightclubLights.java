package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Six bounded coloured beams and floor pools; native monochrome block light stays dim. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NightclubLights {
    private static final int[] COLOURS={0xeb4870,0x467fe8,0x4fcc83};
    private NightclubLights(){}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!NightclubScore.inside())return;
        var mc=Minecraft.getInstance();var b=LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.NIGHTCLUB);
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();var type=RenderType.lightning();var v=buffers.getBuffer(type);
        for(int z:new int[]{-18,-32})for(int x:new int[]{-7,0,7}){
            BlockPos at=b.offset(x,4,z);var state=mc.level.getBlockState(at);
            if(!state.is(NightclubRegistry.BLOCK.get())||state.getValue(NightclubBlock.KIND)!=NightclubBlock.Kind.SPOT)continue;
            int rgb=COLOURS[state.getValue(NightclubBlock.STAGE)];pose.pushPose();pose.translate(at.getX()+.5-camera.x,b.getY()+.015-camera.y,at.getZ()+.5-camera.z);
            for(int i=0;i<16;i++){
                double a=i*Math.PI/8,c=(i+1)*Math.PI/8;float ax=(float)Math.cos(a)*2.1F,az=(float)Math.sin(a)*2.1F,cx=(float)Math.cos(c)*2.1F,cz=(float)Math.sin(c)*2.1F;
                float[][] cone={{ax,0,az},{cx,0,cz},{cx*.035F,4.85F,cz*.035F},{ax*.035F,4.85F,az*.035F}};
                for(int n=0;n<4;n++)v.addVertex(pose.last(),cone[n][0],cone[n][1],cone[n][2]).setColor((n<2?10:24)<<24|rgb);
                for(float[] point:new float[][]{{0,0,0},{ax,0,az},{cx,0,cz},{0,0,0}})v.addVertex(pose.last(),point[0],point[1],point[2]).setColor(0x42000000|rgb);
            }
            pose.popPose();
        }
        buffers.endBatch(type);
    }
}
