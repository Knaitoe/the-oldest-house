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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
 * Going through one is a cut nobody can see. The door is shut when it is
 * clicked. Behind the place's entry door, the mod copies in the stretch of
 * wherever the player is standing (the wall around the door and sixteen
 * blocks of what lies behind them, turned to line up with the entry door),
 * then moves the player to the same spot in front of that door, turned the
 * same way, in the same tick. What they see does not change. Then the door
 * swings open, with an ordinary door's sound, onto the place.
 *
 * Every place stands in the House dimension over the manor (see
 * {@link LabyrinthPlaces}), so the move is a shift within loaded chunks with
 * nothing to wait for. Walking back out through the entry door shifts them
 * back the same way, to in front of the door they came through, which is
 * shut; the entry door shuts behind them once they are well inside.
 */
public final class LabyrinthDoors {
    private static final int LEAK_INTERVAL = 40;
    private static final double LEAK_RADIUS = 6.0D;
    /** Past the door cell's far face by this much counts as in the room; the same back counts as out. */
    private static final double THRESHOLD = 0.85D;
    /** Inside the room this far, the entry door shuts behind the player. */
    private static final double SHUT_BEHIND = 2.5D;
    /** Wandering this far back into the vestibule without entering sends the player back. */
    private static final double WANDER = 6.0D;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private record Pending(LabyrinthData.Waypoint target, Consumer<ServerPlayer> arrived, int[] ticks) {
    }

    private static final Map<UUID, Pending> FADING = new HashMap<>();
    /** Players who have gone through an entry door into its room since arriving. */
    private static final Set<UUID> INSIDE = new HashSet<>();

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
        if (event.getEntity() instanceof ServerPlayer player && event.getHand() == InteractionHand.MAIN_HAND) {
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

        if (LabyrinthData.LOCKED.equals(door.destination)) {
            // Somebody's room. It rattles in its frame and stays shut.
            player.serverLevel().playSound(null, door.lower, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 0.6F);
            locked(player);
            return;
        }
        if (LabyrinthData.RETURN.equals(door.destination) || LabyrinthData.HALLWAY_OR_RETURN.equals(door.destination)) {
            // An entry door, from inside: it just opens. Walking back out
            // through it is what takes the player back.
            setDoorOpen(player.serverLevel(), door.lower, true, player);
            return;
        }

        if (HouseSavedData.get(server).houseOrigin() == null) {
            locked(player);
            return;
        }
        if (!LabyrinthBuilder.ensureBuilt(server)) {
            player.displayClientMessage(Component.literal("The door sticks."), true);
            return;
        }

        LabyrinthPlace place;
        if (LabyrinthData.DEALT.equals(door.destination)) {
            if (door.dealt == null || door.command) {
                LabyrinthDealer.deal(data, List.of(door), placeOf(server, door), player.getRandom());
            }
            place = LabyrinthPlace.byId(door.dealt);
        } else {
            place = door.destination.startsWith("place:") ? LabyrinthPlace.byId(door.destination.substring(6)) : null;
        }
        LabyrinthData.Door entry = place == null || place.slot() < 0 ? null : data.door(place.entryDoorId());
        if (entry == null || (place == LabyrinthPlace.RED_ROOM && !RedRoom.prepare(player))) {
            locked(player);
            return;
        }
        arrive(player, door, entry, place);
    }

    private static void locked(ServerPlayer player) {
        player.displayClientMessage(Component.literal("The door is locked."), true);
    }

    @Nullable
    private static LabyrinthPlace placeOf(MinecraftServer server, LabyrinthData.Door door) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null || !door.dimension.equals(HouseDimensions.INTERIOR) ? null : LabyrinthPlaces.placeAt(origin, door.lower);
    }

    /**
     * Through {@code from} into {@code place}: its entry door gets a copy of
     * where the player stands, and the player is shifted to match.
     */
    private static void arrive(ServerPlayer player, LabyrinthData.Door from, LabyrinthData.Door entry, LabyrinthPlace place) {
        MinecraftServer server = player.server;
        ServerLevel fromLevel = player.serverLevel();
        ServerLevel toLevel = server.getLevel(entry.dimension);
        if (toLevel == null) {
            locked(player);
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        Rotation turn = rotationFrom(from.facing, entry.facing);

        setDoorOpen(toLevel, entry.lower, false, null);
        copyVestibule(fromLevel, from, toLevel, entry, turn);
        Vec3 target = shifted(player.position(), from.lower, entry.lower, turn);
        float yaw = player.getYRot() + angle(turn);

        data.pushReturn(player.getUUID(), new LabyrinthData.Waypoint(
                fromLevel.dimension(), Vec3.atBottomCenterOf(from.lower), from.facing.toYRot(), true));
        INSIDE.remove(player.getUUID());
        Consumer<ServerPlayer> arrived = p -> {
            setDoorOpen(toLevel, entry.lower, true, p);
            LabyrinthDealer.dealPlace(data, place, p.getRandom());
            RedRoom.prepareIfDealt(p, place);
        };
        if (toLevel == fromLevel) {
            shift(player, target, yaw);
            arrived.accept(player);
        } else {
            HouseTransitionEvents.beginDoorTransition(player, entry.dimension, null, arrived, target, yaw);
        }
    }

    /**
     * Each tick for a player in the House dimension. Returns true when they
     * are in the labyrinth's stack (so the manor's own bounds do not
     * apply), after sending them back if they have come back out through
     * the entry door or wandered off from it.
     */
    public static boolean tickPlayer(ServerPlayer player, BlockPos origin) {
        LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, player.blockPosition());
        if (place == null) {
            return false;
        }
        if (isBusy(player)) {
            return true;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Door entry = data.door(place.entryDoorId());
        if (entry == null) {
            return true;
        }
        UUID id = player.getUUID();
        double into = intoRoom(player, entry);
        if (into >= THRESHOLD) {
            INSIDE.add(id);
            if (into >= SHUT_BEHIND) {
                setDoorOpen(player.serverLevel(), entry.lower, false, player);
            }
            if (LabyrinthLoops.isLoop(place)) {
                BlockPos base = LabyrinthPlaces.base(origin, place);
                if (base != null) {
                    LabyrinthLoops.tick(player, place, base);
                }
            }
            return true;
        }
        if (into <= -THRESHOLD) {
            boolean through = INSIDE.remove(id);
            if (through || into <= -WANDER) {
                goBack(player, entry, data);
            }
        }
        return true;
    }

    /** How far the player is past an entry door into its room (negative: out in the vestibule). */
    private static double intoRoom(ServerPlayer player, LabyrinthData.Door entry) {
        Direction toRoom = entry.facing.getOpposite();
        return (player.getX() - (entry.lower.getX() + 0.5D)) * toRoom.getStepX()
                + (player.getZ() - (entry.lower.getZ() + 0.5D)) * toRoom.getStepZ();
    }

    /** Back out through an entry door: to the same spot in front of the door they came through. */
    private static void goBack(ServerPlayer player, LabyrinthData.Door entry, LabyrinthData data) {
        UUID id = player.getUUID();
        setDoorOpen(player.serverLevel(), entry.lower, false, null);
        LabyrinthData.Waypoint back = data.popReturn(id);
        if (back == null) {
            // Nowhere remembered (a relog): into the room instead.
            Direction toRoom = entry.facing.getOpposite();
            Vec3 in = Vec3.atBottomCenterOf(entry.lower.relative(toRoom, 2));
            shift(player, in, toRoom.toYRot());
            INSIDE.add(id);
            return;
        }
        if (!back.door()) {
            // A plain spot (a bedside, or where a command took them from):
            // no door lines up with it, so the lights go out instead.
            fadeTo(player, back, 4, 10, 24);
            return;
        }
        BlockPos from = BlockPos.containing(back.pos());
        Direction fromFacing = Direction.fromYRot(back.yaw());
        Rotation turn = rotationFrom(fromFacing, entry.facing);
        Vec3 target = shifted(player.position(), entry.lower, from, inverse(turn));
        float yaw = player.getYRot() - angle(turn);
        ServerLevel to = player.server.getLevel(back.dimension());
        if (to == null) {
            return;
        }
        if (to == player.serverLevel()) {
            shift(player, target, yaw);
        } else {
            HouseTransitionEvents.beginDoorTransition(player, back.dimension(), null, null, target, yaw);
        }
        // The door they came through has shut behind them.
        playTo(player, SoundEvents.WOODEN_DOOR_CLOSE, Vec3.atCenterOf(from), 0.9F, 0.9F);
    }

    /**
     * Back the way the player came, under a fade: the lights going out.
     * Used when a vignette throws them out.
     */
    public static void sendBack(ServerPlayer player, int fadeIn, int hold, int fadeOut) {
        if (isBusy(player)) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Waypoint back = data.popReturn(player.getUUID());
        INSIDE.remove(player.getUUID());
        LabyrinthData.Waypoint target;
        if (back == null) {
            LabyrinthData.Door junction = data.door(LabyrinthPlace.JUNCTION.entryDoorId());
            if (junction == null) {
                return;
            }
            target = insideRoom(junction);
        } else if (back.door()) {
            BlockPos from = BlockPos.containing(back.pos());
            Direction fromFacing = Direction.fromYRot(back.yaw());
            target = new LabyrinthData.Waypoint(back.dimension(),
                    Vec3.atBottomCenterOf(from.relative(fromFacing)), fromFacing.getOpposite().toYRot(), false);
        } else {
            target = back;
        }
        fadeTo(player, target, fadeIn, hold, fadeOut);
    }

    /** Fades out, moves the player while the screen is dark, and fades back in. */
    private static void fadeTo(ServerPlayer player, LabyrinthData.Waypoint target, int fadeIn, int hold, int fadeOut) {
        PacketDistributor.sendToPlayer(player, new HouseFadePayload(fadeIn, hold, fadeOut));
        FADING.put(player.getUUID(), new Pending(target, p -> { }, new int[]{Math.max(1, fadeIn)}));
    }

    /**
     * Wakes a sleeper somewhere deeper: in a gray place they never walked
     * to, facing away from its door, with the way back leading first to the
     * bed they slept in.
     */
    public static void wakeDeeper(ServerPlayer player) {
        MinecraftServer server = player.server;
        player.stopSleepInBed(false, true);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin == null || isBusy(player)) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        LabyrinthPlace place = player.getRandom().nextBoolean() ? LabyrinthPlace.JUNCTION : LabyrinthPlace.GRAY_CORRIDOR;
        BlockPos base = LabyrinthPlaces.base(origin, place);
        LabyrinthData.Door entry = data.door(place.entryDoorId());
        ServerLevel level = entry == null ? null : server.getLevel(entry.dimension);
        if (base == null || level == null) {
            return;
        }
        data.pushReturn(player.getUUID(), new LabyrinthData.Waypoint(
                player.serverLevel().dimension(), player.position(), player.getYRot(), false));
        setDoorOpen(level, entry.lower, false, null);
        LabyrinthDealer.dealPlace(data, place, player.getRandom());
        INSIDE.add(player.getUUID());

        PacketDistributor.sendToPlayer(player, new HouseFadePayload(0, 40, 60));
        Vec3 to = Vec3.atBottomCenterOf(base.offset(0, 0, place == LabyrinthPlace.JUNCTION ? -9 : -21));
        player.stopRiding();
        player.teleportTo(level, to.x, to.y, to.z, Direction.NORTH.toYRot(), 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        RedRoom.prepareIfDealt(player, place);
    }

    /** Takes a player to the junction from wherever they are, remembering where that was. */
    public static boolean goToJunction(ServerPlayer player) {
        if (!LabyrinthBuilder.ensureBuilt(player.server) || isBusy(player)) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        LabyrinthData.Door junction = data.door(LabyrinthPlace.JUNCTION.entryDoorId());
        if (junction == null) {
            return false;
        }
        data.pushReturn(player.getUUID(), new LabyrinthData.Waypoint(
                player.serverLevel().dimension(), player.position(), player.getYRot(), false));
        travelAbsolute(player, insideRoom(junction));
        INSIDE.add(player.getUUID());
        return true;
    }

    /** Two blocks inside a place from its entry door, facing into the room. */
    private static LabyrinthData.Waypoint insideRoom(LabyrinthData.Door entry) {
        Direction toRoom = entry.facing.getOpposite();
        return new LabyrinthData.Waypoint(entry.dimension, Vec3.atBottomCenterOf(entry.lower.relative(toRoom, 2)), toRoom.toYRot(), false);
    }

    private static void travelAbsolute(ServerPlayer player, LabyrinthData.Waypoint target) {
        ServerLevel to = player.server.getLevel(target.dimension());
        if (to == null) {
            return;
        }
        if (to == player.serverLevel()) {
            shift(player, target.pos(), target.yaw());
        } else {
            HouseTransitionEvents.beginDoorTransition(player, target.dimension(), null, null, target.pos(), target.yaw());
        }
    }

    // ------------------------------------------------------------------
    // The shift, and the copy behind the door

    /**
     * Moves the player within their dimension, sent as a relative move so
     * their view and momentum carry straight over.
     */
    static void shift(ServerPlayer player, Vec3 to, float yaw) {
        player.connection.teleport(to.x, to.y, to.z, yaw, player.getXRot(), RelativeMovement.ALL);
    }

    /** The turn that lines a door up with another: the one taking {@code from} to {@code to}. */
    public static Rotation rotationFrom(Direction from, Direction to) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.rotate(from) == to) {
                return rotation;
            }
        }
        return Rotation.NONE;
    }

    public static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    /** Degrees of yaw a turn adds. */
    public static float angle(Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0.0F;
            case CLOCKWISE_90 -> 90.0F;
            case CLOCKWISE_180 -> 180.0F;
            case COUNTERCLOCKWISE_90 -> -90.0F;
        };
    }

    public static Vec3 rotate(Vec3 v, Rotation rotation) {
        return switch (rotation) {
            case NONE -> v;
            case CLOCKWISE_90 -> new Vec3(-v.z, v.y, v.x);
            case CLOCKWISE_180 -> new Vec3(-v.x, v.y, -v.z);
            case COUNTERCLOCKWISE_90 -> new Vec3(v.z, v.y, -v.x);
        };
    }

    /** The same spot relative to {@code to} as {@code pos} is to {@code from}, turned by {@code turn}. */
    public static Vec3 shifted(Vec3 pos, BlockPos from, BlockPos to, Rotation turn) {
        Vec3 fromCentre = new Vec3(from.getX() + 0.5D, from.getY(), from.getZ() + 0.5D);
        Vec3 toCentre = new Vec3(to.getX() + 0.5D, to.getY(), to.getZ() + 0.5D);
        return toCentre.add(rotate(pos.subtract(fromCentre), turn));
    }

    /**
     * Copies the wall around {@code from} and what lies in front of it (the
     * side the player is on) in behind {@code entry}, turned to line up.
     */
    static void copyVestibule(ServerLevel fromLevel, LabyrinthData.Door from, ServerLevel toLevel, LabyrinthData.Door entry, Rotation turn) {
        Direction f = from.facing;
        Direction fSide = f.getClockWise();
        Direction g = entry.facing;
        Direction gSide = g.getClockWise();
        BoundingBox v = LabyrinthPlaces.localVestibule();
        for (int s = v.minX(); s <= v.maxX(); s++) {
            for (int y = v.minY(); y <= v.maxY(); y++) {
                for (int k = 0; k <= v.maxZ() - v.minZ(); k++) {
                    BlockPos src = from.lower.relative(f, k).relative(fSide, s).above(y);
                    BlockPos dst = entry.lower.relative(g, k).relative(gSide, s).above(y);
                    if (toLevel.isOutsideBuildHeight(dst) || !fromLevel.isLoaded(src)) {
                        continue;
                    }
                    BlockState state = fromLevel.getBlockState(src).rotate(turn);
                    if (toLevel.getBlockState(dst) != state) {
                        toLevel.setBlock(dst, state, FLAGS);
                    }
                }
            }
        }
    }

    static void setDoorOpen(ServerLevel level, BlockPos lower, boolean open, @Nullable ServerPlayer hearer) {
        boolean changed = false;
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            BlockState state = level.getBlockState(half);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN) != open) {
                level.setBlock(half, state.setValue(DoorBlock.OPEN, open), FLAGS);
                changed = true;
            }
        }
        if (changed) {
            level.playSound(null, lower, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
    }

    // ------------------------------------------------------------------
    // Ticking: carving, fades, and leaks under doors

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        LabyrinthBuilder.tick(server);

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
                    Vec3 behind = Vec3.atCenterOf(door.lower.relative(door.facing.getOpposite()));
                    if (door.bark) {
                        // Hillary, somewhere behind it, the way she found round outside.
                        boolean whine = player.getRandom().nextInt(3) == 0;
                        playTo(player, whine ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_AMBIENT, behind, 0.35F, 1.0F);
                    } else {
                        // A heartbeat, faint, from behind the door.
                        playTo(player, SoundEvents.WARDEN_HEARTBEAT, behind, 0.3F, 0.9F);
                    }
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
        LabyrinthBuilder.ensureBuilt(server);
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
        level.setBlock(nearest.lower.above(), Blocks.AIR.defaultBlockState(), FLAGS);
        level.setBlock(nearest.lower, Blocks.AIR.defaultBlockState(), FLAGS);
        data.removeDoor(nearest.id);
        syncSealedDoors(player.server);
        return nearest.id;
    }

    // ------------------------------------------------------------------
    // Keeping doors and the stack whole, and clients informed

    private static boolean isProtected(ServerLevel level, BlockPos pos) {
        MinecraftServer server = level.getServer();
        if (LabyrinthData.get(server).doorAt(level.dimension(), pos) != null) {
            return true;
        }
        if (!level.dimension().equals(HouseDimensions.INTERIOR)) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin != null && LabyrinthPlaces.isInStack(origin, pos);
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && isProtected(level, event.getPos())
                && !TellTaleFloorboards.isLooseBoard(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && isProtected(level, event.getPos())
                && !LabyrinthLoops.allowsPlacing(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(pos -> isProtected(level, pos));
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
        INSIDE.remove(event.getEntity().getUUID());
        LabyrinthLoops.forget(event.getEntity().getUUID());
    }

    public static void clearAll() {
        FADING.clear();
        INSIDE.clear();
        LabyrinthLoops.clearAll();
        LabyrinthBuilder.clearAll();
    }

    // ------------------------------------------------------------------
    // Status

    public static List<String> describe(MinecraftServer server, @Nullable ServerPlayer viewer) {
        LabyrinthData data = LabyrinthData.get(server);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        List<String> lines = new ArrayList<>();
        String where = origin == null ? "no manor yet"
                : LabyrinthBuilder.isBuilt(server) ? "carved around the manor (" + LabyrinthPlaces.slotsAbove(origin) + " slot(s) above it, the rest below)"
                : LabyrinthBuilder.isCarving() ? "being carved" : "not carved yet";
        lines.add("Labyrinth: " + where
                + "; hallway door " + (data.door("hallway_end") != null ? "in place" : "not yet (it comes with the hallway)")
                + "; next dealing has a " + LabyrinthDealer.vignetteChance(data) + "% chance of a vignette door ("
                + data.dryDeals() + " dry dealing(s)); finished vignettes: "
                + (data.completed().isEmpty() ? "none" : String.join(", ", data.completed())) + "."
                + (data.hillaryScent() ? " Hillary has a scent: the next dealing that can will have a vignette door." : ""));
        List<String> deals = new ArrayList<>();
        for (LabyrinthData.Door door : data.doors()) {
            if (LabyrinthData.DEALT.equals(door.destination) && door.dealt != null) {
                deals.add(door.id + " -> " + door.dealt + (door.bark ? " (Hillary barks behind it)" : door.leak ? " (leaks)" : ""));
            }
        }
        if (!deals.isEmpty()) {
            lines.add("Dealt doors: " + String.join(", ", deals) + ".");
        }
        if (viewer != null) {
            lines.add("Your way back: " + data.returnDepth(viewer.getUUID()) + " door(s) deep.");
            String loop = LabyrinthLoops.describe(viewer.getUUID(), viewer.serverLevel().getGameTime());
            if (loop != null) {
                lines.add(loop);
            }
        }
        lines.addAll(RedRoom.describe(server, viewer));
        return lines;
    }
}
