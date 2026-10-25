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
 * The Barretts' house (0.4.121): the walls, blinds and garage of the family's own home, the things that show somebody
 * lives in it, and the film crew's gear spread through it. tools/draw_barrett_home.py draws the models, authored facing
 * north. The double bed and the tub draw past their own block; BED_FILL gives the cells they cover their body.
 */
public final class BarrettHomeBlock extends Block {
    public static final MapCodec<BarrettHomeBlock> CODEC=simpleCodec(BarrettHomeBlock::new);
    public enum Kind implements StringRepresentable {
        PAINT,CONCRETE,DRYWALL,WINDOW,GARAGE_DOOR,STOREY,CEILING_LIGHT,CONSOLE,COAT_HOOKS,SHOE_RACK,PHOTOS,CALENDAR,FRIDGE,FRIDGE_TOP,
        SOFA_A,SOFA_B,TV_STAND,BOOKSHELF,PLANT,LAUNDRY,CLOTHES,DESK,DESK_KID,POSTER_A,POSTER_B,DRESSER,DOUBLE_BED,BED_FILL,
        TOILET,TUB,VANITY,WASHER,DRYER,BOXES,WATER_HEATER,SHELVES,CABLE,CABLE_CORNER,SOFTBOX,DIRECTORS_CHAIR,VILLAGE_CART,
        APPLE_BOXES,SANDBAGS,CRAFT_TABLE,FOLDING_TABLE,OFFICE_CHAIR;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean cube(){return ordinal()<=STOREY.ordinal();}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public BarrettHomeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.PAINT).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.BARRETT_HOME.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind){return of(kind,Direction.NORTH);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.BARRETT_HOME.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return switch(s.getValue(KIND)){case CEILING_LIGHT->14;case SOFTBOX->15;case WINDOW->6;case VILLAGE_CART->5;default->0;};}

    private static VoxelShape base(Kind k){return switch(k){
        case PAINT,CONCRETE,DRYWALL,WINDOW,GARAGE_DOOR,STOREY,FOLDING_TABLE->Shapes.block();
        case CEILING_LIGHT->Block.box(4,14.5,4,12,16,12);
        case CONSOLE->Block.box(0,0,4,16,12.5,15.5);
        case COAT_HOOKS->Block.box(0,2,11.5,16,14.5,16);
        case SHOE_RACK->Block.box(0,0,9,16,8,16);
        case PHOTOS->Block.box(0,2,15.5,16,14,16);
        case CALENDAR->Block.box(4,2,15.6,12,14,16);
        case FRIDGE->Block.box(1,0,2,15,16,16);
        case FRIDGE_TOP->Block.box(1,0,1.5,15,14,16);
        case SOFA_A,SOFA_B->Shapes.or(Block.box(0,0,3,16,7,16),Block.box(0,7,13,16,15,16));
        case TV_STAND->Shapes.or(Block.box(0,0,5,16,5,15),Block.box(0,6.5,9.5,16,16,10.5));
        case BOOKSHELF->Block.box(0,0,4,16,16,16);
        case PLANT->Block.box(4.5,0,4.5,11.5,7,11.5);
        case LAUNDRY->Block.box(2,0,3,14,9.5,13);
        case CLOTHES->Block.box(0,0,0,16,.6,16);
        case DESK->Block.box(0,0,2,16,12,15);
        case DESK_KID->Block.box(0,0,3,16,11,15);
        case POSTER_A,POSTER_B->Block.box(1,1,15.6,15,15,16);
        case DRESSER->Block.box(0,0,4,16,13,16);
        case DOUBLE_BED,BED_FILL->Block.box(0,0,0,16,10,16);
        case TOILET->Shapes.or(Block.box(4,0,2,12,9,12),Block.box(3,8,12,13,16,16));
        case TUB->Block.box(0,0,0,16,9,16);
        case VANITY->Block.box(0,0,4,16,13,16);
        case WASHER,DRYER->Block.box(1,0,2,15,15,16);
        case BOXES->Block.box(1,0,2,15,14,15);
        case WATER_HEATER->Block.box(3,0,3,13,16,13);
        case SHELVES->Block.box(0,0,8,16,16,16);
        case CABLE,CABLE_CORNER->Block.box(0,0,0,16,.25,16);
        case SOFTBOX->Block.box(6,0,6,10,16,10);
        case DIRECTORS_CHAIR->Shapes.or(Block.box(2,0,3,14,9,13.5),Block.box(2,9,12,14,16,13.5));
        case VILLAGE_CART->Block.box(.5,0,3,15.5,16,13);
        case APPLE_BOXES->Block.box(1,0,2,15,10,14);
        case SANDBAGS->Block.box(2,0,3,14,7,13);
        case CRAFT_TABLE->Block.box(0,0,2,16,12,14);
        case OFFICE_CHAIR->Shapes.or(Block.box(3,0,3,13,8,13),Block.box(3,8,12.5,13,16,13.5));
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
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CEILING_LIGHT,COAT_HOOKS,PHOTOS,CALENDAR,CLOTHES,POSTER_A,POSTER_B,CABLE,CABLE_CORNER->Shapes.empty();default->shape(s);};
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    // Walls and windows keep the light where it is; everything else lets it past.
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?15:0;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    /** The cells a model drawn past its own block covers, for its BED_FILL bodies: the bed's three, the tub's one. */
    public static java.util.List<BlockPos> covers(BlockPos at,Kind kind,Direction facing){
        var right=facing.getClockWise();var back=facing.getOpposite();
        return switch(kind){
            case DOUBLE_BED->java.util.List.of(at.relative(right),at.relative(back),at.relative(right).relative(back));
            case TUB->java.util.List.of(at.relative(back));
            default->java.util.List.of();
        };
    }
}
