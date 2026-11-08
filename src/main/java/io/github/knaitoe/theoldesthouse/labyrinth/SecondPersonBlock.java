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
 * 0.4.274, Second Person (docs/SECOND_PERSON_0_4_274.md): the flat's own things, drawn by tools/draw_second_person_art.py. Models face
 * north as drawn and turn with {@link #FACING}; a thing against a wall has its back to it. Stages: a moving box taped (0), open and full
 * (1), open and empty (2) or flattened (3); the hall glass as it hangs (0) or turned to the wall (1); the bathroom mirror clear (0) or
 * steamed (1); the tank's lower (0) and upper (1) halves; the photograph standing (0) or face down (1); the routine item by its number
 * (0-15, as HouseReadMoments.ROUTINES); the fascia's three parts; the tablecloth rumpled (0), straight (1) or laid for two (2); the
 * telephone on its hook (0) or off it (1); 0.4.369: Iris's tin shut (0), open with the sleeve (1) or open without it (2); the sleeve on the
 * armchair's arm (0, drawn down onto the arm from the cell above), finished and folded on the seat (1) or set down flat anywhere (2).
 */
public final class SecondPersonBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SecondPersonBlock> CODEC=simpleCodec(SecondPersonBlock::new);
    public enum Kind implements StringRepresentable {
        BOOK,SHELF,BOX,GLASS,MIRROR,TANK,PHOTO,SHEET,COAT,ROUTINE,SIGN,STOP,ROTA,CLOTH,PHONE,
        /** 0.4.369, winter: Iris's sewing tin, the train ticket in its envelope, the sleeve she never finished. */
        TIN,TICKET,SLEEVE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,15);
    public SecondPersonBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOOK).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return SecondPersonRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(SecondPersonRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(SecondPersonRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stage(BlockState s){return s.is(SecondPersonRegistry.FITTING.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case BOOK->stage==1?Shapes.or(Block.box(0,0,0,16,8,16),box(f,4,8,3,12,9.5,13)):box(f,4,0,3,12,1.5,13);
            case SHELF->box(f,0,0,2,16,16,16);
            case BOX->stage==3?Block.box(0,0,1,16,1.5,15):Block.box(1,0,1,15,12,15);
            case GLASS->box(f,3,1,15,13,15,16);
            case MIRROR->box(f,2,2,12,14,14,16);
            case TANK->stage==0?Block.box(2,0,2,14,16,14):Block.box(2,0,2,14,10,14);
            case PHOTO->stage==1?box(f,4,0,3,12,1,12):box(f,4,0,7,12,9,11);
            case SHEET,ROUTINE->Block.box(2,0,2,14,1,14);
            case COAT->Block.box(2,0,3,14,3,13);
            case SIGN->box(f,0,0,14,16,16,16);
            case STOP->Block.box(6,0,6,10,16,10);
            case ROTA->box(f,3,2,15,13,14,16);
            case CLOTH->Block.box(0,0,0,16,1,16);
            case PHONE->box(f,2.5,0,4,13.5,5,12);
            case TIN->stage==0?Block.box(3.5,0,3.5,12.5,5,12.5):Block.box(3,0,3,13,8,13);
            case TICKET->Block.box(3,0,4,14,1,12);
            case SLEEVE->stage==2?Block.box(3,0,2,13,1,14):Block.box(2,0,2,14,1,14);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case SHELF,SIGN->getShape(s,l,p,c);
            case BOX->s.getValue(STAGE)==3?Shapes.empty():getShape(s,l,p,c);
            case TANK,STOP->getShape(s,l,p,c);
            case BOOK->s.getValue(STAGE)==1?getShape(s,l,p,c):Shapes.empty();
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
