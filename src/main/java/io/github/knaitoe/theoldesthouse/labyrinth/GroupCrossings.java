package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Friends who cross one door together go into the same vignette (owner, October 28). The first through a dealt door
 * leaves it open behind them for the rest of the group: whoever follows through the same door soon after arrives in the
 * same scene, unless they have finished it or belong to another thread of it. When the first to cross has friends
 * waiting at the door with them and some of them cannot share what lies behind it, the House deals a vignette none of
 * them has started; if there is none, the group splits and each reader keeps their own door.
 * <p>
 * 0.4.270 (the owner: "Add a subtle indication that friends can follow through this door, plus feedback when private scenes or
 * different story progress will separate them"): while the door is held, a friend near it who can follow sees a few motes drift at
 * it, seen by them alone, and is told once, quietly, who went through and that the way is still open; a friend who cannot follow is
 * told that instead. Whoever is split from the group by the door is told why, after the crossing, on the action bar.
 * <p>
 * 0.4.400, the owner: "room crossings don't allow time for other players to join?" Only doors into stories were held: a door onto an
 * ordinary way on (a hall, a trial, a room to rest in) was dealt to each reader by their own route, so a friend who came another way,
 * or was further down, went through the same door a moment later and came out somewhere else. Now every dealt door is held for
 * WINDOW behind the first through it: whoever follows through it in that time comes out in the same room. A way on is anyone's to
 * follow into; stories keep their own rules (finished, another thread, one at a time).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GroupCrossings {
    /** How long a crossing waits for the rest of the group, in ticks. */
    public static final int WINDOW=600;
    /** Who counts as waiting at the door with the first to cross, in blocks. */
    public static final double NEAR=4;
    /** How near the held door a friend sees its motes, and how near they are told about it, in blocks. */
    public static final double SEE=6,TELL=4;
    /** How long after crossing a reader is told why the group split, in ticks: past the crossing's own fade. */
    public static final int AFTER=40;
    private record Crossing(UUID leader,String name,LabyrinthPlace place,int tick,ResourceKey<Level> dimension,BlockPos at,Set<UUID> told){}
    private static final Map<String,Crossing> RECENT=new HashMap<>();
    /** Lines owed to readers, each with the tick it is due. */
    private record Owed(String line,int due){}
    private static final Map<UUID,List<Owed>> OWED=new HashMap<>();
    private GroupCrossings(){}
    private static String key(LabyrinthData.Door door){return door.dimension.location()+"|"+door.id+"|"+door.lower.asLong();}

    /** Before a dealt door is read: follow whoever just went through it, or, first through, find a vignette the group can share. */
    public static void before(ServerPlayer p,LabyrinthData.Door door){
        if(p.isSpectator()||door.command||!LabyrinthData.DEALT.equals(door.destination))return;
        var data=LabyrinthData.get(p.server);int now=p.server.getTickCount();var me=p.getUUID();
        RECENT.values().removeIf(c->!held(p.server,c,now));
        var led=RECENT.get(key(door));
        if(led!=null&&!led.leader().equals(me)){
            if(follows(p,led))data.deal(me,door,led.place().id(),false,false);
            else owe(p,why(p,led),AFTER); // otherwise the group splits here, and this reader keeps their own door
            return;
        }
        var deal=data.deal(me,door);var place=deal==null?null:LabyrinthPlace.byId(deal.place());
        if(place==null||!place.isVignette())return;
        // 0.4.244: a finished scene coming back as its aftermath is the returning reader's alone; the others keep their own doors.
        if(Aftermath.returning(data,me,place))return;
        var group=companions(p,door);if(group.isEmpty())return;
        var thread=VignetteThreads.threadId(data,place,me);
        if(SharedVignettes.followable(place)&&group.stream().allMatch(q->canJoin(data,q.getUUID(),place,thread)))return;
        var common=new ArrayList<>(LabyrinthDealer.vignettesAvailable(data,me));
        common.removeIf(v->!SharedVignettes.followable(v)||started(data,me,v));
        // 0.4.401: the yard's stories only off the yard (YardStretch).
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();
        if(!YardStretch.place(origin==null?null:LabyrinthPlaces.placeAt(origin,door.lower)))common.removeIf(YardStretch.STORIES::contains);
        for(var q:group){var theirs=LabyrinthDealer.vignettesAvailable(data,q.getUUID());common.removeIf(v->!theirs.contains(v)||started(data,q.getUUID(),v));}
        if(common.isEmpty()){
            // The group splits here: the first through is told, and so is everyone at the door who cannot follow, and why.
            boolean alone=!SharedVignettes.followable(place);
            owe(p,alone?"What is behind this door is for one at a time. The others will find their own way.":"Not everyone at the door could go where it leads. The others will find their own way.",AFTER);
            for(var q:group)if(alone||!canJoin(data,q.getUUID(),place,thread))owe(q,alone?p.getGameProfile().getName()+" went where only one can go. Your door will lead you elsewhere.":p.getGameProfile().getName()+" went where you cannot follow. Your door will lead you elsewhere.",0);
            return;
        }
        common.sort(Comparator.comparingInt(LabyrinthPlace::slot));
        data.deal(me,door,common.get(Math.floorMod(key(door).hashCode(),common.size())).id(),false,false);
    }
    /** The first through a dealt door into a shared vignette holds it for the group. */
    public static void crossed(ServerPlayer p,LabyrinthData.Door door,@Nullable LabyrinthPlace place){
        if(p.isSpectator()||door.command||!(SharedVignettes.followable(place)||wayOn(place)))return;
        // 0.4.244: nobody follows a reader into the aftermath of a scene they finished.
        if(Aftermath.returning(LabyrinthData.get(p.server),p.getUUID(),place))return;
        int now=p.server.getTickCount();var led=RECENT.get(key(door));
        if(led==null||!held(p.server,led,now)||led.leader().equals(p.getUUID()))RECENT.put(key(door),new Crossing(p.getUUID(),p.getGameProfile().getName(),place,now,door.dimension,door.lower,new HashSet<>()));
    }

    // ------------------------------------------------------------------ 0.4.270, what friends at the door are shown and told
    /** Why this reader cannot follow the one who went through: they have finished it, or their own story there went another way. */
    static String why(LabyrinthData data,UUID reader,Crossing led){
        if(LabyrinthDealer.finishedFor(data,reader,led.place())||Aftermath.returning(data,reader,led.place()))return "You have already been where "+led.name()+" went. This door leads you elsewhere.";
        if(!SharedVignettes.afresh(led.place())&&VignetteThreads.threadId(data,led.place(),reader)!=null)return led.name()+" went into a story yours has already parted from. This door leads you elsewhere.";
        return led.name()+" went where you cannot follow. This door leads you elsewhere.";
    }
    /** The same for a reader at the door, who may also be too late for a scene that is under way (0.4.405, the Goatman's evening). */
    static String why(ServerPlayer q,Crossing led){
        var data=LabyrinthData.get(q.server);
        if(follows(data,q.getUUID(),led)){var late=late(q,led);if(late!=null)return late;}
        return why(data,q.getUUID(),led);
    }
    /** Why a scene under way will not take this reader now, or null: the Goatman's evening once supper has started or night has fallen. */
    static @Nullable String late(ServerPlayer q,Crossing led){return led.place()==LabyrinthPlace.GOATMAN?GoatmanVignette.underway(q,led.name()):null;}
    /** Whether this reader at the door can follow the one who went through, now. */
    static boolean follows(ServerPlayer q,Crossing led){return follows(LabyrinthData.get(q.server),q.getUUID(),led)&&late(q,led)==null;}
    /** The held door near this reader, if any, and whether they could follow through it. */
    record Near(Crossing crossing,boolean canFollow,double distance){}
    @Nullable static Near near(ServerPlayer q){
        if(q.isSpectator()||!q.isAlive())return null;var data=LabyrinthData.get(q.server);int now=q.server.getTickCount();Near best=null;
        for(var c:RECENT.values()){
            if(!held(q.server,c,now)||c.leader().equals(q.getUUID())||!q.level().dimension().equals(c.dimension()))continue;
            double d=q.position().distanceTo(Vec3.atBottomCenterOf(c.at()));if(d>SEE||best!=null&&d>=best.distance())continue;
            best=new Near(c,follows(q,c),d);
        }
        return best;
    }
    /** What this reader is told as they come to a held door: who went through, and whether the way is open to them. Null if nothing. */
    @Nullable public static String cue(ServerPlayer q){
        var n=near(q);if(n==null||n.distance()>TELL)return null;
        return n.canFollow()?n.crossing().name()+" went through here a moment ago. The way is still open behind them.":why(q,n.crossing());
    }
    /** A line for a reader, now or after a crossing's fade. */
    static void owe(ServerPlayer p,String line,int after){OWED.computeIfAbsent(p.getUUID(),k->new ArrayList<>()).add(new Owed(line,p.server.getTickCount()+after));}
    /** The lines still owed to a reader (tests). */
    public static List<String> owed(UUID id){var o=OWED.get(id);return o==null?List.of():o.stream().map(Owed::line).toList();}
    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int now=server.getTickCount();
        if(!OWED.isEmpty())for(var it=OWED.entrySet().iterator();it.hasNext();){
            var en=it.next();var p=server.getPlayerList().getPlayer(en.getKey());if(p==null){if(en.getValue().stream().allMatch(o->now-o.due()>WINDOW))it.remove();continue;}
            if(LabyrinthDoors.isBusy(p))continue;
            for(var o=en.getValue().iterator();o.hasNext();){var line=o.next();if(line.due()<=now){say(p,line.line());o.remove();break;}}
            if(en.getValue().isEmpty())it.remove();
        }
        if(RECENT.isEmpty()||now%10!=0)return;
        RECENT.values().removeIf(c->!held(server,c,now));
        for(var p:server.getPlayerList().getPlayers()){
            var n=near(p);if(n==null)continue;var c=n.crossing();var level=p.serverLevel();
            // A few motes at the held door, for a friend who can follow it, and only them.
            if(n.canFollow())level.sendParticles(p,ParticleTypes.WHITE_ASH,false,c.at().getX()+.5,c.at().getY()+1,c.at().getZ()+.5,5,.3,.7,.3,0);
            if(n.distance()<=TELL&&c.told().add(p.getUUID())){var line=cue(p);if(line!=null)say(p,line);}
        }
    }

    /**
     * 0.4.401, the owner: "pretty sure it's sending me to where it expects me to be based on depth, not through the actual path of the
     * labyrinth". A friend who came to the door more than half a minute after the first through it was dealt their own room by their own
     * depth. A door now leads where the one who went through it went for as long as they are still beyond it: their way back still runs
     * through this door. Half a minute after they come back through it, it is anyone's own door again.
     */
    static boolean held(MinecraftServer server,Crossing c,int now){
        if(now<c.tick())return false;
        if(now-c.tick()<=WINDOW)return true;
        var leader=server.getPlayerList().getPlayer(c.leader());
        if(leader==null||!leader.isAlive()||leader.isSpectator())return false;
        return LabyrinthData.get(server).hasReturn(c.leader(),w->w.door()&&w.dimension().equals(c.dimension())&&BlockPos.containing(w.pos()).equals(c.at()));
    }
    /** Whether someone else's crossing holds this door for this reader, who can follow it (Folds: nobody folds alone behind a friend). */
    public static boolean ledThrough(ServerPlayer p,LabyrinthData.Door door){
        var led=RECENT.get(key(door));
        return led!=null&&!led.leader().equals(p.getUUID())&&held(p.server,led,p.server.getTickCount())&&follows(p,led);
    }

    /** 0.4.400: an ordinary way on, not a story: a gray room of the interior (a hall, a trial, a room to rest in) that stands once for everyone. */
    public static boolean wayOn(@Nullable LabyrinthPlace place){return place!=null&&place.isGray()&&place.slot()>=0&&!SceneInstances.copyable(place);}
    /** Whether this reader can follow the one who went through: into a way on always, into a story as canJoin says. */
    static boolean follows(LabyrinthData data,UUID reader,Crossing led){
        return wayOn(led.place())||canJoin(data,reader,led.place(),VignetteThreads.threadId(data,led.place(),led.leader()));
    }
    /** Whether this reader can go into the leader's scene with them: not finished, and no other thread of it. */
    public static boolean canJoin(LabyrinthData data,UUID reader,LabyrinthPlace place,@Nullable String leaderThread){
        if(!SharedVignettes.followable(place)||LabyrinthDealer.finishedFor(data,reader,place))return false;
        // 0.4.244: a reader who finished it would find only its aftermath, which is theirs alone: they keep their own door.
        if(Aftermath.returning(data,reader,place))return false;
        // 0.4.405: an evening that begins afresh keeps no threads; an earlier evening sat apart does not keep friends apart now.
        var mine=SharedVignettes.afresh(place)?null:VignetteThreads.threadId(data,place,reader);
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
    public static void forget(){RECENT.clear();OWED.clear();}
    /** Fixtures only: every held crossing as if it had been made {@code ticks} earlier. */
    public static void age(int ticks){RECENT.replaceAll((k,c)->new Crossing(c.leader(),c.name(),c.place(),c.tick()-ticks,c.dimension(),c.at(),c.told()));}
}
