package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.MovieNightPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Movie Night on the reader's side (0.4.102): a burst lights the whole lake like lightning; the shark lifts this reader's canoe. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class MovieNightClient {
    private static long rockStart=-1000,flashAt=-1000;private static int rockTicks;
    private MovieNightClient(){}
    public static void accept(MovieNightPayload payload){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(payload.kind()==MovieNightPayload.FLASH){mc.level.setSkyFlashTime(Math.max(mc.level.getSkyFlashTime(),payload.ticks()));flashAt=mc.level.getGameTime();}
        else if(payload.kind()==MovieNightPayload.ROCK){rockStart=mc.level.getGameTime();rockTicks=Math.max(1,payload.ticks());}
    }
    /** Ticks since the last burst lit the lake, for the screen's own wash of light. */
    public static float sinceFlash(float partial){var mc=Minecraft.getInstance();return mc.level==null?1000:mc.level.getGameTime()-flashAt+partial;}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.level==null)return;float age=(float)(mc.level.getGameTime()-rockStart+e.getPartialTick());
        if(age<0||age>rockTicks)return;float fade=1-age/rockTicks,lift=Mth.sin(Mth.clamp(age/6F,0,1)*Mth.PI);
        // A hard lift from under the hull, then the canoe rolling back and forth as it settles.
        e.setRoll(e.getRoll()+Mth.sin(age*.55F)*7*fade*fade);e.setPitch(e.getPitch()-lift*4+Mth.sin(age*.4F+1)*2*fade);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){rockStart=-1000;flashAt=-1000;MovieFilm.release();}
}
