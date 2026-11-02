package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
 * 0.4.252, the Night Operator (docs/NIGHT_OPERATOR_0_4_252.md): the exchange's cord switchboard, drawn facing north (its face to the room)
 * and turned by {@link #FACING}. A line's jack, its lamp dark (0), lit while someone listens (1) or with a cord plugged in (2); the
 * incoming line's panel, its lamp dark (0) or lit while someone is on it (1); the field of lamps above the jacks; the board's shelf of
 * plugs and keys; the headset hook, one headset (0) or two (1); the cork board of shift notes. Drawn by tools/draw_exchange_fittings.py.
 */
public final class ExchangeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ExchangeBlock> CODEC=simpleCodec(ExchangeBlock::new);
    public enum Kind implements StringRepresentable {
        JACK,CALLER,PANEL,DESK,HOOK,NOTES;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public ExchangeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PANEL).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.EXCHANGE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.EXCHANGE.get())&&s.getValue(KIND)==kind;}
    /** The lit lamps give a little light: the incoming line's most. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case CALLER->s.getValue(STAGE)>0?7:0;case JACK->s.getValue(STAGE)>0?3:0;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case JACK,CALLER,PANEL->box(f,0,0,6,16,16,16);
            case DESK->box(f,0,0,2,16,14,16);
            case HOOK->box(f,4,4,12,12,15,16);
            case NOTES->box(f,0,2,14,16,14,16);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return NightOperator.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    /** A listened jack's lamp goes out again. */
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos pos,RandomSource r){if(s.getValue(KIND)==Kind.JACK&&s.getValue(STAGE)==1)l.setBlock(pos,s.setValue(STAGE,0),Block.UPDATE_CLIENTS);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
