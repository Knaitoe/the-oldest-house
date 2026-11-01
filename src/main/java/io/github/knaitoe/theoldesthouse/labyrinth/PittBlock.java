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
 * 0.4.207, the God of the Pitt (docs/GOD_OF_THE_PITT_0_4_207.md): every fitting of the deeper passages under Ted the Caver's
 * cave. Models face north as drawn and turn with {@link #FACING}; a fitting on a wall has its back to that wall.
 *
 * <p>Stages: a pinch-ceiling slab's drop (0 up, 6 down on whoever is under it; it is drawn lower into the crawl below, and has
 * no more collision than the ceiling block it is); a seam's aperture (0 shut to 4 cut through, when it stops being solid); a
 * fissure plain (0), with the two eyes in its crack (1), or with a pale hand reached out of it (2); a bolt bare (0) or with the
 * orange line clipped to it (1); Brad's head with his lamp off (0), flashing white (1) or strobing amber (2); the belay held (0)
 * or its line hanging slack (1); the round stone's sixteen faces (two kinds of eight); Brad's helmet's lamp dark (0) or lit (1).
 * The bridges over the ledge's void are solid and never drawn.
 */
public final class PittBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PittBlock> CODEC=simpleCodec(PittBlock::new);
    public enum Kind implements StringRepresentable {
        SLAB,SEAM,FISSURE,ANCHOR,CRATE,BRAD,BRAD_HEAD,BELAY,NOTE,HOLE,WHEEL,WHEEL_LOW,GROOVE,HELMET,LINE,CUT,CANISTER,TINS,COIL,LANTERN,BRIDGE,JOURNAL,CEILING,RUBBLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    public PittBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.SLAB).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PittRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PittRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PittRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stages(Kind k){return switch(k){case WHEEL,WHEEL_LOW->8;case SLAB->7;case SEAM->5;case FISSURE,BRAD_HEAD->3;case ANCHOR,BELAY,HELMET->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case LANTERN->9;
            case BRAD_HEAD->stage==1?12:stage==2?9:0;
            case HELMET->stage==1?7:0;
            case FISSURE->stage==1?2:0;
            default->0;
        };
    }
    @Override protected RenderShape getRenderShape(BlockState s){return s.getValue(KIND)==Kind.BRIDGE?RenderShape.INVISIBLE:RenderShape.MODEL;}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case SLAB,SEAM,FISSURE,ANCHOR,CRATE,HOLE,WHEEL,WHEEL_LOW,GROOVE,BRIDGE,RUBBLE,CUT->Shapes.block();
            case BRAD->Block.box(2,0,3,14,8,13);
            case BRAD_HEAD->Block.box(3,0,3,13,11,13);
            case BELAY->Block.box(6,0,6,10,13,10);
            case NOTE->box(f,4,3,15,12,13,16);
            case HELMET->Block.box(4,0,4,12,6,12);
            case LINE->box(f,6,0,0,10,1,16);
            case CANISTER->Block.box(4,0,4,12,10,12);
            case TINS->Block.box(2,0,3,14,6,13);
            case COIL->Block.box(2,0,2,14,5,14);
            case LANTERN->Block.box(5,0,5,11,11,11);
            case JOURNAL->Block.box(1,0,2,15,7,14);
            case CEILING->Block.box(0,8,0,16,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            // A seam cut through to its last stage is an opening; the stone round it is drawn, not felt.
            case SEAM->s.getValue(STAGE)>=4?Shapes.empty():Shapes.block();
            case NOTE,HELMET,LINE,COIL->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
