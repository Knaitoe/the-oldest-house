package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Original surfaces and the one continuously positioned industrial recording. No victim items. */
public final class BorrascaRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    /** 0.4.435: a child's bike while somebody rides it (BorrascaBike). Never saved: a bike nobody is on is a block. */
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<BorrascaBike>> BIKE=ENTITIES.register("borrasca_bike",
        ()->net.minecraft.world.entity.EntityType.Builder.<BorrascaBike>of(BorrascaBike::new,net.minecraft.world.entity.MobCategory.MISC).sized(.6F,.9F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("borrasca_bike"));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaFittingBlock>> FITTING_TYPE=CODECS.register("borrasca_fitting",()->BorrascaFittingBlock.CODEC);
    public static final DeferredBlock<BorrascaFittingBlock> FITTING=BLOCKS.registerBlock("borrasca_fitting",BorrascaFittingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noLootTable().lightLevel(s->s.getValue(BorrascaFittingBlock.KIND)==BorrascaFittingBlock.Kind.LAMP?13:0));
    /** 0.4.422: the shops' fittings (BorrascaShopBlock) and three of their surfaces, drawn by tools/draw_borrasca_town_art.py. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaShopBlock>> SHOP_TYPE=CODECS.register("borrasca_shop",()->BorrascaShopBlock.CODEC);
    public static final DeferredBlock<BorrascaShopBlock> SHOP=BLOCKS.registerBlock("borrasca_shop",BorrascaShopBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noLootTable().lightLevel(BorrascaShopBlock::light));
    /** The diner's black and white floor, the doctor's and the drugstore's linoleum, the antiques shop's pressed tin ceiling. */
    public static final DeferredBlock<Block> CHECKER=BLOCKS.registerSimpleBlock("borrasca_checker",BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(-1,3600000).noLootTable());
    public static final DeferredBlock<Block> LINO=BLOCKS.registerSimpleBlock("borrasca_lino",BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(-1,3600000).noLootTable());
    public static final DeferredBlock<Block> TIN=BLOCKS.registerSimpleBlock("borrasca_tin",BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaMachineBlock>> MACHINE_TYPE=CODECS.register("borrasca_machine",()->BorrascaMachineBlock.CODEC);
    public static final DeferredBlock<BorrascaMachineBlock> MACHINE=BLOCKS.registerBlock("borrasca_machine",BorrascaMachineBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BorrascaMachineBlock.Machine>> MACHINE_ENTITY=BES.register("borrasca_machine",()->BlockEntityType.Builder.of(BorrascaMachineBlock.Machine::new,MACHINE.get()).build(null));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrascaCarvingBlock>> CARVING_TYPE=CODECS.register("borrasca_bark",()->BorrascaCarvingBlock.CODEC);
    /** 0.4.448: bark with names cut in it, all over the Triple Tree. */
    public static final DeferredBlock<BorrascaNamesBlock> NAMES=BLOCKS.registerBlock("borrasca_names",BorrascaNamesBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).strength(-1,3600000).noLootTable());
    public static final DeferredBlock<BorrascaCarvingBlock> CARVING=BLOCKS.registerBlock("borrasca_bark",BorrascaCarvingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(-1,3600000).noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BorrascaCarvingEntity>> CARVING_ENTITY=BES.register("borrasca_bark",()->BlockEntityType.Builder.of(BorrascaCarvingEntity::new,CARVING.get()).build(null));
    public static final DeferredItem<BorrascaKeepsakeItem> COMPASS=ITEMS.register("borrasca_compass",()->new BorrascaKeepsakeItem(new Item.Properties().stacksTo(1).fireResistant(),"Compass"));
    public static final DeferredItem<BorrascaKeepsakeItem> PATCH=ITEMS.register("borrasca_name_patch",()->new BorrascaKeepsakeItem(new Item.Properties().stacksTo(1).fireResistant(),"Patch"));
    /** 0.4.445: the comic the friend stands in the reader's window of the fort (BorrascaComic). The reader's own, like her compass. */
    public static final DeferredItem<BorrascaKeepsakeItem> COMIC=ITEMS.register("borrasca_comic",()->new BorrascaKeepsakeItem(new Item.Properties().stacksTo(1).fireResistant(),"Comic"));
    public static final DeferredItem<Item> FLASHLIGHT=ITEMS.registerSimpleItem("borrasca_flashlight",new Item.Properties().stacksTo(1));
    /** 0.4.425: the money a reader carries in the town, and what the shops sell (BorrascaPeople, BorrascaFood). */
    public static final DeferredItem<BorrascaPurseItem> COINS=ITEMS.register("borrasca_coins",()->new BorrascaPurseItem(false)),WALLET=ITEMS.register("borrasca_wallet",()->new BorrascaPurseItem(true));
    public static final DeferredItem<BorrascaFood> PIE=food("pie",BorrascaFood.Kind.PIE),COFFEE=food("coffee",BorrascaFood.Kind.COFFEE),MILK_COFFEE=food("milk_coffee",BorrascaFood.Kind.MILK_COFFEE),SODA=food("soda",BorrascaFood.Kind.SODA),
        CANDY=food("candy",BorrascaFood.Kind.CANDY),LOLLIPOP=food("lollipop",BorrascaFood.Kind.LOLLIPOP),MALT=food("malt",BorrascaFood.Kind.MALT),BEER=food("beer",BorrascaFood.Kind.BEER);
    private static DeferredItem<BorrascaFood> food(String id,BorrascaFood.Kind kind){return ITEMS.register("borrasca_"+id,()->new BorrascaFood(kind));}
    public static final DeferredHolder<SoundEvent,SoundEvent> GRINDER=sound("grinder"),GRINDER_MUFFLED=sound("grinder_muffled"),MOTOR_STOP=sound("motor_stop"),TREE_CREAK=sound("tree_creak"),TOWN_WIND=sound("town_wind"),CHIME=sound("triple_chime"),STROKE=sound("stroke"),CARVE=sound("carve"),BREAKER=sound("breaker"),CHAIN=sound("chain"),MACHINE_WET=sound("machine_wet");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register("borrasca."+id,()->id.equals("triple_chime")||id.equals("stroke")?SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca."+id),256):SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca."+id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);SOUNDS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
    private BorrascaRegistry(){}
}
