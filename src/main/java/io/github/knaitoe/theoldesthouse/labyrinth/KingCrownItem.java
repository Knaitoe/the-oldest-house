package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 0.4.648, A sort of thing, part 8 (docs/SLEEPING_KING_0_4_648.md): the crown that comes down on a reader counted on the eighth square
 * (KingFeast). It is real and worn on the head, and it is notice: a reader wearing their own crown is never less than a sort of thing
 * anywhere in the country (KingBreath), goes through hedges and over brooks as a Queen does (KingHedgeBlock, KingVergeBlock), and beside
 * the King is in check (KingSleep). Each crown is its wearer's own: another reader's crown on a head is only a crown.
 */
public final class KingCrownItem extends Item implements Equipable {
    static final String OWNER="KingCrownOwner";
    public KingCrownItem(){super(new Item.Properties().stacksTo(1));}

    /** A crown for this reader. */
    public static ItemStack of(UUID owner){
        var stack=new ItemStack(KingRegistry.CROWN.get());
        var tag=new net.minecraft.nbt.CompoundTag();tag.putUUID(OWNER,owner);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        return stack;
    }
    public static boolean is(ItemStack stack){return stack.getItem() instanceof KingCrownItem;}
    /** Whose crown this is, if anybody's. */
    public static @Nullable UUID owner(ItemStack stack){
        if(!is(stack))return null;var data=stack.get(DataComponents.CUSTOM_DATA);if(data==null)return null;
        var tag=data.copyTag();return tag.hasUUID(OWNER)?tag.getUUID(OWNER):null;
    }
    /** Whether this player is wearing their own crown: a Queen, on either side. */
    public static boolean crowned(Player p){return p.getUUID().equals(owner(p.getItemBySlot(EquipmentSlot.HEAD)));}
    /** Whether this player has their own crown anywhere about them, worn or carried. */
    public static boolean has(Player p){
        if(crowned(p))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(p.getUUID().equals(owner(inv.getItem(i))))return true;
        return false;
    }

    @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}
    @Override public Holder<SoundEvent> getEquipSound(){return SoundEvents.ARMOR_EQUIP_GOLD;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){return swapWithEquipmentSlot(this,level,p,hand);}
}
