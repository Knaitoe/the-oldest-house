package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.310: the stopped sedan's real cells, clipped from DeerCarMesh for each set of open doors (three doors, eight sets). The
 * collision includes the glass; the shape sight is tested against (the visual shape) leaves the glass out, so a reader looking in
 * through a window sees in, and a reader the woman looks for through one is seen.
 */
public final class DeerCarCells {
    /** Seven across (the open doors reach two and a half blocks out), three high, five long; the root is the middle cell on the road. */
    public static final int COUNT=105,ROOT=47,MASKS=8;
    private static final VoxelShape[][] SOLID=shapes(true),SIGHT=shapes(false);
    private DeerCarCells(){}
    public static BlockPos offset(int cell){return new BlockPos(cell/15-3,cell%15/5,cell%5-2);}
    public static int cell(int x,int y,int z){return (x+3)*15+y*5+(z+2);}
    public static VoxelShape shape(int cell,int mask){return SOLID[mask&7][cell];}
    public static VoxelShape sight(int cell,int mask){return SIGHT[mask&7][cell];}
    private static VoxelShape[][] shapes(boolean glass){
        var shapes=new VoxelShape[MASKS][COUNT];
        for(int mask=0;mask<MASKS;mask++){
            var boxes=new ArrayList<>(DeerCarMesh.solid(mask,glass,0));boxes.addAll(DeerCarMesh.WHEEL);
            for(int cell=0;cell<COUNT;cell++){
                var at=offset(cell);var pieces=new ArrayList<VoxelShape>();
                for(var box:boxes){
                    double x0=Math.max(0,(box.x0()+8)/16D-at.getX()),x1=Math.min(1,(box.x1()+8)/16D-at.getX());
                    double y0=Math.max(0,box.y0()/16D-at.getY()),y1=Math.min(1,box.y1()/16D-at.getY());
                    double z0=Math.max(0,(box.z0()+8)/16D-at.getZ()),z1=Math.min(1,(box.z1()+8)/16D-at.getZ());
                    if(x1-x0>1e-4&&y1-y0>1e-4&&z1-z0>1e-4)pieces.add(Shapes.box(x0,y0,z0,x1,y1,z1));
                }
                shapes[mask][cell]=pieces.isEmpty()?Shapes.empty():Shapes.or(Shapes.empty(),pieces.toArray(VoxelShape[]::new)).optimize();
            }
        }
        return shapes;
    }
}
