package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.ShallowsCuePayload;
import io.github.knaitoe.theoldesthouse.network.ShallowsSwingPayload;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The reader's side of the Shallows (0.4.101): the strafe keys swing her instead of walking, the boys' lines appear over
 * whoever says them and only to this reader, the boys react and watch her, and the reader's own arms hold her wrists.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ShallowsClient {
    private record Cue(String line,int mood,long start,long until){}
    private static final Map<Integer,Cue> CUES=new HashMap<>();
    private static boolean left,right,held;private static long released=-1000;private static int phase=-1;
    @Nullable private static LakeWitchEntity cached;
    private ShallowsClient(){}

    public static void accept(ShallowsCuePayload payload){
        var mc=Minecraft.getInstance();if(mc.level==null||payload.entity()<0)return;long now=mc.level.getGameTime();
        var old=CUES.get(payload.entity());
        // A reaction without words keeps a line already being said over the same head.
        String line=payload.line().isEmpty()&&old!=null&&old.until()>now?old.line():payload.line();
        CUES.put(payload.entity(),new Cue(line,payload.mood(),now,now+payload.ticks()));
    }
    private static @Nullable Cue cue(Entity e){
        var mc=Minecraft.getInstance();var cue=CUES.get(e.getId());
        return cue==null||mc.level==null||cue.until()<mc.level.getGameTime()?null:cue;
    }
    /** The reaction a boy is showing this reader now, and how long he has shown it, in ticks. */
    public static int mood(Entity e){var cue=cue(e);return cue==null?Shallows.CALM:cue.mood();}
    public static float moodAge(Entity e,float partial){var cue=cue(e);var mc=Minecraft.getInstance();return cue==null||mc.level==null?0:mc.level.getGameTime()-cue.start()+partial;}
    /** The recollection near this reader: their own, or one they joined (0.4.157); nobody else is ever sent it. */
    public static @Nullable LakeWitchEntity girl(){return cached==null||cached.isRemoved()||cached.memoryPhase()==Shallows.GONE?null:cached;}
    private static @Nullable LakeWitchEntity find(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return null;
        for(var g:mc.level.getEntitiesOfClass(LakeWitchEntity.class,mc.player.getBoundingBox().inflate(48),LakeWitchEntity::memory))
            if(g.memoryPhase()!=Shallows.GONE)return g;
        return null;
    }
    /** 0.4.157: only the readers holding her (her wrists or, with a friend, her feet) swing her; the rest only watch. */
    public static boolean carrying(){
        var mc=Minecraft.getInstance();var g=girl();return g!=null&&mc.player!=null&&g.memoryPhase()==Shallows.CARRIED&&mc.player.distanceToSqr(g)<16
                &&mc.player.getMainHandItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get());
    }
    /** Where the boys on the bank look: at her, wherever she is. */
    public static @Nullable Vec3 focus(float partial){var g=girl();return g==null?null:g.getPosition(partial).add(0,.3,0);}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null){CUES.clear();cached=null;return;}
        long now=mc.level.getGameTime();CUES.values().removeIf(cue->cue.until()<now-1);
        cached=find();var g=girl();int current=g==null?-1:g.memoryPhase();
        // Only hands that held her follow through; a reader watching from the bank (0.4.157) keeps still.
        if(phase==Shallows.CARRIED&&current==Shallows.FLYING&&held)released=now;
        phase=current;held=carrying();
    }
    /** While she hangs between them the strafe keys swing her, one stroke per press; walking and jumping stop. */
    @SubscribeEvent public static void input(MovementInputUpdateEvent e){
        var mc=Minecraft.getInstance();if(e.getEntity()!=mc.player)return;
        if(!carrying()){left=right=false;return;}
        var input=e.getInput();
        if(input.left&&!left)PacketDistributor.sendToServer(new ShallowsSwingPayload(-1));
        if(input.right&&!right)PacketDistributor.sendToServer(new ShallowsSwingPayload(1));
        left=input.left;right=input.right;
        // Sneaking stays: it is how the reader lets go of her.
        input.forwardImpulse=input.leftImpulse=0;input.up=input.down=input.left=input.right=input.jumping=false;
    }
    /** Each line shows over whoever says it, for this reader only. */
    @SubscribeEvent public static void nameTag(RenderNameTagEvent e){
        var cue=cue(e.getEntity());if(cue==null||cue.line().isEmpty())return;
        e.setContent(Component.literal(cue.line()));e.setCanRender(TriState.TRUE);
    }
    private static float swing(float partial){var g=girl();return g==null?0:g.swingAt(partial);}
    /** Which way the lake lies for the local reader: -1, on their left, at her wrists; 1, on their right, at her feet (0.4.157). */
    private static float waterSide(){var mc=Minecraft.getInstance();var g=girl();return g!=null&&mc.player!=null&&mc.player.getX()>g.getX()?1:-1;}
    private static float followThrough(float partial){
        var mc=Minecraft.getInstance();if(mc.level==null)return 0;float t=mc.level.getGameTime()-released+partial;
        return t<0||t>14?0:Mth.sin(Mth.clamp(t/14,0,1)*Mth.PI);
    }
    /**
     * Both arms of a carrier: forward to her wrists or ankles, out toward the water with the swing. {@code waterSide} is 1
     * when the water is on the model's right (the boy) and -1 when it is on its left (the reader).
     */
    public static void carryArms(HumanoidModel<?> model,float swing,float waterSide,float reach,boolean torso){
        float lift=1-Mth.cos(swing);
        for(var arm:new net.minecraft.client.model.geom.ModelPart[]{model.leftArm,model.rightArm}){
            // A positive yaw turns a raised arm to the body's right; waterSide is +1 when the lake is on that side.
            arm.xRot=-.95F-.45F*lift-reach*.6F;arm.yRot=waterSide*swing*.85F;arm.zRot=(arm==model.leftArm?.12F:-.12F);
        }
        // A boy twists with the swing and watches her; a reader's own head and body stay with their camera.
        if(torso){model.body.yRot=waterSide*swing*.22F;model.head.yRot=model.body.yRot*.5F;model.head.xRot=.35F;}
    }
    /** The reader's own body, seen from behind or by a camera: holding her wrists. */
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        var mc=Minecraft.getInstance();if(entity!=mc.player)return;
        float partial=age-entity.tickCount;float reach=followThrough(partial);
        if(!carrying()&&reach==0)return;
        carryArms(model,carrying()?swing(partial):0,waterSide(),reach,false);
        var missing=BodyLoss.missing(mc.player);
        if(missing==HumanoidArm.LEFT)model.leftArm.xRot=0;else if(missing==HumanoidArm.RIGHT)model.rightArm.xRot=0;
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);
    }
    /** First person: the reader's hands out in front, closed on her wrists, carried left and up with each stroke out. */
    @SubscribeEvent public static void hand(RenderHandEvent e){
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        float partial=e.getPartialTick();boolean carrying=carrying();float reach=followThrough(partial);
        if(!carrying&&reach==0)return;
        e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
        if(!(mc.getEntityRenderDispatcher().getRenderer(mc.player) instanceof PlayerRenderer player))return;
        float swing=(carrying?swing(partial):0)*-waterSide(),lift=1-Mth.cos(swing);var missing=BodyLoss.missing(mc.player);
        for(int side:new int[]{-1,1}){
            if(missing==(side<0?HumanoidArm.LEFT:HumanoidArm.RIGHT))continue;
            var pose=e.getPoseStack();pose.pushPose();
            // The vanilla skin-arm basis, brought in toward the middle where her two wrists are held together.
            pose.translate(side*(.30-.06*reach)-.42*Mth.sin(swing),-.6+.2*lift+.16*reach,-.86-.3*reach);
            pose.mulPose(Axis.YP.rotationDegrees(side*(28-10*reach)));pose.mulPose(Axis.ZP.rotationDegrees(-swing*14));
            pose.translate(-side,3.6,3.5);
            pose.mulPose(Axis.ZP.rotationDegrees(side*120));pose.mulPose(Axis.XP.rotationDegrees(200-12*reach));
            pose.mulPose(Axis.YP.rotationDegrees(-side*135));pose.translate(side*5.6,0,0);
            if(side>0)player.renderRightHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);else player.renderLeftHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);
            pose.popPose();
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){CUES.clear();phase=-1;released=-1000;cached=null;held=false;}
}
