package io.github.knaitoe.theoldesthouse.house;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * Authored rugs and the alternate palettes the House gives them overnight.
 * Each rug keeps its weave and pattern when its colour changes.
 */
public final class HouseRugs {
    public record Rug(String room, int x0, int y, int z0, int x1, int z1,
                      Supplier<? extends Block> authored, Supplier<? extends Block> shifted) {
        public boolean contains(int x, int y, int z) {
            return y == this.y && x >= x0 && x <= x1 && z >= z0 && z <= z1;
        }
        public Block authoredBlock() { return authored.get(); }
        public Block shiftedBlock() { return shifted.get(); }
    }

    public static final Rug GREAT_ROOM = new Rug("great_room", 4, 1, 6, 6, 9,
            HouseBlocks.GREAT_ROOM_RUG_AUTHORED, HouseBlocks.GREAT_ROOM_RUG_SHIFTED);
    public static final Rug STUDY = new Rug("study", 5, 1, 21, 8, 23,
            HouseBlocks.STUDY_RUG_AUTHORED, HouseBlocks.STUDY_RUG_SHIFTED);
    public static final Rug PRINCIPAL_BEDROOM = new Rug("principal_bedroom", 4, 7, 3, 9, 5,
            HouseBlocks.PRINCIPAL_RUG_AUTHORED, HouseBlocks.PRINCIPAL_RUG_SHIFTED);
    public static final Rug LITERARY_BEDROOM = new Rug("literary_bedroom", 4, 7, 11, 9, 12,
            HouseBlocks.LITERARY_RUG_AUTHORED, HouseBlocks.LITERARY_RUG_SHIFTED);

    public static final List<Rug> SHIFTING = List.of(GREAT_ROOM, STUDY, PRINCIPAL_BEDROOM, LITERARY_BEDROOM);

    private HouseRugs() {}

    public static int shift(ServerLevel level, BlockPos origin) {
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Rug rug : SHIFTING) {
            for (int x = rug.x0(); x <= rug.x1(); x++) {
                for (int z = rug.z0(); z <= rug.z1(); z++) {
                    pos.set(origin.getX() + x, origin.getY() + rug.y(), origin.getZ() + z);
                    if (level.getBlockState(pos).is(rug.authoredBlock())) {
                        level.setBlock(pos, rug.shiftedBlock().defaultBlockState(), Block.UPDATE_CLIENTS);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }
}
