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

/**
 * The reader in this room's glass. Since 0.4.161 the glass is a true plane mirror ({@link HouseReadReflection}); the still
 * image below, the reader square to the glass and a step back from it, remains for the clothes proof's mirror page.
 * Turning the glass shows its wooden back.
 */
public final class HouseReadMirror implements BlockEntityRenderer<HouseReadPropBlock.Fitting> {
    /** The glass's front face in the block, and how far the reflection is pressed into it. */
    static final float GLASS=.09f,FLAT=.15f;
    /** Half the drawn height of a player: a plane mirror shows a person at half their size, whatever the distance. */
    static final float HALF=.5f*.9375f;
    private final PlayerModel<net.minecraft.client.player.AbstractClientPlayer> standard,slim;
    private final net.minecraft.client.model.geom.ModelPart regularCoat,slimCoat;
    HouseReadMirror(BlockEntityRendererProvider.Context ctx){standard=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER),false);slim=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM),true);regularCoat=ctx.bakeLayer(HouseReadClothes.STANDARD);slimCoat=ctx.bakeLayer(HouseReadClothes.SLIM);}
    @Override public void render(HouseReadPropBlock.Fitting be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var state=be.getBlockState();
        // 0.4.168: the kitchen's tin, drawing and fridge carry their own colours.
        if(HouseReadKitchenClient.draws(state.getValue(HouseReadPropBlock.PART))){HouseReadKitchenClient.render(be,pose,out,light);return;}
        // 0.4.185: the wall clocks' hands move over their painted faces; the kitchen's says a little after four, the rest are office clocks.
        if(state.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.WALL_CLOCK){ClockHands.wall(be,pose,out,light);return;}
        if(state.getValue(HouseReadPropBlock.PART)!=HouseReadPropBlock.Part.MIRROR||state.getValue(HouseReadPropBlock.TURNED))return;
        // 0.4.161: a true plane mirror. The glass asks to be reflected and, once it is the nearest glass in front of the camera,
        // shows the room and the people in it as the glass would, the reader as the House's version of them.
        var glass=new HouseReadReflection.Glass(be.getBlockPos(),state.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING));HouseReadReflection.want(glass.at(),glass.facing());
        if(HouseReadReflection.showing(glass.at()))HouseReadReflection.drawGlass(glass,pose,out);
    }
    /** As a reader standing square to the glass, a step back from it, would see themselves: for still images of the mirror. */
    public static void drawImage(PlayerModel<net.minecraft.client.player.AbstractClientPlayer> body,net.minecraft.client.model.geom.ModelPart cloth,net.minecraft.resources.ResourceLocation skin,String coat,boolean boots,PoseStack pose,MultiBufferSource out,int light){
        reflect(body,cloth,skin,coat,boots,0,.81f,180,1,false,pose,out,light);
    }
    /**
     * Draws the reader as if standing in front of the glass with the given body yaw, then reflects them through the glass's
     * plane and presses the image into it. {@code lateral} is their offset along the glass, {@code feetY} where their feet fall
     * on it.
     */
    public static void reflect(PlayerModel<net.minecraft.client.player.AbstractClientPlayer> body,net.minecraft.client.model.geom.ModelPart cloth,net.minecraft.resources.ResourceLocation skin,String coat,boolean boots,float lateral,float feetY,float bodyYaw,float scale,boolean house,PoseStack pose,MultiBufferSource out,int light){
        pose.pushPose();pose.translate(.5+lateral,feetY,GLASS);pose.scale(1,1,-FLAT);
        // As the native living renderer turns and sizes a player, at half their height.
        pose.mulPose(Axis.YP.rotationDegrees(180-bodyYaw));float k=HALF*scale;pose.scale(-k,-k,k);pose.translate(0,-1.501f,0);
        body.renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY);
        if(coat!=null)HouseReadClothes.coat(body,cloth,coat,pose,out,light);if(boots)HouseReadClothes.boots(body,cloth,io.github.knaitoe.theoldesthouse.labyrinth.HouseReadWardrobe.bootsFor(coat),pose,out,light);
        // 0.4.159: the House's version of the reader: the glasses an older read gives them, or no face where the glass did not agree.
        if(house)HouseReadLookClient.dress(body,cloth,"",pose,out,light);pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(HouseReadPropBlock.Fitting be){return be.getBlockState().getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {@SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(HouseReadRegistry.FITTING_ENTITY.get(),HouseReadMirror::new);e.registerEntityRenderer(HouseReadRegistry.ROUTINE_ECHO.get(),RoutineRenderer::new);}}
    private static final class RoutineRenderer extends net.minecraft.client.renderer.entity.DisplayRenderer.ItemDisplayRenderer {
        RoutineRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context){super(context);}
    }
}
