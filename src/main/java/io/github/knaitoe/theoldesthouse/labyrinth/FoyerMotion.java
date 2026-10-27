package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;

/** Gusts apply real force to the courtyard fitting; its motion renews the actual light sensor. */
public final class FoyerMotion {
    public static final BlockPos PULL=new BlockPos(3,1,-13),PENDULUM=new BlockPos(3,3,-19);
    private static final double DAMP=.016,FREQUENCY=.105,MAX_ANGLE=.48,SENSE_SPEED=.004;
    public record Pose(double angle,double velocity){}
    private FoyerMotion(){}

    /** The native fitting saves the physical initial conditions, not an accumulating light timer. */
    public static final class Swing {
        private boolean initialized;
        private long epoch,lastSensed,lastPull=Long.MIN_VALUE/2;
        private double angle,velocity;
        private int pulls;
        private long weatherEpoch,lastDriven=Long.MIN_VALUE;
        public double wind(long now){
            if(!initialized)return 0;double t=Math.floorMod(now-weatherEpoch,600);
            return t<180?Math.pow(Math.sin(Math.PI*(t+12)/204),2)*(.72+.23*Math.sin(t*.071)):0;
        }
        public boolean driveWind(long now){
            if(!initialized||lastDriven==now)return false;lastDriven=now;double gust=wind(now);if(gust<.015)return false;
            var current=sample(now);angle=current.angle();double push=.006*gust*(Math.sin((now-weatherEpoch)*.047)+.3*Math.cos((now-weatherEpoch)*.11));
            double limit=FREQUENCY*Math.sqrt(Math.max(0,MAX_ANGLE*MAX_ANGLE-angle*angle));velocity=Math.max(-limit,Math.min(limit,current.velocity()+push));epoch=now;return true;
        }
        public Pose sample(double now){
            if(!initialized)return new Pose(0,0);
            double t=Math.max(0,now-epoch),fade=Math.exp(-DAMP*t),c=Math.cos(FREQUENCY*t),s=Math.sin(FREQUENCY*t);
            double b=(velocity+DAMP*angle)/FREQUENCY;
            return new Pose(fade*(angle*c+b*s),fade*((b*FREQUENCY-DAMP*angle)*c-(angle*FREQUENCY+DAMP*b)*s));
        }
        public boolean initialize(long now){
            if(initialized)return false;
            initialized=true;epoch=now;weatherEpoch=now;lastSensed=now;angle=.28;velocity=0;return true;
        }
        public boolean pull(long now){
            initialize(now);if(now-lastPull<6)return false;
            var current=sample(now);angle=current.angle();
            double direction=current.velocity()<0?-1:1;
            double wanted=current.velocity()+direction*.032;
            double limit=FREQUENCY*Math.sqrt(Math.max(0,MAX_ANGLE*MAX_ANGLE-angle*angle));
            velocity=Math.max(-limit,Math.min(limit,wanted+DAMP*angle))-DAMP*angle;
            epoch=now;lastPull=now;lastSensed=now;pulls++;return true;
        }
        public boolean lit(long now){
            if(Math.abs(sample(now).velocity())>=SENSE_SPEED)lastSensed=now;
            // A sensor holds through a turning point, but cannot renew itself without real movement.
            return initialized&&now-lastSensed<=30;
        }
        public int pulls(){return pulls;}
        public long lastPull(){return lastPull;}
        public CompoundTag save(){
            var t=new CompoundTag();t.putBoolean("Initialized",initialized);t.putLong("Epoch",epoch);t.putLong("LastSensed",lastSensed);t.putLong("LastPull",lastPull);
            t.putDouble("Angle",angle);t.putDouble("Velocity",velocity);t.putInt("Pulls",pulls);t.putLong("WeatherEpoch136",weatherEpoch);t.putLong("WindDriven136",lastDriven);return t;
        }
        public void load(CompoundTag t){
            initialized=t.getBoolean("Initialized");epoch=t.getLong("Epoch");lastSensed=t.getLong("LastSensed");lastPull=t.contains("LastPull")?t.getLong("LastPull"):Long.MIN_VALUE/2;
            angle=t.getDouble("Angle");velocity=t.getDouble("Velocity");pulls=Math.max(0,t.getInt("Pulls"));
            weatherEpoch=t.contains("WeatherEpoch136")?t.getLong("WeatherEpoch136"):epoch;lastDriven=t.contains("WindDriven136")?t.getLong("WindDriven136"):Long.MIN_VALUE;
            if(!Double.isFinite(angle)||!Double.isFinite(velocity)){angle=velocity=0;}
            angle=Math.max(-MAX_ANGLE,Math.min(MAX_ANGLE,angle));velocity=Math.max(-.06,Math.min(.06,velocity));
        }
    }
    public static UncannyPropBlock.Fitting fitting(ServerLevel l,BlockPos b){
        var at=b.offset(PENDULUM);if(!l.hasChunkAt(at))return null;
        return l.getBlockEntity(at) instanceof UncannyPropBlock.Fitting f&&f.getBlockState().getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.COURTYARD_PENDULUM?f:null;
    }
    public static boolean dark(ServerLevel l,BlockPos b){
        var f=fitting(l,b);boolean lit=false;
        if(f!=null){long now=l.getGameTime();if(f.swing.initialize(now))f.syncMotion();if(f.swing.driveWind(now)&&now%10==0)f.syncMotion();lit=f.swing.lit(now);f.setChanged();}
        var lamp=b.offset(UncannyRooms.FLOOD);var s=l.getBlockState(lamp);
        if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FLOODLIGHT&&s.getValue(UncannyPropBlock.STAGE)!=(lit?0:1))l.setBlock(lamp,s.setValue(UncannyPropBlock.STAGE,lit?0:1),UncannyRooms.FLAGS);
        return !lit;
    }
    public static double wind(ServerLevel l,BlockPos b){var f=fitting(l,b);return f==null?0:f.swing.wind(l.getGameTime());}
    public static boolean pull(ServerPlayer p,BlockPos b,CompoundTag own){
        var at=b.offset(PULL);var s=p.serverLevel().getBlockState(at);
        if(!LiteraryVignettes.participant(p)||p.distanceToSqr(at.getCenter())>16||!s.is(UncannyRegistry.PROP.get())||s.getValue(UncannyPropBlock.KIND)!=UncannyPropBlock.Kind.PULL_CHAIN)return false;
        var f=fitting(p.serverLevel(),b);if(f==null)return false;
        if(f.swing.pull(p.serverLevel().getGameTime())){
            f.syncMotion();own.putInt("FoyerPulls131",own.getInt("FoyerPulls131")+1);dark(p.serverLevel(),b);
            UncannyStories.heard(p,SoundEvents.CHAIN_PLACE,at.getCenter(),.3F,.8F);
        }
        return true;
    }
}
