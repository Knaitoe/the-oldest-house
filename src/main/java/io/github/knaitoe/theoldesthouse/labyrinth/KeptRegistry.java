package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.410, the Kept (docs/THE_KEPT_0_4_410.md): the machine's materials, its fittings and its sounds. Drawn by tools/draw_kept_art.py;
 * heard from tools/generate_kept_audio.py.
 */
public final class KeptRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The machine's riveted casing, its floor grating, its ceiling panels hung with cable, the same casing gone under rime, the cave's
     * packed frost, the dining hall's flocked paper and its chequered floor, and the glare behind the cells' doors.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("casing","grating","ceiling","rime","frost","flock","checker","glare");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("kept_"+name,switch(name){
        case "glare"->fixed(Blocks.WHITE_CONCRETE).lightLevel(s->15).emissiveRendering((s,l,p)->true);
        case "frost"->fixed(Blocks.SNOW_BLOCK);case "rime"->fixed(Blocks.PACKED_ICE).friction(.6F);case "flock"->fixed(Blocks.RED_WOOL);case "checker"->fixed(Blocks.SMOOTH_STONE);
        default->fixed(Blocks.IRON_BLOCK);}));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<KeptBlock>> FITTING_TYPE=CODECS.register("kept_fitting",()->KeptBlock.CODEC);
    public static final DeferredBlock<KeptBlock> FITTING=BLOCKS.registerBlock("kept_fitting",KeptBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(KeptBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The machine's hum; a screen waking; tins knocked together in a crate; someone weeping; the door onto the glare; the machine noticing. */
    public static final DeferredHolder<SoundEvent,SoundEvent> HUM=sound("kept.hum"),WAKE=sound("kept.wake"),TINS=sound("kept.tins"),WEEP=sound("kept.weep"),GLARE=sound("kept.glare"),NOTICE=sound("kept.notice");
    private KeptRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
