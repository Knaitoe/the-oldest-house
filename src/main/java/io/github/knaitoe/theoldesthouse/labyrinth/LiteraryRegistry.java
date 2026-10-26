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
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<GhostsSetBlock>> GHOST_SET_TYPE=CODECS.register("ghosts_set",()->GhostsSetBlock.CODEC);
    public static final DeferredBlock<GhostsSetBlock> GHOST_SET=BLOCKS.registerBlock("ghosts_set",GhostsSetBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable().lightLevel(GhostsSetBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<GhostsCaseBlock>> GHOST_CASE_TYPE=CODECS.register("ghosts_case",()->GhostsCaseBlock.CODEC);
    public static final DeferredBlock<GhostsCaseBlock> GHOST_CASE=BLOCKS.registerBlock("ghosts_case",GhostsCaseBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<GhostsCaseBlockEntity>> GHOST_CASE_ENTITY=BES.register("ghosts_case",()->BlockEntityType.Builder.of(GhostsCaseBlockEntity::new,GHOST_CASE.get()).build(null));
    public static final DeferredItem<CamcorderItem> CAMCORDER=ITEMS.registerItem("camcorder",CamcorderItem::new,new Item.Properties().stacksTo(1));
    /** A reader's own storm at the cabin (0.4.52): rain streaks and wind-torn leaves, drawn by their client only. */
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> CABIN_RAIN=PARTICLES.register("cabin_rain",()->new net.minecraft.core.particles.SimpleParticleType(false)),
        CABIN_LEAF=PARTICLES.register("cabin_leaf",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryPropBlock>> PROP_TYPE=CODECS.register("literary_prop",()->LiteraryPropBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryFrozenBlock>> FROZEN_TYPE=CODECS.register("literary_frozen",()->LiteraryFrozenBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<YachtGlazingBlock>> GLAZING_TYPE=CODECS.register("yacht_glazing",()->YachtGlazingBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NurseryBlock>> NURSERY_TYPE=CODECS.register("nursery",()->NurseryBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<FanLadderBlock>> FAN_LADDER_TYPE=CODECS.register("fan_ladder",()->FanLadderBlock.CODEC);
    public static final DeferredBlock<FanLadderBlock> FAN_LADDER=BLOCKS.registerBlock("fan_ladder",FanLadderBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable());
    /** The child's room (0.4.74): its own furniture and toys, and its wallpaper. */
    public static final DeferredBlock<NurseryBlock> NURSERY=BLOCKS.registerBlock("nursery",NurseryBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noOcclusion().noLootTable().lightLevel(NurseryBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<VignetteInteriorBlock>> INTERIOR_TYPE=CODECS.register("vignette_interior",()->VignetteInteriorBlock.CODEC);
    public static final DeferredBlock<VignetteInteriorBlock> INTERIOR=BLOCKS.registerBlock("vignette_interior",VignetteInteriorBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable().lightLevel(VignetteInteriorBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<RoadhouseBlock>> ROADHOUSE_TYPE=CODECS.register("roadhouse",()->RoadhouseBlock.CODEC);
    /** The Last Stop (0.4.78): the bar's furniture, its highway and lot, the field's stalks and the pickups. */
    public static final DeferredBlock<RoadhouseBlock> ROADHOUSE=BLOCKS.registerBlock("roadhouse",RoadhouseBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noLootTable().lightLevel(RoadhouseBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SiteMachineBlock>> SITE_MACHINE_TYPE=CODECS.register("site_machine",()->SiteMachineBlock.CODEC);
    /** The elk carcass site (0.4.120): the road crew's skid steer and flatbed, one cell of either machine per block. */
    public static final DeferredBlock<SiteMachineBlock> SITE_MACHINE=BLOCKS.registerBlock("site_machine",SiteMachineBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CottageBlock>> COTTAGE_TYPE=CODECS.register("cottage",()->CottageBlock.CODEC);
    /** Three Fingers (0.4.81): the cottage's walls, floor and thatch, its range, dresser and mantel, the papers and the lamp on the road. */
    public static final DeferredBlock<CottageBlock> COTTAGE=BLOCKS.registerBlock("cottage",CottageBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noLootTable().lightLevel(CottageBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<MasqueBlock>> MASQUE_TYPE=CODECS.register("masque",()->MasqueBlock.CODEC);
    /** The Masque (0.4.105): the seven chambers' velvet, glass and ebony, the braziers in the closed corridor and the clock. */
    public static final DeferredBlock<MasqueBlock> MASQUE=BLOCKS.registerBlock("masque",MasqueBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS).strength(.8F).noLootTable().lightLevel(MasqueBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<ManorBlock>> MANOR_TYPE=CODECS.register("manor",()->ManorBlock.CODEC);
    /** Bly (0.4.105): the manor's boards, damask and wet line, its rooms and conservatory; and the old man's signal lamp. */
    public static final DeferredBlock<ManorBlock> MANOR=BLOCKS.registerBlock("manor",ManorBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noLootTable().lightLevel(ManorBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CampDecorBlock>> CAMP_DECOR_TYPE=CODECS.register("camp_decor",()->CampDecorBlock.CODEC);
    public static final DeferredBlock<CampDecorBlock> CAMP_DECOR=BLOCKS.registerBlock("camp_decor",CampDecorBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion().noLootTable().lightLevel(CampDecorBlock::light));
    // 0.4.121: the camp's last session: its radio and horn, the mess hall, the schedule and the papers' surfaces.
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CampSessionBlock>> CAMP_SESSION_TYPE=CODECS.register("camp_session",()->CampSessionBlock.CODEC);
    public static final DeferredBlock<CampSessionBlock> CAMP_SESSION=BLOCKS.registerBlock("camp_session",CampSessionBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion().noLootTable());
    // 0.4.121: the Winchester house's Victorian paper, the séance room's blue, parquet and leaded glass.
    public static final DeferredBlock<Block> WINCHESTER_WALLPAPER=BLOCKS.registerBlock("winchester_wallpaper",Block::new,BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TERRACOTTA).noLootTable());
    public static final DeferredBlock<Block> WINCHESTER_SEANCE_PAPER=BLOCKS.registerBlock("winchester_seance_paper",Block::new,BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_TERRACOTTA).noLootTable());
    public static final DeferredBlock<Block> WINCHESTER_PARQUET=BLOCKS.registerBlock("winchester_parquet",Block::new,BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS).noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BarrettHomeBlock>> BARRETT_HOME_TYPE=CODECS.register("barrett_home",()->BarrettHomeBlock.CODEC);
    public static final DeferredBlock<BarrettHomeBlock> BARRETT_HOME=BLOCKS.registerBlock("barrett_home",BarrettHomeBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable().lightLevel(BarrettHomeBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WinchesterFittingBlock>> WINCHESTER_FITTING_TYPE=CODECS.register("winchester_fitting",()->WinchesterFittingBlock.CODEC);
    public static final DeferredBlock<WinchesterFittingBlock> WINCHESTER_FITTING=BLOCKS.registerBlock("winchester_fitting",WinchesterFittingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS).noOcclusion().noLootTable());
    public static final DeferredBlock<net.minecraft.world.level.block.TransparentBlock> WINCHESTER_LEADED_GLASS=BLOCKS.registerBlock("winchester_leaded_glass",net.minecraft.world.level.block.TransparentBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noLootTable());
    public static final DeferredBlock<LiteraryFrozenBlock> FROZEN=BLOCKS.registerBlock("literary_frozen",LiteraryFrozenBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).dynamicShape().noOcclusion().noLootTable());
    public static final DeferredBlock<LiteraryPropBlock> PROP=BLOCKS.registerBlock("literary_prop",LiteraryPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable().lightLevel(s->s.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.FAN&&s.getValue(LiteraryPropBlock.STAGE)!=1?11:0));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LiteraryModelBlockEntity>> MODEL=BES.register("literary_model",()->BlockEntityType.Builder.of(LiteraryModelBlockEntity::new,PROP.get(),FROZEN.get()).build(null));
    public static final DeferredBlock<Block> VAULT_CRACK=material("usher_crack",Blocks.DEEPSLATE_BRICKS),RED_FLOOR=material("crimson_floor",Blocks.RED_TERRACOTTA),TAPE=material("elk_tape",Blocks.GRAY_WOOL),PEELED_TAPE=material("elk_tape_peeled",Blocks.GRAY_WOOL),DRAG_SNOW=material("father_drag_snow",Blocks.SNOW_BLOCK),CARVED_TRUNK=material("rabbit_carved_trunk",Blocks.OAK_PLANKS),SIDING=material("manufactured_siding",Blocks.OAK_PLANKS),DARK_PANEL=material("literary_dark_panel",Blocks.DARK_OAK_PLANKS),CHILD_WALLPAPER=material("child_wallpaper",Blocks.BIRCH_PLANKS);
    // The two-stage elk vignette (0.4.50): the yacht, the crew's site and the cave of carcasses.
    public static final DeferredBlock<Block> YACHT_HULL=material("yacht_hull",Blocks.WHITE_CONCRETE),YACHT_STRIPE=material("yacht_hull_stripe",Blocks.WHITE_CONCRETE),YACHT_BOOT=material("yacht_boot",Blocks.BLUE_CONCRETE),YACHT_ANTIFOUL=material("yacht_antifoul",Blocks.RED_TERRACOTTA),
        YACHT_TEAK=material("yacht_teak",Blocks.OAK_PLANKS),YACHT_PANEL=material("yacht_salon_panel",Blocks.DARK_OAK_PLANKS),YACHT_CARPET=material("yacht_carpet",Blocks.WHITE_WOOL),YACHT_CUSHION=material("yacht_cushion",Blocks.WHITE_WOOL),YACHT_CANVAS=material("yacht_canvas",Blocks.BLUE_WOOL),
        SITE_SIDING=material("site_siding",Blocks.IRON_BLOCK),DRAG_MUD=material("cave_drag_mud",Blocks.PACKED_MUD);
    public static final DeferredBlock<YachtGlazingBlock> YACHT_WINDOW=BLOCKS.registerBlock("yacht_window",YachtGlazingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion().noLootTable()),
        // Glass to hear and see, but as slow to break out as the approved concrete porthole: about nine seconds, tool or not.
        YACHT_PORTHOLE=BLOCKS.registerBlock("yacht_porthole",YachtGlazingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(.6F).noOcclusion().noLootTable()); // 0.4.121: under a second by hand
    public static final DeferredBlock<IronBarsBlock> YACHT_RAIL=BLOCKS.registerBlock("yacht_rail",IronBarsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noLootTable()),
        SAFETY_FENCE=BLOCKS.registerBlock("safety_fence",IronBarsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(net.minecraft.world.level.block.SoundType.WOOL).noLootTable());
    public static final DeferredItem<Item> LIGHTBULB=item("fan_lightbulb");
    public static final DeferredItem<Item> TOOTH=item("elk_tooth"),KEY=item("wheel_house_key"),KNIFE=item("father_knife"),JAR=item("grasshopper_jar"),PHOTOGRAPH=item("literary_photograph"),FILM=item("family_film");
    public static final DeferredItem<LiteraryMealItem> MEAL=ITEMS.register("father_meal",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationModifier(.4F).alwaysEdible().build())));
    public static final DeferredItem<LiteraryMealItem> STEW=ITEMS.register("family_stew",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(.2F).alwaysEdible().build())));
    // The cabin bargain (0.4.51): a globe for the reader who gave everything, a cracked one for the reader who refused; the arm the keeper holds.
    public static final DeferredItem<SnowGlobeItem> SNOW_GLOBE=ITEMS.register("cabin_snow_globe",()->new SnowGlobeItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),false)),
        CRACKED_GLOBE=ITEMS.register("cabin_cracked_snow_globe",()->new SnowGlobeItem(new Item.Properties().stacksTo(1),true));
    public static final DeferredItem<Item> GIVEN_ARM=item("cabin_given_arm");
    /** Three Fingers (0.4.81): the withered paw, bound to the reader who pulls it from the fire. Its art is the owner's to replace. */
    public static final DeferredItem<WitheredPawItem> PAW=ITEMS.register("withered_paw",()->new WitheredPawItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<MillLetterItem> MILL_LETTER=ITEMS.register("mill_letter",()->new MillLetterItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CYLINDER_ONE=item("wax_cylinder_one"),CYLINDER_TWO=item("wax_cylinder_two"),CYLINDER_THREE=item("wax_cylinder_three");
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryActor>> ACTOR=TYPES.register("literary_actor",()->EntityType.Builder.<LiteraryActor>of(LiteraryActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("literary_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryElk>> ELK=TYPES.register("literary_elk",()->EntityType.Builder.<LiteraryElk>of(LiteraryElk::new,MobCategory.CREATURE).sized(1.25F,1.65F).clientTrackingRange(16).updateInterval(2).build("literary_elk"));
    public static final DeferredHolder<SoundEvent,SoundEvent> SCRATCH=sound("scratch"),DRIP=sound("drip"),PARTY=sound("party"),HOOVES=sound("hooves"),GIGGLE=sound("giggle"),BLIZZARD=sound("blizzard"),CYLINDER1=sound("cylinder_one"),CYLINDER2=sound("cylinder_two"),CYLINDER3=sound("cylinder_three"),FILM_SOUND=sound("family_film");
    public static final DeferredHolder<SoundEvent,SoundEvent> CABIN_KNOCK=sound("cabin_knock"),CABIN_HEART=sound("cabin_heart"),CABIN_CORD=sound("cabin_cord"),CABIN_CHOP=sound("cabin_chop"),CABIN_WET=sound("cabin_wet"),CABIN_RING=sound("cabin_ring"),CABIN_WIND=sound("cabin_wind"),GLOBE_SHAKE=sound("globe_shake"),GLOBE_CRACKED=sound("globe_cracked"),CABIN_GALE=sound("cabin_gale");
    /** The child's room's toys, its exits going and what is heard above the bed (0.4.74); each has its own subtitle. */
    public static final DeferredHolder<SoundEvent,SoundEvent> NURSERY_TRAIN=sound("nursery_train"),NURSERY_WHISTLE=sound("nursery_whistle"),NURSERY_TOP=sound("nursery_top"),
            NURSERY_CHIME=sound("nursery_chime"),NURSERY_POP=sound("nursery_pop"),NURSERY_MUSIC_BOX=sound("nursery_music_box"),NURSERY_SQUEAK=sound("nursery_squeak"),
            NURSERY_BLOCKS=sound("nursery_blocks"),NURSERY_BALL=sound("nursery_ball"),NURSERY_CREAK=sound("nursery_creak"),NURSERY_EXIT=sound("nursery_exit"),NURSERY_WHISPER=sound("nursery_whisper");
    /** The Last Stop (0.4.78): the struck pickup's alarm, a truck going by unseen, the pool table and the crash; each has its own subtitle. */
    public static final DeferredHolder<SoundEvent,SoundEvent> LOT_ALARM=sound("lot_alarm"),LOT_HIGHWAY=sound("lot_highway"),LOT_POOL=sound("lot_pool"),LOT_CRASH=sound("lot_crash");
    public static final DeferredHolder<SoundEvent,SoundEvent> PAW_KNOCK=sound("paw_knock"),PAW_HAMMER=sound("paw_hammer"),PAW_WIND=sound("paw_wind"),PAW_TWIST=sound("paw_twist"),PAW_SCRATCH=sound("paw_scratch"),PAW_HISS=sound("paw_hiss");
    public static final DeferredHolder<SoundEvent,SoundEvent> GHOST_LOW=sound("ghost_low"),GHOST_THIN=sound("ghost_thin"),GHOST_SCRAPE=sound("ghost_scrape"),GHOST_CARD=sound("ghost_card");
    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private static DeferredItem<Item> item(String id){var props=new Item.Properties().stacksTo(1);if(id.startsWith("wax_cylinder_")||id.equals("family_film"))props.jukeboxPlayable(net.minecraft.resources.ResourceKey.create(Registries.JUKEBOX_SONG,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id.replace("one","1").replace("two","2").replace("three","3"))));return ITEMS.registerSimpleItem(id,props);}
    // The Winchester house (0.4.121): the carpenters behind the walls and the clock that strikes thirteen.
    public static final DeferredHolder<SoundEvent,SoundEvent> WINCHESTER_HAMMER=sound("winchester_hammer"),WINCHESTER_SAW=sound("winchester_saw"),WINCHESTER_CHIME=sound("winchester_chime");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("literary."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"literary."+name)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);BES.register(bus);SOUNDS.register(bus);PARTICLES.register(bus);bus.addListener(LiteraryRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),LiteraryActor.attributes().build());e.put(ELK.get(),LiteraryElk.attributes().build());}
    private LiteraryRegistry(){}
}
