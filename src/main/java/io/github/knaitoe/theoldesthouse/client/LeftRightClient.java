package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.LeftRightFogPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.205, the Left-Right Game on the reader's client: the road's tight fog (from four blocks, gone by eighteen, a wet brown
 * dark), turning over two seconds to bleached salt white on the salt, and to a grey snow-light in the sinkhole.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LeftRightClient {
    private LeftRightClient(){}
    private static int mode;private static float white,snow;
    public static void fog(LeftRightFogPayload p){mode=p.mode();}
    static boolean active(){var mc=Minecraft.getInstance();return mode>0&&mc.player!=null&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.OUTSIDE);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        white=step(white,mode==2?1:0);snow=step(snow,mode==3?1:0);
    }
    private static float step(float v,float to){return v<to?Math.min(to,v+1/40F):Math.max(to,v-1/40F);}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){mode=0;white=snow=0;}
    @SubscribeEvent(priority=EventPriority.LOW) public static void fog(ViewportEvent.RenderFog e){
        if(!active()||e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        var mc=Minecraft.getInstance();if(mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS)||mc.player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS))return;
        float far=18+6*white+4*snow,near=4;
        e.setNearPlaneDistance(Math.min(near,far*.5F));e.setFarPlaneDistance(Math.min(e.getFarPlaneDistance(),far));e.setCanceled(true);
    }
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        if(!active())return;
        // #2A2621, the wet road; #D8DBD6, the salt; a cold grey for the snow.
        float r=.164F,g=.149F,b=.129F;
        r+=(.847F-r)*white;g+=(.859F-g)*white;b+=(.839F-b)*white;
        r+=(.42F-r)*snow;g+=(.45F-g)*snow;b+=(.5F-b)*snow;
        e.setRed(r);e.setGreen(g);e.setBlue(b);
    }
}
