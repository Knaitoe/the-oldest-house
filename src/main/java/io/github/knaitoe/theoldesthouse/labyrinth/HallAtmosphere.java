package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Shared room clocks pause while empty. No peer can multiply a room's sound or restock it. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HallAtmosphere {
    public static final String STATE="hall_atmosphere_0448";
    private HallAtmosphere() {}
    public static boolean room(LabyrinthPlace p) {
        return p!=null&&(LabyrinthHalls.isHall(p)||StoneHalls.isStone(p)||p==LabyrinthPlace.EXPLORER_CAMP);
    }
    /** Invoked once for the physical room, regardless of its number of readers. */
    public static boolean occupiedSecond(CompoundTag own,int period) {
        if(!own.getBoolean("Occupied"))own.putInt("Returns",own.getInt("Returns")+1);
        own.putBoolean("Occupied",true);
        int seconds=own.getInt("Seconds")+1;
        boolean cue=seconds>=period;
        own.putInt("Seconds",cue?0:seconds);
        return cue;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        var l=server.getLevel(HouseDimensions.INTERIOR);var origin=HouseSavedData.get(server).houseOrigin();
        if(l==null||origin==null)return;
        var data=LabyrinthData.get(server);Set<LabyrinthPlace> active=new HashSet<>();
        for(var p:l.players())if(p.isAlive()&&!p.isSpectator()) {
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(room(place))active.add(place);
        }
        for(var place:active) {
            var base=LabyrinthPlaces.base(origin,place);String key=origin.asLong()+":"+place.id();
            var own=data.stateEntry(STATE,key);int period=60+Math.floorMod(base.asLong(),21);
            if(occupiedSecond(own,period)) {
                var sound=StoneHalls.isStone(place)?LabyrinthRegistry.HALL_STONE:place==LabyrinthPlace.QUIET_ROOM||place==LabyrinthPlace.EXPLORER_CAMP?LabyrinthRegistry.HALL_SETTLE:LabyrinthRegistry.HALL_PIPES;
                // One native positional sound goes to every listener in range, from the same fixed landmark.
                l.playSound(null,base.offset(place==LabyrinthPlace.OFFSET_HALL?4:0,2,-10),sound.get(),SoundSource.AMBIENT,.32F,1F);
            }
            data.setStateEntry(STATE,key,own);
        }
        // A familiar chair changes once, after three real visits, and only in an unseen, vacant room.
        var quiet=LabyrinthPlace.QUIET_ROOM;String key=origin.asLong()+":"+quiet.id();
        var own=data.stateEntry(STATE,key);
        if(!active.contains(quiet)&&own.getBoolean("Occupied")) {
            own.putBoolean("Occupied",false);data.setStateEntry(STATE,key,own);
        }
        if(!active.contains(quiet)&&own.getInt("Returns")>=3&&!own.getBoolean("Turned")) {
            var base=LabyrinthPlaces.base(origin,quiet);
            if(turnQuietChair(l,base)) {own.putBoolean("Turned",true);data.setStateEntry(STATE,key,own);}
        }
        // Empty rooms never accrue elapsed time; returning starts another visit, without a sound burst.
        for(var place:LabyrinthPlace.values())if(room(place)&&place!=quiet&&!active.contains(place)) {
            key=origin.asLong()+":"+place.id();own=data.stateEntry(STATE,key);
            if(own.getBoolean("Occupied")){own.putBoolean("Occupied",false);data.setStateEntry(STATE,key,own);}
        }
    }
    public static boolean turnQuietChair(ServerLevel l,BlockPos base) {
        var box=LabyrinthPlace.QUIET_ROOM.room();
        var area=new AABB(base.getX()+box.minX(),base.getY()+box.minY(),base.getZ()+box.minZ(),
                base.getX()+box.maxX()+1,base.getY()+box.maxY()+1,base.getZ()+box.maxZ()+1);
        // Spectator cameras also prevent the change. No synchronous loading or moving residents.
        for(var p:l.players())if(area.inflate(48).intersects(p.getBoundingBox()))return false;
        for(int x=((int)area.minX)>>4;x<=((int)area.maxX)>>4;x++)for(int z=((int)area.minZ)>>4;z<=((int)area.maxZ)>>4;z++)
            if(!l.isLoaded(new BlockPos(x<<4,base.getY(),z<<4))||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty())return false;
        BlockPos at=base.offset(-4,0,-8);var state=l.getBlockState(at);
        if(!l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below()))return true;
        if(state.getBlock() instanceof HouseholdFurnitureBlock
                &&state.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR
                &&state.getValue(HouseholdFurnitureBlock.FACING)==Direction.EAST)
            state=state.setValue(HouseholdFurnitureBlock.FACING,Direction.NORTH);
        else if((state.is(Blocks.SPRUCE_STAIRS)||state.is(Blocks.DARK_OAK_STAIRS))&&state.getValue(StairBlock.FACING)==Direction.WEST)
            state=state.setValue(StairBlock.FACING,Direction.NORTH);
        else return true; // honor player edits
        l.setBlock(at,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        return true;
    }
}
