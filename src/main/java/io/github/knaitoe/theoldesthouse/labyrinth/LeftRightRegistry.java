package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.205, the Left-Right Game (docs/LEFT_RIGHT_0_4_205.md): the road's materials, its one fitting block, the road flare and
 * the brass conduit key, and the sounds. Drawn by tools/draw_left_right_art.py; the sounds are made by
 * tools/generate_left_right_audio.py.
 */
public final class LeftRightRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The CB's squelch and burble, the payphone's ring, a lit window's hum, her lunge, a flare struck, the wagon idling and the chain-link gate. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CB=sound("cb"),RING=sound("ring"),HUM=sound("hum"),LUNGE=sound("lunge"),FLARE_SOUND=sound("flare"),
        WAGON_SOUND=sound("wagon"),GATE_SOUND=sound("gate");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("left_right."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"left_right."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** Wet asphalt, its double yellow and white edge lines, cracked salt, brutalist concrete, four pastel stuccos and roof shingle. */
    public static final List<String> MATERIAL_NAMES=List.of("asphalt","asphalt_line","asphalt_edge","salt","concrete","pastel_pink","pastel_mint","pastel_blue","pastel_butter","shingle");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("left_right_"+name,fixed(name.startsWith("asphalt")||name.equals("concrete")?Blocks.STONE:name.equals("salt")?Blocks.CALCITE:Blocks.TERRACOTTA)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LeftRightBlock>> FITTING_TYPE=CODECS.register("left_right_fitting",()->LeftRightBlock.CODEC);
    /** Every fitting of the road, from the teletype in the tollbooth to the tailgate of the wagon. */
    public static final DeferredBlock<LeftRightBlock> FITTING=BLOCKS.registerBlock("left_right_fitting",LeftRightBlock::new,fixed(Blocks.STONE).noOcclusion().lightLevel(LeftRightBlock::light));
    /** A road flare from the crate in the tollbooth; Apollo's last one, kept, lights again and again for its owner. */
    public static final DeferredItem<LeftRightFlareItem> FLARE=ITEMS.registerItem("left_right_flare",LeftRightFlareItem::new,new Item.Properties().stacksTo(3));
    /** The brass conduit key from the flooding phone box at the Bluejay cut. */
    public static final DeferredItem<Item> KEY=ITEMS.registerSimpleItem("left_right_key",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    private LeftRightRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
