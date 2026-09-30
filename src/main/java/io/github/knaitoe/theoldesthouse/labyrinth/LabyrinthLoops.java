package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HotelRoomPlaqueBlock;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HotelRoomNumbersPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Seamless loops: identical copies of a stretch of the house, and a
 * same-tick relative shift from one to the next that keeps the player's
 * spot, facing and momentum. Nobody can see it happen, because every copy
 * looks the same and each one bends before the view could reach anything
 * that isn't.
 *
 * <ul>
 *   <li><b>The Five and a Half Minute Hallway.</b> A gray corridor that jogs
 *   aside every twelve blocks. Walking on loops back a period until five and
 *   a half minutes have passed since entering; only then does the far door
 *   come. Sprinting, speed and ender pearls change nothing. Walking back
 *   unwinds only the laps already walked, so the space is always
 *   consistent. Anything placed in it or dropped in it is gone on the next
 *   pass.</li>
 *   <li><b>The hotel hallway.</b> The same bends, in carpet and wallpaper,
 *   with locked numbered doors. The numbers climb as you walk on, and
 *   repeat; forward never ends. The way out is to walk against the rising
 *   numbers.</li>
 *   <li><b>The spiral staircase.</b> A slab stair around an open well, four
 *   blocks a turn. Climbing repeats a turn several times over; descending
 *   never does, so it takes longer to climb than to come down. Anything
 *   dropped down the well vanishes before it lands.</li>
 * </ul>
 *
 * All three are gray places, dealt like the junction and the corridor.
 */
public final class LabyrinthLoops {
    /** Five and a half minutes. */
    public static final long LONG_HALLWAY_TICKS = 330L * 20L;
    /** How many extra turns climbing the spiral takes. */
    public static final int SPIRAL_EXTRA_TURNS = 4;
    /** One period of a jogging hallway: twelve blocks on, three aside. */
    public static final BlockPos PERIOD = new BlockPos(3, 0, -12);
    /** Where the first period starts, relative to the place's base. */
    public static final BlockPos FIRST_PERIOD = new BlockPos(3, 0, -3);
    public static final int STRAIGHT = 9;
    /** One turn of the spiral. */
    public static final int TURN = 4;
    public static final int SPIRAL_TURNS = 4;
    public static final int HOTEL_FIRST_ROOM = 201;
    public static final int HOTEL_ROOMS = 24;

    /** The spiral's steps, one per column around the well, starting at the south and climbing east. */
    public static final List<BlockPos> RING = List.of(
            new BlockPos(0, 0, -1), new BlockPos(1, 0, -1), new BlockPos(1, 0, -2), new BlockPos(1, 0, -3),
            new BlockPos(0, 0, -3), new BlockPos(-1, 0, -3), new BlockPos(-1, 0, -2), new BlockPos(-1, 0, -1));
    public static final BlockPos WELL = new BlockPos(0, 0, -2);

    private static final class State {
        final LabyrinthPlace place;
        final long enteredAt;
        int laps;
        int last;
        long seen;

        State(LabyrinthPlace place, long now, int where) {
            this.place = place;
            this.enteredAt = now;
            this.last = where;
            this.seen = now;
        }
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private LabyrinthLoops() {
    }

    public static boolean isLoop(LabyrinthPlace place) {
        return place == LabyrinthPlace.LONG_HALLWAY || place == LabyrinthPlace.HOTEL_HALLWAY || place == LabyrinthPlace.SPIRAL_STAIR;
    }

    // ------------------------------------------------------------------
    // The jogging hallways

    /** Where period {@code k} starts, relative to the base (period -1 is the jog behind the entry door). */
    public static BlockPos periodOrigin(int k) {
        return FIRST_PERIOD.offset(PERIOD.getX() * k, 0, PERIOD.getZ() * k);
    }

    /** Which period a z coordinate lies in (-1: the jog by the entry door). */
    public static int periodOf(BlockPos base, double z) {
        return (int) Math.floor((base.getZ() + FIRST_PERIOD.getZ() + 1 - z) / -PERIOD.getZ());
    }

    private record Region(BlockPos origin, int x0, int x1, int z0, int z1, boolean straight) {
        boolean contains(int x, int z) {
            int lx = x - origin.getX();
            int lz = z - origin.getZ();
            return lx >= x0 && lx <= x1 && lz <= z0 && lz >= z1;
        }
    }

    /** The open parts of a hallway: the jog behind the entry door, three whole periods, and a last straight. */
    private static List<Region> regions(LabyrinthPlace place) {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region(periodOrigin(-1), -1, 4, -9, -11, false));
        for (int k = 0; k <= 2; k++) {
            regions.add(new Region(periodOrigin(k), -1, 1, 0, -(STRAIGHT - 1), true));
            regions.add(new Region(periodOrigin(k), -1, 4, -9, -11, false));
        }
        int last = place == LabyrinthPlace.LONG_HALLWAY ? 6 : STRAIGHT;
        regions.add(new Region(periodOrigin(3), -1, 1, 0, -(last - 1), true));
        return regions;
    }

    private static Set<Long> openColumns(LabyrinthPlace place) {
        Set<Long> columns = new HashSet<>();
        for (Region region : regions(place)) {
            for (int x = region.x0(); x <= region.x1(); x++) {
                for (int z = region.z1(); z <= region.z0(); z++) {
                    columns.add(BlockPos.asLong(region.origin().getX() + x, 0, region.origin().getZ() + z));
                }
            }
        }
        return columns;
    }

    /** Whether a position (relative to the base) is open floor space in a hallway. */
    public static boolean isHallwayInterior(LabyrinthPlace place, BlockPos rel) {
        return rel.getY() >= 0 && rel.getY() <= 2 && openColumns(place).contains(BlockPos.asLong(rel.getX(), 0, rel.getZ()));
    }

    /** A hallway, carved into its solid slot. */
    public static void buildHallway(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        boolean hotel = place == LabyrinthPlace.HOTEL_HALLWAY;
        BlockState wall = hotel ? HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState() : Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
        BlockState ceiling = hotel ? HouseBlocks.HOTEL_CEILING.get().defaultBlockState() : Blocks.STONE.defaultBlockState();
        Set<Long> open = openColumns(place);
        int flags = LabyrinthBuilder.flags();
        for (long column : open) {
            BlockPos c = BlockPos.of(column);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos n = c.relative(side);
                if (!open.contains(BlockPos.asLong(n.getX(), 0, n.getZ()))) {
                    for (int y = -1; y <= 3; y++) {
                        level.setBlock(base.offset(n.getX(), y, n.getZ()),
                                hotel ? hotelWallFor(y) : wall, flags);
                    }
                }
            }
            level.setBlock(base.offset(c.getX(), 3, c.getZ()), ceiling, flags);
        }
        restore(level, base, place);
        LabyrinthBuilder.entrance(level, base, wall, floorFor(hotel, 0, 0), ceiling);
        LabyrinthBuilder.doors(level, base, place);
    }

    private static BlockState floorFor(boolean hotel, int lx, int lz) {
        return hotel ? HouseBlocks.HOTEL_CARPET.get().defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
    }

    private static BlockState hotelWallFor(int y) {
        if (y <= 0) {
            return HouseBlocks.HOTEL_WAINSCOT.get().defaultBlockState();
        }
        if (y >= 3) {
            return HouseBlocks.HOTEL_CEILING.get().defaultBlockState();
        }
        return HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState();
    }

    /**
     * Puts every open cell of a hallway back as built: the floor, the air
     * over it, its lamps and plaques. Whatever was placed or dropped in it is
     * gone. Cheap when nothing has changed.
     */
    static void restore(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        boolean hotel = place == LabyrinthPlace.HOTEL_HALLWAY;
        int flags = LabyrinthBuilder.flags();
        for (Region region : regions(place)) {
            for (int x = region.x0(); x <= region.x1(); x++) {
                for (int z = region.z1(); z <= region.z0(); z++) {
                    BlockPos column = base.offset(region.origin().getX() + x, 0, region.origin().getZ() + z);
                    set(level, column.below(), floorFor(hotel, x, z), flags);
                    for (int y = 0; y <= 2; y++) {
                        set(level, column.above(y), cellState(hotel, region, x, y, z), flags);
                    }
                }
            }
        }
        AABB bounds = new AABB(base.offset(-2, -1, -50).getCenter(), base.offset(15, 3, 0).getCenter()).inflate(1.0D);
        level.getEntitiesOfClass(ItemEntity.class, bounds).forEach(item -> item.discard());
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        if (level.getBlockState(pos) != state) {
            level.setBlock(pos, state, flags);
        }
    }

    /**
     * The early long hallway repeats around a dim soul lantern. The hotel is
     * deeper and physically darker: each repeated straight has only two
     * candles on a dark-oak ledge, leaving isolated warm pools around the
     * numbered doors instead of a bright ceiling lamp.
     */
    private static BlockState cellState(boolean hotel, Region region, int x, int y, int z) {
        if (!hotel && region.straight() && x == 0 && y == 2 && z == -4) {
            return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        }
        if (hotel && region.straight() && x == 1 && z == -6) {
            if (y == 0) {
                return Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            }
            if (y == 1) {
                return Blocks.CANDLE.defaultBlockState()
                        .setValue(CandleBlock.CANDLES, 2)
                        .setValue(CandleBlock.LIT, true);
            }
        }
        if (hotel && region.straight() && y == 1 && z == -3 && (x == -1 || x == 1)) {
            return HouseBlocks.HOTEL_ROOM_PLAQUE.get().defaultBlockState()
                    .setValue(HotelRoomPlaqueBlock.FACING, x == -1 ? Direction.EAST : Direction.WEST);
        }
        return Blocks.AIR.defaultBlockState();
    }

    // ------------------------------------------------------------------
    // The hotel's numbers

    /** A room door in period {@code k}: west ({@code side} 0) or east (1), relative to the base. */
    public static BlockPos roomDoor(int k, int side) {
        return periodOrigin(k).offset(side == 0 ? -2 : 2, 0, -4);
    }

    /** The plaque beside a room door, relative to the base. */
    public static BlockPos signPos(int k, int side) {
        return periodOrigin(k).offset(side == 0 ? -1 : 1, 1, -3);
    }

    /** The number on a door, for someone who has walked {@code laps} periods on: they climb, and repeat. */
    public static int roomNumber(int laps, int k, int side) {
        return HOTEL_FIRST_ROOM + Math.floorMod((laps + k) * 2 + side, HOTEL_ROOMS);
    }

    /** Shows this player the numbers for how far they have walked, before anything else they see this tick. */
    private static void sendNumbers(ServerPlayer player, BlockPos base, int laps) {
        HousePackets.send(player, new HotelRoomNumbersPayload(base, laps));
    }

    // ------------------------------------------------------------------
    // The spiral

    /** Where the step of column {@code i} of turn {@code turn} is, relative to the base, and whether it is a slab. */
    public static int stepY(int turn, int i) {
        return TURN * turn + i / 2 - (i % 2 == 0 ? 1 : 0);
    }

    public static void buildSpiral(ServerLevel level, BlockPos base) {
        int flags = LabyrinthBuilder.flags();
        BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState full = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        int top = TURN * SPIRAL_TURNS + 1;
        for (int x = -2; x <= 2; x++) {
            for (int z = -4; z <= 0; z++) {
                boolean inside = x >= -1 && x <= 1 && z >= -3 && z <= -1;
                for (int y = -1; y <= top; y++) {
                    BlockState state = !inside ? wall : y == -1 ? full : Blocks.AIR.defaultBlockState();
                    level.setBlock(base.offset(x, y, z), state, flags);
                }
                level.setBlock(base.offset(x, top + 1, z), LabyrinthBuilder.SOLID, flags);
            }
        }
        for (int turn = 0; turn < SPIRAL_TURNS; turn++) {
            for (int i = 0; i < RING.size(); i++) {
                level.setBlock(base.offset(RING.get(i)).above(stepY(turn, i)), i % 2 == 0 ? full : slab, flags);
            }
            // Keep the light in a shallow niche in the outer east wall,
            // not under the next flight. The old hanging lantern occupied the
            // third block above a lower step, which was technically enough
            // standing headroom but clipped jumps/sprinting on this very tight
            // stair. Recessing it one block outside the walkable ring keeps
            // every turn lit without putting collision geometry in the route.
            BlockPos lamp = base.offset(2, TURN * turn + 2, -2);
            level.setBlock(lamp, Blocks.LANTERN.defaultBlockState(), flags);
        }
        // The landing at the top, by the far door.
        level.setBlock(base.offset(RING.get(0)).above(stepY(SPIRAL_TURNS, 0)), full, flags);
        LabyrinthBuilder.entrance(level, base, wall, full, wall);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.SPIRAL_STAIR);
    }

    public static int turnOf(BlockPos base, double y) {
        return (int) Math.floor((y - base.getY()) / TURN);
    }

    // ------------------------------------------------------------------
    // Walking them

    /** Each tick for a player well inside a loop place. */
    static void tick(ServerPlayer player, LabyrinthPlace place, BlockPos base) {
        long now = player.serverLevel().getGameTime();
        boolean spiral = place == LabyrinthPlace.SPIRAL_STAIR;
        int where = spiral ? turnOf(base, player.getY()) : periodOf(base, player.getZ());
        State state = STATES.get(player.getUUID());
        if (state == null || state.place != place || now - state.seen > 40L) {
            state = new State(place, now, where);
            STATES.put(player.getUUID(), state);
            if (place == LabyrinthPlace.HOTEL_HALLWAY) {
                sendNumbers(player, base, 0);
            }
        }
        state.seen = now;
        if (spiral) {
            tickSpiral(player, base, state, where);
        } else {
            tickHallway(player, base, place, state, where, now);
        }
    }

    private static void tickHallway(ServerPlayer player, BlockPos base, LabyrinthPlace place, State state, int k, long now) {
        boolean goesOn = place == LabyrinthPlace.HOTEL_HALLWAY || now - state.enteredAt < LONG_HALLWAY_TICKS;
        int shift = 0;
        if (k >= 2 && state.last <= 1 && goesOn) {
            shift = -(k - 1);
        } else if (k <= 0 && state.last >= 1 && state.laps > 0) {
            shift = Math.min(state.laps, 1 - k);
        }
        if (shift == 0) {
            state.last = k;
            return;
        }
        state.laps -= shift;
        state.last = k + shift;
        ServerLevel level = player.serverLevel();
        restore(level, base, place);
        if (place == LabyrinthPlace.HOTEL_HALLWAY) {
            sendNumbers(player, base, state.laps);
        }
        Vec3 to = player.position().add(PERIOD.getX() * (double) shift, 0.0D, PERIOD.getZ() * (double) shift);
        LabyrinthDoors.shift(player, to, player.getYRot());
    }

    private static void tickSpiral(ServerPlayer player, BlockPos base, State state, int turn) {
        if (turn <= 0) {
            state.laps = 0;
        }
        int shift = 0;
        if (turn >= 2 && turn < SPIRAL_TURNS && state.last <= 1 && state.laps < SPIRAL_EXTRA_TURNS && !isOverWell(player, base)) {
            shift = turn - 1;
        }
        if (shift == 0) {
            state.last = turn;
        } else {
            state.laps += shift;
            state.last = turn - shift;
            LabyrinthDoors.shift(player, player.position().add(0.0D, -TURN * (double) shift, 0.0D), player.getYRot());
        }
        // Nothing dropped down the well ever lands.
        BlockPos well = base.offset(WELL);
        AABB lower = new AABB(well.getX(), well.getY() - 1, well.getZ(), well.getX() + 1, well.getY() + 3, well.getZ() + 1);
        for (FallingBlockEntity falling : player.serverLevel().getEntitiesOfClass(FallingBlockEntity.class, lower)) {
            falling.discard();
        }
    }

    private static boolean isOverWell(ServerPlayer player, BlockPos base) {
        BlockPos well = base.offset(WELL);
        return player.getBlockX() == well.getX() && player.getBlockZ() == well.getZ();
    }

    // ------------------------------------------------------------------
    // Placing things

    /**
     * Blocks may be placed in the hallways' open cells (they are gone on the
     * next pass) and, in the spiral, anything that falls may be set over the
     * well (it never lands). Nowhere else in the stack.
     */
    public static boolean allowsPlacing(Level level, BlockPos pos, BlockState placed) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        LabyrinthPlace place = origin == null ? null : LabyrinthPlaces.placeAt(origin, pos);
        BlockPos base = place == null ? null : LabyrinthPlaces.base(origin, place);
        if (base == null) {
            return false;
        }
        BlockPos rel = pos.subtract(base);
        if (place == LabyrinthPlace.SPIRAL_STAIR) {
            return placed.getBlock() instanceof FallingBlock && rel.getX() == WELL.getX() && rel.getZ() == WELL.getZ()
                    && rel.getY() >= 0 && rel.getY() <= TURN * SPIRAL_TURNS + 1;
        }
        return (place == LabyrinthPlace.LONG_HALLWAY || place == LabyrinthPlace.HOTEL_HALLWAY) && isHallwayInterior(place, rel);
    }

    public static void forget(UUID player) {
        STATES.remove(player);
    }

    public static void clearAll() {
        STATES.clear();
    }

    @Nullable
    public static String describe(UUID player, long now) {
        State state = STATES.get(player);
        if (state == null || now - state.seen > 40L) {
            return null;
        }
        return switch (state.place) {
            case LONG_HALLWAY -> "In the long hallway: " + state.laps + " lap(s) on, "
                    + Math.max(0L, (LONG_HALLWAY_TICKS - (now - state.enteredAt)) / 20L) + " s until it ends.";
            case HOTEL_HALLWAY -> "In the hotel hallway: " + state.laps + " lap(s) on.";
            case SPIRAL_STAIR -> "On the spiral stair: " + state.laps + " extra turn(s) climbed.";
            default -> null;
        };
    }
}
