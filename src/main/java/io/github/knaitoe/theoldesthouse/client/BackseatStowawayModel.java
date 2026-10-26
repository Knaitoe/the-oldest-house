package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The one body hidden against the rear floorboard; no separate chase or approach clock. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BackseatStowawayModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"backseat_stowaway"),"main");
    public static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/backseat_stowaway.png");
    public BackseatStowawayModel(ModelPart root){super(root);hat.visible=false;}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,()->net.minecraft.client.model.geom.builders.LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64));}
    public void pose(float rise){
        HumanoidMotion.reset(this);young=false;riding=false;hat.visible=false;attackTime=0;crouching=false;
        body.xRot=.16F*rise;head.xRot=.12F*rise;
        rightArm.xRot=-2.18F*rise;leftArm.xRot=-1.72F*rise;
        rightArm.zRot=-.13F*rise;leftArm.zRot=.2F*rise;
        rightLeg.xRot=-1.32F*rise;leftLeg.xRot=-1.5F*rise;
        rightLeg.yRot=.22F*rise;leftLeg.yRot=-.28F*rise;
        HumanoidMotion.connect(this);
    }
    /** Floorboard -> driver's shoulder in four ticks; both poses share this physical car-space origin. */
    public void draw(PoseStack p,MultiBufferSource out,int light,int overlay,float rise){
        float q=Mth.clamp(rise,0,1);q=q*q*(3-2*q);pose(q);
        p.pushPose();p.translate(-.625,.565,1.1-.85*q);p.scale(.7F,-Mth.lerp(q,.055F,.8F),.7F);p.translate(0,-1.5,0);
        renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(SKIN)),light,overlay,-1);p.popPose();
    }
}
