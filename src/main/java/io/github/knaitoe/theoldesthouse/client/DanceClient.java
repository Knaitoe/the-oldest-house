package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadShelter;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The reader's own arms in the shelter's slow dance (0.4.194). The owner: "The NPCs arms look great. The players arms are
 * still in their neutral pose, making the scene look unnatural." While one of the reader's strangers dances an arm's length in
 * front of them, the reader holds the partner at the waist, swaying with the partner's sway: on their model (seen from
 * behind, in a mirror or by anyone who sees the partner) and in their own first-person view, where both hands come up in
 * front of them. Nothing here touches the server; the dance itself and its partner are the shelter's.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class DanceClient {
    private DanceClient() {}
    /** How near a dancing stranger has to be for the reader to be holding them. */
    static final double REACH = 2.2;

    /** The stranger dancing with this player, if one is within reach. */
    public static @Nullable LiteraryActor partner(Player player) {
        if (player == null || player.level() == null) return null;
        LiteraryActor best = null;double nearest = REACH * REACH;
        for (var a : player.level().getEntitiesOfClass(LiteraryActor.class, player.getBoundingBox().inflate(REACH))) {
            if ((a.role() != LiteraryActor.READ_MAN && a.role() != LiteraryActor.READ_WOMAN) || a.gesture() != HouseReadShelter.DANCE) continue;
            if (a.owner().isPresent() && !a.owner().get().equals(player.getUUID())) continue;
            double d = a.distanceToSqr(player);
            if (d < nearest) { nearest = d;best = a; }
        }
        return best;
    }
    /** The partner's sway, so the two move together. */
    static float sway(LiteraryActor partner, float partial) { return Mth.sin((partner.tickCount + partial) * .06F); }

    /** On the model: hands forward and in, at the partner's waist, the arms turning with the body's sway. */
    public static void pose(PlayerModel<?> model, LivingEntity entity, float age) {
        if (!(entity instanceof Player player)) return;
        var partner = partner(player);
        if (partner == null) return;
        float partial = age - entity.tickCount, sway = sway(partner, partial);
        model.body.yRot = -sway * .12F;
        model.rightArm.xRot = model.leftArm.xRot = -.95F;
        model.rightArm.yRot = -.36F + model.body.yRot;model.leftArm.yRot = .36F + model.body.yRot;
        model.rightArm.zRot = model.leftArm.zRot = 0;
        model.rightLeg.xRot = -sway * .12F;model.leftLeg.xRot = sway * .12F;model.rightLeg.yRot = model.leftLeg.yRot = 0;
        model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);model.jacket.copyFrom(model.body);
    }

    /** Where each first-person hand rests, in view space: at the partner's side, a little below the chest. */
    static final float HAND_X=.29F,HAND_Y=-.35F,HAND_Z=-.74F;
    /** The way back along the forearm, from the hand toward the shoulder below the view, and the arm's scale. */
    static final float BACK_X=.12F,BACK_Y=-.45F,BACK_Z=.58F,SCALE=.72F;

    /**
     * First person: both forearms come up from below the view to the partner's sides, thumbs up, and sway with them. 0.4.199
     * (owner: "Player arms still need proper scaling"): the vanilla empty-hand basis drew each arm at full size spread past the
     * partner's sides; the arm is now aimed from its hand back toward the shoulder and drawn at three quarters, so the hands rest
     * on the partner the size a reader's own hands would be at arm's length.
     */
    @SubscribeEvent public static void hand(RenderHandEvent e) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        var partner = partner(mc.player);
        if (partner == null) return;
        e.setCanceled(true);
        if (e.getHand() != InteractionHand.MAIN_HAND || !(mc.getEntityRenderDispatcher().getRenderer(mc.player) instanceof PlayerRenderer renderer)) return;
        float sway = sway(partner, e.getPartialTick());var missing = BodyLoss.missing(mc.player);
        for (int side : new int[]{-1, 1}) {
            if (missing == (side < 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT)) continue;
            var pose = e.getPoseStack();pose.pushPose();
            var placed = aim(side, new Vector3f(side * HAND_X + sway * .03F, HAND_Y + Math.abs(sway) * .01F, HAND_Z), new Vector3f(side * BACK_X, BACK_Y, BACK_Z), SCALE);
            pose.translate(placed.offset.x, placed.offset.y, placed.offset.z);pose.mulPose(placed.turn);pose.scale(SCALE, SCALE, SCALE);
            if (side > 0) renderer.renderRightHand(pose, e.getMultiBufferSource(), e.getPackedLight(), mc.player);
            else renderer.renderLeftHand(pose, e.getMultiBufferSource(), e.getPackedLight(), mc.player);
            pose.popPose();
        }
    }
    /** A turn and an offset that put the arm's hand end at {@code hand}, its length running back along {@code back}, thumb side up. */
    record Aim(Vector3f offset, Quaternionf turn) {}
    static Aim aim(int side, Vector3f hand, Vector3f back, float scale) {
        // The arm as renderRightHand/LeftHand draws it: its pivot at (-5, 2, 0) or (5, 2, 0) pixels, turned by its idle bob (0.1),
        // the box's middle at x -1 or 1, from the shoulder at y -2 to the hand at y 10.
        float bob = side > 0 ? .1F : -.1F, cx = (side > 0 ? -1 : 1) / 16F;
        var part = new Matrix4f().translation((side > 0 ? -5 : 5) / 16F, 2 / 16F, 0).rotateZ(bob);
        var handEnd = part.transformPosition(new Vector3f(cx, 10 / 16F, 0));var shoulderEnd = part.transformPosition(new Vector3f(cx, -2 / 16F, 0));
        var axis = new Vector3f(handEnd).sub(shoulderEnd).normalize();var want = new Vector3f(back).negate().normalize();
        var turn = new Quaternionf().rotationTo(axis, want);
        // Roll about the arm until its front face (the thumb side, -z on the model) faces as near up as the arm allows.
        var front = turn.transform(new Vector3f(0, 0, -1));var up = new Vector3f(0, 1, 0);
        front.sub(new Vector3f(want).mul(front.dot(want)));up.sub(new Vector3f(want).mul(up.dot(want)));
        float roll = (float) Math.atan2(new Vector3f(front).cross(up).dot(want), front.dot(up));
        turn = new Quaternionf().rotationAxis(roll, want.x, want.y, want.z).mul(turn);
        var at = turn.transform(new Vector3f(handEnd).mul(scale));
        return new Aim(new Vector3f(hand).sub(at), turn);
    }
}
