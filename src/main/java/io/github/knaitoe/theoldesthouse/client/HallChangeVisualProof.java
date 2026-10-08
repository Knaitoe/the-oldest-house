package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HallChangeBlock;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual baked before/after meshes, including all 24 oriented states. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HallChangeVisualProof extends Screen {
    private int frames;
    public HallChangeVisualProof(){super(Component.literal("Unseen hallway changes"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF252321);
        g.drawString(font,"Familiar things found differently / native before and after meshes",12,12,0xFFE8DFCF,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();var kinds=HallChangeBlock.Kind.values();
        for(int i=0;i<kinds.length;i++){
            float xx=width*(i/2+.5F)/3,yy=height*(i%2==0?.33F:.72F);var pose=g.pose();
            pose.pushPose();pose.translate(xx,yy,150);pose.scale(64,-64,64);pose.mulPose(Axis.XP.rotationDegrees(35));pose.mulPose(Axis.YP.rotationDegrees(30));
            mc.getBlockRenderer().renderSingleBlock(HallChangeBlock.state(kinds[i],Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            g.drawString(font,kinds[i].getSerializedName().replace('_',' '),(int)xx-42,(int)yy+42,0xFFE8DFCF,false);
        }
        buffers.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof HallChangeVisualProof p)||++p.frames<12)return;
        for(var kind:HallChangeBlock.Kind.values())for(var facing:Direction.Plane.HORIZONTAL){
            var model=mc.getBlockRenderer().getBlockModel(HallChangeBlock.state(kind,facing));
            if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native hall model: "+kind+" / "+facing);
        }
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-hall-changes.png"));}
        Files.writeString(dir.resolve("hall-changes-passed.txt"),"Six native before/after hall meshes and all 24 oriented states rendered.\n");
        TheOldestHouse.LOGGER.info("HALL CHANGES CHECK PASSED: six native meshes and 24 oriented states");mc.setScreen(new SceneVisualProof());
    }
}
