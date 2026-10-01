package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** A carried artifact: opens the church in the House, masks its bearer from drowned outside it. */
public final class ChurchKeyItem extends Item {
    public ChurchKeyItem(Properties properties) { super(properties); }

    public static boolean protects(ServerPlayer player) {
        if (player.level().dimension().equals(HouseDimensions.INTERIOR)
                || player.level().dimension().equals(HouseDimensions.BETWEEN)) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).is(DrownedTownRegistry.CHURCH_KEY.get())) return true;
        return false;
    }

    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Drowned
                && event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && protects(player))
            event.setNewAboutToBeSetTarget(null);
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Drowned drowned && !drowned.level().isClientSide()
                && drowned.getTarget() instanceof ServerPlayer player && protects(player)) {
            drowned.setTarget(null);
            drowned.setAggressive(false);
            drowned.getNavigation().stop();
        }
    }
}
