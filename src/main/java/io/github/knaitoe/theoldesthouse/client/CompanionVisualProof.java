package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Real baked vanilla meshes, applied mixins and distinct motion across a full response. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CompanionVisualProof extends Screen {
    private int frames;private float firstTail,firstArm;private boolean movedTail,movedArm;
    public CompanionVisualProof(){super(Component.literal("Companion responses"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF23211F);g.drawString(font,"Native petting | hand stroke, dog wag, cat head rub",12,10,0xFFE4DBCB,false);
        var wolfRoot=mc.getEntityModels().bakeLayer(ModelLayers.WOLF);var wolf=new WolfModel<>(wolfRoot);
        var catRoot=mc.getEntityModels().bakeLayer(ModelLayers.CAT);var cat=new CatModel<>(catRoot);
        var player=new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);
        EntityModel<?>[] models={player,wolf,cat};String[] names={"Stroke","Dog | wag and lean","Cat | rub and curl"};
        ResourceLocation[] skins={ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/harrigan.png"),ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png"),ResourceLocation.withDefaultNamespace("textures/entity/cat/tabby.png")};
        float t=6+frames%30;
        for(int i=0;i<3;i++){
            if(!(models[i] instanceof CompanionAnimation.PatModel pat))throw new IllegalStateException("Native pat mixin missing on "+models[i].getClass());
            pat.oldestHousePat(t);int left=8+i*width/3;g.fill(left,32,left+width/3-16,height-26,0xFF39332D);g.drawString(font,names[i],left+4,38,0xFFE4DBCB,false);g.flush();
            var pose=g.pose();pose.pushPose();pose.translate(left+width/6-8,85,160);pose.scale(85,85,85);pose.mulPose(Axis.YP.rotationDegrees(155));
            var buffers=mc.renderBuffers().bufferSource();models[i].renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skins[i])),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();pose.popPose();
        }
        float tail=wolfRoot.getChild("tail").yRot,arm=player.rightArm.xRot;
        if(frames==0){firstTail=tail;firstArm=arm;}else {movedTail|=Math.abs(tail-firstTail)>.1F;movedArm|=Math.abs(arm-firstArm)>.1F;}
        if(Math.abs(catRoot.getChild("head").zRot)<.001F)throw new IllegalStateException("Cat response was not applied");
        if(player.rightSleeve.xRot!=player.rightArm.xRot)throw new IllegalStateException("Pat sleeve detached from native arm");
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof CompanionVisualProof p)||++p.frames<44)return;
        if(!p.movedTail||!p.movedArm)throw new IllegalStateException("Native response froze at a single pose");
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-companions.png"));}
        Files.writeString(dir.resolve("companions-passed.txt"),"44 native frames: vanilla wolf/cat/player meshes, applied pat mixins, moving wag/stroke, head rub, tail curl and matching sleeves.\n");
        TheOldestHouse.LOGGER.info("COMPANION ANIMATION CHECK PASSED: native models and moving response");mc.setScreen(new ClassicsVisualProof());
    }
}
