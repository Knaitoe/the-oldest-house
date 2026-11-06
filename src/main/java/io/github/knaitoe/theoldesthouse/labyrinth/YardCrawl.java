package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.YardCrawlPayload;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A voluntary crawl at a real one-block culvert. Normal movement does the crossing. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardCrawl {
    private YardCrawl(){}
    private record Lease(ServerPlayer player,Pose forced,Pose pose){}
    private static final Map<UUID,Lease> ACTIVE=new HashMap<>();
    private static BlockPos base(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o==null||!p.level().dimension().equals(HouseDimensions.OUTSIDE)||LabyrinthPlaces.placeAt(o,p.blockPosition())!=LabyrinthPlace.YARD_DITCH?null:LabyrinthPlaces.base(o,LabyrinthPlace.YARD_DITCH);}
    private static boolean atMouth(ServerPlayer p,BlockPos b){double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();return x>-.1&&x<1.1&&(z>-14&&z<-11||z>-25&&z<-22);}
    public static boolean start(ServerPlayer p){
        var b=base(p);if(b==null||!atMouth(p,b)||!p.isAlive()||p.isSpectator())return false;
        return take(p);
    }
    private static boolean take(ServerPlayer p){
        var old=ACTIVE.get(p.getUUID());if(old!=null&&old.player()!=p){clear(old.player());old=null;}
        if(old==null)ACTIVE.put(p.getUUID(),new Lease(p,p.getForcedPose(),p.getPose()));
        p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();HousePackets.send(p,new YardCrawlPayload(true));return true;
    }
    public static boolean crawling(ServerPlayer p){var old=ACTIVE.get(p.getUUID());return old!=null&&old.player()==p;}
    public static void clear(ServerPlayer p){
        var old=ACTIVE.remove(p.getUUID());if(old==null)return;
        if(p.getForcedPose()==Pose.SWIMMING){p.setForcedPose(old.forced());p.setPose(old.forced()==null?old.pose():old.forced());p.refreshDimensions();}
        HousePackets.send(p,new YardCrawlPayload(false));
    }
    public static void clearAll(){for(var old:List.copyOf(ACTIVE.values()))clear(old.player());}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        for(var old:List.copyOf(ACTIVE.values()))if(old.player().server==e.getServer()&&(e.getServer().getPlayerList().getPlayer(old.player().getUUID())!=old.player()||!old.player().isAlive()))clear(old.player());
        for(var p:e.getServer().getPlayerList().getPlayers()){
            var b=base(p);boolean active=crawling(p);
            if(!active){
                if(b!=null&&p.isAlive()&&!p.isSpectator()){
                    double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();
                    boolean pipe=x>0&&x<1&&z>-22&&z<-12&&Math.abs(p.getY()-b.getY())<.2;
                    if(pipe||p.isShiftKeyDown()&&atMouth(p,b))take(p);
                }
                continue;
            }
            if(b==null||!p.isAlive()||p.isSpectator()){clear(p);continue;}
            double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();boolean in=x>-.1&&x<1.1&&z>-24&&z<-11;
            var standing=new AABB(p.getX()-.3,p.getY()+.01,p.getZ()-.3,p.getX()+.3,p.getY()+1.8,p.getZ()+.3);
            if(!in&&p.serverLevel().noCollision(p,standing)){clear(p);continue;}
            if(e.getServer().getTickCount()%10==0)HousePackets.send(p,new YardCrawlPayload(true));
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
}
