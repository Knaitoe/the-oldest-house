package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

/**
 * The hide-and-clap child's drawing: a crayon picture of someone in a
 * blindfold, drawn from exactly where the wardrobe stands. The figure and
 * the bed are where they were, seen from inside the wardrobe (left and right
 * by their true angle, bigger when nearer); the wardrobe's own doors stand
 * ajar at the edges of the page. Kept as a locked map.
 */
public final class CrayonDrawing {
    public static final int SIZE = 128;
    private static final int GROUND = 108;
    /** Where on the map it nominally lies: far from anywhere anyone plays. */
    private static final int CENTER = 29_000_000;

    private static final byte PAPER = MapColor.SNOW.getPackedId(MapColor.Brightness.HIGH);
    private static final byte PAPER_GRAIN = MapColor.SNOW.getPackedId(MapColor.Brightness.NORMAL);
    public static final byte BLACK = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.NORMAL);
    private static final byte BLUE = MapColor.COLOR_BLUE.getPackedId(MapColor.Brightness.HIGH);
    private static final byte SKIN = MapColor.TERRACOTTA_WHITE.getPackedId(MapColor.Brightness.HIGH);
    private static final byte OUTLINE = MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.LOW);
    public static final byte RED = MapColor.COLOR_RED.getPackedId(MapColor.Brightness.HIGH);
    private static final byte PILLOW = MapColor.SNOW.getPackedId(MapColor.Brightness.LOW);
    private static final byte WOOD = MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.NORMAL);
    private static final byte WOOD_DARK = MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.LOWEST);
    private static final byte YELLOW = MapColor.COLOR_YELLOW.getPackedId(MapColor.Brightness.HIGH);
    private static final byte FLOOR = MapColor.COLOR_ORANGE.getPackedId(MapColor.Brightness.LOW);

    private final byte[] pixels = new byte[SIZE * SIZE];
    private final Random random;

    private CrayonDrawing(long seed) {
        this.random = new Random(seed);
        Arrays.fill(pixels, PAPER);
        for (int i = 0; i < pixels.length; i++) {
            if (random.nextInt(23) == 0) {
                pixels[i] = PAPER_GRAIN;
            }
        }
    }

    /**
     * Draws the page. {@code eye} is inside the wardrobe, {@code yaw} the way
     * it looks out; {@code figure} is where the blindfolded player stood.
     */
    public static byte[] draw(Vec3 eye, float yaw, Vec3 figure, @Nullable Vec3 bed, long seed) {
        CrayonDrawing page = new CrayonDrawing(seed);
        double r = Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(r), 0.0D, Math.cos(r));
        Vec3 right = new Vec3(-Math.cos(r), 0.0D, -Math.sin(r));

        // The floor, in a wobbly line; a lamp; the room's corners.
        page.wobblyLine(0, GROUND, SIZE - 1, GROUND, FLOOR, 2);
        page.circle(64, 12, 5, YELLOW, true);
        page.line(64, 0, 64, 7, OUTLINE, 1);

        if (bed != null) {
            page.drawBed(project(eye, forward, right, bed));
        }
        page.drawFigure(project(eye, forward, right, figure));

        // The wardrobe's doors, ajar, at both edges.
        page.fillRect(0, 0, 10, SIZE - 1, WOOD);
        page.fillRect(SIZE - 11, 0, SIZE - 1, SIZE - 1, WOOD);
        page.line(10, 0, 10, SIZE - 1, WOOD_DARK, 2);
        page.line(SIZE - 11, 0, SIZE - 11, SIZE - 1, WOOD_DARK, 2);
        page.circle(7, 64, 1, WOOD_DARK, true);
        page.circle(SIZE - 8, 64, 1, WOOD_DARK, true);
        return page.pixels;
    }

    /** Where something stands on the page, and how tall it is drawn: x, then height; null if behind the eye. */
    @Nullable
    private static int[] project(Vec3 eye, Vec3 forward, Vec3 right, Vec3 at) {
        Vec3 rel = at.subtract(eye);
        double depth = rel.x * forward.x + rel.z * forward.z;
        if (depth < 0.4D) {
            return null;
        }
        double side = rel.x * right.x + rel.z * right.z;
        int x = (int) Math.round(64 + Math.max(-1.2D, Math.min(1.2D, side / depth)) * 50.0D);
        int height = (int) Math.round(Math.max(18.0D, Math.min(92.0D, 150.0D / depth)));
        return new int[]{x, height};
    }

    private void drawFigure(@Nullable int[] at) {
        if (at == null) {
            return;
        }
        int x = at[0];
        int h = at[1];
        int headR = Math.max(4, h / 8);
        int headY = GROUND - h + headR;
        int neck = headY + headR;
        int hip = neck + h * 2 / 5;
        // Body, arms out in front feeling for someone, legs.
        wobblyLine(x, neck, x, hip, BLUE, 3);
        wobblyLine(x, neck + h / 10, x - h / 3, neck + h / 20, BLUE, 2);
        wobblyLine(x, neck + h / 10, x + h / 3, neck + h / 20, BLUE, 2);
        wobblyLine(x, hip, x - h / 6, GROUND - 1, BLUE, 2);
        wobblyLine(x, hip, x + h / 6, GROUND - 1, BLUE, 2);
        // The head, and the blindfold: pressed hard, twice over.
        circle(x, headY, headR, SKIN, true);
        circle(x, headY, headR, OUTLINE, false);
        int band = Math.max(2, headR / 2);
        fillRect(x - headR - 1, headY - band / 2, x + headR + 1, headY + band / 2, BLACK);
        wobblyLine(x + headR, headY, x + headR + headR, headY + headR, BLACK, 2);
        wobblyLine(x + headR, headY, x + headR + headR / 2, headY + headR + 2, BLACK, 2);
        // No mouth drawn.
    }

    private void drawBed(@Nullable int[] at) {
        if (at == null) {
            return;
        }
        int x = at[0];
        int w = Math.max(14, at[1] / 2);
        int h = Math.max(6, at[1] / 5);
        fillRect(x - w / 2, GROUND - h, x + w / 2, GROUND - 1, RED);
        fillRect(x - w / 2, GROUND - h - 3, x - w / 2 + w / 4, GROUND - h, PILLOW);
        wobblyLine(x - w / 2, GROUND - h - 6, x - w / 2, GROUND, OUTLINE, 2);
        wobblyLine(x + w / 2, GROUND - h, x + w / 2, GROUND, OUTLINE, 2);
    }

    // ------------------------------------------------------------------
    // Crayon

    private void plot(int x, int y, byte colour) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE || random.nextInt(9) == 0) {
            return; // Crayon skips on the grain of the paper.
        }
        pixels[y * SIZE + x] = colour;
    }

    private void line(int x0, int y0, int x1, int y1, byte colour, int thickness) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : (double) i / steps;
            int x = (int) Math.round(x0 + (x1 - x0) * t);
            int y = (int) Math.round(y0 + (y1 - y0) * t);
            for (int dx = 0; dx < thickness; dx++) {
                for (int dy = 0; dy < thickness; dy++) {
                    plot(x + dx - thickness / 2, y + dy - thickness / 2, colour);
                }
            }
        }
    }

    private void wobblyLine(int x0, int y0, int x1, int y1, byte colour, int thickness) {
        int mx = (x0 + x1) / 2 + random.nextInt(3) - 1;
        int my = (y0 + y1) / 2 + random.nextInt(3) - 1;
        line(x0, y0, mx, my, colour, thickness);
        line(mx, my, x1, y1, colour, thickness);
    }

    private void circle(int cx, int cy, int radius, byte colour, boolean filled) {
        for (int y = -radius - 1; y <= radius + 1; y++) {
            for (int x = -radius - 1; x <= radius + 1; x++) {
                double d = Math.sqrt(x * x + y * y);
                if (filled ? d <= radius : Math.abs(d - radius) < 0.8D) {
                    plot(cx + x, cy + y, colour);
                }
            }
        }
    }

    private void fillRect(int x0, int y0, int x1, int y1, byte colour) {
        for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
            for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
                plot(x, y, colour);
            }
        }
    }

    // ------------------------------------------------------------------
    // The map

    /** Stores the page as a locked map and returns its id. */
    public static MapId store(ServerLevel level, byte[] pixels) {
        MapItemSavedData data = MapItemSavedData.createFresh(CENTER, CENTER, (byte) 0, false, false, HouseDimensions.INTERIOR);
        System.arraycopy(pixels, 0, data.colors, 0, Math.min(pixels.length, data.colors.length));
        MapId id = level.getFreeMapId();
        level.setMapData(id, data.locked());
        return id;
    }

    public static ItemStack item(MapId id) {
        ItemStack drawing = new ItemStack(Items.FILLED_MAP);
        drawing.set(DataComponents.MAP_ID, id);
        drawing.set(DataComponents.CUSTOM_NAME, Component.literal("A drawing"));
        drawing.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Crayon, pressed hard.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                Component.literal("Someone in a blindfold, seen from the wardrobe.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )));
        drawing.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return drawing;
    }
}
