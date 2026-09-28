package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Subtle changes: the house rearranging itself a little at a time, in ways
 * a player can never be quite sure of. Nothing is ever added or taken away
 * outright, and nothing changes while anyone is looking at it.
 *
 * After the rugs, on a morning when nothing larger happens (the room between
 * rooms, the hallway), there is a chance of one subtle change. The chance
 * rises with every quiet morning in a row and falls back after a change.
 * Once {@link HouseConfig#SHIFTS_BEFORE_HALLWAY} changes have happened (and
 * the room exists), the hallway gets its own, heavier roll ahead of them.
 * Changes carry on, less often, after the hallway opens.
 */
public final class HouseShifts {
    public enum Shift {
        PAINTINGS(10, true),
        DEEPER_HALL(6, false),
        DOORS(8, true),
        CHESTS(7, true),
        CANDLES(9, true),
        ECHOES(8, true),
        CHAIRS(8, true),
        NOTES(9, true),
        GUEST_BED(5, true),
        WINDOW(5, true);

        public final int weight;
        public final boolean repeatable;

        Shift(int weight, boolean repeatable) {
            this.weight = weight;
            this.repeatable = repeatable;
        }

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final int ECHO_CHECK_INTERVAL = 20;
    /** Per second, once someone is far enough away: about half a minute on average. */
    private static final int ECHO_ODDS = 30;

    /** The principal bedroom's door onto the upper hall, where the echoes come from. */
    private static final BlockPos BEDROOM_DOOR = new BlockPos(13, 7, 4);

    // Cached from the House's saved data for the mirror's hot paths.
    private static volatile boolean deepHallActive;
    @Nullable
    private static volatile BlockPos windowLight;

    private record Sound(UUID player, int tick, Vec3 pos, SoundEvent event, float volume, float pitch) {
    }

    private static final List<Sound> SCHEDULED = new ArrayList<>();

    private HouseShifts() {
    }

    // ------------------------------------------------------------------
    // Mornings

    /** Percent chance of a subtle change next morning, if nothing larger happens; -1 before they can start. */
    public static int chance(HouseSavedData data) {
        if (!data.isSpawned() || !data.areRugsShifted()) {
            return -1;
        }
        boolean after = data.isImpossibleDoorRevealed();
        int base = after ? HouseConfig.SHIFT_BASE_CHANCE_AFTER_HALLWAY.getAsInt() : HouseConfig.SHIFT_BASE_CHANCE.getAsInt();
        int step = after ? HouseConfig.SHIFT_CHANCE_STEP_AFTER_HALLWAY.getAsInt() : HouseConfig.SHIFT_CHANCE_STEP.getAsInt();
        return (int) Math.min(100L, base + (long) step * data.shiftDryMornings());
    }

    /**
     * A morning with nothing larger happening: maybe one subtle change.
     *
     * @return what changed, or null for a quiet morning
     */
    @Nullable
    public static String morning(MinecraftServer server, HouseSavedData data) {
        int chance = chance(data);
        if (chance < 0) {
            return null;
        }
        RandomSource random = server.overworld().getRandom();
        if (random.nextInt(100) >= chance) {
            data.noteShiftDryMorning();
            return null;
        }
        String change = trigger(server, data, null);
        if (change == null) {
            data.noteShiftDryMorning();
        }
        return change;
    }

    /**
     * Makes one subtle change now: {@code wanted}, or one picked by weight
     * from those that can happen. Returns what changed, or null if nothing
     * could.
     */
    @Nullable
    public static String trigger(MinecraftServer server, HouseSavedData data, @Nullable Shift wanted) {
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = origin == null ? null : HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return null;
        }
        HouseShiftEffects.Context ctx = new HouseShiftEffects.Context(
                server, data, interior, server.overworld(), origin, server.overworld().getRandom());

        List<Shift> order = wanted != null ? List.of(wanted) : weightedOrder(data, ctx.random());
        for (Shift shift : order) {
            String change = apply(ctx, shift);
            if (change != null) {
                data.recordShift(shift.id());
                HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);
                TheOldestHouse.LOGGER.info("The Oldest House changed ({}): {}.", shift.id(), change);
                return change;
            }
        }
        return null;
    }

    /** Every shift that may happen, in a weighted random order; never the same as last time first. */
    public static List<Shift> weightedOrder(HouseSavedData data, RandomSource random) {
        List<Shift> pool = new ArrayList<>();
        for (Shift shift : Shift.values()) {
            if (shift.repeatable || !data.hasShifted(shift.id())) {
                pool.add(shift);
            }
        }
        String last = data.lastShift();
        List<Shift> order = new ArrayList<>();
        while (!pool.isEmpty()) {
            int total = 0;
            for (Shift shift : pool) {
                total += weightOf(shift, last);
            }
            int roll = random.nextInt(Math.max(1, total));
            Iterator<Shift> it = pool.iterator();
            while (it.hasNext()) {
                Shift shift = it.next();
                roll -= weightOf(shift, last);
                if (roll < 0 || !it.hasNext()) {
                    order.add(shift);
                    it.remove();
                    break;
                }
            }
        }
        return order;
    }

    private static int weightOf(Shift shift, @Nullable String last) {
        return shift.id().equals(last) ? 1 : shift.weight;
    }

    @Nullable
    static String apply(HouseShiftEffects.Context ctx, Shift shift) {
        return switch (shift) {
            case PAINTINGS -> HouseShiftEffects.paintings(ctx);
            case DEEPER_HALL -> {
                String change = HouseShiftEffects.deeperHall(ctx);
                refreshCache(ctx.data());
                yield change;
            }
            case DOORS -> HouseShiftEffects.doors(ctx);
            case CHESTS -> ctx.server() == null ? null : HouseShiftEffects.chests(ctx, HouseMemory.get(ctx.server()));
            case CANDLES -> HouseShiftEffects.candles(ctx);
            case ECHOES -> HouseShiftEffects.echoes(ctx);
            case CHAIRS -> HouseShiftEffects.chairs(ctx);
            case NOTES -> HouseShiftEffects.notes(ctx);
            case GUEST_BED -> HouseShiftEffects.guestBed(ctx);
            case WINDOW -> HouseShiftEffects.window(ctx);
        };
    }

    @Nullable
    public static Shift byId(String id) {
        for (Shift shift : Shift.values()) {
            if (shift.id().equals(id)) {
                return shift;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // What the mirror must leave alone

    /** Refreshes the cached state the mirror consults; call after loading or changing it. */
    public static void refreshCache(HouseSavedData data) {
        deepHallActive = data.isHallDeepened() && !data.isImpossibleDoorRevealed();
        windowLight = data.windowLight();
    }

    public static void clearCache() {
        deepHallActive = false;
        windowLight = null;
        synchronized (SCHEDULED) {
            SCHEDULED.clear();
        }
    }

    /**
     * Positions that differ between the two dimensions on purpose: the end of
     * a deepened hall (until the hallway opens there), and the light behind
     * the window that is lit only from outside.
     */
    public static boolean isUnmirrored(BlockPos origin, BlockPos pos) {
        BlockPos light = windowLight;
        if (light != null && light.equals(pos)) {
            return true;
        }
        if (!deepHallActive) {
            return false;
        }
        int x = pos.getX() - origin.getX();
        int y = pos.getY() - origin.getY();
        int z = pos.getZ() - origin.getZ();
        return z == HouseLayout.THRESHOLD_Z
                && x >= HouseLayout.HALL_MIN_X - 1 && x <= HouseLayout.HALL_MAX_X + 1
                && y >= 0 && y <= 6;
    }

    /** Standing in the extra block at the end of a deepened hall still counts as inside the manor. */
    public static boolean isInDeepenedHall(BlockPos origin, double x, double y, double z) {
        if (!deepHallActive) {
            return false;
        }
        double relX = x - origin.getX();
        double relY = y - origin.getY();
        double relZ = z - origin.getZ();
        return relX >= HouseLayout.HALL_MIN_X + 0.15D && relX <= HouseLayout.HALL_MAX_X + 0.85D
                && relY >= 0.35D && relY <= 5.45D
                && relZ >= HouseLayout.THRESHOLD_Z - 1.0D && relZ <= HouseLayout.THRESHOLD_Z + 1.0D;
    }

    // ------------------------------------------------------------------
    // Echoes

    /**
     * While an echo is pending, the next player in the manor well away from
     * the principal bedroom hears it: its door opening and closing, or
     * footsteps crossing the upper hall and a door shutting. Only they hear
     * it.
     */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickSounds(server);
        if (server.getTickCount() % ECHO_CHECK_INTERVAL != 0) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isEchoPending()) {
            return;
        }
        Vec3 door = Vec3.atCenterOf(origin.offset(BEDROOM_DOOR));
        for (ServerPlayer player : interior.players()) {
            double relY = player.getY() - origin.getY();
            double distance = player.position().distanceTo(door);
            boolean inManor = HouseLayout.isInsideDomesticVolume(
                    player.getX() - origin.getX(), relY, player.getZ() - origin.getZ());
            boolean farEnough = distance >= 14.0D || (relY < 6.0D && distance >= 8.0D);
            if (!inManor || !farEnough || player.getRandom().nextInt(ECHO_ODDS) != 0) {
                continue;
            }
            scheduleEcho(server, player, origin);
            data.setEchoPending(false);
            TheOldestHouse.LOGGER.info("{} heard the principal bedroom of The Oldest House.", player.getGameProfile().getName());
            return;
        }
    }

    private static void scheduleEcho(MinecraftServer server, ServerPlayer player, BlockPos origin) {
        int now = server.getTickCount();
        Vec3 door = Vec3.atCenterOf(origin.offset(BEDROOM_DOOR));
        List<Sound> sounds = new ArrayList<>();
        if (player.getRandom().nextBoolean()) {
            sounds.add(new Sound(player.getUUID(), now, door, SoundEvents.WOODEN_DOOR_OPEN, 0.8F, 0.95F));
            sounds.add(new Sound(player.getUUID(), now + 50, door, SoundEvents.WOODEN_DOOR_CLOSE, 0.8F, 0.9F));
        } else {
            for (int step = 0; step < 6; step++) {
                Vec3 at = Vec3.atBottomCenterOf(origin.offset(HouseLayout.AXIS_X, 7, 9 - step));
                sounds.add(new Sound(player.getUUID(), now + step * 9, at, SoundEvents.WOOD_STEP, 0.45F, 0.85F));
            }
            sounds.add(new Sound(player.getUUID(), now + 6 * 9 + 12, door, SoundEvents.WOODEN_DOOR_CLOSE, 0.8F, 0.9F));
        }
        synchronized (SCHEDULED) {
            SCHEDULED.addAll(sounds);
        }
    }

    private static void tickSounds(MinecraftServer server) {
        synchronized (SCHEDULED) {
            if (SCHEDULED.isEmpty()) {
                return;
            }
            int now = server.getTickCount();
            Iterator<Sound> it = SCHEDULED.iterator();
            while (it.hasNext()) {
                Sound sound = it.next();
                if (sound.tick() > now) {
                    continue;
                }
                it.remove();
                ServerPlayer player = server.getPlayerList().getPlayer(sound.player());
                if (player == null || !player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
                    continue;
                }
                player.connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.event()),
                        SoundSource.BLOCKS,
                        sound.pos().x, sound.pos().y, sound.pos().z,
                        sound.volume(), sound.pitch(),
                        player.getRandom().nextLong()
                ));
            }
        }
    }

    // ------------------------------------------------------------------
    // Status

    public static String describe(HouseSavedData data) {
        List<String> names = new ArrayList<>();
        for (String entry : data.shiftHistory()) {
            names.add(entry.replace('_', ' ').replace("@", " at age "));
        }
        String history = names.isEmpty() ? "none yet" : String.join(", ", names);
        int chance = chance(data);
        String next = chance < 0
                ? "they start after the rugs"
                : chance + "% chance of one next morning if nothing larger happens"
                        + (data.shiftDryMornings() > 0 ? " (" + data.shiftDryMornings() + " quiet morning(s) in a row)" : "");
        return "Subtle changes: " + data.shiftsTriggered() + " (" + history + "); " + next + "."
                + (data.isEchoPending() ? " An echo is waiting for someone across the house." : "");
    }
}
