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
 * 0.4.749, the waxworks (docs/WAXWORKS_0_4_749.md): the museum's materials, its fittings and its sounds. Drawn by
 * tools/draw_waxworks_art.py; heard from tools/generate_waxworks_audio.py.
 */
public final class WaxRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The staff side: its painted block wall, its speckled linoleum, the office's ceiling tile. The public side: the galleries' dark
     * panelling and their wallpaper above it, the parquet, the corridor's runner, its plaster ceiling. Behind all of it, the brick.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("block","lino","tile","panel","paper","parquet","runner","plaster","brick");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("waxworks_"+name,fixed(switch(name){case "lino","tile","plaster"->Blocks.SMOOTH_STONE;case "panel","parquet"->Blocks.DARK_OAK_PLANKS;case "runner"->Blocks.RED_WOOL;case "brick"->Blocks.BRICKS;default->Blocks.WHITE_CONCRETE;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WaxBlock>> FITTING_TYPE=CODECS.register("waxworks_fitting",()->WaxBlock.CODEC);
    public static final DeferredBlock<WaxBlock> FITTING=BLOCKS.registerBlock("waxworks_fitting",WaxBlock::new,fixed(Blocks.DARK_OAK_PLANKS).noOcclusion().lightLevel(WaxBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The building at night (the ventilation, the tick of the radiators); the time clock punching a card; the monitors' hum. */
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOM=sound("waxworks.room"),PUNCH=sound("waxworks.punch"),MONITORS=sound("waxworks.monitors");
    private WaxRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
