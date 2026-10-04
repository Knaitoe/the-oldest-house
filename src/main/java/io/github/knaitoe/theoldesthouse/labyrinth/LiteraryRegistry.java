package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
/** Furniture, real cast and finite keepsakes of the remaining authored stories. */
public final class LiteraryRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryPropBlock>> PROP_TYPE=CODECS.register("literary_prop",()->LiteraryPropBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LiteraryFrozenBlock>> FROZEN_TYPE=CODECS.register("literary_frozen",()->LiteraryFrozenBlock.CODEC);
    public static final DeferredBlock<LiteraryFrozenBlock> FROZEN=BLOCKS.registerBlock("literary_frozen",LiteraryFrozenBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).dynamicShape().noOcclusion().noLootTable());
    public static final DeferredBlock<LiteraryPropBlock> PROP=BLOCKS.registerBlock("literary_prop",LiteraryPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LiteraryModelBlockEntity>> MODEL=BES.register("literary_model",()->BlockEntityType.Builder.of(LiteraryModelBlockEntity::new,PROP.get(),FROZEN.get()).build(null));
    public static final DeferredBlock<Block> VAULT_CRACK=material("usher_crack",Blocks.DEEPSLATE_BRICKS),RED_FLOOR=material("crimson_floor",Blocks.RED_TERRACOTTA),TAPE=material("elk_tape",Blocks.GRAY_WOOL),PEELED_TAPE=material("elk_tape_peeled",Blocks.GRAY_WOOL),DRAG_SNOW=material("father_drag_snow",Blocks.SNOW_BLOCK),CARVED_TRUNK=material("rabbit_carved_trunk",Blocks.OAK_PLANKS),SIDING=material("manufactured_siding",Blocks.OAK_PLANKS),DARK_PANEL=material("literary_dark_panel",Blocks.DARK_OAK_PLANKS);
    public static final DeferredItem<Item> TOOTH=item("elk_tooth"),KEY=item("wheel_house_key"),KNIFE=item("father_knife"),JAR=item("grasshopper_jar"),PHOTOGRAPH=item("literary_photograph"),FILM=item("family_film");
    public static final DeferredItem<LiteraryMealItem> MEAL=ITEMS.register("father_meal",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationModifier(.4F).alwaysEdible().build())));
    public static final DeferredItem<LiteraryMealItem> STEW=ITEMS.register("family_stew",()->new LiteraryMealItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(.2F).alwaysEdible().build())));
    public static final DeferredItem<Item> CYLINDER_ONE=item("wax_cylinder_one"),CYLINDER_TWO=item("wax_cylinder_two"),CYLINDER_THREE=item("wax_cylinder_three");
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryActor>> ACTOR=TYPES.register("literary_actor",()->EntityType.Builder.<LiteraryActor>of(LiteraryActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("literary_actor"));
    public static final DeferredHolder<EntityType<?>,EntityType<LiteraryElk>> ELK=TYPES.register("literary_elk",()->EntityType.Builder.<LiteraryElk>of(LiteraryElk::new,MobCategory.CREATURE).sized(1.25F,1.65F).clientTrackingRange(16).updateInterval(2).build("literary_elk"));
    public static final DeferredHolder<SoundEvent,SoundEvent> SCRATCH=sound("scratch"),DRIP=sound("drip"),PARTY=sound("party"),HOOVES=sound("hooves"),GIGGLE=sound("giggle"),BLIZZARD=sound("blizzard"),CYLINDER1=sound("cylinder_one"),CYLINDER2=sound("cylinder_two"),CYLINDER3=sound("cylinder_three"),FILM_SOUND=sound("family_film");
    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private static DeferredItem<Item> item(String id){var props=new Item.Properties().stacksTo(1);if(id.startsWith("wax_cylinder_")||id.equals("family_film"))props.jukeboxPlayable(net.minecraft.resources.ResourceKey.create(Registries.JUKEBOX_SONG,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id.replace("one","1").replace("two","2").replace("three","3"))));return ITEMS.registerSimpleItem(id,props);}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("literary."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"literary."+name)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);BES.register(bus);SOUNDS.register(bus);bus.addListener(LiteraryRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),LiteraryActor.attributes().build());e.put(ELK.get(),LiteraryElk.attributes().build());}
    private LiteraryRegistry(){}
}
