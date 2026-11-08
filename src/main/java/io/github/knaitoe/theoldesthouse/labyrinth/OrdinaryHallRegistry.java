package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.390, the new ordinary halls of docs/HALLS_TRIALS_AND_TIME_DESIGN.md (OrdinaryHalls): their materials, the washing on the
 * laundry corridor's lines, and their fittings. Drawn by tools/draw_ordinary_hall_art.py.
 */
public final class OrdinaryHallRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The laundry corridor: limewashed brick, its foot tarred against the wet, quarry tiles and a boarded ceiling. */
    public static final List<String> MATERIAL_NAMES=List.of("laundry_wall","laundry_wall_low","laundry_floor","laundry_ceiling");
    private static Block like(String name){return switch(name){case "laundry_floor"->Blocks.BRICKS;case "laundry_ceiling"->Blocks.SPRUCE_PLANKS;default->Blocks.BRICKS;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("hall_"+name,fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean isMaterial(net.minecraft.world.level.block.state.BlockState state){for(var block:MATERIALS.values())if(state.is(block.get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HallSheetBlock>> SHEET_TYPE=CODECS.register("hall_sheet",()->HallSheetBlock.CODEC);
    /** Washing on a line (HallSheetBlock): no collision, light passes, it swings when walked through. */
    public static final DeferredBlock<HallSheetBlock> SHEET=BLOCKS.registerBlock("hall_sheet",HallSheetBlock::new,
        fixed(Blocks.WHITE_WOOL).noCollission().noOcclusion().sound(SoundType.WOOL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<OrdinaryHallBlock>> FITTING_TYPE=CODECS.register("hall_fitting",()->OrdinaryHallBlock.CODEC);
    public static final DeferredBlock<OrdinaryHallBlock> FITTING=BLOCKS.registerBlock("hall_fitting",OrdinaryHallBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion());
    private OrdinaryHallRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
