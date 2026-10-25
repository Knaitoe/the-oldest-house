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
 * The Barretts' own house (0.4.121). The filming rooms GhostsSetRooms lays out stay where the seven days need them (the
 * dining table, the living room, the kitchen, the girls' rooms upstairs, the basement under the kitchen, the den the crew
 * have made their video village); this closes the cutaway and turns the soundstage that stood round them into the rest of
 * the family's home. The front door opens on a hall with the stairs to one side; the garage, which the crew have taken
 * over, is through a doorway; behind the stairs are the laundry and the pantry; upstairs are John and Sarah's bedroom,
 * the bathroom, John's office with his job listings, the girls' bathroom and a spare room full of the crew's cases.
 * Everything is painted, lit and furnished like a home somebody lives in, and the production's cables, softboxes and
 * chairs are spread through it. Every routed cell the days walk (the crew's exit, Merry's last walk, the pickup up the
 * stairs) is left clear.
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
        BlockState paint = BarrettHomeBlock.of(Kind.PAINT), storey = BarrettHomeBlock.of(Kind.STOREY), air = Blocks.AIR.defaultBlockState();
        BlockState ceiling = GhostsSetBlock.of(GhostsSetBlock.Kind.CEILING, Direction.NORTH), tile = GhostsSetBlock.of(GhostsSetBlock.Kind.TILE, Direction.NORTH);

        // Two full storeys: one floor between them everywhere but the stairwell, one ceiling over the whole house.
        fill(l, b, -19, 10, -45, 19, 10, -1, ceiling);
        fill(l, b, -14, 4, -45, 19, 4, -1, storey);
        fill(l, b, -19, 4, -14, -15, 4, -1, storey);
        fill(l, b, -19, 4, -45, -15, 4, -21, storey);
        fill(l, b, 13, 4, -44, 13, 4, -15, paint);

        // The front of the house is closed: the hall, the garage and the rooms above them are the family's.
        fill(l, b, -19, 5, -14, 19, 9, -14, paint);
        fill(l, b, 0, 5, -13, 0, 9, -1, paint);
        fill(l, b, 7, 5, -13, 7, 9, -1, paint);
        door(l, b, -4, 5, -14, Direction.SOUTH);
        door(l, b, 3, 5, -14, Direction.SOUTH);
        door(l, b, 10, 5, -14, Direction.SOUTH);
        // The girls' rooms end at their back wall; the spare room and the girls' bathroom are walled off the dead space.
        fill(l, b, -14, 5, -45, 13, 9, -45, paint);
        door(l, b, 13, 5, -19, Direction.EAST);
        fill(l, b, 14, 5, -31, 19, 9, -31, paint);
        fill(l, b, 14, 5, -45, 19, 9, -32, paint);
        fill(l, b, -19, 5, -23, -15, 9, -23, paint);
        door(l, b, -17, 5, -23, Direction.SOUTH);
        fill(l, b, -19, 5, -33, -15, 9, -33, paint);
        fill(l, b, -19, 5, -45, -15, 9, -34, paint);

        // The garage, which the crew have made their base: a concrete slab, drywall, the roll-up door.
        fill(l, b, 8, -1, -13, 19, -1, -1, BarrettHomeBlock.of(Kind.CONCRETE));
        fill(l, b, 7, 0, -13, 7, 3, -1, paint);
        doorway(l, b, 7, 0, -13, 7, -12);
        fill(l, b, 14, 0, -14, 19, 3, -14, paint);
        doorway(l, b, 16, 0, -14, 17, -14);
        fill(l, b, 20, 0, -13, 20, 3, -1, BarrettHomeBlock.of(Kind.DRYWALL));
        fill(l, b, 8, 0, 0, 19, 3, 0, BarrettHomeBlock.of(Kind.DRYWALL));
        fill(l, b, 10, 0, 0, 17, 2, 0, BarrettHomeBlock.of(Kind.GARAGE_DOOR, Direction.NORTH));

        // Behind the stairs: the laundry, then the pantry off the kitchen.
        fill(l, b, -19, 0, -23, -15, 3, -23, paint);
        door(l, b, -17, 0, -23, Direction.SOUTH);
        fill(l, b, -19, 0, -34, -15, 3, -34, paint);
        fill(l, b, -19, -1, -33, -15, -1, -24, tile);
        door(l, b, -14, 0, -40, Direction.WEST);

        // Windows with their blinds drawn: the crew keep the daylight out of the house.
        window(l, b, -10, 1, 0, -9, 2, 0); window(l, b, 3, 1, 0, 4, 2, 0);
        window(l, b, -20, 1, -6, -20, 2, -5); window(l, b, -20, 2, -29, -20, 2, -28);
        window(l, b, 20, 2, -7, 20, 2, -6); window(l, b, 20, 1, -20, 20, 2, -19); window(l, b, 20, 1, -26, 20, 2, -25);
        window(l, b, -11, 1, -46, -9, 2, -46); window(l, b, 3, 1, -46, 5, 2, -46);
        window(l, b, -15, 6, 0, -14, 7, 0); window(l, b, -8, 6, 0, -7, 7, 0); window(l, b, -20, 6, -12, -20, 7, -11);
        window(l, b, 3, 7, 0, 4, 7, 0); window(l, b, 11, 6, 0, 12, 7, 0); window(l, b, 16, 6, 0, 17, 7, 0); window(l, b, 20, 6, -7, 20, 7, -6);
        window(l, b, 20, 6, -21, 20, 7, -20); window(l, b, -8, 6, -45, -7, 7, -45); window(l, b, 4, 6, -45, 5, 7, -45); window(l, b, -20, 7, -28, -20, 7, -27);

        // Ceiling lights in the rooms the old set never lit.
        for (int[] at : new int[][]{{-8, 3, -7}, {2, 3, -7}, {-17, 3, -10}, {13, 3, -7}, {17, 3, -4}, {17, 3, -20}, {17, 3, -27}, {-17, 3, -28}, {-17, 3, -40}, {-16, 3, -22},
                {-10, 9, -7}, {3, 9, -7}, {13, 9, -7}, {17, 9, -22}, {-17, 9, -28}, {-4, 9, -18}, {6, 9, -18}})
            put(l, b, at[0], at[1], at[2], Kind.CEILING_LIGHT, Direction.NORTH);

        hall(l, b);
        garage(l, b);
        den(l, b);
        backRooms(l, b);
        kitchenAndLiving(l, b);
        upstairs(l, b);
        basement(l, b);
    }

    /** The front hall: the console with the mail, coats and shoes by the door, school portraits, the stairs at the end. */
    private static void hall(ServerLevel l, BlockPos b) {
        put(l, b, -3, 0, -1, Kind.CONSOLE, Direction.NORTH);
        put(l, b, -3, 2, -1, Kind.PHOTOS, Direction.NORTH);
        put(l, b, -6, 1, -1, Kind.COAT_HOOKS, Direction.NORTH);
        put(l, b, -7, 1, -1, Kind.COAT_HOOKS, Direction.NORTH);
        put(l, b, -6, 0, -1, Kind.SHOE_RACK, Direction.NORTH);
        put(l, b, 5, 0, -1, Kind.PLANT, Direction.NORTH);
        for (int x : new int[]{-11, -8, -3, 3}) put(l, b, x, 1, -13, Kind.PHOTOS, Direction.SOUTH);
        put(l, b, -15, 0, -13, Kind.LAUNDRY, Direction.NORTH);
        for (int x = -1; x <= 1; x++) for (int z = -11; z <= -4; z++) set(l, b, x, 0, z, GhostsSetBlock.of(GhostsSetBlock.Kind.RUG, Direction.NORTH));
        // The cable run from the garage, along the hall wall and under the living-room door.
        cable(l, b, 1, 0, -12, 15, -12);
        put(l, b, 0, 0, -12, Kind.CABLE_CORNER, Direction.NORTH);
        set(l, b, 0, 0, -13, BarrettHomeBlock.of(Kind.CABLE, Direction.EAST));
        cable(l, b, 0, 0, -24, 0, -15);
    }

    /** The garage: the family's shelves and Christmas boxes, the crew's craft table, chairs, cases and the call sheets. */
    private static void garage(ServerLevel l, BlockPos b) {
        put(l, b, 17, 0, -9, Kind.FOLDING_TABLE, Direction.NORTH);
        put(l, b, 12, 0, -10, Kind.CRAFT_TABLE, Direction.NORTH);
        put(l, b, 10, 0, -7, Kind.DIRECTORS_CHAIR, Direction.EAST);
        put(l, b, 10, 0, -5, Kind.DIRECTORS_CHAIR, Direction.EAST);
        put(l, b, 19, 0, -5, Kind.SHELVES, Direction.WEST);
        put(l, b, 19, 0, -6, Kind.SHELVES, Direction.WEST);
        put(l, b, 19, 0, -9, Kind.BOXES, Direction.WEST);
        put(l, b, 19, 0, -2, Kind.APPLE_BOXES, Direction.WEST);
        put(l, b, 18, 0, -2, Kind.SANDBAGS, Direction.NORTH);
        put(l, b, 19, 0, -12, Kind.SANDBAGS, Direction.WEST);
        put(l, b, 16, 0, -12, Kind.CABLE_CORNER, Direction.WEST);
        set(l, b, 16, 0, -13, BarrettHomeBlock.of(Kind.CABLE, Direction.EAST));
        set(l, b, 16, 0, -14, BarrettHomeBlock.of(Kind.CABLE, Direction.EAST));
    }

    /** The den the crew made their video village: the family's sofa and shelves, the monitors and director's chairs. */
    private static void den(ServerLevel l, BlockPos b) {
        put(l, b, 19, 0, -23, Kind.SOFA_A, Direction.WEST);
        put(l, b, 19, 0, -24, Kind.SOFA_B, Direction.WEST);
        put(l, b, 14, 0, -23, Kind.TV_STAND, Direction.EAST);
        put(l, b, 14, 0, -17, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, 19, 0, -16, Kind.PLANT, Direction.WEST);
        put(l, b, 14, 2, -20, Kind.PHOTOS, Direction.EAST);
        put(l, b, 18, 0, -29, Kind.VILLAGE_CART, Direction.WEST);
        put(l, b, 19, 0, -27, Kind.SOFTBOX, Direction.WEST);
        put(l, b, 18, 0, -36, Kind.DIRECTORS_CHAIR, Direction.WEST);
        put(l, b, 19, 0, -43, Kind.APPLE_BOXES, Direction.WEST);
        cable(l, b, 16, 0, -28, 16, -15);
    }

    /** The laundry and the pantry behind the stairs. */
    private static void backRooms(ServerLevel l, BlockPos b) {
        put(l, b, -19, 0, -25, Kind.WASHER, Direction.EAST);
        put(l, b, -19, 0, -26, Kind.DRYER, Direction.EAST);
        put(l, b, -19, 0, -28, Kind.LAUNDRY, Direction.EAST);
        put(l, b, -15, 0, -32, Kind.SHELVES, Direction.WEST);
        put(l, b, -16, 0, -26, Kind.CLOTHES, Direction.NORTH);
        for (int z = -38; z <= -36; z++) put(l, b, -19, 0, z, Kind.SHELVES, Direction.EAST);
        put(l, b, -19, 0, -42, Kind.BOXES, Direction.EAST);
        put(l, b, -19, 0, -44, Kind.WATER_HEATER, Direction.EAST);
        put(l, b, -15, 0, -44, Kind.SANDBAGS, Direction.WEST);
    }

    /** The open kitchen's fridge and back counter, the living room's television, the softboxes trained on the table. */
    private static void kitchenAndLiving(ServerLevel l, BlockPos b) {
        put(l, b, -13, 0, -45, Kind.FRIDGE, Direction.SOUTH);
        put(l, b, -13, 1, -45, Kind.FRIDGE_TOP, Direction.SOUTH);
        for (int x = -12; x <= -8; x++) set(l, b, x, 0, -45, GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER, Direction.SOUTH));
        put(l, b, -13, 1, -38, Kind.CALENDAR, Direction.EAST);
        put(l, b, -9, 0, -22, Kind.TV_STAND, Direction.SOUTH);
        put(l, b, -13, 0, -15, Kind.PLANT, Direction.EAST);
        put(l, b, -13, 2, -21, Kind.PHOTOS, Direction.EAST);
        put(l, b, -13, 2, -25, Kind.PHOTOS, Direction.EAST);
        put(l, b, 5, 0, -32, Kind.SOFTBOX, Direction.SOUTH);
        put(l, b, -4, 0, -32, Kind.SOFTBOX, Direction.SOUTH);
    }

    /** Upstairs: the landing, the girls' rooms lived in, John and Sarah's room, the bathrooms, John's office, the spare room. */
    private static void upstairs(ServerLevel l, BlockPos b) {
        put(l, b, -4, 6, -22, Kind.PHOTOS, Direction.SOUTH);
        put(l, b, 4, 6, -22, Kind.PHOTOS, Direction.SOUTH);
        put(l, b, -13, 5, -16, Kind.LAUNDRY, Direction.NORTH);
        put(l, b, 12, 5, -15, Kind.PLANT, Direction.WEST);
        cable(l, b, -9, 5, -22, -9, -17);
        // Marjorie's room: a desk and chair, her poster, clothes on the floor, a softbox in the corner for the reconstructions.
        put(l, b, -2, 5, -43, Kind.DESK, Direction.WEST);
        put(l, b, -3, 5, -43, Kind.OFFICE_CHAIR, Direction.EAST);
        put(l, b, -1, 7, -36, Kind.POSTER_A, Direction.WEST);
        put(l, b, -4, 5, -40, Kind.CLOTHES, Direction.NORTH);
        put(l, b, -2, 5, -25, Kind.SOFTBOX, Direction.WEST);
        // Merry's room: her homework desk, the planets, a pile of clothes.
        put(l, b, 1, 5, -43, Kind.DESK_KID, Direction.EAST);
        put(l, b, 12, 7, -36, Kind.POSTER_B, Direction.WEST);
        put(l, b, 4, 5, -38, Kind.CLOTHES, Direction.NORTH);
        // John and Sarah's room, at the front.
        put(l, b, -18, 5, -7, Kind.DOUBLE_BED, Direction.EAST);
        furniture(l, b, -19, 5, -8, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.EAST);
        furniture(l, b, -19, 5, -5, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.EAST);
        put(l, b, -19, 7, -6, Kind.PHOTOS, Direction.EAST);
        put(l, b, -10, 5, -1, Kind.DRESSER, Direction.NORTH);
        put(l, b, -2, 5, -12, Kind.LAUNDRY, Direction.NORTH);
        put(l, b, -12, 5, -8, Kind.CLOTHES, Direction.NORTH);
        // The family bathroom.
        put(l, b, 1, 5, -1, Kind.TUB, Direction.WEST);
        put(l, b, 6, 5, -1, Kind.TOILET, Direction.NORTH);
        put(l, b, 6, 5, -5, Kind.VANITY, Direction.WEST);
        // John's office: the job listings, the laptop, his shelves and the filing boxes.
        put(l, b, 12, 5, -1, Kind.DESK, Direction.NORTH);
        put(l, b, 12, 5, -2, Kind.OFFICE_CHAIR, Direction.SOUTH);
        put(l, b, 19, 5, -10, Kind.BOOKSHELF, Direction.WEST);
        put(l, b, 19, 5, -12, Kind.BOXES, Direction.WEST);
        // The spare room, where the crew keep their cases.
        set(l, b, 18, 5, -28, GhostsSetBlock.of(GhostsSetBlock.Kind.BED_HEAD, Direction.NORTH));
        set(l, b, 18, 5, -27, GhostsSetBlock.of(GhostsSetBlock.Kind.BED_FOOT, Direction.NORTH));
        put(l, b, 15, 5, -29, Kind.APPLE_BOXES, Direction.NORTH);
        put(l, b, 15, 5, -16, Kind.SANDBAGS, Direction.NORTH);
        put(l, b, 19, 5, -16, Kind.BOXES, Direction.WEST);
        // The girls' bathroom.
        put(l, b, -19, 5, -25, Kind.TUB, Direction.SOUTH);
        put(l, b, -15, 5, -31, Kind.TOILET, Direction.WEST);
        put(l, b, -17, 5, -32, Kind.VANITY, Direction.SOUTH);
    }

    /** The basement: the boxes nobody unpacked, the water heater, shelves. The day-four marks and the stair stay clear. */
    private static void basement(ServerLevel l, BlockPos b) {
        put(l, b, 4, -4, -22, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -24, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -28, Kind.SHELVES, Direction.EAST);
        put(l, b, 10, -4, -29, Kind.WATER_HEATER, Direction.WEST);
    }
}
