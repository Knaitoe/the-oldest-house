package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * One rendering vocabulary for the House's authored writing.
 *
 * The fonts are bitmap providers rendered by Minecraft's ordinary text/book
 * renderer. There is no custom framebuffer, emissive layer, translucent
 * world quad or shader hook here, deliberately: Iris/Oculus-style shaders
 * should see these pages exactly as they see vanilla book text.
 */
public final class HouseWriting {
    /**
     * Kept at the original id so resource packs that already replace
     * Navidson's hand continue to work. navidson.json now references the
     * authored Will atlas.
     */
    public static final ResourceLocation WILL_FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "navidson");
    public static final ResourceLocation KAREN_FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "karen");
    public static final ResourceLocation ZAMPANO_FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "zampano");
    public static final ResourceLocation CHILD_FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "child");
    public static final ResourceLocation CLAW_FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "claw");

    public static final ResourceLocation PELAFINA_FONT=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pelafina");

    public enum WritingStyle {
        PLAIN(null),
        WILL(WILL_FONT),
        KAREN(KAREN_FONT),
        ZAMPANO(ZAMPANO_FONT),
        CHILD(CHILD_FONT),
        CLAW(CLAW_FONT),
        PELAFINA(PELAFINA_FONT);

        @Nullable
        private final ResourceLocation font;

        WritingStyle(@Nullable ResourceLocation font) {
            this.font = font;
        }

        @Nullable
        public ResourceLocation font() {
            return font;
        }
    }

    private static final String CHILD_ALTERNATE_BASE = "aeiotrshnldcmyug";
    private static final int CHILD_ALT_ONE = 0xE100;
    private static final int CHILD_ALT_TWO = 0xE110;

    private HouseWriting() {
    }

    public static Component page(WritingStyle writing, String text) {
        String rendered = writing == WritingStyle.CHILD ? childHand(text) : text;
        Component component = Component.literal(rendered);
        return writing.font() == null
                ? component
                : component.copy().withStyle(style -> style.withFont(writing.font()));
    }

    /**
     * Swaps a minority of common lowercase letters for hand-drawn alternates.
     * The choice is deterministic, so reopening a page never makes the writing
     * crawl around, while repeated letters stop looking mechanically stamped.
     */
    static String childHand(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            int alternate = CHILD_ALTERNATE_BASE.indexOf(letter);
            if (alternate < 0) {
                result.append(letter);
                continue;
            }

            int pick = Math.floorMod(i * 31 + text.length() * 17 + letter * 13, 7);
            if (pick == 0 || pick == 1) {
                result.append((char) (CHILD_ALT_ONE + alternate));
            } else if (pick == 4) {
                result.append((char) (CHILD_ALT_TWO + alternate));
            } else {
                result.append(letter);
            }
        }
        return result.toString();
    }

    public static ItemStack book(String title, String author, WritingStyle writing, List<String> pages) {
        List<Component> rendered = new ArrayList<>();
        for (String page : pages) {
            rendered.add(page(writing, page));
        }
        return book(title, author, rendered);
    }

    public static ItemStack book(String title, String author, List<Component> pages) {
        List<Filterable<Component>> filtered = new ArrayList<>();
        for (Component page : pages) {
            filtered.add(Filterable.passThrough(page));
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(title), author, 0, filtered, true));
        return book;
    }

    /** Four short specimens for /oldesthouse writing samples. */
    public static List<ItemStack> samples() {
        ItemStack will = book(
                "Writing sample: Will", "Will Navidson", WritingStyle.WILL,
                List.of("Measured the north wall twice. Inside is still longer.\n\n"
                        + "Tom says I am holding the tape wrong. I am writing that down so he has to say it again.")
        );

        ItemStack karen = book(
                "Writing sample: Karen", "Karen Green", WritingStyle.KAREN,
                List.of("I know what color the rug was yesterday.\n\n"
                        + "I also know how ridiculous that sentence sounds. Both things can be true.")
        );

        Component zampanoPage = Component.empty()
                .append(page(WritingStyle.ZAMPANO,
                        "APPENDIX C. The corridor cannot be reconciled with the exterior measurements. "))
                .append(page(WritingStyle.ZAMPANO, "The discrepancy is probably clerical.")
                        .copy().withStyle(style -> style.withFont(ZAMPANO_FONT).withStrikethrough(true)))
                .append(page(WritingStyle.ZAMPANO,
                        "\n\n[No. Check the third survey again.]"));
        ItemStack zampano = book(
                "Writing sample: Zampano", "Zampano", List.of(zampanoPage)
        );

        ItemStack child = book(
                "Writing sample: child", "Daisy", WritingStyle.CHILD,
                List.of("the hall came back again.\n\ni counted 32 steps but dad says 31.\n\n"
                        + "i drew the door so i remember it.")
        );

        return List.of(will, karen, zampano, child);
    }

    public static List<ResourceLocation> sampleFonts() {
        return List.of(WILL_FONT, KAREN_FONT, ZAMPANO_FONT, CHILD_FONT);
    }
}
