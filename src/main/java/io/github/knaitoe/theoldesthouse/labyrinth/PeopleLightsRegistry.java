package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.253, After the People Lights Have Gone Off (docs/PEOPLE_LIGHTS_0_4_253.md): the house Stan built. The foyer's large tiles, the
 * kitchen's smaller ones, the living room's boards, the hall runner, the study carpet, the plaster and the high-gloss trim, the siding
 * and the shingles, the garage's concrete and the driveway; the one fitting block; the things a visitor may carry (Mark's work case,
 * the spare chair); and the sounds of Kelly's chair on each floor. Drawn by tools/draw_people_lights_art.py, the sounds by
 * tools/generate_people_lights_audio.py.
 */
public final class PeopleLightsRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("tile_large","tile_small","boards","carpet","plaster","trim","siding","shingle","concrete","drive");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("people_"+name,fixed(name.equals("boards")||name.equals("siding")?Blocks.OAK_PLANKS:name.equals("carpet")?Blocks.WHITE_WOOL:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PeopleLightsBlock>> FITTING_TYPE=CODECS.register("people_fitting",()->PeopleLightsBlock.CODEC);
    public static final DeferredBlock<PeopleLightsBlock> FITTING=BLOCKS.registerBlock("people_fitting",PeopleLightsBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** What a visitor carries for the household: Mark's work case (custom model data 1) or the spare chair (2). */
    public static final DeferredItem<Item> CARRIED=ITEMS.registerSimpleItem("people_carried",new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Kelly's chair: on the foyer's large tiles, the living room's boards, the kitchen's small tiles, the hall runner. */
    public static final DeferredHolder<SoundEvent,SoundEvent> WHEELS_TILE=sound("people.wheels_tile"),WHEELS_WOOD=sound("people.wheels_wood"),
        WHEELS_KITCHEN=sound("people.wheels_kitchen"),WHEELS_RUNNER=sound("people.wheels_runner");
    /** The quieter sound in the night: pulling, and breathing. */
    public static final DeferredHolder<SoundEvent,SoundEvent> PULLING=sound("people.pulling");
    private PeopleLightsRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
