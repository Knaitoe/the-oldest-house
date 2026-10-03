package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Real wallpaper blocks; the revealed plaster and moving figure remain native meshes. */
public final class WallpaperPanelBlock extends Block {
    public static final MapCodec<WallpaperPanelBlock> CODEC=simpleCodec(WallpaperPanelBlock::new);
    public static final BooleanProperty FIGURE=BooleanProperty.create("figure"),PEELED=BooleanProperty.create("peeled");
    public WallpaperPanelBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FIGURE,false).setValue(PEELED,false));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FIGURE,PEELED);}
}
