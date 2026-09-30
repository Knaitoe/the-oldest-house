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
    public static final ResourceLocation MATERIALS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/finale_materials.png");
    private static final ModelLayerLocation WITNESS=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"finale_witness"),"main");
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(MinotaurModel.LAYER,MinotaurModel::createBodyLayer);event.registerLayerDefinition(WITNESS,WitnessModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(FinaleRegistry.MINOTAUR.get(),MinotaurRenderer::new);event.registerEntityRenderer(FinaleRegistry.WITNESS.get(),WitnessRenderer::new);}
    private static final class MinotaurRenderer extends MobRenderer<MinotaurEntity,MinotaurModel>{
        MinotaurRenderer(EntityRendererProvider.Context context){super(context,new MinotaurModel(context.bakeLayer(MinotaurModel.LAYER)),.9F);}
        @Override public ResourceLocation getTextureLocation(MinotaurEntity entity){return MATERIALS;}
    }
    private static final class WitnessRenderer extends MobRenderer<FinaleWitness,WitnessModel>{
        WitnessRenderer(EntityRendererProvider.Context context){super(context,new WitnessModel(context.bakeLayer(WITNESS)),.3F);}
        @Override public ResourceLocation getTextureLocation(FinaleWitness entity){return MATERIALS;}
    }
    private static final class WitnessModel extends HierarchicalModel<FinaleWitness>{
        final ModelPart root,head;WitnessModel(ModelPart model){root=model.getChild("root");head=root.getChild("head");}
        @Override public ModelPart root(){return root;}
        static LayerDefinition layer(){MeshDefinition mesh=new MeshDefinition();var r=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offset(0,24,0));
            r.addOrReplaceChild("coat",CubeListBuilder.create().texOffs(128,256).addBox(-4,-1,-2.5F,8,15,5),PartPose.offsetAndRotation(0,-21,0,.12F,0,0));
            var h=r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,256).addBox(-3,-6,-3,6,7,6).addBox(-.6F,-3,-4,1.2F,2,1),PartPose.offset(0,-22,-1));
            h.addOrReplaceChild("hair",CubeListBuilder.create().texOffs(0,384).addBox(-3.1F,-6.2F,-2.8F,6.2F,1,5.8F).addBox(-3.2F,-5,2.7F,6.4F,5,1),PartPose.ZERO);
            h.addOrReplaceChild("eyes",CubeListBuilder.create().texOffs(128,384).addBox(-2,-3.4F,-3.1F,1,.5F,.2F).addBox(1,-3.4F,-3.1F,1,.5F,.2F),PartPose.ZERO);
            for(int side:new int[]{-1,1}){r.addOrReplaceChild("arm"+side,CubeListBuilder.create().texOffs(128,256).addBox(-1.5F,0,-1.5F,3,10,3).texOffs(0,256).addBox(-1.5F,10,-1.5F,3,2,3),PartPose.offsetAndRotation(side*5,-20,0,-.15F,0,-side*.08F));
                r.addOrReplaceChild("leg"+side,CubeListBuilder.create().texOffs(128,256).addBox(-1.5F,0,-1.5F,3,7,3).texOffs(128,384).addBox(-1.5F,7,-2.5F,3,2,4),PartPose.offset(side*2,-9,1));}
            return LayerDefinition.create(mesh,256,512);}
        @Override public void setupAnim(FinaleWitness e,float w,float a,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);head.yRot=yaw*.01F;head.xRot=.15F+pitch*.006F;}
    }
}
