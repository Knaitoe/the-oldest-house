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

/**
 * 0.4.77: the Crimson Hall's stair stopped one tread short, under its own landing, and the gallery railing ran across the
 * landing, so the third wax cylinder on the north gallery could not be reached on foot. New halls are built with a full
 * stair, an open landing and a gap in the railing. Saved halls are repaired once ({@value #STATE}), keyed by their base,
 * only when the stair's chunks and entity sections are loaded, no camera is within sixteen blocks and nothing living stands
 * on it, and only cells still in their authored state change: a block a player put there stays.
 */
public final class CrimsonGallery {
    public static final String STATE="crimson_gallery_0477";
    static final LabyrinthPlace PLACE=LabyrinthPlace.CRIMSON_HALL;
    private CrimsonGallery(){}
    static void markBuilt(LabyrinthData d,BlockPos b){var s=d.state(STATE);s.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,s);}
    public static boolean built(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}
    public static @Nullable BlockPos base(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    private static AABB area(BlockPos b){return new AABB(Vec3.atLowerCornerOf(b.offset(-15,5,-18)),Vec3.atLowerCornerOf(b.offset(-8,11,-11)));}
    static net.minecraft.world.level.block.state.BlockState tread(){return Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);}
}
