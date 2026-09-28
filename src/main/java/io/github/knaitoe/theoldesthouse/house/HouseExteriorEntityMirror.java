package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Visual entity layer for the domestic House.
 *
 * Blocks outside the windows are mirrored into the House dimension already,
 * but real Overworld mobs cannot cross dimensions with them. While a player
 * occupies the domestic interior this class keeps lightweight, invulnerable,
 * no-AI projections of nearby Overworld mobs at matching coordinates in the
 * House dimension. The real entity remains authoritative in the Overworld.
 *
 * Projections are scenery only: they never drive AI, never carry game logic
 * back to the source, and are discarded as soon as nobody is in the domestic
 * interior. This lets a player look out the front door and still see Hillary
 * sitting on the real Overworld doorstep.
 */
public final class HouseExteriorEntityMirror {
    public static final String PROJECTION_TAG = "the_oldest_house.exterior_projection";

    private static final int SYNC_INTERVAL_TICKS = 2;
    private static final int STATE_REFRESH_INTERVAL_TICKS = 20;
    private static final int ENTITY_VIEW_MARGIN = 40;

    private static final TicketType<ChunkPos> SOURCE_VIEW_TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_exterior_entity_view",
            Comparator.comparingLong(ChunkPos::toLong)
    );

    /** Overworld source UUID -> House-dimension projection UUID. */
    private static final Map<UUID, UUID> PROJECTIONS = new HashMap<>();
    private static final LongSet TICKETED_CHUNKS = new LongOpenHashSet();
    private static boolean scrubbedLoadedProjections;

    private HouseExteriorEntityMirror() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SYNC_INTERVAL_TICKS != 0) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);

        if (!data.isSpawned() || origin == null || interior == null || !data.isInteriorInitialized()) {
            clear(server);
            return;
        }

        if (!hasDomesticObserver(interior, origin)) {
            clearProjections(interior);
            releaseSourceTickets(server.overworld());
            return;
        }

        ServerLevel overworld = server.overworld();
        ensureSourceTickets(overworld, origin);

        AABB view = viewBounds(origin);
        if (!scrubbedLoadedProjections) {
            // A clean shutdown removes projections before saving, but after a
            // crash a tamed/persistent projected mob may have been written to
            // a House chunk. Never let yesterday's scenery become an entity.
            for (Mob mob : interior.getEntitiesOfClass(
                    Mob.class,
                    view,
                    entity -> entity.getTags().contains(PROJECTION_TAG)
            )) {
                mob.discard();
            }
            PROJECTIONS.clear();
            scrubbedLoadedProjections = true;
        }

        removeNativeExteriorMobs(interior, origin, view);
        sync(overworld, interior, origin, view, server.getTickCount() % STATE_REFRESH_INTERVAL_TICKS == 0);
    }

    /**
     * Synchronises one frame of exterior entity scenery.
     *
     * Public primarily so GameTests can exercise the mirror without waiting
     * for the normal server-tick observer gate.
     */
    public static int syncNow(ServerLevel overworld, ServerLevel interior, BlockPos origin) {
        AABB view = viewBounds(origin);
        removeNativeExteriorMobs(interior, origin, view);
        return sync(overworld, interior, origin, view, true);
    }

    private static int sync(
            ServerLevel overworld,
            ServerLevel interior,
            BlockPos origin,
            AABB view,
            boolean refreshState
    ) {
        Set<UUID> seen = new HashSet<>();
        int visible = 0;

        for (Mob source : overworld.getEntitiesOfClass(
                Mob.class,
                view,
                mob -> eligibleSource(origin, mob)
        )) {
            UUID sourceId = source.getUUID();
            seen.add(sourceId);

            Mob projection = projectionOf(interior, sourceId);
            if (projection == null || projection.getType() != source.getType()) {
                if (projection != null) {
                    projection.discard();
                }
                projection = createProjection(interior, source);
                if (projection == null) {
                    PROJECTIONS.remove(sourceId);
                    continue;
                }
                PROJECTIONS.put(sourceId, projection.getUUID());
            } else if (refreshState) {
                loadVisualState(source, projection);
            }

            positionProjection(source, projection);
            visible++;
        }

        Iterator<Map.Entry<UUID, UUID>> iterator = PROJECTIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            if (seen.contains(entry.getKey())) {
                continue;
            }
            Entity stale = interior.getEntity(entry.getValue());
            if (stale != null) {
                stale.discard();
            }
            iterator.remove();
        }

        return visible;
    }

    private static boolean eligibleSource(BlockPos origin, Mob mob) {
        if (!mob.isAlive()
                || mob.isRemoved()
                || mob.getTags().contains(PROJECTION_TAG)) {
            return false;
        }

        double relX = mob.getX() - origin.getX();
        double relY = mob.getY() - origin.getY();
        double relZ = mob.getZ() - origin.getZ();

        // The Overworld proxy itself must stay empty. Mobs inside it are
        // handled by HouseProxyEntityEvacuation, not projected as though they
        // were valid outdoor scenery.
        return !HouseLayout.isInsideDomesticVolume(relX, relY, relZ);
    }

    @Nullable
    private static Mob projectionOf(ServerLevel interior, UUID sourceId) {
        UUID projectionId = PROJECTIONS.get(sourceId);
        if (projectionId == null) {
            return null;
        }
        Entity entity = interior.getEntity(projectionId);
        return entity instanceof Mob mob ? mob : null;
    }

    @Nullable
    private static Mob createProjection(ServerLevel interior, Mob source) {
        Entity created = source.getType().create(interior);
        if (!(created instanceof Mob projection)) {
            return null;
        }

        loadVisualState(source, projection);
        projection.setUUID(UUID.randomUUID());
        projection.addTag(PROJECTION_TAG);
        configureProjection(projection);
        positionProjection(source, projection);

        if (!interior.addFreshEntity(projection)) {
            return null;
        }
        return projection;
    }

    /**
     * Uses normal entity NBT so skins/variants, custom names, collars,
     * villager data and visible equipment match without a parallel catalogue
     * of every mob type. Identity, physics and persistence fields are stripped
     * before loading because this is a projection, not a cloned game object.
     */
    private static void loadVisualState(Mob source, Mob projection) {
        CompoundTag tag = new CompoundTag();
        source.saveWithoutId(tag);

        for (String key : new String[]{
                "UUID",
                "Pos",
                "Motion",
                "Rotation",
                "Passengers",
                "Leash",
                "PortalCooldown",
                "FallDistance",
                "Fire",
                "Air",
                "OnGround",
                "PersistenceRequired",
                "NoAI",
                "Invulnerable",
                "Silent",
                "DeathLootTable",
                "DeathLootTableSeed"
        }) {
            tag.remove(key);
        }

        projection.load(tag);
        configureProjection(projection);
    }

    private static void configureProjection(Mob projection) {
        projection.setNoAi(true);
        projection.setInvulnerable(true);
        projection.setSilent(true);
        projection.setNoGravity(true);
        projection.noPhysics = true;
        projection.setTarget(null);
    }

    private static void positionProjection(Mob source, Mob projection) {
        projection.moveTo(
                source.getX(),
                source.getY(),
                source.getZ(),
                source.getYRot(),
                source.getXRot()
        );
        projection.setYHeadRot(source.getYHeadRot());
        projection.setDeltaMovement(source.getDeltaMovement());
    }

    /**
     * Native mobs in the House dimension's mirrored outdoor view would appear
     * alongside the Overworld projections. Remove only those outdoor/native
     * mobs; authored entities inside the domestic House remain untouched.
     */
    private static void removeNativeExteriorMobs(ServerLevel interior, BlockPos origin, AABB view) {
        for (Mob mob : interior.getEntitiesOfClass(
                Mob.class,
                view,
                entity -> !entity.getTags().contains(PROJECTION_TAG)
        )) {
            double relX = mob.getX() - origin.getX();
            double relY = mob.getY() - origin.getY();
            double relZ = mob.getZ() - origin.getZ();
            if (!HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
                mob.discard();
            }
        }
    }

    private static boolean hasDomesticObserver(ServerLevel interior, BlockPos origin) {
        for (ServerPlayer player : interior.players()) {
            double relX = player.getX() - origin.getX();
            double relY = player.getY() - origin.getY();
            double relZ = player.getZ() - origin.getZ();
            if (HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
                return true;
            }
        }
        return false;
    }

    private static AABB viewBounds(BlockPos origin) {
        return new AABB(
                origin.getX() + HouseLayout.MIN_X - ENTITY_VIEW_MARGIN,
                origin.getY() - HouseDimensionMirror.VIEW_BELOW_FLOOR,
                origin.getZ() + HouseLayout.MIN_Z - ENTITY_VIEW_MARGIN,
                origin.getX() + HouseLayout.MAX_X + ENTITY_VIEW_MARGIN + 1.0D,
                origin.getY() + HouseDimensionMirror.VIEW_ABOVE_FLOOR + 1.0D,
                origin.getZ() + HouseLayout.MAX_Z + ENTITY_VIEW_MARGIN + 1.0D
        );
    }

    private static void ensureSourceTickets(ServerLevel overworld, BlockPos origin) {
        int minX = origin.getX() + HouseLayout.MIN_X - ENTITY_VIEW_MARGIN;
        int maxX = origin.getX() + HouseLayout.MAX_X + ENTITY_VIEW_MARGIN;
        int minZ = origin.getZ() + HouseLayout.MIN_Z - ENTITY_VIEW_MARGIN;
        int maxZ = origin.getZ() + HouseLayout.MAX_Z + ENTITY_VIEW_MARGIN;

        LongSet wanted = new LongOpenHashSet();
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                wanted.add(key);
                if (TICKETED_CHUNKS.add(key)) {
                    ChunkPos chunk = new ChunkPos(cx, cz);
                    overworld.getChunkSource().addRegionTicket(
                            SOURCE_VIEW_TICKET,
                            chunk,
                            0,
                            chunk
                    );
                }
            }
        }

        var iterator = TICKETED_CHUNKS.iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            if (wanted.contains(key)) {
                continue;
            }
            ChunkPos chunk = new ChunkPos(key);
            overworld.getChunkSource().removeRegionTicket(
                    SOURCE_VIEW_TICKET,
                    chunk,
                    0,
                    chunk
            );
            iterator.remove();
        }
    }

    private static void releaseSourceTickets(ServerLevel overworld) {
        var iterator = TICKETED_CHUNKS.iterator();
        while (iterator.hasNext()) {
            ChunkPos chunk = new ChunkPos(iterator.nextLong());
            overworld.getChunkSource().removeRegionTicket(
                    SOURCE_VIEW_TICKET,
                    chunk,
                    0,
                    chunk
            );
            iterator.remove();
        }
    }

    private static void clearProjections(ServerLevel interior) {
        for (UUID projectionId : PROJECTIONS.values()) {
            Entity entity = interior.getEntity(projectionId);
            if (entity != null) {
                entity.discard();
            }
        }
        PROJECTIONS.clear();
    }

    public static void clear(MinecraftServer server) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior != null) {
            clearProjections(interior);
        } else {
            PROJECTIONS.clear();
        }
        releaseSourceTickets(server.overworld());
        scrubbedLoadedProjections = false;
    }
}
