package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.DoorLeakKind;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoorLeaks;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLighting;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthMaze;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.MazeLayout;
import io.github.knaitoe.theoldesthouse.network.DoorLeaksPayload;
import io.netty.buffer.Unpooled;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MazeTests {
    private static final List<LabyrinthPlace> PLACES = List.of(LabyrinthPlace.GRAY_CORRIDOR, LabyrinthPlace.FOLDED_MAZE,
            LabyrinthPlace.DEEP_MAZE, LabyrinthPlace.ABYSS_MAZE);
    private MazeTests() {}

    @GameTest(template = "empty")
    public static void seededMazesHaveChoicesDeadEndsCyclesAndLongRoutes(GameTestHelper helper) {
        for (LabyrinthPlace place : PLACES) for (int seed = 0; seed < 24; seed++) {
            MazeLayout layout = MazeLayout.create(place, seed * 747L);
            helper.assertTrue(layout.pathLength() >= layout.size() * 2, "the exit requires a winding route: " + place + "/" + seed);
            helper.assertTrue(layout.deadEnds() >= 4 && layout.junctions() >= 2 && layout.cycles() >= 1,
                    "a real maze has wrong branches, choices and circuits: " + place + "/" + seed);
            MazeLayout copy = MazeLayout.create(place, seed * 747L);
            helper.assertTrue(layout.floor().equals(copy.floor()) && layout.sleeves().equals(copy.sleeves()),
                    "the seed restores the same geometry rather than moving gear between visits");
        }
        helper.succeed();
    }

    private static Set<BlockPos> reachable(GameTestHelper helper, BlockPos base) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(base.offset(0, 0, -3)); seen.add(queue.peek());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos n = pos.relative(direction);
                if (seen.contains(n) || !helper.getLevel().getBlockState(n).isAir() || !helper.getLevel().getBlockState(n.above()).isAir()
                        || !helper.getLevel().getBlockState(n.below()).isCollisionShapeFullBlock(helper.getLevel(), n.below())) continue;
                seen.add(n); queue.add(n);
            }
        }
        return seen;
    }

    @GameTest(template = "empty")
    public static void physicalMazesHaveFloorsHeadroomAndReachableExits(GameTestHelper helper) {
        for (int i = 0; i < PLACES.size(); i++) {
            LabyrinthPlace place = PLACES.get(i);
            BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(350 + i * 110, 6, 120);
            MazeLayout layout = MazeLayout.create(place, 747);
            LabyrinthMaze.build(helper.getLevel(), base, place, layout);
            Set<BlockPos> reached = reachable(helper, base);
            for (BlockPos floor : layout.floor()) helper.assertTrue(reached.contains(base.offset(floor)),
                    "every carved corridor can be walked without phasing or falling: " + place + " " + floor);
            for (LabyrinthPlace.DoorSpec door : place.doors()) {
                if (door.name().equals("entry")) continue;
                helper.assertTrue(reached.contains(base.offset(door.rel().relative(door.facing()))), "the exit has a real route: " + door.name());
            }
            helper.assertTrue(layout.floor().stream().noneMatch(p -> p.getX() <= place.room().minX()
                    || p.getX() >= place.room().maxX() || p.getZ() <= place.room().minZ()), "the maze retains an enclosing wall");
            // Compact geometry dump for rendering the exact tested layouts outside Minecraft.
            String columns = layout.floor().stream().map(p -> "[" + p.getX() + "," + p.getZ() + "]").collect(java.util.stream.Collectors.joining(","));
            String seams = layout.sleeves().stream().map(s -> "[" + s.center().getX() + "," + s.center().getZ() + "," + s.turn() + "]")
                    .collect(java.util.stream.Collectors.joining(","));
            TheOldestHouse.LOGGER.info("OTH-MAZE {\"place\":\"{}\",\"seed\":747,\"columns\":[{}],\"seams\":[{}]}", place.id(), columns, seams);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foldsAreReversibleAndCatchFastCrossings(GameTestHelper helper) {
        MazeLayout layout = MazeLayout.create(LabyrinthPlace.ABYSS_MAZE, 747);
        for (int i = 0; i < layout.sleeves().size(); i++) {
            MazeLayout.Sleeve a = layout.sleeves().get(i), b = layout.sleeves().get(layout.sleeves().size() - 1 - i);
            Vec3 from = a.world(new Vec3(.37, .04, -.25));
            Vec3 to = MazeLayout.fold(a, b, from);
            helper.assertTrue(MazeLayout.fold(b, a, to).distanceToSqr(from) < 1E-12,
                    "walking back exactly inverts the rotated crossing");
            helper.assertTrue(MazeLayout.crosses(a, a.world(new Vec3(0, 0, 5)), a.world(new Vec3(0, 0, -5))),
                    "a sprint crossing is detected by its segment rather than landing on one block");
            helper.assertTrue(!MazeLayout.crosses(a, a.world(new Vec3(4, 0, 1)), a.world(new Vec3(4, 0, -1))),
                    "a parallel neighboring passage cannot trigger this seam");
            helper.assertTrue(!MazeLayout.crosses(a, a.world(new Vec3(0, 0, 20)), a.world(new Vec3(0, 0, -20))),
                    "a discontinuous command teleport is not treated as walking through the bend");
            Vec3 velocity = new Vec3(.08, -.12, -.3);
            helper.assertTrue(MazeLayout.rotate(MazeLayout.rotate(velocity, b.turn() - a.turn()), a.turn() - b.turn())
                    .distanceToSqr(velocity) < 1E-12, "inverse routes preserve stride and vertical velocity");
        }
        helper.assertTrue(layout.sleeves().getFirst().center().distManhattan(layout.sleeves().getLast().center()) == 60,
                "the deepest fold identifies corridors sixty blocks apart");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crossingCopiesMatchAndHideTheirDifferentEnds(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(700, 6, -180);
        MazeLayout layout = MazeLayout.create(LabyrinthPlace.ABYSS_MAZE, 747);
        LabyrinthMaze.build(helper.getLevel(), base, LabyrinthPlace.ABYSS_MAZE, layout);
        for (int i = 0; i < layout.sleeves().size() / 2; i++) {
            MazeLayout.Sleeve a = layout.sleeves().get(i), b = layout.sleeves().get(layout.sleeves().size() - 1 - i);
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) for (int y = -1; y <= 4; y++) {
                BlockPos p = new BlockPos(x, y, z);
                helper.assertTrue(helper.getLevel().getBlockState(base.offset(a.block(p)))
                        == helper.getLevel().getBlockState(base.offset(b.block(p))), "the crossing copies have identical geometry");
            }
            Vec3 eye = a.world(new Vec3(0, 1.62, 0)).add(Vec3.atLowerCornerOf(base));
            for (Vec3 tip : List.of(new Vec3(-6, 1.62, 4), new Vec3(6, 1.62, -4))) {
                Vec3 end = a.world(tip).add(Vec3.atLowerCornerOf(base));
                helper.assertTrue(helper.getLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, (Entity) null)).getType() == HitResult.Type.BLOCK,
                        "solid corners occlude the nonidentical connectors before the shift");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void routeDepthUnlocksBiggerMazesWithoutFinishingVignettes(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID(), other = UUID.randomUUID();
        for (int depth = 1; depth <= 9; depth++) {
            data.pushReturn(player, new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, Vec3.ZERO, 0));
            List<LabyrinthPlace> pool = LabyrinthDealer.grayAvailable(data, player);
            helper.assertTrue(pool.contains(LabyrinthPlace.FOLDED_MAZE) == (depth >= 3), "folded halls begin at three doors deep");
            helper.assertTrue(pool.contains(LabyrinthPlace.DEEP_MAZE) == (depth >= 6), "the second expansion begins at six");
            helper.assertTrue(pool.contains(LabyrinthPlace.ABYSS_MAZE) == (depth >= 9), "the largest expansion begins at nine");
        }
        helper.assertTrue(!LabyrinthDealer.grayAvailable(data, other).contains(LabyrinthPlace.FOLDED_MAZE), "another player's route depth stays separate");
        helper.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.ABYSS_MAZE, 12) > LabyrinthDealer.grayWeight(LabyrinthPlace.FOLDED_MAZE, 3),
                "deep gray dealings favor the larger impossible networks");
        for (int i = 0; i < 9; i++) data.popReturn(player);
        helper.assertTrue(!LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.FOLDED_MAZE), "backtracking lowers route intensity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mazeMigrationPreservesDropsAndPortableLight(GameTestHelper helper) {
        LabyrinthPlace place = LabyrinthPlace.GRAY_CORRIDOR;
        MazeLayout layout = MazeLayout.create(place, 747);
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(1000, 6, 170);
        BoundingBox box = place.room().moved(base.getX(), base.getY(), base.getZ());
        BlockPos old = base.offset(-12, 0, -12);
        helper.getLevel().setBlock(old, Blocks.SOUL_LANTERN.defaultBlockState(), 3);
        var stack = Items.DIAMOND_SWORD.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Still here")); stack.setDamageValue(47);
        ItemEntity item = new ItemEntity(helper.getLevel(), old.getX() + .5, old.getY(), old.getZ() + .5, stack);
        UUID owner = UUID.randomUUID(); item.getPersistentData().putUUID("MotherDropper", owner);
        helper.getLevel().addFreshEntity(item);
        var migration = LabyrinthMaze.capture(helper.getLevel(), box, base);
        LabyrinthMaze.build(helper.getLevel(), base, place, layout);
        LabyrinthMaze.restoreMigration(helper.getLevel(), base, place, migration);
        helper.assertTrue(!item.isRemoved() && item.getItem().getDamageValue() == 47
                && item.getItem().getHoverName().getString().equals("Still here") && item.getPersistentData().getUUID("MotherDropper").equals(owner),
                "recarving the corridor keeps the actual drop and owner data");
        helper.assertTrue(helper.getLevel().getBlockState(item.blockPosition()).getCollisionShape(helper.getLevel(), item.blockPosition()).isEmpty(),
                "an old drop covered by a new wall is moved onto accessible floor");
        long floorLights = layout.floor().stream().filter(p -> helper.getLevel().getBlockState(base.offset(p)).is(Blocks.SOUL_LANTERN)).count();
        helper.assertTrue(floorLights == 1, "a portable soul lantern is retained rather than confused with a ceiling fixture");
        item.discard(); helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vignetteLeaksMatchPersonalDealsAndPersist(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        var door = new LabyrinthData.Door("maze/north", HouseDimensions.INTERIOR, BlockPos.ZERO, Direction.SOUTH, LabyrinthData.DEALT, false);
        data.putDoor(door);
        data.deal(a, door, LabyrinthPlace.MODEL_HOME.id(), true, false);
        data.deal(b, door, LabyrinthPlace.HIDE_AND_CLAP.id(), true, true);
        helper.assertTrue(LabyrinthDoorLeaks.cue(data, a, door).kind() == DoorLeakKind.WARM_TV,
                "the warm domestic room gives television and light rather than a generic heartbeat");
        helper.assertTrue(LabyrinthDoorLeaks.cue(data, b, door).kind() == DoorLeakKind.CLOTH && LabyrinthDoorLeaks.cue(data, b, door).bark(),
                "another player sees the child's cloth and retains Hillary's cue at the same physical door");
        LabyrinthData restored = LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(), helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.assertTrue(LabyrinthDoorLeaks.cue(restored, a, restored.door(door.id)).kind() == DoorLeakKind.WARM_TV,
                "the persisted deal restores the matching sensory hint");
        data.deal(a, door, LabyrinthPlace.JUNCTION.id(), false, false);
        helper.assertTrue(LabyrinthDoorLeaks.cue(data, a, door) == null, "a redealt nonleaking gray door clears its former vignette cue");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyCurrentVignetteAndTheHotelHaveTheirOwnCue(GameTestHelper helper) {
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.FLOORBOARDS, 0) == DoorLeakKind.HEARTBEAT, "floorboard sounds belong to their room");
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.HARRIGAN, 0) == DoorLeakKind.PHONE, "the phone rings through its study door");
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.MOTHER_DEN, 0) == DoorLeakKind.MOTHER, "cloth and the strays announce the den");
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.RED_ROOM, 0) == DoorLeakKind.WARM_TV, "the copied domestic room stays warm");
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.HOTEL_HALLWAY, 0) == DoorLeakKind.HOTEL, "hotel carpet and music creep into the gray");
        helper.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.FLOODED_PASSAGE, 0) == DoorLeakKind.WATER, "water belongs to the submerged corridor");
        Set<DoorLeakKind> lies = new HashSet<>();
        for (int salt = 0; salt < 7; salt++) lies.add(DoorLeakKind.forDestination(LabyrinthPlace.GRAY_CORRIDOR, salt));
        helper.assertTrue(lies.size() == 7, "a leaking gray door can borrow a misleading cue");
        for (String sound : List.of("television", "phone", "hotel_music")) helper.assertTrue(TheOldestHouse.class.getResource(
                "/assets/the_oldest_house/sounds/leaks/" + sound + ".ogg") != null, "the muffled audio is actually packaged");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void doorHintSnapshotRoundTripsWithoutDestinationLabels(GameTestHelper helper) {
        DoorLeaksPayload original = new DoorLeaksPayload(ResourceLocation.fromNamespaceAndPath("the_oldest_house", "interior"), true,
                List.of(new DoorLeaksPayload.Leak(new BlockPos(2, 80, -30), Direction.EAST.get3DDataValue(), DoorLeakKind.WATER.ordinal())));
        var buffer = Unpooled.buffer();
        try {
            DoorLeaksPayload.STREAM_CODEC.encode(buffer, original);
            helper.assertTrue(original.equals(DoorLeaksPayload.STREAM_CODEC.decode(buffer)), "orientation, material and view restriction reach the client intact");
        } finally { buffer.release(); }
        helper.succeed();
    }
}
