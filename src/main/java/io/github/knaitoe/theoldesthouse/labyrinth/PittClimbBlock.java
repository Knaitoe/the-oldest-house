package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.207, the God of the Pitt: the two things in the deeper passages that are climbed, both in the climbable tag. Brad's fixed
 * ropes, hung free down the sinkhole's throat and the chimney's drops; and the rusted ladder bolted up the far wall of the
 * ledge's void to the hatch. The ladder has its back to its wall, as a ladder does.
 */
public final class PittClimbBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PittClimbBlock> CODEC=simpleCodec(PittClimbBlock::new);
    public enum Kind implements StringRepresentable {
        ROPE,LADDER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public PittClimbBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.ROPE));}
    public static BlockState of(Kind kind,Direction facing){return PittRegistry.CLIMB.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(PittRegistry.CLIMB.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND)==Kind.ROPE?Block.box(6,0,6,10,16,10):PittBlock.box(s.getValue(FACING),0,0,13,16,16,16);
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND)==Kind.ROPE?Shapes.empty():getShape(s,l,p,c);
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
