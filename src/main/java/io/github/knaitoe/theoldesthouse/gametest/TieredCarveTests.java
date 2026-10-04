package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The labyrinth is built by depth: the halls first, deeper places as explorers approach them. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TieredCarveTests {
    private TieredCarveTests() {}

    @GameTest(template = "empty")
    public static void placesAreBuiltInTheOrderExplorersCanReachThem(GameTestHelper h) {
        for (var core : LabyrinthBuilder.CORE) h.assertTrue(LabyrinthBuilder.requiredDepth(core) == 0, core.id() + " is part of the first build");
        int story = LabyrinthBuilder.requiredDepth(LabyrinthPlace.HARRIGAN);
        h.assertTrue(story == LabyrinthPacing.STORY_DEPTH, "stories wait for the depth that deals them");
        h.assertTrue(story < LabyrinthBuilder.requiredDepth(LabyrinthPlace.FOLDED_MAZE)
                && LabyrinthBuilder.requiredDepth(LabyrinthPlace.FOLDED_MAZE) < LabyrinthBuilder.requiredDepth(LabyrinthPlace.SPIRAL_STAIR)
                && LabyrinthBuilder.requiredDepth(LabyrinthPlace.SPIRAL_STAIR) < LabyrinthBuilder.requiredDepth(LabyrinthPlace.DEEP_MAZE)
                && LabyrinthBuilder.requiredDepth(LabyrinthPlace.DEEP_MAZE) < LabyrinthBuilder.requiredDepth(LabyrinthPlace.HOTEL_HALLWAY)
                && LabyrinthBuilder.requiredDepth(LabyrinthPlace.HOTEL_HALLWAY) < LabyrinthBuilder.requiredDepth(LabyrinthPlace.ABYSS_MAZE),
                "deeper tiers are built later");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "tiered_carve", timeoutTicks = 600)
    public static void theHallsStandFirstAndDeeperPlacesWaitForExplorers(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var interior = HouseTestLevel.get(server);
        HouseTestLevel.get(server, HouseDimensions.OUTSIDE);
        var storage = server.overworld().getDataStorage();
        var oldHouse = HouseSavedData.get(server);
        var oldData = LabyrinthData.get(server);
        var origin = new BlockPos(52000, 80, 52000);
        var house = new HouseSavedData();
        house.markSpawned(origin);
        storage.set("the_oldest_house", house);
        storage.set("the_oldest_house_labyrinth", new LabyrinthData());
        LabyrinthBuilder.clearAll();
        LabyrinthBuilder.gateForGameTest(true);
        try {
            h.assertTrue(!LabyrinthBuilder.ensureReachable(server), "a fresh manor starts its carve");
            LabyrinthBuilder.drainDueGameTest(server);
            var data = LabyrinthData.get(server);
            h.assertTrue(LabyrinthBuilder.ensureReachable(server), "the ordinary halls past the hallway stand");
            h.assertTrue(!LabyrinthBuilder.isCarving(), "construction waits for an explorer instead of working");
            h.assertTrue(!LabyrinthBuilder.isBuilt(server), "the deeper labyrinth is not built all at once");
            for (var core : LabyrinthBuilder.CORE)
                h.assertTrue(LabyrinthBuilder.isPlaceReady(data, core) && data.door(core.entryDoorId()) != null, core.id() + " stands with its entry");
            h.assertTrue(!LabyrinthBuilder.isPlaceReady(data, LabyrinthPlace.HARRIGAN), "a story waits until an explorer approaches its depth");
            UUID someone = UUID.randomUUID();
            h.assertTrue(!LabyrinthDealer.vignettesAvailable(data, someone).contains(LabyrinthPlace.HARRIGAN), "an unbuilt story is never dealt");
            h.assertTrue(LabyrinthDealer.grayAvailable(data, someone).stream().allMatch(place -> LabyrinthBuilder.isPlaceReady(data, place)),
                    "only standing halls are dealt");
            h.assertTrue(data.state(LabyrinthBuilder.PROGRESS).getList("Built", 8).size() == LabyrinthBuilder.CORE.size(),
                    "construction progress is saved place by place");

            // A restart resumes from the saved progress without rebuilding what already stands.
            var mark = LabyrinthPlaces.base(origin, LabyrinthPlace.JUNCTION).offset(0, 0, -5);
            interior.setBlock(mark, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            LabyrinthBuilder.clearAll();
            LabyrinthBuilder.gateForGameTest(true);
            h.assertTrue(LabyrinthBuilder.ensureReachable(server), "after a restart the standing halls open at once");
            LabyrinthBuilder.drainDueGameTest(server);
            h.assertTrue(interior.getBlockState(mark).is(Blocks.GOLD_BLOCK), "a restart does not rebuild a standing hall");
            h.succeed();
        } finally {
            LabyrinthBuilder.gateForGameTest(null);
            LabyrinthBuilder.clearAll();
            storage.set("the_oldest_house", oldHouse);
            storage.set("the_oldest_house_labyrinth", oldData);
        }
    }
}
