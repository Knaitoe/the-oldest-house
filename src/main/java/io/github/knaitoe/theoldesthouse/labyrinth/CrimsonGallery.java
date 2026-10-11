package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.77: the Crimson Hall's stair stopped one tread short, under its own landing, and the gallery railing ran across the
 * landing, so the third wax cylinder on the north gallery could not be reached on foot. New halls are built with a full
 * stair, an open landing and a gap in the railing. Saved halls are repaired once ({@value #STATE}), keyed by their base,
 * only when the stair's chunks and entity sections are loaded, no camera is within sixteen blocks and nothing living stands
 * on it, and only cells still in their authored state change: a block a player put there stays.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CrimsonGallery {
    public static final String STATE="crimson_gallery_0477";
    static final LabyrinthPlace PLACE=LabyrinthPlace.CRIMSON_HALL;
    private CrimsonGallery(){}
    static void markBuilt(LabyrinthData d,BlockPos b){var s=d.state(STATE);s.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,s);}
    public static boolean built(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    public static @Nullable BlockPos base(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    private static AABB area(BlockPos b){return new AABB(Vec3.atLowerCornerOf(b.offset(-15,5,-18)),Vec3.atLowerCornerOf(b.offset(-8,11,-11)));}
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(built(d,b))return true;var area=area(b);
        for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
        for(int x=-12;x<=-10;x++){
            // The last tread, on its own stone, with headroom; the landing's plank that roofed the tread below it goes.
            if(l.getBlockState(b.offset(x,5,-14)).isAir())l.setBlock(b.offset(x,5,-14),Blocks.STONE_BRICKS.defaultBlockState(),Block.UPDATE_ALL);
            var top=l.getBlockState(b.offset(x,6,-14));
            if(top.is(Blocks.DARK_OAK_PLANKS)||top.isAir())l.setBlock(b.offset(x,6,-14),tread(),Block.UPDATE_ALL);
            if(l.getBlockState(b.offset(x,6,-13)).is(Blocks.DARK_OAK_PLANKS))l.setBlock(b.offset(x,6,-13),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        }
        for(int z=-17;z<=-15;z++)if(l.getBlockState(b.offset(-14,7,z)).is(Blocks.DARK_OAK_FENCE))l.setBlock(b.offset(-14,7,z),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        markBuilt(d,b);
        return true;
    }
    static net.minecraft.world.level.block.state.BlockState tread(){return Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=23||LabyrinthBuilder.isCarving())return;
        var data=LabyrinthData.get(s);var b=base(s);if(b==null||data.door(PLACE.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(data,PLACE)||built(data,b))return;
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l!=null)repair(l,b);
    }
}
