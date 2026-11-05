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
 * 0.4.310, A Deer in the Road: rain and close fog on the county road for whoever is on it, no music, and a rider in the moving car
 * kept in the seat the car is drawn with (the same synced position for both, as the Backseat does).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DeerRoadClient {
    private DeerRoadClient(){}
    static boolean onRoad(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        return LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==LabyrinthPlace.DEER_ROAD;
    }
    public static boolean precipitating(){return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()&&onRoad();}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){return level==Minecraft.getInstance().level&&onRoad()?1:nativeRain;}
    @SubscribeEvent public static void music(SelectMusicEvent e){if(onRoad())e.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent e){if(onRoad()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC)e.setSound(null);}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(onRoad()){e.setRed(.02F);e.setGreen(.025F);e.setBlue(.035F);}}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){if(onRoad()&&e.getMode()==FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(6);e.setFarPlaneDistance(44);e.setCanceled(true);}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre ignored){
        var mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof DeerRoadSeat seat))return;
        var s=seat.scene();if(s==null||s.car!=DeerRoadScene.DRIVE&&s.car!=DeerRoadScene.SKID)return;
        float partial=mc.getTimer().getGameTimeDeltaPartialTick(false);float f=net.minecraft.util.Mth.clamp(mc.level.getGameTime()+partial-s.syncedAt,0,1);
        var b=s.base();double x=b.getX()+net.minecraft.util.Mth.lerp(f,s.pCarX,s.carX),z=b.getZ()+net.minecraft.util.Mth.lerp(f,s.pCarZ,s.carZ),y=b.getY();
        seat.setPos(x,y,z);seat.xo=x;seat.yo=y;seat.zo=z;
        var at=new net.minecraft.world.phys.Vec3(x,y,z).add(seat.offset());mc.player.setPos(at);mc.player.xo=at.x;mc.player.yo=at.y;mc.player.zo=at.z;
    }
}
