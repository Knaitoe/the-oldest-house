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
     * 0.4.700: the school field's turf, mown and darker, its yard lines and sidelines, the end zones and the cinder track.
     * 0.4.701: the pasture's buffalo grass and the trodden mud round its tank.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("caliche","asphalt","main_line","curb","sidewalk","soil","dirt",
        "lawn","playa","ballast","siding","wainscot","shingle","flat_roof","carpet","lino","drywall","paneling","ceiling","tile","store_carpet",
        "storefront","stucco","cinder","galvanized","rusted","slab","shop_metal","drive_lot",
        "turf","turf_dark","turf_line","turf_side","endzone","track_cinder","pasture","mud");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("list_"+name,fixed(switch(name){
        case "caliche","ballast","track_cinder"->Blocks.GRAVEL;case "soil","dirt","playa"->Blocks.DIRT;case "lawn","turf","turf_dark","turf_line","turf_side","endzone","pasture"->Blocks.GRASS_BLOCK;case "mud"->Blocks.MUD;
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
    /** 0.4.700: the field house's steel doors, which open by hand. */
    public static final DeferredBlock<DoorBlock> STEEL_DOOR=BLOCKS.register("list_steel_door",()->new DoorBlock(BlockSetType.COPPER,fixed(Blocks.IRON_DOOR).noOcclusion()));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TheListBlock>> FITTING_TYPE=CODECS.register("list_fitting",()->TheListBlock.CODEC);
    public static final DeferredBlock<TheListBlock> FITTING=BLOCKS.registerBlock("list_fitting",TheListBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(TheListBlock::light));
    /** 0.4.701: the pasture's windmill and pumpjacks, their still parts in the block and their turning parts drawn by its renderer. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TheListMachineBlock>> MACHINE_TYPE=CODECS.register("list_machine",()->TheListMachineBlock.CODEC);
    public static final DeferredBlock<TheListMachineBlock> MACHINE=BLOCKS.registerBlock("list_machine",TheListMachineBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<TheListMachineBlock.Turning>> MACHINE_ENTITY=BLOCK_ENTITIES.register("list_machine",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(TheListMachineBlock.Turning::new,MACHINE.get()).build(null));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The night's insects; a dog a few yards off; the drive-in's speakers; a loose sheet on the gin; a train out past the playa; the
     * wind; the tower settling. */
    public static final DeferredHolder<SoundEvent,SoundEvent> INSECTS=sound("the_list.insects"),DOG=sound("the_list.dog"),SPEAKER=sound("the_list.speaker"),
        TIN=sound("the_list.tin"),TRAIN=sound("the_list.train"),WIND=sound("the_list.wind"),TOWER=sound("the_list.tower");
    /** 0.4.700: a sprinkler working a lawn. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SPRINKLER=sound("the_list.sprinkler");
    /** 0.4.701: the windmill turning, a pumpjack's knock at the bottom of its stroke, the cattle guard underfoot. */
    public static final DeferredHolder<SoundEvent,SoundEvent> WINDMILL=sound("the_list.windmill"),PUMPJACK=sound("the_list.pumpjack"),GUARD=sound("the_list.guard");
    /**
     * 0.4.705, the slasher's body (docs/THE_LIST_0_4_705.md): a heart heard through a wall; a shoulder against a locked door; the door
     * giving; the feed sack pulled on or off; the slasher's tread, heard by everyone else; the body going down.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> HEART=sound("the_list.heart"),STRIKE=sound("the_list.strike"),SPLINTER=sound("the_list.splinter"),
        SACK_SOUND=sound("the_list.sack"),TREAD=sound("the_list.tread"),DOWN=sound("the_list.down");
    /** 0.4.706, night one: the radio at the stock tank, the seed house's door on its rail, its bolt, the auger and the seed. */
    public static final DeferredHolder<SoundEvent,SoundEvent> RADIO=sound("the_list.radio"),SLIDE=sound("the_list.slide"),BOLT=sound("the_list.bolt"),
        AUGER=sound("the_list.auger"),SEED=sound("the_list.seed");
    /** 0.4.708, the hunt: one of the names screaming as they start to run. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SCREAM=sound("the_list.scream");
    /** 0.4.709, the catch: one of them fighting the slasher's grip, the hay hook going in and torn out, and the catch in a throat. */
    public static final DeferredHolder<SoundEvent,SoundEvent> STRUGGLE=sound("the_list.struggle"),HOOK_IN=sound("the_list.hook_in"),
        HOOK_OUT=sound("the_list.hook_out"),GASP=sound("the_list.gasp");
    /** 0.4.711, the boards: a heel stamped down on a plank of the stands, and something giving under it. */
    public static final DeferredHolder<SoundEvent,SoundEvent> STAMP=sound("the_list.stamp"),CRACK=sound("the_list.crack");
    /** 0.4.713, the line: the valve's wheel turned, and a riser going through someone. */
    public static final DeferredHolder<SoundEvent,SoundEvent> VALVE=sound("the_list.valve"),RISER=sound("the_list.riser");
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheOldestHouse.MOD_ID);
    /** 0.4.709: blood thrown off the hay hook and dripping, drawn by the client (TheListBloodParticles). */
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> BLOOD=
        PARTICLES.register("list_blood",()->new net.minecraft.core.particles.SimpleParticleType(false));
    /** 0.4.705: a house door that has given, split at the lock, a hole punched through its lower panel; it opens and shuts by hand for anyone. */
    public static final DeferredBlock<DoorBlock> BROKEN_DOOR=BLOCKS.register("list_broken_door",()->new DoorBlock(BlockSetType.OAK,fixed(Blocks.OAK_DOOR).noOcclusion()));
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    /** 0.4.705: the feed sack over the slasher's head. */
    public static final DeferredItem<TheListSack> SACK=ITEMS.register("list_sack",()->new TheListSack(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.709: the hay hook, the slasher's; its model shows it bloodied once it has been used (custom model data 1). */
    public static final DeferredItem<net.minecraft.world.item.Item> HAY_HOOK=ITEMS.register("list_hay_hook",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    private TheListRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BLOCK_ENTITIES.register(bus);SOUNDS.register(bus);ITEMS.register(bus);PARTICLES.register(bus);}
}
