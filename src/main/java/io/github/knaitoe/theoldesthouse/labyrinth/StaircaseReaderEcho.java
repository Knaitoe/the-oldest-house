package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The waiting reader's visible body on the tread; it contains no private page text. */
public final class StaircaseReaderEcho extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(StaircaseReaderEcho.class,EntityDataSerializers.OPTIONAL_UUID);
    public StaircaseReaderEcho(EntityType<? extends StaircaseReaderEcho> t,Level l){super(t,l);setNoAi(true);setPersistenceRequired();setCanPickUpLoot(false);setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(StaircaseLeakRegistry.SHEET.get()));setDropChance(EquipmentSlot.MAINHAND,0);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OWNER,Optional.empty());}
    public Optional<UUID> owner(){return entityData.get(OWNER);}
    public void reader(ServerPlayer p){entityData.set(OWNER,Optional.of(p.getUUID()));setCustomName(p.getName());setCustomNameVisible(false);}
    @Override public boolean isPushable(){return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public boolean hurt(DamageSource source,float amount){
        if(level() instanceof net.minecraft.server.level.ServerLevel l&&owner().isPresent()){
            var p=l.getServer().getPlayerList().getPlayer(owner().get());if(p!=null&&StaircaseLeaks.active(p)){StaircaseLeaks.returnNow(p);return p.hurt(source,amount);}
        }return false;
    }
    @Override public void tick(){super.tick();if(level() instanceof net.minecraft.server.level.ServerLevel l&&owner().isPresent()&&!StaircaseLeaks.active(l.getServer(),owner().get()))discard();}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);owner().ifPresent(id->t.putUUID("Reader",id));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(OWNER,t.hasUUID("Reader")?Optional.of(t.getUUID("Reader")):Optional.empty());}
}
