package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Small architectural pieces, using the existing halls' plaster, floor and cable textures. */
public final class HallwayFittingBlock extends Block {
    public static final MapCodec<HallwayFittingBlock> CODEC = simpleCodec(HallwayFittingBlock::new);
    public enum Kind implements StringRepresentable {
        PLASTER, RUBBLE, SEAM, DEEP_STEP, CABLE;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);

    public HallwayFittingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(KIND, Kind.PLASTER).setValue(STAGE, 0));
    }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(KIND, STAGE); }
    public static BlockState of(Kind kind, int stage) {
        return OrdinaryHallRegistry.PASSAGE.get().defaultBlockState().setValue(KIND, kind).setValue(STAGE, stage);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos at, CollisionContext context) {
        return switch (state.getValue(KIND)) {
            case PLASTER -> box(0, 12, 0, 16, 16, 16);
            case RUBBLE -> box(0, 0, 0, 16, 3, 16);
            case SEAM -> Shapes.block();
            case DEEP_STEP -> box(0, 0, 0, 16, 8, 16);
            case CABLE -> box(0, 0, 6, 16, 2, 10);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos at, CollisionContext context) {
        // Loose chips and the fallen wire never place a new obstruction inside an occupied body.
        return switch (state.getValue(KIND)) {
            case RUBBLE, CABLE -> Shapes.empty();
            default -> getShape(state, level, at, context);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos at) { return Shapes.empty(); }
}
