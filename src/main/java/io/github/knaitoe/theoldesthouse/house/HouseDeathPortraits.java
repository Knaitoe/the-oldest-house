package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardVariety;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** The first actual House death claims one existing family portrait, once per persistent player UUID. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class HouseDeathPortraits {
    public static final String STATE = "house_death_portraits_04111";
    public static final String ORIGINAL = "HouseFamilyPortrait04111";
    private record Pending(ServerPlayer player, int deathsBefore, PortraitProfile profile, long origin) {}
    private static final Map<MinecraftServer, Map<UUID, Pending>> PENDING = new WeakHashMap<>();
    private HouseDeathPortraits() {}

    public static CompoundTag record(ServerPlayer player) { return record(player.server, player.getUUID()); }
    public static CompoundTag record(MinecraftServer server, UUID player) {
        return LabyrinthData.get(server).stateEntry(STATE, player.toString());
    }
    private static void save(MinecraftServer server, UUID id, CompoundTag own) {
        LabyrinthData.get(server).setStateEntry(STATE, id.toString(), own);
    }
    private static int deaths(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dying(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()
                || !HouseDimensions.isHouseDimension(player.level().dimension())
                || record(player).getBoolean("FirstDeath")) return;
        var house = HouseSavedData.get(player.server);
        if (house.houseOrigin() == null || house.isOutdated() || house.isCollapsed()) return;
        PENDING.computeIfAbsent(player.server, ignored -> new LinkedHashMap<>()).putIfAbsent(player.getUUID(),
                new Pending(player, deaths(player), PortraitProfile.of(player.getGameProfile()), house.houseOrigin().asLong()));
    }

    /** Native death statistics are awarded only after the entire cancellable death event has succeeded. */
    public static void confirm(MinecraftServer server) {
        var pending = PENDING.remove(server);
        if (pending == null) return;
        for (var entry : pending.entrySet()) {
            var candidate = entry.getValue(); var own = record(server, entry.getKey());
            if (deaths(candidate.player()) <= candidate.deathsBefore() || own.getBoolean("FirstDeath")) continue;
            own.putBoolean("FirstDeath", true); own.putBoolean("Preview", false);
            own.putLong("Origin", candidate.origin()); own.putString("Profile", candidate.profile().encode());
            own.putLong("DeathTime", server.overworld().getGameTime());
            own.putBoolean("Pending", !own.hasUUID("Painting"));
            save(server, entry.getKey(), own);
        }
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal() instanceof ServerPlayer player) confirm(player.server);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) confirm(player.server);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { PENDING.remove(event.getServer()); }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        confirm(event.getServer());
        if (event.getServer().getTickCount() % 20 == 0) place(event.getServer());
    }

    /** Only authored, still-present original family paintings are candidates; never recreate a removed picture. */
    public static void markOriginal(Painting painting) {
        if (family(painting)) painting.getPersistentData().putBoolean(ORIGINAL, true);
    }
    private static boolean family(Painting painting) {
        return List.of(HousePaintings.WILL_PORTRAIT, HousePaintings.KAREN_PORTRAIT, HousePaintings.CHILD_PORTRAIT,
                        HousePaintings.OLD_MAN_PORTRAIT, HousePaintings.UNKNOWN_WOMAN_PORTRAIT).stream()
                .anyMatch(key -> painting.getVariant().is(key));
    }
    public static boolean claimed(Painting painting) { return !((PortraitPainting) painting).housePortrait().isEmpty(); }

    private static boolean ready(ServerLevel level, Painting painting) {
        var at = painting.getPos();
        return level.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4) != null
                && level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX() >> 4, at.getZ() >> 4)) && painting.survives();
    }
    private static boolean watched(ServerLevel level, Painting painting) {
        return HazardVariety.watched(level, painting.getPos())
                || HazardVariety.watched(level, painting.getPos().above());
    }
    public static void place(MinecraftServer server) {
        var house = HouseSavedData.get(server); var origin = house.houseOrigin();
        var level = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || level == null || house.isOutdated() || house.isCollapsed()) return;
        var pictures = level.getEntitiesOfClass(Painting.class, new AABB(origin).inflate(40));
        pictures.sort(Comparator.comparingLong(p -> p.getPos().asLong()));
        var data = LabyrinthData.get(server);
        var all = data.state(STATE);
        for (var id : new TreeSet<>(all.getAllKeys())) {
            var own = all.getCompound(id);
            if (!own.getBoolean("Pending") || own.hasUUID("Painting") || own.getLong("Origin") != origin.asLong()) continue;
            var profile = PortraitProfile.decode(own.getString("Profile")); if (profile == null) continue;
            for (var picture : pictures) {
                if (!picture.getPersistentData().getBoolean(ORIGINAL) || !family(picture) || claimed(picture)
                        || !ready(level, picture) || watched(level, picture)
                        || watched(server.overworld(), picture)) continue;
                ((PortraitPainting) picture).housePortrait(profile.encode());
                own.putUUID("Painting", picture.getUUID()); own.putLong("At", picture.getPos().asLong());
                own.putBoolean("Pending", false); data.setStateEntry(STATE, id, own);
                break;
            }
        }
    }

    /** A reversible operator preview uses the same placement path but never spends the actual first death. */
    public static boolean preview(ServerPlayer player) {
        if (!HomeKnocking.present(player)) return false;
        var own = record(player); if (own.getBoolean("FirstDeath")) return false;
        if (own.getBoolean("Preview")) return true;
        own.putBoolean("Preview", true); own.putBoolean("Pending", true);
        own.putLong("Origin", HouseSavedData.get(player.server).houseOrigin().asLong());
        own.putString("Profile", PortraitProfile.of(player.getGameProfile()).encode());
        save(player.server, player.getUUID(), own); return true;
    }
    public static boolean resetPreview(ServerPlayer player) {
        var own = record(player); if (own.getBoolean("FirstDeath")) return false;
        if (own.hasUUID("Painting")) {
            var level = player.server.getLevel(HouseDimensions.INTERIOR);
            var picture = level == null ? null : level.getEntity(own.getUUID("Painting"));
            if (!(picture instanceof Painting)) return false; // Do not forget a still-unloaded original.
            var access = (PortraitPainting) picture; var profile = PortraitProfile.decode(access.housePortrait());
            if (profile != null && profile.owner().equals(player.getUUID())) access.housePortrait("");
        }
        save(player.server, player.getUUID(), new CompoundTag()); return true;
    }
}
