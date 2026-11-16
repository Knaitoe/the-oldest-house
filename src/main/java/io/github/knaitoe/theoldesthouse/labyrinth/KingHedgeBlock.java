package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.642, the hedges between the files of the King's country (KingBreath): a wall to a thing; to a sort of thing they give, slowly; a
 * line goes through as if they were not there. Everything that is not a reader is held by them.
 */
public class KingHedgeBlock extends Block {
    public static final MapCodec<KingHedgeBlock> CODEC=simpleCodec(KingHedgeBlock::new);
    public KingHedgeBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    static int state(CollisionContext c){return c instanceof EntityCollisionContext e&&e.getEntity() instanceof Player p?KingBreath.of(p):KingBreath.THING;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return state(c)>=KingBreath.THING?Shapes.block():Shapes.empty();}
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){
        if(e instanceof Player r&&KingBreath.of(r)==KingBreath.SORT)e.makeStuckInBlock(s,new Vec3(.35,.9,.35));
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
