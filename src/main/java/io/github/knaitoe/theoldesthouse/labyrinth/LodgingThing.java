package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.550, The Ones Already Inside (docs/ONES_INSIDE_ROOMS_0_4_550.md): what is in the lodging house's rooms, drawn facing north by
 * tools/draw_lodging_things.py and turned by FACING. A thing set against a wall stands with its back to the south as drawn, so one
 * facing south stands against the wall to its north.
 * <ul>
 * <li>Two cells end to end (the beds, the cot): end 0 is the head and end 1 the cell FACING points to ({@link #ahead}). BED's stage 0
 * and 1 are a guest room's bed, 2 and 3 Vera's.</li>
 * <li>Two cells side by side (the settee, the sideboard, the work table, the scullery table): half 0 is the cell on the left as one
 * faces the front, half 1 the cell to its right ({@link #right}): STAGE's bit 0. The scullery table's bit 1 is the tray laid on its
 * half 0.</li>
 * <li>Two cells stacked (the wardrobe, the dresser, the range's flue): the upper is the cell above, stage 1.</li>
 * <li>LONG_TABLE: the dining room's table, two cells wide. Stage 0 a middle cell, its apron along its west edge as drawn; stage 1 an
 * end cell, its leg in its north-west corner as drawn. FACING turns the apron or the leg to its side.</li>
 * <li>Flat on a wall: the key hook (stage 0 with the key, 1 empty), a guest room's card holder (stage 0 empty, 1 with a card) and the
 * shelf under Daniel's hatch. Nothing a body bumps into; the rope on the cellar floor neither.</li>
 * </ul>
 */
public final class LodgingThing extends HorizontalDirectionalBlock {
    public static final MapCodec<LodgingThing> CODEC=simpleCodec(LodgingThing::new);
    public enum Kind implements StringRepresentable {
        BED,COT,WASHSTAND,CHAIR,ARMCHAIR,SETTEE,SIDE_TABLE,LONG_TABLE,SIDEBOARD,WORK_TABLE,KEY_HOOK,CARD_INDEX,WARDROBE,DRESSING_TABLE,
        CARD_HOLDER,SINK,RANGE,DRESSER,SCULLERY_TABLE,SHELF,PAIL,ROPE,CRATE,COAL;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** How many stages it draws; the rest draw stage 0. */
        public int stages(){return switch(this){case BED,SCULLERY_TABLE->4;case COT,SETTEE,LONG_TABLE,SIDEBOARD,WORK_TABLE,KEY_HOOK,CARD_INDEX,WARDROBE,CARD_HOLDER,RANGE,DRESSER->2;default->1;};}
        /** Flat on a wall or the floor: nothing a body bumps into. */
        public boolean flat(){return this==KEY_HOOK||this==CARD_HOLDER||this==SHELF||this==ROPE;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public LodgingThing(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CHAIR).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return LodgingRegistry.THING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static boolean is(BlockState s){return s.is(LodgingRegistry.THING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** What a body sits in: the chairs, the parlor's armchairs and settee. */
    public static boolean seat(BlockState s){return is(s,Kind.CHAIR)||is(s,Kind.ARMCHAIR)||is(s,Kind.SETTEE);}
    /** How high a seat holds the hips, in sixteenths of a block. */
    public static int seatHeight(BlockState s){return is(s,Kind.CHAIR)?10:9;}
    /** The cell to the right of a thing facing f, as one faces its front: half 1 of a thing two cells wide. */
    public static BlockPos right(BlockPos at,Direction f){return at.relative(f.getCounterClockWise());}
    /** The cell a thing's FACING points to: end 1 of a thing two cells long. */
    public static BlockPos ahead(BlockPos at,Direction f){return at.relative(f);}
    /** The range is in, low: its firebox glows. */
    public static int light(BlockState s){return s.getValue(KIND)==Kind.RANGE&&s.getValue(STAGE)==0?5:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case BED->(stage&1)==0?Shapes.or(Block.box(0,0,0,16,9,16),t(f,0,9,15,16,16,16)):Shapes.or(Block.box(0,0,0,16,9,16),t(f,0,9,0,16,11,1));
            case COT->stage==0?Shapes.or(t(f,2,0,0,14,6,16),t(f,2,6,15,14,10,16)):t(f,2,0,0,14,7,16);
            case WASHSTAND->t(f,0,0,3,16,16,16);
            case CHAIR->Shapes.or(t(f,3,0,3,13,10,13),t(f,3,10,12,13,16,13));
            case ARMCHAIR->Shapes.or(t(f,1,0,1,15,10,15),t(f,1,10,12,15,16,15),t(f,1,10,1,3,12,12),t(f,13,10,1,15,12,12));
            case SETTEE->(stage&1)==0?Shapes.or(t(f,0,0,1,15,10,15),t(f,0,10,12,15,16,15),t(f,13,10,1,15,12,12)):Shapes.or(t(f,1,0,1,16,10,15),t(f,1,10,12,16,16,15),t(f,1,10,1,3,12,12));
            case SIDE_TABLE->Block.box(3,0,3,13,12,13);
            case LONG_TABLE->Block.box(0,0,0,16,14,16);
            case SIDEBOARD->t(f,0,0,3,16,15,16);
            case WORK_TABLE,SCULLERY_TABLE->t(f,0,0,1,16,14,15);
            case KEY_HOOK->t(f,6,2,14,10,13,16);
            case CARD_INDEX->t(f,1,0,2,15,16,16);
            case WARDROBE->t(f,0,0,2,16,16,16);
            case DRESSING_TABLE->t(f,0,0,6,16,16,16);
            case CARD_HOLDER->t(f,5,6,15,11,9,16);
            case SINK->t(f,0,0,4,16,12,16);
            case RANGE->stage==0?t(f,0,0,2,16,14,16):t(f,6,0,12,10,16,16);
            case DRESSER->stage==0?t(f,0,0,3,16,14,16):t(f,0,0,11,16,16,16);
            case SHELF->t(f,1,9,10,15,16,16);
            case PAIL->Block.box(4,0,4,12,10,12);
            case ROPE->Block.box(3,0,3,13,2,13);
            case CRATE->Block.box(1,0,1,15,12,15);
            case COAL->Block.box(0,0,0,16,8,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(KIND).flat()?Shapes.empty():getShape(s,l,p,c);
    }
}
