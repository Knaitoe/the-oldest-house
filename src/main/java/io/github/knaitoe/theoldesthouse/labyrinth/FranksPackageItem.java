package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
/** A finite packet of four franks that retains its original round. */
public final class FranksPackageItem extends Item {
    public FranksPackageItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var packet=player.getItemInHand(hand);
        if(!level.isClientSide){
            var franks=new ItemStack(GoatmanRegistry.BRAT.get(),4);var tag=packet.get(DataComponents.CUSTOM_DATA);
            if(tag!=null)franks.set(DataComponents.CUSTOM_DATA,CustomData.of(tag.copyTag()));
            packet.shrink(1);if(packet.isEmpty())return InteractionResultHolder.success(franks);
            if(!player.getInventory().add(franks))player.drop(franks,false);
        }
        return InteractionResultHolder.sidedSuccess(packet,level.isClientSide);
    }
}
