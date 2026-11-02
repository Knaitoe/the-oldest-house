package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.RugFloorBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * Disappearance at Devil's Rock, rebuilt whole (0.4.201; docs/DEVILS_ROCK_REWORK.md). The Sandersons' one-storey house at
 * midnight, its back yard and gate, and the park behind it: woods with old stone walls, the boulder split clean through that
 * the boys called Devil's Rock, and a weedy pond with an island where two rocks stand almost pushed together.
 *
 * <p>The ground is grass at y -1 everywhere but the pond; the house's floor is at the same height, so the reader walks in
 * off the front path. The trail runs from the back gate through the split; the chase's loop runs round the woods beyond.
 */
public final class DevilsRockHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final LabyrinthPlace PLACE = LabyrinthPlace.DEVILS_ROCK;
    /** The mother's watch log on the kitchen table, and the account on the island, south of the two rocks. */
    public static final BlockPos SOURCE = new BlockPos(7, 1, -14), ENDING = new BlockPos(10, 0, -107);
    /** Where each loose page lands: eight rooms and corners of the house, the last in the middle of Tommy's floor. */
    public static final BlockPos[] PAGES = {new BlockPos(-8, 0, -16), new BlockPos(5, 0, -11), new BlockPos(0, 0, -20), new BlockPos(1, 0, -9),
        new BlockPos(-6, 0, -26), new BlockPos(8, 0, -22), new BlockPos(8, 0, -28), new BlockPos(-8, 0, -25)};
    /** The mother's camera on its tripod in the living room, and the trail camera strapped to a trunk facing the rock. */
    public static final BlockPos CAMERA = new BlockPos(-4, 0, -17), TRAIL_CAMERA = new BlockPos(-7, 1, -66);
    /** The old pennies on Tommy's bureau, and the same pennies between the two rocks on the island. */
    public static final BlockPos BUREAU_PENNIES = new BlockPos(-5, 1, -29), ISLAND_PENNIES = new BlockPos(10, 0, -111);
    /** Where the shape crouches: between the chair and the end table, at the end of the hall, in the split of the rock. */
    public static final Vec3[] SHAPE = {new Vec3(5.5, 0, -28.5), new Vec3(.5, 0, -29.3), new Vec3(.5, 0, -71.5)};
    /** The split's middle, where the chase begins; and where the double comes out of the trees beside the rock. */
    public static final Vec3 SPLIT = new Vec3(.5, 0, -71.5), TREES = new Vec3(-9.5, 0, -73.5);
    /** On the island: the gap between the two rocks, where it crouches at the end. */
    public static final Vec3 GAP = new Vec3(10.5, 0, -109.8);
    /** The chase's loop through the woods round the rock, walked either way. */
    public static final Vec3[] LOOP = {new Vec3(.5, 0, -79.5), new Vec3(6.5, 0, -84.5), new Vec3(14.5, 0, -86.5), new Vec3(21.5, 0, -80.5),
        new Vec3(22.5, 0, -70.5), new Vec3(18.5, 0, -60.5), new Vec3(10.5, 0, -54.5), new Vec3(.5, 0, -52.5), new Vec3(-9.5, 0, -54.5),
        new Vec3(-17.5, 0, -60.5), new Vec3(-21.5, 0, -70.5), new Vec3(-19.5, 0, -80.5), new Vec3(-12.5, 0, -86.5), new Vec3(-5.5, 0, -84.5)};
    /** From the loop's foot down to the water and out across it to the island's shore and the gap. */
    public static final Vec3[] TO_ISLAND = {new Vec3(.5, 0, -89.5), new Vec3(2.5, -.4, -94.5), new Vec3(7.5, -1.4, -101.5), new Vec3(10.5, 0, -105.6), GAP};
    /** Pond: an ellipse of water four deep; the island inside it. */
    static final double POND_X = 0, POND_Z = -106, POND_RX = 26, POND_RZ = 14, ISLAND_X = 10, ISLAND_Z = -109.5, ISLAND_RX = 5.2, ISLAND_RZ = 4.4;

    private DevilsRockHouse() {}

    // ------------------------------------------------------------------------------------------------ where a reader is
    public static boolean inPond(double x, double z) { return sq((x - POND_X) / POND_RX) + sq((z - POND_Z) / POND_RZ) <= 1; }
    public static boolean onIsland(double x, double z) { return sq((x - ISLAND_X) / ISLAND_RX) + sq((z - ISLAND_Z) / ISLAND_RZ) <= 1; }
    /** Past the back fence: in the park. */
    public static boolean inPark(Vec3 rel) { return rel.z < -45.5; }
    public static boolean onIslandRel(Vec3 rel) { return onIsland(rel.x, rel.z) && rel.y > -1.2; }
    private static double sq(double v) { return v * v; }

    // ------------------------------------------------------------------------------------------------ the build
    public static void build(ServerLevel l, BlockPos b) {
        ground(l, b);
        house(l, b);
        yard(l, b);
        woods(l, b);
        rock(l, b);
        pond(l, b);
        island(l, b);
        lights(l, b);
    }

    private static void ground(ServerLevel l, BlockPos b) {
        fill(l, b, -34, -6, -124, 34, -2, -1, Blocks.DIRT.defaultBlockState());
        for (int x = -34; x <= 34; x++) for (int z = -124; z <= -1; z++) {
            int h = hash(x, z) % 13;
            set(l, b, x, -1, z, z < -46 && h < 2 ? Blocks.PODZOL : z < -46 && h == 2 ? Blocks.COARSE_DIRT : Blocks.GRASS_BLOCK);
        }
        // The front walk from the street to the door, and the drive beside it.
        fill(l, b, -1, -1, -6, 1, -1, -1, Blocks.SMOOTH_STONE.defaultBlockState());
        fill(l, b, 8, -1, -14, 12, -1, -1, Blocks.GRAY_CONCRETE.defaultBlockState());
        // The mailbox by the walk, and a hedge along each side of the lot.
        set(l, b, 4, 0, -2, Blocks.DARK_OAK_FENCE);set(l, b, 4, 1, -2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.SOUTH));
        for (int z = -1; z >= -20; z--) for (int x : new int[]{-21, 21}) { set(l, b, x, 0, z, leaves(Blocks.OAK_LEAVES));if (z % 3 != 0) set(l, b, x, 1, z, leaves(Blocks.OAK_LEAVES)); }
        // The street lamp at the corner of the lot.
        for (int y = 0; y <= 4; y++) set(l, b, -7, y, -2, Blocks.DARK_OAK_FENCE);
        set(l, b, -7, 5, -2, Blocks.DARK_OAK_SLAB);set(l, b, -7, 4, -3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));set(l, b, -7, 5, -3, Blocks.DARK_OAK_FENCE);
    }

    private static void house(ServerLevel l, BlockPos b) {
        BlockState siding = LiteraryRegistry.SIDING.get().defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        BlockState plaster = VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.WATCHROOM_FLOWERS, Direction.NORTH), boards = VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.WATCHROOM_BOARDS, Direction.NORTH);
        // Shell: siding outside (x 14, z -7 and -31), plaster within (x 13, z -8 and -30); oak floors, a kitchen of tile, a
        // flat ceiling under the roof.
        fill(l, b, -14, 0, -31, 14, 3, -7, siding);
        fill(l, b, -13, 0, -30, 13, 3, -8, plaster);
        fill(l, b, -14, -1, -31, 14, -1, -7, boards);
        fill(l, b, 3, -1, -18, 12, -1, -9, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        fill(l, b, -14, 4, -31, 14, 4, -7, Blocks.WHITE_CONCRETE.defaultBlockState());
        // Rooms: entry and living room (west, open to each other), kitchen (east, open to the entry), the hall north to the
        // back door, Tommy's room (north-west), Kate's (east) and Elizabeth's (north-east).
        fill(l, b, -12, 0, -18, -3, 3, -9, air);
        fill(l, b, -2, 0, -11, 2, 3, -9, air);
        fill(l, b, 3, 0, -18, 12, 3, -9, air);
        fill(l, b, -1, 0, -29, 1, 3, -12, air);
        fill(l, b, -12, 0, -29, -4, 3, -21, air);
        fill(l, b, 4, 0, -23, 12, 3, -21, air);
        fill(l, b, 4, 0, -29, 12, 3, -26, air);
        // Tommy's walls are blue and Kate's are the colour she chose, on their own side of each wall.
        BlockState blue = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(), pink = Blocks.PINK_TERRACOTTA.defaultBlockState();
        fill(l, b, -13, 0, -29, -13, 3, -21, blue);fill(l, b, -3, 0, -29, -3, 3, -21, blue);fill(l, b, -12, 0, -20, -4, 3, -20, blue);fill(l, b, -12, 0, -30, -4, 3, -30, blue);
        fill(l, b, 13, 0, -23, 13, 3, -21, pink);fill(l, b, 3, 0, -23, 3, 3, -21, pink);fill(l, b, 4, 0, -20, 12, 3, -20, pink);fill(l, b, 4, 0, -24, 12, 3, -24, pink);
        // Carpet in the bedrooms.
        fill(l, b, -12, -1, -29, -4, -1, -21, Blocks.BLUE_WOOL.defaultBlockState());
        fill(l, b, 4, -1, -29, 12, -1, -26, Blocks.LIGHT_GRAY_WOOL.defaultBlockState());
        // Doors through the double walls: the front door, Tommy's, Kate's and Elizabeth's (open), and the back door.
        doorway(l, b, 0, -8);door(l, b, 0, 0, -7, Direction.SOUTH, Blocks.OAK_DOOR, false);
        doorway(l, b, -3, -24);door(l, b, -2, 0, -24, Direction.EAST, Blocks.BIRCH_DOOR, true);
        doorway(l, b, 3, -22);door(l, b, 2, 0, -22, Direction.WEST, Blocks.BIRCH_DOOR, true);
        doorway(l, b, 3, -27);door(l, b, 2, 0, -27, Direction.WEST, Blocks.BIRCH_DOOR, true);
        doorway(l, b, 0, -30);door(l, b, 0, 0, -31, Direction.NORTH, Blocks.SPRUCE_DOOR, false);
        // Windows two panes high in the siding, set back behind the plaster.
        for (int x : new int[]{-9, -8, 8, 9}) window(l, b, x, -7, x, -8, Direction.Axis.X);
        for (int x : new int[]{7, 8}) window(l, b, x, -31, x, -30, Direction.Axis.X);
        for (int z : new int[]{-16, -17, -27, -28}) window(l, b, -14, z, -13, z, Direction.Axis.Z);
        for (int z : new int[]{-14, -15, -21, -22, -27, -28}) window(l, b, 14, z, 13, z, Direction.Axis.Z);
        roof(l, b);
        light(l, b, 1, 2, -6, 12);

        // ---- living room: the couch facing the television, the rug, the camera on its tripod facing the couch
        rug(l, b, -10, -6, -15, -11);
        for (int z = -14; z <= -12; z++) furniture(l, b, -6, 0, z, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.WEST);
        furniture(l, b, -12, 0, -13, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.EAST);
        LiteraryRooms.prop(l, b, new BlockPos(-12, 1, -13), LiteraryPropBlock.Kind.TELEVISION, Direction.EAST);
        set(l, b, -12, 0, -12, VignetteDetailBlock.state(VignetteDetailBlock.Kind.CASSETTE, Direction.EAST));
        furniture(l, b, -9, 0, -13, HouseholdFurnitureBlock.Kind.FOOTSTOOL, Direction.EAST);
        furniture(l, b, -11, 0, -18, HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR, Direction.SOUTH);
        furniture(l, b, -12, 0, -18, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.SOUTH);set(l, b, -12, 1, -18, SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP, Direction.SOUTH));
        LiteraryRooms.prop(l, b, CAMERA, LiteraryPropBlock.Kind.CAMERA, Direction.WEST);
        fit(l, b, -3, 0, -17, VignetteInteriorBlock.Kind.WATCHROOM_TRIPOD_CASE, Direction.WEST);fit(l, b, -3, 1, -17, VignetteInteriorBlock.Kind.WATCHROOM_TAPE, Direction.WEST);
        set(l, b, -12, 2, -11, SceneDetailBlock.state(SceneDetailBlock.Kind.FRAME, Direction.EAST));
        set(l, b, -6, 2, -9, SceneDetailBlock.state(SceneDetailBlock.Kind.CLOCK, Direction.NORTH));
        // 0.4.241 (the owner: "add more family details to the house... the same lived-in nature as the barret home", their own
        // pieces, tools/devils_rock_household.py): the coffee table with the papers and the tissues, the basket of laundry by her
        // chair, the plant nobody waters, the three of them at a lake one summer, and his school photograph.
        dr(l, b, -7, 0, -13, VignetteDetailBlock.Kind.SANDERSON_COFFEE_TABLE, Direction.EAST);
        dr(l, b, -9, 0, -18, VignetteDetailBlock.Kind.SANDERSON_LAUNDRY, Direction.SOUTH);
        dr(l, b, -12, 0, -9, VignetteDetailBlock.Kind.SANDERSON_PLANT, Direction.NORTH);
        dr(l, b, -11, 2, -9, VignetteDetailBlock.Kind.SANDERSON_PHOTO, Direction.NORTH);
        dr(l, b, -12, 2, -15, VignetteDetailBlock.Kind.TOMMY_PHOTO, Direction.EAST);
        // ---- the entry: shoes and coats by the door
        set(l, b, -2, 0, -9, SceneDetailBlock.state(SceneDetailBlock.Kind.SHOES, Direction.EAST));
        set(l, b, 2, 2, -9, SceneDetailBlock.state(SceneDetailBlock.Kind.COAT, Direction.NORTH));
        // His jacket still on its peg beside theirs; the mail nobody has opened.
        dr(l, b, -2, 1, -11, VignetteDetailBlock.Kind.SANDERSON_COATS, Direction.SOUTH);
        dr(l, b, 2, 0, -11, VignetteDetailBlock.Kind.SANDERSON_MAIL, Direction.SOUTH);
        // ---- kitchen: counters, stove and sink along the east wall, the fridge with his poster on it, the table with the log
        // 0.4.202: counters a block high, cupboards under them; the dish rack stands on the one by the window.
        for (int z = -16; z <= -10; z++) set(l, b, 12, 0, z, Blocks.STRIPPED_OAK_WOOD);
        set(l, b, 12, 0, -13, Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST));set(l, b, 12, 1, -13, Blocks.AIR);
        for (int z : new int[]{-15, -14, -12, -10}) fit(l, b, 12, 0, z, VignetteInteriorBlock.Kind.WATCHROOM_CUPBOARD, Direction.WEST);
        fit(l, b, 12, 0, -11, VignetteInteriorBlock.Kind.TRAILER_SINK, Direction.WEST);set(l, b, 12, 1, -11, Blocks.AIR);
        set(l, b, 12, 1, -16, SceneDetailBlock.state(SceneDetailBlock.Kind.DISH_RACK, Direction.WEST));
        // The fridge, his flyer under a magnet on the freezer door; the neighbours' casseroles on the counter; the calendar with
        // the day circled; her board of flyers and the park map with its pins.
        dr(l, b, 12, 0, -17, VignetteDetailBlock.Kind.SANDERSON_FRIDGE_LOW, Direction.WEST);
        dr(l, b, 12, 1, -17, VignetteDetailBlock.Kind.SANDERSON_FRIDGE_HIGH, Direction.WEST);
        dr(l, b, 12, 1, -15, VignetteDetailBlock.Kind.SANDERSON_CASSEROLES, Direction.WEST);
        dr(l, b, 12, 1, -12, VignetteDetailBlock.Kind.SANDERSON_CASSEROLES, Direction.SOUTH);
        dr(l, b, 6, 1, -9, VignetteDetailBlock.Kind.SANDERSON_CALENDAR, Direction.NORTH);
        dr(l, b, 11, 1, -9, VignetteDetailBlock.Kind.SANDERSON_SEARCH_BOARD, Direction.NORTH);
        furniture(l, b, 7, 0, -14, HouseholdFurnitureBlock.Kind.FORMICA_TABLE, Direction.SOUTH);
        furniture(l, b, 7, 0, -13, HouseholdFurnitureBlock.Kind.KITCHEN_STOOL, Direction.NORTH);furniture(l, b, 7, 0, -15, HouseholdFurnitureBlock.Kind.KITCHEN_STOOL, Direction.SOUTH);
        furniture(l, b, 6, 0, -14, HouseholdFurnitureBlock.Kind.KITCHEN_STOOL, Direction.EAST);
        // ---- the hall: the thread she strung across it
        for (int x = -1; x <= 1; x++) set(l, b, x, 0, -17, Blocks.TRIPWIRE);
        // ---- Tommy's room: the bed, the bureau with his old pennies, the desk with his drawings, the shelf, the closet door
        NovelRooms.bed(l, b.offset(-11, 0, -28), Blocks.BLUE_BED, Direction.NORTH);
        furniture(l, b, -5, 0, -29, HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS, Direction.SOUTH);
        set(l, b, BUREAU_PENNIES, VignetteDetailBlock.state(VignetteDetailBlock.Kind.PENNIES, Direction.SOUTH));
        furniture(l, b, -12, 0, -22, HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.EAST);set(l, b, -12, 1, -22, SceneDetailBlock.state(SceneDetailBlock.Kind.INK_PAPERS, Direction.EAST));
        furniture(l, b, -11, 0, -22, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.WEST);
        for (int y = 0; y <= 1; y++) for (int x = -8; x <= -7; x++) set(l, b, x, y, -29, Blocks.BOOKSHELF);
        furniture(l, b, -4, 0, -21, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.WEST);set(l, b, -4, 1, -21, SceneDetailBlock.state(SceneDetailBlock.Kind.BOOKS, Direction.WEST));
        set(l, b, -4, 2, -26, SceneDetailBlock.state(SceneDetailBlock.Kind.FRAME, Direction.WEST));
        // His poster, his skateboard against the wall under it, the clothes he left on the floor.
        dr(l, b, -12, 1, -25, VignetteDetailBlock.Kind.SANDERSON_POSTER, Direction.EAST);
        dr(l, b, -12, 0, -24, VignetteDetailBlock.Kind.SANDERSON_SKATEBOARD, Direction.EAST);
        dr(l, b, -9, 0, -23, VignetteDetailBlock.Kind.SANDERSON_CLOTHES, Direction.NORTH);
        // ---- Kate's room: her bed, desk and books
        NovelRooms.bed(l, b.offset(11, 0, -22), Blocks.PURPLE_BED, Direction.EAST);
        furniture(l, b, 5, 0, -21, HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.NORTH);
        // She paints: her jars and palette on the desk, her flowers taped up on the wall.
        dr(l, b, 5, 1, -21, VignetteDetailBlock.Kind.SANDERSON_PAINT_JARS, Direction.NORTH);
        dr(l, b, 8, 1, -23, VignetteDetailBlock.Kind.SANDERSON_PAINTINGS, Direction.SOUTH);
        set(l, b, 12, 0, -23, VignetteDetailBlock.state(VignetteDetailBlock.Kind.BEAR, Direction.WEST));
        // ---- Elizabeth's room: the bed, and the plush chair and the little end table with the empty space between them
        NovelRooms.bed(l, b.offset(11, 0, -27), Blocks.WHITE_BED, Direction.EAST);
        furniture(l, b, 12, 0, -29, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.WEST);set(l, b, 12, 1, -29, SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP, Direction.WEST));
        fit(l, b, 9, 0, -29, VignetteInteriorBlock.Kind.WATCHROOM_DRAWER, Direction.SOUTH);
        dr(l, b, 9, 1, -29, VignetteDetailBlock.Kind.SIBLINGS_PHOTO, Direction.SOUTH);
        dr(l, b, 10, 0, -26, VignetteDetailBlock.Kind.SANDERSON_LAUNDRY, Direction.SOUTH);
        furniture(l, b, 4, 0, -29, HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, Direction.SOUTH);
        furniture(l, b, 6, 0, -29, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.SOUTH);set(l, b, 6, 1, -29, SceneDetailBlock.state(SceneDetailBlock.Kind.VASE, Direction.SOUTH));
    }
    private static void dr(ServerLevel l, BlockPos b, int x, int y, int z, VignetteDetailBlock.Kind k, Direction f) { set(l, b, x, y, z, VignetteDetailBlock.state(k, f)); }
    private static void doorway(ServerLevel l, BlockPos b, int x, int z) { set(l, b, x, 0, z, Blocks.AIR);set(l, b, x, 1, z, Blocks.AIR); }
    private static void window(ServerLevel l, BlockPos b, int x, int z, int ix, int iz, Direction.Axis along) {
        for (int y = 1; y <= 2; y++) { set(l, b, x, y, z, pane(along));set(l, b, ix, y, iz, Blocks.AIR); }
        set(l, b, ix, 0, iz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
    }

    /** A low slab gable, ridge running front to back; the gables filled in with siding. */
    private static void roof(ServerLevel l, BlockPos b) {
        BlockState siding = LiteraryRegistry.SIDING.get().defaultBlockState();
        for (int x = -16; x <= 16; x++) {
            int half = 16 - Math.abs(x), y = 4 + half / 2;
            var slab = Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, half % 2 == 0 ? SlabType.BOTTOM : SlabType.TOP);
            for (int z = -32; z <= -6; z++) set(l, b, x, y, z, slab);
            if (Math.abs(x) <= 14) for (int gy = 5; gy < y; gy++) { set(l, b, x, gy, -7, siding);set(l, b, x, gy, -31, siding); }
        }
    }

    private static void yard(ServerLevel l, BlockPos b) {
        // The deck off the back door, a picnic table, the fence round the yard and its gate onto the park.
        fill(l, b, -4, -1, -34, 4, -1, -32, Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int x = -4; x <= 4; x++) if (x != 0) set(l, b, x, 0, -34, Blocks.SPRUCE_FENCE);
        for (int x = -20; x <= 20; x++) set(l, b, x, 0, -45, x == 0 ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH) : Blocks.SPRUCE_FENCE.defaultBlockState());
        for (int z = -44; z >= -21; z--) for (int x : new int[]{-20, 20}) set(l, b, x, 0, z, Blocks.SPRUCE_FENCE);
        for (int x = 15; x <= 20; x++) { set(l, b, x, 0, -21, Blocks.SPRUCE_FENCE);set(l, b, -x, 0, -21, Blocks.SPRUCE_FENCE); }
        furniture(l, b, -9, 0, -38, HouseholdFurnitureBlock.Kind.FORMICA_TABLE, Direction.SOUTH);
        // A garden shed in the far corner.
        fill(l, b, 13, 0, -43, 17, 3, -39, Blocks.SPRUCE_PLANKS.defaultBlockState());fill(l, b, 14, 0, -42, 16, 2, -40, Blocks.AIR.defaultBlockState());
        fill(l, b, 12, 4, -44, 18, 4, -38, Blocks.SPRUCE_SLAB.defaultBlockState());door(l, b, 15, 0, -39, Direction.SOUTH, Blocks.SPRUCE_DOOR, false);
        // The trail out of the gate, through the split and on round the loop; worn into the ground.
        for (int z = -46; z >= -92; z--) for (int dx = -1; dx <= 1; dx++) path(l, b, dx + trailX(z), z);
        for (int i = 0; i < LOOP.length; i++) { var a = LOOP[i];var c = LOOP[(i + 1) % LOOP.length];int n = (int)Math.ceil(a.distanceTo(c));
            for (int s = 0; s <= n; s++) { double t = s / (double)n;path(l, b, (int)Math.floor(a.x + (c.x - a.x) * t), (int)Math.floor(a.z + (c.z - a.z) * t)); } }
        // His poster on the first tree past the gate.
        set(l, b, 3, 0, -48, Blocks.OAK_LOG);set(l, b, 3, 1, -48, Blocks.OAK_LOG);set(l, b, 3, 1, -47, VignetteDetailBlock.state(VignetteDetailBlock.Kind.MISSING_NOTICE, Direction.SOUTH));
    }
    static int trailX(int z) { return z > -58 ? 0 : z > -64 ? -1 : 0; }
    private static void path(ServerLevel l, BlockPos b, int x, int z) {
        if (Math.abs(x) > 33 || z > -46 || z < -123 || inPond(x + .5, z + .5)) return;
        set(l, b, x, -1, z, hash(x, z) % 4 == 0 ? Blocks.COARSE_DIRT : Blocks.DIRT_PATH);
        set(l, b, x, 0, z, Blocks.AIR);
    }

    private static void woods(ServerLevel l, BlockPos b) {
        // Old stone walls, mossy and broken, along the yard's far side and the pond's near bank, gapped for the trail.
        for (int x = -33; x <= 33; x++) {
            if (Math.abs(x) > 2 && hash(x, 7) % 5 != 0) set(l, b, x, 0, -49, hash(x, 3) % 3 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE);
            if (Math.abs(x) > 6 && Math.abs(x) < 30 && hash(x, 11) % 4 != 0 && !inPond(x + .5, -91.5)) set(l, b, x, 0, -91, hash(x, 5) % 2 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE);
        }
        for (int x = -33; x <= 33; x++) for (int z = -50; z >= -123; z--) {
            if (clearing(x, z)) continue;
            int h = hash(x, z);
            if (h % 23 == 0) tree(l, b, x, z, h);
            else if (h % 7 == 1) set(l, b, x, 0, z, h % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS);
            else if (h % 31 == 2) set(l, b, x, 0, z, leaves(Blocks.OAK_LEAVES));
        }
    }
    /** Where nothing grows: the trail, the loop, round the rock, and the pond with its banks. */
    private static boolean clearing(int x, int z) {
        if (Math.abs(x - trailX(z)) <= 2 && z >= -92) return true;
        if (Math.abs(x) <= 9 && z <= -63 && z >= -81) return true;
        if (inPondMargin(x + .5, z + .5, 2.5)) return true;
        var at = new Vec3(x + .5, 0, z + .5);
        for (int i = 0; i < LOOP.length; i++) if (segment(at, LOOP[i], LOOP[(i + 1) % LOOP.length]) < 3) return true;
        for (int i = 0; i + 1 < TO_ISLAND.length; i++) if (segment(at, TO_ISLAND[i], TO_ISLAND[i + 1]) < 2.5) return true;
        return Math.abs(x) >= 33 || z <= -122;
    }
    static boolean inPondMargin(double x, double z, double margin) { return sq((x - POND_X) / (POND_RX + margin)) + sq((z - POND_Z) / (POND_RZ + margin)) <= 1; }
    static double segment(Vec3 p, Vec3 a, Vec3 c) {
        double dx = c.x - a.x, dz = c.z - a.z, len = dx * dx + dz * dz;
        double t = len == 0 ? 0 : Math.max(0, Math.min(1, ((p.x - a.x) * dx + (p.z - a.z) * dz) / len));
        double ex = a.x + dx * t - p.x, ez = a.z + dz * t - p.z;return Math.sqrt(ex * ex + ez * ez);
    }
    private static void tree(ServerLevel l, BlockPos b, int x, int z, int h) {
        int kind = (h >> 4) % 3, height = 5 + (h >> 6) % 3;
        Block log = kind == 0 ? Blocks.OAK_LOG : kind == 1 ? Blocks.BIRCH_LOG : Blocks.SPRUCE_LOG;
        Block leaf = kind == 0 ? Blocks.OAK_LEAVES : kind == 1 ? Blocks.BIRCH_LEAVES : Blocks.SPRUCE_LEAVES;
        for (int y = 0; y < height; y++) set(l, b, x, y, z, log);
        for (int y = height - 3; y <= height; y++) {
            int r = kind == 2 ? Math.max(0, (height - y + 1) / 2) : y == height ? 1 : 2;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                if (Math.abs(dx) == r && Math.abs(dz) == r && r > 1) continue;
                if (dx == 0 && dz == 0 && y < height) continue;
                int xx = x + dx, zz = z + dz;if (Math.abs(xx) > 33 || zz > -46 || zz < -123) continue;
                set(l, b, xx, y, zz, leaves(leaf));
            }
        }
    }

    /** The split boulder: two halves with the trail through the gap, a ramp of stones up the west side, the footprint on top. */
    private static void rock(ServerLevel l, BlockPos b) {
        for (int x = -6; x <= 6; x++) for (int z = -77; z <= -66; z++) for (int y = 0; y <= 6; y++) {
            if (x == 0) continue;
            double nx = x / 6.5, nz = (z + 71.5) / 6.0, ny = (y + .5) / 6.8;
            if (nx * nx + nz * nz + ny * ny > 1) continue;
            int h = hash(x * 3 + y, z * 5 - y);
            set(l, b, x, y, z, h % 7 == 0 ? Blocks.TUFF : h % 5 == 0 ? Blocks.ANDESITE : h % 11 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.STONE);
        }
        // The split itself stays open to the sky; its floor is bare.
        for (int z = -78; z <= -65; z++) { set(l, b, 0, -1, z, Blocks.COARSE_DIRT);for (int y = 0; y <= 7; y++) set(l, b, 0, y, z, Blocks.AIR); }
        // A ramp of stones up the west half.
        int[][] steps = {{-7, 0, -71}, {-7, 0, -72}, {-6, 1, -72}, {-6, 1, -71}, {-5, 2, -72}, {-5, 3, -71}};
        for (int[] s : steps) for (int y = 0; y <= s[1]; y++) set(l, b, s[0], y, s[2], Blocks.STONE);
        // The devil's footprint, worn into the top of the east half.
        for (int z : new int[]{-72, -73}) { int top = rockTop(3, z);set(l, b, 3, top, z, Blocks.AIR);set(l, b, 3, top - 1, z, Blocks.TUFF); }
        // The tree the boys sat against, and the trail camera strapped to another, facing the rock.
        tree(l, b, 8, -69, 0);
        for (int y = 0; y <= 4; y++) set(l, b, -8, y, -66, Blocks.OAK_LOG);
        LiteraryRooms.prop(l, b, TRAIL_CAMERA, LiteraryPropBlock.Kind.CAMERA, Direction.EAST);
    }

    /** The highest cell of the boulder over a column (the same ellipsoid the build fills). */
    static int rockTop(int x, int z) {
        double nx = x / 6.5, nz = (z + 71.5) / 6.0;int top = -1;
        for (int y = 0; y <= 6; y++) { double ny = (y + .5) / 6.8;if (nx * nx + nz * nz + ny * ny <= 1) top = y; }
        return top;
    }

    private static void pond(ServerLevel l, BlockPos b) {
        for (int x = -28; x <= 28; x++) for (int z = -92; z >= -122; z--) {
            if (!inPond(x + .5, z + .5) || onIsland(x + .5, z + .5)) continue;
            double edge = sq((x + .5 - POND_X) / POND_RX) + sq((z + .5 - POND_Z) / POND_RZ);
            int depth = edge > .85 ? 1 : edge > .6 ? 2 : edge > .3 ? 3 : 4;
            for (int y = -4; y <= 0; y++) set(l, b, x, y, z, Blocks.AIR);
            for (int y = -depth; y <= -1; y++) set(l, b, x, y, z, Blocks.WATER);
            set(l, b, x, -depth - 1, z, hash(x, z) % 3 == 0 ? Blocks.CLAY : Blocks.MUD);
            int h = hash(z, x);
            if (depth >= 2 && h % 9 == 0) set(l, b, x, -depth, z, Blocks.SEAGRASS);
            if (h % 13 == 0) set(l, b, x, 0, z, Blocks.LILY_PAD);
        }
        // Mud and reeds along the banks.
        for (int x = -30; x <= 30; x++) for (int z = -90; z >= -122; z--) {
            if (inPond(x + .5, z + .5) || !inPondMargin(x + .5, z + .5, 1.5)) continue;
            set(l, b, x, -1, z, hash(x, z) % 2 == 0 ? Blocks.MUD : Blocks.GRASS_BLOCK);
            if (hash(z, x) % 3 == 0) set(l, b, x, 0, z, Blocks.TALL_GRASS.defaultBlockState());
        }
    }

    /** The island: a hump of grass, a few saplings, and the two rocks almost pushed together with the gap between them. */
    private static void island(ServerLevel l, BlockPos b) {
        for (int x = 4; x <= 16; x++) for (int z = -104; z >= -115; z--) {
            if (!onIsland(x + .5, z + .5)) continue;
            for (int y = -4; y <= -2; y++) set(l, b, x, y, z, Blocks.DIRT);
            set(l, b, x, -1, z, Blocks.GRASS_BLOCK);set(l, b, x, 0, z, Blocks.AIR);
            if (hash(x, z) % 9 == 0 && Math.abs(x - 10) > 2) set(l, b, x, 0, z, Blocks.SHORT_GRASS);
        }
        for (int[] r : new int[][]{{8, 9}, {11, 12}}) for (int x = r[0]; x <= r[1]; x++) for (int z = -112; z <= -110; z++) for (int y = 0; y <= (z == -111 ? 2 : 1); y++)
            set(l, b, x, y, z, hash(x + y, z) % 4 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.STONE);
        for (int y = 0; y <= 3; y++) { set(l, b, 10, y, -110, Blocks.AIR);set(l, b, 10, y, -111, Blocks.AIR); }
        set(l, b, ISLAND_PENNIES, VignetteDetailBlock.state(VignetteDetailBlock.Kind.PENNIES, Direction.SOUTH));
        for (int[] t : new int[][]{{6, -107}, {14, -112}}) { for (int y = 0; y <= 2; y++) set(l, b, t[0], y, t[1], Blocks.BIRCH_LOG);set(l, b, t[0], 3, t[1], leaves(Blocks.BIRCH_LEAVES)); }
    }

    /** Lamps in the house; and, in the woods, faint light where the moon gets through, enough to see a figure on the trail. */
    private static void lights(ServerLevel l, BlockPos b) {
        for (int[] at : new int[][]{{-8, 3, -14}, {7, 3, -14}, {0, 3, -14}, {0, 3, -24}, {-8, 3, -24}, {8, 3, -22}, {8, 3, -27}, {0, 2, -10}}) light(l, b, at[0], at[1], at[2], at[0] == 0 ? 9 : 7);
        for (int i = 0; i < LOOP.length; i++) { var a = LOOP[i];light(l, b, (int)Math.floor(a.x), 4, (int)Math.floor(a.z), 6); }
        for (int z = -50; z >= -88; z -= 6) light(l, b, trailX(z), 4, z, 6);
        for (var at : TO_ISLAND) light(l, b, (int)Math.floor(at.x), 3, (int)Math.floor(at.z), 6);
        light(l, b, 0, 8, -72, 7);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static int hash(int x, int z) { int h = x * 73856093 ^ z * 19349663;h ^= h >>> 13;h *= 0x5bd1e995;h ^= h >>> 15;return h & 0x7fffffff; }
    private static BlockState leaves(Block block) { return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true); }
    private static BlockState pane(Direction.Axis along) {
        var s = Blocks.GLASS_PANE.defaultBlockState();
        return along == Direction.Axis.X ? s.setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true) : s.setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true);
    }
    private static void rug(ServerLevel l, BlockPos b, int x0, int x1, int z0, int z1) {
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
            String ns = z == z0 ? "NORTH" : z == z1 ? "SOUTH" : "", ew = x == x0 ? "WEST" : x == x1 ? "EAST" : "";
            var rim = RugFloorBlock.Rim.valueOf(ns.isEmpty() ? ew.isEmpty() ? "CENTER" : ew : ew.isEmpty() ? ns : ns + "_" + ew);
            set(l, b, x, -1, z, HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE, RugFloorBlock.Tone.SLATE).setValue(RugFloorBlock.RIM, rim));
        }
    }
    private static void light(ServerLevel l, BlockPos b, int x, int y, int z, int level) { if (BuildBlocks.state(l, b.offset(x, y, z)).isAir()) set(l, b, x, y, z, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, level)); }
    private static void door(ServerLevel l, BlockPos b, int x, int y, int z, Direction facing, Block block, boolean open) { NovelRooms.door(l, b.offset(x, y, z), facing, block, open); }
    private static void fit(ServerLevel l, BlockPos b, int x, int y, int z, VignetteInteriorBlock.Kind k, Direction f) {
        var handling = VignetteHandlingBlock.replacement(k);
        set(l, b, x, y, z, handling == null ? VignetteInteriorBlock.of(k, f) : VignetteHandlingBlock.of(handling, f));
    }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing) { set(l, b, x, y, z, HouseholdFurnitureBlock.state(kind, facing)); }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, Block block) { set(l, b, x, y, z, block.defaultBlockState()); }
    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void set(ServerLevel l, BlockPos b, BlockPos rel, BlockState s) { BuildBlocks.set(l, b.offset(rel), s, F); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) set(l, b, x, y, z, s);
    }
    static List<BlockPos> pages() { return List.of(PAGES); }
}
