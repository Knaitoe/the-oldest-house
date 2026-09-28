package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseSpawnManager;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The opening sequence, per player: settle in, receive Navidson's letter,
 * meet Hillary, and let her lead the player to the Navidsons' manor.
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

    private OpeningSequence() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(OpeningSequence::onServerTick);
        bus.addListener(OpeningSequence::onPlayerLoggedIn);
        bus.addListener(OpeningSequence::onPlayerWakeUp);
        bus.addListener(OpeningSequence::onRightClickBlock);
        bus.addListener(OpeningSequence::onServerStopped);
        bus.addListener(Hillary::onEntityInteract);
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
        NavidsonPhoto.tick(server);

        int tick = server.getTickCount();
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
        BlockPos lower = clicked.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? event.getPos().below()
                : event.getPos();
        state(player).recordDoorUse(
                lower,
                respawn,
                OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt()
        );
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        clearAll();
    }

    public static void clearAll() {
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
            HouseSavedData house = HouseSavedData.get(player.server);
            Hillary.tickGuide(
                    overworld,
                    player,
                    state.hillaryUuid(),
                    state.stage().isAtLeast(OpeningStage.HILLARY_ARRIVED),
                    house.houseOrigin()
            );
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
    // Morning 2: Hillary

    /**
     * Sets Hillary on the doorstep. There is deliberately no anomalous door
     * in the player's home: Hillary is the invitation and guides the player
     * across ordinary Overworld space to the Navidsons' manor.
     */
    public static void secondMorning(ServerPlayer player, OpeningPlayerState state, BlockPos bed, boolean immediate) {
        ServerLevel level = player.server.overworld();
        HouseSavedData house = HouseSavedData.get(player.server);
        if (!house.isSpawned()) {
            HouseSpawnManager.ensureSpawnedNear(level, house, bed);
        }
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
        state.markHillaryArrived();
    }

    // ------------------------------------------------------------------
    // Testing

    /**
     * Runs the player's next opening step now, exactly as the next morning
     * would. Returns a short description of what happened.
     */
    public static String advance(ServerPlayer player) {
        Optional<BlockPos> bed = Doorsteps.bedPosition(player);
        if (bed.isEmpty()) {
            return "no bed respawn point; sleep in a bed first";
        }
        OpeningPlayerState state = state(player);
        long day = currentDay(player.server);
        switch (state.stage()) {
            case NONE, ELIGIBLE -> {
                if (NavidsonPhoto.isRunning(player.getUUID())) {
                    return "Navidson's photo is already being taken";
                }
                state.setStageForTesting(OpeningStage.ELIGIBLE, day - 1L);
                beginLetter(player, bed.get(), day);
                return "Navidson is photographing the house; the letter follows in a few seconds";
            }
            case LETTER_DELIVERED -> {
                state.setStageForTesting(OpeningStage.LETTER_DELIVERED, day - 1L);
                secondMorning(player, state, bed.get(), false);
                return "Hillary is on the doorstep and will lead you toward the Navidsons' manor";
            }
            case HILLARY_ARRIVED -> {
                return state.enteredHouse()
                        ? "you have already entered the manor"
                        : "follow Hillary to the Navidsons' manor and use its ordinary front door";
            }
            default -> {
                return "the opening sequence is complete";
            }
        }
    }

    /** A new Hillary on the doorstep, replacing any earlier one. Returns where she is, or null. */
    @Nullable
    public static BlockPos respawnHillary(ServerPlayer player) {
        Optional<BlockPos> bed = Doorsteps.bedPosition(player);
        if (bed.isEmpty()) {
            return null;
        }
        ServerLevel level = player.server.overworld();
        OpeningPlayerState state = state(player);
        if (state.hillaryUuid() != null && level.getEntity(state.hillaryUuid()) instanceof Wolf old) {
            old.discard();
        }
        Doorsteps.Delivery delivery = Doorsteps.resolve(level, state, bed.get(), OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt());
        if (delivery == null) {
            return null;
        }
        Wolf hillary = Hillary.spawn(level, delivery.spot(), player.getUUID());
        if (hillary == null) {
            return null;
        }
        state.setHillary(hillary.getUUID());
        return delivery.spot();
    }

    // ------------------------------------------------------------------
    // First manor entry

    /** Records an ordinary entry into the manor; the first one unlocks aging. */
    public static void onEnteredHouse(ServerPlayer player) {
        OpeningPlayerState state = state(player);
        HouseSavedData.get(player.server).incrementVisitCount();
        if (!state.enteredHouse()) {
            TheOldestHouse.LOGGER.info("{} has entered the House for the first time.", player.getGameProfile().getName());
        }
        state.markEntered();
        if (state.hillaryUuid() != null) {
            Hillary.waitAtManor(player.server, state.hillaryUuid());
        }
    }

}
