package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** A native crawl, with no teleport, inventory change or attribute change. All lifecycle exits restore the previous pose. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CampBedCrawl {
    private record Lease(Pose forced,Pose pose,BlockPos bed,int grace){}
    private static final Map<ServerPlayer,Lease> ACTIVE=new WeakHashMap<>();
    private CampBedCrawl(){}
    public static boolean under(Player p){if(p.getPose()!=Pose.SWIMMING||p.getBbHeight()>.7)return false;
        for(var at:BlockPos.betweenClosed(p.blockPosition().offset(-1,0,-1),p.blockPosition().offset(1,0,1)))if(CampDecorBlock.bed(p.level().getBlockState(at))){
            var roof=new AABB(at.getX(),at.getY()+.75,at.getZ(),at.getX()+1,at.getY()+1,at.getZ()+1);
            var body=p.getBoundingBox();if(p.getY()>=at.getY()-.04&&body.maxY<roof.minY&&body.minX<roof.maxX&&body.maxX>roof.minX&&body.minZ<roof.maxZ&&body.maxZ>roof.minZ)return true;
        }return false;
    }
    public static void begin(ServerPlayer p,BlockPos bed){if(!LiteraryVignettes.participant(p)||!LiteraryVignettes.inside(p,LabyrinthPlace.CAMP_BLOOD)
            ||!CampDecorBlock.bed(p.level().getBlockState(bed))||p.distanceToSqr(bed.getCenter())>16)return;
        if(!ACTIVE.containsKey(p))ACTIVE.put(p,new Lease(p.getForcedPose(),p.getPose(),bed.immutable(),80));
        p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();HousePackets.send(p,new CampBedPayload(true));
    }
    public static void clear(ServerPlayer p){var lease=ACTIVE.remove(p);if(lease==null)return;if(p.getForcedPose()==Pose.SWIMMING){p.setForcedPose(lease.forced());p.setPose(lease.forced()==null?lease.pose():lease.forced());p.refreshDimensions();}HousePackets.send(p,new CampBedPayload(false));}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var lease=ACTIVE.get(p);if(lease==null)return;
        if(!p.isAlive()||p.isSpectator()||!LiteraryVignettes.inside(p,LabyrinthPlace.CAMP_BLOOD)||p.distanceToSqr(lease.bed().getCenter())>16||lease.grace()<=0&&!under(p)){clear(p);return;}
        ACTIVE.put(p,new Lease(lease.forced(),lease.pose(),lease.bed(),lease.grace()-1));if(p.tickCount%10==0)HousePackets.send(p,new CampBedPayload(true));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void changed(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void died(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
}
