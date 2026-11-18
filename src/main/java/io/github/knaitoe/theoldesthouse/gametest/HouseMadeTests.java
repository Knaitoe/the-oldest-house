package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping.Where;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleProgress;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.MinotaurEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherCollection;
import io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What the House made, and the reader's favourite weapon (docs/NEIGHBOURS.md, R7 and the owner's decisions of 17 November). The
 * House's own stamps on an outside thing (the Mother's marks of who carried it and since when, a weapon's identity and the mark of a
 * copy) do not make it House-made, so it does not walk past the hall drawers; custom data of any other kind does, as it always did, and
 * that is what the House tells A Reaping as {@code house_made}. The one stamped thing that does pass is the reader's favourite, the
 * weapon the finale would arm them with. A weapon from another mod, in NeoForge's common tags, can be that favourite: its uses are
 * counted by the blow it deals, and the Minotaur knows the original by what dealt the blow.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HouseMadeTests {
    private HouseMadeTests() {
    }

    private static int count(java.util.List<ItemStack> list, Item item) {
        int n = 0;
        for (var s : list) {
            if (s.is(item)) {
                n += s.getCount();
            }
        }
        return n;
    }

    private static int carrying(ServerPlayer p, Item item) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            if (p.getInventory().getItem(i).is(item)) {
                n += p.getInventory().getItem(i).getCount();
            }
        }
        return n;
    }

    private static ItemStack marked(Item item, java.util.function.Consumer<CompoundTag> marks) {
        var stack = new ItemStack(item);
        var tag = new CompoundTag();
        marks.accept(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /** The one stack of this item the reader carries. */
    private static ItemStack held(ServerPlayer p, Item item) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            if (p.getInventory().getItem(i).is(item)) {
                return p.getInventory().getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }

    /** A new reader with nothing on them: their favourite is only what the House counts them using. */
    private static ServerPlayer reader(GameTestHelper h, String name) {
        ServerPlayer p = NativeTestPlayers.survival(h, name, UUID.randomUUID());
        p.getInventory().clearContent();
        HouseKeeping.get(p.server).forget(p.getUUID());
        return p;
    }

    /**
     * A House given by its saved data alone, its origin far off, with its front hall and a place past the hall: where the House says a
     * reader stands, and nothing built there. {@link #close} puts the saved data back.
     */
    private record Manor(ServerLevel inside, BlockPos hall, BlockPos rooms, HouseSavedData was) {
        void put(ServerPlayer p, BlockPos at) {
            p.teleportTo(inside, at.getX() + .5, at.getY(), at.getZ() + .5, 0F, 0F);
        }

        void close() {
            inside.getServer().overworld().getDataStorage().set("the_oldest_house", was);
        }
    }

    private static Manor manor(GameTestHelper h) {
        var server = h.getLevel().getServer();
        ServerLevel inside = HouseTestLevel.get(server, HouseDimensions.INTERIOR);
        BlockPos origin = new BlockPos(h.absolutePos(BlockPos.ZERO).getX() + 250000, 80, 250000);
        BlockPos hall = origin.offset(HouseKeeping.DRAWERS).east(2);
        BlockPos rooms = origin.offset(0, 1, 600);
        inside.getChunkAt(hall);
        inside.getChunkAt(rooms);
        var was = HouseSavedData.get(server);
        var standing = new HouseSavedData();
        standing.markSpawned(origin);
        server.overworld().getDataStorage().set("the_oldest_house", standing);
        return new Manor(inside, hall, rooms, was);
    }

    /** The reader lets this thing they carry fall where they stand, as the game tells every listener. */
    private static ItemEntity letFall(ServerPlayer p, Item item) {
        var stack = held(p, item);
        p.getInventory().removeItem(stack);
        var entity = new ItemEntity(p.serverLevel(), p.getX(), p.getY(), p.getZ(), stack);
        NeoForge.EVENT_BUS.post(new ItemTossEvent(entity, p));
        return entity;
    }

    // ------------------------------------------------------------------------------------------------ the stamps
    /**
     * The House's stamps alone do not make a thing House-made: a pickaxe the Mother has marked as carried, a sword with an identity
     * that is not the favourite, and a House copy of an axe are all put away at the door; a stick that carries something else of the
     * House's as well, and the House's own furniture, are House-made and let in. House-made is what A Reaping is told.
     */
    @GameTest(template = "empty", batch = "house_made", timeoutTicks = 100)
    public static void theHousesOwnStampsDoNotMakeAThingHouseMade(GameTestHelper h) {
        ServerPlayer p = reader(h, "stamped_reader");
        try {
            var favourite = new ItemStack(Items.DIAMOND_SWORD);
            WeaponHistory.record(p, favourite, 10);
            var pickaxe = marked(Items.IRON_PICKAXE, t -> {
                t.putUUID(MotherCollection.CARRIER, p.getUUID());
                t.putLong(MotherCollection.CARRIED, 0L);
            });
            var sword = new ItemStack(Items.IRON_SWORD);
            WeaponHistory.stamp(sword);
            var copy = new ItemStack(Items.IRON_AXE);
            WeaponHistory.markCopy(copy);
            var stick = marked(Items.STICK, t -> {
                t.putBoolean("HouseMade", true);
                t.putUUID(MotherCollection.CARRIER, p.getUUID());
            });
            var furniture = new ItemStack(HouseBlocks.FURNITURE_ITEM.get());
            var bread = new ItemStack(Items.BREAD);
            for (var stamped : java.util.List.of(pickaxe, sword, copy)) {
                h.assertTrue(!HouseKeeping.houseMade(stamped) && !HouseKeeping.allowed(stamped), "the House's stamps alone do not make it House-made: " + stamped.get(DataComponents.CUSTOM_DATA));
            }
            h.assertTrue(HouseKeeping.houseMade(stick) && HouseKeeping.allowed(stick), "anything else of the House's in its custom data does");
            h.assertTrue(HouseKeeping.houseMade(furniture), "and the House's own things are House-made");
            h.assertTrue(!HouseKeeping.houseMade(bread) && HouseKeeping.allowed(bread), "bread is let in, but the House did not make it");
            h.assertTrue(!HouseKeeping.houseMade(ItemStack.EMPTY), "nothing is not House-made");

            HouseKeeping.update(p, Where.OUTSIDE);
            var inv = p.getInventory();
            inv.setItem(0, pickaxe);
            inv.setItem(1, sword);
            inv.setItem(2, copy);
            inv.setItem(3, stick);
            inv.setItem(4, furniture);
            HouseKeeping.update(p, Where.MANOR);
            var keep = HouseKeeping.of(p);
            for (var taken : java.util.List.of(Items.IRON_PICKAXE, Items.IRON_SWORD, Items.IRON_AXE)) {
                h.assertTrue(carrying(p, taken) == 0 && count(keep.drawer, taken) == 1, "put away at the door: " + taken);
            }
            h.assertTrue(carrying(p, Items.STICK) == 1 && carrying(p, HouseBlocks.FURNITURE_ITEM.get()) == 1, "the House-made stick and the furniture are let in");
        } finally {
            HouseKeeping.get(p.server).forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ the favourite
    /**
     * The owner: "Yes, to favorite weapon". The reader's favourite passes the drawers, in at the door and on past the hall, and no other
     * stamped weapon: a copy of it is put away, and so is the weapon that was the favourite once another has been used more. It stays the
     * reader's own thing: carried out, it is not taken for something the House gave them.
     */
    @GameTest(template = "empty", batch = "house_made", timeoutTicks = 100)
    public static void theReadersFavouritePassesTheDrawersAndNoOtherStampedWeapon(GameTestHelper h) {
        ServerPlayer p = reader(h, "favourite_reader");
        try {
            var axe = new ItemStack(Items.IRON_AXE);
            var sword = new ItemStack(Items.IRON_SWORD);
            WeaponHistory.record(p, axe, 3);
            WeaponHistory.record(p, sword, 1);
            var copy = axe.copy();
            WeaponHistory.markCopy(copy);
            var inv = p.getInventory();
            HouseKeeping.update(p, Where.OUTSIDE);
            inv.setItem(0, axe);
            inv.setItem(1, sword);
            inv.setItem(2, copy);
            h.assertTrue(WeaponHistory.wounds(axe, WeaponHistory.favorite(p)), "the axe is the favourite");
            HouseKeeping.update(p, Where.MANOR);
            var keep = HouseKeeping.of(p);
            h.assertTrue(inv.getItem(0) == axe && WeaponHistory.wounds(inv.getItem(0), WeaponHistory.favorite(p)), "the favourite passes the door");
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 0 && count(keep.drawer, Items.IRON_SWORD) == 1, "another stamped weapon does not");
            h.assertTrue(count(keep.drawer, Items.IRON_AXE) == 1 && carrying(p, Items.IRON_AXE) == 1, "nor a House copy of the favourite");
            h.assertTrue(count(keep.loose, Items.IRON_AXE) == 1, "the favourite is the reader's own thing, loose in the manor");

            // The sword comes out of the drawer and is used until it is the favourite; past the hall it goes on and the axe goes back.
            HouseKeeping.retrieve(p, null);
            int at = -1;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(Items.IRON_SWORD)) {
                    at = i;
                }
            }
            h.assertTrue(at >= 0 && carrying(p, Items.IRON_AXE) == 2, "the sword is back in the reader's pockets, and the copy with it");
            WeaponHistory.record(p, inv.getItem(at), 5);
            h.assertTrue(WeaponHistory.wounds(inv.getItem(at), WeaponHistory.favorite(p)), "the sword is the favourite now");
            HouseKeeping.update(p, Where.BEYOND);
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 1, "past the hall the favourite goes on with the reader");
            h.assertTrue(carrying(p, Items.IRON_AXE) == 0 && count(keep.drawer, Items.IRON_AXE) == 2, "and the axe, no longer the favourite, goes back to the drawer with the copy");

            HouseKeeping.update(p, Where.OUTSIDE);
            h.assertTrue(count(keep.carried, Items.IRON_SWORD) == 0 && count(keep.loose, Items.IRON_SWORD) == 0, "carried out, it is the reader's own, not something the House gave");
            HouseKeeping.update(p, Where.MANOR);
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 1, "and it comes in again while it is the favourite");
        } finally {
            HouseKeeping.get(p.server).forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ stamped since
    /**
     * What the House stamps on a thing after it was listed does not change whose it is. An outside pickaxe and sword, put away unmarked,
     * taken out, then marked by the Mother and swung once (not the favourite), go back to the drawer past the hall. Something the House
     * gave, marked and swung outside, comes back in with the reader; and an outside sword with no identity yet does not take the place
     * of the House's sword that has one.
     */
    @GameTest(template = "empty", batch = "house_made_stamped_since", timeoutTicks = 100)
    public static void whatTheHouseStampsOnAThingSinceDoesNotChangeWhoseItIs(GameTestHelper h) {
        ServerPlayer p = reader(h, "stamped_since");
        try {
            WeaponHistory.record(p, new ItemStack(Items.DIAMOND_SWORD), 10);
            long now = p.server.overworld().getGameTime();
            var inv = p.getInventory();
            HouseKeeping.update(p, Where.OUTSIDE);
            inv.setItem(0, new ItemStack(Items.IRON_PICKAXE));
            inv.setItem(1, new ItemStack(Items.IRON_SWORD));
            HouseKeeping.update(p, Where.MANOR);
            var keep = HouseKeeping.of(p);
            h.assertTrue(count(keep.drawer, Items.IRON_PICKAXE) == 1 && count(keep.drawer, Items.IRON_SWORD) == 1, "both put away unmarked at the door");
            HouseKeeping.retrieve(p, null);
            var pickaxe = held(p, Items.IRON_PICKAXE);
            var sword = held(p, Items.IRON_SWORD);
            MotherCollection.rememberCarried(pickaxe, p.getUUID(), now);
            WeaponHistory.record(p, sword, 1);
            h.assertTrue(pickaxe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().hasUUID(MotherCollection.CARRIER)
                    && WeaponHistory.identity(sword) != null && !WeaponHistory.wounds(sword, WeaponHistory.favorite(p)),
                    "out of the drawer, the Mother has marked the pickaxe and the sword has an identity that is not the favourite's");
            HouseKeeping.update(p, Where.BEYOND);
            h.assertTrue(carrying(p, Items.IRON_PICKAXE) == 0 && count(keep.drawer, Items.IRON_PICKAXE) == 1, "past the hall the marked pickaxe goes back to the drawer");
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 0 && count(keep.drawer, Items.IRON_SWORD) == 1 && keep.loose.isEmpty(), "and so does the swung sword");

            // What the House gave goes out with the reader: a plain axe, and a sword it has counted a use of.
            var given = new ItemStack(Items.IRON_AXE);
            var blade = new ItemStack(Items.STONE_SWORD);
            WeaponHistory.record(p, blade, 1);
            UUID bladeId = WeaponHistory.identity(blade);
            inv.setItem(5, given);
            inv.setItem(6, blade);
            HouseKeeping.update(p, Where.MANOR);
            HouseKeeping.update(p, Where.OUTSIDE);
            h.assertTrue(count(keep.carried, Items.IRON_AXE) == 1 && count(keep.carried, Items.STONE_SWORD) == 1, "the House notes what it gave going out");
            // Outside the Mother marks the axe and it is swung; another stone sword, never swung, is picked up into an earlier slot.
            MotherCollection.rememberCarried(given, p.getUUID(), now);
            WeaponHistory.record(p, given, 1);
            inv.setItem(0, new ItemStack(Items.STONE_SWORD));
            HouseKeeping.update(p, Where.MANOR);
            h.assertTrue(carrying(p, Items.IRON_AXE) == 1 && count(keep.drawer, Items.IRON_AXE) == 0, "the House's axe, marked and swung outside, comes back in");
            h.assertTrue(carrying(p, Items.STONE_SWORD) == 1 && WeaponHistory.wounds(held(p, Items.STONE_SWORD), bladeId) && count(keep.drawer, Items.STONE_SWORD) == 1,
                    "and the House's sword with it, while the outside one with no identity yet is put away");
        } finally {
            HouseKeeping.get(p.server).forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    /**
     * The favourite passes the hall however it gets there: set down in the manor and picked up past the hall by its reader, it stays
     * with them, while their own pickaxe set down beside it goes back to the drawer. The saved data says where the House stands; nothing
     * is built there.
     */
    @GameTest(template = "empty", batch = "house_made_set_down", timeoutTicks = 100)
    public static void theFavouriteSetDownInTheManorStillPassesTheHall(GameTestHelper h) {
        ServerPlayer p = reader(h, "favourite_set_down");
        var server = p.server;
        Manor manor = manor(h);
        ServerLevel inside = manor.inside();
        BlockPos rooms = manor.rooms();
        try {
            manor.put(p, manor.hall());
            h.assertTrue(HouseKeeping.where(p) == Where.MANOR, "the reader stands in the front hall");
            var axe = new ItemStack(Items.IRON_AXE);
            WeaponHistory.record(p, axe, 3);
            var inv = p.getInventory();
            HouseKeeping.update(p, Where.OUTSIDE);
            inv.setItem(0, axe);
            inv.setItem(1, new ItemStack(Items.IRON_PICKAXE));
            HouseKeeping.update(p, Where.MANOR);
            HouseKeeping.retrieve(p, null);
            var keep = HouseKeeping.of(p);
            h.assertTrue(count(keep.loose, Items.IRON_AXE) == 1 && count(keep.loose, Items.IRON_PICKAXE) == 1, "the favourite and the pickaxe are the reader's own, loose");

            // Both set down in the manor.
            var dropped = new java.util.ArrayList<ItemEntity>();
            for (var item : java.util.List.of(Items.IRON_AXE, Items.IRON_PICKAXE)) {
                var stack = held(p, item);
                inv.removeItem(stack);
                var entity = new ItemEntity(inside, p.getX(), p.getY(), p.getZ(), stack);
                HouseKeeping.tossed(new ItemTossEvent(entity, p));
                dropped.add(entity);
            }
            // Picked up past the hall.
            p.moveTo(rooms.getX() + .5, rooms.getY(), rooms.getZ() + .5);
            h.assertTrue(HouseKeeping.where(p) == Where.BEYOND, "the reader is past the hall");
            var favourite = new ItemEntityPickupEvent.Pre(p, dropped.get(0));
            HouseKeeping.pickup(favourite);
            h.assertTrue(favourite.canPickup() != TriState.FALSE && !dropped.get(0).isRemoved() && count(keep.drawer, Items.IRON_AXE) == 0,
                    "the favourite set down in the manor is its reader's to pick up past the hall");
            var own = new ItemEntityPickupEvent.Pre(p, dropped.get(1));
            HouseKeeping.pickup(own);
            h.assertTrue(own.canPickup() == TriState.FALSE && dropped.get(1).isRemoved() && count(keep.drawer, Items.IRON_PICKAXE) == 1,
                    "while their own pickaxe set down beside it goes back to the drawer");
        } finally {
            manor.close();
            HouseKeeping.get(server).forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    /**
     * Two readers in the manor. The first reader's favourite, set down there and picked up there by the second, is the second's to put
     * away at the hall like anything else of somebody else's they carry: the drawers let through only a reader's own favourite. And a
     * favourite set down in the manor that is no longer the favourite when its reader picks it up past the hall goes back to the drawer.
     */
    @GameTest(template = "empty", batch = "house_made_two_readers", timeoutTicks = 100)
    public static void aFavouriteSetDownPassesTheHallOnlyWithItsOwnReader(GameTestHelper h) {
        ServerPlayer a = reader(h, "set_down_first");
        ServerPlayer b = reader(h, "set_down_second");
        var server = a.server;
        Manor manor = manor(h);
        try {
            manor.put(a, manor.hall());
            manor.put(b, manor.hall());
            h.assertTrue(HouseKeeping.where(a) == Where.MANOR && HouseKeeping.where(b) == Where.MANOR, "both readers stand in the front hall");
            var sword = new ItemStack(Items.IRON_SWORD);
            var axe = new ItemStack(Items.IRON_AXE);
            WeaponHistory.record(a, sword, 10);
            WeaponHistory.record(a, axe, 4);
            UUID swordId = WeaponHistory.identity(sword), axeId = WeaponHistory.identity(axe);
            HouseKeeping.update(a, Where.OUTSIDE);
            a.getInventory().setItem(0, sword);
            a.getInventory().setItem(1, axe);
            HouseKeeping.update(a, Where.MANOR);
            HouseKeeping.update(b, Where.MANOR);
            HouseKeeping.retrieve(a, null);
            var keepA = HouseKeeping.of(a);
            var keepB = HouseKeeping.of(b);
            h.assertTrue(count(keepA.loose, Items.IRON_SWORD) == 1 && count(keepA.loose, Items.IRON_AXE) == 1 && swordId.equals(WeaponHistory.favorite(a)),
                    "the first reader's favourite sword and their axe are their own, loose");

            // The favourite set down in the manor, picked up there by the second reader, and carried to the hall.
            var dropped = letFall(a, Items.IRON_SWORD);
            var picked = new ItemEntityPickupEvent.Pre(b, dropped);
            HouseKeeping.pickup(picked);
            h.assertTrue(picked.canPickup() != TriState.FALSE && count(keepB.loose, Items.IRON_SWORD) == 1,
                    "picked up in the manor by the second reader, the first's favourite is loose in the second's hands");
            b.getInventory().add(dropped.getItem().copy());
            dropped.discard();
            HouseKeeping.update(b, Where.BEYOND);
            h.assertTrue(carrying(b, Items.IRON_SWORD) == 0 && count(keepB.drawer, Items.IRON_SWORD) == 1,
                    "and does not pass the hall with them: it is not their favourite");

            // The axe, used until it is the favourite, is set down in the manor; another weapon is used more before it is picked up.
            WeaponHistory.record(a, held(a, Items.IRON_AXE), 20);
            h.assertTrue(axeId.equals(WeaponHistory.favorite(a)), "the axe is the first reader's favourite now");
            var axeDown = letFall(a, Items.IRON_AXE);
            WeaponHistory.record(a, new ItemStack(Items.BOW), 40);
            h.assertTrue(!axeId.equals(WeaponHistory.favorite(a)), "and is not, once a bow has been used more");
            manor.put(a, manor.rooms());
            h.assertTrue(HouseKeeping.where(a) == Where.BEYOND, "the first reader is past the hall");
            var back = new ItemEntityPickupEvent.Pre(a, axeDown);
            HouseKeeping.pickup(back);
            h.assertTrue(back.canPickup() == TriState.FALSE && axeDown.isRemoved() && count(keepA.drawer, Items.IRON_AXE) == 1,
                    "picked up past the hall, the axe that is no longer the favourite goes back to the drawer");
        } finally {
            manor.close();
            HouseKeeping.get(server).forget(a.getUUID());
            HouseKeeping.get(server).forget(b.getUUID());
            NativeTestPlayers.remove(a);
            NativeTestPlayers.remove(b);
        }
        h.succeed();
    }

    /**
     * A weapon let fall is no use of it: the House never reads the game's item statistics, which count every block mined with an iron
     * axe. A reader who fought with their sword and has chopped a hundred blocks with iron axes lets an unswung iron axe fall in the
     * manor; the sword is still their favourite and passes the hall, and the axe goes back to the drawer.
     */
    @GameTest(template = "empty", batch = "house_made_let_fall", timeoutTicks = 100)
    public static void aWeaponLetFallIsNoUseAndDoesNotPassTheHall(GameTestHelper h) {
        ServerPlayer p = reader(h, "let_fall_reader");
        var server = p.server;
        Manor manor = manor(h);
        try {
            manor.put(p, manor.hall());
            h.assertTrue(HouseKeeping.where(p) == Where.MANOR, "the reader stands in the front hall");
            var sword = new ItemStack(Items.IRON_SWORD);
            WeaponHistory.record(p, sword, 10);
            UUID swordId = WeaponHistory.identity(sword);
            var chopped = Stats.ITEM_USED.get(Items.IRON_AXE);
            p.awardStat(chopped, 100);
            h.assertTrue(p.getStats().getValue(chopped) >= 100, "the game has counted a hundred uses of iron axes");
            var inv = p.getInventory();
            HouseKeeping.update(p, Where.OUTSIDE);
            inv.setItem(0, sword);
            inv.setItem(1, new ItemStack(Items.IRON_AXE));
            HouseKeeping.update(p, Where.MANOR);
            var keep = HouseKeeping.of(p);
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 1 && count(keep.drawer, Items.IRON_AXE) == 1, "the favourite sword passes the door, and the unswung axe is put away");
            HouseKeeping.retrieve(p, null);
            h.assertTrue(carrying(p, Items.IRON_AXE) == 1 && count(keep.loose, Items.IRON_AXE) == 1, "the axe out of the drawer is the reader's own, loose");

            var dropped = letFall(p, Items.IRON_AXE);
            h.assertTrue(WeaponHistory.identity(dropped.getItem()) == null && swordId.equals(WeaponHistory.favorite(p)),
                    "let fall, the axe is counted no use, and the sword is still the favourite: " + WeaponHistory.favorite(p));
            var picked = new ItemEntityPickupEvent.Pre(p, dropped);
            HouseKeeping.pickup(picked);
            h.assertTrue(picked.canPickup() != TriState.FALSE && count(keep.loose, Items.IRON_AXE) == 1, "picked up again in the manor, it is loose");
            inv.add(dropped.getItem().copy());
            dropped.discard();
            HouseKeeping.update(p, Where.BEYOND);
            h.assertTrue(carrying(p, Items.IRON_SWORD) == 1 && swordId.equals(WeaponHistory.favorite(p)), "past the hall the sword goes on with the reader");
            h.assertTrue(carrying(p, Items.IRON_AXE) == 0 && count(keep.drawer, Items.IRON_AXE) == 1, "and the axe goes back to the drawer");
        } finally {
            manor.close();
            HouseKeeping.get(server).forget(p.getUUID());
            NativeTestPlayers.remove(p);
        }
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ another mod's weapon
    private static final String WEAPONS = "house_made_weapons";
    /** The weapons case's reader, put away when the batch ends even if the case did not get as far as putting them away itself. */
    private static final List<ServerPlayer> ARMED = new ArrayList<>();

    @AfterBatch(batch = WEAPONS)
    public static void theWeaponsArePutAway(ServerLevel level) {
        NeighbourTags.bind(Items.SHEARS.builtInRegistryHolder(), Tags.Items.MELEE_WEAPON_TOOLS, false);
        NeighbourTags.bind(Items.FISHING_ROD.builtInRegistryHolder(), Tags.Items.RANGED_WEAPON_TOOLS, false);
        ARMED.forEach(p -> {
            HouseKeeping.get(p.server).forget(p.getUUID());
            if (p.server.getPlayerList().getPlayers().contains(p)) {
                NativeTestPlayers.remove(p);
            }
        });
        ARMED.clear();
    }

    /** The mod's blade deals a blow to {@code target} for the reader, as another mod's melee does: no vanilla attack is made. */
    private static void blade(ServerPlayer p, Mob target) {
        target.invulnerableTime = 0;
        target.setHealth(target.getMaxHealth());
        target.hurt(p.damageSources().playerAttack(p), 1);
    }

    /** Runs {@code then} on a tick after this one. */
    private static void later(GameTestHelper h, Runnable then) {
        long now = h.getLevel().getGameTime();
        SceneReady.when(h, () -> h.getLevel().getGameTime() > now ? null : "a later tick", 1, 20, then);
    }

    /**
     * A weapon from another mod is a weapon to the House when it is in {@code #c:tools/melee_weapon} or {@code #c:tools/ranged_weapon}
     * (shears and a fishing rod stand in, put in the tags for the case). A use is counted by the blow it deals, through the damage
     * source: a melee blow that is not a vanilla attack, once a tick however many it hurts, as a sweep through three is one; and a
     * projectile, whose weapon is the one that loosed it, counted on the real one in the reader's hand. A vanilla attack is counted
     * once, and not at all if another listener cancels it; a vanilla bow's arrow only when the bow is drawn, as before. The one used
     * most is the favourite, and passes the drawers.
     */
    @GameTest(template = "empty", batch = WEAPONS, timeoutTicks = 100)
    public static void aWeaponFromAnotherModCanBeTheFavourite(GameTestHelper h) {
        ServerPlayer p = reader(h, "another_mods_weapon");
        ARMED.add(p);
        var shears = Items.SHEARS.builtInRegistryHolder();
        var rod = Items.FISHING_ROD.builtInRegistryHolder();
        h.assertTrue(!WeaponHistory.weapon(new ItemStack(Items.SHEARS)) && !WeaponHistory.weapon(new ItemStack(Items.FISHING_ROD)), "out of the tags, shears and a rod are no weapons");
        NeighbourTags.bind(shears, Tags.Items.MELEE_WEAPON_TOOLS, true);
        NeighbourTags.bind(rod, Tags.Items.RANGED_WEAPON_TOOLS, true);
        h.assertTrue(WeaponHistory.weapon(new ItemStack(Items.SHEARS)) && WeaponHistory.weapon(new ItemStack(Items.FISHING_ROD)), "in them, they are");
        BlockPos at = h.absolutePos(new BlockPos(1, 2, 1));
        p.teleportTo(h.getLevel(), at.getX() + .5, at.getY(), at.getZ() + .5, 0F, 0F);
        Mob target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 1));
        target.setInvulnerable(false);

        // A mod's melee blow: the damage source carries the main hand, and no vanilla attack was made. Three hurt on one tick, as a
        // sweep through three would, are one use.
        var blade = new ItemStack(Items.SHEARS);
        p.setItemInHand(InteractionHand.MAIN_HAND, blade);
        for (int i = 0; i < 3; i++) {
            blade(p, target);
        }
        UUID bladeId = WeaponHistory.identity(p.getMainHandItem());
        h.assertTrue(bladeId != null && WeaponHistory.uses(p, bladeId) == 1, "the mod's blade hurting three on one tick is one use: " + (bladeId == null ? "none" : WeaponHistory.uses(p, bladeId)));
        later(h, () -> {
            blade(p, target);
            h.assertTrue(WeaponHistory.uses(p, bladeId) == 2, "a blow on a later tick is another: " + WeaponHistory.uses(p, bladeId));
            later(h, () -> {
                blade(p, target);
                h.assertTrue(WeaponHistory.uses(p, bladeId) == 3, "and another: " + WeaponHistory.uses(p, bladeId));
                aWeaponFromAnotherModThen(h, p, target, blade, bladeId);
            });
        });
    }

    private static void aWeaponFromAnotherModThen(GameTestHelper h, ServerPlayer p, Mob target, ItemStack blade, UUID bladeId) {
        target.invulnerableTime = 0;
        target.setHealth(target.getMaxHealth());
        target.hurt(p.damageSources().thorns(p), 1);
        h.assertTrue(target.getHealth() < target.getMaxHealth() && WeaponHistory.uses(p, bladeId) == 3, "thorns, which the reader's armour deals, are no use of the blade");

        // A vanilla attack another listener cancels (another mod's weapon taking the swing) is no use; one that goes through is counted
        // once, not again by its damage.
        var sword = new ItemStack(Items.IRON_SWORD);
        p.setItemInHand(InteractionHand.MAIN_HAND, sword);
        target.invulnerableTime = 0;
        target.setHealth(target.getMaxHealth());
        Consumer<AttackEntityEvent> takesTheSwing = e -> {
            if (e.getEntity() == p) {
                e.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AttackEntityEvent.class, takesTheSwing);
        try {
            p.attack(target);
        } finally {
            NeoForge.EVENT_BUS.unregister(takesTheSwing);
        }
        h.assertTrue(WeaponHistory.identity(p.getMainHandItem()) == null && target.getHealth() == target.getMaxHealth(), "a vanilla attack another listener cancels is no use");
        p.attack(target);
        UUID swordId = WeaponHistory.identity(p.getMainHandItem());
        h.assertTrue(swordId != null && WeaponHistory.uses(p, swordId) == 1 && target.getHealth() < target.getMaxHealth(),
                "a vanilla attack is one use: " + (swordId == null ? "none" : WeaponHistory.uses(p, swordId)));

        // A mod's projectile: its weapon is a copy of what loosed it, and the use is the real one's in the hand. The sling's first
        // shot: the copy is taken before the sling is stamped, as the shot joins the world. It lands on the tick the blade last
        // struck, and is counted all the same: once a tick is each weapon's.
        var sling = new ItemStack(Items.FISHING_ROD);
        p.setItemInHand(InteractionHand.MAIN_HAND, sling);
        var shot = new Arrow(h.getLevel(), p, new ItemStack(Items.ARROW), p.getMainHandItem());
        h.getLevel().addFreshEntity(shot);
        h.assertTrue(WeaponHistory.identity(p.getMainHandItem()) != null && WeaponHistory.identity(shot.getWeaponItem()) == null,
                "the sling is stamped as its shot joins the world, after the shot took its copy");
        target.invulnerableTime = 0;
        target.setHealth(target.getMaxHealth());
        target.hurt(p.damageSources().arrow(shot, p), 1);
        UUID slingId = WeaponHistory.identity(p.getMainHandItem());
        h.assertTrue(slingId != null && WeaponHistory.uses(p, slingId) == 1, "a hit from the mod's sling is a use of the sling in the hand");
        shot.discard();

        // A vanilla bow's arrow is counted when the bow is drawn, not again when it hits.
        var bow = new ItemStack(Items.BOW);
        p.setItemInHand(InteractionHand.MAIN_HAND, bow);
        var arrow = new Arrow(h.getLevel(), p, new ItemStack(Items.ARROW), bow);
        target.invulnerableTime = 0;
        target.setHealth(target.getMaxHealth());
        target.hurt(p.damageSources().arrow(arrow, p), 1);
        h.assertTrue(WeaponHistory.identity(p.getMainHandItem()) == null, "a vanilla arrow's hit is not counted again");
        arrow.discard();

        // The blade, used most, is the favourite, and passes the drawers; the sword does not.
        h.assertTrue(bladeId.equals(WeaponHistory.favorite(p)), "the mod's blade is the favourite");
        var inv = p.getInventory();
        inv.clearContent();
        HouseKeeping.update(p, Where.OUTSIDE);
        inv.setItem(0, blade);
        inv.setItem(1, sword);
        HouseKeeping.update(p, Where.MANOR);
        h.assertTrue(carrying(p, Items.SHEARS) == 1 && carrying(p, Items.IRON_SWORD) == 0, "the favourite from another mod passes the drawers, and the sword does not");
        target.discard();
        h.succeed();
    }

    // ------------------------------------------------------------------------------------------------ the Minotaur
    /**
     * The Minotaur's "only the original" knows the original by what dealt the blow: a projectile whose weapon is the stamped original
     * wounds it though no House mark was put on the projectile (another mod's projectile, carrying its bow), and so does a blow whose
     * source names the original. An arrow from another bow passes through.
     */
    @GameTest(template = "empty", batch = "house_made_minotaur")
    public static void theMinotaurKnowsTheOriginalByWhatDealtTheBlow(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var server = player.server;
        var data = LabyrinthData.get(server);
        var previous = data.state(FinaleProgress.STATE);
        MinotaurEntity first = FinaleRegistry.MINOTAUR.get().create(h.getLevel());
        MinotaurEntity second = FinaleRegistry.MINOTAUR.get().create(h.getLevel());
        try {
            var bow = new ItemStack(Items.BOW);
            UUID original = WeaponHistory.stamp(bow);
            var other = new ItemStack(Items.BOW);
            WeaponHistory.stamp(other);
            Runnable fight = () -> {
                CompoundTag record = new CompoundTag();
                record.putUUID("Weapon", original);
                record.putString("Phase", FinaleProgress.Phase.FIGHT.name());
                FinaleProgress.save(server, player.getUUID(), record);
            };
            fight.run();
            first.owner(player.getUUID());
            first.moveTo(player.position().add(0, 0, 4));
            first.stagger();
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            var stray = new Arrow(h.getLevel(), player, new ItemStack(Items.ARROW), other);
            h.assertTrue(!first.hurt(player.damageSources().arrow(stray, player), 5) && first.motion() == MinotaurEntity.STUNNED, "an arrow from another bow passes through");
            var shot = new Arrow(h.getLevel(), player, new ItemStack(Items.ARROW), bow);
            h.assertTrue(!shot.getPersistentData().hasUUID(WeaponHistory.ORIGINAL), "no House mark on this projectile");
            h.assertTrue(first.hurt(player.damageSources().arrow(shot, player), 5) && first.motion() == MinotaurEntity.WOUNDED, "one whose weapon is the original wounds it");
            stray.discard();
            shot.discard();

            fight.run();
            second.owner(player.getUUID());
            second.moveTo(player.position().add(0, 0, -4));
            second.stagger();
            var blow = new DamageSource(player.damageSources().playerAttack(player).typeHolder(), player) {
                @Override
                public ItemStack getWeaponItem() {
                    return bow;
                }
            };
            h.assertTrue(second.hurt(blow, 5) && second.motion() == MinotaurEntity.WOUNDED, "and so does a blow whose source names the original, the hand empty");
        } finally {
            data.setState(FinaleProgress.STATE, previous);
            first.discard();
            second.discard();
            if (server.getPlayerList().getPlayers().contains(player)) {
                server.getPlayerList().remove(player);
            }
        }
        h.succeed();
    }
}
