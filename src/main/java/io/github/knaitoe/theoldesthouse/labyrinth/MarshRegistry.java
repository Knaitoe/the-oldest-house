package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.348, The Supper (docs/MARSH_SUPPER_0_4_348.md): the Marsh farmhouse's materials and fittings, Corinne's car keys, the dinner
 * bell's clapper, and the supper's three sounds. Drawn by tools/draw_marsh_supper_art.py; heard from tools/generate_marsh_supper_audio.py.
 */
public final class MarshRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** The dining room's faded wallpaper, the farmhouse's worn boards, the kitchen's checked linoleum. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("wallpaper","boards","linoleum");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("marsh_"+name,fixed(name.equals("boards")?Blocks.SPRUCE_PLANKS:Blocks.BIRCH_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<MarshBlock>> FITTING_TYPE=CODECS.register("marsh_fitting",()->MarshBlock.CODEC);
    public static final DeferredBlock<MarshBlock> FITTING=BLOCKS.registerBlock("marsh_fitting",MarshBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(MarshBlock::light));
    /** Corinne's car keys on their fob, off the side by the butcher's block. They never leave the farm. */
    public static final DeferredItem<Item> KEYS=ITEMS.registerSimpleItem("marsh_keys",new Item.Properties().stacksTo(1));
    /** The Marsh dinner bell's clapper: the supper's keepsake. Carried, it leans the dealing toward the outside. */
    public static final DeferredItem<Item> CLAPPER=ITEMS.registerSimpleItem("marsh_clapper",new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The dinner bell in its yoke; the kitchen's swing door; boots on the coir mat. */
    public static final DeferredHolder<SoundEvent,SoundEvent> BELL=sound("marsh.bell"),SWING=sound("marsh.swing"),WIPE=sound("marsh.wipe");
    private MarshRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
