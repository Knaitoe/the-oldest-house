package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseSpawnManager;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The opening sequence, per player: settle in, receive Navidson's letter,
 * find Hillary and a new door, and step through it into the House.
 *
 * Mornings are the Overworld day time crossing into dawn (the first 1000
 * ticks of a day). Each player's morning is handled once, while they are in
 * the Overworld with their bed loaded, so a player who is away or offline
 * receives the next step on the first morning they are home.
 */
public final class OpeningSequence {
    public static final long MORNING_END = 1000L;

    private static final long WAKE_WINDOW = 1500L;
    private static final int PLAYER_CHECK_INTERVAL = 20;
    private static final int DOOR_ATTEMPT_INTERVAL = 10;
    private static final long RECENT_PLACEMENT_TICKS = 1200L;
    private static final long PENDING_DOOR_TIMEOUT = 24000L;
    private static final int MAX_PLANS_CONSIDERED = 32;

    private static final Map<UUID, PendingDoor> PENDING_DOORS = new HashMap<>();
    private static final Map<UUID, Map<Long, Long>> RECENT_PLACEMENTS = new HashMap<>();

    private OpeningSequence() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(OpeningSequence::onServerTick);
        bus.addListener(OpeningSequence::onPlayerLoggedIn);
        bus.addListener(OpeningSequence::onPlayerLoggedOut);
        bus.addListener(OpeningSequence::onPlayerWakeUp);
        bus.addListener(OpeningSequence::onRightClickBlock);
        bus.addListener(OpeningSequence::onBlockPlaced);
        bus.addListener(OpeningSequence::onServerStopped);
        bus.addListener(Hillary::onEntityInteract);
        bus.addListener(Hillary::onEntityJoinLevel);
    }

    public static OpeningPlayerState state(ServerPlayer player) {
        return player.getData(OpeningRegistry.PLAYER_STATE);
    }

    public static long currentDay(MinecraftServer server) {
        return server.overworld().getDayTime() / 24000L;
    }

    // ------------------------------------------------------------------
    // Events

    private static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Hillary.tick(server);
        NavidsonPhoto.tick(server);

        int tick = server.getTickCount();
        if (tick % DOOR_ATTEMPT_INTERVAL == 0 && !PENDING_DOORS.isEmpty()) {
            tickPendingDoors(server);
        }
        if (tick % PLAYER_CHECK_INTERVAL == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player);
            }
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            state(player).noteFirstJoin(currentDay(player.server));
        }
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PENDING_DOORS.remove(player.getUUID());
            RECENT_PLACEMENTS.remove(player.getUUID());
            Hillary.onPlayerLeft(player);
        }
    }

    /** A completed sleep: waking in the post-night morning window, once per day. */
    private static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.serverLevel().dimension().equals(Level.OVERWORLD)) {
            return;
        }
        long dayTime = player.serverLevel().getDayTime();
        if (Math.floorMod(dayTime, 24000L) > WAKE_WINDOW) {
            return;
        }
        state(player).recordSleep(dayTime / 24000L);
    }

    /** Counts uses of ordinary doors near the player's respawn point. */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !event.getLevel().dimension().equals(Level.OVERWORLD)) {
            return;
        }
        BlockPos respawn = player.getRespawnPosition();
        if (respawn == null || !Level.OVERWORLD.equals(player.getRespawnDimension())) {
            return;
        }
        BlockState clicked = event.getLevel().getBlockState(event.getPos());
        if (!Doorsteps.isOrdinaryDoor(clicked)) {
            return;
        }
        state(player).recordDoorUse(
                EntranceDoorBlock.lowerHalf(event.getPos(), clicked),
                respawn,
                OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt()
        );
    }

    /** Blocks a player placed in the last minute are never turned into the door. */
    private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        long now = level.getGameTime();
        Map<Long, Long> placed = RECENT_PLACEMENTS.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
        placed.values().removeIf(time -> now - time > RECENT_PLACEMENT_TICKS);
        placed.put(event.getPos().asLong(), now);
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        clearAll();
    }

    public static void clearAll() {
        PENDING_DOORS.clear();
        RECENT_PLACEMENTS.clear();
        Hillary.clear();
        NavidsonPhoto.clear();
    }

    // ------------------------------------------------------------------
    // Per-player progression

    private static void tickPlayer(ServerPlayer player) {
        OpeningPlayerState state = state(player);
        ServerLevel overworld = player.server.overworld();
        long dayTime = overworld.getDayTime();
        long day = dayTime / 24000L;
        state.noteFirstJoin(day);

        if (player.serverLevel() != overworld) {
            return;
        }
        if (state.hillaryUuid() != null) {
            Hillary.tickLeash(overworld, state.hillaryUuid());
        }

        Optional<BlockPos> bed = Doorsteps.bedPosition(player);
        if (Math.floorMod(dayTime, 24000L) < MORNING_END
                && state.lastMorningDay() != day
                && bed.isPresent()
                && overworld.isPositionEntityTicking(bed.get())) {
            state.markMorningHandled(day);
            onMorning(player, state, bed.get(), day);
        }

        if (state.stage() == OpeningStage.NONE
                && bed.isPresent()
                && state.nightsSlept() >= OpeningConfig.MIN_NIGHTS_SLEPT.getAsInt()
                && state.firstJoinDay() >= 0L
                && day - state.firstJoinDay() >= OpeningConfig.MIN_DAYS_SINCE_JOIN.getAsInt()) {
            state.markEligible(day);
            TheOldestHouse.LOGGER.info("{} is settled in; Navidson's letter will arrive on the next morning.",
                    player.getGameProfile().getName());
        }
    }

    private static void onMorning(ServerPlayer player, OpeningPlayerState state, BlockPos bed, long day) {
        switch (state.stage()) {
            case ELIGIBLE -> {
                if (day > state.eligibleDay()) {
                    beginLetter(player, bed, day);
                }
            }
            case LETTER_DELIVERED -> {
                if (day > state.letterDay()) {
                    secondMorning(player, state, bed, false);
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------
    // Morning 1: the letter

    /**
     * The Navidsons move in next door (the House appears if it has not yet),
     * Navidson photographs the player's house from their porch, and the
     * letter is left with the photo once it is developed, a few seconds on.
     */
    private static void beginLetter(ServerPlayer player, BlockPos bed, long day) {
        MinecraftServer server = player.server;
        UUID id = player.getUUID();
        BlockPos porch = navidsonPorch(server, bed);
        boolean started = NavidsonPhoto.start(server, id, bed, porch, result -> {
            ServerPlayer recipient = server.getPlayerList().getPlayer(id);
            if (recipient == null || recipient.serverLevel() != server.overworld()) {
                return; // Away when it was ready: the next morning they are home, it comes again.
            }
            OpeningPlayerState state = state(recipient);
            if (state.stage() == OpeningStage.ELIGIBLE) {
                deliverLetter(recipient, state, Doorsteps.bedPosition(recipient).orElse(bed), day, result.pixels());
            }
        });
        if (!started && !NavidsonPhoto.isRunning(id)) {
            deliverLetter(player, state(player), bed, day, null);
        }
    }

    /**
     * The Navidsons' porch: the front of The Oldest House, spawned next door
     * to {@code bed} now if it does not exist yet. Null if it has no site.
     */
    @Nullable
    public static BlockPos navidsonPorch(MinecraftServer server, BlockPos bed) {
        HouseSavedData house = HouseSavedData.get(server);
        if (!house.isSpawned()) {
            HouseSpawnManager.ensureSpawnedNear(server.overworld(), house, bed);
        }
        BlockPos origin = house.houseOrigin();
        return origin == null ? null : origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.FRONT_DOOR_Z - 2);
    }

    /**
     * Leaves the letter and snapshot on the doorstep. {@code photo} is the
     * developed photograph (null for the stock print). Returns whether they
     * were delivered.
     */
    public static boolean deliverLetter(ServerPlayer player, OpeningPlayerState state, BlockPos bed, long day, @Nullable byte[] photo) {
        ServerLevel level = player.server.overworld();
        Doorsteps.Delivery delivery = Doorsteps.resolve(level, state, bed, OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt());
        if (delivery == null) {
            TheOldestHouse.LOGGER.info("No doorstep found near {}'s bed at {}; the letter will try again tomorrow.",
                    player.getGameProfile().getName(), bed);
            return false;
        }

        Vec3 spot = Doorsteps.restingPoint(level, delivery.spot());
        level.addFreshEntity(DeliveredItemEntity.create(level, NavidsonLetter.createBook(), spot.add(-0.14D, 0.0D, 0.08D), player.getUUID()));
        level.addFreshEntity(DeliveredItemEntity.create(level, NavidsonLetter.createSnapshot(level, photo), spot.add(0.14D, 0.0D, -0.08D), player.getUUID()));
        if (OpeningConfig.KNOCK_SOUND.getAsBoolean()) {
            knock(player, delivery.door() != null ? delivery.door() : delivery.spot());
        }
        state.markLetterDelivered(day);

        TheOldestHouse.LOGGER.info("Navidson's letter was left for {} at {}.", player.getGameProfile().getName(), delivery.spot());
        return true;
    }

    /** One soft knock, sent to the recipient only. */
    private static void knock(ServerPlayer player, BlockPos at) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WOOD_HIT),
                SoundSource.BLOCKS,
                at.getX() + 0.5D,
                at.getY() + 1.0D,
                at.getZ() + 0.5D,
                1.0F,
                0.55F,
                player.getRandom().nextLong()
        ));
    }

    // ------------------------------------------------------------------
    // Morning 2: Hillary and the door

    /**
     * Sets Hillary on the doorstep (once) and starts looking for the door's
     * wall. With {@code immediate} the door is placed even if the player is
     * looking (debug command).
     */
    public static void secondMorning(ServerPlayer player, OpeningPlayerState state, BlockPos bed, boolean immediate) {
        ServerLevel level = player.server.overworld();
        if (state.hillaryUuid() == null) {
            Doorsteps.Delivery delivery = Doorsteps.resolve(level, state, bed, OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt());
            if (delivery != null) {
                Wolf hillary = Hillary.spawn(level, delivery.spot(), player.getUUID());
                if (hillary != null) {
                    state.setHillary(hillary.getUUID());
                    TheOldestHouse.LOGGER.info("Hillary is waiting on {}'s doorstep at {}.", player.getGameProfile().getName(), delivery.spot());
                }
            }
        }
        startDoorAttempt(player, state, bed, immediate);
    }

    private static void startDoorAttempt(ServerPlayer player, OpeningPlayerState state, BlockPos bed, boolean immediate) {
        ServerLevel level = player.server.overworld();
        UUID owner = player.getUUID();

        OpeningWorldData.EntranceRecord existing = OpeningWorldData.get(player.server).doorOf(owner);
        if (existing != null) {
            state.markDoorPlaced(existing.lower());
            return;
        }

        Predicate<BlockPos> recent = recentlyPlacedBy(owner, level.getGameTime());
        List<EntranceDoorPlacer.Plan> plans = EntranceDoorPlacer.findWallPlans(
                level, bed, OpeningConfig.DOOR_SEARCH_RADIUS.getAsInt(), owner, recent);
        if (plans.isEmpty()) {
            int failed = state.recordFailedDoorNight();
            int allowed = OpeningConfig.FREESTANDING_DOOR_AFTER_FAILED_NIGHTS.getAsInt();
            if (failed < allowed) {
                TheOldestHouse.LOGGER.info("No wall for {}'s door near {} ({} of {} nights); trying again tomorrow.",
                        player.getGameProfile().getName(), bed, failed, allowed);
                return;
            }
            plans = EntranceDoorPlacer.findFreestandingPlans(level, bed, owner);
            if (plans.isEmpty()) {
                TheOldestHouse.LOGGER.info("No open ground for {}'s freestanding door near {}; trying again tomorrow.",
                        player.getGameProfile().getName(), bed);
                return;
            }
        }

        if (plans.size() > MAX_PLANS_CONSIDERED) {
            plans = List.copyOf(plans.subList(0, MAX_PLANS_CONSIDERED));
        }
        PendingDoor pending = new PendingDoor(plans, level.getGameTime(), immediate);
        PENDING_DOORS.put(owner, pending);
        tryPlacePending(player, state, pending);
    }

    private static void tickPendingDoors(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, PendingDoor>> iterator = PENDING_DOORS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingDoor> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null
                    || player.serverLevel() != server.overworld()
                    || now - entry.getValue().startedAt > PENDING_DOOR_TIMEOUT) {
                iterator.remove();
                continue;
            }
            OpeningPlayerState state = state(player);
            if (state.stage() != OpeningStage.LETTER_DELIVERED) {
                iterator.remove();
                continue;
            }
            PendingDoor pending = entry.getValue();
            if (placeFirstUnseen(player, state, pending) != Outcome.WAITING) {
                iterator.remove();
            }
        }
    }

    private static void tryPlacePending(ServerPlayer player, OpeningPlayerState state, PendingDoor pending) {
        if (placeFirstUnseen(player, state, pending) != Outcome.WAITING) {
            PENDING_DOORS.remove(player.getUUID());
        }
    }

    private enum Outcome {
        PLACED,
        WAITING,
        NO_LONGER_POSSIBLE
    }

    /**
     * Places the door at the best plan that is still valid and that the
     * player cannot see right now. If every valid plan is in view, waits.
     */
    private static Outcome placeFirstUnseen(ServerPlayer player, OpeningPlayerState state, PendingDoor pending) {
        ServerLevel level = player.server.overworld();
        UUID owner = player.getUUID();
        Predicate<BlockPos> recent = recentlyPlacedBy(owner, level.getGameTime());
        boolean anyValid = false;

        for (EntranceDoorPlacer.Plan plan : pending.plans) {
            boolean valid = plan.freestanding()
                    ? EntranceDoorPlacer.isValidFreestandingPlan(level, plan.lower(), plan.open(), owner, null)
                    : EntranceDoorPlacer.isValidWallPlan(level, plan.lower(), plan.open(), owner, recent);
            if (!valid) {
                continue;
            }
            anyValid = true;
            if (!pending.immediate && EntranceDoorPlacer.isVisibleTo(player, plan)) {
                continue;
            }

            EntranceDoorPlacer.place(level, plan, owner);
            state.markDoorPlaced(plan.lower());
            TheOldestHouse.LOGGER.info("A new door has appeared near {}'s bed at {} (opening {}, {}).",
                    player.getGameProfile().getName(), plan.lower(), plan.open().getName(),
                    plan.freestanding() ? "freestanding" : "in a wall");
            return Outcome.PLACED;
        }
        return anyValid ? Outcome.WAITING : Outcome.NO_LONGER_POSSIBLE;
    }

    private static Predicate<BlockPos> recentlyPlacedBy(UUID player, long now) {
        Map<Long, Long> placed = RECENT_PLACEMENTS.get(player);
        if (placed == null || placed.isEmpty()) {
            return pos -> false;
        }
        return pos -> {
            Long time = placed.get(pos.asLong());
            return time != null && now - time <= RECENT_PLACEMENT_TICKS;
        };
    }

    // ------------------------------------------------------------------
    // The door

    /**
     * The owner steps through into the House, handed to the existing
     * dimension-shift transition. Returns false (the door rattles) for
     * anyone else, or if the House cannot be reached.
     */
    public static boolean useEntranceDoor(ServerPlayer player, BlockPos lower) {
        OpeningWorldData.EntranceRecord record = OpeningWorldData.get(player.server).doorAt(lower);
        if (record == null || !record.owner().equals(player.getUUID())) {
            return false;
        }
        ServerLevel overworld = player.server.overworld();
        if (player.serverLevel() != overworld) {
            return false;
        }

        HouseSavedData house = HouseSavedData.get(player.server);
        if (!house.isSpawned()) {
            HouseSpawnManager.ensureSpawnedNear(overworld, house, Doorsteps.bedPosition(player).orElse(lower));
        }
        BlockPos origin = house.houseOrigin();
        if (origin == null) {
            TheOldestHouse.LOGGER.warn("{} used their entrance door, but The Oldest House has no site yet.",
                    player.getGameProfile().getName());
            return false;
        }

        UUID hillary = state(player).hillaryUuid();
        Vec3 arrival = hallPosition(origin, HouseLayout.FRONT_DOOR_Z + 1.5D);
        Vec3 hillaryAppears = hallPosition(origin, HouseLayout.FRONT_DOOR_Z + 2.8D);
        Vec3 hillaryRunsTo = hallPosition(origin, HouseLayout.THRESHOLD_Z - 2.5D);
        return HouseTransitionEvents.beginEntranceTransition(
                player,
                arrival,
                0.0F, // South, down the hall.
                0.0F,
                traveller -> Hillary.beforeEntry(traveller, hillary),
                traveller -> Hillary.afterEntry(traveller, hillaryAppears, hillaryRunsTo)
        );
    }

    private static Vec3 hallPosition(BlockPos origin, double relZ) {
        return new Vec3(origin.getX() + HouseLayout.AXIS_X + 0.5D, origin.getY() + 1.0D, origin.getZ() + relZ);
    }

    /** Any first entry into the House completes the opening sequence. */
    public static void onEnteredHouse(ServerPlayer player) {
        OpeningPlayerState state = state(player);
        if (state.stage() == OpeningStage.DOOR_PLACED) {
            state.markEntered();
            TheOldestHouse.LOGGER.info("{} has entered the House for the first time.", player.getGameProfile().getName());
        }
    }

    /**
     * Removes a player's entrance door and restores exactly what it replaced
     * (also the hook for the House's eventual collapse).
     */
    public static boolean removeEntranceDoor(MinecraftServer server, UUID owner) {
        OpeningWorldData data = OpeningWorldData.get(server);
        OpeningWorldData.EntranceRecord record = data.removeDoor(owner);
        if (record == null) {
            return false;
        }
        ServerLevel level = server.overworld();
        for (OpeningWorldData.ReplacedBlock replaced : record.replaced()) {
            level.setBlock(replaced.pos(), replaced.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (OpeningWorldData.ReplacedBlock replaced : record.replaced()) {
            CompoundTag tag = replaced.blockEntity();
            BlockEntity blockEntity = tag == null ? null : level.getBlockEntity(replaced.pos());
            if (blockEntity != null) {
                blockEntity.loadWithComponents(tag, level.registryAccess());
                blockEntity.setChanged();
            }
            level.blockUpdated(replaced.pos(), replaced.state().getBlock());
        }
        return true;
    }

    @Nullable
    public static PendingDoorView pendingDoor(UUID player) {
        PendingDoor pending = PENDING_DOORS.get(player);
        return pending == null ? null : new PendingDoorView(pending.plans.size(), pending.startedAt);
    }

    public record PendingDoorView(int plans, long startedAt) {
    }

    private static final class PendingDoor {
        final List<EntranceDoorPlacer.Plan> plans;
        final long startedAt;
        final boolean immediate;

        PendingDoor(List<EntranceDoorPlacer.Plan> plans, long startedAt, boolean immediate) {
            this.plans = plans;
            this.startedAt = startedAt;
            this.immediate = immediate;
        }
    }
}
