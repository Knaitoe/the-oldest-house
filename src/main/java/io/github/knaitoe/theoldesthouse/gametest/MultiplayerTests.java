package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom.Side;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Several real players at once. Fake players are never ticked and cannot
 * change dimension, so the concurrency cases (two people at one door, one
 * backing out while another is inside, a disconnect mid-crossing) need real
 * ServerPlayers: GameTest mock players, on a connection that goes nowhere.
 * They run in a batch of their own, and every mock player is removed again,
 * after its test and, whatever happened, after the batch.
 *
 * The House's own dimensions do not exist on the test server, so crossings
 * go to the Nether (the transition machinery does not care where it goes),
 * and the room between rooms is built at an out-of-the-way spot in the test
 * level with its own House data, never the shared House.
 *
 * What a mock player is, as probed on this NeoForge: it is in the player list
 * and its level, and the server ticks it as an entity, but it never fires
 * PlayerTickEvent (that comes from the connection's tick, which a mock
 * connection does not get), so tests fire the mod's player tick themselves.
 * Its connection never negotiated the mod's channel, so any payload sent to
 * it straight through PacketDistributor throws; everything goes through
 * HousePackets, which skips it.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiplayerTests {
    private static final String BATCH = "multiplayer";
    private static final List<ServerPlayer> MOCKS = new ArrayList<>();

    private MultiplayerTests() {
    }

    private static ServerPlayer mock(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        MOCKS.add(player);
        return player;
    }

    private static void remove(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null && server.getPlayerList().getPlayers().contains(player)) {
            server.getPlayerList().remove(player);
        }
        MOCKS.remove(player);
    }

    @AfterBatch(batch = BATCH)
    public static void removeMockPlayers(ServerLevel level) {
        for (ServerPlayer player : new ArrayList<>(MOCKS)) {
            remove(player);
        }
    }

    /** One of the mod's player ticks, as the server would fire it. */
    private static void tick(ServerPlayer player) {
        HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(player));
    }

    private static boolean near(Vec3 at, double x, double y, double z) {
        return at.distanceToSqr(x, y, z) < 1.0E-4D;
    }

    // ------------------------------------------------------------------
    // Crossings

    @GameTest(template = "empty", batch = BATCH)
    public static void twoPlayersCrossAtTheSameTime(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        ServerPlayer a = mock(helper);
        ServerPlayer b = mock(helper);
        AtomicInteger aHooks = new AtomicInteger();
        AtomicInteger bHooks = new AtomicInteger();
        Vec3 aThere = new Vec3(0.5D, 100.0D, 0.5D);
        Vec3 bThere = new Vec3(4.5D, 100.0D, 0.5D);

        helper.assertTrue(HouseTransitionEvents.beginDoorTransition(a, Level.NETHER, p -> aHooks.incrementAndGet(),
                p -> aHooks.incrementAndGet(), aThere, 0.0F), "a's crossing should be accepted");
        helper.assertTrue(HouseTransitionEvents.beginDoorTransition(b, Level.NETHER, p -> bHooks.incrementAndGet(),
                p -> bHooks.incrementAndGet(), bThere, 90.0F), "b's crossing, in the same tick, should be accepted too");
        tick(a);
        tick(b);
        helper.assertTrue(a.serverLevel() == nether && b.serverLevel() == nether, "both should have crossed");
        helper.assertTrue(!HouseTransitionEvents.isPending(a) && !HouseTransitionEvents.isPending(b), "both crossings are over");
        helper.assertTrue(aHooks.get() == 2 && bHooks.get() == 2, "each crossing ran its own hooks, once each: a=" + aHooks + " b=" + bHooks);
        helper.assertTrue(near(a.position(), aThere.x, aThere.y, aThere.z) && near(b.position(), bThere.x, bThere.y, bThere.z),
                "each arrived at its own place: a=" + a.position() + " b=" + b.position());

        // And back, both at once, ticked the other way round.
        Vec3 home = Vec3.atBottomCenterOf(helper.absolutePos(BlockPos.ZERO).offset(700, 2, 0));
        HouseTransitionEvents.beginDoorTransition(a, Level.OVERWORLD, null, null, home, 0.0F);
        HouseTransitionEvents.beginDoorTransition(b, Level.OVERWORLD, null, null, home.add(2.0D, 0.0D, 0.0D), 0.0F);
        tick(b);
        tick(a);
        helper.assertTrue(a.serverLevel() == overworld && b.serverLevel() == overworld, "both should be back");
        remove(a);
        remove(b);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void aDisconnectMidCrossingLeavesTheOtherAlone(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        ServerPlayer gone = mock(helper);
        ServerPlayer stays = mock(helper);
        AtomicInteger goneHooks = new AtomicInteger();
        HouseTransitionEvents.beginDoorTransition(gone, Level.NETHER, p -> goneHooks.incrementAndGet(),
                p -> goneHooks.incrementAndGet(), new Vec3(8.5D, 100.0D, 0.5D), 0.0F);
        HouseTransitionEvents.beginDoorTransition(stays, Level.NETHER, null, null, new Vec3(12.5D, 100.0D, 0.5D), 0.0F);

        // Gone before the crossing's tick: the connection drops.
        remove(gone);
        boolean forgottenAtLogout = !HouseTransitionEvents.isPending(gone);
        tick(gone);
        tick(stays);
        helper.assertTrue(!HouseTransitionEvents.isPending(gone), "a departed player's crossing is forgotten (at logout: " + forgottenAtLogout + ")");
        helper.assertTrue(goneHooks.get() == 0, "and none of its hooks ran");
        helper.assertTrue(gone.serverLevel() == overworld, "and they were not moved");
        helper.assertTrue(stays.serverLevel() == nether && !HouseTransitionEvents.isPending(stays), "the other player crossed regardless");
        remove(stays);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The room between rooms, with two people in it

    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 200)
    public static void backingOutNeverShutsTheRoomOnSomeoneElse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(700, 2, -400);
        HouseSavedData data = new HouseSavedData();
        data.markSpawned(origin);
        data.markRoomArmed();
        BlockPos pocket = HouseBetweenRoom.pocketOrigin(origin);
        int dy = HouseBetweenRoom.pocketDy(origin);
        double floor = origin.getY() + HouseBetweenRoom.REAL_DOOR.getY();
        double doorZ = origin.getZ() + HouseBetweenRoom.REAL_DOOR.getZ() + 0.5D;

        ServerPlayer inside = mock(helper);
        ServerPlayer backsOut = mock(helper);
        // Both in the real hall, just east of the study door.
        inside.teleportTo(origin.getX() + 14.5D, floor, doorZ - 0.3D);
        backsOut.teleportTo(origin.getX() + 14.6D, floor, doorZ + 0.3D);

        HouseBetweenRoom.enter(inside, level, data, origin, Side.HALL);
        helper.assertTrue(Math.abs(inside.getY() - (floor + dy)) < 1.0E-4D, "entering shifts them up into the pocket");
        inside.teleportTo(pocket.getX() + 10.5D, inside.getY(), doorZ);
        helper.assertTrue(HouseBetweenRoom.tickPocket(inside, data, origin), "they are in the room");

        HouseBetweenRoom.enter(backsOut, level, data, origin, Side.HALL);
        backsOut.teleportTo(pocket.getX() + 15.5D, backsOut.getY(), backsOut.getZ());
        HouseBetweenRoom.tickPocket(backsOut, data, origin);
        helper.assertTrue(Math.abs(backsOut.getY() - floor) < 1.0E-4D && Math.abs(backsOut.getX() - (origin.getX() + 15.5D)) < 1.0E-4D,
                "the one who stepped back is in the real hall, where they stepped back to: " + backsOut.position());
        helper.assertTrue(level.getBlockState(HouseBetweenRoom.hallDoor(origin)).getValue(DoorBlock.OPEN),
                "stepping back out must not shut the hall door on the one still in the room");

        inside.teleportTo(pocket.getX() + 6.5D, inside.getY(), doorZ);
        HouseBetweenRoom.tickPocket(inside, data, origin);
        helper.assertTrue(Math.abs(inside.getY() - (floor + dy)) < 1.0E-4D, "in the far doorway they are still in the pocket");
        inside.teleportTo(pocket.getX() + 5.4D, inside.getY(), doorZ);
        HouseBetweenRoom.tickPocket(inside, data, origin);
        helper.assertTrue(Math.abs(inside.getY() - floor) < 1.0E-4D
                        && Math.abs(inside.getX() - (origin.getX() + 5.4D - HouseBetweenRoom.STUDY_SHIFT)) < 1.0E-4D,
                "out the far side they are in the real study, just inside its door: " + inside.position());
        helper.assertTrue(data.isRoomTraversed(), "and that was the room walked through");
        helper.assertTrue(!level.getBlockState(HouseBetweenRoom.hallDoor(origin)).getValue(DoorBlock.OPEN)
                        && !level.getBlockState(HouseBetweenRoom.studyDoor(origin)).getValue(DoorBlock.OPEN),
                "the last one out leaves both of the room's doors shut");
        remove(inside);
        remove(backsOut);
        helper.succeed();
    }
}
