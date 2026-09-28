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

    private HouseDimensions() {
    }
}
