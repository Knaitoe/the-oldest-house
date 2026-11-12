package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.PellPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 0.4.526, the Pell house: the house's own hour on this client (PellPayload), in its scene clock (mode 22). Between packets the hour
 * goes on at the rate it was sent with, a tick of the day to a tick of the game, so the sun moves smoothly; held, it stays put.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellClient {
    private PellClient(){}
    private static long time=-1,at,ticks;private static int rate;
    public static void accept(PellPayload payload){time=payload.time();rate=payload.rate();at=ticks;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){ticks++;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){time=-1;}
    /** The house's hour now, as far as this client knows it. */
    public static long current(){return time<0?-1:time+(long)rate*(ticks-at);}
    /** At the Pell house (mode 22), its own hour; anywhere else, what it would be. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==22?current():ordinary;}
}
