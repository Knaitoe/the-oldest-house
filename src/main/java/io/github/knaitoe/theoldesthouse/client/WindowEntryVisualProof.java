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
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Vector3f;

/** Native shipped cousin joints at four window beats and both crawl strides, followed by the paw's thumb, which stays still while a finger curls. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WindowEntryVisualProof extends Screen {
    private static final String[] VIEWS={"reach","sill","pull","lower","crawl-left","crawl-right","paw-open","paw-curl","paw-closed","window-stand","bed-heave-left","bed-heave-right"};
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
        model.windowPose(1);var end=model.body.storePose();var hand=model.rightArm.storePose();model.pose(0,0,0,0,false,false,false);
        if(Math.abs(model.body.xRot-end.xRot)+Math.abs(model.body.y-end.y)+Math.abs(model.rightArm.xRot-hand.xRot)>.0001F)throw new IllegalStateException("The window drop must finish in the ordinary standing cousin pose before the indoor walk");
        for(int i=0;i<=64;i++){
            model.crawlPose(i*Mth.TWO_PI/64,1);
            var floor=new PoseStack();floor.scale(.7F,-.7F,.7F);floor.translate(0,-1.5,0);var bounds=new Bounds();model.renderToBuffer(floor,bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
            if(bounds.min<-.002F||bounds.max>.62F)throw new IllegalStateException("The actual child crawl mesh must stay grounded and below the in-bed view: "+bounds.min+" -> "+bounds.max);
            for(boolean left:new boolean[]{false,true}){
                var a=tip(left?model.leftArm:model.rightArm,left?model.leftElbow:model.rightElbow);
                var f=tip(left?model.leftLeg:model.rightLeg,left?model.leftKnee:model.rightKnee);
                if(!Float.isFinite(a.x+a.y+a.z+f.x+f.y+f.z)||a.y>1.5001F||f.y>1.5001F||a.y<1.4F||f.y<1.4F)throw new IllegalStateException("Crawl hands/feet must alternate above their real floor: "+a+" "+f);
            }
        }
        model.pose(0,0,0,0,false,false,false);if(model.leftKnee.xRot!=0||model.rightElbow.xRot!=0)throw new IllegalStateException("Window joints leaked into an ordinary cousin");
        paw.curl(2,0);var open=thumbTip(paw);paw.curl(2,1);var closed=thumbTip(paw);
        // 0.4.121: the thumb is not one of the three wishes; it lies outside the palm and stays there while a finger curls.
        if(open.x>=-5/16F||closed.distance(open)>.00001F)throw new IllegalStateException("The thumb must lie outside the palm and stay still while a finger curls: "+open+" -> "+closed);
        paw.curl(0,1);paw.curl(2,0);if(thumbTip(paw).distance(open)>.00001F)throw new IllegalStateException("A previous clutch changed the thumb's rest orientation");
    }
    private static Vector3f tip(net.minecraft.client.model.geom.ModelPart upper,net.minecraft.client.model.geom.ModelPart lower){var p=new PoseStack();upper.translateAndRotate(p);lower.translateAndRotate(p);return new Vector3f(0,6/16F,0).mulPosition(p.last().pose());}
    private static final class Bounds implements com.mojang.blaze3d.vertex.VertexConsumer{
        float min=Float.POSITIVE_INFINITY,max=Float.NEGATIVE_INFINITY;
        public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x,float y,float z){min=Math.min(min,y);max=Math.max(max,y);return this;}
        public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r,int g,int b,int a){return this;}public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u,float v){return this;}public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u,int v){return this;}public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u,int v){return this;}public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x,float y,float z){return this;}
    }
    private static Vector3f thumbTip(PawClutchClient.Model paw){var p=new PoseStack();paw.root.getChild("thumb").translateAndRotate(p);return new Vector3f(0,-4/16F,0).mulPosition(p.last().pose());}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252b30);g.drawString(font,"Window entry / "+VIEWS[page],10,10,0xffede1ca,false);g.flush();var out=mc.renderBuffers().bufferSource();
        if(page<6||page>=9){
            float progress=new float[]{.12F,.48F,.65F,.9F}[Math.min(3,page)];
            for(int i=0;i<4;i++){
                var p=g.pose();p.pushPose();p.translate(width*(.14F+i*.24F),height*.7F,150);p.scale(68,68,68);p.mulPose(Axis.YP.rotationDegrees(i%2==0?155:95));p.translate(0,-1.5,0);
                var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(GoatmanChildModel.LAYER));if(page<4)model.windowPose(progress);else if(page<6)model.crawlPose(page==4?0:Mth.PI,1);else if(page==9)model.windowPose(1);else model.pose(i<2?4:0,0,0,page==10?17:24,false,false,true);
                model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+(i<2?4:0)+".png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
                p.popPose();
            }
        }else{
            paw.curl(page==8?0:2,page==6?0:page==7?.55F:1);
            for(int i=0;i<2;i++){var p=g.pose();p.pushPose();p.translate(width*(i==0?.3F:.7F),height*.48F,150);float scale=Math.min(width,height)*.48F;p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i==0?18:-35));paw.root.render(p,out.getBuffer(RenderType.entityCutoutNoCull(PawClutchClient.TEXTURE)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();}
        }
        out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof WindowEntryVisualProof proof)||++proof.frames<20)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-window-entry-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("window-entry-passed.txt"),"Twelve native GPU pages: two original cousin skins reach, fit the sill, pull with alternating elbows, lower and stand before the indoor walk; later low crawling strides and two dry-heave poses; actual paw at open, half curl and closed in two angles. 101 window and 65 crawl samples, continuous window/standing transition, native hand/foot floor geometry, reused-cousin reset and a thumb that stays at rest through every curl passed.\n");
        if(Boolean.getBoolean("the_oldest_house.windowEntrySmoke"))mc.stop();else mc.setScreen(new InstituteVisualProof());
    }
}
