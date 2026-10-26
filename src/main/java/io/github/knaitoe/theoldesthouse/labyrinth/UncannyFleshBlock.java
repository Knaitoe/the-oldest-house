package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Native full tissue cells without a block entity for each growing fold. */
public final class UncannyFleshBlock extends Block {
    public static final net.minecraft.world.level.block.SoundType SOUNDS=new net.minecraft.world.level.block.SoundType(.75F,1,net.minecraft.sounds.SoundEvents.SLIME_BLOCK_BREAK,net.minecraft.sounds.SoundEvents.SLIME_BLOCK_STEP,net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE,net.minecraft.sounds.SoundEvents.SLIME_BLOCK_HIT,net.minecraft.sounds.SoundEvents.SLIME_BLOCK_FALL){@Override public net.minecraft.sounds.SoundEvent getBreakSound(){return CellarSounds.tear();}};
    public static final MapCodec<UncannyFleshBlock> CODEC=simpleCodec(UncannyFleshBlock::new);
    public static final IntegerProperty LAYER=IntegerProperty.create("layer",0,7);
    public UncannyFleshBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LAYER,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LAYER);}
    public static BlockState at(BlockPos p){
        int layer=Math.floorMod(p.getX()*7+p.getY()*3+p.getZ()*11,4);
        if(p.getZ()==-21&&p.getY()==-2&&Math.abs(p.getX())==1)layer=p.getX()<0?4:5;
        if(p.equals(new BlockPos(0,-3,-21)))layer=6;
        if(p.equals(new BlockPos(0,-2,-20)))layer=7;
        return UncannyRegistry.FLESH.get().defaultBlockState().setValue(LAYER,layer);
    }
    public static boolean is(BlockState s){return s.is(UncannyRegistry.FLESH.get())||s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FLESH;}
}
