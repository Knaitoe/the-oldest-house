package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.349, In the Tall Grass (docs/TALL_GRASS_0_4_349.md): the boy's cap, and the story's sounds. The grass, the wagon's hazards and the
 * pickup are the yard's fittings (tools/draw_yard_fittings.py); the sounds come from tools/generate_tall_grass_audio.py.
 */
public final class TallGrassRegistry {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The boy's cap, small and wet, from the trampled place at the heart of the field. Carried into a field, it gives a true bearing. */
    public static final DeferredItem<Item> CAP=ITEMS.registerSimpleItem("tall_grass_cap",new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** A boy calling out of the grass; the wagon's hazard relay; someone shouting; a car coming and pulling onto the gravel; a car pulling away. */
    public static final DeferredHolder<SoundEvent,SoundEvent> BOY=sound("grass.boy"),HAZARD=sound("grass.hazard"),CALL=sound("grass.call"),CAR=sound("grass.car"),AWAY=sound("grass.away");
    private TallGrassRegistry(){}
    public static void register(IEventBus bus){ITEMS.register(bus);SOUNDS.register(bus);}
}
