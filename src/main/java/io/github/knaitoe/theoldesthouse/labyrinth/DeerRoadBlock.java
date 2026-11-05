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
 * 0.4.310, A Deer in the Road: the roadside's own things, drawn facing north by tools/draw_deer_road_art.py.
 * <ul>
 * <li>NOTICE: the county's notice on its post by the hatched shoulder (the first paper).</li>
 * <li>SIGN: the deer-crossing sign on its post, the top of the same post.</li>
 * <li>MARKER: the white marker post by the ditch, its reflector catching light.</li>
 * <li>GRATE: the culvert's grate in the ditch bank; stage 1, fingers through the bars.</li>
 * <li>TRIANGLE: the warning triangle set out on the road.</li>
 * <li>CARCASS: the doe's weight on the road, drawn by the scene; only its low collision is here.</li>
 * <li>LAMP: the caged bulb over the service door.</li>
 * </ul>
 */
public final class DeerRoadBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DeerRoadBlock> CODEC=simpleCodec(DeerRoadBlock::new);
    public enum Kind implements StringRepresentable {
        NOTICE,SIGN,MARKER,GRATE,TRIANGLE,CARCASS,LAMP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public DeerRoadBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.NOTICE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return DeerRoadRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(DeerRoadRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP?9:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected RenderShape getRenderShape(BlockState s){return s.getValue(KIND)==Kind.CARCASS?RenderShape.INVISIBLE:RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case NOTICE,SIGN,MARKER->Block.box(6,0,6,10,16,10);
            case GRATE->Block.box(0,0,0,16,16,16);
            case TRIANGLE->Block.box(3,0,6,13,9,10);
            case CARCASS->Block.box(0,0,0,16,6,16);
            case LAMP->Block.box(5,6,10,11,13,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.LAMP||s.getValue(KIND)==Kind.TRIANGLE?Shapes.empty():getShape(s,l,p,c);}
}
