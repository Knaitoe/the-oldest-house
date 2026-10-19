package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Short bounded leases bridge original and destination sections on low view-distance servers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaCast {
    private static final TicketType<UUID> TICKET=TicketType.create("borrasca_original_cast",UUID::compareTo);
    private record Lease(ServerLevel level,List<ChunkPos> chunks){}
    private static final Map<UUID,Lease> leases=new HashMap<>();
    private static LabyrinthData store;
    public static final List<String> KEYS=List.of("CastKimber","CastSheriff","CastPrescott","Worker0","Worker1");
    private BorrascaCast(){}
    public static BlockPos target(String key,int visit){return switch(key){case "CastKyle"->new BlockPos(36,-4,-57);case "CastKimber"->visit>=3?new BlockPos(39,-4,-57):visit==2?new BlockPos(1,0,-76):new BlockPos(1,0,-18);case "CastSheriff"->visit>=3?BorrascaRooms.SHERIFF:new BlockPos(4,0,-16);case "CastPrescott"->visit>=2?new BlockPos(15,12,-171):new BlockPos(11,0,-25);case "Worker0"->visit>=4?new BlockPos(-6,12,-193):new BlockPos(-8,13,-157);case "Worker1"->visit>=4?new BlockPos(5,12,-195):new BlockPos(9,13,-157);default->throw new IllegalArgumentException(key);};}
    public static void prepare(ServerPlayer p,BlockPos base,CompoundTag own){var d=LabyrinthData.get(p.server);if(store!=d){clear();store=d;}if(leases.containsKey(p.getUUID()))return;var level=p.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return;int visit=own.contains("CastNextVisit")?own.getInt("CastNextVisit"):own.getInt("Visit");var chunks=new LinkedHashSet<ChunkPos>();for(String key:KEYS){var from=own.contains(key+"At")?BlockPos.of(own.getLong(key+"At")):base.offset(target(key,1));var to=base.offset(target(key,key.equals("CastKimber")&&own.getBoolean("FriendMissing")?3:visit));chunks.add(new ChunkPos(from));chunks.add(new ChunkPos(to));}var held=List.copyOf(chunks);for(var c:held)level.getChunkSource().addRegionTicket(TICKET,c,3,p.getUUID());leases.put(p.getUUID(),new Lease(level,held));}
    /** Prepare the next cast while the reader is still at the original House door, before travel. */
    public static boolean prepareNext(ServerPlayer p){
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.BORRASCA);int next=own.getInt("CastNextVisit");if(next==0)return true;
        if(BorrascaSites.inside(p)||own.getBoolean("Here"))return false;var base=BorrascaSites.base(p.server,p.getUUID());var level=p.server.getLevel(HouseDimensions.OUTSIDE);if(base==null||level==null)return false;prepare(p,base,own);
        boolean all=true;for(String key:KEYS){var a=own.hasUUID(key)&&level.getEntity(own.getUUID(key)) instanceof LiteraryActor actor?actor:null;all&=recall(level,a,base.offset(target(key,key.equals("CastKimber")&&own.getBoolean("FriendMissing")?3:next)));if(a!=null)remember(own,key,a);}
        if(all){own.putInt("CastVisit",next);own.remove("CastNextVisit");release(p.getUUID());}LiteraryVignettes.save(data,p.getUUID(),LabyrinthPlace.BORRASCA,own);return all;
    }
    public static boolean recall(ServerPlayer p,LiteraryActor a,BlockPos at){
        return recall(p.serverLevel(),a,at);
    }
    private static boolean recall(ServerLevel level,LiteraryActor a,BlockPos at){
        if(a==null)return false;if(a.position().distanceToSqr(Vec3.atBottomCenterOf(at))<1)return true;
        if(LiteraryVignettes.watched(level,a.getEyePosition())||LiteraryVignettes.watched(level,at.getCenter()))return false;
        if(!level.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;
        stop(a);a.moveTo(Vec3.atBottomCenterOf(at),180,0);a.setYBodyRot(180);a.setYHeadRot(180);return true;
    }
    public static void walk(LiteraryActor a,Vec3 at,double speed){a.getPersistentData().putDouble("BorrascaGoalX",at.x);a.getPersistentData().putDouble("BorrascaGoalY",at.y);a.getPersistentData().putDouble("BorrascaGoalZ",at.z);a.getPersistentData().putDouble("BorrascaSpeed",speed);a.setNoAi(false);a.setNoGravity(false);}
    public static void stop(LiteraryActor a){a.getPersistentData().remove("BorrascaSpeed");a.getNavigation().stop();a.setSpeed(0);a.setNoAi(true);a.setDeltaMovement(Vec3.ZERO);}
    /** Feed the normal once-per-tick travel call; vanilla owns gravity, stepping and ladder collision. */
    public static Vec3 input(LiteraryActor a){
        var t=a.getPersistentData();if(!t.contains("BorrascaSpeed")||a.isInvisible())return Vec3.ZERO;
        Vec3 goal=new Vec3(t.getDouble("BorrascaGoalX"),t.getDouble("BorrascaGoalY"),t.getDouble("BorrascaGoalZ"));Vec3 d=goal.subtract(a.position());double horizontal=d.horizontalDistance();
        if(horizontal<.18&&Math.abs(d.y)<.08){stop(a);return Vec3.ZERO;}
        if(!a.level().hasChunkAt(BlockPos.containing(a.position().add(d.normalize().scale(.5)))))return Vec3.ZERO;
        float yaw=horizontal>.1?(float)Math.toDegrees(Math.atan2(-d.x,d.z)):180;a.setYRot(Mth.approachDegrees(a.getYRot(),yaw,12));a.setYBodyRot(a.getYRot());a.setYHeadRot(a.getYRot());
        a.setSpeed((float)a.getAttributeValue(Attributes.MOVEMENT_SPEED)*(.1F/.24F));
        if(horizontal<.7&&Math.abs(d.y)>.08&&a.onClimbable()){
            a.setYRot(180);a.setYBodyRot(180);a.setDeltaMovement(a.getDeltaMovement().x,d.y>0?.15:-.12,a.getDeltaMovement().z);return new Vec3(0,0,d.y>0?.5:0);
        }return new Vec3(0,0,Math.min(t.getDouble("BorrascaSpeed"),horizontal*2));
    }
    public static void remember(CompoundTag own,String key,LiteraryActor actor){if(actor!=null)own.putLong(key+"At",actor.blockPosition().asLong());}
    public static BlockPos destination(CompoundTag own,String key,BlockPos base){return own.getInt("CastVisit")==own.getInt("Visit")&&own.contains(key+"At")?BlockPos.of(own.getLong(key+"At")).subtract(base):target(key,key.equals("CastKimber")&&own.getBoolean("FriendMissing")?3:own.getInt("Visit"));}
    public static void release(UUID id){var lease=leases.remove(id);if(lease!=null)for(var c:lease.chunks)lease.level.getChunkSource().removeRegionTicket(TICKET,c,3,id);}
    public static void clear(){for(var id:List.copyOf(leases.keySet()))release(id);store=null;}
    public static int heldChunks(){return leases.values().stream().mapToInt(l->l.chunks.size()).sum();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){release(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){clear();}
}
