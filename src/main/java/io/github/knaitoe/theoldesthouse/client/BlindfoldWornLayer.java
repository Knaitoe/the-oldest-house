package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** A fitted eye band, side wraps and a knot in the actual player's head coordinate system. */
public final class BlindfoldWornLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"worn_blindfold"),"main");
    private static final ResourceLocation CLOTH=ResourceLocation.withDefaultNamespace("textures/block/black_wool.png");
    private final ModelPart cloth;
    private static final java.util.Set<String> SKINS=new java.util.HashSet<>();
    public static int registeredSkinCount(){return SKINS.size();}
    public BlindfoldWornLayer(PlayerRenderer parent,EntityModelSet models){super(parent);cloth=models.bakeLayer(LAYER);}
    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("band",CubeListBuilder.create().texOffs(0,0)
                .addBox(-4.6F,-5.5F,-4.65F,9.2F,3,.35F)
                .addBox(-4.6F,-5.5F,4.3F,9.2F,3,.35F)
                .addBox(-4.65F,-5.5F,-4.3F,.35F,3,8.6F)
                .addBox(4.3F,-5.5F,-4.3F,.35F,3,8.6F),PartPose.ZERO);
        root.addOrReplaceChild("knot",CubeListBuilder.create().texOffs(0,0)
                .addBox(-1,-4.9F,4.6F,2,1.7F,.7F)
                .addBox(-1.1F,-3.4F,4.65F,.8F,3.6F,.2F)
                .addBox(.3F,-3.4F,4.65F,.8F,2.8F,.2F),PartPose.ZERO);
        return LayerDefinition.create(mesh,32,32);
    }
    public static boolean visible(ItemStack head){return head.is(LabyrinthRegistry.BLINDFOLD.get());}
    public static void draw(ModelPart head,ModelPart cloth,PoseStack pose,MultiBufferSource out,int light){
        pose.pushPose();head.translateAndRotate(pose);
        cloth.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(CLOTH)),light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
        if(!visible(player.getItemBySlot(EquipmentSlot.HEAD))||player.isInvisible())return;
        draw(getParentModel().head,cloth,pose,out,light);
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,BlindfoldWornLayer::layer);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player){player.addLayer(new BlindfoldWornLayer(player,e.getEntityModels()));SKINS.add(skin.toString());}}
        }
    }
}
