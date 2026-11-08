package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.389, the door that follows (TrialRooms, docs/FOLLOWING_DOOR_0_4_389.md): a tall mirror hung flat on a wall, two blocks
 * high. {@link #FACING} is the way the glass faces; the lower block holds the glass's reflection, a true plane mirror
 * ({@code client.HouseReadReflection}), so a reader can watch the room behind them in it.
 */
public final class TrialMirrorBlock extends BaseEntityBlock {
    public static final MapCodec<TrialMirrorBlock> CODEC=simpleCodec(TrialMirrorBlock::new);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty UPPER=BooleanProperty.create("upper");
    public TrialMirrorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(UPPER,false));}
    public static BlockState of(Direction facing,boolean upper){return TrialRegistry.MIRROR.get().defaultBlockState().setValue(FACING,facing).setValue(UPPER,upper);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,UPPER);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(UPPER)?null:new Glass(p,s);}
    /** The plate against the wall behind the glass, two pixels deep. */
    public static VoxelShape plate(Direction facing){return switch(facing){
        case NORTH->box(0,0,14,16,16,16);case EAST->box(0,0,0,2,16,16);case WEST->box(14,0,0,16,16,16);default->box(0,0,0,16,16,2);};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return plate(s.getValue(FACING));}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    /** The glass's place, for its renderer. */
    public static final class Glass extends BlockEntity {
        public Glass(BlockPos p,BlockState s){super(TrialRegistry.MIRROR_ENTITY.get(),p,s);}
    }
}
