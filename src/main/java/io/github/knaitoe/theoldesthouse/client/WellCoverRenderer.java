package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Vector3f;

/** Native wood, iron and wool sprites; a supported plank lid with two hinges and an articulated closing hand. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WellCoverRenderer implements BlockEntityRenderer<WellCoverBlockEntity> {
    public WellCoverRenderer(BlockEntityRendererProvider.Context context){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(NovelRegistry.WELL_COVER_ENTITY.get(),WellCoverRenderer::new);}
    private static void piece(PoseStack p,MultiBufferSource b,int light,int overlay,Block block,double x,double y,double z,float w,float h,float d){
        var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/"+BuiltInRegistries.BLOCK.getKey(block).getPath()));
        var out=b.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        p.pushPose();p.translate(x,y,z);
        // Crop the native atlas at sixteen pixels per block, including narrow planks and hands.
        // Scaling a full block mesh would squeeze all sixteen pixels into each small part.
        float u=offset(x,w),v=offset(y,h),t=offset(z,d);
        face(p,out,sprite,light,overlay,.8F,0,0,-1,new float[][]{{w,0,0,u+w,v+h},{0,0,0,u,v+h},{0,h,0,u,v},{w,h,0,u+w,v}});
        face(p,out,sprite,light,overlay,.8F,0,0,1,new float[][]{{0,0,d,u,v+h},{w,0,d,u+w,v+h},{w,h,d,u+w,v},{0,h,d,u,v}});
        face(p,out,sprite,light,overlay,.6F,-1,0,0,new float[][]{{0,0,0,t,v+h},{0,0,d,t+d,v+h},{0,h,d,t+d,v},{0,h,0,t,v}});
        face(p,out,sprite,light,overlay,.6F,1,0,0,new float[][]{{w,0,d,t+d,v+h},{w,0,0,t,v+h},{w,h,0,t,v},{w,h,d,t+d,v}});
        face(p,out,sprite,light,overlay,1,0,1,0,new float[][]{{0,h,0,u,t},{0,h,d,u,t+d},{w,h,d,u+w,t+d},{w,h,0,u+w,t}});
        face(p,out,sprite,light,overlay,.5F,0,-1,0,new float[][]{{0,0,d,u,t+d},{0,0,0,u,t},{w,0,0,u+w,t},{w,0,d,u+w,t+d}});
        p.popPose();
    }
    private static float offset(double coordinate,float size){return (float)Math.max(0,Math.min(1-size,coordinate-Math.floor(coordinate)));}
    private static void face(PoseStack p,VertexConsumer out,TextureAtlasSprite sprite,int light,int overlay,float shade,float nx,float ny,float nz,float[][] vertices){
        var normal=new Vector3f(nx,ny,nz).mul(p.last().normal()).normalize();
        int c=(int)(shade*255);
        for(var vertex:vertices)out.addVertex(p.last().pose(),vertex[0],vertex[1],vertex[2]).setColor(c,c,c,255)
                .setUv(sprite.getU0()+(sprite.getU1()-sprite.getU0())*vertex[3],sprite.getV0()+(sprite.getV1()-sprite.getV0())*vertex[4])
                .setOverlay(overlay).setLight(light).setNormal(normal.x(),normal.y(),normal.z());
    }
    @Override public void render(WellCoverBlockEntity lid,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        draw(lid.closure(partial),lid.progress()>0,p,buffers,light,overlay);
    }
    public static void draw(float closed,boolean figure,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        float angle=(1-closed)*90;
        // The hinge stays fixed at the north edge; the lid lowers into the real collision plate.
        p.pushPose();p.translate(0,1,0);p.mulPose(Axis.XP.rotationDegrees(-angle));
        for(int i=0;i<5;i++)piece(p,buffers,light,overlay,Blocks.SPRUCE_PLANKS,i*.2,-.1875,0,.195F,.18F,1);
        for(float x:new float[]{.12F,.76F})piece(p,buffers,light,overlay,Blocks.DARK_OAK_PLANKS,x,-.215,.06,.12F,.04F,.87F);
        for(float x:new float[]{.08F,.84F})piece(p,buffers,light,overlay,Blocks.IRON_BLOCK,x,-.19,0,.08F,.205F,.16F);
        piece(p,buffers,light,overlay,Blocks.IRON_BLOCK,.42,.015,.78,.16F,.04F,.04F);p.popPose();
        if(!figure)return;
        // An anonymous hooded shape leans over the mouth. Hands follow the descending plank;
        // they are seen from the bottom before the cover cuts off the sky, never a flat pasted image.
        float lean=.18F+.23F*closed;
        piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,.28,2.0,-.45,.16F,.38F,.18F);
        piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,.56,2.0,-.45,.16F,.38F,.18F);
        p.pushPose();p.translate(.5,2.30,-.10);p.mulPose(Axis.XP.rotationDegrees(lean*57.29578F));
        piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,-.28,0,-.15,.56F,.50F,.30F);
        piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,-.22,.45,-.12,.44F,.42F,.44F);p.popPose();
        double handY=1+Math.sin(Math.toRadians(angle))*.76,handZ=Math.cos(Math.toRadians(angle))*.76;
        for(double x:new double[]{.18,.68}){
            p.pushPose();p.translate(x,2.70,-.04);
            double dy=handY-2.70,dz=handZ+.04,length=Math.sqrt(dy*dy+dz*dz);
            p.mulPose(Axis.XP.rotation((float)-Math.atan2(dy,dz)));
            piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,-.07,-.065,0,.14F,.13F,(float)length*.53F);
            piece(p,buffers,light,overlay,Blocks.BLACK_WOOL,-.055,-.052,length*.51,.11F,.105F,(float)length*.49F);p.popPose();
            piece(p,buffers,light,overlay,Blocks.GRAY_WOOL,x-.05,handY-.04,handZ-.03,.10F,.07F,.11F);
        }
    }
    @Override public int getViewDistance(){return 64;}
    @Override public boolean shouldRenderOffScreen(WellCoverBlockEntity lid){return true;}
}
