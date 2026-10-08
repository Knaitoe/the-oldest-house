package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

/**
 * Carves the labyrinth's places into their slots above the manor and
 * registers their doors: one place per tick, each slot first filled solid.
 * Everything is authored here in code for now; structure files built in
 * creative can replace these later.
 */
public final class LabyrinthBuilder {
    /** Bump for a layout upgrade; start() chooses structural rebuilds or in-place decoration. */
    public static final int VERSION = 36;

    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** The solid the slots are made of, and the gray: plain, dim, nobody's. */
    static final BlockState SOLID = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_WALL = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState GRAY_CEILING = Blocks.STONE.defaultBlockState();

    @Nullable
    private static Deque<LabyrinthPlace> pending;
    @Nullable
    private static BlockPos pendingOrigin;
    private static final Set<LabyrinthPlace> domesticUpgrades = new HashSet<>();
    private static final Set<LabyrinthPlace> architecturalUpgrades = new HashSet<>();
    /**
     * Standing scenes the owner asked to have authored again (0.4.50: the elk carcasses' two stages).
     * Carved last, once nobody is in the old one or can see it; until then the old scene is not dealt.
     */
    private static final Set<LabyrinthPlace> rebuildUpgrades = new HashSet<>();
    private static boolean legacyDomesticUpgrade;
    private static boolean fixtureDrain;
    /** Places queued for construction (not upgrades of standing places), built on demand by depth. */
    private static final Set<LabyrinthPlace> structuralPending = new HashSet<>();
    /** Set when a caller needs every place (commands, rebuilds); ordinary play builds only what is near. */
    private static boolean demandAll;
    /** False while the queue waits for an explorer to go deeper. */
    private static boolean active = true;
    @Nullable private static Boolean gatingOverride;
    /** Whether the current construction is depth-gated (always in play; GameTests opt in). */
    private static boolean gatingActive;
    /** Whether the running queue extends an existing world rather than carving a new one. */
    private static boolean upgrading;
    /** Saved construction progress, so a restart never rebuilds a place that already stands. */
    public static final String PROGRESS = "labyrinth_carve_0440";
    /** Places are built this many crossings ahead of the deepest explorer. */
    public static final int LEAD = 3;
    public static final List<LabyrinthPlace> CORE = List.of(LabyrinthPlace.JUNCTION, LabyrinthPlace.GRAY_CORRIDOR,
            LabyrinthPlace.STRAIGHT_HALL, LabyrinthPlace.BENT_HALL, LabyrinthPlace.CROSS_HALL, LabyrinthPlace.QUIET_ROOM,
            LabyrinthPlace.STONE_GALLERY);
    @Nullable private static BuildBlocks.Plan geometry;
    private static ServerLevel preparationLevel;
    private static final List<ChunkPos> preparationChunks = new ArrayList<>();
    private static final TicketType<ChunkPos> CARVE_TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_carve", Comparator.comparingLong(ChunkPos::toLong));

    private LabyrinthBuilder() {
    }

    /** Whether every place stands, carved for this manor by this version. */
    public static boolean isBuilt(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin != null && data.builtVersion() >= VERSION && origin.equals(data.builtOrigin());
    }

    /**
     * Starts carving if the places are not built for this manor yet. Returns
     * true once they all stand; while they are being carved (a place a
     * tick), false.
     */
    public static boolean ensureBuilt(MinecraftServer server) {
        if (isBuilt(server)) {
            return true;
        }
        start(server, false);
        demandAll = true;
        return false;
    }

    /**
     * Starts carving if needed and returns true once the ordinary halls past the
     * hallway stand. Deeper places follow in the background as explorers approach them.
     */
    public static boolean ensureReachable(MinecraftServer server) {
        if (isBuilt(server)) {
            return true;
        }
        start(server, false);
        if (!gatingActive) return false;
        LabyrinthData data = LabyrinthData.get(server);
        for (LabyrinthPlace place : CORE) {
            // In an upgraded world the standing core is reachable already; a core room this layout adds
            // (the stone gallery) is carved in the queue without holding every existing door shut.
            if (upgrading && structuralPending.contains(place)) continue;
            if (!isPlaceReady(data, place)) return false;
        }
        return true;
    }

    public static boolean isPlaceReady(MinecraftServer server, LabyrinthPlace place) {
        return isPlaceReady(LabyrinthData.get(server), place);
    }

    /** A place can be dealt and entered unless it is still queued for construction. */
    public static boolean isPlaceReady(LabyrinthData data, LabyrinthPlace place) {
        return place.slot() < 0 || pending == null || !rebuildUpgrades.contains(place) && (!gatingActive || !structuralPending.contains(place));
    }

    /** The route depth at which explorers can first be dealt a place. */
    public static int requiredDepth(LabyrinthPlace place) {
        if (CORE.contains(place)) return 0;
        return switch (place) {
            case FOLDED_MAZE -> LabyrinthPacing.STRANGE_DEPTH;
            case SPIRAL_STAIR, FRACTURED_WALKWAY, LIGHT_SINK, BLIND_STRETCH, MOVING_THRESHOLD -> 10;
            case DEEP_MAZE -> LabyrinthPacing.DEEP_DEPTH;
            case STONE_GALLERY, STONE_CROSSING, STONE_DESCENT, STONE_ARCADE, STONE_BEND, STONE_LANDING -> LabyrinthPacing.STONE_DEPTH;
            case ALCOVE_HALL, OFFSET_HALL, SERVICE_LANDING -> 0;
            case HOTEL_HALLWAY, COMPRESSION_PASSAGE, GRAVITY_DRIFT, DUPLICATE_PASSAGE, HOTEL, HOTEL_GROUNDS -> 14;
            case ABYSS_MAZE -> LabyrinthPacing.ABYSS_DEPTH;
            default -> LabyrinthPacing.STORY_DEPTH;
        };
    }

    private static boolean gated(MinecraftServer server) {
        if (gatingOverride != null) return gatingOverride;
        return !(server instanceof net.minecraft.gametest.framework.GameTestServer);
    }

    private static int deepest(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        int depth = 0;
        for (var player : server.getPlayerList().getPlayers()) depth = Math.max(depth, data.returnDepth(player.getUUID()));
        return depth;
    }

    private static boolean due(MinecraftServer server, LabyrinthPlace place) {
        if (!structuralPending.contains(place) || demandAll || !gated(server)) return true;
        return requiredDepth(place) <= deepest(server) + LEAD;
    }

    private static void recordBuilt(MinecraftServer server, LabyrinthPlace place) {
        if (!structuralPending.remove(place) || pendingOrigin == null) return;
        LabyrinthData data = LabyrinthData.get(server);
        var progress = data.state(PROGRESS);
        if (progress.getLong("Origin") != pendingOrigin.asLong() || progress.getInt("Version") > VERSION) {
            progress = new net.minecraft.nbt.CompoundTag();
            progress.putLong("Origin", pendingOrigin.asLong());
        }
        // Places built under an earlier layout still stand; their record carries into this one.
        progress.putInt("Version", VERSION);
        var built = progress.getList("Built", net.minecraft.nbt.Tag.TAG_STRING);
        built.add(net.minecraft.nbt.StringTag.valueOf(place.id()));
        progress.put("Built", built);
        data.setState(PROGRESS, progress);
    }

    /** Carves every place again, from the beginning. */
    public static boolean rebuild(MinecraftServer server) {
        return start(server, true);
    }

    private static boolean start(MinecraftServer server, boolean force) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin == null || server.getLevel(HouseDimensions.INTERIOR) == null) {
            return false;
        }
        if (pending != null && origin.equals(pendingOrigin)) {
            return true;
        }
        pending = new ArrayDeque<>();
        releasePreparation();
        preparing = null;
        geometry = null;
        domesticUpgrades.clear();
        architecturalUpgrades.clear();
        rebuildUpgrades.clear();
        structuralPending.clear();
        active = true;
        gatingActive = gated(server);
        LabyrinthData data = LabyrinthData.get(server);
        if (force) {
            data.setState(PROGRESS, new net.minecraft.nbt.CompoundTag());
            demandAll = true;
        }
        var progress = data.state(PROGRESS);
        Set<String> alreadyBuilt = new HashSet<>();
        // A layout bump never discards the record of places already carved for this origin: they stand,
        // with their contents and player edits, and are not carved again.
        if (progress.getLong("Origin") == origin.asLong() && progress.getInt("Version") <= VERSION)
            for (var tag : progress.getList("Built", net.minecraft.nbt.Tag.TAG_STRING)) alreadyBuilt.add(tag.getAsString());
        if(!force&&!LakeLandscape.upgradeWorld(server,origin)){pending=null;pendingOrigin=null;return false;}
        if(!force&&!OutdoorRelocation.upgrade(server,origin)){pending=null;pendingOrigin=null;return false;}
        legacyDomesticUpgrade=!force && data.builtVersion()<17 && origin.equals(data.builtOrigin());
        // Older structural upgrades keep their scope. 0.4.17 dresses existing halls in place.
        boolean extend = !force && data.builtVersion() >= 10 && data.builtVersion() < VERSION && origin.equals(data.builtOrigin());
        upgrading = extend;
        List<LabyrinthPlace> queue = new ArrayList<>();
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            boolean structural = !extend || (data.builtVersion() < 13 && place.slot() >= 23)
                    || (data.builtVersion() < 14 && place.slot() >= 27)
                    || (data.builtVersion() < 15 && place.slot() >= 28)
                    || (data.builtVersion() < 16 && place.slot() >= 30)
                    || (data.builtVersion() < 18 && place.slot() >= 31)
                    || (data.builtVersion() < 20 && place.slot() >= 32)
                    || (data.builtVersion() < 21 && place.slot() >= 33)
                    || (data.builtVersion() < 22 && place.slot() >= 39)
                    || (data.builtVersion() < 30 && place.slot() >= 40)
                    || (data.builtVersion() < 31 && place.slot() >= 42)
                    || (data.builtVersion() < 32 && place.slot() >= 45)
                    || (data.builtVersion() < 33 && place.slot() >= 69)
                    || (data.builtVersion() < 34 && place.slot() >= 72)
                    || (data.builtVersion() < 12 && LabyrinthMaze.isMaze(place))
                    || (data.builtVersion() == 10 && place == LabyrinthPlace.MOTHER_DEN);
            boolean domestic = place == LabyrinthPlace.JUNCTION || LabyrinthHalls.isHall(place) || LabyrinthMaze.isMaze(place);
            boolean architecture=VignetteArchitecture.applies(place);
            if (structural && alreadyBuilt.contains(place.id())) continue;
            if (!structural && extend && place == LabyrinthPlace.ELK_CARCASSES && ElkUpgrade.rebuilds(data.builtVersion())) {
                queue.add(place);
                rebuildUpgrades.add(place);
                continue;
            }
            if (place.slot() >= 0 && (structural || domestic || architecture)) {
                queue.add(place);
                if (structural) structuralPending.add(place);
                else {if(architecture)architecturalUpgrades.add(place);else domesticUpgrades.add(place);}
            }
        }
        // Upgrades of standing places first, then construction in the order explorers can reach it.
        queue.sort(Comparator.comparingInt((LabyrinthPlace p) -> rebuildUpgrades.contains(p) ? Integer.MAX_VALUE : structuralPending.contains(p) ? requiredDepth(p) : -1)
                .thenComparingInt(p -> p == LabyrinthPlace.MOTHER_DEN ? 0 : 1)
                .thenComparingInt(LabyrinthPlace::slot));
        pending.addAll(queue);
        pendingOrigin = origin;
        return true;
    }

    /** True while construction is actually working, not while it waits for explorers to go deeper. */
    public static boolean isCarving() {
        return pending != null && active;
    }

    /** One place per tick. */
    public static void tick(MinecraftServer server) {
        DomesticHallUpgrade.tick(server);
        WallNotesRepairs.tick(server);
        ScenePolish.tick(server);
        SceneCraft.tick(server);
        if (pending == null || pendingOrigin == null) {
            return;
        }
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            pending = null;
            return;
        }
        LabyrinthPlace place = pending.peek();
        if (place != null && geometry == null && !due(server, place)) {
            active = false;
            return;
        }
        active = true;
        if (place != null && geometry != null) {
            if (!geometry.tick()) return;
            registerDoors(dataFor(server), place, LabyrinthPlaces.base(pendingOrigin, place));
            ServerLevel site = server.getLevel(NovelRooms.dimension(place));
            // A scene carved again in place settles what its new ground displaced.
            if (rebuildUpgrades.remove(place) && site != null) ElkUpgrade.settle(site, LabyrinthPlaces.base(pendingOrigin, place));
            if (site != null) ScenePolish.polishOnce(site, pendingOrigin, place);
            recordBuilt(server, place);
            geometry = null;
            pending.poll();
            releasePreparation();
            finishIfEmpty(server, interior);
            return;
        }
        if (place != null && !prepared(server, place)) {
            return;
        }
        if(place!=null&&HallVariations.added(place)&&!architecturalUpgrades.contains(place)&&!domesticUpgrades.contains(place)) {
            BlockPos base=LabyrinthPlaces.base(pendingOrigin,place);ScenePolish.forget(server,pendingOrigin,place);
            geometry=BuildBlocks.record(interior,()->{if(HallVariations.domestic(place))LabyrinthHalls.build(interior,base,place);else StoneHalls.build(interior,base,place);});
            return;
        }
        if (place != null && LiteraryRooms.isLiterary(place)
                && !architecturalUpgrades.contains(place) && !domesticUpgrades.contains(place)) {
            ServerLevel site = server.getLevel(NovelRooms.dimension(place));
            if (site != null) {
                BlockPos base = LabyrinthPlaces.base(pendingOrigin, place);
                // A saved world's old elk scene is only taken down once nobody is in it or can see it.
                if (rebuildUpgrades.contains(place) && !ElkUpgrade.vacant(site, base, fixtureDrain)) { active = false; return; }
                ScenePolish.forget(server, pendingOrigin, place);
                geometry = BuildBlocks.record(site, () -> LiteraryRooms.build(site, base, place));
                if (place == LabyrinthPlace.ELK_CARCASSES) TheOldestHouse.LOGGER.info("Recorded {}: {}", place.id(), geometry.describe());
                return;
            }
        }
        pending.poll();
        long started = System.nanoTime();
        if (place != null) {
            if (architecturalUpgrades.remove(place)) {
                ServerLevel site=server.getLevel(NovelRooms.dimension(place));
                if(site!=null){if(LakeLandscape.isLake(place))LakeSettlement.decorateOnce(site,LabyrinthPlaces.base(pendingOrigin,place),place);VignetteArchitecture.decorateOnce(site,pendingOrigin,place);}
            } else if (domesticUpgrades.remove(place)) {
                if(legacyDomesticUpgrade) LabyrinthDomestic.upgrade(interior, pendingOrigin, place);
                io.github.knaitoe.theoldesthouse.house.HouseFurnishings.upgrade(interior,pendingOrigin,place);
                io.github.knaitoe.theoldesthouse.house.HouseFurnishings.reduceNotes(interior,pendingOrigin,place);
            }
            else {
                build(server, interior, pendingOrigin, place);
                recordBuilt(server, place);
            }
            long millis = (System.nanoTime() - started) / 1_000_000L;
            if (millis > 100) TheOldestHouse.LOGGER.info("Carving {} took {} ms in one tick.", place.id(), millis);
        }
        releasePreparation();
        finishIfEmpty(server, interior);
    }

    private static void finishIfEmpty(MinecraftServer server, ServerLevel interior) {
        if (pending.isEmpty()) {
            BlockPos origin = pendingOrigin;
            LabyrinthData data = LabyrinthData.get(server);
            data.pruneDoors(server);
            data.setBuilt(VERSION, origin);
            data.setState(PROGRESS, new net.minecraft.nbt.CompoundTag());
            demandAll = false;
            active = true;
            structuralPending.clear();
            MotherOfStrays.upgradeDen(interior,origin);
            ServerLevel outside=server.getLevel(HouseDimensions.OUTSIDE);
            if(outside!=null)BarnFarm.upgrade(outside,origin);
            // Those finishing upgrades dress two scenes once more: finish them again.
            ScenePolish.forget(server,origin,LabyrinthPlace.MOTHER_DEN);ScenePolish.polishOnce(interior,origin,LabyrinthPlace.MOTHER_DEN);
            if(outside!=null){ScenePolish.forget(server,origin,LabyrinthPlace.BARN_WELL);ScenePolish.polishOnce(outside,origin,LabyrinthPlace.BARN_WELL);}
            FinaleArchitecture.retirePreparationShield(interior,origin);
            io.github.knaitoe.theoldesthouse.house.HouseFurnishings.upgradeManor(interior,origin);
            // WallNotesRepairs dresses the revealed approach after the hallway is vacant.
            // Finishing an append must never rewrite the corridor around its waiting player.
            pending = null;
            pendingOrigin = null;
            domesticUpgrades.clear();
            architecturalUpgrades.clear();
            LabyrinthDoors.syncSealedDoors(server);
            TheOldestHouse.LOGGER.info("Carved the labyrinth around the manor, {} slot(s) above it (version {}).",
                    LabyrinthPlaces.slotsAbove(origin), VERSION);
        }
    }

    // ------------------------------------------------------------------
    // Pacing. A fresh world has never generated the chunks a place stands in,
    // and the largest slots hold a few hundred thousand blocks. Loading those
    // chunks synchronously and filling a whole slot in one tick stalls the
    // server for tens of seconds, so each place first waits for its chunks to
    // load off the main thread, then fills its slot a slice per tick.

    /** Main-thread time one tick may spend filling a slot. */
    private static final long FILL_BUDGET_NANOS = 8_000_000L;
    @Nullable private static LabyrinthPlace preparing;
    private static int fillColumn;

    private static boolean prepared(MinecraftServer server, LabyrinthPlace place) {
        if (preparing != place) {
            preparing = place;
            fillColumn = 0;
        }
        boolean synchronous = fixtureDrain;
        ServerLevel level = server.getLevel(NovelRooms.dimension(place));
        BlockPos base = LabyrinthPlaces.base(pendingOrigin, place);
        if (level == null || base == null) {
            return true;
        }
        if (!chunksReady(level, place, base)) {
            if (!synchronous) return false;
            for (ChunkPos chunk : preparationChunks) level.getChunk(chunk.x, chunk.z);
        }
        boolean upgrade = architecturalUpgrades.contains(place) || domesticUpgrades.contains(place);
        if (!upgrade && prefills(place)) {
            BoundingBox slot = LabyrinthPlaces.slotBounds(pendingOrigin, place);
            if (slot != null && !fillSlice(level, slot, synchronous)) {
                return false;
            }
        }
        return true;
    }

    /** Interior places whose build begins by filling their whole slot (mazes capture their old paths first). */
    private static boolean prefills(LabyrinthPlace place) {
        return !NovelRooms.outside(place) && !NovelVignettes.isNovel(place) && !LabyrinthMaze.isMaze(place);
    }

    private static boolean chunksReady(ServerLevel level, LabyrinthPlace place, BlockPos base) {
        int minX, maxX, minZ, maxZ;
        if (NovelRooms.outside(place)) {
            AABB box = OutdoorRelocation.bounds(base, place);
            minX = (int) Math.floor(box.minX); maxX = (int) Math.ceil(box.maxX);
            minZ = (int) Math.floor(box.minZ); maxZ = (int) Math.ceil(box.maxZ);
        } else {
            BoundingBox slot = LabyrinthPlaces.slotBounds(pendingOrigin, place);
            if (slot == null) return true;
            minX = slot.minX(); maxX = slot.maxX(); minZ = slot.minZ(); maxZ = slot.maxZ();
        }
        if (preparationLevel == null) preparationLevel = level;
        boolean ready = true;
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                ChunkPos chunk = new ChunkPos(x, z);
                if (!preparationChunks.contains(chunk)) {
                    level.getChunkSource().addRegionTicket(CARVE_TICKET, chunk, 1, chunk);
                    preparationChunks.add(chunk);
                }
                ready &= level.getChunkSource().getChunkNow(x, z) != null;
            }
        }
        return ready;
    }

    /** Fills the next columns of a slot; true once the whole slot is solid. */
    private static boolean fillSlice(ServerLevel level, BoundingBox slot, boolean synchronous) {
        int width = slot.maxX() - slot.minX() + 1;
        int columns = width * (slot.maxZ() - slot.minZ() + 1);
        int minY = Math.max(slot.minY(), level.getMinBuildHeight());
        int maxY = Math.min(slot.maxY(), level.getMaxBuildHeight() - 1);
        long deadline = System.nanoTime() + FILL_BUDGET_NANOS;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (fillColumn < columns) {
            int x = slot.minX() + fillColumn % width;
            int z = slot.minZ() + fillColumn / width;
            for (int y = minY; y <= maxY; y++) {
                pos.set(x, y, z);
                if (level.getBlockState(pos) != SOLID) {
                    level.setBlock(pos, SOLID, FLAGS);
                }
            }
            fillColumn++;
            if (!synchronous && (fillColumn & 3) == 0 && System.nanoTime() > deadline) {
                return fillColumn >= columns;
            }
        }
        return true;
    }

    private static void releasePreparation() {
        if (preparationLevel != null) {
            for (ChunkPos chunk : preparationChunks)
                preparationLevel.getChunkSource().removeRegionTicket(CARVE_TICKET, chunk, 1, chunk);
        }
        preparationChunks.clear();
        preparationLevel = null;
    }

    /** Fixture-only drain; production never falls back to synchronous generation. */
    public static void finishGameTest(MinecraftServer server) {
        if (!(server instanceof net.minecraft.gametest.framework.GameTestServer))
            throw new IllegalStateException("Only native GameTest fixtures may drain construction");
        fixtureDrain = true;
        try { while (isCarving()) tick(server); } finally { fixtureDrain = false; }
    }

    /** Fixture-only: build whatever is due now, synchronously, and stop where construction would wait for depth. */
    public static void drainDueGameTest(MinecraftServer server) {
        if (!(server instanceof net.minecraft.gametest.framework.GameTestServer))
            throw new IllegalStateException("Only native GameTest fixtures may drain construction");
        fixtureDrain = true;
        try { while (pending != null) { tick(server); if (!active) break; } } finally { fixtureDrain = false; }
    }

    /** Fixture-only: exercise depth gating on the GameTest server, or restore its default (null). */
    public static void gateForGameTest(@Nullable Boolean gating) {
        gatingOverride = gating;
    }

    public static void clearAll() {
        gatingActive = false;
        structuralPending.clear();
        demandAll = false;
        active = true;
        releasePreparation();
        geometry = null;
        preparing = null;
        pending = null;
        pendingOrigin = null;
        rebuildUpgrades.clear();
        domesticUpgrades.clear();
        architecturalUpgrades.clear();
    }

    private static void build(MinecraftServer server, ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        ScenePolish.forget(server, origin, place);
        buildScene(server, level, origin, place);
        ServerLevel site = server.getLevel(NovelRooms.dimension(place));
        if (site != null) ScenePolish.polishOnce(site, origin, place);
    }

    private static void buildScene(MinecraftServer server, ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        BlockPos base = LabyrinthPlaces.base(origin, place);
        BoundingBox slot = LabyrinthPlaces.slotBounds(origin, place);
        if (base == null || slot == null) {
            return;
        }
        if(LiteraryRooms.isLiterary(place)){var site=server.getLevel(NovelRooms.dimension(place));if(site!=null){if(!LiteraryRooms.outside(place))fillSolid(site,slot);LiteraryRooms.build(site,base,place);registerDoors(dataFor(server),place,base);}return;}
        if(place==LabyrinthPlace.HOTEL_GROUNDS){var site=server.getLevel(HouseDimensions.OUTSIDE);if(site!=null){HotelRooms.build(site,base,place);registerDoors(dataFor(server),place,base);VignetteArchitecture.forget(site,origin,place);VignetteArchitecture.decorateOnce(site,origin,place);}return;}
        if (NovelVignettes.isNovel(place)) {
            ServerLevel site=server.getLevel(NovelRooms.dimension(place));
            if(site!=null){NovelRooms.build(server,site,base,place);registerDoors(dataFor(server),place,base);VignetteArchitecture.forget(site,origin,place);VignetteArchitecture.decorateOnce(site,origin,place);}
            return;
        }
        if(LakeLandscape.isLake(place)){
            ServerLevel site=server.getLevel(HouseDimensions.OUTSIDE);if(site==null)return;
            LakeSettlement.forget(site,base,place);
            switch(place){case DROWNED_TOWN->DrownedTown.build(server,site,base);case SHALLOWS->Shallows.build(server,site,base);case PHONE_CANOE->PhoneCanoe.build(server,site,base);default->{}}
            if(place!=LabyrinthPlace.DROWNED_TOWN)LakeLandscape.dress(site,base,place);NovelRooms.safeApproach(site,base);registerDoors(dataFor(server),place,base);VignetteArchitecture.forget(site,origin,place);VignetteArchitecture.decorateOnce(site,origin,place);return;
        }
        LabyrinthMaze.Migration migration = LabyrinthMaze.isMaze(place) ? LabyrinthMaze.capture(level, slot, base) : null;
        fillSolid(level, slot);
        LabyrinthData data = LabyrinthData.get(server);
        switch (place) {
            case JUNCTION -> {
                buildJunction(level, base);
                LabyrinthLighting.buildEarlyAid(server, level, base);
            }
            case GRAY_CORRIDOR -> buildCorridor(level, base);
            case STRAIGHT_HALL, BENT_HALL, CROSS_HALL, QUIET_ROOM, ALCOVE_HALL, OFFSET_HALL, SERVICE_LANDING -> LabyrinthHalls.build(level, base, place);
            case FOLDED_MAZE, DEEP_MAZE, ABYSS_MAZE -> LabyrinthMaze.build(level, base, place, LabyrinthMaze.layout(server, place));
            case FLOORBOARDS -> TellTaleFloorboards.build(level, base, !data.isCompleted(place.id()));
            case RED_ROOM -> RedRoom.build(server, level, base);
            case HIDE_AND_CLAP -> HideAndClap.build(server, level, base);
            case LONG_HALLWAY, HOTEL_HALLWAY -> LabyrinthLoops.buildHallway(level, base, place);
            case SPIRAL_STAIR -> LabyrinthLoops.buildSpiral(level, base);
            case MODEL_HOME -> ModelHome.build(server, level, base);
            case HARRIGAN -> HarriganVignette.build(server, level, base);
            case DROWNED_TOWN -> DrownedTown.build(server, level, base);
            case PRESERVED_CAVE -> PreservedCave.build(server, level, base);
            case SHALLOWS -> Shallows.build(server, level, base);
            case PHONE_CANOE -> PhoneCanoe.build(server, level, base);
            case GOATMAN -> GoatmanVignette.build(server,level,base);
            case TED_CAVER -> CaverCave.build(server,level,base);
            case HOLLOWAY_CAMP -> HollowayCamp.build(level,base);
            case SEANCE, WALLPAPER_NURSERY -> ClassicsRooms.build(level,base,place);
            case BLIND_STRETCH -> BlindStretch.build(level,base);
            case HOTEL -> HotelRooms.build(level,base,place);
            case FLOODED_PASSAGE -> LabyrinthHazards.buildFloodedPassage(level, base);
            case FRACTURED_WALKWAY -> LabyrinthHazards.buildFracturedWalkway(level, base);
            case COMPRESSION_PASSAGE -> LabyrinthHazards.buildCompressionPassage(level, base);
            case FALSE_DISTANCE -> LabyrinthHazards.buildFalseDistance(level, base);
            case LIGHT_SINK -> LabyrinthHazards.buildLightSink(level, base);
            case MOVING_THRESHOLD -> LabyrinthHazards.buildMovingThreshold(level, base);
            case DUPLICATE_PASSAGE -> LabyrinthHazards.buildDuplicatePassage(level, base);
            case GRAVITY_DRIFT -> LabyrinthHazards.buildGravityDrift(level, base);
            case EXPLORER_CAMP -> LabyrinthCampsite.build(server, level, base);
            case MOTHER_DEN -> MotherOfStrays.build(server, level, base);
            case STONE_GALLERY, STONE_CROSSING, STONE_DESCENT, STONE_ARCADE, STONE_BEND, STONE_LANDING -> StoneHalls.build(level, base, place);
            default -> {
            }
        }
        registerDoors(data, place, base);
        io.github.knaitoe.theoldesthouse.house.HouseFurnishings.decorate(level,base,place);
        VignetteArchitecture.forget(level,origin,place);
        VignetteArchitecture.decorateOnce(level,origin,place);
        if (migration != null) LabyrinthMaze.restoreMigration(level, base, place, migration);
    }

    private static LabyrinthData dataFor(MinecraftServer server){return LabyrinthData.get(server);}

    private static void fillSolid(ServerLevel level, BoundingBox slot) {
        // The paced prefill already completed this slot. Do not scan it again.
        if (preparing != null && prefills(preparing) && preparationLevel == level) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(slot.minY(), level.getMinBuildHeight());
        int maxY = Math.min(slot.maxY(), level.getMaxBuildHeight() - 1);
        for (int x = slot.minX(); x <= slot.maxX(); x++) {
            for (int z = slot.minZ(); z <= slot.maxZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    if (level.getBlockState(pos) != SOLID) {
                        level.setBlock(pos, SOLID, FLAGS);
                    }
                }
            }
        }
    }

    public static void registerDoors(LabyrinthData data, LabyrinthPlace place, BlockPos base) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            data.putDoor(new LabyrinthData.Door(
                    place.doorId(spec), NovelRooms.dimension(place), base.offset(spec.rel()), spec.facing(), spec.destination(), false));
        }
    }

    // ------------------------------------------------------------------
    // The gray

    /** A household landing, 9 by 12, with a door in each wall. */
    public static void buildJunction(ServerLevel level, BlockPos base) {
        room(level, base, -4, 4, 4, -12, -1, LabyrinthDomestic.WALL, LabyrinthDomestic.FLOOR, LabyrinthDomestic.CEILING);
        hangLantern(level, base.offset(0, 4, -3), false);
        hangLantern(level, base.offset(0, 4, -9), false);
        // A single bench against the west wall, facing nothing.
        BuildBlocks.set(level,base.offset(-4, 0, -9), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        BuildBlocks.set(level,base.offset(-4, 0, -10), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        entrance(level, base, LabyrinthDomestic.WALL, LabyrinthDomestic.FLOOR, LabyrinthDomestic.CEILING);
        LabyrinthDomestic.decorateJunction(level, base);
        doors(level, base, LabyrinthPlace.JUNCTION);
    }

    /** The ordinary gray corridor is now a real branching maze. */
    public static void buildCorridor(ServerLevel level, BlockPos base) {
        LabyrinthMaze.build(level, base, LabyrinthPlace.GRAY_CORRIDOR, LabyrinthMaze.layout(level.getServer(), LabyrinthPlace.GRAY_CORRIDOR));
    }

    // ------------------------------------------------------------------
    // Helpers, shared with the vignettes

    /**
     * Carves a room: interior x0..x1, 0..height, z0..z1 (relative to base),
     * wrapped in one block of wall, floor and ceiling. The slot around it is
     * solid already; this also works in an empty test world.
     */
    static void room(ServerLevel level, BlockPos base, int x0, int x1, int height, int z0, int z1,
                     BlockState wall, BlockState floor, BlockState ceiling) {
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                for (int y = -1; y <= height + 1; y++) {
                    boolean inside = x >= x0 && x <= x1 && z >= z0 && z <= z1;
                    BlockState state;
                    if (!inside) {
                        state = wall;
                    } else if (y == -1) {
                        state = floor;
                    } else if (y == height + 1) {
                        state = ceiling;
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    BuildBlocks.set(level,base.offset(x, y, z), state, FLAGS);
                }
            }
        }
        AABB bounds = new AABB(base.offset(x0 - 1, -1, z0 - 1).getCenter(), base.offset(x1 + 1, height + 1, z1 + 1).getCenter()).inflate(1.0D);
        level.getEntitiesOfClass(ItemEntity.class, bounds).forEach(ItemEntity::discard);
    }

    /**
     * The way in, for a room whose entry door stands at (0, 0, +1): an
     * opening through the room's own south wall at z 0, a door wall row at
     * z +1 (part of the vestibule, and overwritten with wherever the player
     * came from on each arrival) and, behind it, a plain gray stub of
     * vestibule so the door has something to open onto before anyone has
     * arrived.
     */
    static void entrance(ServerLevel level, BlockPos base, BlockState wall, BlockState floor, BlockState ceiling) {
        entrance(level, base, wall, floor, ceiling, true);
    }

    /** As above; without {@code carveOpening} the room's own doorway at z 0 is left as it is. */
    static void entrance(ServerLevel level, BlockPos base, BlockState wall, BlockState floor, BlockState ceiling, boolean carveOpening) {
        if (carveOpening) {
            BuildBlocks.set(level,base.offset(0, 0, 0), Blocks.AIR.defaultBlockState(), FLAGS);
            BuildBlocks.set(level,base.offset(0, 1, 0), Blocks.AIR.defaultBlockState(), FLAGS);
        }
        BoundingBox vestibule = LabyrinthPlaces.localVestibule();
        for (int x = vestibule.minX(); x <= vestibule.maxX(); x++) {
            for (int y = vestibule.minY(); y <= vestibule.maxY(); y++) {
                for (int z = vestibule.minZ(); z <= vestibule.maxZ(); z++) {
                    boolean corridor = z >= 2 && x >= -1 && x <= 1 && y >= 0 && y <= 3;
                    boolean lining = z >= 2 && x >= -2 && x <= 2 && y >= -1 && y <= 4;
                    BlockState state = corridor ? Blocks.AIR.defaultBlockState()
                            : !lining ? SOLID
                            : y == -1 ? floor
                            : y == 4 ? ceiling
                            : wall;
                    if (z == 1) {
                        state = wall;
                    }
                    if(level.dimension().equals(HouseDimensions.OUTSIDE))state=ScenePlaytestRepairs.arrivalSkin(new BlockPos(x,y,z),state);
                    BuildBlocks.set(level,base.offset(x, y, z), state, FLAGS);
                }
            }
        }
        for (int z = 6; z <= vestibule.maxZ(); z += 8) {
            hangLantern(level, base.offset(0, 3, z), !floor.equals(LabyrinthDomestic.FLOOR));
        }
    }

    static void doors(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            placeDoor(level, base.offset(spec.rel()), spec.facing());
        }
    }

    /** An ordinary spruce door, shut, set into a wall, facing the side it is approached from. */
    public static void placeDoor(ServerLevel level, BlockPos lower, Direction facing) {
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        BuildBlocks.set(level,lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), FLAGS);
        BuildBlocks.set(level,lower.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), FLAGS);
    }

    static void hangLantern(ServerLevel level, BlockPos pos, boolean soul) {
        Block lantern = soul ? Blocks.SOUL_LANTERN : Blocks.LANTERN;
        BuildBlocks.set(level,pos, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true), FLAGS);
    }

    static BlockState stairs(Block block, Direction back) {
        return block.defaultBlockState().setValue(StairBlock.FACING, back);
    }

    static void bed(ServerLevel level, BlockPos foot, Direction toHead, Block bed) {
        BlockState state = bed.defaultBlockState().setValue(BedBlock.FACING, toHead);
        BuildBlocks.set(level,foot, state.setValue(BedBlock.PART, BedPart.FOOT), FLAGS);
        BuildBlocks.set(level,foot.relative(toHead), state.setValue(BedBlock.PART, BedPart.HEAD), FLAGS);
    }

    static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    static BlockState candle(int count, boolean lit) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, lit);
    }

    static int flags() {
        return FLAGS;
    }
}
