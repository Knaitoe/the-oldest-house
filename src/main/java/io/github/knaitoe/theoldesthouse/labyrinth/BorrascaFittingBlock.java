package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Fitted wood, metal, notices, filing drawers and conveyor. Their native collision matches their use. */
public final class BorrascaFittingBlock extends Block {
    public static final MapCodec<BorrascaFittingBlock> CODEC=simpleCodec(BorrascaFittingBlock::new);
    public enum Kind implements StringRepresentable { TIMBER,STEEL,RUST,PAPER,NOTICE,CARVING,DRAWERS,BELT,TRUCK,TRAIL,AGED_WOOD,TAPE,FENCE,POLISHED,PACK,PATCH,LOCK,LAMP,BREAKER,COUNTY,TRACKS,MARKER_ONE,MARKER_TWO,MARKER_THREE,MARKER_FOUR,LAMP_OFF;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);} }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public BorrascaFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TIMBER).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind k,Direction d){return BorrascaRegistry.FITTING.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return switch(s.getValue(KIND)){case PAPER,PATCH,TRACKS->box(1,0,1,15,1,15);case PACK->box(3,0,3,13,11,13);case TAPE,FENCE->s.getValue(FACING).getAxis()==Direction.Axis.Z?box(0,0,7,16,16,9):box(7,0,0,9,16,16);case NOTICE,CARVING,MARKER_ONE,MARKER_TWO,MARKER_THREE,MARKER_FOUR->switch(s.getValue(FACING)){case SOUTH->box(0,0,0,16,16,2);case NORTH->box(0,0,14,16,16,16);case EAST->box(0,0,0,2,16,16);default->box(14,0,0,16,16,16);};case BELT->box(0,0,0,16,11,16);default->Shapes.block();};}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return switch(s.getValue(KIND)){case PAPER,NOTICE,CARVING,PATCH,TRACKS,TAPE,LOCK,MARKER_ONE,MARKER_TWO,MARKER_THREE,MARKER_FOUR->Shapes.empty();default->getShape(s,l,at,c);};}
    /** Wire stops a body, but its open mesh does not hide the ridge searchlight. */
    @Override public VoxelShape getVisualShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return s.getValue(KIND)==Kind.FENCE?Shapes.empty():super.getVisualShape(s,l,at,c);}
}
