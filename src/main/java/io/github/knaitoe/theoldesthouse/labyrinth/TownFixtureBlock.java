package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * Proofrock's painted and fitted pieces (0.4.67): lockers, chalkboards, the trophy case and crest, folded bleachers, shop
 * signboards in three panels, student desks and missing posters. Each faces the side it is read from.
 */
public final class TownFixtureBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TownFixtureBlock> CODEC=simpleCodec(TownFixtureBlock::new);
    /** The signboards, each painted across a left, middle and right panel. */
    public static final String[] SIGNS={"general","post","laundry","hardware","sheriff","diner","cinema","bait","hall","motel","garage","school"};
    public enum Kind implements StringRepresentable {
        LOCKER, LOCKER_TOP, CHALK_L, CHALK_M, CHALK_R, TROPHY, CREST, BLEACHER, POSTER, STUDENT_DESK,
        GENERAL_L, GENERAL_M, GENERAL_R, POST_L, POST_M, POST_R, LAUNDRY_L, LAUNDRY_M, LAUNDRY_R, HARDWARE_L, HARDWARE_M, HARDWARE_R,
        SHERIFF_L, SHERIFF_M, SHERIFF_R, DINER_L, DINER_M, DINER_R, CINEMA_L, CINEMA_M, CINEMA_R, BAIT_L, BAIT_M, BAIT_R,
        HALL_L, HALL_M, HALL_R, MOTEL_L, MOTEL_M, MOTEL_R, GARAGE_L, GARAGE_M, GARAGE_R, SCHOOL_L, SCHOOL_M, SCHOOL_R,
        CAR_RED,CAR_BLUE,CAR_GREEN,CAR_WHITE,CAR_LIGHT_BLUE,CAR_BROWN,CAR_CYAN,CAR_ORANGE,
        HOOD_RED,HOOD_BLUE,HOOD_GREEN,HOOD_WHITE,HOOD_LIGHT_BLUE,HOOD_BROWN,HOOD_CYAN,HOOD_ORANGE,CAR_WHEEL,CAR_CABIN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** Panel 0, 1 or 2 of a named signboard. */
        public static Kind sign(String name,int panel){return valueOf(name.toUpperCase(java.util.Locale.ROOT)+"_"+"LMR".charAt(panel));}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public TownFixtureBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.LOCKER));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static BlockState of(Kind kind,Direction facing){return DrownedTownRegistry.FIXTURE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            // A paper sheet flat against the wall behind it.
            case POSTER->switch(s.getValue(FACING)){case NORTH->Block.box(1,1,15,15,15,16);case SOUTH->Block.box(1,1,0,15,15,1);case WEST->Block.box(15,1,1,16,15,15);default->Block.box(0,1,1,1,15,15);};
            // The desk and its attached seat stand within one block whichever way they face.
            case STUDENT_DESK->Block.box(1,0,1,15,12,15);
            case HOOD_RED,HOOD_BLUE,HOOD_GREEN,HOOD_WHITE,HOOD_LIGHT_BLUE,HOOD_BROWN,HOOD_CYAN,HOOD_ORANGE->Block.box(0,0,0,16,8,16);
            default->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.POSTER?Shapes.empty():getShape(s,l,p,c);}
    /** Only the full panels hide what is behind them; a sheet of paper and a desk do not. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k==Kind.POSTER||k==Kind.STUDENT_DESK?Shapes.empty():getShape(s,l,p,CollisionContext.empty());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
