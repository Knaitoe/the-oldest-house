package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListSack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.705, The list, part 2 (docs/THE_LIST_0_4_705.md): the feed sack on the head, in the head's own space, at the skin's own density
 * (its faces eight texels across, as the head's are). The bag stands half a pixel off the head all round; its two top corners poke up
 * as a sack's do; it is tied off with twine at the neck and bunches below the tie. The eyeholes are cut out of the texture, so through
 * them is whatever face is under it. Drawn by tools/draw_the_list_art.py.
 */
public final class TheListSackLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_sack"),"main");
    private static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/"+name+".png");}
    private static final ResourceLocation SACK=tex("sack"),TORN=tex("sack_torn"),DIRT=tex("sack_dirt");
    /** 0.4.710: blood on the sack, three ways (thrown at it once, twice, soaked). */
    private static final ResourceLocation[] BLOOD={null,tex("sack_blood_1"),tex("sack_blood_2"),tex("sack_blood_3")};
    private final ModelPart sack;
    public TheListSackLayer(PlayerRenderer parent,EntityModelSet models){super(parent);sack=models.bakeLayer(LAYER);}
    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("bag",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.55F)),PartPose.ZERO);
        root.addOrReplaceChild("skirt",CubeListBuilder.create().texOffs(0,16).addBox(-4,0,-4,8,1,8,new CubeDeformation(.75F,.2F,.75F)),PartPose.ZERO);
        root.addOrReplaceChild("tie",CubeListBuilder.create().texOffs(32,16).addBox(-4,-1,-4,8,1,8,new CubeDeformation(.62F,-.2F,.62F)),PartPose.ZERO);
        root.addOrReplaceChild("left_corner",CubeListBuilder.create().texOffs(32,0).addBox(-1,-1,-1,2,2,2),PartPose.offsetAndRotation(4.2F,-8.25F,.3F,0,0,.5F));
        root.addOrReplaceChild("right_corner",CubeListBuilder.create().texOffs(40,0).addBox(-1,-1,-1,2,2,2),PartPose.offsetAndRotation(-4.2F,-8.25F,-.2F,0,0,-.45F));
        return LayerDefinition.create(mesh,64,32);
    }
    /** Draws the sack on a posed head. */
    public static void draw(ModelPart head,ModelPart sack,PoseStack pose,MultiBufferSource out,int light){draw(head,sack,pose,out,light,0);}
    /**
     * 0.4.710: with what has got on it (TheListSack.marks): torn through, it is a sack with the tear cut out of it; dirt and blood are
     * drawn over it on the same faces.
     */
    public static void draw(ModelPart head,ModelPart sack,PoseStack pose,MultiBufferSource out,int light,int marks){
        pose.pushPose();head.translateAndRotate(pose);
        sack.render(pose,out.getBuffer(RenderType.entityCutoutNoCull((marks&TheListSack.TORN)!=0?TORN:SACK)),light,OverlayTexture.NO_OVERLAY);
        if((marks&TheListSack.DIRT)!=0)sack.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(DIRT)),light,OverlayTexture.NO_OVERLAY);
        int blood=marks&TheListSack.BLOOD;
        if(blood>0)sack.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(BLOOD[blood])),light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
        if(!TheListSack.worn(player)||player.isInvisible())return;
        draw(getParentModel().head,sack,pose,out,light,TheListSack.marks(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)));
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        private Registration(){}
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,TheListSackLayer::layer);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new TheListSackLayer(player,e.getEntityModels()));}
        }
    }
}
