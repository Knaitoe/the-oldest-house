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
 * 0.4.361, the Settling Floor (SettlingFloor, docs/SETTLING_FLOOR_0_4_361.md): the gallery's fittings, drawn by
 * tools/draw_settling_fittings.py, facing toward the room.
 * <ul>
 * <li>A floorboard, settled 0 to 3 steps, each step three pixels lower; it holds a reader at whatever it has come down to.</li>
 * <li>The skirting along a wall: tight (0), or with a dark gap opened under it (1).</li>
 * <li>The far wall's panel, lower half and upper: plain plaster (0), shifted with daylight round its edges (1), swung open (2).</li>
 * <li>The free-standing cabinet; its stage follows the boards it stands on.</li>
 * </ul>
 */
public final class SettlingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SettlingBlock> CODEC=simpleCodec(SettlingBlock::new);
    public enum Kind implements StringRepresentable {
        BOARD,SKIRTING,PANEL,PANEL_TOP,CABINET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** Pixels each step of settling takes off a board. */
    public static final int DROP=3;
    public SettlingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOARD).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.SETTLING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.SETTLING.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.SETTLING.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case BOARD->Block.box(0,0,0,16,16-DROP*stage,16);
            case SKIRTING->Shapes.block();
            case PANEL,PANEL_TOP->stage==2?WorkPrintBlock.box(s.getValue(FACING),0,0,0,2,16,16):Shapes.block();
            case CABINET->Block.box(1,0,1,15,16,15);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){
        return switch(s.getValue(KIND)){
            case CABINET->Shapes.empty();
            case PANEL,PANEL_TOP->s.getValue(STAGE)==2?Shapes.empty():Shapes.block();
            default->getShape(s,l,p,CollisionContext.empty());
        };
    }
    @Override protected boolean useShapeForLightOcclusion(BlockState s){return s.getValue(KIND)==Kind.BOARD;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return SettlingFloor.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
