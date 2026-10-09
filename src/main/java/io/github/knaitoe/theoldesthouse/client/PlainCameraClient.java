package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PlainCameraItem;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
/** The developed map is sampled from this client's actual composed frame, never stock illustration. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PlainCameraClient {
    private static UUID pending;private static Object world;private static int frames;
    public static boolean active(){var p=Minecraft.getInstance().player;return p!=null&&p.isUsingItem()&&(p.getUseItem().getItem() instanceof PlainCameraItem||p.getUseItem().is(net.minecraft.world.item.Items.SPYGLASS));}
    public static void expose(PlainExposurePayload payload){pending=payload.nonce();world=Minecraft.getInstance().level;frames=0;}
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){var p=Minecraft.getInstance().player;if(p!=null&&p.isUsingItem()&&p.getUseItem().getItem() instanceof PlainCameraItem)e.setFOV(e.getFOV()*.42);}
    @SubscribeEvent public static void hand(RenderHandEvent e){var p=Minecraft.getInstance().player;if(p!=null&&p.isUsingItem()&&p.getUseItem().getItem() instanceof PlainCameraItem)e.setCanceled(true);}
    @SubscribeEvent public static void capture(RenderFrameEvent.Post e){
        if(pending==null)return;var mc=Minecraft.getInstance();
        if(mc.level!=world||!active()){pending=null;return;}if(++frames<2)return;
        UUID nonce=pending;pending=null;
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            if(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.enabled()){
                java.nio.file.Files.createDirectories(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.folder());
                image.writeToFile(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.folder().resolve(System.getProperty("the_oldest_house.liveProofRole","")+"-actual-exposure.png"));
            }
            int side=(int)(Math.min(image.getWidth(),image.getHeight())*.70),left=(image.getWidth()-side)/2,top=(image.getHeight()-side)/2;
            int[] palette=new int[256];for(int i=4;i<256;i++)palette[i]=MapColor.getColorFromPackedId(i);
            byte[] pixels=new byte[16384];
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){
                int rgb=image.getPixelRGBA(left+x*side/128,top+y*side/128),best=4;long score=Long.MAX_VALUE;
                for(int i=4;i<256;i++){int c=palette[i];if((c>>>24)==0)continue;int r=(rgb&255)-(c&255),g=(rgb>>8&255)-(c>>8&255),b=(rgb>>16&255)-(c>>16&255);long distance=2L*r*r+4L*g*g+3L*b*b;if(distance<score){score=distance;best=i;}}
                pixels[x+y*128]=(byte)best;
            }
            PacketDistributor.sendToServer(new PlainFramePayload(nonce,pixels));
        }catch(java.io.IOException problem){throw new IllegalStateException("Could not record the native camera proof",problem);}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camera_viewfinder"),(g,d)->{
            var p=Minecraft.getInstance().player;if(p==null||!p.isUsingItem()||!(p.getUseItem().getItem() instanceof PlainCameraItem))return;
            int side=(int)(Math.min(g.guiWidth(),g.guiHeight())*.72),left=(g.guiWidth()-side)/2,top=(g.guiHeight()-side)/2;
            for(int x:new int[]{left,left+side})for(int y:new int[]{top,top+side}){int dx=x==left?1:-1,dy=y==top?1:-1;g.fill(Math.min(x,x+dx*12),y,Math.max(x,x+dx*12),y+1,0xffd0d2c6);g.fill(x,Math.min(y,y+dy*12),x+1,Math.max(y,y+dy*12),0xffd0d2c6);}
        });}
    }
}
