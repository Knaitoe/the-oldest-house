package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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
                enterHouseDimension(player, data);
            }
            return;
        }

        if (player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                && !isValidHouseInteriorSpace(player, data, origin)) {
            leaveHouseDimension(player);
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

    private static void enterHouseDimension(ServerPlayer player, HouseSavedData data) {
        ServerLevel interior = HouseDimensionMirror.ensureInitialized(player.getServer(), data);
        if (interior == null) {
            return;
        }

        teleportMatchingCoordinates(player, interior);
    }

    private static void leaveHouseDimension(ServerPlayer player) {
        teleportMatchingCoordinates(player, player.getServer().overworld());
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
