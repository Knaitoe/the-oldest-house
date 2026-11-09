package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.411: the bouncy ball, thrown. It bounces off every face it meets, losing a little each time, and off anyone it hits without
 * hurting them; when it has almost nothing left it lies where it stopped, as the ball again, for anyone to pick up.
 */
public class PrizeBall extends net.minecraft.world.entity.projectile.ThrowableItemProjectile {
    /** What a bounce keeps across the face and along it, and the speed below which the ball rests. */
    public static final double KEEP=.72,SLIDE=.86,REST=.09;
    public int bounces;
    public PrizeBall(EntityType<? extends PrizeBall> type,Level level){super(type,level);}
    public PrizeBall(Level level,LivingEntity thrower){super(PolybiusRegistry.BALL.get(),thrower,level);}

    @Override protected Item getDefaultItem(){return PolybiusRegistry.PRIZES.get(PolybiusPrize.Kind.BOUNCY_BALL.ordinal()).get();}

    @Override protected void onHitBlock(BlockHitResult hit){
        super.onHitBlock(hit);
        Direction face=hit.getDirection();
        Vec3 n=new Vec3(face.getStepX(),face.getStepY(),face.getStepZ()),v=getDeltaMovement();
        double into=v.dot(n);if(into>=0)return;
        double speed=v.length();
        Vec3 along=v.subtract(n.scale(into)).scale(SLIDE),out=n.scale(-into*KEEP).add(along);
        var at=hit.getLocation().add(n.scale(.13));
        setPos(at.x,at.y,at.z);setDeltaMovement(out);bounces++;
        if(level().isClientSide)return;
        if(speed>.06)level().playSound(null,getX(),getY(),getZ(),PolybiusRegistry.BOING.get(),SoundSource.PLAYERS,(float)Mth.clamp(speed*1.4,.15,1),(float)(.9+Mth.clamp(speed,0,1)*.6));
        if(face==Direction.UP&&out.y<REST*1.5&&out.horizontalDistance()<REST)rest();
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        var v=getDeltaMovement();setDeltaMovement(-v.x*.5,Math.abs(v.y)*.4+.05,-v.z*.5);bounces++;
        if(!level().isClientSide)level().playSound(null,getX(),getY(),getZ(),PolybiusRegistry.BOING.get(),SoundSource.PLAYERS,.6F,1.2F);
    }
    @Override public void tick(){
        super.tick();
        if(!level().isClientSide&&!isRemoved()&&(tickCount>1200||getY()<level().getMinBuildHeight()))rest();
    }
    /** It lies where it stopped, as the ball again. */
    public void rest(){
        if(isRemoved())return;
        var item=new ItemEntity(level(),getX(),getY(),getZ(),getItem().copy());item.setDeltaMovement(Vec3.ZERO);item.setDefaultPickUpDelay();
        level().addFreshEntity(item);discard();
    }
}
