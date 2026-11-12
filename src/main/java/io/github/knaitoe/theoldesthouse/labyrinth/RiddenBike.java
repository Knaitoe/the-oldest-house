package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A child's bike being ridden: the ride Borrasca's two bikes (0.4.435) and Bike night's six (0.4.463) share.
 *
 * <p><b>Pedalling and balance are one thing</b> (0.4.435, the owner: "Maintaining balance and pedaling left/right should be a
 * function."). The strafe keys are the pedals, and each press of left or right is one push on that pedal. A push drives the bike on and
 * tips it a little toward that side. Pushing the same pedal twice running drives it on less and tips it much more. Left to itself, a
 * moving bike leans further whichever way it already leans: quickly when it is slow, slowly when it is going well. Turning leans it
 * into the turn. Uphill slows it and downhill speeds it, by its own weight (0.4.478). So a reader keeps it up by pedalling in turn and, when it starts to go over,
 * by pushing twice on the pedal on the other side. If it leans all the way over, they come off and the bike lies in the road. Run into
 * a wall going fast and they come off too. Stopped, a foot goes down, nothing falls and the bike stands where it is, on a slope too
 * (0.4.478). The back key brakes, to a stop. It goes where the rider
 * looks, turning no faster than a bike can, and sneaking gets off.
 *
 * <p><b>The hop</b> (0.4.463, the owner: "a bike-jump feature I was hoping to include"). Holding jump, the rider crouches over the
 * bars; letting go pops the bike up, higher for a longer hold. In the air nothing drives it and it barely turns; the pedal keys shift
 * the rider's weight instead, to level it before it comes down. <b>A ramp</b> throws a moving bike up by its own speed: any ground it
 * was climbing when the ground ran out, a plank ramp or a flight of steps. <b>Landing</b> level keeps the way on. Landing crooked tips
 * it much further the way it leaned and takes some of the way off, so the rider must push out of it at once. Coming down far too hard
 * puts them over the bars. A bike coming down that just fails to clear a lip catches it with its front wheel and rolls up.
 *
 * <p><b>Precision</b> (0.4.520, the owner, after riding the Hollow: "the bike controls should be more precise for the sort of
 * platforming included"). Five changes, none of which does the riding for the rider. Holding forward keeps the pace: on the
 * ground the bike holds the speed the pedals have given it, its cranks turning, so that a run-up can be judged; pushes still make
 * the speed and keep the balance. A hop wound up when the wheels roll off an edge still pops if it is let go in the next few ticks
 * (COYOTE), as it would have from the brink. In the air the bike answers the bars better (AIR_TURN), and the brake sits the rider
 * back to come down shorter (AIR_BRAKE). Down on both wheels is a little wider (LEVEL). Wound up for a hop, the rider is braced:
 * the bike wanders off true half as much; and turning slowly takes less way than it did.
 *
 * <p>The server decides everything. The client sends its strafe keys as any rider does, and whether the jump and sprint keys are held
 * ({@code BikeKeysPayload}), and draws the bike from what the server says. A bike is never saved: an unridden bike is always a block,
 * and anything that takes the rider off it (getting off, a fall, a door, a teleport, logging out) puts the block back first.
 */
public abstract class RiddenBike extends Entity {
    private static final EntityDataAccessor<Float> LEAN=SynchedEntityData.defineId(RiddenBike.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED=SynchedEntityData.defineId(RiddenBike.class,EntityDataSerializers.FLOAT);
    /** Which pedal was pushed last (-1 left, 1 right) times how many pushes so far, for the cranks. */
    private static final EntityDataAccessor<Integer> STROKES=SynchedEntityData.defineId(RiddenBike.class,EntityDataSerializers.INT);
    /** How far the rider is down over the bars for a hop, from nothing to one. */
    private static final EntityDataAccessor<Float> CROUCH=SynchedEntityData.defineId(RiddenBike.class,EntityDataSerializers.FLOAT);
    /** Blocks a tick: the fastest a child's bike goes on the flat, what a push in turn adds, what a second push on the same pedal adds. */
    public static final float TOP=.32F,TURN_PUSH=.06F,SAME_PUSH=.02F;
    /** How far a push tips the bike toward its own side: in turn, and twice running on the same pedal. A lean of one is over. */
    public static final float TURN_TIP=.06F,SAME_TIP=.22F;
    /** Below STOPPED a foot is down. From STEADY up the bike tips slowest. Into a wall above CRASH, the rider comes off. */
    public static final float STOPPED=.03F,STEADY=.16F,CRASH=.2F;
    /** The most the bike can turn in a tick, in degrees, and how far over it is drawn at a lean of one. */
    public static final float TURN=6,LEAN_DEGREES=38;
    /** The wheels' radius in blocks, for how fast they turn. */
    public static final float WHEEL=5.5F/16F;
    /** The hop: ticks of holding for the highest, the speed upward of the least and of the highest, in blocks a tick. */
    public static final int CHARGE=12;
    public static final float POP_LOW=.26F,POP_HIGH=.42F;
    /** In the air: how far a push on a pedal shifts the rider's weight, and how much of its turning the bike keeps (0.4.520: was .35). */
    public static final float AIR_TIP=.1F,AIR_TURN=.6F;
    /**
     * 0.4.520: ticks after a bike has rolled off an edge in which a hop still wound up pops if it is let go; what of its way a bike
     * in the air keeps each tick while its rider holds the brake; and the least share of its turning a bike has when it is barely
     * moving (was .3).
     */
    public static final int COYOTE=5;
    public static final float AIR_BRAKE=.965F,SLOW_TURN=.55F;
    /** Coming down: slower than SOFT is nothing; from there to HARD a crooked landing tips more and more; faster than a bike's own limit, the rider comes off. */
    public static final float SOFT=.3F,HARD=1.1F,HARD_FALL=1F;
    /** A lean within LEVEL at landing is on both wheels, and keeps the way on (0.4.520: was .12). Past it, the lean grows by this much of itself for a landing as hard as HARD. */
    public static final float LEVEL=.2F,LAND_TIP=3.2F;
    /** Off a ramp: how much of the climb's own speed throws the bike up, the most it can, and the steepest slope that counts as one. */
    public static final float RAMP_GAIN=1.15F,RAMP_MAX=.7F,RAMP_SLOPE=1F;
    /** How high a lip a bike coming down can still catch with its front wheel and roll up. */
    public static final float LIP=.6F;
    /** How far above a ramp's sheet a bike rolling down it may find itself in a tick and still be kept on it. */
    public static final float STICK=.35F;
    /**
     * 0.4.478, what a hill is worth (the owner: "ensure bike properly accelerates and decelerates from the ramp"). GRAVITY is the pull
     * of the ground on a real bike, 9.8 metres a second each second, in blocks and ticks. On the ground a bike gives up twice that of
     * the square of its speed for each block it climbs, and is given as much for each block it comes down. CLIMB is the weight of an
     * ordinary bike (Stats.climb): a make that climbs well is lighter going up and no lighter coming down.
     */
    public static final float GRAVITY=.0245F,CLIMB=.09F;
    /**
     * 0.4.478: what the brake takes off the speed each tick beside its share of it, for a brake as hard as an ordinary bike's. With
     * only a share taken, a braked bike never quite stopped, and on a slope it crept down for as long as the brake was held.
     */
    public static final float GRIP=.012F;

    /** What one make of bike does differently. The plain one is Borrasca's. */
    public static final class Stats {
        /** Fastest on the flat; what a push in turn and a second on the same pedal add. */
        public float top=TOP,turnPush=TURN_PUSH,samePush=SAME_PUSH;
        /** Degrees it can turn in a tick, and how far a degree of turning at full speed leans it. */
        public float turn=TURN,turnLean=.0015F;
        /** What the brake leaves of the speed each tick. */
        public float brake=.9F;
        /** Along a wall: the least of its way a glance leaves it, and how nearly head on (as the share of its way it kept) puts the rider off. */
        public float glance=.3F,headOn=.4F;
        /** How hard a climb is on it: CLIMB is an ordinary bike, which a block climbed at full speed costs about this much of its speed. */
        public float climb=CLIMB;
        /** The highest hop, how hard a crooked landing tips it, and how fast it can come down without the rider coming off. */
        public float popHigh=POP_HIGH,landTip=1F,hardFall=HARD_FALL;
        private Stats(){}
        public static final Stats PLAIN=new Stats();
        public static Stats of(Consumer<Stats> change){var s=new Stats();change.accept(s);return s;}
    }
    /** Why a rider came off: the bike went over, it met something head on, or it came down too hard. */
    public enum Off{OVER,HEAD_ON,LANDING}

    protected float lean,speed;private int lastSide,strokes;private boolean leftDown,rightDown;protected boolean done;
    protected @Nullable UUID reader;
    private boolean jumpHeld;protected boolean turboHeld;private int charge;
    /** 0.4.520: ticks since it rolled off an edge with a hop wound up, or -1; and ticks since the cranks last went round at a kept pace. */
    private int coyote=-1,paceTicks,paceSide;
    /** 0.4.520: how far a bike rolls for each half turn of its cranks, for how often they go round at a kept pace. */
    public static final float STROKE=1.1F;
    /** The last eight ticks on the ground: how far along it went and how far it climbed in each, for the slope it leaves a ramp at. */
    private final float[] runs=new float[8],climbs=new float[8];private int onRoad;
    /** How steeply it climbed a plank ramp's sheet in the last tick, or nothing if it was not on one or not going up it. */
    private float onSheet;
    private boolean catching;
    /** What of its speed it has given up for height since it left the ground, which coming down gives it back. */
    private float lift;
    // The client's own drawing state: how far it has rolled, the cranks turning, the lean, crouch and pitch eased, and the server's position eased in.
    private float rolled,rolledO,crank,crankO,crankTo,shown,shownO,down,downO,pitch,pitchO;private int seenStrokes,lerpSteps;private double lerpX,lerpY,lerpZ;private float lerpYRot;

    protected RiddenBike(EntityType<? extends RiddenBike> type,Level level){super(type,level);}

    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(LEAN,0F);b.define(SPEED,0F);b.define(STROKES,0);b.define(CROUCH,0F);}
    // Never saved (the type is noSave): a bike nobody is on is a block.
    @Override protected void readAdditionalSaveData(CompoundTag t){}
    @Override protected void addAdditionalSaveData(CompoundTag t){}

    public float lean(){return entityData.get(LEAN);}
    public float speed(){return entityData.get(SPEED);}
    public int strokes(){return entityData.get(STROKES);}
    public float crouch(){return entityData.get(CROUCH);}
    /** The lean and speed the server is working with, between ticks' syncs. For tests. */
    public float leanNow(){return lean;}
    public float speedNow(){return speed;}
    /** Sets the lean and speed outright. For tests. */
    public void steady(float lean,float speed){this.lean=lean;this.speed=speed;}
    /** Whose bike this is while it is ridden, and which way it points. */
    public void setUp(ServerPlayer p,float yaw){reader=p.getUUID();setYRot(yaw);yRotO=yaw;}
    /** The keys only this mod sends: whether the rider holds jump (the hop) and sprint (the Rattler's card). */
    public void keys(boolean jump,boolean turbo){jumpHeld=jump;turboHeld=turbo;}

    /** What this make does differently. */
    protected abstract Stats stats();
    /** 0.4.504: what the ground under it leaves of its way each tick, beside what any ground takes: all of it, on anything firm. */
    protected float drag(){return 1;}
    /** 0.4.504: whether what stops it this tick is ground it has sunk into, which takes nothing more off it than its drag does, and not a wall. */
    protected boolean bogged(){return false;}
    /** The bike is a block again: stood or laid at {@code at}, or with nowhere to go ({@code at} null). */
    protected abstract void parkAs(ServerLevel l,@Nullable BlockPos at,boolean lying);
    /** Each tick somebody rides it and stays on. */
    protected void ridden(ServerPlayer p){}
    /** The rider came off, on that side. */
    protected void came(ServerPlayer p,int side,Off why){}
    /** How much of a push or a hop gets through: less with a second rider aboard. */
    protected float load(){return 1;}
    /** A speed above its usual limit the bike may hold just now, or nothing. */
    protected float cap(){return 0;}
    /** The saddle's top above the road, in blocks, less what a body settles into it. */
    protected double saddle(){return SADDLE;}

    /** The saddle's top above the road, less what a body settles into it. */
    public static final double SADDLE=13.5/16-.03;
    /**
     * Legs straight down toward the pedals, not out in front as on a horse. 0.4.447 (owner: "Try to fix mild visual distortion when
     * mounting bike."): the rider sits on the saddle. They used to be set .3 up less the .6 a player is held at, which put a child's
     * feet at the road, their hips at the cranks and their eyes level with the handlebars and the top of the saddle: the bars filled the
     * top of the view and the saddle's top lay across the middle of it. A body's hips are 12 of its 32 pixels up, so the seat is set
     * for whoever is riding: hips on the saddle, eyes well above the bars, which now sit low in the view where a rider's are.
     */
    @Override public boolean shouldRiderSit(){return false;}
    /**
     * 0.4.519: how high a rider's hips are above their feet, in blocks at their own size. A reader's are twelve of a body's thirty-two
     * pixels, 1.8 tall; one of the House's people is drawn on a humanoid model two blocks to its thirty-two pixels, so theirs are
     * twelve of those.
     */
    public static final double READER_HIPS=.675,CAST_HIPS=.75;
    public static double hips(@Nullable Entity rider){return rider instanceof Player?READER_HIPS:CAST_HIPS;}
    /**
     * Where a rider is carried: their hips on the saddle. The game sets a passenger at this point less their own vehicle attachment.
     * 0.4.519 (the owner, of Bike night's three: "the animation for the boys on their bikes needs to be fixed"): this had added a
     * reader's attachment, .6 of their size, for everybody; one of the House's people has none, so the three and Gus were carried
     * that much higher than a reader, their feet where their hips should have been, standing on the saddle. Each rider's own is
     * taken now. A reader is carried where they were.
     */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions d,float scale){
        double s=rider instanceof LivingEntity le?le.getScale():1;
        return new Vec3(0,saddle()-hips(rider)*s+rider.getVehicleAttachmentPoint(this).y,0);
    }
    /**
     * 0.4.496: whoever gets off, or comes off, is beside the bike with their feet where its wheels are: to its right if there is
     * room for them there, else its left, else where it is. The game's own rule sets a passenger down on top of what they were
     * riding, which for a rider thrown off against the face of a ledge was above the ledge: they could step onto ground their
     * bike could not reach (the Hollow's high way).
     */
    public static final double BESIDE=.7;
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){
        double rad=Math.toRadians(getYRot());var right=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
        for(double side:new double[]{BESIDE,-BESIDE}){
            var at=position().add(right.scale(side));
            if(level().noCollision(passenger,passenger.getDimensions(passenger.getPose()).makeBoundingBox(at)))return at;
        }
        return position();
    }
    /** On the ground a bike rolls up a whole block. Coming down, its front wheel catches a lip no higher than LIP. */
    @Override public float maxUpStep(){return catching?LIP:1F;}
    @Override public boolean isPickable(){return false;}
    @Override public boolean isAttackable(){return false;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty()&&takes(e);}
    /** Who may ride it: a reader. */
    protected boolean takes(Entity e){return e instanceof Player;}

    @Override public void tick(){
        super.tick();
        if(level().isClientSide){drawn();return;}
        if(done)return;
        driven();
    }
    /** The server's tick. A reader's bike goes by its reader's keys, and with nobody on it is a block again. */
    protected void driven(){
        if(!(getFirstPassenger() instanceof ServerPlayer p)){park(null,false);return;}
        // The pedals: a strafe key going down is one push (xxa is positive for left).
        boolean left=p.xxa>.5F,right=p.xxa< -.5F;
        if(left&&!leftDown)pedal(-1);
        if(right&&!rightDown)pedal(1);
        leftDown=left;rightDown=right;
        ride(p,p.zza< -.5F,p.zza>.5F);
    }

    /** One push on a pedal: -1 the left, 1 the right. In the air it drives nothing, and only shifts the rider's weight that way. */
    public void pedal(int side){
        if(done)return;
        if(!onGround()){lean=Mth.clamp(lean+side*AIR_TIP,-.95F,.95F);return;}
        // From a standstill any pedal is a first push: there is no pedal it follows.
        boolean same=side==lastSide&&speed>=STOPPED;Stats s=stats();
        // A push never slows a bike already going faster than it can be pedalled (downhill, or in the Rattler's burst).
        if(speed<s.top)speed=Math.min(s.top,speed+(same?s.samePush:s.turnPush)*load());
        lean+=side*(same?SAME_TIP:TURN_TIP);
        lastSide=side;strokes++;entityData.set(STROKES,side*strokes);
    }

    /** Drives the bike on, to no more than {@code limit}: the Rattler's card. */
    protected void boost(float push,float limit){if(speed<limit)speed=Math.min(limit,speed+push);}

    private void ride(ServerPlayer p,boolean braking,boolean pacing){
        Stats s=stats();boolean grounded=onGround();
        // The hop: held on the ground, the rider crouches over the bars; let go, the bike pops up, higher for a longer hold.
        // 0.4.520: just over the brink, rolled off an edge with it still wound up, letting go pops it as it would have from the edge.
        double pop=0;
        if(grounded){
            coyote=-1;
            if(jumpHeld)charge=Math.min(CHARGE,charge+1);
            else if(charge>0){pop=Mth.lerp(charge/(float)CHARGE,POP_LOW,s.popHigh)*load();charge=0;}
        }else if(charge>0&&coyote>=0&&coyote<COYOTE&&getDeltaMovement().y<=0){
            coyote++;
            if(!jumpHeld){pop=Mth.lerp(charge/(float)CHARGE,POP_LOW,s.popHigh)*load();charge=0;coyote=-1;}
        }else{charge=0;coyote=-1;}
        // Where the rider looks, as fast as a bike turns. Turning at speed leans it into the turn: a turn to the right (yaw going up) leans right.
        float want=Mth.wrapDegrees(p.getYRot()-getYRot());float turn=Mth.clamp(want,-s.turn,s.turn)*Math.min(1,SLOW_TURN+speed/STEADY);
        if(!grounded)turn*=AIR_TURN;
        setYRot(getYRot()+turn);lean+=turn*s.turnLean*speed/TOP;
        if(braking&&grounded)speed=Math.max(0,speed*s.brake-GRIP*(1-s.brake)*10);
        // 0.4.520: in the air the brake sits the rider back, and the bike comes down a little shorter.
        else if(braking)speed*=AIR_BRAKE;
        if(isInWater())speed*=.6F;
        // Balance. Stopped, a foot goes down. Moving, it leans further the way it leans, faster when slow, with a little wobble of its own.
        // In the air there is nothing to fall against: it keeps the lean it left the ground with, but for the rider's own weight.
        if(grounded){
            if(speed<STOPPED)lean*=.85F;
            else{
                // 0.4.520: wound up for a hop, the rider is braced over the bars, and the bike wanders off true half as much.
                float steady=Math.min(1,speed/STEADY),braced=charge>0?.5F:1;
                lean+=braced*(lean*(.03F*(1-steady)+.006F*steady)+(float)random.nextGaussian()*(.004F+.008F*(1-steady)));
            }
        }else lean=Mth.clamp(lean,-.95F,.95F);
        // On along the road.
        double rad=Math.toRadians(getYRot());var dir=new Vec3(-Math.sin(rad),0,Math.cos(rad));
        double fall=pop>0?pop:grounded?-.08:getDeltaMovement().y-.08;double y0=getY(),x0=getX(),z0=getZ();
        // Coming down, the front wheel catches a low lip and rolls up it, as on the ground it rolls up a block.
        catching=!grounded&&fall<0;if(catching)setOnGround(true);
        // What it asks to move by this tick: its speed is changed by the ground under it before what stopped it is looked at.
        final double askedX=dir.x*speed,askedZ=dir.z*speed;final boolean bogged=bogged();
        move(MoverType.SELF,new Vec3(askedX,fall,askedZ));
        catching=false;
        // A plank ramp: the bike rides the sheet itself, where the middle of its wheels is.
        float sheet=0;
        double top=sheetUnder();
        if(!Double.isNaN(top)){
            double gap=top-getY();
            if(gap>LIP&&grounded){
                // Into the ramp's side or its open back, well under the sheet: it is a wall.
                setPos(x0,getY(),z0);horizontalCollision=true;
            }else if(gap>=0||(grounded&&gap>-STICK&&!onGround())){
                // Up onto the sheet (rolling up it, or coming down onto it from the air), or kept on it going down. Not pulled down
                // onto it off real ground: where a sheet meets a deck or a ledge, the bike's wheels are on that until they are past it.
                setPos(getX(),top,getZ());setOnGround(true);sheet=1;
            }
        }
        boolean landed=onGround();double climbed=getY()-y0;float went=(float)Math.hypot(getX()-x0,getZ()-z0);
        double next=landed?0:Math.max(-2,fall*.98);
        if(pop>0)hopped((float)pop);
        else if(grounded&&!landed){
            // The ground ran out. If it was climbing, its own speed throws it up: off a ramp, or the top of a flight of steps.
            float slope=onSheet>0?Math.min(RAMP_SLOPE,onSheet):slope();
            if(slope>.05F){
                // Off a plank ramp it has gone on up the line of the sheet for this tick, not dropped off its end.
                if(onSheet>0)setPos(getX(),y0+onSheet*went,getZ());
                float up=Math.min(RAMP_MAX,speed*slope*RAMP_GAIN);hopped(up);
                // What it goes up by in the next tick, before that tick's own share of falling is taken off.
                next=up+.08;
                // Thrown up, a hop wound up for the lip is spent: there is no second pop off nothing.
                charge=0;coyote=-1;
            }else if(charge>0)coyote=0;
        }
        setDeltaMovement(0,next,0);
        if(grounded&&landed){runs[onRoad&7]=went;climbs[onRoad&7]=(float)Math.max(0,climbed);onRoad++;}else onRoad=0;
        // How steeply it is going up a ramp's sheet just now, for the tick the sheet ends: exact, since the sheet is.
        onSheet=sheet>0&&landed&&went>.01F&&climbed>0?(float)(climbed/went):0;
        // A hill. On the ground a bike gives up, or is given, the square of its speed for the height it gains or loses, as a real one
        // does: so a climb a fast bike carries stalls a slow one, and a bike let roll from the top of a ramp is going well by the bottom.
        // In the air what it climbs it pays for, and coming down gives it back.
        if(grounded&&landed){
            if(climbed>.001)speed=(float)Math.sqrt(Math.max(0,speed*speed-2*GRAVITY*(s.climb/CLIMB)*climbed));
            else if(climbed< -.001)speed=(float)Math.sqrt(speed*speed-2*GRAVITY*climbed);
        }else if(climbed>.01){float paid=Math.min(speed,(float)climbed*.04F);speed-=paid;lift+=paid;}
        else if(climbed< -.01){
            // Coming down gives a moving bike speed for the drop. One that has no way on gets back only what its own climb took
            // (0.4.481): hopped where it stands, a bike paid nothing going up, and used to come down rolling on what the fall
            // gave it, just fast enough to be riding and wobbling with nobody pedalling.
            float back=(float)-climbed*.04F;if(speed<STOPPED)back=Math.min(back,lift);
            speed+=back;lift=Math.max(0,lift-back);
        }
        if(landed)lift=0;
        // Into something: a glance along a wall keeps most of the way on; head on at speed, the rider comes off.
        // 0.4.492: what it keeps is the share of its way that lay along the thing, not how much of this tick's move it got in before
        // it met it. By that measure a bike going flat out square at a wall, with more than two fifths of a tick's travel left
        // to the wall, was let off as a glance, came down to under the crash speed in that tick, and stood against the wall with its
        // rider on: about one hit in five at top speed, by where in its stride the bike happened to be.
        if(horizontalCollision&&speed>.001F&&!bogged){
            boolean stopX=Math.abs(getX()-x0-askedX)>1e-4,stopZ=Math.abs(getZ()-z0-askedZ)>1e-4;
            double asked=Math.hypot(askedX,askedZ),kept=asked<1e-6?0:Math.hypot(stopX?0:askedX,stopZ?0:askedZ)/asked;
            if(kept<s.headOn&&speed>CRASH){off(p,lean>=0?1:-1,Off.HEAD_ON);return;}
            speed*=(float)Mth.clamp(kept,s.glance,1);
        }
        // Landing. Level, it keeps its way. Crooked, it tips much further the way it leaned and loses some. Far too hard, the rider is off.
        if(!grounded&&landed){
            float impact=(float)-fall,hard=Mth.clamp((impact-SOFT)/(HARD-SOFT),0,1);
            if(impact>s.hardFall){off(p,lean>=0?1:-1,Off.LANDING);return;}
            if(hard>0){
                float crooked=Math.abs(lean);
                if(crooked>LEVEL){lean+=Math.signum(lean)*crooked*s.landTip*hard*LAND_TIP;speed*=1-.4F*Math.min(1,crooked)*Math.min(1,hard*3);}
                touched(hard);
            }
        }
        // 0.4.520: holding forward on the ground keeps the pace the pedals have made: nothing is lost to rolling, and the cranks
        // go round at it. It makes no speed of its own, and the ground's own drag (sand, mud) is still taken.
        boolean paced=pacing&&grounded&&landed&&speed>=STOPPED&&speed<=s.top;
        // The kept pace's strokes are only drawn: the pedal a push follows is still the rider's own last push.
        if(!paced)paceSide=0;
        else if(++paceTicks>=Math.max(3,Math.round(STROKE/Math.max(.05F,speed)))){paceTicks=0;paceSide=paceSide==0?-(lastSide==0?1:lastSide):-paceSide;strokes++;entityData.set(STROKES,paceSide*strokes);}
        speed=Mth.clamp(speed*(paced?1:.988F)*(grounded&&landed?drag():1),0,Math.max(s.top*1.4F,cap()));
        // Stopped is stopped (0.4.478): with a foot down the bike stands where it is, on the level or on a slope. It used to creep on
        // at whatever was left, which over the many ticks of turning round where it stood took it most of a block.
        if(landed&&speed<STOPPED)speed=0;
        entityData.set(LEAN,Mth.clamp(lean,-1,1));entityData.set(SPEED,speed);entityData.set(CROUCH,charge/(float)CHARGE);
        if(Math.abs(lean)>=1)off(p,lean>0?1:-1,Off.OVER);
        else ridden(p);
    }

    /**
     * How high (in the world) the sheet a bike rides is under the middle of the bike (a plank ramp's, the slide's chute: BikeSurface),
     * or NaN where there is none under it. The cell it is in, or the one below: a bike at the top of a cell is on the cell below, and a
     * chute's sheet stands a little higher than its own block.
     */
    protected double sheetUnder(){
        var at=BlockPos.containing(getX(),getY()+.001,getZ());
        for(int i=0;i<2;i++,at=at.below()){
            var s=level().getBlockState(at);
            if(s.getBlock() instanceof BikeSurface sheet){double top=sheet.sheet(s,at,getX(),getZ());if(!Double.isNaN(top))return at.getY()+top;}
        }
        return Double.NaN;
    }

    /**
     * How steeply it was climbing just before the ground ran out: the steepest it climbed over any two ticks running, both of them
     * climbing, in the last block of road. Two ticks, because one tick's climb is a kerb and not a ramp. The last block, because a
     * bike stands on whatever is highest under it, so the top of a ramp carries it level for its own length before it is clear.
     */
    private float slope(){
        float best=0,back=0;int have=Math.min(onRoad,8);
        for(int i=1;i<have;i++){
            int a=(onRoad-i)&7,b=(onRoad-i-1)&7;float run=runs[a]+runs[b];
            if(climbs[a]>0&&climbs[b]>0&&run>.05F)best=Math.max(best,(climbs[a]+climbs[b])/run);
            back+=runs[a];if(back>1F)break;
        }
        return Math.min(RAMP_SLOPE,best);
    }

    /** For a bike that goes as it is told and not by a reader's keys (PackBike): its lean and speed, as they are and as they are drawn. */
    protected void show(float lean,float speed){this.lean=lean;this.speed=speed;entityData.set(LEAN,Mth.clamp(lean,-1,1));entityData.set(SPEED,speed);}
    /** Half a turn of the cranks, on that side, for the drawing. */
    protected void stroke(int side){lastSide=side;strokes++;entityData.set(STROKES,side*strokes);}

    /** The bike leaves the ground, going up that fast: the tyres let go of the road. */
    protected void hopped(float up){
        level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.HOP.get(),SoundSource.PLAYERS,.5F,.9F+Math.min(.4F,up)+random.nextFloat()*.1F);
    }
    /** The bike comes down, that hard from nothing to one: both tyres, the chain, and the dust it raises. */
    protected void touched(float hard){
        level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.LAND.get(),SoundSource.PLAYERS,.4F+.5F*hard,1.1F-.3F*hard);
        if(level() instanceof ServerLevel l){
            BlockState under=l.getBlockState(getOnPos());
            if(!under.isAir())l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,under),getX(),getY()+.05,getZ(),4+Math.round(8*hard),.25,.02,.25,.02);
        }
    }

    /** Over it goes, or into something head on, or down too hard: everyone on it comes off on that side, and the bike lies in the road. */
    private void off(ServerPlayer p,int side,Off why){
        done=true;
        double rad=Math.toRadians(getYRot());var right=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
        for(Entity e:List.copyOf(getPassengers())){
            e.stopRiding();
            if(!(e instanceof ServerPlayer r))continue;
            r.setDeltaMovement(right.scale(side*.3).add(0,.2,0));r.hurtMarked=true;
            if(r.getHealth()>4)r.hurt(level().damageSources().fall(),1);
        }
        park(p,true);came(p,side,why);
    }

    /**
     * 0.4.484: ridden into something that is not the town's own blocks and has no body to be ridden into (a car). It is as riding
     * into a wall: faster than CRASH, everyone on it comes off and it lies in the road. True if they came off.
     */
    public boolean struck(){
        if(done||speed<=CRASH||!(getFirstPassenger() instanceof ServerPlayer p))return false;
        off(p,lean>=0?1:-1,Off.HEAD_ON);return true;
    }

    /**
     * 0.4.503: stopped dead by something that has no body to be ridden into, going too slowly to come off at: as a front wheel
     * against a kerb. What it has of rising or falling it keeps.
     */
    public void balk(){if(done)return;setDeltaMovement(0,getDeltaMovement().y,0);show(lean,0);}

    /** Only the one pedalling takes the bike with them when they get off: a second rider stepping down leaves it going. */
    @Override protected void removePassenger(Entity e){
        boolean pedalling=getFirstPassenger()==e;super.removePassenger(e);
        if(!level().isClientSide&&!done&&pedalling&&e instanceof ServerPlayer p)park(p,false);
    }

    /**
     * 0.4.495: set down somewhere else as it is, with whoever is on it, stopped and upright and pointing {@code yaw}. The Hollow puts
     * a rider who has missed a jump back on the last island they stood on (BikeNightHollow).
     */
    /**
     * 0.4.521, carried through the cemetery's fold (BikeNightFold): the same bike, somewhere else, with whoever is on it and its way,
     * lean and wound-up hop kept. What it had been riding over is behind it.
     */
    public void fold(double by){teleportTo(getX(),getY(),getZ()+by);onRoad=0;onSheet=0;resetFallDistance();}
    public void putBack(double x,double y,double z,float yaw){
        setYRot(yaw);yRotO=yaw;teleportTo(x,y,z);setDeltaMovement(Vec3.ZERO);setOnGround(true);resetFallDistance();
        speed=0;lean=0;charge=0;coyote=-1;paceTicks=0;paceSide=0;lift=0;onRoad=0;onSheet=0;lastSide=0;
        entityData.set(LEAN,0F);entityData.set(SPEED,0F);entityData.set(CROUCH,0F);
    }

    /** Taken from under whoever is on it: it is gone without being stood anywhere, and everyone on it is left where they were. */
    public void taken(){done=true;discard();}

    /** Back to a block where it stopped, on its kickstand or lying on its side, and gone as an entity. */
    private void park(@Nullable ServerPlayer p,boolean lying){
        if(!(level() instanceof ServerLevel l)||isRemoved())return;done=true;
        parkAs(l,place(l,blockPosition()),lying);
        discard();
    }
    /**
     * The nearest free ground for the bike to stand on: its own cell, else one beside it. A rider who lets go of a bike in the air
     * leaves it on the ground under where it was, as far as DROP down. Null over nothing at all.
     */
    private static @Nullable BlockPos place(ServerLevel l,BlockPos from){
        for(int down=0;down<=DROP;down++){
            var mid=from.below(down);
            for(int r=0;r<=2;r++)for(int dy=1;dy>=-2;dy--)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;var at=mid.offset(dx,dy,dz);
                BlockState here=l.getBlockState(at),below=l.getBlockState(at.below());
                if((here.isAir()||here.canBeReplaced())&&here.getFluidState().isEmpty()&&below.isFaceSturdy(l,at.below(),Direction.UP))return at;
            }
            // Further down only through open air: a bike does not come to rest under a floor.
            if(down>0&&!l.getBlockState(mid).isAir()&&!l.getBlockState(mid).canBeReplaced())break;
        }
        return null;
    }
    /** How far below a bike let go of in the air its place on the ground is looked for, in blocks. */
    public static final int DROP=24;

    // ------------------------------------------------------------------ drawing (client)
    private void drawn(){
        double x0=getX(),y0=getY(),z0=getZ();
        if(lerpSteps>0){double f=1D/lerpSteps;setPos(getX()+(lerpX-getX())*f,getY()+(lerpY-getY())*f,getZ()+(lerpZ-getZ())*f);setYRot(getYRot()+Mth.wrapDegrees(lerpYRot-getYRot())*(float)f);lerpSteps--;}
        rolledO=rolled;rolled+=speed();
        // Each push brings that pedal round to the bottom: the left is down at a turn of 0, the right at half a turn.
        int s=strokes();if(s!=seenStrokes){seenStrokes=s;float bottom=s<0?0:Mth.PI;crankTo=bottom+(float)Math.ceil((crankTo+.01F-bottom)/Mth.TWO_PI)*Mth.TWO_PI;}
        crankO=crank;crank+=(crankTo-crank)*.3F;
        shownO=shown;shown+=(lean()-shown)*.5F;
        downO=down;down+=(crouch()-down)*.5F;
        // Nose up as it climbs or leaves the ground, down as it comes back: the line it is travelling along.
        double rise=getY()-y0,run=Math.hypot(getX()-x0,getZ()-z0);
        float to=run>.02?(float)Mth.clamp(Math.atan2(rise,Math.max(run,.1)),-.6,.6):0;
        pitchO=pitch;pitch+=(to-pitch)*.35F;
    }
    @Override public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){
        // 0.4.521: carried through the fold, or set down far off: there at once, and whoever is on it with it, not swept across what lies between.
        if(distanceToSqr(x,y,z)>FAR_LERP*FAR_LERP){setPos(x,y,z);setYRot(yRot);setOldPosAndRot();lerpSteps=0;for(var e:getPassengers()){positionRider(e);e.setOldPosAndRot();}return;}
        lerpX=x;lerpY=y;lerpZ=z;lerpYRot=yRot;lerpSteps=3;
    }
    /** 0.4.521: past this many blocks, a bike is moved on a client at once. */
    public static final double FAR_LERP=24;
    @Override public double lerpTargetX(){return lerpSteps>0?lerpX:getX();}
    @Override public double lerpTargetY(){return lerpSteps>0?lerpY:getY();}
    @Override public double lerpTargetZ(){return lerpSteps>0?lerpZ:getZ();}
    @Override public float lerpTargetYRot(){return lerpSteps>0?lerpYRot:getYRot();}
    /** How far over it is drawn, from -1 (left) to 1 (right). */
    public float shownLean(float partial){return Mth.lerp(partial,shownO,shown);}
    /** How far it has rolled, in blocks, for wheels of any size; and how far round a wheel of the usual size and the cranks have turned, in radians. */
    public float rolled(float partial){return Mth.lerp(partial,rolledO,rolled);}
    public float spin(float partial){return rolled(partial)/WHEEL;}
    public float crank(float partial){return Mth.lerp(partial,crankO,crank);}
    /** How far the rider is down over the bars, from nothing to one, and the bike's nose up (radians), as drawn. */
    public float shownCrouch(float partial){return Mth.lerp(partial,downO,down);}
    public float shownPitch(float partial){return Mth.lerp(partial,pitchO,pitch);}
}
