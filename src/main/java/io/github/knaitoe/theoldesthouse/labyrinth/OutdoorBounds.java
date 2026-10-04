package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The outside scenes' edges are land and buildings now, not invisible walls.
 * Anyone who still gets past them, by pearl, by climbing or by a fall, is put
 * back where they last stood inside the scene. Creative and spectator players
 * are left alone, as is anyone the scene never saw standing inside it.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class OutdoorBounds {
    /** How far past its walkable area a scene still claims a player as its own. */
    private static final int NEIGHBOURHOOD = 64;
    private static final Map<UUID, Vec3> SAFE = new HashMap<>();

    private OutdoorBounds() {}

    /** A scene's walkable area, relative to its base: x and z inclusive, and its lowest legitimate y. */
    record Area(int minX, int maxX, int minZ, int maxZ, int minY) {
        boolean contains(double x, double y, double z) {
            return x >= minX && x < maxX + 1 && z >= minZ && z < maxZ + 1 && y >= minY;
        }
    }

    static Area area(LabyrinthPlace place) {
        var r = place.room();
        int vestibule = LabyrinthPlaces.VESTIBULE_DEPTH + 1;
        return switch (place) {
            case PLAIN -> new Area(-Landscapes.PLAIN_HALF_WIDTH - 1, Landscapes.PLAIN_HALF_WIDTH + 1, Landscapes.PLAIN_FAR, vestibule, r.minY() - 3);
            case ZAMPANO_COURTYARD -> new Area(-14, 14, -40, vestibule, r.minY() - 3);
            case BARN_WELL -> new Area(-19, 19, -41, vestibule, r.minY() - 3);
            default -> new Area(r.minX() - 2, r.maxX() + 2, r.minZ() - 2, vestibule, r.minY() - 4);
        };
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var outside = server.getLevel(HouseDimensions.OUTSIDE);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (outside == null || origin == null) return;
        for (ServerPlayer player : List.copyOf(outside.players())) check(player, origin);
    }

    /** Returns true when the player was put back inside a scene. */
    public static boolean check(ServerPlayer player, BlockPos origin) {
        // The game mode itself, not isCreative(): creative and spectator players fly where they like.
        if (!player.gameMode.isSurvival() || !player.isAlive()) return false;
        Vec3 back = SAFE.get(player.getUUID());
        BlockPos home = null;
        for (var area : LiteraryCopies.outdoorAreas(player.server)) {
            if (insideCopy(area, player.position())) {
                if (player.onGround() || player.isInWater() || player.isPassenger()) SAFE.put(player.getUUID(), player.position());
                return false;
            }
            if (back != null && insideCopy(area, back)
                    && player.getX() >= area.minX - NEIGHBOURHOOD && player.getX() <= area.maxX + NEIGHBOURHOOD
                    && player.getZ() >= area.minZ - NEIGHBOURHOOD && player.getZ() <= area.maxZ + NEIGHBOURHOOD) {
                if (player.isPassenger()) player.stopRiding();
                player.teleportTo(back.x, back.y, back.z);
                player.setDeltaMovement(Vec3.ZERO);
                player.resetFallDistance();
                player.hurtMarked = true;
                return true;
            }
        }
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (!NovelRooms.outside(place)) continue;
            BlockPos base = LabyrinthPlaces.base(origin, place);
            if (base == null) continue;
            Area area = area(place);
            if (area.contains(player.getX() - base.getX(), player.getY() - base.getY(), player.getZ() - base.getZ())) {
                if (player.onGround() || player.isInWater() || player.isPassenger()) SAFE.put(player.getUUID(), player.position());
                return false;
            }
            // Only the scene the player was last seen standing inside claims them, and only from its neighbourhood.
            if (back != null && area.contains(back.x - base.getX(), back.y - base.getY(), back.z - base.getZ())
                    && near(area, player.getX() - base.getX(), player.getZ() - base.getZ())) home = base;
        }
        if (home == null) return false;
        if (player.isPassenger()) player.stopRiding();
        player.teleportTo(back.x, back.y, back.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.hurtMarked = true;
        return true;
    }

    private static boolean insideCopy(net.minecraft.world.phys.AABB area, Vec3 point) {
        return point.x >= area.minX && point.x < area.maxX && point.z >= area.minZ && point.z < area.maxZ && point.y >= area.minY;
    }

    private static boolean near(Area area, double x, double z) {
        return x >= area.minX() - NEIGHBOURHOOD && x <= area.maxX() + NEIGHBOURHOOD
                && z >= area.minZ() - NEIGHBOURHOOD && z <= area.maxZ() + NEIGHBOURHOOD;
    }

    @Nullable
    static Vec3 lastSafe(UUID id) { return SAFE.get(id); }

    /** For tests: record where a player last stood, as a real step onto the ground would. */
    public static void remember(ServerPlayer player) { SAFE.put(player.getUUID(), player.position()); }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { SAFE.remove(event.getEntity().getUUID()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { SAFE.clear(); }
}
