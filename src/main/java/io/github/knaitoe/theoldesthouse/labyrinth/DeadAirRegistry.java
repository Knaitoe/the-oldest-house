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
 * 0.4.206, the Dead Air Broadcast (docs/DEAD_AIR_0_4_206.md): the basement's, the soundstage's and the marsh's materials, the
 * one fitting block, the brass amplifier tube, and the sounds. Drawn by tools/draw_dead_air_art.py; the sounds are made by
 * tools/generate_dead_air_audio.py.
 */
public final class DeadAirRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** Snow on an empty channel, the dial's click, dead air's flat tone, the feedback burst, Percy's jaw, the strings' creak and a breaker thrown. */
    public static final DeferredHolder<SoundEvent,SoundEvent> STATIC=sound("static"),TUNE=sound("tune"),DEAD=sound("dead"),FEEDBACK=sound("feedback"),
        CLACK=sound("clack"),CREAK=sound("creak"),BREAKER_SOUND=sound("breaker");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("dead_air."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dead_air."+name)));}
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** Walnut paneling, mustard shag, faux brick, the black studio, rotting velvet, stencilled plywood, storm-drain concrete and marsh mud. */
    public static final List<String> MATERIAL_NAMES=List.of("walnut","shag","faux_brick","studio","velvet","plywood","drain","mud");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("dead_air_"+name,fixed(name.equals("shag")||name.equals("velvet")?Blocks.YELLOW_WOOL:name.equals("drain")||name.equals("faux_brick")?Blocks.STONE:name.equals("mud")?Blocks.MUD:Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DeadAirBlock>> FITTING_TYPE=CODECS.register("dead_air_fitting",()->DeadAirBlock.CODEC);
    /** Every fitting of the broadcast, from the shortwave receiver on its folding table to the nine monitors in the master booth. */
    public static final DeferredBlock<DeadAirBlock> FITTING=BLOCKS.registerBlock("dead_air_fitting",DeadAirBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(DeadAirBlock::light));
    /** The brass amplifier tube from the chest at the stage's end, which lets the receiver reach the marsh. */
    public static final DeferredItem<Item> TUBE=ITEMS.registerSimpleItem("dead_air_tube",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    /** The dead-air breaker, kept by the reader who stood in the master booth: eight seconds unseen, for its owner. */
    public static final DeferredItem<DeadAirBreakerItem> QUIET=ITEMS.registerItem("dead_air_breaker",DeadAirBreakerItem::new,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    private DeadAirRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
