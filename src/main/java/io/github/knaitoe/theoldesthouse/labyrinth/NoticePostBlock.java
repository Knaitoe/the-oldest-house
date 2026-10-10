package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** A real notice board on a timber post. Its original pages remain in its saved block entity. */
public final class NoticePostBlock extends BaseEntityBlock {
    public static final MapCodec<NoticePostBlock> CODEC=simpleCodec(NoticePostBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public NoticePostBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(FACING).getAxis()==Direction.Axis.X?Shapes.or(Block.box(6,0,6,10,16,10),Block.box(6,6,0,10,16,16)):Shapes.or(Block.box(6,0,6,10,16,10),Block.box(0,6,6,16,16,10));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new NoticePostBlockEntity(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(at) instanceof NoticePostBlockEntity board)board.open(p);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos at,BlockState next,boolean moving){if(!s.is(next.getBlock())&&l.getBlockEntity(at) instanceof NoticePostBlockEntity board)popResource(l,at,board.take());super.onRemove(s,l,at,next,moving);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
