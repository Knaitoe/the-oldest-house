package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PolybiusPrize;
import io.github.knaitoe.theoldesthouse.labyrinth.PolybiusRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.PrizeProp;
import io.github.knaitoe.theoldesthouse.labyrinth.PrizeWear;
import io.github.knaitoe.theoldesthouse.network.PrizeTakeOffPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.411: how the arcade's prizes look in use. The bracelet is held up straight and slapped down round the wrist; the finger trap and
 * the plush dog take both hands; the trap stretches as it tightens; the ring's stone shows its wearer. Worn bracelets and rings are drawn
 * on every player who wears them, in the world, in mirrors and on the reader's own arm. A set-down spider, dog or star is drawn where it
 * was put, the star glowing, the spider jumping when it is startled. Crouching and using an empty hand asks the server to take one off.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PrizeClient {
    private PrizeClient(){}
    static final ResourceLocation WORN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/prize_wear.png");

    static PolybiusPrize prize(PolybiusPrize.Kind kind){return PolybiusRegistry.PRIZES.get(kind.ordinal()).get();}
    static boolean using(LivingEntity e,ItemStack stack){return e!=null&&e.isUsingItem()&&e.getUseItem()==stack;}

    @SubscribeEvent public static void takeOffInAir(PlayerInteractEvent.RightClickEmpty e){if(e.getHand()==InteractionHand.MAIN_HAND)askTakeOff(e.getEntity());}
    @SubscribeEvent public static void takeOffAtBlock(PlayerInteractEvent.RightClickBlock e){if(e.getLevel().isClientSide()&&e.getHand()==InteractionHand.MAIN_HAND)askTakeOff(e.getEntity());}
    static void askTakeOff(net.minecraft.world.entity.player.Player p){
        if(p.isCrouching()&&p.getMainHandItem().isEmpty()&&PrizeWear.worn(p)!=0&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(PrizeTakeOffPayload.INSTANCE);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){PrizeWear.clientClear();}

    /** The reader's own arm in first person, with what it wears. */
    @SubscribeEvent public static void ownArm(RenderArmEvent e){
        var player=e.getPlayer();int worn=PrizeWear.worn(player);if(worn==0)return;
        if(!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer))return;
        var model=renderer.getModel();model.attackTime=0;model.crouching=false;model.swimAmount=0;model.setupAnim(player,0,0,0,0,0);
        ModelPart arm=e.getArm()==HumanoidArm.RIGHT?model.rightArm:model.leftArm;arm.xRot=0;
        var pose=e.getPoseStack();pose.pushPose();arm.translateAndRotate(pose);
        drawArm(pose,e.getMultiBufferSource(),e.getPackedLight(),OverlayTexture.NO_OVERLAY,player,e.getArm(),worn);
        pose.popPose();
    }

    /** One arm's bracelet and ring, in that arm's own pixels: the arm runs from y -2 at the shoulder to y 10 at the hand. */
    static void drawArm(PoseStack pose,MultiBufferSource out,int light,int overlay,AbstractClientPlayer player,HumanoidArm arm,int worn){
        boolean right=arm==HumanoidArm.RIGHT,slim=player.getSkin().model()==PlayerSkin.Model.SLIM;
        float x0=right?(slim?-2:-3):-1,x1=right?1:(slim?2:3);
        var buffer=out.getBuffer(RenderType.entityCutoutNoCull(WORN));var last=pose.last();
        if((worn&(right?PrizeWear.RIGHT_WRIST:PrizeWear.LEFT_WRIST))!=0)
            box(buffer,last,x0-.35F,7.1F,-2.35F,x1+.35F,8.7F,2.35F,0,0,16,3,light,overlay);
        if((worn&(right?PrizeWear.RIGHT_RING:PrizeWear.LEFT_RING))!=0){
            box(buffer,last,x0-.3F,9.25F,-2.3F,x1+.3F,9.75F,2.3F,0,4,16,5,light,overlay);
            float outer=right?x0:x1,side=right?-1:1;int stone=PolybiusPrize.mood(player);
            box(buffer,last,Math.min(outer,outer+side*.75F),9.0F,-.7F,Math.max(outer,outer+side*.75F),9.9F,.7F,stone*2,6,stone*2+2,8,light,overlay);
        }
    }

    /** A box of the worn texture (16 pixels square), every face showing the same region. */
    static void box(VertexConsumer b,PoseStack.Pose p,float x0,float y0,float z0,float x1,float y1,float z1,float u0,float v0,float u1,float v1,int light,int overlay){
        x0/=16;y0/=16;z0/=16;x1/=16;y1/=16;z1/=16;u0/=16;v0/=16;u1/=16;v1/=16;
        float[][][] faces={
            {{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},{0,0,-1}},
            {{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1},{0,0,1}},
            {{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1},{-1,0,0}},
            {{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0},{1,0,0}},
            {{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0},{0,-1,0}},
            {{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1},{0,1,0}}};
        float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        for(var f:faces)for(int i=0;i<4;i++)b.addVertex(p,f[i][0],f[i][1],f[i][2]).setColor(-1).setUv(uv[i][0],uv[i][1]).setOverlay(overlay).setLight(light).setNormal(p,f[4][0],f[4][1],f[4][2]);
    }

    /** What every player wears, drawn on their arms for every viewer. */
    public static final class Worn extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        public Worn(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            int worn=PrizeWear.worn(player);if(worn==0||player.isInvisible())return;
            int overlay=LivingEntityRenderer.getOverlayCoords(player,0);
            for(var arm:HumanoidArm.values()){
                int bits=arm==HumanoidArm.RIGHT?PrizeWear.RIGHT_WRIST|PrizeWear.RIGHT_RING:PrizeWear.LEFT_WRIST|PrizeWear.LEFT_RING;
                if((worn&bits)==0)continue;
                ModelPart part=arm==HumanoidArm.RIGHT?getParentModel().rightArm:getParentModel().leftArm;if(!part.visible)continue;
                pose.pushPose();part.translateAndRotate(pose);drawArm(pose,out,light,overlay,player,arm,worn);pose.popPose();
            }
        }
    }

    /** A spider, a dog or a star where it was set down. */
    public static final class PropRenderer extends EntityRenderer<PrizeProp> {
        private final ItemRenderer items;
        public PropRenderer(EntityRendererProvider.Context c){super(c);items=c.getItemRenderer();shadowRadius=0;}
        @Override public void render(PrizeProp prop,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){
            var kind=prop.kind();var face=prop.face();
            pose.pushPose();
            if(face==Direction.UP){
                pose.mulPose(Axis.YP.rotationDegrees(-prop.getYRot()));
                if(kind==PolybiusPrize.Kind.SPIDER){
                    float t=prop.tickCount-prop.jumpedAt+partial;
                    if(t>=0&&t<14){float s=Mth.sin(t/14F*Mth.PI);pose.translate(0,s*.32F,0);pose.mulPose(Axis.YP.rotationDegrees(Mth.sin(t*1.7F)*25F*s));}
                }
                if(kind==PolybiusPrize.Kind.PLUSH_DOG){pose.translate(0,.3F,0);pose.scale(.62F,.62F,.62F);}
                else{pose.translate(0,.035F,0);pose.mulPose(Axis.XP.rotationDegrees(90));pose.scale(.5F,.5F,.5F);}
            }else if(face==Direction.DOWN){
                pose.translate(0,prop.getBbHeight()-.035F,0);pose.mulPose(Axis.XP.rotationDegrees(-90));pose.scale(.5F,.5F,.5F);
            }else{
                pose.translate(0,prop.getBbHeight()/2,0);pose.mulPose(Axis.YP.rotationDegrees(180F-face.toYRot()));pose.translate(0,0,-.03F);pose.scale(.5F,.5F,.5F);
            }
            items.renderStatic(prop.stack(),ItemDisplayContext.FIXED,kind==PolybiusPrize.Kind.GLOW_STAR?LightTexture.FULL_BRIGHT:light,OverlayTexture.NO_OVERLAY,pose,out,prop.level(),prop.getId());
            pose.popPose();
            super.render(prop,yaw,partial,pose,out,light);
        }
        @Override public ResourceLocation getTextureLocation(PrizeProp prop){return InventoryMenu.BLOCK_ATLAS;}
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
            e.registerEntityRenderer(PolybiusRegistry.PROP.get(),PropRenderer::new);
            e.registerEntityRenderer(PolybiusRegistry.BALL.get(),ThrownItemRenderer::new);
        }
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins())if(e.getSkin(skin) instanceof PlayerRenderer player)player.addLayer(new Worn(player));
        }
        @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
            ItemProperties.register(prize(PolybiusPrize.Kind.SLAP_BRACELET),id("straight"),(stack,level,entity,seed)->using(entity,stack)?1F:0F);
            ItemProperties.register(prize(PolybiusPrize.Kind.FINGER_TRAP),id("pull"),(stack,level,entity,seed)->PolybiusPrize.pull(stack)/(float)PolybiusPrize.MOST);
            ItemProperties.register(prize(PolybiusPrize.Kind.MOOD_RING),id("mood"),(stack,level,entity,seed)->(4-PolybiusPrize.mood(entity))/4F);
        });}
        static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,path);}

        @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent e){
            // Both hands at it: the finger trap pulled between them, the dog held close.
            e.registerItem(new IClientItemExtensions(){
                @Override public HumanoidModel.ArmPose getArmPose(LivingEntity who,InteractionHand hand,ItemStack stack){return using(who,stack)?HumanoidModel.ArmPose.CROSSBOW_HOLD:null;}
                @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partial,float equip,float swing){
                    if(!using(player,stack))return false;
                    float pull=PolybiusPrize.is(stack,PolybiusPrize.Kind.FINGER_TRAP)?Mth.sin((player.getTicksUsingItem()+partial)*.3F)*.015F:0F;
                    pose.translate(0,-.42F+equip*-.6F+pull,-.62F);
                    if(PolybiusPrize.is(stack,PolybiusPrize.Kind.PLUSH_DOG)){pose.translate(0,-.05F,.14F);pose.mulPose(Axis.YP.rotationDegrees(180));}
                    return true;
                }
            },prize(PolybiusPrize.Kind.FINGER_TRAP),prize(PolybiusPrize.Kind.PLUSH_DOG));
            // The bracelet held up straight, then brought down hard.
            e.registerItem(new IClientItemExtensions(){
                @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partial,float equip,float swing){
                    if(!using(player,stack))return false;
                    float p=Mth.clamp((player.getTicksUsingItem()+partial)/PolybiusPrize.SNAP,0F,1F),side=arm==HumanoidArm.RIGHT?1:-1;
                    pose.translate(side*.56F,-.52F+equip*-.6F+.3F*p,-.72F);
                    pose.mulPose(Axis.XP.rotationDegrees(-55F*p));pose.mulPose(Axis.ZP.rotationDegrees(side*15F*p));
                    return true;
                }
            },prize(PolybiusPrize.Kind.SLAP_BRACELET),prize(PolybiusPrize.Kind.MOOD_RING));
        }
    }
}
