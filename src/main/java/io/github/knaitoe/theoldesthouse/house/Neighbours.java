package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

/**
 * The mods that share the House's world, as far as the House needs to know them (docs/NEIGHBOURS.md). Nothing here names another
 * mod's classes: what they tell the House comes in tags they fill from their own jars and in inter-mod messages whose payloads are
 * {@code java.util.function} types. With no neighbour loaded every tag is empty and nothing holds, and the House is as it was.
 */
public final class Neighbours {
    /** A bed block in this tag counts for sleep and never for home: a bed the reader was given (A Reaping's issued beds). */
    public static final TagKey<Block> NOT_HOME = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "not_home"));
    /**
     * A dimension type in this tag is not the reader's own world (A Reaping's arena, and later its Capitol and train). The House keeps
     * out of it: its curses, returns, tab, weapon history and prizes (docs/NEIGHBOURS.md, R3). Read as {@link #notOwnWorld}, and with
     * the House's own dimensions as {@link HouseDimensions#ownWorld}.
     */
    public static final TagKey<DimensionType> NOT_OWN_WORLD = TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "not_own_world"));
    /**
     * The message a neighbour sends with a {@code BiPredicate<ServerLevel, BlockPos>} that holds on its districts' land and worksites
     * and at least {@link #MARGIN} blocks round them (docs/NEIGHBOURS.md): the House probes a box only every {@link #PROBE} blocks.
     */
    public static final String DISTRICT_LAND = "district_land";
    /** The message the House sends A Reaping with a {@code BiPredicate<ServerLevel, BlockPos>} that holds on the manor's lot. */
    public static final String HOUSE_LAND = "house_land";
    /** A Reaping, the neighbour the House tells where its lot is. A message to a mod that is not loaded is never read. */
    static final String A_REAPING = "a_reaping";
    /**
     * The margin the House keeps from a district: no district's land may lie within this many blocks of the House's envelope, and
     * {@code house_land} holds this far round the manor's lot.
     */
    public static final int MARGIN = 8;
    /**
     * How far apart a box is probed for district land, along each side and always on its far faces. A neighbour's {@code
     * district_land} holds at least {@link #MARGIN} blocks round a district's land and worksites, as the contract asks, so nothing of
     * it lies between two probes; a predicate that held only on a strip narrower than this (a road, one worksite block) could.
     */
    static final int PROBE = 2;

    private static final List<BiPredicate<ServerLevel, BlockPos>> DISTRICTS = new CopyOnWriteArrayList<>();
    private static final List<BiPredicate<ServerLevel, BlockPos>> TEST_DISTRICTS = new CopyOnWriteArrayList<>();
    private static volatile boolean failed;

    private Neighbours() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(Neighbours::onEnqueue);
        modBus.addListener(Neighbours::onProcess);
    }

    private static void onEnqueue(InterModEnqueueEvent event) {
        InterModComms.sendTo(A_REAPING, HOUSE_LAND, () -> (BiPredicate<ServerLevel, BlockPos>) Neighbours::houseLand);
    }

    private static void onProcess(InterModProcessEvent event) {
        event.getIMCStream(DISTRICT_LAND::equals).forEach(Neighbours::district);
    }

    @SuppressWarnings("unchecked")
    private static void district(InterModComms.IMCMessage message) {
        Object payload = message.messageSupplier().get();
        if (payload instanceof BiPredicate<?, ?> predicate) {
            DISTRICTS.add((BiPredicate<ServerLevel, BlockPos>) predicate);
            TheOldestHouse.LOGGER.info("{} told the House where its districts are.", message.senderModId());
        } else {
            TheOldestHouse.LOGGER.warn("{} sent {} with {}, not a BiPredicate<ServerLevel, BlockPos>; it is ignored.",
                    message.senderModId(), DISTRICT_LAND, payload == null ? "nothing" : payload.getClass().getName());
        }
    }

    /**
     * Whether any neighbour's district holds at {@code pos}: its land, its worksites and their margin of at least {@link #MARGIN}. Any
     * number of neighbours may say, and one is enough. A neighbour whose answer throws is taken as not holding, and said once in the
     * log. A neighbour is handed a position of its own, which it may keep: never a cursor the House moves on.
     */
    public static boolean districtLand(ServerLevel level, BlockPos pos) {
        if (!anyDistrict()) {
            return false;
        }
        BlockPos at = pos.immutable();
        return holds(DISTRICTS, level, at) || holds(TEST_DISTRICTS, level, at);
    }

    /**
     * Whether {@code level} is a world a neighbour says is not the reader's own ({@link #NOT_OWN_WORLD}). With no neighbour the tag is
     * empty and this holds nowhere. Read on both sides: the game sends dimension type tags to every client.
     */
    public static boolean notOwnWorld(Level level) {
        return level.dimensionTypeRegistration().is(NOT_OWN_WORLD);
    }

    /** Whether any neighbour has said where its districts are (or a case has installed one). With none, nothing is district land. */
    public static boolean anyDistrict() {
        return !DISTRICTS.isEmpty() || !TEST_DISTRICTS.isEmpty();
    }

    /**
     * Whether any neighbour's district holds anywhere in {@code box}, probed every {@link #PROBE} blocks along each side and on its
     * far faces. With no district, nothing is probed.
     */
    public static boolean districtLandIn(ServerLevel level, BoundingBox box) {
        if (!anyDistrict()) {
            return false;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x : probes(box.minX(), box.maxX())) {
            for (int z : probes(box.minZ(), box.maxZ())) {
                for (int y : probes(box.minY(), box.maxY())) {
                    if (districtLand(level, pos.set(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int[] probes(int min, int max) {
        int steps = (max - min) / PROBE;
        boolean edge = min + steps * PROBE != max;
        int[] at = new int[steps + 1 + (edge ? 1 : 0)];
        for (int i = 0; i <= steps; i++) {
            at[i] = min + i * PROBE;
        }
        if (edge) {
            at[steps + 1] = max;
        }
        return at;
    }

    /**
     * What the House tells A Reaping as {@code house_land}: the manor's lot in the Overworld (every block the House can set,
     * {@link HouseLayout#MIN_X}..{@link HouseLayout#MAX_X} by {@link HouseLayout#MIN_Z}..{@link HouseLayout#MAX_Z} round its origin,
     * at any height) and {@link #MARGIN} blocks round it, from the morning the House comes, standing or fallen. Before then, and
     * anywhere else, it holds nowhere.
     */
    public static boolean houseLand(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).housePosition().orElse(null);
        if (origin == null) {
            return false;
        }
        int x = pos.getX() - origin.getX();
        int z = pos.getZ() - origin.getZ();
        return x >= HouseLayout.MIN_X - MARGIN && x <= HouseLayout.MAX_X + MARGIN
                && z >= HouseLayout.MIN_Z - MARGIN && z <= HouseLayout.MAX_Z + MARGIN;
    }

    private static boolean holds(List<BiPredicate<ServerLevel, BlockPos>> districts, ServerLevel level, BlockPos pos) {
        for (BiPredicate<ServerLevel, BlockPos> district : districts) {
            try {
                if (district.test(level, pos)) {
                    return true;
                }
            } catch (RuntimeException e) {
                if (!failed) {
                    failed = true;
                    TheOldestHouse.LOGGER.warn("A neighbour's district_land failed at {}; it is taken as not holding.", pos, e);
                }
            }
        }
        return false;
    }

    /**
     * For the cases: a district as a neighbour would send it, without the neighbour. The returned handle takes it away again; every
     * case that installs one takes it away in a {@code finally} or an {@code @AfterBatch} hook.
     */
    public static Runnable installDistrictForTesting(BiPredicate<ServerLevel, BlockPos> district) {
        TEST_DISTRICTS.add(district);
        return () -> TEST_DISTRICTS.remove(district);
    }

    /** Takes away every district a case installed. */
    public static void forgetTestDistricts() {
        TEST_DISTRICTS.clear();
    }
}
