package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HouseSealedDoorsPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Doors that lead somewhere else: the impossible hallway's far door, the
 * labyrinth's doors, and test doors placed by command.
 *
 * None of them ever opens where it stands. Clicking one moves the player to
 * the place it leads (see {@link LabyrinthData}), arriving in front of that
 * place's entry door and facing into the room. Between dimensions the usual
 * door transition covers the move; within one, a short fade to black does.
 * Each trip through a door that is not a way back remembers where the player
 * stood, so "back" doors lead back the way they came.
 */
public final class LabyrinthDoors {
    private static final int FADE_IN = 4;
    private static final int FADE_HOLD = 6;
    private static final int FADE_OUT = 10;
    private static final int LEAK_INTERVAL = 40;
    private static final double LEAK_RADIUS = 6.0D;

    private record Pending(LabyrinthData.Waypoint target, Consumer<ServerPlayer> arrived, int[] ticks) {
    }

    private static final Map<UUID, Pending> FADING = new HashMap<>();

    private LabyrinthDoors() {
    }

    // ------------------------------------------------------------------
    // Using a door

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || level.getServer() == null) {
            return;
        }
        LabyrinthData.Door door = LabyrinthData.get(level.getServer()).doorAt(level.dimension(), event.getPos());
        if (door == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            use(player, door);
        }
    }

    public static boolean isBusy(ServerPlayer player) {
        return FADING.containsKey(player.getUUID()) || HouseTransitionEvents.isPending(player);
    }

    /** Where a door takes this player, and goes there. */
    public static void use(ServerPlayer player, LabyrinthData.Door door) {
        if (isBusy(player)) {
            return;
        }
        MinecraftServer server = player.server;
        LabyrinthData data = LabyrinthData.get(server);
        UUID id = player.getUUID();

        LabyrinthData.Waypoint target;
        String destination = door.destination;
        if (LabyrinthData.HALLWAY_OR_RETURN.equals(destination) && data.door("hallway_end") != null) {
            data.clearReturns(id);
            target = arrival(data, LabyrinthPlace.HALLWAY_END);
        } else if (LabyrinthData.RETURN.equals(destination) || LabyrinthData.HALLWAY_OR_RETURN.equals(destination)) {
            target = data.popReturn(id);
            if (target == null) {
                target = arrival(data, LabyrinthPlace.JUNCTION);
            }
        } else {
            LabyrinthPlace place;
            if (LabyrinthData.DEALT.equals(destination)) {
                if (door.dealt == null || door.command) {
                    LabyrinthDealer.deal(data, List.of(door), LabyrinthPlace.containing(door.dimension, door.lower), player.getRandom());
                }
                place = LabyrinthPlace.byId(door.dealt);
            } else {
                place = destination.startsWith("place:") ? LabyrinthPlace.byId(destination.substring(6)) : null;
            }
            if (place == null) {
                player.displayClientMessage(Component.literal("The door is locked."), true);
                return;
            }
            if (place.base() != null && !LabyrinthBuilder.ensureBuilt(server)) {
                return;
            }
            target = arrival(data, place);
            if (target != null) {
                data.pushReturn(id, door.inFront());
            }
        }
        if (target == null) {
            player.displayClientMessage(Component.literal("The door is locked."), true);
            return;
        }
        playTo(player, SoundEvents.WOODEN_DOOR_OPEN, Vec3.atCenterOf(door.lower), 1.0F, 0.95F);
        travel(player, target, false, FADE_IN, FADE_HOLD, FADE_OUT);
    }

    /** Standing in front of a place's entry door, or null if it does not exist yet. */
    @Nullable
    public static LabyrinthData.Waypoint arrival(LabyrinthData data, LabyrinthPlace place) {
        LabyrinthData.Door entry = data.door(place == LabyrinthPlace.HALLWAY_END ? "hallway_end" : place.entryDoorId());
        return entry == null ? null : entry.inFront();
    }

    /**
     * Back the way the player came, with a longer fade: the lights going out.
     * Used when a vignette throws them out.
     */
    public static void sendBack(ServerPlayer player, int fadeIn, int hold, int fadeOut) {
        if (isBusy(player)) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Waypoint target = data.popReturn(player.getUUID());
        if (target == null) {
            target = arrival(data, LabyrinthPlace.JUNCTION);
        }
        if (target != null) {
            travel(player, target, true, fadeIn, hold, fadeOut);
        }
    }

    /** Takes a player to the junction from wherever they are, remembering where that was. */
    public static boolean goToJunction(ServerPlayer player) {
        if (!LabyrinthBuilder.ensureBuilt(player.server) || isBusy(player)) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Waypoint target = arrival(data, LabyrinthPlace.JUNCTION);
        if (target == null) {
            return false;
        }
        data.pushReturn(player.getUUID(), new LabyrinthData.Waypoint(player.serverLevel().dimension(), player.position(), player.getYRot()));
        travel(player, target, false, FADE_IN, FADE_HOLD, FADE_OUT);
        return true;
    }

    private static void travel(ServerPlayer player, LabyrinthData.Waypoint target, boolean forceFade, int fadeIn, int hold, int fadeOut) {
        ServerLevel destination = player.server.getLevel(target.dimension());
        if (destination == null) {
            player.displayClientMessage(Component.literal("The door is locked."), true);
            return;
        }
        Consumer<ServerPlayer> arrived = p -> onArrived(p, target);
        if (forceFade || destination == player.serverLevel()) {
            PacketDistributor.sendToPlayer(player, new HouseFadePayload(fadeIn, hold, fadeOut));
            FADING.put(player.getUUID(), new Pending(target, arrived, new int[]{fadeIn}));
        } else {
            HouseTransitionEvents.beginDoorTransition(player, target.dimension(), null, arrived, target.pos(), target.yaw());
        }
    }

    private static void onArrived(ServerPlayer player, LabyrinthData.Waypoint target) {
        playTo(player, SoundEvents.WOODEN_DOOR_CLOSE, player.position(), 0.9F, 0.9F);
        LabyrinthPlace place = LabyrinthPlace.containing(target.dimension(), BlockPos.containing(target.pos()));
        if (place != null && place.base() != null) {
            LabyrinthDealer.dealPlace(LabyrinthData.get(player.server), place, player.getRandom());
        }
    }

    // ------------------------------------------------------------------
    // Ticking: fades, and leaks under doors

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!FADING.isEmpty()) {
            Iterator<Map.Entry<UUID, Pending>> it = FADING.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Pending> entry = it.next();
                Pending pending = entry.getValue();
                if (--pending.ticks()[0] > 0) {
                    continue;
                }
                it.remove();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                ServerLevel level = server.getLevel(pending.target().dimension());
                if (player == null || level == null) {
                    continue;
                }
                Vec3 pos = pending.target().pos();
                player.stopRiding();
                player.teleportTo(level, pos.x, pos.y, pos.z, pending.target().yaw(), 0.0F);
                player.setDeltaMovement(Vec3.ZERO);
                player.resetFallDistance();
                pending.arrived().accept(player);
            }
        }

        if (server.getTickCount() % LEAK_INTERVAL != 0) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (LabyrinthData.Door door : data.doors()) {
                if (door.leak
                        && door.dimension.equals(player.serverLevel().dimension())
                        && player.position().distanceTo(Vec3.atCenterOf(door.lower)) <= LEAK_RADIUS) {
                    // A heartbeat, faint, from behind the door.
                    Vec3 behind = Vec3.atCenterOf(door.lower.relative(door.facing.getOpposite()));
                    playTo(player, SoundEvents.WARDEN_HEARTBEAT, behind, 0.3F, 0.9F);
                }
            }
        }
    }

    private static void playTo(ServerPlayer player, SoundEvent sound, Vec3 at, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.BLOCKS,
                at.x, at.y, at.z, volume, pitch, player.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------
    // The impossible hallway's far door

    /** Sets a door into the hallway's far wall, leading into the labyrinth, once the hallway exists. */
    public static void ensureHallwayDoor(MinecraftServer server) {
        HouseSavedData house = HouseSavedData.get(server);
        BlockPos origin = house.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !house.isImpossibleDoorRevealed()) {
            return;
        }
        BlockPos lower = origin.offset(HouseLayout.AXIS_X, 1, HouseImpossibleHallway.END_Z_OFFSET);
        if (!(interior.getBlockState(lower).getBlock() instanceof DoorBlock)) {
            LabyrinthBuilder.placeDoor(interior, lower, Direction.NORTH);
        }
        LabyrinthData data = LabyrinthData.get(server);
        LabyrinthData.Door existing = data.door("hallway_end");
        if (existing == null || !existing.lower.equals(lower)) {
            data.putDoor(new LabyrinthData.Door("hallway_end", HouseDimensions.INTERIOR, lower, Direction.NORTH,
                    LabyrinthData.toPlace(LabyrinthPlace.JUNCTION), false));
            syncSealedDoors(server);
            TheOldestHouse.LOGGER.info("The impossible hallway's far wall now has a door.");
        }
    }

    // ------------------------------------------------------------------
    // Test doors

    /**
     * Places a test door two blocks in front of the player, facing them,
     * leading to {@code destination} (a place id, or "dealt").
     *
     * @return an error, or null on success
     */
    @Nullable
    public static String placeCommandDoor(ServerPlayer player, String destination) {
        ServerLevel level = player.serverLevel();
        Direction ahead = player.getDirection();
        BlockPos lower = player.blockPosition().relative(ahead, 2);
        if (!level.getBlockState(lower).canBeReplaced() || !level.getBlockState(lower.above()).canBeReplaced()) {
            return "There is no room for a door two blocks in front of you.";
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        if (data.doorAt(level.dimension(), lower) != null) {
            return "There is already a door there.";
        }
        Direction facing = ahead.getOpposite();
        LabyrinthBuilder.placeDoor(level, lower, facing);
        String dest = LabyrinthData.DEALT.equals(destination) ? LabyrinthData.DEALT : "place:" + destination;
        LabyrinthData.Door door = new LabyrinthData.Door(data.nextCommandDoorId(), level.dimension(), lower, facing, dest, true);
        LabyrinthPlace place = LabyrinthPlace.byId(destination);
        door.leak = place != null && place.isVignette();
        data.putDoor(door);
        syncSealedDoors(player.server);
        return null;
    }

    /** Removes the nearest test door within eight blocks. Returns its id, or null. */
    @Nullable
    public static String removeCommandDoor(ServerPlayer player) {
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Door nearest = null;
        double best = 64.0D;
        for (LabyrinthData.Door door : data.doors()) {
            if (door.command && door.dimension.equals(player.serverLevel().dimension())) {
                double distance = player.position().distanceToSqr(Vec3.atCenterOf(door.lower));
                if (distance < best) {
                    best = distance;
                    nearest = door;
                }
            }
        }
        if (nearest == null) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        level.setBlock(nearest.lower.above(), Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
        level.setBlock(nearest.lower, Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
        data.removeDoor(nearest.id);
        syncSealedDoors(player.server);
        return nearest.id;
    }

    // ------------------------------------------------------------------
    // Keeping doors whole, and clients informed

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && LabyrinthData.get(level.getServer()).doorAt(level.dimension(), event.getPos()) != null) {
            event.setCanceled(true);
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LabyrinthData data = LabyrinthData.get(level.getServer());
            event.getAffectedBlocks().removeIf(pos -> data.doorAt(level.dimension(), pos) != null);
        }
    }

    public static void syncSealedDoors(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(sealedPayload(server));
    }

    private static HouseSealedDoorsPayload sealedPayload(MinecraftServer server) {
        List<GlobalPos> doors = new ArrayList<>();
        for (LabyrinthData.Door door : LabyrinthData.get(server).doors()) {
            doors.add(door.globalPos());
        }
        // The manor's own front and back doors: shut, a click crosses at them.
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin != null) {
            for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
                BlockPos lower = origin.offset(door.x(), door.y(), door.z());
                doors.add(GlobalPos.of(Level.OVERWORLD, lower));
                doors.add(GlobalPos.of(HouseDimensions.INTERIOR, lower));
            }
        }
        return new HouseSealedDoorsPayload(doors);
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, sealedPayload(player.server));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        FADING.remove(event.getEntity().getUUID());
    }

    public static void clearAll() {
        FADING.clear();
    }

    // ------------------------------------------------------------------
    // Status

    public static List<String> describe(MinecraftServer server, @Nullable ServerPlayer viewer) {
        LabyrinthData data = LabyrinthData.get(server);
        List<String> lines = new ArrayList<>();
        lines.add("Labyrinth: " + (data.builtVersion() >= LabyrinthBuilder.VERSION ? "carved" : "not carved yet")
                + "; hallway door " + (data.door("hallway_end") != null ? "in place" : "not yet (it comes with the hallway)")
                + "; next dealing has a " + LabyrinthDealer.vignetteChance(data) + "% chance of a vignette door ("
                + data.dryDeals() + " dry dealing(s)); finished vignettes: "
                + (data.completed().isEmpty() ? "none" : String.join(", ", data.completed())) + ".");
        List<String> deals = new ArrayList<>();
        for (LabyrinthData.Door door : data.doors()) {
            if (LabyrinthData.DEALT.equals(door.destination) && door.dealt != null) {
                deals.add(door.id + " -> " + door.dealt + (door.leak ? " (leaks)" : ""));
            }
        }
        if (!deals.isEmpty()) {
            lines.add("Dealt doors: " + String.join(", ", deals) + ".");
        }
        if (viewer != null) {
            lines.add("Your way back: " + data.returnDepth(viewer.getUUID()) + " door(s) deep.");
        }
        return lines;
    }
}
