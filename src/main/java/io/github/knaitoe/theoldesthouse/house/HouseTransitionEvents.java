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

        if (dimension.equals(HouseDimensions.INTERIOR) && !isValidHouseInteriorSpace(data, origin, player, relX, relY, relZ)) {
            beginPendingTransition(player, classify(relX, relY, relZ), Level.OVERWORLD, HouseLayout.doorAt(relX, relY, relZ), null);
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
        boolean entering = pending.destination.equals(HouseDimensions.INTERIOR);

        if (!entering && origin != null && data.isInteriorInitialized()) {
            // The Overworld proxy is only reconciled while someone there could
            // see it; bring it up to date before this player arrives.
            ServerLevel interior = player.getServer().getLevel(HouseDimensions.INTERIOR);
            if (interior != null) {
                HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, destination, origin);
            }
        }

        if (pending.arrival != null) {
            Arrival arrival = pending.arrival;
            arrival.beforeTeleport().accept(player);
            player.stopRiding();
            player.teleportTo(destination, arrival.pos().x, arrival.pos().y, arrival.pos().z, arrival.yaw(), arrival.pitch());
            player.setDeltaMovement(Vec3.ZERO);
            arrival.afterTeleport().accept(player);
        } else {
            teleportMatchingCoordinates(player, destination);
        }

        if (entering && pending.door != null && origin != null) {
            PENDING_DOOR_CLOSE.put(player.getUUID(), new PendingDoorClose(origin, pending.door));
        }
        if (entering) {
            OpeningSequence.onEnteredHouse(player);
        }
    }

    /**
     * Enters the House through a door somewhere else entirely (the opening
     * sequence's entrance door): the same classified DOOR hand-off as the
     * front door, but arriving at an explicit position in the House instead
     * of at matching coordinates.
     *
     * @return false if a transition is already pending or the House
     *         interior is unavailable
     */
    public static boolean beginEntranceTransition(
            ServerPlayer player,
            Vec3 position,
            float yaw,
            float pitch,
            Consumer<ServerPlayer> beforeTeleport,
            Consumer<ServerPlayer> afterTeleport
    ) {
        if (PENDING.containsKey(player.getUUID())) {
            return false;
        }
        HouseSavedData data = HouseSavedData.get(player.getServer());
        if (!data.isSpawned() || data.houseOrigin() == null) {
            return false;
        }
        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(player.getServer(), data);
        if (interior == null) {
            return false;
        }

        beginPendingTransition(
                player,
                HouseTransitionKind.DOOR,
                HouseDimensions.INTERIOR,
                null,
                new Arrival(position, yaw, pitch, beforeTeleport, afterTeleport)
        );
        return true;
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
                null
        );
    }

    private static void beginPendingTransition(
            ServerPlayer player,
            HouseTransitionKind kind,
            ResourceKey<Level> destination,
            @Nullable HouseLayout.ExteriorDoor door,
            @Nullable Arrival arrival
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

        PENDING.put(player.getUUID(), new PendingTransition(destination, token, door, arrival));
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

    /** An explicit arrival point, with hooks either side of the teleport. */
    private record Arrival(
            Vec3 pos,
            float yaw,
            float pitch,
            Consumer<ServerPlayer> beforeTeleport,
            Consumer<ServerPlayer> afterTeleport
    ) {
    }

    /** Mutable: updated in place every tick rather than re-allocated. */
    private static final class PendingTransition {
        final ResourceKey<Level> destination;
        final int token;
        @Nullable
        final HouseLayout.ExteriorDoor door;
        @Nullable
        final Arrival arrival;
        boolean acknowledged;
        int waitedTicks;

        PendingTransition(
                ResourceKey<Level> destination,
                int token,
                @Nullable HouseLayout.ExteriorDoor door,
                @Nullable Arrival arrival
        ) {
            this.destination = destination;
            this.token = token;
            this.door = door;
            this.arrival = arrival;
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
