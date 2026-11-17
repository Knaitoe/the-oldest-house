package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRooms;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import io.github.knaitoe.theoldesthouse.opening.HomeBed;
import io.github.knaitoe.theoldesthouse.opening.OpeningConfig;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import io.github.knaitoe.theoldesthouse.opening.OpeningStage;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Home is a bed of the reader's own, outside every district (docs/NEIGHBOURS.md): a neighbour's district (installed here as A Reaping
 * would send it) or a bed in {@code #the_oldest_house:not_home} counts for sleep and never for home, and the opening waits for a home.
 * Without a neighbour the House reads a bed of their own as it always has, and remembers the last one they woke in on a morning.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HomeBedTests {
    private HomeBedTests() {
    }

    @AfterBatch(batch = "home_bed")
    public static void homeBedDone(ServerLevel level) {
        Neighbours.forgetTestDistricts();
    }

    @AfterBatch(batch = "home_bed_aging")
    public static void agingDone(ServerLevel level) {
        Neighbours.forgetTestDistricts();
    }

    @AfterBatch(batch = "home_bed_letter")
    public static void letterDone(ServerLevel level) {
        Neighbours.forgetTestDistricts();
    }

    /** A stone floor, and on it a bed facing north whose foot is at {@code rel}; its head, where a reader lies and the respawn is set. */
    private static BlockPos bed(GameTestHelper h, BlockPos rel, Block block) {
        BlockPos foot = h.absolutePos(rel);
        h.setBlock(rel.below(), Blocks.STONE);
        h.setBlock(rel.north().below(), Blocks.STONE);
        NovelRooms.bed(h.getLevel(), foot, block, Direction.NORTH);
        return foot.north();
    }

    private static void floor(GameTestHelper h, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    /** A neighbour's district over the plot's cells from {@code min} to {@code max}, as A Reaping would send it. */
    private static Runnable district(GameTestHelper h, BlockPos min, BlockPos max) {
        BoundingBox box = BoundingBox.fromCorners(h.absolutePos(min), h.absolutePos(max));
        ServerLevel level = h.getLevel();
        return Neighbours.installDistrictForTesting((l, pos) -> l == level && box.isInside(pos));
    }

    /** A morning wake in the bed at {@code head} on House day {@code day}, as the end of a night gives it to the opening. */
    private static void wake(ServerPlayer p, BlockPos head, long day) {
        p.startSleeping(head);
        OpeningSequence.morningWake(p, day);
        p.clearSleepingPos();
    }

    private static void click(ServerPlayer p, BlockPos door) {
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p, InteractionHand.MAIN_HAND, door,
                new BlockHitResult(door.getCenter(), Direction.SOUTH, door, false)));
    }

    /** The tag on {@code block} for the length of a case, as a neighbour's jar would put it there; the handle takes it off again. */
    private static Runnable tagged(Block block, TagKey<Block> tag) throws ReflectiveOperationException {
        Holder.Reference<Block> holder = block.builtInRegistryHolder();
        var before = holder.tags().collect(Collectors.toSet());
        var with = new HashSet<>(before);
        with.add(tag);
        Method bind = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
        bind.setAccessible(true);
        bind.invoke(holder, with);
        return () -> {
            try {
                bind.invoke(holder, before);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @GameTest(template = "empty", batch = "home_bed")
    public static void aBedOnDistrictLandIsNotHomeAndTheOpeningWaitsAtNone(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ServerPlayer p = NativeTestPlayers.survival(h, "district_sleeper");
        Runnable forget = district(h, new BlockPos(0, 0, 0), new BlockPos(15, 8, 15));
        try {
            BlockPos head = bed(h, new BlockPos(4, 1, 6), Blocks.WHITE_BED);
            BlockPos door = h.absolutePos(new BlockPos(8, 1, 4));
            NovelRooms.door(level, door, Direction.SOUTH, Blocks.OAK_DOOR, false);
            p.teleportTo(level, head.getX() + 1.5D, head.getY(), head.getZ() + 0.5D, 0.0F, 0.0F);
            // Lying down in any bed sets the respawn; a bed somebody set down on district land is still not a home.
            p.setRespawnPosition(level.dimension(), head, 0.0F, true, false);
            h.assertTrue(!HomeBed.isHomeBed(level, head) && HomeBed.of(p).isEmpty(), "a bed on a district's land is nobody's home");

            var state = OpeningSequence.state(p);
            long day = OpeningSequence.currentDay(level.getServer());
            for (int night = 0; night < 3; night++) {
                wake(p, head, day + night);
            }
            h.assertTrue(state.nightsSlept() == 0 && state.home() == null,
                    "nights there are not the opening's, and the bed is not remembered: " + state.nightsSlept() + " nights");
            click(p, door);
            h.assertTrue(state.doorUse().isEmpty(), "a door near it is not counted as theirs");

            // Nights enough (as two in the manor would give) and days enough: with no home, the opening still waits.
            state.recordSleep(day + 10);
            state.recordSleep(day + 11);
            OpeningSequence.checkMorning(p, state, state.firstJoinDay() + OpeningConfig.MIN_DAYS_SINCE_JOIN.getAsInt() + 12);
            h.assertTrue(state.nightsSlept() >= OpeningConfig.MIN_NIGHTS_SLEPT.getAsInt() && state.stage() == OpeningStage.NONE,
                    "with no home of their own, the opening waits at NONE: " + state.stage());
            String advanced = OpeningSequence.advance(p);
            h.assertTrue(advanced.startsWith("no home bed") && state.stage() == OpeningStage.NONE, "and nothing moves it on: " + advanced);
        } finally {
            forget.run();
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "home_bed")
    public static void aBedInNotHomeIsNotHome(GameTestHelper h) throws ReflectiveOperationException {
        ServerLevel level = h.getLevel();
        h.assertTrue(level.registryAccess().registryOrThrow(Registries.BLOCK).getTag(Neighbours.NOT_HOME).isPresent(),
                "the House declares #the_oldest_house:not_home, for a neighbour to fill");
        ServerPlayer p = NativeTestPlayers.survival(h, "given_a_bed");
        Runnable untag = tagged(Blocks.LIME_BED, Neighbours.NOT_HOME);
        try {
            BlockPos given = bed(h, new BlockPos(3, 1, 6), Blocks.LIME_BED);
            BlockPos own = bed(h, new BlockPos(9, 1, 6), Blocks.WHITE_BED);
            p.teleportTo(level, given.getX() + 1.5D, given.getY(), given.getZ() + 0.5D, 0.0F, 0.0F);
            p.setRespawnPosition(level.dimension(), given, 0.0F, true, false);
            h.assertTrue(!HomeBed.isHomeBed(level, given) && HomeBed.isHomeBed(level, own) && HomeBed.of(p).isEmpty(),
                    "a bed in #not_home is not a home; the bed beside it, of nobody's issue, could be");
            var state = OpeningSequence.state(p);
            long day = OpeningSequence.currentDay(level.getServer());
            wake(p, given, day);
            wake(p, given, day + 1);
            h.assertTrue(state.nightsSlept() == 0 && state.home() == null, "nights in it are not the opening's: " + state.nightsSlept());
        } finally {
            untag.run();
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "home_bed")
    public static void aHomeBedOutsideEveryDistrictWorksAsItAlwaysHas(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ServerPlayer p = NativeTestPlayers.survival(h, "home_sleeper");
        BlockPos doorRel = new BlockPos(8, 1, 4);
        BlockPos door = h.absolutePos(doorRel);
        Consumer<PlayerInteractEvent.RightClickBlock> refuse = event -> {
            if (event.getPos().equals(door)) {
                event.setCanceled(true);
            }
        };
        try {
            BlockPos head = bed(h, new BlockPos(4, 1, 6), Blocks.WHITE_BED);
            NovelRooms.door(level, door, Direction.SOUTH, Blocks.OAK_DOOR, false);
            p.teleportTo(level, head.getX() + 1.5D, head.getY(), head.getZ() + 0.5D, 0.0F, 0.0F);
            p.setRespawnPosition(level.dimension(), head, 0.0F, true, false);
            h.assertTrue(HomeBed.isHomeBed(level, head) && HomeBed.of(p).equals(Optional.of(head)),
                    "with no neighbour, a bed of their own is home, as the respawn always made it");

            var state = OpeningSequence.state(p);
            long day = OpeningSequence.currentDay(level.getServer());
            wake(p, head, day);
            wake(p, head, day);
            h.assertTrue(state.nightsSlept() == 1, "a night at home counts once a day: " + state.nightsSlept());
            wake(p, head, day + 1);
            var home = HomeBed.home(p).orElseThrow();
            h.assertTrue(state.nightsSlept() == 2 && home.pos().equals(head) && home.since() == day && home.nights() == 2,
                    "two nights at home, and the House knows how long it has been theirs: " + home);

            click(p, door);
            click(p, door);
            h.assertTrue(state.doorUse().size() == 1 && state.doorUse().get(0).count() == 2
                            && door.equals(Doorsteps.mostUsedDoor(level, state, head, OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt())),
                    "the door they use near home is counted: " + state.doorUse());
            // A click something else refused is no use of the door.
            NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, PlayerInteractEvent.RightClickBlock.class, refuse);
            click(p, door);
            h.assertTrue(state.doorUse().get(0).count() == 2, "a refused click is not counted: " + state.doorUse());

            OpeningSequence.checkMorning(p, state, state.firstJoinDay() + OpeningConfig.MIN_DAYS_SINCE_JOIN.getAsInt());
            h.assertTrue(state.stage() == OpeningStage.ELIGIBLE, "settled in at home, they are eligible for the letter: " + state.stage());
        } finally {
            NeoForge.EVENT_BUS.unregister(refuse);
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "home_bed")
    public static void theHomeIsTheBedTheyLastWokeInOnAMorning(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ServerPlayer p = NativeTestPlayers.survival(h, "remembered_home");
        Runnable forget = district(h, new BlockPos(10, 0, 0), new BlockPos(15, 8, 15));
        try {
            BlockPos home = bed(h, new BlockPos(3, 1, 6), Blocks.WHITE_BED);
            BlockPos given = bed(h, new BlockPos(13, 1, 6), Blocks.WHITE_BED);
            p.teleportTo(level, home.getX() + 1.5D, home.getY(), home.getZ() + 0.5D, 0.0F, 0.0F);
            var state = OpeningSequence.state(p);
            long day = OpeningSequence.currentDay(level.getServer());

            p.setRespawnPosition(level.dimension(), home, 0.0F, true, false);
            wake(p, home, day);
            h.assertTrue(state.home() != null && state.home().pos().equals(home) && state.home().since() == day && state.home().nights() == 1,
                    "a morning in their own bed makes it the home the House remembers: " + state.home());

            // The next night they sleep where they were put, and the respawn goes with them, as lying in any bed sets it.
            p.setRespawnPosition(level.dimension(), given, 0.0F, true, false);
            wake(p, given, day + 1);
            var known = HomeBed.home(p).orElseThrow();
            h.assertTrue(known.pos().equals(home) && known.since() == day && known.nights() == 1 && state.nightsSlept() == 1,
                    "their home is still the bed they last woke in on a morning, and the night where they were put was not one of its: " + known);

            wake(p, home, day + 2);
            known = HomeBed.home(p).orElseThrow();
            h.assertTrue(known.pos().equals(home) && known.nights() == 2 && state.nightsSlept() == 2, "back in it, its nights go on: " + known);

            // Another bed of their own, woken in on a morning, is home from that day.
            BlockPos other = bed(h, new BlockPos(6, 1, 12), Blocks.WHITE_BED);
            p.setRespawnPosition(level.dimension(), other, 0.0F, true, false);
            h.assertTrue(HomeBed.home(p).orElseThrow().equals(new HomeBed.Home(other, -1L, 0)),
                    "a bed of their own that holds their respawn is home before they have woken in it");
            wake(p, other, day + 3);
            known = HomeBed.home(p).orElseThrow();
            h.assertTrue(known.pos().equals(other) && known.since() == day + 3 && known.nights() == 1, "and from the morning they wake in it: " + known);

            // Gone (broken where the House can see it), the remembered bed is no home.
            p.setRespawnPosition(level.dimension(), given, 0.0F, true, false);
            level.setBlock(other, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(other.south(), Blocks.AIR.defaultBlockState(), 3);
            h.assertTrue(HomeBed.of(p).isEmpty(), "with the bed gone and only the district's bed left, they have no home");
        } finally {
            forget.run();
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "home_bed_aging")
    public static void sleepInADistrictStillAgesTheHouse(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        var server = level.getServer();
        var storage = server.overworld().getDataStorage();
        HouseSavedData before = HouseSavedData.get(server);
        HouseSavedData house = new HouseSavedData();
        house.markSpawned(new BlockPos(28800, 80, 28800));
        house.incrementVisitCount();
        long time = server.overworld().getDayTime();
        ServerPlayer p = NativeTestPlayers.survival(h, "district_morning");
        Runnable forget = district(h, new BlockPos(0, 0, 0), new BlockPos(15, 8, 15));
        try {
            storage.set("the_oldest_house", house);
            BlockPos head = bed(h, new BlockPos(4, 1, 6), Blocks.WHITE_BED);
            p.teleportTo(level, head.getX() + 1.5D, head.getY(), head.getZ() + 0.5D, 0.0F, 0.0F);
            p.setRespawnPosition(level.dimension(), head, 0.0F, true, false);
            p.startSleeping(head);
            server.overworld().setDayTime(Math.floorDiv(time, 24000L) * 24000L + 24500L);
            int age = house.houseAge();
            NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(p, false, false));
            h.assertTrue(house.houseAge() == age + 1, "a morning in a district's bed is still a morning: the House is a day older (" + house.houseAge() + ")");
            h.assertTrue(OpeningSequence.state(p).nightsSlept() == 0 && HomeBed.of(p).isEmpty(), "but it was not a night at home");
        } finally {
            p.clearSleepingPos();
            server.overworld().setDayTime(time);
            storage.set("the_oldest_house", before);
            forget.run();
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "home_bed_letter")
    public static void theLetterNeverLandsOnDistrictLand(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ServerPlayer outside = NativeTestPlayers.survival(h, "letter_outside");
        ServerPlayer inside = NativeTestPlayers.survival(h, "letter_inside");
        BoundingBox land = BoundingBox.fromCorners(h.absolutePos(new BlockPos(8, 0, 0)), h.absolutePos(new BlockPos(15, 8, 15)));
        Runnable forget = district(h, new BlockPos(8, 0, 0), new BlockPos(15, 8, 15));
        AABB plot = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(24.0D);
        try {
            floor(h, 16);
            // A reader whose only bed is on the district's land: no home, no letter.
            BlockPos given = bed(h, new BlockPos(12, 1, 12), Blocks.WHITE_BED);
            inside.teleportTo(level, given.getX() + 1.5D, given.getY(), given.getZ() + 0.5D, 0.0F, 0.0F);
            inside.setRespawnPosition(level.dimension(), given, 0.0F, true, false);
            String advanced = OpeningSequence.advance(inside);
            h.assertTrue(advanced.startsWith("no home bed"), "the House sends nothing to a reader with no home of their own: " + advanced);

            // A reader at home just outside it, whose most-used door is on the district's side of the line.
            BlockPos home = bed(h, new BlockPos(5, 1, 6), Blocks.WHITE_BED);
            BlockPos door = h.absolutePos(new BlockPos(9, 1, 5));
            NovelRooms.door(level, door, Direction.EAST, Blocks.OAK_DOOR, false);
            outside.teleportTo(level, home.getX() - 0.5D, home.getY(), home.getZ() + 0.5D, 0.0F, 0.0F);
            outside.setRespawnPosition(level.dimension(), home, 0.0F, true, false);
            var state = OpeningSequence.state(outside);
            for (int i = 0; i < 3; i++) {
                click(outside, door);
            }
            h.assertTrue(state.doorUse().size() == 1 && Doorsteps.mostUsedDoor(level, state, home, OpeningConfig.DOORSTEP_SEARCH_RADIUS.getAsInt()) == null,
                    "the door they use most is counted, and never chosen while it stands on a district's land: " + state.doorUse());
            boolean delivered = OpeningSequence.deliverLetter(outside, state, home, OpeningSequence.currentDay(level.getServer()), null);
            var letters = level.getEntitiesOfClass(DeliveredItemEntity.class, plot, e -> outside.getUUID().equals(e.getTarget()));
            h.assertTrue(delivered && !letters.isEmpty(), "the letter is left for the reader at home");
            for (var letter : letters) {
                h.assertTrue(!land.isInside(letter.blockPosition()) && !Neighbours.districtLand(level, letter.blockPosition()),
                        "and nothing of it lands on the district's land: " + letter.blockPosition());
            }
            h.assertTrue(level.getEntitiesOfClass(DeliveredItemEntity.class, plot, e -> inside.getUUID().equals(e.getTarget())).isEmpty(),
                    "nothing was left for the reader in the district");
            letters.forEach(DeliveredItemEntity::discard);
        } finally {
            forget.run();
            NativeTestPlayers.remove(outside);
            NativeTestPlayers.remove(inside);
        }
        h.succeed();
    }
}
