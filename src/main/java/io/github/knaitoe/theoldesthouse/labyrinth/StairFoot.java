package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * 0.4.660, the foot of the stair (docs/STAIR_FOOT_0_4_660.md). A reader who comes off the last tread into the chamber before the cell
 * gets no fanfare: the descent's score dies away on the client (StaircaseScore), the growls that followed them down stop
 * (StaircaseAtmosphere), and after a hush the creature growls once, from the cell, for that reader alone. Once a reader, ever; a reader
 * who goes back up before the hush is over hears it the next time they have stood there that long. No words.
 */
public final class StairFoot {
    /** How long a reader stands at the foot, all at once, before the growl. */
    public static final int HUSH=100;
    /** The reader's own record: heard the growl at the foot. */
    public static final String HEARD="FootGrowl0660";
    /** When each reader came off the stair this time, and when they were last seen there (not saved: after a restart, a log-out or
     *  a second away, the hush starts again). */
    private static final Map<UUID,long[]> SINCE=new HashMap<>();
    private StairFoot(){}

    /** From the finale's tick for a reader on the stair: the hush, then the growl. True when it growls now. */
    public static boolean tick(ServerPlayer p,BlockPos origin,CompoundTag record,long now){
        if(!FinaleArchitecture.atFoot(origin,p.position())){SINCE.remove(p.getUUID());return false;}
        if(record.getBoolean(HEARD))return false;
        long[] t=SINCE.get(p.getUUID());if(t==null||now-t[1]>20||now<t[1]){t=new long[]{now,now};SINCE.put(p.getUUID(),t);}
        t[1]=now;if(now-t[0]<HUSH)return false;
        record.putBoolean(HEARD,true);SINCE.remove(p.getUUID());
        var at=FinaleArchitecture.cellSpot(origin).add(0,2,0);
        var voice=Holder.direct(SoundEvent.createVariableRangeEvent(Growl.Kind.NEAR.sound()));
        p.connection.send(new ClientboundSoundPacket(voice,SoundSource.HOSTILE,at.x,at.y,at.z,2.4F,.8F,p.getRandom().nextLong()));
        return true;
    }
    /** How long this reader has stood at the foot this time, or -1. For tests. */
    public static long standing(ServerPlayer p,long now){long[] t=SINCE.get(p.getUUID());return t==null?-1:now-t[0];}
    public static void forget(UUID reader){SINCE.remove(reader);}
}
