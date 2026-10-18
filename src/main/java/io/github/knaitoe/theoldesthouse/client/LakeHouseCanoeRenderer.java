package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseCanoe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/** Draws the lake house's canoe the way the vanilla boat renderer draws a boat: the same lift, rocking and water mask. */
public final class LakeHouseCanoeRenderer extends EntityRenderer<LakeHouseCanoe> {
    /** Fresh paint, chipped in the culvert, scraped down to the wood. */
    private static final ResourceLocation[] PAINT={paint("lake_house_canoe"),paint("lake_house_canoe_chipped"),paint("lake_house_canoe_scraped")};
    private final LakeHouseCanoeModel<LakeHouseCanoe> model;
    public LakeHouseCanoeRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.7F;model=new LakeHouseCanoeModel<>(context.bakeLayer(LakeHouseCanoeModel.LAYER));}
    private static ResourceLocation paint(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}

    @Override public void render(LakeHouseCanoe canoe,float yaw,float partialTicks,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        pose.translate(0,.375F,0);
        pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
        float hurt=canoe.getHurtTime()-partialTicks,damage=Math.max(0,canoe.getDamage()-partialTicks);
        if(hurt>0)pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt)*hurt*damage/10F*canoe.getHurtDir()));
        float bubble=canoe.getBubbleAngle(partialTicks);
        if(!Mth.equal(bubble,0))pose.mulPose(new Quaternionf().setAngleAxis(bubble*Mth.DEG_TO_RAD,1,0,1));
        pose.scale(-1,-1,1);
        pose.mulPose(Axis.YP.rotationDegrees(90));
        model.setupAnim(canoe,partialTicks,0,-.1F,0,0);
        model.renderToBuffer(pose,buffers.getBuffer(model.renderType(getTextureLocation(canoe))),light,OverlayTexture.NO_OVERLAY,-1);
        if(!canoe.isUnderWater())model.waterPatch().render(pose,buffers.getBuffer(RenderType.waterMask()),light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(canoe,yaw,partialTicks,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(LakeHouseCanoe canoe){return PAINT[Mth.clamp(canoe.chips(),0,PAINT.length-1)];}
}
