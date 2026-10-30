package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
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

    /** First person: both hands come up in front, a little apart, where the partner's waist is, and sway with them. */
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
            // The vanilla skin-arm basis (as the Shallows' carry uses it), brought forward and a hand's width apart.
            pose.translate(side * .36 + sway * .04, -.52, -.98);
            pose.mulPose(Axis.YP.rotationDegrees(side * 22));pose.mulPose(Axis.ZP.rotationDegrees(sway * 4));
            pose.translate(-side, 3.6, 3.5);
            pose.mulPose(Axis.ZP.rotationDegrees(side * 120));pose.mulPose(Axis.XP.rotationDegrees(196));
            pose.mulPose(Axis.YP.rotationDegrees(-side * 135));pose.translate(side * 5.6, 0, 0);
            if (side > 0) renderer.renderRightHand(pose, e.getMultiBufferSource(), e.getPackedLight(), mc.player);
            else renderer.renderLeftHand(pose, e.getMultiBufferSource(), e.getPackedLight(), mc.player);
            pose.popPose();
        }
    }
}
