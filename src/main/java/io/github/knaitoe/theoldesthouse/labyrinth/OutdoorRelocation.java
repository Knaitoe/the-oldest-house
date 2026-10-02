package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Move the existing islands, including their originals; never call a story builder or restock a container. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class OutdoorRelocation {
    public static final String STATE="outdoor_relocation_0429";
    private static final int F=LabyrinthBuilder.flags();
    private record Cell(BlockPos pos,BlockState state,CompoundTag nbt){}
    private record Resident(Entity entity,Vec3 destination){}
    private OutdoorRelocation(){}
    public static AABB bounds(BlockPos base,LabyrinthPlace place){
        var r=place.room();int half=place==LabyrinthPlace.PLAIN?48:Math.max(Math.abs(r.minX()),r.maxX())+9;
        int far=place==LabyrinthPlace.PLAIN?-184:r.minZ()-9;
        return new AABB(base.offset(-half,r.minY()-8,far),base.offset(half+1,Math.max(32,r.maxY()+10),21));
    }
    private static String key(BlockPos origin,LabyrinthPlace place){return origin.asLong()+":"+place.id();}
    public static boolean upgrade(MinecraftServer server,BlockPos origin){
        var d=LabyrinthData.get(server);if(d.builtVersion()<21||d.builtVersion()>=27||!origin.equals(d.builtOrigin()))return true;
        var level=server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return false;
        var done=d.state(STATE);
        for(var place:LabyrinthPlace.values()){
            if(!NovelRooms.outside(place)||done.getBoolean(key(origin,place)))continue;
            if(LakeLandscape.isLake(place)&&d.builtVersion()<23){done.putBoolean(key(origin,place),true);d.setState(STATE,done);continue;}
            var old=LabyrinthPlaces.legacyBase(origin,place);var dest=LabyrinthPlaces.base(origin,place);
            if(old==null||dest==null||level.getBlockState(old.offset(0,-1,-3)).isAir()){done.putBoolean(key(origin,place),true);d.setState(STATE,done);continue;}
            var box=bounds(old,place);
            boolean loaded=true;
            for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++){
                level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,old);
                level.getChunk(x,z);loaded&=level.areEntitiesLoaded(ChunkPos.asLong(x,z));
            }
            if(!loaded)return false;
            moveScene(level,d,old,dest,place);
            done.putBoolean(key(origin,place),true);d.setState(STATE,done);
            for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,old);
        }
        return true;
    }
    public static void moveScene(ServerLevel level,LabyrinthData d,BlockPos old,BlockPos dest,LabyrinthPlace place){
        BlockPos shift=dest.subtract(old);var box=bounds(old,place);List<Cell> cells=new ArrayList<>();
        int low=Math.max(level.getMinBuildHeight(),(int)box.minY),high=Math.min(level.getMaxBuildHeight()-1,(int)box.maxY-1);
        for(var at:BlockPos.betweenClosed((int)box.minX,low,(int)box.minZ,(int)box.maxX-1,high,(int)box.maxZ-1)){
            var be=level.getBlockEntity(at);var st=level.getBlockState(at);
            if(!st.isAir())cells.add(new Cell(at.immutable(),st,be==null?null:be.saveWithFullMetadata(level.registryAccess())));
        }
        for(var cell:cells){
            var at=cell.pos.offset(shift);level.setBlock(at,cell.state,F);
            var be=level.getBlockEntity(at);if(be!=null&&cell.nbt!=null){var nbt=cell.nbt.copy();nbt.putInt("x",at.getX());nbt.putInt("y",at.getY());nbt.putInt("z",at.getZ());be.loadWithComponents(nbt,level.registryAccess());be.setChanged();}
        }
        var residents=new ArrayList<Resident>();for(var e:level.getAllEntities())if(box.contains(e.position()))residents.add(new Resident(e,e.position().add(shift.getX(),shift.getY(),shift.getZ())));
        // Snapshot every rider before moving the vehicle. Same-level connection teleport keeps its custody.
        residents.sort(Comparator.comparing(r->r.entity.isPassenger()));
        for(var resident:residents){var e=resident.entity;var target=resident.destination;
            if(e instanceof LakeWitchEntity witch)witch.relocateLandscape(shift);
            if(e instanceof ServerPlayer p)p.connection.teleport(target.x,target.y,target.z,p.getYRot(),p.getXRot());
            else e.teleportTo(target.x,target.y,target.z);
        }
        d.remapReturns(w->w.dimension().equals(HouseDimensions.OUTSIDE)&&box.contains(w.pos())?new LabyrinthData.Waypoint(w.dimension(),w.pos().add(shift.getX(),shift.getY(),shift.getZ()),w.yaw(),w.door()):w);
        LabyrinthBuilder.registerDoors(d,place,dest);
        if(LakeLandscape.isLake(place)){
            var checkpoint=d.state(LakeSettlement.STATE);
            for(String suffix:List.of(":"+place.id(),"")){
                String a=suffix.isEmpty()?"Sign:"+old.asLong():old.asLong()+suffix;
                String b=suffix.isEmpty()?"Sign:"+dest.asLong():dest.asLong()+suffix;
                if(checkpoint.contains(a)){checkpoint.put(b,checkpoint.get(a).copy());checkpoint.remove(a);}
            }d.setState(LakeSettlement.STATE,checkpoint);
        }
        if(place==LabyrinthPlace.PHONE_CANOE){
            var phones=d.state(PhoneCanoe.ID);for(String id:phones.getAllKeys()){
                var record=phones.getCompound(id);if(record.contains("DropZ")){record.putDouble("DropX",record.getDouble("DropX")+shift.getX());record.putDouble("DropY",record.getDouble("DropY")+shift.getY());record.putDouble("DropZ",record.getDouble("DropZ")+shift.getZ());}
            }d.setState(PhoneCanoe.ID,phones);
        }
        // Clear native originals only after their destination contents and identities are safe.
        for(var cell:cells)Clearable.tryClear(level.getBlockEntity(cell.pos));
        for(var cell:cells)level.setBlock(cell.pos,Blocks.AIR.defaultBlockState(),F);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;var done=LabyrinthData.get(p.server).state(STATE);
        for(var place:LabyrinthPlace.values())if(NovelRooms.outside(place)&&done.getBoolean(key(origin,place))){
            var old=LabyrinthPlaces.legacyBase(origin,place);if(old==null||!bounds(old,place).contains(p.position()))continue;
            var shift=LabyrinthPlaces.base(origin,place).subtract(old);p.teleportTo(p.serverLevel(),p.getX()+shift.getX(),p.getY()+shift.getY(),p.getZ()+shift.getZ(),p.getYRot(),p.getXRot());return;
        }
    }
}
