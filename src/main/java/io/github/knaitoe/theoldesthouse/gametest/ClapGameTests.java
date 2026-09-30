package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGameClock;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherCollection;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ClapGameTests {
    private ClapGameTests() {}

    @GameTest(template = "empty")
    public static void entryReservesWithoutSpendingTheMinute(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        ClapGameClock clock = new ClapGameClock(owner);
        helper.assertTrue(!clock.started() && !clock.bound() && !clock.expired(24000),
                "waiting to equip the cloth does not spend the minute");
        helper.assertTrue(!clock.equip(UUID.randomUUID(), 24000) && !clock.started(),
                "another player cannot start a reserved turn");
        helper.assertTrue(clock.equip(owner, 24000) && clock.bound(), "equipping starts and binds the owner's turn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deadlineIncludesCountingAndCannotBeRestarted(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        ClapGameClock clock = new ClapGameClock(owner);
        clock.equip(owner, 80);
        helper.assertTrue(!clock.equip(owner, 1000), "a second equip does not reset the start time");
        helper.assertTrue(!clock.expired(1279) && clock.expired(1280),
                "the deadline is exactly sixty seconds after equipping, including counting");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void savedTurnKeepsItsOwnerAndOriginalDeadline(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        ClapGameClock clock = new ClapGameClock(owner);
        clock.equip(owner, 300);
        ClapGameClock restored = ClapGameClock.load(clock.save());
        helper.assertTrue(restored.owner().equals(owner) && restored.bound(), "owner and binding survive save/load");
        helper.assertTrue(!restored.equip(owner, 1400) && !restored.expired(1499) && restored.expired(1500),
                "reconnecting or reloading cannot restart or extend the deadline");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wardrobeSuccessReleasesAndSurvivesReload(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        ClapGameClock clock = new ClapGameClock(owner);
        helper.assertTrue(!clock.complete(owner, 0), "the game cannot be completed without wearing the cloth");
        clock.equip(owner, 30);
        helper.assertTrue(!clock.complete(UUID.randomUUID(), 100), "a visitor cannot complete another player's game");
        helper.assertTrue(clock.complete(owner, 1229) && !clock.bound() && !clock.expired(50000),
                "opening the wardrobe before the deadline releases the cloth and prevents failure");
        ClapGameClock restored = ClapGameClock.load(clock.save());
        helper.assertTrue(restored.completed() && !restored.bound() && !restored.equip(owner, 50000),
                "a completed game stays complete after reload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void openingAtTheDeadlineCannotBeatFailure(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        ClapGameClock clock = new ClapGameClock(owner);
        clock.equip(owner, 0);
        helper.assertTrue(!clock.complete(owner, ClapGameClock.LIMIT_TICKS) && clock.bound(),
                "the same-tick wardrobe click cannot cancel the ending once the minute expires");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void failureRespawnIsInTheDomesticHall(GameTestHelper helper) {
        BlockPos origin = new BlockPos(103, 71, -247);
        Vec3 spawn = HideAndClap.manorRespawn(origin);
        BlockPos relative = BlockPos.containing(spawn).subtract(origin);
        helper.assertTrue(relative.getX() == HouseLayout.AXIS_X && relative.getY() == 1
                && relative.getZ() == HouseLayout.THRESHOLD_Z - 2,
                "the return uses the actual manor origin and the hall floor");
        helper.assertTrue(spawn.z < origin.getZ() + HouseLayout.THRESHOLD_Z,
                "the player lands on the domestic side of the near hallway entrance");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ClapDrops"));
        BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
        player.getInventory().clearContent();
        return player;
    }

    private static ItemStack named(ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static List<ItemEntity> drops(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(3),
                entity -> entity.getPersistentData().hasUUID("MotherDropper")
                        && entity.getPersistentData().getUUID("MotherDropper").equals(player.getUUID()));
    }

    @GameTest(template = "empty")
    public static void forcedInventoryLossCreatesRecoverableDropsOnce(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack sword = named(Items.DIAMOND_SWORD.getDefaultInstance(), "Carried sword");
        sword.setDamageValue(47);
        ItemStack boots = named(Items.LEATHER_BOOTS.getDefaultInstance(), "Worn boots");
        ItemStack offhand = named(new ItemStack(Items.STRING, 7), "Offhand threads");
        player.getInventory().setItem(0, sword.copy());
        player.getInventory().setItem(36, boots.copy());
        player.getInventory().setItem(40, offhand.copy());
        HideAndClap.dropInventoryForFailure(player);
        List<ItemEntity> dropped = drops(player);
        helper.assertTrue(player.getInventory().isEmpty() && dropped.size() == 3,
                "carried, armor and offhand stacks become actual world drops while the inventory empties");
        for (ItemStack original : List.of(sword, boots, offhand)) {
            helper.assertTrue(dropped.stream().anyMatch(entity -> ItemStack.isSameItemSameComponents(entity.getItem(), original)
                    && entity.getItem().getCount() == original.getCount()), "names, damage and stack counts survive the drop");
        }
        HideAndClap.dropInventoryForFailure(player);
        helper.assertTrue(drops(player).size() == 3, "running the loss twice cannot duplicate items");
        dropped.forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void motherFindsAnAbandonedFailureDropAtExpiration(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        String name = "Lost in the clap room " + player.getUUID();
        player.getInventory().setItem(0, named(Items.IRON_SWORD.getDefaultInstance(), name));
        HideAndClap.dropInventoryForFailure(player);
        List<ItemEntity> dropped = drops(player);
        helper.assertTrue(dropped.size() == 1 && !dropped.getFirst().getItem().isEmpty(),
                "the object remains recoverable before despawn");
        ItemEntity item = dropped.getFirst();
        ItemExpireEvent extension = new ItemExpireEvent(item);
        extension.addExtraLife(200);
        MotherOfStrays.onItemExpire(extension);
        helper.assertTrue(!item.getItem().isEmpty(), "another mod extending the drop's life postpones collection");
        MotherOfStrays.onItemExpire(new ItemExpireEvent(item));
        MotherCollection archive = MotherCollection.get(helper.getLevel().getServer());
        helper.assertTrue(item.getItem().isEmpty() && archive.visible().stream().anyMatch(entry -> player.getUUID().equals(entry.owner)
                && entry.item(helper.getLevel().registryAccess()).getHoverName().getString().equals(name)),
                "expiration hands the actual named object and its owner to the Mother");
        item.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void girlAndClothAssetsArePackagedWithTheActor(GameTestHelper helper) {
        ClapGhostEntity girl = ClapGhostRegistry.GIRL.get().create(helper.getLevel());
        UUID viewer = UUID.randomUUID();
        girl.setViewer(viewer);
        helper.assertTrue(girl.getMaxHealth() == 1 && girl.viewer().filter(viewer::equals).isPresent(),
                "the girl is registered on the server and her glimpse belongs to one player");
        for (String resource : List.of("entity/clap_ghost_girl.png", "item/blindfold.png", "gui/blindfold_edge.png")) {
            helper.assertTrue(TheOldestHouse.class.getResource("/assets/the_oldest_house/textures/" + resource) != null,
                    "the model's native texture and cloth assets are packaged: " + resource);
        }
        helper.succeed();
    }
}
