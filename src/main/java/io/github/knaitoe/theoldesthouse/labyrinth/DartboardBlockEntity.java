package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.415: a dartboard, where the chalked score is drawn from (client/DartsClient). The game itself is the server's
 * ({@link DartBoards}), sent to everyone near enough to read it.
 */
public final class DartboardBlockEntity extends BlockEntity {
    public DartboardBlockEntity(BlockPos pos,BlockState state){super(BarRegistry.DARTBOARD_ENTITY.get(),pos,state);}
    /** The board and the chalked score on the wall beside it. */
    public AABB renderBox(){var p=getBlockPos();return new AABB(p).inflate(1.6,.8,1.6);}
}
