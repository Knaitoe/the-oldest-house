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
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md): the station's, the yard's, the road's and the field's materials, their fittings, the
 * farmhouse's shingle stairs and the cellar's stone steps, and their sounds. Drawn by tools/draw_fallow_art.py; heard from
 * tools/generate_fallow_audio.py.
 */
public final class FallowRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The store's painted boards, its block plinth, its floor, its ceiling and its roof; the forecourt's concrete; the road's asphalt, its
     * line and its gravel shoulders; the garage's corrugated walls and its stained floor; the trailer's ribbed sides, its roof and its
     * skirting; the yard's earth, the field's soil, the tracks' ruts and the pads' gravel; the cellar's stone, its floor and its roof
     * (the yard on top, timber beneath); the farmhouse's boards, its shingles, its porch and its chimney's brick; the rig's trailer.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("siding","plinth","lino","ceiling","roof","apron","asphalt","road_line",
        "shoulder","metal","garage_floor","trailer","trailer_roof","skirting","yard","soil","track","gravel","cellar_stone","cellar_floor",
        "cellar_roof","farm_siding","farm_shingle","porch","brick","box");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("fallow_"+name,fixed(switch(name){
        case "plinth","apron","asphalt","road_line","cellar_stone","brick","garage_floor"->Blocks.STONE;case "shoulder","gravel","track"->Blocks.GRAVEL;
        case "yard","soil","cellar_floor","cellar_roof"->Blocks.DIRT;case "metal","trailer","trailer_roof","box"->Blocks.IRON_BLOCK;
        case "lino","ceiling"->Blocks.SMOOTH_STONE;default->Blocks.OAK_PLANKS;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredBlock<StairBlock> SHINGLE_STAIRS=BLOCKS.register("fallow_shingle_stairs",()->new StairBlock(MATERIALS.get("farm_shingle").get().defaultBlockState(),fixed(Blocks.SPRUCE_STAIRS)));
    public static final DeferredBlock<StairBlock> CELLAR_STAIRS=BLOCKS.register("fallow_cellar_stairs",()->new StairBlock(MATERIALS.get("cellar_stone").get().defaultBlockState(),fixed(Blocks.STONE_STAIRS)));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<FallowBlock>> FITTING_TYPE=CODECS.register("fallow_fitting",()->FallowBlock.CODEC);
    public static final DeferredBlock<FallowBlock> FITTING=BLOCKS.registerBlock("fallow_fitting",FallowBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(FallowBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The wind over the wheat; the insects in it; the radio on the back shelf; the pump's bell. */
    public static final DeferredHolder<SoundEvent,SoundEvent> WIND=sound("fallow.wind"),INSECTS=sound("fallow.insects"),RADIO=sound("fallow.radio"),
        BELL=sound("fallow.bell");
    /** 0.4.673, the afternoon: the register ringing up a sale, a wrench working under the car, coins left on the counter. */
    public static final DeferredHolder<SoundEvent,SoundEvent> REGISTER=sound("fallow.register"),WRENCH=sound("fallow.wrench"),COINS=sound("fallow.coins");
    /** 0.4.674, the four minutes: the radio's tone, the lids grinding open, a launch, a siren far off, the light, the ground shaking. */
    public static final DeferredHolder<SoundEvent,SoundEvent> TONE=sound("fallow.tone"),LIDS=sound("fallow.lids"),LAUNCH=sound("fallow.launch"),
        SIREN=sound("fallow.siren"),BLAST=sound("fallow.blast"),RUMBLE=sound("fallow.rumble");
    /**
     * 0.4.673: what the afternoon's people carry, never a reader: Opal's bean in its cup (drawn from its block's model), the quart of milk
     * Faye buys, the bottle of pop the stranger buys and the cup of coffee Otis pours.
     */
    public static final DeferredItem<Item> BEAN_CUP=ITEMS.registerSimpleItem("fallow_bean_cup",new Item.Properties().stacksTo(1)),
        MILK=ITEMS.registerSimpleItem("fallow_milk",new Item.Properties().stacksTo(1)),SODA=ITEMS.registerSimpleItem("fallow_soda",new Item.Properties().stacksTo(1)),
        COFFEE=ITEMS.registerSimpleItem("fallow_coffee",new Item.Properties().stacksTo(1));
    private FallowRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);ITEMS.register(bus);}
}
