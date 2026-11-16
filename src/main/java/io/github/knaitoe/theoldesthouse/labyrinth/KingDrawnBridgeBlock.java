package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.645, the drawn bridge over the brook beyond the fifth square (KingCaterpillar): a drawing of a footbridge, always ruled. A drawing
 * will not bear a thing, so a thing goes through it into the brook and is set back on the bank (KingSleep); a sort of thing or a line
 * walks over it. Everything that is not a reader is a thing to it.
 */
public class KingDrawnBridgeBlock extends Block {
    public static final MapCodec<KingDrawnBridgeBlock> CODEC=simpleCodec(KingDrawnBridgeBlock::new);
    public KingDrawnBridgeBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return KingHedgeBlock.state(c)>=KingBreath.THING?Shapes.empty():Shapes.block();}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
