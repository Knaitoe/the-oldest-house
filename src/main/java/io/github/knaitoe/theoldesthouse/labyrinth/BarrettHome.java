package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BarrettHomeBlock.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * The Barretts' own house (0.4.121), condensed in 0.4.174 (owner: "condense house - make it into a appropriately smaller,
 * more lived in structure"). GhostsSetRooms lays out the walls and the rooms the seven days are filmed in (the dining
 * table, the family corner, the girls' rooms upstairs, the basement under the dining room); this furnishes the rest of the
 * family's home round them. The front door opens on a hall with the stairs climbing one side of a two-storey foyer; the
 * front room is to the west and the den the crew have made their video village to the east; behind them the open kitchen
 * and dining room and the utility strip with the basement stair. Upstairs are John and Sarah's room, the bathroom, the
 * landing over the foyer and, at the back, the girls' rooms with their bathroom between. Every room is painted, lit and
 * furnished like a home somebody lives in, with the production's cables, softboxes and cases spread through it, and every
 * routed cell the days walk (the crew's exit, Merry's last walk, the pickup up both stairs, Marjorie's crawl from the
 * table) is left clear.
 */
public final class BarrettHome {
    private BarrettHome() {}

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { GhostsSetRooms.set(l, b, x, y, z, s); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) { GhostsSetRooms.fill(l, b, x0, y0, z0, x1, y1, z1, s); }
    private static void put(ServerLevel l, BlockPos b, int x, int y, int z, Kind k, Direction f) {
        set(l, b, x, y, z, BarrettHomeBlock.of(k, f));
        for (var c : BarrettHomeBlock.covers(new BlockPos(x, y, z), k, f)) set(l, b, c.getX(), c.getY(), c.getZ(), BarrettHomeBlock.of(Kind.BED_FILL, f));
    }
    private static void door(ServerLevel l, BlockPos b, int x, int y, int z, Direction f) {
        var s = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, f).setValue(DoorBlock.OPEN, true);
        set(l, b, x, y, z, s); set(l, b, x, y + 1, z, s.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }
    private static void doorway(ServerLevel l, BlockPos b, int x0, int y, int z0, int x1, int z1) { fill(l, b, x0, y, z0, x1, y + 2, z1, Blocks.AIR.defaultBlockState()); }
    private static void furniture(ServerLevel l, BlockPos b, int x, int y, int z, HouseholdFurnitureBlock.Kind k, Direction f) {
        set(l, b, x, y, z, HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND, k).setValue(HouseholdFurnitureBlock.FACING, f));
    }
    private static void window(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(l, b, x0, y0, z0, x1, y1, z1, BarrettHomeBlock.of(Kind.WINDOW, x0 == x1 ? Direction.EAST : Direction.NORTH));
    }
    private static void cable(ServerLevel l, BlockPos b, int x0, int y, int z0, int x1, int z1) {
        var f = z0 == z1 ? Direction.NORTH : Direction.EAST;
        fill(l, b, x0, y, z0, x1, y, z1, BarrettHomeBlock.of(Kind.CABLE, f));
    }

    public static void build(ServerLevel l, BlockPos b) {
        // Windows with their blinds drawn: the crew keep the daylight out of the house.
        window(l, b, -8, 1, 0, -7, 2, 0); window(l, b, 7, 1, 0, 8, 2, 0);
        window(l, b, -8, 6, 0, -7, 7, 0); window(l, b, -1, 6, 0, 1, 7, 0); window(l, b, 7, 6, 0, 8, 7, 0);
        window(l, b, -11, 1, -6, -11, 2, -5); window(l, b, -11, 2, -16, -11, 2, -14); window(l, b, -11, 2, -21, -11, 2, -20);
        window(l, b, -11, 6, -9, -11, 7, -8); window(l, b, -11, 6, -19, -11, 7, -18);
        window(l, b, 11, 2, -6, 11, 2, -5); window(l, b, 11, 2, -22, 11, 2, -21);
        window(l, b, 11, 6, -7, 11, 7, -6); window(l, b, 11, 6, -19, 11, 7, -18);
        window(l, b, -8, 2, -26, -6, 2, -26); window(l, b, 2, 1, -26, 3, 2, -26);
        window(l, b, -5, 6, -26, -4, 7, -26); window(l, b, 0, 7, -26, 0, 7, -26); window(l, b, 4, 6, -26, 5, 7, -26);

        // Ceiling lights in every room the lamps do not reach, the foyer's from its upper ceiling.
        for (int[] at : new int[][]{{-7, 3, -6}, {1, 3, -10}, {8, 3, -5}, {-7, 3, -15}, {8, 3, -21},
                {-7, 9, -5}, {0, 9, -4}, {7, 9, -5}, {-5, 9, -12}, {5, 9, -12}, {0, 9, -20}})
            put(l, b, at[0], at[1], at[2], Kind.CEILING_LIGHT, Direction.NORTH);

        hall(l, b);
        frontRoom(l, b);
        den(l, b);
        utility(l, b);
        kitchenAndDining(l, b);
        upstairs(l, b);
        basement(l, b);
    }

    /** The front hall: coats and shoes by the door, the mail on the console, school portraits under the landing, a basket of
     *  laundry at the foot of the stairs, and the crew's cable in from the den, under the family's door to the table. */
    private static void hall(ServerLevel l, BlockPos b) {
        put(l, b, 3, 0, -1, Kind.PLANT, Direction.WEST);
        put(l, b, 3, 1, -5, Kind.COAT_HOOKS, Direction.WEST);
        put(l, b, 3, 1, -6, Kind.COAT_HOOKS, Direction.WEST);
        put(l, b, 3, 0, -5, Kind.SHOE_RACK, Direction.WEST);
        put(l, b, 3, 0, -8, Kind.CONSOLE, Direction.WEST);
        put(l, b, 3, 2, -8, Kind.PHOTOS, Direction.WEST);
        for (int x : new int[]{-2, 2}) put(l, b, x, 1, -11, Kind.PHOTOS, Direction.SOUTH);
        put(l, b, -3, 0, -2, Kind.LAUNDRY, Direction.NORTH);
        for (int x = 1; x <= 2; x++) for (int z = -8; z <= -1; z++) set(l, b, x, 0, z, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
        cable(l, b, 1, 0, -3, 4, -3);
        put(l, b, 0, 0, -3, Kind.CABLE_CORNER, Direction.NORTH);
        cable(l, b, 0, 0, -11, 0, -4);
        cable(l, b, 0, 0, -17, 0, -13);
    }

    /** The front room at the west end of the hall: the good sofa under the window and its end table, radiators under the side
     *  window, an armchair and footstool on the rug, the shelves and the sideboard with the girls' school photographs. */
    private static void frontRoom(ServerLevel l, BlockPos b) {
        furniture(l, b, -9, 0, -1, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.NORTH);
        furniture(l, b, -8, 0, -1, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.NORTH);
        furniture(l, b, -10, 0, -1, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.NORTH);
        set(l, b, -10, 1, -1, Blocks.POTTED_FERN.defaultBlockState());
        furniture(l, b, -10, 0, -6, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.EAST);
        furniture(l, b, -10, 0, -5, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.EAST);
        furniture(l, b, -5, 0, -4, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.WEST);
        furniture(l, b, -6, 0, -4, HouseholdFurnitureBlock.Kind.FOOTSTOOL, Direction.WEST);
        for (int x = -8; x <= -7; x++) for (int z = -6; z <= -3; z++) set(l, b, x, 0, z, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
        put(l, b, -5, 0, -1, Kind.PLANT, Direction.NORTH);
        put(l, b, -10, 0, -11, Kind.BOOKSHELF, Direction.SOUTH);
        put(l, b, -9, 0, -11, Kind.DRESSER, Direction.SOUTH);
        put(l, b, -9, 2, -11, Kind.PHOTOS, Direction.SOUTH);
        put(l, b, -10, 2, -8, Kind.PHOTOS, Direction.EAST);
    }

    /** The den the crew made their video village: the family's sofa, television and shelves, the call sheets on the folding
     *  table, the monitor and its chairs, and the private-interview corner. */
    private static void den(ServerLevel l, BlockPos b) {
        put(l, b, 9, 0, -2, Kind.FOLDING_TABLE, Direction.NORTH);
        put(l, b, 5, 0, -1, Kind.SANDBAGS, Direction.NORTH);
        put(l, b, 10, 0, -1, Kind.APPLE_BOXES, Direction.WEST);
        put(l, b, 10, 0, -3, Kind.SOFTBOX, Direction.WEST);
        put(l, b, 10, 0, -4, Kind.PLANT, Direction.WEST);
        put(l, b, 10, 0, -5, Kind.SOFA_A, Direction.WEST);
        put(l, b, 10, 0, -6, Kind.SOFA_B, Direction.WEST);
        put(l, b, 10, 2, -8, Kind.PHOTOS, Direction.WEST);
        put(l, b, 5, 0, -6, Kind.TV_STAND, Direction.EAST);
        // 0.4.199 (owner: "tv is cutting into shelf"): the screen is wider than its stand, so the shelf stands a block along.
        put(l, b, 5, 0, -8, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, 5, 2, -6, Kind.PHOTOS, Direction.EAST);
        put(l, b, 5, 0, -10, Kind.VILLAGE_CART, Direction.EAST);
        put(l, b, 7, 0, -9, Kind.DIRECTORS_CHAIR, Direction.NORTH);
        put(l, b, 9, 0, -9, Kind.DIRECTORS_CHAIR, Direction.NORTH);
        cable(l, b, 6, 0, -10, 6, -3);
    }

    /** The utility strip behind the den: the stair down, the washer and dryer, the pantry shelves and the water heater. */
    private static void utility(ServerLevel l, BlockPos b) {
        put(l, b, 10, 0, -20, Kind.WASHER, Direction.WEST);
        put(l, b, 10, 0, -21, Kind.DRYER, Direction.WEST);
        put(l, b, 10, 0, -23, Kind.LAUNDRY, Direction.WEST);
        put(l, b, 8, 0, -22, Kind.CLOTHES, Direction.NORTH);
        put(l, b, 6, 0, -23, Kind.SHELVES, Direction.EAST);
        put(l, b, 6, 0, -24, Kind.SHELVES, Direction.EAST);
        put(l, b, 7, 0, -25, Kind.BOXES, Direction.SOUTH);
        put(l, b, 10, 0, -25, Kind.WATER_HEATER, Direction.WEST);
    }

    /** The open kitchen and dining room: the fridge, the calendar and the table they eat at when nobody is filming; the family
     *  corner's television and shelves; the family computer and the sideboard by the dining table; the softboxes trained on it. */
    private static void kitchenAndDining(ServerLevel l, BlockPos b) {
        put(l, b, -10, 0, -25, Kind.FRIDGE, Direction.SOUTH);
        put(l, b, -10, 1, -25, Kind.FRIDGE_TOP, Direction.SOUTH);
        put(l, b, -10, 1, -22, Kind.CALENDAR, Direction.EAST);
        furniture(l, b, -8, 0, -21, HouseholdFurnitureBlock.Kind.FORMICA_TABLE, Direction.SOUTH);
        furniture(l, b, -9, 0, -21, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
        furniture(l, b, -7, 0, -21, HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.WEST);
        // 0.4.199 (owner: "furniture placement is also a bit off"): the corner's television faces the chairs, not the wall; the
        // side table stands beside the green armchair rather than wedged behind it, and the shelf has the wall to itself.
        put(l, b, -8, 0, -13, Kind.TV_STAND, Direction.NORTH);
        furniture(l, b, -9, 0, -18, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.EAST);
        set(l, b, -9, 1, -18, GhostsSetBlock.of(GhostsSetBlock.Kind.BOOKS, Direction.EAST));
        put(l, b, -10, 0, -20, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, -10, 2, -15, Kind.PHOTOS, Direction.EAST);
        put(l, b, -5, 0, -13, Kind.PLANT, Direction.SOUTH);
        put(l, b, 4, 0, -16, Kind.DESK, Direction.WEST);
        put(l, b, 3, 0, -16, Kind.OFFICE_CHAIR, Direction.EAST);
        put(l, b, 4, 0, -20, Kind.DRESSER, Direction.WEST);
        put(l, b, 4, 0, -21, Kind.DRESSER, Direction.WEST);
        put(l, b, 4, 2, -20, Kind.PHOTOS, Direction.WEST);
        put(l, b, 4, 0, -23, Kind.SOFTBOX, Direction.SOUTH);
        put(l, b, -3, 0, -23, Kind.SOFTBOX, Direction.SOUTH);
        furniture(l, b, 2, 0, -25, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.SOUTH);
        put(l, b, 3, 0, -25, Kind.PLANT, Direction.SOUTH);
    }

    /** Upstairs: John and Sarah's room with his job listings on the desk, the bathroom, the landing, the girls' rooms lived in
     *  and their bathroom between them. */
    private static void upstairs(ServerLevel l, BlockPos b) {
        put(l, b, -9, 5, -6, Kind.DOUBLE_BED, Direction.EAST);
        furniture(l, b, -10, 5, -7, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.EAST);
        furniture(l, b, -10, 5, -4, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.EAST);
        put(l, b, -10, 7, -6, Kind.PHOTOS, Direction.EAST);
        put(l, b, -8, 5, -1, Kind.DRESSER, Direction.NORTH);
        put(l, b, -6, 5, -1, Kind.DESK, Direction.NORTH);
        put(l, b, -6, 5, -2, Kind.OFFICE_CHAIR, Direction.SOUTH);
        put(l, b, -10, 5, -10, Kind.BOXES, Direction.EAST);
        put(l, b, -5, 5, -9, Kind.LAUNDRY, Direction.NORTH);
        put(l, b, -7, 5, -8, Kind.CLOTHES, Direction.NORTH);
        // The family bathroom.
        put(l, b, 6, 5, -1, Kind.TUB, Direction.WEST);
        put(l, b, 10, 5, -4, Kind.TOILET, Direction.WEST);
        put(l, b, 10, 5, -7, Kind.VANITY, Direction.WEST);
        // The landing and its corridor.
        put(l, b, 3, 5, -11, Kind.PLANT, Direction.WEST);
        put(l, b, -3, 5, -11, Kind.LAUNDRY, Direction.NORTH);
        for (int x : new int[]{-3, 3}) put(l, b, x, 6, -13, Kind.PHOTOS, Direction.SOUTH);
        cable(l, b, -6, 5, -13, -1, -13);
        // Marjorie's room: a desk and chair, her poster, clothes on the floor, a softbox in the corner for the reconstructions.
        put(l, b, -3, 5, -25, Kind.DESK, Direction.WEST);
        put(l, b, -4, 5, -25, Kind.OFFICE_CHAIR, Direction.EAST);
        put(l, b, -3, 7, -20, Kind.POSTER_A, Direction.WEST);
        put(l, b, -5, 5, -21, Kind.CLOTHES, Direction.NORTH);
        put(l, b, -3, 5, -16, Kind.SOFTBOX, Direction.WEST);
        put(l, b, -10, 5, -16, Kind.DRESSER, Direction.EAST);
        put(l, b, -10, 5, -17, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, -9, 5, -15, Kind.LAUNDRY, Direction.NORTH);
        for (int x = -6; x <= -4; x++) for (int z = -20; z <= -18; z++) set(l, b, x, 5, z, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
        // The girls' bathroom between their rooms.
        put(l, b, 0, 5, -24, Kind.TUB, Direction.SOUTH);
        put(l, b, 2, 5, -20, Kind.TOILET, Direction.WEST);
        put(l, b, -1, 5, -18, Kind.VANITY, Direction.EAST);
        // Merry's room: her homework desk, the planets, a pile of clothes.
        put(l, b, 4, 5, -22, Kind.DESK_KID, Direction.EAST);
        put(l, b, 10, 7, -20, Kind.POSTER_B, Direction.WEST);
        put(l, b, 5, 5, -19, Kind.CLOTHES, Direction.NORTH);
        put(l, b, 10, 5, -16, Kind.DRESSER, Direction.WEST);
        put(l, b, 10, 5, -15, Kind.PLANT, Direction.WEST);
        furniture(l, b, 10, 5, -25, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.WEST);
        for (int x = 5; x <= 6; x++) for (int z = -18; z <= -16; z++) set(l, b, x, 5, z, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
        // A runner the length of the corridor.
        for (int x = -9; x <= 9; x++) set(l, b, x, 5, -12, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
    }

    /** The basement: the boxes nobody unpacked, the water heater, shelves. The day-four marks and the stair stay clear. */
    private static void basement(ServerLevel l, BlockPos b) {
        put(l, b, 4, -4, -15, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -17, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -21, Kind.SHELVES, Direction.EAST);
        put(l, b, 10, -4, -22, Kind.WATER_HEATER, Direction.WEST);
    }
}
