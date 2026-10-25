package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.LifeStageBlock;
import io.github.knaitoe.theoldesthouse.house.LifeStageBlock.Kind;
import io.github.knaitoe.theoldesthouse.house.RugFloorBlock;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Wheel as a life (0.4.121). Behind the front door the numbered rooms are the stages of a daughter's life: her nursery,
 * her childhood room, her teenage room, her first flat, her own daughter's nursery and the hospital room where her mother
 * lay. The first two are built at twice the size and make the reader a child in them, going forward. Every room has two
 * doors, each with the number of the room it opens on: back the way she came, on to the next. The sixth room's door says 1,
 * and it goes round to the nursery: the wheel. A reader who has gone back through a door and seen all six finds that door
 * opens on Mother's room instead, farther in than the numbers suggest; there they are small again, and her door, the one
 * with no number, lets them out to the hall at their own height.
 *
 * <p>Going back is the source's hint ("When I went back through two, the chair in one was smaller"): the child's chair in
 * the first two rooms is drawn at its ordinary size to a reader who comes back into them grown. Each room keeps one short
 * text. All of it is the reader's own: rooms are reached by doors, not walked between, and the shrunken chair is only
 * ever sent to that reader.
 */
public final class WheelHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final LabyrinthPlace PLACE = LabyrinthPlace.WHEEL;
    /** A room's interior (inclusive), the height of its ceiling, whether it makes a child of a reader going forward, and the x of its doors. */
    public record Room(int id, int x0, int z0, int x1, int z1, int ceiling, boolean child, int doorX) {
        /** The back door, in the south wall: the way she came. */
        public BlockPos back() { return new BlockPos(doorX, 0, z1 + 1); }
        /** The door on, in the north wall. */
        public BlockPos forward() { return new BlockPos(doorX, 0, z0 - 1); }
        /** Just inside the back door, facing in. */
        Vec3 fromBack() { return new Vec3(doorX + .5, 0, z1 + .5); }
        /** Just inside the door on, facing in. */
        Vec3 fromForward() { return new Vec3(doorX + .5, 0, z0 + .5); }
        /** The room, with the niches of its two doors. */
        public boolean contains(Vec3 rel) { return rel.x >= x0 && rel.x < x1 + 1 && rel.z >= z0 - 1 && rel.z < z1 + 2 && rel.y > -1 && rel.y < ceiling; }
    }
    public static final List<Room> ROOMS = List.of(
            new Room(1, -24, -22, -9, -9, 8, true, -16),
            new Room(2, 9, -22, 24, -9, 8, true, 16),
            new Room(3, -21, -36, -12, -28, 4, false, -16),
            new Room(4, 11, -37, 22, -28, 4, false, 16),
            new Room(5, -21, -50, -12, -42, 4, false, -16),
            new Room(6, 12, -50, 21, -42, 4, false, 16));
    public static final Room MOTHERS = new Room(7, -6, -62, 6, -53, 5, true, 0);
    /** The hall inside the front door, and the front door itself, which has no number. */
    public static final int HALL_X0 = -4, HALL_X1 = 4, HALL_Z0 = -6, HALL_Z1 = -1;
    public static final BlockPos FRONT_DOOR = new BlockPos(0, 0, -7);
    static final Vec3 HALL_LANDING = new Vec3(.5, 0, -5.5);
    /** The daughter's notebook on the hall table (the source) and the last account on the other side of the hall. */
    public static final BlockPos NOTEBOOK = new BlockPos(-3, 0, -3), ACCOUNT = new BlockPos(3, 0, -3);
    /** Where Mother stands, by her window. */
    static final BlockPos MOTHER = new BlockPos(3, 0, -57);
    /** Each room's one short text: where it lies, and what it says. */
    public static final List<BlockPos> TEXT_AT = List.of(new BlockPos(-11, 2, -22), new BlockPos(9, 2, -15), new BlockPos(-21, 0, -30),
            new BlockPos(20, 0, -30), new BlockPos(-12, 1, -46), new BlockPos(20, 0, -46));
    static final String[] TEXTS = {
            "The baby book, in Mother's hand: \"Height at one year, twenty-nine inches. She reaches the second drawer. I have marked it on the door frame.\"",
            "Crayon, in capitals: ME AND MUM. Mum is taller than the house. I come up to her pocket, and there is a sweet in the pocket.",
            "The diary's clasp is broken. \"She read this. She says she didn't. I am taller than her now and it makes no difference at all.\"",
            "Mother's letter: \"You took the good saucepan. Keep it. Ring on Sundays. The house is very big with only me in it.\"",
            "The baby book, in my hand: \"Height at one year, twenty-nine inches. Mother came and said she has my chin, and measured her against the door.\"",
            "The chart on the foot of the bed. Under next of kin, my name, and beside it in pencil: daughter, visits."};
    /** The child's chair in the first two rooms, lower and upper cells. */
    public static final List<BlockPos> BIG_CHAIRS = List.of(new BlockPos(-13, 0, -12), new BlockPos(11, 0, -20));
    private WheelHouse() {}

    public static @Nullable Room room(int id) { return id == 7 ? MOTHERS : id >= 1 && id <= 6 ? ROOMS.get(id - 1) : null; }

    // ------------------------------------------------------------------------------------------------ building

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void set(ServerLevel l, BlockPos b, BlockPos r, BlockState s) { BuildBlocks.set(l, b.offset(r), s, F); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) set(l, b, x, y, z, s);
    }
    private static void piece(ServerLevel l, BlockPos b, int x, int y, int z, Kind k, Direction f) { set(l, b, x, y, z, LifeStageBlock.of(k, f)); }
    private static void light(ServerLevel l, BlockPos b, int x, int y, int z, int level) { set(l, b, x, y, z, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, level)); }

    /** A room's box: walls, floor and ceiling of its own materials, and the air inside. */
    private static void shell(ServerLevel l, BlockPos b, Room r, BlockState walls, BlockState floor, BlockState ceiling) {
        fill(l, b, r.x0 - 1, -1, r.z0 - 1, r.x1 + 1, r.ceiling, r.z1 + 1, walls);
        fill(l, b, r.x0, -1, r.z0, r.x1, -1, r.z1, floor);
        fill(l, b, r.x0, r.ceiling, r.z0, r.x1, r.ceiling, r.z1, ceiling);
        fill(l, b, r.x0, 0, r.z0, r.x1, r.ceiling - 1, r.z1, Blocks.AIR.defaultBlockState());
    }

    /** A door with a white frame, and the number of the room it opens on above it, on the room's side. */
    private static void door(ServerLevel l, BlockPos b, BlockPos at, Direction into, int number) {
        set(l, b, at, Blocks.AIR.defaultBlockState());set(l, b, at.above(), Blocks.AIR.defaultBlockState());
        NovelRooms.door(l, b.offset(at), into, Blocks.BIRCH_DOOR, false);
        var across = into.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.EAST;
        for (int a : new int[]{-1, 1}) for (int dy = 0; dy <= 2; dy++) set(l, b, at.relative(across, a).above(dy), Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
        set(l, b, at.above(2), Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
        if (number > 0) piece(l, b, at.getX() + into.getStepX(), 2, at.getZ() + into.getStepZ(), Kind.number(number), into);
    }

    /** One dark window in a wall, with nothing but night behind it. */
    private static void window(ServerLevel l, BlockPos b, int x, int z, Direction out) {
        for (int y = 1; y <= 2; y++) {
            set(l, b, x, y, z, Blocks.GLASS_PANE.defaultBlockState());
            set(l, b, x + out.getStepX(), y, z + out.getStepZ(), Blocks.BLACK_CONCRETE.defaultBlockState());
        }
    }

    public static void build(ServerLevel l, BlockPos b) {
        var r = PLACE.room();
        // Everything between the rooms is solid: they are reached by their doors, never walked between.
        fill(l, b, r.minX() + 1, -1, r.minZ() + 1, r.maxX() - 1, 9, r.maxZ(), Blocks.STONE.defaultBlockState());
        var white = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        // The hall inside the front door: papered, a runner to the door, the notebook on its table.
        Room hall = new Room(0, HALL_X0, HALL_Z0, HALL_X1, HALL_Z1, 4, false, 0);
        shell(l, b, hall, HouseBlocks.LIFE_HALL_PAPER.get().defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), white);
        fill(l, b, -1, -1, HALL_Z0, 1, -1, HALL_Z1, HouseBlocks.LIFE_HALL_RUNNER.get().defaultBlockState());
        NovelRooms.door(l, b.offset(FRONT_DOOR), Direction.SOUTH, Blocks.DARK_OAK_DOOR, false);
        set(l, b, FRONT_DOOR.above(2), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(l, b, NOTEBOOK, LifeStageBlock.of(Kind.NOTEBOOK, Direction.EAST));
        set(l, b, 0, 3, -3, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        for (int z : new int[]{-5, -2}) { set(l, b, -4, 1, z, Blocks.OAK_FENCE.defaultBlockState()); set(l, b, 4, 1, z, Blocks.OAK_FENCE.defaultBlockState()); }

        // 1. The nursery, at twice the size: the crib against the west wall, its mobile, the drawers, toys and the chair.
        var r1 = ROOMS.get(0);
        shell(l, b, r1, HouseBlocks.LIFE_NURSERY_PAPER.get().defaultBlockState(), HouseBlocks.LIFE_NURSERY_CARPET.get().defaultBlockState(), white);
        crib(l, b, -24, -18);
        piece(l, b, -24, 4, -17, Kind.MOBILE_BIG, Direction.NORTH);
        for (int y = 5; y < r1.ceiling; y++) piece(l, b, -24, y, -17, Kind.MOBILE_STRING, Direction.NORTH);
        for (int x = -12; x <= -11; x++) for (int y = 0; y <= 1; y++) piece(l, b, x, y, -22, Kind.DRAWERS, Direction.SOUTH);
        piece(l, b, -12, 2, -22, Kind.LAMP, Direction.SOUTH);piece(l, b, -11, 2, -22, Kind.BABY_BOOK, Direction.SOUTH);
        piece(l, b, -19, 0, -12, Kind.TOY_A, Direction.NORTH);piece(l, b, -19, 1, -12, Kind.TOY_C, Direction.EAST);piece(l, b, -17, 0, -13, Kind.TOY_B, Direction.WEST);
        piece(l, b, -21, 0, -20, Kind.TOY_B, Direction.SOUTH);piece(l, b, -14, 0, -16, Kind.TOY_C, Direction.NORTH);
        piece(l, b, -24, 0, -20, Kind.NIGHT_LIGHT, Direction.EAST);
        rug(l, b, -20, -14, -16, -11, RugFloorBlock.Tone.GRAY);
        bigChair(l, b, BIG_CHAIRS.get(0), Direction.WEST);
        for (int x : new int[]{-20, -13}) for (int z : new int[]{-18, -12}) light(l, b, x, r1.ceiling - 1, z, 12);

        // 2. The childhood room, at twice the size: yellow paper, the big bed, crayons, a drawing, glow stars overhead.
        var r2 = ROOMS.get(1);
        shell(l, b, r2, HouseBlocks.LIFE_CHILD_PAPER.get().defaultBlockState(), Blocks.BIRCH_PLANKS.defaultBlockState(), HouseBlocks.LIFE_STAR_CEILING.get().defaultBlockState());
        for (int x = 20; x <= 21; x++) {
            piece(l, b, x, 0, -22, Kind.BIGBED_HEAD, Direction.SOUTH);piece(l, b, x, 1, -22, Kind.BIGBED_HEADBOARD, Direction.SOUTH);
            piece(l, b, x, 0, -21, Kind.BIGBED_MID, Direction.SOUTH);piece(l, b, x, 0, -20, Kind.BIGBED_FOOT, Direction.SOUTH);
        }
        piece(l, b, 23, 0, -22, Kind.NIGHT_LIGHT, Direction.SOUTH);
        piece(l, b, 9, 2, -15, Kind.DRAWING, Direction.EAST);
        piece(l, b, 13, 0, -13, Kind.CRAYON_RED, Direction.NORTH);piece(l, b, 14, 0, -11, Kind.CRAYON_BLUE, Direction.EAST);
        piece(l, b, 18, 0, -12, Kind.TOY_B, Direction.SOUTH);
        rug(l, b, 13, 19, -17, -12, RugFloorBlock.Tone.RED);
        bigChair(l, b, BIG_CHAIRS.get(1), Direction.EAST);
        for (int x : new int[]{12, 21}) for (int z : new int[]{-18, -12}) light(l, b, x, r2.ceiling - 1, z, 7);

        // 3. The teenage room: violet walls, the iron bed, the diary forced open, a stereo, posters, a sign on the door.
        var r3 = ROOMS.get(2);
        shell(l, b, r3, HouseBlocks.LIFE_TEEN_PAINT.get().defaultBlockState(), HouseBlocks.LIFE_TEEN_CARPET.get().defaultBlockState(), white);
        piece(l, b, -21, 0, -31, Kind.TEEN_BED_HEAD, Direction.EAST);piece(l, b, -20, 0, -31, Kind.TEEN_BED_FOOT, Direction.EAST);
        piece(l, b, -21, 0, -30, Kind.DIARY, Direction.EAST);piece(l, b, -21, 0, -32, Kind.LAVA_LAMP, Direction.EAST);
        piece(l, b, -12, 0, -34, Kind.STEREO, Direction.WEST);piece(l, b, -14, 0, -30, Kind.CLOTHES, Direction.NORTH);
        piece(l, b, -19, 1, -36, Kind.POSTER_BAND, Direction.SOUTH);piece(l, b, -13, 1, -36, Kind.POSTER_MOON, Direction.SOUTH);
        piece(l, b, -15, 1, -28, Kind.KEEP_OUT, Direction.NORTH);piece(l, b, -18, 0, -29, Kind.CHAIR_SMALL, Direction.NORTH);
        window(l, b, -11, -32, Direction.EAST);rug(l, b, -18, -15, -34, -31, RugFloorBlock.Tone.SLATE);
        light(l, b, -16, r3.ceiling - 1, -32, 11);

        // 4. The first flat: bare boards, a mattress on the floor, boxes not yet unpacked, a hot plate, a bulb, her letter.
        var r4 = ROOMS.get(3);
        shell(l, b, r4, HouseBlocks.LIFE_APARTMENT_WALL.get().defaultBlockState(), HouseBlocks.LIFE_APARTMENT_BOARDS.get().defaultBlockState(), white);
        piece(l, b, 22, 0, -31, Kind.MATTRESS_HEAD, Direction.WEST);piece(l, b, 21, 0, -31, Kind.MATTRESS_FOOT, Direction.WEST);
        piece(l, b, 12, 0, -37, Kind.BOX, Direction.SOUTH);piece(l, b, 12, 1, -37, Kind.BOX, Direction.EAST);piece(l, b, 13, 0, -37, Kind.BOX, Direction.SOUTH);
        piece(l, b, 14, 0, -37, Kind.BOX_KITCHEN, Direction.SOUTH);piece(l, b, 20, 0, -37, Kind.BOX_OPEN, Direction.SOUTH);
        piece(l, b, 11, 0, -32, Kind.HOT_PLATE, Direction.EAST);piece(l, b, 16, r4.ceiling - 1, -32, Kind.BULB, Direction.NORTH);
        piece(l, b, 20, 0, -30, Kind.LETTER, Direction.NORTH);piece(l, b, 13, 0, -30, Kind.CHAIR_SMALL, Direction.EAST);
        window(l, b, 10, -33, Direction.WEST);

        // 5. Her own daughter's nursery: the same paper, a crib she leans over, the chair at its own size.
        var r5 = ROOMS.get(4);
        shell(l, b, r5, HouseBlocks.LIFE_NURSERY_PAPER.get().defaultBlockState(), HouseBlocks.LIFE_NURSERY_CARPET.get().defaultBlockState(), white);
        piece(l, b, -21, 0, -45, Kind.CRIB_END, Direction.NORTH);piece(l, b, -21, 0, -46, Kind.CRIB_END, Direction.SOUTH);
        piece(l, b, -21, r5.ceiling - 1, -46, Kind.MOBILE, Direction.NORTH);
        piece(l, b, -12, 0, -47, Kind.DRAWERS, Direction.WEST);piece(l, b, -12, 1, -47, Kind.LAMP, Direction.WEST);
        piece(l, b, -12, 0, -46, Kind.DRAWERS, Direction.WEST);piece(l, b, -12, 1, -46, Kind.BABY_BOOK, Direction.WEST);
        piece(l, b, -14, 0, -44, Kind.CHAIR_SMALL, Direction.WEST);
        light(l, b, -16, r5.ceiling - 1, -46, 11);

        // 6. The hospital room: green tile, the bed with its chart, the drip, the monitor, carnations, a visitor's chair.
        var r6 = ROOMS.get(5);
        shell(l, b, r6, HouseBlocks.LIFE_HOSPITAL_TILE.get().defaultBlockState(), HouseBlocks.LIFE_HOSPITAL_LINO.get().defaultBlockState(), white);
        piece(l, b, 21, 0, -46, Kind.HOSP_BED_HEAD, Direction.WEST);piece(l, b, 20, 0, -46, Kind.HOSP_BED_FOOT, Direction.WEST);
        piece(l, b, 21, 0, -47, Kind.IV_LOWER, Direction.WEST);piece(l, b, 21, 1, -47, Kind.IV_UPPER, Direction.WEST);
        piece(l, b, 21, 0, -45, Kind.MONITOR, Direction.WEST);piece(l, b, 21, 0, -44, Kind.CARNATIONS, Direction.WEST);
        piece(l, b, 18, 0, -46, Kind.VISITOR_CHAIR, Direction.EAST);piece(l, b, 13, 0, -43, Kind.CHAIR_SMALL, Direction.NORTH);
        window(l, b, 11, -46, Direction.WEST);
        for (int z : new int[]{-48, -44}) light(l, b, 16, r6.ceiling - 1, z, 12);

        // 7. Mother's room: rose paper, her bed and wardrobe, a lamp, the chair once more, and one door with no number.
        var r7 = MOTHERS;
        shell(l, b, r7, HouseBlocks.LIFE_MOTHER_PAPER.get().defaultBlockState(), HouseBlocks.LIFE_MOTHER_CARPET.get().defaultBlockState(), white);
        piece(l, b, 0, 0, -62, Kind.MOTHER_BED_HEAD, Direction.SOUTH);piece(l, b, 0, 0, -61, Kind.MOTHER_BED_FOOT, Direction.SOUTH);
        piece(l, b, -5, 0, -62, Kind.WARDROBE_LOWER, Direction.SOUTH);piece(l, b, -5, 1, -62, Kind.WARDROBE_UPPER, Direction.SOUTH);
        piece(l, b, 2, 0, -62, Kind.DRAWERS, Direction.SOUTH);piece(l, b, 2, 1, -62, Kind.LAMP, Direction.SOUTH);
        piece(l, b, -2, 0, -59, Kind.CHAIR_SMALL, Direction.EAST);
        NovelRooms.furniture(l, b.offset(5, 0, -57), HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR, Direction.WEST);
        window(l, b, 4, -63, Direction.NORTH);rug(l, b, -2, 2, -60, -57, RugFloorBlock.Tone.RED);
        light(l, b, 0, r7.ceiling - 1, -57, 10);

        // The doors: each numbered for the room it opens on. The first room's back door and the hall's front door have no number.
        for (var room : ROOMS) {
            door(l, b, room.back(), Direction.NORTH, room.id() == 1 ? 0 : room.id() - 1);
            door(l, b, room.forward(), Direction.SOUTH, room.id() == 6 ? 1 : room.id() + 1);
        }
        door(l, b, MOTHERS.back(), Direction.NORTH, 0);
    }

    /** The nursery crib at twice the size: two cells wide and three long against the west wall, its rails two cells high. */
    private static void crib(ServerLevel l, BlockPos b, int x, int z) {
        // Corners: (west, north) faces east; (east, north) south; (east, south) west; (west, south) north. Edges face the long side.
        int[][] corners = {{x, z, 1}, {x + 1, z, 2}, {x + 1, z + 2, 3}, {x, z + 2, 0}};
        Direction[] turn = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] c : corners) { piece(l, b, c[0], 0, c[1], Kind.CRIB_BIG_CORNER, turn[c[2]]); piece(l, b, c[0], 1, c[1], Kind.CRIB_BIG_RAIL_CORNER, turn[c[2]]); }
        piece(l, b, x, 0, z + 1, Kind.CRIB_BIG_EDGE, Direction.EAST);piece(l, b, x, 1, z + 1, Kind.CRIB_BIG_RAIL, Direction.EAST);
        piece(l, b, x + 1, 0, z + 1, Kind.CRIB_BIG_EDGE, Direction.WEST);piece(l, b, x + 1, 1, z + 1, Kind.CRIB_BIG_RAIL, Direction.WEST);
    }

    /** A rug laid into the floor, its edge drawn as the household rugs are. */
    private static void rug(ServerLevel l, BlockPos b, int x0, int x1, int z0, int z1, RugFloorBlock.Tone tone) {
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
            String ns = z == z0 ? "north" : z == z1 ? "south" : "", ew = x == x0 ? "west" : x == x1 ? "east" : "";
            var rim = ns.isEmpty() ? (ew.isEmpty() ? RugFloorBlock.Rim.CENTER : RugFloorBlock.Rim.valueOf(ew.toUpperCase(java.util.Locale.ROOT)))
                    : RugFloorBlock.Rim.valueOf((ns + (ew.isEmpty() ? "" : "_" + ew)).toUpperCase(java.util.Locale.ROOT));
            set(l, b, x, -1, z, HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE, tone).setValue(RugFloorBlock.RIM, rim));
        }
    }

    private static void bigChair(ServerLevel l, BlockPos b, BlockPos at, Direction f) {
        set(l, b, at, LifeStageBlock.of(Kind.CHAIR_BIG_LOWER, f));set(l, b, at.above(), LifeStageBlock.of(Kind.CHAIR_BIG_UPPER, f));
    }

    // ------------------------------------------------------------------------------------------------ the wheel

    /** A new crossing starts in the hall, at full height. */
    public static void arrive(CompoundTag own) {
        own.putInt("WheelRoom", 0);
        own.putBoolean("Memory", false);
    }

    /** The right-clicks the Wheel answers: its doors and each room's text. The caller saves the record. */
    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        var l = p.serverLevel();
        if (rel.getY() == 1 && l.getBlockState(b.offset(rel)).getBlock() instanceof DoorBlock) rel = rel.below();
        int text = TEXT_AT.indexOf(rel);
        if (text >= 0) {
            p.displayClientMessage(Component.literal(TEXTS[text]).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            own.putInt("WheelTexts", own.getInt("WheelTexts") | 1 << text);
            return true;
        }
        if (rel.equals(FRONT_DOOR)) {
            // The front door takes her to the beginning, until she has found Mother; then it is only a door.
            if (own.getBoolean("LeftMother")) return false;
            if (go(p, b, own, 1, true)) own.putBoolean("FrontLoop", true);
            return true;
        }
        if (rel.equals(MOTHERS.back())) {
            if (!own.getBoolean("LeftMother")) { p.displayClientMessage(Component.literal("It does not open yet."), true); return true; }
            var at = landing(p, b, HALL_LANDING, Direction.SOUTH);
            if (at != null) { teleport(p, at, 0); own.putInt("WheelRoom", 0); own.putBoolean("Memory", false); }
            return true;
        }
        for (var room : ROOMS) {
            if (rel.equals(room.forward())) {
                int next = room.id() + 1;
                if (room.id() == 6) next = own.getBoolean("WentBack") && (own.getInt("WheelSeen") & 0b1111110) == 0b1111110 ? 7 : 1;
                go(p, b, own, next, true);
                return true;
            }
            if (rel.equals(room.back())) {
                if (room.id() == 1) {
                    var at = landing(p, b, HALL_LANDING, Direction.SOUTH);
                    if (at != null) { teleport(p, at, 0); own.putInt("WheelRoom", 0); own.putBoolean("Memory", false); }
                } else if (go(p, b, own, room.id() - 1, false)) own.putBoolean("WentBack", true);
                return true;
            }
        }
        return false;
    }

    /** Through a door into a room: forward lands inside its back door, back inside its door on. */
    private static boolean go(ServerPlayer p, BlockPos b, CompoundTag own, int id, boolean forward) {
        var room = room(id);
        if (room == null) return false;
        var target = forward ? room.fromBack() : room.fromForward();
        var at = landing(p, b, target, forward ? Direction.NORTH : Direction.SOUTH);
        if (at == null) return false;
        teleport(p, at, forward ? 180 : 0);
        own.putInt("WheelRoom", id);
        if (id <= 6) own.putInt("WheelSeen", own.getInt("WheelSeen") | 1 << id);
        // A child going forward into the first rooms, and again before Mother; at her own height going back.
        own.putBoolean("Memory", room.child() && forward);
        if (id == 7) own.putBoolean("ReachedMother", true);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, .6F, .9F);
        if (id <= 2) chairs(p, b, own);
        return true;
    }

    private static void teleport(ServerPlayer p, Vec3 at, float yaw) {
        p.teleportTo(p.serverLevel(), at.x, at.y, at.z, yaw, 0);
        p.setDeltaMovement(Vec3.ZERO);
    }

    /** Clear, loaded floor at a landing, or one step further in. */
    private static @Nullable Vec3 landing(ServerPlayer p, BlockPos b, Vec3 rel, Direction in) {
        var level = p.serverLevel();
        var target = rel.add(b.getX(), b.getY(), b.getZ());
        for (int step = 0; step <= 1; step++) {
            var at = target.add(in.getStepX() * step, 0, in.getStepZ() * step);
            var cell = BlockPos.containing(at);
            if (!level.hasChunkAt(cell) || !level.areEntitiesLoaded(ChunkPos.asLong(cell.getX() >> 4, cell.getZ() >> 4))) continue;
            if (level.getBlockState(cell.below()).getCollisionShape(level, cell.below()).isEmpty()) continue;
            if (level.noCollision(p, p.getBoundingBox().move(at.subtract(p.position())))) return at;
        }
        p.displayClientMessage(Component.literal("There is no clear space beyond this door yet."), true);
        return null;
    }

    /** The reader's room, from where they stand. */
    public static int roomAt(Vec3 rel) {
        for (var room : ROOMS) if (room.contains(rel)) return room.id();
        if (MOTHERS.contains(rel)) return 7;
        return 0;
    }

    /** The scene tick for one reader: Mother, the chair, and the way out. */
    public static void reader(ServerPlayer p, BlockPos b, CompoundTag own) {
        var mother = LiteraryVignettes.actor(p, PLACE, "Mother", LiteraryActor.MOTHER, MOTHER, true);
        if (own.getBoolean("ReachedMother") && !own.getBoolean("LeftMother") && mother != null && p.distanceToSqr(mother) < 36 && HouseWatchers.sees(p, mother.getEyePosition())) {
            own.putBoolean("LeftMother", true);
            mother.say("There you are. You can leave now. The door will open.");
        }
        int here = roomAt(p.position().subtract(b.getX(), b.getY(), b.getZ()));
        // Somebody who walked out of their room some other way is their own height outside the rooms that make them small.
        if (here == 0 || here >= 3 && here <= 6) own.putBoolean("Memory", false);
        if ((here == 1 || here == 2) && !own.getBoolean("Memory") && own.getInt("Present") % 40 == 0) chairs(p, b, own);
        double rx = p.getX() - b.getX(), rz = p.getZ() - b.getZ();
        if (own.getBoolean("LeftMother") && rx >= HALL_X0 && rx < HALL_X1 + 1 && rz > HALL_Z0 && rz < 1) {
            own.putBoolean("Memory", false);
            LiteraryVignettes.ready(p, PLACE, own, "left_after_finding_mothers_room");
        }
    }

    /**
     * The child's chair as this reader should see it: at twice the size to a child, at its own size to a reader who came
     * back into the room grown. Only their client is told.
     */
    static void chairs(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();
        boolean small = !own.getBoolean("Memory");
        for (var rel : BIG_CHAIRS) {
            var lower = b.offset(rel);
            var real = l.getBlockState(lower);
            if (!LifeStageBlock.is(real, Kind.CHAIR_BIG_LOWER)) continue;
            var f = real.getValue(LifeStageBlock.FACING);
            p.connection.send(new ClientboundBlockUpdatePacket(lower, small ? LifeStageBlock.of(Kind.CHAIR_SMALL, f) : real));
            p.connection.send(new ClientboundBlockUpdatePacket(lower.above(), small ? Blocks.AIR.defaultBlockState() : l.getBlockState(lower.above())));
        }
    }
}
