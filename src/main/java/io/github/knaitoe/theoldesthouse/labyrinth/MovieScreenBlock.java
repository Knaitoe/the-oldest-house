package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The block at the foot of the screen's black wall (0.4.102). It looks like the rest of the wall; its block entity is the
 * anchor the client draws the film, the projector's beam and the shadows from (MovieScreenRenderer).
 */
public final class MovieScreenBlock extends BaseEntityBlock {
    public static final MapCodec<MovieScreenBlock> CODEC=simpleCodec(MovieScreenBlock::new);
    public MovieScreenBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Screen(at,s);}
    public static final class Screen extends BlockEntity {
        public Screen(BlockPos at,BlockState s){super(MovieNightRegistry.SCREEN_ENTITY.get(),at,s);}
    }
}
