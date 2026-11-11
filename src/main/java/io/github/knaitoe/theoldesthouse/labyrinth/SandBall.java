package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.465, Bike night (owner: "the bullies approach, players throw balls of sand"): a ball of the sandpit's sand in the air. It is
 * heavier than a snowball and a child threw it, so it does not go far. It bursts on whatever it meets and hurts nobody; what it met is
 * the night's business (BikeNight.sanded, BikeNight.sandLanded).
 */
public class SandBall extends ThrowableItemProjectile {
    /** How hard a child throws it, how wide of the aim it may go, and how it falls. */
    public static final float THROW=1.15F,WIDE=1F;
    public static final double WEIGHT=.05;
    public SandBall(EntityType<? extends SandBall> type,Level level){super(type,level);}
    public SandBall(Level level,LivingEntity thrower){super(BikeNightRegistry.SAND_BALL_THROWN.get(),thrower,level);}
    @Override protected Item getDefaultItem(){return BikeNightRegistry.SAND_BALL.get();}
    @Override protected double getDefaultGravity(){return WEIGHT;}
    private boolean begun;
    /**
     * 0.4.483: thrown at somebody who is right on top of the thrower, it starts inside them, and a flight that starts inside a body
     * never meets it: the ball went through one of the three at arm's length, which is when a reader most needs it. Whoever it
     * starts inside, it has met.
     */
    @Override public void tick(){
        if(!begun&&!level().isClientSide){
            begun=true;var owner=getOwner();
            // With more than one of them there, it meets one who is not coughing already.
            var inside=level().getEntities(this,getBoundingBox().inflate(.15),x->x!=owner&&canHitEntity(x));
            inside.sort(java.util.Comparator.comparingInt(e->BikeNightBullies.coughing(e)?1:0));
            if(!inside.isEmpty()){onHit(new EntityHitResult(inside.get(0)));return;}
        }
        super.tick();
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        super.onHitEntity(hit);
        if(level() instanceof ServerLevel l)BikeNight.sanded(l,this,hit.getEntity());
    }
    @Override protected void onHitBlock(BlockHitResult hit){
        super.onHitBlock(hit);
        if(level() instanceof ServerLevel l)BikeNight.sandLanded(l,this,hit.getBlockPos());
    }
    @Override protected void onHit(HitResult hit){
        super.onHit(hit);
        if(level() instanceof ServerLevel l){burst(l,hit.getLocation());discard();}
    }
    /** Sand going everywhere: seen and heard by anyone near. */
    public static void burst(ServerLevel l,Vec3 at){
        var sand=Blocks.SAND.defaultBlockState();
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,sand),at.x,at.y,at.z,14,.15,.15,.15,.02);
        l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,sand),at.x,at.y+.1,at.z,6,.2,.1,.2,0);
        l.playSound(null,at.x,at.y,at.z,BikeNightRegistry.SAND_BURST.get(),SoundSource.PLAYERS,.7F,.9F+l.getRandom().nextFloat()*.2F);
    }
}
