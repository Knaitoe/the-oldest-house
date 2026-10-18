package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Native collision and interpolation between the literary script's five-tick decisions. */
public final class LiteraryMotion {
    private record Goal(Vec3 at, double speed, long until) {}
    private static final WeakHashMap<LiteraryActor, Goal> goals = new WeakHashMap<>();
    private record ElkStep(Vec3 velocity,long until) {}
    private static final WeakHashMap<LiteraryElk,ElkStep> elkSteps=new WeakHashMap<>();
    private static final String BLY_CYCLE = "BlyCycle0496";
    /** Readers the Lady is holding, until the game tick she lets go (0.4.105). */
    private static final java.util.Map<java.util.UUID, Long> held = new java.util.HashMap<>();
    private LiteraryMotion() {}

    static void hold(ServerPlayer reader, long until) { held.put(reader.getUUID(), until); }
    static void release(ServerPlayer reader) { held.remove(reader.getUUID()); }

    static void toward(LiteraryActor actor, Vec3 at, double fiveTickDistance) {
        goals.put(actor, new Goal(at, fiveTickDistance / 5, actor.level().getGameTime() + 6));
    }
    static void stop(LiteraryActor actor) { goals.remove(actor); actor.setDeltaMovement(Vec3.ZERO); }
    public static void clearAll() { goals.clear(); elkSteps.clear(); held.clear(); }
    static void legacyElk(LiteraryElk elk,Vec3 fiveTickStep){elkSteps.put(elk,new ElkStep(fiveTickStep.scale(.2),elk.level().getGameTime()+6));}
    public static void tickElk(LiteraryElk elk){
        if(!(elk.level() instanceof ServerLevel level))return;var step=elkSteps.get(elk);if(step==null)return;
        var base=IndianLakeRooms.base(level.getServer(),LabyrinthPlace.ELK_LOT);var data=LabyrinthData.get(level.getServer());
        if(base==null||ElkLot.built(data,base)||level.getGameTime()>step.until||level.players().stream().noneMatch(p->LiteraryVignettes.inside(p,LabyrinthPlace.ELK_LOT))){elkSteps.remove(elk);return;}
        if(!level.hasChunkAt(BlockPos.containing(elk.position().add(step.velocity))))return;
        Vec3 before=elk.position();elk.move(MoverType.SELF,step.velocity);elk.walkAnimation.update((float)elk.position().subtract(before).horizontalDistance()*4,.4F);
        float yaw=(float)Math.toDegrees(Math.atan2(-step.velocity.x,step.velocity.z));elk.setYRot(Mth.approachDegrees(elk.getYRot(),yaw,8));elk.setYBodyRot(elk.getYRot());elk.setYHeadRot(elk.getYRot());
    }

    /** A newly created body is placed before its native spawn, never reset in front of a reader. */
    static void initialLady(LiteraryActor actor, BlockPos base) {
        long now = actor.level().getGameTime();
        actor.moveTo(ladyPoint(base, (int)Math.floorMod(now, 900)), 180, 0);
        actor.setYBodyRot(180); actor.setYHeadRot(180); actor.setNoGravity(true);
        actor.getPersistentData().putLong(BLY_CYCLE, Math.floorDiv(now, 900));
    }

    private static Vec3 ladyPoint(BlockPos base, int clock) {
        return new Vec3(base.getX() + .5, base.getY() - Mth.clamp((clock - 740) * .08, 0, 4),
                base.getZ() - 3.5 - Math.min(44, clock * .06));
    }

    public static void tick(LiteraryActor actor) {
        if (!(actor.level() instanceof ServerLevel level)) return;
        if (actor.role() == LiteraryActor.LADY && actor.scene().equals(LabyrinthPlace.BLY_ROUTE.id())) {
            lady(actor, level); return;
        }
        if (actor.role() == LiteraryActor.DANCER && actor.scene().equals(LabyrinthPlace.MASQUE.id())) {
            if(actor.phase()==1){float yaw=(float)(level.getGameTime()%360);actor.setYRot(yaw);actor.setYBodyRot(yaw);actor.setYHeadRot(yaw);}return;
        }
        var goal = goals.get(actor);
        if (goal == null) return;
        var reader = actor.owner().map(level.getServer().getPlayerList()::getPlayer).orElse(null);
        var place=LabyrinthPlace.byId(actor.scene());
        if (reader == null || place==null || !LiteraryVignettes.inside(reader, place)
                || level.getGameTime() > goal.until) { goals.remove(actor); return; }
        if(place==LabyrinthPlace.HOLY_RABBIT&&actor.role()==LiteraryActor.FATHER){
            var own=LiteraryVignettes.personal(LabyrinthData.get(level.getServer()),reader.getUUID(),place);
            if(!LiteraryVignettes.retreatLocked(reader)||own.getInt("Night")>=3&&!own.getBoolean("LastHunt0490")){stop(actor);return;}
        }
        Vec3 delta = goal.at.subtract(actor.position()).multiply(1, 0, 1);
        if (delta.lengthSqr() <= .04) { goals.remove(actor); return; }
        Vec3 step = delta.normalize().scale(Math.min(goal.speed, delta.length()));
        if (!level.hasChunkAt(BlockPos.containing(actor.position().add(step)))) return;
        Vec3 before=actor.position();actor.move(MoverType.SELF, step);
        actor.walkAnimation.update((float)actor.position().subtract(before).horizontalDistance()*4,.4F);
        float yaw = (float)Math.toDegrees(Math.atan2(-step.x, step.z));
        actor.setYRot(Mth.approachDegrees(actor.getYRot(), yaw, 8));
        actor.setYBodyRot(actor.getYRot()); actor.setYHeadRot(actor.getYRot());
    }

    private static void lady(LiteraryActor actor, ServerLevel level) {
        var base = IndianLakeRooms.base(level.getServer(), LabyrinthPlace.BLY_ROUTE);
        if (base == null || level.players().stream().noneMatch(p -> LiteraryVignettes.inside(p, LabyrinthPlace.BLY_ROUTE)
                && LiteraryVignettes.personal(LabyrinthData.get(level.getServer()), p.getUUID(), LabyrinthPlace.BLY_ROUTE).getBoolean("Here"))) return;
        long now = level.getGameTime(), cycle = Math.floorDiv(now, 900);
        int clock = (int)Math.floorMod(now, 900);
        var saved = actor.getPersistentData();
        if (!saved.contains(BLY_CYCLE) || saved.getLong(BLY_CYCLE) != cycle) {
            // Both the old body and destination must be unseen, including by native observers.
            boolean watched = LiteraryVignettes.watched(level, actor.getEyePosition())
                    || LiteraryVignettes.watched(level, ladyPoint(base, clock).add(0, actor.getEyeHeight(), 0));
            if (!watched) { initialLady(actor, base); return; }
            // An observed old save adopts the controller without relocating the body.
            if (!saved.contains(BLY_CYCLE)) saved.putLong(BLY_CYCLE, cycle);
            else return;
        }
        actor.setNoGravity(true); actor.setDeltaMovement(Vec3.ZERO);
        Vec3 delta = ladyPoint(base, clock).subtract(actor.position());
        double length = delta.horizontalDistance();
        Vec3 step = new Vec3(length < .00001 ? 0 : delta.x / length * Math.min(.06, length),
                Mth.clamp(delta.y, -.08, .08), length < .00001 ? 0 : delta.z / length * Math.min(.06, length));
        if (!level.hasChunkAt(BlockPos.containing(actor.position().add(step)))) return;
        Vec3 before=actor.position();actor.move(MoverType.SELF, step);
        actor.walkAnimation.update((float)actor.position().subtract(before).horizontalDistance()*4,.4F);
        actor.setYRot(Mth.approachDegrees(actor.getYRot(), 180, 8));
        actor.setYBodyRot(actor.getYRot()); actor.setYHeadRot(actor.getYRot());
        // A held reader is carried a pace ahead of her on her own route, lifted, facing her.
        held.entrySet().removeIf(entry -> {
            var reader = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (reader == null || entry.getValue() <= now || reader.level() != level) return true;
            reader.teleportTo(level, actor.getX(), actor.getY() + .3, actor.getZ() - .7, 0, -10);
            reader.setDeltaMovement(Vec3.ZERO); reader.fallDistance = 0;
            return false;
        });
    }
}
