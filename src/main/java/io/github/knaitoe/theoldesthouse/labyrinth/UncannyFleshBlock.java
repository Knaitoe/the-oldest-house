package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Native full tissue cells without a block entity for each growing fold. */
public final class UncannyFleshBlock extends Block {
    public static final MapCodec<UncannyFleshBlock> CODEC=simpleCodec(UncannyFleshBlock::new);
    public static final IntegerProperty LAYER=IntegerProperty.create("layer",0,3);
    public UncannyFleshBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LAYER,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LAYER);}
    public static BlockState at(BlockPos p){return UncannyRegistry.FLESH.get().defaultBlockState().setValue(LAYER,Math.floorMod(p.getX()*7+p.getY()*3+p.getZ()*11,4));}
    public static boolean is(BlockState s){return s.is(UncannyRegistry.FLESH.get())||s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FLESH;}
}
