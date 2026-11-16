package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import java.util.List;
import net.minecraft.util.Mth;
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
 *
 * <p>0.4.652: at a landing's inner corner the drop lies only on the diagonal, past the rail that turns the corner: straight out on
 * either side the next flight's string runs on. A reader standing in the corner and facing it leans out over that rail instead
 * ({@link #corner}), between the two newels, which stand there only as high as the rail (docs/STAIRCASE_JUDGEMENT_0_4_652.md).
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
        var straight=straight(level,feet,eyeHeight,yaw);
        return straight!=null?straight:corner(level,feet,eyeHeight,yaw);
    }
    private static Vec3 straight(BlockGetter level,Vec3 feet,double eyeHeight,float yaw){
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

    /** How far (degrees) a reader may face from the diagonal and still face the corner. */
    public static final float CORNER=22;
    private static boolean open(BlockGetter level,BlockPos at){return level.getBlockState(at).getCollisionShape(level,at).isEmpty();}
    /**
     * 0.4.652: leaning out over the rail that turns a landing's inner corner. The reader stands in the corner cell and faces the
     * diagonal; the rail is the cell diagonally ahead, the two cells beside it (the newels) rise no higher than it, so the eye goes
     * over them, and beyond the rail on the diagonal is the drop. Null for anything else.
     */
    private static Vec3 corner(BlockGetter level,Vec3 feet,double eyeHeight,float yaw){
        double dx=-Mth.sin(yaw*Mth.DEG_TO_RAD),dz=Mth.cos(yaw*Mth.DEG_TO_RAD);
        if(Math.abs(Math.toDegrees(Math.atan2(Math.abs(dz),Math.abs(dx)))-45)>CORNER)return null;
        int sx=dx>0?1:-1,sz=dz>0?1:-1;
        BlockPos cell=BlockPos.containing(feet.x,feet.y+.55,feet.z),rail=cell.offset(sx,0,sz);
        if(!StairWell.isRail(level.getBlockState(rail)))return null;
        // The eye goes over the rail and between the two cells beside it: nothing stands above the rail's height there.
        for(var at:List.of(rail.above(),cell.offset(sx,1,0),cell.offset(0,1,sz),rail.offset(sx,1,0),rail.offset(0,1,sz)))if(!open(level,at))return null;
        BlockPos beyond=rail.offset(sx,0,sz);
        for(int y=1;y>=-3;y--)if(!open(level,beyond.above(y)))return null;
        // How far short of the corner the reader stands, along the diagonal: pressed into it, a body's half-width from each newel.
        double cx=cell.getX()+(sx>0?1:0),cz=cell.getZ()+(sz>0?1:0),gap=((cx-feet.x)*sx+(cz-feet.z)*sz)/Math.sqrt(2);
        if(gap>.3*Math.sqrt(2)+NEAR)return null;
        double clear=feet.y+eyeHeight-(rail.getY()+1)-OVER;
        if(clear<0)return null;
        double out=gap+Math.sqrt(2)+PAST,unit=1/Math.sqrt(2);
        return new Vec3(sx*unit*out,-Math.min(DROP,clear),sz*unit*out);
    }
}
