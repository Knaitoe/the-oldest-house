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
 * 0.4.348, The Supper: the Marsh farmhouse's own things, drawn facing north by tools/draw_marsh_supper_art.py.
 * <ul>
 * <li>MAT: the coir doormat at the top of the porch steps.</li>
 * <li>KEYS: a set of car keys on their fob, lying on the side by the butcher's block.</li>
 * <li>COAT: stage 0 on the hook by the front door (the hook in the wall behind it); stage 1 over the back of a kitchen chair.</li>
 * <li>PORCH_LIGHT: the lamp on the wall by the front door; stage 1 dark.</li>
 * <li>WINDOW: an upstairs window, lit behind its curtains; stage 1 dark.</li>
 * <li>BREAD: the bread board with its loaf. ROAST: stage 0 under its cover on the platter, stage 1 uncovered and carved.</li>
 * <li>BUTCHER: the butcher's block in the kitchen. BELL: the dinner bell in its yoke, on a post. CARD: the supper card on its post.</li>
 * </ul>
 */
public final class MarshBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<MarshBlock> CODEC=simpleCodec(MarshBlock::new);
    public enum Kind implements StringRepresentable {
        MAT,KEYS,COAT,PORCH_LIGHT,WINDOW,BREAD,ROAST,BUTCHER,BELL,CARD;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public MarshBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.MAT).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return MarshRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(MarshRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return s.getValue(STAGE)!=0?0:switch(s.getValue(KIND)){case PORCH_LIGHT->13;case WINDOW->10;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    /** A box drawn facing north, turned to the block's facing. */
    static VoxelShape turned(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        return switch(f){
            case EAST->Block.box(16-z1,y0,x0,16-z0,y1,x1);
            case SOUTH->Block.box(16-x1,y0,16-z1,16-x0,y1,16-z0);
            case WEST->Block.box(z0,y0,16-x1,z1,y1,16-x0);
            default->Block.box(x0,y0,z0,x1,y1,z1);
        };
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case MAT->Block.box(1,0,1,15,1,15);
            case KEYS->Block.box(4,0,6,14,1,10);
            case COAT->s.getValue(STAGE)==0?turned(f,2,3,13,14,15,16):Block.box(2,0,2,14,16,14);
            case PORCH_LIGHT->turned(f,4.5,4,8.5,11.5,13,16);
            case WINDOW->Shapes.block();
            case BREAD->Block.box(2,0,4,14,5,12);
            case ROAST->Block.box(1,0,1,15,s.getValue(STAGE)==0?8:5,15);
            case BUTCHER->Block.box(1,0,1,15,16,15);
            case BELL->Block.box(2,0,5,14,13,11);
            case CARD->Block.box(4,0,4,12,16,12);
        };
    }
    /** The mat, the keys, the light, the bread and the roast are walked over or reached past; the coat on its hook too. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case WINDOW,BUTCHER->Shapes.block();
            case COAT->s.getValue(STAGE)==0?Shapes.empty():Block.box(2,0,2,14,8,14);
            case BELL->Block.box(2,0,6,14,13,10);
            case CARD->Block.box(5,0,5,11,16,11);
            default->Shapes.empty();
        };
    }
}
