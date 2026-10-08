package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * The elk carcasses' two stages for one reader (0.4.50), driving that reader's own killer.
 *
 * <p>Aboard: he waits below until the reader has found enough of the dead (or long enough has
 * passed), then comes for them; the only way off is over the side. He will not swim after them:
 * he watches from the rail, and goes into the lake only once the reader is ashore. In the woods
 * he walks a search, runs at anyone he sees, and loses them behind trees and undergrowth. A
 * reader who lies still in the hollow under the carcasses brings him into the cave; he passes the
 * gap twice, crouches at the pile, and leaves. Moving while he is near gives them away. Only a
 * reader who held still and watched his boots go by, then reached the crew's gate, has the
 * account the ending ledger takes.
 *
 * <p>Every killer is private to its reader, like the other personal cast: a second reader on the
 * same yacht has their own. The killer never damages anyone but his reader, and nothing here
 * changes another reader's state or the shared scene.
 */
public final class ElkHunt {
    public static final int ABOARD = 0, HUNTED = 1, IN_WATER = 2, ASHORE = 3, SEARCH = 4, SPENT = 5;
    /** Watching, searching, running, crouched at the pile: the killer's model phases. */
    public static final int WATCH = 0, WALK = 1, RUN = 2, PEER = 3;
    private static final double SEARCH_SPEED = .75, CHASE_SPEED = 1.35;
    private static final int[][] LANDINGS = {{30, -60, 36, -65}, {-26, -58, -32, -64}, {10, -64, 10, -72}};
    private static final LabyrinthPlace PLACE = LabyrinthPlace.ELK_CARCASSES;

    private ElkHunt() {}

    /** A new crossing starts on the yacht again; facts the reader has already earned are kept. */
    public static void arrive(ServerPlayer p, CompoundTag own) {
        own.putInt("ElkStage", ABOARD);
        own.putInt("ElkSeenVisit", 0);
        own.putInt("ElkClock", 0);
        own.putBoolean("ElkLanded", false);
        own.putBoolean("ElkResetPending", true);
        own.putInt("ElkRoute", 0);
        own.putInt("ElkEmerge", 0);
        own.putInt("ElkHidden", 0);
        own.putBoolean("ElkBetrayed", false);
        own.putBoolean("ElkHintCrawl", false);
        own.putBoolean("ElkFound", false);
        own.remove("ElkLastSeen");
    }

    /** The reader's own killer, adopting the scene's earlier shared one (with its identity) when there is one. */
    public static @Nullable LiteraryActor killer(ServerPlayer p, BlockPos b) {
        var d = LabyrinthData.get(p.server);
        var world = LiteraryVignettes.shared(d, PLACE);
        String id = "Killer_" + p.getUUID();
        if (!world.hasUUID(id) && world.hasUUID("Killer")) {
            var legacy = p.serverLevel().getEntity(world.getUUID("Killer"));
            if (legacy instanceof LiteraryActor a && a.isAlive()) {
                a.bind(PLACE, p.getUUID());
                world.putUUID(id, a.getUUID());
                world.remove("Killer");
                LiteraryVignettes.shared(d, PLACE, world);
            }
        }
        var a = LiteraryVignettes.actor(p, PLACE, "Killer", LiteraryActor.KILLER, ElkCarcassMap.KILLER_START, true);
        if (a != null && a.isNoAi()) {
            a.setNoAi(false);
            var range = a.getAttribute(Attributes.FOLLOW_RANGE);
            if (range != null) range.setBaseValue(72);
        }
        return a;
    }

    private static Vec3 rel(ServerPlayer p, BlockPos b) {
        return p.position().subtract(b.getX(), b.getY(), b.getZ());
    }

    private static Vec3 at(BlockPos b, BlockPos rel) {
        return Vec3.atBottomCenterOf(b.offset(rel));
    }

    public static boolean hidden(ServerPlayer p, BlockPos b) {
        return ElkCarcassMap.inHollow(rel(p, b)) && p.getPose() == Pose.SWIMMING;
    }

    /** The reader gets down to crawl in front of the pile (by crouching), and stays down beneath it. */
    public static boolean crawl(ServerPlayer p, BlockPos b, boolean crawling) {
        var r = rel(p, b);
        if (!ElkCarcassMap.crawlZone(r)) return false;
        return crawling || p.isShiftKeyDown() || ElkCarcassMap.inHollow(r);
    }

    public static void tick(ServerPlayer p, BlockPos b, CompoundTag own) {
        // A saved world's earlier scene is still standing until it is empty and carved again: nothing runs in it.
        if (!LabyrinthBuilder.isPlaceReady(p.server, PLACE)) return;
        var killer = killer(p, b);
        if (killer == null) return;
        Vec3 r = rel(p, b);
        if(KillerNavigation.failed(killer)){
            own.remove("ElkLastSeen");own.putBoolean("ElkFound",false);own.putInt("ElkPause",20);
            own.putInt("ElkWaypoint",Math.floorMod(own.getInt("ElkWaypoint")+1,ElkCarcassMap.patrol().size()));
            if(own.getInt("ElkStage")==SEARCH){setStage(own,ASHORE);own.putInt("ElkHidden",0);own.putInt("ElkPasses",0);}
        }
        int stage = own.getInt("ElkStage");
        own.putInt("ElkClock", own.getInt("ElkClock") + 5);
        if (own.getBoolean("ElkResetPending")) {
            own.putBoolean("ElkResetPending", false);
            KillerNavigation.stop(killer);
            killer.setNoGravity(false);
            killer.moveTo(at(b, ElkCarcassMap.KILLER_START));
            killer.appearance(LiteraryActor.KILLER, WATCH);
        }
        noticeBodies(p, b, own);
        boolean moved = moved(p, own);
        switch (stage) {
            case ABOARD -> {
                KillerNavigation.stop(killer);
                killer.appearance(LiteraryActor.KILLER, WATCH);
                int seen = Integer.bitCount(own.getInt("ElkSeenVisit")), t = own.getInt("ThisVisitTicks");
                boolean topside = r.y > ElkCarcassMap.MAIN - .6;
                if (seen >= 3 && t >= 200 || t >= 1200 || topside && seen >= 2 && t >= 300 || !ElkCarcassMap.aboard(r)) reveal(p, b, killer, own, topside);
            }
            case HUNTED -> {
                if (emerging(b, killer, own)) break;
                if (!ElkCarcassMap.aboard(r) && (p.isInWater() || p.getY() < b.getY() - .5)) { setStage(own, IN_WATER); break; }
                chase(p, killer, own, CHASE_SPEED * .92);
            }
            case IN_WATER -> {
                if (ElkCarcassMap.aboard(r) && !p.isInWater()) { setStage(own, HUNTED); break; }
                watchFromRail(p, b, killer);
                if (ashore(p, r)) {
                    setStage(own, ASHORE);
                    own.putBoolean("EscapedYacht", true);
                    own.putInt("ElkLand", 0);
                }
            }
            case ASHORE -> hunt(p, b, killer, own, r, moved);
            case SEARCH -> search(p, b, killer, own, r, moved);
            case SPENT -> {
                if (killer.getNavigation().isDone()) {
                    var shore = ElkCarcassMap.standAt(LANDINGS[0][2], LANDINGS[0][3]);
                    if (killer.position().distanceToSqr(at(b, shore)) > 9) KillerNavigation.request(killer,new Vec3(b.getX() + shore.getX() + .5, b.getY() + shore.getY(), b.getZ() + shore.getZ() + .5), SEARCH_SPEED);
                    else { killer.appearance(LiteraryActor.KILLER, WATCH); killer.getLookControl().setLookAt(b.getX(), b.getY() + 4, b.getZ() - 20); }
                }
            }
            default -> setStage(own, ABOARD);
        }
        if (own.getBoolean("PassedSearch") && own.getBoolean("BootsSeen") && ElkCarcassMap.nearEnding(r))
            LiteraryVignettes.ready(p, PLACE, own, "held_still_beneath_the_hides_and_left_by_service_path");
    }

    /** Loud native interactions remember their actual position for this reader's killer alone. */
    public static void noise(ServerPlayer p,BlockPos at){
        if(!LiteraryVignettes.participant(p)||!LiteraryVignettes.inside(p,PLACE))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(!own.getBoolean("Here")||own.getInt("ElkStage")<ASHORE)return;
        var world=LiteraryVignettes.shared(d,PLACE);String key="Killer_"+p.getUUID();
        if(!world.hasUUID(key)||!(p.serverLevel().getEntity(world.getUUID(key)) instanceof LiteraryActor a)||a.position().distanceToSqr(at.getCenter())>=32*32)return;
        own.putLong("ElkLastSeen",p.serverLevel().getGameTime());own.putDouble("ElkSeenX",at.getX()+.5);own.putDouble("ElkSeenY",at.getY());own.putDouble("ElkSeenZ",at.getZ()+.5);
        if(own.getInt("ElkStage")==SEARCH&&a.distanceToSqr(p)<16*16)own.putBoolean("ElkBetrayed",true);
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }

    private static void setStage(CompoundTag own, int stage) {
        own.putInt("ElkStage", stage);
        own.putInt("ElkClock", 0);
    }

    /** Horizontal movement since the last look, from positions, not the client-driven velocity. */
    private static boolean moved(ServerPlayer p, CompoundTag own) {
        boolean moved = own.contains("ElkLastX") && Math.hypot(p.getX() - own.getDouble("ElkLastX"), p.getZ() - own.getDouble("ElkLastZ")) > .12;
        own.putDouble("ElkLastX", p.getX());
        own.putDouble("ElkLastZ", p.getZ());
        return moved;
    }

    /** Which of the dead this reader has actually looked at, this crossing and in all. */
    private static void noticeBodies(ServerPlayer p, BlockPos b, CompoundTag own) {
        int visit = own.getInt("ElkSeenVisit"), all = own.getInt("ElkSeenAll");
        for (int i = 0; i < ElkCarcassMap.GUESTS.size(); i++) {
            if ((visit & 1 << i) != 0) continue;
            var body = Vec3.atBottomCenterOf(b.offset(ElkCarcassMap.GUESTS.get(i))).add(0, .3, 0);
            if (p.position().distanceToSqr(body) < 100 && HouseWatchers.sees(p, body)) { visit |= 1 << i; all |= 1 << i; }
        }
        own.putInt("ElkSeenVisit", visit);
        own.putInt("ElkSeenAll", all);
    }

    private static void reveal(ServerPlayer p, BlockPos b, LiteraryActor killer, CompoundTag own, boolean topside) {
        setStage(own, HUNTED);
        var level = p.serverLevel();
        if (topside) {
            // He comes up through the foredeck hatch.
            own.putInt("ElkEmerge", 1);
            killer.setNoGravity(true);
            KillerNavigation.stop(killer);
            killer.moveTo(b.getX() + .5, b.getY() + ElkCarcassMap.LOWER, b.getZ() + ElkCarcassMap.HATCH.getZ() + .5, 180, 0);
            level.playSound(null, b.offset(ElkCarcassMap.HATCH), SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.2F, .7F);
        } else level.playSound(null, b.offset(3, 0, -24), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.2F, .6F);
        p.displayClientMessage(Component.literal("He is still aboard."), true);
    }

    /** The slow climb out of the hatch, a block at a time, before he steps onto the deck. */
    private static boolean emerging(BlockPos b, LiteraryActor killer, CompoundTag own) {
        int step = own.getInt("ElkEmerge");
        if (step == 0) return false;
        if (step <= ElkCarcassMap.MAIN) {
            killer.setPos(b.getX() + .5, b.getY() + step, b.getZ() + ElkCarcassMap.HATCH.getZ() + .5);
            killer.appearance(LiteraryActor.KILLER, WATCH);
            own.putInt("ElkEmerge", step + 1);
            return true;
        }
        killer.setNoGravity(false);
        killer.moveTo(at(b, ElkCarcassMap.HATCH_DECK));
        own.putInt("ElkEmerge", 0);
        return false;
    }

    /** Runs at the reader and strikes when close. Never anyone else. */
    private static void chase(ServerPlayer p, LiteraryActor killer, CompoundTag own, double speed) {
        killer.appearance(LiteraryActor.KILLER, RUN);
        KillerNavigation.request(killer,p.position(),speed);
        killer.getLookControl().setLookAt(p, 30, 30);
        strike(p, killer, own);
    }

    private static boolean strike(ServerPlayer p, LiteraryActor killer, CompoundTag own) {
        long now = p.serverLevel().getGameTime();
        if (killer.distanceToSqr(p) > 3.4 || now - own.getLong("ElkStrikeAt") < 25 || !LiteraryVignettes.participant(p)
                || killer.level().clip(new net.minecraft.world.level.ClipContext(killer.getEyePosition(),p.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,killer)).getType()!=net.minecraft.world.phys.HitResult.Type.MISS) return false;
        own.putLong("ElkStrikeAt", now);
        killer.swing(InteractionHand.MAIN_HAND);
        p.serverLevel().playSound(null, killer.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 1, .7F);
        p.hurt(p.damageSources().mobAttack(killer), 4);
        return true;
    }

    private static boolean ashore(ServerPlayer p, Vec3 r) {
        return !p.isInWater() && p.onGround() && !ElkCarcassMap.aboard(r);
    }

    private static void watchFromRail(ServerPlayer p, BlockPos b, LiteraryActor killer) {
        killer.appearance(LiteraryActor.KILLER, WATCH);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int z = 0; z >= -40; z -= 2) {
            int hw = ElkCarcassMap.halfBeam(z) - 1;
            for (int x : new int[]{-hw, hw}) {
                var rail = new BlockPos(x, ElkCarcassMap.MAIN, z);
                double d = at(b, rail).distanceToSqr(p.position());
                if (d < bestDistance) { bestDistance = d; best = rail; }
            }
        }
        if (best != null && killer.position().distanceToSqr(at(b, best)) > 2.5)
            KillerNavigation.request(killer,new Vec3(b.getX() + best.getX() + .5, b.getY() + best.getY(), b.getZ() + best.getZ() + .5), 1.0);
        killer.getLookControl().setLookAt(p, 20, 20);
    }

    /** Whether he can see the reader: in front of him, in the open, within reach of his eyes. */
    private static boolean sees(ServerPlayer p, LiteraryActor killer, BlockPos b) {
        if (hidden(p, b) || !LiteraryVignettes.participant(p) || killer.distanceToSqr(p)>9&&CarcassHunt.concealed(p,b)) return false;
        double range = p.isShiftKeyDown() ? (cover(p) ? 5 : 12) : 28;
        Vec3 to = p.getEyePosition().subtract(killer.getEyePosition());
        double d = to.length();
        if (d > range) return false;
        if (d > 5 && killer.getLookAngle().dot(to.scale(1 / d)) < .2) return false;
        return killer.hasLineOfSight(p);
    }

    private static boolean cover(ServerPlayer p) {
        var at = p.blockPosition();
        var s = p.serverLevel().getBlockState(at);
        return !s.isAir() && !s.isCollisionShapeFullBlock(p.serverLevel(), at) || p.serverLevel().getBlockState(at.above()).is(net.minecraft.tags.BlockTags.LEAVES);
    }

    private static void hunt(ServerPlayer p, BlockPos b, LiteraryActor killer, CompoundTag own, Vec3 r, boolean moved) {
        var level = p.serverLevel();
        if (!own.getBoolean("ElkLanded")) {
            own.putInt("ElkLand", own.getInt("ElkLand") + 5);
            killer.appearance(LiteraryActor.KILLER, WATCH);
            killer.getLookControl().setLookAt(p, 20, 20);
            boolean watched = HouseWatchers.sees(p, killer.getEyePosition()) && p.distanceTo(killer) < 40;
            if (own.getInt("ElkLand") >= 100 && (!watched || own.getInt("ElkLand") >= 600)) land(p, b, killer, own);
            else if (!hidden(p, b)) return;
        }
        if (hidden(p, b) && own.getBoolean("ElkFound")) {
            // Found: he crouches at the gap and reaches in under the hides until the reader gets out.
            var peer = at(b, ElkCarcassMap.PEER);
            if (killer.position().distanceToSqr(peer) > 4) { killer.appearance(LiteraryActor.KILLER, RUN); KillerNavigation.request(killer,new Vec3(peer.x, peer.y, peer.z), CHASE_SPEED); }
            else {
                KillerNavigation.stop(killer);
                killer.appearance(LiteraryActor.KILLER, PEER);
                killer.getLookControl().setLookAt(p, 30, 30);
                long now = level.getGameTime();
                if (now - own.getLong("ElkStrikeAt") >= 30) {
                    own.putLong("ElkStrikeAt", now);
                    killer.swing(InteractionHand.MAIN_HAND);
                    p.hurt(p.damageSources().mobAttack(killer), 3);
                }
            }
            return;
        }
        if (!hidden(p, b)) own.putBoolean("ElkFound", false);
        if (hidden(p, b)) {
            if (moved) { own.putInt("ElkHidden", 0); return; }
            own.putInt("ElkHidden", own.getInt("ElkHidden") + 5);
            if (own.getInt("ElkHidden") == 5 && own.contains("ElkLastSeen") && level.getGameTime() - own.getLong("ElkLastSeen") < 60 && killer.distanceTo(p) < 12)
                own.putBoolean("ElkBetrayed", true); // he saw where they went
            if (own.getInt("ElkHidden") >= 60) {
                setStage(own, SEARCH);
                own.putInt("ElkRoute", 0);
                own.putInt("ElkPasses", 0);
                if (!own.getBoolean("ElkLanded")) land(p, b, killer, own);
                // A long walk is cut short out of sight: the reader is in the dark under the pile.
                if (killer.position().distanceToSqr(at(b, ElkCarcassMap.MOUTH)) > 70 * 70) {
                    var near = ElkCarcassMap.standAt(2, -167);
                    KillerNavigation.stop(killer);
                    killer.moveTo(at(b, near));
                }
            }
            return;
        }
        own.putInt("ElkHidden", 0);
        if (ElkCarcassMap.crawlZone(r) && !own.getBoolean("ElkHintCrawl") && p.getPose() != Pose.SWIMMING) {
            own.putBoolean("ElkHintCrawl", true);
            p.displayClientMessage(Component.literal("Crouch to get down under the hides."), true);
        }
        long now = level.getGameTime();
        if (sees(p, killer, b)) {
            own.putLong("ElkLastSeen", now);
            own.putDouble("ElkSeenX", p.getX());
            own.putDouble("ElkSeenY", p.getY());
            own.putDouble("ElkSeenZ", p.getZ());
            chase(p, killer, own, CHASE_SPEED);
            return;
        }
        if (strike(p, killer, own)) return;
        if (own.contains("ElkLastSeen") && now - own.getLong("ElkLastSeen") < 160) {
            // Where they were last seen, then a look round.
            killer.appearance(LiteraryActor.KILLER, WALK);
            KillerNavigation.request(killer,new Vec3(own.getDouble("ElkSeenX"), own.getDouble("ElkSeenY"), own.getDouble("ElkSeenZ")), 1.1);
            return;
        }
        patrol(p, b, killer, own);
    }

    /** Into the lake out of the reader's sight, then up out of it on the shore farthest from them. */
    private static void land(ServerPlayer p, BlockPos b, LiteraryActor killer, CompoundTag own) {
        var level = p.serverLevel();
        int[] best = LANDINGS[0];
        double far = -1;
        for (int[] l : LANDINGS) {
            double d = p.position().distanceToSqr(at(b, new BlockPos(l[2], 0, l[3])));
            if (d > far) { far = d; best = l; }
        }
        level.playSound(null, b.offset(0, 0, -38), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2F, .7F);
        var water = new BlockPos(best[0], ElkCarcassMap.stand(best[0], best[1]), best[1]);
        KillerNavigation.stop(killer);
        killer.moveTo(at(b, water));
        var shore = ElkCarcassMap.standAt(best[2], best[3]);
        KillerNavigation.request(killer,new Vec3(b.getX() + shore.getX() + .5, b.getY() + shore.getY(), b.getZ() + shore.getZ() + .5), SEARCH_SPEED);
        own.putBoolean("ElkLanded", true);
        own.putInt("ElkWaypoint", -1);
    }

    private static void patrol(ServerPlayer p, BlockPos b, LiteraryActor killer, CompoundTag own) {
        List<BlockPos> points = ElkCarcassMap.patrol();
        int i = own.getInt("ElkWaypoint");
        if (i < 0 || i >= points.size()) {
            // Begin from wherever he came ashore.
            i = nearest(points, killer.position().subtract(b.getX(), b.getY(), b.getZ()));
            own.putInt("ElkWaypoint", i);
        }
        var target = at(b, points.get(i));
        int pause = own.getInt("ElkPause");
        if (pause > 0) {
            own.putInt("ElkPause", pause - 5);
            killer.appearance(LiteraryActor.KILLER, WATCH);
            KillerNavigation.stop(killer);
            return;
        }
        if (killer.position().distanceToSqr(target) < 6) {
            own.putInt("ElkPause", 40 + p.getRandom().nextInt(50));
            // Every other leg he drifts toward the part of the valley the reader is in.
            int next = (i + 1) % points.size();
            if (p.getRandom().nextBoolean()) next = nearest(points, p.position().subtract(b.getX(), b.getY(), b.getZ()), i);
            own.putInt("ElkWaypoint", next);
            own.putDouble("ElkStuckX", killer.getX());
            own.putDouble("ElkStuckZ", killer.getZ());
            own.putInt("ElkStuck", 0);
            return;
        }
        killer.appearance(LiteraryActor.KILLER, WALK);
        if (killer.getNavigation().isDone()) KillerNavigation.request(killer,new Vec3(target.x, target.y, target.z), SEARCH_SPEED);
    }

    private static int nearest(List<BlockPos> points, Vec3 rel) {
        return nearest(points, rel, -1);
    }

    private static int nearest(List<BlockPos> points, Vec3 rel, int except) {
        int best = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (i == except) continue;
            double d = Vec3.atBottomCenterOf(points.get(i)).distanceToSqr(rel);
            if (d < bestDistance) { bestDistance = d; best = i; }
        }
        return best;
    }

    /** Route through the cave: in, past the gap twice, a crouch at the pile, and out again. */
    private static final BlockPos[] ROUTE = {ElkCarcassMap.MOUTH, ElkCarcassMap.CAVE_ENTRY, ElkCarcassMap.PASS_WEST, ElkCarcassMap.PASS_EAST,
            ElkCarcassMap.PEER, ElkCarcassMap.CAVE_ENTRY, ElkCarcassMap.MOUTH, ElkCarcassMap.CAVE_EXIT};

    private static BlockPos route(int i) {
        var p = ROUTE[i];
        return p == ElkCarcassMap.CAVE_EXIT ? ElkCarcassMap.standAt(p.getX(), p.getZ()) : p;
    }

    private static void search(ServerPlayer p, BlockPos b, LiteraryActor killer, CompoundTag own, Vec3 r, boolean moved) {
        var level = p.serverLevel();
        int i = own.getInt("ElkRoute");
        boolean near = killer.position().distanceToSqr(at(b, ElkCarcassMap.PEER)) < 16 * 16;
        boolean stillHidden = hidden(p, b);
        // Giving themselves away: moving, or leaving the hollow, while he is close; or he saw them go in.
        if (near && (moved || !stillHidden) || own.getBoolean("ElkBetrayed")) {
            own.putBoolean("ElkBetrayed", false);
            own.putBoolean("ElkFound", stillHidden);
            own.putInt("ElkHidden", 0);
            level.playSound(null, killer.blockPosition(), SoundEvents.MUD_HIT, SoundSource.HOSTILE, 1.4F, .6F);
            p.displayClientMessage(Component.literal("He heard you."), true);
            setStage(own, ASHORE);
            own.putLong("ElkLastSeen", level.getGameTime());
            own.putDouble("ElkSeenX", p.getX());
            own.putDouble("ElkSeenY", p.getY());
            own.putDouble("ElkSeenZ", p.getZ());
            KillerNavigation.request(killer,new Vec3(b.getX() + ElkCarcassMap.PEER.getX() + .5, b.getY() + ElkCarcassMap.CAVE_Y, b.getZ() + ElkCarcassMap.PEER.getZ() + .5), CHASE_SPEED);
            return;
        }
        if (!stillHidden && !near) {
            // Left the hollow before he came: he keeps hunting, starting near the cave.
            setStage(own, ASHORE);
            return;
        }
        if (stillHidden && ElkCarcassMap.inCave(killer.position().subtract(b.getX(), b.getY(), b.getZ()))
                && HouseWatchers.sees(p, killer.position().add(0, .2, 0))) own.putBoolean("BootsSeen", true);
        var target = at(b, route(i));
        int pause = own.getInt("ElkPause");
        if (pause > 0) {
            own.putInt("ElkPause", pause - 5);
            KillerNavigation.stop(killer);
            if (ROUTE[i] == ElkCarcassMap.PEER) {
                killer.appearance(LiteraryActor.KILLER, PEER);
                killer.getLookControl().setLookAt(b.getX() + ElkCarcassMap.GAP_X0 + 1, b.getY() + ElkCarcassMap.CAVE_Y, b.getZ() + ElkCarcassMap.GAP_Z + .5);
                if (pause == 30) level.playSound(null, killer.blockPosition(), SoundEvents.MUD_STEP, SoundSource.HOSTILE, 1, .5F);
            } else killer.appearance(LiteraryActor.KILLER, WATCH);
            if (pause - 5 <= 0) { own.putInt("ElkRoute", i + 1); if (ROUTE[i] == ElkCarcassMap.PASS_WEST || ROUTE[i] == ElkCarcassMap.PASS_EAST) own.putInt("ElkPasses", own.getInt("ElkPasses") + 1); }
            return;
        }
        if (killer.position().distanceToSqr(target) < (i == ROUTE.length - 1 ? 9 : 1.8)) {
            if (i == ROUTE.length - 1) {
                own.putBoolean("PassedSearch", true);
                if (own.getBoolean("BootsSeen")) {
                    setStage(own, SPENT);
                    p.displayClientMessage(Component.literal("The boots do not come back. The crew's path leads on to their gate."), true);
                } else {
                    setStage(own, ASHORE);
                    own.putInt("ElkWaypoint", -1);
                    p.displayClientMessage(Component.literal("You never saw him go. He will be back."), true);
                }
                return;
            }
            own.putInt("ElkPause", ROUTE[i] == ElkCarcassMap.PEER ? 70 : ROUTE[i] == ElkCarcassMap.PASS_WEST || ROUTE[i] == ElkCarcassMap.PASS_EAST ? 20 : 5);
            return;
        }
        killer.appearance(LiteraryActor.KILLER, WALK);
        KillerNavigation.request(killer,new Vec3(target.x, target.y, target.z), i <= 1 ? 1.0 : SEARCH_SPEED);
    }
}
