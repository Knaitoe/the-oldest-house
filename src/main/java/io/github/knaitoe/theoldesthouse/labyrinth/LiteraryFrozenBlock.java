package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
/** A captured mechanism retains its native mesh and shape without ticking or emitting power. */
public final class LiteraryFrozenBlock extends BaseEntityBlock {
    public static final MapCodec<LiteraryFrozenBlock> CODEC=simpleCodec(LiteraryFrozenBlock::new);
    public LiteraryFrozenBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new LiteraryModelBlockEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    public static BlockState original(BlockGetter l,BlockPos p){if(l.getBlockEntity(p) instanceof LiteraryModelBlockEntity e&&e.getLevel()!=null&&e.display().contains("FrozenState"))return NbtUtils.readBlockState(e.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK),e.display().getCompound("FrozenState"));return Blocks.AIR.defaultBlockState();}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return original(l,p).getShape(l,p,c);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return original(l,p).getCollisionShape(l,p,c);}
    public static boolean freezes(BlockState s){return s.getBlock() instanceof net.minecraft.world.level.block.piston.PistonBaseBlock||s.is(Blocks.OBSERVER)||s.is(Blocks.REDSTONE_BLOCK)||s.getBlock() instanceof RedStoneWireBlock||s.getBlock() instanceof DiodeBlock||s.getBlock() instanceof CropBlock||s.getBlock() instanceof StemBlock||s.getBlock() instanceof AttachedStemBlock||s.getBlock() instanceof FarmlandBlock||s.getBlock() instanceof SugarCaneBlock||s.getBlock() instanceof CactusBlock;}
}
