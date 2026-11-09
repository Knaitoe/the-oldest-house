package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.BorrascaViewPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** One recording at the refinery, with phase-aligned low-pass distance/enclosure filtering. No score. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaAtmosphere {
    private static BorrascaViewPayload state;
    private static ClientLevel world;
    private static Loop clear,muffled,wind;
    private static int retry;
    private static long received;
    private record SheriffLens(Vec3 position,Vec3 direction){}
    private static SheriffLens sheriffLens;
    private BorrascaAtmosphere(){}
    /** 0.4.440: how many times this reader had been eaten when last told, and when they last came to under the Tree after it. */
    private static int swallowedSeen=-1;
    private static long woke=Long.MIN_VALUE/2;
    public static void accept(BorrascaViewPayload p){var mc=Minecraft.getInstance();boolean same=world!=null&&world==mc.level;state=p;world=mc.level;received=world==null?0:world.getGameTime();
        if(same&&swallowedSeen>=0&&p.swallowed()>swallowedSeen)woke=received;swallowedSeen=p.swallowed();if(!p.active())stop();}
    /** 0.4.440: ticks since he took hold of this reader, or -1 when he has not. */
    static float taken(float partial){var mc=Minecraft.getInstance();if(state==null||mc.level==null||mc.level!=world||state.grab()<=0||state.visit()>2)return -1;return BorrascaStory.GRAB_TICKS-state.grab()+(mc.level.getGameTime()-received)+partial;}
    /** 0.4.440: how black the child's sight is: going as his mouth closes over them, held until they are at the Tree, then coming back. */
    static float black(float partial){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.level!=world)return 0;float a=0,e=taken(partial),end=BorrascaStory.GRAB_TICKS;
        if(e>=0)a=e<end?Mth.clamp((e-end*.78F)/(end*.18F),0,1):e<end+40?1:Mth.clamp(1-(e-end-40)/20,0,1);
        float since=mc.level.getGameTime()-woke+partial;if(since>=0&&since<60)a=Math.max(a,since<14?1:1-(since-14)/46);
        return a;
    }
    @SubscribeEvent public static void dark(RenderGuiEvent.Post e){float a=black(e.getPartialTick().getGameTimeDeltaPartialTick(false));if(a<=0)return;var g=e.getGuiGraphics();g.fill(0,0,g.guiWidth(),g.guiHeight(),((int)(255*a))<<24);}
    /** 0.4.440: him, folding down over this reader and taking them; only they see it. */
    @SubscribeEvent public static void taking(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!active())return;float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        float el=taken(partial);if(el<0||el>BorrascaStory.GRAB_TICKS+40||state.gentleman().equals(Vec3.ZERO))return;var mc=Minecraft.getInstance();
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();pose.pushPose();
        BorrascaGentlemanModel.renderTaking(pose,mc.renderBuffers().bufferSource(),el/BorrascaStory.GRAB_TICKS,mc.level.getGameTime()+partial,state.gentleman().subtract(camera),new Vec3(e.getCamera().getLookVector()));
        pose.popPose();
    }
    public static boolean active(){var mc=Minecraft.getInstance();if(state==null||!state.active()||mc.level==null||mc.level!=world||mc.player==null||!mc.player.isAlive()||mc.level.dimension()!=HouseDimensions.OUTSIDE)return false;var b=state.source().subtract(BorrascaRooms.world(BorrascaRooms.MACHINE));return BorrascaSites.bounds(b).contains(mc.player.position());}
    public static boolean playing(){var mc=Minecraft.getInstance();return clear!=null&&muffled!=null&&mc.getSoundManager().isActive(clear)&&mc.getSoundManager().isActive(muffled);}
    public static long time(long normal){return !active()?normal:switch(state.visit()){case 1->10000;case 2->11500;case 3->800;default->18000;};}
    public static BlockPos source(){return state==null?BlockPos.ZERO:state.source();}
    private static void stop(){var mc=Minecraft.getInstance();for(var s:new Loop[]{clear,muffled,wind})if(s!=null)mc.getSoundManager().stop(s);clear=muffled=wind=null;retry=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!active()){stop();state=null;world=null;return;}mc.getMusicManager().stopPlaying();
        if(clear!=null&&mc.options.getSoundSourceVolume(SoundSource.MASTER)>0&&mc.options.getSoundSourceVolume(SoundSource.AMBIENT)>0&&!playing()){if(++retry>=40)stop();}else retry=0;
        if(clear==null){clear=new Loop(BorrascaRegistry.GRINDER.get(),0);muffled=new Loop(BorrascaRegistry.GRINDER_MUFFLED.get(),1);wind=new Loop(BorrascaRegistry.TOWN_WIND.get(),2);mc.getSoundManager().play(clear);mc.getSoundManager().play(muffled);mc.getSoundManager().play(wind);}
    }
    @SubscribeEvent public static void choose(SelectMusicEvent e){if(active())e.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent e){var sound=e.getSound();if(active()&&sound!=null&&sound.getSource()==SoundSource.MUSIC&&sound.getLocation().getNamespace().equals("minecraft"))e.setSound(null);}
    @SubscribeEvent public static void glare(RenderGuiEvent.Post e){
        if(!active()||state.takingTicks()<=0||state.takingTicks()>60)return;var mc=Minecraft.getInstance();var camera=mc.gameRenderer.getMainCamera();var at=state.source().subtract(BorrascaRooms.world(BorrascaRooms.MACHINE)).offset(BorrascaRooms.LAMP).getCenter();var d=at.subtract(camera.getPosition());if(d.lengthSqr()<.01)return;
        var f=camera.getLookVector();double look=new Vec3(f.x(),f.y(),f.z()).dot(d.normalize());if(look<.7)return;
        var ray=mc.level.clip(new ClipContext(camera.getPosition(),at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,mc.player));if(ray.getType()!=HitResult.Type.MISS&&ray.getLocation().distanceToSqr(at)>2)return;
        double effects=Comfort.effects();double rise=Math.sin(Math.PI*Math.min(60,state.takingTicks()+mc.level.getGameTime()-received)/60D);int alpha=(int)(225*Mth.clamp((look-.7)/.3,0,1)*Math.max(0,rise)*effects);if(alpha<=0)return;
        e.getGuiGraphics().fill(0,0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),alpha<<24|0xF1EEE6);
    }
    static boolean rendersSheriffBeam(LiteraryActor actor){
        return actor.role()==LiteraryActor.BORRASCA_SHERIFF&&actor.gesture()==4&&!actor.isInvisible()
                &&actor.scene().equals(LabyrinthPlace.BORRASCA.id())&&active()&&(state.visit()==3||state.visit()==4)
                &&actor.owner().filter(Minecraft.getInstance().player.getUUID()::equals).isPresent();
    }
    static void sheriffLens(Vec3 position,Vec3 direction){sheriffLens=new SheriffLens(position,direction);}
    @SubscribeEvent public static void beams(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var lens=sheriffLens;sheriffLens=null;
        if(!active())return;
        var b=state.source().subtract(BorrascaRooms.world(BorrascaRooms.MACHINE));
        if(lens!=null)
            beam(e,lens.position().add(lens.direction().scale(.015)),lens.direction(),22,5.8,.065F);
        if(state.takingTicks()>0||state.visit()==4&&state.running()){var direction=BorrascaRooms.at(b,BorrascaRooms.TAKING).getCenter().subtract(BorrascaRooms.at(b,BorrascaRooms.LAMP).getCenter()).normalize();beam(e,BorrascaRooms.at(b,BorrascaRooms.LAMP).getCenter().add(direction.scale(.65)),direction,48,8,.09F);}
    }
    /** 0.4.236: the Shiny Gentleman, where the server says he stands for this reader, for as long as the strokes last. */
    @SubscribeEvent public static void gentleman(RenderLevelStageEvent e){
        if(!active()||e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||state.visit()>2||state.gentlemanTicks()<=0)return;var mc=Minecraft.getInstance();
        double left=state.gentlemanTicks()-(mc.level.getGameTime()-received);if(left<=0)return;
        // 0.4.440: while he has hold of the reader, he is drawn taking them instead (taking).
        if(taken(e.getPartialTick().getGameTimeDeltaPartialTick(false))>=0)return;
        // He is simply there with the stroke, and fades as it dies.
        float alpha=(float)Mth.clamp(left/20D,0,1);
        var at=state.gentleman();var camera=e.getCamera().getPosition();var pose=e.getPoseStack();pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
        pose.mulPose(Axis.YP.rotation((float)Math.atan2(camera.x-at.x,camera.z-at.z)));drawGentleman(pose,mc.renderBuffers().bufferSource(),alpha,mc.level.getGameTime()+e.getPartialTick().getGameTimeDeltaPartialTick(false),state.gentlemanScale());pose.popPose();
    }
    /** Him, standing at the pose's origin and facing +Z, scale times his own height (0.4.434: his body, client.BorrascaGentlemanModel). */
    public static void drawGentleman(PoseStack pose,MultiBufferSource.BufferSource buffers,float alpha,float time,float scale){
        BorrascaGentlemanModel.render(pose,buffers,alpha,time,scale);buffers.endBatch(BorrascaGentlemanModel.type());
    }
    private static void beam(RenderLevelStageEvent e,Vec3 start,Vec3 direction,double length,double radius,float opacity){
        var mc=Minecraft.getInstance();var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();var type=RenderType.lightning();var v=buffers.getBuffer(type);var side=direction.cross(new Vec3(0,1,0)).normalize();var up=side.cross(direction).normalize();Vec3[] ring=new Vec3[17];
        for(int i=0;i<=16;i++){double a=i*Math.PI/8;var end=start.add(direction.scale(length)).add(side.scale(Math.cos(a)*radius)).add(up.scale(Math.sin(a)*radius));var hit=mc.level.clip(new ClipContext(start,end,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,mc.player));ring[i]=hit.getType()==HitResult.Type.MISS?end:hit.getLocation();}
        pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);for(int i=0;i<16;i++){for(var at:new Vec3[]{start,ring[i],ring[i+1],start})v.addVertex(pose.last(),(float)at.x,(float)at.y,(float)at.z).setColor(240,232,202,(int)(opacity*255));}pose.popPose();buffers.endBatch(type);
    }
    private static final class Loop extends AbstractTickableSoundInstance {
        final int band;
        Loop(SoundEvent event,int band){super(event,SoundSource.AMBIENT,RandomSource.create());this.band=band;looping=true;delay=0;relative=band==2;attenuation=Attenuation.NONE;volume=.001F;}
        @Override public boolean canStartSilent(){return true;}
        @Override public void tick(){if(!active()){stop();return;}var mc=Minecraft.getInstance();var p=mc.player;var at=state.source().getCenter();x=band==2?0:at.x;y=band==2?0:at.y;z=band==2?0:at.z;
            double distance=p.getEyePosition().distanceTo(at);var obstruction=mc.level.clip(new ClipContext(p.getEyePosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));boolean enclosed=!mc.level.canSeeSky(p.blockPosition())||obstruction.getType()!=HitResult.Type.MISS&&!obstruction.getBlockPos().equals(state.source());
            float filter=Mth.clamp((float)(distance-24)/70+(enclosed?.55F:0),0,1);float gain=state.running()?(float)(.65/(1+distance*.055)):0;
            float target=band==2?(enclosed?.018F:.045F):gain*(band==0?1-filter:filter*.8F);volume=Mth.lerp(.08F,volume,target);
        }
    }
}
