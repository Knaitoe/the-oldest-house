package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

/**
 * The manor's own paintings. Painting variants are data-driven in 1.21.1;
 * these keys point at the matching files under
 * data/the_oldest_house/painting_variant and textures/painting.
 */
public final class HousePaintings {
    public static final ResourceKey<PaintingVariant> NAVIDSON_MANOR = key("navidson_manor");
    public static final ResourceKey<PaintingVariant> LAKE_EVENING = key("lake_evening");
    public static final ResourceKey<PaintingVariant> WILL_PORTRAIT = key("will_portrait");
    public static final ResourceKey<PaintingVariant> KAREN_PORTRAIT = key("karen_portrait");
    public static final ResourceKey<PaintingVariant> CHILD_PORTRAIT = key("child_portrait");
    public static final ResourceKey<PaintingVariant> OLD_MAN_PORTRAIT = key("old_man_portrait");
    public static final ResourceKey<PaintingVariant> UNKNOWN_WOMAN_PORTRAIT = key("unknown_woman_portrait");
    public static final ResourceKey<PaintingVariant> SCHOLAR_AT_DESK = key("scholar_at_desk");
    public static final ResourceKey<PaintingVariant> ARCHITECTURAL_STUDY = key("architectural_study");
    public static final ResourceKey<PaintingVariant> FAMILY_TABLE = key("family_table");
    public static final ResourceKey<PaintingVariant> HOUSE_DRAWING = key("house_drawing");
    public static final ResourceKey<PaintingVariant> EMPTY_CHAIR = key("empty_chair");
    public static final ResourceKey<PaintingVariant> WINTER_ROAD = key("winter_road");
    public static final ResourceKey<PaintingVariant> THE_YARD = key("the_yard");
    public static final ResourceKey<PaintingVariant> BRASS_CLOCK = key("brass_clock");
    public static final ResourceKey<PaintingVariant> PRESSED_FERN = key("pressed_fern");

    public static final List<ResourceKey<PaintingVariant>> ALL = List.of(
            NAVIDSON_MANOR, LAKE_EVENING,
            WILL_PORTRAIT, KAREN_PORTRAIT, CHILD_PORTRAIT, OLD_MAN_PORTRAIT, UNKNOWN_WOMAN_PORTRAIT,
            SCHOLAR_AT_DESK, ARCHITECTURAL_STUDY, FAMILY_TABLE, HOUSE_DRAWING, EMPTY_CHAIR,
            WINTER_ROAD, THE_YARD, BRASS_CLOCK, PRESSED_FERN
    );

    private HousePaintings() {
    }

    private static ResourceKey<PaintingVariant> key(String id) {
        return ResourceKey.create(Registries.PAINTING_VARIANT,
                ResourceLocation.parse(TheOldestHouse.MOD_ID + ":" + id));
    }
}
