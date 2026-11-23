package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.754, the cold room (docs/COLD_ROOM_0_4_754.md): the rented house's things, drawn facing north by tools/draw_cold_room_art.py. A
 * thing that stands against a wall faces away from it.
 * <ul>
 * <li>BED: the back bedroom's iron bed, its head with the pillow (0) and its foot (1); the straps are buckled to its frame. The head's
 * facing points to the foot.</li>
 * <li>DRESSER: the chest of drawers, shut (0), its drawers slid out (1). BOOK: the old priest's book on it, shut (0), open (1).
 * CRUCIFIX: the cross on its nail, upright (0), turned upside down (1). NIGHTSTAND: a bedside table with its lamp (0), with the flask of
 * water (1). RADIATOR: under the window, cold. CHAIR: a kitchen chair (its seat ten up).</li>
 * <li>HALL_TABLE: the narrow table by the front door, bare (0), with the letter on it (1, the source). HOOKS: the coats on their hooks.
 * KITCHEN_TABLE, STOVE, SINK. BULB: a bare bulb on its flex under a ceiling. STREETLAMP: the lamp at the foot of the outside steps, its
 * post (0) and its head (1).</li>
 * </ul>
 */
public final class ColdBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ColdBlock> CODEC=simpleCodec(ColdBlock::new);
    public enum Kind implements StringRepresentable {
        BED,DRESSER,BOOK,CRUCIFIX,NIGHTSTAND,RADIATOR,CHAIR,HALL_TABLE,HOOKS,KITCHEN_TABLE,STOVE,SINK,BULB,STREETLAMP;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public ColdBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BED).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return ColdRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s){return s.is(ColdRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The house is lit by a bulb in the hall, one in the kitchen, one on the landing, the bedside lamp, and the street lamp outside. */
    static int light(BlockState s){
        return switch(s.getValue(KIND)){case BULB->10;case NIGHTSTAND->s.getValue(STAGE)==0?7:0;case STREETLAMP->s.getValue(STAGE)==1?12:0;default->0;};
    }
    /** The bed's head is a bed to the game, so whoever lies in it lies along it, the head on the pillow. */
    @Override public boolean isBed(BlockState s,BlockGetter level,BlockPos pos,@javax.annotation.Nullable LivingEntity sleeper){return is(s,Kind.BED)&&s.getValue(STAGE)%2==0;}
    @Override public void setBedOccupied(BlockState s,Level level,BlockPos pos,LivingEntity sleeper,boolean occupied){}
    @Override public Direction getBedDirection(BlockState s,LevelReader level,BlockPos pos){return is(s,Kind.BED)?s.getValue(FACING).getOpposite():s.getValue(FACING);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case BED->s.getValue(STAGE)>=2?Block.box(0,3,0,16,13,16):Block.box(0,0,0,16,10,16);
            case DRESSER->t(f,1,0,3,15,16,16);
            case BOOK->Block.box(3,0,4,13,2,12);
            case CRUCIFIX->t(f,5,2,14,11,14,16);
            case NIGHTSTAND->Block.box(2,0,2,14,11,14);
            case RADIATOR->t(f,1,0,10,15,11,15);
            case CHAIR->Shapes.or(t(f,2,0,2,14,10,14),t(f,2,10,12,14,16,14));
            case HALL_TABLE->t(f,1,0,8,15,13,16);
            case HOOKS->t(f,1,0,12,15,16,16);
            case KITCHEN_TABLE->Block.box(0,0,0,16,12,16);
            case STOVE->t(f,1,0,2,15,15,16);
            case SINK->t(f,0,0,3,16,14,16);
            case BULB->Block.box(6,4,6,10,16,10);
            case STREETLAMP->s.getValue(STAGE)==0?Block.box(6,0,6,10,16,10):Block.box(3,0,3,13,12,13);
        };
    }
    /** The crucifix and the bulb are above a hand and stand in nobody's way; the book lies flat; the rest stands in the way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CRUCIFIX,BULB,HOOKS->Shapes.empty();
            case CHAIR->t(f,2,0,2,14,10,14);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
