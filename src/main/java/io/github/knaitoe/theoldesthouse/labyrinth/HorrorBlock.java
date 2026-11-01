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
 * 0.4.209 onward, the horrors of the dark halls (docs/DARK_HALL_HORRORS_PROPOSAL.md): every fitting the five rooms need, in one
 * block. Models face north as drawn and turn with {@link #FACING}; a fitting on a wall has its back to that wall.
 *
 * <p>The closing hall's: Daisy's crayon drawing on the floor (the source), Karen's interview in a folder on a stool (the account,
 * in the room past the squeeze, and a second copy by the way in for a reader who comes back to read it), the children's crayons
 * scattered at the far end, and a small handprint. The handprint has no shape at all: it is only ever shown to one reader, on the
 * inside of a door, and must never stop a hand or a step.
 *
 * <p>The storeroom's (0.4.210): steel shelving, bare, or holding the black ink, the needles in their packs, or the reader's own
 * thing with a tag on it (each a full block of shelf); the bulb on its pull chain (stage 0 out, 1 lit, 2 and 3 its chain swung one
 * way and the other, lit); the list in the reader's hand pinned beside the door (the source); boxes; and the footnote page by the
 * way in (the account).
 *
 * <p>The stillness's (0.4.211): a surveyor's page nailed to the wall by the way in (the source), a count scratched into the dust
 * before the far door in rows of five (the account), and a cloven print pressed into the dust where the thing nobody sees has
 * stood or walked. The print has no shape: it must never stop a step, and nobody can pick at it.
 *
 * <p>The line's (0.4.212): a luggage tag on the knot where the line is tied round the doorframe (the source), the explorer's pack at
 * the end of the line, and the tape recorder on it (the account).
 *
 * <p>Your own light's (0.4.213): words scratched into the lobby wall (the source), and the stub of a torch burnt down to nothing in
 * the far room (the account).
 */
public final class HorrorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HorrorBlock> CODEC=simpleCodec(HorrorBlock::new);
    public enum Kind implements StringRepresentable {
        DRAWING,TRANSCRIPT,CRAYONS,HANDPRINT,
        SHELF,SHELF_INK,SHELF_NEEDLES,SHELF_OWN,BULB,LIST,BOXES,FOOTNOTE,
        LOG,TALLY,HOOF,
        TAG,PACK,RECORDER,
        SCRATCH,STUB;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public HorrorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.DRAWING).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return HorrorRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(HorrorRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(HorrorRegistry.FITTING.get())?s.getValue(KIND):null;}
    public static boolean shelf(BlockState s){var k=kind(s);return k==Kind.SHELF||k==Kind.SHELF_INK||k==Kind.SHELF_NEEDLES||k==Kind.SHELF_OWN;}
    /** How many stages a kind draws; the rest draw its first. */
    public static int stages(Kind k){return k==Kind.BULB?4:1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.BULB&&s.getValue(STAGE)>0?9:0;}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case DRAWING,CRAYONS,FOOTNOTE->Block.box(2,0,2,14,1,14);
            case TRANSCRIPT->Block.box(3,0,3,13,10,13);
            case HANDPRINT,HOOF->Shapes.empty();
            case LOG->box(f,3,3,15,13,15,16);
            case TALLY->Block.box(1,0,1,15,.5,15);
            case TAG->box(f,5,2,15,11,14,16);
            case SCRATCH->box(f,1,3,15,15,13,16);
            case STUB->Block.box(5,0,5,11,3,11);
            case PACK->Block.box(2,0,4,13,11,12);
            case RECORDER->Block.box(4,0,5,12,2.5,11);
            case SHELF,SHELF_INK,SHELF_NEEDLES,SHELF_OWN,BOXES->Shapes.block();
            case BULB->Block.box(5,4,5,11,16,11);
            case LIST->box(f,3,4,15,13,14,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case TRANSCRIPT,SHELF,SHELF_INK,SHELF_NEEDLES,SHELF_OWN,BOXES,PACK->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
}
