package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseProxyEntityEvacuation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Hillary, the Navidsons' husky.
 *
 * She appears at the player's doorstep and waits. When her recipient comes
 * into sight she notices them: she stands, barks, trots over and looks up
 * at them with her head tilted. She will not lead anywhere until they
 * acknowledge her in turn: a right-click (a pat), the bone that tames her,
 * or crouching down facing her. Ignored, she follows them about and whines.
 * Once greeted she barks, bounds, and darts off towards the Navidsons' manor
 * over ordinary Overworld terrain, then leads at a pace the player can keep,
 * waiting when they fall behind. The Overworld manor proxy rejects all
 * non-player mobs, so Hillary waits outside rather than becoming trapped in
 * a duplicate interior the player cannot physically share.
 */
public final class Hillary {
    public static final String NAME = "Hillary";
    public static final int LEASH_RADIUS = 6;

    private static final int GUIDE_RESUME_RADIUS = 14;
    private static final int GUIDE_WAIT_RADIUS = 22;
    private static final double MANOR_STOP_RADIUS = 3.25D;

    /** She notices her recipient within this distance, if she can see them. */
    private static final double NOTICE_RADIUS = 12.0D;
    /** Past this, a greeting she started is given up and she goes back to her doorstep. */
    private static final double GIVE_UP_RADIUS = 28.0D;
    /** How close she comes to look up at them. */
    private static final double GREET_DISTANCE = 2.5D;
    /** Crouching facing her for this long counts as acknowledging her. */
    private static final int CROUCH_TICKS = 10;
    /** Right after the greeting she runs flat out for this long, then leads at walking pace. */
    private static final int DART_TICKS = 100;
    private static final double DART_SPEED = 1.5D;
    private static final double GUIDE_SPEED = 1.15D;

    /** Per-Hillary greeting state; nothing here needs to survive a restart. */
    private static final class Greeting {
        boolean noticed;
        int crouchTicks;
        long nextCall;
        long dartUntil;
        boolean waitingAnnounced;
    }

    private static final Map<UUID, Greeting> GREETINGS = new HashMap<>();

    private Hillary() {
    }

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

    /**
     * The first bone always tames her, and only her recipient can. A bone,
     * or any right-click from her recipient before she has been greeted,
     * acknowledges her.
     */
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()
                || !(event.getTarget() instanceof Wolf wolf)
                || HouseExteriorEntityMirror.isProjection(wolf)) {
            return;
        }

        HillaryTag tag = tagOf(wolf);
        if (tag == null) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        boolean recipient = player.getUUID().equals(tag.recipient());

        if (!wolf.isTame() && stack.is(Items.BONE)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            if (!recipient) {
                wolf.playSound(SoundEvents.WOLF_GROWL, 0.6F, 1.0F);
                return;
            }
            stack.consume(1, player);
            wolf.tame(player);
            wolf.getNavigation().stop();
            wolf.setTarget(null);
            wolf.setOrderedToSit(false);
            wolf.setInSittingPose(false);
            wolf.clearRestriction();
            wolf.level().broadcastEntityEvent(wolf, (byte) 7);
            if (!tag.acknowledged()) {
                acknowledge(wolf);
            }
            return;
        }

        if (recipient && !tag.acknowledged() && event.getHand() == InteractionHand.MAIN_HAND) {
            // A pat. Cancelled so a tamed Hillary does not sit down instead.
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            acknowledge(wolf);
        }
    }

    /** Whether Hillary and her recipient have greeted each other. */
    public static boolean isAcknowledged(Wolf wolf) {
        HillaryTag tag = tagOf(wolf);
        return tag != null && tag.acknowledged();
    }

    /**
     * She has been greeted back: a happy bark, a bound, hearts, and then she
     * darts off towards the manor.
     */
    public static void acknowledge(Wolf wolf) {
        HillaryTag tag = tagOf(wolf);
        if (tag == null || tag.acknowledged()) {
            return;
        }
        wolf.setData(OpeningRegistry.HILLARY, tag.withAcknowledged());
        wolf.setIsInterested(false);
        wolf.getNavigation().stop();
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        wolf.clearRestriction();
        wolf.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.3F);
        wolf.getJumpControl().jump();
        wolf.level().broadcastEntityEvent(wolf, (byte) 7);
        Greeting greeting = GREETINGS.computeIfAbsent(wolf.getUUID(), id -> new Greeting());
        greeting.dartUntil = wolf.level().getGameTime() + DART_TICKS;
    }

    /**
     * Every tick for a Hillary who has not been greeted yet, while her
     * recipient is expecting her: notice them, come over, look up at them,
     * keep asking; and watch for them crouching down to her.
     */
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)
                || !(wolf.level() instanceof ServerLevel level)
                || !wolf.hasData(OpeningRegistry.HILLARY)
                || HouseExteriorEntityMirror.isProjection(wolf)) {
            return;
        }
        HillaryTag tag = tagOf(wolf);
        if (tag == null || tag.acknowledged()) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(tag.recipient());
        if (player == null || player.level() != level || !isExpected(player, wolf)) {
            forget(wolf);
            return;
        }

        Greeting greeting = GREETINGS.computeIfAbsent(wolf.getUUID(), id -> new Greeting());
        double distance = wolf.distanceTo(player);
        long now = level.getGameTime();

        if (!greeting.noticed) {
            if (distance > NOTICE_RADIUS || !wolf.hasLineOfSight(player)) {
                return;
            }
            greeting.noticed = true;
            greeting.nextCall = now + 60 + wolf.getRandom().nextInt(40);
            wolf.setOrderedToSit(false);
            wolf.setInSittingPose(false);
            wolf.clearRestriction();
            wolf.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.1F);
        }

        if (distance > GIVE_UP_RADIUS) {
            // Out of sight and out of reach: back to the doorstep to wait.
            greeting.noticed = false;
            greeting.crouchTicks = 0;
            wolf.setIsInterested(false);
            return;
        }

        wolf.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (distance > GREET_DISTANCE) {
            wolf.setIsInterested(false);
            if (now % 10 == 0) {
                wolf.getNavigation().moveTo(player, distance > 8.0D ? 1.2D : 1.0D);
            }
        } else {
            wolf.getNavigation().stop();
            // The head tilt vanilla wolves give someone holding a bone.
            wolf.setIsInterested(true);
        }

        if (now >= greeting.nextCall) {
            boolean whine = wolf.getRandom().nextInt(3) != 0;
            wolf.playSound(whine ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_AMBIENT, 0.8F, whine ? 1.0F : 1.2F);
            greeting.nextCall = now + 80 + wolf.getRandom().nextInt(80);
        }

        if (player.isShiftKeyDown() && distance <= 4.0D && isFacing(player, wolf)) {
            if (++greeting.crouchTicks >= CROUCH_TICKS) {
                acknowledge(wolf);
            }
        } else {
            greeting.crouchTicks = 0;
        }
    }

    /** The player this Hillary belongs to is at the stage where she should come to them. */
    private static boolean isExpected(ServerPlayer player, Wolf wolf) {
        OpeningPlayerState state = OpeningSequence.state(player);
        return state.stage().isAtLeast(OpeningStage.HILLARY_ARRIVED)
                && !state.enteredHouse()
                && wolf.getUUID().equals(state.hillaryUuid());
    }

    private static boolean isFacing(ServerPlayer player, Wolf wolf) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 toWolf = wolf.getEyePosition().subtract(player.getEyePosition());
        double length = toWolf.length();
        return length < 1.0E-4D || look.dot(toWolf.scale(1.0D / length)) > 0.8D;
    }

    private static boolean isGreeting(Wolf wolf) {
        Greeting greeting = GREETINGS.get(wolf.getUUID());
        return greeting != null && greeting.noticed;
    }

    private static boolean isDarting(Wolf wolf) {
        Greeting greeting = GREETINGS.get(wolf.getUUID());
        return greeting != null && wolf.level().getGameTime() < greeting.dartUntil;
    }

    private static void forget(Wolf wolf) {
        Greeting greeting = GREETINGS.remove(wolf.getUUID());
        if (greeting != null && greeting.noticed) {
            wolf.setIsInterested(false);
        }
    }

    /** Drops all greeting state (server stop). */
    public static void clearAll() {
        GREETINGS.clear();
    }

    /**
     * Drives Hillary's opening behavior. Before the invitation she remains
     * near the doorstep. Afterwards she leads toward the manor, advancing only
     * while the player stays close enough to follow.
     */
    public static void tickGuide(
            ServerLevel overworld,
            ServerPlayer player,
            UUID wolfId,
            boolean active,
            @Nullable BlockPos houseOrigin
    ) {
        if (!(overworld.getEntity(wolfId) instanceof Wolf wolf)) {
            return;
        }

        HillaryTag tag = tagOf(wolf);
        if (tag == null || !player.getUUID().equals(tag.recipient())) {
            return;
        }

        if (active && houseOrigin != null && !tag.acknowledged()) {
            // Not greeted yet: onEntityTick handles her while she is
            // greeting; otherwise she waits on her doorstep.
            if (!isGreeting(wolf)) {
                keepNear(wolf, tag.home());
            }
            return;
        }

        if (!active || houseOrigin == null) {
            // Guiding clears her owner so FollowOwnerGoal cannot fight the
            // path. If the player went in before she reached the porch, give
            // her back to them now.
            if (wolf.isTame() && wolf.getOwnerUUID() == null) {
                wolf.setOwnerUUID(tag.recipient());
            }
            keepNear(wolf, tag.home());
            return;
        }

        Vec3 porch = HouseProxyEntityEvacuation.frontDoorExit(overworld, houseOrigin);
        if (porch == null) {
            porch = Vec3.atBottomCenterOf(porchFallback(houseOrigin));
        }

        double toPorch = wolf.distanceToSqr(porch);
        if (toPorch <= MANOR_STOP_RADIUS * MANOR_STOP_RADIUS) {
            settleAtManor(wolf, tag.recipient(), BlockPos.containing(porch));
            return;
        }

        /*
         * A tamed wolf's vanilla FollowOwnerGoal fights a guide path. Keep the
         * tame flag/collar but temporarily clear the owner UUID while Hillary
         * is leading. The recipient is preserved in HillaryTag and restored at
         * the manor.
         */
        if (wolf.isTame() && wolf.getOwnerUUID() != null) {
            wolf.setOwnerUUID(null);
        }
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        wolf.clearRestriction();

        Greeting greeting = GREETINGS.computeIfAbsent(wolf.getUUID(), id -> new Greeting());
        double playerDistance = wolf.distanceToSqr(player);
        if (playerDistance > (double) GUIDE_WAIT_RADIUS * GUIDE_WAIT_RADIUS) {
            wolf.getNavigation().stop();
            if (!greeting.waitingAnnounced) {
                // She stops, turns, and calls back to them.
                greeting.waitingAnnounced = true;
                wolf.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.0F);
            }
            wolf.getLookControl().setLookAt(player, 30.0F, 30.0F);
            return;
        }
        greeting.waitingAnnounced = false;

        boolean darting = isDarting(wolf);
        if (darting
                || playerDistance <= (double) GUIDE_RESUME_RADIUS * GUIDE_RESUME_RADIUS
                || wolf.getNavigation().isDone()) {
            wolf.getNavigation().moveTo(
                    porch.x,
                    porch.y,
                    porch.z,
                    darting ? DART_SPEED : GUIDE_SPEED
            );
        }
    }

    private static void keepNear(Wolf wolf, BlockPos home) {
        if (wolf.isTame()) {
            if (wolf.hasRestriction()) {
                wolf.clearRestriction();
            }
            return;
        }

        if (!wolf.hasRestriction()) {
            wolf.restrictTo(home, LEASH_RADIUS);
        }

        double distanceSquared = wolf.distanceToSqr(Vec3.atBottomCenterOf(home));
        if (wolf.isLeashed()) {
            return;
        }

        if (distanceSquared > 32.0D * 32.0D) {
            Vec3 at = Doorsteps.restingPoint((ServerLevel) wolf.level(), home);
            wolf.moveTo(at.x, at.y, at.z, wolf.getYRot(), 0.0F);
            wolf.getNavigation().stop();
        } else if (distanceSquared > (LEASH_RADIUS + 1.0D) * (LEASH_RADIUS + 1.0D)
                && wolf.getNavigation().isDone()) {
            wolf.getNavigation().moveTo(
                    home.getX() + 0.5D,
                    home.getY(),
                    home.getZ() + 0.5D,
                    1.0D
            );
        }
    }

    private static BlockPos porchFallback(BlockPos houseOrigin) {
        return houseOrigin.offset(
                HouseLayout.AXIS_X,
                1,
                HouseLayout.FRONT_DOOR_Z - 2
        );
    }

    private static void settleAtManor(Wolf wolf, UUID recipient, BlockPos porch) {
        wolf.getNavigation().stop();
        HillaryTag tag = tagOf(wolf);
        wolf.setData(OpeningRegistry.HILLARY, tag == null
                ? new HillaryTag(recipient, porch.immutable(), true)
                : tag.withHome(porch));
        if (wolf.isTame()) {
            wolf.setOwnerUUID(recipient);
            wolf.setOrderedToSit(true);
            wolf.setInSittingPose(true);
            wolf.clearRestriction();
        } else {
            wolf.restrictTo(porch, 4);
        }
    }

}
