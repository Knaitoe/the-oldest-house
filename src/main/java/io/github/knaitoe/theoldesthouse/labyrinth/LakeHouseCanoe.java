package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The lake house's green canoe with brown trim. Underneath it is a vanilla boat, so it floats, paddles and seats two the
 * way readers already know; the client draws it as a canoe ({@code LakeHouseCanoeRenderer}).
 * <p>It is lent: each reader has one waiting by the dock, it cannot be broken or picked up and drops nothing, and it is
 * put away once its reader has left the scene and nobody is sitting in it. Anyone in the scene may ride in it; Tess takes
 * the second seat only behind a reader.
 * <p>Its paint wears in the culvert ({@link #chip()}), drawn as three textures.
 * <p>0.4.560 (owner: "The canoe tunnel pass needs improvement too"): the hands take over from the paddles wherever the culvert's
 * walls are in reach on both sides, out along its open troughs as well as under the drive; the canoe is eased in at either mouth
 * so its bow finds the gap; whoever sits in the back pushes too, each bench its own hands; a stroke is heard as a palm on the
 * concrete, and the hull scrapes only where it touches.
 */
public final class LakeHouseCanoe extends Boat {
    public static final String TAG="LakeHouseCanoe";
    /** How worn the paint is: 0 fresh, 1 chipped, 2 scraped. */
    private static final EntityDataAccessor<Integer> CHIPS=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PUSH_SIDE=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PUSH_TICKS=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    /** 0.4.560: the back bench's hand and stroke, and its strokes counted and the last of them, for the front bench's game to take. */
    private static final EntityDataAccessor<Integer> BACK_SIDE=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BACK_TICKS=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BACK_STROKES=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BACK_STROKE=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    /** 0.4.560: the scene the canoe was lent in, so it knows where the culvert's mouths are. */
    private static final EntityDataAccessor<Optional<BlockPos>> HOME=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.OPTIONAL_BLOCK_POS);
    /**
     * 0.4.201 (owner: "the movement through the hand-push corridor is dreadfully slow and awkward"): a stroke's arm lasts ten
     * ticks; the other hand may follow after eight, the same hand only once its stroke is done. Hand over hand the canoe now
     * averages about three blocks a second through the culvert (it was about one), gliding between strokes.
     */
    public static final int STROKE_TICKS=10,HAND_OVER_HAND=8;
    /** Each stroke's push along the culvert, the most the hull carries across the water, and how little the push sets it off its wall. */
    static final double STROKE=.15,TOP_SPEED=.3,OFF_WALL=.03;
    public static final int MAX_CHIPS=2;
    /** 0.4.560: how far out from a mouth of the culvert a canoe coming in is eased toward the gap, and how far either side of it. */
    static final double MOUTH_REACH=8,MOUTH_WIDE=3;
    /** How far along the hull each bench's hands meet the walls from, forward of its middle and aft, and how far across they reach. */
    static final double FRONT_BENCH=.35,BACK_BENCH=-.35,HAND_REACH=1.35;
    /** Where the canoes wait, one per reader: either side of the dock's far end. */
    static final BlockPos[] SPOTS={new BlockPos(4,-1,-19),new BlockPos(-4,-1,-19),new BlockPos(4,-1,-16),new BlockPos(-4,-1,-16)};
    private @Nullable UUID owner;
    private boolean inCulvert,underRoof;
    private int scrapeTicks,backApplied=-1;
    private boolean handLeft,handRight,handForward,handBack;

    public LakeHouseCanoe(EntityType<? extends Boat> type,Level level){super(type,level);setInvulnerable(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);b.define(CHIPS,0);b.define(PUSH_SIDE,0);b.define(PUSH_TICKS,0);
        b.define(BACK_SIDE,0);b.define(BACK_TICKS,0);b.define(BACK_STROKES,0);b.define(BACK_STROKE,0);b.define(HOME,Optional.empty());
    }

    /** Both bodies attach to the centres of the actual benches, rather than vanilla boat offsets. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger,EntityDimensions dimensions,float partial){
        double seat=getPassengers().indexOf(passenger)==0?9.5/16D:-9.5/16D;
        return new Vec3(0,dimensions.height()/3,seat).yRot(-getYRot()*(float)Math.PI/180);
    }
    @Override protected void positionRider(Entity passenger,Entity.MoveFunction move){
        if(passenger instanceof Player player)CanoeRiderPose.apply(player,underLowRoof());
        // 0.4.560: Tess (or Theo) ducks under the roof as the reader does, rather than putting her head through it.
        else if(passenger instanceof LiteraryActor actor&&LiteraryActor.lakePartner(actor.role()))CanoeRiderPose.duck(actor,underLowRoof());
        super.positionRider(passenger,move);
    }
    /**
     * Under the culvert's roof anywhere along the hull. Each bench sits well forward or aft of the hull's centre, so a
     * reader ducks before their head reaches the ceiling, whichever way the canoe is going.
     */
    public boolean underLowRoof(){
        if(!isVehicle())return false;var along=heading(getYRot());
        for(double reach:new double[]{-.95,0,.95}){
            var above=BlockPos.containing(getX()+along.x*reach,getY(),getZ()+along.z*reach).above(2);
            if(!level().getBlockState(above).getCollisionShape(level(),above).isEmpty())return true;
        }
        return false;
    }
    public int pushSide(){return entityData.get(PUSH_TICKS)>0?entityData.get(PUSH_SIDE):0;}
    public float pushAge(float partial){return STROKE_TICKS-entityData.get(PUSH_TICKS)+partial;}
    /** Which bench a rider sits on (0.4.560): 0 the front, where the canoe is steered from, 1 the back; -1 for anybody else. */
    public int seat(Entity rider){int i=getPassengers().indexOf(rider);return i>1?-1:i;}
    private static EntityDataAccessor<Integer> side(int seat){return seat==1?BACK_SIDE:PUSH_SIDE;}
    private static EntityDataAccessor<Integer> ticks(int seat){return seat==1?BACK_TICKS:PUSH_TICKS;}
    /** The hand a rider's stroke is using, -1 left, 1 right, 0 none: each bench its own. */
    public int pushSide(Entity rider){int s=seat(rider);return s<0||entityData.get(ticks(s))<=0?0:entityData.get(side(s));}
    public float pushAge(Entity rider,float partial){return STROKE_TICKS-entityData.get(ticks(Math.max(0,seat(rider))))+partial;}
    /**
     * 0.4.560: where the readers' hands do the work and the paddles cannot: under the culvert's roof, or between its walls where its
     * trough runs out to either lake. Only the culvert's own concrete counts, so no narrow place elsewhere takes the paddles away.
     * 0.4.564 (run 1596): the walls are looked for where the front bench's hands reach them, not at the middle of the hull.
     * Measured at the middle, a canoe going out of either mouth came to a place where its middle was still between the walls, so
     * the paddles were still taken away, while the front bench's hands already reached past the walls' ends, so no stroke could be
     * made; a canoe that glided to a stop there could go no further. Now the paddles come back the moment those hands are past the
     * walls.
     */
    public boolean pushable(){return underLowRoof()||isVehicle()&&culvertWall(-1)&&culvertWall(1);}
    private boolean culvertWall(int side){
        var hit=wallHit(side,FRONT_BENCH,HAND_REACH);if(hit==null)return false;
        var state=level().getBlockState(hit.getBlockPos());return state.getBlock() instanceof CulvertBlock||state.is(LakeHouseRegistry.CULVERT_MARK.get());
    }
    /** The wall a hand reaches across the culvert on one side, from a point along the hull, or none. */
    private @Nullable BlockHitResult wallHit(int side,double along,double reach){
        var from=position().add(0,.6,0).add(forward().scale(along));var end=from.add(right().scale(side*reach));
        if(!level().hasChunkAt(BlockPos.containing(end)))return null;
        var hit=level().clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        return hit.getType()==HitResult.Type.BLOCK?hit:null;
    }
    private static Vec3 heading(float yaw){double r=Math.toRadians(yaw);return new Vec3(-Math.sin(r),0,Math.cos(r));}
    /**
     * The way the culvert runs, as the facing nearest the hull's own: no paddle can turn the canoe under the roof, so it
     * lines itself up with the water rather than jamming crosswise between the walls.
     */
    public float channelYaw(){
        var at=BlockPos.containing(getX(),getY()+.3,getZ());
        int alongZ=open(at,net.minecraft.core.Direction.NORTH)+open(at,net.minecraft.core.Direction.SOUTH);
        int alongX=open(at,net.minecraft.core.Direction.EAST)+open(at,net.minecraft.core.Direction.WEST);
        float yaw=net.minecraft.util.Mth.wrapDegrees(getYRot());
        if(alongZ>=alongX)return Math.abs(net.minecraft.util.Mth.wrapDegrees(yaw-180))<=90?180:0;
        return Math.abs(net.minecraft.util.Mth.wrapDegrees(yaw-90))<=90?90:-90;
    }
    private int open(BlockPos at,net.minecraft.core.Direction d){
        int n=0;for(;n<3;n++){var cell=at.relative(d,n+1);if(!level().getBlockState(cell).getCollisionShape(level(),cell).isEmpty())break;}return n;
    }
    /** Along the culvert, and the reader's own right hand across it. */
    private Vec3 forward(){return heading(channelYaw());}
    private Vec3 right(){var f=forward();return new Vec3(-f.z,0,f.x);}
    /** A hand must reach a real solid wall on its own side (-1 left, 1 right); air and water supply no propulsion. */
    public boolean wallWithinReach(int side){return wallWithinReach(side,0);}
    /**
     * From the front bench, or (0.4.560) the back. A native 1.375-wide hull can sit against either wall of the two-wide channel; the
     * opposite wall is then 1.3125 away, and a 1.2 reach stranded an off-centre original.
     */
    public boolean wallWithinReach(int side,int seat){return (side==-1||side==1)&&wallHit(side,seat==1?BACK_BENCH:FRONT_BENCH,HAND_REACH)!=null;}
    /** How far the nearest wall is across the culvert on one side (-1 left, 1 right), up to two blocks. */
    double wallDistance(int side){
        var from=position().add(0,.6,0);var end=from.add(right().scale(side*2.0));
        if(!level().hasChunkAt(BlockPos.containing(end)))return 2;
        var hit=level().clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        return hit.getType()==HitResult.Type.BLOCK?hit.getLocation().distanceTo(from):2;
    }
    /**
     * One hand pushes off its own wall: the canoe goes along the culvert (back out, for a back stroke, {@code side} ±2) and
     * a little away from that wall. The same bounded stroke is predicted locally and checked against the server's actual
     * canoe and walls; the server allows a few ticks for the packet to arrive. 0.4.560: either bench may push, each with its own
     * hands and timing. The front bench's stroke moves the canoe where it is steered from; the back bench's is counted on the
     * canoe, and the game that moves it takes it from there ({@link #takeBackStrokes}).
     */
    public boolean push(Player player,int stroke){
        int side=Integer.signum(stroke),seat=seat(player);boolean back=Math.abs(stroke)==2;
        if(side==0||Math.abs(stroke)>2||seat<0)return false;
        // The other hand may start before this one's stroke is over; the same hand waits for its own. The server allows a few
        // more ticks for the packet to arrive.
        int last=entityData.get(side(seat)),wait=last==-side?STROKE_TICKS-HAND_OVER_HAND:0;
        if(!player.isAlive()||player.isSpectator()||player.getVehicle()!=this||!pushable()
                ||entityData.get(ticks(seat))>wait+(level().isClientSide?0:3)||BodyLoss.missing(player)==(side<0?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT)||!wallWithinReach(side,seat))return false;
        entityData.set(side(seat),side);entityData.set(ticks(seat),STROKE_TICKS);
        if(seat==0)stroke(side,back);
        else if(!level().isClientSide){entityData.set(BACK_STROKE,stroke);entityData.set(BACK_STROKES,entityData.get(BACK_STROKES)+1);}
        if(!level().isClientSide)level().playSound(null,getX(),getY(),getZ(),LakeHouseRegistry.HAND_PUSH.get(),SoundSource.NEUTRAL,.55F,.9F+random.nextFloat()*.2F);
        return true;
    }
    /** One stroke's push: along the culvert (back out, for a back stroke), and a little away from the wall the hand pushed off. */
    private void stroke(int side,boolean back){
        var away=right().scale(-side*OFF_WALL);
        var motion=getDeltaMovement().add(forward().scale(back?-STROKE:STROKE)).add(away);var horizontal=motion.multiply(1,0,1);
        if(horizontal.lengthSqr()>TOP_SPEED*TOP_SPEED)horizontal=horizontal.normalize().scale(TOP_SPEED);
        setDeltaMovement(new Vec3(horizontal.x,motion.y,horizontal.z));
    }
    /**
     * 0.4.560: the back bench's strokes, taken by whichever game moves the canoe (the front rider's, or the server's when nobody steers
     * it). A stroke counted before this game first saw the canoe is not taken late.
     */
    public void takeBackStrokes(){
        int counted=entityData.get(BACK_STROKES);if(counted==backApplied)return;
        if(backApplied>=0&&entityData.get(BACK_TICKS)>0){int s=entityData.get(BACK_STROKE);stroke(Integer.signum(s),Math.abs(s)==2);}
        backApplied=counted;
    }
    /** The stroke a rider's keys ask for: left or right uses that hand; forward or back goes hand over hand (one hand, if that is all the rider has). */
    private int strokeFor(Player player,boolean left,boolean right,boolean forward,boolean backward,int seat){
        boolean back=backward&&!forward;int side=left&&!right?-1:right&&!left?1:0;
        if(side==0&&(forward||back)){
            var missing=BodyLoss.missing(player);int last=entityData.get(side(seat));
            side=missing==net.minecraft.world.entity.HumanoidArm.LEFT?1:missing==net.minecraft.world.entity.HumanoidArm.RIGHT?-1:last!=0?-last:wallWithinReach(-1,seat)?-1:1;
        }
        return side==0?0:back?side*2:side;
    }
    /** 0.4.560: the back bench's keys, read by its rider's game (client.CanoePushClient); the canoe is not steered from there. */
    public void backSeat(Player player,boolean left,boolean right,boolean forward,boolean backward){
        if(!level().isClientSide||seat(player)!=1||!pushable())return;
        int stroke=strokeFor(player,left,right,forward,backward,1);
        if(stroke!=0&&push(player,stroke))net.neoforged.neoforge.network.PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.CanoePushPayload(stroke));
    }
    /** 0.4.560: the scene the canoe was lent in. */
    public @Nullable BlockPos home(){return entityData.get(HOME).orElse(null);}
    void home(BlockPos base){entityData.set(HOME,Optional.of(base.immutable()));}
    /**
     * 0.4.560: coming up to either mouth of the culvert with the bow toward it, the canoe is eased in: slid toward the middle of the
     * gap and turned to the channel, so it finds the two-wide way between the walls rather than the end of one. Nothing pulls a canoe
     * that is going past or away.
     */
    public void easeIn(){
        var b=home();if(b==null||!isVehicle())return;
        double middle=b.getX()+1.0,off=getX()-middle;if(Math.abs(off)>MOUTH_WIDE)return;
        var v=getDeltaMovement();
        for(int end=0;end<2;end++){
            // The south mouth opens on the boathouse's lake and is gone into heading north; the north one from the third lake, heading south.
            double mouth=end==0?b.getZ()+LakeHouse.CHANNEL_SOUTH+1:b.getZ()+LakeHouse.CHANNEL_NORTH;
            double out=end==0?getZ()-mouth:mouth-getZ(),in=end==0?-v.z:v.z;float yaw=end==0?180:0;
            if(out<=0||out>MOUTH_REACH||in<.02||Math.abs(net.minecraft.util.Mth.wrapDegrees(getYRot()-yaw))>60)continue;
            double want=net.minecraft.util.Mth.clamp(-off*.25,-.18,.18);
            setDeltaMovement(v.x+(want-v.x)*.4,v.y,v.z);
            setYRot(net.minecraft.util.Mth.approachDegrees(getYRot(),yaw,3F));
            return;
        }
    }
    @Override public void setInput(boolean left,boolean right,boolean up,boolean down){
        handLeft=left;handRight=right;handForward=up;handBack=down;
        if(pushable())super.setInput(false,false,false,false);else super.setInput(left,right,up,down);
    }

    public int chips(){return entityData.get(CHIPS);}
    /** One more knock against the culvert's walls. */
    public void chip(){entityData.set(CHIPS,Math.min(MAX_CHIPS,chips()+1));}
    void chips(int chips){entityData.set(CHIPS,Math.max(0,Math.min(MAX_CHIPS,chips)));}
    public @Nullable UUID owner(){return owner;}

    @Override protected boolean canAddPassenger(Entity e){
        if(!super.canAddPassenger(e))return false;
        if(e instanceof Player)return true;
        // Tess sits behind her reader, never at the paddle on her own.
        return e instanceof LiteraryActor&&getFirstPassenger() instanceof Player;
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public Item getDropItem(){return Items.AIR;}
    /** A canoe dropped onto land is only set down: no splintering into planks. */
    @Override protected void checkFallDamage(double y,boolean onGround,BlockState state,BlockPos pos){resetFallDistance();}

    /**
     * Under the service drive the culvert's ceiling is a block over the gunwales: too low and narrow for the paddles, so the
     * canoe crawls along as its readers push off the walls, scraping, and comes out with its paint chipped. 0.4.560: the same
     * between the walls of its open troughs, out to either lake.
     */
    @Override public void tick(){
        for(int s=0;s<2;s++){int remaining=entityData.get(ticks(s));if(remaining>0)entityData.set(ticks(s),remaining-1);}
        boolean low=underLowRoof(),hands=low||isVehicle()&&culvertWall(-1)&&culvertWall(1);
        if(hands&&level().isClientSide&&isControlledByLocalInstance()&&getFirstPassenger() instanceof Player player){
            int stroke=strokeFor(player,handLeft,handRight,handForward,handBack,0);
            if(stroke!=0&&push(player,stroke))net.neoforged.neoforge.network.PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.CanoePushPayload(stroke));
        }
        super.tick();
        if(isControlledByLocalInstance()){
            takeBackStrokes();
            if(hands){
                // 0.4.201: it glides between strokes, and keeps to the middle of the channel rather than knocking from wall to
                // wall: most of its drift across is lost each tick, and it eases toward the point between the two walls.
                var v=getDeltaMovement();var across=right();double drift=v.x*across.x+v.z*across.z;
                double toward=net.minecraft.util.Mth.clamp((wallDistance(1)-wallDistance(-1))*.04,-.02,.02);
                v=v.add(across.scale(drift*.4-drift+toward));
                setDeltaMovement(v.multiply(.97,1,.97));
                setYRot(net.minecraft.util.Mth.approachDegrees(getYRot(),channelYaw(),4F));
            }else easeIn();
        }
        if(!hands)for(int s=0;s<2;s++){entityData.set(ticks(s),0);entityData.set(side(s),0);}
        if(!level().isClientSide){
            if(hands&&!inCulvert)for(var e:getPassengers())if(e instanceof ServerPlayer p)p.displayClientMessage(Component.literal((low?"Too low to paddle.":"Too narrow to paddle.")+" Hold forward to push along the walls hand over hand, or back to push out again. Left or right uses that hand."),true);
            if(low&&!underRoof)chip();
            // 0.4.560: the hull scrapes where it touches a wall, not every stroke.
            double dx=getX()-xo,dz=getZ()-zo;
            if(hands&&--scrapeTicks<=0&&dx*dx+dz*dz>1e-4&&Math.min(wallDistance(-1),wallDistance(1))<.76){level().playSound(null,getX(),getY(),getZ(),LakeHouseRegistry.SCRAPE.get(),SoundSource.NEUTRAL,.7F,.9F+random.nextFloat()*.2F);scrapeTicks=20;}
        }
        inCulvert=hands;underRoof=low;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("Owner",owner);tag.putInt("Chips",chips());var b=home();if(b!=null)tag.putLong("Home",b.asLong());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;chips(tag.getInt("Chips"));if(tag.contains("Home"))home(BlockPos.of(tag.getLong("Home")));}

    // ------------------------------------------------------------------------------------------------ lending
    /** Puts a canoe by the dock for this reader unless one of theirs is already in the scene. */
    public static void ensure(ServerPlayer p){
        var b=LakeHouse.readerBase(p);if(b==null)return;var l=p.serverLevel();
        if(l.dimension()!=NovelRooms.dimension(LakeHouse.PLACE))return;
        var box=LakeHouse.box(b);
        if(!l.getEntitiesOfClass(LakeHouseCanoe.class,box,c->p.getUUID().equals(c.owner)).isEmpty())return;
        var c=LakeHouseRegistry.CANOE.get().create(l);if(c==null)return;
        c.owner=p.getUUID();c.addTag(TAG);c.home(b);
        var at=b.offset(SPOTS[0]);
        for(var spot:SPOTS){var cell=b.offset(spot);
            if(l.getEntitiesOfClass(LakeHouseCanoe.class,new net.minecraft.world.phys.AABB(cell).inflate(1.5)).isEmpty()){at=cell;break;}}
        // Bow to the north, toward the culvert and the third lake.
        c.moveTo(at.getX()+.5,at.getY()+.5,at.getZ()+.5,180,0);
        l.addFreshEntity(c);
    }
    /** Puts away every empty canoe whose reader is no longer in the scene. */
    public static void tidy(ServerLevel l,BlockPos b){
        var box=LakeHouse.box(b);
        for(var c:l.getEntitiesOfClass(LakeHouseCanoe.class,box)){
            if(c.isVehicle())continue;
            var reader=c.owner==null?null:l.getServer().getPlayerList().getPlayer(c.owner);
            if(reader==null||reader.level()!=l||!box.contains(reader.position()))c.discard();
        }
    }
}
