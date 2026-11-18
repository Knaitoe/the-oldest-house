package io.github.knaitoe.theoldesthouse.mixin;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The two halves of a double chest, so the House can tell where a chest a reader opened stands (house/Containers). */
@Mixin(CompoundContainer.class)
public interface CompoundContainerAccess {
    @Accessor("container1")
    Container theOldestHouse$first();

    @Accessor("container2")
    Container theOldestHouse$second();
}
