package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.747: a spectator sees the cast of the reader they are watching. That reader is the one whose camera they have taken, or
 * otherwise the nearest living reader in the same level within {@link #RANGE} blocks. What that reader is sent of a private
 * cast, the spectator is sent too; with nobody to watch, a spectator is sent nobody's private cast. The client's renderers
 * follow the same choice (client.SpectatorView).
 */
public final class SpectatorWatch {
    private SpectatorWatch(){}
    public static final double RANGE=64;
    public static @Nullable ServerPlayer watched(ServerPlayer s){
        if(!s.isSpectator())return null;
        if(s.getCamera() instanceof ServerPlayer c&&c!=s&&!c.isSpectator()&&c.isAlive())return c;
        ServerPlayer best=null;double d=RANGE*RANGE;
        for(var p:s.serverLevel().players()){if(p==s||p.isSpectator()||!p.isAlive())continue;double e=p.distanceToSqr(s);if(e<d){d=e;best=p;}}
        return best;
    }
    /** Whether this player is sent what belongs to the reader with this id: that reader, or a spectator watching them. */
    public static boolean sees(ServerPlayer p,@Nullable UUID reader){
        if(reader==null)return false;if(reader.equals(p.getUUID()))return true;
        var w=watched(p);return w!=null&&reader.equals(w.getUUID());
    }
}
