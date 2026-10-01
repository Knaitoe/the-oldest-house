package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Real floor geometry and reversible, same-tick identifications of hidden corridor planes. */
public final class LabyrinthMaze {
    private record Trace(LabyrinthPlace place, Vec3 previous, long tick) {}
    private static final Map<UUID, Trace> TRACES = new HashMap<>();
    private static final Map<LabyrinthPlace, MazeLayout> LAYOUTS = new HashMap<>();
    private LabyrinthMaze() {}

    public static boolean isMaze(LabyrinthPlace place) {
        return place == LabyrinthPlace.GRAY_CORRIDOR || place == LabyrinthPlace.FOLDED_MAZE
                || place == LabyrinthPlace.DEEP_MAZE || place == LabyrinthPlace.ABYSS_MAZE;
    }
    public static int minimumDepth(LabyrinthPlace place) {
        return switch (place) {
            case FOLDED_MAZE -> LabyrinthPacing.STRANGE_DEPTH;
            case DEEP_MAZE -> LabyrinthPacing.DEEP_DEPTH;
            case ABYSS_MAZE -> LabyrinthPacing.ABYSS_DEPTH;
            default -> 0;
        };
    }
    public static MazeLayout layout(MinecraftServer server, LabyrinthPlace place) {
        return LAYOUTS.computeIfAbsent(place, p -> {
            LabyrinthData data = LabyrinthData.get(server);
            CompoundTag state = data.state(p.id());
            if (!state.contains("MazeSeed")) {
                state.putLong("MazeSeed", server.overworld().getSeed() ^ ((long) p.id().hashCode() * 0x9E3779B97F4A7C15L));
                data.setState(p.id(), state);
            }
            return MazeLayout.create(p, state.getLong("MazeSeed"));
        });
    }

    public static void build(ServerLevel level, BlockPos base, LabyrinthPlace place, MazeLayout layout) {
        BoundingBox bounds = place.room();
        int flags = LabyrinthBuilder.flags();
        // Also works in an empty GameTest world; uncarved cells are actual solid walls.
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) for (int z = bounds.minZ(); z <= 0; z++) {
            BlockPos column = new BlockPos(x, 0, z);
            boolean open = layout.floor().contains(column);
            for (int y = -1; y <= 4; y++) {
                BlockState state = y == -1 ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : y == 4 ? Blocks.STONE.defaultBlockState()
                        : open ? Blocks.AIR.defaultBlockState() : Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
                level.setBlock(base.offset(x, y, z), state, flags);
            }
        }
        LabyrinthBuilder.entrance(level, base, Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),
                Blocks.SMOOTH_STONE.defaultBlockState(), Blocks.STONE.defaultBlockState());
        LabyrinthBuilder.doors(level, base, place);
        refreshLights(level, base, place, Math.max(0, LabyrinthLighting.darknessBand(minimumDepth(place))), 0);
        LabyrinthDomestic.decorateMaze(level, base, place, layout);
    }

    public static List<BlockPos> lightPositions(LabyrinthPlace place, int band, int layout) {
        int size = MazeLayout.size(place);
        int count = Math.max(0, 3 - band);
        List<BlockPos> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int row = i * (size - 1) / 2;
            int side = Math.floorMod(layout, 3) - 1;
            result.add(new BlockPos(side * 5, 3, -3 - row * 5));
        }
        return result;
    }
    public static void refreshLights(ServerLevel level, BlockPos base, LabyrinthPlace place, int band, int layout) {
        for (int variant = 0; variant < 3; variant++) for (BlockPos rel : lightPositions(place, 0, variant)) {
            if (level.getBlockState(base.offset(rel)).is(Blocks.SOUL_LANTERN)
                    || level.getBlockState(base.offset(rel)).is(Blocks.LANTERN)) {
                level.setBlock(base.offset(rel), Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
            }
        }
        for (BlockPos rel : lightPositions(place, band, layout)) LabyrinthBuilder.hangLantern(level, base.offset(rel), band > 0);
    }
    public static boolean nearFold(MazeLayout plan, BlockPos relative) {
        for (MazeLayout.Sleeve sleeve : plan.sleeves()) {
            Vec3 local = sleeve.local(Vec3.atBottomCenterOf(relative));
            if (Math.abs(local.x) <= 4 && Math.abs(local.z) <= 4) return true;
        }
        return false;
    }
    static void tick(ServerPlayer player, LabyrinthPlace place, BlockPos base) {
        long now = player.serverLevel().getGameTime();
        Vec3 current = player.position().subtract(Vec3.atLowerCornerOf(base));
        Trace previous = TRACES.put(player.getUUID(), new Trace(place, current, now));
        if (previous == null || previous.place != place || now - previous.tick > 2 || player.isSpectator()) return;
        List<MazeLayout.Sleeve> sleeves = layout(player.server, place).sleeves();
        for (int i = 0; i < sleeves.size(); i++) {
            MazeLayout.Sleeve from = sleeves.get(i);
            if (!MazeLayout.crosses(from, previous.previous, current)) continue;
            MazeLayout.Sleeve to = sleeves.get(sleeves.size() - 1 - i);
            Vec3 target = MazeLayout.fold(from, to, current);
            Vec3 movement = MazeLayout.rotate(player.getDeltaMovement(), to.turn() - from.turn());
            LabyrinthDoors.shift(player, target.add(Vec3.atLowerCornerOf(base)), player.getYRot() + 90 * (to.turn() - from.turn()));
            player.setDeltaMovement(movement);
            // Save the destination sample, preventing a stationary player from bouncing between planes.
            TRACES.put(player.getUUID(), new Trace(place, target, now));
            break;
        }
    }
    public static void forget(UUID player) { TRACES.remove(player); }
    public static void clearAll() { TRACES.clear(); LAYOUTS.clear(); }

    public record Light(BlockPos pos, BlockState state) {}
    public record Migration(List<ServerPlayer> players, List<ItemEntity> items, List<Light> lights) {}
    public static Migration capture(ServerLevel level, BoundingBox slot, BlockPos base) {
        AABB box = new AABB(slot.minX(), slot.minY(), slot.minZ(), slot.maxX() + 1, slot.maxY() + 1, slot.maxZ() + 1);
        List<Light> lights = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(slot.minX(), slot.minY(), slot.minZ(), slot.maxX(), slot.maxY(), slot.maxZ())) {
            BlockState state = level.getBlockState(pos);
            boolean authored = pos.getY() == base.getY() + 3
                    && (state.is(Blocks.SOUL_LANTERN) || state.is(Blocks.LANTERN))
                    && java.util.List.of(LabyrinthPlace.GRAY_CORRIDOR, LabyrinthPlace.FOLDED_MAZE,
                            LabyrinthPlace.DEEP_MAZE, LabyrinthPlace.ABYSS_MAZE).stream().anyMatch(place ->
                            java.util.stream.IntStream.range(0, 3).anyMatch(variant -> lightPositions(place, 0, variant)
                                    .stream().anyMatch(p -> base.offset(p).equals(pos))));
            if (LabyrinthLighting.isPortableLight(state) && !authored)
                lights.add(new Light(pos.immutable(), state));
        }
        return new Migration(level.players().stream().filter(p -> box.contains(p.position())).toList(),
                level.getEntitiesOfClass(ItemEntity.class, box), lights);
    }
    public static void restoreMigration(ServerLevel level, BlockPos base, LabyrinthPlace place, Migration migration) {
        MazeLayout plan = layout(level.getServer(), place);
        Vec3 safe = Vec3.atBottomCenterOf(base.offset(0, 0, -3));
        for (ServerPlayer player : migration.players) if (!clear(level, player.blockPosition())) LabyrinthDoors.shift(player, safe, player.getYRot());
        for (ItemEntity item : migration.items) if (!item.isRemoved() && !clear(level, item.blockPosition())) item.setPos(safe.x, safe.y + .2, safe.z);
        for (Light light : migration.lights) {
            BlockPos target = light.pos;
            if (!level.getBlockState(target).isAir() || !level.getBlockState(target.below()).isSolid()
                    || nearFold(plan, target.subtract(base))) {
                target = plan.floor().stream().filter(p -> !nearFold(plan, p)).map(base::offset)
                        .filter(p -> level.getBlockState(p).isAir()).min(java.util.Comparator.comparingDouble(p -> p.distSqr(light.pos))).orElse(null);
            }
            if (target != null) {
                BlockState state = light.state.is(Blocks.WALL_TORCH) ? Blocks.TORCH.defaultBlockState()
                        : light.state.is(Blocks.SOUL_WALL_TORCH) ? Blocks.SOUL_TORCH.defaultBlockState() : light.state;
                level.setBlock(target, state, LabyrinthBuilder.flags());
            }
        }
    }
    private static boolean clear(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }
}
