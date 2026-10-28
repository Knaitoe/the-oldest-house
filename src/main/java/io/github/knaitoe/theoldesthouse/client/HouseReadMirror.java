package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** A full-length own-skin image in this room's glass; turning the glass shows its wooden back. */
public final class HouseReadMirror implements BlockEntityRenderer<HouseReadPropBlock.Fitting> {
    private final PlayerModel<net.minecraft.client.player.AbstractClientPlayer> standard,slim;
    private final net.minecraft.client.model.geom.ModelPart regularCoat,slimCoat;
    HouseReadMirror(BlockEntityRendererProvider.Context ctx){standard=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER),false);slim=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM),true);regularCoat=ctx.bakeLayer(HouseReadClothes.STANDARD);slimCoat=ctx.bakeLayer(HouseReadClothes.SLIM);}
    @Override public void render(HouseReadPropBlock.Fitting be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var state=be.getBlockState();if(state.getValue(HouseReadPropBlock.PART)!=HouseReadPropBlock.Part.MIRROR||state.getValue(HouseReadPropBlock.TURNED))return;
        var mc=Minecraft.getInstance();var player=mc.player;if(player==null||player.isSpectator()||player.isInvisible())return;boolean narrow=player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;var body=narrow?slim:standard;
        body.setAllVisible(true);body.young=false;body.crouching=player.isCrouching();body.swimAmount=0;body.riding=false;body.attackTime=0;body.setupAnim(player,0,0,player.tickCount+partial,0,Math.max(-20,Math.min(20,player.getXRot())));
        String coat=player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof HouseReadRegistry.Garment g?g.cloth:null;
        drawImage(body,narrow?slimCoat:regularCoat,player.getSkin().texture(),coat,player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof HouseReadRegistry.Garment,pose,out,light);
    }
    /** Native model coordinates are already in sixteenths; the portrait fills this two-block frame. */
    public static void drawImage(PlayerModel<net.minecraft.client.player.AbstractClientPlayer> body,net.minecraft.client.model.geom.ModelPart cloth,net.minecraft.resources.ResourceLocation skin,String coat,boolean boots,PoseStack pose,MultiBufferSource out,int light){
        pose.pushPose();pose.translate(.5,1.22,.09);pose.mulPose(Axis.YP.rotationDegrees(180));pose.scale(.8f,-.8f,.15f);
        body.renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY);
        if(coat!=null)HouseReadClothes.coat(body,cloth,coat,pose,out,light);if(boots)HouseReadClothes.boots(body,cloth,pose,out,light);pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(HouseReadPropBlock.Fitting be){return be.getBlockState().getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {@SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(HouseReadRegistry.FITTING_ENTITY.get(),HouseReadMirror::new);e.registerEntityRenderer(HouseReadRegistry.ROUTINE_ECHO.get(),RoutineRenderer::new);}}
    private static final class RoutineRenderer extends net.minecraft.client.renderer.entity.DisplayRenderer.ItemDisplayRenderer {
        RoutineRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context){super(context);}
    }
}
