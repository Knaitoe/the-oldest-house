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
    /** A double window two blocks wide and two high: each column's trim on its outer edge (the left column turned to face into
     *  the room, the right one turned the other way, so the two meet as one window), the sill below, the blind's rail above.
     *  {@code into} is the way the room lies from the wall. */
    private static void window(ServerLevel l, BlockPos b, int ax, int az, int bx, int bz, int y0, Direction into) {
        var left = into.getOpposite().getCounterClockWise();
        boolean aLeft = (ax - bx) * left.getStepX() + (az - bz) * left.getStepZ() > 0;
        for (int dy = 0; dy <= 1; dy++) {
            var kind = dy == 0 ? Kind.WINDOW : Kind.WINDOW_HEAD;
            set(l, b, ax, y0 + dy, az, BarrettHomeBlock.of(kind, aLeft ? into : into.getOpposite()));
            set(l, b, bx, y0 + dy, bz, BarrettHomeBlock.of(kind, aLeft ? into.getOpposite() : into));
        }
    }
    private static void cable(ServerLevel l, BlockPos b, int x0, int y, int z0, int x1, int z1) {
        var f = z0 == z1 ? Direction.NORTH : Direction.EAST;
        fill(l, b, x0, y, z0, x1, y, z1, BarrettHomeBlock.of(Kind.CABLE, f));
    }

    public static void build(ServerLevel l, BlockPos b) {
        // 0.4.202 (owner: "blinds should be single columns facing opposite directions rather than four blocks", "the placement of
        // those blinds don't make sense"): one double window to a wall where a room wants one, centred on it, blinds drawn.
        window(l, b, -8, 0, -7, 0, 1, Direction.NORTH); window(l, b, -11, -6, -11, -5, 1, Direction.EAST);       // the living room
        window(l, b, 7, 0, 8, 0, 1, Direction.NORTH); window(l, b, 11, -6, 11, -5, 1, Direction.WEST);           // the den
        window(l, b, -8, -26, -7, -26, 1, Direction.SOUTH); window(l, b, -11, -21, -11, -20, 1, Direction.EAST); // the kitchen
        window(l, b, 0, -26, 1, -26, 1, Direction.SOUTH);                                                        // the dining room
        window(l, b, 11, -22, 11, -21, 1, Direction.WEST);                                                       // the utility strip
        window(l, b, -8, 0, -7, 0, 6, Direction.NORTH); window(l, b, -11, -9, -11, -8, 6, Direction.EAST);       // John and Sarah's room
        window(l, b, 11, -9, 11, -8, 6, Direction.WEST);                                                         // the family bathroom
        window(l, b, -11, -13, -11, -12, 6, Direction.EAST);                                                     // the end of the landing
        window(l, b, -11, -19, -11, -18, 6, Direction.EAST); window(l, b, -7, -26, -6, -26, 6, Direction.SOUTH); // Marjorie's room
        window(l, b, 11, -19, 11, -18, 6, Direction.WEST); window(l, b, 7, -26, 8, -26, 6, Direction.SOUTH);     // Merry's room

        // Ceiling lights in every room the lamps do not reach, the foyer's from its upper ceiling.
        for (int[] at : new int[][]{{-7, 3, -6}, {1, 3, -10}, {8, 3, -5}, {-7, 3, -15}, {8, 3, -21},
                {-7, 9, -5}, {0, 9, -4}, {8, 9, -7}, {-5, 9, -12}, {5, 9, -12}, {0, 9, -18}})
            put(l, b, at[0], at[1], at[2], Kind.CEILING_LIGHT, Direction.NORTH);

        hall(l, b);
        livingRoom(l, b);
        den(l, b);
        utility(l, b);
        kitchenAndDining(l, b);
        upstairs(l, b);
        bathrooms(l, b);
        girlsRooms(l, b);
        basement(l, b);
    }

    /** The front hall: coats and shoes by the door, the mail on the console under the beach picture, the girls' school portraits
     *  under the landing, the studio portrait of the four of them high over the front door where the landing looks at it, and
     *  the crew's cable in from the den, under the family's door to the table. */
    private static void hall(ServerLevel l, BlockPos b) {
        put(l, b, 3, 0, -1, Kind.PLANT, Direction.WEST);
        put(l, b, 3, 1, -5, Kind.COAT_HOOKS, Direction.WEST);
        put(l, b, 3, 1, -6, Kind.COAT_HOOKS_B, Direction.WEST);
        put(l, b, 3, 0, -5, Kind.SHOE_RACK, Direction.WEST);
        put(l, b, 3, 0, -8, Kind.CONSOLE, Direction.WEST);
        put(l, b, 3, 2, -8, Kind.PHOTO_BEACH, Direction.WEST);
        put(l, b, -2, 1, -11, Kind.PHOTO_MARJORIE, Direction.SOUTH);
        put(l, b, 2, 1, -11, Kind.PHOTO_MERRY, Direction.SOUTH);
        put(l, b, 0, 6, -1, Kind.PORTRAIT, Direction.NORTH);
        // 0.4.220: a runner of the hall's own, its border down both outer edges.
        for (int x = 1; x <= 2; x++) for (int z = -8; z <= -1; z++) set(l, b, x, 0, z, BarrettHomeBlock.of(Kind.RUG_RUNNER, x == 1 ? Direction.NORTH : Direction.SOUTH));
        cable(l, b, 1, 0, -3, 4, -3);
        put(l, b, 0, 0, -3, Kind.CABLE_CORNER, Direction.NORTH);
        cable(l, b, 0, 0, -11, 0, -4);
        cable(l, b, 0, 0, -17, 0, -13);
    }

    /** 0.4.202 (owner: "two living/lounging spaces in the house"): the front room is the one living room. The set is on (a
     *  cartoon nobody turned off), the sofa faces it, the two armchairs face each other across the rug where the day-one
     *  interviews are shot, Merry's colouring book and crayons are on the coffee table and her toys by the window, and the
     *  pictures over the sofa are Sarah with Merry new and Marjorie leaning in, and the two girls in the yard. */
    private static void livingRoom(ServerLevel l, BlockPos b) {
        put(l, b, -10, 0, -8, Kind.TV_STAND, Direction.EAST);
        put(l, b, -10, 0, -10, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, -10, 0, -11, Kind.PLANT, Direction.EAST);
        furniture(l, b, -6, 0, -7, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.WEST);
        furniture(l, b, -6, 0, -8, HouseholdFurnitureBlock.Kind.BLUE_SOFA, Direction.WEST);
        put(l, b, -7, 0, -8, Kind.COFFEE_TABLE, Direction.WEST);
        put(l, b, -6, 0, -6, Kind.FLOOR_LAMP, Direction.WEST);
        furniture(l, b, -8, 0, -5, HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, Direction.NORTH);
        furniture(l, b, -8, 0, -10, HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR, Direction.SOUTH);
        for (int x = -9; x <= -8; x++) for (int z = -9; z <= -6; z++) set(l, b, x, 0, z, BarrettHomeBlock.of(Kind.RUG_LIVING, Direction.NORTH));
        put(l, b, -9, 0, -3, Kind.TOYS, Direction.EAST);
        furniture(l, b, -8, 0, -1, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.NORTH);
        furniture(l, b, -7, 0, -1, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.NORTH);
        put(l, b, -10, 0, -1, Kind.PLANT, Direction.NORTH);
        put(l, b, -5, 0, -1, Kind.PLANT, Direction.NORTH);
        put(l, b, -5, 2, -7, Kind.PHOTO_BABY, Direction.WEST);
        put(l, b, -5, 2, -8, Kind.PHOTO_SISTERS, Direction.WEST);
    }

    /** The den the crew took for their video village. The family's sofa and their old set are pushed against the wall under dust
     *  sheets; the call sheets are on the folding table, the shooting schedule on a whiteboard, coffee and doughnuts on the craft
     *  table, the monitor and its chairs, cases, sandbags and apple boxes. */
    private static void den(ServerLevel l, BlockPos b) {
        // 0.4.220 (owner: "this should just be on the ground"): the call sheet's stand stands on the floor by the folding table.
        put(l, b, 8, 0, -1, Kind.FOLDING_TABLE, Direction.NORTH);
        put(l, b, 5, 0, -1, Kind.SANDBAGS, Direction.NORTH);
        put(l, b, 10, 0, -1, Kind.APPLE_BOXES, Direction.WEST);
        put(l, b, 10, 0, -3, Kind.SOFTBOX, Direction.WEST);
        put(l, b, 10, 0, -5, Kind.COVERED, Direction.WEST);
        put(l, b, 10, 0, -6, Kind.COVERED, Direction.WEST);
        put(l, b, 5, 2, -6, Kind.SCHEDULE_BOARD, Direction.EAST);
        put(l, b, 5, 0, -7, Kind.CRAFT_TABLE, Direction.EAST);
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
        put(l, b, 6, 0, -23, Kind.SHELVES, Direction.EAST);
        put(l, b, 6, 0, -24, Kind.SHELVES, Direction.EAST);
        put(l, b, 7, 0, -25, Kind.BOXES, Direction.SOUTH);
        put(l, b, 10, 0, -25, Kind.WATER_HEATER, Direction.WEST);
    }

    /** The open kitchen and dining room, where they eat when nobody is filming as well as when somebody is. The fridge with its
     *  magnets and a drawing, the calendar, counters under the side window; the island where the girls have their cereal, the cork
     *  board with the chore chart and the girls' backpacks on their hooks by the way in from the front room; the family computer
     *  where John reads the job listings; the sideboard; the softboxes trained on the table. */
    private static void kitchenAndDining(ServerLevel l, BlockPos b) {
        put(l, b, -10, 0, -25, Kind.FRIDGE, Direction.SOUTH);
        put(l, b, -10, 1, -25, Kind.FRIDGE_TOP, Direction.SOUTH);
        put(l, b, -10, 1, -24, Kind.CALENDAR, Direction.EAST);
        for (int z = -22; z <= -19; z++) set(l, b, -10, 0, z, GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER, Direction.EAST));
        for (int x = -8; x <= -7; x++) {
            set(l, b, x, 0, -16, GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER, Direction.SOUTH));
            furniture(l, b, x, 0, -15, HouseholdFurnitureBlock.Kind.KITCHEN_STOOL, Direction.NORTH);
        }
        put(l, b, -8, 1, -16, Kind.FRUIT_BOWL, Direction.SOUTH);
        put(l, b, -10, 1, -16, Kind.CORK_BOARD, Direction.EAST);
        put(l, b, -10, 1, -14, Kind.BACKPACKS, Direction.EAST);
        put(l, b, -5, 0, -13, Kind.PLANT, Direction.SOUTH);
        put(l, b, 4, 0, -16, Kind.DESK, Direction.WEST);
        put(l, b, 3, 0, -16, Kind.OFFICE_CHAIR, Direction.EAST);
        // 0.4.220 (owner: "more repeated clutter"): one sideboard in two halves, not two dressers side by side.
        put(l, b, 4, 0, -21, Kind.SIDEBOARD_L, Direction.WEST);
        put(l, b, 4, 0, -20, Kind.SIDEBOARD_R, Direction.WEST);
        // 0.4.220 (owner: "a kitchen with no appliances"): the stove under its hood, the dishwasher by the sink, cabinets on the walls
        // either side of the windows, the microwave, the toaster and the coffee maker on the counters.
        put(l, b, -6, 0, -25, Kind.DISHWASHER, Direction.SOUTH);
        put(l, b, -5, 0, -25, Kind.RANGE, Direction.SOUTH);
        put(l, b, -5, 2, -25, Kind.RANGE_HOOD, Direction.SOUTH);
        for (int x : new int[]{-9, -6, -4}) put(l, b, x, 2, -25, Kind.WALL_CABINET, Direction.SOUTH);
        for (int z : new int[]{-22, -19}) put(l, b, -10, 2, z, Kind.WALL_CABINET, Direction.EAST);
        put(l, b, -6, 1, -25, Kind.COFFEE_MAKER, Direction.SOUTH);
        put(l, b, -10, 1, -22, Kind.MICROWAVE, Direction.EAST);
        put(l, b, -10, 1, -19, Kind.TOASTER, Direction.EAST);
        put(l, b, 4, 0, -23, Kind.SOFTBOX, Direction.SOUTH);
        put(l, b, -3, 0, -23, Kind.SOFTBOX, Direction.SOUTH);
        furniture(l, b, 1, 0, -25, HouseholdFurnitureBlock.Kind.RADIATOR, Direction.SOUTH);
        put(l, b, 3, 0, -25, Kind.PLANT, Direction.SOUTH);
    }

    /** Upstairs: John and Sarah's room (the crucifix over the bed and their wedding picture beside it, his Bible and rosary on his
     *  side, her paperback and glasses on hers, his job listings on the desk), the landing with Merry's drawing taped by her door,
     *  and the linen closet the corridor ends in. */
    private static void upstairs(ServerLevel l, BlockPos b) {
        put(l, b, -9, 5, -6, Kind.DOUBLE_BED, Direction.EAST);
        put(l, b, -10, 5, -7, Kind.NIGHTSTAND_JOHN, Direction.EAST);
        put(l, b, -10, 5, -4, Kind.NIGHTSTAND_SARAH, Direction.EAST);
        put(l, b, -10, 7, -6, Kind.CRUCIFIX, Direction.EAST);
        put(l, b, -10, 7, -5, Kind.PHOTO_WEDDING, Direction.EAST);
        put(l, b, -10, 5, -1, Kind.DRESSER, Direction.NORTH);
        put(l, b, -6, 5, -1, Kind.DESK, Direction.NORTH);
        put(l, b, -6, 5, -2, Kind.OFFICE_CHAIR, Direction.SOUTH);
        put(l, b, -5, 5, -9, Kind.LAUNDRY, Direction.WEST);
        // 0.4.202 (owner: "hallway should end with a closet"): the corridor ends in the linen closet, towels, sheets and the board
        // games on its shelves.
        fill(l, b, 9, 5, -13, 9, 9, -12, BarrettHomeBlock.of(Kind.PAINT));
        door(l, b, 9, 5, -12, Direction.WEST);
        for (int y = 5; y <= 6; y++) for (int z = -13; z <= -12; z++) put(l, b, 10, y, z, Kind.LINEN_SHELVES, Direction.WEST);
        put(l, b, 3, 5, -11, Kind.PLANT, Direction.WEST);
        put(l, b, 5, 6, -13, Kind.MERRY_DRAWING, Direction.SOUTH);
        cable(l, b, -6, 5, -13, -1, -13);
        for (int x = -9; x <= 8; x++) set(l, b, x, 5, -12, BarrettHomeBlock.of(Kind.RUG_RUNNER_NARROW, Direction.EAST));
    }

    /** 0.4.202 (owner: "bathroom is host to a family of four. there is clutter. a cup with toothbrushes. punny artwork. a
     *  tubshower with curtains. dirty towels and clothes bins"): both bathrooms are tiled to the waist with a tile floor, a tub-
     *  shower with its curtain, toothbrushes in a cup, towels on the bar and on the floor, the laundry sorter full. The family's
     *  bathroom over the den is a wall shorter now, and the girls' between their rooms stops where its tub does. */
    private static void bathrooms(ServerLevel l, BlockPos b) {
        var wall = BarrettHomeBlock.of(Kind.PAINT);
        // The family bathroom: x 7..10, z -6..-10 (0.4.220, owner: "this bathroom is far too large. too much unused space"); the
        // rest of the old room, and the chase over the front door, are closed in behind its walls.
        fill(l, b, 5, 5, -5, 10, 9, -1, wall);
        fill(l, b, 5, 5, -10, 6, 9, -6, wall);
        tiled(l, b, 7, -10, 10, -6, -11, 7, 6, -5, -99, 11);
        put(l, b, 10, 5, -7, Kind.TUB_SHOWER, Direction.NORTH);
        put(l, b, 9, 5, -7, Kind.BATH_MAT, Direction.NORTH);
        put(l, b, 7, 5, -8, Kind.VANITY_FAMILY, Direction.EAST);
        put(l, b, 10, 5, -9, Kind.TOILET, Direction.WEST);
        put(l, b, 8, 6, -6, Kind.TOWEL_BAR, Direction.NORTH);
        put(l, b, 7, 5, -6, Kind.HAMPER, Direction.EAST);
        put(l, b, 8, 5, -6, Kind.DIRTY_TOWELS, Direction.NORTH);
        put(l, b, 9, 7, -10, Kind.PRINT_SOAP, Direction.SOUTH);
        // The girls' bathroom: x -1..2, z -15..-21, the tub across its far end.
        fill(l, b, -1, 5, -25, 2, 9, -22, wall);
        tiled(l, b, -1, -21, 2, -15, -14, 0, -2, -22, -99, 3);
        put(l, b, 0, 5, -21, Kind.TUB_SHOWER, Direction.WEST);
        put(l, b, 0, 5, -20, Kind.BATH_MAT, Direction.NORTH);
        put(l, b, 2, 5, -19, Kind.TOILET, Direction.WEST);
        put(l, b, -1, 5, -18, Kind.VANITY_KIDS, Direction.EAST);
        put(l, b, 0, 5, -18, Kind.STEP_STOOL, Direction.EAST);
        put(l, b, 2, 5, -16, Kind.HAMPER, Direction.WEST);
        put(l, b, -1, 6, -16, Kind.TOWEL_BAR, Direction.EAST);
        put(l, b, 1, 5, -16, Kind.DIRTY_TOWELS, Direction.EAST);
        put(l, b, 2, 7, -17, Kind.PRINT_WHALE, Direction.WEST);
    }
    /** A bathroom's tile: the floor under x0..x1, z0..z1 and the wainscot round it on the walls at the given lines (the door's
     *  cell in the near wall left as it is; a wall line of -99 is not tiled). */
    private static void tiled(ServerLevel l, BlockPos b, int x0, int z0, int x1, int z1, int nearZ, int doorX, int westX, int farZ, int farDoorX, int eastX) {
        fill(l, b, x0, 4, z0, x1, 4, z1, BarrettHomeBlock.of(Kind.STOREY_TILE));
        boolean nearIsNorth = nearZ < z0;
        for (int x = x0; x <= x1; x++) {
            if (x != doorX) set(l, b, x, 5, nearZ, BarrettHomeBlock.of(Kind.BATH_WALL, nearIsNorth ? Direction.SOUTH : Direction.NORTH));
            if (farZ != -99 && x != farDoorX) set(l, b, x, 5, farZ, BarrettHomeBlock.of(Kind.BATH_WALL, nearIsNorth ? Direction.NORTH : Direction.SOUTH));
        }
        for (int z = z0; z <= z1; z++) {
            set(l, b, westX, 5, z, BarrettHomeBlock.of(Kind.BATH_WALL, Direction.EAST));
            set(l, b, eastX, 5, z, BarrettHomeBlock.of(Kind.BATH_WALL, Direction.WEST));
        }
    }

    /** 0.4.202 (owner: "very little character for a young girl's bedroom", "very little character to marjorie's bedroom too"). */
    private static void girlsRooms(ServerLevel l, BlockPos b) {
        // Marjorie's room, fourteen: fairy lights strung along the wall, a band poster with half its faces scratched out, her
        // pinboard of photo-booth strips and ticket stubs over the desk, the guitar she was learning, the boombox and its CDs on
        // the floor, clothes where she dropped them, and a softbox in the corner for the reconstructions.
        put(l, b, -3, 5, -25, Kind.DESK, Direction.WEST);
        put(l, b, -4, 5, -25, Kind.OFFICE_CHAIR, Direction.EAST);
        put(l, b, -3, 7, -20, Kind.POSTER_A, Direction.WEST);
        put(l, b, -10, 7, -21, Kind.POSTER_C, Direction.EAST);
        put(l, b, -3, 6, -24, Kind.PINBOARD, Direction.WEST);
        for (int z = -24; z <= -16; z++) put(l, b, -3, 8, z, Kind.FAIRY_LIGHTS, Direction.WEST);
        put(l, b, -10, 5, -21, Kind.GUITAR, Direction.EAST);
        put(l, b, -4, 5, -23, Kind.BOOMBOX, Direction.WEST);
        put(l, b, -5, 5, -21, Kind.CLOTHES, Direction.NORTH);
        put(l, b, -8, 5, -20, Kind.CLOTHES, Direction.EAST);
        put(l, b, -3, 5, -16, Kind.SOFTBOX, Direction.WEST);
        put(l, b, -10, 5, -16, Kind.DRESSER, Direction.EAST);
        put(l, b, -10, 5, -17, Kind.BOOKSHELF, Direction.EAST);
        for (int x = -6; x <= -4; x++) for (int z = -20; z <= -18; z++) set(l, b, x, 5, z, BarrettHomeBlock.of(Kind.RUG_SHAG, Direction.NORTH));
        // Merry's room, eight: her homework desk, the planets poster, her drawings taped up in a row, the stuffed animals by her
        // bed, glow-in-the-dark stars on the ceiling, her picture books, toys on the floor, the cardboard house in the middle of it.
        put(l, b, 4, 5, -22, Kind.DESK_KID, Direction.EAST);
        put(l, b, 10, 7, -20, Kind.POSTER_B, Direction.WEST);
        put(l, b, 10, 6, -22, Kind.DRAWINGS, Direction.WEST);
        put(l, b, 10, 5, -23, Kind.STUFFED, Direction.WEST);
        put(l, b, 4, 5, -17, Kind.BOOKSHELF, Direction.EAST);
        put(l, b, 5, 5, -20, Kind.TOYS, Direction.EAST);
        put(l, b, 10, 5, -16, Kind.DRESSER, Direction.WEST);
        put(l, b, 10, 5, -15, Kind.PLANT, Direction.WEST);
        furniture(l, b, 10, 5, -25, HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, Direction.WEST);
        for (int[] c : new int[][]{{9, -23}, {9, -25}, {7, -24}, {8, -21}, {6, -22}, {9, -20}}) put(l, b, c[0], 9, c[1], Kind.CEILING_STARS, Direction.NORTH);
        // 0.4.220 (owner: "add a unique rug to mary's room", "don't recycle the same child's playmat"): her own round rug, a night sky
        // with a moon and planets like the poster on her wall; one picture over four blocks, each block's facing naming its quarter.
        set(l, b, 5, 5, -18, BarrettHomeBlock.of(Kind.MERRY_RUG, Direction.NORTH));set(l, b, 6, 5, -18, BarrettHomeBlock.of(Kind.MERRY_RUG, Direction.EAST));
        set(l, b, 6, 5, -17, BarrettHomeBlock.of(Kind.MERRY_RUG, Direction.SOUTH));set(l, b, 5, 5, -17, BarrettHomeBlock.of(Kind.MERRY_RUG, Direction.WEST));
    }

    /** The basement: the boxes nobody unpacked, the water heater, shelves. The day-four marks and the stair stay clear. */
    private static void basement(ServerLevel l, BlockPos b) {
        put(l, b, 4, -4, -15, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -17, Kind.BOXES, Direction.EAST);
        put(l, b, 4, -4, -21, Kind.SHELVES, Direction.EAST);
        put(l, b, 10, -4, -22, Kind.WATER_HEATER, Direction.WEST);
        // 0.4.220: what a family keeps in a basement, round the walls; the middle stays clear for day four.
        put(l, b, 10, -4, -15, Kind.COVERED, Direction.WEST);
        put(l, b, 4, -4, -23, Kind.BOXES, Direction.EAST);
        put(l, b, 7, -4, -25, Kind.SHELVES, Direction.SOUTH);
        put(l, b, 8, -4, -25, Kind.BOXES, Direction.SOUTH);
        put(l, b, 9, -4, -25, Kind.LAUNDRY, Direction.SOUTH);
    }
}
