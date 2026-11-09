package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.425: what Drisking's shops sell (docs/BORRASCA_PEOPLE_0_4_425.md). Pie and coffee at the diner, soda and candy at the station, the
 * store and the drugstore, a malt at the fountain, beer at the bar, and the receptionist's lollipop. Each says what it is on the hover and
 * one plain thing when it is finished.
 */
public final class BorrascaFood extends Item {
    public enum Kind {
        PIE(false,6,.5F,"Today's pie, on a saucer, a fork across it.","Still warm in the middle."),
        COFFEE(true,1,.1F,"Diner coffee in a thick cup.","Hot, and bitter at the bottom."),
        MILK_COFFEE(true,2,.2F,"More milk than coffee, and two sugars in it.","Sweet. Barely coffee."),
        SODA(true,1,.1F,"A glass bottle, cold from the ice water.","It fizzes up into your nose."),
        CANDY(false,3,.2F,"Soft at one corner from somebody's pocket.","Peanuts and caramel, stuck in your teeth."),
        LOLLIPOP(false,1,.1F,"Wrapped, on a paper stick.","It lasts a long time."),
        MALT(true,4,.4F,"Too thick for the straw, with a cherry on top.","The last of it rattles in the straw."),
        BEER(true,1,.1F,"A long-neck, sweating.","Cold and thin.");
        final boolean drink;final int nutrition;final float saturation;final String hover,finished;
        Kind(boolean drink,int nutrition,float saturation,String hover,String finished){this.drink=drink;this.nutrition=nutrition;this.saturation=saturation;this.hover=hover;this.finished=finished;}
        /** The food each kind is: drinks can always be drunk; coffee gives a short burst of speed. */
        public FoodProperties food(){var b=new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);if(drink||this==LOLLIPOP||this==CANDY)b.alwaysEdible();if(this==COFFEE)b.effect(()->new MobEffectInstance(MobEffects.MOVEMENT_SPEED,400,0),1);return b.build();}
    }
    private final Kind kind;
    public BorrascaFood(Kind kind){super(new Item.Properties().stacksTo(16).food(kind.food()));this.kind=kind;}
    public Kind kind(){return kind;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return kind.drink?UseAnim.DRINK:UseAnim.EAT;}
    @Override public SoundEvent getDrinkingSound(){return SoundEvents.GENERIC_DRINK;}
    @Override public SoundEvent getEatingSound(){return kind.drink?SoundEvents.GENERIC_DRINK:SoundEvents.GENERIC_EAT;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.literal(kind.hover).withStyle(ChatFormatting.GRAY));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(entity instanceof ServerPlayer p)p.displayClientMessage(Component.literal(kind.finished).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return super.finishUsingItem(stack,level,entity);
    }
}
