package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Devil's Rock's nights (0.4.201; docs/DEVILS_ROCK_REWORK.md): the loose pages, the shape at the corner of the eye, the two
 * cameras, and on the last night the chase.
 *
 * <p>The chase is the boys' last night as Rooney told it: "it flipped, me and Tommy switching spots. Tommy wasn't chasing me
 * anymore. I was chasing him. We never said anything. No names." A double in Tommy's shape, pennies over its eyes, runs from
 * the reader round the woods; lose sight of it, or nearly catch it, and it is behind them; let it catch up, or turn and run at
 * it, and it is ahead again. After enough of that it breaks for the pond, swims to the island and crouches between the two
 * rocks, and is gone when it is looked at straight. Nothing is written on screen while it runs.
 *
 * <p>The chase moves every tick and keeps its motion in memory only: a reader who leaves the park, the scene or the server
 * ends it, and it starts again at the rock. Only the stages that matter to the story are saved.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class DevilsRockNights {
    static final LabyrinthPlace PLACE = DevilsRockHouse.PLACE;
    /** The silhouette's phase for the double: Tommy's shape, a penny over each eye. */
    public static final int DOUBLE_PHASE = 6;
    /** Saved: none, ready at the rock, running (any way round), done. */
    public static final String CHASE = "Chase0201";
    public static final int ARMED = 1, RUNNING = 2, DONE = 3;
    /** How many times the chase turns round before it can break for the pond, and the longest it runs before it does. */
    public static final int FLIPS = 6, MIN_TICKS = 400, MAX_TICKS = 2400;
    /** Ahead of the reader, behind them, on the way to the island, crouched in the gap. */
    public enum Mode { FLEE, HUNT, BREAK, CROUCHED }
    public static final class Run {
        public Mode mode = Mode.FLEE;public int flips, ticks, lost, turned, target, crouched;public boolean forward = true;public UUID actor;double px, pz;
    }
    private static final Map<UUID, Run> RUNS = new HashMap<>();

    private DevilsRockNights() {}

    public static @Nullable Run run(ServerPlayer p) { return RUNS.get(p.getUUID()); }
    static int visit(CompoundTag own) { return Math.max(1, own.getInt("Visit")); }

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p, BlockPos b, CompoundTag own) {
        end(p, own, false);
        if (own.getInt(CHASE) == RUNNING) own.putInt(CHASE, ARMED);
    }
    /** The eighth page read: the chase waits at the rock. */
    public static void pagesRead(ServerPlayer p, CompoundTag own) {
        if (own.getInt(CHASE) >= ARMED) return;
        own.putInt(CHASE, ARMED);
        p.displayClientMessage(Component.literal("Out past the yard, the path to the rock is still there.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    // ------------------------------------------------------------------------------------------------ every five ticks
    public static void reader(ServerPlayer p, BlockPos b, CompoundTag own) {
        pages(p, b, own);
        if (visit(own) >= 3 && own.getInt("ThisVisitTicks") % 100 == 5)
            p.connection.send(new ClientboundBlockUpdatePacket(b.offset(DevilsRockHouse.BUREAU_PENNIES), Blocks.AIR.defaultBlockState()));
        if (own.getInt(CHASE) == ARMED && run(p) == null && visit(own) >= 3) {
            var rel = p.position().subtract(Vec3.atLowerCornerOf(b));
            if (rel.distanceTo(DevilsRockHouse.SPLIT) < 4.5 || Math.abs(rel.x) < 7.5 && rel.z < -64 && rel.z > -79) start(p, b, own);
        }
    }

    /** Three pages a visit, eight in all; each appears where no one is looking, after the reader has been in the house a while. */
    static void pages(ServerPlayer p, BlockPos b, CompoundTag own) {
        int target = Math.min(8, visit(own) * 3), placed = own.getInt("DiaryPlaced");
        if (own.getInt("ThisVisitTicks") >= 100) for (int i = 0; i < target; i++) {
            if ((placed & (1 << i)) != 0) continue;
            var rel = DevilsRockHouse.PAGES[i];var at = b.offset(rel);
            if (LiteraryVignettes.watched(p.serverLevel(), at.getCenter()) || LiteraryVignettes.watched(p.serverLevel(), at.above().getCenter())) continue;
            LiteraryRooms.prop(p.serverLevel(), b, rel, LiteraryPropBlock.Kind.PAGE, Direction.SOUTH);
            own.putInt("DiaryPlaced", placed | (1 << i));own.putLong("ArrivalTime" + i, p.serverLevel().getGameTime());
            LiteraryVignettes.heard(p, SoundEvents.BOOK_PAGE_TURN, at, .4F, .7F);
            break;
        }
        if (own.getInt("ThisVisitTicks") >= 900 && Integer.bitCount(own.getInt("DiaryPlaced")) < target) {
            int i = 0;while (i < target && (own.getInt("DiaryPlaced") & (1 << i)) != 0) i++;
            if (i < target) { own.putInt("DiaryPlaced", own.getInt("DiaryPlaced") | (1 << i));LiteraryVignettes.reward(p, PLACE, own, "Diary" + i, LiteraryTexts.diary(i)); }
        }
        int expected = (1 << target) - 1;
        if ((own.getInt("DiaryRead") & expected) == expected) own.putInt("Beat", own.getInt("Visit"));
    }

    // ------------------------------------------------------------------------------------------------ the account
    /** The reader's account: the pages and the camera, the shape if they caught it, the chase, and the island. */
    public static String account(CompoundTag own) {
        var text = new StringBuilder("I read the pages where they lay. The camera had a time for each one, and never a face.");
        boolean shape = own.getBoolean("ShapeSeen1") || own.getBoolean("ShapeSeen2") || own.getBoolean("ShapeSeen3");
        if (shape) text.append(" At the corner of my eye, something crouched in the corners of that house, the way a child hides. Whenever I looked, there was nobody.");
        text.append("\n\nThe last page asked me to tell him the path was still there. It was. It ran out past the yard to the split rock.");
        text.append("\n\nSomething in his shape came out of the trees there, with pennies over its eyes, and ran. I ran after it. Then it was behind me, and I was the one running. Neither of us said a word. Somewhere in the woods I stopped knowing which of us was chasing.");
        text.append("\n\nIt went into the pond and swam out to the island, and crouched between two rocks like it was hiding. When I looked straight at it, there was nobody there. The old pennies from his bureau were on the ground between the stones.");
        return text.toString();
    }

    // ------------------------------------------------------------------------------------------------ the two cameras
    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        int index = DevilsRockHouse.pages().indexOf(rel);
        if (index >= 0 && (own.getInt("DiaryPlaced") & (1 << index)) != 0) { LiteraryVignettes.open(p, PLACE, own, "Diary" + index, LiteraryTexts.diary(index), b.offset(rel));return true; }
        if (rel.equals(DevilsRockHouse.CAMERA)) {
            var prints = new StringBuilder("MOTION PRINT\n\n");
            for (int i = 0; i < 8; i++) if (own.contains("ArrivalTime" + i)) prints.append("Page ").append(i + 1).append(": ").append(own.getLong("ArrivalTime" + i)).append(" ticks\n");
            prints.append("\nThe paper was behind the lens before the image became mine.");
            LiteraryVignettes.open(p, PLACE, own, "CameraPrint" + visit(own), HouseWriting.book("Motion print", "The living-room camera", HouseWriting.WritingStyle.WILL, List.of(prints.toString())), b.offset(rel));
            return true;
        }
        if (rel.equals(DevilsRockHouse.TRAIL_CAMERA)) {
            LiteraryVignettes.open(p, PLACE, own, "TrailPrint" + Math.min(3, visit(own)), trailPrint(Math.min(3, visit(own))), b.offset(rel));
            return true;
        }
        return false;
    }
    public static net.minecraft.world.item.ItemStack trailPrint(int night) {
        String frames = switch (night) {
            case 1 -> "11:40 PM  No motion.\n12:55 AM  No motion.\n2:10 AM  No motion.\n\nThe card holds three nights of the rock and nothing on it.";
            case 2 -> "1:31 AM  No motion.\n2:13 AM  Motion. A shape on top of the rock, crouched, facing the trail.\n2:14 AM  No motion.";
            default -> "1:58 AM  Motion in the split.\n1:58 AM  Motion in the split.\n1:58 AM  Motion in the split.\n\nEvery frame has the same time on it.";
        };
        return HouseWriting.book("Trail camera", "The camera at the rock", HouseWriting.WritingStyle.WILL, List.of("TRAIL CAMERA\n\n" + frames));
    }

    // ------------------------------------------------------------------------------------------------ every tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        var d = LabyrinthData.get(e.getServer());
        for (var p : e.getServer().getPlayerList().getPlayers()) {
            if (!LiteraryVignettes.inside(p, PLACE)) { if (RUNS.containsKey(p.getUUID())) { var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);end(p, own, true);LiteraryVignettes.save(d, p.getUUID(), PLACE, own); } continue; }
            var b = LiteraryVignettes.base(p, PLACE);if (b == null) continue;
            var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);
            shape(p, b, own);
            var r = RUNS.get(p.getUUID());
            // A double left standing from before a restart has no run to belong to.
            if (r == null && p.tickCount % 100 == 0 && existing(p, own, "Double") != null) forget(p, "Double");
            if (r != null && step(p, b, own, r)) LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !RUNS.containsKey(p.getUUID())) return;
        var d = LabyrinthData.get(p.server);var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);end(p, own, true);LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
    }

    /** The shape: crouched where it is tonight, seen only out of the corner of the eye; gone when looked at, and gone once the chase begins. */
    static void shape(ServerPlayer p, BlockPos b, CompoundTag own) {
        int chase = own.getInt(CHASE);
        boolean wanted = chase != DONE && run(p) == null && !own.getBoolean("Completed");
        var a = wanted ? LiteraryVignettes.actor(p, PLACE, "Shape", LiteraryActor.SILHOUETTE, BlockPos.containing(DevilsRockHouse.SHAPE[0]), true) : existing(p, own, "Shape");
        if (a == null) return;
        if (!wanted) { a.setInvisible(true);return; }
        a.appearance(LiteraryActor.SILHOUETTE, 0);a.noPhysics = true;a.setNoGravity(true);a.setPose(Pose.CROUCHING);
        var spot = DevilsRockHouse.SHAPE[Math.min(3, visit(own)) - 1];var at = Vec3.atLowerCornerOf(b).add(spot);
        if (a.position().distanceToSqr(at) > .01) { a.moveTo(at.x, at.y, at.z, 180, 0);a.setYHeadRot(180);a.setYBodyRot(180); }
        boolean edge = edge(p, a.position().add(0, .6, 0));
        a.setInvisible(!edge);
        if (edge && !own.getBoolean("ShapeSeen" + visit(own))) own.putBoolean("ShapeSeen" + visit(own), true);
    }
    /** At the edge of sight: in clear view, close, and between about thirty-five and seventy-five degrees off where the reader is looking. */
    static boolean edge(ServerPlayer p, Vec3 point) {
        var to = point.subtract(p.getEyePosition());double dist = to.length();
        if (dist > 14 || dist < .5 || p.isSpectator()) return false;
        double cos = p.getLookAngle().dot(to.normalize());
        if (cos < .26 || cos > .82) return false;
        return clear(p, point);
    }
    static boolean clear(ServerPlayer p, Vec3 point) {
        var hit = p.level().clip(new ClipContext(p.getEyePosition(), point, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, p));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(point) < .5;
    }
    /** Discards a private actor and forgets its id, so the next call makes a fresh one. */
    static void forget(ServerPlayer p, String key) {
        var d = LabyrinthData.get(p.server);var s = LiteraryVignettes.shared(d, PLACE);var id = key + "_" + p.getUUID();
        if (s.hasUUID(id) && p.serverLevel().getEntity(s.getUUID(id)) instanceof LiteraryActor a) a.discard();
        if (s.contains(id)) { s.remove(id);LiteraryVignettes.shared(d, PLACE, s); }
    }
    static @Nullable LiteraryActor existing(ServerPlayer p, CompoundTag own, String key) {
        var s = LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE);var id = key + "_" + p.getUUID();
        return s.hasUUID(id) && p.serverLevel().getEntity(s.getUUID(id)) instanceof LiteraryActor a ? a : null;
    }

    // ------------------------------------------------------------------------------------------------ the chase
    static void start(ServerPlayer p, BlockPos b, CompoundTag own) {
        var a = LiteraryVignettes.actor(p, PLACE, "Double", LiteraryActor.SILHOUETTE, BlockPos.containing(DevilsRockHouse.TREES), true);
        if (a == null) return;
        var r = new Run();r.actor = a.getUUID();RUNS.put(p.getUUID(), r);
        a.appearance(LiteraryActor.SILHOUETTE, DOUBLE_PHASE);a.noPhysics = true;a.setNoGravity(true);a.setPose(Pose.STANDING);a.setInvisible(false);
        var at = Vec3.atLowerCornerOf(b).add(DevilsRockHouse.TREES);a.moveTo(at.x, at.y, at.z, -90, 0);
        // Out of the trees beside the rock, crashing, and off down the loop away from the reader.
        LiteraryVignettes.heard(p, SoundEvents.AZALEA_LEAVES_BREAK, BlockPos.containing(at), 1F, .6F);
        LiteraryVignettes.heard(p, SoundEvents.SWEET_BERRY_BUSH_BREAK, BlockPos.containing(at), .8F, .7F);
        aim(r, rel(b, p.position()), rel(b, a.position()));
        own.putInt(CHASE, RUNNING);own.putInt("ChaseStarts0201", own.getInt("ChaseStarts0201") + 1);
    }
    /** One tick of the chase. Returns whether anything saved changed. */
    static boolean step(ServerPlayer p, BlockPos b, CompoundTag own, Run r) {
        if (!(p.serverLevel().getEntity(r.actor) instanceof LiteraryActor a)) { RUNS.remove(p.getUUID());forget(p, "Double");own.putInt(CHASE, ARMED);return true; }
        var me = rel(b, p.position());var it = rel(b, a.position());
        // Back in the yard or the house: the chase is over until the rock again.
        if (!DevilsRockHouse.inPark(me) && r.mode != Mode.CROUCHED) { end(p, own, true);return true; }
        r.ticks++;
        double dist = horizontal(me, it);
        if (r.mode != Mode.HUNT) { r.px = me.x;r.pz = me.z; }
        switch (r.mode) {
            case FLEE -> {
                boolean seen = HouseWatchers.sees(p, a.getEyePosition());r.lost = seen ? 0 : r.lost + 1;
                if (r.flips >= FLIPS && r.ticks >= MIN_TICKS || r.ticks >= MAX_TICKS) { breakForPond(p, a, r, it);return false; }
                if (dist < 2.2 || r.lost >= 60) { behind(p, b, a, r, me);return false; }
                double speed = dist < 5 ? .33 : dist < 9 ? .27 : dist < 14 ? .2 : .07;
                var next = DevilsRockHouse.LOOP[r.target];
                if (horizontal(it, next) < .7) { r.target = Math.floorMod(r.target + (r.forward ? 1 : -1), DevilsRockHouse.LOOP.length);next = DevilsRockHouse.LOOP[r.target]; }
                move(a, b, it, next, speed);
                if (dist >= 14) face(a, me.subtract(it));
                if (r.ticks % 7 == 0) feet(p, a, .5F);
            }
            case HUNT -> {
                // Turned on: the reader looks straight at it and comes at it, and it runs again.
                var look = p.getLookAngle();var toIt = it.subtract(me).normalize();double mx = me.x - r.px, mz = me.z - r.pz;
                boolean charging = dist < 10 && look.dot(a.getEyePosition().subtract(p.getEyePosition()).normalize()) > .9 && mx * toIt.x + mz * toIt.z > .12;
                r.turned = charging ? r.turned + 1 : Math.max(0, r.turned - 1);
                if (r.turned >= 10) { flip(p, r, Mode.FLEE);aim(r, me, it);r.px = me.x;r.pz = me.z;return false; }
                if (dist < 1.3) { caught(p, b, a, r, me);return false; }
                r.px = me.x;r.pz = me.z;
                move(a, b, it, new Vec3(me.x, 0, me.z), .255);
                if (r.ticks % 6 == 0) feet(p, a, .8F);
                if (r.ticks % 31 == 0) LiteraryVignettes.heard(p, DareRegistry.BREATH.get(), a.blockPosition(), .7F, .8F);
            }
            case BREAK -> {
                var next = DevilsRockHouse.TO_ISLAND[r.target];
                boolean water = DevilsRockHouse.inPond(it.x, it.z) && !DevilsRockHouse.onIsland(it.x, it.z);
                if (horizontal(it, next) < .6) {
                    if (r.target == DevilsRockHouse.TO_ISLAND.length - 1) { crouch(a, b);r.mode = Mode.CROUCHED;return false; }
                    r.target++;next = DevilsRockHouse.TO_ISLAND[r.target];
                }
                move(a, b, it, next, water ? .17 : .24);
                if (water && r.ticks % 9 == 0) LiteraryVignettes.heard(p, SoundEvents.PLAYER_SWIM, a.blockPosition(), .6F, .9F);
                else if (!water && r.ticks % 7 == 0) feet(p, a, .5F);
            }
            case CROUCHED -> {
                r.crouched++;
                boolean looked = DevilsRockHouse.onIslandRel(me) && dist < 9 && p.getLookAngle().dot(a.getEyePosition().subtract(p.getEyePosition()).normalize()) > .97 && clear(p, a.getEyePosition());
                boolean unwatched = DevilsRockHouse.onIslandRel(me) && dist < 6 && r.crouched > 200 && !HouseWatchers.sees(p, a.getEyePosition());
                if (looked || unwatched) {
                    forget(p, "Double");RUNS.remove(p.getUUID());
                    LiteraryVignettes.heard(p, SoundEvents.BUCKET_EMPTY, a.blockPosition(), .3F, .6F);
                    own.putInt(CHASE, DONE);own.putInt("ChaseFlips0201", r.flips);
                    LiteraryVignettes.ready(p, PLACE, own, "followed_him_to_the_island");
                    return true;
                }
            }
        }
        return false;
    }
    static void flip(ServerPlayer p, Run r, Mode to) { r.mode = to;r.flips++;r.lost = 0;r.turned = 0; }
    /** Nearly caught, or lost sight of: it is behind the reader now, out of their view, and coming. */
    static void behind(ServerPlayer p, BlockPos b, LiteraryActor a, Run r, Vec3 me) {
        var look = p.getLookAngle();int best = -1;double score = Double.MAX_VALUE;
        for (int i = 0; i < DevilsRockHouse.LOOP.length; i++) {
            var pt = DevilsRockHouse.LOOP[i];double dist = horizontal(me, pt);if (dist < 7 || dist > 16) continue;
            var to = new Vec3(pt.x - me.x, 0, pt.z - me.z).normalize();double dot = look.x * to.x + look.z * to.z;
            if (dot > .2 || edge(p, Vec3.atLowerCornerOf(b).add(pt).add(0, 1, 0))) continue;
            if (dot < score) { score = dot;best = i; }
        }
        Vec3 spot = best >= 0 ? DevilsRockHouse.LOOP[best] : new Vec3(me.x - look.x * 10, 0, me.z - look.z * 10);
        var at = Vec3.atLowerCornerOf(b).add(spot.x, 0, spot.z);a.moveTo(at.x, at.y, at.z, a.getYRot(), 0);
        flip(p, r, Mode.HUNT);
        LiteraryVignettes.heard(p, SoundEvents.AZALEA_LEAVES_BREAK, a.blockPosition(), .9F, .5F);
    }
    /** Caught: a moment of dark and its breath, and then it is ahead of the reader, running. */
    static void caught(ServerPlayer p, BlockPos b, LiteraryActor a, Run r, Vec3 me) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false, false));
        LiteraryVignettes.heard(p, SoundEvents.WARDEN_HEARTBEAT, p.blockPosition(), 1F, 1.1F);
        LiteraryVignettes.heard(p, NovelRegistry.WELL_BREATH.get(), p.blockPosition(), 1F, .9F);
        var look = p.getLookAngle();int best = -1;double score = -2;
        for (int i = 0; i < DevilsRockHouse.LOOP.length; i++) {
            var pt = DevilsRockHouse.LOOP[i];double dist = horizontal(me, pt);if (dist < 6 || dist > 14) continue;
            var to = new Vec3(pt.x - me.x, 0, pt.z - me.z).normalize();double dot = look.x * to.x + look.z * to.z;
            if (dot > score) { score = dot;best = i; }
        }
        var spot = best >= 0 ? DevilsRockHouse.LOOP[best] : new Vec3(me.x + look.x * 8, 0, me.z + look.z * 8);
        var at = Vec3.atLowerCornerOf(b).add(spot.x, 0, spot.z);a.moveTo(at.x, at.y, at.z, a.getYRot(), 0);
        flip(p, r, Mode.FLEE);aim(r, me, spot);
    }
    static void breakForPond(ServerPlayer p, LiteraryActor a, Run r, Vec3 it) {
        r.mode = Mode.BREAK;r.target = 0;
        LiteraryVignettes.heard(p, SoundEvents.AZALEA_LEAVES_BREAK, a.blockPosition(), 1F, .7F);
    }
    static void crouch(LiteraryActor a, BlockPos b) {
        var at = Vec3.atLowerCornerOf(b).add(DevilsRockHouse.GAP);a.moveTo(at.x, at.y, at.z, 180, 0);a.setYHeadRot(180);a.setYBodyRot(180);
        a.setPose(Pose.CROUCHING);a.setDeltaMovement(Vec3.ZERO);
    }
    /** Ends a run: the double goes, and the chase waits at the rock again unless it was finished. */
    static void end(ServerPlayer p, CompoundTag own, boolean reset) {
        var r = RUNS.remove(p.getUUID());
        if (r != null && p.serverLevel().getEntity(r.actor) instanceof LiteraryActor a) a.discard();
        forget(p, "Double");
        if (reset && own.getInt(CHASE) == RUNNING) own.putInt(CHASE, ARMED);
    }

    // ------------------------------------------------------------------------------------------------ motion
    static void move(LiteraryActor a, BlockPos b, Vec3 from, Vec3 to, double speed) {
        var delta = new Vec3(to.x - from.x, 0, to.z - from.z);double len = delta.length();
        var step = len <= speed ? delta : delta.scale(speed / len);
        double y = groundY(b, from.x + step.x, from.z + step.z, to.y);
        a.moveTo(b.getX() + from.x + step.x, b.getY() + y, b.getZ() + from.z + step.z, a.getYRot(), 0);
        if (len > .01) face(a, delta);
    }
    /** The double runs at ground height, wades and swims in the pond, and climbs out onto the island. */
    static double groundY(BlockPos b, double x, double z, double waypoint) {
        if (DevilsRockHouse.inPond(x, z) && !DevilsRockHouse.onIsland(x, z)) return DevilsRockHouse.inPondMargin(x, z, -2.5) ? -1.4 : -.4;
        return 0;
    }
    static void face(LiteraryActor a, Vec3 dir) {
        float yaw = (float)(Math.atan2(-dir.x, dir.z) * 180 / Math.PI);a.setYRot(yaw);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
    }
    static void feet(ServerPlayer p, LiteraryActor a, float volume) { LiteraryVignettes.heard(p, SoundEvents.GRASS_STEP, a.blockPosition(), volume, .9F + p.getRandom().nextFloat() * .2F); }
    static Vec3 rel(BlockPos b, Vec3 at) { return at.subtract(b.getX(), b.getY(), b.getZ()); }
    static double horizontal(Vec3 a, Vec3 c) { double dx = a.x - c.x, dz = a.z - c.z;return Math.sqrt(dx * dx + dz * dz); }
    static int nearest(Vec3 at) {
        int best = 0;double d = Double.MAX_VALUE;
        for (int i = 0; i < DevilsRockHouse.LOOP.length; i++) { double h = horizontal(at, DevilsRockHouse.LOOP[i]);if (h < d) { d = h;best = i; } }
        return best;
    }
    /** Sets it running round the loop from where it is, in whichever direction takes it away from the reader. */
    static void aim(Run r, Vec3 me, Vec3 it) {
        int here = nearest(it), n = DevilsRockHouse.LOOP.length;
        var ahead = DevilsRockHouse.LOOP[Math.floorMod(here + 1, n)];var back = DevilsRockHouse.LOOP[Math.floorMod(here - 1, n)];
        r.forward = horizontal(me, ahead) >= horizontal(me, back);
        r.target = Math.floorMod(here + (r.forward ? 1 : -1), n);
    }
}
