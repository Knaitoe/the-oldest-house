package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.468, Bike night: a reader's seat on an end of the park's seesaw. The board puts it where that end's seat is each tick, on
 * the client and the server alike (SeesawBlock.Board), and its rider goes with it. Nothing is drawn of it. It is never saved, and
 * it is gone when its rider is, or when the seesaw is.
 */
public class SeesawSeat extends Entity {
    private static final EntityDataAccessor<BlockPos> STAND=SynchedEntityData.defineId(SeesawSeat.class,EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> END=SynchedEntityData.defineId(SeesawSeat.class,EntityDataSerializers.INT);
    /** How far to the side of the board a reader is put when they get off. */
    public static final double ASIDE=.8;
    /** Thrown off the top: how much of the end's own speed they leave with, the most that is, and how fast they go outward. */
    public static final double LAUNCH=5,HIGHEST=.62,OUTWARD=.12;
    public SeesawSeat(EntityType<? extends SeesawSeat> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}

    public BlockPos stand(){return entityData.get(STAND);}
    /** Which end of the board: 0 its first, 1 its other. */
    public int end(){return entityData.get(END);}
    public @Nullable SeesawBlock.Board board(){
        return SeesawBlock.is(level().getBlockState(stand()),SeesawBlock.Part.STAND)&&level().getBlockEntity(stand()) instanceof SeesawBlock.Board b?b:null;
    }
    /** A hand on an end of the board sits on it. Returns the seat, or null if they cannot: somebody is on it, or they are riding something. */
    public static @Nullable SeesawSeat sit(ServerPlayer p,BlockPos endBlock){
        var l=p.serverLevel();var board=SeesawBlock.board(l,endBlock);
        if(board==null||!SeesawBlock.is(l.getBlockState(endBlock),SeesawBlock.Part.END)||p.isPassenger()||p.isSpectator()||!BikeNight.here(p))return null;
        int end=SeesawBlock.endOf(l,endBlock);if(board.ridden(end)||board.seat(end)!=null)return null;
        var seat=BikeNightRegistry.SEESAW_SEAT.get().create(l);if(seat==null)return null;
        seat.entityData.set(STAND,board.getBlockPos());seat.entityData.set(END,end);
        var at=SeesawBlock.seat(board.getBlockPos(),board.facing(),board.tilt(),end);
        // They face the stand, and whoever is on the other end.
        float yaw=(end==0?board.facing().getOpposite():board.facing()).toYRot();
        seat.moveTo(at.x,at.y,at.z,yaw,0);
        if(!l.addFreshEntity(seat)||!p.startRiding(seat,true)){seat.discard();return null;}
        board.sat(end,seat);
        p.setYRot(yaw);p.setYHeadRot(yaw);
        return seat;
    }
    /** 0.4.476: Gus gets on an end of the board, as a reader does. Null if he cannot: somebody is on it, or he is on something. */
    public static @Nullable SeesawSeat sit(LiteraryActor a,BlockPos endBlock){
        if(!(a.level() instanceof net.minecraft.server.level.ServerLevel l))return null;var board=SeesawBlock.board(l,endBlock);
        if(board==null||!SeesawBlock.is(l.getBlockState(endBlock),SeesawBlock.Part.END)||a.isPassenger())return null;
        int end=SeesawBlock.endOf(l,endBlock);if(board.ridden(end)||board.seat(end)!=null)return null;
        var seat=BikeNightRegistry.SEESAW_SEAT.get().create(l);if(seat==null)return null;
        seat.entityData.set(STAND,board.getBlockPos());seat.entityData.set(END,end);
        var at=SeesawBlock.seat(board.getBlockPos(),board.facing(),board.tilt(),end);
        float yaw=(end==0?board.facing().getOpposite():board.facing()).toYRot();
        seat.moveTo(at.x,at.y,at.z,yaw,0);
        if(!l.addFreshEntity(seat)||!a.startRiding(seat,true)){seat.discard();return null;}
        board.sat(end,seat);
        a.setYRot(yaw);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        return seat;
    }
    /** Who is on a seat, by name: a reader's, or Gus's. */
    static @Nullable String rider(@Nullable SeesawSeat seat){
        var who=seat==null?null:seat.getFirstPassenger();
        return who instanceof ServerPlayer p?p.getGameProfile().getName():BikeNightGus.is(who)?BikeNightGus.NAME:null;
    }
    /** The board puts the seat where its end is, and its rider with it. */
    void place(double tilt){
        var board=board();if(board==null)return;
        var at=SeesawBlock.seat(stand(),board.facing(),tilt,end());setPos(at.x,at.y,at.z);
        for(var rider:getPassengers())positionRider(rider);
    }
    /** The rider pressed jump (SwingJumpPayload): they push off the sand with their legs, if their end is down. True if it took. */
    public static boolean jump(ServerPlayer p){return p.getVehicle() instanceof SeesawSeat seat&&!p.isSpectator()&&seat.pushed();}
    /** Its rider pushes off: now, if their end is down (or kept for when it lands, if it is on its way). True if it took now. */
    boolean pushed(){
        // 0.4.476: whoever is on it pushes, Gus as well as a reader.
        var who=getFirstPassenger();if(who==null||!(level() instanceof net.minecraft.server.level.ServerLevel l))return false;
        var board=board();if(board==null)return false;
        int took=board.push(end());if(took==0)return false;
        l.playSound(null,getX(),getY(),getZ(),BikeNightRegistry.SEESAW_CREAK.get(),SoundSource.BLOCKS,took==2?.9F:.7F,(took==2?1.05F:.9F)+l.getRandom().nextFloat()*.2F);
        if(who instanceof ServerPlayer p)BikeNight.seesawed(p,rider(board.seat(1-end())),took==2);
        return true;
    }
    /**
     * The other end came down hard under its rider (`by`, if it has one): this seat's rider, up on top, is thrown off it, up and a
     * little outward, with the speed the end had (as a jump off a swing carries the swing's).
     */
    void thrown(double hit,@Nullable SeesawSeat by){
        if(!(getFirstPassenger() instanceof ServerPlayer p))return;
        var board=board();if(board==null)return;
        var out=end()==0?board.facing():board.facing().getOpposite();var from=position().add(0,.1,0);
        p.stopRiding();
        p.connection.teleport(from.x,from.y,from.z,p.getYRot(),p.getXRot());
        p.setDeltaMovement(out.getStepX()*OUTWARD,Math.min(HIGHEST,LAUNCH*hit*SeesawBlock.SEAT),out.getStepZ()*OUTWARD);p.hurtMarked=true;p.resetFallDistance();
        BikeNight.thrownOff(p,by!=null&&by.getFirstPassenger() instanceof ServerPlayer who?who:null);
    }
    @Override protected boolean canAddPassenger(Entity passenger){return getPassengers().isEmpty();}
    /** They sit on the seat's top, which is where this is. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions dimensions,float partial){return Vec3.ZERO;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(STAND,BlockPos.ZERO);builder.define(END,0);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public void tick(){
        super.tick();
        var board=board();
        if(!level().isClientSide()&&(board==null||getPassengers().isEmpty())){discard();return;}
        // It tells the board it is there; the board moves it (after this, in the same tick).
        if(board!=null)board.sat(end(),this);
    }
    /** Getting off puts a reader on the sand beside the end they were on. */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){
        var board=board();if(board==null)return position();
        var side=board.facing().getClockWise();
        return new Vec3(getX()+side.getStepX()*ASIDE,stand().getY(),getZ()+side.getStepZ()*ASIDE);
    }
}
