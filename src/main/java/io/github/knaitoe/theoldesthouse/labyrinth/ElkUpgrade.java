package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
    private static final String FROZEN="ElkRebuildResident0460";

    private ElkUpgrade() {}

    /** Whether an upgrade from this saved layout carves the elk scene again. */
    public static boolean rebuilds(int builtVersion) {
        return builtVersion >= FIRST_LAYOUT && builtVersion < REBUILT_IN;
    }

    /** The cabin at the end of the world is carved again too (layout 37): its new rooms, windows and exit shed. */
    static final int CABIN_REBUILT_IN = 37;

    /** The Goatman's trailer and camp are carved again too (layout 38): real seats, lamps, the bathroom window, the shed, the hollows. Layout 18 first built it. */
    static final int GOATMAN_FIRST = 18, GOATMAN_REBUILT_IN = 38;

    /** The Whalestoe institute is carved again (layout 39): post room, corridor, her room, the dayroom and three attics. Layout 21 first built it. */
    static final int WHALE_FIRST = 21, WHALE_REBUILT_IN = 39;

    /** Drowned Town is carved again as Proofrock (layout 40): the town, the high school and the old town under the lake. Layout 13 first built it. */
    static final int DROWNED_FIRST = 13, DROWNED_REBUILT_IN = 40;

    /** Whether an upgrade from this saved layout carves this scene again. */
    public static boolean rebuilds(LabyrinthPlace place, int builtVersion) {
        if (place == LabyrinthPlace.ELK_CARCASSES) return rebuilds(builtVersion);
        if (place == LabyrinthPlace.GOATMAN) return builtVersion >= GOATMAN_FIRST && builtVersion < GOATMAN_REBUILT_IN;
        if (place == LabyrinthPlace.WHALE) return builtVersion >= WHALE_FIRST && builtVersion < WHALE_REBUILT_IN;
        if (place == LabyrinthPlace.DROWNED_TOWN) return builtVersion >= DROWNED_FIRST && builtVersion < DROWNED_REBUILT_IN;
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

    /** After the carve: the cabin's visitors are placed by their own scene; anything else displaced goes to the porch (the trailer's yard for the Goatman, the institute's reception). */
    public static void settle(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        if (place == LabyrinthPlace.ELK_CARCASSES) { settle(level, base); return; }
        Vec3 porch = Vec3.atBottomCenterOf(place == LabyrinthPlace.GOATMAN ? base.offset(-1, 0, -47) : place == LabyrinthPlace.WHALE ? base.offset(-4, 0, -4) : place == LabyrinthPlace.DROWNED_TOWN ? base.offset(0, 0, -8) : base.offset(-6, 0, -10));
        for (Entity e : level.getEntitiesOfClass(Entity.class, area(base, place), e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
            if (e instanceof Player || !e.isAlive()) continue;
            restoreResident(e);
            if(e instanceof LiteraryActor || e instanceof GoatmanChild || e instanceof GoatmanFigure || e instanceof LakeWitchEntity || e instanceof LakeCongregantEntity)continue;
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

    /** Keep actual residents safe through a sliced carve, including a save/reload midway. */
    public static void protectResidents(ServerLevel level,BlockPos base){
        protectResidents(level,base,LabyrinthPlace.ELK_CARCASSES);
    }
    public static void protectResidents(ServerLevel level,BlockPos base,LabyrinthPlace place){
        for(Entity e:level.getEntitiesOfClass(Entity.class,area(base,place),e->e instanceof LivingEntity||e instanceof ItemEntity)){
            if(e instanceof Player||!e.isAlive()||e.getPersistentData().contains(FROZEN))continue;
            var original=new CompoundTag();original.putBoolean("NoGravity",e.isNoGravity());original.putBoolean("Invulnerable",e.isInvulnerable());
            if(e instanceof Mob mob){original.putBoolean("NoAI",mob.isNoAi());mob.getNavigation().stop();mob.setNoAi(true);}
            if(e instanceof ItemEntity item){var saved=new CompoundTag();item.saveWithoutId(saved);original.putShort("Age",saved.getShort("Age"));item.setUnlimitedLifetime();}
            e.getPersistentData().put(FROZEN,original);e.setNoGravity(true);e.setInvulnerable(true);e.setDeltaMovement(Vec3.ZERO);
        }
    }
    private static void restoreResident(Entity e){
        if(!e.getPersistentData().contains(FROZEN))return;var original=e.getPersistentData().getCompound(FROZEN);e.getPersistentData().remove(FROZEN);
        e.setNoGravity(original.getBoolean("NoGravity"));e.setInvulnerable(original.getBoolean("Invulnerable"));
        if(e instanceof Mob mob)mob.setNoAi(original.getBoolean("NoAI"));
        if(e instanceof ItemEntity item){var saved=new CompoundTag();item.saveWithoutId(saved);saved.putShort("Age",original.getShort("Age"));item.load(saved);}
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
            restoreResident(e);
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
