package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.CornerPayload;
import net.minecraft.world.level.Level;

/** 0.4.408, The House on the Corner: the street at night for a reader who comes back after the last room; siesta otherwise. */
public final class CornerClient {
    private CornerClient(){}
    private static long time=-1;
    public static void accept(CornerPayload payload){time=payload.time();}
    public static long current(){return time;}
    /** In the street's scene clock (mode 19), the hour the server has sent this reader. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==19?time:ordinary;}
}
