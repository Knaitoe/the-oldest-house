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
 * 0.4.206, the Dead Air Broadcast: every fitting of the basement, the hallway in its three tunings, the soundstage, the marsh
 * relay and the master booth. Models face north as drawn and turn with {@link #FACING}.
 *
 * <p>Stages: the receiver's channel (0 static, 1 the stage, 2 the marsh, 3 dead air); a lamp's channel colour (the same four);
 * the Zenith's picture (0 snow, 1 Horace, 2 the stage, 3 dark); Percy's body (0) or head (1); a lever or breaker up (0) or
 * pulled (1); the bulkhead locked (0) or open (1); the stage chest with its tube (0) or empty (1); the Skin-Taker's legs, body and head
 * (0 to 2 loose, 3 to 5 frozen in dead air); its arm loose (0) or frozen solid (1); a dish's bearing in eighths (0 to 7, 0 north); a signal bulb off
 * (0), red (1), amber low (2), amber high (3), green (4) or cyan (5); a curtain shut (0) or open (1); a monitor's snow (0) or
 * Percy's marquee (1).
 */
public final class DeadAirBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DeadAirBlock> CODEC=simpleCodec(DeadAirBlock::new);
    public enum Kind implements StringRepresentable {
        RECEIVER,CRT,PERCY,CRATE,LAMP,LEVER,BREAKER,BULKHEAD,CHEST,SKIN,ARM,DISH,MAST,BULB,TELEPROMPTER,CURTAIN,MONITOR,HIGHCHAIR,STRINGS,FLAT,BRACE,WAVE,SPOT,GRASS,PRINTOUTS,TABLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    public DeadAirBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.RECEIVER).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return DeadAirRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(DeadAirRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(DeadAirRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stages(Kind k){return switch(k){case DISH->8;case BULB->6;case RECEIVER,LAMP,CRT->4;case SKIN->6;case PERCY,LEVER,BREAKER,BULKHEAD,CHEST,ARM,CURTAIN,MONITOR,SPOT->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case LAMP->switch(stage){case 0->12;case 1->10;case 2->8;default->15;};
            case CRT->stage==3?0:6;
            case RECEIVER->4;
            case BULB->stage==0?0:stage==5?12:8;
            case SPOT->stage==1?12:0;
            case MONITOR->stage==1?10:6;
            case TELEPROMPTER->7;
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
            case RECEIVER->Block.box(1,0,3,15,8,13);
            case CRT,MONITOR->Block.box(1,0,1,15,14,15);
            case PERCY->stage==0?Block.box(4,0,5,12,16,11):Block.box(4,0,5,12,8,11);
            case CRATE,TABLE->Shapes.block();
            case LAMP->Block.box(4,12,4,12,16,12);
            case LEVER,BREAKER->box(f,5,3,12,11,13,16);
            case BULKHEAD->stage==0?Shapes.block():box(f,0,0,0,2,16,16);
            case CHEST->Block.box(1,0,1,15,14,15);
            case SKIN->Block.box(3,0,4,13,16,12);
            case ARM->Block.box(0,6,5,16,12,11);
            case DISH->Block.box(1,0,1,15,12,15);
            case MAST->Block.box(4,0,4,12,16,12);
            case BULB->Block.box(5,4,5,11,12,11);
            case TELEPROMPTER->Block.box(2,0,2,14,16,14);
            case CURTAIN->stage==0?box(f,0,0,6,16,16,10):box(f,0,0,6,2,16,10);
            case HIGHCHAIR->Block.box(3,0,3,13,16,13);
            case STRINGS,GRASS,PRINTOUTS->Block.box(4,0,4,12,12,12);
            case FLAT->box(f,0,0,13,16,16,16);
            case BRACE->Block.box(2,0,2,14,10,14);
            case WAVE->box(f,0,0,6,16,10,10);
            case SPOT->Block.box(4,10,4,12,16,12);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case LAMP,LEVER,BREAKER,BULB,STRINGS,GRASS,PRINTOUTS,SPOT->Shapes.empty();
            // The Skin-Taker has a body only while the air is dead; otherwise it is strings and burlap and nothing to push against.
            case SKIN->frozen(s)?getShape(s,l,p,c):Shapes.empty();
            case ARM->s.getValue(STAGE)==1?getShape(s,l,p,c):Shapes.empty();
            case CURTAIN->s.getValue(STAGE)==0?getShape(s,l,p,c):Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    /** A Skin-Taker block is solid once frozen: its legs, body and head are stages 0 to 2 loose and 3 to 5 frozen. */
    static boolean frozen(BlockState s){return s.getValue(STAGE)>=3;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
