package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.450 (the owner: "Custom signs for last orders"): one block of the league slate in Last orders, a slate four blocks by three
 * in an oak frame. {@link #PIECE} is which square of its picture a block shows, 0 at the top left; the top course is the heading
 * and each block under it is one square of the table, into which the game chalks a week ({@link #chalk}). Its facing is the way
 * the slate looks. Drawn by tools/draw_bar_art.py; the chalk by client/BarSlateRenderer.
 */
public final class BarSlateBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BarSlateBlock> CODEC=simpleCodec(BarSlateBlock::new);
    public static final int COLS=4,ROWS=3;
    public static final IntegerProperty PIECE=IntegerProperty.create("piece",0,COLS*ROWS-1);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public BarSlateBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PIECE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PIECE);}
    public static boolean is(BlockState s){return s.is(BarRegistry.SLATE.get());}
    public static BlockState of(int piece,Direction facing){return BarRegistry.SLATE.get().defaultBlockState().setValue(PIECE,piece).setValue(FACING,facing);}
    /** The way that is right for someone reading the slate. */
    public static Direction right(Direction facing){return facing.getCounterClockWise();}
    /** Where block {@code piece} hangs, from the slate's bottom left as someone reading it sees it. */
    public static BlockPos cell(BlockPos at,Direction facing,int piece){return at.relative(right(facing),piece%COLS).above(ROWS-1-piece/COLS);}
    /** Hang the whole slate, its bottom left block at {@code at} for someone reading it. */
    public static void hang(ServerLevel l,BlockPos at,Direction facing){
        for(int piece=0;piece<COLS*ROWS;piece++)BuildBlocks.set(l,cell(at,facing,piece),of(piece,facing),F);
    }
    /** Chalk three lines into the square at {@code at}; three empty lines wipe it. */
    public static void chalk(ServerLevel l,BlockPos at,String first,String second,String third){
        BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof BarSlateEntity e)e.chalk(l,first,second,third);});
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MarshBlock.turned(s.getValue(FACING),0,0,14,16,16,16);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BarSlateEntity(pos,state);}
}
