package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.409, the city on the ice: one block of a carved panel in the wall of the ramp. Twelve panels, each four blocks wide and three high
 * (twelve parts, numbered across from the viewer's left and down from the top); FACING is the way the carving faces, into the pit. Drawn
 * by tools/draw_city_art.py, a 64-pixel picture to a panel, wordless.
 */
public final class CityMural extends HorizontalDirectionalBlock {
    public static final MapCodec<CityMural> CODEC=simpleCodec(CityMural::new);
    public static final int PANELS=12,WIDE=4,HIGH=3;
    public static final IntegerProperty PANEL=IntegerProperty.create("panel",0,PANELS-1);
    public static final IntegerProperty PART=IntegerProperty.create("part",0,WIDE*HIGH-1);
    public CityMural(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PANEL,0).setValue(PART,0));}
    public static BlockState of(int panel,int part,Direction facing){return CityRegistry.MURAL.get().defaultBlockState().setValue(PANEL,panel).setValue(PART,part).setValue(FACING,facing);}
    public static boolean is(BlockState s){return s.is(CityRegistry.MURAL.get());}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PANEL,PART);}
}
