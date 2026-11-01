package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.202, the Whistlers in the Pines: the fire lookout's materials, its one fitting block, the two things a reader carries
 * (the diesel starter crank and the canvas repair tarps) and its sounds. Drawn by tools/draw_lookout_art.py; the sounds are
 * made by tools/generate_lookout_audio.py.
 */
public final class LookoutRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /**
     * The whistle (two falling notes), three knocks, a canvas blind rolling down, the glass cracking and breaking, the
     * teleprinter's chatter, the radio's squelch, the diesel catching, the crank's ratchet and a gust of the gale.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> WHISTLE=sound("whistle"),KNOCK=sound("knock"),SHUTTER=sound("shutter"),CRACK=sound("crack"),
        SHATTER=sound("shatter"),TTY=sound("tty"),RADIO=sound("radio"),GENERATOR=sound("generator"),CRANK=sound("crank"),GUST=sound("gust");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("lookout."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lookout."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The creosoted timber of the stilts and frame, the cab's worn floorboards, its painted panel walls, the shed's corrugated iron and the roofing. */
    public static final List<String> MATERIAL_NAMES=List.of("timber","boards","panel","iron","roof");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("lookout_"+name,fixed(name.equals("iron")?Blocks.IRON_BLOCK:Blocks.SPRUCE_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LookoutBlock>> FITTING_TYPE=CODECS.register("lookout_fitting",()->LookoutBlock.CODEC);
    /** Every fitting of the lookout: the windows and their blinds, the fire finder, the desk and its sets, the lamps, the generator. */
    public static final DeferredBlock<LookoutBlock> FITTING=BLOCKS.registerBlock("lookout_fitting",LookoutBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().lightLevel(LookoutBlock::light));
    /** The diesel generator's starter crank, from its hook by the hatch; and the canvas tarps that patch a torn frame. */
    public static final DeferredItem<Item> CRANK_ITEM=ITEMS.registerSimpleItem("lookout_crank",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> TARP_ITEM=ITEMS.registerSimpleItem("lookout_tarp",new Item.Properties().stacksTo(1));
    private LookoutRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
