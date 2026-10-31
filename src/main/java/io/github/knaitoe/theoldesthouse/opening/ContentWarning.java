package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.201: the content warning, at the very beginning. The first time a player joins a world with the mod, a short book
 * opens in the native lectern view: what the mod is, what it contains, and that a few rooms ask again before they go
 * further. It closes like any book (Done or Escape), and once closed it is not shown to that player again.
 *
 * <p>It waits a few seconds after login and for no other screen to be open, so the world has finished loading behind it.
 * Game tests and the opt-in proof runs (any {@code the_oldest_house.*} system property) never see it.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class ContentWarning {
    /** Kept in the player's persisted data, so it survives death and respawn. */
    public static final String SEEN = "ContentWarningSeen201";
    private static final int DELAY = 60;
    private static final Set<UUID> PENDING = new HashSet<>();

    private ContentWarning() {}

    public static ItemStack book() {
        return HouseWriting.book("Before you go in", "This House Has Ghosts in It", HouseWriting.WritingStyle.PLAIN, List.of(
                "This House Has Ghosts in It is a horror mod.\n\nIt is quiet for long stretches, and then it is not. Its rooms are made from stories, and many of them are about loss.",
                "Inside there is death and grief, violence and gore, and body horror.\n\nThere is harm to children and to animals, abduction, and abuse, including sexual abuse, which is never shown.",
                "There is drowning, suffocation and being shut in, dark and cramped places, sudden loud sounds and flashing lights.\n\nA few rooms ask again before they go further.",
                "Close this book to begin. It will not be shown again."));
    }

    public static boolean seen(ServerPlayer p) { return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(SEEN); }
    static void markSeen(ServerPlayer p) {
        var all = p.getPersistentData();var kept = all.getCompound(Player.PERSISTED_NBT_TAG);kept.putBoolean(SEEN, true);all.put(Player.PERSISTED_NBT_TAG, kept);
    }
    /** Proof runs and game tests drive their own players; the warning is for people. */
    static boolean quiet(net.minecraft.server.MinecraftServer s) {
        return s instanceof GameTestServer || System.getProperties().stringPropertyNames().stream().anyMatch(k -> k.startsWith("the_oldest_house."));
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !quiet(p.server) && !seen(p)) PENDING.add(p.getUUID());
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        if (PENDING.isEmpty()) return;
        var list = e.getServer().getPlayerList();
        PENDING.removeIf(id -> {
            var p = list.getPlayer(id);if (p == null) return true;
            if (seen(p)) return true;
            if (p.tickCount < DELAY || p.containerMenu != p.inventoryMenu || p.isSleeping()) return false;
            open(p);return true;
        });
    }
    /** The book in the native lectern view: turn its pages and close it; nothing can be taken from it. */
    public static void open(ServerPlayer p) {
        var book = book();var content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        p.openMenu(new SimpleMenuProvider((id, inventory, player) -> new Reader(id, p, book), Component.literal(content.title().raw())));
    }

    static final class Reader extends LecternMenu {
        private final ServerPlayer reader;private final int pages;
        Reader(int id, ServerPlayer reader, ItemStack book) { this(id, reader, book, new SimpleContainer(1)); }
        private Reader(int id, ServerPlayer reader, ItemStack book, SimpleContainer display) {
            super(id, display, new SimpleContainerData(1));this.reader = reader;
            pages = book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();display.setItem(0, book);
        }
        @Override public boolean stillValid(Player p) { return p == reader && reader.isAlive(); }
        @Override public boolean clickMenuButton(Player p, int button) {
            if (!stillValid(p) || button == 3) return false;
            if (button >= 100) { if (button - 100 >= pages) return false; }
            else if (button == 1) { if (getPage() <= 0) return false; }
            else if (button == 2) { if (getPage() >= pages - 1) return false; }
            else return false;
            return super.clickMenuButton(p, button);
        }
        @Override public void removed(Player p) { super.removed(p);if (p == reader) markSeen(reader); }
    }
}
