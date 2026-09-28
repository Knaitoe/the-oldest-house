package io.github.knaitoe.theoldesthouse.opening;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

/**
 * World-level records for the opening sequence.
 *
 * The 0.3.0 entrance-door and Hillary-return records are deliberately no
 * longer loaded or saved. Old NBT keys are simply ignored, so existing worlds
 * migrate without a destructive data conversion.
 */
public final class OpeningWorldData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_opening";

    /**
     * Where Navidson's photo of a player's house was taken: the copy's level
     * and corner, the camera position/look angles and the lit window.
     */
    public record PhotoRecord(
            ResourceLocation dimension,
            BlockPos copyMin,
            Vec3 camera,
            float yaw,
            float pitch,
            @Nullable BlockPos window,
            boolean windowCarved
    ) {
    }

    public static final Factory<OpeningWorldData> FACTORY =
            new Factory<>(OpeningWorldData::new, OpeningWorldData::load);

    private final Map<UUID, PhotoRecord> photos = new HashMap<>();

    public OpeningWorldData() {
    }

    public static OpeningWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static OpeningWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        OpeningWorldData data = new OpeningWorldData();

        ListTag photoList = tag.getList("Photos", Tag.TAG_COMPOUND);
        for (int i = 0; i < photoList.size(); i++) {
            CompoundTag entry = photoList.getCompound(i);
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dimension"));
            if (!entry.hasUUID("Player") || dimension == null) {
                continue;
            }
            data.photos.put(entry.getUUID("Player"), new PhotoRecord(
                    dimension,
                    BlockPos.of(entry.getLong("CopyMin")),
                    new Vec3(
                            entry.getDouble("CameraX"),
                            entry.getDouble("CameraY"),
                            entry.getDouble("CameraZ")
                    ),
                    entry.getFloat("Yaw"),
                    entry.getFloat("Pitch"),
                    entry.contains("Window") ? BlockPos.of(entry.getLong("Window")) : null,
                    entry.getBoolean("WindowCarved")
            ));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag photoList = new ListTag();
        for (Map.Entry<UUID, PhotoRecord> mapEntry : photos.entrySet()) {
            PhotoRecord photo = mapEntry.getValue();
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", mapEntry.getKey());
            entry.putString("Dimension", photo.dimension().toString());
            entry.putLong("CopyMin", photo.copyMin().asLong());
            entry.putDouble("CameraX", photo.camera().x);
            entry.putDouble("CameraY", photo.camera().y);
            entry.putDouble("CameraZ", photo.camera().z);
            entry.putFloat("Yaw", photo.yaw());
            entry.putFloat("Pitch", photo.pitch());
            if (photo.window() != null) {
                entry.putLong("Window", photo.window().asLong());
            }
            entry.putBoolean("WindowCarved", photo.windowCarved());
            photoList.add(entry);
        }
        tag.put("Photos", photoList);
        return tag;
    }

    @Nullable
    public PhotoRecord photoOf(UUID player) {
        return photos.get(player);
    }

    public void putPhoto(UUID player, PhotoRecord photo) {
        photos.put(player, photo);
        setDirty();
    }
}
