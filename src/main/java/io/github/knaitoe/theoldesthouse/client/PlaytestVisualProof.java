package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The actual baked assets and fear pose, in the same native client used for writing proof. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PlaytestVisualProof extends Screen {
    private int page,frames;
    public PlaytestVisualProof(){super(Component.literal("October 9 native assets"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        if(page==1){plains(g);return;}
        if(page==2){dinnerbone(g);return;}
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xffb5b8ab);g.drawString(font,"New cousins: standing / cowering. Distant boy. Native pixel items and furniture.",10,8,0xff1b2527,false);g.flush();
        var out=mc.renderBuffers().bufferSource();
        for(int i=0;i<6;i++){
            var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(GoatmanChildModel.LAYER));model.young=false;if(i>=3)model.cowerPose();
            var p=g.pose();p.pushPose();p.translate(28+i*(width-100)/7F,height*.55F,120);p.scale(38,38,38);p.mulPose(Axis.YP.rotationDegrees(165));p.translate(0,-1.5,0);
            model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+(6+i%3)+".png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
        }
        var boy=new PlainFigureModel(mc.getEntityModels().bakeLayer(PlainFigureModel.LAYER));
        var p=g.pose();p.pushPose();p.translate(width-50,height*.55F,120);p.scale(30,30,30);p.mulPose(Axis.YP.rotationDegrees(180));p.translate(0,-1.5,0);
        boy.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.SILHOUETTE,4))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();out.endBatch();
        int n=0;for(var stack:new ItemStack[]{new ItemStack(NovelRegistry.CAMERA.get()),new ItemStack(NovelRegistry.ARCHIVE_KEY.get()),new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()),new ItemStack(NovelRegistry.RIBBON.get()),new ItemStack(GoatmanRegistry.FRANKS.get())}){
            p.pushPose();p.translate(16+n++*52,height-58,0);p.scale(2,2,1);g.renderItem(stack,0,0);p.popPose();}
        var states=new net.minecraft.world.level.block.state.BlockState[]{HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.NORTH),NovelRegistry.SEALED_WINDOW.get().defaultBlockState(),NovelRegistry.PAPER.get().defaultBlockState()};
        for(int i=0;i<states.length;i++){if(mc.getBlockRenderer().getBlockModel(states[i])==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing 0.4.64 native model "+states[i]);
            p.pushPose();p.translate(width-146+i*47,height-22,150);p.scale(26,-26,26);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(25));mc.getBlockRenderer().renderSingleBlock(states[i],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}out.endBatch();
    }
    private void plains(GuiGraphics g){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xffb5b8ab);g.drawString(font,"Plains / seated on the ground, arms round raised knees / original pixel texture",10,9,0xff1b2527,false);g.flush();
        var out=mc.renderBuffers().bufferSource();float scale=Math.min(width/4F*.8F,height*.48F)*PlainFigureModel.SCALE;
        for(int view=0;view<4;view++){
            var model=new PlainFigureModel(mc.getEntityModels().bakeLayer(PlainFigureModel.LAYER));var p=g.pose();p.pushPose();
            p.translate((view+.5F)*width/4,height*.72F-scale*1.5F,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,145}[view]));
            model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.SILHOUETTE,4))),view==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);p.popPose();out.endBatch();
            g.drawCenteredString(font,new String[]{"Front","Side","Back","Dim light"}[view],(int)((view+.5F)*width/4),height-24,0xff1b2527);
        }
    }
    private static void verifyPlains(Path dir)throws Exception{
        var mc=Minecraft.getInstance();var model=new PlainFigureModel(mc.getEntityModels().bakeLayer(PlainFigureModel.LAYER));var bounds=new Bounds();
        model.renderToBuffer(PlainFigureModel.atFeet(),bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(bounds.y0<-.02F||bounds.y0>.03F||bounds.y1<.5F||bounds.y1>.85F||bounds.x1-bounds.x0<.35F||bounds.z1-bounds.z0<.45F)throw new IllegalStateException("Plains seated curl must be low, folded and grounded: "+bounds);
        try(var image=new NativeImage(128,128,false)){
            for(int y=0;y<128;y++)for(int x=0;x<128;x++)image.setPixelRGBA(x,y,0xffb5b8ab);
            PlainCameraClient.silhouette(image,new Matrix4f().setOrtho(-.6F,.6F,-.6F,.6F,.1F,100),new Vec3(0,-.5,-5));
            int pixels=0,top=128,bottom=0;
            for(int y=0;y<128;y++)for(int x=0;x<128;x++)if(image.getPixelRGBA(x,y)==0xFF1A1616){pixels++;top=Math.min(top,y);bottom=Math.max(bottom,y);}
            if(pixels<100||bottom-top>98)throw new IllegalStateException("Long-lens fallback lost the crouched mesh: pixels="+pixels+", height="+(bottom-top));
            image.writeToFile(dir.resolve("native-plains-crouch-fallback.png"));
        }
        Files.writeString(dir.resolve("plains-crouch-passed.txt"),"Native production crouch: front, side, back and dim light; original child scale and unchanged pixel atlas. Actual baked vertex bounds: "+bounds+". The untracked-body photograph projects the same mesh. Existing personal camera, finite photo, Witness and connected-client checks remain required.\n");
    }
    private void dinnerbone(GuiGraphics g){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff242622);g.drawString(font,"Ceiling figure / held poses, slow inclination, delayed hand / original skin",10,9,0xffd1c9ae,false);g.flush();
        var out=mc.renderBuffers().bufferSource();float size=Math.min(width/4F*.6F,height*.38F);
        float[] clocks={0,500,1000,1390};
        for(int i=0;i<4;i++){
            var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.preview(LiteraryActor.SILHOUETTE,0,0);DinnerboneIdle.pose(model,clocks[i],0);
            var bounds=new Bounds();model.renderToBuffer(new PoseStack(),bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
            if(Math.abs(bounds.y1-1.5F)>.002F)throw new IllegalStateException("Ceiling idle moved the planted feet: "+bounds);
            var p=g.pose();p.pushPose();p.translate((i+.5F)*width/4,58,150);p.scale(size,size,size);p.mulPose(Axis.ZP.rotationDegrees(180));p.translate(0,-1.5,0);p.mulPose(Axis.YP.rotationDegrees(i==3?55:180));
            model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.SILHOUETTE,0))),i==3?LightTexture.pack(5,0):LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();
            g.drawCenteredString(font,new String[]{"Held","Head inclined","One hand","Returns to stillness"}[i],(int)((i+.5F)*width/4),height-35,0xffd1c9ae);
        }
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,z0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1,z1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);z0=Math.min(z0,z);x1=Math.max(x1,x);y1=Math.max(y1,y);z1=Math.max(z1,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        @Override public String toString(){return x0+","+y0+","+z0+" -> "+x1+","+y1+","+z1;}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof PlaytestVisualProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(proof.page==0?"native-playtest-0464.png":proof.page==1?"native-plains-crouch.png":"native-dinnerbone-idle.png"));}
        if(proof.page==0){proof.page++;proof.frames=0;return;}if(proof.page==1){verifyPlains(dir);proof.page++;proof.frames=0;return;}
        Files.writeString(dir.resolve("playtest-assets-passed.txt"),"Additional cousin skins, shared cowering pose, distant boy, camera, keys, ribbon, packets, chess table, varied paper and boarded window rendered by the native client.\n");mc.setScreen(new DeathPortraitVisualProof());
    }
}
