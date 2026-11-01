package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.WorkPrint;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.203, Work print: the film's monster, worn. Stitched sacking over the whole body, a little proud of it all round; a hood
 * with no face cut into it; arms two pixels longer than the wearer's, so the gloves' fingers hang a joint too far. Drawn over
 * anyone wearing the costume in the chest slot, from tools/draw_work_print_art.py's sheet on the player layout.
 */
public final class WorkPrintCostumeLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"work_print_costume"),"main");
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/work_print_costume.png");
    private final HumanoidModel<AbstractClientPlayer> suit;
    WorkPrintCostumeLayer(PlayerRenderer parent,EntityModelSet set){super(parent);suit=new HumanoidModel<>(set.bakeLayer(LAYER));}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(new CubeDeformation(.4F),0);var r=mesh.getRoot();
        r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.6F)),PartPose.ZERO);
        r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-3,-2,-2,4,14,4,new CubeDeformation(.3F)),PartPose.offset(-5,2,0));
        r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(40,16).mirror().addBox(-1,-2,-2,4,14,4,new CubeDeformation(.3F)),PartPose.offset(5,2,0));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer p,float walk,float amount,float partial,float age,float yaw,float pitch){
        if(p.isInvisible()||!WorkPrint.wearing(p))return;
        getParentModel().copyPropertiesTo(suit);suit.hat.visible=false;
        suit.renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,OverlayTexture.NO_OVERLAY,-1);
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,WorkPrintCostumeLayer::layer);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins())if(e.getSkin(skin) instanceof PlayerRenderer p)p.addLayer(new WorkPrintCostumeLayer(p,e.getEntityModels()));}
    }
}
