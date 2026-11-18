package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
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
 * 0.4.705, The list, part 2 (docs/THE_LIST_0_4_705.md): the feed sack over the slasher's head, eyeholes cut, a face drawn on it in
 * grease pencil. Worn on the head as a pumpkin is; drawn on the head by its own layer (client/TheListSackLayer) and from inside by its
 * own overlay (client/TheListBodyClient). Each sack is its reader's, marked with their id; it stays in the town.
 */
public final class TheListSack extends Item implements Equipable {
    public TheListSack(Properties properties){super(properties);}
    @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return swapWithEquipmentSlot(this,level,player,hand);}
    @Override public net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> getEquipSound(){return TheListRegistry.SACK_SOUND;}

    /** A sack for this reader. */
    public static ItemStack made(UUID reader){
        var sack=new ItemStack(TheListRegistry.SACK.get());
        CustomData.update(DataComponents.CUSTOM_DATA,sack,t->t.putUUID("ListSack0705",reader));
        return sack;
    }
    public static boolean is(ItemStack stack){return stack.is(TheListRegistry.SACK.get());}
    /** Whose sack it is, if anyone's. */
    public static @Nullable UUID owner(ItemStack stack){
        if(!is(stack))return null;var data=stack.get(DataComponents.CUSTOM_DATA);
        if(data==null)return null;var tag=data.copyTag();return tag.hasUUID("ListSack0705")?tag.getUUID("ListSack0705"):null;
    }
    public static boolean worn(Player player){return is(player.getItemBySlot(EquipmentSlot.HEAD));}
}
