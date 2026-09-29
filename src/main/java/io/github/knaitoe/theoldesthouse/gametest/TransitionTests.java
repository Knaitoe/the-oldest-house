package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom.Crossing;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom.Side;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The transition state machine's unhappy paths (a crossing is only ever
 * forgotten once it has completed or explicitly failed, and a failed one
 * leaves the player where they were with none of its hooks run), and the
 * room between rooms deciding by plane crossings where a player goes.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TransitionTests {
    private static final ResourceKey<Level> NOWHERE = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "nowhere"));

    private TransitionTests() {
    }

    private static ServerPlayer walker(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static void tick(ServerPlayer player) {
        HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(player));
    }

    @GameTest(template = "empty")
    public static void missingDestinationWaitsThenFailsWithoutMoving(GameTestHelper helper) {
        ServerPlayer player = walker(helper, "transition_nowhere");
        AtomicInteger hooks = new AtomicInteger();
        helper.assertTrue(HouseTransitionEvents.beginDoorTransition(player, NOWHERE, p -> hooks.incrementAndGet(), p -> hooks.incrementAndGet()),
                "the transition should be accepted");
        helper.assertTrue(!HouseTransitionEvents.beginDoorTransition(player, NOWHERE, null, null),
                "a second transition must not replace one in flight");

        // A missing level is waited for, not dropped on the first tick.
        for (int i = 0; i < 5; i++) {
            tick(player);
        }
        helper.assertTrue(HouseTransitionEvents.pendingPhase(player) == HouseTransitionEvents.Phase.PREPARED,
                "a transition whose destination is not there yet should keep waiting, not vanish");

        for (int i = 0; i < 40 && HouseTransitionEvents.isPending(player); i++) {
            tick(player);
        }
        helper.assertTrue(!HouseTransitionEvents.isPending(player), "a destination that never appears should fail the transition");
        helper.assertTrue(hooks.get() == 0, "a failed transition must run none of its hooks");
        helper.assertTrue(player.serverLevel() == helper.getLevel(), "a failed transition must leave the player where they were");
        helper.assertTrue(HouseTransitionEvents.beginDoorTransition(player, NOWHERE, null, null),
                "after a failure the player can cross again");
        HouseTransitionEvents.cancelPending(player, "test over");
        helper.assertTrue(!HouseTransitionEvents.isPending(player), "cancelling forgets the transition");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void transitionIsAbandonedIfThePlayerLeftByAnotherWay(GameTestHelper helper) {
        ServerLevel start = helper.getLevel();
        // The test server has only the vanilla dimensions; any other level will do.
        ServerLevel elsewhere = start.getServer().getLevel(Level.NETHER);
        helper.assertTrue(elsewhere != null && elsewhere != start, "another level should exist beside the test level");

        ServerPlayer player = walker(helper, "transition_elsewhere");
        AtomicInteger hooks = new AtomicInteger();
        HouseTransitionEvents.beginDoorTransition(player, HouseDimensions.BETWEEN, p -> hooks.incrementAndGet(), p -> hooks.incrementAndGet());

        // Before the transition's tick, something else (a death, a command)
        // has already put them somewhere else.
        player.setServerLevel(elsewhere);
        try {
            tick(player);
            helper.assertTrue(!HouseTransitionEvents.isPending(player), "a transition from a level the player has left should be abandoned");
            helper.assertTrue(hooks.get() == 0, "an abandoned transition must run none of its hooks");
            helper.assertTrue(player.serverLevel() == elsewhere, "an abandoned transition must not move the player again");
        } finally {
            player.setServerLevel(start);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disconnectingForgetsThePendingTransition(GameTestHelper helper) {
        ServerPlayer player = walker(helper, "transition_logout");
        HouseTransitionEvents.beginDoorTransition(player, NOWHERE, null, null);
        helper.assertTrue(HouseTransitionEvents.isPending(player), "the transition should be pending");
        HouseTransitionEvents.onPlayerLoggedOut(new PlayerEvent.PlayerLoggedOutEvent(player));
        helper.assertTrue(!HouseTransitionEvents.isPending(player), "logging out should drop the pending transition");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The room between rooms: plane crossings (pocket coordinates)

    private static final double DOOR_Z = HouseBetweenRoom.REAL_DOOR.getZ() + 0.5D;
    private static final double FLOOR = HouseBetweenRoom.ROOM_MIN_Y;

    @GameTest(template = "empty")
    public static void sidesteppingAtTheDoorIsNotBackingOut(GameTestHelper helper) {
        Crossing c = new Crossing(Side.HALL, 14.3D);
        helper.assertTrue(c.step(14.3D, FLOOR, DOOR_Z + 2.0D) == null, "strafing along the wall is not backing away");
        helper.assertTrue(c.step(14.3D, FLOOR, DOOR_Z - 2.0D) == null, "nor is strafing back the other way");
        helper.assertTrue(c.step(14.3D + HouseBetweenRoom.BACK_OUT_MARGIN * 0.5D, FLOOR, DOOR_Z) == null,
                "a half step back is not yet backing away");
        helper.assertTrue(c.step(14.3D + HouseBetweenRoom.BACK_OUT_MARGIN + 0.05D, FLOOR, DOOR_Z) == Side.HALL,
                "a full step back along the door's normal returns them to the hall");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void backingOutOfTheStudySideReturnsToTheStudy(GameTestHelper helper) {
        Crossing c = new Crossing(Side.STUDY, 5.7D);
        helper.assertTrue(c.step(5.7D, FLOOR, DOOR_Z) == null, "standing in the study doorway does nothing");
        helper.assertTrue(c.step(5.5D, FLOOR, DOOR_Z) == null, "just past the exit plane, a small step is not backing away");
        helper.assertTrue(c.step(4.9D, FLOOR, DOOR_Z) == Side.STUDY, "a full step back returns them to the study");
        helper.assertTrue(!c.hasBeenInside(), "they never went in");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void walkingThroughTheRoomIsATraversal(GameTestHelper helper) {
        Crossing c = new Crossing(Side.HALL, 13.4D);
        helper.assertTrue(c.step(13.9D, FLOOR, DOOR_Z) == null,
                "in the doorway, nothing happens: putting them back there would stand them in the real door");
        helper.assertTrue(c.step(12.5D, FLOOR, DOOR_Z) == null && c.hasBeenInside(), "crossing into the room marks them inside");
        helper.assertTrue(c.step(9.0D, FLOOR, DOOR_Z + 2.0D) == null, "walking about the room does nothing");
        helper.assertTrue(c.step(6.5D, FLOOR, DOOR_Z) == null, "in the far doorway, nothing yet");
        helper.assertTrue(c.step(5.5D, FLOOR, DOOR_Z) == Side.STUDY, "past the far exit plane they come out in the study");
        helper.assertTrue(c.cameFrom() == Side.HALL, "and they came from the hall: a traversal");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aFastCrossingCannotSkipThePlanes(GameTestHelper helper) {
        Crossing bounced = new Crossing(Side.HALL, 14.2D);
        helper.assertTrue(bounced.step(12.0D, FLOOR, DOOR_Z) == null, "in the room");
        helper.assertTrue(bounced.step(14.8D, FLOOR, DOOR_Z) == Side.HALL && bounced.hasBeenInside(),
                "knocked straight back out: returned to the hall, having been in");

        Crossing flung = new Crossing(Side.HALL, 14.2D);
        helper.assertTrue(flung.step(5.0D, FLOOR, DOOR_Z) == Side.STUDY && flung.hasBeenInside(),
                "a player carried through the whole room in one tick has still been through it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wanderingOffInACopyReturnsThem(GameTestHelper helper) {
        Crossing sideways = new Crossing(Side.HALL, 14.3D);
        helper.assertTrue(sideways.step(14.3D, FLOOR, DOOR_Z + HouseBetweenRoom.APPROACH_RADIUS + 0.5D) == Side.HALL,
                "walking far along the copied hall returns them");
        Crossing fell = new Crossing(Side.HALL, 14.3D);
        helper.assertTrue(fell.step(14.3D, FLOOR - 1.0D, DOOR_Z) == Side.HALL, "dropping off the copied floor returns them");
        helper.succeed();
    }
}
