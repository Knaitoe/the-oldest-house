package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.RugFloorBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * 0.4.201 (owner: custom textures, a natural arrangement, and no photos on curtains or windows): Karen's room, built whole.
 * Until now the room was a plank box that four generic passes dressed in turn, which strung chests of drawers with lamps along
 * every wall, ran beams across it at head height and left its fittings where they fell. Here it is a room someone lives and
 * works in: papered walls and a cork floor (the room's own textures), two windows on the west wall with nothing pinned to
 * them, the reading chair on its rug facing the screen with the projector on its table behind, the slide desk under the
 * noticeboard, where the photographs belong, and the bed in the far corner with its head to the wall.
 *
 * <p>The projector, the screen the projections land on, the bed the room remembers and the book by it keep their roles; no
 * generic dressing pass touches the room.
 */
public final class KarenRoom {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private KarenRoom() {}

    public static void build(ServerLevel l, BlockPos b) {
        var paper = VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.KAREN_PAPER, Direction.NORTH);
        var cork = VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.KAREN_CORK, Direction.NORTH);
        var air = Blocks.AIR.defaultBlockState();
        // The shell: papered walls, a cork floor and a plain ceiling.
        box(l, b, -9, 0, -18, 9, 4, 0, paper);
        box(l, b, -8, 0, -17, 8, 4, -1, air);
        box(l, b, -9, -1, -18, 9, -1, 0, cork);
        box(l, b, -9, 5, -18, 9, 5, 0, Blocks.BIRCH_PLANKS.defaultBlockState());
        // Dark skirting round the walls, left out before the two doors (the way in, and the way on in the far wall).
        for (int x = -8; x <= 8; x++) for (int z : new int[]{-1, -17}) if (x != 0) skirting(l, b, x, z, z == -1 ? Direction.NORTH : Direction.SOUTH);
        for (int z = -16; z <= -2; z++) for (int x : new int[]{-8, 8}) skirting(l, b, x, z, x < 0 ? Direction.EAST : Direction.WEST);
        NovelRooms.door(l, b, Direction.SOUTH, Blocks.BIRCH_DOOR, true);
        // Two windows in the west wall, set back behind its plane, a sill under each and nothing on the glass.
        for (int z0 : new int[]{-6, -12}) for (int z = z0; z <= z0 + 1; z++) {
            for (int y = 1; y <= 2; y++) { set(l, b, -9, y, z, air);set(l, b, -10, y, z, Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true)); }
            set(l, b, -10, 0, z, paper);set(l, b, -10, 3, z, paper);set(l, b, -9, 0, z, Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        }
        // Between them, the bookshelves; a fern in the corner by the door.
        for (int z : new int[]{-8, -9}) for (int y = 0; y <= 1; y++) set(l, b, -8, y, z, Blocks.BOOKSHELF.defaultBlockState());
        set(l, b, -8, 0, -2, Blocks.POTTED_FERN.defaultBlockState());
        // The reading corner: the chair and its footstool on a rug, facing the screen; the projector on its table behind.
        rug(l, b, -5, 1, -11, -5);
        furniture(l, b, -3, 0, -9, HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, Direction.NORTH);
        furniture(l, b, -3, 0, -10, HouseholdFurnitureBlock.Kind.FOOTSTOOL, Direction.NORTH);
        furniture(l, b, NovelRooms.PROJECTOR.getX(), 0, NovelRooms.PROJECTOR.getZ(), HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.NORTH);
        set(l, b, NovelRooms.PROJECTOR.getX(), NovelRooms.PROJECTOR.getY(), NovelRooms.PROJECTOR.getZ(), NovelRooms.prop(NovelPropBlock.Kind.PROJECTOR, Direction.NORTH));
        // The screen: a white sheet hung from a dark roller, its lower edge clear of the floor.
        box(l, b, -3, 1, -15, 3, 3, -15, Blocks.WHITE_CONCRETE.defaultBlockState());
        for (int x = -3; x <= 3; x++) set(l, b, x, 4, -15, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        // The slide desk on the east wall under the noticeboard, where the photographs go, with its chair.
        fit(l, b, 8, 0, -11, VignetteInteriorBlock.Kind.SLIDE_CABINET, Direction.WEST);fit(l, b, 8, 0, -12, VignetteInteriorBlock.Kind.SLIDE_CABINET, Direction.WEST);
        fit(l, b, 8, 1, -11, VignetteInteriorBlock.Kind.KAREN_SLIDE_SORTER, Direction.WEST);
        set(l, b, 8, 1, -12, SceneDetailBlock.state(SceneDetailBlock.Kind.FILE_TRAY, Direction.WEST));
        fit(l, b, 8, 2, -11, VignetteInteriorBlock.Kind.KAREN_NOTICEBOARD, Direction.WEST);
        furniture(l, b, 7, 0, -11, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
        // A chest of drawers by the door.
        furniture(l, b, 8, 0, -3, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.WEST);
        set(l, b, 8, 1, -3, SceneDetailBlock.state(SceneDetailBlock.Kind.VASE, Direction.WEST));
        // The bed in the far corner, head to the wall; the book on one side of it, her lamp on the other.
        NovelRooms.bed(l, b.offset(NovelRooms.BED), Blocks.RED_BED, Direction.NORTH);
        var book = NovelRooms.KAREN_BOOK;
        furniture(l, b, book.getX(), 0, book.getZ(), HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.SOUTH);
        set(l, b, book.getX(), book.getY(), book.getZ(), SceneDetailBlock.state(SceneDetailBlock.Kind.BOOKS, Direction.SOUTH));
        furniture(l, b, 8, 0, NovelRooms.BED.getZ() - 1, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.SOUTH);
        fit(l, b, 8, 1, NovelRooms.BED.getZ() - 1, VignetteInteriorBlock.Kind.KAREN_LAMP, Direction.SOUTH);
        // One light from the ceiling by the door; the rest is the lamps and the projector.
        set(l, b, 0, 4, -3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private static void rug(ServerLevel l, BlockPos b, int x0, int x1, int z0, int z1) {
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
            String ns = z == z0 ? "NORTH" : z == z1 ? "SOUTH" : "", ew = x == x0 ? "WEST" : x == x1 ? "EAST" : "";
            var rim = RugFloorBlock.Rim.valueOf(ns.isEmpty() ? ew.isEmpty() ? "CENTER" : ew : ew.isEmpty() ? ns : ns + "_" + ew);
            set(l, b, x, -1, z, HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE, RugFloorBlock.Tone.SLATE).setValue(RugFloorBlock.RIM, rim));
        }
    }
    private static void skirting(ServerLevel l, BlockPos b, int x, int z, Direction out) {
        set(l, b, x, 0, z, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM).setValue(TrapDoorBlock.FACING, out).setValue(TrapDoorBlock.OPEN, true));
    }
    private static void fit(ServerLevel l, BlockPos b, int x, int y, int z, VignetteInteriorBlock.Kind k, Direction f) {
        var handling = VignetteHandlingBlock.replacement(k);
        set(l, b, x, y, z, handling == null ? VignetteInteriorBlock.of(k, f) : VignetteHandlingBlock.of(handling, f));
    }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing) {
        set(l, b, x, y, z, HouseholdFurnitureBlock.state(kind, facing));
    }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void box(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) set(l, b, x, y, z, s);
    }
}
