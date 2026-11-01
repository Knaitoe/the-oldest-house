package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.208, the dark halls (docs/DARK_HALLS_0_4_208.md): the smooth ash-grey wall, its floor and its ceiling, and four sounds: the
 * House's own low rumble when it takes a light (never the Minotaur's), footsteps behind, a door slamming late, and a thing landing
 * far below. Drawn by tools/draw_dark_hall_art.py; the sounds are made by tools/generate_dark_hall_audio.py.
 */
public final class DarkHallRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<SoundEvent,SoundEvent> RUMBLE=sound("rumble"),STEPS=sound("steps"),SLAM=sound("slam"),LAND=sound("land");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("dark_hall."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dark_hall."+name)));}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The smooth ash wall, its floor and its ceiling. */
    public static final List<String> MATERIAL_NAMES=List.of("ash","floor","ceiling");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("dark_hall_"+name,BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).strength(-1,3600000).noLootTable()));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    private DarkHallRegistry(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);SOUNDS.register(bus);}
}
