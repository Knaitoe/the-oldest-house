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
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md): Rowan's shop's and flat's own things, drawn by tools/draw_longer_art.py.
 * Models face north as drawn and turn with {@link #FACING}; a thing against a wall has its back to it, and a seat faces the way its
 * sitter looks.
 * <p>
 * Stages: the workbench's left end (0), middle (1) and right end (2); the lamp off (0), on (1) or on and turned onto the radio (2, 0.4.623); the radio off (0), playing (1) or with
 * its back panel off (2); the shelf's three loads (0-2); the kettle cold (0) or boiling (1); the two cups together (0), the cracked one
 * alone (1) or the whole one alone (2); the toaster up (0) or down (1); the coat hook with Rowan's coat (0) or empty (1); the window's
 * lower (0) and upper (1) halves; the worktop (0) or the worktop with the sink (1).
 */
public final class LongerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<LongerBlock> CODEC=simpleCodec(LongerBlock::new);
    public enum Kind implements StringRepresentable {
        WORKBENCH,TRAYS,LAMP,RADIO,CHAIR,ORDER,SHELF,COUNTER,KETTLE,CUPS,TOASTER,TABLE,ARMCHAIR,KITCHEN_CHAIR,HOOK,WINDOW,WORKTOP,BASIN,
        LAVATORY,BENCH,AWNING,TANK;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Something a body sits in. */
        public boolean seat(){return this==CHAIR||this==ARMCHAIR||this==KITCHEN_CHAIR||this==BENCH;}
        /** Where a sitter's seat is, in pixels. */
        public int seatHeight(){return switch(this){case ARMCHAIR->7;case BENCH->8;default->9;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public LongerBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.TABLE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return LongerRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LongerRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(LongerRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static int stage(BlockState s){return s.is(LongerRegistry.FITTING.get())?s.getValue(STAGE):-1;}
    public static boolean seat(BlockState s){return s.is(LongerRegistry.FITTING.get())&&s.getValue(KIND).seat();}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)>=1?12:s.getValue(KIND)==Kind.RADIO&&s.getValue(STAGE)==1?3:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case WORKBENCH,COUNTER,WORKTOP,TANK->Shapes.block();
            case SHELF->box(f,0,0,8,16,16,16);
            case TRAYS->box(f,1,0,3,15,4,13);
            case LAMP->Block.box(4,0,4,12,13,12);
            case RADIO->box(f,2,0,5,14,9,12);
            case CHAIR,KITCHEN_CHAIR->Shapes.or(Block.box(2,0,2,14,9,14),box(f,2,9,12,14,19,14));
            case ARMCHAIR->Shapes.or(Block.box(1,0,1,15,7,15),box(f,1,7,11,15,18,15),box(f,1,7,1,3,12,15),box(f,13,7,1,15,12,15));
            case BENCH->Shapes.or(Block.box(0,0,3,16,8,13),box(f,0,8,11,16,16,13));
            case ORDER->box(f,4,2,15,12,13,16);
            case KETTLE->Block.box(4,0,4,12,8,12);
            case CUPS->Block.box(2,0,5,14,5,11);
            case TOASTER->box(f,4,0,5,12,7,11);
            case TABLE->Shapes.or(Block.box(0,13,0,16,16,16),Block.box(7,0,7,9,13,9));
            case HOOK->box(f,4,4,12,12,16,16);
            case WINDOW->box(f,0,0,7,16,16,9);
            case BASIN->box(f,3,0,7,13,14,16);
            case LAVATORY->box(f,3,0,2,13,15,16);
            case AWNING->Block.box(0,0,0,16,2,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case ORDER,HOOK,TRAYS,CUPS,KETTLE,TOASTER->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k==Kind.COUNTER||k==Kind.WORKTOP||k==Kind.WORKBENCH||k==Kind.TANK?Shapes.block():Shapes.empty();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
