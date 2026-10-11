package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Last Stop (0.4.78): the roadside bar, its highway, lot and field, in vanilla-style pixels. One block carries every
 * piece: {@link Kind} picks it, {@code facing} turns it and {@code stage} is its variant (which lot gravel, the pool balls
 * racked or broken, a neon tube lit or dark, and for the pickups their paint, drawn by the client's tint).
 * <p>Wall pieces face the room they are read from; the pickup's side pieces face outward; its hood, cab and bed face the
 * way the truck points, its front pieces forward and its tail pieces back.
 */
public final class RoadhouseBlock extends Block {
    public static final MapCodec<RoadhouseBlock> CODEC=simpleCodec(RoadhouseBlock::new);
    public enum Kind implements StringRepresentable {
        ASPHALT,ASPHALT_EDGE,ASPHALT_CENTRE,GRAVEL,BOARDS,
        COUNTER,BACK_BAR,SHELF,MIRROR,TAPS,REGISTER,GLASSES,STOOL(10),BOOTH(7),BOOTH_TABLE,POOL_L,POOL_R,POOL_LAMP,DARTBOARD,CORKBOARD,NEON_BEER,
        SIGN_TL,SIGN_TM,SIGN_TR,SIGN_BL,SIGN_BM,SIGN_BR,POLE,REFLECTOR,STALKS,STALKS_TOP,STUBBLE,CAP,FALLEN_RAIL,
        PU_FRONT_L,PU_FRONT_M,PU_FRONT_R,PU_WHEEL,PU_HOOD,PU_DOOR,PU_DOOR_DENT,PU_BENCH,PU_CAB,PU_BED_SIDE,PU_BED_WHEEL,PU_BED,PU_TAIL_L,PU_TAIL_M,PU_TAIL_R;
        /** How high a sitter's hips rest, in sixteenths; zero for anything that is not a seat. */
        public final int seatHeight;
        Kind(){this(0);}
        Kind(int seat){seatHeight=seat;}
        public boolean seat(){return seatHeight>0;}
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Painted by the client's tint from {@code stage}. */
        public boolean pickup(){return ordinal()>=PU_FRONT_L.ordinal();}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** The pickups' four paints, in stage order: rust red, faded blue, tan and dark green. */
    public static final int[] PAINT={0x9a4330,0x4d6a8e,0xc2ae84,0x4f6b46};
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public RoadhouseBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BOARDS).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.ROADHOUSE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.ROADHOUSE.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){
        case SIGN_TL,SIGN_TM,SIGN_TR->s.getValue(STAGE)==0?12:0;case SIGN_BL,SIGN_BM,SIGN_BR->s.getValue(STAGE)==0?9:0;
        case NEON_BEER->s.getValue(STAGE)==0?9:0;case POOL_LAMP->13;case PU_FRONT_L,PU_FRONT_R->6;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case MIRROR->Block.box(0,0,14,16,16,16);case TAPS->Block.box(5,0,6,11,12,10);case REGISTER->Block.box(3,0,4,13,9,13);case GLASSES->Block.box(2,0,2,14,5,14);
        case STOOL->Block.box(4,0,4,12,10,12);case BOOTH->Shapes.or(Block.box(0,0,0,16,7,16),Block.box(0,0,13,16,16,16));case BOOTH_TABLE->Block.box(0,0,0,16,12,16);
        case POOL_L,POOL_R->Block.box(0,0,0,16,13,16);case POOL_LAMP->Block.box(3,4,3,13,16,13);case DARTBOARD->Block.box(3,3,15,13,13,16);case CORKBOARD->Block.box(0,1,15,16,15,16);
        case NEON_BEER->Block.box(0,0,7,16,16,9);case SIGN_TL,SIGN_TM,SIGN_TR,SIGN_BL,SIGN_BM,SIGN_BR->Block.box(0,0,6,16,16,10);case POLE->Block.box(6,0,6,10,16,10);
        case REFLECTOR->Block.box(7,0,7,9,12,9);case STALKS->Block.box(2,0,2,14,16,14);case STALKS_TOP->Block.box(2,0,2,14,10,14);case STUBBLE->Block.box(1,0,1,15,4,15);
        case CAP->Block.box(4,0,4,12,3,12);case FALLEN_RAIL->Block.box(0,0,6,16,2,10);
        case PU_FRONT_L,PU_FRONT_M,PU_FRONT_R,PU_WHEEL,PU_HOOD,PU_TAIL_L,PU_TAIL_M,PU_TAIL_R->Block.box(0,0,0,16,13,16);
        case PU_CAB->Block.box(0,0,0,16,12,16);
        case PU_BED_SIDE,PU_BED_WHEEL->Shapes.or(Block.box(0,0,0,16,6,16),Block.box(0,6,0,16,13,4));case PU_BED->Block.box(0,0,0,16,6,16);
        default->Shapes.block();
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        // get2DDataValue: south 0, west 1, north 2, east 3; turns are clockwise quarter turns from north.
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var k=s.getValue(KIND);var all=SHAPES.computeIfAbsent(k,x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(base(k),s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    /** Stalks, stubble and what lies flat are walked through; pictures on the wall and the lamp over the table are not in the way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case STALKS,STALKS_TOP,STUBBLE,CAP,FALLEN_RAIL,MIRROR,DARTBOARD,CORKBOARD,POOL_LAMP->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){
        return switch(s.getValue(KIND)){case ASPHALT,ASPHALT_EDGE,ASPHALT_CENTRE,GRAVEL,BOARDS->Shapes.block();default->Shapes.empty();};
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return switch(s.getValue(KIND)){case ASPHALT,ASPHALT_EDGE,ASPHALT_CENTRE,GRAVEL,BOARDS,COUNTER,BACK_BAR,SHELF->false;default->true;};}
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
