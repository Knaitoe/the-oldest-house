package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A strip of cloth worn on the head, like a carved pumpkin. While it is on,
 * the screen is black but for a thin strip at the bottom where the floor
 * around the player's feet shows (drawn by the client). Not the Blindness
 * effect: that still shows a few blocks, and milk clears it.
 */
public final class BlindfoldItem extends Item implements Equipable {
    public BlindfoldItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    public static boolean isWorn(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(LabyrinthRegistry.BLINDFOLD.get());
    }
}
