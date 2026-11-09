package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.414: a pool table's foot, where the balls are drawn from (client/PoolClient). The frame itself is the server's
 * ({@link PoolTables}), sent to everyone near enough to watch.
 */
public final class PoolTableBlockEntity extends BlockEntity {
    public PoolTableBlockEntity(BlockPos pos,BlockState state){super(BarRegistry.POOL_TABLE_ENTITY.get(),pos,state);}
    /** Both blocks of the table, so the balls are drawn while either end is in view. */
    public AABB renderBox(){var p=getBlockPos();return new AABB(p).inflate(1.2,.5,1.2);}
}
