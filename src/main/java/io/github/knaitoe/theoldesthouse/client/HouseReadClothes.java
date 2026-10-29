package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadWardrobe;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Only an actually borrowed garment is drawn. No skin pixel, face or later House double is changed. */
public final class HouseReadClothes extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation STANDARD=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"read_coat"),"main"),SLIM=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"read_coat_slim"),"main");
    private final ModelPart cloth;
    private static int registered,registeredSlim;
    public static int registeredSlimSkins(){return registeredSlim;}
    public static int registeredSkins(){return registered;}
    HouseReadClothes(PlayerRenderer parent,EntityModelSet set,boolean slim){super(parent);cloth=set.bakeLayer(slim?SLIM:STANDARD);}
    public static LayerDefinition layer(boolean slim){
        var mesh=new MeshDefinition();var r=mesh.getRoot();float width=slim?3:4;
        r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-4.35f,-.2f,-2.35f,8.7f,13,4.7f),PartPose.ZERO);
        r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(28,0).addBox(-width+.65f,-2.25f,-2.5f,width+.7f,12.5f,5),PartPose.ZERO);
        r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(28,0).mirror().addBox(-1.35f,-2.25f,-2.5f,width+.7f,12.5f,5),PartPose.ZERO);
        r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,18).addBox(-2.35f,7.8f,-2.4f,4.7f,4.5f,5),PartPose.ZERO);
        r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,18).addBox(-2.35f,7.82f,-2.38f,4.7f,4.5f,5),PartPose.ZERO);
        // 0.4.166 (owner: "try to better anticipate player movement for the legs of the clothes ... so the clothes move with the
        // legs"): the coat's skirt is two halves, each hung from its own leg's hip, so it swings with the stride instead of
        // standing still while the legs walk through it. Same place and size as the single hem it replaces, in each leg's frame.
        r.addOrReplaceChild("right_hem",CubeListBuilder.create().texOffs(0,18).addBox(-2.6f,.85f,-2.45f,4.5f,5.6f,4.9f),PartPose.ZERO);
        r.addOrReplaceChild("left_hem",CubeListBuilder.create().texOffs(0,18).mirror().addBox(-1.9f,.85f,-2.45f,4.5f,5.6f,4.9f),PartPose.ZERO);
        r.addOrReplaceChild("scarf",CubeListBuilder.create().texOffs(28,18).addBox(-4.45f,-2.2f,-4.55f,8.9f,3.7f,9.1f),PartPose.ZERO);
        r.addOrReplaceChild("belt",CubeListBuilder.create().texOffs(28,18).addBox(-4.5f,8.8f,-2.5f,9,.9f,5),PartPose.ZERO);
        // 0.4.159: reading glasses, and the veil over an obscured face, each with its own small texture.
        r.addOrReplaceChild("glasses",CubeListBuilder.create().texOffs(0,0).addBox(-4,-5,-4.65f,8,2,1),PartPose.ZERO);
        r.addOrReplaceChild("veil",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4.75f,8,8,1,new CubeDeformation(.15f)),PartPose.ZERO);
        return LayerDefinition.create(mesh,64,32);
    }
    private static void part(ModelPart parent,ModelPart garment,PoseStack pose,MultiBufferSource out,int light,ResourceLocation texture){pose.pushPose();parent.translateAndRotate(pose);garment.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY);pose.popPose();}
    public static void glasses(net.minecraft.client.model.HumanoidModel<?> body,ModelPart cloth,PoseStack pose,MultiBufferSource out,int light){part(body.head,cloth.getChild("glasses"),pose,out,light,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_read/specs.png"));}
    public static void veil(net.minecraft.client.model.HumanoidModel<?> body,ModelPart cloth,PoseStack pose,MultiBufferSource out,int light){pose.pushPose();body.head.translateAndRotate(pose);cloth.getChild("veil").render(pose,out.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_read/veil.png"))),light,OverlayTexture.NO_OVERLAY);pose.popPose();}
    public static void coat(net.minecraft.client.model.HumanoidModel<?> body,ModelPart cloth,String type,PoseStack pose,MultiBufferSource out,int light){
        var tex=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_read/"+type+".png");part(body.body,cloth.getChild("body"),pose,out,light,tex);part(body.rightArm,cloth.getChild("right_arm"),pose,out,light,tex);part(body.leftArm,cloth.getChild("left_arm"),pose,out,light,tex);
        // 0.4.169: the mesh follows the coat's cut. A short jacket has a collar and no skirt; a long coat a skirt; a belted coat both
        // its skirt and its belt.
        var garment=HouseReadWardrobe.byId(type);var cut=garment==null?HouseReadWardrobe.Cut.LONG:garment.cut();
        if(cut==HouseReadWardrobe.Cut.SHORT)part(body.body,cloth.getChild("scarf"),pose,out,light,tex);else{part(body.rightLeg,cloth.getChild("right_hem"),pose,out,light,tex);part(body.leftLeg,cloth.getChild("left_hem"),pose,out,light,tex);}
        if(cut==HouseReadWardrobe.Cut.BELTED)part(body.body,cloth.getChild("belt"),pose,out,light,tex);
    }
    public static void boots(net.minecraft.client.model.HumanoidModel<?> body,ModelPart cloth,String type,PoseStack pose,MultiBufferSource out,int light){var tex=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_read/"+type+".png");part(body.rightLeg,cloth.getChild("right_leg"),pose,out,light,tex);part(body.leftLeg,cloth.getChild("left_leg"),pose,out,light,tex);}
    public static void draw(PlayerModel<AbstractClientPlayer> body,ModelPart cloth,AbstractClientPlayer player,PoseStack pose,MultiBufferSource out,int light){
        ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST),feet=player.getItemBySlot(EquipmentSlot.FEET);
        if(chest.getItem() instanceof HouseReadRegistry.Garment garment)coat(body,cloth,garment.cloth,pose,out,light);
        if(feet.getItem() instanceof HouseReadRegistry.Garment boots)boots(body,cloth,boots.cloth,pose,out,light);
    }
    @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer p,float walk,float amount,float partial,float age,float yaw,float pitch){if(!p.isInvisible())draw(getParentModel(),cloth,p,pose,out,light);}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(STANDARD,()->layer(false));e.registerLayerDefinition(SLIM,()->layer(true));}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins())if(e.getSkin(skin) instanceof PlayerRenderer p){boolean slim=skin.toString().equalsIgnoreCase("slim");p.addLayer(new HouseReadClothes(p,e.getEntityModels(),slim));registered++;if(slim)registeredSlim++;}}
    }
}
