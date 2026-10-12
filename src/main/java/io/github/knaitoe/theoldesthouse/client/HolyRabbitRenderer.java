package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HolyRabbit;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Uses the vanilla rabbit joints and saved body; only the synced, owner-marked scene rabbit has custom art. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class HolyRabbitRenderer extends RabbitRenderer {
    public static final ModelLayerLocation MARKS=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"holy_rabbit_marks"),"main");
    public HolyRabbitRenderer(EntityRendererProvider.Context context){super(context);addLayer(new Marks(this,context.bakeLayer(MARKS)));}
    @Override public ResourceLocation getTextureLocation(Rabbit rabbit){return HolyRabbit.owner(rabbit).isPresent()?ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_snow_rabbit.png"):super.getTextureLocation(rabbit);}
    @Override public boolean shouldRender(Rabbit rabbit,Frustum f,double x,double y,double z){
        var owner=HolyRabbit.owner(rabbit);var p=Minecraft.getInstance().player;
        return (owner.isEmpty()||p!=null&&owner.get().equals(p.getUUID()))&&super.shouldRender(rabbit,f,x,y,z);
    }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(EntityType.RABBIT,HolyRabbitRenderer::new);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(MARKS,HolyRabbitRenderer::marks);}
    /** Native geometry places eyes, nose and inner ears precisely; the painted fur can tile over every native UV face. */
    public static LayerDefinition marks(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("eyes",CubeListBuilder.create().texOffs(0,0).addBox(-2.51F,-2.9F,-4.2F,.02F,.85F,.85F).addBox(2.49F,-2.9F,-4.2F,.02F,.85F,.85F),PartPose.offset(0,16,-1));
        root.addOrReplaceChild("nose",CubeListBuilder.create().texOffs(0,0).addBox(-.48F,-2.48F,-5.51F,.96F,.75F,.02F),PartPose.offset(0,16,-1));
        root.addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(0,0).addBox(-2.25F,-8.6F,-1.01F,1.5F,4.1F,.02F),PartPose.offsetAndRotation(0,16,-1,0,-(float)Math.PI/12,0));
        root.addOrReplaceChild("left_ear",CubeListBuilder.create().texOffs(0,0).addBox(.75F,-8.6F,-1.01F,1.5F,4.1F,.02F),PartPose.offsetAndRotation(0,16,-1,0,(float)Math.PI/12,0));
        return LayerDefinition.create(mesh,16,16);
    }
    public static void markings(ModelPart root,boolean young,float yaw,float pitch,PoseStack pose,MultiBufferSource out,int light,int overlay){
        root.getAllParts().forEach(ModelPart::resetPose);for(var part:root.getAllParts().toList())if(part!=root){part.xRot=pitch*(float)Math.PI/180;part.yRot+=yaw*(float)Math.PI/180;}
        pose.pushPose();if(young){pose.scale(.56666666F,.56666666F,.56666666F);pose.translate(0,1.375,.125);}else{pose.scale(.6F,.6F,.6F);pose.translate(0,1,0);}
        root.getChild("eyes").render(pose,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/brown_terracotta.png"))),light,overlay,0xff29221c);
        var pink=out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/pink_terracotta.png")));
        for(var name:new String[]{"nose","right_ear","left_ear"})root.getChild(name).render(pose,pink,light,overlay,0xffc9b5ad);
        pose.popPose();
    }
    private static final class Marks extends RenderLayer<Rabbit,RabbitModel<Rabbit>>{
        private final ModelPart root;Marks(HolyRabbitRenderer renderer,ModelPart root){super(renderer);this.root=root;}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,Rabbit rabbit,float walk,float amount,float partial,float age,float yaw,float pitch){if(HolyRabbit.owner(rabbit).isPresent())markings(root,rabbit.isBaby(),yaw,pitch,pose,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);}
    }
}
