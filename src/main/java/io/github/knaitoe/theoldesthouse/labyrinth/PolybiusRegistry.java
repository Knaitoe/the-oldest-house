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
    public static final List<DeferredItem<net.minecraft.world.item.Item>> PRIZES=PRIZE_NAMES.stream().map(n->ITEMS.registerSimpleItem("polybius_prize_"+n,new net.minecraft.world.item.Item.Properties().stacksTo(16))).toList();
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
    private PolybiusRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);}
}
