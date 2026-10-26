package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** One hollow car, with native collision and a saved physical driver's seat. */
public final class BackseatCarBlock extends BaseEntityBlock {
    public static final MapCodec<BackseatCarBlock> CODEC=simpleCodec(BackseatCarBlock::new);
    public static final IntegerProperty CELL=IntegerProperty.create("cell",0,BackseatCarCells.COUNT-1);
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public BackseatCarBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(CELL,BackseatCarCells.ROOT).setValue(OPEN,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CELL,OPEN);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return BackseatCarCells.shape(s.getValue(CELL),s.getValue(OPEN));}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return getShape(s,l,at,c);}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return s.getValue(CELL)==BackseatCarCells.ROOT?new Car(at,s):null;}
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,BackseatRegistry.CAR_ENTITY.get(),(world,at,state,car)->car.tick());}
    public static @Nullable Car root(Level l,BlockPos at){var s=l.getBlockState(at);if(!s.is(BackseatRegistry.CAR.get()))return null;var b=at.subtract(BackseatCarCells.offset(s.getValue(CELL)));return l.getBlockEntity(b) instanceof Car c?c:null;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){var car=root(l,at);return p instanceof ServerPlayer reader&&car!=null&&car.click(reader,hit.getLocation())?InteractionResult.SUCCESS:InteractionResult.PASS;}
    /** Fresh-only construction. It never replenishes a removed original or replaces a used car. */
    public static boolean place(ServerLevel l,BlockPos root,UUID owner){
        if(l.getBlockEntity(root) instanceof Car car)return owner.equals(car.owner);
        if(!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(root.getX()-3,root.getY(),root.getZ()-3,root.getX()+2,root.getY()+3,root.getZ()+4),Entity::isAlive).isEmpty())return false;
        for(int i=0;i<BackseatCarCells.COUNT;i++){var at=root.offset(BackseatCarCells.offset(i));if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at))||l.getBlockEntity(at)!=null||!l.getBlockState(at).isAir())return false;}
        for(int i=0;i<BackseatCarCells.COUNT;i++)l.setBlock(root.offset(BackseatCarCells.offset(i)),BackseatRegistry.CAR.get().defaultBlockState().setValue(CELL,i),FLAGS);
        if(!(l.getBlockEntity(root) instanceof Car car))return false;car.owner=owner;car.sync();return true;
    }
    public static final class Car extends BlockEntity {
        public static final int PARKED=0,SEATED=1,CLOSING=2,LOCKED=3,REVEAL=4;
        public @Nullable UUID owner,seatId;
        public int phase=PARKED,clock;
        public long syncedAt;
        private float lockYaw,lockPitch;
        private long lastRattle=-40;
        private boolean audible;
        private int soundAge;
        public Car(BlockPos at,BlockState state){super(BackseatRegistry.CAR_ENTITY.get(),at,state);}
        public boolean mine(Player p){return owner!=null&&owner.equals(p.getUUID())&&p.isAlive()&&!p.isSpectator();}
        public @Nullable BackseatSeat seat(){return level instanceof ServerLevel l&&seatId!=null&&l.getEntity(seatId) instanceof BackseatSeat s?s:null;}
        public boolean committed(){return phase>=CLOSING;}
        public boolean board(ServerPlayer p){
            if(!mine(p)||phase!=PARKED||p.isPassenger()||p.distanceToSqr(worldPosition.getCenter())>36||!(level instanceof ServerLevel l))return false;
            if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(worldPosition)))return false;
            var seat=seat();
            if(seatId!=null&&seat==null)return false; // a saved original in an unloaded section is not a fresh seat
            if(seat==null){seat=BackseatRegistry.SEAT.get().create(l);if(seat==null)return false;seat.bind(worldPosition,p.getUUID());if(!l.addFreshEntity(seat))return false;seatId=seat.getUUID();}
            if(!p.startRiding(seat,true))return false;
            seat.positionRider(p);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();phase=SEATED;clock=0;sync();return true;
        }
        public boolean click(ServerPlayer p,Vec3 hit){
            if(!mine(p)||p.distanceToSqr(hit)>36)return false;
            // A stand-up and another click can arrive in the same native tick.
            // Reconcile the now-empty original anchor before deciding what the click means.
            if(phase==SEATED&&p.getVehicle()!=seat()){phase=PARKED;clock=0;sync();}
            if(phase==PARKED){var rel=hit.subtract(worldPosition.getCenter());return rel.x<-.45&&rel.z> -1.9&&rel.z<1.1&&board(p);}
            if(!(p.getVehicle() instanceof BackseatSeat s)||!s.root().equals(worldPosition))return false;
            if(phase==SEATED){phase=CLOSING;clock=0;sync();return true;}
            rattle(p);return true;
        }
        private void play(ServerPlayer p,SoundEvent sound,float volume){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()-.5,volume,1,p.getRandom().nextLong()));}
        private void stop(ServerPlayer p,String kind){p.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"backseat."+kind),SoundSource.BLOCKS));}
        public void rattle(ServerPlayer p){if(level==null||level.getGameTime()-lastRattle<20)return;lastRattle=level.getGameTime();play(p,BackseatRegistry.LOCK.get(),.6F);}
        private void ambient(ServerPlayer p){
            if(p.distanceToSqr(worldPosition.getCenter())>24*24){if(audible)for(String sound:new String[]{"radio","idle","wiper"})stop(p,sound);audible=false;return;}
            if(!audible){soundAge=0;audible=true;}
            if(soundAge%80==0)play(p,BackseatRegistry.IDLE.get(),.2F);
            if(phase<LOCKED){if(soundAge%171==0)play(p,BackseatRegistry.RADIO.get(),.14F);if(soundAge%48==0)play(p,BackseatRegistry.WIPER.get(),.25F);}
            soundAge=(soundAge+1)%13680;
        }
        private boolean door(boolean open){
            if(!(level instanceof ServerLevel l))return false;
            if(!open)for(var box:BackseatCarMesh.DRIVER_DOOR){
                var at=new AABB(worldPosition.getX()+.5+box.x0()/16D,worldPosition.getY()+box.y0()/16D,worldPosition.getZ()+.5+box.z0()/16D,worldPosition.getX()+.5+box.x1()/16D,worldPosition.getY()+box.y1()/16D,worldPosition.getZ()+.5+box.z1()/16D);
                if(!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,at,e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
            }
            // Changing a moving leaf never replaces cells another build or edit now owns.
            for(int i=0;i<BackseatCarCells.COUNT;i++){var at=worldPosition.offset(BackseatCarCells.offset(i));var s=l.getBlockState(at);if(!s.is(BackseatRegistry.CAR.get())||s.getValue(CELL)!=i)return false;}
            for(int i=0;i<BackseatCarCells.COUNT;i++){var at=worldPosition.offset(BackseatCarCells.offset(i));l.setBlock(at,l.getBlockState(at).setValue(OPEN,open),FLAGS);}
            return true;
        }
        public boolean turned(ServerPlayer p){
            float delta=Math.abs(Mth.wrapDegrees(p.getYRot()-lockYaw))+Math.abs(p.getXRot()-lockPitch);
            if(delta<8)return false;
            var eye=p.getEyePosition();var look=p.getLookAngle();
            for(var target:new Vec3[]{worldPosition.getCenter().add(BackseatCarMesh.MIRROR).add(0,-.5,0),worldPosition.getCenter().add(.65,.7,-.2)}){
                var to=target.subtract(eye);if(to.lengthSqr()>.0001&&look.dot(to.normalize())>Math.cos(Math.toRadians(28)))return true;
            }
            return false;
        }
        private void tick(){
            if(!(level instanceof ServerLevel l)||owner==null)return;var p=l.getServer().getPlayerList().getPlayer(owner);var seat=seat();
            if(p==null||p.serverLevel()!=l||!mine(p)){audible=false;return;}ambient(p);if(seat==null)return;
            if(phase==SEATED&&p.getVehicle()!=seat){phase=PARKED;clock=0;sync();return;}
            if(phase>=CLOSING&&p.getVehicle()!=seat){if(p.distanceToSqr(worldPosition.getCenter())<36&&!p.isPassenger())p.startRiding(seat,true);return;}
            if(p.getVehicle()!=seat)return;
            if(phase==CLOSING){clock++;if(clock>=6){if(door(false)){phase=LOCKED;lockYaw=p.getYRot();lockPitch=p.getXRot();play(p,BackseatRegistry.LATCH.get(),.65F);stop(p,"radio");stop(p,"wiper");rattle(p);}else phase=SEATED;clock=0;sync();}}
            else if(phase==LOCKED&&turned(p)){phase=REVEAL;clock=0;play(p,BackseatRegistry.RISE.get(),.8F);sync();}
            else if(phase==REVEAL){clock++;setChanged();}
        }
        public void sync(){setChanged();if(level instanceof ServerLevel){syncedAt=level.getGameTime();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(owner!=null)t.putUUID("Driver",owner);if(seatId!=null)t.putUUID("Seat",seatId);t.putInt("Phase",phase);t.putInt("Clock",clock);t.putFloat("LockYaw",lockYaw);t.putFloat("LockPitch",lockPitch);t.putLong("SyncedAt",level==null?syncedAt:level.getGameTime());}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);owner=t.hasUUID("Driver")?t.getUUID("Driver"):null;seatId=t.hasUUID("Seat")?t.getUUID("Seat"):null;phase=Mth.clamp(t.getInt("Phase"),PARKED,REVEAL);clock=Math.max(0,t.getInt("Clock"));lockYaw=t.getFloat("LockYaw");lockPitch=t.getFloat("LockPitch");syncedAt=t.getLong("SyncedAt");}
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
