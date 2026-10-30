package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * The blue word, without changing the words, signed messages, or their interactions.
 * The red Minotaur is literature's alone: it shows only while a book or page is drawn,
 * never in chat, tooltips, signs or the HUD.
 */
public final class HouseText {
    public static final int INK_BLUE = 0x2447B2;
    public static final int SCREEN_BLUE = 0x789FFF;
    public static final int INK_RED = 0xA3201C;
    public static final int SCREEN_RED = 0xFF6B5E;
    private static final String HOUSE = "house", MINOTAUR = "minotaur";
    private static final String CHILD_LETTERS = "aeiotrshnldcmyug";
    private static int literature;

    private record Glyph(int index, Style style, int codePoint) {}
    private HouseText() {}

    /** Book and page screens hold this while they draw their pages. */
    public static void enterLiterature() { literature++; }
    public static void leaveLiterature() { if (literature > 0) literature--; }
    public static boolean inLiterature() { return literature > 0; }

    private static boolean dark(int textColor) {
        int r = textColor >> 16 & 255, g = textColor >> 8 & 255, b = textColor & 255;
        return r * 299 + g * 587 + b * 114 < 128000;
    }

    /** Dark ink on paper; a lighter blue against dark HUD/chat backgrounds. */
    public static int blueFor(int textColor) {
        return dark(textColor) ? INK_BLUE : SCREEN_BLUE;
    }

    public static int redFor(int textColor) {
        return dark(textColor) ? INK_RED : SCREEN_RED;
    }

    public static Component color(Component original) {
        return color(original, false);
    }

    /** For literature drawn outside a book screen (the House of Leaves' embers): the red Minotaur too. */
    public static Component literature(Component original) {
        return color(original, true);
    }

    private static Component color(Component original, boolean red) {
        List<Glyph> glyphs = new ArrayList<>();
        original.visit((style, text) -> {
            text.codePoints().forEach(cp -> glyphs.add(new Glyph(glyphs.size(), style, cp)));
            return Optional.empty();
        }, Style.EMPTY);
        BitSet blue = words(glyphs, HOUSE), minotaur = red ? words(glyphs, MINOTAUR) : new BitSet();
        if (blue.isEmpty() && minotaur.isEmpty()) return original;
        MutableComponent result = Component.empty().withStyle(original.getStyle());
        StringBuilder run = new StringBuilder();
        Style previous = null;
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = glyphs.get(i);
            Style style = blue.get(i) ? glyph.style().withColor(INK_BLUE)
                    : minotaur.get(i) ? glyph.style().withColor(INK_RED) : glyph.style();
            if (previous != null && !previous.equals(style)) {
                result.append(Component.literal(run.toString()).withStyle(previous));
                run.setLength(0);
            }
            run.appendCodePoint(glyph.codePoint());
            previous = style;
        }
        if (previous != null) result.append(Component.literal(run.toString()).withStyle(previous));
        return result;
    }

    /** Applied after native wrapping and bidi ordering; indices and advances stay unchanged. */
    public static FormattedCharSequence color(FormattedCharSequence original, int textColor) {
        boolean red = inLiterature();
        if (!mentions(original, red)) return original;
        List<Glyph> glyphs = new ArrayList<>();
        original.accept((index, style, cp) -> {
            glyphs.add(new Glyph(index, style, cp));
            return true;
        });
        BitSet blue = words(glyphs, HOUSE), minotaur = red ? words(glyphs, MINOTAUR) : new BitSet();
        if (blue.isEmpty() && minotaur.isEmpty()) return original;
        int blueColor = blueFor(textColor), redColor = redFor(textColor);
        return sink -> {
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph glyph = glyphs.get(i);
                Style style = blue.get(i) ? glyph.style().withColor(blueColor)
                        : minotaur.get(i) ? glyph.style().withColor(redColor) : glyph.style();
                if (!sink.accept(glyph.index(), style, glyph.codePoint())) return false;
            }
            return true;
        };
    }

    /** Most labels contain neither word: scan without allocating a Glyph per character. */
    private static boolean mentions(FormattedCharSequence text, boolean red) {
        String[] targets = red ? new String[]{HOUSE, MINOTAUR} : new String[]{HOUSE};
        int[] progress = new int[targets.length];
        boolean[] found = {false};
        text.accept((index, style, cp) -> {
            int letter = logical(style, cp);
            boolean part = wordPart(letter);
            for (int w = 0; w < targets.length; w++) {
                String target = targets[w];
                if (!part) {
                    if (progress[w] == target.length()) { found[0] = true; return false; }
                    progress[w] = 0;
                } else if (progress[w] >= 0 && progress[w] < target.length()
                        && Character.toLowerCase(letter) == target.charAt(progress[w])) {
                    progress[w]++;
                } else progress[w] = -1;
            }
            return true;
        });
        if (found[0]) return true;
        for (int w = 0; w < targets.length; w++) if (progress[w] == targets[w].length()) return true;
        return false;
    }

    private static BitSet words(List<Glyph> glyphs, String target) {
        BitSet result = new BitSet();
        int length = target.length();
        for (int i = 0; i + length <= glyphs.size(); i++) {
            if (i > 0 && wordPart(logical(glyphs.get(i - 1)))) continue;
            boolean match = true;
            for (int j = 0; j < length; j++) {
                if (Character.toLowerCase(logical(glyphs.get(i + j))) != target.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match && (i + length == glyphs.size() || !wordPart(logical(glyphs.get(i + length))))) {
                result.set(i, i + length);
                i += length - 1;
            }
        }
        return result;
    }

    private static boolean wordPart(int cp) {
        int type = Character.getType(cp);
        return Character.isLetterOrDigit(cp) || cp == '_' || type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK || type == Character.ENCLOSING_MARK;
    }

    private static int logical(Glyph glyph) {
        return logical(glyph.style(), glyph.codePoint());
    }

    private static int logical(Style style, int cp) {
        if (HouseWriting.CHILD_FONT.equals(style.getFont()) && cp >= 0xE100 && cp < 0xE120)
            return CHILD_LETTERS.charAt((cp - 0xE100) % 16);
        return cp;
    }
}
