package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.203, Work print: the film's monster, from the mannequin by the counter. Stitched sacking a little too long in every
 * limb, gloves with fingers a joint too long, a hood with no face cut into it. Worn in the chest slot, it is drawn over the
 * reader's whole body (WorkPrintCostumeLayer). While its wearer stands still, nothing in the House that hunts by sight takes
 * them for prey ({@link WorkPrint#unseen}).
 */
public final class WorkPrintCostume extends Item implements Equipable {
    public WorkPrintCostume(Properties p){super(p);}
    @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.CHEST;}
    @Override public Holder<SoundEvent> getEquipSound(){return SoundEvents.ARMOR_EQUIP_LEATHER;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return swapWithEquipmentSlot(this,level,player,hand);}
}
