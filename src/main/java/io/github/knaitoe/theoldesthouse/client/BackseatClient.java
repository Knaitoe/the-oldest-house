package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BackseatSeat;
import io.github.knaitoe.theoldesthouse.labyrinth.BackseatCarBlock;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** Only the actual driver uses the first-person cabin view; restore their prior camera afterward. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatClient {
    private static CameraType previous;
    private static BackseatWindSound wind;
    private static ClientLevel world;private static BackseatCarBlock.Car approach;private static int scan;
    private BackseatClient(){}
    private static void restore(){var mc=Minecraft.getInstance();if(previous!=null){mc.options.setCameraType(mc.level!=null&&mc.level.dimension().equals(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR)?CameraType.FIRST_PERSON:previous);previous=null;}if(wind!=null){wind.end();mc.getSoundManager().stop(wind);wind=null;}}
    private static BackseatCarBlock.Car driving(){
        var mc=Minecraft.getInstance();if(mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()||!(mc.player.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(mc.player.getUUID()::equals).isEmpty())return null;
        var car=seat.car();return car!=null&&car.sequenceEnabled&&car.phase>=BackseatCarBlock.Car.REVEAL&&car.phase<=BackseatCarBlock.Car.IMPACT?car:null;
    }
    /** Along the car's rear axis, even while the driver glances toward the mirror. */
    public static double cameraPullback(float partial){
        var mc=Minecraft.getInstance();var car=driving();if(car==null||!mc.options.getCameraType().isFirstPerson())return 0;
        float q=car.phase==BackseatCarBlock.Car.IMPACT?1:car.acceleration(partial);q=q*q*(3-2*q);
        return .18*q*mc.options.screenEffectScale().get();
    }
    private static BackseatCarBlock.Car nearby(){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator())return null;
        if(mc.player.getVehicle() instanceof BackseatSeat seat&&seat.owner().filter(mc.player.getUUID()::equals).isPresent())return seat.car();
        return world==mc.level&&approach!=null&&!approach.isRemoved()&&approach.road&&approach.owner!=null&&approach.owner.equals(mc.player.getUUID())&&mc.player.distanceToSqr(approach.getBlockPos().getCenter())<64*64?approach:null;
    }
    private static boolean road(){var car=nearby();return car!=null&&car.road&&car.phase<BackseatCarBlock.Car.DONE;}
    /** The outside uses the dry void biome; only this reader's open car road receives native precipitation. */
    public static boolean precipitating(){var car=nearby();return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()&&car!=null&&car.road&&car.phase<BackseatCarBlock.Car.LOCKED;}
    public static long time(net.minecraft.world.level.Level level,long nativeTime){return level==Minecraft.getInstance().level&&road()?18000:nativeTime;}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){
        if(level!=Minecraft.getInstance().level)return nativeRain;var car=nearby();if(car==null)return nativeRain;
        return car.phase>=BackseatCarBlock.Car.LOCKED?0:car.road?1:nativeRain;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(world!=mc.level){world=mc.level;approach=null;scan=0;}
        if(mc.level!=null&&mc.player!=null&&scan--<=0){scan=5;approach=null;int cx=mc.player.blockPosition().getX()>>4,cz=mc.player.blockPosition().getZ()>>4;
            for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++){
                var chunk=mc.level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);if(chunk==null)continue;
                for(var block:chunk.getBlockEntities().values())if(block instanceof BackseatCarBlock.Car car&&car.road&&car.owner!=null&&car.owner.equals(mc.player.getUUID())&&mc.player.distanceToSqr(car.getBlockPos().getCenter())<64*64)approach=car;
            }
        }
        if(road())mc.getMusicManager().stopPlaying();
        if(mc.player==null||!mc.player.isAlive()||!(mc.player.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(mc.player.getUUID()::equals).isEmpty()){restore();return;}
        if(previous==null)previous=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);
        var car=driving();
        if(car!=null&&car.phase==BackseatCarBlock.Car.REVEAL){if(wind==null||wind.isStopped()){wind=new BackseatWindSound();wind.tick();mc.getSoundManager().play(wind);}}
        else if(wind!=null){wind.end();mc.getSoundManager().stop(wind);wind=null;}
    }
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){if(e.usedConfiguredFov()&&driving()!=null)e.setFOV(e.getFOV()+5*cameraPullback((float)e.getPartialTick())/.18);}
    @SubscribeEvent public static void music(SelectMusicEvent e){if(road())e.setMusic(null);}
    /** Native vehicle packets can trail the BE's per-tick motion. Use one authoritative
     * movement sample for this original seat, its driver and the rendered cabin. */
    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre ignored){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||!(mc.player.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(mc.player.getUUID()::equals).isEmpty())return;
        var car=seat.car();if(car==null||!car.road||!car.sequenceEnabled||car.phase<BackseatCarBlock.Car.REVEAL||car.phase>BackseatCarBlock.Car.IMPACT)return;
        var root=seat.root();double travel=car.visualTravel(mc.getTimer().getGameTimeDeltaPartialTick(false));
        double x=root.getX()+.5,y=root.getY(),z=root.getZ()+.5-travel;
        seat.setPos(x,y,z);seat.xo=x;seat.yo=y;seat.zo=z;
        var at=new net.minecraft.world.phys.Vec3(x,y,z).add(io.github.knaitoe.theoldesthouse.labyrinth.BackseatCarMesh.DRIVER);
        mc.player.setPos(at);mc.player.xo=at.x;mc.player.yo=at.y;mc.player.zo=at.z;
    }
    @SubscribeEvent public static void music(PlaySoundEvent e){if(road()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC)e.setSound(null);}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(road()){e.setRed(.025F);e.setGreen(.035F);e.setBlue(.04F);}}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){if(road()&&e.getMode()==FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(7);e.setFarPlaneDistance(48);e.setCanceled(true);}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut ignored){restore();BackseatMirror.clear();approach=null;world=null;scan=0;}
}
