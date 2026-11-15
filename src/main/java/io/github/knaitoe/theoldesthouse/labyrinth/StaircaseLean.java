package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.594: leaning over a rail of the great staircase's well (docs/STAIR_FALLING_LIGHT_0_4_594.md).
 *
 * <p>A reader on the stair stands a block back from the drop, behind a rail on a stone string. From there the stair they stand on
 * hides everything more than about ten blocks below, so a light let fall could not be watched. A reader who stands at a rail and
 * looks down therefore leans out over it: their view goes out past the rail's far face, as a head does over a balustrade. Their
 * body does not move. A light let go while leaning falls from the hand held out there ({@link StaircaseFallingLight#held}).
 *
 * <p>One rule for both sides: the client eases its view to this offset ({@code client.StaircaseLeanView}), and the server puts
 * what is let go at it.
 */
public final class StaircaseLean {
    /** How far past the rail's far face the eye goes, the most it drops, and how far above the rail's top it must stay. */
    public static final double PAST=.2,DROP=.25,OVER=.1;
    /** How far short of the rail's cell a reader may stand and still be at the rail, and the least look down (degrees) that leans. */
    public static final double NEAR=.15;
    public static final float LOOK=30;
    private StaircaseLean(){}

    /**
     * Where a reader's eye goes when they lean over the rail they face, as an offset from their own eye. Null when they are not at
     * a rail in the well, are not looking down, would not clear the rail's top, or have floor beyond the rail instead of a drop.
     */
    public static Vec3 over(BlockGetter level,BlockPos origin,Vec3 feet,double eyeHeight,float yaw,float pitch){
        if(origin==null||pitch<LOOK||!StaircaseFallingLight.inWell(origin,feet))return null;
        Direction d=Direction.fromYRot(yaw);boolean x=d.getAxis()==Direction.Axis.X;int step=d.getAxisDirection().getStep();
        // A reader pressed against a rail stands a little inside its cell.
        BlockPos cell=BlockPos.containing(feet.x,feet.y+.55,feet.z),rail=StairWell.isRail(level.getBlockState(cell))?cell:cell.relative(d);
        if(!StairWell.isRail(level.getBlockState(rail)))return null;
        BlockPos beyond=rail.relative(d);
        for(int y=1;y>=-3;y--){var at=beyond.above(y);if(!level.getBlockState(at).getCollisionShape(level,at).isEmpty())return null;}
        // The rail cell's near face, and how far short of it the reader stands (less than nothing when pressed against the bars).
        double near=(x?rail.getX():rail.getZ())+(step>0?0:1),gap=(near-(x?feet.x:feet.z))*step;
        if(gap>NEAR)return null;
        double clear=feet.y+eyeHeight-(rail.getY()+1)-OVER;
        if(clear<0)return null;
        double out=gap+1+PAST;
        return new Vec3(d.getStepX()*out,-Math.min(DROP,clear),d.getStepZ()*out);
    }
}
