package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * One story per thread (owner, October 28). The readers present on a shared vignette's first visit share one story;
 * whoever from it comes back, alone or together, picks it up where it left off. A reader who arrives with a thread after
 * its first visit joins it partway. When the story ends, the members there watched it, and a member who was elsewhere can
 * come back to its aftermath. Each reader is in at most one thread per vignette.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class VignetteThreads {
    public static final String STATE="vignette_threads_04146";
    /** A thread's own cast carries its thread here, in the actor's saved data, and is seen only by that thread's members. */
    public static final String ACTOR_TAG="VignetteThread0147";
    /** How long a holder may stay away before what they hold goes back where it came from: three days. */
    public static final long RELEASE=72000;
    private VignetteThreads(){}

    private static CompoundTag all(LabyrinthData d,LabyrinthPlace place){return d.stateEntry(STATE,place.id());}
    private static void save(LabyrinthData d,LabyrinthPlace place,CompoundTag all){d.setStateEntry(STATE,place.id(),all);}
    private static @Nullable String find(CompoundTag threads,UUID member){
        for(var id:threads.getAllKeys())if(threads.getCompound(id).getCompound("Members").contains(member.toString()))return id;
        return null;
    }

    /** The thread this reader belongs to in this vignette, or null. */
    public static @Nullable String threadId(LabyrinthData d,LabyrinthPlace place,UUID member){return find(all(d,place).getCompound("Threads"),member);}
    /** A copy of the reader's thread, or an empty tag. */
    public static CompoundTag thread(LabyrinthData d,LabyrinthPlace place,UUID member){
        var id=threadId(d,place,member);return id==null?new CompoundTag():all(d,place).getCompound("Threads").getCompound(id).copy();
    }
    /** A vignette's own story state for this reader's thread is kept under its Story key. */
    public static void saveThread(LabyrinthData d,LabyrinthPlace place,UUID member,CompoundTag thread){
        var all=all(d,place);var threads=all.getCompound("Threads");var id=find(threads,member);if(id==null)return;
        threads.put(id,thread);all.put("Threads",threads);save(d,place,all);
    }
    /** A copy of one thread by its id, or an empty tag. */
    public static CompoundTag threadById(LabyrinthData d,LabyrinthPlace place,String id){return all(d,place).getCompound("Threads").getCompound(id).copy();}
    public static void saveThreadById(LabyrinthData d,LabyrinthPlace place,String id,CompoundTag thread){
        var all=all(d,place);var threads=all.getCompound("Threads");if(!threads.contains(id))return;threads.put(id,thread);all.put("Threads",threads);save(d,place,all);
    }
    /** Whether this player may see a cast that belongs to one thread of a scene. */
    public static boolean sees(ServerPlayer p,String scene,String thread){
        var place=LabyrinthPlace.byId(scene);return place!=null&&thread.equals(threadId(LabyrinthData.get(p.server),place,p.getUUID()));
    }
    /** A thread of this reader alone, when they have none: fixtures, and a reader whose part began before threads. */
    public static void solo(ServerPlayer p,LabyrinthPlace place){
        if(p.isSpectator())return;var d=LabyrinthData.get(p.server);var all=all(d,place);var threads=all.getCompound("Threads");if(find(threads,p.getUUID())!=null)return;
        int next=all.getInt("Next");var id=Integer.toString(next);all.putInt("Next",next+1);
        var thread=new CompoundTag();thread.putLong("Begun",p.server.overworld().getGameTime());thread.putInt("Visit",1);
        var members=new CompoundTag();var mine=new CompoundTag();mine.putString("Joined","founding");mine.putString("Name",p.getGameProfile().getName());members.put(p.getUUID().toString(),mine);thread.put("Members",members);
        threads.put(id,thread);all.put("Threads",threads);save(d,place,all);
    }
    /** Fixtures only: take a reader out of their thread, as if they had never been part of it. */
    public static void forget(LabyrinthData d,LabyrinthPlace place,UUID member){
        var all=all(d,place);var threads=all.getCompound("Threads");var id=find(threads,member);if(id==null)return;
        var thread=threads.getCompound(id);var members=thread.getCompound("Members");members.remove(member.toString());thread.put("Members",members);
        if(member.equals(holder(thread))){thread.remove("Holder");thread.remove("HolderSince");}
        threads.put(id,thread);all.put("Threads",threads);save(d,place,all);
    }
    public static Set<UUID> members(CompoundTag thread){
        var out=new LinkedHashSet<UUID>();for(var key:thread.getCompound("Members").getAllKeys())out.add(UUID.fromString(key));return out;
    }
    public static boolean partway(CompoundTag thread,UUID member){return "partway".equals(thread.getCompound("Members").getCompound(member.toString()).getString("Joined"));}
    public static String name(CompoundTag thread,UUID member){return thread.getCompound("Members").getCompound(member.toString()).getString("Name");}

    private static boolean present(ServerPlayer p,LabyrinthPlace place){return p.isAlive()&&!p.isSpectator()&&IndianLakeRooms.inside(p,place);}
    private static List<ServerPlayer> presentMembers(MinecraftServer server,CompoundTag thread,LabyrinthPlace place,@Nullable UUID except){
        var out=new ArrayList<ServerPlayer>();
        for(var id:members(thread)){if(id.equals(except))continue;var p=server.getPlayerList().getPlayer(id);if(p!=null&&present(p,place))out.add(p);}
        return out;
    }

    /**
     * An actual participant has arrived in a shared vignette. They join the thread of a member already there, keep their
     * own, or begin one. A visit starts when they arrive and nobody from their thread is there; joining after the first
     * visit is joining partway. A member arriving after their thread's ending without them reads its aftermath.
     */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(p.isSpectator()||!p.isAlive()||!SharedVignettes.followable(place))return;
        var d=LabyrinthData.get(p.server);var all=all(d,place);var threads=all.getCompound("Threads");var me=p.getUUID();
        var id=find(threads,me);boolean joining=id==null;
        if(joining)for(var other:p.server.getPlayerList().getPlayers()){
            if(other==p||!present(other,place))continue;var theirs=find(threads,other.getUUID());
            if(theirs!=null&&!threads.getCompound(theirs).getBoolean("Ended")){id=theirs;break;}
        }
        if(id==null){int next=all.getInt("Next");id=Integer.toString(next);all.putInt("Next",next+1);var fresh=new CompoundTag();fresh.putLong("Begun",p.server.overworld().getGameTime());threads.put(id,fresh);}
        var thread=threads.getCompound(id);
        if(presentMembers(p.server,thread,place,me).isEmpty())thread.putInt("Visit",thread.getInt("Visit")+1);
        var members=thread.getCompound("Members");var mine=members.getCompound(me.toString());
        if(joining)mine.putString("Joined",thread.getInt("Visit")<=1?"founding":"partway");
        mine.putString("Name",p.getGameProfile().getName());members.put(me.toString(),mine);thread.put("Members",members);
        threads.put(id,thread);all.put("Threads",threads);save(d,place,all);
        if(thread.getBoolean("Ended"))aftermath(p,place,thread);
    }

    /** Whoever holds the thread's one object, such as the paw. */
    public static void hold(LabyrinthData d,LabyrinthPlace place,ServerPlayer holder){
        var thread=thread(d,place,holder.getUUID());if(thread.isEmpty())return;hold(thread,holder);saveThread(d,place,holder.getUUID(),thread);
    }
    /** The same, on a thread the caller holds and will save. */
    public static void hold(CompoundTag thread,ServerPlayer holder){thread.putUUID("Holder",holder.getUUID());thread.putLong("HolderSince",holder.server.overworld().getGameTime());}
    public static @Nullable UUID holder(CompoundTag thread){return thread.hasUUID("Holder")?thread.getUUID("Holder"):null;}
    /** The holder was here: the clock towards giving the object back starts again. */
    public static void seenHolder(LabyrinthData d,LabyrinthPlace place,ServerPlayer holder){
        var thread=thread(d,place,holder.getUUID());if(!holder.getUUID().equals(holder(thread)))return;
        thread.putLong("HolderSince",holder.server.overworld().getGameTime());saveThread(d,place,holder.getUUID(),thread);
    }

    /** The next beat of this reader's thread needs another member who is not with them: deal them something else. */
    public static boolean waiting(LabyrinthData d,UUID member,LabyrinthPlace place){
        var scene=SharedVignettes.drafted(place);if(scene==null)return false;
        var thread=thread(d,place,member);if(thread.isEmpty()||thread.getBoolean("Ended"))return false;
        var needed=scene.waitsFor(d,thread);return needed!=null&&!needed.equals(member);
    }

    /**
     * A reader ended a shared story as its actor. Members there throughout watched it, and members who joined partway
     * watched from where they came in; both get their own account and the Witness credit where the scene is drafted.
     * Members elsewhere read its aftermath when they come back.
     */
    public static void ended(ServerPlayer actor,WitnessAccount.Story story,String outcome){
        var place=LabyrinthPlace.byId(story.id);if(!SharedVignettes.followable(place))return;
        var d=LabyrinthData.get(actor.server);var thread=thread(d,place,actor.getUUID());var whole=SharedVignettes.drafted(place);
        if(whole!=null&&whole.whole()){ended(actor,story,outcome,place,d,thread,whole);return;}
        if(thread.isEmpty()||thread.getBoolean("Ended"))return;
        thread.putBoolean("Ended",true);thread.putString("Outcome",outcome);thread.putUUID("Actor",actor.getUUID());thread.putString("ActorName",actor.getGameProfile().getName());
        saveThread(d,place,actor.getUUID(),thread);
        var scene=SharedVignettes.drafted(place);if(scene==null)return;
        for(var watcher:presentMembers(actor.server,thread,place,actor.getUUID())){
            var role=scene.role(d,thread,watcher.getUUID(),partway(thread,watcher.getUUID())?VignetteRole.PARTWAY:VignetteRole.WITNESS);
            if(scene.line(role,outcome,actor.getGameProfile().getName())!=null)WitnessAccount.resolveAs(watcher,story,outcome,role,actor.getGameProfile().getName());
        }
    }
    /** A story that belongs to the world: whoever is in the scene when it ends watched it, whatever thread they came in with. */
    private static void ended(ServerPlayer actor,WitnessAccount.Story story,String outcome,LabyrinthPlace place,LabyrinthData d,CompoundTag thread,SharedVignettes.Scene scene){
        String name=actor.getGameProfile().getName();
        if(!thread.isEmpty()&&!thread.getBoolean("Ended")){thread.putBoolean("Ended",true);thread.putString("Outcome",outcome);thread.putUUID("Actor",actor.getUUID());thread.putString("ActorName",name);saveThread(d,place,actor.getUUID(),thread);}
        for(var watcher:actor.server.getPlayerList().getPlayers()){
            if(watcher==actor||!present(watcher,place))continue;
            var role=scene.role(d,thread,watcher.getUUID(),VignetteRole.WITNESS);
            if(scene.line(role,outcome,name)!=null)WitnessAccount.resolveAs(watcher,story,outcome,role,name);
        }
    }
    private static void aftermath(ServerPlayer p,LabyrinthPlace place,CompoundTag thread){
        var story=WitnessAccount.Story.of(place.id());var scene=SharedVignettes.drafted(place);var d=LabyrinthData.get(p.server);
        if(story==null||scene==null||WitnessAccount.has(d,p.getUUID(),story))return;
        String outcome=thread.getString("Outcome"),actor=thread.getString("ActorName");
        var role=scene.role(d,thread,p.getUUID(),VignetteRole.AFTERMATH);if(role!=VignetteRole.AFTERMATH||scene.line(role,outcome,actor)==null)return;
        p.displayClientMessage(Component.literal("It ended here without you."),false);
        WitnessAccount.resolveAs(p,story,outcome,role,actor);
    }

    /** A holder long gone: the object goes back where it came from, so no thread is stuck. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%200==13)releaseOverdue(e.getServer());}
    /** Changes the scene's release makes to the thread it is given are kept. */
    public static void releaseOverdue(MinecraftServer server){
        long now=server.overworld().getGameTime();var d=LabyrinthData.get(server);
        for(var place:SharedVignettes.drafted()){
            var all=all(d,place);var threads=all.getCompound("Threads");boolean changed=false;
            for(var id:threads.getAllKeys()){
                var thread=threads.getCompound(id);var holder=holder(thread);if(holder==null||now-thread.getLong("HolderSince")<RELEASE)continue;
                var p=server.getPlayerList().getPlayer(holder);if(p!=null&&present(p,place))continue;
                SharedVignettes.drafted(place).release(server,thread);thread.remove("Holder");thread.remove("HolderSince");threads.put(id,thread);changed=true;
            }
            if(changed){all.put("Threads",threads);save(d,place,all);}
        }
    }
}
