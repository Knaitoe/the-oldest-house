package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.377, the texture pass's first interior release (docs/TEXTURE_PASS_PROOFROCK_0_4_377.md): Proofrock's own streets and walls.
 * Asphalt and the same patched, the painted centre line (turned with its street), concrete sidewalk slabs and the same cracked, a
 * granite kerb, lap clapboard in three paints and painted cinder block. Drawn by tools/draw_proofrock_art.py. Each keeps the
 * collision and sound of the vanilla block it stands in for and, like the rest of the House, cannot be broken.
 */
public final class ProofrockSurface {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> ALL=new LinkedHashMap<>();
    private static DeferredBlock<Block> simple(String name,Block like){var block=BLOCKS.registerSimpleBlock("proofrock_"+name,fixed(like));ALL.put(name,block);return block;}

    public static final DeferredBlock<Block> ASPHALT=simple("asphalt",Blocks.GRAY_CONCRETE),PATCHED=simple("asphalt_patched",Blocks.GRAY_CONCRETE);
    public static final DeferredBlock<RotatedPillarBlock> ROAD_LINE=BLOCKS.registerBlock("proofrock_road_line",RotatedPillarBlock::new,fixed(Blocks.YELLOW_CONCRETE));
    public static final DeferredBlock<Block> SIDEWALK=simple("sidewalk",Blocks.STONE_BRICKS),SIDEWALK_CRACKED=simple("sidewalk_cracked",Blocks.STONE_BRICKS),
        KERB=simple("kerb",Blocks.POLISHED_ANDESITE);
    public static final DeferredBlock<Block> CLAPBOARD=simple("clapboard",Blocks.OAK_PLANKS),CLAPBOARD_PAINTED=simple("clapboard_painted",Blocks.OAK_PLANKS),
        CLAPBOARD_WEATHERED=simple("clapboard_weathered",Blocks.OAK_PLANKS),CINDER_BLOCK=simple("cinder_block",Blocks.WHITE_TERRACOTTA);

    /** The centre line, running down a street that runs along this axis. */
    public static BlockState line(Direction.Axis along){return ROAD_LINE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,along);}
    /** Any of the town's own surfaces. */
    public static boolean is(BlockState s){if(s.is(ROAD_LINE.get()))return true;for(var b:ALL.values())if(s.is(b.get()))return true;return false;}
    /** The road surfaces: asphalt, patched or lined. */
    public static boolean road(BlockState s){return s.is(ASPHALT.get())||s.is(PATCHED.get())||s.is(ROAD_LINE.get());}
    private ProofrockSurface(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
}
