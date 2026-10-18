package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeCanoeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * The authored canoes: an unpainted one is still drawn as the vanilla boat (the cave's and the phone's), a painted one
 * (Movie Night, 0.4.102) as a real canoe in its own colour, with the lake house canoe's hull, paddles and water mask.
 */
public final class LakeCanoeRenderer extends EntityRenderer<LakeCanoeEntity> {
    /** Paint 1 to 5, drawn by tools/draw_movie_night_art.py on the canoe's 128 x 64 layout. */
    static final ResourceLocation[] PAINT={paint("red"),paint("blue"),paint("yellow"),paint("white"),paint("teal")};
    private final BoatRenderer boat;
    private final LakeHouseCanoeModel<LakeCanoeEntity> model;
    public LakeCanoeRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.7F;boat=new BoatRenderer(context,false);model=new LakeHouseCanoeModel<>(context.bakeLayer(LakeHouseCanoeModel.LAYER));}
    private static ResourceLocation paint(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/movie_canoe_"+name+".png");}

    @Override public void render(LakeCanoeEntity canoe,float yaw,float partialTicks,PoseStack pose,MultiBufferSource buffers,int light){
        if(canoe.paint()<=0){boat.render(canoe,yaw,partialTicks,pose,buffers,light);return;}
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
    @Override public ResourceLocation getTextureLocation(LakeCanoeEntity canoe){return canoe.paint()<=0?boat.getTextureLocation(canoe):PAINT[Mth.clamp(canoe.paint()-1,0,PAINT.length-1)];}
}
