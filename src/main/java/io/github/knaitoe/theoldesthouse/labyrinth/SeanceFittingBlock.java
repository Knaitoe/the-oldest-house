package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** The drawing room's own furniture: the cupboard the family watches, and the three things a reader can open. */
public final class SeanceFittingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SeanceFittingBlock> CODEC=simpleCodec(SeanceFittingBlock::new);
    public enum Kind implements StringRepresentable { CABINET, LETTERS, SITTING, ALBUM;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    public SeanceFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABINET).setValue(OPEN,false));}
    public static BlockState of(Kind kind,Direction facing){return ClassicsRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,OPEN);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case CABINET->switch(s.getValue(FACING)){case SOUTH->Block.box(0,0,0,16,16,13);case EAST->Block.box(0,0,0,13,16,16);case WEST->Block.box(3,0,0,16,16,16);default->Block.box(0,0,3,16,16,16);};
        case LETTERS->Block.box(1,0,1,15,15.5,15);
        case SITTING->Block.box(2,0,2,14,15,14);
        case ALBUM->Block.box(1,0,1,15,16,15);};}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
