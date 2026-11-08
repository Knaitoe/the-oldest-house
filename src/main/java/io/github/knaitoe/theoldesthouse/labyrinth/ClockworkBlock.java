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
 * 0.4.362, the Clockwork Attic (Clockwork, docs/CLOCKWORK_ATTIC_0_4_362.md): the trains' working parts, drawn by
 * tools/draw_clockwork_fittings.py facing north (front toward the room, back to the wall) and turned by {@link #FACING}.
 * <ul>
 * <li>A master clock's case, its pendulum hanging still (0) or swung left (1) or right (2); its face, the hands in one of four places.</li>
 * <li>A gear on the wall, turned through four positions; a pinned gear, the same with an iron pin through its teeth.</li>
 * <li>A train's end lever, up (0) or seated (1).</li>
 * <li>A weight on its chain; the catch that can hold it, open (0) or set (1); bars across a doorway, down (0) or drawn up (1).</li>
 * </ul>
 */
public final class ClockworkBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ClockworkBlock> CODEC=simpleCodec(ClockworkBlock::new);
    public enum Kind implements StringRepresentable {
        CASE,FACE,GEAR,PINNED,LEVER,WEIGHT,CATCH,GATE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public ClockworkBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.GEAR).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.CLOCKWORK.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.CLOCKWORK.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.CLOCKWORK.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CASE,FACE->box(f,2,0,6,14,16,16);
            case GEAR,PINNED->box(f,0,0,12,16,16,16);
            case LEVER->box(f,4,0,10,12,16,16);
            case WEIGHT->Block.box(4,0,4,12,10,12);
            case CATCH->box(f,0,6,6,16,10,16);
            case GATE->s.getValue(STAGE)==0?box(f,0,0,6,16,16,10):box(f,0,12,6,16,16,10);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case CASE,FACE,WEIGHT->getShape(s,l,p,c);
            case GATE->s.getValue(STAGE)==0?getShape(s,l,p,c):Shapes.empty();
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return Clockwork.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
