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
 * 0.4.250, the Folding Rooms (docs/FOLDING_ROOMS_0_4_250.md): a dining room's working parts, drawn facing north and turned by
 * {@link #FACING}. A partition panel, lower (stage 0), upper (1), or upper with its brass pulls (2), its faces to the north and south
 * of the track it runs along; the brass hanger rod from a panel's top to the ceiling track, plain (0) or with its carriage where it
 * meets the ceiling (1); the partition folded back on its hinges, lower (0) and upper (1); the sideboard, a dining table, a dining
 * chair, and a serving hatch through to the pantry, its shutter down (0) or up (1). Drawn by tools/draw_fold_fittings.py.
 * <p>
 * A partition's upper panels stand half a block taller to the hand than to the eye, as a fence does, so that nothing climbed on in
 * the room puts a reader over it: the gap above it is for seeing through.
 */
public final class FoldBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FoldBlock> CODEC=simpleCodec(FoldBlock::new);
    public enum Kind implements StringRepresentable {
        PANEL,HANGER,FOLDED,SIDEBOARD,TABLE,CHAIR,HATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    /** A panel's stages. */
    public static final int LOWER=0,UPPER=1,PULL=2;
    public FoldBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PANEL).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.FOLD.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.FOLD.get())&&s.getValue(KIND)==kind;}
    /** Any part of a standing partition: its panels and its hangers. */
    public static boolean partition(BlockState s){return is(s,Kind.PANEL)||is(s,Kind.HANGER);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case PANEL->box(f,0,0,5,16,16,11);
            case HANGER->box(f,6,0,6,10,16,10);
            case FOLDED->box(f,1,0,2,15,16,14);
            case SIDEBOARD->box(f,1,0,3,15,13,15);
            case TABLE->Block.box(0,0,0,16,14,16);
            case CHAIR->box(f,3,0,3,13,19,14);
            case HATCH->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            // The upper panels stand taller to the hand than to the eye, as a fence does.
            case PANEL->s.getValue(STAGE)==LOWER?box(f,0,0,5,16,16,11):box(f,0,0,5,16,24,11);
            case HANGER->Shapes.empty();
            case CHAIR->box(f,3,0,3,13,12,14);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.HATCH?Shapes.block():Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return FoldingRooms.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
