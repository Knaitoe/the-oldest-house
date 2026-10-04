package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fire and explosions from outside cannot destroy the impossible hallway. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HallwayShieldTests {
    private HallwayShieldTests() {}

    @GameTest(template = "empty", batch = "hallway_shield", timeoutTicks = 100)
    public static void theHallwayNeitherBurnsNorBlowsApart(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var interior = HouseTestLevel.get(server, HouseDimensions.INTERIOR);
        var storage = server.overworld().getDataStorage();
        var old = HouseSavedData.get(server);
        var origin = new BlockPos(64000, 80, 64000);
        var chunks = new NativeTestChunks();
        var fireTick = interior.getGameRules().getRule(GameRules.RULE_DOFIRETICK);
        boolean oldFireTick = fireTick.get();
        fireTick.set(true, server);
        try {
            var house = new HouseSavedData();
            house.markSpawned(origin);
            house.markImpossibleDoorRevealed();
            storage.set("the_oldest_house", house);
            int z = HouseImpossibleHallway.START_Z_OFFSET + 20;
            chunks.hold(interior, new AABB(origin.offset(HouseLayout.AXIS_X, 0, z)).inflate(16));

            // A plank wall of the hallway with a fire burning right outside it.
            BlockPos wall = origin.offset(HouseLayout.AXIS_X - 2, 2, z);
            BlockPos fire = wall.west();
            interior.setBlock(wall, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            interior.setBlock(fire.below(), Blocks.STONE.defaultBlockState(), 3);
            h.assertTrue(HouseImpossibleHallway.isShielded(interior, wall) && !HouseImpossibleHallway.isShielded(interior, fire),
                    "the wall is the hallway's, the fire is outside it");
            var random = RandomSource.create(5);
            for (int i = 0; i < 400; i++) {
                if (!interior.getBlockState(fire).is(Blocks.FIRE))
                    interior.setBlock(fire, BaseFireBlock.getState(interior, fire), 3);
                interior.getBlockState(fire).tick(interior, fire, random);
            }
            h.assertTrue(interior.getBlockState(wall).is(Blocks.SPRUCE_PLANKS), "fire outside the hallway does not burn its wall");

            // A fire lit inside the hallway goes out.
            BlockPos inside = origin.offset(HouseLayout.AXIS_X, 1, z);
            interior.setBlock(inside.below(), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            interior.setBlock(inside, Blocks.FIRE.defaultBlockState(), 3);
            interior.getBlockState(inside).tick(interior, inside, random);
            h.assertTrue(interior.getBlockState(inside).isAir(), "fire does not keep inside the hallway");
            h.assertTrue(interior.getBlockState(inside.below()).is(Blocks.SPRUCE_PLANKS), "nor burns its floor");

            // A creeper-sized blast beside the hallway leaves it standing.
            BlockPos lantern = origin.offset(HouseLayout.AXIS_X + 1, 1, z + 4);
            interior.setBlock(lantern.below(), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            interior.setBlock(lantern, Blocks.LANTERN.defaultBlockState(), 3);
            BlockPos east = origin.offset(HouseLayout.AXIS_X + 2, 2, z + 4);
            interior.setBlock(east, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            BlockPos outside = east.east(2);
            interior.setBlock(outside, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            interior.explode(null, east.getX() + 1.5, east.getY() + 0.5, east.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.TNT);
            h.assertTrue(interior.getBlockState(east).is(Blocks.SPRUCE_PLANKS), "a blast does not break the hallway's wall");
            h.assertTrue(interior.getBlockState(lantern).is(Blocks.LANTERN), "nor what stands inside it");
            h.assertTrue(interior.getBlockState(outside).isAir(), "the blast is real outside the hallway");

            // A hallway that burned before this protection mends its shell, and only its shell.
            BlockPos burnt = origin.offset(HouseLayout.AXIS_X - 2, 3, z + 8);
            BlockPos floor = origin.offset(HouseLayout.AXIS_X, 0, z + 8);
            BlockPos furnishing = origin.offset(HouseLayout.AXIS_X, 1, z + 8);
            interior.setBlock(burnt, Blocks.FIRE.defaultBlockState(), 3);
            interior.setBlock(floor, Blocks.AIR.defaultBlockState(), 3);
            interior.setBlock(furnishing, Blocks.AIR.defaultBlockState(), 3);
            h.assertTrue(HouseImpossibleHallway.repairShell(interior, origin) >= 2, "burnt shell cells are mended");
            h.assertTrue(interior.getBlockState(burnt).is(Blocks.WHITE_TERRACOTTA) && interior.getBlockState(floor).is(Blocks.SPRUCE_PLANKS),
                    "with the hallway's own materials");
            h.assertTrue(interior.getBlockState(furnishing).isAir(), "the open hallway itself is not filled");
            h.succeed();
        } finally {
            fireTick.set(oldFireTick, server);
            chunks.close();
            storage.set("the_oldest_house", old);
        }
    }
}
