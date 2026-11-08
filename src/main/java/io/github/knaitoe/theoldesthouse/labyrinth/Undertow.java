package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * 0.4.352, The Last Crossing, Night One (docs/LAST_CROSSING_0_4_352.md): the things that come up out of the water at night, what the
 * design calls the source's mobs. A zombie's body and appetite on the station's own skin: they don't burn by day, they don't turn into
 * anything in the water, they don't break doors, they carry and drop nothing and call nobody else. LastCrossingNight brings them up out of
 * the spillway (and through the inspection landing when the lower floor is lost and left open) and sends them back into the water at
 * dawn.
 *
 * <p>0.4.353: they pass a carrier by. One of the crew with a claw's worth of the source in them is not what they come up for; they go for
 * whoever else is nearest, and turn only on a carrier who hurts them.
 *
 * <p>0.4.354: one drawn by a turned reader's call after dark goes where it was called and takes nobody on the way.
 */
public class Undertow extends Zombie {
    public Undertow(EntityType<? extends Zombie> type,Level level){super(type,level);setCanPickUpLoot(false);setCanBreakDoors(false);setPersistenceRequired();}
    public static AttributeSupplier.Builder attributes(){return Zombie.createAttributes().add(Attributes.MAX_HEALTH,16).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE,0).add(Attributes.FOLLOW_RANGE,28);}
    @Override protected void addBehaviourGoals(){
        goalSelector.addGoal(2,new net.minecraft.world.entity.ai.goal.ZombieAttackGoal(this,1.0,false));
        goalSelector.addGoal(7,new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this,1.0));
        targetSelector.addGoal(1,new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        targetSelector.addGoal(2,new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this,net.minecraft.world.entity.player.Player.class,10,true,false,
            t->!(t instanceof net.minecraft.server.level.ServerPlayer p&&LastCrossing.spared(p))&&!LastCrossingNightTwo.drawn(this)));
    }
    @Override protected boolean isSunSensitive(){return false;}
    @Override protected boolean convertsInWater(){return false;}
    @Override public boolean isBaby(){return false;}
    @Override public void setBaby(boolean baby){}
    @Override protected int getBaseExperienceReward(){return 0;}
    @Override protected SoundEvent getAmbientSound(){return CrossingRegistry.UNDERTOW.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.DROWNED_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.DROWNED_DEATH;}
    @Override protected SoundEvent getStepSound(){return SoundEvents.DROWNED_STEP;}
    @Override public int getAmbientSoundInterval(){return 160;}
    @Override public void tick(){
        super.tick();
        // Still running with water.
        if(level() instanceof ServerLevel l&&tickCount%12==0)l.sendParticles(ParticleTypes.DRIPPING_WATER,getX(),getY()+1.2,getZ(),2,.25,.4,.25,0);
    }
}
