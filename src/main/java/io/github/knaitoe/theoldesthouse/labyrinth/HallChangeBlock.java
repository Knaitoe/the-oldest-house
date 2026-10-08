package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Supported scenery, without item rewards or inventories. */
public final class HallChangeBlock extends Block {
    public static final MapCodec<HallChangeBlock> CODEC=simpleCodec(HallChangeBlock::new);
    public enum Kind implements StringRepresentable {
        PICTURE,PICTURE_TURNED,RUG,RUG_FOLDED,LAMP,LAMP_OFF;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public HallChangeBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.PICTURE).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState state(Kind kind,Direction facing){return HallChangeRegistry.PROP.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean rug(BlockState s){return s.getValue(KIND)==Kind.RUG||s.getValue(KIND)==Kind.RUG_FOLDED;}
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){
        var support=rug(s)?p.below():p.relative(s.getValue(FACING).getOpposite());
        return l.getBlockState(support).isCollisionShapeFullBlock(l,support);
    }
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos p,BlockPos neighbour){return canSurvive(s,l,p)?s:Blocks.AIR.defaultBlockState();}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(rug(s))return box(1,0,1,15,s.getValue(KIND)==Kind.RUG_FOLDED?4:1,15);
        return switch(s.getValue(FACING)){case NORTH->box(2,2,12,14,14,16);case SOUTH->box(2,2,0,14,14,4);case EAST->box(0,2,2,4,14,14);default->box(12,2,2,16,14,14);};
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return rug(s)?box(1,0,1,15,1,15):Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
