package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 0.4.449 (owner, of the corridor under the depot: "Custom signs, wall texture"): block wall painted as an institution paints
 * it, on the one side that {@link #FACING} looks; its other sides are bare concrete, as the rooms behind the wall are.
 * {@link #COURSE} is which of a wall's three courses a block is: 0 the foot, green with a scuffed band along the floor; 1 green to a
 * line and cream above it; 2 cream, stained where water has come down from the ceiling (tools/draw_borrasca_town_art.py).
 */
public final class BorrascaWardBlock extends Block {
    public static final IntegerProperty COURSE=IntegerProperty.create("course",0,2);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public BorrascaWardBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(COURSE,0).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(COURSE,FACING);}
    public static boolean is(BlockState s){return s.is(BorrascaRegistry.WARD.get());}
    /** A block of the wall's {@code course} (0 its foot), painted on the side that looks {@code painted}. */
    public static BlockState course(int course,Direction painted){return BorrascaRegistry.WARD.get().defaultBlockState().setValue(COURSE,Math.max(0,Math.min(2,course))).setValue(FACING,painted);}
}
