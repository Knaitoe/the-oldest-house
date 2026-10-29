package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * The barn well's farm things (0.4.164, owner: "Improve clutter in barn_well shed"): a feed trough, milk cans, seed sacks, a
 * workbench with its vise, a pegboard of garden tools, a potting bench, a wheelbarrow, a pitchfork leant on a wall and a
 * harness on its peg. Each model faces north, its back to the south; FACING turns it. Drawn by tools/draw_barn_well.py.
 */
public final class FarmPropBlock extends Block {
    public static final MapCodec<FarmPropBlock> CODEC=simpleCodec(FarmPropBlock::new);
    public enum Kind implements StringRepresentable {
        TROUGH,MILK_CANS,SEED_SACKS,WORKBENCH,TOOL_RACK,POTTING_BENCH,WHEELBARROW,PITCHFORK,HARNESS;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public FarmPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TROUGH).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction facing){return NovelRegistry.FARM_PROP.get().defaultBlockState().setValue(KIND,k).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind k){return s.is(NovelRegistry.FARM_PROP.get())&&s.getValue(KIND)==k;}
    /** True for the things that hang on a wall: their back must lie flush against a solid face. */
    public static boolean onWall(Kind k){return k==Kind.TOOL_RACK||k==Kind.HARNESS;}
    private static double[][] boxes(Kind k){return switch(k){
        case TROUGH->new double[][]{{1,0,4,15,6,12}};
        case MILK_CANS->new double[][]{{3,0,3,8,12,8},{9,0,8,14,12,13}};
        case SEED_SACKS->new double[][]{{1,0,2,15,10,15}};
        case WORKBENCH->new double[][]{{0,0,1,16,15,16},{11,15,1,15,18,5}};
        case TOOL_RACK->new double[][]{{1,1,14,15,16,16}};
        case POTTING_BENCH->new double[][]{{0,0,3,16,15,13}};
        case WHEELBARROW->new double[][]{{3,0,1,13,8,16}};
        case PITCHFORK->new double[][]{{6,0,9,10,14,16}};
        case HARNESS->new double[][]{{4,2,13,12,12,16}};
    };}
    private static VoxelShape turned(Direction d,double[] a){
        double x0=a[0],y0=a[1],z0=a[2],x1=a[3],y1=a[4],z1=a[5];
        return switch(d){
            case SOUTH->box(16-x1,y0,16-z1,16-x0,y1,16-z0);
            case EAST->box(16-z1,y0,x0,16-z0,y1,x1);
            case WEST->box(z0,y0,16-x1,z1,y1,16-x0);
            default->box(x0,y0,z0,x1,y1,z1);};
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        VoxelShape out=Shapes.empty();for(var a:boxes(s.getValue(KIND)))out=Shapes.or(out,turned(s.getValue(FACING),a));return out;
    }
    /** What hangs on a wall or leans on one is not in the way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var k=s.getValue(KIND);return onWall(k)||k==Kind.PITCHFORK?Shapes.empty():getShape(s,l,at,c);
    }
}
