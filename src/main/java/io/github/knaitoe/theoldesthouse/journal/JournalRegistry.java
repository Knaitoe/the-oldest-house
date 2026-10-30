package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The reader's journal (JOURNAL_DESIGN.md). Its icon's five wear stages are drawn by tools/draw_journal_art.py. */
public final class JournalRegistry {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    public static final DeferredItem<JournalItem> JOURNAL=ITEMS.registerItem("journal",JournalItem::new,new Item.Properties().stacksTo(1));
    /** 0.4.178: a reader's own copy of one of Zampano's courtyard leaves (ZampanoLeaves); its icon is drawn by tools/draw_zampano_cabinet_art.py. */
    public static final DeferredItem<ZampanoLeafItem> ZAMPANO_LEAF=ITEMS.registerItem("zampano_leaf",ZampanoLeafItem::new,new Item.Properties().stacksTo(1));
    /** 0.4.199: a page torn from a reader's journal (TornPages); its icon is drawn by tools/draw_journal_art.py. */
    public static final DeferredItem<TornPageItem> TORN_PAGE=ITEMS.registerItem("torn_page",TornPageItem::new,new Item.Properties().stacksTo(1));
    private static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    /** A torn page pinned to a wall or lying on a table. */
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<PinnedPage>> PINNED_PAGE=TYPES.register("pinned_page",
            ()->net.minecraft.world.entity.EntityType.Builder.<PinnedPage>of(PinnedPage::new,net.minecraft.world.entity.MobCategory.MISC).sized(.6F,.75F).clientTrackingRange(8).updateInterval(Integer.MAX_VALUE).build("pinned_page"));
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    /** 0.4.199: the ringing in the ears after the worst rooms (Grounding), from tools/generate_journal_audio.py. */
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> STATIC=SOUNDS.register("journal.static",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal.static")));
    private JournalRegistry(){}
    public static void register(IEventBus bus){ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);}
}
