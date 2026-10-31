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

/** 0.4.200, Feet Up: the family house the children's game is played in, its materials and its fittings. Drawn by tools/draw_feet_up_art.py. */
public final class FeetUpRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The voices below raised, the count's knock, a board giving way (tools/generate_feet_up_audio.py). */
    public static final DeferredHolder<SoundEvent,SoundEvent> SHOUT=sound("shout"),COUNT=sound("count"),CRACK=sound("crack");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("feet_up."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"feet_up."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /**
     * The ground floor's varnished boards and the hall's tiles (the floor, which is lava), the first floor's boards that hold,
     * the attic's rough boards, the papered and the painted walls, the plaster ceilings, the brick of the chimney breast and
     * the slates of the roof seen from inside.
     */
    public static final List<String> MATERIAL_NAMES=List.of("floor","hall_tile","board","attic","paper","paper_dark","plaster","brick","roof");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("feet_up_"+name,fixed(Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<FeetUpBlock>> FITTING_TYPE=CODECS.register("feet_up_fitting",()->FeetUpBlock.CODEC);
    /** Every fitting the game is played over or round: the boards that go, the mattress, the pots, the dumbwaiter, the lamps, the chalk. */
    public static final DeferredBlock<FeetUpBlock> FITTING=BLOCKS.registerBlock("feet_up_fitting",FeetUpBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(FeetUpBlock::light));
    private FeetUpRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
