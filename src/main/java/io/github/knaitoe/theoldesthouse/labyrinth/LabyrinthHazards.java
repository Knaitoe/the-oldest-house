package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Physical gray-space hazards.
 *
 * None is an instant-kill trap. They are meant to spend breath, food, health,
 * time or carried blocks, so a sequence of survivable problems becomes the
 * actual danger of being lost.
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
    private static long compressionStarted = -1L;
    private static int compressionStage;
    private static final Map<UUID, Long> NEXT_PRESSURE_HIT = new HashMap<>();

    private LabyrinthHazards() {
    }

    public static void buildFloodedPassage(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -1, 1, 5, -27, -1, WALL, FLOOR, CEILING);

        // Dry threshold, then twenty-one blocks of low submerged tunnel.
        for (int z = -4; z >= -24; z--) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(base.offset(x, 0, z), Blocks.WATER.defaultBlockState(), FLAGS);
                level.setBlock(base.offset(x, 1, z), Blocks.WATER.defaultBlockState(), FLAGS);
                level.setBlock(base.offset(x, 2, z), CEILING, FLAGS);
            }
        }

        // Two unmistakable breathing chimneys. They are deliberately spaced
        // so a player who keeps moving never has to bet the whole route on one
        // breath bar.
        for (int z : new int[] {-9, -17}) {
            level.setBlock(base.offset(0, 2, z), Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(base.offset(0, 3, z), Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(base.offset(0, 4, z), Blocks.AIR.defaultBlockState(), FLAGS);
            LabyrinthBuilder.hangLantern(level, base.offset(0, 5, z), true);
        }

        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FLOODED_PASSAGE);
    }

    public static void buildFracturedWalkway(ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -3, 3, 10, -27, -1, WALL, FLOOR, CEILING);

        // A broad stair takes the player onto a platform seven blocks above
        // the lower floor. Missing the broken span therefore hurts, but cannot
        // turn into a lethal void fall.
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
        // z -12 through -15 is the missing span: four blocks, difficult to
        // clear cleanly but bridgeable with carried blocks.
        for (int z = -16; z >= -27; z--) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(base.offset(x, 6, z), Blocks.STONE_BRICKS.defaultBlockState(), FLAGS);
            }
        }

        // Falling is a setback, not a soft lock. A second stair on the east
        // side climbs from the lower floor back to the far platform.
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

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 5 != 0) {
            return;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (level == null || origin == null) {
            resetState();
            return;
        }

        tickFlooded(level, origin, server.getTickCount());
        tickCompression(level, origin, server.getTickCount());
    }

    private static void tickFlooded(ServerLevel level, BlockPos origin, long now) {
        if (now % 20L != 0L) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.FLOODED_PASSAGE
                    && player.isInWater()) {
                // Vanilla swimming already costs hunger. This small extra cost
                // makes repeated flooded routes matter without making one
                // crossing a death sentence.
                player.causeFoodExhaustion(0.12F);
            }
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
            resetState();
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

    private static void closeLayer(
            ServerLevel level,
            BlockPos base,
            List<ServerPlayer> players,
            long now,
            int side
    ) {
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

    private static void resetState() {
        compressionStarted = -1L;
        compressionStage = 0;
        NEXT_PRESSURE_HIT.clear();
    }

    public static void clearAll() {
        resetState();
    }

    public static int compressionStage() {
        return compressionStage;
    }
}
