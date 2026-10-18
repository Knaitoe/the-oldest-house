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
 * The Masque (0.4.105): Prince Prospero's seven chambers in vanilla-style pixels. {@link Kind} picks the piece, {@code facing}
 * turns it and {@code stage} is the chamber's colour (0 blue, 1 purple, 2 green, 3 orange, 4 white, 5 violet, 6 black, whose
 * panes are scarlet); the clock's case and face use stage 1 for stopped.
 * <p>Wall pieces face the room they are seen from; everything else faces the way its front looks.
 */
public final class MasqueBlock extends Block {
    public static final MapCodec<MasqueBlock> CODEC=simpleCodec(MasqueBlock::new);
    public enum Kind implements StringRepresentable {
        VELVET,PARQUET,COFFER,EBONY,CORNICE,TRACERY,LANCET,LANCET_ARCH,TRIPOD,BRAZIER,CLOCK_BASE,CLOCK_CASE,CLOCK_FACE,CLOCK_CROWN,DIVAN,MUSIC_STAND,MASK,INVITATION,STAND;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Whole cubes: the chambers' hangings, floors, ceilings, ebony frames and stone. */
        public boolean cube(){return this==VELVET||this==PARQUET||this==COFFER||this==EBONY||this==CORNICE||this==TRACERY;}
        /** Stained glass: seen through, and lets the braziers' light in. */
        public boolean glass(){return this==LANCET||this==LANCET_ARCH;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,6);
    public static final int BLACK=6;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public MasqueBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.VELVET).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.MASQUE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.MASQUE.get())&&s.getValue(KIND)==kind;}
    /** The braziers in the closed corridor are the only fire: Poe's chambers have no lamp or candle inside. */
    public static int light(BlockState s){return s.getValue(KIND)==Kind.BRAZIER?15:0;}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case LANCET,LANCET_ARCH->Block.box(0,0,6,16,16,10);
        case TRIPOD->Block.box(3,0,3,13,16,13);case BRAZIER->Block.box(2,0,2,14,9,14);
        case CLOCK_BASE,CLOCK_CASE,CLOCK_FACE->Block.box(1,0,3,15,16,16);case CLOCK_CROWN->Block.box(0,0,2,16,12,16);
        case DIVAN->Block.box(0,0,3,16,10,16);case MUSIC_STAND->Block.box(4,0,5,12,15,11);case MASK->Block.box(3,3,14,13,13,16);
        case INVITATION->Block.box(4,0,4,12,3,12);case STAND->Block.box(3,0,3,13,16,13);
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
    /** A window is a wall to walk into; a mask on the wall is in nobody's way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case LANCET,LANCET_ARCH->Shapes.block();case MASK->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube()&&s.getValue(KIND)!=Kind.CLOCK_BASE&&s.getValue(KIND)!=Kind.CLOCK_CASE&&s.getValue(KIND)!=Kind.CLOCK_FACE;}
    /** Lancets side by side read as one window: the edges they share are not drawn. */
    @Override protected boolean skipRendering(BlockState s,BlockState adjacent,Direction d){
        return s.getValue(KIND).glass()&&adjacent.is(this)&&adjacent.getValue(KIND).glass()&&adjacent.getValue(STAGE).equals(s.getValue(STAGE))&&d.getAxis()!=s.getValue(FACING).getAxis()||super.skipRendering(s,adjacent,d);
    }
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
