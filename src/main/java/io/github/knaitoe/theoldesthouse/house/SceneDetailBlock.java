package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Small authored clutter. Selection follows the mesh; it never obstructs a story route. */
public final class SceneDetailBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SceneDetailBlock> CODEC=simpleCodec(SceneDetailBlock::new);
    public enum Kind implements StringRepresentable { BOOKS, TEA_SET, SHOES, BLANKET, SATCHEL, TOOLS, BOTTLES, VASE, FEED_SACK, FILE_TRAY, TOWELS, COAT, CRATE, ROPE_COIL, CROCK, TABLE_LAMP, CLOCK, TOYS, DISH_RACK, FRAME, INK_PAPERS, MEDICAL_TRAY, DUSTPAN, HYMNALS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean wall(){return this==COAT||this==CLOCK||this==FRAME;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public SceneDetailBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOOKS));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static BlockState state(Kind kind,Direction facing){return HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean supported(BlockGetter level,BlockPos at,BlockState state){
        var facing=state.getValue(FACING);var kind=state.getValue(KIND);
        BlockPos support=kind.wall()?at.relative(facing.getOpposite()):at.below();
        return level.getBlockState(support).isFaceSturdy(level,support,kind.wall()?facing:Direction.UP);
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        double[][] boxes=switch(s.getValue(KIND)) {
            case BOOKS -> new double[][]{{2,0,3,14,2,12},{3,2,4,13,4,11},{1,4,2,13,6,11},{2,6,3,12,8,10},{3,8,4,14,10,12}};
            case TEA_SET -> new double[][]{{1,0,2,15,1,14},{3,1,4,7,5,8},{7,2,5,8,4,7},{10,1,8,14,5,12},{9,2,9,10,4,11}};
            case SHOES -> new double[][]{{2,0,3,6,3,13},{9,0,1,13,3,11},{2,0,3,6,1,13},{9,0,1,13,1,11}};
            case BLANKET -> new double[][]{{2,0,2,14,3,13},{3,3,3,13,5,12},{2,5,2,14,7,13}};
            case SATCHEL -> new double[][]{{3,0,3,13,9,12},{5,9,5,11,12,7},{7,4,2,9,6,3}};
            case TOOLS -> new double[][]{{1,0,2,15,3,13},{3,3,5,12,4,7},{10,4,4,13,6,8},{3,3,9,13,5,10}};
            case BOTTLES -> new double[][]{{2,0,4,6,8,8},{3,8,5,5,10,7},{9,0,7,14,7,12},{10,7,8,13,9,11}};
            case VASE -> new double[][]{{5,0,5,11,7,11},{7,7,7,9,14,9},{4,10,6,7,14,9},{9,9,5,12,12,8},{7,12,7,11,15,11}};
            case FEED_SACK -> new double[][]{{2,0,2,14,9,14},{5,9,5,11,11,11},{4,2,1,12,6,2}};
            case FILE_TRAY -> new double[][]{{1,0,2,15,1,14},{2,1,3,14,3,13},{1,1,2,2,5,14},{14,1,2,15,5,14},{3,3,4,12,4,12}};
            case TOWELS -> new double[][]{{2,0,2,14,3,13},{3,3,3,13,5,12},{2,5,2,14,7,13}};
            case COAT -> new double[][]{{7,12,14,9,15,16},{4,2,13,12,13,16},{2,6,12,4,12,15},{12,6,12,14,12,15}};
            case CRATE -> new double[][]{{1,0,1,15,9,15},{3,9,3,10,11,12},{10,9,5,13,13,10}};
            case ROPE_COIL -> new double[][]{{2,0,3,14,2,5},{2,0,11,14,2,13},{2,0,5,4,2,11},{12,0,5,14,2,11},{4,1,5,12,3,7},{4,1,9,12,3,11}};
            case CROCK -> new double[][]{{4,0,4,12,9,12},{3,9,3,13,10,13},{7,10,7,9,12,9}};
            case TABLE_LAMP -> new double[][]{{3,0,3,13,1,13},{7,1,7,9,9,9},{3,9,3,13,14,13},{5,14,5,11,15,11}};
            case CLOCK -> new double[][]{{3,3,13,13,14,16},{4,4,12,12,13,13},{7,8,11.5,8,12,12},{8,7,11.5,11,8,12}};
            case TOYS -> new double[][]{{2,0,3,6,4,7},{6,0,5,10,4,9},{10,0,2,14,4,6},{4,4,4,8,7,8}};
            case DISH_RACK -> new double[][]{{1,0,1,15,1,15},{1,1,1,2,5,15},{14,1,1,15,5,15},{4,1,3,5,8,12},{7,1,3,8,8,12},{10,1,3,11,8,12}};
            case FRAME -> new double[][]{{2,2,13,14,14,16},{3,3,12,13,13,13},{5,4,11.5,8,9,12},{8,6,11.5,11,12,12}};
            case INK_PAPERS -> new double[][]{{1,0,2,13,0.5,14},{3,0.5,1,15,1,12},{3,1,4,9,1.2,4.5},{11,1,7,14,4,10},{4,1,10,12,1.5,11}};
            case MEDICAL_TRAY -> new double[][]{{1,0,2,15,1,14},{2,1,3,7,3,8},{10,1,4,13,6,7},{3,1,10,12,2,12}};
            case DUSTPAN -> new double[][]{{1,0,4,9,1,13},{9,0,7,15,2,9},{2,1,5,8,2,6}};
            case HYMNALS -> new double[][]{{2,0,3,14,3,12},{3,1,4,13,2,11},{2,3,3,14,4,12},{7,4,5,9,4.3,10}};
        };
        int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        VoxelShape shape=Shapes.empty();
        for(var box:boxes){double x0=box[0],z0=box[2],x1=box[3],z1=box[5];
            for(int i=0;i<turns;i++){double a=16-z1,b=16-z0;z0=x0;z1=x1;x0=a;x1=b;}
            shape=Shapes.or(shape,Block.box(x0,box[1],z0,x1,box[4],z1));
        }
        return shape.optimize();
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
