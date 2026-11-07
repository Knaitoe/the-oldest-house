package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 0.4.331: what the familiar kitchen's stove makes of more than one thing in the pan. Drawn by tools/draw_kitchen_dishes.py. */
public final class KitchenRegistry {
    private KitchenRegistry(){}
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    /** One dish; what it is (texture, seasoning, how well cooked, who made it) and how much it fills are set on each stack. */
    public static final DeferredItem<Item> DISH=ITEMS.registerSimpleItem("kitchen_dish",new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(4).saturationModifier(.6F).build()));
    public static void register(IEventBus bus){ITEMS.register(bus);}
}
