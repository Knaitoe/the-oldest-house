package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MinotaurEntity;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Native animated adult model, materials and render-cost proof after transformation. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class MinotaurVisualProof extends Screen {
    private MinotaurModel model;private int frames;private long total,max;
    public MinotaurVisualProof(){super(Component.literal("Minotaur combat rendering"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        long start=System.nanoTime();var mc=Minecraft.getInstance();if(model==null)model=new MinotaurModel(mc.getEntityModels().bakeLayer(MinotaurModel.LAYER));
        g.fill(0,0,width,height,0xFF23211F);g.drawString(font,"Native transformed creature / animated attack phases",12,10,0xFFE4DBCB,false);
        String[] names={"Watching","Windup","Charge","Stagger"};int[] phases={MinotaurEntity.WATCHING,MinotaurEntity.WINDUP,MinotaurEntity.CHARGING,MinotaurEntity.STUNNED};
        for(int i=0;i<4;i++){
            int left=i*width/4+8;g.fill(left,32,left+width/4-16,height-25,0xFF3B3530);g.drawString(font,names[i],left+4,38,0xFFE4DBCB,false);g.flush();
            float scale=Math.min(45,Math.min((height-92)/4.25F,(width/4F-24)/2.7F));
            var pose=g.pose();pose.pushPose();pose.translate(left+width/8-8,(height+28)/2F+scale*.45F,180);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(205));
            model.pose(phases[i],frames*.5F,.6F,frames,0,0);var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(FinaleClientEvents.MATERIALS)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();pose.popPose();
        }
        long cost=System.nanoTime()-start;if(frames>=4){total+=cost;max=Math.max(max,cost);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof MinotaurVisualProof p)||++p.frames<64)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-minotaur.png"));}
        double average=p.total/60_000_000.0;Files.writeString(dir.resolve("minotaur-render-passed.txt"),"64 native rendered frames / four adult attack poses.\nMean CPU render submission: "+average+" ms; maximum: "+p.max/1_000_000.0+" ms.\n");
        if(average>250)throw new IllegalStateException("Native Minotaur rendering stalls: "+average+" ms");
        mc.setScreen(new InventoryVisualProof());
    }
}
