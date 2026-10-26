package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The parked car's real native cells, clipped from the production model. */
public final class BackseatCarCells {
    public static final int COUNT=105,ROOT=24;
    private static final VoxelShape[][] SHAPES=shapes();
    private BackseatCarCells(){}
    public static BlockPos offset(int cell){return new BlockPos(cell/7%5-3,cell/35,cell%7-3);}
    public static VoxelShape shape(int cell,boolean open){return SHAPES[open?1:0][cell];}
    public static BackseatCarMesh.Box door(BackseatCarMesh.Box box,boolean open){
        // A full right-angle hinge gives the native collision the exact same
        // cuboids as the renderer, including the projecting exterior handle.
        return open?new BackseatCarMesh.Box(-39-box.z1(),box.y0(),box.x0()-1,-39-box.z0(),box.y1(),box.x1()-1,box.material()):box;
    }
    private static VoxelShape[][] shapes(){
        var shapes=new VoxelShape[2][COUNT];
        for(int open=0;open<2;open++){
            var boxes=new ArrayList<>(BackseatCarMesh.SHELL);boxes.addAll(BackseatCarMesh.STEERING);
            for(var b:BackseatCarMesh.DRIVER_DOOR)boxes.add(door(b,open==1));
            for(int cell=0;cell<COUNT;cell++){
                var at=offset(cell);var pieces=new ArrayList<VoxelShape>();
                for(var box:boxes){
                    double x0=Math.max(0,(box.x0()+8)/16D-at.getX()),x1=Math.min(1,(box.x1()+8)/16D-at.getX());
                    double y0=Math.max(0,box.y0()/16D-at.getY()),y1=Math.min(1,box.y1()/16D-at.getY());
                    double z0=Math.max(0,(box.z0()+8)/16D-at.getZ()),z1=Math.min(1,(box.z1()+8)/16D-at.getZ());
                    if(x1>x0&&y1>y0&&z1>z0)pieces.add(Shapes.box(x0,y0,z0,x1,y1,z1));
                }
                shapes[open][cell]=pieces.isEmpty()?Shapes.empty():Shapes.or(Shapes.empty(),pieces.toArray(VoxelShape[]::new)).optimize();
            }
        }
        return shapes;
    }
}
