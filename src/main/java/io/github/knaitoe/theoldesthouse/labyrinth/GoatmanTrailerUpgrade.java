package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.LinkedHashMap;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A once-only replacement of recognized bathroom placeholders, preserving actors, supplies and saved evenings. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GoatmanTrailerUpgrade {
    public static final String STATE="trailer_fixtures_0465";
    public static final String WINDOW_STATE="trailer_window_spill_0495";
    public static final String BED_STATE="trailer_inward_beds_0131";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private GoatmanTrailerUpgrade(){}
    public static boolean apply(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var done=data.state(STATE);String key=Long.toString(b.asLong());if(done.getBoolean(key))return true;
        for(var target:new BlockPos[]{b.offset(GoatmanWoods.TOILET),b.offset(GoatmanWoods.SINK),b.offset(GoatmanWoods.WINDOW),b.offset(GoatmanWoods.DOOR)})if(!l.hasChunkAt(target)||!l.areEntitiesLoaded(ChunkPos.asLong(target.getX()>>4,target.getZ()>>4)))return false;
        var changes=new LinkedHashMap<BlockPos,BlockState>();
        var toilet=b.offset(GoatmanWoods.TOILET);var old=l.getBlockState(toilet);
        if(old.is(Blocks.QUARTZ_STAIRS)&&old.getValue(StairBlock.FACING)==Direction.EAST){
            changes.put(toilet,GoatmanRegistry.TOILET.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,Direction.WEST));
            if(l.getBlockState(toilet.above()).is(Blocks.QUARTZ_SLAB))changes.put(toilet.above(),Blocks.AIR.defaultBlockState());
        }
        var sink=b.offset(GoatmanWoods.SINK);old=l.getBlockState(sink);
        if(old.is(Blocks.WATER_CAULDRON)||old.is(Blocks.CAULDRON))changes.put(sink,GoatmanRegistry.SINK.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,Direction.WEST).setValue(TrailerFixtureBlock.FILLED,old.is(Blocks.WATER_CAULDRON)));
        var window=b.offset(GoatmanWoods.WINDOW);old=l.getBlockState(window);
        if(old.is(Blocks.SPRUCE_TRAPDOOR))changes.put(window,GoatmanRegistry.WINDOW.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,Direction.WEST).setValue(TrailerFixtureBlock.OPEN,!old.getValue(TrapDoorBlock.OPEN)));
        var door=b.offset(GoatmanWoods.DOOR);old=l.getBlockState(door);var upper=l.getBlockState(door.above());
        if(old.is(Blocks.SPRUCE_DOOR)&&upper.is(Blocks.SPRUCE_DOOR)&&old.getValue(DoorBlock.FACING)==Direction.SOUTH){
            var next=GoatmanRegistry.DOOR.get().defaultBlockState();
            for(var property:old.getProperties())if(next.hasProperty(property))next=copy(next,old,property);
            changes.put(door,next.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));changes.put(door.above(),next.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        }
        for(var e:changes.entrySet()){
            var at=e.getKey();if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4))||!SceneHuntReview.safe(l,at,e.getValue(),false))return false;
            if(l.players().stream().anyMatch(p->new AABB(at).inflate(48).intersects(p.getCamera().getBoundingBox())))return false;
        }
        for(var e:changes.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        done.putBoolean(key,true);data.setState(STATE,done);return true;
    }
    private static <T extends Comparable<T>> BlockState copy(BlockState next,BlockState old,net.minecraft.world.level.block.state.properties.Property<T> p){return next.setValue(p,old.getValue(p));}
    /** Move only the complete authored lamp pairs beside the front windows, once; preserve edits, removals and occupied destinations. */
    public static boolean clearWindowSpill(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var done=data.state(WINDOW_STATE);String key=Long.toString(b.asLong());if(done.getBoolean(key))return true;
        for(int x:new int[]{-7,7})for(int z:new int[]{-59,-57})for(int y:new int[]{2,3}){var at=b.offset(x,y,z);if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        var shelf=Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);var lamp=Blocks.LANTERN.defaultBlockState();var changes=new LinkedHashMap<BlockPos,BlockState>();
        for(int x:new int[]{-7,7}){
            var from=b.offset(x,2,-59);var to=b.offset(x,2,-57);
            if(!l.getBlockState(from).equals(shelf)||!l.getBlockState(from.above()).equals(lamp)||!l.getBlockState(to).isAir()||!l.getBlockState(to.above()).isAir())continue;
            changes.put(from,Blocks.AIR.defaultBlockState());changes.put(from.above(),Blocks.AIR.defaultBlockState());changes.put(to,shelf);changes.put(to.above(),lamp);
        }
        for(var e:changes.entrySet()){
            if(!SceneHuntReview.safe(l,e.getKey(),e.getValue(),false)||l.players().stream().anyMatch(p->new AABB(e.getKey()).inflate(48).intersects(p.getCamera().getBoundingBox())))return false;
        }
        for(var e:changes.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        done.putBoolean(key,true);data.setState(WINDOW_STATE,done);return true;
    }
    public static boolean inwardBeds(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var done=d.state(BED_STATE);String key=Long.toString(b.asLong());if(done.getBoolean(key))return true;
        var area=IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN);if(l.players().stream().anyMatch(p->area.inflate(12).intersects(p.getCamera().getBoundingBox())))return false;
        boolean complete=true;
        for(int i=1;i<24;i+=2){var at=b.offset(GoatmanWoods.bunk(i));if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;
            var left=l.getBlockState(at);var right=l.getBlockState(at.east());
            if(!left.is(Blocks.RED_BED)||!right.is(Blocks.RED_BED)||left.getValue(BedBlock.FACING)!=Direction.EAST||right.getValue(BedBlock.FACING)!=Direction.EAST||left.getValue(BedBlock.PART)!=net.minecraft.world.level.block.state.properties.BedPart.FOOT||right.getValue(BedBlock.PART)!=net.minecraft.world.level.block.state.properties.BedPart.HEAD)continue;
            var pair=new AABB(at).expandTowards(1,1,0);if(left.getValue(BedBlock.OCCUPIED)||right.getValue(BedBlock.OCCUPIED)||!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,pair,net.minecraft.world.entity.LivingEntity::isAlive).isEmpty()){complete=false;continue;}
            l.setBlock(at,left.setValue(BedBlock.FACING,Direction.WEST).setValue(BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD),F);l.setBlock(at.east(),right.setValue(BedBlock.FACING,Direction.WEST).setValue(BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.FOOT),F);
        }
        if(complete){done.putBoolean(key,true);d.setState(BED_STATE,done);}return complete;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=0||LabyrinthBuilder.isCarving())return;
        var l=s.getLevel(HouseDimensions.INTERIOR);var b=GoatmanVignette.base(s);if(l==null||b==null)return;
        var area=IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN);
        for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return;
        apply(l,b);clearWindowSpill(l,b);inwardBeds(l,b);
    }
}
