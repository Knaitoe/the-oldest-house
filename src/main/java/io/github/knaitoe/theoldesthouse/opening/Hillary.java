package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Hillary, the Navidsons' husky.
 *
 * She waits near the doorstep she was found on until her recipient tames
 * her with a single bone. If she follows them through the entrance door she
 * runs a few blocks ahead down the hall, and once she is out of sight (or
 * after about two seconds) the House lets her go: she is taken out of the
 * House dimension and set down, sitting, on her doorstep.
 *
 * While she is between dimensions her saved entity data is kept in
 * {@link OpeningWorldData}, so a server stop cannot lose her.
 */
public final class Hillary {
    public static final String NAME = "Hillary";
    public static final int LEASH_RADIUS = 6;

    private static final int FOLLOW_RADIUS = 12;
    private static final int RUN_MIN_TICKS = 10;
    private static final int RUN_MAX_TICKS = 40;
    private static final int APPEAR_TIMEOUT_TICKS = 100;
    private static final int TICKET_REFRESH_TICKS = 100;
    private static final double VIEW_CONE_COS = Math.cos(Math.toRadians(60.0D));

    private static final TicketType<ChunkPos> RETURN_TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_hillary",
            Comparator.comparingLong(ChunkPos::toLong),
            TICKET_REFRESH_TICKS * 2
    );

    private static final Map<UUID, Run> RUNS = new HashMap<>();
    private static final Set<UUID> STRAYS = new HashSet<>();
    private static final Map<UUID, Long> LAST_TICKET = new HashMap<>();

    private Hillary() {
    }

    // ------------------------------------------------------------------
    // Arrival on the doorstep

    @Nullable
    public static Wolf spawn(ServerLevel level, BlockPos home, UUID recipient) {
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf == null) {
            return null;
        }
        Vec3 at = Doorsteps.restingPoint(level, home);
        float yaw = level.getRandom().nextFloat() * 360.0F;
        wolf.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        wolf.setYHeadRot(yaw);
        wolf.setVariant(level.registryAccess()
                .registryOrThrow(Registries.WOLF_VARIANT)
                .getHolderOrThrow(WolfVariants.ASHEN));
        wolf.setCustomName(Component.literal(NAME));
        wolf.setCustomNameVisible(false);
        wolf.setPersistenceRequired();
        wolf.restrictTo(home, LEASH_RADIUS);
        wolf.setData(OpeningRegistry.HILLARY, new HillaryTag(recipient, home.immutable()));
        return level.addFreshEntity(wolf) ? wolf : null;
    }

    @Nullable
    public static HillaryTag tagOf(Entity entity) {
        if (!(entity instanceof Wolf) || !entity.hasData(OpeningRegistry.HILLARY)) {
            return null;
        }
        HillaryTag tag = entity.getData(OpeningRegistry.HILLARY);
        return tag.isSet() ? tag : null;
    }

    /** The first bone always tames her, and only her recipient can. */
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || !(event.getTarget() instanceof Wolf wolf) || wolf.isTame()) {
            return;
        }
        HillaryTag tag = tagOf(wolf);
        ItemStack stack = event.getItemStack();
        if (tag == null || !stack.is(Items.BONE)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);

        Player player = event.getEntity();
        if (!player.getUUID().equals(tag.recipient())) {
            wolf.playSound(SoundEvents.WOLF_GROWL, 0.6F, 1.0F);
            return;
        }

        stack.consume(1, player);
        wolf.tame(player);
        wolf.getNavigation().stop();
        wolf.setTarget(null);
        wolf.setOrderedToSit(true);
        wolf.clearRestriction();
        wolf.level().broadcastEntityEvent(wolf, (byte) 7);
    }

    /** Keeps an untamed Hillary near her doorstep. */
    public static void tickLeash(ServerLevel overworld, UUID wolfId) {
        if (!(overworld.getEntity(wolfId) instanceof Wolf wolf)) {
            return;
        }
        HillaryTag tag = tagOf(wolf);
        if (tag == null) {
            return;
        }
        if (wolf.isTame()) {
            if (wolf.hasRestriction()) {
                wolf.clearRestriction();
            }
            return;
        }

        BlockPos home = tag.home();
        if (!wolf.hasRestriction()) {
            wolf.restrictTo(home, LEASH_RADIUS);
        }
        double distanceSquared = wolf.distanceToSqr(Vec3.atBottomCenterOf(home));
        if (wolf.isLeashed()) {
            return;
        }
        if (distanceSquared > 32.0D * 32.0D) {
            Vec3 at = Doorsteps.restingPoint(overworld, home);
            wolf.moveTo(at.x, at.y, at.z, wolf.getYRot(), 0.0F);
            wolf.getNavigation().stop();
        } else if (distanceSquared > (LEASH_RADIUS + 1.0D) * (LEASH_RADIUS + 1.0D) && wolf.getNavigation().isDone()) {
            wolf.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.0D);
        }
    }

    // ------------------------------------------------------------------
    // Following the player through the entrance door

    /**
     * Called just before the owner is moved into the House. If their tamed
     * Hillary is following (standing, close by), she goes too.
     */
    public static void beforeEntry(ServerPlayer player, @Nullable UUID wolfId) {
        if (wolfId == null || RUNS.containsKey(wolfId)) {
            return;
        }
        if (!(player.serverLevel().getEntity(wolfId) instanceof Wolf wolf)) {
            return;
        }
        HillaryTag tag = tagOf(wolf);
        if (tag == null
                || !wolf.isAlive()
                || !wolf.isTame()
                || !wolf.isOwnedBy(player)
                || wolf.isOrderedToSit()
                || wolf.isLeashed()
                || wolf.isPassenger()
                || wolf.distanceToSqr(player) > (double) FOLLOW_RADIUS * FOLLOW_RADIUS) {
            return;
        }

        CompoundTag data = capture(wolf, false);
        // Held in saved data until she is safely in the House, in case the
        // server stops in between.
        OpeningWorldData.get(player.server).putReturn(new OpeningWorldData.PendingReturn(wolfId, data, tag.home()));
        RUNS.put(wolfId, new Run(wolfId, player.getUUID(), tag.home(), data));
    }

    /** Called once the owner has arrived; she appears just ahead of them. */
    public static void afterEntry(ServerPlayer player, Vec3 appearAt, Vec3 runTo) {
        for (Run run : RUNS.values()) {
            if (run.player.equals(player.getUUID()) && run.phase == Phase.APPEARING) {
                run.appearAt = appearAt;
                run.runTo = runTo;
                run.yaw = player.getYRot();
            }
        }
    }

    public static boolean isRunning(UUID wolfId) {
        return RUNS.containsKey(wolfId);
    }

    // ------------------------------------------------------------------
    // Ticking

    public static void tick(MinecraftServer server) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        OpeningWorldData data = OpeningWorldData.get(server);

        if (interior != null) {
            tickRuns(server, interior, data);
            tickStrays(interior, data);
        }
        tickReturns(server, data);
    }

    private static void tickRuns(MinecraftServer server, ServerLevel interior, OpeningWorldData data) {
        Iterator<Run> iterator = RUNS.values().iterator();
        while (iterator.hasNext()) {
            Run run = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(run.player);
            boolean playerInside = player != null && player.serverLevel() == interior;
            run.ticks++;

            if (run.phase == Phase.APPEARING) {
                if (!playerInside || run.ticks > APPEAR_TIMEOUT_TICKS) {
                    // Her saved return takes her home.
                    iterator.remove();
                    continue;
                }
                if (run.appearAt == null || !interior.isPositionEntityTicking(BlockPos.containing(run.appearAt))) {
                    continue;
                }
                Wolf wolf = recreate(interior, run.data, run.appearAt, run.yaw);
                if (wolf == null) {
                    iterator.remove();
                    continue;
                }
                wolf.setOrderedToSit(false);
                wolf.setInSittingPose(false);
                data.removeReturn(run.wolf);
                run.phase = Phase.RUNNING;
                run.ticks = 0;
                run.data = null;
                continue;
            }

            if (!(interior.getEntity(run.wolf) instanceof Wolf wolf)) {
                // Unloaded or gone; if she is still saved in the House she
                // will be found as a stray when her chunk loads.
                iterator.remove();
                continue;
            }
            if (run.runTo != null && run.ticks % 5 == 1) {
                wolf.getNavigation().moveTo(run.runTo.x, run.runTo.y, run.runTo.z, 1.5D);
            }
            boolean outOfSight = !playerInside || !canSee(player, wolf);
            if (run.ticks >= RUN_MAX_TICKS || (run.ticks >= RUN_MIN_TICKS && outOfSight)) {
                sendHome(wolf, run.home, data);
                iterator.remove();
            }
        }
    }

    private static void tickStrays(ServerLevel interior, OpeningWorldData data) {
        if (STRAYS.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = STRAYS.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            if (RUNS.containsKey(id)) {
                iterator.remove();
                continue;
            }
            if (interior.getEntity(id) instanceof Wolf wolf) {
                HillaryTag tag = tagOf(wolf);
                if (tag != null) {
                    sendHome(wolf, tag.home(), data);
                }
            }
            iterator.remove();
        }
    }

    private static void tickReturns(MinecraftServer server, OpeningWorldData data) {
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();
        for (OpeningWorldData.PendingReturn pending : data.pendingReturns()) {
            if (RUNS.containsKey(pending.wolf())) {
                continue;
            }
            BlockPos home = pending.home();
            if (!overworld.isPositionEntityTicking(home)) {
                Long last = LAST_TICKET.get(pending.wolf());
                if (last == null || now - last >= TICKET_REFRESH_TICKS) {
                    ChunkPos chunk = new ChunkPos(home);
                    overworld.getChunkSource().addRegionTicket(RETURN_TICKET, chunk, 2, chunk);
                    LAST_TICKET.put(pending.wolf(), now);
                }
                continue;
            }

            LAST_TICKET.remove(pending.wolf());
            data.removeReturn(pending.wolf());
            if (overworld.getEntity(pending.wolf()) != null) {
                continue; // Already out here; never duplicate her.
            }
            Vec3 at = Doorsteps.restingPoint(overworld, home);
            Wolf wolf = recreate(overworld, pending.entity(), at, overworld.getRandom().nextFloat() * 360.0F);
            if (wolf == null) {
                TheOldestHouse.LOGGER.warn("Could not return Hillary ({}) to her doorstep at {}.", pending.wolf(), home);
                continue;
            }
            wolf.getNavigation().stop();
            wolf.setOrderedToSit(wolf.isTame());
            wolf.setInSittingPose(wolf.isTame());
        }
    }

    /** Takes her out of the House; the return tick sets her down, sitting, on her doorstep. */
    private static void sendHome(Wolf wolf, BlockPos home, OpeningWorldData data) {
        CompoundTag tag = capture(wolf, true);
        data.putReturn(new OpeningWorldData.PendingReturn(wolf.getUUID(), tag, home));
    }

    private static CompoundTag capture(Wolf wolf, boolean sitting) {
        if (wolf.isTame()) {
            wolf.setOrderedToSit(sitting);
            wolf.setInSittingPose(sitting);
        }
        wolf.getNavigation().stop();
        CompoundTag tag = new CompoundTag();
        wolf.saveWithoutId(tag);
        wolf.discard();
        return tag;
    }

    @Nullable
    private static Wolf recreate(ServerLevel level, CompoundTag data, Vec3 pos, float yaw) {
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf == null) {
            return null;
        }
        try {
            wolf.load(data);
        } catch (RuntimeException exception) {
            TheOldestHouse.LOGGER.warn("Could not restore Hillary's saved data.", exception);
            return null;
        }
        wolf.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
        wolf.setYHeadRot(yaw);
        wolf.setYBodyRot(yaw);
        wolf.setDeltaMovement(Vec3.ZERO);
        wolf.resetFallDistance();
        return level.addFreshEntity(wolf) ? wolf : null;
    }

    private static boolean canSee(ServerPlayer player, Wolf wolf) {
        if (!player.hasLineOfSight(wolf)) {
            return false;
        }
        Vec3 toWolf = wolf.getEyePosition().subtract(player.getEyePosition());
        double distance = toWolf.length();
        return distance < 0.5D || player.getViewVector(1.0F).dot(toWolf.scale(1.0D / distance)) >= VIEW_CONE_COS;
    }

    // ------------------------------------------------------------------
    // Housekeeping

    /** A Hillary loaded in the House outside a run (e.g. after a restart) is sent home. */
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !event.getLevel().dimension().equals(HouseDimensions.INTERIOR)
                || RUNS.containsKey(event.getEntity().getUUID())) {
            return;
        }
        if (tagOf(event.getEntity()) != null) {
            STRAYS.add(event.getEntity().getUUID());
        }
    }

    /** Anyone mid-run whose owner leaves is sent home at once. */
    public static void onPlayerLeft(ServerPlayer player) {
        ServerLevel interior = player.server.getLevel(HouseDimensions.INTERIOR);
        OpeningWorldData data = OpeningWorldData.get(player.server);
        List<UUID> finished = new ArrayList<>();
        for (Run run : RUNS.values()) {
            if (!run.player.equals(player.getUUID())) {
                continue;
            }
            finished.add(run.wolf);
            if (run.phase == Phase.RUNNING && interior != null && interior.getEntity(run.wolf) instanceof Wolf wolf) {
                sendHome(wolf, run.home, data);
            }
        }
        finished.forEach(RUNS::remove);
    }

    public static void clear() {
        RUNS.clear();
        STRAYS.clear();
        LAST_TICKET.clear();
    }

    private enum Phase {
        APPEARING,
        RUNNING
    }

    private static final class Run {
        final UUID wolf;
        final UUID player;
        final BlockPos home;
        @Nullable
        CompoundTag data;
        @Nullable
        Vec3 appearAt;
        @Nullable
        Vec3 runTo;
        float yaw;
        int ticks;
        Phase phase = Phase.APPEARING;

        Run(UUID wolf, UUID player, BlockPos home, CompoundTag data) {
            this.wolf = wolf;
            this.player = player;
            this.home = home;
            this.data = data;
        }
    }
}
