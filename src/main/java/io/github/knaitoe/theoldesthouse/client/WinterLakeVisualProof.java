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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * Winter Lake (0.4.104) on the production code: the lake's ice and what stands on it with her ribbon, the four boys from
 * the shallows in their winter things, and her hanging upside down under a sheet of black ice. Checks confirm every block
 * texture, the ribbon's item model and each boy's skin are present; they report after every view is saved.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WinterLakeVisualProof extends Screen {
    private static final String[] VIEWS={"the lake's ice, cattails, snowballs and her ribbon","Bud, Hank, Clay and Lonnie, that winter","her, under the black ice"};
    private final List<String> problems=new ArrayList<>();
    private int page,frames;
    public WinterLakeVisualProof(){super(Component.literal("Winter Lake"));}
    private static ResourceLocation texture(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,path);}

    @Override protected void init(){
        problems.clear();var mc=Minecraft.getInstance();
        for(var name:List.of("winter_black_ice","winter_snow_ice","winter_snow_ice_side","winter_snow_ice_bottom","winter_cracked","winter_split","winter_cattails","winter_snowballs"))
            if(mc.getResourceManager().getResource(texture("textures/block/"+name+".png")).isEmpty())problems.add("a lake texture is missing: "+name);
        if(mc.getResourceManager().getResource(texture("textures/item/winter_ribbon.png")).isEmpty())problems.add("her ribbon's texture is missing");
        var model=mc.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(WinterLakeRegistry.RIBBON.get()),null,null,0);
        if(model==mc.getModelManager().getMissingModel())problems.add("her ribbon has no item model");
        for(var boy:WinterLake.BOYS){var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,boy.phase());if(mc.getResourceManager().getResource(skin).isEmpty())problems.add("a boy's skin is missing: "+skin);}
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        g.fill(0,0,width,height,page==2?0xff04070c:0xff1a2230);g.drawString(font,"Winter Lake / "+VIEWS[page],10,9,0xfff2ece0,false);
        if(page==0){ice(g);return;}
        if(page==1){boys(g);return;}
        under(g);
    }
    /** Each kind of the lake's block, turned three-quarters, in a row; her ribbon beside them. */
    private void ice(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        var kinds=WinterPropBlock.Kind.values();float cell=Math.min(width/(kinds.length+1.5F),height*.5F),scale=cell*.6F;
        for(int i=0;i<kinds.length;i++){
            p.pushPose();p.translate(width*.5F+(i-kinds.length/2F)*cell,height*.55F,400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(150));p.translate(-.5,-.5,-.5);
            mc.getBlockRenderer().renderSingleBlock(WinterPropBlock.of(kinds[i]),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
        p.pushPose();p.translate(width*.5F+(kinds.length/2F)*cell-cell*.3F,height*.55F-cell*.3F,400);p.scale(cell/32,cell/32,1);
        g.renderItem(new net.minecraft.world.item.ItemStack(WinterLakeRegistry.RIBBON.get()),0,0);p.popPose();
    }
    /** The four boys, side on, at the size they are on the bank. */
    private void boys(GuiGraphics g){
        var mc=Minecraft.getInstance();float cell=width/(WinterLake.BOYS.size()+1F);
        for(int i=0;i<WinterLake.BOYS.size();i++){
            var boy=WinterLake.BOYS.get(i);var m=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));m.preview(LiteraryActor.STRANGER,boy.phase(),i*9);
            draw(g,m,LiteraryRenderers.skin(LiteraryActor.STRANGER,boy.phase()),cell*(i+1),height*.55F,cell*.8F,height*.6F,false);
        }
    }
    /** Her hunting body, turned over as the renderer turns her, under a sheet of black ice. */
    private void under(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();float cell=Math.min(width/9F,height*.18F);
        for(int i=-4;i<=4;i++){
            p.pushPose();p.translate(width*.5F+i*cell,height*.3F,300);p.scale(cell,-cell,cell);p.mulPose(Axis.XP.rotationDegrees(14));p.translate(-.5,-.5,-.5);
            mc.getBlockRenderer().renderSingleBlock(WinterPropBlock.of(Math.abs(i)<3?WinterPropBlock.Kind.BLACK_ICE:WinterPropBlock.Kind.SNOW_ICE),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
        var witch=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));witch.young=false;witch.huntPose(0,0,0,LakeWitchEntity.STALK,false);
        draw(g,witch,texture("textures/entity/lake_witch.png"),width*.5F,height*.55F,width*.4F,height*.35F,true);
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);x1=Math.max(x1,x);y1=Math.max(y1,y);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
    private static void draw(GuiGraphics g,EntityModel<?> model,ResourceLocation skin,float cx,float cy,float width,float height,boolean flipped){
        var p=g.pose();var out=Minecraft.getInstance().renderBuffers().bufferSource();var sample=new PoseStack();sample.mulPose(Axis.YP.rotationDegrees(110));
        var bounds=new Bounds();model.renderToBuffer(sample,bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        float scale=Math.min(width/Math.max(.01F,bounds.x1-bounds.x0),height/Math.max(.01F,bounds.y1-bounds.y0));
        p.pushPose();p.translate(cx,cy,180);p.scale(scale,scale,scale);if(flipped)p.mulPose(Axis.ZP.rotationDegrees(180));p.translate(-(bounds.x0+bounds.x1)/2,-(bounds.y0+bounds.y1)/2,0);p.mulPose(Axis.YP.rotationDegrees(110));
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof WinterLakeVisualProof proof)||++proof.frames<8)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-winter-"+proof.page+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        if(!proof.problems.isEmpty())throw new IllegalStateException("WINTER LAKE VIEW CHECK FAILED: "+String.join("; ",proof.problems));
        Files.writeString(dir.resolve("winter-lake-passed.txt"),"Three native Winter Lake views: the lake's ice, cattails, snowballs and her ribbon, the four boys in their winter things, and her upside down under the black ice; every lake texture, the ribbon's item model and each boy's skin are present.\n");
        TheOldestHouse.LOGGER.info("WINTER LAKE VIEW CHECK PASSED: three native views");
        PhoneVisualProof.start();
    }
}
