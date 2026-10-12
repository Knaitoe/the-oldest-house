package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/** Each saved body keeps dancing; interaction never replaces or pauses it. */
public final class NightclubActor extends PathfinderMob {
    private static final EntityDataAccessor<Integer> SLOT=SynchedEntityData.defineId(NightclubActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DESPAIR=SynchedEntityData.defineId(NightclubActor.class,EntityDataSerializers.INT);
    private float previousDespair;
    private BlockPos base=BlockPos.ZERO;
    public NightclubActor(EntityType<? extends NightclubActor> type,Level l){super(type,l);setNoAi(true);setPersistenceRequired();setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.2);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SLOT,0);b.define(DESPAIR,0);}
    public int slot(){return entityData.get(SLOT);}public boolean dj(){return slot()==Nightclub.DANCERS;}public int style(){return slot()%2;}
    public static int look(int slot){return slot==Nightclub.DANCERS?8:Math.floorMod(slot,8);}
    public int look(){return look(slot());}
    public float despair(){return entityData.get(DESPAIR)/20F;}
    public float despair(float partial){return net.minecraft.util.Mth.lerp(partial,previousDespair,despair());}
    public void bind(BlockPos base,int slot){this.base=base.immutable();entityData.set(SLOT,Math.max(0,Math.min(Nightclub.DANCERS,slot)));}
    public static Vec3 home(BlockPos b,int slot){if(slot==Nightclub.DANCERS)return new Vec3(b.getX()+.5,b.getY()+1,b.getZ()-44.5);int col=slot%6,row=slot/6;return new Vec3(b.getX()-8.5+col*3.5,b.getY(),b.getZ()-18.5-row*7);}
    @Override public void tick(){previousDespair=despair();super.tick();if(level().isClientSide||tickCount%2!=0)return;
        var mixer=level().getBlockState(base.offset(Nightclub.MIXER));
        if(mixer.is(NightclubRegistry.BLOCK.get()))entityData.set(DESPAIR,mixer.getValue(NightclubBlock.STAGE)==0?Math.min(20,entityData.get(DESPAIR)+4):0);
        double time=level().getGameTime(),seed=slot()*1.73;var anchor=home(base,slot());
        if(!dj()){var target=anchor.add(Math.sin(time*.06+seed)*.28,0,Math.cos(time*.047+seed)*.3);var delta=target.subtract(position()).multiply(1,0,1);double length=delta.length();if(length>.002)move(MoverType.SELF,delta.scale(Math.min(.055,length)/length));setYRot((float)(slot()*37+Math.sin(time*.024+seed)*28));}
        else setYRot(0);yBodyRot=getYRot();yHeadRot=getYRot();
    }
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){
        if(p instanceof ServerPlayer reader&&hand==InteractionHand.MAIN_HAND&&Nightclub.inside(reader)&&reader.isAlive()&&!reader.isSpectator()){
            long now=level().getGameTime();String key="ClubReply0488";if(now-reader.getPersistentData().getLong(key)>80){reader.getPersistentData().putLong(key,now);reader.displayClientMessage(Component.literal(despair()>.5F?"Their mouth opens. Nothing comes out. Their feet keep moving.":dj()?"The DJ keeps one hand on the controls. A finger lifts, then drops with the beat.":switch(slot()%3){case 0->"\"One more.\" Their feet keep moving.";case 1->"They lean close, say something the bass takes away, and turn back.";default->"Their eyes find yours for a moment. They keep dancing.";}),false);}
        }return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("ClubSlot",slot());t.putLong("ClubBase",base.asLong());t.putInt("ClubDespair",entityData.get(DESPAIR));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);bind(BlockPos.of(t.getLong("ClubBase")),t.getInt("ClubSlot"));entityData.set(DESPAIR,Math.max(0,Math.min(20,t.getInt("ClubDespair"))));previousDespair=despair();}
}
