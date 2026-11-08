package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.CrossingPayload;
import net.minecraft.world.level.Level;

/** 0.4.351, The Last Crossing: the station's own hour on this client (morning, the day running to dusk), sent by the server. */
public final class CrossingClient {
    private CrossingClient(){}
    private static long time=-1;
    public static void accept(CrossingPayload payload){time=payload.time();}
    /** In the station's scene clock (mode 17), the hour its day has reached. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==17?time:ordinary;}
}
