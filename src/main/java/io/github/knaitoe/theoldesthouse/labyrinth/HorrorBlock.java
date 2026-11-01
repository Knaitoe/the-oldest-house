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
 */
public final class HorrorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HorrorBlock> CODEC=simpleCodec(HorrorBlock::new);
    public enum Kind implements StringRepresentable {
        DRAWING,TRANSCRIPT,CRAYONS,HANDPRINT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public HorrorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.DRAWING));}
    public static BlockState of(Kind kind,Direction facing){return HorrorRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(HorrorRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(HorrorRegistry.FITTING.get())?s.getValue(KIND):null;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static int light(BlockState s){return 0;}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
        double[] a=turn(f,x0,z0),b=turn(f,x1,z1);
        return Block.box(Math.min(a[0],b[0]),y0,Math.min(a[1],b[1]),Math.max(a[0],b[0]),y1,Math.max(a[1],b[1]));
    }
    private static double[] turn(Direction f,double x,double z){return switch(f){case SOUTH->new double[]{16-x,16-z};case EAST->new double[]{16-z,x};case WEST->new double[]{z,16-x};default->new double[]{x,z};};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case DRAWING->Block.box(2,0,2,14,1,14);
            case TRANSCRIPT->Block.box(3,0,3,13,10,13);
            case CRAYONS->Block.box(2,0,2,14,1,14);
            case HANDPRINT->Shapes.empty();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case TRANSCRIPT->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
}
