package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Below twelve doors the house stops pretending to be a home. The ordinary
 * ways on are dressed stone: a vaulted gallery, a pillared crossing around a
 * dry fountain, and a stair that really goes down. Masonry is laid with the
 * cracks and moss of age, and every lamp hangs from the vault.
 */
public final class StoneHalls {
    private StoneHalls() {}

    public static boolean isStone(LabyrinthPlace place) {
        return place == LabyrinthPlace.STONE_GALLERY || place == LabyrinthPlace.STONE_CROSSING || place == LabyrinthPlace.STONE_DESCENT
                || place == LabyrinthPlace.STONE_ARCADE || place == LabyrinthPlace.STONE_BEND || place == LabyrinthPlace.STONE_LANDING;
    }

    private static final BlockState BRICK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState CHISELED = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
    private static final BlockState POLISHED = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState DIORITE = Blocks.POLISHED_DIORITE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** Aged masonry: mostly dressed brick, with cracked and mossy courses. */
    private static BlockState wall(BlockPos at) {
        int h = Math.floorMod(at.getX() * 73428767 ^ at.getY() * 19349663 ^ at.getZ() * 83492791, 100);
        return h < 9 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : h < 17 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : BRICK;
    }

    private static BlockState stair(Direction facing, boolean top) {
        return Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    private static void set(ServerLevel level, BlockPos at, BlockState state) {
        BuildBlocks.set(level, at, state, LabyrinthBuilder.flags());
    }

    public static void build(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        switch (place) {
            case STONE_GALLERY -> gallery(level, base);
            case STONE_CROSSING -> crossing(level, base);
            case STONE_DESCENT -> descent(level, base);
            case STONE_ARCADE, STONE_BEND, STONE_LANDING -> HallVariations.buildStone(level,base,place);
            default -> throw new IllegalArgumentException(place.id());
        }
        LabyrinthBuilder.entrance(level, base, BRICK, POLISHED, BRICK);
        LabyrinthBuilder.doors(level, base, place);
    }

    // ------------------------------------------------------------------

    /** A shell: walls, floor and ceiling in masonry around an empty interior. */
    private static void shell(ServerLevel level, BlockPos base, int x0, int x1, int z0, int z1, int height) {
        for (int x = x0 - 1; x <= x1 + 1; x++)
            for (int z = z0 - 1; z <= z1 + 1; z++)
                for (int y = -1; y <= height + 1; y++) {
                    BlockPos at = base.offset(x, y, z);
                    boolean inside = x >= x0 && x <= x1 && z >= z0 && z <= z1;
                    if (inside && y >= 0 && y <= height) set(level, at, AIR);
                    else if (inside && y == -1) set(level, at, x == 0 ? BRICK : POLISHED);
                    else if (inside) set(level, at, BRICK);
                    else set(level, at, wall(at));
                }
        // The vault: a chamfer of upturned steps where the walls meet the ceiling.
        for (int z = z0; z <= z1; z++) {
            set(level, base.offset(x0, height, z), stair(Direction.WEST, true));
            set(level, base.offset(x1, height, z), stair(Direction.EAST, true));
        }
    }

    private static void gallery(ServerLevel level, BlockPos base) {
        shell(level, base, -3, 3, -41, -1, 5);
        for (int z = -4; z >= -40; z -= 6) {
            // Pilasters on a chiselled plinth, carrying a rib across the vault.
            for (int x : new int[]{-3, 3}) {
                if (x == 3 && z <= -18 && z >= -24) continue; // the side way stays clear
                set(level, base.offset(x, 0, z), CHISELED);
                for (int y = 1; y <= 4; y++) set(level, base.offset(x, y, z), BRICK);
            }
            for (int x = -2; x <= 2; x++)
                set(level, base.offset(x, 5, z), Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        }
        // Benches in the bays along the west wall, as in a cloister.
        for (int z : new int[]{-13, -25, -37}) set(level, base.offset(-3, 0, z), stair(Direction.WEST, false));
        for (int z : new int[]{-7, -19, -31, -38}) LabyrinthBuilder.hangLantern(level, base.offset(0, 5, z), false);
    }

    private static void crossing(ServerLevel level, BlockPos base) {
        shell(level, base, -11, 11, -25, -1, 6);
        int cz = -13;
        // A floor laid in rings around the fountain.
        for (int x = -11; x <= 11; x++)
            for (int z = -25; z <= -1; z++) {
                int d = Math.max(Math.abs(x), Math.abs(z - cz));
                set(level, base.offset(x, -1, z), d == 4 || d == 8 ? DIORITE : d < 4 ? BRICK : POLISHED);
            }
        // Four pillars with chiselled bases and capitals.
        for (int px : new int[]{-6, 6})
            for (int pz : new int[]{-7, -19})
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        for (int y = 0; y <= 6; y++)
                            set(level, base.offset(px + dx, y, pz + dz), (y == 0 || y == 5) && dx != 0 && dz != 0 ? CHISELED : BRICK);
        // The dry fountain: a low parapet ring and an empty basin.
        for (int x = -2; x <= 2; x++)
            for (int z = cz - 2; z <= cz + 2; z++)
                if (Math.abs(x) == 2 || Math.abs(z - cz) == 2) set(level, base.offset(x, 0, z), Blocks.STONE_BRICK_WALL.defaultBlockState());
        set(level, base.offset(0, 0, cz), Blocks.CAULDRON.defaultBlockState());
        for (BlockPos lamp : new BlockPos[]{new BlockPos(0, 6, -4), new BlockPos(0, 6, -22), new BlockPos(-9, 6, cz), new BlockPos(9, 6, cz)})
            LabyrinthBuilder.hangLantern(level, base.offset(lamp), false);
    }

    /** The floor level of the stair at a given depth into the room. */
    static int level(int z) {
        if (z >= -6) return 0;
        if (z >= -14) return -((-z - 5) / 2);
        return -4;
    }

    private static void descent(ServerLevel level, BlockPos base) {
        for (int z = 0; z >= -34; z--)
            for (int x = -3; x <= 3; x++) {
                int floor = level(Math.max(-33, Math.min(-1, z)));
                for (int y = floor - 1; y <= floor + 5; y++) {
                    BlockPos at = base.offset(x, y, z);
                    boolean inside = Math.abs(x) <= 2 && z <= -1 && z >= -33;
                    if (!inside) set(level, at, wall(at));
                    else if (y == floor - 1) set(level, at, x == 0 ? BRICK : POLISHED);
                    else if (y == floor + 5) set(level, at, BRICK);
                    else set(level, at, AIR);
                }
                // A rising step at the start of each lower level, so the climb back needs no jump.
                boolean first = z <= -7 && z >= -13 && (-z - 7) % 2 == 0;
                if (first && Math.abs(x) <= 2) set(level, base.offset(x, floor, z), stair(Direction.SOUTH, false));
            }
        for (int z = -16; z >= -32; z -= 4)
            for (int x : new int[]{-2, 2}) set(level, base.offset(x, -4, z), CHISELED);
        for (int z : new int[]{-3, -11, -21, -30}) LabyrinthBuilder.hangLantern(level, base.offset(0, level(z) + 4, z), false);
    }
}
