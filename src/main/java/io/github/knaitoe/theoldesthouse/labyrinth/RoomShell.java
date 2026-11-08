package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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

    private RoomShell(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}

    /** The child's room's own wall at a height above the floor: panelling to the rail, the rail, then the paper. */
    public static BlockState nurseryWall(int y){return (y<=1?NURSERY_PANEL.get():y==2?NURSERY_RAIL.get():LiteraryRegistry.CHILD_WALLPAPER.get()).defaultBlockState();}
    /** Whether a block is one of the child's room's own surfaces. */
    public static boolean nursery(BlockState s){
        return s.is(NURSERY_FLOOR.get())||s.is(NURSERY_PANEL.get())||s.is(NURSERY_RAIL.get())||s.is(NURSERY_CEILING.get())||s.is(NURSERY_POST.get())
            ||s.is(CELLAR_BRICK.get())||s.is(CELLAR_FLOOR.get())||s.is(LiteraryRegistry.CHILD_WALLPAPER.get());
    }
    /** Whether a block is one of the boiler room's own surfaces. */
    public static boolean boiler(BlockState s){
        return s.is(BOILER_BRICK.get())||s.is(BOILER_FLOOR.get())||s.is(BOILER_JOISTS.get())||s.is(BOILER_STONE.get())||s.is(BOILER_STAIR.get());
    }
}
