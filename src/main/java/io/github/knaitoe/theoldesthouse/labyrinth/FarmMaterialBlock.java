package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** The barn well's farm (0.4.164): the barn's red board-and-batten, its slid-open doors, white corner boards and four-pane windows, and the shed's grey boards. Drawn by tools/draw_barn_well.py. */
public final class FarmMaterialBlock extends Block {
    public static final MapCodec<FarmMaterialBlock> CODEC=simpleCodec(FarmMaterialBlock::new);
    public enum Kind implements StringRepresentable {
        SIDING,DOOR,TRIM,WINDOW,SHED_BOARDS;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public FarmMaterialBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SIDING));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    public static BlockState of(Kind k){return NovelRegistry.FARM_MATERIAL.get().defaultBlockState().setValue(KIND,k);}
    public static boolean is(BlockState s,Kind k){return s.is(NovelRegistry.FARM_MATERIAL.get())&&s.getValue(KIND)==k;}
}
