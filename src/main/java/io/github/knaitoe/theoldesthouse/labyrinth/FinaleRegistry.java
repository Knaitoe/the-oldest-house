package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class FinaleRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<MinotaurEntity>> MINOTAUR=TYPES.register("minotaur",()->EntityType.Builder
            .<MinotaurEntity>of(MinotaurEntity::new,MobCategory.MISC).sized(1.8F,3.1F).eyeHeight(2.65F).clientTrackingRange(16).updateInterval(1).build("minotaur"));
    public static final DeferredHolder<EntityType<?>,EntityType<FinaleWitness>> WITNESS=TYPES.register("finale_witness",()->EntityType.Builder
            .<FinaleWitness>of(FinaleWitness::new,MobCategory.MISC).sized(.6F,1.8F).eyeHeight(1.55F).clientTrackingRange(12).updateInterval(3).build("finale_witness"));
    private FinaleRegistry(){}
    public static void register(IEventBus bus){TYPES.register(bus);bus.addListener(FinaleRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(MINOTAUR.get(),MinotaurEntity.attributes().build());event.put(WITNESS.get(),FinaleWitness.attributes().build());}
}
