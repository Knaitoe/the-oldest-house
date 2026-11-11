package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
 * 0.4.468, Bike night: the playground's seesaw, drawn by tools/draw_calder_play_art.py. A board on a steel stand, with a seat and a
 * handle at each end and a bumper under each.
 * <ul>
 * <li>STAND: the stand the board turns on, in the middle. It faces the way the board's first end points, and it keeps the board
 * ({@link Board}): how far over it is and how fast it is going. The board is drawn turned about the stand's axle by the stand's
 * renderer, so it is drawn where it is and not where a block would have to put it.</li>
 * <li>END: the place an end of the board is over, two blocks out from the stand. It faces the way out. Nothing is drawn of it and
 * it stops nobody; a hand on it sits on that end.</li>
 * <li>BOARD and SEAT: the board's middle three blocks, and one end of it with its seat, handle and bumper. They are what the
 * renderer draws. They are never placed.</li>
 * </ul>
 * The board itself stops nobody: it moves, and a block cannot.
 */
public final class SeesawBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<SeesawBlock> CODEC=simpleCodec(SeesawBlock::new);
    public enum Part implements StringRepresentable {
        STAND,END,BOARD,SEAT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    /** How far out from the stand an end's block is; how far out along the board a rider sits, and the bumper under them is. */
    public static final int OUT=2;
    public static final double SEAT=2,BUMPER_AT=2.2;
    /** The axle's height over the sand; how far under the board's middle line the bottom of a bumper is; how far over it a rider sits. */
    public static final double AXLE=9.5/16,BUMPER=2.75/16,PAD=1.75/16;
    /** How far over the board goes: until the bottom of a bumper, where it is under the board, comes down on the sand. */
    public static final double MAX=Math.asin(AXLE/Math.hypot(BUMPER,BUMPER_AT))-Math.atan2(BUMPER,BUMPER_AT);
    public SeesawBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.STAND));}
    public static BlockState of(Part part,Direction facing){return BikeNightRegistry.SEESAW.get().defaultBlockState().setValue(PART,part).setValue(FACING,facing);}
    public static boolean is(BlockState s,Part part){return s.is(BikeNightRegistry.SEESAW.get())&&s.getValue(PART)==part;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}

    // ------------------------------------------------------------------------------------------------ where things are
    /**
     * A point on the board: {@code along} blocks out from the axle toward the board's first end (less than nothing, toward its
     * other), {@code over} blocks over its middle line, with the board {@code tilt} over (more than nothing: its first end down).
     */
    public static Vec3 at(BlockPos stand,Direction facing,double tilt,double along,double over){
        double out=along*Math.cos(tilt)+over*Math.sin(tilt),up=over*Math.cos(tilt)-along*Math.sin(tilt);
        return new Vec3(stand.getX()+.5+facing.getStepX()*out,stand.getY()+AXLE+up,stand.getZ()+.5+facing.getStepZ()*out);
    }
    /** Where the rider of an end sits (0 the board's first end, 1 its other). */
    public static Vec3 seat(BlockPos stand,Direction facing,double tilt,int end){return at(stand,facing,tilt,end==0?SEAT:-SEAT,PAD);}
    /** The block an end of the board is over. */
    public static BlockPos end(BlockPos stand,Direction facing,int end){return stand.relative(end==0?facing:facing.getOpposite(),OUT);}
    /** The board a block of the seesaw belongs to, or null. */
    public static @Nullable Board board(Level l,BlockPos at){
        var s=l.getBlockState(at);if(!s.is(BikeNightRegistry.SEESAW.get()))return null;
        var stand=s.getValue(PART)==Part.END?at.relative(s.getValue(FACING).getOpposite(),OUT):at;
        return is(l.getBlockState(stand),Part.STAND)&&l.getBlockEntity(stand) instanceof Board b?b:null;
    }
    /** Which end of its board an END block is: 0 if it lies the way the stand faces. */
    public static int endOf(Level l,BlockPos at){var b=board(l,at);return b!=null&&l.getBlockState(at).getValue(FACING)==b.getBlockState().getValue(FACING)?0:1;}

    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(PART)){
            // The stand's two posts and the axle between them, which runs across the way it faces.
            case STAND->Shapes.or(MarshBlock.turned(f,2,0,6.5,4.5,11,9.5),MarshBlock.turned(f,11.5,0,6.5,14,11,9.5),MarshBlock.turned(f,4.5,8.5,7,11.5,10.5,9));
            // Where a hand finds the end of the board, whichever way it is tipped.
            case END->Block.box(3,1,3,13,16,13);
            case BOARD,SEAT->Shapes.empty();
        };
    }
    /** The stand stops a body; an end's place does not. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PART)==Part.STAND?getShape(s,l,p,c):Shapes.empty();}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return s.getValue(PART)==Part.STAND?new Board(at,s):null;}
    @SuppressWarnings("unchecked")
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){
        return type==BikeNightRegistry.SEESAW_BOARD.get()&&s.getValue(PART)==Part.STAND?(BlockEntityTicker<T>)(BlockEntityTicker<Board>)Board::tick:null;
    }

    /**
     * The board. A rider's weight turns it their way; one whose end is down can push off the sand with their legs. Two riders
     * weigh the same, so between two a push carries the board all the way over; one alone comes back down. It comes down on its
     * bumper, and under a lone rider it bounces a little if it came down hard. The server and each client step it alike; the server sends where it is
     * whenever something happens to it, and a little while it moves.
     */
    public static final class Board extends BlockEntity {
        /**
         * How hard a rider's weight turns the board, in radians a tick each tick; what a push off the sand gives it; what it keeps
         * of its speed each tick; how near its limit an end is that is down; what it keeps of its speed coming down on a bumper,
         * the speed under which it does not bounce, and the speed over which it is heard.
         */
        public static final double WEIGHT=.002,PUSH=.03,KEEP=.98,DOWN=.02,BOUNCE=.25,SETTLE=.012,HEARD=.006;
        /** Before any game time: no rider's seat has said it is there. (Not the least long there is: the time since it is taken.) */
        private static final long NEVER=-1000;
        private double tilt=MAX,before=MAX,speed;
        /** Whether each end has a rider. */
        private boolean on0,on1;
        /** The server's: the riders' seats, which say each tick that they are there; and the game time each last did. */
        private final SeesawSeat[] seats=new SeesawSeat[2];
        private final long[] sat={NEVER,NEVER};
        public Board(BlockPos at,BlockState s){super(BikeNightRegistry.SEESAW_BOARD.get(),at,s);}

        public Direction facing(){return getBlockState().getValue(FACING);}
        /** How far over it is: more than nothing, its first end is down. */
        public double tilt(){return tilt;}
        public double tilt(float partial){return Mth.lerp(partial,before,tilt);}
        public double speed(){return speed;}
        /** Whether an end has a rider. */
        public boolean ridden(int end){return end==0?on0:on1;}
        /** Whether an end is down on the sand, near enough to push off it. */
        public boolean down(int end){return Math.abs(tilt-(end==0?MAX:-MAX))<=DOWN;}
        /** The seat on an end, if somebody is on it. */
        public @Nullable SeesawSeat seat(int end){var s=seats[end];return s!=null&&!s.isRemoved()&&here(end)?s:null;}
        private boolean here(int end){return level!=null&&level.getGameTime()-sat[end]<=2;}
        /** A rider's seat says it is there (each tick, on either side). */
        void sat(int end,SeesawSeat seat){seats[end]=seat;if(level!=null)sat[end]=level.getGameTime();}
        /** A rider pushes off the sand with their legs. It takes only while their end is down. */
        public boolean push(int end){
            if(level==null||level.isClientSide||!down(end))return false;
            speed=end==0?-PUSH:PUSH;sync();return true;
        }
        /** For the tests: put it somewhere. */
        public void set(double tilt,double speed){this.tilt=before=Mth.clamp(tilt,-MAX,MAX);this.speed=speed;sync();}

        static void tick(Level l,BlockPos at,BlockState s,Board b){
            if(!l.isClientSide){boolean a=b.here(0),c=b.here(1);if(a!=b.on0||c!=b.on1){b.on0=a;b.on1=c;b.sync();}}
            b.before=b.tilt;
            b.speed=(b.speed+WEIGHT*((b.on0?1:0)-(b.on1?1:0)))*KEEP;b.tilt+=b.speed;
            if(Math.abs(b.tilt)>=MAX){
                // Down on a bumper.
                int end=b.tilt>0?0:1;double hit=Math.abs(b.speed);boolean into=b.speed>0==b.tilt>0;b.tilt=b.tilt>0?MAX:-MAX;
                if(into){
                    if(!l.isClientSide&&hit>HEARD){
                        var where=SeesawBlock.at(at,b.facing(),b.tilt,end==0?BUMPER_AT:-BUMPER_AT,-BUMPER);
                        l.playSound(null,where.x,where.y,where.z,BikeNightRegistry.SEESAW_THUD.get(),SoundSource.BLOCKS,(float)Mth.clamp(hit/.04,.25,1),.9F+l.getRandom().nextFloat()*.2F);
                        b.sync();
                    }
                    // It bounces only where a weight will bring it back down: under a rider with nobody on the other end.
                    b.speed=hit>SETTLE&&b.ridden(end)&&!b.ridden(1-end)?-b.speed*BOUNCE:0;
                }
            }
            if(Math.abs(b.speed)<1e-6)b.speed=0;
            // Its riders go with it: now, so that they are drawn where the board is and not where it was.
            for(int end=0;end<2;end++){var seat=b.seats[end];if(seat!=null&&!seat.isRemoved())seat.place(b.tilt);else b.seats[end]=null;}
            if(!l.isClientSide&&b.speed!=0&&l.getGameTime()%10==0)b.sync();
        }
        private void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putDouble("Tilt",tilt);t.putDouble("Speed",speed);t.putBoolean("On0",on0);t.putBoolean("On1",on1);}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
            super.loadAdditional(t,r);if(!t.contains("Tilt"))return;
            double now=Mth.clamp(t.getDouble("Tilt"),-MAX,MAX);
            // A client that is nearly where the server is keeps what it was drawing from; one that is not starts again there.
            if(level==null||!level.isClientSide||Math.abs(now-tilt)>.08)before=now;
            tilt=now;speed=t.getDouble("Speed");
            on0=t.getBoolean("On0");on1=t.getBoolean("On1");
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
