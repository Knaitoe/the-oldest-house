package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * One of Zampanò's three filing cabinets in the courtyard archive: three drawers, one of which may stand open. Which drawer
 * shows open is shared; what a reader has pulled from it is their own (ZampanoDrawers), so the block holds nothing.
 */
public final class ZampanoCabinetBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ZampanoCabinetBlock> CODEC=simpleCodec(ZampanoCabinetBlock::new);
    /** Which cabinet of the three, left to right as the reader faces them. */
    public static final IntegerProperty CABINET=IntegerProperty.create("cabinet",0,io.github.knaitoe.theoldesthouse.journal.ZampanoLeaves.CABINETS-1);
    /** 0 when shut; otherwise the open drawer, 1 at the top to 3 at the foot. */
    public static final IntegerProperty DRAWER=IntegerProperty.create("drawer",0,3);
    private static final VoxelShape SHAPE=Block.box(1,0,1,15,16,16);
    public ZampanoCabinetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(CABINET,0).setValue(DRAWER,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,CABINET,DRAWER);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return rotated(s.getValue(FACING));}
    static VoxelShape rotated(Direction facing){
        // The model's front is its north face; a cabinet facing south has its back to the north wall.
        return switch(facing){case NORTH->SHAPE;case SOUTH->Block.box(1,0,0,15,16,15);case EAST->Block.box(0,0,1,15,16,15);default->Block.box(1,0,1,16,16,15);};
    }
    /** The drawer under the reader's hand: top, middle or foot of the cabinet's front. */
    public static int drawerAt(BlockHitResult hit,BlockPos pos){double y=hit.getLocation().y-pos.getY();return y>=2/3D?0:y>=1/3D?1:2;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(hit.getDirection()!=state.getValue(FACING))return InteractionResult.PASS;
        if(player instanceof ServerPlayer reader)ZampanoDrawers.open(reader,pos,state.getValue(CABINET),drawerAt(hit,pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
