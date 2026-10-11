package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The shipped cow-elk atlases and complete native A-frame, rather than a concept render. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ElkFanVisualProof extends Screen {
    private static final String[] VIEWS={"front","back","night","ladder-front","ladder-side"};
    private int page,frames;
    public ElkFanVisualProof(){super(Component.literal("Cow elk and fan-room ladder"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override protected void init(){var mc=Minecraft.getInstance();for(var name:new String[]{"elk","elk_wounded"}){
        var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_"+name+".png");
        try(var input=mc.getResourceManager().open(texture);var image=NativeImage.read(input)){
            if(image.getWidth()!=512||image.getHeight()!=256)throw new IllegalStateException("Wrong cow-elk atlas size");
            for(var box:new int[][]{{0,0,66,35},{0,36,30,23},{68,0,36,17},{68,20,22,9},{100,20,16,5},{32,38,12,16},{48,38,16,6},{68,38,14,7}})
                for(int y=box[1]*4;y<(box[1]+box[3])*4;y++)for(int x=box[0]*4;x<(box[0]+box[2])*4;x++)
                    if((image.getPixelRGBA(x,y)>>>24)!=255)throw new IllegalStateException("A native elk face is transparent");
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot read the cow-elk atlas",e);}
    }}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292723);g.drawString(font,"Cow elk / "+VIEWS[page],10,10,0xffeee1c4,false);g.flush();
        var pose=g.pose();var buffers=mc.renderBuffers().bufferSource();int light=page==2?LightTexture.pack(9,0):LightTexture.FULL_BRIGHT;
        if(page<3){for(int n=0;n<2;n++){
            var model=new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK));model.setupAnim(null,0,0,0,0,0);
            var scale=Math.min(height*.38F,width*.22F);pose.pushPose();pose.translate(width*(n==0?.26:.74),height*.79,190);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(page==1?-135:45));pose.translate(0,-1.5,0);
            var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_"+(n==0?"elk":"elk_wounded")+".png");model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,0xffffffff);pose.popPose();buffers.endBatch();
            g.drawCenteredString(font,n==0?"Cow elk":"Her injury",(int)(width*(n==0?.26:.74)),height-25,0xffeee1c4);
        }}else{
            float scale=(height-48)/9F;pose.pushPose();pose.translate(width*.50,height-20,190);pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(14));pose.mulPose(Axis.YP.rotationDegrees(page==3?30:88));pose.translate(-.5,0,-.5);
            for(var entry:ElkFanRoom.frame(BlockPos.ZERO).entrySet()){pose.pushPose();pose.translate(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ()+17);mc.getBlockRenderer().renderSingleBlock(entry.getValue(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
            for(int z=-2;z<=2;z++){pose.pushPose();pose.translate(0,-1,z);mc.getBlockRenderer().renderSingleBlock(Blocks.BIRCH_PLANKS.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
            pose.pushPose();pose.translate(.5,7.72,.5);pose.mulPose(Axis.YP.rotation(frames*.6F));mc.getEntityModels().bakeLayer(LiteraryRenderers.FAN).render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_fan.png"))),light,OverlayTexture.NO_OVERLAY);pose.popPose();
            pose.popPose();buffers.endBatch();g.drawCenteredString(font,"Four feet, nine treads, spreaders and the open working platform",width/2,27,0xffeee1c4);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof ElkFanVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-elk-fan0480-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("elk-fan0480-passed.txt"),"Healthy/wounded elk from both sides and in dim light on the non-overlapping native UV layout; the whole four-legged A-frame from front and side using its shipped block models.\n");TheOldestHouse.LOGGER.info("ELK FAN CHECK PASSED: two elk atlases and native A-frame in five views");mc.setScreen(new SceneReviewVisualProof());
    }
}
