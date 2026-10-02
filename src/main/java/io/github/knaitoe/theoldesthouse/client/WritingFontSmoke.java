package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelTexts;
import io.github.knaitoe.theoldesthouse.opening.NavidsonLetter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Opt-in CI probe of actual loaded glyphs, native wrapping and both Font mixin paths. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class WritingFontSmoke {
    private static boolean started;
    private static int frames;

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("the_oldest_house.fontSmoke") || started) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getOverlay() != null || mc.screen == null) return;
        started = true;
        mc.options.guiScale().set(2);
        mc.resizeDisplay();
        List<ItemStack> books = new ArrayList<>(HouseWriting.samples());
        books.add(NavidsonLetter.createBook());
        books.add(io.github.knaitoe.theoldesthouse.labyrinth.HollowayCamp.journal());
        books.addAll(List.of(NovelTexts.archive(), NovelTexts.whaleOpening(), NovelTexts.whaleLast(),
                NovelTexts.well(), NovelTexts.apology(), NovelTexts.hospitalOpening(), NovelTexts.hospitalLast(), NovelTexts.karen()));
        for (int n = 0; n < 4; n++) books.add(NovelTexts.letter(n, "Explorer"));
        int pages = 0;
        for (ItemStack book : books) {
            var content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            for (var page : content.pages()) {
                int lines = mc.font.split(page.raw(), 114).size();
                if (lines > 14) throw new IllegalStateException(content.title().raw() + " page " + pages + " wraps to " + lines + " lines");
                pages++;
            }
        }
        try {
            var corpus = com.google.gson.JsonParser.parseString(Files.readString(Path.of("../build/font-smoke/serial-pages.json"))).getAsJsonArray();
            for (var entry : corpus) {
                var object = entry.getAsJsonObject();
                var page = Component.empty();
                for (var segment : object.getAsJsonArray("segments")) {
                    var part = segment.getAsJsonObject();
                    page.append(Component.literal(part.get("text").getAsString()).withStyle(style -> style
                            .withFont(net.minecraft.resources.ResourceLocation.parse(part.get("font").getAsString()))
                            .withBold(part.get("bold").getAsBoolean())));
                }
                int lines = mc.font.split(page,114).size();
                if (lines > 14) throw new IllegalStateException(object.get("title").getAsString()+" serial page wraps to "+lines+" lines");
                pages++;
            }
        } catch (java.io.IOException exception) { throw new IllegalStateException("Server serial-page corpus missing",exception); }
        TheOldestHouse.LOGGER.info("WRITING FONT CHECK: {} real book pages fit native 114px / 14-line limits", pages);
        mc.setScreen(new ProofScreen());
    }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("the_oldest_house.fontSmoke") || !(mc.screen instanceof ProofScreen) || ++frames < 4) return;
        Path folder = Path.of("../build/font-smoke");
        Files.createDirectories(folder);
        try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(folder.resolve("native-writing.png"));
        }
        Files.writeString(folder.resolve("passed.txt"), "Loaded all custom fonts; native book wrapping and String/Component/sequence rendering passed.\n");
        TheOldestHouse.LOGGER.info("WRITING FONT CHECK PASSED: native screenshot saved");
        mc.setScreen(new NpcVisualProof());
    }

    private static final class ProofScreen extends Screen {
        ProofScreen() { super(Component.literal("Writing proof")); }
        @Override public void renderBackground(GuiGraphics g, int x, int y, float partial) {}
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            g.fill(0,0,width,height,0xFF211E1B);
            g.drawString(font, "The Oldest House | native writing proof", 10, 8, 0xFFEAE0D0);
            g.drawString(font,Component.literal("HOUSE 31").withStyle(style -> style.withFont(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel"))),
                    width-100,8,0xFFEAE0D0,false);
            for (int i = 0; i < HouseWriting.sampleFonts().size(); i++) {
                int x = 10 + i%3 * (width/3), y = 30 + i/3 * 164;
                g.fill(x,y,x+width/3-18,y+154,0xFFE7D9BC);
                g.drawString(font, new String[]{"Will","Karen","Zampano","Child","Pelafina","Claw"}[i],x+8,y+7,0xFF241E19,false);
                HouseWriting.WritingStyle style = new HouseWriting.WritingStyle[]{HouseWriting.WritingStyle.WILL,
                        HouseWriting.WritingStyle.KAREN,HouseWriting.WritingStyle.ZAMPANO,HouseWriting.WritingStyle.CHILD,
                        HouseWriting.WritingStyle.PELAFINA,HouseWriting.WritingStyle.CLAW}[i];
                String text = "The House is longer inside.\nI measured it twice.\n\n\u201cDo not come for me.\u201d\nThe house's door.\n0123456789 [No. Again.]";
                if (style == HouseWriting.WritingStyle.CLAW) text = text.toUpperCase(java.util.Locale.ROOT);
                int row = y+24;
                for (var line : font.split(HouseWriting.page(style,text),114)) {
                    g.drawString(font,line,x+8,row,0xFF181411,false);row+=9;
                }
            }
            int bottom = height-26;
            g.drawString(font,"Old House, house's door, HOUSE. Household / boathouse stay ordinary.",10,bottom,0xFFF0EBDD,false);
            // Old saved components without authored coloring exercise the sequence path directly.
            g.drawString(font,Component.literal("The house is still here. The House is still here.").getVisualOrderText(),10,bottom+12,0xFFF0EBDD,false);
        }
    }
}
