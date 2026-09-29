package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Will Navidson's letter and the snapshot tucked into it. */
public final class NavidsonLetter {
    public static final String TITLE = "Howdy, Neighbor";
    public static final String AUTHOR = "Will Navidson";

    /**
     * The mod ships this font as a plain reference to Minecraft's default, so
     * applying it is always safe; a resource pack can replace
     * {@code assets/the_oldest_house/font/navidson.json} with Navidson's hand.
     */
    public static final ResourceLocation FONT = HouseWriting.WILL_FONT;

    /**
     * One beat per page. The spec's page 2 wraps to 16 lines in the default
     * font (a page holds 14), so it is split after "We measured twice."
     */
    public static final List<String> PAGES = List.of(
            "Howdy, neighbor.\n\n"
                    + "Will Navidson here. We just moved in next door: Karen, me, two kids, and more boxes than "
                    + "four people should own. I take pictures for a living, so a flash from our porch at odd "
                    + "hours is only me.",
            "Strange thing. My brother Tom and I measured the place for shelves, inside and out. The inside "
                    + "comes up a quarter inch longer than the outside. We measured twice.",
            "Tom says it's the tape. I don't think it's the tape.\n\n"
                    + "Ever measure your place? Humor me.",
            "A favor. Our husky, Hillary, keeps slipping out. Gray, answers to her name when she feels like "
                    + "it.\n\n"
                    + "If she turns up at your door, would you hang on to her for us? She seems to know the way "
                    + "already.",
            "I've tucked in a picture I took from our porch last night. The light was too good to pass up. "
                    + "Hope you don't mind.\n\n"
                    + "Your little window up top was lit. You keep late hours too.",
            "Come by anytime. Door's always open.\n\n"
                    + "Your neighbor,\n"
                    + "Will Navidson\n\n"
                    + "P.S. Measured again this morning. Half an inch now."
    );

    public static final String SNAPSHOT_NAME = "A snapshot";
    public static final String SNAPSHOT_LORE = "On the back: \"Your place, from ours.\"";

    /**
     * The stock print (128 x 128 map colour ids), used only when the House
     * cannot photograph the player's own house; datapacks may replace it.
     */
    public static final ResourceLocation SNAPSHOT_ART =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "snapshot/porch.bin");

    private static final int MAP_PIXELS = 128 * 128;

    /**
     * Centre of the snapshot's (locked) map, far outside any playable area,
     * so it could never redraw real terrain.
     */
    private static final int SNAPSHOT_CENTER = 29_000_000;

    private NavidsonLetter() {
    }

    public static ItemStack createBook() {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (String page : PAGES) {
            pages.add(Filterable.passThrough(
                    HouseWriting.page(HouseWriting.WritingStyle.WILL, page)
            ));
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(TITLE),
                AUTHOR,
                0,
                pages,
                true
        ));
        return book;
    }

    /**
     * The snapshot as a locked map. {@code photo} is Navidson's photograph
     * of the player's house (128 x 128 map colours); without one, the stock
     * print is used.
     */
    public static ItemStack createSnapshot(ServerLevel level, @Nullable byte[] photo) {
        MapItemSavedData data = MapItemSavedData.createFresh(
                SNAPSHOT_CENTER,
                SNAPSHOT_CENTER,
                (byte) 0,
                false,
                false,
                HouseDimensions.INTERIOR
        );
        byte[] pixels = photo != null && photo.length == MAP_PIXELS ? photo : loadSnapshotPixels(level.getServer());
        System.arraycopy(pixels, 0, data.colors, 0, Math.min(pixels.length, data.colors.length));

        MapId id = level.getFreeMapId();
        level.setMapData(id, data.locked());

        ItemStack snapshot = new ItemStack(Items.FILLED_MAP);
        snapshot.set(DataComponents.MAP_ID, id);
        snapshot.set(DataComponents.CUSTOM_NAME, Component.literal(SNAPSHOT_NAME));
        snapshot.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(SNAPSHOT_LORE).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )));
        snapshot.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return snapshot;
    }

    public static byte[] loadSnapshotPixels(MinecraftServer server) {
        Optional<Resource> resource = server.getResourceManager().getResource(SNAPSHOT_ART);
        if (resource.isPresent()) {
            try (InputStream in = resource.get().open()) {
                byte[] bytes = in.readAllBytes();
                if (bytes.length == MAP_PIXELS) {
                    return bytes;
                }
                TheOldestHouse.LOGGER.warn("Snapshot art {} has {} bytes, expected {}.", SNAPSHOT_ART, bytes.length, MAP_PIXELS);
            } catch (IOException exception) {
                TheOldestHouse.LOGGER.warn("Could not read snapshot art {}.", SNAPSHOT_ART, exception);
            }
        }
        return fallbackPixels();
    }

    /** A dark frame with one warm window, if the baked art is missing. */
    private static byte[] fallbackPixels() {
        byte black = (byte) (29 * 4 + 3);
        byte window = (byte) (18 * 4 + 2);
        byte[] pixels = new byte[MAP_PIXELS];
        java.util.Arrays.fill(pixels, black);
        for (int y = 44; y < 54; y++) {
            for (int x = 76; x < 84; x++) {
                pixels[y * 128 + x] = window;
            }
        }
        return pixels;
    }
}
