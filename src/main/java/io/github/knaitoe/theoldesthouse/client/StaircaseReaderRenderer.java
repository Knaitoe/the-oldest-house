package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class StaircaseReaderRenderer extends MobRenderer<StaircaseReaderEcho,PlayerModel<StaircaseReaderEcho>> {
    private final PlayerModel<StaircaseReaderEcho> standard,slim;
    public StaircaseReaderRenderer(EntityRendererProvider.Context c){super(c,new ReaderModel(c.bakeLayer(ModelLayers.PLAYER),false),.3F);standard=model;slim=new ReaderModel(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
    private PlayerSkin skin(StaircaseReaderEcho e){var mc=Minecraft.getInstance();var info=mc.getConnection()==null?null:e.owner().map(mc.getConnection()::getPlayerInfo).orElse(null);return info!=null?info.getSkin():DefaultPlayerSkin.get(e.owner().orElse(new java.util.UUID(0,0)));}
    @Override public ResourceLocation getTextureLocation(StaircaseReaderEcho e){return skin(e).texture();}
    @Override public void render(StaircaseReaderEcho e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack poses,net.minecraft.client.renderer.MultiBufferSource buffers,int light){model=skin(e).model()==PlayerSkin.Model.SLIM?slim:standard;super.render(e,yaw,partial,poses,buffers,light);}
    private static final class ReaderModel extends PlayerModel<StaircaseReaderEcho>{
        ReaderModel(net.minecraft.client.model.geom.ModelPart part,boolean slim){super(part,slim);}
        @Override public void setupAnim(StaircaseReaderEcho e,float walk,float speed,float time,float yaw,float pitch){super.setupAnim(e,0,0,time,yaw,pitch);rightArm.xRot=-.85F;leftArm.xRot=-.85F;rightArm.yRot=-.25F;leftArm.yRot=.25F;rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);head.xRot=.22F;hat.copyFrom(head);}
    }
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StaircaseLeakRegistry.PROP.get(),net.minecraft.client.renderer.RenderType.cutout()));}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(StaircaseLeakRegistry.READER.get(),StaircaseReaderRenderer::new);}
}
