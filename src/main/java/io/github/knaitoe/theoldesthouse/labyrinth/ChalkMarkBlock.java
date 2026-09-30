package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
/** A painted surface, with no collision, light or loot. */
public final class ChalkMarkBlock extends Block {
    public static final MapCodec<ChalkMarkBlock> CODEC=simpleCodec(ChalkMarkBlock::new);
    public static final DirectionProperty FACE=BlockStateProperties.FACING;
    public static final DirectionProperty ARROW=DirectionProperty.create("arrow",Direction.Plane.HORIZONTAL);
    public ChalkMarkBlock(BlockBehaviour.Properties properties) {
        super(properties);registerDefaultState(stateDefinition.any().setValue(FACE,Direction.UP).setValue(ARROW,Direction.NORTH));
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,
            net.minecraft.core.BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return switch(state.getValue(FACE)) {
            case UP -> Block.box(0,0,0,16,.2,16);
            case DOWN -> Block.box(0,15.8,0,16,16,16);
            case NORTH -> Block.box(0,0,15.8,16,16,16);
            case SOUTH -> Block.box(0,0,0,16,16,.2);
            case EAST -> Block.box(0,0,0,.2,16,16);
            case WEST -> Block.box(15.8,0,0,16,16,16);
        };
    }
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACE,ARROW);}
}
