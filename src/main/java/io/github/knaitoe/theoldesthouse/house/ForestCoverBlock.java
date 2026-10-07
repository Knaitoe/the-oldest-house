package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** A real leafy half block: its underside is 1.5 blocks above the forest floor. */
public final class ForestCoverBlock extends Block {
    public static final MapCodec<ForestCoverBlock> CODEC=simpleCodec(ForestCoverBlock::new);
    private static final VoxelShape CANOPY=Block.box(0,8,0,16,16,16);
    public ForestCoverBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return CANOPY;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return CANOPY;}
}
