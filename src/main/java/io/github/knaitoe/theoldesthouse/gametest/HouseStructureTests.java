package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Generates The Oldest House on the game-test server and checks the
 * architecture the rest of the mod depends on. Run with
 * {@code gradle runGameTestServer}; CI runs it on every push.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HouseStructureTests {
    private HouseStructureTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void houseStructure(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Well clear of the test's own one-block structure, and raised off
        // the superflat test world (ground just above bedrock) so the cellar
        // has room above the world's minimum build height. The foundations
        // carry the house down to the ground.
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(48, 8, 48);

        HouseBuilder.build(level, origin);
        HouseBuilder.applyInteriorContents(level, origin);
        HouseStructureDump.print(level, origin);

        HouseStructureChecks checks = new HouseStructureChecks(level, origin);
        List<String> failures = checks.runAll();

        HouseBuilder.revealImpossibleDoor(level, origin);
        failures.addAll(checks.runAfterReveal());

        for (String failure : failures) {
            TheOldestHouse.LOGGER.error("House structure check failed: {}", failure);
        }
        if (!failures.isEmpty()) {
            helper.fail(failures.size() + " house structure check(s) failed; first: " + failures.get(0));
            return;
        }
        helper.succeed();
    }
}
