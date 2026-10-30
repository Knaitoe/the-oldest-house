package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

/** 0.4.191: the contests' trades from the success-and-failure audit, on real players. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ContestTradeTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tedsCaveGivesBreathUnderWaterAndTakesTheSpoolThreeTimesAtMost(GameTestHelper h) {
        var caver = NativeTestPlayers.survival(h, "trade_caver");var other = NativeTestPlayers.survival(h, "trade_other");
        try {
            var data = LabyrinthData.get(h.getLevel().getServer());
            WitnessAccount.resolve(data, caver.getUUID(), WitnessAccount.Story.TED_CAVER, "retraced_the_squeeze");
            h.assertTrue(ContestTrades.caver(caver) && !ContestTrades.caver(other), "only the reader who retraced the squeeze has the cave's trade");
            // Two blocks of water, each reader standing in their own column.
            for (int x : new int[]{1, 3}) for (int y = 1; y <= 3; y++) h.setBlock(new BlockPos(x, y, 1), Blocks.WATER);
            var a = h.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5));var b = h.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 1, 1.5));
            caver.teleportTo(h.getLevel(), a.x, a.y, a.z, 0, 0);other.teleportTo(h.getLevel(), b.x, b.y, b.z, 0, 0);
            caver.setNoGravity(true);other.setNoGravity(true);caver.setAirSupply(200);other.setAirSupply(200);
            for (int i = 0; i < 40; i++) { caver.doTick();other.doTick(); }
            int gained = caver.getAirSupply() - other.getAirSupply();
            h.assertTrue(other.getAirSupply() < 200 && gained >= 8 && gained <= 12, "under water the caver loses a quarter less air: caver " + caver.getAirSupply() + ", other " + other.getAirSupply());
            // The spool: a share of what is left, once a House day, three times, then never again.
            caver.getInventory().setItem(5, new ItemStack(net.minecraft.world.item.Items.TORCH, 16));
            var spool = new ItemStack(LabyrinthRegistry.TRAIL_SPOOL.get());int full = spool.getMaxDamage();
            h.assertTrue(ContestTrades.unwind(caver, spool, false), "the first unwinding comes");
            int left = full - spool.getDamageValue();
            h.assertTrue(left >= full * .3 && left <= full * .7, "it takes a share of the line, not all of it: " + left + " of " + full);
            h.assertTrue(!ContestTrades.unwind(caver, spool, false), "not twice in one House day");
            var own = ContestTrades.record(caver);own.putLong("UnwoundDay", own.getLong("UnwoundDay") - 1);data.setStateEntry(ContestTrades.STATE, caver.getUUID().toString(), own);
            caver.getInventory().setItem(0, spool);
            h.assertTrue(ContestTrades.unwind(caver, spool, true) && spool.isEmpty(), "rarely it takes all of it");
            own = ContestTrades.record(caver);own.putLong("UnwoundDay", own.getLong("UnwoundDay") - 1);data.setStateEntry(ContestTrades.STATE, caver.getUUID().toString(), own);
            var second = new ItemStack(LabyrinthRegistry.TRAIL_SPOOL.get());
            h.assertTrue(ContestTrades.unwind(caver, second, false) && !second.isEmpty(), "the third unwinding is the last");
            own = ContestTrades.record(caver);own.putLong("UnwoundDay", own.getLong("UnwoundDay") - 1);data.setStateEntry(ContestTrades.STATE, caver.getUUID().toString(), own);
            int before = second.getDamageValue();
            h.assertTrue(!ContestTrades.unwind(caver, second, true) && second.getDamageValue() == before && ContestTrades.record(caver).getInt("Unwound") == 3, "after three the cave lets go");
            h.assertTrue(caver.getInventory().getItem(5).is(net.minecraft.world.item.Items.TORCH) && caver.getInventory().getItem(5).getCount() == 16, "their torches are never touched");
        } finally { NativeTestPlayers.remove(caver);NativeTestPlayers.remove(other); }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theHidesSmellTurnsAnimalsAwayUntilWaterWashesItAndTheDinerLeavesNauseaOrBlackFrost(GameTestHelper h) {
        var p = NativeTestPlayers.survival(h, "trade_hides");
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
        var at = h.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 1, 2.5));p.teleportTo(h.getLevel(), at.x, at.y, at.z, 0, 0);
        var cow = h.spawn(EntityType.COW, new BlockPos(5, 1, 5));
        var wolf = h.spawn(EntityType.WOLF, new BlockPos(5, 1, 2));wolf.tame(p);
        // The animals settle onto the grass before anything can turn them.
        h.runAfterDelay(20, () -> {
            try {
                h.assertTrue(ContestTrades.smell(p) == 0, "nothing shies from a reader who has no smell on them");
                ContestTrades.hides(p);
                int shied = ContestTrades.smell(p);
                h.assertTrue(ContestTrades.smells(p) && shied == 1 && cow.getNavigation().isInProgress(), "the cow turns away and the tamed companion is not counted: " + shied);
                var target = cow.getNavigation().getTargetPos();
                h.assertTrue(target != null && target.distSqr(p.blockPosition()) > cow.blockPosition().distSqr(p.blockPosition()), "it goes away from the reader");
                // Water washes it off.
                h.setBlock(new BlockPos(2, 1, 2), Blocks.WATER);p.doTick();
                ContestTrades.smell(p);
                h.assertTrue(!ContestTrades.smells(p), "the water takes the smell of the hides");
                // The second apron.
                ContestTrades.nausea(p);
                h.assertTrue(p.hasEffect(MobEffects.CONFUSION), "the meal leaves a moment of nausea");
                float health = p.getHealth();
                ContestTrades.blackFrost(p);
                h.assertTrue(p.getTicksFrozen() == p.getTicksRequiredToFreeze() - 1 && !p.isFullyFrozen() && p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && p.getHealth() == health,
                        "her black frost freezes and slows them, one tick short of a wound");
                h.succeed();
            } finally { NativeTestPlayers.remove(p); }
        });
    }
}
