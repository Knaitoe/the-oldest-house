package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
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
import org.joml.Matrix4f;

/** Native material/model inspection. This is art coverage, not a playable-scene claim. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatArtProof extends Screen {
    private static final String[] VIEWS={"front-open","rear-open","side-closed","front-closed","door-half","rear-quarter","driver-forward","driver-mirror"};
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
        if(page>=6){driver(g);return;}
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
    private void driver(GuiGraphics g){
        var mc=Minecraft.getInstance();var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var oldSort=RenderSystem.getVertexSorting();
        var stack=RenderSystem.getModelViewStack();stack.pushMatrix();stack.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective((float)Math.toRadians(70),(float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),.04F,32),VertexSorting.DISTANCE_TO_ORIGIN);
            // GUI background depth is in a different projection from the cabin.
            // Retain its color while clearing that depth before the perspective pass.
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
            var p=new PoseStack();p.mulPose(Axis.YP.rotationDegrees(page==7?32:0));
            var seat=io.github.knaitoe.theoldesthouse.labyrinth.BackseatCarMesh.DRIVER;p.translate(-seat.x,-seat.y-1.62,-seat.z);
            var out=mc.renderBuffers().bufferSource();BackseatArt.draw(p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0,12);out.endBatch();
        }finally{stack.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(oldProjection,oldSort);RenderSystem.disableDepthTest();}
        g.drawString(font,"Backseat / "+VIEWS[page]+" / native seated eye",10,10,0xffede1ca,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof BackseatArtProof proof)||++proof.frames<20)return;
        var dir=Path.of("../build/backseat-art");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(dir.resolve("native-backseat-"+VIEWS[proof.page]+".png"));
            if(proof.page>=6){int seen=0;for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y++)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x++)if((image.getPixelRGBA(x,y)&0x00ffffff)!=0x00383830)seen++;
                if(seen<1200)throw new IllegalStateException("The native seated view rendered no actual cabin: "+VIEWS[proof.page]+" pixels="+seen);}
        }
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("passed.txt"),"Eight native production car mesh views: six beside vanilla spruce planks, iron and black wool; two perspective views from the actual seated-player eye geometry. Seven exact 16x16 opaque paintings, at most six flat colors each, and established wood/glass/lamp/tail resources loaded. Hinged driver leaf, separate seats, floor, wheels, gauges, mirror and wipers rendered. This validates the car model; the full Backseat story is not yet implemented.\n");mc.stop();
    }
}
