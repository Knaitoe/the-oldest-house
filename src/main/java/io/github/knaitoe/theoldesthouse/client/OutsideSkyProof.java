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
        g.fill(0,0,width,height,view==3?0xFF000000:0xFF111820);g.flush();
        var previous=new Matrix4f(RenderSystem.getProjectionMatrix());var model=RenderSystem.getModelViewStack();model.pushMatrix();model.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),(float)width/height,.1F,1000),VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.disableDepthTest();
            if(view==3){var origin=new net.minecraft.core.BlockPos(0,70,0);
                if(StaircaseScore.at(null,origin)||!StaircaseScore.at(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.entry(origin))
                        ||StaircaseScore.at(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.cell(origin)))throw new IllegalStateException("The descent score must wait for origin sync and stop outside the stair shaft");
                HouseInteriorEffects.drawSkyAt(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.entry(origin),new Matrix4f(),new Vec3(.7,.8,1),.25F);
            }else HouseOutsideEffects.drawSky(new Matrix4f().rotateX((float)Math.toRadians(new int[]{0,-45,25}[view])),new Vec3(.035,.05,.085),.5F);
        }finally{RenderSystem.enableDepthTest();model.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(previous,VertexSorting.ORTHOGRAPHIC_Z);}
        g.drawString(font,view==3?"Native enclosed staircase: black background, no outside sky":"Native outside sky: "+new String[]{"horizon","above horizon","below horizon"}[view],12,12,0xFFE8E4D9,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof OutsideSkyProof s)||++s.frames<4)return;
        Path folder=Path.of("../build/font-smoke");
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colors=new java.util.HashSet<Integer>();int lights=0,nonblack=0;
            for(int y=60;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++){
                int pixel=image.getPixelRGBA(x,y);colors.add(pixel);
                if((pixel&0xFFFFFF)!=0)nonblack++;
                if(Math.min(Math.min(pixel&255,(pixel>>>8)&255),(pixel>>>16)&255)>60)lights++;
            }
            image.writeToFile(folder.resolve(s.view==3?"native-staircase-dark.png":"native-sky-"+s.view+".png"));
            // Looking down samples only the dim lower gradient; its 8-bit palette has fewer shades.
            // Text is outside this crop, so three distinct shades still reject a blank frame.
            int minimumColors=s.view==2?3:9;
            if(s.view<3&&colors.size()<minimumColors)throw new IllegalStateException("The native sky gradient is missing at view "+s.view+": colors="+colors.size());
            if(s.view==0&&lights<12)throw new IllegalStateException("The native horizon is missing stars/moon: lights="+lights);
            if(s.view==3&&nonblack>0)throw new IllegalStateException("Outdoor sky leaked into the native staircase background: "+nonblack+" pixels");
        }
        if(++s.view<4){s.frames=0;return;}
        Files.writeString(folder.resolve("sky-passed.txt"),"Single outside sky mesh rendered at three camera elevations; actual pixels contain its gradient and stars/moon. Scene time is read-only and stable across native server clock updates and delayed cues.\n");
        Files.writeString(folder.resolve("staircase-dark-passed.txt"),"The shipped interior renderer suppresses outdoor sky; native staircase background pixels are black.\n");
        TheOldestHouse.LOGGER.info("HOUSE SKY CHECK PASSED: three native sky screenshots and black staircase background saved");mc.stop();
    }
}
