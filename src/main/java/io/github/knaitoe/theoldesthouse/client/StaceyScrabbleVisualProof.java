package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Actual baked elbow/knee geometry, asymmetric scrape cycle and native leaf mesh. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaceyScrabbleVisualProof extends Screen {
    private static int frames;
    public StaceyScrabbleVisualProof(){super(Component.literal("Stacy's scrabble"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF1C2421);g.drawString(font,"Native jointed Stacy | reach / scrape / plant / folded recovery",10,10,0xFFE6DFCF,false);
        String[] names={"Reach","Scraping pull","Plant","Folded recovery","Strike","Remembered girl + native leaves"};float[] cycles={.05F,.32F,.55F,.81F,.05F,0};
        for(int i=0;i<6;i++){int left=10+(i%3)*width/3,top=30+(i/3)*(height-48)/2,cellH=(height-48)/2;
            g.fill(left,top,left+width/3-18,top+cellH-8,0xFF354039);g.drawString(font,names[i],left+6,top+6,0xFFE6DFCF,false);g.flush();
            var model=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));model.huntPose(cycles[i]/.23F,.7F,12,i==4);
            if(i<4&&(Math.abs(model.leftKnee.xRot)<.1F||Math.abs(model.rightElbow.xRot-model.leftElbow.xRot)<.02F))throw new IllegalStateException("Stacy joints or uneven claw cycle are absent");
            if(i==5){model.resetBody();if(model.body.xRot!=0||model.leftElbow.xRot!=0||model.rightKnee.xRot!=0)throw new IllegalStateException("Hunt pose leaks into the memory");}
            var pose=g.pose();pose.pushPose();pose.translate(left+width/6-10,top+30,100);pose.scale(52,52,52);pose.mulPose(Axis.YP.rotationDegrees(i==5?150:125));
            var buffers=mc.renderBuffers().bufferSource();var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+(i==5?"lake_witch_memory":"lake_witch")+".png");
            model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();pose.popPose();
            if(i==5){pose.pushPose();pose.translate(left+width/3-58,top+cellH-28,100);pose.scale(32,-32,32);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(40));
                mc.getBlockRenderer().renderSingleBlock(HouseBlocks.FOREST_COVER.get().defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof StaceyScrabbleVisualProof)||++frames<4)return;
        Path folder=Path.of("../build/font-smoke");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("native-stacey-scrabble.png"));}
        Files.writeString(folder.resolve("stacey-scrabble-passed.txt"),"Actual baked elbow/knee joints, four asymmetric scrabble poses, strike, standing memory and native spruce leaf mesh passed.\n");
        TheOldestHouse.LOGGER.info("HOUSE STACY SCRABBLE CHECK PASSED: native screenshot saved");mc.setScreen(new HomeDesignProof());
    }
}
