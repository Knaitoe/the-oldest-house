package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextPayload;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseTransitionEvents {
    private static final int ACK_TIMEOUT_TICKS = 40;
    private static final int DOOR_CLOSE_DELAY_TICKS = 8;

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
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return;
        }

        double relX = player.getX() - origin.getX();
        double relY = player.getY() - origin.getY();
        double relZ = player.getZ() - origin.getZ();
        ResourceKey<Level> dimension = player.serverLevel().dimension();

        if (dimension.equals(Level.OVERWORLD)) {
            if (HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
                scheduleEntry(player, data, relX, relY, relZ);
            }
            return;
        }

        if (dimension.equals(HouseDimensions.BETWEEN)) {
            HouseBetweenRoom.tickPlayer(player, data, origin);
            return;
        }

        if (dimension.equals(HouseDimensions.INTERIOR) && !isValidHouseInteriorSpace(data, origin, player, relX, relY, relZ)) {
            beginPendingTransition(player, classify(relX, relY, relZ), Level.OVERWORLD, HouseLayout.doorAt(relX, relY, relZ), null, null);
        }
    }

    private static void tickPendingTransition(ServerPlayer player, PendingTransition pending) {
        if (!pending.acknowledged) {
            if (++pending.waitedTicks > ACK_TIMEOUT_TICKS) {
                // Do not perform an unclassified fallback teleport. Cancel and
                // allow the boundary to retrigger instead.
                PENDING.remove(player.getUUID());
            }
            return;
        }

        PENDING.remove(player.getUUID());
        ServerLevel destination = player.getServer().getLevel(pending.destination);
        if (destination == null) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        BlockPos origin = data.houseOrigin();
        // Crossing the manor's own boundary, as opposed to a door within the
        // House (the room between rooms and back), which changes neither.
        boolean entering = pending.destination.equals(HouseDimensions.INTERIOR) && pending.from.equals(Level.OVERWORLD);
        boolean leaving = pending.destination.equals(Level.OVERWORLD) && pending.from.equals(HouseDimensions.INTERIOR);

        if (leaving && origin != null && data.isInteriorInitialized()) {
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

        if (entering && origin != null) {
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

        if (pending.before != null) {
            pending.before.accept(player);
        }

        if (pending.target != null) {
            player.stopRiding();
            player.teleportTo(destination, pending.target.x, pending.target.y, pending.target.z, pending.targetYaw, 0.0F);
            player.setDeltaMovement(Vec3.ZERO);
        } else {
            teleportMatchingCoordinates(player, destination);
        }

        if (pending.after != null) {
            pending.after.accept(player);
        }

        if (entering && pending.door != null && origin != null) {
            PENDING_DOOR_CLOSE.put(player.getUUID(), new PendingDoorClose(origin, pending.door));
        }
        if (entering) {
            OpeningSequence.onEnteredHouse(player);
        }
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

    public static void acknowledgeContext(ServerPlayer player, int token) {
        PendingTransition pending = PENDING.get(player.getUUID());
        if (pending != null && pending.token == token) {
            pending.acknowledged = true;
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

        if (HouseShifts.isInDeepenedHall(origin, player.getX(), player.getY(), player.getZ())) {
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
        PacketDistributor.sendToPlayer(player, new HouseTransitionContextPayload(kind, token));
    }

    private static void tickPendingDoorClose(ServerPlayer player) {
        PendingDoorClose pending = PENDING_DOOR_CLOSE.get(player.getUUID());
        if (pending == null) {
            return;
        }

        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            PENDING_DOOR_CLOSE.remove(player.getUUID());
            return;
        }

        if (pending.ticksRemaining-- > 0) {
            return;
        }

        PENDING_DOOR_CLOSE.remove(player.getUUID());
        closeDoorBehindPlayer(player, pending.origin, pending.door);
    }

    private static void closeDoorBehindPlayer(ServerPlayer player, BlockPos origin, HouseLayout.ExteriorDoor exteriorDoor) {
        ServerLevel interior = player.getServer().getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            return;
        }

        BlockPos lowerPos = origin.offset(exteriorDoor.x(), exteriorDoor.y(), exteriorDoor.z());
        BlockPos upperPos = lowerPos.above();
        BlockState lowerState = interior.getBlockState(lowerPos);

        if (!(lowerState.getBlock() instanceof DoorBlock door) || !lowerState.getValue(DoorBlock.OPEN)) {
            return;
        }

        // Let vanilla own the actual close interaction so its sound and game
        // event remain indistinguishable from an ordinary wooden door.
        door.setOpen(player, interior, lowerState, lowerPos, false);

        // Be explicit about both halves before mirroring. This avoids relying
        // on the timing of the paired-door neighbor update.
        for (BlockPos pos : new BlockPos[]{lowerPos, upperPos}) {
            BlockState current = interior.getBlockState(pos);
            if (current.getBlock() instanceof DoorBlock && current.getValue(DoorBlock.OPEN)) {
                interior.setBlock(pos, current.setValue(DoorBlock.OPEN, false), 10);
            }
        }

        ServerLevel overworld = player.getServer().overworld();
        HouseDimensionMirror.copyStateAndBlockEntity(interior, overworld, lowerPos);
        HouseDimensionMirror.copyStateAndBlockEntity(interior, overworld, upperPos);
    }

    private static void teleportMatchingCoordinates(ServerPlayer player, ServerLevel destination) {
        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.stopRiding();
        player.teleportTo(destination, player.getX(), player.getY(), player.getZ(), yaw, pitch);
        player.setDeltaMovement(movement);
    }

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
        boolean acknowledged;
        int waitedTicks;
        @Nullable
        Vec3 target;
        float targetYaw;

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
    }

    private static final class PendingDoorClose {
        final BlockPos origin;
        final HouseLayout.ExteriorDoor door;
        int ticksRemaining = DOOR_CLOSE_DELAY_TICKS;

        PendingDoorClose(BlockPos origin, HouseLayout.ExteriorDoor door) {
            this.origin = origin;
            this.door = door;
        }
    }
}
