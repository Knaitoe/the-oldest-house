package io.github.knaitoe.theoldesthouse.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** 0.4.747: whose private cast this client draws: its own player's, or, for a spectator, the reader they watch (labyrinth.SpectatorWatch). */
public final class SpectatorView {
    private SpectatorView(){}
    public static @Nullable UUID viewer(){
        var mc=Minecraft.getInstance();var me=mc.player;if(me==null)return null;if(!me.isSpectator()||mc.level==null)return me.getUUID();
        if(mc.getCameraEntity() instanceof Player c&&c!=me&&!c.isSpectator())return c.getUUID();
        Player best=null;double d=io.github.knaitoe.theoldesthouse.labyrinth.SpectatorWatch.RANGE*io.github.knaitoe.theoldesthouse.labyrinth.SpectatorWatch.RANGE;
        for(var p:mc.level.players()){if(p==me||p.isSpectator()||!p.isAlive())continue;double e=p.distanceToSqr(me);if(e<d){d=e;best=p;}}
        return best==null?me.getUUID():best.getUUID();
    }
    public static boolean is(@Nullable UUID owner){var v=viewer();return owner!=null&&owner.equals(v);}
}
