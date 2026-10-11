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
 * Three Fingers (0.4.81): the cottage on the mill road, in vanilla-style pixels. One block carries each piece the
 * household furniture does not: {@link Kind} picks it, {@code facing} turns it and {@code stage} is its variant (the
 * lamp lit or guttering, the kettle cold or on the boil).
 * <p>Wall pieces face the room they are seen from; everything else faces the way its front looks.
 */
public final class CottageBlock extends Block {
    public static final MapCodec<CottageBlock> CODEC=simpleCodec(CottageBlock::new);
    public enum Kind implements StringRepresentable {
        WALLPAPER,WAINSCOT,FLAGSTONE,WHITEWASH,THATCH,RANGE,KETTLE,DRESSER,DRESSER_TOP,MANTEL,SAMPLER,POCKETBOOK,BIBLE,LAMP_HEAD,MILESTONE,DOORSTEP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Whole cubes: the cottage's own walls, floor and roof. */
        public boolean cube(){return this==WALLPAPER||this==WAINSCOT||this==FLAGSTONE||this==WHITEWASH||this==THATCH;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public CottageBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALLPAPER).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.COTTAGE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.COTTAGE.get())&&s.getValue(KIND)==kind;}
    /** The lamp burns, or gutters low; the range glows a little through its door. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP_HEAD->s.getValue(STAGE)==0?13:4;case RANGE->5;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case KETTLE->Block.box(4,0,4,12,8,12);case DRESSER->Block.box(0,0,2,16,16,16);case DRESSER_TOP->Block.box(0,0,9,16,14,16);
        case MANTEL->Block.box(0,10,6,16,14,16);case SAMPLER->Block.box(2,3,15,14,13,16);case POCKETBOOK->Block.box(4,0,5,12,2,11);
        case BIBLE->Block.box(3,0,3,13,4,13);case LAMP_HEAD->Block.box(4,0,4,12,12,12);case MILESTONE->Block.box(3,0,4,13,12,12);
        case DOORSTEP->Block.box(0,0,0,16,4,16);
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
    /** A sampler on the wall and a mantel overhead are not in anyone's way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case SAMPLER,MANTEL->Shapes.empty();default->shape(s);};
    }
    /** Only the whole cubes hide the faces behind them. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube()&&s.getValue(KIND)!=Kind.RANGE&&s.getValue(KIND)!=Kind.DRESSER;}
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
