package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;

/** A single physical lid. Its occupied animation is saved by the native block entity. */
public final class WellCoverBlock extends BaseEntityBlock {
    public static final MapCodec<WellCoverBlock> CODEC=simpleCodec(WellCoverBlock::new);
    public static final BooleanProperty OPEN=TrapDoorBlock.OPEN;
    public WellCoverBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(OPEN,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(OPEN);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(OPEN)?box(0,16,0,16,32,3):box(0,13,0,16,16,16);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new WellCoverBlockEntity(p,s);}
}
