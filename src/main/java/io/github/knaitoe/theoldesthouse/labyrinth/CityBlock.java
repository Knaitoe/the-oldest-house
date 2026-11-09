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
 * 0.4.409, the city on the ice: the camp's things and the tunnel's, drawn facing north by tools/draw_city_art.py.
 * <ul>
 * <li>RADIO: the radio set on the crates in the hut, its dials lit. LOGBOOK: the operator's log beside it (the source). PAD: the message
 * pad on the other side; stage 0 blank, stage 1 a page written on (sent to a reader whose account is ready).</li>
 * <li>LAMP: a hurricane lamp hung from a hook; stage 1 lit. In the tunnel they go out as what fills it passes.</li>
 * <li>CRATE: a supply crate, stacked for tables and walls. STOVE: the hut's oil stove.</li>
 * <li>HARNESS: an empty dog harness on the snow, its buckles still done up. CORES: rock cores in their labelled box.</li>
 * <li>TABLE: the dissecting table under the tarp, its straps cut. FABRIC: the aeroplane's doped fabric, opened in long cuts. PROP: the
 * aeroplane's wooden propeller.</li>
 * <li>PRINTS: prints in the grit of the tunnel floor, three-toed, of birds nobody sees.</li>
 * </ul>
 */
public final class CityBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CityBlock> CODEC=simpleCodec(CityBlock::new);
    public enum Kind implements StringRepresentable {
        RADIO,LOGBOOK,PAD,LAMP,CRATE,STOVE,HARNESS,CORES,TABLE,FABRIC,PROP,PRINTS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public CityBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CRATE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return CityRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(1,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(CityRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a lit lamp, and the radio's dials a little. */
    static int light(BlockState s){return s.getValue(KIND)==Kind.LAMP&&s.getValue(STAGE)==1?13:s.getValue(KIND)==Kind.RADIO?4:s.getValue(KIND)==Kind.STOVE?6:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case RADIO->MarshBlock.turned(f,2,0,5,14,9,13);
            case LOGBOOK->Block.box(4,0,4,12,2,12);
            case PAD->Block.box(4,0,5,12,1,11);
            case LAMP->Block.box(5,3,5,11,16,11);
            case CRATE,FABRIC->Shapes.block();
            case STOVE->Block.box(3,0,3,13,12,13);
            case HARNESS->Block.box(2,0,2,14,2,14);
            case CORES->Block.box(1,0,3,15,6,13);
            case TABLE->Block.box(0,0,0,16,12,16);
            case PROP->MarshBlock.turned(f,0,0,6,16,16,10);
            case PRINTS->Block.box(0,0,0,16,1,16);
        };
    }
    /** Crates, the stove, the table and the aeroplane's fabric stand in the way; the rest is reached past. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case CRATE,FABRIC->Shapes.block();
            case STOVE->Block.box(3,0,3,13,12,13);
            case TABLE->Block.box(0,0,0,16,12,16);
            case RADIO->Block.box(2,0,5,14,9,13);
            default->Shapes.empty();
        };
    }
}
