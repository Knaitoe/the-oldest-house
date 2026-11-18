package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.mixin.CompoundContainerAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Where a container a reader has open stands in the world. */
public final class Containers {
    private Containers() {
    }

    /**
     * The blocks a container stands on: a chest, barrel or the like where it is, both halves of a double chest, a cart where it is.
     * A container that stands nowhere of its own (an ender chest's contents, one a story opens) is where {@code opener} stands.
     */
    public static List<BlockPos> where(Container container, Entity opener) {
        List<BlockPos> at = new ArrayList<>();
        collect(container, at);
        if (at.isEmpty()) {
            at.add(opener.blockPosition());
        }
        return at;
    }

    /** The chests, barrels and the like a container is made of, each where it stands; none for a container that stands nowhere. */
    public static List<BlockEntity> blocks(Container container) {
        List<BlockEntity> blocks = new ArrayList<>();
        blocks(container, blocks);
        return blocks;
    }

    private static void blocks(Container container, List<BlockEntity> blocks) {
        if (container instanceof BlockEntity block) {
            blocks.add(block);
        } else if (container instanceof CompoundContainer compound) {
            CompoundContainerAccess halves = (CompoundContainerAccess) compound;
            blocks(halves.theOldestHouse$first(), blocks);
            blocks(halves.theOldestHouse$second(), blocks);
        }
    }

    private static void collect(Container container, List<BlockPos> at) {
        if (container instanceof BlockEntity block) {
            at.add(block.getBlockPos());
        } else if (container instanceof Entity entity) {
            at.add(entity.blockPosition());
        } else if (container instanceof CompoundContainer compound) {
            CompoundContainerAccess halves = (CompoundContainerAccess) compound;
            collect(halves.theOldestHouse$first(), at);
            collect(halves.theOldestHouse$second(), at);
        }
    }
}
