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
 * 0.4.410, the Kept: the machine's things, drawn facing north by tools/draw_kept_art.py.
 * <ul>
 * <li>CABINET: a tall machine cabinet, its reels and its rows of small lamps; stage 1 sealed in rime. SCREEN: a cathode screen set into the
 * cabinets; stage 0 dark, stage 1 awake and crawling with lines.</li>
 * <li>CABLE: a run of cable along the floor. TUBE: a strip lamp under the ceiling. ICICLE: ice hanging from the cave's roof.</li>
 * <li>PRINTER: the printer on its stand, its paper run out on the floor (the source). TRAY: the tray it prints into; stage 0 empty, stage 1
 * with a sheet in it (sent to a reader whose account is ready).</li>
 * <li>WIRE: wire grown into a thicket, too dense to push through; stage 1 with small lamps lit along it.</li>
 * <li>TABLE: a length of the long table, a place laid on it for nobody. CHAIR: one of the hundred chairs; it faces where it is turned.
 * CRATE: the crate of tins at the end of the walk. CHANDELIER: a chandelier made of cable and bare bulbs.</li>
 * </ul>
 */
public final class KeptBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<KeptBlock> CODEC=simpleCodec(KeptBlock::new);
    public enum Kind implements StringRepresentable {
        CABINET,SCREEN,CABLE,TUBE,ICICLE,PRINTER,TRAY,WIRE,TABLE,CHAIR,CRATE,CHANDELIER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public KeptBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CABINET).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return KeptRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(1,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(KeptRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: the strip lamps and the chandeliers, a screen that is awake, the cabinets' small lamps, the wire's. */
    static int light(BlockState s){
        return switch(s.getValue(KIND)){
            case TUBE->11;case CHANDELIER->12;case SCREEN->s.getValue(STAGE)==1?8:1;case CABINET->s.getValue(STAGE)==1?2:5;case WIRE->s.getValue(STAGE)==1?4:0;case PRINTER->3;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CABINET,SCREEN,WIRE,CRATE->Shapes.block();
            case CABLE->Block.box(0,0,0,16,2,16);
            case TUBE->Block.box(1,13,6,15,16,10);
            case ICICLE->Block.box(5,4,5,11,16,11);
            case PRINTER->MarshBlock.turned(f,2,0,3,14,13,13);
            case TRAY->MarshBlock.turned(f,3,0,3,13,9,13);
            case TABLE->Block.box(0,0,0,16,13,16);
            case CHAIR->Block.box(3,0,3,13,16,13);
            case CHANDELIER->Block.box(3,4,3,13,16,13);
        };
    }
    /** The cabinets, the screens, the wire, the crate and the table stand in the way; the chairs are pushed past; the rest is reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case CABINET,SCREEN,WIRE,CRATE->Shapes.block();
            case TABLE->Block.box(0,0,0,16,13,16);
            case PRINTER->Block.box(2,0,3,14,13,13);
            case CHAIR->Block.box(3,0,3,13,9,13);
            default->Shapes.empty();
        };
    }
}
