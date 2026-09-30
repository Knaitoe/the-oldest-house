package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherCollection;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherRegistry;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MotherTests {
    private MotherTests() {}
    private static ItemStack named(ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    @GameTest(template = "empty")
    public static void archiveKeepsMeaningButRejectsRubble(GameTestHelper helper) {
        MotherCollection data = new MotherCollection();
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(data.keepItem(Items.COBBLESTONE.getDefaultInstance(), registries, null, 0) == null,
                "ordinary rubble does not become a keepsake");
        ItemStack sword = named(Items.DIAMOND_SWORD.getDefaultInstance(), "First sword");
        sword.setDamageValue(43);
        var entry = data.keepItem(sword, registries, UUID.randomUUID(), 0);
        helper.assertTrue(entry != null && entry.loved && entry.item(registries).getDamageValue() == 43,
                "the actual named and damaged object is retained");
        helper.assertTrue(entry.item(registries).getHoverName().getString().equals("First sword"), "its name survives");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void claimIsAtomicAndOnePerRealVisit(GameTestHelper helper) {
        MotherCollection data = new MotherCollection();
        var registries = helper.getLevel().registryAccess();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var a = data.keepItem(Items.COMPASS.getDefaultInstance(), registries, first, 0);
        var b = data.keepItem(Items.CLOCK.getDefaultInstance(), registries, second, 0);
        data.presence(first, true);
        ItemStack borrowed = data.claimItem(first, a.id, registries);
        helper.assertTrue(!borrowed.isEmpty() && data.claimItem(second, a.id, registries).isEmpty(),
                "two players cannot extract one shelf object");
        helper.assertTrue(data.returnItem(first, borrowed, registries), "returning the actual claim works");
        helper.assertTrue(data.claimItem(first, b.id, registries).isEmpty(), "settling is not another retrieval this visit");
        data.presence(first, true);
        helper.assertTrue(data.claimItem(first, b.id, registries).isEmpty(), "a second arrival or login does not reset the visit");
        data.presence(first, false); data.presence(first, true);
        helper.assertTrue(!data.claimItem(first, b.id, registries).isEmpty(), "crossing out and back buys a new visit");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void debtSurvivesSaveAndOrdinaryTradesAreRefused(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        MotherCollection data = new MotherCollection();
        UUID player = UUID.randomUUID();
        var entry = data.keepItem(Items.COMPASS.getDefaultInstance(), registries, null, 0);
        data.presence(player, true);
        data.claimItem(player, entry.id, registries);
        MotherCollection loaded = MotherCollection.load(data.save(new CompoundTag(), registries), registries);
        helper.assertTrue(loaded.debt(player) != null && loaded.usedVisit(player), "the unsettled claim and visit survive restart");
        ItemStack rubble = Items.DIRT.getDefaultInstance();
        helper.assertTrue(!loaded.trade(player, rubble, registries, 0) && rubble.getCount() == 1,
                "ordinary inventory filler cannot buy her off");
        ItemStack gift = named(Items.NAME_TAG.getDefaultInstance(), "For Button");
        helper.assertTrue(loaded.trade(player, gift, registries, 0) && gift.isEmpty() && loaded.debt(player) == null,
                "one named object pays the actual claim");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void splitClaimNeverDuplicatesTheArchive(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        MotherCollection data = new MotherCollection();
        UUID player = UUID.randomUUID();
        var entry = data.keepItem(named(new ItemStack(Items.STRING, 4), "Four strands"), registries, player, 0);
        ItemStack borrowed = data.claimItem(player, entry.id, registries);
        ItemStack fragment = borrowed.split(1);
        data.keepItem(fragment, registries, player, 0);
        helper.assertTrue(data.debt(player) != null && data.visible().isEmpty(), "one expired strand cannot restore four to a shelf");
        MotherCollection loaded = MotherCollection.load(data.save(new CompoundTag(), registries), registries);
        helper.assertTrue(loaded.returnItem(player, borrowed, registries) && borrowed.isEmpty(), "only the three outstanding strands are due after restart");
        helper.assertTrue(loaded.entry(entry.id).item(registries).getCount() == 4, "the total is four after all strands return");
        data.trade(player, named(Items.NAME_TAG.getDefaultInstance(), "Gift"), registries, 0);
        helper.assertTrue(data.entry(entry.id).item(registries).getCount() == 1, "trading away the outstanding part retains the one already returned");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growingWrongPausesOutsideAndViolenceRestoresHerMask(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        MotherCollection data = new MotherCollection();
        UUID player = UUID.randomUUID();
        var entry = data.keepItem(Items.IRON_SWORD.getDefaultInstance(), registries, null, 0);
        ItemStack borrowed = data.claimItem(player, entry.id, registries);
        data.advance(1800, true, false);
        helper.assertTrue(data.corruption() > .49F && data.corruption() < .51F, "the body changes by degree");
        data.advance(24000, false, false);
        helper.assertTrue(data.corruption() < .51F, "time at home or offline does not grow an active debt");
        data.advance(2400, true, false);
        helper.assertTrue(data.corruption() == 1.0F, "an ignored active claim reaches its severe form");
        helper.assertTrue(data.brutalReclaim(player, borrowed, registries, 0), "the actual object can be forcibly reclaimed");
        helper.assertTrue(data.corruption() == 0 && !data.salved(), "violent relief restores the mask without healing her");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mercyAndBanishmentRequireAllThreeActs(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        MotherCollection data = new MotherCollection();
        UUID player = UUID.randomUUID();
        helper.assertTrue(!data.banish(), "she cannot be dismissed before any acts of mercy");
        var entry = data.keepItem(Items.CLOCK.getDefaultInstance(), registries, null, 0);
        ItemStack borrowed = data.claimItem(player, entry.id, registries);
        data.returnItem(player, borrowed, registries);
        data.offer(player, named(Items.STRING.getDefaultInstance(), "A bracelet"), registries, 0);
        helper.assertTrue(!data.salved() && !data.banish(), "giving objects alone does not replace saving a life");
        data.recordDogRecovery(player);
        helper.assertTrue(data.salved() && data.corruption() == 0, "return, freely give, and rescue together salve her");
        helper.assertTrue(data.banish() && data.banished(), "the final voluntary farewell ends her hold permanently");
        MotherCollection loaded = MotherCollection.load(data.save(new CompoundTag(), registries), registries);
        helper.assertTrue(loaded.banished(), "banishment survives world reload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dogHasFullInterventionWindowOnActualLedge(GameTestHelper helper) {
        MotherCollection data = new MotherCollection();
        UUID player = UUID.randomUUID();
        data.beginDogThreat(player);
        data.advance(6000, false, false);
        helper.assertTrue(data.dogThreatTicks() == 0, "the threat does not advance away from the den");
        data.advance(6000, false, true);
        data.dogAtLedge(true);
        helper.assertTrue(data.dogLedgeTicks() == 0, "watching her for a long time does not spend the gallery window");
        data.advance(280, false, true);
        helper.assertTrue(data.dogLedgeTicks() == 280 && !data.dogThrown(), "there is a real fifteen-second intervention interval");
        data.endDogThreat(false);
        helper.assertTrue(data.dogGone() && data.corruption() == 0, "the brutal ending is permanent and restores her mask");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lostPetRetainsIdentityAndCannotBeRecordedTwice(GameTestHelper helper) {
        Wolf wolf = EntityType.WOLF.create(helper.getLevel());
        UUID owner = UUID.randomUUID();
        wolf.setTame(true, true); wolf.setOwnerUUID(owner);
        wolf.setCustomName(Component.literal("Button"));
        CompoundTag saved = new CompoundTag(); wolf.save(saved);
        saved.putByte("CollarColor", (byte) DyeColor.BLUE.getId());
        MotherCollection data = new MotherCollection();
        var entry = data.keepPet(wolf.getUUID(), saved, owner, "Button");
        helper.assertTrue(data.keepPet(wolf.getUUID(), saved, owner, "Button") == null, "one death is kept once");
        Entity restored = MotherOfStrays.restorePetEntity(helper.getLevel(), entry);
        helper.assertTrue(restored instanceof Wolf recovered && owner.equals(recovered.getOwnerUUID())
                && recovered.getCollarColor() == DyeColor.BLUE && recovered.getHealth() > 0,
                "the saved pet returns alive with owner and collar, rather than a generic replacement");
        helper.assertTrue(!data.canRecoverPet(UUID.randomUUID(), entry.id,
                named(Items.NAME_TAG.getDefaultInstance(), "Gift"), 0), "a friend cannot claim someone else's pet");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lurkingEscalatesAndPrefersCorners(GameTestHelper helper) {
        helper.assertTrue(MotherOfStrays.lurkInterval(1) < MotherOfStrays.lurkInterval(0), "deformation increases reposition frequency");
        helper.assertTrue(MotherOfStrays.lurkDistances(1)[0] < MotherOfStrays.lurkDistances(0)[0], "deformation brings her closer");
        BlockPos feet = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.getLevel().setBlock(feet.north().above(), Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(feet.east().above(), Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(MotherOfStrays.coverScore(helper.getLevel(), feet) >= 6, "perpendicular cover is recognized as a corner");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vanillaHostileRulesDoNotEraseTheMother(GameTestHelper helper) {
        MotherEntity mother = MotherRegistry.MOTHER.get().create(helper.getLevel());
        helper.assertTrue(mother != null && mother.getMaxHealth() == 40.0F, "entity attributes are registered on the server");
        helper.assertTrue(LabyrinthDealer.vignettesAvailable(new LabyrinthData()).contains(LabyrinthPlace.MOTHER_DEN),
                "her den is reachable through normal dealer routing");
        helper.assertTrue(!LabyrinthPlace.MOTHER_DEN.isFinishable(), "the anchor remains findable after a retrieval");
        for (int stage = 0; stage < 16; stage++) {
            String texture = "/assets/the_oldest_house/textures/entity/mother_of_strays"
                    + (stage == 0 ? "" : "_" + stage) + ".png";
            helper.assertTrue(TheOldestHouse.class.getResource(texture) != null,
                    "every step in the Mother's gradual texture progression is packaged: " + stage);
        }
        helper.assertTrue(TheOldestHouse.class.getResourceAsStream("/assets/the_oldest_house/textures/entity/mother_pekingese.png") != null,
                "the dog's actual model texture is packaged");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anotherModsExtraItemLifeIsRespected(GameTestHelper helper) {
        ItemEntity entity = new ItemEntity(helper.getLevel(), 0, 5, 0, Items.DIAMOND_SWORD.getDefaultInstance());
        ItemExpireEvent event = new ItemExpireEvent(entity);
        event.addExtraLife(200);
        MotherOfStrays.onItemExpire(event);
        helper.assertTrue(entity.getItem().is(Items.DIAMOND_SWORD), "a lifespan extension keeps the item in the world");
        helper.succeed();
    }
}
