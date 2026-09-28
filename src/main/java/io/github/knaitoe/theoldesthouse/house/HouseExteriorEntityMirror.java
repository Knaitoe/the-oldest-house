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
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bidirectional visual entity continuity across the domestic House boundary.
 *
 * The Overworld and House dimension each own their real entities. The other
 * dimension receives disposable, non-interactive projections at matching
 * coordinates:
 *
 *   Overworld exterior mob -> House-dimension exterior projection
 *   House domestic mob      -> Overworld proxy-interior projection
 *
 * This means a player inside can still see Hillary on the real doorstep, and
 * a player outside can see a real House-dimension NPC through a window. No
 * entity is actually duplicated for gameplay: the projection has no AI,
 * collision, damage, interaction or authority.
 *
 * Impossible-space entities are intentionally excluded. Only real mobs inside
 * {@link HouseLayout#isInsideDomesticVolume(double, double, double)} project
 * from the House to the Overworld.
 */
public final class HouseExteriorEntityMirror {
    public static final String PROJECTION_TAG = "the_oldest_house.entity_projection";
    private static final String LEGACY_PROJECTION_TAG = "the_oldest_house.exterior_projection";

    private static final int SYNC_INTERVAL_TICKS = 2;
    private static final int STATE_REFRESH_INTERVAL_TICKS = 20;
    private static final int ENTITY_VIEW_MARGIN = 40;

    private static final TicketType<ChunkPos> OVERWORLD_SOURCE_TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_overworld_entity_view",
            Comparator.comparingLong(ChunkPos::toLong)
    );
    private static final TicketType<ChunkPos> INTERIOR_SOURCE_TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_interior_entity_view",
            Comparator.comparingLong(ChunkPos::toLong)
    );

    /** Real Overworld UUID -> projection UUID in the House dimension. */
    private static final Map<UUID, UUID> OVERWORLD_TO_INTERIOR = new HashMap<>();
    /** Real House-dimension UUID -> projection UUID in the Overworld proxy. */
    private static final Map<UUID, UUID> INTERIOR_TO_OVERWORLD = new HashMap<>();

    private static final LongSet OVERWORLD_TICKETED_CHUNKS = new LongOpenHashSet();
    private static final LongSet INTERIOR_TICKETED_CHUNKS = new LongOpenHashSet();

    private static boolean scrubbedInteriorProjections;
    private static boolean scrubbedOverworldProjections;

    private HouseExteriorEntityMirror() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isProjection(event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isProjection(event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    public static void onAttack(AttackEntityEvent event) {
        if (isProjection(event.getTarget())) {
            event.setCanceled(true);
        }
    }

    /** True for scenery entities created by either mirror direction. */
    public static boolean isProjection(Entity entity) {
        return entity.getTags().contains(PROJECTION_TAG)
                || entity.getTags().contains(LEGACY_PROJECTION_TAG);
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

        ServerLevel overworld = server.overworld();
        AABB view = viewBounds(origin);
        boolean refreshState = server.getTickCount() % STATE_REFRESH_INTERVAL_TICKS == 0;

        // Inside looking out: real Overworld mobs become projections in the
        // House dimension's mirrored exterior scenery.
        if (hasDomesticObserver(interior, origin)) {
            ensureOverworldSourceTickets(overworld, origin);
            scrubInteriorProjectionsIfNeeded(interior, view);
            removeNativeExteriorMobs(interior, origin, view);
            syncDirection(
                    overworld,
                    interior,
                    origin,
                    view,
                    OVERWORLD_TO_INTERIOR,
                    mob -> eligibleOverworldExteriorSource(origin, mob),
                    refreshState
            );
        } else {
            clearProjectionMap(interior, OVERWORLD_TO_INTERIOR);
            releaseTickets(overworld, OVERWORLD_SOURCE_TICKET, OVERWORLD_TICKETED_CHUNKS);
        }

        // Outside looking in: real domestic House mobs become projections
        // inside the Overworld proxy shell.
        if (hasOverworldObserver(overworld, origin)) {
            ensureInteriorSourceTickets(interior, origin);
            scrubOverworldProjectionsIfNeeded(overworld, view);
            syncDirection(
                    interior,
                    overworld,
                    origin,
                    view,
                    INTERIOR_TO_OVERWORLD,
                    mob -> eligibleDomesticInteriorSource(origin, mob),
                    refreshState
            );
        } else {
            clearProjectionMap(overworld, INTERIOR_TO_OVERWORLD);
            releaseTickets(interior, INTERIOR_SOURCE_TICKET, INTERIOR_TICKETED_CHUNKS);
        }
    }

    /**
     * Seeds the inside-looking-out layer immediately before an entering
     * player's teleport. Prevents one empty exterior frame on arrival.
     */
    public static int syncNow(ServerLevel overworld, ServerLevel interior, BlockPos origin) {
        AABB view = viewBounds(origin);
        scrubInteriorProjectionsIfNeeded(interior, view);
        removeNativeExteriorMobs(interior, origin, view);
        return syncDirection(
                overworld,
                interior,
                origin,
                view,
                OVERWORLD_TO_INTERIOR,
                mob -> eligibleOverworldExteriorSource(origin, mob),
                true
        );
    }

    /**
     * Seeds the outside-looking-in layer immediately before an exiting
     * player's teleport. Prevents domestic NPCs popping into view a tick late.
     */
    public static int syncDomesticToOverworldNow(
            ServerLevel interior,
            ServerLevel overworld,
            BlockPos origin
    ) {
        AABB view = viewBounds(origin);
        scrubOverworldProjectionsIfNeeded(overworld, view);
        return syncDirection(
                interior,
                overworld,
                origin,
                view,
                INTERIOR_TO_OVERWORLD,
                mob -> eligibleDomesticInteriorSource(origin, mob),
                true
        );
    }

    private static int syncDirection(
            ServerLevel sourceLevel,
            ServerLevel targetLevel,
            BlockPos origin,
            AABB view,
            Map<UUID, UUID> projections,
            Predicate<Mob> sourceFilter,
            boolean refreshState
    ) {
        Set<UUID> seen = new HashSet<>();
        int visible = 0;

        for (Mob source : sourceLevel.getEntitiesOfClass(Mob.class, view, sourceFilter)) {
            UUID sourceId = source.getUUID();
            seen.add(sourceId);

            Mob projection = projectionOf(targetLevel, projections, sourceId);
            if (projection == null || projection.getType() != source.getType()) {
                if (projection != null) {
                    projection.discard();
                }
                projection = createProjection(targetLevel, source);
                if (projection == null) {
                    projections.remove(sourceId);
                    continue;
                }
                projections.put(sourceId, projection.getUUID());
            } else if (refreshState) {
                loadVisualState(source, projection);
            }

            positionProjection(source, projection);
            visible++;
        }

        Iterator<Map.Entry<UUID, UUID>> iterator = projections.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            if (seen.contains(entry.getKey())) {
                continue;
            }
            Entity stale = targetLevel.getEntity(entry.getValue());
            if (stale != null) {
                stale.discard();
            }
            iterator.remove();
        }

        return visible;
    }

    private static boolean eligibleOverworldExteriorSource(BlockPos origin, Mob mob) {
        if (!eligibleRealMob(mob)) {
            return false;
        }

        double relX = mob.getX() - origin.getX();
        double relY = mob.getY() - origin.getY();
        double relZ = mob.getZ() - origin.getZ();

        // The proxy itself is intentionally kept empty of real mobs.
        return !HouseLayout.isInsideDomesticVolume(relX, relY, relZ);
    }

    private static boolean eligibleDomesticInteriorSource(BlockPos origin, Mob mob) {
        if (!eligibleRealMob(mob)) {
            return false;
        }

        double relX = mob.getX() - origin.getX();
        double relY = mob.getY() - origin.getY();
        double relZ = mob.getZ() - origin.getZ();

        // Never leak impossible-hall/vignette inhabitants into the mundane
        // Overworld facade. Only the ordinary domestic manor is transparent.
        return HouseLayout.isInsideDomesticVolume(relX, relY, relZ)
                && !HouseImpossibleHallway.isInteriorOnlyPosition(origin, mob.blockPosition());
    }

    private static boolean eligibleRealMob(Mob mob) {
        return mob.isAlive() && !mob.isRemoved() && !isProjection(mob);
    }

    @Nullable
    private static Mob projectionOf(
            ServerLevel targetLevel,
            Map<UUID, UUID> projections,
            UUID sourceId
    ) {
        UUID projectionId = projections.get(sourceId);
        if (projectionId == null) {
            return null;
        }
        Entity entity = targetLevel.getEntity(projectionId);
        return entity instanceof Mob mob ? mob : null;
    }

    @Nullable
    private static Mob createProjection(ServerLevel targetLevel, Mob source) {
        Entity created = source.getType().create(targetLevel);
        if (!(created instanceof Mob projection)) {
            return null;
        }

        loadVisualState(source, projection);
        projection.setUUID(UUID.randomUUID());
        projection.addTag(PROJECTION_TAG);
        configureProjection(projection);
        positionProjection(source, projection);

        if (!targetLevel.addFreshEntity(projection)) {
            TheOldestHouse.LOGGER.warn(
                    "Could not add {} projection for source {} from {} into {} at ({}, {}, {}).",
                    source.getType(),
                    source.getUUID(),
                    source.level().dimension().location(),
                    targetLevel.dimension().location(),
                    source.getX(),
                    source.getY(),
                    source.getZ()
            );
            return null;
        }
        return projection;
    }

    /**
     * Uses normal entity NBT so visible variants, names, collars, villager
     * appearance and equipment match without maintaining a catalogue for every
     * mob type. Gameplay identity and physics fields are stripped first.
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
                "DeathLootTableSeed",
                // Data attachments are gameplay identity (Hillary's tag, for
                // one): a projection must never carry them.
                AttachmentHolder.ATTACHMENTS_NBT_KEY
        }) {
            tag.remove(key);
        }

        projection.load(tag);
        // Loading source NBT replaces the projection's scoreboard tags too.
        // Re-apply the marker after every visual-state refresh or the scenery
        // entity could become eligible as a real source on the next sync.
        projection.addTag(PROJECTION_TAG);
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
     * The House dimension uses matching Overworld terrain for scenery. Native
     * mobs spawned in that copied exterior would double the projected real
     * population, so only outdoor/native mobs are removed. Authored domestic
     * mobs remain untouched and can project outward in the reverse direction,
     * and nothing past the labyrinth threshold is touched: the impossible
     * hallway lies inside this view box.
     */
    private static void removeNativeExteriorMobs(ServerLevel interior, BlockPos origin, AABB view) {
        for (Mob mob : interior.getEntitiesOfClass(
                Mob.class,
                view,
                entity -> !isProjection(entity)
        )) {
            double relX = mob.getX() - origin.getX();
            double relY = mob.getY() - origin.getY();
            double relZ = mob.getZ() - origin.getZ();
            if (!HouseLayout.isInsideDomesticVolume(relX, relY, relZ)
                    && !HouseLabyrinth.isBeyondThreshold(origin, mob.blockPosition())) {
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

    private static boolean hasOverworldObserver(ServerLevel overworld, BlockPos origin) {
        AABB view = viewBounds(origin);
        for (ServerPlayer player : overworld.players()) {
            if (view.contains(player.position())) {
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

    private static void scrubInteriorProjectionsIfNeeded(ServerLevel interior, AABB view) {
        if (scrubbedInteriorProjections) {
            return;
        }
        scrubLoadedProjections(interior, view);
        OVERWORLD_TO_INTERIOR.clear();
        scrubbedInteriorProjections = true;
    }

    private static void scrubOverworldProjectionsIfNeeded(ServerLevel overworld, AABB view) {
        if (scrubbedOverworldProjections) {
            return;
        }
        scrubLoadedProjections(overworld, view);
        INTERIOR_TO_OVERWORLD.clear();
        scrubbedOverworldProjections = true;
    }

    private static void scrubLoadedProjections(ServerLevel level, AABB view) {
        // Clean shutdown removes projections before saving. After a crash, a
        // tame/persistent projection might have been written into a chunk.
        for (Mob mob : level.getEntitiesOfClass(Mob.class, view, HouseExteriorEntityMirror::isProjection)) {
            mob.discard();
        }
    }

    private static void ensureOverworldSourceTickets(ServerLevel overworld, BlockPos origin) {
        ensureTickets(
                overworld,
                OVERWORLD_SOURCE_TICKET,
                OVERWORLD_TICKETED_CHUNKS,
                origin.getX() + HouseLayout.MIN_X - ENTITY_VIEW_MARGIN,
                origin.getX() + HouseLayout.MAX_X + ENTITY_VIEW_MARGIN,
                origin.getZ() + HouseLayout.MIN_Z - ENTITY_VIEW_MARGIN,
                origin.getZ() + HouseLayout.MAX_Z + ENTITY_VIEW_MARGIN
        );
    }

    private static void ensureInteriorSourceTickets(ServerLevel interior, BlockPos origin) {
        ensureTickets(
                interior,
                INTERIOR_SOURCE_TICKET,
                INTERIOR_TICKETED_CHUNKS,
                origin.getX() + HouseLayout.MIN_X,
                origin.getX() + HouseLayout.MAX_X,
                origin.getZ() + HouseLayout.MIN_Z,
                origin.getZ() + HouseLayout.MAX_Z
        );
    }

    private static void ensureTickets(
            ServerLevel level,
            TicketType<ChunkPos> ticketType,
            LongSet ticketed,
            int minX,
            int maxX,
            int minZ,
            int maxZ
    ) {
        LongSet wanted = new LongOpenHashSet();

        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                wanted.add(key);
                if (ticketed.add(key)) {
                    ChunkPos chunk = new ChunkPos(cx, cz);
                    level.getChunkSource().addRegionTicket(ticketType, chunk, 0, chunk);
                }
            }
        }

        var iterator = ticketed.iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            if (wanted.contains(key)) {
                continue;
            }
            ChunkPos chunk = new ChunkPos(key);
            level.getChunkSource().removeRegionTicket(ticketType, chunk, 0, chunk);
            iterator.remove();
        }
    }

    private static void releaseTickets(
            ServerLevel level,
            TicketType<ChunkPos> ticketType,
            LongSet ticketed
    ) {
        var iterator = ticketed.iterator();
        while (iterator.hasNext()) {
            ChunkPos chunk = new ChunkPos(iterator.nextLong());
            level.getChunkSource().removeRegionTicket(ticketType, chunk, 0, chunk);
            iterator.remove();
        }
    }

    private static void clearProjectionMap(ServerLevel target, Map<UUID, UUID> projections) {
        for (UUID projectionId : projections.values()) {
            Entity entity = target.getEntity(projectionId);
            if (entity != null) {
                entity.discard();
            }
        }
        projections.clear();
    }

    public static void clear(MinecraftServer server) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        ServerLevel overworld = server.overworld();

        if (interior != null) {
            clearProjectionMap(interior, OVERWORLD_TO_INTERIOR);
            releaseTickets(interior, INTERIOR_SOURCE_TICKET, INTERIOR_TICKETED_CHUNKS);
        } else {
            OVERWORLD_TO_INTERIOR.clear();
            INTERIOR_TICKETED_CHUNKS.clear();
        }

        clearProjectionMap(overworld, INTERIOR_TO_OVERWORLD);
        releaseTickets(overworld, OVERWORLD_SOURCE_TICKET, OVERWORLD_TICKETED_CHUNKS);

        scrubbedInteriorProjections = false;
        scrubbedOverworldProjections = false;
    }
}
