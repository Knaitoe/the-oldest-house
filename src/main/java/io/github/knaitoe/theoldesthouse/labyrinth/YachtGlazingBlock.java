package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Survival mining shatters the glass after the native break event accepts it. The frame stays. */
public final class YachtGlazingBlock extends TransparentBlock {
    public static final MapCodec<YachtGlazingBlock> CODEC=simpleCodec(YachtGlazingBlock::new);
    public static final BooleanProperty BROKEN=BooleanProperty.create("broken");
    public YachtGlazingBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(BROKEN,false));}
    @Override public MapCodec<YachtGlazingBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(BROKEN);}
    public static boolean shattered(BlockState state){return state.getBlock() instanceof YachtGlazingBlock&&state.getValue(BROKEN);}
    /** Old saves can already contain mined-out portholes. They remain usable. */
    public static boolean opening(BlockState state){return state.isAir()||shattered(state);}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return shattered(state)?Shapes.empty():Shapes.block();}
    @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,Player player,boolean willHarvest,FluidState fluid){
        if(shattered(state))return false;
        playerWillDestroy(level,pos,state,player);
        return level.setBlock(pos,state.setValue(BROKEN,true),Block.UPDATE_ALL);
    }
}
