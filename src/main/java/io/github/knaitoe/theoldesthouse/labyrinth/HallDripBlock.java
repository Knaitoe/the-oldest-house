package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.391, the cistern walk (OrdinaryHalls, docs/FOUR_HALLS_0_4_391.md): a stone of the vault that water comes through. Now and
 * then a drop gathers under it and falls, and is heard landing, in the water or on the walk. Drawn by
 * tools/draw_ordinary_hall_art.py, the vault's own stone.
 */
public final class HallDripBlock extends Block {
    public static final MapCodec<HallDripBlock> CODEC=simpleCodec(HallDripBlock::new);
    public HallDripBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override public void animateTick(BlockState s,Level l,BlockPos at,RandomSource random){
        if(random.nextInt(3)!=0||!l.getBlockState(at.below()).isAir())return;
        l.addParticle(ParticleTypes.DRIPPING_WATER,at.getX()+.3+random.nextDouble()*.4,at.getY()-.05,at.getZ()+.3+random.nextDouble()*.4,0,0,0);
        if(random.nextInt(4)==0){
            // Where it lands: the first thing under it, water or stone.
            var below=at.below();for(int i=0;i<12&&l.getBlockState(below).isAir();i++)below=below.below();
            boolean water=l.getFluidState(below).isSource();
            l.playLocalSound(below.getX()+.5,below.getY()+1,below.getZ()+.5,water?SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON:SoundEvents.POINTED_DRIPSTONE_DRIP_WATER,SoundSource.BLOCKS,.6F,.8F+random.nextFloat()*.3F,false);
        }
    }
}
