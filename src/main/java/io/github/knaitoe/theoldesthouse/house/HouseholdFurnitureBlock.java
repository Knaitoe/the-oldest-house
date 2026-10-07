package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Small ordinary furnishings. Visual cubes and collision share the same authored dimensions. */
public final class HouseholdFurnitureBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HouseholdFurnitureBlock> CODEC = simpleCodec(HouseholdFurnitureBlock::new);
    public enum Kind implements StringRepresentable {
        CANE_CHAIR(true, 8), KITCHEN_STOOL(true, 8), GREEN_ARMCHAIR(true, 8), FLORAL_ARMCHAIR(true, 8),
        BLUE_SOFA(true, 8), FOOTSTOOL(true, 6), WALNUT_DESK(false, 0), FORMICA_TABLE(false, 0),
        BEDSIDE_TABLE(false, 0), CHEST_OF_DRAWERS(false, 0), RADIATOR(false, 0), WASHING_MACHINE(false, 0), READING_DESK(false, 0);
        public final boolean seat;
        public final int seatHeight;
        Kind(boolean seat, int height) { this.seat = seat; this.seatHeight = height; }
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    private static final Map<Kind, Map<Direction, VoxelShape>> SHAPES = new EnumMap<>(Kind.class);
    static {
        for (Kind kind : Kind.values()) {
            double[][] boxes = switch (kind) {
                case CANE_CHAIR -> new double[][]{{2,7,2,14,9,14},{2,0,2,4,7,4},{12,0,2,14,7,4},{2,0,12,4,16,14},{12,0,12,14,16,14},{4,10,12,12,16,14}};
                case KITCHEN_STOOL -> new double[][]{{2,7,2,14,9,14},{3,0,3,5,7,5},{11,0,3,13,7,5},{3,0,11,5,7,13},{11,0,11,13,7,13}};
                case GREEN_ARMCHAIR, FLORAL_ARMCHAIR -> new double[][]{{2,2,2,14,8,14},{2,8,11,14,16,15},{0,6,2,3,12,14},{13,6,2,16,12,14},{2,0,3,4,2,5},{12,0,3,14,2,5},{2,0,12,4,2,14},{12,0,12,14,2,14}};
                case BLUE_SOFA -> new double[][]{{0,2,2,16,8,14},{0,8,11,16,15,15},{1,0,3,3,2,5},{13,0,3,15,2,5},{1,0,12,3,2,14},{13,0,12,15,2,14}};
                case FOOTSTOOL -> new double[][]{{2,3,2,14,7,14},{3,0,3,5,3,5},{11,0,3,13,3,5},{3,0,11,5,3,13},{11,0,11,13,3,13}};
                case WALNUT_DESK, READING_DESK -> new double[][]{{0,14,0,16,16,16},{1,0,2,7,14,14},{12,0,2,14,14,4},{12,0,12,14,14,14},{7,11,11,14,14,14}};
                case FORMICA_TABLE -> new double[][]{{0,14,0,16,16,16},{2,0,2,4,14,4},{12,0,2,14,14,4},{2,0,12,4,14,14},{12,0,12,14,14,14}};
                case BEDSIDE_TABLE -> new double[][]{{2,0,2,4,5,4},{12,0,2,14,5,4},{2,0,12,4,5,14},{12,0,12,14,5,14},{1,5,1,15,16,15}};
                case CHEST_OF_DRAWERS -> new double[][]{{1,0,1,15,16,15}};
                case RADIATOR -> new double[][]{{1,2,11,15,14,15},{2,0,12,4,2,15},{12,0,12,14,2,15}};
                case WASHING_MACHINE -> new double[][]{{0,0,0,16,16,16}};
            };
            Map<Direction, VoxelShape> facings = new EnumMap<>(Direction.class);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                VoxelShape shape = Shapes.empty();
                int turns = switch (direction) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
                for (double[] box : boxes) {
                    double x0=box[0], z0=box[2], x1=box[3], z1=box[5];
                    for (int t=0; t<turns; t++) { double nx0=16-z1, nx1=16-z0; z0=x0; z1=x1; x0=nx0; x1=nx1; }
                    shape = Shapes.or(shape, Block.box(x0,box[1],z0,x1,box[4],z1));
                }
                facings.put(direction, shape.optimize());
            }
            SHAPES.put(kind, facings);
        }
    }
    public HouseholdFurnitureBlock(Properties properties) {
        super(properties); registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CANE_CHAIR));
    }
    @Override public MapCodec<HouseholdFurnitureBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,KIND); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPES.get(state.getValue(KIND)).get(state.getValue(FACING)); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state,mirror.getRotation(state.getValue(FACING))); }
    public static BlockState state(Kind kind, Direction facing) { return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing); }
}
