package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
/** Porter, bartender, scarred guest, orchestra and paired dancers retain their native identities. */
public final class HotelActor extends PathfinderMob {
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DANCING=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.BOOLEAN);
    public HotelActor(EntityType<? extends HotelActor> type,Level l){super(type,l);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.2);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(DANCING,false);}
    public int role(){return entityData.get(ROLE);}public boolean dancing(){return entityData.get(DANCING);}
    public void appearance(int role,boolean dance){entityData.set(ROLE,Math.max(0,Math.min(5,role)));entityData.set(DANCING,dance);}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HotelVignette.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Role",role());t.putBoolean("Dancing",dancing());}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);appearance(t.getInt("Role"),t.getBoolean("Dancing"));}
}
