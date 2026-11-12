package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.463, Bike night's bikes (docs/BIKE_NIGHT_PROPOSAL.md; the owner: "Players can select from several bikes. These bikes have
 * slightly different stats - or really, each has a unique perk. better maneuverability, better acceleration, higher top speed, better
 * jumping, headlights, a card in the wheel that allows a temporary 'turbo' boost."). One of six, being ridden. Each rides as any bike
 * does ({@link RiddenBike}) and does one thing the others do not:
 *
 * <ul>
 *   <li><b>The Wasp</b>, a low BMX on fat tyres: handling. It turns much tighter, leans far less into a turn, and a glance along a wall
 *   takes almost nothing off it.</li>
 *   <li><b>The Hare</b>, a light racer: acceleration. Each push drives it on nearly twice as hard, and a climb costs it a third.</li>
 *   <li><b>The Comet</b>, a ten-speed: top speed. Much faster flat out, slow to turn and slow to stop.</li>
 *   <li><b>The Hopper</b>, a BMX with stunt pegs: jumping. A higher hop, a soft landing, a far harder fall before its rider comes off,
 *   and room on the pegs for a second rider, who gets on by a hand on the bike and steps off with the sneak key.</li>
 *   <li><b>The Lamp</b>, a paper-route bike: its dynamo lamp. A real light thrown ahead of the bike (BikeBeamBlock), brighter the faster
 *   it goes and dark when it stops.</li>
 *   <li><b>The Rattler</b>, a banana-seat bike with a card pegged into its spokes: the burst. Holding the sprint key flaps the card into
 *   a roar and drives the bike well past its top speed for two seconds; it comes back over fifteen of steady riding.</li>
 * </ul>
 *
 * Unridden it is a NightBikeBlock.
 */
public final class NightBike extends RiddenBike {
    private static final EntityDataAccessor<Integer> MAKE=SynchedEntityData.defineId(NightBike.class,EntityDataSerializers.INT);
    /** How much of the Rattler's burst is left, from nothing to one. */
    private static final EntityDataAccessor<Float> TURBO=SynchedEntityData.defineId(NightBike.class,EntityDataSerializers.FLOAT);
    /** How bright the Lamp's lamp is, from 0 (dark) to 15. */
    private static final EntityDataAccessor<Integer> LIT=SynchedEntityData.defineId(NightBike.class,EntityDataSerializers.INT);

    /**
     * The six. Each line's numbers are the bike's shape as tools/draw_bike_night_art.py draws it, in the model's pixels (the tool's
     * check reads them here): the back hub, the front hub, the crank axle, the wheels' radii back and front, and the saddle's height.
     */
    public enum Make implements StringRepresentable {
        WASP(new float[]{2.5F,4.5F,13.5F,4.5F,7.5F,4.5F,4.5F,4.5F,12F},Stats.of(s->{s.turn=10;s.turnLean=.00045F;s.glance=.8F;s.headOn=.15F;})),
        HARE(new float[]{1.5F,5.5F,14.5F,5.5F,7F,4F,5.5F,5.5F,13.7F},Stats.of(s->{s.turnPush=.11F;s.samePush=.04F;s.climb=.03F;})),
        COMET(new float[]{1.5F,6F,14.5F,6F,7F,4.5F,6F,6F,15.3F},Stats.of(s->{s.top=.46F;s.turn=4;s.brake=.95F;})),
        HOPPER(new float[]{2.5F,4.5F,13.5F,4.5F,7.5F,4.5F,4.5F,4.5F,12F},Stats.of(s->{s.popHigh=.56F;s.landTip=.2F;s.hardFall=1.3F;})),
        LAMP(new float[]{1.5F,5.5F,14.5F,5.5F,7F,4F,5.5F,5.5F,13.6F},Stats.PLAIN),
        RATTLER(new float[]{1.5F,5.5F,14.5F,4.5F,7F,4F,5.5F,4.5F,12.9F},Stats.PLAIN);
        private final float[] shape;public final Stats stats;
        Make(float[] shape,Stats stats){this.shape=shape;this.stats=stats;}
        @Override public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public String id(){return getSerializedName();}
        public float backX(){return shape[0];}public float backY(){return shape[1];}
        public float frontX(){return shape[2];}public float frontY(){return shape[3];}
        public float axleX(){return shape[4];}public float axleY(){return shape[5];}
        public float backRadius(){return shape[6];}public float frontRadius(){return shape[7];}
        public float saddle(){return shape[8];}
        /** Its name, as the kids who ride it say it. */
        public String title(){return "The "+name().charAt(0)+name().substring(1).toLowerCase(Locale.ROOT);}
    }
    /** The Rattler's burst: how long it lasts and how long it takes to come back (ticks), what it adds each tick, and how far past the bike's top it goes. */
    public static final int TURBO_TICKS=40,TURBO_BACK=300;
    public static final float TURBO_PUSH=.025F,TURBO_TOP=1.5F;
    /** With someone on the pegs, what gets through of a push or a hop. */
    public static final float CARRYING=.85F;

    private float turbo=1;private boolean burning;
    private @Nullable BlockPos beam;

    public NightBike(EntityType<? extends NightBike> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MAKE,0);b.define(TURBO,1F);b.define(LIT,0);}

    public Make make(){return Make.values()[Mth.clamp(entityData.get(MAKE),0,Make.values().length-1)];}
    public float turbo(){return entityData.get(TURBO);}
    public int lit(){return entityData.get(LIT);}
    /** The cell this bike's lamp is lighting, if any. */
    public @Nullable BlockPos beam(){return beam;}
    /** The burst the server is working with. For tests. */
    public float turboNow(){return turbo;}
    public void setUp(ServerPlayer p,Make make,float yaw){setUp(p,yaw);entityData.set(MAKE,make.ordinal());}

    @Override protected Stats stats(){return make().stats;}
    @Override protected double saddle(){return make().saddle()/16-.03;}
    @Override protected float load(){return getPassengers().size()>1?CARRYING:1;}
    @Override protected float cap(){return burning?stats().top*TURBO_TOP:0;}
    /**
     * 0.4.504, the Hollow's loose earth (CalderHollow.STEEP). A wheel sinks in it: a bike on it keeps LOOSE of its way each tick,
     * so it is ridden at a crawl however fast it was come onto. And a wheel digs in at a step: a bike on it mounts a whole block
     * only with the way on to pay for the climb (RiddenBike's hill: twice GRAVITY of the square of its speed for a block, times what
     * a climb costs its make), and is otherwise held at the step's foot, keeping what way it has. On firm ground a bike with no
     * way on mounts a step and stops on it.
     *
     * <p>LOOSE is set so that the make decides it and not the hands. Pushed ten times a second, an ordinary bike on loose earth
     * is going .16 at its fastest, and a step wants .22: it would take fourteen pushes a second. The Hare's push is .11 and its
     * step wants .13: a push every fifth tick, four a second, has it up; any slower and it has stopped between pushes.
     */
    public static final float LOOSE=.8F;
    private boolean onLoose(){return level().getBlockState(getOnPos()).is(BikeNightRegistry.material("hollow_earth"));}
    @Override protected float drag(){return onLoose()?LOOSE:1;}
    @Override protected boolean bogged(){return speedNow()*speedNow()<2*GRAVITY*stats().climb/CLIMB&&onLoose();}
    @Override public float maxUpStep(){float up=super.maxUpStep();return up>=1&&bogged()?LIP:up;}

    // ------------------------------------------------------------------ the pegs
    @Override protected boolean canAddPassenger(Entity e){return e instanceof Player&&getPassengers().size()<(make()==Make.HOPPER?2:1);}
    /** The one on the pegs stands over the back axle, feet on the pegs. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions d,float scale){
        if(getPassengers().indexOf(rider)<=0)return super.getPassengerAttachmentPoint(rider,d,scale);
        double s=rider instanceof LivingEntity le?le.getScale():1;Make m=make();
        return new Vec3(0,m.backY()/16+.6*s,(m.backX()-8)/16).yRot(-getYRot()*Mth.DEG_TO_RAD);
    }
    /** A Hopper with one rider can be touched by somebody else, to get on its pegs. Its own riders' hands pass through it. */
    @Override public boolean isPickable(){return !isRemoved()&&make()==Make.HOPPER&&getPassengers().size()==1;}
    @Override public boolean canBeHitByProjectile(){return false;}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(make()!=Make.HOPPER||getPassengers().size()!=1||p.isPassenger()||p.isSecondaryUseActive())return InteractionResult.PASS;
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(!p.startRiding(this))return InteractionResult.PASS;
        if(p instanceof ServerPlayer rider)NightBikes.onPegs(rider,this);
        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------------ each tick somebody rides it
    @Override protected void ridden(ServerPlayer p){
        Make m=make();
        if(m==Make.LAMP)lamp();
        if(m==Make.RATTLER)card();
        NightBikes.night.rode(p,this);
    }

    /** The Rattler. Held, the sprint key burns the burst; let go, steady riding brings it back. The card rattles all the while. */
    private void card(){
        boolean burn=turboHeld&&turbo>0&&onGround()&&speed>STOPPED;
        if(burn){
            if(!burning)level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.TURBO.get(),SoundSource.PLAYERS,.9F,1F);
            turbo=Math.max(0,turbo-1F/TURBO_TICKS);boost(TURBO_PUSH,stats().top*TURBO_TOP);
        }else if(!turboHeld&&speed>=STEADY&&Math.abs(lean)<.5F)turbo=Math.min(1,turbo+1F/TURBO_BACK);
        burning=burn;
        if(Math.abs(turbo-entityData.get(TURBO))>=.02F||turbo==0||turbo==1)entityData.set(TURBO,turbo);
        if(speed>STOPPED&&tickCount%(burning?3:6)==0){
            float going=Math.min(1.5F,speed/TOP);
            level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.CARD.get(),SoundSource.PLAYERS,(burning?.7F:.2F+.2F*going),.75F+.5F*going);
        }
    }

    /** The Lamp. Its dynamo makes light only while the wheel turns: brighter the faster, and dark when it stops. */
    private void lamp(){
        if(!(level() instanceof ServerLevel l))return;
        int level=speed<STOPPED?0:6+3*Math.round(3*Math.min(1,speed/STEADY));
        if(lit()!=level)entityData.set(LIT,level);
        BlockPos to=null;
        if(level>0){
            // Two blocks ahead at the bars' height; nearer, if something stands there.
            double rad=Math.toRadians(getYRot());
            for(double ahead:new double[]{2,1,0}){
                var cell=BlockPos.containing(getX()-Math.sin(rad)*ahead,getY()+1,getZ()+Math.cos(rad)*ahead);var s=l.getBlockState(cell);
                if(s.isAir()||s.is(BikeNightRegistry.BEAM.get())){to=cell;break;}
            }
        }
        if(beam!=null&&!beam.equals(to)){dark(l);}
        if(to!=null){
            var s=l.getBlockState(to);
            if(s.isAir()||(s.is(BikeNightRegistry.BEAM.get())&&s.getValue(BikeBeamBlock.LEVEL)!=level))l.setBlock(to,BikeBeamBlock.of(level),Block.UPDATE_CLIENTS);
            beam=to;
        }
        if(level>0&&tickCount%8==0){
            float going=Math.min(1,speed/TOP);
            l.playSound(null,getX(),getY(),getZ(),BikeNightRegistry.DYNAMO.get(),SoundSource.PLAYERS,.12F+.14F*going,.7F+.7F*going);
        }
    }
    private void dark(ServerLevel l){
        if(beam!=null&&l.isLoaded(beam)&&l.getBlockState(beam).is(BikeNightRegistry.BEAM.get()))l.setBlock(beam,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        beam=null;
    }
    @Override public void remove(RemovalReason reason){if(level() instanceof ServerLevel l)dark(l);super.remove(reason);}

    @Override protected void came(ServerPlayer p,int side,Off why){NightBikes.cameOff(p,this,why);}
    @Override protected void parkAs(ServerLevel l,@Nullable BlockPos at,boolean lying){
        if(at!=null)l.setBlock(at,NightBikeBlock.of(make(),Direction.fromYRot(getYRot()),lying),3);
        NightBikes.night.parked(l,reader,make(),at,lying);
    }
}
