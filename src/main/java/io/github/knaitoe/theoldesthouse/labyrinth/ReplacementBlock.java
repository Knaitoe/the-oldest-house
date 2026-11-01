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
 * 0.4.218, the replacement: the street's and the open house's fittings. Models face north as drawn and turn with {@link #FACING};
 * a fitting on a wall has its back to that wall. Stages: a boarded door's lower (0) and upper (1) half; a street lamp lit (0) or
 * dead (1); the crate shut (0) or open (1); a blanket over one shape (0) or two (1); the poster on the pole LOST (0); the mailbox
 * empty (0) or with a page in it (1).
 */
public final class ReplacementBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ReplacementBlock> CODEC=simpleCodec(ReplacementBlock::new);
    public enum Kind implements StringRepresentable {
        BOARDS,BOARD_DOOR,POSTER,LAMP,MAILBOX,CRATE,CRATE_SIDE,COLLARS,BLANKET,CAT,DOG,BOWLS,LITTER,PAPERS,BAGS,CANS,SOFA;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public ReplacementBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOARDS).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return ReplacementRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(ReplacementRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(ReplacementRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stages(Kind k){return switch(k){case BOARD_DOOR,LAMP,CRATE,BLANKET,MAILBOX->2;default->1;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)==0?13:0;}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case BOARDS,BOARD_DOOR,CRATE_SIDE->Shapes.block();
            case CRATE->s.getValue(STAGE)==0?Shapes.block():box(f,0,0,15,2,16,16);
            case POSTER->box(f,2,2,15,14,14,16);
            case LAMP->Block.box(4,0,4,12,6,12);
            case MAILBOX->Block.box(4,0,3,12,14,13);
            case COLLARS->box(f,0,6,14,16,14,16);
            case BLANKET->Block.box(0,0,0,16,s.getValue(STAGE)==0?4:5,16);
            case CAT->Block.box(3,0,3,13,5,13);
            case DOG->Block.box(1,0,2,15,7,14);
            case BOWLS,LITTER,PAPERS,CANS->Block.box(1,0,1,15,4,15);
            case BAGS->Block.box(1,0,1,15,10,15);
            case SOFA->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case POSTER,COLLARS,BLANKET,BOWLS,LITTER,PAPERS,CANS,CAT->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.BOARDS||s.getValue(KIND)==Kind.BOARD_DOOR?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
