package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.310: one cell of the stopped car. The scene draws the car (DeerRoadRenderer); the cells give it its real collision, clipped
 * from DeerCarMesh, for the doors as they stand ({@link #MASK}: driver's, rear, passenger's). A hand on a cell is a hand on the car.
 */
public final class DeerCarBlock extends Block {
    public static final MapCodec<DeerCarBlock> CODEC=simpleCodec(DeerCarBlock::new);
    public static final IntegerProperty CELL=IntegerProperty.create("cell",0,DeerCarCells.COUNT-1),MASK=IntegerProperty.create("mask",0,DeerCarCells.MASKS-1);
    public DeerCarBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(CELL,DeerCarCells.ROOT).setValue(MASK,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CELL,MASK);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){var shape=DeerCarCells.shape(s.getValue(CELL),s.getValue(MASK));return shape.isEmpty()?Block.box(7,7,7,9,9,9):shape;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return DeerCarCells.shape(s.getValue(CELL),s.getValue(MASK));}
    @Override protected VoxelShape getVisualShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return DeerCarCells.sight(s.getValue(CELL),s.getValue(MASK));}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos at){return true;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos at,Player player,BlockHitResult hit){
        if(level.isClientSide)return InteractionResult.SUCCESS;
        return player instanceof ServerPlayer p&&DeerRoad.carUse(p,hit.getLocation())?InteractionResult.CONSUME:InteractionResult.PASS;
    }
}
