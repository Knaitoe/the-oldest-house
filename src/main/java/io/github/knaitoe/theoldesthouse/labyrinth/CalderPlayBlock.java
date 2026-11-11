package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.465, Bike night: the playground's equipment in Calder Park, each piece drawn facing north by tools/draw_calder_play_art.py.
 * This slice, the swings.
 * <ul>
 * <li>POST: a leg of a steel frame. Stage 0 its foot, with a stay each way the frame is pushed; 1 its shaft; 2 its top where a bar
 * ends on it (the bar goes off to the east of a post facing north); 3 its top where a bar runs on over it both ways.</li>
 * <li>BAR: the frame's top bar, across the way it faces. Stage 1 has the two hangers a swing's chains hang from.</li>
 * <li>CHAIN: a swing's two chains, one at each end of its seat, across the way it faces. Stage 1: gone, while somebody is on the
 * swing and the moving one is drawn in its place.</li>
 * <li>SWING: a belt seat on the ends of its chains. It faces as the porch's does: what it faces is behind whoever sits on it.
 * Stage 1: gone, while somebody is on it.</li>
 * </ul>
 * A frame stops a body; the chains and the seat do not.
 *
 * <p>0.4.466, the slide. Each of its pieces faces the way the slide goes down.
 * <ul>
 * <li>LADDER: the ladder up the back of it, its rungs against the deck. Stage 0 its lower block, 1 its upper. It is climbed.</li>
 * <li>DECK: the platform at the top, two blocks up. Stage 0 the deck and the legs it stands on; 1 the guard over it, a rail each
 * side, open to the ladder and to the chute.</li>
 * <li>CHUTE: the chute, four blocks of it, stage 0 at the top to 3 at the bottom, each falling SLOPE of a block along its length.
 * A body stands and walks on it. A reader who comes into its top block is taken down it (SlideRide).</li>
 * <li>RUNOUT: the level end of the chute, where a rider is let go.</li>
 * </ul>
 *
 * <p>0.4.467, the monkey bars.
 * <ul>
 * <li>RUNGS: a block of them, two rails and two rungs at the bottom of its block, running the way it faces. Stage 1: an end, its
 * rails running on over the ladder that stands beyond the way it faces. A body stands on them; a reader hangs under them and goes
 * along them hand over hand (BarsHang).</li>
 * <li>LADDER at stage 2 (its lower block) or 3 (its upper) is the bars' own, in their paint. It faces the rungs.</li>
 * </ul>
 */
public final class CalderPlayBlock extends HorizontalDirectionalBlock implements BikeSurface {
    public static final MapCodec<CalderPlayBlock> CODEC=simpleCodec(CalderPlayBlock::new);
    public enum Kind implements StringRepresentable {
        POST,BAR,CHAIN,SWING,LADDER,DECK,CHUTE,RUNOUT,RUNGS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** A post's pieces; a bar with hangers; a chain or a seat that is in use. */
    public static final int FOOT=0,SHAFT=1,END=2,THROUGH=3,HANGERS=1,GONE=1;
    /** The deck's guard; how far a chute falls in a block of its length (22.5 degrees); how high the deck is over the ground its ladder stands on; the chute's blocks. */
    public static final int GUARD=1,CHUTES=4;
    public static final double SLOPE=Math.tan(Math.toRadians(22.5)),DECK_HEIGHT=2;
    /** How high over its own block's floor a chute's surface is at the uphill edge of its block, in pixels; and which block of the chute stands a block lower than the deck's. */
    static double chuteTop(int stage){return 16*(DECK_HEIGHT-stage*SLOPE-(stage<2?1:0));}
    public static boolean chuteLow(int stage){return stage>=2;}
    /** The run-out's surface over its block's floor, in pixels. */
    static final double RUNOUT_TOP=16*(DECK_HEIGHT-CHUTES*SLOPE);
    /** The monkey bars: added to a ladder's stage for the bars' own paint; a block of rungs that is an end of them. */
    public static final int BARS=2,RUNG_END=1;
    /** How thick the rungs' block is, in pixels, and how high over its floor the middle of a rung is, in blocks. */
    public static final double RUNGS_THICK=1.2,RUNG=RUNGS_THICK/32;
    public CalderPlayBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.POST).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return BikeNightRegistry.PLAY.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BikeNightRegistry.PLAY.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case POST->stage==END?Shapes.or(Block.box(6.5,0,6.5,9.5,4,9.5),MarshBlock.turned(f,8,1,6.5,16,4,9.5))
                :stage==THROUGH?Shapes.or(Block.box(6.5,0,6.5,9.5,4,9.5),MarshBlock.turned(f,0,1,6.5,16,4,9.5))
                :Block.box(6.5,0,6.5,9.5,16,9.5);
            case BAR->MarshBlock.turned(f,0,1,6.5,16,4,9.5);
            case CHAIN->stage==GONE?Shapes.empty():Shapes.or(MarshBlock.turned(f,1,0,7,3,16,9),MarshBlock.turned(f,13,0,7,15,16,9));
            case SWING->stage==GONE?Shapes.empty():Shapes.or(MarshBlock.turned(f,1,6,5.5,15,9,10.5),MarshBlock.turned(f,1,9,7,3,16,9),MarshBlock.turned(f,13,9,7,15,16,9));
            case LADDER->MarshBlock.turned(f,2,0,0,14,16,2.5);
            case DECK->stage==GUARD?Shapes.or(MarshBlock.turned(f,0,0,0,1.5,16,16),MarshBlock.turned(f,14.5,0,0,16,16,16)):Block.box(0,13,0,16,16,16);
            // Two steps to a block, each at the height of the chute's surface over its middle: the uphill half (south, for a chute
            // going down to the north) and the downhill half.
            case CHUTE->{double top=chuteTop(stage),fall=16*SLOPE;yield Shapes.or(MarshBlock.turned(f,2,Math.max(0,top-fall*.25-3),8,14,top-fall*.25,16),MarshBlock.turned(f,2,Math.max(0,top-fall*.75-3),0,14,top-fall*.75,8));}
            case RUNOUT->MarshBlock.turned(f,2,RUNOUT_TOP-3,0,14,RUNOUT_TOP,16);
            case RUNGS->MarshBlock.turned(f,2,0,0,14,RUNGS_THICK,16);
        };
    }
    /** A ladder is climbed: the slide's, and the two the monkey bars stand on. */
    @Override public boolean isLadder(BlockState s,net.minecraft.world.level.LevelReader l,BlockPos p,net.minecraft.world.entity.LivingEntity e){return s.getValue(KIND)==Kind.LADDER;}
    /** A reader who comes into the top of the chute goes down it. */
    @Override protected void entityInside(BlockState s,net.minecraft.world.level.Level l,BlockPos pos,net.minecraft.world.entity.Entity e){
        if(!l.isClientSide&&s.getValue(KIND)==Kind.CHUTE&&s.getValue(STAGE)==0&&e instanceof net.minecraft.server.level.ServerPlayer p)SlideRide.begin(p,pos,s.getValue(FACING));
    }
    /** The chains and the seat give way to a body, as hanging things do. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case CHAIN,SWING->Shapes.empty();
            // 0.4.478: a bike rides the chute's own surface, not the steps a body stands on (BikeSurface).
            case CHUTE->c instanceof EntityCollisionContext e&&e.getEntity() instanceof RiddenBike?Shapes.empty():getShape(s,l,p,c);
            // And the deck's guards stand too high for a bike to mount. A bike rolls up anything a block high, and a rail is a block high.
            case DECK->s.getValue(STAGE)==GUARD&&c instanceof EntityCollisionContext e&&e.getEntity() instanceof RiddenBike
                ?Shapes.or(MarshBlock.turned(s.getValue(FACING),0,0,0,1.5,24,16),MarshBlock.turned(s.getValue(FACING),14.5,0,0,16,24,16)):getShape(s,l,p,c);
            default->getShape(s,l,p,c);
        };
    }
    /**
     * 0.4.478, the chute as a bike rides it: one sheet from the deck's own height to the run-out's, falling SLOPE of a block for each
     * block along the way it faces. The steps a body stands on are at this sheet's height over their middles.
     */
    @Override public double sheet(BlockState s,BlockPos at,double x,double z){
        if(s.getValue(KIND)!=Kind.CHUTE)return Double.NaN;
        double along=switch(s.getValue(FACING)){case SOUTH->z-at.getZ();case EAST->x-at.getX();case WEST->1-(x-at.getX());default->1-(z-at.getZ());};
        return (chuteTop(s.getValue(STAGE))-16*SLOPE*Math.max(0,Math.min(1,along)))/16;
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
