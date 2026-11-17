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
 * 0.4.663, Not Yet (docs/NOT_YET_0_4_663.md): the ward's materials, its fittings and its sounds. The machine's own casing, grating and
 * cable ceiling are the Kept's (KeptRegistry). Drawn by tools/draw_not_yet_art.py; heard from tools/generate_not_yet_audio.py.
 */
public final class NotYetRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** The ward's tiled floor, its painted wall, and the darker gloss of the dado under it. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("tile","wall","dado");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("not_yet_"+name,fixed(name.equals("tile")?Blocks.SMOOTH_STONE:Blocks.WHITE_CONCRETE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NotYetBlock>> FITTING_TYPE=CODECS.register("not_yet_fitting",()->NotYetBlock.CODEC);
    public static final DeferredBlock<NotYetBlock> FITTING=BLOCKS.registerBlock("not_yet_fitting",NotYetBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(NotYetBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The ward's hum; one turn of the counter. 0.4.664: the plates dropping open; the arms at work on the table. */
    public static final DeferredHolder<SoundEvent,SoundEvent> HUM=sound("not_yet.hum"),TICK=sound("not_yet.tick"),HATCH=sound("not_yet.hatch"),MEND=sound("not_yet.mend");
    private NotYetRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
