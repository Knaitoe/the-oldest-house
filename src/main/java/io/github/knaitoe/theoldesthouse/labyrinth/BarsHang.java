package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.467, Bike night: a reader hanging from the park's monkey bars. It is the hold they have on the rungs: it stays at the rungs'
 * height and the reader hangs under it by their hands, at their full length (they are not sat down, as on a seat). Their walk keys
 * take it along the bars the way they are looking, a rung at a time, and it stops at the last rung each way. Getting off lets go,
 * and they drop where they hang. Nothing is drawn of it. It is never saved, and it is gone when its rider is, or when the rungs are.
 */
public class BarsHang extends Entity {
    /** Whether the bars run along x (east and west). */
    private static final EntityDataAccessor<Boolean> ALONG_X=SynchedEntityData.defineId(BarsHang.class,EntityDataSerializers.BOOLEAN);
    /**
     * A reader's fists over their feet with their arms straight up, in their own heights; how far short of the rungs their fists
     * can be and still catch them (a stretch); how fast they go along, in blocks a tick; how far apart the rungs are; how far to
     * the side of the bars a reader can be and take hold, and how far past their last rung; how nearly square across the bars a
     * look is that goes neither way.
     */
    public static final double REACH=.97,STRETCH=.3,PACE=.07,HAND=.5,SIDE=.8,PAST=1.3,SQUARE=.15;
    /** The first and last rungs along the bars; the least and most this hold has been along them; and how far it has come since a rung was last heard. */
    private double lo,hi,least,most,since;
    private boolean crossed;
    public BarsHang(EntityType<? extends BarsHang> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}

    public boolean alongX(){return entityData.get(ALONG_X);}
    /** Where the hold is along the bars. */
    public double along(){return alongX()?getX():getZ();}
    /** The first and last rungs it can be at. */
    public double lo(){return lo;}
    public double hi(){return hi;}
    /** How high a hold on a block of rungs is. */
    public static double height(BlockPos rungs){return rungs.getY()+CalderPlayBlock.RUNG;}
    /** The run of rungs a block belongs to: where its first and last rungs are along the way it runs. A block has a rung a quarter of the way in from each end. */
    public static double[] run(Level l,BlockPos rungs){
        var axis=l.getBlockState(rungs).getValue(CalderPlayBlock.FACING).getAxis();var step=Direction.fromAxisAndDirection(axis,Direction.AxisDirection.POSITIVE);var a=rungs;var b=rungs;
        while(CalderPlayBlock.is(l.getBlockState(a.relative(step.getOpposite())),CalderPlayBlock.Kind.RUNGS))a=a.relative(step.getOpposite());
        while(CalderPlayBlock.is(l.getBlockState(b.relative(step)),CalderPlayBlock.Kind.RUNGS))b=b.relative(step);
        return new double[]{a.get(axis)+.25,b.get(axis)+.75};
    }
    /** Why a reader cannot take hold of these rungs from where they are, as the line they are told; null if they can. */
    public static @Nullable String cannot(ServerPlayer p,BlockPos rungs){
        var l=p.serverLevel();var s=l.getBlockState(rungs);if(!CalderPlayBlock.is(s,CalderPlayBlock.Kind.RUNGS))return BikeNightTexts.BARS_FAR;
        boolean x=s.getValue(CalderPlayBlock.FACING).getAxis()==Direction.Axis.X;double[] run=run(l,rungs);
        double side=Math.abs((x?p.getZ():p.getX())-((x?rungs.getZ():rungs.getX())+.5)),along=x?p.getX():p.getZ();
        if(side>SIDE||along<run[0]-PAST||along>run[1]+PAST)return BikeNightTexts.BARS_FAR;
        if(p.getY()>=rungs.getY())return BikeNightTexts.BARS_ON;
        if(p.getY()+p.getBbHeight()*REACH+STRETCH<height(rungs))return BikeNightTexts.BARS_LOW;
        return null;
    }
    /** A hand on the rungs from under them, near enough: the reader takes hold over where they are. Returns the hold, or null if they cannot. */
    public static @Nullable BarsHang take(ServerPlayer p,BlockPos rungs){
        if(p.isPassenger()||p.isSpectator()||!BikeNight.here(p)||cannot(p,rungs)!=null)return null;
        var l=p.serverLevel();boolean x=l.getBlockState(rungs).getValue(CalderPlayBlock.FACING).getAxis()==Direction.Axis.X;double[] run=run(l,rungs);
        var hold=BikeNightRegistry.BARS_HANG.get().create(l);if(hold==null)return null;
        hold.lo=run[0];hold.hi=run[1];double at=Mth.clamp(x?p.getX():p.getZ(),hold.lo,hold.hi),across=(x?rungs.getZ():rungs.getX())+.5;hold.least=hold.most=at;
        hold.entityData.set(ALONG_X,x);
        hold.moveTo(x?at:across,height(rungs),x?across:at,p.getYRot(),0);
        if(!l.addFreshEntity(hold)||!p.startRiding(hold,true)){hold.discard();return null;}
        l.playSound(null,hold.getX(),hold.getY(),hold.getZ(),BikeNightRegistry.BARS.get(),SoundSource.PLAYERS,.6F,.95F+p.getRandom().nextFloat()*.1F);
        BikeNight.hung(p);
        return hold;
    }
    @Override protected boolean canAddPassenger(Entity passenger){return getPassengers().isEmpty();}
    /** They hang at their full length. */
    @Override public boolean shouldRiderSit(){return false;}
    /** The reader hangs under the hold by their hands: their feet are their reach below it. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions dimensions,float partial){
        return new Vec3(0,rider.getVehicleAttachmentPoint(this).y-rider.getBbHeight()*REACH,0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(ALONG_X,true);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public void tick(){
        super.tick();
        if(level().isClientSide())return;
        if(!(getFirstPassenger() instanceof ServerPlayer rider)||!CalderPlayBlock.is(level().getBlockState(blockPosition()),CalderPlayBlock.Kind.RUNGS)){discard();return;}
        // The walk keys take the hold along the bars, the way the reader is looking; a look square across them goes neither way.
        var look=rider.getLookAngle();double facing=alongX()?look.x:look.z;int push=rider.zza>.2F?1:rider.zza<-.2F?-1:0;
        if(push==0||Math.abs(facing)<SQUARE)return;
        double before=along(),now=Mth.clamp(before+Math.signum(facing)*push*PACE,lo,hi);
        if(now==before)return;
        setPos(alongX()?now:getX(),getY(),alongX()?getZ():now);
        since+=Math.abs(now-before);
        if(since>=HAND){since-=HAND;level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.BARS.get(),SoundSource.PLAYERS,.35F,1.05F+rider.getRandom().nextFloat()*.15F);}
        // From the last rung one way to the last rung the other, on one hold: across.
        least=Math.min(least,now);most=Math.max(most,now);
        if(!crossed&&least<=lo+1e-6&&most>=hi-1e-6&&hi-lo>1){crossed=true;BikeNight.crossed(rider);}
    }
    /** Letting go drops a reader where they hang. */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){return new Vec3(getX(),getY()-rider.getBbHeight()*REACH,getZ());}
}
