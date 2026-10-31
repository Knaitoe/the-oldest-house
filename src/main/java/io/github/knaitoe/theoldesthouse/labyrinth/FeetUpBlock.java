package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.200, Feet Up: the fittings of the house the children's game is played in. Models face north as drawn and turn with
 * {@link #FACING}; a mark on a wall has its back to that wall. {@link #STAGE}: a two-block fitting's upper half is 1; a lamp
 * that is out is 1; a chalk mark's word.
 *
 * The pieces a reader stands on while the floor is lava: the loose boards upstairs (a full block that gives way under a
 * reader, see FeetUp), the children's mattress (which throws a reader back up higher than they came down), the pots on the
 * kitchen rack (a hand's width to stand on), the dumbwaiter's cage, the window seat, the counters and the press. The stove
 * burns a foot that touches its top. The lamps light the rooms and go out together when the shouting peaks.
 */
public final class FeetUpBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FeetUpBlock> CODEC=simpleCodec(FeetUpBlock::new);
    public enum Kind implements StringRepresentable {
        LOOSE_BOARD,MATTRESS,POT,POT_RAIL,CAGE,WINDOW_SEAT,COUNTER,STOVE,FRIDGE,PRESS,LAMP,FLOOR_LAMP,CEILING_LIGHT,BATH,
        RULES_PAGE,NOTEBOOK,CHALK,TALLY,BLANKETS,COBWEB_CORNER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** The chalk's words, by stage: BASE, an arrow, a scrawl too small to read, and WAIT FOR ME by the hatch. */
    public static final int CHALK_BASE=0,CHALK_ARROW=1,CHALK_SCRAWL=2,CHALK_WAIT=3;
    /** How much faster a reader leaves the mattress than they came down on it, and the most it ever throws them. */
    public static final double BOUNCE=1.6,BOUNCE_CAP=.8;
    public FeetUpBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.LOOSE_BOARD).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return FeetUpRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(FeetUpRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** Which stages a kind is drawn in: the two halves, a lamp lit or out (the floor lamp: its base, its shade lit, its shade out), the bath's two ends, the four chalk words; a loose board that has fallen lies flat (1). */
    public static int stages(Kind k){return switch(k){case LOOSE_BOARD,FRIDGE,PRESS,LAMP,CEILING_LIGHT,BATH->2;case FLOOR_LAMP->3;case CHALK->4;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        return switch(s.getValue(KIND)){
            case LAMP,CEILING_LIGHT->s.getValue(STAGE)==0?14:0;
            case FLOOR_LAMP->s.getValue(STAGE)==1?13:0;
            case STOVE->5;
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
        var f=s.getValue(FACING);boolean upper=s.getValue(STAGE)>=1;
        return switch(s.getValue(KIND)){
            case LOOSE_BOARD->upper?Block.box(0,0,0,16,2,16):Shapes.block();
            case CAGE,FRIDGE,PRESS->Shapes.block();
            case COUNTER->Block.box(0,0,0,16,15,16);
            case STOVE->Block.box(0,0,0,16,14,16);
            case MATTRESS->Block.box(0,0,0,16,9,16);
            case WINDOW_SEAT->Block.box(0,0,0,16,8,16);
            case BATH->Block.box(0,0,0,16,10,16);
            case POT->Block.box(4,6,4,12,12,12);
            case POT_RAIL->box(f,0,12,6,16,14,10);
            case LAMP->Block.box(5,0,5,11,12,11);
            case FLOOR_LAMP->upper?Block.box(3,0,3,13,10,13):Block.box(6,0,6,10,16,10);
            case CEILING_LIGHT->Block.box(3,13,3,13,16,13);
            case RULES_PAGE->Block.box(3,0,2,13,1,14);
            case NOTEBOOK->Block.box(4,0,3,12,2,13);
            case BLANKETS->Block.box(0,0,0,16,5,16);
            case CHALK,TALLY->box(f,0,0,15,16,16,16);
            case COBWEB_CORNER->box(f,8,8,8,16,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case RULES_PAGE,NOTEBOOK,CHALK,TALLY,COBWEB_CORNER,POT_RAIL,CEILING_LIGHT->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return switch(s.getValue(KIND)){case LOOSE_BOARD->s.getValue(STAGE)==0?Shapes.block():Shapes.empty();case CAGE,FRIDGE,PRESS->Shapes.block();default->Shapes.empty();};}
    /** The mattress: a fall onto it costs nothing and sends the reader back up, faster than they came down. */
    @Override public void fallOn(Level l,BlockState s,BlockPos p,Entity e,float distance){
        if(s.getValue(KIND)==Kind.MATTRESS){super.fallOn(l,s,p,e,0);return;}
        super.fallOn(l,s,p,e,distance);
    }
    @Override public void updateEntityAfterFallOn(BlockGetter l,Entity e){
        var s=l.getBlockState(e.getOnPos());
        if(is(s,Kind.MATTRESS)&&!e.isSuppressingBounce()){
            Vec3 d=e.getDeltaMovement();
            if(d.y<0){double up=Math.min(BOUNCE_CAP,-d.y*BOUNCE);e.setDeltaMovement(d.x,up,d.z);e.hasImpulse=true;return;}
        }
        super.updateEntityAfterFallOn(l,e);
    }
    /** The stove's top is hot: a foot on it is burnt, as the rules said the floor would be. */
    @Override public void stepOn(Level l,BlockPos p,BlockState s,Entity e){
        if(s.getValue(KIND)==Kind.STOVE&&e instanceof LivingEntity living&&!e.isSteppingCarefully()&&l instanceof ServerLevel)living.hurt(l.damageSources().hotFloor(),1F);
        super.stepOn(l,p,s,e);
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
