package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MotherRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<MotherEntity>> MOTHER =
            TYPES.register("mother_of_strays", () -> EntityType.Builder
                    .<MotherEntity>of(MotherEntity::new, MobCategory.MISC)
                    .sized(0.66F, 1.9F).eyeHeight(1.62F)
                    .clientTrackingRange(12).updateInterval(3)
                    .build("mother_of_strays"));
    public static final DeferredHolder<EntityType<?>, EntityType<MotherPekingese>> PEKINGESE =
            TYPES.register("mother_pekingese", () -> EntityType.Builder
                    .<MotherPekingese>of(MotherPekingese::new, MobCategory.CREATURE)
                    .sized(0.55F, 0.55F).eyeHeight(0.4F)
                    .clientTrackingRange(10).updateInterval(3)
                    .build("mother_pekingese"));

    private MotherRegistry() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(MotherRegistry::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(MOTHER.get(), MotherEntity.attributes().build());
        event.put(PEKINGESE.get(), Wolf.createAttributes().build());
    }
}
