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
 * 0.4.203, Work print: the fittings of the closed rental shop and the rooms behind it. Models face north as drawn and turn with
 * {@link #FACING}; a fitting on a wall has its back to that wall.
 *
 * <p>Stages, where a kind has more than one: a shelf's three runs of spines; the counter plain (0) or with the till (1); the
 * returns bin empty (0), with the tape (1) or with the stained tape (2); the phone quiet (0) or ringing (1); the mannequin
 * dressed (0) or bare (1); four posters; a ceiling tube lit (0) or flickering low (1); the CRT dark (0), showing snow (1) or
 * playing (2, the picture drawn by the client); the cart's decks empty (0), the first loaded (1), both loaded (2) or dubbing
 * (3); the splicer bare (0) or holding a reel (1); the light table off (0) or lit (1); three walls of continuity photographs;
 * the flatbed empty (0), threaded (1) or running (2, the picture drawn by the client); the set's bulb on (0) or off (1); the
 * camera on its tripod idle (0), running (1) or gone (2); a set flat papered (0) or its plywood back (1).
 */
public final class WorkPrintBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<WorkPrintBlock> CODEC=simpleCodec(WorkPrintBlock::new);
    public enum Kind implements StringRepresentable {
        SHELF,COUNTER,LEDGER,CARD,RETURNS,SLOT,PHONE,MANNEQUIN,POSTER,SECTION,TUBE,
        CRT,CART,BLANKS,CHAIR,TAPES,
        RACK,SPLICER,LIGHT_TABLE,PHOTOS,SCRIPT,CALL_SHEET,FLATBED,TRIM_BIN,
        DESK,MIC,
        FLAT,BULB,MARK,TRIPOD,SLATE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public WorkPrintBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.SHELF).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return WorkPrintRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(WorkPrintRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(WorkPrintRegistry.FITTING.get())?s.getValue(KIND):null;}
    /** How many stages each kind is drawn in. */
    public static int stages(Kind k){return switch(k){case POSTER,CART->4;case SHELF,RETURNS,CRT,PHOTOS,FLATBED,TRIPOD->3;case COUNTER->3;case PHONE,MANNEQUIN,TUBE,SPLICER,LIGHT_TABLE,BULB,FLAT->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case TUBE->stage==0?11:4;
            case CRT->stage==0?0:6;
            case LIGHT_TABLE->stage==1?9:0;
            case BULB->stage==0?12:0;
            case FLATBED->stage==2?5:0;
            case PHONE->stage==1?2:0;
            case TRIPOD->stage==1?3:0;
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
            case SHELF->box(f,0,0,2,16,16,14);
            case COUNTER,SPLICER,FLATBED,DESK->Shapes.block();
            case LEDGER->Block.box(3,0,3,13,2,13);
            case CARD->Block.box(4,0,4,12,1.5,12);
            case RETURNS->Block.box(2,0,2,14,12,14);
            case SLOT->box(f,3,8,13,13,12,16);
            case PHONE->Block.box(4,0,4,12,6,12);
            case MANNEQUIN,TRIPOD->Block.box(4,0,4,12,16,12);
            case POSTER->box(f,1,1,15,15,15,16);
            case SECTION->box(f,0,4,15,16,12,16);
            case TUBE->Block.box(2,13,6,14,16,10);
            case CRT->Block.box(1,0,1,15,14,15);
            case CART->Block.box(0,0,0,16,16,16);
            case TRIM_BIN->Block.box(1,0,1,15,16,15);
            case BLANKS->Block.box(3,0,4,13,6,12);
            case CHAIR->Block.box(3,0,3,13,15,13);
            case TAPES->Block.box(2,0,2,14,8,14);
            case RACK->box(f,0,0,8,16,16,16);
            case LIGHT_TABLE->Block.box(0,0,0,16,14,16);
            case PHOTOS->box(f,0,0,15,16,16,16);
            case SCRIPT->box(f,2,2,15,14,14,16);
            case CALL_SHEET->box(f,3,3,15,13,13,16);
            case MIC->Block.box(6,0,6,10,16,10);
            case FLAT->Shapes.block();
            case BULB->Block.box(6,6,6,10,16,10);
            case MARK->Block.box(0,0,0,16,0.5,16);
            case SLATE->Block.box(4,0,4,12,10,12);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case LEDGER,CARD,SLOT,POSTER,SECTION,TUBE,PHOTOS,SCRIPT,CALL_SHEET,BULB,MARK->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.FLAT?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
