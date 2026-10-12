package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** The production case is a native nine-slot container, with an opening lid and no generated stock. */
public final class GhostsCaseBlock extends BaseEntityBlock {
    public static final MapCodec<GhostsCaseBlock> CODEC=simpleCodec(GhostsCaseBlock::new);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    public GhostsCaseBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,OPEN);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GhostsCaseBlockEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(1,0,1,15,s.getValue(OPEN)?21:14,15);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(at) instanceof GhostsCaseBlockEntity box)p.openMenu(box);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){if(l.getBlockEntity(p) instanceof GhostsCaseBlockEntity box)box.recheck();}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof GhostsCaseBlockEntity box){Containers.dropContents(l,p,box);l.updateNeighbourForOutputSignal(p,this);}super.onRemove(s,l,p,next,moving);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
