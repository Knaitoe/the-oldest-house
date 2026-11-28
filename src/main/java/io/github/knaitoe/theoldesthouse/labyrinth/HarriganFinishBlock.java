package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;
public final class HarriganFinishBlock extends Block {
    public enum Kind implements StringRepresentable{WALLPAPER,CARPET,PANEL,CASKET,PLAQUE;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final MapCodec<HarriganFinishBlock> CODEC=simpleCodec(HarriganFinishBlock::new);
    public HarriganFinishBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALLPAPER));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    public static BlockState of(Kind k){return LabyrinthRegistry.HARRIGAN_FINISH.get().defaultBlockState().setValue(KIND,k);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var kept=SHAPES_0767.get(s);if(kept==null){kept=shapeBuilt0767(s);SHAPES_0767.put(s,kept);}return kept;}
    // 0.4.767: the shape of each state, built once and kept; it was built again on every call (docs/REVIEW_REPAIRS_0_4_767.md).
    private static final java.util.Map<BlockState,VoxelShape> SHAPES_0767=new java.util.concurrent.ConcurrentHashMap<>();
    private VoxelShape shapeBuilt0767(BlockState s){return switch(s.getValue(KIND)){case CASKET->Block.box(-2,0,-12,18,12,28);case PLAQUE->Shapes.or(Block.box(0,0,4,16,2,12),Block.box(3,2,7,5,11,9),Block.box(11,2,7,13,11,9),Block.box(1,10,6.4,15,28,9.1));default->Shapes.block();};}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.CASKET||s.getValue(KIND)==Kind.PLAQUE?Shapes.empty():Shapes.block();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.CASKET||s.getValue(KIND)==Kind.PLAQUE?0:15;}
}
