package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.SphinxDreamPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 0.4.342, the Sphinx's dream on the dreamer's screen (docs/SPHINX_DREAM_0_4_342.md). Before the first waking her face fills the screen,
 * out of the dark: the face of the first crossing, close enough that only her eyes and mouth are in view, her eyes on the dreamer, her
 * mouth moving as she asks, and the skin under one eye shifting when she draws breath. At the first waking her face is still there and
 * goes slowly off it. The first waking goes to black, and the second comes up out of the black. Her words come in the action bar above
 * her face, as every unseen voice's do.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SphinxDreamClient {
    private SphinxDreamClient(){}
    private static int stage=SphinxDreamPayload.NONE;private static long at,ticks;
    /** Ticks for the face to go off the first waking, the first waking to go dark, the second to come up out of the dark. */
    static final int WAKE=50,FADE=40,CLEAR=60;
    public static void accept(SphinxDreamPayload p){stage=p.stage();at=ticks;}
    public static int stage(){return stage;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        ticks++;
        // A beat that the server never ends (a lost connection) clears itself.
        if(stage==SphinxDreamPayload.WAKE&&ticks-at>WAKE||stage==SphinxDreamPayload.CLEAR&&ticks-at>CLEAR||stage!=SphinxDreamPayload.NONE&&ticks-at>20*90)stage=SphinxDreamPayload.NONE;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){stage=SphinxDreamPayload.NONE;}
    /** Drawn over the sleeping screen too, which opens while the dreamer lies in the dream's bed. */
    @SubscribeEvent public static void overScreen(ScreenEvent.Render.Post e){if(e.getScreen() instanceof InBedChatScreen)draw(e.getGuiGraphics(),e.getPartialTick());}

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers{
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){
            e.registerBelow(VanillaGuiLayers.OVERLAY_MESSAGE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"sphinx_dream"),(g,delta)->draw(g,delta.getGameTimeDeltaPartialTick(false)));
        }
    }

    static void draw(GuiGraphics g,float partial){
        if(stage==SphinxDreamPayload.NONE)return;
        float t=ticks-at+partial;int w=g.guiWidth(),h=g.guiHeight();
        float black,face;
        switch(stage){
            case SphinxDreamPayload.FACE->{black=1;face=1;}
            case SphinxDreamPayload.WAKE->{black=face=1-Mth.clamp(t/WAKE,0,1);}
            case SphinxDreamPayload.FADE->{black=Mth.clamp(t/FADE,0,1);face=0;}
            case SphinxDreamPayload.CLEAR->{black=1-Mth.clamp(t/CLEAR,0,1);face=0;}
            default->{return;}
        }
        if(black<=.002F)return;
        RenderSystem.enableBlend();
        g.fill(0,0,w,h,((int)(255*black)<<24));
        if(face>.002F)face(g,w,h,ticks+partial,face);
        RenderSystem.setShaderColor(1,1,1,1);RenderSystem.disableBlend();
    }
    /** Her face, close: the picture centred between her eyes and her mouth, scaled so they fill the screen from top to bottom. */
    static void face(GuiGraphics g,int w,int h,float now,float alpha){
        float s=h*.92F/150F;float cx=w/2F,cy=h/2F;
        float x0=cx-144*s,y0=cy-150*s;
        float breath=(Mth.sin(now*.07F)+1)/2;
        float zoom=1+.012F*breath;
        var pose=g.pose();pose.pushPose();pose.translate(cx,cy,0);pose.scale(zoom,zoom,1);pose.translate(-cx,-cy,0);
        RenderSystem.setShaderColor(1,1,1,alpha);
        // Her eyes behind the holes, on the dreamer, drifting a little.
        float gx=Mth.sin(now*.031F)*3,gy=Mth.cos(now*.023F)*1.5F;
        for(int[] e:new int[][]{{100,100},{188,100}}){
            blit(g,SphinxFaceRenderer.WHITE,x0+(e[0]-33)*s,y0+(e[1]-15)*s,66*s,30*s,0,0,1,1);
            blit(g,SphinxFaceRenderer.IRIS,x0+(e[0]-15+gx)*s,y0+(e[1]-15+gy)*s,30*s,30*s,0,0,1,1);
        }
        blit(g,SphinxFaceRenderer.FACE,x0,y0,288*s,240*s,0,0,1,1);
        // The skin beneath one eye moves when she draws breath.
        float shift=breath*breath*1.4F;
        blit(g,SphinxFaceRenderer.FACE,x0+76*s,y0+(114+shift)*s,48*s,14*s,76/288F,114/240F,124/288F,128/240F);
        // Her mouth, while she asks.
        int mouth=SphinxMouth.frame(SphinxDreamRenderer.clientTime());
        if(mouth>0){float v0=(float)(mouth-1)/SphinxFaceRenderer.MOUTH_FRAMES;
            blit(g,SphinxFaceRenderer.MOUTH,x0+SphinxFaceRenderer.MOUTH_X0*s,y0+SphinxFaceRenderer.MOUTH_Y0*s,SphinxFaceRenderer.MOUTH_W*s,SphinxFaceRenderer.MOUTH_H*s,0,v0,1,v0+1F/SphinxFaceRenderer.MOUTH_FRAMES);}
        pose.popPose();
    }
    /** A picture stretched over a rectangle of the screen, from texture coordinates (u0, v0) to (u1, v1). */
    static void blit(GuiGraphics g,ResourceLocation tex,float x,float y,float w,float h,float u0,float v0,float u1,float v1){
        int[] size=tex==SphinxFaceRenderer.FACE?new int[]{288,240}:tex==SphinxFaceRenderer.WHITE?new int[]{64,32}:tex==SphinxFaceRenderer.IRIS?new int[]{32,32}:new int[]{SphinxFaceRenderer.MOUTH_W,SphinxFaceRenderer.MOUTH_H*SphinxFaceRenderer.MOUTH_FRAMES};
        int tw=size[0],th=size[1];
        g.blit(tex,Math.round(x),Math.round(y),Math.max(1,Math.round(w)),Math.max(1,Math.round(h)),u0*tw,v0*th,Math.max(1,Math.round((u1-u0)*tw)),Math.max(1,Math.round((v1-v0)*th)),tw,th);
    }
}
