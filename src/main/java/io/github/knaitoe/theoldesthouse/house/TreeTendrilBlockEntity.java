package io.github.knaitoe.theoldesthouse.house;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class TreeTendrilBlockEntity extends BlockEntity {
    public TreeTendrilBlockEntity(BlockPos pos,BlockState state){super(HouseBlockEntities.TREE_TENDRIL.get(),pos,state);}
}
