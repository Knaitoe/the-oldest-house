package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.LastSeenTimePayload;
import net.minecraft.world.level.Level;

/** 0.4.669, Last Seen: the park's hour, as the server sends it to this reader; the morning until it says otherwise. */
public final class LastSeenClient {
    private LastSeenClient(){}
    private static long time=-1;
    public static void accept(LastSeenTimePayload payload){time=payload.time();}
    public static long current(){return time;}
    /** In the park's scene clock (mode 12), the hour the server has sent this reader. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==12?time:ordinary;}
}
