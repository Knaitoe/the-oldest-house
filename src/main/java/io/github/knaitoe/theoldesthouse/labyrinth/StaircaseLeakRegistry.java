package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class StaircaseLeakRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredBlock<Block> WALLPAPER=BLOCKS.register("leak_wallpaper",()->new Block(BlockBehaviour.Properties.of().strength(.8F)));
    public static final DeferredBlock<StaircaseLeakProps> PROP=BLOCKS.register("leak_prop",()->new StaircaseLeakProps(BlockBehaviour.Properties.of().noOcclusion().noLootTable().strength(.4F)
            .lightLevel(s->switch(s.getValue(StaircaseLeakProps.KIND)){case LIGHT->14;case SMALL_LIGHT->6;default->0;})));
    public static final DeferredItem<Item> SHEET=ITEMS.registerSimpleItem("leak_reading_sheet");
    public static final List<DeferredItem<Item>> SOCKS=List.of(ITEMS.registerSimpleItem("leak_sock_blue"),ITEMS.registerSimpleItem("leak_sock_ochre"),ITEMS.registerSimpleItem("leak_sock_grey"),ITEMS.registerSimpleItem("leak_sock_green"),ITEMS.registerSimpleItem("leak_sock_red"),ITEMS.registerSimpleItem("leak_sock_single"));
    public static final DeferredHolder<EntityType<?>,EntityType<StaircaseReaderEcho>> READER=ENTITIES.register("staircase_reader",()->EntityType.Builder.<StaircaseReaderEcho>of(StaircaseReaderEcho::new,MobCategory.MISC).sized(.6F,1.8F).eyeHeight(1.62F).clientTrackingRange(12).updateInterval(3).build("staircase_reader"));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("leak."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"leak."+name)));}
    public static final DeferredHolder<SoundEvent,SoundEvent> DRIP=sound("drip"),CLOCK=sound("clock"),RAIN=sound("rain"),RADIO=sound("radio"),BAG=sound("bag");
    private StaircaseLeakRegistry(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);bus.addListener((EntityAttributeCreationEvent e)->e.put(READER.get(),StaircaseReaderEcho.attributes().build()));}
}
