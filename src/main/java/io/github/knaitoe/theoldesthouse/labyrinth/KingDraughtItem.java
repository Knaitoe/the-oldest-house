package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 0.4.644, A sort of thing, part 4 (docs/SLEEPING_KING_0_4_644.md): the bottle (less) and the cake (more) from the glass table on the
 * hill. Each reader takes their own; three swallows or three bites, each of which moves them one step for twenty seconds
 * ({@link KingBreath#swallow}). Only in the country do they do anything; leaving it takes them away again ({@link KingBreath#leave}).
 */
public final class KingDraughtItem extends Item {
    /** Three swallows, three bites. */
    public static final int USES=3;
    /** How long one lasts, in ticks: twenty seconds. */
    public static final int LASTS=400;
    static final String TAG="KingDraught";
    private final boolean more;
    public KingDraughtItem(boolean more){super(new Item.Properties().stacksTo(1).durability(USES));this.more=more;}
    public boolean more(){return more;}
    /** A reader's own bottle or cake, marked so that leaving the country takes it back. */
    public static ItemStack own(boolean more){
        var stack=new ItemStack((more?KingRegistry.CAKE:KingRegistry.BOTTLE).get());
        var tag=new net.minecraft.nbt.CompoundTag();tag.putBoolean(TAG,true);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        return stack;
    }
    public static boolean is(ItemStack stack){return stack.getItem() instanceof KingDraughtItem;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return more?UseAnim.EAT:UseAnim.DRINK;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity e){return 24;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){return ItemUtils.startUsingInstantly(level,p,hand);}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity e){
        if(level.isClientSide||!(e instanceof ServerPlayer p))return stack;
        var b=LiteraryVignettes.current(p)==LabyrinthPlace.SLEEPING_KING?LiteraryVignettes.base(p,LabyrinthPlace.SLEEPING_KING):null;
        if(b==null){King.cue(p,KingTexts.DRAUGHT_NOTHING);return stack;}
        KingBreath.swallow(p,more,level.getGameTime()+LASTS);
        King.cue(p,more?KingTexts.BITE:KingTexts.SWALLOW);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.SLEEPING_KING);
        own.putInt(more?"KingBites":"KingSwallows",own.getInt(more?"KingBites":"KingSwallows")+1);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.SLEEPING_KING,own);
        int used=stack.getDamageValue()+1;
        if(used>=USES)return ItemStack.EMPTY;
        stack.setDamageValue(used);
        return stack;
    }
}
