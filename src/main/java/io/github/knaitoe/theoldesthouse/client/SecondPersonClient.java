package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.SecondPersonPayload;
import net.minecraft.world.level.Level;

/** 0.4.274, Second Person: the flat's own hour on this client (late summer afternoon to the last morning), sent by the server. */
public final class SecondPersonClient {
    private SecondPersonClient(){}
    private static long time=-1;
    public static void accept(SecondPersonPayload payload){time=payload.time();}
    public static long current(){return time;}
    /** In the flat's scene clock (mode 15), the hour the reader's year has reached. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==15?time:ordinary;}
}
