package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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
        shift(player,to,yaw,player.getXRot(),true);
    }

    /** Personal scenery leaves every following companion at the actual source tread. */
    public static void shiftPlayerOnly(ServerPlayer player,Vec3 to,float yaw,float pitch){shift(player,to,yaw,pitch,false);}

    private static void shift(ServerPlayer player,Vec3 to,float yaw,float pitch,boolean carryCompanions){
        ServerLevel level = player.serverLevel();
        var companions = carryCompanions?io.github.knaitoe.theoldesthouse.opening.CompanionOrders.followingAll(player):java.util.List.<net.minecraft.world.entity.animal.TamableAnimal>of();

        // Do the potentially visible/loading work before the client is moved.
        // Once the teleport packet is sent, the destination must already be a
        // real place rather than a chunk that materializes a frame later.
        level.getChunkAt(BlockPos.containing(to));

        Vec3 movement = player.getDeltaMovement();
        Vec3 from = player.position();

        player.stopRiding();
        player.connection.teleport(
                to.x,
                to.y,
                to.z,
                yaw,
                pitch,
                Set.of(RelativeMovement.Y_ROT, RelativeMovement.X_ROT)
        );

        // A doorway can move us after the listener has taken this tick's
        // movement baseline. Its next packet must be checked against the new
        // location, including when the client acknowledges and walks before
        // the next listener tick. Otherwise vanilla can undo a real return
        // after its personal route has already been consumed.
        player.connection.resetPosition();

        // Be explicit rather than depending on packet-relative velocity
        // behavior. The House changes adjacency, not the player's stride.
        player.setDeltaMovement(movement);
        // Relative XYZ packets add an offset to the client's predicted
        // position, which can already differ while two arrivals load chunks.
        // Use the authoritative landing and restore stride through vanilla's
        // motion packet instead of making position depend on that prediction.
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.resetFallDistance();
        if (Boolean.getBoolean("the_oldest_house.liveProof"))
            TheOldestHouse.LOGGER.info("LIVE EXPEDITION native shift {}: {} -> {} movement={}",
                    player.getGameProfile().getName(), from, player.position(), movement);
        for (var companion : companions) io.github.knaitoe.theoldesthouse.opening.CompanionOrders.followAcross(companion, player);
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
