package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.CamcorderItem;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes;
import net.minecraft.core.component.DataComponents;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

/** An ordinary first-person camera, actual GPU keyframes and a small record light; no stolen player camera or game mode. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CamcorderClient {
    private static UUID pending;private static Object world;private static int frames;private static CameraType previous;
    private CamcorderClient() {}
    public static boolean active(){var p=Minecraft.getInstance().player;if(p==null||p.isSpectator()||!p.isAlive()||!p.isUsingItem()||!(p.getUseItem().getItem() instanceof CamcorderItem))return false;var data=p.getUseItem().get(DataComponents.CUSTOM_DATA);if(data==null)return false;var tag=data.copyTag();return tag.hasUUID(LiteraryVignettes.OWNER)&&tag.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}
    public static void expose(CamcorderExposurePayload p){if(active()){pending=p.nonce();world=Minecraft.getInstance().level;frames=0;}}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(active()){if(previous==null)previous=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);}else{if(previous!=null)mc.options.setCameraType(previous);previous=null;pending=null;world=null;}}
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){if(active())e.setFOV(70);}
    @SubscribeEvent public static void hand(RenderHandEvent e){if(active())e.setCanceled(true);}
    @SubscribeEvent public static void capture(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||pending==null)return;var mc=Minecraft.getInstance();
        if(!active()||world!=mc.level){pending=null;return;}if(++frames<2)return;var nonce=pending;pending=null;
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            int[] rgb=new int[384];double aspect=24D/16;int w=Math.min(image.getWidth(),(int)(image.getHeight()*aspect)),h=(int)(w/aspect),x0=(image.getWidth()-w)/2,y0=(image.getHeight()-h)/2;
            for(int y=0;y<16;y++)for(int x=0;x<24;x++){int c=image.getPixelRGBA(x0+(2*x+1)*w/48,y0+(2*y+1)*h/32);rgb[x+y*24]=(c&255)<<16|(c>>8&255)<<8|(c>>16&255);}
            PacketDistributor.sendToServer(new CamcorderFramePayload(nonce,rgb));
        }
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camcorder"),(g,d)->{
            if(!active())return;int x=g.guiWidth()-55,y=18;g.fill(x,y,x+4,y+4,0xffb73c35);g.drawString(Minecraft.getInstance().font,"REC",x+8,y-2,0xffe8dfcc,false);
            g.blit(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/camcorder.png"),12,12,0,0,24,16,24,16);
        });}
    }
}
