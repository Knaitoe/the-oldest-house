package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
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
 * 0.4.227, the last swimmer (docs/LAST_SWIMMER_0_4_227.md): the pool's tile, lane line, deck, wall tile and band, ceiling, the
 * office's blockwork and the corridor's lino, and its one fitting block. Drawn by tools/draw_swimmer_art.py.
 */
public final class SwimmerRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("pool_tile","pool_line","deck","wall_tile","wall_band","ceiling","blockwork","lino");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("swimmer_"+name,fixed(name.equals("lino")?Blocks.OAK_PLANKS:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SwimmerBlock>> FITTING_TYPE=CODECS.register("swimmer_fitting",()->SwimmerBlock.CODEC);
    public static final DeferredBlock<SwimmerBlock> FITTING=BLOCKS.registerBlock("swimmer_fitting",SwimmerBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(SwimmerBlock::light));
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The turn against the tiles, the reel, the step under the cover (tools/generate_swimmer_audio.py); the radio ships empty. */
    public static final DeferredHolder<SoundEvent,SoundEvent> TURN=sound("swimmer.turn"),HAUL=sound("swimmer.haul"),STEP=sound("swimmer.step"),RADIO=sound("swimmer.radio");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private SwimmerRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
