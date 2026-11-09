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
 * 0.4.408, The House on the Corner: the street's and the house's own things, drawn facing north by tools/draw_corner_art.py.
 * <ul>
 * <li>GATE: one cell of the tall iron gate in the front fence. Stage bit 0 open, bit 1 the upper cell; shut, it stops a body.</li>
 * <li>JAR: a board on the front room's wall with a glass jar on it. Stage 0 the bare board; 1 a jar of small teeth; 2 a jar of clipped
 * fingernails; 3 a jar of something pale folded on itself.</li>
 * <li>LAMP: a wall lamp with a frosted shade; stage 1 lit, though the power has been off for years.</li>
 * <li>WINDOW: one of the house's tall windows behind its shutters; stage 0 shut and dark, stage 1 lit through the slats.</li>
 * <li>KIOSK: the newspaper rack at the kiosk. DOG: the dog asleep on the pavement.</li>
 * <li>NOTEBOOK: a school exercise book left on the kerb (the source). NOTE: the kerb where the children sat; stage 0 a pebble, stage
 * 1 a folded paper under it (sent to a reader whose account is ready).</li>
 * <li>PETALS: jacaranda petals fallen on the pavement and in the gutter. BLOSSOM: the jacarandas' flowering crowns.</li>
 * </ul>
 */
public final class CornerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CornerBlock> CODEC=simpleCodec(CornerBlock::new);
    public enum Kind implements StringRepresentable {
        GATE,JAR,LAMP,WINDOW,KIOSK,DOG,NOTEBOOK,NOTE,PETALS,BLOSSOM;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public CornerBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.JAR).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return CornerRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(CornerRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a lit lamp, and a lit window less. */
    static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)==1?13:s.getValue(KIND)==Kind.WINDOW&&s.getValue(STAGE)==1?7:0;}
    public static boolean open(BlockState s){return is(s,Kind.GATE)&&(s.getValue(STAGE)&1)==1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case GATE->(s.getValue(STAGE)&1)==1?MarshBlock.turned(f,0,0,0,2,16,16):MarshBlock.turned(f,0,0,7,16,16,9);
            case JAR->MarshBlock.turned(f,0,3,8,16,13,16);
            case LAMP->MarshBlock.turned(f,5,6,11,11,13,16);
            case WINDOW,BLOSSOM->Shapes.block();
            case KIOSK->Block.box(1,0,3,15,14,13);
            case DOG->Block.box(2,0,3,14,6,13);
            case NOTEBOOK->Block.box(4,0,4,12,1,12);
            case NOTE->Block.box(5,0,5,11,2,11);
            case PETALS->Block.box(0,0,0,16,1,16);
        };
    }
    /** A shut gate, the windows, the crowns, the kiosk and the dog stand in the way; the rest is reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case GATE->(s.getValue(STAGE)&1)==1?Shapes.empty():MarshBlock.turned(f,0,0,7,16,24,9);
            case WINDOW,BLOSSOM->Shapes.block();
            case KIOSK->Block.box(1,0,3,15,14,13);
            case DOG->Block.box(2,0,3,14,6,13);
            default->Shapes.empty();
        };
    }
}
