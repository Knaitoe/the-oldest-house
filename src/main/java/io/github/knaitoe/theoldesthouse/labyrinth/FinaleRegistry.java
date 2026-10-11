package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The charge (0.4.84): its bellow, and a hoof on stone. Both are subtitled. */
    public static final DeferredHolder<SoundEvent,SoundEvent> BELLOW=sound("finale.minotaur_bellow"),HOOF=sound("finale.minotaur_hoof");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private FinaleRegistry(){}
    public static void register(IEventBus bus){TYPES.register(bus);SOUNDS.register(bus);bus.addListener(FinaleRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(MINOTAUR.get(),MinotaurEntity.attributes().build());event.put(WITNESS.get(),FinaleWitness.attributes().build());}
}
