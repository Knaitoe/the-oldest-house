package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class DrownedTownRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NoticePostBlock>> NOTICE_TYPE=CODECS.register("notice_post",()->NoticePostBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TownStreetlightBlock>> STREETLIGHT_TYPE=CODECS.register("streetlight",()->TownStreetlightBlock.CODEC);
    public static final DeferredBlock<TownStreetlightBlock> STREETLIGHT=BLOCKS.registerBlock("streetlight",TownStreetlightBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredBlock<NoticePostBlock> NOTICE=BLOCKS.registerBlock("notice_post",NoticePostBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<NoticePostBlockEntity>> NOTICE_ENTITY=BLOCK_ENTITIES.register("notice_post",()->BlockEntityType.Builder.of(NoticePostBlockEntity::new,NOTICE.get()).build(null));
    /** Proofrock's fitted pieces and the school's teacher desks (0.4.67). */
    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<TownFixtureBlock>> FIXTURE_TYPE = CODECS.register("town_fixture", () -> TownFixtureBlock.CODEC);
    public static final DeferredBlock<TownFixtureBlock> FIXTURE = BLOCKS.registerBlock("town_fixture", TownFixtureBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<SchoolDeskBlock>> SCHOOL_DESK_TYPE = CODECS.register("school_desk", () -> SchoolDeskBlock.CODEC);
    public static final DeferredBlock<SchoolDeskBlock> SCHOOL_DESK = BLOCKS.registerBlock("school_desk", SchoolDeskBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SchoolDeskBlockEntity>> SCHOOL_DESK_ENTITY = BLOCK_ENTITIES.register("school_desk",
            () -> BlockEntityType.Builder.of(SchoolDeskBlockEntity::new, SCHOOL_DESK.get()).build(null));
    public static final DeferredItem<Item> WET_ESSAY_ONE = wet("waterlogged_essay_one");
    public static final DeferredItem<Item> WET_ESSAY_TWO = wet("waterlogged_essay_two");
    public static final DeferredItem<Item> WET_ESSAY_THREE = wet("waterlogged_essay_three");
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_ONE = dry("dried_essay_one", 0);
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_TWO = dry("dried_essay_two", 1);
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_THREE = dry("dried_essay_three", 2);
    public static final DeferredItem<ChurchKeyItem> CHURCH_KEY = ITEMS.register("church_key", () -> new ChurchKeyItem(yieldProperties()));
    public static final DeferredItem<ShallowsBurdenItem> SHALLOWS_BURDEN = ITEMS.register("shallows_burden", () -> new ShallowsBurdenItem(new Item.Properties()));
    public static final DeferredItem<LakePhoneItem> LAKE_PHONE=ITEMS.register("lake_phone",()->new LakePhoneItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<EntityType<?>,EntityType<LakePhoneCamera>> PHONE_CAMERA=TYPES.register("lake_phone_camera",
            ()->EntityType.Builder.<LakePhoneCamera>of(LakePhoneCamera::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(10).updateInterval(1).build("lake_phone_camera"));
    public static final DeferredHolder<EntityType<?>, EntityType<LakeCanoeEntity>> CAVE_CANOE = TYPES.register("lake_canoe",
            () -> EntityType.Builder.<LakeCanoeEntity>of(LakeCanoeEntity::new, MobCategory.MISC).sized(1.375F,.5625F).clientTrackingRange(10).build("lake_canoe"));
    public static final DeferredHolder<EntityType<?>, EntityType<LakeWitchEntity>> LAKE_WITCH = TYPES.register("lake_witch",
            () -> EntityType.Builder.<LakeWitchEntity>of(LakeWitchEntity::new, MobCategory.MONSTER)
                    .sized(.78F, .94F).eyeHeight(.68F).clientTrackingRange(12).updateInterval(2).build("lake_witch"));
    public static final DeferredHolder<EntityType<?>, EntityType<LakeCongregantEntity>> CONGREGANT = TYPES.register("lake_congregant",
            () -> EntityType.Builder.<LakeCongregantEntity>of(LakeCongregantEntity::new, MobCategory.MISC)
                    .sized(.6F, 1.8F).eyeHeight(1.6F).clientTrackingRange(10).build("lake_congregant"));
    public static final DeferredHolder<SoundEvent, SoundEvent> HYMN = sound("vignette.lake_hymn");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_VOICE = sound("vignette.lake_witch");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAKE_LEAK = sound("leak.indian_lake");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVE_LEAK = sound("leak.preserved_cave");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHALLOWS_LEAK = sound("leak.shallows");
    public static final DeferredHolder<SoundEvent,SoundEvent> PHONE_LEAK=sound("leak.phone_canoe");
    public static final DeferredHolder<SoundEvent,SoundEvent> PHONE_RECORD=sound("vignette.phone_record");
    public static final DeferredHolder<SoundEvent,SoundEvent> PHONE_WATER=sound("vignette.phone_water");
    private static final java.util.List<DeferredHolder<SoundEvent,SoundEvent>> CAVE_VOICES = java.util.stream.IntStream.range(0,8).mapToObj(i->sound("vignette.cave_voice_"+i)).toList();
    public static SoundEvent caveVoice(int index){return CAVE_VOICES.get(Math.max(0,Math.min(7,index))).get();}
    private DrownedTownRegistry() {}

    private static Item.Properties yieldProperties() {
        CompoundTag tag = new CompoundTag(); tag.putString("the_oldest_house_yield", DrownedTown.ID);
        return new Item.Properties().stacksTo(1).component(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    private static DeferredItem<Item> wet(String id) { return ITEMS.registerSimpleItem(id, yieldProperties()); }
    private static DeferredItem<DrownedEssayItem> dry(String id, int essay) {
        return ITEMS.register(id, () -> new DrownedEssayItem(yieldProperties(), essay));
    }
    private static DeferredHolder<SoundEvent, SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, id)));
    }
    public static Item wetEssay(int index) { return switch(index) {case 0 -> WET_ESSAY_ONE.get(); case 1 -> WET_ESSAY_TWO.get(); default -> WET_ESSAY_THREE.get();}; }
    public static Item dryEssay(int index) { return switch(index) {case 0 -> DRIED_ESSAY_ONE.get(); case 1 -> DRIED_ESSAY_TWO.get(); default -> DRIED_ESSAY_THREE.get();}; }
    public static void register(IEventBus bus) {
        CODECS.register(bus); BLOCKS.register(bus); BLOCK_ENTITIES.register(bus); ITEMS.register(bus); TYPES.register(bus); SOUNDS.register(bus);
        bus.addListener(DrownedTownRegistry::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(LAKE_WITCH.get(), LakeWitchEntity.attributes().build());
        event.put(CONGREGANT.get(), LakeCongregantEntity.attributes().build());
    }
}
