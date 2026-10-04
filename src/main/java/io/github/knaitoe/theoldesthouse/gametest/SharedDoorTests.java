package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Labyrinth entry doors and vestibules shared by more than one explorer. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SharedDoorTests {
    private SharedDoorTests() {}

    private static Vec3 at(LabyrinthData.Door entry, double into, double side) {
        Direction toRoom = entry.facing.getOpposite();
        Direction across = toRoom.getClockWise();
        return Vec3.atBottomCenterOf(entry.lower)
                .add(toRoom.getStepX() * into + across.getStepX() * side, 0, toRoom.getStepZ() * into + across.getStepZ() * side);
    }

    private static void put(ServerPlayer player, ServerLevel level, Vec3 pos) {
        player.teleportTo(level, pos.x, pos.y, pos.z, 0, 0);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static boolean open(ServerLevel level, LabyrinthData.Door entry) {
        var state = level.getBlockState(entry.lower);
        return state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN);
    }

    @GameTest(template = "empty", batch = "shared_doors", timeoutTicks = 600)
    public static void entryDoorsShutOncePerCrossingAndNeverOnAnotherExplorer(GameTestHelper h) {
        var server = h.getLevel().getServer();
        HouseTestLevel.get(server);
        HouseTestLevel.get(server, HouseDimensions.OUTSIDE);
        var storage = server.overworld().getDataStorage();
        var oldHouse = HouseSavedData.get(server);
        var oldData = LabyrinthData.get(server);
        var origin = new BlockPos(56000, 80, 56000);
        var house = new HouseSavedData();
        house.markSpawned(origin);
        storage.set("the_oldest_house", house);
        storage.set("the_oldest_house_labyrinth", new LabyrinthData());
        LabyrinthBuilder.clearAll();
        LabyrinthBuilder.gateForGameTest(true);
        ServerPlayer a = null, b = null;
        try {
            LabyrinthBuilder.ensureReachable(server);
            LabyrinthBuilder.drainDueGameTest(server);
            var data = LabyrinthData.get(server);
            var entry = data.door(LabyrinthPlace.JUNCTION.entryDoorId());
            h.assertTrue(entry != null, "the junction's entry door stands");
            ServerLevel level = server.getLevel(entry.dimension);
            a = NativeTestPlayers.survival(h, "shared_door_a");
            b = NativeTestPlayers.survival(h, "shared_door_b");
            a.setNoGravity(true);
            b.setNoGravity(true);
            put(b, level, at(entry, 12, 0));

            // Walking in, the door shuts behind once.
            put(a, level, at(entry, 1.5, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            put(a, level, at(entry, 3.0, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            h.assertTrue(!open(level, entry), "the entry door shuts behind an explorer well inside");

            // Turning back, they open it from a step away; it is not shut on them again.
            LabyrinthDoors.use(a, entry);
            h.assertTrue(open(level, entry), "the explorer opens the entry door from inside");
            for (int i = 0; i < 5; i++) LabyrinthDoors.tickPlayer(a, origin);
            put(a, level, at(entry, 3.4, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            h.assertTrue(open(level, entry), "a door just opened from reach is not shut again before the explorer crosses");

            // Another explorer in the doorway keeps it open while the first walks in again.
            put(a, level, at(entry, 0.5, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            put(b, level, at(entry, -0.4, 0.5));
            put(a, level, at(entry, 4.0, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            h.assertTrue(open(level, entry), "the door is not shut on another explorer in the doorway");
            put(b, level, at(entry, 12, 0));
            LabyrinthDoors.tickPlayer(a, origin);
            h.assertTrue(!open(level, entry), "once the doorway is clear the door shuts behind");

            // Walking back down a vestibule rebuilt wider by another explorer's
            // corridor returns this one inside the three-wide hallway, not its wall.
            var hallwayDoor = origin.offset(io.github.knaitoe.theoldesthouse.house.HouseLayout.AXIS_X, 1,
                    io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway.END_Z_OFFSET);
            data.putDoor(new LabyrinthData.Door("hallway_end", HouseDimensions.INTERIOR, hallwayDoor, Direction.NORTH,
                    LabyrinthData.toPlace(LabyrinthPlace.JUNCTION), false));
            data.pushReturn(a.getUUID(), new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, Vec3.atBottomCenterOf(hallwayDoor),
                    Direction.NORTH.toYRot(), true));
            put(a, level, at(entry, -6.5, 2.6));
            LabyrinthDoors.tickPlayer(a, origin);
            h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway.isInsideWalkableVolume(origin, a.getX(), a.getY(), a.getZ()),
                    "a walk-back from a wide vestibule lands inside the hallway: " + a.position());
            put(a, level, at(entry, 4.0, 0));

            // A copied vestibule leaves the cells another explorer occupies.
            var mark = BlockPos.containing(at(entry, -2, 1)).above();
            var far = BlockPos.containing(at(entry, -4, -1)).above();
            put(b, level, Vec3.atBottomCenterOf(mark.below()));
            level.setBlock(mark.above(), Blocks.GLASS.defaultBlockState(), 3);
            level.setBlock(far, Blocks.GLASS.defaultBlockState(), 3);
            var source = new LabyrinthData.Door("shared_source", entry.dimension, entry.lower.above(30), entry.facing, "", true);
            for (int s = -7; s <= 7; s++) for (int y = 0; y <= 7; y++) for (int k = 0; k <= 16; k++)
                level.setBlock(source.lower.relative(source.facing, k).relative(source.facing.getClockWise(), s).above(y), Blocks.AIR.defaultBlockState(), 3);
            LabyrinthDoors.copyVestibule(level, source, level, entry, net.minecraft.world.level.block.Rotation.NONE, a);
            h.assertTrue(level.getBlockState(mark.above()).is(Blocks.GLASS), "the copy leaves what stands against another explorer");
            h.assertTrue(level.getBlockState(far).isAir(), "the rest of the vestibule is copied");
            h.succeed();
        } finally {
            if (a != null) NativeTestPlayers.remove(a);
            if (b != null) NativeTestPlayers.remove(b);
            LabyrinthBuilder.gateForGameTest(null);
            LabyrinthBuilder.clearAll();
            LabyrinthDoors.clearAll();
            storage.set("the_oldest_house", oldHouse);
            storage.set("the_oldest_house_labyrinth", oldData);
        }
    }
}
