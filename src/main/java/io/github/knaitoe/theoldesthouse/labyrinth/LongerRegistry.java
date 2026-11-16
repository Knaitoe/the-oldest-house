package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md): Rowan's shop and the flat above it. The soot brick and the parapet's coping,
 * the roof's felt, the shop's plaster and boards, the flat's paper and boards, the bathroom's tiles, the ceilings, the shopfront's
 * painted timber, the pavement, the House's corridor (its paper and runner), and the buildings round the yard that should not be there
 * (their brick, their lit and their dark windows); the one fitting block. Drawn by tools/draw_longer_art.py.
 */
public final class LongerRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("brick","coping","felt","plaster","boards","paper","flatboards","tile","ceiling",
        "shopfront","pavement","road","corridor","runner","facade","facade_lit","facade_dark");
    private static final Set<String> WOOD=Set.of("boards","flatboards","shopfront","runner");
    static{for(String name:MATERIAL_NAMES){var props=fixed(WOOD.contains(name)?Blocks.OAK_PLANKS:Blocks.STONE);if(name.equals("facade_lit"))props=props.lightLevel(s->6);
        MATERIALS.put(name,BLOCKS.registerSimpleBlock("longer_"+name,props));}}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean material(net.minecraft.world.level.block.state.BlockState s){for(var b:MATERIALS.values())if(s.is(b.get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LongerBlock>> FITTING_TYPE=CODECS.register("longer_fitting",()->LongerBlock.CODEC);
    public static final DeferredBlock<LongerBlock> FITTING=BLOCKS.registerBlock("longer_fitting",LongerBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(LongerBlock::light));
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** 0.4.622 (tools/generate_longer_audio.py): Rowan's five notes, hummed at the bench; a valve set's dial turned through the band. */
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> HUM=sound("longer.hum"),TUNING=sound("longer.tuning"),BURST=sound("longer.burst");
    private LongerRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
