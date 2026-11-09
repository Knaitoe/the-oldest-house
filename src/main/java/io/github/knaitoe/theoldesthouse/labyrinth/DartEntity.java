package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 0.4.415: a thrown dart. It flies with a little drop and sticks where it lands. Landing in a board's face scores there; landing
 * anywhere else, or striking someone, or flying on too long, is a miss. The board pulls it out at the end of the turn
 * ({@link DartBoards}). Nobody picks it up, it does no harm, and it is not kept when the world is saved.
 */
public final class DartEntity extends AbstractArrow {
    static final EntityDataAccessor<Integer> SEAT=SynchedEntityData.defineId(DartEntity.class,EntityDataSerializers.INT);
    /** How long a dart may fly before it counts as a miss. */
    static final int FLIGHT=60;
    @Nullable BlockPos board;long serial;boolean landed;

    public DartEntity(EntityType<? extends DartEntity> type,Level level){super(type,level);pickup=Pickup.DISALLOWED;setBaseDamage(0);}
    DartEntity(Level level,@Nullable LivingEntity thrower,double x,double y,double z,BlockPos board,long serial,int seat){
        this(BarRegistry.DART_ENTITY.get(),level);
        setPos(x,y,z);if(thrower!=null)setOwner(thrower);
        this.board=board.immutable();this.serial=serial;entityData.set(SEAT,seat);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SEAT,0);}
    /** The seat that threw it, for the flights' colour. */
    public int seat(){return entityData.get(SEAT);}
    public boolean stuck(){return inGround;}
    @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(BarRegistry.DART.get());}
    @Override protected double getDefaultGravity(){return .02;}
    @Override protected SoundEvent getDefaultHitGroundSoundEvent(){return BarRegistry.DARTS_WALL.get();}

    @Override public void tick(){
        super.tick();
        if(level().isClientSide)return;
        if(board==null||!DartBoards.knows(this)){discard();return;}
        if(!landed&&!inGround&&tickCount>FLIGHT)land(DartsGame.Hit.MISS);
    }
    @Override protected void onHitBlock(BlockHitResult r){
        boolean onBoard=false;DartsGame.Hit hit=DartsGame.Hit.MISS;
        var s=level().getBlockState(r.getBlockPos());
        if(DartboardBlock.is(s)&&r.getDirection()==s.getValue(DartboardBlock.FACING)){
            onBoard=true;hit=DartboardBlock.hit(r.getBlockPos(),s.getValue(DartboardBlock.FACING),r.getLocation());
        }
        setSoundEvent(onBoard&&hit.ring()!=DartsGame.Ring.MISS?BarRegistry.DARTS_BOARD.get():BarRegistry.DARTS_WALL.get());
        super.onHitBlock(r);
        if(!level().isClientSide&&!landed)land(hit);
    }
    @Override protected void onHitEntity(EntityHitResult r){
        // A dart that strikes someone drops away harmlessly and counts for nothing.
        setDeltaMovement(getDeltaMovement().scale(-.1));
        if(!level().isClientSide&&!landed)land(DartsGame.Hit.MISS);
    }
    void land(DartsGame.Hit hit){landed=true;DartBoards.landed(this,hit);}
    @Override public boolean shouldBeSaved(){return false;}
}
