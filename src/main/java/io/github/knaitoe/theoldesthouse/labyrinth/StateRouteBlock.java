package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.333, State Route 9: the highway's own things, drawn facing north by tools/draw_state_route_art.py.
 * <ul>
 * <li>MAILBOX: the galvanized rural mailbox on its post; stage 0 shut with the red flag up, stage 1 open with the flag down.</li>
 * <li>SHIELD: the route sign, its post below; stage 0 the old painted steel shield, stage 1 the retroreflective aluminum panel. Each
 * reader is sent the one the House read them for.</li>
 * <li>MILE: the reflective mile-marker post beside the mailbox, painted (0) or sheeted (1), likewise.</li>
 * <li>LAMP: the sodium head over the far shoulder; stage 1 dark. POLE: its pole and arm.</li>
 * <li>CHAIN: a panel of the chain-link fence along the starting shoulder; stage 1 the upper panel with its top rail.</li>
 * <li>POST: a bent guardrail post in the median with its amber reflector; stage 1 the reflector gone.</li>
 * <li>CORN: wet corn, two high: stage 0 the stalk, stage 1 the tassel.</li>
 * </ul>
 */
public final class StateRouteBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StateRouteBlock> CODEC=simpleCodec(StateRouteBlock::new);
    public enum Kind implements StringRepresentable {
        MAILBOX,SHIELD,MILE,LAMP,POLE,CHAIN,POST,CORN;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public StateRouteBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.MAILBOX).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return StateRouteRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,stage);}
    public static boolean is(BlockState s,Kind kind){return s.is(StateRouteRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)==0?15:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case MAILBOX->Block.box(3,0,2,13,14,14);
            case SHIELD,MILE,POLE,POST->Block.box(6,0,6,10,16,10);
            case LAMP->Block.box(3,10,3,13,16,13);
            case CHAIN->switch(s.getValue(FACING)){case EAST,WEST->Block.box(7,0,0,9,16,16);default->Block.box(0,0,7,16,16,9);};
            case CORN->Block.box(2,0,2,14,16,14);
        };
    }
    /** The fence and the corn stop a body; corn is a wall of stalks you cannot push through. The lamp hangs clear overhead. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.LAMP?Shapes.empty():s.getValue(KIND)==Kind.CORN?Shapes.block():getShape(s,l,p,c);}
}
