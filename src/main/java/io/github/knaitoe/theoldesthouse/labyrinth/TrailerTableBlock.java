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

/** A continuous long tabletop, with a leg only in the six authored support tiles. */
public final class TrailerTableBlock extends Block {
    public static final MapCodec<TrailerTableBlock> CODEC=simpleCodec(TrailerTableBlock::new);
    public enum Support implements StringRepresentable {
        NONE,WEST,EAST;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Support> SUPPORT=EnumProperty.create("support",Support.class);
    private static final VoxelShape TOP=Block.box(0,14,0,16,16,16);
    private static final VoxelShape WEST=Shapes.or(TOP,Block.box(2,0,6,4,14,10));
    private static final VoxelShape EAST=Shapes.or(TOP,Block.box(12,0,6,14,14,10));
    public TrailerTableBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(SUPPORT,Support.NONE));}
    @Override public MapCodec<TrailerTableBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(SUPPORT);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return switch(state.getValue(SUPPORT)){case WEST->WEST;case EAST->EAST;default->TOP;};}
    public static BlockState tile(int x,int z){return GoatmanRegistry.TABLE.get().defaultBlockState().setValue(SUPPORT,(z==-69||z==-65||z==-61)?x==-1?Support.WEST:x==1?Support.EAST:Support.NONE:Support.NONE);}
}
