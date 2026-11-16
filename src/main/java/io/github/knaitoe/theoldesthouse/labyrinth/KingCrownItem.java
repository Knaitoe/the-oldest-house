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
 *
 * <p>0.4.649, the keepsake (KingKeepsake, docs/SLEEPING_KING_0_4_649.md): out of the King's country the crown is only drawn, an outline
 * with nothing in it, and it fills in for as long as something is looking at its wearer. Which it is, is kept on the crown itself, so
 * that everybody who can see it sees the same crown.
 */
public final class KingCrownItem extends Item implements Equipable {
    static final String OWNER="KingCrownOwner";
    /** 0.4.649: on a crown that is only drawn; and that its wearer has been told once what each of its two looks means. */
    static final String DRAWN="KingCrownDrawn",TOLD_DRAWN="KingCrownToldDrawn",TOLD_FILLED="KingCrownToldFilled";
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
    /** 0.4.649: whether this crown is only drawn (out of the King's country, with nothing looking at its wearer). */
    public static boolean drawn(ItemStack stack){
        if(!is(stack))return false;var data=stack.get(DataComponents.CUSTOM_DATA);return data!=null&&data.contains(DRAWN);
    }
    /** 0.4.649: draws the crown or fills it in. True if that changed it. */
    public static boolean draw(ItemStack stack,boolean drawn){
        if(!is(stack)||drawn(stack)==drawn)return false;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{if(drawn)tag.putBoolean(DRAWN,true);else tag.remove(DRAWN);});
        return true;
    }
    /** 0.4.649: whether the crown's wearer has yet to be told something of it; marks it told. */
    static boolean tell(ItemStack stack,String key){
        var data=stack.get(DataComponents.CUSTOM_DATA);if(data!=null&&data.contains(key))return false;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.putBoolean(key,true));return true;
    }
    /** Whether this player is wearing their own crown: a Queen, on either side. */
    public static boolean crowned(Player p){return p.getUUID().equals(owner(p.getItemBySlot(EquipmentSlot.HEAD)));}
    /** Whether this player has their own crown anywhere about them, worn or carried. */
    public static boolean has(Player p){
        if(crowned(p))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(p.getUUID().equals(owner(inv.getItem(i))))return true;
        return false;
    }

    /** 0.4.649: filling in or going back to a drawing is not the crown being put down and taken up again. */
    @Override public boolean shouldCauseReequipAnimation(ItemStack from,ItemStack to,boolean slotChanged){return slotChanged||!is(from)||!is(to);}
    @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}
    @Override public Holder<SoundEvent> getEquipSound(){return SoundEvents.ARMOR_EQUIP_GOLD;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){return swapWithEquipmentSlot(this,level,p,hand);}
}
