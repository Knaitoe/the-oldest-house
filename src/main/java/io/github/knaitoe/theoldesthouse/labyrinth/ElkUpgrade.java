package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Replaces a saved world's single-field elk scene with the two-stage one (layout 36), which the
 * owner asked for in place of it. Like a fresh construction it is carved by the builder in bounded
 * slices; unlike one, it waits until nobody is in the scene or can see it, with its chunks and
 * entity sections loaded. Nothing that belongs to anyone is lost: pets, animals and dropped items
 * keep their identity and are set down on the new ground, and the scene's own killer keeps his and
 * waits below decks for whoever reads the scene next. Readers' saved originals, facts and Witness
 * evidence are personal records and are not touched.
 */
public final class ElkUpgrade {
    /** Layouts before 32 had no elk scene; 32 to 35 built the earlier one. */
    static final int FIRST_LAYOUT = 32, REBUILT_IN = 36;

    private ElkUpgrade() {}

    /** Whether an upgrade from this saved layout carves the elk scene again. */
    public static boolean rebuilds(int builtVersion) {
        return builtVersion >= FIRST_LAYOUT && builtVersion < REBUILT_IN;
    }

    /** The cabin at the end of the world is carved again too (layout 37): its new rooms, windows and exit shed. */
    static final int CABIN_REBUILT_IN = 37;

    /** Whether an upgrade from this saved layout carves this scene again. */
    public static boolean rebuilds(LabyrinthPlace place, int builtVersion) {
        if (place == LabyrinthPlace.ELK_CARCASSES) return rebuilds(builtVersion);
        return place == LabyrinthPlace.END_WORLD_CABIN && builtVersion >= FIRST_LAYOUT && builtVersion < CABIN_REBUILT_IN;
    }

    static AABB area(BlockPos base, LabyrinthPlace place) {
        if (place == LabyrinthPlace.ELK_CARCASSES) return area(base);
        var r = place.room();
        return new AABB(base.getX() + r.minX() - 1, base.getY() + r.minY() - 2, base.getZ() + r.minZ() - 1,
                base.getX() + r.maxX() + 2, base.getY() + r.maxY() + 2, base.getZ() + r.maxZ() + 2);
    }

    public static boolean vacant(ServerLevel level, BlockPos base, LabyrinthPlace place, boolean fixture) {
        if (place == LabyrinthPlace.ELK_CARCASSES) return vacant(level, base, fixture);
        AABB area = area(base, place);
        for (var player : level.players())
            if (area.intersects(player.getBoundingBox()) || area.inflate(32).intersects(player.getCamera().getBoundingBox())) return false;
        return fixture || ScenePolish.loaded(level, area, base);
    }

    /** After the carve: the cabin's visitors are placed by their own scene; anything else displaced goes to the porch. */
    public static void settle(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        if (place == LabyrinthPlace.ELK_CARCASSES) { settle(level, base); return; }
        Vec3 porch = Vec3.atBottomCenterOf(base.offset(-6, 0, -10));
        for (Entity e : level.getEntitiesOfClass(Entity.class, area(base, place), e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
            if (e instanceof Player || !e.isAlive() || e instanceof LiteraryActor) continue;
            if (!stranded(level, e)) continue;
            if (e.isPassenger()) e.stopRiding();
            e.teleportTo(porch.x + (e.getId() % 5) * .7 - 1.4, porch.y, porch.z - (e.getId() % 3) * .6);
            e.setDeltaMovement(Vec3.ZERO);
            e.resetFallDistance();
        }
    }

    static AABB area(BlockPos base) {
        return new AABB(base.getX() - ElkCarcassMap.SKIRT_X, base.getY() - 12, base.getZ() + ElkCarcassMap.SKIRT_NORTH,
                base.getX() + ElkCarcassMap.SKIRT_X + 1, base.getY() + 40, base.getZ() + ElkCarcassMap.SKIRT_SOUTH + 1);
    }

    /**
     * Nobody inside, no camera near, everything loaded: the old scene may be taken down. A native
     * GameTest fixture drain cannot wait on entity sections inside one tick; its fixtures place
     * their own bodies, so it checks only for people.
     */
    public static boolean vacant(ServerLevel level, BlockPos base, boolean fixture) {
        AABB area = area(base);
        for (var player : level.players())
            if (area.intersects(player.getBoundingBox()) || area.inflate(32).intersects(player.getCamera().getBoundingBox())) return false;
        return fixture || ScenePolish.loaded(level, area, base);
    }

    /**
     * After the carve: the scene's earlier shared killer goes below decks with its identity, and
     * anything else alive or dropped that the new ground buried or left over water is set down on
     * the north beach, unchanged.
     */
    public static void settle(ServerLevel level, BlockPos base) {
        Vec3 beach = Vec3.atBottomCenterOf(base.offset(ElkCarcassMap.standAt(4, -74)));
        for (Entity e : level.getEntitiesOfClass(Entity.class, area(base), e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
            if (e instanceof Player || !e.isAlive()) continue;
            if (e instanceof LiteraryActor a && a.scene().equals(LabyrinthPlace.ELK_CARCASSES.id())) {
                if (a.role() == LiteraryActor.KILLER) {
                    a.getNavigation().stop();
                    a.setNoGravity(false);
                    a.moveTo(Vec3.atBottomCenterOf(base.offset(ElkCarcassMap.KILLER_START)));
                }
                continue;
            }
            if (!stranded(level, e)) continue;
            if (e.isPassenger()) e.stopRiding();
            e.teleportTo(beach.x + (e.getId() % 5) * .7 - 1.4, beach.y, beach.z - (e.getId() % 3) * .7);
            e.setDeltaMovement(Vec3.ZERO);
            e.resetFallDistance();
        }
    }

    /** Inside the new ground, in the lake, or with nothing under it. */
    public static boolean stranded(ServerLevel level, Entity e) {
        var at = e.blockPosition();
        if (!level.noCollision(e, e.getBoundingBox())) return true;
        if (e.isInWater() || level.getBlockState(at).liquid()) return true;
        for (int dy = 1; dy <= 3; dy++) if (!level.getBlockState(at.below(dy)).getCollisionShape(level, at.below(dy)).isEmpty()) return false;
        return true;
    }
}
