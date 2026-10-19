package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** A real piece of bark: the names and the scar belong to this reader's site. */
public final class BorrascaCarvingBlock extends BaseEntityBlock {
    public static final MapCodec<BorrascaCarvingBlock> CODEC=simpleCodec(BorrascaCarvingBlock::new);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,4);
    public BorrascaCarvingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(STAGE,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(STAGE);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){return new BorrascaCarvingEntity(at,s);}
}
