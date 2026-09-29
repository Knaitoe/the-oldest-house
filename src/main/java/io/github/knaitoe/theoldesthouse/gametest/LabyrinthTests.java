package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.labyrinth.CrayonDrawing;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.HomeRooms;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLoops;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.RedRoom;
import io.github.knaitoe.theoldesthouse.labyrinth.RoomSnapshot;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import io.github.knaitoe.theoldesthouse.labyrinth.VignetteYields;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The labyrinth's rooms, its dealer, its doors' shifts, and the floorboards' rules. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LabyrinthTests {
    private LabyrinthTests() {
    }

    @GameTest(template = "empty")
    public static void floorboardsRoomHasItsBoardSculkAndDoor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 20);
        TellTaleFloorboards.build(level, base, true);

        helper.assertTrue(level.getBlockState(base.offset(TellTaleFloorboards.LOOSE_BOARD)).is(Blocks.DARK_OAK_PLANKS),
                "the loose board is down");
        helper.assertTrue(level.getBlockState(base.offset(0, -1, -2)).is(Blocks.SPRUCE_PLANKS), "the rest is spruce boards");
        helper.assertTrue(level.getBlockState(base.offset(1, -2, -1)).is(Blocks.SCULK), "sculk lies under the boards");
        helper.assertTrue(level.getBlockState(base.offset(0, 1, -4)).isAir(), "there is room to stand");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, 1)).getBlock() instanceof DoorBlock, "a door in the south wall");
        helper.assertTrue(level.getBlockState(base.offset(0, 1, 1)).getBlock() instanceof DoorBlock, "both halves of it");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, 0)).isAir(), "an opening through the room's own wall");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, 2)).isAir(), "and a vestibule behind the door");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void junctionHasADoorInEachWall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 90);
        LabyrinthBuilder.buildJunction(level, base);
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
            helper.assertTrue(level.getBlockState(base.offset(spec.rel())).getBlock() instanceof DoorBlock,
                    "no door for " + spec.name());
            helper.assertTrue(level.getBlockState(base.offset(spec.rel()).relative(spec.facing())).isAir(),
                    "no room to approach the " + spec.name() + " door");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dealerGuaranteesAVignetteAfterADrySpellAndNeverRepeatsAFinishedOne(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
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
                    LabyrinthPlace dealtPlace = LabyrinthPlace.byId(door.dealt);
                    if (dealtPlace != null && dealtPlace.isVignette()) {
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

        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isOneShot()) {
                data.setCompleted(place.id(), true);
            }
        }
        helper.assertTrue(LabyrinthDealer.vignetteChance(data) == 0, "nothing left to deal while the Red Room has no room to copy");
        data.setReady(RedRoom.ID, true);
        helper.assertTrue(LabyrinthDealer.vignetteChance(data) > 0, "the Red Room keeps being dealt once it has one");
        data.setReady(RedRoom.ID, false);
        for (int dealing = 0; dealing < 6; dealing++) {
            LabyrinthDealer.dealPlace(data, LabyrinthPlace.JUNCTION, random);
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                LabyrinthPlace dealtPlace = LabyrinthPlace.byId(door.dealt);
                helper.assertTrue(dealtPlace == null || !dealtPlace.isOneShot(), "a finished one-shot was dealt again");
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
        LabyrinthData.Waypoint first = new LabyrinthData.Waypoint(Level.OVERWORLD, new Vec3(1, 64, 1), 0.0F, true);
        LabyrinthData.Waypoint second = new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, new Vec3(0.5, 64, 1.5), 0.0F);
        data.pushReturn(player, first);
        data.pushReturn(player, second);
        helper.assertTrue(second.equals(data.popReturn(player)), "the last door first");
        helper.assertTrue(first.equals(data.popReturn(player)), "then the one before");
        helper.assertTrue(data.popReturn(player) == null, "then nowhere remembered");

        // The hallway is the graph's recovery root. A lost/stale return stack
        // must still have an unambiguous way back into the House proper.
        BlockPos hallwayDoor = new BlockPos(100_015, 65, 100_083);
        data.putDoor(new LabyrinthData.Door(
                "hallway_end",
                HouseDimensions.INTERIOR,
                hallwayDoor,
                Direction.NORTH,
                LabyrinthData.toPlace(LabyrinthPlace.JUNCTION),
                false
        ));
        LabyrinthData.Waypoint root = LabyrinthDoors.hallwayReturn(data);
        helper.assertTrue(root != null, "the hallway should provide a return root");
        helper.assertTrue(root.dimension().equals(HouseDimensions.INTERIOR), "the return root is not in the House");
        helper.assertTrue(root.pos().z < hallwayDoor.getZ() - 0.25D,
                "the return root is not safely on the hallway side of the far door: " + root.pos());

        BlockPos origin = new BlockPos(100_000, 64, 100_000);
        BlockPos junction = LabyrinthPlaces.base(origin, LabyrinthPlace.JUNCTION);
        helper.assertTrue(junction != null && HouseLabyrinth.isBeyondThreshold(origin, junction.offset(0, 1, -4)),
                "beds do not work in the labyrinth");
        helper.assertTrue(!HouseLabyrinth.isBeyondThreshold(origin, origin.offset(4, 7, 4)), "but they do in the manor");
        helper.assertTrue(LabyrinthPlaces.placeAt(origin, LabyrinthPlaces.base(origin, LabyrinthPlace.FLOORBOARDS).offset(0, 2, -5))
                == LabyrinthPlace.FLOORBOARDS, "the floorboards' slot is its own");
        helper.assertTrue(LabyrinthPlaces.stackAbove(origin), "a manor at y 64 has its labyrinth above it");
        helper.assertTrue(!LabyrinthPlaces.stackAbove(new BlockPos(0, 200, 0)), "one at y 200 has it below");
        helper.succeed();
    }

    /** Every place gets a slot of its own inside the world, however high the manor stands. */
    @GameTest(template = "empty")
    public static void everyPlaceHasASlotThatOverlapsNoOther(GameTestHelper helper) {
        for (int y : new int[]{-20, 64, 120, 180, 230}) {
            BlockPos origin = new BlockPos(1_000, y, -2_000);
            List<BoundingBox> slots = new ArrayList<>();
            for (LabyrinthPlace place : LabyrinthPlace.values()) {
                if (place.slot() < 0) {
                    continue;
                }
                BoundingBox slot = LabyrinthPlaces.slotBounds(origin, place);
                helper.assertTrue(slot != null, place.id() + " has no slot for a manor at y " + y);
                helper.assertTrue(slot.minY() >= -64 && slot.maxY() <= 319, place.id() + " is outside the world for a manor at y " + y);
                helper.assertTrue(slot.maxY() < origin.getY() - 5 || slot.minY() > origin.getY() + 22,
                        place.id() + " cuts through the manor at y " + y);
                for (BoundingBox other : slots) {
                    helper.assertTrue(!slot.intersects(other), place.id() + " overlaps another slot for a manor at y " + y);
                }
                slots.add(slot);
            }
        }
        helper.succeed();
    }

    /**
     * A room is copied with its door turned to the south at (0, 0, 0), its
     * chest empty and its window kept, and stands in the Red Room behind
     * the entry door.
     */
    @GameTest(template = "empty")
    public static void redRoomCopiesARoomThroughItsOwnDoorWithNothingInIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 160);
        for (int x = 0; x <= 6; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = 0; z <= 5; z++) {
                    boolean inside = x >= 1 && x <= 5 && y >= 1 && y <= 3 && z >= 1 && z <= 4;
                    level.setBlock(o.offset(x, y, z), inside ? Blocks.AIR.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }
        // A window in the north wall, a door in the east wall, a chest with something in it, a bed.
        level.setBlock(o.offset(3, 2, 0), Blocks.GLASS_PANE.defaultBlockState(), 2);
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
        level.setBlock(o.offset(6, 1, 2), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(o.offset(6, 2, 2), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        level.setBlock(o.offset(1, 1, 1), Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(o.offset(1, 1, 1)) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        }
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(o.offset(2, 1, 4), bed.setValue(BedBlock.PART, BedPart.FOOT), 2);
        level.setBlock(o.offset(3, 1, 4), bed.setValue(BedBlock.PART, BedPart.HEAD), 2);

        RoomSnapshot snapshot = RedRoom.capture(level, o.offset(3, 1, 2), 5L, 1L, 0L);
        helper.assertTrue(snapshot != null, "the room was not copied");
        BlockState doorway = snapshot.stateAt(BlockPos.ZERO);
        helper.assertTrue(doorway != null && doorway.getBlock() instanceof DoorBlock && doorway.getValue(DoorBlock.OPEN),
                "its own door should stand open at the doorway, was " + doorway);
        // The east door turned to the south: (dx, dz) becomes (-dz, dx).
        helper.assertTrue(Blocks.CHEST.equals(stateBlock(snapshot, new BlockPos(1, 0, -5))), "the chest lies where the turn puts it");
        helper.assertTrue(Blocks.GLASS_PANE.equals(stateBlock(snapshot, new BlockPos(2, 1, -3))), "the window is kept");
        helper.assertTrue(snapshot.max().getZ() == 0, "nothing of it lies past its doorway");

        BlockPos base = o.offset(0, 0, 90);
        RedRoom.place(level, base, snapshot);
        helper.assertTrue(level.getBlockEntity(base.offset(1, 0, -5)) instanceof ChestBlockEntity chest && chest.isEmpty(),
                "the copied chest should be there, and empty");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, 1)).is(Blocks.SPRUCE_DOOR), "the entry door stands behind the doorway");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, -1)).isAir(), "and the room opens beyond it");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, -7)).is(Blocks.WHITE_TERRACOTTA), "past its walls there is only white");
        helper.assertTrue(RedRoom.isInRoom(base, base.offset(1, 0, -3)), "its bed lies in the Red Room");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hideAndClapRoomHasOpenFloorAndABedWithItsPost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 330);
        HideAndClap.build(level.getServer(), level, base);
        helper.assertTrue(level.getBlockState(base.offset(HideAndClap.BED_FOOT)).getBlock() instanceof BedBlock, "the child's bed");
        helper.assertTrue(level.getBlockState(base.offset(HideAndClap.BED_POST)).is(Blocks.SPRUCE_FENCE), "its post");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, 1)).getBlock() instanceof DoorBlock, "a door in the south wall");
        int open = HideAndClap.openSpots(level, base).size();
        helper.assertTrue(open >= 40, "open floor to clap from, found " + open + " spots");
        helper.succeed();
    }

    /** The wardrobe goes two blocks directly behind the player, or within 45 degrees of it. */
    @GameTest(template = "empty")
    public static void wardrobeStandsBehindThePlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 400);
        HideAndClap.build(level.getServer(), level, base);
        Vec3 player = new Vec3(base.getX() + 0.5D, base.getY(), base.getZ() - 6.5D);
        BlockPos behind = HideAndClap.wardrobeSpot(level, base, player, 180.0F);
        helper.assertTrue(base.offset(0, 0, -5).equals(behind), "facing north, it goes two blocks south, was " + behind);

        level.setBlock(behind, Blocks.STONE.defaultBlockState(), 3);
        BlockPos aside = HideAndClap.wardrobeSpot(level, base, player, 180.0F);
        helper.assertTrue(aside != null && !aside.equals(behind), "with that blocked it goes somewhere else");
        double dx = aside.getX() + 0.5D - player.x;
        double dz = aside.getZ() + 0.5D - player.z;
        double angle = Math.toDegrees(Math.atan2(Math.abs(dx), dz));
        double distance = Math.sqrt(dx * dx + dz * dz);
        helper.assertTrue(dz > 0 && angle <= 50.0D && distance >= 1.3D && distance <= 2.9D,
                "still behind them, within 45 degrees: " + aside + " at " + angle + " degrees, " + distance + " away");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crayonDrawingShowsSomeoneBlindfoldedAndTheBed(GameTestHelper helper) {
        byte[] page = CrayonDrawing.draw(new Vec3(0.5D, 1.0D, 0.5D), 0.0F, new Vec3(0.5D, 0.0D, 4.5D), new Vec3(2.5D, 0.0D, 6.5D), 7L);
        int black = 0;
        int red = 0;
        for (byte pixel : page) {
            if (pixel == CrayonDrawing.BLACK) {
                black++;
            } else if (pixel == CrayonDrawing.RED) {
                red++;
            }
        }
        helper.assertTrue(page.length == CrayonDrawing.SIZE * CrayonDrawing.SIZE, "a full map's worth");
        helper.assertTrue(black >= 10, "a blindfold, pressed hard: " + black + " black pixels");
        helper.assertTrue(red >= 10, "and the bed: " + red + " red pixels");
        helper.assertTrue(LabyrinthRegistry.BLINDFOLD.get().getEquipmentSlot() == EquipmentSlot.HEAD, "the blindfold is worn on the head");
        helper.succeed();
    }

    /** Each period of a jogging hallway is block for block the same as the next, so the shift between them can't be seen. */
    @GameTest(template = "empty")
    public static void hallwayPeriodsAreIdentical(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (LabyrinthPlace place : List.of(LabyrinthPlace.LONG_HALLWAY, LabyrinthPlace.HOTEL_HALLWAY)) {
            BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(place == LabyrinthPlace.LONG_HALLWAY ? -300 : -340, 6, 40);
            LabyrinthLoops.buildHallway(level, base, place);
            for (int k = 0; k <= 1; k++) {
                BlockPos a = base.offset(LabyrinthLoops.periodOrigin(k));
                BlockPos b = base.offset(LabyrinthLoops.periodOrigin(k + 1));
                for (int x = -2; x <= 5; x++) {
                    for (int y = -1; y <= 3; y++) {
                        for (int z = -11; z <= 0; z++) {
                            BlockState first = level.getBlockState(a.offset(x, y, z));
                            BlockState second = level.getBlockState(b.offset(x, y, z));
                            helper.assertTrue(first == second, place.id() + ": period " + k + " and " + (k + 1) + " differ at "
                                    + x + " " + y + " " + z + ": " + first + " / " + second);
                        }
                    }
                }
            }
            helper.assertTrue(level.getBlockState(base.offset(0, 0, 1)).getBlock() instanceof DoorBlock, place.id() + " has its entry door");
        }
        helper.assertTrue(level.getBlockState(helper.absolutePos(BlockPos.ZERO).offset(-300, 6, 40).offset(12, 0, -45)).getBlock() instanceof DoorBlock,
                "the long hallway's far door");
        helper.succeed();
    }

    /** Each turn of the spiral is the same as the next, and it ends at a landing with a door. */
    @GameTest(template = "empty")
    public static void spiralTurnsAreIdentical(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-300, 6, 130);
        LabyrinthLoops.buildSpiral(level, base);
        for (int x = -2; x <= 2; x++) {
            for (int z = -4; z <= 0; z++) {
                for (int y = 0; y < LabyrinthLoops.TURN; y++) {
                    BlockState first = level.getBlockState(base.offset(x, LabyrinthLoops.TURN + y, z));
                    BlockState second = level.getBlockState(base.offset(x, 2 * LabyrinthLoops.TURN + y, z));
                    helper.assertTrue(first == second, "turns 1 and 2 differ at " + x + " " + y + " " + z + ": " + first + " / " + second);
                }
            }
        }
        // Every ordinary step has three completely empty blocks above it.
        // The previous spiral lamps hung in that third block and interfered
        // with jumping/sprinting even though two-block standing headroom was
        // technically clear.
        for (int turn = 0; turn < LabyrinthLoops.SPIRAL_TURNS - 1; turn++) {
            for (int i = 0; i < LabyrinthLoops.RING.size(); i++) {
                BlockPos step = base.offset(LabyrinthLoops.RING.get(i)).above(LabyrinthLoops.stepY(turn, i));
                helper.assertTrue(
                        level.getBlockState(step.above()).isAir()
                                && level.getBlockState(step.above(2)).isAir()
                                && level.getBlockState(step.above(3)).isAir(),
                        "three-block maneuvering clearance over turn " + turn + " step " + i
                );
            }

            BlockPos lamp = base.offset(2, LabyrinthLoops.TURN * turn + 2, -2);
            BlockState lampState = level.getBlockState(lamp);
            helper.assertTrue(lampState.is(Blocks.LANTERN), "turn " + turn + " lamp is not recessed into the east wall");
            helper.assertTrue(!lampState.getValue(LanternBlock.HANGING), "turn " + turn + " lamp still hangs into the stair");
        }
        helper.assertTrue(level.getBlockState(base.offset(0, 16, 0)).getBlock() instanceof DoorBlock, "the far door at the top");
        helper.assertTrue(level.getBlockState(base.offset(0, 16, -1)).isAir(), "and a landing before it");
        helper.assertTrue(level.getBlockState(base.offset(LabyrinthLoops.WELL).above(9)).isAir(), "the well is open");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void loopsCountPeriodsTurnsAndRoomNumbers(GameTestHelper helper) {
        BlockPos base = new BlockPos(0, 64, 0);
        helper.assertTrue(LabyrinthLoops.periodOf(base, 0.5D) == -1, "the jog by the entry door is period -1");
        helper.assertTrue(LabyrinthLoops.periodOf(base, -2.5D) == 0, "the first straight is period 0");
        helper.assertTrue(LabyrinthLoops.periodOf(base, -14.5D) == 1, "twelve on is period 1");
        helper.assertTrue(LabyrinthLoops.turnOf(base, 72.0D) == 2, "eight up is the third turn");
        helper.assertTrue(LabyrinthLoops.roomNumber(0, 1, 0) == LabyrinthLoops.roomNumber(1, 0, 0),
                "a lap on, the doors behind show what the doors ahead did");
        helper.assertTrue(LabyrinthLoops.roomNumber(0, 0, 1) > LabyrinthLoops.roomNumber(0, 0, 0), "the numbers climb");
        helper.assertTrue(LabyrinthLoops.roomNumber(LabyrinthLoops.HOTEL_ROOMS / 2, 0, 0) == LabyrinthLoops.roomNumber(0, 0, 0),
                "and repeat");
        helper.succeed();
    }

    /** A scent from a vignette's object makes the next dealing include a vignette door, with Hillary heard behind it. */
    @GameTest(template = "empty")
    public static void hillarysScentDealsAVignetteDoorSheBarksBehind(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        helper.assertTrue(VignetteYields.of(TellTaleFloorboards.caregiversNote()) != null, "the caregiver's note carries its vignette");
        helper.assertTrue(VignetteYields.of(new ItemStack(Items.WRITABLE_BOOK)) == null, "an ordinary book does not");
        helper.assertTrue(LabyrinthDealer.giveScent(data) == LabyrinthDealer.Scent.SEEKING, "she takes the scent");
        helper.assertTrue(LabyrinthDealer.giveScent(data) == LabyrinthDealer.Scent.ALREADY, "and keeps it until it is used");

        LabyrinthDealer.dealPlace(data, LabyrinthPlace.JUNCTION, RandomSource.create(3));
        int barking = 0;
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
            LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
            if (door != null && door.bark) {
                barking++;
                LabyrinthPlace dealt = LabyrinthPlace.byId(door.dealt);
                helper.assertTrue(dealt != null && dealt.isOneShot() && door.leak, "the door she found leads to an unfound vignette");
            }
        }
        helper.assertTrue(barking == 1, "exactly one door has her behind it, found " + barking);
        helper.assertTrue(!data.hillaryScent(), "the scent is spent");

        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isOneShot()) {
                data.setCompleted(place.id(), true);
            }
        }
        helper.assertTrue(LabyrinthDealer.giveScent(data) == LabyrinthDealer.Scent.NOTHING, "with nothing left she finds nothing");
        helper.succeed();
    }

    /** The Growl comes more often the deeper you are, but never often; the close ones only deep in. */
    @GameTest(template = "empty")
    public static void theGrowlComesMoreOftenDeeperButStaysSporadic(GameTestHelper helper) {
        helper.assertTrue(Growl.meanGap(1) == Growl.BASE_GAP, "one door deep: the base gap");
        helper.assertTrue(Growl.meanGap(4) < Growl.meanGap(2) && Growl.meanGap(2) < Growl.meanGap(1), "deeper, sooner");
        helper.assertTrue(Growl.meanGap(60) == Growl.MIN_MEAN_GAP, "but the average never falls below its floor");
        RandomSource random = RandomSource.create(5);
        for (int i = 0; i < 200; i++) {
            helper.assertTrue(Growl.gap(60, random) >= Growl.MIN_GAP, "no gap is ever short");
        }
        boolean near = false;
        for (int i = 0; i < 400; i++) {
            helper.assertTrue(Growl.pick(1, random) == Growl.Kind.FAR, "near the hallway it is always far off");
            helper.assertTrue(Growl.pick(4, random) != Growl.Kind.NEAR, "not close until deep in");
            near |= Growl.pick(9, random) == Growl.Kind.NEAR;
        }
        helper.assertTrue(near, "deep in, sometimes close");

        Growl.GrowlData data = new Growl.GrowlData();
        UUID player = UUID.randomUUID();
        helper.assertTrue(!data.isBasementEligible(player, 0L), "the cellar waits");
        for (int i = 0; i < Growl.BASEMENT_HEARD; i++) {
            data.noteHeard(player);
        }
        helper.assertTrue(data.isBasementEligible(player, 0L), "until they have heard it often enough");
        data.noteBasement(player, 3L);
        helper.assertTrue(!data.isBasementEligible(player, 3L + Growl.BASEMENT_COOLDOWN_DAYS - 1), "then not again for a while");
        helper.assertTrue(data.isBasementEligible(player, 3L + Growl.BASEMENT_COOLDOWN_DAYS), "and then it can");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theCellarHasSomewhereToWakeUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(-420, 12, 40);
        for (int x = 17; x <= 27; x++) {
            for (int z = 12; z <= 25; z++) {
                for (int y = -5; y <= 0; y++) {
                    boolean inside = x >= 18 && x <= 26 && z >= 13 && z <= 24 && y >= -4 && y <= -1;
                    level.setBlock(origin.offset(x, y, z), inside ? Blocks.AIR.defaultBlockState() : Blocks.STONE.defaultBlockState(), 2);
                }
            }
        }
        BlockPos spot = Growl.cellarSpot(level, origin, RandomSource.create(1));
        helper.assertTrue(spot != null, "an open spot on the cellar floor");
        BlockPos rel = spot.subtract(origin);
        helper.assertTrue(rel.getY() == -4 && rel.getX() > 18 && rel.getX() < 26 && rel.getZ() > 13 && rel.getZ() < 24,
                "standing on the floor, off the walls: " + rel);
        helper.succeed();
    }

    private static Block stateBlock(RoomSnapshot snapshot, BlockPos local) {
        BlockState state = snapshot.stateAt(local);
        return state == null ? null : state.getBlock();
    }

    /** The spot a player spends the most time in is their room; old habits fade. */
    @GameTest(template = "empty")
    public static void homeRoomsTallyTheMostLivedInSpot(GameTestHelper helper) {
        HomeRooms rooms = new HomeRooms();
        UUID player = UUID.randomUUID();
        BlockPos kitchen = new BlockPos(10, 64, 10);
        BlockPos study = new BlockPos(30, 64, 10);
        rooms.record(player, kitchen, 10);
        rooms.record(player, study, 10);
        rooms.record(player, study.east(), 10);
        helper.assertTrue(rooms.topCell(player) != null && rooms.topCell(player).getKey() == HomeRooms.cellOf(study),
                "the study has the most time");
        helper.assertTrue(rooms.topCell(player).getValue().last().equals(study.east()), "and remembers where they last stood in it");
        helper.assertTrue(rooms.choiceFor(player) == null, "nothing copied yet");
        helper.assertTrue(rooms.needsCapture(player, HomeRooms.cellOf(study), 1L), "so the study wants copying");
        helper.succeed();
    }

    /**
     * Every seamless same-dimension House shift uses the same low-level
     * contract: destination available first, pitch and ordinary momentum
     * preserved, no accumulated fall distance from the impossible vertical
     * displacement.
     */
    @GameTest(template = "empty")
    public static void internalHouseTeleportPreservesThePlayersStride(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = FakePlayerFactory.get(
                level,
                new GameProfile(UUID.randomUUID(), "house_internal_shift")
        );

        Vec3 start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 3, 2)));
        player.moveTo(start.x, start.y, start.z, 37.0F, -14.0F);
        Vec3 motion = new Vec3(0.21D, 0.03D, -0.13D);
        player.setDeltaMovement(motion);
        player.fallDistance = 18.0F;

        Vec3 target = start.add(34.0D, 18.0D, 34.0D);
        HouseInternalTeleport.shift(player, target, 101.0F);

        helper.assertTrue(player.position().distanceTo(target) < 1.0E-6D,
                "internal shift did not land at the prepared destination: " + player.position());
        helper.assertTrue(Math.abs(player.getXRot() - (-14.0F)) < 0.001F,
                "internal shift changed pitch");
        helper.assertTrue(player.getDeltaMovement().distanceTo(motion) < 1.0E-6D,
                "internal shift changed ordinary momentum: " + player.getDeltaMovement());
        helper.assertTrue(player.fallDistance == 0.0F,
                "impossible vertical distance leaked into fall damage");
        helper.assertTrue(level.hasChunkAt(BlockPos.containing(target)),
                "destination chunk was not available after the shift");
        helper.succeed();
    }

    /** Through a door, the player keeps their spot and facing relative to it, turned to match. */
    @GameTest(template = "empty")
    public static void doorsShiftPlayersToTheMatchingSpot(GameTestHelper helper) {
        helper.assertTrue(LabyrinthDoors.rotationFrom(Direction.NORTH, Direction.EAST) == Rotation.CLOCKWISE_90, "north to east is a quarter turn");
        helper.assertTrue(LabyrinthDoors.rotationFrom(Direction.NORTH, Direction.SOUTH) == Rotation.CLOCKWISE_180, "north to south a half");
        helper.assertTrue(LabyrinthDoors.rotationFrom(Direction.SOUTH, Direction.SOUTH) == Rotation.NONE, "south to south none");

        // A door facing north, the player 1.2 in front of it and 0.3 to its right (east), looking south at it.
        BlockPos from = new BlockPos(10, 64, 10);
        BlockPos to = new BlockPos(500, 200, 500);
        Vec3 player = new Vec3(from.getX() + 0.5D + 0.3D, 64.0D, from.getZ() + 0.5D - 1.2D);
        Rotation turn = LabyrinthDoors.rotationFrom(Direction.NORTH, Direction.EAST);
        Vec3 shifted = LabyrinthDoors.shifted(player, from, to, turn);
        Vec3 expected = new Vec3(to.getX() + 0.5D + 1.2D, 200.0D, to.getZ() + 0.5D + 0.3D);
        helper.assertTrue(shifted.distanceTo(expected) < 1.0E-6D, "in front of an east-facing door, 1.2 east of it and 0.3 south, was " + shifted);
        helper.assertTrue(LabyrinthDoors.angle(turn) == 90.0F, "looking south becomes looking west");

        Vec3 back = LabyrinthDoors.shifted(shifted, to, from, LabyrinthDoors.inverse(turn));
        helper.assertTrue(back.distanceTo(player) < 1.0E-6D, "and the way back undoes it");
        helper.succeed();
    }
}
