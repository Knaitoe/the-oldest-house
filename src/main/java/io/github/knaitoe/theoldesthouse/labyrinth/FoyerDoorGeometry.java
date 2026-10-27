package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** One pair of native cuboids supplies the visible leaf, picking and physical passage. */
public final class FoyerDoorGeometry {
    private FoyerDoorGeometry(){}
    public static double[][] boxes(boolean open){
        double[][] out={{0,0,6,16,16,10},{3,2,10.02,13,14,10.25}};
        if(open)for(var q:out)open(q);
        return out;
    }
    private static void open(double[] q){double x=q[0],X=q[3];q[0]=q[2]-7.2;q[3]=q[5]-7.2;q[2]=8.8-X;q[5]=8.8-x;}
    public static double[] mirror(boolean opened){double[] q={0,0,10.3,1,16,10.5};if(opened)open(q);return q;}
    public static VoxelShape shape(BlockState state){
        var out=Shapes.empty();int turns=switch(state.getValue(UncannyPropBlock.FACING)){case EAST->1;case NORTH->2;case WEST->3;default->0;};
        for(var q:boxes(state.getValue(UncannyPropBlock.STAGE)>=2)){
            for(int n=0;n<turns;n++){double x=q[0],X=q[3];q[0]=q[2];q[3]=q[5];q[2]=16-X;q[5]=16-x;}
            out=Shapes.or(out,Block.box(q[0],q[1],q[2],q[3],q[4],q[5]));
        }return out.optimize();
    }
}
