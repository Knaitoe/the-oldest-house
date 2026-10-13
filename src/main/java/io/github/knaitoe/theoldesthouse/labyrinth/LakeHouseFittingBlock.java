package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;

/** The submerged house's own fitted furniture. Only its deliberately dry dining chairs hold air. */
public final class LakeHouseFittingBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<LakeHouseFittingBlock> CODEC=simpleCodec(LakeHouseFittingBlock::new);
    public enum Kind implements StringRepresentable {
        COATRACK,KEY_BOWL,BULB,TABLE,CHAIR,ASH_TRAY,BOOKCASE,POOL_TABLE,SOFA,PAINTING,
        ISLAND,BEAVER,RED_RUNNER,WARDROBE,BED,SAUNA_BENCH,SAUNA_DOOR,HALF_DOOR,LAMP,CANDLE,HEARTH,DRESS;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT,WATERLOGGED=BlockStateProperties.WATERLOGGED;
    public LakeHouseFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TABLE).setValue(FACING,Direction.NORTH).setValue(LIT,false).setValue(WATERLOGGED,true));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,LIT,WATERLOGGED);}
    @Override public boolean canPlaceLiquid(net.minecraft.world.entity.player.Player p,BlockGetter l,BlockPos at,BlockState s,Fluid f){return s.getValue(KIND)!=Kind.CHAIR&&SimpleWaterloggedBlock.super.canPlaceLiquid(p,l,at,s,f);}
    @Override public boolean placeLiquid(LevelAccessor l,BlockPos at,BlockState s,FluidState f){return s.getValue(KIND)!=Kind.CHAIR&&SimpleWaterloggedBlock.super.placeLiquid(l,at,s,f);}
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos at,BlockPos neighbour){if(s.getValue(WATERLOGGED))l.scheduleTick(at,Fluids.WATER,Fluids.WATER.getTickDelay(l));return super.updateShape(s,d,other,l,at,neighbour);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static boolean is(BlockState s,Kind k){return s.is(LakeHouseRegistry.FITTING.get())&&s.getValue(KIND)==k;}
    public static BlockState of(Kind k,Direction d){return LakeHouseRegistry.FITTING.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d).setValue(WATERLOGGED,k!=Kind.CHAIR);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var k=s.getValue(KIND);return switch(k){
            case KEY_BOWL,ASH_TRAY,BEAVER->box(4,0,4,12,5,12);
            case RED_RUNNER->box(0,0,0,16,1,16);
            case BULB,CANDLE,LAMP->box(5,0,5,11,15,11);
            case CHAIR,SOFA,SAUNA_BENCH,BED->box(1,0,1,15,11,15);
            case TABLE,ISLAND,POOL_TABLE->box(0,0,0,16,13,16);
            case PAINTING,HALF_DOOR,SAUNA_DOOR->switch(s.getValue(FACING)){case EAST->box(0,0,0,2,16,16);case WEST->box(14,0,0,16,16,16);case SOUTH->box(0,0,0,16,16,2);default->box(0,0,14,16,16,16);};
            case DRESS->Shapes.empty();
            default->box(1,0,1,15,16,15);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return switch(s.getValue(KIND)){case HALF_DOOR,DRESS,BULB,LAMP,PAINTING->Shapes.empty();default->getShape(s,l,at,c);};}
}
