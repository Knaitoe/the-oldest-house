package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 0.4.415: a dart. Holding use raises it. The aim settles for a moment, then starts to sway, more the longer it is held; crouching
 * halves the sway. Letting go throws it along the swayed aim. The sway is worked out the same way on both sides from how long the dart
 * has been held, so the view sways with it (client/DartsClient) and the dart goes where the crosshair was.
 *
 * <p>The darts in play are the house's: a board hands three to whoever is throwing and takes back what is left ({@link DartBoards}).
 */
public final class DartItem extends Item {
    /** A dart held for less than this is put down again, not thrown. */
    public static final int MIN_HOLD=4;
    /** How fast a dart leaves the hand, in blocks a tick. */
    public static final float SPEED=1.1F;
    public DartItem(Properties p){super(p);}
    @Override public int getUseDuration(ItemStack stack,LivingEntity e){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPEAR;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        p.startUsingItem(hand);return InteractionResultHolder.consume(p.getItemInHand(hand));
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity e,int left){
        int held=getUseDuration(stack,e)-left;
        if(held<MIN_HOLD||level.isClientSide||!(e instanceof ServerPlayer p))return;
        DartBoards.thrown(p,stack,held);
    }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity e,int slot,boolean selected){
        if(!level.isClientSide&&e instanceof ServerPlayer p&&!DartBoards.keeps(p,stack))stack.setCount(0);
    }

    /** How far the aim wanders, in degrees, after a dart has been held this long. */
    public static double amplitude(float held,boolean crouching){
        double a=held<14?2.2-(2.2-.55)*held/14:held<30?.55:Math.min(4.5,.55+(held-30)*.06);
        return crouching?a/2:a;
    }
    /** Where the aim has wandered to, as yaw and pitch in degrees. */
    public static double[] sway(float held,boolean crouching,double phase){
        double a=amplitude(held,crouching);
        return new double[]{a*(.72*Math.sin(held*.23+phase)+.28*Math.sin(held*.61+phase*1.7)),a*(.72*Math.sin(held*.17+phase*2.3)+.28*Math.sin(held*.53+phase*.6))};
    }
    /** Each thrower's own wander, different for each dart of a turn. */
    public static double phase(UUID thrower,int dartsLeft){
        long h=thrower.getMostSignificantBits()^thrower.getLeastSignificantBits();
        return Math.floorMod(h,1000L)/1000.0*Math.PI*2+dartsLeft*1.3;
    }
}
