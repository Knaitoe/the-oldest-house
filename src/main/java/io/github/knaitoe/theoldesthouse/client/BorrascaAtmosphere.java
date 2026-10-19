package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.BorrascaViewPayload;
import net.minecraft.client.Minecraft;
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
    private BorrascaAtmosphere(){}
    public static void accept(BorrascaViewPayload p){var mc=Minecraft.getInstance();state=p;world=mc.level;received=world==null?0:world.getGameTime();if(!p.active())stop();}
    public static boolean active(){var mc=Minecraft.getInstance();if(state==null||!state.active()||mc.level==null||mc.level!=world||mc.player==null||!mc.player.isAlive()||mc.level.dimension()!=HouseDimensions.OUTSIDE)return false;var b=state.source().subtract(BorrascaRooms.MACHINE);return BorrascaSites.bounds(b).contains(mc.player.position());}
    public static boolean playing(){var mc=Minecraft.getInstance();return clear!=null&&muffled!=null&&mc.getSoundManager().isActive(clear)&&mc.getSoundManager().isActive(muffled);}
    public static long time(long normal){return !active()?normal:switch(state.visit()){case 1->6000;case 2->9000;case 3->12500;default->18000;};}
    public static BlockPos source(){return state==null?BlockPos.ZERO:state.source();}
    private static void stop(){var mc=Minecraft.getInstance();for(var s:new Loop[]{clear,muffled,wind})if(s!=null)mc.getSoundManager().stop(s);clear=muffled=wind=null;retry=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!active()){stop();state=null;world=null;return;}mc.getMusicManager().stopPlaying();
        if(clear!=null&&mc.options.getSoundSourceVolume(SoundSource.MASTER)>0&&mc.options.getSoundSourceVolume(SoundSource.AMBIENT)>0&&!playing()){if(++retry>=40)stop();}else retry=0;
        if(clear==null){clear=new Loop(BorrascaRegistry.GRINDER.get(),0);muffled=new Loop(BorrascaRegistry.GRINDER_MUFFLED.get(),1);wind=new Loop(BorrascaRegistry.TOWN_WIND.get(),2);mc.getSoundManager().play(clear);mc.getSoundManager().play(muffled);mc.getSoundManager().play(wind);}
    }
    @SubscribeEvent public static void choose(SelectMusicEvent e){if(active())e.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent e){var sound=e.getSound();if(active()&&sound!=null&&sound.getSource()==SoundSource.MUSIC&&sound.getLocation().getNamespace().equals("minecraft"))e.setSound(null);}
    @SubscribeEvent public static void glare(RenderGuiEvent.Post e){
        if(!active()||state.takingTicks()<=0||state.takingTicks()>60)return;var mc=Minecraft.getInstance();var camera=mc.gameRenderer.getMainCamera();var at=state.source().subtract(BorrascaRooms.MACHINE).offset(BorrascaRooms.LAMP).getCenter();var d=at.subtract(camera.getPosition());if(d.lengthSqr()<.01)return;
        var f=camera.getLookVector();double look=new Vec3(f.x(),f.y(),f.z()).dot(d.normalize());if(look<.7)return;
        var ray=mc.level.clip(new ClipContext(camera.getPosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));if(ray.getType()!=HitResult.Type.MISS&&ray.getLocation().distanceToSqr(at)>2)return;
        double effects=mc.options.screenEffectScale().get();double rise=Math.sin(Math.PI*Math.min(60,state.takingTicks()+mc.level.getGameTime()-received)/60D);int alpha=(int)(225*Mth.clamp((look-.7)/.3,0,1)*Math.max(0,rise)*effects);if(alpha<=0)return;
        e.getGuiGraphics().fill(0,0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),alpha<<24|0xF1EEE6);
    }
    @SubscribeEvent public static void beams(RenderLevelStageEvent e){
        if(!active()||e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();var b=state.source().subtract(BorrascaRooms.MACHINE);
        if(state.visit()==3){double clock=state.clock()+mc.level.getGameTime()-received;double yaw=Math.toRadians(180+55*Math.sin(clock*.018));Vec3 start=state.sheriff().getCenter().add(0,1,0);for(var entity:mc.level.entitiesForRendering())if(entity instanceof LiteraryActor a&&a.role()==LiteraryActor.BORRASCA_SHERIFF&&a.scene().equals(LabyrinthPlace.BORRASCA.id())&&a.owner().filter(mc.player.getUUID()::equals).isPresent()){yaw=Math.toRadians(a.getYRot());start=a.getEyePosition();break;}beam(e,start,new Vec3(-Math.sin(yaw),-.03,Math.cos(yaw)),22,5.8,.065F);}
        if(state.takingTicks()>0&&state.takingTicks()<=60||state.visit()==4&&state.running())beam(e,b.offset(BorrascaRooms.LAMP).getCenter(),b.offset(BorrascaRooms.TAKING).getCenter().subtract(b.offset(BorrascaRooms.LAMP).getCenter()).normalize(),48,8,.09F);
    }
    private static void beam(RenderLevelStageEvent e,Vec3 start,Vec3 direction,double length,double radius,float opacity){
        var mc=Minecraft.getInstance();var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();var type=RenderType.lightning();var v=buffers.getBuffer(type);var side=direction.cross(new Vec3(0,1,0)).normalize();var up=side.cross(direction).normalize();Vec3[] ring=new Vec3[17];
        for(int i=0;i<=16;i++){double a=i*Math.PI/8;var end=start.add(direction.scale(length)).add(side.scale(Math.cos(a)*radius)).add(up.scale(Math.sin(a)*radius));var hit=mc.level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));ring[i]=hit.getType()==HitResult.Type.MISS?end:hit.getLocation();}
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
