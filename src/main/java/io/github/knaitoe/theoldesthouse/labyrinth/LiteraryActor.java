package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
/** Role is appearance; ownership and scene attribution remain server facts. */
public final class LiteraryActor extends PathfinderMob {
    public static final int MOTHER=0,DANCER=1,RED_FIGURE=2,COFFIN_WOMAN=3,LADY=4,SILHOUETTE=5,BROTHER=6,FATHER=7,VISITOR=8,KILLER=9,FAMILY_FATHER=10,FAMILY_MOTHER=11,FAMILY_CHILD=12,OLD_MAN=13,STRANGER=14,CAMERA=15;
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.OPTIONAL_UUID);
    private String scene="";private boolean small;
    private final CarcassHunt carcassHunt=new CarcassHunt();
    private final KillerNavigation privateHunt=new KillerNavigation();
    public LiteraryActor(EntityType<? extends LiteraryActor> type,Level l){super(type,l);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.SCALE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(PHASE,0);b.define(OWNER,Optional.empty());}
    public int role(){return entityData.get(ROLE);}public int phase(){return entityData.get(PHASE);}public Optional<UUID> owner(){return entityData.get(OWNER);}public String scene(){return scene;}
    public void appearance(int role,int phase){entityData.set(ROLE,role);entityData.set(PHASE,phase);boolean now=role==BROTHER||role==FAMILY_CHILD||role==SILHOUETTE&&phase==4;if(now!=small){small=now;getAttribute(Attributes.SCALE).setBaseValue(now?.58:1);refreshDimensions();}}
    public void bind(LabyrinthPlace p,UUID owner){scene=p.id();entityData.set(OWNER,Optional.ofNullable(owner));}
    @Override public boolean shouldRenderAtSqrDistance(double distance){return role()==SILHOUETTE&&phase()==4?distance<192D*192D:super.shouldRenderAtSqrDistance(distance);}
    public void say(String text){setCustomName(Component.literal(text));setCustomNameVisible(true);}
    @Override public void tick(){
        boolean privateKiller=!level().isClientSide&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent();
        var before=position();boolean active=!privateKiller||privateHunt.prepare(this);super.tick();
        if(privateKiller){if(active)privateHunt.tick(this,before);}
        else if(!level().isClientSide&&role()==KILLER&&(scene.equals(LabyrinthPlace.CAMP_BLOOD.id())||scene.equals(LabyrinthPlace.ELK_CARCASSES.id())))carcassHunt.tick(this);
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose){return role()==KILLER&&pose==Pose.CROUCHING?EntityDimensions.scalable(.6F,1.3F).withEyeHeight(1.05F):super.getDefaultDimensions(pose);}
    @Override public void move(MoverType type,net.minecraft.world.phys.Vec3 movement){
        if(type==MoverType.SELF&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent()&&KillerNavigation.moving(this)){
            double horizontal=Math.hypot(movement.x,movement.z);
            if(horizontal>KillerNavigation.CHASE_STEP){double scale=KillerNavigation.CHASE_STEP/horizontal;movement=new net.minecraft.world.phys.Vec3(movement.x*scale,movement.y,movement.z*scale);}
        }super.move(type,movement);
    }
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)LiteraryVignettes.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){if(source.getEntity() instanceof ServerPlayer p){if(role()==FATHER&&LiteraryVignettes.inside(p,LabyrinthPlace.HOLY_RABBIT)){if(getHealth()<=1)return false;boolean hit=super.hurt(source,Math.min(Math.min(1,amount),getHealth()-1));if(hit)LiteraryVignettes.attacked(p,this);return hit;}LiteraryVignettes.attacked(p,this);}return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Role",role());t.putInt("Phase",phase());t.putString("Scene",scene);owner().ifPresent(id->t.putUUID("Reader",id));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);scene=t.getString("Scene");entityData.set(OWNER,t.hasUUID("Reader")?Optional.of(t.getUUID("Reader")):Optional.empty());appearance(t.getInt("Role"),t.getInt("Phase"));}
}
