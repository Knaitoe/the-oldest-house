package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.217, Polybius (docs/POLYBIUS_0_4_217.md): the arcade in the strip mall, its carpet, its walls and its ceiling, and its one
 * fitting block, from the cabinets to the change machine. Drawn by tools/draw_polybius_art.py. The arcade makes its noises with
 * vanilla's own sounds.
 */
public final class PolybiusRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The arcade's black carpet with its scattered neon shapes, its dark painted walls, and its ceiling of black tiles. */
    public static final List<String> MATERIAL_NAMES=List.of("carpet","wall","ceiling");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("polybius_"+name,fixed(name.equals("carpet")?Blocks.BLACK_WOOL:Blocks.OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PolybiusBlock>> FITTING_TYPE=CODECS.register("polybius_fitting",()->PolybiusBlock.CODEC);
    /** Every fitting of the arcade. */
    public static final DeferredBlock<PolybiusBlock> FITTING=BLOCKS.registerBlock("polybius_fitting",PolybiusBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(PolybiusBlock::light));
    /**
     * 0.4.221 (owner: "Add tickets/ticket exchange"): prize tickets, paid out by every game, and the eight prizes on the shelf
     * behind the counter, each with its price; the shelf's cells hold them in this order, the lower row first.
     */
    public static final DeferredItem<net.minecraft.world.item.Item> TICKETS=ITEMS.registerSimpleItem("polybius_tickets",new net.minecraft.world.item.Item.Properties());
    public static final List<String> PRIZE_NAMES=List.of("spider","finger_trap","bouncy_ball","glow_star","slap_bracelet","kazoo","mood_ring","plush_dog");
    public static final int[] PRIZE_COST={5,5,10,10,15,20,25,40};
    /** 0.4.411: each prize does something (PolybiusPrize): the bracelet and the ring are worn, the others played with or set down. */
    public static final List<DeferredItem<PolybiusPrize>> PRIZES=java.util.stream.IntStream.range(0,PRIZE_NAMES.size()).mapToObj(i->ITEMS.register("polybius_prize_"+PRIZE_NAMES.get(i),
        ()->new PolybiusPrize(PolybiusPrize.Kind.values()[i],new net.minecraft.world.item.Item.Properties().stacksTo(16)))).toList();
    private static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    /** A spider, a dog or a star set down in the world, and a ball in flight. */
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<PrizeProp>> PROP=TYPES.register("polybius_prize_prop",
        ()->net.minecraft.world.entity.EntityType.Builder.<PrizeProp>of(PrizeProp::new,net.minecraft.world.entity.MobCategory.MISC).sized(.45F,.3F).clientTrackingRange(8).updateInterval(20).build("polybius_prize_prop"));
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<PrizeBall>> BALL=TYPES.register("polybius_bouncy_ball",
        ()->net.minecraft.world.entity.EntityType.Builder.<PrizeBall>of(PrizeBall::new,net.minecraft.world.entity.MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(6).updateInterval(1).build("polybius_bouncy_ball"));
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The prizes' own noises (tools/generate_prize_audio.py). */
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> KAZOO=sound("prize.kazoo"),BOING=sound("prize.boing"),SQUEAK=sound("prize.squeak"),SLAP=sound("prize.slap"),CREAK=sound("prize.creak"),POP=sound("prize.pop");
    /**
     * 0.4.224 (owner: "add a pizza and soda booth/vendor, too! describe the pizza as overly greasy"): a slice off the warmer and a
     * fountain soda, sold for tickets at the pizza counter.
     */
    public static final DeferredItem<PolybiusFood> PIZZA=ITEMS.register("polybius_pizza",()->new PolybiusFood(new net.minecraft.world.item.Item.Properties().stacksTo(16)
        .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(.3F).build()),false));
    public static final DeferredItem<PolybiusFood> SODA=ITEMS.register("polybius_soda",()->new PolybiusFood(new net.minecraft.world.item.Item.Properties().stacksTo(16)
        .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(.1F).alwaysEdible()
            .effect(()->new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,300,0),1).build()),true));
    public static final int PIZZA_COST=4,SODA_COST=3;
    /**
     * 0.4.413 (docs/POLYBIUS_HOME_CABINET_0_4_413.md): the service key under the men's clipboard, and the cabinet it makes at a
     * crafting table: an ordinary block, breakable, that plays the arcade's three working games wherever its reader puts it.
     */
    public static final DeferredItem<ServiceKeyItem> SERVICE_KEY=ITEMS.register("polybius_service_key",()->new ServiceKeyItem(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HomeCabinetBlock>> HOME_CABINET_TYPE=CODECS.register("polybius_home_cabinet",()->HomeCabinetBlock.CODEC);
    public static final DeferredBlock<HomeCabinetBlock> HOME_CABINET=BLOCKS.registerBlock("polybius_home_cabinet",HomeCabinetBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2F,3F).noOcclusion().lightLevel(HomeCabinetBlock::light).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredItem<net.minecraft.world.item.DoubleHighBlockItem> HOME_CABINET_ITEM=ITEMS.register("polybius_home_cabinet",()->new net.minecraft.world.item.DoubleHighBlockItem(HOME_CABINET.get(),new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<HomeCabinetBlockEntity>> HOME_CABINET_ENTITY=BLOCK_ENTITIES.register("polybius_home_cabinet",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(HomeCabinetBlockEntity::new,HOME_CABINET.get()).build(null));
    /** The recipe the key makes, given to its reader's recipe book with it. */
    public static final net.minecraft.resources.ResourceLocation HOME_CABINET_RECIPE=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius_home_cabinet");
    private PolybiusRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);BLOCK_ENTITIES.register(bus);}
}
