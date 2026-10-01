package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class DrownedTownRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TheOldestHouse.MOD_ID);
    public static final DeferredItem<Item> WET_ESSAY_ONE = wet("waterlogged_essay_one");
    public static final DeferredItem<Item> WET_ESSAY_TWO = wet("waterlogged_essay_two");
    public static final DeferredItem<Item> WET_ESSAY_THREE = wet("waterlogged_essay_three");
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_ONE = dry("dried_essay_one", 0);
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_TWO = dry("dried_essay_two", 1);
    public static final DeferredItem<DrownedEssayItem> DRIED_ESSAY_THREE = dry("dried_essay_three", 2);
    public static final DeferredItem<ChurchKeyItem> CHURCH_KEY = ITEMS.register("church_key", () -> new ChurchKeyItem(yieldProperties()));
    public static final DeferredHolder<EntityType<?>, EntityType<LakeWitchEntity>> LAKE_WITCH = TYPES.register("lake_witch",
            () -> EntityType.Builder.<LakeWitchEntity>of(LakeWitchEntity::new, MobCategory.MONSTER)
                    .sized(.68F, 2.05F).eyeHeight(1.78F).clientTrackingRange(12).updateInterval(2).build("lake_witch"));
    public static final DeferredHolder<EntityType<?>, EntityType<LakeCongregantEntity>> CONGREGANT = TYPES.register("lake_congregant",
            () -> EntityType.Builder.<LakeCongregantEntity>of(LakeCongregantEntity::new, MobCategory.MISC)
                    .sized(.6F, 1.8F).eyeHeight(1.6F).clientTrackingRange(10).build("lake_congregant"));
    public static final DeferredHolder<SoundEvent, SoundEvent> HYMN = sound("vignette.lake_hymn");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_VOICE = sound("vignette.lake_witch");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAKE_LEAK = sound("leak.indian_lake");
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
        ITEMS.register(bus); TYPES.register(bus); SOUNDS.register(bus);
        bus.addListener(DrownedTownRegistry::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(LAKE_WITCH.get(), LakeWitchEntity.attributes().build());
        event.put(CONGREGANT.get(), LakeCongregantEntity.attributes().build());
    }
}
