package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

/** Twenty-seven native slots, so an old barrel's exact contents fit without spilling. */
public final class InstituteCabinetBlock extends BaseEntityBlock {
    public static final MapCodec<InstituteCabinetBlock> CODEC=simpleCodec(InstituteCabinetBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public InstituteCabinetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new InstituteCabinetBlockEntity(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player who,BlockHitResult hit){
        if(!l.isClientSide&&l.getBlockEntity(p) instanceof InstituteCabinetBlockEntity cabinet)who.openMenu(cabinet);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){Containers.dropContentsOnDestroy(s,next,l,p);super.onRemove(s,l,p,next,moving);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
