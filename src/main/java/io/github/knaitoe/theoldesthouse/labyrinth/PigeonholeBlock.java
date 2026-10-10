package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** One numbered box in the institute's post room. What a box holds is each reader's own, so the block holds nothing. */
public final class PigeonholeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PigeonholeBlock> CODEC=simpleCodec(PigeonholeBlock::new);
    public static final IntegerProperty NUMBER=IntegerProperty.create("number",1,12);
    public PigeonholeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.WEST).setValue(NUMBER,1));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,NUMBER);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
