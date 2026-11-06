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
 * 0.4.306, the Tension Room (docs/TENSION_ROOM_0_4_306.md): a utility loft's cable gear, drawn facing north (front to the room, back to
 * its wall) and turned by {@link #FACING}; drawn by tools/draw_tension_fittings.py.
 * <ul>
 * <li>A hand-wheel: its spokes at one of three turns (stage 0-2). The far wheel's stage is also how many turns its drum holds.</li>
 * <li>The cleat on the shaft: cast off (0) or made fast, the cable's turns round it (1).</li>
 * <li>The counterweight: a cast-iron block on its shackle, riding the shaft.</li>
 * <li>The locking pin in the pit wall: seated (0), half drawn (1), drawn free (2).</li>
 * <li>The cable: a run along the ceiling (0), a rise down a wall (1).</li>
 * <li>The sheave at the head of the shaft.</li>
 * <li>The sprung platform in front of the far door.</li>
 * </ul>
 */
public final class TensionBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TensionBlock> CODEC=simpleCodec(TensionBlock::new);
    public enum Kind implements StringRepresentable {
        WHEEL,CLEAT,WEIGHT,PIN,CABLE,SHEAVE,PLATFORM,RELEASE_BAR;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,4);
    /** A cable's stages. */
    public static final int RUN=0,RISE=1;
    public TensionBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.TENSION.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(4,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.TENSION.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.TENSION.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case WHEEL->box(f,1,1,11,15,15,16);
            case CLEAT->box(f,3,4,11,13,12,16);
            case RELEASE_BAR->box(f,1,5,11,15,11,16);
            case WEIGHT->Block.box(3,0,3,13,14,13);
            case PIN->box(f,5,5,8,11,11,16);
            case CABLE->s.getValue(STAGE)==RISE?box(f,7,0,7,9,16,9):box(f,0,7,7,16,9,9);
            case SHEAVE->Block.box(4,0,4,12,10,12);
            case PLATFORM->Block.box(0,8,0,16,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case PLATFORM,WEIGHT->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(DeepTensionRooms.use(p,pos,s))return InteractionResult.CONSUME;
        return TensionRooms.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
