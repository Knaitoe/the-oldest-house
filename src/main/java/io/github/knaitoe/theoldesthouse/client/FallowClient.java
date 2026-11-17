package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md): the late afternoon over the wheat on its reader's screen. Outside, a dry, warm haze the
 * colour of the dust off the road, near enough that the wheat runs on into it and no edge of the field is ever seen; under a roof
 * (the store, the garage, the cellar) the ordinary air. 0.4.682 (docs/FALLOW_0_4_682.md): after the light, the ash instead, by day and by
 * night, with the sky gone to it and ash coming down.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FallowClient {
    private FallowClient(){}
    /** Where the haze begins and where it is whole, in blocks: the field is 56 blocks to each side of the road, so it closes before them. */
    public static final float HAZE_NEAR=12F,HAZE_FAR=46F;
    /** Where this reader is, relative to the station's base, if they are at it. */
    @Nullable static Vec3 here(){var mc=Minecraft.getInstance();return mc.player==null||mc.player.isSpectator()?null:at();}
    /** 0.4.674: as here(), but a spectator watching counts too (FallowStrikeClient: they see the strike, and earn nothing). */
    @Nullable static Vec3 at(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!HouseSightlineState.hasOrigin())return null;
        var origin=HouseSightlineState.origin();var p=LabyrinthPlaces.placeAt(origin,mc.player.blockPosition());
        if(p!=LabyrinthPlace.FALLOW||!mc.level.dimension().equals(NovelRooms.dimension(p)))return null;
        var b=LabyrinthPlaces.base(origin,p);if(b==null)return null;
        var r=mc.player.position().subtract(b.getX(),b.getY(),b.getZ());return FallowSite.inScene(r)?r:null;
    }
    /** Under the store's, the garage's or the cellar's roof. */
    static boolean indoors(Vec3 r){return FallowSite.inStore(r)||FallowSite.inGarage(r)||FallowSite.inCellar(r);}
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        float partial=(float)e.getPartialTick();var a=ash(partial);
        if(a!=null){
            var r=at();if(r==null||indoors(r))return;var c=ashColour(a[1]);
            // The dust's haze gives way to the ash's over the first seconds after the light.
            e.setRed(Mth.lerp(a[0],.80F,c[0]));e.setGreen(Mth.lerp(a[0],.72F,c[1]));e.setBlue(Mth.lerp(a[0],.54F,c[2]));return;
        }
        var r=here();if(r==null||indoors(r))return;
        e.setRed(.80F);e.setGreen(.72F);e.setBlue(.54F);
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void haze(ViewportEvent.RenderFog e){
        if(e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        var a=ash((float)e.getPartialTick());
        if(a!=null){
            var r=at();if(r==null||indoors(r))return;
            float near=Mth.lerp(a[1],ASH_NEAR,ASH_NIGHT_NEAR),far=Mth.lerp(a[1],ASH_FAR,ASH_NIGHT_FAR);
            near=Mth.lerp(a[0],HAZE_NEAR,near);far=Math.min(e.getFarPlaneDistance(),Mth.lerp(a[0],HAZE_FAR,far));
            e.setNearPlaneDistance(Math.min(near,far*.5F));e.setFarPlaneDistance(far);e.setCanceled(true);return;
        }
        var r=here();if(r==null||indoors(r))return;
        float far=Math.min(e.getFarPlaneDistance(),HAZE_FAR),near=Math.min(HAZE_NEAR,far*.5F);
        e.setNearPlaneDistance(near);e.setFarPlaneDistance(far);e.setCanceled(true);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.682, the ash
    /** After the light (docs/FALLOW_0_4_682.md): the ash close round, closer still by night. */
    public static final float ASH_NEAR=4F,ASH_FAR=30F,ASH_NIGHT_NEAR=2F,ASH_NIGHT_FAR=14F;
    /** How far the light is over this reader's afternoon: {how far the ash has come in, 0 to 1 over the first three seconds; how dark,
     * 0 by day to 1 by night, on the buried days' own clock}; null before the light, or away from the station. Dusk comes on over the
     * last three seconds before Otis's nightfall, and the dark goes off it over the first three of the morning. */
    @Nullable static float[] ash(float partial){
        float t=FallowStrikeClient.now(partial);if(t<FallowStrike.LIGHT)return null;
        float in=Mth.clamp((t-FallowStrike.LIGHT)/60F,0,1),b=t-FallowStrike.LIGHT-FallowBuried.SETTLE;
        if(b<0)return new float[]{in,0};
        float d=b%FallowBuried.DAY;boolean first=b<FallowBuried.DAY;
        float night=d<60?(first?0:1-d/60):d<FallowBuried.NIGHT_AT-60?0:d<FallowBuried.NIGHT_AT?(d-FallowBuried.NIGHT_AT+60)/60:1;
        return new float[]{in,night};
    }
    /** The station's hour after the light, for the light on the ground (NovelSceneClient): by day as dim as the late evening, by night the
     * middle of it; the afternoon going over to it as the ash comes in. Elsewhere, and before the light, the time it was. */
    public static long time(net.minecraft.world.level.Level level,long time){
        var mc=Minecraft.getInstance();if(level!=mc.level)return time;var a=ash(0);if(a==null||at()==null)return time;
        return (long)Mth.lerp(a[0],time,Mth.lerp(a[1],12000F,18000F));
    }
    /** The ash's air, by day a dull grey with no warmth in it and by night near black. */
    static float[] ashColour(float night){return new float[]{Mth.lerp(night,.40F,.05F),Mth.lerp(night,.38F,.05F),Mth.lerp(night,.36F,.06F)};}
    /** The sky over the ash for HouseOutsideEffects: one low ceiling of it, no sun, no moon, no stars; null where there is no ash. The sky
     * goes to it with the fog over the first three seconds. */
    @Nullable public static Vec3 ashSky(float partial,Vec3 sky){
        var a=ash(partial);if(a==null||at()==null)return null;var c=ashColour(a[1]);
        return new Vec3(Mth.lerp(a[0],sky.x,c[0]*.92F),Mth.lerp(a[0],sky.y,c[1]*.92F),Mth.lerp(a[0],sky.z,c[2]*.92F));
    }
    /** Ash coming down out of it, round a reader who is out under the sky after the field has burnt (not in the light, not below or under
     * a roof): grey flakes and pale ones, the most of it by day. */
    @SubscribeEvent public static void falling(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.isPaused())return;
        float t=FallowStrikeClient.now(0);if(t<FallowStrike.LIGHT+FallowStrike.OUT_FAINT+5)return;
        var r=at();if(r==null||indoors(r))return;var a=ash(0);if(a==null)return;
        var random=mc.level.random;var eye=mc.player.getEyePosition();int count=a[1]>.5F?4:10;
        for(int i=0;i<count;i++){
            double x=eye.x+(random.nextDouble()-.5)*28,y=eye.y+random.nextDouble()*10-2,z=eye.z+(random.nextDouble()-.5)*28;
            var pos=net.minecraft.core.BlockPos.containing(x,y,z);if(!mc.level.canSeeSky(pos))continue;
            mc.level.addParticle(random.nextInt(3)==0?ParticleTypes.WHITE_ASH:ParticleTypes.ASH,x,y,z,0,-.02,0);
        }
    }
}
