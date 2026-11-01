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
 */
public final class PolybiusBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PolybiusBlock> CODEC=simpleCodec(PolybiusBlock::new);
    public enum Kind implements StringRepresentable {
        CABINET,SCREEN,DEAD,DEAD_SCREEN,CHANGE,NOTICE,CLIPBOARD,NEON,STOOL,COUNTER,PRIZES,POSTER,BIN,LIGHT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PolybiusBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABINET).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PolybiusRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PolybiusRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PolybiusRegistry.FITTING.get())?s.getValue(KIND):null;}
    /** How many stages each kind is drawn in. */
    public static int stages(Kind k){return switch(k){case CABINET,SCREEN->4;case NEON,POSTER->3;case DEAD,DEAD_SCREEN,LIGHT->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case SCREEN->stage==3?5:8;
            case NEON->10;
            case CHANGE->4;
            case LIGHT->stage==0?9:0;
            case PRIZES->3;
            default->0;
        };
    }
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CABINET,DEAD->box(f,1,0,1,15,16,15);
            case SCREEN,DEAD_SCREEN->box(f,1,0,2,15,16,15);
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
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case NOTICE,CLIPBOARD,NEON,POSTER,LIGHT->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.COUNTER?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
