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
 * 0.4.202, the Whistlers in the Pines: the fittings of the fire lookout. Models face north as drawn and turn with
 * {@link #FACING}; a fitting on a wall has its back to that wall, and a window faces out of the cab.
 *
 * <p>A window cell is one of five kinds, which is its blind: {@code PANE} (the canvas rolled up, the glass bare),
 * {@code SHUTTER} (the canvas down), {@code TORN} (the canvas torn away by the gale; the glass bare and no cord to pull),
 * {@code TARP} (a torn frame patched with a repair tarp) and {@code BROKEN} (the glass gone). On a pane and a torn frame
 * {@link #STAGE} is the frost on the glass, 0 to 3. The other kinds use their stage as drawn: a lamp lit (0) or out (1), the
 * pendulum still (0) or swung toward its facing (1), the teleprinter idle (0), receiving (1) or dead (2), the stove lit (0)
 * or cold (1), the generator stopped (0) or running (1), the shed lamp lit (0) or red (1), a landing lantern burning (0),
 * burning cold (1) or out (2), the crank hook holding the crank (0) or empty (1), a cot's two ends, the propane tank's two.
 */
public final class LookoutBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<LookoutBlock> CODEC=simpleCodec(LookoutBlock::new);
    public enum Kind implements StringRepresentable {
        PANE,SHUTTER,TORN,TARP,BROKEN,CORD,FIRE_FINDER,PENDULUM,QUADRANT_LAMP,CAB_LAMP,TELEPRINTER,RADIO,STOVE,DESK,BINDER,FIRE_LOG,COT,
        CRANK_HOOK,TARP_PILE,GENERATOR,SHED_LAMP,PROPANE,LANTERN,HATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The five kinds a window cell can be. */
        public boolean window(){return this==PANE||this==SHUTTER||this==TORN||this==TARP||this==BROKEN;}
        /** A window that something outside can be seen through, and so looked at. */
        public boolean open(){return this==PANE||this==TORN||this==BROKEN;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public LookoutBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PANE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return LookoutRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LookoutRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(LookoutRegistry.FITTING.get())?s.getValue(KIND):null;}
    /** How many stages each kind is drawn in. */
    public static int stages(Kind k){return switch(k){case PANE,TORN->4;case TELEPRINTER,LANTERN->3;case PENDULUM,QUADRANT_LAMP,CAB_LAMP,STOVE,COT,CRANK_HOOK,GENERATOR,SHED_LAMP,PROPANE->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case CAB_LAMP->stage==0?14:0;
            case QUADRANT_LAMP->stage==1?7:0;
            case TELEPRINTER->stage==1?4:0;
            case STOVE->stage==0?10:0;
            case SHED_LAMP->stage==0?12:6;
            case LANTERN->stage==0?12:stage==1?8:0;
            case GENERATOR->stage==1?3:0;
            default->0;
        };
    }
    /** A box as drawn facing north, turned to the fitting's facing. */
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case PANE,SHUTTER,TORN,TARP,BROKEN,DESK,HATCH->Shapes.block();
            case CORD->box(f,6,0,13,10,16,16);
            case FIRE_FINDER->Block.box(2,0,2,14,15,14);
            case PENDULUM->Block.box(5,2,5,11,16,11);
            case QUADRANT_LAMP->box(f,5,4,12,11,11,16);
            case CAB_LAMP->Block.box(5,7,5,11,16,11);
            case TELEPRINTER->Block.box(1,0,1,15,14,15);
            case RADIO->Block.box(2,0,4,14,8,12);
            case STOVE->Block.box(2,0,2,14,13,14);
            case BINDER->Block.box(3,0,2,13,4,14);
            case FIRE_LOG->Block.box(3,0,3,13,2,13);
            case COT->Block.box(0,0,0,16,7,16);
            case CRANK_HOOK->box(f,3,2,11,13,14,16);
            case TARP_PILE->Block.box(2,0,2,14,4,14);
            case GENERATOR->Block.box(0,0,1,16,14,15);
            case SHED_LAMP->box(f,5,3,10,11,11,16);
            case PROPANE->Block.box(0,0,2,16,14,14);
            case LANTERN->Block.box(5,1,5,11,12,11);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case CORD,PENDULUM,QUADRANT_LAMP,CAB_LAMP,BINDER,FIRE_LOG,CRANK_HOOK,SHED_LAMP,LANTERN->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return switch(s.getValue(KIND)){case SHUTTER,TARP,DESK,HATCH->Shapes.block();default->Shapes.empty();};}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).open();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
