package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FinaleClientEvents {
    public static final ResourceLocation MATERIALS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/minotaur_materials.png");
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(MinotaurModel.LAYER,MinotaurModel::createBodyLayer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(FinaleRegistry.MINOTAUR.get(),MinotaurRenderer::new);event.registerEntityRenderer(FinaleRegistry.WITNESS.get(),WitnessRenderer::new);}
    private static final class MinotaurRenderer extends MobRenderer<MinotaurEntity,MinotaurModel>{
        private final CagedBoyRenderer boy;
        MinotaurRenderer(EntityRendererProvider.Context context){super(context,new MinotaurModel(context.bakeLayer(MinotaurModel.LAYER)),.9F);boy=new CagedBoyRenderer(context);}
        @Override public ResourceLocation getTextureLocation(MinotaurEntity entity){return MATERIALS;}
        @Override public void render(MinotaurEntity e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack poses,net.minecraft.client.renderer.MultiBufferSource buffers,int light){if(e.childAppearance())boy.render(e,yaw,partial,poses,buffers,light);else super.render(e,yaw,partial,poses,buffers,light);}
    }
    private static final class CagedBoyRenderer extends MobRenderer<MinotaurEntity,net.minecraft.client.model.PlayerModel<MinotaurEntity>>{
        CagedBoyRenderer(EntityRendererProvider.Context context){super(context,new net.minecraft.client.model.PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER),false),.2F);}
        @Override public ResourceLocation getTextureLocation(MinotaurEntity e){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_boy.png");}
        @Override protected void scale(MinotaurEntity e,com.mojang.blaze3d.vertex.PoseStack poses,float partial){poses.scale(.7F,.7F,.7F);}
    }
    private static final class WitnessRenderer extends MobRenderer<FinaleWitness,WitnessModel>{
        WitnessRenderer(EntityRendererProvider.Context context){super(context,new WitnessModel(context.bakeLayer(ModelLayers.PLAYER)),.3F);}
        @Override public ResourceLocation getTextureLocation(FinaleWitness entity){return HouseHumanRenderers.WITNESS;}
        @Override protected boolean shouldShowName(FinaleWitness entity){return false;}
    }
    private static final class WitnessModel extends net.minecraft.client.model.PlayerModel<FinaleWitness>{
        WitnessModel(ModelPart model){super(model,false);}
        @Override public void setupAnim(FinaleWitness e,float w,float a,float age,float yaw,float pitch){
            super.setupAnim(e,0,0,age,yaw,pitch);body.xRot=.12F;head.xRot+=.15F;
            rightArm.xRot=-.15F;leftArm.xRot=-.15F;jacket.copyFrom(body);hat.copyFrom(head);rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);
        }
    }
}
