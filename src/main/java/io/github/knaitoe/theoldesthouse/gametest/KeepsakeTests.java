package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** 0.4.192: every keepsake does something, for its own reader only. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class KeepsakeTests {
    private static ItemStack keepsake(net.minecraft.server.level.ServerPlayer p, LabyrinthPlace place, String key, ItemStack s) { return LiteraryVignettes.mark(p, place, key, s); }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theToothKnifeFootAndPhotographEachWorkOnlyForTheirReader(GameTestHelper h) {
        var p = NativeTestPlayers.survival(h, "keeper");var other = NativeTestPlayers.survival(h, "not_keeper");
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
        var at = h.absoluteVec(new Vec3(2.5, 1, 2.5));p.teleportTo(h.getLevel(), at.x, at.y, at.z, 0, 0);other.teleportTo(h.getLevel(), at.x + 3, at.y, at.z, 0, 0);
        try {
            // The tooth: only in the keeper's off hand.
            p.setItemInHand(InteractionHand.OFF_HAND, keepsake(p, LabyrinthPlace.ELK_FAN, "Tooth", new ItemStack(LiteraryRegistry.TOOTH.get())));
            other.setItemInHand(InteractionHand.OFF_HAND, p.getOffhandItem().copy());
            h.assertTrue(Keepsakes.tooth(p) && p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= Keepsakes.TOOTH_RESIST && !Keepsakes.tooth(other),
                    "the tooth weighs its own keeper down, and nobody else");
            p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            h.assertTrue(!Keepsakes.tooth(p) && p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0, "out of the hand, the weight goes");
            // The rabbit's foot: luck while carried.
            p.getInventory().setItem(9, keepsake(p, LabyrinthPlace.HOLY_RABBIT, "RabbitFoot", new ItemStack(Items.RABBIT_FOOT)));
            h.assertTrue(Keepsakes.carries(p, LabyrinthPlace.HOLY_RABBIT, "RabbitFoot") && !Keepsakes.carries(other, LabyrinthPlace.HOLY_RABBIT, "RabbitFoot"), "the foot is carried by its keeper");
            // Father's knife: one more cooked portion.
            p.setItemInHand(InteractionHand.MAIN_HAND, keepsake(p, LabyrinthPlace.HOLY_RABBIT, "Knife", new ItemStack(LiteraryRegistry.KNIFE.get())));
            var cow = h.spawn(EntityType.COW, new BlockPos(4, 1, 4));
            var area = new AABB(cow.blockPosition()).inflate(4);
            cow.hurt(p.damageSources().playerAttack(p), 100);
            var cooked = h.getLevel().getEntitiesOfClass(ItemEntity.class, area, i -> i.getItem().is(Items.COOKED_BEEF));
            h.assertTrue(cow.isDeadOrDying() && cooked.size() == 1, "the cow killed with Father's knife leaves one cooked portion: " + cooked.size());
            cooked.forEach(ItemEntity::discard);
            var second = h.spawn(EntityType.COW, new BlockPos(5, 1, 5));
            second.hurt(other.damageSources().playerAttack(other), 100);
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(second.blockPosition()).inflate(4), i -> i.getItem().is(Items.COOKED_BEEF)).isEmpty(), "anyone else's kill leaves nothing extra");
            h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(BlockPos.ZERO)).inflate(12)).forEach(ItemEntity::discard);
            // The brother's photograph: the keeper's own companion takes half.
            var wolf = h.spawn(EntityType.WOLF, new BlockPos(3, 1, 3));wolf.tame(p);
            var stray = h.spawn(EntityType.WOLF, new BlockPos(5, 1, 3));stray.tame(other);
            float full = wolf.getHealth();
            p.getInventory().setItem(10, keepsake(p, LabyrinthPlace.MAPPING_INTERIOR, "Photograph", new ItemStack(LiteraryRegistry.PHOTOGRAPH.get())));
            wolf.hurt(h.getLevel().damageSources().generic(), 4);stray.hurt(h.getLevel().damageSources().generic(), 4);
            h.assertTrue(Math.abs(full - wolf.getHealth() - 2) < .01 && Math.abs(stray.getMaxHealth() - stray.getHealth() - 4) < .01,
                    "the keeper's companion takes half, another's the whole: " + (full - wolf.getHealth()) + " / " + (stray.getMaxHealth() - stray.getHealth()));
        } finally { NativeTestPlayers.remove(p);NativeTestPlayers.remove(other); }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theBulbCylindersLeafAndCapChangeTheReadersWorld(GameTestHelper h) {
        var p = NativeTestPlayers.survival(h, "keeper_world");
        for (int x = 0; x < 9; x++) for (int z = 0; z < 9; z++) h.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
        var at = h.absoluteVec(new Vec3(2.5, 1, 2.5));p.teleportTo(h.getLevel(), at.x, at.y, at.z, 0, 0);
        var cow = h.spawn(EntityType.COW, new BlockPos(7, 1, 7));
        var zombie = h.spawn(EntityType.ZOMBIE, new BlockPos(7, 1, 2));
        // Run 1351: the empty template keeps only its own corner loaded, and the cow can stand in the next chunk, where nothing
        // ticks it or finds it. Hold the corner's chunks and wait until the cow is ticking and has settled onto the grass.
        var chunks = new NativeTestChunks();chunks.hold(h.getLevel(), new net.minecraft.world.phys.AABB(h.absolutePos(BlockPos.ZERO)).inflate(16));
        h.startSequence().thenWaitUntil(() -> h.assertTrue(h.getTick() >= 20 && chunks.ticking() && h.getLevel().isPositionEntityTicking(cow.blockPosition()) && cow.onGround(), "the cow is ticking and settled: tick " + h.getTick() + ", ticking " + h.getLevel().isPositionEntityTicking(cow.blockPosition()) + ", ground " + cow.onGround() + ", alive " + cow.isAlive() + " at " + h.relativePos(cow.blockPosition()))).thenExecute(() -> {
            try {
                // The bulb: a light where they stand, then not again for a while.
                var bulb = keepsake(p, LabyrinthPlace.ELK_FAN, "Lightbulb", new ItemStack(LiteraryRegistry.LIGHTBULB.get()));
                p.setItemInHand(InteractionHand.MAIN_HAND, bulb);
                h.assertTrue(Keepsakes.bulb(p, bulb) && h.getLevel().getBlockState(p.blockPosition().above()).is(Blocks.LIGHT) && !Keepsakes.bulb(p, bulb),
                        "the bulb hangs a light where the reader stands, once in a while");
                Keepsakes.lightsOut(h.getLevel().getServer(), true);
                h.assertTrue(h.getLevel().getBlockState(p.blockPosition().above()).isAir(), "and the light goes out");
                // The cylinders: a playing jukebox keeps monsters from spawning near it.
                var box = new BlockPos(1, 1, 7);h.setBlock(box, Blocks.JUKEBOX);
                var jukebox = (JukeboxBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(box));
                jukebox.setTheItem(keepsake(p, LabyrinthPlace.CRIMSON_HALL, "Cylinder0", new ItemStack(LiteraryRegistry.CYLINDER_ONE.get())));
                Keepsakes.ward(h.getLevel(), h.absolutePos(box), p.getUUID());
                h.assertTrue(jukebox.getSongPlayer().isPlaying() && Keepsakes.warded(h.getLevel(), h.absoluteVec(new Vec3(20, 1, 20))) && !Keepsakes.warded(h.getLevel(), h.absoluteVec(new Vec3(80, 1, 80))),
                        "while the cylinder plays, no monster spawns within its reach");
                jukebox.popOutTheItem();
                h.assertTrue(!Keepsakes.warded(h.getLevel(), h.absoluteVec(new Vec3(20, 1, 20))), "taken out, the house is not kept");
                // The register leaf: the dead pass over them until close, or until struck.
                h.assertTrue(!Keepsakes.unnoticed(p, zombie), "without the leaf the dead notice them");
                p.getInventory().setItem(9, keepsake(p, LabyrinthPlace.USHER, "RegisterLeaf", new ItemStack(Items.WRITTEN_BOOK)));
                h.assertTrue(Keepsakes.unnoticed(p, zombie), "carrying the leaf, a zombie five blocks off does not notice them");
                zombie.setLastHurtByMob(p);
                h.assertTrue(!Keepsakes.unnoticed(p, zombie), "struck, it does");
                // The cap: worn, the herd follows.
                h.assertTrue(Keepsakes.herd(p) == 0, "bareheaded, nothing follows");
                p.setItemSlot(EquipmentSlot.HEAD, keepsake(p, LabyrinthPlace.ELK_LOT, "Cap", ElkFollows.cap()));
                int led = Keepsakes.herd(p);
                // Animals in a neighbouring test's corner may be in reach too; this corner's cow must be one of them, walking.
                h.assertTrue(led >= 1 && cow.getNavigation().isInProgress(), "wearing the cap, the cow is led after them: led " + led + ", cow at " + cow.position().distanceTo(p.position()) + ", ground " + cow.onGround());
                // The diary is silent by day.
                h.assertTrue(h.getLevel().getDayTime() % 24000 >= 13000 || Keepsakes.diary(p) == null, "the last page says nothing in daylight");
                h.succeed();
            } finally { NativeTestPlayers.remove(p);chunks.close(); }
        });
    }

    /**
     * 0.4.198: four more keepsakes. A leaf held in a plain hall marks the door back; the vulture photograph, held still, keeps
     * far monsters from fixing on its keeper; her compass points to where they last fell; her patch keeps what is in hand,
     * once a House day, when they die away from the House.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theLeavesPhotographCompassAndPatchEachDoWhatTheirStoryDid(GameTestHelper h) {
        var p = NativeTestPlayers.survival(h, "keeper_four");
        var server = h.getLevel().getServer();var store = server.overworld().getDataStorage();var old = LabyrinthData.get(server);
        var at = h.absoluteVec(new Vec3(2.5, 1, 2.5));p.teleportTo(h.getLevel(), at.x, at.y, at.z, 0, 0);
        for (int x = 0; x < 9; x++) for (int z = 0; z < 9; z++) h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
        // The photograph: a keeper standing still with it in hand.
        var photo = new ItemStack(Items.FILLED_MAP);
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, photo, t -> t.putUUID(NovelVignettes.PHOTO_OWNER, p.getUUID()));
        p.setItemInHand(InteractionHand.MAIN_HAND, photo);Keepsakes.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        var far = h.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 8 + 1));var near = h.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 3));
        h.runAfterDelay(30, () -> {
            try {
                h.assertTrue(Keepsakes.holdingStill(p), "a second held still with the photograph in hand");
                far.setTarget(p);near.setTarget(p);
                h.assertTrue(far.getTarget() == null && near.getTarget() == p, "the far one cannot fix on them, the near one can");
                // The compass, away from the House: where they last fell.
                var fell = p.blockPosition().east(40);
                p.setLastDeathLocation(java.util.Optional.of(net.minecraft.core.GlobalPos.of(p.level().dimension(), fell)));
                var line = Keepsakes.compass(p);
                h.assertTrue(line.contains("east") && line.contains("where you last fell") && line.contains("40"), "the needle settles toward where they last fell: " + line);
                // The patch: what is in hand when they fall, once a House day.
                p.getInventory().setItem(9, LiteraryVignettes.mark(p, LabyrinthPlace.BORRASCA, "Patch", new ItemStack(BorrascaRegistry.PATCH.get())));
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
                var kept = Keepsakes.sew(p);
                h.assertTrue(kept.is(Items.DIAMOND_SWORD) && p.getMainHandItem().isEmpty() && Keepsakes.record(p).contains("Sewn"), "the patch keeps the sword out of the fall");
                h.assertTrue(Keepsakes.unsew(p) && p.getInventory().contains(new ItemStack(Items.DIAMOND_SWORD)) && !Keepsakes.record(p).contains("Sewn"), "and gives it back on waking");
                p.getInventory().clearOrCountMatchingItems(st -> st.is(Items.DIAMOND_SWORD), -1, p.inventoryMenu.getCraftSlots());
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                h.assertTrue(Keepsakes.sew(p).isEmpty() && p.getMainHandItem().is(Items.IRON_AXE), "only once a House day");
                h.assertTrue(AccountAfterwards.patch(Keepsakes.record(p)).contains("once"), "the account says what the patch kept");
                // The leaves: in a plain hall, the door back.
                var origin = new BlockPos(41000, 80, 41000);var d = new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION, origin);store.set("the_oldest_house_labyrinth", d);
                var interior = HouseTestLevel.get(server, io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR);
                var base = LabyrinthPlaces.base(origin, LabyrinthPlace.STRAIGHT_HALL);
                p.teleportTo(interior, base.getX() + .5, base.getY(), base.getZ() - 10.5, 0, 0);
                h.assertTrue(Keepsakes.leafWay(p) == null, "empty-handed, nothing marks the way");
                p.setItemInHand(InteractionHand.MAIN_HAND, io.github.knaitoe.theoldesthouse.journal.ZampanoLeafItem.of(0));
                h.assertTrue(base.offset(0, 0, 1).equals(Keepsakes.leafWay(p)), "with a leaf held up in a plain hall, the ash drifts at the door back: " + Keepsakes.leafWay(p));
                h.succeed();
            } finally { store.set("the_oldest_house_labyrinth", old);NativeTestPlayers.remove(p); }
        });
    }

    /** 0.4.194: the owner, "Ensure you're updating literature too": the accounts, the leaves and the keepsakes say what happened afterwards. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void theAccountsLeavesAndKeepsakesSayWhatHappenedAfterwards(GameTestHelper h) {
        var d = new LabyrinthData();var id = java.util.UUID.randomUUID();
        // Harrigan: kept frees him; buried rots by the names.
        var harrigan = new net.minecraft.nbt.CompoundTag();harrigan.putBoolean("KeptHarriganPhone", true);harrigan.putInt("KindSent0188", 2);
        h.assertTrue(AccountAfterwards.harrigan(harrigan).contains("never asked me for a name"), "kept, the account says he is freed");
        harrigan = new net.minecraft.nbt.CompoundTag();harrigan.putBoolean("Contact", true);harrigan.putInt("TotalCalls", 1);
        String slowed = AccountAfterwards.harrigan(harrigan);harrigan.putInt("TotalCalls", 9);
        h.assertTrue(slowed.contains("slowed") && AccountAfterwards.harrigan(harrigan).contains("missed call"), "buried, the account follows the rot");
        // Ted, Tell-Tale, the hides, the diner, the elk and the Lady.
        var trades = new net.minecraft.nbt.CompoundTag();trades.putInt("Unwound", 3);trades.putInt("Unease", 2);trades.putBoolean("HideWashed", true);
        h.assertTrue(AccountAfterwards.caver(trades).contains("three times") && AccountAfterwards.caver(trades).endsWith("Then it let go.")
                && AccountAfterwards.floorboards(trades).contains("twice") && AccountAfterwards.hides(trades).contains("until water took it off"), "the trades are written down");
        var elk = new net.minecraft.nbt.CompoundTag();elk.putInt(ElkFollows.STRUCK_VISIT, 2);
        h.assertTrue(AccountAfterwards.elk(elk).contains("let me pass") && ElkLotEnding.account("Reader", elk).startsWith("Once I raised a hand to her"), "the Last Stop's account begins with the night she followed");
        var bly = new net.minecraft.nbt.CompoundTag();bly.putInt(BlyFollows.CARRIED, 1);bly.putBoolean("Dragged", true);
        h.assertTrue(AccountAfterwards.bly(bly).contains("once") && LiteraryTexts.ending("Reader", LabyrinthPlace.BLY_ROUTE, bly).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream()
                .anyMatch(page -> page.raw().getString().contains("walked me to the edge")), "Bly's account remembers the carries");
        // The Witness account: a resolved room, the manor's echo of it and the trades after it.
        WitnessAccount.resolve(d, id, WitnessAccount.Story.WALLPAPER, "collected_the_pages");WitnessAccount.resolve(d, id, WitnessAccount.Story.TED_CAVER, "retraced_the_squeeze");
        var remembers = new net.minecraft.nbt.CompoundTag();remembers.putLong("Since_wallpaper_nursery", 0);d.setStateEntry(io.github.knaitoe.theoldesthouse.house.HouseRemembers.STATE, id.toString(), remembers);
        d.setStateEntry(ContestTrades.STATE, id.toString(), trades);
        var pages = WitnessAccount.book(d, id, "Reader", false).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page -> page.raw().getString()).toList();
        h.assertTrue(pages.stream().anyMatch(t -> t.contains("Afterwards the pattern came to the manor")) && pages.stream().anyMatch(t -> t.contains("pulled my line back")),
                "the Witness account carries what happened afterwards");
        // The keepsakes say what they do; the House of Leaves counts what the manor kept and what the cave took back.
        var p = NativeTestPlayers.survival(h, "afterwards_reader");
        try {
            var tooth = LiteraryVignettes.mark(p, LabyrinthPlace.ELK_FAN, "Tooth", new ItemStack(LiteraryRegistry.TOOTH.get()));
            var lore = tooth.get(net.minecraft.core.component.DataComponents.LORE);
            h.assertTrue(lore != null && lore.lines().stream().anyMatch(c -> c.getString().contains("keeps your feet under you")), "the tooth says what it does");
            var cap = LiteraryVignettes.mark(p, LabyrinthPlace.ELK_LOT, "Cap", ElkFollows.cap());
            h.assertTrue(cap.get(net.minecraft.core.component.DataComponents.LORE).lines().size() == 2, "the cap keeps its own line and gains one");
            var live = LabyrinthData.get(h.getLevel().getServer());
            live.setStateEntry(io.github.knaitoe.theoldesthouse.house.HouseRemembers.STATE, p.getStringUUID(), remembers.copy());
            live.setStateEntry(ContestTrades.STATE, p.getStringUUID(), trades.copy());
            var facts = StaircaseAccount.facts(p, new net.minecraft.nbt.CompoundTag());
            h.assertTrue(facts.stream().anyMatch(f -> f.kind().id().equals("remembered") && f.value().contains("pattern")) && facts.stream().anyMatch(f -> f.kind().id().equals("unwound") && f.count() == 3),
                    "the leaves can tell of the manor's echo and the line taken back: " + facts.stream().map(f -> f.kind().id()).toList());
        } finally { NativeTestPlayers.remove(p); }
        h.succeed();
    }
}
