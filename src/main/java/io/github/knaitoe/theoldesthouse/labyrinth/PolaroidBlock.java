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
 * 0.4.204, the Polaroid Trail: every fitting of the yard, the crawlspace, the cinema and the pines. Models face north as drawn
 * and turn with {@link #FACING}; a fitting on a wall has its back to that wall.
 *
 * <p>Stages: a dropped photograph face down (0) or face up (1); a balloon dark (0) or lit (1); the culvert grate, the lattice
 * vent and the floor hatch shut (0) or open (1); a cinema seat empty (0), folded (1) or with someone in it (2); the aisle light
 * and the fire exit sign lit (0) or out (1); the projector and the screen off (0) or running (1).
 */
public final class PolaroidBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PolaroidBlock> CODEC=simpleCodec(PolaroidBlock::new);
    public enum Kind implements StringRepresentable {
        TAG,PHOTO,BALLOON,RAFT,GRATE,VENT,BOWL,CLOTHES,HATCH,DRAIN,BATT,SEAT,AISLE_LIGHT,PROJECTOR,SCREEN,COUNTER,POSTER,ALBUM,LUNCHBOX,EXIT_SIGN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PolaroidBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TAG).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PolaroidRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PolaroidRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PolaroidRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stages(Kind k){return switch(k){case SEAT->3;case PHOTO,BALLOON,GRATE,VENT,HATCH,AISLE_LIGHT,PROJECTOR,SCREEN,EXIT_SIGN->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case BALLOON->stage==1?10:0;
            case AISLE_LIGHT->stage==0?6:0;
            case PROJECTOR->stage==1?10:0;
            case SCREEN->stage==1?7:0;
            case EXIT_SIGN->stage==0?5:0;
            default->0;
        };
    }
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case TAG->box(f,4,2,15,12,10,16);
            case PHOTO->Block.box(4,0,3,12,.5,13);
            case BALLOON->Block.box(3,2,3,13,14,13);
            case RAFT->Block.box(0,0,0,16,3,16);
            case GRATE,VENT->stage==0?box(f,0,0,14,16,16,16):box(f,0,0,0,1,16,16);
            case BOWL->Block.box(4,0,4,12,3,12);
            case CLOTHES->Block.box(0,0,0,16,4,16);
            case HATCH->stage==0?Block.box(0,13,0,16,16,16):box(f,0,0,0,16,16,3);
            case DRAIN->Block.box(5,0,5,11,16,11);
            case BATT->Block.box(1,6,2,15,16,14);
            case SEAT->Block.box(0,0,2,16,stage==2?16:9,14);
            case AISLE_LIGHT->box(f,5,0,15,11,4,16);
            case PROJECTOR->Block.box(3,0,2,13,13,14);
            case SCREEN,POSTER->box(f,0,0,15,16,16,16);
            case COUNTER->Shapes.block();
            case ALBUM->Block.box(3,0,3,13,2,13);
            case LUNCHBOX->Block.box(3,0,5,13,9,11);
            case EXIT_SIGN->box(f,2,4,15,14,12,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case TAG,PHOTO,BALLOON,BATT,AISLE_LIGHT,POSTER,ALBUM,EXIT_SIGN->Shapes.empty();
            case GRATE,VENT->s.getValue(STAGE)==0?getShape(s,l,p,c):Shapes.empty();
            case HATCH->s.getValue(STAGE)==0?getShape(s,l,p,c):Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
