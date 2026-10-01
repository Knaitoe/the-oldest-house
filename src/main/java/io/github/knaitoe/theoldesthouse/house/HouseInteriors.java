package io.github.knaitoe.theoldesthouse.house;

import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.log;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.slab;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.stairs;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.stairsTop;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.trapdoor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Furnishing for The Oldest House. Every room is dressed as somewhere a
 * particular person lived rather than as a recoloured copy of its neighbours.
 */
final class HouseInteriors {
    private static final BlockState BRICK = Blocks.BRICKS.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * Original paintings for the manor. Their dimensions deliberately match
     * the vanilla variants they replace so the subtle-change system can still
     * trade equal-sized paintings between walls without changing collision.
     */
    static final List<PaintingSpec> PAINTINGS = List.of(
            // Great room: family residue, the manor itself, and the lake.
            new PaintingSpec(3, 4, 8, Direction.EAST, HousePaintings.FAMILY_TABLE),
            new PaintingSpec(7, 3, 14, Direction.NORTH, HousePaintings.NAVIDSON_MANOR),
            new PaintingSpec(12, 3, 11, Direction.WEST, HousePaintings.LAKE_EVENING),
            // Hall portraits, well off the axis.
            new PaintingSpec(14, 2, 15, Direction.EAST, HousePaintings.WILL_PORTRAIT),
            new PaintingSpec(16, 2, 8, Direction.WEST, HousePaintings.KAREN_PORTRAIT),
            // Study: the scholar over the fire.
            new PaintingSpec(7, 4, 24, Direction.NORTH, HousePaintings.SCHOLAR_AT_DESK),
            // Principal bedroom: formal, but not necessarily reassuring.
            new PaintingSpec(10, 8, 8, Direction.NORTH, HousePaintings.ARCHITECTURAL_STUDY),
            new PaintingSpec(12, 8, 5, Direction.WEST, HousePaintings.CHILD_PORTRAIT),
            // Literary bedroom.
            new PaintingSpec(4, 9, 14, Direction.NORTH, HousePaintings.PRESSED_FERN),
            // Upper hall.
            new PaintingSpec(14, 8, 8, Direction.EAST, HousePaintings.OLD_MAN_PORTRAIT),
            new PaintingSpec(16, 8, 18, Direction.WEST, HousePaintings.UNKNOWN_WOMAN_PORTRAIT),
            // Maker loft.
            new PaintingSpec(18, 8, 9, Direction.EAST, HousePaintings.BRASS_CLOCK),
            // Long gallery.
            new PaintingSpec(4, 8, 16, Direction.SOUTH, HousePaintings.HOUSE_DRAWING),
            new PaintingSpec(9, 8, 16, Direction.SOUTH, HousePaintings.EMPTY_CHAIR),
            new PaintingSpec(6, 8, 25, Direction.NORTH, HousePaintings.WINTER_ROAD),
            new PaintingSpec(10, 8, 25, Direction.NORTH, HousePaintings.THE_YARD)
    );

    private HouseInteriors() {
    }

    static void furnish(HouseCanvas c) {
        greatRoom(c);
        hall(c);
        kitchen(c);
        study(c);
        stairTower(c);
        scullery(c);
        cellar(c);
        principalBedroom(c);
        literaryBedroom(c);
        upperHall(c);
        makerLoft(c);
        longGallery(c);
        boxRoom(c);
        placeDoors(c);
        clearDoorways(c);
        HouseFurnishings.decorateManor(c.level,c.origin);
    }

    // ------------------------------------------------------------------
    // Ground floor
    // ------------------------------------------------------------------

    /**
     * The great room is the house's heart: an inglenook-style hearth with a
     * heavy timber bressumer and mantel, flanking built-ins, beams overhead,
     * a seating group turned to the fire and a deep window seat in the bay.
     */
    private static void greatRoom(HouseCanvas c) {
        // Hearth.
        c.fill(1, 0, 7, 2, 0, 8, BRICK);
        c.fill(3, 0, 6, 3, 0, 9, BRICK);
        c.clearBox(1, 1, 7, 2, 2, 8);
        c.set(1, 1, 7, campfire(true));
        c.set(1, 1, 8, campfire(true));
        c.fill(2, 3, 6, 2, 3, 9, log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        for (int z = 6; z <= 9; z++) {
            c.set(3, 3, z, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.WEST));
        }
        c.set(3, 4, 6, candle(3));
        c.set(3, 4, 9, candle(2));

        // Built-in cupboards and shelves either side of the chimney breast,
        // with small windows above them.
        for (int z : new int[]{2, 3, 4, 5, 10, 11, 12, 13}) {
            c.set(1, 1, z, barrel(Direction.EAST));
            c.set(1, 2, z, BOOKSHELF);
            if (z != 4 && z != 11) {
                c.set(1, 3, z, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            }
        }
        c.set(1, 4, 2, Blocks.POTTED_FERN.defaultBlockState());
        c.set(1, 4, 13, candle(1));

        // Lamp tables flanking the hearth.
        for (int z : new int[]{5, 10}) {
            c.set(3, 1, z, barrel(Direction.UP));
            c.set(3, 2, z, lantern(false));
        }

        // Seating group turned to the fire.
        for (int z = 6; z <= 9; z++) {
            c.set(7, 1, z, stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        }
        c.set(7, 1, 5, trapdoor(Blocks.DARK_OAK_TRAPDOOR, Direction.NORTH, Half.BOTTOM, true));
        c.set(7, 1, 10, trapdoor(Blocks.DARK_OAK_TRAPDOOR, Direction.SOUTH, Half.BOTTOM, true));
        c.set(4, 1, 5, stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
        c.set(4, 1, 10, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.fill(5, 1, 7, 5, 1, 8, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        rug(c, HouseRugs.GREAT_ROOM);
        c.set(7, 4, 8, lantern(true));

        // Deep window seat in the bay, with its own lantern.
        for (int x = 5; x <= 8; x++) {
            c.set(x, 1, -1, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        }
        c.set(6, 5, -1, lantern(true));

        // Sideboard under the long landscape.
        c.set(5, 1, 14, barrel(Direction.NORTH));
        c.set(6, 1, 14, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(7, 1, 14, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(8, 1, 14, barrel(Direction.NORTH));
        c.set(6, 2, 14, candle(3));
        c.set(8, 2, 14, Blocks.POTTED_ALLIUM.defaultBlockState());

        // Card table at the rear.
        table(c, 10, 1, 12);
        c.set(10, 1, 11, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        c.set(10, 1, 13, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        c.set(11, 1, 12, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));

        // Writing desk under the front-right window.
        trestleTable(c, 10, 1, 1, 12, 1, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(10, 2, 1, candle(2));
        c.set(11, 1, 2, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(12, 2, 1, candle(1));

        // Standing lamp in the rear corner.
        c.set(1, 1, 14, Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(1, 2, 14, lantern(false));
    }

    /**
     * The hall stays empty along its centre line: a runner, lanterns and
     * beams draw the eye straight to the far wall.
     */
    private static void hall(HouseCanvas c) {
        int axis = HouseLayout.AXIS_X;
        c.fill(axis, 1, HouseLayout.FRONT_DOOR_Z + 1, axis, 1, HouseLayout.THRESHOLD_Z - 1,
                Blocks.RED_CARPET.defaultBlockState());

        for (int z : new int[]{8, 14, 20, 24}) {
            c.set(axis, 5, z, lantern(true));
        }

        c.set(HouseLayout.HALL_MIN_X, 1, 10, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        c.set(HouseLayout.HALL_MIN_X, 1, 11, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));

        c.set(HouseLayout.HALL_MAX_X, 1, 18, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        c.set(HouseLayout.HALL_MAX_X, 1, 19, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        c.set(HouseLayout.HALL_MAX_X, 2, 18, Blocks.POTTED_FERN.defaultBlockState());
        c.set(HouseLayout.HALL_MAX_X, 2, 19, candle(2));

        c.set(HouseLayout.HALL_MAX_X, 1, 5, Blocks.POTTED_DEAD_BUSH.defaultBlockState());

        // Porch lantern.
        c.set(axis, 5, 2, lantern(true));
    }

    /**
     * A working old-house kitchen: dining table at the window end, a brick
     * range with its hood, a sink under the window, a long counter with wall
     * cupboards, a dresser and a work table.
     */
    private static void kitchen(HouseCanvas c) {
        // Dining end.
        trestleTable(c, 20, 1, 4, 23, 4, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(20, 1, 3, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        c.set(22, 1, 3, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        c.set(21, 1, 5, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        c.set(23, 1, 5, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        c.set(19, 1, 4, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        c.set(24, 1, 4, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        c.set(21, 2, 4, candle(3));
        c.set(22, 2, 4, Blocks.POTTED_RED_TULIP.defaultBlockState());
        c.set(21, 5, 4, lantern(true));

        // Range, hood and flue.
        c.fill(26, 1, 7, 26, 5, 9, BRICK);
        c.set(26, 1, 8, facing(Blocks.SMOKER, Direction.WEST));
        for (int z = 7; z <= 9; z++) {
            c.set(25, 4, z, stairsTop(Blocks.BRICK_STAIRS, Direction.EAST));
        }

        // Sink under the east window.
        c.set(26, 1, 2, barrel(Direction.WEST));
        c.set(26, 1, 3, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        c.set(26, 1, 4, barrel(Direction.WEST));

        // Long counter with wall cupboards along the rear.
        c.set(19, 1, 11, barrel(Direction.NORTH));
        c.set(20, 1, 11, Blocks.CRAFTING_TABLE.defaultBlockState());
        c.set(21, 1, 11, facing(Blocks.FURNACE, Direction.NORTH));
        c.set(22, 1, 11, barrel(Direction.NORTH));
        c.set(23, 1, 11, stairsTop(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        c.set(24, 1, 11, barrel(Direction.NORTH));
        for (int x = 19; x <= 24; x++) {
            c.set(x, 3, 11, trapdoor(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, Half.TOP, true));
        }

        // Dresser against the hall wall.
        for (int z = 8; z <= 10; z++) {
            c.set(18, 1, z, barrel(Direction.EAST));
        }
        c.set(18, 2, 8, candle(2));
        c.set(18, 2, 10, Blocks.POTTED_FERN.defaultBlockState());

        // Work table between range and dresser.
        trestleTable(c, 21, 1, 8, 23, 8, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS);
        c.set(22, 5, 9, lantern(true));
    }

    /**
     * The study is visibly bookish and cluttered: a floor-to-ceiling library
     * wall with a ladder, a map table, lecterns, a writing desk under the
     * window and an armchair by its own small fire.
     */
    private static void study(HouseCanvas c) {
        // Fireplace in the rear stack.
        c.fill(5, 0, 24, 8, 0, 24, BRICK);
        c.clearBox(6, 1, 25, 7, 2, 25);
        c.set(6, 1, 25, campfire(true));
        c.set(7, 1, 25, campfire(true));
        c.fill(5, 3, 25, 8, 3, 25, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int x = 5; x <= 8; x++) {
            c.set(x, 3, 24, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        }
        c.set(5, 4, 24, candle(2));
        c.set(8, 4, 24, candle(3));

        // Library wall with a ladder.
        c.fill(3, 1, 16, 11, 4, 16, BOOKSHELF);
        c.set(5, 2, 16, chiseled(Direction.SOUTH));
        c.set(9, 3, 16, chiseled(Direction.SOUTH));
        for (int y = 1; y <= 4; y++) {
            c.set(7, y, 17, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }

        // Shelving on the side walls between windows and door.
        c.fill(2, 1, 16, 2, 4, 16, BOOKSHELF);
        c.fill(2, 1, 19, 2, 4, 21, BOOKSHELF);
        c.fill(2, 1, 24, 2, 4, 25, BOOKSHELF);
        c.fill(12, 1, 16, 12, 4, 18, BOOKSHELF);
        c.fill(12, 1, 22, 12, 4, 25, BOOKSHELF);
        c.set(12, 2, 23, chiseled(Direction.WEST));

        // Chest of papers and a map table under the first window.
        c.set(2, 1, 17, chest(Direction.EAST));
        c.set(2, 1, 18, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());

        // Writing desk under the second window.
        trestleTable(c, 2, 1, 22, 2, 23, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(3, 1, 22, stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        c.set(2, 2, 22, lantern(false));
        c.set(2, 2, 23, candle(4));

        // Central map table.
        c.fill(6, 1, 19, 7, 1, 20, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        c.set(5, 1, 19, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        c.set(8, 1, 20, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));

        // Lecterns.
        c.set(4, 1, 21, facing(Blocks.LECTERN, Direction.EAST));
        c.set(10, 1, 22, facing(Blocks.LECTERN, Direction.WEST));

        // Armchair by the fire.
        c.set(6, 1, 22, stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
        c.set(8, 1, 22, barrel(Direction.UP));
        c.set(8, 2, 22, candle(2));

        // Clutter.
        c.set(11, 1, 24, Blocks.BREWING_STAND.defaultBlockState());
        c.set(10, 1, 17, chiseled(Direction.WEST));
        c.set(3, 1, 25, barrel(Direction.NORTH));
        c.set(3, 2, 25, Blocks.POTTED_CACTUS.defaultBlockState());

        rug(c, HouseRugs.STUDY);

        c.set(7, 5, 18, lantern(true));
        c.set(4, 5, 23, lantern(true));
    }

    /** Stair tower furnishings; the stair itself is architecture (see HouseShell). */
    private static void stairTower(HouseCanvas c) {
        // Storage built into the base of the upper flight.
        c.fill(22, 1, 15, 23, 2, 15, barrel(Direction.NORTH));

        // Library nook on the half landing: shelves along the back wall, a
        // lamp table and a window seat under the landing window.
        c.fill(18, 4, 19, 21, 6, 19, BOOKSHELF);
        c.set(19, 5, 19, chiseled(Direction.NORTH));
        c.set(22, 4, 19, barrel(Direction.UP));
        c.set(22, 5, 19, lantern(false));
        c.set(23, 4, 19, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        rug(c, 18, 4, 18, 23, 18, Blocks.GREEN_CARPET.defaultBlockState());

        // Pendant down the stair well.
        c.fill(20, 10, 17, 20, 14, 17, Blocks.CHAIN.defaultBlockState());
        c.set(20, 9, 17, lantern(true));

        // Foot of the stair and upper landing.
        c.set(23, 1, 13, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        c.set(20, 5, 13, lantern(true));
        c.set(23, 7, 13, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        c.set(18, 7, 13, Blocks.POTTED_FERN.defaultBlockState());
    }

    private static void scullery(HouseCanvas c) {
        for (int x = 19; x <= 22; x++) {
            c.set(x, 1, 21, barrel(Direction.SOUTH));
            c.set(x, 3, 21, slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
        }
        c.set(23, 1, 21, Blocks.COMPOSTER.defaultBlockState());
        c.set(24, 1, 21, barrel(Direction.SOUTH));
        c.set(25, 1, 21, barrel(Direction.SOUTH));
        c.set(26, 1, 21, facing(Blocks.SMOKER, Direction.WEST));
        c.set(26, 1, 22, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        c.set(26, 1, 23, barrel(Direction.WEST));
        c.set(18, 1, 21, barrel(Direction.SOUTH));
        c.set(18, 1, 24, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        c.set(22, 5, 22, lantern(true));
    }

    private static void cellar(HouseCanvas c) {
        c.set(19, -4, 13, chest(Direction.SOUTH));
        c.set(21, -4, 13, facing(Blocks.FURNACE, Direction.SOUTH));
        c.set(22, -4, 13, facing(Blocks.BLAST_FURNACE, Direction.SOUTH));
        c.set(23, -4, 13, Blocks.SMITHING_TABLE.defaultBlockState());
        c.set(24, -4, 13, facing(Blocks.ANVIL, Direction.EAST));
        c.set(25, -4, 13, Blocks.GRINDSTONE.defaultBlockState()
                .setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.SOUTH));

        for (int z = 15; z <= 19; z++) {
            c.set(18, -4, z, barrel(Direction.EAST));
            c.set(18, -3, z, barrel(Direction.EAST));
        }
        c.set(18, -4, 22, Blocks.CAULDRON.defaultBlockState());
        c.set(26, -4, 17, chest(Direction.WEST));
        c.set(26, -4, 13, Blocks.CRAFTING_TABLE.defaultBlockState());

        c.set(21, -1, 16, lantern(true));
        c.set(21, -1, 22, lantern(true));
        c.set(25, -1, 15, lantern(true));
    }

    // ------------------------------------------------------------------
    // Upper floor
    // ------------------------------------------------------------------

    /**
     * The principal bedroom is the oldest and most formal room: a tester bed,
     * its own fireplace, wardrobe, dressing table and portraits.
     */
    private static void principalBedroom(HouseCanvas c) {
        // Unlit bedroom fireplace in the great chimney.
        c.fill(3, 6, 6, 3, 6, 9, BRICK);
        c.clearBox(1, 7, 7, 2, 8, 8);
        c.set(1, 7, 7, campfire(false));
        c.set(1, 7, 8, campfire(false));
        c.fill(2, 9, 6, 2, 9, 9, log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        for (int z = 6; z <= 9; z++) {
            c.set(3, 9, z, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.WEST));
        }
        c.set(3, 10, 6, candle(2));
        c.set(3, 10, 9, candle(2));

        // Tester bed: a double bed with foot posts and a canopy.
        bed(c, 6, 7, 1, Direction.NORTH, Blocks.RED_BED);
        bed(c, 7, 7, 1, Direction.NORTH, Blocks.RED_BED);
        c.fill(5, 7, 2, 5, 9, 2, Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.fill(8, 7, 2, 8, 9, 2, Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.fill(5, 10, 0, 8, 10, 2, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        c.set(5, 7, 0, barrel(Direction.UP));
        c.set(5, 8, 0, candle(3));
        c.set(8, 7, 0, barrel(Direction.UP));
        c.set(8, 8, 0, lantern(false));

        // Wardrobe.
        c.fill(12, 7, 1, 12, 8, 2, barrel(Direction.WEST));
        c.fill(12, 9, 1, 12, 9, 2, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));

        // Dressing table under the front window.
        trestleTable(c, 9, 7, 0, 11, 0, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(10, 7, 1, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(11, 8, 0, Blocks.POTTED_ALLIUM.defaultBlockState());

        // Armchair at the fire.
        c.set(5, 7, 7, stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        c.set(5, 7, 8, barrel(Direction.UP));
        c.set(5, 8, 8, candle(1));

        c.set(12, 7, 7, chest(Direction.WEST));
        rug(c, HouseRugs.PRINCIPAL_BEDROOM);
        c.set(7, 10, 5, lantern(true));
    }

    /** Books everywhere: shelves along the partition, a desk, a lectern and a reading chair. */
    private static void literaryBedroom(HouseCanvas c) {
        bed(c, 2, 7, 14, Direction.WEST, Blocks.BLUE_BED);
        c.set(1, 7, 13, chiseled(Direction.EAST));
        c.set(1, 8, 13, candle(2));

        c.fill(3, 7, 10, 8, 9, 10, BOOKSHELF);
        c.set(9, 7, 10, chiseled(Direction.SOUTH));
        c.set(10, 7, 10, chiseled(Direction.SOUTH));
        c.fill(9, 8, 10, 10, 9, 10, BOOKSHELF);
        c.set(5, 8, 10, chiseled(Direction.SOUTH));

        trestleTable(c, 5, 7, 14, 7, 14, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(6, 7, 13, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        c.set(7, 8, 14, candle(3));
        c.set(9, 7, 14, facing(Blocks.LECTERN, Direction.NORTH));

        c.set(11, 7, 11, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        c.set(11, 7, 14, Blocks.SPRUCE_FENCE.defaultBlockState());
        c.set(11, 8, 14, lantern(false));

        rug(c, HouseRugs.LITERARY_BEDROOM);
        c.set(6, 10, 12, lantern(true));
    }

    private static void upperHall(HouseCanvas c) {
        int axis = HouseLayout.AXIS_X;
        for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
            c.set(x, 7, 1, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        }
        for (int z = 2; z <= 25; z++) {
            c.setIfAir(axis, 7, z, Blocks.RED_CARPET.defaultBlockState());
        }
        for (int z : new int[]{5, 11, 17, 23}) {
            c.set(axis, 9, z, lantern(true));
        }

        c.set(HouseLayout.HALL_MAX_X, 7, 9, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
        c.set(HouseLayout.HALL_MAX_X, 7, 10, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST));
    }

    /** Music and making: jukebox and note blocks, loom, fletching and a workbench under the eaves. */
    private static void makerLoft(HouseCanvas c) {
        for (int z : new int[]{4, 8}) {
            c.fill(20, 11, z, 24, 11, z, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            c.set(22, 10, z, lantern(true));
        }

        bed(c, 24, 7, 10, Direction.EAST, Blocks.CYAN_BED);

        c.set(19, 7, 10, Blocks.JUKEBOX.defaultBlockState());
        c.set(20, 7, 10, Blocks.NOTE_BLOCK.defaultBlockState());
        c.set(21, 7, 10, Blocks.NOTE_BLOCK.defaultBlockState());
        c.set(20, 7, 9, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));

        c.set(19, 7, 2, Blocks.CRAFTING_TABLE.defaultBlockState());
        c.set(20, 7, 2, chest(Direction.SOUTH));
        c.set(24, 7, 2, Blocks.FLETCHING_TABLE.defaultBlockState());
        c.set(25, 7, 2, facing(Blocks.LOOM, Direction.SOUTH));
        c.set(26, 7, 5, Blocks.GRINDSTONE.defaultBlockState()
                .setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.WEST));

        rug(c, 21, 7, 5, 23, 7, Blocks.GREEN_CARPET.defaultBlockState());
    }

    /** A Tudor long gallery under the open cross-gable, lined with pictures. */
    private static void longGallery(HouseCanvas c) {
        for (int x : new int[]{4, 8}) {
            c.fill(x, 11, 17, x, 11, 24, log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        }
        c.set(4, 10, 20, lantern(true));
        c.set(8, 10, 21, lantern(true));

        for (int z = 19; z <= 22; z++) {
            c.set(2, 7, z, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        }

        trestleTable(c, 6, 7, 18, 8, 18, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS);
        c.set(7, 8, 18, Blocks.POTTED_FERN.defaultBlockState());

        c.set(5, 7, 25, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(6, 7, 25, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        c.set(10, 7, 25, chest(Direction.NORTH));

        for (int x = 3; x <= 11; x++) {
            c.setIfAir(x, 7, 20, Blocks.GRAY_CARPET.defaultBlockState());
            c.setIfAir(x, 7, 21, Blocks.GRAY_CARPET.defaultBlockState());
        }
    }

    private static void boxRoom(HouseCanvas c) {
        for (int x = 20; x <= 25; x++) {
            c.set(x, 7, 24, barrel(Direction.NORTH));
        }
        c.set(24, 8, 24, barrel(Direction.NORTH));
        c.set(25, 8, 24, barrel(Direction.NORTH));
        c.set(22, 7, 21, chest(Direction.SOUTH));
        c.set(19, 7, 21, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        c.set(26, 7, 21, chiseled(Direction.WEST));
        c.set(22, 10, 22, lantern(true));
    }

    // ------------------------------------------------------------------
    // Doors and doorway clearance
    // ------------------------------------------------------------------

    private static void placeDoors(HouseCanvas c) {
        door(c, HouseLayout.FRONT_DOOR.x(), 1, HouseLayout.FRONT_DOOR.z(), Direction.NORTH, Blocks.OAK_DOOR, DoorHingeSide.LEFT);
        door(c, HouseLayout.BACK_DOOR.x(), 1, HouseLayout.BACK_DOOR.z(), Direction.SOUTH, Blocks.OAK_DOOR, DoorHingeSide.LEFT);
        door(c, 13, 1, 20, Direction.EAST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
        door(c, 17, 1, 23, Direction.WEST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
        door(c, 13, 7, 4, Direction.EAST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
        door(c, 13, 7, 12, Direction.EAST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
        door(c, 17, 7, 7, Direction.WEST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
        door(c, 17, 7, 22, Direction.WEST, Blocks.SPRUCE_DOOR, DoorHingeSide.LEFT);
    }

    /** Openings as (x, z, floorY, wallNormalIsX) for the final clearance pass. */
    private static final int[][] DOORWAYS = {
            {HouseLayout.AXIS_X, HouseLayout.FRONT_DOOR_Z, 1, 0},
            {26, 12, 1, 0},
            {13, 7, 1, 1}, {13, 8, 1, 1},
            {17, 5, 1, 1}, {17, 6, 1, 1},
            {17, 13, 1, 1}, {17, 14, 1, 1},
            {13, 20, 1, 1},
            {17, 23, 1, 1},
            {13, 4, 7, 1},
            {13, 12, 7, 1},
            {13, 19, 7, 1}, {13, 20, 7, 1},
            {17, 7, 7, 1},
            {17, 13, 7, 1}, {17, 14, 7, 1},
            {17, 22, 7, 1}
    };

    /**
     * After every other pass, removes anything with real collision from the
     * cell on either side of each doorway, so no later furnishing or trim can
     * block a door. Carpets and other flat decoration may stay.
     */
    static void clearDoorways(HouseCanvas c) {
        for (int[] doorway : DOORWAYS) {
            int x = doorway[0];
            int z = doorway[1];
            int y0 = doorway[2];
            boolean normalX = doorway[3] == 1;

            for (int side = -1; side <= 1; side += 2) {
                int cx = normalX ? x + side : x;
                int cz = normalX ? z : z + side;
                for (int y = y0; y <= y0 + 1; y++) {
                    BlockPos pos = c.pos(cx, y, cz);
                    BlockState state = c.level.getBlockState(pos);
                    if (state.isAir() || state.getBlock() instanceof DoorBlock) {
                        continue;
                    }
                    if (state.getCollisionShape(c.level, pos).max(Direction.Axis.Y) > 0.1D) {
                        c.set(cx, y, cz, AIR);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Paintings
    // ------------------------------------------------------------------

    /**
     * @param x      the painting's anchor block: for a viewer facing the
     *               picture, the lower-left cell of an even-width painting
     *               or the centre column of an odd-width one
     * @param facing direction the picture faces, away from its wall
     */
    record PaintingSpec(int x, int y, int z, Direction facing, ResourceKey<PaintingVariant> variant) {
    }

    static void spawnPaintings(ServerLevel level, BlockPos origin) {
        for (PaintingSpec spec : PAINTINGS) {
            Holder<PaintingVariant> variant = level.registryAccess()
                    .lookupOrThrow(Registries.PAINTING_VARIANT)
                    .getOrThrow(spec.variant());

            Painting painting = new Painting(level, origin.offset(spec.x(), spec.y(), spec.z()), spec.facing(), variant);
            if (painting.survives()) {
                level.addFreshEntity(painting);
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void rug(HouseCanvas c, HouseRugs.Rug rug) {
        rug(c, rug.x0(), rug.y(), rug.z0(), rug.x1(), rug.z1(), rug.authoredBlock().defaultBlockState());
    }

    private static void rug(HouseCanvas c, int x0, int y, int z0, int x1, int z1, BlockState carpet) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                c.setIfAir(x, y, z, carpet);
            }
        }
    }

    /**
     * A board table standing on its own legs: upside-down stairs at each end
     * (their lower half reads as a leg) with top slabs between them.
     */
    private static void trestleTable(HouseCanvas c, int x0, int y, int z0, int x1, int z1, Block top, Block legs) {
        boolean alongX = z0 == z1;
        Direction low = alongX ? Direction.WEST : Direction.NORTH;
        c.fill(x0, y, z0, x1, y, z1, slab(top, SlabType.TOP));
        c.set(x0, y, z0, stairsTop(legs, low));
        c.set(x1, y, z1, stairsTop(legs, low.getOpposite()));
    }

    private static void table(HouseCanvas c, int x, int y, int z) {
        c.set(x, y, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(x, y + 1, z, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
    }

    private static void bed(HouseCanvas c, int x, int y, int z, Direction toHead, Block block) {
        BlockState foot = block.defaultBlockState()
                .setValue(BedBlock.FACING, toHead)
                .setValue(BedBlock.PART, BedPart.FOOT);
        c.set(x, y, z, foot);
        c.set(x + toHead.getStepX(), y, z + toHead.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static void door(HouseCanvas c, int x, int y, int z, Direction facing, Block block, DoorHingeSide hinge) {
        BlockState base = block.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, hinge)
                .setValue(DoorBlock.OPEN, false);
        c.set(x, y, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        c.set(x, y + 1, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    static BlockState lantern(boolean hanging) {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging);
    }

    private static BlockState candle(int count) {
        return Blocks.CANDLE.defaultBlockState()
                .setValue(CandleBlock.CANDLES, count)
                .setValue(CandleBlock.LIT, false);
    }

    private static BlockState campfire(boolean lit) {
        return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit);
    }

    private static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    private static BlockState chest(Direction facing) {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
    }

    private static BlockState chiseled(Direction facing) {
        return Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }

    private static BlockState facing(Block block, Direction facing) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }
}
