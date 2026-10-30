package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.UsherBlock.Kind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The house of Usher (0.4.181), built whole in the room's own bounds. From the reader's door a causeway crosses a black tarn
 * set with dead trunks to the house's front: grey stone furred with fungus, vacant windows, and the fissure that runs from
 * the roof down the wall in a zigzag until it is lost in the water. Through the Gothic archway is the hall, tapestried, its
 * floor black; a stair from its corner goes down past the studio to the vault behind a copper door, where Madeline's coffin
 * lies. The studio is lofty and dark, its pointed windows out of reach, with Roderick's chair, his easel and its painting,
 * his guitar and books on the floor. Dark galleries run down both sides to the passage at the back and the way on.
 * <p>Everything the room's story uses keeps its place: the coffin at {@link LiteraryRooms#COFFIN}, the wing where she is
 * found standing, the register and the account.
 */
public final class UsherHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** The front of the house and its archway. */
    public static final int FRONT = -13;
    public static final BlockPos ARCH = new BlockPos(0, 0, FRONT);
    /** The studio's double door off the hall, and the copper door of the vault at the foot of the stair. */
    public static final BlockPos STUDIO_DOOR = new BlockPos(-1, 0, -22), VAULT_DOOR = new BlockPos(7, -3, -23);
    /** Roderick's chair, easel and guitar in the studio. */
    public static final BlockPos CHAIR = new BlockPos(-4, 0, -31), EASEL = new BlockPos(6, 0, -27), GUITAR = new BlockPos(7, 0, -31);
    /** The studio's one hearth, in the east wall. */
    public static final BlockPos HEARTH = new BlockPos(10, 0, -27);
    /** The vault register on its desk in the hall, and the account on a stump on the far bank. */
    public static final BlockPos REGISTER = new BlockPos(-6, 0, -16), ACCOUNT = new BlockPos(6, 1, -2);
    /** The vault's west wall, where the crack follows the scratching on each return to a closed lid. */
    public static final int VAULT_WEST = -7;
    /** Where the fissure runs down the front, one column a row from the roof (y 12) to the foot of the wall (y -3). */
    public static final int[] FISSURE_X = {9, 9, 10, 10, 9, 9, 10, 10, 9, 9, 10, 10, 9, 9, 10, 10};
    public static final int FISSURE_TOP = 12;
    /** The front's windows (two rows) and the studio's high windows, each the bottom cell of three. */
    public static final List<BlockPos> FRONT_WINDOWS = List.of(new BlockPos(-13, 2, FRONT), new BlockPos(-7, 2, FRONT), new BlockPos(7, 2, FRONT), new BlockPos(13, 2, FRONT),
            new BlockPos(-13, 7, FRONT), new BlockPos(-4, 7, FRONT), new BlockPos(4, 7, FRONT), new BlockPos(13, 7, FRONT));
    public static final List<BlockPos> STUDIO_WINDOWS = List.of(new BlockPos(-11, 6, -25), new BlockPos(-11, 6, -29), new BlockPos(-11, 6, -33),
            new BlockPos(11, 6, -25), new BlockPos(11, 6, -29), new BlockPos(11, 6, -33));
    /** Every window cell in the house, front and studio. */
    public static List<BlockPos> windowCells() {
        var out = new ArrayList<BlockPos>();
        for (var w : FRONT_WINDOWS) for (int y = 0; y < 3; y++) out.add(w.above(y));
        for (var w : STUDIO_WINDOWS) for (int y = 0; y < 3; y++) out.add(w.above(y));
        return out;
    }

    private UsherHouse() {}

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(l, b, x, y, z, s);
    }
    private static BlockState u(Kind k) { return UsherBlock.of(k); }
    private static BlockState u(Kind k, Direction f) { return UsherBlock.of(k, f); }

    public static void build(ServerLevel l, BlockPos b) {
        var air = Blocks.AIR.defaultBlockState();
        // The night all round, and the ooze under the tarn.
        fill(l, b, -18, -4, -42, 18, 13, 0, u(Kind.NIGHT));
        fill(l, b, -17, -3, -41, 17, 12, -1, air);
        fill(l, b, -17, -4, -41, 17, -4, -1, u(Kind.MUD));
        approach(l, b);
        house(l, b);
        hall(l, b);
        vault(l, b);
        studio(l, b);
        galleries(l, b);
    }

    /** The bank at the reader's door, the causeway, the tarn and its dead trunks, and the faint light over the water. */
    private static void approach(ServerLevel l, BlockPos b) {
        fill(l, b, -17, -3, -3, 17, -2, -1, Blocks.COARSE_DIRT.defaultBlockState());
        fill(l, b, -17, -1, -3, 17, -1, -1, Blocks.PODZOL.defaultBlockState());
        for (int x : new int[]{-14, -11, -5, 4, 9, 15}) set(l, b, x, 0, -2 - Math.floorMod(x, 2), (x % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState());
        for (int x : new int[]{-8, 6, 12}) set(l, b, x, 0, -1, Blocks.DEAD_BUSH.defaultBlockState());
        set(l, b, ACCOUNT.getX(), 0, ACCOUNT.getZ(), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        fill(l, b, -17, -3, -12, 17, -1, -4, Blocks.WATER.defaultBlockState());
        fill(l, b, -2, -3, -12, 2, -2, -4, u(Kind.STONE));
        fill(l, b, -2, -1, -12, 2, -1, -4, u(Kind.CAUSEWAY));
        for (var trunk : new int[][]{{-15, -1, -2, 5}, {14, -1, -3, 6}, {-10, -3, -9, 6}, {11, -3, -6, 5}, {-6, -3, -11, 4}, {15, -3, -10, 7}}) {
            fill(l, b, trunk[0], trunk[1], trunk[2], trunk[0], trunk[1] + trunk[3], trunk[2], Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(l, b, trunk[0] + 1, trunk[1] + trunk[3] - 1, trunk[2], Blocks.DARK_OAK_FENCE.defaultBlockState());
            set(l, b, trunk[0] - 1, trunk[1] + trunk[3] - 2, trunk[2], Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        // The vapour hanging about the house gives the only light outside: no lamp, no moon.
        for (var at : new int[][]{{-9, 5, -8}, {9, 5, -8}, {0, 7, -4}, {0, 6, -11}})
            set(l, b, at[0], at[1], at[2], Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 6));
    }

    /** The house's mass, its front with the windows, archway and fissure, and its battlements against the night. */
    private static void house(ServerLevel l, BlockPos b) {
        fill(l, b, -17, -3, -41, 17, 12, FRONT, u(Kind.STONE));
        for (int x = -17; x <= 17; x += 2) set(l, b, x, 12, FRONT, Blocks.AIR.defaultBlockState());
        // The archway: three wide, pointed at its head.
        fill(l, b, -1, 0, FRONT, 1, 2, FRONT, Blocks.AIR.defaultBlockState());
        set(l, b, 0, 3, FRONT, Blocks.AIR.defaultBlockState());
        for (int side : new int[]{-1, 1}) set(l, b, side, 3, FRONT, Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, side < 0 ? Direction.EAST : Direction.WEST).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
        // Vacant eye-like windows: two storeys of them.
        for (var w : FRONT_WINDOWS) window(l, b, w.getX(), w.getY(), w.getZ(), Direction.SOUTH);
        for (var cell : fissure()) set(l, b, cell.x(), cell.y(), FRONT, cell.state());
    }
    private static void window(ServerLevel l, BlockPos b, int x, int y, int z, Direction facing) {
        set(l, b, x, y, z, UsherBlock.of(Kind.WINDOW, facing, 0));
        set(l, b, x, y + 1, z, UsherBlock.of(Kind.WINDOW, facing, 0));
        set(l, b, x, y + 2, z, UsherBlock.of(Kind.WINDOW, facing, 1));
    }

    /** A cell of the fissure on the front: where it is and how the crack crosses it. */
    public record Cell(int x, int y, BlockState state) {}
    /**
     * The fissure as a chain of cells, top to bottom, the crack entering and leaving each at the middle of an edge. Where it
     * steps a column aside, the cell beside it carries it across. Seen from the front, west is the left edge.
     */
    public static List<Cell> fissure() {
        var cells = new ArrayList<Cell>();
        char entry = 't';
        for (int i = 0; i < FISSURE_X.length; i++) {
            int x = FISSURE_X[i], y = FISSURE_TOP - i;
            int next = i + 1 < FISSURE_X.length ? FISSURE_X[i + 1] : x;
            if (next == x) { cells.add(new Cell(x, y, crack(entry, 'b'))); entry = 't'; }
            else {
                char out = next > x ? 'r' : 'l', in = next > x ? 'l' : 'r';
                cells.add(new Cell(x, y, crack(entry, out)));
                cells.add(new Cell(next, y, crack(in, 'b')));
                entry = 't';
            }
        }
        return cells;
    }
    private static BlockState crack(char in, char out) {
        String path = "" + in + out;
        return switch (path) {
            case "tb" -> UsherBlock.of(Kind.FISSURE, Direction.SOUTH, 0);
            case "tr" -> UsherBlock.of(Kind.FISSURE, Direction.SOUTH, 1);
            case "tl" -> UsherBlock.of(Kind.FISSURE, Direction.SOUTH, 2);
            case "lb" -> UsherBlock.of(Kind.FISSURE, Direction.SOUTH, 3);
            case "rb" -> UsherBlock.of(Kind.FISSURE_B, Direction.SOUTH, 0);
            default -> UsherBlock.of(Kind.FISSURE_B, Direction.SOUTH, 1);
        };
    }

    /** The hall: tapestried, its floor black, under a fretted ceiling, with armorial trophies and the stair down. */
    private static void hall(ServerLevel l, BlockPos b) {
        var air = Blocks.AIR.defaultBlockState();
        fill(l, b, -11, 0, -14, 11, 8, -22, u(Kind.TAPESTRY));
        fill(l, b, -10, 0, -14, 10, 8, -21, air);
        fill(l, b, -10, -1, -14, 10, -1, -21, u(Kind.FLOOR));
        fill(l, b, -10, 9, -14, 10, 9, -21, u(Kind.FRET));
        // The front wall is the house's own front: its windows look out on the tarn, and the fissure runs through it.
        // Arched ways into the galleries on either side.
        for (int side : new int[]{-11, 11}) fill(l, b, side, 0, -17, side, 2, -18, air);
        for (var at : new int[][]{{-10, 4, -16, 1}, {-10, 4, -19, 1}, {10, 4, -15, -1}, {10, 4, -20, -1}})
            set(l, b, at[0], at[1], at[2], u(Kind.ARMORIAL, at[3] > 0 ? Direction.EAST : Direction.WEST));
        for (var at : new int[][]{{-8, 0, -15}, {-8, 0, -20}, {3, 0, -15}}) set(l, b, at[0], at[1], at[2], u(Kind.CANDELABRA));
        set(l, b, -10, 3, -21, VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.USHER_PORTRAIT, Direction.EAST));
        // The stair in the east corner, railed in the hall, down past the studio to the vault's landing.
        fill(l, b, 6, -1, -17, 8, -1, -19, air);
        for (int z = -17; z >= -19; z--) for (int x = 5; x <= 9; x += 4) set(l, b, x, 0, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        fill(l, b, 6, 0, -20, 8, 0, -20, Blocks.DARK_OAK_FENCE.defaultBlockState());
        for (int i = 0; i < 3; i++) {
            int z = -17 - i, y = -1 - i;
            fill(l, b, 6, y - 1, z, 8, -4, z, u(Kind.VAULT));
            fill(l, b, 6, y, z, 8, y, z, Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
            fill(l, b, 6, y + 1, z, 8, Math.max(y + 2, -1), z, air);
        }
        fill(l, b, 5, -4, -20, 9, -2, -22, u(Kind.VAULT));
        fill(l, b, 6, -3, -20, 8, -2, -22, air);
        // The studio's doors, a pair, black and heavy, hung from either side.
        fill(l, b, -1, 0, -22, 0, 1, -22, air);
        for (int i = 0; i < 2; i++) {
            var hinge = i == 0 ? net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT : net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT;
            var door = Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH).setValue(net.minecraft.world.level.block.DoorBlock.HINGE, hinge);
            set(l, b, STUDIO_DOOR.getX() + i, 0, STUDIO_DOOR.getZ(), door);
            set(l, b, STUDIO_DOOR.getX() + i, 1, STUDIO_DOOR.getZ(), door.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        }
    }

    /** The vault: low, damp, under the studio, behind a door of iron sheathed in copper, where Madeline's coffin lies. */
    private static void vault(ServerLevel l, BlockPos b) {
        var air = Blocks.AIR.defaultBlockState();
        fill(l, b, VAULT_WEST, -4, -23, 9, -1, -35, u(Kind.VAULT));
        fill(l, b, -6, -3, -24, 8, -2, -34, air);
        set(l, b, VAULT_DOOR.getX(), -3, -23, air);
        set(l, b, VAULT_DOOR.getX(), -2, -23, air);
        NovelRooms.door(l, b.offset(VAULT_DOOR), Direction.NORTH, Blocks.WAXED_WEATHERED_COPPER_DOOR, false);
        LiteraryRooms.prop(l, b, LiteraryRooms.COFFIN, LiteraryPropBlock.Kind.COFFIN, Direction.NORTH);
        LiteraryRooms.prop(l, b, LiteraryRooms.COFFIN.north(), LiteraryPropBlock.Kind.COFFIN, Direction.NORTH);
        set(l, b, -5, -3, -33, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        set(l, b, 6, -3, -24, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
    }

    /** The studio: lofty and dark over the vault, its windows high in the walls, Roderick's things about the floor. */
    private static void studio(ServerLevel l, BlockPos b) {
        var air = Blocks.AIR.defaultBlockState();
        fill(l, b, -10, 0, -23, 10, 8, -34, air);
        fill(l, b, -10, -1, -23, 10, -1, -34, u(Kind.FLOOR));
        fill(l, b, -10, 9, -23, 10, 9, -34, u(Kind.FRET));
        // The windows stand high over the galleries' roofs, out of reach from the black floor, open on the night.
        for (var w : STUDIO_WINDOWS) window(l, b, w.getX(), w.getY(), w.getZ(), w.getX() < 0 ? Direction.WEST : Direction.EAST);
        for (int x = -9; x <= 9; x += 3) {
            set(l, b, x, 3, -34, u(Kind.DRAPERY, Direction.SOUTH));
            set(l, b, x, 2, -34, u(Kind.DRAPERY, Direction.SOUTH));
        }
        for (int z : new int[]{-24, -27, -31}) for (int x : new int[]{-10, 10}) if (x < 0 || z != HEARTH.getZ()) set(l, b, x, 2, z, u(Kind.DRAPERY, x < 0 ? Direction.EAST : Direction.WEST));
        // The one hearth, in the east wall between the windows, its chimney rising into the dark.
        set(l, b, HEARTH.getX(), 0, HEARTH.getZ(), Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.FACING, Direction.WEST));
        for (int dz : new int[]{-1, 1}) fill(l, b, HEARTH.getX(), 0, HEARTH.getZ() + dz, HEARTH.getX(), 1, HEARTH.getZ() + dz, u(Kind.STONE));
        for (int dz = -1; dz <= 1; dz++) set(l, b, HEARTH.getX(), 2, HEARTH.getZ() + dz, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        fill(l, b, HEARTH.getX(), 3, HEARTH.getZ(), HEARTH.getX(), 8, HEARTH.getZ(), u(Kind.STONE));
        // An old rug before his chair, worn to the warp.
        for (int x = -6; x <= -1; x++) for (int z = -30; z <= -28; z++) {
            String ns = z == -30 ? "NORTH" : z == -28 ? "SOUTH" : "", ew = x == -6 ? "WEST" : x == -1 ? "EAST" : "";
            var rim = io.github.knaitoe.theoldesthouse.house.RugFloorBlock.Rim.valueOf(ns.isEmpty() ? ew.isEmpty() ? "CENTER" : ew : ew.isEmpty() ? ns : ns + "_" + ew);
            set(l, b, x, -1, z, io.github.knaitoe.theoldesthouse.house.HouseBlocks.RUG_FLOOR.get().defaultBlockState()
                    .setValue(io.github.knaitoe.theoldesthouse.house.RugFloorBlock.TONE, io.github.knaitoe.theoldesthouse.house.RugFloorBlock.Tone.SLATE)
                    .setValue(io.github.knaitoe.theoldesthouse.house.RugFloorBlock.RIM, rim));
        }
        set(l, b, CHAIR.getX(), 0, CHAIR.getZ(), u(Kind.CHAIR, Direction.SOUTH));
        set(l, b, EASEL.getX(), 0, EASEL.getZ(), u(Kind.EASEL, Direction.WEST));
        set(l, b, GUITAR.getX(), 0, GUITAR.getZ(), u(Kind.GUITAR, Direction.WEST));
        for (var at : new int[][]{{-7, -25}, {3, -32}, {-6, -33}, {1, -26}, {-2, -33}}) set(l, b, at[0], 0, at[1], u(Kind.BOOKS));
        for (var at : new int[][]{{-8, -24}, {8, -33}}) set(l, b, at[0], 0, at[1], u(Kind.CANDELABRA));
        // His spinet, a writing desk with its ink, and the press in the gallery: the fittings the older room had, given places here.
        set(l, b, -8, 0, -28, VignetteHandlingBlock.of(VignetteHandlingBlock.Kind.SPINET, Direction.EAST));
        set(l, b, 5, 0, -33, VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.DICTATION_DESK, Direction.SOUTH));
        set(l, b, 5, 1, -33, VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.INK_SET, Direction.SOUTH));
        // The way through to the passage at the back.
        fill(l, b, 0, 0, -35, 0, 1, -35, air);
        NovelRooms.door(l, b.offset(0, 0, -35), Direction.SOUTH, Blocks.DARK_OAK_DOOR, false);
    }

    /** The galleries down either side and the passage across the back, dark, with trophies and a light apiece. */
    private static void galleries(ServerLevel l, BlockPos b) {
        var air = Blocks.AIR.defaultBlockState();
        for (int side : new int[]{-1, 1}) {
            int x0 = side * 12, x1 = side * 16;
            // A lower wing: its fretted ceiling, a stone roof over it and the night above, under the studio's windows.
            fill(l, b, x0, 0, -14, x1, 4, -40, air);
            fill(l, b, x0, -1, -14, x1, -1, -40, u(Kind.FLOOR));
            fill(l, b, x0, 5, -14, x1, 5, -40, u(Kind.FRET));
            fill(l, b, x0, 6, -14, side * 17, 6, -40, u(Kind.STONE));
            fill(l, b, x0, 7, -14, side * 17, 12, -40, air);
            for (int z = -19; z >= -37; z -= 9) set(l, b, x1, 3, z, u(Kind.ARMORIAL, side < 0 ? Direction.EAST : Direction.WEST));
            set(l, b, side * 14, 0, -27, u(Kind.CANDELABRA));
        }
        fill(l, b, -11, 0, -36, 11, 4, -40, air);
        fill(l, b, -11, -1, -36, 11, -1, -40, u(Kind.FLOOR));
        fill(l, b, -11, 5, -36, 11, 5, -40, u(Kind.FRET));
        set(l, b, -6, 0, -38, u(Kind.CANDELABRA));
        set(l, b, -16, 0, -30, VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.USHER_PRESS, Direction.EAST));
    }
}
