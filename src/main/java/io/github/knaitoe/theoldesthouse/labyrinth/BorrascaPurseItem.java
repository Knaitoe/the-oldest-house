package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 0.4.425: a child's change on the childhood visits and a wallet when grown. What is in it is kept in the reader's own record for the visit
 * (BorrascaPeople), so a dropped or doubled purse holds nothing more; the item only shows it. It belongs to the town: anywhere else it is gone.
 */
public final class BorrascaPurseItem extends Item {
    private final boolean wallet;
    public BorrascaPurseItem(boolean wallet){super(new Item.Properties().stacksTo(1));this.wallet=wallet;}
    public boolean wallet(){return wallet;}
    static ItemStack of(ServerPlayer p,int cents,boolean wallet){var s=new ItemStack(wallet?BorrascaRegistry.WALLET.get():BorrascaRegistry.COINS.get());mark(s,p.getUUID(),cents);return s;}
    static void mark(ItemStack s,UUID owner,int cents){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("BorrascaPurse",owner);t.putInt("Cents",cents);});}
    static @Nullable UUID owner(ItemStack s){var t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return t.hasUUID("BorrascaPurse")?t.getUUID("BorrascaPurse"):null;}
    public static int cents(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("Cents");}
    /** Dollars and cents the way a price card writes them: ".35", "1.50", "20.00". */
    public static String money(int cents){return (cents>=100?String.valueOf(cents/100):"")+"."+String.format("%02d",cents%100);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        int c=cents(stack);
        lines.add(Component.literal(wallet?(c==0?"Empty but for a receipt.":money(c)+" folded in it."):(c==0?"Nothing left but lint.":money(c)+", in coins.")).withStyle(ChatFormatting.GRAY));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var s=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p){int c=cents(s);p.displayClientMessage(Component.literal(c==0?(wallet?"The wallet is empty.":"Your pockets are empty."):"You count it again: "+money(c)+".").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
        return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
    }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){
        if(level.isClientSide||!(holder instanceof ServerPlayer p))return;
        if(!p.getUUID().equals(owner(stack))||!BorrascaSites.inside(p))stack.setCount(0);
    }
}
