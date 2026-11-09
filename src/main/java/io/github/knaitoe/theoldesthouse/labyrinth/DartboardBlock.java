package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.415 (docs/DARTS_0_4_415.md): a dartboard hung on a wall. {@link #FACING} is the way its face looks, out from the wall. The board
 * fills the block's face: the double's outer wire is 7 pixels from the centre and the number ring runs to the edge. Using it joins the
 * game before the first dart, or watches; crouching at it before the first dart changes the game. The game is the server's
 * ({@link DartBoards}).
 */
public final class DartboardBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<DartboardBlock> CODEC=simpleCodec(DartboardBlock::new);
    /** The double's outer wire, in blocks from the centre of the face. */
    public static final double SCORING=7/16.0;
    /** How far the face stands out from the wall. */
    public static final double DEPTH=2/16.0;
    public DartboardBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    public static boolean is(BlockState s){return s.is(BarRegistry.DARTBOARD.get());}

    static final VoxelShape[] SHAPES={Block.box(0,0,14,16,16,16),Block.box(0,0,0,2,16,16),Block.box(0,0,0,16,16,2),Block.box(14,0,0,16,16,16)};
    /** North, east, south, west: the board against the wall behind its face. */
    static VoxelShape shape(Direction facing){return SHAPES[facing.get2DDataValue()==2?0:facing.get2DDataValue()==3?1:facing.get2DDataValue()==0?2:3];}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s.getValue(FACING));}

    /** The centre of the face, in the world. */
    public static Vec3 centre(BlockPos pos,Direction facing){
        return Vec3.atCenterOf(pos).add(facing.getStepX()*(DEPTH-.5),0,facing.getStepZ()*(DEPTH-.5));
    }
    /** The way that is right for someone looking at the face. */
    public static Direction right(Direction facing){return facing.getCounterClockWise();}
    /** Where a point on the face lands, as the board scores it. */
    public static DartsGame.Hit hit(BlockPos pos,Direction facing,Vec3 at){
        var c=centre(pos,facing);var r=right(facing);var d=at.subtract(c);
        double u=(d.x*r.getStepX()+d.z*r.getStepZ())/SCORING,v=d.y/SCORING;
        return DartsGame.score(u,v);
    }
    /** A point on the face, from the board's own units (1 being the double's outer wire). */
    public static Vec3 onFace(BlockPos pos,Direction facing,double u,double v){
        var c=centre(pos,facing);var r=right(facing);
        return c.add(r.getStepX()*u*SCORING,v*SCORING,r.getStepZ()*u*SCORING);
    }

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx){
        for(var dir:ctx.getNearestLookingDirections()){
            if(!dir.getAxis().isHorizontal())continue;
            var s=defaultBlockState().setValue(FACING,dir.getOpposite());
            if(s.canSurvive(ctx.getLevel(),ctx.getClickedPos()))return s;
        }
        return null;
    }
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos pos){
        var f=s.getValue(FACING);var wall=pos.relative(f.getOpposite());
        return l.getBlockState(wall).isFaceSturdy(l,wall,f);
    }
    @Override protected BlockState updateShape(BlockState s,Direction dir,BlockState other,LevelAccessor l,BlockPos pos,BlockPos otherPos){
        return dir==s.getValue(FACING).getOpposite()&&!s.canSurvive(l,pos)?Blocks.AIR.defaultBlockState():super.updateShape(s,dir,other,l,pos,otherPos);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p)DartBoards.use(p,pos,p.isShiftKeyDown());
        return InteractionResult.CONSUME;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new DartboardBlockEntity(pos,state);}
}
