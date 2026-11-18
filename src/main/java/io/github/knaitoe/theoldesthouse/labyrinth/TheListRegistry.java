package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.699, The list (docs/THE_LIST_0_4_699.md): Lamesa's materials, its fittings, its roofs' stairs, its two doors and its sounds.
 * Drawn by tools/draw_the_list_art.py; heard from tools/generate_the_list_audio.py.
 */
public final class TheListRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The streets' caliche, Main Street's asphalt, its painted line, its curbs and its sidewalks; the fields' soil, the lots' dirt, the
     * yards' grass, the playa's crust and the tracks' ballast; the houses' siding, their brick wainscot, their shingles, the stores' flat
     * roofs; inside, carpet, linoleum, painted wallboard, paneling and the ceilings; the stores' tile, the video store's carpet, the
     * fronts' face brick, stucco and block; the gin's galvanized and rusted iron and its slab; the shop's painted steel; the drive-in's lot.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("caliche","asphalt","main_line","curb","sidewalk","soil","dirt",
        "lawn","playa","ballast","siding","wainscot","shingle","flat_roof","carpet","lino","drywall","paneling","ceiling","tile","store_carpet",
        "storefront","stucco","cinder","galvanized","rusted","slab","shop_metal","drive_lot");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("list_"+name,fixed(switch(name){
        case "caliche","ballast"->Blocks.GRAVEL;case "soil","dirt","playa"->Blocks.DIRT;case "lawn"->Blocks.GRASS_BLOCK;
        case "carpet","store_carpet"->Blocks.WHITE_WOOL;case "siding","paneling","shingle"->Blocks.OAK_PLANKS;
        case "galvanized","rusted","shop_metal"->Blocks.IRON_BLOCK;default->Blocks.STONE;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredBlock<StairBlock> SHINGLE_STAIRS=BLOCKS.register("list_shingle_stairs",()->new StairBlock(MATERIALS.get("shingle").get().defaultBlockState(),fixed(Blocks.SPRUCE_STAIRS)));
    /** The houses' low roofs, half a block up for every block in. */
    public static final DeferredBlock<SlabBlock> SHINGLE_SLAB=BLOCKS.register("list_shingle_slab",()->new SlabBlock(fixed(Blocks.SPRUCE_SLAB)));
    public static final DeferredBlock<StairBlock> METAL_STAIRS=BLOCKS.register("list_metal_stairs",()->new StairBlock(MATERIALS.get("galvanized").get().defaultBlockState(),fixed(Blocks.STONE_STAIRS)));
    /** The houses' panel doors, inside and out; the stores' aluminum doors with their glass. */
    public static final DeferredBlock<DoorBlock> HOUSE_DOOR=BLOCKS.register("list_house_door",()->new DoorBlock(BlockSetType.OAK,fixed(Blocks.OAK_DOOR).noOcclusion()));
    public static final DeferredBlock<DoorBlock> GLASS_DOOR=BLOCKS.register("list_glass_door",()->new DoorBlock(BlockSetType.COPPER,fixed(Blocks.COPPER_DOOR).noOcclusion()));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TheListBlock>> FITTING_TYPE=CODECS.register("list_fitting",()->TheListBlock.CODEC);
    public static final DeferredBlock<TheListBlock> FITTING=BLOCKS.registerBlock("list_fitting",TheListBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(TheListBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The night's insects; a dog a few yards off; the drive-in's speakers; a loose sheet on the gin; a train out past the playa; the
     * wind; the tower settling. */
    public static final DeferredHolder<SoundEvent,SoundEvent> INSECTS=sound("the_list.insects"),DOG=sound("the_list.dog"),SPEAKER=sound("the_list.speaker"),
        TIN=sound("the_list.tin"),TRAIN=sound("the_list.train"),WIND=sound("the_list.wind"),TOWER=sound("the_list.tower");
    private TheListRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
