package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.478: a block a ridden bike rolls over as one sloping sheet: a plank ramp, the slide's chute.
 *
 * <p>A body stands on such a block as a row of shallow steps and walks up them without noticing. A bike cannot use the steps: going
 * up it would climb them a jolt at a time, and coming down it would leave each one and fall to the next, in the air the whole way, where
 * nothing drives it and nothing can be gained. So for a bike the steps are not there ({@code getCollisionShape} gives it nothing), and
 * RiddenBike keeps it on the sheet itself, at exactly the sheet's slope.
 */
public interface BikeSurface {
    /**
     * How high the sheet is over the floor of the block at {@code at}, under a point in the world: in blocks. It may be higher than the
     * block itself is. NaN where this block has no sheet.
     */
    double sheet(BlockState s,BlockPos at,double x,double z);
}
