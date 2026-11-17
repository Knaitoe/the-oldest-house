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
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

/**
 * The mods that share the House's world, as far as the House needs to know them (docs/NEIGHBOURS.md). Nothing here names another
 * mod's classes: what they tell the House comes in tags they fill from their own jars and in inter-mod messages whose payloads are
 * {@code java.util.function} types. With no neighbour loaded every tag is empty and nothing holds, and the House is as it was.
 */
public final class Neighbours {
    /** A bed block in this tag counts for sleep and never for home: a bed the reader was given (A Reaping's issued beds). */
    public static final TagKey<Block> NOT_HOME = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "not_home"));
    /** The message a neighbour sends with a {@code BiPredicate<ServerLevel, BlockPos>} that holds on its districts' land. */
    public static final String DISTRICT_LAND = "district_land";

    private static final List<BiPredicate<ServerLevel, BlockPos>> DISTRICTS = new CopyOnWriteArrayList<>();
    private static final List<BiPredicate<ServerLevel, BlockPos>> TEST_DISTRICTS = new CopyOnWriteArrayList<>();
    private static volatile boolean failed;

    private Neighbours() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(Neighbours::onProcess);
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
     * Whether any neighbour's district holds at {@code pos}: its land, its worksites and their margin. Any number of neighbours may
     * say, and one is enough. A neighbour whose answer throws is taken as not holding, and said once in the log.
     */
    public static boolean districtLand(ServerLevel level, BlockPos pos) {
        return holds(DISTRICTS, level, pos) || holds(TEST_DISTRICTS, level, pos);
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
