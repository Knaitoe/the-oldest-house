package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Winchester house's fittings (0.4.121): the séance table with its hand bell, a blue velvet side chair, the hall clock
 * (two blocks, its dial with thirteen hours), and what the unseen carpenters leave out: a sawhorse with a board half cut,
 * new lumber, a keg of nails with the hammer on it, and the brass 13 beside a door. tools/draw_winchester.py draws the
 * models, authored facing north.
 */
public final class WinchesterFittingBlock extends Block {
    public static final MapCodec<WinchesterFittingBlock> CODEC=simpleCodec(WinchesterFittingBlock::new);
    public enum Kind implements StringRepresentable {
        SEANCE_TABLE,SEANCE_CHAIR,CLOCK,CLOCK_TOP,SAWHORSE,LUMBER,NAIL_KEG,PLAQUE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public WinchesterFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SEANCE_TABLE).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.WINCHESTER_FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.WINCHESTER_FITTING.get())&&s.getValue(KIND)==kind;}

    private static VoxelShape base(Kind k){return switch(k){
        case SEANCE_TABLE->Shapes.or(Block.box(1,11,1,15,13,15),Block.box(5.5,0,5.5,10.5,11,10.5));
        case SEANCE_CHAIR->Shapes.or(Block.box(2.5,0,2.5,13.5,9.5,13.5),Block.box(3,9.5,12,13,16,13.5));
        case CLOCK->Shapes.or(Block.box(2,0,4,14,3,15),Block.box(3,3,5,13,16,14));
        case CLOCK_TOP->Shapes.or(Block.box(2,0,4,14,12,15),Block.box(1,12,3,15,14,16));
        case SAWHORSE->Block.box(0,0,4,16,13,12);
        case LUMBER->Block.box(0,0,1,16,6,15.5);
        case NAIL_KEG->Block.box(4,0,4,12,12,12);
        case PLAQUE->Block.box(4,5,15,12,12,16);
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var all=SHAPES.computeIfAbsent(s.getValue(KIND),x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(base(s.getValue(KIND)),s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.PLAQUE?Shapes.empty():shape(s);}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
