package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/** Painted enamel boards and brass door plates, mounted against their actual backing wall. */
public final class InstituteSignBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<InstituteSignBlock> CODEC=simpleCodec(InstituteSignBlock::new);
    public enum Kind implements StringRepresentable { POST,OUTGOING,HOURS,WRITING,ROOM_1,ROOM_2,ROOM_3,ROOM_4,ROOM_5,ROOM_6,ROOM_8,MISSING;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public InstituteSignBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.POST));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var kept=SHAPES_0767.get(s);if(kept==null){kept=shapeBuilt0767(s);SHAPES_0767.put(s,kept);}return kept;}
    // 0.4.767: the shape of each state, built once and kept; it was built again on every call (docs/REVIEW_REPAIRS_0_4_767.md).
    private static final java.util.Map<BlockState,VoxelShape> SHAPES_0767=new java.util.concurrent.ConcurrentHashMap<>();
    private VoxelShape shapeBuilt0767(BlockState s){return switch(s.getValue(FACING)){case NORTH->Block.box(0,3,15,16,13,16);case SOUTH->Block.box(0,3,0,16,13,1);case WEST->Block.box(15,3,0,16,13,16);default->Block.box(0,3,0,1,13,16);};}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction d){return NovelRegistry.SIGN.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
}
