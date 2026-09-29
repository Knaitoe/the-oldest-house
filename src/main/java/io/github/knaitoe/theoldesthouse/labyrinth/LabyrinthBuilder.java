package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

/**
 * Carves the labyrinth's places into their slots above the manor and
 * registers their doors: one place per tick, each slot first filled solid.
 * Everything is authored here in code for now; structure files built in
 * creative can replace these later.
 */
public final class LabyrinthBuilder {
    /** Bump to rebuild every place in existing worlds on next use. */
    public static final int VERSION = 7;

    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** The solid the slots are made of, and the gray: plain, dim, nobody's. */
    static final BlockState SOLID = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_WALL = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState GRAY_CEILING = Blocks.STONE.defaultBlockState();

    @Nullable
    private static Deque<LabyrinthPlace> pending;
    @Nullable
    private static BlockPos pendingOrigin;

    private LabyrinthBuilder() {
    }

    /** Whether every place stands, carved for this manor by this version. */
    public static boolean isBuilt(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin != null && data.builtVersion() >= VERSION && origin.equals(data.builtOrigin());
    }

    /**
     * Starts carving if the places are not built for this manor yet. Returns
     * true once they all stand; while they are being carved (a place a
     * tick), false.
     */
    public static boolean ensureBuilt(MinecraftServer server) {
        if (isBuilt(server)) {
            return true;
        }
        start(server);
        return false;
    }

    /** Carves every place again, from the beginning. */
    public static boolean rebuild(MinecraftServer server) {
        return start(server);
    }

    private static boolean start(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin == null || server.getLevel(HouseDimensions.INTERIOR) == null) {
            return false;
        }
        if (pending != null && origin.equals(pendingOrigin)) {
            return true;
        }
        pending = new ArrayDeque<>();
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.slot() >= 0) {
                pending.add(place);
            }
        }
        pendingOrigin = origin;
        return true;
    }

    public static boolean isCarving() {
        return pending != null;
    }

    /** One place per tick. */
    public static void tick(MinecraftServer server) {
        if (pending == null || pendingOrigin == null) {
            return;
        }
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            pending = null;
            return;
        }
        LabyrinthPlace place = pending.poll();
        if (place != null) {
            build(server, interior, pendingOrigin, place);
        }
        if (pending.isEmpty()) {
            BlockPos origin = pendingOrigin;
            LabyrinthData data = LabyrinthData.get(server);
            data.pruneDoors(server);
            data.setBuilt(VERSION, origin);
            pending = null;
            pendingOrigin = null;
            LabyrinthDoors.syncSealedDoors(server);
            TheOldestHouse.LOGGER.info("Carved the labyrinth around the manor, {} slot(s) above it (version {}).",
                    LabyrinthPlaces.slotsAbove(origin), VERSION);
        }
    }

    public static void clearAll() {
        pending = null;
        pendingOrigin = null;
    }

    private static void build(MinecraftServer server, ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        BlockPos base = LabyrinthPlaces.base(origin, place);
        BoundingBox slot = LabyrinthPlaces.slotBounds(origin, place);
        if (base == null || slot == null) {
            return;
        }
        fillSolid(level, slot);
        LabyrinthData data = LabyrinthData.get(server);
        switch (place) {
            case JUNCTION -> buildJunction(level, base);
            case GRAY_CORRIDOR -> buildCorridor(level, base);
            case FLOORBOARDS -> TellTaleFloorboards.build(level, base, !data.isCompleted(place.id()));
            case RED_ROOM -> RedRoom.build(server, level, base);
            case HIDE_AND_CLAP -> HideAndClap.build(server, level, base);
            case LONG_HALLWAY, HOTEL_HALLWAY -> LabyrinthLoops.buildHallway(level, base, place);
            case SPIRAL_STAIR -> LabyrinthLoops.buildSpiral(level, base);
            case MODEL_HOME -> ModelHome.build(server, level, base);
            case HARRIGAN -> HarriganVignette.build(server, level, base);
            default -> {
            }
        }
        registerDoors(data, place, base);
    }

    private static void fillSolid(ServerLevel level, BoundingBox slot) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(slot.minY(), level.getMinBuildHeight());
        int maxY = Math.min(slot.maxY(), level.getMaxBuildHeight() - 1);
        for (int x = slot.minX(); x <= slot.maxX(); x++) {
            for (int z = slot.minZ(); z <= slot.maxZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    if (level.getBlockState(pos) != SOLID) {
                        level.setBlock(pos, SOLID, FLAGS);
                    }
                }
            }
        }
    }

    public static void registerDoors(LabyrinthData data, LabyrinthPlace place, BlockPos base) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            data.putDoor(new LabyrinthData.Door(
                    place.doorId(spec), HouseDimensions.INTERIOR, base.offset(spec.rel()), spec.facing(), spec.destination(), false));
        }
    }

    // ------------------------------------------------------------------
    // The gray

    /** A plain gray hall, 9 by 12, with a door in each wall. */
    public static void buildJunction(ServerLevel level, BlockPos base) {
        room(level, base, -4, 4, 4, -12, -1, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        hangLantern(level, base.offset(0, 4, -3), true);
        hangLantern(level, base.offset(0, 4, -9), true);
        // A single bench against the west wall, facing nothing.
        level.setBlock(base.offset(-4, 0, -9), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        level.setBlock(base.offset(-4, 0, -10), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        entrance(level, base, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        doors(level, base, LabyrinthPlace.JUNCTION);
    }

    /** A long, narrow gray corridor. */
    public static void buildCorridor(ServerLevel level, BlockPos base) {
        room(level, base, -1, 1, 3, -27, -1, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        for (int z = -4; z >= -27; z -= 9) {
            hangLantern(level, base.offset(0, 3, z), true);
        }
        entrance(level, base, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        doors(level, base, LabyrinthPlace.GRAY_CORRIDOR);
    }

    // ------------------------------------------------------------------
    // Helpers, shared with the vignettes

    /**
     * Carves a room: interior x0..x1, 0..height, z0..z1 (relative to base),
     * wrapped in one block of wall, floor and ceiling. The slot around it is
     * solid already; this also works in an empty test world.
     */
    static void room(ServerLevel level, BlockPos base, int x0, int x1, int height, int z0, int z1,
                     BlockState wall, BlockState floor, BlockState ceiling) {
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                for (int y = -1; y <= height + 1; y++) {
                    boolean inside = x >= x0 && x <= x1 && z >= z0 && z <= z1;
                    BlockState state;
                    if (!inside) {
                        state = wall;
                    } else if (y == -1) {
                        state = floor;
                    } else if (y == height + 1) {
                        state = ceiling;
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    level.setBlock(base.offset(x, y, z), state, FLAGS);
                }
            }
        }
        AABB bounds = new AABB(base.offset(x0 - 1, -1, z0 - 1).getCenter(), base.offset(x1 + 1, height + 1, z1 + 1).getCenter()).inflate(1.0D);
        level.getEntitiesOfClass(ItemEntity.class, bounds).forEach(ItemEntity::discard);
    }

    /**
     * The way in, for a room whose entry door stands at (0, 0, +1): an
     * opening through the room's own south wall at z 0, a door wall row at
     * z +1 (part of the vestibule, and overwritten with wherever the player
     * came from on each arrival) and, behind it, a plain gray stub of
     * vestibule so the door has something to open onto before anyone has
     * arrived.
     */
    static void entrance(ServerLevel level, BlockPos base, BlockState wall, BlockState floor, BlockState ceiling) {
        entrance(level, base, wall, floor, ceiling, true);
    }

    /** As above; without {@code carveOpening} the room's own doorway at z 0 is left as it is. */
    static void entrance(ServerLevel level, BlockPos base, BlockState wall, BlockState floor, BlockState ceiling, boolean carveOpening) {
        if (carveOpening) {
            level.setBlock(base.offset(0, 0, 0), Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(base.offset(0, 1, 0), Blocks.AIR.defaultBlockState(), FLAGS);
        }
        BoundingBox vestibule = LabyrinthPlaces.localVestibule();
        for (int x = vestibule.minX(); x <= vestibule.maxX(); x++) {
            for (int y = vestibule.minY(); y <= vestibule.maxY(); y++) {
                for (int z = vestibule.minZ(); z <= vestibule.maxZ(); z++) {
                    boolean corridor = z >= 2 && x >= -1 && x <= 1 && y >= 0 && y <= 3;
                    boolean lining = z >= 2 && x >= -2 && x <= 2 && y >= -1 && y <= 4;
                    BlockState state = corridor ? Blocks.AIR.defaultBlockState()
                            : !lining ? SOLID
                            : y == -1 ? floor
                            : y == 4 ? ceiling
                            : wall;
                    if (z == 1) {
                        state = wall;
                    }
                    level.setBlock(base.offset(x, y, z), state, FLAGS);
                }
            }
        }
        for (int z = 6; z <= vestibule.maxZ(); z += 8) {
            hangLantern(level, base.offset(0, 3, z), true);
        }
    }

    static void doors(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            placeDoor(level, base.offset(spec.rel()), spec.facing());
        }
    }

    /** An ordinary spruce door, shut, set into a wall, facing the side it is approached from. */
    public static void placeDoor(ServerLevel level, BlockPos lower, Direction facing) {
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        level.setBlock(lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), FLAGS);
        level.setBlock(lower.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), FLAGS);
    }

    static void hangLantern(ServerLevel level, BlockPos pos, boolean soul) {
        Block lantern = soul ? Blocks.SOUL_LANTERN : Blocks.LANTERN;
        level.setBlock(pos, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true), FLAGS);
    }

    static BlockState stairs(Block block, Direction back) {
        return block.defaultBlockState().setValue(StairBlock.FACING, back);
    }

    static void bed(ServerLevel level, BlockPos foot, Direction toHead, Block bed) {
        BlockState state = bed.defaultBlockState().setValue(BedBlock.FACING, toHead);
        level.setBlock(foot, state.setValue(BedBlock.PART, BedPart.FOOT), FLAGS);
        level.setBlock(foot.relative(toHead), state.setValue(BedBlock.PART, BedPart.HEAD), FLAGS);
    }

    static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    static BlockState candle(int count, boolean lit) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, lit);
    }

    static int flags() {
        return FLAGS;
    }
}
