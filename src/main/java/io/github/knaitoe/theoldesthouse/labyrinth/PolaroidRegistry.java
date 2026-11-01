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
 * 0.4.204, the Polaroid Trail (docs/POLAROID_TRAIL_0_4_204.md): the back yard, the crawlspace next door, the neighbourhood
 * cinema and the pine wood; their one fitting block; the developed photograph and Josh's walkie-talkie; and the sounds. Drawn
 * by tools/draw_polaroid_trail_art.py; the sounds are made by tools/generate_polaroid_trail_audio.py.
 */
public final class PolaroidRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** The flash charging (a thin rising whine), the flash, the shutter's click, a balloon's rubber, Josh's walkie, the projector and the power going. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CHARGE=sound("charge"),FLASH=sound("flash"),CLICK=sound("click"),BALLOON=sound("balloon"),
        WALKIE_SOUND=sound("walkie"),PROJECTOR=sound("projector"),POWER=sound("power");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("polaroid."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polaroid."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** Corrugated culvert steel, crawlspace clay, damp red brick, pink insulation, the cinema's carpet and walls, velvet curtain and clapboard siding. */
    public static final List<String> MATERIAL_NAMES=List.of("culvert","clay","pier_brick","insulation","cinema_carpet","cinema_wall","velvet","siding");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("polaroid_"+name,fixed(name.equals("culvert")?Blocks.IRON_BLOCK:name.equals("clay")?Blocks.MUD:name.equals("pier_brick")?Blocks.BRICKS:name.equals("insulation")||name.equals("velvet")?Blocks.PINK_WOOL:Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PolaroidBlock>> FITTING_TYPE=CODECS.register("polaroid_fitting",()->PolaroidBlock.CODEC);
    /** Every fitting of the trail, from the balloon tag on the porch rail to the photo album in the deer blind. */
    public static final DeferredBlock<PolaroidBlock> FITTING=BLOCKS.registerBlock("polaroid_fitting",PolaroidBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(PolaroidBlock::light));
    /** A developed photograph: the reader, from where the watcher stood, half a minute before. */
    public static final DeferredItem<PolaroidPhotoItem> PHOTO=ITEMS.registerItem("polaroid_photo",PolaroidPhotoItem::new,new Item.Properties().stacksTo(1));
    /** Josh's walkie-talkie, kept by the reader who looked up from the trench. */
    public static final DeferredItem<PolaroidWalkieItem> WALKIE=ITEMS.registerItem("polaroid_walkie",PolaroidWalkieItem::new,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    private PolaroidRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
