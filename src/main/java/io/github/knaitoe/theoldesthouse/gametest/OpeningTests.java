package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseProxyEntityEvacuation;
import io.github.knaitoe.theoldesthouse.house.HouseSpawnManager;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import io.github.knaitoe.theoldesthouse.opening.HillaryTag;
import io.github.knaitoe.theoldesthouse.opening.NavidsonLetter;
import io.github.knaitoe.theoldesthouse.opening.NavidsonPhoto;
import io.github.knaitoe.theoldesthouse.opening.SnapshotRenderer;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import io.github.knaitoe.theoldesthouse.opening.OpeningWorldData;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Checks the opening sequence's pieces on the game-test server: the letter,
 * the snapshot, doorstep selection, labyrinth bed rules and Hillary's
 * taming/guidance. Each test builds in its own band well clear of the house
 * structure test.
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
    public static void writingSamplesCarryTheirOwnFonts(GameTestHelper helper) {
        List<ItemStack> samples = HouseWriting.samples();
        List<ResourceLocation> fonts = HouseWriting.sampleFonts();
        helper.assertTrue(samples.size() == 4 && fonts.size() == 4, "expected four writing specimens");
        helper.assertTrue(fonts.stream().distinct().count() == 4, "the writing styles must use distinct font ids");
        helper.assertTrue(NavidsonLetter.FONT.equals(HouseWriting.WILL_FONT), "Navidson's established font id changed");

        for (int i = 0; i < samples.size(); i++) {
            WrittenBookContent content = samples.get(i).get(DataComponents.WRITTEN_BOOK_CONTENT);
            helper.assertTrue(content != null && !content.pages().isEmpty(), "writing sample " + i + " is not readable");
            helper.assertTrue(usesFont(content.pages().getFirst().raw(), fonts.get(i)),
                    "writing sample " + i + " does not use " + fonts.get(i));
        }

        WrittenBookContent child = samples.get(3).get(DataComponents.WRITTEN_BOOK_CONTENT);
        String childText = child.pages().getFirst().raw().getString();
        helper.assertTrue(childText.chars().anyMatch(c -> c >= 0xE100 && c <= 0xE11F),
                "child writing should use alternate hand-drawn glyphs");
        helper.succeed();
    }

    private static boolean usesFont(Component component, ResourceLocation font) {
        if (font.equals(component.getStyle().getFont())) {
            return true;
        }
        for (Component child : component.getSiblings()) {
            if (usesFont(child, font)) {
                return true;
            }
        }
        return false;
    }

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

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(level.getServer(), level, UUID.randomUUID(), bed, bed.offset(0, 0, 60));
        helper.assertTrue(photo != null && photo.pixels() != null && photo.sawHouse(), "no photo of the house: " + photo);
        helper.assertTrue(photo.window() != null && !photo.windowCarved(), "their upper window was not the lit one: " + photo);
        helper.assertTrue(photo.window().getY() == upperWindow.getY(), "lit window is not the upper one: " + photo.window());

        // The game-test server has no mod dimensions: the copy stands in a far corner of the test world.
        ServerLevel outside = level;
        BlockPos offset = copyOffset(photo, bed);
        helper.assertTrue(outside.getBlockState(bed.offset(offset)).is(BlockTags.BEDS), "the bed was not copied");
        helper.assertTrue(outside.getBlockState(base.offset(11, 2, 11).offset(offset)).is(Blocks.COBBLESTONE), "the walls were not copied");
        helper.assertTrue(outside.getBlockState(upperWindow.offset(offset)).is(Blocks.GLASS), "the window was not copied");
        helper.assertTrue(outside.getBlockState(upperWindow.north().offset(offset)).is(Blocks.LIGHT), "no light behind the copy's window");
        helper.assertTrue(level.getBlockState(upperWindow.north()).isAir(), "the real house was altered");
        assertValidMap(helper, photo.pixels());
        logPhoto("upper_window", photo.pixels());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void photoGivesAWindowlessHouseAWindow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 1, 340);
        BlockPos bed = buildTestHouse(level, base, false);

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(level.getServer(), level, UUID.randomUUID(), bed, bed.offset(0, 0, 60));
        helper.assertTrue(photo != null && photo.pixels() != null && photo.sawHouse(), "no photo of the house: " + photo);
        helper.assertTrue(photo.window() != null && photo.windowCarved(), "no window was cut into the copy: " + photo);

        ServerLevel outside = level;
        BlockPos real = photo.window().subtract(copyOffset(photo, bed));
        helper.assertTrue(outside.getBlockState(photo.window()).is(Blocks.GLASS), "the copy has no window");
        helper.assertTrue(level.getBlockState(real).is(Blocks.COBBLESTONE), "the real wall was cut: " + level.getBlockState(real));
        helper.assertTrue(real.getY() >= bed.getY() + 2, "the window is not up top: " + real);
        assertValidMap(helper, photo.pixels());
        logPhoto("windowless", photo.pixels());
        helper.succeed();
    }

    /**
     * Regression for 0.3.1: if the geographically preferred facade is
     * obstructed, the renderer must widen its angle around the captured
     * settlement rather than silently substituting the baked stock house.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void photoKeepsThePlayersHouseWhenPreferredFacadeIsBlocked(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 1, 440);
        BlockPos bed = buildTestHouse(level, base, true);

        // A broad wall between the preferred +Z camera side and the house.
        // Same-side framing is intentionally awful; another side is clear.
        fill(
                level,
                base.offset(-8, 0, 25),
                base.offset(38, 14, 27),
                Blocks.DEEPSLATE_BRICKS.defaultBlockState()
        );

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(
                level.getServer(),
                level,
                UUID.randomUUID(),
                bed,
                bed.offset(0, 0, 60)
        );

        helper.assertTrue(photo != null, "photo job returned null");
        helper.assertTrue(photo.pixels() != null, "blocked preferred facade fell back to stock/null capture");
        helper.assertTrue(photo.sawHouse(), "fallback framing never found the copied custom house");

        byte[] stock = NavidsonLetter.loadSnapshotPixels(level.getServer());
        helper.assertTrue(
                !Arrays.equals(photo.pixels(), stock),
                "the player's captured build was replaced by the stock house"
        );

        assertValidMap(helper, photo.pixels());
        logPhoto("blocked_preferred_facade", photo.pixels());
        helper.succeed();
    }

    /**
     * The camera's own view of trees and timber, block by block: a natural
     * trunk is see-through however tall it is, while a bare log post and a
     * post under persistent (player-placed) leaves stay in the picture.
     */
    @GameTest(template = "empty")
    public static void photoSceneSeesThroughTrunksButNotTimber(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(300, 1, -200);
        BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState naturalLeaves = Blocks.JUNGLE_LEAVES.defaultBlockState();
        BlockState placedLeaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        // A giant trunk, twice the height an earlier build scanned up, under its canopy.
        BlockPos tree = base;
        fill(level, tree, tree.above(24), log);
        fill(level, tree.offset(-2, 25, -2), tree.offset(2, 26, 2), naturalLeaves);
        // A bare timber post, and a post under player-placed leaves.
        BlockPos post = base.offset(8, 0, 0);
        fill(level, post, post.above(5), log);
        BlockPos arbour = base.offset(14, 0, 0);
        fill(level, arbour, arbour.above(5), log);
        fill(level, arbour.offset(-1, 6, -1), arbour.offset(1, 6, 1), placedLeaves);

        BlockPos min = base.offset(-4, -1, -4);
        BlockPos max = base.offset(20, 30, 4);
        try {
            SnapshotRenderer.Scene upward = NavidsonPhoto.sceneOf(level, min, max);
            for (int dy : new int[]{0, 12, 24}) {
                helper.assertTrue(upward.sample(tree.getX(), tree.getY() + dy, tree.getZ()) == 0,
                        "a natural trunk is see-through at height " + dy + " of 25");
            }
            helper.assertTrue(upward.sample(tree.getX(), tree.getY() + 25, tree.getZ()) == 0, "and so is its canopy");

            // Asked top first, the lower logs take the column's answer from the cache.
            SnapshotRenderer.Scene downward = NavidsonPhoto.sceneOf(level, min, max);
            for (int dy : new int[]{24, 12, 0}) {
                helper.assertTrue(downward.sample(tree.getX(), tree.getY() + dy, tree.getZ()) == 0,
                        "a natural trunk is see-through at height " + dy + ", asked from the top down");
            }

            helper.assertTrue(upward.sample(post.getX(), post.getY(), post.getZ()) >>> 24 == SnapshotRenderer.SOLID,
                    "a bare log post stays in the photograph");
            helper.assertTrue(upward.sample(arbour.getX(), arbour.getY(), arbour.getZ()) >>> 24 == SnapshotRenderer.SOLID,
                    "a post under player-placed leaves stays in the photograph");
            helper.assertTrue(upward.sample(arbour.getX(), arbour.getY() + 6, arbour.getZ()) >>> 24 == SnapshotRenderer.SOLID,
                    "and so do the leaves themselves");
        } finally {
            fill(level, base.offset(-4, 0, -4), max, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /**
     * A wooded base must still produce a photograph of the build. Natural
     * leaf canopy and the trunks feeding it are scenery, not evidence that
     * the camera found the house.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void photoSeesTheHomeThroughNaturalLeafCover(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(0, 1, 540);
        BlockPos bed = buildTestHouse(level, base, true);
        BlockPos upperWindow = base.offset(15, 6, 19);

        // A dense tree-canopy shell around every facade. This reproduces the
        // failure where the scorer happily framed leaves because they were
        // close to the bed and therefore counted as "house" pixels.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
        fill(level, base.offset(8, 0, 8), base.offset(22, 11, 10), leaves);
        fill(level, base.offset(8, 0, 20), base.offset(22, 11, 22), leaves);
        fill(level, base.offset(8, 0, 11), base.offset(10, 11, 19), leaves);
        fill(level, base.offset(20, 0, 11), base.offset(22, 11, 19), leaves);

        // Put several genuine vertical trunks directly across the preferred
        // south-facing composition. Without trunk filtering these are close
        // enough to the bed to score as "house" and can beat the facade.
        for (int x : new int[]{10, 13, 16, 19, 22}) {
            for (int y = 0; y <= 8; y++) {
                level.setBlock(
                        base.offset(x, y, 21),
                        Blocks.OAK_LOG.defaultBlockState(),
                        Block.UPDATE_CLIENTS
                );
            }
        }

        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(
                level.getServer(),
                level,
                UUID.randomUUID(),
                bed,
                bed.offset(0, 0, 60)
        );

        helper.assertTrue(photo != null && photo.pixels() != null && photo.sawHouse(),
                "trees prevented a usable photo of the actual home: " + photo);
        helper.assertTrue(photo.window() != null && !photo.windowCarved(),
                "leaves/trunks were mistaken for the house facade: " + photo);

        BlockPos photographedRealWindow = photo.window().subtract(copyOffset(photo, bed));
        helper.assertTrue(photographedRealWindow.equals(upperWindow),
                "the camera did not find the real upper window through the foliage: " + photographedRealWindow);

        assertValidMap(helper, photo.pixels());
        logPhoto("leaf_cover", photo.pixels());
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

    /** Logs a photo's map colours as hex so CI output can be turned back into an image. */
    private static void logPhoto(String name, byte[] pixels) {
        StringBuilder hex = new StringBuilder(pixels.length * 2);
        for (byte pixel : pixels) {
            hex.append(Character.forDigit((pixel >> 4) & 15, 16)).append(Character.forDigit(pixel & 15, 16));
        }
        TheOldestHouse.LOGGER.info("OTH-PHOTO|{}|{}", name, hex);
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
    // Beds and the labyrinth threshold

    @GameTest(template = "empty")
    public static void bedsStopWorkingPastTheLabyrinthThreshold(GameTestHelper helper) {
        BlockPos origin = new BlockPos(100_000, 64, 100_000);
        helper.assertTrue(!HouseLabyrinth.isBeyondThreshold(origin, origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z - 2)),
                "a bed in the hall should work");
        helper.assertTrue(!HouseLabyrinth.isBeyondThreshold(origin, origin.offset(4, 7, 4)),
                "a bed in an upstairs bedroom should work");
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(origin, origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z + 5)),
                "a bed in the impossible hallway should not work");
        helper.assertTrue(HouseLabyrinth.isBeyondThreshold(helper.getLevel().getServer(), HouseDimensions.OUTSIDE, BlockPos.ZERO),
                "the outside dimension lies past the threshold");
        helper.assertTrue(!HouseLabyrinth.isBeyondThreshold(helper.getLevel().getServer(), Level.OVERWORLD, origin),
                "the Overworld is not the labyrinth");
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
            helper.assertTrue(!Hillary.isAcknowledged(wolf), "a stranger's bone counted as her recipient's greeting");

            recipient.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            recipient.interactOn(wolf, InteractionHand.MAIN_HAND);
            // Compare owner UUIDs: isOwnedBy looks the owner up in the level's
            // player list, which fake players are not part of.
            helper.assertTrue(wolf.isTame() && recipient.getUUID().equals(wolf.getOwnerUUID()),
                    "one bone did not tame Hillary for her recipient");
            helper.assertTrue(Hillary.isAcknowledged(wolf), "the bone should also greet her");
            wolf.discard();
        } finally {
            recipient.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theHouseOnlyEverClearsNaturalGrowth(GameTestHelper helper) {
        helper.assertTrue(HouseSpawnManager.isNaturalGrowth(Blocks.OAK_LOG.defaultBlockState()), "trees can be cleared");
        helper.assertTrue(HouseSpawnManager.isNaturalGrowth(Blocks.OAK_LEAVES.defaultBlockState()), "their leaves too");
        helper.assertTrue(HouseSpawnManager.isNaturalGrowth(Blocks.SHORT_GRASS.defaultBlockState()), "grass");
        helper.assertTrue(HouseSpawnManager.isNaturalGrowth(Blocks.SNOW.defaultBlockState()), "snow");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true)), "leaves a player placed");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.STRIPPED_OAK_LOG.defaultBlockState()), "stripped logs");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.OAK_PLANKS.defaultBlockState()), "planks");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.GLASS.defaultBlockState()), "glass");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.CHEST.defaultBlockState()), "a chest");
        helper.assertTrue(!HouseSpawnManager.isNaturalGrowth(Blocks.COBBLESTONE.defaultBlockState()), "cobblestone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hillaryWaitsToBeGreetedByHerRecipient(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos home = helper.absolutePos(new BlockPos(2, 3, 6));
        level.setBlock(home.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);

        ServerPlayer recipient = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "hillary_greeter"));
        ServerPlayer stranger = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "hillary_passerby"));
        Wolf wolf = Hillary.spawn(level, home, recipient.getUUID());
        helper.assertTrue(wolf != null, "Hillary did not spawn");

        BlockPos manorOrigin = home.offset(28, 0, 0);
        recipient.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        Hillary.tickGuide(level, recipient, wolf.getUUID(), true, manorOrigin);
        helper.assertTrue(wolf.getNavigation().isDone() || wolf.getNavigation().getTargetPos() == null
                        || wolf.getNavigation().getTargetPos().distSqr(home) < 100,
                "Hillary set off for the manor before anyone greeted her");

        stranger.interactOn(wolf, InteractionHand.MAIN_HAND);
        helper.assertTrue(!Hillary.isAcknowledged(wolf), "a stranger's pat counted as her greeting");

        recipient.interactOn(wolf, InteractionHand.MAIN_HAND);
        helper.assertTrue(Hillary.isAcknowledged(wolf), "her recipient's pat should greet her");
        helper.assertTrue(!wolf.isTame(), "a pat is not a bone: she is greeted, not tamed");

        wolf.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hillaryLeadsThenWaitsOutsideTheProxyManor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos home = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlock(home.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);

        ServerPlayer recipient = FakePlayerFactory.get(
                level,
                new GameProfile(UUID.randomUUID(), "hillary_guide_recipient")
        );
        Wolf wolf = Hillary.spawn(level, home, recipient.getUUID());
        helper.assertTrue(wolf != null, "Hillary did not spawn");

        wolf.tame(recipient);
        wolf.setOrderedToSit(false);
        Hillary.acknowledge(wolf);
        recipient.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);

        BlockPos manorOrigin = home.offset(28, 0, 0);
        fill(
                level,
                manorOrigin.offset(HouseLayout.AXIS_X - 3, 0, HouseLayout.FRONT_DOOR_Z - 6),
                manorOrigin.offset(HouseLayout.AXIS_X + 3, 0, HouseLayout.FRONT_DOOR_Z - 1),
                Blocks.STONE.defaultBlockState()
        );
        Hillary.tickGuide(level, recipient, wolf.getUUID(), true, manorOrigin);
        helper.assertTrue(
                wolf.getOwnerUUID() == null,
                "Hillary kept vanilla follow-owner AI while she was supposed to lead"
        );

        Vec3 porch = HouseProxyEntityEvacuation.frontDoorExit(level, manorOrigin);
        helper.assertTrue(porch != null, "no safe visible front-door waiting point");
        wolf.moveTo(porch.x, porch.y, porch.z, 0.0F, 0.0F);
        Hillary.tickGuide(level, recipient, wolf.getUUID(), true, manorOrigin);

        helper.assertTrue(
                recipient.getUUID().equals(wolf.getOwnerUUID()),
                "Hillary did not restore her owner while waiting outside"
        );
        helper.assertTrue(wolf.isOrderedToSit(), "Hillary should wait outside the proxy manor");

        HillaryTag tag = Hillary.tagOf(wolf);
        helper.assertTrue(
                tag != null && tag.home().equals(BlockPos.containing(porch)),
                "Hillary did not adopt the visible front doorstep as her waiting place"
        );

        wolf.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void proxyManorEvacuatesNonPlayerMobs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(0, 4, 520));

        // Give both authored exits a safe patch of ordinary ground.
        fill(
                level,
                origin.offset(HouseLayout.AXIS_X - 4, 0, HouseLayout.FRONT_DOOR_Z - 7),
                origin.offset(HouseLayout.AXIS_X + 4, 0, HouseLayout.FRONT_DOOR_Z + 1),
                Blocks.STONE.defaultBlockState()
        );
        fill(
                level,
                origin.offset(HouseLayout.BACK_DOOR.x() - 4, 0, HouseLayout.BACK_DOOR.z() - 1),
                origin.offset(HouseLayout.BACK_DOOR.x() + 4, 0, HouseLayout.BACK_DOOR.z() + 7),
                Blocks.STONE.defaultBlockState()
        );

        BlockPos inside = origin.offset(
                HouseLayout.AXIS_X,
                1,
                HouseLayout.FRONT_DOOR_Z + 3
        );

        Wolf wolf = EntityType.WOLF.create(level);
        Villager villager = EntityType.VILLAGER.create(level);
        helper.assertTrue(wolf != null && villager != null, "test mobs did not create");

        wolf.moveTo(inside.getX() + 0.35D, inside.getY(), inside.getZ() + 0.35D, 0.0F, 0.0F);
        villager.moveTo(inside.getX() + 0.65D, inside.getY(), inside.getZ() + 0.65D, 0.0F, 0.0F);
        level.addFreshEntity(wolf);
        level.addFreshEntity(villager);

        helper.assertTrue(
                HouseLayout.isInsideDomesticVolume(
                        wolf.getX() - origin.getX(),
                        wolf.getY() - origin.getY(),
                        wolf.getZ() - origin.getZ()
                ),
                "wolf did not begin inside the proxy"
        );
        helper.assertTrue(
                HouseLayout.isInsideDomesticVolume(
                        villager.getX() - origin.getX(),
                        villager.getY() - origin.getY(),
                        villager.getZ() - origin.getZ()
                ),
                "villager did not begin inside the proxy"
        );

        // Fresh entities enter EntitySectionStorage on the following level
        // tick. The evacuation code intentionally uses the world's spatial
        // query, so testing it in the spawn tick is nondeterministic.
        helper.runAfterDelay(1, () -> {
            try {
                int moved = HouseProxyEntityEvacuation.evacuateAll(level, origin);
                helper.assertTrue(moved == 2, "expected two mobs evacuated, got " + moved);

                for (var mob : List.of(wolf, villager)) {
                    helper.assertTrue(
                            !HouseLayout.isInsideDomesticVolume(
                                    mob.getX() - origin.getX(),
                                    mob.getY() - origin.getY(),
                                    mob.getZ() - origin.getZ()
                            ),
                            mob.getName().getString() + " remained trapped inside the proxy at " + mob.blockPosition()
                    );

                    double nearestDoor = Double.MAX_VALUE;
                    for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
                        Vec3 doorCenter = Vec3.atCenterOf(origin.offset(door.x(), door.y(), door.z()));
                        nearestDoor = Math.min(nearestDoor, mob.position().distanceTo(doorCenter));
                    }
                    helper.assertTrue(
                            nearestDoor <= 7.0D,
                            mob.getName().getString() + " was evacuated out of sight instead of visibly outside: " + mob.blockPosition()
                    );
                }

                wolf.discard();
                villager.discard();
                helper.succeed();
            } catch (Throwable failure) {
                wolf.discard();
                villager.discard();
                throw failure;
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void domesticHouseMobsProjectIntoTheOverworldProxy(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerLevel overworld = helper.getLevel();
        ServerLevel interiorStandIn = server.getLevel(Level.NETHER);
        helper.assertTrue(interiorStandIn != null, "test server has no second dimension");

        // Use ordinary valid build height in both dimensions and keep this
        // far away from every other GameTest. The mirror code is coordinate-
        // based, so the Nether can stand in for the House source dimension.
        BlockPos origin = new BlockPos(2_000_000, 64, 2_000_000);
        BlockPos inside = origin.offset(
                HouseLayout.AXIS_X,
                1,
                HouseLayout.FRONT_DOOR_Z + 4
        );
        interiorStandIn.getChunkAt(inside);
        overworld.getChunkAt(inside);

        Wolf resident = EntityType.WOLF.create(interiorStandIn);
        helper.assertTrue(resident != null, "domestic source wolf did not create");
        resident.setCustomName(Component.literal("House Resident"));
        resident.moveTo(
                inside.getX() + 0.5D,
                inside.getY(),
                inside.getZ() + 0.5D,
                37.0F,
                0.0F
        );
        helper.assertTrue(
                HouseLayout.isInsideDomesticVolume(
                        resident.getX() - origin.getX(),
                        resident.getY() - origin.getY(),
                        resident.getZ() - origin.getZ()
                ),
                "test source is not inside the domestic volume"
        );
        helper.assertTrue(
                interiorStandIn.addFreshEntity(resident),
                "domestic source wolf was not added"
        );

        // EntitySectionStorage indexes fresh entities on the following level
        // tick. Test the real query path, not the direct object reference.
        helper.runAfterDelay(1, () -> {
            try {
                helper.assertTrue(resident.isAlive(), "domestic source wolf is not alive after insertion");
                helper.assertTrue(
                        !HouseExteriorEntityMirror.isProjection(resident),
                        "real domestic source was misclassified as a projection"
                );
                helper.assertTrue(
                        !HouseImpossibleHallway.isInteriorOnlyPosition(origin, resident.blockPosition()),
                        "ordinary domestic source was misclassified as impossible-space"
                );

                AABB sourceSearch = new AABB(
                        origin.getX() + HouseLayout.MIN_X - 40,
                        origin.getY() - 6,
                        origin.getZ() + HouseLayout.MIN_Z - 40,
                        origin.getX() + HouseLayout.MAX_X + 41,
                        origin.getY() + 25,
                        origin.getZ() + HouseLayout.MAX_Z + 41
                );
                helper.assertTrue(
                        interiorStandIn.getEntitiesOfClass(Mob.class, sourceSearch).contains(resident),
                        "domestic source wolf is not visible to the source entity query"
                );

                int mirrored = HouseExteriorEntityMirror.syncDomesticToOverworldNow(
                        interiorStandIn,
                        overworld,
                        origin
                );
                helper.assertTrue(mirrored == 1, "expected one domestic source projection, got " + mirrored);

                AABB search = new AABB(inside).inflate(2.0D);
                List<Mob> projections = overworld.getEntitiesOfClass(
                        Mob.class,
                        search,
                        HouseExteriorEntityMirror::isProjection
                );
                helper.assertTrue(
                        projections.size() == 1,
                        "expected one visual projection, got " + projections.size()
                );

                Mob projection = projections.getFirst();
                helper.assertTrue(
                        projection.getType() == resident.getType(),
                        "projection changed entity type"
                );
                helper.assertTrue(
                        projection.getCustomName() != null
                                && "House Resident".equals(projection.getCustomName().getString()),
                        "projection did not preserve visible source state"
                );
                helper.assertTrue(
                        projection.isInvulnerable() && projection.isNoAi() && projection.isSilent(),
                        "projection retained gameplay behavior"
                );
                helper.assertTrue(
                        HouseLayout.isInsideDomesticVolume(
                                projection.getX() - origin.getX(),
                                projection.getY() - origin.getY(),
                                projection.getZ() - origin.getZ()
                        ),
                        "reverse projection was not placed at matching proxy-interior coordinates"
                );

                // Movement is visual state too. The real mob remains the only
                // authoritative entity, but its projection must visibly walk,
                // turn and carry its current stride across the dimension seam.
                resident.moveTo(
                        inside.getX() + 0.75D,
                        inside.getY(),
                        inside.getZ() + 0.80D,
                        123.0F,
                        11.0F
                );
                helper.assertTrue(
                        HouseLayout.isInsideDomesticVolume(
                                resident.getX() - origin.getX(),
                                resident.getY() - origin.getY(),
                                resident.getZ() - origin.getZ()
                        ),
                        "movement test accidentally moved the real mob outside the domestic volume"
                );
                resident.setYHeadRot(147.0F);
                resident.setYBodyRot(109.0F);
                resident.setDeltaMovement(new Vec3(0.18D, 0.0D, -0.07D));

                int movedMirrors = HouseExteriorEntityMirror.syncDomesticToOverworldNow(
                        interiorStandIn,
                        overworld,
                        origin
                );
                helper.assertTrue(movedMirrors == 1, "moving domestic source stopped projecting");

                helper.assertTrue(
                        projection.position().distanceTo(resident.position()) < 1.0E-6D,
                        "projection did not follow the source mob's movement"
                );
                helper.assertTrue(
                        Math.abs(projection.getYRot() - resident.getYRot()) < 0.001F
                                && Math.abs(projection.getXRot() - resident.getXRot()) < 0.001F,
                        "projection did not follow source body orientation"
                );
                helper.assertTrue(
                        Math.abs(projection.getYHeadRot() - resident.getYHeadRot()) < 0.001F
                                && Math.abs(projection.yBodyRot - resident.yBodyRot) < 0.001F,
                        "projection did not follow source head/body turn"
                );
                helper.assertTrue(
                        projection.getDeltaMovement().distanceTo(resident.getDeltaMovement()) < 1.0E-6D,
                        "projection did not carry the source mob's current stride"
                );

                // Geometrically the projection is inside the Overworld proxy,
                // but it is scenery. The real-NPC evacuation rule must not
                // throw it outside.
                int evacuated = HouseProxyEntityEvacuation.evacuateAll(overworld, origin);
                helper.assertTrue(
                        evacuated == 0,
                        "projection was counted as a trapped real Overworld mob"
                );
                helper.assertTrue(
                        !projection.isRemoved()
                                && HouseLayout.isInsideDomesticVolume(
                                        projection.getX() - origin.getX(),
                                        projection.getY() - origin.getY(),
                                        projection.getZ() - origin.getZ()
                                ),
                        "projection was evacuated from the proxy"
                );

                HouseExteriorEntityMirror.clear(server);
                resident.discard();
                helper.succeed();
            } catch (Throwable failure) {
                HouseExteriorEntityMirror.clear(server);
                resident.discard();
                throw failure;
            }
        });
    }



    // ------------------------------------------------------------------

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
