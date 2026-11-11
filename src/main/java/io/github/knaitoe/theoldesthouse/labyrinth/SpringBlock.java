package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.471, Bike night: the playground's spring rider, drawn by tools/draw_calder_play_art.py. A horse on a coil spring, bolted to
 * the ground: "A horse on a spring that rocks with the strafe keys. It is pure play."
 * <ul>
 * <li>FOOT: the plate it is bolted down by. It faces the way the horse looks, and it keeps the rocking ({@link Rocker}): how far
 * over the horse is and how fast it is going. The horse is drawn rocked about the foot by the foot's renderer.</li>
 * <li>HORSE: the horse on its coil, which the renderer draws. It is never placed.</li>
 * </ul>
 * The spring's column stops a body; the horse, which moves, does not.
 */
public final class SpringBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<SpringBlock> CODEC=simpleCodec(SpringBlock::new);
    public enum Part implements StringRepresentable {
        FOOT,HORSE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    /** How high over the sand the coil meets its foot, which is what the horse rocks about; how far over that the saddle is; how far it rocks either way, in radians. */
    public static final double PIVOT=1.5/16,SADDLE=11.1/16,MAX=.45;
    public SpringBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.FOOT));}
    public static BlockState of(Part part,Direction facing){return BikeNightRegistry.SPRING_RIDER.get().defaultBlockState().setValue(PART,part).setValue(FACING,facing);}
    public static boolean is(BlockState s,Part part){return s.is(BikeNightRegistry.SPRING_RIDER.get())&&s.getValue(PART)==part;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    /** Where its rider sits, the horse rocked `angle` over: more than nothing, the way it looks. */
    public static Vec3 saddle(BlockPos foot,Direction facing,double angle){
        double out=SADDLE*Math.sin(angle);
        return new Vec3(foot.getX()+.5+facing.getStepX()*out,foot.getY()+PIVOT+SADDLE*Math.cos(angle),foot.getZ()+.5+facing.getStepZ()*out);
    }
    /** What a hand finds: the horse, about where it stands. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(PART)==Part.FOOT?MarshBlock.turned(s.getValue(FACING),4,0,0,12,16,15):Shapes.empty();
    }
    /** What stops a body: the foot and the coil on it. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PART)==Part.FOOT?Block.box(5,0,5,11,8,11):Shapes.empty();}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return s.getValue(PART)==Part.FOOT?new Rocker(at,s):null;}
    @SuppressWarnings("unchecked")
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){
        return type==BikeNightRegistry.SPRING_ROCKER.get()&&s.getValue(PART)==Part.FOOT?(BlockEntityTicker<T>)(BlockEntityTicker<Rocker>)Rocker::tick:null;
    }

    /**
     * The rocking. The spring pulls the horse back upright, harder the further over it is; it keeps most of its speed each tick;
     * its rider's lean pushes it. A lean held holds it a little over; leaning with it, back and forth, takes it out to its stops.
     * The server and each client step it alike (the rider's lean is sent, so that a client knows what is pushing it), and the
     * server sends where it is while it moves.
     */
    public static final class Rocker extends BlockEntity {
        /**
         * How hard the spring pulls it upright, for a radian of lean; what it keeps of its speed each tick; what a rider's lean
         * gives it each tick; under what lean and speed, with nobody on it, it has stopped; how far over is rocking, for a
         * rider's record (further than one lean, held, ever takes it: it has to be rocked); and the speed through the middle
         * over which the spring is heard.
         */
        public static final double SPRING=.05,KEEP=.965,LEAN=.01,STILL=.003,ROCKED=.4,HEARD=.03;
        private static final long NEVER=-1000;
        private double angle,before,speed;
        /** Whether somebody is on it, and which way they lean: 1 the way the horse looks, -1 back, 0 neither. */
        private boolean ridden;
        private int lean;
        /** The rider's seat, which says each tick that it is there; and the game time it last did (the server's). */
        private @Nullable SpringSeat seat;
        private long sat=NEVER;
        public Rocker(BlockPos at,BlockState s){super(BikeNightRegistry.SPRING_ROCKER.get(),at,s);}

        public Direction facing(){return getBlockState().getValue(FACING);}
        /** How far over it is: more than nothing, the way the horse looks. */
        public double angle(){return angle;}
        public double angle(float partial){return Mth.lerp(partial,before,angle);}
        public double speed(){return speed;}
        public boolean ridden(){return ridden;}
        /** The seat on it, if somebody is on it. */
        public @Nullable SpringSeat seat(){return seat!=null&&!seat.isRemoved()&&here()?seat:null;}
        private boolean here(){return level!=null&&level.getGameTime()-sat<=2;}
        /** Its rider's seat says it is there (each tick, on either side). */
        void sat(SpringSeat seat){this.seat=seat;if(level!=null)sat=level.getGameTime();}
        /** For the tests: put it somewhere. */
        public void set(double angle,double speed){this.angle=before=Mth.clamp(angle,-MAX,MAX);this.speed=speed;sync();}

        static void tick(Level l,BlockPos at,BlockState s,Rocker r){
            if(!l.isClientSide){
                boolean here=r.here();int lean=0;
                // The walk keys lean them the way the horse looks or back; so do the strafe keys, right for forward.
                if(here&&r.seat!=null&&r.seat.getFirstPassenger() instanceof ServerPlayer p)lean=Mth.clamp((p.zza>.2F?1:p.zza<-.2F?-1:0)+(p.xxa<-.2F?1:p.xxa>.2F?-1:0),-1,1);
                if(here!=r.ridden||lean!=r.lean){r.ridden=here;r.lean=lean;r.sync();}
            }
            r.before=r.angle;
            r.speed=(r.speed-SPRING*r.angle+LEAN*r.lean)*KEEP;r.angle+=r.speed;
            if(Math.abs(r.angle)>MAX){r.angle=r.angle>0?MAX:-MAX;r.speed=0;}
            if(!r.ridden&&Math.abs(r.angle)<STILL&&Math.abs(r.speed)<STILL){r.angle=0;r.speed=0;}
            // Its rider goes with it: now, so that they are drawn where the horse is and not where it was.
            if(r.seat!=null&&!r.seat.isRemoved())r.seat.place(r.angle);else r.seat=null;
            if(l.isClientSide)return;
            // The spring is heard as the horse comes through upright, going at all fast.
            if(r.before*r.angle<0&&Math.abs(r.speed)>HEARD)
                l.playSound(null,at.getX()+.5,at.getY()+.4,at.getZ()+.5,BikeNightRegistry.SPRING.get(),SoundSource.BLOCKS,(float)Mth.clamp(Math.abs(r.speed)/.12,.3,1),.9F+l.getRandom().nextFloat()*.2F);
            // Out past where rocking begins, from inside it: one rock, for its rider's record.
            if(Math.abs(r.angle)>=ROCKED&&Math.abs(r.before)<ROCKED&&r.seat!=null&&r.seat.getFirstPassenger() instanceof ServerPlayer p)BikeNight.sprung(p);
            if(r.speed!=0&&l.getGameTime()%10==0)r.sync();
        }
        private void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putDouble("Angle",angle);t.putDouble("Speed",speed);t.putBoolean("Ridden",ridden);t.putInt("Lean",lean);}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
            super.loadAdditional(t,r);if(!t.contains("Angle"))return;
            double now=Mth.clamp(t.getDouble("Angle"),-MAX,MAX);
            // A client that is nearly where the server is keeps what it was drawing from; one that is not starts again there.
            if(level==null||!level.isClientSide||Math.abs(now-angle)>.1)before=now;
            angle=now;speed=t.getDouble("Speed");ridden=t.getBoolean("Ridden");lean=t.getInt("Lean");
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
