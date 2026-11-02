package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.257, A Place for You (docs/A_PLACE_FOR_YOU_0_4_257.md): Mara's house's own things, drawn by tools/draw_place_for_you_art.py.
 * Models face north as drawn and turn with {@link #FACING}; a thing against a wall has its back to it. Stages: Kit's bag packed (0),
 * open (1) or unpacked into a pile (2); a plate with supper (0), empty (1) or three stacked (2); the cups empty (0) or poured (1);
 * Theo's photograph standing (0) or face down (1); the cupboard door's lower (0) and upper (1) halves; the routine item by its number
 * (0-15, as HouseReadMoments.ROUTINES); a counter (0) or the counter with the sink (1); the stove cold (0) or lit (1); the hall glass
 * as it hangs (0), turned to the wall (1) or covered (2).
 */
public final class PlaceForYouBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PlaceForYouBlock> CODEC=simpleCodec(PlaceForYouBlock::new);
    public enum Kind implements StringRepresentable {
        TABLE,BAG,CARD,PLATE,KETTLE,CUPS,RADIO,PHOTO,PICTURES,CUPBOARD,ROUTINE,SPOON,COUNTER,STOVE,HOOKS,GLASS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,15);
    public PlaceForYouBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PlaceForYouRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(PlaceForYouRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PlaceForYouRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stage(BlockState s){return s.is(PlaceForYouRegistry.FITTING.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case TABLE,CUPBOARD,COUNTER,STOVE->Shapes.block();
            case BAG->s.getValue(STAGE)==2?Block.box(3,0,4,13,5,12):box(f,3,0,4,13,s.getValue(STAGE)==1?11:9,13);
            case CARD->box(f,5,0,7,11,4,9);
            case PLATE->Block.box(2,0,2,14,s.getValue(STAGE)==2?3:1,14);
            case KETTLE->Block.box(5,0,3,11,8,11);
            case CUPS->Block.box(3,0,6,13,4,9);
            case RADIO->box(f,2,0,5,14,9,12);
            case PHOTO->s.getValue(STAGE)==1?Block.box(4,0,3,12,1,13):box(f,4,0,7,12,10,11);
            case PICTURES->box(f,0,0,15,16,16,16);
            case ROUTINE,SPOON->Block.box(2,0,2,14,1,14);
            case HOOKS->box(f,1,1,13,15,12,16);
            case GLASS->box(f,2,0,14,14,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case TABLE,CUPBOARD,COUNTER,STOVE->Shapes.block();
            case BAG,RADIO,KETTLE->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k==Kind.COUNTER||k==Kind.STOVE||k==Kind.CUPBOARD?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
