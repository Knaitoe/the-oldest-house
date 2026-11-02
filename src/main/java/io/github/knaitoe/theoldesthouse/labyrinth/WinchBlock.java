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
 * 0.4.251, the Window Cleaner (docs/WINDOW_CLEANER_0_4_251.md): the plant room's emergency winch and the cradle it lets down, drawn
 * facing north and turned by {@link #FACING}. The winch's drum and crank, a quarter turn further round at each stage; the brake lever,
 * on (0) or off (1); the cradle's slatted floor, at the top of its block; its guard rail; the cable it hangs from; the corridor's casement, shut (0) or open (1).
 * Drawn by tools/draw_winch_fittings.py.
 */
public final class WinchBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<WinchBlock> CODEC=simpleCodec(WinchBlock::new);
    public enum Kind implements StringRepresentable {
        DRUM,BRAKE,CRADLE,RAIL,CABLE,CASEMENT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public WinchBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.WINCH.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.WINCH.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case DRUM->box(f,1,1,0,15,15,12);
            case BRAKE->box(f,5,2,0,11,14,6);
            case CRADLE->Block.box(0,13,0,16,16,16);
            case RAIL->box(f,0,0,13,16,16,16);
            case CABLE->Block.box(7,0,7,9,16,9);
            case CASEMENT->s.getValue(STAGE)==0?box(f,0,0,0,16,16,2):box(f,0,0,0,2,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CABLE->Shapes.empty();
            case RAIL->box(f,0,0,13,16,24,16);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return WindowCleaner.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
