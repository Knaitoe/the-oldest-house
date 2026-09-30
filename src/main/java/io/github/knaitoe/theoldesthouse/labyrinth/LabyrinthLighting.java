package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The gray's relationship with light.
 *
 * Early on, somebody has tried to help. Deeper in, ordinary darkness becomes
 * harder to see through and unattended light sources stop being reliable
 * landmarks. The House rearranges lights, rather than simply deleting them:
 * the exception is the authored Light Sink, whose entire point is consumption.
 */
public final class LabyrinthLighting {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public static final BlockPos TOM_NOTE = new BlockPos(-3, 0, -6);
    public static final BlockPos TOM_CACHE = new BlockPos(3, 0, -9);

    private static final Map<UUID, Integer> DEPTH = new HashMap<>();
    private static final Map<UUID, Long> NEXT_SHIFT = new HashMap<>();

    private LabyrinthLighting() {
    }

    public static void buildEarlyAid(MinecraftServer server, ServerLevel level, BlockPos base) {
        level.setBlock(
                base.offset(TOM_NOTE),
                Blocks.LECTERN.defaultBlockState()
                        .setValue(LecternBlock.FACING, Direction.EAST)
                        .setValue(LecternBlock.HAS_BOOK, true),
                FLAGS
        );
        if (level.getBlockEntity(base.offset(TOM_NOTE)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(tomsNote());
        }

        level.setBlock(base.offset(TOM_CACHE), Blocks.BARREL.defaultBlockState(), FLAGS);
        if (level.getBlockEntity(base.offset(TOM_CACHE)) instanceof Container cache) {
            boolean empty = true;
            for (int i = 0; i < cache.getContainerSize(); i++) {
                empty &= cache.getItem(i).isEmpty();
            }
            if (empty) {
                cache.setItem(0, new ItemStack(Items.TORCH, 14));
                cache.setItem(1, new ItemStack(Items.BREAD, 2));
            }
        }
    }

    public static ItemStack tomsNote() {
        return HouseWriting.book(
                "What I know so far",
                "Tom",
                HouseWriting.WritingStyle.PLAIN,
                List.of(
                        "I have come back to this room from three different doors. I stopped trying to draw the order. "
                                + "There may not be one.\n\nI started marking the way with lights instead.",
                        "There are torches in the barrel. Take them.\n\n"
                                + "One warning: some of mine were not where I left them when I came back. "
                                + "Same number. Different walls.\n\n"
                                + "If that happens, don't waste food putting everything back. Keep moving.\n\n-Tom"
                )
        );
    }

    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        if (place.kind() != LabyrinthPlace.Kind.GRAY) {
            DEPTH.remove(player.getUUID());
            NEXT_SHIFT.remove(player.getUUID());
            return;
        }
        int depth = LabyrinthData.get(player.server).returnDepth(player.getUUID());
        DEPTH.put(player.getUUID(), depth);
        NEXT_SHIFT.putIfAbsent(player.getUUID(), (long) player.server.getTickCount() + rearrangeInterval(depth));
    }

    public static int darknessBand(int returnDepth) {
        if (returnDepth < 3) {
            return 0;
        }
        if (returnDepth < 5) {
            return 1;
        }
        if (returnDepth < 7) {
            return 2;
        }
        return 3;
    }

    public static int rearrangeInterval(int returnDepth) {
        return Math.max(90, 260 - Math.max(0, returnDepth - 2) * 24);
    }

    public static boolean isPortableLight(BlockState state) {
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.CANDLE);
    }

    public static boolean mayRearrange(LabyrinthPlace place) {
        return switch (place) {
            case JUNCTION, GRAY_CORRIDOR, FALSE_DISTANCE, COMPRESSION_PASSAGE,
                    MOVING_THRESHOLD, DUPLICATE_PASSAGE -> true;
            default -> false;
        };
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 10 != 0) {
            return;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (level == null || origin == null) {
            clearAll();
            return;
        }

        long now = server.getTickCount();
        LabyrinthData data = LabyrinthData.get(server);
        for (ServerPlayer player : level.players()) {
            LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, player.blockPosition());
            if (place == null || place.kind() != LabyrinthPlace.Kind.GRAY) {
                DEPTH.remove(player.getUUID());
                NEXT_SHIFT.remove(player.getUUID());
                continue;
            }

            int depth = data.returnDepth(player.getUUID());
            DEPTH.put(player.getUUID(), depth);
            dimForDepth(level, player, place, depth);

            if (darknessBand(depth) == 0 || !mayRearrange(place)) {
                continue;
            }

            long next = NEXT_SHIFT.getOrDefault(player.getUUID(), now + rearrangeInterval(depth));
            if (now < next) {
                NEXT_SHIFT.putIfAbsent(player.getUUID(), next);
                continue;
            }
            NEXT_SHIFT.put(player.getUUID(), now + rearrangeInterval(depth));

            // "Sometimes" matters. A fixed clock becomes a mechanic the
            // player farms; one-in-three checks keeps it uncanny.
            if (player.getRandom().nextInt(3) != 0) {
                continue;
            }
            rearrangeOne(level, origin, place, player);
        }
    }

    private static void dimForDepth(ServerLevel level, ServerPlayer player, LabyrinthPlace place, int depth) {
        int band = darknessBand(depth);
        if (band == 0
                || place == LabyrinthPlace.EXPLORER_CAMP
                || place == LabyrinthPlace.FLOODED_PASSAGE
                || place == LabyrinthPlace.FRACTURED_WALKWAY
                || place == LabyrinthPlace.GRAVITY_DRIFT
                || place == LabyrinthPlace.LIGHT_SINK) {
            return;
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition().above());
        int threshold = switch (band) {
            case 1 -> 1;
            case 2 -> 4;
            default -> 7;
        };
        if (blockLight <= threshold) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, true, false, false));
        } else {
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    public static boolean rearrangeOne(
            ServerLevel level,
            BlockPos origin,
            LabyrinthPlace place,
            ServerPlayer player
    ) {
        if (!mayRearrange(place)) {
            return false;
        }
        BlockPos base = LabyrinthPlaces.base(origin, place);
        BoundingBox room = place.room();
        if (base == null || room == null) {
            return false;
        }

        List<BlockPos> lights = new ArrayList<>();
        for (BlockPos cursor : BlockPos.betweenClosed(
                base.getX() + room.minX(), base.getY() + room.minY(), base.getZ() + room.minZ(),
                base.getX() + room.maxX(), base.getY() + room.maxY(), base.getZ() + room.maxZ())) {
            if (isPortableLight(level.getBlockState(cursor)) && !HouseWatchers.isWatched(level, cursor)) {
                lights.add(cursor.immutable());
            }
        }
        if (lights.isEmpty()) {
            return false;
        }

        // Prefer the lamp furthest from the entrance. Over repeated unseen
        // changes, illumination slowly retreats behind the player.
        lights.sort((a, b) -> Integer.compare(a.getZ(), b.getZ()));
        for (BlockPos source : lights) {
            BlockPos target = retreatTarget(level, base, room, place, source, player);
            if (target == null) {
                continue;
            }
            BlockState moved = standingVersion(level.getBlockState(source));
            level.setBlock(source, Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(target, moved, FLAGS);
            return true;
        }
        return false;
    }

    @Nullable
    private static BlockPos retreatTarget(
            ServerLevel level,
            BlockPos base,
            BoundingBox room,
            LabyrinthPlace place,
            BlockPos source,
            ServerPlayer player
    ) {
        int sourceRelZ = source.getZ() - base.getZ();
        int minZ = Math.max(sourceRelZ + 4, room.minZ() + 1);
        int maxZ = Math.min(-2, room.maxZ() - 1);
        if (minZ > maxZ) {
            return null;
        }

        for (int attempt = 0; attempt < 28; attempt++) {
            int x = room.minX() + 1 + player.getRandom().nextInt(Math.max(1, room.maxX() - room.minX() - 1));
            int z = minZ + player.getRandom().nextInt(maxZ - minZ + 1);
            BlockPos target = base.offset(x, 0, z);
            if (!level.getBlockState(target).isAir()
                    || !level.getBlockState(target.above()).isAir()
                    || !level.getBlockState(target.below()).isCollisionShapeFullBlock(level, target.below())
                    || HouseWatchers.isWatched(level, target)
                    || nearDoor(place, base, target)) {
                continue;
            }
            return target;
        }
        return null;
    }

    private static boolean nearDoor(LabyrinthPlace place, BlockPos base, BlockPos target) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            if (base.offset(spec.rel()).distManhattan(target) <= 2) {
                return true;
            }
        }
        return false;
    }

    private static BlockState standingVersion(BlockState state) {
        if (state.is(Blocks.WALL_TORCH)) {
            return Blocks.TORCH.defaultBlockState();
        }
        if (state.is(Blocks.SOUL_WALL_TORCH)) {
            return Blocks.SOUL_TORCH.defaultBlockState();
        }
        if ((state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN))
                && state.hasProperty(LanternBlock.HANGING)) {
            return state.setValue(LanternBlock.HANGING, false);
        }
        return state;
    }

    public static void clearPlayer(UUID player) {
        DEPTH.remove(player);
        NEXT_SHIFT.remove(player);
    }

    public static void clearAll() {
        DEPTH.clear();
        NEXT_SHIFT.clear();
    }
}
