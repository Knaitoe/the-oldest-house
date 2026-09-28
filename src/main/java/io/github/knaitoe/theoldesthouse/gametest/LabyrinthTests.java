package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The labyrinth's rooms, its dealer, and the floorboards' rules. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LabyrinthTests {
    private LabyrinthTests() {
    }

    @GameTest(template = "empty")
    public static void floorboardsRoomHasItsBoardSculkAndDoor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 0);
        TellTaleFloorboards.build(level, base, true);

        helper.assertTrue(level.getBlockState(base.offset(TellTaleFloorboards.LOOSE_BOARD)).is(Blocks.DARK_OAK_PLANKS),
                "the loose board is down");
        helper.assertTrue(level.getBlockState(base.offset(0, -1, 2)).is(Blocks.SPRUCE_PLANKS), "the rest is spruce boards");
        helper.assertTrue(level.getBlockState(base.offset(1, -2, 1)).is(Blocks.SCULK), "sculk lies under the boards");
        helper.assertTrue(level.getBlockState(base.offset(0, 1, 4)).isAir(), "there is room to stand");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, -1)).getBlock() instanceof DoorBlock, "a door in the south wall");
        helper.assertTrue(level.getBlockState(base.offset(0, 1, -1)).getBlock() instanceof DoorBlock, "both halves of it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void junctionHasADoorInEachWall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 60);
        LabyrinthBuilder.buildJunction(level, base);
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
            helper.assertTrue(level.getBlockState(base.offset(spec.rel())).getBlock() instanceof DoorBlock,
                    "no door for " + spec.name());
            helper.assertTrue(level.getBlockState(base.offset(spec.rel()).relative(spec.facing())).isAir(),
                    "no room in front of the " + spec.name() + " door");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dealerGuaranteesAVignetteAfterADrySpellAndNeverRepeatsAFinishedOne(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION);
        RandomSource random = RandomSource.create(99);

        boolean dealt = false;
        for (int dealing = 0; dealing < 4 && !dealt; dealing++) {
            LabyrinthDealer.dealPlace(data, LabyrinthPlace.JUNCTION, random);
            int vignettes = 0;
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                if (LabyrinthData.DEALT.equals(door.destination)) {
                    helper.assertTrue(door.dealt != null && !door.dealt.equals(LabyrinthPlace.JUNCTION.id()),
                            "a junction door was dealt back to the junction, or not at all");
                    if (TellTaleFloorboards.ID.equals(door.dealt)) {
                        vignettes++;
                        helper.assertTrue(door.leak, "a vignette door should leak");
                    }
                }
            }
            helper.assertTrue(vignettes <= 1, "more than one vignette door in a dealing");
            dealt = vignettes == 1;
        }
        helper.assertTrue(dealt, "four dealings passed without a vignette door");
        helper.assertTrue(data.dryDeals() == 0, "the dry spell resets");

        data.setCompleted(TellTaleFloorboards.ID, true);
        helper.assertTrue(LabyrinthDealer.vignetteChance(data) == 0, "nothing left to deal");
        for (int dealing = 0; dealing < 6; dealing++) {
            LabyrinthDealer.dealPlace(data, LabyrinthPlace.JUNCTION, random);
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                helper.assertTrue(!TellTaleFloorboards.ID.equals(door.dealt), "a finished one-shot was dealt again");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sneakingMakesNoVibrationsButDoorsDo(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "floorboards_walker"));
        player.setShiftKeyDown(false);
        helper.assertTrue(!TellTaleFloorboards.isSilent(player, GameEvent.STEP), "footsteps are heard");
        player.setShiftKeyDown(true);
        helper.assertTrue(TellTaleFloorboards.isSilent(player, GameEvent.STEP), "crouched steps are not");
        helper.assertTrue(!TellTaleFloorboards.isSilent(player, GameEvent.BLOCK_OPEN), "a door is heard, crouching or not");
        player.setShiftKeyDown(false);
        helper.assertTrue(TellTaleFloorboards.weight(GameEvent.HIT_GROUND) > TellTaleFloorboards.weight(GameEvent.STEP),
                "landing from a jump is louder than a step");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void backDoorsLeadBackTheWayYouCame(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();
        LabyrinthData.Waypoint first = new LabyrinthData.Waypoint(Level.OVERWORLD, new Vec3(1, 64, 1), 0.0F);
        LabyrinthData.Waypoint second = new LabyrinthData.Waypoint(HouseDimensions.LABYRINTH, new Vec3(0.5, 64, 1.5), 0.0F);
        data.pushReturn(player, first);
        data.pushReturn(player, second);
        helper.assertTrue(second.equals(data.popReturn(player)), "the last door first");
        helper.assertTrue(first.equals(data.popReturn(player)), "then the one before");
        helper.assertTrue(data.popReturn(player) == null, "then nowhere remembered");
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(helper.getLevel().getServer(), HouseDimensions.LABYRINTH, BlockPos.ZERO),
                "beds do not work in the labyrinth");
        helper.succeed();
    }
}
