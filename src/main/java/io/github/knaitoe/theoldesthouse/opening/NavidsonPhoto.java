package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.capture.CopySlots;
import io.github.knaitoe.theoldesthouse.capture.SettlementCopy;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Navidson's snapshot of the player's house, "from our porch last night".
 *
 * The House takes it the way it will later take the base copy: it captures
 * the settlement around the player's bed, rebuilds it in the outside
 * dimension, alters the copy (the upper window facing the Navidsons' porch
 * is lit by a hidden light; a house with no window there gets one cut into
 * its wall), lets the light settle, and photographs the copy, not the real
 * base. Everything is spread over ticks.
 */
public final class NavidsonPhoto {
    /** Half-width of the captured square around the bed. */
    public static final int CAPTURE_RADIUS = 40;
    public static final int CAPTURE_BELOW = 8;
    public static final int CAPTURE_ABOVE = 32;

    private static final int COLUMNS_PER_TICK = 2;
    private static final int ROWS_PER_TICK = 10;
    private static final int SETTLE_TICKS = 40;
    private static final int WINDOW_LIGHT = 13;

    private static final Map<UUID, NavidsonPhoto> JOBS = new HashMap<>();

    /**
     * @param pixels   the finished map colours, or null if no photo could be
     *                 taken (the pre-baked art is used instead)
     * @param copyMin  where the copy stands in the outside dimension
     * @param window   the copy's lit window (glass block), if any
     */
    public record Result(@Nullable byte[] pixels, boolean sawHouse, @Nullable BlockPos copyMin,
                         @Nullable BlockPos window, boolean windowCarved) {
    }

    private enum Phase {
        COPYING,
        FRAMING,
        SETTLING,
        RENDERING,
        DONE
    }

    private final UUID player;
    private final BlockPos bed;
    private final double preferredYaw;
    private final double preferredDistance;
    private final long seed;
    private final SettlementCopy copy;
    private final Consumer<Result> onDone;
    private Phase phase = Phase.COPYING;
    private int settle;
    private LevelSnapshotScene scene;
    private SnapshotRenderer.Camera camera;
    private SnapshotRenderer.Render render;
    private double tx;
    private double ty;
    private double tz;
    @Nullable
    private BlockPos window;
    private boolean carved;

    private NavidsonPhoto(
            UUID player,
            BlockPos bed,
            double preferredYaw,
            double preferredDistance,
            SettlementCopy copy,
            Consumer<Result> onDone
    ) {
        this.player = player;
        this.bed = bed.immutable();
        this.preferredYaw = preferredYaw;
        this.preferredDistance = preferredDistance;
        this.seed = bed.asLong() ^ player.getMostSignificantBits() ^ 0x5DEECE66DL;
        this.copy = copy;
        this.onDone = onDone;
    }

    /**
     * Starts taking the photo of the house around {@code bed}, as seen from
     * the direction of {@code lookFrom} (the Navidsons' porch), if one is not
     * already being taken for this player.
     *
     * @return false if the outside dimension is unavailable or a photo is already under way
     */
    public static boolean start(MinecraftServer server, UUID player, BlockPos bed, @Nullable BlockPos lookFrom, Consumer<Result> onDone) {
        if (JOBS.containsKey(player)) {
            return false;
        }
        NavidsonPhoto job = create(server, server.getLevel(HouseDimensions.OUTSIDE), player, bed, lookFrom, onDone);
        if (job == null) {
            return false;
        }
        JOBS.put(player, job);
        return true;
    }

    /** Takes the photo in one go (commands), copying into the outside dimension. */
    @Nullable
    public static Result takeNow(MinecraftServer server, UUID player, BlockPos bed, @Nullable BlockPos lookFrom) {
        return takeNow(server, server.getLevel(HouseDimensions.OUTSIDE), player, bed, lookFrom);
    }

    /**
     * Takes the photo in one go, copying into {@code copyLevel} (the game-test
     * server has no mod dimensions, so its tests copy into a far corner of
     * the test world instead).
     */
    @Nullable
    public static Result takeNow(MinecraftServer server, @Nullable ServerLevel copyLevel, UUID player, BlockPos bed, @Nullable BlockPos lookFrom) {
        Result[] result = new Result[1];
        NavidsonPhoto job = create(server, copyLevel, player, bed, lookFrom, done -> result[0] = done);
        if (job == null) {
            return null;
        }
        job.copy.runToCompletion();
        job.phase = Phase.FRAMING;
        while (job.phase != Phase.DONE) {
            if (job.phase == Phase.SETTLING) {
                job.settle = 0; // No ticks pass here; the explicit lit window still shows.
            }
            job.step(Integer.MAX_VALUE);
        }
        return result[0];
    }

    public static boolean isRunning(UUID player) {
        return JOBS.containsKey(player);
    }

    public static void tick(MinecraftServer server) {
        if (JOBS.isEmpty()) {
            return;
        }
        List<UUID> finished = new ArrayList<>();
        for (Map.Entry<UUID, NavidsonPhoto> entry : JOBS.entrySet()) {
            try {
                if (entry.getValue().step(ROWS_PER_TICK)) {
                    finished.add(entry.getKey());
                }
            } catch (RuntimeException exception) {
                TheOldestHouse.LOGGER.error("Navidson's photo of {}'s house failed; using the stock print.", entry.getKey(), exception);
                entry.getValue().fail();
                finished.add(entry.getKey());
            }
        }
        finished.forEach(JOBS::remove);
    }

    public static void clear() {
        JOBS.values().forEach(job -> job.copy.releaseTickets());
        JOBS.clear();
    }

    @Nullable
    private static NavidsonPhoto create(MinecraftServer server, @Nullable ServerLevel outside, UUID player, BlockPos bed,
                                        @Nullable BlockPos lookFrom, Consumer<Result> onDone) {
        if (outside == null) {
            TheOldestHouse.LOGGER.warn("The outside dimension is missing; Navidson's photo uses the stock print.");
            return null;
        }
        BlockPos sourceMin = bed.offset(-CAPTURE_RADIUS, -CAPTURE_BELOW, -CAPTURE_RADIUS);
        BlockPos sourceMax = bed.offset(CAPTURE_RADIUS, CAPTURE_ABOVE, CAPTURE_RADIUS);
        int slot = CopySlots.get(server).slotFor("photo/" + player);
        SettlementCopy copy = new SettlementCopy(server.overworld(), sourceMin, sourceMax, outside, CopySlots.targetMin(slot, sourceMin));

        double yaw;
        double distance;
        if (lookFrom != null && (lookFrom.getX() != bed.getX() || lookFrom.getZ() != bed.getZ())) {
            double dx = lookFrom.getX() - bed.getX();
            double dz = lookFrom.getZ() - bed.getZ();
            yaw = Math.atan2(dx, dz);
            distance = Math.sqrt(dx * dx + dz * dz);
        } else {
            yaw = (Math.floorMod(bed.asLong() * 31L, 360L)) * Math.PI / 180.0D;
            distance = 28.0D;
        }
        return new NavidsonPhoto(player, bed, yaw, distance, copy, onDone);
    }

    /** One tick of work; true when finished (the callback has run). */
    private boolean step(int rows) {
        switch (phase) {
            case COPYING -> {
                if (copy.tick(COLUMNS_PER_TICK)) {
                    phase = Phase.FRAMING;
                }
            }
            case FRAMING -> frame();
            case SETTLING -> {
                if (--settle <= 0) {
                    render = new SnapshotRenderer.Render(scene, camera, tx, ty, tz,
                            window == null ? null : SnapshotRenderer.packPos(window.getX(), window.getY(), window.getZ()), seed);
                    phase = Phase.RENDERING;
                }
            }
            case RENDERING -> {
                if (render.step(rows)) {
                    SnapshotRenderer.Photo photo = render.finish(LevelSnapshotScene.paletteRgb(), LevelSnapshotScene.paletteIds());
                    // Once Capture has succeeded, never substitute the stock
                    // home merely because the conservative visibility flag
                    // disagrees with the finished render. The photograph is
                    // always of this player's copied settlement.
                    finish(new Result(photo.pixels(), photo.sawHouse(), copy.targetMin(), window, carved));
                }
            }
            case DONE -> {
            }
        }
        return phase == Phase.DONE;
    }

    /** Chooses the camera, then alters the copy so the window it sees up top is lit. */
    private void frame() {
        ServerLevel outside = copy.target();
        scene = new LevelSnapshotScene(outside, copy.targetMin(), copy.targetMax());
        BlockPos home = copy.toTarget(bed);
        tx = home.getX() + 0.5D;
        tz = home.getZ() + 0.5D;
        ty = SnapshotRenderer.houseCenterY(scene, home.getX(), home.getY(), home.getZ());
        camera = SnapshotRenderer.chooseCamera(scene, tx, ty, tz, preferredYaw, preferredDistance);
        if (camera == null) {
            TheOldestHouse.LOGGER.warn("No viable camera view of {}'s captured settlement at {}.", player, bed);
            fail();
            return;
        }

        SnapshotRenderer.WindowPlan plan = SnapshotRenderer.planLitWindow(scene, camera, tx, ty, tz);
        if (plan != null) {
            BlockPos glass = new BlockPos(plan.glassX(), plan.glassY(), plan.glassZ());
            if (plan.carve()) {
                // "Your little window up top": in the copy there is one, whether or not the player built it.
                outside.setBlock(glass, Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            BlockPos lightPos = new BlockPos(plan.lightX(), plan.lightY(), plan.lightZ());
            if (!outside.getBlockState(lightPos).isAir()) {
                // The photograph may invent a shallow room behind the copied
                // facade, but it never alters the real home.
                outside.setBlock(lightPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            outside.setBlock(lightPos,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, WINDOW_LIGHT), Block.UPDATE_ALL);
            window = glass;
            carved = plan.carve();
        }
        settle = SETTLE_TICKS;
        phase = Phase.SETTLING;
    }

    private void fail() {
        finish(new Result(null, false, copy.targetMin(), window, carved));
    }

    private void finish(Result result) {
        phase = Phase.DONE;
        copy.releaseTickets();
        if (camera != null && result.pixels() != null) {
            // Remember where the photo was taken, for /oldesthouse opening copy.
            float yaw = (float) Math.toDegrees(Math.atan2(-camera.fx(), camera.fz()));
            float pitch = (float) -Math.toDegrees(Math.asin(Math.max(-1.0D, Math.min(1.0D, camera.fy()))));
            OpeningWorldData.get(copy.target().getServer()).putPhoto(player, new OpeningWorldData.PhotoRecord(
                    copy.target().dimension().location(),
                    copy.targetMin(),
                    new Vec3(camera.x(), camera.y(), camera.z()),
                    yaw,
                    pitch,
                    window,
                    carved
            ));
        }
        onDone.accept(result);
    }
}
