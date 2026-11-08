package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.385, the trial rooms' fittings (TrialRooms, docs/HALLS_TRIALS_AND_TIME_DESIGN.md), drawn by tools/draw_trial_art.py.
 *
 * <p>The still light's dusty boards, which hold in the light and give way under a reader in the dark (stage 1: gone, nothing to
 * stand on), and its caged lamps (stage 1: lit).
 */
public final class TrialBlock extends Block {
    public static final MapCodec<TrialBlock> CODEC=simpleCodec(TrialBlock::new);
    public enum Kind implements StringRepresentable {
        STILL_BOARD,STILL_LAMP;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public TrialBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.STILL_BOARD).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,STAGE);}
    public static BlockState of(Kind kind){return TrialRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind);}
    public static BlockState of(Kind kind,int stage){return of(kind).setValue(STAGE,stage);}
    public static boolean is(BlockState state,Kind kind){return state.is(TrialRegistry.FITTING.get())&&state.getValue(KIND)==kind;}
    public static boolean is(BlockState state,Kind kind,int stage){return is(state,kind)&&state.getValue(STAGE)==stage;}
    public static int light(BlockState state){return state.getValue(KIND)==Kind.STILL_LAMP&&state.getValue(STAGE)==1?15:0;}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case STILL_BOARD->s.getValue(STAGE)==1?Shapes.empty():Shapes.block();
        case STILL_LAMP->Shapes.or(box(7,12,7,9,16,9),box(5,5,5,11,12,11));
    };}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
}
