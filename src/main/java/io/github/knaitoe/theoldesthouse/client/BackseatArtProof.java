package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native material/model inspection. This is art coverage, not a playable-scene claim. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatArtProof extends Screen {
    private static final String[] VIEWS={"front-open","rear-open","side-closed","front-closed","door-half","rear-quarter"};
    private static boolean launched;
    private int page,frames;
    private BackseatArtProof(){super(Component.literal("Backseat native car materials"));}
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.backseatArtSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){
            launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new BackseatArtProof());
        }
    }
    @Override protected void init(){
        var mc=Minecraft.getInstance();
        for(String name:BackseatArt.NEW_MATERIALS)try(var in=mc.getResourceManager().open(BackseatArt.texture(name));var image=NativeImage.read(in)){
            if(image.getWidth()!=16||image.getHeight()!=16)throw new IllegalStateException("Non-native Backseat material size: "+name);
            var colors=new HashSet<Integer>();
            for(int y=0;y<16;y++)for(int x=0;x<16;x++){int color=image.getPixelRGBA(x,y);colors.add(color);if((color>>>24)!=255)throw new IllegalStateException("Holes in opaque Backseat material: "+name);}
            if(colors.size()>6)throw new IllegalStateException("Backseat palette exceeds the authored flat colors: "+name);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
        for(String name:new String[]{"wood","glass","lamp","tail"})if(mc.getResourceManager().getResource(BackseatArt.texture(name)).isEmpty())throw new IllegalStateException("Missing established Backseat material: "+name);
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff303838);
        g.drawString(font,"Backseat / "+VIEWS[page],10,10,0xffede1ca,false);
        g.drawString(font,"Native 16px materials beside spruce, iron and black wool",10,height-19,0xffede1ca,false);g.flush();
        var out=mc.renderBuffers().bufferSource();var p=g.pose();p.pushPose();
        float scale=Math.min(width*.10F,height*.14F);
        p.translate(width*.50F,height*.50F,180);p.scale(scale,-scale,scale);
        p.mulPose(Axis.XP.rotationDegrees(24));
        p.mulPose(Axis.YP.rotationDegrees(new float[]{142,38,90,142,142,-35}[page]));
        int light=LightTexture.FULL_BRIGHT;
        BackseatArt.draw(p,out,light,OverlayTexture.NO_OVERLAY,page<2||page==5?1:page==4?.5F:0,page==4?62:18);
        out.endBatch();p.popPose();
        for(int i=0;i<3;i++){
            p.pushPose();p.translate(width*(.22F+i*.28F),height*.84F,180);p.scale(scale,-scale,scale);
            p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(142));
            p.translate(-.5,0,-.5);
            mc.getBlockRenderer().renderSingleBlock((i==0?Blocks.SPRUCE_PLANKS:i==1?Blocks.IRON_BLOCK:Blocks.BLACK_WOOL).defaultBlockState(),p,out,light,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof BackseatArtProof proof)||++proof.frames<20)return;
        var dir=Path.of("../build/backseat-art");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-backseat-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("passed.txt"),"Six native production car mesh views beside vanilla spruce planks, iron and black wool. Seven exact 16x16 opaque paintings, at most six flat colors each, and established wood/glass/lamp resources loaded. Hinged driver leaf, separate seats, floor, wheels, gauges, mirror and wipers rendered. This validates art resources only; the Backseat story is not yet implemented.\n");mc.stop();
    }
}
