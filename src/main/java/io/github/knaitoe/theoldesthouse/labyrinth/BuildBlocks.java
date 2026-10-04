package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Ordered construction commands, executed only on the server thread in bounded slices. */
public final class BuildBlocks {
    public static final int MAX_VISITS = 4096;
    private static final long BUDGET_NANOS = 6_000_000L;
    private static Plan recording;
    private BuildBlocks() {}

    private interface Command {
        boolean step(ServerLevel level);
        default BlockState stateAt(BlockPos pos) { return null; }
    }

    private static final class Fill implements Command {
        private final BlockPos low, high;
        private final BlockState state;
        private final int flags, width, depth;
        private final long size;
        private long cursor;
        Fill(BlockPos a, BlockPos b, BlockState state, int flags) {
            low = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
            high = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
            this.state = state; this.flags = flags;
            width = high.getX() - low.getX() + 1; depth = high.getZ() - low.getZ() + 1;
            size = (long) width * depth * (high.getY() - low.getY() + 1);
        }
        public boolean step(ServerLevel level) {
            long column = cursor / width;
            BlockPos pos = low.offset((int)(cursor % width), (int)(column / depth), (int)(column % depth));
            if (!level.getBlockState(pos).equals(state)) level.setBlock(pos, state, flags);
            return ++cursor == size;
        }
        public BlockState stateAt(BlockPos pos) {
            return pos.getX() >= low.getX() && pos.getX() <= high.getX()
                    && pos.getY() >= low.getY() && pos.getY() <= high.getY()
                    && pos.getZ() >= low.getZ() && pos.getZ() <= high.getZ() ? state : null;
        }
    }

    public static final class Plan {
        private final ServerLevel level;
        private final List<Command> commands = new ArrayList<>();
        private int cursor;
        private int lastVisits;
        private Plan(ServerLevel level) { this.level = level; }
        /** A hard position limit also bounds slices on faster machines. */
        public boolean tick() {
            lastVisits = 0;
            long deadline = System.nanoTime() + BUDGET_NANOS;
            while (cursor < commands.size() && lastVisits < MAX_VISITS) {
                if (commands.get(cursor).step(level)) cursor++;
                lastVisits++;
                if ((lastVisits & 31) == 0 && System.nanoTime() >= deadline) break;
            }
            return cursor == commands.size();
        }
        public int lastVisits() { return lastVisits; }
    }

    public static Plan record(ServerLevel level, Runnable author) {
        if (recording != null) throw new IllegalStateException("Nested construction plan");
        Plan plan = new Plan(level);
        recording = plan;
        try { author.run(); } finally { recording = null; }
        return plan;
    }

    public static void box(ServerLevel level, BlockPos a, BlockPos b, BlockState state, int flags) {
        if (recording != null && recording.level == level) {
            recording.commands.add(new Fill(a, b, state, flags));
        } else {
            for (BlockPos pos : BlockPos.betweenClosed(a, b)) level.setBlock(pos, state, flags);
        }
    }

    public static void set(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        box(level, pos, pos, state, flags);
    }

    /** Read earlier authored commands when a later prop depends on them. */
    public static BlockState state(ServerLevel level, BlockPos pos) {
        if (recording != null && recording.level == level) {
            for (int i = recording.commands.size() - 1; i >= 0; i--) {
                BlockState state = recording.commands.get(i).stateAt(pos);
                if (state != null) return state;
            }
        }
        return level.getBlockState(pos);
    }

    /** Native books, sign text and actors are installed once, after their supporting blocks. */
    public static void after(ServerLevel level, Runnable action) {
        if (recording != null && recording.level == level) {
            recording.commands.add(new Command() {
                public boolean step(ServerLevel ignored) { action.run(); return true; }
            });
        } else action.run();
    }
}
