package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import net.minecraft.world.entity.animal.Wolf;

/**
 * The one low-level move used by seamless House topology.
 *
 * Internal House doors and loops do not perform a dimension transition and do
 * not get an overlay. They move the player inside the current dimension after
 * the destination chunk is available, while preserving view and ordinary
 * movement. Mounts do not cross House thresholds.
 */
public final class HouseInternalTeleport {
    private HouseInternalTeleport() {
    }

    /** Move to an absolute point in the player's current dimension. */
    public static void shift(ServerPlayer player, Vec3 to, float yaw) {
        ServerLevel level = player.serverLevel();
        Wolf companion = Hillary.following(player);

        // Do the potentially visible/loading work before the client is moved.
        // Once the teleport packet is sent, the destination must already be a
        // real place rather than a chunk that materializes a frame later.
        level.getChunkAt(BlockPos.containing(to));

        Vec3 movement = player.getDeltaMovement();
        float pitch = player.getXRot();

        player.stopRiding();
        player.connection.teleport(
                to.x,
                to.y,
                to.z,
                yaw,
                pitch,
                RelativeMovement.ALL
        );

        // Be explicit rather than depending on packet-relative velocity
        // behavior. The House changes adjacency, not the player's stride.
        player.setDeltaMovement(movement);
        player.resetFallDistance();
        Hillary.followAcross(companion, player);
    }

    /** Translate by a fixed offset without rotating the player. */
    public static void translate(ServerPlayer player, double dx, double dy, double dz) {
        shift(
                player,
                player.position().add(dx, dy, dz),
                player.getYRot()
        );
    }
}
