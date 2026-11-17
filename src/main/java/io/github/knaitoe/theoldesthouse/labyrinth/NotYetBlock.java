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
 * 0.4.663, Not Yet (docs/NOT_YET_0_4_663.md): the ward's things, drawn facing north by tools/draw_not_yet_art.py.
 * <ul>
 * <li>COT and COT_FOOT: the two cells of a ward cot, its head in the cell it stands in and its foot in the cell it faces. Stage 0 as it is,
 * 1 heating, 2 made. The head is a bed to the game, so a reader can lie on it.</li>
 * <li>CURTAIN: a curtain on its rail, two cells high: 0 the rail alone, 1 the rail with the curtain bunched under it, 2 the bunched curtain's
 * lower half, 3 the rail with the curtain drawn, 4 the drawn curtain's lower half.</li>
 * <li>LAMP: a lamp on a jointed arm from the wall, off (0) or on (1). SCREEN: dark (0), the machine's two words (1), the far door open (2),
 * the far door open with somebody in it (3), noise (4). COUNTER: one window of the counter.</li>
 * <li>TABLE and TABLE_FOOT: the mending table, its head a bed to the game. ARM: an arm over it, folded (0), let down (1), working (2).</li>
 * <li>HATCH: a plate of the passage floor, shut (0) or open (1). VENT: still (0) or letting in cold (1). WINDOW: the painted outdoors, its
 * stage the piece of the picture. DOOR: the far door, lower and upper shut (0, 1) or open (2, 3). CHART: the ward's chart by the door.</li>
 * </ul>
 */
public final class NotYetBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<NotYetBlock> CODEC=simpleCodec(NotYetBlock::new);
    public enum Kind implements StringRepresentable {
        COT,COT_FOOT,CURTAIN,LAMP,SCREEN,COUNTER,TABLE,TABLE_FOOT,ARM,HATCH,VENT,WINDOW,DOOR,CHART;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    /** Stages of a cot. */
    public static final int PLAIN=0,HEATING=1,MADE=2;
    /** Stages of a screen. */
    public static final int DARK=0,WORDS=1,DOOR_OPEN=2,SOMEONE=3,NOISE=4;
    public NotYetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.COT).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return NotYetRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s){return s.is(NotYetRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a lamp that is on, a screen (a little even when dark), the painted window, the counter's digits. */
    static int light(BlockState s){
        return switch(s.getValue(KIND)){
            case LAMP->s.getValue(STAGE)==1?11:0;case SCREEN->s.getValue(STAGE)==DARK?1:6;case WINDOW->8;case COUNTER->4;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COT->Shapes.or(t(f,1,0,0,15,9,16),t(f,1,9,14,15,14,15.5));
            case COT_FOOT->Shapes.or(t(f,1,0,.5,15,9,16),t(f,1,9,.5,15,10,2));
            case CURTAIN->stage==0?t(f,0,15,7.5,16,16,8.5):stage<=2?t(f,0,0,6.5,6.25,16,9.75):t(f,0,0,7,16,16,9);
            case LAMP->t(f,5,6,1,11,15,16);
            case SCREEN->t(f,2,3,12,14,13,16);
            case COUNTER->t(f,0,0,13,16,16,16);
            case TABLE,TABLE_FOOT->t(f,1,0,0,15,11,16);
            case ARM->Block.box(5,stage==0?9:2,5,11,16,11);
            case HATCH->stage==0?Shapes.block():Shapes.or(t(f,0,0,0,8,16,.75),t(f,8,0,15.25,16,16,16));
            case VENT->t(f,2,2,15,14,14,16);
            case WINDOW->Shapes.block();
            case DOOR->stage<=1?t(f,0,0,5.5,16,16,10.5):Shapes.or(t(f,0,0,5.5,1.5,16,10.5),t(f,14.5,0,5.5,16,16,10.5));
            case CHART->t(f,3.5,2,14.5,12.5,14,16);
        };
    }
    /** Cots, the table, the screens, the counter, the window and a shut door or hatch stand in the way; curtains, lamps and arms are reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COT,COT_FOOT,SCREEN,COUNTER,TABLE,TABLE_FOOT,VENT,WINDOW->getShape(s,l,p,c);
            case HATCH->stage==0?Shapes.block():Shapes.empty();
            case DOOR->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k!=Kind.WINDOW&&!(k==Kind.HATCH&&s.getValue(STAGE)==0);}
    // ------------------------------------------------------------------------------------------------ lying down
    /**
     * A cot's head and the mending table's head are beds to the game, so a body can lie on them. The game lays a sleeper with their head
     * toward the way a bed points; a cot points from its head to its foot, which is the way it does not face (as PellThing's camp bed).
     */
    @Override public boolean isBed(BlockState s,BlockGetter level,BlockPos pos,@javax.annotation.Nullable LivingEntity sleeper){return is(s,Kind.COT)||is(s,Kind.TABLE);}
    @Override public void setBedOccupied(BlockState s,Level level,BlockPos pos,LivingEntity sleeper,boolean occupied){}
    @Override public Direction getBedDirection(BlockState s,LevelReader level,BlockPos pos){return is(s,Kind.COT)||is(s,Kind.TABLE)?s.getValue(FACING).getOpposite():s.getValue(FACING);}
    /** The cell a thing's FACING points to: the foot of a cot or of the table. */
    public static BlockPos foot(BlockPos head,BlockState s){return head.relative(s.getValue(FACING));}
}
