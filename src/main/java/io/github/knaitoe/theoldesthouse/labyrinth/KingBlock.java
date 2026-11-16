package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.641, A sort of thing (docs/SLEEPING_KING_PLAN.md): the drawing-room's things, drawn by tools/draw_king_art.py. Models face north
 * as drawn and turn with {@link #FACING}; a thing against a wall has its back to it.
 * <p>
 * Stages: the chimney-piece's left jamb (0), the fire between (1), the right jamb (2); the looking-glass over it, three across and two
 * high (0-2 the lower row, 3-5 the upper); the tall window's lower (0) and upper (1) halves. 0.4.643: the brothers' rattle (RATTLE)
 * lies on the King's square. 0.4.644: the Queen's pegs (PEG), one on every square past its crossing; the glass table on the hill with
 * the bottle and the cake on it (GLASS_TABLE); the railway carriage's seats (SEAT, sat in) and its wheels (WHEEL); the mushroom's stalk (STALK)
 * and cap (CAP, 0.4.645).
 */
public final class KingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<KingBlock> CODEC=simpleCodec(KingBlock::new);
    public enum Kind implements StringRepresentable {
        MANTEL,GLASS,RUG,CHESSMEN,TABLE,PROBLEM,ARMCHAIR,WINDOW,RATTLE,PEG,GLASS_TABLE,SEAT,WHEEL,STALK,CAP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,5);
    public KingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return KingRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(KingRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(KingRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stage(BlockState s){return s.is(KingRegistry.FITTING.get())?s.getValue(STAGE):-1;}
    /** 0.4.644: a carriage seat is sat in (HouseSitting), seven pixels up, facing the way it faces. */
    public static boolean seat(BlockState s){return is(s,Kind.SEAT);}
    public static final double SEAT_HEIGHT=7/16.;
    /** 0.4.645: the top of the mushroom's cap, where the Caterpillar sits (KingCaterpillar). */
    public static final double CAP_TOP=15/16.;
    /** The fire in the grate gives the room its light. */
    public static int light(BlockState s){return s.getValue(KIND)==Kind.MANTEL&&s.getValue(STAGE)==1?13:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case MANTEL->s.getValue(STAGE)==1?Shapes.or(box(f,0,11,0,16,16,16),box(f,0,0,15,16,11,16)):Shapes.or(box(f,0,14,0,16,16,14),box(f,0,0,4,16,14,16));
            case GLASS->box(f,0,0,14,16,16,16);
            case RUG,CHESSMEN->Block.box(0,0,0,16,1,16);
            case TABLE->Shapes.or(Block.box(0,13,0,16,15,16),Block.box(6,0,6,10,13,10));
            case PROBLEM->Block.box(3,0,3,13,1,13);
            case ARMCHAIR->Shapes.or(Block.box(1,0,1,15,7,15),box(f,1,7,11,15,20,15));
            case WINDOW->box(f,0,0,6,16,16,10);
            case RATTLE->Block.box(1,0,4,15,5,12);
            case PEG->Block.box(6,0,6,10,7,10);
            case GLASS_TABLE->Shapes.or(Block.box(2,11,2,14,12,14),Block.box(3,0,3,13,11,13));
            case SEAT->Shapes.or(Block.box(0,0,2,16,7,14),box(f,0,7,12,16,22,16));
            case WHEEL->box(f,0,0,5,16,16,11);
            // 0.4.645: the mushroom's stalk, and its cap on the block above, broad and flat on top.
            case STALK->Block.box(5,0,5,11,16,11);
            case CAP->Shapes.or(Block.box(5,0,5,11,10,11),Block.box(0,10,0,16,15,16));
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case RUG,CHESSMEN,PROBLEM,RATTLE,PEG->Shapes.empty();default->getShape(s,l,p,c);};
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
