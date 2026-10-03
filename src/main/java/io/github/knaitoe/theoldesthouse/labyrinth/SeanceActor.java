package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;

/** Four persistent people. They never target, track or look at an invisible explorer. */
public final class SeanceActor extends PathfinderMob {
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(SeanceActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SEATED=SynchedEntityData.defineId(SeanceActor.class,EntityDataSerializers.BOOLEAN),AFRAID=SynchedEntityData.defineId(SeanceActor.class,EntityDataSerializers.BOOLEAN);
    public SeanceActor(EntityType<? extends SeanceActor> type,Level level){super(type,level);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.FOLLOW_RANGE,8);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ROLE,0);builder.define(SEATED,true);builder.define(AFRAID,false);}
    public int role(){return entityData.get(ROLE);}
    public boolean seated(){return entityData.get(SEATED);}
    public boolean afraid(){return entityData.get(AFRAID);}
    public void appearance(int role,boolean seated,boolean afraid){entityData.set(ROLE,Math.max(0,Math.min(3,role)));entityData.set(SEATED,seated);entityData.set(AFRAID,afraid);}
    public void say(String words){
        if(!(level() instanceof ServerLevel level))return;
        level.getEntitiesOfClass(Display.TextDisplay.class,getBoundingBox().inflate(12),e->e.getTags().contains("SeanceSpeech")&&e.getPersistentData().hasUUID("Speaker")&&e.getPersistentData().getUUID("Speaker").equals(getUUID())).forEach(Entity::discard);
        var text=EntityType.TEXT_DISPLAY.create(level);if(text==null)return;var tag=new CompoundTag();text.saveWithoutId(tag);
        tag.putString("text",Component.Serializer.toJson(Component.literal(words),level.registryAccess()));tag.putString("billboard","center");tag.putInt("line_width",170);tag.putInt("background",0x77000000);tag.putBoolean("see_through",false);text.load(tag);
        text.moveTo(getX(),getY()+(role()==3?1.55:2.15),getZ());text.addTag("SeanceSpeech");text.getPersistentData().putUUID("Speaker",getUUID());text.getPersistentData().putLong("Until",level.getGameTime()+130);level.addFreshEntity(text);
    }
    @Override public boolean hurt(DamageSource source,float amount){if(source.getEntity() instanceof ServerPlayer p)ClassicsVignettes.attacked(p,this);return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Role",role());tag.putBoolean("Seated",seated());tag.putBoolean("Afraid",afraid());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);appearance(tag.getInt("Role"),tag.getBoolean("Seated"),tag.getBoolean("Afraid"));}
}
