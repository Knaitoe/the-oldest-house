package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class VignetteViewRules {
    private static CameraType previous;private static net.minecraft.client.multiplayer.ClientLevel scene;
    private VignetteViewRules(){}
    private static boolean vignette(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.player.isAlive()||!HouseDimensions.isHouseDimension(mc.level.dimension()))return false;
        var place=LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition());return !mc.player.isSpectator()&&place!=null&&place.isVignette()||mc.level.dimension().equals(HouseDimensions.INTERIOR)&&FinaleArchitecture.contains(HouseSightlineState.origin(),mc.player.blockPosition());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){enforce();}
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre e){enforce();}
    private static void enforce(){var mc=Minecraft.getInstance();if(vignette()){
        if(previous==null){previous=mc.options.getCameraType();scene=mc.level;}mc.options.setCameraType(CameraType.FIRST_PERSON);
    }else if(previous!=null){mc.options.setCameraType(previous);previous=null;scene=null;}}
    /** Outdoor scenes are separate islands of one dimension: distance haze keeps each one's horizon its own. */
    static final float OUTSIDE_FOG_END=110F;
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){var mc=Minecraft.getInstance();
        if(mc.player!=null&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.OUTSIDE)&&e.getMode()==net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN){
            // Only ever draws the haze nearer: a scene's own closer mist always wins.
            if(e.getFarPlaneDistance()>OUTSIDE_FOG_END){e.setFarPlaneDistance(OUTSIDE_FOG_END);e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(),OUTSIDE_FOG_END*.55F));e.setCanceled(true);}
            return;
        }
        if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)||e.getMode()!=net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN)return;
        var b=FinaleArchitecture.base(HouseSightlineState.origin());var p=mc.player.position();if(Math.abs(p.x-b.getX())>FinaleArchitecture.SHAFT_RADIUS||Math.abs(p.z-b.getZ())>FinaleArchitecture.SHAFT_RADIUS||p.y<FinaleArchitecture.LOOP_BOTTOM||p.y>FinaleArchitecture.TOP+15)return;
        e.setNearPlaneDistance(10);e.setFarPlaneDistance(46);e.setCanceled(true);
    }
}
