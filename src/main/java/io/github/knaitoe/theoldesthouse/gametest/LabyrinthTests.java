package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.labyrinth.CrayonDrawing;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganPhoneItem;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.HomeRooms;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthCampsite;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthHazards;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLighting;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLoops;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthSpawnRules;
import io.github.knaitoe.theoldesthouse.labyrinth.RedRoom;
import io.github.knaitoe.theoldesthouse.labyrinth.RoomSnapshot;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import io.github.knaitoe.theoldesthouse.labyrinth.VignetteYields;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
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
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.StairBlock;
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

        helper.assertTrue(level.getBlockState(base.offset(TellTaleFloorboards.LOOSE_BOARD)).is(HouseBlocks.LOOSE_FLOORBOARD.get()),
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
    public static void vanillaMonstersCannotSpawnInLabyrinthSlots(GameTestHelper helper) {
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos inside = LabyrinthPlaces.base(origin, LabyrinthPlace.JUNCTION);
        helper.assertTrue(inside != null, "junction slot fits the test house origin");

        helper.assertTrue(LabyrinthSpawnRules.shouldBlock(
                        HouseDimensions.INTERIOR, origin, EntityType.ZOMBIE, inside, false),
                "vanilla monsters are rejected inside a Labyrinth slot");
        helper.assertTrue(LabyrinthSpawnRules.shouldBlock(
                        HouseDimensions.INTERIOR, origin, EntityType.CREEPER, inside, false),
                "other vanilla monster types are rejected too");
        helper.assertTrue(LabyrinthSpawnRules.shouldBlock(
                        HouseDimensions.INTERIOR, origin, EntityType.COW, inside, false),
                "ordinary passive spawns are rejected along with monsters");
        helper.assertTrue(!LabyrinthSpawnRules.shouldBlock(
                        Level.OVERWORLD, origin, EntityType.ZOMBIE, inside, false),
                "the same mob remains legal outside the House dimension");
        helper.assertTrue(!LabyrinthSpawnRules.shouldBlock(
                        HouseDimensions.INTERIOR, origin, EntityType.ZOMBIE, origin, false),
                "the rule is Labyrinth-only, not a blanket ban across the manor");
        helper.assertTrue(!LabyrinthSpawnRules.shouldBlock(
                        HouseDimensions.INTERIOR, origin, EntityType.ZOMBIE, inside, true),
                "authored vanilla monsters can opt in explicitly");
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
        UUID player = UUID.randomUUID();
        deepen(data, player, 10);
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        RandomSource random = RandomSource.create(99);

        boolean dealt = false;
        for (int dealing = 0; dealing < 4 && !dealt; dealing++) {
            LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.JUNCTION, random);
            int vignettes = 0;
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                if (!LabyrinthData.DEALT.equals(door.destination)) {
                    continue;
                }
                LabyrinthData.Deal answer = data.deal(player, door);
                helper.assertTrue(answer != null, "a junction door was not dealt");
                LabyrinthPlace dealtPlace = LabyrinthPlace.byId(answer.place());
                if (dealtPlace != null && dealtPlace.isVignette()) {
                    vignettes++;
                    helper.assertTrue(answer.leak(), "a vignette door should leak");
                }
            }
            helper.assertTrue(vignettes <= 1, "more than one vignette door in a dealing");
            dealt = vignettes == 1;
        }
        helper.assertTrue(dealt, "four dealings passed without a vignette door");
        helper.assertTrue(data.dryDeals(player) == 0, "the dry spell resets");

        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isFinishable()) {
                data.setCompleted(place.id(), true);
                var story=WitnessAccount.Story.of(place.id());if(story!=null)WitnessAccount.resolve(data,player,story,"resolved");
            }
        }
        helper.assertTrue(LabyrinthDealer.vignetteChance(data, player) > 0
                        && LabyrinthDealer.vignettesAvailable(data).equals(List.of(LabyrinthPlace.MOTHER_DEN,LabyrinthPlace.ZAMPANO_COURTYARD,LabyrinthPlace.KAREN_ROOM,LabyrinthPlace.HOTEL)),
                "the recurring Mother, courtyard, room and hotel anchors stay findable when finishable stories are finished");
        data.setReady(RedRoom.ID, true);
        helper.assertTrue(LabyrinthDealer.vignettesAvailable(data).contains(LabyrinthPlace.RED_ROOM)
                        && LabyrinthDealer.vignettesAvailable(data).contains(LabyrinthPlace.MOTHER_DEN),
                "a captured Red Room joins the recurring Mother's anchor");
        data.setReady(RedRoom.ID, false);
        for (int dealing = 0; dealing < 6; dealing++) {
            LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.JUNCTION, random);
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                LabyrinthData.Deal answer = data.deal(player, door);
                LabyrinthPlace dealtPlace = answer == null ? null : LabyrinthPlace.byId(answer.place());
                helper.assertTrue(dealtPlace == null || !dealtPlace.isFinishable(), "a finished vignette was dealt again");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dealsDrySpellsAndScentsBelongToThePlayer(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        LabyrinthData.Door west = data.door("junction/west");
        helper.assertTrue(west != null, "the west door exists");

        data.deal(a, west, LabyrinthPlace.JUNCTION.id(), true, false);
        data.deal(b, west, LabyrinthPlace.GRAY_CORRIDOR.id(), false, true);
        data.setDryDeals(a, 3);
        data.setDryDeals(b, 1);
        data.setHillaryScent(a, true);

        helper.assertTrue(LabyrinthPlace.JUNCTION.id().equals(data.deal(a, west).place()), "a keeps a's route");
        helper.assertTrue(LabyrinthPlace.GRAY_CORRIDOR.id().equals(data.deal(b, west).place()), "b keeps b's route");
        helper.assertTrue(data.deal(a, west).leak() && !data.deal(a, west).bark(), "a keeps a's leak");
        helper.assertTrue(!data.deal(b, west).leak() && data.deal(b, west).bark(), "b keeps b's leak");
        helper.assertTrue(data.dryDeals(a) == 3 && data.dryDeals(b) == 1, "dry streaks are independent");
        helper.assertTrue(data.hillaryScent(a) && !data.hillaryScent(b), "Hillary's scent is independent");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void grayMazeGrowsWithRouteDepthWithoutRoutineSelfLoops(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();

        List<LabyrinthPlace> start = LabyrinthDealer.grayAvailable(data, player);
        helper.assertTrue(start.size() == 6 && start.contains(LabyrinthPlace.JUNCTION)
                        && start.contains(LabyrinthPlace.STRAIGHT_HALL)
                        && start.contains(LabyrinthPlace.BENT_HALL) && start.contains(LabyrinthPlace.CROSS_HALL)
                        && start.contains(LabyrinthPlace.QUIET_ROOM),
                "the opening pool contains familiar halls and a quiet place");

        data.visit(player, LabyrinthPlace.FLOORBOARDS);
        deepen(data, player, 6);
        helper.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.LONG_HALLWAY),
                "six crossings open the first deeper tier");
        data.visit(player, LabyrinthPlace.HIDE_AND_CLAP);
        deepen(data, player, 10);
        helper.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.SPIRAL_STAIR),
                "ten crossings open the spiral stair");
        data.visit(player, LabyrinthPlace.MODEL_HOME);
        deepen(data, player, 14);
        helper.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.HOTEL_HALLWAY),
                "fourteen crossings open the hotel hallway");

        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isFinishable()) {
                data.setCompleted(place.id(), true);
                var story=WitnessAccount.Story.of(place.id());if(story!=null)WitnessAccount.resolve(data,player,story,"resolved");
            }
        }
        RandomSource random = RandomSource.create(17);
        boolean selfLoop = false;
        for (int i = 0; i < 24 && !selfLoop; i++) {
            LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.JUNCTION, random);
            for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
                LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
                if (door == null || !LabyrinthData.DEALT.equals(door.destination)) {
                    continue;
                }
                LabyrinthData.Deal answer = data.deal(player, door);
                selfLoop |= answer != null && LabyrinthPlace.JUNCTION.id().equals(answer.place());
            }
        }
        helper.assertTrue(!selfLoop, "ordinary junction doors do not immediately repeat the same room");
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
        // Including the heights where one column above and below the manor
        // is too short for every place, and the stack carries on in another.
        for (int y : new int[]{-20, 36, 55, 64, 78, 100, 120, 180, 230}) {
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
    public static void harriganStudyHasItsReadingChairPhonesAndFuneralRoom(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-200, 6, 470);
        HarriganVignette.build(level.getServer(), level, base);

        helper.assertTrue(level.getBlockState(base.offset(HarriganVignette.LECTERN)).is(Blocks.LECTERN),
                "the reading lectern is in the study");
        helper.assertTrue(level.getBlockState(base.offset(HarriganVignette.CHAIR)).getBlock() instanceof StairBlock,
                "Harrigan's chair faces away from the entry");
        helper.assertTrue(level.getBlockState(base.offset(HarriganVignette.FUNERAL_DOOR)).is(Blocks.DARK_OAK_PLANKS),
                "the funeral room is hidden on the first visit");
        helper.assertTrue(level.getBlockState(base.offset(HarriganVignette.CASKET)).is(Blocks.DARK_OAK_SLAB),
                "the funeral room already has its casket behind the wall");
        helper.assertTrue(HarriganPhoneItem.homeTime(0L).equals("06:00")
                        && HarriganPhoneItem.homeTime(6000L).equals("12:00")
                        && HarriganPhoneItem.homeTime(18000L).equals("00:00"),
                "the phone reads Overworld time rather than dimension clock behavior");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void harriganTargetRulesProtectNamedMobsAndBosses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Entity zombie = EntityType.ZOMBIE.create(level);
        Entity cow = EntityType.COW.create(level);
        Entity dragon = EntityType.ENDER_DRAGON.create(level);
        helper.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, false, zombie) == HarriganVignette.TargetKind.HOSTILE,
                "an unnamed zombie is a hostile target");
        helper.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, true, zombie) == HarriganVignette.TargetKind.FRIENDLY,
                "a name-tagged zombie is friendly");
        helper.assertTrue(HarriganVignette.classify(EntityType.COW, false, cow) == HarriganVignette.TargetKind.FRIENDLY,
                "ordinary non-monsters are friendly");
        helper.assertTrue(HarriganVignette.classify(EntityType.ENDER_DRAGON, false, dragon) == HarriganVignette.TargetKind.FORBIDDEN,
                "bosses are out of Harrigan's reach");
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

    @GameTest(template = "empty")
    public static void hazardPlacesStaySurvivableAndGiveRecoveryRoutes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos flood = helper.absolutePos(BlockPos.ZERO).offset(-500, 8, 40);
        LabyrinthHazards.buildFloodedPassage(level, flood);
        helper.assertTrue(level.getBlockState(flood.offset(0, -1, -6)).is(Blocks.WATER),
                "the flooded passage really submerges the route");
        for(var pocket:LabyrinthHazards.floodAir(flood))helper.assertTrue(level.getBlockState(pocket.above(2)).isAir()&&level.getBlockState(pocket.above()).is(Blocks.WATER),"a physical chimney gives real air above the submerged route");
        var submerged=LabyrinthHazards.floodRoute(flood);helper.assertTrue(submerged.size()>100,"turns replace the trivial straight swimming corridor");
        for(int i=1;i<submerged.size();i++)helper.assertTrue(submerged.get(i).distManhattan(submerged.get(i-1))==1&&level.getBlockState(submerged.get(i)).is(Blocks.WATER),"all underwater turns are physically connected");
        helper.assertTrue(level.getBlockState(flood.offset(0, 0, -28)).getBlock() instanceof DoorBlock,
                "the flooded route has a way through");

        BlockPos broken = helper.absolutePos(BlockPos.ZERO).offset(-540, 8, 40);
        LabyrinthHazards.buildFracturedWalkway(level, broken);
        helper.assertTrue(level.getBlockState(broken.offset(0, 6, -13)).isAir(),
                "the elevated span is visibly broken");
        helper.assertTrue(level.getBlockState(broken.offset(0, -1, -13)).is(Blocks.SMOOTH_STONE),
                "missing the span lands on a real lower floor instead of a death void");
        helper.assertTrue(level.getBlockState(broken.offset(2, 0, -19)).getBlock() instanceof StairBlock,
                "the lower route has stairs back to the upper level");
        helper.assertTrue(level.getBlockState(broken.offset(0, 7, -28)).getBlock() instanceof DoorBlock,
                "the broken route remains completable");

        BlockPos squeeze = helper.absolutePos(BlockPos.ZERO).offset(-580, 8, 40);
        LabyrinthHazards.buildCompressionPassage(level, squeeze);
        helper.assertTrue(level.getBlockState(squeeze.offset(0, 1, -12)).isAir(),
                "the compression passage always leaves its center line open");
        helper.assertTrue(level.getBlockState(squeeze.offset(2, 1, -12)).isAir(),
                "it begins broad enough to telegraph the later closing");
        helper.assertTrue(level.getBlockState(squeeze.offset(0, 0, -28)).getBlock() instanceof DoorBlock,
                "the compression passage has an exit");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hazardsEnterTheGrayPoolWithDepth(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();

        helper.assertTrue(!LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.FLOODED_PASSAGE),
                "fresh players do not immediately draw hazards");
        helper.assertTrue(!LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.EXPLORER_CAMP),
                "fresh players do not immediately find the recovery camp");

        data.visit(player, LabyrinthPlace.FLOORBOARDS);
        deepen(data, player, 6);
        List<LabyrinthPlace> tierOne = LabyrinthDealer.grayAvailable(data, player);
        helper.assertTrue(tierOne.contains(LabyrinthPlace.FLOODED_PASSAGE),
                "the flooded passage waits for the sixth crossing");
        helper.assertTrue(tierOne.contains(LabyrinthPlace.FALSE_DISTANCE),
                "false distance begins at the first grown tier");
        helper.assertTrue(tierOne.contains(LabyrinthPlace.EXPLORER_CAMP),
                "the explorer camp can be found once the maze has opened up");
        helper.assertTrue(!tierOne.contains(LabyrinthPlace.FRACTURED_WALKWAY),
                "the broken route still waits");

        data.visit(player, LabyrinthPlace.HIDE_AND_CLAP);
        deepen(data, player, 10);
        List<LabyrinthPlace> tierTwo = LabyrinthDealer.grayAvailable(data, player);
        helper.assertTrue(tierTwo.contains(LabyrinthPlace.FRACTURED_WALKWAY),
                "the fractured walkway appears deeper in");
        helper.assertTrue(tierTwo.contains(LabyrinthPlace.LIGHT_SINK),
                "the light sink appears deeper in");
        helper.assertTrue(tierTwo.contains(LabyrinthPlace.MOVING_THRESHOLD),
                "the moving threshold appears deeper in");
        helper.assertTrue(!tierTwo.contains(LabyrinthPlace.COMPRESSION_PASSAGE),
                "the crushing passage waits until the deepest tier");

        data.visit(player, LabyrinthPlace.HARRIGAN);
        deepen(data, player, 14);
        List<LabyrinthPlace> tierThree = LabyrinthDealer.grayAvailable(data, player);
        helper.assertTrue(tierThree.contains(LabyrinthPlace.COMPRESSION_PASSAGE),
                "the compression passage joins the deepest gray pool");
        helper.assertTrue(tierThree.contains(LabyrinthPlace.GRAVITY_DRIFT),
                "gravity drift joins the deepest gray pool");
        helper.assertTrue(tierThree.contains(LabyrinthPlace.DUPLICATE_PASSAGE),
                "the duplicate passage joins the deepest gray pool");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eldritchHazardsHaveConcreteRulesAndRecovery(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos far = helper.absolutePos(BlockPos.ZERO).offset(-620, 8, 40);
        LabyrinthHazards.buildFalseDistance(level, far);
        helper.assertTrue(level.getBlockState(far.offset(0, 0, -34)).getBlock() instanceof DoorBlock,
                "false distance eventually has a real far door");

        BlockPos sink = helper.absolutePos(BlockPos.ZERO).offset(-660, 8, 40);
        LabyrinthHazards.buildLightSink(level, sink);
        helper.assertTrue(LabyrinthHazards.isPortableLight(Blocks.TORCH.defaultBlockState()),
                "the light sink accepts sacrificial portable light");
        helper.assertTrue(!LabyrinthHazards.isPortableLight(Blocks.COBBLESTONE.defaultBlockState()),
                "ordinary building blocks are not light-sink sacrifices");

        BlockPos threshold = helper.absolutePos(BlockPos.ZERO).offset(-700, 8, 40);
        LabyrinthHazards.buildMovingThreshold(level, threshold);
        int visibleThresholds = 0;
        for (BlockPos rel : List.of(new BlockPos(0, 0, -15), new BlockPos(-8, 0, -8), new BlockPos(8, 0, -8))) {
            if (level.getBlockState(threshold.offset(rel)).getBlock() instanceof DoorBlock) {
                visibleThresholds++;
            }
        }
        helper.assertTrue(visibleThresholds == 1, "the moving-threshold room exposes exactly one exit at a time");

        BlockPos duplicate = helper.absolutePos(BlockPos.ZERO).offset(-740, 8, 40);
        LabyrinthHazards.buildDuplicatePassage(level, duplicate);
        int duplicateDoors = 0;
        for (BlockPos rel : List.of(new BlockPos(0, 0, -15), new BlockPos(-8, 0, -8), new BlockPos(8, 0, -8))) {
            if (level.getBlockState(duplicate.offset(rel)).getBlock() instanceof DoorBlock) {
                duplicateDoors++;
            }
        }
        helper.assertTrue(duplicateDoors == 3, "all three duplicate exits look equally real");

        BlockPos gravity = helper.absolutePos(BlockPos.ZERO).offset(-780, 12, 40);
        LabyrinthHazards.buildGravityDrift(level, gravity);
        helper.assertTrue(level.getBlockState(gravity.offset(-2, -2, -12)).isAir(),
                "gravity drift has a real lower recovery trench");
        helper.assertTrue(level.getBlockState(gravity.offset(-3, -3, -10)).is(Blocks.LADDER),
                "the trench has a ladder back out");
        helper.assertTrue(level.getBlockState(gravity.offset(0, 0, -28)).getBlock() instanceof DoorBlock,
                "gravity drift still has a normal exit");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void duplicatePassageAlwaysHasTwoWrongDoorsAndOneWayOn(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID();
        BlockPos base = new BlockPos(0, 64, 0);
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.DUPLICATE_PASSAGE, base);
        data.visit(player, LabyrinthPlace.FLOORBOARDS);
        data.visit(player, LabyrinthPlace.HIDE_AND_CLAP);
        data.visit(player, LabyrinthPlace.HARRIGAN);

        LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.DUPLICATE_PASSAGE, RandomSource.create(7));
        int loops = 0;
        int onward = 0;
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.DUPLICATE_PASSAGE.doors()) {
            if (!LabyrinthData.DEALT.equals(spec.destination())) {
                continue;
            }
            LabyrinthData.Door door = data.door(LabyrinthPlace.DUPLICATE_PASSAGE.doorId(spec));
            LabyrinthData.Deal deal = door == null ? null : data.deal(player, door);
            helper.assertTrue(deal != null, "every apparent exit is dealt");
            if (LabyrinthPlace.DUPLICATE_PASSAGE.id().equals(deal.place())) {
                loops++;
            } else {
                onward++;
            }
        }
        helper.assertTrue(loops == 2 && onward == 1,
                "two identical exits recurse and exactly one advances: " + loops + " / " + onward);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void junctionGivesThePlayerTomsNoteAndTorches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-800, 8, -40);
        LabyrinthBuilder.buildJunction(level, base);
        LabyrinthLighting.buildEarlyAid(level.getServer(), level, base);

        helper.assertTrue(level.getBlockState(base.offset(LabyrinthLighting.TOM_NOTE)).is(Blocks.LECTERN),
                "Tom's note is physically waiting in the first junction");
        helper.assertTrue(level.getBlockState(base.offset(LabyrinthLighting.TOM_NOTE))
                        .getValue(net.minecraft.world.level.block.LecternBlock.HAS_BOOK),
                "the lectern actually contains the note");
        helper.assertTrue(level.getBlockEntity(base.offset(LabyrinthLighting.TOM_CACHE)) instanceof Container,
                "Tom leaves a real scavengable cache");

        Container cache = (Container) level.getBlockEntity(base.offset(LabyrinthLighting.TOM_CACHE));
        int torches = 0;
        for (int i = 0; i < cache.getContainerSize(); i++) {
            if (cache.getItem(i).is(Items.TORCH)) {
                torches += cache.getItem(i).getCount();
            }
        }
        helper.assertTrue(torches >= 12, "the early cache contains enough torches to matter, found " + torches);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void labyrinthDarknessAndLightMovementScaleWithDepth(GameTestHelper helper) {
        helper.assertTrue(LabyrinthLighting.darknessBand(1) == 0 && LabyrinthLighting.darknessBand(7) == 0,
                "the first rooms still let the player trust ordinary light");
        helper.assertTrue(LabyrinthLighting.darknessBand(8) == 1,
                "darkness waits for the eighth crossing");
        helper.assertTrue(LabyrinthLighting.darknessBand(12) == 2
                        && LabyrinthLighting.darknessBand(16) == 3,
                "the darkness pressure increases with route depth");
        helper.assertTrue(LabyrinthLighting.rearrangeInterval(16) < LabyrinthLighting.rearrangeInterval(8),
                "unwatched light changes become more frequent deeper in");
        helper.assertTrue(LabyrinthLighting.mayRearrange(LabyrinthPlace.GRAY_CORRIDOR)
                        && LabyrinthLighting.mayRearrange(LabyrinthPlace.LONG_HALLWAY)
                        && LabyrinthLighting.mayRearrange(LabyrinthPlace.HOTEL_HALLWAY),
                "ordinary and impossible hallways can betray light landmarks");
        helper.assertTrue(LabyrinthLighting.authoredLightCount(LabyrinthPlace.GRAY_CORRIDOR, 1) == 3
                        && LabyrinthLighting.authoredLightCount(LabyrinthPlace.GRAY_CORRIDOR, 16) == 0,
                "ordinary corridor fixtures physically thin from three lights to none");
        helper.assertTrue(LabyrinthLighting.authoredLightCount(LabyrinthPlace.LONG_HALLWAY, 1)
                        > LabyrinthLighting.authoredLightCount(LabyrinthPlace.LONG_HALLWAY, 12),
                "the long hallway physically loses fixtures with depth");
        helper.assertTrue(LabyrinthLighting.authoredLightCount(LabyrinthPlace.HOTEL_HALLWAY, 2) == 1,
                "the hotel begins with only one dim fixture per repeated straight");
        helper.assertTrue(!LabyrinthLighting.mayRearrange(LabyrinthPlace.FLOODED_PASSAGE)
                        && !LabyrinthLighting.mayRearrange(LabyrinthPlace.EXPLORER_CAMP),
                "survival-critical air pockets and the recovery camp keep their safety lighting");
        helper.assertTrue(LabyrinthLighting.isPortableLight(Blocks.TORCH.defaultBlockState())
                        && LabyrinthLighting.isPortableLight(Blocks.LANTERN.defaultBlockState()),
                "the House recognizes the player's ordinary portable lights");
        helper.assertTrue(LabyrinthLighting.allowsPortableLightIn(
                        LabyrinthPlace.GRAY_CORRIDOR, Blocks.TORCH.defaultBlockState()),
                "Tom's torches can actually be placed in ordinary gray space");
        helper.assertTrue(LabyrinthLighting.allowsPortableLightIn(
                        LabyrinthPlace.FALSE_DISTANCE, Blocks.LANTERN.defaultBlockState()),
                "portable lights can mark stranger gray rooms too");
        helper.assertTrue(!LabyrinthLighting.allowsPortableLightIn(
                        LabyrinthPlace.FLOORBOARDS, Blocks.TORCH.defaultBlockState()),
                "vignettes remain protected from player lighting edits");
        helper.assertTrue(!LabyrinthLighting.allowsPortableLightIn(
                        LabyrinthPlace.GRAY_CORRIDOR, Blocks.COBBLESTONE.defaultBlockState()),
                "the exception is light, not general construction");
        helper.assertTrue(!LabyrinthLighting.allowsPortableLightIn(
                        LabyrinthPlace.LIGHT_SINK, Blocks.TORCH.defaultBlockState()),
                "the Light Sink keeps its own consumptive placement rules");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void explorerCampContainsFiniteRecoverySupplies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(-820, 8, 40);
        LabyrinthCampsite.build(level.getServer(), level, base);

        helper.assertTrue(level.getBlockState(base.offset(LabyrinthCampsite.FIRE)).is(Blocks.CAMPFIRE),
                "the abandoned camp has a working fire");
        helper.assertTrue(level.getBlockState(base.offset(0, 0, -15)).getBlock() instanceof DoorBlock,
                "the camp is a route through the maze, not a dead end");
        helper.assertTrue(level.getBlockEntity(base.offset(LabyrinthCampsite.CACHE)) instanceof Container,
                "the explorers left a real scavengable cache");
        Container cache = (Container) level.getBlockEntity(base.offset(LabyrinthCampsite.CACHE));
        int food = 0;
        for (int i = 0; i < cache.getContainerSize(); i++) {
            ItemStack stack = cache.getItem(i);
            if (stack.is(Items.BREAD) || stack.is(Items.BAKED_POTATO) || stack.is(Items.COOKED_BEEF)
                    || stack.is(Items.APPLE) || stack.is(Items.COOKED_COD)) {
                food += stack.getCount();
            }
        }
        helper.assertTrue(food >= 15, "the first discoverers can actually recover food, found " + food);
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

        BlockPos hotelBase = helper.absolutePos(BlockPos.ZERO).offset(-340, 6, 40);
        BlockPos hotelPeriod = hotelBase.offset(LabyrinthLoops.periodOrigin(0));
        helper.assertTrue(level.getBlockState(hotelPeriod.below()).is(HouseBlocks.HOTEL_CARPET.get()),
                "the hotel has its authored carpet");
        helper.assertTrue(level.getBlockState(hotelPeriod.offset(-2, 0, 0)).is(HouseBlocks.HOTEL_WAINSCOT.get()),
                "dark wainscot runs along the lower hotel wall");
        helper.assertTrue(level.getBlockState(hotelPeriod.offset(-2, 1, 0)).is(HouseBlocks.HOTEL_WALLPAPER.get()),
                "wallpaper stands above the trim");
        helper.assertTrue(level.getBlockState(hotelPeriod.above(3)).is(HouseBlocks.HOTEL_CEILING.get()),
                "the hotel ceiling is its own warm plaster");
        helper.assertTrue(level.getBlockState(hotelBase.offset(LabyrinthLoops.signPos(0, 0))).is(HouseBlocks.HOTEL_ROOM_PLAQUE.get()),
                "hotel room numbers use the authored brass plaque block");
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
        UUID player = UUID.randomUUID();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        helper.assertTrue(VignetteYields.of(TellTaleFloorboards.caregiversNote()) != null, "the caregiver's note carries its vignette");
        helper.assertTrue(VignetteYields.of(new ItemStack(Items.WRITABLE_BOOK)) == null, "an ordinary book does not");
        helper.assertTrue(LabyrinthDealer.giveScent(data, player) == LabyrinthDealer.Scent.SEEKING, "she takes the scent");
        helper.assertTrue(LabyrinthDealer.giveScent(data, player) == LabyrinthDealer.Scent.ALREADY, "and keeps it until it is used");

        LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.JUNCTION, RandomSource.create(3));
        int barking = 0;
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
            LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
            LabyrinthData.Deal answer = door == null ? null : data.deal(player, door);
            if (answer != null && answer.bark()) {
                barking++;
                LabyrinthPlace dealt = LabyrinthPlace.byId(answer.place());
                helper.assertTrue(dealt != null && dealt.isVignette() && answer.leak(), "the door she found leads to an available vignette");
            }
        }
        helper.assertTrue(barking == 1, "exactly one door has her behind it, found " + barking);
        helper.assertTrue(!data.hillaryScent(player), "the scent is spent");

        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isFinishable()) {
                data.setCompleted(place.id(), true);
                var story=WitnessAccount.Story.of(place.id());if(story!=null)WitnessAccount.resolve(data,player,story,"resolved");
            }
        }
        helper.assertTrue(LabyrinthDealer.giveScent(data, player) == LabyrinthDealer.Scent.SEEKING,
                "she can still find the recurring Mother's den after the one-shots are finished");
        LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.JUNCTION, RandomSource.create(4));
        int motherDoors = 0;
        for (LabyrinthPlace.DoorSpec spec : LabyrinthPlace.JUNCTION.doors()) {
            LabyrinthData.Door door = data.door(LabyrinthPlace.JUNCTION.doorId(spec));
            LabyrinthData.Deal answer = door == null ? null : data.deal(player, door);
            if (answer != null && answer.bark()) {
                helper.assertTrue(java.util.Set.of(LabyrinthPlace.MOTHER_DEN.id(),LabyrinthPlace.HOTEL.id()).contains(answer.place()), "the scent finds an available recurring Mother or hotel anchor");
                motherDoors++;
            }
        }
        helper.assertTrue(motherDoors == 1 && !data.hillaryScent(player), "the anchor scent is routed and spent once");
        helper.succeed();
    }

    /** The Growl comes more often the deeper you are, but never often; the close ones only deep in. */
    @GameTest(template = "empty")
    public static void theGrowlComesMoreOftenDeeperButStaysSporadic(GameTestHelper helper) {
        helper.assertTrue(Growl.meanGap(8) == Growl.BASE_GAP, "the first eligible depth starts with the full base gap");
        helper.assertTrue(Growl.meanGap(12) < Growl.meanGap(10) && Growl.meanGap(10) < Growl.meanGap(8), "deeper, sooner");
        helper.assertTrue(Growl.meanGap(60) == Growl.MIN_MEAN_GAP, "but the average never falls below its floor");
        RandomSource random = RandomSource.create(5);
        for (int i = 0; i < 200; i++) {
            helper.assertTrue(Growl.gap(60, random) >= Growl.MIN_GAP, "no gap is ever short");
        }
        boolean near = false;
        for (int i = 0; i < 400; i++) {
            helper.assertTrue(Growl.pick(8, random) == Growl.Kind.FAR, "near the hallway it is always far off");
            helper.assertTrue(Growl.pick(12, random) != Growl.Kind.NEAR, "not close until deep in");
            near |= Growl.pick(16, random) == Growl.Kind.NEAR;
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

    private static void deepen(LabyrinthData data, UUID player, int depth) {
        while (data.returnDepth(player) < depth)
            data.pushReturn(player, new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, Vec3.ZERO, 0));
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
