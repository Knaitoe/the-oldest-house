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
    private JournalRegistry(){}
    public static void register(IEventBus bus){ITEMS.register(bus);}
}
