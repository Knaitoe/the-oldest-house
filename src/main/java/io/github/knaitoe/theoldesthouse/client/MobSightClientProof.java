package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.gametest.MobSightProof;
import io.github.knaitoe.theoldesthouse.house.HouseMobSight;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Native registered-renderer proof on the actual dedicated server's moving entities. */
public final class MobSightClientProof {
    private static int phase;
    private static Vec3 first;
    private static UUID identity;
    private MobSightClientProof() {}
    private static void require(boolean okay, String message) {
        if (!okay) throw new IllegalStateException("LIVE MOB SIGHT CLIENT: " + message);
    }
    public static boolean ready(Minecraft mc, int step, int ticks, String role) {
        if (phase != step) { phase = step;first = null;identity = null; }
        mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);
        if (step == 39) return ticks > 30;
        mc.options.entityDistanceScaling().set(.5D);
        mc.options.fov().set(30);
        var camera = mc.gameRenderer.getMainCamera().getPosition();
        // A received dimension/marker and client ticks can precede its first rendered
        // frame. Do not test the new world's mob against the previous world's camera.
        if(camera.distanceToSqr(mc.player.getEyePosition())>64)return false;
        if(step==38&&!HouseSightlineState.hasOrigin())return false;
        var mobs = mc.level.getEntitiesOfClass(Mob.class, new AABB(mc.player.blockPosition()).inflate(160));
        String name = step == 37 ? MobSightProof.WOLF : MobSightProof.MIRROR;
        var moving = mobs.stream().filter(mob -> mob.getCustomName() != null && mob.getCustomName().getString().equals(name)).findFirst().orElse(null);
        if (moving == null || ticks < 40) return false;
        if (identity == null) { identity = moving.getUUID();first = moving.position(); }
        require(identity.equals(moving.getUUID()), "distant movement retains the same received body");
        var wide = new Frustum(new Matrix4f(), new Matrix4f().ortho(-256, 256, -256, 256, -256, 256));
        wide.prepare(camera.x, camera.y, camera.z);
        var renderer = mc.getEntityRenderDispatcher().getRenderer(moving);
        require(!moving.shouldRender(camera.x, camera.y, camera.z), "fixture must exceed the original native model-distance gate");
        require(renderer.shouldRender(moving, wide, camera.x, camera.y, camera.z),
                "registered native renderer shows a distant moving mob: step="+step+" role="+role
                +" camera="+camera+" player="+mc.player.position()+" mob="+moving.position()
                +" bounds="+moving.getBoundingBox()+" origin="+HouseSightlineState.origin()
                +" horizon="+HouseMobSight.rendersToHorizon(moving,HouseSightlineState.hasOrigin()?HouseSightlineState.origin():null)
                +" view="+mc.options.getEffectiveRenderDistance());
        var narrow = new Frustum(new Matrix4f(), new Matrix4f().ortho(-1, 1, -1, 1, -1, 1));
        narrow.prepare(camera.x, camera.y, camera.z);
        require(!renderer.shouldRender(moving, narrow, camera.x, camera.y, camera.z), "distance extension preserves native frustum culling");
        if (step == 37) {
            var actors = mobs.stream().filter(mob -> mob instanceof LiteraryActor && mob.getCustomName() != null
                    && List.of(MobSightProof.PRIVATE_A, MobSightProof.PRIVATE_B).contains(mob.getCustomName().getString())).toList();
            require(actors.stream().allMatch(actor -> ((LiteraryActor)actor).owner().filter(mc.player.getUUID()::equals).isPresent()),
                    "native distant tracking must not send another reader's private body");
            if (actors.size() != 1) return false;
            for (var actor : actors) {
                boolean mine = ((LiteraryActor)actor).owner().filter(mc.player.getUUID()::equals).isPresent();
                require(mc.getEntityRenderDispatcher().getRenderer(actor).shouldRender(actor, wide, camera.x, camera.y, camera.z) == mine,
                        "greater distance neither hides the reader's actor nor reveals their peer's");
            }
            var foreign = io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.ACTOR.get().create(mc.level);
            require(foreign != null, "native private-renderer range control creates");
            foreign.appearance(LiteraryActor.STRANGER, io.github.knaitoe.theoldesthouse.labyrinth.ThreeFingers.SERGEANT);
            foreign.bind(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.THREE_FINGERS, new UUID(0, 1));
            foreign.moveTo(actors.getFirst().position());
            require(!mc.getEntityRenderDispatcher().getRenderer(foreign).shouldRender(foreign, wide, camera.x, camera.y, camera.z),
                    "the native renderer independently rejects a foreign private body at the extended range");
            var beyond = EntityType.WOLF.create(mc.level);require(beyond != null, "native range-control wolf creates");
            beyond.moveTo(mc.player.getX() + Math.sqrt(HouseMobSight.distanceSquared(mc.options.getEffectiveRenderDistance())) + 32,
                    mc.player.getY(), mc.player.getZ());
            require(!mc.getEntityRenderDispatcher().getRenderer(beyond).shouldRender(beyond, wide, camera.x, camera.y, camera.z),
                    "native renderer caps visibility at the effective terrain horizon");
            var boy = io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.ACTOR.get().create(mc.level);
            require(boy != null, "native distant-boy range control creates");
            boy.appearance(LiteraryActor.SILHOUETTE, 4);boy.moveTo(camera.x + 184, camera.y, camera.z);
            require(boy.shouldRender(camera.x, camera.y, camera.z)
                    && mc.getEntityRenderDispatcher().getRenderer(boy).shouldRender(boy, wide, camera.x, camera.y, camera.z),
                    "extension retains the existing 192-block boy/camera allowance");
        }
        var direction = moving.getEyePosition().subtract(mc.player.getEyePosition());
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        mc.player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, Math.hypot(direction.x, direction.z))));
        if (ticks < 80 || first.distanceToSqr(moving.position()) < .2) return false;
        try { Files.writeString(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.folder().resolve(
                role + (step == 37 ? "-outside-mob-sight.txt" : "-window-mob-sight.txt")),
                "Actual native body " + identity + " moved from " + first + " to " + moving.position()
                        + " at distance " + Math.sqrt(mc.player.distanceToSqr(moving))
                        + "; registered renderer visibility, frustum, ownership and horizon checks passed.\n");
        } catch (Exception e) { throw new IllegalStateException(e); }
        return true;
    }
}
