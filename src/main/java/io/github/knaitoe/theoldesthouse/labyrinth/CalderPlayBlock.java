package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.465, Bike night: the playground's equipment in Calder Park, each piece drawn facing north by tools/draw_calder_play_art.py.
 * This slice, the swings.
 * <ul>
 * <li>POST: a leg of a steel frame. Stage 0 its foot, with a stay each way the frame is pushed; 1 its shaft; 2 its top where a bar
 * ends on it (the bar goes off to the east of a post facing north); 3 its top where a bar runs on over it both ways.</li>
 * <li>BAR: the frame's top bar, across the way it faces. Stage 1 has the two hangers a swing's chains hang from.</li>
 * <li>CHAIN: a swing's two chains, one at each end of its seat, across the way it faces. Stage 1: gone, while somebody is on the
 * swing and the moving one is drawn in its place.</li>
 * <li>SWING: a belt seat on the ends of its chains. It faces as the porch's does: what it faces is behind whoever sits on it.
 * Stage 1: gone, while somebody is on it.</li>
 * </ul>
 * A frame stops a body; the chains and the seat do not.
 */
public final class CalderPlayBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CalderPlayBlock> CODEC=simpleCodec(CalderPlayBlock::new);
    public enum Kind implements StringRepresentable {
        POST,BAR,CHAIN,SWING;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** A post's pieces; a bar with hangers; a chain or a seat that is in use. */
    public static final int FOOT=0,SHAFT=1,END=2,THROUGH=3,HANGERS=1,GONE=1;
    public CalderPlayBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.POST).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return BikeNightRegistry.PLAY.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BikeNightRegistry.PLAY.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case POST->stage==END?Shapes.or(Block.box(6.5,0,6.5,9.5,4,9.5),MarshBlock.turned(f,8,1,6.5,16,4,9.5))
                :stage==THROUGH?Shapes.or(Block.box(6.5,0,6.5,9.5,4,9.5),MarshBlock.turned(f,0,1,6.5,16,4,9.5))
                :Block.box(6.5,0,6.5,9.5,16,9.5);
            case BAR->MarshBlock.turned(f,0,1,6.5,16,4,9.5);
            case CHAIN->stage==GONE?Shapes.empty():Shapes.or(MarshBlock.turned(f,1,0,7,3,16,9),MarshBlock.turned(f,13,0,7,15,16,9));
            case SWING->stage==GONE?Shapes.empty():Shapes.or(MarshBlock.turned(f,1,6,5.5,15,9,10.5),MarshBlock.turned(f,1,9,7,3,16,9),MarshBlock.turned(f,13,9,7,15,16,9));
        };
    }
    /** The chains and the seat give way to a body, as hanging things do. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CHAIN,SWING->Shapes.empty();default->getShape(s,l,p,c);};
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
