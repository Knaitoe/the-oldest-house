package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.471, Bike night: the playground's climbing dome, drawn by tools/draw_calder_play_art.py. "Climbing dome. Climbable on every
 * bar, with a view over the park." A dome of steel bars in triangles, three blocks across and a child and a half high: eight feet
 * in the sand, two rings of eight joints over them, and one joint at the top.
 *
 * <p>Bars at every angle are not blocks. The dome's ground is: five blocks square and two high, each a CELL that knows where in
 * the dome it is. The middle cell on the sand keeps a block entity, whose renderer draws every bar and joint (DomeRenderer) from
 * models that are never blocks in the world (the three paints of bar, and the joint).
 * <ul>
 * <li>Climbed: a body near the dome's skin, inside it or out, is on a ladder there ({@link #near}). Walking into it goes up it,
 * as a ladder does.</li>
 * <li>Stood on: from its first ring up, the skin stops a body ({@link #SOLID}), so a reader who has climbed past that ring walks
 * on up over the top, and stands there.</li>
 * <li>Got into: under the first ring the bars are far enough apart for a child keeping low to get between them, and nothing
 * stops a body there. Inside there is room to stand.</li>
 * </ul>
 */
public final class DomeBlock extends Block implements EntityBlock {
    public static final MapCodec<DomeBlock> CODEC=simpleCodec(DomeBlock::new);
    public enum Part implements StringRepresentable {
        CELL,RED,YELLOW,BLUE,JOINT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    /** Where a cell is in the dome's ground: 0 to 4 from the west and from the north, 0 or 1 up. The middle is (2,0,2). */
    public static final int WIDE=5,HIGH=2,MID=2;
    public static final IntegerProperty X=IntegerProperty.create("x",0,WIDE-1),Z=IntegerProperty.create("z",0,WIDE-1),Y=IntegerProperty.create("y",0,HIGH-1);
    /**
     * The dome is a cap of a ball. From the middle of its middle cell, on the sand: how far out its feet stand, how high its top
     * is, the ball's radius and how high its middle is; how high its two rings are; and how thick a bar is, in blocks.
     */
    public static final double FOOT=1.4,TOP=1.6,BALL=(FOOT*FOOT+TOP*TOP)/(2*TOP),HEART=TOP-BALL,RING_1=.9,RING_2=1.38,BAR=1.2/16;
    /**
     * How far inside the skin and how far outside it a body is still on it; from how high the skin stops a body; how thick it is
     * there; and how thick it is to a hand, which finds it from the sand up.
     */
    public static final double IN=.5,OUT=.45,SOLID=RING_1,SKIN=.3,REACH=.45;
    /** How near the top a reader standing on it is at the top: how high their feet are, and how far from the middle. */
    public static final double CROWN=TOP-.2,ACROSS=.6;
    /** The joints, from the middle of the middle cell on the sand: eight feet, eight on each ring (the first ring's between the feet), and the top. */
    public static final List<Vec3> JOINTS;
    /** The bars: the two joints each runs between, and its paint (0 red, 1 yellow, 2 blue). */
    public static final List<int[]> BARS;
    static{
        var joints=new ArrayList<Vec3>();var bars=new ArrayList<int[]>();
        for(int k=0;k<8;k++)joints.add(ring(0,FOOT,k*Math.PI/4));
        for(int k=0;k<8;k++)joints.add(ring(RING_1,across(RING_1),(k+.5)*Math.PI/4));
        for(int k=0;k<8;k++)joints.add(ring(RING_2,across(RING_2),k*Math.PI/4));
        joints.add(new Vec3(0,TOP,0));
        for(int k=0;k<8;k++){
            int next=(k+1)%8;
            bars.add(new int[]{k,8+k,0});bars.add(new int[]{next,8+k,0});             // a foot up to the first ring, either side
            bars.add(new int[]{8+k,8+next,1});                                         // round the first ring
            bars.add(new int[]{8+k,16+k,1});bars.add(new int[]{8+k,16+next,1});        // the first ring up to the second
            bars.add(new int[]{16+k,16+next,2});                                       // round the second ring
            bars.add(new int[]{16+k,24,2});                                            // the second ring up to the top
        }
        JOINTS=List.copyOf(joints);BARS=List.copyOf(bars);
    }
    private static Vec3 ring(double high,double out,double round){return new Vec3(out*Math.cos(round),high,out*Math.sin(round));}
    /** How far out the skin is at a height. */
    public static double across(double high){double up=high-HEART;return Math.sqrt(Math.max(0,BALL*BALL-up*up));}
    /** How high the skin is, a distance out from the middle; under the sand's level past its feet. */
    public static double over(double out){return out>=BALL?-1:HEART+Math.sqrt(BALL*BALL-out*out);}
    private static final Map<Integer,VoxelShape> SOLIDS=new HashMap<>(),REACHED=new HashMap<>();

    public DomeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PART,Part.CELL).setValue(X,MID).setValue(Y,0).setValue(Z,MID));}
    public static BlockState of(Part part){return BikeNightRegistry.DOME.get().defaultBlockState().setValue(PART,part);}
    public static BlockState cell(int x,int y,int z){return of(Part.CELL).setValue(X,x).setValue(Y,y).setValue(Z,z);}
    public static boolean is(BlockState s){return s.is(BikeNightRegistry.DOME.get())&&s.getValue(PART)==Part.CELL;}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,X,Y,Z);}

    /** The dome's middle cell, from any cell of it. */
    public static BlockPos middle(BlockPos at,BlockState s){return at.offset(MID-s.getValue(X),-s.getValue(Y),MID-s.getValue(Z));}
    /** The middle of the dome on the sand, from its middle cell. */
    public static Vec3 heart(BlockPos middle){return new Vec3(middle.getX()+.5,middle.getY(),middle.getZ()+.5);}
    /** Whether a body is on the dome's skin, inside it or out: its middle is near enough the ball's surface, over the sand. */
    public static boolean near(BlockPos middle,Entity e){
        var h=heart(middle);double x=e.getX()-h.x,y=e.getY()+e.getBbHeight()/2-h.y-HEART,z=e.getZ()-h.z,far=Math.sqrt(x*x+y*y+z*z);
        return far>=BALL-IN&&far<=BALL+OUT&&e.getY()>=h.y-.05;
    }
    /** Whether a body is standing on the top of the dome: its feet on the skin, near enough the middle. */
    public static boolean onTop(BlockPos middle,Entity e){
        var h=heart(middle);double x=e.getX()-h.x,z=e.getZ()-h.z,out=Math.sqrt(x*x+z*z);
        return !e.isPassenger()&&out<=ACROSS&&e.getY()>=h.y+CROWN&&Math.abs(e.getY()-h.y-over(out))<.25;
    }
    /**
     * The skin within a cell, in two-pixel squares: for each square of the cell's floor, one box from the skin's underside
     * (`thick` under it) up to the top of the bars, where the skin is over that square and at least `from` up.
     */
    private static VoxelShape skin(int cx,int cy,int cz,double from,double thick){
        VoxelShape out=Shapes.empty();
        for(int z=0;z<16;z+=2)for(int x=0;x<16;x+=2){
            double px=cx-MID+(x+1)/16.-.5,pz=cz-MID+(z+1)/16.-.5,top=over(Math.sqrt(px*px+pz*pz));
            if(top<from)continue;
            // In pixels of this cell, to the pixel: its top the top of a bar lying on the skin, its underside the skin's thickness under that.
            double hi=Math.round((top+BAR/2-cy)*16),lo=Math.max(Math.floor((Math.max(from,top-thick)-cy)*16),0);
            hi=Math.min(hi,16);if(hi<=lo)continue;
            out=Shapes.or(out,Block.box(x,lo,z,x+2,hi,z+2));
        }
        return out.optimize();
    }
    private static int key(BlockState s){return s.getValue(X)+WIDE*(s.getValue(Z)+WIDE*s.getValue(Y));}
    /** What a hand finds: the dome's skin, from the sand up. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(PART)!=Part.CELL?Shapes.empty():REACHED.computeIfAbsent(key(s),k->skin(s.getValue(X),s.getValue(Y),s.getValue(Z),0,REACH));
    }
    /** What stops a body: the skin from the first ring up. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(PART)!=Part.CELL?Shapes.empty():SOLIDS.computeIfAbsent(key(s),k->skin(s.getValue(X),s.getValue(Y),s.getValue(Z),SOLID,SKIN));
    }
    /** Every bar is a rung: a body on the skin is on a ladder. */
    @Override public boolean isLadder(BlockState s,LevelReader l,BlockPos p,LivingEntity e){return s.getValue(PART)==Part.CELL&&near(middle(p,s),e);}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
    @Override protected float getShadeBrightness(BlockState s,BlockGetter l,BlockPos p){return 1;}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos at,BlockState s){
        return s.getValue(PART)==Part.CELL&&s.getValue(X)==MID&&s.getValue(Y)==0&&s.getValue(Z)==MID?new Bars(at,s):null;
    }

    /** The middle cell's: it is there to be drawn. It keeps nothing. */
    public static final class Bars extends BlockEntity {
        public Bars(BlockPos at,BlockState s){super(BikeNightRegistry.DOME_BARS.get(),at,s);}
    }
}
