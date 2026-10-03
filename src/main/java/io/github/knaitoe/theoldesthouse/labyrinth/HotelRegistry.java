package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.world.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
public final class HotelRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HotelPropBlock>> PROP_TYPE=CODECS.register("hotel_prop",()->HotelPropBlock.CODEC);
    public static final DeferredBlock<HotelPropBlock> PROP=BLOCKS.registerBlock("hotel_prop",HotelPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredItem<Item> MASTER_KEY=ITEMS.registerSimpleItem("hotel_master_key",new Item.Properties().stacksTo(1));
    public static final DeferredItem<HotelDrinkItem> DRINK=ITEMS.register("hotel_drink",()->new HotelDrinkItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PHOTOGRAPH=ITEMS.registerSimpleItem("hotel_photograph",new Item.Properties().stacksTo(1));
    public static final DeferredHolder<EntityType<?>,EntityType<HotelActor>> ACTOR=TYPES.register("hotel_actor",()->EntityType.Builder.<HotelActor>of(HotelActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("hotel_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<HotelHose>> HOSE=TYPES.register("hotel_hose",()->EntityType.Builder.<HotelHose>of(HotelHose::new,MobCategory.MISC).sized(1.1F,.22F).clientTrackingRange(12).updateInterval(2).build("hotel_hose"));
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("hotel."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel."+name)));}
    public static final DeferredHolder<SoundEvent,SoundEvent> PIANO=sound("piano"),ORCHESTRA=sound("orchestra"),BELL=sound("bell"),RUBBER=sound("hose");
    private HotelRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);bus.addListener(HotelRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),HotelActor.attributes().build());e.put(HOSE.get(),HotelHose.attributes().build());}
}
