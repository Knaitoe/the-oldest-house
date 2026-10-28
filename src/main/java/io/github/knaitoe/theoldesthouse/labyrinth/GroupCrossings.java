package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Friends who cross one door together go into the same vignette (owner, October 28). The first through a dealt door
 * leaves it open behind them for the rest of the group: whoever follows through the same door soon after arrives in the
 * same scene, unless they have finished it or belong to another thread of it. When the first to cross has friends
 * waiting at the door with them and some of them cannot share what lies behind it, the House deals a vignette none of
 * them has started; if there is none, the group splits and each reader keeps their own door.
 */
public final class GroupCrossings {
    /** How long a crossing waits for the rest of the group, in ticks. */
    public static final int WINDOW=600;
    /** Who counts as waiting at the door with the first to cross, in blocks. */
    public static final double NEAR=4;
    private record Crossing(UUID leader,LabyrinthPlace place,int tick){}
    private static final Map<String,Crossing> RECENT=new HashMap<>();
    private GroupCrossings(){}
    private static String key(LabyrinthData.Door door){return door.dimension.location()+"|"+door.id+"|"+door.lower.asLong();}

    /** Before a dealt door is read: follow whoever just went through it, or, first through, find a vignette the group can share. */
    public static void before(ServerPlayer p,LabyrinthData.Door door){
        if(p.isSpectator()||door.command||!LabyrinthData.DEALT.equals(door.destination))return;
        var data=LabyrinthData.get(p.server);int now=p.server.getTickCount();var me=p.getUUID();
        RECENT.values().removeIf(c->now-c.tick()>WINDOW||now<c.tick());
        var led=RECENT.get(key(door));
        if(led!=null&&!led.leader().equals(me)){
            if(canJoin(data,me,led.place(),VignetteThreads.threadId(data,led.place(),led.leader())))data.deal(me,door,led.place().id(),false,false);
            return; // otherwise the group splits here, and this reader keeps their own door
        }
        var deal=data.deal(me,door);var place=deal==null?null:LabyrinthPlace.byId(deal.place());
        if(place==null||!place.isVignette())return;
        var group=companions(p,door);if(group.isEmpty())return;
        var thread=VignetteThreads.threadId(data,place,me);
        if(SharedVignettes.followable(place)&&group.stream().allMatch(q->canJoin(data,q.getUUID(),place,thread)))return;
        var common=new ArrayList<>(LabyrinthDealer.vignettesAvailable(data,me));
        common.removeIf(v->!SharedVignettes.followable(v)||started(data,me,v));
        for(var q:group){var theirs=LabyrinthDealer.vignettesAvailable(data,q.getUUID());common.removeIf(v->!theirs.contains(v)||started(data,q.getUUID(),v));}
        if(common.isEmpty())return;
        common.sort(Comparator.comparingInt(LabyrinthPlace::slot));
        data.deal(me,door,common.get(Math.floorMod(key(door).hashCode(),common.size())).id(),false,false);
    }
    /** The first through a dealt door into a shared vignette holds it for the group. */
    public static void crossed(ServerPlayer p,LabyrinthData.Door door,@Nullable LabyrinthPlace place){
        if(p.isSpectator()||door.command||!SharedVignettes.followable(place))return;
        int now=p.server.getTickCount();var led=RECENT.get(key(door));
        if(led==null||now-led.tick()>WINDOW||now<led.tick()||led.leader().equals(p.getUUID()))RECENT.put(key(door),new Crossing(p.getUUID(),place,now));
    }

    /** Whether this reader can go into the leader's scene with them: not finished, and no other thread of it. */
    public static boolean canJoin(LabyrinthData data,UUID reader,LabyrinthPlace place,@Nullable String leaderThread){
        if(!SharedVignettes.followable(place)||LabyrinthDealer.finishedFor(data,reader,place))return false;
        var mine=VignetteThreads.threadId(data,place,reader);
        if(mine!=null)return mine.equals(leaderThread);
        return LabyrinthDealer.vignettesAvailable(data,reader).contains(place);
    }
    private static boolean started(LabyrinthData data,UUID reader,LabyrinthPlace place){
        var story=WitnessAccount.Story.of(place.id());
        return story!=null?WitnessAccount.begun(data,reader,story):data.visited(reader).contains(place.id());
    }
    private static List<ServerPlayer> companions(ServerPlayer p,LabyrinthData.Door door){
        var at=Vec3.atBottomCenterOf(door.lower);var out=new ArrayList<ServerPlayer>();
        for(var q:p.serverLevel().players())if(q!=p&&q.isAlive()&&!q.isSpectator()&&q.position().distanceTo(at)<=NEAR)out.add(q);
        return out;
    }
    /** Fixtures only. */
    public static void forget(){RECENT.clear();}
}
