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
        h.runAfterDelay(20, () -> {
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
                h.assertTrue(led == 1, "wearing the cap, the cow is led after them: led " + led + ", cow at " + cow.position().distanceTo(p.position()) + ", ground " + cow.onGround());
                // The diary is silent by day.
                h.assertTrue(h.getLevel().getDayTime() % 24000 >= 13000 || Keepsakes.diary(p) == null, "the last page says nothing in daylight");
                h.succeed();
            } finally { NativeTestPlayers.remove(p); }
        });
    }
}
