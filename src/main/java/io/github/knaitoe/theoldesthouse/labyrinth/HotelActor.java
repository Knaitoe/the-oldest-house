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
    public static final int IDLE=0,GREET=1,POUR=2,LISTEN=3;
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DANCING=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GESTURE=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.LONG),UNTIL=SynchedEntityData.defineId(HotelActor.class,EntityDataSerializers.LONG);
    private int danceSlot=-1;
    public void danceSlot(int slot){danceSlot=Math.max(-1,Math.min(5,slot));}
    public int danceSlot(){return danceSlot;}
    public static net.minecraft.world.phys.Vec3 dancePosition(net.minecraft.core.BlockPos b,int slot,long time){double[][] centres={{18.2,-25},{23.2,-21},{18.2,-16}};int pair=Math.max(0,Math.min(2,slot/2));double angle=time*.008+pair*2+(slot%2)*Math.PI;return new net.minecraft.world.phys.Vec3(b.getX()+centres[pair][0]+.62*Math.cos(angle),b.getY(),b.getZ()+centres[pair][1]+.62*Math.sin(angle));}
    @Override public void tick(){super.tick();if(level().isClientSide||tickCount%5!=0||getServer()==null)return;var b=IndianLakeRooms.base(getServer(),LabyrinthPlace.HOTEL);if(b==null)return;
        if(dancing()&&danceSlot>=0&&Math.abs(getY()-b.getY())<2){var target=dancePosition(b,danceSlot,level().getGameTime());var delta=target.subtract(position()).multiply(1,0,1);double length=delta.length();if(length>.002)move(MoverType.SELF,delta.scale(Math.min(.08,length)/length));var partner=dancePosition(b,danceSlot^1,level().getGameTime()).subtract(position());float yaw=(float)(Math.atan2(-partner.x,partner.z)*180/Math.PI);setYRot(yaw);yBodyRot=yaw;yHeadRot=yaw;}
        else if(role()==3){var target=new net.minecraft.world.phys.Vec3(b.getX()+20.5,getY(),b.getZ()-30.5);var delta=target.subtract(position());double length=delta.length();if(length>.002&&length<4)move(MoverType.SELF,delta.scale(Math.min(.05,length)/length));}
    }
    public HotelActor(EntityType<? extends HotelActor> type,Level l){super(type,l);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.2);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(DANCING,false);b.define(GESTURE,IDLE);b.define(START,0L);b.define(UNTIL,0L);}
    public int role(){return entityData.get(ROLE);}public boolean dancing(){return entityData.get(DANCING);}
    public int gesture(){return level().getGameTime()<entityData.get(UNTIL)?entityData.get(GESTURE):IDLE;}
    public float gestureProgress(float partial){long duration=entityData.get(UNTIL)-entityData.get(START);return duration<=0?1:net.minecraft.util.Mth.clamp((level().getGameTime()-entityData.get(START)+partial)/duration,0,1);}
    /** Native interaction starts a bounded motion on the existing saved body. */
    public void gesture(int motion,int duration){if(level().isClientSide)return;long now=level().getGameTime();entityData.set(GESTURE,Math.max(IDLE,Math.min(LISTEN,motion)));entityData.set(START,now);entityData.set(UNTIL,now+Math.max(1,Math.min(100,duration)));}
    public void appearance(int role,boolean dance){entityData.set(ROLE,Math.max(0,Math.min(5,role)));entityData.set(DANCING,dance);}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HotelVignette.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Role",role());t.putBoolean("Dancing",dancing());t.putInt("StanleyGesture",entityData.get(GESTURE));t.putLong("StanleyStart",entityData.get(START));t.putLong("StanleyUntil",entityData.get(UNTIL));t.putInt("StanleyDanceSlot",danceSlot);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);appearance(t.getInt("Role"),t.getBoolean("Dancing"));danceSlot=t.contains("StanleyDanceSlot")?Math.max(-1,Math.min(5,t.getInt("StanleyDanceSlot"))):-1;long now=level().getGameTime(),start=t.getLong("StanleyStart"),until=t.getLong("StanleyUntil");if(start<=now&&until>now&&until-start<=100){entityData.set(GESTURE,Math.max(IDLE,Math.min(LISTEN,t.getInt("StanleyGesture"))));entityData.set(START,start);entityData.set(UNTIL,until);}}
}
