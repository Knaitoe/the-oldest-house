package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.337, the Sphinx's face in the first crossing (docs/SPHINX_FACE_0_4_337.md). The owner: "The first sphinx vignette also needs a fully
 * visible sphinx face." The block holds no shape of its own; it stands in the dark over the hall wall and its renderer draws her whole face
 * leaning out over the hall, from her chin at the top of the wall to the crown of her headcloth (see {@link Sphinx#faceAt}).
 * {@link #SHOWN} is whether she is looking: her face rises out of the dark and sinks back into it over a second and a half. {@link #LOOK}
 * is where the server has her eyes turned, a three-by-three of west/middle/east and under her/the middle/the back of the hall, toward
 * whoever is nearest; the renderer turns each iris smoothly toward that same reader.
 */
public final class SphinxFaceBlock extends Block implements EntityBlock {
    public static final MapCodec<SphinxFaceBlock> CODEC=simpleCodec(SphinxFaceBlock::new);
    public static final BooleanProperty SHOWN=BooleanProperty.create("shown");
    public static final IntegerProperty LOOK=IntegerProperty.create("look",0,8);
    /** Looking down at the middle of the hall. */
    public static final int AHEAD=4;
    public SphinxFaceBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(SHOWN,false).setValue(LOOK,AHEAD));}
    public static BlockState of(boolean shown,int look){return PuzzleRegistry.SPHINX_FACE.get().defaultBlockState().setValue(SHOWN,shown).setValue(LOOK,look);}
    public static boolean is(BlockState s){return s.is(PuzzleRegistry.SPHINX_FACE.get());}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(SHOWN,LOOK);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new Face(pos,s);}

    /**
     * Holds nothing the server needs. On the client it remembers how far her face has risen and where each iris is turned, so that both
     * move smoothly rather than jumping when the block's state changes.
     */
    public static final class Face extends BlockEntity {
        public Face(BlockPos pos,BlockState s){super(PuzzleRegistry.SPHINX_FACE_BE.get(),pos,s);}
        // Client-side only: the rise, and each eye's gaze across and up the face.
        public boolean seen,lastShown;public float changedAt;public final float[] gaze=new float[4];public float gazeAt=-1;
    }
}
