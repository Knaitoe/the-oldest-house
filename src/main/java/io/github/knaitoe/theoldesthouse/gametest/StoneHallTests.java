package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The deep tier's stone halls: walkable, every way on reachable, and dealt only deep. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StoneHallTests {
    private StoneHallTests() {}

    private static final List<LabyrinthPlace> HALLS = List.of(LabyrinthPlace.STONE_GALLERY, LabyrinthPlace.STONE_CROSSING, LabyrinthPlace.STONE_DESCENT);

    private static boolean open(ServerLevel level, BlockPos at) {
        var state = level.getBlockState(at);
        return state.getCollisionShape(level, at).isEmpty() || state.getBlock() instanceof DoorBlock;
    }

    private static Set<BlockPos> walk(ServerLevel level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos at = queue.poll();
            for (Direction side : Direction.Plane.HORIZONTAL)
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos next = at.relative(side).above(dy);
                    if (seen.contains(next) || !open(level, next) || !open(level, next.above())) continue;
                    if (dy == 1 && !open(level, at.above(2))) continue;
                    if (level.getBlockState(next.below()).getCollisionShape(level, next.below()).isEmpty()) continue;
                    seen.add(next);
                    queue.add(next);
                }
        }
        return seen;
    }

    @GameTest(template = "empty")
    public static void stoneHallsCanBeWalkedToEveryWayOn(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        for (int i = 0; i < HALLS.size(); i++) {
            LabyrinthPlace hall = HALLS.get(i);
            BlockPos base = h.absolutePos(BlockPos.ZERO).offset(700 + i * 60, 12, 140);
            StoneHalls.build(level, base, hall);
            Set<BlockPos> reached = walk(level, base.offset(0, 0, -1));
            for (var door : hall.doors()) {
                BlockPos at = base.offset(door.rel());
                h.assertTrue(level.getBlockState(at).getBlock() instanceof DoorBlock, hall.id() + " has its " + door.name() + " door");
                if (door.name().equals("entry")) continue;
                h.assertTrue(reached.contains(at.relative(door.facing())), hall.id() + ": the " + door.name() + " way on can be walked to");
            }
            for (BlockPos at : BlockPos.betweenClosed(base.offset(hall.room().minX(), hall.room().minY(), hall.room().minZ()),
                    base.offset(hall.room().maxX(), hall.room().maxY(), hall.room().maxZ())))
                if (level.getBlockState(at).is(Blocks.LANTERN)) h.assertTrue(level.getBlockState(at).canSurvive(level, at), hall.id() + " hangs every lamp from the vault");
            h.assertTrue(LabyrinthDealer.onward(hall) && LabyrinthPacing.ordinary(hall), hall.id() + " is an ordinary way on");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void theDeepTierIsStone(GameTestHelper h) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();
        for (int depth = 1; depth <= 16; depth++) {
            data.pushReturn(player, new LabyrinthData.Waypoint(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR, new Vec3(depth, 64, 0), 0.0F, true));
            boolean stone = data.returnDepth(player) >= LabyrinthPacing.DEEP_DEPTH;
            for (LabyrinthPlace hall : HALLS)
                h.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(hall) == stone, hall.id() + " belongs to the deep tier only (depth " + depth + ")");
        }
        h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.STRAIGHT_HALL, 14) == 0 && LabyrinthDealer.grayWeight(LabyrinthPlace.STONE_GALLERY, 14) > 0,
                "at fourteen doors the ordinary way on is stone");
        for (int depth = 17; depth <= LabyrinthPacing.STAIRCASE_DEPTH; depth++)
            data.pushReturn(player, new LabyrinthData.Waypoint(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR, new Vec3(depth, 64, 0), 0.0F, true));
        h.assertTrue(data.returnDepth(player) == LabyrinthPacing.STAIRCASE_DEPTH, "the way back is remembered as deep as the great staircase");
        h.succeed();
    }
}
