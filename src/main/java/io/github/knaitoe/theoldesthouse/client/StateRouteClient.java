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
    // ------------------------------------------------------------------ 0.4.336, struck: what is left on the tar, then home
    private static net.minecraft.world.entity.Entity marker;private static int shotLeft,shotLength;private static net.minecraft.world.phys.Vec3 corpse;private static float corpseYaw;private static boolean hidGui;
    /** Whether this client is holding on the body: no death screen, no hand, no HUD. */
    public static boolean framing(){return shotLeft>0;}
    /** The reader has been struck: hold the camera on where the body lies, coming in a little, in place of the death screen. */
    public static void wreck(io.github.knaitoe.theoldesthouse.network.StateRouteWreckPayload payload){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        corpse=new net.minecraft.world.phys.Vec3(payload.x(),payload.y(),payload.z());corpseYaw=payload.yaw();shotLength=shotLeft=Math.max(20,payload.ticks());
        if(marker==null)marker=net.minecraft.world.entity.EntityType.MARKER.create(mc.level);if(marker==null){shotLeft=0;return;}
        place(0,true);mc.setCameraEntity(marker);
        if(!mc.options.hideGui){mc.options.hideGui=true;hidGui=true;}
    }
    /** Above and to one side of the body, looking down at it, closing in over the shot (t from 0 to 1). */
    private static void place(float t,boolean jump){
        double ang=Math.toRadians(corpseYaw+135),dist=net.minecraft.util.Mth.lerp(t,4.6,2.8),high=net.minecraft.util.Mth.lerp(t,3.4,2.0);
        double cx=corpse.x+Math.sin(ang)*dist,cz=corpse.z+Math.cos(ang)*dist,cy=corpse.y+high;
        double dx=corpse.x-cx,dy=corpse.y+.2-cy,dz=corpse.z-cz;
        float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz)),pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz)));
        if(jump){marker.moveTo(cx,cy,cz,yaw,pitch);return;}
        marker.xo=marker.getX();marker.yo=marker.getY();marker.zo=marker.getZ();marker.yRotO=marker.getYRot();marker.xRotO=marker.getXRot();
        marker.setPos(cx,cy,cz);marker.setYRot(yaw);marker.setXRot(pitch);
    }
    private static void endShot(){
        var mc=Minecraft.getInstance();shotLeft=0;
        if(marker!=null){if(mc.getCameraEntity()==marker&&mc.player!=null)mc.setCameraEntity(mc.player);marker.discard();marker=null;}
        if(hidGui){mc.options.hideGui=false;hidGui=false;}
    }
    @SubscribeEvent public static void shot(ClientTickEvent.Post e){
        if(shotLeft<=0)return;var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||marker==null){endShot();return;}
        shotLeft--;place(1-shotLeft/(float)shotLength,false);
        // A strike that did not kill (refused, or a totem) ends the shot at once.
        if(shotLength-shotLeft>10&&!mc.player.isDeadOrDying()){endShot();return;}
        if(shotLeft==16)HouseFadeState.begin(14,40,30);
        if(shotLeft<=0){if(mc.player.isDeadOrDying())mc.player.respawn();endShot();}
    }
    /** No death screen while the camera holds on the body: the reader is respawned when it is over. */
    @SubscribeEvent public static void deathScreen(ScreenEvent.Opening e){if(shotLeft>0&&e.getNewScreen() instanceof net.minecraft.client.gui.screens.DeathScreen)e.setCanceled(true);}
    /** The dying body is not drawn where the road has left its own: the wreck stands in for it, for everyone. */
    @SubscribeEvent public static void dying(RenderPlayerEvent.Pre e){
        var player=e.getEntity();if(!player.isDeadOrDying())return;var s=scene();if(s==null)return;
        for(var w:s.wrecks)if(w.reader().equals(player.getUUID())){e.setCanceled(true);return;}
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){endShot();}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        if(!onRoad())return;float amount=shudder((float)e.getPartialTick())*Comfort.effects();if(amount<=0)return;
        var mc=Minecraft.getInstance();double t=mc.player.tickCount+e.getPartialTick();
        e.setRoll(e.getRoll()+(float)Math.sin(t*2.7)*amount*1.6F);e.setPitch(e.getPitch()+(float)Math.sin(t*3.1)*amount*.5F);
    }
}
