package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class HouseDimensions {
    public static final ResourceKey<Level> INTERIOR = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "house_interior")
    );

    /**
     * The outside dimension: an empty void with a sky, holding every place
     * that needs one (captured copies of the player's own base first).
     */
    public static final ResourceKey<Level> OUTSIDE = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "outside")
    );

    /**
     * The space between rooms: an empty void holding rooms the manor has no
     * room for, at the same coordinates as the part of the house they open
     * from. A door in the manor leads here without the player's position
     * changing, exactly as the front door leads into the interior.
     */
    public static final ResourceKey<Level> BETWEEN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "between")
    );

    private HouseDimensions() {
    }
}
