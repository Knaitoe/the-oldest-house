package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.224: the pizza counter's slice and soda. The slice is overly greasy, and says so on the plate and in the mouth; the soda is
 * drunk, not eaten, and is flat.
 */
public final class PolybiusFood extends Item {
    private final boolean drink;
    static final String[] SLICE_EATEN={"The cheese slides off in one sheet.","Grease runs down to your wrist.","It folds under its own weight and drips."};
    public PolybiusFood(Properties p,boolean drink){super(p);this.drink=drink;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return drink?UseAnim.DRINK:UseAnim.EAT;}
    @Override public SoundEvent getDrinkingSound(){return SoundEvents.GENERIC_DRINK;}
    @Override public SoundEvent getEatingSound(){return drink?SoundEvents.GENERIC_DRINK:SoundEvents.GENERIC_EAT;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        if(drink){lines.add(Component.literal("Mostly ice. Flat.").withStyle(ChatFormatting.GRAY));return;}
        lines.add(Component.literal("Shining with grease.").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("The paper plate has gone see-through.").withStyle(ChatFormatting.GRAY));
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(entity instanceof ServerPlayer p)p.displayClientMessage(Component.literal(drink?"Flat, and mostly ice.":SLICE_EATEN[(int)(level.getGameTime()/40)%SLICE_EATEN.length]),true);
        return super.finishUsingItem(stack,level,entity);
    }
}
