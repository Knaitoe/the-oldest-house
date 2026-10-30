package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.network.ZampanoFolderPayload;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** A reader's own copy of one of Zampanò's leaves, pulled from the courtyard drawers. Using it lifts it to read, turn and turn over. */
public final class ZampanoLeafItem extends Item {
    public static final String LEAF="ZampanoLeaf";
    public ZampanoLeafItem(Properties p){super(p);}
    public static ItemStack of(int leaf){var s=new ItemStack(JournalRegistry.ZAMPANO_LEAF.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt(LEAF,leaf));return s;}
    /** The leaf a stack carries, or -1. */
    public static int leafOf(ItemStack s){if(!s.is(JournalRegistry.ZAMPANO_LEAF.get()))return -1;var d=s.get(DataComponents.CUSTOM_DATA);if(d==null||!d.contains(LEAF))return -1;int i=d.copyTag().getInt(LEAF);return ZampanoLeaves.valid(i)?i:-1;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);int leaf=leafOf(stack);if(leaf<0)return InteractionResultHolder.pass(stack);
        if(player instanceof ServerPlayer p)io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new ZampanoFolderPayload(-1,leaf,0));
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    @Override public Component getName(ItemStack s){int leaf=leafOf(s);return leaf<0?super.getName(s):Component.translatable("item.the_oldest_house.zampano_leaf.titled",ZampanoLeaves.titleOf(leaf));}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){
        if(leafOf(s)>=0){lines.add(Component.translatable("item.the_oldest_house.zampano_leaf.tooltip").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));lines.add(Component.translatable("item.the_oldest_house.zampano_leaf.way").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));}
    }
}
