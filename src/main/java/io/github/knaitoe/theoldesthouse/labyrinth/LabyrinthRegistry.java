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

    private LabyrinthRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
