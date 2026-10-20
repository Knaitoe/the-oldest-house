package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class HotelPhotoRenderer implements BlockEntityRenderer<HotelPhotoBlockEntity> {
    public HotelPhotoRenderer(BlockEntityRendererProvider.Context context){}
    @Override public boolean shouldRenderOffScreen(HotelPhotoBlockEntity photo){return true;}
    @Override public void render(HotelPhotoBlockEntity photo,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(photo.sitter()==null)return;var texture=DeathPortraitClient.texture(photo.sitter(),true);if(texture!=null)front(pose,buffers,photo.getBlockState().getValue(HotelPropBlock.FACING),texture,light);
    }
    public static void front(PoseStack pose,MultiBufferSource buffers,Direction facing,ResourceLocation texture,int light){
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-((facing.get2DDataValue()+2)%4)*90));pose.translate(-.5,0,-.5);
        var out=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));var p=pose.last();float lo=1/16F,hi=31/16F,z=.799F;
        out.addVertex(p,hi,lo,z).setColor(-1).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,0,-1);
        out.addVertex(p,lo,lo,z).setColor(-1).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,0,-1);
        out.addVertex(p,lo,hi,z).setColor(-1).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,0,-1);
        out.addVertex(p,hi,hi,z).setColor(-1).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,0,-1);
        pose.popPose();
    }
}
