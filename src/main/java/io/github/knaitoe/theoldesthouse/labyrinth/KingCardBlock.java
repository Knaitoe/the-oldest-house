package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.646, A sort of thing, part 6 (docs/SLEEPING_KING_0_4_646.md): one of the court's pack, a playing card stood on its edge, a block
 * and a half tall and as flat as a card. Standing, it bears a thing (a thing cannot get between two of them) and is nothing to anything
 * less, which goes between them edge-on (KingCourt). Fallen, it lies flat in the grass and bears nothing. Its face is the pips or the
 * picture on its front ({@link #FACE}); its back is the pack's.
 */
public class KingCardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<KingCardBlock> CODEC=simpleCodec(KingCardBlock::new);
    /** Two, three, four, five, six and eight of the pack (the jurors and the guard), then the knave, the queen and the king. */
    public static final IntegerProperty FACE=IntegerProperty.create("face",0,8);
    public static final BooleanProperty FALLEN=BooleanProperty.create("fallen");
    public static final int KNAVE=6,QUEEN=7,KING=8;
    public KingCardBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FACE,0).setValue(FALLEN,false));}
    public static BlockState of(int face,Direction facing,boolean fallen){return KingRegistry.CARD.get().defaultBlockState().setValue(FACE,face).setValue(FACING,facing).setValue(FALLEN,fallen);}
    public static boolean is(BlockState s){return s.is(KingRegistry.CARD.get());}
    public static boolean standing(BlockState s){return is(s)&&!s.getValue(FALLEN);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,FACE,FALLEN);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    /** Its face looks the way it faces; the card stands across the middle of its block. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(s.getValue(FALLEN))return Block.box(0,0,0,16,1,16);
        return s.getValue(FACING).getAxis()==Direction.Axis.Z?Block.box(2,0,7,14,24,9):Block.box(7,0,2,9,24,14);
    }
    /** A thing cannot get past a standing card; anything less goes between them edge-on. Everything that is not a reader is a thing. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(s.getValue(FALLEN)||KingHedgeBlock.state(c)<KingBreath.THING)return Shapes.empty();
        // The cards stand shoulder to shoulder: to a thing the row is a wall the width of the block, not twelve pixels with gaps.
        return s.getValue(FACING).getAxis()==Direction.Axis.Z?Block.box(0,0,7,16,24,9):Block.box(7,0,0,9,24,16);
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
