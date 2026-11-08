package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.350, Growing Things: the Barretts' yard and kitchen, drawn facing north by tools/draw_growing_things_art.py.
 * <ul>
 * <li>SHOOT: shoots coming up where nothing was planted, in four heights (stage 0 to 3); no collision.</li>
 * <li>STEPS: the back step, bare with shoots at its edges (0), grown over (1), and gone under (2).</li>
 * <li>FLOOR: the kitchen floor over its boards: flat (0), a ridge (1), a tent with shoots in the gap (2), heaved and broken (3).</li>
 * <li>FRAME: the kitchen window, square (0), bowing in at the sill (1), split with shoots through the pane (2).</li>
 * <li>WASHING: on the line, a sheet (0), a shirt (1), a towel (2), a small dress (3). LINE: the bare line.</li>
 * <li>TABLE: the kitchen table's west (0) and east (1) halves. UP_TABLE: the girls' card table's halves.</li>
 * <li>COUNTER: the kitchen counter by the hall door. SPADE: a spade standing in the ground. NOTE: the Saturday list on the gate post.</li>
 * <li>DRAWING: a child's drawing held by a magnet to the fridge door behind it. BASKET: the washing basket, empty (0) and full (1).</li>
 * </ul>
 */
public final class GrowingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<GrowingBlock> CODEC=simpleCodec(GrowingBlock::new);
    public enum Kind implements StringRepresentable {
        SHOOT,STEPS,FLOOR,FRAME,WASHING,LINE,TABLE,UP_TABLE,COUNTER,SPADE,NOTE,DRAWING,BASKET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public GrowingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.SHOOT).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return GrowingRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(GrowingRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int st=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case SHOOT->Block.box(2,0,2,14,new int[]{5,8,12,16}[st],14);
            case STEPS->Block.box(0,0,0,16,st==2?12:8,16);
            case FLOOR->Block.box(0,0,0,16,new int[]{1,4,9,14}[st],16);
            case FRAME->f.getAxis()==Direction.Axis.Z?Block.box(0,0,6,16,16,10):Block.box(6,0,0,10,16,16);
            case WASHING,LINE->f.getAxis()==Direction.Axis.Z?Block.box(0,1,7,16,16,9):Block.box(7,1,0,9,16,16);
            case TABLE->Block.box(0,12,0,16,14,16);
            case UP_TABLE->Block.box(0,10,0,16,12,16);
            case COUNTER->Shapes.block();
            case SPADE->Block.box(5,0,7,11,16,9);
            case NOTE->Block.box(4,0,4,12,16,12);
            case DRAWING->MarshBlock.turned(f,3,4,15,13,14,16);
            case BASKET->Block.box(2,0,3,14,st==1?9:7,13);
        };
    }
    /** The shoots, the washing, the line and the drawing are walked through; the tables are walked under; the rest stand. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case SHOOT,WASHING,LINE,DRAWING->Shapes.empty();
            case TABLE->Shapes.or(Block.box(0,12,0,16,14,16),Block.box(1,0,1,15,12,15));
            case UP_TABLE->Shapes.or(Block.box(0,10,0,16,12,16),Block.box(2,0,2,14,10,14));
            case SPADE->Block.box(6,0,7,10,16,9);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){
        if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(GrowingThings.use(p,pos,s))return InteractionResult.CONSUME;
        String text=switch(s.getValue(KIND)){
            case SHOOT->"New shoots, thin, as if they came up in the dark. Nobody planted them.";
            case STEPS->s.getValue(STAGE)==2?"Where the back step was, the shoots stand thick.":"The back step. Shoots are coming up round its edges.";
            case FLOOR->"The kitchen floor's boards, lifting.";
            case FRAME->s.getValue(STAGE)==0?"The kitchen window.":"The kitchen window's frame, bowing in. Something is pushing at the sill from outside.";
            case WASHING->"Washing on the line, nearly dry.";
            case LINE->"The washing line.";
            case TABLE->"The kitchen table, under its oilcloth.";
            case UP_TABLE->"The girls' card table, set by the window.";
            case COUNTER->"The kitchen counter.";
            case SPADE->"A spade, standing where somebody left it.";
            case NOTE->"A list, tied to the gate post with twine.";
            case DRAWING->"A child's drawing on the fridge.";
            case BASKET->"The washing basket.";
        };
        p.displayClientMessage(net.minecraft.network.chat.Component.literal(text).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        return InteractionResult.CONSUME;
    }
}
