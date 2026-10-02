package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** The blue word, without changing the words, signed messages, or their interactions. */
public final class HouseText {
    public static final int INK_BLUE = 0x2447B2;
    public static final int SCREEN_BLUE = 0x789FFF;
    private static final String CHILD_LETTERS = "aeiotrshnldcmyug";

    private record Glyph(int index, Style style, int codePoint) {}
    private HouseText() {}

    /** Dark ink on paper; a lighter blue against dark HUD/chat backgrounds. */
    public static int blueFor(int textColor) {
        int r = textColor >> 16 & 255, g = textColor >> 8 & 255, b = textColor & 255;
        return r * 299 + g * 587 + b * 114 < 128000 ? INK_BLUE : SCREEN_BLUE;
    }

    public static Component color(Component original) {
        List<Glyph> glyphs = new ArrayList<>();
        original.visit((style, text) -> {
            text.codePoints().forEach(cp -> glyphs.add(new Glyph(glyphs.size(), style, cp)));
            return Optional.empty();
        }, Style.EMPTY);
        BitSet blue = words(glyphs);
        if (blue.isEmpty()) return original;
        MutableComponent result = Component.empty().withStyle(original.getStyle());
        StringBuilder run = new StringBuilder();
        Style previous = null;
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = glyphs.get(i);
            Style style = blue.get(i) ? glyph.style().withColor(INK_BLUE) : glyph.style();
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
        // Most labels contain no House word: scan without allocating a Glyph per character.
        int[] word = {0};
        original.accept((index, style, cp) -> {
            int letter = logical(style, cp);
            if (!wordPart(letter)) {
                if (word[0] == 5) { word[0] = 6; return false; }
                word[0] = 0;
            } else if (word[0] >= 0 && word[0] < 5
                    && Character.toLowerCase(letter) == "house".charAt(word[0])) {
                word[0]++;
            } else word[0] = -1;
            return true;
        });
        if (word[0] != 5 && word[0] != 6) return original;
        List<Glyph> glyphs = new ArrayList<>();
        original.accept((index, style, cp) -> {
            glyphs.add(new Glyph(index, style, cp));
            return true;
        });
        BitSet blue = words(glyphs);
        if (blue.isEmpty()) return original;
        int color = blueFor(textColor);
        return sink -> {
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph glyph = glyphs.get(i);
                if (!sink.accept(glyph.index(), blue.get(i) ? glyph.style().withColor(color) : glyph.style(),
                        glyph.codePoint())) return false;
            }
            return true;
        };
    }

    private static BitSet words(List<Glyph> glyphs) {
        BitSet result = new BitSet();
        for (int i = 0; i + 5 <= glyphs.size(); i++) {
            if (i > 0 && wordPart(logical(glyphs.get(i - 1)))) continue;
            boolean match = true;
            for (int j = 0; j < 5; j++) {
                if (Character.toLowerCase(logical(glyphs.get(i + j))) != "house".charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match && (i + 5 == glyphs.size() || !wordPart(logical(glyphs.get(i + 5))))) {
                result.set(i, i + 5);
                i += 4;
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
