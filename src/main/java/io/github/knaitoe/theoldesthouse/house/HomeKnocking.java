package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A finite personal sound in the actual manor. Nothing is behind the wall. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class HomeKnocking {
    public static final String STATE = "home_knocking_04110";
    private HomeKnocking() {}

    public static CompoundTag record(ServerPlayer player) {
        return LabyrinthData.get(player.server).stateEntry(STATE, player.getUUID().toString());
    }

    private static void save(ServerPlayer player, CompoundTag own) {
        LabyrinthData.get(player.server).setStateEntry(STATE, player.getUUID().toString(), own);
    }

    public static List<BlockPos> walls(BlockPos origin) {
        return List.of(origin.offset(9, 2, 15), origin.offset(4, 2, 15), origin.offset(10, 2, 15));
    }

    public static boolean start(ServerPlayer player) {
        if (!present(player)) return false;
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        for (BlockPos wall : walls(origin)) {
            if (!player.level().hasChunkAt(wall)
                    || player.level().getBlockState(wall).getCollisionShape(player.level(), wall).isEmpty()) continue;
            CompoundTag own = new CompoundTag();
            own.putLong("Origin", origin.asLong());
            own.putLong("Wall", wall.asLong());
            own.putBoolean("Active", true);
            save(player, own);
            return true;
        }
        return false;
    }

    /** Explicit cancellation also suppresses the natural encounter for this reader. */
    public static void reset(ServerPlayer player) {
        CompoundTag own = new CompoundTag();
        own.putBoolean("Done", true);
        save(player, own);
    }

    public static boolean present(ServerPlayer player) {
        var house = HouseSavedData.get(player.server);
        return player.isAlive() && !player.isSpectator() && !house.isOutdated() && !house.isCollapsed()
                && HouseExperience.inManor(player, player.blockPosition());
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        for (var player : event.getServer().getPlayerList().getPlayers()) tickPlayer(player);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!present(player) || player.containerMenu != player.inventoryMenu || player.isSleeping()) return;
        var house = HouseSavedData.get(player.server);
        CompoundTag own = record(player);
        if (!own.getBoolean("Active")) {
            if (own.getBoolean("Done") || !house.isImpossibleDoorRevealed()
                    || HouseExperience.record(LabyrinthData.get(player.server), player.getUUID()).getInt("Returns") < 1
                    || !start(player)) return;
            own = record(player);
        }
        if (own.getLong("Origin") != house.houseOrigin().asLong()) { reset(player); return; }
        BlockPos wall = BlockPos.of(own.getLong("Wall"));
        // The sound belongs to these adjoining ground-floor rooms, never to a remote listener.
        if (player.distanceToSqr(wall.getCenter()) > 324 || Math.abs(player.getY() - wall.getY()) > 3) return;
        int elapsed = own.getInt("Elapsed") + 1;
        int phase = own.getInt("Phase");
        if (phase == 0) {
            if (elapsed == 80 || elapsed == 160 || elapsed == 260)
                knock(player, wall, elapsed == 160 ? .66F : 1F, own);
            if (elapsed >= 320) { phase = 1; elapsed = 0; }
        } else if (phase == 1) {
            if (player.isCrouching() && player.distanceToSqr(wall.getCenter()) <= 12.25) {
                own.putBoolean("Listened", true);
                phase = 2;
                elapsed = 0;
            } else {
                if (elapsed == 200 || elapsed == 400) knock(player, wall, .9F, own);
                if (elapsed >= 600) phase = 3;
            }
        } else if (phase == 2) {
            if (elapsed == 60) {
                // The answer is now against the hall side of the study. It is still in the home.
                BlockPos answer = house.houseOrigin().offset(13, 2, 19);
                knock(player, answer, .85F, own);
                own.putLong("Answer", answer.asLong());
            }
            if (elapsed >= 120) phase = 3;
        }
        own.putInt("Elapsed", elapsed);
        own.putInt("Phase", phase);
        if (phase == 3) { own.putBoolean("Done", true); own.putBoolean("Active", false); }
        save(player, own);
    }

    private static void knock(ServerPlayer player, BlockPos at, float pitch, CompoundTag own) {
        // A vanilla positional packet is addressed only to this listener. Peers acquire no event.
        player.connection.send(new ClientboundSoundPacket(LabyrinthRegistry.HOME_KNOCK,
                SoundSource.BLOCKS, at.getX() + .5, at.getY() + .5, at.getZ() + .5,
                .32F, pitch, player.getRandom().nextLong()));
        own.putInt("Heard", own.getInt("Heard") + 1);
    }
}
