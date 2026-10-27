package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
/** One finite original serving, consumed through seven native eating actions. */
public final class UncannyMealItem extends Item {
    public static final int PORTIONS=7;
    public UncannyMealItem(Properties p){super(p);}
    public static int bites(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?0:Math.max(0,Math.min(PORTIONS,c.copyTag().getInt("MealBites133")));}
    private static boolean owns(Player p,ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null||!c.copyTag().hasUUID("UncannyMealOwner")||c.copyTag().getUUID("UncannyMealOwner").equals(p.getUUID());}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){return owns(p,p.getItemInHand(hand))?super.use(l,p,hand):InteractionResultHolder.fail(p.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){
        if(e instanceof Player p&&!owns(p,s))return s;
        var before=s.copy();int used=bites(s)+1;var model=s.get(DataComponents.CUSTOM_MODEL_DATA);int order=model==null?1:Math.max(1,Math.min(4,model.value()%10));var out=super.finishUsingItem(s,l,e);
        if(used<PORTIONS){s.setCount(1);CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("MealBites133",used));s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(used*10+order));out=s;}
        if(e instanceof ServerPlayer p){UncannyStories.ate(p,before);out=DinerPlate.finishBite(p,out);}return out;
    }
}
