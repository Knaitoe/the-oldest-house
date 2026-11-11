package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 0.4.462: Biscuit, the Corleys' lost dog (BorrascaDogs). He is the game's wolf in body, walk and voice, on a skin of his own, with
 * none of a wolf's business: he hunts nothing, is angered by nothing, cannot be fed, tamed, bred or hurt, and nobody's lead goes on
 * him. The rope he is led by is his own, and when it is let go nothing drops. What a hand on him does is BorrascaDogs' to say.
 */
public final class BorrascaDog extends Wolf {
    public BorrascaDog(EntityType<? extends Wolf> type,Level level){super(type,level);setPersistenceRequired();setInvulnerable(true);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new FloatGoal(this));
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8F));
        goalSelector.addGoal(9,new RandomLookAroundGoal(this));
    }
    @Override public boolean isFood(ItemStack stack){return false;}
    @Override public boolean canMate(Animal other){return false;}
    @Override public boolean canBeLeashed(){return false;}
    @Override public void dropLeash(boolean broadcast,boolean dropItem){super.dropLeash(broadcast,false);}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){return InteractionResult.PASS;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    /** His own voice, so that what is heard is told as a dog: he pants where he lies, and barks now and then on his feet. */
    @Override protected SoundEvent getAmbientSound(){return isInSittingPose()||getRandom().nextInt(4)!=0?BorrascaRegistry.DOG_PANT.get():BorrascaRegistry.DOG_BARK.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return BorrascaRegistry.DOG_WHINE.get();}
    @Override protected SoundEvent getDeathSound(){return BorrascaRegistry.DOG_WHINE.get();}
    @Override public int getAmbientSoundInterval(){return 240;}
}
