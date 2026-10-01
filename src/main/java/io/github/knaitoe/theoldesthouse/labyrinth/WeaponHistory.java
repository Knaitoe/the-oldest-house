package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Identity belongs to the physical weapon; a House duplicate never inherits it. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class WeaponHistory {
    public static final String ORIGINAL = "HouseOriginalWeapon", COPY = "HouseWeaponCopy";
    private static final String STATE = "weapon_history_049";
    private WeaponHistory() {}
    public static boolean weapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof TridentItem || item instanceof MaceItem;
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
    /** Existing saves have item-type statistics but no instance history. Seed each type once. */
    public static void seed(ServerPlayer player) {
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag root = data.state(STATE);
        CompoundTag history = root.getCompound(player.getStringUUID());
        if (history.getBoolean("Seeded")) return;
        java.util.Set<Item> seen = new java.util.HashSet<>();
        for (ItemStack stack : player.getInventory().items) {
            if (weapon(stack) && seen.add(stack.getItem()))
                record(player, stack, Math.max(1, player.getStats().getValue(Stats.ITEM_USED.get(stack.getItem()))));
        }
        root = data.state(STATE); history = root.getCompound(player.getStringUUID()); history.putBoolean("Seeded", true);
        root.put(player.getStringUUID(), history); data.setState(STATE, root);
    }
    @Nullable public static UUID favorite(ServerPlayer player) {
        seed(player); CompoundTag history = LabyrinthData.get(player.server).state(STATE).getCompound(player.getStringUUID());
        UUID best = null; int uses = -1;
        for (String key : new java.util.TreeSet<>(history.getAllKeys())) {
            if (key.equals("Seeded")) continue;
            int count = history.getCompound(key).getInt("Uses");
            if (count > uses) { try { best = UUID.fromString(key); uses = count; } catch (IllegalArgumentException ignored) {} }
        }
        return best;
    }
    public static ItemStack sample(ServerPlayer player, UUID id) {
        return ItemStack.parseOptional(player.registryAccess(), LabyrinthData.get(player.server).state(STATE)
                .getCompound(player.getStringUUID()).getCompound(id.toString()).getCompound("Sample"));
    }
    public static boolean wounds(ItemStack stack, @Nullable UUID original) { return original != null && original.equals(identity(stack)); }
    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(event.getTarget() instanceof MinotaurEntity))
            record(player, player.getMainHandItem(), 1);
    }
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && (event.getItemStack().getItem() instanceof BowItem
                || event.getItemStack().getItem() instanceof CrossbowItem || event.getItemStack().getItem() instanceof TridentItem))
            record(player, event.getItemStack(), 1);
    }
    @SubscribeEvent public static void toss(net.neoforged.neoforge.event.entity.item.ItemTossEvent event) {
        if(event.getPlayer() instanceof ServerPlayer player&&weapon(event.getEntity().getItem())&&identity(event.getEntity().getItem())==null)
            record(player,event.getEntity().getItem(),Math.max(1,player.getStats().getValue(Stats.ITEM_USED.get(event.getEntity().getItem().getItem()))));
    }
}
