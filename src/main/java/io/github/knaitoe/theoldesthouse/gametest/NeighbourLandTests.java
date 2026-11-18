package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
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
 * highest ground or in front; the elk, Your Own Light, the finale's chest and overgrowth and the hotel tab leave district land alone;
 * and the House tells its neighbour where its own lot is. Every case works on ground far from every other case's.
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
            } finally {
                if (cow != null) {
                    cow.discard();
                }
                NativeTestPlayers.remove(p);
            }
        }));
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
                h.assertTrue(turnedRound(theirs) && asLeft(nearest) && asLeft(onDistrict),
                        "the chest turned round is the one the reader opened themselves, never the one on the district");
                h.assertTrue(!level.getBlockState(candle).getValue(CandleBlock.LIT) && level.getBlockState(districtCandle).getValue(CandleBlock.LIT),
                        "the lights left burning are put out, except on the district");

                OwnLight.visit(level, other, bed, "Not yet.");
                h.assertTrue(turnedRound(nearest) && asLeft(onDistrict), "for a reader who opened none, the nearest chest off the district");
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

                p.openMenu((ChestBlockEntity) level.getBlockEntity(single));
                h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 0 && p.getInventory().countItem(Items.DIAMOND) == 0,
                        "a chest off the district collects one place on the tab, as it always has");
                p.closeContainer();
            } finally {
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
                Runnable west = district(level, min, max);
                FinaleController.overgrowBase(p, record);
                west.run();
                h.assertTrue(level.getBlockState(mine).getValue(CropBlock.AGE) == ((CropBlock) Blocks.WHEAT).getMaxAge(), "the crop off the district ripens");
                for (var entry : before.entrySet()) {
                    h.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()), "nothing on the district is changed: " + entry.getKey());
                }
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(24)).forEach(ItemEntity::discard);
                NativeTestPlayers.remove(p);
            }
        });
    }
}
