package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.307, Johnny Truant's service halls (docs/TRUANT_SERVICE_0_4_307.md): the fittings of his three middle-hall places, drawn facing
 * north and turned by {@link #FACING}, by tools/draw_truant_service_fittings.py in vanilla's own manner.
 * <ul>
 * <li>A chalk arrow on a wall: none (0), pointing left (1), pointing right (2), as seen facing the wall.</li>
 * <li>The steam pipe at the elbow: still (0), shivering (1), venting (2).</li>
 * <li>The lift brake on the landing: off (0), held (1).</li>
 * <li>The brake on the lift itself: off (0), on (1).</li>
 * <li>The lift's platform: an iron-bound floor across the shaft.</li>
 * <li>0.4.359, his copying room (TruantSentence): a sheaf of pages on the desk, written (0), one line run by the wet (1), blank and
 * ruled (2); the blotter, clean (0) or with a line taken off backwards (1); and pigeonholes stuffed with paper, a whole block.</li>
 * </ul>
 */
public final class TruantFittingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TruantFittingBlock> CODEC=simpleCodec(TruantFittingBlock::new);
    public enum Kind implements StringRepresentable {
        ARROW,PIPE,BRAKE,CAR_BRAKE,PLATFORM,HASP,WEDGE,HATCH,SPLIT,STOCK_CHAIN,BOUGH,PAGE,BLOTTER,PIGEONHOLES;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public TruantFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.ARROW).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.TRUANT_FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.TRUANT_FITTING.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.TRUANT_FITTING.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case ARROW->box(f,1,1,15,15,15,16);
            case PIPE->box(f,0,4,8,16,12,16);
            case BRAKE,CAR_BRAKE->box(f,5,0,10,11,14,16);
            case PLATFORM->Block.box(0,10,0,16,16,16);
            case HASP,STOCK_CHAIN->box(f,4,1,12,12,15,16);
            case WEDGE->box(f,2,0,2,13,3,14);
            case HATCH->s.getValue(STAGE)==0?box(f,0,0,13,16,16,16):box(f,0,13,0,16,16,16);
            case SPLIT->Shapes.or(box(f,0,0,13,7,16,16),box(f,9,0,13,16,16,16));
            case BOUGH->box(f,0,1,6,16,15,10);
            case PAGE->Block.box(2,0,2.5,14,1,14);
            case BLOTTER->Block.box(1,0,2,15,1,14);
            case PIGEONHOLES->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND)==Kind.PLATFORM||s.getValue(KIND)==Kind.SPLIT||s.getValue(KIND)==Kind.PIGEONHOLES||s.getValue(KIND)==Kind.HATCH&&s.getValue(STAGE)==0?getShape(s,l,p,c):Shapes.empty();
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(TruantSentence.use(p,pos,s))return InteractionResult.CONSUME;
        if(TruantYard.use(p,pos,s))return InteractionResult.CONSUME;
        return TruantService.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
