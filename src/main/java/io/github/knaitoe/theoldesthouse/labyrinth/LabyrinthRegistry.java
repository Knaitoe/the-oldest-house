package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items the labyrinth's vignettes hand out. */
public final class LabyrinthRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);

    public static final DeferredItem<BlindfoldItem> BLINDFOLD =
            ITEMS.register("blindfold", () -> new BlindfoldItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<HarriganPhoneItem> PHONE =
            ITEMS.register("phone", () -> new HarriganPhoneItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> HARRIGANS_PHONE =
            ITEMS.registerSimpleItem("harrigans_phone", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SCRATCH_TICKET =
            ITEMS.registerSimpleItem("scratch_ticket", new Item.Properties().stacksTo(1));

    public static final DeferredItem<NavigationItems.Chalk> CHALK =
            ITEMS.register("chalk", () -> new NavigationItems.Chalk(new Item.Properties().durability(64)));
    public static final DeferredItem<NavigationItems.Spool> TRAIL_SPOOL =
            ITEMS.register("trail_spool", () -> new NavigationItems.Spool(new Item.Properties().durability(192)));

    private LabyrinthRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
