package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;

/** The Mother's actual collection; the frames in her den are only views of it. */
public final class MotherCollection extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_mother";
    public static final String CLAIM = "the_oldest_house_mother_claim";
    public static final String CARRIED = "the_oldest_house_mother_carried";
    public static final String CARRIER = "the_oldest_house_mother_carrier";
    public static final int LOVED_AFTER = 20 * 60 * 10;
    public static final Factory<MotherCollection> FACTORY =
            new Factory<>(MotherCollection::new, MotherCollection::load);

    public static final class Entry {
        public final UUID id;
        public final boolean pet;
        public final boolean loved;
        @Nullable public final UUID owner;
        public CompoundTag contents;
        public final String name;
        @Nullable public UUID claimant;
        public int remaining;
        public boolean sealed;

        private Entry(UUID id, boolean pet, boolean loved, @Nullable UUID owner,
                      CompoundTag contents, String name) {
            this.id = id;
            this.pet = pet;
            this.loved = loved;
            this.owner = owner;
            this.contents = contents.copy();
            this.name = name;
        }

        public ItemStack item(HolderLookup.Provider registries) {
            return pet ? ItemStack.EMPTY : ItemStack.parseOptional(registries, contents);
        }
    }

    private final Map<UUID, Entry> entries = new LinkedHashMap<>();
    private final Map<UUID, UUID> debts = new HashMap<>();
    private final Set<UUID> inDen = new HashSet<>();
    private final Set<UUID> usedVisit = new HashSet<>();
    private final Set<UUID> rememberedPets = new HashSet<>();
    private final Set<UUID> kindToHer = new HashSet<>();
    private final Set<UUID> tookShelves = new HashSet<>();
    private final Map<UUID, Integer> shelfPages = new HashMap<>();
    private long revision;
    private boolean seeded;
    private int hungerTicks;
    private int dogThreatTicks;
    private int dogLedgeTicks;
    @Nullable private UUID dogThreatOwner;
    private boolean dogAtLedge;
    private boolean dogThrown;
    private boolean dogGone;
    private boolean dogRescued;
    private boolean returned;
    private boolean offered;
    private boolean banished;

    public static MotherCollection get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static boolean meaningful(ItemStack stack) {
        return !stack.isEmpty() && (stack.isDamageableItem() || stack.isEnchanted()
                || stack.has(DataComponents.CUSTOM_NAME)
                || stack.has(DataComponents.WRITTEN_BOOK_CONTENT)
                || stack.has(DataComponents.MAP_ID)
                || stack.has(DataComponents.JUKEBOX_PLAYABLE)
                || stack.is(Items.COMPASS) || stack.is(Items.RECOVERY_COMPASS)
                || stack.is(Items.CLOCK) || stack.is(Items.SPYGLASS)
                || VignetteYields.of(stack) != null);
    }

    public static boolean loved(ItemStack stack, long now, @Nullable UUID carrier) {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.CUSTOM_NAME) || stack.isEnchanted()) return true;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return carrier != null && tag.hasUUID(CARRIER) && carrier.equals(tag.getUUID(CARRIER))
                && tag.contains(CARRIED, Tag.TAG_LONG) && now - tag.getLong(CARRIED) >= LOVED_AFTER;
    }

    /** Stamp only already-meaningful, unstackable objects; ordinary resources still stack normally. */
    public static void rememberCarried(ItemStack stack, UUID player, long now) {
        if (!meaningful(stack) || stack.getMaxStackSize() != 1) return;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.hasUUID(CARRIER) && player.equals(tag.getUUID(CARRIER)) && tag.contains(CARRIED)) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putUUID(CARRIER, player);
            data.putLong(CARRIED, now);
        });
    }

    @Nullable
    public Entry keepItem(ItemStack stack, HolderLookup.Provider registries, @Nullable UUID owner, long now) {
        if (!meaningful(stack)) return null;
        UUID existingClaim = claimOf(stack);
        // A borrowed object that despawns returns to its own shelf, not a second shelf.
        if (existingClaim != null) {
            Entry existing = entries.get(existingClaim);
            if (existing != null && !existing.pet && existing.claimant != null) {
                existing.remaining = Math.max(0, existing.remaining - stack.getCount());
                if (existing.remaining == 0) {
                    debts.remove(existing.claimant);
                    existing.claimant = null;
                }
                changed();
                return existing;
            }
        }
        ItemStack stored = withoutClaim(stack);
        Entry entry = new Entry(UUID.randomUUID(), false, loved(stored, now, owner), owner,
                (CompoundTag) stored.save(registries), stored.getHoverName().getString());
        entries.put(entry.id, entry);
        changed();
        return entry;
    }

    @Nullable
    public Entry keepPet(UUID original, CompoundTag contents, @Nullable UUID owner, String name) {
        if (!rememberedPets.add(original)) return null;
        Entry entry = new Entry(UUID.randomUUID(), true, true, owner, contents, name);
        entries.put(entry.id, entry);
        changed();
        return entry;
    }

    public void keepFinaleItem(ItemStack stack, HolderLookup.Provider registries, UUID owner, long now) {
        if (stack.isEmpty()) return;
        Entry entry = keepItem(stack, registries, owner, now);
        if (entry == null) {
            ItemStack stored = withoutClaim(stack);
            entry = new Entry(UUID.randomUUID(), false, loved(stored, now, owner), owner,
                    (CompoundTag) stored.save(registries), stored.getHoverName().getString());
            entries.put(entry.id, entry);
        }
        entry.sealed = true; entry.claimant = null; entry.remaining = 0; debts.remove(owner); changed();
    }
    public boolean canGuide(UUID player) { return wasKind(player) || !tookShelves.contains(player); }
    public boolean releaseFinalePet(UUID id) { Entry entry = entries.get(id); if (entry == null || !entry.pet) return false; entries.remove(id); changed(); return true; }

    public List<Entry> all() { return List.copyOf(entries.values()); }
    @Nullable public Entry entry(UUID id) { return entries.get(id); }
    @Nullable public Entry debt(UUID player) { return entry(debts.get(player)); }
    public long revision() { return revision; }
    public boolean usedVisit(UUID player) { return usedVisit.contains(player); }
    public boolean wasKind(UUID player) { return kindToHer.contains(player); }
    public boolean banished() { return banished; }
    public boolean salved() { return returned && offered && dogRescued && !banished; }
    public boolean dogGone() { return dogGone; }
    public boolean dogRescued() { return dogRescued; }
    public int hungerTicks() { return hungerTicks; }
    public int dogThreatTicks() { return dogThreatTicks; }
    public int dogLedgeTicks() { return dogLedgeTicks; }
    @Nullable public UUID dogThreatOwner() { return dogThreatOwner; }
    public boolean dogAtLedge() { return dogAtLedge; }
    public boolean dogThrown() { return dogThrown; }
    public boolean anyDebt() { return !debts.isEmpty(); }

    public float corruption() {
        return salved() || banished ? 0.0F
                : Math.min(1.0F, Math.max(hungerTicks / 3600.0F, dogThreatTicks / 900.0F));
    }

    /** Only active House visits advance a threat; logging out or going home never kills a pet off-screen. */
    public void advance(int ticks, boolean activeDebt, boolean activeDogThreat) {
        if (salved() || banished) {
            if (hungerTicks != 0) { hungerTicks = 0; setDirty(); }
            return;
        }
        if (activeDebt) { hungerTicks += ticks; setDirty(); }
        else if (hungerTicks > 0 && !anyDebt()) { hungerTicks = Math.max(0, hungerTicks - ticks); setDirty(); }
        if (dogThreatOwner != null && activeDogThreat && !dogThrown) {
            dogThreatTicks += ticks;
            if (dogAtLedge) dogLedgeTicks += ticks;
            setDirty();
        }
    }

    public boolean beginDogThreat(UUID player) {
        if (dogGone || dogRescued || salved() || banished || dogThreatOwner != null) return false;
        dogThreatOwner = player;
        dogThreatTicks = 0;
        dogLedgeTicks = 0;
        dogAtLedge = false;
        dogThrown = false;
        setDirty();
        return true;
    }

    public void dogAtLedge(boolean value) { dogAtLedge = value; dogLedgeTicks = 0; setDirty(); }
    public void dogThrown(boolean value) { dogThrown = value; setDirty(); }

    public void endDogThreat(boolean rescued) {
        dogRescued |= rescued;
        dogGone |= !rescued;
        dogThreatOwner = null;
        dogThreatTicks = 0;
        dogLedgeTicks = 0;
        dogAtLedge = false;
        dogThrown = false;
        // Violence restores the ordinary mask immediately. Mercy settles it gradually unless the three acts are complete.
        hungerTicks = rescued ? Math.max(0, hungerTicks - 1800) : 0;
        changed();
    }

    public boolean offer(UUID player, ItemStack held, HolderLookup.Provider registries, long now) {
        if (claimOf(held) != null || !loved(held, now, player)) return false;
        keepItem(held.copyWithCount(1), registries, player, now);
        held.shrink(1);
        offered = true;
        kindToHer.add(player);
        hungerTicks = Math.max(0, hungerTicks - 1800);
        changed();
        return true;
    }

    public boolean banish() {
        if (!salved() || anyDebt() || dogThreatOwner != null) return false;
        banished = true;
        hungerTicks = 0;
        changed();
        return true;
    }

    /** A reclamation is satiation, not healing. It does not satisfy the peaceful return condition. */
    public boolean brutalReclaim(UUID player, ItemStack held, HolderLookup.Provider registries, long now) {
        Entry entry = debt(player);
        if (entry == null || held.isEmpty()) return false;
        if (entry.id.equals(claimOf(held))) {
            ItemStack original = entry.item(registries);
            if (held.getCount() < entry.remaining || !held.is(original.getItem())) return false;
            held.shrink(entry.remaining);
            entry.remaining = 0;
            entry.claimant = null;
        } else {
            if (claimOf(held) != null || !meaningful(held)) return false;
            keepItem(held.copyWithCount(1), registries, player, now);
            held.shrink(1);
            retainReturnedPart(entry, registries);
        }
        debts.remove(player);
        hungerTicks = 0;
        changed();
        return true;
    }

    /** Persist the threshold, so disconnecting in the den does not buy another retrieval. */
    public void presence(UUID player, boolean here) {
        if (here && inDen.add(player)) {
            usedVisit.remove(player);
            setDirty();
        } else if (!here && inDen.remove(player)) {
            setDirty();
        }
    }

    /** Latest fifty ordinary objects plus the loved ones. Extra loved objects get further shelf pages. */
    public List<Entry> visible() {
        List<Entry> result = new ArrayList<>();
        List<Entry> ordinary = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (entry.claimant != null || entry.pet) continue;
            if (entry.loved) result.add(entry); else ordinary.add(entry);
        }
        int start = Math.max(0, ordinary.size() - 50);
        result.addAll(ordinary.subList(start, ordinary.size()));
        return result;
    }

    public int nextPage(UUID player, int pages) {
        int page = Math.floorMod(shelfPages.getOrDefault(player, 0) + 1, Math.max(1, pages));
        shelfPages.put(player, page);
        setDirty();
        return page;
    }

    /** Server-thread atomic claim: the shelf becomes empty before the stack is delivered. */
    public ItemStack claimItem(UUID player, UUID id, HolderLookup.Provider registries) {
        Entry entry = entries.get(id);
        if (entry == null || entry.pet || entry.sealed || entry.claimant != null || (!banished && usedVisit(player)) || debt(player) != null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = entry.item(registries);
        if (stack.isEmpty()) return stack;
        tookShelves.add(player);
        if (salved() || banished) {
            entries.remove(id);
            usedVisit.add(player);
            changed();
            return stack;
        }
        entry.claimant = player;
        entry.remaining = stack.getCount();
        debts.put(player, id);
        usedVisit.add(player);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(CLAIM, id));
        changed();
        return stack;
    }

    public boolean returnItem(UUID player, ItemStack held, HolderLookup.Provider registries) {
        Entry entry = debt(player);
        UUID claim = claimOf(held);
        if (entry == null || entry.pet || !entry.id.equals(claim)) return false;
        ItemStack original = entry.item(registries);
        if (held.getCount() < entry.remaining || !held.is(original.getItem())) return false;
        held.shrink(entry.remaining);
        entry.remaining = 0;
        entry.claimant = null;
        debts.remove(player);
        returned = true;
        kindToHer.add(player);
        hungerTicks = Math.max(0, hungerTicks - 1200);
        changed();
        return true;
    }

    /** Exchange one loved thing for the whole claim, preserving both objects' components. */
    public boolean trade(UUID player, ItemStack held, HolderLookup.Provider registries, long now) {
        Entry entry = debt(player);
        if (entry == null || entry.pet || claimOf(held) != null || !loved(held, now, player)) return false;
        keepItem(held.copyWithCount(1), registries, player, now);
        held.shrink(1);
        retainReturnedPart(entry, registries);
        debts.remove(player);
        kindToHer.add(player);
        hungerTicks = Math.max(0, hungerTicks - 1800);
        changed();
        return true;
    }

    /** A split stack may already have partly expired; a trade must not erase that returned part. */
    private void retainReturnedPart(Entry entry, HolderLookup.Provider registries) {
        ItemStack original = entry.item(registries);
        int recovered = Math.max(0, original.getCount() - entry.remaining);
        if (recovered == 0) entries.remove(entry.id);
        else {
            entry.contents = (CompoundTag) original.copyWithCount(recovered).save(registries);
            entry.remaining = 0;
            entry.claimant = null;
        }
    }

    /** Pets are recovered by an explicit loved-object exchange; the original owner alone can do it. */
    public boolean canRecoverPet(UUID player, UUID id, ItemStack offering, long now) {
        Entry entry = entries.get(id);
        return entry != null && entry.pet && !entry.sealed && player.equals(entry.owner)
                && (banished || !usedVisit(player)) && debt(player) == null
                && (salved() || banished || (claimOf(offering) == null && loved(offering, now, player)));
    }

    public boolean recoverPet(UUID player, UUID id, ItemStack offering, HolderLookup.Provider registries, long now) {
        if (!canRecoverPet(player, id, offering, now)) return false;
        if (!salved() && !banished) {
            keepItem(offering.copyWithCount(1), registries, player, now);
            offering.shrink(1);
        }
        entries.remove(id);
        usedVisit.add(player);
        kindToHer.add(player);
        changed();
        return true;
    }

    public boolean seedOnce() {
        if (seeded) return false;
        seeded = true;
        setDirty();
        return true;
    }

    public void recordDogRecovery(UUID player) {
        dogRescued = true;
        dogThreatOwner = null;
        dogThreatTicks = 0;
        dogLedgeTicks = 0;
        dogAtLedge = false;
        dogThrown = false;
        usedVisit.add(player);
        kindToHer.add(player);
        hungerTicks = Math.max(0, hungerTicks - 1800);
        changed();
    }

    @Nullable
    public static UUID claimOf(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID(CLAIM) ? tag.getUUID(CLAIM) : null;
    }

    public static ItemStack withoutClaim(ItemStack source) {
        ItemStack stack = source.copy();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(CLAIM);
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private void changed() { revision++; setDirty(); }

    public static MotherCollection load(CompoundTag tag, HolderLookup.Provider registries) {
        MotherCollection data = new MotherCollection();
        ListTag entries = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag saved = entries.getCompound(i);
            if (!saved.hasUUID("Id")) continue;
            Entry entry = new Entry(saved.getUUID("Id"), saved.getBoolean("Pet"), saved.getBoolean("Loved"),
                    saved.hasUUID("Owner") ? saved.getUUID("Owner") : null,
                    saved.getCompound("Contents"), saved.getString("Name"));
            entry.sealed = saved.getBoolean("Sealed");
            if (saved.hasUUID("Claimant")) {
                entry.claimant = saved.getUUID("Claimant");
                entry.remaining = saved.contains("Remaining") ? saved.getInt("Remaining") : entry.item(registries).getCount();
                data.debts.put(entry.claimant, entry.id);
            }
            data.entries.put(entry.id, entry);
        }
        readIds(tag, "InDen", data.inDen);
        readIds(tag, "UsedVisit", data.usedVisit);
        readIds(tag, "PetsRemembered", data.rememberedPets);
        readIds(tag, "Kind", data.kindToHer);
        readIds(tag, "TookShelves", data.tookShelves);
        data.tookShelves.addAll(data.debts.keySet());
        data.seeded = tag.getBoolean("Seeded");
        data.revision = tag.getLong("Revision");
        data.hungerTicks = tag.getInt("Hunger");
        data.dogThreatTicks = tag.getInt("DogThreatTicks");
        data.dogLedgeTicks = tag.getInt("DogLedgeTicks");
        data.dogThreatOwner = tag.hasUUID("DogThreatOwner") ? tag.getUUID("DogThreatOwner") : null;
        data.dogAtLedge = tag.getBoolean("DogAtLedge");
        data.dogThrown = tag.getBoolean("DogThrown");
        data.dogGone = tag.getBoolean("DogGone");
        data.dogRescued = tag.getBoolean("DogRescued");
        data.returned = tag.getBoolean("Returned");
        data.offered = tag.getBoolean("Offered");
        data.banished = tag.getBoolean("Banished");
        return data;
    }

    private static void readIds(CompoundTag tag, String key, Set<UUID> destination) {
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Id")) destination.add(entry.getUUID("Id"));
        }
    }

    private static ListTag ids(Set<UUID> source) {
        ListTag list = new ListTag();
        for (UUID id : source) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id);
            list.add(tag);
        }
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries.values()) {
            CompoundTag saved = new CompoundTag();
            saved.putUUID("Id", entry.id);
            saved.putBoolean("Pet", entry.pet);
            saved.putBoolean("Sealed", entry.sealed);
            saved.putBoolean("Loved", entry.loved);
            if (entry.owner != null) saved.putUUID("Owner", entry.owner);
            if (entry.claimant != null) {
                saved.putUUID("Claimant", entry.claimant);
                saved.putInt("Remaining", entry.remaining);
            }
            saved.put("Contents", entry.contents.copy());
            saved.putString("Name", entry.name);
            list.add(saved);
        }
        tag.put("Entries", list);
        tag.put("InDen", ids(inDen));
        tag.put("UsedVisit", ids(usedVisit));
        tag.put("PetsRemembered", ids(rememberedPets));
        tag.put("Kind", ids(kindToHer));
        tag.put("TookShelves", ids(tookShelves));
        tag.putBoolean("Seeded", seeded);
        tag.putLong("Revision", revision);
        tag.putInt("Hunger", hungerTicks);
        tag.putInt("DogThreatTicks", dogThreatTicks);
        tag.putInt("DogLedgeTicks", dogLedgeTicks);
        if (dogThreatOwner != null) tag.putUUID("DogThreatOwner", dogThreatOwner);
        tag.putBoolean("DogAtLedge", dogAtLedge);
        tag.putBoolean("DogThrown", dogThrown);
        tag.putBoolean("DogGone", dogGone);
        tag.putBoolean("DogRescued", dogRescued);
        tag.putBoolean("Returned", returned);
        tag.putBoolean("Offered", offered);
        tag.putBoolean("Banished", banished);
        return tag;
    }
}
