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
 * 0.4.207, the God of the Pitt (docs/GOD_OF_THE_PITT_0_4_207.md): the deeper passages' limestone, the one fitting block, the
 * climbing block, Brad's three tools from the crate, and the sounds. Drawn by tools/draw_pitt_art.py; the sounds are made by
 * tools/generate_pitt_audio.py.
 */
public final class PittRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The draft through the passages, the rock taking the weight, a slab stepping down, the hammer on a seam, something in the fissure, a footstep on stone, the passages coming down, the barometer's needle. */
    public static final DeferredHolder<SoundEvent,SoundEvent> DRAFT=sound("draft"),GROAN=sound("groan"),GRIND=sound("grind"),CHISEL=sound("chisel"),
        SCRAPE=sound("scrape"),STEP=sound("step"),RUMBLE=sound("rumble"),TICK=sound("tick");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("pitt."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pitt."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** Wet slate limestone, ochre limestone, red cave clay and calcite flowstone. */
    public static final List<String> MATERIAL_NAMES=List.of("limestone","ochre","clay","calcite");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("pitt_"+name,fixed(name.equals("clay")?Blocks.MUD:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PittBlock>> FITTING_TYPE=CODECS.register("pitt_fitting",()->PittBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PittClimbBlock>> CLIMB_TYPE=CODECS.register("pitt_climb",()->PittClimbBlock.CODEC);
    /** Every fitting of the deeper passages, from the pinch-ceiling slabs over Floyd's Tomb to the field journal on its stone. */
    public static final DeferredBlock<PittBlock> FITTING=BLOCKS.registerBlock("pitt_fitting",PittBlock::new,fixed(Blocks.STONE).noOcclusion().lightLevel(PittBlock::light));
    /** Brad's fixed ropes and the rusted ladder (in the climbable tag). */
    public static final DeferredBlock<PittClimbBlock> CLIMB=BLOCKS.registerBlock("pitt_climb",PittClimbBlock::new,fixed(Blocks.LADDER).noOcclusion());
    /** Brad's heavy masonry hammer: held on a seam, it cuts the seam a little at a time, while the draft blows. */
    public static final DeferredItem<Item> HAMMER=ITEMS.registerSimpleItem("pitt_masonry_hammer",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    /** Brad's carbide lamp: in the hand, it lights the passages; in the still air, it is a reason for the ceiling to come down. */
    public static final DeferredItem<Item> LAMP=ITEMS.registerSimpleItem("pitt_carbide_lamp",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    /** Brad's brass aneroid barometer: carried, it shows the draft's needle (green, then shaking, then red). */
    public static final DeferredItem<Item> BAROMETER=ITEMS.registerSimpleItem("pitt_aneroid_barometer",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    private PittRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
