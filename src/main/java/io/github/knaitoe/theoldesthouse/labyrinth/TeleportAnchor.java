package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.402, the owner: "the game tracks where it expects players to be and not where they actually are. this is especially apparent when
 * teleporting to/from friends".
 * <p>
 * The House knows where a reader is by the doors they came through: their way back, and what lies behind the doors of each room on it.
 * A command that moves a reader (a teleport to a friend or away from one), or a respawn, moved the body and left all of that where it
 * was. The reader stood in their friend's hall while the House still walked them through their own: its doors led where their old
 * route dealt them, the way back out took them to the door they had last come through somewhere else, and their depth was the old
 * depth. Walking into the House again after a respawn carried on from deep inside it.
 * <p>
 * Now a reader the House did not move takes up where they actually are, once they have landed:
 * <ul>
 * <li><b>With a friend in the same room</b>, they take up the friend's way through the House. That covers the way back, each room's
 * doors on it as the friend found them, and the depth. Going back takes them back the way the friend came, and the doors lead where
 * the friend's do.</li>
 * <li><b>Alone in a room</b>, the spot they were moved from becomes the way back (the lights go out and they are returned there). The
 * room's doors are dealt from there.</li>
 * <li><b>In the manor, or out of the House</b>, they have no way back to keep. The next time they go in, they start from the first
 * hall.</li>
 * </ul>
 * The quiet rooms keep their own rules. A move within the same room changes nothing.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TeleportAnchor {
    private TeleportAnchor(){}
    /** Where a reader was before a command moved them (null for a respawn), and when. */
    public record From(@Nullable ResourceKey<Level> dimension, @Nullable Vec3 pos, float yaw, @Nullable LabyrinthPlace place, int tick) {}
    private static final Map<UUID,From> PENDING=new HashMap<>();
    /** How long a move waits for the reader to land before it is let go, in ticks. */
    private static final int GIVE_UP=200;

    @SubscribeEvent public static void command(EntityTeleportEvent.TeleportCommand e){
        if(e.getEntity() instanceof ServerPlayer p&&!p.isSpectator())PENDING.put(p.getUUID(),from(p));
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&!e.isEndConquered())PENDING.put(p.getUUID(),new From(null,null,0F,null,p.server.getTickCount()));
    }
    private static From from(ServerPlayer p){
        var origin=HouseSavedData.get(p.server).houseOrigin();
        var place=origin==null||!HouseDimensions.isHouseDimension(p.level().dimension())?null:LabyrinthDoors.placeOf(p,origin);
        return new From(p.level().dimension(),p.position(),p.getYRot(),place,p.server.getTickCount());
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(PENDING.isEmpty())return;
        var server=e.getServer();int now=server.getTickCount();
        for(var it=PENDING.entrySet().iterator();it.hasNext();){
            var en=it.next();var p=server.getPlayerList().getPlayer(en.getKey());var from=en.getValue();
            if(p==null||now-from.tick()>GIVE_UP){it.remove();continue;}
            if(now==from.tick()||!p.isAlive()||LabyrinthDoors.isBusy(p))continue;
            it.remove();reanchor(p,from);
        }
    }

    /** What a reader moved by something other than the House takes up, where they now stand. Public for the tests. */
    public static String reanchor(ServerPlayer p,From from){
        if(p.isSpectator())return "spectator";
        if(HouseReadSites.contains(p))return "quiet room";
        var server=p.server;var data=LabyrinthData.get(server);var origin=HouseSavedData.get(server).houseOrigin();
        if(origin==null)return "no house";
        var dim=p.level().dimension();
        if(!HouseDimensions.isHouseDimension(dim)||manor(p,origin)){
            // In the manor, or out of the House: nothing of a way back to keep.
            data.clearReturns(p.getUUID());LabyrinthDoors.settle(p,null);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"reanchor","to","manor");return "manor";
        }
        var place=LabyrinthDoors.placeOf(p,origin);
        if(place==null)return "between places";
        if(from.place()==place&&dim.equals(from.dimension()))return "same room";
        var friend=friend(p,place,origin);
        if(friend!=null){
            follow(data,friend.getUUID(),p.getUUID(),place,origin);
            LabyrinthDoors.settle(p,place);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"reanchor","to",place.id(),"with",friend.getGameProfile().getName(),"depth",data.returnDepth(p.getUUID()));
            return "friend";
        }
        // Alone: the spot they were moved from is their way back, and this room's doors are dealt from there.
        if(from.dimension()!=null&&from.pos()!=null&&HouseDimensions.isHouseDimension(from.dimension())&&!manorAt(from,origin))
            data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(from.dimension(),from.pos(),from.yaw(),false));
        else data.clearReturns(p.getUUID());
        LabyrinthDealer.arriveAt(data,p.getUUID(),place,origin.asLong(),true);
        LabyrinthDoors.settle(p,place);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"reanchor","to",place.id(),"with","","depth",data.returnDepth(p.getUUID()));
        return "alone";
    }

    /**
     * The friend's way through the House becomes this reader's: the way back, the key each room on it is known by, and what lies behind
     * each room's doors on it, as the friend found them. Public for the tests.
     */
    public static void follow(LabyrinthData data,UUID friend,UUID reader,LabyrinthPlace here,BlockPos origin){
        data.adoptReturns(friend,reader);
        var bottomFirst=new ArrayList<>(data.returns(friend));Collections.reverse(bottomFirst);
        for(int k=0;k<=bottomFirst.size();k++){
            LabyrinthPlace place;
            if(k==bottomFirst.size())place=here;
            else{var w=bottomFirst.get(k);place=HouseDimensions.isHouseDimension(w.dimension())?LabyrinthPlaces.placeAt(origin,BlockPos.containing(w.pos())):null;}
            if(place==null)continue;
            long key=LabyrinthData.routeKey(bottomFirst.subList(0,k),place);
            var node=data.node(friend,key);
            if(node!=null)data.rememberNode(reader,key,node);
            for(var spec:place.doors()){
                var door=data.door(place.doorId(spec));if(door==null||!LabyrinthData.DEALT.equals(door.destination))continue;
                var deal=node!=null&&node.containsKey(door.id)?node.get(door.id):k==bottomFirst.size()?data.deal(friend,door):null;
                if(deal!=null)data.deal(reader,door,deal.place(),deal.leak(),deal.bark());
            }
        }
    }

    /** The nearest other living reader in the same room. */
    private static @Nullable ServerPlayer friend(ServerPlayer p,LabyrinthPlace place,BlockPos origin){
        ServerPlayer best=null;double bestD=Double.MAX_VALUE;
        for(var q:p.serverLevel().players()){
            if(q==p||!q.isAlive()||q.isSpectator()||LabyrinthDoors.isBusy(q)||LabyrinthDoors.placeOf(q,origin)!=place)continue;
            double d=q.distanceToSqr(p);if(d<bestD){bestD=d;best=q;}
        }
        return best;
    }
    private static boolean manor(ServerPlayer p,BlockPos origin){
        return p.level().dimension().equals(HouseDimensions.INTERIOR)&&HouseLayout.isInsideDomesticVolume(p.getX()-origin.getX(),p.getY()-origin.getY(),p.getZ()-origin.getZ());
    }
    private static boolean manorAt(From from,BlockPos origin){
        return HouseDimensions.INTERIOR.equals(from.dimension())&&HouseLayout.isInsideDomesticVolume(from.pos().x-origin.getX(),from.pos().y-origin.getY(),from.pos().z-origin.getZ());
    }
    /** Fixtures only. */
    public static void forget(){PENDING.clear();}
    /** Fixtures only: a reader as a command would have found them just before moving them. */
    public static From before(ServerPlayer p){return from(p);}
}
