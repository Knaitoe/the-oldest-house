package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * Movie Night's fixtures (0.4.102): a horn speaker on its bracket, the projector at the dock's end, a firework mortar, and
 * the audience's things: lawn chairs (one knocked over), a plaid blanket, a cooler, a popcorn box (one spilled), the film's
 * poster on its board, a life ring, the projectionist's film cans and a mooring buoy. Each model faces north; FACING turns it.
 */
public final class MoviePropBlock extends Block {
    public static final MapCodec<MoviePropBlock> CODEC=simpleCodec(MoviePropBlock::new);
    public enum Kind implements StringRepresentable {
        SPEAKER,PROJECTOR,MORTAR,LAWN_CHAIR,LAWN_CHAIR_TIPPED,BLANKET,COOLER,POPCORN,POPCORN_SPILLED,POSTER,POSTER_TOP,LIFE_RING,FILM_CANS,BUOY;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public MoviePropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SPEAKER).setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction d){return MovieNightRegistry.PROP.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static boolean is(BlockState s,Kind k){return s.is(MovieNightRegistry.PROP.get())&&s.getValue(KIND)==k;}
    /** A box given as it sits facing north, turned to the block's facing. */
    private static VoxelShape turned(Direction d,double x0,double y0,double z0,double x1,double y1,double z1){
        return switch(d){case SOUTH->box(16-x1,y0,16-z1,16-x0,y1,16-z0);case EAST->box(16-z1,y0,x0,16-z0,y1,x1);case WEST->box(z0,y0,16-x1,z1,y1,16-x0);default->box(x0,y0,z0,x1,y1,z1);};
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var d=s.getValue(FACING);
        return switch(s.getValue(KIND)){case SPEAKER->box(3,3,3,13,13,13);case PROJECTOR->box(3,0,0,13,9,13);case MORTAR->box(2,0,2,14,10,14);
            case LAWN_CHAIR->box(2,0,2,14,9,14);case LAWN_CHAIR_TIPPED->box(1,0,1,15,6,15);case BLANKET->box(0,0,0,16,1,16);case COOLER->turned(d,3,0,4,13,10,12);
            case POPCORN->box(5,0,5,11,7,11);case POPCORN_SPILLED->box(3,0,3,13,4,13);case POSTER,POSTER_TOP->turned(d,1,0,6,15,16,10);case LIFE_RING->turned(d,2,2,15,14,14,16);
            case FILM_CANS->box(3,0,3,13,6,13);case BUOY->box(4,0,4,12,13,12);};
    }
    /** The blanket, the popcorn, the ring and the low tipped chair are underfoot or on a wall; nothing to bump into. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return switch(s.getValue(KIND)){case BLANKET,POPCORN,POPCORN_SPILLED,LIFE_RING->Shapes.empty();case LAWN_CHAIR->box(2,0,2,14,5,14);default->getShape(s,l,at,c);};
    }
}
