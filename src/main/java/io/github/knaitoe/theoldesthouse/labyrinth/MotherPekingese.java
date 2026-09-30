package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

/** The den's small dog has a breed-specific model; wolves elsewhere keep their ordinary renderer. */
public final class MotherPekingese extends Wolf {
    public MotherPekingese(EntityType<? extends Wolf> type, Level level) { super(type, level); }
}
