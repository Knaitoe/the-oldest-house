package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;

/**
 * A native 16-by-32 painted bust. Skin pixels, including the hat and jacket, remain recognizable. 0.4.223: a skin at more than
 * the vanilla scale (the House's bodies are painted four to a texel) is read at up to twice the detail, as the manor's own
 * family portraits are (tools/draw_manor_portraits.py); and what the glass shows the reader in, their coat or outfit, the
 * reading glasses and the obscured face, is painted over the House's body.
 */
public final class PortraitPainter {
    private PortraitPainter() {}

    /** What the House's version of the reader wears over their body, each a 64-by-32 garment sheet or null. */
    public record Overlay(@Nullable NativeImage coat, boolean outfit, boolean crop, @Nullable NativeImage specs, @Nullable NativeImage veil) {
        static final Overlay NONE = new Overlay(null, false, false, null, null);
    }

    /**
     * 0.4.224 (owner: "do you not see the eyes of the portraits"): which of the face's columns and rows the head's ten columns and
     * twelve rows show. A sixteen-wide face squeezed into ten by skipping every few columns lost one iris and kept the other, so
     * every portrait looked lopsided; and an eight-wide face doubled a column right of centre. These are mirror-symmetric, and the
     * sixteen-wide ones keep both eyes whole (white, iris, white), the nose between them and the cheeks.
     */
    static final int[] COLS1 = {0, 0, 1, 2, 3, 4, 5, 6, 7, 7}, ROWS1 = {0, 0, 1, 2, 2, 3, 4, 4, 5, 6, 6, 7},
            COLS2 = {1, 3, 4, 5, 7, 8, 10, 11, 12, 14}, ROWS2 = {1, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14};

    public static NativeImage paint(NativeImage skin, NativeImage family) { return paint(skin, family, Overlay.NONE); }

    public static NativeImage paint(NativeImage skin, NativeImage family, Overlay overlay) {
        if (skin.getWidth() < 64 || skin.getHeight() < 32 || family.getWidth() != 16 || family.getHeight() != 32)
            throw new IllegalArgumentException("Portraits require a native skin and the existing 16x32 family painting");
        int s = Math.max(1, skin.getWidth() / 64), d = Math.min(2, s), step = s / d;
        boolean layered = skin.getHeight() >= 64 * s;
        var result = new NativeImage(16, 32, false);
        // The family's actual ground colour, not a photographic background or high-resolution filter.
        int ground = family.getPixelRGBA(0, 0);
        for (int y = 0; y < 32; y++) for (int x = 0; x < 16; x++) {
            int colour = ground;
            if (y >= 16 && x >= (y == 16 ? 2 : 1) && x <= (y == 16 ? 13 : 14)) {
                int u = Math.min(8 * d - 1, (x - 1) * 8 * d / 14), v = Math.min(12 * d - 1, (y - 16) * 12 * d / 16);
                colour = skin.getPixelRGBA(20 * s + u * step, 20 * s + v * step);
                if (layered) colour = over(colour, skin.getPixelRGBA(20 * s + u * step, 36 * s + v * step));
                boolean sleeve = x < 3 || x > 12;
                if (sleeve) {
                    int a = (x % 3) * d;
                    colour = skin.getPixelRGBA(44 * s + a * step, 20 * s + v * step);
                    if (layered) colour = over(colour, skin.getPixelRGBA(44 * s + a * step, 36 * s + v * step));
                }
                colour = garment(colour, overlay, x, y, sleeve);
                colour = pigment(colour, x, y, false);
            }
            if (x >= 3 && x <= 12 && y >= 5 && y <= 16) {
                int u = (d == 2 ? COLS2 : COLS1)[x - 3], v = (d == 2 ? ROWS2 : ROWS1)[y - 5];
                int face = over(skin.getPixelRGBA(8 * s + u * step, 8 * s + v * step), skin.getPixelRGBA(40 * s + u * step, 8 * s + v * step));
                // The reading glasses sit on the eyes' rows; the obscured face is veiled whole.
                int fu = u / d, fv = v / d;
                if (overlay.specs() != null && (fv == 3 || fv == 4)) face = over(face, overlay.specs().getPixelRGBA(1 + fu, 1 + fv - 3));
                if (overlay.veil() != null) face = over(face, overlay.veil().getPixelRGBA(1 + fu, 1 + fv));
                colour = pigment(face, x, y, true);
            }
            result.setPixelRGBA(x, y, colour);
        }
        return result;
    }

    /** A coat's front (its body box laid at 0,0, 8.7 by 13 by 4.7) or an outfit's top (8 by 12 by 4, a crop top 6 high), over the body. */
    private static int garment(int under, Overlay overlay, int x, int y, boolean sleeve) {
        var coat = overlay.coat(); if (coat == null) return under;
        if (overlay.outfit()) {
            if (sleeve) { int v = (y - 16) * 12 / 16; return v < 5 ? over(under, coat.getPixelRGBA(28 + x % 3, 4 + v)) : under; }
            int u = Math.min(7, (x - 1) * 8 / 14), v = Math.min(11, (y - 16) * 12 / 16);
            if (overlay.crop() && v >= 6) return under;
            return over(under, coat.getPixelRGBA(4 + u, 4 + v));
        }
        if (sleeve) { int v = Math.min(12, (y - 16) * 13 / 16); return over(under, coat.getPixelRGBA(33 + x % 3, 5 + v)); }
        int u = Math.min(8, (x - 1) * 9 / 14), v = Math.min(12, (y - 16) * 13 / 16);
        return over(under, coat.getPixelRGBA(5 + u, 5 + v));
    }

    private static int over(int under, int top) {
        int alpha = top >>> 24;
        int r = ((top & 255) * alpha + (under & 255) * (255 - alpha)) / 255;
        int g = (((top >>> 8) & 255) * alpha + ((under >>> 8) & 255) * (255 - alpha)) / 255;
        int b = (((top >>> 16) & 255) * alpha + ((under >>> 16) & 255) * (255 - alpha)) / 255;
        return 0xff000000 | b << 16 | g << 8 | r;
    }
    private static int pigment(int abgr, int x, int y, boolean head) {
        int r = abgr & 255, g = abgr >>> 8 & 255, b = abgr >>> 16 & 255;
        float grey = r * .299F + g * .587F + b * .114F;
        float light = (head ? 1.04F : .9F) - Math.max(0, x - 6) * .018F - Math.max(0, y - 22) * .009F;
        int red = step((r * .62F + grey * .18F + 16) * light);
        int green = step((g * .60F + grey * .17F + 10) * light);
        int blue = step((b * .54F + grey * .19F + 6) * light);
        return 0xff000000 | blue << 16 | green << 8 | red;
    }
    private static int step(float colour) { return Math.max(16, Math.min(208, Math.round(colour / 8) * 8)); }
}
