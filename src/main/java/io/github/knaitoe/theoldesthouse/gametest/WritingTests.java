package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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

@GameTestHolder(TheOldestHouse.MOD_ID+"_writing")
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
    public static void theMinotaurIsRedOnlyInLiterature(GameTestHelper helper) {
        String text = "The Minotaur's house. minotaurs Minotaur2 MINOTAUR";
        FormattedCharSequence sequence = Component.literal(text).getVisualOrderText();
        int first = text.indexOf("Minotaur'"), house = text.indexOf("house"), last = text.indexOf("MINOTAUR");
        // Outside a book or page: chat, tooltips, signs and the HUD keep only the blue House word.
        List<Letter> plain = letters(HouseText.color(sequence, 0));
        for (int i = 0; i < plain.size(); i++) {
            boolean blue = i >= house && i < house + 5;
            helper.assertTrue(blue ? plain.get(i).style().getColor().getValue() == HouseText.INK_BLUE : plain.get(i).style().getColor() == null,
                    "outside literature only the House word is colored, at " + i);
        }
        helper.assertTrue(HouseText.color(Component.literal("the Minotaur")).getString().equals("the Minotaur")
                && letters(HouseText.color(Component.literal("the Minotaur")).getVisualOrderText()).stream().allMatch(c -> c.style().getColor() == null),
                "a baked page outside literature leaves the Minotaur alone");
        List<Letter> before = letters(sequence), inBook;
        HouseText.enterLiterature();
        try {
            inBook = letters(HouseText.color(sequence, 0));
        } finally {
            HouseText.leaveLiterature();
        }
        helper.assertTrue(!HouseText.inLiterature(), "leaving the page ends literature");
        helper.assertTrue(before.size() == inBook.size(), "characters were added or removed");
        for (int i = 0; i < inBook.size(); i++) {
            boolean red = (i >= first && i < first + 8) || (i >= last && i < last + 8), blue = i >= house && i < house + 5;
            var color = inBook.get(i).style().getColor();
            helper.assertTrue(before.get(i).cp() == inBook.get(i).cp() && before.get(i).index() == inBook.get(i).index(), "sink indices changed");
            helper.assertTrue(red ? color != null && color.getValue() == HouseText.INK_RED : blue ? color != null && color.getValue() == HouseText.INK_BLUE : color == null,
                    "in a book the Minotaur is red, the House blue and nothing else, at " + i);
        }
        HouseText.enterLiterature();
        try {
            helper.assertTrue(letters(HouseText.color(Component.literal("the Minotaur").getVisualOrderText(), 0xFFFFFF)).subList(4, 12).stream()
                    .allMatch(c -> c.style().getColor().getValue() == HouseText.SCREEN_RED), "against a dark page the red is the lighter red");
        } finally {
            HouseText.leaveLiterature();
        }
        List<Letter> embers = letters(HouseText.literature(Component.literal("the Minotaur")).getVisualOrderText());
        helper.assertTrue(embers.subList(4, 12).stream().allMatch(c -> c.style().getColor().getValue() == HouseText.INK_RED),
                "literature drawn outside a book (the House of Leaves' embers) carries the red itself");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void exportNativeSerialsForClientWrapping(GameTestHelper helper) throws Exception {
        var player = helper.makeMockServerPlayerInLevel();
        try {
            com.google.gson.JsonArray pages = new com.google.gson.JsonArray();
            var specimens = new java.util.ArrayList<>(io.github.knaitoe.theoldesthouse.house.HouseMarginalia.samples(player));
            specimens.addAll(io.github.knaitoe.theoldesthouse.house.HouseCorrespondence.samples(player));
            helper.assertTrue(specimens.size()==125+io.github.knaitoe.theoldesthouse.house.NovelCorrespondence.all().size(),"the complete original and novel-source correspondence corpus is required");
            specimens.addAll(io.github.knaitoe.theoldesthouse.house.ExpeditionInquiry.specimens());
            var staircase=staircaseSpecimens(player);specimens.addAll(staircase);
            for(int draft=0;draft<3;draft++)specimens.add(io.github.knaitoe.theoldesthouse.labyrinth.NovelTexts.archiveDraft(draft));
            var progress=accountSpecimens();specimens.addAll(progress);
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
                        segment.addProperty("strikethrough", style.isStrikethrough());
                        segments.add(segment);
                        return java.util.Optional.empty();
                    }, Style.EMPTY);
                    entry.add("segments", segments);pages.add(entry);
                }
            }
            java.nio.file.Path folder = java.nio.file.Path.of("../build/font-smoke");
            java.nio.file.Files.createDirectories(folder);
            java.nio.file.Files.writeString(folder.resolve("serial-pages.json"), pages.toString());
            helper.assertTrue(staircase.size() >= 16 && specimens.size() == 132 + io.github.knaitoe.theoldesthouse.house.NovelCorrespondence.all().size() + staircase.size() + progress.size() && pages.size() >= 700, "the real correspondence and personal staircase corpus is incomplete");
        } finally { helper.getLevel().getServer().getPlayerList().remove(player); }
        helper.succeed();
    }
    /**
     * Every staircase leaf the writer can produce at its widest (longest names and places, counts
     * at the native maximum, every narrator, phrasing and frame line), plus real accounts written
     * from a native player's record through every narrator.
     */
    /** Render every new account page, including the widest counts, kind hints and unfinished entries. */
    private static java.util.List<ItemStack> accountSpecimens(){
        var books=new java.util.ArrayList<ItemStack>();
        for(int count:new int[]{0,11,43}){
            var data=new io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData();var id=java.util.UUID.randomUUID();
            var own=new CompoundTag();var begun=new CompoundTag();
            for(var story:io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story.values())begun.putBoolean(story.id,true);
            own.put("Begun",begun);data.setStateEntry(io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.STATE,id.toString(),own);
            int added=0;
            for(var story:io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story.values()){
                if(added>=count)break;if(count==11&&!story.kind.equals("survival"))continue;
                io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.resolve(data,id,story,"heard");added++;
            }
            var account=io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.book(data,id,"reader",false);
            var pages=account.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw())
                    .filter(page->{String text=page.getString();return text.contains("THE ACCOUNT SO FAR")||text.contains("OTHER ENDINGS")||text.contains("left this part unfinished");}).toList();
            books.add(io.github.knaitoe.theoldesthouse.house.HouseWriting.book("An account","reader",pages));
        }
        // 0.4.159: the House's opening of the account, at its longest and as the owner wrote it, in its own plain type.
        String[][] nights={{"glasses","paper","4","3","collared_coat","riding_boots","covered_after","lantern","window","RUNAWAY"},{"paper","paper","0","","trench","","","woman","in","HAVEN"},{"phone","phone","11","","barn","barn","turned_after","middle","lattice","DANGER"}};
        for(String[] n:nights){
            var seen=new CompoundTag();var waiting=new CompoundTag();waiting.putString("First",n[0]);waiting.putString("Kept",n[1]);waiting.putBoolean("Waited",true);seen.put("waiting",waiting);
            var bath=new CompoundTag();bath.putInt("Item",Integer.parseInt(n[2]));bath.putInt("Used",Integer.parseInt(n[3].isEmpty()?n[2]:n[3]));bath.putBoolean("Tap",true);bath.putBoolean("LightOff",true);seen.put("bathroom",bath);
            // 0.4.169: a wardrobe garment is kept by itself beside its set; the longest names are the widest pages.
            var coat=new CompoundTag();var worn=io.github.knaitoe.theoldesthouse.labyrinth.HouseReadWardrobe.byId(n[4]);coat.putString("Coat",worn!=null&&!n[4].equals(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadWardrobe.SETS[worn.set()])?io.github.knaitoe.theoldesthouse.labyrinth.HouseReadWardrobe.SETS[worn.set()]:n[4]);if(worn!=null)coat.putString("CoatId",n[4]);
            if(n[5].contains("_"))coat.putString("BootsId",n[5]);else if(!n[5].isEmpty())coat.putString("Boots",n[5]);if(!n[6].isEmpty())coat.putString("Mirror",n[6]);seen.put("coatroom",coat);
            var eaves=new CompoundTag();eaves.putString("Stood",n[7]);eaves.putBoolean("Song",true);seen.put("shelter",eaves);
            var porch=new CompoundTag();porch.putString("First",n[8]);porch.putBoolean("Hidden",true);porch.putBoolean("Table",true);porch.putBoolean("Trees",true);seen.put("porch",porch);
            var pages=io.github.knaitoe.theoldesthouse.labyrinth.HouseReadProse.pages(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadProse.opening(seen,true,n[9])).stream().map(page->io.github.knaitoe.theoldesthouse.house.HouseWriting.page(io.github.knaitoe.theoldesthouse.house.HouseWriting.WritingStyle.PLAIN,page)).toList();
            books.add(io.github.knaitoe.theoldesthouse.house.HouseWriting.book("The House's opening","reader",pages));
        }
        return books;
    }
    private static List<ItemStack> staircaseSpecimens(net.minecraft.server.level.ServerPlayer p) {
        var books=new ArrayList<ItemStack>();var voices=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseProse.Voice.values();var all=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseProse.specimens();
        for(int v=0;v<voices.length;v++){var pages=all.get(v);var hand=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseAccount.hand(voices[v].hand);
            for(int from=0;from<pages.size();from+=50)books.add(HouseWriting.book("Staircase leaf proof "+voices[v]+" "+from,"Widest recorded facts",hand,pages.subList(from,Math.min(pages.size(),from+50))));}
        var seen=new java.util.HashSet<io.github.knaitoe.theoldesthouse.labyrinth.StaircaseProse.Voice>();
        for(long seed=1;seen.size()<voices.length&&seed<400;seed++){
            var story=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseAccount.write(p,new net.minecraft.nbt.CompoundTag(),seed);if(!seen.add(story.voice()))continue;
            var pages=new ArrayList<String>();pages.add(story.front());pages.addAll(story.leaves());
            books.add(HouseWriting.book("Staircase account "+story.voice(),"Native recorded facts",io.github.knaitoe.theoldesthouse.labyrinth.StaircaseAccount.hand(story.voice().hand),pages));
        }
        return books;
    }
}
