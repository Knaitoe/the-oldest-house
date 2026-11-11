package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.450 (the owner: "Custom booths in last order"): the joinery of the booths in Last orders. A partition between two booths is a
 * panelled lower course ({@link Kind#PANEL}) under a screen of reeded glass with a brass rail along its top ({@link Kind#SCREEN}),
 * and ends at the aisle in a post with the booth's number on a brass plate ({@link Kind#POST}; number 0 is a bare post). A
 * partition's facing is the way its aisle end looks. {@link Kind#CANDLE} is the candle in red glass on a booth's table, placed in
 * the block over the table; {@link Kind#PICTURE} is the picture on a booth's wall, its facing the way it looks, its number which
 * picture. Drawn by tools/draw_bar_art.py.
 */
public final class BarBoothBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BarBoothBlock> CODEC=simpleCodec(BarBoothBlock::new);
    public enum Kind implements StringRepresentable {
        PANEL,SCREEN,POST,CANDLE,PICTURE;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty NUMBER=IntegerProperty.create("number",0,5);
    /** How brightly the candle burns. */
    public static final int CANDLE_LIGHT=6;
    public BarBoothBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PANEL).setValue(NUMBER,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,NUMBER);}
    public static boolean is(BlockState s){return s.is(BarRegistry.BOOTH.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    public static BlockState of(Kind kind,Direction facing,int number){return BarRegistry.BOOTH.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(NUMBER,number);}
    static int light(BlockState s){return s.getValue(KIND)==Kind.CANDLE?CANDLE_LIGHT:0;}

    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case PANEL->Shapes.block();
            case SCREEN->MarshBlock.turned(f,5,0,0,11,16,16);
            case POST->Shapes.or(MarshBlock.turned(f,3,0,0,13,16,10),MarshBlock.turned(f,5,0,10,11,16,16));
            case CANDLE->Block.box(1,0,6,5,1,10);
            case PICTURE->MarshBlock.turned(f,1,2,14,15,14,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case CANDLE,PICTURE->Shapes.empty();default->getShape(s,l,p,c);};
    }
}
