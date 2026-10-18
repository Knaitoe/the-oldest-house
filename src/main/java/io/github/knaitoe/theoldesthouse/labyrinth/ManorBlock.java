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
 * Bly (0.4.105): the manor's boards and damask, its wet line, cupboards, schoolroom and conservatory, in vanilla-style pixels;
 * and the old man's signal lamp. {@link Kind} picks the piece, {@code facing} turns it and {@code stage} is its variant (which
 * portrait hangs, the lamp dark or lit).
 * <p>Wall pieces face the room they are seen from; everything else faces the way its front looks.
 */
public final class ManorBlock extends Block {
    public static final MapCodec<ManorBlock> CODEC=simpleCodec(ManorBlock::new);
    public enum Kind implements StringRepresentable {
        BOARDS,WET_BOARDS,DAMASK,WAINSCOT,CORNICE,PLASTER,FLAGS,WET_FLAGS,GLAZING,RUG,PORTRAIT,SCONCE,CATCH,LINEN,BENCH,NOTE,
        DOLLHOUSE,BLACKBOARD,SCHOOL_DESK,GLOBE,CLOCK_LOW,CLOCK_HIGH,DOORMAT,UMBRELLA,MANTEL,SIGNAL_LAMP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Whole cubes: the manor's floors, walls and ceilings, and the old man's lamp box. */
        public boolean cube(){return this==BOARDS||this==WET_BOARDS||this==DAMASK||this==WAINSCOT||this==CORNICE||this==PLASTER||this==FLAGS||this==WET_FLAGS;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public ManorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BOARDS).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.MANOR.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.MANOR.get())&&s.getValue(KIND)==kind;}
    /** Candles in the sconces burn low; the old man's lamp is bright when it is on. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case SCONCE->8;case SIGNAL_LAMP->s.getValue(STAGE)==1?15:0;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case GLAZING->Block.box(0,0,7,16,16,9);case RUG,DOORMAT->Block.box(0,0,0,16,1,16);
        case PORTRAIT->Block.box(2,2,15,14,15,16);case SCONCE->Block.box(5,3,12,11,13,16);case CATCH->Block.box(6,6,14,10,10,16);
        case LINEN->Block.box(0,0,6,16,16,16);case BENCH->Block.box(0,0,2,16,15,16);case NOTE->Block.box(3,0,4,13,1,12);
        case DOLLHOUSE->Block.box(1,0,4,15,15,15);case BLACKBOARD->Block.box(0,2,14,16,15,16);case SCHOOL_DESK->Block.box(1,0,2,15,13,15);
        case GLOBE->Block.box(4,0,4,12,14,12);case CLOCK_LOW->Block.box(2,0,4,14,16,15);case CLOCK_HIGH->Block.box(1,0,3,15,14,15);
        case UMBRELLA->Block.box(4,0,4,12,14,12);case MANTEL->Block.box(0,10,6,16,14,16);
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
    /** Glazing is a wall; what hangs on a wall, and a rug or a note underfoot, is in nobody's way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case GLAZING->Shapes.block();case PORTRAIT,SCONCE,CATCH,BLACKBOARD,MANTEL,RUG,DOORMAT,NOTE->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    /** Panes side by side read as one glazed wall: the edges they share are not drawn. */
    @Override protected boolean skipRendering(BlockState s,BlockState adjacent,Direction d){
        return s.getValue(KIND)==Kind.GLAZING&&ManorBlock.is(adjacent,Kind.GLAZING)&&d.getAxis()!=s.getValue(FACING).getAxis()||super.skipRendering(s,adjacent,d);
    }
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
