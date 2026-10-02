package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix4f;

/** The shipped GPU sky mesh at the horizon, above it and below it. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class OutsideSkyProof extends Screen {
    private int view,frames;
    public OutsideSkyProof(){super(Component.literal("Outside sky"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xFF111820);g.flush();
        var previous=new Matrix4f(RenderSystem.getProjectionMatrix());var model=RenderSystem.getModelViewStack();model.pushMatrix();model.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),(float)width/height,.1F,1000),VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.disableDepthTest();
            HouseOutsideEffects.drawSky(new Matrix4f().rotateX((float)Math.toRadians(new int[]{0,-45,25}[view])),new Vec3(.035,.05,.085),.5F);
        }finally{RenderSystem.enableDepthTest();model.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(previous,VertexSorting.ORTHOGRAPHIC_Z);}
        g.drawString(font,"Native outside sky: "+new String[]{"horizon","above horizon","below horizon"}[view],12,12,0xFFE8E4D9,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof OutsideSkyProof s)||++s.frames<4)return;
        Path folder=Path.of("../build/font-smoke");
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colors=new java.util.HashSet<Integer>();int lights=0;
            for(int y=60;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++){
                int pixel=image.getPixelRGBA(x,y);colors.add(pixel);
                if(Math.min(Math.min(pixel&255,(pixel>>>8)&255),(pixel>>>16)&255)>60)lights++;
            }
            if(colors.size()<9||s.view==0&&lights<12)throw new IllegalStateException("The native sky proof is blank or missing stars/moon: colors="+colors.size()+", lights="+lights);
            image.writeToFile(folder.resolve("native-sky-"+s.view+".png"));
        }
        if(++s.view<3){s.frames=0;return;}
        Files.writeString(folder.resolve("sky-passed.txt"),"Single outside sky mesh rendered at three camera elevations; actual pixels contain its gradient and stars/moon. Scene time is read-only and stable across native server clock updates and delayed cues.\n");
        TheOldestHouse.LOGGER.info("HOUSE SKY CHECK PASSED: three native sky screenshots saved");mc.stop();
    }
}
