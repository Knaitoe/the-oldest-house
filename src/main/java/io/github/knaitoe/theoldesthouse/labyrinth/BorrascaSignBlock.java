package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.449: one block of a painted sign in Drisking ({@link BorrascaSigns}). {@link #PIECE} is which square of which sign's picture
 * it shows; its facing is the way the lettering looks. Nothing walks into it.
 */
public final class BorrascaSignBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BorrascaSignBlock> CODEC=simpleCodec(BorrascaSignBlock::new);
    public static final IntegerProperty PIECE=IntegerProperty.create("piece",0,BorrascaSigns.Sign.TOTAL-1);
    public BorrascaSignBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PIECE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PIECE);}
    public static boolean is(BlockState s){return s.is(BorrascaRegistry.SIGN.get());}
    public static boolean is(BlockState s,BorrascaSigns.Sign sign){return is(s)&&sign(s)==sign;}
    public static BorrascaSigns.Sign sign(BlockState s){return BorrascaSigns.Sign.of(s.getValue(PIECE));}
    /** Block {@code piece} of a sign (0 is its top left), looking {@code facing}. */
    public static BlockState of(BorrascaSigns.Sign sign,int piece,Direction facing){return BorrascaRegistry.SIGN.get().defaultBlockState().setValue(PIECE,sign.first()+piece).setValue(FACING,facing);}
    static int light(BlockState s){return sign(s).light;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(sign(s).mount){
            case WALL,DECAL->MarshBlock.turned(f,0,0,14,16,16,16);
            case WINDOW->MarshBlock.turned(f,0,1,0,16,15,2);
            case FREE->MarshBlock.turned(f,0,0,7,16,16,9);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
