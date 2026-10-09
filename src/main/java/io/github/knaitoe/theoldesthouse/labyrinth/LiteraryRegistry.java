package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
/** Furniture, real cast and finite keepsakes of the remaining authored stories. */
public final class LiteraryRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheOldestHouse.MOD_ID);
    /** A reader's own storm at the cabin (0.4.52): rain streaks and wind-torn leaves, drawn by their client only. */
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> CABIN_RAIN=PARTICLES.register("cabin_rain",()->new net.minecraft.core.particles.SimpleParticleType(false)),
        CABIN_LEAF=PARTICLES.register("cabin_leaf",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryPropBlock>> PROP_TYPE=CODECS.register("literary_prop",()->LiteraryPropBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryFrozenBlock>> FROZEN_TYPE=CODECS.register("literary_frozen",()->LiteraryFrozenBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<YachtGlazingBlock>> GLAZING_TYPE=CODECS.register("yacht_glazing",()->YachtGlazingBlock.CODEC);
    public static final DeferredBlock<LiteraryFrozenBlock> FROZEN=BLOCKS.registerBlock("literary_frozen",LiteraryFrozenBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).dynamicShape().noOcclusion().noLootTable());
    public static final DeferredBlock<LiteraryPropBlock> PROP=BLOCKS.registerBlock("literary_prop",LiteraryPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LiteraryModelBlockEntity>> MODEL=BES.register("literary_model",()->BlockEntityType.Builder.of(LiteraryModelBlockEntity::new,PROP.get(),FROZEN.get()).build(null));
    public static final DeferredBlock<Block> VAULT_CRACK=material("usher_crack",Blocks.DEEPSLATE_BRICKS),RED_FLOOR=material("crimson_floor",Blocks.RED_TERRACOTTA),TAPE=material("elk_tape",Blocks.GRAY_WOOL),PEELED_TAPE=material("elk_tape_peeled",Blocks.GRAY_WOOL),DRAG_SNOW=material("father_drag_snow",Blocks.SNOW_BLOCK),CARVED_TRUNK=material("rabbit_carved_trunk",Blocks.OAK_PLANKS),SIDING=material("manufactured_siding",Blocks.OAK_PLANKS),DARK_PANEL=material("literary_dark_panel",Blocks.DARK_OAK_PLANKS);
    // The two-stage elk vignette (0.4.50): the yacht, the crew's site and the cave of carcasses.
    public static final DeferredBlock<Block> YACHT_HULL=material("yacht_hull",Blocks.WHITE_CONCRETE),YACHT_STRIPE=material("yacht_hull_stripe",Blocks.WHITE_CONCRETE),YACHT_BOOT=material("yacht_boot",Blocks.BLUE_CONCRETE),YACHT_ANTIFOUL=material("yacht_antifoul",Blocks.RED_TERRACOTTA),
        YACHT_TEAK=material("yacht_teak",Blocks.OAK_PLANKS),YACHT_PANEL=material("yacht_salon_panel",Blocks.DARK_OAK_PLANKS),YACHT_CARPET=material("yacht_carpet",Blocks.WHITE_WOOL),YACHT_CUSHION=material("yacht_cushion",Blocks.WHITE_WOOL),YACHT_CANVAS=material("yacht_canvas",Blocks.BLUE_WOOL),
        SITE_SIDING=material("site_siding",Blocks.IRON_BLOCK),DRAG_MUD=material("cave_drag_mud",Blocks.PACKED_MUD);
    public static final DeferredBlock<YachtGlazingBlock> YACHT_WINDOW=BLOCKS.registerBlock("yacht_window",YachtGlazingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion().noLootTable()),
        YACHT_PORTHOLE=BLOCKS.registerBlock("yacht_porthole",YachtGlazingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion().noLootTable());
    public static final DeferredBlock<IronBarsBlock> YACHT_RAIL=BLOCKS.registerBlock("yacht_rail",IronBarsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noLootTable()),
        SAFETY_FENCE=BLOCKS.registerBlock("safety_fence",IronBarsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(net.minecraft.world.level.block.SoundType.WOOL).noLootTable());
    public static final DeferredItem<Item> TOOTH=item("elk_tooth"),KEY=item("wheel_house_key"),KNIFE=item("father_knife"),JAR=item("grasshopper_jar"),PHOTOGRAPH=item("literary_photograph"),FILM=item("family_film");
    public static final DeferredItem<LiteraryMealItem> MEAL=ITEMS.register("father_meal",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationModifier(.4F).alwaysEdible().build())));
    public static final DeferredItem<LiteraryMealItem> STEW=ITEMS.register("family_stew",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(.2F).alwaysEdible().build())));
    // The cabin bargain (0.4.51): a globe for the reader who gave everything, a cracked one for the reader who refused; the arm the keeper holds.
    public static final DeferredItem<SnowGlobeItem> SNOW_GLOBE=ITEMS.register("cabin_snow_globe",()->new SnowGlobeItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),false)),
        CRACKED_GLOBE=ITEMS.register("cabin_cracked_snow_globe",()->new SnowGlobeItem(new Item.Properties().stacksTo(1),true));
    public static final DeferredItem<Item> GIVEN_ARM=item("cabin_given_arm");
    public static final DeferredItem<Item> CYLINDER_ONE=item("wax_cylinder_one"),CYLINDER_TWO=item("wax_cylinder_two"),CYLINDER_THREE=item("wax_cylinder_three");
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryActor>> ACTOR=TYPES.register("literary_actor",()->EntityType.Builder.<LiteraryActor>of(LiteraryActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("literary_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryElk>> ELK=TYPES.register("literary_elk",()->EntityType.Builder.<LiteraryElk>of(LiteraryElk::new,MobCategory.CREATURE).sized(1.25F,1.65F).clientTrackingRange(16).updateInterval(2).build("literary_elk"));
    public static final DeferredHolder<SoundEvent,SoundEvent> SCRATCH=sound("scratch"),DRIP=sound("drip"),PARTY=sound("party"),HOOVES=sound("hooves"),GIGGLE=sound("giggle"),BLIZZARD=sound("blizzard"),CYLINDER1=sound("cylinder_one"),CYLINDER2=sound("cylinder_two"),CYLINDER3=sound("cylinder_three"),FILM_SOUND=sound("family_film");
    public static final DeferredHolder<SoundEvent,SoundEvent> CABIN_KNOCK=sound("cabin_knock"),CABIN_HEART=sound("cabin_heart"),CABIN_CORD=sound("cabin_cord"),CABIN_CHOP=sound("cabin_chop"),CABIN_WET=sound("cabin_wet"),CABIN_RING=sound("cabin_ring"),CABIN_WIND=sound("cabin_wind"),GLOBE_SHAKE=sound("globe_shake"),GLOBE_CRACKED=sound("globe_cracked"),CABIN_GALE=sound("cabin_gale");
    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private static DeferredItem<Item> item(String id){var props=new Item.Properties().stacksTo(1);if(id.startsWith("wax_cylinder_")||id.equals("family_film"))props.jukeboxPlayable(net.minecraft.resources.ResourceKey.create(Registries.JUKEBOX_SONG,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id.replace("one","1").replace("two","2").replace("three","3"))));return ITEMS.registerSimpleItem(id,props);}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("literary."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"literary."+name)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);BES.register(bus);SOUNDS.register(bus);PARTICLES.register(bus);bus.addListener(LiteraryRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),LiteraryActor.attributes().build());e.put(ELK.get(),LiteraryElk.attributes().build());}
    private LiteraryRegistry(){}
}
