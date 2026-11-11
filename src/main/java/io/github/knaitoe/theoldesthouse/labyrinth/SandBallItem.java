package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 0.4.465, Bike night: a ball of sand in the hand. Packed from the playground's sand (BikeNight.scoop), eight at most to a reader.
 * Using it throws it. It does not keep: let fall, it falls apart where it lands; carried out of the night, it runs out through the
 * fingers.
 */
public class SandBallItem extends Item {
    /** The most a reader carries, and the ticks between one throw and the next. */
    public static final int MOST=8,WAIT=6;
    public SandBallItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(!level.isClientSide){
            var ball=new SandBall(level,p);ball.setItem(stack.copyWithCount(1));
            ball.shootFromRotation(p,p.getXRot(),p.getYRot(),0F,SandBall.THROW,SandBall.WIDE);level.addFreshEntity(ball);
            level.playSound(null,p.getX(),p.getY(),p.getZ(),BikeNightRegistry.SAND_THROW.get(),SoundSource.PLAYERS,.5F,.9F+level.getRandom().nextFloat()*.2F);
            if(p instanceof ServerPlayer reader)BikeNight.threw(reader);
        }
        p.getCooldowns().addCooldown(this,WAIT);
        stack.consume(1,p);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    /** Out of the night, there is nothing left of it. */
    @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){
        if(level.isClientSide||!(holder instanceof ServerPlayer p)||p.tickCount%20!=0||stack.isEmpty()||BikeNight.here(p))return;
        stack.setCount(0);
        BikeNight.cue(p,BikeNightTexts.SAND_GONE);
    }
    /** Let fall, it falls apart where it lands. */
    @Override public boolean onEntityItemUpdate(ItemStack stack,ItemEntity entity){
        if(entity.level() instanceof ServerLevel l&&(entity.onGround()||entity.isInWater()||entity.getAge()>40)){SandBall.burst(l,entity.position());entity.discard();return true;}
        return false;
    }
}
