package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Physical and perceptual gray-space hazards.
 *
 * None is designed as an instant execution. They cost breath, health, food,
 * time, light, orientation or confidence, so surviving several is the actual
 * threat of remaining lost.
 */
public final class LabyrinthHazards {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState WALL = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState CEILING = Blocks.STONE.defaultBlockState();

    private static final int COMPRESSION_TRIGGER_Z = -4;
    private static final int COMPRESSION_START_Z = -5;
    private static final int COMPRESSION_END_Z = -24;
    private static final int OUTER_CLOSE_TICKS = 30;
    private static final int INNER_CLOSE_TICKS = 75;

    private static final Map<UUID, Integer> FALSE_DISTANCE_LAPS = new HashMap<>();
    private static final Map<UUID, Long> FALSE_DISTANCE_COOLDOWN = new HashMap<>();
    private static final Map<BlockPos, Long> SINK_LIGHTS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_PRESSURE_HIT = new HashMap<>();

    private static long compressionStarted = -1L;
    private static int compressionStage;
    private static int movingDoorIndex;
    private static long movingDoorNextTick;

    private static final BlockPos[] MOVING_DOORS = {
            new BlockPos(0, 0, -15),
            new BlockPos(-8, 0, -8),
            new BlockPos(8, 0, -8)
    };
    private static final Direction[] MOVING_FACING = {
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };

    private LabyrinthHazards() {
    }

    // ---------------------------------------------------------------------
    // Existing physical hazards

    public static void buildFloodedPassage(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -7, 7, 5, -27, -1, WALL, FLOOR, CEILING);
        for(int x=-7;x<=7;x++)for(int z=-26;z<=-3;z++)for(int y=-2;y<=4;y++)level.setBlock(base.offset(x,y,z),y==-2?FLOOR:WALL,FLAGS);
        for(BlockPos feet:floodRoute(base)){
            level.setBlock(feet,Blocks.WATER.defaultBlockState(),FLAGS);
            level.setBlock(feet.below(),Math.floorMod(feet.getZ(),7)==0?Blocks.MOSSY_STONE_BRICKS.defaultBlockState():FLOOR,FLAGS);
        }
        // The channels sit below the dry vestibule, so source water cannot wash through a door.
        for(int z:new int[]{-3,-27})for(int x=0;x<=1;x++){
            level.setBlock(base.offset(x,-3,z),FLOOR,FLAGS);
            for(int y=-2;y<=3;y++)level.setBlock(base.offset(x,y,z),y<0?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),FLAGS);
        }
        for(BlockPos pocket:floodAir(base)){
            for(int x=0;x<=1;x++)for(int z=0;z<=1;z++)for(int y=-1;y<=4;y++)level.setBlock(pocket.offset(x,y,z),y<2?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),FLAGS);
            LabyrinthBuilder.hangLantern(level,pocket.above(5),true);
        }
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FLOODED_PASSAGE);
        var d=LabyrinthData.get(level.getServer());var state=d.state("water_trial_0427");state.putBoolean(Long.toString(base.asLong()),true);d.setState("water_trial_0427",state);
    }
    public static List<BlockPos> floodAir(BlockPos base){return List.of(base.offset(-6,0,-24),base.offset(0,0,-14),base.offset(6,0,-17));}
    public static List<BlockPos> floodRoute(BlockPos base){
        int[][] corners={{0,-3},{-6,-3},{-6,-24},{-3,-24},{-3,-6},{0,-6},{0,-22},{3,-22},{3,-9},{6,-9},{6,-26},{0,-26},{0,-27}};
        var route=new java.util.ArrayList<BlockPos>();
        for(int i=0;i<corners.length-1;i++){int x=corners[i][0],z=corners[i][1];while(x!=corners[i+1][0]||z!=corners[i+1][1]){route.add(base.offset(x,-1,z));x+=Integer.signum(corners[i+1][0]-x);z+=Integer.signum(corners[i+1][1]-z);}}
        route.add(base.offset(0,-1,-27));return List.copyOf(route);
    }
    public static void upgradeFlooded(ServerLevel level,BlockPos origin){
        if(!LabyrinthBuilder.ready(level.getServer()))return;var base=LabyrinthPlaces.base(origin,LabyrinthPlace.FLOODED_PASSAGE);if(base==null)return;
        var d=LabyrinthData.get(level.getServer());if(d.state("water_trial_0427").getBoolean(Long.toString(base.asLong())))return;
        if(level.players().stream().anyMatch(p->new AABB(base.offset(-9,-3,-30),base.offset(9,8,2)).contains(p.position())))return;
        // Preserve a player-authored container instead of replacing its identity during a scenery upgrade.
        for(BlockPos pos:BlockPos.betweenClosed(base.offset(-8,-2,-27),base.offset(8,5,-1)))if(level.getBlockEntity(pos)!=null)return;
        buildFloodedPassage(level,base);
    }

    public static void buildFracturedWalkway(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -3, 3, 10, -27, -1, WALL, FLOOR, CEILING);
        for (int step = 0; step <= 6; step++) {
            int z = -2 - step;
            for (int x = -1; x <= 1; x++) {
                level.setBlock(
                        base.offset(x, step, z),
                        Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH),
                        FLAGS
                );
            }
        }
        for (int z = -9; z >= -11; z--) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(base.offset(x, 6, z), Blocks.STONE_BRICKS.defaultBlockState(), FLAGS);
            }
        }
        for (int z = -15; z >= -27; z--) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(base.offset(x, 6, z), Blocks.STONE_BRICKS.defaultBlockState(), FLAGS);
            }
        }
        for (int step = 0; step <= 6; step++) {
            level.setBlock(
                    base.offset(2, step, -19 - step),
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH),
                    FLAGS
            );
        }
        LabyrinthBuilder.hangLantern(level, base.offset(0, 10, -10), true);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 10, -22), true);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FRACTURED_WALKWAY);
    }

    public static void buildCompressionPassage(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -2, 2, 3, -27, -1, WALL, FLOOR, CEILING);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 3, -3), true);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 3, -26), true);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.COMPRESSION_PASSAGE);
    }

    // ---------------------------------------------------------------------
    // Perceptual / eldritch hazards

    public static void buildFalseDistance(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -1, 1, 3, -33, -1, WALL, FLOOR, CEILING);
        for (int z = -5; z >= -29; z -= 6) {
            LabyrinthBuilder.hangLantern(level, base.offset(0, 3, z), true);
        }
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FALSE_DISTANCE);
    }

    public static void buildLightSink(ServerLevel level, BlockPos base) {
        BlockState sinkWall = Blocks.GRAY_TERRACOTTA.defaultBlockState();
        BlockState sinkFloor = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState sinkCeiling = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        LabyrinthBuilder.room(level, base, -2, 2, 3, -27, -1, sinkWall, sinkFloor, sinkCeiling);
        // There is deliberately no authored light in this room.
        LabyrinthBuilder.entrance(level, base, sinkWall, sinkFloor, sinkCeiling);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.LIGHT_SINK);
    }

    public static void buildMovingThreshold(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -7, 7, 4, -14, -1, WALL, FLOOR, CEILING);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 4, -7), true);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.placeDoor(level, base.offset(0, 0, 1), Direction.SOUTH);
        LabyrinthBuilder.placeDoor(level, base.offset(MOVING_DOORS[0]), MOVING_FACING[0]);
    }

    public static void buildDuplicatePassage(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -7, 7, 4, -14, -1, WALL, FLOOR, CEILING);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 4, -7), true);
        // Three identical benches make the room readable as deliberately
        // symmetrical rather than merely unfinished.
        level.setBlock(base.offset(0, 0, -11), LabyrinthBuilder.stairs(Blocks.STONE_STAIRS, Direction.NORTH), FLAGS);
        level.setBlock(base.offset(-5, 0, -8), LabyrinthBuilder.stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        level.setBlock(base.offset(5, 0, -8), LabyrinthBuilder.stairs(Blocks.STONE_STAIRS, Direction.EAST), FLAGS);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.DUPLICATE_PASSAGE);
    }

    public static void buildGravityDrift(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -3, 3, 4, -27, -1, WALL, FLOOR, CEILING);

        // West-side recovery trench: a four-block fall, painful but ordinarily
        // survivable, with ladders at two points so the pull cannot soft-lock.
        for (int z = -5; z >= -24; z--) {
            for (int x = -3; x <= -2; x++) {
                for (int y = -4; y <= -1; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), FLAGS);
                }
                level.setBlock(base.offset(x, -5, z), FLOOR, FLAGS);
            }
        }
        for (int z : new int[] {-10, -20}) {
            for (int y = -4; y <= -1; y++) {
                level.setBlock(
                        base.offset(-3, y, z),
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST),
                        FLAGS
                );
            }
        }

        LabyrinthBuilder.hangLantern(level, base.offset(1, 3, -7), true);
        LabyrinthBuilder.hangLantern(level, base.offset(1, 3, -21), true);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.GRAVITY_DRIFT);
    }

    // ---------------------------------------------------------------------
    // Runtime

    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        if (place == LabyrinthPlace.FALSE_DISTANCE) {
            FALSE_DISTANCE_LAPS.put(player.getUUID(), 0);
            FALSE_DISTANCE_COOLDOWN.put(player.getUUID(), player.server.getTickCount() + 20L);
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 5 != 0) {
            return;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (level == null || origin == null) {
            clearAll();
            return;
        }

        long now = server.getTickCount();
        if(now%100==0)upgradeFlooded(level,origin);
        tickFlooded(level, origin, now);
        tickCompression(level, origin, now);
        tickFalseDistance(level, origin, now);
        tickLightSink(level, origin, now);
        tickMovingThreshold(level, origin, now);
        tickGravityDrift(level, origin);
    }

    private static void tickFlooded(ServerLevel level, BlockPos origin, long now) {
        if (now % 20L != 0L) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.FLOODED_PASSAGE
                    && player.isInWater()) {
                player.causeFoodExhaustion(0.12F);
            }
        }
    }

    private static void tickFalseDistance(ServerLevel level, BlockPos origin, long now) {
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.FALSE_DISTANCE);
        if (base == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) != LabyrinthPlace.FALSE_DISTANCE) {
                continue;
            }
            UUID id = player.getUUID();
            int laps = FALSE_DISTANCE_LAPS.getOrDefault(id, 0);
            if (laps >= 3 || now < FALSE_DISTANCE_COOLDOWN.getOrDefault(id, 0L)) {
                continue;
            }
            if (player.getZ() <= base.getZ() - 24.0D) {
                FALSE_DISTANCE_LAPS.put(id, laps + 1);
                FALSE_DISTANCE_COOLDOWN.put(id, now + 30L);
                player.causeFoodExhaustion(0.6F);
                player.connection.teleport(
                        base.getX() + 0.5D,
                        player.getY(),
                        base.getZ() - 8.5D,
                        player.getYRot(),
                        player.getXRot()
                );
            }
        }
    }

    public static boolean allowsPlacing(Level level, BlockPos pos, BlockState placed) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        LabyrinthPlace place = origin == null ? null : LabyrinthPlaces.placeAt(origin, pos);
        return place != null && place.kind() == LabyrinthPlace.Kind.GRAY && isPortableLight(placed);
    }

    public static boolean isPortableLight(BlockState state) {
        return LabyrinthLighting.isPortableLight(state);
    }

    private static void tickLightSink(ServerLevel level, BlockPos origin, long now) {
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.LIGHT_SINK);
        if (base == null) {
            return;
        }
        var bounds = LabyrinthPlaces.placeBounds(origin, LabyrinthPlace.LIGHT_SINK);
        if (bounds == null) {
            return;
        }

        if (now % 20L == 0L) {
            for (BlockPos cursor : BlockPos.betweenClosed(
                    bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                BlockState state = level.getBlockState(cursor);
                if (!isPortableLight(state)) {
                    continue;
                }
                BlockPos pos = cursor.immutable();
                long born = SINK_LIGHTS.computeIfAbsent(pos, p -> now);
                if (now - born >= 240L) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
                    SINK_LIGHTS.remove(pos);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 0.7F);
                }
            }
            SINK_LIGHTS.keySet().removeIf(pos -> !isPortableLight(level.getBlockState(pos)));
        }

        for (ServerPlayer player : level.players()) {
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) != LabyrinthPlace.LIGHT_SINK) {
                continue;
            }
            boolean nearLight = false;
            for (BlockPos light : SINK_LIGHTS.keySet()) {
                if (light.distToCenterSqr(player.position()) <= 25.0D) {
                    nearLight = true;
                    break;
                }
            }
            if (nearLight) {
                player.removeEffect(MobEffects.DARKNESS);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 35, 0, true, false, false));
            }
        }
    }

    private static void tickMovingThreshold(ServerLevel level, BlockPos origin, long now) {
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.MOVING_THRESHOLD);
        if (base == null) {
            return;
        }
        boolean occupied = level.players().stream()
                .anyMatch(player -> LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.MOVING_THRESHOLD);
        if (!occupied) {
            if (movingDoorIndex != 0) {
                moveThreshold(level, base, 0);
            }
            movingDoorNextTick = now + 40L;
            return;
        }
        if (now < movingDoorNextTick) {
            return;
        }

        BlockPos current = base.offset(MOVING_DOORS[movingDoorIndex]);
        if (HouseWatchers.isWatched(level, current.above())) {
            movingDoorNextTick = now + 20L;
            return;
        }
        for (int step = 1; step <= MOVING_DOORS.length; step++) {
            int next = (movingDoorIndex + step) % MOVING_DOORS.length;
            BlockPos target = base.offset(MOVING_DOORS[next]);
            if (!HouseWatchers.isWatched(level, target.above())) {
                moveThreshold(level, base, next);
                movingDoorNextTick = now + 70L;
                return;
            }
        }
        movingDoorNextTick = now + 20L;
    }

    private static void moveThreshold(ServerLevel level, BlockPos base, int next) {
        BlockPos old = base.offset(MOVING_DOORS[movingDoorIndex]);
        level.setBlock(old, WALL, FLAGS);
        level.setBlock(old.above(), WALL, FLAGS);

        movingDoorIndex = next;
        LabyrinthBuilder.placeDoor(level, base.offset(MOVING_DOORS[next]), MOVING_FACING[next]);
    }

    private static void tickGravityDrift(ServerLevel level, BlockPos origin) {
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.GRAVITY_DRIFT);
        if (base == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.GRAVITY_DRIFT) {
                double depth = Math.max(0.0D, Math.min(1.0D, (base.getZ() - player.getZ()) / 24.0D));
                player.push(-0.035D - depth * 0.035D, 0.0D, 0.0D);
                player.hurtMarked = true;
                player.causeFoodExhaustion(0.01F);
            }
        }
        AABB area = new AABB(
                base.getX() - 4.0D, base.getY() - 5.0D, base.getZ() - 27.0D,
                base.getX() + 4.0D, base.getY() + 5.0D, base.getZ()
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.push(-0.025D, 0.0D, 0.0D);
        }
    }

    private static void tickCompression(ServerLevel level, BlockPos origin, long now) {
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.COMPRESSION_PASSAGE);
        if (base == null) {
            return;
        }

        List<ServerPlayer> inside = level.players().stream()
                .filter(player -> LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.COMPRESSION_PASSAGE)
                .toList();

        if (inside.isEmpty()) {
            if (compressionStarted >= 0L || compressionStage != 0) {
                openCompression(level, base);
            }
            resetCompression();
            return;
        }

        if (compressionStarted < 0L) {
            boolean triggered = inside.stream().anyMatch(player -> player.getZ() <= base.getZ() + COMPRESSION_TRIGGER_Z);
            if (!triggered) {
                return;
            }
            compressionStarted = now;
            level.playSound(null, base.offset(0, 1, -7), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.2F, 0.55F);
            for (ServerPlayer player : inside) {
                player.displayClientMessage(Component.literal("Stone grinds somewhere inside the walls.")
                        .withStyle(ChatFormatting.DARK_GRAY), true);
            }
        }

        long elapsed = now - compressionStarted;
        int wanted = elapsed >= INNER_CLOSE_TICKS ? 2 : elapsed >= OUTER_CLOSE_TICKS ? 1 : 0;
        if (wanted > compressionStage) {
            compressionStage = wanted;
            level.playSound(null, base.offset(0, 1, -14), SoundEvents.STONE_PLACE, SoundSource.BLOCKS,
                    1.4F, wanted == 1 ? 0.48F : 0.38F);
        }

        if (compressionStage >= 1) {
            closeLayer(level, base, inside, now, 2);
        }
        if (compressionStage >= 2) {
            closeLayer(level, base, inside, now, 1);
        }
    }

    private static void closeLayer(ServerLevel level, BlockPos base, List<ServerPlayer> players, long now, int side) {
        for (int z = COMPRESSION_START_Z; z >= COMPRESSION_END_Z; z--) {
            for (int x : new int[] {-side, side}) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = base.offset(x, y, z);
                    ServerPlayer caught = occupant(players, pos);
                    if (caught != null) {
                        pressure(caught, level, base, now);
                        continue;
                    }
                    if (!level.getBlockState(pos).is(WALL.getBlock())) {
                        level.setBlock(pos, WALL, FLAGS);
                    }
                }
            }
        }
    }

    @Nullable
    private static ServerPlayer occupant(List<ServerPlayer> players, BlockPos pos) {
        AABB block = new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D
        );
        for (ServerPlayer player : players) {
            if (player.getBoundingBox().intersects(block)) {
                return player;
            }
        }
        return null;
    }

    private static void pressure(ServerPlayer player, ServerLevel level, BlockPos base, long now) {
        long next = NEXT_PRESSURE_HIT.getOrDefault(player.getUUID(), 0L);
        if (now >= next) {
            player.hurt(level.damageSources().inWall(), 1.0F);
            player.causeFoodExhaustion(0.4F);
            NEXT_PRESSURE_HIT.put(player.getUUID(), now + 20L);
        }
        double center = base.getX() + 0.5D;
        double dx = center - player.getX();
        player.push(Math.max(-0.22D, Math.min(0.22D, dx)), 0.02D, 0.0D);
    }

    private static void openCompression(ServerLevel level, BlockPos base) {
        for (int z = COMPRESSION_START_Z; z >= COMPRESSION_END_Z; z--) {
            for (int x : new int[] {-2, -1, 1, 2}) {
                for (int y = 0; y <= 2; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), FLAGS);
                }
            }
        }
    }

    private static void resetCompression() {
        compressionStarted = -1L;
        compressionStage = 0;
        NEXT_PRESSURE_HIT.clear();
    }

    public static void clearAll() {
        FALSE_DISTANCE_LAPS.clear();
        FALSE_DISTANCE_COOLDOWN.clear();
        SINK_LIGHTS.clear();
        movingDoorIndex = 0;
        movingDoorNextTick = 0L;
        resetCompression();
    }

    public static int compressionStage() {
        return compressionStage;
    }
}
