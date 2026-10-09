package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Native wood, iron and wool sprites; a supported plank lid with two hinges and an articulated closing hand. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WellCoverRenderer implements BlockEntityRenderer<WellCoverBlockEntity> {
    public WellCoverRenderer(BlockEntityRendererProvider.Context context){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(NovelRegistry.WELL_COVER_ENTITY.get(),WellCoverRenderer::new);}
    private static void piece(PoseStack p,MultiBufferSource b,int light,int overlay,Block block,double x,double y,double z,float w,float h,float d){
        p.pushPose();p.translate(x,y,z);p.scale(w,h,d);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),p,b,light,overlay);p.popPose();
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
}
