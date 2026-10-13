package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The compressor on the raft: a petrol engine on a tank, feeding the diving helmet's hose. It runs while somebody minds
 * it ({@link LakeHouseDive}); right-clicked, it fills a reader's lent air tanks.
 */
public final class LakeHouseCompressorBlock extends Block {
    public static final MapCodec<LakeHouseCompressorBlock> CODEC=simpleCodec(LakeHouseCompressorBlock::new);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty RUNNING=BooleanProperty.create("running");
    private static final VoxelShape SHAPE=box(2,0,3,14,11,13);
    public LakeHouseCompressorBlock(BlockBehaviour.Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(RUNNING,false));
    }
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,RUNNING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return SHAPE;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){
        if(p instanceof ServerPlayer reader)LakeHouseDive.refill(reader,at);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
