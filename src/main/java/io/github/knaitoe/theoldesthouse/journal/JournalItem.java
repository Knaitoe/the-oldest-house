package io.github.knaitoe.theoldesthouse.journal;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** A reader's own journal. Using it opens the pages; only its owner can, and only the copy that is theirs now. */
public final class JournalItem extends Item {
    public JournalItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(p instanceof ServerPlayer reader)Journals.open(reader,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    /** Picked up from where it lay open, it is shut again. */
    @Override public void inventoryTick(ItemStack stack,Level level,net.minecraft.world.entity.Entity holder,int slot,boolean selected){
        if(!level.isClientSide&&PostMortem.isOpen(stack))net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,stack,t->t.remove(PostMortem.OPEN));
    }
    /** Burnt, crushed or torn apart out in the world: it finds its way back to its owner. */
    @Override public void onDestroyed(ItemEntity item){
        if(item.level() instanceof ServerLevel level)Journals.get(level.getServer()).markLost(item.getItem());
    }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> text,TooltipFlag f){
        String name=Journals.nameOf(s);
        if(!name.isEmpty())text.add(Component.literal("Inside the cover: "+name+".").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        int wear=Journals.wearOf(s);
        String[] worn={"","Water rings on the cover.","Dog-eared.","The pages have swollen.","A corner is torn away."};
        if(wear>0)text.add(Component.literal(worn[Math.min(wear,Journals.WORN)]).withStyle(ChatFormatting.DARK_GRAY));
    }
}
