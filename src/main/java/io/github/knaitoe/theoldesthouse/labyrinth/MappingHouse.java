package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * Mapping the Interior, rebuilt whole (0.4.201; docs/MAPPING_INTERIOR_REWORK.md). A small modular house on a skirt at the edge of
 * a dirt lot, at 2:49 in the morning: the front room with its window, the kitchen with the floor plan on its table, the hall that
 * leads back to the utility room, the boys' room with Dino's bed, the mother's shut door. Under the house the crawl runs on past
 * the walls, down a slope and out under the yard to a dry plywood door in the dirt; under that is black water, and from the
 * water a ladder climbs to the old tetherball pole. The neighbour's dogs are in their run next door.
 *
 * <p>The floor is a block above the yard, so the gap under it is one block high and every reader in it crawls; the tunnel past
 * the roofline is dug two blocks lower, one high, under the yard's own turf.
 */
public final class MappingHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** Where every night begins: standing at the front window, looking out. */
    public static final Vec3 WAKE = new Vec3(-6.5, 2, -13.4);
    /** The floor plan on the kitchen table, and the account on the boys' desk. */
    public static final BlockPos SOURCE = new BlockPos(4, 2, -16), ENDING = new BlockPos(-3, 2, -27);
    /** His walk: out of the kitchen doorway, up the hall, in at the utility room door. */
    public static final Vec3[] CROSSING = {new Vec3(.5, 2, -17.5), new Vec3(.5, 2, -19.5), new Vec3(.5, 2, -23.5), new Vec3(2.5, 2, -23.5), new Vec3(5.5, 2, -24.5)};
    /** Where the bead lies after his first crossing: on the hall carpet. */
    public static final Vec3 BEAD = new Vec3(.5, 2, -21.5);
    /** Dino's bed (its foot; the head lies north) and where the father stands over it. */
    public static final BlockPos DINO_BED = new BlockPos(-7, 2, -24);
    public static final Vec3 OVER_DINO = new Vec3(-5.6, 2, -24.5);
    /** The loose skirt panel on the east side, the hatch in the utility room floor, the plywood door at the tunnel's end. */
    public static final BlockPos PANEL = new BlockPos(10, 0, -16), HATCH = new BlockPos(5, 1, -26), PLYWOOD = new BlockPos(4, -3, -63);
    /** The water under the plywood door, where he lies; the shaft up to the tetherball pole and its lid. */
    public static final Vec3 WATER = new Vec3(4.5, -5, -64.5);
    public static final BlockPos SHAFT_LID = new BlockPos(8, -1, -64), POLE = new BlockPos(10, 0, -64);
    /** The neighbour's dog run, its gate, and the pocket in the tunnel where what is left of the dogs turns up. */
    public static final BlockPos DOG_GATE = new BlockPos(13, 0, -14), REMAINS = new BlockPos(6, -2, -42);
    public static final Vec3[] DOGS = {new Vec3(16.5, 0, -11.5), new Vec3(17.5, 0, -17.5)};
    /** The mother's door, which stays shut. */
    public static final BlockPos MOM_DOOR = new BlockPos(2, 2, -21);

    private MappingHouse() {}

    // ------------------------------------------------------------------------------------------------ where a reader is
    /** Under the floor, in the tunnel past the roofline, or ducking in at the open skirt panel: anywhere a reader must crawl. */
    public static boolean crawling(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        if (y >= .9) return false;
        boolean under = x > -9.7 && x < 9.7 && z < -12.3 && z > -27.7;
        boolean tunnel = x > 3.3 && x < 5.7 && z <= -27.7 && z > -63.6 && y < -1.1 && y > -2.6;
        boolean pocket = x > 2 && x < 8 && z < -39 && z > -45 && y < -1.1 && y > -2.6;
        boolean ducking = p.isShiftKeyDown() && x >= 9.7 && x < 11.5 && z < -15 && z > -17;
        return under || tunnel || pocket || ducking;
    }
    /** Past the house's own walls, under the yard. */
    public static boolean beyondRoof(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        return y < -1 && z < -29 && x > 1 && x < 8;
    }
    /** In the water under the plywood door. */
    public static boolean inWater(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        return y < -3.5 && y > -6 && x > 1.7 && x < 8.3 && z < -60.3 && z > -67.7;
    }
    /** Climbing the shaft to the pole. */
    public static boolean inShaft(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        return x > 7.6 && x < 9 && z < -63.6 && z > -64.6 && y < -.5 && y > -5.6;
    }
    /** Inside the house's walls, upstairs of the crawl. */
    public static boolean indoors(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        return y >= 1.5 && y < 5 && x > -10 && x < 10 && z < -12 && z > -28;
    }
    /** In the boys' room. */
    public static boolean boysRoom(Player p, BlockPos b) {
        double x = p.getX() - b.getX(), y = p.getY() - b.getY(), z = p.getZ() - b.getZ();
        return y >= 1.5 && y < 5 && x > -10 && x < -1 && z < -19 && z > -28;
    }

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, Block s) { set(l, b, x, y, z, s.defaultBlockState()); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(l, b, x, y, z, s);
    }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, Block s) { fill(l, b, x0, y0, z0, x1, y1, z1, s.defaultBlockState()); }
    private static void tract(ServerLevel l, BlockPos b, int x, int y, int z, Kind k, Direction f) { set(l, b, x, y, z, TractHouseBlock.of(k, f)); }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind k, Direction f) { set(l, b, x, y, z, HouseholdFurnitureBlock.state(k, f)); }
    private static void detail(ServerLevel l, BlockPos b, int x, int y, int z, SceneDetailBlock.Kind k, Direction f) { set(l, b, x, y, z, SceneDetailBlock.state(k, f)); }
    private static void light(ServerLevel l, BlockPos b, int x, int y, int z, int level) { set(l, b, x, y, z, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, level)); }
    private static void door(ServerLevel l, BlockPos b, int x, int y, int z, Direction facing, Block block, boolean open) { NovelRooms.door(l, b.offset(x, y, z), facing, block, open); }
    private static BlockState trapdoor(Block block, Direction facing, Half half, boolean open) {
        return block.defaultBlockState().setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half).setValue(TrapDoorBlock.OPEN, open);
    }
    private static BlockState slab(Block block, SlabType type) { return block.defaultBlockState().setValue(SlabBlock.TYPE, type); }

    public static void build(ServerLevel l, BlockPos b) {
        BlockState air = Blocks.AIR.defaultBlockState(), siding = LiteraryRegistry.SIDING.get().defaultBlockState();
        BlockState panel = Blocks.SPRUCE_PLANKS.defaultBlockState(), ceiling = Blocks.WHITE_CONCRETE.defaultBlockState();

        // ---- the lot: packed dirt and dry grass under a night sky, stone below, everything above cleared
        fill(l, b, -22, 0, -79, 22, 15, -1, air);
        fill(l, b, -22, -6, -79, 22, -2, -1, Blocks.DIRT);
        for (int x = -22; x <= 22; x++) for (int z = -79; z <= -1; z++) {
            int h = Math.floorMod(x * 73 + z * 31, 11);
            set(l, b, x, -1, z, h < 3 ? Blocks.COARSE_DIRT : h < 4 ? Blocks.ROOTED_DIRT : Blocks.GRASS_BLOCK);
            if (h == 5 && Math.abs(x) > 12) set(l, b, x, 0, z, Blocks.SHORT_GRASS);
            if (h == 7 && (Math.abs(x) > 15 || z < -40)) set(l, b, x, 0, z, Blocks.DEAD_BUSH);
        }
        // The gravel drive from the road to the porch.
        fill(l, b, -4, -1, -10, -2, -1, -1, Blocks.GRAVEL);
        // A sagging wire fence round the lot, posts every four blocks, gapped at the drive.
        for (int x = -20; x <= 20; x++) for (int z : new int[]{-1, -76}) if (z == -76 || Math.abs(x + 3) > 2) fencePost(l, b, x, z);
        for (int z = -76; z <= -1; z++) for (int x : new int[]{-20, 20}) fencePost(l, b, x, z);
        // The propane tank on its blocks beside the house.
        fill(l, b, -15, 0, -21, -15, 0, -18, Blocks.STONE_BRICK_WALL);
        fill(l, b, -15, 1, -21, -15, 1, -18, Blocks.SMOOTH_QUARTZ);
        set(l, b, -15, 1, -22, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.TOP));set(l, b, -15, 1, -17, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.TOP));
        set(l, b, -15, 2, -20, Blocks.LIGHTNING_ROD);
        // The old tetherball pole out back, its ball gone flat at its foot.
        for (int y = 0; y <= 3; y++) set(l, b, POLE.getX(), y, POLE.getZ(), Blocks.IRON_BARS);
        set(l, b, POLE.getX(), 4, POLE.getZ(), Blocks.LIGHTNING_ROD);set(l, b, POLE.getX() + 1, 0, POLE.getZ(), Blocks.BROWN_CARPET);

        // ---- the neighbour's dog run: chain-link, a doghouse, a gate onto this lot
        for (int z = -21; z <= -7; z++) for (int x : new int[]{13, 19}) for (int y = 0; y <= 1; y++) set(l, b, x, y, z, Blocks.IRON_BARS);
        for (int x = 13; x <= 19; x++) for (int z : new int[]{-7, -21}) for (int y = 0; y <= 1; y++) set(l, b, x, y, z, Blocks.IRON_BARS);
        for (int y = 0; y <= 1; y++) set(l, b, DOG_GATE.getX(), y, DOG_GATE.getZ(), air);
        set(l, b, DOG_GATE.getX(), 0, DOG_GATE.getZ(), Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.EAST));
        fill(l, b, 16, 0, -20, 18, 1, -19, Blocks.SPRUCE_PLANKS);fill(l, b, 16, 2, -20, 18, 2, -19, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        set(l, b, 17, 0, -19, air);set(l, b, 17, 1, -19, air);
        fill(l, b, 14, -1, -20, 18, -1, -8, Blocks.COARSE_DIRT);
        set(l, b, 15, 0, -9, Blocks.CAULDRON);

        // ---- the house: a floor a block over the yard, siding walls, a white ceiling and a low roof
        fill(l, b, -10, 1, -28, 10, 1, -12, Blocks.SPRUCE_PLANKS);
        for (int x = -10; x <= 10; x++) for (int y = 2; y <= 4; y++) { set(l, b, x, y, -12, siding);set(l, b, x, y, -28, siding); }
        for (int z = -28; z <= -12; z++) for (int y = 2; y <= 4; y++) { set(l, b, -10, y, z, siding);set(l, b, 10, y, z, siding); }
        fill(l, b, -10, 5, -28, 10, 5, -12, ceiling);
        // The roof: a shallow gable along the length, eaves over the walls.
        for (int x = -11; x <= 11; x++) for (int z = -29; z <= -11; z++) {
            int d = Math.abs(x);
            if (d >= 8) set(l, b, x, d == 11 ? 5 : 6, z, slab(Blocks.DEEPSLATE_TILE_SLAB, d == 11 ? SlabType.TOP : SlabType.BOTTOM));
            else if (d >= 3) set(l, b, x, 6, z, Blocks.DEEPSLATE_TILES);
            else { set(l, b, x, 6, z, Blocks.DEEPSLATE_TILES);set(l, b, x, 7, z, slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM)); }
        }
        // The skirt round the base; one panel on the east side has come loose.
        for (int x = -10; x <= 10; x++) { set(l, b, x, 0, -12, Blocks.WHITE_TERRACOTTA);set(l, b, x, 0, -28, Blocks.WHITE_TERRACOTTA); }
        for (int z = -28; z <= -12; z++) { set(l, b, -10, 0, z, Blocks.WHITE_TERRACOTTA);set(l, b, 10, 0, z, Blocks.WHITE_TERRACOTTA); }
        set(l, b, PANEL.getX(), PANEL.getY(), PANEL.getZ(), trapdoor(Blocks.BIRCH_TRAPDOOR, Direction.WEST, Half.BOTTOM, true));
        // Under the floor: the crawl, a block high, cinderblock piers, a duct.
        fill(l, b, -9, 0, -27, 9, 0, -13, air);
        for (int x : new int[]{-6, 0, 7}) for (int z : new int[]{-16, -22}) set(l, b, x, 0, z, Blocks.POLISHED_ANDESITE);
        // The slope at the back of the crawl, down two blocks in halves, toward where the house stops.
        for (int x = 4; x <= 5; x++) {
            set(l, b, x, -1, -22, slab(Blocks.MUD_BRICK_SLAB, SlabType.BOTTOM));
            set(l, b, x, -1, -23, air);set(l, b, x, -2, -23, Blocks.PACKED_MUD);
            set(l, b, x, -1, -24, air);set(l, b, x, -2, -24, slab(Blocks.MUD_BRICK_SLAB, SlabType.BOTTOM));
            for (int z = -27; z <= -25; z++) { set(l, b, x, -1, z, air);set(l, b, x, -2, z, air);set(l, b, x, -3, z, Blocks.PACKED_MUD); }
        }
        // Past the roofline: one block high, two wide, under the yard's turf, to the plywood door.
        for (int z = -63; z <= -28; z++) for (int x = 4; x <= 5; x++) { set(l, b, x, -2, z, air);set(l, b, x, -3, z, Blocks.PACKED_MUD); }
        for (int x = 3; x <= 6; x++) set(l, b, x, -2, -28, x == 3 || x == 6 ? Blocks.DIRT.defaultBlockState() : air);
        // A wider pocket halfway, roots through its ceiling.
        for (int x = 2; x <= 7; x++) for (int z = -44; z <= -40; z++) { set(l, b, x, -2, z, air);set(l, b, x, -3, z, Blocks.PACKED_MUD); }
        for (int[] c : new int[][]{{3, -41}, {6, -43}, {2, -44}, {7, -40}}) set(l, b, c[0], -2, c[1], Blocks.HANGING_ROOTS);
        // The plywood door with its pull ring, shut over the water.
        // The water under it: a cistern, its old walls, a hand of water deep over a mud floor.
        fill(l, b, 1, -6, -68, 8, -3, -60, Blocks.MUD_BRICKS);
        fill(l, b, 2, -6, -67, 7, -6, -61, Blocks.MUD);
        fill(l, b, 2, -5, -67, 7, -5, -61, Blocks.WATER);
        fill(l, b, 2, -4, -67, 7, -4, -61, air);
        set(l, b, PLYWOOD.getX(), PLYWOOD.getY(), PLYWOOD.getZ(), trapdoor(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, Half.TOP, false));
        set(l, b, PLYWOOD.getX() + 1, PLYWOOD.getY(), PLYWOOD.getZ(), Blocks.MUD_BRICKS);
        // The shaft from the water to the pole: a ladder up the east side, a lid in the yard.
        for (int y = -5; y <= -2; y++) {
            set(l, b, 8, y, -64, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST).setValue(LadderBlock.WATERLOGGED, y == -5));
            set(l, b, 9, y, -64, Blocks.MUD_BRICKS);
        }
        set(l, b, SHAFT_LID.getX(), SHAFT_LID.getY(), SHAFT_LID.getZ(), trapdoor(Blocks.IRON_TRAPDOOR, Direction.WEST, Half.TOP, false));

        // ---- inside: walls, rooms, doors
        // Partition between the front room and the kitchen, open at its back for the kitchen doorway.
        for (int z = -16; z <= -13; z++) for (int y = 2; y <= 4; y++) set(l, b, 0, y, z, panel);
        for (int y = 2; y <= 3; y++) for (int z = -18; z <= -17; z++) set(l, b, 0, y, z, air);
        set(l, b, 0, 4, -17, panel);set(l, b, 0, 4, -18, panel);
        // The wall across the house, open where the hall begins.
        for (int x = -9; x <= 9; x++) for (int y = 2; y <= 4; y++) if (x < 0 || x > 1 || y == 4) set(l, b, x, y, -19, panel);
        // The hall's walls; the boys' room west of it, the mother's room and the utility room east.
        for (int z = -27; z <= -20; z++) for (int y = 2; y <= 4; y++) set(l, b, -1, y, z, panel);
        for (int z = -27; z <= -20; z++) for (int y = 2; y <= 4; y++) set(l, b, 2, y, z, panel);
        for (int x = 0; x <= 1; x++) for (int y = 2; y <= 4; y++) set(l, b, x, y, -25, panel);
        for (int x = 3; x <= 9; x++) for (int y = 2; y <= 4; y++) set(l, b, x, y, -23, panel);
        door(l, b, -1, 2, -22, Direction.WEST, Blocks.BIRCH_DOOR, true);
        door(l, b, MOM_DOOR.getX(), MOM_DOOR.getY(), MOM_DOOR.getZ(), Direction.EAST, Blocks.BIRCH_DOOR, false);
        door(l, b, 2, 2, -24, Direction.EAST, Blocks.BIRCH_DOOR, true);
        door(l, b, -3, 2, -12, Direction.SOUTH, Blocks.SPRUCE_DOOR, false);
        // Windows: the front room's, the kitchen's, the boys' room's, the mother's.
        for (int x = -8; x <= -6; x++) for (int y = 2; y <= 3; y++) set(l, b, x, y, -12, Blocks.GLASS_PANE);
        for (int x = 4; x <= 6; x++) set(l, b, x, 3, -12, Blocks.GLASS_PANE);
        for (int z = -25; z <= -23; z++) set(l, b, -10, 3, z, Blocks.GLASS_PANE);
        set(l, b, 10, 3, -21, Blocks.GLASS_PANE);
        // Floors: carpet in the front room, the hall and the bedrooms, linoleum in the kitchen and the utility room.
        fill(l, b, -9, 2, -18, -1, 2, -13, Blocks.BROWN_CARPET);
        fill(l, b, 0, 2, -24, 1, 2, -19, Blocks.BROWN_CARPET);
        fill(l, b, -9, 2, -27, -2, 2, -20, Blocks.BLUE_CARPET);
        fill(l, b, 3, 2, -22, 9, 2, -20, Blocks.LIGHT_GRAY_CARPET);
        fill(l, b, 1, 1, -18, 9, 1, -13, Blocks.WHITE_TERRACOTTA);
        fill(l, b, 3, 1, -27, 9, 1, -24, Blocks.WHITE_TERRACOTTA);

        // ---- the porch: a landing at the door, steps down to the drive
        fill(l, b, -5, 1, -11, -1, 1, -11, Blocks.SPRUCE_PLANKS);
        for (int x = -5; x <= -1; x++) set(l, b, x, 0, -10, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        for (int x : new int[]{-5, -1}) set(l, b, x, 2, -11, Blocks.SPRUCE_FENCE);
        // The back steps from the utility room's screen door.
        set(l, b, 7, 1, -29, Blocks.SPRUCE_PLANKS);set(l, b, 7, 0, -30, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));

        // ---- the front room: the couch, the recliner, the television, curtains
        tract(l, b, -9, 2, -14, Kind.SOFA_LEFT, Direction.EAST);tract(l, b, -9, 2, -15, Kind.SOFA_MIDDLE, Direction.EAST);tract(l, b, -9, 2, -16, Kind.SOFA_RIGHT, Direction.EAST);
        tract(l, b, -6, 2, -15, Kind.COFFEE_TABLE, Direction.EAST);tract(l, b, -3, 2, -17, Kind.RECLINER, Direction.WEST);
        tract(l, b, -2, 2, -14, Kind.TV_OFF, Direction.WEST);tract(l, b, -9, 2, -18, Kind.END_TABLE_LAMP, Direction.EAST);
        tract(l, b, -8, 4, -13, Kind.CURTAIN_LEFT, Direction.SOUTH);tract(l, b, -6, 4, -13, Kind.CURTAIN_RIGHT, Direction.SOUTH);
        tract(l, b, -5, 3, -18, Kind.FAMILY_PHOTO, Direction.SOUTH);
        // ---- the kitchen: counter, sink and stove along the east wall, the fridge, the table with the floor plan
        tract(l, b, 9, 2, -13, Kind.FRIDGE_LOWER, Direction.WEST);tract(l, b, 9, 3, -13, Kind.FRIDGE_UPPER, Direction.WEST);
        tract(l, b, 9, 2, -14, Kind.COUNTER, Direction.WEST);tract(l, b, 9, 2, -15, Kind.COUNTER_SINK, Direction.WEST);
        tract(l, b, 9, 2, -16, Kind.STOVE, Direction.WEST);tract(l, b, 9, 2, -17, Kind.COUNTER, Direction.WEST);
        tract(l, b, 9, 4, -15, Kind.WALL_CABINET, Direction.WEST);tract(l, b, 9, 4, -17, Kind.WALL_CABINET, Direction.WEST);
        tract(l, b, 1, 3, -13, Kind.WALL_PHONE, Direction.EAST);
        tract(l, b, 4, 2, -15, Kind.TABLE_WEST, Direction.SOUTH);tract(l, b, 5, 2, -15, Kind.TABLE_EAST, Direction.SOUTH);
        tract(l, b, 4, 2, -14, Kind.DINING_CHAIR, Direction.NORTH);tract(l, b, 6, 2, -15, Kind.KIDS_CHAIR, Direction.WEST);
        tract(l, b, 3, 2, -15, Kind.DINING_CHAIR, Direction.EAST);
        // ---- the boys' room: two beds, the toy chest, the desk where the account will lie
        NovelRooms.bed(l, b.offset(DINO_BED), Blocks.BLUE_BED, Direction.NORTH);
        NovelRooms.bed(l, b.offset(-4, 2, -24), Blocks.RED_BED, Direction.NORTH);
        tract(l, b, -9, 2, -21, Kind.TOY_CHEST, Direction.EAST);detail(l, b, -9, 3, -20, SceneDetailBlock.Kind.TOYS, Direction.EAST);
        furniture(l, b, ENDING.getX(), ENDING.getY() - 1 + 1, ENDING.getZ() + 1, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.NORTH);
        furniture(l, b, -6, 2, -27, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.SOUTH);
        // ---- the mother's room behind its shut door
        NovelRooms.bed(l, b.offset(6, 2, -21), Blocks.WHITE_BED, Direction.EAST);
        furniture(l, b, 4, 2, -20, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.SOUTH);
        // ---- the utility room: the washer, the water heater, the hamper
        furniture(l, b, 9, 2, -27, HouseholdFurnitureBlock.Kind.WASHING_MACHINE, Direction.WEST);
        set(l, b, 9, 2, -26, Blocks.CAULDRON);set(l, b, 9, 3, -26, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM));
        detail(l, b, 8, 2, -24, SceneDetailBlock.Kind.TOWELS, Direction.SOUTH);

        // ---- the night: no lamps on, the stove's clock, a night light in the boys' room, the porch light
        light(l, b, 8, 3, -16, 6);light(l, b, -8, 2, -26, 4);light(l, b, -6, 3, -11, 9);light(l, b, 0, 4, -21, 3);light(l, b, 6, 4, -25, 3);
        light(l, b, 4, -1, -45, 2);light(l, b, 4, -4, -64, 3);
    }
    private static void fencePost(ServerLevel l, BlockPos b, int x, int z) {
        boolean post = Math.floorMod(x + z, 4) == 0;
        set(l, b, x, 0, z, post ? Blocks.SPRUCE_FENCE.defaultBlockState() : Blocks.TRIPWIRE.defaultBlockState());
    }

    /** The utility room's hatch, shown by deadfooting: the floor there becomes a trapdoor onto the slope below. */
    public static void showHatch(ServerLevel l, BlockPos b) {
        var at = b.offset(HATCH);
        if (!(l.getBlockState(at).getBlock() instanceof TrapDoorBlock)) l.setBlock(at, trapdoor(Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH, Half.TOP, false), Block.UPDATE_ALL);
    }
    public static boolean hatchShown(ServerLevel l, BlockPos b) { return l.getBlockState(b.offset(HATCH)).getBlock() instanceof TrapDoorBlock; }
    /** The plywood door: shut and held from below until the last night, open over the water then. */
    public static void plywood(ServerLevel l, BlockPos b, boolean open) {
        var at = b.offset(PLYWOOD);var s = l.getBlockState(at);
        if (s.getBlock() instanceof TrapDoorBlock && s.getValue(TrapDoorBlock.OPEN) != open) l.setBlock(at, s.setValue(TrapDoorBlock.OPEN, open), Block.UPDATE_ALL);
    }
    /** The lid at the top of the shaft: it lifts for whoever is climbing it, and drops when they are out. */
    public static void lid(ServerLevel l, BlockPos b, boolean open) {
        var at = b.offset(SHAFT_LID);var s = l.getBlockState(at);
        if (s.getBlock() instanceof TrapDoorBlock && s.getValue(TrapDoorBlock.OPEN) != open) l.setBlock(at, s.setValue(TrapDoorBlock.OPEN, open), Block.UPDATE_ALL);
    }
}
