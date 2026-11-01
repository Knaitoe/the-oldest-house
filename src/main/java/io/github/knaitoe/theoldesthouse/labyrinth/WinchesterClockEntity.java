package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 0.4.220 (owner: the hall clock "could have animated arms"): the clock top's entity, holding nothing; its renderer draws the hands. */
public final class WinchesterClockEntity extends BlockEntity {
    public WinchesterClockEntity(BlockPos at,BlockState s){super(LiteraryRegistry.WINCHESTER_CLOCK.get(),at,s);}
}
