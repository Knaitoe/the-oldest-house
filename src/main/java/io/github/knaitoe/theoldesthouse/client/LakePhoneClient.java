package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakePhoneCamera;
import io.github.knaitoe.theoldesthouse.network.LakePhonePayload;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Client controls always expire; a disconnect, dimension switch or missing camera restores normal play. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LakePhoneClient {
    private static int phase,cameraId=-1,elapsed,lease;
    private static CameraType previousCamera;
    private static net.minecraft.client.multiplayer.ClientLevel sceneLevel;
    private LakePhoneClient(){}
    public static void accept(LakePhonePayload packet){
        if(packet.phase()==0){clear();return;}
        phase=packet.phase();cameraId=packet.cameraId();elapsed=packet.elapsed();lease=80;
    }
    public static boolean bound(){return phase>=2&&lease>0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(lease>0){lease--;elapsed++;}enforce();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre event){enforce();}
    private static void enforce(){
        Minecraft mc=Minecraft.getInstance();
        if(!bound()||mc.player==null||mc.level==null||mc.player.isDeadOrDying()||(sceneLevel!=null&&sceneLevel!=mc.level)){clear();return;}
        if(previousCamera==null){previousCamera=mc.options.getCameraType();sceneLevel=mc.level;}
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(mc.screen instanceof AbstractContainerScreen<?>)mc.setScreen(null);
        if(phase>=3&&mc.level.getEntity(cameraId) instanceof LakePhoneCamera camera)mc.setCameraEntity(camera);
    }
    public static void clear(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.getCameraEntity() instanceof LakePhoneCamera)mc.setCameraEntity(mc.player);
        if(previousCamera!=null)mc.options.setCameraType(previousCamera);
        previousCamera=null;sceneLevel=null;phase=0;cameraId=-1;lease=0;
    }
    @SubscribeEvent public static void input(MovementInputUpdateEvent event){
        if(!bound())return;var input=event.getInput();input.forwardImpulse=0;input.leftImpulse=0;
        input.up=false;input.down=false;input.left=false;input.right=false;input.jumping=false;input.shiftKeyDown=false;
    }
    @SubscribeEvent public static void interact(InputEvent.InteractionKeyMappingTriggered event){if(bound()){event.setCanceled(true);event.setSwingHand(false);}}
    @SubscribeEvent public static void angles(ViewportEvent.ComputeCameraAngles event){
        if(bound()&&phase>=3){event.setYaw(0);event.setPitch(-88);event.setRoll(3);}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_phone"),(g,delta)->{
                if(!bound())return;int w=g.guiWidth(),h=g.guiHeight();var font=Minecraft.getInstance().font;
                g.fill(0,0,w,18,0xCC050709);g.fill(0,h-18,w,h,0xCC050709);
                g.drawString(font,"REC  "+String.format(java.util.Locale.ROOT,"00:%02d",elapsed/20),12,5,phase>=3?0xFFACB1AB:0xFFE66355,false);
            });
        }
    }
}
