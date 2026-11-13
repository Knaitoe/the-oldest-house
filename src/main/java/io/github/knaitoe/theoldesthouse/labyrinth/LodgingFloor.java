package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 0.4.549, The Ones Already Inside: a cell of the lodging house's floors, the floor of the room above it on top and the ceiling of the
 * room below underneath. Ground floor over the cellar: the hall's tiles, boards, the back passage's boards worn in a path, the
 * scullery's flags, Daniel's boards, the passages' bare boards, each over joists. Upstairs over the ground floor: the gallery's
 * runner (its west edge, its middle, its east edge), the rooms' boards, the workroom's stained boards, Vera's carpet and the upstairs
 * passages' boards, each over a ceiling (rough lath under the passages). The roof's cells over the rooms and over the passages.
 * Drawn by tools/draw_lodging_house.py.
 */
public final class LodgingFloor extends Block {
    public static final MapCodec<LodgingFloor> CODEC=simpleCodec(LodgingFloor::new);
    public enum Pair implements StringRepresentable {
        TILE,BOARDS,WORN,FLAGS,SCUFFED,PASSAGE_LOW,RUNNER_W,RUNNER,RUNNER_E,ROOM,WORK,CARPET,PASSAGE,TOP,TOP_PASSAGE;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Pair> PAIR=EnumProperty.create("pair",Pair.class);
    public LodgingFloor(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PAIR,Pair.BOARDS));}
    public static BlockState of(Pair pair){return LodgingRegistry.FLOOR.get().defaultBlockState().setValue(PAIR,pair);}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PAIR);}
}
