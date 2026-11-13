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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The culvert's board-formed concrete (0.4.379), plain or stained where the water stands. Since 0.4.560 the tube is cast in
 * sections: where two meet, a joint ({@link #JOINT}) runs up the walls and across the ceiling, crusted white where water from
 * the drive above has come through it, and the ceiling's joints drip into the channel.
 */
public final class CulvertBlock extends Block {
    public static final MapCodec<CulvertBlock> CODEC=simpleCodec(CulvertBlock::new);
    public static final BooleanProperty JOINT=BooleanProperty.create("joint");
    public CulvertBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(JOINT,false));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(JOINT);}

    /** A joint with open air under it is in the ceiling: a drop gathers along the seam now and then, and somewhere one falls. */
    @Override public void animateTick(BlockState state,Level level,BlockPos at,RandomSource random){
        if(!state.getValue(JOINT))return;
        var below=at.below();if(!level.getBlockState(below).getCollisionShape(level,below).isEmpty())return;
        if(random.nextInt(3)==0)level.addParticle(ParticleTypes.DRIPPING_DRIPSTONE_WATER,at.getX()+.15+random.nextDouble()*.7,at.getY()-.06,at.getZ()+.5,0,0,0);
        if(random.nextInt(5)==0)level.playLocalSound(at.getX()+.5,at.getY()-1.1,at.getZ()+.5,SoundEvents.POINTED_DRIPSTONE_DRIP_WATER,SoundSource.AMBIENT,.2F+random.nextFloat()*.25F,.85F+random.nextFloat()*.3F,false);
    }
}
