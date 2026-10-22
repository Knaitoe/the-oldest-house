package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
import net.neoforged.neoforge.client.event.*;
import org.joml.Vector3f;

/** Native shipped joints at the entry's four physical beats, followed by the actual opposable thumb. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WindowEntryVisualProof extends Screen {
    private static final String[] VIEWS={"reach","sill","pull","recover","paw-open","paw-curl","paw-closed"};
    private static boolean launched;
    private int page,frames;
    private final PawClutchClient.Model paw=new PawClutchClient.Model();
    public WindowEntryVisualProof(){super(Component.literal("Window entry and paw"));}
    @SubscribeEvent public static void start(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.windowEntrySmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new WindowEntryVisualProof());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override protected void init(){
        var mc=Minecraft.getInstance();var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(GoatmanChildModel.LAYER));
        for(int i=0;i<=100;i++){
            model.windowPose(i/100F);
            for(var part:new net.minecraft.client.model.geom.ModelPart[]{model.head,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg,model.rightElbow,model.leftElbow,model.rightKnee,model.leftKnee})
                if(!Float.isFinite(part.xRot+part.yRot+part.zRot+part.x+part.y+part.z))throw new IllegalStateException("Non-finite window joint at "+i);
        }
        model.windowPose(.5F);if(model.leftKnee.xRot<2.5F||model.rightElbow.xRot>-.5F)throw new IllegalStateException("The sill pull must fold actual knees and elbows");
        model.pose(0,0,0,0,false,false,false);if(model.leftKnee.xRot!=0||model.rightElbow.xRot!=0)throw new IllegalStateException("Window joints leaked into an ordinary cousin");
        paw.curl(2,0);var open=thumbTip(paw);paw.curl(2,1);var closed=thumbTip(paw);
        if(open.x>=-5/16F||closed.x<=-3/16F||closed.z<=1.5F/16)throw new IllegalStateException("Thumb must begin outside the palm and oppose across its front: "+open+" -> "+closed);
        paw.curl(0,1);paw.curl(2,0);if(thumbTip(paw).distance(open)>.00001F)throw new IllegalStateException("A previous clutch changed the thumb's rest orientation");
    }
    private static Vector3f thumbTip(PawClutchClient.Model paw){var p=new PoseStack();paw.root.getChild("thumb").translateAndRotate(p);return new Vector3f(0,-4/16F,0).mulPosition(p.last().pose());}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252b30);g.drawString(font,"Window entry / "+VIEWS[page],10,10,0xffede1ca,false);g.flush();var out=mc.renderBuffers().bufferSource();
        if(page<4){
            float progress=new float[]{.12F,.38F,.61F,.9F}[page];
            for(int i=0;i<4;i++){
                var p=g.pose();p.pushPose();p.translate(width*(.14F+i*.24F),height*.7F,150);p.scale(68,68,68);p.mulPose(Axis.YP.rotationDegrees(i%2==0?155:95));p.translate(0,-1.5,0);
                if(i<2){var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(GoatmanChildModel.LAYER));model.windowPose(progress);model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_4.png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);}
                else{var model=new GoatmanFigureModel<>(mc.getEntityModels().bakeLayer(GoatmanFigureModel.LAYER));model.windowPose(progress);model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(GoatmanFigureRenderer.SKIN)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);}
                p.popPose();
            }
        }else{
            paw.curl(page==6?0:2,page==4?0:page==5?.55F:1);
            for(int i=0;i<2;i++){var p=g.pose();p.pushPose();p.translate(width*(i==0?.3F:.7F),height*.48F,150);float scale=Math.min(width,height)*.48F;p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i==0?18:-35));paw.root.render(p,out.getBuffer(RenderType.entityCutoutNoCull(PawClutchClient.TEXTURE)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();}
        }
        out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof WindowEntryVisualProof proof)||++proof.frames<20)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-window-entry-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("window-entry-passed.txt"),"Seven native GPU pages: child and monster reach, sill fit, pull and standing recovery in two angles; actual paw at open, half curl and closed in two angles. 101 native child joint samples, reused-cousin reset and thumb opposition/rest geometry passed.\n");
        if(Boolean.getBoolean("the_oldest_house.windowEntrySmoke"))mc.stop();else mc.setScreen(new InstituteVisualProof());
    }
}
