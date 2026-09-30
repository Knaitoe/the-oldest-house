package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClapGhostRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(
            Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<ClapGhostEntity>> GIRL = TYPES.register(
            "clap_ghost_girl", () -> EntityType.Builder.<ClapGhostEntity>of(ClapGhostEntity::new, MobCategory.MISC)
                    .sized(0.48F, 1.3F).eyeHeight(1.05F).clientTrackingRange(8).updateInterval(1)
                    .build("clap_ghost_girl"));
    private ClapGhostRegistry() {}
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(ClapGhostRegistry::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(GIRL.get(), ClapGhostEntity.attributes().build());
    }
}
