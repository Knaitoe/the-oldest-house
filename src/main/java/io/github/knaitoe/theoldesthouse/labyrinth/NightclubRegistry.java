package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
public final class NightclubRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NightclubBlock>> TYPE=CODECS.register("nightclub",()->NightclubBlock.CODEC);
    public static final DeferredBlock<NightclubBlock> BLOCK=BLOCKS.registerBlock("nightclub",NightclubBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CONCRETE).noOcclusion().noLootTable().lightLevel(NightclubBlock::light));
    public static final DeferredItem<NightclubGlassItem> GLASS=ITEMS.registerItem("nightclub_glass",NightclubGlassItem::new,new Item.Properties().stacksTo(1));
    public static final DeferredHolder<EntityType<?>,EntityType<NightclubActor>> ACTOR=TYPES.register("nightclub_actor",()->EntityType.Builder.<NightclubActor>of(NightclubActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("nightclub_actor"));
    public static final DeferredHolder<SoundEvent,SoundEvent> BEAT=SOUNDS.register("nightclub.beat",()->SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"nightclub.beat"),64));
    private NightclubRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);bus.addListener(NightclubRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),NightclubActor.attributes().build());}
}
