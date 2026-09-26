package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class HouseTransitionEvents {
    private HouseTransitionEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        if (!data.isImpossibleDoorRevealed()) {
            return;
        }

        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null) {
            return;
        }

        if (player.getY() < 200.0D) {
            tryEnterInterior(player, origin);
        } else {
            tryReturnToDomesticFloor(player, origin);
        }
    }

    private static void tryEnterInterior(ServerPlayer player, BlockPos origin) {
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D + 0.5D;

        if (Math.abs(player.getX() - centerX) > 0.8D
                || player.getY() < origin.getY() + 1.0D
                || player.getY() > origin.getY() + 4.2D
                || player.getZ() < origin.getZ() + 16.15D
                || player.getZ() > origin.getZ() + 17.85D) {
            return;
        }

        HouseInteriorPrototype.build(player.serverLevel(), origin);

        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // Same dimension, same X/Z, same loaded chunk column. Only Y changes.
        // This is specifically a seam-quality experiment.
        player.teleportTo(
                player.serverLevel(),
                player.getX(),
                HouseInteriorPrototype.PLAYER_Y + 0.10D,
                player.getZ(),
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }

    private static void tryReturnToDomesticFloor(ServerPlayer player, BlockPos origin) {
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D + 0.5D;

        if (Math.abs(player.getX() - centerX) > 0.9D
                || player.getY() < HouseInteriorPrototype.PLAYER_Y
                || player.getY() > HouseInteriorPrototype.PLAYER_Y + 4.2D
                || player.getZ() > origin.getZ() + 14.85D
                || player.getZ() < origin.getZ() + 13.25D) {
            return;
        }

        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.teleportTo(
                player.serverLevel(),
                player.getX(),
                origin.getY() + 1.10D,
                origin.getZ() + 14.35D,
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }
}
