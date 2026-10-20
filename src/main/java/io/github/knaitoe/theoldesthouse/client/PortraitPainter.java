package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;

/** A native 16-by-32 painted bust. Skin pixels, including the hat and jacket, remain recognizable. */
public final class PortraitPainter {
    private PortraitPainter() {}

    public static NativeImage paint(NativeImage skin, NativeImage family) {
        if (skin.getWidth() < 64 || skin.getHeight() < 32 || family.getWidth() != 16 || family.getHeight() != 32)
            throw new IllegalArgumentException("Portraits require a native skin and the existing 16x32 family painting");
        var result = new NativeImage(16, 32, false);
        // The family's actual ground colour, not a photographic background or high-resolution filter.
        int ground = family.getPixelRGBA(0, 0);
        for (int y = 0; y < 32; y++) for (int x = 0; x < 16; x++) {
            int colour = ground;
            if (y >= 16 && x >= (y == 16 ? 2 : 1) && x <= (y == 16 ? 13 : 14)) {
                int u = Math.min(7, (x - 1) * 8 / 14), v = Math.min(11, (y - 16) * 12 / 16);
                colour = skin.getPixelRGBA(20 + u, 20 + v);
                if (skin.getHeight() >= 64) colour = over(colour, skin.getPixelRGBA(20 + u, 36 + v));
                if (x < 3 || x > 12) {
                    colour = skin.getPixelRGBA(44 + (x % 3), 20 + v);
                    if (skin.getHeight() >= 64) colour = over(colour, skin.getPixelRGBA(44 + (x % 3), 36 + v));
                }
                colour = pigment(colour, x, y, false);
            }
            if (x >= 3 && x <= 12 && y >= 5 && y <= 16) {
                int u = (x - 3) * 8 / 10, v = (y - 5) * 8 / 12;
                colour = pigment(over(skin.getPixelRGBA(8 + u, 8 + v), skin.getPixelRGBA(40 + u, 8 + v)), x, y, true);
            }
            result.setPixelRGBA(x, y, colour);
        }
        return result;
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
