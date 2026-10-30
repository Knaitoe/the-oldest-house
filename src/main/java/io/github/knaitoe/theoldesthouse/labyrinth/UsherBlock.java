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
 * The house of Usher (0.4.181), in vanilla-style pixels drawn by tools/draw_usher_art.py: its fungus-grown stone and the
 * fissure down its front, the black floors, tapestries and fretted ceilings, the damp vault, the causeway, the tarn's ooze and
 * the night over it; the long pointed windows, draperies, Roderick's easel, guitar and strewn books, the candelabra, the
 * armorial trophies and his chair. {@link Kind} picks the piece, {@code facing} turns it and {@code stage} is its variant.
 * <p>The fissure runs through two kinds so each cell can show where the crack enters and leaves it: {@link Kind#FISSURE}
 * stages are top to bottom, top to east, top to west and west to bottom (looking at the front); {@link Kind#FISSURE_B}
 * stages are east to bottom, west to east, and, last, the crack opened wide.
 */
public final class UsherBlock extends Block {
    public static final MapCodec<UsherBlock> CODEC=simpleCodec(UsherBlock::new);
    public enum Kind implements StringRepresentable {
        STONE,FISSURE,FISSURE_B,FLOOR,TAPESTRY,FRET,VAULT,CAUSEWAY,MUD,NIGHT,
        WINDOW,DRAPERY,EASEL,GUITAR,BOOKS,CANDELABRA,ARMORIAL,CHAIR;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Whole cubes: the house's stone, floors, walls and ceilings, the causeway, the ooze and the night. */
        public boolean cube(){return ordinal()<=NIGHT.ordinal();}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** FISSURE_B's last stage: the crack opened into a black gap. */
    public static final int OPEN=2;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public UsherBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.STONE).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind){return LiteraryRegistry.USHER.get().defaultBlockState().setValue(KIND,kind);}
    public static BlockState of(Kind kind,Direction facing){return of(kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.USHER.get())&&s.getValue(KIND)==kind;}
    /** The candelabra burn; nothing else in the house gives light of its own. */
    public static int light(BlockState s){return s.getValue(KIND)==Kind.CANDELABRA?11:0;}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case WINDOW->Block.box(0,0,7,16,16,9);case DRAPERY->Block.box(0,0,14,16,16,16);
        case EASEL->Block.box(2,0,6,14,16,10);case GUITAR->Block.box(5,0,6,11,14,10);case BOOKS->Block.box(1,0,1,15,2,15);
        case CANDELABRA->Block.box(5,0,5,11,16,11);case ARMORIAL->Block.box(2,2,15,14,14,16);case CHAIR->Block.box(2,0,2,14,16,14);
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
    /** A window is a wall; what hangs on a wall, and books underfoot, are in nobody's way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case WINDOW->Shapes.block();case DRAPERY,ARMORIAL,BOOKS->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    /** Windows side by side read as one long pane: the edges they share are not drawn. */
    @Override protected boolean skipRendering(BlockState s,BlockState adjacent,Direction d){
        return s.getValue(KIND)==Kind.WINDOW&&is(adjacent,Kind.WINDOW)&&d.getAxis()==Direction.Axis.Y||super.skipRendering(s,adjacent,d);
    }
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
