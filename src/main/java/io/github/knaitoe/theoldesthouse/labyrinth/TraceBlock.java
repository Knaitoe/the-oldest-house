package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
 * 0.4.231, traces and the door wedge: what keeps a reader's traces, drawn facing north (front toward the room, back to the wall) and
 * turned by {@link #FACING}. Dust on a floor: clean (0), walked through (1, the prints pointing the way the reader went), a second
 * set of prints beside where the reader walked (2), both (3), or scored where a chair was dragged (4). A loose chair, standing on
 * bare boards (0) or in dust (1), that a hand can pull a block at a time and turn. A small table; furniture under a dust sheet; a
 * coat hook with a wooden wedge on it; the closer's arm above a door that shuts itself. 0.4.234: grit on a stone floor, with the same
 * stages as dust; a chair standing in grit is stage 2. Drawn by tools/draw_trace_fittings.py.
 */
public final class TraceBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TraceBlock> CODEC=simpleCodec(TraceBlock::new);
    public enum Kind implements StringRepresentable {
        DUST,CHAIR,TABLE,SHEET,HOOK,CLOSER,GRIT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,4);
    /** Dust's stages. */
    public static final int CLEAN=0,PRINTS=1,OTHER=2,BOTH=3,DRAGGED=4;
    public TraceBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.DUST).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return TraceRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(4,stage)));}
    public static BlockState dust(){return of(Kind.DUST,Direction.NORTH);}
    public static BlockState grit(){return of(Kind.GRIT,Direction.NORTH);}
    /** Dust or grit: a floor that keeps prints. */
    public static boolean dusty(BlockState s){return is(s,Kind.DUST)||is(s,Kind.GRIT);}
    /** The chair's stage standing on this floor (0 bare, 1 dust, 2 grit), and the floor it leaves behind it. */
    public static int under(BlockState floor){return is(floor,Kind.DUST)?1:is(floor,Kind.GRIT)?2:0;}
    public static boolean is(BlockState s,Kind kind){return s.is(TraceRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case DUST,GRIT->Block.box(0,0,0,16,.5,16);
            case CHAIR->Shapes.or(box(f,2,0,2,14,9,14),box(f,2,9,12,14,21,14));
            case TABLE->Block.box(1,0,1,15,15,15);
            case SHEET->Block.box(0,0,0,16,14,16);
            case HOOK->box(f,4,4,12,12,15,16);
            case CLOSER->box(f,1,10,12,15,15,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case DUST,GRIT,HOOK,CLOSER->Shapes.empty();
            case CHAIR->box(s.getValue(FACING),2,0,2,14,9,14);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected boolean isPathfindable(BlockState s,net.minecraft.world.level.pathfinder.PathComputationType t){return s.getValue(KIND)==Kind.DUST||s.getValue(KIND)==Kind.GRIT||s.getValue(KIND)==Kind.HOOK||s.getValue(KIND)==Kind.CLOSER;}
    @Override protected void entityInside(BlockState s,Level level,BlockPos pos,Entity e){
        if(!level.isClientSide&&(s.getValue(KIND)==Kind.DUST||s.getValue(KIND)==Kind.GRIT)&&e instanceof ServerPlayer p)TraceRooms.step(p,pos,s);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        var kind=s.getValue(KIND);if(kind!=Kind.CHAIR&&kind!=Kind.HOOK)return InteractionResult.PASS;
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        // 0.4.239: a hand on the seat sits down in it; a hand on the back pulls it (and, crouching, turns it).
        if(kind==Kind.CHAIR&&!p.isShiftKeyDown()&&onSeat(pos,hit))return TraceRooms.sitOn(p,pos)?InteractionResult.CONSUME:InteractionResult.PASS;
        return (kind==Kind.CHAIR?TraceRooms.chair(p,pos,s):TraceRooms.hook(p,pos))?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    /** The top of the loose chair's seat, in blocks: a hand at or below it is on the seat, above it on the back. */
    public static final double SEAT=9/16.;
    public static boolean onSeat(BlockPos pos,BlockHitResult hit){return hit.getLocation().y-pos.getY()<=SEAT+.03;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
