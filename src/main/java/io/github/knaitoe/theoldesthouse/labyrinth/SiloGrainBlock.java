package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** The crust receives the real fall. Falling is the place's history, not a health payment. */
public final class SiloGrainBlock extends Block {
    public SiloGrainBlock(Properties p){super(p);}
    @Override public void fallOn(Level l,BlockState s,BlockPos at,Entity body,float distance){body.fallDistance=0;if(!l.isClientSide&&distance>1)l.playSound(null,at,SiloRegistry.GRAIN.get(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,.85F);}
}
