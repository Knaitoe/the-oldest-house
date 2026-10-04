package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The labyrinth holds still: the same doors from the same hallway lead to the same halls. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LabyrinthMapTests {
    private LabyrinthMapTests() {}

    private static final long SALT = new BlockPos(9000, 64, 9000).asLong();

    private static Map<String, String> doorsOf(LabyrinthData data, UUID player, LabyrinthPlace place) {
        Map<String, String> out = new LinkedHashMap<>();
        for (var spec : place.doors()) {
            var door = data.door(place.doorId(spec));
            if (door != null && LabyrinthData.DEALT.equals(door.destination)) out.put(door.id, data.deal(player, door).place());
        }
        return out;
    }

    private static void enter(LabyrinthData data, UUID player, BlockPos through, LabyrinthPlace place) {
        data.pushReturn(player, new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, Vec3.atBottomCenterOf(through), 0.0F, true));
        data.visit(player, place);
        LabyrinthDealer.arriveAt(data, player, place, SALT);
    }

    @GameTest(template = "empty")
    public static void theSameRouteLeadsToTheSameHallsForEveryExplorer(GameTestHelper h) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.CROSS_HALL, new BlockPos(0, 64, 200));
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        BlockPos hallway = new BlockPos(15, 65, 83);

        enter(data, first, hallway, LabyrinthPlace.JUNCTION);
        var map = doorsOf(data, first, LabyrinthPlace.JUNCTION);
        h.assertTrue(!map.isEmpty(), "the junction's doors are dealt");

        // Somewhere else, the same physical doors are dealt anew for this player...
        for (int i = 0; i < 20; i++) LabyrinthDealer.dealPlace(data, first, LabyrinthPlace.JUNCTION, RandomSource.create(i));
        data.popReturn(first);
        // ...but coming back by the same route finds what was there.
        enter(data, first, hallway, LabyrinthPlace.JUNCTION);
        h.assertTrue(map.equals(doorsOf(data, first, LabyrinthPlace.JUNCTION)), "a place reached the same way keeps its doors: " + map);

        // A companion taking the same doors finds the same ordinary halls.
        enter(data, second, hallway, LabyrinthPlace.JUNCTION);
        h.assertTrue(map.equals(doorsOf(data, second, LabyrinthPlace.JUNCTION)), "explorers sharing a route share its halls");

        // A different route to the same physical room is a different spot on the map.
        data.popReturn(first);
        enter(data, first, hallway.east(40), LabyrinthPlace.JUNCTION);
        long here = data.nodeKey(first, LabyrinthPlace.JUNCTION);
        data.popReturn(first);
        enter(data, first, hallway, LabyrinthPlace.JUNCTION);
        h.assertTrue(here != data.nodeKey(first, LabyrinthPlace.JUNCTION), "routes are told apart");

        // The map survives a restart.
        var saved = data.save(new CompoundTag(), h.getLevel().registryAccess());
        var loaded = LabyrinthData.load(saved, h.getLevel().registryAccess());
        h.assertTrue(loaded.node(first, data.nodeKey(first, LabyrinthPlace.JUNCTION)) != null, "the explorer's map is saved");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryHallsDoNotPingPongBackToWhereTheExplorerCameFrom(GameTestHelper h) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.STRAIGHT_HALL, new BlockPos(0, 64, 0));
        UUID player = UUID.randomUUID();
        for (int i = 0; i < 7; i++) data.pushReturn(player, new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, new Vec3(i, 64, 0), 0.0F, true));
        var random = RandomSource.create(77);
        for (int i = 0; i < 200; i++) {
            data.visit(player, LabyrinthPlace.BENT_HALL);
            data.visit(player, LabyrinthPlace.STRAIGHT_HALL);
            LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.STRAIGHT_HALL, random);
            for (var place : doorsOf(data, player, LabyrinthPlace.STRAIGHT_HALL).values())
                h.assertTrue(!LabyrinthPlace.BENT_HALL.id().equals(place), "a hall does not lead straight back to the one just left");
        }
        h.succeed();
    }
}
