package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** 0.4.173, "Third Floor Girls'": the school's third floor, its girls' bathroom and the dare's candle. Drawn by tools/draw_dare_in_the_dark.py. */
public final class DareRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** Muffled through the door: giggling, whispering and feet; the latch, the handle, the breath at the neck, the door thrown open and the glass. */
    public static final DeferredHolder<SoundEvent,SoundEvent> GIGGLE=sound("giggle"),WHISPER=sound("whisper"),SHUFFLE=sound("shuffle"),LATCH=sound("latch"),RATTLE=sound("rattle"),BREATH=sound("breath"),SLAM=sound("slam"),SHATTER=sound("shatter"),FLAME=sound("flame");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("dare."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dare."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** Painted block wall and its darker band, the hall's vinyl tile and acoustic ceiling, the bathroom's hex floor and glazed wall, and the hall's trim. */
    public static final List<String> MATERIAL_NAMES=List.of("wall","wall_band","hall_tile","ceiling","bath_floor","bath_wall","trim");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("dare_"+name,fixed(Blocks.WHITE_TERRACOTTA)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DareFittingBlock>> FITTING_TYPE=CODECS.register("dare_fitting",()->DareFittingBlock.CODEC);
    public static final DeferredBlock<DareFittingBlock> FITTING=BLOCKS.registerBlock("dare_fitting",DareFittingBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(DareFittingBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DareMirrorBlock>> MIRROR_TYPE=CODECS.register("dare_mirror",()->DareMirrorBlock.CODEC);
    public static final DeferredBlock<DareMirrorBlock> MIRROR=BLOCKS.registerBlock("dare_mirror",DareMirrorBlock::new,fixed(Blocks.GLASS).noOcclusion());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DareMirrorBlock.Glass>> MIRROR_ENTITY=BES.register("dare_mirror",()->BlockEntityType.Builder.of(DareMirrorBlock.Glass::new,MIRROR.get()).build(null));
    /** The heavy bathroom door. Shut, it lets no light through; once the third count is said, its handle is held from the hall. */
    public static final DeferredBlock<DareDoorBlock> BATHROOM_DOOR=BLOCKS.registerBlock("dare_bathroom_door",DareDoorBlock::new,fixed(Blocks.OAK_DOOR).noOcclusion());
    /** The stairwell door at the end of the hall, with wired glass: the way on. */
    public static final DeferredBlock<DoorBlock> STAIR_DOOR=BLOCKS.register("dare_stair_door",()->new DoorBlock(BlockSetType.OAK,fixed(Blocks.OAK_DOOR).noOcclusion()));
    /** The candle stub the kids hand over: held, it lights the reader's face and the glass. */
    public static final DeferredItem<Item> CANDLE=ITEMS.registerSimpleItem("candle_stub",new Item.Properties().stacksTo(1));
    private DareRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BES.register(bus);SOUNDS.register(bus);}
}
