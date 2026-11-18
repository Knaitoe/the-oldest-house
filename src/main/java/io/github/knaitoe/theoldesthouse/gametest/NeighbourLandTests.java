package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseSpawnManager;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkFollows;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleController;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleProgress;
import io.github.knaitoe.theoldesthouse.labyrinth.HotelVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRooms;
import io.github.knaitoe.theoldesthouse.labyrinth.OwnLight;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import io.github.knaitoe.theoldesthouse.opening.HomeBed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The House never edits a neighbour's district (docs/NEIGHBOURS.md, R2). The districts are installed here as A Reaping would send
 * them, without A Reaping. The manor is never sited within eight blocks of one, nor on a lot with anything built on it, below the
 * highest ground, in front, or under the lot where its foundations would run down to it; the elk, Your Own Light, the finale's chest
 * and overgrowth and the hotel tab leave district land alone; a neighbour is handed positions it may keep; and the House tells its
 * neighbour where its own lot is. Every case works on ground far from every other case's.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeighbourLandTests {
    private NeighbourLandTests() {
    }

    /** How far round a survey's anchor the ground is made resident: the nearest rings and the lot round each. */
    private static final int SURVEY_REACH = 80;

    /** A neighbour's district over the box from {@code min} to {@code max}, as A Reaping would send it. */
    private static Runnable district(ServerLevel level, BlockPos min, BlockPos max) {
        BoundingBox box = BoundingBox.fromCorners(min, max);
        return Neighbours.installDistrictForTesting((l, pos) -> l == level && box.isInside(pos));
    }

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, Block block) {
        for (BlockPos at : BlockPos.betweenClosed(from, to)) {
            level.setBlock(at, block.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** A stone floor under {@code base}'s plot, cleared above, out to {@code size} blocks. */
    private static void plot(ServerLevel level, BlockPos base, int size) {
        fill(level, base.offset(-2, -1, -2), base.offset(size, -1, size), Blocks.STONE);
        fill(level, base.offset(-2, 0, -2), base.offset(size, 5, size), Blocks.AIR);
    }

    /** The House's envelope with its origin at {@code origin}, and the margin no district may lie in. */
    private static BoundingBox kept(BlockPos origin) {
        HouseLayout.Box e = HouseLayout.ENVELOPE;
        int m = Neighbours.MARGIN;
        return new BoundingBox(origin.getX() + e.x0() - m, origin.getY() + e.y0() - m, origin.getZ() + e.z0() - m,
                origin.getX() + e.x1() + m, origin.getY() + e.y1() + m, origin.getZ() + e.z1() + m);
    }

    /** The lot with its origin at {@code origin}: what the site search checks and the finale's demolition clears. */
    private static BoundingBox lot(BlockPos origin) {
        HouseLayout.Box s = HouseLayout.SITE;
        return new BoundingBox(origin.getX() + s.x0(), origin.getY() + s.y0(), origin.getZ() + s.z0(),
                origin.getX() + s.x1(), origin.getY() + s.y1(), origin.getZ() + s.z1());
    }

    /**
     * Runs {@code body} once the chunks within {@code reach} of {@code base} are resident; the chunks are let go and every installed
     * district taken away however it ends.
     */
    private static void onGround(GameTestHelper h, BlockPos base, int reach, Consumer<ServerLevel> body) {
        ServerLevel level = h.getLevel();
        NativeTestChunks chunks = new NativeTestChunks();
        chunks.request(level, new AABB(base).inflate(reach));
        SceneReady.when(h, () -> chunks.ready() ? null : "the ground round " + base, 2, 1100, () -> {
            try {
                body.accept(level);
            } finally {
                Neighbours.forgetTestDistricts();
                chunks.close();
            }
            h.succeed();
        });
    }

    /** Runs {@code body} with a fresh LabyrinthData, and puts the world's own back afterwards. */
    private static void withFreshRecords(ServerLevel level, Runnable body) {
        var store = level.getServer().overworld().getDataStorage();
        LabyrinthData old = LabyrinthData.get(level.getServer());
        store.set("the_oldest_house_labyrinth", new LabyrinthData());
        try {
            body.run();
        } finally {
            store.set("the_oldest_house_labyrinth", old);
        }
    }

    // ------------------------------------------------------------------------------------------------ the manor's site

    @GameTest(template = "empty", batch = "neighbour_site", timeoutTicks = 1200)
    public static void theManorIsNeverSitedWithinEightBlocksOfADistrict(GameTestHelper h) {
        BlockPos anchor = new BlockPos(-91000, 0, 91000);
        onGround(h, anchor, SURVEY_REACH, level -> {
            var clean = HouseSpawnManager.survey(level, anchor);
            BlockPos first = clean.origin();
            h.assertTrue(first != null && clean.district() == 0, "with no neighbour the House finds its site as it always has: " + clean.describe());
            BoundingBox kept = kept(first);
            int bottom = level.getMinBuildHeight();
            int top = level.getMaxBuildHeight();

            // A district whose land begins right at the edge of the margin: the site is passed over for one clear of it.
            BoundingBox near = BoundingBox.fromCorners(new BlockPos(kept.maxX(), bottom, kept.minZ()), new BlockPos(kept.maxX() + 20, top, kept.maxZ()));
            Runnable nearby = district(level, new BlockPos(near.minX(), near.minY(), near.minZ()), new BlockPos(near.maxX(), near.maxY(), near.maxZ()));
            var moved = HouseSpawnManager.survey(level, anchor);
            nearby.run();
            h.assertTrue(moved.origin() != null && !moved.origin().equals(first) && moved.district() > 0,
                    "a site within eight blocks of a district is passed over: " + moved.describe());
            h.assertTrue(!kept(moved.origin()).intersects(near), "and the site chosen instead keeps its eight blocks from the district: " + moved.describe());

            // One block further off, the district leaves the site where it was.
            Runnable beyond = district(level, new BlockPos(kept.maxX() + 1, bottom, kept.minZ()), new BlockPos(kept.maxX() + 21, top, kept.maxZ()));
            var stays = HouseSpawnManager.survey(level, anchor);
            beyond.run();
            h.assertTrue(first.equals(stays.origin()) && stays.district() == 0, "a district nine blocks off leaves the site as it was: " + stays.describe());
        });
    }

    @GameTest(template = "empty", batch = "neighbour_lot", timeoutTicks = 1200)
    public static void anythingBuiltOnTheLotBelowTheGroundOrInFrontBlocksTheSite(GameTestHelper h) {
        BlockPos anchor = new BlockPos(-92000, 0, 92000);
        onGround(h, anchor, SURVEY_REACH, level -> {
            h.assertTrue(!HouseSpawnManager.isNaturalGround(Blocks.FARMLAND.defaultBlockState())
                            && !HouseSpawnManager.isNaturalGround(Blocks.DIRT_PATH.defaultBlockState())
                            && !HouseSpawnManager.isNaturalGround(Blocks.COBBLESTONE.defaultBlockState())
                            && !HouseSpawnManager.isNaturalGround(Blocks.OAK_FENCE.defaultBlockState()),
                    "tilled ground, a path, cobblestone and a fence are things somebody made");
            h.assertTrue(Stream.of(Blocks.LILY_PAD, Blocks.MANGROVE_ROOTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.BIG_DRIPLEAF,
                                    Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF, Blocks.SMALL_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER,
                                    Blocks.KELP_PLANT, Blocks.SEA_PICKLE, Blocks.BRAIN_CORAL_BLOCK)
                            .allMatch(block -> HouseSpawnManager.isNaturalGround(block.defaultBlockState())),
                    "what the world grows or leaves in swamps, lush caves, geodes and the sea is the ground");
            h.assertTrue(Stream.of(Blocks.BEEHIVE, Blocks.BONE_BLOCK, Blocks.MOSSY_COBBLESTONE, Blocks.BEE_NEST)
                            .noneMatch(block -> HouseSpawnManager.isNaturalGround(block.defaultBlockState()))
                            && !HouseSpawnManager.isNaturalGrowth(Blocks.BEE_NEST.defaultBlockState())
                            && !HouseSpawnManager.isNaturalGrowth(Blocks.LILY_PAD.defaultBlockState()),
                    "a hive somebody made is built; fossil bone, a forest rock's stone and a bees' nest are the ground only where the world leaves"
                            + " them; and what is only not built is not cleared for the House either");
            h.assertTrue(HouseSpawnManager.isNaturalGround(Blocks.GRASS_BLOCK.defaultBlockState())
                            && HouseSpawnManager.isNaturalGround(Blocks.DIRT.defaultBlockState())
                            && HouseSpawnManager.isNaturalGround(Blocks.STONE.defaultBlockState())
                            && HouseSpawnManager.isNaturalGround(Blocks.BEDROCK.defaultBlockState())
                            && HouseSpawnManager.isNaturalGround(Blocks.WATER.defaultBlockState()),
                    "grass, dirt, stone, bedrock and water are the ground");
            var clean = HouseSpawnManager.survey(level, anchor);
            BlockPos first = clean.origin();
            h.assertTrue(first != null && HouseSpawnManager.nothingBuiltOnTheLot(level, first), "on open ground the House finds its site: " + clean.describe());

            // Tilled ground, level with the ground round it: the floor would bury it and the demolition fill it, and nothing above
            // the highest ground shows it.
            BlockPos tilled = first.offset(10, -1, 10);
            BlockState ground = level.getBlockState(tilled);
            level.setBlock(tilled, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
            try {
                h.assertTrue(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tilled.getX(), tilled.getZ()) == first.getY(),
                        "the tilled ground stands level with the ground round it");
                var moved = HouseSpawnManager.survey(level, anchor);
                h.assertTrue(moved.origin() != null && !moved.origin().equals(first) && !lot(moved.origin()).isInside(tilled),
                        "tilled ground below the highest ground blocks the site, and the lot chosen instead leaves it be: " + moved.describe());
            } finally {
                level.setBlock(tilled, ground, Block.UPDATE_CLIENTS);
            }

            // In the ground below the origin, where the floor buries it: a forest rock's mossy cobblestone is the ground; fossil bone,
            // which the world leaves only deeper, and a bees' nest set in the ground are somebody's.
            try {
                level.setBlock(tilled, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, first), "a forest rock's stone below the origin is the ground");
                for (Block built : new Block[]{Blocks.BONE_BLOCK, Blocks.BEE_NEST}) {
                    level.setBlock(tilled, built.defaultBlockState(), Block.UPDATE_CLIENTS);
                    h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, first), built + " set in the ground on the lot is something built");
                }
            } finally {
                level.setBlock(tilled, ground, Block.UPDATE_CLIENTS);
            }

            // A fence where the steps come down, in front of the cleared ground: the demolition reaches it, so the search does.
            BlockPos front = first.offset(HouseLayout.AXIS_X, 0, -6);
            h.assertTrue(front.getZ() - first.getZ() < HouseLayout.CLEAR_MIN_Z && lot(first).isInside(front), "the fence stands on the lot, in front of the cleared ground");
            level.setBlock(front, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
            try {
                var moved = HouseSpawnManager.survey(level, anchor);
                h.assertTrue(moved.origin() != null && !moved.origin().equals(first) && !lot(moved.origin()).isInside(front),
                        "a fence in front blocks the site, and the lot chosen instead leaves it be: " + moved.describe());
            } finally {
                level.setBlock(front, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            // In front, level with the origin, mossy cobblestone may be somebody's; a bees' nest hanging over the open ground is the
            // bees' own, as trees carry them.
            BlockState overFront = level.getBlockState(front.above());
            try {
                level.setBlock(front, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, first), "mossy cobblestone in front, level with the origin, is something built");
                level.setBlock(front, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(front.above(), Blocks.BEE_NEST.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, first), "a bees' nest hanging over the ground in front is the ground");
            } finally {
                level.setBlock(front.above(), overFront, Block.UPDATE_CLIENTS);
                level.setBlock(front, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            // A fence down in a hollow, below the highest ground, where the cellar would cut: built. The empty hollow is only ground.
            BlockPos lip = first.offset(5, -1, 10);
            BlockPos low = lip.below();
            BlockState lipWas = level.getBlockState(lip);
            BlockState lowWas = level.getBlockState(low);
            level.setBlock(lip, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(low, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
            try {
                h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, first), "a fence two below the highest ground is something built on the lot");
                level.setBlock(low, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, first), "an empty hollow is only the shape of the ground");
            } finally {
                level.setBlock(low, lowWas, Block.UPDATE_CLIENTS);
                level.setBlock(lip, lipWas, Block.UPDATE_CLIENTS);
            }
            var again = HouseSpawnManager.survey(level, anchor);
            h.assertTrue(first.equals(again.origin()), "with the lot as it was, the site is as it was: " + again.describe());
        });
    }

    @GameTest(template = "empty", batch = "neighbour_dip", timeoutTicks = 1200)
    public static void anythingTheFoundationsWouldRunDownToUnderTheLotBlocksTheSite(GameTestHelper h) {
        BlockPos origin = new BlockPos(92600, 120, -92600);
        onGround(h, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.CENTER_Z), 48, level -> {
            HouseLayout.Box s = HouseLayout.SITE;
            // A raised stone plot under the whole lot, its top a row under the floor course and eight rows deep, open above.
            fill(level, origin.offset(s.x0(), -8, s.z0()), origin.offset(s.x1(), -1, s.z1()), Blocks.STONE);
            h.assertTrue(HouseBuilder.canBuildAt(level, origin) && HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "the plot is open, and nothing is built on it");

            // A dip as deep as the open pass takes, its foot a row under the cellar's floor: the foundations run down it and stop on
            // what lies at the bottom, burying it, and tilled ground under them turns to dirt.
            BlockPos dip = origin.offset(10, 0, 10);
            fill(level, dip.below(5), dip.below(), Blocks.AIR);
            BlockPos foot = dip.below(6);
            h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "an empty dip is only the shape of the ground");
            for (Block built : new Block[]{Blocks.OAK_FENCE, Blocks.FARMLAND}) {
                level.setBlock(foot, built.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(origin.getY() - level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, dip.getX(), dip.getZ()) == HouseBuilder.MAX_SITE_RELIEF
                                && HouseBuilder.canBuildAt(level, origin),
                        "with " + built + " at its foot the dip is as deep as the open pass takes, and nothing stands in the House's way");
                h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, origin), built + " at the foot of the dip is something built under the lot");
            }
            level.setBlock(foot, Blocks.BONE_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "fossil bone at the foot of the dip, in the ground under the lot, is the ground");
            level.setBlock(foot, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            fill(level, dip.below(5), dip.below(), Blocks.STONE);

            // A dip as deep as the rough pass takes: the foundations' whole run, with a fence at its foot.
            BlockPos deep = origin.offset(4, 0, 22);
            fill(level, deep.below(8), deep.below(), Blocks.AIR);
            BlockPos deepFoot = deep.below(HouseLayout.SUPPORT_RUN + 1);
            level.setBlock(deepFoot, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
            h.assertTrue(origin.getY() - level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, deep.getX(), deep.getZ()) == HouseLayout.SUPPORT_RUN,
                    "the deep dip is as deep as the rough pass takes");
            h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "a fence at the foot of the deepest dip any pass takes is something built under the lot");
            level.setBlock(deepFoot, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            fill(level, deep.below(8), deep.below(), Blocks.STONE);

            // A cave under the cellar's floor, the ground over it whole: the cellar is dug whatever the ground, and its foundations run
            // down the cave.
            BlockPos cellar = origin.offset(22, 0, 18);
            h.assertTrue(HouseLayout.CELLAR.box().contains(22, -1, 18), "the column is under the cellar");
            fill(level, cellar.below(8), cellar.below(6), Blocks.AIR);
            BlockPos caveFloor = cellar.below(10);
            level.setBlock(caveFloor, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
            h.assertTrue(level.getBlockState(cellar.below(5)).is(Blocks.STONE) && level.getBlockState(cellar.below()).is(Blocks.STONE), "the ground over the cave is whole");
            h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "a fence on the floor of a cave under the cellar is something built under the lot");
            level.setBlock(caveFloor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            fill(level, cellar.below(8), cellar.below(6), Blocks.STONE);

            // The deepest a run goes: from a row under the cellar's floor down to LOWEST_SUPPORT_Y, through a shaft of air. A fence
            // on the row under that is what the run stops on and buries; a fence a row lower still is out of the House's reach.
            BlockPos shaftTop = cellar.offset(0, HouseLayout.MIN_Y - 1, 0);
            BlockPos stop = cellar.offset(0, HouseLayout.LOWEST_SUPPORT_Y - 1, 0);
            h.assertTrue(stop.getY() - origin.getY() == -19, "a run under the cellar stops on the nineteenth row below the origin");
            Map<BlockPos, BlockState> shaft = new HashMap<>();
            for (BlockPos at : BlockPos.betweenClosed(stop.below(), shaftTop)) {
                shaft.put(at.immutable(), level.getBlockState(at));
            }
            try {
                fill(level, stop.above(), shaftTop, Blocks.AIR);
                level.setBlock(stop, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(!HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "a fence where the deepest run stops is something built under the lot");
                level.setBlock(stop, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(stop.below(), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
                h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "a fence a row under it is out of the House's reach");
            } finally {
                shaft.forEach((at, was) -> level.setBlock(at, was, Block.UPDATE_CLIENTS));
            }

            // A fence sealed in the ground under the floor course, where no run gets to: not the House's to bury, and the site stands.
            BlockPos sealed = origin.offset(6, -7, 6);
            level.setBlock(sealed, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_CLIENTS);
            h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "a fence sealed in the ground below where the runs stop leaves the site be");
            level.setBlock(sealed, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            h.assertTrue(HouseSpawnManager.nothingBuiltOnTheLot(level, origin), "with the plot as it was, nothing is built on it");
        });
    }

    @GameTest(template = "empty", batch = "neighbour_positions")
    public static void aNeighbourIsHandedPositionsItMayKeep(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        List<BlockPos> kept = new ArrayList<>();
        Runnable keeping = Neighbours.installDistrictForTesting((l, pos) -> {
            kept.add(pos);
            return false;
        });
        try {
            Neighbours.districtLandIn(level, new BoundingBox(0, 0, 0, 4, 4, 4));
            Neighbours.districtLand(level, new BlockPos.MutableBlockPos(1, 2, 3));
        } finally {
            keeping.run();
        }
        h.assertTrue(kept.size() == 28 && kept.stream().noneMatch(pos -> pos instanceof BlockPos.MutableBlockPos) && new HashSet<>(kept).size() == 28,
                "every position a neighbour is asked about is its own to keep: " + kept.size() + " asked, " + new HashSet<>(kept).size() + " kept apart");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "neighbour_house_land")
    public static void houseLandHoldsOnTheManorsLotAndItsMarginInTheOverworldOnly(GameTestHelper h) {
        ServerLevel overworld = h.getLevel().getServer().overworld();
        var store = overworld.getDataStorage();
        HouseSavedData old = HouseSavedData.get(overworld.getServer());
        BlockPos origin = new BlockPos(93000, 70, -93000);
        int m = Neighbours.MARGIN;
        try {
            store.set("the_oldest_house", new HouseSavedData());
            h.assertTrue(!Neighbours.houseLand(overworld, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.CENTER_Z)), "before the House comes there is no lot");
            HouseSavedData house = new HouseSavedData();
            house.markSpawned(origin);
            store.set("the_oldest_house", house);
            h.assertTrue(Neighbours.houseLand(overworld, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.CENTER_Z))
                            && Neighbours.houseLand(overworld, origin.offset(HouseLayout.MIN_X - m, -60, HouseLayout.MIN_Z - m))
                            && Neighbours.houseLand(overworld, origin.offset(HouseLayout.MAX_X + m, 200, HouseLayout.MAX_Z + m)),
                    "the lot holds at any height, out to its margin");
            h.assertTrue(!Neighbours.houseLand(overworld, origin.offset(HouseLayout.MIN_X - m - 1, 0, HouseLayout.CENTER_Z))
                            && !Neighbours.houseLand(overworld, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.MAX_Z + m + 1)),
                    "and not a block beyond it");
            ServerLevel interior = HouseTestLevel.get(overworld.getServer(), HouseDimensions.INTERIOR);
            h.assertTrue(!Neighbours.houseLand(interior, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.CENTER_Z)), "only in the Overworld");
            house.collapse();
            h.assertTrue(Neighbours.houseLand(overworld, origin.offset(HouseLayout.CENTER_X, 0, HouseLayout.CENTER_Z)), "a fallen House's lot is still its own");
        } finally {
            store.set("the_oldest_house", old);
        }
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ the other edits

    @GameTest(template = "empty", batch = "neighbour_elk", timeoutTicks = 1200)
    public static void theElkNeverTakesAnAnimalNorOpensAGateOnADistrict(GameTestHelper h) {
        BlockPos base = new BlockPos(91300, 100, -91300);
        onGround(h, base, 40, level -> withFreshRecords(level, () -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "elk_neighbour");
            Cow cow = null;
            try {
                plot(level, base, 18);
                // A pen with a gate and one cow in it, and a bed of the reader's own beside it.
                for (int x = 2; x <= 6; x++) {
                    for (int z = 2; z <= 6; z++) {
                        if (x == 2 || x == 6 || z == 2 || z == 6) {
                            level.setBlock(base.offset(x, 0, z), (x == 4 && z == 2 ? Blocks.OAK_FENCE_GATE : Blocks.OAK_FENCE).defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
                BlockPos gate = base.offset(4, 0, 2);
                // The next pen's gate, nearer the cow than six blocks but not its own.
                BlockPos nextPen = base.offset(4, 0, 9);
                level.setBlock(nextPen.west(), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(nextPen, Blocks.OAK_FENCE_GATE.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(nextPen.east(), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
                cow = EntityType.COW.create(level);
                cow.moveTo(Vec3.atBottomCenterOf(base.offset(4, 0, 4)));
                level.addFreshEntity(cow);
                BlockPos foot = base.offset(12, 0, 6);
                NovelRooms.bed(level, foot, Blocks.WHITE_BED, Direction.NORTH);
                p.setRespawnPosition(level.dimension(), foot.north(), 0, true, false);
                h.assertTrue(HomeBed.of(p).isPresent(), "the bed is the reader's home");
                LabyrinthData d = LabyrinthData.get(level.getServer());
                CompoundTag own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
                own.putBoolean(ElkFollows.FOLLOWS, true);
                own.putInt(ElkFollows.IN_KIND, 2);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, own);

                Runnable pen = district(level, base.offset(0, -1, 0), base.offset(8, 3, 8));
                boolean took = ElkFollows.morning(p);
                pen.run();
                h.assertTrue(!took && cow.isAlive() && !level.getBlockState(gate).getValue(FenceGateBlock.OPEN)
                                && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 2,
                        "a penned animal on a district is never taken, its gate stays shut, and the debt is kept");

                Runnable gateOnly = district(level, gate, gate);
                took = ElkFollows.morning(p);
                gateOnly.run();
                h.assertTrue(took && !cow.isAlive() && !level.getBlockState(gate).getValue(FenceGateBlock.OPEN)
                                && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 1,
                        "off the district she takes one in kind, and leaves shut a gate that stands on one");
                h.assertTrue(!level.getBlockState(nextPen).getValue(FenceGateBlock.OPEN), "and opens no other pen's gate instead");
            } finally {
                if (cow != null) {
                    cow.discard();
                }
                NativeTestPlayers.remove(p);
            }
        }));
    }

    /** The first cell south of {@code from} that the sixty mornings' overgrowth tries for a vine (one cell in eleven). */
    private static BlockPos vineCell(BlockPos from) {
        for (int i = 0; i < 11; i++) {
            if (Math.floorMod(from.south(i).asLong(), 11) == 0) {
                return from.south(i);
            }
        }
        throw new IllegalStateException("no cell the overgrowth tries south of " + from);
    }

    private static ChestBlockEntity chest(ServerLevel level, BlockPos at) {
        level.setBlock(at, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(at);
        chest.setItem(0, new ItemStack(Items.APPLE));
        chest.setItem(1, new ItemStack(Items.BREAD));
        return chest;
    }

    private static boolean turnedRound(ChestBlockEntity chest) {
        int n = chest.getContainerSize();
        return chest.getItem(n - 1).is(Items.APPLE) && chest.getItem(n - 2).is(Items.BREAD) && chest.getItem(0).isEmpty();
    }

    private static boolean asLeft(ChestBlockEntity chest) {
        return chest.getItem(0).is(Items.APPLE) && chest.getItem(1).is(Items.BREAD);
    }

    @GameTest(template = "empty", batch = "neighbour_own_light", timeoutTicks = 1200)
    public static void yourOwnLightTurnsRoundAChestOfTheReadersOwnAndNothingOnADistrict(GameTestHelper h) {
        BlockPos base = new BlockPos(91600, 100, -91600);
        onGround(h, base, 40, level -> withFreshRecords(level, () -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "light_neighbour");
            ServerPlayer other = NativeTestPlayers.survival(h, "light_neighbour_too");
            try {
                plot(level, base, 18);
                BlockPos foot = base.offset(8, 0, 8);
                NovelRooms.bed(level, foot, Blocks.WHITE_BED, Direction.NORTH);
                BlockPos bed = foot.north();
                p.setRespawnPosition(level.dimension(), bed, 0, true, false);
                other.setRespawnPosition(level.dimension(), bed, 0, true, false);
                ChestBlockEntity nearest = chest(level, bed.offset(2, 0, 0));
                ChestBlockEntity theirs = chest(level, bed.offset(5, 0, 0));
                ChestBlockEntity onDistrict = chest(level, bed.offset(-3, 0, 0));
                // Further off than the nearest, and where the visit has always looked first.
                ChestBlockEntity firstFound = chest(level, bed.offset(6, 0, -5));
                BlockPos districtCandle = bed.offset(-4, 0, 2);
                BlockPos candle = bed.offset(3, 0, 3);
                level.setBlock(districtCandle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true), Block.UPDATE_ALL);
                level.setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true), Block.UPDATE_ALL);
                // The reader opens two chests near their bed with their own hands: one beside it, one on the district.
                p.openMenu(theirs);
                p.closeContainer();
                p.openMenu(onDistrict);
                p.closeContainer();
                district(level, bed.offset(-6, -1, -2), bed.offset(-3, 3, 3));

                OwnLight.visit(level, p, bed, "Not yet.");
                h.assertTrue(turnedRound(theirs) && asLeft(nearest) && asLeft(onDistrict) && asLeft(firstFound),
                        "the chest turned round is the one the reader opened themselves, never the one on the district");
                h.assertTrue(!level.getBlockState(candle).getValue(CandleBlock.LIT) && level.getBlockState(districtCandle).getValue(CandleBlock.LIT),
                        "the lights left burning are put out, except on the district");

                OwnLight.visit(level, other, bed, "Not yet.");
                h.assertTrue(turnedRound(firstFound) && asLeft(nearest) && asLeft(onDistrict),
                        "for a reader who opened none, the first chest found off the district, as it always was");
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(24)).forEach(ItemEntity::discard);
                NativeTestPlayers.remove(p);
                NativeTestPlayers.remove(other);
            }
        }));
    }

    @GameTest(template = "empty", batch = "neighbour_hotel_tab", timeoutTicks = 1200)
    public static void theHotelTabIsNeverCollectedFromAContainerOnADistrict(GameTestHelper h) {
        BlockPos base = new BlockPos(91900, 100, -91900);
        onGround(h, base, 40, level -> withFreshRecords(level, () -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "tab_neighbour");
            MinecartChest cart = null;
            try {
                plot(level, base, 18);
                // A double chest whose east half stands on a district, and a chest off it.
                BlockPos west = base.offset(2, 0, 2);
                BlockPos east = west.east();
                BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
                level.setBlock(west, chest.setValue(ChestBlock.TYPE, ChestType.LEFT), Block.UPDATE_ALL);
                level.setBlock(east, chest.setValue(ChestBlock.TYPE, ChestType.RIGHT), Block.UPDATE_ALL);
                BlockPos single = base.offset(12, 0, 2);
                level.setBlock(single, chest, Block.UPDATE_ALL);
                district(level, east, east.offset(3, 2, 3));
                p.getInventory().add(new ItemStack(Items.DIAMOND));
                LabyrinthData d = LabyrinthData.get(level.getServer());
                CompoundTag own = HotelVignette.personal(d, p.getUUID());
                own.putInt("Debt", 1);
                HotelVignette.save(d, p.getUUID(), own);

                var both = level.getBlockState(west).getMenuProvider(level, west);
                h.assertTrue(both != null, "the two halves make one double chest");
                p.openMenu(both);
                h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 1 && p.getInventory().countItem(Items.DIAMOND) == 1,
                        "a double chest half on a district collects nothing on the tab");
                p.closeContainer();

                // A cart on the district, opened by a reader standing off it: the cart is where it stands.
                p.teleportTo(level, base.getX() + 10.5, base.getY(), base.getZ() + 9.5, 0, 0);
                cart = EntityType.CHEST_MINECART.create(level);
                cart.moveTo(Vec3.atBottomCenterOf(east.offset(2, 0, 2)));
                level.addFreshEntity(cart);
                h.assertTrue(Neighbours.districtLand(level, cart.blockPosition()) && !Neighbours.districtLand(level, p.blockPosition()),
                        "the cart stands on the district, and the reader off it");
                p.openMenu(cart);
                h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 1 && p.getInventory().countItem(Items.DIAMOND) == 1,
                        "a cart on a district collects nothing on the tab");
                p.closeContainer();

                // An ender chest stands nowhere of its own: it is where the reader stands, here on the district.
                p.teleportTo(level, east.getX() + 2.5, east.getY(), east.getZ() + 1.5, 0, 0);
                h.assertTrue(Neighbours.districtLand(level, p.blockPosition()), "the reader stands on the district");
                p.openMenu(new SimpleMenuProvider((id, inventory, who) -> ChestMenu.threeRows(id, inventory, p.getEnderChestInventory()),
                        Component.literal("Ender Chest")));
                h.assertTrue(p.containerMenu instanceof ChestMenu
                                && HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 1 && p.getInventory().countItem(Items.DIAMOND) == 1,
                        "an ender chest opened on a district collects nothing on the tab");
                p.closeContainer();

                p.openMenu((ChestBlockEntity) level.getBlockEntity(single));
                h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 0 && p.getInventory().countItem(Items.DIAMOND) == 0,
                        "a chest off the district collects one place on the tab, as it always has");
                p.closeContainer();
            } finally {
                if (cart != null) {
                    cart.discard();
                }
                NativeTestPlayers.remove(p);
            }
        }));
    }

    @GameTest(template = "empty", batch = "neighbour_epilogue", timeoutTicks = 1200)
    public static void theEndingsChestAndOvergrowthLeaveADistrictAloneAndThePagesStillCome(GameTestHelper h) {
        BlockPos base = new BlockPos(92200, 100, -92200);
        onGround(h, base, 40, level -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "epilogue_neighbour");
            try {
                plot(level, base, 16);
                BlockPos foot = base.offset(6, 0, 6);
                NovelRooms.bed(level, foot, Blocks.WHITE_BED, Direction.NORTH);
                BlockPos bed = foot.north();
                p.setRespawnPosition(level.dimension(), bed, 0, true, false);
                h.assertTrue(HomeBed.of(p).isPresent(), "the bed is the reader's home");
                CompoundTag record = new CompoundTag();
                record.putString("Phase", FinaleProgress.Phase.ESCAPED.name());

                // Every free cell round the bed on a district, the bed itself off it: no chest, and the pages are left for the reader.
                BoundingBox all = BoundingBox.fromCorners(base.offset(-2, -1, -2), base.offset(16, 5, 16));
                Runnable everywhere = Neighbours.installDistrictForTesting((l, pos) -> l == level && all.isInside(pos) && !pos.equals(bed) && !pos.equals(foot));
                h.assertTrue(HomeBed.of(p).isPresent(), "the bed is still the reader's home");
                boolean delivered = FinaleController.epilogue(p, record);
                everywhere.run();
                AABB round = new AABB(bed).inflate(5);
                boolean chest = BlockPos.betweenClosedStream(bed.offset(-4, 0, -4), bed.offset(4, 2, 4)).anyMatch(at -> level.getBlockState(at).is(Blocks.CHEST));
                var pages = level.getEntitiesOfClass(DeliveredItemEntity.class, round, e -> p.getUUID().equals(e.getTarget()));
                h.assertTrue(delivered && !chest && pages.size() == 2, "with no free cell off the district the pages are left for the reader as they are: " + pages.size() + (chest ? ", and a chest" : ""));
                pages.forEach(DeliveredItemEntity::discard);

                // The district over the near half of the ring round the bed, where the first cells are: the chest stands in the first
                // free cell off it, none on it, and nothing is left loose.
                BoundingBox near = BoundingBox.fromCorners(bed.offset(-4, -1, -4), bed.offset(4, 3, -1));
                Runnable half = Neighbours.installDistrictForTesting((l, pos) -> l == level && near.isInside(pos));
                delivered = FinaleController.epilogue(p, record);
                half.run();
                List<BlockPos> chests = BlockPos.betweenClosedStream(bed.offset(-4, 0, -4), bed.offset(4, 2, 4))
                        .filter(at -> level.getBlockState(at).is(Blocks.CHEST)).map(BlockPos::immutable).toList();
                var loose = level.getEntitiesOfClass(DeliveredItemEntity.class, round, e -> e.isAlive() && p.getUUID().equals(e.getTarget()));
                h.assertTrue(delivered && chests.equals(List.of(bed.offset(-4, 0, 0))) && loose.isEmpty(),
                        "with the near half of the ring on a district the chest stands in the first free cell off it, and nothing is left loose: "
                                + chests + ", " + loose.size() + " loose");
                ChestBlockEntity left = (ChestBlockEntity) level.getBlockEntity(chests.get(0));
                h.assertTrue(left.getItem(0).is(Items.WRITTEN_BOOK) && left.getItem(1).is(Items.WRITTEN_BOOK), "and the pages are in it");
                left.clearContent();
                level.setBlock(chests.get(0), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);

                // Sixty mornings: a crop on the district is left as it was, and nothing grows on it; one off it ripens.
                BlockPos theirs = base.offset(2, 0, 10);
                BlockPos mine = base.offset(10, 0, 10);
                for (BlockPos crop : new BlockPos[]{theirs, mine}) {
                    level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(crop, Blocks.WHEAT.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                BlockPos min = base.offset(-4, -4, -4);
                BlockPos max = base.offset(5, 6, 16);
                Map<BlockPos, BlockState> before = new HashMap<>();
                for (BlockPos at : BlockPos.betweenClosed(min, max)) {
                    before.put(at.immutable(), level.getBlockState(at));
                }
                // A stone a step off the district with an open cell between where a vine would go, and one well off it.
                BlockPos besideIt = vineCell(base.offset(max.getX() - base.getX() + 1, 2, -3));
                BlockPos offIt = vineCell(base.offset(max.getX() - base.getX() + 5, 2, -3));
                for (BlockPos cell : new BlockPos[]{besideIt, offIt}) {
                    level.setBlock(cell.east(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                Runnable west = district(level, min, max);
                FinaleController.overgrowBase(p, record);
                west.run();
                h.assertTrue(level.getBlockState(mine).getValue(CropBlock.AGE) == ((CropBlock) Blocks.WHEAT).getMaxAge(), "the crop off the district ripens");
                h.assertTrue(level.getBlockState(offIt).is(Blocks.VINE), "a vine grows on a stone off the district");
                h.assertTrue(level.getBlockState(besideIt).isAir(), "and none where it could spread onto the district in a step");
                for (var entry : before.entrySet()) {
                    h.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()), "nothing on the district is changed: " + entry.getKey());
                }
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(24)).forEach(ItemEntity::discard);
                NativeTestPlayers.remove(p);
            }
        });
    }

    @GameTest(template = "empty", batch = "neighbour_epilogue_homeless", timeoutTicks = 1200)
    public static void theEndingsPagesWaitUntilAReaderWithNoHomeStandsWhereTheyCanLie(GameTestHelper h) {
        BlockPos base = new BlockPos(92800, 100, -92800);
        onGround(h, base, 40, level -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "epilogue_homeless");
            BlockPos pool = base.offset(4, 0, 4);
            BlockPos pit = base.offset(10, -1, 4);
            try {
                plot(level, base, 16);
                h.assertTrue(HomeBed.of(p).isEmpty(), "the reader has no home");
                CompoundTag record = new CompoundTag();
                record.putString("Phase", FinaleProgress.Phase.ESCAPED.name());
                // Every cell round them on a district, so no chest stands near them anywhere.
                district(level, base.offset(-8, -8, -8), base.offset(24, 12, 24));
                // A basin of water, a pit of lava in the floor, and the open air four rows up.
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    level.setBlock(pool.relative(side), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                level.setBlock(pool, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pit.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pit, Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);
                AABB round = new AABB(base).inflate(24);
                String[] where = {"swimming", "over lava", "in the air"};
                BlockPos[] at = {pool, pit.above(), base.offset(4, 4, 10)};
                for (int i = 0; i < at.length; i++) {
                    p.teleportTo(level, at[i].getX() + .5, at[i].getY(), at[i].getZ() + .5, 0, 0);
                    boolean delivered = FinaleController.epilogue(p, record);
                    boolean chest = BlockPos.betweenClosedStream(at[i].offset(-4, 0, -4), at[i].offset(4, 2, 4)).anyMatch(c -> level.getBlockState(c).is(Blocks.CHEST));
                    var pages = level.getEntitiesOfClass(DeliveredItemEntity.class, round, e -> e.isAlive() && p.getUUID().equals(e.getTarget()));
                    h.assertTrue(p.blockPosition().equals(at[i]) && !delivered && !chest && pages.isEmpty(),
                            "a reader with no home " + where[i] + " is left nothing, and the pages wait: " + pages.size() + (chest ? ", and a chest" : ""));
                }

                // On the stone, where pages lie: left at their feet.
                BlockPos stand = base.offset(8, 0, 10);
                p.teleportTo(level, stand.getX() + .5, stand.getY(), stand.getZ() + .5, 0, 0);
                boolean delivered = FinaleController.epilogue(p, record);
                boolean chest = BlockPos.betweenClosedStream(stand.offset(-4, 0, -4), stand.offset(4, 2, 4)).anyMatch(c -> level.getBlockState(c).is(Blocks.CHEST));
                var pages = level.getEntitiesOfClass(DeliveredItemEntity.class, round, e -> e.isAlive() && p.getUUID().equals(e.getTarget()));
                h.assertTrue(delivered && !chest && pages.size() == 2 && pages.stream().allMatch(e -> e.blockPosition().equals(stand)),
                        "standing on the stone they are left the pages at their feet: " + pages.size() + (chest ? ", and a chest" : ""));
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(24)).forEach(ItemEntity::discard);
                level.setBlock(pit, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pool, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                NativeTestPlayers.remove(p);
            }
        });
    }
}
