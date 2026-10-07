package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.SceneReview;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** The original costume stands keep their native identity and equipment. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CostumeRenderers {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/costume_scarecrow.png");
    private static CostumeRenderer renderer;
    private CostumeRenderers(){}
    @SubscribeEvent public static void render(RenderLivingEvent.Pre<?,?> e){
        if(renderer==null||e.getRenderer()==renderer||!(e.getEntity() instanceof ArmorStand stand)||!SceneReview.costume(stand))return;
        e.setCanceled(true);renderer.render(stand,stand.getYRot(),e.getPartialTick(),e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());
    }
    public static final class CostumeModel extends PlayerModel<ArmorStand>{
        public CostumeModel(net.minecraft.client.model.geom.ModelPart part){super(part,false);}
        @Override public void setupAnim(ArmorStand e,float walk,float amount,float age,float yaw,float pitch){
            super.setupAnim(e,0,0,age,0,0);leftArm.zRot=-1.05F;rightArm.zRot=1.05F;head.zRot=.07F;
            hat.visible=false;jacket.visible=false;leftSleeve.visible=false;rightSleeve.visible=false;leftPants.visible=false;rightPants.visible=false;
        }
    }
    private static final class CostumeRenderer extends LivingEntityRenderer<ArmorStand,CostumeModel>{
        CostumeRenderer(EntityRendererProvider.Context c){super(c,new CostumeModel(c.bakeLayer(ModelLayers.PLAYER)),0);}
        @Override public ResourceLocation getTextureLocation(ArmorStand s){return TEXTURE;}
        @Override protected boolean isBodyVisible(ArmorStand s){return true;}
        @Override protected boolean shouldShowName(ArmorStand s){return false;}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration{
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){renderer=new CostumeRenderer(e.getContext());}
    }
}
