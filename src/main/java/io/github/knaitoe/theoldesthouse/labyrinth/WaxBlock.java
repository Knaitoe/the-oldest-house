package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.749, the waxworks (docs/WAXWORKS_0_4_749.md): the museum's things, drawn facing north by tools/draw_waxworks_art.py. A thing that
 * stands against a wall faces away from it.
 * <ul>
 * <li>PLINTH: the low painted block a figure stands on. PLAQUE: the brass plate on its short post in front of one. ROPE: a brass
 * stanchion with the velvet rope running from it toward the way it faces (0), or a stanchion at the end of a run (1).</li>
 * <li>CAMERA: a camera on its bracket high in a corner, looking the way it faces. MONITOR: one screen of the bank in the office, dark
 * (0) or showing its feed (1).</li>
 * <li>DESK: the guard's desk, plain (0), with the shift log open on it (1), with the telephone (2). CHAIR: the guard's chair, or a chair
 * at the kitchen table (its seat ten up). CLOCK: the office clock over the door, its hands at 11:52 (0), then at each hour from twelve to
 * six (1-7).</li>
 * <li>TIMECLOCK: the punch clock in the staff corridor, with its rack of cards. NOTICE: the typed card pinned beside it (the source).
 * LOCKER and LOCKER_TOP: the guard's locker, shut (0) or open (1).</li>
 * <li>FRONT: the museum's front doors, glass in bronze, locked: left lower and upper (0, 1), right lower and upper (2, 3). SIGN: the
 * painted board over them, left and right halves (0, 1). BOOTH and BOOTH_TOP: the ticket booth's counter and its glass.</li>
 * <li>TABLE: the kitchen tableau's supper table, a quarter of it by stage (0-3). DRESSER: its kitchen dresser, lower and upper (0, 1).
 * STOVE: its stove.</li>
 * <li>COFFIN and COFFIN_FOOT: the chamber's open coffin, head and foot. GRAVESTONE: a stone behind the rope.</li>
 * <li>VAT: a wax vat in the workshop, cold (0). BENCH: the modeller's bench, with tools (0), with a head on a stand (1), with hands
 * laid out (2). HEADS: a shelf of finished heads, lower and upper (0, 1).</li>
 * </ul>
 */
public final class WaxBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<WaxBlock> CODEC=simpleCodec(WaxBlock::new);
    public enum Kind implements StringRepresentable {
        PLINTH,PLAQUE,ROPE,CAMERA,MONITOR,DESK,CHAIR,CLOCK,TIMECLOCK,NOTICE,LOCKER,LOCKER_TOP,FRONT,SIGN,BOOTH,BOOTH_TOP,TABLE,DRESSER,STOVE,
        COFFIN,COFFIN_FOOT,GRAVESTONE,VAT,BENCH,HEADS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    /** How high a plinth's top is, in pixels: a figure stands this far above the floor. */
    public static final int PLINTH_TOP=6;
    public WaxBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PLINTH).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return WaxRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s){return s.is(WaxRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The museum is dark. The only light is a screen that is on, and the faint red lamp of a camera. */
    static int light(BlockState s){
        return switch(s.getValue(KIND)){case MONITOR->s.getValue(STAGE)==1?4:0;case CAMERA->1;default->0;};
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var kept=SHAPES_0767.get(s);if(kept==null){kept=shapeBuilt0767(s);SHAPES_0767.put(s,kept);}return kept;}
    // 0.4.767: the shape of each state, built once and kept; it was built again on every call (docs/REVIEW_REPAIRS_0_4_767.md).
    private static final java.util.Map<BlockState,VoxelShape> SHAPES_0767=new java.util.concurrent.ConcurrentHashMap<>();
    private VoxelShape shapeBuilt0767(BlockState s){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case PLINTH->Block.box(1,0,1,15,PLINTH_TOP,15);
            case PLAQUE->t(f,5,0,6,11,9,10);
            case ROPE->Block.box(6.5,0,6.5,9.5,15,9.5);
            case CAMERA->t(f,5,9,4,11,15,16);
            case MONITOR->t(f,1,0,4,15,13,16);
            case DESK->Block.box(0,0,0,16,13,16);
            case CHAIR->Shapes.or(t(f,2,0,2,14,10,14),t(f,2,10,12,14,16,14));
            case CLOCK->t(f,2,2,14,14,14,16);
            case TIMECLOCK->t(f,3,1,11,13,15,16);
            case NOTICE->t(f,3,3,15,13,13,16);
            case LOCKER,LOCKER_TOP->t(f,1,0,3,15,16,16);
            case FRONT->t(f,0,0,6,16,16,10);
            case SIGN->t(f,0,2,13,16,14,16);
            case BOOTH->Block.box(0,0,0,16,16,16);
            case BOOTH_TOP->t(f,0,0,6,16,16,10);
            case TABLE->Block.box(0,0,0,16,12,16);
            case DRESSER->t(f,0,0,4,16,16,16);
            case STOVE->t(f,1,0,2,15,15,16);
            case COFFIN,COFFIN_FOOT->Block.box(1,0,1,15,6,15);
            case GRAVESTONE->t(f,2,0,6,14,15,10);
            case VAT->Block.box(1,0,1,15,13,15);
            case BENCH->Block.box(0,0,0,16,14,16);
            case HEADS->t(f,0,0,6,16,16,16);
        };
    }
    /** Everything stands in the way but the camera, the clock, the notice and the sign, which are on walls above a hand; a rope run is a fence. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CAMERA,CLOCK,NOTICE,SIGN->Shapes.empty();
            case CHAIR->t(f,2,0,2,14,10,14);
            // A rope run stands in the way all along it, as high as a fence: a stanchion and the rope on to the next, or the last post.
            case ROPE->s.getValue(STAGE)==0?Block.box(6.5,0,0,9.5,24,16):Block.box(6.5,0,6.5,9.5,24,16);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
