package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseRoomDoorPayload;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.decoration.HangingEntity;
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
 * side there are only bookshelves.
 *
 * The room is real, and so is its door, but they are not where the bedroom
 * is: they stand in "the pocket", high above the manor in the House
 * dimension, directly over it (see {@link #pocketDy}), in front of an exact
 * copy of the principal bedroom, which also has the real view outside its
 * windows. Nobody inside the manor can see that far up.
 *
 * Clicking the door in the real bedroom moves the player straight up into the
 * copy, by a relative shift of exactly the pocket's height, so position,
 * facing and momentum are untouched and nothing reloads: the chunks are the
 * ones they are already standing in. In the same tick the copy's door swings
 * open, with an ordinary door's sound, onto the room. Walking back out
 * through the doorway (or wandering away from the door without going in)
 * shifts them back down the same way. The real door never opens: there is
 * nothing behind it but shelves.
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

    // The principal bedroom with its walls, copied in front of the pocket's door.
    private static final int COPY_MIN_X = 0;
    private static final int COPY_MAX_X = 13;
    private static final int COPY_MIN_Y = FLOOR_Y;
    private static final int COPY_MAX_Y = CEILING_Y;
    private static final int COPY_MIN_Z = -1;
    private static final int COPY_MAX_Z = WALL_Z;

    /**
     * What can be seen from the bedroom that is not the bedroom: the view out
     * of its front and west windows, and the upper hall through its door.
     * Copied once, when the pocket is made. {x0, y0, z0, x1, y1, z1}.
     */
    private static final int[][] BACKDROP = {
            {-3, 0, -18, 17, 24, -2},
            {-18, 0, -2, -1, 24, 10},
            {14, FLOOR_Y, -1, 17, CEILING_Y, 10}
    };

    /** How far from the door someone can wander in the copied bedroom before being put back. */
    private static final double APPROACH_RADIUS = 6.0D;
    private static final int DOOR_CHECK_INTERVAL = 20;

    /** Walls and floors placed together, with no shape updates to break anything half-built. */
    private static final int BUILD_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** Players who have been inside the room since arriving; stepping back out returns them. */
    private static final Set<UUID> INSIDE = new HashSet<>();
    /** Whether this server session has checked the door's spot against the paintings. */
    private static boolean doorChecked;

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

    /** Whether a position (in the House dimension) is inside the pocket's copied bedroom or room. */
    public static boolean isInPocket(BlockPos origin, double x, double y, double z) {
        BlockPos pocket = pocketOrigin(origin);
        double relX = x - pocket.getX();
        double relY = y - pocket.getY();
        double relZ = z - pocket.getZ();
        return relX >= COPY_MIN_X - 1 && relX <= COPY_MAX_X + 2
                && relY >= FLOOR_Y - 1 && relY <= CEILING_Y + 1
                && relZ >= COPY_MIN_Z - 1 && relZ <= ROOM_MAX_Z + 2;
    }

    // ------------------------------------------------------------------
    // Appearing

    /**
     * Sets the door into the bedroom partition and builds the pocket above.
     *
     * @return false if the House dimension is missing
     */
    public static boolean reveal(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return false;
        }
        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return false;
        }

        int doorX = chooseDoorX(interior, origin);
        placeDoor(interior, origin, doorX);
        HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);
        data.markRoomRevealed(doorX);
        buildPocket(interior, data, origin);
        PacketDistributor.sendToAllPlayers(doorPayload(data));

        TheOldestHouse.LOGGER.info("A door has appeared in the principal bedroom of The Oldest House at {} (perceived age {}).",
                doorPos(origin, doorX), data.houseAge());
        return true;
    }

    /**
     * Builds the pocket (view, room, bedroom copy) if this house's room does
     * not have one yet.
     */
    public static void ensurePocket(MinecraftServer server) {
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isRoomRevealed()) {
            return;
        }
        if (!data.isRoomPocketBuilt()) {
            buildPocket(interior, data, origin);
        }
    }

    /**
     * Moves the door along the partition if something hangs right beside it
     * (a painting, a frame). Returns whether it moved.
     */
    public static boolean moveDoorIfCrowded(MinecraftServer server, ServerLevel interior, HouseSavedData data, BlockPos origin) {
        int oldX = data.roomDoorX();
        BlockPos oldDoor = doorPos(origin, oldX);
        // The partition's plaster goes back first, so the old spot is judged like any other.
        interior.setBlock(oldDoor, HouseShell.PLASTER, BUILD_FLAGS);
        interior.setBlock(oldDoor.above(), HouseShell.PLASTER, BUILD_FLAGS);
        if (isClearInFront(interior, origin, oldX)) {
            placeDoor(interior, origin, oldX);
            return false;
        }
        int newX = chooseDoorX(interior, origin);
        if (newX == oldX || !isClearInFront(interior, origin, newX)) {
            placeDoor(interior, origin, oldX);
            return false;
        }
        placeDoor(interior, origin, newX);
        HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);

        // The pocket's room moves with it.
        BlockPos pocket = pocketOrigin(origin);
        for (int x = oldX - ROOM_HALF_WIDTH - 1; x <= oldX + ROOM_HALF_WIDTH + 1; x++) {
            for (int y = FLOOR_Y; y <= CEILING_Y; y++) {
                for (int z = WALL_Z + 1; z <= ROOM_MAX_Z + 1; z++) {
                    interior.setBlock(pocket.offset(x, y, z), Blocks.AIR.defaultBlockState(), BUILD_FLAGS);
                }
            }
        }
        data.setRoomDoorX(newX);
        buildRoom(interior, pocket, newX);
        copyBedroom(interior, origin);
        PacketDistributor.sendToAllPlayers(doorPayload(data));
        TheOldestHouse.LOGGER.info("Moved the room between rooms' door from x+{} to x+{}, away from what hangs on the wall.", oldX, newX);
        return true;
    }

    private static void buildPocket(ServerLevel interior, HouseSavedData data, BlockPos origin) {
        int dy = pocketDy(origin);
        BlockPos.MutableBlockPos from = new BlockPos.MutableBlockPos();
        for (int[] box : BACKDROP) {
            for (int x = box[0]; x <= box[3]; x++) {
                for (int y = box[1]; y <= box[4]; y++) {
                    for (int z = box[2]; z <= box[5]; z++) {
                        from.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        BlockState state = interior.getBlockState(from);
                        BlockPos to = from.above(dy);
                        if (interior.getBlockState(to) != state) {
                            interior.setBlock(to, state.hasBlockEntity() ? Blocks.AIR.defaultBlockState() : state, BUILD_FLAGS);
                        }
                    }
                }
            }
        }
        buildRoom(interior, pocketOrigin(origin), data.roomDoorX());
        copyBedroom(interior, origin);
        data.markRoomPocketBuilt();
        TheOldestHouse.LOGGER.info("Built the room between rooms {} blocks above the manor.", dy);
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
        // Nothing hanging on the wall beside or above it: a door squeezed up
        // against a painting looks like what it is, an afterthought.
        BlockPos lower = origin.offset(x, DOOR_Y, WALL_Z - 1);
        AABB around = new AABB(lower.getX() - 1.0D, lower.getY(), lower.getZ(),
                lower.getX() + 2.0D, lower.getY() + 3.0D, lower.getZ() + 1.0D);
        return level.getEntitiesOfClass(HangingEntity.class, around).isEmpty();
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
     * Copies the principal bedroom, its walls and its paintings into the
     * pocket, in front of the room's door. Block entities come without their
     * inventories, and nothing in the copy can be used or broken.
     *
     * Paintings are matched one for one with any already hanging in the
     * copy, never added on top of them: two paintings in one place pop off
     * the wall and drop as items.
     */
    public static void copyBedroom(ServerLevel level, BlockPos origin) {
        int dy = pocketDy(origin);
        BlockPos.MutableBlockPos from = new BlockPos.MutableBlockPos();
        for (int x = COPY_MIN_X; x <= COPY_MAX_X; x++) {
            for (int y = COPY_MIN_Y; y <= COPY_MAX_Y; y++) {
                for (int z = COPY_MIN_Z; z <= COPY_MAX_Z; z++) {
                    from.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    copyUp(level, from.immutable(), dy);
                }
            }
        }

        AABB real = copyBounds(origin);
        AABB pocket = real.move(0, dy, 0);
        if (!entitiesLoaded(level, real) || !entitiesLoaded(level, pocket)) {
            return; // Not now: better no paintings than two.
        }
        List<Painting> copies = new ArrayList<>(level.getEntitiesOfClass(Painting.class, pocket));
        for (Painting painting : level.getEntitiesOfClass(Painting.class, real)) {
            BlockPos at = painting.getPos().above(dy);
            Painting match = null;
            for (Painting copy : copies) {
                if (copy.getPos().equals(at) && copy.getDirection() == painting.getDirection()) {
                    match = copy;
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
        // Anything left in the copy has no original any more.
        copies.forEach(Painting::discard);
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

    /** One block and its non-inventory block-entity data, copied straight up by {@code dy}. */
    private static void copyUp(ServerLevel level, BlockPos from, int dy) {
        BlockPos to = from.above(dy);
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
     * where it is, so it never shows it opening onto the shelves). The player
     * is shifted up into the copy, where the same door swings open.
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
        if (event.getEntity() instanceof ServerPlayer player
                && level instanceof ServerLevel interior
                && level.dimension().equals(HouseDimensions.INTERIOR)
                && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            enter(player, interior, data, origin);
        }
    }

    private static void enter(ServerPlayer player, ServerLevel interior, HouseSavedData data, BlockPos origin) {
        if (!data.isRoomPocketBuilt()) {
            buildPocket(interior, data, origin);
        }
        copyBedroom(interior, origin);
        int dy = pocketDy(origin);
        BlockPos pocketDoor = doorPos(origin, data.roomDoorX()).above(dy);
        setDoorOpen(interior, pocketDoor, false);
        INSIDE.remove(player.getUUID());

        shift(player, dy);
        setDoorOpen(interior, pocketDoor, true);
        interior.playSound(null, pocketDoor, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F,
                0.9F + interior.getRandom().nextFloat() * 0.1F);
    }

    /**
     * Each tick for a player in the House dimension. Returns true when they
     * are in the pocket (so the manor's own bounds do not apply), after
     * returning them to the real bedroom if they have come back out through
     * the doorway or wandered off from the door.
     */
    public static boolean tickPocket(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        if (!data.isRoomRevealed() || !isInPocket(origin, player.getX(), player.getY(), player.getZ())) {
            INSIDE.remove(player.getUUID());
            return false;
        }
        UUID id = player.getUUID();
        BlockPos pocket = pocketOrigin(origin);
        int doorX = data.roomDoorX();
        double relX = player.getX() - pocket.getX();
        double relY = player.getY() - pocket.getY();
        double relZ = player.getZ() - pocket.getZ();

        if (relZ >= ROOM_MIN_Z + 0.35D) {
            INSIDE.add(id);
            return true;
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
        return true;
    }

    private static void returnToManor(ServerPlayer player, BlockPos origin, int doorX) {
        int dy = pocketDy(origin);
        boolean cameThrough = INSIDE.remove(player.getUUID());
        shift(player, -dy);
        ServerLevel level = player.serverLevel();
        setDoorOpen(level, doorPos(origin, doorX).above(dy), false);
        if (cameThrough) {
            // The door they came through has swung shut behind them.
            BlockPos real = doorPos(origin, doorX);
            player.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WOODEN_DOOR_CLOSE), SoundSource.BLOCKS,
                    real.getX() + 0.5D, real.getY() + 0.5D, real.getZ() + 0.5D, 1.0F,
                    0.9F + player.getRandom().nextFloat() * 0.1F, player.getRandom().nextLong()));
        }
    }

    /**
     * Straight up or down by {@code dy}, sent to the client as a relative
     * move so nothing about where they stand, look or are moving changes.
     * The server's teleport takes the absolute destination; the relative
     * flags only shape the packet.
     */
    private static void shift(ServerPlayer player, int dy) {
        player.connection.teleport(player.getX(), player.getY() + dy, player.getZ(),
                player.getYRot(), player.getXRot(), RelativeMovement.ALL);
    }

    /** Players left in the old separate dimension by an earlier version are brought back to the bedroom. */
    static void rescueFromBetween(ServerPlayer player, HouseSavedData data, BlockPos origin) {
        if (HouseTransitionEvents.isPending(player)) {
            return;
        }
        int doorX = data.isRoomRevealed() ? data.roomDoorX() : 9;
        Vec3 target = Vec3.atBottomCenterOf(doorPos(origin, doorX).north());
        HouseTransitionEvents.beginDoorTransition(player, HouseDimensions.INTERIOR, null, null, target, 0.0F);
    }

    private static void setDoorOpen(ServerLevel level, BlockPos lower, boolean open) {
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState state = level.getBlockState(half);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN) != open) {
                level.setBlock(half, state.setValue(DoorBlock.OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
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
                setDoorOpen(level, lower, false);
            }
        }

        // Once per session, with the bedroom's paintings loaded and nobody
        // looking, a door crowded against a painting moves along the wall.
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (!doorChecked && interior != null && interior.isLoaded(lower)
                && interior.areEntitiesLoaded(ChunkPos.asLong(lower))
                && !HouseWatchers.isWatched(interior, lower)
                && !HouseWatchers.isWatched(interior, lower.above())
                && interior.players().stream().noneMatch(p -> isInPocket(origin, p.getX(), p.getY(), p.getZ()))) {
            doorChecked = true;
            moveDoorIfCrowded(server, interior, data, origin);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, doorPayload(HouseSavedData.get(player.server)));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        INSIDE.remove(event.getEntity().getUUID());
    }

    public static void clearAll() {
        INSIDE.clear();
        doorChecked = false;
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

    /**
     * In the pocket ({@code pocketOrigin} is the manor's origin shifted up):
     * the copied bedroom, and the room's walls, floor and ceiling.
     */
    public static boolean isProtectedPocketPosition(BlockPos pocketOrigin, int doorX, BlockPos pos) {
        int x = pos.getX() - pocketOrigin.getX();
        int y = pos.getY() - pocketOrigin.getY();
        int z = pos.getZ() - pocketOrigin.getZ();
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

    /** The manor's origin, if {@code level} is the House dimension and the room exists. */
    private static BlockPos pocketHouse(Level level) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return null;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        return data.isRoomRevealed() ? data.houseOrigin() : null;
    }

    private static boolean isProtectedInPocket(Level level, BlockPos pos) {
        BlockPos origin = pocketHouse(level);
        return origin != null
                && isProtectedPocketPosition(pocketOrigin(origin), HouseSavedData.get(level.getServer()).roomDoorX(), pos);
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && isProtectedInPocket(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        BlockPos origin = pocketHouse(level);
        if (origin == null) {
            return;
        }
        BlockPos rel = event.getPos().subtract(pocketOrigin(origin));
        if (isInCopy(rel.getX(), rel.getY(), rel.getZ()) || isProtectedInPocket(level, event.getPos())) {
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
        if (!(event.getLevel() instanceof Level level) || pocketHouse(level) == null) {
            return;
        }
        Direction direction = event.getDirection();
        BlockPos face = event.getFaceOffsetPos();
        if (isProtectedInPocket(level, face) || isProtectedInPocket(level, face.relative(direction))) {
            event.setCanceled(true);
            return;
        }
        var resolver = event.getStructureHelper();
        if (resolver != null && resolver.resolve()) {
            for (List<BlockPos> list : List.of(resolver.getToPush(), resolver.getToDestroy())) {
                for (BlockPos pos : list) {
                    if (isProtectedInPocket(level, pos) || isProtectedInPocket(level, pos.relative(direction))) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    /** Nothing in the copied bedroom can be used; only its door opens. */
    public static void onRightClickInPocket(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos origin = pocketHouse(level);
        if (origin == null || level.isClientSide()) {
            return;
        }
        BlockPos pocket = pocketOrigin(origin);
        BlockPos pos = event.getPos();
        BlockPos rel = pos.subtract(pocket);
        if (isInCopy(rel.getX(), rel.getY(), rel.getZ())
                && !isDoorCell(pocket, HouseSavedData.get(level.getServer()).roomDoorX(), pos)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
