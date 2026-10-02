package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.PersonalProjectionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import com.mojang.math.Axis;
/** A native map projected onto the wall, visible only to its recipient. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PersonalProjectionClient {
    private static BlockPos wall;private static int map=-1,lease;
    public static void accept(PersonalProjectionPayload p){wall=p.wall();map=p.map();lease=60;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(lease==0){wall=null;map=-1;}}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS||lease<=0||wall==null||map<0)return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;var data=mc.level.getMapData(new MapId(map));if(data==null)return;
        var camera=e.getCamera().getPosition();if(camera.distanceToSqr(wall.getCenter())>400)return;
        var pose=e.getPoseStack();pose.pushPose();pose.translate(wall.getX()+2.5-camera.x,wall.getY()+1.5-camera.y,wall.getZ()+.99-camera.z);
        pose.mulPose(Axis.ZP.rotationDegrees(180));pose.scale(5F/128F,3F/128F,1F/128F);
        mc.gameRenderer.getMapRenderer().render(pose,mc.renderBuffers().bufferSource(),new MapId(map),data,true,LightTexture.FULL_BRIGHT);
        pose.popPose();mc.renderBuffers().bufferSource().endBatch();
    }
}
