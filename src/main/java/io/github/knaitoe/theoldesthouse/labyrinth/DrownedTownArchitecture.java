package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

/** A sealed night sky over real source water; all streets and interiors can be swum through. */
public final class DrownedTownArchitecture {
    private static final BlockState NIGHT = Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final int F = LabyrinthBuilder.flags();
    private DrownedTownArchitecture() {}

    public static void build(ServerLevel level, BlockPos base) {
        for (int x = -29; x <= 29; x++) for (int z = -64; z <= 0; z++) for (int y = -13; y <= 8; y++) {
            BlockState state;
            if (x == -29 || x == 29 || z == -64 || z == 0 || y == 8) state = NIGHT;
            else if (y == -13) state = Blocks.DEEPSLATE.defaultBlockState();
            else if (z >= -11 && y <= -1) state = shore(x, z);
            else if (y == -12) state = Blocks.GRAVEL.defaultBlockState();
            else if (z <= -12 && y <= -1) state = WATER;
            else state = Blocks.AIR.defaultBlockState();
            level.setBlock(base.offset(x, y, z), state, F);
        }
        // A low dark bank and stumps hide the square boundary in a tree line.
        for (int x = -27; x <= 27; x += 6) {
            if (Math.abs(x) < 4) continue;
            for (int y = 0; y <= 4 + Math.floorMod(x, 3); y++)
                level.setBlock(base.offset(x, y, -1), Blocks.DARK_OAK_LOG.defaultBlockState(), F);
            for (int dx = -2; dx <= 2; dx++) for (int dz = -1; dz <= 1; dz++)
                level.setBlock(base.offset(x + dx, 5, -2 + dz), Blocks.DARK_OAK_LEAVES.defaultBlockState()
                        .setValue(LeavesBlock.PERSISTENT, true), F);
        }
        // Native grass must stay grass beneath the authored night ceiling.
        for (int x = -28; x <= 28; x++) for (int z = -10; z <= -2; z++)
            if (isGrassPatch(x, z) && Math.floorMod(x + z, 2) == 0)
                light(level, base.offset(x, 1, z), 10);
        for (int z = -13; z >= -60; z--) for (int x = -3; x <= 3; x++)
            level.setBlock(base.offset(x, -12, z), Math.abs(x) == 3 ? Blocks.STONE_BRICKS.defaultBlockState()
                    : (x == 0 && z % 4 < -1 ? Blocks.YELLOW_TERRACOTTA.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState()), F);
        for (int x = -26; x <= 26; x++) for (int z = -38; z <= -34; z++)
            level.setBlock(base.offset(x, -12, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), F);

        school(level, base);
        church(level, base);
        house(level, base, -24, -15, -59, -46, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        house(level, base, 9, 22, -31, -20, Blocks.BRICKS.defaultBlockState());
        // Lampposts and the school sign are orientation aids seen from the surface.
        for (int z : new int[]{-18, -38, -60}) {
            for (int y = -11; y <= -7; y++) level.setBlock(base.offset(4, y, z), Blocks.IRON_BARS.defaultBlockState(), F);
            light(level, base.offset(4, -6, z), 9);
        }
        // An old well supplies a genuine upward bubble column, with a readable stone rim.
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            if (dx != 0 || dz != 0) level.setBlock(base.offset(-2 + dx, -11, -18 + dz), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), F);
        level.setBlock(base.offset(-2, -12, -18), Blocks.SOUL_SAND.defaultBlockState(), F);
        for (int y = -11; y <= -1; y++) level.setBlock(base.offset(-2, y, -18),
                Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, false), F);

        // The shore furnace is deliberately on bare dirt, several steps from the nearest grass.
        level.setBlock(base.offset(DrownedTown.FURNACE), Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH), F);
        level.setBlock(base.offset(DrownedTown.SUPPLIES), LabyrinthBuilder.barrel(Direction.UP), F);
        level.setBlock(base.offset(-5, 0, -7), LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH), F);
        level.setBlock(base.offset(-4, 0, -7), LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH), F);
        level.setBlock(base.offset(-6, 1, -7), Blocks.LANTERN.defaultBlockState(), F);
        level.setBlock(base.offset(-6, 0, -7), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), F);
        level.setBlock(base.offset(-3, 0, -5), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH), F);
        if (level.getBlockEntity(base.offset(-3, 0, -5)) instanceof LecternBlockEntity desk) {
            desk.setBook(HouseWriting.book("At the waterline", "An explorer", HouseWriting.WritingStyle.PLAIN, List.of(
                    "I saw her come along the dirt.\n\nShe stopped at the grass.\n\nShe followed the edge when I swam out. She was still there when I came back for air.",
                    "The school is left of the main street. Its papers are soaked.\n\nThere is a furnace here, and fuel beside it.\n\nThe old well still makes bubbles. The school door holds a breath.",
                    "The steeple belongs to the church. There is a voice beneath it.\n\nI could not turn the door handle.")));
            desk.setChanged();
            level.setBlock(desk.getBlockPos(), level.getBlockState(desk.getBlockPos()).setValue(LecternBlock.HAS_BOOK, true), F);
        }
        sign(level, base.offset(-3, 0, -10), "INDIAN LAKE", "SCHOOL: LEFT", "CHURCH: RIGHT", "THE WELL BREATHES");
        // A small moon and sparse pinholes are physical scenery, never a change to shared world time.
        level.setBlock(base.offset(19, 7, -59), Blocks.SEA_LANTERN.defaultBlockState(), F);
        level.setBlock(base.offset(20, 7, -59), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), F);
        for (int x = -24; x <= 24; x += 12) light(level, base.offset(x, 7, -52 - Math.floorMod(x, 9)), 5);
        LabyrinthBuilder.entrance(level, base, NIGHT, Blocks.COARSE_DIRT.defaultBlockState(), NIGHT);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.DROWNED_TOWN);
    }

    public static boolean isGrassPatch(int x, int z) {
        return (x >= -2 && x <= 3 && z >= -5 && z <= -2)
                || (x >= 18 && x <= 22 && z >= -10 && z <= -6)
                || (x >= -23 && x <= -19 && z >= -10 && z <= -7);
    }
    private static BlockState shore(int x, int z) {
        return isGrassPatch(x, z) ? Blocks.GRASS_BLOCK.defaultBlockState()
                : (Math.floorMod(x * 17 + z * 7, 5) < 2 ? Blocks.PODZOL : Blocks.COARSE_DIRT).defaultBlockState();
    }
    private static void light(ServerLevel level, BlockPos pos, int brightness) {
        level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, brightness), F);
    }
    private static void school(ServerLevel level, BlockPos base) {
        shell(level, base, -25, -7, -34, -23, -11, -6, Blocks.BRICKS.defaultBlockState());
        // Water stays in the hall, classrooms and between the desks.
        for (int z = -33; z <= -24; z++) for (int y = -11; y <= -7; y++)
            if (z != -26 && z != -30) level.setBlock(base.offset(-16, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), F);
        for (int x : new int[]{-22, -19, -12, -9}) for (int z : new int[]{-26, -30}) {
            level.setBlock(base.offset(x, -11, z), Blocks.SPRUCE_FENCE.defaultBlockState(), F);
            level.setBlock(base.offset(x, -10, z), Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true), F);
            level.setBlock(base.offset(x, -11, z + 1), LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH).setValue(StairBlock.WATERLOGGED, true), F);
        }
        level.setBlock(base.offset(-14, -9, -34), Blocks.GREEN_CONCRETE.defaultBlockState(), F);
        for (BlockPos paper : DrownedTown.PAPERS) level.setBlock(base.offset(paper), LabyrinthBuilder.barrel(Direction.UP), F);
        level.setBlock(base.offset(DrownedTown.KEY_DESK), Blocks.POLISHED_ANDESITE.defaultBlockState(), F);
        woodenDoor(level, base.offset(DrownedTown.SCHOOL_DOOR), Direction.SOUTH);
        for (int x : new int[]{-23, -20, -11, -8}) for (int y = -9; y <= -8; y++)
            level.setBlock(base.offset(x, y, -22), Blocks.GLASS.defaultBlockState(), F);
        sign(level, base.offset(-15, -8, -22), "INDIAN LAKE", "SCHOOL", "FILM STUDIES", "");
        light(level, base.offset(-21, -6, -28), 8);
        light(level, base.offset(-10, -6, -29), 8);
    }
    private static void church(ServerLevel level, BlockPos base) {
        shell(level, base, 8, 22, -58, -41, -11, -5, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        for (int z = -51; z <= -44; z += 3) for (int x : new int[]{10, 11, 12, 18, 19, 20})
            level.setBlock(base.offset(x, -11, z), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH).setValue(StairBlock.WATERLOGGED, true), F);
        level.setBlock(base.offset(15, -11, -55), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), F);
        level.setBlock(base.offset(15, -10, -55), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true), F);
        BlockState gate = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
        level.setBlock(base.offset(DrownedTown.CHURCH_DOOR), gate.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), F);
        level.setBlock(base.offset(DrownedTown.CHURCH_DOOR).above(), gate.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), F);
        level.setBlock(base.offset(DrownedTown.ROOF_HATCH), Blocks.IRON_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.HALF, Half.TOP)
                .setValue(TrapDoorBlock.WATERLOGGED, true), F);
        for (int z : new int[]{-45, -50, -55}) for (int y = -9; y <= -7; y++) {
            level.setBlock(base.offset(7, y, z), Blocks.BLUE_STAINED_GLASS.defaultBlockState(), F);
            level.setBlock(base.offset(23, y, z), Blocks.BLUE_STAINED_GLASS.defaultBlockState(), F);
        }
        for (int x = 13; x <= 17; x++) for (int z = -58; z <= -54; z++) for (int y = -4; y <= 3; y++) {
            boolean wall = x == 13 || x == 17 || z == -58 || z == -54;
            level.setBlock(base.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState()
                    : (y <= -1 ? WATER : Blocks.AIR.defaultBlockState()), F);
        }
        for (int x = 13; x <= 17; x++) for (int z = -58; z <= -54; z++)
            level.setBlock(base.offset(x, 4, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), F);
        level.setBlock(base.offset(15, 5, -56), Blocks.DEEPSLATE_WALL.defaultBlockState(), F);
        light(level, base.offset(15, -7, -52), 7);
    }
    private static void house(ServerLevel level, BlockPos base, int x0, int x1, int z0, int z1, BlockState wall) {
        shell(level, base, x0, x1, z0, z1, -11, -8, wall);
        BlockPos doorway = base.offset((x0 + x1) / 2, -11, z1 + 1);
        woodenDoor(level, doorway, Direction.SOUTH);
        for (int x = x0 + 1; x <= x1 - 1; x += 3) level.setBlock(base.offset(x, -9, z1 + 1), Blocks.GLASS.defaultBlockState(), F);
    }
    private static void shell(ServerLevel level, BlockPos base, int x0, int x1, int z0, int z1, int floor, int ceiling, BlockState wall) {
        for (int x = x0 - 1; x <= x1 + 1; x++) for (int z = z0 - 1; z <= z1 + 1; z++) for (int y = floor - 1; y <= ceiling + 1; y++) {
            boolean boundary = x == x0 - 1 || x == x1 + 1 || z == z0 - 1 || z == z1 + 1;
            level.setBlock(base.offset(x, y, z), boundary || y == floor - 1 || y == ceiling + 1 ? wall : WATER, F);
        }
    }
    private static void woodenDoor(ServerLevel level, BlockPos at, Direction facing) {
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, facing);
        level.setBlock(at, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), F);
        level.setBlock(at.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), F);
    }
    private static void sign(ServerLevel level, BlockPos at, String... lines) {
        level.setBlock(at, Blocks.SPRUCE_SIGN.defaultBlockState(), F);
        if (level.getBlockEntity(at) instanceof SignBlockEntity sign) {
            var text = sign.getFrontText();
            for (int i = 0; i < 4; i++) text = text.setMessage(i, Component.literal(lines[i]));
            sign.setText(text, true); sign.setWaxed(true); sign.setChanged();
        }
    }
}
