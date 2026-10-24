package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * The mother in the miniatures workshop (0.4.121): pressed into an upper corner, face down, limbs spread on the ceiling. She
 * is only ever put in a corner that already sits at the edge of the reader's sight, with a clear line to it, and she is gone
 * the moment the reader turns toward her, or after a second and a half. Nothing of her is on the server or seen by anyone
 * else; the server only says where the corners are and that enough time has passed.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class MiniatureMotherClient {
    private static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_miniature_mother.png");
    private static final RandomSource RANDOM=RandomSource.create();
    /** Where on the screen she may appear, and where looking puts her out: fractions of the half-width or half-height. */
    static final float EDGE_FROM=.74F,EDGE_TO=.97F,GONE_INSIDE=.58F;
    private static long[] corners=new long[0];
    private static int lease,corner=-1,shown,cooldown=160,glimpses;
    private static ClientLevel world;
    private static LiteraryRenderers.CastModel model;
    private MiniatureMotherClient(){}

    public static void accept(CompoundTag models){
        var mc=Minecraft.getInstance();if(mc.player==null||!models.contains("MiniatureMother"))return;
        var t=models.getCompound("MiniatureMother");if(!t.hasUUID("Reader")||!t.getUUID("Reader").equals(mc.player.getUUID()))return;
        var c=t.getLongArray("Corners");if(c.length==0||c.length>8)return;
        if(world!=mc.level){glimpses=0;cooldown=160;}
        corners=c;lease=20;world=mc.level;
    }
    public static boolean visible(){return corner>=0&&lease>0;}

    /** The point she is drawn from: a hand under the ceiling in the corner cell, and the way her head points, out of the corner. */
    static Vec3 anchor(int i){var at=BlockPos.of(corners[i]);return new Vec3(at.getX()+.5,at.getY()+.86,at.getZ()+.5);}
    static float yaw(int i){
        // Her head points along the diagonal, out into the room, from the corner where her hips are.
        var at=BlockPos.of(corners[i]);double cx=0,cz=0;for(long c:corners){var o=BlockPos.of(c);cx+=o.getX();cz+=o.getZ();}cx/=corners.length;cz/=corners.length;
        return (float)Math.toDegrees(Math.atan2(cx-at.getX(),cz-at.getZ()));
    }
    /** A point along her body, from the hips in the corner (0) to the crown of her head (1.8), a little under the ceiling. */
    static Vec3 along(int i,double blocks){double r=Math.toRadians(yaw(i));return anchor(i).add(Math.sin(r)*blocks,-.25,Math.cos(r)*blocks);}
    /**
     * Where a point falls on the reader's screen, as the larger of its horizontal and vertical offsets over the half-width
     * or half-height: 0 at the centre, 1 at the edge, and a large number behind them.
     */
    static float screen(Minecraft mc,Vec3 at){
        var camera=mc.gameRenderer.getMainCamera();var eye=camera.getPosition();var to=at.subtract(eye);
        var f=camera.getLookVector();var u=camera.getUpVector();var l=camera.getLeftVector();
        double ahead=to.x*f.x()+to.y*f.y()+to.z*f.z();if(ahead<=.05)return 99;
        double side=-(to.x*l.x()+to.y*l.y()+to.z*l.z())/ahead,up=(to.x*u.x()+to.y*u.y()+to.z*u.z())/ahead;
        double halfV=Math.tan(Math.toRadians(mc.options.fov().get())/2),aspect=(double)mc.getWindow().getWidth()/Math.max(1,mc.getWindow().getHeight());
        return (float)Math.max(Math.abs(side)/(halfV*aspect),Math.abs(up)/halfV);
    }
    private static boolean clear(Minecraft mc,Vec3 at){
        var eye=mc.player.getEyePosition();var hit=mc.level.clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,mc.player));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(at)<.36;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(lease>0)lease--;
        if(lease<=0||mc.player==null||mc.level!=world){corner=-1;shown=0;return;}
        if(mc.isPaused())return;
        if(corner>=0){
            shown++;var head=along(corner,1.6);
            // Her head is the part nearest the middle of the screen; it is the part that must never be looked at.
            if(screen(mc,head)<GONE_INSIDE||screen(mc,along(corner,.8))<GONE_INSIDE||shown>30||!clear(mc,head)){corner=-1;shown=0;cooldown=300+RANDOM.nextInt(420)+glimpses*60;}
            return;
        }
        if(cooldown>0){cooldown--;return;}
        int start=RANDOM.nextInt(corners.length);
        for(int k=0;k<corners.length;k++){int i=(start+k)%corners.length;var at=along(i,.8);float head=screen(mc,along(i,1.6)),hips=screen(mc,along(i,0));
            if(Math.min(head,hips)>=EDGE_FROM&&screen(mc,at)<=EDGE_TO&&mc.player.getEyePosition().distanceToSqr(at)>16&&clear(mc,along(i,1.6))&&clear(mc,at)){
                corner=i;shown=0;glimpses++;
                mc.level.playLocalSound(at.x,at.y,at.z,SoundEvents.SPIDER_STEP,SoundSource.HOSTILE,.18F,.55F,false);
                return;
            }
        }
        cooldown=10;
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||!visible())return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.level!=world)return;
        if(model==null)model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
        var at=anchor(corner);var camera=e.getCamera().getPosition();float age=mc.player.tickCount+e.getPartialTick().getGameTimeDeltaPartialTick(false);
        var pose=e.getPoseStack();pose.pushPose();
        pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(yaw(corner)));
        place(pose);ceiling(model,age);
        int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(at));
        model.renderToBuffer(pose,mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(SKIN)),light,OverlayTexture.NO_OVERLAY,0xffffffff);
        pose.popPose();mc.renderBuffers().bufferSource().endBatch();
    }
    /**
     * From the corner's frame to the model's. Standing, the flipped model's head is up and her face looks toward -Z; a quarter
     * turn forward about X and a half turn about Y lay her along +Z, head out of the corner, face to the floor and back to the
     * ceiling, the back a hand under the anchor's plane.
     */
    static void place(com.mojang.blaze3d.vertex.PoseStack pose){pose.mulPose(Axis.YP.rotationDegrees(180));pose.mulPose(Axis.XP.rotationDegrees(-90));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);}
    /** Her pose on the ceiling: arms reaching on past her head, legs a little apart, the face lifted toward the room. */
    static void ceiling(LiteraryRenderers.CastModel m,float age){
        m.preview(LiteraryActor.LADY,0,0);
        float twitch=Mth.sin(age*1.7F)*.04F;
        // Past straight overhead, so the hands are flat to the ceiling ahead of her; the legs only a little back, under the gown.
        m.rightArm.xRot=-3.3F+twitch;m.leftArm.xRot=-3.15F-twitch;m.rightArm.zRot=.5F;m.leftArm.zRot=-.5F;
        m.rightLeg.xRot=.1F;m.leftLeg.xRot=.14F;m.rightLeg.zRot=.12F;m.leftLeg.zRot=-.12F;
        m.head.xRot=-.95F;m.head.zRot=.25F;m.hat.copyFrom(m.head);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){corners=new long[0];lease=0;corner=-1;world=null;glimpses=0;}
}
