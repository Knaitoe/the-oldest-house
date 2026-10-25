package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

/** Local camera, native visual effects and item senses, bound to one actual reader and world. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class UncannyClient {
    private static CompoundTag view=new CompoundTag();private static ClientLevel world;private static int lease;private static float fov=70;
    private UncannyClient(){}
    public static void accept(UncannyViewPayload payload){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.player.isSpectator()){clear();return;}
        if(view.getString("Room").equals("missing_sleep")&&!payload.view().getString("Room").equals("missing_sleep"))UncannyCameras.clear();
        view=payload.view().copy();world=mc.level;lease=20;
        if(view.getBoolean("OpenMonitor")&&room().equals("missing_sleep"))mc.setScreen(new UncannyCameras.MonitorScreen());
    }
    public static boolean active(){var mc=Minecraft.getInstance();return lease>0&&mc.level==world&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator();}
    public static String room(){return active()?view.getString("Room"):"";}
    public static BlockPos base(){return BlockPos.of(view.getLong("Base"));}
    public static boolean green(){return active()&&view.getBoolean("Green");}
    public static boolean mirror(){var p=Minecraft.getInstance().player;return active()&&view.getBoolean("Mirror")&&p!=null&&(p.getMainHandItem().is(UncannyRegistry.SMOKY_MIRROR.get())||p.getOffhandItem().is(UncannyRegistry.SMOKY_MIRROR.get()));}
    public static boolean ring(){var p=Minecraft.getInstance().player;return active()&&view.getBoolean("Ring")&&p!=null&&p.getOffhandItem().is(UncannyRegistry.BONE_RING.get());}
    public static float fov(){return fov;}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){return active()&&level==world&&room().equals("second_waitress")?1:nativeRain;}
    public static int sleeps(){return active()?view.getInt("Sleeps"):0;}
    public static int doll(){return active()?view.getInt("Doll"):-1;}
    public static String sign(){return active()?view.getString("Sign"):"";}
    public static boolean outline(Entity e){
        if(!ring()||e.level()!=world||!e.isAlive())return false;
        var p=Minecraft.getInstance().player;if(p==null||p.distanceToSqr(e)>225)return false;
        for(var t:view.getList("Sensed",Tag.TAG_COMPOUND))if(((CompoundTag)t).getInt("Id")==e.getId())return true;return false;
    }
    public static String intent(Entity e){for(var t:view.getList("Sensed",Tag.TAG_COMPOUND)){var row=(CompoundTag)t;if(row.getInt("Id")==e.getId())return row.getString("Intent");}return "";}
    private static void clear(){view=new CompoundTag();lease=0;world=null;UncannyCameras.clear();}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){clear();UncannyTextures.clear();}
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov event){if(!UncannyCameras.recording())fov=(float)event.getFOV();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(lease>0)lease--;if(!active()){if(world!=null)clear();return;}
        if(!room().equals("missing_sleep")&&mc.screen instanceof UncannyCameras.MonitorScreen)mc.setScreen(null);
        if(!room().isEmpty()&&mc.player.tickCount%5==0){
            var c=mc.gameRenderer.getMainCamera();var at=c.getPosition();var d=c.getLookVector();
            PacketDistributor.sendToServer(new UncannyCameraPayload(at.x,at.y,at.z,d.x(),d.y(),d.z(),fov,(float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),mc.screen instanceof UncannyCameras.MonitorScreen m&&UncannyCameras.frames(m.selected())>0&&(m.selected()!=1||UncannyCameras.ownFace(1))?m.selected():-1));
        }
    }
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post event){
        if(!active())return;var mc=Minecraft.getInstance();var g=event.getGuiGraphics();
        if(room().equals("second_waitress")&&view.getInt("Age")>0){int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();g.fill(0,0,3,h,0x99101418);g.fill(w-3,0,w,h,0x99101418);}
        // The growth pulse has a static visual equivalent; it does not require forced camera shake.
        if(room().equals("layered_man")&&view.getInt("Age")%80<12){int h=mc.getWindow().getGuiScaledHeight();g.fill(0,h-4,mc.getWindow().getGuiScaledWidth(),h,0x99855e53);}
    }
    @SubscribeEvent public static void labels(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||!ring()||UncannyCameras.recording())return;
        var mc=Minecraft.getInstance();var eye=event.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();
        for(var t:view.getList("Sensed",Tag.TAG_COMPOUND)){
            var row=(CompoundTag)t;var e=world.getEntity(row.getInt("Id"));if(e==null||!outline(e))continue;var at=e.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false)).add(0,e.getBbHeight()+.25,0).subtract(eye);
            var p=new PoseStack();p.translate(at.x,at.y,at.z);p.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());p.scale(-.025F,-.025F,.025F);
            String text="["+row.getString("Intent")+"]";mc.font.drawInBatch(text,-mc.font.width(text)*.5F,0,0xffe2dccb,false,p.last().pose(),buffers,Font.DisplayMode.SEE_THROUGH,0x44181512,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
        }buffers.endBatch();
    }
}
