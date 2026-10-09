package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** A reader who gave an arm is drawn without it, for every viewer and in first person: a short, bandaged stump at the shoulder. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BodyLossClient {
    public static final ModelLayerLocation STUMP=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"arm_stump"),"main");
    public static final ModelLayerLocation BANDAGE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"arm_stump"),"bandage");
    public static final ResourceLocation BANDAGE_TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/arm_stump_bandage.png");
    private BodyLossClient(){}
    /**
     * After every pose of a player's model (third person, first person and the two-handed map alike): the missing arm and
     * its sleeve are not drawn. Zero scale also travels with the pose into armour, so no chestplate sleeve hangs in the air.
     */
    public static void pose(PlayerModel<?> model,LivingEntity entity){
        HumanoidArm missing=entity instanceof Player p?BodyLoss.missing(p):null;
        hide(model.leftArm,model.leftSleeve,missing==HumanoidArm.LEFT);hide(model.rightArm,model.rightSleeve,missing==HumanoidArm.RIGHT);
    }
    public static void hide(ModelPart arm,ModelPart sleeve,boolean gone){
        if(gone){arm.visible=false;sleeve.visible=false;scale(arm,0);scale(sleeve,0);}
        else{if(arm.xScale==0)scale(arm,1);if(sleeve.xScale==0)scale(sleeve,1);}
    }
    private static void scale(ModelPart part,float s){part.xScale=s;part.yScale=s;part.zScale=s;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){BodyLoss.clientClear();}

    /** Upper arm only: the same skin and sleeve pixels as the shoulder, four high instead of twelve. */
    public static LayerDefinition stumpLayer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        for(boolean slim:new boolean[]{false,true})for(boolean left:new boolean[]{false,true}){
            int w=slim?3:4;float x0=left?-1:(slim?-2:-3);String key=(slim?"slim_":"wide_")+(left?"left":"right");
            root.addOrReplaceChild(key+"_skin",CubeListBuilder.create().texOffs(left?32:40,left?48:16).addBox(x0,-2,-2,w,4,4),PartPose.ZERO);
            root.addOrReplaceChild(key+"_sleeve",CubeListBuilder.create().texOffs(left?48:40,left?48:32).addBox(x0,-2,-2,w,4,4,new CubeDeformation(.25F)),PartPose.ZERO);
        }
        return LayerDefinition.create(mesh,64,64);
    }
    /** Linen wound over the end of the stump, darker where it has soaked through. */
    public static LayerDefinition bandageLayer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        for(boolean slim:new boolean[]{false,true})for(boolean left:new boolean[]{false,true}){
            int w=slim?3:4;float x0=left?-1:(slim?-2:-3);
            root.addOrReplaceChild((slim?"slim_":"wide_")+(left?"left":"right"),CubeListBuilder.create().texOffs(0,slim?8:0).addBox(x0,.5F,-2,w,2,4,new CubeDeformation(.38F)),PartPose.ZERO);
        }
        return LayerDefinition.create(mesh,32,16);
    }
    public static final class StumpLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        private final ModelPart skins,bandages;
        public StumpLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,EntityModelSet models){super(parent);skins=models.bakeLayer(STUMP);bandages=models.bakeLayer(BANDAGE);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            var missing=BodyLoss.missing(player);if(missing==null||player.isInvisible())return;
            boolean left=missing==HumanoidArm.LEFT,slim=player.getSkin().model()==PlayerSkin.Model.SLIM;String key=(slim?"slim_":"wide_")+(left?"left":"right");
            var model=getParentModel();var arm=left?model.leftArm:model.rightArm;int overlay=LivingEntityRenderer.getOverlayCoords(player,0);
            var skin=skins.getChild(key+"_skin");var sleeve=skins.getChild(key+"_sleeve");var bandage=bandages.getChild(key);
            for(var part:new ModelPart[]{skin,sleeve,bandage}){
                // The shoulder still turns with the body; what is left of the arm hardly swings.
                part.copyFrom(arm);part.xScale=1;part.yScale=1;part.zScale=1;part.xRot*=.3F;part.yRot*=.3F;part.zRot=left?-.07F:.07F;part.visible=true;
            }
            skin.render(pose,out.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),light,overlay);
            if(player.isModelPartShown(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE:net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE))
                sleeve.render(pose,out.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),light,overlay);
            bandage.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(BANDAGE_TEXTURE)),light,overlay);
        }
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(STUMP,BodyLossClient::stumpLayer);e.registerLayerDefinition(BANDAGE,BodyLossClient::bandageLayer);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new StumpLayer(player,e.getEntityModels()));}
        }
    }
}
