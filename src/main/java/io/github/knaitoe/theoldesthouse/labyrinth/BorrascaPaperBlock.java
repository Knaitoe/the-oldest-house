package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.451: one block of the papers on the front of Drisking General ({@link BorrascaPapers}): a LOST sheet, a square of the papered
 * front, or a quarter of the noticeboard. {@link #PIECE} is which square of which picture it shows; its facing is the way the paper
 * looks. {@link #GLASS} is a sheet taped to the outside of a window: it lies on the pane in the block behind it, which stands half a
 * block further back than a wall's boards do. Nothing walks into it.
 */
public final class BorrascaPaperBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BorrascaPaperBlock> CODEC=simpleCodec(BorrascaPaperBlock::new);
    public static final IntegerProperty PIECE=IntegerProperty.create("piece",0,BorrascaPapers.Sheet.TOTAL-1);
    public static final BooleanProperty GLASS=BooleanProperty.create("glass");
    public BorrascaPaperBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PIECE,0).setValue(GLASS,false));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PIECE,GLASS);}
    public static boolean is(BlockState s){return s.is(BorrascaRegistry.PAPER.get());}
    public static boolean is(BlockState s,BorrascaPapers.Sheet sheet){return is(s)&&sheet(s)==sheet;}
    public static BorrascaPapers.Sheet sheet(BlockState s){return BorrascaPapers.Sheet.of(s.getValue(PIECE));}
    /** Block {@code piece} of a sheet's picture (0 is its top left), looking {@code facing}; {@code glass} for one taped to a window. */
    public static BlockState of(BorrascaPapers.Sheet sheet,int piece,Direction facing,boolean glass){return BorrascaRegistry.PAPER.get().defaultBlockState().setValue(PIECE,sheet.first()+piece).setValue(FACING,facing).setValue(GLASS,glass);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MarshBlock.turned(s.getValue(FACING),0,0,15,16,16,16);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
