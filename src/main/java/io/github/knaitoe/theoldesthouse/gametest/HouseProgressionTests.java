package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseProgression;
import io.github.knaitoe.theoldesthouse.house.HouseRugs;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The House after the first visit: the mod's day count, the rugs, the room
 * between rooms and when the hallway follows.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HouseProgressionTests {
    private HouseProgressionTests() {
    }

    // ------------------------------------------------------------------
    // Days

    @GameTest(template = "empty")
    public static void calendarCountsDawnsWhateverTheClockDoes(GameTestHelper helper) {
        helper.assertTrue(HouseCalendar.dawnsBetween(13000L, 24000L) == 1L, "sleeping through the night is one dawn");
        helper.assertTrue(HouseCalendar.dawnsBetween(1000L, 13000L) == 0L, "/time set night is not a dawn");
        helper.assertTrue(HouseCalendar.dawnsBetween(13000L, 1000L) == 1L, "/time set day after dark is a dawn");
        helper.assertTrue(HouseCalendar.dawnsBetween(50000L, 13000L) == 0L,
                "setting the clock back to an evening is not a dawn (and never counts days backwards)");
        helper.assertTrue(HouseCalendar.dawnsBetween(0L, 240000L) == 10L, "/time add counts every day it skips");
        helper.assertTrue(HouseCalendar.dawnsBetween(23990L, 24010L) == 1L, "the ordinary passing of midnight into day");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The rugs

    @GameTest(template = "empty")
    public static void rugsTradeColoursButPlayerCarpetStays(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Well away from other tests' blocks.
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(160, 4, 0);
        for (HouseRugs.Rug rug : HouseRugs.SHIFTING) {
            for (int x = rug.x0(); x <= rug.x1(); x++) {
                for (int z = rug.z0(); z <= rug.z1(); z++) {
                    level.setBlock(origin.offset(x, rug.y(), z), rug.authored().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        HouseRugs.Rug bedroom = HouseRugs.PRINCIPAL_BEDROOM;
        BlockPos players = origin.offset(bedroom.x0(), bedroom.y(), bedroom.z0());
        level.setBlock(players, Blocks.YELLOW_CARPET.defaultBlockState(), Block.UPDATE_CLIENTS);

        int changed = HouseRugs.shift(level, origin);

        helper.assertTrue(level.getBlockState(players).is(Blocks.YELLOW_CARPET), "a player's carpet should stay as it is");
        for (HouseRugs.Rug rug : HouseRugs.SHIFTING) {
            helper.assertTrue(rug.authored() != rug.shifted(), rug.room() + " should change colour");
            BlockPos far = origin.offset(rug.x1(), rug.y(), rug.z1());
            helper.assertTrue(level.getBlockState(far).is(rug.shifted()),
                    rug.room() + " should now be " + rug.shifted() + ", was " + level.getBlockState(far));
        }
        helper.assertTrue(HouseRugs.GREAT_ROOM.shifted() == HouseRugs.PRINCIPAL_BEDROOM.authored()
                        && HouseRugs.PRINCIPAL_BEDROOM.shifted() == HouseRugs.GREAT_ROOM.authored(),
                "the great room and bedroom rugs trade colours");
        helper.assertTrue(changed > 0, "some carpet should have changed");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Timing

    @GameTest(template = "empty")
    public static void roomChanceRisesAndTheHallwayFollows(GameTestHelper helper) {
        HouseSavedData data = new HouseSavedData();
        data.markSpawned(new BlockPos(100_000, 64, 100_000));

        helper.assertTrue(HouseProgression.roomChance(data, 5) == -1, "no room before anyone has visited");
        helper.assertTrue(!HouseProgression.isRugShiftDue(data), "no rugs before anyone has visited");

        data.incrementVisitCount();
        data.setHouseAge(1);
        helper.assertTrue(HouseProgression.isRugShiftDue(data) == (1 >= HouseConfig.RUGS_FALLBACK_AGE.getAsInt()),
                "without a night in the manor, the rugs wait for the fallback age");
        data.markSleptInManor();
        helper.assertTrue(HouseProgression.isRugShiftDue(data), "a night in the manor shifts the rugs");
        helper.assertTrue(HouseProgression.roomChance(data, 5) == -1, "no room before the rugs");

        data.markRugsShifted();
        int wait = HouseConfig.ROOM_MIN_MORNINGS_AFTER_RUGS.getAsInt();
        int base = HouseConfig.ROOM_BASE_CHANCE.getAsInt();
        int step = HouseConfig.ROOM_CHANCE_STEP.getAsInt();
        if (wait > 0) {
            helper.assertTrue(HouseProgression.roomChance(data, 1 + wait - 1) == -1, "the room waits after the rugs");
        }
        helper.assertTrue(HouseProgression.roomChance(data, 1 + wait) == Math.min(100, base),
                "first eligible morning: the base chance");
        data.noteRoomMissedMorning();
        helper.assertTrue(HouseProgression.roomChance(data, 2 + wait) == Math.min(100, base + step),
                "each missed morning raises the chance");
        for (int i = 0; i < 10; i++) {
            data.noteRoomMissedMorning();
        }
        helper.assertTrue(HouseProgression.roomChance(data, 12 + wait) == 100 || step == 0, "the chance caps at 100%");

        data.setHouseAge(3);
        data.markRoomRevealed(9);
        helper.assertTrue(HouseProgression.roomChance(data, 4) == -1, "only one room");
        int hallway = HouseConfig.HALLWAY_MORNINGS_AFTER_ROOM.getAsInt();
        helper.assertTrue(HouseProgression.isHallwayDue(data) == (hallway == 0), "the hallway waits after the room");
        data.setHouseAge(3 + hallway);
        helper.assertTrue(HouseProgression.isHallwayDue(data), "then the hallway opens");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The room between rooms

    @GameTest(template = "empty")
    public static void doorGoesWhereThereIsRoomInFrontAndShelvesBehind(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(160, -2, 40);
        int z = HouseBetweenRoom.WALL_Z;
        for (int x = 3; x <= 12; x++) {
            for (int y = 7; y <= 10; y++) {
                level.setBlock(origin.offset(x, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(origin.offset(x, y, z - 1), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(origin.offset(x, y, z + 1), Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        // Something stands in front of the first choice.
        level.setBlock(origin.offset(9, 7, z - 1), Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);

        int doorX = HouseBetweenRoom.chooseDoorX(level, origin);
        helper.assertTrue(doorX == 10, "the door should move along to x=10, was " + doorX);

        HouseBetweenRoom.placeDoor(level, origin, doorX);
        BlockState lower = level.getBlockState(HouseBetweenRoom.doorPos(origin, doorX));
        BlockState upper = level.getBlockState(HouseBetweenRoom.doorPos(origin, doorX).above());
        helper.assertTrue(lower.getBlock() instanceof DoorBlock && lower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                        && !lower.getValue(DoorBlock.OPEN), "a closed door's lower half in the partition");
        helper.assertTrue(upper.getBlock() instanceof DoorBlock && upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER,
                "and its upper half");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void roomBetweenRoomsIsEnclosedAndFurnished(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(160, -2, 80);
        int doorX = 9;
        HouseBetweenRoom.buildRoom(level, origin, doorX);

        int z0 = HouseBetweenRoom.ROOM_MIN_Z;
        int z1 = HouseBetweenRoom.ROOM_MAX_Z;
        helper.assertTrue(level.getBlockState(origin.offset(doorX, 6, z0 + 3)).isSolid(), "a floor");
        helper.assertTrue(level.getBlockState(origin.offset(doorX, 11, z0 + 3)).isSolid(), "a ceiling");
        helper.assertTrue(level.getBlockState(origin.offset(doorX - 4, 8, z0 + 3)).isSolid(), "a west wall");
        helper.assertTrue(level.getBlockState(origin.offset(doorX + 4, 8, z0 + 3)).isSolid(), "an east wall");
        helper.assertTrue(level.getBlockState(origin.offset(doorX, 8, z1 + 1)).isSolid(), "a back wall");
        helper.assertTrue(level.getBlockState(origin.offset(doorX, 8, z0)).isAir(), "headroom just inside the door");
        helper.assertTrue(level.getBlockState(origin.offset(doorX, 7, z0 + 4)).is(Blocks.RED_CARPET), "the bedroom's old red rug");

        helper.assertTrue(HouseBetweenRoom.isProtectedBetweenPosition(origin, doorX, origin.offset(doorX + 4, 8, z0 + 3)),
                "the room's walls cannot be broken");
        helper.assertTrue(!HouseBetweenRoom.isProtectedBetweenPosition(origin, doorX, origin.offset(doorX, 7, z0 + 4)),
                "its furnishings can");
        helper.assertTrue(HouseBetweenRoom.isProtectedBetweenPosition(origin, doorX, origin.offset(5, 7, 4)),
                "nor can anything in the copied bedroom");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theRoomLiesPastTheThreshold(GameTestHelper helper) {
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(helper.getLevel().getServer(), HouseDimensions.BETWEEN, BlockPos.ZERO),
                "beds do not work in the room between rooms");
        helper.succeed();
    }
}
