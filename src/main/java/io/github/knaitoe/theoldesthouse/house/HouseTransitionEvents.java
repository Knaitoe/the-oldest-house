package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseTransitionEvents {
    private static final double BOUNDARY_MARGIN = 0.55D;

    private HouseTransitionEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
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
                enterHouseDimension(player, data, kind);
            }
            return;
        }

        if (player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                && !isValidHouseInteriorSpace(player, data, origin)) {
            HouseTransitionKind kind = classifyBoundary(player, origin);
            leaveHouseDimension(player, kind);
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

        boolean front = relZ <= 1.35D;
        boolean left = relX <= 1.35D;
        boolean right = relX >= HouseBuilder.WIDTH - 1.35D;

        double centerX = HouseBuilder.WIDTH / 2.0D + 0.5D;

        if (front
                && Math.abs(relX - centerX) <= 1.0D
                && relY >= 0.55D
                && relY <= 3.45D) {
            return HouseTransitionKind.DOOR;
        }

        if (front
                && relY >= 1.35D
                && relY <= 4.35D
                && (within(relX, 1.65D, 4.35D) || within(relX, 10.65D, 13.35D))) {
            return HouseTransitionKind.WINDOW;
        }

        if ((left || right)
                && relY >= 1.35D
                && relY <= 4.35D
                && within(relZ, 3.65D, 6.35D)) {
            return HouseTransitionKind.WINDOW;
        }

        return HouseTransitionKind.BREACH;
    }

    private static boolean within(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private static void enterHouseDimension(
            ServerPlayer player,
            HouseSavedData data,
            HouseTransitionKind kind
    ) {
        ServerLevel interior = HouseDimensionMirror.ensureInitialized(player.getServer(), data);
        if (interior == null) {
            return;
        }

        sendTransitionContext(player, kind);
        teleportMatchingCoordinates(player, interior);
    }

    private static void leaveHouseDimension(ServerPlayer player, HouseTransitionKind kind) {
        sendTransitionContext(player, kind);
        teleportMatchingCoordinates(player, player.getServer().overworld());
    }

    private static void sendTransitionContext(ServerPlayer player, HouseTransitionKind kind) {
        PacketDistributor.sendToPlayer(
                player,
                new HouseTransitionContextPayload(kind)
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
}
