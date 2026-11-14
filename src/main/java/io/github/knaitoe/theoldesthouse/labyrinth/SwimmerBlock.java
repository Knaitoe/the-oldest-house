package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.227, the last swimmer: the pool's fittings. Models face north as drawn and turn with {@link #FACING}; a fitting on a wall has
 * its back to that wall. The rope and the cover stand in the water and are waterlogged. The ladder's rails stand on the deck, dry (0.4.403: waterlogged, they
 * stood in a block of water above the pool's edge). Stages: the cover flat (0), its leading edge (2), or a foot pressed up under
 * it (1 and 3 to 9: the way it walks and which foot, {@link #foot}); the reel full (0), half run out (1) or empty (2); a starting block's
 * number (0-5); the pace clock's sweep hand at each five-second mark (0-11); a depth tile shallow (0) or deep (1); the lockers shut
 * (0) or one ajar (1); the radio off (0) or on (1); a lamp lit (0) or off (1); the signs no running (0) and last swim (1).
 */
public final class SwimmerBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<SwimmerBlock> CODEC=simpleCodec(SwimmerBlock::new);
    public enum Kind implements StringRepresentable {
        ROPE,COVER,REEL,REEL_END,START,PACE_CLOCK,LADDER,DEPTH,BENCH,LOCKERS,RADIO,CLIPBOARD,BOARDS,LAMP,EXIT_SIGN,TOWEL,DRAIN,SIGN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,11);
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    public SwimmerBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.DRAIN).setValue(STAGE,0).setValue(WATERLOGGED,false));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return SwimmerRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage).setValue(WATERLOGGED,wet(kind));}
    /** The fittings that stand in the pool's water. */
    public static boolean wet(Kind k){return k==Kind.ROPE||k==Kind.COVER;}
    public static boolean is(BlockState s,Kind kind){return s.is(SwimmerRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(SwimmerRegistry.FITTING.get())?s.getValue(KIND):null;}
    /**
     * 0.4.581: the cover's stage for a foot pressed up under it: the way it walks, and the left foot or the right. The eight are stage 1
     * and stages 3 to 9, since 2 is the leading edge. tools/draw_swimmer_art.py numbers its models by the same sum.
     */
    public static int foot(Direction way,boolean right){
        int n=switch(way){case EAST->1;case SOUTH->2;case WEST->3;default->0;}*2+(right?1:0);
        return n==0?1:n+2;
    }
    /** Whether a foot is pressed up under this cell of the cover. */
    public static boolean pressed(BlockState s){if(!is(s,Kind.COVER))return false;int stage=s.getValue(STAGE);return stage==1||stage>=3&&stage<=9;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE,WATERLOGGED);}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP->s.getValue(STAGE)==0?15:0;case EXIT_SIGN->8;case RADIO->s.getValue(STAGE)==1?3:0;default->0;};}
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos at,BlockPos next){if(s.getValue(WATERLOGGED))l.scheduleTick(at,Fluids.WATER,Fluids.WATER.getTickDelay(l));return super.updateShape(s,d,other,l,at,next);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case ROPE->box(f,6.5,13,0,9.5,15.5,16);
            case COVER->pressed(s)?Shapes.or(Block.box(0,13.5,0,16,15.5,16),Block.box(2,15.5,2,14,18.25,14)):Block.box(0,13.5,0,16,15.5,16);
            case REEL->Block.box(0,3,0,16,13,16);
            case REEL_END->Block.box(2,0,2,14,13,14);
            case START->Block.box(2,0,2,14,9,14);
            case PACE_CLOCK->box(f,0,0,15,16,16,16);
            case LADDER->box(f,3,0,10,13,16,14);
            case DEPTH,LOCKERS->Shapes.block();
            case BENCH->Block.box(0,0,3,16,8,13);
            case RADIO->Block.box(3,0,6,13,11,10);
            case CLIPBOARD->Block.box(4,0,3,12,1,13);
            case BOARDS->Block.box(2,0,4,14,7,12);
            case LAMP->Block.box(2,14,2,14,16,14);
            case EXIT_SIGN->box(f,0.5,4,15,15.5,13,16);
            case TOWEL->Block.box(3,0,4,13,2,12);
            case DRAIN->Block.box(2,0,2,14,1,14);
            case SIGN->box(f,0,0,15.5,16,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            // A safety cover holds whoever walks on it: the cover's top is the floor.
            case COVER->Block.box(0,13.5,0,16,15.5,16);
            case ROPE,PACE_CLOCK,LADDER,CLIPBOARD,LAMP,EXIT_SIGN,TOWEL,DRAIN,SIGN->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.DEPTH||s.getValue(KIND)==Kind.LOCKERS?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
