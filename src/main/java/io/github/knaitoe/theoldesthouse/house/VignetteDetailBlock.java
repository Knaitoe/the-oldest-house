package io.github.knaitoe.theoldesthouse.house;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Custom scene objects; mesh and rotated selection share exact dimensions. */
public final class VignetteDetailBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<VignetteDetailBlock> CODEC=simpleCodec(VignetteDetailBlock::new);
    public enum Kind implements StringRepresentable { BEAR, TRAIN, TOY_BLOCKS, DOLL, CHESS, NOTICE_BOARD, MAP_BOARD, MISSING_NOTICE, DIARY_STACK, FIELD_NOTEBOOK, FLASHLIGHT, CASSETTE, SEALED_BOX, BOOK_TRAY, SHADOW, CHILD_WALL;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public VignetteDetailBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BEAR));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static BlockState state(Kind kind,Direction facing){return HouseBlocks.VIGNETTE_DETAIL.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        double[][] boxes=switch(s.getValue(KIND)){
            case BEAR -> new double[][]{{4,0,4,12,8,12},{3,8,3,13,14,13},{2,12,4,5,16,7},{11,12,4,14,16,7},{1,1,5,4,7,10},{12,1,5,15,7,10}};
            case TRAIN -> new double[][]{{1,2,3,15,6,12},{1,6,4,6,11,11},{9,6,5,12,9,10},{10,9,6,12,13,8},{2,0,2,5,3,13},{11,0,2,14,3,13}};
            case TOY_BLOCKS -> new double[][]{{1,0,2,6,5,7},{7,0,6,12,5,11},{3,5,4,8,10,9},{10,0,1,15,5,6}};
            case DOLL -> new double[][]{{4,0,5,7,5,8},{9,0,5,12,5,8},{4,5,4,12,11,10},{5,11,4,11,16,10},{1,6,5,4,10,8},{12,6,5,15,10,8}};
            case CHESS -> new double[][]{{1,0,1,15,1,15},{1,1,1,15,1.2,15},{3,1.2,3,4,3,4},{11,1.2,11,12,4,12}};
            case NOTICE_BOARD -> new double[][]{{7,0,7,9,14,9},{1,10,7,15,25,9},{2,11,6.8,14,24,7}};
            case MAP_BOARD -> new double[][]{{7,0,7,9,14,9},{1,10,7,15,25,9},{2,11,6.8,14,24,7}};
            case MISSING_NOTICE -> new double[][]{{1,0,13,15,16,16},{2,1,12.8,14,15,13}};
            case DIARY_STACK -> new double[][]{{1,0,2,14,1,14},{3,1,1,15,2,12},{2,2,4,8,2.7,4.5}};
            case FIELD_NOTEBOOK -> new double[][]{{2,0,2,14,2,13},{3,2,3,13,2.3,12}};
            case FLASHLIGHT -> new double[][]{{2,0,5,12,3,8},{12,0,4,15,4,9}};
            case CASSETTE -> new double[][]{{2,0,3,14,2,11},{3,2,4,13,2.2,10}};
            case SEALED_BOX -> new double[][]{{1,0,1,15,11,15},{6,11,1,10,11.2,15}};
            case BOOK_TRAY -> new double[][]{{1,14,1,15,16,15},{2,0,2,4,14,4},{12,0,2,14,14,4},{2,0,12,4,14,14},{12,0,12,14,14,14}};
            case SHADOW -> new double[][]{{6,0,13,8,9,15},{9,0,13,11,9,15},{5,9,13,12,22,15},{6,22,13,11,28,15},{2,12,13,5,21,15},{12,12,13,15,21,15}};
            case CHILD_WALL -> new double[][]{{0,0,15.8,16,16,16}};
        };VoxelShape shape=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(var b:boxes){double x0=b[0],z0=b[2],x1=b[3],z1=b[5];for(int t=0;t<turns;t++){double nx0=16-z1,nx1=16-z0;z0=x0;z1=x1;x0=nx0;x1=nx1;}shape=Shapes.or(shape,Block.box(x0,b[1],z0,x1,b[4],z1));}return shape.optimize();
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
