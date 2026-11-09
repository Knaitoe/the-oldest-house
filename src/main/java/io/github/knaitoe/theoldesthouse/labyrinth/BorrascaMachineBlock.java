package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;

/** The source has an actual position and a saved power switch. Its rollers are native animated geometry. */
public final class BorrascaMachineBlock extends BaseEntityBlock {
    public static final MapCodec<BorrascaMachineBlock> CODEC=simpleCodec(BorrascaMachineBlock::new);
    public static final BooleanProperty RUNNING=BooleanProperty.create("running");
    public BorrascaMachineBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(RUNNING,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(RUNNING);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Machine(at,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return createTickerHelper(type,BorrascaRegistry.MACHINE_ENTITY.get(),Machine::tick);}
    public static final class Machine extends BlockEntity {
        private float angle,previous,speed;
        /** 0.4.439: how far it has turned, never wrapped, so that its arms and hoops can turn at their own rates (client.BorrascaOrrery). */
        private double run,runBefore;
        public Machine(BlockPos at,BlockState s){super(BorrascaRegistry.MACHINE_ENTITY.get(),at,s);speed=s.getValue(RUNNING)?.12F:0;}
        static void tick(Level l,BlockPos at,BlockState s,Machine m){m.previous=m.angle;m.speed=net.minecraft.util.Mth.approach(m.speed,s.getValue(RUNNING)?.12F:0,.004F);m.angle+=m.speed;m.runBefore=m.run;m.run+=m.speed;if(m.angle>=Math.PI*2){m.angle-=(float)(Math.PI*2);m.previous-=(float)(Math.PI*2);}if(!l.isClientSide&&l.getGameTime()%20==0)m.setChanged();}
        public float angle(float partial){return net.minecraft.util.Mth.lerp(partial,previous,angle);}
        public double run(float partial){return net.minecraft.util.Mth.lerp(partial,runBefore,run);}
        /** How fast it runs now: 1 at full speed, 0 stopped. */
        public float power(){return speed/.12F;}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putFloat("Angle",angle);t.putFloat("Speed",speed);}
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);angle=t.getFloat("Angle");previous=angle;run=runBefore=run-run%(Math.PI*2)+angle;speed=t.getFloat("Speed");}
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    }
}
