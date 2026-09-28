package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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

/**
 * Hillary, the Navidsons' husky.
 *
 * She appears at the player's doorstep, accepts the recipient's first bone,
 * then tries to lead them over ordinary Overworld terrain to the Navidsons'
 * manor. She waits when the player falls behind and stops outside the front
 * door. Hillary never crosses the manor threshold during the opening.
 */
public final class Hillary {
    public static final String NAME = "Hillary";
    public static final int LEASH_RADIUS = 6;

    private static final int GUIDE_RESUME_RADIUS = 14;
    private static final int GUIDE_WAIT_RADIUS = 22;
    private static final double MANOR_STOP_RADIUS = 3.25D;

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

    /** The first bone always tames her, and only her recipient can. */
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()
                || !(event.getTarget() instanceof Wolf wolf)
                || wolf.isTame()) {
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
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        wolf.clearRestriction();
        wolf.level().broadcastEntityEvent(wolf, (byte) 7);
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

        if (!active || houseOrigin == null) {
            keepNear(wolf, tag.home());
            return;
        }

        BlockPos porch = houseOrigin.offset(
                HouseLayout.AXIS_X,
                1,
                HouseLayout.FRONT_DOOR_Z - 2
        );

        double toPorch = wolf.distanceToSqr(Vec3.atBottomCenterOf(porch));
        if (toPorch <= MANOR_STOP_RADIUS * MANOR_STOP_RADIUS) {
            settleAtManor(wolf, tag.recipient(), porch);
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

        double playerDistance = wolf.distanceToSqr(player);
        if (playerDistance > (double) GUIDE_WAIT_RADIUS * GUIDE_WAIT_RADIUS) {
            wolf.getNavigation().stop();
            return;
        }

        if (playerDistance <= (double) GUIDE_RESUME_RADIUS * GUIDE_RESUME_RADIUS
                || wolf.getNavigation().isDone()) {
            wolf.getNavigation().moveTo(
                    porch.getX() + 0.5D,
                    porch.getY(),
                    porch.getZ() + 0.5D,
                    1.15D
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

    private static void settleAtManor(Wolf wolf, UUID recipient, BlockPos porch) {
        wolf.getNavigation().stop();
        wolf.setData(OpeningRegistry.HILLARY, new HillaryTag(recipient, porch.immutable()));
        if (wolf.isTame()) {
            wolf.setOwnerUUID(recipient);
            wolf.setOrderedToSit(true);
            wolf.setInSittingPose(true);
            wolf.clearRestriction();
        } else {
            wolf.restrictTo(porch, 4);
        }
    }

    /** Hillary stays outside when her recipient enters the manor. */
    public static void waitAtManor(MinecraftServer server, UUID wolfId) {
        ServerLevel overworld = server.overworld();
        if (!(overworld.getEntity(wolfId) instanceof Wolf wolf)) {
            return;
        }

        HillaryTag tag = tagOf(wolf);
        if (tag == null) {
            return;
        }

        settleAtManor(wolf, tag.recipient(), tag.home());
    }
}
