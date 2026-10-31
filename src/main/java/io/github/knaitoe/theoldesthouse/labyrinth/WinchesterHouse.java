package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.WinchesterFittingBlock.Kind;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Winchester house, rebuilt whole (0.4.201; the owner agreed every change in docs/OWNER_NOTES_0_4_201.md). A warren of
 * small rooms on two floors and mismatched levels: the entrance hall with the clock and the construction ledger, a raised
 * parlour whose chimney stops short of the ceiling, a dining room, a spine of doors that open onto walls, a narrow passage to
 * the Blue Room, and the upper ledger at the back of the house, over the carpenters' open court.
 *
 * <p>The night runs on the séance. In a robe, the bell on the Blue Room's table starts the hall clock striking thirteen, a
 * stroke every seven seconds, and gives the reader tonight's plan. The way up is through the house's own mistakes, and the
 * carpenters build each piece of it only where no one is looking, hammering where they will work next: the wall behind the
 * door with the brass 13 comes down; a stair of seven short flights doubles back on itself for half a block a turn and ends
 * in the ceiling, until a hatch is cut there; upstairs the room with its floor taken up is crossed on the joists and the
 * window in the floor; the door that opens on nothing opens over the court, and boards are laid out ahead to the last room.
 * What nobody needs any more they close again behind them. Whoever is not in the last room at the thirteenth stroke has a
 * room built round them and wakes by the front door. In the last room the upper ledger names a room for every kind of
 * creature the reader has killed in their own world, and the last line is the reader's name.
 */
public final class WinchesterHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    static final String STATE = "winchester_0201";
    static final LabyrinthPlace PLACE = LabyrinthPlace.WINCHESTER;
    /** The hall clock, two blocks tall, on the hall's east side; the construction ledger on its table by the west wall. */
    public static final BlockPos CLOCK = new BlockPos(5, 0, -7), SOURCE = new BlockPos(-4, 1, -5);
    /** The upper ledger on its desk in the last room, and where its reader stands to have reached it. */
    public static final BlockPos ENDING = new BlockPos(22, 8, -70);
    /** The door with the brass 13, in the spine's north wall; the Blue Room's door at the end of the passage. */
    public static final BlockPos DOOR13 = new BlockPos(-28, 0, -17), SEANCE_DOOR = new BlockPos(0, 0, -44);
    /** The séance table with its bell, and its one chair. */
    public static final BlockPos TABLE = new BlockPos(0, 0, -52), CHAIR = new BlockPos(0, 0, -54);
    /** The middle of the window in the floor of the room with its floor taken up. */
    public static final BlockPos FLOOR_WINDOW = new BlockPos(0, 6, -26);
    /** The door that opens on nothing, high in the court's west wall; the last room's door across the court. */
    public static final BlockPos NOTHING_DOOR = new BlockPos(14, 7, -26), LEDGER_DOOR = new BlockPos(22, 7, -59);
    /** The stair's last steps go into the ceiling: the two cells over them, and the step in the floor above. */
    public static final BlockPos HATCH_A = new BlockPos(-16, 6, -33), HATCH_B = new BlockPos(-16, 6, -34), TOP_STEP = new BlockPos(-16, 6, -35);
    /** Where a reader the carpenters closed in wakes. */
    public static final BlockPos WAKE = new BlockPos(0, 0, -3);
    public static final int STRIKES = 13, STRIKE_EVERY = 140, LANES = 7;
    /** The pieces of the way up, in order. */
    public enum Stage { DOOR13, HATCH, BOARDS1, BOARDS2, BOARDS3 }
    private static final String[] ROOMS = {"The parlour", "The dining room", "The sewing room", "The storeroom", "The Blue Room",
            "The room above the stair", "The room with its floor up", "The carpenters' shop", "The hall", "The stair that ends",
            "The court", "The room with no door", "The thirteenth room"};

    private WinchesterHouse() {}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(l, b, x, y, z, s);
    }
    private static void fit(ServerLevel l, BlockPos b, BlockPos at, Kind k, Direction f) { BuildBlocks.set(l, b.offset(at), WinchesterFittingBlock.of(k, f), F); }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind k, Direction f) { set(l, b, x, y, z, HouseholdFurnitureBlock.state(k, f)); }
    private static void detail(ServerLevel l, BlockPos b, int x, int y, int z, SceneDetailBlock.Kind k, Direction f) { set(l, b, x, y, z, SceneDetailBlock.state(k, f)); }
    private static void light(ServerLevel l, BlockPos b, int x, int y, int z, int level) { set(l, b, x, y, z, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, level)); }
    private static BlockState paper() { return LiteraryRegistry.WINCHESTER_WALLPAPER.get().defaultBlockState(); }
    private static BlockState parquet() { return LiteraryRegistry.WINCHESTER_PARQUET.get().defaultBlockState(); }
    private static BlockState boards() { return Blocks.DARK_OAK_PLANKS.defaultBlockState(); }
    /** A room: air from y0 to y1 over a parquet floor at y0-1. */
    private static void room(ServerLevel l, BlockPos b, int x0, int x1, int z0, int z1, int y0, int y1) {
        fill(l, b, x0, y0, z0, x1, y1, z1, Blocks.AIR.defaultBlockState());fill(l, b, x0, y0 - 1, z0, x1, y0 - 1, z1, parquet());
    }
    private static void door(ServerLevel l, BlockPos b, int x, int y, int z, Direction facing) { NovelRooms.door(l, b.offset(x, y, z), facing, Blocks.DARK_OAK_DOOR, false); }

    /** The lane of the switchback stair: its x, the block its slabs sit in, and whether they sit in the top half. */
    public static int laneX(int k) { return -28 + 2 * k; }
    public static int laneY(int k) { return k / 2; }
    public static boolean laneTop(int k) { return k % 2 == 1; }
    /** The height a reader stands at on a lane. */
    public static double laneFloor(int k) { return laneY(k) + (laneTop(k) ? 1 : .5); }
    private static BlockState slab(int k) { return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, laneTop(k) ? SlabType.TOP : SlabType.BOTTOM); }

    public static void build(ServerLevel l, BlockPos b) {
        BlockState paper = paper(), blue = LiteraryRegistry.WINCHESTER_SEANCE_PAPER.get().defaultBlockState(), glass = LiteraryRegistry.WINCHESTER_LEADED_GLASS.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState(), planks = boards();
        // Everything inside the walls begins as wall; the rooms are cut out of it.
        fill(l, b, -29, 0, -76, 29, 14, -1, paper);

        // ---- the ground floor
        room(l, b, -6, 6, -12, -1, 0, 5);                                   // the entrance hall
        room(l, b, -29, -8, -12, -1, 1, 5);                                 // the parlour, a step up
        set(l, b, -8, 0, -6, Blocks.OAK_SLAB.defaultBlockState());          // the half step into it
        room(l, b, 8, 29, -12, -1, 0, 5);                                   // the dining room
        room(l, b, -29, 29, -16, -14, 0, 4);                                // the spine
        room(l, b, -1, 1, -43, -18, 0, 5);                                  // the passage, open to the room above
        room(l, b, -13, -3, -35, -19, 1, 5);                                // the sewing room, a step up, open above
        room(l, b, 3, 13, -35, -19, 0, 5);                                  // the storeroom, open above
        room(l, b, -8, 8, -58, -45, 0, 5);                                  // the Blue Room
        room(l, b, 15, 29, -58, -18, 0, 11);                                // the carpenters' court, open to the roof
        fill(l, b, 15, -1, -58, 29, -1, -18, Blocks.HAY_BLOCK.defaultBlockState());
        door(l, b, 0, 0, -13, Direction.SOUTH);door(l, b, -7, 0, -6, Direction.EAST);door(l, b, 7, 0, -6, Direction.WEST);
        door(l, b, DOOR13.getX(), 0, DOOR13.getZ(), Direction.SOUTH);fit(l, b, new BlockPos(-27, 1, -16), Kind.PLAQUE, Direction.SOUTH);
        door(l, b, 0, 0, -17, Direction.SOUTH);door(l, b, 22, 0, -17, Direction.SOUTH);
        // Doors onto wall: one off the spine, one in the dining room's far wall.
        door(l, b, 14, 0, -17, Direction.SOUTH);door(l, b, 30, 0, -6, Direction.WEST);fill(l, b, 31, 0, -6, 31, 1, -6, paper);
        door(l, b, -2, 0, -27, Direction.EAST);set(l, b, -3, 0, -27, Blocks.OAK_SLAB.defaultBlockState());door(l, b, 2, 0, -27, Direction.WEST);
        door(l, b, SEANCE_DOOR.getX(), 0, SEANCE_DOOR.getZ(), Direction.SOUTH);

        // ---- the stair of seven short flights: one-wide lanes between walls, each half a block above the last at its turn
        for (int k = 0; k < LANES; k++) {
            int x = laneX(k);
            for (int z = -31; z <= -19; z++) { set(l, b, x, laneY(k), z, slab(k));fill(l, b, x, laneY(k) + 1, z, x, 5, z, air); }
        }
        for (int k = 0; k < LANES - 1; k++) {
            // Even lanes turn at the north end, odd lanes at the south end.
            int z = k % 2 == 0 ? -32 : -18, x = laneX(k);
            set(l, b, x, laneY(k), z, slab(k));fill(l, b, x, laneY(k) + 1, z, x, 5, z, air);
            for (int dx = 1; dx <= 2; dx++) { set(l, b, x + dx, laneY(k + 1), z, slab(k + 1));fill(l, b, x + dx, laneY(k + 1) + 1, z, x + dx, 5, z, air); }
        }
        // The last lane climbs on into the ceiling.
        BlockState up = Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        set(l, b, -16, 3, -32, up);fill(l, b, -16, 4, -32, -16, 5, -32, air);
        set(l, b, -16, 4, -33, up);set(l, b, -16, 5, -33, air);
        set(l, b, -16, 5, -34, up);
        light(l, b, -22, 4, -25, 7);light(l, b, -18, 4, -20, 7);

        // ---- upstairs: the room above the stair, the room with its floor up, and the last room
        room(l, b, -29, -15, -36, -18, 7, 11);
        door(l, b, -14, 7, -26, Direction.EAST);
        fill(l, b, -13, 6, -36, 13, 11, -18, air);
        for (int z = -36; z <= -18; z++) { set(l, b, -13, 6, z, planks);set(l, b, 13, 6, z, planks); }
        for (int x = -13; x <= 13; x++) { set(l, b, x, 6, -36, planks);set(l, b, x, 6, -18, planks); }
        for (int z : new int[]{-20, -23, -26, -29, -32, -35}) for (int x = -12; x <= 12; x++)
            set(l, b, x, 6, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        // The window in the floor, where the middle of the joist is missing.
        fill(l, b, -3, 6, -27, 3, 6, -25, glass);
        door(l, b, NOTHING_DOOR.getX(), NOTHING_DOOR.getY(), NOTHING_DOOR.getZ(), Direction.WEST);
        room(l, b, 15, 29, -76, -60, 7, 11);
        door(l, b, LEDGER_DOOR.getX(), LEDGER_DOOR.getY(), LEDGER_DOOR.getZ(), Direction.NORTH);
        // A glazed roof over the court, so the boards are seen against the light.
        fill(l, b, 16, 12, -57, 28, 12, -19, glass);

        // ---- the period rooms
        // The hall: the clock, the ledger's table against the west wall, a runner, a chandelier.
        fit(l, b, CLOCK, Kind.CLOCK, Direction.WEST);fit(l, b, CLOCK.above(), Kind.CLOCK_TOP, Direction.WEST);
        // The runner stops short of the doormat, where every reader comes in (and the walled wake).
        fill(l, b, -1, 0, -11, 1, 0, -5, Blocks.RED_CARPET.defaultBlockState());
        set(l, b, 0, 5, -6, Blocks.CHAIN.defaultBlockState());set(l, b, 0, 4, -6, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // The parlour: its chimney rises from the hearth and stops short of the ceiling; a spider-web window; a window onto wallpaper.
        fill(l, b, -19, 1, -12, -17, 4, -12, Blocks.BRICKS.defaultBlockState());set(l, b, -18, 1, -12, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        fill(l, b, -19, 4, -11, -17, 4, -11, Blocks.BRICK_SLAB.defaultBlockState());
        fit(l, b, new BlockPos(-29, 2, -6), Kind.SPIDER_WINDOW, Direction.EAST);fit(l, b, new BlockPos(-29, 3, -6), Kind.SPIDER_WINDOW, Direction.EAST);
        fill(l, b, -30, 2, -3, -30, 3, -2, glass);fill(l, b, -31, 2, -3, -31, 3, -2, paper);
        fill(l, b, -23, 1, -11, -15, 1, -9, Blocks.BROWN_CARPET.defaultBlockState());
        furniture(l, b, -22, 1, -8, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.NORTH);furniture(l, b, -21, 1, -8, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.NORTH);
        furniture(l, b, -24, 1, -10, HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR, Direction.EAST);furniture(l, b, -14, 1, -10, HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, Direction.WEST);
        light(l, b, -18, 5, -6, 10);
        // The dining room: a long table and its chairs, a sideboard.
        for (int x = 14; x <= 22; x++) { furniture(l, b, x, 0, -6, HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.SOUTH);if (x % 2 == 0) { furniture(l, b, x, 0, -5, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);furniture(l, b, x, 0, -7, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.SOUTH); } }
        furniture(l, b, 18, 0, -11, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.SOUTH);light(l, b, 18, 5, -6, 10);
        // The spine, the passage, the sewing room and the storeroom.
        for (int x = -24; x <= 24; x += 12) light(l, b, x, 4, -15, 8);
        light(l, b, 0, 4, -30, 6);
        furniture(l, b, -8, 1, -30, HouseholdFurnitureBlock.Kind.READING_DESK, Direction.SOUTH);furniture(l, b, -8, 1, -29, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);
        detail(l, b, -12, 1, -34, SceneDetailBlock.Kind.CRATE, Direction.SOUTH);detail(l, b, -11, 1, -34, SceneDetailBlock.Kind.BLANKET, Direction.SOUTH);light(l, b, -8, 4, -25, 7);
        for (int[] c : new int[][]{{5, -33}, {6, -33}, {5, -32}, {11, -21}, {12, -21}, {12, -22}}) detail(l, b, c[0], 0, c[1], SceneDetailBlock.Kind.CRATE, Direction.SOUTH);
        fit(l, b, new BlockPos(9, 0, -24), Kind.LUMBER, Direction.NORTH);light(l, b, 8, 4, -27, 7);
        // The Blue Room: blue paper all round, the table with its bell, one chair, thirteen robes on their hooks, a dim light.
        for (int y = 0; y <= 5; y++) {
            for (int z = -59; z <= -44; z++) { set(l, b, -9, y, z, blue);set(l, b, 9, y, z, blue); }
            for (int x = -9; x <= 9; x++) { set(l, b, x, y, -59, blue);if (x != 0 || y > 1) set(l, b, x, y, -44, blue); }
        }
        fit(l, b, TABLE, Kind.SEANCE_TABLE, Direction.SOUTH);fit(l, b, CHAIR, Kind.SEANCE_CHAIR, Direction.SOUTH);
        for (int z : new int[]{-47, -49, -51, -53, -55}) { detail(l, b, -8, 2, z, SceneDetailBlock.Kind.COAT, Direction.EAST);detail(l, b, 8, 2, z, SceneDetailBlock.Kind.COAT, Direction.WEST); }
        for (int x : new int[]{-2, 0, 2}) detail(l, b, x, 2, -58, SceneDetailBlock.Kind.COAT, Direction.SOUTH);
        light(l, b, 0, 4, -52, 6);fit(l, b, new BlockPos(1, 1, -45), Kind.PLAQUE, Direction.NORTH);
        // The court: the carpenters' sawhorse, lumber and nails on the hay.
        fit(l, b, new BlockPos(20, 0, -40), Kind.SAWHORSE, Direction.NORTH);fit(l, b, new BlockPos(24, 0, -44), Kind.LUMBER, Direction.NORTH);fit(l, b, new BlockPos(18, 0, -46), Kind.NAIL_KEG, Direction.SOUTH);
        light(l, b, 22, 10, -38, 11);
        // Upstairs: the room above the stair kept as a lumber room; the floored edges of the next; the last room's desk and shelves.
        for (int[] c : new int[][]{{-28, -19}, {-27, -19}, {-28, -20}, {-20, -36}, {-19, -36}}) detail(l, b, c[0], 7, c[1], SceneDetailBlock.Kind.CRATE, Direction.SOUTH);
        light(l, b, -22, 10, -27, 8);light(l, b, 0, 10, -27, 9);
        furniture(l, b, ENDING.getX(), ENDING.getY() - 1, ENDING.getZ(), HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.SOUTH);
        furniture(l, b, ENDING.getX(), ENDING.getY() - 1, ENDING.getZ() + 1, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);
        for (int x = 16; x <= 28; x++) if (Math.abs(x - 22) > 1) fill(l, b, x, 7, -76, x, 9, -76, Blocks.BOOKSHELF.defaultBlockState());
        light(l, b, 22, 10, -68, 10);

        for (var stage : Stage.values()) unbuild(l, b, stage);
    }

    // ------------------------------------------------------------------------------------------------ the way up
    /** The cells each piece of the way up takes. */
    static List<BlockPos> cells(Stage s) {
        var out = new ArrayList<BlockPos>();
        switch (s) {
            case DOOR13 -> { for (int y = 0; y <= 2; y++) out.add(new BlockPos(-28, y, -18)); }
            case HATCH -> { out.add(HATCH_A);out.add(HATCH_B);out.add(TOP_STEP); }
            case BOARDS1 -> { for (int x = 15; x <= 23; x++) for (int z = -27; z <= -26; z++) out.add(new BlockPos(x, 6, z)); }
            case BOARDS2 -> { for (int z = -42; z <= -28; z++) for (int x = 22; x <= 23; x++) out.add(new BlockPos(x, 6, z)); }
            case BOARDS3 -> { for (int z = -58; z <= -43; z++) for (int x = 22; x <= 23; x++) out.add(new BlockPos(x, 6, z)); }
        }
        return out;
    }
    public static void build(ServerLevel l, BlockPos b, Stage s) {
        switch (s) {
            case DOOR13 -> { BuildBlocks.set(l, b.offset(-28, 0, -18), slab(0), F);for (int y = 1; y <= 2; y++) BuildBlocks.set(l, b.offset(-28, y, -18), Blocks.AIR.defaultBlockState(), F); }
            case HATCH -> { BuildBlocks.set(l, b.offset(HATCH_A), Blocks.AIR.defaultBlockState(), F);BuildBlocks.set(l, b.offset(HATCH_B), Blocks.AIR.defaultBlockState(), F);
                BuildBlocks.set(l, b.offset(TOP_STEP), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM), F); }
            default -> { for (var c : cells(s)) BuildBlocks.set(l, b.offset(c), Blocks.SPRUCE_PLANKS.defaultBlockState(), F); }
        }
    }
    public static void unbuild(ServerLevel l, BlockPos b, Stage s) {
        switch (s) {
            case DOOR13 -> { for (var c : cells(s)) BuildBlocks.set(l, b.offset(c), paper(), F); }
            case HATCH -> { for (var c : cells(s)) BuildBlocks.set(l, b.offset(c), boards(), F); }
            default -> { for (var c : cells(s)) BuildBlocks.set(l, b.offset(c), Blocks.AIR.defaultBlockState(), F); }
        }
    }
    /** Whether a piece of the way up stands as it does when built. */
    public static boolean built(ServerLevel l, BlockPos b, Stage s) {
        return switch (s) {
            case DOOR13 -> l.getBlockState(b.offset(-28, 1, -18)).isAir();
            case HATCH -> l.getBlockState(b.offset(HATCH_B)).isAir();
            default -> !l.getBlockState(b.offset(cells(s).getFirst())).isAir();
        };
    }
    private static Vec3 centre(BlockPos b, BlockPos rel) { return Vec3.atCenterOf(b.offset(rel)); }
    /** Nobody, reader or camera, looks at any part of it. */
    static boolean unwatched(ServerLevel l, BlockPos b, Stage s) {
        var cells = cells(s);
        for (int i = 0; i < cells.size(); i += Math.max(1, cells.size() / 4)) if (LiteraryVignettes.watched(l, centre(b, cells.get(i)))) return false;
        return !LiteraryVignettes.watched(l, centre(b, cells.getLast()));
    }
    /** Nothing living stands in it or on it. */
    static boolean clear(ServerLevel l, BlockPos b, Stage s) {
        for (var c : cells(s)) if (!l.getEntitiesOfClass(LivingEntity.class, new AABB(b.offset(c)).expandTowards(0, 1.9, 0).inflate(.3), LivingEntity::isAlive).isEmpty()) return false;
        return true;
    }

    // ------------------------------------------------------------------------------------------------ where a reader is
    enum Zone { GROUND, STAIRS, UPSTAIRS, BOARDS, LEDGER, NONE }
    static Zone zone(ServerPlayer p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        if (y >= 6.5 && x >= 15 && x < 30 && z < -59 && z >= -77) return Zone.LEDGER;
        if (y >= 6.5 && x >= 15 && z < -25 && z >= -59) return Zone.BOARDS;
        if (y >= 6.5 && x < 14 && z < -17 && z >= -37) return Zone.UPSTAIRS;
        if (y < 6.5 && x >= -29 && x < -14 && z < -17 && z >= -36) return Zone.STAIRS;
        if (y < 6.5) return Zone.GROUND;
        return Zone.NONE;
    }
    static boolean near(ServerPlayer p, BlockPos b, BlockPos rel, double r) { return p.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(rel))) <= r * r; }
    /** Whether a reader in the night needs this piece of the way up now. */
    static boolean wants(ServerPlayer p, BlockPos b, Stage s) {
        var zone = zone(p, b);double x = p.getX() - b.getX(), z = p.getZ() - b.getZ();
        return switch (s) {
            case DOOR13 -> zone == Zone.GROUND;
            case HATCH -> zone == Zone.STAIRS;
            case BOARDS1 -> zone == Zone.UPSTAIRS && near(p, b, NOTHING_DOOR, 5) && doorOpen(p.serverLevel(), b.offset(NOTHING_DOOR)) || zone == Zone.BOARDS && z >= -28;
            case BOARDS2 -> zone == Zone.BOARDS && (x >= 19 && z >= -28 || z < -28 && z >= -43);
            case BOARDS3 -> zone == Zone.BOARDS && z <= -36;
        };
    }
    static boolean doorOpen(ServerLevel l, BlockPos at) { var s = l.getBlockState(at);return s.getBlock() instanceof DoorBlock && s.getValue(DoorBlock.OPEN); }

    /** The readers in this house now (spectators watch; they are never started or built for). */
    static List<ServerPlayer> readers(ServerLevel l, BlockPos b) {
        var out = new ArrayList<ServerPlayer>();
        for (var o : l.players()) { var base = LiteraryVignettes.base(o, PLACE);if (base != null && base.equals(b) && LiteraryVignettes.participant(o) && LiteraryVignettes.inside(o, PLACE)) out.add(o); }
        return out;
    }
    static boolean running(LabyrinthData d, ServerPlayer p) { return LiteraryVignettes.personal(d, p.getUUID(), PLACE).getBoolean("InRun"); }

    /**
     * The carpenters, once a tick for the whole house: each piece somebody in the night needs goes up where nobody is looking;
     * each piece nobody needs any more comes down again, unseen and with nobody on it.
     */
    public static void work(ServerLevel l, BlockPos b) {
        var d = LabyrinthData.get(l.getServer());String key = Long.toString(b.asLong());var s = d.stateEntry(STATE, key);
        var runners = readers(l, b).stream().filter(p -> running(d, p)).toList();
        for (var stage : Stage.values()) {
            boolean needed = runners.stream().anyMatch(p -> wants(p, b, stage)), up = built(l, b, stage);
            if (needed == up || !unwatched(l, b, stage) || !clear(l, b, stage)) continue;
            if (needed) build(l, b, stage); else unbuild(l, b, stage);
            s.putInt(needed ? "Built" : "Taken", s.getInt(needed ? "Built" : "Taken") + 1);
        }
        d.setStateEntry(STATE, key, s);
    }

    // ------------------------------------------------------------------------------------------------ a reader
    public static void arrive(ServerPlayer p, BlockPos b, CompoundTag own) {
        own.putInt("Carpenter", 60 + p.getRandom().nextInt(80));
        // A night left unfinished is not waiting when they come back.
        if (own.getBoolean("InRun")) { own.putBoolean("InRun", false);own.putInt("Strikes", 0); }
    }

    /** Every five ticks for a reader inside: the strokes, the carpenters, the last room. */
    public static void reader(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();long now = l.getGameTime();
        // Once a tick for the house, however many readers are in it.
        var d = LabyrinthData.get(p.server);String key = Long.toString(b.asLong());var s = d.stateEntry(STATE, key);
        if (s.getLong("WorkAt") != now) { s.putLong("WorkAt", now);d.setStateEntry(STATE, key, s);work(l, b); }
        if (!own.getBoolean("InRun")) {
            // Before the night, the carpenters are only heard, behind the nearest wall.
            int t = own.getInt("ThisVisitTicks");
            if (t >= own.getInt("Carpenter")) { carpenters(p, b, null);own.putInt("Carpenter", t + 100 + p.getRandom().nextInt(120)); }
            return;
        }
        int due = (int) Math.min(STRIKES, (now - own.getLong("RunAt")) / STRIKE_EVERY + 1), struck = own.getInt("Strikes");
        for (; struck < due; struck++) sound(p, Vec3.atCenterOf(b.offset(CLOCK.above())), LiteraryRegistry.WINCHESTER_CHIME.get(), 3F, 1F);
        own.putInt("Strikes", struck);
        if (zone(p, b) == Zone.LEDGER) { reached(p, own);return; }
        if (struck >= STRIKES && now - own.getLong("RunAt") >= (long) (STRIKES - 1) * STRIKE_EVERY + 40) { walled(p, b, own);return; }
        // Where they will work next for this reader, the hammering comes from there.
        if (now >= own.getLong("NextHammer")) {
            for (var stage : Stage.values()) if (wants(p, b, stage) && !built(l, b, stage)) { carpenters(p, b, stage);break; }
            own.putLong("NextHammer", now + 18 + p.getRandom().nextInt(10));
        }
    }
    /** A hammer or a saw: at the piece they will build next, or before the night behind the nearer side wall. */
    static void carpenters(ServerPlayer p, BlockPos b, Stage next) {
        var rng = p.getRandom();Vec3 at;
        if (next != null) { var cells = cells(next);at = centre(b, cells.get(rng.nextInt(cells.size()))); }
        else { double rx = p.getX() - b.getX(), rz = p.getZ() - b.getZ();at = Vec3.atLowerCornerOf(b).add(rx < 0 ? -30.5 : 30.5, Math.max(1, p.getY() - b.getY() + 1), Math.max(-76, Math.min(-2, rz + rng.nextInt(13) - 6))); }
        boolean saw = rng.nextInt(4) == 0;
        sound(p, at, saw ? LiteraryRegistry.WINCHESTER_SAW.get() : LiteraryRegistry.WINCHESTER_HAMMER.get(), .9F, .9F + rng.nextFloat() * .2F);
    }

    /** The reader's hand on a robe or on the bell. */
    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        var s = p.serverLevel().getBlockState(b.offset(rel));
        if (s.is(HouseBlocks.SCENE_DETAIL.get()) && s.getValue(SceneDetailBlock.KIND) == SceneDetailBlock.Kind.COAT && rel.getZ() <= -45 && rel.getZ() >= -58) {
            if (own.getBoolean("Robed")) cue(p, "You are already wearing one.");
            else { own.putBoolean("Robed", true);cue(p, "You take a robe down from its hook and put it on. It is cold.");p.serverLevel().playSound(null, b.offset(rel), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, .6F, .8F); }
            return true;
        }
        if (!rel.equals(TABLE)) return false;
        if (own.getBoolean("Ready")) { cue(p, "The bell is quiet. The plans are done.");return true; }
        if (own.getBoolean("InRun")) { cue(p, "The bell rings. Far off, the clock is already striking.");return true; }
        if (!own.getBoolean("Robed")) { cue(p, "The bell will not sound. The robes hang on their hooks.");return true; }
        ring(p, b, own);return true;
    }
    /** The bell: the clock begins to strike, for everyone in the house not done with it, and the ringer has the plan. */
    static void ring(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();long now = l.getGameTime();var d = LabyrinthData.get(p.server);
        l.playSound(null, b.offset(TABLE), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1F, 1.2F);
        start(own, now);
        for (var o : readers(l, b)) {
            if (o == p) continue;var t = LiteraryVignettes.personal(d, o.getUUID(), PLACE);
            if (t.getBoolean("Ready") || t.getBoolean("InRun")) continue;
            start(t, now);LiteraryVignettes.save(d, o.getUUID(), PLACE, t);cue(o, "Somewhere in the house a bell rings, and the clock begins to strike.");
        }
        if (!own.getBoolean("PlanGiven")) { own.putBoolean("PlanGiven", true);var plan = plan();if (!p.getInventory().add(plan)) p.drop(plan, false); }
        cue(p, "The bell rings once. Far off in the house, the clock begins to strike.");
    }
    static void start(CompoundTag own, long now) { own.putBoolean("InRun", true);own.putLong("RunAt", now);own.putInt("Strikes", 0);own.putInt("Nights", own.getInt("Nights") + 1); }

    /** In the last room before the thirteenth stroke: the ledger can be read, and the names in it are the reader's own. */
    static void reached(ServerPlayer p, CompoundTag own) {
        own.putBoolean("InRun", false);own.putString("Ledger201", ledger(p));
        LiteraryVignettes.ready(p, PLACE, own, own.getInt("Walled") > 0 ? "reached_the_upper_ledger_after_a_room_was_built_round_them" : "reached_the_upper_ledger_before_the_thirteenth_stroke");
    }
    /** The thirteenth stroke: hammering close all round, the dark, and the front door. */
    static void walled(ServerPlayer p, BlockPos b, CompoundTag own) {
        own.putBoolean("InRun", false);own.putInt("Walled", own.getInt("Walled") + 1);own.putInt("Strikes", 0);
        for (int i = 0; i < 6; i++) sound(p, p.position().add(Math.cos(i) * 1.5, 1 + (i % 2), Math.sin(i) * 1.5), LiteraryRegistry.WINCHESTER_HAMMER.get(), 1.2F, .8F + i * .07F);
        HousePackets.send(p, new HouseFadePayload(6, 30, 24));
        var at = b.offset(WAKE);p.teleportTo(p.serverLevel(), at.getX() + .5, at.getY(), at.getZ() + .5, 180, 0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
        cue(p, "The thirteenth stroke. Hammering all round you, close; then the front door, and the house quiet.");
    }

    private static void sound(ServerPlayer p, Vec3 at, SoundEvent sound, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.AMBIENT, at.x, at.y, at.z, volume, pitch, p.getRandom().nextLong()));
    }
    private static void cue(ServerPlayer p, String line) { p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true); }

    // ------------------------------------------------------------------------------------------------ the papers
    /** Tonight's plan, given at the bell: the way up through the house's mistakes, and its one rule. */
    public static ItemStack plan() {
        return HouseWriting.book("Tonight's plan", "S. W.", HouseWriting.WritingStyle.WILL, List.of(
                "Thirteen strokes, and then the night's work is done, whoever is still inside it.\n\nThe door with the brass thirteen. The stair that goes into the ceiling. The room with its floor taken up: cross on the glass. The door that opens on nothing.",
                "The carpenters will not work while they are watched. Do not look at the work.\n\nThe ledger is in the last room. Be there before the thirteenth stroke, or they will build a room around you and write your name beside it."));
    }
    /** The rooms of the upper ledger: one for every kind of creature this reader has killed, most first. */
    static String ledger(ServerPlayer p) {
        var stats = p.getStats();var kills = new ArrayList<Map.Entry<String, Integer>>();
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            if (type == EntityType.PLAYER) continue;
            int n = stats.getValue(Stats.ENTITY_KILLED.get(type));
            if (n > 0) kills.add(Map.entry(type.getDescription().getString(), n));
        }
        kills.sort((a, c) -> c.getValue() - a.getValue());
        var lines = new ArrayList<String>();
        for (int i = 0; i < Math.min(ROOMS.length, kills.size()); i++) lines.add(ROOMS[i] + ": " + count(kills.get(i).getValue(), kills.get(i).getKey()) + ".");
        return String.join(" ", lines);
    }
    static String count(int n, String name) {
        String thing = name.toLowerCase(java.util.Locale.ROOT);
        if (n == 1) return ("aeiou".indexOf(thing.charAt(0)) >= 0 ? "an " : "a ") + thing;
        return words(n) + " " + plural(thing);
    }
    static String plural(String t) {
        if (t.endsWith("sheep") || t.endsWith("fish") || t.endsWith("cod") || t.endsWith("salmon") || t.endsWith("squid")) return t;
        if (t.endsWith("man")) return t.substring(0, t.length() - 3) + "men";
        if (t.endsWith("wolf")) return t.substring(0, t.length() - 1) + "ves";
        if (t.endsWith("ch") || t.endsWith("sh") || t.endsWith("x") || t.endsWith("s") || t.endsWith("z")) return t + "es";
        if (t.endsWith("y") && t.length() > 1 && "aeiou".indexOf(t.charAt(t.length() - 2)) < 0) return t.substring(0, t.length() - 1) + "ies";
        return t + "s";
    }
    private static final String[] SMALL = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] TENS = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    static String words(int n) {
        if (n < 20) return SMALL[n];
        if (n < 100) return TENS[n / 10] + (n % 10 == 0 ? "" : "-" + SMALL[n % 10]);
        if (n < 1000) return SMALL[n / 100] + " hundred" + (n % 100 == 0 ? "" : " and " + words(n % 100));
        return String.format(java.util.Locale.ROOT, "%,d", n);
    }
    /** The account: the night, the strokes, the ledger's rooms and the last name. */
    public static String account(CompoundTag own) {
        boolean walled = own.getInt("Walled") > 0;String names = own.getString("Ledger201");
        return "The clock struck while I climbed, and the carpenters built only where I was not looking: the wall behind a door, a stair into the ceiling, boards over nothing."
                + (walled ? " Once the thirteenth stroke came first, and they built a room around me; I woke by the front door." : "")
                + " In the upper ledger every room had a name beside it. " + (names.isEmpty() ? "None of them were mine to answer for." : names + " Every one of them had died by my hand.")
                + " The last line was my name. " + (walled ? "Beside it was the room they had built around me." : "It had no room yet.");
    }

    /** The cells a generic test may treat as the house's own fittings. */
    public static boolean fitting(BlockState s) { return s.is(LiteraryRegistry.WINCHESTER_FITTING.get()) || s.is(HouseBlocks.SCENE_DETAIL.get()) || s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get()); }
}
