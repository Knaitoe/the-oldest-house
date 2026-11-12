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
 * 0.4.489, Bike night: what stands in Calder Hill Cemetery, each a kind of this one block (tools/draw_calder_graves_art.py), as the
 * town's fittings are kinds of {@link CalderTownBlock}. What a stone faces is the way its inscription looks.
 * <ul>
 * <li>STONE: a headstone. Its stage is which: four shapes of four each, and the last four are low markers.</li>
 * <li>SMALL: one of the row of small stones near the back. Its stage is which of the row. (0.4.510: and stage NEW_STONE is a
 * new one, at the row's end: BikeNightStones.)</li>
 * <li>CROSS: of stone (0), or of wood and leaning (1).</li>
 * <li>OBELISK, ANGEL: three blocks each; the stage counts up from the base.</li>
 * <li>MOUND: the heap of earth beside an open grave. FACING is the way up it.</li>
 * <li>TARP: a tarpaulin pegged out flat (0), or thrown over a heap (1).</li>
 * <li>GOODS: what is left at a grave: flowers, a lantern (lit), a wreath, a flag.</li>
 * <li>DOOR: a quarter of the mausoleum's iron doors, flat against the back of its block.</li>
 * <li>ROOTS (0.4.493): what hangs under a piece of ground that has come away. In nobody's way.</li>
 * <li>PICKET (0.4.493): a length of old board fence on the block's middle line, across the way it faces: upright (0), leaning to
 * either side (1, 2), broken off short (3). It is a fence where it stands, whatever it leans to; broken, it is a bump.</li>
 * </ul>
 *
 * <p>The proposal: "A stone is a wall: a glance off it is fine, a head-on hit puts the rider over the bars." A bike takes a step a
 * block high in its stride, and no headstone is a block high; so to a bike a standing stone stands higher than it looks, as a fence
 * does to everybody: a block and a half, which no hop clears but the Hopper's held to its fullest. A low marker is half a block to a
 * bike as to anybody: it is hopped, or it is a bump ("Low stones can be hopped"). To anybody on foot a stone is as high as it is,
 * and can be climbed. A stone that leans is in the way where it stands, not where it leans to.
 *
 * <p>The mound is ridden as one slope ({@link BikeSurface}), as a plank ramp is, and throws a bike that leaves its top still climbing.
 */
public final class CalderGraveBlock extends HorizontalDirectionalBlock implements BikeSurface {
    public static final MapCodec<CalderGraveBlock> CODEC=simpleCodec(CalderGraveBlock::new);
    public enum Kind implements StringRepresentable {
        STONE,SMALL,CROSS,OBELISK,ANGEL,MOUND,TARP,GOODS,DOOR,ROOTS,PICKET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,15);
    /** How many headstones there are, the first of the low markers among them, and how many small stones the row has. */
    public static final int STONES=16,LOW=12,SMALL_STONES=6;
    /** 0.4.510: the small stone that is new. */
    public static final int NEW_STONE=SMALL_STONES;
    /** What is left at a grave. */
    public static final int FLOWERS=0,LANTERN=1,WREATH=2,FLAG=3;
    /** A tarpaulin over a heap. */
    public static final int HEAPED=1;
    /** A picket broken off short. */
    public static final int BROKEN=3;
    /** How high a standing stone is to a bike, in pixels; and the mound's sheet at its low end and at its top. */
    public static final double STANDING=24,MOUND_FROM=2,MOUND_TO=12;
    public CalderGraveBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.STONE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return BikeNightRegistry.GRAVE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(15,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BikeNightRegistry.GRAVE.get())&&s.getValue(KIND)==kind;}
    /** Whether this is something with words cut on it. */
    public static boolean carved(BlockState s){return s.is(BikeNightRegistry.GRAVE.get())&&switch(s.getValue(KIND)){case STONE,SMALL,CROSS,OBELISK,ANGEL,DOOR->true;default->false;};}
    /** Whether a headstone is one of the low markers. */
    public static boolean low(BlockState s){return is(s,Kind.SMALL)||is(s,Kind.STONE)&&s.getValue(STAGE)>=LOW;}
    /** The light a thing gives: a lantern's candle. */
    static int light(BlockState s){return s.getValue(KIND)==Kind.GOODS&&s.getValue(STAGE)==LANTERN?9:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    /** A thing as it stands, {@code high} pixels of it (nothing, for what it is). */
    private static VoxelShape stands(BlockState s,double high){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case STONE->stage>=LOW?MarshBlock.turned(f,.5,0,5,15.5,high>0?high:8,11):stage/4==1?MarshBlock.turned(f,2,0,6,14,high>0?high:15,10):MarshBlock.turned(f,2,0,6.5,14,high>0?high:stage/4==0?13:14,9.5);
            case SMALL->MarshBlock.turned(f,.5,0,5.5,15.5,high>0?high:8,10.5);
            case CROSS->MarshBlock.turned(f,3,0,6.5,13,high>0?high:15,9.5);
            case OBELISK->stage==0?Block.box(3,0,3,13,16,13):stage==1?Block.box(5,0,5,11,16,11):Block.box(5.5,0,5.5,10.5,14,10.5);
            case ANGEL->stage==0?Block.box(1.5,0,1.5,14.5,16,14.5):stage==1?MarshBlock.turned(f,4,0,5,12,16,11):MarshBlock.turned(f,1,0,5.5,15,9,11.6);
            // Eight lumps along the way up, each as high as the sheet is over its middle (the model's are the same).
            case MOUND->{VoxelShape steps=Shapes.empty();for(int i=0;i<8;i++)steps=Shapes.or(steps,MarshBlock.turned(f,0,0,14-2*i,16,MOUND_FROM+(MOUND_TO-MOUND_FROM)*(i+.5)/8,16-2*i));yield steps.optimize();}
            case TARP->stage==HEAPED?Block.box(.5,0,.5,15.5,6,15.5):Block.box(0,0,0,16,1,16);
            case GOODS->Block.box(4,0,4,12,11,12);
            case DOOR->MarshBlock.turned(f,0,0,14.5,16,16,16);
            case ROOTS->Block.box(2,2,2,14,16,14);
            case PICKET->MarshBlock.turned(f,0,0,7,16,high>0?high:stage==BROKEN?8:16,9);
        };
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return stands(s,0);}
    /**
     * To a bike a standing stone is higher than it looks, and the mound is not steps but a slope it is kept on (so nothing of it is in its way).
     * A pegged tarpaulin and what is left at a grave are in nobody's way.
     */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        boolean bike=c instanceof EntityCollisionContext e&&e.getEntity() instanceof RiddenBike;
        return switch(s.getValue(KIND)){
            case STONE,SMALL->bike&&!low(s)?stands(s,STANDING):stands(s,0);
            case CROSS->bike?stands(s,STANDING):stands(s,0);
            case MOUND->bike?Shapes.empty():stands(s,0);
            case GOODS,ROOTS->Shapes.empty();
            // A standing picket is a fence: half a block higher than it looks, to everybody. Broken off short, it is what it is.
            case PICKET->s.getValue(STAGE)==BROKEN?stands(s,0):stands(s,STANDING);
            case TARP->s.getValue(STAGE)==HEAPED?stands(s,0):Shapes.empty();
            default->stands(s,0);
        };
    }
    /** The mound's sheet: from its low end to its top, along the way it faces. Nothing else here has one. */
    @Override public double sheet(BlockState s,BlockPos at,double x,double z){
        if(s.getValue(KIND)!=Kind.MOUND)return Double.NaN;
        double along=switch(s.getValue(FACING)){case SOUTH->z-at.getZ();case EAST->x-at.getX();case WEST->1-(x-at.getX());default->1-(z-at.getZ());};
        return (MOUND_FROM+(MOUND_TO-MOUND_FROM)*Math.max(0,Math.min(1,along)))/16;
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
