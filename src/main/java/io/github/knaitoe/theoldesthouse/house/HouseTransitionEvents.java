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
            tryEnterHiddenCell(player, origin);
        } else {
            tryReturnToDomesticFloor(player, origin);
        }
    }

    private static void tryEnterHiddenCell(ServerPlayer player, BlockPos origin) {
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2 + 0.5D;

        // The player has already crossed the visible doorway before this seam.
        if (Math.abs(player.getX() - centerX) > 0.72D
                || player.getY() < origin.getY() + 1.0D
                || player.getY() > origin.getY() + 4.2D
                || player.getZ() < origin.getZ() + 16.25D
                || player.getZ() > origin.getZ() + 17.40D) {
            return;
        }

        HouseInteriorPrototype.syncDoorFromDomesticFloor(player.serverLevel(), origin);
        moveVertically(player, HouseInteriorPrototype.PLAYER_Y + 0.10D);
    }

    private static void tryReturnToDomesticFloor(ServerPlayer player, BlockPos origin) {
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2 + 0.5D;
        double returnZ = origin.getZ() + HouseInteriorPrototype.RETURN_SEAM_Z_OFFSET;

        // Return only after the player has crossed the copied door and walked
        // several blocks into the fake domestic hall. The doorway itself is
        // therefore ordinary architecture in both directions.
        if (Math.abs(player.getX() - centerX) > 0.72D
                || player.getY() < HouseInteriorPrototype.PLAYER_Y
                || player.getY() > HouseInteriorPrototype.PLAYER_Y + 4.2D
                || player.getZ() < returnZ - 0.55D
                || player.getZ() > returnZ + 0.55D) {
            return;
        }

        HouseInteriorPrototype.syncDoorToDomesticFloor(player.serverLevel(), origin);
        moveVertically(player, origin.getY() + 1.10D);
    }

    private static void moveVertically(ServerPlayer player, double destinationY) {
        Vec3 movement = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.teleportTo(
                player.serverLevel(),
                player.getX(),
                destinationY,
                player.getZ(),
                yaw,
                pitch
        );
        player.setDeltaMovement(movement);
    }
}
