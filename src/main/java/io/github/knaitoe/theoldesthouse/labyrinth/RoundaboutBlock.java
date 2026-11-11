package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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
 * 0.4.469, Bike night: the playground's merry-go-round, drawn by tools/draw_calder_play_art.py. A round deck on a hub, three
 * blocks across, with four rails from the hub to its rim. It is pushed round by its rails from the sand beside it, and stood on.
 * <ul>
 * <li>HUB: the middle of it. It keeps the wheel ({@link Wheel}): how far round it is and how fast it is turning. The deck and its
 * rails are drawn turned about the hub by the hub's renderer.</li>
 * <li>The eight cells round the hub (NW to SE): the rest of the ground the deck is over. Nothing is drawn of them.</li>
 * <li>WHEEL: the deck and its rails, which the renderer draws. It is never placed.</li>
 * </ul>
 * A deck that is round is over the same ground however far round it is, so its blocks can be stood on although it turns: every
 * cell stops a body where the deck is over it, at the deck's height. The rails stop nobody: they move. A hand finds the wheel
 * anywhere over the deck up to its rails' height, so that a reader running round beside it can keep a hand on it.
 */
public final class RoundaboutBlock extends Block implements EntityBlock {
    public static final MapCodec<RoundaboutBlock> CODEC=simpleCodec(RoundaboutBlock::new);
    public enum Cell implements StringRepresentable {
        NW(-1,-1),N(0,-1),NE(1,-1),W(-1,0),HUB(0,0),E(1,0),SW(-1,1),S(0,1),SE(1,1),WHEEL(0,0);
        /** Where the cell is from the hub. */
        public final int dx,dz;
        Cell(int dx,int dz){this.dx=dx;this.dz=dz;}
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Cell> CELL=EnumProperty.create("cell",Cell.class);
    /** The deck's radius, in blocks; how high its top is over the sand; how tall the hub's post is, in pixels. */
    public static final double RADIUS=23./16,DECK=3./16;
    public static final int POST=14;
    private static final Map<Cell,VoxelShape> SHAPES=new HashMap<>(),REACHED=new HashMap<>();
    public RoundaboutBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(CELL,Cell.HUB));}
    public static BlockState of(Cell cell){return BikeNightRegistry.ROUNDABOUT.get().defaultBlockState().setValue(CELL,cell);}
    public static boolean is(BlockState s){return s.is(BikeNightRegistry.ROUNDABOUT.get())&&s.getValue(CELL)!=Cell.WHEEL;}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CELL);}

    /** The hub of the merry-go-round a block belongs to. */
    public static BlockPos hub(BlockPos at,BlockState s){var c=s.getValue(CELL);return at.offset(-c.dx,0,-c.dz);}
    /** The wheel a block of the merry-go-round belongs to, or null. */
    public static @Nullable Wheel wheel(Level l,BlockPos at){
        var s=l.getBlockState(at);if(!is(s))return null;
        return l.getBlockEntity(hub(at,s)) instanceof Wheel w?w:null;
    }
    /**
     * The ground the deck is over in a cell, `high` pixels up: every two-pixel square of the cell whose middle is within the
     * deck's radius of the middle of the hub.
     */
    private static VoxelShape over(Cell c,double high){
        VoxelShape out=Shapes.empty();
        if(c==Cell.WHEEL)return out;
        for(int z=0;z<16;z+=2){
            // One run of the row's squares that are under the deck: a row of a disc is one run.
            int from=-1,to=-1;
            for(int x=0;x<16;x+=2){
                double px=c.dx+(x+1)/16.-.5,pz=c.dz+(z+1)/16.-.5;
                if(px*px+pz*pz<=RADIUS*RADIUS){if(from<0)from=x;to=x+2;}
            }
            if(from>=0)out=Shapes.or(out,Block.box(from,0,z,to,high,z+2));
        }
        return out.optimize();
    }
    /** What a hand finds: the wheel's whole height over the ground its deck is over, up to its rails. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return REACHED.computeIfAbsent(s.getValue(CELL),cell->over(cell,POST));}
    /** What stops a body: the deck, at the deck's height, and the hub's post. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return SHAPES.computeIfAbsent(s.getValue(CELL),cell->cell==Cell.HUB?Shapes.or(over(cell,DECK*16),Block.box(6.5,0,6.5,9.5,POST,9.5)):over(cell,DECK*16));
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return s.getValue(CELL)==Cell.HUB?new Wheel(at,s):null;}
    @SuppressWarnings("unchecked")
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){
        return type==BikeNightRegistry.ROUNDABOUT_WHEEL.get()&&s.getValue(CELL)==Cell.HUB?(BlockEntityTicker<T>)(BlockEntityTicker<Wheel>)Wheel::tick:null;
    }

    /**
     * The wheel. It turns as fast as it was last pushed, and slows by itself. The server and each client step it alike; the
     * server sends where it is at each push and a little while it turns. Whoever stands on its deck is carried round with it
     * ({@link #carry}); standing up on it while it goes fast, they are thrown outward, and keeping low they hold on.
     */
    public static final class Wheel extends BlockEntity {
        /**
         * What a push gives it, in radians a tick; the fastest it goes; what it keeps of its speed each tick; the speed under
         * which it has stopped; the ticks between one reader's pushes; how far from the hub a reader can push it from; how
         * nearly at the hub a look is that pushes neither way; the speed over which someone standing up on it is thrown, and how
         * hard.
         */
        public static final double PUSH=.035,TOP=.25,KEEP=.994,STILL=.0008,OUT=.1,FLING=.5;
        public static final int WAIT=8;
        public static final double REACH=3.2,SQUARE=.25;
        private double angle,before,speed;
        /** The server's: when each reader last pushed, and how far round it was when it was last heard. */
        private final Map<UUID,Long> pushed=new HashMap<>();
        private double heardAt;
        public Wheel(BlockPos at,BlockState s){super(BikeNightRegistry.ROUNDABOUT_WHEEL.get(),at,s);}

        /** How far round it is, in radians: a point of it that was east of the hub is that far round toward the south. */
        public double angle(){return angle;}
        public double angle(float partial){return Mth.lerp(partial,before,angle);}
        /** How fast it is turning, in radians a tick. */
        public double speed(){return speed;}
        /** The middle of the hub, on the sand. */
        public Vec3 middle(){return new Vec3(worldPosition.getX()+.5,worldPosition.getY(),worldPosition.getZ()+.5);}
        /** Whether a body is standing on the deck. */
        public boolean on(Entity e){
            var m=middle();double dx=e.getX()-m.x,dz=e.getZ()-m.z;
            return dx*dx+dz*dz<=RADIUS*RADIUS&&Math.abs(e.getY()-(m.y+DECK))<.05&&!e.isPassenger();
        }
        /**
         * A reader shoves a rail from the sand beside it. It goes the way they are facing along its rim. Why it did not take, as
         * the line they are told; null if it took.
         */
        public @Nullable String push(ServerPlayer p){
            if(level==null||level.isClientSide)return null;
            if(on(p))return BikeNightTexts.ROUNDABOUT_ON;
            var m=middle();double rx=p.getX()-m.x,rz=p.getZ()-m.z,far=Math.sqrt(rx*rx+rz*rz);
            if(far>REACH||far<1e-3)return BikeNightTexts.ROUNDABOUT_FAR;
            var look=p.getLookAngle();double along=(-rz*look.x+rx*look.z)/far;
            if(Math.abs(along)<SQUARE)return BikeNightTexts.ROUNDABOUT;
            Long last=pushed.get(p.getUUID());long now=level.getGameTime();if(last!=null&&now-last<WAIT)return null;
            pushed.put(p.getUUID(),now);
            speed=Mth.clamp(speed+Math.signum(along)*PUSH,-TOP,TOP);sync();BikeNight.shoved(p);
            level.playSound(null,p.getX(),p.getY()+.6,p.getZ(),BikeNightRegistry.BARS.get(),SoundSource.PLAYERS,.5F,.8F+level.getRandom().nextFloat()*.1F);
            return null;
        }
        /** For the tests: set it turning. */
        public void set(double angle,double speed){this.angle=before=angle;this.speed=Mth.clamp(speed,-TOP,TOP);sync();}

        static void tick(Level l,BlockPos at,BlockState s,Wheel w){
            w.before=w.angle;
            w.speed*=KEEP;if(Math.abs(w.speed)<STILL)w.speed=0;
            w.angle+=w.speed;
            // Kept between nothing and a whole turn, with what it is drawn from kept beside it.
            double turn=Math.PI*2;if(w.angle>=turn){w.angle-=turn;w.before-=turn;w.heardAt-=turn;}else if(w.angle<0){w.angle+=turn;w.before+=turn;w.heardAt+=turn;}
            if(l.isClientSide)return;
            // Its bearing is heard once a quarter of a turn, while it is going at all.
            if(Math.abs(w.angle-w.heardAt)>=Math.PI/2){
                w.heardAt=w.angle;
                if(Math.abs(w.speed)>.02)l.playSound(null,at.getX()+.5,at.getY()+.3,at.getZ()+.5,BikeNightRegistry.ROUNDABOUT_SQUEAK.get(),SoundSource.BLOCKS,(float)Mth.clamp(Math.abs(w.speed)/TOP,.25,.9),.9F+l.getRandom().nextFloat()*.2F);
            }
            // Whatever stands on it that is not somebody's own to move goes round with it. (A reader is moved by their own game.)
            if(w.speed!=0)for(var e:l.getEntities((Entity)null,new net.minecraft.world.phys.AABB(at).inflate(1.5,1,1.5),e->!(e instanceof net.minecraft.world.entity.player.Player)&&w.on(e)))w.carry(e,false);
            if(w.speed!=0&&l.getGameTime()%20==0)w.sync();
        }
        /**
         * Somebody standing on the deck goes round with it by this tick's turn, and is turned with it. Standing up on it while it
         * goes fast they are pushed outward as well; keeping low (`low`), they hold on. True if they were on it.
         */
        public boolean carry(Entity e,boolean low){
            if(!on(e))return false;
            double turned=speed;if(turned==0)return true;
            var m=middle();double dx=e.getX()-m.x,dz=e.getZ()-m.z,cos=Math.cos(turned),sin=Math.sin(turned),nx=dx*cos-dz*sin,nz=dx*sin+dz*cos;
            e.setPos(m.x+nx,e.getY(),m.z+nz);
            e.setYRot(e.getYRot()+(float)Math.toDegrees(turned));e.setYHeadRot(e.getYHeadRot()+(float)Math.toDegrees(turned));
            // Outward from where they are now, harder the faster it goes and the further out they stand.
            double far=Math.sqrt(nx*nx+nz*nz);
            if(!low&&Math.abs(speed)>OUT&&far>1e-3){double out=FLING*speed*speed*far;e.push(nx/far*out,0,nz/far*out);}
            return true;
        }
        private void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putDouble("Angle",angle);t.putDouble("Speed",speed);}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
            super.loadAdditional(t,r);if(!t.contains("Angle"))return;
            double now=t.getDouble("Angle"),off=Mth.wrapDegrees(Math.toDegrees(now-angle));
            // A client that is nearly where the server is keeps what it was drawing from; one that is not starts again there.
            if(level==null||!level.isClientSide||Math.abs(off)>20)before=now;else before+=now-angle;
            angle=now;speed=t.getDouble("Speed");
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
