package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeSharkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Draws the reader's own shark (0.4.102) by its facing and pitch, its body centred on the entity. */
public final class LakeSharkRenderer extends EntityRenderer<LakeSharkEntity> {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_shark.png");
    private final LakeSharkModel model;
    public LakeSharkRenderer(EntityRendererProvider.Context context){super(context);model=new LakeSharkModel(context.bakeLayer(LakeSharkModel.LAYER));shadowRadius=0;}
    @Override public void render(LakeSharkEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180-Mth.rotLerp(partial,e.yRotO,e.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partial,e.xRotO,e.getXRot())));
        pose.scale(-1,-1,1);
        model.setupAnim(e,0,0,e.tickCount+partial,0,0);
        model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
        pose.popPose();
        super.render(e,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(LakeSharkEntity e){return TEXTURE;}
}
