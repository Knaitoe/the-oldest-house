package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Per-explorer visit locks and dormant source doors; shared native doors retain other players' access. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class VignetteGate {
    private static final String ID="vignette_thresholds_0427";
    private VignetteGate(){}
    private static CompoundTag own(LabyrinthData d,UUID id){return d.state(ID).getCompound(id.toString()).copy();}
    private static void save(LabyrinthData d,UUID id,CompoundTag own){var all=d.state(ID);all.put(id.toString(),own);d.setState(ID,all);}
    public static void begin(ServerPlayer p,LabyrinthPlace place){if(p.isSpectator()||!place.isVignette())return;WitnessAccount.begin(p,place);var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());o.putString("Place",place.id());o.putBoolean("Inside",false);o.putBoolean("VisitInteraction",false);o.remove("Failed");o.putLong("Began",p.serverLevel().getGameTime());save(d,p.getUUID(),o);}
    /**
     * 0.4.599 (owner, 9 November: "once a player fails a vignette that simply bounces them to the exit, if the vignette is retriable, it
     * should disappear and lead to a normal hallway."). The story this reader stands in has failed them this visit: thrown them back
     * to the hall under a fade (LabyrinthDoors.sendBack), stood them at its own door, or left them nothing to do but go. It is marked
     * here. Once they are out, the door that led to it is dealt an ordinary hall (LabyrinthDoors.gone), and the story, still theirs
     * to finish, is the dealer's to put behind some other door later. A reader who steps back out of a story of their own accord is as
     * before: the door is quiet behind them till they have been away. A story they have finished is a passage, as it was.
     */
    public static void failed(ServerPlayer p){
        if(p.isSpectator())return;var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var place=LabyrinthPlace.byId(o.getString("Place"));if(place==null||!place.isVignette()||o.getBoolean("Failed"))return;
        // The gate's place is only as fresh as the last story this reader came into: it has to be the one they are standing in.
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();if(origin==null||LabyrinthDoors.placeOf(p,origin)!=place)return;
        o.putBoolean("Failed",true);save(d,p.getUUID(),o);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"story_put_out","place",place.id(),"depth",d.returnDepth(p.getUUID()));
    }
    /**
     * 0.4.762 (the owner: forced ejections escaped the unfinished-return tracking). LabyrinthDoors.sendBack is about to carry this
     * reader out of a story without its door. A failure has already said so (story_put_out); any other carrying out (a fade at the end,
     * a sleep, a death averted) is logged here as story_sent_back, resolved or not, so the journey log sees the stay end and, if the
     * story is unfinished, that it is one to find again.
     */
    static void carriedOut(ServerPlayer p){
        if(p.isSpectator())return;var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var place=LabyrinthPlace.byId(o.getString("Place"));if(place==null||!place.isVignette()||o.getBoolean("Failed"))return;
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();if(origin==null||LabyrinthDoors.placeOf(p,origin)!=place)return;
        var story=WitnessAccount.Story.of(place.id());if(story==null)return;
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"story_sent_back","place",place.id(),"resolved",WitnessAccount.has(d,p.getUUID(),story),"depth",d.returnDepth(p.getUUID()));
    }
    /** Whether the story this reader is in has failed them this visit, and they are not out of it yet. */
    public static boolean failedVisit(LabyrinthData d,UUID id){return own(d,id).getBoolean("Failed");}
    /** 0.4.599: LabyrinthDoors.sendBack has put this reader in the hall. If their story failed them, it is gone from the door they took. */
    static void sentBack(ServerPlayer p,LabyrinthData.Waypoint back){
        var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());if(!o.getBoolean("Failed"))return;
        var place=LabyrinthPlace.byId(o.getString("Place"));o.remove("Place");o.remove("Failed");o.putBoolean("Inside",false);save(d,p.getUUID(),o);
        if(place==null||back==null||!back.door()||LabyrinthDealer.finishedFor(d,p.getUUID(),place))return;
        var source=d.doorAt(back.dimension(),BlockPos.containing(back.pos()));if(source!=null&&!source.command)LabyrinthDoors.gone(p,source,false);
    }
    public static void stepped(ServerPlayer p,LabyrinthPlace place){if(!place.isVignette())return;var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());if(place.id().equals(o.getString("Place"))&&!o.getBoolean("Inside")){o.putBoolean("Inside",true);save(d,p.getUUID(),o);var entry=place==LabyrinthPlace.BORRASCA?BorrascaSites.entry(p):place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN?LiteraryCopies.entry(p,place):d.door(place.entryDoorId());ExpeditionRhythm.offerRetreat(p,place,entry);}}
    public static void interaction(ServerPlayer p,LabyrinthPlace place){var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());if(place.id().equals(o.getString("Place"))){o.putBoolean("VisitInteraction",true);save(d,p.getUUID(),o);}}
    public static boolean exitLocked(ServerPlayer p,LabyrinthData.Door door){
        if(p.isCreative()||p.isSpectator()||!p.isAlive())return false;var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var place=LabyrinthPlace.byId(o.getString("Place"));
        // Ordinary unfinished visits allow retreat. Existing controllers own the closed-well
        // vigil, Goatman door and phone drop; only an armed Holloway pursuit commits here.
        if(place!=LabyrinthPlace.HOLLOWAY_CAMP||!o.getBoolean("Inside")||!place.isReturnDoor(door.id))return false;
        var h=HollowayVignette.personal(d,p.getUUID());return h.getBoolean("Run")&&!h.getBoolean("Escaped");
    }
    public static boolean dormant(LabyrinthData d,UUID id,LabyrinthData.Door door){return own(d,id).getCompound("Dormant").contains(door.id);}
    /** The story place a dormant door leads back to, or an empty string. */
    public static String dormantPlace(LabyrinthData d,UUID id,LabyrinthData.Door door){return own(d,id).getCompound("Dormant").getString(door.id);}
    public static void redealt(LabyrinthData d,UUID id,LabyrinthData.Door door){var o=own(d,id);var dormant=o.getCompound("Dormant");if(!dormant.contains(door.id))return;dormant.remove(door.id);o.put("Dormant",dormant);save(d,id,o);}
    public static void departed(ServerPlayer p,LabyrinthData.Waypoint back,LabyrinthData.Door entry){
        var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var place=LabyrinthPlace.byId(o.getString("Place"));if(place==null||!place.isVignette()||!(place.isReturnDoor(entry.id)||place==LabyrinthPlace.BORRASCA&&entry.id.equals("borrasca/"+p.getUUID()+"/entry")))return;
        var story=WitnessAccount.Story.of(place.id());
        if(story!=null&&!p.isSpectator())io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"story_leave","place",place.id(),"resolved",story!=null&&WitnessAccount.has(d,p.getUUID(),story),"depth",d.returnDepth(p.getUUID()));
        // 0.4.599: a story that failed this reader, and that they can try again, is gone from the door: it leads to an ordinary hall.
        boolean out=o.getBoolean("Failed")&&!LabyrinthDealer.finishedFor(d,p.getUUID(),place);
        var source=back.door()?d.doorAt(back.dimension(),BlockPos.containing(back.pos())):null;if(source!=null&&source.command)source=null;
        o.remove("Place");o.remove("Failed");o.putBoolean("Inside",false);save(d,p.getUUID(),o);
        if(source==null||out&&LabyrinthDoors.gone(p,source,back.equals(d.peekReturn(p.getUUID()))))return;
        // One they stepped back out of is quiet behind it till they have been away.
        o=own(d,p.getUUID());var dormant=o.getCompound("Dormant");dormant.putString(source.id,place.id());o.put("Dormant",dormant);var away=o.getCompound("Away");away.remove(source.id);o.put("Away",away);save(d,p.getUUID(),o);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());o.remove("Place");o.remove("Failed");o.putBoolean("Inside",false);save(d,p.getUUID(),o);}}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%20!=0)return;
        for(var level:e.getServer().getAllLevels())for(var p:List.copyOf(level.players())){
            var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var dormant=o.getCompound("Dormant");var away=o.getCompound("Away");var rediscover=new ArrayList<LabyrinthData.Door>();if(dormant.isEmpty())continue;
            for(String key:List.copyOf(dormant.getAllKeys())){var door=d.door(key);if(door==null)continue;
                if(!door.dimension.equals(p.level().dimension())||p.distanceToSqr(door.lower.getCenter())>144)away.putBoolean(key,true);
                else if(away.getBoolean(key)&&p.distanceToSqr(door.lower.getCenter())<36){dormant.remove(key);away.remove(key);rediscover.add(door);}
            }
            o.put("Dormant",dormant);o.put("Away",away);save(d,p.getUUID(),o);
            for(var door:rediscover)if(LabyrinthData.DEALT.equals(door.destination))LabyrinthDealer.deal(d,p.getUUID(),List.of(door),null,p.getRandom());
        }
    }
    public static void explain(ServerPlayer p){p.displayClientMessage(Component.literal("The service latch is farther inside. Footsteps follow the light."),true);}
}
