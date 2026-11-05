package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.309, the Sphinx's eye (docs/SPHINX_0_4_306.md): one of the eight blocks of her eye, lying face down over the top of a hall wall and
 * looking down, drawn by tools/draw_sphinx_fittings.py. {@link #TILE} is which eighth of the picture (four across, two deep);
 * {@link #LOOK} is where her pupil is turned, a three-by-three of left/middle/right and away/under/toward the hall, so that wherever a
 * reader stands she is looking at them. Faintly lit; nothing else of her is.
 */
public final class SphinxEyeBlock extends Block {
    public static final MapCodec<SphinxEyeBlock> CODEC=simpleCodec(SphinxEyeBlock::new);
    public static final IntegerProperty TILE=IntegerProperty.create("tile",0,7),LOOK=IntegerProperty.create("look",0,8);
    /** Looking straight down at the middle of the hall below her. */
    public static final int AHEAD=7;
    public SphinxEyeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(TILE,0).setValue(LOOK,AHEAD));}
    public static BlockState of(int tile,int look){return PuzzleRegistry.SPHINX_EYE.get().defaultBlockState().setValue(TILE,tile).setValue(LOOK,look);}
    public static boolean is(BlockState s){return s.is(PuzzleRegistry.SPHINX_EYE.get());}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(TILE,LOOK);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(0,0,0,16,1,16);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        PuzzleRooms.say(p,"High in the dark, an eye wider than a door looks down at you. Its pupil moves when you do.");
        return InteractionResult.CONSUME;
    }
}
