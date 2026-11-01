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
 * 0.4.218, the replacement (docs/REPLACEMENT_0_4_218.md): the street's asphalt and its houses' siding in three paints, the one open
 * house's siding gone to stains, and its one fitting block. Drawn by tools/draw_replacement_art.py. Its noises are vanilla's.
 */
public final class ReplacementRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("asphalt","siding_a","siding_b","siding_c","siding_stained","carpet_stained");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("replacement_"+name,fixed(name.equals("asphalt")?Blocks.STONE:name.startsWith("carpet")?Blocks.BROWN_WOOL:Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<ReplacementBlock>> FITTING_TYPE=CODECS.register("replacement_fitting",()->ReplacementBlock.CODEC);
    public static final DeferredBlock<ReplacementBlock> FITTING=BLOCKS.registerBlock("replacement_fitting",ReplacementBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(ReplacementBlock::light));
    private ReplacementRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
