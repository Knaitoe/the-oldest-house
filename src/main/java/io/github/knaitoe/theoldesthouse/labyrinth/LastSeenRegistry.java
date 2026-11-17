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
 * 0.4.667, Last Seen (docs/LAST_SEEN_0_4_667.md): the park's and the office's materials, their fittings and their sounds. Drawn by
 * tools/draw_last_seen_art.py; heard from tools/generate_last_seen_audio.py.
 */
public final class LastSeenRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The park's path tiles and its curbs; the beds' trodden earth; the office's render, its plinth and its floor and ceiling; the avenue's
     * asphalt and its pavement; the houses' painted render; the bandstand's stone; the plane trees' patched bark; two more houses' renders;
     * the avenue's centre line and the stripes of the crossing at the park's gate.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("paving","curb","earth","render","plinth","floor","ceiling","asphalt",
        "pavement","house","stone","bark","house_b","house_c","road_line","crossing");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("last_seen_"+name,fixed(switch(name){
        case "paving","curb","asphalt","pavement","stone","plinth","road_line","crossing"->Blocks.STONE;case "earth"->Blocks.DIRT;case "bark"->Blocks.OAK_PLANKS;
        case "floor","ceiling"->Blocks.SMOOTH_STONE;default->Blocks.WHITE_TERRACOTTA;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LastSeenBlock>> FITTING_TYPE=CODECS.register("last_seen_fitting",()->LastSeenBlock.CODEC);
    public static final DeferredBlock<LastSeenBlock> FITTING=BLOCKS.registerBlock("last_seen_fitting",LastSeenBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(LastSeenBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The avenue beyond the railings; pigeons on the paths; a folder taken down or put back; the typewriter. */
    public static final DeferredHolder<SoundEvent,SoundEvent> TRAFFIC=sound("last_seen.traffic"),PIGEONS=sound("last_seen.pigeons"),PAPER=sound("last_seen.paper"),
        TYPEWRITER=sound("last_seen.typewriter");
    private LastSeenRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
