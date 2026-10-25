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
 * The Wheel's rooms (0.4.121): the stages of a daughter's life, from her nursery to her mother's room. The nursery and the
 * childhood room are furnished at twice the size, for a reader made a child in them; the rest at ordinary size. One block
 * carries every piece: {@link Kind} picks it and {@code facing} turns it. tools/draw_wheel_house.py draws the models,
 * authored facing north with the front toward -z, and their textures.
 */
public final class LifeStageBlock extends Block {
    public static final MapCodec<LifeStageBlock> CODEC=simpleCodec(LifeStageBlock::new);
    public enum Kind implements StringRepresentable {
        CRIB_BIG_EDGE,CRIB_BIG_CORNER,CRIB_BIG_RAIL,CRIB_BIG_RAIL_CORNER,CRIB_END,MOBILE,MOBILE_BIG,MOBILE_STRING,DRAWERS,TOY_A,TOY_B,TOY_C,
        LAMP,BABY_BOOK,CHAIR_SMALL,CHAIR_BIG_LOWER,CHAIR_BIG_UPPER,BIGBED_HEAD,BIGBED_HEADBOARD,BIGBED_MID,BIGBED_FOOT,CRAYON_RED,CRAYON_BLUE,
        DRAWING,NIGHT_LIGHT,POSTER_BAND,POSTER_MOON,STEREO,LAVA_LAMP,DIARY,CLOTHES,KEEP_OUT,TEEN_BED_HEAD,TEEN_BED_FOOT,BOX,BOX_KITCHEN,
        BOX_OPEN,MATTRESS_HEAD,MATTRESS_FOOT,HOT_PLATE,BULB,LETTER,HOSP_BED_HEAD,HOSP_BED_FOOT,IV_LOWER,IV_UPPER,MONITOR,VISITOR_CHAIR,
        CARNATIONS,MOTHER_BED_HEAD,MOTHER_BED_FOOT,WARDROBE_LOWER,WARDROBE_UPPER,NOTEBOOK,NUMBER_1,NUMBER_2,NUMBER_3,NUMBER_4,NUMBER_5,NUMBER_6;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public static Kind number(int n){return values()[NUMBER_1.ordinal()+n-1];}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public LifeStageBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.DRAWERS).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return HouseBlocks.LIFE_STAGE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(HouseBlocks.LIFE_STAGE.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LAMP->12;case BULB->13;case LAVA_LAMP->9;case NIGHT_LIGHT->7;case MONITOR->5;default->0;};}

    /** Outline shapes, authored facing north (front toward -z) in sixteenths. */
    private static VoxelShape base(Kind k){return switch(k){
        case CRIB_BIG_EDGE->Shapes.or(Block.box(0,0,0,16,12,13),Block.box(0,0,13,16,16,16));
        case CRIB_BIG_CORNER->Shapes.or(Block.box(0,0,0,16,12,16),Block.box(0,12,13,16,16,16),Block.box(0,12,0,4,16,16));
        case CRIB_BIG_RAIL->Block.box(0,0,13,16,14.5,16);
        case CRIB_BIG_RAIL_CORNER->Shapes.or(Block.box(0,0,13,16,15,16),Block.box(0,0,0,4,15,16));
        case CRIB_END->Shapes.or(Block.box(1,0,0,15,7,14),Block.box(0,0,14,16,15,16),Block.box(0,0,0,1.75,14.5,14),Block.box(14.25,0,0,16,14.5,14));
        case MOBILE,MOBILE_BIG,MOBILE_STRING->Block.box(6,0,6,10,16,10);
        case DRAWERS->Block.box(0,0,.5,16,16,16);
        case TOY_A,TOY_B,TOY_C->Block.box(1.5,0,1.5,14.5,13,14.5);
        case LAMP->Block.box(3,0,3,13,16,13);
        case BABY_BOOK->Block.box(2,0,4,14,1,12);
        case CHAIR_SMALL->Shapes.or(Block.box(2.5,0,2.5,13.5,7.5,13.5),Block.box(3,7.5,11.5,13,15,13));
        case CHAIR_BIG_LOWER->Block.box(1,0,1,15,16,15);
        case CHAIR_BIG_UPPER->Block.box(1,0,11.5,15,16,15.5);
        case BIGBED_HEAD,BIGBED_MID,BIGBED_FOOT->Shapes.block();
        case BIGBED_HEADBOARD->Block.box(0,0,13.5,16,15.5,16);
        case CRAYON_RED,CRAYON_BLUE->Block.box(0,0,5.5,16,5,10.5);
        case DRAWING->Block.box(1,2,15.5,15,14,16);
        case NIGHT_LIGHT->Block.box(5,2,14.5,11,9,16);
        case POSTER_BAND,POSTER_MOON->Block.box(1,0,15.6,15,16,16);
        case KEEP_OUT->Block.box(3.5,5,15.6,12.5,12,16);
        case STEREO->Block.box(1,0,3,15,15,15);
        case LAVA_LAMP->Block.box(2,0,2,14,16,14);
        case DIARY->Block.box(2,0,3,14,11.5,15);
        case CLOTHES->Block.box(1,0,2,15,5.5,14);
        case TEEN_BED_HEAD->Shapes.or(Block.box(0,0,0,16,10,14.5),Block.box(0,0,14.5,16,15,16));
        case TEEN_BED_FOOT->Shapes.or(Block.box(0,0,0,16,11,1.5),Block.box(0,0,1.5,16,10,16));
        case BOX,BOX_KITCHEN->Block.box(1,0,1,15,13,15);
        case BOX_OPEN->Block.box(1,0,1,15,12.5,15);
        case MATTRESS_HEAD,MATTRESS_FOOT->Block.box(0,0,0,16,6,16);
        case HOT_PLATE->Block.box(1,0,1,15,14,15);
        case BULB->Block.box(6.5,3,6.5,9.5,16,9.5);
        case LETTER->Block.box(3,0,4.5,13,.5,11.5);
        case HOSP_BED_HEAD->Shapes.or(Block.box(0,0,0,16,12,14),Block.box(0,0,14,16,16,15.5));
        case HOSP_BED_FOOT->Block.box(0,0,0,16,12,16);
        case IV_LOWER->Block.box(6,0,6,10,16,10);
        case IV_UPPER->Block.box(3.5,0,6,12,13,10);
        case MONITOR->Block.box(3,0,4.8,13,15,12);
        case VISITOR_CHAIR->Shapes.or(Block.box(2,0,2,14,9,14),Block.box(2,9,12,14,16,14));
        case CARNATIONS->Block.box(2,0,3,14,16,15);
        case MOTHER_BED_HEAD->Shapes.or(Block.box(0,0,0,16,12,14),Block.box(0,0,14,16,16,16));
        case MOTHER_BED_FOOT->Shapes.or(Block.box(0,0,0,16,12,2),Block.box(0,0,2,16,10,16));
        case WARDROBE_LOWER,WARDROBE_UPPER->Block.box(0,0,1.5,16,16,16);
        case NOTEBOOK->Block.box(1,0,3,15,13,15);
        case NUMBER_1,NUMBER_2,NUMBER_3,NUMBER_4,NUMBER_5,NUMBER_6->Block.box(4.5,3,15.5,11.5,11,16);
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
    /** Things on the walls and overhead, and paper on the floor, are walked past and under. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case MOBILE,MOBILE_BIG,MOBILE_STRING,DRAWING,NIGHT_LIGHT,POSTER_BAND,POSTER_MOON,KEEP_OUT,BULB,LETTER,BABY_BOOK,
            NUMBER_1,NUMBER_2,NUMBER_3,NUMBER_4,NUMBER_5,NUMBER_6->Shapes.empty();default->shape(s);};
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
