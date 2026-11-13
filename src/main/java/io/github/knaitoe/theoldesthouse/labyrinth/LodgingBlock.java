package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.549, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md): the lodging house's fittings, drawn facing north by
 * tools/draw_lodging_house.py and turned by FACING. A fitting set against a wall is drawn against the south face of its cell, so
 * one facing east hangs on the wall to its west.
 * <ul>
 * <li>DOOR, SERVICE, CUPBOARD: a door's leaf (four panels; ledged boards for the passages; the low door under the stair). Stage bit 0
 * open, bit 1 the upper cell. BOLT_DOOR: Daniel's, the same with a bolt on its outer face; bit 2 the bolt shot. JIB: the papered
 * door at the back passage's end, flush with the wall while it is shut.</li>
 * <li>FRAME_SIDE, FRAME_BACK, FRAME_HEAD: a frame, a doorcase set into an outside wall with nothing behind it. The side: bit 0 the
 * upper cell, bit 1 on the opening's east as drawn (else its west), bit 2 Daniel's. The back, the opening a body can stand in,
 * plastered a hand's depth back: bit 0 upper, bit 1 Daniel's. The head: 0 west, 1 middle, 2 east as drawn; 3 to 5 Daniel's.</li>
 * <li>WINDOW: a window shuttered from inside, its shutters screwed shut. Stage 1 the upper cell.</li>
 * <li>RAIL: the gallery's railing round the stairwell, along its cell's north edge as drawn; stage 1 a corner, north and west.</li>
 * <li>BANISTER: the stair's handrail, along its cell's west edge, rising north; stage 1 the newel post at the stair's foot.</li>
 * <li>LADDER: a service ladder in the passages, climbed.</li>
 * <li>LAMP: a bare bulb on its flex (stage 0), the same turned low (1), a wall bracket (2).</li>
 * <li>FIREPLACE: the parlor's grate (stage 0) and its mantel and overmantel glass (1).</li>
 * <li>COAT_RAIL, HALL_TABLE (where the house's terms lie: the first paper), HALL_STAND (stage 1 its glass), BENCH (stage 1 its
 * other end; where each reader's account will wait).</li>
 * <li>NUMBER: a guest room's enamel number, one to six by stage. TALLY: marks on a wall, eight hands of them by stage.</li>
 * <li>SILL, POST, SHOE, RATCHET: the frames' shared support in the cellar: the sill, a timber half a cell high laid along its run
 * (FACING north or south lays it north and south); a post; a post's foot in its iron shoe with the wedge driven, sitting on the sill
 * (stage 1 on the bare floor); a post (stage 1 with rope burns round it); and the ratchet that holds the sill (stage 1 its wheel).</li>
 * </ul>
 */
public final class LodgingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<LodgingBlock> CODEC=simpleCodec(LodgingBlock::new);
    public enum Kind implements StringRepresentable {
        DOOR,BOLT_DOOR,SERVICE,JIB,CUPBOARD,FRAME_SIDE,FRAME_BACK,FRAME_HEAD,WINDOW,RAIL,BANISTER,LADDER,LAMP,FIREPLACE,COAT_RAIL,HALL_TABLE,HALL_STAND,BENCH,NUMBER,TALLY,SILL,POST,SHOE,RATCHET;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** A door of two cells whose stage's bit 0 is open and bit 1 the upper cell. */
        public boolean leaf(){return this==DOOR||this==BOLT_DOOR||this==SERVICE||this==JIB||this==CUPBOARD;}
        /** How many stages it draws; the rest draw stage 0. */
        public int stages(){return switch(this){case DOOR,SERVICE,JIB,CUPBOARD,FRAME_BACK->4;case BOLT_DOOR,FRAME_SIDE,TALLY->8;case FRAME_HEAD,NUMBER->6;case LAMP->3;
            case WINDOW,RAIL,BANISTER,FIREPLACE,HALL_STAND,BENCH,RATCHET,SHOE,POST->2;default->1;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    public LodgingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.DOOR).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return LodgingRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(LodgingRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** Whether a body can go through this cell: an open leaf. */
    public static boolean open(BlockState s){return s.is(LodgingRegistry.FITTING.get())&&s.getValue(KIND).leaf()&&(s.getValue(STAGE)&1)==1;}
    /** Daniel's door with its bolt shot. */
    public static boolean bolted(BlockState s){return is(s,Kind.BOLT_DOOR)&&(s.getValue(STAGE)&4)==4;}
    public static int light(BlockState s){return s.getValue(KIND)!=Kind.LAMP?0:switch(s.getValue(STAGE)){case 1->6;case 2->9;default->12;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case DOOR,BOLT_DOOR,SERVICE,CUPBOARD->(stage&1)==1?t(f,0,0,0,2,16,16):t(f,0,0,7,16,16,9);
            case JIB->(stage&1)==1?t(f,0,0,0,2,16,16):Shapes.block();
            case FRAME_SIDE,FRAME_HEAD,WINDOW,FIREPLACE->Shapes.block();
            case SILL->t(f,2,0,0,14,8,16);
            case FRAME_BACK->t(f,0,0,10,16,16,16);
            case RAIL->stage==1?Shapes.or(t(f,0,0,0,16,16,2),t(f,0,0,0,2,16,16)):t(f,0,0,0,16,16,2);
            case BANISTER->stage==1?t(f,12,0,6,16,16,10):t(f,0,0,0,2,16,16);
            case LADDER->t(f,0,0,13,16,16,16);
            case LAMP->stage==2?t(f,6,6,10,10,14,16):Block.box(5,4,5,11,16,11);
            case COAT_RAIL->t(f,0,2,12,16,10,16);
            case HALL_TABLE->t(f,1,0,6,15,15,16);
            case HALL_STAND->stage==1?t(f,1,0,12,15,16,16):t(f,1,0,8,15,16,16);
            case BENCH->Shapes.or(t(f,0,0,5,16,8,16),t(f,0,8,13,16,16,16));
            case NUMBER->t(f,4,4,15,12,12,16);
            case TALLY->Shapes.empty();
            case POST->Block.box(2,0,2,14,16,14);
            case SHOE->stage==1?Shapes.or(Block.box(1,0,1,15,5,15),Block.box(2,5,2,14,16,14)):Shapes.or(t(f,2,0,0,14,8,16),Block.box(1,8,1,15,13,15),Block.box(2,13,2,14,16,14));
            case RATCHET->stage==1?t(f,1,0,4,15,16,12):t(f,0,0,3,16,16,13);
        };
    }
    /** What stops a body: a shut leaf stops and an open one does not; the gallery's rail is too tall to step over. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case DOOR,BOLT_DOOR,SERVICE,CUPBOARD->(stage&1)==1?Shapes.empty():t(f,0,0,7,16,16,9);
            case JIB->(stage&1)==1?Shapes.empty():Shapes.block();
            case RAIL->stage==1?Shapes.or(t(f,0,0,0,16,24,2),t(f,0,0,0,2,24,16)):t(f,0,0,0,16,24,2);
            case BANISTER->stage==1?t(f,12,0,6,16,16,10):t(f,0,0,0,2,16,16);
            case LAMP,COAT_RAIL,NUMBER,TALLY,LADDER->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override public boolean isLadder(BlockState s,LevelReader l,BlockPos p,LivingEntity e){return s.getValue(KIND)==Kind.LADDER;}
}
