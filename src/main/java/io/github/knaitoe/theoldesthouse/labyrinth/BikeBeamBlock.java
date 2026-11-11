package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.463: the light the Lamp's dynamo throws ahead of the bike. A real moving light: the bike sets this block in the air in front of
 * itself as it goes and takes the one behind away (NightBike). There is nothing to see or touch, anything may be put where it is, and
 * it looks after itself: once a second it checks that a Lamp is still lighting it, and if none is (the bike was parked, went over, or
 * the server stopped with somebody riding), it goes out. So no light is ever left standing in the air.
 */
public final class BikeBeamBlock extends Block {
    public static final MapCodec<BikeBeamBlock> CODEC=simpleCodec(BikeBeamBlock::new);
    public static final IntegerProperty LEVEL=IntegerProperty.create("level",1,15);
    /** Ticks between its checks, and how near (in blocks) the Lamp lighting it must be. */
    static final int CHECK=20;static final double NEAR=5;
    public BikeBeamBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LEVEL,15));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LEVEL);}
    public static BlockState of(int level){return BikeNightRegistry.BEAM.get().defaultBlockState().setValue(LEVEL,Math.max(1,Math.min(15,level)));}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
    @Override protected float getShadeBrightness(BlockState s,BlockGetter l,BlockPos p){return 1F;}
    @Override protected void onPlace(BlockState s,Level l,BlockPos at,BlockState old,boolean moved){if(!old.is(this))l.scheduleTick(at,this,CHECK);}
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos at,RandomSource r){
        boolean lit=!l.getEntitiesOfClass(NightBike.class,new AABB(at).inflate(NEAR),b->at.equals(b.beam())).isEmpty();
        if(lit)l.scheduleTick(at,this,CHECK);else l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
}
