package io.github.knaitoe.theoldesthouse.house;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The manor's authored rugs, and the colours they trade overnight.
 *
 * The first night someone spends in the manor, pairs of rugs swap colours
 * between rooms: nothing is added or taken away, so a player can never be
 * sure it was not always this way. The landing and loft rugs stay as they
 * were, which makes the others harder to be certain about.
 */
public final class HouseRugs {
    /** A rectangle of carpet at one floor level, in house-relative coordinates. */
    public record Rug(String room, int x0, int y, int z0, int x1, int z1, Block authored, Block shifted) {
        public boolean contains(int x, int y, int z) {
            return y == this.y && x >= x0 && x <= x1 && z >= z0 && z <= z1;
        }
    }

    public static final Rug GREAT_ROOM = new Rug("great_room", 4, 1, 6, 6, 9, Blocks.BROWN_CARPET, Blocks.RED_CARPET);
    public static final Rug STUDY = new Rug("study", 5, 1, 21, 8, 23, Blocks.RED_CARPET, Blocks.LIGHT_BLUE_CARPET);
    public static final Rug PRINCIPAL_BEDROOM = new Rug("principal_bedroom", 4, 7, 3, 9, 5, Blocks.RED_CARPET, Blocks.BROWN_CARPET);
    public static final Rug LITERARY_BEDROOM = new Rug("literary_bedroom", 4, 7, 11, 9, 12, Blocks.LIGHT_BLUE_CARPET, Blocks.RED_CARPET);

    /** Every rug that changes colour. */
    public static final List<Rug> SHIFTING = List.of(GREAT_ROOM, STUDY, PRINCIPAL_BEDROOM, LITERARY_BEDROOM);

    private HouseRugs() {
    }

    /**
     * Changes every shifting rug to its other colour in {@code level}. Only
     * carpet still of the authored colour, where the house laid it, changes:
     * carpet a player has moved, replaced or put down stays theirs.
     *
     * @return how many carpet blocks changed
     */
    public static int shift(ServerLevel level, BlockPos origin) {
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Rug rug : SHIFTING) {
            for (int x = rug.x0(); x <= rug.x1(); x++) {
                for (int z = rug.z0(); z <= rug.z1(); z++) {
                    pos.set(origin.getX() + x, origin.getY() + rug.y(), origin.getZ() + z);
                    if (level.getBlockState(pos).is(rug.authored())) {
                        level.setBlock(pos, rug.shifted().defaultBlockState(), Block.UPDATE_CLIENTS);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }
}
