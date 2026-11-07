package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.343, loose fill held aside by a piece of the Dream Stela (docs/STELA_PASSAGE_0_4_343.md). Drawn in the fill's own texture, sand,
 * red sand, gravel or the dream's drifted sand, along the passage's {@link #AXIS}. {@link #PHASE} 0 is the narrow crack a dismissive
 * stone opens first, solid to a body but clear to the eye down its length; 1 is the passage open, only a skin of loose fill on its floor;
 * 2 is the passage shedding grit before it closes. Being neither air nor replaceable, it holds up the loose fill above it, which would
 * otherwise come down into the passage. Nobody can break it or push it, and it drops nothing: {@link StelaPassage} puts the exact fill
 * back when it settles.
 */
public final class StelaFillBlock extends Block {
    public static final MapCodec<StelaFillBlock> CODEC=simpleCodec(StelaFillBlock::new);
    public enum Fill implements StringRepresentable {
        SAND,RED_SAND,GRAVEL,DRIFT;
        @Override public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public static Fill of(BlockState s){return s.is(Blocks.RED_SAND)?RED_SAND:s.is(Blocks.GRAVEL)?GRAVEL:SphinxBlock.is(s,SphinxBlock.Kind.DRIFT)?DRIFT:SAND;}
    }
    public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty PHASE=IntegerProperty.create("phase",0,2);
    public static final EnumProperty<Fill> FILL=EnumProperty.create("fill",Fill.class);
    public static final int CRACK=0,OPEN=1,SHEDDING=2;
    private static final VoxelShape SKIN=Block.box(0,0,0,16,1,16);

    public StelaFillBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(AXIS,Direction.Axis.Z).setValue(PHASE,OPEN).setValue(FILL,Fill.SAND));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AXIS,PHASE,FILL);}
    public static BlockState of(BlockState fill,Direction along,int phase){
        return PuzzleRegistry.STELA_FILL.get().defaultBlockState().setValue(AXIS,along.getAxis()).setValue(PHASE,phase).setValue(FILL,Fill.of(fill));
    }
    public static boolean is(BlockState s){return s.getBlock() instanceof StelaFillBlock;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PHASE)==CRACK?Shapes.block():SKIN;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PHASE)==CRACK?Shapes.block():Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
