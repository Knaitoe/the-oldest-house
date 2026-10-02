package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseHumanRenderers {
    public static final ResourceLocation HOLLOWAY=texture("holloway"),HARRIGAN=texture("harrigan"),HARRIGAN_DEAD=texture("harrigan_dead");
    public static final ResourceLocation WITNESS=texture("finale_witness");
    private static HarriganRenderer harrigan;
    private HouseHumanRenderers(){}
    public static boolean ready(){return harrigan!=null;}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}
    @SubscribeEvent public static void renderHarrigan(RenderLivingEvent.Pre<?,?> event){
        if(harrigan==null||event.getRenderer()==harrigan||!(event.getEntity() instanceof ArmorStand body)||HarriganAppearance.variant(body)==0)return;
        event.setCanceled(true);harrigan.render(body,body.getYRot(),event.getPartialTick(),event.getPoseStack(),event.getMultiBufferSource(),event.getPackedLight());
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(NovelRegistry.HUMAN.get(),HumanRenderer::new);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event){harrigan=new HarriganRenderer(event.getContext());}
    }
    public static final class HumanModel extends PlayerModel<HouseHuman> {
        public HumanModel(net.minecraft.client.model.geom.ModelPart part){super(part,false);}
        @Override public void setupAnim(HouseHuman human,float walk,float amount,float age,float yaw,float pitch){
            rightArmPose=human.hunting()?HumanoidModel.ArmPose.CROSSBOW_HOLD:HumanoidModel.ArmPose.EMPTY;
            leftArmPose=HumanoidModel.ArmPose.EMPTY;super.setupAnim(human,walk,amount,age,yaw,pitch);
        }
    }
    private static final class HumanRenderer extends MobRenderer<HouseHuman,HumanModel> {
        HumanRenderer(EntityRendererProvider.Context context){super(context,new HumanModel(context.bakeLayer(ModelLayers.PLAYER)),.3F);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,context.getItemInHandRenderer()));}
        @Override public ResourceLocation getTextureLocation(HouseHuman actor){return HOLLOWAY;}
        @Override protected boolean shouldShowName(HouseHuman actor){return false;}
    }
    public static final class HarriganModel extends PlayerModel<ArmorStand> {
        public HarriganModel(net.minecraft.client.model.geom.ModelPart part){super(part,false);}
        private static void rotate(net.minecraft.client.model.geom.ModelPart part,net.minecraft.core.Rotations pose){float radians=(float)Math.PI/180;part.xRot=pose.getX()*radians;part.yRot=pose.getY()*radians;part.zRot=pose.getZ()*radians;}
        @Override public void setupAnim(ArmorStand body,float walk,float amount,float age,float yaw,float pitch){
            super.setupAnim(body,0,0,age,0,0);rotate(head,body.getHeadPose());rotate(this.body,body.getBodyPose());
            rotate(leftArm,body.getLeftArmPose());rotate(rightArm,body.getRightArmPose());rotate(leftLeg,body.getLeftLegPose());rotate(rightLeg,body.getRightLegPose());copyClothing();
        }
        public void copyClothing(){hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);}
    }
    private static final class HarriganRenderer extends LivingEntityRenderer<ArmorStand,HarriganModel> {
        HarriganRenderer(EntityRendererProvider.Context context){super(context,new HarriganModel(context.bakeLayer(ModelLayers.PLAYER)),0);}
        @Override public ResourceLocation getTextureLocation(ArmorStand body){return HarriganAppearance.variant(body)>=2?HARRIGAN_DEAD:HARRIGAN;}
        @Override protected boolean isBodyVisible(ArmorStand body){return true;}
        @Override protected boolean shouldShowName(ArmorStand body){return false;}
        @Override protected void setupRotations(ArmorStand body,com.mojang.blaze3d.vertex.PoseStack poses,float age,float yaw,float partial,float scale){
            super.setupRotations(body,poses,age,yaw,partial,scale);
            if(HarriganAppearance.variant(body)==3){
                poses.mulPose(Axis.XP.rotationDegrees(-90));poses.mulPose(Axis.YP.rotationDegrees(180));
            }
        }
    }
}
