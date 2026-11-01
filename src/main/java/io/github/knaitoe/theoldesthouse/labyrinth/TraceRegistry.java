package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.231, traces and the door wedge (docs/TRACES_0_4_231.md): the shut-up rooms' grey boards, faded paper, dark panelling and
 * stained plaster; the one fitting block (dust, the loose chair, the table, the dust sheets, the hook and the closer); and the wedge.
 * Drawn by tools/draw_trace_fittings.py.
 */
public final class TraceRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("boards","paper","panel","plaster");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("trace_"+name,fixed(name.equals("plaster")?Blocks.STONE:Blocks.SPRUCE_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TraceBlock>> FITTING_TYPE=CODECS.register("trace_fitting",()->TraceBlock.CODEC);
    public static final DeferredBlock<TraceBlock> FITTING=BLOCKS.registerBlock("trace_fitting",TraceBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().sound(net.minecraft.world.level.block.SoundType.WOOD));
    /** A wooden door wedge: one to a reader, from a hook in a cloakroom. It holds a door where it is left (Wedges). */
    public static final DeferredItem<Item> WEDGE=ITEMS.registerSimpleItem("door_wedge",new Item.Properties().stacksTo(1));
    private TraceRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);}
}
