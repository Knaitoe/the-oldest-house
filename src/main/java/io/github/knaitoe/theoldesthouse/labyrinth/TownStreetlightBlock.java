package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;

/** Proofrock's fitted iron streetlights, including the lamps of the old submerged street. */
public final class TownStreetlightBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<TownStreetlightBlock> CODEC=simpleCodec(TownStreetlightBlock::new);
    public enum Kind implements StringRepresentable {BASE,POLE,TOP,ARM,HEAD;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    private static final VoxelShape POLE=Block.box(6,0,6,10,16,10);
    private static final VoxelShape BASE=Shapes.or(POLE,Block.box(4,0,4,12,5,12));
    private static final VoxelShape HEAD=Shapes.or(Block.box(4,2,4,12,11,12),Block.box(3,11,3,13,13,13),Block.box(7,11,7,9,16,9));
    public TownStreetlightBlock(Properties p){super(p.lightLevel(s->s.getValue(KIND)==Kind.HEAD?(s.getValue(WATERLOGGED)?9:15):0));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.POLE).setValue(WATERLOGGED,false));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,WATERLOGGED);}
    public static BlockState of(Kind kind,Direction direction,boolean wet){return DrownedTownRegistry.STREETLIGHT.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,direction).setValue(WATERLOGGED,wet);}
    private static VoxelShape arm(Direction d,boolean outer){return switch(d){
        case NORTH->Block.box(6,12,outer?6:0,10,16,outer?16:8);
        case SOUTH->Block.box(6,12,outer?0:8,10,16,outer?10:16);
        case WEST->Block.box(outer?6:0,12,6,outer?16:8,16,10);
        default->Block.box(outer?0:8,12,6,outer?10:16,16,10);
    };}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case BASE->BASE;case POLE->POLE;case TOP->Shapes.or(POLE,arm(s.getValue(FACING),false));case ARM->Shapes.or(arm(s.getValue(FACING),true),Block.box(7,0,7,9,12,9));case HEAD->HEAD;
    };}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos p,BlockPos neighbor){if(s.getValue(WATERLOGGED))l.scheduleTick(p,Fluids.WATER,Fluids.WATER.getTickDelay(l));return super.updateShape(s,d,other,l,p,neighbor);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
