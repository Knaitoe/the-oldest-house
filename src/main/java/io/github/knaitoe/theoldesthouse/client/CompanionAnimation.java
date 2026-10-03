package io.github.knaitoe.theoldesthouse.client;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.PettingPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** The server starts a short pat; vanilla models supply its hand, head and tail motions. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CompanionAnimation {
    public interface PatModel {void oldestHousePat(float age);}
    private record Pat(UUID player,UUID pet,int start,int duration){}
    private static final Map<UUID,Pat> PATS=new HashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel scene;
    private CompanionAnimation(){}
    public static void accept(PettingPayload payload){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(scene!=mc.level){PATS.clear();scene=mc.level;}
        Entity player=mc.level.getEntity(payload.playerId()),pet=mc.level.getEntity(payload.petId());
        if(player==null||pet==null)return;
        PATS.put(player.getUUID(),new Pat(player.getUUID(),pet.getUUID(),player.tickCount,Math.max(1,Math.min(60,payload.ticks()))));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||scene!=mc.level){PATS.clear();scene=mc.level;return;}
        PATS.values().removeIf(p->{Entity player=mc.level.getPlayerByUUID(p.player);return player==null||!player.isAlive()||player.tickCount-p.start>p.duration;});
    }
    private static Pat forEntity(Entity entity,boolean pet){return pet?PATS.values().stream().filter(p->p.pet.equals(entity.getUUID())).findFirst().orElse(null):PATS.get(entity.getUUID());}
    public static float age(Entity entity,float partial,boolean pet){
        Pat pat=forEntity(entity,pet);if(pat==null)return -1;
        var mc=Minecraft.getInstance();Entity player=pet&&mc.level!=null?mc.level.getPlayerByUUID(pat.player):entity;
        if(player==null)return -1;float age=player.tickCount+partial-pat.start;return age>=0&&age<pat.duration?age:-1;
    }
    public static float weight(float age){return age<0?0:Math.max(0,Math.min(1,Math.min(age/6,(44-age)/8)));}
    public static void player(PlayerModel<?> model,LivingEntity entity,float age){
        float t=age(entity,age-entity.tickCount,false),w=weight(t);if(w==0)return;
        playerPose(model,entity.getMainArm(),t);
    }
    public static void playerPose(PlayerModel<?> model,HumanoidArm side,float t){
        float w=weight(t);var arm=side==HumanoidArm.RIGHT?model.rightArm:model.leftArm;
        arm.xRot=-1.05F*w+(float)Math.sin(t*.55F)*.17F*w;arm.yRot=(side==HumanoidArm.RIGHT?-.22F:.22F)*w;
        model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
    }
    public static void wolf(ModelPart head,ModelPart tail,Entity entity,float age){
        float t=age(entity,age-entity.tickCount,true),w=weight(t);if(w==0)return;
        wolfPose(head,tail,t);
    }
    public static void wolfPose(ModelPart head,ModelPart tail,float t){
        float w=weight(t);
        head.xRot+=.18F*w;head.zRot+=(float)Math.sin(t*.18F)*.13F*w;tail.yRot=(float)Math.sin(t*.8F)*.65F*w;
    }
    public static void cat(ModelPart head,ModelPart tail1,ModelPart tail2,Entity entity,float age){
        float t=age(entity,age-entity.tickCount,true),w=weight(t);if(w==0)return;
        catPose(head,tail1,tail2,t);
    }
    public static void catPose(ModelPart root,float t){
        catPose(root.getChild("head"),root.getChild("tail1"),root.getChild("tail2"),t);
    }
    public static void catPose(ModelPart head,ModelPart tail1,ModelPart tail2,float t){
        float w=weight(t);head.xRot-=.18F*w;head.zRot+=(float)Math.sin(t*.17F)*.2F*w;
        tail1.yRot=(float)Math.sin(t*.22F)*.22F*w;tail2.xRot+=.35F*w;
    }
    @SubscribeEvent public static void hand(RenderHandEvent e){
        var mc=Minecraft.getInstance();if(mc.player==null||e.getHand()!=InteractionHand.MAIN_HAND||!e.getItemStack().isEmpty())return;
        float t=age(mc.player,e.getPartialTick(),false),w=weight(t);if(w==0)return;
        var renderer=mc.getEntityRenderDispatcher().getRenderer(mc.player);if(!(renderer instanceof PlayerRenderer player))return;
        var pose=e.getPoseStack();pose.pushPose();float side=mc.player.getMainArm()==HumanoidArm.RIGHT?1:-1;
        pose.translate(side*.40,-.38-.06*Math.sin(t*.55),-.55);pose.mulPose(Axis.XP.rotationDegrees(-35));pose.mulPose(Axis.YP.rotationDegrees(side*15));
        if(side>0)player.renderRightHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);else player.renderLeftHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);
        pose.popPose();e.setCanceled(true);
    }
}
