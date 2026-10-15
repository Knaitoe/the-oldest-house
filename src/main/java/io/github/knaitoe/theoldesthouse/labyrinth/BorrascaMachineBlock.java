package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    public static final class Machine extends BlockEntity {public Machine(BlockPos at,BlockState s){super(BorrascaRegistry.MACHINE_ENTITY.get(),at,s);}}
}
