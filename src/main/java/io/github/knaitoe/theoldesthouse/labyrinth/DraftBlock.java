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
 * 0.4.230, drafts and pressure: the draft rooms' working parts, drawn facing north (front toward the room, back to the wall) and
 * turned by {@link #FACING}. Stages: a wall vent's grille shut (0) or open (1); a sash window open (0) or shut (1); a sheet on the
 * line, or a curtain, hanging still (0) or pulled by the air toward the way it faces (1); the plaster where a door is hidden, plain
 * (0), with the air lifting dust off its edges (1), or swung open (2); a louvre in a wall shut (0) or open (1); the ceiling grate the
 * air comes in by. Drawn by tools/draw_draft_fittings.py.
 */
public final class DraftBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DraftBlock> CODEC=simpleCodec(DraftBlock::new);
    public enum Kind implements StringRepresentable {
        VENT,SASH,SHEET,CURTAIN,SEAM,LOUVRE,GRATE,LINE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public DraftBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.LINE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.DRAFT.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.DRAFT.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case VENT,LOUVRE->box(f,2,2,14,14,14,16);
            case SASH->Shapes.block();
            case SHEET->s.getValue(STAGE)==0?box(f,0,0,7,16,16,9):box(f,0,0,3,16,16,9);
            case CURTAIN->box(f,1,0,13,15,16,16);
            case SEAM->s.getValue(STAGE)==2?box(f,0,0,0,2,16,16):Shapes.block();
            case GRATE->Block.box(0,14,0,16,16,16);
            case LINE->Block.box(0,14,7.5,16,15,8.5);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case SHEET,CURTAIN,LINE,GRATE->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.SASH||s.getValue(KIND)==Kind.SEAM&&s.getValue(STAGE)<2?Shapes.block():Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return DraftRooms.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
