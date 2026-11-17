package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.KeptVoicePayload;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 0.4.675, the Kept: the machine's line, large and white across the middle of the view as the vanilla subtitle drew it, but wrapped
 * to the screen's width and, where even that would not fit, drawn smaller (owner: "it doesn't fit into the screen"). It comes in over
 * six ticks and goes over twenty, as the title did.
 */
public final class KeptVoiceClient {
    private KeptVoiceClient(){}
    private static String text="";private static long start=-1,until=-1;
    static final int IN=6,OUT=20;
    public static void accept(KeptVoicePayload p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        text=p.text();start=mc.level.getGameTime();until=start+Math.max(IN+OUT+20,p.ticks());
    }
    /** The lines the words wrap to at a width, and the scale they are drawn at: as large as the subtitle (2), smaller only if need be. */
    static float scale(int width,int height,int lines){return Mth.clamp(Math.min(2F,(height*.45F)/(lines*10F)),1F,2F);}
    static void render(GuiGraphics g,DeltaTracker delta){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.options.hideGui||text.isEmpty())return;
        float now=mc.level.getGameTime()+delta.getGameTimeDeltaPartialTick(false);if(now>until){text="";return;}
        float alpha=Mth.clamp(Math.min((now-start)/IN,(until-now)/OUT),0,1);if(alpha<=.02F)return;
        var font=mc.font;int w=g.guiWidth(),h=g.guiHeight();
        float scale=2F;List<FormattedCharSequence> lines=font.split(Component.literal(text),(int)((w-48)/scale));
        float fit=scale(w,h,lines.size());
        if(fit<scale){scale=fit;lines=font.split(Component.literal(text),(int)((w-48)/scale));}
        var pose=g.pose();pose.pushPose();
        float top=h/2F-(lines.size()*10*scale)/2F-6;
        pose.translate(w/2F,top,0);pose.scale(scale,scale,1);
        int colour=((int)(alpha*255)<<24)|0xFFFFFF;
        for(int i=0;i<lines.size();i++){var line=lines.get(i);g.drawString(font,line,-font.width(line)/2,i*10,colour,true);}
        pose.popPose();
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layer{
        private Layer(){}
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){
            e.registerAbove(VanillaGuiLayers.TITLE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"kept_voice"),KeptVoiceClient::render);
        }
    }
}
