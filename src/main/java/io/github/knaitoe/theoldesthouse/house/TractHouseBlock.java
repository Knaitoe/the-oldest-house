package io.github.knaitoe.theoldesthouse.house;

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
 * The Poltergeist model home's furnishings (0.4.121): a 1980s tract house, honey oak and harvest gold. One block carries
 * every piece: {@link Kind} picks it and {@code facing} turns it. tools/draw_tract_house.py draws the models, authored
 * facing north with the front toward -z, and their textures.
 */
public final class TractHouseBlock extends Block {
    public static final MapCodec<TractHouseBlock> CODEC=simpleCodec(TractHouseBlock::new);
    public enum Kind implements StringRepresentable {
        DINING_CHAIR,KIDS_CHAIR,TABLE_WEST,TABLE_EAST,SOFA_LEFT,SOFA_MIDDLE,SOFA_RIGHT,RECLINER,COFFEE_TABLE,END_TABLE_LAMP,
        TV_OFF,TV_STATIC,FRIDGE_LOWER,FRIDGE_UPPER,STOVE,COUNTER,COUNTER_SINK,WALL_CABINET,CEILING_LIGHT,PENDANT_LIGHT,
        CLOWN_DOLL,TOY_CHEST,CURTAIN_LEFT,CURTAIN_RIGHT,WALL_PHONE,FAMILY_PHOTO;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean television(){return this==TV_OFF||this==TV_STATIC;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public TractHouseBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.DINING_CHAIR).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return HouseBlocks.TRACT_HOUSE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(HouseBlocks.TRACT_HOUSE.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case END_TABLE_LAMP->11;case CEILING_LIGHT->14;case PENDANT_LIGHT->13;case TV_STATIC->7;case TV_OFF->1;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case DINING_CHAIR->Shapes.or(Block.box(2,0,2,14,9,14),Block.box(2,9,12,14,16,14));
        case KIDS_CHAIR->Shapes.or(Block.box(2,0,2,14,7,14),Block.box(2,7,12,14,13,14));
        case TABLE_WEST,TABLE_EAST->Block.box(0,0,1,16,16,15);
        case SOFA_LEFT,SOFA_MIDDLE,SOFA_RIGHT,RECLINER->Shapes.or(Block.box(0,0,1,16,8,16),Block.box(0,8,12,16,16,16));
        case COFFEE_TABLE->Block.box(1,0,3,15,8,13);case END_TABLE_LAMP->Block.box(2,0,2,14,16,14);
        case TV_OFF,TV_STATIC->Block.box(1,0,3,15,15,14);
        case FRIDGE_LOWER->Block.box(1,0,1,15,16,15);case FRIDGE_UPPER->Block.box(1,0,1,15,15,15);
        case STOVE,COUNTER,COUNTER_SINK->Block.box(0,0,0,16,16,16);case WALL_CABINET->Block.box(0,1,8,16,15,16);
        case CEILING_LIGHT->Block.box(4,12,4,12,16,12);case PENDANT_LIGHT->Block.box(3,5,3,13,16,13);
        case CLOWN_DOLL->Block.box(4,0,3,12,12,12);case TOY_CHEST->Block.box(1,0,3,15,11,13);
        case CURTAIN_LEFT->Block.box(0,0,14,5,16,16);case CURTAIN_RIGHT->Block.box(11,0,14,16,16,16);
        case WALL_PHONE->Block.box(3,4,13,11,13,16);case FAMILY_PHOTO->Block.box(3,5,15,13,13,16);
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        // get2DDataValue: south 0, west 1, north 2, east 3; turns are clockwise quarter turns from north, as the block states'.
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
    /** Wall pieces and lamps overhead are walked past and under. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CURTAIN_LEFT,CURTAIN_RIGHT,WALL_PHONE,FAMILY_PHOTO,CEILING_LIGHT,PENDANT_LIGHT->Shapes.empty();default->shape(s);};
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
