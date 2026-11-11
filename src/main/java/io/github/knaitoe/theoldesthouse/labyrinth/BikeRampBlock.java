package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.463: a plank ramp: plywood on a timber frame. FACING is the way up. The long ramp is two blocks, LOW then HIGH, at the 22.5
 * degrees a model's piece can lie at (so it rises a little under seven pixels a block, to a lip a little over thirteen up); the kicker,
 * STEEP, is one block at 45.
 *
 * <p>A body on it stands on eight shallow steps under the sheet, each no more than two pixels, which it walks up without noticing. A
 * bike does not: the game climbs one step in a move, and a bike crosses several in a tick, so on the steps it would stall and throw its
 * rider. For a bike the steps are not there, and it rides the sheet itself, at exactly the sheet's slope ({@link #surface}, used by
 * RiddenBike). A bike that is still climbing when the ramp ends is thrown up by its own speed.
 */
public final class BikeRampBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BikeRampBlock> CODEC=simpleCodec(BikeRampBlock::new);
    /** The rise of the long ramp over one block, in pixels: sixteen times the tangent of 22.5 degrees. */
    public static final double LONG=16*Math.tan(Math.toRadians(22.5));
    public enum Part implements StringRepresentable {
        LOW(0,LONG),HIGH(LONG,2*LONG),STEEP(0,16);
        /** How high the sheet is at its low end and at its high end, in pixels. */
        public final double from,to;
        Part(double from,double to){this.from=from;this.to=to;}
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The slope: what it rises for each block along. */
        public double slope(){return (to-from)/16;}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    private static final Map<Part,Map<Direction,VoxelShape>> SHAPES=new EnumMap<>(Part.class);
    static{
        for(Part part:Part.values()){
            var by=new EnumMap<Direction,VoxelShape>(Direction.class);
            for(Direction f:Direction.Plane.HORIZONTAL){
                VoxelShape s=Shapes.empty();
                // Eight steps along the way up, each as high as the sheet is over its middle.
                for(int i=0;i<8;i++){double top=part.from+(part.to-part.from)*(i+.5)/8;if(top<.05)continue;s=Shapes.or(s,MarshBlock.turned(f,0,0,14-i*2,16,top,16-i*2));}
                by.put(f,s.optimize());
            }
            SHAPES.put(part,by);
        }
    }
    public BikeRampBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.LOW));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    public static BlockState of(Direction up,Part part){return BikeNightRegistry.RAMP.get().defaultBlockState().setValue(FACING,up).setValue(PART,part);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES.get(s.getValue(PART)).get(s.getValue(FACING));}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(c instanceof EntityCollisionContext e&&e.getEntity() instanceof RiddenBike)return Shapes.empty();
        return getShape(s,l,p,c);
    }
    /** How high the sheet is over the floor of the ramp at {@code at}, under a point in the world: in blocks, from its low end to its high end. */
    public static double surface(BlockState s,BlockPos at,double x,double z){
        Part part=s.getValue(PART);
        double along=switch(s.getValue(FACING)){case SOUTH->z-at.getZ();case EAST->x-at.getX();case WEST->1-(x-at.getX());default->1-(z-at.getZ());};
        return (part.from+(part.to-part.from)*Math.max(0,Math.min(1,along)))/16;
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
