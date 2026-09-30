package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionCancelPayload;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextPayload;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class HouseTransitionEvents {

    private static int nextToken = 1;
    private static final Map<UUID, PendingTransition> PENDING = new HashMap<>();
    private static final Map<UUID, PendingDoorClose> PENDING_DOOR_CLOSE = new HashMap<>();

    private HouseTransitionEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        tickPendingDoorClose(player);

        PendingTransition pending = PENDING.get(player.getUUID());
        if (pending != null) {
            tickPendingTransition(player, pending);
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        if (data.isOutdated() && HouseDimensions.isHouseDimension(player.serverLevel().dimension())) {
            leaveOutdatedHouse(player);
            return;
        }
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return;
        }

        double relX = player.getX() - origin.getX();
        double relY = player.getY() - origin.getY();
        double relZ = player.getZ() - origin.getZ();
        ResourceKey<Level> dimension = player.serverLevel().dimension();

        if (dimension.equals(Level.OVERWORLD)) {
            // The doorstep itself is crossed by the door's handle, not the wall.
            if (HouseLayout.isInsideDomesticVolume(relX, relY, relZ)
                    && !nearExteriorDoor(origin, player.getX(), player.getY(), player.getZ())) {
                scheduleEntry(player, data, relX, relY, relZ);
            }
            return;
        }

        if (dimension.equals(HouseDimensions.BETWEEN)) {
            HouseBetweenRoom.rescueFromBetween(player, data, origin);
            return;
        }

        if (dimension.equals(HouseDimensions.INTERIOR)
                && (HouseBetweenRoom.tickPocket(player, data, origin) || LabyrinthDoors.tickPlayer(player, origin))) {
            return;
        }

        if (dimension.equals(HouseDimensions.INTERIOR) && !isValidHouseInteriorSpace(data, origin, player, relX, relY, relZ)) {
            beginPendingTransition(player, classify(relX, relY, relZ), Level.OVERWORLD, HouseLayout.doorAt(relX, relY, relZ), null, null);
        }
    }

    /**
     * Moves a pending transition on. A transition goes
     * PREPARED -> SYNCING -> MOVING -> COMPLETE within one tick when all is
     * well; it waits in PREPARED (up to {@link #RESOLVE_ATTEMPTS} ticks) if
     * the destination level is not available yet, and ends FAILED, with the
     * player left where they are and the client told, if it cannot finish.
     * It is only forgotten once it has completed or failed.
     *
     * <p>The context payload was sent when the transition was prepared, and
     * packets on one connection are ordered, so the client has it before the
     * respawn packet the move sends. Nothing waits on the client.
     */
    private static void tickPendingTransition(ServerPlayer player, PendingTransition pending) {
        if (player.isRemoved() || player.isDeadOrDying()) {
            fail(player, pending, "the player is no longer there to move");
            return;
        }
        if (!player.serverLevel().dimension().equals(pending.from)) {
            // Something else moved them first (death and respawn, a command,
            // another mod). Their crossing is moot.
            fail(player, pending, "the player left " + pending.from.location() + " by another way");
            return;
        }

        ServerLevel destination = player.getServer().getLevel(pending.destination);
        if (destination == null) {
            if (++pending.attempts >= RESOLVE_ATTEMPTS) {
                fail(player, pending, "destination " + pending.destination.location() + " never became available");
            }
            return;
        }

        try {
            pending.phase = Phase.SYNCING;
            HouseSavedData data = HouseSavedData.get(player.getServer());
            BlockPos origin = data.houseOrigin();
            syncBeforeMove(player, pending, destination, data, origin);

            pending.phase = Phase.MOVING;
            move(player, pending, destination);
            pending.phase = Phase.COMPLETE;
            PENDING.remove(player.getUUID());

            settle(player, pending, origin);
        } catch (RuntimeException e) {
            TheOldestHouse.LOGGER.error("The Oldest House transition {} for {} threw during {}",
                    pending.token, player.getGameProfile().getName(), pending.phase, e);
            if (pending.arrived) {
                // The move itself happened; only what follows it went wrong.
                pending.phase = Phase.COMPLETE;
                PENDING.remove(player.getUUID());
            } else {
                fail(player, pending, "it threw during " + pending.phase);
            }
        }
    }

    /** Brings both sides of the seam up to date before the player crosses it. */
    private static void syncBeforeMove(
            ServerPlayer player,
            PendingTransition pending,
            ServerLevel destination,
            HouseSavedData data,
            @Nullable BlockPos origin
    ) {
        if (origin == null) {
            return;
        }
        if (pending.isLeaving() && data.isInteriorInitialized()) {
            // The Overworld proxy is only reconciled while someone there could
            // see it; bring it up to date before this player arrives.
            ServerLevel interior = player.getServer().getLevel(HouseDimensions.INTERIOR);
            if (interior != null) {
                HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, destination, origin);

                // Seed the reverse visual layer before arrival so domestic
                // House NPCs are already visible through the Overworld shell
                // on the player's first frame outside.
                HouseExteriorEntityMirror.syncDomesticToOverworldNow(
                        interior,
                        destination,
                        origin
                );
            }
        }

        if (pending.isEntering()) {
            // The proxy shell is not a playable interior. Clear any pets,
            // villagers or other mobs before the player disappears across the
            // dimension seam so nothing is visibly stranded in an inaccessible
            // duplicate of the room.
            ServerLevel overworld = player.getServer().overworld();
            HouseProxyEntityEvacuation.evacuateAll(overworld, origin);

            // Seed the visual exterior-entity layer before the player changes
            // dimensions. Their first frame inside can therefore still show
            // Hillary, villagers, mobs, etc. outside at matching coordinates.
            HouseExteriorEntityMirror.syncNow(overworld, destination, origin);
        }
    }

    private static void move(ServerPlayer player, PendingTransition pending, ServerLevel destination) {
        var companions = io.github.knaitoe.theoldesthouse.opening.CompanionOrders.followingAll(player);
        if (pending.before != null) {
            pending.before.accept(player);
        }

        if (pending.target != null) {
            player.stopRiding();
            player.teleportTo(destination, pending.target.x, pending.target.y, pending.target.z, pending.targetYaw, player.getXRot());
            player.setDeltaMovement(Vec3.ZERO);
        } else {
            teleportMatchingCoordinates(player, destination);
        }
        if (!player.serverLevel().dimension().equals(pending.destination)) {
            throw new IllegalStateException("the teleport did not arrive in " + pending.destination.location());
        }
        pending.arrived = true;
        for (var companion : companions) io.github.knaitoe.theoldesthouse.opening.CompanionOrders.followAcross(companion, player);

        if (pending.after != null) {
            pending.after.accept(player);
        }
    }

    /** What follows a completed crossing: the door shutting behind, the opening's first entry. */
    private static void settle(ServerPlayer player, PendingTransition pending, @Nullable BlockPos origin) {
        if (pending.door != null && origin != null) {
            // The door shuts once they are a block past it on the far side.
            PENDING_DOOR_CLOSE.put(player.getUUID(), new PendingDoorClose(origin, pending.door, pending.isEntering() ? -1.0D : 1.0D));
        }
        if (pending.isEntering()) {
            OpeningSequence.onEnteredHouse(player);
        }
    }

    /** Ends a transition that will not happen: logged, forgotten, and the client's held context released. */
    private static void fail(ServerPlayer player, PendingTransition pending, String reason) {
        pending.phase = Phase.FAILED;
        PENDING.remove(player.getUUID());
        TheOldestHouse.LOGGER.warn("The Oldest House transition {} for {} ({} -> {}) failed: {}",
                pending.token, player.getGameProfile().getName(),
                pending.from.location(), pending.destination.location(), reason);
        if (!player.hasDisconnected()) {
            HousePackets.send(player, new HouseTransitionCancelPayload(pending.token));
        }
    }

    /**
     * Someone inside a House whose layout this build no longer understands
     * (see {@link HouseSavedData#houseOrigin()}): nothing in there works any
     * more, and nowhere in it can be trusted to match the Overworld, so they
     * are taken back to the Overworld's spawn, on the ground.
     */
    private static void leaveOutdatedHouse(ServerPlayer player) {
        if (PENDING.containsKey(player.getUUID())) {
            return;
        }
        ServerLevel overworld = player.getServer().overworld();
        BlockPos spawn = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        TheOldestHouse.LOGGER.warn("{} was inside The Oldest House, whose layout is outdated; returning them to the Overworld spawn.",
                player.getGameProfile().getName());
        beginDoorTransition(player, Level.OVERWORLD, null, null, Vec3.atBottomCenterOf(spawn), player.getYRot());
    }

    /**
     * A door inside the House that leads to another House dimension at the
     * same coordinates. {@code before} runs just ahead of the move (with the
     * player still where they were) and {@code after} once they have arrived.
     *
     * @return false if the player is already on their way somewhere
     */
    public static boolean beginDoorTransition(
            ServerPlayer player,
            ResourceKey<Level> destination,
            @Nullable Consumer<ServerPlayer> before,
            @Nullable Consumer<ServerPlayer> after
    ) {
        if (PENDING.containsKey(player.getUUID())) {
            return false;
        }
        beginPendingTransition(player, HouseTransitionKind.DOOR, destination, null, before, after);
        return true;
    }

    /**
     * A door to another dimension that arrives at a set place rather than at
     * the same coordinates (the labyrinth's doors).
     */
    public static boolean beginDoorTransition(
            ServerPlayer player,
            ResourceKey<Level> destination,
            @Nullable Consumer<ServerPlayer> before,
            @Nullable Consumer<ServerPlayer> after,
            Vec3 target,
            float yaw
    ) {
        if (!beginDoorTransition(player, destination, before, after)) {
            return false;
        }
        PendingTransition pending = PENDING.get(player.getUUID());
        pending.target = target;
        pending.targetYaw = yaw;
        return true;
    }

    /** Whether the player is part-way through a transition. */
    public static boolean isPending(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }

    /** The phase the player's pending transition is in, or null if there is none. For tests and diagnostics. */
    @Nullable
    public static Phase pendingPhase(ServerPlayer player) {
        PendingTransition pending = PENDING.get(player.getUUID());
        return pending == null ? null : pending.phase;
    }

    /** Abandons the player's pending transition, if any, as a failure. */
    public static void cancelPending(ServerPlayer player, String reason) {
        PendingTransition pending = PENDING.get(player.getUUID());
        if (pending != null) {
            fail(player, pending, reason);
        }
    }

    /** Drops per-player state for a player who has left. */
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PENDING.remove(id);
        PENDING_DOOR_CLOSE.remove(id);
    }

    /** Drops all per-player state (server stop, world reset). */
    public static void clearAll() {
        PENDING.clear();
        PENDING_DOOR_CLOSE.clear();
        DOOR_IDLE.clear();
    }

    private static boolean isValidHouseInteriorSpace(
            HouseSavedData data,
            BlockPos origin,
            ServerPlayer player,
            double relX,
            double relY,
            double relZ
    ) {
        if (HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
            return true;
        }
        if (nearExteriorDoor(origin, player.getX(), player.getY(), player.getZ())) {
            // Just crossed at the door and not yet through it.
            return true;
        }

        if (HouseShifts.isInDeepenedHall(origin, player.getX(), player.getY(), player.getZ())
                || LabyrinthPlaces.isInStack(origin, player.getX(), player.getY(), player.getZ())) {
            return true;
        }
        return data.isImpossibleDoorRevealed()
                && HouseImpossibleHallway.isInsideWalkableVolume(origin, player.getX(), player.getY(), player.getZ());
    }

    private static HouseTransitionKind classify(double relX, double relY, double relZ) {
        if (HouseLayout.doorAt(relX, relY, relZ) != null) {
            return HouseTransitionKind.DOOR;
        }
        if (HouseLayout.isAtWindow(relX, relY, relZ)) {
            return HouseTransitionKind.WINDOW;
        }
        return HouseTransitionKind.BREACH;
    }

    private static void scheduleEntry(ServerPlayer player, HouseSavedData data, double relX, double relY, double relZ) {
        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(player.getServer(), data);
        if (interior == null) {
            return;
        }

        beginPendingTransition(
                player,
                classify(relX, relY, relZ),
                HouseDimensions.INTERIOR,
                HouseLayout.doorAt(relX, relY, relZ),
                null,
                null
        );
    }

    private static void beginPendingTransition(
            ServerPlayer player,
            HouseTransitionKind kind,
            ResourceKey<Level> destination,
            @Nullable HouseLayout.ExteriorDoor door,
            @Nullable Consumer<ServerPlayer> before,
            @Nullable Consumer<ServerPlayer> after
    ) {
        int token = nextToken;
        nextToken = nextToken == Integer.MAX_VALUE ? 1 : nextToken + 1;

        TheOldestHouse.LOGGER.info(
                "Prepared The Oldest House transition for {}: kind={}, token={}, from={}, to={}, pos=({}, {}, {})",
                player.getGameProfile().getName(),
                kind,
                token,
                player.serverLevel().dimension().location(),
                destination.location(),
                String.format("%.2f", player.getX()),
                String.format("%.2f", player.getY()),
                String.format("%.2f", player.getZ())
        );

        PENDING.put(player.getUUID(), new PendingTransition(player.serverLevel().dimension(), destination, token, door, before, after));
        HousePackets.send(player, new HouseTransitionContextPayload(kind, token));
    }

    private static void tickPendingDoorClose(ServerPlayer player) {
        PendingDoorClose pending = PENDING_DOOR_CLOSE.get(player.getUUID());
        if (pending == null) {
            return;
        }
        ResourceKey<Level> dimension = player.serverLevel().dimension();
        if (!dimension.equals(HouseDimensions.INTERIOR) && !dimension.equals(Level.OVERWORLD)) {
            PENDING_DOOR_CLOSE.remove(player.getUUID());
            return;
        }

        double d = outwardDistance(pending.origin, pending.door, player.getX(), player.getZ());
        boolean through = Math.signum(d) == pending.doneSide && Math.abs(d) >= 1.0D;
        boolean gone = player.distanceToSqr(doorCentre(pending.origin, pending.door)) > 36.0D
                || ++pending.ticks > DOOR_PASS_TIMEOUT_TICKS;
        if (!through && !gone) {
            return;
        }
        PENDING_DOOR_CLOSE.remove(player.getUUID());
        setExteriorDoor(player.getServer(), pending.origin, pending.door, false, player.serverLevel());
    }

    // ------------------------------------------------------------------
    // The front and back doors: crossing by the handle

    /** How close to an exterior door a player may stand on the wrong side of the wall, having just crossed at it. */
    private static final double DOOR_GRACE = 3.0D;
    private static final int DOOR_PASS_TIMEOUT_TICKS = 200;
    private static final int DOOR_IDLE_CLOSE_TICKS = 40;
    private static final int DOOR_IDLE_INTERVAL = 10;
    private static final Map<String, Integer> DOOR_IDLE = new HashMap<>();

    /**
     * Clicking a shut front or back door, from either side, crosses at it:
     * the switch is made while the view is a still, shut door, and the same
     * door then swings open on the other side. An open door works as any
     * door does; walking through one crosses at the wall instead.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        boolean fromOverworld = dimension.equals(Level.OVERWORLD);
        if (!fromOverworld && !dimension.equals(HouseDimensions.INTERIOR)) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(player.getServer());
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return;
        }
        HouseLayout.ExteriorDoor door = exteriorDoorAt(origin, event.getPos());
        if (door == null) {
            return;
        }
        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof DoorBlock) || state.getValue(DoorBlock.OPEN)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (PENDING.containsKey(player.getUUID())) {
            return;
        }
        if (fromOverworld && HouseInteriorInitializer.ensureInitialized(player.getServer(), data) == null) {
            return;
        }
        beginPendingTransition(
                player,
                HouseTransitionKind.DOOR,
                fromOverworld ? HouseDimensions.INTERIOR : Level.OVERWORLD,
                door,
                null,
                p -> setExteriorDoor(p.getServer(), origin, door, true, p.serverLevel())
        );
    }

    /** Doors left open with nobody at them swing shut, so the next crossing is by the handle. */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % DOOR_IDLE_INTERVAL != 0) {
            return;
        }
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null) {
            DOOR_IDLE.clear();
            return;
        }
        for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
            BlockPos lower = origin.offset(door.x(), door.y(), door.z());
            if (!interior.isLoaded(lower)) {
                continue;
            }
            BlockState state = interior.getBlockState(lower);
            Vec3 centre = doorCentre(origin, door);
            if (!(state.getBlock() instanceof DoorBlock) || !state.getValue(DoorBlock.OPEN)
                    || anyoneWithin(server.overworld(), centre, 2.0D) || anyoneWithin(interior, centre, 2.0D)) {
                DOOR_IDLE.remove(door.name());
                continue;
            }
            if (DOOR_IDLE.merge(door.name(), DOOR_IDLE_INTERVAL, Integer::sum) >= DOOR_IDLE_CLOSE_TICKS) {
                DOOR_IDLE.remove(door.name());
                setExteriorDoor(server, origin, door, false, null);
            }
        }
    }

    private static boolean anyoneWithin(ServerLevel level, Vec3 point, double radius) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(point) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static HouseLayout.ExteriorDoor exteriorDoorAt(BlockPos origin, BlockPos pos) {
        for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
            BlockPos lower = origin.offset(door.x(), door.y(), door.z());
            if (pos.equals(lower) || pos.equals(lower.above())) {
                return door;
            }
        }
        return null;
    }

    private static Vec3 doorCentre(BlockPos origin, HouseLayout.ExteriorDoor door) {
        return Vec3.atCenterOf(origin.offset(door.x(), door.y(), door.z()));
    }

    /** Signed distance from the door's plane: positive outside the house. */
    private static double outwardDistance(BlockPos origin, HouseLayout.ExteriorDoor door, double x, double z) {
        Vec3 c = doorCentre(origin, door);
        return switch (door.face()) {
            case NORTH -> c.z - z;
            case SOUTH -> z - c.z;
            case EAST -> x - c.x;
            case WEST -> c.x - x;
        };
    }

    private static boolean nearExteriorDoor(BlockPos origin, double x, double y, double z) {
        for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
            Vec3 c = doorCentre(origin, door);
            double dx = x - c.x;
            double dz = z - c.z;
            if (dx * dx + dz * dz <= DOOR_GRACE * DOOR_GRACE && y >= c.y - 2.0D && y <= c.y + 3.0D) {
                return true;
            }
        }
        return false;
    }

    /**
     * Opens or shuts an exterior door on the authoritative (House) side and
     * mirrors it, with an ordinary door's sound in {@code soundLevel} (both
     * dimensions if null).
     */
    private static void setExteriorDoor(
            MinecraftServer server,
            BlockPos origin,
            HouseLayout.ExteriorDoor door,
            boolean open,
            @Nullable ServerLevel soundLevel
    ) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        ServerLevel overworld = server.overworld();
        if (interior == null) {
            return;
        }
        BlockPos lower = origin.offset(door.x(), door.y(), door.z());
        boolean changed = false;
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState state = interior.getBlockState(half);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN) != open) {
                interior.setBlock(half, state.setValue(DoorBlock.OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                changed = true;
            }
            HouseDimensionMirror.copyStateAndBlockEntity(interior, overworld, half);
        }
        if (!changed) {
            return;
        }
        for (ServerLevel level : soundLevel != null ? new ServerLevel[]{soundLevel} : new ServerLevel[]{interior, overworld}) {
            level.playSound(null, lower, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
    }

    private static void teleportMatchingCoordinates(ServerPlayer player, ServerLevel destination) {
        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.stopRiding();
        player.teleportTo(destination, player.getX(), player.getY(), player.getZ(), yaw, pitch);
        player.setDeltaMovement(movement);
    }

    /** Where a transition has got to. PREPARED is the only phase that outlasts a tick. */
    public enum Phase {
        /** Context sent to the client; waiting for the next tick and for the destination to exist. */
        PREPARED,
        /** Mirrors and proxies on both sides of the seam being brought up to date. */
        SYNCING,
        /** The before hook, the teleport and the after hook. */
        MOVING,
        COMPLETE,
        FAILED
    }

    /** How many ticks a transition waits for its destination level before giving up. */
    private static final int RESOLVE_ATTEMPTS = 20;

    /** Mutable: updated in place every tick rather than re-allocated. */
    private static final class PendingTransition {
        final ResourceKey<Level> from;
        final ResourceKey<Level> destination;
        final int token;
        @Nullable
        final HouseLayout.ExteriorDoor door;
        @Nullable
        final Consumer<ServerPlayer> before;
        @Nullable
        final Consumer<ServerPlayer> after;
        @Nullable
        Vec3 target;
        float targetYaw;
        Phase phase = Phase.PREPARED;
        int attempts;
        /** The teleport itself has happened; anything that fails after it cannot undo the crossing. */
        boolean arrived;

        PendingTransition(
                ResourceKey<Level> from,
                ResourceKey<Level> destination,
                int token,
                @Nullable HouseLayout.ExteriorDoor door,
                @Nullable Consumer<ServerPlayer> before,
                @Nullable Consumer<ServerPlayer> after
        ) {
            this.from = from;
            this.destination = destination;
            this.token = token;
            this.door = door;
            this.before = before;
            this.after = after;
        }

        /** Crossing the manor's own boundary inwards, as opposed to a door within the House. */
        boolean isEntering() {
            return destination.equals(HouseDimensions.INTERIOR) && from.equals(Level.OVERWORLD);
        }

        boolean isLeaving() {
            return destination.equals(Level.OVERWORLD) && from.equals(HouseDimensions.INTERIOR);
        }
    }

    private static final class PendingDoorClose {
        final BlockPos origin;
        final HouseLayout.ExteriorDoor door;
        /** Which side of the door's plane counts as through: +1 outside, -1 inside. */
        final double doneSide;
        int ticks;

        PendingDoorClose(BlockPos origin, HouseLayout.ExteriorDoor door, double doneSide) {
            this.origin = origin;
            this.door = door;
            this.doneSide = doneSide;
        }
    }
}
