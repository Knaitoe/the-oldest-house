package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseProxyEntityEvacuation;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.VignetteYields;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
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

    public static final String INTRO_CONFIRMED="HillaryHousePat";
    public static final String INTRO_DONE="HillaryHouseIntroduced";

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
    /** Hillaries on a scent, and when they give up scratching at the door. */
    private static final Map<UUID, Long> SEEKING = new HashMap<>();
    private static final int SEEK_TICKS = 400;
    private static final int SCRATCH_TICKS = 100;

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

    /** Opening guidance temporarily owns movement, while retaining the wheel's chosen order. */
    public static boolean introductionActive(Entity entity) {
        HillaryTag tag=tagOf(entity);
        if(tag==null||!(entity.level() instanceof ServerLevel level)
                ||level!=level.getServer().overworld()||entity.getPersistentData().getBoolean(INTRO_DONE)
                ||entity.getTags().contains(io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays.PET))return false;
        ServerPlayer player=level.getServer().getPlayerList().getPlayer(tag.recipient());
        return player!=null&&player.level()==level&&isExpected(player,(Wolf)entity)
                &&HouseSavedData.get(level.getServer()).houseOrigin()!=null;
    }
    public static boolean introductionConfirmed(Entity entity) {return entity.getPersistentData().getBoolean(INTRO_CONFIRMED);}
    public static void confirmIntroduction(Wolf wolf,ServerPlayer player) {
        HillaryTag tag=tagOf(wolf);
        if(tag==null||!tag.recipient().equals(player.getUUID()))return;
        acknowledge(wolf);
        if(introductionActive(wolf)) {
            wolf.getPersistentData().putBoolean(INTRO_CONFIRMED,true);
            wolf.setOrderedToSit(false);wolf.setInSittingPose(false);wolf.clearRestriction();
            GREETINGS.computeIfAbsent(wolf.getUUID(),id->new Greeting()).dartUntil=wolf.level().getGameTime()+DART_TICKS;
        }
    }
    private static void finishIntroduction(Wolf wolf) {
        wolf.getPersistentData().putBoolean(INTRO_DONE,true);
        CompanionOrders.resume(wolf);forget(wolf);
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

        if (wolf.getTags().contains(io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays.PET))return;
        if (recipient && stack.isEmpty() && event.getHand()==InteractionHand.MAIN_HAND
                && player instanceof ServerPlayer owner && introductionActive(wolf) && !introductionConfirmed(wolf)) {
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            confirmIntroduction(wolf,owner);return;
        }
        if (recipient && stack.is(Items.COMPASS) && player instanceof ServerPlayer owner) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                SEEKING.remove(wolf.getUUID());
                HillaryPaths.askForExit(wolf, owner);
            }
            return;
        }
        if (recipient && stack.isEmpty()&&!CompanionOrders.managed(wolf)) {
            wolf.getPersistentData().remove("HillaryFindExit");
            SEEKING.remove(wolf.getUUID());
            if (wolf.isTame()) wolf.setOwnerUUID(tag.recipient());
        }

        if (VignetteYields.of(stack) != null && (recipient || wolf.isOwnedBy(player))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (event.getHand() == InteractionHand.MAIN_HAND && wolf.level() instanceof ServerLevel level) {
                takeScent(wolf, level, player.getUUID());
            }
            return;
        }

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
                // Taming at the new house still leaves the invitation waiting for an explicit pat.
                if(introductionActive(wolf))wolf.getPersistentData().remove(INTRO_CONFIRMED);
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
        if (tag == null) return;
        if(introductionActive(wolf))wolf.getPersistentData().putBoolean(INTRO_CONFIRMED,true);
        if(tag.acknowledged())return;
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
        if (wolf.getPersistentData().getBoolean(io.github.knaitoe.theoldesthouse.labyrinth.FinaleController.GUIDE)) return;
        boolean introducing=introductionActive(wolf);
        if (wolf.getTags().contains(io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays.PET))return;
        if (CompanionOrders.managed(wolf)&&!introducing) return;
        if (!introducing && tag != null && tickSeeking(wolf, level)) {
            return;
        }
        if (!introducing && tag != null && HillaryPaths.tickExit(wolf, level, tag.recipient())) return;
        if (tag != null && tag.acknowledged() && level.dimension().equals(HouseDimensions.INTERIOR)
                && !wolf.isOrderedToSit() && !wolf.isTame()) {
            ServerPlayer recipient = level.getServer().getPlayerList().getPlayer(tag.recipient());
            if (recipient != null && recipient.level() == level && wolf.distanceToSqr(recipient) > 9
                    && level.getGameTime() % 10 == 0) wolf.getNavigation().moveTo(recipient, 1.15);
        }
        if (tag == null || (tag.acknowledged()&&(!introducing||introductionConfirmed(wolf)))) {
            return;
        }
        wolf.setOrderedToSit(false);wolf.setInSittingPose(false);
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

        if (!introducing && player.isShiftKeyDown() && distance <= 4.0D && isFacing(player, wolf)) {
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
        SEEKING.clear();
    }

    // ------------------------------------------------------------------
    // Seeking

    /**
     * Something a vignette gave up, held out to her. She takes the scent
     * and makes for the manor's front door, where she scratches to be let
     * in; she never is. The house deals the next vignette door it can, and
     * she can be heard barking behind it, the way she found round outside.
     * With nothing left in the house to find, she whines and lies down.
     */
    public static void takeScent(Wolf wolf, ServerLevel level, UUID seeker) {
        // Keep a saved movement order: the threshold follower and native guide must agree.
        wolf.getPersistentData().remove("HillaryFindExit");
        wolf.playSound(SoundEvents.WOLF_PANT, 1.0F, 1.2F);
        LabyrinthDealer.Scent scent = LabyrinthDealer.giveScent(LabyrinthData.get(level.getServer()), seeker);
        if (scent == LabyrinthDealer.Scent.NOTHING || HouseSavedData.get(level.getServer()).houseOrigin() == null) {
            wolf.playSound(SoundEvents.WOLF_WHINE, 0.8F, 0.8F);
            if (wolf.isTame()) {
                wolf.setOrderedToSit(true);
                wolf.setInSittingPose(true);
            }
            return;
        }
        wolf.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.2F);
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        LabyrinthPlace place = origin == null ? null : LabyrinthPlaces.placeAt(origin, wolf.blockPosition());
        if (level.dimension().equals(HouseDimensions.INTERIOR)) {
            if(place!=null)LabyrinthDealer.dealPlace(LabyrinthData.get(level.getServer()), seeker, place, wolf.getRandom());
            else {var door=LabyrinthData.get(level.getServer()).door("hallway_end");if(door!=null)LabyrinthDealer.deal(LabyrinthData.get(level.getServer()),seeker,java.util.List.of(door),null,wolf.getRandom());}
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(seeker);
            if (owner != null) { CompanionOrders.issue(wolf, owner, CompanionOrders.Order.DEEPER); return; }
        }
        SEEKING.put(wolf.getUUID(), level.getGameTime() + 2400);
    }

    public static boolean isSeeking(Wolf wolf) {
        return SEEKING.containsKey(wolf.getUUID());
    }

    /** On a scent: to the front door, then scratching at it. Returns false once she has given up. */
    private static boolean tickSeeking(Wolf wolf, ServerLevel level) {
        Long until = SEEKING.get(wolf.getUUID());
        if (until == null) {
            return false;
        }
        long now = level.getGameTime();
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (now >= until || origin == null || !level.dimension().equals(Level.OVERWORLD)) {
            if (level.dimension().equals(HouseDimensions.INTERIOR) && now < until && origin != null) {
                HillaryTag tag = tagOf(wolf);
                return tag != null && HillaryPaths.tickDeeper(wolf, level, tag.recipient());
            }
            SEEKING.remove(wolf.getUUID());
            wolf.getNavigation().stop();
            if (wolf.isTame()) {
                HillaryTag owner = tagOf(wolf);
                if (owner != null) wolf.setOwnerUUID(owner.recipient());
                wolf.setOrderedToSit(true);
                wolf.setInSittingPose(true);
            }
            return false;
        }
        Vec3 porch = HouseProxyEntityEvacuation.frontDoorExit(level, origin);
        if (porch == null) {
            porch = Vec3.atBottomCenterOf(porchFallback(origin));
        }
        BlockPos door = origin.offset(HouseLayout.FRONT_DOOR.x(), HouseLayout.FRONT_DOOR.y(), HouseLayout.FRONT_DOOR.z());
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        if (wolf.position().distanceTo(porch) > 1.5D) {
            if (now % 10 == 0) {
                wolf.getNavigation().moveTo(porch.x, porch.y, porch.z, GUIDE_SPEED);
            }
            return true;
        }
        wolf.getNavigation().stop();
        wolf.getLookControl().setLookAt(Vec3.atCenterOf(door));
        if (until - now > SCRATCH_TICKS) {
            // Arrived: scratch for a while, then give up.
            SEEKING.put(wolf.getUUID(), now + SCRATCH_TICKS);
        }
        if (now % 8 == 0) {
            level.playSound(null, door, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.12F, 1.9F);
        }
        if (now % 40 == 0) {
            wolf.playSound(SoundEvents.WOLF_WHINE, 0.7F, 1.1F);
        }
        return true;
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
        if (wolf.getTags().contains(io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays.PET))return;

        HillaryTag tag = tagOf(wolf);
        if (tag == null || !player.getUUID().equals(tag.recipient())) {
            return;
        }

        if(!active) {
            if(!wolf.getPersistentData().getBoolean(INTRO_DONE)&&OpeningSequence.state(player).enteredHouse())finishIntroduction(wolf);
            if(CompanionOrders.managed(wolf))return;
        }
        if(wolf.getPersistentData().getBoolean(INTRO_DONE))return;
        if (active && houseOrigin != null && (!tag.acknowledged()||!introductionConfirmed(wolf))) {
            wolf.setOrderedToSit(false);wolf.setInSittingPose(false);
            // A saved Stay command must not suppress her request for a pat.
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
            if(player.distanceToSqr(porch)<=64)finishIntroduction(wolf);
            else {wolf.setOrderedToSit(false);wolf.setInSittingPose(false);wolf.getLookControl().setLookAt(player);}
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

    /** Capture the real nearby companion before a threshold moves its player. */
    @Nullable public static Wolf following(ServerPlayer player) {
        UUID id = OpeningSequence.state(player).hillaryUuid();
        if (id == null || !(player.serverLevel().getEntity(id) instanceof Wolf wolf)) return null;
        HillaryTag tag = tagOf(wolf);
        return tag != null && tag.acknowledged() && wolf.isAlive() && !wolf.isOrderedToSit()
                && !wolf.isLeashed() && !wolf.isPassenger() && !HouseExteriorEntityMirror.isProjection(wolf)
                && wolf.distanceToSqr(player) <= 144 ? wolf : null;
    }

    /** Normal dimension transfer retains identity, tame state, health and attachments. */
    @Nullable public static Wolf followAcross(@Nullable Wolf wolf, ServerPlayer player) {
        if (wolf == null || wolf.isRemoved()) return null;
        ServerLevel to = player.serverLevel();
        Vec3 point = HillaryPaths.safeBeside(wolf, player);
        if (wolf.level() != to) {
            Entity moved = wolf.changeDimension(new DimensionTransition(to, point, Vec3.ZERO,
                    player.getYRot(), 0, DimensionTransition.PLACE_PORTAL_TICKET));
            if (!(moved instanceof Wolf arriving)) return null;
            wolf = arriving;
        } else wolf.teleportTo(point.x, point.y, point.z);
        HillaryTag tag = tagOf(wolf);
        if (tag != null && wolf.isTame()) wolf.setOwnerUUID(tag.recipient());
        wolf.clearRestriction();
        wolf.getNavigation().stop();
        wolf.setDeltaMovement(Vec3.ZERO);
        wolf.resetFallDistance();
        return wolf;
    }

    private static void settleAtManor(Wolf wolf, UUID recipient, BlockPos porch) {
        wolf.getNavigation().stop();
        HillaryTag tag = tagOf(wolf);
        boolean firstArrival = tag == null || !tag.home().equals(porch);
        wolf.setData(OpeningRegistry.HILLARY, tag == null
                ? new HillaryTag(recipient, porch.immutable(), true)
                : tag.withHome(porch));
        if (wolf.isTame()) {
            wolf.setOwnerUUID(recipient);
            if (firstArrival) {
                wolf.setOrderedToSit(false);
                wolf.setInSittingPose(false);
            }
            wolf.clearRestriction();
        } else {
            wolf.restrictTo(porch, 4);
        }
    }

}
