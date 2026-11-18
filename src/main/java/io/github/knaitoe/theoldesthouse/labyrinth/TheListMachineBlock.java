package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.701, The list (docs/THE_LIST_0_4_701.md): the pasture's two machines. The block draws what stands still (the windmill's head on
 * its mast, the pumpjack's saddle on its Samson post); its renderer draws what moves (the wheel, the walking beam, the cranks, the pitman
 * arms and the rod) from the level's time (TheListMachines). 0.4.716: a pumpjack's block entity keeps its brake (Turning).
 */
public final class TheListMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<TheListMachineBlock> CODEC=simpleCodec(TheListMachineBlock::new);
    public enum Machine implements StringRepresentable {
        /** CABLE is never placed: it is the pumpjack's bridle, which the renderer stretches from the carrier bar to the horsehead. 0.4.716:
         * nor is BRAKE, the brake's lever on the gearbox, which the renderer turns on its pivot. */
        WINDMILL,PUMPJACK,CABLE,BRAKE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Machine> MACHINE=EnumProperty.create("machine",Machine.class);
    public TheListMachineBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(MACHINE,Machine.WINDMILL));}
    public static BlockState of(Machine m,Direction facing){return TheListRegistry.MACHINE.get().defaultBlockState().setValue(MACHINE,m).setValue(FACING,facing);}
    public static boolean is(BlockState s,Machine m){return s.is(TheListRegistry.MACHINE.get())&&s.getValue(MACHINE)==m;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,MACHINE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return s.getValue(MACHINE)==Machine.WINDMILL?MarshBlock.turned(f,4,2,2,12,13,14):MarshBlock.turned(f,0,0,5,16,19,11);
    }
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Turning(at,s);}
    /**
     * 0.4.716 (docs/THE_LIST_0_4_716.md): a pumpjack's brake. Running, the well keeps the level's time and `offset` ticks more;
     * stopped (the brake on, or stalled on something in the weight's way), it is held at the tick `stop`, and let go it runs on from
     * there. Sent to everyone with the block and never saved, so that a server started again finds every well running.
     */
    public static final class Turning extends BlockEntity {
        double stop=-1,offset;boolean braked,strain;
        /** Drawn only: when the lever last moved, and from where. */
        long leverAt=Long.MIN_VALUE;float leverFrom=LEVER_OFF;
        /** The lever's angle on its pivot, back toward the motor: off, and pulled up on. */
        public static final float LEVER_OFF=.8F,LEVER_ON=-.26F;
        public Turning(BlockPos at,BlockState s){super(TheListRegistry.MACHINE_ENTITY.get(),at,s);}
        /** The well's own tick now. */
        public double tick(long time,float partial){return stop>=0?stop:time+offset+partial;}
        public boolean running(){return stop<0;}
        public boolean braked(){return braked;}
        /** The motor labouring against a stall: the machine shudders where it stands. */
        public boolean strain(){return strain;}
        /** The brake on, the well held where it stands. */
        public void brake(long time){if(stop<0)stop=time+offset;braked=true;strain=false;sync();}
        /** Held at a tick of its own: the weight down on what is in its way, the motor straining or not. */
        public void stall(double tick,boolean strain){stop=tick;this.strain=strain;braked=false;sync();}
        /** Let go: it runs on from where it stood. */
        public void run(long time){if(stop>=0){offset=stop-time;stop=-1;}braked=false;strain=false;sync();}
        void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        /** The lever's angle now, swung over four ticks from where it was. */
        public float lever(long time,float partial){
            float to=braked?LEVER_ON:LEVER_OFF,f=Math.max(0,Math.min(1,(time+partial-leverAt)/4F));
            return leverFrom+(to-leverFrom)*f;
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){
            var t=new CompoundTag();t.putDouble("Stop",stop);t.putDouble("Offset",offset);t.putBoolean("Brake",braked);t.putBoolean("Strain",strain);return t;
        }
        @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
            super.loadAdditional(t,r);if(!t.contains("Stop"))return;
            long now=level==null?0:level.getGameTime();float was=lever(now,0);boolean had=braked;
            stop=t.getDouble("Stop");offset=t.getDouble("Offset");braked=t.getBoolean("Brake");strain=t.getBoolean("Strain");
            if(had!=braked){leverFrom=was;leverAt=now;}
        }
    }
}
