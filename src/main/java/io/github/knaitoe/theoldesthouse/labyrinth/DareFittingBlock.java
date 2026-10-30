package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.173: the school's third floor and its girls' bathroom. Models face north as drawn and turn with {@link #FACING};
 * a fixture on a wall has its back to that wall. {@link #STAGE}: a two-block fixture's upper half is 1; a stall door
 * standing ajar is 2 and 3; the second bulletin board and the second sheet are 1.
 */
public final class DareFittingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DareFittingBlock> CODEC=simpleCodec(DareFittingBlock::new);
    public enum Kind implements StringRepresentable {
        LOCKER,CLASSROOM_DOOR,TROPHY,BULLETIN,POSTER,CLOCK,FOUNTAIN,HALL_LIGHT,BATH_LIGHT,PLAQUE,
        SINK,PARTITION,STALL_PANEL,STALL_DOOR,TOILET,BATHMAT,TOWELS,BIN,WINDOW,NOTE,SHEET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public DareFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.LOCKER).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return DareRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(DareRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** Which stages a kind is drawn in: the two halves, the stall door shut and ajar, the two boards and the two sheets. */
    public static int stages(Kind k){return switch(k){case LOCKER,CLASSROOM_DOOR,TROPHY,PARTITION,STALL_PANEL,BULLETIN,SHEET->2;case STALL_DOOR->4;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.HALL_LIGHT?14:0;}
    /** A box as drawn facing north, turned to the fixture's facing. */
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);boolean upper=(s.getValue(STAGE)&1)==1;
        return switch(s.getValue(KIND)){
            case LOCKER,CLASSROOM_DOOR,TROPHY->Shapes.block();
            case BULLETIN,POSTER,CLOCK,PLAQUE,TOWELS,WINDOW->box(f,1,1,15,15,15,16);
            case FOUNTAIN->box(f,3,6,7,13,13,16);
            case HALL_LIGHT,BATH_LIGHT->box(f,2,14,2,14,16,14);
            case SINK->Shapes.or(box(f,1,8,9,15,14,16),box(f,6,0,13,10,8,16));
            case PARTITION->box(f,7,upper?0:2,0,9,upper?12:16,16);
            case STALL_PANEL,STALL_DOOR->box(f,0,upper?0:2,7,16,upper?12:16,9);
            case TOILET->Shapes.or(box(f,3,0,4,13,9,16),box(f,3,9,12,13,15,16));
            case BATHMAT->Block.box(1,0,1,15,1,15);
            case BIN->Block.box(4,0,4,12,11,12);
            case NOTE->Block.box(5,0,5,11,1,11);
            case SHEET->Block.box(1,0,1,15,4,15);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case BULLETIN,POSTER,CLOCK,PLAQUE,TOWELS,WINDOW,HALL_LIGHT,BATH_LIGHT,BATHMAT,NOTE,SHEET->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return switch(s.getValue(KIND)){case LOCKER,CLASSROOM_DOOR,TROPHY->Shapes.block();default->Shapes.empty();};}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
