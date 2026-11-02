package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PolaroidTrail;
import io.github.knaitoe.theoldesthouse.network.PolaroidFlashPayload;
import io.github.knaitoe.theoldesthouse.network.PolaroidViewPayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 0.4.204, the Polaroid Trail, on the reader's own client: a developed photograph held up to look at (the server's picture,
 * dithered to black and white, in its white border with the caption in pencil under it), and the camera's flash over their
 * sight.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PolaroidClient {
    private PolaroidClient(){}
    private static int flash,flashTotal;private static float strength;
    private static @Nullable DynamicTexture texture;private static @Nullable ResourceLocation location;

    public static void flash(PolaroidFlashPayload p){if(p.ticks()>=flash){flash=p.ticks();flashTotal=Math.max(1,p.ticks());strength=p.strength();}}
    public static void view(PolaroidViewPayload p){
        if(p.frame().length!=PolaroidTrail.FW*PolaroidTrail.FH)return;var mc=Minecraft.getInstance();
        if(texture==null){texture=new DynamicTexture(PolaroidTrail.FW,PolaroidTrail.FH,false);location=mc.getTextureManager().register("the_oldest_house_polaroid",texture);}
        var img=texture.getPixels();if(img==null)return;
        // Ordered dithering to black and white, as a cheap instant camera's print looks up close.
        int[][] bayer={{0,8,2,10},{12,4,14,6},{3,11,1,9},{15,7,13,5}};
        for(int y=0;y<PolaroidTrail.FH;y++)for(int x=0;x<PolaroidTrail.FW;x++){
            int v=p.frame()[x+y*PolaroidTrail.FW]&255;v=Math.min(255,(int)(v*1.35));
            int threshold=(bayer[y&3][x&3]*16)+8;int c=v>threshold?0xE8E6E0:0x141210;
            img.setPixelRGBA(x,y,0xFF000000|(c&255)<<16|(c>>8&255)<<8|(c>>16&255));
        }
        texture.upload();mc.setScreen(new Photo(p.caption()));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(flash>0)flash--;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){flash=0;}

    static final class Photo extends Screen {
        private final String caption;
        Photo(String caption){super(Component.literal("Photograph"));this.caption=caption;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            super.render(g,mx,my,partial);
            int scale=Math.max(2,Math.min((width-60)/PolaroidTrail.FW,(height-80)/PolaroidTrail.FH));int w=PolaroidTrail.FW*scale,h=PolaroidTrail.FH*scale;
            int x=(width-w)/2,y=(height-h)/2-12;
            g.fill(x-8,y-8,x+w+8,y+h+34,0xFFF4F2EC);g.fill(x-1,y-1,x+w+1,y+h+1,0xFF2A2826);
            if(location!=null)g.blit(location,x,y,w,h,0F,0F,PolaroidTrail.FW,PolaroidTrail.FH,PolaroidTrail.FW,PolaroidTrail.FH);
            g.drawCenteredString(font,caption,width/2,y+h+14,0xFF3A3E6A);
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polaroid_flash"),(g,d)->{
            if(flash<=0)return;float k=strength*(flash/(float)flashTotal)*Comfort.effects();if(k<=0)return;int a=(int)(Math.min(1,k*1.2F)*255);
            g.fill(0,0,g.guiWidth(),g.guiHeight(),a<<24|0xF4F8FF);
        });}
    }
}
