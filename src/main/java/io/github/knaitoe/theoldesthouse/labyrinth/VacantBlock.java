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
 * 0.4.228, Vacant Possession: the flat's furniture, what each piece was hiding, and the street's fittings. Models face north as
 * drawn and turn with {@link #FACING}; a piece against a wall has its back to that wall. Stages: the bed's head (0), foot (1) or the
 * bed stood on end in the van (2); the wardrobe's lower (0) and upper (1) halves; the rug laid (0) or rolled (1); the standard lamp
 * lit (0) or off (1); the pencil marks and the unfaded square's lower (0) and upper (1) halves; a bulb lit (0) or off (1); the van's
 * rear doors' lower (0) and upper (1) halves; a street lamp's post (0) and head (1).
 */
public final class VacantBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<VacantBlock> CODEC=simpleCodec(VacantBlock::new);
    public enum Kind implements StringRepresentable {
        DRESSER,RUG,BED,WARDROBE,TABLE,ARMCHAIR,BOXES,LAMP,CHAIR,MARKS,SCRATCHES,SQUARE,LIST,DOCKET,BULB,VAN_DOOR,MAILBOXES,STREETLAMP,RADIATOR,HOOKS,
        /** 0.4.260: a van wheel, on the outside of its side, so the load bay's walls are plain panel inside. */
        VAN_WHEEL;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The pieces that go down to the van, the last chair among them. */
        public boolean piece(){return ordinal()<=CHAIR.ordinal();}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public VacantBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOXES).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return VacantRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(VacantRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(VacantRegistry.FITTING.get())?s.getValue(KIND):null;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP->s.getValue(STAGE)==0?12:0;case BULB->s.getValue(STAGE)==0?14:0;case STREETLAMP->s.getValue(STAGE)==1?15:0;default->0;};}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case DRESSER->box(f,1,0,5,15,14,16);
            case RUG->stage==0?Block.box(0,0,0,16,1,16):Block.box(1,0,5,15,5,11);
            case BED->stage==2?box(f,1,0,9,15,16,16):Block.box(0,0,0,16,9,16);
            case WARDROBE->box(f,1,0,3,15,16,16);
            case TABLE->Block.box(1,0,1,15,13,15);
            case ARMCHAIR->box(f,1,0,1,15,13,16);
            case BOXES->Block.box(1,0,1,15,14,15);
            case LAMP->Block.box(4,0,4,12,16,12);
            case CHAIR->box(f,3,0,3,13,16,14);
            case MARKS,SQUARE->box(f,0,0,15.5,16,16,16);
            case SCRATCHES->Block.box(0,0,0,16,.25,16);
            case LIST->box(f,4,2,15.5,12,14,16);
            case DOCKET->Block.box(3,0,3,13,1,13);
            case BULB->Block.box(6,8,6,10,16,10);
            case VAN_DOOR->box(f,0,0,13,16,16,16);
            case MAILBOXES->box(f,0,2,11,16,16,16);
            case STREETLAMP->stage==0?Block.box(6,0,6,10,16,10):Block.box(4,0,0,12,6,16);
            case RADIATOR->box(f,1,1,12,15,11,16);
            case HOOKS->box(f,1,6,13,15,14,16);
            case VAN_WHEEL->box(f,1,0,13,15,14,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            // The chair holds its sitter at the seat: low collision, so the tenant may sit in it.
            case CHAIR->Block.box(3,0,3,13,8,14);
            case RUG,MARKS,SCRATCHES,SQUARE,LIST,DOCKET,BULB,HOOKS->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
