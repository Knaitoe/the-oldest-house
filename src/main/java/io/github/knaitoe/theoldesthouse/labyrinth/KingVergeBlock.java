package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.642, the brook's verge (KingBreath): nothing to see, laid over each brook's two rows but not its crossing. A thing cannot step
 * down into the brook or over it (a wall half again as high as a jump); a sort of thing wades it slowly; a line walks through. Only
 * readers meet it.
 */
public class KingVergeBlock extends Block {
    public static final MapCodec<KingVergeBlock> CODEC=simpleCodec(KingVergeBlock::new);
    private static final VoxelShape WALL=Block.box(0,0,0,16,24,16);
    public KingVergeBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return KingHedgeBlock.state(c)>=KingBreath.THING&&!KingHedgeBlock.queen(c)&&c instanceof EntityCollisionContext e&&e.getEntity() instanceof Player?WALL:Shapes.empty();}
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){
        if(e instanceof Player r&&KingBreath.of(r)==KingBreath.SORT&&!KingCrownItem.crowned(r))e.makeStuckInBlock(s,new Vec3(.5,.9,.5));
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
