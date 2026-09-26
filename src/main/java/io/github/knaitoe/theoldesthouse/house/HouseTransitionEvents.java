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

        HouseSavedData data = HouseSavedData.get(player.getServer());
        if (!data.isImpossibleDoorRevealed()) {
            return;
        }

        ServerLevel currentLevel = player.serverLevel();

        if (currentLevel.dimension().equals(Level.OVERWORLD)) {
            data.housePosition().ifPresent(origin -> tryEnterInterior(player, origin));
        } else if (currentLevel.dimension().equals(HouseDimensions.INTERIOR)) {
            tryReturnToOverworld(player, data);
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

        ServerLevel interior = player.getServer().getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            return;
        }

        HouseInteriorPrototype.build(interior);

        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.stopRiding();
        player.teleportTo(
                interior,
                HouseInteriorPrototype.ENTRY_X + 0.5D,
                HouseInteriorPrototype.PLAYER_Y + 0.10D,
                0.50D,
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }

    private static void tryReturnToOverworld(ServerPlayer player, HouseSavedData data) {
        if (Math.abs(player.getX() - (HouseInteriorPrototype.ENTRY_X + 0.5D)) > 0.9D
                || player.getY() < HouseInteriorPrototype.PLAYER_Y
                || player.getY() > HouseInteriorPrototype.PLAYER_Y + 4.2D
                || player.getZ() > -2.15D
                || player.getZ() < -3.75D) {
            return;
        }

        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null) {
            return;
        }

        ServerLevel overworld = player.getServer().overworld();
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D + 0.5D;

        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.stopRiding();
        player.teleportTo(
                overworld,
                centerX,
                origin.getY() + 1.10D,
                origin.getZ() + 14.35D,
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }
}
