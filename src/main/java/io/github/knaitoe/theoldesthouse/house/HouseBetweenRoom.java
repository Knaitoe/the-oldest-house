package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseRoomDoorPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The room between rooms: the first space the manor has no room for.
 *
 * The door between the hall and the study is a door everyone knows. One
 * morning it quietly begins to lead somewhere else first: open it from the
 * hall and there is a small sitting room inside the thickness of a one-block
 * wall, and on its far side another door, and through that the study,
 * exactly where it always was. The same the other way. Nothing is added to
 * the manor; the door itself never changes.
 *
 * The room stands in "the pocket", high above the manor in the House
 * dimension ({@link #pocketDy}). Its east door is the hall-to-study door,
 * directly over the real one, with a copy of the hall in front of it. Its
 * west door stands {@link #STUDY_SHIFT} blocks further west, with a copy of
 * the study, shifted by the same amount, behind it. So:
 *
 * <ul>
 *   <li>Clicking the real door from the hall shifts the player straight up
 *   into the copied hall; the pocket's east door swings open onto the room.</li>
 *   <li>Walking out through the west door into the copied study shifts them
 *   down and back east, into the real study just inside its door.</li>
 *   <li>From the study it is the same in reverse.</li>
 * </ul>
 *
 * Every shift is a relative move within loaded chunks, so position, facing,
 * pitch and momentum carry over and nothing reloads. The real door never
 * opens. Coming out on the other side from the one you went in by counts as
 * having found the room (see {@link HouseSavedData#isRoomTraversed()}).
 *
 * The room is the House's first impossibility, not yet the labyrinth: beds
 * and every other ordinary rule still apply in it.
 *
 * The morning roll only arms the room ({@link #arm}); it is built out of
 * sight then, and starts routing ({@link #activate}) the first time nobody is
 * in or looking at the doorway.
 */
public final class HouseBetweenRoom {
    /** The hall-to-study door's lower half, relative to the manor's origin. */
    public static final BlockPos REAL_DOOR = new BlockPos(13, 1, 20);
    /** How far west the study's copy (and the room's far door) stands from the real study. */
    public static final int STUDY_SHIFT = -7;
    /** The room's inside, relative to the pocket's origin: between the two doors. */
    public static final int ROOM_MIN_X = 7;
    public static final int ROOM_MAX_X = 12;
    public static final int ROOM_MIN_Z = 17;
    public static final int ROOM_MAX_Z = 23;
    public static final int ROOM_MIN_Y = 1;
    public static final int ROOM_MAX_Y = 5;
    /** Layout of the pocket this code builds; older rooms (the bedroom version) are replaced. */
    public static final int LAYOUT = 2;

    /** Walls and floors placed together, with no shape updates to break anything half-built. */
    private static final int BUILD_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int DOOR_CHECK_INTERVAL = 20;
    /** How far from its door someone can wander in a copy without going in before being put back. */
    private static final double APPROACH_RADIUS = 6.0D;
    /** How much farther than their entry point counts as backing away. */
    private static final double BACK_OUT_MARGIN = 0.65D;
    /** Nobody closer than this to the doorway when it starts routing. */
    private static final double CLEAR_RADIUS = 4.0D;

    /** The pocket's extent, relative to its origin: everything copied or built there. */
    private static final int POCKET_MIN_X = -26;
    private static final int POCKET_MAX_X = 46;
    private static final int POCKET_MIN_Y = -1;
    private static final int POCKET_MAX_Y = 25;
    private static final int POCKET_MIN_Z = -19;
    private static final int POCKET_MAX_Z = 33;

    /** A box of the manor (real, relative to origin) and where its copy goes. {x0, y0, z0, x1, y1, z1, dx}. */
    private static final int[][] COPIES = {
            // What can be seen outside through the windows on the hall side.
            {-3, 0, -18, 30, 24, -2, 0},
            {-18, 0, -2, -1, 24, 13, 0},
            {29, 0, -2, 45, 24, 32, 0},
            // The ground floor on the hall side: the great room, the hall, the kitchen,
            // the stair tower and the scullery, whatever an open doorway shows.
            {0, -1, -1, 28, 7, 14, 0},
            {13, -1, 15, 28, 7, 28, 0},
            // What can be seen through the study's windows, shifted with the study.
            {-18, 0, 13, 0, 24, 32, STUDY_SHIFT},
            // The study itself, with its east wall and the real door in it.
            {0, -1, 15, 13, 7, 27, STUDY_SHIFT},
    };

    private enum Side { HALL, STUDY }

    /** Players who have been inside the room since arriving. */
    private static final Set<UUID> INSIDE = new HashSet<>();
    /** The side each player in the pocket came in by. */
    private static final Map<UUID, Side> CAME_FROM = new HashMap<>();
    /** Horizontal distance from that side's copied door when routing began. */
    private static final Map<UUID, Double> ENTRY_DISTANCE = new HashMap<>();

    private HouseBetweenRoom() {
    }

    // ------------------------------------------------------------------
    // Where the pocket is

    /**
     * How far above the manor the pocket stands: 100 blocks, or less if the
     * House is so high the pocket would not fit under the build limit (the
     * House dimension's is 320).
     */
    public static int pocketDy(BlockPos origin) {
        return Math.max(40, Math.min(100, 320 - 32 - origin.getY()));
    }

    /** The pocket's own origin: the manor's, shifted up. */
    public static BlockPos pocketOrigin(BlockPos origin) {
        return origin.above(pocketDy(origin));
    }

    /** Whether a position (in the House dimension) is anywhere in the pocket: the room or its copies. */
    public static boolean isInPocket(BlockPos origin, double x, double y, double z) {
        BlockPos pocket = pocketOrigin(origin);
        double relX = x - pocket.getX();
        double relY = y - pocket.getY();
        double relZ = z - pocket.getZ();
        return relX >= POCKET_MIN_X && relX < POCKET_MAX_X + 1
                && relY >= POCKET_MIN_Y && relY < POCKET_MAX_Y + 1
                && relZ >= POCKET_MIN_Z && relZ < POCKET_MAX_Z + 1;
    }

    public static BlockPos realDoor(BlockPos origin) {
        return origin.offset(REAL_DOOR);
    }

    /** The pocket's door on the hall side, directly above the real one. */
    public static BlockPos hallDoor(BlockPos origin) {
        return realDoor(origin).above(pocketDy(origin));
    }

    /** The pocket's door on the study side. */
    public static BlockPos studyDoor(BlockPos origin) {
        return hallDoor(origin).offset(STUDY_SHIFT, 0, 0);
    }

    // ------------------------------------------------------------------
    // Appearing

    /**
     * The morning roll: builds the pocket out of sight and arms the door. It
     * starts routing once nobody is looking ({@link #activate}).
     *
     * @return false if the House dimension is missing
     */
    public static boolean arm(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return false;
        }
        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return false;
        }
        buildPocket(interior, data, origin);
        data.markRoomArmed();
        TheOldestHouse.LOGGER.info("The room between rooms is ready behind the study door (perceived age {}); it opens once nobody is looking.",
                data.houseAge());
        return true;
    }

    /**
     * The door starts leading through the room. It is shut first, quietly,
     * if someone left it open; nothing else in the manor changes.
     */
    public static boolean activate(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isRoomArmed()) {
            return false;
        }
        if (data.roomLayout() < LAYOUT) {
            buildPocket(interior, data, origin);
        }
        for (ServerLevel level : new ServerLevel[]{interior, server.overworld()}) {
            if (level.isLoaded(realDoor(origin))) {
                setDoorOpen(level, realDoor(origin), false);
            }
        }
        data.markRoomRevealed();
        PacketDistributor.sendToAllPlayers(doorPayload(data));
        TheOldestHouse.LOGGER.info("The study door of The Oldest House now leads through the room between rooms (perceived age {}).",
                data.houseAge());
        return true;
    }

    /** Whether the doorway can change now: nobody in it, near it, or looking at it. */
    public static boolean isUnwitnessed(ServerLevel interior, BlockPos origin) {
        BlockPos lower = realDoor(origin);
        Vec3 centre = Vec3.atCenterOf(lower);
        for (ServerPlayer player : interior.players()) {
            if (!player.isSpectator() && player.position().distanceTo(centre) < CLEAR_RADIUS) {
                return false;
            }
        }
        return !HouseWatchers.isWatched(interior, lower) && !HouseWatchers.isWatched(interior, lower.above());
    }

    /**
     * On server start: a pocket from an older version (the bedroom's door
     * upstairs) is taken down and the room rebuilt behind the study door.
     */
    public static void ensurePocket(MinecraftServer server) {
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isRoomArmed() || data.roomLayout() >= LAYOUT) {
            return;
        }
        int oldDoorX = data.roomDoorX();
        if (oldDoorX >= 0) {
            // The bedroom partition goes back to plain plaster.
            BlockPos oldDoor = origin.offset(oldDoorX, 7, 9);
            interior.setBlock(oldDoor, HouseShell.PLASTER, BUILD_FLAGS);
            interior.setBlock(oldDoor.above(), HouseShell.PLASTER, BUILD_FLAGS);
            HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);
            data.clearRoomDoorX();
        }
        buildPocket(interior, data, origin);
        PacketDistributor.sendToAllPlayers(doorPayload(data));
        TheOldestHouse.LOGGER.info("Moved the room between rooms behind the study door.");
    }

    private static void buildPocket(ServerLevel interior, HouseSavedData data, BlockPos origin) {
        BlockPos pocket = pocketOrigin(origin);
        int dy = pocketDy(origin);
        clearPocket(interior, pocket);
        for (int[] box : COPIES) {
            copyBox(interior, origin, box, dy);
        }
        buildRoom(interior, pocket);
        copyPaintings(interior, origin);
        data.setRoomLayout(LAYOUT);
        TheOldestHouse.LOGGER.info("Built the room between rooms {} blocks above the manor.", dy);
    }

    /** Clears whatever an older pocket (or the terrain that high up) left. */
    private static void clearPocket(ServerLevel level, BlockPos pocket) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = POCKET_MIN_X; x <= POCKET_MAX_X; x++) {
            for (int z = POCKET_MIN_Z; z <= POCKET_MAX_Z; z++) {
                for (int y = POCKET_MIN_Y; y <= POCKET_MAX_Y; y++) {
                    pos.set(pocket.getX() + x, pocket.getY() + y, pocket.getZ() + z);
                    if (!level.isOutsideBuildHeight(pos) && !level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, air, BUILD_FLAGS);
                    }
                }
            }
        }
        AABB box = new AABB(pocket.offset(POCKET_MIN_X, POCKET_MIN_Y, POCKET_MIN_Z).getCenter(),
                pocket.offset(POCKET_MAX_X, POCKET_MAX_Y, POCKET_MAX_Z).getCenter());
        level.getEntitiesOfClass(Painting.class, box).forEach(Painting::discard);
    }

    private static void copyBox(ServerLevel level, BlockPos origin, int[] box, int dy) {
        BlockPos.MutableBlockPos from = new BlockPos.MutableBlockPos();
        for (int x = box[0]; x <= box[3]; x++) {
            for (int y = box[1]; y <= box[4]; y++) {
                for (int z = box[2]; z <= box[5]; z++) {
                    from.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    copyBlock(level, from.immutable(), box[6], dy);
                }
            }
        }
    }

    /** One block and its non-inventory block-entity data, copied by {@code (dx, dy, 0)}. */
    private static void copyBlock(ServerLevel level, BlockPos from, int dx, int dy) {
        BlockPos to = from.offset(dx, dy, 0);
        if (level.isOutsideBuildHeight(to)) {
            return;
        }
        BlockState state = level.getBlockState(from);
        if (level.getBlockState(to) != state) {
            level.setBlock(to, state, BUILD_FLAGS);
        }
        BlockEntity source = level.getBlockEntity(from);
        BlockEntity target = level.getBlockEntity(to);
        if (source == null || target == null || source.getType() != target.getType()
                || HouseDimensionMirror.isInventoryBearing(level, source)) {
            return;
        }
        CompoundTag data = source.saveWithoutMetadata(level.registryAccess());
        if (!data.equals(target.saveWithoutMetadata(level.registryAccess()))) {
            target.loadWithComponents(data, level.registryAccess());
            target.setChanged();
            level.sendBlockUpdated(to, state, state, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * The room itself, built of the manor's own materials between the two
     * copied doors: oak boards, plaster, a dark ceiling, no windows. A red
     * rug, shelves, an armchair facing the hall door, a candle still burning.
     * Its east and west walls are the copied partition, so from either side
     * the wall is the wall everyone knows.
     */
    public static void buildRoom(ServerLevel level, BlockPos pocket) {
        for (int x = ROOM_MIN_X; x <= ROOM_MAX_X; x++) {
            for (int z = ROOM_MIN_Z - 1; z <= ROOM_MAX_Z + 1; z++) {
                for (int y = ROOM_MIN_Y - 1; y <= ROOM_MAX_Y + 1; y++) {
                    BlockState state;
                    if (y == ROOM_MIN_Y - 1) {
                        state = HouseShell.OAK_FLOOR;
                    } else if (y == ROOM_MAX_Y + 1) {
                        state = HouseShell.CEILING;
                    } else if (z < ROOM_MIN_Z || z > ROOM_MAX_Z) {
                        state = HouseShell.PLASTER;
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    level.setBlock(pocket.offset(x, y, z), state, BUILD_FLAGS);
                }
            }
        }
        // The two doors, each a copy of the real one, shut.
        placeDoor(level, pocket.offset(REAL_DOOR));
        placeDoor(level, pocket.offset(REAL_DOOR).offset(STUDY_SHIFT, 0, 0));

        // A beam across the ceiling, as in the hall.
        for (int z = ROOM_MIN_Z; z <= ROOM_MAX_Z; z++) {
            level.setBlock(pocket.offset(10, ROOM_MAX_Y, z),
                    Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), BUILD_FLAGS);
        }
        int y = ROOM_MIN_Y;
        for (int x = 8; x <= 11; x++) {
            for (int z = 19; z <= 21; z++) {
                level.setBlock(pocket.offset(x, y, z), Blocks.RED_CARPET.defaultBlockState(), BUILD_FLAGS);
            }
        }
        for (int x = 8; x <= 11; x++) {
            for (int dy = 0; dy <= 1; dy++) {
                level.setBlock(pocket.offset(x, y + dy, ROOM_MIN_Z), Blocks.BOOKSHELF.defaultBlockState(), BUILD_FLAGS);
            }
        }
        // The armchair has its back to the far corner: it faces the hall door.
        level.setBlock(pocket.offset(ROOM_MIN_X, y, ROOM_MAX_Z),
                Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), BUILD_FLAGS);
        level.setBlock(pocket.offset(ROOM_MIN_X, y, ROOM_MAX_Z - 1),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), BUILD_FLAGS);
        level.setBlock(pocket.offset(ROOM_MIN_X, y + 1, ROOM_MAX_Z - 1),
                Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true), BUILD_FLAGS);
        level.setBlock(pocket.offset(ROOM_MAX_X, y, ROOM_MAX_Z), Blocks.POTTED_FERN.defaultBlockState(), BUILD_FLAGS);
        level.setBlock(pocket.offset(9, ROOM_MAX_Y, 18), HouseInteriors.lantern(true), BUILD_FLAGS);
        level.setBlock(pocket.offset(9, ROOM_MAX_Y, 22), HouseInteriors.lantern(true), BUILD_FLAGS);
    }

    /** The hall-to-study door as the manor has it: spruce, facing the hall, shut. */
    private static void placeDoor(ServerLevel level, BlockPos lower) {
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.EAST)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        level.setBlock(lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), BUILD_FLAGS);
        level.setBlock(lower.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), BUILD_FLAGS);
    }

    /**
     * Hangs copies of the hall's and the study's paintings in the pocket,
     * matched one for one with any already there, never on top of them: two
     * paintings in one place pop off the wall and drop as items.
     */
    public static void copyPaintings(ServerLevel level, BlockPos origin) {
        int dy = pocketDy(origin);
        for (int[] box : COPIES) {
            AABB real = new AABB(origin.getX() + box[0], origin.getY() + box[1], origin.getZ() + box[2],
                    origin.getX() + box[3] + 1, origin.getY() + box[4] + 1, origin.getZ() + box[5] + 1);
            AABB copy = real.move(box[6], dy, 0);
            if (!entitiesLoaded(level, real) || !entitiesLoaded(level, copy)) {
                continue; // Not now: better no paintings than two.
            }
            List<Painting> copies = new ArrayList<>(level.getEntitiesOfClass(Painting.class, copy));
            for (Painting painting : level.getEntitiesOfClass(Painting.class, real)) {
                BlockPos at = painting.getPos().offset(box[6], dy, 0);
                Painting match = null;
                for (Painting candidate : copies) {
                    if (candidate.getPos().equals(at) && candidate.getDirection() == painting.getDirection()) {
                        match = candidate;
                        break;
                    }
                }
                if (match != null) {
                    copies.remove(match);
                    if (match.getVariant().equals(painting.getVariant())) {
                        continue;
                    }
                    match.discard();
                }
                level.addFreshEntity(new Painting(level, at, painting.getDirection(), painting.getVariant()));
            }
            copies.forEach(Painting::discard);
        }
    }

    private static boolean entitiesLoaded(ServerLevel level, AABB box) {
        for (int cx = ((int) Math.floor(box.minX)) >> 4; cx <= ((int) Math.floor(box.maxX)) >> 4; cx++) {
            for (int cz = ((int) Math.floor(box.minZ)) >> 4; cz <= ((int) Math.floor(box.maxZ)) >> 4; cz++) {
                if (!level.areEntitiesLoaded(ChunkPos.asLong(cx, cz))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** What clients need to know to leave the door shut when it is clicked. */
    public static HouseRoomDoorPayload doorPayload(HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (origin == null || !data.isRoomRevealed()) {
            return new HouseRoomDoorPayload(BlockPos.ZERO, false);
        }
        return new HouseRoomDoorPayload(realDoor(origin), true);
    }

    // ------------------------------------------------------------------
    // Going through

    /**
     * Clicking the study door in the manor, from either side: it stays shut
     * there (the client is told where it is, so it never shows it opening).
     * The player is shifted into the copy on their own side, where the same
     * door swings open onto the room.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()
                || !(level.dimension().equals(HouseDimensions.INTERIOR) || level.dimension().equals(Level.OVERWORLD))
                || level.getServer() == null) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.houseOrigin();
        if (origin == null || !data.isRoomRevealed() || !isRealDoorCell(origin, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player
                && level instanceof ServerLevel interior
                && level.dimension().equals(HouseDimensions.INTERIOR)
                && event.getHand() == InteractionHand.MAIN_HAND) {
            Side side = player.getX() >= realDoor(origin).getX() + 0.5D ? Side.HALL : Side.STUDY;
            enter(player, interior, data, origin, side);
        }
    }

    private static void enter(ServerPlayer player, ServerLevel interior, HouseSavedData data, BlockPos origin, Side side) {
        if (data.roomLayout() < LAYOUT) {
            buildPocket(interior, data, origin);
        }
        copyPaintings(interior, origin);
        BlockPos door = side == Side.HALL ? hallDoor(origin) : studyDoor(origin);
        UUID id = player.getUUID();
        INSIDE.remove(id);
        CAME_FROM.put(id, side);
        BlockPos localDoor = side == Side.HALL ? REAL_DOOR : REAL_DOOR.offset(STUDY_SHIFT, 0, 0);
        double relX = player.getX() - origin.getX() + (side == Side.STUDY ? STUDY_SHIFT : 0);
        double relZ = player.getZ() - origin.getZ();
        ENTRY_DISTANCE.put(id, Math.sqrt(
                sq(relX - (localDoor.getX() + 0.5D))
                        + sq(relZ - (localDoor.getZ() + 0.5D))
        ));

        // Open the destination copy before moving the player. Sending the
        // relative teleport first lets the client render one frame of the
        // still-shut copied doorway / plaster wall before its block update
        // arrives, which gives the trick away.
        setDoorOpen(interior, door, true);
        shift(player, side == Side.HALL ? 0 : STUDY_SHIFT, pocketDy(origin));
        interior.playSound(null, door, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F,
                0.9F + interior.getRandom().nextFloat() * 0.1F);
    }

    /**
     * Each tick for a player in the House dimension. Returns true when they
     * are in the pocket (so the manor's own bounds do not apply), after
     * returning them to the manor once they come out of the room on either
     * side, or wander off from a door without going in.
     */
    public static boolean tickPocket(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        if (!data.isRoomArmed() || !isInPocket(origin, player.getX(), player.getY(), player.getZ())) {
            INSIDE.remove(player.getUUID());
            return false;
        }
        UUID id = player.getUUID();
        BlockPos pocket = pocketOrigin(origin);
        double relX = player.getX() - pocket.getX();
        double relY = player.getY() - pocket.getY();
        double relZ = player.getZ() - pocket.getZ();
        if (relX > ROOM_MIN_X + 0.35D && relX < ROOM_MAX_X + 0.65D) {
            INSIDE.add(id);
            return true;
        }
        // Out past a doorway, far enough that the real door's shut panel
        // (on the study side of its block) is clear of them when they land.
        Side side;
        if (relX >= REAL_DOOR.getX() + 1.0D) {
            side = Side.HALL;
        } else if (relX <= REAL_DOOR.getX() + STUDY_SHIFT - 0.4D) {
            side = Side.STUDY;
        } else {
            return true; // In a doorway.
        }

        boolean leave;
        BlockPos localDoor = side == Side.HALL ? REAL_DOOR : REAL_DOOR.offset(STUDY_SHIFT, 0, 0);
        double dx = relX - (localDoor.getX() + 0.5D);
        double dz = relZ - (localDoor.getZ() + 0.5D);
        double distanceSq = dx * dx + dz * dz;

        if (INSIDE.contains(id)) {
            // Once they clear either far side, there is no reason to keep them
            // in a dead copy of the hall/study. Shut the copied door first,
            // then immediately rejoin the real room behind the identical shut
            // door. The panel itself hides the vertical shift, interactions
            // become real at once, and multiplayer observers see the arrival
            // at the threshold rather than six blocks later.
            leave = true;
        } else {
            Side from = CAME_FROM.get(id);
            double enteredAt = ENTRY_DISTANCE.getOrDefault(id, APPROACH_RADIUS);
            boolean backedOut = from == side
                    && Math.sqrt(distanceSq) >= enteredAt + BACK_OUT_MARGIN;
            leave = backedOut
                    || distanceSq > APPROACH_RADIUS * APPROACH_RADIUS
                    || relY < ROOM_MIN_Y - 0.5D
                    || relY > ROOM_MAX_Y + 1;
        }
        if (leave) {
            returnToManor(player, data, origin, side);
        }
        return true;
    }

    private static void returnToManor(ServerPlayer player, HouseSavedData data, BlockPos origin, Side side) {
        UUID id = player.getUUID();
        boolean cameThrough = INSIDE.remove(id);
        Side from = CAME_FROM.remove(id);
        ENTRY_DISTANCE.remove(id);
        ServerLevel level = player.serverLevel();

        // Block updates are sent before the teleport packet on this connection:
        // close the visible copied threshold first so a player backing through
        // it sees an ordinary door panel, never the 100-block vertical move.
        setDoorOpen(level, side == Side.HALL ? hallDoor(origin) : studyDoor(origin), false);
        setDoorOpen(level, side == Side.HALL ? studyDoor(origin) : hallDoor(origin), false);
        shift(player, side == Side.HALL ? 0 : -STUDY_SHIFT, -pocketDy(origin));

        // The real partition is always shut. Its close sound belongs at the
        // player's actual destination, whether they traversed or backed out.
        BlockPos real = realDoor(origin);
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WOODEN_DOOR_CLOSE), SoundSource.BLOCKS,
                real.getX() + 0.5D, real.getY() + 0.5D, real.getZ() + 0.5D, 1.0F,
                0.9F + player.getRandom().nextFloat() * 0.1F, player.getRandom().nextLong()));

        if (cameThrough) {
            if (from != null && from != side && !data.isRoomTraversed()) {
                data.markRoomTraversed();
                TheOldestHouse.LOGGER.info("{} went through the room between rooms, from the {} to the {}.",
                        player.getGameProfile().getName(), from == Side.HALL ? "hall" : "study", side == Side.HALL ? "hall" : "study");
            }
        }
    }

    /**
     * By {@code (dx, dy, 0)} using the shared seamless-House teleporter.
     */
    private static void shift(ServerPlayer player, int dx, int dy) {
        HouseInternalTeleport.translate(player, dx, dy, 0.0D);
    }

    private static double sq(double value) {
        return value * value;
    }

    /** Players left in the old separate dimension by an earlier version are brought back to the hall. */
    static void rescueFromBetween(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        if (HouseTransitionEvents.isPending(player)) {
            return;
        }
        Vec3 target = Vec3.atBottomCenterOf(realDoor(origin).east());
        HouseTransitionEvents.beginDoorTransition(player, HouseDimensions.INTERIOR, null, null, target, 90.0F);
    }

    private static void setDoorOpen(ServerLevel level, BlockPos lower, boolean open) {
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState state = level.getBlockState(half);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN) != open) {
                level.setBlock(half, state.setValue(DoorBlock.OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    /**
     * Starts routing an armed door once nobody is looking, and keeps the
     * manor's side of the door shut afterwards, whatever opens it.
     */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % DOOR_CHECK_INTERVAL != 0) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isRoomArmed()) {
            return;
        }
        BlockPos lower = realDoor(origin);
        if (!data.isRoomRevealed()) {
            if (interior.isLoaded(lower) && isUnwitnessed(interior, origin)) {
                activate(server, data);
            }
            return;
        }
        for (ServerLevel level : new ServerLevel[]{interior, server.overworld()}) {
            if (level.isLoaded(lower)) {
                setDoorOpen(level, lower, false);
            }
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, doorPayload(HouseSavedData.get(player.server)));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        INSIDE.remove(event.getEntity().getUUID());
        CAME_FROM.remove(event.getEntity().getUUID());
        ENTRY_DISTANCE.remove(event.getEntity().getUUID());
    }

    public static void clearAll() {
        INSIDE.clear();
        CAME_FROM.clear();
        ENTRY_DISTANCE.clear();
    }

    // ------------------------------------------------------------------
    // Protection

    private static boolean isRealDoorCell(BlockPos origin, BlockPos pos) {
        BlockPos rel = pos.subtract(origin);
        return rel.getX() == REAL_DOOR.getX() && rel.getZ() == REAL_DOOR.getZ()
                && (rel.getY() == REAL_DOOR.getY() || rel.getY() == REAL_DOOR.getY() + 1);
    }

    /**
     * In the manor (and its Overworld proxy): once the room is armed, the
     * study door and the partition around it cannot be broken, pushed or
     * blown up, so it always stays the door the copies were made of.
     */
    public static boolean isProtectedHousePosition(HouseSavedData data, BlockPos origin, BlockPos pos) {
        if (!data.isRoomArmed()) {
            return false;
        }
        BlockPos rel = pos.subtract(origin);
        return rel.getX() == REAL_DOOR.getX()
                && rel.getZ() >= REAL_DOOR.getZ() - 1 && rel.getZ() <= REAL_DOOR.getZ() + 1
                && rel.getY() >= REAL_DOOR.getY() - 1 && rel.getY() <= REAL_DOOR.getY() + 2;
    }

    /** The manor's origin, if {@code level} is the House dimension and the room is armed. */
    private static BlockPos pocketHouse(Level level) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return null;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        return data.isRoomArmed() ? data.houseOrigin() : null;
    }

    /** Everything in the pocket, copies and room alike, stays as built. */
    private static boolean isProtectedInPocket(Level level, BlockPos pos) {
        BlockPos origin = pocketHouse(level);
        return origin != null && isInPocket(origin, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && isProtectedInPocket(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && isProtectedInPocket(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (pocketHouse(level) != null) {
            event.getAffectedBlocks().removeIf(pos -> isProtectedInPocket(level, pos));
        }
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (event.getLevel() instanceof Level level && isProtectedInPocket(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    /** Nothing in the pocket can be used but its two doors. */
    public static void onRightClickInPocket(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos origin = pocketHouse(level);
        if (origin == null || level.isClientSide() || !isProtectedInPocket(level, event.getPos())) {
            return;
        }
        BlockPos pos = event.getPos();
        for (BlockPos door : new BlockPos[]{hallDoor(origin), studyDoor(origin)}) {
            if (pos.equals(door) || pos.equals(door.above())) {
                return;
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }
}
