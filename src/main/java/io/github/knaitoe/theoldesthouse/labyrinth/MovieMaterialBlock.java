package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Movie Night's own boards and surfaces (0.4.102), drawn in code by tools/draw_movie_night_art.py. The grey dock boards
 * and the tarred raft are where she can stand; the warm cedar of the far pier is not, so the two read apart at a glance.
 */
public final class MovieMaterialBlock extends Block {
    public static final MapCodec<MovieMaterialBlock> CODEC=simpleCodec(MovieMaterialBlock::new);
    public enum Kind implements StringRepresentable {
        /** Sun-greyed dock boards (the dock and the barge's deck). */
        DOCK,
        /** Tarred black boards under the screen. */
        RAFT,
        /** Oiled cedar on the far pier and its boardwalk. */
        PIER,
        /** The screen's black canvas, seamed. */
        CANVAS,
        /** White-painted wood, peeling, round the screen. */
        FRAME,
        /** The ticket hut's painted clapboard. */
        SIDING;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public MovieMaterialBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.DOCK));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    public static BlockState of(Kind k){return MovieNightRegistry.MATERIAL.get().defaultBlockState().setValue(KIND,k);}
    public static boolean is(BlockState s,Kind k){return s.is(MovieNightRegistry.MATERIAL.get())&&s.getValue(KIND)==k;}
}
