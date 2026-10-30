package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.173: the wide mirror over the bathroom's sinks, four blocks across and two high, hung flat on the wall. {@link #PART}
 * is the column (0 to 3, from the left as one faces the glass) plus four for the upper row; the lower left holds the glass's
 * reflection ({@code client.HouseReadReflection}). Shattered, it shows only cracks.
 */
public final class DareMirrorBlock extends BaseEntityBlock {
    public static final MapCodec<DareMirrorBlock> CODEC=simpleCodec(DareMirrorBlock::new);
    public static final IntegerProperty PART=IntegerProperty.create("part",0,7);
    public static final BooleanProperty SHATTERED=BooleanProperty.create("shattered");
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final int WIDTH=4,HEIGHT=2;
    public DareMirrorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(PART,0).setValue(SHATTERED,false));}
    public static BlockState of(Direction facing,int column,int row){return DareRegistry.MIRROR.get().defaultBlockState().setValue(FACING,facing).setValue(PART,column+row*WIDTH);}
    public static boolean anchor(BlockState s){return s.is(DareRegistry.MIRROR.get())&&s.getValue(PART)==0;}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART,SHATTERED);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Glass(p,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return DareFittingBlock.box(s.getValue(FACING),0,0,15,16,16,16);}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    /** The glass's place for its renderer. */
    public static final class Glass extends BlockEntity {
        public Glass(BlockPos p,BlockState s){super(DareRegistry.MIRROR_ENTITY.get(),p,s);}
    }
}
