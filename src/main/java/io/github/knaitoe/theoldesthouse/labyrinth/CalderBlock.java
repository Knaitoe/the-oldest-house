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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.464, Bike night: Calder Falls' own things, each drawn facing north by tools/draw_calder_art.py. This slice, the park's.
 * <ul>
 * <li>FENCE: iron railings on the block's middle line, across the way it faces. Stage 0 the lower block, 1 the upper with its spear
 * tops. Two high, and nothing gets over it: a body is stopped half a block above the spears, so the best hop there is falls short.</li>
 * <li>GATE: one block of a gate's leaf, its heavy stile at one edge (turn it about for the other). Stage 0 the lower block, 1 the
 * upper, 2 the lower with the chain round it, 3 the same with the padlock on it. Shut, and as tall as the railings.</li>
 * <li>LAMP: a lamp post in pieces. Stage 0 its foot, 1 its post, 2 its lantern lit, 3 its lantern dark.</li>
 * <li>BENCH: a slatted bench on iron ends, looking the way it faces. Stage 0 one block long; 1 and 2 the two ends of a longer one,
 * the end on the sitter's left and the end on their right.</li>
 * <li>BIN: a wire litter bin.</li>
 * <li>BORDER: the sandpit's timber edging along the edge it faces; stage 1 a corner, with another along the edge to its left.</li>
 * <li>NOTE: a sheet of lined paper taped to what is behind it.</li>
 * <li>CAP: the top of a brick pier: its last courses, and a stone slab that oversails them.</li>
 * </ul>
 */
public final class CalderBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CalderBlock> CODEC=simpleCodec(CalderBlock::new);
    public enum Kind implements StringRepresentable {
        FENCE,GATE,LAMP,BENCH,BIN,BORDER,NOTE,CAP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** A lamp's pieces. */
    public static final int FOOT=0,POST=1,LIT=2,DARK=3;
    public CalderBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.FENCE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return BikeNightRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BikeNightRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a lit lantern. */
    static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)==LIT?14:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case FENCE,GATE->MarshBlock.turned(f,0,0,7,16,16,9);
            case LAMP->stage==FOOT?Block.box(4.5,0,4.5,11.5,16,11.5):stage==POST?Block.box(6.5,0,6.5,9.5,16,9.5):Block.box(3.5,0,3.5,12.5,15.5,12.5);
            case BENCH->Shapes.or(MarshBlock.turned(f,0,0,4,16,8,12.5),MarshBlock.turned(f,0,8,11.3,16,15,12.5));
            case BIN->Block.box(3.5,0,3.5,12.5,12,12.5);
            case BORDER->stage==1?Shapes.or(MarshBlock.turned(f,0,0,0,16,3,3),MarshBlock.turned(f,0,0,3,3,3,16)):MarshBlock.turned(f,0,0,0,16,3,3);
            case NOTE->MarshBlock.turned(f,4,3.5,15,12,13.5,16);
            case CAP->Shapes.block();
        };
    }
    /** The railings and the gate stand half a block higher than they look; the paper is reached through. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case FENCE,GATE->MarshBlock.turned(s.getValue(FACING),0,0,7,16,s.getValue(KIND)==Kind.FENCE&&s.getValue(STAGE)==0||s.getValue(KIND)==Kind.GATE&&s.getValue(STAGE)!=1?16:24,9);
            case NOTE->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
