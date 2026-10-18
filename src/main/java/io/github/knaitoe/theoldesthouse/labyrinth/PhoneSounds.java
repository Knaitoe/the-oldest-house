package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The player's phone (0.4.105): ringback, the line opening, the voicemail tone, texts going and coming, and his phone ringing. */
public final class PhoneSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<SoundEvent,SoundEvent> RINGBACK=sound("phone.ringback"),PICKUP=sound("phone.pickup"),BEEP=sound("phone.beep"),
            SENT=sound("phone.sent"),RECEIVED=sound("phone.received"),HIS_RING=sound("phone.his_ring");
    private PhoneSounds(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){SOUNDS.register(bus);}
}
