package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Small authored objects; choreography never puts these into a player's inventory. */
public final class StaircaseLeakProps extends Block {
    public enum Kind implements StringRepresentable {
        CUP_BLUE,CUP_CREAM,CUP_RED,HOOK,CUP_HUNG_BLUE,CUP_HUNG_CREAM,CUP_HUNG_RED,
        TOWEL,PLATE,GERANIUM,GERANIUM_PLATE,SINK_FULL,SINK_EMPTY,SINK_CLEAN,
        LAUNDRY,SOCK_SINGLE,SHIRTS,TROUSERS,LIST_SPOT,LIST,
        RADIO,RADIO_OFF,BREAD_BAG,CAR_SEAT,BELT,
        DRAWER,DRAWER_OPEN,CANDLE,BUTTON_TIN,CLOCK,APRON,LIGHT,LIGHT_OFF,SMALL_LIGHT;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public StaircaseLeakProps(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.CUP_BLUE).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
    public static BlockState state(Kind kind){return StaircaseLeakRegistry.PROP.get().defaultBlockState().setValue(KIND,kind);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,BlockStateProperties.HORIZONTAL_FACING);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return box(1,0,1,15,15,15);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(BlockStateProperties.HORIZONTAL_FACING,r.rotate(s.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
}
