package io.github.knaitoe.theoldesthouse.client;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardRegistry;
import io.github.knaitoe.theoldesthouse.network.HazardViewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Only the recipient's own skin is drawn; no reflected entity is spawned or tracked by another client. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HazardClient {
    private static HazardViewPayload state;private static ClientLevel level;private static LocalPlayer reader;private static int lease;
    private HazardClient(){}
    public static void accept(HazardViewPayload payload){if(payload.mode()==0){clear();return;}var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;state=payload;level=mc.level;reader=mc.player;lease=16;}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&state!=null&&mc.level==level&&mc.player==reader&&reader!=null&&reader.isAlive()&&!reader.isSpectator()&&level.dimension().equals(HouseDimensions.INTERIOR);}
    private static void clear(){state=null;reader=null;level=null;lease=0;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(!active()){clear();return;}if(state.mode()==2)reader.setDeltaMovement(reader.getDeltaMovement().add(state.view().getFloat("Push"),0,0));}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(active()&&state.mode()==2)e.setRoll(e.getRoll()+state.view().getFloat("Tilt")*7*Minecraft.getInstance().options.screenEffectScale().get().floatValue());}
    public static PlayerModel<LocalPlayer> model(Minecraft mc,boolean slim){return new PlayerModel<>(mc.getEntityModels().bakeLayer(slim?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),slim);}
    public static void pose(PlayerModel<LocalPlayer> model,float pitch,boolean crouch,float stride,float time){
        java.util.stream.Stream.of(model.head,model.hat,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg,model.jacket,model.leftSleeve,model.rightSleeve,model.leftPants,model.rightPants).forEach(net.minecraft.client.model.geom.ModelPart::resetPose);
        model.crouching=crouch;model.head.xRot=pitch*(float)Math.PI/180;model.hat.copyFrom(model.head);model.rightLeg.xRot=(float)Math.cos(time*.35)*stride;model.leftLeg.xRot=-model.rightLeg.xRot;model.rightArm.xRot=-model.rightLeg.xRot;model.leftArm.xRot=model.rightLeg.xRot;
        if(crouch){model.body.xRot=.5F;model.body.y=3.2F;model.head.y=4.2F;model.leftArm.y=model.rightArm.y=5.2F;model.leftLeg.y=model.rightLeg.y=12.2F;model.leftLeg.z=model.rightLeg.z=4;model.hat.copyFrom(model.head);}model.jacket.copyFrom(model.body);model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);model.leftPants.copyFrom(model.leftLeg);model.rightPants.copyFrom(model.rightLeg);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS||!active()||state.mode()!=1)return;var mc=Minecraft.getInstance();var t=state.view();var at=new net.minecraft.world.phys.Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z"));var eye=e.getCamera().getPosition();if(eye.distanceToSqr(at)>3600)return;
        var skin=reader.getSkin();var model=model(mc,skin.model()==PlayerSkin.Model.SLIM);pose(model,t.getFloat("Pitch"),t.getBoolean("Crouch"),t.getFloat("Stride"),reader.tickCount);
        var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();pose.pushPose();pose.translate(at.x-eye.x,at.y-eye.y,at.z-eye.z);pose.mulPose(Axis.YP.rotationDegrees(180-t.getFloat("Yaw")));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);
        model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin.texture())),LevelRenderer.getLightColor(level,BlockPos.containing(at)),OverlayTexture.NO_OVERLAY,0xffd2d9dc);pose.popPose();buffers.endBatch();}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->ItemBlockRenderTypes.setRenderLayer(HazardRegistry.PIECE.get(),RenderType.translucent()));}
    }
}
