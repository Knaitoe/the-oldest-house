package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
    public static final int VERSION = 28;

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
    private static boolean legacyDomesticUpgrade;

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
        return false;
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
        domesticUpgrades.clear();
        architecturalUpgrades.clear();
        LabyrinthData data = LabyrinthData.get(server);
        if(!force&&!LakeLandscape.upgradeWorld(server,origin)){pending=null;pendingOrigin=null;return false;}
        if(!force&&!OutdoorRelocation.upgrade(server,origin)){pending=null;pendingOrigin=null;return false;}
        legacyDomesticUpgrade=!force && data.builtVersion()<17 && origin.equals(data.builtOrigin());
        // Older structural upgrades keep their scope. 0.4.17 dresses existing halls in place.
        boolean extend = !force && data.builtVersion() >= 10 && data.builtVersion() < VERSION && origin.equals(data.builtOrigin());
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            boolean structural = !extend || (data.builtVersion() < 13 && place.slot() >= 23)
                    || (data.builtVersion() < 14 && place.slot() >= 27)
                    || (data.builtVersion() < 15 && place.slot() >= 28)
                    || (data.builtVersion() < 16 && place.slot() >= 30)
                    || (data.builtVersion() < 18 && place.slot() >= 31)
                    || (data.builtVersion() < 20 && place.slot() >= 32)
                    || (data.builtVersion() < 21 && place.slot() >= 33)
                    || (data.builtVersion() < 22 && place.slot() >= 39)
                    || (data.builtVersion() < 12 && LabyrinthMaze.isMaze(place))
                    || (data.builtVersion() == 10 && place == LabyrinthPlace.MOTHER_DEN);
            boolean domestic = place == LabyrinthPlace.JUNCTION || LabyrinthHalls.isHall(place) || LabyrinthMaze.isMaze(place);
            boolean architecture=VignetteArchitecture.applies(place);
            if (place.slot() >= 0 && (structural || domestic || architecture)) {
                pending.add(place);
                if (!structural) {if(architecture)architecturalUpgrades.add(place);else domesticUpgrades.add(place);}
            }
        }
        pendingOrigin = origin;
        return true;
    }

    public static boolean isCarving() {
        return pending != null;
    }

    /** One place per tick. */
    public static void tick(MinecraftServer server) {
        DomesticHallUpgrade.tick(server);
        if (pending == null || pendingOrigin == null) {
            return;
        }
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            pending = null;
            return;
        }
        LabyrinthPlace place = pending.poll();
        if (place != null) {
            if (architecturalUpgrades.remove(place)) {
                ServerLevel site=server.getLevel(NovelRooms.dimension(place));
                if(site!=null){if(LakeLandscape.isLake(place))LakeSettlement.decorateOnce(site,LabyrinthPlaces.base(pendingOrigin,place),place);VignetteArchitecture.decorateOnce(site,pendingOrigin,place);}
            } else if (domesticUpgrades.remove(place)) {
                if(legacyDomesticUpgrade) LabyrinthDomestic.upgrade(interior, pendingOrigin, place);
                io.github.knaitoe.theoldesthouse.house.HouseFurnishings.upgrade(interior,pendingOrigin,place);
                io.github.knaitoe.theoldesthouse.house.HouseFurnishings.reduceNotes(interior,pendingOrigin,place);
            }
            else build(server, interior, pendingOrigin, place);
        }
        if (pending.isEmpty()) {
            BlockPos origin = pendingOrigin;
            LabyrinthData data = LabyrinthData.get(server);
            data.pruneDoors(server);
            data.setBuilt(VERSION, origin);
            MotherOfStrays.upgradeDen(interior,origin);
            ServerLevel outside=server.getLevel(HouseDimensions.OUTSIDE);
            if(outside!=null)BarnFarm.upgrade(outside,origin);
            FinaleArchitecture.retirePreparationShield(interior,origin);
            io.github.knaitoe.theoldesthouse.house.HouseFurnishings.upgradeManor(interior,origin);
            if (HouseSavedData.get(server).isImpossibleDoorRevealed())
                io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway.dressDomesticApproach(interior, origin);
            pending = null;
            pendingOrigin = null;
            domesticUpgrades.clear();
            architecturalUpgrades.clear();
            LabyrinthDoors.syncSealedDoors(server);
            TheOldestHouse.LOGGER.info("Carved the labyrinth around the manor, {} slot(s) above it (version {}).",
                    LabyrinthPlaces.slotsAbove(origin), VERSION);
        }
    }

    public static void clearAll() {
        pending = null;
        pendingOrigin = null;
        domesticUpgrades.clear();
        architecturalUpgrades.clear();
    }

    private static void build(MinecraftServer server, ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        BlockPos base = LabyrinthPlaces.base(origin, place);
        BoundingBox slot = LabyrinthPlaces.slotBounds(origin, place);
        if (base == null || slot == null) {
            return;
        }
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
            case STRAIGHT_HALL, BENT_HALL, CROSS_HALL, QUIET_ROOM -> LabyrinthHalls.build(level, base, place);
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
        level.setBlock(base.offset(-4, 0, -9), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
        level.setBlock(base.offset(-4, 0, -10), stairs(Blocks.STONE_STAIRS, Direction.WEST), FLAGS);
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
                    level.setBlock(base.offset(x, y, z), state, FLAGS);
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
            level.setBlock(base.offset(0, 0, 0), Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(base.offset(0, 1, 0), Blocks.AIR.defaultBlockState(), FLAGS);
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
                    level.setBlock(base.offset(x, y, z), state, FLAGS);
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
        level.setBlock(lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), FLAGS);
        level.setBlock(lower.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), FLAGS);
    }

    static void hangLantern(ServerLevel level, BlockPos pos, boolean soul) {
        Block lantern = soul ? Blocks.SOUL_LANTERN : Blocks.LANTERN;
        level.setBlock(pos, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true), FLAGS);
    }

    static BlockState stairs(Block block, Direction back) {
        return block.defaultBlockState().setValue(StairBlock.FACING, back);
    }

    static void bed(ServerLevel level, BlockPos foot, Direction toHead, Block bed) {
        BlockState state = bed.defaultBlockState().setValue(BedBlock.FACING, toHead);
        level.setBlock(foot, state.setValue(BedBlock.PART, BedPart.FOOT), FLAGS);
        level.setBlock(foot.relative(toHead), state.setValue(BedBlock.PART, BedPart.HEAD), FLAGS);
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
