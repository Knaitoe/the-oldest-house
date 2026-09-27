package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Prints the generated House as a compact, line-oriented block dump so the
 * architecture can be rendered and reviewed outside the game. Lines are
 * prefixed so they can be filtered out of a server log:
 *
 * <pre>
 * OTH-DUMP|BEGIN|minX|minY|minZ|sizeX|sizeY|sizeZ
 * OTH-DUMP|P|index|block-state
 * OTH-DUMP|L|y|z|cells      (two base-62 characters per cell, x ascending)
 * OTH-DUMP|E|painting|x|y|z|facing|variant
 * OTH-DUMP|END
 * </pre>
 */
final class HouseStructureDump {
    private static final String PREFIX = "OTH-DUMP|";
    private static final String DIGITS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private HouseStructureDump() {
    }

    static void print(ServerLevel level, BlockPos origin) {
        int minX = HouseLayout.MIN_X;
        int maxX = HouseLayout.MAX_X;
        int minY = HouseLayout.MIN_Y - 1;
        int maxY = HouseLayout.MAX_Y;
        int minZ = HouseLayout.MIN_Z;
        int maxZ = HouseLayout.MAX_Z;

        Map<BlockState, Integer> palette = new HashMap<>();
        StringBuilder line = new StringBuilder();

        log("BEGIN|" + minX + "|" + minY + "|" + minZ + "|"
                + (maxX - minX + 1) + "|" + (maxY - minY + 1) + "|" + (maxZ - minZ + 1));

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                line.setLength(0);
                for (int x = minX; x <= maxX; x++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    Integer index = palette.get(state);
                    if (index == null) {
                        index = palette.size();
                        palette.put(state, index);
                        log("P|" + index + "|" + describe(state));
                    }
                    line.append(DIGITS.charAt(index / 62)).append(DIGITS.charAt(index % 62));
                }
                log("L|" + y + "|" + z + "|" + line);
            }
        }

        AABB bounds = new AABB(
                origin.getX() + minX, origin.getY() + minY, origin.getZ() + minZ,
                origin.getX() + maxX + 1, origin.getY() + maxY + 1, origin.getZ() + maxZ + 1
        );
        List<Painting> paintings = level.getEntitiesOfClass(Painting.class, bounds);
        for (Painting painting : paintings) {
            BlockPos pos = painting.getPos().subtract(origin);
            log("E|painting|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ() + "|"
                    + painting.getDirection().getSerializedName() + "|"
                    + painting.getVariant().unwrapKey().map(key -> key.location().getPath()).orElse("?"));
        }

        log("END");
    }

    private static String describe(BlockState state) {
        StringBuilder text = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
        if (!state.getValues().isEmpty()) {
            text.append('[');
            boolean first = true;
            for (var entry : state.getValues().entrySet()) {
                if (!first) {
                    text.append(',');
                }
                first = false;
                text.append(entry.getKey().getName()).append('=').append(entry.getValue());
            }
            text.append(']');
        }
        return text.toString();
    }

    private static void log(String message) {
        TheOldestHouse.LOGGER.info(PREFIX + message);
    }
}
