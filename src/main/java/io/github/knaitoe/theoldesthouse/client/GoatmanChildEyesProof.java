package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Dark native readback of all nine unchanged faces, with and without the production emissive layer. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanChildEyesProof extends Screen {
    private static boolean launched;
    private int frames;
    public GoatmanChildEyesProof(){super(Component.literal("Goatman child disguise eyes"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.goatmanEyesSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){
            launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new GoatmanChildEyesProof());
        }
    }
    private int center(int skin,boolean glow){return Math.round((skin%3+.5F)*width/3+(glow?32:-32));}
    private int baseline(int skin){return Math.round(42+(skin/3+1)*(height-60)/3F-18);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff14161a);
        g.drawString(font,"All nine child faces / ordinary eyes, then Goatman's amber glow",10,10,0xffeee0c4,false);g.flush();
        var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));
        var buffers=mc.renderBuffers().bufferSource();
        for(int skin=0;skin<9;skin++){
            var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+skin+".png");
            for(boolean glow:new boolean[]{false,true}){
                model.pose(skin,0,0,0,false,false,false);var poses=g.pose();poses.pushPose();
                poses.translate(center(skin,glow),baseline(skin),160);
                // GUI Y points down: reflect Z so the native, culled eyes pass sees the front face.
                poses.scale(90,90,-90);
                model.head.render(poses,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),LightTexture.pack(0,0),OverlayTexture.NO_OVERLAY,0xffffffff);
                if(glow)GoatmanChildEyes.render(model,poses,buffers);
                buffers.endBatch();poses.popPose();
            }
            g.drawCenteredString(font,"Child "+(skin+1),Math.round((skin%3+.5F)*width/3),baseline(skin)+8,0xffeee0c4);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof GoatmanChildEyesProof proof)||++proof.frames<24)return;
        try(var stream=mc.getResourceManager().open(GoatmanFigureRenderer.EYES);var atlas=NativeImage.read(stream)){
            int amber=atlas.getPixelRGBA(8,10);
            if((amber>>>24)!=255||(amber&255)<220||(amber>>8&255)<150||(amber>>16&255)>110)throw new IllegalStateException("The child eyes no longer sample the original amber eye texel");
        }
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);StringBuilder evidence=new StringBuilder();
        try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var view=dir.resolve("native-goatman-child-eyes.png");shot.writeToFile(view);
            if(Boolean.getBoolean("the_oldest_house.goatmanEyesSmoke")){
                String data=java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(view));
                TheOldestHouse.LOGGER.info("GOATMAN_EYES_VIEW_BEGIN");
                for(int i=0;i<data.length();i+=3800)TheOldestHouse.LOGGER.info("GOATMAN_EYES_VIEW_DATA {}",data.substring(i,Math.min(i+3800,data.length())));
                TheOldestHouse.LOGGER.info("GOATMAN_EYES_VIEW_END");
            }
            double factor=shot.getWidth()/(double)proof.width;
            for(int skin=0;skin<9;skin++){
                int left=(int)Math.round(proof.center(skin,false)*factor),right=(int)Math.round(proof.center(skin,true)*factor),base=(int)Math.round(proof.baseline(skin)*factor),changed=0;
                for(int dy=(int)(-44*factor);dy<0;dy++)for(int dx=(int)(-21*factor);dx<=21*factor;dx++){
                    int a=shot.getPixelRGBA(left+dx,base+dy),b=shot.getPixelRGBA(right+dx,base+dy);
                    int red=b&255,green=b>>8&255,blue=b>>16&255;
                    int difference=Math.abs((a&255)-red)+Math.abs((a>>8&255)-green)+Math.abs((a>>16&255)-blue);
                    if(difference>60&&red>160&&green>100&&red>=blue)changed++;
                }
                if(changed<20)throw new IllegalStateException("The child disguise has no visible emissive eye pixels: skin="+skin+" changed="+changed);
                evidence.append("Child ").append(skin).append(": ").append(changed).append(" visible emissive pixels at minimum packed light.\n");
            }
        }
        Files.writeString(dir.resolve("goatman-child-eyes-passed.txt"),evidence.toString());
        if(Boolean.getBoolean("the_oldest_house.goatmanEyesSmoke"))mc.stop();else mc.setScreen(new UncannyAssetProof());
    }
}
