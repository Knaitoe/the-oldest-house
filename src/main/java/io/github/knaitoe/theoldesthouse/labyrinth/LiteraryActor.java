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
    public static final int GHOST_MARJORIE=16,GHOST_MERRY=17,GHOST_JOHN=18,GHOST_SARAH=19,GHOST_PRODUCER=20,GHOST_PRIEST=21,GHOST_PLAYHOUSE=22,GHOST_DRAWER=23;
    public static final int LAKE_TESS=24,LAKE_WAX=25;
    public static final int BORRASCA_KYLE=26,BORRASCA_KIMBER=27,BORRASCA_SHERIFF=28,BORRASCA_PRESCOTT=29;
    /** Mr. Harrigan (0.4.105): phase 0 alive in his chair, 1 dead in it, 2 laid in his casket; and his three mourners, phases 0 to 2. */
    public static final int HARRIGAN=30,HARRIGAN_MOURNER=31;
    public static final int UNCANNY_PROPPED=32,UNCANNY_WAITRESS=33,UNCANNY_FALSE_WAITRESS=34,UNCANNY_GIANT=35,UNCANNY_MARIONETTE=36,UNCANNY_PEELER=37,UNCANNY_VISITOR=38;
    public static final int READ_MAN=39,READ_WOMAN=40;
    /** 0.4.152: the young man in Tess's place at the lake house, for a reader the House reads as drawn to men. */
    public static final int LAKE_THEO=41;
    /** 0.4.173: the two kids under bedsheets outside the third-floor girls' bathroom; phase 0 waiting, 1 giggling, 2 silent. */
    public static final int DARE_KID_A=42,DARE_KID_B=43;
    public static boolean dareKid(int role){return role==DARE_KID_A||role==DARE_KID_B;}
    /** The lake house partner, whichever of the two they are. */
    public static boolean lakePartner(int role){return role==LAKE_TESS||role==LAKE_THEO;}
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GESTURE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.OPTIONAL_UUID);
    private String scene="";private boolean small;
    private final CarcassHunt carcassHunt=new CarcassHunt();
    private final KillerNavigation privateHunt=new KillerNavigation();
    public LiteraryActor(EntityType<? extends LiteraryActor> type,Level l){super(type,l);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.SCALE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(PHASE,0);b.define(GESTURE,0);b.define(OWNER,Optional.empty());}
    public int gesture(){return entityData.get(GESTURE);}public void gesture(int value){entityData.set(GESTURE,Math.max(0,Math.min(5,value)));}
    @Override public net.minecraft.world.phys.Vec3 getVehicleAttachmentPoint(Entity vehicle){if(lakePartner(role())&&vehicle instanceof LakeHouseCanoe)return net.minecraft.world.entity.player.Player.DEFAULT_VEHICLE_ATTACHMENT;if(vehicle instanceof LakeCanoeEntity&&MovieNightCast.teen(this))return net.minecraft.world.entity.player.Player.DEFAULT_VEHICLE_ATTACHMENT;return super.getVehicleAttachmentPoint(vehicle);}
    @Override protected net.minecraft.world.phys.Vec3 getPassengerAttachmentPoint(Entity passenger,EntityDimensions dimensions,float partial){if(role()==FATHER&&passenger instanceof net.minecraft.world.entity.animal.Rabbit)return new net.minecraft.world.phys.Vec3(0,.8,.4).yRot(-getYRot()*(float)Math.PI/180);return super.getPassengerAttachmentPoint(passenger,dimensions,partial);}
    public int role(){return entityData.get(ROLE);}public int phase(){return entityData.get(PHASE);}public Optional<UUID> owner(){return entityData.get(OWNER);}public String scene(){return scene;}
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps){
        // A late pairing can first spawn the unseen woman at the tracker's old far-end
        // position. Its absolute correction must enter the loaded client section now:
        // an entity in an unloaded section never ticks to consume an interpolation target.
        // Short, visible pursuit steps retain ordinary native interpolation.
        if(level().isClientSide&&role()==UNCANNY_GIANT&&distanceToSqr(x,y,z)>64&&level().getChunkSource().getChunk(blockPosition().getX()>>4,blockPosition().getZ()>>4,net.minecraft.world.level.chunk.status.ChunkStatus.FULL,false)==null)moveTo(x,y,z,yaw,pitch);
        if(level().isClientSide&&UncannyStories.role(role())&&role()!=UNCANNY_WAITRESS&&role()!=UNCANNY_GIANT&&distanceToSqr(x,y,z)<16)steps=Math.max(steps,5);
        super.lerpTo(x,y,z,yaw,pitch,steps);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==ROLE||key==PHASE||key==GESTURE)refreshDimensions();}
    public void appearance(int role,int phase){boolean changed=role()!=role||phase()!=phase;entityData.set(ROLE,role);entityData.set(PHASE,phase);boolean now=role==BROTHER||role==FAMILY_CHILD||role==GHOST_MERRY||role==SILHOUETTE&&phase==4||role==BORRASCA_KIMBER||role==STRANGER&&(phase==MovieNightCast.JAMIE_PHASE||CostumeNight.child(phase));double size=dareKid(role)?.82:role==UNCANNY_GIANT?2.5:role==GHOST_MARJORIE?.86:role==BORRASCA_KIMBER?.54:now?.58:role==STRANGER&&WinterLake.boy(phase)?.8:1;if(now!=small||getAttribute(Attributes.SCALE).getBaseValue()!=size){small=now;getAttribute(Attributes.SCALE).setBaseValue(size);changed=true;}if(role==UNCANNY_FALSE_WAITRESS){noPhysics=phase<2;setNoGravity(phase<2);}if(changed)refreshDimensions();}
    public void bind(LabyrinthPlace p,UUID owner){scene=p.id();entityData.set(OWNER,Optional.ofNullable(owner));}
    @Override public void startSeenByPlayer(ServerPlayer p){
        super.startSeenByPlayer(p);
        // Native pairing caches its spawn position before the unseen appearance can move this
        // original. Reconcile after the spawn bundle, including a fresh observer after reconnect.
        if(role()==UNCANNY_GIANT&&owner().filter(p.getUUID()::equals).isPresent())p.connection.send(new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(this));
    }
    @Override public boolean shouldRenderAtSqrDistance(double distance){return role()==SILHOUETTE&&phase()==4?distance<192D*192D:super.shouldRenderAtSqrDistance(distance);}
    public void say(String text){setCustomName(Component.literal(text));setCustomNameVisible(true);}
    @Override public void tick(){
        boolean privateKiller=!level().isClientSide&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent();
        var before=position();boolean active=!privateKiller||privateHunt.prepare(this);if(!level().isClientSide)BorrascaStory.actorTick(this);super.tick();
        if(!level().isClientSide){DomesticGlimpses.actorTick(this);GhostsMotion.tick(this);LiteraryMotion.tick(this);DinerWalk.tick(this);TunnelStride.tick(this);HouseReadShelter.tick(this);}
        if(lakePartner(role()))setAirSupply(getMaxAirSupply());
        if(privateKiller){if(active)privateHunt.tick(this,before);}
        else if(!level().isClientSide&&role()==KILLER&&(scene.equals(LabyrinthPlace.CAMP_BLOOD.id())||scene.equals(LabyrinthPlace.ELK_CARCASSES.id())))carcassHunt.tick(this);
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose){if(role()==UNCANNY_GIANT)return EntityDimensions.scalable(.32F,1.1F).withEyeHeight(.9F);if(role()==HARRIGAN)return phase()==HarriganVignette.CASKET_PHASE?EntityDimensions.scalable(.6F,.5F).withEyeHeight(.3F):EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(gesture()==CostumeNight.CRAWL)return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(lakePartner(role())&&(phase()==2||phase()==6))return EntityDimensions.scalable(.6F,.6F).withEyeHeight(.35F);if(lakePartner(role())&&phase()==1)return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(role()==FATHER&&phase()==3)return EntityDimensions.scalable(.6F,1.15F).withEyeHeight(.9F);if(role()==GHOST_PLAYHOUSE)return EntityDimensions.scalable(1F,phase()>=2?.2F:1F).withEyeHeight(.4F);if(role()==GHOST_DRAWER)return EntityDimensions.scalable(.9F,.9F).withEyeHeight(.5F);if(role()>=GHOST_MARJORIE&&role()<=GHOST_PRIEST){if(phase()==GhostsSet.CRAWL)return EntityDimensions.scalable(.55F,.65F).withEyeHeight(.38F);if(GhostsSet.seatedPose(role(),phase()))return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(phase()==GhostsSet.BED)return EntityDimensions.scalable(.6F,.7F).withEyeHeight(.4F);}return role()==KILLER&&pose==Pose.CROUCHING?EntityDimensions.scalable(.6F,1.3F).withEyeHeight(1.05F):super.getDefaultDimensions(pose);}
    @Override public void travel(net.minecraft.world.phys.Vec3 input){if(!level().isClientSide&&scene.equals(LabyrinthPlace.BORRASCA.id()))input=BorrascaCast.input(this);super.travel(input);}
    @Override public void move(MoverType type,net.minecraft.world.phys.Vec3 movement){
        if(type==MoverType.SELF&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent()&&KillerNavigation.moving(this)){
            double horizontal=Math.hypot(movement.x,movement.z);
            if(horizontal>KillerNavigation.CHASE_STEP){double scale=KillerNavigation.CHASE_STEP/horizontal;movement=new net.minecraft.world.phys.Vec3(movement.x*scale,movement.y,movement.z*scale);}
        }super.move(type,movement);
    }
    /** A PathfinderMob never advances its swing on its own; the cast's blows and chops are seen only if it does (0.4.120). */
    @Override public void aiStep(){updateSwingTime();super.aiStep();}
    @Override public boolean isPickable(){return !(role()==UNCANNY_FALSE_WAITRESS&&phase()<2)&&super.isPickable();}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(HouseReadShelter.role(role())){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HouseReadShelter.take(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()==HARRIGAN||role()==HARRIGAN_MOURNER){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HarriganVignette.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()>=GHOST_MARJORIE&&role()<=GHOST_DRAWER&&p.getItemInHand(hand).getItem() instanceof CamcorderItem){if(p instanceof ServerPlayer s)CamcorderItem.toggle(s,p.getItemInHand(hand));p.startUsingItem(hand);return InteractionResult.CONSUME;}if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)LiteraryVignettes.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){if(source.getEntity() instanceof ServerPlayer p){if(role()==FATHER&&LiteraryVignettes.inside(p,LabyrinthPlace.HOLY_RABBIT)){if(getHealth()<=1)return false;boolean hit=super.hurt(source,Math.min(Math.min(1,amount),getHealth()-1));if(hit)LiteraryVignettes.attacked(p,this);return hit;}LiteraryVignettes.attacked(p,this);}return false;}
    @Override public boolean broadcastToPlayer(ServerPlayer p){
        // Match the renderer's privacy rule at native tracking too. A hidden peer's
        // overlapping cast must not remain on this client and intercept its entity pick.
        boolean sharedCopy=scene.equals(LabyrinthPlace.FAMILY_COPY.id())||scene.equals(LabyrinthPlace.OLD_CABIN.id());
        // 0.4.156: the lake house's figure is also seen by the reader who goes down in Tess's place with its reader.
        if(owner().isPresent()&&!sharedCopy&&!owner().get().equals(p.getUUID())&&!(role()==LAKE_WAX&&LakeHousePairs.sharesWith(p,owner().get())))return false;
        // 0.4.147: a thread's cast is seen by that thread's members alone.
        if(getPersistentData().contains(VignetteThreads.ACTOR_TAG)&&!VignetteThreads.sees(p,scene,getPersistentData().getString(VignetteThreads.ACTOR_TAG)))return false;
        // 0.4.183: Usher's Roderick is not there for a reader who has seen the house fall, and Madeline at the doors is that night's alone.
        if(scene.equals(LabyrinthPlace.USHER.id())&&!UsherStory.sees(p,this))return false;
        if(scene.equals(ElkLot.PLACE.id())&&role()==STRANGER&&phase()>=5&&phase()<=8){var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),ElkLot.PLACE);if(own.getBoolean("Ready")||own.getBoolean("Completed"))return false;}
        if((lakePartner(role())||role()==LAKE_WAX)&&owner().isPresent()&&p.isSpectator())return false;
        return super.broadcastToPlayer(p);
    }
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public boolean isPushable(){return role()==UNCANNY_FALSE_WAITRESS&&phase()>=2;}
    @Override public boolean canBeCollidedWith(){if(role()==UNCANNY_FALSE_WAITRESS)return phase()>=2;return super.canBeCollidedWith();}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Role",role());t.putInt("Phase",phase());t.putInt("Gesture0491",gesture());t.putString("Scene",scene);owner().ifPresent(id->t.putUUID("Reader",id));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);scene=t.getString("Scene");entityData.set(OWNER,t.hasUUID("Reader")?Optional.of(t.getUUID("Reader")):Optional.empty());appearance(t.getInt("Role"),t.getInt("Phase"));gesture(t.getInt("Gesture0491"));}
}
