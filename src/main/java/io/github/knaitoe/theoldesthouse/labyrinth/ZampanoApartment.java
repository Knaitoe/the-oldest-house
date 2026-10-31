package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.WallSide;

/**
 * 0.4.201: Zampanò's apartment court, built whole (docs/OWNER_NOTES_0_4_201.md). Until now the courtyard was a mossy box that
 * seven later passes dressed in turn: facades on the invisible walls, an archive exterior, beams, chess tables, scribbles,
 * a handwriting wall. Here it is one old courtyard building, the way the novel has him: a ground-floor flat at the back of a
 * court of stucco flats, his windows sealed, cats in the court, and inside, the papers.
 *
 * <p>Every place the scene's mechanics and tests name is where it was: the entry, the two side doors at x ±12, the
 * fountain, the mat, the sealed iron door, the reading desk, the trunk and its gouges, the drawers, the files and their
 * labels, the drafts on their desks and the sheets on his walls. No generic dressing pass touches the court.
 */
public final class ZampanoApartment {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private static final Block STUCCO = Blocks.WHITE_TERRACOTTA, TRIM = Blocks.SMOOTH_SANDSTONE, PLASTER = NovelRegistry.PLASTER.get();
    /** The court's open ground, between the two wings and the gatehouse, up to his front wall. */
    public static final int COURT_X = 11, COURT_FRONT = -1, COURT_BACK = -18;

    private ZampanoApartment() {}

    public static void build(ServerLevel l, BlockPos b) {
        clear(l, b);
        court(l, b);
        gatehouse(l, b);
        for (int side : new int[]{-1, 1}) wing(l, b, side);
        frontage(l, b);
        flat(l, b);
        furnish(l, b);
        papers(l, b);
        CourtyardFountain.build(l, b);
        ArchiveReaderFiles.build(l, b);
        ZampanoDrawers.build(l, b);
    }

    // ------------------------------------------------------------------------------------------------ the ground
    private static void clear(ServerLevel l, BlockPos b) {
        box(l, b, -15, 0, -41, 15, 16, 0, Blocks.AIR.defaultBlockState());
        box(l, b, -15, -2, -41, 15, -2, 0, Blocks.STONE.defaultBlockState());
    }
    /** Old paving, a little moss in the joints, worn to cracks where people cross to his door. */
    private static void court(ServerLevel l, BlockPos b) {
        for (int x = -15; x <= 15; x++) for (int z = -41; z <= 0; z++) {
            int h = Math.floorMod(x * 7349 + z * 1931, 11);
            BlockState s = h < 2 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : h == 2 || Math.abs(x) <= 1 && z < -12 && h < 6
                    ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
            set(l, b, x, -1, z, s);
        }
        // A kerb of smooth stone along the wings, and the two benches of the chess players.
        for (int z = COURT_BACK; z <= COURT_FRONT; z++) for (int side : new int[]{-1, 1}) set(l, b, side * COURT_X, -1, z, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int side : new int[]{-1, 1}) {
            int x = side * 8;
            set(l, b, x, 0, -10, HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH));
            set(l, b, x, 0, -14, HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.SOUTH));
            set(l, b, x, 0, -12, HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE, Direction.SOUTH));
            set(l, b, x, 1, -12, VignetteDetailBlock.state(VignetteDetailBlock.Kind.CHESS, Direction.NORTH));
        }
        // The mat the cat keeps, before his door.
        set(l, b, NovelRooms.MAT.getX(), NovelRooms.MAT.getY(), NovelRooms.MAT.getZ(), Blocks.BROWN_CARPET.defaultBlockState());
        // Two lamps on posts by his door and two by the gate; the court is otherwise lit by the windows that are lit.
        for (int side : new int[]{-1, 1}) { post(l, b, side * 3, -17);post(l, b, side * 4, -2); }
        // Pots along the wing walls, clear of the side doors.
        for (int side : new int[]{-1, 1}) for (int z : new int[]{-3, -11, -17}) {
            set(l, b, side * COURT_X, 0, z, Blocks.DECORATED_POT.defaultBlockState());
        }
    }
    private static void post(ServerLevel l, BlockPos b, int x, int z) {
        set(l, b, x, 0, z, Blocks.STONE_BRICK_WALL.defaultBlockState());set(l, b, x, 1, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
        set(l, b, x, 2, z, Blocks.LANTERN.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ the street range
    /** The front range over the street: two storeys, the way in a plain arch under it. */
    private static void gatehouse(ServerLevel l, BlockPos b) {
        for (int x = -15; x <= 15; x++) for (int y = 0; y <= 9; y++) set(l, b, x, y, 0, facade(y));
        box(l, b, -1, 0, 0, 1, 2, 0, Blocks.AIR.defaultBlockState());
        for (int x = -2; x <= 2; x += 4) for (int y = 0; y <= 3; y++) set(l, b, x, y, 0, TRIM.defaultBlockState());
        for (int x = -1; x <= 1; x++) set(l, b, x, 3, 0, Blocks.CHISELED_SANDSTONE.defaultBlockState());
        for (int x : new int[]{-10, -5, 5, 10}) window(l, b, x, 6, 0, Direction.NORTH, x == -5);
        coping(l, b, -15, 15, 10, 0, 0);
    }

    // ------------------------------------------------------------------------------------------------ the wings
    /**
     * A wing of flats down each side of the court: a ground floor fronting the court at x ±12 (its side door is one of the
     * scene's dealt doors), a gallery above it on posts, the upper flats' doors and windows set back behind the rail.
     */
    private static void wing(ServerLevel l, BlockPos b, int side) {
        int front = side * 12, walk = side * 13, back = side * 14, outer = side * 15;
        // Solid behind the ground-floor front, the party wall behind that.
        for (int z = -18; z <= -1; z++) {
            for (int y = 0; y <= 3; y++) { set(l, b, front, y, z, facade(y));set(l, b, walk, y, z, STUCCO.defaultBlockState());set(l, b, back, y, z, STUCCO.defaultBlockState()); }
            for (int y = 0; y <= 10; y++) set(l, b, outer, y, z, STUCCO.defaultBlockState());
            // The gallery floor and its kerb.
            set(l, b, front, 4, z, TRIM.defaultBlockState());set(l, b, walk, 4, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            set(l, b, front, 5, z, rail(z));
            for (int y = 4; y <= 8; y++) set(l, b, back, y, z, y == 4 ? TRIM.defaultBlockState() : STUCCO.defaultBlockState());
            // The gallery's ceiling, and the tiles over it.
            for (int x : new int[]{front, walk, back}) { set(l, b, x, 9, z, Blocks.SPRUCE_PLANKS.defaultBlockState());set(l, b, x, 10, z, Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM)); }
        }
        // Posts carry the gallery roof; lanterns hang under it.
        for (int z = -17; z <= -2; z += 5) {
            for (int y = 5; y <= 8; y++) set(l, b, front, y, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            set(l, b, walk, 8, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        coping(l, b, outer, outer, 11, -18, -1);
        // Ground floor: windows, most dark, one lit; the side door's own small hall behind it.
        for (int z : new int[]{-4, -12, -16}) window(l, b, front, 1, z, side < 0 ? Direction.EAST : Direction.WEST, z == -12 && side > 0);
        box(l, b, walk, 0, -9, back, 1, -7, Blocks.AIR.defaultBlockState());
        box(l, b, walk, -1, -9, back, -1, -7, Blocks.SPRUCE_PLANKS.defaultBlockState());
        set(l, b, back, 2, -8, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Upper flats: a door and a window to each, behind the rail.
        for (int z : new int[]{-5, -14}) {
            Direction in = side < 0 ? Direction.EAST : Direction.WEST;
            set(l, b, back, 5, z, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in).setValue(DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
            set(l, b, back, 6, z, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in).setValue(DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
            set(l, b, outer, 5, z, STUCCO.defaultBlockState());set(l, b, outer, 6, z, STUCCO.defaultBlockState());
            window(l, b, back, 6, z - 2, in, z == -14 && side < 0);
        }
    }
    private static BlockState rail(int z) {
        return Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, z > -18).setValue(FenceBlock.SOUTH, z < -1);
    }

    // ------------------------------------------------------------------------------------------------ his front
    /** His flat's front on the court: the sealed iron door under a small hood, its lamp, and the windows he sealed. */
    private static void frontage(ServerLevel l, BlockPos b) {
        // The flat and the storey of flats above it.
        for (int x = -12; x <= 12; x++) for (int z = -40; z <= -19; z++) for (int y = 0; y <= 13; y++) {
            boolean shell = Math.abs(x) == 12 || z == -19 || z <= -39;
            if (y <= 7 && !shell) continue;
            set(l, b, x, y, z, y >= 8 ? STUCCO.defaultBlockState() : PLASTER.defaultBlockState());
        }
        // Beside it, a narrow light well each side between his walls and the neighbours', shut off from the court.
        for (int x = -15; x <= 15; x++) for (int z = -41; z <= -19; z++) {
            boolean wall = Math.abs(x) == 15 || z == -41 || z == -19 && Math.abs(x) > 12;
            if (!wall || Math.abs(x) <= 12 && z >= -40) continue;
            for (int y = 0; y <= 13; y++) set(l, b, x, y, z, STUCCO.defaultBlockState());
        }
        // The court face, in stucco, with a sill course and the upper windows.
        for (int x = -12; x <= 12; x++) for (int y = 0; y <= 13; y++) set(l, b, x, y, -19, facade(y));
        for (int x : new int[]{-9, -4, 4, 9}) window(l, b, x, 9, -19, Direction.SOUTH, x == -4);
        coping(l, b, -12, 12, 14, -19, -19);
        for (int x = -12; x <= 12; x++) for (int z = -40; z <= -20; z++) set(l, b, x, 14, z, Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        // His door: the iron door, a hood over it, a lamp under the hood.
        NovelRooms.door(l, b.offset(NovelRooms.ARCHIVE_DOOR), Direction.SOUTH, Blocks.IRON_DOOR, false);
        for (int y = 0; y <= 2; y++) for (int x : new int[]{-1, 1}) set(l, b, x, y, -19, TRIM.defaultBlockState());
        for (int x = -1; x <= 1; x++) { set(l, b, x, 3, -19, Blocks.CHISELED_SANDSTONE.defaultBlockState());set(l, b, x, 3, -18, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)); }
        set(l, b, 1, 2, -18, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // The two front windows he sealed from inside, and the window boxes nobody waters.
        for (int x0 : new int[]{-9, 7}) for (int x = x0; x <= x0 + 1; x++) {
            for (int y = 1; y <= 2; y++) set(l, b, x, y, -19, NovelRegistry.SEALED_WINDOW.get().defaultBlockState());
            set(l, b, x, 0, -18, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.FACING, Direction.SOUTH).setValue(TrapDoorBlock.OPEN, false));
        }
        set(l, b, -8, 1, -18, Blocks.POTTED_DEAD_BUSH.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ inside
    /** The flat itself: plaster walls, his floor, a beamed ceiling, and the side windows he sealed too. */
    private static void flat(ServerLevel l, BlockPos b) {
        box(l, b, -11, -1, -38, 11, -1, -20, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        box(l, b, -11, 7, -38, 11, 7, -20, Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int z : new int[]{-25, -31}) for (int x = -11; x <= 11; x++) set(l, b, x, 6, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        for (int x : new int[]{-12, 12}) for (int z : new int[]{-23, -29, -35}) for (int y = 1; y <= 3; y++) set(l, b, x, y, z, NovelRegistry.SEALED_WINDOW.get().defaultBlockState());
        // A skirting of dark wood along every wall.
        for (int x = -11; x <= 11; x++) for (int z : new int[]{-20, -38}) skirting(l, b, x, z, z == -20 ? Direction.NORTH : Direction.SOUTH);
        for (int z = -37; z <= -21; z++) for (int x : new int[]{-11, 11}) skirting(l, b, x, z, x < 0 ? Direction.EAST : Direction.WEST);
        // Lamps hung from the beams and the ceiling: enough to read by, never more.
        hang(l, b, -7, -25);hang(l, b, 4, -28);hang(l, b, 6, -35);hang(l, b, -6, -21);
        // A runner of old rugs from the door to the desks.
        for (int x = -4; x <= 3; x++) for (int z = -35; z <= -25; z++) set(l, b, x, 0, z, (x == -4 || x == 3 || z == -35 || z == -25 ? Blocks.GRAY_CARPET : Blocks.BROWN_CARPET).defaultBlockState());
        for (int z = -24; z <= -20; z++) set(l, b, 0, 0, z, Blocks.BROWN_CARPET.defaultBlockState());
    }
    private static void skirting(ServerLevel l, BlockPos b, int x, int z, Direction out) {
        if (!get(l, b, x, 0, z).isAir()) return;
        set(l, b, x, 0, z, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM).setValue(TrapDoorBlock.FACING, out).setValue(TrapDoorBlock.OPEN, true));
    }
    private static void hang(ServerLevel l, BlockPos b, int x, int z) {
        if (get(l, b, x, 6, z).isAir()) set(l, b, x, 6, z, Blocks.CHAIN.defaultBlockState());
        set(l, b, x, 5, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /** What he lived with: the kitchen corner, his chair, the shelves, the desks the readers used, the trunk. */
    private static void furnish(ServerLevel l, BlockPos b) {
        // The kitchen corner against the front wall, left of the door.
        tract(l, b, -10, 0, -20, TractHouseBlock.Kind.FRIDGE_LOWER);tract(l, b, -10, 1, -20, TractHouseBlock.Kind.FRIDGE_UPPER);
        tract(l, b, -9, 0, -20, TractHouseBlock.Kind.COUNTER);tract(l, b, -8, 0, -20, TractHouseBlock.Kind.COUNTER_SINK);tract(l, b, -7, 0, -20, TractHouseBlock.Kind.STOVE);
        tract(l, b, -9, 2, -20, TractHouseBlock.Kind.WALL_CABINET);tract(l, b, -8, 2, -20, TractHouseBlock.Kind.WALL_CABINET);
        detail(l, b, -9, 1, -20, SceneDetailBlock.Kind.CROCK, Direction.NORTH);detail(l, b, -8, 1, -20, SceneDetailBlock.Kind.DISH_RACK, Direction.NORTH);
        detail(l, b, 2, 0, -20, SceneDetailBlock.Kind.CROCK, Direction.NORTH);
        detail(l, b, -11, 2, -21, SceneDetailBlock.Kind.COAT, Direction.EAST);
        // His chair right of the door, turned to where the readers sat, and the radiator under the sealed window.
        furniture(l, b, 8, 0, -21, HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, Direction.WEST);
        furniture(l, b, 7, 0, -21, HouseholdFurnitureBlock.Kind.FOOTSTOOL, Direction.WEST);
        furniture(l, b, 9, 0, -20, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.NORTH);detail(l, b, 9, 1, -20, SceneDetailBlock.Kind.TABLE_LAMP, Direction.NORTH);
        furniture(l, b, 5, 0, -21, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
        furniture(l, b, 7, 0, -20, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.NORTH);
        // Shelves along the west wall, and what spills off them.
        for (int z : new int[]{-26, -32, -36}) for (int y = 0; y <= 2; y++) set(l, b, -11, y, z, Blocks.BOOKSHELF.defaultBlockState());
        cabinet(l, b, -10, 0, -24, SceneDetailBlock.Kind.FILE_TRAY);detail(l, b, -10, 0, -27, SceneDetailBlock.Kind.CRATE, Direction.NORTH);detail(l, b, -9, 0, -27, SceneDetailBlock.Kind.BOOKS, Direction.NORTH);
        // The readers' desks, two rows of two, each with its cane chair.
        for (int z : new int[]{-27, -33}) {
            table(l, b, -3, z, SceneDetailBlock.Kind.INK_PAPERS);table(l, b, -2, z, SceneDetailBlock.Kind.BOOKS);
            table(l, b, 2, z, SceneDetailBlock.Kind.FILE_TRAY);table(l, b, 3, z, SceneDetailBlock.Kind.BOOKS);
            furniture(l, b, -3, 0, z + 2, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);furniture(l, b, 3, 0, z + 2, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);
        }
        table(l, b, -8, -34, SceneDetailBlock.Kind.INK_PAPERS);table(l, b, -7, -34, SceneDetailBlock.Kind.BOOKS);table(l, b, -6, -34, SceneDetailBlock.Kind.TABLE_LAMP);
        furniture(l, b, -7, 0, -32, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);
        cabinet(l, b, 8, 0, -37, SceneDetailBlock.Kind.FILE_TRAY);cabinet(l, b, 7, 0, -37, SceneDetailBlock.Kind.BOOKS);
        detail(l, b, 11, 2, -37, SceneDetailBlock.Kind.CLOCK, Direction.WEST);
        // His reading desk, where the survey is.
        set(l, b, NovelRooms.ARCHIVE_DESK.getX(), NovelRooms.ARCHIVE_DESK.getY(), NovelRooms.ARCHIVE_DESK.getZ(), HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.READING_DESK, Direction.SOUTH));
        // The trunk, the marks in the floor beside it, and the readers' appointments inside.
        set(l, b, 5, -1, -33, NovelRegistry.GOUGES.get().defaultBlockState());
        set(l, b, 5, 0, -34, Blocks.CHEST.defaultBlockState());
        var trunk = b.offset(5, 0, -34);
        BuildBlocks.after(l, () -> {
            if (l.getBlockEntity(trunk) instanceof ChestBlockEntity chest && chest.isEmpty()) {
                chest.setItem(0, HouseWriting.book("Readers' appointments", "Zampano", HouseWriting.WritingStyle.ZAMPANO, List.of("Seven names. Seven visits.\n\nThe margins remember the voices more clearly than the survey remembers the walls.")));
                chest.setChanged();
            }
        });
    }

    /** The drafts on the desks, the sheets along the west wall and the dense wall of his hand behind the cabinets. */
    private static void papers(ServerLevel l, BlockPos b) {
        for (int i = 0; i < SceneReview.DRAFTS.size(); i++) {
            var at = SceneReview.DRAFTS.get(i);
            set(l, b, at.getX(), at.getY(), at.getZ(), VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_" + i), Direction.SOUTH));
        }
        for (int i = 8; i < 16; i++) set(l, b, -11, 4, -22 - (i - 8) * 2, VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_" + i), Direction.EAST));
        for (int x = -10; x <= 10; x++) for (int y = 1; y <= 5; y++)
            set(l, b, x, y, -38, VignetteDetailBlock.state(VignetteDetailBlock.Kind.valueOf("SCRIBBLE_" + (8 + Math.floorMod(x * 3 + y * 5, 8))), Direction.SOUTH));
    }

    // ------------------------------------------------------------------------------------------------ small pieces
    /** Stucco walls: a smooth plinth, sill courses at each floor, plain render between. */
    private static BlockState facade(int y) {
        return (y == 0 || y == 4 || y == 8 ? TRIM : STUCCO).defaultBlockState();
    }
    /** A window two high in a wall: glass, and behind it either a dark room or a lit one. */
    private static void window(ServerLevel l, BlockPos b, int x, int y, int z, Direction out, boolean lit) {
        int bx = x - out.getStepX(), bz = z - out.getStepZ();
        for (int dy = 0; dy <= 1; dy++) {
            set(l, b, x, y + dy, z, (lit ? Blocks.WHITE_STAINED_GLASS_PANE : Blocks.GLASS_PANE).defaultBlockState());
            set(l, b, bx, y + dy, bz, (lit ? Blocks.SHROOMLIGHT : Blocks.BLACK_CONCRETE).defaultBlockState());
        }
        set(l, b, x, y - 1, z, TRIM.defaultBlockState());
    }
    /** Tile coping along a roof edge. */
    private static void coping(ServerLevel l, BlockPos b, int x0, int x1, int y, int z0, int z1) {
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++)
            set(l, b, x, y, z, Blocks.BRICK_WALL.defaultBlockState().setValue(WallBlock.EAST_WALL, x0 == x1 ? WallSide.NONE : WallSide.LOW).setValue(WallBlock.WEST_WALL, x0 == x1 ? WallSide.NONE : WallSide.LOW)
                    .setValue(WallBlock.NORTH_WALL, z0 == z1 ? WallSide.NONE : WallSide.LOW).setValue(WallBlock.SOUTH_WALL, z0 == z1 ? WallSide.NONE : WallSide.LOW).setValue(WallBlock.UP, false));
    }
    private static void table(ServerLevel l, BlockPos b, int x, int z, SceneDetailBlock.Kind kind) {
        furniture(l, b, x, 0, z, HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.SOUTH);detail(l, b, x, 1, z, kind, Direction.NORTH);
    }
    private static void cabinet(ServerLevel l, BlockPos b, int x, int y, int z, SceneDetailBlock.Kind kind) {
        furniture(l, b, x, y, z, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.SOUTH);detail(l, b, x, y + 1, z, kind, Direction.NORTH);
    }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing) {
        set(l, b, x, y, z, HouseholdFurnitureBlock.state(kind, facing));
    }
    private static void detail(ServerLevel l, BlockPos b, int x, int y, int z, SceneDetailBlock.Kind kind, Direction facing) {
        set(l, b, x, y, z, SceneDetailBlock.state(kind, facing));
    }
    private static void tract(ServerLevel l, BlockPos b, int x, int y, int z, TractHouseBlock.Kind kind) {
        set(l, b, x, y, z, TractHouseBlock.of(kind, Direction.NORTH));
    }
    private static BlockState get(ServerLevel l, BlockPos b, int x, int y, int z) { return BuildBlocks.state(l, b.offset(x, y, z)); }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, Block s) { set(l, b, x, y, z, s.defaultBlockState()); }
    private static void box(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(l, b, x, y, z, s);
    }
}
