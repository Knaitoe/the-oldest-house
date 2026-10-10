package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class NovelRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WellCoverBlock>> WELL_COVER_CODEC=CODECS.register("well_cover",()->WellCoverBlock.CODEC);
    public static final DeferredBlock<WellCoverBlock> WELL_COVER=BLOCKS.registerBlock("well_cover",WellCoverBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_TRAPDOOR).noOcclusion().noLootTable());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<WellCoverBlockEntity>> WELL_COVER_ENTITY=BLOCK_ENTITIES.register("well_cover",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(WellCoverBlockEntity::new,WELL_COVER.get()).build(null));
    public static final DeferredBlock<Block> GOUGES=material("cell_gouges",Blocks.DEEPSLATE_TILES),SEALED_WINDOW=material("sealed_window",Blocks.DARK_OAK_PLANKS),
        INSTITUTE=material("institute_paint",Blocks.WHITE_TERRACOTTA),PLASTER=material("collapse_plaster",Blocks.WHITE_TERRACOTTA),
        CARVINGS=material("well_carvings",Blocks.MOSSY_COBBLESTONE),PAPER=material("archive_paper",Blocks.WHITE_TERRACOTTA);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NovelPropBlock>> PROP_TYPE=CODECS.register("novel_prop",()->NovelPropBlock.CODEC);
    public static final DeferredBlock<NovelPropBlock> PROP=BLOCKS.registerBlock("novel_prop",NovelPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<InstituteSignBlock>> SIGN_TYPE=CODECS.register("institute_sign",()->InstituteSignBlock.CODEC);
    public static final DeferredBlock<InstituteSignBlock> SIGN=BLOCKS.registerBlock("institute_sign",InstituteSignBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<InstituteFixtureBlock>> WARD_FIXTURE_TYPE=CODECS.register("institute_fixture",()->InstituteFixtureBlock.CODEC);
    public static final DeferredBlock<InstituteFixtureBlock> WARD_FIXTURE=BLOCKS.registerBlock("institute_fixture",InstituteFixtureBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable().lightLevel(s->s.getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.LIGHT?13:0));
    public static final DeferredBlock<FenceBlock> WARD_RAIL=BLOCKS.registerBlock("institute_rail",FenceBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredBlock<StairBlock> WARD_STAIRS=BLOCKS.registerBlock("institute_stairs",p->new StairBlock(Blocks.SMOOTH_STONE.defaultBlockState(),p),BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).noLootTable());
    public static final DeferredBlock<SlabBlock> WARD_SLAB=BLOCKS.registerBlock("institute_slab",SlabBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).noLootTable());
    public static final DeferredBlock<DoorBlock> WARD_DOOR=BLOCKS.registerBlock("institute_door",p->new DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.BIRCH,p),BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_DOOR).noLootTable());
    public static final DeferredBlock<DoorBlock> WARD_LOCKED_DOOR=BLOCKS.registerBlock("institute_locked_door",p->new DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON,p),BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).noLootTable());
    public static final DeferredBlock<IronBarsBlock> WARD_GLASS=BLOCKS.registerBlock("institute_glass",IronBarsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_STAINED_GLASS_PANE).noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<InstituteCabinetBlock>> WARD_CABINET_TYPE=CODECS.register("institute_cabinet",()->InstituteCabinetBlock.CODEC);
    public static final DeferredBlock<InstituteCabinetBlock> WARD_CABINET=BLOCKS.registerBlock("institute_cabinet",InstituteCabinetBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noLootTable());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<InstituteCabinetBlockEntity>> WARD_CABINET_ENTITY=BLOCK_ENTITIES.register("institute_cabinet",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(InstituteCabinetBlockEntity::new,WARD_CABINET.get()).build(null));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<InstituteNotebookBlock>> WARD_NOTEBOOK_TYPE=CODECS.register("institute_notebook",()->InstituteNotebookBlock.CODEC);
    public static final DeferredBlock<InstituteNotebookBlock> WARD_NOTEBOOK=BLOCKS.registerBlock("institute_notebook",InstituteNotebookBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<InstituteNotebookBlockEntity>> WARD_NOTEBOOK_ENTITY=BLOCK_ENTITIES.register("institute_notebook",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(InstituteNotebookBlockEntity::new,WARD_NOTEBOOK.get()).build(null));
    public static final DeferredItem<Item> COLLAR=ITEMS.registerSimpleItem("cat_collar",new Item.Properties().stacksTo(1)),RIBBON=ITEMS.registerSimpleItem("well_ribbon",new Item.Properties().stacksTo(1)),
        ARCHIVE_KEY=ITEMS.registerSimpleItem("archive_key",new Item.Properties().stacksTo(1));
    public static final DeferredItem<ShieldItem> HOLLOWAY_SHIELD=ITEMS.register("holloway_shield",()->new ShieldItem(new Item.Properties().durability(336)));
    public static final DeferredItem<PlainCameraItem> CAMERA=ITEMS.registerItem("plain_camera",PlainCameraItem::new,new Item.Properties().stacksTo(1));
    public static final DeferredItem<FlintAndSteelItem> LIGHTER=ITEMS.register("lighter",()->new FlintAndSteelItem(new Item.Properties().durability(64)));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PigeonholeBlock>> PIGEONHOLE_TYPE=CODECS.register("pigeonhole",()->PigeonholeBlock.CODEC);
    public static final DeferredBlock<PigeonholeBlock> PIGEONHOLE=BLOCKS.registerBlock("pigeonhole",PigeonholeBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noLootTable());
    public static final DeferredItem<SelfAddressedEnvelopeItem> ENVELOPE=ITEMS.registerItem("self_addressed_envelope",SelfAddressedEnvelopeItem::new,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<WalkieTalkieItem> RADIO=ITEMS.register("walkie_talkie",()->new WalkieTalkieItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<EntityType<?>,EntityType<NovelVulture>> VULTURE=TYPES.register("vulture",()->EntityType.Builder.<NovelVulture>of(NovelVulture::new,MobCategory.MISC).sized(.9F,.45F).clientTrackingRange(16).updateInterval(2).build("vulture"));
    public static final DeferredHolder<EntityType<?>,EntityType<NovelActor>> ACTOR=TYPES.register("novel_actor",()->EntityType.Builder.<NovelActor>of(NovelActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(12).updateInterval(2).build("novel_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<HouseHuman>> HUMAN=TYPES.register("house_human",()->EntityType.Builder.<HouseHuman>of(HouseHuman::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("house_human"));
    public static final DeferredHolder<SoundEvent,SoundEvent> MONITOR=sound("novel.monitor"),SHUTTER=sound("novel.shutter"),RADIO_STATIC=sound("novel.radio"),COLLAPSE=sound("novel.collapse");
    public static final DeferredHolder<SoundEvent,SoundEvent> WELL_BREATH=sound("well.breath");
    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private NovelRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BLOCK_ENTITIES.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);bus.addListener(NovelRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(VULTURE.get(),NovelVulture.attributes().build());e.put(ACTOR.get(),NovelActor.attributes().build());e.put(HUMAN.get(),HouseHuman.attributes().build());}
}
