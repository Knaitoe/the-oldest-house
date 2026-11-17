package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseDays;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The reader's home, as the House reads it: a bed of their own, outside every district (docs/NEIGHBOURS.md). The House comes to a
 * home; a bed somebody was given, or one on a district's land, counts for sleep and never for home.
 *
 * A home bed is in the Overworld, is a bed (when its chunk is loaded), is not in {@code #the_oldest_house:not_home}, and is on no
 * neighbour's district land. The reader's home is the first of: their respawn, if it is a home bed; the home bed they last woke in on
 * a morning, which the opening remembers; none. An unloaded bed is trusted: it can only have been set or remembered by using one.
 */
public final class HomeBed {
    /** A reader's home: where, the House's day it became home (-1 when the House never saw it so), and the nights slept in it. */
    public record Home(BlockPos pos, long since, int nights) {
    }

    private HomeBed() {
    }

    /** Whether the bed at {@code pos} in {@code level} could be somebody's home. */
    public static boolean isHomeBed(ServerLevel level, BlockPos pos) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return false;
        }
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.BEDS) || state.is(Neighbours.NOT_HOME)) {
                return false;
            }
        }
        return !Neighbours.districtLand(level, pos);
    }

    /** The reader's home bed, if they have one. */
    public static Optional<BlockPos> of(ServerPlayer player) {
        return home(player).map(Home::pos);
    }

    /** The reader's home, with how long it has been home and how many nights they have slept in it. */
    public static Optional<Home> home(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        OpeningPlayerState.Home remembered = OpeningSequence.state(player).home();
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null && Level.OVERWORLD.equals(player.getRespawnDimension()) && isHomeBed(overworld, respawn)) {
            return Optional.of(remembered != null && remembered.pos().equals(respawn)
                    ? new Home(respawn, remembered.since(), remembered.nights())
                    : new Home(respawn, -1L, 0));
        }
        if (remembered != null && isHomeBed(overworld, remembered.pos())) {
            return Optional.of(new Home(remembered.pos(), remembered.since(), remembered.nights()));
        }
        return Optional.empty();
    }

    /** Whether {@code pos} is the reader's home bed. */
    public static boolean isHome(ServerPlayer player, @Nullable BlockPos pos) {
        return pos != null && of(player).filter(pos::equals).isPresent();
    }

    /** The home bed the reader lies in (or is just getting out of), or null. */
    @Nullable
    public static BlockPos asleepAtHome(ServerPlayer player) {
        BlockPos at = player.getSleepingPos().orElse(null);
        return at != null && isHomeBed(player.serverLevel(), at) ? at : null;
    }

    /** Whether the reader lies (or wakes) in a bed the House counts: a home bed, or one of the manor's. */
    public static boolean asleepAtHomeOrInManor(ServerPlayer player) {
        return asleepAtHome(player) != null || player.getSleepingPos().isPresent() && HouseDays.isInManor(player);
    }
}
