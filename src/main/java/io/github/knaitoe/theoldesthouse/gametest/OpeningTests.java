package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import io.github.knaitoe.theoldesthouse.opening.EntranceDoorBlock;
import io.github.knaitoe.theoldesthouse.opening.EntranceDoorPlacer;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import io.github.knaitoe.theoldesthouse.opening.NavidsonLetter;
import io.github.knaitoe.theoldesthouse.opening.NavidsonPhoto;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import io.github.knaitoe.theoldesthouse.opening.OpeningWorldData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Checks the opening sequence's pieces on the game-test server: the letter,
 * the snapshot, the doorstep, the entrance door (in a wall and freestanding)
 * and Hillary's taming. Each test builds in its own band well clear of the
 * house structure test.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OpeningTests {
    private static final int BOOK_PAGE_WIDTH = 114;
    private static final int BOOK_PAGE_LINES = 14;

    private OpeningTests() {
    }

    // ------------------------------------------------------------------
    // The letter

    @GameTest(template = "empty")
    public static void letterPagesFit(GameTestHelper helper) {
        ItemStack book = NavidsonLetter.createBook();
        WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        helper.assertTrue(content != null, "letter has no book content");
        helper.assertTrue(NavidsonLetter.TITLE.equals(content.title().raw()), "wrong title: " + content.title().raw());
        helper.assertTrue(NavidsonLetter.AUTHOR.equals(content.author()), "wrong author: " + content.author());
        helper.assertTrue(content.pages().size() == NavidsonLetter.PAGES.size(), "wrong page count: " + content.pages().size());

        List<Filterable<Component>> pages = content.pages();
        for (int i = 0; i < pages.size(); i++) {
            Component page = pages.get(i).raw();
            int lines = wrappedLines(page.getString());
            helper.assertTrue(lines <= BOOK_PAGE_LINES, "page " + (i + 1) + " wraps to " + lines + " lines");
            helper.assertTrue(NavidsonLetter.FONT.equals(page.getStyle().getFont()), "page " + (i + 1) + " is not in Navidson's font");
        }
        helper.assertTrue(pages.get(0).raw().getString().startsWith("Howdy, neighbor."), "page 1 text");
        helper.assertTrue(pages.get(pages.size() - 1).raw().getString().endsWith("Half an inch now."), "last page text");
        helper.succeed();
    }

    /**
     * Word-wraps like the book screen, using the default font's glyph
     * advances (glyph width plus one pixel of spacing).
     */
    private static int wrappedLines(String text) {
        int lines = 0;
        for (String paragraph : text.split("\n", -1)) {
            int width = 0;
            boolean first = true;
            lines++;
            for (String word : paragraph.split(" ", -1)) {
                int wordWidth = advance(word);
                int needed = first ? wordWidth : width + advance(" ") + wordWidth;
                if (!first && needed > BOOK_PAGE_WIDTH) {
                    lines++;
                    width = wordWidth;
                } else {
                    width = needed;
                }
                first = false;
            }
        }
        return lines;
    }

    private static int advance(String text) {
        int total = 0;
        for (char c : text.toCharArray()) {
            int glyph = switch (c) {
                case '!', '\'', ',', '.', ':', ';', 'i', '|' -> 1;
                case 'l', '`' -> 2;
                case ' ', '"', '(', ')', '*', 'I', 't', '[', ']', '{', '}' -> 3;
                case 'f', 'k', '<', '>' -> 4;
                case '@', '~' -> 6;
                default -> 5;
            };
            total += glyph + 1;
        }
        return total;
    }

    // ------------------------------------------------------------------
    // The snapshot and delivered items

    @GameTest(template = "empty")
    public static void snapshotIsLockedArt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack snapshot = NavidsonLetter.createSnapshot(level, null);
        MapId id = snapshot.get(DataComponents.MAP_ID);
        helper.assertTrue(id != null, "snapshot has no map id");
        MapItemSavedData data = level.getMapData(id);
        helper.assertTrue(data != null, "snapshot map data missing");
        helper.assertTrue(data.locked, "snapshot map is not locked");

        byte[] art = NavidsonLetter.loadSnapshotPixels(level.getServer());
        helper.assertTrue(art.length == 128 * 128, "snapshot art is " + art.length + " bytes");
        boolean lit = false;
        for (int i = 0; i < art.length; i++) {
            int colour = art[i] & 0xFF;
            helper.assertTrue(colour >= 4 && (colour >> 2) <= 61, "invalid map colour " + colour + " at " + i);
            helper.assertTrue(data.colors[i] == art[i], "map colour differs from the art at " + i);
            int base = colour >> 2;
            lit |= base == 18 || base == 30 || base == 40; // yellow, gold, yellow terracotta
        }
        helper.assertTrue(lit, "the upper window is not lit");
        helper.assertTrue(snapshot.has(DataComponents.CUSTOM_NAME) && snapshot.has(DataComponents.LORE), "snapshot name/lore");

        UUID recipient = UUID.randomUUID();
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 4, 1)));
        DeliveredItemEntity item = DeliveredItemEntity.create(level, snapshot, at, recipient);
        helper.assertTrue(recipient.equals(item.getTarget()), "delivered item is not reserved for its recipient");
        helper.assertTrue(!item.isPushedByFluid(), "delivered item would drift in water");
        helper.assertTrue(item.getAge() < 0, "delivered item would despawn");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Navidson's photo: a copy of the player's own house

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void photoLightsTheirUpperWindow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 1, 240);
        BlockPos bed = buildTestHouse(level, base, true);
        BlockPos upperWindow = base.offset(15, 6, 19);

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(level.getServer(), UUID.randomUUID(), bed, bed.offset(0, 0, 60));
        helper.assertTrue(photo != null && photo.pixels() != null && photo.sawHouse(), "no photo of the house: " + photo);
        helper.assertTrue(photo.window() != null && !photo.windowCarved(), "their upper window was not the lit one: " + photo);
        helper.assertTrue(photo.window().getY() == upperWindow.getY(), "lit window is not the upper one: " + photo.window());

        ServerLevel outside = level.getServer().getLevel(HouseDimensions.OUTSIDE);
        helper.assertTrue(outside != null, "outside dimension missing");
        BlockPos offset = copyOffset(photo, bed);
        helper.assertTrue(outside.getBlockState(bed.offset(offset)).is(BlockTags.BEDS), "the bed was not copied");
        helper.assertTrue(outside.getBlockState(base.offset(11, 2, 11).offset(offset)).is(Blocks.COBBLESTONE), "the walls were not copied");
        helper.assertTrue(outside.getBlockState(upperWindow.offset(offset)).is(Blocks.GLASS), "the window was not copied");
        helper.assertTrue(outside.getBlockState(upperWindow.north().offset(offset)).is(Blocks.LIGHT), "no light behind the copy's window");
        helper.assertTrue(level.getBlockState(upperWindow.north()).isAir(), "the real house was altered");
        assertValidMap(helper, photo.pixels());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void photoGivesAWindowlessHouseAWindow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 1, 340);
        BlockPos bed = buildTestHouse(level, base, false);

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(level.getServer(), UUID.randomUUID(), bed, bed.offset(0, 0, 60));
        helper.assertTrue(photo != null && photo.pixels() != null && photo.sawHouse(), "no photo of the house: " + photo);
        helper.assertTrue(photo.window() != null && photo.windowCarved(), "no window was cut into the copy: " + photo);

        ServerLevel outside = level.getServer().getLevel(HouseDimensions.OUTSIDE);
        BlockPos real = photo.window().subtract(copyOffset(photo, bed));
        helper.assertTrue(outside.getBlockState(photo.window()).is(Blocks.GLASS), "the copy has no window");
        helper.assertTrue(level.getBlockState(real).is(Blocks.COBBLESTONE), "the real wall was cut: " + level.getBlockState(real));
        helper.assertTrue(real.getY() >= bed.getY() + 2, "the window is not up top: " + real);
        assertValidMap(helper, photo.pixels());
        helper.succeed();
    }

    /**
     * A two-storey cobblestone house (walls 11..19, eaves at 8, flat roof at
     * 9) on a grass platform, bed inside; with {@code windows}, glass on the
     * south wall below and up top.
     */
    private static BlockPos buildTestHouse(ServerLevel level, BlockPos base, boolean windows) {
        fill(level, base.offset(-4, -1, -4), base.offset(34, -1, 34), Blocks.GRASS_BLOCK.defaultBlockState());
        fill(level, base.offset(-4, 0, -4), base.offset(34, 12, 34), Blocks.AIR.defaultBlockState());
        for (int x = 11; x <= 19; x++) {
            for (int z = 11; z <= 19; z++) {
                boolean wall = x == 11 || x == 19 || z == 11 || z == 19;
                for (int y = 0; y <= 8; y++) {
                    if (wall) {
                        level.setBlock(base.offset(x, y, z), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
                level.setBlock(base.offset(x, 4, z), wall ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(base.offset(x, 9, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        if (windows) {
            level.setBlock(base.offset(13, 2, 19), Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(base.offset(15, 6, 19), Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos bed = base.offset(15, 0, 14);
        BlockState bedState = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        level.setBlock(bed, bedState.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_CLIENTS);
        level.setBlock(bed.south(), bedState.setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_CLIENTS);
        return bed;
    }

    /** Copy position minus real position (the copy keeps heights). */
    private static BlockPos copyOffset(NavidsonPhoto.Result photo, BlockPos bed) {
        BlockPos sourceMin = bed.offset(-NavidsonPhoto.CAPTURE_RADIUS, 0, -NavidsonPhoto.CAPTURE_RADIUS);
        return new BlockPos(photo.copyMin().getX() - sourceMin.getX(), 0, photo.copyMin().getZ() - sourceMin.getZ());
    }

    private static void assertValidMap(GameTestHelper helper, byte[] pixels) {
        helper.assertTrue(pixels.length == 128 * 128, "photo is " + pixels.length + " bytes");
        for (byte pixel : pixels) {
            int colour = pixel & 0xFF;
            helper.assertTrue(colour >= 4 && (colour >> 2) <= 61, "invalid map colour " + colour);
        }
    }

    // ------------------------------------------------------------------
    // Doorstep

    @GameTest(template = "empty")
    public static void doorstepIsOutdoors(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 24, 180);
        fill(level, base.offset(0, 0, 0), base.offset(10, 0, 12), Blocks.STONE.defaultBlockState());
        // A hut, 2..6 x 2..6, with a door in its south wall at x=4.
        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                boolean wall = x == 2 || x == 6 || z == 2 || z == 6;
                for (int y = 1; y <= 2; y++) {
                    level.setBlock(base.offset(x, y, z), wall ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                level.setBlock(base.offset(x, 3, z), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        BlockPos door = base.offset(4, 1, 6);
        BlockState oak = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        level.setBlock(door, oak.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
        level.setBlock(door.above(), oak.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
        BlockPos bed = base.offset(3, 1, 3);

        BlockPos step = Doorsteps.doorstep(level, door, bed);
        helper.assertTrue(base.offset(4, 1, 7).equals(step), "doorstep should be outside the hut, was " + step);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The entrance door

    @GameTest(template = "empty")
    public static void doorAppearsInAWall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 24, 120);
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        fill(level, base, base.offset(12, 0, 12), Blocks.STONE.defaultBlockState());
        // A roofed room, walls at 2 and 10 on both axes, three blocks high.
        for (int x = 2; x <= 10; x++) {
            for (int z = 2; z <= 10; z++) {
                boolean wall = x == 2 || x == 10 || z == 2 || z == 10;
                for (int y = 1; y <= 3; y++) {
                    level.setBlock(base.offset(x, y, z), wall ? planks : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                level.setBlock(base.offset(x, 4, z), planks, Block.UPDATE_CLIENTS);
            }
        }
        BlockPos bed = base.offset(5, 1, 5);
        BlockState bedState = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        level.setBlock(bed, bedState.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_CLIENTS);
        level.setBlock(bed.south(), bedState.setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_CLIENTS);

        UUID owner = UUID.randomUUID();
        List<EntranceDoorPlacer.Plan> plans = EntranceDoorPlacer.findWallPlans(level, bed, 12, owner, pos -> false);
        helper.assertTrue(!plans.isEmpty(), "no wall position found in a plain room");
        EntranceDoorPlacer.Plan plan = plans.get(0);
        BlockPos lower = plan.lower();
        helper.assertTrue(level.getBlockState(lower).is(Blocks.OAK_PLANKS), "door planned outside the wall: " + lower);
        helper.assertTrue(level.getBlockState(lower.relative(plan.open())).isAir(), "door does not open onto the room");

        EntranceDoorPlacer.place(level, plan, owner);
        BlockState placedLower = level.getBlockState(lower);
        BlockState placedUpper = level.getBlockState(lower.above());
        helper.assertTrue(placedLower.getBlock() instanceof EntranceDoorBlock
                        && placedLower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER, "lower half: " + placedLower);
        helper.assertTrue(placedUpper.getBlock() instanceof EntranceDoorBlock
                        && placedUpper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER, "upper half: " + placedUpper);
        helper.assertTrue(placedLower.getValue(DoorBlock.FACING) == plan.open().getOpposite(), "door faces the wrong way");
        helper.assertTrue(placedLower.getDestroySpeed(level, lower) < 0.0F, "door can be broken in survival");
        helper.assertTrue(placedLower.getPistonPushReaction() == PushReaction.BLOCK, "pistons can move the door");

        OpeningWorldData.EntranceRecord record = OpeningWorldData.get(level.getServer()).doorOf(owner);
        helper.assertTrue(record != null && record.lower().equals(lower), "door not recorded");
        helper.assertTrue(record.replaced().size() == 2
                        && record.replaced().stream().allMatch(replaced -> replaced.state().is(Blocks.OAK_PLANKS)),
                "replaced blocks not recorded: " + record.replaced());

        // Digging out the floor must not destroy it.
        level.setBlock(lower.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(lower).getBlock() instanceof EntranceDoorBlock, "door fell without a floor");

        // Another player's door must keep its distance and its own wall.
        UUID neighbour = UUID.randomUUID();
        for (EntranceDoorPlacer.Plan other : EntranceDoorPlacer.findWallPlans(level, bed, 12, neighbour, pos -> false)) {
            helper.assertTrue(other.lower().distSqr(lower) >= EntranceDoorPlacer.MIN_DOOR_SPACING * EntranceDoorPlacer.MIN_DOOR_SPACING,
                    "second door too close: " + other.lower());
            helper.assertTrue(other.open() != plan.open() || !samePlane(other, plan), "second door on the same wall: " + other.lower());
        }

        helper.assertTrue(OpeningSequence.removeEntranceDoor(level.getServer(), owner), "door record missing on removal");
        helper.assertTrue(level.getBlockState(lower).is(Blocks.OAK_PLANKS) && level.getBlockState(lower.above()).is(Blocks.OAK_PLANKS),
                "wall not restored after removing the door");
        helper.succeed();
    }

    private static boolean samePlane(EntranceDoorPlacer.Plan a, EntranceDoorPlacer.Plan b) {
        return a.open().getAxis() == Direction.Axis.X
                ? a.lower().getX() == b.lower().getX()
                : a.lower().getZ() == b.lower().getZ();
    }

    @GameTest(template = "empty")
    public static void freestandingDoorOnOpenGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 24, 150);
        fill(level, base, base.offset(16, 0, 16), Blocks.GRASS_BLOCK.defaultBlockState());
        fill(level, base.offset(0, 1, 0), base.offset(16, 4, 16), Blocks.AIR.defaultBlockState());
        BlockPos bed = base.offset(8, 1, 8);

        UUID owner = UUID.randomUUID();
        helper.assertTrue(EntranceDoorPlacer.findWallPlans(level, bed, 12, owner, pos -> false).isEmpty(),
                "found a wall on open ground");
        List<EntranceDoorPlacer.Plan> plans = EntranceDoorPlacer.findFreestandingPlans(level, bed, owner);
        helper.assertTrue(!plans.isEmpty(), "no open ground for a freestanding door");
        EntranceDoorPlacer.Plan plan = plans.get(0);
        helper.assertTrue(plan.lower().distSqr(bed) <= EntranceDoorPlacer.FREESTANDING_RADIUS * EntranceDoorPlacer.FREESTANDING_RADIUS,
                "freestanding door too far from the bed");

        EntranceDoorPlacer.place(level, plan, owner);
        BlockPos lower = plan.lower();
        Direction side = plan.open().getClockWise();
        helper.assertTrue(level.getBlockState(lower).getBlock() instanceof EntranceDoorBlock, "freestanding door missing");
        helper.assertTrue(level.getBlockState(lower.relative(side)).is(Blocks.STRIPPED_DARK_OAK_LOG)
                && level.getBlockState(lower.relative(side.getOpposite()).above()).is(Blocks.STRIPPED_DARK_OAK_LOG)
                && level.getBlockState(lower.above(2)).is(Blocks.STRIPPED_DARK_OAK_LOG), "frame incomplete");

        OpeningWorldData.EntranceRecord record = OpeningWorldData.get(level.getServer()).doorOf(owner);
        helper.assertTrue(record != null && record.freestanding() && record.replaced().size() == 9, "freestanding door not recorded");
        OpeningSequence.removeEntranceDoor(level.getServer(), owner);
        helper.assertTrue(level.getBlockState(lower).isAir() && level.getBlockState(lower.above(2)).isAir(), "frame not removed");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Hillary

    @GameTest(template = "empty")
    public static void hillaryTamesOnFirstBoneForHerRecipientOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos home = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlock(home.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(home, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(home.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);

        // Fake players never log in, so no client payloads are sent to them.
        ServerPlayer recipient = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "hillary_recipient"));
        ServerPlayer stranger = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "hillary_stranger"));
        try {
            Wolf wolf = Hillary.spawn(level, home, recipient.getUUID());
            helper.assertTrue(wolf != null, "Hillary did not spawn");
            helper.assertTrue(wolf.getVariant().is(WolfVariants.ASHEN), "Hillary is not ashen");
            helper.assertTrue(wolf.getCustomName() != null && Hillary.NAME.equals(wolf.getCustomName().getString()), "Hillary is unnamed");
            helper.assertTrue(wolf.isPersistenceRequired() && !wolf.isTame(), "Hillary should be persistent and untamed");
            helper.assertTrue(Hillary.tagOf(wolf) != null, "Hillary is not tagged");

            stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            stranger.interactOn(wolf, InteractionHand.MAIN_HAND);
            helper.assertTrue(!wolf.isTame(), "someone else tamed Hillary");

            recipient.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            recipient.interactOn(wolf, InteractionHand.MAIN_HAND);
            // Compare owner UUIDs: isOwnedBy looks the owner up in the level's
            // player list, which fake players are not part of.
            helper.assertTrue(wolf.isTame() && recipient.getUUID().equals(wolf.getOwnerUUID()),
                    "one bone did not tame Hillary for her recipient");
            wolf.discard();
        } finally {
            recipient.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
