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
 * 0.4.408, The House on the Corner (docs/THE_HOUSE_ON_THE_CORNER_0_4_408.md): the street's and the house's materials and fittings, and
 * their sounds. Drawn by tools/draw_corner_art.py; heard from tools/generate_corner_audio.py.
 */
public final class CornerRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The street's painted render, its pavement slabs and its old stone setts; the house's patterned floor tiles and the corridor's paper.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("stucco","paving","setts","tiles","wallpaper");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("corner_"+name,fixed(switch(name){case "paving","setts","tiles"->Blocks.STONE;case "wallpaper"->Blocks.OAK_PLANKS;default->Blocks.WHITE_TERRACOTTA;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CornerBlock>> FITTING_TYPE=CODECS.register("corner_fitting",()->CornerBlock.CODEC);
    public static final DeferredBlock<CornerBlock> FITTING=BLOCKS.registerBlock("corner_fitting",CornerBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(CornerBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The iron gate on its hinges; a jar set down on a board; a door shut softly, deep in the house; cicadas at siesta; the lit room's hum. */
    public static final DeferredHolder<SoundEvent,SoundEvent> GATE=sound("corner.gate"),JAR=sound("corner.jar"),SHUT=sound("corner.shut"),CICADAS=sound("corner.cicadas"),HUM=sound("corner.hum");
    private CornerRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
