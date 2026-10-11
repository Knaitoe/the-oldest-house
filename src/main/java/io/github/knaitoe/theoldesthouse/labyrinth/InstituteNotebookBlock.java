package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;

/** An open notebook, either resting on a table or with its own reading table underneath. */
public final class InstituteNotebookBlock extends BaseEntityBlock {
    public static final MapCodec<InstituteNotebookBlock> CODEC=simpleCodec(InstituteNotebookBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty TABLE=BooleanProperty.create("table");
    private static final VoxelShape BOOK=Block.box(3,0,2,13,2,14),READING=Shapes.or(Block.box(0,12,0,16,14,16),Block.box(2,0,2,4,12,4),Block.box(12,0,2,14,12,4),Block.box(2,0,12,4,12,14),Block.box(12,0,12,14,12,14),Block.box(3,14,2,13,16,14));
    public InstituteNotebookBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(TABLE,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,TABLE);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(s.getValue(FACING).getAxis()==Direction.Axis.Z)return s.getValue(TABLE)?READING:BOOK;
        var turnedBook=Block.box(2,0,3,14,2,13);return s.getValue(TABLE)?Shapes.or(Block.box(0,12,0,16,14,16),Block.box(2,0,2,4,12,4),Block.box(12,0,2,14,12,4),Block.box(2,0,12,4,12,14),Block.box(12,0,12,14,12,14),Block.box(2,14,3,14,16,13)):turnedBook;
    }
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new InstituteNotebookBlockEntity(p,s);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(boolean table,Direction facing){return NovelRegistry.WARD_NOTEBOOK.get().defaultBlockState().setValue(TABLE,table).setValue(FACING,facing);}
}
