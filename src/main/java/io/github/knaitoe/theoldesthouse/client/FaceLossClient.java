package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.FaceLoss;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.410: a reader whose mouth the machine took is drawn, for every viewer and in every mirror, with the lower face smooth: one flat
 * patch of their own skin, taken from the back of their hand, over everything below the eyes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FaceLossClient {
    private FaceLossClient(){}
    /** The pixel the patch is coloured from: the back of the right hand, the last row of the arm's front face. */
    static final float U=45.5F/64,V=31.5F/64;
    /** The patch, in the head's own pixels: the whole width of the face, from just under the eyes to the chin, a hair in front of it. */
    static final float X0=-4,X1=4,Y0=-3,Y1=0,Z=-4.04F;
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){FaceLoss.clientClear();}

    public static final class Smooth extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        public Smooth(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(!FaceLoss.faceless(player)||player.isInvisible())return;
            var head=getParentModel().head;if(!head.visible)return;
            pose.pushPose();head.translateAndRotate(pose);
            var buffer=out.getBuffer(RenderType.entityCutoutNoCull(player.getSkin().texture()));int overlay=LivingEntityRenderer.getOverlayCoords(player,0);var last=pose.last();
            float[][] corners={{X0,Y0},{X0,Y1},{X1,Y1},{X1,Y0}};
            for(var c:corners)buffer.addVertex(last,c[0]/16,c[1]/16,Z/16).setColor(-1).setUv(U,V).setOverlay(overlay).setLight(light).setNormal(last,0,0,-1);
            pose.popPose();
        }
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new Smooth(player));}
        }
    }
}
