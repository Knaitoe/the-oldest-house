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
    public enum Kind implements StringRepresentable{WALLPAPER,CARPET,PANEL,CASKET;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final MapCodec<HarriganFinishBlock> CODEC=simpleCodec(HarriganFinishBlock::new);
    public HarriganFinishBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALLPAPER));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    public static BlockState of(Kind k){return LabyrinthRegistry.HARRIGAN_FINISH.get().defaultBlockState().setValue(KIND,k);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.CASKET?Block.box(-3,0,-16,19,12,32):Shapes.block();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.CASKET?Shapes.empty():Shapes.block();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND)==Kind.CASKET?0:15;}
}
