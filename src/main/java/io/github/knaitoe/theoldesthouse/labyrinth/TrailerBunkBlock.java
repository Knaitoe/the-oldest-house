package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * The frame under an upper bunk: posts at its outer end, a ladder at the foot and side boards under the mattress.
 * Bunks always lie east-west in the trailer, foot to the west, so only the half is needed. Only the posts collide.
 */
public final class TrailerBunkBlock extends Block {
    public static final MapCodec<TrailerBunkBlock> CODEC=simpleCodec(TrailerBunkBlock::new);
    public enum Part implements StringRepresentable {
        FOOT,HEAD;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    private static final VoxelShape FOOT_POSTS=Shapes.or(Block.box(0,0,0,2,16,2),Block.box(0,0,14,2,16,16));
    private static final VoxelShape HEAD_POSTS=Shapes.or(Block.box(14,0,0,16,16,2),Block.box(14,0,14,16,16,16));
    public TrailerBunkBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(PART,Part.FOOT));}
    @Override public MapCodec<TrailerBunkBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(PART);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return state.getValue(PART)==Part.FOOT?FOOT_POSTS:HEAD_POSTS;}
    public static BlockState of(Part part){return GoatmanRegistry.BUNK.get().defaultBlockState().setValue(PART,part);}
}
