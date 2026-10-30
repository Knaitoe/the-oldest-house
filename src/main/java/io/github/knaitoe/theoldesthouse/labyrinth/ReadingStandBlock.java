package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.164 (owner: "podiums like this need new textures suited to their vignettes"): the scenes whose paper stood as a
 * vanilla lectern on a table. Each is still a native lectern underneath, its block entity holding the scene's original
 * book, so every reading, archive and test of it is unchanged; it is drawn and shaped as what that scene would leave a
 * paper on. Its front faces {@link #FACING}, like a lectern's.
 */
public final class ReadingStandBlock extends LecternBlock {
    public static final MapCodec<ReadingStandBlock> CODEC=simpleCodec(ReadingStandBlock::new);
    public enum Kind implements StringRepresentable {
        /** Movie Night: Ray's printed program on a little easel, a stack of them in front. */
        PROGRAM,
        /** Costume Night: the judge's card on a clipboard stand, and the judge's pencil. */
        SCORECARD,
        /** Winter Lake: Bud's note, folded small, opened on the stump under a stone. */
        FOLDED_NOTE,
        /** Winchester: the construction ledger open on a mahogany book rest with brass corners. */
        LEDGER_REST,
        /** Ghosts: a black script stand; the call sheet lies on its desk where the lectern's top was. */
        SCRIPT_STAND,
        /** Borrasca: the county's green leaflet box at the trailhead. */
        TRAIL_RACK,
        /** Usher (0.4.181): the vault register open on a black oak slope desk bound in iron. */
        VAULT_REGISTER;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);

    public ReadingStandBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(KIND,Kind.PROGRAM));}
    @SuppressWarnings("unchecked") @Override public MapCodec<LecternBlock> codec(){return (MapCodec<LecternBlock>)(MapCodec<?>)CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(KIND);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.READING_STAND.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(HAS_BOOK,true);}
    public static boolean is(BlockState s){return s.getBlock() instanceof ReadingStandBlock;}

    /** Boxes in the model's own frame, front to the north, as in the models drawn by tools/draw_reading_stands.py. */
    private static double[][] boxes(Kind kind){return switch(kind){
        case PROGRAM->new double[][]{{2,0,2,14,1,9},{2,1,6,14,14,11}};
        case SCORECARD->new double[][]{{3,0,2,13,1,9},{3,1,6,13,14,11}};
        case FOLDED_NOTE->new double[][]{{3,0,3,13,1,13},{9,1,4,13,3,7}};
        case LEDGER_REST->new double[][]{{1,0,2,15,4,15},{1,4,2,15,9,15}};
        case SCRIPT_STAND->new double[][]{{4,0,4,12,13,12},{1,13,3,15,16,15}};
        case TRAIL_RACK->new double[][]{{3,0,4,13,10,11},{4,10,4,12,12,6}};
        case VAULT_REGISTER->new double[][]{{3,0,4,13,2,12},{5,2,6,11,11,10},{1,11,2,15,15,15}};
    };}
    private static VoxelShape turned(Direction d,double[] a){
        double x0=a[0],y0=a[1],z0=a[2],x1=a[3],y1=a[4],z1=a[5];
        return switch(d){
            case SOUTH->box(16-x1,y0,16-z1,16-x0,y1,16-z0);
            case EAST->box(16-z1,y0,x0,16-z0,y1,x1);
            case WEST->box(z0,y0,16-x1,z1,y1,16-x0);
            default->box(x0,y0,z0,x1,y1,z1);};
    }
    private static VoxelShape shape(BlockState s){
        VoxelShape out=Shapes.empty();for(var a:boxes(s.getValue(KIND)))out=Shapes.or(out,turned(s.getValue(FACING),a));return out;
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos at){return Shapes.empty();}
    @Override protected boolean useShapeForLightOcclusion(BlockState s){return false;}
}
