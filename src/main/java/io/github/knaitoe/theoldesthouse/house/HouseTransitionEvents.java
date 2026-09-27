package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseTransitionEvents {
    private static final double BOUNDARY_MARGIN = 0.55D;
    private static final int ACK_TIMEOUT_TICKS = 40;
    private static final int FRONT_DOOR_CLOSE_DELAY_TICKS = 8;

    private static final AtomicInteger NEXT_TOKEN = new AtomicInteger(1);
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
            if (pending.acknowledged()) {
                PENDING.remove(player.getUUID());
                ServerLevel destination = player.getServer().getLevel(
                        pending.destination()
                );
                if (destination != null) {
                    boolean enteringThroughFrontDoor =
                            pending.kind() == HouseTransitionKind.DOOR
                                    && pending.destination().equals(HouseDimensions.INTERIOR);

                    teleportMatchingCoordinates(player, destination);

                    if (enteringThroughFrontDoor) {
                        HouseSavedData data = HouseSavedData.get(player.getServer());
                        data.housePosition().ifPresent(origin ->
                                PENDING_DOOR_CLOSE.put(
                                        player.getUUID(),
                                        new PendingDoorClose(
                                                origin.immutable(),
                                                FRONT_DOOR_CLOSE_DELAY_TICKS
                                        )
                                )
                        );
                    }
                }
                return;
            }

            int waited = pending.waitedTicks() + 1;
            if (waited > ACK_TIMEOUT_TICKS) {
                // Do not perform an unclassified fallback teleport. Cancel and
                // allow the boundary to retrigger instead.
                PENDING.remove(player.getUUID());
            } else {
                PENDING.put(
                        player.getUUID(),
                        pending.withWaitedTicks(waited)
                );
            }
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        if (!data.isSpawned()) {
            return;
        }

        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null) {
            return;
        }

        if (player.serverLevel().dimension().equals(Level.OVERWORLD)) {
            if (isInsideDomesticVolume(player, origin)) {
                HouseTransitionKind kind = classifyBoundary(player, origin);
                scheduleEntry(player, data, kind);
            }
            return;
        }

        if (player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                && !isValidHouseInteriorSpace(player, data, origin)) {
            HouseTransitionKind kind = classifyBoundary(player, origin);
            scheduleExit(player, kind);
        }
    }

    public static void acknowledgeContext(ServerPlayer player, int token) {
        PendingTransition pending = PENDING.get(player.getUUID());
        if (pending == null || pending.token() != token) {
            return;
        }

        PENDING.put(
                player.getUUID(),
                pending.acknowledge()
        );
    }

    private static boolean isInsideDomesticVolume(ServerPlayer player, BlockPos origin) {
        double minX = origin.getX() + BOUNDARY_MARGIN;
        double maxX = origin.getX() + HouseBuilder.WIDTH - BOUNDARY_MARGIN;
        double minZ = origin.getZ() + BOUNDARY_MARGIN;
        double maxZ = origin.getZ() + HouseBuilder.DEPTH - BOUNDARY_MARGIN;
        double minY = origin.getY() + 0.35D;
        double maxY = origin.getY() + 5.45D;

        return player.getX() >= minX
                && player.getX() <= maxX
                && player.getZ() >= minZ
                && player.getZ() <= maxZ
                && player.getY() >= minY
                && player.getY() <= maxY;
    }

    private static boolean isValidHouseInteriorSpace(
            ServerPlayer player,
            HouseSavedData data,
            BlockPos origin
    ) {
        if (isInsideDomesticVolume(player, origin)) {
            return true;
        }

        return data.isImpossibleDoorRevealed()
                && HouseImpossibleHallway.isInsideWalkableVolume(
                        origin,
                        player.getX(),
                        player.getY(),
                        player.getZ()
                );
    }

    private static HouseTransitionKind classifyBoundary(ServerPlayer player, BlockPos origin) {
        double relX = player.getX() - origin.getX();
        double relY = player.getY() - origin.getY();
        double relZ = player.getZ() - origin.getZ();

        boolean front = relZ <= 1.20D;
        boolean left = relX <= 1.20D;
        boolean right = relX >= HouseBuilder.WIDTH - 1.20D;

        double doorCenterX = HouseBuilder.WIDTH / 2.0D + 0.5D;

        if (front
                && Math.abs(relX - doorCenterX) <= 0.52D
                && relY >= 0.55D
                && relY <= 3.20D) {
            return HouseTransitionKind.DOOR;
        }

        if (front
                && relY >= 1.35D
                && relY <= 4.35D
                && (within(relX, 1.75D, 4.25D)
                || within(relX, 10.75D, 13.25D))) {
            return HouseTransitionKind.WINDOW;
        }

        if ((left || right)
                && relY >= 1.35D
                && relY <= 4.35D
                && within(relZ, 3.75D, 6.25D)) {
            return HouseTransitionKind.WINDOW;
        }

        return HouseTransitionKind.BREACH;
    }

    private static boolean within(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private static void scheduleEntry(
            ServerPlayer player,
            HouseSavedData data,
            HouseTransitionKind kind
    ) {
        ServerLevel interior = HouseDimensionMirror.ensureInitialized(
                player.getServer(),
                data
        );
        if (interior == null) {
            return;
        }

        beginPendingTransition(player, kind, HouseDimensions.INTERIOR);
    }

    private static void scheduleExit(
            ServerPlayer player,
            HouseTransitionKind kind
    ) {
        beginPendingTransition(player, kind, Level.OVERWORLD);
    }

    private static void beginPendingTransition(
            ServerPlayer player,
            HouseTransitionKind kind,
            ResourceKey<Level> destination
    ) {
        int token = NEXT_TOKEN.getAndUpdate(
                current -> current == Integer.MAX_VALUE ? 1 : current + 1
        );

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

        PENDING.put(
                player.getUUID(),
                new PendingTransition(
                        destination,
                        kind,
                        token,
                        false,
                        0
                )
        );

        PacketDistributor.sendToPlayer(
                player,
                new HouseTransitionContextPayload(kind, token)
        );
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

        if (pending.ticksRemaining() > 0) {
            PENDING_DOOR_CLOSE.put(
                    player.getUUID(),
                    pending.withTicksRemaining(pending.ticksRemaining() - 1)
            );
            return;
        }

        PENDING_DOOR_CLOSE.remove(player.getUUID());
        closeFrontDoorBehindPlayer(player, pending.origin());
    }

    private static void closeFrontDoorBehindPlayer(
            ServerPlayer player,
            BlockPos origin
    ) {
        ServerLevel interior = player.getServer().getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            return;
        }

        BlockPos lowerPos = origin.offset(HouseBuilder.WIDTH / 2, 1, 0);
        BlockPos upperPos = lowerPos.above();
        BlockState lowerState = interior.getBlockState(lowerPos);

        if (!(lowerState.getBlock() instanceof DoorBlock door)
                || !lowerState.getValue(DoorBlock.OPEN)) {
            return;
        }

        // Let vanilla own the actual close interaction so its sound and game
        // event remain indistinguishable from an ordinary wooden door.
        door.setOpen(player, interior, lowerState, lowerPos, false);

        // Be explicit about both halves before mirroring. This avoids relying
        // on the timing of the paired-door neighbor update.
        BlockState currentLower = interior.getBlockState(lowerPos);
        if (currentLower.getBlock() instanceof DoorBlock
                && currentLower.getValue(DoorBlock.OPEN)) {
            interior.setBlock(
                    lowerPos,
                    currentLower.setValue(DoorBlock.OPEN, false),
                    10
            );
        }

        BlockState currentUpper = interior.getBlockState(upperPos);
        if (currentUpper.getBlock() instanceof DoorBlock
                && currentUpper.getValue(DoorBlock.OPEN)) {
            interior.setBlock(
                    upperPos,
                    currentUpper.setValue(DoorBlock.OPEN, false),
                    10
            );
        }

        ServerLevel overworld = player.getServer().overworld();
        HouseDimensionMirror.copyState(interior, overworld, lowerPos);
        HouseDimensionMirror.copyState(interior, overworld, upperPos);
    }

    private static void teleportMatchingCoordinates(
            ServerPlayer player,
            ServerLevel destination
    ) {
        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.stopRiding();
        player.teleportTo(
                destination,
                player.getX(),
                player.getY(),
                player.getZ(),
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }

    private record PendingTransition(
            ResourceKey<Level> destination,
            HouseTransitionKind kind,
            int token,
            boolean acknowledged,
            int waitedTicks
    ) {
        PendingTransition acknowledge() {
            return new PendingTransition(
                    destination,
                    kind,
                    token,
                    true,
                    waitedTicks
            );
        }

        PendingTransition withWaitedTicks(int ticks) {
            return new PendingTransition(
                    destination,
                    kind,
                    token,
                    acknowledged,
                    ticks
            );
        }
    }

    private record PendingDoorClose(
            BlockPos origin,
            int ticksRemaining
    ) {
        PendingDoorClose withTicksRemaining(int ticks) {
            return new PendingDoorClose(origin, ticks);
        }
    }
}
