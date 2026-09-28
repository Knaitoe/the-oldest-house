package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseRoomDoorPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
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
 * One morning a door is set into the partition between the principal and
 * literary bedrooms. The partition is a single block thick; on the literary
 * side there are only bookshelves. Opening the door moves the player, at
 * unchanged coordinates, into {@link HouseDimensions#BETWEEN}, where a copy
 * of the principal bedroom stands in front of the same door and a windowless
 * sitting room lies behind it, where the literary bedroom ought to be.
 *
 * The copy of the bedroom is refreshed each time someone goes through, so the
 * swap cannot be seen. It only has to hold up for as long as the player is
 * facing the door: walking back out through the doorway returns them to the
 * real bedroom, and so does wandering away from the door without going in.
 * The real door in the manor never opens, since there is nothing behind it.
 *
 * This is the manor's first teleport door. Later doors will reuse the same
 * transition, and "rearranging" the house will mean changing where a door
 * leads rather than moving blocks.
 */
public final class HouseBetweenRoom {
    /** The partition between the principal (north) and literary (south) bedrooms. */
    public static final int WALL_Z = 9;
    public static final int DOOR_Y = 7;
    private static final int FLOOR_Y = 6;
    private static final int CEILING_Y = 11;

    /** Doors are tried in this order; the first with a clear floor in front wins. */
    private static final int[] DOOR_X_CANDIDATES = {9, 10, 8, 11, 7, 6, 5, 4};

    // The room behind the door, relative to the door's x.
    private static final int ROOM_HALF_WIDTH = 3;
    public static final int ROOM_MIN_Z = WALL_Z + 1;
    public static final int ROOM_MAX_Z = WALL_Z + 9;
    private static final int ROOM_MIN_Y = 7;
    private static final int ROOM_MAX_Y = 10;

    // The principal bedroom with its walls, copied in front of the door.
    private static final int COPY_MIN_X = 0;
    private static final int COPY_MAX_X = 13;
    private static final int COPY_MIN_Y = FLOOR_Y;
    private static final int COPY_MAX_Y = CEILING_Y;
    private static final int COPY_MIN_Z = -1;
    private static final int COPY_MAX_Z = WALL_Z;

    /** How far from the door someone can wander in the copied bedroom before being put back. */
    private static final double APPROACH_RADIUS = 6.0D;
    private static final int DOOR_OPEN_DELAY_TICKS = 6;
    private static final int DOOR_CHECK_INTERVAL = 20;

    private static final int BUILD_FLAGS = Block.UPDATE_CLIENTS;

    /** Players who have been inside the room since arriving; stepping back out returns them. */
    private static final Set<UUID> INSIDE = new HashSet<>();
    /** Ticks until the door opens in front of a player who has just arrived. */
    private static final Map<UUID, Integer> DOOR_OPENING = new HashMap<>();

    private HouseBetweenRoom() {
    }

    // ------------------------------------------------------------------
    // Appearing

    /**
     * Sets the door into the bedroom partition and builds the room behind it.
     *
     * @return false if a dimension it needs is missing
     */
    public static boolean reveal(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return false;
        }
        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(server, data);
        ServerLevel between = server.getLevel(HouseDimensions.BETWEEN);
        if (interior == null || between == null) {
            return false;
        }

        int doorX = chooseDoorX(interior, origin);
        placeDoor(interior, origin, doorX);
        HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);

        buildRoom(between, origin, doorX);
        copyBedroom(interior, between, origin);
        data.markRoomRevealed(doorX);
        PacketDistributor.sendToAllPlayers(doorPayload(data));

        TheOldestHouse.LOGGER.info("A door has appeared in the principal bedroom of The Oldest House at {} (perceived age {}).",
                doorPos(origin, doorX), data.houseAge());
        return true;
    }

    /** What clients need to know to leave the door shut when it is clicked. */
    public static HouseRoomDoorPayload doorPayload(HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (origin == null || !data.isRoomRevealed()) {
            return new HouseRoomDoorPayload(BlockPos.ZERO, false);
        }
        return new HouseRoomDoorPayload(doorPos(origin, data.roomDoorX()), true);
    }

    /** The lower half of the room's door. */
    public static BlockPos doorPos(BlockPos origin, int doorX) {
        return origin.offset(doorX, DOOR_Y, WALL_Z);
    }

    /**
     * Where in the partition the door goes: plaster at door height with
     * nothing standing or hanging in front of it, preferably with full blocks
     * behind it so the literary bedroom never shows its back.
     */
    public static int chooseDoorX(ServerLevel level, BlockPos origin) {
        int fallback = -1;
        for (int x : DOOR_X_CANDIDATES) {
            if (!isPlainPartition(level, origin, x) || !isClearInFront(level, origin, x)) {
                continue;
            }
            if (isBacked(level, origin, x)) {
                return x;
            }
            if (fallback < 0) {
                fallback = x;
            }
        }
        return fallback >= 0 ? fallback : DOOR_X_CANDIDATES[0];
    }

    private static boolean isPlainPartition(ServerLevel level, BlockPos origin, int x) {
        for (int y = DOOR_Y; y <= DOOR_Y + 1; y++) {
            if (!level.getBlockState(origin.offset(x, y, WALL_Z)).is(HouseShell.PLASTER.getBlock())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isClearInFront(ServerLevel level, BlockPos origin, int x) {
        for (int y = DOOR_Y; y <= DOOR_Y + 1; y++) {
            BlockPos pos = origin.offset(x, y, WALL_Z - 1);
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            // A carpet or nothing at all.
            if (!shape.isEmpty() && shape.max(Direction.Axis.Y) > 0.07D) {
                return false;
            }
        }
        BlockPos lower = origin.offset(x, DOOR_Y, WALL_Z - 1);
        AABB front = new AABB(lower).expandTowards(0.0D, 1.0D, 0.0D);
        return level.getEntitiesOfClass(HangingEntity.class, front).isEmpty();
    }

    private static boolean isBacked(ServerLevel level, BlockPos origin, int x) {
        for (int y = DOOR_Y; y <= DOOR_Y + 2; y++) {
            BlockPos pos = origin.offset(x, y, WALL_Z + 1);
            if (!level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                return false;
            }
        }
        return true;
    }

    /** A spruce door like the manor's others, flush with the bedroom side of the partition. */
    public static void placeDoor(ServerLevel level, BlockPos origin, int doorX) {
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        BlockPos lower = doorPos(origin, doorX);
        level.setBlock(lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), BUILD_FLAGS);
        level.setBlock(lower.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), BUILD_FLAGS);

        // Anything missing from the shelves behind it is filled in, so the
        // back of the door can never be seen from the literary bedroom.
        for (int y = DOOR_Y; y <= DOOR_Y + 2; y++) {
            BlockPos behind = origin.offset(doorX, y, WALL_Z + 1);
            if (level.getBlockState(behind).isAir()) {
                level.setBlock(behind, Blocks.BOOKSHELF.defaultBlockState(), BUILD_FLAGS);
            }
        }
    }

    /**
     * The room itself, built of the manor's own materials: plaster walls,
     * oak boards and a dark ceiling, no windows. A red rug, the colour the
     * bedroom's rug used to be; shelves; an armchair facing the door; a
     * candle still burning.
     */
    public static void buildRoom(ServerLevel level, BlockPos origin, int doorX) {
        int x0 = doorX - ROOM_HALF_WIDTH - 1;
        int x1 = doorX + ROOM_HALF_WIDTH + 1;
        int z0 = WALL_Z;
        int z1 = ROOM_MAX_Z + 1;

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = FLOOR_Y; y <= CEILING_Y; y++) {
                    boolean shell = y == FLOOR_Y || y == CEILING_Y || x == x0 || x == x1 || z == z0 || z == z1;
                    BlockState state;
                    if (!shell) {
                        state = Blocks.AIR.defaultBlockState();
                    } else if (y == FLOOR_Y) {
                        state = HouseShell.OAK_FLOOR;
                    } else if (y == CEILING_Y) {
                        state = HouseShell.CEILING;
                    } else {
                        state = HouseShell.PLASTER;
                    }
                    level.setBlock(origin.offset(x, y, z), state, BUILD_FLAGS);
                }
            }
        }

        // A beam across the ceiling, as in the bedrooms.
        for (int x = x0 + 1; x <= x1 - 1; x++) {
            level.setBlock(origin.offset(x, ROOM_MAX_Y, ROOM_MIN_Z + 4),
                    Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X),
                    BUILD_FLAGS);
        }

        int y = ROOM_MIN_Y;
        for (int x = doorX - 2; x <= doorX + 2; x++) {
            for (int z = ROOM_MIN_Z + 2; z <= ROOM_MIN_Z + 6; z++) {
                level.setBlock(origin.offset(x, y, z), Blocks.RED_CARPET.defaultBlockState(), BUILD_FLAGS);
            }
        }

        for (int dx = 1; dx <= ROOM_HALF_WIDTH; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                level.setBlock(origin.offset(doorX - dx, y + dy, ROOM_MAX_Z), Blocks.BOOKSHELF.defaultBlockState(), BUILD_FLAGS);
                level.setBlock(origin.offset(doorX + dx, y + dy, ROOM_MAX_Z), Blocks.BOOKSHELF.defaultBlockState(), BUILD_FLAGS);
            }
        }

        // The armchair's back is to the far wall: it faces the door.
        level.setBlock(origin.offset(doorX, y, ROOM_MAX_Z),
                Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), BUILD_FLAGS);
        level.setBlock(origin.offset(doorX - 1, y, ROOM_MAX_Z - 1),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), BUILD_FLAGS);
        level.setBlock(origin.offset(doorX - 1, y + 1, ROOM_MAX_Z - 1),
                Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true), BUILD_FLAGS);
        level.setBlock(origin.offset(doorX + ROOM_HALF_WIDTH, y, ROOM_MIN_Z), Blocks.POTTED_FERN.defaultBlockState(), BUILD_FLAGS);

        level.setBlock(origin.offset(doorX, ROOM_MAX_Y, ROOM_MIN_Z + 4 - 1), HouseInteriors.lantern(true), BUILD_FLAGS);
        level.setBlock(origin.offset(doorX, ROOM_MAX_Y, ROOM_MIN_Z + 4 + 1), HouseInteriors.lantern(true), BUILD_FLAGS);
    }

    /**
     * Copies the principal bedroom, its walls and its paintings from the
     * manor into the space between, in front of the room's door. Block
     * entities come without their inventories, and nothing in the copy can
     * be used or broken.
     */
    public static void copyBedroom(ServerLevel interior, ServerLevel between, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = COPY_MIN_X; x <= COPY_MAX_X; x++) {
            for (int y = COPY_MIN_Y; y <= COPY_MAX_Y; y++) {
                for (int z = COPY_MIN_Z; z <= COPY_MAX_Z; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    HouseDimensionMirror.copyStateAndBlockEntity(interior, between, pos);
                }
            }
        }

        AABB bounds = copyBounds(origin);
        for (Painting old : between.getEntitiesOfClass(Painting.class, bounds)) {
            old.discard();
        }
        for (Painting painting : interior.getEntitiesOfClass(Painting.class, bounds)) {
            Painting copy = new Painting(between, painting.getPos(), painting.getDirection(), painting.getVariant());
            between.addFreshEntity(copy);
        }
    }

    private static AABB copyBounds(BlockPos origin) {
        return new AABB(
                origin.getX() + COPY_MIN_X, origin.getY() + COPY_MIN_Y, origin.getZ() + COPY_MIN_Z,
                origin.getX() + COPY_MAX_X + 1, origin.getY() + COPY_MAX_Y + 1, origin.getZ() + COPY_MAX_Z + 1
        );
    }

    // ------------------------------------------------------------------
    // Going through

    /**
     * Clicking the door in the manor: it stays shut there (the client is told
     * where it is, so it never shows it opening onto the shelves) and the
     * player goes through to the space between instead.
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
        if (origin == null || !data.isRoomRevealed() || !isDoorCell(origin, data.roomDoorX(), event.getPos())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player && level.dimension().equals(HouseDimensions.INTERIOR)) {
            enter(player, data, origin);
        }
    }

    private static void enter(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        MinecraftServer server = player.server;
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        ServerLevel between = server.getLevel(HouseDimensions.BETWEEN);
        if (interior == null || between == null) {
            return;
        }
        int doorX = data.roomDoorX();
        HouseTransitionEvents.beginDoorTransition(
                player,
                HouseDimensions.BETWEEN,
                p -> {
                    copyBedroom(interior, between, origin);
                    setDoorOpen(between, doorPos(origin, doorX), false, null);
                    INSIDE.remove(p.getUUID());
                },
                p -> DOOR_OPENING.put(p.getUUID(), DOOR_OPEN_DELAY_TICKS)
        );
    }

    /**
     * Each tick for a player in the space between: opens the door in front
     * of someone who has just arrived, and returns them to the manor when
     * they come back out of the room or wander off from the door.
     */
    static void tickPlayer(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        if (!data.isRoomRevealed()) {
            return;
        }
        UUID id = player.getUUID();
        int doorX = data.roomDoorX();
        ServerLevel between = player.serverLevel();

        Integer opening = DOOR_OPENING.get(id);
        if (opening != null) {
            if (opening <= 0) {
                DOOR_OPENING.remove(id);
                setDoorOpen(between, doorPos(origin, doorX), true, player);
            } else {
                DOOR_OPENING.put(id, opening - 1);
            }
        }

        double relX = player.getX() - origin.getX();
        double relY = player.getY() - origin.getY();
        double relZ = player.getZ() - origin.getZ();
        if (relX < COPY_MIN_X - 4 || relX > COPY_MAX_X + 5
                || relZ < COPY_MIN_Z - 4 || relZ > ROOM_MAX_Z + 3
                || relY < FLOOR_Y - 4 || relY > CEILING_Y + 3) {
            // Somewhere else in this dimension (a command put them there).
            INSIDE.remove(id);
            return;
        }

        if (relZ >= ROOM_MIN_Z + 0.35D) {
            INSIDE.add(id);
            return;
        }

        boolean leave;
        if (INSIDE.contains(id)) {
            // Back out through the doorway into the bedroom.
            leave = relZ < WALL_Z - 0.35D;
        } else {
            double dx = relX - (doorX + 0.5D);
            double dz = relZ - WALL_Z;
            leave = dx * dx + dz * dz > APPROACH_RADIUS * APPROACH_RADIUS
                    || relY < FLOOR_Y + 0.5D
                    || relY > CEILING_Y;
        }
        if (leave) {
            returnToManor(player, origin, doorX);
        }
    }

    private static void returnToManor(ServerPlayer player, BlockPos origin, int doorX) {
        if (!HouseTransitionEvents.beginDoorTransition(player, HouseDimensions.INTERIOR, null, p -> {
            ServerLevel between = p.server.getLevel(HouseDimensions.BETWEEN);
            if (between != null) {
                setDoorOpen(between, doorPos(origin, doorX), false, null);
            }
            // The door they came through has swung shut behind them.
            p.serverLevel().playSound(null, doorPos(origin, doorX), SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS,
                    1.0F, 0.9F + p.getRandom().nextFloat() * 0.1F);
        })) {
            return;
        }
        INSIDE.remove(player.getUUID());
        DOOR_OPENING.remove(player.getUUID());
    }

    private static void setDoorOpen(ServerLevel level, BlockPos lower, boolean open, @Nullable ServerPlayer opener) {
        BlockState state = level.getBlockState(lower);
        if (!(state.getBlock() instanceof DoorBlock door) || state.getValue(DoorBlock.OPEN) == open) {
            return;
        }
        if (opener != null) {
            // Vanilla's own open, so its sound and game event are an ordinary door's.
            door.setOpen(opener, level, state, lower, open);
            return;
        }
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState current = level.getBlockState(half);
            if (current.getBlock() instanceof DoorBlock) {
                level.setBlock(half, current.setValue(DoorBlock.OPEN, open), BUILD_FLAGS);
            }
        }
    }

    /** Keeps the manor's side of the door shut, whatever opens it (redstone included). */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % DOOR_CHECK_INTERVAL != 0) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        if (origin == null || !data.isRoomRevealed()) {
            return;
        }
        BlockPos lower = doorPos(origin, data.roomDoorX());
        for (ServerLevel level : new ServerLevel[]{server.getLevel(HouseDimensions.INTERIOR), server.overworld()}) {
            if (level != null && level.isLoaded(lower)) {
                setDoorOpen(level, lower, false, null);
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
        DOOR_OPENING.remove(event.getEntity().getUUID());
    }

    public static void clearAll() {
        INSIDE.clear();
        DOOR_OPENING.clear();
    }

    // ------------------------------------------------------------------
    // Protection

    private static boolean isDoorCell(BlockPos origin, int doorX, BlockPos pos) {
        return pos.getX() - origin.getX() == doorX
                && pos.getZ() - origin.getZ() == WALL_Z
                && (pos.getY() - origin.getY() == DOOR_Y || pos.getY() - origin.getY() == DOOR_Y + 1);
    }

    /**
     * In the manor (and its Overworld proxy): the door, the partition around
     * it and the shelves behind it cannot be broken, pushed or blown up, so
     * the thinness of the wall can never be exposed.
     */
    public static boolean isProtectedHousePosition(HouseSavedData data, BlockPos origin, BlockPos pos) {
        if (!data.isRoomRevealed()) {
            return false;
        }
        int doorX = data.roomDoorX();
        int x = pos.getX() - origin.getX();
        int y = pos.getY() - origin.getY();
        int z = pos.getZ() - origin.getZ();
        boolean partition = z == WALL_Z && y >= DOOR_Y && y <= DOOR_Y + 3 && x >= doorX - 1 && x <= doorX + 1;
        boolean backing = z == WALL_Z + 1 && x == doorX && y >= DOOR_Y && y <= DOOR_Y + 2;
        return partition || backing;
    }

    /** In the space between: the copied bedroom, and the room's walls, floor and ceiling. */
    public static boolean isProtectedBetweenPosition(BlockPos origin, int doorX, BlockPos pos) {
        int x = pos.getX() - origin.getX();
        int y = pos.getY() - origin.getY();
        int z = pos.getZ() - origin.getZ();
        if (isInCopy(x, y, z)) {
            return true;
        }
        int x0 = doorX - ROOM_HALF_WIDTH - 1;
        int x1 = doorX + ROOM_HALF_WIDTH + 1;
        int z1 = ROOM_MAX_Z + 1;
        boolean inBox = x >= x0 && x <= x1 && y >= FLOOR_Y && y <= CEILING_Y && z >= WALL_Z && z <= z1;
        return inBox && (y == FLOOR_Y || y == CEILING_Y || x == x0 || x == x1 || z == WALL_Z || z == z1);
    }

    private static boolean isInCopy(int x, int y, int z) {
        return x >= COPY_MIN_X && x <= COPY_MAX_X
                && y >= COPY_MIN_Y && y <= COPY_MAX_Y
                && z >= COPY_MIN_Z && z <= COPY_MAX_Z;
    }

    @Nullable
    private static BlockPos betweenOrigin(Level level, HouseSavedData[] dataOut) {
        if (!level.dimension().equals(HouseDimensions.BETWEEN) || level.getServer() == null) {
            return null;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        if (!data.isRoomRevealed()) {
            return null;
        }
        dataOut[0] = data;
        return data.houseOrigin();
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        HouseSavedData[] data = new HouseSavedData[1];
        BlockPos origin = betweenOrigin(level, data);
        if (origin != null && isProtectedBetweenPosition(origin, data[0].roomDoorX(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        HouseSavedData[] data = new HouseSavedData[1];
        BlockPos origin = betweenOrigin(level, data);
        if (origin == null) {
            return;
        }
        BlockPos pos = event.getPos();
        if (isInCopy(pos.getX() - origin.getX(), pos.getY() - origin.getY(), pos.getZ() - origin.getZ())
                || isProtectedBetweenPosition(origin, data[0].roomDoorX(), pos)) {
            event.setCanceled(true);
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        HouseSavedData[] data = new HouseSavedData[1];
        BlockPos origin = betweenOrigin(event.getLevel(), data);
        if (origin != null) {
            int doorX = data[0].roomDoorX();
            event.getAffectedBlocks().removeIf(pos -> isProtectedBetweenPosition(origin, doorX, pos));
        }
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        HouseSavedData[] data = new HouseSavedData[1];
        BlockPos origin = betweenOrigin(level, data);
        if (origin == null) {
            return;
        }
        int doorX = data[0].roomDoorX();
        Direction direction = event.getDirection();
        BlockPos face = event.getFaceOffsetPos();
        if (isProtectedBetweenPosition(origin, doorX, face) || isProtectedBetweenPosition(origin, doorX, face.relative(direction))) {
            event.setCanceled(true);
            return;
        }
        var resolver = event.getStructureHelper();
        if (resolver != null && resolver.resolve()) {
            for (List<BlockPos> list : List.of(resolver.getToPush(), resolver.getToDestroy())) {
                for (BlockPos pos : list) {
                    if (isProtectedBetweenPosition(origin, doorX, pos) || isProtectedBetweenPosition(origin, doorX, pos.relative(direction))) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    /** Nothing in the copied bedroom can be used; only its door opens. */
    public static void onRightClickInBetween(PlayerInteractEvent.RightClickBlock event) {
        HouseSavedData[] data = new HouseSavedData[1];
        BlockPos origin = betweenOrigin(event.getLevel(), data);
        if (origin == null) {
            return;
        }
        BlockPos pos = event.getPos();
        int doorX = data[0].roomDoorX();
        if (isInCopy(pos.getX() - origin.getX(), pos.getY() - origin.getY(), pos.getZ() - origin.getZ())
                && !isDoorCell(origin, doorX, pos)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
