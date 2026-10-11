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
 * The child's room (0.4.74): its own furniture and toys, each one of a kind, in vanilla-style pixels. One block carries them
 * all: {@link Kind} picks the piece, {@code facing} turns it, and {@code stage} is its state of play (a lid open, a top
 * fallen over, a tower of blocks one higher). Stage 3 of the small toys is the same toy stuck to the ceiling.
 */
public final class NurseryBlock extends Block {
    public static final MapCodec<NurseryBlock> CODEC=simpleCodec(NurseryBlock::new);
    public enum Kind implements StringRepresentable {
        BED_HEAD,BED_FOOT,DESK,CHAIR,TOY_CHEST,SHELF,WARDROBE_LOW,WARDROBE_HIGH,DOLLHOUSE,ROCKING_HORSE,NIGHT_LIGHT,CEILING_LAMP,MOBILE,
        RUG,TRACK,DRAWINGS,CARD,ACCOUNT,BOXES,CRIB,TRAIN,TOP,JACK_BOX,MUSIC_BOX,TEDDY,BLOCKS,BALL,DOLL;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The small toys a reader can find stuck to the ceiling. */
        public boolean ceilingToy(){return this==TEDDY||this==DOLL||this==BLOCKS||this==BALL;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public static final int CEILING=3;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public NurseryBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.TEDDY).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.NURSERY.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.NURSERY.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case NIGHT_LIGHT->s.getValue(STAGE)==0?8:0;case CEILING_LAMP->s.getValue(STAGE)==0?12:s.getValue(STAGE)==1?5:0;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k,int stage){return switch(k){
        case BED_HEAD->Shapes.or(Block.box(0,0,0,16,6,16),Block.box(0,0,0,16,14,2));
        case BED_FOOT->Shapes.or(Block.box(0,0,0,16,6,16),Block.box(0,0,14,16,10,16));
        case DESK->Block.box(1,0,1,15,12,15);case CHAIR->Block.box(3,0,3,13,8,13);case TOY_CHEST->Block.box(1,0,3,15,stage==1?14:9,13);
        case SHELF->Block.box(0,0,6,16,16,16);case WARDROBE_LOW->Block.box(1,0,4,15,16,16);case WARDROBE_HIGH->Block.box(1,0,4,15,14,16);
        case DOLLHOUSE->Block.box(1,0,3,15,15,16);case ROCKING_HORSE->Block.box(4,0,1,12,13,15);case NIGHT_LIGHT->Block.box(5,0,5,11,7,11);
        case CEILING_LAMP->Block.box(4,6,4,12,16,12);case MOBILE->Block.box(2,4,2,14,16,14);case RUG,TRACK->Block.box(0,0,0,16,1,16);
        case DRAWINGS->Block.box(1,2,15,15,14,16);case CARD->Block.box(5,0,5,11,3,11);case ACCOUNT->Block.box(3,0,4,13,2,12);
        case BOXES->Block.box(1,0,1,15,13,15);case CRIB->Block.box(0,0,2,16,12,14);case TRAIN->Block.box(4,0,1,12,8,15);
        case TOP->stage==1?Block.box(2,0,5,14,5,11):Block.box(5,0,5,11,8,11);case JACK_BOX->Block.box(4,0,4,12,stage==1?15:8,12);
        case MUSIC_BOX->Block.box(4,0,5,12,stage==1?10:5,11);case TEDDY->Block.box(4,0,4,12,10,12);
        case BLOCKS->Block.box(3,0,3,13,stage==0?4:stage==1?8:stage==2?12:5,13);case BALL->Block.box(5,0,5,11,6,11);case DOLL->Block.box(5,0,5,11,10,11);
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        // get2DDataValue: south 0, west 1, north 2, east 3; turns are clockwise quarter turns from north.
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape ceiling(VoxelShape shape){var out=new VoxelShape[]{Shapes.empty()};shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->out[0]=Shapes.or(out[0],Shapes.box(x0,1-y1,z0,x1,1-y0,z1)));return out[0];}
    private static VoxelShape shape(BlockState s){
        var k=s.getValue(KIND);int stage=s.getValue(STAGE);var all=SHAPES.computeIfAbsent(k,x->new VoxelShape[16]);int i=s.getValue(FACING).get2DDataValue()*4+stage;
        if(all[i]==null){var shape=rotated(base(k,k.ceilingToy()&&stage==CEILING?0:stage),s.getValue(FACING));all[i]=k.ceilingToy()&&stage==CEILING?ceiling(shape):shape;}
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    /** Flat things are walked over and the hanging ones walked under; the bed's deck leaves room beneath it. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case MOBILE,DRAWINGS,CARD->Shapes.empty();case RUG,TRACK->Block.box(0,0,0,16,1,16);default->shape(s);};
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
