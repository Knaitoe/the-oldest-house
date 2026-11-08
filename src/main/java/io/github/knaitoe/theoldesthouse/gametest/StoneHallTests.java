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

    /**
     * 0.4.374 (docs/TEXTURE_PASS_STONE_0_4_374.md): all six stone halls wear their own stone. Walls and vaults are dressed ashlar
     * with some cracked (never most), the floors flagstones worn down the middle, every wall's foot a damp course or a carved band,
     * and none of the vanilla masonry they were laid in shows; the shapes stay, so every way on is still walked to.
     */
    @GameTest(template = "empty")
    public static void stoneHallsWearTheirOwnStone(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        var all = List.of(LabyrinthPlace.STONE_GALLERY, LabyrinthPlace.STONE_CROSSING, LabyrinthPlace.STONE_DESCENT,
                LabyrinthPlace.STONE_ARCADE, LabyrinthPlace.STONE_BEND, LabyrinthPlace.STONE_LANDING);
        for (int i = 0; i < all.size(); i++) {
            LabyrinthPlace hall = all.get(i);
            BlockPos base = h.absolutePos(BlockPos.ZERO).offset(4400 + i * 60, 12, 420);
            StoneHalls.build(level, base, hall);
            int ashlar = 0, cracked = 0, flags = 0, worn = 0, course = 0, carved = 0, vanilla = 0;
            var box = hall.room();
            for (BlockPos at : BlockPos.betweenClosed(base.offset(box.minX(), box.minY(), box.minZ()), base.offset(box.maxX(), box.maxY(), box.maxZ()))) {
                var s = level.getBlockState(at);
                if (s.is(HallRegistry.stone("flags_worn"))) { flags++; worn++; }
                else if (s.is(HallRegistry.stone("flags"))) flags++;
                else if (s.is(HallRegistry.stone("damp_course"))) course++;
                else if (s.is(HallRegistry.stone("carved"))) carved++;
                else if (s.is(HallRegistry.stone("ashlar_cracked"))) { ashlar++; cracked++; }
                else if (s.is(HallRegistry.stone("ashlar")) || s.is(HallRegistry.stone("ashlar_weathered")) || s.is(HallRegistry.stone("ashlar_damp"))) ashlar++;
                else if (s.is(Blocks.STONE_BRICKS) || s.is(Blocks.CRACKED_STONE_BRICKS) || s.is(Blocks.MOSSY_STONE_BRICKS) || s.is(Blocks.CHISELED_STONE_BRICKS)) vanilla++;
            }
            h.assertTrue(ashlar > 100 && cracked > 5 && cracked * 5 < ashlar && flags > 20 && worn > 0 && course + carved > 10 && vanilla == 0,
                    hall.id() + " wears its own stone: ashlar " + ashlar + " (cracked " + cracked + "), flags " + flags + " (worn " + worn + "), damp course " + course + ", carved " + carved + ", vanilla masonry " + vanilla);
            Set<BlockPos> reached = walk(level, base.offset(0, 0, -1));
            for (var door : hall.doors())
                if (!door.name().equals("entry"))
                    h.assertTrue(reached.contains(base.offset(door.rel()).relative(door.facing())), hall.id() + ": the " + door.name() + " way on is still walked to");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void theSeventhPassStartsTheStoneTier(GameTestHelper h) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();
        for (int depth = 1; depth <= 16; depth++) {
            data.pushReturn(player, new LabyrinthData.Waypoint(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR, new Vec3(depth, 64, 0), 0.0F, true));
            boolean stone = data.returnDepth(player) >= LabyrinthPacing.STONE_DEPTH;
            for (LabyrinthPlace hall : HALLS)
                h.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(hall) == stone, hall.id() + " enters the middle at seven forward passes (depth " + depth + ")");
        }
        h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.STRAIGHT_HALL, 7) == 0 && LabyrinthDealer.grayWeight(LabyrinthPlace.STONE_GALLERY, 7) > 0,
                "at seven passes the newly discovered ordinary way on is stone");
        for (int depth = 17; depth <= LabyrinthPacing.STAIRCASE_DEPTH; depth++)
            data.pushReturn(player, new LabyrinthData.Waypoint(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR, new Vec3(depth, 64, 0), 0.0F, true));
        h.assertTrue(data.returnDepth(player) == LabyrinthPacing.STAIRCASE_DEPTH, "the way back is remembered as deep as the great staircase");
        h.succeed();
    }
}
