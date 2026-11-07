package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * 0.4.333, State Route 9: rain and fog on the highway for whoever is on it, no music, and the shudder of a semi going by close. The
 * shudder is the reader's own: it comes from the traffic this client is drawing, and scales to the screen-effects setting. Crouched in
 * the median it is a tremble from both sides at once.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StateRouteClient {
    private StateRouteClient(){}
    static boolean onRoad(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        return LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==LabyrinthPlace.STATE_ROUTE;
    }
    public static boolean precipitating(){return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()&&onRoad()&&!dark();}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){return level==Minecraft.getInstance().level&&onRoad()?(dark()?.4F:1):nativeRain;}
    static StateRouteScene scene(){
        var mc=Minecraft.getInstance();if(mc.level==null||!HouseSightlineState.hasOrigin())return null;
        var b=LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.STATE_ROUTE);if(b==null)return null;
        return mc.level.getBlockEntity(b.offset(StateRouteSite.ANCHOR)) instanceof StateRouteScene s?s:null;
    }
    static boolean dark(){var s=scene();return s!=null&&s.mode==StateRouteScene.DARK;}
    @SubscribeEvent public static void music(SelectMusicEvent e){if(onRoad())e.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent e){if(onRoad()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC)e.setSound(null);}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(onRoad()){e.setRed(.02F);e.setGreen(.022F);e.setBlue(.03F);}}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){if(onRoad()&&e.getMode()==FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(8);e.setFarPlaneDistance(dark()?22:52);e.setCanceled(true);}}
    /** How hard the wind off passing trailers is shaking this reader now: 0 to 1. */
    static float shudder(float partial){
        var mc=Minecraft.getInstance();var s=scene();if(s==null||mc.player==null||s.mode!=StateRouteScene.FAST)return 0;
        var r=mc.player.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(s.base()));float now=mc.level.getGameTime()+partial;float amount=0;
        for(var v:s.traffic){if(v.kind!=StateRouteScene.SEMI&&v.kind!=StateRouteScene.CAR)continue;double x=StateRouteRenderer.x(s,v,now);
            double dx=Math.abs(r.x-x)-v.length()/2,dz=Math.abs(r.z-StateRouteSite.LANE_Z[v.lane])-1.2;if(dx>2||dz>3)continue;
            float near=(float)(1-Math.max(0,dz)/3)*(float)(1-Math.max(0,dx)/2);amount=Math.max(amount,near*(v.kind==StateRouteScene.SEMI?1:.35F));}
        return amount;
    }
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        if(!onRoad())return;float amount=shudder((float)e.getPartialTick())*Comfort.effects();if(amount<=0)return;
        var mc=Minecraft.getInstance();double t=mc.player.tickCount+e.getPartialTick();
        e.setRoll(e.getRoll()+(float)Math.sin(t*2.7)*amount*1.6F);e.setPitch(e.getPitch()+(float)Math.sin(t*3.1)*amount*.5F);
    }
}
