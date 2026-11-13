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
public final class LiteraryActor extends PathfinderMob implements Voiced {
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
    /** The split-feed bedroom's parents (0.4.201): asleep in their beds, or dead in them; drawn in the reader's own body. */
    public static final int UNCANNY_PARENT=44;
    /** 0.4.202, the Whistlers in the Pines: the thing in the woods (phase is its tier, 0 to 3); and the figures in the eight towers round tower four, drawn in the reader's own body (phase 1 crouching; gesture 1 binoculars up). */
    public static final int WHISTLER=45,LOOKOUT_DOUBLE=46;
    /** 0.4.274, Second Person: the resident, drawn in the reader's own House body, coat and hair; never sees the reader, never pushes or is pushed (gesture is their pose, SecondPersonRenderers). */
    public static final int SP_RESIDENT=47;
    /** 0.4.310, A Deer in the Road: the woman from the tunnel, on the county road; phase is DeerRoad's (out of the doe, standing, striding, fallen, dead at the door or on the road, out of the bench, held in the lights). Shared, drawn with the tunnel's bent body. */
    public static final int ROAD_WOMAN=48;
    public static boolean roadDead(int role,int phase){return role==ROAD_WOMAN&&(phase==DeerRoad.DEAD_DOOR||phase==DeerRoad.DEAD_ROAD||phase==DeerRoad.DEAD_DOOR_OPEN||phase==DeerRoad.HIDDEN);}
    public static boolean dareKid(int role){return role==DARE_KID_A||role==DARE_KID_B;}
    /** The lake house partner, whichever of the two they are. */
    public static boolean lakePartner(int role){return role==LAKE_TESS||role==LAKE_THEO;}
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GESTURE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    /** 0.4.309: ticks left of the line this person is speaking, so their mouth moves while their words are on screen. */
    private static final EntityDataAccessor<Integer> SPEECH=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.OPTIONAL_UUID);
    /** 0.4.228: how far the actor has faded, 0 (solid) to 100 (gone); the tenant in Vacant Possession thins as the flat empties. */
    private static final EntityDataAccessor<Integer> FADE=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    /** 0.4.429: who is saying the line up now, for its caption (empty when the scene gives no name). */
    private static final EntityDataAccessor<String> SPEAKER=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.STRING);
    /**
     * 0.4.439: how much of the body something has torn away, in thousandths of a block measured from the feet; 0 is whole. Borrasca's
     * machine takes Dana in feet first and the renderer cuts her body there, a slice at a time (BorrascaFeeding, BorrascaNativeArt).
     */
    private static final EntityDataAccessor<Integer> TORN=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    /** 0.4.535: marks a scene puts on one of its people that their look follows, a bit each (the Pell house: June's wrapped hand). */
    private static final EntityDataAccessor<Integer> MARKS=SynchedEntityData.defineId(LiteraryActor.class,EntityDataSerializers.INT);
    private String scene="";private boolean small;
    private final CarcassHunt carcassHunt=new CarcassHunt();
    private final KillerNavigation privateHunt=new KillerNavigation();
    public LiteraryActor(EntityType<? extends LiteraryActor> type,Level l){super(type,l);setPersistenceRequired();setNoAi(true);setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.SCALE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(PHASE,0);b.define(GESTURE,0);b.define(SPEECH,0);b.define(OWNER,Optional.empty());b.define(FADE,0);b.define(SPEAKER,"");b.define(TORN,0);b.define(MARKS,0);}
    public int speaking(){return entityData.get(SPEECH);}
    /** Sets them talking for about as long as the line takes to say. */
    public void silent(){entityData.set(SPEECH,0);}
    public void speaks(String line){entityData.set(SPEECH,Math.min(110,14+line.length()*2));}
    public int torn(){return entityData.get(TORN);}public void torn(int value){entityData.set(TORN,Math.max(0,Math.min(4000,value)));}
    public int marks(){return entityData.get(MARKS);}public void marks(int value){entityData.set(MARKS,value);}
    public int fade(){return entityData.get(FADE);}public void fade(int value){entityData.set(FADE,Math.max(0,Math.min(100,value)));}
    /**
     * 0.4.262: up to 15. It was held to 5, so Borrasca's led and reaching gestures (6 and 7, since 0.4.235) and the men's hands on her
     * arm (8 and 9) all arrived as 5, a stoop that also stopped the walking legs.
     */
    public int gesture(){return entityData.get(GESTURE);}public void gesture(int value){entityData.set(GESTURE,Math.max(0,Math.min(31,value)));}
    /**
     * 0.4.447 (owner, of the two in the bar's next booth lying over their table: "Lol was this intentional, in last orders?"). The
     * scenes that pose their own cast number their gestures from one, so a bare number means something different in each of them. The
     * bar's lean and the Kept's reach are both five, which is also Costume Night's crawl, and both were drawn on all fours and stood
     * as low; the bar's whistle is four, which is also the plain reach. A body whose scene keeps its own gestures takes neither of the
     * shared poses. A scene that gives its cast a gesture table of its own belongs in this list.
     */
    public boolean ownGestures(){int phase=phase();return role()==STRANGER&&(LastOrders.cast(phase)||Kept.cast(phase)||Pinehook.cast(phase)||Truant.phase(phase)||Drawing.cast(phase)||Corner.cast(phase)||BikeNightGus.cast(phase)||BikeNightBullies.cast(phase)||CalderPeople.cast(phase)||BikeNightSpecters.cast(phase)||PellPeople.cast(phase));}
    /** On all fours (Costume Night's crawl, 0.4.103), and as low as that. */
    public boolean crawling(){return gesture()==CostumeNight.CRAWL&&!ownGestures();}
    /** Both arms out in front (the plain reach). */
    public boolean reaching(){return role()==STRANGER&&gesture()==4&&!ownGestures();}
    @Override public net.minecraft.world.phys.Vec3 getVehicleAttachmentPoint(Entity vehicle){if(lakePartner(role())&&vehicle instanceof LakeHouseCanoe)return net.minecraft.world.entity.player.Player.DEFAULT_VEHICLE_ATTACHMENT;if(vehicle instanceof LakeCanoeEntity&&MovieNightCast.teen(this))return net.minecraft.world.entity.player.Player.DEFAULT_VEHICLE_ATTACHMENT;return super.getVehicleAttachmentPoint(vehicle);}
    @Override protected net.minecraft.world.phys.Vec3 getPassengerAttachmentPoint(Entity passenger,EntityDimensions dimensions,float partial){if(role()==FATHER&&passenger instanceof net.minecraft.world.entity.animal.Rabbit)return new net.minecraft.world.phys.Vec3(0,.8,.4).yRot(-getYRot()*(float)Math.PI/180);if(scene.equals(BorrascaStory.PLACE.id())&&BorrascaStory.worker(role(),phase())
            &&passenger instanceof LiteraryActor child&&child.role()==BORRASCA_KIMBER)
        return new net.minecraft.world.phys.Vec3(0,.92,.48).yRot(-getYRot()*(float)Math.PI/180);
        return super.getPassengerAttachmentPoint(passenger,dimensions,partial);}
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
    public void appearance(int role,int phase){boolean changed=role()!=role||phase()!=phase;entityData.set(ROLE,role);entityData.set(PHASE,phase);boolean now=role==BROTHER||role==FAMILY_CHILD||role==GHOST_MERRY||role==SILHOUETTE&&phase==4||role==BORRASCA_KIMBER&&phase!=BorrascaStory.GROWN||BorrascaStory.townChild(role,phase)||role==STRANGER&&(phase==MovieNightCast.JAMIE_PHASE||CostumeNight.child(phase)||ClosingHall.child(phase));double size=dareKid(role)?.82:role==UNCANNY_GIANT||role==ROAD_WOMAN?2.5:role==WHISTLER?1.12:role==GHOST_MARJORIE?.86:role==BORRASCA_KIMBER&&phase!=BorrascaStory.GROWN?.54:now?.58:role==STRANGER&&WinterLake.boy(phase)?.8:role==STRANGER&&phase==BikeNightSpecters.GOBLIN?.42:role==STRANGER&&BikeNightGus.cast(phase)?.54:role==STRANGER&&BikeNightBullies.cast(phase)?BikeNightBullies.size(phase):role==STRANGER&&CalderPeople.cast(phase)?CalderPeople.size(phase):role==STRANGER&&PellPeople.cast(phase)?PellPeople.size(phase):role==STRANGER&&Drawing.child(phase)||Corner.child(phase)?.68:1;if(now!=small||getAttribute(Attributes.SCALE).getBaseValue()!=size){small=now;getAttribute(Attributes.SCALE).setBaseValue(size);changed=true;}if(role==UNCANNY_FALSE_WAITRESS){noPhysics=phase<2;setNoGravity(phase<2);}if(changed)refreshDimensions();}
    public void bind(LabyrinthPlace p,UUID owner){scene=p.id();entityData.set(OWNER,Optional.ofNullable(owner));}
    @Override public void startSeenByPlayer(ServerPlayer p){
        super.startSeenByPlayer(p);
        // Native pairing caches its spawn position before the unseen appearance can move this
        // original. Reconcile after the spawn bundle, including a fresh observer after reconnect.
        if(role()==UNCANNY_GIANT&&owner().filter(p.getUUID()::equals).isPresent())p.connection.send(new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(this));
    }
    @Override public boolean shouldRenderAtSqrDistance(double distance){return role()==SILHOUETTE&&phase()==4?distance<192D*192D:super.shouldRenderAtSqrDistance(distance);}
    /**
     * A line over the actor's head. 0.4.398 (owner, of the Masque's red figure: "his dialogue does not disappear"): it stays up long
     * enough to read, five seconds or longer for a long line, then goes; saying it again keeps it up. 0.4.473: how long that is, is
     * Dialogue's to say, for every person in every scene; a scene asks {@link #saying} and does not time a line itself.
     */
    public void say(String text){say(null,text);}
    /**
     * 0.4.429: a line with its speaker's name. The owner: "text still needs to be centered, framed in center of player's vision and clear
     * itself of obstacles": the line is shown as a caption (client.SpeechCaptions), with this name before it, not as a nameplate. And:
     * "spoken npc dialogue doesn't track to journal": each new line is kept for whoever is near enough to hear it (TalkHistory.overheard).
     */
    public void say(@javax.annotation.Nullable String speaker,String text){
        boolean again=spokenFor>0&&getCustomName()!=null&&getCustomName().getString().equals(text)&&entityData.get(SPEAKER).equals(speaker==null?"":speaker);
        setCustomName(Component.literal(text));setCustomNameVisible(true);spokenFor=Dialogue.ticks(text);
        entityData.set(SPEAKER,speaker==null?"":speaker);
        if(!again&&!level().isClientSide)TalkHistory.overheard(this,speaker,text);
    }
    /** Who is saying the line up now, or empty. */
    public String speaker(){return entityData.get(SPEAKER);}
    /** Ticks the last line said stays up. */
    private int spokenFor;
    /** Whether the line this person last said is still up to be read (the server's to know). */
    public boolean saying(){return spokenFor>0;}
    /** Takes the line down now, read or not: for a person who is cut off, put away, or dead. */
    public void hush(){spokenFor=0;setCustomNameVisible(false);}
    @Override public void tick(){
        boolean privateKiller=!level().isClientSide&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent();
        if(!level().isClientSide&&entityData.get(SPEECH)>0)entityData.set(SPEECH,entityData.get(SPEECH)-1);
        if(!level().isClientSide&&spokenFor>0&&--spokenFor==0)setCustomNameVisible(false);
        var before=position();boolean active=!privateKiller||privateHunt.prepare(this);if(!level().isClientSide)BorrascaStory.actorTick(this);super.tick();
        if(!level().isClientSide){DomesticGlimpses.actorTick(this);GhostsMotion.tick(this);LiteraryMotion.tick(this);DinerWalk.tick(this);TunnelStride.tick(this);HouseReadShelter.tick(this);}
        if(lakePartner(role()))setAirSupply(getMaxAirSupply());
        if(privateKiller){if(active)privateHunt.tick(this,before);}
        else if(!level().isClientSide&&role()==KILLER&&(scene.equals(LabyrinthPlace.CAMP_BLOOD.id())||scene.equals(LabyrinthPlace.ELK_CARCASSES.id())))carcassHunt.tick(this);
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose){if(role()==ROAD_WOMAN)return roadDead(role(),phase())?EntityDimensions.scalable(.32F,.25F).withEyeHeight(.2F):EntityDimensions.scalable(.32F,1.1F).withEyeHeight(.95F);if(role()==UNCANNY_GIANT)return EntityDimensions.scalable(.32F,1.1F).withEyeHeight(.9F);if(role()==HARRIGAN)return phase()==HarriganVignette.CASKET_PHASE?EntityDimensions.scalable(.6F,.5F).withEyeHeight(.3F):EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(crawling())return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(role()==STRANGER&&PeopleLights.dragging(phase()))return EntityDimensions.scalable(.6F,.5F).withEyeHeight(.35F);if(role()==STRANGER&&PeopleLights.onFloor(phase()))return EntityDimensions.scalable(.6F,.95F).withEyeHeight(.8F);if(lakePartner(role())&&(phase()==2||phase()==6))return EntityDimensions.scalable(.6F,.6F).withEyeHeight(.35F);if(lakePartner(role())&&phase()==1)return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(role()==FATHER&&phase()==3)return EntityDimensions.scalable(.6F,1.15F).withEyeHeight(.9F);if(role()==GHOST_PLAYHOUSE)return EntityDimensions.scalable(1F,phase()>=2?.2F:1F).withEyeHeight(.4F);if(role()==GHOST_DRAWER)return EntityDimensions.scalable(.9F,.9F).withEyeHeight(.5F);if(role()>=GHOST_MARJORIE&&role()<=GHOST_PRIEST){if(phase()==GhostsSet.CRAWL)return EntityDimensions.scalable(.55F,.65F).withEyeHeight(.38F);if(phase()==GhostsSet.FLOOR)return EntityDimensions.scalable(.6F,.85F).withEyeHeight(.62F);if(GhostsSet.seatedPose(role(),phase()))return EntityDimensions.scalable(.6F,1.25F).withEyeHeight(1.05F);if(phase()==GhostsSet.BED)return EntityDimensions.scalable(.6F,.7F).withEyeHeight(.4F);}return role()==KILLER&&pose==Pose.CROUCHING?EntityDimensions.scalable(.6F,1.3F).withEyeHeight(1.05F):super.getDefaultDimensions(pose);}
    @Override public void travel(net.minecraft.world.phys.Vec3 input){if(!level().isClientSide&&scene.equals(LabyrinthPlace.BORRASCA.id()))input=BorrascaCast.input(this);super.travel(input);}
    @Override public void move(MoverType type,net.minecraft.world.phys.Vec3 movement){
        if(type==MoverType.SELF&&role()==KILLER&&scene.equals(LabyrinthPlace.ELK_CARCASSES.id())&&owner().isPresent()&&KillerNavigation.moving(this)){
            double horizontal=Math.hypot(movement.x,movement.z);
            if(horizontal>KillerNavigation.CHASE_STEP){double scale=KillerNavigation.CHASE_STEP/horizontal;movement=new net.minecraft.world.phys.Vec3(movement.x*scale,movement.y,movement.z*scale);}
        }super.move(type,movement);
    }
    /** 0.4.218: the cast drops nothing, ever: no loot, no equipment, no experience, nothing spawned at its feet (the owner saw the Wheel's mother drop fire charges). */
    @Override protected boolean shouldDropLoot(){return false;}
    @Override public boolean shouldDropExperience(){return false;}
    @Override protected void dropEquipment(){}
    @Override protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,DamageSource source,boolean recentlyHit){}
    @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(net.minecraft.world.item.ItemStack stack,float offsetY){return null;}
    /** A PathfinderMob never advances its swing on its own; the cast's blows and chops are seen only if it does (0.4.120). */
    @Override public void aiStep(){updateSwingTime();super.aiStep();}
    /**
     * 0.4.524: whose body stays where the scene turns it. The Pell house's people turn their heads to a reader and not their bodies
     * (only Callum turns his, and the server turns it); a still mob's body is otherwise brought round to its head after half a
     * second, on the client as well as the server.
     */
    public boolean holdsBody(){return role()==STRANGER&&PellPeople.cast(phase());}
    @Override protected net.minecraft.world.entity.ai.control.BodyRotationControl createBodyControl(){
        return new net.minecraft.world.entity.ai.control.BodyRotationControl(this){
            @Override public void clientTick(){if(holdsBody()){yBodyRot=getYRot();return;}super.clientTick();}
        };
    }
    @Override public boolean isPickable(){return !(role()==UNCANNY_FALSE_WAITRESS&&phase()<2)&&!waitingUnseen()&&super.isPickable();}
    /** 0.4.272: the tunnel woman before she comes out: nothing to aim at, strike or be pushed by (UncannyStories.GIANT_HOME). */
    public boolean waitingUnseen(){return role()==UNCANNY_GIANT&&phase()==0||roadDead(role(),phase());}
    @Override public boolean isAttackable(){return !waitingUnseen()&&super.isAttackable();}
    @Override protected void pushEntities(){if(!waitingUnseen()&&role()!=SP_RESIDENT&&role()!=ROAD_WOMAN)super.pushEntities();}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(HouseReadShelter.role(role())){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HouseReadShelter.take(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()==STRANGER&&KitchenVisitors.visitor(phase())){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)KitchenVisitors.interact(s,this,p.getItemInHand(hand));return InteractionResult.sidedSuccess(level().isClientSide);}if(role()==STRANGER&&Truant.phase(phase())){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)Truant.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()==STRANGER&&SpeakUp.cast(phase())){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)SpeakUp.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()==HARRIGAN||role()==HARRIGAN_MOURNER){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)HarriganVignette.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}if(role()>=GHOST_MARJORIE&&role()<=GHOST_DRAWER&&p.getItemInHand(hand).getItem() instanceof CamcorderItem){if(p instanceof ServerPlayer s)CamcorderItem.toggle(s,p.getItemInHand(hand));p.startUsingItem(hand);return InteractionResult.CONSUME;}if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND)LiteraryVignettes.talk(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){if(role()==ROAD_WOMAN){if(source.getEntity() instanceof ServerPlayer p)DeerRoad.struck(p,this,amount);return false;}if(role()==SP_RESIDENT||role()==STRANGER&&SecondPerson.cast(phase())){if(source.getEntity() instanceof ServerPlayer p)SecondPerson.touched(p,this);return false;}if(role()==STRANGER&&KitchenVisitors.visitor(phase())){if(source.getEntity() instanceof ServerPlayer p)KitchenVisitors.struck(p,this);return false;}if(role()==STRANGER&&Truant.phase(phase())){if(source.getEntity() instanceof ServerPlayer p)Truant.struck(p,this);return false;}if(source.getEntity() instanceof ServerPlayer p){
        if(role()==BORRASCA_SHERIFF){
            var fight=BorrascaStory.sheriffFight(p,this);
            if(fight!=null){
                // 0.4.427: a grown reader's blows land (three to eight each) but never kill outright; once he is down, the next one does.
                if(amount<=0)return false;
                if(fight.getBoolean("SheriffDown")){if(!BorrascaStory.sheriffFinished(p,this,fight))return false;super.hurt(source,Math.min(1,getHealth()-1));return true;}
                // 0.4.457: a blow not brought all the way back glances off him: it rouses him and does nothing else.
                if(!BorrascaSheriffFight.full(p,amount)){BorrascaSheriffFight.glanced(p,this,fight);return false;}
                float dealt=Math.min(Math.min(8,Math.max(3,amount)),getHealth()-1);
                boolean hit=dealt>0&&super.hurt(source,dealt);
                if(hit)BorrascaStory.sheriffStruck(p,this,fight);
                return hit;
            }
        }
        if(role()==BORRASCA_PRESCOTT&&phase()<BorrascaStory.WORKER){
            var night=BorrascaPrescott.night(p,this);
            if(night!=null){
                // 0.4.516: on the last night a grown reader's blow puts him down; once he is down, the next one kills him.
                if(amount<=0)return false;
                if(night.getBoolean("PrescottDown")){if(!BorrascaPrescott.finished(p,this,night))return false;super.hurt(source,Math.min(1,getHealth()-1));return true;}
                boolean hit=super.hurt(source,Math.min(BorrascaPrescott.BLOW,getHealth()-1));
                if(hit)BorrascaPrescott.struck(p,this,night);
                return hit;
            }
        }
        if(role()==FATHER&&LiteraryVignettes.inside(p,LabyrinthPlace.HOLY_RABBIT)){if(getHealth()<=1)return false;boolean hit=super.hurt(source,Math.min(Math.min(1,amount),getHealth()-1));if(hit)LiteraryVignettes.attacked(p,this);return hit;}LiteraryVignettes.attacked(p,this);}return false;}
    @Override public boolean broadcastToPlayer(ServerPlayer p){
        // Match the renderer's privacy rule at native tracking too. A hidden peer's
        // overlapping cast must not remain on this client and intercept its entity pick.
        boolean sharedCopy=scene.equals(LabyrinthPlace.FAMILY_COPY.id())||scene.equals(LabyrinthPlace.OLD_CABIN.id());
        // 0.4.156: the lake house's figure is also seen by the reader who goes down in Tess's place with its reader.
        // 0.4.404: the Ghosts set's cast is seen by the reader whose take it is and by their second cameras (GhostsCrew); a second camera's
        // own cast is out of their sight while they crew.
        boolean ghosts=GhostsCrew.castRole(role())&&owner().isPresent();
        if(ghosts&&!owner().get().equals(GhostsCrew.castOf(p.getUUID())))return false;
        if(!ghosts&&owner().isPresent()&&!sharedCopy&&!owner().get().equals(p.getUUID())&&!(role()==LAKE_WAX&&LakeHousePairs.sharesWith(p,owner().get())))return false;
        // 0.4.147: a thread's cast is seen by that thread's members alone.
        if(getPersistentData().contains(VignetteThreads.ACTOR_TAG)&&!VignetteThreads.sees(p,scene,getPersistentData().getString(VignetteThreads.ACTOR_TAG)))return false;
        // 0.4.183: Usher's Roderick is not there for a reader who has seen the house fall, and Madeline at the doors is that night's alone.
        if(scene.equals(LabyrinthPlace.USHER.id())&&!UsherStory.sees(p,this))return false;
        // 0.4.407: once a reader's day in the square is cut, the square is empty for them.
        if(scene.equals(LabyrinthPlace.THE_DRAWING.id())&&Drawing.cutOut(p.getUUID()))return false;
        if(scene.equals(ElkLot.PLACE.id())&&role()==STRANGER&&phase()>=5&&phase()<=8){var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),ElkLot.PLACE);if(own.getBoolean("Ready")||own.getBoolean("Completed"))return false;}
        if((lakePartner(role())||role()==LAKE_WAX)&&owner().isPresent()&&p.isSpectator())return false;
        return super.broadcastToPlayer(p);
    }
    @Override public boolean removeWhenFarAway(double d){return false;}
    /**
     * Run 1419: a body its scene has hidden stays hidden. Vanilla's first tick of any living entity (effectsDirty starts true), and every
     * change to its effects, sets the invisible flag from its effects alone, so an actor hidden as it was added could show for the tick
     * its chunk began ticking. The scene's own flag is kept apart from the effects': the body is hidden while either hides it, and an
     * invisibility potion on an actor still wears off.
     */
    private boolean sceneHidden,fromEffects;
    @Override public void setInvisible(boolean hidden){if(!fromEffects)sceneHidden=hidden;super.setInvisible(hidden);}
    @Override protected void updateInvisibilityStatus(){
        fromEffects=true;
        try{super.updateInvisibilityStatus();}finally{fromEffects=false;}
        if(sceneHidden&&!isInvisible())super.setInvisible(true);
    }
    @Override public boolean isPushable(){return role()==UNCANNY_FALSE_WAITRESS&&phase()>=2;}
    @Override public boolean canBeCollidedWith(){if(role()==UNCANNY_FALSE_WAITRESS)return phase()>=2;return super.canBeCollidedWith();}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(spokenFor>0)t.putInt("Said0398",spokenFor);t.putInt("Role",role());t.putInt("Phase",phase());t.putInt("Gesture0491",gesture());t.putString("Scene",scene);if(fade()>0)t.putInt("Fade0228",fade());if(torn()>0)t.putInt("Torn0439",torn());if(marks()!=0)t.putInt("Marks0535",marks());if(sceneHidden)t.putBoolean("SceneHidden",true);owner().ifPresent(id->t.putUUID("Reader",id));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);spokenFor=t.getInt("Said0398");scene=t.getString("Scene");entityData.set(OWNER,t.hasUUID("Reader")?Optional.of(t.getUUID("Reader")):Optional.empty());appearance(t.getInt("Role"),t.getInt("Phase"));gesture(t.getInt("Gesture0491"));fade(t.getInt("Fade0228"));torn(t.getInt("Torn0439"));marks(t.getInt("Marks0535"));sceneHidden=t.getBoolean("SceneHidden");}
}
