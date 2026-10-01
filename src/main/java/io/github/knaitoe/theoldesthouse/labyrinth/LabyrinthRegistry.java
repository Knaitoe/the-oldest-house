package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Items the labyrinth's vignettes hand out. */
public final class LabyrinthRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final ResourceLocation HEARTBEAT_ID=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vignette.heartbeat");
    public static final DeferredHolder<SoundEvent,SoundEvent> FLOORBOARD_HEARTBEAT=SOUNDS.register("vignette.heartbeat",()->SoundEvent.createVariableRangeEvent(HEARTBEAT_ID));

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
        SOUNDS.register(modEventBus);
    }
}
