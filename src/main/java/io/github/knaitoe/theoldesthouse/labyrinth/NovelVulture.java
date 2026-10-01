package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/** Its circle belongs to the distant photograph. It never attacks or swoops. */
public final class NovelVulture extends PathfinderMob {
    private BlockPos center=BlockPos.ZERO;
    private double angle;
    public NovelVulture(EntityType<? extends NovelVulture> t,Level l){super(t,l);setNoAi(true);setNoGravity(true);setPersistenceRequired();}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,10).add(Attributes.MOVEMENT_SPEED,0);}
    public void circle(BlockPos at){center=at;}
    @Override public void tick(){super.tick();if(level().isClientSide)return;angle+=.009;moveTo(center.getX()+Math.cos(angle)*7,center.getY()+9+Math.sin(angle*.4)*.3,center.getZ()+Math.sin(angle)*7,(float)(-angle*180/Math.PI),0);}
    @Override public boolean hurt(DamageSource s,float n){return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putLong("Circle",center.asLong());t.putDouble("Angle",angle);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);center=BlockPos.of(t.getLong("Circle"));angle=t.getDouble("Angle");}
}
