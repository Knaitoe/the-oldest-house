package io.github.knaitoe.theoldesthouse.house;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

/**
 * Positional audio continuity across the ordinary House boundary.
 *
 * The real sound remains authoritative in the dimension where it happened.
 * A matching seeded sound is replayed at the same coordinates in the other
 * dimension so windows and open doors sound physically adjacent.
 *
 * Only mundane/domestic space participates. Nothing from the impossible hall,
 * the room between rooms, vignettes or deeper labyrinth can leak into the
 * Overworld facade, and nothing from the Overworld reaches them. Its reach
 * is the visual mirror's own shared region
 * ({@link HouseDimensionMirror#isSharedPosition}), so what can be seen across
 * the seam and what can be heard across it are always the same place.
 */
public final class HouseSoundBridge {
    private static final ThreadLocal<Boolean> REPLAYING =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private HouseSoundBridge() {
    }

    public static void onSoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        if (!(event.getLevel() instanceof ServerLevel source)) {
            return;
        }
        mirror(
                source,
                event.getPosition(),
                event.getSound(),
                event.getSource(),
                event.getNewVolume(),
                event.getNewPitch()
        );
    }

    public static void onSoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getLevel() instanceof ServerLevel source)) {
            return;
        }

        Entity entity = event.getEntity();
        if (HouseExteriorEntityMirror.isProjection(entity)) {
            return;
        }

        mirror(
                source,
                entity.position(),
                event.getSound(),
                event.getSource(),
                event.getNewVolume(),
                event.getNewPitch()
        );
    }

    private static void mirror(
            ServerLevel source,
            Vec3 position,
            @Nullable Holder<SoundEvent> sound,
            SoundSource category,
            float volume,
            float pitch
    ) {
        if (REPLAYING.get()
                || sound == null
                || volume <= 0.0F
                || !bridgedCategory(category)) {
            return;
        }

        MinecraftServer server = source.getServer();
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || !data.isInteriorInitialized() || origin == null) {
            return;
        }

        ServerLevel target = targetLevel(server, source, origin, position);
        if (target == null || target.players().isEmpty()) {
            return;
        }

        // The replay itself fires PlayLevelSoundEvent too. Guard it rather
        // than maintaining a fragile list of "mirrored" sound events.
        REPLAYING.set(Boolean.TRUE);
        try {
            target.playSeededSound(
                    null,
                    position.x,
                    position.y,
                    position.z,
                    sound,
                    category,
                    volume,
                    pitch,
                    source.getRandom().nextLong()
            );
        } finally {
            REPLAYING.set(Boolean.FALSE);
        }
    }

    @Nullable
    static ServerLevel targetLevel(
            MinecraftServer server,
            ServerLevel source,
            BlockPos origin,
            Vec3 position
    ) {
        BlockPos pos = BlockPos.containing(position);

        if (source.dimension().equals(Level.OVERWORLD)) {
            if (!isAudibleOverworldPosition(origin, pos)) {
                return null;
            }
            return server.getLevel(HouseDimensions.INTERIOR);
        }

        if (source.dimension().equals(HouseDimensions.INTERIOR)) {
            if (!isAudibleDomesticPosition(origin, pos)) {
                return null;
            }
            return server.overworld();
        }

        return null;
    }

    /**
     * Exterior/threshold sounds can cross inward anywhere the visual mirror is
     * valid. The Overworld proxy contains no authoritative living occupants,
     * so allowing the whole shared shell also preserves ordinary door and
     * block sounds at the facade.
     */
    public static boolean isAudibleOverworldPosition(BlockPos origin, BlockPos pos) {
        return HouseDimensionMirror.isSharedPosition(origin, pos);
    }

    /**
     * Only ordinary domestic House sounds cross outward. Impossible geometry
     * and subtle unmirrored changes stay acoustically private.
     */
    public static boolean isAudibleDomesticPosition(BlockPos origin, BlockPos pos) {
        return HouseDimensionMirror.isDomesticPosition(origin, pos)
                && !HouseDimensionMirror.isUnmirrored(origin, pos);
    }

    /**
     * Keep the bridge spatial and physical. Music/records/ambience/weather are
     * either non-positional presentation or already synchronized elsewhere.
     */
    public static boolean bridgedCategory(SoundSource source) {
        return source == SoundSource.BLOCKS
                || source == SoundSource.HOSTILE
                || source == SoundSource.NEUTRAL
                || source == SoundSource.PLAYERS;
    }
}
