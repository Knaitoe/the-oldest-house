package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Keeps ordinary vanilla hostile spawns out of the Labyrinth.
 *
 * The House's authored encounters are responsible for anything dangerous
 * that appears here. An authored vanilla monster can explicitly opt in by
 * receiving {@link #ALLOWED_TAG} before it is added to the level.
 */
public final class LabyrinthSpawnRules {
    public static final String ALLOWED_TAG = "the_oldest_house_labyrinth_allowed";

    private LabyrinthSpawnRules() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = event.getEntity();
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (shouldBlock(level.dimension(), origin, entity.getType(), entity.blockPosition(),
                entity.getTags().contains(ALLOWED_TAG))) {
            event.setCanceled(true);
        }
    }

    /**
     * Pure predicate kept separate so the geometry/category contract is
     * covered by GameTests without having to manufacture a spawn event.
     */
    public static boolean shouldBlock(
            ResourceKey<Level> dimension,
            @Nullable BlockPos houseOrigin,
            EntityType<?> type,
            BlockPos position,
            boolean explicitlyAllowed
    ) {
        if (explicitlyAllowed
                || houseOrigin == null
                || !dimension.equals(HouseDimensions.INTERIOR)
                || type.getCategory() != MobCategory.MONSTER
                || !isVanilla(type)) {
            return false;
        }
        return LabyrinthPlaces.isInStack(houseOrigin, position) || FinaleArchitecture.contains(houseOrigin, position);
    }

    private static boolean isVanilla(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null && "minecraft".equals(key.getNamespace());
    }

    /** Mark a deliberately scripted vanilla hostile before addFreshEntity. */
    public static <T extends Entity> T allowInLabyrinth(T entity) {
        entity.addTag(ALLOWED_TAG);
        return entity;
    }
}
