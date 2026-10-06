package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class NovelRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredBlock<Block> GOUGES=material("cell_gouges",Blocks.DEEPSLATE_TILES),SEALED_WINDOW=material("sealed_window",Blocks.DARK_OAK_PLANKS),
        INSTITUTE=material("institute_paint",Blocks.WHITE_TERRACOTTA),PLASTER=material("collapse_plaster",Blocks.WHITE_TERRACOTTA),
        CARVINGS=material("well_carvings",Blocks.OAK_PLANKS),PAPER=material("archive_paper",Blocks.WHITE_TERRACOTTA);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NovelPropBlock>> PROP_TYPE=CODECS.register("novel_prop",()->NovelPropBlock.CODEC);
    public static final DeferredBlock<NovelPropBlock> PROP=BLOCKS.registerBlock("novel_prop",NovelPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredItem<Item> COLLAR=ITEMS.registerSimpleItem("cat_collar",new Item.Properties().stacksTo(1)),RIBBON=ITEMS.registerSimpleItem("well_ribbon",new Item.Properties().stacksTo(1)),
        ARCHIVE_KEY=ITEMS.registerSimpleItem("archive_key",new Item.Properties().stacksTo(1));
    public static final DeferredItem<ShieldItem> HOLLOWAY_SHIELD=ITEMS.register("holloway_shield",()->new ShieldItem(new Item.Properties().durability(336)));
    public static final DeferredItem<FlintAndSteelItem> LIGHTER=ITEMS.register("lighter",()->new FlintAndSteelItem(new Item.Properties().durability(64)));
    public static final DeferredItem<WalkieTalkieItem> RADIO=ITEMS.register("walkie_talkie",()->new WalkieTalkieItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<EntityType<?>,EntityType<NovelVulture>> VULTURE=TYPES.register("vulture",()->EntityType.Builder.<NovelVulture>of(NovelVulture::new,MobCategory.MISC).sized(.9F,.45F).clientTrackingRange(16).updateInterval(2).build("vulture"));
    public static final DeferredHolder<EntityType<?>,EntityType<NovelActor>> ACTOR=TYPES.register("novel_actor",()->EntityType.Builder.<NovelActor>of(NovelActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(12).updateInterval(2).build("novel_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<HouseHuman>> HUMAN=TYPES.register("house_human",()->EntityType.Builder.<HouseHuman>of(HouseHuman::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("house_human"));
    public static final DeferredHolder<SoundEvent,SoundEvent> MONITOR=sound("novel.monitor"),SHUTTER=sound("novel.shutter"),RADIO_STATIC=sound("novel.radio"),COLLAPSE=sound("novel.collapse");
    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private NovelRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);bus.addListener(NovelRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(VULTURE.get(),NovelVulture.attributes().build());e.put(ACTOR.get(),NovelActor.attributes().build());e.put(HUMAN.get(),HouseHuman.attributes().build());}
}
