package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseMemory;
import io.github.knaitoe.theoldesthouse.house.HouseShiftEffects;
import io.github.knaitoe.theoldesthouse.house.HouseShifts;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import io.github.knaitoe.theoldesthouse.house.HouseProgression;
import io.github.knaitoe.theoldesthouse.house.HouseRugs;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
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
                    // Carpet needs something under it, or its neighbours' updates pop it off.
                    level.setBlock(origin.offset(x, rug.y() - 1, z), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
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

        int shiftBase = HouseConfig.SHIFT_BASE_CHANCE.getAsInt();
        int shiftStep = HouseConfig.SHIFT_CHANCE_STEP.getAsInt();
        helper.assertTrue(HouseShifts.chance(data) == Math.min(100, shiftBase), "subtle changes start after the rugs");
        data.noteShiftDryMorning();
        helper.assertTrue(HouseShifts.chance(data) == Math.min(100, shiftBase + shiftStep), "quiet mornings raise the chance");

        int needed = HouseConfig.SHIFTS_BEFORE_HALLWAY.getAsInt();
        for (int i = 0; i < needed; i++) {
            helper.assertTrue(HouseProgression.hallwayChance(data) == -1, "the hallway waits for subtle changes (" + i + " so far)");
            data.recordShift("candles");
        }
        helper.assertTrue(HouseShifts.chance(data) == Math.min(100, shiftBase), "a change resets the quiet mornings");
        int hallBase = HouseConfig.HALLWAY_BASE_CHANCE.getAsInt();
        int hallStep = HouseConfig.HALLWAY_CHANCE_STEP.getAsInt();
        helper.assertTrue(HouseProgression.hallwayChance(data) == Math.min(100, hallBase),
                "after enough changes, the hallway's heavier roll begins");
        data.noteHallwayMissedMorning();
        helper.assertTrue(HouseProgression.hallwayChance(data) == Math.min(100, hallBase + hallStep),
                "and rises each morning it stays shut");

        data.markImpossibleDoorRevealed();
        helper.assertTrue(HouseProgression.hallwayChance(data) == -1, "only one hallway");
        helper.assertTrue(HouseShifts.chance(data) == Math.min(100, HouseConfig.SHIFT_BASE_CHANCE_AFTER_HALLWAY.getAsInt()),
                "changes carry on after the hallway, at their own rate");

        data.recordShift(HouseShifts.Shift.DEEPER_HALL.id());
        helper.assertTrue(!HouseShifts.weightedOrder(data, RandomSource.create(7)).contains(HouseShifts.Shift.DEEPER_HALL),
                "the hall only deepens once");
        helper.assertTrue(HouseShifts.weightedOrder(data, RandomSource.create(7)).contains(HouseShifts.Shift.CANDLES),
                "candles can happen again");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Subtle changes, on hand-built furniture at the right house positions

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void subtleChangesRearrangeWhatNobodyIsWatching(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Far from every other test: the effects search the whole house envelope.
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(420, 4, 420);
        HouseSavedData data = new HouseSavedData();
        data.markSpawned(origin);
        HouseShiftEffects.Context ctx = new HouseShiftEffects.Context(null, data, level, level, origin, RandomSource.create(42));
        int keep = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

        // Candles.
        level.setBlock(origin.offset(5, 1, 5), Blocks.STONE.defaultBlockState(), keep);
        level.setBlock(origin.offset(5, 2, 5), Blocks.CANDLE.defaultBlockState(), keep);
        helper.assertTrue(HouseShiftEffects.candles(ctx) != null, "a candle should light");
        helper.assertTrue(level.getBlockState(origin.offset(5, 2, 5)).getValue(AbstractCandleBlock.LIT), "the candle is lit");

        // A chair at the great-room table turns towards the front door.
        level.setBlock(origin.offset(4, 1, 5), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM), keep);
        helper.assertTrue(HouseShiftEffects.chairs(ctx) != null, "a chair should turn");
        helper.assertTrue(level.getBlockState(origin.offset(4, 1, 5)).getValue(StairBlock.FACING) == Direction.WEST,
                "it turns its back on the front door, to face it");

        // A door changes hinge.
        level.setBlock(origin.offset(13, 0, 20), Blocks.STONE.defaultBlockState(), keep);
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HINGE, DoorHingeSide.LEFT);
        level.setBlock(origin.offset(13, 1, 20), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), keep);
        level.setBlock(origin.offset(13, 2, 20), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), keep);
        helper.assertTrue(HouseShiftEffects.doors(ctx) != null, "a door should change");
        helper.assertTrue(level.getBlockState(origin.offset(13, 1, 20)).getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT
                        && level.getBlockState(origin.offset(13, 2, 20)).getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT,
                "both halves hang from the other side");

        // A note on the literary bedroom's shelf.
        level.setBlock(origin.offset(1, 7, 13), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), keep);
        helper.assertTrue(HouseShiftEffects.notes(ctx) != null, "a note should appear");
        helper.assertTrue(level.getBlockEntity(origin.offset(1, 7, 13)) instanceof ChiseledBookShelfBlockEntity shelf
                && shelf.getItem(0).is(Items.WRITTEN_BOOK), "the note is on the shelf");
        helper.assertTrue(data.isNoteWritten(0), "the first note is used up");

        // The guest bed in the loft changes colour.
        level.setBlock(origin.offset(24, 6, 10), Blocks.STONE.defaultBlockState(), keep);
        level.setBlock(origin.offset(25, 6, 10), Blocks.STONE.defaultBlockState(), keep);
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(origin.offset(24, 7, 10), bed.setValue(BedBlock.PART, BedPart.FOOT), keep);
        level.setBlock(origin.offset(25, 7, 10), bed.setValue(BedBlock.PART, BedPart.HEAD), keep);
        helper.assertTrue(HouseShiftEffects.guestBed(ctx) != null, "a bed should change colour");
        helper.assertTrue(level.getBlockState(origin.offset(24, 7, 10)).is(Blocks.BROWN_BED)
                        && level.getBlockState(origin.offset(25, 7, 10)).is(Blocks.BROWN_BED),
                "both halves of the red bed are now brown");

        // The end of the hall moves back a block.
        int z = HouseLayout.THRESHOLD_Z;
        for (int x = HouseLayout.HALL_MIN_X - 1; x <= HouseLayout.HALL_MAX_X + 1; x++) {
            for (int y = 0; y <= 6; y++) {
                level.setBlock(origin.offset(x, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), keep);
            }
        }
        for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
            level.setBlock(origin.offset(x, 0, z - 1), Blocks.SPRUCE_PLANKS.defaultBlockState(), keep);
        }
        level.setBlock(origin.offset(HouseLayout.AXIS_X, 1, z - 1), Blocks.RED_CARPET.defaultBlockState(), keep);
        helper.assertTrue(HouseShiftEffects.deeperHall(ctx) != null, "the hall should deepen");
        helper.assertTrue(level.getBlockState(origin.offset(HouseLayout.AXIS_X, 2, z + 1)).is(Blocks.WHITE_TERRACOTTA),
                "the end wall is a block further back");
        helper.assertTrue(level.getBlockState(origin.offset(HouseLayout.AXIS_X, 1, z)).is(Blocks.RED_CARPET)
                        && level.getBlockState(origin.offset(HouseLayout.AXIS_X, 2, z)).isAir(),
                "and the runner carries on into the extra block");
        helper.assertTrue(HouseShiftEffects.deeperHall(ctx) == null, "only once");

        // Two paintings of a size trade walls, or one moves along.
        level.setBlock(origin.offset(2, 2, 3), Blocks.STONE.defaultBlockState(), keep);
        level.setBlock(origin.offset(6, 2, 3), Blocks.STONE.defaultBlockState(), keep);
        for (int x = 1; x <= 8; x++) {
            level.setBlock(origin.offset(x, 3, 2), Blocks.STONE.defaultBlockState(), keep);
        }
        var variants = level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        level.addFreshEntity(new Painting(level, origin.offset(3, 3, 3), Direction.SOUTH, variants.getHolderOrThrow(PaintingVariants.KEBAB)));
        level.addFreshEntity(new Painting(level, origin.offset(6, 3, 3), Direction.SOUTH, variants.getHolderOrThrow(PaintingVariants.PLANT)));
        helper.assertTrue(HouseShiftEffects.paintings(ctx) != null, "a painting should change");

        // An upper window lit from outside.
        helper.assertTrue(HouseShiftEffects.window(ctx) != null, "a window should be lit");
        BlockPos light = data.windowLight();
        helper.assertTrue(light != null && level.getBlockState(light).is(Blocks.LIGHT), "an invisible light sits behind it");
        HouseShifts.refreshCache(HouseSavedData.get(level.getServer()));

        // An echo waits for someone across the house.
        helper.assertTrue(HouseShiftEffects.echoes(ctx) != null && data.isEchoPending(), "an echo is pending");
        helper.assertTrue(HouseShiftEffects.echoes(ctx) == null, "one at a time");

        // The house puts back one common thing that was taken, never a tool.
        BlockPos chestPos = origin.offset(10, 1, 9);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), keep);
        if (!(level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) {
            helper.fail("no chest block entity");
            return;
        }
        chest.setItem(0, new ItemStack(Items.BREAD, 3));
        chest.setItem(1, new ItemStack(Items.IRON_SWORD));
        HouseMemory memory = new HouseMemory();
        memory.remember(chestPos, chest);
        chest.setItem(0, ItemStack.EMPTY);
        chest.setItem(1, ItemStack.EMPTY);
        helper.assertTrue(chestPos.equals(memory.restoreOne(level, RandomSource.create(3))), "something comes back");
        helper.assertTrue(chest.getItem(0).is(Items.BREAD) && chest.getItem(0).getCount() == 3, "the bread is back");
        helper.assertTrue(chest.getItem(1).isEmpty(), "the sword is not");
        helper.assertTrue(memory.restoreOne(level, RandomSource.create(3)) == null, "and never the same slot twice");
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

        helper.assertTrue(HouseBetweenRoom.isProtectedPocketPosition(origin, doorX, origin.offset(doorX + 4, 8, z0 + 3)),
                "the room's walls cannot be broken");
        helper.assertTrue(!HouseBetweenRoom.isProtectedPocketPosition(origin, doorX, origin.offset(doorX, 7, z0 + 4)),
                "its furnishings can");
        helper.assertTrue(HouseBetweenRoom.isProtectedPocketPosition(origin, doorX, origin.offset(5, 7, 4)),
                "nor can anything in the copied bedroom");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyRoomLootTableLoads(GameTestHelper helper) {
        for (String name : new String[]{"kitchen", "scullery", "parlour", "study", "basement", "bedroom", "literary", "workshop", "attic"}) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "chests/" + name));
            helper.assertTrue(helper.getLevel().getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY,
                    "loot table chests/" + name + " should load");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theRoomLiesPastTheThreshold(GameTestHelper helper) {
        BlockPos origin = new BlockPos(100_000, 64, 100_000);
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(origin, HouseBetweenRoom.pocketOrigin(origin).offset(9, 7, 14)),
                "beds do not work in the room between rooms");
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(origin, HouseBetweenRoom.pocketOrigin(origin).offset(5, 7, 4)),
                "nor in the copy of the bedroom in front of it");
        helper.assertTrue(!HouseLabyrinth.isBeyondThreshold(origin, origin.offset(5, 7, 4)),
                "but they do in the real bedroom");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void copyingTheBedroomNeverDoublesItsPaintings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Right by the test, where its chunks (and their entities) stay loaded.
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(2, -2, 2);
        int dy = HouseBetweenRoom.pocketDy(origin);
        level.setBlock(origin.offset(5, 8, 9), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        var variants = level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        level.addFreshEntity(new Painting(level, origin.offset(5, 8, 8), Direction.NORTH, variants.getHolderOrThrow(PaintingVariants.KEBAB)));
        AABB pocket = new AABB(origin.offset(0, dy, -2).getCenter(), origin.offset(14, dy + 12, 10).getCenter()).inflate(1.0D);
        long start = level.getGameTime();

        // Copy again every tick, as if someone kept going through the door,
        // and wait long enough for any doubled painting to pop off its wall.
        helper.succeedWhen(() -> {
            HouseBetweenRoom.copyBedroom(level, origin);
            int copies = level.getEntitiesOfClass(Painting.class, pocket).size();
            helper.assertTrue(copies == 1, "the copy should hang exactly one painting, had " + copies);
            helper.assertTrue(level.getBlockState(origin.offset(5, 8 + dy, 9)).is(Blocks.STONE), "its wall is copied too");
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, pocket.inflate(4.0D)).isEmpty(),
                    "nothing should have dropped off the copied walls");
            helper.assertTrue(level.getGameTime() - start >= 120, "still watching for drops");
        });
    }
}
