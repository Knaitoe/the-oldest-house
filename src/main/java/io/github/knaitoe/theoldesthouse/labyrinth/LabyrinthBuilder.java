package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
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
import net.minecraft.world.phys.AABB;

/**
 * Carves the labyrinth's places into the solid labyrinth dimension and
 * registers their doors. Everything is authored here in code for now; the
 * design's structure files built in creative can replace these later.
 */
public final class LabyrinthBuilder {
    /** Bump to rebuild every place in existing worlds on next use. */
    public static final int VERSION = 1;

    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** The gray: plain, dim, nobody's. */
    private static final BlockState GRAY_WALL = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState GRAY_CEILING = Blocks.STONE.defaultBlockState();

    private LabyrinthBuilder() {
    }

    /** Builds (or rebuilds, after an update) every place, once. */
    public static boolean ensureBuilt(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        if (data.builtVersion() >= VERSION) {
            return true;
        }
        return buildAll(server);
    }

    public static boolean buildAll(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.LABYRINTH);
        if (level == null) {
            TheOldestHouse.LOGGER.error("The labyrinth dimension is missing; cannot build it.");
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            BlockPos base = place.base();
            if (base == null) {
                continue;
            }
            switch (place) {
                case JUNCTION -> buildJunction(level, base);
                case GRAY_CORRIDOR -> buildCorridor(level, base);
                case FLOORBOARDS -> TellTaleFloorboards.build(level, base, !data.isCompleted(place.id()));
                default -> {
                }
            }
            registerDoors(data, place);
        }
        data.setBuiltVersion(VERSION);
        LabyrinthDoors.syncSealedDoors(server);
        TheOldestHouse.LOGGER.info("Carved the labyrinth (version {}).", VERSION);
        return true;
    }

    public static void registerDoors(LabyrinthData data, LabyrinthPlace place) {
        BlockPos base = place.base();
        if (base == null) {
            return;
        }
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            data.putDoor(new LabyrinthData.Door(
                    place.doorId(spec), HouseDimensions.LABYRINTH, base.offset(spec.rel()), spec.facing(), spec.destination(), false));
        }
    }

    // ------------------------------------------------------------------
    // The gray

    /** A plain gray hall, 9 by 13, with a door in each wall. */
    public static void buildJunction(ServerLevel level, BlockPos base) {
        room(level, base, -4, 4, 4, 0, 12, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        hangLantern(level, base.offset(0, 4, 3), true);
        hangLantern(level, base.offset(0, 4, 9), true);
        // A single bench against the west wall, facing nothing.
        level.setBlock(base.offset(-4, 0, 9), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        level.setBlock(base.offset(-4, 0, 10), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        doors(level, base, LabyrinthPlace.JUNCTION);
    }

    /** A long, narrow gray corridor. */
    public static void buildCorridor(ServerLevel level, BlockPos base) {
        room(level, base, -1, 1, 3, 0, 28, GRAY_WALL, GRAY_FLOOR, GRAY_CEILING);
        for (int z = 4; z <= 28; z += 9) {
            hangLantern(level, base.offset(0, 3, z), true);
        }
        doors(level, base, LabyrinthPlace.GRAY_CORRIDOR);
    }

    // ------------------------------------------------------------------
    // Helpers, shared with the vignettes

    /**
     * Carves a room: interior x0..x1, 0..height, 0..z1 (relative to base),
     * wrapped in one block of wall, floor and ceiling. The labyrinth around
     * it is solid already; this also works in an empty test world.
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

    static void doors(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            placeDoor(level, base.offset(spec.rel()), spec.facing());
        }
    }

    /**
     * An ordinary spruce door, shut, set into a wall. Facing the room it is
     * used from, it sits on the far side of the wall, as a door in a thick
     * wall does.
     */
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
