package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DomesticLabyrinthTests {
    private DomesticLabyrinthTests() {}

    @GameTest(template = "empty")
    public static void domesticApproachSurvivesRerollsSharedProgressAndReload(GameTestHelper h) {
        LabyrinthData data = new LabyrinthData();
        UUID player = UUID.randomUUID(), other = UUID.randomUUID();
        for (LabyrinthPlace place : LabyrinthPlace.values()) if (place.isFinishable()) data.setCompleted(place.id(), true);
        data.setDryDeals(player, 100); // Legacy dry streaks also cannot jump the approach.
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.STRAIGHT_HALL, new BlockPos(0, 64, 0));
        var random = RandomSource.create(417);
        for (int depth = 0; depth < LabyrinthPacing.STORY_DEPTH; depth++) {
            deepen(data, player, depth);
            for (int roll = 0; roll < 32; roll++) {
                LabyrinthDealer.dealPlace(data, player, LabyrinthPlace.STRAIGHT_HALL, random);
                for (var spec : LabyrinthPlace.STRAIGHT_HALL.doors()) {
                    var door = data.door(LabyrinthPlace.STRAIGHT_HALL.doorId(spec));
                    if (!LabyrinthData.DEALT.equals(door.destination)) continue;
                    var deal = data.deal(player, door);
                    var destination = LabyrinthPlace.byId(deal.place());
                    h.assertTrue(destination != null && (LabyrinthPacing.ordinary(destination) || destination == LabyrinthPlace.QUIET_ROOM),
                            "an established world's early doors stay domestic: " + depth + "/" + deal.place());
                    h.assertTrue(!deal.leak() && !deal.bark(), "no false story cues precede the ordinary approach");
                }
            }
            data = LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(), h.getLevel().registryAccess()), h.getLevel().registryAccess());
            h.assertTrue(data.returnDepth(player) == depth && data.completed().size() > 0, "reload preserves route and existing story progress");
        }
        h.assertTrue(LabyrinthDealer.mazeTier(data, other) == 0, "another explorer's approach stays independent");
        deepen(data, player, 16);
        h.assertTrue(LabyrinthDealer.grayAvailable(data, player).contains(LabyrinthPlace.ABYSS_MAZE), "the route eventually reaches the esoteric");
        data.clearReturns(player);
        h.assertTrue(LabyrinthDealer.vignetteChance(data, player) == 0, "backtracking restores the domestic approach");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void quietCrossingsDoNotBankStoriesButRescueAndScentStillWork(GameTestHelper h) {
        var data = new LabyrinthData();
        UUID owner = UUID.randomUUID(), newcomer = UUID.randomUUID();
        LabyrinthBuilder.registerDoors(data, LabyrinthPlace.JUNCTION, new BlockPos(0, 64, 0));
        for (int i = 0; i < 64; i++) LabyrinthDealer.dealPlace(data, newcomer, LabyrinthPlace.JUNCTION, RandomSource.create(i));
        h.assertTrue(data.dryDeals(newcomer) == 0, "quiet exploration cannot bank a guaranteed vignette");
        deepen(data, newcomer, 6);
        h.assertTrue(LabyrinthDealer.vignetteChance(data, newcomer) == 28, "the modest first eligible story chance leaves room for ordinary exploration");
        data.setDryDeals(newcomer, 12);
        h.assertTrue(LabyrinthDealer.vignetteChance(data, newcomer) == 100, "eligible dry spells still guarantee a story");
        CompoundTag state = data.state(MotherOfStrays.ID), owners = new CompoundTag();
        owners.putBoolean(owner.toString(), true); state.put("LivingPetOwners", owners); data.setState(MotherOfStrays.ID, state);
        h.assertTrue(LabyrinthDealer.vignetteChance(data, owner) == 65, "urgent rescue bypasses the quiet approach");
        data.setHillaryScent(owner, true);
        LabyrinthDealer.dealPlace(data, owner, LabyrinthPlace.JUNCTION, RandomSource.create(19));
        long barking = data.doors().stream().filter(d -> LabyrinthData.DEALT.equals(d.destination))
                .map(d -> data.deal(owner, d)).filter(d -> d != null && d.bark() && LabyrinthPlace.byId(d.place()).isVignette()).count();
        h.assertTrue(barking == 1 && !data.hillaryScent(owner), "a deliberate scent still routes one real story and is consumed once");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void homeFragmentsAreFurnishedReachableAndDoNotBlockDoorsOrFolds(GameTestHelper h) {
        int index = 0;
        for (var place : List.of(LabyrinthPlace.STRAIGHT_HALL, LabyrinthPlace.BENT_HALL, LabyrinthPlace.CROSS_HALL)) {
            BlockPos base = h.absolutePos(BlockPos.ZERO).offset(2400 + index++ * 90, 6, 130);
            LabyrinthHalls.build(h.getLevel(), base, place);
            Set<BlockPos> reached = reachable(h, base);
            for (var fragment : LabyrinthDomestic.fragments(place)) {
                BlockPos center = base.offset((fragment.x0() + fragment.x1()) / 2, 0, (fragment.z0() + fragment.z1()) / 2);
                h.assertTrue(reached.contains(center), "the player can walk into a trapped household fragment: " + place + "/" + fragment.room());
                boolean furnished = false;
                for (BlockPos pos : BlockPos.betweenClosed(base.offset(fragment.x0(), 0, fragment.z0()), base.offset(fragment.x1(), 0, fragment.z1()))) {
                    var block = h.getLevel().getBlockState(pos);
                    furnished |= block.is(Blocks.CAULDRON) || block.is(Blocks.WATER_CAULDRON) || block.is(Blocks.WHITE_BED) || block.is(Blocks.OAK_FENCE);
                }
                h.assertTrue(furnished, "the fragment contains actual household furniture");
            }
            for (var door : place.doors()) if (!door.name().equals("entry"))
                h.assertTrue(reached.contains(base.offset(door.rel().relative(door.facing()))), "furnishing leaves every exit reachable");
        }
        for (var place : List.of(LabyrinthPlace.GRAY_CORRIDOR, LabyrinthPlace.FOLDED_MAZE, LabyrinthPlace.DEEP_MAZE, LabyrinthPlace.ABYSS_MAZE)) {
            BlockPos base = h.absolutePos(BlockPos.ZERO).offset(2700 + index++ * 110, 6, 260);
            var layout = MazeLayout.create(place, 747);
            LabyrinthMaze.build(h.getLevel(), base, place, layout);
            h.assertTrue(!LabyrinthDomestic.mazeFragments(place, layout).isEmpty(), "domestic remnants persist in deeper mazes");
            var reached = reachable(h, base);
            for (BlockPos pos : layout.floor()) h.assertTrue(reached.contains(base.offset(pos)), "every old maze tile remains walkable");
            for (var fragment : LabyrinthDomestic.mazeFragments(place, layout)) {
                h.assertTrue(!LabyrinthMaze.nearFold(layout, fragment.back()), "household fragments leave fold sightlines intact");
                h.assertTrue(!h.getLevel().getBlockState(base.offset(fragment.back())).isAir(), "the old wall now contains real furniture");
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void domesticUpgradeKeepsCachesDropsCompanionsAndResolvedStories(GameTestHelper h) {
        var level = h.getLevel();
        var worldData = LabyrinthData.get(level.getServer());
        var previousUpgrade = worldData.state("domestic_0417");
        BlockPos origin = h.absolutePos(BlockPos.ZERO).offset(3700, 0, 0);
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.JUNCTION);
        h.assertTrue(base != null, "the junction slot fits");
        LabyrinthBuilder.buildJunction(level, base);
        LabyrinthLighting.buildEarlyAid(level.getServer(), level, base);
        var cache = (Container) level.getBlockEntity(base.offset(LabyrinthLighting.TOM_CACHE));
        ItemStack saved = new ItemStack(Items.DIAMOND_SWORD);
        saved.set(DataComponents.CUSTOM_NAME, Component.literal("Still mine")); saved.setDamageValue(37);
        cache.setItem(0, saved); for (int i = 1; i < cache.getContainerSize(); i++) cache.setItem(i, ItemStack.EMPTY);
        BlockPos lamp = base.offset(1, 0, -5); level.setBlock(lamp, Blocks.TORCH.defaultBlockState(), 3);
        var item = new ItemEntity(level, base.getX() + .5, base.getY() + .2, base.getZ() - 4.5, new ItemStack(Items.GOLD_INGOT, 3));
        level.addFreshEntity(item);
        Cat cat = EntityType.CAT.create(level); h.assertTrue(cat != null, "a real companion exists");
        cat.moveTo(base.getX() + .5, base.getY(), base.getZ() - 7.5); cat.setPersistenceRequired(); level.addFreshEntity(cat);
        UUID id = cat.getUUID();
        try {
            LabyrinthDomestic.upgrade(level, origin, LabyrinthPlace.JUNCTION);
            h.assertTrue(cache == level.getBlockEntity(base.offset(LabyrinthLighting.TOM_CACHE)) && cache.getItem(0).getDamageValue() == 37
                    && cache.getItem(0).getHoverName().getString().equals("Still mine") && cache.getItem(1).isEmpty(), "upgrade keeps the cache identity and cannot refill it");
            h.assertTrue(level.getBlockState(lamp).is(Blocks.TORCH) && !item.isRemoved() && item.getItem().getCount() == 3, "placed lights and dropped items survive");
            h.assertTrue(!cat.isRemoved() && cat.getUUID().equals(id), "companions retain identity");
            h.assertTrue(level.getBlockState(base.offset(0, -1, -3)).is(Blocks.SPRUCE_PLANKS)
                    && level.getBlockState(base.offset(0, 4, -3)).is(Blocks.LANTERN), "the entrance has boards and ordinary warm lamps");
            BlockPos hallBase = LabyrinthPlaces.base(origin, LabyrinthPlace.STRAIGHT_HALL);
            h.assertTrue(hallBase != null, "the hall slot fits");
            LabyrinthHalls.build(level, hallBase, LabyrinthPlace.STRAIGHT_HALL);
            LabyrinthDomestic.upgrade(level, origin, LabyrinthPlace.STRAIGHT_HALL);
            BlockPos spentLight = hallBase.offset(-4, 0, -19);
            level.setBlock(spentLight, Blocks.TORCH.defaultBlockState(), 3);
            var checkpoint = LabyrinthData.FACTORY.deserializer().apply(worldData.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            h.assertTrue(checkpoint.state("domestic_0417").getBoolean(LabyrinthPlace.STRAIGHT_HALL.id()), "completed decoration is checkpointed across reload");
            LabyrinthDomestic.upgrade(level, origin, LabyrinthPlace.STRAIGHT_HALL);
            h.assertTrue(level.getBlockState(spentLight).is(Blocks.TORCH), "a resumed upgrade cannot clear a finished alcove");
            BlockPos hallOrigin = h.absolutePos(BlockPos.ZERO).offset(4100, 5, 0);
            HouseImpossibleHallway.build(level, hallOrigin);
            h.assertTrue(level.getBlockState(hallOrigin.offset(HouseLayout.AXIS_X - 2, 1, HouseImpossibleHallway.START_Z_OFFSET + 12)).is(Blocks.BARREL), "the approach contains an ordinary cupboard");
            var data = new LabyrinthData(); UUID player = UUID.randomUUID();
            WitnessAccount.resolve(data, player, WitnessAccount.Story.FLOORBOARDS, "the ending");
            data.setCompleted(TellTaleFloorboards.ID, true);
            var loaded = LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            h.assertTrue(loaded.isCompleted(TellTaleFloorboards.ID) && WitnessAccount.has(loaded, player, WitnessAccount.Story.FLOORBOARDS)
                    && WitnessAccount.REQUIRED == WitnessAccount.requiredForPoolSize(WitnessAccount.Story.values().length), "existing evidence survives decoration and the quota follows the eligible story pool");
        } finally { item.discard(); cat.discard(); worldData.setState("domestic_0417", previousUpgrade); }
        h.succeed();
    }

    private static void deepen(LabyrinthData data, UUID player, int depth) {
        while (data.returnDepth(player) < depth) data.pushReturn(player, new LabyrinthData.Waypoint(HouseDimensions.INTERIOR, Vec3.ZERO, 0));
    }
    private static Set<BlockPos> reachable(GameTestHelper h, BlockPos base) {
        Set<BlockPos> seen = new HashSet<>(); var queue = new ArrayDeque<BlockPos>();
        queue.add(base.offset(0, 0, -3)); seen.add(queue.peek());
        while (!queue.isEmpty()) {
            BlockPos at = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = at.relative(direction);
                if (!seen.contains(next) && h.getLevel().getBlockState(next).getCollisionShape(h.getLevel(), next).isEmpty()
                        && h.getLevel().getBlockState(next.above()).getCollisionShape(h.getLevel(), next.above()).isEmpty()
                        && h.getLevel().getBlockState(next.below()).isCollisionShapeFullBlock(h.getLevel(), next.below())) {
                    seen.add(next); queue.add(next);
                }
            }
        }
        return seen;
    }
}
