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
 * The hotel rebuilt (0.4.105): its siding, roof, papers, tiles and fittings in vanilla-style pixels. {@link Kind} picks the
 * piece, {@code facing} turns it and {@code stage} is its variant: how high the snow stands at a window (0 at the sill, 3 near
 * the top), which half of the tub (low bit) and whether her remains fill it (high bit), whether the table for two is laid,
 * served or cleared.
 * <p>A window's front is the room side, painted night and snow; its back is the outside, lit panes. Wall pieces face the room
 * they are seen from; everything else faces the way its front looks.
 */
public final class StanleyBlock extends Block {
    public static final MapCodec<StanleyBlock> CODEC=simpleCodec(StanleyBlock::new);
    public enum Kind implements StringRepresentable {
        SIDING,ROOF,EAVE,GOLD,ROSE,PARQUET,TILE,WHITE_TILE,PAINT,PAINT_LOW,LINO,CEILING,SHUTTERS,WINDOW_LOW,WINDOW_HIGH,
        COUNTER,REGISTER,KEY_RACK,BAR_COUNTER,BACK_BAR,TABLE,TABLE_SET,EASEL,CHANDELIER,SCONCE,CARD,BOOK,NOTICE,VALVE,SWITCH,
        TIME_CLOCK,RANGE,STEEL_TABLE,TUB,SINK,TRUNK,LINEN_SHELF,CART,SWITCHBOARD,FILING;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Whole cubes: siding, roof, papers, floors, ceilings and the windows set flush in the walls. */
        public boolean cube(){return ordinal()<=WINDOW_HIGH.ordinal();}
        public boolean window(){return this==WINDOW_LOW||this==WINDOW_HIGH;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** The table for two: laid, the guest's dinner served, the plate cleared. */
    public static final int LAID=0,SERVED=1,CLEARED=2;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public StanleyBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SIDING).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return HotelRegistry.STANLEY.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(HotelRegistry.STANLEY.get())&&s.getValue(KIND)==kind;}
    /** Chandeliers and sconces light the rooms; the bar's shelves glow a little behind the bottles. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case CHANDELIER->15;case SCONCE->12;case BACK_BAR->6;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case COUNTER,BAR_COUNTER->Shapes.or(Block.box(0,0,3,16,14,16),Block.box(0,14,0,16,16,16));
        case REGISTER->Shapes.or(Block.box(0,0,3,16,14,16),Block.box(0,14,0,16,16,16),Block.box(3,16,4,13,17,12));
        case KEY_RACK,SWITCHBOARD->Block.box(0,0,12,16,16,16);
        case BACK_BAR,LINEN_SHELF->Block.box(0,0,6,16,16,16);
        case TABLE,TABLE_SET->Block.box(0,0,0,16,16,16);
        case EASEL->Block.box(2,0,6,14,16,10);
        case CHANDELIER->Block.box(2,2,2,14,16,14);
        case SCONCE->Shapes.or(Block.box(5.5,4,15,10.5,13,16),Block.box(7,5,9,9,7,15.5),Block.box(7,6,8,9,10,10),Block.box(4,9,6,12,15,13));
        case CARD->Block.box(4,0,5,12,1,11);
        case BOOK->Block.box(3,0,4,13,2,12);
        case NOTICE->Block.box(3,3,15,13,14,16);
        case VALVE->Block.box(3,3,9,13,13,16);
        case SWITCH->Block.box(3,2,13,13,14,16);
        case TIME_CLOCK->Block.box(3,1,11,13,15,16);
        case RANGE->Block.box(0,0,1,16,15,16);
        case STEEL_TABLE->Shapes.or(Block.box(0,13,0,16,16,16),Block.box(1,0,1,3,13,3),Block.box(13,0,1,15,13,3),Block.box(1,0,13,3,13,15),Block.box(13,0,13,15,13,15));
        case TUB->Block.box(1,0,0,15,10,16);
        case SINK->Shapes.or(Block.box(6,0,8,10,10,12),Block.box(3,10,5,13,14,16));
        case TRUNK->Block.box(1,0,2,15,12,14);
        case CART->Block.box(1,0,2,15,15,14);
        case FILING->Block.box(2,0,3,14,16,16);
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
    /** What hangs from a ceiling or a wall, and a card or a book lying on a desk, is in nobody's way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CHANDELIER,SCONCE,CARD,BOOK,NOTICE,KEY_RACK,TIME_CLOCK,SWITCH->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
