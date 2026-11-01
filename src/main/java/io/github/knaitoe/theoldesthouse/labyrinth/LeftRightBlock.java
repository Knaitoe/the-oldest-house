package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.205, the Left-Right Game: every fitting of the road, the toll, the Bluejay cut, Jubilation, the sinkhole and the salt.
 * Models face north as drawn and turn with {@link #FACING}; a fitting on a wall has its back to it.
 *
 * <p>Stages: the CB radio quiet (0) or live (1); a sign's sector (0 the toll's green, 1 the cut's brown, 2 Jubilation's
 * pastel, 3 the sinkhole's snow-caked); a sodium lamp lit (0) or out (1); a fluorescent tube on (0) or off (1); a sedan's paint
 * (0 to 3); the payphone booth's water (0 to 3); the key hook with its key (0) or empty (1); the maintenance gate shut (0) or
 * open (1); a dormer window dark (0), lit (1) or with someone behind its curtain (2); the wagon's hazards off (0) or on (1);
 * its tailgate shut (0) or down (1); Marjorie's lower half (0) or upper (1).
 */
public final class LeftRightBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<LeftRightBlock> CODEC=simpleCodec(LeftRightBlock::new);
    public enum Kind implements StringRepresentable {
        TTY,RADIO,LOGBOOK,CRATE,JERSEY,GANTRY,SIGN_L,SIGN_R,LAMP,FLUORESCENT,ARM,SEDAN,PAYPHONE,KEY_HOOK,GATE,DORMER,SIGN_BENT,WAGON,TAILGATE,MARJORIE,FLARE,POLE,REEDS,BARRICADE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** A flare burns this long, then goes out by itself. */
    public static final int BURN=240;
    public LeftRightBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TTY).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return LeftRightRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LeftRightRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(LeftRightRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stages(Kind k){return switch(k){case SIGN_L,SIGN_R,SEDAN,PAYPHONE->4;case DORMER->3;case RADIO,LAMP,FLUORESCENT,KEY_HOOK,GATE,WAGON,TAILGATE,MARJORIE->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case LAMP->stage==0?12:0;
            case FLUORESCENT->stage==0?10:0;
            case DORMER->stage==1?9:stage==2?5:0;
            case FLARE->15;
            case RADIO->stage==1?4:2;
            case WAGON->stage==1?6:0;
            case PAYPHONE->7;
            default->0;
        };
    }
    @Override protected void onPlace(BlockState s,Level l,BlockPos pos,BlockState old,boolean moving){
        if(s.getValue(KIND)==Kind.FLARE&&!l.isClientSide&&!(old.is(this)&&old.getValue(KIND)==Kind.FLARE))l.scheduleTick(pos,this,BURN);
    }
    /** A flare burnt down goes out. */
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos pos,RandomSource r){if(s.getValue(KIND)==Kind.FLARE)l.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case TTY->Block.box(3,0,3,13,8,13);
            case RADIO->Block.box(2,0,4,14,7,12);
            case LOGBOOK->Block.box(3,0,3,13,2,13);
            case CRATE->Block.box(1,0,2,15,10,14);
            case JERSEY->box(f,0,0,3,16,13,13);
            case GANTRY->Block.box(0,4,4,16,12,12);
            case SIGN_L,SIGN_R->box(f,0,0,14,16,16,16);
            case LAMP->Block.box(4,8,4,12,16,12);
            case FLUORESCENT->Block.box(2,13,6,14,16,10);
            case ARM->Block.box(7,0,7,9,16,9);
            case SEDAN,BARRICADE,WAGON->Shapes.block();
            case PAYPHONE->box(f,3,0,11,13,16,16);
            case KEY_HOOK->box(f,5,5,13,11,12,16);
            case GATE->stage==0?box(f,0,0,7,16,16,9):box(f,0,0,7,2,16,9);
            case DORMER->Shapes.block();
            case SIGN_BENT->Block.box(6,0,6,10,16,10);
            case TAILGATE->stage==0?box(f,0,0,12,16,14,16):Block.box(0,0,0,16,3,16);
            case MARJORIE->Block.box(4,0,5,12,16,11);
            case FLARE->Block.box(6,0,6,10,6,10);
            case POLE->Block.box(0,0,6,16,4,10);
            case REEDS->Block.box(2,0,2,14,14,14);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case GANTRY,SIGN_L,SIGN_R,LAMP,FLUORESCENT,KEY_HOOK,SIGN_BENT,MARJORIE,FLARE,REEDS,LOGBOOK->Shapes.empty();
            // A Jersey barrier and a shut chain-link gate are too high to step over.
            case JERSEY->box(s.getValue(FACING),0,0,3,16,24,13);
            case GATE->s.getValue(STAGE)==0?box(s.getValue(FACING),0,0,7,16,24,9):Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
