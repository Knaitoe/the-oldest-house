package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WritingTests {
    private record Letter(int index, Style style, int cp) {}
    private static List<Letter> letters(FormattedCharSequence sequence) {
        List<Letter> result = new ArrayList<>();
        sequence.accept((index, style, cp) -> { result.add(new Letter(index, style, cp)); return true; });
        return result;
    }

    @GameTest(template = "empty")
    public static void blueWordRespectsBoundariesAndExistingStyles(GameTestHelper helper) {
        Style ink = Style.EMPTY.withColor(ChatFormatting.RED).withFont(HouseWriting.KAREN_FONT)
                .withItalic(true).withStrikethrough(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Original annotation")));
        Component original = Component.literal("Household HOUSE's boathouse house2 _house House\u0301 (hOuSe).").withStyle(ink);
        Component colored = HouseText.color(original);
        helper.assertTrue(original.getString().equals(colored.getString()), "formatting changed the story");
        List<Letter> actual = letters(colored.getVisualOrderText());
        for (int i = 0; i < actual.size(); i++) {
            int last = original.getString().indexOf("hOuSe");
            boolean blue = (i >= 10 && i < 15) || (i >= last && i < last + 5);
            helper.assertTrue(actual.get(i).style().equals(blue ? ink.withColor(HouseText.INK_BLUE) : ink),
                    "wrong color or lost annotation at " + i);
        }
        helper.assertTrue(original.getStyle().equals(ink), "the source was mutated");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blueWordCrossesStyledComponentsAndPreservesSinkIndices(GameTestHelper helper) {
        Style one = Style.EMPTY.withFont(HouseWriting.ZAMPANO_FONT).withBold(true);
        Style two = Style.EMPTY.withFont(HouseWriting.KAREN_FONT).withUnderlined(true);
        FormattedCharSequence original = FormattedCharSequence.composite(
                FormattedCharSequence.forward("a Ho", one), FormattedCharSequence.forward("use!", two));
        List<Letter> before = letters(original), after = letters(HouseText.color(original, 0xFFFFFF));
        helper.assertTrue(before.size() == after.size(), "characters were added or removed");
        for (int i = 0; i < before.size(); i++) {
            Letter old = before.get(i), now = after.get(i);
            helper.assertTrue(old.cp() == now.cp() && old.index() == now.index(), "sink indices changed");
            helper.assertTrue(now.style().equals(i >= 2 && i < 7 ? old.style().withColor(HouseText.SCREEN_BLUE) : old.style()),
                    "a split word lost its original font or emphasis");
        }
        int[] calls = {0};
        helper.assertTrue(!HouseText.color(original, 0).accept((index, style, cp) -> ++calls[0] < 3) && calls[0] == 3,
                "the native sink's early termination was ignored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void savedChildAlternatesRemainBlueAndDoNotChange(GameTestHelper helper) {
        Component page = HouseWriting.page(HouseWriting.WritingStyle.CHILD, "the house's door. house HOUSE.");
        ItemStack book = HouseWriting.book("Old house notes", "Daisy", List.of(page));
        ItemStack restored = ItemStack.parseOptional(helper.getLevel().registryAccess(),
                (net.minecraft.nbt.CompoundTag) book.save(helper.getLevel().registryAccess()));
        Component saved = restored.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw();
        List<Letter> before = letters(saved.getVisualOrderText()), after = letters(HouseText.color(saved.getVisualOrderText(), 0));
        helper.assertTrue(before.stream().anyMatch(c -> c.cp() >= 0xE100 && c.cp() < 0xE120), "no saved alternate glyphs");
        helper.assertTrue(before.size() == after.size(), "saved letters were decoded into different glyphs");
        for (int i = 0; i < before.size(); i++) {
            boolean word = (i >= 4 && i < 9) || (i >= 18 && i < 23) || (i >= 24 && i < 29);
            helper.assertTrue(before.get(i).cp() == after.get(i).cp(), "a saved alternate changed");
            helper.assertTrue(HouseWriting.CHILD_FONT.equals(after.get(i).style().getFont()), "lost child hand");
            helper.assertTrue(!word || after.get(i).style().getColor().getValue() == HouseText.INK_BLUE, "saved House word is not blue");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void displayFormattingLeavesChatAndTranslationsIntact(GameTestHelper helper) {
        Component source = Component.translatableWithFallback("test.house", "Welcome to the %s.", Component.literal("House"));
        FormattedCharSequence sequence = source.getVisualOrderText();
        HouseText.color(sequence, 0xFFFFFF).accept((index, style, cp) -> true);
        helper.assertTrue(source.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents,
                "the display pass flattened stored translations");
        helper.assertTrue(source.getString().equals("Welcome to the House."), "the displayed words changed");
        helper.assertTrue(source.getStyle().getColor() == null, "the chat source was recolored");
        List<Letter> actual = letters(HouseText.color(sequence, 0xFFFFFF));
        helper.assertTrue(actual.subList(15,20).stream().allMatch(c -> c.style().getColor().getValue() == HouseText.SCREEN_BLUE),
                "a localized House word was missed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void exportNativeSerialsForClientWrapping(GameTestHelper helper) throws Exception {
        var player = helper.makeMockServerPlayerInLevel();
        try {
            com.google.gson.JsonArray pages = new com.google.gson.JsonArray();
            var specimens = new java.util.ArrayList<>(io.github.knaitoe.theoldesthouse.house.HouseMarginalia.samples(player));
            specimens.addAll(io.github.knaitoe.theoldesthouse.house.HouseCorrespondence.samples(player));
            for (ItemStack book : specimens) {
                var content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                for (var page : content.pages()) {
                    com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
                    entry.addProperty("title", content.title().raw());
                    com.google.gson.JsonArray segments = new com.google.gson.JsonArray();
                    page.raw().visit((style, text) -> {
                        com.google.gson.JsonObject segment = new com.google.gson.JsonObject();
                        segment.addProperty("text", text);
                        segment.addProperty("font", style.getFont().toString());
                        segment.addProperty("bold", style.isBold());
                        segments.add(segment);
                        return java.util.Optional.empty();
                    }, Style.EMPTY);
                    entry.add("segments", segments);pages.add(entry);
                }
            }
            java.nio.file.Path folder = java.nio.file.Path.of("../build/font-smoke");
            java.nio.file.Files.createDirectories(folder);
            java.nio.file.Files.writeString(folder.resolve("serial-pages.json"), pages.toString());
            helper.assertTrue(specimens.size() == 125 && pages.size() >= 125, "the real original and expanded correspondence corpus is incomplete");
        } finally { helper.getLevel().getServer().getPlayerList().remove(player); }
        helper.succeed();
    }
}
