package io.github.knaitoe.theoldesthouse.opening;

import com.mojang.serialization.Codec;
import java.util.Locale;

/** Where one player stands in the opening sequence. Strictly increasing. */
public enum OpeningStage {
    NONE,
    ELIGIBLE,
    LETTER_DELIVERED,
    DOOR_PLACED,
    ENTERED;

    public static final Codec<OpeningStage> CODEC = Codec.STRING.xmap(
            OpeningStage::byName,
            stage -> stage.name().toLowerCase(Locale.ROOT)
    );

    public static OpeningStage byName(String name) {
        for (OpeningStage stage : values()) {
            if (stage.name().equalsIgnoreCase(name)) {
                return stage;
            }
        }
        return NONE;
    }

    public boolean isAtLeast(OpeningStage other) {
        return ordinal() >= other.ordinal();
    }
}
