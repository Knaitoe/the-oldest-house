package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NovelCreatureRenderers {
    private static final ModelLayerLocation VULTURE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vulture"),"main");
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(VULTURE,VultureModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(NovelRegistry.VULTURE.get(),VultureRenderer::new);e.registerEntityRenderer(NovelRegistry.ACTOR.get(),ActorRenderer::new);}
    private static final class VultureRenderer extends MobRenderer<NovelVulture,VultureModel>{VultureRenderer(EntityRendererProvider.Context c){super(c,new VultureModel(c.bakeLayer(VULTURE)),0);}
        @Override public ResourceLocation getTextureLocation(NovelVulture e){return FinaleClientEvents.MATERIALS;}}
    private static final class VultureModel extends HierarchicalModel<NovelVulture>{private final ModelPart root,left,right;VultureModel(ModelPart r){root=r.getChild("root");left=root.getChild("left");right=root.getChild("right");}
        @Override public ModelPart root(){return root;}
        static LayerDefinition layer(){var m=new MeshDefinition();var r=m.getRoot().addOrReplaceChild("root",CubeListBuilder.create().texOffs(128,128).addBox(-2,-3,-4,4,6,10),PartPose.offset(0,20,0));
            r.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(0,256).addBox(-1,-1,-7,2,3,4).texOffs(128,0).addBox(-.7F,0,-9,1.4F,1,2),PartPose.ZERO);
            r.addOrReplaceChild("left",CubeListBuilder.create().texOffs(0,128).addBox(0,-.5F,-3,18,1,7),PartPose.offset(2,-1,0));r.addOrReplaceChild("right",CubeListBuilder.create().texOffs(0,128).addBox(-18,-.5F,-3,18,1,7),PartPose.offset(-2,-1,0));
            r.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(128,128).addBox(-3,-.5F,5,6,1,7),PartPose.ZERO);return LayerDefinition.create(m,256,512);}
        @Override public void setupAnim(NovelVulture e,float walk,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.zRot=.18F;left.zRot=(float)Math.sin(age*.025)*.025F;right.zRot=-left.zRot;}}
    private static final class ActorRenderer extends MobRenderer<NovelActor,PlayerModel<NovelActor>>{
        ActorRenderer(EntityRendererProvider.Context c){super(c,new PlayerModel<>(c.bakeLayer(ModelLayers.PLAYER),false),.3F);}
        @Override public boolean shouldRender(NovelActor e,Frustum f,double x,double y,double z){var p=Minecraft.getInstance().player;return p!=null&&e.owner().filter(p.getUUID()::equals).isPresent()&&super.shouldRender(e,f,x,y,z);}
        @Override public ResourceLocation getTextureLocation(NovelActor e){var mc=Minecraft.getInstance();var info=mc.getConnection()==null?null:e.owner().map(mc.getConnection()::getPlayerInfo).orElse(null);return info!=null?info.getSkin().texture():ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_2.png");}
        @Override protected boolean shouldShowName(NovelActor e){return false;}
    }
}
