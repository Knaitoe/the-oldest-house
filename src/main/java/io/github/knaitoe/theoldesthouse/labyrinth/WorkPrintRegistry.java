package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.203, Work print (docs/WORK_PRINT_0_4_203.md): the closed rental shop, its back room, edit bay, sound booth and set; its
 * one fitting block; the things a reader carries (the unlabelled tape, a blank, a copy, the exposed last scene, the House's
 * camera, the monster's costume and, once, a water-stained tape); and its sounds. Drawn by tools/draw_work_print_art.py;
 * the sounds are made by tools/generate_work_print_audio.py.
 */
public final class WorkPrintRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /**
     * A tape pushed into a deck, the deck's motor and heads, the tape coming out, the shop's phone, the snow on an empty
     * channel, the splicer's blade, the flatbed's motor, the record button's beep and the camera's run.
     */
    public static final DeferredHolder<SoundEvent,SoundEvent> INSERT=sound("insert"),DECK=sound("deck"),EJECT=sound("eject"),RING=sound("ring"),
        SNOW=sound("snow"),SPLICE=sound("splice"),FLATBED=sound("flatbed"),REC=sound("rec"),CAMERA=sound("camera");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("work_print."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"work_print."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /**
     * The shop's worn carpet tiles and painted wall panels, the ceiling's suspended tiles, the back room's concrete, the edit
     * bay's pegboard, the booth's acoustic foam and the set's plywood floor.
     */
    public static final List<String> MATERIAL_NAMES=List.of("carpet","wall","ceiling","concrete","pegboard","foam","plywood");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("work_print_"+name,fixed(name.equals("concrete")?Blocks.STONE:name.equals("foam")?Blocks.GRAY_WOOL:Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WorkPrintBlock>> FITTING_TYPE=CODECS.register("work_print_fitting",()->WorkPrintBlock.CODEC);
    /** Every fitting of the shop, from the tape shelves to the camera on its tripod. */
    public static final DeferredBlock<WorkPrintBlock> FITTING=BLOCKS.registerBlock("work_print_fitting",WorkPrintBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(WorkPrintBlock::light));
    /** The tape from the returns slot: no label, a rental card in the reader's own hand. One original a reader at a time. */
    public static final DeferredItem<Item> TAPE=ITEMS.registerSimpleItem("work_print_tape",new Item.Properties().stacksTo(1));
    /** A blank tape from the box in the back room, and the copy the dual deck makes on one. */
    public static final DeferredItem<Item> BLANK=ITEMS.registerSimpleItem("work_print_blank",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> COPY=ITEMS.registerSimpleItem("work_print_copy",new Item.Properties().stacksTo(1));
    /** The film's last scene, shot on the set and not yet spliced into the work print. */
    public static final DeferredItem<Item> SCENE=ITEMS.registerSimpleItem("work_print_scene",new Item.Properties().stacksTo(1));
    /** The House's camera, lifted off its tripod. Held up and used like a spyglass, it films what it is pointed at. */
    public static final DeferredItem<WorkPrintCamera> CAMERA_ITEM=ITEMS.registerItem("work_print_camera",WorkPrintCamera::new,new Item.Properties().stacksTo(1));
    /** The film's monster: stitched sacking, gloves a joint too long, a hood with no face. Worn over the reader's own skin. */
    public static final DeferredItem<WorkPrintCostume> COSTUME=ITEMS.registerItem("work_print_costume",WorkPrintCostume::new,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    /** Once, a second tape in the returns slot, with nothing on it but a ring-shaped water stain. */
    public static final DeferredItem<Item> STAINED=ITEMS.registerSimpleItem("work_print_stained",new Item.Properties().stacksTo(1));
    private WorkPrintRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
