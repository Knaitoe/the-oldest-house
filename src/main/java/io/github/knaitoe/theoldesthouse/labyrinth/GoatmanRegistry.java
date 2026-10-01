package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class GoatmanRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    public static final DeferredItem<net.minecraft.world.item.Item> PLATE=ITEMS.registerSimpleItem("trailer_plate",new net.minecraft.world.item.Item.Properties().stacksTo(1));
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<GoatmanChild>> CHILD=TYPES.register("trailer_child",()->EntityType.Builder.<GoatmanChild>of(GoatmanChild::new,MobCategory.MISC).sized(.6F,1.8F).eyeHeight(1.62F).clientTrackingRange(12).updateInterval(1).build("trailer_child"));
    public static final DeferredHolder<SoundEvent,SoundEvent> HAMMER=sound("goatman.hammer"),LAUGH=sound("goatman.laugh"),WOODS=sound("goatman.woods");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private GoatmanRegistry(){}
    public static void register(IEventBus bus){TYPES.register(bus);SOUNDS.register(bus);ITEMS.register(bus);bus.addListener(GoatmanRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(CHILD.get(),GoatmanChild.attributes().build());}
}
