package io.github.knaitoe.theoldesthouse.opening;

import javax.annotation.Nullable;

/**
 * Takes Navidson's photograph: the player's own house at night, seen from
 * the direction of the Navidsons' porch, with one lit window up top.
 *
 * A small voxel ray tracer over a {@link Scene}. It deliberately has no
 * Minecraft dependencies (the world is reached only through {@link Scene})
 * so it can be exercised outside the game; {@link LevelSnapshotScene}
 * adapts a server level. The result is quantized onto a supplied palette
 * (the map colours) and framed like an instant photo.
 */
public final class SnapshotRenderer {
    public static final int SIZE = 128;

    // The print's photo area (inclusive); the rest is the white border.
    static final int PHOTO_X0 = 7;
    static final int PHOTO_Y0 = 7;
    static final int PHOTO_X1 = 120;
    static final int PHOTO_Y1 = 104;
    static final int PHOTO_W = PHOTO_X1 - PHOTO_X0 + 1;
    static final int PHOTO_H = PHOTO_Y1 - PHOTO_Y0 + 1;

    /** Sample kinds, packed into the top byte of {@link Scene#sample}. */
    public static final int AIR = 0;
    public static final int SOLID = 1;
    public static final int GLASS = 2;
    public static final int LIGHT = 3;
    public static final int WATER = 4;
    /** Outside the loaded world: the ray ends in sky. */
    public static final int VOID = 5;

    private static final int FRAME_RGB = 0xE2DED2;
    private static final int LIT_RGB = 0xEEC660;
    private static final int MULLION_RGB = 0x5A3C18;
    private static final double MAX_DISTANCE = 96.0D;
    /** Width of the scene framed at the target, in blocks. */
    private static final double FRAME_WIDTH = 24.0D;
    private static final double[] STRICT_RELATION_OFFSETS_DEGREES = {0, 5, -5, 10, -10};
    private static final double[] SAME_SIDE_OFFSETS_DEGREES = {0, 15, -15, 25, -25, 40, -40, 55, -55, 70, -70};
    private static final double[] ANY_SIDE_OFFSETS_DEGREES = {
            0, 20, -20, 40, -40, 70, -70, 110, -110, 150, -150, 180
    };

    /**
     * A view scoring this well (about 60% of the probe rays landing on the
     * house) is taken without scoring the remaining distances. The search runs
     * in one server tick, so it must not try every candidate when the first
     * is already clear.
     */
    private static final double GOOD_VIEW_SCORE = 70.0D;

    /** The world as the camera sees it. */
    public interface Scene {
        /** {@code (kind << 24) | rgb} for the block at this position. */
        int sample(int x, int y, int z);

        /** Block light (0-15) at this position. */
        int blockLight(int x, int y, int z);

        /** The first free y above the ground in this column, or {@link Integer#MIN_VALUE} if unknown. */
        int groundY(int x, int z);
    }

    public static int pack(int kind, int rgb) {
        return (kind << 24) | (rgb & 0xFFFFFF);
    }

    /** Where the camera stands and where it looks. */
    public record Camera(double x, double y, double z, double fx, double fy, double fz, double fovX) {
    }

    /**
     * The finished print.
     *
     * @param pixels     128 x 128 palette ids, row-major
     * @param sawHouse   whether the camera had a clear view of the target
     * @param litWindow  packed position of the lit window's block, or null if
     *                   a window had to be imagined on a wall
     */
    public record Photo(byte[] pixels, boolean sawHouse, Long litWindow, Camera camera) {
    }

    private SnapshotRenderer() {
    }

    // ------------------------------------------------------------------
    // Camera placement

    /**
     * Picks a camera position that preserves the real geographic relationship
     * between the Navidsons' porch and the player's home. The bearing is a hard
     * constraint: framing may slide only a few degrees to clear a tree or wall,
     * and may move nearer/farther along that same side. It never walks around
     * to the opposite facade simply because that would make a prettier image.
     */
    public static Camera chooseCamera(
            Scene scene,
            double tx,
            double ty,
            double tz,
            double preferredYaw,
            double preferredDistance
    ) {
        double baseDistance = clamp(preferredDistance, 18.0D, 38.0D);

        // First choice: preserve the actual porch-to-home relationship very
        // closely. This is the intended composition when terrain permits it.
        Camera camera = chooseCameraFromOffsets(
                scene, tx, ty, tz, preferredYaw, baseDistance,
                STRICT_RELATION_OFFSETS_DEGREES, 18.0D
        );
        if (camera != null) {
            return camera;
        }

        // Second choice: stay on the same broad facade/hemisphere. A tree,
        // hill or neighboring build should not turn the player's own house
        // into a stock photograph.
        camera = chooseCameraFromOffsets(
                scene, tx, ty, tz, preferredYaw, baseDistance,
                SAME_SIDE_OFFSETS_DEGREES, 45.0D
        );
        if (camera != null) {
            return camera;
        }

        // Last resort: preserve the important truth, namely that the image is
        // of the player's captured home. Only after the geographically
        // faithful searches fail may framing walk around the copied build.
        return chooseCameraFromOffsets(
                scene, tx, ty, tz, preferredYaw, baseDistance,
                ANY_SIDE_OFFSETS_DEGREES, 90.0D
        );
    }

    @Nullable
    private static Camera chooseCameraFromOffsets(
            Scene scene,
            double tx,
            double ty,
            double tz,
            double preferredYaw,
            double baseDistance,
            double[] offsets,
            double offsetPenaltyScale
    ) {
        double[] distances = {
                baseDistance,
                clamp(baseDistance + 4.0D, 18.0D, 38.0D),
                clamp(baseDistance - 4.0D, 18.0D, 38.0D),
                38.0D,
                34.0D,
                28.0D,
                22.0D,
                18.0D
        };

        Camera best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (double distance : distances) {
            for (double offset : offsets) {
                double yaw = preferredYaw + Math.toRadians(offset);
                double cx = tx + Math.sin(yaw) * distance;
                double cz = tz + Math.cos(yaw) * distance;
                int ground = scene.groundY(floor(cx), floor(cz));
                if (ground == Integer.MIN_VALUE) {
                    continue;
                }

                // Try a small vertical stack. Custom houses and neighboring
                // terrain can make ordinary eye height unusable even though a
                // perfectly legible porch-like shot exists a block higher.
                double baseY = Math.min(ty + 4.0D, Math.max(ground + 1.62D, ty - 3.0D));
                for (double lift : new double[]{0.0D, 1.5D, 3.0D}) {
                    double cy = baseY + lift;
                    int camKind = scene.sample(floor(cx), floor(cy), floor(cz)) >>> 24;
                    if (camKind != AIR) {
                        continue;
                    }

                    Camera candidate = lookAt(cx, cy, cz, tx, ty, tz, distance);
                    double rawView = viewScore(scene, candidate, tx, ty, tz);
                    if (rawView <= 0.0D) {
                        continue;
                    }

                    double score = rawView
                            - Math.abs(offset) / offsetPenaltyScale
                            - Math.abs(distance - baseDistance) / 10.0D
                            - lift * 0.12D;
                    if (score > bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
            if (best != null && bestScore >= GOOD_VIEW_SCORE) {
                break; // Distances are tried nearest the preferred one first.
            }
        }
        return best;
    }

    /** Backwards-compatible helper for tests/tools that do not have a real porch distance. */
    public static Camera chooseCamera(Scene scene, double tx, double ty, double tz, double preferredYaw) {
        return chooseCamera(scene, tx, ty, tz, preferredYaw, 28.0D);
    }

    static Camera lookAt(double cx, double cy, double cz, double tx, double ty, double tz, double distance) {
        double dx = tx - cx;
        double dy = ty - cy;
        double dz = tz - cz;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double fov = 2.0D * Math.atan(FRAME_WIDTH / 2.0D / Math.max(8.0D, distance));
        return new Camera(cx, cy, cz, dx / length, dy / length, dz / length, fov);
    }

    /** Rays over the frame that land on the house count for; rays stopped short count against. */
    private static double viewScore(Scene scene, Camera camera, double tx, double ty, double tz) {
        double targetDistance = Math.sqrt(sq(tx - camera.x) + sq(ty - camera.y) + sq(tz - camera.z));
        double score = 0.0D;
        Hit hit = new Hit();
        for (int j = 0; j < 9; j++) {
            for (int i = 0; i < 13; i++) {
                double u = (i + 0.5D) / 13.0D;
                double v = (j + 0.5D) / 9.0D;
                double[] dir = rayDirection(camera, u, v, (double) PHOTO_H / PHOTO_W);
                trace(scene, camera.x, camera.y, camera.z, dir[0], dir[1], dir[2], hit);
                if (hit.kind == AIR || hit.kind == VOID) {
                    continue;
                }
                double horizontal = Math.sqrt(sq(hit.x + 0.5D - tx) + sq(hit.z + 0.5D - tz));
                if (horizontal <= 12.0D && hit.y >= ty - 10.0D && hit.y <= ty + 12.0D) {
                    score += 1.0D;
                    if (hit.kind == GLASS && hit.y >= ty - 2.0D) {
                        score += 3.0D; // An upper window in view.
                    }
                } else if (hit.t < targetDistance - 6.0D && hit.y >= ty - 3.0D) {
                    // Something between the camera and the house at the house's
                    // height (a tree, a wall); foreground ground is fine.
                    score -= 1.5D;
                }
            }
        }
        return score;
    }

    /**
     * Height to aim at: halfway between the bed and the top of whatever
     * stands over it (the roof), so the whole house is in frame.
     */
    public static double houseCenterY(Scene scene, int bedX, int bedY, int bedZ) {
        int top = bedY;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int y = bedY + 1; y <= bedY + 20; y++) {
                    if (isSolid(scene, bedX + dx, y, bedZ + dz)) {
                        top = Math.max(top, y);
                    }
                }
            }
        }
        double centre = (bedY + top) / 2.0D + 1.0D;
        return clamp(centre, bedY + 2.0D, bedY + 9.0D);
    }

    /**
     * Where to alter the copy so that its upper window, facing the camera,
     * is lit: an existing pane of glass with room behind it, or else a wall
     * block to replace with glass. {@code light} is the room cell behind it.
     */
    public record WindowPlan(int glassX, int glassY, int glassZ, boolean carve, int lightX, int lightY, int lightZ) {
        public long glassKey() {
            return packPos(glassX, glassY, glassZ);
        }
    }

    public static WindowPlan planLitWindow(Scene scene, Camera camera, double tx, double ty, double tz) {
        int columns = 57;
        int rows = 49;
        double aspect = (double) PHOTO_H / PHOTO_W;
        java.util.Map<Long, int[]> glassHits = new java.util.HashMap<>();
        java.util.Map<Long, int[]> glassBehind = new java.util.HashMap<>();
        java.util.Map<Long, int[]> wallHits = new java.util.HashMap<>();
        java.util.Map<Long, int[]> wallBehind = new java.util.HashMap<>();
        java.util.Map<Long, int[]> fallbackWallHits = new java.util.HashMap<>();
        java.util.Map<Long, int[]> fallbackWallBehind = new java.util.HashMap<>();
        Hit hit = new Hit();
        int floorY = (int) Math.floor(ty);

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < columns; i++) {
                double[] dir = rayDirection(camera, (i + 0.5D) / columns, (j + 0.5D) / rows, aspect);
                trace(scene, camera.x, camera.y, camera.z, dir[0], dir[1], dir[2], hit);
                if (hit.kind != GLASS && hit.kind != SOLID) {
                    continue;
                }
                if (Math.sqrt(sq(hit.x + 0.5D - tx) + sq(hit.z + 0.5D - tz)) > 12.0D || hit.axis == 1) {
                    continue;
                }
                // The room cell behind the face the ray came in through.
                int[] behind = {
                        hit.x + (hit.axis == 0 ? hit.stepSign : 0),
                        hit.y,
                        hit.z + (hit.axis == 2 ? hit.stepSign : 0)
                };
                long key = packPos(hit.x, hit.y, hit.z);
                boolean wallCandidate = hit.kind == SOLID
                        && hit.y >= floorY - 1
                        && hit.y <= floorY + 5
                        && isInsideWall(scene, hit.x, hit.y, hit.z, hit.axis);

                // Keep a fallback candidate even when the copied room behind
                // it is solid. NavidsonPhoto is allowed to hollow one copied
                // cell for the light, guaranteeing the invented window while
                // leaving the player's real house untouched.
                if (wallCandidate) {
                    fallbackWallHits.computeIfAbsent(key, k -> new int[1])[0]++;
                    fallbackWallBehind.putIfAbsent(key, behind);
                }

                if (scene.sample(behind[0], behind[1], behind[2]) >>> 24 != AIR) {
                    continue;
                }
                if (hit.kind == GLASS && hit.y >= floorY - 3) {
                    glassHits.computeIfAbsent(key, k -> new int[1])[0]++;
                    glassBehind.putIfAbsent(key, behind);
                } else if (wallCandidate) {
                    wallHits.computeIfAbsent(key, k -> new int[1])[0]++;
                    wallBehind.putIfAbsent(key, behind);
                }
            }
        }

        Long glass = pickHighest(glassHits, 2);
        if (glass != null) {
            int[] light = glassBehind.get(glass);
            return new WindowPlan(unpackX(glass), unpackY(glass), unpackZ(glass), false, light[0], light[1], light[2]);
        }
        Long wall = pickMostVisibleNearTop(wallHits, 6);
        if (wall != null) {
            int[] light = wallBehind.get(wall);
            return new WindowPlan(unpackX(wall), unpackY(wall), unpackZ(wall), true, light[0], light[1], light[2]);
        }

        // Last resort for a windowless or very cramped build: use the most
        // visible square wall face on the correct geographic side and carve
        // one cell behind it in the copy.
        wall = pickMostVisibleNearTop(fallbackWallHits, 4);
        if (wall != null) {
            int[] light = fallbackWallBehind.get(wall);
            return new WindowPlan(unpackX(wall), unpackY(wall), unpackZ(wall), true, light[0], light[1], light[2]);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Rendering

    /**
     * Renders the whole photo from {@code camera} at once. {@code tx/ty/tz}
     * is the house the photo is of; {@code windowHint} (a packed position,
     * or null) is the window to show lit if it is in view.
     */
    public static Photo render(Scene scene, Camera camera, double tx, double ty, double tz, Long windowHint,
                               int[] paletteRgb, byte[] paletteIds, long seed) {
        Render render = new Render(scene, camera, tx, ty, tz, windowHint, seed);
        while (!render.step(PHOTO_H)) {
            // Rendered in one go.
        }
        return render.finish(paletteRgb, paletteIds);
    }

    /** A photo rendered a few rows at a time, so no single server tick pays for all of it. */
    public static final class Render {
        private final Scene scene;
        private final Camera camera;
        private final double tx;
        private final double ty;
        private final double tz;
        private final Long windowHint;
        private final long seed;
        private final int w = PHOTO_W;
        private final int h = PHOTO_H;
        private final double[] r;
        private final double[] g;
        private final double[] b;
        private final int[] kind;
        private final int[] hx;
        private final int[] hy;
        private final int[] hz;
        private final int[] axis;
        private final double[] fu;
        private final double[] fv;
        private final Hit hit = new Hit();
        private int nextRow;
        private boolean sawHouse;

        public Render(Scene scene, Camera camera, double tx, double ty, double tz, Long windowHint, long seed) {
            this.scene = scene;
            this.camera = camera;
            this.tx = tx;
            this.ty = ty;
            this.tz = tz;
            this.windowHint = windowHint;
            this.seed = seed;
            int n = w * h;
            r = new double[n];
            g = new double[n];
            b = new double[n];
            kind = new int[n];
            hx = new int[n];
            hy = new int[n];
            hz = new int[n];
            axis = new int[n];
            fu = new double[n];
            fv = new double[n];
        }

        /** Renders up to {@code rows} more rows; true once every row is done. */
        public boolean step(int rows) {
            double aspect = (double) h / w;
            int end = rows >= h - nextRow ? h : nextRow + Math.max(0, rows);
            for (int py = nextRow; py < end; py++) {
                for (int px = 0; px < w; px++) {
                    int i = py * w + px;
                    double[] dir = rayDirection(camera, (px + 0.5D) / w, (py + 0.5D) / h, aspect);
                    trace(scene, camera.x, camera.y, camera.z, dir[0], dir[1], dir[2], hit);
                    kind[i] = hit.kind;
                    hx[i] = hit.x;
                    hy[i] = hit.y;
                    hz[i] = hit.z;
                    axis[i] = hit.axis;
                    fu[i] = hit.u;
                    fv[i] = hit.v;

                    int rgb;
                    if (hit.kind == AIR || hit.kind == VOID) {
                        rgb = dir[1] < -0.01D ? groundBeyond(dir[1]) : sky(dir[1], px, py, seed);
                    } else {
                        rgb = shade(scene, hit, dir);
                        if (Math.sqrt(sq(hit.x + 0.5D - tx) + sq(hit.z + 0.5D - tz)) <= 12.0D) {
                            sawHouse = true;
                        }
                    }
                    r[i] = (rgb >> 16) & 255;
                    g[i] = (rgb >> 8) & 255;
                    b[i] = rgb & 255;
                }
            }
            nextRow = end;
            return nextRow >= h;
        }

        /** Lights the window, frames the print and quantizes it. */
        public Photo finish(int[] paletteRgb, byte[] paletteIds) {
            while (!step(h)) {
                // Finish any rows not yet rendered.
            }
            Long window = lightUpperWindow(scene, windowHint, r, g, b, kind, hx, hy, hz, axis, fu, fv, w, h, tx, ty, tz);
            vignette(r, g, b, w, h);
            byte[] pixels = compose(r, g, b, w, h, paletteRgb, paletteIds);
            return new Photo(pixels, sawHouse, window, camera);
        }
    }

    /** Dark ground where the copied land runs out below the horizon. */
    private static int groundBeyond(double dy) {
        int horizon = sky(0.0D, 0, 0, 0L);
        double t = clamp(-dy * 4.0D, 0.0D, 1.0D);
        return rgb(lerp((horizon >> 16) & 255, 10, t), lerp((horizon >> 8) & 255, 13, t), lerp(horizon & 255, 12, t));
    }

    private static int shade(Scene scene, Hit hit, double[] dir) {
        int base = hit.rgb;
        double br = (base >> 16) & 255;
        double bg = (base >> 8) & 255;
        double bb = base & 255;

        int nx = hit.axis == 0 ? -hit.stepSign : 0;
        int ny = hit.axis == 1 ? -hit.stepSign : 0;
        int nz = hit.axis == 2 ? -hit.stepSign : 0;

        double r;
        double g;
        double b;
        switch (hit.kind) {
            case LIGHT -> {
                r = 250;
                g = 214;
                b = 140;
            }
            case GLASS -> {
                // Dark panes, warmed if the room behind them is lit.
                int behind = scene.blockLight(hit.x - nx, hit.y - ny, hit.z - nz);
                double warm = Math.pow(behind / 15.0D, 2.0D);
                r = 20 + 200 * warm;
                g = 26 + 150 * warm;
                b = 42 + 40 * warm;
            }
            default -> {
                double shade = switch (hit.axis) {
                    case 1 -> ny > 0 ? 0.40D : 0.16D;
                    case 0 -> 0.26D;
                    default -> 0.31D;
                };
                if (hit.kind == WATER) {
                    shade = 0.30D;
                }
                // Moonlight is cool: pull colours slightly towards blue.
                r = br * shade * 0.92D;
                g = bg * shade * 0.97D;
                b = bb * shade * 1.08D + 6.0D;
                int light = scene.blockLight(hit.x + nx, hit.y + ny, hit.z + nz);
                double warm = Math.pow(light / 15.0D, 2.2D);
                r += (br * 0.55D + 120.0D) * warm;
                g += (bg * 0.45D + 80.0D) * warm;
                b += (bb * 0.25D + 30.0D) * warm;
            }
        }

        // Night haze towards the horizon colour with distance.
        double fog = clamp((hit.t - 24.0D) / 72.0D, 0.0D, 0.85D);
        int horizon = sky(0.0D, 0, 0, 0L);
        r = lerp(r, (horizon >> 16) & 255, fog);
        g = lerp(g, (horizon >> 8) & 255, fog);
        b = lerp(b, horizon & 255, fog);
        return rgb(r, g, b);
    }

    /** Night sky: deep blue at the horizon to near black overhead, with a few stars. */
    private static int sky(double dy, int px, int py, long seed) {
        double t = clamp(dy * 2.4D + 0.08D, 0.0D, 1.0D);
        double r = lerp(34, 9, t);
        double g = lerp(40, 11, t);
        double b = lerp(70, 26, t);
        if (seed != 0L && dy > 0.06D) {
            long hash = mix(seed ^ (px * 0x9E3779B97F4A7C15L) ^ (py * 0xC2B2AE3D27D4EB4FL));
            if ((hash & 1023L) < 11L) {
                double star = 130 + (hash >>> 10 & 63);
                r = g = star;
                b = star + 10;
            }
        }
        return rgb(r, g, b);
    }

    /**
     * "Your little window up top was lit." Lights the highest pane of glass
     * the camera can see in the house; if the house shows no glass, lights
     * a window in its highest visible stretch of wall anyway (a window the
     * player does not have).
     *
     * @return the packed position of the lit glass block, or null
     */
    private static Long lightUpperWindow(Scene scene, Long windowHint, double[] r, double[] g, double[] b, int[] kind,
                                         int[] hx, int[] hy, int[] hz, int[] axis,
                                         double[] fu, double[] fv, int w, int h,
                                         double tx, double ty, double tz) {
        int n = w * h;
        // Count pixels per candidate block; pick the highest, then the most visible.
        java.util.Map<Long, int[]> glass = new java.util.HashMap<>();
        java.util.Map<Long, int[]> walls = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            if ((kind[i] != GLASS && kind[i] != SOLID)
                    || Math.sqrt(sq(hx[i] + 0.5D - tx) + sq(hz[i] + 0.5D - tz)) > 12.0D) {
                continue;
            }
            long key = packPos(hx[i], hy[i], hz[i]);
            if (kind[i] == GLASS) {
                glass.computeIfAbsent(key, k -> new int[1])[0]++;
            } else if (kind[i] == SOLID && axis[i] != 1 && isInsideWall(scene, hx[i], hy[i], hz[i], axis[i])) {
                walls.computeIfAbsent(key, k -> new int[1])[0]++;
            }
        }

        Long chosen = windowHint != null && glass.containsKey(windowHint) ? windowHint : pickHighest(glass, 2);
        boolean imagined = false;
        if (chosen == null) {
            chosen = pickHighest(walls, 16);
            imagined = true;
        }
        if (chosen == null) {
            return null;
        }

        int cx = unpackX(chosen);
        int cy = unpackY(chosen);
        int cz = unpackZ(chosen);
        boolean[] lit = new boolean[n];
        int litCount = 0;
        int wantedKind = imagined ? SOLID : GLASS;
        for (int i = 0; i < n; i++) {
            if (kind[i] != wantedKind || hx[i] != cx || hy[i] != cy || hz[i] != cz) {
                continue;
            }
            if (imagined && (fu[i] < 0.28D || fu[i] > 0.72D || fv[i] < 0.2D || fv[i] > 0.8D)) {
                continue;
            }
            lit[i] = true;
            litCount++;
        }
        if (litCount == 0) {
            return null;
        }

        // Warm glow spilling onto the pixels around the window.
        double[] glow = new double[n];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!lit[y * w + x]) {
                    continue;
                }
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dx = -3; dx <= 3; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx < 0 || ny < 0 || nx >= w || ny >= h) {
                            continue;
                        }
                        double falloff = 1.0D - Math.sqrt(dx * dx + dy * dy) / 4.3D;
                        int j = ny * w + nx;
                        glow[j] = Math.max(glow[j], falloff);
                    }
                }
            }
        }
        // Glazing bars only when the window is big enough to show them; smaller, they just break up the light.
        boolean mullions = litCount >= 64;
        for (int i = 0; i < n; i++) {
            if (lit[i]) {
                boolean bar = mullions && (Math.abs(fu[i] - 0.5D) < 0.06D || Math.abs(fv[i] - 0.5D) < 0.06D);
                int colour = bar ? MULLION_RGB : LIT_RGB;
                r[i] = (colour >> 16) & 255;
                g[i] = (colour >> 8) & 255;
                b[i] = colour & 255;
            } else if (glow[i] > 0.0D) {
                double k = glow[i] * 0.5D;
                r[i] = lerp(r[i], 150, k);
                g[i] = lerp(g[i], 104, k);
                b[i] = lerp(b[i], 34, k);
            }
        }
        return imagined ? null : chosen;
    }

    /**
     * Among the top two rows of candidates, the block showing the most of
     * itself: squarely facing the camera and clear of corners.
     */
    private static Long pickMostVisibleNearTop(java.util.Map<Long, int[]> counts, int minPixels) {
        int topY = Integer.MIN_VALUE;
        for (java.util.Map.Entry<Long, int[]> entry : counts.entrySet()) {
            if (entry.getValue()[0] >= minPixels) {
                topY = Math.max(topY, unpackY(entry.getKey()));
            }
        }
        Long best = null;
        int bestCount = 0;
        for (java.util.Map.Entry<Long, int[]> entry : counts.entrySet()) {
            int count = entry.getValue()[0];
            int y = unpackY(entry.getKey());
            if (count < minPixels || y < topY - 1) {
                continue;
            }
            if (count > bestCount || (count == bestCount && y > unpackY(best))) {
                best = entry.getKey();
                bestCount = count;
            }
        }
        return best;
    }

    /** Wall on both sides within the face and above or below: somewhere a window could be. */
    private static boolean isInsideWall(Scene scene, int x, int y, int z, int faceAxis) {
        int ax = faceAxis == 2 ? 1 : 0;
        int az = faceAxis == 0 ? 1 : 0;
        return isSolid(scene, x + ax, y, z + az)
                && isSolid(scene, x - ax, y, z - az)
                && (isSolid(scene, x, y + 1, z) || isSolid(scene, x, y - 1, z));
    }

    private static boolean isSolid(Scene scene, int x, int y, int z) {
        return scene.sample(x, y, z) >>> 24 == SOLID;
    }

    private static Long pickHighest(java.util.Map<Long, int[]> counts, int minPixels) {
        Long best = null;
        int bestY = Integer.MIN_VALUE;
        int bestCount = 0;
        for (java.util.Map.Entry<Long, int[]> entry : counts.entrySet()) {
            int count = entry.getValue()[0];
            if (count < minPixels) {
                continue;
            }
            int y = unpackY(entry.getKey());
            if (y > bestY || (y == bestY && count > bestCount)) {
                best = entry.getKey();
                bestY = y;
                bestCount = count;
            }
        }
        return best;
    }

    private static void vignette(double[] r, double[] g, double[] b, int w, int h) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5D) / w - 0.5D;
                double dy = (y + 0.5D) / h - 0.5D;
                double k = clamp(1.0D - (dx * dx + dy * dy) * 1.3D, 0.55D, 1.0D);
                int i = y * w + x;
                r[i] *= k;
                g[i] *= k;
                b[i] *= k;
            }
        }
    }

    /** Frames the photo and quantizes it onto the palette with gentle error diffusion. */
    private static byte[] compose(double[] r, double[] g, double[] b, int w, int h, int[] paletteRgb, byte[] paletteIds) {
        double[] cr = new double[SIZE * SIZE];
        double[] cg = new double[SIZE * SIZE];
        double[] cb = new double[SIZE * SIZE];
        for (int i = 0; i < SIZE * SIZE; i++) {
            cr[i] = (FRAME_RGB >> 16) & 255;
            cg[i] = (FRAME_RGB >> 8) & 255;
            cb[i] = FRAME_RGB & 255;
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int src = y * w + x;
                int dst = (y + PHOTO_Y0) * SIZE + x + PHOTO_X0;
                cr[dst] = r[src];
                cg[dst] = g[src];
                cb[dst] = b[src];
            }
        }

        byte[] out = new byte[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int i = y * SIZE + x;
                double or = clamp(cr[i], 0, 255);
                double og = clamp(cg[i], 0, 255);
                double ob = clamp(cb[i], 0, 255);
                int best = nearest(paletteRgb, or, og, ob);
                out[i] = paletteIds[best];
                boolean inPhoto = x >= PHOTO_X0 && x <= PHOTO_X1 && y >= PHOTO_Y0 && y <= PHOTO_Y1;
                if (!inPhoto) {
                    continue;
                }
                int p = paletteRgb[best];
                double er = (or - ((p >> 16) & 255)) * 0.6D;
                double eg = (og - ((p >> 8) & 255)) * 0.6D;
                double eb = (ob - (p & 255)) * 0.6D;
                diffuse(cr, cg, cb, x + 1, y, er, eg, eb, 7.0D / 16.0D);
                diffuse(cr, cg, cb, x - 1, y + 1, er, eg, eb, 3.0D / 16.0D);
                diffuse(cr, cg, cb, x, y + 1, er, eg, eb, 5.0D / 16.0D);
                diffuse(cr, cg, cb, x + 1, y + 1, er, eg, eb, 1.0D / 16.0D);
            }
        }
        return out;
    }

    private static void diffuse(double[] r, double[] g, double[] b, int x, int y, double er, double eg, double eb, double f) {
        if (x < PHOTO_X0 || x > PHOTO_X1 || y < PHOTO_Y0 || y > PHOTO_Y1) {
            return;
        }
        int i = y * SIZE + x;
        r[i] += er * f;
        g[i] += eg * f;
        b[i] += eb * f;
    }

    static int nearest(int[] palette, double r, double g, double b) {
        int best = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < palette.length; i++) {
            int p = palette[i];
            double dr = r - ((p >> 16) & 255);
            double dg = g - ((p >> 8) & 255);
            double db = b - (p & 255);
            double distance = 2.0D * dr * dr + 4.0D * dg * dg + 3.0D * db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // Ray casting

    static double[] rayDirection(Camera camera, double u, double v, double aspect) {
        double fx = camera.fx;
        double fy = camera.fy;
        double fz = camera.fz;
        // Right = forward x up(0,1,0); true up = right x forward.
        double rx = -fz;
        double rz = fx;
        double rl = Math.sqrt(rx * rx + rz * rz);
        if (rl < 1.0E-6D) {
            rx = 1.0D;
            rz = 0.0D;
            rl = 1.0D;
        }
        rx /= rl;
        rz /= rl;
        double ux = -rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy;

        double half = Math.tan(camera.fovX / 2.0D);
        double sx = (u - 0.5D) * 2.0D * half;
        double sy = (0.5D - v) * 2.0D * half * aspect;
        double dx = fx + rx * sx + ux * sy;
        double dy = fy + uy * sy;
        double dz = fz + rz * sx + uz * sy;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return new double[]{dx / length, dy / length, dz / length};
    }

    /** Mutable trace result, reused across rays. */
    static final class Hit {
        int kind;
        int rgb;
        int x;
        int y;
        int z;
        /** 0, 1 or 2: the axis crossed to enter the block. */
        int axis;
        /** +1 or -1: the step direction along that axis. */
        int stepSign;
        double t;
        double u;
        double v;
    }

    /** Amanatides-Woo voxel walk until something that is not air. */
    static void trace(Scene scene, double ox, double oy, double oz, double dx, double dy, double dz, Hit hit) {
        int x = floor(ox);
        int y = floor(oy);
        int z = floor(oz);
        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;
        double tDeltaX = dx == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dx);
        double tDeltaY = dy == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dy);
        double tDeltaZ = dz == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dz);
        double tMaxX = dx == 0 ? Double.MAX_VALUE : (dx > 0 ? (x + 1 - ox) : (ox - x)) * tDeltaX;
        double tMaxY = dy == 0 ? Double.MAX_VALUE : (dy > 0 ? (y + 1 - oy) : (oy - y)) * tDeltaY;
        double tMaxZ = dz == 0 ? Double.MAX_VALUE : (dz > 0 ? (z + 1 - oz) : (oz - z)) * tDeltaZ;

        while (true) {
            double t;
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                x += stepX;
                t = tMaxX;
                tMaxX += tDeltaX;
                hit.axis = 0;
                hit.stepSign = stepX;
            } else if (tMaxY < tMaxZ) {
                y += stepY;
                t = tMaxY;
                tMaxY += tDeltaY;
                hit.axis = 1;
                hit.stepSign = stepY;
            } else {
                z += stepZ;
                t = tMaxZ;
                tMaxZ += tDeltaZ;
                hit.axis = 2;
                hit.stepSign = stepZ;
            }
            if (t > MAX_DISTANCE) {
                hit.kind = AIR;
                hit.t = t;
                return;
            }
            int sample = scene.sample(x, y, z);
            int kind = sample >>> 24;
            if (kind == AIR) {
                continue;
            }
            hit.kind = kind;
            hit.rgb = sample & 0xFFFFFF;
            hit.x = x;
            hit.y = y;
            hit.z = z;
            hit.t = t;
            double px = ox + dx * t;
            double py = oy + dy * t;
            double pz = oz + dz * t;
            switch (hit.axis) {
                case 0 -> {
                    hit.u = frac(pz);
                    hit.v = 1.0D - frac(py);
                }
                case 1 -> {
                    hit.u = frac(px);
                    hit.v = frac(pz);
                }
                default -> {
                    hit.u = frac(px);
                    hit.v = 1.0D - frac(py);
                }
            }
            return;
        }
    }

    // ------------------------------------------------------------------
    // Small helpers

    static long packPos(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    static int unpackX(long packed) {
        return (int) (packed << 0 >> 38);
    }

    static int unpackY(long packed) {
        return (int) (packed << 52 >> 52);
    }

    static int unpackZ(long packed) {
        return (int) (packed << 26 >> 38);
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static double frac(double value) {
        return value - Math.floor(value);
    }

    private static double sq(double value) {
        return value * value;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private static int rgb(double r, double g, double b) {
        int ri = (int) clamp(Math.round(r), 0, 255);
        int gi = (int) clamp(Math.round(g), 0, 255);
        int bi = (int) clamp(Math.round(b), 0, 255);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        value ^= value >>> 33;
        return value;
    }
}
