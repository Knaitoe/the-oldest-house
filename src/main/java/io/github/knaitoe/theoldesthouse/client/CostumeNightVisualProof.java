package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * Costume Night (0.4.103) on the production code: the party's props with the power on, the same props once the lights
 * go, and five entrants on all fours beside her. Checks confirm the crawl stands about a block and a quarter high with
 * the torso level, and that every prop texture and costume skin is present; they report after every view is saved.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CostumeNightVisualProof extends Screen {
    private static final String[] VIEWS={"the party's props, lit","the props once the lights go","the crawl, and her among them"};
    private static final int[] CRAWLERS={23,25,26,29,31};
    private final List<String> problems=new ArrayList<>();
    private int page,frames;
    public CostumeNightVisualProof(){super(Component.literal("Costume Night"));}
    private static ResourceLocation texture(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,path);}

    @Override protected void init(){
        problems.clear();var mc=Minecraft.getInstance();
        var crawl=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));crawl.previewCrawl(LiteraryActor.STRANGER,26,0);
        var b=new Bounds();crawl.renderToBuffer(new PoseStack(),b,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(b.y1-b.y0<.95F||b.y1-b.y0>1.45F)problems.add("the crawl is not on all fours: "+b);
        if(b.z1-b.z0<.9F)problems.add("the crawl's torso does not lie level: "+b);
        for(var name:List.of("costume_bulbs","costume_bulbs_off","costume_radio_front","costume_radio_front_off","costume_tub_top","costume_pail_face","costume_candy","costume_board_top","costume_board_bottom"))
            if(mc.getResourceManager().getResource(texture("textures/block/"+name+".png")).isEmpty())problems.add("a prop texture is missing: "+name);
        for(int phase=21;phase<=33;phase++){var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,phase);if(mc.getResourceManager().getResource(skin).isEmpty())problems.add("a costume skin is missing: "+skin);}
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,z0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1,z1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);z0=Math.min(z0,z);x1=Math.max(x1,x);y1=Math.max(y1,y);z1=Math.max(z1,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        @Override public String toString(){return String.format(Locale.ROOT,"x %.2f..%.2f y %.2f..%.2f z %.2f..%.2f",x0,x1,y0,y1,z0,z1);}
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        g.fill(0,0,width,height,page==1?0xff07080b:0xff141820);g.drawString(font,"Costume Night / "+VIEWS[page],10,9,0xfff2ece0,false);
        if(page<2){props(g,page==0);return;}
        crawlers(g);
    }
    /** Each prop, turned three-quarters, in a row; the bulbs and the radio show their lit or dark state. */
    private void props(GuiGraphics g,boolean lit){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        var kinds=CostumePropBlock.Kind.values();float cell=Math.min(width/(kinds.length+.5F),height*.5F),scale=cell*.6F;
        for(int i=0;i<kinds.length;i++){
            p.pushPose();p.translate(width*.5F+(i-(kinds.length-1)/2F)*cell,height*.55F,400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(150));p.translate(-.5,-.5,-.5);
            mc.getBlockRenderer().renderSingleBlock(CostumePropBlock.of(kinds[i],Direction.NORTH).setValue(CostumePropBlock.LIT,lit),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
    }
    /** Five entrants doing the crawl, and her, side on. */
    private void crawlers(GuiGraphics g){
        var mc=Minecraft.getInstance();float cell=width/(CRAWLERS.length+1.5F);
        for(int i=0;i<CRAWLERS.length;i++){
            var m=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));m.previewCrawl(LiteraryActor.STRANGER,CRAWLERS[i],i*7);
            draw(g,m,LiteraryRenderers.skin(LiteraryActor.STRANGER,CRAWLERS[i]),cell*(i+.75F),height*.55F,cell*.9F,height*.5F);
        }
        var witch=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));witch.young=false;witch.huntPose(0,0,0,LakeWitchEntity.STALK,false);
        draw(g,witch,texture("textures/entity/lake_witch.png"),cell*(CRAWLERS.length+.75F),height*.55F,cell*.9F,height*.5F);
    }
    private static void draw(GuiGraphics g,EntityModel<?> model,ResourceLocation skin,float cx,float cy,float width,float height){
        var p=g.pose();var out=Minecraft.getInstance().renderBuffers().bufferSource();var sample=new PoseStack();sample.mulPose(Axis.YP.rotationDegrees(110));
        var bounds=new Bounds();model.renderToBuffer(sample,bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        float scale=Math.min(width/Math.max(.01F,bounds.x1-bounds.x0),height/Math.max(.01F,bounds.y1-bounds.y0));
        p.pushPose();p.translate(cx,cy,180);p.scale(scale,scale,scale);p.translate(-(bounds.x0+bounds.x1)/2,-(bounds.y0+bounds.y1)/2,0);p.mulPose(Axis.YP.rotationDegrees(110));
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof CostumeNightVisualProof proof)||++proof.frames<8)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-costume-"+proof.page+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        if(!proof.problems.isEmpty())throw new IllegalStateException("COSTUME NIGHT VIEW CHECK FAILED: "+String.join("; ",proof.problems));
        Files.writeString(dir.resolve("costume-night-passed.txt"),"Three native Costume Night views: the party's props lit and dark, and five entrants on all fours beside her; the crawl is about a block and a quarter high with its torso level, and every prop texture and costume skin is present.\n");
        TheOldestHouse.LOGGER.info("COSTUME NIGHT VIEW CHECK PASSED: three native views");
        mc.setScreen(new WinterLakeVisualProof());
    }
}
