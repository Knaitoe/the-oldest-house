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
 * 0.4.209 onward, the horrors of the dark halls (docs/DARK_HALL_HORRORS_PROPOSAL.md): the one fitting block the five rooms share
 * and their sounds. They are built in the dark halls' own ash (DarkHallRegistry). Drawn by tools/draw_horror_art.py; the sounds
 * are made by tools/generate_horror_audio.py.
 */
public final class HorrorRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /**
     * The closing hall: the walls stepping in (stone grinding, with something breathing under it), the hall holding a hand's
     * width from a child, the squeeze cracking open, a child crying close by, a child calling far off, and a child's voice from
     * inside a wall.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> CLOSING_GRIND=sound("closing.grind"),CLOSING_HOLD=sound("closing.hold"),CLOSING_CRACK=sound("closing.crack"),
        CLOSING_CRY=sound("closing.cry"),CLOSING_CALL=sound("closing.call"),CLOSING_WALL=sound("closing.wall");
    /**
     * The storeroom (0.4.210): breathing an arm's length behind, the shelves dragged a block nearer, the bulb buzzing as it dies,
     * the spring door banging shut, a heartbeat, and a blow from behind.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> STORE_BREATH=sound("storeroom.breath"),STORE_SCRAPE=sound("storeroom.scrape"),STORE_FLICKER=sound("storeroom.flicker"),
        STORE_SLAM=sound("storeroom.slam"),STORE_HEART=sound("storeroom.heart"),STORE_BLOW=sound("storeroom.blow");
    /**
     * The stillness (0.4.211): something enormous breathing out over the reader, snuffling at the floor, the thud and grunt of its
     * head shoving at them, and the horns.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> STILL_BREATH=sound("stillness.breath"),STILL_SNIFF=sound("stillness.sniff"),STILL_NUDGE=sound("stillness.nudge"),
        STILL_GORE=sound("stillness.gore");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HorrorBlock>> FITTING_TYPE=CODECS.register("horror_fitting",()->HorrorBlock.CODEC);
    /** Every fitting of the five rooms, from Daisy's drawing to a handprint on the inside of a door. */
    public static final DeferredBlock<HorrorBlock> FITTING=BLOCKS.registerBlock("horror_fitting",HorrorBlock::new,fixed(Blocks.WHITE_WOOL).noOcclusion().lightLevel(HorrorBlock::light));
    private HorrorRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
