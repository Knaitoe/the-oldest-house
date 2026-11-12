package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 0.4.511, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Failure: caught in town"): "The bike turns up later, in a later hall, chained
 * to a school fence with its front wheel bent."
 *
 * <p>Two bays of a school yard's fence standing against a hall's wall, and the bike one of the three took, chained to it through its
 * frame, its front wheel buckled. It is one reader's: only they are sent it, so it is in nobody else's hall. It stands in nothing's
 * way (nothing collides with it), it is never saved, and it goes when its reader has been gone from it a while. Coming near it and
 * looking at it, or a hand on it, tells them what it is (BikeNightHalls.seen).
 *
 * <p>It faces out from the wall it stands against: its yaw is that way.
 */
public final class ChainedBike extends Entity {
    private static final EntityDataAccessor<Integer> MAKE=SynchedEntityData.defineId(ChainedBike.class,EntityDataSerializers.INT);
    /** In blocks: how near its reader comes before they are told, and how far off they are gone from it; in ticks, how long gone before it goes. */
    public static final double NEAR=5,FAR=40;public static final int GONE=200;
    private @Nullable UUID owner;private int away;

    public ChainedBike(EntityType<? extends ChainedBike> type,Level level){super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(MAKE,0);}
    public NightBike.Make make(){var all=NightBike.Make.values();return all[Math.max(0,Math.min(all.length-1,entityData.get(MAKE)))];}
    public void make(NightBike.Make make){entityData.set(MAKE,make.ordinal());}
    public void owner(UUID id){owner=id;}
    public boolean ownedBy(UUID id){return id.equals(owner);}

    @Override public void tick(){
        super.tick();
        if(!(level() instanceof ServerLevel l))return;
        var reader=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);
        boolean here=reader!=null&&reader.level()==l&&reader.isAlive()&&reader.distanceToSqr(this)<FAR*FAR;
        away=here?0:away+1;
        if(away>GONE){discard();return;}
        if(!here||tickCount%5!=0||reader.isSpectator())return;
        var to=position().add(0,.6,0).subtract(reader.getEyePosition());
        if(to.lengthSqr()<NEAR*NEAR&&to.normalize().dot(reader.getLookAngle())>.6)BikeNightHalls.seen(reader,this);
    }
    /** Only its reader is sent it. */
    @Override public boolean broadcastToPlayer(ServerPlayer p){return owner!=null&&owner.equals(p.getUUID())&&!p.isSpectator()&&super.broadcastToPlayer(p);}
    @Override public boolean isPickable(){return !isRemoved();}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean canBeHitByProjectile(){return false;}
    @Override public boolean isAttackable(){return false;}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(hand==InteractionHand.MAIN_HAND&&p instanceof ServerPlayer reader&&ownedBy(reader.getUUID()))BikeNightHalls.hand(reader,this);
        return InteractionResult.CONSUME;
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
}
