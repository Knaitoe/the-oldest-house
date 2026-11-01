package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.217, Polybius: the arcade's fittings. Models face north as drawn and turn with {@link #FACING}; a fitting on a wall has its
 * back to that wall.
 *
 * <p>A cabinet is two blocks, its body below ({@link Kind#CABINET}, the control panel) and its screen and marquee above
 * ({@link Kind#SCREEN}); their stage is the game: Mothlight (0), Undertow (1), Signal (2), and the black cabinet with no maker's
 * name (3). Out-of-order cabinets ({@link Kind#DEAD} and {@link Kind#DEAD_SCREEN}) come in two looks. Neon strips come in three
 * colours, posters in three, and a ceiling light is lit (0) or dead (1).
 *
 * <p>0.4.221: the prize shelf's eight cells each hold one prize with its price (stage 0 to 7); Block Party, the four-player
 * cabinet, is two blocks wide, its left half stage 0 and its right half stage 1 ({@link Kind#BRAWL} and {@link Kind#BRAWL_SCREEN});
 * and three cabinets the arcade's regulars are playing ({@link Kind#OTHER} and {@link Kind#OTHER_SCREEN}, one stage a game).
 */
public final class PolybiusBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PolybiusBlock> CODEC=simpleCodec(PolybiusBlock::new);
    public enum Kind implements StringRepresentable {
        CABINET,SCREEN,DEAD,DEAD_SCREEN,CHANGE,NOTICE,CLIPBOARD,NEON,STOOL,COUNTER,PRIZES,POSTER,BIN,LIGHT,BRAWL,BRAWL_SCREEN,OTHER,OTHER_SCREEN,
        /** 0.4.224 (owner: "add a pizza and soda booth/vendor"): the pizza counter and what is on and behind it. */
        PIZZA_COUNTER,BACK_COUNTER,PIZZA_WARMER,SODA,PIZZA_OVEN,PIZZA_BOXES,NAPKINS,MENU_BOARD;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    public PolybiusBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABINET).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PolybiusRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PolybiusRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PolybiusRegistry.FITTING.get())?s.getValue(KIND):null;}
    /** How many stages each kind is drawn in. */
    public static int stages(Kind k){return switch(k){case PRIZES->8;case CABINET,SCREEN->4;case NEON,POSTER,OTHER,OTHER_SCREEN->3;case DEAD,DEAD_SCREEN,LIGHT,BRAWL,BRAWL_SCREEN->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case SCREEN->stage==3?5:8;
            case BRAWL_SCREEN,OTHER_SCREEN->8;
            case NEON->10;
            case CHANGE->4;
            case LIGHT->stage==0?9:0;
            case PRIZES->3;
            case PIZZA_WARMER->10;
            case SODA->3;
            default->0;
        };
    }
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CABINET,DEAD,OTHER->box(f,1,0,1,15,16,15);
            case SCREEN,DEAD_SCREEN,OTHER_SCREEN->box(f,1,0,2,15,16,15);
            case BRAWL->box(f,0,0,1,16,16,15);
            case BRAWL_SCREEN->box(f,0,0,2,16,16,15);
            case CHANGE->box(f,2,0,6,14,16,16);
            case NOTICE->box(f,1,1,15,15,15,16);
            case CLIPBOARD->Block.box(4,0,3,12,1,13);
            case NEON->box(f,0,6,15,16,10,16);
            case STOOL->Block.box(4,0,4,12,10,12);
            case COUNTER->Shapes.block();
            case PRIZES->box(f,0,0,8,16,16,16);
            case POSTER->box(f,1,1,15,15,15,16);
            case BIN->Block.box(4,0,4,12,12,12);
            case LIGHT->Block.box(2,14,2,14,16,14);
            case PIZZA_COUNTER,BACK_COUNTER->Shapes.block();
            case PIZZA_WARMER->box(f,1,0,3,15,12,13);
            case SODA->box(f,1,0,2,15,14,15);
            case PIZZA_OVEN->box(f,0,0,0,16,10,15);
            case PIZZA_BOXES->Block.box(2,0,2,14,7,14);
            case NAPKINS->Block.box(3,0,6,14,6,10);
            case MENU_BOARD->box(f,0,2,15,16,14,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case NOTICE,CLIPBOARD,NEON,POSTER,LIGHT,MENU_BOARD->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k==Kind.COUNTER||k==Kind.PIZZA_COUNTER||k==Kind.BACK_COUNTER?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
