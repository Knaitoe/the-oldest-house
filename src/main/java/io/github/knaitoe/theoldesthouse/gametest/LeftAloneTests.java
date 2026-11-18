package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkFollows;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleController;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays;
import io.github.knaitoe.theoldesthouse.labyrinth.TallyCounterItem;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A neighbour's people are left alone, and the House's are not fought (docs/NEIGHBOURS.md, R5 and R9). The House declares the tags it
 * reads and fills the two A Reaping reads from its own jar: its dimension types in {@code #a_reaping:sheltered}, every living one of its
 * entity types in {@code #a_reaping:not_fighters}. A type a neighbour lists in {@code #the_oldest_house:left_alone} is never projected by
 * the mirror, killed by Harrigan's call or counted by the tally counter; Hillary carries {@code a_reaping.not_a_fighter} while she
 * guides; and a type listed in {@code #the_oldest_house:hunted} is game to the elk's men. Here vanilla types stand in for a neighbour's,
 * put in a tag for a moment as a neighbour's jar would list them, and taken out again to show the House as it is without one.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LeftAloneTests {
    private LeftAloneTests() {
    }

    private static final TagKey<DimensionType> SHELTERED = TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath("a_reaping", "sheltered"));
    private static final TagKey<EntityType<?>> NOT_FIGHTERS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("a_reaping", "not_fighters"));

    private static final String MIRROR = "left_alone_mirror";
    private static NativeTestChunks mirrorChunks;
    private static final List<Entity> MIRRORED = new ArrayList<>();

    @AfterBatch(batch = MIRROR)
    public static void theMirrorCloses(ServerLevel level) {
        HouseExteriorEntityMirror.clear(level.getServer());
        MIRRORED.forEach(Entity::discard);
        MIRRORED.clear();
        if (mirrorChunks != null) {
            mirrorChunks.close();
            mirrorChunks = null;
        }
        NeighbourTags.bind(EntityType.PIG.builtInRegistryHolder(), Neighbours.LEFT_ALONE, false);
    }

    /**
     * The tally counter's and Hillary's cases: their chunks are held, since the one-block template forces only its own and a body a
     * few blocks off may stand in the next, which never ticks its entities nor answers a search.
     */
    private static final String WATCH = "left_alone_watch";
    private static final List<NativeTestChunks> HELD = new ArrayList<>();

    @AfterBatch(batch = WATCH)
    public static void theWatchEnds(ServerLevel level) {
        HELD.forEach(NativeTestChunks::close);
        HELD.clear();
        NeighbourTags.bind(EntityType.ZOMBIE.builtInRegistryHolder(), Neighbours.LEFT_ALONE, false);
    }

    /** Holds the chunks round {@code box} and runs {@code then} once they tick their entities. */
    private static void held(GameTestHelper h, AABB box, BlockPos[] at, Runnable then) {
        NativeTestChunks chunks = new NativeTestChunks();
        HELD.add(chunks);
        chunks.hold(h.getLevel(), box);
        SceneReady.when(h, () -> chunks.entityTicking(h.getLevel(), at) ? null : "the case's chunks ticking their entities", 2, 300, then);
    }

    // ------------------------------------------------------------------------------------------------ the vocabulary
    /**
     * The tags the House reads are declared, empty, so it is as it was without a neighbour; the two it fills for A Reaping hold its own
     * dimension types, and every living one of its entity types and nothing else. A new living type left out of the file fails here,
     * by name.
     */
    @GameTest(template = "empty")
    public static void theHouseDeclaresItsTagsAndFillsANeighboursFromItsOwnJar(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        var dimensionTypes = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        h.assertTrue(BuiltInRegistries.ENTITY_TYPE.getTag(Neighbours.LEFT_ALONE).map(t -> t.size() == 0).orElse(false), "#the_oldest_house:left_alone is declared and empty");
        h.assertTrue(BuiltInRegistries.ENTITY_TYPE.getTag(Neighbours.HUNTED).map(t -> t.size() == 0).orElse(false), "#the_oldest_house:hunted is declared and empty");
        h.assertTrue(BuiltInRegistries.BLOCK.getTag(Neighbours.NOT_HOME).map(t -> t.size() == 0).orElse(false), "#the_oldest_house:not_home is declared and empty");
        h.assertTrue(dimensionTypes.getTag(Neighbours.NOT_OWN_WORLD).map(t -> t.size() == 0).orElse(false), "#the_oldest_house:not_own_world is declared and empty");

        Set<ResourceLocation> house = dimensionTypes.keySet().stream().filter(k -> k.getNamespace().equals(TheOldestHouse.MOD_ID)).collect(Collectors.toCollection(TreeSet::new));
        Set<ResourceLocation> sheltered = dimensionTypes.getTag(SHELTERED).map(t -> t.stream().map(x -> x.unwrapKey().orElseThrow().location()).collect(Collectors.toCollection(TreeSet::new))).orElse(new TreeSet<>());
        h.assertTrue(house.size() == 3 && sheltered.equals(house), "#a_reaping:sheltered holds the House's dimension types, all of them and no other: " + sheltered + " for " + house);
        // The test server makes no level of the House's own (its cases stand the Nether in), so the dimension types are asked.
        for (ResourceKey<Level> dimension : List.of(HouseDimensions.INTERIOR, HouseDimensions.OUTSIDE, HouseDimensions.BETWEEN)) {
            var type = dimensionTypes.getHolder(ResourceKey.create(Registries.DIMENSION_TYPE, dimension.location()));
            h.assertTrue(type.isPresent() && type.get().is(SHELTERED), dimension.location() + "'s dimension type is sheltered");
        }
        h.assertTrue(!server.overworld().dimensionTypeRegistration().is(SHELTERED), "the Overworld is not");

        List<String> missing = new ArrayList<>(), extra = new ArrayList<>(), unmade = new ArrayList<>();
        Set<EntityType<?>> listed = BuiltInRegistries.ENTITY_TYPE.getTag(NOT_FIGHTERS).map(t -> t.stream().map(Holder::value).collect(Collectors.<EntityType<?>>toSet())).orElse(Set.of());
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!key.getNamespace().equals(TheOldestHouse.MOD_ID)) {
                continue;
            }
            Entity made = type.create(h.getLevel());
            if (made == null) {
                unmade.add(key.toString());
                continue;
            }
            boolean living = made instanceof LivingEntity;
            made.discard();
            if (living && !listed.contains(type)) {
                missing.add(key.toString());
            } else if (!living && listed.contains(type)) {
                extra.add(key.toString());
            }
        }
        h.assertTrue(unmade.isEmpty(), "every House entity type can be made to be asked: " + unmade);
        h.assertTrue(missing.isEmpty(), "every living House entity type is in #a_reaping:not_fighters; missing " + missing);
        h.assertTrue(extra.isEmpty(), "and nothing that is not living: " + extra);
        h.assertTrue(listed.stream().allMatch(t -> BuiltInRegistries.ENTITY_TYPE.getKey(t).getNamespace().equals(TheOldestHouse.MOD_ID)), "the House lists only its own");
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ the mirror
    /**
     * The mirror projects the manor's people into the Overworld's manor, and a neighbour's people never: here a pig stands in the hall
     * beside a wolf, and while pigs are listed in {@code #left_alone} only the wolf is projected. What it copies of the wolf carries
     * none of the wolf's own NeoForge data, a neighbour's or the House's. Out of the tag, the pig is projected as anyone is.
     */
    @GameTest(template = "empty", batch = MIRROR, timeoutTicks = 1200)
    public static void theMirrorLeavesANeighboursPeopleAloneAndCopiesNoOnesData(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        ServerLevel overworld = h.getLevel();
        ServerLevel standIn = server.getLevel(Level.NETHER);
        // The mirror is coordinate-based, so the Nether stands in for the House's interior, far from every other case.
        BlockPos origin = new BlockPos(2_100_000, 64, 2_100_000);
        BlockPos inside = origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.FRONT_DOOR_Z + 4);
        BlockPos beside = inside.offset(0, 0, 1);
        mirrorChunks = new NativeTestChunks();
        ChunkPos chunk = new ChunkPos(inside);
        BlockPos centre = new BlockPos(chunk.getMinBlockX() + 8, inside.getY(), chunk.getMinBlockZ() + 8);
        mirrorChunks.hold(standIn, new AABB(centre));
        mirrorChunks.hold(overworld, new AABB(centre));
        SceneReady.when(h, () -> mirrorChunks.entityTicking(standIn, inside, beside) && mirrorChunks.entityTicking(overworld, inside, beside) ? null : "the hall's chunk in both worlds", 2, 1100, () -> {
            for (BlockPos at : List.of(inside, beside)) {
                h.assertTrue(HouseLayout.isInsideDomesticVolume(at.getX() + .5 - origin.getX(), at.getY() - origin.getY(), at.getZ() + .5 - origin.getZ()), at + " is in the manor's rooms");
            }
            Wolf wolf = EntityType.WOLF.create(standIn);
            wolf.setNoGravity(true);
            wolf.setNoAi(true);
            wolf.moveTo(inside.getX() + .5, inside.getY(), inside.getZ() + .5, 0F, 0F);
            wolf.getPersistentData().putString("a_reaping_kin", "the district's");
            wolf.getPersistentData().putBoolean(Hillary.INTRO_DONE, true);
            var pig = EntityType.PIG.create(standIn);
            pig.setNoGravity(true);
            pig.setNoAi(true);
            pig.moveTo(beside.getX() + .5, beside.getY(), beside.getZ() + .5, 0F, 0F);
            h.assertTrue(standIn.addFreshEntity(wolf) && standIn.addFreshEntity(pig), "the wolf and the pig are in the hall");
            MIRRORED.add(wolf);
            MIRRORED.add(pig);
            AABB hall = new AABB(inside).inflate(3);

            NeighbourTags.bind(EntityType.PIG.builtInRegistryHolder(), Neighbours.LEFT_ALONE, true);
            int mirrored = HouseExteriorEntityMirror.syncDomesticToOverworldNow(standIn, overworld, origin);
            List<Mob> projections = overworld.getEntitiesOfClass(Mob.class, hall, HouseExteriorEntityMirror::isProjection);
            h.assertTrue(mirrored == 1 && projections.size() == 1 && projections.getFirst().getType() == EntityType.WOLF,
                    "with pigs in #left_alone only the wolf is projected: " + mirrored + " " + projections.stream().map(m -> m.getType().toShortString()).toList());
            CompoundTag copied = projections.getFirst().getPersistentData();
            h.assertTrue(copied.isEmpty() && wolf.getPersistentData().contains("a_reaping_kin"),
                    "the projection carries none of the wolf's NeoForge data, which the wolf keeps: " + copied);

            NeighbourTags.bind(EntityType.PIG.builtInRegistryHolder(), Neighbours.LEFT_ALONE, false);
            mirrored = HouseExteriorEntityMirror.syncDomesticToOverworldNow(standIn, overworld, origin);
            projections = overworld.getEntitiesOfClass(Mob.class, hall, HouseExteriorEntityMirror::isProjection);
            h.assertTrue(mirrored == 2 && projections.stream().anyMatch(m -> m.getType() == EntityType.PIG),
                    "out of the tag the pig is projected as anyone is: " + mirrored);
            h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ Harrigan and the counter
    /** Harrigan's call refuses a neighbour's people, by kind or by name; out of the tag a zombie is fair game, as it always was. */
    @GameTest(template = "empty")
    public static void harrigansCallRefusesANeighboursPeople(GameTestHelper h) {
        Entity probe = EntityType.ZOMBIE.create(h.getLevel());
        try {
            NeighbourTags.listed(EntityType.ZOMBIE.builtInRegistryHolder(), Neighbours.LEFT_ALONE, true, () -> {
                h.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, false, probe) == HarriganVignette.TargetKind.FORBIDDEN, "a kind in #left_alone is refused");
                h.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, true, probe) == HarriganVignette.TargetKind.FORBIDDEN, "and one by name");
            });
            h.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, false, probe) == HarriganVignette.TargetKind.HOSTILE, "out of it a zombie is hostile");
            h.assertTrue(HarriganVignette.classify(EntityType.ZOMBIE, true, probe) == HarriganVignette.TargetKind.FRIENDLY, "and a named one friendly, as ever");
        } finally {
            probe.discard();
        }
        h.succeed();
    }

    /** The tally counter does not count a neighbour's people among what is out there; out of the tag it counts the zombie. */
    @GameTest(template = "empty", batch = WATCH, timeoutTicks = 400)
    public static void theTallyCounterDoesNotCountANeighboursPeople(GameTestHelper h) {
        BlockPos at = h.absolutePos(new BlockPos(1, 2, 1));
        BlockPos there = h.absolutePos(new BlockPos(4, 2, 4));
        held(h, new AABB(at).minmax(new AABB(there)), new BlockPos[] { at, there }, () -> {
            ServerPlayer p = NativeTestPlayers.survival(h, "tally_beside_a_neighbour");
            p.teleportTo(h.getLevel(), at.getX() + .5, at.getY(), at.getZ() + .5, 0F, 0F);
            Mob zombie = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
            try {
                h.assertTrue(TallyCounterItem.nearbyHostiles(p, 16).contains(zombie), "a zombie is out there");
                NeighbourTags.listed(EntityType.ZOMBIE.builtInRegistryHolder(), Neighbours.LEFT_ALONE, true,
                        () -> h.assertTrue(TallyCounterItem.nearbyHostiles(p, 16).isEmpty(), "but not while its kind is in #left_alone"));
                h.assertTrue(TallyCounterItem.nearbyHostiles(p, 16).contains(zombie), "out of it again the zombie is counted");
            } finally {
                zombie.discard();
                NativeTestPlayers.remove(p);
            }
            h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ Hillary
    /**
     * Hillary carries {@code a_reaping.not_a_fighter} while she guides, from her reader's doorstep until her introduction to the manor
     * is done, and in the finale if she is the guide; never as a Mother's pet; and it comes off after. With her reader online, she
     * stops guiding once they have been inside, as her walk to the manor does.
     */
    @GameTest(template = "empty", batch = WATCH, timeoutTicks = 400)
    public static void hillaryIsNoFighterWhileSheGuides(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos home = h.absolutePos(new BlockPos(2, 2, 2));
        BlockPos second = h.absolutePos(new BlockPos(6, 2, 6));
        // She is ticked, and the tag kept, only where her chunk ticks its entities.
        held(h, new AABB(home).minmax(new AABB(second)), new BlockPos[] { home, second }, () -> hillaryGuides(h, level, home, second));
    }

    private static void hillaryGuides(GameTestHelper h, ServerLevel level, BlockPos home, BlockPos second) {
        level.setBlock(home.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(second.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        Wolf away = Hillary.spawn(level, home, UUID.randomUUID());
        ServerPlayer reader = NativeTestPlayers.survival(h, "hillary_not_a_fighter");
        Wolf hers = Hillary.spawn(level, second, reader.getUUID());
        h.assertTrue(away != null && hers != null, "both Hillaries came");
        var tagged = (java.util.function.Predicate<Wolf>) w -> w.getTags().contains(Neighbours.NOT_A_FIGHTER);
        SceneReady.when(h, () -> tagged.test(away) && tagged.test(hers) ? null : "the tag on her at her reader's doorstep (ticked " + away.tickCount + " and "
                + hers.tickCount + ", guiding " + Hillary.guiding(away) + " and " + Hillary.guiding(hers) + ")", 1, 40, () -> {
            away.getPersistentData().putBoolean(Hillary.INTRO_DONE, true);
            OpeningSequence.state(reader).markEntered();
            SceneReady.when(h, () -> !tagged.test(away) && !tagged.test(hers) ? null : "the tag off once she has done her guiding", 1, 40, () -> {
                away.getPersistentData().putBoolean(FinaleController.GUIDE, true);
                SceneReady.when(h, () -> tagged.test(away) ? null : "the tag on her as the finale's guide", 1, 40, () -> {
                    away.getPersistentData().remove(FinaleController.GUIDE);
                    away.getPersistentData().remove(Hillary.INTRO_DONE);
                    away.addTag(MotherOfStrays.PET);
                    SceneReady.when(h, () -> !tagged.test(away) ? null : "the tag off her as a Mother's pet", 1, 40, () -> {
                        away.discard();
                        hers.discard();
                        NativeTestPlayers.remove(reader);
                        h.succeed();
                    });
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ game
    /**
     * The owner's decision, "the house counts kills from the game": a neighbour's game animals, listed in {@code #hunted}, are counted
     * by the elk's men beside the animals of the reader's world, by the game's own statistics, named by their own name; one killed in
     * the reader's world while she follows them is owed in kind. Out of the tag a bat is nobody's game.
     */
    @GameTest(template = "empty", batch = "left_alone_hunted", timeoutTicks = 200)
    public static void theElkCountsANeighboursGameAsGame(GameTestHelper h) {
        ServerPlayer p = NativeTestPlayers.survival(h, "elk_hunted");
        LabyrinthData d = LabyrinthData.get(h.getLevel().getServer());
        Holder.Reference<EntityType<?>> bat = EntityType.BAT.builtInRegistryHolder();
        try {
            CompoundTag own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
            own.putBoolean(ElkFollows.FOLLOWS, true);
            own.putInt(ElkFollows.BASE, 0);
            LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, own);
            var first = h.spawnWithNoFreeWill(EntityType.BAT, new BlockPos(2, 3, 2));
            first.hurt(h.getLevel().damageSources().playerAttack(p), 100);
            h.assertTrue(!first.isAlive() && ElkFollows.kills(p) == 0 && ElkFollows.accusation(p).startsWith("Never even shot a deer"),
                    "out of the tag a bat is nobody's game: " + ElkFollows.accusation(p));
            h.assertTrue(LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 0, "and is owed nothing in kind");

            NeighbourTags.bind(bat, Neighbours.HUNTED, true);
            h.assertTrue(ElkFollows.kills(p) == 1 && ElkFollows.accusation(p).equals("One bat, %s. We counted."),
                    "listed in #hunted, the bat already killed is counted by the game's own record: " + ElkFollows.accusation(p));
            var second = h.spawnWithNoFreeWill(EntityType.BAT, new BlockPos(4, 3, 4));
            second.hurt(h.getLevel().damageSources().playerAttack(p), 100);
            h.assertTrue(!second.isAlive() && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 1,
                    "one killed while she follows is owed in kind");
            h.assertTrue(ElkFollows.kills(p) == 2 && ElkFollows.accusation(p).equals("Two bats, %s. We counted."), "and counted: " + ElkFollows.accusation(p));
        } finally {
            NeighbourTags.bind(bat, Neighbours.HUNTED, false);
            LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, new CompoundTag());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }
}
