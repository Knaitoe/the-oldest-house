package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.411: a prize set down in the world: the rubber spider or the plush dog sitting on a floor or a table, or a glow star stuck to a
 * face of a block. Anyone may pick it up again with a hand, or knock it loose. It stays where it was put, and drops as itself when what
 * holds it is taken away. The spider jumps and squeaks at anyone who comes within arm's reach of it, at most once in five seconds.
 */
public class PrizeProp extends Entity {
    private static final EntityDataAccessor<Byte> KIND=SynchedEntityData.defineId(PrizeProp.class,EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FACE=SynchedEntityData.defineId(PrizeProp.class,EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> STARTLED=SynchedEntityData.defineId(PrizeProp.class,EntityDataSerializers.INT);
    /** How near someone must come to make the spider jump, and how long before it will again. */
    public static final double REACH=1.6;
    public static final int REST=100;
    public PrizeProp(EntityType<? extends PrizeProp> type,Level level){super(type,level);}

    /** Sets a prize down against a block's face at the point the player chose. */
    public static PrizeProp place(Level level,PolybiusPrize.Kind kind,BlockPos support,Direction face,float yaw,Vec3 click){
        var prop=new PrizeProp(PolybiusRegistry.PROP.get(),level);
        prop.entityData.set(KIND,(byte)kind.ordinal());prop.entityData.set(FACE,(byte)face.get3DDataValue());
        Vec3 at;
        if(face==Direction.UP)at=new Vec3(kind==PolybiusPrize.Kind.GLOW_STAR?click.x:support.getX()+.5,click.y+.001,kind==PolybiusPrize.Kind.GLOW_STAR?click.z:support.getZ()+.5);
        else if(face==Direction.DOWN)at=new Vec3(click.x,click.y-.02-prop.getBbHeight(),click.z);
        else at=new Vec3(click.x+face.getStepX()*.02,click.y-prop.getBbHeight()/2,click.z+face.getStepZ()*.02);
        prop.moveTo(at.x,at.y,at.z,face.getAxis().isHorizontal()?face.toYRot():yaw,0F);
        level.addFreshEntity(prop);
        return prop;
    }

    public PolybiusPrize.Kind kind(){return PolybiusPrize.Kind.values()[Mth.clamp(entityData.get(KIND),0,PolybiusPrize.Kind.values().length-1)];}
    public Direction face(){return Direction.from3DDataValue(entityData.get(FACE));}
    /** The tick the spider last jumped, by this entity's own count. */
    public int startled(){return entityData.get(STARTLED);}
    public ItemStack stack(){return new ItemStack(PolybiusRegistry.PRIZES.get(kind().ordinal()).get());}

    /** On a client: the tick, by this client's count, the spider last jumped. */
    public int jumpedAt=-1000;
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,(byte)0);b.define(FACE,(byte)1);b.define(STARTLED,-1000);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(STARTLED.equals(key)&&level().isClientSide&&startled()>=0)jumpedAt=tickCount;}

    @Override public void tick(){
        super.tick();
        if(level().isClientSide)return;
        if(tickCount%10==0&&!supported()){drop();return;}
        if(kind()==PolybiusPrize.Kind.SPIDER&&tickCount-startled()>REST){
            var near=level().getNearestPlayer(getX(),getY(),getZ(),REACH,p->!p.isSpectator());
            if(near!=null){entityData.set(STARTLED,tickCount);level().playSound(null,getX(),getY(),getZ(),PolybiusRegistry.SQUEAK.get(),SoundSource.NEUTRAL,.7F,1.5F);}
        }
    }
    /** The block it was set against: below it on a floor, above on a ceiling, behind it on a wall. */
    public BlockPos support(){
        var face=face();
        if(face==Direction.UP)return BlockPos.containing(getX(),getY()-.05,getZ());
        if(face==Direction.DOWN)return BlockPos.containing(getX(),getY()+getBbHeight()+.05,getZ());
        return BlockPos.containing(getX()-face.getStepX()*.1,getY()+getBbHeight()/2,getZ()-face.getStepZ()*.1);
    }
    private boolean supported(){var at=support();var state=level().getBlockState(at);return !state.getCollisionShape(level(),at).isEmpty();}

    private void drop(){if(isRemoved())return;spawnAtLocation(stack());discard();}

    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(isRemoved())return InteractionResult.PASS;
        var stack=stack();
        if(!p.getInventory().add(stack))spawnAtLocation(stack);
        level().playSound(null,getX(),getY(),getZ(),kind()==PolybiusPrize.Kind.GLOW_STAR?SoundEvents.ITEM_FRAME_REMOVE_ITEM:PolybiusRegistry.SQUEAK.get(),SoundSource.PLAYERS,.5F,1.1F);
        discard();
        return InteractionResult.CONSUME;
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(isInvulnerableTo(source))return false;
        if(!level().isClientSide)drop();
        return true;
    }
    @Override public boolean isPickable(){return true;}
    @Override public boolean isAttackable(){return true;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public ItemStack getPickResult(){return stack();}

    @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(KIND,t.getByte("Kind"));entityData.set(FACE,t.getByte("Face"));}
    @Override protected void addAdditionalSaveData(CompoundTag t){t.putByte("Kind",entityData.get(KIND));t.putByte("Face",entityData.get(FACE));}
}
