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

/** Movie Night's fixtures (0.4.102): a horn speaker on its bracket, the projector at the dock's end, a firework mortar. */
public final class MoviePropBlock extends Block {
    public static final MapCodec<MoviePropBlock> CODEC=simpleCodec(MoviePropBlock::new);
    public enum Kind implements StringRepresentable {
        SPEAKER,PROJECTOR,MORTAR;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public MoviePropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SPEAKER).setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction d){return MovieNightRegistry.PROP.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static boolean is(BlockState s,Kind k){return s.is(MovieNightRegistry.PROP.get())&&s.getValue(KIND)==k;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return switch(s.getValue(KIND)){case SPEAKER->box(3,3,3,13,13,13);case PROJECTOR->box(3,0,0,13,9,13);case MORTAR->box(2,0,2,14,10,14);};
    }
}
