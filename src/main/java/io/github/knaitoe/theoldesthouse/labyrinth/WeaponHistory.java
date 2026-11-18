package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Identity belongs to the physical weapon; a House duplicate never inherits it. A weapon is the game's own kind, or another mod's in
 * NeoForge's common tags {@code #c:tools/melee_weapon} and {@code #c:tools/ranged_weapon}, so another mod's weapon can be the reader's
 * favourite and be carried to the Minotaur (the owner, 17 November: "Yes, to favorite weapon"; docs/NEIGHBOURS.md). A use is a vanilla
 * attack that goes through, a vanilla bow, crossbow or trident drawn or thrown, or any other blow a weapon deals for the reader, by the
 * damage source, once a tick however many it hurts. Nothing else is: a weapon let fall is no use, and the game's item statistics (which
 * count every block mined with a tool) are never read (docs/NEIGHBOURS.md R7).
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class WeaponHistory {
    public static final String ORIGINAL = "HouseOriginalWeapon", COPY = "HouseWeaponCopy";
    private static final String STATE = "weapon_history_049";
    /** The tick of each reader's last vanilla attack, already counted, so its own damage is not counted again. */
    private static final Map<UUID, Long> SWUNG = new HashMap<>();
    /** The weapons each reader's blows were counted for on the tick of their last counted blow: a blow that hurts several is one use. */
    private static final Map<UUID, Blows> BLOWN = new HashMap<>();
    private record Blows(long tick, Set<UUID> weapons) {}
    private WeaponHistory() {}
    public static boolean weapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof TridentItem || item instanceof MaceItem
                || stack.is(Tags.Items.MELEE_WEAPON_TOOLS) || stack.is(Tags.Items.RANGED_WEAPON_TOOLS);
    }
    /** A vanilla bow, crossbow or trident: a use is counted when it is drawn or thrown, never again when what it loosed hits. */
    static boolean drawn(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem;
    }
    @Nullable public static UUID identity(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.getBoolean(COPY) && tag.hasUUID(ORIGINAL) ? tag.getUUID(ORIGINAL) : null;
    }
    @Nullable public static UUID stamp(ItemStack stack) {
        if (!weapon(stack) || stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(COPY)) return null;
        UUID id = identity(stack); if (id != null) return id;
        id = UUID.randomUUID(); UUID original = id;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(ORIGINAL, original)); return id;
    }
    public static void markCopy(ItemStack stack) {
        if (!weapon(stack)) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.remove(ORIGINAL); tag.putBoolean(COPY, true); });
    }
    public static void record(ServerPlayer player, ItemStack stack, int uses) {
        UUID id = stamp(stack); if (id == null) return;
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag root = data.state(STATE);
        CompoundTag history = root.getCompound(player.getStringUUID()), entry = history.getCompound(id.toString());
        entry.putInt("Uses", Math.min(Integer.MAX_VALUE - 1000, entry.getInt("Uses") + uses));
        entry.put("Sample", stack.save(player.registryAccess())); history.put(id.toString(), entry);
        root.put(player.getStringUUID(), history); data.setState(STATE, root);
    }
    /** The reader's favourite: the weapon the House has counted the most uses of. Asking stamps and counts nothing. */
    @Nullable public static UUID favorite(ServerPlayer player) {
        CompoundTag history = LabyrinthData.get(player.server).state(STATE).getCompound(player.getStringUUID());
        UUID best = null; int uses = -1;
        for (String key : new java.util.TreeSet<>(history.getAllKeys())) {
            if (key.equals("Seeded")) continue; // an older save's mark that it was seeded from the game's item statistics
            int count = history.getCompound(key).getInt("Uses");
            if (count > uses) { try { best = UUID.fromString(key); uses = count; } catch (IllegalArgumentException ignored) {} }
        }
        return best;
    }
    /**
     * 0.4.704 (docs/FAVORITE_ITEM_0_4_704.md): the one thing a reader left on the hall stand, handed to them at the cell door. Whatever it
     * is, a sword or a spade, it is given an identity of its own if it has none and recorded as the reader's most used, so the Minotaur
     * answers to it and the witness, the ending and a death in the fight all know it as the original.
     */
    public static UUID keepsake(ServerPlayer player, ItemStack stack) {
        UUID id = identity(stack);
        if (id == null) { UUID made = UUID.randomUUID(); id = made; CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.remove(COPY); tag.putUUID(ORIGINAL, made); }); }
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag root = data.state(STATE);
        CompoundTag history = root.getCompound(player.getStringUUID()); int most = 0;
        for (String key : history.getAllKeys()) if (!key.equals("Seeded") && !key.equals(id.toString())) most = Math.max(most, history.getCompound(key).getInt("Uses"));
        CompoundTag entry = history.getCompound(id.toString());
        entry.putInt("Uses", Math.max(entry.getInt("Uses"), Math.min(Integer.MAX_VALUE - 1000, most + 1)));
        entry.put("Sample", stack.save(player.registryAccess())); history.put(id.toString(), entry);
        root.put(player.getStringUUID(), history); data.setState(STATE, root);
        return id;
    }
    /** How many uses the House has counted for this weapon of the reader's. */
    public static int uses(ServerPlayer player, UUID id) {
        return LabyrinthData.get(player.server).state(STATE).getCompound(player.getStringUUID()).getCompound(id.toString()).getInt("Uses");
    }
    public static ItemStack sample(ServerPlayer player, UUID id) {
        return ItemStack.parseOptional(player.registryAccess(), LabyrinthData.get(player.server).state(STATE)
                .getCompound(player.getStringUUID()).getCompound(id.toString()).getCompound("Sample"));
    }
    public static boolean wounds(ItemStack stack, @Nullable UUID original) { return original != null && original.equals(identity(stack)); }
    /** Nothing is recorded in a neighbour's world that is not the reader's own (docs/NEIGHBOURS.md R3): the House does not watch there. */
    public static boolean watched(ServerPlayer player) { return !Neighbours.notOwnWorld(player.level()); }
    /** Last, so an attack another listener cancels (another mod's weapon taking the swing for its own) is no use and marks nothing. */
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void attack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SWUNG.put(player.getUUID(), player.level().getGameTime());
        if (watched(player) && !(event.getTarget() instanceof MinotaurEntity)) record(player, player.getMainHandItem(), 1);
    }
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && watched(player) && drawn(event.getItemStack()))
            record(player, event.getItemStack(), 1);
    }
    /**
     * Any other blow a weapon deals for the reader is a use, by its damage source: another mod's melee blow, whose source carries the
     * reader's main hand, and a projectile, whose source carries the weapon that loosed it. Not the damage of a vanilla attack, its sweep
     * included, which was counted as the attack on the same tick; not a vanilla bow's, crossbow's or trident's, counted when drawn or
     * thrown; not thorns, which the reader's armour deals. A weapon's blow is counted once a tick, however many it hurts (another mod's
     * sweep, a thrust through two, a volley), as a vanilla sweep is one attack.
     */
    @SubscribeEvent public static void blow(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || event.getEntity() == player || event.getEntity() instanceof MinotaurEntity
                || source.is(DamageTypes.THORNS) || !watched(player)) return;
        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || weapon.isEmpty() || !weapon(weapon) || drawn(weapon)) return;
        if (source.getDirectEntity() == player && Long.valueOf(player.level().getGameTime()).equals(SWUNG.get(player.getUUID()))) return;
        ItemStack dealt = dealtBy(player, weapon);
        if (dealt == null) return;
        long now = player.level().getGameTime();
        Blows blows = BLOWN.get(player.getUUID());
        if (blows == null || blows.tick() != now) BLOWN.put(player.getUUID(), blows = new Blows(now, new HashSet<>()));
        UUID counted = identity(dealt);
        if (counted != null && blows.weapons().contains(counted)) return;
        record(player, dealt, 1);
        counted = identity(dealt);
        if (counted != null) blows.weapons().add(counted);
    }
    /**
     * The reader's own stack that dealt a blow with {@code weapon}: the one in a hand that is it, or is the same thing wear aside (a
     * projectile carries a copy of what loosed it, which is stamped on the real one); else the copy itself if it carries an identity. A
     * copy with no identity was taken before the weapon was first stamped, as the projectile joined the world, so the hand's identity and
     * the Mother's marks are set aside for it ({@link HouseKeeping#same}).
     */
    @Nullable static ItemStack dealtBy(ServerPlayer player, ItemStack weapon) {
        boolean known = identity(weapon) != null;
        for (ItemStack hand : new ItemStack[] { player.getMainHandItem(), player.getOffhandItem() }) {
            if (hand == weapon) return hand;
            if (hand.isEmpty() || hand.getItem() != weapon.getItem()) continue;
            if (!known) { if (HouseKeeping.same(hand, weapon)) return hand; continue; }
            ItemStack a = hand.copyWithCount(1), b = weapon.copyWithCount(1);
            a.remove(DataComponents.DAMAGE); b.remove(DataComponents.DAMAGE);
            if (ItemStack.isSameItemSameComponents(a, b)) return hand;
        }
        return known ? weapon : null;
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        SWUNG.remove(event.getEntity().getUUID());
        BLOWN.remove(event.getEntity().getUUID());
    }
}
