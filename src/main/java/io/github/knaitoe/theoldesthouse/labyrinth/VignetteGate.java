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
    public static void begin(ServerPlayer p,LabyrinthPlace place){if(p.isSpectator()||!place.isVignette())return;WitnessAccount.begin(p,place);var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());o.putString("Place",place.id());o.putBoolean("Inside",false);o.putBoolean("VisitInteraction",false);o.putLong("Began",p.serverLevel().getGameTime());save(d,p.getUUID(),o);}
    public static void stepped(ServerPlayer p,LabyrinthPlace place){if(!place.isVignette())return;var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());if(place.id().equals(o.getString("Place"))&&!o.getBoolean("Inside")){o.putBoolean("Inside",true);save(d,p.getUUID(),o);var entry=place==LabyrinthPlace.FAMILY_COPY||place==LabyrinthPlace.OLD_CABIN?LiteraryCopies.entry(p,place):d.door(place.entryDoorId());ExpeditionRhythm.offerRetreat(p,place,entry);}}
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
        var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());var place=LabyrinthPlace.byId(o.getString("Place"));if(place==null||!place.isVignette()||!place.isReturnDoor(entry.id))return;
        var story=WitnessAccount.Story.of(place.id());
        if(story!=null&&!p.isSpectator())io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"story_leave","place",place.id(),"resolved",story!=null&&WitnessAccount.has(d,p.getUUID(),story),"depth",d.returnDepth(p.getUUID()));
        if(back.door()){var source=d.doorAt(back.dimension(),BlockPos.containing(back.pos()));if(source!=null&&!source.command){var dormant=o.getCompound("Dormant");dormant.putString(source.id,place.id());o.put("Dormant",dormant);var away=o.getCompound("Away");away.remove(source.id);o.put("Away",away);}}
        o.remove("Place");o.putBoolean("Inside",false);save(d,p.getUUID(),o);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){var d=LabyrinthData.get(p.server);var o=own(d,p.getUUID());o.remove("Place");o.putBoolean("Inside",false);save(d,p.getUUID(),o);}}
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
