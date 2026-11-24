package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.WaxMonitorPayload;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.752, the waxworks, part 3b (docs/WAXWORKS_0_4_752.md): the bank of six monitors in the office. A hand on a screen sits the reader
 * down at them: the six feeds, drawn on that reader's client from the museum itself by its six cameras (WaxCameras). The feed they have
 * up is watched: nothing moves in a gallery whose feed somebody is watching. "Something's moved" on a feed notes an irregularity the
 * feed shows; on a feed that shows nothing out of place it notes nothing, and the next mark waits two seconds.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WaxMonitors {
    private WaxMonitors(){}
    /** The feeds, in the order of WaxSite.CAMERAS: the five galleries, then the ring. */
    public static final int RING=5,FEEDS=6,AGAIN=40;
    /** Who is at the monitors, the feed they have up, and when they may next mark one. */
    static final Map<UUID,Integer> WATCHING=new ConcurrentHashMap<>();
    /** Which museum each watcher sits in: a feed watched in one copy holds nothing still in another. */
    static final Map<UUID,Long> WATCHING_AT=new ConcurrentHashMap<>();
    static final Map<UUID,Long> NEXT=new ConcurrentHashMap<>();

    /** A hand on a screen: sat down at the bank, if the reader is in the office. */
    public static void sit(ServerPlayer p,BlockPos b,CompoundTag own){
        if(p.isSpectator())return;
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(!WaxSite.inOffice(r)){Waxworks.cue(p,WaxTexts.MONITOR);return;}
        var n=WaxShift.night(b);
        HousePackets.send(p,new WaxMonitorPayload(b,n!=null&&n.failed,n==null?"11:52":WaxShift.time(n.t)));
        WATCHING.put(p.getUUID(),0);WATCHING_AT.put(p.getUUID(),b.asLong());own.putBoolean("Monitors",true);
    }
    /** From the reader's client: the feed they have up, or that they stood up; and a mark on it. */
    public static void feed(ServerPlayer p,int feed,boolean note){
        var b=WaxNight.site(p);
        if(b==null||feed<0||feed>=FEEDS||!WaxSite.inOffice(p.position().subtract(b.getX(),b.getY(),b.getZ()))){WATCHING.remove(p.getUUID());WATCHING_AT.remove(p.getUUID());return;}
        WATCHING.put(p.getUUID(),feed);WATCHING_AT.put(p.getUUID(),b.asLong());
        if(note)note(p,b,feed);
    }
    /** Whether somebody at this museum's monitors has this feed up. */
    public static boolean watched(BlockPos site,int feed){
        long at=site.asLong();for(var e:WATCHING.entrySet())if(e.getValue()==feed&&WATCHING_AT.getOrDefault(e.getKey(),Long.MIN_VALUE)==at)return true;return false;
    }
    /** The feed this reader has up, or -1. */
    public static int watching(UUID reader){return WATCHING.getOrDefault(reader,-1);}
    /** The feed that shows a place an irregular figure can be: its gallery's at first, the ring's in the corridor, none in the office. */
    public static int feedOf(int who,int stage){return stage==0?WaxSite.FIGURES.get(who).gallery():stage<WaxShift.ARRIVED?RING:-1;}

    static void note(ServerPlayer p,BlockPos b,int feed){
        long now=p.serverLevel().getGameTime();
        if(NEXT.getOrDefault(p.getUUID(),0L)>now)return;
        NEXT.put(p.getUUID(),now+AGAIN);
        var n=WaxShift.night(b);
        if(n==null||n.over||!n.punched.contains(p.getUUID())){Waxworks.cue(p,WaxTexts.NOT_ON_SHIFT);return;}
        for(var i:n.irregular){
            if(i.noticed||feedOf(i.who,i.stage)!=feed)continue;
            WaxShift.noticed(p,n,i,true);return;
        }
        Waxworks.cue(p,WaxTexts.NOTHING_MOVED);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(WATCHING.isEmpty()||e.getServer().getTickCount()%10!=0)return;
        for(var id:List.copyOf(WATCHING.keySet())){
            var p=e.getServer().getPlayerList().getPlayer(id);var b=p==null?null:WaxNight.site(p);
            if(b==null||p.isSpectator()||!WaxSite.inOffice(p.position().subtract(b.getX(),b.getY(),b.getZ()))){WATCHING.remove(id);WATCHING_AT.remove(id);}
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){WATCHING.clear();WATCHING_AT.clear();NEXT.clear();}
}
