package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The subtle changes themselves. Each takes the House dimension (where the
 * real manor is) and the Overworld (where its proxy shell is seen from
 * outside), changes only what nobody is looking at, keeps the two agreeing
 * where they should, and returns a line saying what it did, or null if it
 * could not happen right now.
 *
 * The game tests pass one level as both, so nothing here may assume the two
 * are different.
 */
public final class HouseShiftEffects {
    /** Everything an effect needs. {@code overworld} may be {@code interior} in tests. */
    public record Context(
            @Nullable MinecraftServer server,
            HouseSavedData data,
            ServerLevel interior,
            ServerLevel overworld,
            BlockPos origin,
            RandomSource random
    ) {
        boolean separate() {
            return interior != overworld;
        }

        BlockPos at(int x, int y, int z) {
            return origin.offset(x, y, z);
        }
    }

    /** Both halves change together, with no shape update in between to break either. */
    private static final int PAIR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** The manor's own interior doors (lower halves); the front and back doors are left alone. */
    static final List<BlockPos> INTERIOR_DOORS = List.of(
            new BlockPos(13, 1, 20), new BlockPos(17, 1, 23),
            new BlockPos(13, 7, 4), new BlockPos(13, 7, 12),
            new BlockPos(17, 7, 7), new BlockPos(17, 7, 22)
    );

    /** Single seats: stairs standing in for chairs, stools and armchairs. */
    static final List<BlockPos> CHAIRS = List.of(
            new BlockPos(4, 1, 5), new BlockPos(4, 1, 10), new BlockPos(11, 1, 2),
            new BlockPos(10, 1, 11), new BlockPos(10, 1, 13), new BlockPos(11, 1, 12),
            new BlockPos(20, 1, 3), new BlockPos(22, 1, 3), new BlockPos(21, 1, 5),
            new BlockPos(23, 1, 5), new BlockPos(19, 1, 4), new BlockPos(24, 1, 4),
            new BlockPos(3, 1, 22), new BlockPos(5, 1, 19), new BlockPos(8, 1, 20), new BlockPos(6, 1, 22),
            new BlockPos(18, 1, 24),
            new BlockPos(10, 7, 1), new BlockPos(5, 7, 7),
            new BlockPos(6, 7, 13), new BlockPos(11, 7, 11),
            new BlockPos(19, 7, 21)
    );

    private static final Map<DyeColor, Block> BEDS = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, DyeColor> NEAR_COLOUR = new EnumMap<>(DyeColor.class);

    static {
        Block[] beds = {
                Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED,
                Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED,
                Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED,
                Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED
        };
        for (Block bed : beds) {
            BEDS.put(((BedBlock) bed).getColor(), bed);
        }
        DyeColor[][] near = {
                {DyeColor.WHITE, DyeColor.LIGHT_GRAY}, {DyeColor.LIGHT_GRAY, DyeColor.GRAY},
                {DyeColor.GRAY, DyeColor.LIGHT_GRAY}, {DyeColor.BLACK, DyeColor.GRAY},
                {DyeColor.BROWN, DyeColor.RED}, {DyeColor.RED, DyeColor.BROWN},
                {DyeColor.ORANGE, DyeColor.YELLOW}, {DyeColor.YELLOW, DyeColor.ORANGE},
                {DyeColor.LIME, DyeColor.GREEN}, {DyeColor.GREEN, DyeColor.LIME},
                {DyeColor.CYAN, DyeColor.LIGHT_BLUE}, {DyeColor.LIGHT_BLUE, DyeColor.CYAN},
                {DyeColor.BLUE, DyeColor.CYAN}, {DyeColor.PURPLE, DyeColor.MAGENTA},
                {DyeColor.MAGENTA, DyeColor.PINK}, {DyeColor.PINK, DyeColor.MAGENTA}
        };
        for (DyeColor[] pair : near) {
            NEAR_COLOUR.put(pair[0], pair[1]);
        }
    }

    private HouseShiftEffects() {
    }

    // ------------------------------------------------------------------
    // Shared helpers

    private static boolean watched(Context ctx, BlockPos pos) {
        return HouseWatchers.isWatched(ctx.interior(), pos) || (ctx.separate() && HouseWatchers.isWatched(ctx.overworld(), pos));
    }

    private static void mirror(Context ctx, BlockPos pos) {
        if (ctx.separate()) {
            HouseDimensionMirror.copyStateAndBlockEntity(ctx.interior(), ctx.overworld(), pos);
        }
    }

    private static AABB envelope(BlockPos origin) {
        return new AABB(
                origin.getX() + HouseLayout.MIN_X, origin.getY() + HouseLayout.MIN_Y, origin.getZ() + HouseLayout.CLEAR_MIN_Z,
                origin.getX() + HouseLayout.MAX_X + 1, origin.getY() + HouseLayout.MAX_Y + 1, origin.getZ() + HouseLayout.MAX_Z + 1
        );
    }

    /** Every position inside the manor's rooms, for effects that look for a kind of block. */
    private static List<BlockPos> roomPositions(Context ctx) {
        List<BlockPos> positions = new ArrayList<>();
        for (HouseLayout.Room room : HouseLayout.ROOMS) {
            HouseLayout.Box box = room.box();
            for (int x = box.x0(); x <= box.x1(); x++) {
                for (int y = box.y0(); y <= box.y1(); y++) {
                    for (int z = box.z0(); z <= box.z1(); z++) {
                        positions.add(ctx.at(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    private static <T> List<T> shuffled(List<T> list, RandomSource random) {
        List<T> copy = new ArrayList<>(list);
        for (int i = copy.size() - 1; i > 0; i--) {
            Collections.swap(copy, i, random.nextInt(i + 1));
        }
        return copy;
    }

    // ------------------------------------------------------------------
    // 1. Paintings trade walls, or one hangs a block over

    @Nullable
    public static String paintings(Context ctx) {
        List<Painting> paintings = new ArrayList<>();
        for (Painting painting : ctx.interior().getEntitiesOfClass(Painting.class, envelope(ctx.origin()))) {
            if (!watched(ctx, painting.getPos())) {
                paintings.add(painting);
            }
        }
        if (paintings.isEmpty()) {
            return null;
        }
        paintings = shuffled(paintings, ctx.random());

        if (ctx.random().nextInt(10) < 7) {
            for (int i = 0; i < paintings.size(); i++) {
                for (int j = i + 1; j < paintings.size(); j++) {
                    Painting a = paintings.get(i);
                    Painting b = paintings.get(j);
                    PaintingVariant va = a.getVariant().value();
                    PaintingVariant vb = b.getVariant().value();
                    if (va.width() == vb.width() && va.height() == vb.height() && !a.getVariant().equals(b.getVariant())) {
                        BlockPos posA = a.getPos();
                        BlockPos posB = b.getPos();
                        Direction dirA = a.getDirection();
                        Direction dirB = b.getDirection();
                        swap(ctx.interior(), a, b);
                        if (ctx.separate()) {
                            Painting proxyA = paintingAt(ctx.overworld(), posA, dirA);
                            Painting proxyB = paintingAt(ctx.overworld(), posB, dirB);
                            if (proxyA != null && proxyB != null) {
                                swap(ctx.overworld(), proxyA, proxyB);
                            }
                        }
                        return "two paintings traded walls";
                    }
                }
            }
        }

        for (Painting painting : paintings) {
            BlockPos pos = painting.getPos();
            Direction facing = painting.getDirection();
            for (Direction side : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
                if (nudge(ctx.interior(), painting, side)) {
                    if (ctx.separate()) {
                        Painting proxy = paintingAt(ctx.overworld(), pos, facing);
                        if (proxy != null) {
                            nudge(ctx.overworld(), proxy, side);
                        }
                    }
                    return "a painting hangs one block over";
                }
            }
        }
        return null;
    }

    @Nullable
    private static Painting paintingAt(ServerLevel level, BlockPos pos, Direction facing) {
        for (Painting painting : level.getEntitiesOfClass(Painting.class, new AABB(pos).inflate(2.0D))) {
            if (painting.getPos().equals(pos) && painting.getDirection() == facing) {
                return painting;
            }
        }
        return null;
    }

    private static void swap(ServerLevel level, Painting a, Painting b) {
        Holder<PaintingVariant> variantA = a.getVariant();
        Holder<PaintingVariant> variantB = b.getVariant();
        a.discard();
        b.discard();
        level.addFreshEntity(new Painting(level, a.getPos(), a.getDirection(), variantB));
        level.addFreshEntity(new Painting(level, b.getPos(), b.getDirection(), variantA));
    }

    /** Moves a painting one block along its wall if it fits there; otherwise leaves it as it was. */
    private static boolean nudge(ServerLevel level, Painting painting, Direction side) {
        BlockPos pos = painting.getPos();
        Direction facing = painting.getDirection();
        Holder<PaintingVariant> variant = painting.getVariant();
        painting.discard();
        Painting moved = new Painting(level, pos.relative(side), facing, variant);
        if (moved.survives()) {
            level.addFreshEntity(moved);
            return true;
        }
        level.addFreshEntity(new Painting(level, pos, facing, variant));
        return false;
    }

    // ------------------------------------------------------------------
    // 2. The hall is one block longer

    @Nullable
    public static String deeperHall(Context ctx) {
        HouseSavedData data = ctx.data();
        if (data.isHallDeepened() || data.isImpossibleDoorRevealed()) {
            return null;
        }
        int z = HouseLayout.THRESHOLD_Z;
        BlockPos end = ctx.at(HouseLayout.AXIS_X, 2, z);
        AABB hall = new AABB(ctx.at(HouseLayout.HALL_MIN_X - 1, 0, HouseLayout.FRONT_DOOR_Z))
                .minmax(new AABB(ctx.at(HouseLayout.HALL_MAX_X + 2, 7, z + 2)));
        if (watched(ctx, end) || !ctx.interior().getEntitiesOfClass(ServerPlayer.class, hall).isEmpty()) {
            return null;
        }
        deepenHall(ctx.interior(), ctx.origin());
        data.markHallDeepened();
        return "the hall is one block longer";
    }

    /**
     * The end wall moves back a block into space only the House dimension
     * has, and the last slice of hall is repeated in front of it. From
     * outside, and in the Overworld proxy, nothing changes: that slice is
     * not mirrored until the hallway opens (see HouseShifts#isUnmirrored).
     */
    public static void deepenHall(ServerLevel level, BlockPos origin) {
        int z = HouseLayout.THRESHOLD_Z;
        for (int x = HouseLayout.HALL_MIN_X - 1; x <= HouseLayout.HALL_MAX_X + 1; x++) {
            for (int y = 0; y <= 6; y++) {
                BlockState wall = level.getBlockState(origin.offset(x, y, z));
                level.setBlock(origin.offset(x, y, z + 1), wall, PAIR_FLAGS);
            }
        }
        for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
            for (int y = 0; y <= 5; y++) {
                BlockState hall = level.getBlockState(origin.offset(x, y, z - 1));
                level.setBlock(origin.offset(x, y, z), hall, PAIR_FLAGS);
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. A door hangs from its other side; open doors are shut

    @Nullable
    public static String doors(Context ctx) {
        int closed = 0;
        for (BlockPos rel : INTERIOR_DOORS) {
            BlockPos lower = ctx.origin().offset(rel);
            BlockState state = ctx.interior().getBlockState(lower);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN) && !watched(ctx, lower)) {
                setDoor(ctx, lower, state.getValue(DoorBlock.HINGE), false);
                closed++;
            }
        }
        for (BlockPos rel : shuffled(INTERIOR_DOORS, ctx.random())) {
            BlockPos lower = ctx.origin().offset(rel);
            BlockState state = ctx.interior().getBlockState(lower);
            if (!(state.getBlock() instanceof DoorBlock) || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER || watched(ctx, lower)) {
                continue;
            }
            DoorHingeSide flipped = state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            setDoor(ctx, lower, flipped, false);
            return "a door now hangs from its other side" + (closed > 0 ? "; " + closed + " open door(s) were shut" : "");
        }
        return closed > 0 ? closed + " open door(s) were shut" : null;
    }

    private static void setDoor(Context ctx, BlockPos lower, DoorHingeSide hinge, boolean open) {
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState state = ctx.interior().getBlockState(half);
            if (state.getBlock() instanceof DoorBlock) {
                ctx.interior().setBlock(half, state.setValue(DoorBlock.HINGE, hinge).setValue(DoorBlock.OPEN, open), PAIR_FLAGS);
            }
        }
        if (ctx.separate()) {
            for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
                ctx.overworld().setBlock(half, ctx.interior().getBlockState(half), PAIR_FLAGS);
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Something taken from a chest is back

    @Nullable
    public static String chests(Context ctx, HouseMemory memory) {
        BlockPos pos = memory.restoreOne(ctx.interior(), ctx.random());
        return pos == null ? null : "something taken from a chest is back where it was";
    }

    // ------------------------------------------------------------------
    // 5. A candle nobody lit is burning

    @Nullable
    public static String candles(Context ctx) {
        List<BlockPos> unlit = new ArrayList<>();
        for (BlockPos pos : roomPositions(ctx)) {
            BlockState state = ctx.interior().getBlockState(pos);
            if (state.getBlock() instanceof AbstractCandleBlock
                    && state.hasProperty(AbstractCandleBlock.LIT)
                    && !state.getValue(AbstractCandleBlock.LIT)
                    && !(state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))) {
                unlit.add(pos);
            }
        }
        for (BlockPos pos : shuffled(unlit, ctx.random())) {
            if (watched(ctx, pos)) {
                continue;
            }
            ctx.interior().setBlock(pos, ctx.interior().getBlockState(pos).setValue(AbstractCandleBlock.LIT, true), Block.UPDATE_CLIENTS);
            mirror(ctx, pos);
            return "a candle nobody lit is burning";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 6. Something moves about in the principal bedroom (see HouseShifts' echoes)

    @Nullable
    public static String echoes(Context ctx) {
        if (ctx.data().isEchoPending()) {
            return null;
        }
        ctx.data().setEchoPending(true);
        return "the principal bedroom will be heard from, next time someone is across the house";
    }

    // ------------------------------------------------------------------
    // 7. A chair has turned to face a door

    @Nullable
    public static String chairs(Context ctx) {
        for (BlockPos rel : shuffled(CHAIRS, ctx.random())) {
            BlockPos pos = ctx.origin().offset(rel);
            BlockState state = ctx.interior().getBlockState(pos);
            if (!(state.getBlock() instanceof StairBlock) || state.getValue(StairBlock.HALF) != Half.BOTTOM || watched(ctx, pos)) {
                continue;
            }
            BlockPos target = chairTarget(ctx, rel);
            Direction current = state.getValue(StairBlock.FACING);
            // A stair's facing is its back, so to face the door it turns its back away from it.
            Direction wanted = horizontalFrom(target, rel);
            if (wanted == current) {
                continue;
            }
            Direction turned = wanted == current.getOpposite() ? current.getClockWise() : wanted;
            ctx.interior().setBlock(pos, state.setValue(StairBlock.FACING, turned), Block.UPDATE_CLIENTS);
            mirror(ctx, pos);
            return "a chair has turned to face a door";
        }
        return null;
    }

    /** Upstairs, the door to the room between rooms once it exists; otherwise the nearest door. */
    private static BlockPos chairTarget(Context ctx, BlockPos chair) {
        if (chair.getY() >= 6 && ctx.data().isRoomRevealed()) {
            return new BlockPos(ctx.data().roomDoorX(), HouseBetweenRoom.DOOR_Y, HouseBetweenRoom.WALL_Z);
        }
        List<BlockPos> doors = new ArrayList<>(INTERIOR_DOORS);
        doors.add(new BlockPos(HouseLayout.FRONT_DOOR.x(), HouseLayout.FRONT_DOOR.y(), HouseLayout.FRONT_DOOR.z()));
        BlockPos best = doors.get(0);
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos door : doors) {
            double distance = door.distSqr(chair) + (door.getY() == chair.getY() ? 0 : 400);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = door;
            }
        }
        return best;
    }

    /** The horizontal direction pointing from {@code from} towards {@code to}. */
    static Direction horizontalFrom(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // ------------------------------------------------------------------
    // 8. A note from the Navidsons on a shelf

    @Nullable
    public static String notes(Context ctx) {
        int index = HouseNotes.next(ctx.data());
        ItemStack book = HouseNotes.book(index);
        if (book == null) {
            return null;
        }
        List<BlockPos> literary = new ArrayList<>();
        List<BlockPos> elsewhere = new ArrayList<>();
        for (BlockPos pos : roomPositions(ctx)) {
            if (ctx.interior().getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity) {
                BlockPos rel = pos.subtract(ctx.origin());
                (HouseLayout.LITERARY_BEDROOM.box().contains(rel.getX(), rel.getY(), rel.getZ()) ? literary : elsewhere).add(pos);
            }
        }
        List<BlockPos> shelves = new ArrayList<>(shuffled(literary, ctx.random()));
        shelves.addAll(shuffled(elsewhere, ctx.random()));
        for (BlockPos pos : shelves) {
            if (watched(ctx, pos) || !(ctx.interior().getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity shelf)) {
                continue;
            }
            for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                if (shelf.getItem(slot).isEmpty()) {
                    shelf.setItem(slot, book);
                    shelf.setChanged();
                    mirror(ctx, pos);
                    ctx.data().markNoteWritten(index);
                    HouseNotes.Note note = HouseNotes.NOTES.get(index);
                    return "a note of " + note.author() + "'s is on a shelf (\"" + note.title() + "\")";
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 9. Someone else's bed has changed colour

    @Nullable
    public static String guestBed(Context ctx) {
        List<BlockPos> heads = new ArrayList<>();
        for (BlockPos pos : roomPositions(ctx)) {
            BlockState state = ctx.interior().getBlockState(pos);
            if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                heads.add(pos);
            }
        }
        List<ServerPlayer> players = ctx.server() == null ? List.of() : ctx.server().getPlayerList().getPlayers();
        for (BlockPos head : shuffled(heads, ctx.random())) {
            BlockState state = ctx.interior().getBlockState(head);
            BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
            if (isSomeonesBed(players, head, foot) || watched(ctx, head) || watched(ctx, foot)) {
                continue;
            }
            DyeColor current = ((BedBlock) state.getBlock()).getColor();
            DyeColor colour = homeBedColour(ctx, players);
            if (colour == null || colour == current) {
                colour = NEAR_COLOUR.getOrDefault(current, DyeColor.GRAY);
            }
            Block bed = BEDS.get(colour);
            for (ServerLevel level : ctx.separate() ? List.of(ctx.interior(), ctx.overworld()) : List.of(ctx.interior())) {
                for (BlockPos half : new BlockPos[]{head, foot}) {
                    BlockState old = level.getBlockState(half);
                    if (old.getBlock() instanceof BedBlock) {
                        level.setBlock(half, bed.defaultBlockState()
                                .setValue(BedBlock.FACING, old.getValue(BedBlock.FACING))
                                .setValue(BedBlock.PART, old.getValue(BedBlock.PART)), PAIR_FLAGS);
                    }
                }
            }
            return "a bed nobody sleeps in is now " + colour.getName().replace('_', ' ');
        }
        return null;
    }

    private static boolean isSomeonesBed(List<ServerPlayer> players, BlockPos head, BlockPos foot) {
        for (ServerPlayer player : players) {
            BlockPos respawn = player.getRespawnPosition();
            if (respawn != null && HouseDimensions.INTERIOR.equals(player.getRespawnDimension())
                    && (respawn.equals(head) || respawn.equals(foot))) {
                return true;
            }
        }
        return false;
    }

    /** The colour of a player's own bed at home, if one is loaded: the guest bed takes it. */
    @Nullable
    private static DyeColor homeBedColour(Context ctx, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            BlockPos respawn = player.getRespawnPosition();
            if (respawn != null && Level.OVERWORLD.equals(player.getRespawnDimension())
                    && ctx.overworld().isLoaded(respawn)
                    && ctx.overworld().getBlockState(respawn).getBlock() instanceof BedBlock home) {
                return home.getColor();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 10. An upper window is lit at night, from outside only

    @Nullable
    public static String window(Context ctx) {
        List<ServerPlayer> players = ctx.server() == null ? List.of() : ctx.server().getPlayerList().getPlayers();
        Vec3 centre = Vec3.atCenterOf(ctx.at(HouseLayout.CENTER_X, 8, HouseLayout.CENTER_Z));
        Vec3 home = null;
        for (ServerPlayer player : players) {
            BlockPos respawn = player.getRespawnPosition();
            if (respawn != null && Level.OVERWORLD.equals(player.getRespawnDimension())) {
                home = Vec3.atCenterOf(respawn);
                break;
            }
        }
        Vec3 toHome = home == null ? null : home.subtract(centre).multiply(1.0D, 0.0D, 1.0D).normalize();

        List<HouseLayout.Window> upper = new ArrayList<>();
        for (HouseLayout.Window window : HouseLayout.WINDOWS) {
            if (window.y0() >= 7) {
                upper.add(window);
            }
        }
        upper = shuffled(upper, ctx.random());
        if (toHome != null) {
            Vec3 bearing = toHome;
            upper.sort((a, b) -> Double.compare(outward(b).dot(bearing), outward(a).dot(bearing)));
        }

        BlockPos previous = ctx.data().windowLight();
        for (HouseLayout.Window window : upper) {
            BlockPos light = lightBehind(ctx, window);
            if (light == null || light.equals(previous)) {
                continue;
            }
            if (previous != null && ctx.overworld().getBlockState(previous).is(Blocks.LIGHT)) {
                ctx.overworld().setBlock(previous, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            ctx.overworld().setBlock(light, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 14), Block.UPDATE_ALL);
            ctx.data().setWindowLight(light);
            HouseShifts.refreshCache(ctx.data());
            return "an upstairs window is lit at night, seen from outside (" + window.face().name().toLowerCase() + " side)";
        }
        return null;
    }

    private static Vec3 outward(HouseLayout.Window window) {
        return switch (window.face()) {
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0, 1);
            case EAST -> new Vec3(1, 0, 0);
            case WEST -> new Vec3(-1, 0, 0);
        };
    }

    /** The first empty cell just inside a window's middle pane, in the Overworld proxy. */
    @Nullable
    private static BlockPos lightBehind(Context ctx, HouseLayout.Window window) {
        int along = (window.a0() + window.a1()) / 2;
        BlockPos pane = ctx.at(window.x(along), window.y0(), window.z(along));
        Direction inward = switch (window.face()) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
        for (int depth = 1; depth <= 2; depth++) {
            BlockPos candidate = pane.relative(inward, depth);
            if (ctx.overworld().getBlockState(candidate).isAir()) {
                return candidate;
            }
        }
        return null;
    }
}
