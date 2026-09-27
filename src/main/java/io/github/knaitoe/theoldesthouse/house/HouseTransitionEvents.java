package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseTransitionEvents {
    private static final double BOUNDARY_MARGIN = 0.55D;

    // Give the context payload one full server tick to reach the client before
    // Minecraft sends the actual dimension-change packet. Without this, the
    // transition-screen factory can win the race and fall back to DOOR.
    private static final int CONTEXT_LEAD_TICKS = 1;
    private static final Map<UUID, PendingTransition> PENDING = new HashMap<>();

    private HouseTransitionEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PendingTransition pending = PENDING.get(player.getUUID());
        if (pending != null) {
            if (pending.ticksRemaining() > 0) {
                PENDING.put(
                        player.getUUID(),
                        new PendingTransition(
                                pending.destination(),
                                pending.ticksRemaining() - 1
                        )
                );
                return;
            }

            PENDING.remove(player.getUUID());
            ServerLevel destination = player.getServer().getLevel(pending.destination());
            if (destination != null) {
                teleportMatchingCoordinates(player, destination);
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

        // The authored front door occupies the single center block. Keep this
        // deliberately narrow so mining the plaster beside it is still BREACH.
        double doorCenterX = HouseBuilder.WIDTH / 2.0D + 0.5D;

        if (front
                && Math.abs(relX - doorCenterX) <= 0.52D
                && relY >= 0.55D
                && relY <= 3.20D) {
            return HouseTransitionKind.DOOR;
        }

        // Authored front windows: x = 2,3 and x = 11,12.
        if (front
                && relY >= 1.35D
                && relY <= 4.35D
                && (within(relX, 1.75D, 4.25D)
                || within(relX, 10.75D, 13.25D))) {
            return HouseTransitionKind.WINDOW;
        }

        // Authored side windows: z = 4,5.
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
        ServerLevel interior = HouseDimensionMirror.ensureInitialized(player.getServer(), data);
        if (interior == null) {
            return;
        }

        beginPendingTransition(player, kind, HouseDimensions.INTERIOR);
    }

    private static void scheduleExit(ServerPlayer player, HouseTransitionKind kind) {
        beginPendingTransition(player, kind, Level.OVERWORLD);
    }

    private static void beginPendingTransition(
            ServerPlayer player,
            HouseTransitionKind kind,
            ResourceKey<Level> destination
    ) {
        PacketDistributor.sendToPlayer(
                player,
                new HouseTransitionContextPayload(kind)
        );

        PENDING.put(
                player.getUUID(),
                new PendingTransition(destination, CONTEXT_LEAD_TICKS)
        );
    }

    private static void teleportMatchingCoordinates(ServerPlayer player, ServerLevel destination) {
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

    private record PendingTransition(ResourceKey<Level> destination, int ticksRemaining) {
    }
}
