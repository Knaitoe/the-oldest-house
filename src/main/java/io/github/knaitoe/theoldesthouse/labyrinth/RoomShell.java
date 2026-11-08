package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.380, the texture pass's fourth interior release (docs/TEXTURE_PASS_ROOMS_0_4_380.md): the child's room and the hotel's boiler
 * room, both built whole, in their own surfaces. The child's room: honey floorboards, cream beadboard to the rail, the rail, a plaster
 * ceiling (one stretch in four with a child's pale stars stuck to it), the corner posts, and the cellar's whitewashed brick and cracked
 * floor. The boiler room: soot-dark brick, stained concrete (a drain in places), the porch floor's joists overhead (plain spruce from
 * above, where it is the porch's floor), and the service stair's worn stone. Drawn by tools/draw_room_shell_art.py. Each copies the
 * collision and sound of the vanilla block it stands in for and cannot be broken.
 */
public final class RoomShell {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static DeferredBlock<Block> cube(String id,Block like){return BLOCKS.registerSimpleBlock("room_shell_"+id,fixed(like));}

    public static final DeferredBlock<Block> NURSERY_FLOOR=cube("nursery_floor",Blocks.BIRCH_PLANKS),NURSERY_PANEL=cube("nursery_panel",Blocks.BIRCH_PLANKS),
        NURSERY_RAIL=cube("nursery_rail",Blocks.STRIPPED_BIRCH_WOOD),NURSERY_CEILING=cube("nursery_ceiling",Blocks.SMOOTH_QUARTZ),
        NURSERY_POST=cube("nursery_post",Blocks.STRIPPED_BIRCH_WOOD),CELLAR_BRICK=cube("cellar_brick",Blocks.BRICKS),
        CELLAR_FLOOR=cube("cellar_floor",Blocks.POLISHED_ANDESITE);
    public static final DeferredBlock<Block> BOILER_BRICK=cube("boiler_brick",Blocks.BRICKS),BOILER_FLOOR=cube("boiler_floor",Blocks.SMOOTH_STONE),
        BOILER_JOISTS=cube("boiler_joists",Blocks.SPRUCE_PLANKS),BOILER_STONE=cube("boiler_stone",Blocks.STONE_BRICKS);
    public static final DeferredBlock<StairBlock> BOILER_STAIR=BLOCKS.registerBlock("room_shell_boiler_stair",
        p->new StairBlock(Blocks.STONE_BRICKS.defaultBlockState(),p),fixed(Blocks.STONE_BRICK_STAIRS));

    /**
     * 0.4.381, the fifth interior release (docs/TEXTURE_PASS_CAMP_0_4_381.md): Holloway's hunt and the drowned congregation's chapel,
     * drawn by tools/draw_camp_chapel_art.py. Rough ashlar (plain, cracked or mossed by position), irregular flags, hewn timber, camp
     * boards, packed grit and slate for the camp's three rooms; a pew worn smooth on its seat and the causeway's dark boards.
     */
    public static final DeferredBlock<Block> CAMP_ASHLAR=cube("camp_ashlar",Blocks.STONE_BRICKS),CAMP_FLAGS=cube("camp_flags",Blocks.STONE),
        CAMP_TIMBER=cube("camp_timber",Blocks.STRIPPED_SPRUCE_WOOD),CAMP_BOARDS=cube("camp_boards",Blocks.SPRUCE_PLANKS),
        CAMP_GRIT=cube("camp_grit",Blocks.TUFF),CAMP_SLATE=cube("camp_slate",Blocks.DEEPSLATE_TILES),CAUSEWAY=cube("causeway",Blocks.DARK_OAK_PLANKS);
    public static final DeferredBlock<StairBlock> PEW=BLOCKS.registerBlock("room_shell_pew",
        p->new StairBlock(Blocks.DARK_OAK_PLANKS.defaultBlockState(),p),fixed(Blocks.DARK_OAK_STAIRS));

    /**
     * 0.4.382, the ninth release (docs/TEXTURE_PASS_TRAILER_0_4_382.md): Goatman's trailer, drawn by tools/draw_trailer_art.py. Its
     * walls are one block thick, so each wall block faces out: ribbed aluminium on the side it faces, the camper's veneer panelling
     * on the side within. The skirt course, the stripe and the long sides' drip rail are the same block in their own skins; the
     * corners are rounded trim; the roof is coated sheet over the camper's ceiling; the floor is linoleum laid as a checker.
     */
    public static final DeferredBlock<Skin> TRAILER_SIDING=skin("trailer_siding",Blocks.WHITE_TERRACOTTA),TRAILER_BAND=skin("trailer_band",Blocks.LIGHT_GRAY_CONCRETE),
        TRAILER_SKIRT=skin("trailer_skirt",Blocks.CYAN_TERRACOTTA),TRAILER_EAVE=skin("trailer_eave",Blocks.IRON_BLOCK);
    public static final DeferredBlock<Block> TRAILER_CORNER=cube("trailer_corner",Blocks.IRON_BLOCK),TRAILER_LINO=cube("trailer_lino",Blocks.WHITE_CONCRETE),
        TRAILER_LINO_DARK=cube("trailer_lino_dark",Blocks.LIGHT_GRAY_CONCRETE);
    public static final DeferredBlock<SlabBlock> TRAILER_ROOF=BLOCKS.registerBlock("room_shell_trailer_roof",SlabBlock::new,fixed(Blocks.SMOOTH_STONE_SLAB));

    /** A wall block of a skin one block thick: its outside faces the way it faces, its inside the other way. */
    public static final class Skin extends HorizontalDirectionalBlock {
        public static final MapCodec<Skin> CODEC=simpleCodec(Skin::new);
        public Skin(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
        @Override public MapCodec<Skin> codec(){return CODEC;}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    }
    private static DeferredBlock<Skin> skin(String id,Block like){return BLOCKS.registerBlock("room_shell_"+id,Skin::new,fixed(like));}

    private RoomShell(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}

    /** The child's room's own wall at a height above the floor: panelling to the rail, the rail, then the paper. */
    public static BlockState nurseryWall(int y){return (y<=1?NURSERY_PANEL.get():y==2?NURSERY_RAIL.get():LiteraryRegistry.CHILD_WALLPAPER.get()).defaultBlockState();}
    /** Whether a block is one of the child's room's own surfaces. */
    public static boolean nursery(BlockState s){
        return s.is(NURSERY_FLOOR.get())||s.is(NURSERY_PANEL.get())||s.is(NURSERY_RAIL.get())||s.is(NURSERY_CEILING.get())||s.is(NURSERY_POST.get())
            ||s.is(CELLAR_BRICK.get())||s.is(CELLAR_FLOOR.get())||s.is(LiteraryRegistry.CHILD_WALLPAPER.get());
    }
    /** Whether a block is one of the camp's own surfaces. */
    public static boolean camp(BlockState s){
        return s.is(CAMP_ASHLAR.get())||s.is(CAMP_FLAGS.get())||s.is(CAMP_TIMBER.get())||s.is(CAMP_BOARDS.get())||s.is(CAMP_GRIT.get())||s.is(CAMP_SLATE.get());
    }
    /** Whether a block is one of the trailer's own surfaces. */
    public static boolean trailer(BlockState s){
        return s.is(TRAILER_SIDING.get())||s.is(TRAILER_BAND.get())||s.is(TRAILER_SKIRT.get())||s.is(TRAILER_EAVE.get())||s.is(TRAILER_CORNER.get())
            ||s.is(TRAILER_ROOF.get())||s.is(TRAILER_LINO.get())||s.is(TRAILER_LINO_DARK.get());
    }
    /** Whether a block is one of the boiler room's own surfaces. */
    public static boolean boiler(BlockState s){
        return s.is(BOILER_BRICK.get())||s.is(BOILER_FLOOR.get())||s.is(BOILER_JOISTS.get())||s.is(BOILER_STONE.get())||s.is(BOILER_STAIR.get());
    }
}
