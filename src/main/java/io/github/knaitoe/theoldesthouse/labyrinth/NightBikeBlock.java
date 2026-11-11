package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.463: one of Bike night's six bikes while nobody is on it: on its kickstand, or lying on its side where it went over. FACING is
 * the way the bike points. A hand on it looks at it the first time, stands it up if it is lying, and otherwise gets on (NightBikes),
 * when the block becomes a NightBike.
 */
public final class NightBikeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<NightBikeBlock> CODEC=simpleCodec(NightBikeBlock::new);
    public static final EnumProperty<NightBike.Make> MAKE=EnumProperty.create("make",NightBike.Make.class);
    public static final BooleanProperty LYING=BooleanProperty.create("lying");
    public NightBikeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.EAST).setValue(MAKE,NightBike.Make.WASP).setValue(LYING,false));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,MAKE,LYING);}
    public static BlockState of(NightBike.Make make,Direction facing,boolean lying){
        return BikeNightRegistry.BIKE_BLOCK.get().defaultBlockState().setValue(MAKE,make).setValue(FACING,facing).setValue(LYING,lying);
    }
    /** A box given for a bike pointing east, turned to the way this one points. */
    static VoxelShape turned(Direction facing,double x0,double y0,double z0,double x1,double y1,double z1){
        return switch(facing){
            case SOUTH->Block.box(16-z1,y0,x0,16-z0,y1,x1);
            case WEST->Block.box(16-x1,y0,16-z1,16-x0,y1,16-z0);
            case NORTH->Block.box(z0,y0,16-x1,z1,y1,16-x0);
            default->Block.box(x0,y0,z0,x1,y1,z1);
        };
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(LYING)?turned(s.getValue(FACING),0,0,2,16,6,16):turned(s.getValue(FACING),0,0,6,16,14,10);
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){
        if(l instanceof ServerLevel level&&p instanceof ServerPlayer reader)NightBikes.use(reader,level,at,s);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
